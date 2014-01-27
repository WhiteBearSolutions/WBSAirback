package com.whitebearsolutions.imagine.wbsairback.bacula;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.db.DBException;
import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.util.Configuration;

public class StorageManager {
	private Configuration _c; 
	public static String VDRIVE_STRING = "-vDrive-";
	
	
	public StorageManager(Configuration conf) {
		this._c = conf;
	}
	
	public static void addAutochangerDevice(String name, List<String> drive, String changer, String format, String volumeGroup, String volume, int spool_size, String units, String address) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		for(Map<String, String> _device : getAutochangerDevices()) {
			if(name.equals(_device.get("name"))) {
				throw new Exception("backup storage already exists");
			}
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
		try {
			updateAutochangerDevice(name, drive, changer, format, volumeGroup, volume, spool_size, units, address);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
			throw _ex;
		}
	}
	public static void addAutochangerDevice(String name, List<String> drive, String changer, String format, String volumeGroup, String volume, int spool_size, String units) throws Exception {
		addAutochangerDevice(name,drive,changer,format,volumeGroup,volume,spool_size, units, null);
	}
	
	public static void addTapeDevice(String name, String drive, String format, String volumeGroup, String volume, int spool_size,String address, boolean auto_mount) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		for(Map<String, String> _device : getTapeDevices()) {
			if(name.equals(_device.get("name"))) {
				throw new Exception("backup storage already exists");
			}
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf","storages", name);
		try {
			updateTapeDevice(name, drive, format, volumeGroup, volume, spool_size,address, auto_mount);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","storages", name);
			throw _ex;
		}
	}
	
