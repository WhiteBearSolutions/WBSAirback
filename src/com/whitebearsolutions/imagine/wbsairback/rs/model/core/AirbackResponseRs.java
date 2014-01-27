package com.whitebearsolutions.imagine.wbsairback.rs.model.core;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.AdvancedStorageRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ApplicationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupJobConfigRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ScriptProcessRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.StepRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.StorageInventoryRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.TemplateJobRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.BackupFileRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.CategoryRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ClientRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.FilesetRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorDataStoreRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorJobRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorVirtualMachineRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobArchievedRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobScheduledRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.PoolRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.PoolVolumeRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleDailyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleMonthlyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleWeeklyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleYearlyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageDiskRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageDriveRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageLibraryRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageRemoteRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageSlotRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageTapeRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.AggregateRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.CloudAccountRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.CloudDiskRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.CloudDiskStatRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.IscsiExternalTargetRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.IscsiTargetRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.IscsiTargetSimpleRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.PhysicalDeviceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.QuotaGroupRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.QuotaUserRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ReplicationDestinationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ReplicationSourceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ShareExternalRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ShareRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.SnapshotRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.VolumeRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.subscription.LicenseRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.subscription.PackageRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.MaintenanceConfigurationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkInterfaceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkNameServerRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkRouteRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.ProxyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.RoleRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.ServiceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.SystemConfigurationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.TimeDateRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.TimeServerRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.TimeZoneRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.UserRs;

@XmlRootElement(name="response")
public class AirbackResponseRs {
	
	private List<JobScheduledRs> jobsScheduled;
	private JobScheduledRs jobScheduled;
	
	private List<JobArchievedRs> jobsArchieved;
	private JobArchievedRs jobArchieved;
	
	private List<ClientRs> clients;
	private ClientRs client;
	
	private List<CategoryRs> categories;
	private CategoryRs category;
	
	private List<StorageDiskRs> storageDisks;
	private StorageDiskRs storageDisk;
	
	private List<StorageTapeRs> storageTapes;
	private StorageTapeRs storageTape;
	
	private List<StorageLibraryRs> storageLibrarys;
	private StorageLibraryRs storageLibrary;
	
	private List<StorageDriveRs> storageDrives;
	private StorageDriveRs storageDrive;
	
	private List<StorageSlotRs> storageSlots;
	private StorageSlotRs storageSlot;
	
	private List<StorageRemoteRs> storageRemotes;
	private StorageRemoteRs storageRemote;
	
	private List<PoolRs> pools;
	private PoolRs pool;
	
	private List<PoolVolumeRs> poolVolumes;
	private PoolVolumeRs poolVolume;
	
	private List<FilesetRs> filesets;
	private FilesetRs fileset;
	
	private List<BackupFileRs> files;
	
	private List<ScheduleMonthlyRs> schedulesMonthly;
	private ScheduleMonthlyRs scheduleMonthly;
	
	private List<ScheduleDailyRs> schedulesDaily;
	private ScheduleDailyRs scheduleDaily;
	
	private List<ScheduleWeeklyRs> schedulesWeekly;
	private ScheduleWeeklyRs scheduleWeekly;
	
	private List<ScheduleYearlyRs> schedulesYearly;
	private ScheduleYearlyRs scheduleYearly;
	
	private List<AggregateRs> aggregates;
	private AggregateRs aggregate;
	
	private List<PhysicalDeviceRs> devices;
	private PhysicalDeviceRs device;
	
	private List<VolumeRs> volumes;
	private VolumeRs volume;
	
	private List<SnapshotRs> snapshots;
	private SnapshotRs snapshot;
	
	private List<ShareRs> shares;
	private ShareRs share;
	
	private List<ShareExternalRs> sharesExternal;
	private ShareExternalRs shareExternal;
	
	private List<ReplicationSourceRs> replicationSources;
	private ReplicationSourceRs replicationSource;
	
	private List<ReplicationDestinationRs> replicationDestinations;
	private ReplicationDestinationRs replicationDestination;
	
	private List<IscsiTargetRs> iscsiTargets;
	private IscsiTargetRs iscsiTarget;
	
	private List<IscsiExternalTargetRs> iscsiExternalTargets;
	private IscsiExternalTargetRs iscsiExternalTarget;
	
	private List<IscsiTargetSimpleRs> iscsiTargetSimples;
	private IscsiTargetSimpleRs iscsiTargetSimple;
	
	private List<CloudAccountRs> cloudAccounts;
	private CloudAccountRs cloudAccount;
	
