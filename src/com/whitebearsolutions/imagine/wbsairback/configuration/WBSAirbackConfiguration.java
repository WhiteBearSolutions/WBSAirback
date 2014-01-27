package com.whitebearsolutions.imagine.wbsairback.configuration;

public abstract class WBSAirbackConfiguration {
	protected static final String APT_SOURCE_SERVER = "ftp.wbsgo.com";
	protected static final String APT_SOURCE_VERSION = "openairback";
	protected static final String DIR_DEST_BACULA_WINAGENT = "Program Files";
	protected static final String DIR_WINEXE = "/usr/share/wbsairback/winexe/";
	protected static final String DIRECTORY_BACULA_AGENTS = "/etc/wbsairback-admin/agents/";
	protected static final String DIRECTORY_CATEGORIES = "/etc/bacula/categories";
	protected static final String DIRECTORY_CLIENTS = "/etc/bacula/clients";
	protected static final String DIRECTORY_FILESETS = "/etc/bacula/filesets";
	protected static final String DIRECTORY_HYPERVISORS = "/etc/bacula/hypervisors";
	protected static final String DIRECTORY_HYPERVISOR_JOBS = "/etc/bacula/hypervisors/jobs";
	protected static final String DIRECTORY_I18N = "/etc/wbsairback-admin/i18n";
	protected static final String DIRECTORY_IMAGINE = "/opt/imagine";
	protected static final String DIRECTORY_JOBS = "/etc/bacula/jobs";
	protected static final String DIRECTORY_MHVTL = "/etc/mhvtl/";
	protected static final String DIRECTORY_MHVTL_LINKS = "/opt/mhvtl/";
	protected static final String DIRECTORY_MULTIPATH = "/etc/wbsairback-admin/multipath";
	protected static final String DIRECTORY_CLOUD = "/etc/cloud";
	protected static final String DIRECTORY_EXTERNAL_STORAGES = "/etc/wbsairback-admin/external_storages";
	protected static final String DIRECTORY_REMOTE_STORAGE_MOUNT = "/mnt/airback/remote";
	protected static final String DIRECTORY_POOLS = "/etc/bacula/pools";
	protected static final String DIRECTORY_REPLICATION = "/etc/wbsairback-admin/replication";
	protected static final String DIRECTORY_ROLES = "/etc/bacula/roles";
	protected static final String DIRECTORY_SCHEDULES = "/etc/bacula/schedules";
	protected static final String DIRECTORY_STORAGE = "/etc/bacula/storages";
	protected static final String DIRECTORY_SUDO = "/etc/sudoers.d";
	protected static final String DIRECTORY_SYNC = "/etc/wbsairback-admin/sync";
	protected static final String DIRECTORY_USERS = "/etc/bacula/users";
	protected static final String DIRECTORY_VOLUME_MOUNT = "/mnt/airback/volumes";
	protected static final String DIRECTORY_SHARES_VOLUME_MOUNT = "/mnt/airback/volumes/shares";
	protected static final String DIRECTORY_VPN = "/etc/openvpn";
	protected static final String DIRECTORY_PROXY = "/etc/proxy";
	protected static final String DIRECTORY_WSDL = "/var/www/webadministration/wsdl";
	protected static final String DIRECTORY_ADVANCED_STEP = "/etc/wbsairback-admin/advanced/step";
	protected static final String DIRECTORY_ADVANCED_APP_SO = "/etc/wbsairback-admin/advanced/appsAndSo.xml";
	protected static final String DIRECTORY_ADVANCED_SCRIPT_PROCESS = "/etc/wbsairback-admin/advanced/script";
	protected static final String DIRECTORY_ADVANCED_REMOTE_INVENTORY = "/etc/wbsairback-admin/advanced/remote_storage_invetory";
	protected static final String DIRECTORY_ADVANCED_REMOTE_STORAGE = "/etc/wbsairback-admin/advanced/remote_storage";
	protected static final String DIRECTORY_ADVANCED_TEMPLATE_JOB = "/etc/wbsairback-admin/advanced/template_job";
	protected static final String DIRECTORY_ADVANCED_GROUP_JOB = "/etc/wbsairback-admin/advanced/group_job";
	protected static final String DIRECTORY_REMOVE_VOLUME_BLOCK = "/etc/wbsairback-admin";
	protected static final String DIRECTORY_OBJECT_LOCK = "/tmp/wbsairback-object-lock";
	protected static final String FILE_APT_SOURCES = "/etc/apt/sources.list";
	protected static final String FILE_APT_PROXY = "/etc/proxy/wbsairback-proxyconf";
	protected static final String FILE_CATEGORIES = "/etc/bacula/categories/categories.xml";
	protected static final String FILE_CLUSTER = "/etc/cluster/cluster.conf";
	protected static final String FILE_CONFIGURATION = "/etc/wbsairback-admin/config.xml";
	protected static final String FILE_REPLICATION_CONFIGURATION = "/rdata/systemConfig.xml";
	protected static final String FILE_CONFIGURATION_NTP = "/etc/ntp.conf";
	protected static final String FILE_CONFIGURATION_NTP_TIMEZONE = "/etc/timezone";
	protected static final String FILE_FSTAB = "/etc/bacula/fstab";
	protected static final String FILE_TMP_FSTAB = "/etc/wbsairback-admin/tmpfstab";
	protected static final String FILE_IMAGINE_HA_BREAK = "/etc/wbsairback-admin/ha-break.xml";
	protected static final String FILE_IMAGINE_HA_REQUEST = "/etc/wbsairback-admin/ha-request.xml";
	protected static final String FILE_ISCSI_TARGETS = "/etc/scst/scst.conf";
	protected static final String FILE_KEYSTORE = "/etc/wbsairback-admin/store";
	protected static final String FILE_MHVTL_CONFIGURATION = "/etc/mhvtl/device.conf";
	protected static final String FILE_PLANNED_SNAPSHOTS = "/etc/wbsairback-admin/snapshots.db";
	protected static final String FILE_ROLES = "roles.xml";
	protected static final String FILE_SAMBA = "/etc/samba/smb.conf";
	protected static final String FILE_SHARE_CIFS = "/etc/samba/shares.conf";
	protected static final String FILE_SHARE_NFS = "/etc/exports";
	protected static final String FILE_SHARE_FTP = "/etc/proftpd/shares.conf";
	protected static final String FILE_TOMCAT_CONF = "/usr/share/wbsairback/tomcat/conf/server.xml";
	protected static final String FILE_USERS = "users.xml";
	protected static final String FILE_LOCAL_DEVICE_GROUPS = "/etc/wbsairback-admin/local-devices.xml";
	protected static final String FILE_STATISTICS_NETWORK_INTERFACES = "/etc/wbsairback-admin/rrd/scripts/interfaces.txt";
	protected static final String FILE_RUNNING_PROCESSES = "/tmp/wbsairback-processes-map.bin";
	protected static final String FILE_VMWARE_CLIENTS = "/opt/bacula/etc/vsphere_global.conf";
	protected static final String FILE_VSPHERE_PROFILES = "/opt/bacula/working/vsphere/vsphere_all_vm.profile";
	protected static final String TOMCAT_HOME = "/usr/share/wbsairback/tomcat/";
	protected static final String BIN_S3BACKER = "/usr/share/s3backer/s3backer";
	protected static final String BIN_BACULA_BASE64_DECODER = "/usr/sbin/baculaBase64Decode";
	protected static final Integer TIMEOUT_DF_COMMAND = 8;
	
