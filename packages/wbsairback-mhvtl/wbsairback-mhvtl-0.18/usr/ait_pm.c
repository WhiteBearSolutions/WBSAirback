/*
 * This handles any SCSI OP codes defined in the standards as 'STREAM'
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
#include "ssc.h"
#include "spc.h"
#include "vtltape.h"
#include "q.h"

/*
 * Mode Pages defined for 'default'

 *** Minimum default requirements is **
 static struct mode sm[] = {
	{0x01, 0x00, 0x00, NULL, }, * RW error recovery - SSC3-8.3.5 *
	{0x02, 0x00, 0x00, NULL, }, * Disconnect Reconnect - SPC3 *
	{0x0a, 0x00, 0x00, NULL, }, * Control Extension - SPC3 *
	{0x0f, 0x00, 0x00, NULL, }, * Data Compression - SSC3-8.3.3
	{0x10, 0x00, 0x00, NULL, }, * Device config - SSC3-8.3.3
	{0x11, 0x00, 0x00, NULL, }, * Medium Partition - SSC3-8.3.4
	{0x1a, 0x00, 0x00, NULL, }, * Power condition - SPC3
	{0x1c, 0x00, 0x00, NULL, }, * Information Exception Ctrl SSC3-8.3.6
	{0x1d, 0x00, 0x00, NULL, }, * Medium configuration - SSC3-8.3.7
	{0x00, 0x00, 0x00, NULL, }, * NULL terminator
	};
 */

static struct mode sm[] = {
/*	Page,  subpage, len, 'pointer to data struct' */
	{0x01, 0x00, 0x00, NULL, }, /* RW error recovery - SSC3-8.3.5 */
	{0x02, 0x00, 0x00, NULL, }, /* Disconnect Reconnect - SPC3 */
	{0x0a, 0x00, 0x00, NULL, }, /* Control Extension - SPC3 */
	{0x0f, 0x00, 0x00, NULL, }, /* Data Compression - SSC3-8.3.3 */
	{0x10, 0x00, 0x00, NULL, }, /* Device config - SSC3-8.3.3 */
	{0x11, 0x00, 0x00, NULL, }, /* Medium Partition - SSC3-8.3.4 */
	{0x1a, 0x00, 0x00, NULL, }, /* Power condition - SPC3 */
	{0x1c, 0x00, 0x00, NULL, }, /* Information Exception Ctrl SSC3-8.3.6 */
	{0x1d, 0x00, 0x00, NULL, }, /* Medium configuration - SSC3-8.3.7 */
	{0x31, 0x00, 0x00, NULL, }, /* AIT Device Configuration */
	{0x00, 0x00, 0x00, NULL, }, /* NULL terminator */
	};

static struct media_handling ait1_media_handling[] = {
	{ "ait1", "RW", medium_density_code_ait1, },
	};

static struct media_handling ait2_media_handling[] = {
	{ "ait1", "RW", medium_density_code_ait1, },
	{ "ait2", "RW", medium_density_code_ait2, },
	};

/* FIXME: Need to check AIT-3 SPECs to see if they handle WORM & Encryption */
static struct media_handling ait3_media_handling[] = {
	{ "ait1", "RO", medium_density_code_ait1, },
	{ "ait2", "RW", medium_density_code_ait2, },
	{ "ait3", "RW", medium_density_code_ait3, },
	};

/* FIXME: Need to check AIT-4 SPECs to see if they handle WORM & Encryption */
static struct media_handling ait4_media_handling[] = {
	{ "ait2", "RO", medium_density_code_ait2, },
	{ "ait3", "RW", medium_density_code_ait3, },
	{ "ait4", "RW", medium_density_code_ait4, },
	{ "ait4", "WORM", medium_density_code_ait4, },
	{ "ait4", "ENCR", medium_density_code_ait4, },
	};

static uint8_t clear_ait_WORM(void)
{
	uint8_t *smp_dp;
	struct mode *smp;

	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);

	smp = find_pcode(sm, 0x31, 0);
	if (smp) {
		smp_dp = smp->pcodePointer;
		smp_dp[4] = 0x0;
	}

	return SAM_STAT_GOOD;
}

static uint8_t set_ait_WORM(void)
{
	uint8_t *smp_dp;
	struct mode *smp;

	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);

	smp = find_pcode(sm, 0x31, 0);
	if (smp) {
		smp_dp = smp->pcodePointer;
		smp_dp[4] = 0x40;
	}

	return SAM_STAT_GOOD;
}