	private List<CloudDiskRs> cloudDisks;
	private CloudDiskRs cloudDisk;
	
	private List<CloudDiskStatRs> cloudGroupStats;
	
	private List<QuotaUserRs> quotasUsers;
	private QuotaUserRs quotaUser;
	
	private List<QuotaGroupRs> quotasGroups;
	private QuotaGroupRs quotaGroup;
	
	private List<HypervisorRs> hypervisors;
	private HypervisorRs hypervisor;
	
	private List<HypervisorJobRs> hypervisorJobs;
	private HypervisorJobRs hypervisorJob;
	
	private List<HypervisorDataStoreRs> hypervisorDataStores;
	private HypervisorDataStoreRs hypervisorDataStore;
	
	private List<HypervisorVirtualMachineRs> hypervisorVirtualMachines;
	private HypervisorVirtualMachineRs hypervisorVirtualMachine;
	
	private List<UserRs> users;
	private UserRs user;
	
	private List<RoleRs> roles;
	private UserRs role;
	
	private List<TimeServerRs> timeServers;
	private TimeServerRs timeServer;
	
	private List<TimeZoneRs> timeZones;
	private TimeZoneRs timeZone;
	
	private List<ServiceRs> services;
	private ServiceRs service;
	
	private List<NetworkNameServerRs> networkNameServers;
	private NetworkNameServerRs networkNameServer;
	
	private List<NetworkInterfaceRs> networkInterfaces;
	private NetworkInterfaceRs networkInterface;
	
	private List<NetworkRouteRs> networkRoutes;
	private NetworkRouteRs networkRoute;
	
	private List<StepRs> steps;
	private StepRs step;
	
	private List<ApplicationRs> applications;
	private ApplicationRs application;
	
	private List<SystemRs> systems;
	private SystemRs system;
	
	private List<StorageInventoryRs> storageInventories;
	private StorageInventoryRs storageInventory;
	
	private List<ScriptProcessRs> scriptsProcesses;
	private ScriptProcessRs scriptProcess;
	
	private List<AdvancedStorageRs> advancedStorages;
	private AdvancedStorageRs advancedStorage;
	
	private List<TemplateJobRs> templateJobs;
	private TemplateJobRs templateJob;
	
	private List<GroupRs> groups;
	private GroupRs group;
	private GroupJobConfigRs groupJobConfiguration;
	
	private List<LicenseRs> licenses;
	private List<PackageRs> packages;
	
	private TimeDateRs timeDate;
	
	private ProxyRs proxy;
	
	private MaintenanceConfigurationRs maintenanceConfiguration;
	private SystemConfigurationRs systemConfiguration;
	
	private String clientPassword;
	private String storagePassword;
	private String airbackIscsiName;
	private String rsyncInterval;
	private String registerStatus;
	private String serialNumber;
	
	private String error;
	private String success;
	
	@XmlElementWrapper(name="jobsArchieved")
	@XmlElementRef()
	public List<JobArchievedRs> getJobsArchieved() {
		return jobsArchieved;
	}
	public void setJobsArchieved(List<JobArchievedRs> jobsArchieved) {
		this.jobsArchieved = jobsArchieved;
	}
	
	@XmlElementWrapper(name="jobsScheduled")
	@XmlElementRef()
	public List<JobScheduledRs> getJobsScheduled() {
		return jobsScheduled;
	}
	public void setJobsScheduled(List<JobScheduledRs> jobsScheduled) {
		this.jobsScheduled = jobsScheduled;
	}
	
	public JobArchievedRs getJobArchieved() {
		return jobArchieved;
	}
	public void setJobArchieved(JobArchievedRs jobArchieved) {
		this.jobArchieved = jobArchieved;
	}
	
	public JobScheduledRs getJobScheduled() {
		return jobScheduled;
	}
	public void setJobScheduled(JobScheduledRs jobScheduled) {
		this.jobScheduled = jobScheduled;
	}
	
	@XmlElementWrapper(name="clients")
	@XmlElementRef()
	public List<ClientRs> getClients() {
		return clients;
	}
	public void setClients(List<ClientRs> clients) {
		this.clients = clients;
	}
	
