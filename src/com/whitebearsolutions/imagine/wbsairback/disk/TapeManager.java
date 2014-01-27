package com.whitebearsolutions.imagine.wbsairback.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class TapeManager {
	private Configuration _c;
	
	public TapeManager(Configuration conf) {
		this._c = conf;
	}
	
	public static List<Map<String, String>> getStorageSlots(String storage) throws Exception {
		List<Map<String, String>> slots = new ArrayList<Map<String,String>>();
		String _output = Command.systemCommand("echo \"update slots storage=" + storage + " drive=all\" | /usr/bin/bconsole | grep Volume | tr -s \" \" | tr -d .");
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
					slot.put("name", Command.systemCommand("echo \"list volumes\" | bconsole | grep " + _values[4].replaceAll("\"", "") + " | tr -d \" \" | cut -d \"|\" -f 10"));
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
	
	public static void formatTape(String storage, String drive) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		umountTape(storage);
		ServiceManager.stop(ServiceManager.BACULA_SD);
		Command.systemCommand("mt -f "+getTapeDeviceById(drive)+" rewind && sleep 1 ");
		Command.systemCommand("mt -f "+getTapeDeviceById(drive)+" weof && sleep 1");
		ServiceManager.start(ServiceManager.BACULA_SD);
		mountTape(storage);
	}
	
	public static void mountTape(String storage) throws Exception {
		Command.systemCommand("echo \"mount storage=" + storage + "\" | /usr/bin/bconsole");
	}
	
	public static void umountTape(String storage) throws Exception {
		Command.systemCommand("echo \"umount storage=" + storage + "\" | /usr/bin/bconsole");
		Command.systemCommand("echo \"mount\" | /usr/bin/bconsole");
	}
	
	private static Map<String, String> getTapesById() throws Exception {
		Map<String, String> _tapes_by_id = new HashMap<String,String>();
		String _output = Command.systemCommand("ls -1 -o -T 1 /dev/tape/by-id/st-* | grep lrwxrwxrwx |awk '{print $8,$10}'");
		if(_output != null) {
			StringTokenizer _st = new StringTokenizer(_output, "\n");
			while(_st.hasMoreTokens()) {
				String[] _device = _st.nextToken().split(" ");
				_tapes_by_id.put("/dev/".concat(_device[1].replaceAll("../", "")), _device[0]);
			}
		}
		return _tapes_by_id;
	}
	
	public static String getTapeDeviceById(String device) throws Exception {
		Map<String, String> _tapes_by_id = getTapesById();
		if(_tapes_by_id.containsKey(device)) {
			return _tapes_by_id.get(device);
		}
		return device;
	}
	
	public static List<Map<String, String>> getTapeDevices() throws Exception {
		List<Map<String, String>> tapes = new ArrayList<Map<String,String>>();
		Map<String, String> _tapes_by_id = getTapesById();
		for(Map<String, String> _device : SCSIManager.getDevices(SCSIManager.TAPE)) {
			Map<String, String> tape = new HashMap<String, String>();
			if(_tapes_by_id.containsKey(_device.get("device"))) {
				tape.put("device", _tapes_by_id.get(_device.get("device")));
			} else if (_device.get("devicen")!=null && _tapes_by_id.containsKey(_device.get("devicen"))) {
				tape.put("device", _tapes_by_id.get(_device.get("devicen")));
			} else {
				tape.put("device", _device.get("device"));
			}
			tape.put("scsi", _device.get("scsi"));
			tape.put("description", tape.get("device").substring(tape.get("device").lastIndexOf("/") + 1));
			if(tape.get("description").contains("-")) {
				tape.put("serial", tape.get("description").substring(tape.get("description").lastIndexOf("-") + 1));
			} else {
				tape.put("serial", tape.get("description"));
			}
			tape.put("model", _device.get("model"));
			tape.put("vendor", _device.get("vendor"));
			tapes.add(tape);
		}
		return tapes;
	}
	
	public static Map<String, String> getTapeDevice(String device) throws Exception {
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
			if(new File("/dev/tape/by-id/".concat(device)).exists()) {
				device = "/dev/tape/by-id/".concat(device);
			} else if(new File("/dev/".concat(device)).exists()) {
				device = "/dev/".concat(device);
			}
		}
		_unknown.put("device", device);
		Map<String, String> _tapes_by_id = getTapesById();
		for(Map<String, String> _device : getTapeDevices()) {
			if(device.equals(_device.get("device"))) {
				return _device;
			} else if(_tapes_by_id.containsKey(device) && _tapes_by_id.get(device).equals(_device.get("device"))) {
				return _device;
			}
		}
		return _unknown;
	}
	
	public static String getTapeDevicePath(String device) throws Exception {
		Map<String, String> _device = getTapeDevice(device);
		if(_device.get("device") == null) {
			return device;
		}
		return _device.get("device");
	}
	
	public static String getTapeDeviceDescription(String device) throws Exception {
		StringBuilder _sb = new StringBuilder();
		Map<String, String> _device = getTapeDevice(device);
		_sb.append(_device.get("vendor"));
		_sb.append(" / ");
		_sb.append(_device.get("model"));
		_sb.append(" (");
		_sb.append(_device.get("serial"));
		_sb.append(")");
		return _sb.toString();
	}
	
	public void removeTape(String device) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		DBConnection connection = new DBConnectionManager(this._c).getConnection();
	    for(Map<String, Object> row : connection.query("select p.name, p.poolid from pool as p where p.name <> \'" + this._c.getProperty("bacula.defaultPool") + "\' order by p.name asc")) {
			if(BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryPools() + "/" + row.get("name").toString() + ".conf", "Pool", row.get("name").toString(), "Storage").equals(device)) {
				throw new Exception("storage used by pool " + row.get("name").toString());
			}
		}
		
		connection.query("delete from storage where name='" + device + "'");
		
		BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","storages", device);
	    File file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".xml");
		if(file.exists()) {
	        file.delete();
		}
	    file = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + device + ".conf");
		if(file.exists()) {
	        file.delete();
		}
		
		BaculaConfiguration.deleteBaculaResource("/etc/bacula/bacula-sd.conf", "Device", device);
		ServiceManager.restart(ServiceManager.BACULA_SD);
		
		BackupOperator.reload();
	}
}
