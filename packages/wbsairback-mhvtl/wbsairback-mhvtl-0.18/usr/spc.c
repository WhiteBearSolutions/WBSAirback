/*
 * This handles any SCSI OP codes defined in the standards as 'PRIMARY'
 *
 * Copyright (C) 2005 - 2009 Mark Harvey markh794 at gmail dot com
 *                                mark_harvey at symantec dot com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See comments in vtltape.c for a more complete version release...
 *
 */

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <dirent.h>
#include <syslog.h>
#include <ctype.h>
#include <inttypes.h>
#include <assert.h>
#include "be_byteshift.h"
#include "scsi.h"
#include "list.h"
#include "vtl_common.h"
#include "vtllib.h"

extern unsigned char sense[];

uint32_t SPR_Reservation_Generation;
uint8_t SPR_Reservation_Type;
uint64_t SPR_Reservation_Key;

struct vpd *alloc_vpd(uint16_t sz)
{
	struct vpd *vpd_pg = NULL;

	vpd_pg = malloc(sizeof(struct vpd) + sz);
	if (!vpd_pg)
		return NULL;
	memset(vpd_pg, 0, sizeof(struct vpd) + sz);
	vpd_pg->sz = sz;

	return vpd_pg;
}

#define INQUIRY_LEN 512

uint8_t spc_inquiry(struct scsi_cmd *cmd)
{
	int len = 0;
	struct vpd *vpd_pg;
	unsigned char key = ILLEGAL_REQUEST;
	uint16_t asc = E_INVALID_FIELD_IN_CDB;
	uint8_t *data = cmd->dbuf_p->data;
	uint8_t *cdb = cmd->scb;
	struct lu_phy_attr *lu = cmd->lu;

	MHVTL_DBG(1, "INQUIRY ** (%ld)", (long)cmd->dbuf_p->serialNo);

	if (((cdb[1] & 0x3) == 0x3) || (!(cdb[1] & 0x3) && cdb[2]))
		goto sense;

	memset(data, 0, INQUIRY_LEN);

	if (!(cdb[1] & 0x3)) {
		int i;
		uint16_t *desc;

		data[0] = lu->ptype;
		data[1] = (lu->removable) ? 0x80 : 0;
		data[2] = 5;	/* ANSI-Approved Version -> SPC-3 */
		data[3] = 0x02; /* Response Data Format (2) */
		data[7] = 0x00;

		memset(data + 8, 0x20, 28);
		memcpy(data + 8,  &lu->vendor_id, VENDOR_ID_LEN);
		memcpy(data + 16, &lu->product_id, PRODUCT_ID_LEN);
		memcpy(data + 32, &lu->product_rev, PRODUCT_REV_LEN);

		desc = (uint16_t *)(data + 58);
		for (i = 0; i < ARRAY_SIZE(lu->version_desc); i++)
			*desc++ = htons(lu->version_desc[i]);

		len = 66;
		data[4] = len - 5;	/* Additional Length */

	} else if (cdb[1] & 0x2) {
		/* CmdDt bit is set */
		/* We do not support it now. */
		data[1] = 0x1;
		data[5] = 0;
		len = 6;

	} else if (cdb[1] & 0x1) {
		uint8_t pcode = cdb[2];

		MHVTL_DBG(2, "Page code 0x%02x\n", pcode);

		if (pcode == 0x00) {
			uint8_t *p;
			int i, cnt;

			data[0] = lu->ptype;
			data[1] = 0;
			data[2] = 0;

			cnt = 1;
			p = data + 5;
			for (i = 0; i < ARRAY_SIZE(lu->lu_vpd); i++) {
				if (lu->lu_vpd[i]) {
					*p++ = i | 0x80;
					cnt++;
				}
			}
			data[3] = cnt;
			data[4] = 0x0;
			len = cnt + 4;
		} else if (lu->lu_vpd[PCODE_OFFSET(pcode)]) {
			vpd_pg = lu->lu_vpd[PCODE_OFFSET(pcode)];

			MHVTL_DBG(2, "Found page 0x%x\n", pcode);

			data[0] = lu->ptype;
			data[1] = pcode;
			data[2] = (vpd_pg->sz >> 8);
			data[3] = vpd_pg->sz & 0xff;
			memcpy(&data[4], vpd_pg->data, vpd_pg->sz);
			len = vpd_pg->sz + 4;
		}
	}
	cmd->dbuf_p->sz = len;
	return SAM_STAT_GOOD;

sense:
	mkSenseBuf(key, asc, &cmd->dbuf_p->sam_stat);
	return SAM_STAT_CHECK_CONDITION;
}