	public ClientRs getClient() {
		return client;
	}
	public void setClient(ClientRs client) {
		this.client = client;
	}
	
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	@XmlElementWrapper(name="storageDisks")
	@XmlElementRef()
	public List<StorageDiskRs> getStorageDisks() {
		return storageDisks;
	}
	public void setStorageDisks(List<StorageDiskRs> storageDisks) {
		this.storageDisks = storageDisks;
	}
	public StorageDiskRs getStorageDisk() {
		return storageDisk;
	}
	public void setStorageDisk(StorageDiskRs storageDisk) {
		this.storageDisk = storageDisk;
	}
	
	
	@XmlElementWrapper(name="pools")
	@XmlElementRef()
	public List<PoolRs> getPools() {
		return pools;
	}
	public void setPools(List<PoolRs> pools) {
		this.pools = pools;
	}
	public PoolRs getPool() {
		return pool;
	}
	public void setPool(PoolRs pool) {
		this.pool = pool;
	}
	
	@XmlElementWrapper(name="poolVolumes")
	@XmlElementRef()
	public List<PoolVolumeRs> getPoolVolumes() {
		return poolVolumes;
	}
	public void setPoolVolumes(List<PoolVolumeRs> poolVolumes) {
		this.poolVolumes = poolVolumes;
	}
	public PoolVolumeRs getPoolVolume() {
		return poolVolume;
	}
	public void setPoolVolume(PoolVolumeRs poolVolume) {
		this.poolVolume = poolVolume;
	}
	
	@XmlElementWrapper(name="filesets")
	@XmlElementRef()
	public List<FilesetRs> getFilesets() {
		return filesets;
	}
	public void setFilesets(List<FilesetRs> filesets) {
		this.filesets = filesets;
	}
	public FilesetRs getFileset() {
		return fileset;
	}
	public void setFileset(FilesetRs fileset) {
		this.fileset = fileset;
	}
	
	@XmlElementWrapper(name="storageTapes")
	@XmlElementRef()
	public List<StorageTapeRs> getStorageTapes() {
		return storageTapes;
	}
	public void setStorageTapes(List<StorageTapeRs> storageTapes) {
		this.storageTapes = storageTapes;
	}
	public StorageTapeRs getStorageTape() {
		return storageTape;
	}
	public void setStorageTape(StorageTapeRs storageTape) {
		this.storageTape = storageTape;
	}
	
	@XmlElementWrapper(name="storageLibrarys")
	@XmlElementRef()
	public List<StorageLibraryRs> getStorageLibrarys() {
		return storageLibrarys;
	}
	public void setStorageLibrarys(List<StorageLibraryRs> storageLibrarys) {
		this.storageLibrarys = storageLibrarys;
	}
	public StorageLibraryRs getStorageLibrary() {
		return storageLibrary;
	}
	public void setStorageLibrary(StorageLibraryRs storageLibrary) {
		this.storageLibrary = storageLibrary;
	}
	
	@XmlElementWrapper(name="storageRemotes")
	@XmlElementRef()
	public List<StorageRemoteRs> getStorageRemotes() {
		return storageRemotes;
	}
	public void setStorageRemotes(List<StorageRemoteRs> storageRemotes) {
		this.storageRemotes = storageRemotes;
	}
	public StorageRemoteRs getStorageRemote() {
		return storageRemote;
	}
	public void setStorageRemote(StorageRemoteRs storageRemote) {
		this.storageRemote = storageRemote;
	}
	
	@XmlElementWrapper(name="files")
	@XmlElementRef()
	public List<BackupFileRs> getFiles() {
		return files;
	}
	public void setFiles(List<BackupFileRs> files) {
		this.files = files;
	}
	
	@XmlElementWrapper(name="schedulesMonthly")
	@XmlElementRef()
	public List<ScheduleMonthlyRs> getSchedulesMonthly() {
		return schedulesMonthly;
	}
	public void setSchedulesMonthly(List<ScheduleMonthlyRs> schedulesMonthly) {
		this.schedulesMonthly = schedulesMonthly;
	}
	public ScheduleMonthlyRs getScheduleMonthly() {
		return scheduleMonthly;
	}
	public void setScheduleMonthly(ScheduleMonthlyRs scheduleMonthly) {
		this.scheduleMonthly = scheduleMonthly;
	}
	
	@XmlElementWrapper(name="schedulesDaily")
	@XmlElementRef()
	public List<ScheduleDailyRs> getSchedulesDaily() {
		return schedulesDaily;
	}
	public void setSchedulesDaily(List<ScheduleDailyRs> schedulesDaily) {
		this.schedulesDaily = schedulesDaily;
	}
	public ScheduleDailyRs getScheduleDaily() {
		return scheduleDaily;
	}
	public void setScheduleDaily(ScheduleDailyRs scheduleDaily) {
		this.scheduleDaily = scheduleDaily;
	}
	