	/**
	 * @return the aptSourceServer
	 */
	public static String getAptSourceServer() {
		return APT_SOURCE_SERVER;
	}
	/**
	 * @return the aptSourceVersion
	 */
	public static String getAptSourceVersion() {
		return APT_SOURCE_VERSION;
	}
	/**
	 * @return the dirDestBaculaWinagent
	 */
	public static String getDirDestBaculaWinagent() {
		return DIR_DEST_BACULA_WINAGENT;
	}
	/**
	 * @return the dirWinexe
	 */
	public static String getDirWinexe() {
		return DIR_WINEXE;
	}
	/**
	 * @return the directoryBaculaAgents
	 */
	public static String getDirectoryBaculaAgents() {
		return DIRECTORY_BACULA_AGENTS;
	}
	/**
	 * @return the directoryCategories
	 */
	public static String getDirectoryCategories() {
		return DIRECTORY_CATEGORIES;
	}
	/**
	 * @return the directoryClients
	 */
	public static String getDirectoryClients() {
		return DIRECTORY_CLIENTS;
	}
	/**
	 * @return the directoryFilesets
	 */
	public static String getDirectoryFilesets() {
		return DIRECTORY_FILESETS;
	}
	/**
	 * @return the directoryHypervisors
	 */
	public static String getDirectoryHypervisors() {
		return DIRECTORY_HYPERVISORS;
	}
	/**
	 * @return the directoryHypervisorJobs
	 */
	public static String getDirectoryHypervisorJobs() {
		return DIRECTORY_HYPERVISOR_JOBS;
	}
	/**
	 * @return the directoryI18n
	 */
	public static String getDirectoryI18n() {
		return DIRECTORY_I18N;
	}
	/**
	 * @return the directoryImagine
	 */
	public static String getDirectoryImagine() {
		return DIRECTORY_IMAGINE;
	}
	/**
	 * @return the directoryJobs
	 */
	public static String getDirectoryJobs() {
		return DIRECTORY_JOBS;
	}
	/**
	 * @return the directoryPools
	 */
	public static String getDirectoryPools() {
		return DIRECTORY_POOLS;
	}
	/**
	 * @return the directoryReplication
	 */
	public static String getDirectoryReplication() {
		return DIRECTORY_REPLICATION;
	}
	/**
	 * @return the directoryRoles
	 */
	public static String getDirectoryRoles() {
		return DIRECTORY_ROLES;
	}
	/**
	 * @return the directorySchedules
	 */
	public static String getDirectorySchedules() {
		return DIRECTORY_SCHEDULES;
	}
	/**
	 * @return the directoryStorage
	 */
	public static String getDirectoryStorage() {
		return DIRECTORY_STORAGE;
	}
	/**
	 * @return the directorySync
	 */
	public static String getDirectorySync() {
		return DIRECTORY_SYNC;
	}
	/**
	 * @return the directoryUsers
	 */
	public static String getDirectoryUsers() {
		return DIRECTORY_USERS;
	}
	/**
	 * @return the directoryVolumeMount
	 */
	public static String getDirectoryVolumeMount() {
		return DIRECTORY_VOLUME_MOUNT;
	}
	/**
	 * @return the directoryVpn
	 */
	public static String getDirectoryVpn() {
		return DIRECTORY_VPN;
	}
	/**
	 * @return the fileAptSources
	 */
	public static String getFileAptSources() {
		return FILE_APT_SOURCES;
	}
	/**
	 * @return the fileAptProxy
	 */
	public static String getFileAptProxy() {
		return FILE_APT_PROXY;
	}
	/**
	 * @return the fileCategories
	 */
	public static String getFileCategories() {
		return FILE_CATEGORIES;
	}
	/**
	 * @return the fileCluster
	 */
	public static String getFileCluster() {
		return FILE_CLUSTER;
	}
	/**
	 * @return the fileConfiguration
	 */
	public static String getFileConfiguration() {
		return FILE_CONFIGURATION;
	}
	/**
	 * @return the fileConfigurationNtp
	 */
	public static String getFileConfigurationNtp() {
		return FILE_CONFIGURATION_NTP;
	}
	/**
	 * @return the fileConfigurationNtpTimezone
	 */
	public static String getFileConfigurationNtpTimezone() {
		return FILE_CONFIGURATION_NTP_TIMEZONE;
	}
	/**
	 * @return the fileFstab
	 */
	public static String getFileFstab() {
		return FILE_FSTAB;
	}
	/**
	 * @return the fileImagineHaBreak
	 */
	public static String getFileImagineHaBreak() {
		return FILE_IMAGINE_HA_BREAK;
	}
	/**
	 * @return the fileImagineHaRequest
	 */
	public static String getFileImagineHaRequest() {
		return FILE_IMAGINE_HA_REQUEST;
	}
	/**
	 * @return the fileIscsiTargets
	 */
	public static String getFileIscsiTargets() {
		return FILE_ISCSI_TARGETS;
	}
	/**
	 * @return the fileKeystore
	 */
	public static String getFileKeystore() {
		return FILE_KEYSTORE;
	}
	/**
	 * @return the fileMhvtlConfiguration
	 */
	public static String getFileMhvtlConfiguration() {
		return FILE_MHVTL_CONFIGURATION;
	}
	/**
	 * @return the filePlannedSnapshots
	 */
	public static String getFilePlannedSnapshots() {
		return FILE_PLANNED_SNAPSHOTS;
	}
	/**
	 * @return the fileRoles
	 */
	public static String getFileRoles() {
		return FILE_ROLES;
	}
	/**
	 * @return the fileSamba
	 */
	public static String getFileSamba() {
		return FILE_SAMBA;
	}
	/**
	 * @return the fileShareCifs
	 */
	public static String getFileShareCifs() {
		return FILE_SHARE_CIFS;
	}
	/**
	 * @return the fileShareNfs
	 */
	public static String getFileShareNfs() {
		return FILE_SHARE_NFS;
	}
	/**
	 * @return the fileShareFtp
	 */
	public static String getFileShareFtp() {
		return FILE_SHARE_FTP;
	}
	/**
	 * @return the fileTomcatConf
	 */
	public static String getFileTomcatConf() {
		return FILE_TOMCAT_CONF;
	}
	/**
	 * @return the fileUsers
	 */
	public static String getFileUsers() {
		return FILE_USERS;
	}
	/**
	 * @return the tomcatHome
	 */
	public static String getTomcatHome() {
		return TOMCAT_HOME;
	}
	/**
	 * @return the directoryMhvtl
	 */
	public static String getDirectoryMhvtl() {
		return DIRECTORY_MHVTL;
	}
	/**
	 * @return the directoryMhvtlLinks
	 */
	public static String getDirectoryMhvtlLinks() {
		return DIRECTORY_MHVTL_LINKS;
	}
	/**
	 * @return the directorySudo
	 */
	public static String getDirectorySudo() {
		return DIRECTORY_SUDO;
	}
	public static String getDirectoryMultipath() {
		return DIRECTORY_MULTIPATH;
	}
	public static String getDirectoryCloud() {
		return DIRECTORY_CLOUD;
	}
	public static String getDirectoryProxy() {
		return DIRECTORY_PROXY;
	}
	public static String getFileLocalDeviceGroups() {
		return FILE_LOCAL_DEVICE_GROUPS;
	}
	public static String getFileTmpFstab() {
		return FILE_TMP_FSTAB;
	}
	public static String getBinS3backer() {
		return BIN_S3BACKER;
	}
	public static String getDirectoryExternalStorages() {
		return DIRECTORY_EXTERNAL_STORAGES;
	}
	public static String getFileStatisticsNetworkInterfaces() {
		return FILE_STATISTICS_NETWORK_INTERFACES;
	}
	public static String getDirectoryWsdl() {
		return DIRECTORY_WSDL;
	}
	public static String getDirectoryRemoveVolumeBlock() {
		return DIRECTORY_REMOVE_VOLUME_BLOCK;
	}

