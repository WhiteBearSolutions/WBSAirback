package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class ISCSIManager {
	public ISCSIManager(Configuration conf) throws Exception {
		initialize();
	}
	
	public static void addChangerTarget(String device, String iqn, String user, String password) throws Exception {
		Map<String, String> _device = LibraryManager.getAutochangerDevice(device);
		if(_device.get("scsi") == null) {
			throw new Exception("cannot found scsi information");
		}
		
		List<Map<String, String>> _targets = getAllTargets();
		Map<String, String> _target = new HashMap<String, String>();
		_target.put("iqn", iqn);
		_target.put("type", "autochanger");
		_target.put("scsi", _device.get("scsi"));
		if(user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
			if(password.length() < 12) {
				throw new Exception("invalid password lenght for target (>12 characters)");
			}
			_target.put("user", user);
			_target.put("password", password);
		}
		_targets.add(_target);
		writeTargets(_targets);
	}
	
	public static void addTapeTarget(String device, String iqn, String user, String password) throws Exception {
		Map<String, String> _device = TapeManager.getTapeDevice(device);
		if(_device.get("scsi") == null) {
			throw new Exception("cannot determine scsi id");
		}
		
		List<Map<String, String>> _targets = getAllTargets();
		Map<String, String> _target = new HashMap<String, String>();
		_target.put("iqn", iqn);
		_target.put("type", "tape");
		_target.put("scsi", _device.get("scsi"));
		if(user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
			if(password.length() < 12) {
				throw new Exception("invalid password lenght for target (>12 characters)");
			}
			_target.put("user", user);
			_target.put("password", password);
		}
		
		_targets.add(_target);
		writeTargets(_targets);
	}
	
	public static void addVolumeTargets(String group, String volume, String iqn, String user, String password) throws Exception {
		if(group == null || group.isEmpty() || volume == null || volume.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		if(iqn == null || iqn.trim().isEmpty()) {
			iqn = getRandomIQN();
		}
		VolumeManager.getLogicalVolume(group, volume);
		if(VolumeManager.isMount(group, volume)) {
			throw new Exception("volume must be SAN type");
		}
		
		List<Map<String, String>> _targets = getAllTargets();
		Map<String, String> _target = new HashMap<String, String>();
		for(Map<String, String> _t : _targets) {
			if(_t.get("iqn") != null && iqn.equals(_t.get("iqn"))) {
				throw new Exception("iqn identifier already exists");
			}
		}
		_target.put("iqn", iqn);
		_target.put("type", "volume");
		_target.put("vg", group);
		_target.put("lv", volume);
		if(user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
			if(password.length() < 12) {
				throw new Exception("invalid password lenght for target (>12 characters)");
			}
			_target.put("user", user);
			_target.put("password", password);
		}
		
		_targets.add(_target);
		writeTargets(_targets);
	}
	
	public static void addVolumeSnapshotTargets(String group, String volume, String snapshot) throws Exception {
		if(group == null || group.isEmpty() || volume == null || volume.isEmpty()) {
			throw new Exception("invalid logical volume");
		}
		VolumeManager.getLogicalVolumeSnapshot(group, volume, snapshot);
		if(VolumeManager.isMount(group, volume)) {
			throw new Exception("volume must be SAN type");
		}
		
		StringBuilder _iqn = new StringBuilder();
		Map<String, String> _target = getVolumeTarget(group, volume);
		_iqn.append(_target.get("iqn"));
		_iqn.append(":snapshot");
		_iqn.append(getRandomChars(8));
		List<Map<String, String>> _targets = getAllTargets();
		for(Map<String, String> _t : _targets) {
			if(_t.get("iqn") != null && _t.get("iqn").equals(_iqn.toString())) {
				throw new Exception("iqn identifier already exists");
			}
		}
		_target.put("iqn", _iqn.toString());
		_target.put("lv", snapshot);
		
		_targets.add(_target);
		writeTargets(_targets);
	}
	
	public static List<Map<String, String>> getAllTargets() throws Exception {
		List<Map<String, String>> _targets = new ArrayList<Map<String,String>>();
		for(Map<String, String> _target : readTargets()) {
			if(_target.get("iqn").contains(":snapshot")) {
				continue;
			}
			_targets.add(_target);
		}
		return _targets;
	}
	
	public static List<Map<String, String>> getChangerTargets() throws Exception {
		List<Map<String, String>> _targets = new ArrayList<Map<String,String>>();
		Map<String, Map<String, String>> _devices = new HashMap<String, Map<String,String>>();
		for(Map<String, String> _device : LibraryManager.getAutochangers()) {
			if(_device.get("scsi") != null) {
				_devices.put(_device.get("scsi"), _device);
			}
		}
		for(Map<String, String> _target : readTargets()) {
			if((_target.get("type") == null || "autochanger".equalsIgnoreCase(_target.get("type"))) && _target.get("scsi") != null) {
				if(_devices.containsKey(_target.get("scsi"))) {
					Map<String, String> _device = _devices.get(_target.get("scsi"));
					_target.put("vendor", _device.get("vendor"));
					_target.put("model", _device.get("model"));
					_target.put("serial", _device.get("serial"));
				} else {
					_target.put("vendor", "Unknown");
					_target.put("model", "Unknown");
					_target.put("serial", "SCSI " + _target.get("scsi"));
				}
				_targets.add(_target);
			}
		}
		return _targets;
	}
	
	public static  String getClientInitiatorName() throws Exception {
		File _f = new File("/etc/iscsi/initiatorname.iscsi");
		if(_f.exists()) {
			BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
			String _line = null;
			while((_line = _br.readLine()) != null) {
				if(_line.startsWith("InitiatorName=")) {
					return _line.substring(14);
				}
			}
		}
		return getRandomIQN();
	}
	
	public static List<Map<String, String>> getExternalSessionTargets() throws Exception {
		String _output = Command.systemCommand("/usr/bin/iscsiadm -m session");
		
		List<Map<String, String>> targets = new ArrayList<Map<String, String>>();
		StringTokenizer _st = new StringTokenizer(_output, "\n");
		while(_st.hasMoreTokens()) {
			String _line = _st.nextToken();
			if(_line.contains("no active sessions")) {
				break;
			}
			try {
				Map<String, String> target = new HashMap<String, String>();
				String[] _value = new String[] {};
				if(_line.contains(" ")) {
					_value = _line.split(" ");
				}
				_line = _value[_value.length - 2];
				target.put("port", _line.substring(_line.indexOf(":")+1, _line.indexOf(",")));
				_line = _line.substring(0, _line.indexOf(":"));
				target.put("iqn", _value[_value.length - 1]);
				target.put("address", _line);
				targets.add(target);
			} catch(Exception _ex) {}
		}
		return targets;
	}
	
	/**
	 * Elimina un target de sended_targets para que no se busque en el startup de la máquina
	 * @param port
	 * @param address
	 * @throws Exception
	 */
	public static void removeOldExternalTarget(String address, String port) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/etc/iscsi/send_targets/");
		_sb.append(address);
		_sb.append(",");
		_sb.append(port);
		File _f = new File(_sb.toString());
		if(_f.exists()) {
			FileSystem.delete(_f);
		}
	}
	
	/**
	 * Obtiene el listado de targets old (se pueden borrar) a partir de los 'send_targets' y los 'session_targets'
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> getOldTargets() throws Exception {
		List<Map<String, String>> _sendTargets = getSendTargets();
		List<Map<String, String>> _externalSessionTargets = getExternalSessionTargets();
		List<Map<String, String>> _oldTargets = new ArrayList<Map<String, String>>();
		for(Map<String, String> _sendTarget : _sendTargets) {
			boolean _isOk = false;
			for(Map<String, String> _externalTarget : _externalSessionTargets) {
				if(_isOk) {
					break;
				} else if(_externalTarget.get("address").equals(_sendTarget.get("address")) && 
						_externalTarget.get("port").equals(_sendTarget.get("port"))) {
					_isOk = true;
				}
			}
			if(!_isOk) {
				_oldTargets.add(_sendTarget);
			}
		}
		return _oldTargets;
	}
	
	/**
	 * Obtiene el listado de targets iscsi para el que se intentará conexión en cada startup
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> getSendTargets() throws Exception {
		List<Map<String, String>> targets = new ArrayList<Map<String, String>>();
		File _dir = new File("/etc/iscsi/send_targets");
		if(_dir.exists()) {
			for(File _f : _dir.listFiles()) {
				if(!_f.isDirectory()) {
					continue;
				}
				Map<String, String> target = new HashMap<String, String>();
				if(_f.getName().contains(",")) {
					try {
						String[] _value = _f.getName().split(",");
						target.put("port", _value[_value.length - 1]);
						target.put("address", _value[0]);
						targets.add(target);
					} catch(Exception ex) {}
				}
			}
		}
		return targets;
	}
	
	private static String getRandomChars(int number) {
		char[] _characters = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		StringBuilder _chars = new StringBuilder();
		Random _r = new Random();
		for(int i = number; --i >= 0; ) {
			_chars.append(_characters[(int) _r.nextInt(36)]);
		}
		return _chars.toString();
	}
	
	public static String getRandomIQN() {
		StringBuilder _base = new StringBuilder("iqn.2003-12.com.wbsgo.imagine.wbsairback:");
		_base.append(getRandomChars(15));
		return _base.toString();
	}
	
	public static List<Map<String, String>> getTapeTargets() throws Exception {
		List<Map<String, String>> _targets = new ArrayList<Map<String,String>>();
		Map<String, Map<String, String>> _devices = new HashMap<String, Map<String,String>>();
		for(Map<String, String> _device : TapeManager.getTapeDevices()) {
			if(_device.get("scsi") != null) {
				_devices.put(_device.get("scsi"), _device);
			}
		}
		for(Map<String, String> _target : readTargets()) {
			if((_target.get("type") == null || "tape".equalsIgnoreCase(_target.get("type"))) && _target.get("scsi") != null) {
				if(_devices.containsKey(_target.get("scsi"))) {
					Map<String, String> _device = _devices.get(_target.get("scsi"));
					_target.put("vendor", _device.get("vendor"));
					_target.put("model", _device.get("model"));
					_target.put("serial", _device.get("serial"));
				} else {
					_target.put("vendor", "Unknown");
					_target.put("model", "Unknown");
					_target.put("serial", "SCSI " + _target.get("scsi"));
				}
				_targets.add(_target);
			}
		}
		return _targets;
	}
	
	public static Map<String, String> getVolumeTarget(String group, String name) throws Exception {
		if(group == null || name == null) {
			throw new Exception("invalid logical volume");
		}
		for(Map<String, String> _target : getVolumeTargets()) {
			if(group.equals(_target.get("vg")) && name.equals(_target.get("lv"))) {
				return _target;
			}
		}
		throw new Exception("target does not exists");
	}
	
	public static List<Map<String, String>> getVolumeTargets() throws Exception {
		List<Map<String, String>> _targets = new ArrayList<Map<String,String>>();
		for(Map<String, String> _target : readTargets()) {
			if((_target.get("type") == null || "volume".equalsIgnoreCase(_target.get("type"))) && (_target.get("vg") != null && _target.get("lv") != null)) {
				if(_target.get("iqn").contains(":snapshot")) {
					continue;
				}
				_targets.add(_target);
			}
		}
		return _targets;
	}
	
	public static Map<String, String> getSnapshotTarget(String group, String volume, String snapshot) throws Exception {
		if(group == null || volume == null || snapshot == null) {
			throw new Exception("invalid logical volume snapshot");
		}
		for(Map<String, String> _target : readTargets()) {
			if((_target.get("type") == null || "volume".equalsIgnoreCase(_target.get("type"))) && (_target.get("vg") != null && _target.get("lv") != null)) {
				if(_target.get("iqn").contains(":snapshot") && group.equals(_target.get("vg")) && snapshot.equals(_target.get("lv"))) {
					return _target;
				}
			}
		}
		throw new Exception("target does not exists");
	}
	
	private void initialize() {
		File _f = new File("/etc/iscsi/iscsid.conf");
		if(!isFormatted(_f)) {
			StringBuilder _sb = new StringBuilder();
			_sb.append("#WBSAirback iSCSI autoconfiguration\n");
			_sb.append("node.startup = automatic\n");
			_sb.append("node.session.timeo.replacement_timeout = 120\n");
			_sb.append("node.conn[0].timeo.login_timeout = 15\n");
			_sb.append("node.conn[0].timeo.logout_timeout = 15\n");
			_sb.append("node.conn[0].timeo.noop_out_interval = 5\n");
			_sb.append("node.conn[0].timeo.noop_out_timeout = 5\n");
			_sb.append("node.session.initial_login_retry_max = 8\n");
			_sb.append("node.session.cmds_max = 128\n");
			_sb.append("node.session.queue_depth = 32\n");
			_sb.append("node.session.iscsi.InitialR2T = No\n");
			_sb.append("node.session.iscsi.ImmediateData = Yes\n");
			_sb.append("node.session.iscsi.FirstBurstLength = 262144\n");
			_sb.append("node.session.iscsi.MaxBurstLength = 16776192\n");
			_sb.append("node.conn[0].iscsi.MaxRecvDataSegmentLength = 131072\n");
			_sb.append("discovery.sendtargets.iscsi.MaxRecvDataSegmentLength = 32768\n");
			_sb.append("node.session.iscsi.FastAbort = Yes\n");
			
			try {
				FileSystem.writeFile(_f, _sb.toString());
			} catch(Exception _ex) {}
		}
		
		_f = new File("/etc/iscsi/initiatorname.iscsi");
		if(!isFormatted(_f)) {
			try {
				setClientInitiatorName(getRandomIQN());
			} catch(Exception _ex) {}
		}
	}
	
	private boolean isFormatted(File _f) {
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				try {
					String _line = null;
					if((_line = _br.readLine()) != null) {
						return _line.startsWith("#WBSAirback ");
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {}
		}
		return false;
	}
	
	public static boolean isVolumeTarget(String group, String name) {
		if(group == null || name == null) {
			return false;
		}
		for(Map<String, String> _target : readTargets()) {
			if(group.equals(_target.get("vg")) && name.equals(_target.get("lv"))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isVolumeSnapshotTarget(String group, String volume, String snapshot) {
		if(group == null || volume == null) {
			return false;
		}
		for(Map<String, String> _target : readTargets()) {
			if(_target.get("iqn").contains(":snapshot") && group.equals(_target.get("vg")) && snapshot.equals(_target.get("lv"))) {
				return true;
			}
		}
		return false;
	}
	
	public static void loginExternalTarget(int[] address, String target, String method, String user, String password) throws Exception {
		if(address == null || address.length != 4) {
			throw new Exception("invalid iSCSI address");
		}
		
		if(target == null || target.isEmpty()) {
			throw new Exception("invalid iSCSI target");
		}
		
		StringBuilder _address = new StringBuilder();
		NetworkManager _nm = new NetworkManager(new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration())));
		for(int _o : address) {
			if(_o < 0 || _o > 255) {
				throw new Exception("invalid iSCSI address");
			}
			if(_address.length() > 0) {
				_address.append(".");
			}
			_address.append(_o);
		}
		if(_nm.isConfiguredAddress(NetworkManager.toAddress(_address.toString()))) {
			throw new Exception("network address [" + _address.toString() +  "] is a local address");
		}
		
		if(user != null && password != null && !user.isEmpty() && !password.isEmpty()) {
			if(method == null || method.isEmpty()) {
				method = "CHAP";
			}
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("/usr/bin/iscsiadm -m node --targetname \"");
			_sb.append(target);
			_sb.append("\" --portal \"");
			_sb.append(_address.toString());
			_sb.append(":3260\" --op=update --name node.session.auth.authmethod --value=");
			_sb.append(method);
			Command.systemCommand(_sb.toString());
			
			_sb = new StringBuilder();
			_sb.append("/usr/bin/iscsiadm -m node --targetname \"");
			_sb.append(target);
			_sb.append("\" --portal \"");
			_sb.append(_address.toString());
			_sb.append(":3260\" --op=update --name node.session.auth.username --value=");
			_sb.append(user);
			Command.systemCommand(_sb.toString());
			
			_sb = new StringBuilder();
			_sb.append("/usr/bin/iscsiadm -m node --targetname \"");
			_sb.append(target);
			_sb.append("\" --portal \"");
			_sb.append(_address.toString());
			_sb.append(":3260\" --op=update --name node.session.auth.password --value=");
			_sb.append(password);
			Command.systemCommand(_sb.toString());
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/iscsiadm -m node --targetname \"");
		_sb.append(target);
		_sb.append("\" --portal \"");
		_sb.append(_address.toString());
		_sb.append(":3260\" --login");
		Command.systemCommand(_sb.toString());
	}
	
	public static void logoutExternalTarget(int[] address, String target) throws Exception {
		if(address == null || address.length != 4) {
			throw new Exception("invalid iSCSI address");
		}
		
		if(target == null || target.isEmpty()) {
			throw new Exception("invalid iSCSI address");
		}
		
		StringBuilder _address = new StringBuilder();
		for(int _o : address) {
			if(_o < 0 || _o > 255) {
				throw new Exception("invalid iSCSI address");
			}
			if(_address.length() > 0) {
				_address.append(".");
			}
			_address.append(_o);
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/iscsiadm -m node --targetname \"");
		_sb.append(target);
		_sb.append("\" --portal \"");
		_sb.append(_address.toString());
		_sb.append(":3260\" --logout");
		Command.systemCommand(_sb.toString());
	}
	
	public static void removeTarget(String iqn) throws Exception {
		List<Map<String, String>> _valid_targets = new ArrayList<Map<String,String>>(); 
		List<Map<String, String>> _targets = getAllTargets();
		for(Map<String, String> _target : _targets) {
			if(_target.get("iqn").contains(iqn)) {
				continue;
			}
			_valid_targets.add(_target);
		}
		writeTargets(_valid_targets);
	}
	
	public void setClientInitiatorName(String name) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("#WBSAirback iSCSI autoconfiguration\n");
		_sb.append("InitiatorName=");
		_sb.append(name);
		
		FileSystem.writeFile(new File("/etc/iscsi/initiatorname.iscsi"), _sb.toString());
		ServiceManager.restart(ServiceManager.ISCSI_INITIATOR);
	}
	
	public static List<String> searchExternalTargets(int[] address) throws Exception {
		if(address == null || address.length != 4) {
			throw new Exception("invalid iSCSI address");
		}
		
		StringBuilder _command = new StringBuilder();
		NetworkManager _nm = new NetworkManager(new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration())));
		for(int _o : address) {
			if(_o < 0 || _o > 255) {
				throw new Exception("invalid iSCSI address");
			}
			if(_command.length() > 0) {
				_command.append(".");
			}
			_command.append(_o);
		}
		String _address = _command.toString();
		if(_nm.isConfiguredAddress(NetworkManager.toAddress(_address.toString()))) {
			throw new Exception("network address [" + _address.toString() +  "] is a local address");
		}
		_command.insert(0, "/usr/bin/iscsiadm -m discovery -t sendtargets -p ");
		_command.append(" -o delete -o new");
		
		String _output = Command.systemCommand(_command.toString());
		
		List<String> targets = new ArrayList<String>();
		StringTokenizer _st = new StringTokenizer(_output, "\n");
		while(_st.hasMoreTokens()) {
			String _line = _st.nextToken();
			if(_line.contains(" ") && _line.contains(_address)) {
				_line = _line.substring(_line.lastIndexOf(" ") + 1);
			}
			targets.add(_line);
		}
		return targets;
	}
	
	private static List<Map<String, String>> readTargets() {
		List<Map<String, String>> _valid_targets = new ArrayList<Map<String,String>>();
		File _f = new File(WBSAirbackConfiguration.getFileIscsiTargets());
		if(_f.exists()) {
			try {
				Map<String, Map<String, String>> _targets = new HashMap<String, Map<String,String>>();
				Map<String, Map<String, String>> _devices = new HashMap<String, Map<String, String>>();
				List<String> _scsi_tapes = new ArrayList<String>();
				List<String> _scsi_changers = new ArrayList<String>();
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(_line.trim().startsWith("TARGET ")) {
							Map<String, String> _target = new HashMap<String, String>();
							_target.put("iqn", _line.substring(_line.indexOf(" ") + 1, _line.indexOf(" {")));
							for(_line = _br.readLine(); _line != null && !_line.contains("}"); _line = _br.readLine()) {
								if(_line.trim().isEmpty()) {
									break;	
								} else if(_line.contains("IncomingUser ")) {
									String[] _user = _line.split(" ");
									if(_user.length >= 2) {
										_target.put("user", _user[1].replace("\"", ""));
										_target.put("password", _user[2].replace("\"", ""));
									}
								} else if(_line.contains("LUN ")) {
									String[] _lun = _line.split(" ");
									if(_lun.length >= 2) {
										if(_lun[2].contains("disk")) {
											_target.put("disk", _lun[2]);
										} else {
											_target.put("scsi", _lun[2]);
										}
									}
								}
							}
							_targets.put(_target.get("iqn"), _target);
						} else if(_line.trim().startsWith("DEVICE ")) {
							Map<String, String> _device = new HashMap<String, String>();
							_device.put("name", _line.substring(_line.indexOf(" ") + 1, _line.indexOf(" {")));
							for(_line = _br.readLine(); _line != null && !_line.contains("}"); _line = _br.readLine()) {
								if(_line.trim().isEmpty()) {
									break;	
								} else if(_line.contains("filename ")) {
									String[] _path = _line.split(" ");
									if(_path.length >= 1) {
										_path = _path[1].split("/");
										if(_path.length >= 2) {
											_device.put("vg", _path[_path.length - 2]);
											_device.put("lv", _path[_path.length - 1]);
										}
									}
								}
							}
							_devices.put(_device.get("name"), _device);
						} else if(_line.trim().startsWith("HANDLER dev_tape")) {
							for(_line = _br.readLine(); _line != null && !_line.contains("}"); _line = _br.readLine()) {
								_scsi_tapes.add(_line.trim().substring(_line.indexOf("DEVICE ") + 6));
							}
						} else if(_line.trim().startsWith("HANDLER dev_changer")) {
							for(_line = _br.readLine(); _line != null && !_line.contains("}"); _line = _br.readLine()) {
								_scsi_changers.add(_line.trim().substring(_line.indexOf("DEVICE ") + 6));
							}
						}	
					}
					for(Map<String, String> _target : _targets.values()) {
						if(_target.get("disk") != null) {
							if(_devices.containsKey(_target.get("disk"))) {
								Map<String, String> _device = _devices.get(_target.get("disk"));
								if(_device.containsKey("vg") && _device.containsKey("lv")) {
									_target.put("type", "volume");
									_target.put("vg", _device.get("vg"));
									_target.put("lv", _device.get("lv"));
									_valid_targets.add(_target);
								}
							}
						} else if(_target.get("scsi") != null) {
							if(_scsi_tapes.contains(_target.get("scsi"))) {
								_target.put("type", "tape");
								_valid_targets.add(_target);
							} else if(_scsi_changers.contains(_target.get("scsi"))) {
								_target.put("type", "autochanger");
								_valid_targets.add(_target);
							}
						}
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {
				System.out.println("iSCSIManager::configuration parse error: " + _ex.toString());
            }
		}
		return _valid_targets;
	}
	
	public static void removeAllTargets() throws Exception {
		List<Map<String, String>> targets = new ArrayList<Map<String, String>>();
		ServiceManager.fullStop(ServiceManager.ISCSI_TARGET);
		writeTargets(targets);
	}
	
	private static void writeTargets(List<Map<String, String>> targets) throws Exception {
		int disk = 0;
		StringBuilder _vdisk_handlers = new StringBuilder();
		StringBuilder _tape_handlers = new StringBuilder();
		StringBuilder _changer_handlers = new StringBuilder();
		StringBuilder _iscsi_targets = new StringBuilder();
		_vdisk_handlers.append("#WBSAirback SCST target configuration\n#\n\n");
		_vdisk_handlers.append("HANDLER vdisk_fileio {\n");
		_tape_handlers.append("HANDLER dev_tape {\n");
		_changer_handlers.append("HANDLER dev_changer {\n");
		_iscsi_targets.append("TARGET_DRIVER iscsi {\n");
		if(targets.isEmpty()) {
			_iscsi_targets.append("\tenabled 0\n");
		} else {
			_iscsi_targets.append("\tenabled 1\n");
			for(Map<String, String> _target : targets) {
				if(_target.get("iqn") == null || _target.get("iqn").isEmpty()) {
					continue;
				}
				if(_target.get("type") == null || "volume".equalsIgnoreCase(_target.get("type"))) {
					if(_target.get("vg") == null || _target.get("lv") == null) {
						continue;
					}
					if(_target.get("path") == null) {
						_target.put("path", VolumeManager.getLogicalVolumeDevicePath(_target.get("vg"), _target.get("lv")));
					}
					_vdisk_handlers.append("\tDEVICE disk");
					_vdisk_handlers.append(disk);
					_vdisk_handlers.append(" {\n");
					_vdisk_handlers.append("\t\tfilename ");
					_vdisk_handlers.append(_target.get("path"));
					_vdisk_handlers.append("\n");
					_vdisk_handlers.append("\t}\n");
				} else if("tape".equalsIgnoreCase(_target.get("type"))) {
					if(_target.get("scsi") == null || _target.get("scsi").isEmpty()) {
						continue;
					}
					_tape_handlers.append("\tDEVICE ");
					_tape_handlers.append(_target.get("scsi"));
					_tape_handlers.append("\n");
				} else if("autochanger".equalsIgnoreCase(_target.get("type"))) {
					if(_target.get("scsi") == null || _target.get("scsi").isEmpty()) {
						continue;
					}
					_changer_handlers.append("\tDEVICE ");
					_changer_handlers.append(_target.get("scsi"));
					_changer_handlers.append("\n");
				}
				
				_iscsi_targets.append("\tTARGET ");
				_iscsi_targets.append(_target.get("iqn"));
				_iscsi_targets.append(" {\n");
				if(_target.get("user") != null && _target.get("password") != null) {
					if(_target.get("password").length() < 12) {
						throw new Exception("invalid password lenght for target (>12 characters)");
					}
					_iscsi_targets.append("\t\tIncomingUser \"");
					_iscsi_targets.append(_target.get("user"));
					_iscsi_targets.append(" ");
					_iscsi_targets.append(_target.get("password"));
					_iscsi_targets.append("\"\n");
				}
				if(_target.get("type") == null || "volume".equalsIgnoreCase(_target.get("type"))) {
					_iscsi_targets.append("\t\tLUN 0 disk");
					_iscsi_targets.append(disk);
					disk++;
				} else if("tape".equalsIgnoreCase(_target.get("type")) || "autochanger".equalsIgnoreCase(_target.get("type"))) {
					_iscsi_targets.append("\t\tInitialR2T No\n");
					_iscsi_targets.append("\t\tImmediateData Yes\n");
					_iscsi_targets.append("\t\tFirstBurstLength 131072\n");
					_iscsi_targets.append("\t\tMaxRecvDataSegmentLength 131072\n");
					_iscsi_targets.append("\t\tLUN 0 ");
					_iscsi_targets.append(_target.get("scsi"));
				}
				_iscsi_targets.append("\n\t\tenabled 1\n");
				_iscsi_targets.append("\t}\n");
			}
		}
		_vdisk_handlers.append("}\n\n");
		_tape_handlers.append("}\n\n");
		_changer_handlers.append("}\n\n");
		_iscsi_targets.append("}\n");
		
		_iscsi_targets.insert(0, _changer_handlers.toString());
		_iscsi_targets.insert(0, _tape_handlers.toString());
		_iscsi_targets.insert(0, _vdisk_handlers.toString());
		File _f = new File(WBSAirbackConfiguration.getFileIscsiTargets());
		FileSystem.writeFile(_f, _iscsi_targets.toString());
		ServiceManager.restart(ServiceManager.ISCSI_TARGET);
	}
}
