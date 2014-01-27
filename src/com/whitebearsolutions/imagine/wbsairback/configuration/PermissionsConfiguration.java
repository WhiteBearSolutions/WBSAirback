package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.RoleManager;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedApplication;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedBackup;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedGroupJob;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedRemoteInventory;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedRemoteStorage;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedScriptProcess;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedStep;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedSystem;
import com.whitebearsolutions.imagine.wbsairback.servlets.AdvancedTemplateJob;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupCategories;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupClients;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupFiles;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupFilesetsClients;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupFilesetsLocal;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupFilesetsNDMP;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupHypervisors;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupJobs;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupJobsExtended;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupLog;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupPools;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupScheduleDaily;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupScheduleMonthly;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupScheduleWeekly;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupScheduleYearly;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupStorageDisk;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupStorageLibrary;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupStorageRemote;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupStorageTape;
import com.whitebearsolutions.imagine.wbsairback.servlets.BackupSummary;
import com.whitebearsolutions.imagine.wbsairback.servlets.DeviceDisk;
import com.whitebearsolutions.imagine.wbsairback.servlets.DeviceISCSI;
import com.whitebearsolutions.imagine.wbsairback.servlets.DeviceNAS;
import com.whitebearsolutions.imagine.wbsairback.servlets.DeviceQuota;
import com.whitebearsolutions.imagine.wbsairback.servlets.DeviceReplication;
import com.whitebearsolutions.imagine.wbsairback.servlets.LibraryOperations;
import com.whitebearsolutions.imagine.wbsairback.servlets.SystemStatistics;
import com.whitebearsolutions.imagine.wbsairback.servlets.TapeOperations;

public class PermissionsConfiguration {

	private final static Logger logger = LoggerFactory.getLogger(PermissionsConfiguration.class);
	
	private TreeMap<String, Map<String, List<Integer>>> privileges;
	private TreeMap<String, List<String>> generalSections;
	private Map<String, String> defaultUrl;
	private List<String> backupUris; 
	
