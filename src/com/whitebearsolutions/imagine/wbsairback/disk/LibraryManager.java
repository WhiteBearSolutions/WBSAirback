package com.whitebearsolutions.imagine.wbsairback.disk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.util.Command;

public class LibraryManager {
	public LibraryManager() {
		
	}
	
	public static List<Map<String, String>> getAutochangers() throws Exception {
		List<Map<String, String>> _changers = new ArrayList<Map<String,String>>();
		Map<String, String> _changers_by_id = getAutochangersById();
		for(Map<String, String> _device : SCSIManager.getDevices(SCSIManager.CHANGER)) {
			Map<String, String> changer = new HashMap<String, String>();
			if(_changers_by_id.containsKey(_device.get("device"))) {
				changer.put("device", _changers_by_id.get(_device.get("device")));
			} else {
				changer.put("device", _device.get("device"));
			}
			changer.put("scsi", _device.get("scsi"));
			changer.put("description", changer.get("device").substring(changer.get("device").lastIndexOf("/") + 1));
			if(changer.get("description").contains("-")) {
				changer.put("serial", changer.get("description").substring(changer.get("description").lastIndexOf("-") + 1));
			} else {
				changer.put("serial", changer.get("description"));
			}
			changer.put("model", _device.get("model"));
			changer.put("vendor", _device.get("vendor"));
			_changers.add(changer);
		}
		return _changers;
	}
	
	private static Map<String, String> getAutochangersById() throws Exception {
		Map<String, String> _changers_by_id = new HashMap<String,String>();
		String _output = Command.systemCommand("ls -1 -o -T 1 /dev/tape/by-id/sch-*| grep lrwxrwxrwx |awk '{print $8,$10}'");
		if(_output != null) {
			StringTokenizer _st = new StringTokenizer(_output, "\n");
			while(_st.hasMoreTokens()) {
				String[] _device = _st.nextToken().split(" ");
				_changers_by_id.put("/dev/".concat(_device[1].replaceAll("../", "")), _device[0]);
			}
		}
		return _changers_by_id;
	}
	
	public static String getAutochangerDeviceById(String device) throws Exception {
		Map<String, String> _changers_by_id = getAutochangersById();
		if(_changers_by_id.containsKey(device)) {
			return _changers_by_id.get(device);
		}
		return device;
	}
	
	public static Map<String, String> getAutochangerDevice(String device) throws Exception {
		Map<String, String> _unknown = new HashMap<String, String>();
		_unknown.put("model", "Unknown");
		_unknown.put("vendor", "Unknown");
		_unknown.put("version", "Unknown");
		_unknown.put("description", "none");
		_unknown.put("serial", "Unknown");
		if(device == null || device.isEmpty()) {
			return _unknown;
		}
		if(!device.startsWith("/dev/")) {
			device = "/dev/".concat(device);
		}
		_unknown.put("device", device);
		Map<String, String> _changers_by_id = getAutochangersById();
		for(Map<String, String> _device : getAutochangers()) {
			if(device.equals(_device.get("device"))) {
				return _device;
			} else if(_changers_by_id.containsKey(device) && _changers_by_id.get(device).equals(_device.get("device"))) {
				return _device;
			}
		}
		return _unknown;
	}
	
	public static String getAutochangerDescription(String device) throws Exception {
		StringBuilder _sb = new StringBuilder();
		Map<String, String> _device = getAutochangerDevice(device);
		_sb.append(_device.get("vendor"));
		_sb.append(" / ");
		_sb.append(_device.get("model"));
		_sb.append(" (");
		_sb.append(_device.get("serial"));
		_sb.append(")");
		return _sb.toString();
	}
	
	public static List<Map<String, String>> getStorageSlots(String storage) throws Exception {
		List<Map<String, String>> slots = new ArrayList<Map<String,String>>();
		String _output = Command.systemCommand("/bin/echo \"update slots storage=" + storage + " drive=all\" | /usr/bin/bconsole | /bin/grep Volume | /usr/bin/tr -s \" \" | /usr/bin/tr -d .");
		StringTokenizer _st = new StringTokenizer(_output.trim(), "\n");
		while(_st.hasMoreTokens()){
			try {
				Map<String, String> slot = new HashMap<String, String>();
				String _line = _st.nextToken();
				String[] _values = _line.split(" ");
				if(_line.contains("not found in catalog")) {
					slot.put("status", "unassigned");
					slot.put("name", _values[6].replaceAll("Slot=", ""));
					slot.put("value", _values[1].replaceAll("\"", ""));
				} else if(_line.contains("Catalog record for Volume")) {
					slot.put("status", "assigned");
					slot.put("name", Command.systemCommand("/bin/echo \"list volumes\" | /usr/bin/bconsole | /bin/grep " + _values[4].replaceAll("\"", "") + " | /usr/bin/tr -d \" \" | /usr/bin/cut -d \"|\" -f 10").trim());
					slot.put("value", _values[4].replaceAll("\"", ""));
				}
				slots.add(slot);
			} catch(Exception _ex) {
				Map<String, String> slot = new HashMap<String, String>();
				slot.put("name", "error:" + _ex.getMessage());
				slot.put("status", "unknown");
				slot.put("value", "unknown");
				slots.add(slot);
			}
		}
		return slots;
	}
	
