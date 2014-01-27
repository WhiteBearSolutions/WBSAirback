package com.whitebearsolutions.imagine.wbsairback.backup;

import java.io.File;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BaculaBase64;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Configuration;

public class FileManager {
	private Configuration _c;
	private DBConnection connection;
	
	private final static Logger logger = LoggerFactory.getLogger(FileManager.class);
	
	public FileManager(Configuration conf) throws Exception {
		this._c = conf;
		this.connection = new DBConnectionManager(this._c).getConnection();
	}
	
	public TreeMap<String, Map<String, String>> getRootClientFiles(Integer clientId, FileSetManager _fsm, boolean localClient) throws Exception {
		
		TreeMap<String, Map<String, String>> files = new TreeMap<String, Map<String, String>>(Collator.getInstance(new Locale("es")));
		Map<String, Map<String, String>> includes = _fsm.getFileSetIncludesForClientRecovery(clientId);
		if (includes.isEmpty())
			return files;

		List<Map<String, Object>> result = null;
		for (String path : includes.keySet()) {
			String sPath = path;
			String sDir = includes.get(path).get("directory");
			Map<String, String> include = includes.get(path);
			if (!sPath.contains(WBSAirbackConfiguration.getDirectoryVolumeMount()) || sPath.contains("shares")) {
				if((!sPath.matches("^[a-zA-Z]:/") && !sPath.matches("^[a-zA-Z]:/[\\w\\W]+")) && !sPath.startsWith("/")) {
					sPath = "/".concat(sPath);
					sDir = "/".concat(sDir);
				}
				if((!sPath.matches("^[a-zA-Z]:/") && !sPath.matches("^[a-zA-Z]:/[\\w\\W]+")) && !sPath.endsWith("/")) {
					sPath = sPath.concat("/");
					sDir = sDir.concat("/");
				} else if((sPath.matches("^[a-zA-Z]:/") || sPath.matches("^[a-zA-Z]:/[\\w\\W]+")) && !sPath.endsWith("/")) {
					if(!sPath.endsWith(":/")) {
						sPath = sPath.concat("/");
						sDir = sDir.concat("/");
					}
				}
				if (sPath != null && !sPath.isEmpty() && !sPath.endsWith("/"))
					sPath+="/";
				sPath = "P.path = '"+sPath+"'";
				sDir = "P.path = '"+sDir+"'";
			} else {
				if (sPath != null && !sPath.isEmpty() && !sPath.endsWith("/"))
					sPath+="/";
				sPath = "P.path = '"+sPath+"'";
				sDir = "P.path = '"+sDir+"'";
			}
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("SELECT  F1.filenameid, P.path, F1.pathid, F1.fileid, F1.lstat, J.jobid, F2.name, true as directory FROM job J, file F1, filename F2, path P WHERE J.clientid = "+clientId+" AND J.jobid = F1.jobid AND F1.pathid = P.pathid AND "+sPath+" AND F1.filenameid = F2.filenameid ORDER BY J.jobid DESC");
			logger.debug("Query lista restore dir ... {}", _sb.toString());
			this.connection.transactionInit();
			this.connection.transactionQuery("SET enable_seqscan = off");
			result = this.connection.transactionQuery(_sb.toString());
			this.connection.transactionClose();
			
			if (result.isEmpty() && include.get("file") != null && !include.get("file").isEmpty()) {
				_sb = new StringBuilder();
				_sb.append("SELECT  F1.filenameid, P.path, F1.pathid, F1.fileid, F1.lstat, J.jobid, F2.name, false as directory FROM job J, file F1, filename F2, path P WHERE J.clientid = "+clientId+" AND J.jobid = F1.jobid AND F1.pathid = P.pathid AND "+sDir+" AND F2.name <> '' AND F1.filenameid = F2.filenameid ORDER BY J.jobid DESC");
				logger.debug("Query ... {}", _sb.toString());
				this.connection.transactionInit();
				this.connection.transactionQuery("SET enable_seqscan = off");
				result = this.connection.transactionQuery(_sb.toString());
				this.connection.transactionClose();
				for(Map<String, Object> row : result) {
					if (row.get("name") != null && ((String)row.get("name")).equals(include.get("file"))) {
						addFileRecovert(files, row);
					}
				}
			} else {
				for(Map<String, Object> row : result) {
					if (row.get("name") != null && ((String)row.get("name")).isEmpty()) {
						addFileRecovert(files, row);
					}
				}
			}
		}
		return files;
	}
	