	public PermissionsConfiguration() {
		privileges = new TreeMap<String, Map<String, List<Integer>>>();
		generalSections = new TreeMap<String, List<String>>();
		defaultUrl = new TreeMap<String, String>();
		List<Integer> emptyUnauthorized = new ArrayList<Integer>();
		
		Map<String, List<Integer>> role = new TreeMap<String, List<Integer>>();
		role.put(BackupSummary.baseUrl, emptyUnauthorized);
		role.put(AdvancedApplication.baseUrl, emptyUnauthorized);
		role.put(AdvancedBackup.baseUrl, emptyUnauthorized);
		role.put(AdvancedGroupJob.baseUrl, emptyUnauthorized);
		role.put(AdvancedRemoteInventory.baseUrl, emptyUnauthorized);
		role.put(AdvancedRemoteStorage.baseUrl, emptyUnauthorized);
		role.put(AdvancedScriptProcess.baseUrl, emptyUnauthorized);
		role.put(AdvancedStep.baseUrl, emptyUnauthorized);
		role.put(AdvancedSystem.baseUrl, emptyUnauthorized);
		role.put(AdvancedTemplateJob.baseUrl, emptyUnauthorized);
		role.put(BackupCategories.baseUrl, emptyUnauthorized);
		role.put(BackupClients.baseUrl, emptyUnauthorized);
		role.put(BackupFiles.baseUrl, emptyUnauthorized);
		role.put(BackupFilesetsClients.baseUrl, emptyUnauthorized);
		role.put(BackupFilesetsLocal.baseUrl, emptyUnauthorized);
		role.put(BackupFilesetsNDMP.baseUrl, emptyUnauthorized);
		role.put(BackupHypervisors.baseUrl, emptyUnauthorized);
		role.put(BackupJobs.baseUrl, emptyUnauthorized);
		role.put(BackupJobsExtended.baseUrl, emptyUnauthorized);
		role.put(BackupLog.baseUrl, emptyUnauthorized);
		role.put(BackupPools.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleDaily.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleMonthly.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleWeekly.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleYearly.baseUrl, emptyUnauthorized);
		role.put(BackupStorageDisk.baseUrl, emptyUnauthorized);
		role.put(BackupStorageLibrary.baseUrl, emptyUnauthorized);
		role.put(BackupStorageRemote.baseUrl, emptyUnauthorized);
		role.put(BackupStorageTape.baseUrl, emptyUnauthorized);
		role.put(LibraryOperations.baseUrl, emptyUnauthorized);
		role.put(TapeOperations.baseUrl, emptyUnauthorized);
		role.put(SystemStatistics.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleGlobalOperator, role);
		generalSections.put(RoleManager.roleGlobalOperator, Arrays.asList(new String[]{"Backup", "Filesets", "Advanced", "AdvancedConfiguration", "AdvancedTemplateStep", "AdvancedInventory", "SystemStatistics"}));
		defaultUrl.put(RoleManager.roleGlobalOperator, BackupSummary.baseUrl);
		
		role = new TreeMap<String, List<Integer>>();
		role.put(BackupSummary.baseUrl, emptyUnauthorized);
		role.put(BackupClients.baseUrl, emptyUnauthorized);
		role.put(BackupFiles.baseUrl, emptyUnauthorized);
		role.put(BackupHypervisors.baseUrl, emptyUnauthorized);
		role.put(BackupJobs.baseUrl, emptyUnauthorized);
		role.put(BackupJobsExtended.baseUrl, emptyUnauthorized);
		role.put(BackupLog.baseUrl, emptyUnauthorized);
		role.put(BackupPools.baseUrl, emptyUnauthorized);
		role.put(BackupStorageDisk.baseUrl, Arrays.asList(new Integer[]{BackupStorageDisk.DISK_ADD,BackupStorageDisk.DISK_DELETE}));
		role.put(BackupStorageLibrary.baseUrl, Arrays.asList(new Integer[]{BackupStorageLibrary.LIBRARY_ADD,BackupStorageLibrary.LIBRARY_DELETE}));
		role.put(BackupStorageRemote.baseUrl, Arrays.asList(new Integer[]{BackupStorageRemote.REMOTE_ADD,BackupStorageRemote.REMOTE_DELETE}));
		role.put(BackupStorageTape.baseUrl, Arrays.asList(new Integer[]{BackupStorageTape.TAPE_ADD,BackupStorageTape.TAPE_DELETE}));
		role.put(LibraryOperations.baseUrl, emptyUnauthorized);
		role.put(TapeOperations.baseUrl, emptyUnauthorized);
		role.put(SystemStatistics.baseUrl, emptyUnauthorized);
		role.put(AdvancedBackup.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleOperator, role);
		generalSections.put(RoleManager.roleOperator, Arrays.asList(new String[]{"Backup", "Advanced", "SystemStatistics"}));
		defaultUrl.put(RoleManager.roleOperator, BackupSummary.baseUrl);
		
		role = new TreeMap<String, List<Integer>>();
		role.put(BackupSummary.baseUrl, emptyUnauthorized);
		role.put(BackupClients.baseUrl, Arrays.asList(new Integer[]{BackupClients.EDIT_CLIENT,BackupClients.NEW_CLIENT, BackupClients.REMOVE_CLIENT, BackupClients.STORE_CLIENT}));
		role.put(BackupJobs.baseUrl, Arrays.asList(new Integer[]{BackupJobs.CANCELJOB, BackupJobs.CREATEJOB, BackupJobs.DELETEJOB, BackupJobs.LAUNCHJOB, BackupJobs.PRUNEJOBS, BackupJobs.RESTARTJOB, BackupJobs.REMOVEJOB, BackupJobs.SAVEJOB, BackupJobs.STOPJOB}));
		role.put(BackupJobsExtended.baseUrl, Arrays.asList(new Integer[]{BackupJobsExtended.CANCELJOB, BackupJobsExtended.RESTARTJOB, BackupJobsExtended.STOPJOB}));
		role.put(BackupLog.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleUser, role);
		generalSections.put(RoleManager.roleUser, Arrays.asList(new String[]{"Backup"}));
		defaultUrl.put(RoleManager.roleUser, BackupSummary.baseUrl);
		
		
		role = new TreeMap<String, List<Integer>>();
		role.put(BackupStorageDisk.baseUrl, emptyUnauthorized);
		role.put(BackupStorageLibrary.baseUrl, emptyUnauthorized);
		role.put(BackupStorageRemote.baseUrl, emptyUnauthorized);
		role.put(BackupStorageTape.baseUrl, emptyUnauthorized);
		role.put(BackupFilesetsClients.baseUrl, emptyUnauthorized);
		role.put(BackupFilesetsLocal.baseUrl, emptyUnauthorized);
		role.put(BackupFilesetsNDMP.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleDaily.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleMonthly.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleWeekly.baseUrl, emptyUnauthorized);
		role.put(BackupScheduleYearly.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleCoordinator, role);
		generalSections.put(RoleManager.roleCoordinator, Arrays.asList(new String[]{"Backup", "Filesets",}));
		defaultUrl.put(RoleManager.roleCoordinator, BackupStorageDisk.baseUrl);
		
		role = new TreeMap<String, List<Integer>>();
		role.put(AdvancedApplication.baseUrl, emptyUnauthorized);
		role.put(AdvancedSystem.baseUrl, emptyUnauthorized);
		role.put(AdvancedScriptProcess.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleAdInventoryAppSo, role);
		generalSections.put(RoleManager.roleAdInventoryAppSo, Arrays.asList(new String[]{"Backup", "Advanced", "AdvancedConfiguration", "AdvancedInventory"}));
		defaultUrl.put(RoleManager.roleAdInventoryAppSo, AdvancedApplication.baseUrl);
		
		role = new TreeMap<String, List<Integer>>();
		role.put(AdvancedRemoteInventory.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleAdInventoryRemote, role);
		generalSections.put(RoleManager.roleAdInventoryRemote, Arrays.asList(new String[]{"Backup", "Advanced", "AdvancedConfiguration", "AdvancedInventory"}));
		defaultUrl.put(RoleManager.roleAdInventoryRemote, AdvancedRemoteInventory.baseUrl);
		
		role = new TreeMap<String, List<Integer>>();
		role.put(AdvancedRemoteStorage.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleAdAdvancedStorage, role);
		generalSections.put(RoleManager.roleAdAdvancedStorage, Arrays.asList(new String[]{"Backup", "Advanced", "AdvancedConfiguration"}));
		defaultUrl.put(RoleManager.roleAdAdvancedStorage, AdvancedRemoteStorage.baseUrl);
		
		role = new TreeMap<String, List<Integer>>();
		role.put(AdvancedTemplateJob.baseUrl, emptyUnauthorized);
		role.put(AdvancedStep.baseUrl, emptyUnauthorized);
		role.put(AdvancedBackup.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleAdCoordinator, role);
		generalSections.put(RoleManager.roleAdCoordinator, Arrays.asList(new String[]{"Backup", "Advanced", "AdvancedTemplateStep"}));
		defaultUrl.put(RoleManager.roleAdCoordinator, AdvancedTemplateJob.baseUrl);
		
		role = new TreeMap<String, List<Integer>>();
		role.put(DeviceDisk.baseUrl, emptyUnauthorized);
		role.put(DeviceISCSI.baseUrl, emptyUnauthorized);
		role.put(DeviceNAS.baseUrl, emptyUnauthorized);
		role.put(DeviceQuota.baseUrl, emptyUnauthorized);
		role.put(DeviceReplication.baseUrl, emptyUnauthorized);
		role.put(SystemStatistics.baseUrl, emptyUnauthorized);
		privileges.put(RoleManager.roleStorageManager, role);
		generalSections.put(RoleManager.roleStorageManager, Arrays.asList(new String[]{"Storage", "SystemStatistics"}));
		defaultUrl.put(RoleManager.roleStorageManager, DeviceDisk.baseUrl);
		
		backupUris = new ArrayList<String>();
		backupUris.add(BackupSummary.baseUrl);
		backupUris.add(AdvancedApplication.baseUrl);
		backupUris.add(AdvancedBackup.baseUrl);
		backupUris.add(AdvancedGroupJob.baseUrl);
		backupUris.add(AdvancedRemoteInventory.baseUrl);
		backupUris.add(AdvancedRemoteStorage.baseUrl);
		backupUris.add(AdvancedScriptProcess.baseUrl);
		backupUris.add(AdvancedStep.baseUrl);
		backupUris.add(AdvancedSystem.baseUrl);
		backupUris.add(AdvancedTemplateJob.baseUrl);
		backupUris.add(BackupCategories.baseUrl);
		backupUris.add(BackupClients.baseUrl);
		backupUris.add(BackupFiles.baseUrl);
		backupUris.add(BackupFilesetsClients.baseUrl);
		backupUris.add(BackupFilesetsLocal.baseUrl);
		backupUris.add(BackupFilesetsNDMP.baseUrl);
		backupUris.add(BackupHypervisors.baseUrl);
		backupUris.add(BackupJobs.baseUrl);
		backupUris.add(BackupJobsExtended.baseUrl);
		backupUris.add(BackupLog.baseUrl);
		backupUris.add(BackupPools.baseUrl);
		backupUris.add(BackupScheduleDaily.baseUrl);
		backupUris.add(BackupScheduleMonthly.baseUrl);
		backupUris.add(BackupScheduleWeekly.baseUrl);
		backupUris.add(BackupScheduleYearly.baseUrl);
		backupUris.add(BackupStorageDisk.baseUrl);
		backupUris.add(BackupStorageLibrary.baseUrl);
		backupUris.add(BackupStorageRemote.baseUrl);
		backupUris.add(BackupStorageTape.baseUrl);
		backupUris.add(LibraryOperations.baseUrl);
		backupUris.add(TapeOperations.baseUrl);
		
		backupUris.add("advanced/step");
		backupUris.add("advanced/templatejob");
		backupUris.add("advanced/inventory/storage");
		backupUris.add("advanced/inventory/system");
		backupUris.add("advanced/inventory/application");
		backupUris.add("advanced/storage");
		backupUris.add("advanced/script");
		backupUris.add("groupjob");
		backupUris.add("pools");
		backupUris.add("storage/disks");
		backupUris.add("storage/tapes");
		backupUris.add("storage/remotes");
		backupUris.add("storage/libraries");
		backupUris.add("clients");
		backupUris.add("jobs");
		backupUris.add("files");
		backupUris.add("hypervisors");
		backupUris.add("schedules");
																																			backupUris.add("filesets");
	}
	
