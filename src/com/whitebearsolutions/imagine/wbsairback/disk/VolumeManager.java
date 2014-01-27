package com.whitebearsolutions.imagine.wbsairback.disk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.BTRFSConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.ZFSConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.imagine.wbsairback.net.ReplicationManager;
import com.whitebearsolutions.imagine.wbsairback.util.ObjectLock;
import com.whitebearsolutions.imagine.wbsairback.virtual.HypervisorManager;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;
import com.whitebearsolutions.xml.db.XMLAttribute;
import com.whitebearsolutions.xml.db.XMLDB;
import com.whitebearsolutions.xml.db.XMLObject;

public class VolumeManager {
	public static final int VG_LVM = 1;
	public static final int VG_ZFS = 2;
	public static final int LV_STORAGE_NAS = 1;
	public static final int LV_STORAGE_SAN = 2;
	public static final int LV_LINEAR = 1;
	public static final int LV_STRIPED = 2;
	public static final int LV_MIRRROR = 3;
	public static final int LV_ZFS = 4;
	public static final int SIZE_LV_M = 1;
	public static final int SIZE_LV_G = 2;
	public static final int SIZE_LV_T = 3;
	public static final int LV_SNAPSHOT_MANUAL = 1;
	public static final int LV_SNAPSHOT_HOURLY = 2;
	public static final int LV_SNAPSHOT_DAILY = 3;
	public static final String _preffix = "REMOVE-VOLUME-";
	
	private final static Logger logger = LoggerFactory.getLogger(VolumeManager.class);
	
	private static void addLogicalMount(int type, String group, String name, String compression, boolean encryption) throws Exception {
		getLogicalVolume(group, name);
		File _f = new File(getMountPath(group, name));
		switch(type) {
			case FileSystemManager.FS_XFS: {
					try {
						if(_f.exists()) {
							throw new Exception("mount point already exists");
						}
						_f.mkdirs();
						FileSystemManager.createFileSystem(FileSystemManager.FS_XFS, group, name, null, false);
						FileSystemManager.addMountToFstab(type, group, name, null);
					} catch(Exception _ex) {
						_f.delete();
						throw _ex;
					}
				}
				break;
			case FileSystemManager.FS_BTRFS: {
					try {
						if(_f.exists()) {
							throw new Exception("mount point already exists");
						}
						_f.mkdirs();
						FileSystemManager.createFileSystem(FileSystemManager.FS_BTRFS, group, name, compression, encryption);
						if(BTRFSConfiguration.isSupportedCompression(compression)) {
							StringBuilder _sb = new StringBuilder();
							_sb.append("defaults,compress=");
							_sb.append(compression.toLowerCase());
							FileSystemManager.addMountToFstab(type, group, name, _sb.toString());
						} else {
							FileSystemManager.addMountToFstab(type, group, name, null);
						}
					} catch(Exception _ex) {
						_f.delete();
						throw _ex;
					}
				}
				break;
			case FileSystemManager.FS_ZFS: {
					ZFSConfiguration.setCompression(group, name, compression);
					FileSystemManager.addMountToFstab(type, group, name, null);
				}
				break;
		}
		
		try {
			if(!isLogicalVolumeMounted(group, name)) {
				mountLogicalVolume(group, name);
			}
		} catch(Exception _ex) {
			FileSystemManager.removeMountFromFstab(group, name);
			_f.delete();
			throw _ex;
		}
		
		Command.systemCommand("/bin/chmod 0777 " + _f.getAbsolutePath());
		File _recycle = new File(_f.getAbsolutePath() + "/.recycle");
		if(!_recycle.exists()) {
			_recycle.mkdirs();
		}
		Command.systemCommand("/bin/chmod 0777 " + _recycle.getAbsolutePath());
	}
	