	@XmlElementWrapper(name="schedulesWeekly")
	@XmlElementRef()
	public List<ScheduleWeeklyRs> getSchedulesWeekly() {
		return schedulesWeekly;
	}
	public void setSchedulesWeekly(List<ScheduleWeeklyRs> schedulesWeekly) {
		this.schedulesWeekly = schedulesWeekly;
	}
	public ScheduleWeeklyRs getScheduleWeekly() {
		return scheduleWeekly;
	}
	public void setScheduleWeekly(ScheduleWeeklyRs scheduleWeekly) {
		this.scheduleWeekly = scheduleWeekly;
	}
	
	@XmlElementWrapper(name="schedulesYearly")
	@XmlElementRef()
	public List<ScheduleYearlyRs> getSchedulesYearly() {
		return schedulesYearly;
	}
	public void setSchedulesYearly(List<ScheduleYearlyRs> schedulesYearly) {
		this.schedulesYearly = schedulesYearly;
	}
	public ScheduleYearlyRs getScheduleYearly() {
		return scheduleYearly;
	}
	public void setScheduleYearly(ScheduleYearlyRs scheduleYearly) {
		this.scheduleYearly = scheduleYearly;
	}
	
	@XmlElementWrapper(name="aggregates")
	@XmlElementRef()
	public List<AggregateRs> getAggregates() {
		return aggregates;
	}
	public void setAggregates(List<AggregateRs> aggregates) {
		this.aggregates = aggregates;
	}
	public AggregateRs getAggregate() {
		return aggregate;
	}
	public void setAggregate(AggregateRs aggregate) {
		this.aggregate = aggregate;
	}
	
	@XmlElementWrapper(name="devices")
	@XmlElementRef()
	public List<PhysicalDeviceRs> getDevices() {
		return devices;
	}
	public void setDevices(List<PhysicalDeviceRs> devices) {
		this.devices = devices;
	}
	public PhysicalDeviceRs getDevice() {
		return device;
	}
	public void setDevice(PhysicalDeviceRs device) {
		this.device = device;
	}
	
	@XmlElementWrapper(name="volumes")
	@XmlElementRef()
	public List<VolumeRs> getVolumes() {
		return volumes;
	}
	public void setVolumes(List<VolumeRs> volumes) {
		this.volumes = volumes;
	}
	public VolumeRs getVolume() {
		return volume;
	}
	public void setVolume(VolumeRs volume) {
		this.volume = volume;
	}
	
	@XmlElementWrapper(name="snapshots")
	@XmlElementRef()
	public List<SnapshotRs> getSnapshots() {
		return snapshots;
	}
	public void setSnapshots(List<SnapshotRs> snapshots) {
		this.snapshots = snapshots;
	}
	public SnapshotRs getSnapshot() {
		return snapshot;
	}
	public void setSnapshot(SnapshotRs snapshot) {
		this.snapshot = snapshot;
	}
	
	@XmlElementWrapper(name="shares")
	@XmlElementRef()
	public List<ShareRs> getShares() {
		return shares;
	}
	public void setShares(List<ShareRs> shares) {
		this.shares = shares;
	}
	public ShareRs getShare() {
		return share;
	}
	public void setShare(ShareRs share) {
		this.share = share;
	}
	
	@XmlElementWrapper(name="sharesExternal")
	@XmlElementRef()
	public List<ShareExternalRs> getSharesExternal() {
		return sharesExternal;
	}
	public void setSharesExternal(List<ShareExternalRs> sharesExternal) {
		this.sharesExternal = sharesExternal;
	}
	public ShareExternalRs getShareExternal() {
		return shareExternal;
	}
	public void setShareExternal(ShareExternalRs shareExternal) {
		this.shareExternal = shareExternal;
	}
	
	@XmlElementWrapper(name="replicationSources")
	@XmlElementRef()
	public List<ReplicationSourceRs> getReplicationSources() {
		return replicationSources;
	}
	public void setReplicationSources(List<ReplicationSourceRs> replicationSources) {
		this.replicationSources = replicationSources;
	}
	public ReplicationSourceRs getReplicationSource() {
		return replicationSource;
	}
	public void setReplicationSource(ReplicationSourceRs replicationSource) {
		this.replicationSource = replicationSource;
	}
	
	@XmlElementWrapper(name="replicationDestinations")
	@XmlElementRef()
	public List<ReplicationDestinationRs> getReplicationDestinations() {
		return replicationDestinations;
	}
	public void setReplicationDestinations(
			List<ReplicationDestinationRs> replicationDestinations) {
		this.replicationDestinations = replicationDestinations;
	}
	public ReplicationDestinationRs getReplicationDestination() {
		return replicationDestination;
	}
	public void setReplicationDestination(
			ReplicationDestinationRs replicationDestination) {
		this.replicationDestination = replicationDestination;
	}
	
