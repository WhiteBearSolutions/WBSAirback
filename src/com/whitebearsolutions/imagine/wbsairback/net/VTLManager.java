package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.io.FileLockException;
import com.whitebearsolutions.util.Command;

public class VTLManager {
	private static List<String> _scsi_tags = Arrays.asList(new String[] { "CHANNEL: ", "TARGET: ", "LUN: " });
	
	public VTLManager() throws Exception {
		initialize();
	}
	
	private void backupConfiguration() throws Exception{
		try {
			Command.systemCommand("mkdir /etc/mhvtl/Restore");
			Command.systemCommand("mv  /etc/mhvtl/device.conf /etc/mhvtl/library_contents* /etc/mhvtl/Restore/");
		} catch(Exception _ex) {
			if(_ex.getMessage().contains("El fichero ya existe")) {
				try {
					Command.systemCommand("mv  /etc/mhvtl/device.conf /etc/mhvtl/library_contents* /etc/mhvtl/Restore/");
				} catch(Exception ex) {}
			}
			_ex.printStackTrace();
		}		
	}
	
	public int getNextId(List<Map<String,Object>> libraries){
		int _val=0;
		int _aux=0;
		if(libraries==null || libraries.isEmpty()) {
			return 10;
		}
		for(Map<String,Object> _ele : libraries){
			if(_ele.get("id")!=null && !((String)_ele.get("id")).isEmpty()) {
				_aux=Integer.valueOf((String)_ele.get("id")).intValue();
				if(_val<_aux) {
					_val=_aux;
				}					
			}
		}
		return _val!=0 ? (_val%10!=0 ? (_val/10)+1*10 : _val+10) : 10;
	}
	
	public Map<String, Object> getLibrary(String id) {
		List<Map<String,Object>> _libraries = getLibraries();
		if(_libraries!=null && !_libraries.isEmpty()) {
			for (Map<String,Object> _lib: _libraries) {
				if(id.equals(_lib.get("id"))) {
					return _lib;
				}
			}	
		}
		return null;
	}
	
	public void deleteAllLibraries() throws Exception {
		List<Map<String,Object>> libraries = getLibraries();
		if (libraries != null && libraries.size()>0) {
			for (Map<String, Object> lib : libraries) {
				removeLibrary((String) lib.get("id"));
			}
		}
		
		try {
			Command.systemCommand("rm -f /rdata/mhvtl/E*");
		} catch (Exception ex){}
	}
	