	public static void addLogicalVolume(int storage_type, int volume_type, int fs_type, String group, String name, int size, int size_units, String compression, boolean encryption, boolean dedup, double percent_snap) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid logical volume name");
		}
		
		if(size < 1) {
			throw new Exception("invalid logical volume size");
		}
		
		if(size_units != SIZE_LV_M && size_units != SIZE_LV_G && size_units != SIZE_LV_T) {
			throw new Exception("invalid logical volume size units");
		}
		
		if(volume_type != LV_LINEAR && volume_type != LV_STRIPED && volume_type != LV_MIRRROR) {
			throw new Exception("invalid logical volume type");
		}
		
		List<String> _vgs = getVolumeGroupNames();
		if(!_vgs.contains(group)) {
			throw new Exception("volume group does not exists");
		}
		
		Map<String, String> _lv = null;
		try {
			_lv = getLogicalVolume(group, name);
		} catch(Exception _ex) {}
		if(_lv != null) {
			throw new Exception("logical volume already exists");
		}
		
		if(fs_type != FileSystemManager.FS_ZFS && !ZFSConfiguration.poolExists(group)) {
			if(fs_type == FileSystemManager.FS_BTRFS &&
					"none".equals(compression) &&
					!BTRFSConfiguration.isSupportedCompression(compression)) {
				throw new Exception("unsupported compression type");
			}
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("/sbin/lvcreate -n ");
			_sb.append(name);
			switch(volume_type) {
				case LV_STRIPED: {
						List<String> _pvs = getPhysicalVolumeNames(group);
						_sb.append(" -i ");
						_sb.append(_pvs.size());
					}
					break;
				case LV_MIRRROR: {
						_sb.append(" -m 1");
					}
					break;
			}
			_sb.append(" -L ");
			_sb.append(size);
			switch(size_units) {
				case SIZE_LV_G:
					_sb.append("G");
					break;
				case SIZE_LV_T:
					_sb.append("T");
					break;
				default: {
					// son megas
					if (size < 16)
						throw new Exception("<m>device.logical_volumes.disk.too.small</m>");
					break;
				}
			}
			_sb.append(" ");
			_sb.append(group);
			Command.systemCommand(_sb.toString());
			logger.info("Nuevo volumen XFS: {}", _sb.toString());
		} else if(fs_type == FileSystemManager.FS_ZFS && ZFSConfiguration.poolExists(group)) {
			if(storage_type == VolumeManager.LV_STORAGE_NAS) {
				ZFSConfiguration.createFileSystem(group, name, null, false, dedup);
				try {
					ZFSConfiguration.resizeFileSystemInit(group, name, getSize(size, size_units), percent_snap);
				} catch(Exception _ex) {
					// Reintentamos hasta 8 veces borrar el volumen con intervalos de 250 milisegundos
					int _time_wait=250;
					int _retrys=8;
					boolean _success=false;
					Exception cantRemoveEx=null;
					
					while (_retrys>0 && !_success) {
						try {
							Thread.sleep(_time_wait);
							ZFSConfiguration.removeFileSystem(group, name);
							_success=true;
						} catch (Exception ex) {
							_retrys--;	
							cantRemoveEx=ex;
						}
					}
					if (!_success)
						throw cantRemoveEx;
					else
						throw _ex;
				}
			} else {
				ZFSConfiguration.createZVol(group, name, getSize(size, size_units), percent_snap, compression, dedup);
			}
		} else {
			throw new Exception("<m>zfs.error.invalid.filesystem</m>");
		}
		
		if(storage_type == VolumeManager.LV_STORAGE_NAS) {
			addLogicalMount(fs_type, group, name, compression, encryption);
		}
	}
	
	public static void addVolumeGroupDevice(int type, String group, String[] devices) throws Exception {
		int r = 0;
		if(devices == null) {
			return;
		}
		logger.info("Añadiendo agregado: {} => {}", group, devices);
		switch(type) {
			case VG_LVM: {
					List<String> _vgs = getVolumeGroupNames();
					for(String device : devices) {
						Command.systemCommand("/bin/dd if=/dev/zero of=" + device + " bs=512 count=1");
						Thread.sleep(200);
						Command.systemCommand("/sbin/pvcreate " + device + " --force");
						if(r > 0 || _vgs.contains(group)) {
							Command.systemCommand("/sbin/vgextend " + group + " " + device);
						} else {
							Command.systemCommand("/sbin/vgcreate " + group + " " + device);
						}
						r++;
					}
				}
				break;
			case VG_ZFS: {
					ZFSConfiguration.createPool(group, Arrays.asList(devices));
				}
				break;
		}
	}
	
	public static void createLogicalVolumeSnapshot(String group, String name, int type) throws Exception {
		if(type != LV_SNAPSHOT_MANUAL && type != LV_SNAPSHOT_HOURLY && type != LV_SNAPSHOT_DAILY) {
			throw new Exception("invalid logical volume snapshot type");
		}
		
		Map<String, String> _volume = getLogicalVolume(group, name);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _volume.get("fstype"))) {
			throw new Exception("filesystem type does not support snapshots");
		}
		
		Calendar _cal = Calendar.getInstance();
		StringBuilder _sb = new StringBuilder();
		_sb.append("GMT-");
		_sb.append(_cal.get(Calendar.YEAR));
		_sb.append(".");
		_sb.append(formatNumber(_cal.get(Calendar.MONTH) + 1));
		_sb.append(".");
		_sb.append(formatNumber(_cal.get(Calendar.DAY_OF_MONTH)));
		_sb.append("-");
		_sb.append(formatNumber(_cal.get(Calendar.HOUR_OF_DAY)));
		_sb.append(".");
		_sb.append(formatNumber(_cal.get(Calendar.MINUTE)));
		_sb.append(".");
		_sb.append(formatNumber(_cal.get(Calendar.SECOND)));
		
		String _snapshot = _sb.toString();
		if(isMount(group, name)) {
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _volume.get("fstype")) ||
					FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _volume.get("fstype"))) {
				_sb = new StringBuilder();
				_sb.append(getMountPath(group, name));
				_sb.append("/.snapshots/");
				File _snapshots_dir = new File(_sb.toString());
				if(!_snapshots_dir.exists()) {
					_snapshots_dir.mkdirs();
				}
				if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _volume.get("fstype"))) {
					ZFSConfiguration.createFileSystemSnapshot(group, name, _snapshot);
					ZFSConfiguration.mountFileSystemSnapshot(group, name, _snapshot);
				} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _volume.get("fstype"))) {
					BTRFSConfiguration.createFileSystemSnapshot(group, name, _snapshot);
				}
				indexLogicalVolumeSnapshot(type, group, name, _snapshot);
			}
		} else if(ISCSIManager.isVolumeTarget(group, name)) {
			double _size = 0;
			try {
				_size = Double.parseDouble(_volume.get("size-raw"));
			} catch(NumberFormatException _ex) {}
			
			_sb = new StringBuilder();
			_sb.append("/sbin/lvcreate -n ");
			_sb.append(_snapshot);
			_sb.append(" -L ");
			_sb.append(_size);
			_sb.append(" -s ");
			_sb.append(getLogicalVolumeDevicePath(group, name));
			Command.systemCommand(_sb.toString());
			ISCSIManager.addVolumeSnapshotTargets(group, name, _snapshot);
		}
	}
	
	public static void extendLogicalVolume(String group, String name, int size, int size_type, Double data_size, Double total_reservation, Double data_reservation) throws Exception {
		if(size_type != SIZE_LV_M && size_type != SIZE_LV_G && size_type != SIZE_LV_T) {
			throw new Exception("invalid logical volume size type");
		}
		logger.info("Se extiende volumen {}/{} con tamaño {} tipo {}", new Object[]{group, name, size, size_type});
		
		Map<String, String> _lv = getLogicalVolume(group, name);
		if(size < 0) {
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype"))) {
				throw new Exception("filesystem cannot be shrinked");
			} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
				FileSystemManager.resizeFileSystem(group, name, getSize(size, size_type), data_size, total_reservation, data_reservation);
				return;
			} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _lv.get("fstype"))) {
				FileSystemManager.resizeFileSystem(group, name, getSize(size, size_type), data_size, total_reservation, data_reservation);
			}
			
			StringBuilder _sb = new StringBuilder();
			switch(size_type) {
				case SIZE_LV_M:
					_sb.append("/sbin/lvreduce -f -L");
					_sb.append(size);
					_sb.append(" ");
					_sb.append(getLogicalVolumeDevicePath(group, name));
					break;
				case SIZE_LV_G:
					_sb.append("/sbin/lvreduce -f -L");
					_sb.append(size);
					_sb.append("G ");
					_sb.append(getLogicalVolumeDevicePath(group, name));
					break;
				case SIZE_LV_T:
					_sb.append("/sbin/lvreduce -f -L");
					_sb.append(size);
					_sb.append("T ");
					_sb.append(getLogicalVolumeDevicePath(group, name));
					break;
			}
			try {
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				logger.error("logical volume device cannot be shrinked - {}", _ex.getMessage());
				throw new Exception("logical volume device cannot be shrinked - " + _ex.toString());
			}
		} else if(size >= 0) {
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype")) && VolumeManager.isMount(group, name)) {
				if (size > 0) {
					StringBuilder _sb = new StringBuilder();
					switch(size_type) {
						case SIZE_LV_M:
							_sb.append("/sbin/lvextend -L+");
							_sb.append(size);
							_sb.append(" ");
							_sb.append(getLogicalVolumeDevicePath(group, name));
							break;
						case SIZE_LV_G:
							_sb.append("/sbin/lvextend -L+");
							_sb.append(size);
							_sb.append("G ");
							_sb.append(getLogicalVolumeDevicePath(group, name));
							break;
						case SIZE_LV_T:
							_sb.append("/sbin/lvextend -L+");
							_sb.append(size);
							_sb.append("T ");
							_sb.append(getLogicalVolumeDevicePath(group, name));	
							break;
					}
					try {
						Command.systemCommand(_sb.toString());
					} catch(Exception _ex) {
						logger.error("logical volume device cannot be extended - " + _ex);
						throw new Exception("logical volume device cannot be extended - " + _ex.toString());
					}
				}
			}
			
			if(VolumeManager.isMount(group, name) || FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
				FileSystemManager.resizeFileSystem(group, name, getSize(size, size_type),  data_size, total_reservation, data_reservation);
			}
		}
	}
	
	/**
	 * Busca el disco /dev/sdx asociado a cierto id de disco
	 * @param device
	 * @return
	 * @throws Exception
	 */
	public static String findDevSdFromDeviceId(String id) throws Exception {
		if (id.contains("/mapper/")) { 	//multipath
			return id;
		}
		
		// Resto
		String deviceSd = id;

		if (id.contains("by-id/"))
			deviceSd = id.substring(id.indexOf("by-id/")+"by-id/".length());
		else if (id.contains("dev/"))
			deviceSd = id.substring(id.indexOf("dev/")+"dev/".length());
		
		String output = Command.systemCommand("ls -l /dev/disk/by-id/ | grep \""+deviceSd+"\" | awk '{print $11}' | head -1");
		if (output != null && output.length()>0) {
			deviceSd = output.trim().replaceAll("../", "");
			deviceSd = "/dev/"+deviceSd;
		}
		return deviceSd;
	}
	
	public static String findDiskId(String device) throws Exception {
		String dev = findDiskPathById(device);
		if (dev != null && dev.contains("/mapper"))
			return dev.substring(dev.lastIndexOf("/")+1);
		else if (dev != null && dev.contains("by-id")) {
			dev = dev.substring(dev.lastIndexOf("/")+1);
			return dev.substring(dev.indexOf("-")+1);
		}
		else
			return dev;
	}
	
	/**
	 * Busca el id de un dispositivo dado
	 * @param device
	 * @return
	 * @throws Exception
	 */
	public static String findDiskPathById(String device) throws Exception {
		if (device.contains("/mapper/")) { 	//multipath
			return device;
		}
		// Resto
		String pathid = null;
		if (device.contains("/dev/"))
			device = device.substring(device.indexOf("/dev/")+"/dev/".length());
		String output = Command.systemCommand("ls -l /dev/disk/by-id/ | grep -w "+device+" | awk '{print $9}' | head -1");
		if (output != null && output.length()>0) {
			pathid = "/dev/disk/by-id/"+output.trim();
		} else {
			pathid = device;
		}
			
		return pathid;
	}
	
	private static String formatNumber(int n) {
		DecimalFormat _format = new DecimalFormat("00");
		return _format.format(n);
	}
	
	public static String formatNumber(long n) {
		DecimalFormat _format = new DecimalFormat("00");
		return _format.format(n);
	}
	
	
	public static String getBlockSize(String value) {
		float _size = 0;
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		try {
			_size = Float.parseFloat(value);
		} catch(NumberFormatException _ex) {}
		if(_size > 0) {
			_size = ((_size * 1024 * 1024)/1024)/1024;
		}
		if(_size > (1024 * 1024 * 1024)) {
			_sb.append(_df.format(_size / (1024 * 1024 * 1024)));
			_sb.append(" TB");
		} else if(_size > (1024 * 1024)) {
			_sb.append(_df.format(_size / (1024 * 1024)));
			_sb.append(" GB");
		} else if(_size > 1024) {
			_sb.append(_df.format(_size / 1024));
			_sb.append(" MB");
		} else {
			_sb.append(_df.format(_size));
			_sb.append(" KB");
		}
		return _sb.toString();
	}
	
	public static Float getByteSizeFromHuman(String value) {
		float _size = 0.0F;
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		char unit = value.toUpperCase().charAt(value.length() - 1);
		try {
			value = value.substring(0, value.length() - 1);
			_size = Float.parseFloat(value);
		} catch(NumberFormatException _ex) {}
		switch(unit) {
			case 'B':
				return _size;
			case 'K':
				return _size*1024F;
			case 'M':
				return _size*1024F*1024F;
			case 'G':
				return _size*1024F*1024F*1024F;
			case 'T':
				return _size*1024F*1024F*1024F*1024F;
		}
		return _size;
	}
	
	public static String getFormatSize(String value) {
		float _size = 0;
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		char unit = value.toUpperCase().charAt(value.length() - 1);
		StringBuilder _sb = new StringBuilder();
		try {
			value = value.substring(0, value.length() - 1);
			_size = Float.parseFloat(value);
		} catch(NumberFormatException _ex) {}
		switch(unit) {
			case 'B':
				if(_size >= 1099511627776F) {
					_sb.append(_df.format(_size / 1099511627776F));
					_sb.append(" TB");
				} else if(_size >= 1073741824F) {
					_sb.append(_df.format(_size / 1073741824F));
					_sb.append(" GB");
				} else if(_size >= 1048576F) {
					_sb.append(_df.format(_size / 1048576F));
					_sb.append(" MB");
				} else if(_size >= 1024F) {
					_sb.append(_df.format(_size / 1024F));
					_sb.append(" KB");
				} else if(_size == 0) {
					_sb.append("0 B");
				} else {
					_sb.append(_df.format(_size));
					_sb.append(" B");
				}
				break;
			case 'K':
				if(_size >= 1073741824F) {
					_sb.append(_df.format(_size / 1073741824F));
					_sb.append(" TB");
				} else if(_size >= 1048576F) {
					_sb.append(_df.format(_size / 1048576F));
					_sb.append(" GB");
				} else if(_size >= 1024F) {
					_sb.append(_df.format(_size / 1024F));
					_sb.append(" MB");
				} else if(_size == 0) {
					_sb.append("0 B");
				}  else {
					_sb.append(_df.format(_size));
					_sb.append(" KB");
				}
				break;
			case 'M':
				if(_size >= 1048576F) {
					_sb.append(_df.format(_size / 1048576F));
					_sb.append(" TB");
				} else if(_size >= 1024F) {
					_sb.append(_df.format(_size / 1024F));
					_sb.append(" GB");
				} else if(_size == 0) {
					_sb.append("0 B");
				} else {
					_sb.append(_df.format(_size));
					_sb.append(" MB");
				}
				break;
			case 'G':
				if(_size >= 1024) {
					_sb.append(_df.format(_size / 1024F));
					_sb.append(" TB");
				} else if(_size == 0) {
					_sb.append("0 B");
				} else {
					_sb.append(_df.format(_size));
					_sb.append(" GB");
				}
				break;
			case 'T':
				if(_size >= 1024) {
					_sb.append(_df.format(_size / 1024F));
					_sb.append(" PB");
				} else if(_size == 0) {
					_sb.append("0 B");
				} else {
					_sb.append(_df.format(_size));
					_sb.append(" TB");
				}
				break;
		}
		return _sb.toString();
	}
	
	/**
	 * Obtiene la lista de volumenes locales definidos en el sistema 
	 * @return
	 * @throws Exception
	 */
	public static List<String> getLocalDeviceVolumes() throws Exception {
		List<String> localDeviceVolumes = null;
		try {
			localDeviceVolumes = new ArrayList<String>();
		
			File file = new File(WBSAirbackConfiguration.getFileLocalDeviceGroups());
			if (file.exists()) {
				Configuration _c = new Configuration(file);
				if (_c.getPropertyNames() != null && _c.getPropertyNames().size()>0) {
					for (String vg : _c.getPropertyNames()) {
						List<String> volumes = getLogicalVolumeNames(vg);
						if (volumes != null && volumes.size()>0) {
							for (String vol : volumes) {
								if (!localDeviceVolumes.contains(vg+"/"+vol))
									localDeviceVolumes.add(vg+"/"+vol);
							}
						} else {
							if (!localDeviceVolumes.contains(vg))
								localDeviceVolumes.add(vg);
						}
					}
				}
			}
		} catch (Exception ex) {
			throw new Exception("Error on getting local volumes");
		}
		return localDeviceVolumes;
	}
	
	public static Map<String, String> getLogicalVolume(String group, String name) throws Exception {
		Map<String, String> _lv = new HashMap<String,String>();
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		
		if(ZFSConfiguration.fileSystemExists(group, name)) {
			_lv = ZFSConfiguration.getVolume(group, name, false);
			if(_lv != null && isLogicalVolumeMounted(_lv.get("vg"), _lv.get("name"))) {
				_lv.put("mount", "true");
			} else {
				_lv.put("mount", "false");
			}
		} else {
			Map<String, Integer> _partitions = GeneralSystemConfiguration.getDiskLoad();
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/lvs --noheadings --units B --separator : -o +origin,origin_size,stripes 2> /dev/null")));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					try {
						String[] _values = _line.trim().split(":");
						if(!_values[2].startsWith("-") && !_values[2].startsWith("o") && !_values[2].startsWith("m")) {
							if(!_values[10].contains(_values[0] + "_vorigin")) {
								continue;
							}
						}
						if(getLogicalVolumeDevicePath(group, name).equals(getLogicalVolumeDevicePath(_values[1], _values[0]))) {
							if(isMount(_values[1], _values[0]) && _partitions.containsKey(getLogicalVolumeMountPath(_values[1], _values[0]))) {
								String _percent = "0";
								if(isLogicalVolumeMounted(_values[1], _values[0])) {
									_lv.put("mount", "true");
								} else {
									_lv.put("mount", "false");
								}
								Map<String, String> _parameters = FileSystemManager.getFileSystemParameters(_values[1], _values[0]);
								_lv.putAll(_parameters);
								if(_parameters.containsKey("used")) {
									_percent = _parameters.get("used");
								}
								if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")) ||
										FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _lv.get("fstype"))) {
									if(_parameters.containsKey("deduplicated")) {
										_lv.put("deduplicated", _parameters.get("deduplicated"));
									}
									if(_parameters.containsKey("compression")) {
										_lv.put("compression", _parameters.get("compression"));
										if(_parameters.containsKey("compressed")) {
											_lv.put("compressed", _parameters.get("compressed"));
										}
									}
								}
								_lv.put("used", _percent.concat("%"));
								_lv.put("used-raw", _percent);
							} else {
								_lv.put("mount", "false");
								_lv.put("used", "");
							}
							_lv.put("name", _values[0]);
							_lv.put("path", getLogicalVolumeDevicePath(_values[1], _values[0]));
							_lv.put("vg", _values[1]);
							_lv.put("size", getFormatSize(_values[3]));
							if(_values[3] != null && _values[3].endsWith("B")) {
								_lv.put("size-raw", _values[3].substring(0, _values[3].length() - 1));
							} else {
								_lv.put("size-raw", _values[3]);
							}
							_lv.put("stripes", _values[12]);
							_lv.put("fstype", getLogicalVolumeFS(_values[1], _values[0]));
							try {
								_lv.put("type", getLogicalVolumeType(_values[1], _values[0]));
							} catch(Exception _ex) {}
							break;
						}
					} catch(Exception _ex) {
						break;
					}
				}
			} finally {
				_br.close();
			}
			if(_lv.isEmpty()) {
				throw new Exception("logical volume does not exists");
			}
		}
		return _lv;
	}
	
	public static String getLogicalVolumeDevicePath(String group, String name) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/dev/");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		return _sb.toString();
	}
	
	public static Map<String, String> getLogicalVolumeFromPath(String path) {
		if(path.startsWith(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
			path = path.substring(WBSAirbackConfiguration.getDirectoryVolumeMount().length() + 1);
		}
		if(path.startsWith("/")) {
			path = path.substring(1);
		}
		Map<String, String> _volume = new HashMap<String, String>();
		StringTokenizer _st = new StringTokenizer(path, "/");
		if(_st.hasMoreTokens()) {
			_volume.put("vg", _st.nextToken());
		}
		if(_st.hasMoreTokens()) {
			_volume.put("lv", _st.nextToken());
		}
		return _volume;
	}
	
	public static String getLogicalVolumeFS(String group, String name) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			return "unknown";
		}
		for(String[] _value : FileSystemManager.readFstab().values()) {
			if(FileSystemManager.isSupportedFilesystemType(_value[2]) &&
					getLogicalVolumeDevicePath(group, name).equals(_value[0])) {
				return _value[2];
			}
		}
		
		if (ZFSConfiguration.fileSystemExists(group, name))
			return "zfs";
		else {
			return "xfs";
		}
	}
	
	public static String getLogicalVolumeMountPath(String group, String name) throws Exception {
		if(isMount(group, name)) {
			return getMountPath(group, name);
		}
		throw new Exception("logical volume does not exists or is not mountable");
	}
	
	public static List<String> getLogicalVolumeNames() throws Exception {
		List<String> _volumes = new ArrayList<String>();
		BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/lvs --noheadings --segments --units B --separator : -o +origin 2> /dev/null")));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					String[] _values = _line.trim().split(":");
					if(!_values[2].startsWith("-") && !_values[2].startsWith("o") && !_values[2].startsWith("m")) {
						if(!_values[6].contains(_values[0] + "_vorigin")) {
							continue;
						}
					}
					_volumes.add(_values[0]);
				} catch(Exception _ex) {}
			}
		} finally {
			_br.close();
		}
		for(String _pool : ZFSConfiguration.getPoolNames()) {
			_volumes.addAll(ZFSConfiguration.getPoolFileSystemNames(_pool));
		}
		return _volumes;
	}
	
	public static List<String> getLogicalVolumeNames(String group) throws Exception {
		if(ZFSConfiguration.poolExists(group)) {
			return ZFSConfiguration.getPoolFileSystemNames(group);
		} else if(volumeGroupExists(group)) {
			List<String> _volumes = new ArrayList<String>();
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/lvs --noheadings --segments --units B --separator : -o +origin 2> /dev/null")));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					try {
						String[] _values = _line.trim().split(":");
						if(!_values[2].startsWith("-") && !_values[2].startsWith("o") && !_values[2].startsWith("m")) {
							if(!_values[6].contains(_values[0] + "_vorigin")) {
								continue;
							}
						}
						if(group.equals(_values[1])) {
							_volumes.add(_values[0]);
						}
					} catch(Exception _ex) {}
				}
			} finally {
				_br.close();
			}
			return _volumes;
		} else {
			throw new Exception("aggregate does not exists");
		}
	}
	
	public static List<Map<String, String>> getLogicalVolumes(String group) throws Exception {
		List<Map<String, String>> allvolumes = getLogicalVolumes();
		List<Map<String, String>> volumes = new ArrayList<Map<String, String>>();
		if (allvolumes != null && !allvolumes.isEmpty()) {
			for (Map<String, String> vol : allvolumes) {
				if (vol.get("vg") != null && vol.get("vg").equals(group))
					volumes.add(vol);
			}
		}
		return volumes;
		
	}
	public static List<Map<String, String>> getLogicalVolumes() throws Exception {
		Map<String, Map<String, String>> _volumes = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/lvs --noheadings --units B --separator : -o +origin,origin_size,stripes 2> /dev/null")));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					String[] _values = _line.trim().split(":");
					if(!_values[2].startsWith("-") && !_values[2].startsWith("o") && !_values[2].startsWith("m")) {
						if(!_values[10].contains(_values[0] + "_vorigin")) {
							continue;
						}
					}
					Map<String, String> _lv = new HashMap<String, String>();
					_lv.put("name", _values[0]);
					_lv.put("path", getLogicalVolumeDevicePath(_values[1], _values[0]));
					_lv.put("vg", _values[1]);
					_lv.put("size", getFormatSize(_values[3]));
					if(_values[3] != null && _values[3].endsWith("B")) {
						_lv.put("size-raw", _values[3].substring(0, _values[3].length() - 1));
					} else {
						_lv.put("size-raw", _values[3]);
					}
					_lv.put("stripes", _values[12]);
					_lv.put("fstype", getLogicalVolumeFS(_values[1], _values[0]));
					if(isLogicalVolumeMounted(_values[1], _values[0])) {
						String _percent = "0";
						Map<String, String> _parameters = FileSystemManager.getFileSystemParameters(_values[1], _values[0]);
						_lv.put("mount", "true");
						if(_parameters.containsKey("used")) {
							_percent = _parameters.get("used");
						}
						if(_parameters.containsKey("deduplicated")) {
							_lv.put("deduplicated", _parameters.get("deduplicated"));
						}
						if(_parameters.containsKey("compression")) {
							_lv.put("compression", _parameters.get("compression"));
							if(_parameters.containsKey("compressed")) {
								_lv.put("compressed", _parameters.get("compressed"));
							}
						}
						_lv.put("used", _percent.concat("%"));
						_lv.put("used-raw", _percent);
					} else {
						_lv.put("mount", "false");
						_lv.put("used", "");
					}
					try {
						_lv.put("type", getLogicalVolumeType(_values[1], _values[0]));
					} catch(Exception _ex) {}
					_volumes.put(_lv.get("vg")+_lv.get("name"), _lv);
				} catch(Exception _ex) {
					System.out.println("VolumeManager::getLogicalVolumes:error - " + _ex.toString());
				}
			}
		} finally {
			_br.close();
		}
		for(Map<String, String> _lv : ZFSConfiguration.getVolumes(true)) {
			if(isLogicalVolumeMounted(_lv.get("vg"), _lv.get("name"))) {
				_lv.put("mount", "true");
			} else {
				_lv.put("mount", "false");
			}
			_volumes.put(_lv.get("vg")+_lv.get("name"), _lv);
		}
		return new ArrayList<Map<String,String>>(_volumes.values());
	}
	
	public static double getLogicalVolumeSize(String group, String name) throws Exception {
		String _path = null;
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid volume name");
		}
		
		_path = getLogicalVolumeMountPath(group, name);
		if(!isSystemMounted(_path)) {
			return 0;
		}
		
		String _output;
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" /bin/df -lP ");
			_sb.append(_path);
			_sb.append(" | /bin/grep ");
			_sb.append(_path);
			_sb.append(" | /usr/bin/awk '{print $2}'");
			_output = Command.systemCommand(_sb.toString());
			if(_output == null || _output.isEmpty()) {
				throw new Exception(name);
			} else {
				try {
					return Double.parseDouble(_output);
				} catch(NumberFormatException _ex) {
					throw new Exception("invalid volume size format found");
				}
			}
		} catch(Exception _ex) {
			throw new Exception("fail to get volume size - " + _ex.getMessage());
	    }
	}
	
	public static Map<String, String> getLogicalVolumeSnapshot(String group, String name, String snapshot) throws Exception {
		return FileSystemManager.getFileSystemSnapshot(group, name, snapshot);
	}
	
	public static List<String> getLogicalVolumeSnapshotIndexes(int type, String group, String name) throws Exception {
		File _f = null;
		List<String> _snapshots = new ArrayList<String>();
		if(!isMount(group, name)) {
			return _snapshots;
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append(getLogicalVolumeMountPath(group, name));
		_sb.append("/.snapshots/");
		switch(type) {
			case LV_SNAPSHOT_HOURLY: {
					_sb.append(".hourly");
					_f = new File(_sb.toString());
				}
				break;
			case LV_SNAPSHOT_DAILY: {
					_sb.append(".daily");
					_f = new File(_sb.toString());
				}
				break;
		}
		if(_f != null && _f.exists()) {
			BufferedReader _br = new BufferedReader(new FileReader(_f));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					_snapshots.add(_line);
				}
			} finally {
				_br.close();
			}
		}
		return _snapshots;
	}
	
	public static String getLogicalVolumeSnapshotMountPath(String group, String name, String snapshot) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(getMountPath(group, name));
		_sb.append("/.snapshots/");
		if(!snapshot.startsWith("@")) {
			_sb.append("@");
		}
		_sb.append(snapshot);
		return _sb.toString();
	}
	
	public static List<Map<String, String>> getLogicalVolumeSnapshots(String group, String name) throws Exception {
		if(isMount(group, name)) {
			return FileSystemManager.getFileSystemSnapshots(group, name);
		} else {
			/**
			 * TODO
			 */
			return new ArrayList<Map<String,String>>();
		}
	}
	
	public static List<Map<String, String>> getLogicalVolumeSnapshots(String group, String name, int type) throws Exception {
		if(type != LV_SNAPSHOT_MANUAL && type != LV_SNAPSHOT_HOURLY && type != LV_SNAPSHOT_DAILY) {
			throw new Exception("invalid logical volume snapshot type");
		}
		
		List<Map<String, String>> _snapshots = new ArrayList<Map<String,String>>();
		List<String> _hourly_snapshots = getLogicalVolumeSnapshotIndexes(LV_SNAPSHOT_HOURLY, group, name);
		List<String> _daily_snapshots = getLogicalVolumeSnapshotIndexes(LV_SNAPSHOT_DAILY, group, name);
		for(Map<String, String> _snapshot : getLogicalVolumeSnapshots(group, name)) {
			switch(type) {
				case LV_SNAPSHOT_MANUAL: {
						if(!_hourly_snapshots.contains(_snapshot.get("name")) &&
								!_daily_snapshots.contains(_snapshot.get("name"))) {
							_snapshots.add(_snapshot);
						}
					}
					break;
				case LV_SNAPSHOT_HOURLY: {
						if(_hourly_snapshots.contains(_snapshot.get("name"))) {
							_snapshots.add(_snapshot);
						}
					}
					break;
				case LV_SNAPSHOT_DAILY: {
						if(_daily_snapshots.contains(_snapshot.get("name"))) {
							_snapshots.add(_snapshot);
						}
					}
					break;
			}
		}
		return _snapshots;
	}
	
	public static Map<Date, Map<String, String>> getLogicalVolumeSnapshotsOrderedByDate(String group, String name, int type) throws Exception {
		TreeMap<Date, Map<String, String>> _ordered_snapshots = new TreeMap<Date, Map<String,String>>();
		List<Map<String, String>> _snapshots = VolumeManager.getLogicalVolumeSnapshots(group, name, type);
		if (_snapshots != null && !_snapshots.isEmpty()) {
			for(Map<String, String> _snapshot : _snapshots) {
				SimpleDateFormat _sdtf = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
				String _date = _snapshot.get("name"); 
				if(_date.contains("-")) {
					_date = _date.substring(_date.indexOf("-") + 1);
				}
				_ordered_snapshots.put(_sdtf.parse(_date), _snapshot);
			}
		}
		return _ordered_snapshots;
	}
	
	public static int getLogicalVolumeSnapshotType(String group, String name, String snapshot) throws Exception {
		if(getLogicalVolumeSnapshotIndexes(LV_SNAPSHOT_HOURLY, group, name).contains(snapshot)) {
			return LV_SNAPSHOT_HOURLY;
		}
		if(getLogicalVolumeSnapshotIndexes(LV_SNAPSHOT_DAILY, group, name).contains(snapshot)) {
			return LV_SNAPSHOT_DAILY;
		}
		return LV_SNAPSHOT_MANUAL;
	}
	
	public static String getLogicalVolumeType(String group, String name) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		
		if(ZFSConfiguration.fileSystemExists(group, name)) {
			return "DEDUPE";
		} else {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/lvs --noheading --segments --units B --separator : " + getLogicalVolumeDevicePath(group, name) + " 2> /dev/null")));
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					String[] _values = _line.trim().split(":");
					if(_values[2].startsWith("m")) {
						return "MIRROR";
					} else {
						if("striped".equals(_values[4])) {
							return "STRIPED";
						} else {
							return "LINEAR";
						}
					}
				} catch(Exception _ex) {}
			}
		}
		return "";
	}
	
	public static List<String> getMountableLogicalVolumeNames() throws Exception {
		List<String> _mounts = new ArrayList<String>();
		for(Map<String, String> _lv : getLogicalVolumes()) {
			if(isMount(_lv.get("vg"), _lv.get("name"))) {
				StringBuilder _sb = new StringBuilder();
				_sb.append(_lv.get("vg"));
				_sb.append("/");
				_sb.append(_lv.get("name"));
				_mounts.add(_sb.toString());
			}
		}
		return _mounts;
	}
	
	public static List<Map<String, String>> getMountableLogicalVolumes() throws Exception {
		List<Map<String, String>> _mounts = new ArrayList<Map<String,String>>();
		for(Map<String, String> _lv : getLogicalVolumes()) {
			if(isMount(_lv.get("vg"), _lv.get("name"))) {
				_mounts.add(_lv);
			}
		}
		return _mounts;
	}
	
	public static String getMountPath(String group, String name) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(WBSAirbackConfiguration.getDirectoryVolumeMount());
		_sb.append("/");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		return _sb.toString();
	}
	
	public static List<Map<String, String>> getNewLogicalVolumeSnapshots(String group, String name, Calendar date) throws Exception {
		Calendar _time = Calendar.getInstance();
		TreeMap<Date, Map<String, String>> _new_snapshots = new TreeMap<Date, Map<String,String>>();
		List<Map<String, String>> _snapshots = VolumeManager.getLogicalVolumeSnapshots(group, name);
		for(Map<String, String> _snapshot : _snapshots) {
			SimpleDateFormat _sdtf = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
			String _date = _snapshot.get("name"); 
			if(_date.contains("-")) {
				_date = _date.substring(_date.indexOf("-") + 1);
			}
			_time.setTime(_sdtf.parse(_date));
			if(date == null ||_time.after(date)) {
				_new_snapshots.put(_time.getTime(), _snapshot);
			}
		}
		return new ArrayList<Map<String, String>>(_new_snapshots.values());
	}
	
	public static List<Map<String, String>> getNoMountableLogicalVolumes() throws Exception {
		List<Map<String, String>> _volumes = getLogicalVolumes();
		List<Map<String, String>> _volumesNoMount = new ArrayList<Map<String,String>>();
		try {
			for (Map<String, String> _vol : _volumes) {
				if(!isMount(_vol.get("vg"), _vol.get("name"))) {
					_volumesNoMount.add(_vol);
				}
			}
		} catch (Exception ex){}
		return _volumesNoMount;
	}
	
	private static List<String[]> getPartitions() throws Exception {
		List<String[]> disks = new ArrayList<String[]>();
		File _f = new File("/proc/partitions");
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.contains("major") || _line.trim().isEmpty()) {
					continue;
				}
				List<String> _values = new ArrayList<String>();
				StringTokenizer _st = new StringTokenizer(_line);
				while(_st.hasMoreTokens()) {
					_values.add(_st.nextToken());
				}
				disks.add(_values.toArray(new String[] {}));
			}
		} catch (Exception ex) {
			logger.error("Error leyendo particones de /proc/partitions. Ex:{}", ex.getMessage());
		} finally {
			_br.close();
		}
		return disks;
	}
	
	public static Map<String, String> getPhysicalDisk(String device) throws Exception {
		Map<String, String> _disk = new HashMap<String, String>();
		if(device == null || device.isEmpty()) {
			return _disk;
		}
		List<String> system_partitions = getSystemPartitions();
		
		if(!device.startsWith("/dev/")) {
			device = "/dev/".concat(device);
		}
		_disk.put("device", device);
		
		if (CloudManager.isCloudDevice(device))
			return CloudManager.getCloudDiskByDevice(device);
		
		if (!system_partitions.contains(device)) {
			device = VolumeManager.findDevSdFromDeviceId(device);
		}
		
		
		for(String[] _value : getPartitions()) {
			boolean _match = false;
			if(_value[3].matches("[a-z]+[0-9]+") ||
					_value[3].startsWith("dm-")) {
				continue;
			}
			if(!device.equalsIgnoreCase("/dev/" + _value[3])) {
				continue;
			}
			for(String partition : system_partitions) {
				if(partition.contains("/dev/" + _value[3])) {
					_match = true;
					break;
				}
			}
			if(_match) {
				continue;
			}
			_disk.put("size", getBlockSize(_value[2]));
			_disk.put("size-raw", _value[2]);
			if(_value[3].contains("hd")) {
				File _f = new File("/proc/ide/"+ _value[3] + "/model");
				if(_f.exists()) {
					BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
					_disk.put("model", _br.readLine());
					_br.close();
					if(_disk.get("model") == null) {
						_disk.put("model", "Generic");
					}
					_disk.put("vendor", "IDE");
				} else {
					_disk.put("vendor", "Unknown");
					_disk.put("model", "Generic");
				}
			} else if(_value[3].contains("sd")) {
				Map<String, String> _device = SCSIManager.getDevice(_disk.get("device"));
				if(_device.get("vendor") != null) {
					_disk.put("vendor", _device.get("vendor"));
				} else {
					_disk.put("vendor", "SCSI");
				}
				_disk.put("model", _device.get("model"));
			} else {
				_disk.put("vendor", "Unknown");
				_disk.put("model", "Generic");
			} 
			break;
		}
		return _disk;
	}
	
	public static List<Map<String, String>> getPhysicalDisks() throws Exception {
		List<Map<String, String>> disks = new ArrayList<Map<String, String>>();
		List<String> system_partitions = getSystemPartitions();
		List<String> sds = new ArrayList<String>();
		for(String[] _value : getPartitions()) {
			boolean _match = false;
			if(_value[3].matches("[a-z]+[0-9]+")) {
				continue;
			}
			if(_value[3].startsWith("dm-")) {
				continue;
			}
			for(String partition : system_partitions) {
				if(partition.contains("/dev/" + _value[3])) {
					_match = true;
					break;
				}
			}
			if(_match) {
				continue;
			}
			Map<String, String> _disk = new HashMap<String, String>();
			_disk.put("device", "/dev/" + _value[3]);
			_disk.put("size", getBlockSize(_value[2]));
			_disk.put("size-raw", _value[2]);
			if(_value[3].contains("hd")) {
				File _f = new File("/proc/ide/"+ _value[3] + "/model");
				if(_f.exists()) {
					BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
					_disk.put("model", _br.readLine());
					_br.close();
					if(_disk.get("model") == null) {
						_disk.put("model", "Generic");
					}
					_disk.put("vendor", "IDE");
				} else {
					_disk.put("vendor", "Unknown");
					_disk.put("model", "Generic");
				}
			} else if(_value[3].contains("sd")) {
				Map<String, String> _device = SCSIManager.getDevice(_disk.get("device"));
				if(_device.get("vendor") != null) {
					_disk.put("vendor", _device.get("vendor"));
				} else {
					_disk.put("vendor", "SCSI");
				}
				_disk.put("model", _device.get("model"));
			} else {
				_disk.put("vendor", "Unknown");
				_disk.put("model", "Generic");
			} 
			_disk.put("deviceId", findDiskPathById(_disk.get("device")));
			String sd = findDevSdFromDeviceId(_disk.get("deviceId"));
			if (!sds.contains(sd)) {
				sds.add(sd);
				disks.add(_disk);
			}
		}
		return disks;
	}
	
	public static List<String> getPhysicalVolumeNames() throws Exception {
		List<String> _devices = new ArrayList<String>();
		BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/pvdisplay -c 2> /dev/null")));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					if (!_line.contains("new physical volume") && !_line.contains("orphans_lvm2")) {
						String[] _values = _line.trim().split(":");
						_devices.add(_values[0]);
					}
				} catch(Exception _ex) {
					logger.error("Error obteniendo nombres de volumen (pvdisplay -c). Linea:{} Ex: {}",_line, _ex.getMessage());
				}
			}
		} finally {
			_br.close();
		}
		for(String _pool : ZFSConfiguration.getPoolNames()) {
			for(Map<String, String> _device : ZFSConfiguration.getPoolDevices(_pool)) {
				_devices.add(_device.get("device"));
			}
		}
		return _devices;
	}
	
	public static List<String> getPhysicalVolumeNames(String group) throws Exception {
		List<String> _devices = new ArrayList<String>();
		if(group == null || group.isEmpty()) {
			return _devices;
		}
		if(volumeGroupExists(group)) {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/pvdisplay --noheadings --units B -C --separator : 2> /dev/null")));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					try {
						if (!_line.contains("new physical volume") && !_line.contains("orphans_lvm2")) {
							String[] _values = _line.trim().split(":");
							if(group.equals(_values[1])) {
								_devices.add(_values[0]);
							}
						}
					} catch(Exception _ex) {}
				}
			} finally {
				_br.close();
			}
		} else if(ZFSConfiguration.poolExists(group)) {
			for(Map<String, String> _device : ZFSConfiguration.getPoolDevices(group)) {
				_devices.add(_device.get("device"));
			}
		} else {
			throw new Exception("aggregate does not exists");
		}
		return _devices;
	}
	
	public static List<Map<String, String>> getPhysicalVolumes() throws Exception {
		List<Map<String, String>> _devices = new ArrayList<Map<String, String>>();
		BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/pvdisplay --noheadings --units B -C --separator : 2> /dev/null")));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					String[] _values = _line.trim().split(":");
					Map<String, String> _pv = new HashMap<String, String>();
					_pv.put("device", _values[0]);
					_pv.put("vg", _values[1]);
					_pv.put("size", getFormatSize(_values[4]));
					_pv.put("size-raw", _values[4]);
					_pv.put("free", getFormatSize(_values[5]));
					_pv.put("free-raw",_values[5]);
					_devices.add(_pv);
				} catch(Exception _ex) {}
			}
		} finally {
			_br.close();
		}
		for(String _pool : ZFSConfiguration.getPoolNames()) {
			for(Map<String, String> _device : ZFSConfiguration.getPoolDevices(_pool)) {
				_device.put("vg", _pool);
				_device.put("size", "");
				_device.put("free", "");
				_devices.add(_device);
			}
		}
		return _devices;
	}
	
	public static List<Map<String, String>> getPhysicalVolumes(String group) throws Exception {
		List<Map<String, String>> _devices = new ArrayList<Map<String, String>>();
		if(group == null || group.isEmpty()) {
			return _devices;
		}
		if(volumeGroupExists(group)) {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/pvdisplay --noheadings --units B -C --separator : 2> /dev/null")));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					try {
						String[] _values = _line.trim().split(":");
						if(group.equals(_values[1])) {
							Map<String, String> _device = new HashMap<String, String>();
							_device.put("device", _values[0]);
							_device.put("vg", _values[1]);
							_device.put("size", getFormatSize(_values[4]));
							_device.put("size-raw", _values[4]);
							_device.put("free", getFormatSize(_values[5]));
							_device.put("free-raw",_values[5]);
							_devices.add(_device);
						}
					} catch(Exception _ex) {
						System.out.println("VolumeManager::getPhysicalVolumes:error - " + _ex.getMessage());
					}
				}
			} finally {
				_br.close();
			}
		} else if(ZFSConfiguration.poolExists(group)) {
			for(Map<String, String> _device : ZFSConfiguration.getPoolDevices(group)) {
				_device.put("vg", group);
				if(_device.get("device") != null) {
					_device.putAll(getPhysicalDisk(_device.get("device")));
				}
				if(_device.get("size") == null) {
					_device.put("size", "");
				}
				if(_device.get("free") == null) {
					_device.put("free", "");
				}
				_devices.add(_device);
			}
		} else {
			throw new Exception("aggregate does not exists");
		}
		return _devices;
	}
	
	public static Map<String, String> getPlannedSnapshot(int type, String group, String volume) throws Exception {
		File _f = new File(WBSAirbackConfiguration.getFilePlannedSnapshots());
		if(!_f.exists()) {
			return new HashMap<String,String>();
		}
		XMLDB _db = new XMLDB(_f);
		switch(type) {
			case LV_SNAPSHOT_HOURLY: {
					for(XMLObject _o : _db.getObjectsByType("hourly")) {
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						if(group.equals(_snapshot.get("group")) && volume.equals(_snapshot.get("volume"))) {
							return _snapshot;
						}
					}
				}
				break;
			case LV_SNAPSHOT_DAILY: {
					for(XMLObject _o : _db.getObjectsByType("daily")) {
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						if(group.equals(_snapshot.get("group")) && volume.equals(_snapshot.get("volume"))) {
							return _snapshot;
						}
					}
				}
				break;
		}
		return new HashMap<String,String>();
	}
	
	public static List<Map<String, String>> getPlannedSnapshots(int type) throws Exception {
		Map<String, Map<String, String>> _planned_snapshot = new HashMap<String, Map<String,String>>();
		File _f = new File(WBSAirbackConfiguration.getFilePlannedSnapshots());
		if(!_f.exists()) {
			return new ArrayList<Map<String, String>>();
		}
		XMLDB _db = new XMLDB(_f);
		switch(type) {
			case LV_SNAPSHOT_HOURLY: {
					for(XMLObject _o : _db.getObjectsByType("hourly")) {
						StringBuilder _sb = new StringBuilder();
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						_sb.append(_snapshot.get("group"));
						_sb.append("/");
						_sb.append(_snapshot.get("volume"));
						if(!_planned_snapshot.containsKey(_sb.toString())) {
							_planned_snapshot.put(_sb.toString(), _snapshot);
						}
					}
				}
				break;
			case LV_SNAPSHOT_DAILY: {
					for(XMLObject _o : _db.getObjectsByType("daily")) {
						StringBuilder _sb = new StringBuilder();
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						_sb.append(_snapshot.get("group"));
						_sb.append("/");
						_sb.append(_snapshot.get("volume"));
						if(!_planned_snapshot.containsKey(_sb.toString())) {
							_planned_snapshot.put(_sb.toString(), _snapshot);
						}
					}
				}
				break;
		}
		return new ArrayList<Map<String, String>>(_planned_snapshot.values());
	}
	
	
	public static double getSize(int size, int units) {
		switch(units) {
			case SIZE_LV_M: {
					return size * 1048576D;
				}
			case SIZE_LV_G: {
					return size * 1073741824D;
				}
			case SIZE_LV_T: {
					return size * 1099511627776D;
				}
			default: {
				return size;
			}
		}
	}
	
	
	
	public static String getSizeOnUnit(String value, char unit) {
		double _size = 0D;
		try {
			_size = Double.parseDouble(value);
		} catch(Exception ex) {}
		
		StringBuilder _sb = new StringBuilder();
		switch(unit) {
			case 'B':
				_sb.append(_size);
				break;
			case 'K':
				_sb.append(_size / 1024F);
				break;
			case 'M':
				_sb.append(_size / (1024F*1024F));
				break;
			case 'G':
				_sb.append(_size / (1024F*1024F*1024F));
				break;
			case 'T':
				_sb.append(_size / (1024F*1024F*1024F*1024F));
				break;
		}
		return _sb.toString();
	}
	
	private static List<String> getSystemPartitions() throws Exception {
		List<String> partitions = new ArrayList<String>();
		File _f = new File("/proc/mounts");
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.contains(" / ext3 ") || _line.contains(" /boot ext3 ") || _line.contains(" /rdata ext3 ")) {
					partitions.add(_line.substring(0, _line.indexOf(" ")));
				}
			}
		} catch (Exception ex) {
			logger.error("Error interpretando particiones en /proc/mounts. Ex: {}", ex.getMessage());
		} finally {
			_br.close();
		}
		return partitions;
	}
	
	public static String getThreadName(String group, String name) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append(_preffix);
		_sb.append(group);
		_sb.append("_");
		_sb.append(name);
		return _sb.toString();
	}
	
	/**
	 * Devuelve los discos sin asignar de tipo lun o multipath
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> getUnassignedLuns() throws Exception {
		List<Map<String, String>> _disks = getUnassignedPhysicalDisks();
		List<Map<String, String>> _luns = new ArrayList<Map<String, String>>();
		for (Map<String, String> _disk : _disks) {
			if (_disk.get("vendor") != null && (_disk.get("vendor").equals("MULTIPATH") || _disk.get("vendor").equals("SCSI")) && _disk.get("model") != null && _disk.get("model").contains("SCS") ) {
				_luns.add(_disk);
			}
		}
		return _disks;
	}
	
	public static List<Map<String, String>> getUnassignedPhysicalDisks() throws Exception {
		List<String> _assigned_disks = getPhysicalVolumeNames();
		List<String> _assigned_disks_ids = new ArrayList<String>();
		
		for(String _pool : ZFSConfiguration.getPoolNames())  {
			for(Map<String, String> _device : ZFSConfiguration.getPoolDevices(_pool)) {
				if(_device.get("device") != null && !_assigned_disks.contains(_device.get("device"))) {
					_assigned_disks.add(_device.get("device"));
				}
			}
			for(Map<String, String> _device : ZFSConfiguration.getVolumeGroupCacheDisks(_pool)) {
				if(_device.get("device") != null && !_assigned_disks.contains(_device.get("device"))) {
					_assigned_disks.add(_device.get("device"));
				}
			}
			for(Map<String, String> _device : ZFSConfiguration.getVolumeGroupLogDisks(_pool)) {
				if(_device.get("device") != null && !_assigned_disks.contains(_device.get("device"))) {
					_assigned_disks.add(_device.get("device"));
				}
			}
			
		}
		
		for (String ad : _assigned_disks) {
			String id = findDiskId(ad);
			if (id != null) {
				_assigned_disks_ids.add(id);
			}
		}
		
		List<Map<String, String>> _disks = getPhysicalDisks();
		List<Map<String, String>> _disks_including_mp = new ArrayList<Map<String, String>>();
		_disks_including_mp.addAll(_disks);
		
		try {
			// Si multipath esta activado, recuperamos todos los dispositivos de esta naturaleza
			if (MultiPathManager.isMultipathEnabled()) {
				List<Map<String, Object>> _multiPathDisks = MultiPathManager.getMultiPathVolumes();
				logger.info("Obtenidos correctamente los discos multipath, en total: {}", _multiPathDisks.size());
				boolean isAssigned = false;
				for (Map<String, Object> _device_mp : _multiPathDisks) {					// Si alguno de los dispositivos esta asignado
					
					@SuppressWarnings("unchecked")
					List<String> _devices = (List<String>) _device_mp.get("disks");			// Añadimos el resto también como 'asignados' porque son 'caminos'
					for (String _dev : _devices) {								   			 // Y no deben aparecer como dispositivos libres
						if (_assigned_disks != null && _assigned_disks.contains(_dev))
							isAssigned=true;
					}
					if (isAssigned) {
						logger.info("El disco multipath {} tiene alguno de sus 'caminos' asigando, por lo que añadimos al conjunto de asignados todos sus demás 'caminos'",_device_mp.get("dev"));
						for (String _dev : _devices) {
							if (!_assigned_disks.contains(_dev))
								_assigned_disks.add(_dev);
						}
					} else {
						logger.info("El disco multipath {} no tiene ninguno de sus 'caminos' asigando, por lo que añadimos el disco multipath a la lista de discos",_device_mp.get("dev"));
						Map<String, String> _unic_mp_disk = new HashMap<String, String>();							// Si el multipath 'completo' esta sin asignar, lo marcamos como tal
						_unic_mp_disk.put("model", (String)_device_mp.get("model"));
						_unic_mp_disk.put("vendor", "MULTIPATH");
						_unic_mp_disk.put("device", (String)_device_mp.get("dev"));
						_unic_mp_disk.put("deviceId", (String)_device_mp.get("dev"));
						_unic_mp_disk.put("size", (String)_device_mp.get("size"));
						_disks_including_mp.add(_unic_mp_disk);
					}
						
					// Eliminamos todos los discos que formen parte de los 'caminos' de mp 
					for(Map<String, String> disk : _disks) {
						if (_devices.contains(disk.get("device"))) {
							_disks_including_mp.remove(disk);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error al intentar rehacer la lista de discos incluyendo los discos multipath .. Mostramos lista sin tener en cuenta el multipath. Ex: {}",ex.getMessage());
			_disks_including_mp = new ArrayList<Map<String, String>>();
			_disks_including_mp.addAll(_disks);
		}
		
		// Obtenemos el disco configurado para el cluster para, si lo hay, quitarlo de la lista
		String diskCluster = null;
		if (HAConfiguration.inCluster()) {
			diskCluster = HAConfiguration.getConfiguredClusterDisk();
			if (diskCluster != null)
				logger.debug("Estamos en un nodo cluster de tipo lun compartida en {}. Quitaremos esta lun del listado de discos libres", diskCluster);
		}
		
		List<Map<String, String>> _unassigned_disks = new ArrayList<Map<String,String>>();
		for(Map<String, String> _disk : _disks_including_mp) {
			logger.debug("Comprobamos si disco: {} size: {} esta asignado o sin asignar ...", _disk.get("deviceId"), _disk.get("size"));
			if(!_assigned_disks.contains(_disk.get("device")) && !_assigned_disks_ids.contains(findDiskId(_disk.get("device")))) {
				if (_disk.get("deviceId") == null || !_assigned_disks.contains(_disk.get("deviceId"))) {
					if (diskCluster == null || !diskCluster.equals(_disk.get("device")) ) {
						if ( diskCluster == null || (diskCluster != null && _disk.get("deviceId") == null) || (diskCluster != null && _disk.get("deviceId") != null && !diskCluster.equals(_disk.get("deviceId"))) ) {
							_unassigned_disks.add(_disk);
							logger.debug("Disco {} NO ASIGNADO", _disk.get("device"));
						} else {
							logger.debug("Disco {} ASIGNADO (4)", _disk.get("device"));
						}
					} else {
						logger.debug("Disco {} ASIGNADO (3)", _disk.get("device"));
					}
				} else {
					logger.debug("Disco {} ASIGNADO (2)", _disk.get("device"));
				}
			} else {
				logger.debug("Disco {} ASIGNADO (1)", _disk.get("device"));
			}
		}
		
		return _unassigned_disks;
	}
	
	public static Map<String, String> getVolumeGroup(String group) throws Exception {
		Map<String, String> _volume_group = new HashMap<String,String>();
		if(volumeGroupExists(group)) {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/vgdisplay --noheadings --units B -C --separator :  2> /dev/null")));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					try {
						String[] _values = _line.trim().split(":");
						if(group.equals(_values[0])) {
							_volume_group.put("name", _values[0]);
							_volume_group.put("type", "lvm");
							_volume_group.put("pv", _values[1]);
							_volume_group.put("lv", _values[2]);
							_volume_group.put("size", getFormatSize(_values[5]));
							if(_values[5] != null && _values[5].endsWith("B")) {
								_volume_group.put("size-raw", _values[5].substring(0, _values[5].length() - 1));
							} else {
								_volume_group.put("size-raw", _values[5]);
							}
							_volume_group.put("free", getFormatSize(_values[6]));
							if(_values[6] != null && _values[6].endsWith("B")) {
								_volume_group.put("free-raw", _values[6].substring(0, _values[6].length() - 1));
							} else {
								_volume_group.put("free-raw", _values[6]);
							}
							break;
						}
					} catch(Exception _ex) {
						System.out.println("VolumeManager::getVolumeGroup:error - " + _ex.getMessage());
					}
				}
			} finally {
				_br.close();
			}
		} else if(ZFSConfiguration.poolExists(group)) {
			_volume_group.putAll(ZFSConfiguration.getPool(group));
			_volume_group.put("type", "zfs");
			_volume_group.put("pv", String.valueOf(ZFSConfiguration.getPoolDevices(group).size()));
			//_volume_group.put("lv", String.valueOf(ZFSConfiguration.getPoolFileSystemNames(group).size()));
		} else {
			throw new Exception("volume group does not exists");
		}
		return _volume_group;
	}
	
	public static List<String> getVolumeGroupNames() throws Exception {
		List<String> _volume_groups = new ArrayList<String>();
		BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/vgdisplay -c 2> /dev/null")));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					String[] _values = _line.trim().split(":");
					_volume_groups.add(_values[0]);
				} catch(Exception _ex) {}
			}
		} finally {
			_br.close();
		}
		for(Map<String, String> _pool : ZFSConfiguration.getPools()) {
			_volume_groups.add(_pool.get("name"));
		}
		return _volume_groups;
	}
	
	public static List<Map<String, String>> getVolumeGroups() throws Exception {
		List<Map<String, String>> _volume_groups = new ArrayList<Map<String,String>>();
		BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/vgdisplay --noheadings --units B -C --separator :  2> /dev/null")));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					String[] _values = _line.trim().split(":");
					Map<String, String> _vg = new HashMap<String, String>();
					_vg.put("name", _values[0]);
					_vg.put("type", "lvm");
					_vg.put("pv", _values[1]);
					_vg.put("lv", _values[2]);
					_vg.put("size", getFormatSize(_values[5]));
					if(_values[5] != null && _values[5].endsWith("B")) {
						_vg.put("size-raw", _values[5].substring(0, _values[5].length() - 1));
					} else {
						_vg.put("size-raw", _values[5]);
					}
					_vg.put("free", getFormatSize(_values[6]));
					if(_values[6] != null && _values[6].endsWith("B")) {
						_vg.put("free-raw", _values[6].substring(0, _values[6].length() - 1));
					} else {
						_vg.put("free-raw", _values[6]);
					}
					_volume_groups.add(_vg);
				} catch(Exception _ex) {
					System.out.println("VolumeManager::getVolumeGroups:error - " + _ex.getMessage());
					}
			}
		} finally {
			_br.close();
		}
		for(Map<String, String> _pool : ZFSConfiguration.getPools()) {
			_pool.put("type", "zfs");
			_pool.put("pv", String.valueOf(ZFSConfiguration.getPoolDevices(_pool.get("name")).size()));
			_volume_groups.add(_pool);
		}
		return _volume_groups;
	}
	
	private static void indexLogicalVolumeSnapshot(int type, String group, String name, String snapshot) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append(getLogicalVolumeMountPath(group, name));
		_sb.append("/.snapshots/");
		File _f = new File(_sb.toString());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		_f = null;
		switch(type) {
			case LV_SNAPSHOT_HOURLY: {
					_sb.append(".hourly");
					_f = new File(_sb.toString());
				}
				break;
			case LV_SNAPSHOT_DAILY: {
					_sb.append(".daily");
					_f = new File(_sb.toString());
				}
				break;
		}
		if(_f != null) {
			List<String> _snapshots = getLogicalVolumeSnapshotIndexes(type, group, name);
			if(!_snapshots.contains(snapshot)) {
				_snapshots.add(snapshot);
				_sb = new StringBuilder();
				for(String _snapshot : _snapshots) {
					_sb.append(_snapshot);
					_sb.append("\n");
				}
				FileSystem.writeFile(_f, _sb.toString());
			}
		}
	}
	
	public static boolean isHypervisorVolume(String vg, String lv) {
		try {
			HypervisorManager _hm = HypervisorManager.getInstance(HypervisorManager.GENERIC, null, null, null);
			return _hm.isVolumeOnHypervisorJob(vg, lv);
		} catch (Exception ex) {
			logger.error("An error occured while checking if {}/{} is on hypervisor job. Ex: {}", new Object[]{vg, lv, ex.getMessage()});
			return false;
		}
	}
	
	/**
	 * Comprueba si un volumeGroup es local (para temas de HA)
	 * @param nameGroup
	 * @return
	 * @throws Exception
	 */
	public static boolean isLocalDeviceGroup(String nameGroup) throws Exception {
		File file = new File(WBSAirbackConfiguration.getFileLocalDeviceGroups());
		if (!file.exists())
			return false;
		else {
			Configuration _c = new Configuration(file);
			if(_c.checkProperty(nameGroup, "local")) {
				return true;
			} 
		}
		return false;
	}
	
	public static boolean isLogicalVolumeMounted(String group, String name) {
		try {
			if(name.contains("/")) {
				name = name.replace("/", "");
			}
			File _f = new File("/proc/mounts");
			BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(!_line.startsWith("/dev/mapper") && !_line.contains(" fuse ") && !_line.contains(" zfs ") && !_line.startsWith("kstat ")) {
						continue;
					} else if(_line.contains(" "+getMountPath(group, name)+" ")) {
						return true;
					}
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			return true;
		}
		return false;
	}
	
	public static boolean isMount(String group, String name) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			return false;
		}
		for(String[] _value : FileSystemManager.readFstab().values()) {
			if(FileSystemManager.isSupportedFilesystemType(_value[2]) &&
					getLogicalVolumeDevicePath(group, name).equals(_value[0])) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isOnRemovingProcess(String group, String name) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("removeVolBlock-");
		_sb.append(group);
		_sb.append("-");
		_sb.append(name);
		_sb.append(".rv");
		File _block_file = new File(WBSAirbackConfiguration.getDirectoryRemoveVolumeBlock()+"/"+_sb.toString());
		if (_block_file.exists())
			return true;
		return false;
	}
	
	public static boolean isSystemMounted(String path) {
		try {
			File _f = new File("/proc/mounts");
			BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					String test = path.replace("\\ ", "\\040");
					if (test.endsWith("/"))
						test = test.substring(0,test.length()-1);
					if(_line.contains(" " + test + " ") || _line.contains(" " + test + "/ "))
						return true;
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			return false;
		}
		return false;
	}
	
	
	public static void mountAggregate(String group) throws Exception {
		if(group == null || group.isEmpty()) {
			return;
		}
		
		for(String name : getLogicalVolumeNames(group)) {
			mountLogicalVolume(group, name);
		}
	}
	
	public static void mountLogicalVolume(String group, String name) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		
		Map<String, String[]> _data = FileSystemManager.readFstab();
		if(!_data.containsKey(getMountPath(group, name))) {
			throw new Exception("logical volume is not mountable");
		}
		if(isLogicalVolumeMounted(group, name)) {
			return;
		}
		
		File _f = new File(getMountPath(group, name));
		if(!_f.exists()) {
			_f.mkdirs();
		}
		FileSystemManager.mountFileSystem(group, name, _data.get(getMountPath(group, name))[3]);
		mountLogicalVolumeSnapshotsOfVolume(group, name);
	}
	
	public static void mountLogicalVolumeSnapshot(String group, String name, String snapshot) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume snapshot");
		}
		
		if(!isMount(group, name)) {
			throw new Exception("logical volume is not mountable");
		}
		FileSystemManager.mountFileSystemSnapshot(group, name, snapshot);
	}
	
	public static void mountLogicalVolumeSnapshotsOfVolume(String group, String name) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume snapshot");
		}
		
		if(isMount(group, name)) {
			List<Map<String, String>> snapshots = getLogicalVolumeSnapshots(group, name);
			if (snapshots != null && !snapshots.isEmpty()) {
				for (Map<String, String> snap : snapshots) {
					FileSystemManager.mountFileSystemSnapshot(group, name, snap.get("name"));
				}
			}
		}
	}
	
	public static void umountLogicalVolumeSnapshotsOfVolume(String group, String name) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume snapshot");
		}
		
		if(isMount(group, name)) {
			List<Map<String, String>> snapshots = getLogicalVolumeSnapshots(group, name);
			if (snapshots != null && !snapshots.isEmpty()) {
				for (Map<String, String> snap : snapshots) {
					umountLogicalVolumeSnapshot(group, name, snap.get("name"));
				}
			}
		}
	}
	
	public static void mountSystemVolume(String path) throws Exception {
		File _f = new File(path);
		if(!_f.exists()) {
			_f.mkdirs();
		}
		String _output = Command.systemCommand("/bin/mount " + path + " && /bin/echo \"yes\" || /bin/echo \"no\"").trim();
		if(!"yes".equals(_output)) {
			throw new Exception("unable to mount system volume [" + path + "]");
		}
	}
	
	public static void refreshLogicalVolumes() {
		try {
			logger.info("Re-montando todos los volúmenes del sistema ...");
			Command.systemCommand("/sbin/vgchange -a y");
			for(Map<String, String> _lv : getLogicalVolumes()) {
				if(isMount(_lv.get("vg"), _lv.get("name")) && !isLogicalVolumeMounted(_lv.get("vg"), _lv.get("name"))) {
					mountLogicalVolume(_lv.get("vg"), _lv.get("name"));
					FileSystemManager.updateRemountValueToFstab(_lv.get("vg"), _lv.get("name"), true);
				} else if (isLogicalVolumeMounted(_lv.get("vg"), _lv.get("name")))
					FileSystemManager.updateRemountValueToFstab(_lv.get("vg"), _lv.get("name"), true);
			}
		} catch(Exception _ex) {}
	}
	
	public static void removeAllLogicalVolumeSnapshot(String group, String name, int type) throws Exception {
		if(type != LV_SNAPSHOT_MANUAL && type != LV_SNAPSHOT_HOURLY && type != LV_SNAPSHOT_DAILY) {
			throw new Exception("invalid logical volume snapshot type");
		}
		getLogicalVolume(group, name);
		Exception _exception = null;
		for(Map<String, String> _snapshot : getLogicalVolumeSnapshots(group, name, type)) {
			try {
				removeLogicalVolumeSnapshot(group, name, _snapshot.get("name"));
			} catch(Exception _ex) {
				_exception = _ex;
			}
		}
		if(_exception != null) {
			throw _exception;
		}
	}
	
	public static void removeLogicalVolumeSnapshot(String group, String name, String snapshot) throws Exception {
		try {
			if (!ObjectLock.isBlock(ObjectLock.SNAPSHOTS_TYPE_OBJECT, snapshot, null)) {
				ObjectLock.block(ObjectLock.SNAPSHOTS_TYPE_OBJECT, snapshot, null);
				
				if(isMount(group, name)) {
					Map<String, String[]> _data = FileSystemManager.readFstab();
					String _type = _data.get(getMountPath(group, name))[2];
					if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type) ||
							FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _type)) {
						FileSystemManager.removeFileSystemSnapshot(group, name, snapshot);
						removeLogicalVolumeSnapshotIndex(getLogicalVolumeSnapshotType(group, name, snapshot), group, name, snapshot);
					}
				} else if(ISCSIManager.isVolumeTarget(group, name)) {
					try {
						Map<String, String> _target = ISCSIManager.getSnapshotTarget(group, name, snapshot);
						ISCSIManager.removeTarget(_target.get("iqn"));
					} catch(Exception _ex) {}
				}
			}
		} catch (Exception ex) {
			logger.error("Error removing logical volume snapshot. Group: {} Name: {} Snapshot: {}. Ex: {}", new Object[]{group, name, snapshot, ex.getMessage()});
			throw new Exception("Error removing logical volume snapshot. Ex: "+ex.getMessage());
		} finally {
			ObjectLock.unblock(ObjectLock.SNAPSHOTS_TYPE_OBJECT, snapshot, null);
		}
	}
	
	private static void removeLogicalVolumeSnapshotIndex(int type, String group, String name, String snapshot) throws Exception {
		File _f = null;
		switch(type) {
			case LV_SNAPSHOT_HOURLY: {
					StringBuilder _sb = new StringBuilder();
					_sb.append(getLogicalVolumeMountPath(group, name));
					_sb.append("/.snapshots/");
					_sb.append(".hourly");
					_f = new File(_sb.toString());
				}
				break;
			case LV_SNAPSHOT_DAILY: {
					StringBuilder _sb = new StringBuilder();
					_sb.append(getLogicalVolumeMountPath(group, name));
					_sb.append("/.snapshots/");
					_sb.append(".daily");
					_f = new File(_sb.toString());
				}
				break;
		}
		if(_f != null) {
			List<String> _snapshots = getLogicalVolumeSnapshotIndexes(type, group, name);
			if(_snapshots.contains(snapshot)) {
				_snapshots.remove(snapshot);
				StringBuilder _sb = new StringBuilder();
				for(String _snapshot : _snapshots) {
					_sb.append(_snapshot);
					_sb.append("\n");
				}
				FileSystem.writeFile(_f, _sb.toString());
			}
		}
	}
	
	public static void removePlannedSnapshot(String group, String volume, int type) throws Exception {
		if(group == null || volume == null || group.isEmpty() || volume.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		
		XMLDB _db = new XMLDB(new File(WBSAirbackConfiguration.getFilePlannedSnapshots()));
		switch(type) {
			case LV_SNAPSHOT_HOURLY: {
					List<Map<String, String>> _snapshots = new ArrayList<Map<String,String>>();
					for(XMLObject _o : _db.getObjects()) {
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						if(! ("hourly".equals(_snapshot.get("type")) && !group.equals(_snapshot.get("group")) && !volume.equals(_snapshot.get("volume")))) {
							_snapshots.add(_snapshot);
						}
					}
					
					List<XMLObject> _objects = new ArrayList<XMLObject>();
					for(Map<String, String> _snapshot : _snapshots) {
						XMLObject _o = _db.createXMLObject();
						_o.setType(_snapshot.get("type"));
						for(String _key : _snapshot.keySet()) {
							XMLAttribute _att = new XMLAttribute(_key);
							_att.setValue(_snapshot.get(_key));
							_o.addAttribute(_att);
						}
						_objects.add(_o);
					}
					_db.setObjects(_objects);
					_db.store();
				}
				break;
			case LV_SNAPSHOT_DAILY: {
					List<Map<String, String>> _snapshots = new ArrayList<Map<String,String>>();
					for(XMLObject _o : _db.getObjects()) {
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						if(! ("daily".equals(_snapshot.get("type")) && !group.equals(_snapshot.get("group")) && !volume.equals(_snapshot.get("volume")))) {
							_snapshots.add(_snapshot);
						}
					}
					
					List<XMLObject> _objects = new ArrayList<XMLObject>();
					for(Map<String, String> _snapshot : _snapshots) {
						XMLObject _o = _db.createXMLObject();
						_o.setType(_snapshot.get("type"));
						for(String _key : _snapshot.keySet()) {
							XMLAttribute _att = new XMLAttribute(_key);
							_att.setValue(_snapshot.get(_key));
							_o.addAttribute(_att);
						}
						_objects.add(_o);
					}
					_db.setObjects(_objects);
					_db.store();
				}
				break;
			default:
				throw new Exception("invalid planned snapshot type");
		}
	}
	
	public static void removeVolumeGroup(String group) throws Exception {
		if(ZFSConfiguration.poolExists(group)) {
			if(!ZFSConfiguration.getPoolFileSystemNames(group).isEmpty()) {
				throw new Exception("volume group already has logical volumes");
			}
			logger.info("Eliminando agregado zfs {} ...",group);
			ZFSConfiguration.removePool(group);
			try {
				Thread.sleep(1000);
				Command.systemCommand("rm -r "+WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+group);
			} catch (Exception ex) {}
		} else if(volumeGroupExists(group)) {
			List<String> _lvs = getLogicalVolumeNames(group);
			if(!_lvs.isEmpty()) {
				throw new Exception("volume group already has logical volumes");
			}
			if(ZFSConfiguration.poolExists(group)) {
				logger.info("Eliminando agregado zfs {} ...",group);
				ZFSConfiguration.removePool(group);
			} else if(volumeGroupExists(group)) {
				logger.info("Eliminando agregado {} ...",group);
				List<String> _pvs = getPhysicalVolumeNames(group);
				Command.systemCommand("/sbin/vgremove -f " + group);
				for(String _device : _pvs) {
					Command.systemCommand("/sbin/pvremove " + _device);
				}
			} else {
				throw new Exception("volume group does not exists");
			}
			try {
				Thread.sleep(1000);
				Command.systemCommand("rm -r "+WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+group);
			} catch (Exception ex) {}
		}
		if (isLocalDeviceGroup(group))
			setNoLocalDeviceGroup(group);
	}
	
	/**
	 * Establece un volumeGroup como local 
	 * @param nameGroup
	 * @throws Exception
	 */
	public static void setLocalDeviceGroup(String nameGroup) throws Exception {
		try {
			logger.info("Estableciendo grupo: {} como local", nameGroup);
			File file = new File(WBSAirbackConfiguration.getFileLocalDeviceGroups());
			if (!file.exists()) {
				StringBuilder xml_content = new StringBuilder();
				xml_content.append("<localDevices></localDevices>");
				FileSystem.writeFile(file, xml_content.toString());
			}
			
			Configuration _c = new Configuration(file);
			_c.setProperty(nameGroup, "local");
			_c.store();
		} catch (Exception ex) {
			logger.error("Error al establer grupo: {} como local. Ex: {}", nameGroup, ex.getMessage());
			throw new Exception("Error on puttin volume group local");
		}
	}
	
	/**
	 * Establece un volumeGroup como no local
	 * @param nameGroup
	 * @throws Exception
	 */
	public static void setNoLocalDeviceGroup(String nameGroup) throws Exception {
		try {
			logger.info("Estableciendo grupo {} como no local", nameGroup);
			File file = new File(WBSAirbackConfiguration.getFileLocalDeviceGroups());
			if (file.exists()) {
				Configuration _c = new Configuration(file);
				if (_c.hasProperty(nameGroup))
					_c.removeProperty(nameGroup);
				_c.store();
			}
		} catch (Exception ex) {
			logger.error("Error al elimina de grupos locales a: {}", nameGroup);
			throw new Exception("Error deleting volume group local");
		}
	}
	
	public static void setPlannedSnapshot(String group, String volume, int type, int hour, int retention) throws Exception {
		if(group == null || volume == null || group.isEmpty() || volume.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		if(retention <= 0) {
			throw new Exception("invalid retention");
		}
		
		String _fstype = getLogicalVolumeFS(group, volume);
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _fstype)) {
			throw new Exception("filesystem type does not support snapshots");
		}
		
		XMLDB _db = new XMLDB(new File(WBSAirbackConfiguration.getFilePlannedSnapshots()));
		switch(type) {
			case LV_SNAPSHOT_HOURLY: {
					Map<String, Map<String, String>> _snapshots = new HashMap<String, Map<String,String>>();
					for(XMLObject _o : _db.getObjects()) {
						StringBuilder _sb = new StringBuilder();
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						_sb.append(_snapshot.get("group"));
						_sb.append("/");
						_sb.append(_snapshot.get("volume"));
						if("hourly".equals(_snapshot.get("type"))) {
							_sb.append("/hourly");
							if(group.equals(_snapshot.get("group")) && volume.equals(_snapshot.get("volume"))) {
								_snapshot.put("retention", String.valueOf(retention));
							}
						} else {
							_sb.append("/daily");
						}
						_snapshots.put(_sb.toString(), _snapshot);
					}
					StringBuilder _sb = new StringBuilder();
					_sb.append(group);
					_sb.append("/");
					_sb.append(volume);
					_sb.append("/hourly");
					Map<String, String> _newsnapshot = new HashMap<String, String>();
					_newsnapshot.put("type", "hourly");
					_newsnapshot.put("group", group);
					_newsnapshot.put("volume", volume);
					_newsnapshot.put("retention", String.valueOf(retention));
					_snapshots.put(_sb.toString(), _newsnapshot);
					
					List<XMLObject> _objects = new ArrayList<XMLObject>();
					for(Map<String, String> _snapshot : _snapshots.values()) {
						XMLObject _o = _db.createXMLObject();
						_o.setType(_snapshot.get("type"));
						for(String _key : _snapshot.keySet()) {
							XMLAttribute _att = new XMLAttribute(_key);
							_att.setValue(_snapshot.get(_key));
							_o.addAttribute(_att);
						}
						_objects.add(_o);
					}
					_db.setObjects(_objects);
					_db.store();
				}
				break;
			case LV_SNAPSHOT_DAILY: {
					Map<String, Map<String, String>> _snapshots = new HashMap<String, Map<String,String>>();
					for(XMLObject _o : _db.getObjects()) {
						StringBuilder _sb = new StringBuilder();
						Map<String, String> _snapshot = new HashMap<String, String>();
						for(XMLAttribute _att : _o.getAttributes()) {
							_snapshot.put(_att.getName(), _att.getValue());
						}
						_sb.append(_snapshot.get("group"));
						_sb.append("/");
						_sb.append(_snapshot.get("volume"));
						if("daily".equals(_snapshot.get("type"))) {
							_sb.append("/daily");
							if(group.equals(_snapshot.get("group")) && volume.equals(_snapshot.get("volume"))) {
								_snapshot.put("retention", String.valueOf(retention));
								_snapshot.put("hour", String.valueOf(hour));
							}
						} else {
							_sb.append("/hourly");
						}
						_snapshots.put(_sb.toString(), _snapshot);
					}
					StringBuilder _sb = new StringBuilder();
					_sb.append(group);
					_sb.append("/");
					_sb.append(volume);
					_sb.append("/daily");
					Map<String, String> _newsnapshot = new HashMap<String, String>();
					_newsnapshot.put("type", "daily");
					_newsnapshot.put("group", group);
					_newsnapshot.put("volume", volume);
					_newsnapshot.put("retention", String.valueOf(retention));
					_newsnapshot.put("hour", String.valueOf(hour));
					_snapshots.put(_sb.toString(), _newsnapshot);
					
					List<XMLObject> _objects = new ArrayList<XMLObject>();
					for(Map<String, String> _snapshot : _snapshots.values()) {
						XMLObject _o = _db.createXMLObject();
						_o.setType(_snapshot.get("type"));
						for(String _key : _snapshot.keySet()) {
							XMLAttribute _att = new XMLAttribute(_key);
							_att.setValue(_snapshot.get(_key));
							_o.addAttribute(_att);
						}
						_objects.add(_o);
					}
					_db.setObjects(_objects);
					_db.store();
				}
				break;
			default:
				throw new Exception("invalid planned snapshot type");
		}
	}
		
	public static void setVolumeMissingRefValues() throws Exception {
		List<Map<String, String>> volumes = getLogicalVolumes();
		for (Map<String, String> volume : volumes) {
			if (volume.containsKey("zfstype") && volume.get("zfstype").equals("filesystem") && volume.containsKey("norefs") && volume.get("norefs").equals("true")) {
				try {
					StringBuilder _sb = new StringBuilder();
					_sb.append(ZFSConfiguration.getZFSParamCommand(volume.get("vg"), volume.get("name"), "refquota", Double.valueOf(volume.get("size-raw"))));
					_sb.append(" && ");
					_sb.append(ZFSConfiguration.getZFSParamCommand(volume.get("vg"), volume.get("name"), "refreservation", Double.valueOf(volume.get("reservation-raw"))));
					logger.info("Estableciendo, en proceso de migración, refquota {} y refreservation {} a {}/{}", new Object[]{volume.get("size-raw"), volume.get("reservation-raw"), volume.get("vg"), volume.get("name")});
					logger.debug("Comando: {}", _sb.toString());
					Command.systemCommand(_sb.toString());
					logger.info("Establecidos refquota y refreservation correctamente");
				} catch (Exception ex) {
					logger.error("Error estableciendo, en proceso de migración, refquota {} y refreservation {} a {}/{}", new Object[]{volume.get("size-raw"), volume.get("reservation-raw"), volume.get("vg"), volume.get("name")});
				}
			}
		}
	}
	
	
	public static void umountAggregate(String group) throws Exception {
		if(group == null || group.isEmpty()) {
			return;
		}
		
		List<String> _lvs = getLogicalVolumeNames(group);
		for(String name : _lvs) {
			if(!isMount(group, name)) {
				continue;
			}
			
			if(ShareManager.isShare(group, name)) {
				throw new Exception("logical volume [" + group + "/" + name + "] is a share");
			}
			
			ReplicationManager _rm = new ReplicationManager();
			for(Map<String, String> _replica : _rm.getDestinations()) {
				if(_replica.get("vg").equals(group) &&
						_replica.get("lv").equals(name)) {
					throw new Exception("logical volume [" + group + "/" + name + "] is a replica destination");
				}
			}
			for(Map<String, String> _replica : _rm.getSources()) {
				if(_replica.get("vg").equals(group) &&
						_replica.get("lv").equals(name)) {
					throw new Exception("logical volume [" + group + "/" + name + "] is a replica source");
				}
			}
			for(Map<String, String> _storage : StorageManager.getDiskVolumeDevices()) {
				if("LV".equals(_storage.get("type"))) {
					if(_storage.get("vg") != null && _storage.get("lv") != null &&
							group.equals(_storage.get("vg")) &&
							name.equals(_storage.get("lv"))) {
						if(JobManager.hasRunningJobs(_storage.get("name"))) {
							throw new Exception("logical volume [" + group + "/" + name + "] has running jobs");
						}
					}
				}
			}
		}
		
		logger.info("Desmontando agregado {}",group);
		for(String name : _lvs) {
			if(!isMount(group, name)) {
				continue;
			}
			umountLogicalVolume(group, name, true);
			FileSystemManager.updateRemountValueToFstab(group, name, false);
		}
	}
	
	
	public static void umountLogicalVolume(String group, String name) throws Exception {
		umountLogicalVolume(group, name, false);
	}
	
	
	public static void umountLogicalVolume(String group, String name, boolean deleteDirectory) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		
		if(isLogicalVolumeMounted(group, name)) {
			try {
				umountLogicalVolumeSnapshotsOfVolume(group, name);
			} catch (Exception ex) {
				logger.error("Algún error desmontando snapshots de {}/{}. Ex: {}", new Object[]{group, name, ex.getMessage()});
			}
			StringBuilder _sb = new StringBuilder();
			_sb.append("/bin/umount ");
			_sb.append(getMountPath(group, name));
			_sb.append(" && /bin/echo \"yes\" || /bin/echo \"no\"");
			String _output = Command.systemCommand(_sb.toString()).trim();
			logger.info("Desmontando volumen: {}", _sb.toString());
			if(!"yes".equals(_output)) {
				logger.error("Se produjo un error al desmontar el volumen con: {}", _sb.toString());
				throw new Exception("unable to umount logical volume");
			}
		}
		if(deleteDirectory) {
			File _f = new File(getMountPath(group, name));
			_f.delete();
		}
	}
	
	
	public static void umountLogicalVolumeSnapshot(String group, String name, String snapshot) throws Exception {
		if(group == null || name == null || group.isEmpty() || name.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		
		String _mount = getLogicalVolumeSnapshotMountPath(group, name, snapshot);
		if(isSystemMounted(_mount)) {
			Command.systemCommand("zfs umount "+group+"/"+name+"/"+snapshot);
		}
	}
	
	public static void umountSystemVolume(String path) throws Exception {
		String _output = Command.systemCommand("/bin/umount " + path + " && echo \"yes\" || echo \"no\"").trim();
		logger.info("Desmontando volumen: {}", path);
		if(!"yes".equals(_output)) {
			logger.error("No se pudo desmontar volumen: {}", path);
			throw new Exception("unable to umount volume [" + path + "]");
		}
	}
	
	private static boolean volumeGroupExists(String group) throws Exception {
		BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand("/sbin/vgdisplay --noheadings --units B -C --separator : 2> /dev/null")));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				try {
					if(!_line.contains(":")) {
						continue;
					}
					String[] _values = _line.trim().split(":");
					if(_values[0].equals(group)) {
						return true;
					}
				} catch(Exception _ex) {}
			}
		} finally {
			_br.close();
		}
		return false;
	}
	
	
	public VolumeManager(Configuration conf) {
		
	}
	
	public void removeLogicalVolume(final String group, final String name) throws Exception {
		final StringBuilder _sb = new StringBuilder();
		_sb.append("removeVolBlock-");
		_sb.append(group);
		_sb.append("-");
		_sb.append(name);
		_sb.append(".rv");
		final File _block_file = new File(WBSAirbackConfiguration.getDirectoryRemoveVolumeBlock()+"/"+_sb.toString());
		
		File dirBlocks = new File(WBSAirbackConfiguration.getDirectoryRemoveVolumeBlock());
		if (dirBlocks.exists()) {
			String[] listDir = dirBlocks.list();
			if (listDir != null) {
	    		for (String el : listDir) {
	    			if (el.contains(".rv")) {
	    				logger.info("Another remove volume operation is in progress. Wait until it finishes");
	    				throw new Exception ("Another remove volume operation is in progress. Wait until it finishes");
	    			}
	    		}
			}
		}
		
		try {
			VolumeManager.removePlannedSnapshot(group, name, VolumeManager.LV_SNAPSHOT_HOURLY);
			VolumeManager.removePlannedSnapshot(group, name, VolumeManager.LV_SNAPSHOT_DAILY);
		} catch (Exception ex) {
			logger.error("Error eliminando snapshots planificados de {}/{}", group, name);
		}
		
		try {
			if(isMount(group, name)) {
				removeMount(group, name);
			} else {
				try {
					logger.info("El volumen que se intenta borrar {}/{} no está en el fstab.", group, name);
					if (isLogicalVolumeMounted(group, name)) {
						logger.info("El volumen que se intenta borrar {}/{} si esta montado, desmontamos...", group, name);
						umountLogicalVolume(group, name, true);
					}
				} catch (Exception ex){
					logger.error("Error al intentar desmontar el volumen a borrar {}/{}", group, name);
					throw new Exception("Error unmounting volume. Maybe resource is bussy :"+ex.getMessage());
				}
			}
		} catch (Exception ex) {
			logger.error("Error al intentar desmontar el volumen a borrar {}/{}", group, name);
			throw new Exception("Error unmounting volume. Maybe resource is bussy :"+ex.getMessage());
		}
		
		Runnable r = new Runnable() {
			public void run() {
				File _error_file = null;
				String _output = null;
				
				_error_file = new File("/tmp/"+_sb.toString());
				try {
					FileSystem.writeFile(_block_file, "--");
					
					Map<String, String> _volume = getLogicalVolume(group, name);

					try {
						FileSystemManager.removeMountFromFstab(group, name);
					} catch (Exception ex) {
						logger.error("Error al intentar eliminar de fstab el volumen a borrar {}/{}", group, name);
					}
					
					logger.info("Eliminando volumen logico {}/{}", group, name);
					if(ZFSConfiguration.fileSystemExists(group, name)) {
						_output = ZFSConfiguration.removeFileSystem(group, name);
					} else {
						String command = "/sbin/lvremove -f " + _volume.get("path");
						if (!Command.isRunning(command))
							_output = Command.systemCommand(command);
					}
					
					try {
						Thread.sleep(250);
						File dir = new File(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+group+"/"+name);
						if (dir.exists())
							Command.systemCommand("rm -r "+WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+group+"/"+name);
					} catch (Exception ex) {
						logger.error("No se pudo borrar el directorio del volumen a borrar: {}. Ex: {}", WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+group+"/"+name, ex.getMessage());
					}

					if(_error_file.exists()) {
						_error_file.delete();
					}
					
				} catch (Exception ex) {
					logger.error("Se produjo un error al intentar borrar el volumen {}/{} . Ex: {}", new Object[]{ group, name, ex.getMessage() });
					if(!_error_file.exists()) {
						try {
							FileOutputStream _fos = new FileOutputStream(_error_file);
							try {
								if(_output != null) {
									_fos.write(_output.getBytes());
								} else {
									_fos.write(("unknown remove volume error "+group+"/"+name).getBytes());
								}
							} finally {
								_fos.close();
							}
						} catch(Exception _ex2) {}
					}
				} finally {
					_block_file.delete();
				}
			}
		};
		
		Thread internalThread = new Thread(r);
		internalThread.setName(getThreadName(group, name));
		internalThread.start();
	}
	
	public void removeMount(String group, String name) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		Map<String, String> _lv = getLogicalVolume(group, name);
		if(!isMount(group, name)) {
			throw new Exception("logical volume is not a mount point");
		}
		
		List<String> _storages = StorageManager.getDiskVolumeDevicesNamesForLogicalVolume(group, name);
		if(!_storages.isEmpty()) {
			throw new Exception("this logical volume are currently used by backup storage [" + _storages.get(0) + "]");
		}
		
		if (!HAConfiguration.isSlaveNode()) {
			if(ShareManager.isShare(ShareManager.CIFS, group, name)) {
				ShareManager.removeShare(group, name, ShareManager.CIFS);
			}
			if(ShareManager.isShare(ShareManager.NFS, group, name)) {
				ShareManager.removeShare(group, name, ShareManager.NFS);
			}
			if(ShareManager.isShare(ShareManager.FTP, group, name)) {
				ShareManager.removeShare(group, name, ShareManager.FTP);
			}
		}
		
		if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")) ||
				FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _lv.get("fstype"))) {
			for(Map<String, String> _snapshot : FileSystemManager.getFileSystemSnapshots(group, name)) {
				FileSystemManager.removeFileSystemSnapshot(group, name, _snapshot.get("name"));
			}
		}
		umountLogicalVolume(group, name, true);
		FileSystemManager.removeMountFromFstab(group, name);
	}
	
	public static double getXFSAggOcupped(List<Map<String, String>> _vg_disks, Map<String, String> vg) throws Exception {
		double occuped = Double.parseDouble(vg.get("size-raw"));
		for (Map<String, String> disk: _vg_disks) {
			occuped-=VolumeManager.getByteSizeFromHuman(disk.get("free-raw"));
		}
		return occuped;
	}
	
	public static void setSnapshotManualRemove(String group, String volume, boolean val) throws Exception {
		Configuration c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		if (val)
			c.setProperty(group+"_"+volume+"_snapshot_manual_remove", "true");
		else
			c.setProperty(group+"_"+volume+"_snapshot_manual_remove", "false");
		c.store();
	}
	
	public static boolean isSnapshotManualRemoveOn(String group, String volume) throws Exception {
		Configuration c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		if (c.hasProperty(group+"_"+volume+"_snapshot_manual_remove") && c.getProperty(group+"_"+volume+"_snapshot_manual_remove").equals("false"))
			return false;
		else
			return true;
	}
}