	private void addFileRecovert(Map<String, Map<String, String>> files, Map<String, Object> row ) throws Exception {
		Map<String, String> file = new HashMap<String, String>();
		String name = (String) row.get("name");
		if (name.isEmpty())
			name = String.valueOf(row.get("path"));
		if (!name.matches("[a-zA-Z]:") && !name.matches("[a-zA-Z]:/")) {
			while (name.endsWith("/"))
				name = name.substring(0, name.length()-1);
			if (name.contains("/"))
				name = name.substring(name.lastIndexOf("/")+1,name.length());
		} 
		file.put("directory", String.valueOf(row.get("directory")));
		if (file.get("directory").equals("false")) {
			file.put("path", String.valueOf(row.get("path"))+name);	
		} else {
			file.put("path", String.valueOf(row.get("path")));
		}
		file.put("name", name);
		if(row.get("lstat") != null) {
			String _lstat = String.valueOf(row.get("lstat"));
			if(!_lstat.isEmpty()) {
				String[] _data = _lstat.split(" ");
				long _size = BaculaBase64.decode(_data[7]);
                file.put("size", getStringSize(_size));
                file.put("last-modified", new Timestamp(BaculaBase64.decode(_data[11])*1000).toString());
			} else {
				file.put("size", "unknown");
                file.put("last-modified", "unknown");
			}
		} else {
			file.put("size", "unknown");
            file.put("last-modified", "unknown");
		}
		files.put(file.get("path"), file);
	}
	