	public List<Map<String,Object>> getLibraries() {
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		File _f = new File(WBSAirbackConfiguration.getFileMhvtlConfiguration());
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(!_line.trim().startsWith("#LIBRARY_START")) {						
						continue;
					} else {
						Map<String,Object> _library = new HashMap<String, Object>();
						_library.put("name", _line.trim().substring(15, _line.trim().length()));
						_line = _br.readLine();
						_library.put("density", _line.trim().substring(9, _line.trim().length()));
						_line = _br.readLine();
						_library.put("compression", _line.trim().substring(13, _line.trim().length()));
						_line = _br.readLine();
						_library.put("path", _line.trim().substring(6, _line.trim().length()));	
						_line = _br.readLine();
						_library.put("numTapes", _line.trim().substring(10, _line.trim().length()));
						_line = _br.readLine();
						Float _auxCapacity=Float.valueOf(_line.trim().substring(10, _line.trim().length()));
						if(_auxCapacity>10000) {
							_library.put("capacity",((Float)(_auxCapacity/1000)).toString());	
							_library.put("capacityMag","G");	
						} else {
							_library.put("capacity", _auxCapacity.toString());	
							_library.put("capacityMag","M");
						}
						_line = _br.readLine();
						_library.put("id", _line.trim().substring(9, _line.trim().indexOf(" ", 11)));
						StringBuilder _sb = new StringBuilder();
						for(String _tag : _scsi_tags) {
							int _index = _line.indexOf(_tag) + _tag.length() + 1;
							if(!_line.contains(_tag)) {
								continue;
							}
							if(_sb.length() > 0) {
								_sb.append(":");
							}
							if(_line.indexOf(" ", _index) != -1) {
								try {
									_sb.append(Integer.parseInt(_line.substring(_index, _line.indexOf(" ", _index))));
								} catch(NumberFormatException _ex) {}
							} else {
								try {
									_sb.append(Integer.parseInt(_line.substring(_index)));
								} catch(NumberFormatException _ex) {}
							}
						}
						_library.put("scsi", _sb.toString());
						List<Map<String, String>> drives = new ArrayList<Map<String, String>>();
						List<String> _library_drives = getLibraryDrives(String.valueOf(_library.get("id")));
						for(_line = _br.readLine(); _line != null && !_line.trim().startsWith("#LIBRARY_END"); _line = _br.readLine()) {
							if(_line.trim().startsWith("Unit serial number:")) {						
								_library.put("unitSerialNumber",_line.trim().substring(20, _line.trim().length()));
							}
							if(_line.trim().startsWith("Drive:")) {
								HashMap<String, String> drive = new HashMap<String, String>();
								_sb = new StringBuilder();
								for(String _tag : _scsi_tags) {
									int _index = _line.indexOf(_tag) + _tag.length() + 1;
									if(!_line.contains(_tag)) {
										continue;
									}
									if(_sb.length() > 0) {
										_sb.append(":");
									}
									if(_line.indexOf(" ", _index) != -1) {
										try {
											_sb.append(Integer.parseInt(_line.substring(_index, _line.indexOf(" ", _index))));
										} catch(NumberFormatException _ex) {}
									} else {
										try {
											_sb.append(Integer.parseInt(_line.substring(_index)));
										} catch(NumberFormatException _ex) {}
									}
								}
								drive.put("scsi", _sb.toString());
								for(_line = _br.readLine(); _line != null && !_line.isEmpty(); _line = _br.readLine()) {
									if(_line.trim().startsWith("Unit serial number:")) {	
										drive.put("unitSerialNumber",_line.trim().substring(20, _line.trim().length()));
									}
									if(_line!=null && _line.trim().startsWith("#LIBRARY_END")) {
										continue;
									}
								}
								if(_library_drives.contains(drive.get("unitSerialNumber"))) {
									drives.add(drive);
								}
							}
						}
						_library.put("slotTapes", getLibraryTapes(String.valueOf(_library.get("id")), String.valueOf(_library.get("path"))));
						_library.put("drives", drives);
						result.add(_library);
					}
				}
			} catch(Exception _ex) {
				System.out.print("VTLManager error: " + _ex.getMessage());
			}
		}
		return result;
	}
	
	public String getLibraryChangerDevice(String library) throws Exception{
		if(library != null && !library.isEmpty()) {
			String _out = Command.systemCommand("lsscsi -g |awk '/mediumx/ {print $1,$7}'");
			if(_out != null && ! _out.isEmpty()) {
				StringTokenizer _tok = new StringTokenizer(_out, "\n");
				while(_tok.hasMoreTokens()) {
					String[] _device = _tok.nextToken().split(" ");
					Map<String, Object> _library = getLibrary(library);
					if(_library != null && _library.get("scsi") != null) {
						if(_device[0].contains(String.valueOf(_library.get("scsi")))) {
							return _device[1];
						}
					}
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLibraryDeviceDrives(String library) throws Exception {
		Map<String, String> _all_devices = new HashMap<String, String>();
		List<String> _devices = new ArrayList<String>();
		if(library != null && !library.isEmpty()) {
			String _out = Command.systemCommand("lsscsi -g |awk '/mediumx/ {print $1,$7}'");
			if(_out != null && ! _out.isEmpty()) {
				StringTokenizer _tok = new StringTokenizer(_out, "\n");
				while(_tok.hasMoreTokens()) {
					String[] _device = _tok.nextToken().split(" ");
					_all_devices.put(_device[1], _device[0]);
				}
			} 
			System.out.println("All-devices: " + _all_devices);
			Map<String, Object> _library = getLibrary(library);
			for(Map<String, String> _drive :  (List<Map<String, String>>) _library.get("drives")) {
				System.out.println("Drive: " + _drive);
				if(_drive.get("scsi") != null) {
					Iterator<Entry<String, String>> it=_all_devices.entrySet().iterator();
					while(it.hasNext()) {
						Entry<String, String> ent=it.next();
						if(_drive.get("scsi") != null && ent.getValue().contains(String.valueOf(_drive.get("scsi")))) {
							_devices.add(_all_devices.get(ent.getKey()));
						}
					} 
				}
			}
		}
		return _devices;
	}
	
	private List<String> getLibraryDrives(String idLibrary){
		List<String> result = new ArrayList<String>();
		File _f = new File(WBSAirbackConfiguration.getDirectoryMhvtl() + "/library_contents." + idLibrary);
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(!_line.trim().matches("^Drive\\ [0-9]+:.*")) {						
							continue;
						} else {
							try {
								result.add( _line.substring(_line.indexOf(":") + 2, _line.length()).trim());
							} catch(Exception ex) {
								result.add("");
							}
						}	
					}
			} catch(Exception ex) {}
		}
		return result;
	}
	
	private List<String> getLibraryTapes(String idLibrary, String path){
		List<String> result= new ArrayList<String>();
		File _f = new File(WBSAirbackConfiguration.getDirectoryMhvtl() + "/library_contents." + idLibrary);
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(!_line.trim().startsWith("Slot ")) {						
							continue;
						} else {
							try {
								result.add( _line.substring(_line.indexOf(":") + 2, _line.length()).trim());
							} catch(Exception ex) {
								result.add("");
							}
						}	
					}
			} catch(Exception ex) {}
		}
		return result;
	}
	
	private static String getRandomString(int lenght){
		String cadenaAleatoria = "";
		long milis = new java.util.GregorianCalendar().getTimeInMillis();
		Random r = new Random(milis);
		int i = 0;
		while( i < lenght) {
			char c = (char)r.nextInt(255);
			if( (c >= '0' && c <='9') || (c >='A' && c <='Z') ) {
				cadenaAleatoria += c;
				i ++;
			}
		}
		return cadenaAleatoria;
	}
	
	public static String generateLabel(int len){
		return getRandomString(len);
	}
	
	private static void initialize() {
		File _f = new File("/etc/mhvtl/device.conf");
		if(!isFormatted(_f)) {
			StringBuilder _sb = new StringBuilder();
			_sb.append("\n");
			_sb.append("VERSION: 5\n");
			_sb.append("# VPD page format:\n");
			_sb.append("# <page #> <Length> <x> <x+1>... <x+n>\n");
			_sb.append("# NAA format is an 8 hex byte value seperated by ':'\n");
			_sb.append("# Note: NAA is part of inquiry VPD 0x83\n");
			_sb.append("#\n");
			_sb.append("# Each 'record' is separated by one (or more) blank lines.\n");
			_sb.append("# Each 'record' starts at column 1\n");
			_sb.append("# Serial num max len is 10.\n");
			_sb.append("# Compression: factor X enabled 0|1\n");
			_sb.append("#     Where X is zlib compression factor        1 = Fastest compression\n");
			_sb.append("#                                               9 = Best compression\n");
			_sb.append("#     enabled 0 == off, 1 == on\n");
			_sb.append("\n");
			try {
				FileSystem.writeFile(_f, _sb.toString());
			} catch(Exception _ex) {}
		}
	}
	
	private static boolean isFormatted(File _f) {
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				String _line = null;
				if((_line = _br.readLine()) != null && (_line = _br.readLine()) != null) {
					return _line.startsWith("VERSION: 5");
				}
			} catch(Exception _ex) {}
		}
		return false;
	}
	
	public static List<Map<String, Object>> orderLibraryList(List<Map<String, Object>> values) {
		@SuppressWarnings("unchecked")
		Map<String, Object>[] result = new HashMap[values.size()];
		Map<String, Map<String, Object>> _aux = new HashMap<String, Map<String,Object>>();
		int _indexResult=0;
		List<String> _indexOrder=new ArrayList<String>();
		for(int x = 0; x < values.size(); x++) {
			_indexOrder.add(String.valueOf(values.get(x).get("id")));
			_aux.put(String.valueOf(values.get(x).get("id")), values.get(x));
		}
		Collections.sort(_indexOrder);
		for(String _id: _indexOrder) {
			result[_indexResult] = _aux.get(_id);
			_indexResult++;
		} 		
		return Arrays.asList(result);
	} 
	
	@SuppressWarnings("unchecked")
	public int removeLibrary(String id) throws Exception{
		List<Map<String,Object>> _libraries = getLibraries();
		Map<String,Object> del = null;
		for(Map<String,Object> _aux: _libraries) {
			if (id.equals(_aux.get("id"))){
				del=_aux;
			}
		}
		if(del!=null) {
			_libraries.remove(del);
			if(writeLibraries(_libraries,false)!=-1 && del.get("path")!=null && del.get("slotTapes")!=null){
				for(String _tape: (List<String>) del.get("slotTapes")){
					removeTape(_tape, (String)del.get("path"));
				}
				File _f = new File(WBSAirbackConfiguration.getDirectoryMhvtl() + "/library_contents." + del.get("id"));
				if(_f.exists()) {
					_f.delete();
				}
				return 1;
			}
		}
		return -1;
	}
	
	public int removeTape(String _idTape,String _path) {
		if(_idTape!=null && !_idTape.isEmpty() && _path!=null && !_path.isEmpty()) {
			File _tapeFile = new File(WBSAirbackConfiguration.getDirectoryVolumeMount() + "/" + _path + "/" + _idTape);
			try {
				if(_tapeFile.exists() && Command.systemCommand("rm -r " + WBSAirbackConfiguration.getDirectoryVolumeMount() + "/" + _path + "/" + _idTape).isEmpty() && Command.systemCommand("rm -r "+WBSAirbackConfiguration.getDirectoryVolumeMount() + "/" + _idTape).isEmpty()) {
					Command.systemCommand("rm -f /rdata/mhvtl/"+_idTape);
					return 1;
				}
			} catch(Exception _ex) {
				_ex.printStackTrace();
			}
		}
		return -1;
	}
	
	private static void restoreConfiguration() {
		try {
			Command.systemCommand("rm /etc/mhvtl/device.conf /etc/mhvtl/library_contents*");			
		} catch(Exception _ex) {
			_ex.printStackTrace();
		}
		try {					
			Command.systemCommand("mv /etc/mhvtl/Restore/* /etc/mhvtl/");
		} catch(Exception _ex) {
			_ex.printStackTrace();
		}
		try {					
			Command.systemCommand("rm -r /etc/mhvtl/Restore");
		} catch(Exception _ex) {
			_ex.printStackTrace();
		}
	}
	
	private static void restoreService(Exception e) {
		e.printStackTrace();
		restoreConfiguration();
		try {
			ServiceManager.start(ServiceManager.VTL);
		} catch(Exception _ex) {
			_ex.printStackTrace();
		}
	}
	
	public int storeLibrary(Map<String,Object> library) throws Exception {
		Map<String,Object> del = null;
		boolean edit = false;
		List<Map<String,Object>> _libraries = getLibraries();
		for(Map<String,Object> _aux: _libraries) {
			if(library.get("id").equals(_aux.get("id"))) {
				del =_aux;
				edit = true;
			}
		}
		if(del != null) {
			_libraries.remove(del);
		}
		_libraries.add(library);
		return writeLibraries(_libraries, edit);
	}
	
	@SuppressWarnings("unchecked")
	public int writeLibraries(List<Map<String, Object>> values, boolean edit) throws Exception{
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		try {
			int channel=0;
			int target=0;
			int slot=1;
			DecimalFormat myFormatter = new DecimalFormat("00");
			
			backupConfiguration();
			ServiceManager.fullStop(ServiceManager.VTL);
			StringBuilder _sb = new StringBuilder();
			_sb.append("\n");
			_sb.append("VERSION: 5\n");
			_sb.append("# VPD page format:\n");
			_sb.append("# <page #> <Length> <x> <x+1>... <x+n>\n");
			_sb.append("# NAA format is an 8 hex byte value seperated by ':'\n");
			_sb.append("# Note: NAA is part of inquiry VPD 0x83\n");
			_sb.append("#\n");
			_sb.append("# Each 'record' is separated by one (or more) blank lines.\n");
			_sb.append("# Each 'record' starts at column 1\n");
			_sb.append("# Serial num max len is 10.\n");
			_sb.append("# Compression: factor X enabled 0|1\n");
			_sb.append("#     Where X is zlib compression factor \n");
			_sb.append("       1 = Fastest compression\n");
			_sb.append("#      9 = Best compression\n");
			_sb.append("#     enabled 0 == off, 1 == on\n");
			_sb.append("\n");

			for(Map<String, Object> _lib : orderLibraryList(values)) {
				slot=1;
				_sb.append("#LIBRARY_START "+_lib.get("name")+"\n");
				_sb.append("#DENSITY "+_lib.get("density")+"\n");
				_sb.append("#COMPRESSION "+_lib.get("compression")+"\n");
				_sb.append("#PATH "+_lib.get("path")+"\n");
				_sb.append("#NUMTAPES "+_lib.get("numTapes")+"\n");
				_sb.append("#CAPACITY "+_lib.get("capacity")+"\n");
				_sb.append("Library: ");
				_sb.append(_lib.get("id"));
				_sb.append(" CHANNEL: ");
				_sb.append(myFormatter.format(channel));
				_sb.append(" TARGET: ");
				_sb.append(myFormatter.format(target));
				_sb.append(" LUN: 00\n");
				
				int _driveCount = Integer.valueOf((String)_lib.get("id")).intValue();
				List<String> drives = new ArrayList<String>();
				_sb.append("Vendor identification: STK\n");
				_sb.append("Product identification: L700 \n");
				_sb.append("Product revision level: 550V\n");
				_sb.append("Unit serial number: ");
				_sb.append(_lib.get("unitSerialNumber"));
				_sb.append("\n");
				_sb.append("NAA: 10:22:33:44:ab:00:");
				_sb.append(channel);
				_sb.append(target);
				_sb.append(":00\n");
				_sb.append("\n");
				_driveCount++;
				for(Map<String, Object> drive: (List<Map<String,Object>>) _lib.get("drives")) {
					target++;
					drives.add((String)drive.get("unitSerialNumber"));
					_sb.append("Drive: " + (_driveCount++) + " CHANNEL: " + myFormatter.format(channel) + " TARGET: " + myFormatter.format(target) + " LUN: 00\n");
					_sb.append("Library ID: " + _lib.get("id") + " Slot: " + myFormatter.format(slot) + "\n");
					_sb.append("Vendor identification: IBM\n");
					_sb.append("Product identification: ULT3580-TD5 \n");
					_sb.append("Product revision level: 550V\n");
					_sb.append("Unit serial number: " + drive.get("unitSerialNumber") + "\n");
					_sb.append("NAA: 10:22:33:44:ab:00:" + channel + target + ":00\n");
					_sb.append("Compression: factor " + _lib.get("compression") + " enabled 1\n");
					_sb.append("READ_ONLY: LTO1\n");
					_sb.append("READ_ONLY: LTO2\n");
					_sb.append("READ_WRITE: LTO4\n");
					_sb.append("READ_WRITE: LTO5\n");
					_sb.append("WORM: LTO4\n");
					_sb.append("\n");
					slot++;
				}
				_sb.append("#LIBRARY_END\n");
				_sb.append("\n");
				_sb.append("\n");
				writeLibrariesTapes((String)_lib.get("id"),drives,(String)_lib.get("path"),(List<String>)_lib.get("slotTapes"),_lib.get("capacity")!=null ? ((String)_lib.get("capacity")) : null);
				if(edit && _lib.get("tapesToRemove")!=null && !((List<String>)_lib.get("tapesToRemove")).isEmpty()) {
					for(String _tape: (List<String>)_lib.get("tapesToRemove")) {
						removeTape(_tape, (String)_lib.get("path"));
					}
				}
				target++;
			}
		
			File _f = new File(WBSAirbackConfiguration.getFileMhvtlConfiguration());
			FileSystem.writeFile(_f, _sb.toString());
			Thread.sleep(500);
			ServiceManager.start(ServiceManager.VTL);
		} catch(FileNotFoundException e) {
			restoreService(e);
			return -1;
		} catch(FileLockException e) {
			restoreService(e);
			return -1;
		} catch (IOException e) {
			restoreService(e);
			return -1;
		} catch (Exception e) {
			restoreService(e);
			return -1;
		}
		return 1;
	}
	
	private int writeLibrariesTapes(String idLibrary,List<String> drives,String path,List<String> SlotsTapes,String capacity) throws Exception{
		int index = 1;
		StringBuilder _sb = new StringBuilder();
		_sb.append("\n");
		int driveCount=1;
		for (String drive: drives){
			_sb.append("Drive "+driveCount+": "+drive+"\n");
			driveCount++;
		}
		_sb.append("Picker 1:\n\n");
		_sb.append("MAP 1:\n\n");
		_sb.append("# Slot 1 - ?, no gaps\n");
		_sb.append("# Slot N: [barcode]\n");
		_sb.append("# [barcode]\n");
		_sb.append("# a barcode is comprised of three fields: [Leading] [identifier] [Trailing]\n");
		_sb.append("# Leading \"CLN\" -- cleaning tape\n");
		_sb.append("# Leading \"W\" -- WORM tape\n");
		_sb.append("# Leading \"NOBAR\" -- will appear to have no barcode\n");
		_sb.append("# If the barcode is at least 8 character long, then the last two characters are Trailing\n");
		_sb.append("# Trailing \"S3\" - SDLT600\n");
		_sb.append("# Trailing \"X4\" - AIT-4\n");
		_sb.append("# Trailing \"L1\" - LTO 1, \"L2\" - LTO 2, \"L3\" - LTO 3, \"L4\" - LTO 4, \"L5\" - LTO 5\n");
		_sb.append("# Training \"LT\" - LTO 3 WORM, \"LU\" LTO 4 WORM, \"LV\" LTO 5 WORM\n");
		_sb.append("# Trailing \"TA\" - T10000+\n");
		_sb.append("# Trailing \"JA\" - 3592+\n");
		_sb.append("# Trailing \"JB\" - 3592E05+\n");
		_sb.append("# Trailing \"JW\" - WORM 3592+\n");
		_sb.append("# Trailing \"JX\" - WORM 3592E05+\n");
		_sb.append("# WBS-DIR " + path + "\n");
		_sb.append("# CAPACITY " + (capacity!=null ? capacity : "") + "\n");
		_sb.append("#\n\n");
		for(String _tape: SlotsTapes) {
			if(_tape!=null && !_tape.isEmpty()) {
				_sb.append("Slot "+index+": "+_tape+"\n");
			} else {
				_sb.append("Slot "+index+": "+"\n");
			}
			index++;
		}
		File _f = new File(WBSAirbackConfiguration.getDirectoryMhvtl() + "/library_contents." + idLibrary);
		FileSystem.writeFile(_f, _sb.toString());
		return 1;
	}
}
