package com.whitebearsolutions.imagine.wbsairback.disk;

import java.util.Map;

import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.XFSConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.ZFSConfiguration;

public class QuotaManager {
	
	public QuotaManager() {
		
	}
	
	public static Map<String, String> getVolumeUserQuota(String username, String group, String volume) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, volume);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _type)) {
			return XFSConfiguration.getUserQuota(group, volume, username);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
			return ZFSConfiguration.getUserQuota(group, volume, username);
		}
		throw new Exception("invalid filesystem type");
	}
	
	public static Map<String, String> getVolumeGroupQuota(String groupname, String group, String volume) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, volume);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _type)) {
			return XFSConfiguration.getGroupQuota(groupname, group, volume);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
			return ZFSConfiguration.getGroupQuota(group, volume, groupname);
		}
		throw new Exception("invalid filesystem type");
	}
	
	public static Map<String, Map<String, String>> searchVolumeGroupQuotas(String match, String group, String volume) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, volume);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _type)) {
			return XFSConfiguration.searchGroupQuotas(match, group, volume);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
			return ZFSConfiguration.searchGroupQuotas(match, group, volume);
		}
		throw new Exception("invalid filesystem type");
	}
	
	public static Map<String, Map<String, String>> searchVolumeUserQuotas(String match, String group, String volume) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, volume);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _type)) {
			return XFSConfiguration.searchUserQuotas(match, group, volume);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
			return ZFSConfiguration.searchUserQuotas(match, group, volume);
		}
		throw new Exception("invalid filesystem type");
	}
	
	public static void setVolumeGroupQuota(String groupname, String group, String volume, double size) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, volume);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _type)) {
			XFSConfiguration.setGroupQuota(groupname, group, volume, size);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
			ZFSConfiguration.setGroupQuota(groupname, group, volume, size);
		}
	}
	
	public static void setVolumeUserQuota(String username, String group, String volume, double size) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, volume);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _type)) {
			XFSConfiguration.setUserQuota(username, group, volume, size);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
			ZFSConfiguration.setUserQuota(username, group, volume, size);
		}
	}
}