	public List<Map<String, String>> getClientFiles(int clientId, String clientName, String directory, int limit, int offset) throws Exception {
		TreeMap<String, Map<String, String>> files = new TreeMap<String, Map<String, String>>(Collator.getInstance(new Locale("es")));
		ClientManager _cm = new ClientManager(this._c);
		if(directory == null || directory.isEmpty()) {
			try {
				FileSetManager _fsm = new FileSetManager(this._c);
				
				if (clientId == 0) { 											//vmware
					Map<String, String> file = new HashMap<String, String>();
					file.put("name", "/@vsphere");
					file.put("path", "/@vsphere");
					file.put("directory", "true");
					file.put("size", "");
	                file.put("last-modified", "");
					files.put(file.get("path"), file);
				} else {
					boolean localClient = false;
					if (_cm.isLocalClient(clientId))
						localClient = true;
					files = getRootClientFiles(clientId, _fsm, localClient);
					
					if(localClient) {
						if(FileSetManager.hasNDMPFileSets()) {
							Map<String, String> file = new HashMap<String, String>();
							file.put("name", "/@ndmp");
							file.put("path", "/@ndmp");
							file.put("directory", "true");
							file.put("size", "");
			                file.put("last-modified", "");
							files.put(file.get("path"), file);
						}
					} else {
						for(String plugin : _fsm.getFilesetPluginsForClient(clientId)) {
							if(plugin.equals(FileSetManager.SUPPORTED_PLUGINS.get(FileSetManager.PLUGIN_SYSTEMSTATE))) {
								Map<String, String> file = new HashMap<String, String>();
								file.put("name", "/@SYSTEMSTATE");
								file.put("path", "/@SYSTEMSTATE");
								file.put("directory", "true");
								file.put("size", "");
				                file.put("last-modified", "");
								files.put(file.get("path"), file);
							} else if(plugin.equals(FileSetManager.SUPPORTED_PLUGINS.get(FileSetManager.PLUGIN_SHAREPOINT))) {
								Map<String, String> file = new HashMap<String, String>();
								file.put("name", "/@SHAREPOINT");
								file.put("path", "/@SHAREPOINT");
								file.put("directory", "true");
								file.put("size", "");
				                file.put("last-modified", "");
								files.put(file.get("path"), file);
							} else if(plugin.equals(FileSetManager.SUPPORTED_PLUGINS.get(FileSetManager.PLUGIN_MSSQL))) {
								Map<String, String> file = new HashMap<String, String>();
								file.put("name", "/@MSSQL");
								file.put("path", "/@MSSQL");
								file.put("directory", "true");
								file.put("size", "");
				                file.put("last-modified", "");
								files.put(file.get("path"), file);
							} else if(plugin.equals(FileSetManager.SUPPORTED_PLUGINS.get(FileSetManager.PLUGIN_EXCHANGE))) {
								Map<String, String> file = new HashMap<String, String>();
								file.put("name", "/@EXCHANGE");
								file.put("path", "/@EXCHANGE");
								file.put("directory", "true");
								file.put("size", "");
				                file.put("last-modified", "");
								files.put(file.get("path"), file);
							}
						}
					}
				}
				return new ArrayList<Map<String,String>>(files.values());
			} catch(Exception _ex) {
				directory = "/";
			}
			directory = "/";
		}
		
		boolean isVmware = false;
		String vmware = ClientManager.getVMWareIncludeQuery(clientName, "J");
		if (clientId == 0) {
			clientId = _cm.getClientId("airback-fd");
			isVmware = true;
		} else
			vmware = ClientManager.getVMWareExcludeQuery("J");
		
		if((!directory.matches("^[a-zA-Z]:/") && !directory.matches("^[a-zA-Z]:/[\\w\\W]+")) && !directory.startsWith("/")) {
			directory = "/".concat(directory);
		}
		if((!directory.matches("^[a-zA-Z]:/") && !directory.matches("^[a-zA-Z]:/[\\w\\W]+")) && !directory.endsWith("/")) {
			directory = directory.concat("/");
		} else if((directory.matches("^[a-zA-Z]:/") || directory.matches("^[a-zA-Z]:/[\\w\\W]+")) && !directory.endsWith("/")) {
			if(!directory.endsWith(":/")) {
				directory = directory.concat("/");
			}
		}
		
		String directoryEscaped = escapePostgreChars(directory);
		
		if (!isVmware || (vmware != null && !vmware.isEmpty())) {
			try {
				this.connection.transactionInit();
				this.connection.transactionQuery("SET enable_seqscan = off");
				
				StringBuilder _sb = new StringBuilder();
				_sb.append("SELECT X.name as name, X.lstat, X.directory ");
				_sb.append("FROM ( ");
				_sb.append("SELECT DISTINCT ON (F3.name) F3.name, F3.lstat, false AS directory, F3.jobid ");
				_sb.append("FROM path P, ( ");
				_sb.append("SELECT F2.pathid, F2.path, F2.fileid, F2.lstat, FN.name, F2.jobid ");
				_sb.append("FROM  filename FN, (select  F1.filenameid, P.path, F1.pathid, F1.fileid, ");
				_sb.append("F1.lstat, J.jobid FROM job J, file F1, path P ");
				_sb.append("WHERE J.clientid = ");
				_sb.append(clientId);
				_sb.append(vmware);
				_sb.append(" AND J.jobid = F1.jobid AND ");
				_sb.append("F1.pathid = P.pathid and P.path = '");
				_sb.append(directory);
				_sb.append("') F2 ");
				//_sb.append("WHERE F2.filenameid = FN.filenameid ORDER BY F2.path, F2.jobid DESC");
				_sb.append("WHERE F2.filenameid = FN.filenameid");
				_sb.append(") AS F3 ");
				_sb.append("WHERE P.pathid = F3.pathid AND name <> '' ");
				_sb.append("UNION ");
				if(directory.startsWith("/@SYSTEMSTATE") ||
						directory.startsWith("/@SHAREPOINT") ||
						directory.startsWith("/@MSSQL") ||
						directory.startsWith("/@EXCHANGE")) {
					StringBuilder _function = new StringBuilder();
					_function.append("CREATE OR REPLACE FUNCTION CountInString(text,text)");
					_function.append(" RETURNS integer AS $$");
					_function.append(" SELECT(Length($1) - Length(REPLACE($1, $2, ''))) / Length($2);");
					_function.append(" $$ LANGUAGE SQL IMMUTABLE;");
					this.connection.transactionQuery(_function.toString());
					
					_sb.append("SELECT DISTINCT Qcount.path AS name, F1.lstat, true AS directory, J.jobid");
					_sb.append(" FROM (SELECT pathid, path, CountInString(path,'/') AS countBars");
					_sb.append(" FROM path WHERE path LIKE '");
					_sb.append(directory);
					_sb.append("%' AND path <> '");
					_sb.append(directory);
					_sb.append("') QCount, file F1, job J");
					_sb.append(" WHERE Qcount.countBars = (SELECT MIN(QCount2.countBars)");
					_sb.append(" FROM (SELECT path, CountInString(path,'/') AS countBars");
					_sb.append(" FROM path WHERE path LIKE '");
					_sb.append(directory);
					_sb.append("%' AND path <> '");
					_sb.append(directory);
					_sb.append("') QCount2) AND QCount.pathid = F1.pathid");
					_sb.append(" AND F1.jobid = J.jobid AND J.clientId = ");
					_sb.append(clientId);
					//_sb.append(" ORDER BY name DESC");
				} else {
					_sb.append("SELECT DISTINCT ON (F3.name) F3.name, F3.lstat, true AS directory, F3.jobid ");
					_sb.append("FROM path P, ( ");
					_sb.append("SELECT F2.pathid, F2.path AS name, F2.fileid, F2.lstat, F2.jobid ");
					_sb.append("FROM  filename FN, (SELECT  F1.filenameid, P.path, F1.pathid, F1.fileid, ");
					_sb.append("F1.lstat, J.jobid FROM job J, file F1, path P ");
					_sb.append("WHERE J.clientid = ");
					_sb.append(clientId);
					_sb.append(" AND J.jobid = F1.jobid AND ");
					_sb.append("F1.pathid = P.pathid AND (P.path ~ '^");
					_sb.append(directoryEscaped);
					_sb.append("[^/]*/$')) F2 ");
					//_sb.append("WHERE F2.filenameid = FN.filenameid ORDER BY F2.path, F2.jobid DESC ");
					_sb.append("WHERE F2.filenameid = FN.filenameid");
					_sb.append(") AS F3 ");
					_sb.append("WHERE P.pathid = F3.pathid");
				}
				
				_sb.append(") AS X ");
				_sb.append("ORDER BY X.name asc, X.jobid DESC");
				if(limit > 0) {
					_sb.append(" LIMIT ");
					_sb.append(limit);
				}
				
				if(offset > 0) {
					_sb.append(" OFFSET ");
					_sb.append(offset);
				}
				
				//logger.debug("Get client files of {}. Query: {}", directory, _sb.toString());
				List<Map<String, Object>> result = this.connection.transactionQuery(_sb.toString());
				this.connection.transactionClose();
				
				if(result == null || result.isEmpty()) {
					return new ArrayList<Map<String,String>>(files.values());
				}
				
				for(Map<String, Object> row : result) {
					Map<String, String> file = new HashMap<String, String>();
					if("true".equals(String.valueOf(row.get("directory")))) {
						String _td = null;
						if(row.get("name") == null) {
							_td = "";
						} else {
							_td = String.valueOf(row.get("name"));
						}
						if(_td.length() > 1 && (_td.endsWith("/") || _td.endsWith("\\")) ) {
							_td = _td.substring(0, _td.length() - 1);
						}
						file.put("path", _td);
						if(_td.contains("/") && _td.length()>1) {
							file.put("name", _td.substring(_td.lastIndexOf("/") + 1));
						} else if(_td.contains("\\") && _td.length()>1) {
							file.put("name", _td.substring(_td.lastIndexOf("\\") + 1));
						} else {
							file.put("name", _td);
						}
					} else {
						file.put("name", String.valueOf(row.get("name")));
						if(directory.endsWith("/") || directory.endsWith("\\")) {
							directory = directory.substring(0, directory.length() - 1);
						}
						file.put("path", directory + "/" + String.valueOf(row.get("name")));
					}
					file.put("directory", String.valueOf(row.get("directory")));
					if(row.get("lstat") != null) {
						String _lstat = String.valueOf(row.get("lstat"));
						if(!_lstat.isEmpty()) {
							String[] _data = _lstat.split(" ");
							long _size = BaculaBase64.decode(_data[7]);
			                file.put("size", getStringSize(_size));
			                file.put("last-modified", new Timestamp(BaculaBase64.decode(_data[11])*1000).toString());
						} else {
							file.put("size", "unknown");
			                file.put("last-modified", "unknown");
						}
					} else {
						file.put("size", "unknown");
		                file.put("last-modified", "unknown");
					}
					files.put(file.get("path"), file);
				}
			} catch(Exception _ex) {
				logger.error("Error getting client files of {}. Ex: {}", directory, _ex.getMessage());
				System.out.println("FileManager::getClient: error: " + _ex.getMessage());
			}
		}
		return new ArrayList<Map<String,String>>(files.values());
	}
	
