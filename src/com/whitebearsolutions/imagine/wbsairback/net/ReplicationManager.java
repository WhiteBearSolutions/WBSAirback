package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.UserManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.ZFSConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.imagine.wbsairback.mongodb.CommandEntry;
import com.whitebearsolutions.imagine.wbsairback.mongodb.CommandManager;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class ReplicationManager {
	private Map<String, Map<String, String>> _destinations;
	private Map<String, Map<String, String>> _sources;
	private final static String _prefix = "LOGICAL-VOLUME-REPLICATION-";
    private static File _replication_directory;
    private static CommandManager cm;
    
    private final static Logger logger = LoggerFactory.getLogger(ReplicationManager.class);
    public final static int DEFAULT_MAX_EXECUTION_TIME = 7200000;
    public final static int DEFAULT_RETENTION = 172800000;
    public final static int DEFAULT_LAUNCHING_INTERVAL = 43200000;
    
    public final static String TYPE_COMMAND_REPLICATON = "syncronization";
    
    public final static String pathLaunch = "/tmp/replication.control";
    
    static {
    	_replication_directory = new File(WBSAirbackConfiguration.getDirectoryReplication());
        if(!_replication_directory.exists()) {
        	_replication_directory.mkdirs();
        }
        if (cm == null)
			cm = new CommandManager();
    }
	
	public ReplicationManager() throws Exception {
		this._destinations = new HashMap<String, Map<String,String>>();
		this._sources = new HashMap<String, Map<String,String>>();
		loadDestinations();
		loadSources();
		if (cm == null)
			cm = new CommandManager();
	}
	
	private boolean checkRemoteDestination(String address, String group, String volume, String password, boolean filesystem) {
		if(group == null || volume == null || group.isEmpty() || volume.isEmpty()) {
			return false;
		}
		String destination = group + "/" + volume;
		if(!filesystem) {
			StringBuilder _sb = new StringBuilder();
			_sb.append("/usr/bin/rsync sync@");
			_sb.append(address);
			_sb.append("::");
			try {
				BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(_line.contains(volume)) {
							return true;
						}
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {
				System.out.println("ReplicationManager::remote::error: " + _ex.getMessage());
			}
		} else {
			try {
				boolean connectionOk = false;
				BufferedReader _br = new BufferedReader(new StringReader(runRemoteCommand(address, getReplicationUser(group, volume), password, "sudo -l")));
				try {
					for(String _line = _br.readLine(); _line != null && !connectionOk; _line = _br.readLine()) {
						if(_line.contains(destination)) {
							connectionOk = true;
						}
					}
				} finally {
					_br.close();
				}
				// Seteamos ssh
				if (connectionOk == true) {
					setSshConfig(address, password, group, volume);
					return true;
				}	
			} catch(Exception _ex) {
				System.out.println("ReplicationManager::remote::error: " + _ex.getMessage());
			}
		}
		return false;
	}

	
	private static void setSshConfig(String hostDestino, String passDestino, String group, String volume) throws Exception {
		// Generamos la key en local, es decir, el origen de la copia
		File _sshDir = new File("/var/.ssh/");
		if (!_sshDir.exists())
			_sshDir.mkdirs();
		
		String keyName = ZFSConfiguration.getReplicationKeyName(group, volume);
		String userDestino = getReplicationUser(group, volume);
		
		try {
			File _key = new File("/var/.ssh/"+keyName);
			if (_key.exists())
				_key.delete();
			Command.systemCommand("ssh-keygen -f /var/.ssh/"+keyName+" -C 'ZFS Replication key' -N '' -t rsa -q");
		} catch (Exception ex){}
		// Enviamos la clave publica al host remoto, el destino
		try {
			runSshCopyId("/var/.ssh/"+keyName+".pub", userDestino, passDestino, hostDestino);
		} catch (Exception ex){}
	}
	
	/**
	 * Ejecuta un comando de transferencia de ficheros remota mediante scp
	 * @param command
	 * @param user
	 * @param password
	 * @param host
	 * @return
	 * @throws Exception
	 */
	private static String runSshCopyId(String pathKey, String user, String password, String host) throws Exception {
        StringBuilder _command = new StringBuilder();
        StringBuilder _ssh_command = new StringBuilder();
        _ssh_command.append("ssh-copy-id -i ");
        _ssh_command.append(" ");
        _ssh_command.append(pathKey);
        _ssh_command.append(" ");
        _ssh_command.append(user);
        _ssh_command.append("@");
        _ssh_command.append(host);
        _ssh_command.append(":");
        _command.append("expect -c 'set timeout 120 ; spawn ");
        _command.append(_ssh_command.toString());
        _command.append(" ; expect \"*?assword:*\" {send \"");
        _command.append(password);
        _command.append("\\r\"} ; interact'");
        return Command.systemCommand(_command.toString());
	}
	
	
	private static void checkReplicationUser(String group, String volume, String password) throws Exception {
		String _user = getReplicationUser(group, volume);
		File _f = new File("/rdata/replication/" + _user);
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		if(UserManager.systemUserExists(_user)) {
			UserManager.removeSystemUser(_user, false);
		}
		UserManager.addSystemUser(_user, "WBSAirback replication user", _f.getAbsolutePath(), "/bin/bash", false);
		
		try {
			
			if(!UserManager.getRemotePasswordAccess()) {
				UserManager.setRemotePasswordAccess(true);
			}
			
			File _ssh = new File("/rdata/replication/" + _user +"/.ssh");
			if(!_ssh.exists()) {
				_ssh.mkdirs();
			}
			
			Command.systemCommand("chown "+_user+" /rdata/replication/" + _user +"/.ssh");
			Command.systemCommand("chmod 700 /rdata/replication/" + _user +"/.ssh");
			try {
				Command.systemCommand("rm -f /rdata/replication/" + _user +"/.ssh/*");
			} catch (Exception ex){}
		} catch(Exception _ex) {
			throw new Exception("cannot create replication user - " + _ex.getMessage());
		}
		if(password != null && !password.isEmpty()) {
			try {
				UserManager.changeSystemUserPassword(_user, password, false);
			} catch(Exception _ex) {
				throw new Exception("cannot update replication user password - " + _ex.getMessage());
			}
		}
	}
	
	private static void deleteReplicationUser(String group, String volume) throws Exception {
		String _user = getReplicationUser(group, volume);
		if(UserManager.systemUserExists(_user)) {
			UserManager.removeSystemUser(_user, false);
		}
	}
	
    private static List<Thread> getActiveThreads() {
        ThreadGroup _rootGroup = Thread.currentThread().getThreadGroup();
        for(ThreadGroup _parentGroup; (_parentGroup = _rootGroup.getParent()) != null; ) {
            _rootGroup = _parentGroup;
        }

        Thread[] _threads = new Thread[_rootGroup.activeCount()];
        for(int i = _rootGroup.enumerate(_threads, true); i >= _threads.length; i = _rootGroup.enumerate(_threads, true)) {
            _threads = new Thread[_threads.length + 1];
        }
        List<Thread> _threadList = new ArrayList<Thread>(Arrays.asList(_threads));
        _threadList.removeAll(Collections.singleton(null));
        return _threadList;
    }
	
	public List<Map<String, String>> getDestinations() {
		return new ArrayList<Map<String, String>>(this._destinations.values()); 
	}
	
    private static Calendar getLastModificationCalendar(File lastModificationFile) {
        Calendar _cal = null;
        if(lastModificationFile != null && lastModificationFile.isFile()) {
            ObjectInputStream _ois = null;
            try {
            	_ois = new ObjectInputStream(new FileInputStream(lastModificationFile));
		        for(Object _o = _ois.readObject(); _o != null; _o = _ois.readObject()) {
			        if(_o instanceof Calendar) {
		                _cal = (Calendar) _o;
		            }
		        }
            } catch(Exception _ex) {
            } finally {
                if(_ois != null) {
                    try {
                        _ois.close();
                    } catch(IOException _ex) {}
                }
            }
        }
        return _cal;
    }
    
    public static File getReplicationFile(String group, String volume, String server, String dgroup, String dvolume, String extension) {
        if(group == null || group.isEmpty() ||
        		volume == null || volume.isEmpty() ||
        		server == null || server.isEmpty() ||
				group == null || group.isEmpty() ||
        		volume == null || volume.isEmpty() ||
        		extension == null || extension.isEmpty()) {
                return null;
        }
        StringBuilder _sb = new StringBuilder();
        _sb.append(_replication_directory.getAbsolutePath());
        _sb.append("/");
        _sb.append(group);
        _sb.append("_");
        _sb.append(volume);
        _sb.append("_");
        _sb.append(server);
        _sb.append(".");
        _sb.append(extension);
        return new File(_sb.toString());
    }
    
    private static String getReplicationUser(String group, String volume) {
    	StringBuilder _user = new StringBuilder();
		_user.append(group);
		_user.append("_");
		_user.append(volume);
		return _user.toString();
    }
    
	
	public List<Map<String, String>> getSources() {
		return new ArrayList<Map<String, String>>(this._sources.values()); 
	}
	
	private static String getThreadName(String group, String volume, String server) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(_prefix);
		_sb.append(group);
		_sb.append("_");
		_sb.append(volume);
		_sb.append("_");
		_sb.append(server);
		return _sb.toString();
	}
	
    public static boolean isRunning(String group, String volume, String server) {
       String _threadName = getThreadName(group, volume, server);
        for(Thread _t : getActiveThreads()) {
            String _name = _t.getName();
            if(_name != null && _name.toLowerCase().equalsIgnoreCase(_threadName)) {
                    return true;
            }
        }
        return false;
	}
	
	private void loadDestinations() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getDirectorySync());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		_f = new File(WBSAirbackConfiguration.getDirectorySync() + "/destinations.xml");
        if(_f.exists()) {
			DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document _doc = _db.parse(_f);
			NodeList _nl = _doc.getElementsByTagName("destinations");
			for(int i = 0; i < _nl.getLength() ; i++) {
				Map<String, String> destinations = new HashMap<String, String>();
				NodeList _cnl = ((Element) _nl.item(i)).getChildNodes();
				for(int j = 0; j < _cnl.getLength() ; j++) {
					Element _e = (Element) _cnl.item(j);
					destinations.put(_e.getNodeName(), _e.getTextContent());
				}
				if(!destinations.containsKey("name")) {
					continue;
				}
				if(!destinations.containsKey("filesystem")) {
					destinations.put("filesystem", "no");
				}
				this._destinations.put(destinations.get("name")+"_"+destinations.get("address"), destinations);
			}
        }
	}
	
	private void loadSources() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getDirectorySync());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		_f = new File(WBSAirbackConfiguration.getDirectorySync() + "/sources.xml");
        if(_f.exists()) {
			DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document _doc = _db.parse(_f);
			NodeList _nl = _doc.getElementsByTagName("source");
			for(int i = 0; i < _nl.getLength() ; i++) {
				Map<String, String> _source = new HashMap<String, String>();
				NodeList _cnl = ((Element) _nl.item(i)).getChildNodes();
				for(int j = 0; j < _cnl.getLength() ; j++) {
					Element _e = (Element) _cnl.item(j);
					_source.put(_e.getNodeName(), _e.getTextContent());
				}
				if(!_source.containsKey("name")) {
					continue;
				}
				if(!_source.containsKey("mbs")) {
					_source.put("mbs", "0");
				}
				if(!_source.containsKey("delete")) {
					_source.put("delete", "no");
				} else if (_source.get("delete").equals("yes")){
					_source.put("delete", "yes");
				}
				if(!_source.containsKey("filesystem")) {
					_source.put("filesystem", "no");
				} else if (_source.get("filesystem").equals("yes")){
					_source.put("filesystem", "yes");
				}
				if(!_source.containsKey("checksum")) {
					_source.put("checksum", "no");
				} else if (_source.get("checksum").equals("yes")){
					_source.put("checksum", "yes");
				}
				if(!_source.containsKey("append")) {
					_source.put("append", "no");
				} else if (_source.get("append").equals("yes")){
					_source.put("append", "yes");
				}
				if(!_source.containsKey("compress")) {
					_source.put("compress", "no");
				} else if (_source.get("compress").equals("yes")){
					_source.put("compress", "yes");
				}
				if(!_source.containsKey("delta")) {
					_source.put("delta", "no");
				} else if (_source.get("delta").equals("yes")){
					_source.put("delta", "yes");
				}
					
				this._sources.put(_source.get("name")+"_"+_source.get("address"), _source);
			}
        }
	}
	
	public Map<String, String> getDestination(String group, String volume, String address) throws Exception {
		if(volume == null || group == null ||
				!volume.matches("[0-9a-zA-Z-._]+") ||
				!group.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid logical volume name");
		}
		
		String _name = group + "/" + volume + "_" + address;
		if(this._destinations.containsKey(_name)) {
			return this._destinations.get(_name);
		}
		return null;
	}
	
	public Map<String, String> getSource(String group, String volume, String address) throws Exception {
		if(volume == null || group == null ||
				!volume.matches("[0-9a-zA-Z-._]+") ||
				!group.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid logical volume name");
		}
		
		String _name = group + "/" + volume + "_" + address;
		if(this._sources.containsKey(_name)) {
			return this._sources.get(_name);
		}
		return null;
	}
	
	public void removeDestination(String group, String volume, String address) throws Exception {
		if(volume == null || group == null ||
				!volume.matches("[0-9a-zA-Z-._]+") ||
				!group.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid logical volume name");
		}
		
		String _name = group + "/" + volume + "_" + address;
		if(this._destinations.containsKey(_name)) {
			this._destinations.remove(_name);
		}
		writeDestinations();
		deleteReplicationUser(group, volume);
		_name = _name.replace("/", "@");
		File _f = new File(WBSAirbackConfiguration.getDirectorySync() + "/" + _name + ".conf");
		if (_f.exists())
			_f.delete();
	}
	
	public void removeSource(String group, String volume, String address) throws Exception {
		if(volume == null || group == null ||
				!volume.matches("[0-9a-zA-Z-._]+") ||
				!group.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid logical volume name");
		}
		
		String _name = group + "/" + volume + "_" + address;
		if(this._sources.containsKey(_name)) {
			Map<String, String> source = this._sources.get(_name); 
			StringBuilder _sb = new StringBuilder();
			_sb.append("/tmp/");
			_sb.append(source.get("address"));
			_sb.append("-");
			_sb.append(source.get("dest-vg"));
			_sb.append("@");
			_sb.append(source.get("dest-lv"));
			_sb.append(".sync");
			File _error_file = new File(_sb.toString());
			_sb = new StringBuilder();
			_sb.append("/tmp/");
			_sb.append(source.get("address"));
			_sb.append("-");
			_sb.append(source.get("dest-vg"));
			_sb.append("@");
			_sb.append(source.get("dest-lv"));
			_sb.append(".pwd");
			File _pwd_file = new File(_sb.toString());
			if (_error_file.exists())
				_error_file.delete();
			if (_pwd_file.exists())
				_pwd_file.delete();
			
			this._sources.remove(_name);
		}
		writeSources();
	}
	
	private static void replicateFileSystem(String group, String volume, Calendar lastDate, String server, String password, String dgroup, String dvolume, boolean forceSnapshot) throws Exception {
		if(!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, VolumeManager.getLogicalVolumeFS(group, volume))) {
			throw new Exception("filesystem type does not support low level replication");
		}
		
		if (forceSnapshot)
			VolumeManager.createLogicalVolumeSnapshot(group, volume, VolumeManager.LV_SNAPSHOT_DAILY);
		
		List<Map<String, String>> _snapshotsDestiny = ZFSConfiguration.getRemoteFileSystemSnapshots(server, dgroup, dvolume);
		List<Map<String, String>> _snapshotsLocal = ZFSConfiguration.getFileSystemSnapshots(group, volume);
		if (_snapshotsLocal != null && _snapshotsLocal.size()>0) {
			if (_snapshotsDestiny.size() != _snapshotsLocal.size()) {
				Map<String, String> baseSnapshot = null;
				int init = 0;
				if(_snapshotsDestiny != null && _snapshotsDestiny.size() > 0) {  // Ya hay replica, intentamos incremental
					// Rescatamos la copia base, la antigua, es decir, la última que se tenga en destino
					boolean found = false;
					int indexFound = -1;
					for(int i = _snapshotsDestiny.size()-1 ; i >= 0 && !found; i--) {
						indexFound = hasSnapshot(_snapshotsLocal, _snapshotsDestiny.get(i).get("name"));
						if(indexFound>-1) {
							baseSnapshot = _snapshotsLocal.get(indexFound);
							found = true;
							init = indexFound+1;
						} else {
							ZFSConfiguration.removeRemoteFileSystemSnapshot(server, dgroup, dvolume, _snapshotsDestiny.get(i).get("name"), cm);
						}
					}
				}
				
				if(baseSnapshot == null) {
					// Primera replicacion, la base para después es la que acabamos de replicar
					ZFSConfiguration.replicateFileSystemSnapshotFirst(group, volume, _snapshotsLocal.get(0).get("name"), server, password, dgroup, dvolume, cm);
					ZFSConfiguration.mountRemoteFileSystemSnapshot(server, dgroup, dvolume, _snapshotsLocal.get(0).get("name"), cm);
					baseSnapshot = _snapshotsLocal.get(0);
					init = 1;
				}
				
				// Replicamos ... Tras cada iteración, la base pasa a ser, la que se ha replicado
				if(init < _snapshotsLocal.size()) {
					ZFSConfiguration.replicateFileSystemSnapshotIncremental(group, volume, baseSnapshot.get("name"), _snapshotsLocal.get(_snapshotsLocal.size()-1).get("name"), server, password, dgroup, dvolume, cm);
					ZFSConfiguration.mountRemoteFileSystemSnapshot(server, dgroup, dvolume, _snapshotsLocal.get(_snapshotsLocal.size()-1).get("name"), cm);
					baseSnapshot = _snapshotsLocal.get(_snapshotsLocal.size()-1);
				}
				
				// Por ultimo, hacemos un rollback en el destino
				ZFSConfiguration.runRollBack(baseSnapshot.get("name"), server, dgroup, dvolume, cm);
				
				// Ahora copiamos los indices de origen a destino
				ZFSConfiguration.copySnapshotsIndexes(group, volume, server, dgroup, dvolume);
			}
		}
	}
	

	public static int hasSnapshot(List<Map<String, String>> snapshotsDestiny, String snapshotName) {
		if(snapshotsDestiny != null && snapshotsDestiny.size() > 0 && snapshotName != null && !snapshotName.equals("")) {
			for(Map<String, String> snap : snapshotsDestiny) {
				if(snap.get("name").equals(snapshotName)) {
					return snapshotsDestiny.indexOf(snap);
				}
			}				
		}
		return -1;
	}
	
	public void runAllSourceReplication(boolean forceSnapshot) throws Exception {
		for(Map<String, String> _source : this._sources.values()) {
			if(_source.get("filesystem") == null || "no".equalsIgnoreCase(_source.get("filesystem"))) {
				runReplication(_source);
			} else {
				runFileSystemReplication(_source, forceSnapshot);
			}
		}
		
		File lastLaunch = new File(pathLaunch);
		if (lastLaunch.exists())
			lastLaunch.delete();
		lastLaunch.createNewFile();
	}
	
	public static boolean hasToLaunch() throws Exception {
		File lastLaunch = new File(pathLaunch);
		int interval = DEFAULT_LAUNCHING_INTERVAL;
		Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		if (_c.hasProperty("rsync_launching_interval"))
			interval = Integer.parseInt(_c.getProperty("rsync_launching_interval"));
		if (lastLaunch.exists()) {
			if ( (new Date().getTime() - lastLaunch.lastModified()) < interval)
				return false;
		}
		return true;
	}
	
	private static void runFileSystemReplication(final Map<String, String> source, final boolean forceSnapshot) throws Exception {
		if(!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, VolumeManager.getLogicalVolumeFS(source.get("vg"), source.get("lv")))) {
			throw new Exception("filesystem type does not support low level replication");
		}
		Runnable r = new Runnable() {
			public void run() {
				
				File _f = getReplicationFile(source.get("vg"), source.get("lv"), source.get("address"), source.get("dest-vg"), source.get("dest-lv"), "cal");
				Calendar _lastDate = getLastModificationCalendar(_f);
				File _error_file = null;
				StringBuilder _sb = new StringBuilder();
				_sb.append("/tmp/");
				_sb.append(source.get("address"));
				_sb.append("-");
				_sb.append(source.get("dest-vg"));
				_sb.append("@");
				_sb.append(source.get("dest-lv"));
				_sb.append(".sync");
				
				
				try {
					boolean launch = false;
					List<CommandEntry> commands = cm.getZFSSendRunning(source.get("vg"), source.get("lv"));
					if (commands == null || commands.isEmpty())
						launch = true;
					else {
						int interval = ReplicationManager.DEFAULT_MAX_EXECUTION_TIME;
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
						if (_c.hasProperty("rsync_interval"))
							interval = Integer.parseInt(_c.getProperty("rsync_interval"));
						launch = true;
						for (CommandEntry com : commands) {
							if (com.getDateLaunched() != null) {
								if((Calendar.getInstance().getTimeInMillis() - com.getDateLaunched().getTime()) <= interval) {
									launch = false;
								} 
							}
						}
						if (launch) {
							for (CommandEntry com : commands) {
								ReplicationManager.killRunningSyncronizationsOf(com);
								cm.updateUnfinishedCommand(com);
							}
						}
					}
					
					if (launch) {
						_error_file = new File(_sb.toString());
						
						logger.info("Lanzamos replicacion de tipo filesystem ...");
						replicateFileSystem(source.get("vg"), source.get("lv"), _lastDate, source.get("address"), source.get("password"), source.get("dest-vg"), source.get("dest-lv"), forceSnapshot);
						
						if(_error_file.exists()) {
							_error_file.delete();
						}
					}
				
				} catch(Exception _ex) {
					logger.error("Error de replicacion filesystem: {}", _ex);
					if(!_error_file.exists()) {
						try {
							FileOutputStream _fos = new FileOutputStream(_error_file);
							try {
								_fos.write("unknown synchronization error".getBytes());
							}
							finally {
								_fos.close();
							}
						} catch(Exception _ex2) {}
					}
				}
			}
		};
		Thread internalThread = new Thread(r);
		internalThread.setName(getThreadName(source.get("vg"), source.get("lv"), source.get("address")));
		internalThread.start();
	}
	
	private static String runRemoteCommand(String host, String user, String password, String command) throws Exception {
        StringBuilder _command = new StringBuilder();
        StringBuilder _ssh_command = new StringBuilder();
        _ssh_command.append("ssh -x -oStrictHostKeyChecking=no -oCheckHostIP=no ");
        _ssh_command.append(user);
        _ssh_command.append("@");
        _ssh_command.append(host);
        _ssh_command.append(" ");
        _ssh_command.append(command);
        if(password != null && !password.isEmpty()) {
                _command.append("expect -c 'set timeout 60 ; spawn ");
                _command.append(_ssh_command.toString());
                _command.append(" ; expect \"*?assword:*\" ; send \"");
                _command.append(password);
                _command.append("\n\" ; interact' | sed '1,+1d'");
        } else {
                _command.append(_ssh_command.toString());
        }
        return Command.systemCommand(_command.toString());
	}
	
	private static void runReplication(final Map<String, String> source) throws Exception {
		File _error_file = null;
		StringBuilder _sb = new StringBuilder();
		_sb.append("/tmp/");
		_sb.append(source.get("address"));
		_sb.append("-");
		_sb.append(source.get("dest-vg"));
		_sb.append("@");
		_sb.append(source.get("dest-lv"));
		_sb.append(".sync");
		_error_file = new File(_sb.toString());
		_sb = new StringBuilder();
		_sb.append("/tmp/");
		_sb.append(source.get("address"));
		_sb.append("-");
		_sb.append(source.get("dest-vg"));
		_sb.append("@");
		_sb.append(source.get("dest-lv"));
		_sb.append(".pwd");
		File _pwd_file = new File(_sb.toString());
		try {
			FileSystem.writeFile(_pwd_file, source.get("password"));
			_sb = new StringBuilder();
			_sb.append("/bin/chmod 600 ");
			_sb.append(_pwd_file.getAbsolutePath());
			Command.systemCommand(_sb.toString());
			
			_sb = new StringBuilder();
			_sb.append("/usr/bin/rsync");
			if(source.get("mbs") != null && !"0".equals(source.get("mbs"))) {
				_sb.append(" --bwlimit=");
				_sb.append(source.get("mbs"));
			}
			_sb.append(" --password-file=");
			_sb.append(_pwd_file.getAbsolutePath());
			if(source.get("delete") != null && "yes".equals(source.get("delete"))) {
				_sb.append(" --delete");
			}
			if(source.get("checksum") != null && "yes".equals(source.get("checksum"))) {
				_sb.append(" --checksum");
			}
			if(source.get("compress") != null && "yes".equals(source.get("compress"))) {
				_sb.append(" --compress");
			}
			if(source.get("append") != null && "yes".equals(source.get("append"))) {
				_sb.append(" --append-verify --partial");
			} else if(source.get("delta") != null && "yes".equals(source.get("delta"))) {
				_sb.append(" --whole-file");
			}
			_sb.append(" --progress --archive --acls --xattrs --exclude .recycle --exclude @GMT-* ");
			_sb.append(VolumeManager.getLogicalVolumeMountPath(source.get("vg"), source.get("lv")));
			_sb.append("/ sync@");
			_sb.append(source.get("address"));
			_sb.append("::");
			_sb.append(source.get("dest-vg"));
			_sb.append("@");
			_sb.append(source.get("dest-lv"));
			_sb.append("_");
			_sb.append(source.get("local-address"));
			
			boolean launch = false;
			if(!cm.isRunning(_sb.toString())) {
				launch = true;
			} else {
				int interval = DEFAULT_MAX_EXECUTION_TIME;
				Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
				if (_c.hasProperty("rsync_interval"))
					interval = Integer.parseInt(_c.getProperty("rsync_interval"));
				List<CommandEntry> commands = cm.findCommandsByCommandStringAndStatus(_sb.toString(), CommandManager.STATUS_COMAND_RUNING);
				for (CommandEntry com : commands) {
					if (com.getDateLaunched() != null) {
						if((Calendar.getInstance().getTimeInMillis() - com.getDateLaunched().getTime()) > interval) {
							launch = true;
							killRunningSyncronizationsOf(com);
							cm.updateUnfinishedCommand(com);
						}
					}
				}
			}
			
			if (launch) {
				logger.info("Lanzamos replicacion rsync: {}", _sb.toString());
				killRunningSincronizationsOfString(_sb.toString());
				cm.asyncExecute(_sb.toString(), ReplicationManager.TYPE_COMMAND_REPLICATON, _pwd_file.getAbsolutePath());
			}
			
			if(_error_file.exists()) {
				_error_file.delete();
			}
		} catch(Exception _ex) {
			logger.error("Error de rsync: {}", _ex);
			if(!_error_file.exists()) {
				try {
					FileOutputStream _fos = new FileOutputStream(_error_file);
					try {
						_fos.write("unknown synchronization error".getBytes());
					}
					finally {
						_fos.close();
					}
				} catch(Exception _ex2) {}
			}
		}
	}
	
	
	/**
	 * Elimina los comandos antiguos, que han superado el período de retencion
	 * @throws Exception
	 */
	public static void removeOldCommands() {
		int retention = DEFAULT_RETENTION;
		try {
			logger.info("Eliminando conmandos de sincronización antiguos de la BD ...");
			Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
			if (_c.hasProperty("command_history_retention"))
				retention = Integer.parseInt(_c.getProperty("command_history_retention"));
			List<String> bins = new ArrayList<String>();
			bins.add("/usr/bin/rsync");
			bins.add("/sbin/zfs");
			List<CommandEntry> commands = cm.findCommandsByCommandBinOptions(bins);
			for (CommandEntry com : commands) {
				if (com.getDateLaunched() != null && com.getStatus() != null && com.getStatus() != CommandManager.STATUS_COMAND_RUNING  && com.getStatus() != CommandManager.STATUS_COMAND_UNLAUNCHED) {
					if((Calendar.getInstance().getTimeInMillis() - com.getDateLaunched().getTime()) > retention) {
						cm.deleteCommand(com);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Se produjo un error al intentar eliminar los comandos más antiguos de {}", retention, ex.getMessage());
		}
	}

	
	public void setDestination(String group, String volume, String[] address, String password, boolean filesystem) throws Exception {
		if(volume == null || group == null ||
				!volume.matches("[0-9a-zA-Z-._]+") ||
				!group.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid logical volume name");
		}
		if(!VolumeManager.isMount(group, volume)) {
			throw new Exception("invalid logical volume");
		}
		if(!NetworkManager.isValidAddress(address)) {
			throw new Exception("invalid network address");
		}
		if(!filesystem) {
			if(password == null || password.isEmpty()) {
				throw new Exception("invalid password");
			}
			
			Map<String, String> _destination = new HashMap<String, String>();
			_destination.put("name", group + "/" + volume);
			_destination.put("vg", group);
			_destination.put("lv", volume);
			_destination.put("address", NetworkManager.addressToString(address));
			_destination.put("password", password);
			_destination.put("filesystem", "no");
			this._destinations.put(_destination.get("name")+"_"+_destination.get("address"), _destination);
			writeDestinations();
			ServiceManager.restart(ServiceManager.RSYNC);
		} else {
			if(!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, VolumeManager.getLogicalVolumeFS(group, volume))) {
				throw new Exception("logical volume does not support filesystem low level replication");
			}
			
			checkReplicationUser(group, volume, password);
			Map<String, String> _destination = new HashMap<String, String>();
			_destination.put("name", group + "/" + volume);
			_destination.put("vg", group);
			_destination.put("lv", volume);
			_destination.put("address", NetworkManager.addressToString(address));
			_destination.put("password", password);
			_destination.put("filesystem", "yes");
			this._destinations.put(_destination.get("name")+"_"+_destination.get("address"), _destination);
			
			writeDestinations();
		}
	}
	
	public void setSource(String group, String volume, String[] address, String[] addressLocal, String dgroup, String dvolume, String password, int mbs, boolean delete, boolean filesystem, boolean checksum, boolean compress, boolean append, boolean delta) throws Exception {
		if(volume == null || group == null ||
				!volume.matches("[0-9a-zA-Z-._]+") ||
				!group.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid logical volume name");
		}
		if(!VolumeManager.isMount(group, volume)) {
			throw new Exception("invalid logical volume");
		}
		if(!NetworkManager.isValidAddress(address)) {
			throw new Exception("invalid network address");
		}
		if(!checkRemoteDestination(NetworkManager.addressToString(address), dgroup, dvolume, password, filesystem)) {
			throw new Exception("invalid destination logical volume");
		}
		
		if (!NetworkManager.isValidAddress(addressLocal)) {
			throw new Exception("invalid local addresss");
		}
		
		Map<String, String> _source = new HashMap<String, String>();
		_source.put("name", group + "/" + volume);
		_source.put("vg", group);
		_source.put("lv", volume);
		_source.put("dest-vg", dgroup);
		_source.put("dest-lv", dvolume);
		_source.put("address", NetworkManager.addressToString(address));
		_source.put("local-address", NetworkManager.addressToString(addressLocal));
		if(delete) {
			_source.put("delete", "yes");
		} else {
			_source.put("delete", "no");
		}
		if(checksum) {
			_source.put("checksum", "yes");
		} else {
			_source.put("checksum", "no");
		}
		if(compress) {
			_source.put("compress", "yes");
		} else {
			_source.put("compress", "no");
		}
		if(append) {
			_source.put("append", "yes");
		} else {
			_source.put("append", "no");
		}
		if(delta) {
			_source.put("delta", "yes");
		} else {
			_source.put("delta", "no");
		}
		if(filesystem) {
			if(!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, VolumeManager.getLogicalVolumeFS(group, volume))) {
				throw new Exception("logical volume does not support filesystem low level replication");
			}
			_source.put("filesystem", "yes");
		} else {
			if(mbs > 0) {
				_source.put("mbs", String.valueOf(mbs));
			}
			if(password == null || password.isEmpty()) {
				throw new Exception("invalid password");
			}
			_source.put("password", password);
			_source.put("filesystem", "no");
		}
		this._sources.put(_source.get("name")+"_"+_source.get("address"), _source);
		writeSources();
	}
	
	private void writeDestinations() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getDirectorySync());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		writeRsyncDestinations(this._destinations);
		Map<String, ArrayList<String>> _commands = new HashMap<String, ArrayList<String>>();
		for(String _name : this._destinations.keySet()) {
			Map<String, String> _destination = this._destinations.get(_name);
			if(_destination.get("filesystem") != null &&
					!"yes".equalsIgnoreCase(_destination.get("filesystem"))) {
				continue;
			}
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, VolumeManager.getLogicalVolumeFS(_destination.get("vg"), _destination.get("lv")))) {
				String _command = ZFSConfiguration.getFileSystemRemoteReplicationCommand(_destination.get("vg"), _destination.get("lv"));
				ArrayList<String> _volumeCommands = new ArrayList<String>();
				if(_volumeCommands == null || !_volumeCommands.contains(_command)) {
					_volumeCommands.add(_command);
					_volumeCommands.add("/sbin/zfs list -H -t snapshot -o name");
					_volumeCommands.add("/sbin/zfs list -H -o name");
					_volumeCommands.add("/sbin/zfs release -r keep "+_destination.get("vg")+"/"+_destination.get("lv")+"@GMT-*");
					_volumeCommands.add("/sbin/zfs destroy -fr "+_destination.get("vg")+"/"+_destination.get("lv")+"@GMT-*");
					_volumeCommands.add("/sbin/zfs destroy -R "+_destination.get("vg")+"/"+_destination.get("lv")+"@GMT-*");
					_volumeCommands.add("/sbin/zfs rollback "+_destination.get("vg")+"/"+_destination.get("lv")+"@GMT-*");
					_volumeCommands.add("/sbin/zfs clone -o mountpoint=/mnt/airback/volumes/"+_destination.get("vg")+"/"+_destination.get("lv")+"/.snapshots/*");
					_volumeCommands.add("/sbin/zfs mount "+_destination.get("vg")+"/"+_destination.get("lv")+"@GMT-*");
					_volumeCommands.add("/sbin/zfs hold -r keep "+_destination.get("vg")+"/"+_destination.get("lv")+"@GMT-*");
					_volumeCommands.add("/bin/chmod 777 /mnt/airback/volumes/"+_destination.get("vg")+"/"+_destination.get("lv")+"/.snapshots/");
					_volumeCommands.add("/bin/mkdir /mnt/airback/volumes/"+_destination.get("vg")+"/"+_destination.get("lv")+"/.snapshots/");
					_volumeCommands.add("/bin/echo \"yes\"");
					_volumeCommands.add("/bin/echo \"no\"");
					_volumeCommands.add("/bin/rm -R /mnt/airback/volumes/"+_destination.get("vg")+"/"+_destination.get("lv")+"/*");
					_volumeCommands.add("/sbin/mbuffer");
					_commands.put(getReplicationUser(_destination.get("vg"), _destination.get("lv")), _volumeCommands);
				}
			}
		}
		writeSudoFileSystemCommands(_commands);
		
		Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    	Element _sources = _doc.createElement("destinations");
    	for(Map<String, String> _map : this._destinations.values()) {
    		Element _source = _doc.createElement("destinations");
    		for(String _name : _map.keySet()) {
    			Element _e = _doc.createElement(_name);
    			_e.setTextContent(_map.get(_name));
    			_source.appendChild(_e);
    		}
    		_sources.appendChild(_source);
    	}
    	_doc.appendChild(_sources);
    	
    	Source source = new DOMSource(_doc);
    	_f = new File(WBSAirbackConfiguration.getDirectorySync() + "/destinations.xml");
        Result result = new StreamResult(_f);
    	FileLock _fl = new FileLock(_f);
        Transformer _t = TransformerFactory.newInstance().newTransformer();
    	try {
    		_fl.lock();
	    	_t.transform(source, result);
    	} finally {
    		_fl.unlock();
    	}
	}
	
	private static void writeRsyncDestinations(Map<String, Map<String, String>> destinations) throws Exception {
		StringBuilder _sb = new StringBuilder();
		File _f = new File(WBSAirbackConfiguration.getDirectorySync());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		_sb.append("uid=root\n");
		_sb.append("gid=root\n");
		_sb.append("use chroot = yes\n");
		_sb.append("max connections = 0\n");
		_sb.append("strict modes = yes\n");
		_sb.append("pid file = /var/run/rsyncd.pid\n");
		for(String _name : destinations.keySet()) {
			Map<String, String> _destination = destinations.get(_name);
			if(_destination.get("filesystem") != null &&
					"yes".equalsIgnoreCase(_destination.get("filesystem"))) {
				continue;
			}
			StringBuilder _sb_destination = new StringBuilder();
			try {
				if(VolumeManager.isMount(_destination.get("vg"), _destination.get("lv"))) {
					_name = _name.replace("/", "@");
					_f = new File(WBSAirbackConfiguration.getDirectorySync() + "/" + _name + ".conf");
					_sb_destination.append("sync:");
					_sb_destination.append(_destination.get("password"));
					_sb_destination.append("\n");
					FileSystem.writeFile(_f, _sb_destination.toString());
					Command.systemCommand("/bin/chmod 600 " + _f.getAbsolutePath());
					
					_sb_destination = new StringBuilder();
					_sb_destination.append("[");
					_sb_destination.append(_name);
					_sb_destination.append("]\n");
					_sb_destination.append("\tpath = ");
					_sb_destination.append(VolumeManager.getLogicalVolumeMountPath(_destination.get("vg"), _destination.get("lv")));
					_sb_destination.append("\n");
					_sb_destination.append("\tcomment = ");
					_sb_destination.append(_destination.get("vg"));
					_sb_destination.append("/");
					_sb_destination.append(_destination.get("lv"));
					_sb_destination.append("\n");
					_sb_destination.append("\tauth users = sync\n");
					_sb_destination.append("\tread only = no\n");
					_sb_destination.append("\twrite only = no\n");
					_sb_destination.append("\thosts allow = ");
					_sb_destination.append(_destination.get("address"));
					_sb_destination.append("\n");
					_sb_destination.append("\texclude = @GMT-* .recycle\n");
					_sb_destination.append("\tsecrets file = ");
					_sb_destination.append(_f.getAbsolutePath());
					_sb_destination.append("\n");
					_sb.append(_sb_destination.toString());
				}
			} catch(Exception _ex) {
				System.out.println("ReplicationManager::destination::error: " + _ex.toString());
				throw _ex;
			}
		}
		_f = new File(WBSAirbackConfiguration.getDirectorySync() + "/rsyncd.conf");
		FileSystem.writeFile(_f, _sb.toString());
	}
	
	private void writeSources() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getDirectorySync());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    	Element _sources = _doc.createElement("sources");
    	for(Map<String, String> _map : this._sources.values()) {
    		Element _source = _doc.createElement("source");
    		for(String _name : _map.keySet()) {
    			Element _e = _doc.createElement(_name);
    			_e.setTextContent(_map.get(_name));
    			_source.appendChild(_e);
    		}
    		_sources.appendChild(_source);
    	}
    	_doc.appendChild(_sources);
    	
    	Source source = new DOMSource(_doc);
    	_f = new File(WBSAirbackConfiguration.getDirectorySync() + "/sources.xml");
        Result result = new StreamResult(_f);
    	FileLock _fl = new FileLock(_f);
        Transformer _t = TransformerFactory.newInstance().newTransformer();
    	try {
    		_fl.lock();
	    	_t.transform(source, result);
    	} finally {
    		_fl.unlock();
    	}
	}
	
	private static void writeSudoFileSystemCommands(Map<String, ArrayList<String>> commands) throws Exception {
		File _f = new File(WBSAirbackConfiguration.getDirectorySudo());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		StringBuilder _sb = new StringBuilder();
		_f = new File(WBSAirbackConfiguration.getDirectorySudo() + "/filesystem-replication");
		for(String _user : commands.keySet()) {
			for (String cmd : commands.get(_user)) {
				_sb.append(_user);
				_sb.append(" ALL = (root) NOPASSWD: ");
				_sb.append(cmd);
				_sb.append("\n");
			}
		}
		FileSystem.writeFile(_f, _sb.toString());
		Command.systemCommand("/bin/chmod 440 " + _f.getAbsolutePath());
	}
	
	public Map<String, List<Map<String, String>>> getCurrentSyncronizations() throws Exception {
		try {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			DateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
			Map<String, List<Map<String, String>>> list = new HashMap<String, List<Map<String, String>>>();
			List<Map<String, String>> notRunning = new ArrayList<Map<String, String>>();
			List<Map<String, String>> running = new ArrayList<Map<String, String>>();
			List<String> bins = new ArrayList<String>();
			bins.add("/usr/bin/rsync");
			bins.add("/sbin/zfs");
			List<CommandEntry> commands = cm.findCommandsByCommandBinOptions(bins);
			String tmp = null;

			if (commands != null && !commands.isEmpty()) {
				for (CommandEntry command : commands) {
					Map<String, String> sync = new HashMap<String, String>();
					if (command.getStatus() != null && command.getDateLaunched() != null) {
						sync.put("id", command.getId().toString());
						if (command.getStatus().equals(CommandManager.STATUS_COMAND_RUNING))
							sync.put("status", "Running");
						else if (command.getStatus().equals(CommandManager.STATUS_COMAND_FINISHED))
							sync.put("status", "Finished");
						else if (command.getStatus().equals(CommandManager.STATUS_COMAND_UNLAUNCHED))
							sync.put("status", "Unlaunched");
						else if (command.getStatus().equals(CommandManager.STATUS_COMAND_TIME_OUT))
							sync.put("status", "Time out");
						sync.put("launched", df.format(command.getDateLaunched()));
						
						if (command.getExitCode() != null)
							sync.put("exit_status", command.getExitCode().toString());
						
						if (sync.get("left") == null || sync.get("left").isEmpty())
							sync.put("left", "--");
						if (command.getCommandString() != null && !command.getCommandString().isEmpty()) {
							if (command.getCommandString().contains("sync@") && command.getCommandString().contains("/mnt/airback/volumes/")) {
								sync.put("source", command.getCommandString().substring(command.getCommandString().indexOf("/mnt/airback/volumes/")+"/mnt/airback/volumes/".length(), command.getCommandString().indexOf("sync@")).trim());
								sync.put("destination", command.getCommandString().substring(command.getCommandString().indexOf("sync@")+"sync@".length(), command.getCommandString().lastIndexOf("_")));
								sync.put("desc", "sync");
								if (command.getStOutput() != null)
									readRsyncOutput(command.getStOutput(), sync);
							} else if (command.getCommandString().contains("zfs send") && command.getCommandString().contains("zfs receive")) {
								tmp = command.getCommandString().substring(0, command.getCommandString().indexOf(" |")).trim();
								sync.put("source", tmp.substring(tmp.lastIndexOf(" ")+1).trim());
								tmp = command.getCommandString().substring(command.getCommandString().lastIndexOf("@")+1);
								tmp = tmp.substring(0, tmp.indexOf(" ")).trim();
								tmp = tmp + "::" +command.getCommandString().substring(command.getCommandString().indexOf("receive -F ")+"receive -F ".length()).replace("/", "@");
								sync.put("destination", tmp.trim());
								if (command.getCommandString().contains("-DvI"))
									sync.put("desc", "fs-incremental");
								else
									sync.put("desc", "fs-full");
								if (command.getDateLaunched() != null && command.getDateFinished() != null)
									sync.put("time", formatTime(command.getDateFinished().getTime(), command.getDateLaunched().getTime()));
								else
									sync.put("time", formatTime((new Date()).getTime(), command.getDateLaunched().getTime()));
								if (command.getStOutput() != null)
									readZfsOutput(command.getStOutput(), sync, command);
							} else if (command.getCommandString().contains("zfs hold")) {
								sync.put("desc", "fs-snap-hold");
								putZFSReplicationCommandData(command, sync, dfTime);
							} else if (command.getCommandString().contains("zfs rollback")) {
								sync.put("desc", "fs-snap-mount");
								putZFSReplicationCommandData(command, sync, dfTime);
							} else if (command.getCommandString().contains("zfs release")) {
								sync.put("desc", "fs-snap-release");
								putZFSReplicationCommandData(command, sync, dfTime);
							} else if (command.getCommandString().contains("zfs destroy -fr")) {
								sync.put("desc", "fs-snap-destroy-1");
								putZFSReplicationCommandData(command, sync, dfTime);
							} else if (command.getCommandString().contains("zfs destroy -R")) {
								sync.put("desc", "fs-snap-destroy-2");
								putZFSReplicationCommandData(command, sync, dfTime);
							}
						}
						
						if (command.getDateFinished() != null) {
							sync.put("finished", df.format(command.getDateFinished()));
							sync.put("time", formatTime(command.getDateFinished().getTime(), command.getDateLaunched().getTime()));
						}
						
						if ( (command.getExitCode() != null && command.getExitCode() != 0) || (command.getErrOutput() != null && !command.getErrOutput().isEmpty()) ) {
							sync.put("error", "true");
						} else
							sync.put("error", "false");
						
						if (!sync.containsKey("desc"))
							sync.put("desc", "--");
						
						if (!sync.containsKey("speed"))
							sync.put("speed", "--");
						
						if (sync.get("status").equals("Running"))
							running.add(sync);
						else
							notRunning.add(sync);
					}
				}
			}
			if (running != null && !running.isEmpty())
				list.put("running", running);
			if (notRunning != null && !notRunning.isEmpty())
				list.put("notRunning", notRunning);
				
			return list;
		} catch (Exception ex) {
			logger.error("Error obteniendo sincronizaciones lanzadas. Ex:{}", ex.getMessage());
			return null;
		}
	}
	
	public String formatTime(long timeMillis1, long timeMillis2) {
		try {
			long millis = timeMillis1 - timeMillis2;
			return String.format("%s:%s:%s", 
					VolumeManager.formatNumber(TimeUnit.MILLISECONDS.toHours(millis)),
					VolumeManager.formatNumber(TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))),
					VolumeManager.formatNumber(TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
		} catch (Exception ex) {
			logger.error("Error formating date for {} - {}. Ex: {}", new Object[] {timeMillis1, timeMillis2, ex.getMessage()});
			return "";
		}
	}
	
	public void putZFSReplicationCommandData(CommandEntry command, Map<String, String> sync, DateFormat dfTime) {
		String tmp = command.getCommandString().substring(0, command.getCommandString().indexOf(" sudo"));
		String destip = tmp.substring(tmp.indexOf("@")+1);
		tmp = command.getCommandString().substring( command.getCommandString().lastIndexOf(" ")+1);
		if (tmp.contains("@"))
			sync.put("source", tmp.substring(tmp.indexOf("@")+1));
		else
			sync.put("source", tmp.replaceFirst("/", "@").substring(tmp.indexOf("@")+1));
		if (tmp.contains("@"))
			sync.put("destination", destip+"::"+tmp.substring(0,tmp.lastIndexOf("@")).replace("/", "@"));
		else
			sync.put("destination", destip+"::"+tmp.substring(0,tmp.lastIndexOf("/")).replace("/", "@"));
		if (command.getDateFinished() != null && command.getDateLaunched() != null)
			sync.put("time", formatTime(command.getDateFinished().getTime(), command.getDateLaunched().getTime()));
		else
			sync.put("time", formatTime((new Date()).getTime(), command.getDateLaunched().getTime()));
		//ssh -x -oStrictHostKeyChecking=no -oCheckHostIP=no -i /var/.ssh/key_repagg1_sync agg1_sync@192.168.1.55 sudo /sbin/zfs rollback agg1/sync@GMT-2013.07.17-16.21.15
	}
	
	public String getRsyncLog(String commandId) throws Exception {
		CommandEntry command = cm.getCommandById(new ObjectId(commandId));
		String html = "";
		if (command != null) {
			/*html += "<p>";
			html+= command.getCommandString();
			html += "</p>";*/
			if (command.getErrOutput() != null && !command.getErrOutput().isEmpty()) {
				if (!command.getCommandString().contains("zfs send"))
					html += "<p style=\"color:red;\">";
				else
					html += "<p>";
				html += command.getErrOutput().replace("\n", "<br />");
				html += "</p>";
			}
			if (command.getStOutput() != null && !command.getStOutput().isEmpty()) {
				html += "<p>";
				html += command.getStOutput().replace("\n", "<br />");
				html += "</p>";
			}
			if (html.isEmpty()) {
				html += "<p> -- </p>";
			}
		}
		return html;
	}
	
	public void readZfsOutput(String output, Map<String, String> syncObject, CommandEntry command) throws Exception {
		String totalSize  = "";
		String currentSize  = "";
		if (output != null && !output.isEmpty()) {
			BufferedReader _br = new BufferedReader(new StringReader(output));
			String lastLine = null;
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if (_line.contains("total estimated size is")) {
					totalSize = _line.trim();
					totalSize = _line.substring(_line.lastIndexOf(" ")+1);
				}
				if (_line != null && _line.contains("@GMT"))
					lastLine = _line;
			}
			//13:45:57 1.63G agg1/origen@GMT-2013.07.29-13.43.46
			if (lastLine != null && !lastLine.contains("estimated")) {
				StringTokenizer st = new StringTokenizer(lastLine, " ");
				st.nextToken();
				currentSize = st.nextToken().trim();
			}
			
			if (command.getDateFinished() != null && !totalSize.isEmpty())
				syncObject.put("left", totalSize);
			else if (!totalSize.isEmpty() && !currentSize.isEmpty())
				syncObject.put("left", currentSize+"/"+totalSize);
			else if (!totalSize.isEmpty())
				syncObject.put("left", totalSize);
			else if (!currentSize.isEmpty())
				syncObject.put("left", currentSize+"/--");
			
			if (command.getDateFinished() != null && command.getDateLaunched() != null && !totalSize.isEmpty()) {
				float tam = VolumeManager.getByteSizeFromHuman(totalSize.replace(",", "."));
				float speed = tam;						
				long timeSecs = command.getDateFinished().getTime() - command.getDateLaunched().getTime();
				if (timeSecs > 0) {
					float secs = (float) TimeUnit.MILLISECONDS.toSeconds(timeSecs);
					if (secs > 0)
						speed = speed / secs;
				}
				syncObject.put("speed", VolumeManager.getBlockSize(String.valueOf(speed/1024F))+"/s");
			} else if (command.getDateLaunched() != null && !currentSize.isEmpty()){
				float tam = VolumeManager.getByteSizeFromHuman(currentSize.replace(",", "."));
				float speed = tam;						
				long timeSecs = (new Date()).getTime() - command.getDateLaunched().getTime();
				if (timeSecs > 0) {
					float secs = (float) TimeUnit.MILLISECONDS.toSeconds(timeSecs);
					if (secs > 0)
						speed = speed / secs;
				}
				syncObject.put("speed", VolumeManager.getBlockSize(String.valueOf(speed/1024F))+"/s");
			}
		}
	}
	
	public void readRsyncOutput(String output, Map<String, String> syncObject) throws Exception {
		BufferedReader _br = new BufferedReader(new StringReader(output));
		DateFormat df = new SimpleDateFormat("H:mm:ss");
		try {
			boolean finalizedRsync = false;
			String lastSpeed = "";
			Long time = 0L;
			String lastLeft = "";
			String finalSpeed = "";
			String tmp = ""; 
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if (_line.contains("(xfer") && _line.contains("to-check")) {
					//            6 100%    0.00kB/s    0:00:00 (xfer#1, to-check=4/6)
					StringTokenizer _st = new StringTokenizer(_line, " ");
					//            6
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					// 100%
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					//    0.00kB/s
					if (_st.hasMoreTokens())
						lastSpeed = _st.nextToken();
					else
						continue;
					//    0:00:00
					if (_st.hasMoreTokens())
						time += df.parse(_st.nextToken()).getTime();
					else
						continue;
					// (xfer#1,
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					// to-check=4/6)
					if (_st.hasMoreTokens()) {
						tmp =  _st.nextToken();
						if (tmp.contains("to-check")) {
							lastLeft = tmp.substring(tmp.indexOf("to-check=")+"to-check=".length(), tmp.indexOf(")"));
						}
					} else
						continue;
					
				} else if (_line.contains("sent") && _line.contains("received")) {
					//sent 351525599 bytes  received 106 bytes  1905288.37 bytes/sec
					StringTokenizer _st = new StringTokenizer(_line, " ");
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					if (_st.hasMoreTokens())
						finalSpeed = _st.nextToken();
					else
						continue;
				} else if (_line.contains("total") && _line.contains("speedup")) {
					//total size is 566890533  speedup is 1.61
					finalizedRsync = true;
				} else if (_line.contains("%")) {
					StringTokenizer _st = new StringTokenizer(_line, " ");
					//            6
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					// 100%
					if (_st.hasMoreTokens())
						_st.nextToken();
					else
						continue;
					//    0.00kB/s
					if (_st.hasMoreTokens())
						lastSpeed = _st.nextToken();
					else
						continue;
				}
			}
			if (finalizedRsync && finalSpeed != null) {
				Float speed = Float.parseFloat(finalSpeed);
				if (speed > 0) {
					speed=speed/1024;
					syncObject.put("speed", VolumeManager.getBlockSize(speed.toString())+"/s");
				} else
					syncObject.put("speed", "0 KB/s");
				if (lastLeft.contains("/"))
					syncObject.put("left", lastLeft.substring(lastLeft.indexOf("/")+1));
				else
					syncObject.put("left", "0");
			} else if (!lastSpeed.isEmpty()) {
				syncObject.put("speed", lastSpeed);
				syncObject.put("left", lastLeft);
			}
			String stime = "0:00:00";
			if (time > 0L)
				stime = df.format(new Date(time));
			syncObject.put("time", stime);
		} catch (Exception ex) {
			logger.error("Se produjo un error intentando interpretar la salida de un rsync");
		}
	}
	
	public void eraseCommandDB() throws Exception {
		List<String> bins = new ArrayList<String>();
		bins.add("/usr/bin/rsync");
		bins.add("/sbin/zfs");
		List<CommandEntry> commands = cm.findCommandsByCommandBinOptions(bins);
		if (commands != null && !commands.isEmpty()) {
			for (CommandEntry ce : commands)
				eraseCommandById(ce.getId().toString());
		}
	}
	
	public void eraseCommandById(String id) throws Exception {
		try {
			if (id != null && !id.isEmpty()) {
				CommandEntry ce = cm.getCommandById(new ObjectId(id));
				if (ce.getCommandString() != null && ce.getStatus() != null && ce.getStatus() != CommandManager.STATUS_COMAND_FINISHED) {
					killRunningSyncronizationsOf(ce);
				}
				ce.getCommandString();
				cm.deleteCommand(ce);
			}
		} catch (Exception ex) {
			logger.error("Se produjo un error al borrar un comando de rsync por id");
		}
	}
	
	public static void killRunningSyncronizationsOf(CommandEntry command) throws Exception {
		if (command.getProcessPid() != null) {
			try {
				logger.debug("Matando sincronizacion de pid: {}", command.getProcessPid());
				Command.systemCommand("kill -9 "+command.getProcessPid());
				// Damos Tiempo a que se mate el proceso
				Thread.sleep(400);
				logger.info("Sincronizacion de pid: {} matada", command.getProcessPid());
			} catch (Exception ex) {
				logger.error("Error intentando matar pid: {}. Ex: {}", command.getProcessPid(), ex.getMessage());
			}
		}
		killRunningSincronizationsOfString(command.getCommandString());
		
	}
	
	public static void killRunningSincronizationsOfString(String command) throws Exception {
		List<String> pids = findPidsRunningSinc(command);
		if (pids != null && !pids.isEmpty()) {
			for (String pid : pids) {
				try {
					logger.debug("Matando sincronizacion de pid: {}", pid);
					Command.systemCommand("kill -9 "+pid);
					logger.info("Sincronizacion de pid: {} matada", pid);
				} catch (Exception ex) {
					logger.error("Error intentando matar pid: {}. Ex: {}", pid, ex.getMessage());
				}
			}
		}
	}
	
	public static List<String> findPidsRunningSinc(String command) throws Exception {
		try {
			List<String> pids = null;
			if (command != null && !command.isEmpty()) {
				if (command.contains("sync@") && command.contains("/mnt/airback/volumes/")) {
					String source = command.substring(command.indexOf("/mnt/airback/volumes/")+"/mnt/airback/volumes/".length(), command.indexOf("sync@")).trim();
					String destination = command.substring(command.indexOf("sync@")+"sync@".length(), command.lastIndexOf("_"));
					String findPidsCommand = "ps ax | grep rsync | grep "+source+" | grep "+destination+" | awk '{ print $1; }'";
					String output = Command.systemCommand(findPidsCommand);
					if (output != null && !output.isEmpty())
						pids = Arrays.asList(output.split("\n"));
				} else if (command.contains("zfs") && command.contains("send")) {
					String tmp = command.substring(0, command.indexOf(" |"));
					String source = tmp.substring(tmp.lastIndexOf(" ")+1).trim();
					tmp = command.substring(command.lastIndexOf("@")+1);
					tmp = tmp.substring(0, tmp.indexOf(" ")).trim();
					String destination = command.substring(command.indexOf("receive -F ")+"receive -F ".length()).replace("/", "@");
					String findPidsCommand = "ps ax | grep zfs | grep send | grep "+source+" | grep "+destination+" | awk '{ print $1; }'";
					String output = Command.systemCommand(findPidsCommand);
					if (output != null && !output.isEmpty())
						pids = Arrays.asList(output.split("\n"));
				}
			}
			return pids;
		} catch (Exception ex) {
			logger.error("Error buscando pids de sincronizaciones lanzadas. Ex: {}", ex.getMessage());
			return null;
		}
	}
}