#ifdef MHVTL_DEBUG
/*
 * Process PERSITENT RESERVE OUT scsi command
 * Returns 0 if OK
 *         or -1 on failure.
 */
static char *type_str[] = {
	"Obsolete",
	"Write exclusive",
	"Obsolete",
	"Exclusive access",
	"Obsolete",
};

static char *type_unknown = "Undefined";

static char *lookup_type(uint8_t type)
{
	if (type > 4)
		return type_unknown;
	else
		return type_str[type];
}

static char *serv_action_str[] = {
	 "Register",
	 "Reserve",
	 "Release",
	 "Clear",
	 "Preempt",
	 "Preempt & abort",
	 "Register & ignore existing key",
	 "Register & move",
};
static char *sa_unknown = "Undefined";

static char *lookup_sa(uint8_t sa)
{
	if (sa > 7)
		return sa_unknown;
	else
		return serv_action_str[sa];
}
#endif

#define SPR_EXCLUSIVE_ACCESS 3
uint8_t resp_spc_pro(uint8_t *cdb, struct vtl_ds *dbuf_p)
{
	uint64_t RK;
	uint64_t SARK;
	uint16_t SA;
	uint8_t TYPE;
	uint8_t *sam_stat = &dbuf_p->sam_stat;
	uint8_t *buf = dbuf_p->data;

	if (dbuf_p->sz != 24) {
		mkSenseBuf(ILLEGAL_REQUEST, E_PARAMETER_LIST_LENGTH_ERR, sam_stat);
		return SAM_STAT_CHECK_CONDITION;
	}

	*sam_stat = SAM_STAT_GOOD;

	SA = cdb[1] & 0x1f;
	TYPE = cdb[2] & 0x0f;

	RK = get_unaligned_be64(&buf[0]);
	SARK = get_unaligned_be64(&buf[8]);

	MHVTL_DBG(2, "Key 0x%.8x %.8x SA Key 0x%.8x %.8x "
			"Service Action: %s, Type: %s\n",
			(uint32_t)(RK >> 32) & 0xffffffff,
			(uint32_t) RK & 0xffffffff,
			(uint32_t)(SARK >> 32) & 0xffffffff,
			(uint32_t)SARK & 0xffffffff,
			lookup_sa(SA), lookup_type(TYPE));
	MHVTL_DBG(2, "Reservation key was: 0x%.8x 0x%.8x\n",
			(uint32_t)(SPR_Reservation_Key >> 32) & 0xffffffff,
			(uint32_t)(SPR_Reservation_Key & 0xffffffff));

	switch(SA) {
	case 0: /* REGISTER */
		if (SPR_Reservation_Key) {
			if (RK == SPR_Reservation_Key) {
				if (SARK) {
					SPR_Reservation_Key = SARK;
				} else {
					SPR_Reservation_Key = 0UL;
					SPR_Reservation_Type = 0;
				}
				SPR_Reservation_Generation++;
			} else {
				*sam_stat = SAM_STAT_RESERVATION_CONFLICT;
			}
		} else {
			if (RK) {
				*sam_stat = SAM_STAT_RESERVATION_CONFLICT;
			} else {
				SPR_Reservation_Key = SARK;
				SPR_Reservation_Generation++;
			}
		}
		break;
	case 1: /* RESERVE */
		if (SPR_Reservation_Key)
			if (RK == SPR_Reservation_Key)
				if (TYPE == SPR_EXCLUSIVE_ACCESS) {
					SPR_Reservation_Type = TYPE;
					break;
				}
		*sam_stat = SAM_STAT_RESERVATION_CONFLICT;
		break;
	case 2: /* RELEASE */
		if (SPR_Reservation_Key)
			if (RK == SPR_Reservation_Key)
				if (TYPE == SPR_EXCLUSIVE_ACCESS) {
					SPR_Reservation_Type = 0;
					break;
				}
		*sam_stat = SAM_STAT_RESERVATION_CONFLICT;
		break;
	case 3: /* CLEAR */
		if (!SPR_Reservation_Key && !SPR_Reservation_Key) {
			*sam_stat = SAM_STAT_RESERVATION_CONFLICT;
		} else {
			if (RK == SPR_Reservation_Key) {
				SPR_Reservation_Key = 0UL;
				SPR_Reservation_Type = 0;
				SPR_Reservation_Generation++;
			} else {
				*sam_stat = SAM_STAT_RESERVATION_CONFLICT;
			}
		}
		break;
	case 4: /* PREEMT */
	case 5: /* PREEMPT AND ABORT */
		/* this is pretty weird,
		 * in that we can only have a single key registered,
		 * so preempt is pretty simplified */
		if ((!SPR_Reservation_Key) && (!RK) && (!SARK)) {
			*sam_stat = SAM_STAT_RESERVATION_CONFLICT;
		} else {
			if (SPR_Reservation_Type) {
				if (SARK == SPR_Reservation_Key) {
					SPR_Reservation_Key = RK;
					SPR_Reservation_Type = TYPE;
					SPR_Reservation_Generation++;
				}
			} else {
				if (SARK == SPR_Reservation_Key) {
					SPR_Reservation_Key = 0UL;
					SPR_Reservation_Generation++;
				}
			}
		}

		break;
	case 6: /* REGISTER AND IGNORE EXISTING KEY */
		if (SPR_Reservation_Key) {
			if (SARK) {
				SPR_Reservation_Key = SARK;
			} else {
				SPR_Reservation_Key = 0UL;
				SPR_Reservation_Type = 0;
			}
		} else {
			SPR_Reservation_Key = SARK;
		}
		SPR_Reservation_Generation++;
		break;
	case 7: /* REGISTER AND MOVE */
		mkSenseBuf(ILLEGAL_REQUEST, E_INVALID_FIELD_IN_CDB, sam_stat);
		return SAM_STAT_CHECK_CONDITION;
		break;
	default:
		mkSenseBuf(ILLEGAL_REQUEST, E_INVALID_FIELD_IN_CDB, sam_stat);
		return SAM_STAT_CHECK_CONDITION;
		break;
	}
	MHVTL_DBG(2, "Reservation key now: 0x%.8x 0x%.8x\n",
			(uint32_t)(SPR_Reservation_Key >> 32) & 0xffffffff,
			(uint32_t)(SPR_Reservation_Key & 0xffffffff));
	return *sam_stat;
}