	@XmlElementWrapper(name="iscsiTargets")
	@XmlElementRef()
	public List<IscsiTargetRs> getIscsiTargets() {
		return iscsiTargets;
	}
	public void setIscsiTargets(List<IscsiTargetRs> iscsiTargets) {
		this.iscsiTargets = iscsiTargets;
	}
	public IscsiTargetRs getIscsiTarget() {
		return iscsiTarget;
	}
	public void setIscsiTarget(IscsiTargetRs iscsiTarget) {
		this.iscsiTarget = iscsiTarget;
	}
	
	@XmlElementWrapper(name="iscsiExternalTargets")
	@XmlElementRef()
	public List<IscsiExternalTargetRs> getIscsiExternalTargets() {
		return iscsiExternalTargets;
	}
	public void setIscsiExternalTargets(
			List<IscsiExternalTargetRs> iscsiExternalTargets) {
		this.iscsiExternalTargets = iscsiExternalTargets;
	}
	public IscsiExternalTargetRs getIscsiExternalTarget() {
		return iscsiExternalTarget;
	}
	public void setIscsiExternalTarget(IscsiExternalTargetRs iscsiExternalTarget) {
		this.iscsiExternalTarget = iscsiExternalTarget;
	}
	
	@XmlElementWrapper(name="iscsiTargetSimples")
	@XmlElementRef()
	public List<IscsiTargetSimpleRs> getIscsiTargetSimples() {
		return iscsiTargetSimples;
	}
	public void setIscsiTargetSimples(List<IscsiTargetSimpleRs> iscsiTargetSimples) {
		this.iscsiTargetSimples = iscsiTargetSimples;
	}
	public IscsiTargetSimpleRs getIscsiTargetSimple() {
		return iscsiTargetSimple;
	}
	public void setIscsiTargetSimple(IscsiTargetSimpleRs iscsiTargetSimple) {
		this.iscsiTargetSimple = iscsiTargetSimple;
	}
	public String getAirbackIscsiName() {
		return airbackIscsiName;
	}
	public void setAirbackIscsiName(String airbackIscsiName) {
		this.airbackIscsiName = airbackIscsiName;
	}
	
	@XmlElementWrapper(name="cloudAccounts")
	@XmlElementRef()
	public List<CloudAccountRs> getCloudAccounts() {
		return cloudAccounts;
	}
	public void setCloudAccounts(List<CloudAccountRs> cloudAccounts) {
		this.cloudAccounts = cloudAccounts;
	}
	public CloudAccountRs getCloudAccount() {
		return cloudAccount;
	}
	public void setCloudAccount(CloudAccountRs cloudAccount) {
		this.cloudAccount = cloudAccount;
	}
	
	@XmlElementWrapper(name="cloudDisks")
	@XmlElementRef()
	public List<CloudDiskRs> getCloudDisks() {
		return cloudDisks;
	}
	public void setCloudDisks(List<CloudDiskRs> cloudDisks) {
		this.cloudDisks = cloudDisks;
	}
	public CloudDiskRs getCloudDisk() {
		return cloudDisk;
	}
	public void setCloudDisk(CloudDiskRs cloudDisk) {
		this.cloudDisk = cloudDisk;
	}
	
	@XmlElementWrapper(name="cloudGroupStats")
	@XmlElementRef()
	public List<CloudDiskStatRs> getCloudGroupStats() {
		return cloudGroupStats;
	}
	public void setCloudGroupStats(List<CloudDiskStatRs> cloudGroupStats) {
		this.cloudGroupStats = cloudGroupStats;
	}
	
	@XmlElementWrapper(name="quotaUsers")
	@XmlElementRef()
	public List<QuotaUserRs> getQuotasUsers() {
		return quotasUsers;
	}
	public void setQuotasUsers(List<QuotaUserRs> quotasUsers) {
		this.quotasUsers = quotasUsers;
	}
	public QuotaUserRs getQuotaUser() {
		return quotaUser;
	}
	public void setQuotaUser(QuotaUserRs quotaUser) {
		this.quotaUser = quotaUser;
	}
	