/*
 * Initialise structure data for mode pages.
 * - Allocate memory for each mode page & init to 0
 * - Set up size of mode page
 * - Set initial values of mode pages
 *
 * Return void  - Nothing
 */
static void init_ait4_mode_pages(struct lu_phy_attr *lu, struct mode *m)
{
	struct mode *mp;

	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);

	mp = alloc_mode_page(m, 0x31, 0, 8);
	if (mp)
		mp->pcodePointer[2] = 0xf0;
		mp->pcodePointer[3] = 0x0a;
		mp->pcodePointer[4] = 0x40;
}

static uint8_t clear_ait_compression(void)
{
	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);
	/* default clear_compression is in libvtlscsi */
	return clear_compression_mode_pg(sm);
}

static uint8_t set_ait_compression(int lvl)
{
	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);
	/* default set_compression is in libvtlscsi */
	return set_compression_mode_pg(sm, lvl);
}

static uint8_t update_ait_encryption_mode(void *p, int value)
{
	MHVTL_DBG(3, "+++ Trace +++");

	return SAM_STAT_GOOD;
}

static void init_ait_inquiry(struct lu_phy_attr *lu)
{
	int pg;
	uint8_t worm = 1;	/* Supports WORM */
	uint8_t local_TapeAlert[8] =
			{ 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };

	/* Sequential Access device capabilities - Ref: 8.4.2 */
	pg = 0xb0 & 0x7f;
	lu->lu_vpd[pg] = alloc_vpd(VPD_B0_SZ);
	lu->lu_vpd[pg]->vpd_update = update_vpd_b0;
	lu->lu_vpd[pg]->vpd_update(lu, &worm);

	/* Manufacture-assigned serial number - Ref: 8.4.3 */
	pg = 0xb1 & 0x7f;
	lu->lu_vpd[pg] = alloc_vpd(VPD_B1_SZ);
	lu->lu_vpd[pg]->vpd_update = update_vpd_b1;
	lu->lu_vpd[pg]->vpd_update(lu, lu->lu_serial_no);

	/* TapeAlert supported flags - Ref: 8.4.4 */
	pg = 0xb2 & 0x7f;
	lu->lu_vpd[pg] = alloc_vpd(VPD_B2_SZ);
	lu->lu_vpd[pg]->vpd_update = update_vpd_b2;
	lu->lu_vpd[pg]->vpd_update(lu, &local_TapeAlert);

	/* VPD page 0xC0 */
	pg = 0xc0 & 0x7f;
	lu->lu_vpd[pg] = alloc_vpd(VPD_C0_SZ);
	lu->lu_vpd[pg]->vpd_update = update_vpd_c0;
	lu->lu_vpd[pg]->vpd_update(lu, "10-03-2008 19:38:00");

	/* VPD page 0xC1 */
	pg = 0xc1 & 0x7f;
	lu->lu_vpd[pg] = alloc_vpd(strlen("Security"));
	lu->lu_vpd[pg]->vpd_update = update_vpd_c1;
	lu->lu_vpd[pg]->vpd_update(lu, "Security");
}

/* Dummy routine. Always return false */
static int ait_kad_validation(int mode, int ukad, int akad)
{
	return FALSE;
}

/* Some comments before I forget how this is supose to work..
 - cleaning_media_state is either
   0 - Not mounted
   1 - Cleaning media mounted -> return Cleaning cartridge installed
   2 - Cleaning media mounted -> return Cause not reportable
   3 - Cleaning media mounted -> return Initializing command required

 On cleaning media mount, ait_cleaning() is called which:
   Sets a pointer from priv_lu_ssc -> cleaning_media_state.
   Sets cleaning_media_state to 1.
   Sets a 30 second timer to call inc_cleaning_state()

 inc_cleaning_state()
   Increments cleaning_media_state.
   If cleaning media_state == 2, set another timer for 90 seconds to again
   call inc_cleaning_state.

 If the application issues a TUR, ssc_tur() will return one of the
 above status codes depending on the current value of cleaning_media_state.

 When the cleaning media is unmounted, the pointer in priv_lu_ssc to this
 var will be re-set to NULL so the ssc_tur() will return defautl value.

 */
static volatile sig_atomic_t cleaning_media_state;

static void inc_cleaning_state(int sig);

