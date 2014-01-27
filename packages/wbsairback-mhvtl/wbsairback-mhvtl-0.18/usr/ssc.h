
#include <signal.h>

#define ENCR_C	1	/* Device supports Encryption */
#define ENCR_E	4	/* Encryption is enabled */

#define ENCR_IN_SUPPORT_PAGES		0
#define ENCR_OUT_SUPPORT_PAGES		1
#define ENCR_CAPABILITIES		0x10
#define ENCR_KEY_FORMATS		0x11
#define ENCR_KEY_MGT_CAPABILITIES	0x12
#define ENCR_DATA_ENCR_STATUS		0x20
#define ENCR_NEXT_BLK_ENCR_STATUS	0x21

#define ENCR_SET_DATA_ENCRYPTION	0x10

struct media_handling {
	char media_type[16];
	char op[8];
	unsigned char density;
};

struct ssc_personality_template {
	char *name;
	int drive_native_density;
	int drive_type;
	struct media_handling *media_capabilities;

	/* Read check if this block contains valid encryption keys */
	uint8_t (*valid_encryption_blk)(struct scsi_cmd *cmd);

	/* Write check if the media supports encryption */
	uint8_t (*valid_encryption_media)(struct scsi_cmd *cmd);

	/* SPIN page 20 -> Encryption Capabilities */
	int (*encryption_capabilities)(struct scsi_cmd *cmd);

	/* SPOUT -> Validation KAD info is correct */
	int (*kad_validation)(int encrypt_mode, int akad, int ukad);

	/* Update mode page for encryption capabilities */
	uint8_t (*update_encryption_mode)(void *p, int mode);


	/* Media access, check if any write restrictions */
	uint8_t (*check_restrictions)(struct scsi_cmd *cmd);
	/* enable/disable compression */
	uint8_t (*clear_compression)(void);
	uint8_t (*set_compression)(int level);
	/* enable/disable WORM */
	uint8_t (*clear_WORM)(void);
	uint8_t (*set_WORM)(void);

	/* Cleaning media mount calls into here */
	uint8_t (*cleaning_media)(void *priv);

	/* Called on load/unload - where var load = 0 on unload, 1 on load */
	uint8_t (*media_load)(int load);
};

/* Load capabilities - density_status bits */
#define	LOAD_INVALID		1
#define	LOAD_RW			2
#define	LOAD_RO			4
#define	LOAD_WORM		8
#define	LOAD_ENCRYPT		0x10
#define	LOAD_FAIL		0x20
#define LOAD_CLEANING		0x40

#define CLEAN_MOUNT_STAGE1	1
#define CLEAN_MOUNT_STAGE2	2
#define CLEAN_MOUNT_STAGE3	3

struct media_details {
	struct list_head siblings;
	unsigned int media_type;	/* Media Type */
	unsigned int density_status;	/* RO, RW, invalid or fail mount */
};

struct priv_lu_ssc {
/* Variables for simple, single initiator, SCSI Reservation system */
	int I_am_SPC_2_Reserved;

	int bufsize;
	int tapeLoaded;
	int inLibrary;
	uint8_t sam_status;

	/* True if virtual "write protect" switch is set */
	uint8_t MediaWriteProtect;

	/* Default value read from config file */
	uint8_t configCompressionFactor;
	uint8_t configCompressionEnabled;

	loff_t capacity_unit;

	/* Pointer into Device config mode page */
	uint8_t *compressionFactor;

	int *OK_2_write;
	struct MAM *mamp;

	uint64_t bytesRead;
	uint64_t bytesWritten;

	struct blk_header *c_pos;

	uint32_t KEY_INSTANCE_COUNTER;
	uint32_t DECRYPT_MODE;
	uint32_t ENCRYPT_MODE;
	struct encryption *encr;
	struct encryption *cryptop;

	unsigned char mediaSerialNo[34];

	/* cleaning_media_state -  Only used for cleaning media status..
		Using signal / alarm which will increment this value thru:
			0/NULL (unmonted)
			1 (mounted) -> sense: "Cleaning cartridge installed"
			2 (mounted) -> sense: "Logical unit not ready"
			3 (mounted) -> sense: "Cause not reportable"
	 */
	volatile sig_atomic_t *cleaning_media_state;

	struct ssc_personality_template *pm;	/* Personality Module */
};

void personality_module_register(struct ssc_personality_template *pm);