	public static String getDirectoryAdvancedStep() {
		return DIRECTORY_ADVANCED_STEP;
	}

	public static String getDirectoryAdvancedRemoteInventory() {
		return DIRECTORY_ADVANCED_REMOTE_INVENTORY;
	}

	public static String getDirectoryAdvancedRemoteStorage() {
		return DIRECTORY_ADVANCED_REMOTE_STORAGE;
	}
	public static String getDirectoryAdvancedScriptProcess(){
		return DIRECTORY_ADVANCED_SCRIPT_PROCESS;
	}
	public static String getDirectoryAdvancedTemplateJob() {
		return DIRECTORY_ADVANCED_TEMPLATE_JOB;
	}
	public static String getDirectoryAdvancedGroupJob() {
		return DIRECTORY_ADVANCED_GROUP_JOB;
	}
	public static String getDirectoryRemoteStorageMount() {
		return DIRECTORY_REMOTE_STORAGE_MOUNT;
	}
	public static String getDirectoryAdvancedAppSo() {
		return DIRECTORY_ADVANCED_APP_SO;
	}
	public static String getDirectoryObjectLock() {
		return DIRECTORY_OBJECT_LOCK;
	}
	public static String getFileRunningProcesses() {
		return FILE_RUNNING_PROCESSES;
	}
	public static String getFileVmwareClients() {
		return FILE_VMWARE_CLIENTS;
	}
	public static String getDirectorySharesVolumeMount() {
		return DIRECTORY_SHARES_VOLUME_MOUNT;
	}
	public static String getFileVsphereProfiles() {
		return FILE_VSPHERE_PROFILES;
	}
	public static String getFileReplicationConfiguration() {
		return FILE_REPLICATION_CONFIGURATION;
	}
	public static String getBinBaculaBase64Decoder() {
		return BIN_BACULA_BASE64_DECODER;
	}
	public static Integer getTimeoutDfCommand() {
		return TIMEOUT_DF_COMMAND;
	}
}