	public static void addExternalShareDevice(String name, String server, String share,String address, Integer paralelJobs) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		name = name.replaceAll("\\b\\s+\\b", "");
		for(Map<String, String> _device : getDiskVolumeDevices()) {
			if(name.equals(_device.get("name"))) {
				throw new Exception("backup storage name already exists");
			}
		}
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
		try {
			updateExternalShareDevice(name, server, share,address, paralelJobs, true);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
			throw _ex;
		}
	}
	
	public static void addLogicalVolumeDevice(String name, String group, String volume,String address, boolean aligned, Integer paralelJobs) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		name = name.replaceAll("\\b\\s+\\b", "");
		volume = volume.replaceAll("\\b\\s+\\b", "");
		for(Map<String, String> _device : getDiskVolumeDevices()) {
			if(name.equals(_device.get("name"))) {
				throw new Exception("backup storage name already exists");
			}
		}
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
		try {
			updateLogicalVolumeDevice(name, group, volume,address, aligned, paralelJobs, true);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
			throw _ex;
		}
	}
	
	public static void addRemoteDevice(String name, String type, String device, String[] address, String password, String mediatype) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+") || type == null || type.isEmpty() || device == null || device.isEmpty() || address == null || address.length != 4 || password == null || password.isEmpty()) {
			throw new Exception("invalid parameters");
		}
		name = name.replaceAll("\\b\\s+\\b", "");

		for(String _device : getRemoteDevices()) {
			if(name.equals(_device)) {
				throw new Exception("backup storage name already exists");
			}
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf",	"storages", name);
	    File file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".xml");
		if(file.exists()) {
	        file.delete();
		}
		
		try {
			updateRemoteDevice(name, type, device, address, password, mediatype);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
			throw _ex;
		}
	}
	
	public static List<String> getAllLocalDevicesNames() throws Exception {
		List<String> resources = new ArrayList<String>();
		for(String device : BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf", "Device")) {
			try {
				File _f = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".xml");
				if(_f.exists()) {
					Configuration _cdevice = new Configuration(_f);
					if(_cdevice.checkProperty("storage.external", "yes")) {
						continue;
					}
				}
				resources.add(device);
			} catch(Exception _ex) {}
		}
		return resources;
	}
	
	public static List<Map<String, String>> getAutochangerDevices() throws Exception {
		List<Map<String, String>> resources = new ArrayList<Map<String, String>>();
		
		for(String resource : BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf", "Autochanger")) {
			String device = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", resource, "Changer Device");
			if (!device.equals("/dev/null")) {
				Map<String, String> values = new HashMap<String, String>();
				values.put("name", resource);
				values.put("device", device);
				
				int j = 0;
				for(String drive : BaculaConfiguration.getBaculaParameters("/etc/bacula/bacula-sd.conf", "Autochanger", resource, "Device")) {
					if(j == 0) {
						values.put("type", BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Media Type"));
						try {
							if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Spool Directory") != null) {
								String[] _path = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Spool Directory").split("/");
								values.put("spool", _path[_path.length - 2] + "/" + _path[_path.length - 1]);
								if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Maximum Spool Size") != null) {
									String pool_size = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Maximum Spool Size").trim();
									int _size = 0;
									try {
										if(pool_size.endsWith("g")) {
											_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
											_size = _size / 1024;
										} else if(pool_size.endsWith("m")) {
											_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
										}  else if(pool_size.endsWith("k")) {
											_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
											_size = _size * 1024;
										}
									} catch(NumberFormatException _ex) {}
									values.put("spool-size", String.valueOf(_size));
								}
							} else {
								values.put("spool", "unknown");
							}
						} catch(Exception _ex) {
							values.put("spool", "unknown");
						}
					}
					values.put("drive" + j, drive);
					j++;
				}
				resources.add(values);
			}
		}
		
		return resources;
	}
	
	/**
	 * Busca los repositorios a los cuales está asociado cierto volumen loǵico
	 * @param lv_group		[String]		Grupo del volumen lógico
	 * @param lv_name		[String]		Nombre del volumen lógico
	 * @return				[List<String>]	Lista de nombres de repositorios asociados al volumen lógico
	 * @throws Exception
	 */
	public List<String> findStorageByLogicalVolume(String lv_group, String lv_name) throws Exception {
		List<String> linked_storages = new ArrayList<String>();
		String _mountPathVolume = WBSAirbackConfiguration.getDirectoryVolumeMount() +"/"+lv_group+"/"+lv_name;
		List<String> _available_storages = this.getAvailableStorages();
		if (_available_storages != null && _available_storages.size() > 0) {
			for (String storage : _available_storages)  {
				String _archive_device = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", storage, "Archive Device");
				if (_archive_device.equals(_mountPathVolume)) {
					linked_storages.add(storage);
				}
			}
		}
		return linked_storages;
	}
	
	
	public static Map<String, String> getAutochangerDevice(String name) throws Exception {
		Map<String, String> resource = new HashMap<String, String>();
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid autochanger device name");
		}
		if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Changer Device") == null) {
			throw new Exception("autochanger device [" + name + "] not found");
		}
		resource.put("name", name);
		String device = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Changer Device");
		if (device == null || device.isEmpty())
			throw new Exception("autochanger device [" + name + "] not found");
		resource.put("device", device);
		String value = BaculaConfiguration.getBaculaParameter("/etc/bacula/storages/"+name+".conf", "Storage", name, "Address");
		resource.put("address", value);
		int j = 0;
		for(String drive : BaculaConfiguration.getBaculaParameters("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Device")) {
			if(j == 0) {
				resource.put("type", BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Media Type"));
				try {
					if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Spool Directory") != null) {
						String[] _path = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Spool Directory").split("/");
						resource.put("spool", _path[_path.length - 2] + "/" + _path[_path.length - 1]);
						if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Maximum Spool Size") != null) {
							String pool_size = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", drive, "Maximum Spool Size").trim();
							int _size = 0;
							try {
								if(pool_size.endsWith("g")) {
									_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
									_size = _size * 1024;
								} else if(pool_size.endsWith("m")) {
									_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
								}  else if(pool_size.endsWith("k")) {
									_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
									_size = _size / 1024;
								}
							} catch(NumberFormatException _ex) {}
							resource.put("spool-size", String.valueOf(_size));
						}
					} else {
						resource.put("spool", "unknown");
					}
				} catch(Exception _ex) {
					resource.put("spool", "unknown");
				}
			}
			resource.put("drive" + j, drive);
			j++;
		}
		return resource;
	}
	
	public List<String> getAvailableStorages() throws Exception {
		DBConnection connection = null;
	    List<String> storages = new ArrayList<String>();
		
		connection = new DBConnectionManager(this._c).getConnection();
		for(Map<String, Object> result : connection.query("SELECT g.name FROM storage as g WHERE g.name <> \'" + this._c.getProperty("bacula.defaultStorage") + "\' ORDER BY g.name ASC")) {
			storages.add(String.valueOf(result.get("name")));
		}
		return storages;
	}
	
	public static List<String> getAvailableVolumeDevicesNames() throws Exception {
		List<String> _devices = new ArrayList<String>();
		
		for(Map<String, String> device : getDiskVolumeDevices()) {
			_devices.add(device.get("device"));
		}
		
		return _devices;
	}
	
	public static Map<String, String> getDiskVolumeDevice(String device) throws Exception {
		Map<String, String> values = new HashMap<String, String>();
		if(device == null || device.isEmpty()) {
			return values;
		}
		String  value = BaculaConfiguration.getBaculaParameter("/etc/bacula/storages/"+device+".conf", "Storage", device, "Address");
		if (value != null && !value.isEmpty())
			values.put("address", value);
		else
			return values;
		
		String nameDevice = device;
		value = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".conf", "Storage", device, "Autochanger");
		if (value != null && !value.isEmpty() && value.equals("yes")) {
			putParalelJobs(device, values);
			nameDevice = getVirtualDeviceName(device, "R");
		}
		
		value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", nameDevice, "Device Type");
		if (value != null && !value.isEmpty())
			values.put("deviceType", value);
		
		value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", nameDevice, "Media Type");
		if (value != null && !value.isEmpty())
			values.put("mediaType", value);
		
		value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", nameDevice, "Archive Device");
		if(value != null && value.contains(WBSAirbackConfiguration.getDirectoryVolumeMount()))
			values.putAll(getStorageParams(nameDevice, value));
		values.put("name", device);
		return values;
	}
	
	private static void putParalelJobs(String repo, Map<String, String> values) throws Exception {
		Integer paralelJobs = 0;
		Integer tmpParalelJobs = 0;
		String deviceType = null;
		for(String device : BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf","Device")) {
			try {
				if (device.contains(repo+VDRIVE_STRING)) {
					String value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Drive Index");
					if (value != null && !value.isEmpty())
						tmpParalelJobs = Integer.parseInt(value);
					if (tmpParalelJobs > paralelJobs)
						paralelJobs = tmpParalelJobs;
					if (deviceType == null) {
						deviceType = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Device Type");
						if (deviceType != null && !deviceType.isEmpty())
							values.put("deviceType", deviceType);
					}
				}
			} catch(Exception _ex) {}
		}
		
		values.put("paralelJobs", paralelJobs.toString());
	}
	
	public static List<Map<String, String>> getDiskVolumeDevices() throws Exception {
		List<Map<String, String>> resources = new ArrayList<Map<String, String>>();
		for(String device : BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf","Device")) {
			try {
				if (!device.contains(VDRIVE_STRING)) {
					String value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Archive Device");
					if(value.contains(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
						Map<String, String> values = getDiskVolumeDevice(device);
						resources.add(values);
					}
				}
			} catch(Exception _ex) {}
		}
		
		for(String device : BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf","Autochanger")) {
			try {
				String changerDevice = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", device, "Changer Device");
				if (changerDevice != null && changerDevice.equals("/dev/null")) {
					String value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", getVirtualDeviceName(device, "R"), "Archive Device");
					if(value.contains(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
						Map<String, String> values = getDiskVolumeDevice(device);
						resources.add(values);
					}
				}
			} catch(Exception _ex) {}
		}
		
		return resources;
	}
	
	public static Map<String, String> getStorageParams(String device, String value) throws Exception {
		Map<String, String> values = new HashMap<String, String>();
		values.put("name", device);
		String[] _value = value.split("/");
		if(!value.contains("/shares/") && VolumeManager.isMount(_value[_value.length - 2], _value[_value.length - 1])) {
			values.put("type", "LV");
			values.put("device", _value[_value.length - 2] + "/" + _value[_value.length - 1]);
			values.put("vg", _value[_value.length - 2]);
			values.put("lv", _value[_value.length - 1]);
		} else {
			values.put("type", "EXT");
			value = value.replace(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/shares/", "").trim();
			if(value.endsWith("/")) {
				value = value.substring(0, value.length() - 1);
			}
			_value = new String[2];
			_value[0] = value.substring(0, value.indexOf("/"));
			_value[1] = value.substring(value.indexOf("/"));
			values.put("device", _value[0] + "@" + _value[1]);
			values.put("server", _value[0]);
			values.put("share", _value[1]);
		}
		return values;
	}
	
	public static List<String> getDiskVolumeDevicesNamesForLogicalVolume(String group, String volume) throws Exception {
		List<String> _storages = new ArrayList<String>();
		if(group == null || volume == null) {
			return _storages;
		}
		for(Map<String, String> disk_storage : getDiskVolumeDevices()) {
			if(disk_storage.get("type") != null && "LV".equals(disk_storage.get("type"))) {
				if(disk_storage.get("device") != null && disk_storage.get("device").equals(group + "/" + volume)) {
					_storages.add(disk_storage.get("name"));
				}
			}
		}
		return _storages;
	}
	
	public static Map<String, String> getTapeDevice(String name) throws Exception {
		Map<String, String> values = new HashMap<String, String>();
		
		String value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Device Type");
		if(value != null && "Tape".equals(value)) {
			values.put("name", name);
			String device = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Archive Device");
			if (device == null || device.isEmpty())
				return new HashMap<String, String>();
			values.put("device", device);
			values.put("type", BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Media Type"));
			value = BaculaConfiguration.getBaculaParameter("/etc/bacula/storages/"+name+".conf", "Storage", name, "Address");
			values.put("address", value);
			
			boolean auto_mount = false;
			if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AlwaysOpen") != null && BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AlwaysOpen").equals("no")) {
				auto_mount = true;
			}
			values.put("auto_mount", String.valueOf(auto_mount));
			
			try {
				if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Spool Directory") != null) {
					String[] _path = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Spool Directory").split("/");
					values.put("spool", _path[_path.length - 2] + "/" + _path[_path.length - 1]);
					if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Maximum Spool Size") != null) {
						String pool_size = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Maximum Spool Size").trim();
						int _size = 0;
						try {
							if(pool_size.endsWith("g")) {
								_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
								_size = _size / 1024;
							} else if(pool_size.endsWith("m")) {
								_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
							}  else if(pool_size.endsWith("k")) {
								_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
								_size = _size * 1024;
							}
						} catch(NumberFormatException _ex) {}
						values.put("spool-size", String.valueOf(_size));
					}
				} else {
					values.put("spool", "unknown");
				}
			} catch(Exception _ex) {
				values.put("spool", "unknown");
			}
		}
		
		return values;
	}
	
	public static List<Map<String, String>> getTapeDevices() throws Exception {
		List<Map<String, String>> resources = new ArrayList<Map<String, String>>();
			
		for(String device : BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf", "Device")) {
			try {
				String value = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Device Type");
				if(value != null && value.equals("Tape")) {
					Map<String, String> values = new HashMap<String, String>();
					values.put("name", device);
					values.put("device", BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Archive Device"));
					values.put("type", BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Media Type"));
					try {
						if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Spool Directory") != null) {
							String[] _path = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Spool Directory").split("/");
							values.put("spool", _path[_path.length - 2] + "/" + _path[_path.length - 1]);
							if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Maximum Spool Size") != null) {
								String pool_size = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Maximum Spool Size").trim();
								int _size = 0;
								try {
									if(pool_size.endsWith("g")) {
										_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
										_size = _size / 1024;
									} else if(pool_size.endsWith("m")) {
										_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
									}  else if(pool_size.endsWith("k")) {
										_size = Integer.parseInt(pool_size.substring(0, pool_size.length() - 1));
										_size = _size * 1024;
									}
								} catch(NumberFormatException _ex) {}
								values.put("spool-size", String.valueOf(_size));
							}
						} else {
							values.put("spool", "unknown");
						}
					} catch(Exception _ex) {
						values.put("spool", "unknown");
					}
					resources.add(values);
				}
			} catch(Exception _ex) {}
		}
		
		return resources;
	}
	
	public static List<String> getRemoteDevices() throws Exception {
		List<String> resources = new ArrayList<String>();
		String[] _files = new File(WBSAirbackConfiguration.getDirectoryStorage()).list();
		
		if(_files == null) {
			return resources;
		} else {
			for(String filename : _files) {
				try {
					if(filename.endsWith(".xml")) {
						filename = filename.substring(0, filename.length() - 4);
						File file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + filename + ".xml");
		    			if(file.exists()) {
		    				Configuration _c = new Configuration(file);
		    				if(_c.checkProperty("storage.external", "yes")) {
								resources.add(filename);
							}
						}
					}
				} catch(Exception _ex) {}
			}
		}
		
		return resources;
	}
	
	public static String getStorageConfiguration(String name) {
		StringBuilder _sb = new StringBuilder();
		File _file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf");
		if(!_file.exists()) {
			return _sb.toString();
		}
		
		try {
			FileInputStream fstream = new FileInputStream(_file);
			BufferedReader in = new BufferedReader(new InputStreamReader(fstream));
			String line = null;
			while((line = in.readLine()) != null) {
				_sb.append(line);
				_sb.append("\n");
			}
			in.close();
		} catch(Exception _ex) {}
		
		return _sb.toString();
	}
	
	public static Map<String, String> getStorageParameters(String name) {
		Map<String, String> parameters = new HashMap<String, String>();
		File _file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".xml");
		if(!_file.exists()) {
			return parameters;
		}
		
		try {
			Configuration _conf = new Configuration(_file);
			for(String property : _conf.getPropertyNames()) {
				parameters.put(property, _conf.getProperty(property));
			}
			if (parameters.size()>0)
				parameters.put("name", name);
		} catch(Exception _ex) {}
		
		return parameters;
	}
	
	public static void updateAllStoragePublicAddress() throws Exception {
		Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Storage", _c.getProperty("bacula.defaultStorage"), "Address", new String[] { "127.0.0.1" });
		for(String _name : getAllLocalDevicesNames()) {
			File _f = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + _name + ".conf");
			if(_f.exists()) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Storage", _name, "Address", new String[]{ NetworkManager.getPublicAddress() });
			}
		}
		BackupOperator.reload();
	}
	
	
	public static void updateAllStorageByInterface(String oldIp, String newIp) {
		try {
			Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Storage", _c.getProperty("bacula.defaultStorage"), "Address", new String[] { "127.0.0.1" });
		} catch(Exception _ex) {
			System.out.println("Error: fail to update default storage network address - " + _ex.getMessage());
		} 
		File dir = new File(WBSAirbackConfiguration.getDirectoryStorage());
		String[] _list = dir.list();
		if(_list != null) {
			try {
				for(String name : _list) {
					File _f = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name);
					if(_f.exists()) {
						String ipOld = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name, "Storage", name.substring(0, name.length()-5), "Address");
						if(oldIp.equals(ipOld)) {
							BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name, "Storage", name.substring(0, name.length()-5), "Address", newIp == null || newIp.isEmpty() ? new String[]{ NetworkManager.getPublicAddress() } : new String[]{ newIp });
						} 
					}
				}
				ServiceManager.restart(ServiceManager.BACULA_SD);
			} catch(Exception _ex) {
				System.out.println("Error: fail to update storage network address - " + _ex.getMessage());
			}  
		}
		try {
			BackupOperator.reload();
		} catch(Exception _ex) {
			System.out.println("Error: fail to update storage network address configuration - " + _ex.getMessage());
		}
	}
	
	public static void updateAutochangerDevice(String name, List<String> drive, String changer, String format, String volumeGroup, String volume, int spool_size, String units, String address) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		if(drive == null || drive.isEmpty()) {
			throw new Exception("must specify at least one drive");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		List<Map<String, String>> _originalDrives= LibraryManager.getStorageDrives(name);
		List<Map<String, String>> _deletedDrives= new ArrayList<Map<String,String>>();
		if (_originalDrives!=null && _originalDrives.size()>0){
			for (Map<String,String> _dri : _originalDrives){
				if (!drive.contains("/dev/tape/by-id/"+_dri.get("name"))){
					_deletedDrives.add(_dri);
				} 
			}
		}
		try {
			if(volumeGroup != null && !volumeGroup.isEmpty() && volume != null && !volume.isEmpty()) {
				if(!VolumeManager.isMount(volumeGroup, volume)) {
					throw new Exception("spool logical volume is not mountable");
				}
			}
		
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Changer Command", new String[] { "\"/opt/bacula/scripts/mtx-changer %c %o %S %a %d\"" });
			if(changer.startsWith("/dev/")) {
				BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Changer Device", new String[] { changer });
			} else {
				BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Changer Device", new String[] { "/dev/" + changer });
			}
			
			String[] _drives = new String[drive.size()];
			int offset = 0;
			for(Map<String,String> _dri : _deletedDrives) {
				String _name = _dri.get("name");
				BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Device", _name);
			}
			for(String value : drive) {
				String _name = value;
				if(_name.contains("/")) {
					_name = _name.substring(_name.lastIndexOf("/") + 1);
				}
				_drives[offset] = _name;
				if(!value.isEmpty()) {
					double _size = 0;
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "Drive Index", new String[] { String.valueOf(offset) });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "Media Type", new String[ ] { format });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "Archive Device", new String[] { value });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "AutomaticMount", new String[] { "yes" });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "Autochanger", new String[] { "yes" });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "AlwaysOpen", new String[] { "yes" });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "RemovableMedia", new String[] { "yes"});
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "RandomAccess", new String[] { "no" });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "AutoChanger", new String[] { "yes" });
					BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "Alert Command", new String[] { "\"sh -c 'tapeinfo -f %c |grep TapeAlert|cat'\"" });
					if(spool_size > 0) {
						_size = new Double(spool_size).doubleValue();
					} else if(volumeGroup != null && !volumeGroup.isEmpty() && volume != null && !volume.isEmpty()) {
						_size = VolumeManager.getLogicalVolumeSize(volumeGroup, volume);
						_size = _size * 0.70;
					}
					if(_size > 0) {
						StringBuffer _sb = new StringBuffer();
						_sb.append(Math.round((_size*100)/100));
						_sb.append(units.toLowerCase());
						
						BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "Spool Directory", new String[] { VolumeManager.getLogicalVolumeMountPath(volumeGroup, volume) });
						BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _name, "Maximum Spool Size", new String[] { _sb.toString() });
					}
					offset++;
				}
			}
			
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Device", _drives);
			
			BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Address", address==null || address.isEmpty() ? new String[]{ NetworkManager.getPublicAddress() } : new String[]{address});
		    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "SDPort", new String[]{ "9103" });
		    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Password", new String[]{ "\"" + BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Director", "airback-dir", "Password") + "\"" });
		    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Device", new String[]{ name });
		    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Media Type", new String[]{ format });
		    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Autochanger", new String[]{ "yes" });
		    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Maximum Concurrent Jobs", new String[]{ "50" });
		    
		    ServiceManager.restart(ServiceManager.BACULA_SD);
		    BackupOperator.reload();
		} catch(Exception _ex) {
			System.out.println("Error updating autochanger:" + _ex.toString());
			throw _ex;
		}
	}
	
	
	public static void updateAutochangerDevice(String name, List<String> drive, String changer, String format, String volumeGroup, String volume, int spool_size, String units) throws Exception {
		updateAutochangerDevice(name, drive, changer, format, volumeGroup, volume, spool_size, units, null);
	}
	
	public static void updateExternalShareDevice(String name, String server, String share,String address, Integer paralelJobs, boolean isNew) throws Exception {
		String _path = ShareManager.getExternalShareMountPath(server, share);
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		String mediaType = calculateMediaType(name, isNew, _path);
		
		removeVirtualAutoChagerDevices(name);
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Device", name);
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Autochanger", name);
		
		name = name.replaceAll("\\b\\s+\\b", "");
		BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Address", address == null || address.isEmpty() ? new String[] { NetworkManager.getPublicAddress() } : new String[] {address});
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "SDPort", new String[]{ "9103" });
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Password", new String[]{ "\"" + BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Director", "airback-dir", "Password") + "\"" });
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Device", new String[]{ name });
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Media Type", new String[]{ mediaType  });
	    BaculaConfiguration.deleteBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Autochanger");
		if (paralelJobs == null || paralelJobs <= 1) {
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Media Type", new String[]{ mediaType  });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Archive Device", new String[]{ _path });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "LabelMedia", new String[]{ "yes" });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Random Access", new String[]{ "Yes" });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AutomaticMount", new String[]{ "yes" });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "RemovableMedia", new String[]{ "no" });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AlwaysOpen", new String[]{ "no" });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Spool Directory", new String[]{ _path });
			BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Maximum Concurrent Jobs", new String[]{ "50" });
	    } else {
	    	Integer maxConcurrentJobs = paralelJobs*3;
	    	if (maxConcurrentJobs < 50)
	    		maxConcurrentJobs = 50;
	    	BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Autochanger", new String[]{ "yes" });
	    	BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Maximum Concurrent Jobs", new String[]{ maxConcurrentJobs.toString() });
	    	setVirtualAutoChagerDevice(name, _path, paralelJobs, false, mediaType);
	    }
		
	    ServiceManager.restart(ServiceManager.BACULA_SD);
	    BackupOperator.reload();
	}
	public static void updateExternalShareDevice(String name, String server, String share, Integer paralelJobs) throws Exception{
		updateExternalShareDevice(name,server,share,null);
	}
	
	public static void updateLogicalVolumeDevice(String name, String group, String volume,String address, boolean aligned, Integer paralelJobs, boolean isNew) throws Exception {
		String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		String mediaType = calculateMediaType(name, isNew, _path);
		
		removeVirtualAutoChagerDevices(name);
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Device", name);
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Autochanger", name);
		
		name = name.replaceAll("\\b\\s+\\b", "");
		volume = volume.replaceAll("\\b\\s+\\b", "");
		BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Address", address==null || address.isEmpty() ? new String[] { NetworkManager.getPublicAddress() } : new String[] {address});
		BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "SDPort", new String[]{ "9103" });
		BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Password", new String[]{ "\"" + BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Director", "airback-dir", "Password") + "\"" });
		BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Device", new String[]{ name });
		BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Media Type", new String[]{ mediaType  });
		BaculaConfiguration.deleteBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Autochanger");
		if (paralelJobs == null || paralelJobs <= 1) {
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Media Type", new String[]{ mediaType });
		    BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Archive Device", new String[]{ _path });
		    BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "LabelMedia", new String[]{ "yes" });
		    BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Random Access", new String[]{ "Yes" });
		    BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AutomaticMount", new String[]{ "yes" });
		    BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "RemovableMedia", new String[]{ "no" });
		    BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AlwaysOpen", new String[]{ "no" });
		    BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Spool Directory", new String[]{ _path });
		    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Maximum Concurrent Jobs", new String[]{ "50" });
		    if (aligned) {
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Device Type", new String[]{ "Aligned" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Maximum Block Size", new String[]{ "128K" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Minimum Block Size", new String[]{ "0" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "File Alignment", new String[]{ "128K" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Padding Size", new String[]{ "512" });
		    } else {
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Device Type");
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Maximum Block Size");
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Minimum Block Size");
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "File Alignment");
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Padding Size");
		    }
		} else {
	    	Integer maxConcurrentJobs = paralelJobs*3;
	    	if (maxConcurrentJobs < 50)
	    		maxConcurrentJobs = 50;
	    	BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Autochanger", new String[]{ "yes" });
	    	BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Maximum Concurrent Jobs", new String[]{ maxConcurrentJobs.toString() });
	    	setVirtualAutoChagerDevice(name, _path, paralelJobs, aligned, mediaType);
	    }

	    ServiceManager.restart(ServiceManager.BACULA_SD);
	    BackupOperator.reload();
	}
	
	private static String calculateMediaType(String device, boolean isNew, String path) throws Exception {
		String mediaType = "File";
		if (!isNew) {
			Map<String, String> storage = getDiskVolumeDevice(device);
			if (storage.get("mediaType") != null && !storage.get("mediaType").isEmpty()) {
				mediaType = storage.get("mediaType");
			}
		} else if (path != null && !path.isEmpty()){
			if (path.contains("/shares/")) {
				mediaType = path.substring(path.indexOf("/shares/")+"/shares/".length()).trim();
				mediaType = mediaType.replace("/", "-").replace(" ", "");
			} else if (path.contains(WBSAirbackConfiguration.getDirectoryVolumeMount())){
				mediaType = path.substring(path.indexOf(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/")+(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/").length()).trim();
				mediaType = mediaType.replace("/", "-").replace(" ", "");
			}
		}
		return mediaType;
	}
	
	private static void setVirtualAutoChagerDevice(String device, String path, Integer numDevices, boolean aligned, String mediaType) throws Exception {
		String []_drives = new String[numDevices+1];
		int o=1;
		for (o=1; o<=numDevices; o++) {
			_drives[o-1] = getVirtualDeviceName(device, String.valueOf(o));
		}
		_drives[o-1] = getVirtualDeviceName(device, "R");
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", device, "Device", _drives);
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", device, "Changer Device", new String[]{"/dev/null"});
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", device, "Changer Command", new String[]{"/dev/null"});
		
		String name_drive = null;
		for (int i=1; i<=(numDevices+1); i++) {
			name_drive = _drives[i-1];
			
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Media Type", new String[]{mediaType });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Archive Device", new String[]{path});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "AutomaticMount", new String[]{"yes"});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "AlwaysOpen", new String[]{"yes"});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "RemovableMedia", new String[]{"yes"});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Autochanger", new String[]{"yes"});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Drive Index", new String[]{new Integer(i-1).toString()});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Maximum Concurrent Jobs", new String[]{"1"});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Volume Poll Interval", new String[]{"15"});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Label Media", new String[]{"yes"});
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Spool Directory", new String[] {path});
			if (aligned) {
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Device Type", new String[]{ "Aligned" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Maximum Block Size", new String[]{ "128K" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Minimum Block Size", new String[]{ "0" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "File Alignment", new String[]{ "128K" });
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Padding Size", new String[]{ "512" });
		    } else {
		    	BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Device Type", new String[]{"File"});
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Maximum Block Size");
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Minimum Block Size");
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "File Alignment");
		    	BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name_drive, "Padding Size");
		    }
		}
	}
	
	private static void removeVirtualAutoChagerDevices(String device) throws Exception {
		List<String> devices = BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf", "Device");
		for (String d : devices) {
			if (d.contains(device+VDRIVE_STRING))
				BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Device", d);
		}
		
		devices = BaculaConfiguration.getBaculaResources("/etc/bacula/bacula-sd.conf", "Autochanger");
		for (String d : devices) {
			if (d.contains(device))
				BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Autochanger", d);
		}
	}
	
	public static String getVirtualDeviceName(String device, String order) {
		return device+VDRIVE_STRING+order;
	}
	
	public static void updateTapeDevice(String name, String drive, String format, String volumeGroup, String volume, int spool_size, String address, boolean auto_mount) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid storage name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Media Type", new String[] { format });
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Device Type", new String[] { "Tape" });
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Archive Device", new String[] { drive });
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AutomaticMount", new String[] { "yes" });
		if (!auto_mount)
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AlwaysOpen", new String[]{ "yes" });
		else 
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AlwaysOpen", new String[]{ "no" });
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "RemovableMedia", new String[] { "yes" });
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "RandomAccess", new String[] { "no"});
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "AutoChanger", new String[] { "no"});
		
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Alert Command", new String[] { "\"sh -c 'tapeinfo -f "+TapeManager.getTapeDeviceById(drive.replace("/dev/st", "/dev/nst"))+" |grep TapeAlert|cat'\""});
		if(volumeGroup != null && !volumeGroup.isEmpty() && volume != null && !volume.isEmpty()) {
			double _size = (VolumeManager.getLogicalVolumeSize(volumeGroup, volume) * 1024) / 1000;
			if(spool_size > 0) {
				_size = new Double(spool_size).doubleValue() * 1024;
			} else if(volumeGroup != null && !volumeGroup.isEmpty() && volume != null && !volume.isEmpty()) {
				_size = VolumeManager.getLogicalVolumeSize(volumeGroup, volume);
				_size = _size * 0.70;
			}
			if(_size > 0) {
				StringBuilder _sb =new StringBuilder();
				if(_size >= 1048576) {
					_sb.append(Math.round(((_size/1048576)*100)/100));
					_sb.append("g");
				} else if(_size >= 1024) {
					_sb.append(Math.round(((_size/1024)*100)/100));
					_sb.append("m");
				} else {
					_sb.append(Math.round((_size*100)/100));
					_sb.append("k");
				}
				
				BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Spool Directory", new String[] { VolumeManager.getLogicalVolumeMountPath(volumeGroup, volume) });
				BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", name, "Maximum Spool Size", new String[] { _sb.toString() });
			}
		}

		BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Address", address==null || address.isEmpty() ? new String[] { NetworkManager.getPublicAddress() } : new String[] {address});
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "SDPort", new String[] { "9103" });
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Password", new String[] { "\"" + BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Director", "airback-dir", "Password") + "\""});
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Device", new String[]{ name });
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Media Type", new String[]{ format });
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Autochanger", new String[] { "no" });
	    BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf", "Storage", name, "Maximum Concurrent Jobs", new String[] { "50" });
	    
	    ServiceManager.restart(ServiceManager.BACULA_SD);
	    BackupOperator.reload();
	}
	
	/*public static void updateTapeDevice(String name, String drive, String format, String volumeGroup, String volume, int spool_size, boolean a) throws Exception{
		updateTapeDevice(name, drive, format, volumeGroup, volume, spool_size,null);
	}*/
	
	public static void updateRemoteDevice(String name, String type, String device, String[] address, String password, String mediatype) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-._]+") || type == null || type.isEmpty() || device == null || device.isEmpty() || address == null || address.length != 4 || password == null || password.isEmpty()) {
			throw new Exception("invalid parameters");
		}
		
		List<String> _types = Arrays.asList(new String[] { "File", "Tape", "Autochanger" });
		name = name.replaceAll("\\b\\s+\\b", "");
		
		if(!_types.contains(type)) {
			throw new Exception("invalid remote storage type");
		}
		if(mediatype == null || mediatype.isEmpty()) {
			mediatype = "File";
		}
		if(type == null || type.isEmpty()) {
			type = "File";
		}
		
		Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".xml"));
		_c.setProperty("storage.external", "yes");
		_c.setProperty("storage.devicetype", type);
		_c.setProperty("storage.device", device);
		_c.setProperty("storage.address", NetworkManager.addressToString(address));
		_c.setProperty("storage.password", password);
		_c.setProperty("storage.mediatype", mediatype);
		_c.store();
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("Storage {\n");
		_sb.append("  Name = ");
		_sb.append(name);
		_sb.append("\n");
		_sb.append("  Address = ");
		_sb.append(NetworkManager.addressToString(address));
		_sb.append("\n");
		_sb.append("  Password = \"");
		_sb.append(password);
		_sb.append("\"\n");
		_sb.append("  Device = ");
		_sb.append(device);
		_sb.append("\n");
		_sb.append("  Media Type = ");
		_sb.append(mediatype);
		_sb.append("\n");
		_sb.append("  Maximum Concurrent Jobs = 50\n");
		if (type.equals("Autochanger")) {
			_sb.append("  AutoChanger = yes\n");
		}
		_sb.append("}\n");
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + name + ".conf");
		FileLock _fl = new FileLock(_f);
		FileOutputStream _fos = new FileOutputStream(_f);
		try {
			_fl.lock();
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fl.unlock();
			} catch(Exception _ex) {}
			_fos.close();
		}

		BackupOperator.reload();
	}
	
	public void removeVolumeDevice(String name) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		List<String> _names = PoolManager.getPoolsForStorage(name);
		if(!_names.isEmpty()) {
			throw new Exception("this storage still used by pool [" + _names.get(0) + "]");
		}
	    
	    _names = JobManager.getJobsForStorage(name);
		if(!_names.isEmpty()) {
			throw new Exception("storage still used by job [" + _names.get(0) + "]");
		}
		
		try {
			DBConnection connection = new DBConnectionManager(this._c).getConnection();
			connection.query("DELETE FROM storage WHERE name='" + name + "'");
	    } catch (DBException _ex) {
			throw new DBException("fail to remove from database");
	    }
	    
	    removeVirtualAutoChagerDevices(name);
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Device", name);
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Autochanger", name);
		BaculaConfiguration.trimBaculaFile("/etc/bacula/bacula-sd.conf");
		ServiceManager.restart(ServiceManager.BACULA_SD);
		
		BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", name);
		BackupOperator.reload();
	}
	
	public void removeAutochangerDevice(String device) throws Exception {
		DBConnection connection = null;
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		try {
			connection = new DBConnectionManager(this._c).getConnection();
			List<Map<String, Object>> resultPool = connection.query("select p.name, p.poolid from pool as p where p.name <> \'" + this._c.getProperty("bacula.defaultPool") + "\' order by p.name asc");
			if(resultPool != null) {
				for(Map<String, Object> row : resultPool) {
					if(new File(WBSAirbackConfiguration.getDirectoryPools() + "/" + row.get("name") + ".conf").exists()) {
						if(BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryPools() + "/" + row.get("name") + ".conf", "Pool", String.valueOf(row.get("name")), "Storage").equals(device)) {
							throw new Exception("this storage is used by pool [" + row.get("name") + "]");
						}
					}
				}
			}
	    } catch(DBException _ex) {
	    	throw new DBException("fail to check database");
	    }	
		
	    try {
			connection.query("delete from storage where name='" + device + "'");
	    } catch (DBException _ex) {
			throw new DBException("fail to remove from database");
	    }
	    
	    BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", device);
	    File file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".xml");
		if(file.exists()) {
	        file.delete();
		}
		file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".conf");
		if(file.exists()) {
	        file.delete();
		}
		
		for(String dev : BaculaConfiguration.getBaculaParameters("/etc/bacula/bacula-sd.conf", "Autochanger", device, "Device")) {
			BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Device", dev);
		}
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Autochanger", device);
		
		ServiceManager.restart(ServiceManager.BACULA_SD);
		BackupOperator.reload();
	}
	
	public void removeRemoteDevice(String device) throws Exception {
		DBConnection connection = null;
	    if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		try {
			connection = new DBConnectionManager(this._c).getConnection();
			List<Map<String, Object>> resultPool = connection.query("SELECT p.name, p.poolid FROM pool as p WHERE p.name <> \'" + this._c.getProperty("bacula.defaultPool") + "\' ORDER BY p.name ASC");
			if(resultPool != null) {
				for(Map<String, Object> row : resultPool) {
					if(BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryPools() + "/" + row.get("name") + ".conf", "Pool", String.valueOf(row.get("name")), "Storage").equals(device)) {
						throw new Exception("this storage is used by pool [" + row.get("name") + "]");
					}
				}
			}
	    } catch(DBException _ex) {
	    	throw new DBException("fail to check database");
	    }
		
		try {
			connection.query("DELETE FROM storage WHERE name = '" + device + "'");
	    } catch(DBException _ex) {
			throw new DBException("unable to remove storage [" + device + "] from database");
	    }
	    
		BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "storages", device);
	    File file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".xml");
		if(file.exists()) {
	        file.delete();
		}
	    file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".conf");
		if(file.exists()) {
	        file.delete();
		}
		
		BackupOperator.reload();
	}
}