	@XmlElementWrapper(name="quotaGroups")
	@XmlElementRef()
	public List<QuotaGroupRs> getQuotasGroups() {
		return quotasGroups;
	}
	public void setQuotasGroups(List<QuotaGroupRs> quotasGroups) {
		this.quotasGroups = quotasGroups;
	}
	public QuotaGroupRs getQuotaGroup() {
		return quotaGroup;
	}
	public void setQuotaGroup(QuotaGroupRs quotaGroup) {
		this.quotaGroup = quotaGroup;
	}
	
	@XmlElementWrapper(name="hypervisors")
	@XmlElementRef()
	public List<HypervisorRs> getHypervisors() {
		return hypervisors;
	}
	public void setHypervisors(List<HypervisorRs> hypervisors) {
		this.hypervisors = hypervisors;
	}
	public HypervisorRs getHypervisor() {
		return hypervisor;
	}
	public void setHypervisor(HypervisorRs hypervisor) {
		this.hypervisor = hypervisor;
	}	
	
	@XmlElementWrapper(name="hypervisorJobs")
	@XmlElementRef()
	public List<HypervisorJobRs> getHypervisorJobs() {
		return hypervisorJobs;
	}
	public void setHypervisorJobs(List<HypervisorJobRs> hypervisorJobs) {
		this.hypervisorJobs = hypervisorJobs;
	}
	public HypervisorJobRs getHypervisorJob() {
		return hypervisorJob;
	}
	public void setHypervisorJob(HypervisorJobRs hypervisorJob) {
		this.hypervisorJob = hypervisorJob;
	}
	
	@XmlElementWrapper(name="hypervisorDatastores")
	@XmlElementRef()
	public List<HypervisorDataStoreRs> getHypervisorDataStores() {
		return hypervisorDataStores;
	}
	public void setHypervisorDataStores(
			List<HypervisorDataStoreRs> hypervisorDataStores) {
		this.hypervisorDataStores = hypervisorDataStores;
	}
	public HypervisorDataStoreRs getHypervisorDataStore() {
		return hypervisorDataStore;
	}
	public void setHypervisorDataStore(HypervisorDataStoreRs hypervisorDataStore) {
		this.hypervisorDataStore = hypervisorDataStore;
	}
	
	@XmlElementWrapper(name="hypervisorVirtualMachines")
	@XmlElementRef()
	public List<HypervisorVirtualMachineRs> getHypervisorVirtualMachines() {
		return hypervisorVirtualMachines;
	}
	public void setHypervisorVirtualMachines(
			List<HypervisorVirtualMachineRs> hypervisorVirtualMachines) {
		this.hypervisorVirtualMachines = hypervisorVirtualMachines;
	}
	public HypervisorVirtualMachineRs getHypervisorVirtualMachine() {
		return hypervisorVirtualMachine;
	}
	public void setHypervisorVirtualMachine(
			HypervisorVirtualMachineRs hypervisorVirtualMachine) {
		this.hypervisorVirtualMachine = hypervisorVirtualMachine;
	}
	
	@XmlElementWrapper(name="storageDrives")
	@XmlElementRef()
	public List<StorageDriveRs> getStorageDrives() {
		return storageDrives;
	}
	public void setStorageDrives(List<StorageDriveRs> storageDrives) {
		this.storageDrives = storageDrives;
	}
	public StorageDriveRs getStorageDrive() {
		return storageDrive;
	}
	public void setStorageDrive(StorageDriveRs storageDrive) {
		this.storageDrive = storageDrive;
	}
	
	@XmlElementWrapper(name="storageSlots")
	@XmlElementRef()
	public List<StorageSlotRs> getStorageSlots() {
		return storageSlots;
	}
	public void setStorageSlots(List<StorageSlotRs> storageSlots) {
		this.storageSlots = storageSlots;
	}
	public StorageSlotRs getStorageSlot() {
		return storageSlot;
	}
	public void setStorageSlot(StorageSlotRs storageSlot) {
		this.storageSlot = storageSlot;
	}
	
	@XmlElementWrapper(name="categories")
	@XmlElementRef()
	public List<CategoryRs> getCategories() {
		return categories;
	}
	public void setCategories(List<CategoryRs> categories) {
		this.categories = categories;
	}
	public CategoryRs getCategory() {
		return category;
	}
	public void setCategory(CategoryRs category) {
		this.category = category;
	}
	public String getRsyncInterval() {
		return rsyncInterval;
	}
	public void setRsyncInterval(String rsyncInterval) {
		this.rsyncInterval = rsyncInterval;
	}
	
