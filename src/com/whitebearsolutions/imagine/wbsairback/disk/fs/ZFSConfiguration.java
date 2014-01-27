package com.whitebearsolutions.imagine.wbsairback.disk.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.UserManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.mongodb.CommandEntry;
import com.whitebearsolutions.imagine.wbsairback.mongodb.CommandManager;
import com.whitebearsolutions.imagine.wbsairback.net.ReplicationManager;
import com.whitebearsolutions.imagine.wbsairback.util.ObjectLock;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class ZFSConfiguration {
	private final static Logger logger = LoggerFactory.getLogger(ZFSConfiguration.class);
	
	private final static String makeAggScriptPath = "/usr/share/wbsairback/bin/zfs/make_aggregate.sh";
	
	/**
	 * Copia los indices de los snapshots para una replica zfs
	 * @param group
	 * @param volume
	 * @param server
	 * @param keyName
	 * @param dgroup
	 * @param dvolume
	 */
	public static void copySnapshotsIndexes(String group, String volume, String server, String dgroup, String dvolume) throws Exception {
		
		StringBuilder _sb = new StringBuilder();
		_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
		String keyName = getReplicationKeyName(dgroup, dvolume);
		
		// Copiamos .daily
		if (new File("/mnt/airback/volumes/"+group+"/"+volume+"/.snapshots/.daily").exists()) {
			
			// Creamos directorio temporal
			_sb = new StringBuilder();
			_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
			_sb.append("sudo mkdir -p /tmp/snapIndexes/"+dgroup+"/"+dvolume+"/");
			try {
				logger.info("Creamos directorio temporal para copiar los indices de snapshots diarios zfs: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
			
			_sb = new StringBuilder();
			_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
			_sb.append("sudo chmod 777 /tmp/snapIndexes/"+dgroup+"/"+dvolume+"/");
			try {
				logger.info("Aplicamos permisos a directorio temporal para copiar los indices de snapshots diarios zfs: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
			
			//Copiamos en directorio temporal
			_sb = new StringBuilder();
			_sb.append("scp -oStrictHostKeyChecking=no -oCheckHostIP=no -i /var/.ssh/");
			_sb.append(keyName);
			_sb.append(" /mnt/airback/volumes/"+group+"/"+volume+"/.snapshots/.daily");
			_sb.append(" ");
			_sb.append(getFilesystemReplicationUser(dgroup, dvolume));
			_sb.append("@");
			_sb.append(server);
			_sb.append(":");
			_sb.append("/tmp/snapIndexes/"+dgroup+"/"+dvolume+"/");
			try {
				logger.info("Copiamos los indices de snapshots diarios zfs a un directorio temporal: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
			
			// Movemos el índice al directorio real
			_sb = new StringBuilder();
			_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
			_sb.append("sudo mv /tmp/snapIndexes/"+dgroup+"/"+dvolume+"/.daily /mnt/airback/volumes/"+dgroup+"/"+dvolume+"/.snapshots/");
			try {
				logger.info("Movemos los indices de snapshots diarios zfs a su directorio real: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
		}
		
		// Copiamos .hourly
		if (new File("/mnt/airback/volumes/"+group+"/"+volume+"/.snapshots/.hourly").exists()) {
			_sb = new StringBuilder();
			
			// Creamos directorio temporal
			_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
			_sb.append("sudo mkdir -p /tmp/snapIndexes/"+dgroup+"/"+dvolume+"/");
			try {
				logger.info("Creamos indice temporal para copiar los indices de snapshots horarios zfs: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
			
			_sb = new StringBuilder();
			_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
			_sb.append("sudo chmod 777 /tmp/snapIndexes/"+dgroup+"/"+dvolume+"/");
			try {
				logger.info("Aplicamos permisos a directorio temporal para copiar los indices de snapshots horarios zfs: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
			
			//Copiamos en directorio temporal
			_sb = new StringBuilder();
			_sb.append("scp -oStrictHostKeyChecking=no -oCheckHostIP=no -i /var/.ssh/");
			_sb.append(keyName);
			_sb.append(" /mnt/airback/volumes/"+group+"/"+volume+"/.snapshots/.hourly");
			_sb.append(" ");
			_sb.append(getFilesystemReplicationUser(dgroup, dvolume));
			_sb.append("@");
			_sb.append(server);
			_sb.append(":");
			_sb.append("/tmp/snapIndexes/"+dgroup+"/"+dvolume+"/");
			try {
				logger.info("Copiamos los indices de snapshots horarios zfs a un directorio temporal: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
			
			// Movemos el índice al directorio real
			_sb = new StringBuilder();
			_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
			_sb.append("sudo mv /tmp/snapIndexes/"+dgroup+"/"+dvolume+"/.hourly /mnt/airback/volumes/"+dgroup+"/"+dvolume+"/.snapshots/");
			try {
				logger.info("Movemos los indices de snapshots horarios zfs a su directorio real: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){}
		}
	}
	
	public static void createFileSystem(String group, String name, String compression, boolean encryption, boolean deduplication) throws Exception {
		if(compression != null && (!"gzip".equals(compression) &&
				"lzjb".equals(compression) && "zle".equals(compression))) {
			throw new Exception("filesystem do not support this compression type");
		}
		
		if(!poolExists(group)) {
			throw new Exception("filesystem pool does not exists");
		}
		
		StringBuilder _sb = new StringBuilder();
		if(!fileSystemExists(group, name)) {
			_sb = new StringBuilder();
			_sb.append("/sbin/zfs create ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
				logger.info("Nuevo volumen ZFS: {}",_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot create filesystem - " + _ex.getMessage());
			}
			_sb = new StringBuilder();
			_sb.append("/sbin/zfs set mountpoint=");
			_sb.append(VolumeManager.getMountPath(group, name));
			_sb.append(" ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
				logger.info("Volumen ZFS aplicamos mountpoint: {}",_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot set filesystem mountpoint - " + _ex.getMessage());
			}
			
			if (deduplication) {
				_sb = new StringBuilder();
				_sb.append("/sbin/zfs set dedup=on ");
				_sb.append(group);
				_sb.append("/");
				_sb.append(name);
				try {
					Command.systemCommand(_sb.toString());
					logger.info("Volumen ZFS con deduplicacion: {}",_sb.toString());
				} catch(Exception _ex) {
					throw new Exception("cannot set filesystem deduplication - " + _ex.getMessage());
				}
			}
			
			_sb = new StringBuilder();
			_sb.append("/sbin/zfs set sharenfs=on ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
				logger.info("Volumen ZFS activamos nfs: {}",_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot set filesystem share status - " + _ex.getMessage());
			}
		}
	}
	
	public static void createFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs snapshot ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append("@");
		_sb.append(snapshot);
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			throw new Exception("cannot create filesystem snapshot - " + _ex.getMessage());
		}
		
		mountFileSystemSnapshot(group, name, snapshot);
		
		_sb = new StringBuilder();
		_sb.append("/sbin/zfs hold -r keep ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append("@");
		_sb.append(snapshot);
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			removeFileSystemSnapshot(group, name, snapshot);
			throw new Exception("cannot hold filesystem snapshot - " + _ex.getMessage());
		}
	}
	
	public static void createPool(String group, List<String> devicePaths) throws Exception {
		if(!poolExists(group)) {
			StringBuilder _sb = new StringBuilder();
			_sb.append("bash ");
			_sb.append(makeAggScriptPath);
			_sb.append(" ");
			_sb.append(group);
			for (String device : devicePaths) {
				_sb.append(" ");
				_sb.append(device);
			}
			try {
				logger.info("Creamos un nuevo pool: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot create filesystem pool - " + _ex.getMessage());
			}
		} else {
			StringBuilder _sb = new StringBuilder();
			_sb.append("/sbin/zpool add -f ");
			_sb.append(group);
			_sb.append(" ");
			for (String device : devicePaths) {
				_sb.append(" ");
				_sb.append(device);
			}
			try {
				logger.info("Añadimos dispositivos a un pool: {}", _sb.toString());
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot extend filesystem pool - " + _ex.getMessage());
			}
		}
	}
	
	public static void createPool(String group, String devicePath) throws Exception {
		List<String> devices = new ArrayList<String>();
		devices.add(devicePath);
		createPool(group, devices);
	}
	
	public static void createZVol(String group, String name, double size, double percent_snap, String compression, Boolean deduplication) throws Exception {
		if(compression != null && (!"gzip".equals(compression) &&
				"lzjb".equals(compression) && "zle".equals(compression))) {
			throw new Exception("filesystem do not support this compression type");
		}
		
		if(!poolExists(group)) {
			throw new Exception("filesystem pool does not exists");
		}
		
		StringBuilder _sb = new StringBuilder();
		if(!fileSystemExists(group, name)) {
			_sb = new StringBuilder();
			_sb.append("/sbin/zfs create -s -b 128K -V ");
			_sb.append(getFormattedPlain(size));
			_sb.append(" ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
				logger.info("Nuevo volumen tipo bloque (Zvol) ZFS: {}",_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot create filesystem - " + _ex.getMessage());
			}
			
			if (deduplication) {
				_sb = new StringBuilder();
				_sb.append("/sbin/zfs set dedup=on ");
				_sb.append(group);
				_sb.append("/");
				_sb.append(name);
				try {
					Command.systemCommand(_sb.toString());
					logger.info("Volumen ZFS con deduplicacion: {}",_sb.toString());
				} catch(Exception _ex) {
					throw new Exception("cannot set filesystem deduplication - " + _ex.getMessage());
				}
			}
			
			if (compression != null && !compression.isEmpty()) {
				setCompression(group, name, compression);
			}
			
			double percent = 1 - percent_snap/100;
			double refreservation = percent*size;
			setZVolParams(group, name, size, size, refreservation);
		}
	}
	
	public static void extendFileSystem(String group, String name, Double size, Double data_size, Double total_reservation, Double data_reservation) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		double quota = size;
		double refquota = data_size;
		double reservation = total_reservation;
		double refreservation = data_reservation;
		setVolumeParams(group, name, quota, refquota, reservation, refreservation);
	}
	
	public static void extendZvol(String group, String name, Double size, Double total_reservation, Double data_reservation) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		double reservation = total_reservation;
		double refreservation = data_reservation;
		setZVolParams(group, name, size, reservation, refreservation);
	}
	
	public static boolean fileSystemExists(String group, String name) throws Exception {
		if(group == null || name == null ||
				group.isEmpty() || name.isEmpty()) {
			return false;
		}
		
		StringBuilder _volume = new StringBuilder();
		_volume.append(group);;
		_volume.append("/");
		_volume.append(name);
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs list -H -o name");
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.trim().equals(_volume.toString())) {
					return true;
				}
			}
		} finally {
			_br.close();
		}
		return false;
	}
	
	public static boolean fileSystemSnapshotExists(String group, String name, String snapshot) throws Exception {
		if(group == null || name == null || snapshot == null ||
				group.isEmpty() || name.isEmpty() || snapshot.isEmpty()) {
			return false;
		}
		
		StringBuilder _volume = new StringBuilder();
		_volume.append(group);
		_volume.append("/");
		_volume.append(name);
		_volume.append("/");
		_volume.append(snapshot);
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs list -H -o name");
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.trim().equals(_volume.toString())) {
					return true;
				}
			}
		} finally {
			_br.close();
		}
		return false;
	}
	
	public static String getFileSystemRemoteReplicationCommand(String group, String volume) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs receive -F ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(volume);
		return _sb.toString();
	}
	
	private static String getFilesystemReplicationUser(String group, String volume) {
    	StringBuilder _user = new StringBuilder();
		_user.append(group);
		_user.append("_");
		_user.append(volume);
		return _user.toString();
    }

	public static double getFileSystemSize(String group, String name) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
			
		double _size = 0;
		StringBuilder _sb = new StringBuilder();
		_sb = new StringBuilder();
		_sb.append("/sbin/zfs list -H -o quota ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		String _output = Command.systemCommand(_sb.toString());
		if(_output != null) {
			if(_output.contains("none")) {
				return 0D;
			}
			_size = getSize(_output.trim());
		}
		return _size;
	}
	
	public static Map<String, String> getFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		if(!fileSystemSnapshotExists(group, name, snapshot)) {
			throw new Exception("filesystem snapshot does not exists");
		}
		
		List<String> _hourly_snapshots = VolumeManager.getLogicalVolumeSnapshotIndexes(VolumeManager.LV_SNAPSHOT_HOURLY, group, name);
		List<String> _daily_snapshots = VolumeManager.getLogicalVolumeSnapshotIndexes(VolumeManager.LV_SNAPSHOT_DAILY, group, name);
		
		StringBuilder _sb = new StringBuilder();
		Map<String, String> _snapshot = new HashMap<String, String>();
		_sb = new StringBuilder();
		_sb.append("/sbin/zfs list -H -t snapshot -o name,used,written,referenced ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append("@");
		_sb.append(snapshot);
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null) {
				StringTokenizer _st = new StringTokenizer(_output);
				String _name = _st.nextToken();
				_name = _name.substring(_name.indexOf("@") + 1);
				String _used = _st.nextToken();
				_snapshot.put("name", _name);
				_snapshot.put("used-raw", String.valueOf(getSize(_used)));
				_snapshot.put("used", getFormattedSize(getSize(_used)));
				String _written = _st.nextToken();
				_snapshot.put("written-raw", String.valueOf(getSize(_written)));
				_snapshot.put("written", getFormattedSize(getSize(_written)));
				String _referenced = _st.nextToken();
				_snapshot.put("referenced-raw", String.valueOf(getSize(_referenced)));
				_snapshot.put("referenced", getFormattedSize(getSize(_referenced)));
				if(_hourly_snapshots.contains(_snapshot.get("name"))) {
					_snapshot.put("schedule", "H");
				} else if(_daily_snapshots.contains(_snapshot.get("name"))) {
					_snapshot.put("schedule", "D");
				} else {
					_snapshot.put("schedule", "M");
				}
				return _snapshot;
			}
		} catch(Exception _ex) {}
		throw new Exception("snapshot does not exists");
	}
	
	public static List<Map<String, String>> getFileSystemSnapshots(String group, String name) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		StringBuilder _sb = new StringBuilder();
		List<Map<String, String>> _snapshots = new ArrayList<Map<String,String>>();
		List<String> _hourly_snapshots = VolumeManager.getLogicalVolumeSnapshotIndexes(VolumeManager.LV_SNAPSHOT_HOURLY, group, name);
		List<String> _daily_snapshots = VolumeManager.getLogicalVolumeSnapshotIndexes(VolumeManager.LV_SNAPSHOT_DAILY, group, name);
		_sb.append("/sbin/zfs list -H -t snapshot -o name,used,written,referenced");
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null) {
				_sb = new StringBuilder();
				_sb.append(group);
				_sb.append("/");
				_sb.append(name);
				BufferedReader _br = new BufferedReader(new StringReader(_output));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(!_line.startsWith(_sb.toString()+"@")) {
							continue;
						}
						Map<String, String> _snapshot = new HashMap<String, String>();
						StringTokenizer _st = new StringTokenizer(_line);
						String _name = _st.nextToken();
						_name = _name.substring(_name.indexOf("@") + 1);
						_snapshot.put("name", _name);
						String _used = _st.nextToken();
						_snapshot.put("used-raw", String.valueOf(getSize(_used)));
						_snapshot.put("used", getFormattedSize(getSize(_used)));
						String _written = _st.nextToken();
						_snapshot.put("written-raw", String.valueOf(getSize(_written)));
						_snapshot.put("written", getFormattedSize(getSize(_written)));
						String _referenced = _st.nextToken();
						_snapshot.put("referenced-raw", String.valueOf(getSize(_referenced)));
						_snapshot.put("referenced", getFormattedSize(getSize(_referenced)));
						if(_hourly_snapshots.contains(_snapshot.get("name"))) {
							_snapshot.put("schedule", "H");
						} else if(_daily_snapshots.contains(_snapshot.get("name"))) {
							_snapshot.put("schedule", "D");
						} else {
							_snapshot.put("schedule", "M");
						}
						_snapshots.add(_snapshot);
					}
				} finally {
					_br.close();
				}
			}
		} catch(Exception _ex) {}
		return _snapshots;
	}
	
	private static String getFormattedInternalMB(double size) {
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		_sb.append(_df.format(size / 1048576D));
		return _sb.toString();
	}
	
	private static String getFormattedInternalSize(double size) {
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		if(size >= 1125899906842620D) {
			_sb.append(_df.format(size / 1125899906842620D));
			_sb.append("P");
		} else if(size >= 1099511627776D) {
			_sb.append(_df.format(size / 1099511627776D));
			_sb.append("T");
		} else if(size >= 1073741824D) {
			_sb.append(_df.format(size / 1073741824D));
			_sb.append("G");
		} else if(size >= 1048576D) {
			_sb.append(_df.format(size / 1048576D));
			_sb.append("M");
		} else if(size >= 1024D) {
			_sb.append(_df.format(size / 1024D));
			_sb.append("K");
		} else {
			_sb.append(_df.format(size));
		}
		return _sb.toString();
	}
	
	
	private static String getFormattedPercent(double size) {
		DecimalFormat _df = new DecimalFormat("#");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		_sb.append(_df.format(size));
		_sb.append("%");
		return _sb.toString();
	}
	
	private static String getFormattedPercentTwo(double size) {
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		_sb.append(_df.format(size));
		_sb.append("%");
		return _sb.toString();
	}
	
	private static String getFormattedPlain(double size) {
		DecimalFormat _df = new DecimalFormat("#");
		_df.setDecimalSeparatorAlwaysShown(false);
		return _df.format(size);
	}
	
	private static String getFormattedSize(double size) {
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		if(size >= 1125899906842620D) {
			_sb.append(_df.format(size / 1125899906842620D));
			_sb.append(" PB");
		} else if(size >= 1099511627776D) {
			_sb.append(_df.format(size / 1099511627776D));
			_sb.append(" TB");
		} else if(size >= 1073741824D) {
			_sb.append(_df.format(size / 1073741824D));
			_sb.append(" GB");
		} else if(size >= 1048576D) {
			_sb.append(_df.format(size / 1048576D));
			_sb.append(" MB");
		} else if(size >= 1024D) {
			_sb.append(_df.format(size / 1024D));
			_sb.append(" KB");
		} else {
			_sb.append(_df.format(size));
			_sb.append(" B");
		}
		return _sb.toString();
	}
	
	private static Map<String, String> getGeneralVolumeParams(String completeName, boolean withGroupParams) throws Exception {
		Map<String, String> volume = new HashMap<String, String>();
		String nameVg =  completeName.substring(0, completeName.indexOf("/"));
		String nameVol = completeName.substring(completeName.indexOf("/") + 1);
		if (withGroupParams)
			volume.putAll(getPool(nameVg));
		volume.put("name", nameVol);
		volume.put("vg", nameVg);
		volume.put("type", "DEDUPE");
		volume.put("fstype", "zfs");
		volume.put("path", VolumeManager.getLogicalVolumeDevicePath(nameVg, nameVol));
		volume.put("stripes", "");
		return volume;
	}
	
	public static Map<String, String> getGroupQuota(String group, String volume, String groupname) throws Exception {
		Map<String, String> _group = new HashMap<String, String>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs list -H -o groupused@");
		_sb.append(groupname);
		_sb.append(",groupquota@");
		_sb.append(groupname);
		_sb.append(" ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(volume);
		try {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			_sb = new StringBuilder();
			_sb.append(group);
			_sb.append("/");
			_sb.append(volume);
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(!_line.startsWith(_sb.toString())) {
						return _group;
					}
					StringTokenizer _st = new StringTokenizer(_line);
					if(!_st.hasMoreTokens()) {
						continue;
					}
					try {
						_group.put("used", getFormattedSize(getSize(_st.nextToken())));
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							String _value = _st.nextToken();
							double _size = getSize(_value);
							_group.put("soft", getFormattedSize(_size));
							_group.put("hard", getFormattedSize(_size));
							_group.put("mb_soft", getFormattedInternalMB(_size));
							_group.put("mb_hard", getFormattedInternalMB(_size));
						}
					} catch(NumberFormatException _ex) {}
					if(_group.get("used") == null) {
						_group.put("used", "");
					}
					if(_group.get("soft") == null) {
						_group.put("soft", "");
					}
					if(_group.get("hard") == null) {
						_group.put("hard", "");
					}
					
					return _group;
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read user quotas : " + _ex.getMessage());
		}
		throw new Exception("user quota not found");
	}
	
	private static String getMBufferMemory() throws Exception {
		String min = "100M";
		Long max = 2024L;
		try {
			String output = Command.systemCommand("free -m | grep -A1 free | grep Mem: | awk '{print $4}'");
			if (output != null && !output.isEmpty()) {
				Long mem = Long.parseLong(output.trim());
				mem = Math.round(mem*0.25);
				if (mem > max)
					return max+"M";
				else if (mem < 100)
					return min;
				else
					return mem+"M";
			}
			return min;
		} catch (Exception ex) {
			logger.error("Error obteniendo memoria para mbuffer. ex: {}", ex.getMessage());
			return min;
		}
		
	}
	
	public static Map<String, String> getPool(String pool) throws Exception {
		if(pool == null || pool.isEmpty()) {
			throw new Exception("filesystem pool does not exists");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool list -H -o name,size,capacity,allocated,dedupratio,free");
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				double _size = 0, _allocated = 0, _free = 0;
				Map<String, String> _pool = new HashMap<String, String>();
				StringTokenizer _st = new StringTokenizer(_line);	
				if(_st.hasMoreTokens()) {
					_pool.put("name", _st.nextToken());
				}
				if(!pool.equals(_pool.get("name"))) {
					continue;
				}
				DecimalFormat _df = new DecimalFormat("#");
				_df.setDecimalSeparatorAlwaysShown(false);
				if(_st.hasMoreTokens()) {
					_size = getSize(_st.nextToken());
					_pool.put("size-raw", _df.format(_size));
					_pool.put("size", getFormattedSize(_size));
				}
				if(_st.hasMoreTokens()) {
					String _value = _st.nextToken();
					_pool.put("used", _value.replace("%",""));
				}
				if(_st.hasMoreTokens()) {
					_allocated = getSize(_st.nextToken());
					_pool.put("allocated-raw", String.valueOf(_allocated));
					_pool.put("allocated", getFormattedSize(_allocated));
				}
				if (_size > 0) {
					_free = _size;
				}
				if (_allocated > 0) {
					_free -=_allocated;
				}
				_pool.put("free-raw", String.valueOf(_free));
				_pool.put("free", getFormattedSize(_free));
				if(_st.hasMoreTokens()) {
					double _ratio = 0;
					String _value = _st.nextToken();
					if(_value.endsWith("x")) {
						try {
							_ratio = Double.parseDouble(_value.substring(0, _value.length() - 1));
						} catch(NumberFormatException _ex) {}
					}
					_pool.put("dedup-ratio", String.valueOf(_ratio));
					if(_ratio > 1.00D) {
						_ratio = 100.00D - (1.00D/_ratio)*100.00D;
					} else {
						_ratio = 0;
					}
					_pool.put("deduplicated", getFormattedPercentTwo(_ratio));
					_pool.put("deduplicated-raw", String.valueOf(_ratio));
				}
				if (_st.hasMoreTokens()) {
					double _unused = getSize(_st.nextToken());
					_pool.put("unused-raw", _df.format(_unused));	
					_pool.put("unused", getFormattedSize(_unused));
					double _percent = _unused * 100 /Double.parseDouble(_pool.get("size-raw"));
					if (_percent < 0D)
						_percent = 0D;
					else if (_percent > 100D)
						_percent = 100D;
					_pool.put("unused-percent-raw", String.valueOf(_percent));
					_pool.put("unused-percent", getFormattedPercent(_percent));
				}
				putPoolVolumesQuotaReservation(_pool);
				return _pool;
			}
		} finally {
			_br.close();
		}
		throw new Exception("filesystem pool does not exists");
	}
	
	public static List<Map<String, String>> getPoolDevices(String pool) throws Exception {
		List<Map<String, String>> _devices = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool status ");
		_sb.append(pool);
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null ; _line = _br.readLine()) {
				_line = _line.trim();
				if(!_line.startsWith("pool: ") && _line.startsWith(pool + " ")) {
					for(_line = _br.readLine(); _line != null && !_line.isEmpty(); _line = _br.readLine()) {
						_line = _line.trim();
						if (_line.startsWith("logs") || _line.startsWith("cache"))
							break;
						Map<String, String> _device = new HashMap<String, String>();
						StringTokenizer _st = new StringTokenizer(_line);
						if(_st.hasMoreTokens()) {
							String _dev = _st.nextToken();
							
							if(!_dev.startsWith("/dev/")) {
								_dev = "/dev/".concat(_dev);
							}
							_device.put("device", _dev);
						}
						if(_st.hasMoreTokens()) {
							_device.put("status", _st.nextToken().toLowerCase());
						}
						_devices.add(_device);
					}
				}
			}
		} finally {
			_br.close();
		}
		return _devices;
	}
	
	public static List<String> getPoolFileSystemNames(String pool) throws Exception {
		List<String> _list = new ArrayList<String>();
		if(pool == null || pool.isEmpty()) {
			return _list;
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs list -H -o name,origin");
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				StringTokenizer _st = new StringTokenizer(_line);	
				if(_st.hasMoreTokens()) {
					String _name = _st.nextToken();
					if(_st.hasMoreTokens()) {
						String _origin = _st.nextToken();
						if(!_origin.equals("-")) {
							continue;
						}
					}
					if(!_name.contains("/")) {
						continue;
					}
					if(pool.equalsIgnoreCase(_name.substring(0, _name.indexOf("/")))) {
						_list.add(_name.substring(_name.indexOf("/") + 1));
					}
				}
			}
		} finally {
			_br.close();
		}
		return _list;
	}
	
	public static List<String> getPoolNames() throws Exception {
		List<String> _pools = new ArrayList<String>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool list -H");
		String _output = null;
		BufferedReader _br = null;
		try {
			try {
				_output = Command.systemCommand(_sb.toString());
			} catch (Exception ex) {}
			if (_output != null) {
				_br = new BufferedReader(new StringReader(_output));
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					StringTokenizer _st = new StringTokenizer(_line);	
					if(_st.hasMoreTokens()) {
						_pools.add(_st.nextToken());
					}
				}
			}
		} finally {
			if (_br != null)
				_br.close();
		}
		return _pools;
	}
	
	public static List<Map<String, String>> getPools() throws Exception {
		List<Map<String, String>> _pools = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool list -H -o name,size,capacity,allocated,dedupratio,free");
		BufferedReader _br = null;
		try {
			String _output = null;
			try {
				_output = Command.systemCommand(_sb.toString());
			} catch (Exception ex) {
				
			}
			if (_output != null) {
				_br = new BufferedReader(new StringReader(_output));
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					double _size = 0, _allocated = 0, _free = 0;
					DecimalFormat _df = new DecimalFormat("#");
					_df.setDecimalSeparatorAlwaysShown(false);
					Map<String, String> _pool = new HashMap<String, String>();
					StringTokenizer _st = new StringTokenizer(_line);	
					if(_st.hasMoreTokens()) {
						_pool.put("name", _st.nextToken());
					}
					if(_st.hasMoreTokens()) {
						_size = getSize(_st.nextToken());
						_pool.put("size-raw", _df.format(_size));
						_pool.put("size", getFormattedSize(_size));
					}
					if(_st.hasMoreTokens()) {
						String _value = _st.nextToken();
						_pool.put("used", _value.replace("%",""));
					}
					if(_st.hasMoreTokens()) {
						_allocated = getSize(_st.nextToken());
						_pool.put("allocated-raw", String.valueOf(_allocated));
						_pool.put("allocated", getFormattedSize(_allocated));
					}
					if (_size > 0) {
						_free = _size;
					}
					if (_allocated > 0) {
						_free -=_allocated;
					}
					_pool.put("free-raw", String.valueOf(_free));
					_pool.put("free", getFormattedSize(_free));
					if(_st.hasMoreTokens()) {
						double _ratio = 0;
						String _value = _st.nextToken();
						if(_value.endsWith("x")) {
							try {
								_ratio = Double.parseDouble(_value.substring(0, _value.length() - 1));
							} catch(NumberFormatException _ex) {}
						}
						_pool.put("dedup-ratio", String.valueOf(_ratio));
						if(_ratio > 1.00D) {
							_ratio = 100.00D - (1.00D/_ratio)*100.00D;
						} else {
							_ratio = 0;
						}
						_pool.put("deduplicated", getFormattedPercentTwo(_ratio));
						_pool.put("deduplicated-raw", String.valueOf(_ratio));
					}
					if (_st.hasMoreTokens()) {
						double _unused = getSize(_st.nextToken());
						_pool.put("unused-raw", _df.format(_unused));	
						_pool.put("unused", getFormattedSize(_unused));
						double _percent = _unused * 100 /Double.parseDouble(_pool.get("size-raw"));
						if (_percent < 0D)
							_percent = 0D;
						_pool.put("unused-percent-raw", String.valueOf(_percent));
						_pool.put("unused-percent", getFormattedPercent(_percent));
					}
					putPoolVolumesQuotaReservation(_pool);
					_pools.add(_pool);
				}
			}
		} catch (Exception ex) {
			throw new Exception("Error while getting groups sizes: "+ex);
		} finally {
			if (_br != null)
				_br.close();
		}
		return _pools;
	}
	
	/**
	 * Devuelve el 'prefijo' ssh para ejecutar un comando de forma remota en la ip server, para el volumen zfs group/volume
	 * @param server
	 * @param group
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static String getRemoteCommandPrefix(String server, String group, String name) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("ssh -x -oStrictHostKeyChecking=no -oCheckHostIP=no -i /var/.ssh/");
		_sb.append(getReplicationKeyName(group, name));
		_sb.append(" ");
		_sb.append(getFilesystemReplicationUser(group, name));
		_sb.append("@");
		_sb.append(server);
		_sb.append(" ");
		return _sb.toString();
	}
	
	public static List<Map<String, String>> getRemoteFileSystemSnapshots(String server, String group, String name) throws Exception {
		List<Map<String, String>> _snapshots = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		
		_sb.append(getRemoteCommandPrefix(server, group, name));
		_sb.append("sudo /sbin/zfs list -H -t snapshot -o name");
		
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null) {
				_sb = new StringBuilder();
				_sb.append(group);
				_sb.append("/");
				_sb.append(name);
				BufferedReader _br = new BufferedReader(new StringReader(_output));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(!_line.startsWith(_sb.toString()+"@")) {
							continue;
						}
						Map<String, String> _snapshot = new HashMap<String, String>();
						StringTokenizer _st = new StringTokenizer(_line);
						String _name = _st.nextToken();
						_name = _name.substring(_name.indexOf("@") + 1);
						_snapshot.put("name", _name);
						_snapshots.add(_snapshot);
					}
				} finally {
					_br.close();
				}
			}
		} catch(Exception _ex) {}
		return _snapshots;
	}
	
	public static String getReplicationKeyName(String group, String volume) {
    	StringBuilder _user = new StringBuilder();
    	_user.append("key_rep");
		_user.append(group);
		_user.append("_");
		_user.append(volume);
		return _user.toString();
    }
	
	private static double getSize(String value) {
		double _size = 0;
		char unit = 'B';
		if(value.matches("[0-9,.]+([KMGTP])$")) {
			unit = value.charAt(value.length() - 1);
			value = value.substring(0, value.length() - 1).trim();
		}
		try {
			if(value.contains(",")) {
				value = value.replace(",", ".");
			}
			_size = Double.parseDouble(value);
		} catch(NumberFormatException _ex) {}
		switch(unit) {
			case 'B': {
					// nothing
				}
				break;
			case 'K': {
					_size = _size * 1024D;
				}
				break;
			case 'M': {
					_size = _size * 1048576D;
				}
				break;
			case 'G': {
					_size = _size * 1073741824D;
				}
				break;
			case 'T': {
					_size = _size * 1099511627776D;
				}
				break;
			case 'P': {
					_size = _size * 1125899906842620D;
				}
				break;
		}
		return _size;
	}
	
	public static Map<String, String> getUserQuota(String group, String volume, String user) throws Exception {
		Map<String, String> _user = new HashMap<String, String>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs list -H -o userused@");
		_sb.append(user);
		_sb.append(",userquota@");
		_sb.append(user);
		_sb.append(" ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(volume);
		try {
			BufferedReader _bf = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			_sb = new StringBuilder();
			_sb.append(group);
			_sb.append("/");
			_sb.append(volume);
			for(String _line = _bf.readLine(); _line != null; _line = _bf.readLine()) {
				StringTokenizer _st = new StringTokenizer(_line);
				if(!_st.hasMoreTokens()) {
					continue;
				}
				try {
					_user.put("used", getFormattedSize(getSize(_st.nextToken())));
				} catch(NumberFormatException _ex) {}
				try {
					if(_st.hasMoreTokens()) {
						String _value = _st.nextToken();
						double _size = getSize(_value);
						_user.put("soft", getFormattedSize(_size));
						_user.put("hard", getFormattedSize(_size));
						_user.put("mb_soft", getFormattedInternalMB(_size));
						_user.put("mb_hard", getFormattedInternalMB(_size));
					}
				} catch(NumberFormatException _ex) {}
				if(_user.get("used") == null) {
					_user.put("used", "");
				}
				if(_user.get("soft") == null) {
					_user.put("soft", "");
				}
				if(_user.get("hard") == null) {
					_user.put("hard", "");
				}
				
				return _user;
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read user quotas : " + _ex.getMessage());
		}
		throw new Exception("user quota not found");
	}
	
	public static Map<String, String> getVolume(String group, String name, boolean withGroupParams) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("/sbin/zfs get -Hp name,origin,type,quota,volsize,usedbydataset,usedbysnapshots,usedbyrefreservation,available,compress,compressratio,dedup,reservation,refreservation,refquota,used ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			String _output = Command.systemCommand(_sb.toString());
			if (_output != null && !_output.isEmpty()) {
				Map<String, String> data = readVolumeParameters(_output);
				if (data != null) {
					Map<String, String> volume = getGeneralVolumeParams(group+"/"+name, withGroupParams);
					volume.putAll(data);
					return volume;
				}
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error interpretando parametros del volumen zfs. {}/{}. Ex:", new Object[]{group, name, ex.getMessage()});
			throw new Exception("Could not read zfs params of "+group+"/"+name);
		}
	}
	
	public static List<Map<String, String>> getVolumeGroupCacheDisks(String pool) throws Exception {
		List<Map<String, String>> _devices = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool status ");
		_sb.append(pool);
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null ; _line = _br.readLine()) {
				_line = _line.trim();
				if(_line.startsWith("cache")) {
					for(_line = _br.readLine(); _line != null && !_line.isEmpty(); _line = _br.readLine()) {
						_line = _line.trim();
						if (_line.startsWith("logs"))
							break;
						Map<String, String> _device = new HashMap<String, String>();
						StringTokenizer _st = new StringTokenizer(_line);
						if(_st.hasMoreTokens()) {
							String _dev = _st.nextToken();
							
							if(!_dev.startsWith("/dev/")) {
								_dev = "/dev/".concat(_dev);
							}
							_device.put("device", _dev);
						}
						if(_st.hasMoreTokens()) {
							_device.put("status", _st.nextToken().toLowerCase());
						}
						_device.putAll(VolumeManager.getPhysicalDisk(_device.get("device")));
						_devices.add(_device);
					}
				}
			}
		} finally {
			_br.close();
		}
		return _devices;
	}
	
	public static List<Map<String, String>> getVolumeGroupLogDisks(String pool) throws Exception {
		List<Map<String, String>> _devices = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool status ");
		_sb.append(pool);
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null ; _line = _br.readLine()) {
				_line = _line.trim();
				if(_line.startsWith("logs")) {
					for(_line = _br.readLine(); _line != null && !_line.isEmpty(); _line = _br.readLine()) {
						_line = _line.trim();
						if (_line.startsWith("cache"))
							break;
						Map<String, String> _device = new HashMap<String, String>();
						StringTokenizer _st = new StringTokenizer(_line);
						if(_st.hasMoreTokens()) {
							String _dev = _st.nextToken();
							
							if(!_dev.startsWith("/dev/")) {
								_dev = "/dev/".concat(_dev);
							}
							_device.put("device", _dev);
						}
						if(_st.hasMoreTokens()) {
							_device.put("status", _st.nextToken().toLowerCase());
						}
						_device.putAll(VolumeManager.getPhysicalDisk(_device.get("device")));
						_devices.add(_device);
					}
				}
			}
		} finally {
			_br.close();
		}
		return _devices;
	}
	
	public static List<Map<String, String>> getVolumes(boolean withGroupParams) throws Exception {
		List<Map<String, String>> _volumes = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs get -Hp name,origin,type,quota,volsize,usedbydataset,usedbysnapshots,usedbyrefreservation,available,compress,compressratio,dedup,reservation,refreservation,refquota,used");
		try {
			
			String _output = CommandManager.launchNoBDCommand(_sb.toString());
			BufferedReader _br = new BufferedReader(new StringReader(_output));
		
			String _outvol="";
			String _lastname = null;
			String _name = null;
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				StringTokenizer _st = new StringTokenizer(_line);
				if(_st.hasMoreTokens()) {
					_name = _st.nextToken();
					if (!_name.contains("/"))
						continue;
				}
				
				if (_lastname == null) {
					_lastname = _name;
				} else if (!_lastname.equals(_name)) {
					Map<String, String> data = readVolumeParameters(_outvol);
					if (data != null) {
						Map<String, String> volume = getGeneralVolumeParams(_lastname, withGroupParams);
						volume.putAll(data);
						_volumes.add(volume);
					}
					_outvol="";
					_lastname = _name;
				} else {
					if (_outvol.equals(""))
						_outvol=_line+"\n";
					else
						_outvol+=_line+"\n";
				}
			}
			
			if (_lastname != null && !_outvol.equals("")) {
				Map<String, String> data = readVolumeParameters(_outvol);
				if (data != null) {
					Map<String, String> volume = getGeneralVolumeParams(_lastname, withGroupParams);
					volume.putAll(data);
					_volumes.add(volume);
				}
			}
			
			return _volumes;
		} catch (Exception ex) {
			logger.error("Error interpretando parametros de volumenes zfs. {}", ex.getMessage());
			throw new Exception("Could not read zfs params");
		}
	}
	
	public static List<Map<String, String>> getVolumes(String group, boolean withGroupParams) throws Exception {
		List<Map<String, String>> _volumes = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs get -Hp name,origin,type,quota,volsize,usedbydataset,usedbysnapshots,usedbyrefreservation,available,compress,compressratio,dedup,reservation,refreservation,refquota,used");
		try {
			String _output = CommandManager.launchNoBDCommand(_sb.toString());
			BufferedReader _br = new BufferedReader(new StringReader(_output));
		
			String _outvol="";
			String _lastname = null;
			String _name = null;
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				StringTokenizer _st = new StringTokenizer(_line);
				if(_st.hasMoreTokens()) {
					_name = _st.nextToken();
					if (!_name.contains("/") || !_name.startsWith(group))
						continue;
				}
				
				if (_lastname == null) {
					_lastname = _name;
				} else if (!_lastname.equals(_name)) {
					Map<String, String> data = readVolumeParameters(_outvol);
					if (data != null) {
						Map<String, String> volume = getGeneralVolumeParams(_lastname, withGroupParams);
						volume.putAll(data);
						_volumes.add(volume);
					}
					_outvol="";
					_lastname = _name;
				} else {
					if (_outvol.equals(""))
						_outvol=_line+"\n";
					else
						_outvol+=_line+"\n";
				}
			}
			
			if (_lastname != null && !_outvol.equals("")) {
				Map<String, String> data = readVolumeParameters(_outvol);
				if (data != null) {
					Map<String, String> volume = getGeneralVolumeParams(_lastname, withGroupParams);
					volume.putAll(data);
					_volumes.add(volume);
				}
			}
			
			return _volumes;
		} catch (Exception ex) {
			logger.error("Error interpretando parametros de volumenes zfs. {}", ex.getMessage());
			throw new Exception("Could not read zfs params");
		}
	}
	
	
	public static String getZFSParamCommand(String group, String name, String param, double value) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs "); 
		_sb.append(param);
		_sb.append("=");
		_sb.append(getFormattedPlain(value));
		_sb.append(" ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		return _sb.toString();
	}
	
	
	public static String getZFSReservationCommands(Map<String, String> vol, double reservation, double refreservation, boolean withAnd) throws Exception {
		StringBuilder _sb = new StringBuilder();
		double current_reservation = Double.parseDouble(vol.get("reservation-raw"));
		double current_refreservation = Double.parseDouble(vol.get("refreservation-raw"));
		if (reservation > current_reservation) {
			if (withAnd)
				_sb.append(" && ");
			_sb.append(getZFSParamCommand(vol.get("vg"), vol.get("name"), "reservation", reservation));
			_sb.append(" && ");
			_sb.append(getZFSParamCommand(vol.get("vg"), vol.get("name"), "refreservation", refreservation));
		} else if (reservation < current_reservation) {
			if (withAnd)
				_sb.append(" && ");
			_sb.append(getZFSParamCommand(vol.get("vg"), vol.get("name"), "refreservation", refreservation));
			_sb.append(" && ");
			_sb.append(getZFSParamCommand(vol.get("vg"), vol.get("name"), "reservation", reservation));
		} else if (current_refreservation != refreservation){
			if (withAnd)
				_sb.append(" && ");
			_sb.append(getZFSParamCommand(vol.get("vg"), vol.get("name"), "refreservation", refreservation));
		}
		return _sb.toString();
	}
	
	public static boolean isRemoteSystemMounted(String path) {
		try {
			String _sb = "sudo cat /proc/mounts";
			String _output = Command.systemCommand(_sb);
			if(_output.contains(" " + path + " ")) {
					return true;
			}
		} catch(Exception _ex) {
			return false;
		}
		return false;
	}
	
	public static boolean mountFileSystem(String group, String name) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs mount ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append(" && /bin/echo \"yes\" || /bin/echo \"no\"");
		String _output = Command.systemCommand(_sb.toString()).trim();
		if(!"yes".contains(_output)) {
			return false;
		} else {
			return true;
		}
	}
	
	public static void mountFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		
		File _f = new File(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot));
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		StringBuilder _sb = new StringBuilder();
		if(!fileSystemSnapshotExists(group, name, snapshot)) {
			_sb.append("/sbin/zfs clone -o mountpoint=");
			_sb.append(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot));
			_sb.append(" ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			_sb.append("@");
			_sb.append(snapshot);
			_sb.append(" ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			_sb.append("/");
			_sb.append(snapshot);
			_sb.append(" && /bin/echo \"yes\" || /bin/echo \"no\"");
			String _output = Command.systemCommand(_sb.toString()).trim();
			if(!"yes".equals(_output)) {
				throw new Exception("unable to mount filesystem snapshot");
			}
		} else {
			if(!VolumeManager.isSystemMounted(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot))) {
				_sb.append("/sbin/zfs mount ");
				_sb.append(group);
				_sb.append("/");
				_sb.append(name);
				_sb.append("/");
				_sb.append(snapshot);
				_sb.append(" && /bin/echo \"yes\" || /bin/echo \"no\"");
				String _output = Command.systemCommand(_sb.toString()).trim();
				if(!"yes".equals(_output)) {
					throw new Exception("unable to mount filesystem snapshot");
				}
			}
		}
	}
	
	public static void mountRemoteFileSystemSnapshot(String server, String group, String name, String snapshot, CommandManager cm) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		
		File _f = new File(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot));
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		StringBuilder _sb = new StringBuilder();
		
		_sb = new StringBuilder();
		_sb.append(getRemoteCommandPrefix(server, group, name));
		_sb.append("sudo /sbin/zfs hold -r keep ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append("@");
		_sb.append(snapshot);
		try {
			boolean launch = false;
			if(!cm.isRunning(_sb.toString())) {
				launch = true;
			} else {
				int interval = ReplicationManager.DEFAULT_MAX_EXECUTION_TIME;
				Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
				if (_c.hasProperty("rsync_interval"))
					interval = Integer.parseInt(_c.getProperty("rsync_interval"));
				List<CommandEntry> commands = cm.findCommandsByCommandStringAndStatus(_sb.toString(), CommandManager.STATUS_COMAND_RUNING);
				for (CommandEntry com : commands) {
					if (com.getDateLaunched() != null) {
						if((Calendar.getInstance().getTimeInMillis() - com.getDateLaunched().getTime()) > interval) {
							launch = true;
							ReplicationManager.killRunningSyncronizationsOf(com);
							cm.updateUnfinishedCommand(com);
						}
					}
				}
			}
			
			if (launch) {
				logger.debug("Lanzamos zfs hold en destino: {}", _sb.toString());
				cm.asyncExecuteAndWait(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON, null);
			}
		} catch(Exception _ex) {
			removeRemoteFileSystemSnapshot(server, group, name, snapshot, cm);
			throw new Exception("cannot hold filesystem snapshot - " + _ex.getMessage());
		}
	}
	

	public static boolean poolExists(String group) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool list -H");
		String _output = null;
		try {
			_output = Command.systemCommand(_sb.toString());
		} catch (Exception ex) {}
		if (_output != null) {
			BufferedReader _br = new BufferedReader(new StringReader(_output));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					StringTokenizer _st = new StringTokenizer(_line);
					if(_st.hasMoreTokens()) {
						String _token = _st.nextToken();
						if(_token.equals(group)) {
							return true;
						}
					}
				}
			} finally {
				_br.close();
			}
		}
		return false;
	}
	
	public static void putPoolDedupInfo(Map<String, String> pool) throws Exception {
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("/sbin/zpool status -D ");
			_sb.append(pool.get("name"));
			String _output = Command.systemCommand(_sb.toString());
			if (_output != null && !_output.isEmpty()) {
				BufferedReader _br = new BufferedReader(new StringReader(_output));
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if (_line.contains("Total")) {
						StringTokenizer _st = new StringTokenizer(_line);
						if (_st.hasMoreTokens()) {	// Total
							_st.nextToken();
						}
						if (_st.hasMoreTokens()) {	// blocks
							_st.nextToken();
						}
						
						Double dedupAlloc = null;
						if (_st.hasMoreTokens()) {
							dedupAlloc = getSize(_st.nextToken());
							pool.put("dedupAlloc-raw", String.valueOf(dedupAlloc));
							pool.put("dedupAlloc", VolumeManager.getFormatSize(String.valueOf(dedupAlloc)+" B"));
						}
						
						Double dedupAllocCompress = null;
						if (_st.hasMoreTokens()) {
							dedupAllocCompress = getSize(_st.nextToken());
							pool.put("dedupAllocCompress-raw", String.valueOf(dedupAllocCompress));
							pool.put("dedupAllocCompress", VolumeManager.getFormatSize(String.valueOf(dedupAllocCompress)+" B"));
						}
						
						if (_st.hasMoreTokens()) {	// dsize
							_st.nextToken();
						}
						if (_st.hasMoreTokens()) {	// blocks
							_st.nextToken();
						}
						
						Double dedupReferenced = null;
						if (_st.hasMoreTokens()) {
							dedupReferenced = getSize(_st.nextToken());
							pool.put("dedupReferenced-raw", String.valueOf(dedupReferenced));
							pool.put("dedupReferenced", VolumeManager.getFormatSize(String.valueOf(dedupReferenced)+" B"));
						}
						
						/*Double dedupReferencedCompress = null;
						if (_st.hasMoreTokens()) {
							dedupReferencedCompress = getSize(_st.nextToken());
							pool.put("dedupReferencedCompress-raw", String.valueOf(dedupReferencedCompress));
							pool.put("dedupReferencedCompress", VolumeManager.getFormatSize(String.valueOf(dedupReferencedCompress)));
						}*/
								
						Double totalSavedSpace = dedupReferenced - dedupAllocCompress;
						pool.put("totalSavedSpace-raw", String.valueOf(totalSavedSpace));
						pool.put("totalSavedSpace", VolumeManager.getFormatSize(String.valueOf(totalSavedSpace)+" B"));
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error interpretando los datos de deduplicación del pool zfs: {}. Ex: {}", new Object[]{pool.get("name"), ex.getMessage()});
			throw new Exception("Could not read zfs dedup data of "+pool.get("name"));
		}
	}
	
	public static void putFreeSizePool(Map<String, String> pool) throws Exception {
		try {
			Double _free = 0D;
			Double _used = 0D;
			DecimalFormat _df = new DecimalFormat("#");
			_df.setDecimalSeparatorAlwaysShown(false);
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("/sbin/zfs get -Hp avail,used ");
			_sb.append(pool.get("name"));
			String _output = Command.systemCommand(_sb.toString());
			if (_output != null && !_output.isEmpty()) {
				BufferedReader _br = new BufferedReader(new StringReader(_output));
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					StringTokenizer _st = new StringTokenizer(_line);
					if (_st.hasMoreTokens()) {	// Name
						_st.nextToken();
					}
					String property = null;
					if (_st.hasMoreTokens()) {	// Property
						property = _st.nextToken();
					}
					
					if (_st.hasMoreTokens()) {	// Value
						if (property.equals("available")) {
							_free = getSize(_st.nextToken());
							if (_free < 0D)
								_free = 0D;
							pool.put("fs-free-raw", _df.format(_free));	
							pool.put("fs-free", getFormattedSize(_free));
						} else if (property.equals("used")) {
							_used = getSize(_st.nextToken());
							if (_used < 0D)
								_used = 0D;
							double size = _free+_used;
							//double volused = Double.parseDouble(pool.get("vol-used-raw"));
							
							pool.put("fs-used-raw", _df.format(_used));	
							pool.put("fs-used", getFormattedSize(_used));
							
							pool.put("fs-size-raw", _df.format(size));
							pool.put("fs-size", getFormattedSize(size));
							
							/*double zfssys = Double.parseDouble(pool.get("size-raw"))-size;
							if (_used > volused) {
								zfssys+=_used-volused;
							}
							pool.put("zfssys-raw", _df.format(zfssys));
							pool.put("zfssys", getFormattedSize(zfssys));*/
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error interpretando el espacio libre del pool zfs: {}. Ex: {}", new Object[]{pool.get("name"), ex.getMessage()});
			throw new Exception("Could not read zfs free size of "+pool.get("name"));
		}
	}
	
	public static void putPoolVolumesQuotaReservation(Map<String, String> pool) throws Exception {
		List<Map<String, String>> volumes = getVolumes(pool.get("name"), false);
		Double size = Double.parseDouble(pool.get("size-raw"));
		
		Double quota = 0D;
		Double reservation = 0D;
		Double used = 0D;
		for (Map<String, String> vol : volumes) {
			quota+=Double.parseDouble(vol.get("size-raw"));
			reservation+=Double.parseDouble(vol.get("reservation-raw"));
			used+=Double.parseDouble(vol.get("total-used-raw"));
		}
		
		pool.put("quota-raw", quota.toString());
		pool.put("quota", getFormattedSize(quota));
		pool.put("reservation-raw", reservation.toString());
		pool.put("reservation", getFormattedSize(reservation));
		
		pool.put("vol-used-raw", used.toString());
		pool.put("vol-used", getFormattedSize(used));
		
		double freequota = size-quota;
		if (freequota < 0)
			freequota = 0;
		pool.put("freequota-raw", String.valueOf(freequota));
		pool.put("freequota", getFormattedSize(freequota));
		
		double freereservation = size-reservation;
		if (freereservation < 0)
			freereservation = 0;
		pool.put("freereservation-raw", String.valueOf(freereservation));
		pool.put("freereservation", getFormattedSize(freereservation));
		
		putFreeSizePool(pool);
		putPoolDedupInfo(pool);
		/*Double zfssys = Double.parseDouble(pool.get("zfssys-raw")) + freereservation - Double.parseDouble(pool.get("free-raw"));
		pool.put("zfssys-raw", String.valueOf(zfssys));
		pool.put("zfssys", getFormattedSize(zfssys));*/
	}
	
	public static Map<String, String> readVolumeParameters(String output) throws Exception {
		Map<String, String> vol = new HashMap<String, String>();
		StringTokenizer _st = new StringTokenizer(output, "\n");
		double _used = 0D, _size = 0D;
		String val = "";
		String valraw = "";
		String type = "filesystem";
		while (_st.hasMoreTokens()) {
			String property = null;
			String value = null;
			String line = _st.nextToken();
			StringTokenizer _st2 = new StringTokenizer(line);
			if (_st2.hasMoreTokens()) {	// Name
				String name = _st2.nextToken();
				if (name.contains("/")) {												
					String tmp = name.substring(name.indexOf("/")+"/".length());		
					if (tmp.contains("/") || tmp.contains("@"))												// Snapshots
						return null;
				} else if (!name.contains("/") || name.contains("@"))					// Aggregates and snapshots
					return null;
			}
			if (_st2.hasMoreTokens())	// Property
				property = _st2.nextToken();
			if (_st2.hasMoreTokens())	// Value
				value = _st2.nextToken();
			
			if (property.equals("origin")) {
				if (!value.equals("-"))
					return null;
			}
			
			if (value.equals("-"))
				value = "0";
			
			if (property.equals("type"))
				type = value;
			else if (property.equals("quota") || property.equals("reservation") || property.equals("refreservation") || property.equals("refquota") || property.equals("available") || property.equals("volsize")) {
				if (property.equals("volsize") && type.equals("volume")) {
					_size = getSize(value);
					val = getFormattedSize(getSize(value));
					valraw = String.valueOf(getSize(value));
					vol.put("size", val);
					vol.put("size-raw", valraw);
				} else {
					if (property.equals("quota") && !type.equals("volume")) {
						_size = getSize(value);
						property = "size";
					}
					else if (property.equals("available") && !type.equals("volume")) {
						property = "free";
					}
					
					val = "0 B";
					valraw = "0";	
					if (property.equals("refreservation") && (value.equals("none")) && vol.get("reservation") != null) {
						val = vol.get("reservation");
						valraw = vol.get("reservation-raw");
						vol.put("norefs", "true");
					} else if (property.equals("refquota") && (value.equals("none")) && vol.get("size") != null) {
						val = vol.get("size");
						valraw = vol.get("size-raw");
						vol.put("norefs", "true");
					} else if (!value.equals("none") && !value.equals("-")) {
						val = getFormattedSize(getSize(value));
						valraw = String.valueOf(getSize(value));
					}
					vol.put(property, val);
					vol.put(property+"-raw", valraw);
				}
			} else if (property.equals("usedbydataset") || property.equals("usedbysnapshots")  || property.equals("used") || property.equals("usedbyrefreservation")) {
				if (property.equals("usedbydataset")) {
					property = "used";
					_used = getSize(value);
				} else if (property.equals("used")) {
					property = "total-used";
				} else if (property.equals("usedbysnapshots")) {
					property = "snapshot-used";
				} else if (property.equals("usedbyrefreservation")) {
					property = "refreservation-used";
				}
				vol.put(property, getFormattedPercent((getSize(value) * 100D) / _size));
				vol.put(property+"-raw", String.valueOf(value));
			} else if (property.equals("compression")) {
				if (!value.equals("off")) {
					vol.put(property, value);
				} else {
					vol.put(property, "none");
				}
			} else if (property.equals("compressratio")) {
				property="compressed";
				if (value.endsWith("x") && vol.get("compression") != null && !vol.get("compression").equals("none")) {
					String ratio = value.substring(0, value.indexOf("x"));
					if (ratio != null && !ratio.isEmpty()) {
						vol.put("compressed-ratio", ratio);
						double _ratio_n = Double.parseDouble(ratio);
						if(_used > 0) {
							_ratio_n = ((_used * _ratio_n) - _used) * 100 / (_used * _ratio_n);
						} else {
							_ratio_n = 0;
						}
						
						vol.put(property, getFormattedPercent(_ratio_n));
						vol.put(property+"-raw", String.valueOf(_ratio_n));
					} else {
						vol.put("compressed-ratio", "0");
						vol.put(property, "0 %");
						vol.put(property+"-raw", "0");
					}
					
				} else {
					vol.put("compressed-ratio", "0");
					vol.put(property, "0 %");
					vol.put(property+"-raw", "0");
				}
			} else if (property.equals("dedup")) {
				vol.put(property, value);
			}
		}
		if (type.equals("volume")) {
			double free = Double.parseDouble(vol.get("size-raw")) - Double.parseDouble(vol.get("used-raw")) - Double.parseDouble(vol.get("snapshot-used-raw"));
			val = getFormattedSize(getSize(free+" B"));
			valraw = String.valueOf(free);
			vol.put("free", val);
			vol.put("free-raw", valraw);
		}
		vol.put("zfstype", type);
		return vol;
	}
	
	public static void recoverDamagedGroup(String name) throws Exception {
    	logger.info("Intentando recuperar el aggregado dañado {}", name);
    	if (name != null) {
    		if (!name.isEmpty()) {
    			name = name.trim();
		    	StringBuilder _sb = new StringBuilder();
		    	_sb.append("/sbin/zpool export ");
		    	_sb.append(name);
		    	try {
					Command.systemCommand(_sb.toString());
				} catch(Exception _ex) {
					logger.error("Fallo en recuperación de agregado {}. Fallo en export. Ex: {}", name, _ex.getMessage());
				}
		    	
		    	_sb = new StringBuilder();
		    	_sb.append("/sbin/zpool import -f ");
		    	_sb.append(name);
		    	try {
					Command.systemCommand(_sb.toString());
				} catch(Exception _ex) {
					logger.error("No se pudo recuperar {}. Fallo en import. Ex: {}", name, _ex.getMessage());
					throw new Exception ("Unable to recover group "+name+". Ex: "+_ex.getMessage());
				}
    		}
    	}
    }
	
	public static boolean remoteFileSystemSnapshotExists(String server, String group, String name, String snapshot) throws Exception {
		if(group == null || name == null || snapshot == null ||
				group.isEmpty() || name.isEmpty() || snapshot.isEmpty()) {
			return false;
		}
		
		StringBuilder _volume = new StringBuilder();
		_volume.append(group);
		_volume.append("/");
		_volume.append(name);
		_volume.append("/");
		_volume.append(snapshot);
		
		StringBuilder _sb = new StringBuilder();
		_sb.append(getRemoteCommandPrefix(server, group, name));
		_sb.append("sudo /sbin/zfs list -H -o name");
		String _output = Command.systemCommand(_sb.toString());
		BufferedReader _br = new BufferedReader(new StringReader(_output));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.trim().equals(_volume.toString())) {
					return true;
				}
			}
		} finally {
			_br.close();
		}
		return false;
	}
	
	public static String removeFileSystem(String group, String name) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		String output = null;
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs destroy -rf ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		try {
			if (!Command.isRunning(_sb.toString())) {
				output = Command.systemCommand(_sb.toString());
			}
		} catch(Exception _ex) {
			int _time_wait=250;
			int _retrys=8;
			boolean _success=false;
			
			while (_retrys>0 && !_success) {
				try {
					Thread.sleep(_time_wait);
					if (!Command.isRunning(_sb.toString())) {
						output = Command.systemCommand(_sb.toString());
						_success=true;
					}
				} catch (Exception ex) {
					if (!Command.isRunning(_sb.toString())) {
						_retrys--;
					}
				}
			}
			if (!_success)
				throw new Exception("cannot remove filesystem  - " + _ex.getMessage());
			else
				return output;
		}
		return output;
	}
	
	public static void removeFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("/sbin/zfs release -r keep ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			_sb.append("@");
			_sb.append(snapshot);
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {}
		Thread.sleep(1000);
		umountFileSystemSnapshot(group, name, snapshot);
		Thread.sleep(1000);
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs destroy -R ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append("@");
		_sb.append(snapshot);
		Command.systemCommand(_sb.toString());
		
		try {
			new File(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot)).delete();
		} catch(Exception _ex) {}
	}
	
	
	public static void removePookDisk(String groupname, String device) throws Exception {
		try  {
			if (groupname != null && !groupname.isEmpty() && device != null && !device.isEmpty()) {
				Map<String, String> group = VolumeManager.getVolumeGroup(groupname);
				if (group == null)
					throw new Exception("Volume group "+groupname+" does not exists");
				if (!group.get("type").equals("zfs"))
					throw new Exception("Volume group "+groupname+" it is not a deduplication group");
				
				if (device.contains("dev/"))
					device = device.substring(device.indexOf("dev/")+"dev/".length());
				
				StringBuilder _sb = new StringBuilder();
				_sb.append("/sbin/zpool remove ");
				_sb.append(groupname);
				_sb.append(" ");
				_sb.append(device);
				Command.systemCommand(_sb.toString());
				logger.info("Disk [{}] removed of pool [{}] successfully", device, groupname);
			}
		} catch (Exception ex) {
			String msg = "Error removing disk ["+device+"] of ["+groupname+"].Ex:"+ex.getMessage();
			logger.error(msg);
			throw new Exception(msg);
		}
	}
	
	
	public static void removePool(String group) throws Exception {
		if(!poolExists(group)) {
			throw new Exception("filesystem does not exists");
		}
		
		Thread.sleep(500);
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zpool destroy -f ");
		_sb.append(group);
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			int _time_wait=250;
			int _retrys=8;
			boolean _success=false;
			
			while (_retrys>0 && !_success) {
				try {
					Thread.sleep(_time_wait);
					Command.systemCommand(_sb.toString());
					_success=true;
				} catch (Exception ex) {
					_retrys--;	
				}
			}
			if (!_success)
				throw new Exception("cannot remove filesystem pool - " + _ex);
		}
	}
	
	public static void removeRemoteFileSystemSnapshot(String server, String group, String name, String snapshot, CommandManager cm) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		
		String _sbRemote = getRemoteCommandPrefix(server, group, name);
		
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append(_sbRemote);
			_sb.append("sudo /sbin/zfs release -r keep ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			_sb.append("@");
			_sb.append(snapshot);
			cm.execute(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON);
		} catch(Exception _ex) {}
		
		Thread.sleep(1000);
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append(_sbRemote);
			_sb.append("sudo /sbin/zfs destroy -fr ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			_sb.append("/");
			_sb.append(snapshot);
			cm.execute(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON);
		} catch(Exception _ex) {}
		Thread.sleep(1000);
		
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append(_sbRemote);
			_sb.append("sudo /sbin/zfs destroy -R ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			_sb.append("@");
			_sb.append(snapshot);
			cm.execute(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON);
		} catch(Exception _ex) {}
			
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append(_sbRemote);
			_sb.append("sudo rm -r");
			_sb.append(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot));
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {}
	}
	
	/**
	 * Ejecuta una replicacion zfs TOTAL, es decir, el primer send-recevie de un proceso de replica
	 * @param group
	 * @param name
	 * @param snapshot
	 * @param server
	 * @param password
	 * @param keyName
	 * @param dgroup
	 * @param dname
	 * @throws Exception
	 */
	public static void replicateFileSystemSnapshotFirst(String group, String name, String snapshot, String server, String password, String dgroup, String dname, CommandManager cm) throws Exception {
		if(!fileSystemSnapshotExists(group, name, snapshot)) {
			throw new Exception("source filesystem snapshot does not exists");
		}
		String pathDebugLog = "/tmp/repFSLog-"+group+"-"+name+"-"+snapshot+".log";
		
		// Si existe en el destino un snapshot de ese volumen => copia incremental a partir de ese snapshot
		StringBuilder _sb = new StringBuilder();
		_sb.append("set -o pipefail && /sbin/zfs send -Dv ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append("@");
		_sb.append(snapshot);
		_sb.append(" | /sbin/mbuffer -q -s 128k -m "+getMBufferMemory()+" 2>/dev/null ");
		_sb.append(" | ssh -x -oStrictHostKeyChecking=no -oCheckHostIP=no -i /var/.ssh/"+getReplicationKeyName(dgroup, dname)+" ");
		_sb.append(getFilesystemReplicationUser(dgroup, dname));
		_sb.append("@");
		_sb.append(server);
		_sb.append(" sudo \"");
		_sb.append(getFileSystemRemoteReplicationCommand(dgroup, dname));
		_sb.append(" | /sbin/mbuffer -q -s 128k -m "+getMBufferMemory()+" 2>/dev/null \"");
		_sb.append(" | tee ");
		_sb.append(pathDebugLog);
    	try {
    		long init = new Date().getTime();
			while (ObjectLock.isBlock(ObjectLock.SNAPSHOTS_TYPE_OBJECT, snapshot, null) && (new Date().getTime() - init) < ObjectLock.getTimeTypeObject(ObjectLock.SNAPSHOTS_TYPE_OBJECT) );
			ObjectLock.block(ObjectLock.SNAPSHOTS_TYPE_OBJECT, snapshot, null);
			
			boolean launch = false;
			if(!cm.isRunning(_sb.toString())) {
				launch = true;
			} else {
				int interval = ReplicationManager.DEFAULT_MAX_EXECUTION_TIME;
				Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
				if (_c.hasProperty("rsync_interval"))
					interval = Integer.parseInt(_c.getProperty("rsync_interval"));
				List<CommandEntry> commands = cm.findCommandsByCommandStringAndStatus(_sb.toString(), CommandManager.STATUS_COMAND_RUNING);
				for (CommandEntry com : commands) {
					if (com.getDateLaunched() != null) {
						if((Calendar.getInstance().getTimeInMillis() - com.getDateLaunched().getTime()) > interval) {
							launch = true;
							ReplicationManager.killRunningSyncronizationsOf(com);
							cm.updateUnfinishedCommand(com);
						}
					}
				}
			}
			
			if (launch) {
				logger.debug("Lanazmos replica total (primera) zfs: {}", _sb.toString());
				cm.asyncExecuteAndWait(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON, null);
			}
		} catch(Exception _ex) {
			logger.error("Error lanzando replica total (primera) zfs. Ex: {}", _ex.getMessage());
			throw new Exception("replication error - " + _ex.getMessage());
		} finally {
			ObjectLock.unblock(ObjectLock.SNAPSHOTS_TYPE_OBJECT, snapshot, null);
			if (new File(pathDebugLog).exists())
				new File(pathDebugLog).delete();
		}
	}
	
	
	/**
	 * Ejecuta una replica zfs INCREMENTAL, es decir, un send-receive despues de tener una copia total base
	 * @param group
	 * @param name
	 * @param snapshotBase
	 * @param newSnapshot
	 * @param server
	 * @param password
	 * @param keyName
	 * @param dgroup
	 * @param dname
	 * @throws Exception
	 */
	public static void replicateFileSystemSnapshotIncremental(String group, String name, String snapshotBase, String newSnapshot, String server, String password, String dgroup, String dname, CommandManager cm) throws Exception {
		if(!fileSystemSnapshotExists(group, name, snapshotBase) || !fileSystemSnapshotExists(group, name, newSnapshot)) {
			throw new Exception("source filesystem snapshot does not exists");
		}
		
		String pathDebugLog = "/tmp/repFSLog-"+group+"-"+name+"-"+newSnapshot+".log";
		
		// Si existe en el destino un snapshot de ese volumen => copia incremental a partir de ese snapshot
		StringBuilder _sb = new StringBuilder();
		_sb.append("set -o pipefail && /sbin/zfs send -DvI ");
		_sb.append(snapshotBase);
		_sb.append(" ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(name);
		_sb.append("@");
		_sb.append(newSnapshot);
		_sb.append(" | /sbin/mbuffer -q -s 128k -m "+getMBufferMemory()+" 2>/dev/null ");
		_sb.append(" | ssh -x -oStrictHostKeyChecking=no -oCheckHostIP=no -i /var/.ssh/"+getReplicationKeyName(dgroup, dname)+" ");
		_sb.append(getFilesystemReplicationUser(dgroup, dname));
		_sb.append("@");
		_sb.append(server);
		_sb.append(" sudo \"");
		_sb.append(getFileSystemRemoteReplicationCommand(dgroup, dname));
		_sb.append(" | /sbin/mbuffer -q -s 128k -m "+getMBufferMemory()+" 2>/dev/null \"");
		_sb.append(" | tee ");
		_sb.append(pathDebugLog);
    	try {
    		long init = new Date().getTime();
			while (ObjectLock.isBlock(ObjectLock.SNAPSHOTS_TYPE_OBJECT, newSnapshot, null) && (new Date().getTime() - init) < ObjectLock.getTimeTypeObject(ObjectLock.SNAPSHOTS_TYPE_OBJECT) );
			ObjectLock.block(ObjectLock.SNAPSHOTS_TYPE_OBJECT, newSnapshot, null);
			
			boolean launch = false;
			if(!cm.isRunning(_sb.toString())) {
				launch = true;
			} else {
				int interval = ReplicationManager.DEFAULT_MAX_EXECUTION_TIME;
				Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
				if (_c.hasProperty("rsync_interval"))
					interval = Integer.parseInt(_c.getProperty("rsync_interval"));
				List<CommandEntry> commands = cm.findCommandsByCommandStringAndStatus(_sb.toString(), CommandManager.STATUS_COMAND_RUNING);
				for (CommandEntry com : commands) {
					if (com.getDateLaunched() != null) {
						if((Calendar.getInstance().getTimeInMillis() - com.getDateLaunched().getTime()) > interval) {
							launch = true;
							ReplicationManager.killRunningSyncronizationsOf(com);
							cm.updateUnfinishedCommand(com);
						}
					}
				}
			}
			
			if (launch) {
				logger.debug("Lanazmos replica incremental zfs: {}", _sb.toString());
				cm.asyncExecuteAndWait(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON, null);
			}
		} catch(Exception _ex) {
			logger.error("Error lanzando replica incremental. Ex: {}", _ex.getMessage());
			throw new Exception("replication error - " + _ex.getMessage());
		} finally {
			ObjectLock.unblock(ObjectLock.SNAPSHOTS_TYPE_OBJECT, newSnapshot, null);
			if (new File(pathDebugLog).exists())
				new File(pathDebugLog).delete();
		}
	}
	
	public static void resizeFileSystemInit(String group, String name, double size, double percent_snap) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		double percent = 1 - percent_snap/100;
		
		double refreservation = percent*size;
		double refquota = percent*size;
		
		setVolumeParams(group, name, size, refquota, size, refreservation);
	}
	
	/**
	 * Ejecuta un rollback en destino para replicar el volumen origen
	 * @param snapshot
	 * @param server
	 * @param keyName
	 * @param dgroup
	 * @param dvolume
	 * @throws Exception
	 */
	public static void runRollBack(String snapshot, String server, String dgroup, String dvolume, CommandManager cm) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append(getRemoteCommandPrefix(server, dgroup, dvolume));
		_sb.append("sudo /sbin/zfs rollback ");
		_sb.append(dgroup);
		_sb.append("/");
		_sb.append(dvolume);
		_sb.append("@");
		_sb.append(snapshot);
		try {			
			boolean launch = false;
			if(!cm.isRunning(_sb.toString())) {
				launch = true;
			} else {
				int interval = ReplicationManager.DEFAULT_MAX_EXECUTION_TIME;
				Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
				if (_c.hasProperty("rsync_interval"))
					interval = Integer.parseInt(_c.getProperty("rsync_interval"));
				List<CommandEntry> commands = cm.findCommandsByCommandStringAndStatus(_sb.toString(), CommandManager.STATUS_COMAND_RUNING);
				for (CommandEntry com : commands) {
					if (com.getDateLaunched() != null) {
						if((Calendar.getInstance().getTimeInMillis() - com.getDateLaunched().getTime()) > interval) {
							launch = true;
							ReplicationManager.killRunningSyncronizationsOf(com);
							cm.updateUnfinishedCommand(com);
						}
					}
				}
			}
			
			if (launch) {
				logger.debug("Lanzamos rollback zfs en destino: {}", _sb.toString());
				cm.asyncExecuteAndWait(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON, null);
			}
		} catch(Exception ex) {
			logger.error("Error lanzando rollback zfs en destino. Ex: {}", ex.getMessage());
		}
	}
	
	public static Map<String, Map<String, String>> searchGroupQuotas(String match, String group, String volume) throws Exception {
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		Map<String, Map<String, String>> _groups = new HashMap<String, Map<String, String>>();
		if(mountPath == null || mountPath.isEmpty()) {
			return _groups;
		}
		if(match == null) {
			match = "";
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs groupspace ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(volume);
		try {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.startsWith("PROP")) {
						continue;
					}
					StringTokenizer _st = new StringTokenizer(_line);
					if(!_st.hasMoreTokens()) {
						continue;
					}
					_st.nextToken(); // type
					String _object_type = _st.nextToken();
					if(!"group".equals(_object_type)) {
						continue;
					}
					String _groupname = _st.nextToken();
					double _size = getSize(_st.nextToken());
					if(!match.isEmpty() && !_groupname.startsWith(match)) {
						continue;
					} else if(_groupname.equalsIgnoreCase("root") ||
							_groupname.equalsIgnoreCase("tape") ||
							 _groupname.equalsIgnoreCase("bacula")) {
						continue;
					}
					Map<String, String> _group;
					if(_groups.containsKey(_groupname)) {
						_group = _groups.get(_groupname);
					} else {
						_group = new HashMap<String, String>();
					}
					double _quota = getSize(_st.nextToken());
					_group.put("used", getFormattedSize(_size));
					_group.put("hard", getFormattedSize(_quota));
					_group.put("soft", getFormattedSize(_quota));
					_groups.put(_groupname, _group);
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read group quotas : " + _ex.getMessage());
		}
		return _groups;
	}
	
	public static Map<String, Map<String, String>> searchUserQuotas(String match, String group, String volume) throws Exception {
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		Map<String, Map<String, String>> _users = new HashMap<String, Map<String, String>>();
		if(mountPath == null || mountPath.isEmpty()) {
			return _users;
		}
		if(match == null) {
			match = "";
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs userspace ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(volume);
		try {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_users.size() > 10) {
						break;
					}
					if(_line.startsWith("PROP")) {
						continue;
					}
					StringTokenizer _st = new StringTokenizer(_line);
					if(!_st.hasMoreTokens()) {
						continue;
					}
					_st.nextToken(); //type
					String _object_type = _st.nextToken();
					if(!"user".equalsIgnoreCase(_object_type)) {
						continue;
					}
					String _username = _st.nextToken();
					double _size = getSize(_st.nextToken());
					if(!match.isEmpty() && !_username.startsWith(match)) {
						continue;
					} else if(_username.equalsIgnoreCase("root") || _username.equalsIgnoreCase("bacula")) {
						continue;
					}
					Map<String, String> _user;
					if(_users.containsKey(_username)) {
						_user = _users.get(_username);
					} else {
						_user = new HashMap<String, String>();
					}
					double _quota = getSize(_st.nextToken());
					_user.put("used", getFormattedSize(_size));
					_user.put("hard", getFormattedSize(_quota));
					_user.put("soft", getFormattedSize(_quota));
					_users.put(_username, _user);
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read user quotas : " + _ex.getMessage());
		}
		return _users;
	}
	
	public static void setCompression(String group, String name, String compression) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		StringBuilder _sb = new StringBuilder();
		if(compression != null && "gzip".equalsIgnoreCase(compression)) {
			_sb.append("/sbin/zfs set compression=gzip-9 ");
			_sb.append(group);;
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot set filesystem compression - " + _ex.getMessage());
			}
		} else if(compression != null && "lzjb".equalsIgnoreCase(compression)) {
			_sb.append("/sbin/zfs set compression=lzjb ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot set filesystem compression - " + _ex.getMessage());
			}
		} else if(compression != null && "zle".equalsIgnoreCase(compression)) {
			_sb.append("/sbin/zfs set compression=zle ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot set filesystem compression - " + _ex.getMessage());
			}
		} else {
			_sb.append("/sbin/zfs set compression=off ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			try {
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				throw new Exception("cannot unset filesystem compression - " + _ex.getMessage());
			}
		}
	}
	 
	public static void setGroupQuota(String groupname, String group, String volume, double size) throws Exception {
		if(!UserManager.systemGroupExists(groupname)) {
			throw new Exception("group does not exists");
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs set groupquota@");
		_sb.append(groupname);
		_sb.append("=");
		if(size > 0) {
			_sb.append(getFormattedInternalSize(size));
		} else {
			_sb.append("0");
		}
		_sb.append(" ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(volume);
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			throw new Exception("cannot write group quotas : " + _ex.getMessage());
		}
	}
	
	public static void setPoolCacheDisks(String groupname, List<String> disks) throws Exception {
		try  {
			if (groupname != null && !groupname.isEmpty() && disks != null && !disks.isEmpty()) {
				Map<String, String> group = VolumeManager.getVolumeGroup(groupname);
				if (group == null)
					throw new Exception("Volume group "+groupname+" does not exists");
				if (!group.get("type").equals("zfs"))
					throw new Exception("Volume group "+groupname+" it is not a deduplication group");
				
				StringBuilder _sb = new StringBuilder();
				_sb.append("/sbin/zpool add -f ");
				_sb.append(groupname);
				_sb.append(" cache");
				for (String disk : disks) {
					_sb.append(" ");
					_sb.append(disk);
				}
			
				Command.systemCommand(_sb.toString());
			}
		} catch (Exception ex) {
			String msg = "Error setting pool cache disks on ["+groupname+"].Ex:"+ex.getMessage();
			logger.error(msg);
			throw new Exception(msg);
		}
	}
	
    public static void setPoolLogDisks(String groupname, List<String> disks) throws Exception {
		try  {
			if (groupname != null && !groupname.isEmpty() && disks != null && !disks.isEmpty()) {
				Map<String, String> group = VolumeManager.getVolumeGroup(groupname);
				if (group == null)
					throw new Exception("Volume group "+groupname+" does not exists");
				if (!group.get("type").equals("zfs"))
					throw new Exception("Volume group "+groupname+" it is not a deduplication group");
				
				StringBuilder _sb = new StringBuilder();
				_sb.append("/sbin/zpool add -f ");
				_sb.append(groupname);
				_sb.append(" log");
				for (String disk : disks) {
					_sb.append(" ");
					_sb.append(disk);
				}
			
				Command.systemCommand(_sb.toString());
			}
		} catch (Exception ex) {
			String msg = "Error setting pool log disks on ["+groupname+"].Ex:"+ex.getMessage();
			logger.error(msg);
			throw new Exception(msg);
		}
	}
    
    public static void setUserQuota(String username, String group, String volume, double size) throws Exception {
		if(!UserManager.systemUserExists(username)) {
			throw new Exception("user does not exists");
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/zfs set userquota@");
		_sb.append(username);
		_sb.append("=");
		if(size > 0) {
			_sb.append(getFormattedInternalSize(size));
		} else {
			_sb.append("0");
		}
		_sb.append(" ");
		_sb.append(group);
		_sb.append("/");
		_sb.append(volume);
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			throw new Exception("cannot write user quotas : " + _ex.getMessage());
		}
	}
    
    private static void setVolumeParams(String group, String name, double quota, double refquota, double reservation, double refreservation) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		try {
			StringBuilder _sb = new StringBuilder();
			Map<String, String> vol = getVolume(group, name, false);
			
			double current_quota = Double.parseDouble(vol.get("size-raw"));
			double current_refquota = Double.parseDouble(vol.get("refquota-raw"));
			if(quota > current_quota) {
				_sb.append(getZFSParamCommand(group, name, "quota", quota));
				_sb.append(" && ");
				_sb.append(getZFSParamCommand(group, name, "refquota", refquota));
				_sb.append(getZFSReservationCommands(vol, reservation, refreservation, true));
			} else if (refquota > current_refquota ){
				_sb.append(getZFSParamCommand(group, name, "refquota", refquota));
				_sb.append(" && ");
				_sb.append(getZFSParamCommand(group, name, "quota", quota));
				_sb.append(getZFSReservationCommands(vol, reservation, refreservation, true));	
			} else {
				_sb.append(getZFSReservationCommands(vol, reservation, refreservation, false));	
				if (quota < current_quota) {
					if (_sb.length()>0)
						_sb.append(" && ");
					_sb.append(getZFSParamCommand(group, name, "refquota", refquota));
					_sb.append(" && ");
					_sb.append(getZFSParamCommand(group, name, "quota", quota));
				} else if (current_refquota != refquota){
					if (_sb.length()>0)
						_sb.append(" && ");
					_sb.append(getZFSParamCommand(group, name, "refquota", refquota));
				}
			}
	
			Command.systemCommand(_sb.toString());
			logger.info("Volumen ZFS aplicamos tamaños y cuotas: {}",_sb.toString());
		} catch(Exception _ex) {
			logger.error("cannot change filesystem size - {}", _ex.getMessage());
			throw new Exception("cannot change filesystem size - " + _ex.getMessage());
		}
	}
	
	private static void setZVolParams(String group, String name, double size, double reservation, double refreservation) throws Exception {
		if(!fileSystemExists(group, name)) {
			throw new Exception("filesystem does not exists");
		}
		
		try {
			StringBuilder _sb = new StringBuilder();
			Map<String, String> vol = getVolume(group, name, false);
			_sb.append(getZFSParamCommand(group, name, "volsize", size));
			logger.info("Volumen ZFS aplicamos tamaño: {}",_sb.toString());
			Command.systemCommand(_sb.toString());
			_sb = new StringBuilder();
			_sb.append(getZFSReservationCommands(vol, reservation, refreservation, false));
			logger.info("Volumen ZFS aplicamos reservas: {}",_sb.toString());
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			logger.error("cannot change filesystem size - --- {}", _ex.getMessage());
			throw new Exception("cannot change filesystem size - " + _ex.getMessage());
		}
	}
	
	public static void umountFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		
		if(fileSystemSnapshotExists(group, name, snapshot)) {
			StringBuilder _sb = new StringBuilder();
			_sb.append("/sbin/zfs destroy -fr ");
			_sb.append(group);
			_sb.append("/");
			_sb.append(name);
			_sb.append("/");
			_sb.append(snapshot);
			try {
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				System.out.println("ZFSConfiguration::umountFileSystemSnapshot:error - " + _ex.getMessage());
				throw new Exception("cannot remove snapshot filesystem clone");
			}
		}
	}
	
	public static String getGroupCompressRatio(List<Map<String, String>> vols) throws Exception {
		try {
			double ratio = 0d;
			double numVols = 0d;
			if (vols != null && vols.size() > 0) {
				for (Map<String, String> vol : vols) {
					if (vol.get("compression") != null && !vol.get("compression").equals("off")) {
						if (vol.get("compressed-ratio") != null) {
							double r = Double.parseDouble(vol.get("compressed-ratio"));
							if (r > 0d) {
								ratio += r;
								numVols++;
							}
						}
					}
				}
				if (ratio > 0) {
					ratio = ratio / numVols;
					DecimalFormat _df = new DecimalFormat("#.##");
					_df.setDecimalSeparatorAlwaysShown(false);
					return _df.format(ratio);
				}
			}
			return "1.0";
		} catch (Exception ex) {
			logger.error("Error getting group compress ratio for {}", vols);
			return "1.0";
		}
	}
}