/*
 * Process PERSITENT RESERVE IN scsi command
 */
uint8_t resp_spc_pri(uint8_t *cdb, struct vtl_ds *dbuf_p)
{
	uint16_t alloc_len;
	uint16_t SA;
	uint8_t *buf = dbuf_p->data;
	uint8_t *sam_stat = &dbuf_p->sam_stat;
	uint8_t sam_status;

	SA = cdb[1] & 0x1f;

	alloc_len = get_unaligned_be16(&cdb[7]);

	memset(buf, 0, alloc_len);	// Clear memory

	MHVTL_DBG(1, "service action: %d\n", SA);

	sam_status = SAM_STAT_GOOD;
	switch(SA) {
	case 0: /* READ KEYS */
		put_unaligned_be32(SPR_Reservation_Generation, &buf[0]);
		if (!SPR_Reservation_Key) {
			dbuf_p->sz = 8;
			break;
		}
		buf[7] = 8;
		put_unaligned_be64(SPR_Reservation_Key, &buf[8]);
		dbuf_p->sz = 16;
		break;
	case 1: /* READ RESERVATON */
		put_unaligned_be32(SPR_Reservation_Generation, &buf[0]);
		if (!SPR_Reservation_Type) {
			dbuf_p->sz = 8;
			break;
		}
		buf[7] = 16;
		put_unaligned_be64(SPR_Reservation_Key, &buf[8]);
		buf[21] = SPR_Reservation_Type;
		dbuf_p->sz = 24;
		break;
	case 2: /* REPORT CAPABILITIES */
		buf[1] = 8;
		buf[2] = 0x10;
		buf[3] = 0x80;
		buf[4] = 0x08;
		dbuf_p->sz = 8;
		break;
	case 3: /* READ FULL STATUS */
	default:
		dbuf_p->sz = 0;
		mkSenseBuf(ILLEGAL_REQUEST, E_INVALID_FIELD_IN_CDB, sam_stat);
		sam_status = SAM_STAT_CHECK_CONDITION;
		break;
	}
	return sam_status;
}