	public static String getStringSize(long _size) throws Exception {
		StringBuilder _sb = new StringBuilder();
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		if (_size >= 1073741824L*1024L) {
			_sb.append(_df.format((double)_size/(1073741824L*1024L)));
			_sb.append(" TB");
		} else if(_size >= 1073741824) {
			_sb.append(_df.format((double)_size/1073741824));
			_sb.append(" GB");
		} else if(_size >= 1048576) {
			_sb.append(_df.format((double)_size/1048576));
			_sb.append(" MB");
		} else if(_size >= 1024) {
			_sb.append(_df.format((double)_size/1024));
			_sb.append(" KB");
		} else {
			_sb.append(_df.format(_size));
			_sb.append(" B");
		}
		return _sb.toString();
	}
	
	public List<Map<String, String>> getJobsForVmwareClient(String clientName, List<String> vms, int limit, int page, String sortRow, String sortOrder) throws Exception {
		List<Map<String, String>> jobs = new ArrayList<Map<String, String>>();
		ClientManager _cm = new ClientManager(this._c);
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		int clientId = _cm.getClientId("airback-fd");
		StringBuilder _sb = new StringBuilder();
		
		boolean init = true;
		for (String vm : vms) {
			if (!init)
				_sb.append(" INTERSECT ");
			else
				init = false;
			_sb.append("SELECT J.jobid as id, J.name as jobname, J.level, J.starttime, J.endtime, type, S.jobstatuslong as status, ");
			_sb.append("jobfiles as files, jobbytes as bytes ");
			_sb.append("FROM client C, job J, file F, filename FN, path P, status S WHERE C.clientid = ");
			_sb.append(clientId);
			_sb.append(" AND C.clientid = J.clientid ");
			_sb.append("AND J.jobstatus = S.jobstatus ");
			_sb.append("AND J.jobid = F.jobid ");
			_sb.append("AND P.pathid = F.pathid AND FN.filenameid = F.filenameid ");
			_sb.append("AND P.path LIKE '");
			_sb.append(vm);
			_sb.append("' ");
			_sb.append("GROUP BY id, J.name, J.level, starttime, endtime, type, status, files, bytes");
		}
			
		_sb.append(" ORDER BY "+sortRow+" "+sortOrder+"");
		
		if(limit > 0) {
			_sb.append(" LIMIT ");
			_sb.append(limit);
		}
		
		if(page > 0) {
			int offset = page*limit-limit;
			_sb.append(" OFFSET ");
			_sb.append(offset);
		}
		this.connection.transactionInit();
		this.connection.transactionQuery("SET enable_seqscan = off");
		List<Map<String, Object>> result = this.connection.transactionQuery(_sb.toString());
		this.connection.transactionClose();
		
		if(result == null || result.isEmpty()) {
			return jobs;
		}
		
		for(Map<String, Object> row : result) {
			Map<String, String> job = new HashMap<String, String>();
			job.put("id", String.valueOf(row.get("id")));
			job.put("name", String.valueOf(row.get("jobname")));
			job.put("start", dateFormat.format(row.get("starttime")));
			job.put("end", dateFormat.format(row.get("endtime")));
			job.put("type", String.valueOf(row.get("type")));
			job.put("level", String.valueOf(row.get("level")));
			job.put("status", String.valueOf(row.get("status")));
			job.put("files", String.valueOf(row.get("files")));
			long size = 0;
			_sb = new StringBuilder();
			if(row.get("bytes") instanceof Double) {
				size = ((Double) row.get("bytes")).longValue();
			} else if(row.get("bytes") instanceof Long) {
				size = (Long) row.get("bytes");
			} else if(row.get("bytes") instanceof Integer) {
				size = ((Integer) row.get("bytes")).longValue();
			}
			job.put("size", getStringSize(size));
			jobs.add(job);
		}
		
		return jobs;
	}

	
	public List<Map<String, String>> getJobsForFile(int clientId, String path, int limit, int offset) throws Exception {
		String directory = "";
		List<Map<String, String>> jobs = new ArrayList<Map<String, String>>();
		ClientManager _cm = new ClientManager(this._c);
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		if(path.contains("/")) {
			directory = path.substring(0, path.lastIndexOf("/"));
			path = path.substring(path.lastIndexOf("/") + 1);
			if(!directory.endsWith("/")) {
				directory += "/";
			}
		} else if(path.contains("\\")) {
			directory = path.substring(0, path.lastIndexOf("\\"));
			path = path.substring(path.lastIndexOf("\\") + 1);
			if(!directory.endsWith("\\")) {
				directory += "\\";
			}
		} else {
			return jobs;
		}
		
		if (clientId == 0)
			clientId = _cm.getClientId("airback-fd");
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT J.jobid as id, J.name as jobname, J.level, J.starttime, J.endtime, type, S.jobstatuslong as status, ");
		_sb.append("jobfiles as files, jobbytes as bytes ");
		_sb.append("FROM client C, job J, file F, filename FN, path P, status S WHERE C.clientid = ");
		_sb.append(clientId);
		_sb.append(" AND C.clientid = J.clientid ");
		_sb.append("AND J.jobstatus = S.jobstatus ");
		_sb.append("AND J.jobid = F.jobid ");
		_sb.append("AND P.pathid = F.pathid AND FN.filenameid = F.filenameid ");
		if(!directory.isEmpty()) {
			if(!path.isEmpty()) {
				_sb.append("AND FN.name LIKE '");
				_sb.append(path);
				_sb.append("' ");
			} else {
				if(directory.contains("/") && !directory.endsWith("/")) {
					directory += "/";
				} else if(path.contains("\\") && !directory.endsWith("\\")) {
					directory += "\\";
				}
			}
			_sb.append("AND P.path LIKE '");
			_sb.append(directory);
			_sb.append("' ");
		}
		_sb.append("GROUP BY id, J.name, J.level, starttime, endtime, type, status, files, bytes ORDER BY J.starttime DESC");
		
		if(limit > 0) {
			_sb.append(" LIMIT ");
			_sb.append(limit);
		}
		
		if(offset > 0) {
			_sb.append(" OFFSET ");
			_sb.append(offset);
		}
		
		this.connection.transactionInit();
		this.connection.transactionQuery("SET enable_seqscan = off");
		List<Map<String, Object>> result = this.connection.transactionQuery(_sb.toString());
		this.connection.transactionClose();
		
		if(result == null || result.isEmpty()) {
			return jobs;
		}
		
		for(Map<String, Object> row : result) {
			Map<String, String> job = new HashMap<String, String>();
			job.put("id", String.valueOf(row.get("id")));
			job.put("name", String.valueOf(row.get("jobname")));
			job.put("start", dateFormat.format(row.get("starttime")));
			job.put("end", dateFormat.format(row.get("endtime")));
			job.put("type", String.valueOf(row.get("type")));
			job.put("level", String.valueOf(row.get("level")));
			job.put("status", String.valueOf(row.get("status")));
			job.put("files", String.valueOf(row.get("files")));
			long size = 0;
			_sb = new StringBuilder();
			if(row.get("bytes") instanceof Double) {
				size = ((Double) row.get("bytes")).longValue();
			} else if(row.get("bytes") instanceof Long) {
				size = (Long) row.get("bytes");
			} else if(row.get("bytes") instanceof Integer) {
				size = ((Integer) row.get("bytes")).longValue();
			}
			job.put("size", getStringSize(size));
			jobs.add(job);
		}
		
		return jobs;
	}
	
	
	public String getMostRecentFileset(Integer clientId, String path, Calendar date) throws Exception {
		try {
			String directory = "";
			String file = "";
			if(path.contains("/")) {
				directory = path.substring(0, path.lastIndexOf("/"));
				file = path.substring(path.lastIndexOf("/") + 1);
				if(!directory.endsWith("/")) {
					directory += "/";
				}
			} else if(path.contains("\\")) {
				directory = path.substring(0, path.lastIndexOf("\\"));
				file = path.substring(path.lastIndexOf("\\") + 1);
				if(!directory.endsWith("\\")) {
					directory += "\\";
				}
			}
			
			if(!path.endsWith("/")) {
				path += "/";
			}
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("SELECT J.jobid,Fil.fileset as fileset, J.endtime ");
			_sb.append("FROM client C, job J, file F, filename FN, path P, status S, fileset Fil WHERE C.clientid = ");
			_sb.append(clientId);
			_sb.append(" AND C.clientid = J.clientid ");
			_sb.append("AND J.jobstatus = S.jobstatus ");
			_sb.append("AND J.jobid = F.jobid ");
			_sb.append("AND J.filesetid = Fil.filesetid ");
			_sb.append("AND P.pathid = F.pathid AND FN.filenameid = F.filenameid ");
			_sb.append("AND  J.jobstatus = 'T' AND J.starttime < '");
			_sb.append(date.get(Calendar.YEAR));
			_sb.append("-");
			_sb.append(twoCharFormat(date.get(Calendar.MONTH) + 1));
			_sb.append("-");
			_sb.append(twoCharFormat(date.get(Calendar.DAY_OF_MONTH)));
			_sb.append(" ");
			_sb.append(twoCharFormat(date.get(Calendar.HOUR_OF_DAY)));
			_sb.append(":");
			_sb.append(twoCharFormat(date.get(Calendar.MINUTE)));
			_sb.append(":00' ");
			_sb.append(" AND P.path like '"+path+"'");
			_sb.append(" GROUP BY J.jobid,Fil.fileset,J.endtime ");
			if(!directory.isEmpty() && !file.isEmpty()) {
				_sb.append("UNION ");
				_sb.append("SELECT J.jobid,Fil.fileset as fileset, J.endtime ");
				_sb.append("FROM client C, job J, file F, filename FN, path P, status S, fileset Fil WHERE C.clientid = ");
				_sb.append(clientId);
				_sb.append(" AND C.clientid = J.clientid ");
				_sb.append("AND J.jobstatus = S.jobstatus ");
				_sb.append("AND J.jobid = F.jobid ");
				_sb.append("AND J.filesetid = Fil.filesetid ");
				_sb.append("AND P.pathid = F.pathid AND FN.filenameid = F.filenameid ");
				_sb.append("AND  J.jobstatus = 'T' AND J.starttime < '");
				_sb.append(date.get(Calendar.YEAR));
				_sb.append("-");
				_sb.append(twoCharFormat(date.get(Calendar.MONTH) + 1));
				_sb.append("-");
				_sb.append(twoCharFormat(date.get(Calendar.DAY_OF_MONTH)));
				_sb.append(" ");
				_sb.append(twoCharFormat(date.get(Calendar.HOUR_OF_DAY)));
				_sb.append(":");
				_sb.append(twoCharFormat(date.get(Calendar.MINUTE)));
				_sb.append(":00' ");
				_sb.append(" AND P.path like '"+directory+"' AND FN.name <> ''");
				_sb.append(" GROUP BY J.jobid,Fil.fileset,J.endtime ");
			}
			_sb.append(" ORDER BY endtime DESC LIMIT 1");
			
			this.connection.transactionInit();
			this.connection.transactionQuery("SET enable_seqscan = off");
			logger.debug("Query para encontrar fileset reciente: {}", _sb.toString());
			List<Map<String, Object>> result = this.connection.transactionQuery(_sb.toString());
			this.connection.transactionClose();
			
			if(result == null || result.isEmpty()) {
				return null;
			}
			
			for(Map<String, Object> row : result) {
				return String.valueOf(row.get("fileset"));
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error obtaining most recent fileset for client {} date {} path {}. Ex: {} ", new Object[]{clientId, date, path, ex.getMessage()});
			throw ex;
		}
	}
		
	public static String twoCharFormat(Integer value) {
		StringBuilder _sb = new StringBuilder();
		if(value < 10) {
			_sb.append("0");
		}
		_sb.append(value);
		return _sb.toString();
	}
	
	public List<Map<String, String>> searchClientFiles(int clientId, String clientName, String match, int limit, int offset) throws Exception {
		if(match == null || match.isEmpty()) {
			return new ArrayList<Map<String,String>>();
		}
		
		ClientManager _cm = new ClientManager(this._c);
		
		match = match.replaceAll("[*()&$@'?\"%|]", "");
		
		TreeMap<String, Map<String, String>> files = new TreeMap<String, Map<String, String>>(Collator.getInstance(new Locale("es")));
		
		String vmware = ClientManager.getVMWareIncludeQuery(clientName, "J");
		if (clientId == 0)
			clientId = _cm.getClientId("airback-fd");
		else
			vmware = ClientManager.getVMWareExcludeQuery("J");
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT X.name as name, X.path, X.lstat, X.directory ");
		_sb.append("FROM ( ");
		_sb.append("SELECT DISTINCT ON (F3.name) F3.name, F3.lstat, 0 = 1 AS directory, F3.jobid, F3.path ");
		_sb.append("FROM path P, ( ");
		_sb.append("SELECT F2.pathid, F2.path, F2.fileid, F2.lstat, FN.name, F2.jobid ");
		_sb.append("FROM  filename FN, (select  F1.filenameid, P.path, F1.pathid, F1.fileid, ");
		_sb.append("F1.lstat, J.jobid from job J, file F1, path P ");
		_sb.append("WHERE J.clientid = ");
        _sb.append(clientId);
        _sb.append(vmware);
        _sb.append(" AND J.jobid = F1.jobid AND F1.pathid = P.pathid ) F2 ");
		_sb.append("WHERE F2.filenameid = FN.filenameid  AND FN.name ilike '%");
        _sb.append(match);
        //_sb.append("%' ORDER BY F2.path, F2.jobid DESC");
        _sb.append("%'");
		_sb.append(") AS F3 ");
		_sb.append("WHERE P.pathid = F3.pathid AND name <> '' ");
		_sb.append("UNION ");
		_sb.append("SELECT DISTINCT ON (F3.name) F3.name, F3.lstat, 1 = 1 as directory, F3.jobid, F3.path ");
		_sb.append("FROM path P, ( ");
		_sb.append("SELECT F2.pathid, F2.path as name, F2.path as path, F2.fileid, F2.lstat, F2.jobid ");
		_sb.append("FROM  filename FN, (select  F1.filenameid, P.path, F1.pathid, F1.fileid, F1.lstat, ");
		_sb.append("J.jobid from job J, file F1, path P ");
		_sb.append("WHERE J.clientid = ");
        _sb.append(clientId);
        _sb.append(" AND J.jobid = F1.jobid AND F1.pathid = P.pathid and P.path ilike '%");
        _sb.append(match);
        _sb.append("%') F2 ");
		//_sb.append("WHERE F2.filenameid = FN.filenameid order by F2.path, F2.jobid desc ");
        _sb.append("WHERE F2.filenameid = FN.filenameid");
		_sb.append(") AS F3 ");
		_sb.append("WHERE P.pathid = F3.pathid) AS X ");
		_sb.append("ORDER BY X.directory desc, X.name asc, X.jobid DESC");
        
		if(limit > 0) {
			_sb.append(" LIMIT ");
			_sb.append(limit);
		}
		
		if(offset > 0) {
			_sb.append(" OFFSET ");
			_sb.append(offset);
		}
		
		this.connection.transactionInit();
		this.connection.transactionQuery("SET enable_seqscan = off");
		List<Map<String, Object>> result = this.connection.transactionQuery(_sb.toString());
		this.connection.transactionClose();
		
		if(result == null || result.isEmpty()) {
			return new ArrayList<Map<String,String>>();
		}
		
		for(Map<String, Object> row : result) {
			Map<String, String> file = new HashMap<String, String>();
			String _name = String.valueOf(row.get("name"));
			if(_name.endsWith("/") || _name.endsWith("\\")) {
				_name = _name.substring(0, _name.length() - 1);
			}
			if(_name.contains("/")) {
				_name = _name.substring(_name.lastIndexOf("/") + 1);
			} else if(_name.contains("\\")) {
				_name = _name.substring(_name.lastIndexOf("\\") + 1);
			}
			file.put("directory", String.valueOf(row.get("directory")));
			file.put("name", _name);
			if("true".equals(String.valueOf(row.get("directory")))) {
				file.put("path", String.valueOf(row.get("path")));
			} else {
				file.put("path", String.valueOf(row.get("path")) + _name);
			}
			if(row.get("lstat") != null) {
				String _lstat = String.valueOf(row.get("lstat"));
				if(!_lstat.isEmpty()) {
					String[] _data = _lstat.split(" ");
					long _size = BaculaBase64.decode(_data[7]);
	                file.put("size", getStringSize(_size));
	                file.put("last-modified", new Timestamp(BaculaBase64.decode(_data[11])*1000).toString());
				} else {
					file.put("size", "unknown");
	                file.put("last-modified", "unknown");
				}
			} else {
				file.put("size", "unknown");
                file.put("last-modified", "unknown");
			}
			files.put(file.get("path"), file);
		}
		
		return new ArrayList<Map<String,String>>(files.values());
	}
	
	public String getFileSetNameForFile(File _f) throws Exception {
		for(Map<String, String> fileset : FileSetManager.getAllFileSets()) {
			if(fileset.containsKey("include")) {
				if(fileset.get("include").contains(_f.getAbsolutePath())) {
					return fileset.get("name");
				}
			}
		}
		return "";
	}
	
	public static String escapePostgreChars(String input) {
		String escaped = escapeSpecial(input, '+');
		escaped = escapeSpecial(escaped, '%');
		escaped = escapeSpecial(escaped, '.');
		escaped = escapeSpecial(escaped, '$');
		escaped = escapeSpecial(escaped, '(');
		escaped = escapeSpecial(escaped, ')');
		return escaped;
	}
	public static String escapeSpecial(String input, char x) {
		if (input != null && !input.isEmpty()) {
			StringBuffer _sb = new StringBuffer();
			for (char c : input.toCharArray()) {
				if (c == x) {
					_sb.append('\\');
					_sb.append('\\');
				}
				_sb.append(c);
			}
			input = _sb.toString();
		}
		return input;
	}
}