	public static List<Map<String, String>> getStorageDrives(String storage) throws Exception {
		List<Map<String, String>> drives = new ArrayList<Map<String,String>>();
		String _data = Command.systemCommand("/bin/echo \"status storage=" + storage + "\" | /usr/bin/bconsole");
		
		for(String baculaDevice : BaculaConfiguration.getBaculaParameters("/etc/bacula/bacula-sd.conf", "Autochanger", storage, "Device")) {
			if(baculaDevice != null && !baculaDevice.isEmpty()) {
				Map<String, String> drive = new HashMap<String, String>();
				try {
					drive.put("name", baculaDevice);
					drive.put("label", "unknown");
					drive.put("index", BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", baculaDevice, "Drive Index"));
					String _line = getMultipleMatchLine(_data, "\"" + baculaDevice + "\"", "Device");
					if(_line.contains("not open")) {
						drive.put("status", "unassigned");
						_line = getMultipleNextMacthLine(_data, "\"" + baculaDevice + "\"", "Device", "is loaded in drive " + drive.get("index"));
						if(!_line.isEmpty()) {
							_line = _line.split(" ")[1];
							drive.put("slot", _line);
						} else {
							drive.put("status", "empty");
							drive.put("slot", "none");
						}
					} else if(_line.contains("open but no Bacula volume is currently mounted")) {
						_line = getMultipleNextMacthLine(_data, "\"" + baculaDevice + "\"", "Device", "is loaded in drive " + drive.get("index"));
						if(!_line.isEmpty()) {
							drive.put("status", "unassigned");
							_line = _line.split(" ")[1];
							drive.put("slot", _line);
						} else {
							drive.put("status", "empty");
							drive.put("slot", "none");
						}
					} else if(_line.contains("mounted with")) {
						drive.put("status", "assigned");
						_line = getMultipleNextMacthLine(_data, "\"" + baculaDevice + "\"", "Device", "is loaded in drive " + drive.get("index"));
						if(!_line.isEmpty()) {
							_line = _line.split(" ")[1];
							drive.put("slot", _line);
						} else {
							drive.put("slot", "none");
						}
						_line = getMultipleNextMacthLine(_data, "\"" + baculaDevice + "\"", "Device", "Volume:");
						if(!_line.isEmpty()) {
							_line = _line.trim().substring(7).trim();
							drive.put("label", _line);
						} else {
							drive.put("label", "unknown");
						}
					}
					drives.add(drive);
				} catch(Exception _ex) {
					drive.put("name", "error:" + _ex.getMessage());
					drive.put("status", "unknown");
					drive.put("label", "unknown");
					drive.put("slot", "unknown");
					drives.add(drive);
				}
			}
		}
		return drives;
	}
	
	public static void mountSlotToDrive(String storage, int drive, int slot) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"mount storage=");
		_sb.append(storage);
		if(drive >= 0) {
			_sb.append(" drive=");
			_sb.append(drive);
		}
		_sb.append(" slot=");
		_sb.append(slot);
		_sb.append("\"");
		_sb.append(" | /usr/bin/bconsole");		
	    String output = Command.systemCommand(_sb.toString());
	    if (output != null && (output.contains("Unable") || output.contains("Fail")))
	    	throw new Exception("Fail mounting slot to Drive: "+output);
	}
		
	public static void umountDrive(String storage, int drive) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"umount storage=");
		_sb.append(storage);
		if(drive >= 0) {
			_sb.append(" drive=");
			_sb.append(drive);
		}
		_sb.append("\"");
		_sb.append(" | /usr/bin/bconsole");
		
		Command.systemCommand(_sb.toString());
		
		_sb = new StringBuilder();
		_sb.append("/bin/echo \"mount storage=");
		_sb.append(storage);
		if(drive >= 0) {
			_sb.append(" drive=");
			_sb.append(drive);
		}
		_sb.append("\"");
		_sb.append(" | /usr/bin/bconsole");
		Command.systemCommand(_sb.toString());
	}
	
	public static void formatTape(String storage, int drive) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		String device = "";
		try {
			device = BaculaConfiguration.getBaculaParameters("/etc/bacula/bacula-sd.conf", "Autochanger", storage, "Device").get(drive);
		} catch(ArrayIndexOutOfBoundsException _ex) {
			throw new Exception("invalid drive number [" + drive + "]");
		}
		
		ServiceManager.fullStop(ServiceManager.BACULA_SD);
		try {
			Thread.sleep(1000);
			Command.systemCommand("/bin/mt -f " + BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Archive Device") + " rewind");
			Command.systemCommand("/bin/mt -f " + BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Archive Device") + " weof");
		} finally {
			ServiceManager.start(ServiceManager.BACULA_SD);
		}
	}
	
	private static String getMultipleMatchLine(String data, String match1, String match2) {
		if(data == null || data.isEmpty()) {
			return "";
		}
		
		for(String _line : data.split("\n")) {
			if(_line.contains(match1) && _line.contains(match2)) {
				return _line.trim();
			}
		}
		return "";
	}
	
	private static String getMultipleNextMacthLine(String data, String match1, String match2, String secondMatch) {
		if(data == null || data.isEmpty()) {
			return "";
		}
		
		String[] _lines = data.split("\n");
		for(int i = 0; i < _lines.length; i++) {
			String _line = _lines[i];
			if(!_line.isEmpty() && _line.contains(match1) && _line.contains(match2)) {
				for(i++; i < _lines.length; i++) {
					_line = _lines[i];
					if (_line.contains("=="))
						return "";
					if(!_line.isEmpty() && _line.contains(secondMatch)) {
						return _line.trim();
					}
				}
				break;
			}
		}
		return "";
	}
}