static void set_cleaning_timer(int t)
{
	MHVTL_DBG(3, "+++ Trace +++ Setting alarm for %d", t);
	signal(SIGALRM, inc_cleaning_state);
	alarm(t);
}

static void inc_cleaning_state(int sig)
{
	MHVTL_DBG(3, "+++ Trace +++");
	signal(sig, inc_cleaning_state);

	cleaning_media_state++;

	if (cleaning_media_state == CLEAN_MOUNT_STAGE2)
		set_cleaning_timer(90);
}

static uint8_t ait_cleaning(void *ssc_priv)
{
	struct priv_lu_ssc *ssc;

	MHVTL_DBG(3, "+++ Trace +++");

	ssc = ssc_priv;

	ssc->cleaning_media_state = &cleaning_media_state;
	cleaning_media_state = CLEAN_MOUNT_STAGE1;

	set_cleaning_timer(30);

	return 0;
}

/* load set to 1 for load, 0 for unload */
static uint8_t ait_media_load(int load)
{
	uint8_t *smp_dp;
	struct mode *smp;

	MHVTL_DBG(3, "+++ Trace +++");

	smp = find_pcode(sm, 0x31, 0);
	if (smp) {
		smp_dp = smp->pcodePointer;
		if (load)
			smp_dp[3] = 0x0a;	/* SPAN -> Set to 0Ah on load */
		else
			smp_dp[3] = 0x0;	/* SPAN -> Set to 0 on unload */
	}

	return 0;
}

static char *name_ait_1 = "AIT";
static char *name_ait_2 = "AIT-2";
static char *name_ait_3 = "AIT-3";
static char *name_ait_4 = "AIT-4";

static struct ssc_personality_template ssc_pm = {
	.valid_encryption_blk	= valid_encryption_blk,
	.update_encryption_mode	= update_ait_encryption_mode,
	.kad_validation		= ait_kad_validation,
	.check_restrictions	= check_restrictions,
	.clear_compression	= clear_ait_compression,
	.set_compression	= set_ait_compression,
	.media_load		= ait_media_load,
	.cleaning_media		= ait_cleaning,
};

void init_ait1_ssc(struct lu_phy_attr *lu)
{
	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);

	init_ait_inquiry(lu);
	ssc_pm.name = name_ait_1;
	ssc_pm.drive_native_density = medium_density_code_ait1;
	ssc_pm.media_capabilities = ait1_media_handling;
	personality_module_register(&ssc_pm);
	init_default_ssc_mode_pages(sm);	/* init default mode pages */
	lu->mode_pages = sm;
}

void init_ait2_ssc(struct lu_phy_attr *lu)
{
	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);

	init_ait_inquiry(lu);
	ssc_pm.name = name_ait_2;
	ssc_pm.drive_native_density = medium_density_code_ait2;
	ssc_pm.media_capabilities = ait2_media_handling;
	personality_module_register(&ssc_pm);
	init_default_ssc_mode_pages(sm);	/* init default mode pages */
	lu->mode_pages = sm;
}

void init_ait3_ssc(struct lu_phy_attr *lu)
{
	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);

	init_ait_inquiry(lu);
	ssc_pm.name = name_ait_3;
	ssc_pm.drive_native_density = medium_density_code_ait3;
	ssc_pm.media_capabilities = ait3_media_handling;
	personality_module_register(&ssc_pm);
	init_default_ssc_mode_pages(sm);	/* init default mode pages */
	lu->mode_pages = sm;
}

void init_ait4_ssc(struct lu_phy_attr *lu)
{
	MHVTL_DBG(3, "+++ Trace mode pages at %p +++", sm);

	init_ait_inquiry(lu);
	ssc_pm.name = name_ait_4;
	ssc_pm.drive_native_density = medium_density_code_ait4;
	ssc_pm.media_capabilities = ait4_media_handling;
	ssc_pm.clear_WORM	= clear_ait_WORM,
	ssc_pm.set_WORM		= set_ait_WORM,
	personality_module_register(&ssc_pm);
	init_default_ssc_mode_pages(sm);	/* init default mode pages */
	init_ait4_mode_pages(lu, sm);	/* init AIT uniq mode pages */
	lu->mode_pages = sm;
	((struct priv_lu_ssc *)lu->lu_private)->capacity_unit = 1L << 10; /* Capacity units in KBytes */
	register_ops(lu, SECURITY_PROTOCOL_IN, ssc_spin);
	register_ops(lu, SECURITY_PROTOCOL_OUT, ssc_spout);
}

