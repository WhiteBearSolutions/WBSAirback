package com.whitebearsolutions.imagine.wbsairback.disk.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.imagine.wbsairback.net.HACommClient;
import com.whitebearsolutions.util.Command;

public class FileSystemManager {
	public static final int FS_XFS = 1;
	public static final int FS_BTRFS = 3;
	public static final int FS_ZFS = 4;
	public static Map<Integer, String> SUPPORTED_FILESYSTEMS;
	
	private final static Logger logger = LoggerFactory.getLogger(FileSystemManager.class);
	
	static{
		SUPPORTED_FILESYSTEMS = new HashMap<Integer, String>();
		SUPPORTED_FILESYSTEMS.put(FS_XFS, "xfs");
		SUPPORTED_FILESYSTEMS.put(FS_BTRFS, "btrfs");
		SUPPORTED_FILESYSTEMS.put(FS_ZFS, "zfs");
	}
	
	public FileSystemManager() {}
	
	
	/**
	 * Actualiza el campo que indica al watchdog si ha de remontar o no un volumen en caso de encontrarlo desmontado. Si se desmontó manualmente,
	 * el watchdog no re-montara ese volumen
	 * @param group
	 * @param name
	 * @param remount
	 * @throws Exception
	 */
	public static void updateRemountValueToFstab(String group, String name, boolean remount) throws Exception {
		Map<String, String[]> _mounts = readFstab();
		if (_mounts.containsKey(VolumeManager.getMountPath(group, name))) {
			String[] _valuesArr = _mounts.get(VolumeManager.getMountPath(group, name));
			int type = FileSystemManager.FS_XFS;
			if (_valuesArr[2].equals("btrfs"))
				type = FileSystemManager.FS_BTRFS;
			else if (_valuesArr[2].equals("zfs"))
				type = FileSystemManager.FS_ZFS;
			List<String> values = fillMountValues(type, group, name, _valuesArr[3], remount);
			_mounts.put(VolumeManager.getMountPath(group, name), values.toArray(new String[] {}));
			writeFstab(_mounts);
		}
	}
	
	public static List<String> fillMountValues(int type, String group, String name, String options, boolean remount) throws Exception {
		Map<String, String> _volume = VolumeManager.getLogicalVolume(group, name);
		List<String> _values = new ArrayList<String>();
		_values.add(_volume.get("path"));
		_values.add(VolumeManager.getMountPath(group, name));
		switch(type) {
			case FileSystemManager.FS_XFS: {
					_values.add("xfs");
					if(options == null) {
						_values.add("defaults,usrquota,grpquota");
					} else {
						_values.add(options);
					}
					
				}
				break;
			case FileSystemManager.FS_BTRFS: {
					_values.add("btrfs");
					if(options == null) {
						_values.add("defaults");
					} else {
						_values.add(options);
					}
				}
				break;
			case FileSystemManager.FS_ZFS: {
					_values.add("zfs");
					if(options == null) {
						_values.add("defaults");
					} else {
						_values.add(options);
					}
				}
				break;
			default: {
					throw new Exception("invalid filesystem type");
				}
		}
		_values.add("0");
		_values.add("0");
		if (remount == true) {
			_values.add("1");
		} else {
			_values.add("0");
		}
		return _values;
	}
	
	public static void addMountToFstab(int type, String group, String name, String options) throws Exception {
		Map<String, String[]> _mounts = readFstab();
		if(!_mounts.containsKey(VolumeManager.getMountPath(group, name))) {
			logger.info("Añadiendo mount a fstab: {}/{}",group, name);
			List<String> _values = fillMountValues(type, group, name, options, true);
			_mounts.put(_values.get(0), _values.toArray(new String[] {}));
			writeFstab(_mounts);
		}
	}
	