	@XmlElementWrapper(name="users")
	@XmlElementRef()
	public List<UserRs> getUsers() {
		return users;
	}
	public void setUsers(List<UserRs> users) {
		this.users = users;
	}
	public UserRs getUser() {
		return user;
	}
	public void setUser(UserRs user) {
		this.user = user;
	}
	
	@XmlElementWrapper(name="roles")
	@XmlElementRef()
	public List<RoleRs> getRoles() {
		return roles;
	}
	public void setRoles(List<RoleRs> roles) {
		this.roles = roles;
	}
	public UserRs getRole() {
		return role;
	}
	public void setRole(UserRs role) {
		this.role = role;
	}
	
	@XmlElementWrapper(name="timeServers")
	@XmlElementRef()
	public List<TimeServerRs> getTimeServers() {
		return timeServers;
	}
	public void setTimeServers(List<TimeServerRs> timeServers) {
		this.timeServers = timeServers;
	}
	public TimeServerRs getTimeServer() {
		return timeServer;
	}
	public void setTimeServer(TimeServerRs timeServer) {
		this.timeServer = timeServer;
	}
	
	@XmlElementWrapper(name="timeZones")
	@XmlElementRef()
	public List<TimeZoneRs> getTimeZones() {
		return timeZones;
	}
	public void setTimeZones(List<TimeZoneRs> timeZones) {
		this.timeZones = timeZones;
	}
	public TimeZoneRs getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(TimeZoneRs timeZone) {
		this.timeZone = timeZone;
	}
	
	public TimeDateRs getTimeDate() {
		return timeDate;
	}
	public void setTimeDate(TimeDateRs timeDate) {
		this.timeDate = timeDate;
	}
	
	public ProxyRs getProxy() {
		return proxy;
	}
	public void setProxy(ProxyRs proxy) {
		this.proxy = proxy;
	}
	
	public MaintenanceConfigurationRs getMaintenanceConfiguration() {
		return maintenanceConfiguration;
	}
	public void setMaintenanceConfiguration(
			MaintenanceConfigurationRs maintenanceConfiguration) {
		this.maintenanceConfiguration = maintenanceConfiguration;
	}
	public String getClientPassword() {
		return clientPassword;
	}
	public void setClientPassword(String clientPassword) {
		this.clientPassword = clientPassword;
	}
	public String getStoragePassword() {
		return storagePassword;
	}
	public void setStoragePassword(String storagePassword) {
		this.storagePassword = storagePassword;
	}
	public SystemConfigurationRs getSystemConfiguration() {
		return systemConfiguration;
	}
	public void setSystemConfiguration(SystemConfigurationRs systemConfiguration) {
		this.systemConfiguration = systemConfiguration;
	}
	
	@XmlElementWrapper(name="services")
	@XmlElementRef()
	public List<ServiceRs> getServices() {
		return services;
	}
	public void setServices(List<ServiceRs> services) {
		this.services = services;
	}
	public ServiceRs getService() {
		return service;
	}
	public void setService(ServiceRs service) {
		this.service = service;
	}
	
	@XmlElementWrapper(name="networkNameServers")
	@XmlElementRef()
	public List<NetworkNameServerRs> getNetworkNameServers() {
		return networkNameServers;
	}
	public void setNetworkNameServers(List<NetworkNameServerRs> networkNameServers) {
		this.networkNameServers = networkNameServers;
	}
	public NetworkNameServerRs getNetworkNameServer() {
		return networkNameServer;
	}
	public void setNetworkNameServer(NetworkNameServerRs networkNameServer) {
		this.networkNameServer = networkNameServer;
	}
	
	@XmlElementWrapper(name="networkInterfaces")
	@XmlElementRef()
	public List<NetworkInterfaceRs> getNetworkInterfaces() {
		return networkInterfaces;
	}
	public void setNetworkInterfaces(List<NetworkInterfaceRs> networkInterfaces) {
		this.networkInterfaces = networkInterfaces;
	}
	public NetworkInterfaceRs getNetworkInterface() {
		return networkInterface;
	}
	public void setNetworkInterface(NetworkInterfaceRs networkInterface) {
		this.networkInterface = networkInterface;
	}
	
	@XmlElementWrapper(name="networkInterfaces")
	@XmlElementRef()
	public List<NetworkRouteRs> getNetworkRoutes() {
		return networkRoutes;
	}
	public void setNetworkRoutes(List<NetworkRouteRs> networkRoutes) {
		this.networkRoutes = networkRoutes;
	}
	public NetworkRouteRs getNetworkRoute() {
		return networkRoute;
	}
	public void setNetworkRoute(NetworkRouteRs networkRoute) {
		this.networkRoute = networkRoute;
	}
	