uint8_t spc_tur(struct scsi_cmd *cmd)
{
	MHVTL_DBG(1, "** %s (%ld) %s **", "Test Unit Ready : Returning => ",
				(long)cmd->dbuf_p->serialNo,
				(cmd->lu->online) ? "Online" : "Offline");
	if (cmd->lu->online)
		return SAM_STAT_GOOD;

	mkSenseBuf(NOT_READY, NO_ADDITIONAL_SENSE, &cmd->dbuf_p->sam_stat);
	return SAM_STAT_CHECK_CONDITION;
}

uint8_t spc_illegal_op(struct scsi_cmd *cmd)
{
	MHVTL_DBG(1, "Unsupported OP CODE");
	mkSenseBuf(ILLEGAL_REQUEST, E_INVALID_OP_CODE, &cmd->dbuf_p->sam_stat);
	return SAM_STAT_CHECK_CONDITION;
}

uint8_t spc_request_sense(struct scsi_cmd *cmd)
{
	int sz;
	uint8_t *sense_buf = cmd->dbuf_p->sense_buf;
	uint8_t *cdb = cmd->scb;

	MHVTL_DBG(1, "Request Sense (%ld) : key/ASC/ASCQ "
			"[0x%02x 0x%02x 0x%02x]"
			" Filemark: %s, EOM: %s, ILI: %s",
				(long)cmd->dbuf_p->serialNo,
				sense_buf[2] & 0x0f,
				sense_buf[12],
				sense_buf[13],
				(sense_buf[2] & SD_FILEMARK) ? "yes" : "no",
				(sense_buf[2] & SD_EOM) ? "yes" : "no",
				(sense_buf[2] & SD_ILI) ? "yes" : "no");

	sz = cdb[4] < SENSE_BUF_SIZE ?  cdb[4] : SENSE_BUF_SIZE;
	assert(cmd->dbuf_p->data);
	/* Clear out the request sense flag */
	cmd->dbuf_p->sam_stat = 0;
	/* set buf size */
	cmd->dbuf_p->sz = sz;
	memcpy(cmd->dbuf_p->data, sense_buf, cmd->dbuf_p->sz);
	memset(sense_buf, 0, cmd->dbuf_p->sz);
	sense_buf[0] = SD_CURRENT_INFORMATION_FIXED;
	return SAM_STAT_GOOD;
}

/*
 * Log Select
 *
 * Set the logs to a known state.
 *
 * Currently a no-op
 */
static char LOG_SELECT_00[] = "Current threshold values";
static char LOG_SELECT_01[] = "Current cumulative values";
static char LOG_SELECT_10[] = "Default threshold values";
static char LOG_SELECT_11[] = "Default cumulative values";