	public boolean isAllowed(List<String> roles, String uri, Integer type) {
		try {
			if (roles != null && !roles.isEmpty()) {
				if (uri != null && !uri.isEmpty()) {
					for (String r : roles) {
						if (r.equals(RoleManager.roleAdmin))
							return true;
						else if (privileges.containsKey(r) && uri != null && !uri.isEmpty()) {
							Map<String, List<Integer>> urisAllowed = privileges.get(r);
							if (urisAllowed.containsKey(uri)) {
								List<Integer> unallowedSections = urisAllowed.get(uri);
								if (type != null && type > 0) {
									if (!unallowedSections.contains(type))
										return true;
								} else
									return true;
							}
						}
					}
				}
			}
			return false;
		} catch (Exception ex) {
			logger.error("ERROR al comprobar si [{}] tiene permitido el acceso a [{}] con type {}. Ex: {}", new Object[]{roles, uri, type, ex.getMessage()});
			return true;
		}
	}
	
	public boolean hasSection(List<String> roles, String section) {
		try {
			if (roles != null && !roles.isEmpty()) {
				for (String r : roles) {
					if (r.equals(RoleManager.roleAdmin))
						return true;
					else if (generalSections.containsKey(r) && section != null && !section.isEmpty()) {
						List<String> sections = generalSections.get(r);
						if (sections.contains(section))
							return true;
					}
				}
			}
			return false;
		} catch (Exception ex) {
			logger.error("ERROR al comprobar si [{}] tiene permitido el acceso a la seccion general [{}]. Ex: {}", new Object[]{roles, section, ex.getMessage()});
			return true;
		}
	}
	
	public List<String> getAllowedUris(String r) {
		if (privileges.containsKey(r))
			return new ArrayList<String>(privileges.get(r).keySet());
		else
			return null;
	}
	
	public String getDefaultUrl(List<String> r) {
		if (r != null) {
			if (r.contains(RoleManager.roleAdmin)) {
				return defaultUrl.get(RoleManager.roleAdmin);
			} else if (r.contains(RoleManager.roleGlobalOperator)) {
				return defaultUrl.get(RoleManager.roleGlobalOperator);
			} else if (r.contains(RoleManager.roleStorageManager)) {
				return defaultUrl.get(RoleManager.roleStorageManager);
			} else {
				return defaultUrl.get(r.iterator().next());
			}
		} else {
			return BackupSummary.baseUrl;
		}
	}
	
	public boolean isBackup(String uri) {
		return backupUris.contains(uri);
	}
}
