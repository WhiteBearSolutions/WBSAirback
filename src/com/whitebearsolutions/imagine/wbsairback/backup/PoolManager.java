package com.whitebearsolutions.imagine.wbsairback.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.db.DBException;
import com.whitebearsolutions.http.HTTPClient;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;
import com.whitebearsolutions.imagine.wbsairback.net.CommResponse;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class PoolManager {
	private Configuration _c;
	private DBConnection _db;
	
	private final static Logger logger = LoggerFactory.getLogger(PoolManager.class);
	
	public PoolManager(Configuration conf) throws DBException {
		this._c = conf;
		this._db = new DBConnectionManager(this._c).getConnection();
	}
	
	public void addAutochangerVolume(String pool, String label, String slot, String drive) throws Exception {
		int slotInt = -1;
		if(pool == null) {
			throw new Exception("invalid pool");
		}
		
		try {
			slotInt = Integer.parseInt(slot);
		} catch(NumberFormatException _ex) {
			throw new Exception("invalid volume slot");
		}
		
		Map<String, String> _data = getPool(pool);
		String _output = Command.systemCommand("/bin/echo \"label storage=" + _data.get("storage") + " volume=" + label +" slot=" + slotInt + " pool=" + pool + " drive="+drive+"\" | /usr/bin/bconsole");
	    if(_output.contains("Cannot label Volume because it is already labeled")) {
		    throw new Exception("tape is already labeled, you must reformat");
	    } else if (!(_output.contains("is mounted") || _output.contains("is already mounted")) || !_output.contains("OK label") || !_output.contains("successfully created"))
	    	throw new Exception("Error labeling: "+_output);
	}
	
	public void addTapeVolume(String pool, String label) throws Exception {
		if(pool == null) {
			throw new Exception("invalid pool");
		}
		
		Map<String, String> _data = getPool(pool);
		
		Command.systemCommand("echo \"label storage=" + _data.get("storage") + " volume=" + label + " pool=" + pool + "\" | /usr/bin/bconsole");
	}
	
	public List<Map<String, String>> getAvailableAutochangerVolumes(String storage) {
		List<Map<String, String>> volumes = new ArrayList<Map<String,String>>();
		try {
			for(Map<String, String> slot : LibraryManager.getStorageSlots(storage)) {
				if(slot.get("status") != null && "unassigned".equals(slot.get("status"))) {
					Map<String, String> volume = new HashMap<String, String>();
					volume.put("name", slot.get("value"));
					volume.put("slot", slot.get("name"));
					volumes.add(volume);
				}
			}
		} catch(Exception _ex) {}
		return volumes;
	}
	
	public Map<String, String> getPool(String name) throws Exception {
		Map<String, String> pool = new HashMap<String,String>();
		
		try {
			List<Map<String, Object>> resultPool = this._db.query("SELECT p.poolid FROM pool as p WHERE p.name <> \'" + this._c.getProperty("bacula.defaultPool") + "\' AND p.name = '" + name + "'");
			if(resultPool != null && !resultPool.isEmpty()) {
				Map<String, Object> row = resultPool.get(0);
				File _f = new File(WBSAirbackConfiguration.getDirectoryPools() + "/" + name + ".conf");
				pool.put("id", String.valueOf(row.get("poolid")));
				pool.put("name", name);
				if(_f.exists()) {
					pool.put("storage", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Storage"));
					pool.put("retention", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Volume Retention").split(" ")[0]);
					if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Volume Use Duration") != null) {
						pool.put("volume-duration", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Volume Use Duration").split(" ")[0]);
					}
					if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "NextPool") != null) {
						pool.put("copy", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "NextPool"));
						if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Migration Time") != null) {
							pool.put("migration-hours", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Migration Time").split(" ")[0]);
						}
					}
					if("Tape".equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Device", name, "Device Type"))) {
						pool.put("type", "tape");
					} else if(!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", name, "Changer Device").isEmpty()) {
						pool.put("type", "autochanger");
					} else {
						pool.put("type", "unknown");
					}
					if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "RecyclePool") != null) {
						pool.put("recycle_pool", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "RecyclePool").split(" ")[0]);
					}
					if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "ScratchPool") != null) {
						pool.put("scratch_pool", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "ScratchPool").split(" ")[0]);
					}

				}
				if(pool.get("storage") == null) {
					pool.put("storage", "");
				}
				if(pool.get("retention") == null) {
					pool.put("retention", "");
				}
				if(pool.get("copy") == null) {
					pool.put("copy", "");
				}
			}
	    } catch(DBException _ex) {
			throw new Exception("pool database error: " + _ex.getMessage());
		}
		return pool;
	}
	
	public int getPoolId(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("pool [" + name + "] not found");
		}
		
		List<Map<String, Object>> resultPool = this._db.query("SELECT p.poolid FROM pool as p WHERE p.name <> \'" + this._c.getProperty("bacula.defaultPool") + "\' AND p.name = '" + name + "'");
		if(resultPool != null && !resultPool.isEmpty()) {
			Map<String, Object> row = resultPool.get(0);
			if(row.get("poolid") instanceof Integer) {
				return (Integer) row.get("poolid");
			} else if(row.get("poolid") instanceof Long) {
				return ((Long) row.get("poolid")).intValue();
			} else if(row.get("poolid") instanceof Double) {
				return ((Double) row.get("poolid")).intValue();
			} else {
				try {
					return Integer.parseInt(String.valueOf(row.get("poolid")));
				} catch(NumberFormatException _ex) {}
			}
		}
		throw new Exception("pool [" + name + "] not found");
	}
	
	public static String getPoolType(String name) throws Exception {
		File _f = new File(WBSAirbackConfiguration.getDirectoryPools() + "/" + name + ".conf");
		if(!_f.exists()) {
			return "unknown";
		}
		
		String storage = BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Storage");
		
		_f = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + storage + ".xml");
		if(_f.exists()) {
			Configuration _c = new Configuration(_f);
			if(_c.checkProperty("storage.devicetype", "autochanger")) {
				return "autochanger";
			} else if(_c.checkProperty("storage.devicetype", "tape")) {
				return "tape";
			}
		} else if("Tape".equals(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", storage, "Device Type"))) {
			return "tape";
		} else if(!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", storage, "Changer Device").isEmpty() && !BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", storage, "Changer Device").equals("/dev/null")) {
			return "autochanger";
		}
		return "unknown";
	}
	
	public List<Map<String, String>> getPools() throws Exception {
		TreeMap<String, Map<String, String>> pools = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		try {
			List<Map<String, Object>> resultPool = this._db.query("select p.name, p.poolid from pool as p where p.name <> \'" + this._c.getProperty("bacula.defaultPool") + "\' order by p.name asc");
			for(Map<String, Object> row : resultPool) {
				Map<String, String> pool = new HashMap<String, String>();
				try {
					File _f = new File(WBSAirbackConfiguration.getDirectoryPools() + "/" + row.get("name") + ".conf");
					pool.put("id", String.valueOf(row.get("poolid")));
					pool.put("name", String.valueOf(row.get("name")));
					if(_f.exists()) {
						pool.put("storage", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", String.valueOf(row.get("name")), "Storage"));
						pool.put("retention", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", String.valueOf(row.get("name")), "Volume Retention"));
					}
					if(pool.get("storage") == null) {
						pool.put("storage", "");
					}
					if(pool.get("retention") == null) {
						pool.put("retention", "");
					}
					pools.put(pool.get("name"), pool);
				} catch(Exception _ex) {
					System.out.println("PoolManager::getPools: " + _ex.getMessage());
				}
			}
	    } catch(DBException _ex) {
			throw new Exception("pool database error: " + _ex.getMessage());
		}
	    
		return new ArrayList<Map<String,String>>(pools.values());
	}
	
	public List<String> getPoolNames() throws Exception {
		List<String> pools = new ArrayList<String>();
		
		try {
			List<Map<String, Object>> resultPool = this._db.query("select p.name from pool as p where p.name <> \'" + this._c.getProperty("bacula.defaultPool") + "\' order by p.name asc");
			for(Map<String, Object> row : resultPool) {
				try {
					pools.add(String.valueOf(row.get("name")));
				} catch(Exception _ex) {}
			}
	    } catch(DBException _ex) {
			throw new Exception("pool database error: " + _ex.getMessage());
		}
	    
	    Collections.sort(pools, String.CASE_INSENSITIVE_ORDER);
		return pools;
	}
	
	public List<Map<String, String>> getCompletePools() throws Exception {
		List<String> names = getPoolNames();
		List<Map<String, String>> pools = new ArrayList<Map<String, String>>();
		if (names != null && !names.isEmpty()) {
			for (String name : names) {
				Map<String, String> pool = getPool(name);
				if (pool != null && !pool.isEmpty())
					pools.add(pool);
			}
		}
		return pools;
	}
	
	public static Map<String, String> getTapeStatus(String storage) throws Exception {
		Map<String, String> status = new HashMap<String, String>();
		if(storage == null) {
			return status;
		}
		String _output = Command.systemCommand("echo \"status storage=" + storage + "\" | /usr/bin/bconsole | /bin/grep 'Device tape: \"" + storage + "\"'");
		status.put("label", "unknown");
		if(_output.contains("is not open")) {
			status.put("status", "empty");
		} else if(_output.contains("open but no Bacula volume is currently mounted")) {
			status.put("status", "unassigned");
		} else if(_output.contains("is mounted with:")) {
			status.put("status", "mounted");
			try {
				status.put("label", Command.systemCommand("/bin/echo \"status storage=" + storage + "\" | /usr/bin/bconsole | /bin/grep -A1 'Device tape: \"" + storage + "\"' | /bin/grep -v 'Device tape: \"" + storage + "\"' | tr -s \" \" | cut -d \" \" -f 3"));
			} catch(Exception _ex) {}
		} else {
			status.put("status", "unknown");
		}
		return status;
	}
	
	public String getPoolVolumesSizeFormated(int poolId) {
		Long size = this.getPoolVolumesSize(poolId);
		StringBuilder _sb = new StringBuilder();
		if(size >= 1000000000) {
			_sb.append(Math.rint((size/1000000000)*100)/100);
			_sb.append(" GB");
		} else if(size >= 1000000) {
			_sb.append(Math.rint((size/1000000)*100)/100);
			_sb.append(" MB");
		} else if(size >= 1000) {
			_sb.append(Math.rint((size/1000)*100)/100);
			_sb.append(" KB");
		} else {
			_sb.append(size);
			_sb.append(" B");
		}
		//logger.debug("Volumes size string {} ", _sb.toString());
		return _sb.toString();
	}
	
	public Long getPoolVolumesSize(int poolId) {
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("SELECT m.volbytes+m.volabytes as volbytes ");
			_sb.append("FROM media as m WHERE poolid = ");
			_sb.append(poolId);
			long totalsize = 0;
			for(Map<String, Object> result : this._db.query(_sb.toString())) {
				long size = 0;
				if(result.get("volbytes") instanceof Double) {
					size = ((Double) result.get("volbytes")).longValue();
					//logger.debug("Volumes size double : {} ", size);
				} else if(result.get("volbytes") instanceof Long) {
					size = (Long) result.get("volbytes");
					//logger.debug("Volumes size Long : {} ", size);
				} else if(result.get("volbytes") instanceof Integer) {
					size = (Integer) result.get("volbytes");
					//logger.debug("Volumes size Integer : {} ", size);
				}
				//logger.debug("Volumes size : {} ", size);
				totalsize+=size;
			}
			return totalsize;
		} catch (Exception ex) {
			logger.error("Error obtaining pool volumes size for pool {}. Ex: {}", poolId, ex.getMessage());
			return 0L;
		}
	}
	
	public List<Map<String, String>> getPoolVolumes(int poolId) throws Exception {
		List<Map<String, String>> volumes = new ArrayList<Map<String,String>>();
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT m.volumename, m.volstatus, m.volbytes+m.volabytes as volbytes, m.volfiles, m.volretention, m.mediatype, m.firstwritten, m.lastwritten, m.slot ");
		_sb.append("FROM media as m WHERE poolid = ");
		_sb.append(poolId);
		_sb.append(" ORDER BY m.lastwritten DESC, m.volumename");
		
		for(Map<String, Object> result : this._db.query(_sb.toString())) {
			Map<String, String> volume = new HashMap<String, String>();
			volume.put("name", String.valueOf(result.get("volumename")));
			volume.put("status", String.valueOf(result.get("volstatus")));
			volume.put("type", String.valueOf(result.get("mediatype")));
			volume.put("slot", String.valueOf(result.get("slot")));
			volume.put("files", String.valueOf(result.get("volfiles")));
			long size = 0;
			if(result.get("volbytes") instanceof Double) {
				size = ((Double) result.get("volbytes")).longValue();
			} else if(result.get("volbytes") instanceof Long) {
				size = (Long) result.get("volbytes");
			} else if(result.get("volbytes") instanceof Integer) {
				size = ((Integer) result.get("volbytes")).longValue();
			}
			_sb = new StringBuilder();
			if(size >= 1000000000) {
				_sb.append(Math.rint((size/1000000000)*100)/100);
				_sb.append(" GB");
			} else if(size >= 1000000) {
				_sb.append(Math.rint((size/1000000)*100)/100);
				_sb.append(" MB");
			} else if(size >= 1000) {
				_sb.append(Math.rint((size/1000)*100)/100);
				_sb.append(" KB");
			} else {
				_sb.append(size);
				_sb.append(" B");
			}
			volume.put("size", _sb.toString());
			if(result.get("volretention") != null) {
				size = 0;
				if(result.get("volretention") instanceof Double) {
					size = ((Double) result.get("volretention")).longValue();
				} else if(result.get("volretention") instanceof Long) {
					size = (Long) result.get("volretention");
				} else if(result.get("volretention") instanceof Integer) {
					size = ((Integer) result.get("volretention")).longValue();
				}
				_sb = new StringBuilder();
				if(size >= 86400) {
					_sb.append(size/86400);
					_sb.append(" days");
				} else if(size >= 3600) {
					_sb.append(size/3600);
					_sb.append(" hours");
				} else if(size >= 60) {
					_sb.append(size/60);
					_sb.append(" minutes");
				} else {
					_sb.append(size);
					_sb.append(" seconds");
				}
				volume.put("retention", _sb.toString());
			} else {
				volume.put("retention", "unknown");
			}
			
			if(result.get("firstwritten") != null) {
				volume.put("first-write", dateFormat.format(result.get("firstwritten")));
			} else {
				volume.put("first-write", "");
			}
			
			if(result.get("lastwritten") != null) {
				volume.put("last-write", dateFormat.format(result.get("lastwritten")));
			} else {
				volume.put("last-write", "");
			}
			
			volumes.add(volume);
		}
		return volumes;
	}
	
	public boolean hasPoolVolumes(int poolId) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT m.volumename, m.volstatus, m.volbytes, m.volfiles, m.volretention, m.mediatype, m.firstwritten, m.lastwritten, m.slot ");
		_sb.append("FROM media as m WHERE poolid = ");
		_sb.append(poolId);
		
		List<Map<String, Object>> _result = this._db.query(_sb.toString());
		if(_result != null && _result.size() > 0) {
			return true;
		}
		return false;
	}
	
	public static void purgePoolVolume(String label) throws Exception {
		Command.systemCommand("/bin/echo \"purge volume=" + label +"\" | /usr/bin/bconsole");
	}
	
	public static void removePoolVolume(String pool, String label) throws Exception {
		boolean deleteFile = true;
		if(pool == null || pool.isEmpty()) {
			throw new Exception("invalid pool name [" + pool + "]");
		}
		if(label == null || label.isEmpty()) {
			throw new Exception("invalid volume name [" + label + "]");
		}
		
		StringBuilder _sb = new StringBuilder(); 
        _sb.append("/tmp/wbsairback-poolvolume-");
        Random _r = new Random();
        for(int i = 15; i >= 0; --i) {
                _sb.append(_r.nextInt(10));
        }
        
        File _f = new File(_sb.toString());
        try {
        	_sb = new StringBuilder();
    		_sb.append("/usr/bin/bconsole <<END_OF_DATA\n");
    		_sb.append("delete media volume=" + label + "\n");
    		_sb.append("yes\n");
    		_sb.append("END_OF_DATA\n");
    		
    		FileOutputStream _fos = new FileOutputStream(_f);
    		_fos.write(_sb.toString().getBytes());
    		_fos.close();
    		
    		Command.systemCommand("/usr/bin/bconsole < " + _f.getAbsolutePath());
        } finally {
        	if(_f != null && _f.exists()) {
                _f.delete();
        	}
        }
		
		String storage = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryPools() + "/" + pool + ".conf", "Pool", pool, "Storage");
	    _f = new File(WBSAirbackConfiguration.getDirectoryStorage() + "/" + storage + ".xml");
		if(_f.exists()) {
			Configuration _cs = new Configuration(_f);
			if(_cs == null || _cs.getProperty("storage.external").equals("yes")) {
				deleteFile = false;
				String _device = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + storage + ".conf", "Storage", storage, "Device");
				sendRemovePoolVolume(_cs.getProperty("storage.address"), _device, label);
			}
		}
		
		if(deleteFile) {
	    	String _device = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + storage + ".conf", "Storage", storage, "Device");
	    	_sb = new StringBuilder();
	    	String value = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryStorage() + "/" + storage + ".conf", "Storage", storage, "Autochanger");
			if (value != null && !value.isEmpty() && value.equals("yes")) {
				_device = StorageManager.getVirtualDeviceName(storage, "R");
			}
	    	if (BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _device, "Archive Device")!=null && !BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _device, "Archive Device").isEmpty())
	    	{
		    	_sb.append(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", _device, "Archive Device"));
		    	if(!_sb.toString().endsWith("/")) {
		    		_sb.append("/");
		    	}
		    	_sb.append(label);
		    	_f = new File(_sb.toString()); 
			    if(_f.exists()) {
			    	Command.systemCommand("/bin/rm -rf " + _f.getAbsolutePath());
			    }
			    _sb.append(".add");
		    	_f = new File(_sb.toString());
			    if(_f.exists()) {
			    	Command.systemCommand("/bin/rm -rf " + _f.getAbsolutePath());
			    }
	    	}
		} 
	}
	
	public static void sendRemovePoolVolume(String address, String device, String label) throws Exception {
		HTTPClient _hc = new HTTPClient(address);
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/admin/Comm?type=");
		_sb.append(CommResponse.TYPE_POOL_MANAGER);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_REMOVE_POOL_VOL);
		_sb.append("&device=");
		_sb.append(device);
		_sb.append("&label=");
		_sb.append(label);
		_hc.load(_sb.toString());
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception("Catalog deletion complete. But got error deleting remote volume: "+_reply);
		}
	}
	
	public static void removePoolVolumeFromRemote(String storage, String label) throws Exception {
		String pathVolume = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", storage, "Archive Device");
		try { 
			Command.systemCommand("rm "+pathVolume+"/"+label);
		} catch (Exception ex) {}
	}
	
	public void setPool(String name, String device, int retention, int useDuration, String copyPool, int migrationHours, String scratchPool, String recyclePool)throws Exception{
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid pool name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryPools() + "/" + name + ".conf");
		if(!_f.exists()) {
			BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "pools", name);
		}
		
		try {
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Pool Type", new String[]{ "Backup" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Recycle", new String[]{ "yes" } );
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "AutoPrune", new String[]{ "yes" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Volume Retention", new String[]{ retention + " days" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "ActionOnPurge", new String[]{ "Truncate" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Storage", new String[]{ device });
			if(useDuration > 0) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Volume Use Duration", new String[]{ useDuration + " hours" });
			} else if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Volume Use Duration") != null) {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Volume Use Duration");
			}
			if(copyPool != null && !copyPool.isEmpty()) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "NextPool", new String[]{ copyPool });
				if(migrationHours < 0) {
					migrationHours = 0;
				}
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Migration Time", new String[]{ (migrationHours + " hours") });
			} else if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", name, "NextPool") != null) {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Pool", name, "NextPool");
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Migration Time");
			}
			
			if(recyclePool != null && !recyclePool.isEmpty()) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "RecyclePool", new String[]{ recyclePool });
			}else{
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Pool", name, "RecyclePool");
			}
			
			if(scratchPool != null && !scratchPool.isEmpty()) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "ScratchPool", new String[]{ scratchPool });
			}
			else{
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Pool", name, "ScratchPool");
			}
			
			boolean mediaTypeFile = false;
			Map<String, String> storage = StorageManager.getStorageParameters(device);
			if (storage != null && storage.get("storage.external") != null && storage.get("storage.external").equals("yes") && storage.get("storage.mediatype")!= null){
					mediaTypeFile = true;
			} else if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Media Type") != null && (BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Media Type").equals("File") || BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", device, "Media Type").contains("-")) ) {
					mediaTypeFile = true;
			} else {
				String changerDevice = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Autochanger", device, "Changer Device");
				if (changerDevice != null && changerDevice.equals("/dev/null"))
					mediaTypeFile = true;
			}
			
			if (mediaTypeFile) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "LabelFormat", new String[]{ "\"" + name + "\"" });
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Pool", name, "Maximum Volume Bytes", new String[]{ "10g" });
			}
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("/usr/bin/bconsole <<END_OF_DATA\n");
			_sb.append("update volume parameters\n");
			_sb.append("14\n");
			_sb.append("quit\n");
			_sb.append("END_OF_DATA");
			BackupOperator.reload();
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "pools", name);
			throw _ex;
		}
	}
	
	public void setPool(String name, String device, int retention, int useDuration, String copyPool, int migrationHours) throws Exception {
		setPool(name,device,retention,useDuration,copyPool,migrationHours,null,null);
	}
	
	public static void removePool(String name) throws Exception {
		try {
			if(JobManager.hasRunningJobs()) {
				throw new Exception("<m>backup.pool.jobs.currently.running</m>");
			}
			
			PoolManager _pm = new PoolManager(new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration())));
			if(_pm.hasPoolVolumes(_pm.getPoolId(name))) {
				throw new Exception("<m>backup.pool.still.volumes</m>");
			}
			
			List<String> _names = ScheduleManager.getSchedulesForPool(name);
			if(!_names.isEmpty()) {
				throw new Exception("<m>backup.pool.still.used.schedule</m> [" + _names.get(0) + "]");
			}
			
			_names = JobManager.getJobsForPool(name);
			if(!_names.isEmpty()) {
				throw new Exception("<m>backup.pool.still.used.job</m> [" + _names.get(0) + "]");
			}
			
			List<Map<String, String>> pools = _pm.getCompletePools();
			if (pools != null && !pools.isEmpty()) {
				for (Map<String, String> pool : pools) {
					if (pool.get("name") != null && !pool.get("name").equals(name)) {
						if (pool.get("copy") != null && pool.get("copy").equals(name))
							throw new Exception("This backup pool is copy destination of "+pool.get("name")+". Remove this association first.");
						else if (pool.get("copy") != null && pool.get("scratch_pool").equals(name))
							throw new Exception("This backup pool is scratch of "+pool.get("name")+". Remove this association first.");
						else if (pool.get("copy") != null && pool.get("recycle_pool").equals(name))
							throw new Exception("This backup pool is recycle pool of "+pool.get("name")+". Remove this association first.");
					}
				}
			}
			
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","pools", name);
		    Command.systemCommand("/usr/bin/bconsole <<END_OF_DATA\ndelete pool=" + name + "\nyes\nEND_OF_DATA" );
		    BackupOperator.reload();
		} catch (DBException _ex) {
			throw new DBException(_ex.getMessage());
	    }
	}
	
	public static List<String> getPoolsForStorage(String storage) {
		List<String> _pools = new ArrayList<String>();
		if(storage == null || storage.isEmpty()) {
			return _pools;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryPools());
		File[] _list = _dir.listFiles();
		
		for(File _f :_list) {
			if(_f.getName().endsWith(".conf")) {
				try {
					if(storage.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Pool", _f.getName().replaceAll(".conf[^ ]*", ""), "Storage"))) {
						_pools.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					}
				} catch(Exception _ex) {}
			}
		}
		return _pools;
	}
	
	public List<Map<String,String>> getPendantPoolJobs(String poolName, String level) throws DBException {
		List<Map<String,String>> jobs = new ArrayList<Map<String,String>>();
		
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT DISTINCT j.jobid,j.name,j.type,j.level,j.starttime,j.endtime,j.jobbytes FROM pool as p,job as j WHERE p.name = '");
		_sb.append(poolName);
		_sb.append("' AND p.poolid = j.poolid AND j.type = 'B' AND j.jobstatus = 'T'");
		if (level != null) {
			if (level.equals("F") || level.equals("I") || level.equals("D")) {
				_sb.append(" AND j.level = '");
				_sb.append(level);
				_sb.append("'");
			}
		}
		_sb.append(" AND j.jobid NOT IN (SELECT priorjobid FROM job WHERE type IN('C') AND j.jobstatus='T' AND priorjobid != 0) ORDER by j.jobid DESC");
		
		for(Map<String, Object> result : this._db.query(_sb.toString())) {
			HashMap<String,String> job = new HashMap<String,String>();
			job.put("jobid",String.valueOf(result.get("jobid")));
			job.put("name",String.valueOf(result.get("name")));
			job.put("type",String.valueOf(result.get("type")));
			job.put("level",String.valueOf(result.get("level")));
			job.put("starttime",dateFormat.format(result.get("starttime")));
			job.put("endtime",dateFormat.format(result.get("endtime")));
			
			long size = 0;
			String sizestr = "";
			if(result.get("jobbytes") instanceof Double) {
				size = ((Double) result.get("jobbytes")).longValue();
			} else if(result.get("jobbytes") instanceof Long) {
				size = (Long) result.get("jobbytes");
			} else if(result.get("jobbytes") instanceof Integer) {
				size = ((Integer) result.get("jobbytes")).longValue();
			}
			if(size >= 1073741824) {
				sizestr = String.valueOf(Math.rint((size/1073741824)*100)/100);
				sizestr += (" GB");
			} else if(size >= 1048576) {
				sizestr = String.valueOf(Math.rint((size/1048576)*100)/100);
				sizestr += (" MB");
			} else if(size >= 1024) {
				sizestr = String.valueOf(Math.rint((size/1024)*100)/100);
				sizestr += (" KB");
			} else {
				sizestr = String.valueOf(size);
				sizestr += (" B");
			}
			job.put("size",sizestr);
			
			String status = "backup.jobs.no_copied";
			StringBuilder _sb2 = new StringBuilder();
			_sb2.append("SELECT j.jobid FROM job as j WHERE priorjobid = ");
			_sb2.append(String.valueOf(result.get("jobid")));
			
			List<Map<String, Object>> _result = this._db.query(_sb2.toString());
			if(_result != null && _result.size() > 0) {
				status = "backup.jobs.copied";
			}
			job.put("status",status);
			
			jobs.add(job);
		}
		return jobs;
	}
	
	public void markJobAsCopied(String jobId) throws DBException {
		StringBuilder _sb = new StringBuilder();
		_sb.append("INSERT INTO job(job,name,type,level,jobstatus,priorjobid) VALUES ('Disabled','Disabled','C','F','T','");
		_sb.append(jobId);
		_sb.append("');");
							
		this._db.query(_sb.toString());
	}
	
	public void markJobsAsCopiedFromRemovedJob(String jobname) throws Exception {
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("SELECT j.jobid FROM job as j WHERE name='");
			_sb.append(jobname);
			_sb.append("'");
			for(Map<String, Object> result : this._db.query(_sb.toString())) {
				try {
					String jobid = String.valueOf(result.get("jobid"));
					markJobAsCopied(jobid);
				} catch (Exception ex) {
					logger.error("Error marcando como copiado un job de {}", jobname);
				}
			}
		} catch (Exception ex) {
			logger.error("Error marcando como copiados los jobs de {}", jobname);
		}
	}
}