	public static void addSnapshotMountToFstab(String group, String name, String snapshot) throws Exception {
		Map<String, String> _volume = VolumeManager.getLogicalVolumeSnapshot(group, name, snapshot);
		Map<String, String[]> _mounts = readFstab();
		if(!_mounts.containsKey(VolumeManager.getMountPath(group, snapshot))) {
			List<String> _values = new ArrayList<String>();
			_values.add(_volume.get("path"));
			_values.add(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot));
			_values.add("xfs");
			_values.add("nouuid,ro");
			_values.add("0");
			_values.add("0");
			_mounts.put(_values.get(0), _values.toArray(new String[] {}));
			writeFstab(_mounts);
		}
	}
	
	public static void createFileSystem(int type, String group, String name, String compression, boolean encryption) throws Exception {
		switch(type) {
			case FS_XFS: {
					XFSConfiguration.createFileSystem(group, name, compression, encryption);
				}
				break;
			case FS_BTRFS: {
					BTRFSConfiguration.createFileSystem(group, name, compression, encryption);
				}
				break;
			case FS_ZFS: {
					ZFSConfiguration.createFileSystem(group, name, compression, encryption, true);
				}
				break;
		}
	}
	
	public static void createFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		Map<String, String> _volume = VolumeManager.getLogicalVolume(group, name);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _volume.get("fstype"))) {
			throw new Exception("filesystem does not support snapshots");
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _volume.get("fstype"))) {
			ZFSConfiguration.createFileSystemSnapshot(group, name, snapshot);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _volume.get("fstype"))) {
			BTRFSConfiguration.createFileSystemSnapshot(group, name, snapshot);
		}
	}
	
	public static boolean equalsFilesystemType(int type, String name) {
		if(name == null) {
			return false;
		}
		if(SUPPORTED_FILESYSTEMS.containsKey(type)) {
			if(SUPPORTED_FILESYSTEMS.get(type).equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static Map<String, String> getFileSystemParameters(String group, String name) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, name);
		Map<String, String> _parameters = new HashMap<String, String>();
		_parameters.put("type", _type);
		if(equalsFilesystemType(FS_ZFS, _type)) {
			_parameters.putAll(ZFSConfiguration.getVolume(group, name, true));
		} else if(equalsFilesystemType(FS_BTRFS, _type)) {
			Map<String, String> _btrfs_parameters = BTRFSConfiguration.getFileSystemParameters(group, name);
			_parameters.put("used", _btrfs_parameters.get("used"));
			Map<String, String[]> _fstab = readFstab();
			String[] _values = _fstab.get(VolumeManager.getLogicalVolumeMountPath(group, name));
			StringTokenizer _st = new StringTokenizer(_values[2], ",");
			if(_st.hasMoreTokens()) {
				for(String _token = _st.nextToken(); _st.hasMoreTokens(); _token = _st.nextToken()) {
					if(_token.startsWith("compress=")) {
						_parameters.put("compression", _token.substring(_token.indexOf("=") + 1));
						break;
					}
				}
			}
			if(_parameters.get("compression") == null) {
				_parameters.put("compression", "none");
			}
		} else if(equalsFilesystemType(FS_XFS, _type)) {
			_parameters.putAll(XFSConfiguration.getFileSystemParameters(group, name));
		}	
		return _parameters;
	}
	
	public static Map<String, String> getFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, name);
		if(equalsFilesystemType(FS_XFS, _type)) {
			throw new Exception("filesystem does not support snapshots");
		} else if(equalsFilesystemType(FS_ZFS, _type)) {
			return ZFSConfiguration.getFileSystemSnapshot(group, name, snapshot);
		} else if(equalsFilesystemType(FS_BTRFS, _type)) {
			//return BTRFSConfiguration.getFileSystemSnapshot(group, name, snapshot);
		}
		return new HashMap<String,String>();
	}
	
	public static List<Map<String, String>> getFileSystemSnapshots(String group, String name) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, name);
		if(equalsFilesystemType(FS_XFS, _type)) {
			return new ArrayList<Map<String, String>>();
		} else if(equalsFilesystemType(FS_ZFS, _type)) {
			return ZFSConfiguration.getFileSystemSnapshots(group, name);
		} else if(equalsFilesystemType(FS_BTRFS, _type)) {
			return BTRFSConfiguration.getFileSystemSnapshots(group, name);
		}
		return new ArrayList<Map<String,String>>();
	}
	
	public static boolean isSupportedFilesystemType(String type) {
		if(SUPPORTED_FILESYSTEMS.containsValue(type)) {
			return true;
		}
		return false;
	}
	
	public static void mountFileSystem(String group, String name, String options) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, name);
		StringBuilder _sb = new StringBuilder();
		if(equalsFilesystemType(FS_XFS, _type)) {
			_sb.append("/bin/mount -t xfs ");
			_sb.append(VolumeManager.getLogicalVolumeDevicePath(group, name));
			_sb.append(" ");
			_sb.append(VolumeManager.getLogicalVolumeMountPath(group, name));
			if(options != null) {
				_sb.append(" -o ");
				_sb.append(options);
			}
			_sb.append(" && /bin/echo \"yes\" || /bin/echo \"no\"");
			String _output = Command.systemCommand(_sb.toString()).trim();
			if(!"yes".equals(_output)) {
				throw new Exception("unable to mount filesystem on logical volume");
			}
		} else if(equalsFilesystemType(FS_ZFS, _type)) {
			ZFSConfiguration.mountFileSystem(group, name);
		} else if(equalsFilesystemType(FS_BTRFS, _type)) {
			_sb.append("/bin/mount -t btrfs ");
			_sb.append(VolumeManager.getLogicalVolumeDevicePath(group, name));
			_sb.append(" ");
			_sb.append(VolumeManager.getLogicalVolumeMountPath(group, name));
			if(options != null) {
				_sb.append(" -o ");
				_sb.append(options);
			}
			_sb.append(" && /bin/echo \"yes\" || /bin/echo \"no\"");
			String _output = Command.systemCommand(_sb.toString()).trim();
			if(!"yes".equals(_output)) {
				throw new Exception("unable to mount filesystem on logical volume");
			}
		} else {
			throw new Exception("invalid filesystem type");
		}
	}
	
	public static void mountFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, name);
		if(equalsFilesystemType(FS_XFS, _type)) {
			throw new Exception("filesystem does not support snapshots");
		} else if(equalsFilesystemType(FS_ZFS, _type)) {
			ZFSConfiguration.mountFileSystemSnapshot(group, name, snapshot);
		} else if(equalsFilesystemType(FS_BTRFS, _type)) {
			// nothing
		} else {
			throw new Exception("invalid filesystem type");
		}
	}
	
	public static Map<String, String[]> readFstab() {
		Map<String, String[]> _mounts = new HashMap<String, String[]>();
		String path = WBSAirbackConfiguration.getFileFstab();
		if (HAConfiguration.isSlaveNode())
			path = WBSAirbackConfiguration.getFileTmpFstab();
		File _f = new File(path);
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						String[] _value = _line.split("(?<!\\\\) +");
						_mounts.put(_value[1], _value);
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {}
		}
		return _mounts;
	}
	
	public static Map<String, String[]> readOriginalFstab() {
		Map<String, String[]> _mounts = new HashMap<String, String[]>();
		String path = WBSAirbackConfiguration.getFileFstab();
		File _f = new File(path);
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						String[] _value = _line.split(" ");
						_mounts.put(_value[1], _value);
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {}
		}
		return _mounts;
	}
	
	public static Map<String, String[]> readTempFstab() {
		Map<String, String[]> _mounts = new HashMap<String, String[]>();
		File _f = new File(WBSAirbackConfiguration.getFileTmpFstab());
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						String[] _value = _line.split(" ");
						_mounts.put(_value[1], _value);
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {}
		}
		return _mounts;
	}
	
	public static void removeMountFromFstab(String group, String name) throws Exception {
		Map<String, String[]> _mounts = readFstab();
		if(_mounts.containsKey(VolumeManager.getMountPath(group, name))) {
			_mounts.remove(VolumeManager.getMountPath(group, name));
			logger.info("Eliminamos de fstab {}/{}", group, name);
			writeFstab(_mounts);
		}
	}
	
	public static void removeFileSystem(String group, String name) throws Exception {
		Map<String, String> _volume = VolumeManager.getLogicalVolume(group, name);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _volume.get("fstype"))) {
			// nothing
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _volume.get("fstype"))) {
			ZFSConfiguration.removeFileSystem(group, name);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _volume.get("fstype"))) {
			BTRFSConfiguration.removeFileSystem(group, name);
		}
		removeMountFromFstab(group, name);
	}
	
	public static void removeFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		Map<String, String> _volume = VolumeManager.getLogicalVolume(group, name);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _volume.get("fstype"))) {
			throw new Exception("filesystem does not support snapshots");
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _volume.get("fstype"))) {
			ZFSConfiguration.removeFileSystemSnapshot(group, name, snapshot);
		} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _volume.get("fstype"))) {
			BTRFSConfiguration.removeFileSystemSnapshot(group, name, snapshot);
		}
	}
	
	public static void removeSnapshotMountFromFstab(String group, String name, String snapshot) throws Exception {
		Map<String, String[]> _mounts = readFstab();
		String _path = VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot);
		if(_mounts.containsKey(_path)) {
			_mounts.remove(_path);
			writeFstab(_mounts);
		}
	}
	
	public static void resizeFileSystem(String group, String name, double size, Double data_size, Double total_reservation, Double data_reservation) throws Exception {
		logger.info("Se re-establece el tamaño del sistema {}/{} en {}",new Object[]{group, name, size});
		Map<String, String> _lv = VolumeManager.getLogicalVolume(group, name);
		String _type = _lv.get("fstype");
		if(equalsFilesystemType(FS_XFS, _type) && size != 0) {
			XFSConfiguration.resizeFileSystem(group, name);
		} else if(equalsFilesystemType(FS_ZFS, _type)) {
			double _size = 0;
			try {
				_size = Double.parseDouble(_lv.get("size-raw"));
			} catch(NumberFormatException _ex) {
				throw new Exception("cannot get the actual logical volume size");
			}
			_size += size;
			if (VolumeManager.isMount(group, name))
				ZFSConfiguration.extendFileSystem(group, name, _size, data_size, total_reservation, data_reservation);
			else
				ZFSConfiguration.extendZvol(group, name, _size, total_reservation, data_reservation);
		} else if(equalsFilesystemType(FS_BTRFS, _type) && size != 0) {
			BTRFSConfiguration.resizeFileSystem(group, name, size);
		} else {
			throw new Exception("invalid filesystem type");
		}
	}
	
	public static void setReplica(String group, String name, boolean replication, int port, String[] slave_address) throws Exception {
		String _type = VolumeManager.getLogicalVolumeFS(group, name);
		if(equalsFilesystemType(FS_ZFS, _type)) {
			VolumeManager.umountLogicalVolume(group, name);
			Thread.sleep(2000);
			/**
			 * TODO ZFS replication
			 */
			VolumeManager.mountLogicalVolume(group, name);
		}
	}
	
	private static void writeFstab(Map<String, String[]> mounts) throws Exception {
		StringBuilder _sb = new StringBuilder();
		for(String[] _mount : mounts.values()) {
			for(String _value : _mount) {
				_sb.append(_value);
				_sb.append(" ");
			}
			_sb.append("\n");
		}
		if (HAConfiguration.isActiveNode())
			FileSystem.writeFile(new File(WBSAirbackConfiguration.getFileFstab()), _sb.toString());
		
		if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode())
			HACommClient.sendFsTab();
	}
}