int readBlock(uint8_t *buf, uint32_t request_sz, int sili, uint8_t *sam_stat);
int writeBlock(struct scsi_cmd *cmd, uint32_t request_sz);

uint8_t ssc_a3_service_action(struct scsi_cmd *cmd);
uint8_t ssc_a4_service_action(struct scsi_cmd *cmd);
uint8_t ssc_allow_overwrite(struct scsi_cmd *cmd);
uint8_t ssc_allow_prevent_removal(struct scsi_cmd *cmd);
uint8_t ssc_erase(struct scsi_cmd *cmd);
uint8_t ssc_format_media(struct scsi_cmd *cmd);
uint8_t ssc_load_display(struct scsi_cmd *cmd);
uint8_t ssc_mode_select(struct scsi_cmd *cmd);
uint8_t ssc_pr_in(struct scsi_cmd *cmd);
uint8_t ssc_pr_out(struct scsi_cmd *cmd);
uint8_t ssc_read_6(struct scsi_cmd *cmd);
uint8_t ssc_read_attributes(struct scsi_cmd *cmd);
uint8_t ssc_read_block_limits(struct scsi_cmd *cmd);
uint8_t ssc_read_display(struct scsi_cmd *cmd);
uint8_t ssc_read_media_sn(struct scsi_cmd *cmd);
uint8_t ssc_read_position(struct scsi_cmd *cmd);
uint8_t ssc_release(struct scsi_cmd *cmd);
uint8_t ssc_report_density_support(struct scsi_cmd *cmd);
uint8_t ssc_report_luns(struct scsi_cmd *cmd);
uint8_t ssc_reserve(struct scsi_cmd *cmd);
uint8_t ssc_rewind(struct scsi_cmd *cmd);
uint8_t ssc_seek_10(struct scsi_cmd *cmd);
uint8_t ssc_space(struct scsi_cmd *cmd);
uint8_t ssc_spin(struct scsi_cmd *cmd);
uint8_t ssc_spout(struct scsi_cmd *cmd);
uint8_t ssc_load_unload(struct scsi_cmd *cmd);
uint8_t ssc_tur(struct scsi_cmd *cmd);
uint8_t ssc_write_6(struct scsi_cmd *cmd);
uint8_t ssc_write_attributes(struct scsi_cmd *cmd);
uint8_t ssc_write_filemarks(struct scsi_cmd *cmd);

void init_ait1_ssc(struct lu_phy_attr *lu);
void init_ait2_ssc(struct lu_phy_attr *lu);
void init_ait3_ssc(struct lu_phy_attr *lu);
void init_ait4_ssc(struct lu_phy_attr *lu);
void init_default_ssc(struct lu_phy_attr *lu);
void init_t10kA_ssc(struct lu_phy_attr *lu);
void init_t10kB_ssc(struct lu_phy_attr *lu);
void init_t10kC_ssc(struct lu_phy_attr *lu);
void init_ult3580_td1(struct lu_phy_attr *lu);
void init_ult3580_td2(struct lu_phy_attr *lu);
void init_ult3580_td3(struct lu_phy_attr *lu);
void init_ult3580_td4(struct lu_phy_attr *lu);
void init_ult3580_td5(struct lu_phy_attr *lu);
void init_hp_ult_1(struct lu_phy_attr *lu);
void init_hp_ult_2(struct lu_phy_attr *lu);
void init_hp_ult_3(struct lu_phy_attr *lu);
void init_hp_ult_4(struct lu_phy_attr *lu);
void init_hp_ult_5(struct lu_phy_attr *lu);
void init_3592_j1a(struct lu_phy_attr *lu);
void init_3592_E05(struct lu_phy_attr *lu);
void init_3592_E06(struct lu_phy_attr *lu);

void register_ops(struct lu_phy_attr *lu, int op, void *f);

uint8_t valid_encryption_blk(struct scsi_cmd *cmd);
uint8_t check_restrictions(struct scsi_cmd *cmd);
void init_default_ssc_mode_pages(struct mode *m);
uint8_t resp_spin(struct scsi_cmd *cmd);
uint8_t resp_spout(struct scsi_cmd *cmd);
int resp_write_attribute(struct scsi_cmd *cmd);
int resp_read_attribute(struct scsi_cmd *cmd);
int resp_report_density(uint8_t media, struct vtl_ds *dbuf_p);
void resp_space(int32_t count, int code, uint8_t *sam_stat);
void unloadTape(uint8_t *sam_stat);