uint8_t spc_log_select(struct scsi_cmd *cmd)
{
	uint8_t *cdb = cmd->scb;
	uint8_t *sam_stat = &cmd->dbuf_p->sam_stat;
	char pcr = cdb[1] & 0x1;
	uint16_t parmList;
	char *parmString = "Undefined";

	parmList = ntohs((uint16_t)cdb[7]); /* bytes 7 & 8 are parm list. */

	MHVTL_DBG(1, "LOG SELECT (%ld) %s",
				(long)cmd->dbuf_p->serialNo,
				(pcr) ? ": Parameter Code Reset **" : "**");

	if (pcr) {	/* Check for Parameter code reset */
		if (parmList) {	/* If non-zero, error */
			mkSenseBuf(ILLEGAL_REQUEST, E_INVALID_FIELD_IN_CDB,
						sam_stat);
			return SAM_STAT_CHECK_CONDITION;
		}
		switch ((cdb[2] & 0xc0) >> 5) {
		case 0:
			parmString = LOG_SELECT_00;
			break;
		case 1:
			parmString = LOG_SELECT_01;
			break;
		case 2:
			parmString = LOG_SELECT_10;
			break;
		case 3:
			parmString = LOG_SELECT_11;
			break;
		}
		MHVTL_DBG(1, "  %s", parmString);
	}
	return SAM_STAT_GOOD;
}

/*
 * Process the MODE_SELECT command
 */
uint8_t spc_mode_select(struct scsi_cmd *cmd)
{
	MHVTL_DBG(1, "MODE SELECT (%ld) **", (long)cmd->dbuf_p->serialNo);

	cmd->dbuf_p->sz = 0;
	return SAM_STAT_GOOD;
}

/*
 * Build mode sense data into *buf
 * Return SAM STATUS
 */
