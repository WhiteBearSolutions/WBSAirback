package com.whitebearsolutions.imagine.wbsairback.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.imagine.wbsairback.util.StringFormat;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class FileSetManager {
	public static final int PLUGIN_NONE = 0;
	public static final int PLUGIN_SYSTEMSTATE = 1;
	public static final int PLUGIN_SHAREPOINT = 2;
	public static final int PLUGIN_MSSQL = 3;
	public static final int PLUGIN_EXCHANGE = 4;
	public static final int PLUGIN_DELTA = 5;
	public static final int NDMP_AUTH_NONE = 0;
	public static final int NDMP_AUTH_TEXT = 1;
	public static final int NDMP_AUTH_MD5 = 2;
	public static final int NDMP_TYPE_DUMP = 1;
	public static final int NDMP_TYPE_TAR = 2;
	public static final int NDMP_TYPE_SMTAPE = 3;
	
	public static final String COMPRESSION_NONE = "no";
	public static final String COMPRESSION_LZO = "LZO";
	public static final String COMPRESSION_GZIP = "GZIP";
	
	public final static String VCENTER = "vcenter___";
	public final static String VHOST = "vhost___";
	public final static String VDATASTORE = "vdatastore___";
	public final static String VMACHINE = "vmachine___";
	
	public static final Map<Integer, String> SUPPORTED_PLUGINS;
	public static final List<String> SUPPORTED_COMPRESSIONS;
	Configuration _c;
	DBConnection _db;
	
	private final static Logger logger = LoggerFactory.getLogger(FileSetManager.class);
	
	static {
		SUPPORTED_PLUGINS = new HashMap<Integer, String>();
		SUPPORTED_PLUGINS.put(PLUGIN_SYSTEMSTATE, "systemstate");
		SUPPORTED_PLUGINS.put(PLUGIN_SHAREPOINT, "sharepoint");
		SUPPORTED_PLUGINS.put(PLUGIN_MSSQL, "mssql");
		SUPPORTED_PLUGINS.put(PLUGIN_EXCHANGE, "exchange");
		SUPPORTED_PLUGINS.put(PLUGIN_DELTA, "delta");
		
		SUPPORTED_COMPRESSIONS = new ArrayList<String>();
		SUPPORTED_COMPRESSIONS.add(COMPRESSION_NONE);
		SUPPORTED_COMPRESSIONS.add(COMPRESSION_GZIP);
		SUPPORTED_COMPRESSIONS.add(COMPRESSION_LZO);
	}
	
	public FileSetManager(Configuration conf) throws Exception {
		this._c = conf;
		this._db = new DBConnectionManager(this._c).getConnection();
	}
	
	public static void addFileSet(String name, String include, String exclude, String extension, boolean md5, String compression, boolean acl, boolean multiplefs, boolean vss, List<Integer> plugins) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-.]+")) {
			throw new Exception("invalid fileset name");
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
		try {
			updateFileSet(name, include, exclude, extension, md5, compression, acl, multiplefs, vss, plugins);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
			throw _ex;
		}
	}
	
	
	/**
	 * Crea un fileset oculto, para sistemas de almacenamiento externo (NetApp, Svc ...)
	 * @param name
	 * @throws Exception
	 */
	public static void addExternalStorageFileSet(String name, String path, String compression) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-.]+")) {
			throw new Exception("invalid fileset name");
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
		try {
			updateExternalStorageFileSet(name, path, compression);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
			throw _ex;
		}
	}
	
	
	public static void addLocalFileSet(String name, List<String> volumes, String include, String extension, boolean md5, String compression) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-.]+")) {
			throw new Exception("invalid fileset name");
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
		try {
			updateLocalFileSet(name, volumes, include, extension, md5, compression);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
			throw _ex;
		}
	}
	
	public static void addNDMPFileSet(String name, String[] address, int port, int ndmp_auth, String user, String password, Map<String, String> volumes, int ndmp_type) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-.]+")) {
			throw new Exception("invalid fileset name");
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
		try {
			updateNDMPFileSet(name, address, port, ndmp_auth, user, password, volumes, ndmp_type);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
			throw _ex;
		}
	}
	
	public void addVmwareFileSet(String name, String root, String clientName, List<String> vcenters, List<String> hosts, List<String> datastores, List<String> machines, String virtualElements, boolean cbt, String compression, boolean md5, Map<String, Map<String, List<String>>> virtualTree) throws Exception {
		if(name == null || !name.matches("[0-9a-zA-Z-.]+")) {
			throw new Exception("invalid fileset name");
		}
		
		BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
		try {
			updateVmwareFileset(name, root, clientName, vcenters, hosts, datastores, machines, virtualElements, cbt, compression, md5, virtualTree);
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
			throw _ex;
		}
	}
	
	public static List<Map<String, String>> getAllFileSets() {
		TreeMap<String, Map<String, String>> filesets = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		File dir = new File(WBSAirbackConfiguration.getDirectoryFilesets());
		String[] files = dir.list();
		if(files != null) {
			for(String fileset : files) {
				if(fileset.endsWith(".xml")) {
					fileset = fileset.substring(0, fileset.length() - 4);
					try {
						Map<String, String> filesetMap = getFileSet(fileset);
						if(!filesetMap.isEmpty() && filesetMap.get("name") != null && filesetMap.get("remote_storage") == null &&
								(filesetMap.get("local") == null || !"yes".equals(filesetMap.get("local"))) &&
								(filesetMap.get("ndmp") == null || !"yes".equals(filesetMap.get("ndmp"))) &&
								(filesetMap.get("vmware") == null || !"yes".equals(filesetMap.get("vmware")))) {
							filesets.put(filesetMap.get("name"), filesetMap);
						}
					} catch(Exception _ex) {}
				}
			}
		}
		return new ArrayList<Map<String,String>>(filesets.values());
	}
	
	public static List<String> getAllFileSetNames() {
		List<String> _names = new ArrayList<String>();
		List<Map<String, String>> _filesets = getAllFileSets();
		if(_filesets == null || _filesets.isEmpty()) {
			return _names;
		}
		for(Map<String, String> _fileset : _filesets) {
			if(_fileset.get("name") != null) {
				_names.add(_fileset.get("name"));
			}
		}
		
		Collections.sort(_names, String.CASE_INSENSITIVE_ORDER);
		return _names;
	}
	
	public static List<Map<String, String>> getAllLocalFileSets() {
		TreeMap<String, Map<String, String>> filesets = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		File dir = new File(WBSAirbackConfiguration.getDirectoryFilesets());
		String[] files = dir.list();
		if(files != null) {
			for(String fileset : files) {
				if(fileset.endsWith(".xml")) {
					fileset = fileset.substring(0, fileset.length() - 4);
					try {
						Map<String, String> filesetMap = getFileSet(fileset);
						if(!filesetMap.isEmpty() && filesetMap.get("name") != null && filesetMap.get("local") != null && "yes".equals(filesetMap.get("local"))) {
							filesets.put(filesetMap.get("name"), filesetMap);
						}
					} catch(Exception _ex) {}
				}
			}
		}
		
		return new ArrayList<Map<String,String>>(filesets.values());
	}
	
	public static Map<String, Map<String, String>> getAllVmwareFileSetsTree() {
		TreeMap<String, Map<String, String>> filesets = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		File dir = new File(WBSAirbackConfiguration.getDirectoryFilesets());
		String[] files = dir.list();
		if(files != null) {
			for(String fileset : files) {
				if(fileset.endsWith(".xml")) {
					fileset = fileset.substring(0, fileset.length() - 4);
					try {
						Map<String, String> filesetMap = getFileSet(fileset);
						if(!filesetMap.isEmpty() && filesetMap.get("name") != null && filesetMap.get("vmware") != null && "yes".equals(filesetMap.get("vmware"))) {
							filesets.put(filesetMap.get("name"), filesetMap);
						}
					} catch(Exception _ex) {}
				}
			}
		}
		
		return filesets;
	}
	
	public static List<Map<String, String>> getAllVmwareFileSets() {
		return new ArrayList<Map<String,String>>(getAllVmwareFileSetsTree().values());
	}
	
	public static List<Map<String, String>> getVmwareFilesetsOfClient(String client) throws Exception {
		List<Map<String, String>> clientFilesets = new ArrayList<Map<String, String>>();
		List<Map<String, String>> filesets = getAllVmwareFileSets();
		if (filesets != null && !filesets.isEmpty()) {
			for (Map<String, String> f : filesets) {
				if (f.containsKey("vmwareClient") && f.get("vmwareClient").equals(client)) {
					clientFilesets.add(f);
				}
			}
		}
		return clientFilesets;
	}
	
	public static List<String> getAllVmwareFileSetNames() {
		List<String> _names = new ArrayList<String>();
		List<Map<String, String>> _filesets = getAllVmwareFileSets();
		if(_filesets == null || _filesets.isEmpty()) {
			return _names;
		}
		for(Map<String, String> _fileset : _filesets) {
			if(_fileset.get("name") != null) {
				_names.add(_fileset.get("name"));
			}
		}
		
		Collections.sort(_names, String.CASE_INSENSITIVE_ORDER);
		return _names;
	}
	
	public static List<String> getAllLocalFileSetNames() {
		List<String> _names = new ArrayList<String>();
		List<Map<String, String>> _filesets = getAllLocalFileSets();
		if(_filesets == null || _filesets.isEmpty()) {
			return _names;
		}
		for(Map<String, String> _fileset : _filesets) {
			if(_fileset.get("name") != null) {
				_names.add(_fileset.get("name"));
			}
		}
		
		Collections.sort(_names, String.CASE_INSENSITIVE_ORDER);
		return _names;
	}
	
	public static List<Map<String, String>> getAllNDMPFileSets() {
		TreeMap<String, Map<String, String>> filesets = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		File dir = new File(WBSAirbackConfiguration.getDirectoryFilesets());
		String[] files = dir.list();
		if(files != null) {
			for(String fileset : files) {
				if(fileset.endsWith(".xml")) {
					fileset = fileset.substring(0, fileset.length() - 4);
					try {
						Map<String, String> filesetMap = getFileSet(fileset);
						if(!filesetMap.isEmpty() && filesetMap.get("name") != null && filesetMap.get("ndmp") != null && "yes".equals(filesetMap.get("ndmp"))) {
							filesets.put(filesetMap.get("name"), filesetMap);
						}
					} catch(Exception _ex) {}
				}
			}
		}
		
		return new ArrayList<Map<String,String>>(filesets.values());
	}
	
	public static List<String> getAllNDMPFileSetNames() {
		List<String> _names = new ArrayList<String>();
		List<Map<String, String>> _filesets = getAllNDMPFileSets();
		if(_filesets == null || _filesets.isEmpty()) {
			return _names;
		}
		for(Map<String, String> _fileset : _filesets) {
			if(_fileset.get("name") != null) {
				_names.add(_fileset.get("name"));
			}
		}
		
		Collections.sort(_names, String.CASE_INSENSITIVE_ORDER);
		return _names;
	}
	
	public static Map<String, String> getFileSet(String name) throws Exception {
		Map<String, String> fileset = new HashMap<String, String>();
		try {
			File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".xml");
			if(!_f.exists()) {
				throw new Exception("fileset does not exists");
			}
			Configuration _c = new Configuration(_f);
			fileset.put("name", name);
			if(_c.checkProperty("fileset.remote_storage", "yes")) {
				fileset.put("remote_storage", "yes");
			}
			if(_c.checkProperty("fileset.local", "yes")) {
				fileset.put("local", "yes");
			}
			if(_c.checkProperty("fileset.ndmp", "yes")) {
				fileset.put("ndmp", "yes");
			}
			if(_c.getProperty("fileset.include") != null) {
				fileset.put("include", _c.getProperty("fileset.include"));
			}
			if(_c.getProperty("fileset.volumes") != null) {
				fileset.put("volumes", _c.getProperty("fileset.volumes"));
			}
			if(_c.getProperty("fileset.oldVolumes") != null) {
				fileset.put("oldVolumes", _c.getProperty("fileset.oldVolumes"));
			}
			if(_c.getProperty("fileset.exclude") != null) {
				fileset.put("exclude", _c.getProperty("fileset.exclude"));
			}
			if(_c.getProperty("fileset.md5") != null) {
				fileset.put("md5", _c.getProperty("fileset.md5"));
			}
			if(_c.getProperty("fileset.compression") != null) {
				if (_c.getProperty("fileset.compression").equals("yes"))
					fileset.put("compression", COMPRESSION_LZO);
				else
					fileset.put("compression", _c.getProperty("fileset.compression"));
			}
			if(_c.getProperty("fileset.extension") != null) {
				fileset.put("extension", _c.getProperty("fileset.extension"));
			}
			if(_c.getProperty("fileset.acl") != null) {
				fileset.put("acl", _c.getProperty("fileset.acl"));
			}
			if(_c.getProperty("fileset.multiplefs") != null) {
				fileset.put("multiplefs", _c.getProperty("fileset.multiplefs"));
			}
			if(_c.getProperty("fileset.vss") != null) {
				fileset.put("vss", _c.getProperty("fileset.vss"));
			}
			if(_c.getProperty("fileset.plugins") != null) {
				fileset.put("plugins", _c.getProperty("fileset.plugins"));
			}
			if(_c.getProperty("fileset.ndmp.address") != null) {
				fileset.put("ndmp.address", _c.getProperty("fileset.ndmp.address"));
			}
			if(_c.getProperty("fileset.ndmp.port") != null) {
				fileset.put("ndmp.port", _c.getProperty("fileset.ndmp.port"));
			}
			if(_c.getProperty("fileset.ndmp.auth") != null) {
				fileset.put("ndmp.auth", _c.getProperty("fileset.ndmp.auth"));
			}
			if(_c.getProperty("fileset.ndmp.user") != null) {
				fileset.put("ndmp.user", _c.getProperty("fileset.ndmp.user"));
			}
			if(_c.getProperty("fileset.ndmp.password") != null) {
				fileset.put("ndmp.password", _c.getProperty("fileset.ndmp.password"));
			}
			if(_c.getProperty("fileset.ndmp.type") != null) {
				fileset.put("ndmp.type", _c.getProperty("fileset.ndmp.type"));
			}
			if(_c.getProperty("fileset.ndmp.volumes") != null) {
				fileset.put("ndmp.volumes", _c.getProperty("fileset.ndmp.volumes"));
			}
			if(_c.getProperty("fileset.virtualElements") != null) {
				fileset.put("virtualElements", _c.getProperty("fileset.virtualElements"));
			}
			if(_c.getProperty("fileset.vmwareClient") != null ) {
				fileset.put("vmwareClient", _c.getProperty("fileset.vmwareClient"));
			}
			if(_c.getProperty("fileset.allVirtualMachines") != null ) {
				fileset.put("allVirtualMachines", _c.getProperty("fileset.allVirtualMachines"));
			}
			if(_c.getProperty("fileset.vmware") != null && _c.checkProperty("fileset.vmware", "yes")) {
				fileset.put("vmware", "yes");
			} else {
				fileset.put("vmware", "no");
			}
			if(_c.getProperty("fileset.cbt") != null && _c.checkProperty("fileset.cbt", "yes")) {
				fileset.put("cbt", "yes");
			} else {
				fileset.put("cbt", "no");
			}
		} catch(Exception _ex) {}
		return fileset;
	}
	
	public List<String> getFileSetIncludesForClient(int clientId) throws Exception {
		List<String> includes = new ArrayList<String>();
		for(String fileset : getFileSetsForClient(clientId)) {
			try {
				Map<String, String> filesetMap = getFileSet(fileset); 
				String include =  filesetMap.get("include");
				if(include != null) {
					for(String dir : include.split("\n")) {
						if (filesetMap.get("local") != null && filesetMap.get("local").equals("yes")) {
							String volumesCad = filesetMap.get("volumes");
							String[] volumes = volumesCad.split(":::");
							if (dir.indexOf("/") == 0)
								dir = dir.substring(1);
							for (String vol : volumes) {
								if (vol.indexOf("/") == 0)
									vol = vol.substring(1);
								if (NetworkManager.isValidAddress(vol.substring(0, vol.indexOf("/"))))
									includes.add(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/shares/"+vol+"/"+dir);
								else
									includes.add(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+vol+"/"+dir);
							}
						} else
							includes.add(dir);
					}
				}
			} catch(NullPointerException _ex) {}
		}
		return includes;
	}
	
	public Map<String, Map<String, String>> getFileSetIncludesForClientRecovery(int clientId) throws Exception {
		Map<String, Map<String, String>> includes = new HashMap<String, Map<String, String>>();
		for(String fileset : getFileSetsForClient(clientId)) {
			try {
				Map<String, String> filesetMap = getFileSet(fileset); 
				String include =  filesetMap.get("include");
				if(include != null) {
					for(String dir : include.split("\n")) {
						if (filesetMap.get("local") != null && filesetMap.get("local").equals("yes")) {
							String volumesCad = filesetMap.get("volumes");
							if (volumesCad != null && !volumesCad.isEmpty()) {
								List<String> volumes = new ArrayList<String>();
								volumes.addAll(Arrays.asList(volumesCad.split(":::")));
								if (dir.indexOf("/") == 0)
									dir = dir.substring(1);
								if (dir.endsWith("/"))
									dir = dir.substring(dir.length()-1);
								
								if (filesetMap.containsKey("oldVolumes") && !filesetMap.get("oldVolumes").isEmpty()) {
									String oldVolumesCad = filesetMap.get("oldVolumes");
									volumes.addAll(Arrays.asList(oldVolumesCad.split(":::")));
								}
								
								for (String vol : volumes) {
									Map<String, String> mapInclude = new HashMap<String, String>();
									if (vol.indexOf("/") == 0)
										vol = vol.substring(1);
									if (NetworkManager.isValidAddress(vol.substring(0, vol.indexOf("/")))) {
										include = WBSAirbackConfiguration.getDirectoryVolumeMount()+"/shares/"+vol+"/"+dir.trim();
										mapInclude.put("directory", include.substring(0, include.lastIndexOf("/")+1));
										mapInclude.put("file", include.substring(include.lastIndexOf("/")+1));
										if (!include.endsWith("/"))
											include = include+"/";
										mapInclude.put("path", include);
										includes.put(mapInclude.get("path"), mapInclude);
									}
									else {
	//									include = "^"+WBSAirbackConfiguration.getDirectoryVolumeMount()+"/[^/]*/[^/]*/"+dir.trim();
	//									mapInclude.put("directory", include.substring(0, include.lastIndexOf("/")+1)+"$");
	//									mapInclude.put("file", include.substring(include.lastIndexOf("/")+1));
	//									if (!include.endsWith("/"))
	//										include = include+"/$";
										include = WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+vol+"/"+dir.trim();
										mapInclude.put("directory", include.substring(0, include.lastIndexOf("/")+1));
										mapInclude.put("file", include.substring(include.lastIndexOf("/")+1));
										mapInclude.put("path", include);
										includes.put(mapInclude.get("path"), mapInclude);
									}
								}
							}
						} else {
							include = dir.trim();
							Map<String, String> mapInclude = new HashMap<String, String>();
							mapInclude.put("directory", include.substring(0, include.lastIndexOf("/")+1));
							mapInclude.put("file", include.substring(include.lastIndexOf("/")+1));
							if (!include.endsWith("/"))
								include = include+"/";
							mapInclude.put("path", include);
							
							includes.put(mapInclude.get("path"), mapInclude);
						}
					}
				}
			} catch(Exception _ex) {
				logger.error("Error reading fileset includes for recovery of client {}. Ex: {} ", clientId, _ex.getMessage());
			}
		}
		return includes;
	}
	
	public List<String> getFileSetsForClient(int clientId) throws Exception {
		List<String> fileset_names = new ArrayList<String>();
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT FS.fileset FROM job J, fileset FS"); 
		_sb.append(" WHERE J.clientid = ");
		_sb.append(clientId);
		_sb.append(" AND J.filesetid = FS.filesetid");
		_sb.append(" GROUP BY FS.fileset");
		
		for(Map<String, Object> result : this._db.query(_sb.toString())) {
			fileset_names.add(String.valueOf(result.get("fileset")));
		}
		
		Collections.sort(fileset_names, String.CASE_INSENSITIVE_ORDER);
		return fileset_names;
	}
	
	public static List<String> getFilesetPlugins(String name) throws Exception {
		List<String> _plugins = new ArrayList<String>();
		Map<String, String> _fileset = getFileSet(name);
		if(_fileset.get("plugins") != null) {
			StringTokenizer _st = new StringTokenizer(_fileset.get("plugins"), ",");
			while(_st.hasMoreTokens()) {
				_plugins.add(_st.nextToken());
			}
		}
		return _plugins;
	}
	
	public List<String> getFilesetPluginsForClient(int clientId) throws Exception {
		List<String> _plugins = new ArrayList<String>();
		for(String _fileset : getFileSetsForClient(clientId)) {
			for(String _plugin : getFilesetPlugins(_fileset)) {
				if(!_plugins.contains(_plugin)) {
					_plugins.add(_plugin);
				}
			}
		}
		return _plugins;
	}
	
	public static List<String> getLocalFilesetVolumes(String name) throws Exception {
		List<String> _volumes = new ArrayList<String>();
		Map<String, String> _fileset = getFileSet(name);
		if(_fileset.get("volumes") != null) {
			StringTokenizer _st = new StringTokenizer(_fileset.get("volumes"), ":::");
			while (_st.hasMoreTokens()){
				String volume = _st.nextToken().trim();
				_volumes.add(volume);
			}
		}
		return _volumes;
	}
	
	public static Map<String, String> getLocalFileSet(String name) throws Exception {
		Map<String, String> fileset = getFileSet(name);
		return fileset;
	}
	
	public static Map<String, String> getNDMPFilesetVolumes(String name) throws Exception {
		Map<String, String> _volumes = new HashMap<String, String>();
		Map<String, String> _fileset = getFileSet(name);
		if(_fileset.get("ndmp.volumes") != null) {
			BufferedReader _br = new BufferedReader(new StringReader(_fileset.get("ndmp.volumes")));
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
				if(_line.contains(":::")) {
					_volumes.put(_line.substring(0, _line.indexOf(":::")), _line.substring(_line.indexOf(":::") + 3));
				} else {
					_volumes.put(_line, null);
				}
			}
		}
		return _volumes;
	}
	
	public static boolean hasNDMPFileSets() {
		File dir = new File(WBSAirbackConfiguration.getDirectoryFilesets());
		String[] files = dir.list();
		if(files != null) {
			for(String fileset : files) {
				if(fileset.endsWith(".xml")) {
					fileset = fileset.substring(0, fileset.length() - 4);
					try {
						Map<String, String> filesetMap = getFileSet(fileset);
						if(!filesetMap.isEmpty() && filesetMap.get("name") != null && filesetMap.get("ndmp") != null && "yes".equals(filesetMap.get("ndmp"))) {
							return true;
						}
					} catch(Exception _ex) {}
				}
			}
		}
		return false;
	}
	
	public static void updateExternalStorageFileSet(String name, String path, String compression) throws Exception {
		Map<String, String> fileset = getFileSet(name);
		fileset.put("remote_storage", "yes");
		fileset.put("include", path);
		fileset.put("md5", "yes");
		if (compression != null && !compression.equals(COMPRESSION_NONE))
			fileset.put("compression", compression);
		else
			fileset.remove("compression");
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("FileSet {\n");
		_sb.append("\tName = ");
		_sb.append(name);
		_sb.append("\n");
		_sb.append("\tEnable VSS = no\n");
		_sb.append("\tInclude {\n");
		_sb.append("\t\tOptions {\n");
		if(fileset.containsKey("md5")) {
			_sb.append("\t\t\tsignature = MD5\n");
		}
		if(fileset.containsKey("compression")) {
			_sb.append("\t\t\tcompression = ");
			_sb.append(fileset.get("compression"));
			_sb.append("\n");
		}
		_sb.append("\t\t\taclsupport = yes\n");
		_sb.append("\t\t\tonefs = no\n");
		_sb.append("\t\tWildFile = \"/\"\n");
		_sb.append("\t\t}\n");
		if (path != null && !path.isEmpty()) {
			_sb.append("\t\tFile = \"" + path + "\"\n");
		}
		_sb.append("\t}\n");
		_sb.append("}\n");
    	_sb.append("\n");
    	
    	File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".conf");
    	FileSystem.writeFileCharset(_f, _sb.toString(), "UTF-8");
    	
	    _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		Configuration _c = new Configuration(_f);
		for(String key : fileset.keySet()) {
			_c.setProperty("fileset.".concat(key), fileset.get(key));
		}
		_c.store();
		BackupOperator.reload();
	}
	
	
	public static void updateFileSet(String name, String include, String exclude, String extension, boolean md5, String compression, boolean acl, boolean multiplefs, boolean vss, List<Integer> plugins) throws Exception {
		Map<String, String> fileset = getFileSet(name);
		boolean removeSuportVss = false; 
		if(include != null && !include.isEmpty()) {
			if(include.contains(":\\\\")) {
				include = include.replace("\\\\", "/");
			} else if(include.contains(":\\")) {
				include = include.replace("\\", "/");
			}
			fileset.put("include", include);
			if(extension != null && !extension.isEmpty()) {
				fileset.put("extension", extension);
			} else {
				fileset.remove("extension");
			}
			if(md5) {
				fileset.put("md5", "yes");
			} else {
				fileset.remove("md5");
			}
			if(compression != null && !compression.equals(COMPRESSION_NONE)) {
				fileset.put("compression", compression);
			} else {
				fileset.remove("compression");
			}
			if(acl) {
				fileset.put("acl", "yes");
			} else {
				fileset.remove("acl");
			}
			if(multiplefs) {
				fileset.put("multiplefs", "yes");
			} else {
				fileset.remove("multiplefs");
			}
		} else {
			fileset.remove("include");
		}
		if(exclude != null && !exclude.isEmpty()) {
			if(exclude.contains(":\\\\")) {
				exclude = exclude.replace("\\\\", "/");
			} else if(exclude.contains(":\\")) {
				exclude = exclude.replace("\\", "/");
			}
			fileset.put("exclude", exclude);
		} else {
			fileset.remove("exclude");
		}
		if(plugins != null && plugins.size()>0) {
			StringBuilder _plugins = new StringBuilder();
			for(Integer _plugin : plugins) {
				if(_plugins.length() > 0) {
					_plugins.append(",");
				}
				switch(_plugin) {
					case PLUGIN_SYSTEMSTATE: {
							_plugins.append(SUPPORTED_PLUGINS.get(PLUGIN_SYSTEMSTATE));
							removeSuportVss=true;
						}
						break;
					case PLUGIN_SHAREPOINT: {
							_plugins.append(SUPPORTED_PLUGINS.get(PLUGIN_SHAREPOINT));
							removeSuportVss=true;
						}
						break;
					case PLUGIN_MSSQL: {
							_plugins.append(SUPPORTED_PLUGINS.get(PLUGIN_MSSQL));
							removeSuportVss=true;
						}
						break;
					case PLUGIN_EXCHANGE: {
							_plugins.append(SUPPORTED_PLUGINS.get(PLUGIN_EXCHANGE));
							removeSuportVss=true;
						}
						break;
				}
			}
			fileset.put("plugins", _plugins.toString());
		} else {
			fileset.remove("plugins");
		}
		if(vss) {
			fileset.put("vss", "yes");
		} else {
			fileset.remove("vss");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("FileSet {\n");
		_sb.append("\tName = ");
		_sb.append(name);
		_sb.append("\n");
		if (!removeSuportVss) {
			if(fileset.containsKey("vss")) {
				_sb.append("\tEnable VSS = yes\n");
			} else {
				_sb.append("\tEnable VSS = no\n");
			}
		}
		
		if(fileset.containsKey("include") || plugins != null) {
			_sb.append("\tInclude {\n");
			if(fileset.containsKey("include")) {
				_sb.append("\t\tOptions {\n");
				if(fileset.containsKey("md5")) {
					_sb.append("\t\t\tsignature = MD5\n");
				}
				if(fileset.containsKey("compression")) {
					_sb.append("\t\t\tcompression = ");
					_sb.append(fileset.get("compression"));
					_sb.append("\n");
				}
				if(fileset.containsKey("acl")) {
					_sb.append("\t\t\taclsupport = yes\n");
				}
				if(fileset.containsKey("multiplefs")) {
					_sb.append("\t\t\tonefs = no\n");
				}
		
				if(fileset.containsKey("extension")) {
					extension.replaceAll(" ", "");
					StringTokenizer _st = new StringTokenizer(extension, ",");
					if(_st.countTokens() > 0) {
						_sb.append("\t\t\tExclude = yes\n");
					}
					while(_st.hasMoreTokens()) {
						_sb.append("\t\t\tWildFile = \"" + _st.nextToken() + "\"\n");
					}
				}
				_sb.append("\t\t}\n");
				StringTokenizer _st = new StringTokenizer(include, "\n");
				while(_st.hasMoreTokens()) {
					String _file = _st.nextToken().replace("\r", "");
					if(!_file.trim().isEmpty()) {
						_sb.append("\t\tFile = \"" + _file + "\"\n");	
					}
				}
			}
			
			if(plugins != null) {
				StringBuilder _plugins = new StringBuilder();
				for(Integer _plugin : plugins) {
					if(_plugins.length() > 0) {
						_plugins.append(",");
					}
					switch(_plugin) {
						case PLUGIN_SYSTEMSTATE: {
								_sb.append("\t\tPlugin = \"vss:/@SYSTEMSTATE/\"\n");
							}
							break;
						case PLUGIN_SHAREPOINT: {
								_sb.append("\t\tPlugin = \"vss:/@SHAREPOINT/\"\n");
							}
							break;
						case PLUGIN_MSSQL: {
								_sb.append("\t\tPlugin = \"vss:/@MSSQL/\"\n");
							}
							break;
						case PLUGIN_EXCHANGE: {
								_sb.append("\t\tPlugin = \"vss:/@EXCHANGE/\"\n");
							}
							break;
					}
				}
			}
			_sb.append("\t}\n");
		}
		
		if(fileset.containsKey("exclude")) {
			_sb.append("\tExclude {\n");
			StringTokenizer _st = new StringTokenizer(exclude, "\n");
			while(_st.hasMoreTokens()) {
				String _file = _st.nextToken().replace("\r", "");
				if(!_file.trim().isEmpty()) {
					_sb.append("\t\tFile = \"" + _file + "\"\n");
				}
			}
			_sb.append("\t}\n");
		}
		_sb.append("}\n");
    	_sb.append("\n");
    	
    	File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".conf");
    	FileSystem.writeFileCharset(_f, _sb.toString(), "UTF-8");
    	
    	_f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		Configuration _c = new Configuration(_f);
		for(String key : fileset.keySet()) {
			_c.setProperty("fileset.".concat(key), fileset.get(key));
		}
		_c.store();
		BackupOperator.reload();
	}
	
	public static void updateLocalFileSet(String name, List<String> volumes, String include, String extension, boolean md5, String compression) throws Exception {
		Map<String, String> fileset = getFileSet(name);
		StringBuilder _complete_includes = new StringBuilder();
		StringBuilder _relative_includes = new StringBuilder();
		List<String> _volumes = new ArrayList<String>();
		List<String> _pools = new ArrayList<String>();
		
		fileset.put("local", "yes");
		StringBuilder _sbVol = new StringBuilder();
		int c=0;
		for(String _volume : volumes) {
			if(_volume.trim().isEmpty()) {
				continue;
			}
			
			if (_sbVol.length() > 0)
				_sbVol.append(":::");
			_sbVol.append(_volume);
			
			String[] _value = new String[2];
			_value[0] = _volume.substring(0, _volume.indexOf("/"));
			_value[1] = _volume.substring(_volume.indexOf("/") + 1);
			if(_value.length != 2) {
				throw new Exception("invalid local volume");
			}
			if(VolumeManager.isMount(_value[0], _value[1])) {
				String _mount = VolumeManager.getLogicalVolumeMountPath(_value[0], _value[1]);
				_volumes.add(VolumeManager.getLogicalVolumeMountPath(_value[0], _value[1]));
				if(include != null && !include.isEmpty()) {
					if(include.contains("\\")) {
						include = include.replace("\\", "/");
					}
					StringTokenizer _st = new StringTokenizer(include, "\n");
					while(_st.hasMoreTokens()) {
						String _tok = _st.nextToken();
						_tok = _tok.replace("\r", "");
						if (! (_complete_includes.length() > 0 && _tok.isEmpty()) ) {
							if (c == 0)									// No debemos guardar repetidos los patrones
								_relative_includes.append(_tok+"\n");
							if(!_tok.startsWith("/")) {
								_tok = "/".concat(_tok);
							}
							_complete_includes.append(_mount.concat(_tok));
							_complete_includes.append("\n");
						}
					}
				} else {
					_complete_includes.append(_mount);
					_complete_includes.append("\n");
				}
			} else if(ShareManager.isExternalShare(_value[0], _value[1])) {
				String _mount = ShareManager.getExternalShareMountPath(_value[0], _value[1]);
				if(include != null && !include.isEmpty()) {
					if(include.contains("\\")) {
						include = include.replace("\\", "/");
					}
					StringTokenizer _st = new StringTokenizer(include, "\n");
					while(_st.hasMoreTokens()) {
						String _tok = _st.nextToken();
						_tok = _tok.replace("\r", "");
						if (! (_complete_includes.length() > 0 && _tok.isEmpty()) ) {
							if (c == 0)									// No debemos guardar repetidos los patrones
								_relative_includes.append(_tok+"\n");
							if(!_tok.startsWith("/")) {
								_tok = "/".concat(_tok);
							}
							_complete_includes.append(_mount.concat(_tok));
							_complete_includes.append("\n");
						}
					}
				} else {
					_complete_includes.append(_mount);
					_complete_includes.append("\n");
				}
			} else {
				throw new Exception("invalid local volume");
			}
			for(String _storage : StorageManager.getDiskVolumeDevicesNamesForLogicalVolume(_value[0], _value[1])) {
				for(String _pool : PoolManager.getPoolsForStorage(_storage)) {
					_pools.add(VolumeManager.getLogicalVolumeMountPath(_value[0], _value[1]) + "/" + _pool);
				}
			}
			c++;
		}
		
		if (_sbVol.length()>0)
			fileset.put("volumes", _sbVol.toString());
		fileset.put("include", _relative_includes.toString());
		if(extension != null && !extension.isEmpty()) {
			fileset.put("extension", extension);
		} else {
			fileset.remove("extension");
		}
		if(md5) {
			fileset.put("md5", "yes");
		} else {
			fileset.remove("md5");
		}
		if(compression != null && !compression.equals(COMPRESSION_NONE)) {
			fileset.put("compression", compression);
		} else {
			fileset.remove("compression");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("FileSet {\n");
		_sb.append("\tName = ");
		_sb.append(name);
		_sb.append("\n");
		_sb.append("\tEnable VSS = no\n");
		_sb.append("\tInclude {\n");
		_sb.append("\t\tOptions {\n");
		if(fileset.containsKey("md5")) {
			_sb.append("\t\t\tsignature = MD5\n");
		}
		if(fileset.containsKey("compression")) {
			_sb.append("\t\t\tcompression = ");
			_sb.append(fileset.get("compression"));
			_sb.append("\n");
		}
		_sb.append("\t\t\taclsupport = yes\n");
		_sb.append("\t\t\tonefs = no\n");
		if(fileset.containsKey("extension") || !_pools.isEmpty() || !_volumes.isEmpty()) {
			_sb.append("\t\t\tExclude = yes\n");
			if(fileset.containsKey("extension")) {
				List<String> extensions = StringFormat.getList(extension);
				for (String ex : extensions) {
					_sb.append("\t\t\tWildFile = \"");
					_sb.append(ex);
					_sb.append("\"\n");
				}
			}
			if(!_pools.isEmpty()) {
				for(String _pool : _pools) {
					_sb.append("\t\t\tWildFile = \"");
					_sb.append(_pool);
					_sb.append("*\"\n");
				}
			}
			if(!_volumes.isEmpty()) {
				for(String _volume : _volumes) {
					_sb.append("\t\t\tWildDir = \"");
					_sb.append(_volume);
					_sb.append("/.snapshots/\"\n");
					_sb.append("\t\t\tWildFile = \"");
					_sb.append(_volume);
					_sb.append("/airback-sd.data.*.spool\"\n");
				}
			}
		}
		_sb.append("\t\t}\n");
		
		StringTokenizer _st = new StringTokenizer(_complete_includes.toString(), "\n");
		while(_st.hasMoreTokens()) {
			String _file = _st.nextToken().replace("\r", "");
			if(!_file.trim().isEmpty()) {
				_sb.append("\t\tFile = \"");
				_sb.append(_file);
				_sb.append("\"\n");	
			}
		}
		_sb.append("\t}\n");
		_sb.append("}\n");
    	_sb.append("\n");
    	
    	File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".conf");
    	FileSystem.writeFileCharset(_f, _sb.toString(), "UTF-8");
    	
	    _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		Configuration _c = new Configuration(_f);
		for(String key : fileset.keySet()) {
			_c.setProperty("fileset.".concat(key), fileset.get(key));
		}
		_c.store();
		BackupOperator.reload();
	}
	
	public void updateVmwareFileset(String name, String root, String client, List<String> vcenters, List<String> hosts, List<String> datastores, List<String> machines, String virtualElements, boolean cbt, String compression, boolean md5, Map<String, Map<String, List<String>>> virtualTree) throws Exception {
		Map<String, String> fileset = getFileSet(name);
		
		List<String> oldAllVms = new ArrayList<String> ();
		if (fileset.containsKey("allVirtualMachines")) {
			String cadAllVirtualMachines = (String) fileset.get("allVirtualMachines");
			if (!cadAllVirtualMachines.isEmpty()) {
				String [] macs = cadAllVirtualMachines.split(",");
				oldAllVms = Arrays.asList(macs);
			}
		}
		
		fileset.put("vmware", "yes");
		fileset.put("virtualElements", virtualElements);
		if (cbt)
			fileset.put("cbt", "yes");
		else
			fileset.put("cbt", "no");
		
		if (md5)
			fileset.put("md5", "yes");
		else
			fileset.put("md5", "no");
		
		if (compression != null && !compression.isEmpty())
			fileset.put("compression", compression);
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("FileSet {\n");
		_sb.append("\tName = ");
		_sb.append(name);
		_sb.append("\n");
		_sb.append("\tInclude {\n");
		_sb.append("\t\tOptions {\n");
		if(fileset.containsKey("md5") && fileset.get("md5").equals("yes")) {
			_sb.append("\t\t\tsignature = MD5\n");
		}
		if(fileset.containsKey("compression") && !fileset.get("compression").equals(FileSetManager.COMPRESSION_NONE)) {
			_sb.append("\t\t\tcompression = ");
			_sb.append(fileset.get("compression"));
			_sb.append("\n");
		}
		_sb.append("\t\t}\n");
		
		TreeSet<String> vms = new TreeSet<String>();
		List<String> allVms = new ArrayList<String>();
		boolean fullServer = false;
		
		if (vcenters != null && vcenters.size() > 0) {		// Caso seleccion vcenter
			logger.debug("VMware filesets: seleccion directa de vcenter {} ", root);
			fullServer = true;
			_sb.append("\t\tPlugin = \"vsphere:");
			if (!cbt)
				_sb.append(" keep_cbt");
			_sb.append(" server=");
			_sb.append(root);
			_sb.append("\"\n");
		} else if (hosts != null && hosts.size() > 0) {		
			if (hosts.contains(root)) {						// Caso seleccion host, pero es el raiz
				logger.debug("VMware filesets: seleccion de host {}, aunque es el raiz ", root);
				fullServer = true;
				_sb.append("\t\tPlugin = \"vsphere:");
				if (!cbt)
					_sb.append(" keep_cbt");
				_sb.append(" server=");
				_sb.append(root);
				_sb.append("\"\n");
			} else {
				for (String host : hosts) {					// Caso seleccion algun host, pero sin ser el raiz
					List<String> hostMachines = findHostMachinesOnTree(host, virtualTree);
					if (hostMachines != null && !hostMachines.isEmpty()) {
						logger.debug("VMware filesets: Añadimos todas las máquinas del host {}: {}",host, hostMachines);
						vms.addAll(hostMachines);
					}
				}
			}
		}
		
		if (!fullServer) {
			if (datastores != null && !datastores.isEmpty()) {
				for (String ds : datastores) {
					List<String> dsMachines = findDatastoreMachinesOnTree(ds, virtualTree);
					if (dsMachines != null && !dsMachines.isEmpty())
						vms.addAll(dsMachines);
				}
			}
			
			if (machines != null && !machines.isEmpty()) 
				vms.addAll(machines);
		
			if (!vms.isEmpty()) {
				for(String vm : vms) {
					_sb.append("\t\tPlugin = \"vsphere:");
					if (!cbt)
						_sb.append(" keep_cbt");
					_sb.append(" server=");
					_sb.append(root);
					_sb.append(" host=\\\"");
					_sb.append(vm);
					_sb.append("\\\"");
					_sb.append("\"\n");
				}
				allVms.addAll(vms);
			}
		}
		_sb.append("\t}\n");
		_sb.append("}\n");
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".conf");
    	FileSystem.writeFileCharset(_f, _sb.toString(), "UTF-8");
    	
	    _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		Configuration _c = new Configuration(_f);
		for(String key : fileset.keySet()) {
			_c.setProperty("fileset.".concat(key), fileset.get(key));
		}
		
		allVms.addAll(oldAllVms);
		String allVmsCad = "";
		for (String vm : allVms) {
			if (allVmsCad.equals("")) {
				allVmsCad = vm;
			} else {
				allVmsCad+=","+vm;
			}
		}
		_c.setProperty("fileset.allVirtualMachines", allVmsCad);
		_c.setProperty("fileset.vmwareClient", client);
		_c.store();
		BackupOperator.reload();
	}
	
	public List<String> findHostMachinesOnTree(String host, Map<String, Map<String, List<String>>> tree) throws Exception {
		List<String> vms = new ArrayList<String>();
		try {
			if (tree != null && !tree.isEmpty() && host != null && !host.isEmpty()) {
				if (tree.containsKey(host)) {
					Map<String, List<String>> dssHost = tree.get(host);
					if (dssHost != null && !dssHost.isEmpty()) {
						for (String ds : dssHost.keySet()) {
							if (dssHost.get(ds) != null && !dssHost.get(ds).isEmpty())
								vms.addAll(dssHost.get(ds));
						}
					}
				}
			}
			
		} catch (Exception ex) {
			logger.error("Error buscando máquinas virtuales pertenecientes a host {} en el árbol, al crear el fileset. Ex: {}", host, ex.getMessage());
		}
		return vms;
	}
	
	public List<String> findDatastoreMachinesOnTree(String ds, Map<String, Map<String, List<String>>> tree) throws Exception {
		String host = ds.substring(0, ds.indexOf(FileSetManager.VHOST));
		String dsName = ds.substring(ds.indexOf(FileSetManager.VHOST)+FileSetManager.VHOST.length());
		try {
			if (tree != null && !tree.isEmpty() && host != null && !host.isEmpty()) {
				if (tree.containsKey(host)) {
					Map<String, List<String>> dssHost = tree.get(host);
					if (dssHost != null && !dssHost.isEmpty() && dssHost.containsKey(dsName)) {
						return dssHost.get(dsName);
					}
				}
			}
			
		} catch (Exception ex) {
			logger.error("Error buscando máquinas virtuales pertenecientes a host {} en el árbol, al crear el fileset. Ex: {}", host, ex.getMessage());
		}
		return null;
	}
	
	public List<String> getVirtualMachinesOfFileset(String name, String client) throws Exception {
		Map<String, String> fileset = getFileSet(name);
		List<String> vms = new ArrayList<String>();
		if (fileset != null && !fileset.isEmpty()) {
			if (fileset.containsKey("allVirtualMachines")) {
				String cadAllVirtualMachines = (String) fileset.get("allVirtualMachines");
				if (!cadAllVirtualMachines.isEmpty()) {
					String [] machines = cadAllVirtualMachines.split(",");
					vms = Arrays.asList(machines);
				}
			}
		}
		logger.debug("Para el fileset {} y el cliente {}, obtenemos estas vms: {}", new Object[]{name, client, vms});
		return vms;
	}
	
	
	public static void updateNDMPFileSet(String name, String[] address, int port, int ndmp_auth, String user, String password, Map<String, String> volumes, int ndmp_type) throws Exception {
		Map<String, String> fileset = getFileSet(name);
		StringBuilder _volumes = new StringBuilder();
		
		if(!NetworkManager.isValidAddress(address)) {
			throw new Exception("invalid NDMP network address");
		}
		if(port <= 0) {
			throw new Exception("invalid NDMP network port");
		}
		if(NDMP_AUTH_NONE != ndmp_auth &&
				NDMP_AUTH_TEXT != ndmp_auth &&
				NDMP_AUTH_MD5 != ndmp_auth) {
			throw new Exception("invalid NDMP network auth");
		}
		if(NDMP_TYPE_DUMP != ndmp_type &&
				NDMP_TYPE_TAR != ndmp_type &&
				NDMP_TYPE_SMTAPE != ndmp_type) {
			throw new Exception("invalid NDMP format type");
		}
		if(user != null && user.contains(" ")) {
			throw new Exception("invalid NDMP user");
		}
		if(password != null && password.contains(" ")) {
			throw new Exception("invalid NDMP password");
		}
		
		fileset.put("ndmp", "yes");
		fileset.put("ndmp.address", NetworkManager.addressToString(address));
		fileset.put("ndmp.port", String.valueOf(port));
		switch(ndmp_auth) {
			case NDMP_AUTH_NONE: {
					fileset.put("ndmp.auth", "none");
				}
				break;
			case NDMP_AUTH_TEXT: {
					fileset.put("ndmp.auth", "text");
				}
				break;
			case NDMP_AUTH_MD5: {
					fileset.put("ndmp.auth", "md5");
				}
				break;
		}
		if(user != null && !user.isEmpty()) {
			fileset.put("ndmp.user", user);
			if(password != null && !password.isEmpty()) {
				fileset.put("ndmp.password", password);
			} else if(fileset.containsKey("ndmp.password")) {
				fileset.remove("ndmp.password");
			}
		} else if(fileset.containsKey("ndmp.user")) {
			fileset.remove("ndmp.user");
		}
		
		switch(ndmp_type) {
			case NDMP_TYPE_DUMP: {
					fileset.put("ndmp.type", "dump");
				}
				break;
			case NDMP_TYPE_TAR: {
					fileset.put("ndmp.type", "tar");
				}
				break;
			case NDMP_TYPE_SMTAPE: {
					fileset.put("ndmp.type", "SMTAPE");
				}
				break;
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("FileSet {\n");
		_sb.append("\tName = ");
		_sb.append(name);
		_sb.append("\n");
		
		_sb.append("\tInclude {\n");
		for(String _volume : volumes.keySet()) {
			if(_volume.trim().isEmpty()) {
				continue;
			}
			_sb.append("\t\tPlugin = \"ndmp:host=");
			_sb.append(fileset.get("ndmp.address"));
			_sb.append(" port=");
			_sb.append(fileset.get("ndmp.port"));
			_sb.append(" auth=");
			_sb.append(fileset.get("ndmp.auth"));
			if(fileset.containsKey("ndmp.user")) {
				_sb.append(" user=");
				_sb.append(fileset.get("ndmp.user"));
				if(fileset.containsKey("ndmp.password")) {
					_sb.append(" pass=");
					_sb.append(fileset.get("ndmp.password"));
				}
			}
			_sb.append(" volume=");
			_sb.append(_volume);
			if(_volumes.length() > 0) {
				_volumes.append("\n");
			}
			_volumes.append(_volume);
			if(volumes.get(_volume) != null && !volumes.get(_volume).isEmpty()) {
				_sb.append(" file=");
				_sb.append(volumes.get(_volume));
				_volumes.append(":::");
				_volumes.append(volumes.get(_volume));
			}
			if(fileset.containsKey("ndmp.type")) {
				_sb.append(" type=");
				_sb.append(fileset.get("ndmp.type"));
			}
			_sb.append("\"\n");
		}
		fileset.put("ndmp.volumes", _volumes.toString());
		_sb.append("\t}\n");
		_sb.append("}\n");
    	
    	File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".conf");
    	FileSystem.writeFileCharset(_f, _sb.toString(), "UTF-8");
    	
	    _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + name + ".xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		Configuration _c = new Configuration(_f);
		for(String key : fileset.keySet()) {
			_c.setProperty("fileset.".concat(key), fileset.get(key));
		}
		_c.store();
		BackupOperator.reload();
	}
	
	public static void removeFileSet(String name) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		List<String> _jobs = JobManager.getJobsForFileset(name);
		if(!_jobs.isEmpty()) {
			throw new Exception("fileset still used by job [" + _jobs.get(0) + "]");
		}
		
		BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", name);
	    BaculaConfiguration.trimBaculaFile("/etc/bacula/bacula-dir.conf");
	    BackupOperator.reload();
	}
	
	public void updateVmwareFilesetsOfClient(String clientName, String clientVName) throws Exception {
		List<Map<String, String>> filesetsClient = FileSetManager.getVmwareFilesetsOfClient(clientName);
    	for (Map<String, String> fileset: filesetsClient) {
			try {
				Command.systemCommand("sed -i -e 's/server="+clientName+"/server="+clientVName+"/g' "+WBSAirbackConfiguration.getDirectoryFilesets()+"/"+fileset.get("name")+".conf");
			} catch (Exception ex) {
				logger.error("Error actualizando el nombre del cliente vmware {} para que sea el nuevo vsphere principal. Ex: {}"+ex.getMessage());
			}
			File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + fileset.get("name") + ".xml");
			if(_f.exists()) {
				_f.delete();
			}
			
			_c.setProperty("fileset.vmwareClient", clientName);
			Configuration _c = new Configuration(_f);
			for(String key : fileset.keySet()) {
				_c.setProperty("fileset.".concat(key), fileset.get(key));
			}
			_c.store();
    	}
		BackupOperator.reload();
	}
}