	@XmlElementWrapper(name="licenses")
	@XmlElementRef()
	public List<LicenseRs> getLicenses() {
		return licenses;
	}
	public void setLicenses(List<LicenseRs> licenses) {
		this.licenses = licenses;
	}
	public String getRegisterStatus() {
		return registerStatus;
	}
	public void setRegisterStatus(String registerStatus) {
		this.registerStatus = registerStatus;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	@XmlElementWrapper(name="packages")
	@XmlElementRef()
	public List<PackageRs> getPackages() {
		return packages;
	}
	public void setPackages(List<PackageRs> packages) {
		this.packages = packages;
	}
	
	@XmlElementWrapper(name="steps")
	@XmlElementRef()
	public List<StepRs> getSteps() {
		return steps;
	}
	public void setSteps(List<StepRs> steps) {
		this.steps = steps;
	}
	public StepRs getStep() {
		return step;
	}
	public void setStep(StepRs step) {
		this.step = step;
	}
	
	@XmlElementWrapper(name="applications")
	@XmlElementRef()
	public List<ApplicationRs> getApplications() {
		return applications;
	}
	public void setApplications(List<ApplicationRs> applications) {
		this.applications = applications;
	}
	public ApplicationRs getApplication() {
		return application;
	}
	public void setApplication(ApplicationRs application) {
		this.application = application;
	}
	
	@XmlElementWrapper(name="systems")
	@XmlElementRef()
	public List<SystemRs> getSystems() {
		return systems;
	}
	public void setSystems(List<SystemRs> systems) {
		this.systems = systems;
	}
	public SystemRs getSystem() {
		return system;
	}
	public void setSystem(SystemRs system) {
		this.system = system;
	}
	
	@XmlElementWrapper(name="storageInventories")
	@XmlElementRef()
	public List<StorageInventoryRs> getStorageInventories() {
		return storageInventories;
	}
	public void setStorageInventories(List<StorageInventoryRs> storageInventories) {
		this.storageInventories = storageInventories;
	}
	public StorageInventoryRs getStorageInventory() {
		return storageInventory;
	}
	public void setStorageInventory(StorageInventoryRs storageInventory) {
		this.storageInventory = storageInventory;
	}
	
	@XmlElementWrapper(name="scriptProcesses")
	@XmlElementRef()
	public List<ScriptProcessRs> getScriptsProcesses() {
		return scriptsProcesses;
	}
	public void setScriptsProcesses(List<ScriptProcessRs> scriptsProcesses) {
		this.scriptsProcesses = scriptsProcesses;
	}
	public ScriptProcessRs getScriptProcess() {
		return scriptProcess;
	}
	public void setScriptProcess(ScriptProcessRs scriptProcess) {
		this.scriptProcess = scriptProcess;
	}
	
	@XmlElementWrapper(name="advancedStorages")
	@XmlElementRef()
	public List<AdvancedStorageRs> getAdvancedStorages() {
		return advancedStorages;
	}
	public void setAdvancedStorages(List<AdvancedStorageRs> advancedStorages) {
		this.advancedStorages = advancedStorages;
	}
	public AdvancedStorageRs getAdvancedStorage() {
		return advancedStorage;
	}
	public void setAdvancedStorage(AdvancedStorageRs advancedStorage) {
		this.advancedStorage = advancedStorage;
	}
	
	@XmlElementWrapper(name="templateJobs")
	@XmlElementRef()
	public List<TemplateJobRs> getTemplateJobs() {
		return templateJobs;
	}
	public void setTemplateJobs(List<TemplateJobRs> templateJobs) {
		this.templateJobs = templateJobs;
	}
	public TemplateJobRs getTemplateJob() {
		return templateJob;
	}
	public void setTemplateJob(TemplateJobRs templateJob) {
		this.templateJob = templateJob;
	}
	
	@XmlElementWrapper(name="groups")
	@XmlElementRef()
	public List<GroupRs> getGroups() {
		return groups;
	}
	public void setGroups(List<GroupRs> groups) {
		this.groups = groups;
	}
	public GroupRs getGroup() {
		return group;
	}
	public void setGroup(GroupRs group) {
		this.group = group;
	}
	public GroupJobConfigRs getGroupJobConfiguration() {
		return groupJobConfiguration;
	}
	public void setGroupJobConfiguration(GroupJobConfigRs groupJobConfiguration) {
		this.groupJobConfiguration = groupJobConfiguration;
	}
}