uint8_t spc_mode_sense(struct scsi_cmd *cmd)
{
	int pcontrol, pcode, subpcode;
	int media_type;
	int alloc_len, msense_6;
	int dev_spec, len = 0;
	int offset = 0;
	uint8_t *ap;
	struct mode *smp;	/* Struct mode pointer... */
	int a;
	int WriteProtect = 0;

	uint8_t *buf = cmd->dbuf_p->data;
	uint8_t *scb = cmd->scb;
	uint8_t *sam_stat = &cmd->dbuf_p->sam_stat;
	struct mode *m = cmd->lu->mode_pages;

#ifdef MHVTL_DEBUG
	char *pcontrolString[] = {
		"Current configuration",
		"Every bit that can be modified",
		"Power-on configuration",
		"Power-on configuration"
	};
#endif

	/* Disable Block Descriptors */
	uint8_t blockDescriptorLen = (scb[1] & 0x8) ? 0 : 8;

	MHVTL_DBG(1, "MODE SENSE (%ld) **", (long)cmd->dbuf_p->serialNo);

	/*
	 pcontrol => page control
		00 -> 0: Report current vaules
		01 -> 1: Report Changable Vaules
		10 -> 2: Report default values
		11 -> 3: Report saved values
	*/
	pcontrol = (scb[2] & 0xc0) >> 6;
	/* pcode -> Page Code */
	pcode = scb[2] & 0x3f;
	subpcode = scb[3];
	msense_6 = (MODE_SENSE == scb[0]);

	alloc_len = msense_6 ? scb[4] : ((scb[7] << 8) | scb[8]);
	offset = msense_6 ? 4 : 8;

	MHVTL_DBG(2, " Mode Sense %d byte version", (msense_6) ? 6 : 10);
	MHVTL_DBG(2, " Page Control  : %s(0x%x)",
				pcontrolString[pcontrol], pcontrol);
	MHVTL_DBG(2, " Page Code     : 0x%x", pcode);
	MHVTL_DBG(2, " Disable Block Descriptor => %s",
				(blockDescriptorLen) ? "No" : "Yes");
	MHVTL_DBG(2, " Allocation len: %d", alloc_len);

	if (0x3 == pcontrol) {  /* Saving values not supported */
		mkSenseBuf(ILLEGAL_REQUEST, E_SAVING_PARMS_UNSUP, sam_stat);
		return SAM_STAT_CHECK_CONDITION;
	}

	memset(buf, 0, alloc_len);	/* Set return data to null */
	dev_spec = (WriteProtect ? 0x80 : 0x00) | 0x10;
	media_type = 0x0;

	offset += blockDescriptorLen;
	ap = buf + offset;

	if (0 != subpcode) { /* TODO: Control Extension page */
		MHVTL_DBG(1, "Non-zero sub-page sense code not supported");
		mkSenseBuf(ILLEGAL_REQUEST, E_INVALID_FIELD_IN_CDB, sam_stat);
		return SAM_STAT_CHECK_CONDITION;
	}

	MHVTL_DBG(3, "pcode: 0x%02x", pcode);

	if (0x0 == pcode) {
		len = 0;
	} else if (0x3f == pcode) {	/* Return all pages */
		for (a = 1; a < 0x3f; a++) { /* Walk thru all possibilities */
			smp = find_pcode(m, a, 0);
			if (smp)
				len += add_pcode(smp, (uint8_t *)ap + len);
		}
	} else {
		smp = find_pcode(m, pcode, 0);
		if (smp)
			len = add_pcode(smp, (uint8_t *)ap);
	}
	offset += len;

	if (pcode != 0)	/* 0 = No page code requested */
		if (0 == len) {	/* Page not found.. */
		MHVTL_DBG(2, "Unknown mode page : %d", pcode);
		mkSenseBuf(ILLEGAL_REQUEST, E_INVALID_FIELD_IN_CDB, sam_stat);
		return SAM_STAT_CHECK_CONDITION;
		}

	/* Fill in header.. */
	if (msense_6) {
		buf[0] = offset - 1;	/* size - sizeof(buf[0]) field */
		buf[1] = media_type;
		buf[2] = dev_spec;
		buf[3] = blockDescriptorLen;
		/* If the length > 0, copy Block Desc. */
		if (blockDescriptorLen)
			memcpy(&buf[4],blockDescriptorBlock,blockDescriptorLen);
	} else {
		put_unaligned_be16(offset - 2, &buf[0]);
		buf[2] = media_type;
		buf[3] = dev_spec;
		put_unaligned_be16(blockDescriptorLen, &buf[6]);
		/* If the length > 0, copy Block Desc. */
		if (blockDescriptorLen)
			memcpy(&buf[8],blockDescriptorBlock,blockDescriptorLen);
	}

	if (debug) {
		printf("mode sense: Returning %d bytes\n", offset);
		hex_dump(buf, offset);
	}

	cmd->dbuf_p->sz = offset;

	return SAM_STAT_GOOD;
}

uint8_t spc_release(struct scsi_cmd *cmd)
{
	MHVTL_DBG(1, "RELEASE UNIT (%ld) **", (long)cmd->dbuf_p->serialNo);
	return SAM_STAT_GOOD;
}

uint8_t spc_reserve(struct scsi_cmd *cmd)
{
	MHVTL_DBG(1, "RESERVE UNIT (%ld) **", (long)cmd->dbuf_p->serialNo);
	return SAM_STAT_GOOD;
}

uint8_t spc_send_diagnostics(struct scsi_cmd *cmd)
{
	MHVTL_DBG(1, "SEND DIAGNOSTICS (%ld) **", (long)cmd->dbuf_p->serialNo);
	return SAM_STAT_GOOD;
}

uint8_t spc_recv_diagnostics(struct scsi_cmd *cmd)
{
	MHVTL_DBG(1, "Receive Diagnostic (%ld) **",
						(long)cmd->dbuf_p->serialNo);
	return SAM_STAT_GOOD;
}

