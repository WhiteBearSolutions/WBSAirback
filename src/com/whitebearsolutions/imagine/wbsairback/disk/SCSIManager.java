package com.whitebearsolutions.imagine.wbsairback.disk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.RemoteStorageManager;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.util.Command;

public class SCSIManager {
	public final static int DISK = 1;
	public final static int CDROM = 2;
	public final static int TAPE = 3;
	public final static int CHANGER = 4;
	public final static int TRANSPORT_SCSI = 1;
	public final static int TRANSPORT_iSCSI = 2;
	private final static Logger logger = LoggerFactory.getLogger(SCSIManager.class);
	private final static File DETECT_NEW_SCSI_DEVICE_BLOCK;
	private final static long DETECT_NEW_SCSI_DEVICE_TIMEOUT = 300L;
	public final static String threadScsiRescanName = "wbsairback-scsi-rescan-thread";
	public final static Integer secondsToKillScsiRescan = 30;
	
	static {
		DETECT_NEW_SCSI_DEVICE_BLOCK = new File("/tmp/SCSI_DEVICE_DETECTION.lock");
	}
	
	public static Map<String, String> getDevice(String device) throws Exception {
		Map<String, String> _device = new HashMap<String, String>();
		if(device == null || device.isEmpty()) {
			return _device;
		}
		File _f = new File("/proc/scsi/scsi");
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.trim().startsWith("Host: ")) {
					StringBuilder _scsi = new StringBuilder();
					try {
						StringTokenizer _st = new StringTokenizer(_line.trim(), ":");
						while(_st.hasMoreTokens()) {
							String _token = _st.nextToken().trim();
							if(_token.contains(" ")) {
								_token = _token.substring(0, _token.indexOf(" "));
							}
							if(_token.startsWith("scsi")) {
								int _number = Integer.parseInt(_token.substring(4));
								_scsi.append(_number);
							} else if(_token.matches("[0-9]+")) {
								int _number = Integer.parseInt(_token);
								_scsi.append(":");
								_scsi.append(_number);
							}
						}
					} catch(NumberFormatException _ex) {
						_scsi = new StringBuilder();
					}
					_device.put("scsi", _scsi.toString());
					for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
						_line = _line.trim();
						if(_line.startsWith("Vendor: ")) {
							_device.put("vendor", _line.substring(8, _line.indexOf("Model: ") - 1).trim());
							if(_line.contains("Rev:")) {
	                            _device.put("model", _line.substring(_line.indexOf("Model: ") + 7, _line.indexOf("Rev:") - 1).trim());
                            } else {
                                _device.put("model", _line.substring(_line.indexOf("Model: ") + 7, _line.length()));
                            }
						} else if(_line.startsWith("Type:")) {
							if(_line.contains("Direct-Access")) {
								_device.put("type", "disk");
							} else if(_line.contains("CD-ROM")) {
								_device.put("type", "cdrom");
							} else if(_line.contains("Sequential-Access")) {
								_device.put("type", "tape");
							} else if(_line.contains("Medium Changer")) {
								_device.put("type", "autochanger");
							} 
							break;
						}
					}
					if(_device.get("type") != null && ("disk".equalsIgnoreCase(_device.get("type")) ||
							"cdrom".equalsIgnoreCase(_device.get("type")) ||
							"tape".equalsIgnoreCase(_device.get("type")))) {
						String _device_path = getDevice(_scsi.toString(), false);
						if(device.equalsIgnoreCase(_device_path)) {
							_device.put("device", _device_path);
							return _device;
						}
					} else if(_device.get("type") != null && "autochanger".equalsIgnoreCase(_device.get("type"))) {
						String _device_path = getDevice(_scsi.toString(), true);
						if(device.equalsIgnoreCase(_device_path)) {
							_device.put("device", _device_path);
							return _device;
						}
					}
				}
			}
		} finally {
			_br.close();
		}
		_device.put("vendor", "Unknown");
		_device.put("model", "Unknown");
		return _device;
	}
	
	public static List<Map<String, String>> getDevices(int type) throws Exception {
		List<Map<String, String>> _devices = new ArrayList<Map<String,String>>();
		if(type != DISK && type != CDROM && type != TAPE && type != CHANGER) {
			throw new Exception("invalid scsi type");
		}
		
		File _f = new File("/proc/scsi/scsi");
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.trim().startsWith("Host: ")) {
					StringBuilder _scsi = new StringBuilder();
					Map<String, String> _device = new HashMap<String, String>();
					try {
						StringTokenizer _st = new StringTokenizer(_line.trim(), ":");
						while(_st.hasMoreTokens()) {
							String _token = _st.nextToken().trim();
							if(_token.contains(" ")) {
								_token = _token.substring(0, _token.indexOf(" "));
							}
							if(_token.startsWith("scsi")) {
								int _number = Integer.parseInt(_token.substring(4));
								_scsi.append(_number);
							} else if(_token.matches("[0-9]+")) {
								int _number = Integer.parseInt(_token);
								_scsi.append(":");
								_scsi.append(_number);
							}
						}
					} catch(NumberFormatException _ex) {
						_scsi = new StringBuilder();
					}
					_device.put("scsi", _scsi.toString());
					for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
						_line = _line.trim();
						if(_line.startsWith("Vendor: ")) {
							_device.put("vendor", _line.substring(8, _line.indexOf("Model: ") - 1).trim());
							_device.put("model", _line.substring(_line.indexOf("Model: ") + 7, _line.indexOf("Rev: ") - 1).trim());
						} else if(_line.startsWith("Type:")) {
							if(_line.contains("Direct-Access")) {
								_device.put("type", "disk");
							} else if(_line.contains("CD-ROM")) {
								_device.put("type", "cdrom");
							} else if(_line.contains("Sequential-Access")) {
								_device.put("type", "tape");
							} else if(_line.contains("Medium Changer")) {
								_device.put("type", "autochanger");
							} 
							break;
						}
					}
					try {
						switch(type) {
							case DISK: {
									if(_device.get("type") != null && "disk".equalsIgnoreCase(_device.get("type"))) {
										_device.put("device", getDevice(_scsi.toString(), false));
										_devices.add(_device);
									}
								}
								break;
							case CDROM: {
									if(_device.get("type") != null && "cdrom".equalsIgnoreCase(_device.get("type"))) {
										_device.put("device", getDevice(_scsi.toString(), false));
										_devices.add(_device);
									}
								}
								break;
							case TAPE: {
									if(_device.get("type") != null && "tape".equalsIgnoreCase(_device.get("type"))) {
										_device.put("device", getDevice(_scsi.toString(), false));
										if (_device.get("device").indexOf("/dev/") > -1) { 
											String nDev = "/dev/" + "n" + _device.get("device").substring("/dev/".length());
											_device.put("devicen", nDev);
										}
										_devices.add(_device);
									}
								}
								break;
							case CHANGER: {
									if(_device.get("type") != null && "autochanger".equalsIgnoreCase(_device.get("type"))) {
										_device.put("device", getDevice(_scsi.toString(), true));
										_devices.add(_device);
									}
								}
								break;
						}
					} catch(Exception _ex) {
						logger.error("Error obteniendo discos de tipo {}. Ex: {}", type, _ex);
					}
				}
			}
		} finally {
			_br.close();
		}
		return _devices;
	}
	
	private static String getDevice(String scsi, boolean generic) {
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("lsscsi -g ");
			_sb.append(scsi);
			String line = Command.systemCommand(_sb.toString());
			int _offset = -1;
			if(generic) {
				_offset = line.lastIndexOf("/dev/");
			} else {
				_offset = line.indexOf("/dev/");
			}
			if(_offset == -1) {
				return null;
			}
			if(line.indexOf(" ", _offset + 1) != -1) {
				return line.substring(_offset, line.indexOf(" ", _offset + 1));
			} else {
				return line.substring(_offset, line.length()).trim();
			}
		} catch(Exception _ex) {
			logger.error("Error obteniendo datos de disco scsi (lsscsi -g) para {}. Ex: {}", scsi, _ex);
			return null;
		}
	}
	
	public static String getNewAttachedDevice(int transport, Map<String, String> transport_attributes, int seconds_to_wait) throws Exception {
		if(transport != TRANSPORT_SCSI && transport != TRANSPORT_iSCSI) {
			throw new IOException("invalid transport");
		}
		
		switch(transport) {
			case TRANSPORT_SCSI:
				// none
				break;
			case TRANSPORT_iSCSI:
				if(transport_attributes == null) {
					throw new IOException("invalid transport attributes");
				}
				if(transport_attributes.get("address") == null ||
						transport_attributes.get("target") == null ||
						transport_attributes.get("address").isEmpty() ||
						transport_attributes.get("target").isEmpty()) {
					throw new IOException("invalid transport attributes");
				}
				if(!NetworkManager.isValidAddress(transport_attributes.get("address"))) {
					throw new IOException("invalid transport attributes");
				}
				break;
		}
		
		try {
			if(!isDeviceDetectionLocked()) {
				lockDeviceDetection();
			}
			
			if(seconds_to_wait > 0) {
				try {
					Thread.sleep(seconds_to_wait * 1000L);
				} catch(InterruptedException _ex) {}
			}
			
			switch(transport) {
				case TRANSPORT_SCSI:
					commandRescan();
					return findDeviceOfWWN(transport_attributes.get("target"));
				default: //TRANSPORT_iSCSI
					int i = 0;
					int[] _address = new int[4];
					for(String _octet : NetworkManager.toAddress(transport_attributes.get("address"))) {
						_address[i] = Integer.parseInt(_octet);
						i++;
					}
					
					try {
						ISCSIManager.searchExternalTargets(_address);
						try {
							ISCSIManager.logoutExternalTarget(_address, transport_attributes.get("target"));
						} catch (Exception ex) {}
						ISCSIManager.loginExternalTarget(_address, transport_attributes.get("target"), transport_attributes.get("method"), transport_attributes.get("user"), transport_attributes.get("password"));
					} catch(Exception _ex) {
						throw new IOException(_ex.getMessage());
					}
					try {
						Thread.sleep(2 * 1000L);
					} catch(InterruptedException _ex) {}
					return getNewAttachedDevice();
			}
			
		} catch (Exception ex) {
			String msg = "Error getting new Attached device ["+transport+"] on ["+transport_attributes.get("address")+"]:{"+transport_attributes.get("target")+"}. Ex: "+ ex.getMessage();
			logger.error(msg);
			System.out.println(msg);
			throw new Exception(msg);
		} finally {
			unlockDeviceDetection();
		}
	}
	
	private static String getNewAttachedDevice() throws Exception {
		try {
			String _dmesg = Command.systemCommand("/bin/dmesg | /usr/bin/tail -20");
			BufferedReader _br = new BufferedReader(new StringReader(_dmesg));
			try {
				String _device = null;
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.matches(".* sd[a-z]:.*")) {
						if(_line.matches(".* sd[a-z]: sd[a-z].*")) {
							_device = _line.substring(_line.indexOf(": ") + 2).trim();
							if(_device.contains(" ")) {
								_device = _device.substring(0, _device.indexOf(" "));
							}
							if (new File("/dev/"+_device+"1").exists())
								_device = _device+"1";
							return _device;
						} else if (_device ==null){
							_device = _line.substring(_line.indexOf("]  ") + 2).trim();
							_device = _device.substring(0, _device.indexOf(": ")).trim();
						}
					}
				}
				if (new File("/dev/"+_device+"1").exists())
					_device = _device+"1";
				return _device;
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			String msg = "Error getting new Attached device. Ex:"+ _ex.getMessage();
			logger.error(msg);
			System.out.println(msg);
			throw new Exception(msg);
		}
	}
	
	public static boolean isDeviceDetectionLocked() {
		if(DETECT_NEW_SCSI_DEVICE_BLOCK.exists()) {
			return true;
		}
		return false;
	}
	
	public static void lockDeviceDetection() throws IOException {
		while(true) {
			if(DETECT_NEW_SCSI_DEVICE_BLOCK.exists()) {
				if(DETECT_NEW_SCSI_DEVICE_BLOCK.lastModified() < (DETECT_NEW_SCSI_DEVICE_TIMEOUT * 1000L)) {
					continue;
				}
				DETECT_NEW_SCSI_DEVICE_BLOCK.delete();
			}
			try {
				DETECT_NEW_SCSI_DEVICE_BLOCK.createNewFile();
				break;
			} catch(IOException _ex) {
				throw new IOException("cannot block SCSI device detection");
			}
		}
	}
	
	public static void commandRescan() {
		try {
			//Command.systemCommand("/bin/bash /usr/share/doc/sg3-utils/examples/archive/rescan-scsi-bus.sh");
			logger.info("Launching scsi bus rescan ...");
			Command.systemCommand("/usr/sbin/wbsairback-scsi-rescan");
			Command.systemCommand("udevadm trigger --action=change");
			logger.info("Scsi bus rescan finished successfully ...");
			try {
				Command.systemCommand("partprobe -s");
			} catch (Exception ex1) {}
		} catch (Exception ex) {
			logger.error("ERROR on /sbin/wbsairback-scsi-rescan: "+ex.getMessage());
		}
	}
	
	public static void rescanSCSIBus() {
		try {
			Runnable r = new Runnable() {
				public void run() {
					commandRescan();
				}
			};
			
			Runnable rMonitorThread = new Runnable() {
				public void run() {
					try {
						int secondsToKill = secondsToKillScsiRescan;
						while (secondsToKill > 0) {
							Thread.sleep(1000);
							secondsToKill--;
						}
						
						try {
							if (Command.isRunning("/bin/bash /usr/sbin/wbsairback-scsi-rescan")) {
								Command.systemCommand("killall wbsairback-scsi-rescan");
								logger.info("wbsairback-scsi-rescan killed successfully");
								
								Command.systemCommand("udevadm trigger --action=change");
								logger.info("udevadm trigger --action=change launched successfully");
							}
						} catch (Exception ex) {
							logger.error("Error killing wbsairback-scsi-rescan : "+ex.getMessage());
						}
						
					} catch (Exception ex) {
						logger.error("ERROR in monitoring of /sbin/wbsairback-scsi-rescan: "+ex.getMessage());
					}
				}
			};
						
			Thread internalThread = new Thread(r);
			internalThread.setName(threadScsiRescanName);
			internalThread.start();
			
			Thread internalThreadMonitor = new Thread(rMonitorThread);
			internalThreadMonitor.setName(threadScsiRescanName+"-Monitor");
			internalThreadMonitor.start();
		} catch (Exception ex) {
			logger.error("ERROR en el reescaneo scsi de /usr/sbin/wbsairback-scsi-rescan: "+ex.getMessage());
		}
	}
	
    public static void unlockDeviceDetection() {
    	if(DETECT_NEW_SCSI_DEVICE_BLOCK.exists()) {
    		DETECT_NEW_SCSI_DEVICE_BLOCK.delete();
    	}
	}
    
    public static void main(String [] args) {
    	Map<String, String> transport_attributes = new HashMap<String, String>();
    	if (args.length < 2 && !args[0].equals("block")) {
    		System.out.println("Incorrect call. Use: wbsairback-scsi-login address target [protocol] [method] [user] [password] || wbsairback-scsi-block");
    	}
    	
    	try {
    		if (args[0].equals("block")) {
    			lockDeviceDetection();
    			System.exit(0);
    		}
    		
    		int protocol = TRANSPORT_iSCSI;
    		
	    	transport_attributes.put("address", args[0]);
	    	transport_attributes.put("target", args[1]);
	    	if (args.length > 2) {
	    		String sProtocol = args[2];
	    		if (sProtocol.trim().equalsIgnoreCase(RemoteStorageManager.STORAGE_TYPE_FIBRE))
	    			protocol = TRANSPORT_SCSI;
	    	}
	    		
	    	if (args.length > 3)
	    		transport_attributes.put("method", args[3]);
	    	if (args.length > 4)
	    		transport_attributes.put("user", args[4]);
	    	if (args.length > 5)
	    		transport_attributes.put("password", args[5]);
    	
    		String device = getNewAttachedDevice(protocol, transport_attributes, 10);
    		System.out.println(device);
    		System.exit(0);
    	} catch (Exception ex) {
    		System.err.println("Could not obtain scsi device. Ex: "+ex.getMessage());
    		System.exit(2);
    	}
    }
    
    public static String findDeviceOfWWN(String wwn) {
    	try {
    		wwn = wwn.toLowerCase();
    		logger.info("Buscando disco asociado a wwn {} ...", wwn);
	    	StringBuilder _sb = new StringBuilder();
	    	_sb.append("ls /dev/sd*");
	    	String output = Command.systemCommand(_sb.toString());
	    	StringTokenizer st = new StringTokenizer(output);
	    	String device = null;
	    	String scsi_id = null;
	    	do {
	    		device = st.nextToken();
	    		if (device != null) {
	    			device = device.trim();
	    			if (!device.isEmpty()) {
	    				scsi_id = Command.systemCommand("/lib/udev/scsi_id -g "+device);
	    				if (scsi_id != null && !scsi_id.isEmpty() && scsi_id.toLowerCase().trim().contains(wwn)) {
	    					if (device.contains("dev/"))
	    						device = device.substring(device.indexOf("dev/")+"dev/".length());
	    					if (new File("/dev/"+device+"1").exists())
								device = device+"1";
	    					return device;
	    				}
	    			}
	    		}
	    	}
	    	while (device != null && !device.isEmpty());
	    	return "";
    	} catch (Exception ex) {
    		logger.error("Error buscando el disco asociado al wwn {}. Ex: {}", wwn, ex.getMessage());
    		return "";
    	}
    }
    
    /**
	 * Devuelve el primer wwn asociado a airback que encuentra
	 * @return
	 */
	public static String getClientFibreChannelWWN() {
		String wwn = "";
		List<String> listwwn = getWWNList();
		if (listwwn != null && !listwwn.isEmpty())
			wwn = listwwn.iterator().next();
		return wwn;
	}
	
	
	/**
	 * Obtiene el listado de wwn asociados a las tarjetas de fibra presentes en el sistema
	 * @return
	 */
	public static List<String> getWWNList() {
		try {
			List<String> list = new ArrayList<String>();
			String wwn = null;
			File f1 = new File("/sys/class/fc_host");
			if (f1.exists()) {
				String [] entrys = f1.list();
				if (entrys != null && entrys.length>0) {
					for (String e : entrys) {
						if (e.contains("host")) {
							String path = "/sys/class/fc_host/"+e+"/port_name";
							if (new File(path).exists() && new File(path).isFile()) {
								wwn = Command.systemCommand("cat "+path);
								if (wwn != null && !wwn.isEmpty()) {
									if (wwn.contains("x"))
										wwn = wwn.substring(wwn.indexOf("x")+1);
									list.add(wwn);
								}
							}
						}
					}
				}
			}
			return list;
		} catch (Exception ex) {
			return null;
		}
	}
}
