package com.whitebearsolutions.imagine.wbsairback.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.db.DBException;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.mongodb.CommandManager;
import com.whitebearsolutions.imagine.wbsairback.util.jQgridParameters;
import com.whitebearsolutions.imagine.wbsairback.util.jQgridParameters.Rules;
import com.whitebearsolutions.imagine.wbsairback.virtual.HypervisorManager;
import com.whitebearsolutions.util.Configuration;

public class JobManager {
	public static final int TYPE_BACKUP = 1;
	public static final int TYPE_COPY = 2;
	public static final int TYPE_MIGRATE = 3;
	
	public static boolean existsjob(String jobName) {
		File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
		if(_f.exists()) {
			return true;
		}
		 return false;
	}
	private static String getFormattedSize(double size) {
		StringBuilder _sb = new StringBuilder();
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
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
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> getJobProcessScripts(String jobName, String clientName) throws Exception{
		List<Map<String, Object>> _result=new ArrayList<Map<String, Object>>();
		Map<String,Map<String, Object>> _scripts = new TreeMap<String,Map<String, Object>>();
		if(jobName == null || jobName.isEmpty()) {
			return new ArrayList<Map<String, Object>>();
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts");
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		File[] _list = _dir.listFiles();
		Map<String, Object> _script =null;
		Map<String, Integer> _ordersMap =new HashMap<String, Integer>();
		int order=0;
		for(File _f : _list) {
			if(_f.getName().endsWith(".conf") && (_f.getName().startsWith(jobName+"_"))) {
				try {
					if(_f.getName().startsWith(jobName + "_") && !_f.getName().contains(".vars")) {
						String _command =  BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "Command");
						String _name = _f.getName().substring(jobName.length() + 1);
						_name = _name.substring(0, _name.lastIndexOf("_"));
						
						if (_scripts.get(_name)==null){
							_script = new HashMap<String, Object>();
						}else{
							_script = _scripts.get(_name);
						}
						_script.put("name", _name);
						_script.put("job", jobName);
						if(_command.contains(";")) {
							_command = _command.replace(";", ";\n");
						}
						if(_command.contains("&&")) {
							_command = _command.replace("&&", "\n");
						}
						String _orden=_f.getName().substring(_f.getName().lastIndexOf("_")+1, _f.getName().length()-5);						
						if (_script.get("scripts")==null){
							try{
								order=Integer.valueOf(_orden);
								_ordersMap.put(_name, order);
							}catch(Exception ex){
								_ordersMap.put(_name, 0);
							}
							_script.put("scripts", new ArrayList<Map<String, String>>());
						}else{
							try{
								order=Integer.valueOf(_orden);
								_ordersMap.put(_name, order);								
							}catch(Exception ex){
								order=_ordersMap.get(_name);							
								order++;
							}
							_ordersMap.put(_name, order);
						}
						if (_command.contains("/usr/bin/expect -f ")){
							if (clientName.equals("airback-fd")) {
								File _fscript=new File(_command.replace("/usr/bin/expect -f  ", ""));
								if (_fscript.exists()){
									_command=readFile(_fscript.getAbsolutePath());
								}
							}
						}
						
						if (!clientName.equals("airback-fd")) {
							for (String shell : ScriptProcessManager.supportedShells.keySet()) {
								if (_command.contains(shell+" -c '")) {
									_command = _command.substring(_command.indexOf(shell+" -c '")+(shell+" -c '").length(), _command.length()-1);
								}
							}
						}
						
						if(_command.contains("\\\"")) {
							_command = _command.replace("\\\"", "\"");
						}
						
						if (_command.startsWith("\"") && _command.endsWith("\"")) {
							_command = _command.substring(1, _command.length()-1);
						}
								
						
						Map<String, String> _scriptItem=new HashMap<String, String>();
						_scriptItem.put("order", String.valueOf(order));
						_scriptItem.put("content", _command);
						((ArrayList<Map<String, String>>)_script.get("scripts")).add(_scriptItem);	
						
						if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "RunsWhen") != null && "Before".equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "RunsWhen"))) {
							_script.put("before", "true");
							_script.put("type", ScriptProcessManager.BEFORE_EXECUTION);
						} else {
							_script.put("before", "false");
							_script.put("type", ScriptProcessManager.AFTER_EXECUTION);
						}
						if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "RunsOnSuccess") != null && "yes".equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "RunsOnSuccess"))) {
							_script.put("success", "true");
						} else {
							_script.put("success", "false");
						}
						if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "RunsOnFailure") != null && "yes".equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "RunsOnFailure"))) {
							_script.put("fail", "true");
						} else {
							_script.put("fail", "false");
						}
						if(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "AbortJobOnError") != null && "no".equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "RunScript", "AbortJobOnError"))) {
							_script.put("abort", "false");
						} else {
							_script.put("abort", "true");
						}
						Map<String, String> vars = getScriptVars(_f.getName().substring(0, _f.getName().indexOf(".conf")));
						if (vars != null && !vars.isEmpty())
							_script.put("variables", vars);
						_scripts.put(_name,_script);
					}
				} catch(Exception _ex) {}
			}
		}
		for (Map<String, Object> _name: getJobScripts(jobName,false)){
			_result.add(_name);
		}
		for (String _name: _scripts.keySet()){
			_result.add(_scripts.get(_name));
		}
		

		return _result;
	}
	
	public static Map<String, Object> getJobScript(String jobName, String jobScriptName) throws Exception{
		Map<String, Object> _script = new HashMap<String, Object>();
		if(jobName == null || jobName.isEmpty()) {
			return _script;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts");
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		//File[] _list = _dir.listFiles();
		
		List<String> fileScripts = BaculaConfiguration.getBaculaIncludes(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
		
		for(String nameFile : fileScripts) {
			String name = nameFile.substring(nameFile.lastIndexOf("/")+1);
			if (name.endsWith(".conf")) {
				name = name.substring(0, name.indexOf(".conf"));
				if(name.equals(jobScriptName)) {
					try {
						if(name.startsWith(jobName + "_")) {
							String _command =  BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "Command");
							String _name = name.substring(jobName.length() + 1);
							_name = _name.substring(0, _name.length() - 5);
							_script.put("name", _name);
							_script.put("job", jobName);
							if(_command.contains("\\\"")) {
								_command = _command.replace("\\\"", "");
							}
							if (_command.contains(";")) {
								_command = _command.replace(";", ";\n");
							}
							if (_command.contains("&&")) {
								_command = _command.replace("&&", "\n");
							}
							_script.put("command", _command);
							if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsWhen") != null && "Before".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsWhen"))) {
								_script.put("before", "true");
							} else {
								_script.put("before", "false");
							}
							if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnSuccess") != null && "yes".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnSuccess"))) {
								_script.put("success", "true");
							} else {
								_script.put("success", "false");
							}
							if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnFailure") != null && "yes".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnFailure"))) {
								_script.put("fail", "true");
							} else {
								_script.put("fail", "false");
							}
							if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "AbortJobOnError") != null && "no".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "AbortJobOnError"))) {
								_script.put("abort", "false");
							} else {
								_script.put("abort", "true");
							}
						}
					} catch(Exception _ex) {}
					return _script;
				}
			}
		}
		return _script;
	}
	
	
	public static List<Map<String, Object>> getJobScripts(String jobName, boolean all) throws Exception{
		List<Map<String, Object>> _scripts = new ArrayList<Map<String, Object>>();
		if(jobName == null || jobName.isEmpty()) {
			return _scripts;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts");
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		//File[] _list = _dir.listFiles();
		
		List<String> fileScripts = BaculaConfiguration.getBaculaIncludes(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
		
		for(String nameFile : fileScripts) {
			String name = nameFile.substring(nameFile.lastIndexOf("/")+1);
			if(name.endsWith(".conf") && (all || (!all &&(name.indexOf("_")==name.lastIndexOf("_"))))) {
				try {
					if(name.startsWith(jobName + "_")) {
						String _command =  BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "Command");
						String _name = name.substring(jobName.length() + 1);
						_name = _name.substring(0, _name.length() - 5);
						Map<String, Object> _script = new HashMap<String, Object>();
						_script.put("name", _name);
						_script.put("job", jobName);
						if(_command.contains("\\\"")) {
							_command = _command.replace("\\\"", "");
						}
						if (_command.contains(";")) {
							_command = _command.replace(";", ";\n");
						}
						if (_command.contains("&&")) {
							_command = _command.replace("&&", "\n");
						}
						_script.put("command", _command);
						if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsWhen") != null && "Before".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsWhen"))) {
							_script.put("before", "true");
						} else {
							_script.put("before", "false");
						}
						if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnSuccess") != null && "yes".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnSuccess"))) {
							_script.put("success", "true");
						} else {
							_script.put("success", "false");
						}
						if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnFailure") != null && "yes".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "RunsOnFailure"))) {
							_script.put("fail", "true");
						} else {
							_script.put("fail", "false");
						}
						if(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "AbortJobOnError") != null && "no".equals(BaculaConfiguration.getBaculaParameter(nameFile, "RunScript", "AbortJobOnError"))) {
							_script.put("abort", "false");
						} else {
							_script.put("abort", "true");
						}
						_scripts.add(_script);
					}
				} catch(Exception _ex) {}
			}
		}
		return _scripts;
	}
	
	public static List<String> getJobsForFileset(String filesetName) {
		List<String> _jobs = new ArrayList<String>();
		if(filesetName == null || filesetName.isEmpty()) {
			return _jobs;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
		File[] _list = _dir.listFiles();
		
		for(File _f :_list) {
			if(_f.getName().endsWith(".conf")) {
				try {
					if(filesetName.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _f.getName().replaceAll(".conf[^ ]*", ""), "FileSet"))) {
						_jobs.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					}
				} catch(Exception _ex) {}
			}
		}
		return _jobs;
	}
	
	public static List<String> getJobsForPool(String poolName) {
		List<String> _jobs = new ArrayList<String>();
		if(poolName == null || poolName.isEmpty()) {
			return _jobs;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
		File[] _list = _dir.listFiles();
		
		for(File _f :_list) {
			if(_f.getName().endsWith(".conf")) {
				try {
					if(poolName.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _f.getName().replaceAll(".conf[^ ]*", ""), "Pool"))) {
						_jobs.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					} else if(poolName.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _f.getName().replaceAll(".conf[^ ]*", ""), "Full Backup Pool"))) {
						_jobs.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					} else if(poolName.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _f.getName().replaceAll(".conf[^ ]*", ""), "Incremental Backup Pool"))) {
						_jobs.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					} else if(poolName.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _f.getName().replaceAll(".conf[^ ]*", ""), "Differential Backup Pool"))) {
						_jobs.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					} 
				} catch(Exception _ex) {}
			}
		}
		return _jobs;
	}
	
	public static List<String> getJobsForSchedule(String scheduleName) {
		List<String> _jobs = new ArrayList<String>();
		if(scheduleName == null || scheduleName.isEmpty()) {
			return _jobs;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
		File[] _list = _dir.listFiles();
		
		for(File _f :_list) {
			if(_f.getName().endsWith(".conf")) {
				try {
					if(scheduleName.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _f.getName().replaceAll(".conf[^ ]*", ""), "Schedule"))) {
						_jobs.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					}
				} catch(Exception _ex) {}
			}
		}
		return _jobs;
	}
	
	public static List<String> getJobsForScriptProcess(String scriptProcessName) {
		List<String> _jobs = new ArrayList<String>();
		if(scriptProcessName == null || scriptProcessName.isEmpty()) {
			return _jobs;
		}
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts");
		File[] _list = _dir.listFiles();
		
		for(File _f :_list) {
			if(_f.getName().endsWith(".conf") && _f.getName().contains(scriptProcessName)) {
				String jobName = _f.getName().substring(0, _f.getName().indexOf("_"+scriptProcessName));
				if (!_jobs.contains(jobName))
					_jobs.add(jobName);
			}
		}
		return _jobs;
	}

	public static List<String> getJobsForStorage(String poolName) {
		List<String> _jobs = new ArrayList<String>();
		if(poolName == null || poolName.isEmpty()) {
			return _jobs;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
		File[] _list = _dir.listFiles();
		
		for(File _f :_list) {
			if(_f.getName().endsWith(".conf")) {
				try {
					if(poolName.equals(BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _f.getName().replaceAll(".conf[^ ]*", ""), "Storage"))) {
						_jobs.add(_f.getName().replaceAll(".conf[^ ]*", ""));
					}
				} catch(Exception _ex) {}
			}
		}
		return _jobs;
	}
	
	public static boolean hasRunningJobs() {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo status dir  | /usr/bin/bconsole | ");
		_sb.append("/bin/grep \"No Jobs running\" | wc -l");
		try {
			if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
				return false;
			}
			String _output = CommandManager.launchNoBDCommand(_sb.toString());
			if(_output != null && _output.trim().equals("0")) {
				return true;
			}
		} catch(Exception _ex) {
			return false;
		}
		return false;
	}
	
	public static boolean hasRunningJobs(String storage) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo status ");
		if(storage != null && !storage.isEmpty()) {
			_sb.append("storage=\"");
			_sb.append(storage);
			_sb.append("\"");
		} else {
			_sb.append("dir");
		}
		_sb.append(" | /usr/bin/bconsole | /bin/grep \"No Jobs running\" | wc -l");
		try {
			if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
				return false;
			}
			String _output = CommandManager.launchNoBDCommand(_sb.toString());
			if(_output != null && _output.trim().equals("0")) {
				return true;
			}
		} catch(Exception _ex) {
			return false;
		}
		return false;
	}
	
	private Configuration _c;
	
	private DBConnection _db;
	
	private ClientManager _cm;
	
	private DateFormat df_in;
	
	private DateFormat df_out;
	
	private final static Logger logger = LoggerFactory.getLogger(JobManager.class);
	
	public static Map<String, String> getScriptVars(String nameScript) throws Exception {
		Map<String, String> vars = new HashMap<String, String>();
		File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + nameScript + ".vars.conf");
		if (_f.exists()) {
			Configuration confVars = new Configuration(_f);
			for (String nameVar : confVars.getPropertyNames()) {
				vars.put(nameVar, confVars.getProperty(nameVar));
			}
		}
		return vars;
	}
	
	private static String readFile( String file ) throws IOException {
	    BufferedReader reader = new BufferedReader( new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }
	    reader.close();
	    return stringBuilder.toString();
	}
	
	public JobManager(Configuration conf) throws Exception {
		this._c = conf;
		this._db = new DBConnectionManager(this._c).getConnection();
		this._cm = new ClientManager(this._c);
	}
	
	public Map<String, String> buildJobFromBDResult(Map<String, Object> result, Map<String, Map<String, String>> running_jobs, java.text.SimpleDateFormat dateFormat) {		
		Map<String, String> job = new HashMap<String, String>();
		StringBuilder _sb;
		
		job.put("id", String.valueOf(result.get("jobid")));
		job.put("name", String.valueOf(result.get("name")));
		job.put("type", String.valueOf(result.get("type")));
		job.put("level", String.valueOf(result.get("level")));
		job.put("clientid", String.valueOf(result.get("clientid")));
		if(result.get("starttime") != null) {
			job.put("start", dateFormat.format(result.get("starttime")));
		} else {
			job.put("start", "");
		}
		if(result.get("endtime") != null) {
			job.put("end", dateFormat.format(result.get("endtime")));
		} else {
			job.put("end", "");
		}
		job.put("status", String.valueOf(result.get("jobstatuslong")));
		if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
				String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
			job.put("run", "true");
			try {
				//job.put("spool", Command.systemCommand("ls -Falh /rdata/working/ | grep airback-sd.attr." + job.get("name") + " |  awk '{print $5}'").replace("\n", "").replace("\r", ""));
			} catch(Exception _ex) {}
		} else {
			job.put("run", "false");
		}
		job.put("spool", "");
		if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
				String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
			job.put("return", "RUN");
			if("0".equals(String.valueOf(result.get("joberrors")))) {
				job.put("alert", "good");
			} else {
				job.put("alert", "warning");
			}
		} else if(String.valueOf(result.get("jobstatuslong")).equals("Completed successfully")) {
			job.put("return", "OK");
			if("0".equals(String.valueOf(result.get("joberrors")))) {
				job.put("alert", "good");
			} else {
				job.put("alert", "warning");
			}
		} else if(String.valueOf(result.get("jobstatuslong")).equals("Incomplete Job")) {
			job.put("return", "INCOMPLETE");
			job.put("alert", "warning");
		} else if(String.valueOf(result.get("jobstatuslong")).equals("Canceled by user")) {
			job.put("return", "CANCEL");
			job.put("alert", "error");
		} else {
			job.put("return", "ERROR");
			job.put("alert", "error");
		}
		if(running_jobs.get(job.get("id")) != null) {
			_sb = new StringBuilder();
			Map<String, String> _job_parameters = running_jobs.get(job.get("id"));
			_sb.append(_job_parameters.get("bytes"));
			_sb.append("&nbsp;(");
			_sb.append(_job_parameters.get("speed"));
			_sb.append(")");
			job.put("size", _sb.toString());
			job.put("errors", _job_parameters.get("errors"));
			job.put("files", _job_parameters.get("files"));
		} else {
			double size = 0;
			_sb = new StringBuilder();
			job.put("files", String.valueOf(result.get("jobfiles")));
			job.put("errors", String.valueOf(result.get("joberrors")));
			if(result.get("jobbytes") instanceof Double) {
				size = ((Double) result.get("jobbytes")).longValue();
			} else if(result.get("jobbytes") instanceof Long) {
				size = (Long) result.get("jobbytes");
			} else if(result.get("jobbytes") instanceof Integer) {
				size = ((Integer) result.get("jobbytes")).longValue();
			}
			job.put("size", getFormattedSize(size));
		}
		return job;
	}
	
	public long countArchivedClientJobs(int clientId) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT count(*) AS total FROM status as s,job as j WHERE j.type <> 'D' AND j.clientid = ");
		_sb.append(clientId);
		_sb.append(" AND  j.jobstatus = s.jobstatus");
		
		List<Map<String, Object>> _result = this._db.query(_sb.toString());
		if(_result == null || _result.isEmpty()) {
			return 0;
		}
		
		Map<String, Object> _data = _result.get(0); 
		if(_data.get("total") instanceof Double) {
			return ((Double) _data.get("total")).longValue();
		} else if(_data.get("total") instanceof Long) {
			return (Long) _data.get("total");
		} else if(_data.get("total") instanceof Integer) {
			return ((Integer) _data.get("total")).longValue();
		}
		
		return 0;
	}
	
	public Integer countTotalArchivedClientJobs(int clientId, String clientName ) throws Exception {
		String vmware = "";
		if (clientId == 0) {
			clientId = _cm.getClientId("airback-fd");
			vmware = ClientManager.getVMWareIncludeQuery(clientName, "j");
			if (vmware == null || vmware.isEmpty())
				return 0;
		} else
			vmware = ClientManager.getVMWareExcludeQuery("j");
		
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT j.jobid,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,j.jobbytes,j.joberrors,s.jobstatuslong FROM status as s,job as j WHERE j.type <> 'D' AND j.clientid = ");
		_sb.append(clientId);
		_sb.append(vmware);
		_sb.append(" AND  j.jobstatus = s.jobstatus order BY j.starttime DESC");
		
		return this._db.query(_sb.toString()).size();
	}
	
	public void deleteAllJobScritps(String jobName) throws Exception {		
		if(jobName == null || jobName.isEmpty()) {
			return;
		}
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts");
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		File[] _list = _dir.listFiles();
		
		for(File _f : _list) {
			if(_f.getName().endsWith(".conf") || _f.getName().endsWith(".ssh")  || _f.getName().endsWith(".sh")) {
				try {
					if(_f.getName().startsWith(jobName + "_")) {
						if (BaculaConfiguration.existBaculaInclude(WBSAirbackConfiguration.getDirectoryJobs()+"/"+jobName+".conf",WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/"+_f.getName())) {
							File f = new File(_f.getAbsolutePath());
							f.delete();
						} else if (_f.getName().contains(".vars.")) {
							File f = new File(_f.getAbsolutePath());
							f.delete();
						}
					}
				} catch (Exception ex) {}
			}
		}
	}
	
	public void deleteJobScript(String jobName, String name) throws Exception {
		if(jobName == null || jobName.isEmpty() || name == null || name.isEmpty()) {
			return;
		}
		
		try {
			File f = new File("/scripts/" + jobName + "_" + name + ".conf");
			if (f.exists()) {
				f.delete();
			} else {
				f = new File("/scripts/" + name + ".conf");
				if (f.exists()) {
					f.delete();
				}
			}
			f = new File("/scripts/" + jobName + "_" + name + ".vars.conf");
			if (f.exists())
				f.delete();
			f = new File("/scripts/" + jobName + "_" + name + ".vars.conf");
			if (f.exists())
				f.delete();
		} catch (Exception ex) {}
	}
	
	public void disableProgrammedJob(String jobName) throws Exception {
		Map<String, String> _job = getProgrammedJob(jobName);
		boolean verifyPreviousJob = false, spoolData = false, accurate = false, rescheduleOnError = false;
		int type = TYPE_BACKUP, maxStartDelay = 0, maxRunTime = 0, maxWaitTime = 0, priority = 10, bandwith = 0, rescheduleInterval = 2, rescheduleTimes = 12;
		
		if(_job.get("enabled") != null && "no".equalsIgnoreCase(_job.get("enabled"))) {
			return;
		}
		
		if(_job.get("type") == null) {
			throw new Exception("invalid job type");
		} else if("Backup".equalsIgnoreCase(_job.get("type"))) {
			type = TYPE_BACKUP;
		} else if("Copy".equalsIgnoreCase(_job.get("type"))) {
			type = TYPE_COPY;
		} else if("Migrate".equalsIgnoreCase(_job.get("type"))) {
			type = TYPE_MIGRATE;
		} else {
			throw new Exception("invalid job type");
		}
		
		if(_job.get("verifyPreviousJob") != null && "yes".equalsIgnoreCase(_job.get("verifyPreviousJob"))) {
			verifyPreviousJob = true;
		}
		if(_job.get("spooldata") != null && "yes".equalsIgnoreCase(_job.get("spooldata"))) {
			spoolData = true;
		}
		if(_job.get("accurate") != null && "yes".equalsIgnoreCase(_job.get("accurate"))) {
			accurate = true;
		}
		if(_job.get("max-start-delay") != null) {
			try {
				maxStartDelay = Integer.parseInt(_job.get("max-start-delay"));
			} catch(NumberFormatException _ex) {}
		}
		if(_job.get("max-run-time") != null) {
			try {
				maxRunTime = Integer.parseInt(_job.get("max-run-time"));
			} catch(NumberFormatException _ex) {}
		}
		if(_job.get("max-wait-time") != null) {
			try {
				maxWaitTime = Integer.parseInt(_job.get("max-wait-time"));
			} catch(NumberFormatException _ex) {}
		}
		if(_job.get("reschedule-on-error") != null && "yes".equalsIgnoreCase(_job.get("reschedule-on-error")) )  {
			rescheduleOnError = true;
			if(_job.get("reschedule-interval") != null) {
				try {
					rescheduleInterval = Integer.parseInt(_job.get("reschedule-interval"));
				} catch(NumberFormatException _ex) {}
			}
			if(_job.get("reschedule-times") != null) {
				try {
					rescheduleTimes = Integer.parseInt(_job.get("reschedule-times"));
				} catch(NumberFormatException _ex) {}
			}
		}
		if(_job.get("priority") != null) {
			try {
				priority = Integer.parseInt(_job.get("priority"));
			} catch(NumberFormatException _ex) {}
		}
		if(_job.get("bandwith") != null) {
			try {
				bandwith = Integer.parseInt(_job.get("bandwith"));
			} catch(NumberFormatException _ex) {}
		}
		
		setJob(jobName, _job.get("client"), _job.get("level"), _job.get("schedule"),
				_job.get("fileset"), _job.get("storage"), _job.get("pool"), _job.get("pool-full"),
				_job.get("pool-incremental"), _job.get("pool-differential"), _job.get("hypervisorJob"),
				_job.get("nextJob"), verifyPreviousJob, maxStartDelay, maxRunTime, maxWaitTime,
				spoolData, false,
				priority, type, bandwith, accurate, rescheduleOnError, rescheduleInterval, rescheduleTimes);
	}
	
	public String formatStringDate(String value, DateFormat dfin, DateFormat dfout) throws Exception {
		Date date = dfin.parse(value);
		return dfout.format(date);
	}
	
	public List<String> getAllProgrammedJobs() {
		List<String> _job_names = new ArrayList<String>();
		try {
			File dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
			String[] _files = dir.list();			
			for(String jobfile : _files) {
				if(jobfile.contains(".conf")) {
					_job_names.add(jobfile.replaceAll(".conf[^ ]*", ""));
				}
			}
		} catch(Exception _ex) {
			System.out.println("Error::JobManager::getAllProgrammedJobs: " + _ex.getMessage());
		}
		return _job_names;
	}
	/**
	 * Obtiene el listado de Jobs. Si jobId = 0, obtiene todos, si no, obtiene uno.
	 * @param jobId
	 * @return
	 */
	public Map<String, Object> getArchivedJobs(int jobId, List<String> clients, final jQgridParameters params) throws Exception
	{

		final SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

		// Orden
		boolean manualPaging = false;
		String order = null;
		if (params.getSidx() != null) {
			if (params.getSidx().equals("size") || params.getSidx().equals("exp") || params.getSidx().equals("speed")) {
				order = "ORDER BY j.starttime DESC";
				manualPaging = true;
			} else
				order = "ORDER BY " + params.getSidx() +" "+params.getSord();
		} else {
			order = "ORDER BY j.starttime DESC";
		}
		
		// Start Index
		int offset = params.getPage() != null ? (params.getPage() -1) * params.getRows() : 0;
		
		// End Index
		String limit = params.getRows() != null && params.getRows() > 0 ? "LIMIT " + params.getRows() + " OFFSET " + offset : "";
		
		// Searchs
		String whereQuery = " ";
		if(params.getSearch())
		{
			for(Rules _filter : params.getFilters().getRules())
			{
				if(!_filter.getData().isEmpty())
				{
					if(!_filter.getField().equals("j.starttime") && !_filter.getField().equals("j.endtime"))
					{
						// Esto es para buscar por integer o string (case insensitive) ya que LOWER(int) dará fallo.
						try{
							Integer.parseInt(_filter.getData());
							whereQuery += params.getFilters().getGroupOp() + " " + _filter.getField() + " = " + _filter.getData() + "";
						}
						catch(Exception ex){
							whereQuery += params.getFilters().getGroupOp() + " LOWER(" + _filter.getField() + ") LIKE LOWER('%" + _filter.getData() + "%')";
						}
					
					}else{
						// Igual A (filtro normal del tooltip)
						// GE = Greater or Equal
						if(_filter.getOp().equals("ge"))
							whereQuery += params.getFilters().getGroupOp() + " " + _filter.getField() + " >= '" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(_filter.getData() + " 00:00:00") + "'";
						// LE = Less or Equal
						if(_filter.getOp().equals("le"))
							whereQuery += params.getFilters().getGroupOp() + " " + _filter.getField() + " <= '" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(_filter.getData() + " 23:59:59") + "'";
						/* 
						 * EQ = Equal
						 * Aquí necesitamos poner las dos query de arriba pero juntas
						 * para buscar una fecha entre las 00:00 y 23:59
						 */
						if(_filter.getOp().equals("eq"))
						{
							whereQuery += params.getFilters().getGroupOp() + " " + _filter.getField() + " >= '" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(_filter.getData() + " 00:00:00") + "'";
							whereQuery += params.getFilters().getGroupOp() + " " + _filter.getField() + " <= '" + new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse(_filter.getData() + " 23:59:59") + "'";
						}
						//this._db.setObject(0, dateSearch);
					}
				}
			}
		}
		// Listado de Jobs
		List<Map<String, String>> jobs = new ArrayList<Map<String,String>>();
		Map<String, Object> listadoJson = new HashMap<String, Object>();	
		PoolManager _pm = new PoolManager(_c);
		Map<String, Map<String, String>> vmwareJobs = JobManager.getProgrammedVmwareJobs();
		// Listado de jobs lanzados
		Map<String, Map<String, String>> running_jobs = getRunningStorageJobs();
		Map<String, Map<String, String>> canceled_jobs = getTerminatedJobs("system", true);
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DATE, -7);

		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT j.jobid,j.job,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,"
				+ "j.jobbytes,j.joberrors,c.name as cliente,c.jobretention,c.clientid,s.jobstatuslong");
		_sb.append(" FROM status as s,job as j");
		_sb.append(" JOIN client as c ON c.clientid = j.clientid");

		if(jobId == 0)
		{
			_sb.append(" WHERE j.type <> 'D' ");
			_sb.append(!clients.isEmpty() && !(clients == null) ? " AND c.clientid IN ("+clients.toString().replaceAll("[\\s\\[\\]]", "")+")" : "");
			_sb.append(" AND  j.jobstatus = s.jobstatus "+whereQuery+" "+order);
			if (!manualPaging)
				_sb.append(" "+limit);
		} else {
			_sb.append(" WHERE j.jobid = " + jobId);
		}
		logger.debug("Query jobs {}", _sb.toString());
		
		/*
		 * Contador de jobs para jQgrid
		 */
		StringBuilder _sbcount = new StringBuilder();
		_sbcount.append("SELECT COUNT(j.jobid) AS count");
		_sbcount.append(" FROM status s,job j");
		_sbcount.append(" JOIN client c ON c.clientid = j.clientid");
		_sbcount.append(" WHERE j.type <> 'D'");
		_sbcount.append(!clients.isEmpty() && !(clients == null) ? " AND c.clientid IN ("+clients.toString().replaceAll("[\\s\\[\\]]", "")+")" : "");
		_sbcount.append(" AND  j.jobstatus = s.jobstatus " + whereQuery);
		List<Map<String, Object>> _count = this._db.query(_sbcount.toString());
		int recordsCount = (int) Math.round((Double) _count.get(0).get("count"));
		String total = params.getRows() != null && params.getRows() > 0 ? String.valueOf((recordsCount + params.getRows() - 1) / params.getRows()) : "1";
	
		listadoJson.put("records", recordsCount);
		// Si hay 0, se obtiene 1 ya que esto sirve para "Página X de n"
		listadoJson.put("total", Integer.parseInt(total) > 0 ? Integer.parseInt(total) : 1);
		listadoJson.put("page", params.getPage() != null && params.getPage() > 0 ? params.getPage() : 1);
		
		Map<String, Map<String, Map<String, String>>> mapRunning = new HashMap<String, Map<String, Map<String, String>>>();
		Map<String, Map<String, Map<String, String>>> mapCanceled = new HashMap<String, Map<String, Map<String, String>>>();
		for(Map<String, Object> result : this._db.query(_sb.toString())) {
			Map<String, String> job = new HashMap<String, String>();			
			job.put("j.jobid", String.valueOf(result.get("jobid")));
			job.put("j.name", String.valueOf(result.get("name")));
			job.put("j.job_unique_name", String.valueOf(result.get("job")));
			job.put("j.type", String.valueOf(result.get("type")));
			job.put("j.level", String.valueOf(result.get("level")));
			job.put("c.name", String.valueOf(result.get("cliente")));
			job.put("j.starttime", result.get("starttime") == null ? "" : dateFormat.format(result.get("starttime")));
			job.put("j.endtime", result.get("endtime") == null ? "" : dateFormat.format(result.get("endtime")));

			job.put("clientId", String.valueOf(result.get("clientid")));
			job.put("realClientId", String.valueOf(result.get("clientid")));
			job.put("jobretention", String.valueOf(result.get("jobretention")));
			job.put("run", String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running") ? "true" : "false");
			job.put("spool", "");
			if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
				job.put("return", "RUN");
				job.put("alert", String.valueOf(result.get("joberrors")).equals("0") ? "good" : "warning");
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Completed successfully")) {
				job.put("return", "OK");
				if("0".equals(String.valueOf(result.get("joberrors")))) {
					job.put("alert", "good");
				} else {
					job.put("alert", "warning");
				}
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Incomplete Job")) {
				job.put("return", "INCOMPLETE");
				job.put("alert", "warning");
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Canceled by user")) {
				job.put("return", "CANCEL");
				job.put("alert", "error");
			} else {
				job.put("return", "ERROR");
				job.put("alert", "error");
			}
			
			
			Map<String, String> progJob = getProgrammedJob(job.get("j.name"));
			Map<String, String> _job_parameters = null;
			Map<String, String> storageParams = null;
			Map<String, Map<String, String>> running_self_storage_jobs = new HashMap<String,  Map<String, String>>();
			Map<String, Map<String, String>> canceled_self_storage_jobs = new HashMap<String,  Map<String, String>>();
			String storage = null;
			if (progJob != null) {
				storage = progJob.get("storage");
				if (storage != null && !mapRunning.containsKey(storage)) {
					storageParams = StorageManager.getStorageParameters(storage);
					if (storageParams.get("storage.external") != null && storageParams.get("storage.external").equals("yes")) {
						running_self_storage_jobs = getRunningStorageJobs(storage);
						canceled_self_storage_jobs = getTerminatedJobs(storage, true);
						mapRunning.put(storage, running_self_storage_jobs);
						mapCanceled.put(storage, canceled_self_storage_jobs);
					}
				} else {
					running_self_storage_jobs = mapRunning.get(storage);
					canceled_self_storage_jobs = mapCanceled.get(storage);
				}
					
			}
			if(running_jobs.get(job.get("j.jobid")) != null) {
				_job_parameters = running_jobs.get(job.get("j.jobid"));
			} else if (running_self_storage_jobs.get(job.get("j.jobid")) != null) { 
				_job_parameters = running_self_storage_jobs.get(job.get("j.jobid"));
			}
			if((canceled_jobs.get(job.get("j.jobid")) != null || canceled_self_storage_jobs.get(job.get("j.jobid")) != null) && (String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running"))) {
				job.put("canceled", "true");
			}

			if (running_jobs.get(job.get("id")) != null || running_self_storage_jobs.get(job.get("id")) != null) {
				job.put("size",_job_parameters.get("bytes-raw"));
				job.put("speed",_job_parameters.get("speed-raw"));
				job.put("errors", _job_parameters.get("errors"));
				job.put("files", _job_parameters.get("files"));
			} else {
				double size = 0;
				job.put("files", String.valueOf(result.get("jobfiles")));
				job.put("errors", String.valueOf(result.get("joberrors")));
				if(result.get("jobbytes") instanceof Double) {
					size = ((Double) result.get("jobbytes")).longValue();
				} else if(result.get("jobbytes") instanceof Long) {
					size = (Long) result.get("jobbytes");
				} else if(result.get("jobbytes") instanceof Integer) {
					size = ((Integer) result.get("jobbytes")).longValue();
				}
				Double speed;
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					long elapsedtime = (sdf.parse(dateFormat.format(result.get("endtime"))).getTime()
							- sdf.parse(dateFormat.format(result.get("starttime"))).getTime()) / 1000;
					speed = size > 0 && elapsedtime > 0 && this.getSingleJobLog((Integer)result.get("jobid"), true, false).isEmpty() ? size / elapsedtime : 0;
				} catch(Exception ex) {
					speed = 0D;
				}
				
				job.put("speed", String.valueOf(speed));
				job.put("size", String.valueOf(size));
				job.put("exp", getJobExpireDateWrapper(job, _pm));
			}
			// Aquí ponemos el color rojo en caso de error

			job.put("s.jobstatuslong", "<div class=\"grid_"+job.get("alert")+"\">"+String.valueOf(result.get("jobstatuslong"))+" ("+job.get("errors")+")</div>");
			if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(job.get("j.name"))) {
				job.put("c.name", vmwareJobs.get(job.get("j.name")).get("c.name"));
				job.put("clientId", "0");
			}
			jobs.add(job);
		}
		
		if (manualPaging) {
			if (params.getSidx().equals("size") || params.getSidx().equals("speed") || params.getSidx().equals("exp")) {
				Collections.sort(jobs, new Comparator<Map<String, String>>() {
				    @Override
				    public int compare(final Map<String, String> job1, final Map<String, String> job2) {
				        if (job1 != null) {
				        	if (job2 != null) {
				        		String v1 = job1.get(params.getSidx());
				        		String v2 = job2.get(params.getSidx());
				        		if (v1 != null && !v1.isEmpty()) {
				        			if (v2 != null && !v2.isEmpty()) {
				        				if (!params.getSidx().equals("exp")) {
				        					Double size1 = Double.parseDouble(v1);
				        					Double size2 = Double.parseDouble(v2);
				        					return Double.compare(size1, size2);
				        				} else {
				        					try {
				        						Date date1 = dateFormat.parse(v1);
				        						Date date2 = dateFormat.parse(v2);
				        						if (date1.getTime() > date2.getTime())
					        						return 1;
					        					else if (date2.getTime() > date1.getTime())
					        						return -1;
					        					else
					        						return 0;
				        					} catch (Exception ex) {
				        						logger.error("Error comparando fechas {} y {}. Ex: {}", new Object[]{v1, v2, ex.getMessage()});
				        						return 0;
				        					}
				        				}
				        			} else
				        				return 1;
				        		} else if (v2 != null) {
				        			return -1;
				        		} else
				        			return 0;
				        	} else {
				        		return 1;
				        	}
				        } else if (job2 != null) {
				        	return -1;
				        } else
				        	return 0;
				    }
				});
				if (params.getSord() != null && params.getSord().equalsIgnoreCase("DESC"))
					Collections.reverse(jobs);
				
				if (params.getPage() != null && params.getPage() > 0 && params.getRows() != null && params.getRows() > 0) {
					int last = offset+params.getRows();
					
					if (offset > jobs.size()) {
						listadoJson.put("rows", jobs);
						return listadoJson;
					}
					
					if (last > jobs.size())
						last = jobs.size();
						
					jobs = jobs.subList(offset, last);
				}
			}
		}

		listadoJson.put("rows", jobs);
		return listadoJson;
	}
	
	public Map<String, String> getCopyJobOfCoordinator(Integer jobid) throws Exception {
		Map<String, String> job = null;
		Integer originalJobId = null;
		List<Map<String, Object>> list = this.getSingleJobLog(jobid, false, true);
		if (list != null && !list.isEmpty()) {
			Map<String, Object> row = list.iterator().next();
			String text = (String) row.get("logtext");
			if (text != null && !text.isEmpty() && text.contains("JobId=")) {
				text = text.substring(text.indexOf("JobId=")+"JobId=".length());
				originalJobId = Integer.parseInt(text.substring(0, text.indexOf(" ")).trim());
			}
		}
		
		if (originalJobId != null) {
			StringBuilder _sb = new StringBuilder();			
			_sb.append("SELECT j.jobid,j.job,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,"
					+ "j.jobbytes,j.joberrors,c.name as cliente,c.jobretention,c.clientid,s.jobstatuslong");
			_sb.append(" FROM status as s,job as j");
			_sb.append(" JOIN client as c ON c.clientid = j.clientid");
			_sb.append(" WHERE j.type = 'C' AND  j.jobstatus = s.jobstatus AND priorjobid=");
			_sb.append(originalJobId);
			for(Map<String, Object> result : this._db.query(_sb.toString())) {
				job = new HashMap<String, String>();
				 job.put("id", String.valueOf(result.get("jobid")));
				 job.put("name", String.valueOf(result.get("name")));
				 job.put("type", String.valueOf(result.get("type")));
				 job.put("level", String.valueOf(result.get("level")));
				 job.put("start", String.valueOf(result.get("starttime")));
				 job.put("end", String.valueOf(result.get("endtime")));
				 job.put("files", String.valueOf(result.get("jobfiles")));
				 job.put("size", getFormattedSize(Double.parseDouble(String.valueOf(result.get("jobbytes")))));
				 job.put("joberrors", String.valueOf(result.get("joberrors")));
				 job.put("client", String.valueOf(result.get("cliente")));
				 job.put("clientId", String.valueOf(result.get("clientid")));
				 job.put("jobretention", String.valueOf(result.get("jobretention")));
				 job.put("status", String.valueOf(result.get("jobstatuslong")));
				 if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
							String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
						job.put("return", "RUN");
						job.put("alert", String.valueOf(result.get("joberrors")).equals("0") ? "good" : "warning");
					} else if(String.valueOf(result.get("jobstatuslong")).equals("Completed successfully")) {
						job.put("return", "OK");
						if("0".equals(String.valueOf(result.get("joberrors")))) {
							job.put("alert", "good");
						} else {
							job.put("alert", "warning");
						}
					} else if(String.valueOf(result.get("jobstatuslong")).equals("Incomplete Job")) {
						job.put("return", "INCOMPLETE");
						job.put("alert", "warning");
					} else if(String.valueOf(result.get("jobstatuslong")).equals("Canceled by user")) {
						job.put("return", "CANCEL");
						job.put("alert", "error");
					} else {
						job.put("return", "ERROR");
						job.put("alert", "error");
					}
				 return job;
			}
		}
		
		return job;
	}
	
	private String getJobExpireDateWrapper(Map<String, String> job, PoolManager pm) throws Exception {
		String date = "";
		try {
			SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			if (job != null && !job.isEmpty()) {
				Date jobClientExpireDate = getJobClientExpireDate(job.get("j.endtime"), job.get("jobretention"));
				Date jobPoolExpireDate = getJobVolumeExpireDate(Integer.parseInt(job.get("j.jobid")), job.get("j.endtime"), pm);
				if (jobClientExpireDate != null) {
					if (jobPoolExpireDate != null) {
						if (jobClientExpireDate.getTime() < jobPoolExpireDate.getTime())
							return dateFormat.format(jobClientExpireDate);
						else
							return dateFormat.format(jobPoolExpireDate);
					} else
						return dateFormat.format(jobClientExpireDate);
				} else if (jobPoolExpireDate != null) {
					return dateFormat.format(jobPoolExpireDate);
				} else {
					return date;
				}
			}
		} catch (Exception ex) {
			logger.error("Error getting expire date for job {}. Ex: {}", job, ex.getMessage());
		}
		return date;
	}
	
	public static Date getJobClientExpireDate(String jobEndTime, String jobRetention) throws Exception {
		Date expireDate = null;
		try {
			if (jobEndTime != null && !jobEndTime.isEmpty() && jobRetention != null && !jobRetention.isEmpty()) {
				SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				Calendar c = Calendar.getInstance();
				c.setTime(dateFormat.parse(jobEndTime)); 
				Double jobretention = Double.parseDouble(jobRetention);
				jobretention = jobretention / 60d / 60d / 24d;
				c.add(Calendar.DATE, jobretention.intValue());
				expireDate = c.getTime();
			}
		} catch (Exception ex) {
			logger.error("Error getting expire date for entime {}. Ex: {}", jobEndTime, ex.getMessage());
		}
		return expireDate;
	}
	
	public Date getJobVolumeExpireDate(int jobId, String jobEndTime, PoolManager pm) throws Exception {
		try {
			List<Map<String, String>> volumes = getVolumesForJob(jobId);
			for (Map<String, String> vol : volumes) {
				if (vol.get("status") != null && !vol.get("status").equalsIgnoreCase("Append")) {
					Map<String, String> pool = pm.getPool(vol.get("pool"));
					if (pool != null && pool.get("retention") != null) {
						SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						Calendar c = Calendar.getInstance();
						c.setTime(dateFormat.parse(jobEndTime)); 
						Integer poolretention = Integer.parseInt(pool.get("retention"));
						c.add(Calendar.DATE, poolretention);
						return c.getTime();
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error getting expire date for entime {}", jobEndTime);
		}
		return null;
	}
	
	public List<Map<String, Object>> getSingleJobLog(int _jobid, boolean _onlyvirtual, boolean _onlycopy)
	{
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT l.time, l.logtext");
		_sb.append(" FROM log l ");
		_sb.append(_jobid == 0 ? "WHERE " : "WHERE l.jobid = " + _jobid);
		_sb.append(_onlyvirtual || _onlycopy ? " AND length(logtext) < 120 AND " : ""); // Si es virtual o copia, limitamos los caracteres para agilizar la búsqueda
		_sb.append(_onlyvirtual ? "logtext like '%Virtual Backup%'" : ""); // Si es virtual, busca el patrón
		_sb.append( _onlycopy ? "logtext like '%Copying using JobId=%'" : ""); // Si es copia, busca el patrón
		_sb.append(" ORDER BY l.time ASC");
		_sb.append( _onlyvirtual || _onlycopy ? " LIMIT 1" : ""); // Limita 1 resultado
		List<Map<String, Object>> _result;
		try {
			_result = this._db.query(_sb.toString());
		} catch (DBException e) {
			_result = null;
			e.printStackTrace();
		}
		return _result;
		
	}
	
	public Map<String, Map<String, String>> getArchivedClientJobs(int clientId, String clientName, int limit, int offset) throws Exception {
		Map<String, Map<String, String>> jobs = new TreeMap<String, Map<String,String>>();
		Map<String, Map<String, String>> running_jobs = getRunningStorageJobs();
		    			
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		String vmware = "";
		if (clientId == 0) {
			clientId = _cm.getClientId("airback-fd");
			vmware = ClientManager.getVMWareIncludeQuery(clientName, "j");
			if (vmware == null || vmware.isEmpty())
				return jobs;
		} else
			vmware = ClientManager.getVMWareExcludeQuery("j");
		
		StringBuilder _sb = new StringBuilder();
		
		if (clientId < 0) {
			_sb.append("SELECT j.jobid,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,j.jobbytes,j.joberrors,s.jobstatuslong FROM status as s,job as j WHERE j.type <> 'D' AND j.jobstatus = s.jobstatus AND j.starttime=(SELECT p.starttime FROM (SELECT x.jobid,x.name,x.starttime FROM job as x WHERE x.type <> 'D') as p where p.name=j.name order by p.starttime DESC limit 1) order BY j.starttime DESC");
		} else {
			_sb.append("SELECT j.jobid,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,j.jobbytes,j.joberrors,s.jobstatuslong FROM status as s,job as j WHERE j.type <> 'D'");
			_sb.append(vmware);
			_sb.append("AND j.clientid = ");
			_sb.append(clientId);
			
			_sb.append(" AND  j.jobstatus = s.jobstatus order BY j.starttime DESC");
			if(limit > 0) {
				_sb.append(" LIMIT ");
				_sb.append(limit);
			}
			if(offset > 0) {
				_sb.append(" OFFSET ");
				_sb.append(offset);
			}
		}
		
		//logger.debug("Query jobs: {}", _sb);
		for(Map<String, Object> result : this._db.query(_sb.toString())) {
			Map<String, String> job = new HashMap<String, String>();
			job.put("id", String.valueOf(result.get("jobid")));
			job.put("name", String.valueOf(result.get("name")));
			job.put("type", String.valueOf(result.get("type")));
			job.put("level", String.valueOf(result.get("level")));
			if(result.get("starttime") != null) {
				job.put("start", dateFormat.format(result.get("starttime")));
			} else {
				job.put("start", "");
			}
			if(result.get("endtime") != null) {
				job.put("end", dateFormat.format(result.get("endtime")));
			} else {
				job.put("end", "");
			}
			job.put("status", String.valueOf(result.get("jobstatuslong")));
			if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
				job.put("run", "true");
			} else {
				job.put("run", "false");
			}
			job.put("spool", "");
			if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
				job.put("return", "RUN");
				try {
					//job.put("spool", Command.systemCommand("ls -Falh /rdata/working/ | grep airback-sd.attr." + job.get("name") + " |  awk '{print $5}'").replace("\n", "").replace("\r", ""));
				} catch(Exception _ex) {}
				if("0".equals(String.valueOf(result.get("joberrors")))) {
					job.put("alert", "good");
				} else {
					job.put("alert", "warning");
				}
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Completed successfully")) {
				job.put("return", "OK");
				if("0".equals(String.valueOf(result.get("joberrors")))) {
					job.put("alert", "good");
				} else {
					job.put("alert", "warning");
				}
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Incomplete Job")) {
				job.put("return", "INCOMPLETE");
				job.put("alert", "warning");
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Canceled by user")) {
				job.put("return", "CANCEL");
				job.put("alert", "error");
			} else {
				job.put("return", "ERROR");
				job.put("alert", "error");
			}
			if(running_jobs.get(job.get("id")) != null) {
				_sb = new StringBuilder();
				Map<String, String> _job_parameters = running_jobs.get(job.get("id"));
				_sb.append(_job_parameters.get("bytes"));
				_sb.append("&nbsp;(");
				_sb.append(_job_parameters.get("speed"));
				_sb.append(")");
				job.put("size", _sb.toString());
				job.put("errors", _job_parameters.get("errors"));
				job.put("files", _job_parameters.get("files"));
			} else {
				double size = 0;
				_sb = new StringBuilder();
				job.put("files", String.valueOf(result.get("jobfiles")));
				job.put("errors", String.valueOf(result.get("joberrors")));
				if(result.get("jobbytes") instanceof Double) {
					size = ((Double) result.get("jobbytes")).longValue();
				} else if(result.get("jobbytes") instanceof Long) {
					size = (Long) result.get("jobbytes");
				} else if(result.get("jobbytes") instanceof Integer) {
					size = ((Integer) result.get("jobbytes")).longValue();
				}
				job.put("size", getFormattedSize(size));
			}
			jobs.put(job.get("name"),job);
		}
		return jobs;
	}
	
	public List<Map<String, String>> getArchivedClientJobsFlexGrid(Integer clientId, String clientName, Integer page, Integer rp, String sortname, String sortorder, String query, String qtype) throws Exception {
		List<Map<String, String>> jobs = new ArrayList<Map<String,String>>();
		Map<String, Map<String, String>> running_jobs = getRunningStorageJobs();
		Map<String, Map<String, String>> canceled_jobs = getTerminatedJobs("system", true);
		    			
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		String vmware = "";
		if (clientId == 0) {
			clientId = _cm.getClientId("airback-fd");
			vmware = ClientManager.getVMWareIncludeQuery(clientName, "j");
			if (vmware == null || vmware.isEmpty())
				return jobs;
		} else
			vmware = ClientManager.getVMWareExcludeQuery("j");
		
		
		
		String order = "order BY j.starttime DESC";
		if (sortname != null && sortorder != null) {
			if (sortname.equals("startdate"))
				sortname = "starttime";
			else if (sortname.equals("enddate"))
				sortname = "endtime";
			else if (sortname.equals("totalfiles"))
				sortname = "jobfiles";
			else if (sortname.equals("totalsize"))
				sortname = "jobbytes";
			else if (sortname.equals("id"))
				sortname = "jobid";
			order = "ORDER BY j."+sortname+" "+sortorder;
		}
		
		int offset = (page * rp) - rp;
		String limit = "LIMIT "+rp;
		if (offset > 0)
			limit+=" OFFSET "+offset;
		String whereQuery = "";
		if (query != null && !query.equals(""))	{
			if (qtype.equals("startdate"))
				qtype = "starttime";
			else if (qtype.equals("enddate"))
				qtype = "endtime";
			else if (qtype.equals("totalfiles"))
				qtype = "jobfiles";
			else if (qtype.equals("totalsize"))
				qtype = "jobbytes";
			
			if (!qtype.equals("starttime") && !qtype.equals("endtime"))
				whereQuery = " AND j."+qtype+" LIKE '%"+query+"%' ";
			else {
				whereQuery = " AND ? BETWEEN j.starttime AND j.endtime";
				Date dateSearch = dateFormat.parse(query);
				this._db.setObject(0, dateSearch);
			}
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT j.jobid,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,j.jobbytes,j.joberrors,s.jobstatuslong FROM status as s,job as j WHERE j.type <> 'D' ");
		_sb.append("AND j.clientid = ");
		_sb.append(clientId);
		_sb.append(vmware);
		_sb.append(" AND  j.jobstatus = s.jobstatus "+whereQuery+" "+order+" "+limit);
		logger.debug("Query jobs {}", _sb.toString());
		
		Map<String, Map<String, Map<String, String>>> mapRunning = new HashMap<String, Map<String, Map<String, String>>>();
		Map<String, Map<String, Map<String, String>>> mapCanceled = new HashMap<String, Map<String, Map<String, String>>>();
		for(Map<String, Object> result : this._db.query(_sb.toString())) {
			
			Map<String, String> job = new HashMap<String, String>();
			job.put("id", String.valueOf(result.get("jobid")));
			job.put("name", String.valueOf(result.get("name")));
			job.put("type", String.valueOf(result.get("type")));
			job.put("level", String.valueOf(result.get("level")));
			
			Map<String, String> progJob = getProgrammedJob(job.get("name"));
			Map<String, String> _job_parameters = null;
			Map<String, String> storageParams = null;
			Map<String, Map<String, String>> running_self_storage_jobs = new HashMap<String,  Map<String, String>>();
			Map<String, Map<String, String>> canceled_self_storage_jobs = new HashMap<String,  Map<String, String>>();
			String storage = null;
			if (progJob != null) {
				storage = progJob.get("storage");
				if (storage != null && !mapRunning.containsKey(storage)) {
					storageParams = StorageManager.getStorageParameters(storage);
					if (storageParams.get("storage.external") != null && storageParams.get("storage.external").equals("yes")) {
						running_self_storage_jobs = getRunningStorageJobs(storage);
						canceled_self_storage_jobs = getTerminatedJobs(storage, true);
						mapRunning.put(storage, running_self_storage_jobs);
						mapCanceled.put(storage, canceled_self_storage_jobs);
					}
				} else {
					running_self_storage_jobs = mapRunning.get(storage);
					canceled_self_storage_jobs = mapCanceled.get(storage);
				}
					
			}
			
			if(result.get("starttime") != null) {
				job.put("start", dateFormat.format(result.get("starttime")));
			} else {
				job.put("start", "");
			}
			if(result.get("endtime") != null) {
				job.put("end", dateFormat.format(result.get("endtime")));
			} else {
				job.put("end", "");
			}
			job.put("status", String.valueOf(result.get("jobstatuslong")));
			if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
				job.put("run", "true");
				try {
					//job.put("spool", Command.systemCommand("ls -Falh /rdata/working/ | grep airback-sd.attr." + job.get("name") + " |  awk '{print $5}'").replace("\n", "").replace("\r", ""));
				} catch(Exception _ex) {}
			} else {
				job.put("run", "false");
			}
			job.put("spool", "");
			if(String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running")) {
				job.put("return", "RUN");
				if("0".equals(String.valueOf(result.get("joberrors")))) {
					job.put("alert", "good");
				} else {
					job.put("alert", "warning");
				}
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Completed successfully")) {
				job.put("return", "OK");
				if("0".equals(String.valueOf(result.get("joberrors")))) {
					job.put("alert", "good");
				} else {
					job.put("alert", "warning");
				}
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Incomplete Job")) {
				job.put("return", "INCOMPLETE");
				job.put("alert", "warning");
			} else if(String.valueOf(result.get("jobstatuslong")).equals("Canceled by user")) {
				job.put("return", "CANCEL");
				job.put("alert", "error");
			} else {
				job.put("return", "ERROR");
				job.put("alert", "error");
			}
			if(running_jobs.get(job.get("id")) != null) {
				_job_parameters = running_jobs.get(job.get("id"));
			} else if (running_self_storage_jobs.get(job.get("id")) != null) { 
				_job_parameters = running_self_storage_jobs.get(job.get("id"));
			}
			
			if((canceled_jobs.get(job.get("id")) != null || canceled_self_storage_jobs.get(job.get("id")) != null) && (String.valueOf(result.get("jobstatuslong")).equals("Running") ||
					String.valueOf(result.get("jobstatuslong")).equals("Created, not yet running"))) {
				job.put("canceled", "true");
			}
			
			if (running_jobs.get(job.get("id")) != null || running_self_storage_jobs.get(job.get("id")) != null) {
				_sb = new StringBuilder();
				_sb.append(_job_parameters.get("bytes"));
				_sb.append("&nbsp;(");
				_sb.append(_job_parameters.get("speed"));
				_sb.append(")");
				job.put("size", _sb.toString());
				job.put("errors", _job_parameters.get("errors"));
				job.put("files", _job_parameters.get("files"));
			} else {
				double size = 0;
				_sb = new StringBuilder();
				job.put("files", String.valueOf(result.get("jobfiles")));
				job.put("errors", String.valueOf(result.get("joberrors")));
				if(result.get("jobbytes") instanceof Double) {
					size = ((Double) result.get("jobbytes")).longValue();
				} else if(result.get("jobbytes") instanceof Long) {
					size = (Long) result.get("jobbytes");
				} else if(result.get("jobbytes") instanceof Integer) {
					size = ((Integer) result.get("jobbytes")).longValue();
				}
				job.put("size", getFormattedSize(size));
			}
			jobs.add(job);
		}
		return jobs;
	}
	
	public Map<String, String> getClientForJob(int jobId) {
		Map<String, String> _client = new HashMap<String, String>();
		StringBuilder _sb = new StringBuilder();
		_client.put("id", "");
		_client.put("name", "");
		_sb.append("SELECT c.name AS name, j.clientid as id, j.filesetid as filesetid, j.name as jobname FROM job AS j, client AS c WHERE j.jobid = ");
		_sb.append(jobId);
		_sb.append(" AND j.clientid = c.clientid");
		
		try {
			List<Map<String, Object>> result = this._db.query(_sb.toString());
			if(result == null || result.isEmpty()) {
				return _client;
			}
			
			String jobname = String.valueOf(result.get(0).get("jobname"));
			String filesetId = String.valueOf(result.get(0).get("filesetid"));
			if (filesetId != "0") {
				_sb = new StringBuilder();
				_sb.append("SELECT c.name AS name, j.clientid as id, f.fileset as fileset, j.name as jobname FROM job AS j, client AS c, fileset as f WHERE j.jobid = ");
				_sb.append(jobId);
				_sb.append(" AND j.clientid = c.clientid AND f.filesetid = j.filesetid");
				List<Map<String, Object>> result2 = this._db.query(_sb.toString());
				if(result2 != null && !result2.isEmpty()) {
					String filesetName = String.valueOf(result.get(0).get("fileset"));
					
					Map<String, String> fileset = FileSetManager.getFileSet(filesetName);
					if (fileset.get("vmware") != null && fileset.get("vmware").equals("yes")) {
						_client.put("id", "0");
						_client.put("name", fileset.get("vmwareClient"));
						return _client;
					}
				}
			} else if (jobname.contains("Vmware")) {
				_client.put("id", "0");
				_client.put("name", "vmware-clients");
			}
			
			_client.put("id", String.valueOf(result.get(0).get("id")));
			_client.put("name", String.valueOf(result.get(0).get("name")));

			return _client;
		} catch(Exception _ex) {
			return _client;
		}
	}
	
	public String getExternalStorageAssociated(String jobName) throws Exception{
		List<Map<String, Object>> scripts = getJobScripts(jobName,true);
		if (scripts != null && scripts.size()>0) {
			for (Map<String, Object> script : scripts) {
				if (script.get("name") != null && ((String)script.get("name")).contains("_ex_storage_")) {
					return ((String)script.get("name")).substring(((String)script.get("name")).indexOf("_ex_storage_"));
				}
			}
		}
		return null;
	}
	
	public Map<String, String> getJob(int jobId) throws Exception {
		Map<String, String> job = new HashMap<String, String>();
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT j.jobid,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,j.jobbytes,j.joberrors,s.jobstatuslong,j.clientid FROM status as s,job as j WHERE j.type <> 'D' AND j.jobid = ");
		_sb.append(jobId);
		_sb.append(" AND  j.jobstatus = s.jobstatus");
		List<Map<String, Object>> result = this._db.query(_sb.toString());
		if (result != null && result.size() > 0)
		{
			Map<String, Map<String, String>> running_jobs = getRunningStorageJobs();
			java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			job = this.buildJobFromBDResult(result.get(0), running_jobs, dateFormat);
		}
		return job;
	}
	
	public List<Map<String, String>> getJobLog(String clientName, int jobId) throws Exception {
		String encoding = "UTF-8";
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		List<Map<String, String>> logs = new ArrayList<Map<String,String>>();
		try {
			Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + clientName + ".xml"));
			if(_c.getProperty("client.charset") != null) {
				encoding = _c.getProperty("client.charset");
			}
		} catch(Exception _ex) {}
		
		for(Map<String, Object> result : this._db.query("select l.time,l.logtext from log as l where l.jobid = " + jobId + " order by l.logid asc")) {
			Map<String, String> log = new HashMap<String, String>();
			
			try {
				String _text = new String(String.valueOf(result.get("logtext")).getBytes(encoding), "UTF-8");
				log.put("text", _text);
				if(_text.contains("ERR=")) {
					log.put("error", "true");
				} else {
					log.put("error", "false");
				}
				log.put("time", dateFormat.format(result.get("time")));
				logs.add(log);
			} catch(Exception _ex) {}
		}
		
		return logs;
	}
	
	public String getJobName(int jobId) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("select j.name from job as j where j.jobid = ");
		_sb.append(jobId);
		
		try {
			List<Map<String, Object>> result = this._db.query(_sb.toString());
			if(result == null || result.isEmpty()) {
				return "";
			}
			
			return String.valueOf(result.get(0).get("name"));
		} catch(DBException _ex) {
			return "";
		}
	}
	
	public List<String> getJobNames() throws Exception {
		List<String> jobs = new ArrayList<String>();
		for(Map<String, Object> result : this._db.query("SELECT distinct j.name FROM job as j ORDER BY j.name")) {
			jobs.add(String.valueOf(result.get("name")));
		}
		return jobs;
	}
	
	public Map<String, String> getLastArchivedJob(String jobName) throws Exception {
		Map<String, String> job = new HashMap<String, String>();
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT j.jobid,j.name,j.type,j.level,j.starttime,j.endtime,j.jobfiles,j.jobbytes,j.joberrors,s.jobstatuslong FROM status as s,job as j WHERE j.type <> 'D' AND j.name LIKE '");
		_sb.append(jobName);
		_sb.append("' AND  j.jobstatus = s.jobstatus order BY j.starttime DESC LIMIT 1");
		
		List<Map<String, Object>> _result = this._db.query(_sb.toString());
		
		if(!_result.isEmpty()) {
			Map<String, Map<String, String>> running_jobs = getRunningStorageJobs();
			job = buildJobFromBDResult(_result.get(0), running_jobs, dateFormat);
			return job;
		}
		return null;
	}
	
	public List<Map<String, String>> getProgrammedClientJobs(int clientId) throws Exception {
		TreeMap<String, Map<String, String>> jobs = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		String client = this._cm.getClientName(clientId);
		
		File dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
		String[] _files = dir.list();
		
		for(String jobfile : _files) {
			if(jobfile.contains(".conf")) {
				File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobfile);
				String _name = jobfile.substring(0, jobfile.length() - 5);
				String _client = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobfile, "Job", _name, "Client");
				if(_client != null && _client.equalsIgnoreCase(client)) {
					Map<String, String> job = new HashMap<String, String>();
					try {
						String fileset = BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "FileSet");
						if (fileset != null && !fileset.isEmpty()) {
							Map<String, String> ofileset = FileSetManager.getFileSet(fileset);
							if (ofileset.get("vmwareClient") == null) {
								job.put("name", _name);
								job.put("client", client);
								job.put("type", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Type"));
								job.put("level", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Level"));
								job.put("fileset", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "FileSet"));
								job.put("pool", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Pool"));
								job.put("schedule", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Schedule"));
								job.put("enabled", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Enabled"));
								jobs.put(job.get("name"), job);
							}
						}
					} catch(Exception _ex) {
						System.out.println("JobManager::getProgrammedJobs: error - " + _ex.toString());
					}
				}
			}
		}
		return new ArrayList<Map<String,String>>(jobs.values());
	}
	
	public Map<String, String> getProgrammedJob(String jobName) {
		File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
		if(_f.exists()) {
			Map<String, String> job = new HashMap<String, String>();
			try {
				job.put("name", jobName);
				job.put("type", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Type"));
				job.put("client", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Client"));
				job.put("level", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Level"));
				job.put("fileset", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "FileSet"));
				Map<String, String> fileset = FileSetManager.getFileSet(job.get("fileset"));
				if (fileset.get("vmwareClient") != null && !fileset.get("vmwareClient").isEmpty())
					job.put("client", fileset.get("vmwareClient"));
				job.put("pool", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Pool"));
				job.put("pool-full", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Full Backup Pool"));
				job.put("pool-incremental", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Incremental Backup Pool"));
				job.put("pool-differential", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Differential Backup Pool"));
				job.put("schedule", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Schedule"));
				job.put("enabled", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Enabled").trim().toLowerCase());
				job.put("storage", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Storage"));
				
				String _line = BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run After Job");
				if(_line != null && !_line.isEmpty()) {
					if(_line.contains(" -v ")) {
						job.put("verifyPreviousJob", "yes");
					} else {
						job.put("verifyPreviousJob", "no");
					}
					try {
						_line = _line.substring(_line.lastIndexOf(" ") + 1);
						if(_line.contains(" ")) {
							_line = _line.substring(0, _line.indexOf(" "));
						} else {
							_line = _line.substring(0, _line.length());
						}
						job.put("nextJob", _line.trim());
					} catch(Exception _ex) {
						System.out.println("Error::JobManager::getProgrammedJob: nextjob - " + _ex.getMessage());
						job.put("nextJob", "");
					}
				} else {
					job.put("nextJob", "");
					job.put("verifyPreviousJob", "no");
				}
				
				_line = BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run Before Job");
				if(_line != null && !_line.isEmpty()) {
					try {
						_line = _line.substring(_line.lastIndexOf(" ") + 1);
						if(_line.contains(" ")) {
							_line = _line.substring(0, _line.indexOf(" "));
						} else {
							_line = _line.substring(0, _line.length());
						}
						job.put("hypervisorJob", _line.trim());
					} catch(Exception _ex) {
						System.out.println("Error::JobManager::getProgrammedJob: hypervisorjob - " + _ex.getMessage());
						job.put("hypervisorJob", "");
					}
				} else {
					job.put("hypervisorJob", "");
				}
				
				_line = BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Maximum Bandwidth");
				if(_line != null && !_line.isEmpty()) {
					job.put("bandwith", _line.replace("Mb/s", ""));
				} else {
					job.put("bandwith", "");
				}
				
				job.put("max-start-delay", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Start Delay").split(" ")[0]);
				job.put("max-run-time", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Run Time").split(" ")[0]);
				job.put("max-wait-time", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Wait Time").split(" ")[0]);
				job.put("reschedule-on-error", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule On Error").trim().toLowerCase());
				job.put("reschedule-interval", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule Interval").split(" ")[0]);
				job.put("reschedule-times", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule Times").trim().toLowerCase());
				job.put("spooldata", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "SpoolData").trim().toLowerCase());
				job.put("accurate", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Accurate").trim().toLowerCase());
				job.put("priority", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Priority"));
				return job;
			} catch(Exception _ex) {
				System.out.println("Error::JobManager::getProgrammedJob: " + _ex.getMessage());
			}
		}
		return null;
	}
	
	public List<Map<String, String>> getProgrammedVmwareClientJobs(String name) throws Exception {
		TreeMap<String, Map<String, String>> jobs = new TreeMap<String, Map<String,String>>(Collator.getInstance(new Locale("es")));
		String client = "airback-fd";
		File dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
		String[] _files = dir.list();
		
		for(String jobfile : _files) {
			if(jobfile.contains(".conf")) {
				File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobfile);
				String _name = jobfile.substring(0, jobfile.length() - 5);
				String _client = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobfile, "Job", _name, "Client");
				if(_client != null && _client.equalsIgnoreCase(client)) {
					Map<String, String> job = new HashMap<String, String>();
					try {
						String fileset = BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "FileSet");
						if (fileset != null && !fileset.isEmpty()) {
							Map<String, String> ofileset = FileSetManager.getFileSet(fileset);
							if (ofileset.get("vmwareClient") != null && ofileset.get("vmwareClient").equals(name)) {
								job.put("name", _name);
								job.put("client", client);
								job.put("type", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Type"));
								job.put("level", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Level"));
								job.put("fileset", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "FileSet"));
								job.put("pool", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Pool"));
								job.put("schedule", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Schedule"));
								job.put("enabled", BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "Enabled"));
								jobs.put(job.get("name"), job);
							}
						}
					} catch(Exception _ex) {
						System.out.println("JobManager::getProgrammedJobs: error - " + _ex.toString());
					}
				}
			}
		}
		return new ArrayList<Map<String,String>>(jobs.values());
	}
	
	
	/**
	 * Obtiene el listado de jobs completo de tipo vmware
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Map<String, String>> getProgrammedVmwareJobs() throws Exception {
		Map<String, Map<String, String>> vmwareJobs = new HashMap<String, Map<String, String>>();
		try {
			String client = "airback-fd";
			File dir = new File(WBSAirbackConfiguration.getDirectoryJobs());
			String[] _files = dir.list();
			Map<String, Map<String, String>> vmwareFilesets = FileSetManager.getAllVmwareFileSetsTree();
			Map<String, String> vmwareJob = null;
			Map<String, String> vmwareFileset = null;
			
			for(String jobfile : _files) {
				if(jobfile.contains(".conf")) {
					File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobfile);
					String _name = jobfile.substring(0, jobfile.length() - 5);
					String _client = BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobfile, "Job", _name, "Client");
					if(_client != null && _client.equalsIgnoreCase(client)) {
						String fileset = BaculaConfiguration.getBaculaParameter(_f.getAbsolutePath(), "Job", _name, "FileSet");
						if (fileset != null && !fileset.isEmpty() && vmwareFilesets.keySet().contains(fileset)) {
							vmwareFileset = vmwareFilesets.get(fileset);
							vmwareJob = new HashMap<String, String>();
							vmwareJob.put("client", vmwareFileset.get("vmwareClient"));
							vmwareJobs.put(_name, vmwareJob);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error obteniendo la lista total de nombres de jobs de tipo vmware. Ex: {}", ex.getMessage());
		} 
		return vmwareJobs;
	}
	
	public Map<String, Map<String, String>> getRunningClientJobs(String client) throws Exception {
		if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
			throw new Exception("backup director service is not running");
		}
		
		Map<String, Map<String, String>> _runningJobs = new HashMap<String, Map<String, String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"status client=");
		_sb.append(client);
		_sb.append("\" | /opt/bacula/bin/bconsole");
		String _output = CommandManager.launchNoBDCommand(_sb.toString());
		if(_output != null) {
			BufferedReader _br = new BufferedReader(new StringReader(_output));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.startsWith("Running Jobs:")) {
						Map<String, String> _job = null;
						for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.trim().isEmpty()) {
								break;
							} else if(_line.startsWith("====")) {
								continue;
							}
							try {
								if(_line.startsWith("JobId")) {
									int _offset = 0;
									_job = new HashMap<String, String>();
									StringTokenizer _st = new StringTokenizer(_line);
									_st.nextToken();
									_job.put("id", _st.nextToken());
									_st.nextToken();
									String _value = _st.nextToken();
									for(String _tok : _value.split("[.]")) {
										_sb.append(_tok);
										if(_tok.matches("[0-9]{4}-(0[1-9]|1[012])-[0123][0-9]_[0-9]+")) {
											_offset = _value.indexOf(_tok);
											break;
										}
									}
									_job.put("name", _value.substring(0, _offset - 1));
									_value = _value.substring(_offset + 1, _value.lastIndexOf("_"));
									_value = _value.replace(".", ":");
									_value = _value.replace("_", " ");
									_job.put("date", _value);
									while(_st.hasMoreTokens()) {
										if(_sb.length() > 0) {
											_sb.append(" ");
										}
										_sb.append(_st.nextToken());
									}
									_job.put("status", _sb.toString());
								} else if(_line.contains("Files=") && _line.contains("Bytes=")) {
									StringTokenizer _st = new StringTokenizer(_line);
									while(_st.hasMoreTokens()) {
										String _value = _st.nextToken();
										if(_value.startsWith("Files=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_job.put("files", _value);
										} else if(_value.startsWith("Bytes=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_value = _value.replace(",", "");
											try {
												_job.put("bytes-raw", _value);
												_job.put("bytes", getFormattedSize(Double.parseDouble(_value)));
											} catch(NumberFormatException _ex) {
												_job.put("bytes-raw", _value);
												_job.put("bytes", _value);
											}
										} else if(_value.startsWith("Bytes/sec=")  || _value.startsWith("AveBytes/sec=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_value = _value.replace(",", "");
											try {
												_job.put("speed-raw", _value);
												_job.put("speed", getFormattedSize(Double.parseDouble(_value)).concat("/s"));
											} catch(NumberFormatException _ex) {
												_job.put("speed-raw", _value);
												_job.put("speed", _value);
											}
										} else if(_value.startsWith("Errors=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_job.put("errors", _value);
										}
									}
									if(_job.get("name") != null) {
										_runningJobs.put(_job.get("name"), _job);
									}
								}
							} catch(Exception _ex) {}
						}
					}
				}
			} finally {
				_br.close();
			}
		}
		return _runningJobs;
	}
	
	public Map<String, Map<String, String>> getRunningStorageJobs() throws Exception {
		return getRunningStorageJobs("SystemStorage");	
	}
	
	public Map<String, Map<String, String>> getRunningStorageJobs(String storage) throws Exception {
		if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
			throw new Exception("backup director service is not running");
		}
		
		Map<String, Map<String, String>> _runningJobs = new HashMap<String, Map<String, String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"status storage="+storage+"\" | /opt/bacula/bin/bconsole");
		String _output = CommandManager.launchNoBDCommand(_sb.toString());
		if(_output != null) {
			BufferedReader _br = new BufferedReader(new StringReader(_output));
			try {
				String tmp = null;
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.startsWith("Running Jobs:")) {
						Map<String, String> _job = null;
						for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.trim().isEmpty()) {
								break;
							} else if(_line.startsWith("====")) {
								continue;
							}
							try {	
								if(_line.startsWith("Writing:")) {
									_job = new HashMap<String, String>();
									StringTokenizer _st = new StringTokenizer(_line);
									_st.nextToken();
									tmp = _st.nextToken();
									if (tmp.equals("Virtual"))
										_job.put("level", _st.nextToken());
									else
										_job.put("level", tmp);
									_job.put("type", _st.nextToken());
									_st.nextToken();
									_job.put("name", _st.nextToken());
									String _value = _st.nextToken();
									if(_value.startsWith("JobId=")) {
										_job.put("id", _value.substring(_value.indexOf("=") + 1));
									}
									_value = _st.nextToken();
									if(_value.startsWith("Volume=")) {
										_value = _value.substring(_value.indexOf("=") + 1);
										if(_value.startsWith("\"")) {
											_value = _value.substring(1);
										}
										if(_value.endsWith("\"")) {
											_value = _value.substring(0, _value.length() - 1);
										}
										_job.put("volume", _value);
									}
								} else if(_line.contains("Files=") &&
										_line.contains("Bytes=")) {
									StringTokenizer _st = new StringTokenizer(_line);
									while(_st.hasMoreTokens()) {
										String _value = _st.nextToken();
										if(_value.startsWith("Files=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_job.put("files", _value);
										} else if(_value.startsWith("Bytes=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_value = _value.replace(",", "");
											try {
												_job.put("bytes-raw", _value);
												_job.put("bytes", getFormattedSize(Double.parseDouble(_value)));
											} catch(NumberFormatException _ex) {
												_job.put("bytes", _value);
												_job.put("bytes-raw", _value);
											}
										} else if(_value.startsWith("Bytes/sec=") || _value.startsWith("AveBytes/sec=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_value = _value.replace(",", "");
											try {
												_job.put("speed-raw", _value);
												_job.put("speed", getFormattedSize(Double.parseDouble(_value)).concat("/s"));
											} catch(NumberFormatException _ex) {
												_job.put("speed-raw", _value);
												_job.put("speed", _value);
											}
										} else if(_value.startsWith("Errors=")) {
											_value = _value.substring(_value.indexOf("=") + 1);
											_job.put("errors", _value);
										}
									}
									if(_job.get("id") != null) {
										if(_job.get("errors") == null) {
											_job.put("errors", "0");
										}
										_runningJobs.put(_job.get("id"), _job);
									}
								}
							} catch(Exception _ex) {
								System.out.println("JobManager::getRunningJobs:error - " + _ex.toString());
							}
						}
					}
				}
			} finally {
				_br.close();
			}
		}
		return _runningJobs;
	}
	
	public Map<String, List<Map<String, String>>> getSummaryJobs(List<String> categories) throws Exception {
		if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
			throw new Exception("backup director service is not running");
		}
		df_out = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		Map<String, List<Map<String, String>>> _summary_jobs = new HashMap<String, List<Map<String,String>>>();
		List<Map<String, String>> _runningJobs = new ArrayList<Map<String, String>>();
		List<Map<String, String>> _scheduleJobs = new ArrayList<Map<String, String>>();
		List<Map<String, String>> _terminatedJobs = new ArrayList<Map<String, String>>();
		Map<String, Map<String, String>> _running_jobs = getRunningStorageJobs();
		Map<String, Map<String, Map<String, String>>> mapRunning = new HashMap<String, Map<String, Map<String, String>>>();
		List<String> idsRunning = new ArrayList<String>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"status dir\" | /opt/bacula/bin/bconsole");
		String _output = CommandManager.launchNoBDCommand(_sb.toString());
		if(_output != null) {
			BufferedReader _br = new BufferedReader(new StringReader(_output));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.startsWith("Running Jobs:")) {
						df_in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.trim().isEmpty()) {
								break;
							} else if(_line.startsWith("====") || _line.startsWith(" JobId ")) {
								continue;
							}
							try {
								int _offset = 0;
								_sb = new StringBuilder();
								Map<String, String> _job = new HashMap<String, String>();
								StringTokenizer _st = new StringTokenizer(_line);
								_job.put("id", _st.nextToken());
								try {
									Map<String, String> _client = getClientForJob(Integer.parseInt(_job.get("id")));
									_job.put("clientid", _client.get("id"));
									_job.put("client", _client.get("name"));
									if (categories != null && !categories.isEmpty()) {
										if (!_cm.isClientOnCategories(_client.get("name"), categories))
											continue;
									}
								} catch(NumberFormatException _ex) {
									_job.put("clientid", "");
									_job.put("client", "");
								}
								String next = _st.nextToken();
								String _value = "";
								if (!next.contains("Restore")) {
									_job.put("level", next);
								 	_value = _st.nextToken();
								} else
									_value = next;
								
								for(String _tok : _value.split("[.]")) {
									_sb.append(_tok);
									if(_tok.matches("[0-9]{4}-(0[1-9]|1[012])-[0123][0-9]_[0-9]+")) {
										_offset = _value.indexOf(_tok);
										break;
									}
								}
								_job.put("name", _value.substring(0, _offset - 1));
								_value = _value.substring(_offset, _value.lastIndexOf("_"));
								_value = _value.replace(".", ":");
								_value = _value.replace("_", " ");
								
								_job.put("date", formatStringDate(_value,df_in, df_out ));
								_sb = new StringBuilder();
								while(_st.hasMoreTokens()) {
									if(_sb.length() > 0) {
										_sb.append(" ");
									}
									_sb.append(_st.nextToken());
								}
								_job.put("status", _sb.toString());
								
								Map<String, String> _job_parameters = null;
								Map<String, String> storageParams = null;
								Map<String, Map<String, String>> running_self_storage_jobs = new HashMap<String,  Map<String, String>>();
								Map<String, String> progJob = getProgrammedJob(_job.get("name"));
								String storage = null;
								if (progJob != null) {
									storage = progJob.get("storage");
									if (storage != null && !mapRunning.containsKey(storage)) {
										storageParams = StorageManager.getStorageParameters(storage);
										if (storageParams.get("storage.external") != null && storageParams.get("storage.external").equals("yes")) {
											running_self_storage_jobs = getRunningStorageJobs(storage);
											mapRunning.put(storage, running_self_storage_jobs);
										}
									} else {
										running_self_storage_jobs = mapRunning.get(storage);
									}
								}
								
								if(_running_jobs.get(_job.get("id")) != null) {
									_job_parameters = _running_jobs.get(_job.get("id"));
								} else if (running_self_storage_jobs.get(_job.get("id")) != null) { 
									_job_parameters = running_self_storage_jobs.get(_job.get("id"));
								}
								
								if (_running_jobs.get(_job.get("id")) != null || running_self_storage_jobs.get(_job.get("id")) != null) {
									_job.put("errors", _job_parameters.get("errors"));
									_job.put("files", _job_parameters.get("files"));
									_job.put("bytes", _job_parameters.get("bytes"));
									_job.put("bytes-raw", _job_parameters.get("bytes-raw"));
									_job.put("speed", _job_parameters.get("speed"));
									_job.put("speed-raw", _job_parameters.get("speed-raw"));
								}

								if(_job.get("errors") == null) {
									_job.put("errors", "0");
								}
								if(_job.get("files") == null) {
									_job.put("files", "0");
								}
								if(_job.get("bytes") == null) {
									_job.put("bytes", "0");
								}
								if(_job.get("bytes-raw") == null) {
									_job.put("bytes-raw", "0");
								}
								if(_job.get("speed") == null) {
									_job.put("speed", "");
								}
								if(_job.get("speed-raw") == null) {
									_job.put("speed-raw", "");
								}
								_runningJobs.add(0, _job);
								idsRunning.add(_job.get("id"));
							} catch(Exception _ex) {}
						}
					} else if(_line.startsWith("Scheduled Jobs:")) {
						df_in = new SimpleDateFormat("dd-MMM-yyHH:mm");
						for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.trim().isEmpty()) {
								break;
							} else if(_line.startsWith("====") || _line.startsWith("Level ")) {
								continue;
							}
							try {
								String _value;
								Map<String, String> _job = new HashMap<String, String>();
								StringTokenizer _st = new StringTokenizer(_line);
								if(_st.countTokens() < 7) {
									continue;
								}
								String level = _st.nextToken();
								String type = _st.nextToken();
								String priority = _st.nextToken();
								_value = _st.nextToken();
								_value = _value.concat(_st.nextToken());
								try {
									_job.put("date", formatStringDate(_value,df_in, df_out ));
								} catch(Exception _ex) {
									_job.put("date", _value);
								}
								_job.put("name", _st.nextToken());
								_job.putAll(getProgrammedJob(_job.get("name")));
								if (_job.get("client") != null && !_job.get("client").isEmpty()) {
									if (categories != null && !categories.isEmpty()) {
										if (!_cm.isClientOnCategories(_job.get("client"), categories))
											continue;
									}
								}
								_job.put("level", level);
								_job.put("type", type);
								_job.put("priority", priority);
								if(_st.hasMoreTokens()) {
									_job.put("volume", _st.nextToken());
								} else {
									_job.put("volume", "");
								}
								_scheduleJobs.add(0, _job);
							} catch(Exception _ex) {}
						}
					} else if(_line.startsWith("Terminated Jobs:")) {
						df_in = new SimpleDateFormat("dd-MMM-yyHH:mm");
						for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.trim().isEmpty()) {
								break;
							} else if(_line.startsWith("====") || _line.startsWith("Level ")) {
								continue;
							}
							try {
								double _size = 0;
								String _value;
								Map<String, String> _job = new HashMap<String, String>();
								StringTokenizer _st = new StringTokenizer(_line);
								if(_st.countTokens() < 7) {
									continue;
								}
								_job.put("id", _st.nextToken());
								try {
									Map<String, String> _client = getClientForJob(Integer.parseInt(_job.get("id")));
									_job.put("clientid", _client.get("id"));
									_job.put("client", _client.get("name"));
									if (categories != null && !categories.isEmpty()) {
										if (!_cm.isClientOnCategories(_client.get("name"), categories))
											continue;
									}
								} catch(NumberFormatException _ex) {
									_job.put("clientid", "");
									_job.put("client", "");
								}
								String value = _st.nextToken();
								try {
									 Double.parseDouble(value.replace(",", "."));
									 _job.put("files", value);
								} catch(Exception _ex) {
									_job.put("level", value);
									_job.put("files", _st.nextToken());
								}
								
								try {
									_size = Double.parseDouble(_st.nextToken());
								} catch(NumberFormatException _ex) {}
								
								_value = _st.nextToken().toLowerCase();
								if(_value.equals("k")) {
									_size = _size * 1024D;
									_value = _st.nextToken();
								} else if(_value.equals("m")) {
									_size = _size * 1048576D;
									_value = _st.nextToken();
								} else if(_value.equals("g")) {
									_size = _size * 1073741824D;
									_value = _st.nextToken();
								} else if(_value.equals("t")) {
									_size = _size * 1099511627776D;
									_value = _st.nextToken();
								} else if(_value.equals("p")) {
									_size = _size * 1125899906842620D;
									_value = _st.nextToken();
								}
								_job.put("size", getFormattedSize(_size));
								_job.put("status", _value.toUpperCase());
								_value = _st.nextToken();
								_value = _value.concat(_st.nextToken());
								try {
									_job.put("date", formatStringDate(_value,df_in, df_out ));
								} catch(Exception _ex) {
									_job.put("date", _value);
								}
								_job.put("name", _st.nextToken());
								if (this.getJobName(Integer.parseInt(_job.get("id"))).equals(""))
									_job.put("eliminated", "yes");
								else
									_job.put("eliminated", "no");
								_terminatedJobs.add(0, _job);
								
								if ((_job.get("status").equals("CANCEL") || _job.get("status").equals("INCOMPLETE")) && idsRunning.contains(_job.get("id"))) {
									for (Map<String, String> rjob : _runningJobs) {
										if (rjob.get("id").equals(_job.get("id")))
											rjob.put("canceled", "true");
									}
								}
							} catch(Exception _ex) {}
						}
					}
				}
			} finally {
				_br.close();
			}
			
			_summary_jobs.put("running", _runningJobs);
			_summary_jobs.put("scheduled", _scheduleJobs);
			_summary_jobs.put("terminated", _terminatedJobs);
		}
		return _summary_jobs;
	}
	
	public Map<String, Map<String, String>> getTerminatedJobs(String storage, boolean onlyCanceled) throws Exception {
		Map<String, Map<String, String>> _terminatedJobs = new HashMap<String, Map<String, String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"status storage="+storage+"\" | /opt/bacula/bin/bconsole");
		String _output = CommandManager.launchNoBDCommand(_sb.toString());
		if(_output != null) {
			BufferedReader _br = new BufferedReader(new StringReader(_output));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.startsWith("Terminated Jobs:")) {
						df_in = new SimpleDateFormat("dd-MMM-yyHH:mm");
						for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.trim().isEmpty()) {
								break;
							} else if(_line.startsWith("====") || _line.startsWith("Level ")) {
								continue;
							}
							try {
								double _size = 0;
								String _value;
								Map<String, String> _job = new HashMap<String, String>();
								StringTokenizer _st = new StringTokenizer(_line);
								if(_st.countTokens() < 8) {
									continue;
								}
								_job.put("id", _st.nextToken());
								try {
									Map<String, String> _client = getClientForJob(Integer.parseInt(_job.get("id")));
									_job.put("clientid", _client.get("id"));
									_job.put("client", _client.get("name"));
								} catch(NumberFormatException _ex) {
									_job.put("clientid", "");
									_job.put("client", "");
								}
								_job.put("level", _st.nextToken());
								_job.put("files", _st.nextToken());
								try {
									_size = Double.parseDouble(_st.nextToken());
								} catch(NumberFormatException _ex) {}
								
								_value = _st.nextToken().toLowerCase();
								if(_value.equals("k")) {
									_size = _size * 1024D;
									_value = _st.nextToken();
								} else if(_value.equals("m")) {
									_size = _size * 1048576D;
									_value = _st.nextToken();
								} else if(_value.equals("g")) {
									_size = _size * 1073741824D;
									_value = _st.nextToken();
								} else if(_value.equals("t")) {
									_size = _size * 1099511627776D;
									_value = _st.nextToken();
								} else if(_value.equals("p")) {
									_size = _size * 1125899906842620D;
									_value = _st.nextToken();
								}
								_job.put("size", getFormattedSize(_size));
								_job.put("status", _value.toUpperCase());
								_value = _st.nextToken();
								_value = _value.concat(_st.nextToken());
								try {
									_job.put("date", formatStringDate(_value,df_in, df_out ));
								} catch(Exception _ex) {
									_job.put("date", _value);
								}
								_job.put("name", _st.nextToken());
								if (this.getJobName(Integer.parseInt(_job.get("id"))).equals(""))
									_job.put("eliminated", "yes");
								else
									_job.put("eliminated", "no");
								if (onlyCanceled == false || (onlyCanceled == true && _job.get("status") != null && (_job.get("status").equals("CANCEL") || _job.get("status").equals("INCOMPLETE"))))
									_terminatedJobs.put(_job.get("id"), _job);
									
							} catch(Exception _ex) {}
						}
					}
				}
			} finally {
				_br.close();
			}
		}
		return _terminatedJobs;
	}
	
	public List<Map<String, String>> getVolumesForJob(int jobId) throws Exception {
		List<Map<String, String>> volumes = new ArrayList<Map<String,String>>();
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT DISTINCT m.volumename AS volume, p.name AS pool, m.volstatus as status");
		_sb.append(" FROM jobmedia AS jm, media AS m, pool AS p");
		_sb.append(" WHERE jm.mediaid = m.mediaid AND m.poolid = p.poolid");
		_sb.append(" AND jobid = ");
		_sb.append(jobId);
		
		for(Map<String, Object> result : this._db.query(_sb.toString())) {
			Map<String, String> volume = new HashMap<String, String>();
			volume.put("volume", String.valueOf(result.get("volume")));
			volume.put("pool", String.valueOf(result.get("pool")));
			volume.put("status", String.valueOf(result.get("status")));
			volumes.add(volume);
		}
		return volumes;
	}
	
	public void removeAllJobScripts(String jobName) throws Exception {
		deleteAllJobScritps(jobName);
		for(Map<String, Object> script : getJobScripts(jobName,true)) {
			String _command =  BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf", "RunScript", "Command");
			BaculaConfiguration.deleteBaculaIncludeParameter(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf", "Job", jobName, WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + jobName + "_" + script.get("name") + ".conf");
			if (_command.contains("bash")){
				File _fscript=new File(_command.replace("bash ", ""));
				if (_fscript.exists()){
					_fscript.delete();
				}
			}
		}
	}
	
	public void removeJob(String jobName) throws Exception {
		removeAllJobScripts(jobName);
		if (BaculaConfiguration.existBaculaInclude("/etc/bacula/bacula-dir.conf", WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf"))
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","jobs", jobName);
		PoolManager pm = new PoolManager(this._c);
		pm.markJobsAsCopiedFromRemovedJob(jobName);
		BackupOperator.reload();
	}
	
	public void removeJobScript(String jobName, String name) throws Exception {
		BaculaConfiguration.deleteBaculaIncludeParameter(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf", "Job", jobName, WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + jobName + "_" + name + ".conf");
		BackupOperator.reload();
	}
	
	public void setEmptyJobForScripts(String jobName, String clientName, String nextJob, String fileset, String schedule, String storage) throws Exception {
		try {
			if(jobName == null || !jobName.matches("[0-9a-zA-Z-._]+")) {
				throw new Exception("invalid job name");
			}
			
			File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
			if(!_f.exists()) {
				BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf","jobs", jobName);
			}
			
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Type", new String[]{ "Backup" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Client", new String[]{ clientName });
			
			if(nextJob != null && !nextJob.isEmpty()) {
				Map<String, String> _job = getProgrammedJob(jobName);
				if(_job != null) {
					StringBuilder _sb = new StringBuilder();
					_sb.append("\"/usr/sbin/bacula-job ");
					_sb.append("-v ");
					_sb.append(jobName);
					_sb.append(" ");
					_sb.append(nextJob);
					_sb.append("\"");
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run After Job", new String[] { _sb.toString() });
				}
			} else {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run After Job");
			}
			
			if (!BaculaConfiguration.existBaculaInclude("/etc/bacula/bacula-dir.conf", "@/etc/bacula/" + "filesets" + "/advancedEmptyFileSet"))
				FileSetManager.addExternalStorageFileSet("advancedEmptyFileSet", "", FileSetManager.COMPRESSION_NONE);
			else
				FileSetManager.updateExternalStorageFileSet("advancedEmptyFileSet", "", FileSetManager.COMPRESSION_NONE);
			
			if (fileset != null && !fileset.isEmpty())
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "FileSet", new String[]{ fileset });
			else 
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "FileSet", new String[]{ "advancedEmptyFileSet" });
			
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Level", new String[]{ "Full" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Pool", new String[]{ "DefaultPool" });
			
			if (storage != null)
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Storage", new String[]{ storage });
			else {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Storage", new String[]{ "SystemStorage" });
			}
				
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Messages", new String[]{ "Standard" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Start Delay", new String[]{ 10 + " hours" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Run Time", new String[]{ 72 + " hours" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Wait Time", new String[]{ 10 + " hours" });
			if (schedule != null && !schedule.isEmpty()) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Schedule", new String[]{ schedule });
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Enabled", new String[]{ "yes" });
			}
			
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Accurate", new String[]{ "no" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "SpoolData", new String[]{ "no" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Write Bootstrap", new String[]{ "\"/var/bacula/working/" + jobName + ".bsr\"" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Priority", new String[]{ "10" });
			
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","jobs", jobName);
			throw _ex;
		}
		BackupOperator.reload();
	}
	
	public void setJob(String jobName, String clientName, String level, String schedule, String fileset, String storage, String pool, 
			String poolFull, String poolIncremental, String poolDifferential, String hypervisorJob, String nextJob, boolean verifyPreviousJob,
			int maxStartDelay, int maxRunTime, int maxWaitTime,
			boolean spooldata, boolean enabled, int priority, int type, int bandwith, boolean accurate, boolean rescheduleOnError,int rescheduleInterval, int rescheduleTimes) throws Exception {
		if(jobName == null || !jobName.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid job name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
		if(!_f.exists()) {
			BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf","jobs", jobName);
		}
		
		try {
			switch(type) {
				case TYPE_COPY: {
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Type", new String[]{ "Copy" });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Level", new String[]{ "Full" });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "SpoolData", new String[]{ "no" });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Selection Type", new String[]{ "PoolUncopiedJobs" });
					}
					break;
				case TYPE_MIGRATE: {
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Type", new String[]{ "Migrate" });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Level", new String[]{ "Full" });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "SpoolData", new String[]{ "no" });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Selection Type", new String[]{ "PoolTime" });
					}
					break;
				default: {
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Type", new String[]{ "Backup" });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Level", new String[]{ level });
						if(poolFull != null && !poolFull.trim().isEmpty()) {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Full Backup Pool", new String[]{ poolFull });
						} else {
							BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Full Backup Pool");
						}
						if(poolIncremental != null && !poolIncremental.trim().isEmpty()) {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Incremental Backup Pool", new String[]{ poolIncremental });
						} else {
							BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Incremental Backup Pool");
						}
						if(poolDifferential != null && !poolDifferential.trim().isEmpty()) {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Differential Backup Pool", new String[]{ poolDifferential });
						} else {
							BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Differential Backup Pool");
						}
						String _value = "no";
						if(spooldata) {
							_value = "yes";
						}
						if(bandwith > 0) {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Maximum Bandwidth", new String[]{ String.valueOf(bandwith).concat("Mb/s") });
						} else {
							BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Maximum Bandwidth");
						}
						if(accurate) {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Accurate", new String[]{ "yes" });
						} else {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Accurate", new String[]{ "no" });
						}
						if (rescheduleOnError) {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule On Error", new String[]{ "yes" });
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule Interval", new String[]{ String.valueOf(rescheduleInterval).concat(" hours") });
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule Times", new String[]{ String.valueOf(rescheduleTimes) });
						} else {
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule On Error", new String[]{ "no" });
							BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule Interval");
							BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Reschedule Times");
						}
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "SpoolData", new String[]{ _value });
						BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Write Bootstrap", new String[]{ "\"/var/bacula/working/" + jobName + ".bsr\"" });
					}
					break;
			}
			
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Client", new String[]{ clientName });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "FileSet", new String[]{ fileset });
			if (storage != null && !storage.isEmpty())
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Storage", new String[]{ storage });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Messages", new String[]{ "Standard" });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Pool", new String[]{ pool });
			if (schedule != null && !schedule.isEmpty())
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Schedule", new String[]{ schedule });
			
			if(nextJob != null && !nextJob.isEmpty()) {
				Map<String, String> _job = getProgrammedJob(jobName);
				if(_job != null) {
					StringBuilder _sb = new StringBuilder();
					_sb.append("\"/usr/sbin/bacula-job ");
					if(verifyPreviousJob) {
						_sb.append("-v ");
						_sb.append(jobName);
						_sb.append(" ");
					}
					_sb.append(nextJob);
					_sb.append("\"");
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run After Job", new String[] { _sb.toString() });
				}
			} else {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run After Job");
			}
			
			if(hypervisorJob != null && !hypervisorJob.isEmpty()) {
				HypervisorManager _hm = HypervisorManager.getInstance("");
				Map<String, Object> _hjob = _hm.getHypervisorJob(hypervisorJob);
				if(_hjob.containsKey("hypervisor")) {
					StringBuilder _sb = new StringBuilder();
					_sb.append("\"/usr/sbin/bacula-job -h ");
					_sb.append(_hjob.get("hypervisor"));
					_sb.append(" ");
					_sb.append(hypervisorJob);
					_sb.append("\"");
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run Before Job", new String[] { _sb.toString() });
				}
			} else {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run Before Job");
			}
			
			String _value = "no";
			if(enabled) {
				_value = "yes";
			}
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Enabled", new String[]{ _value });
			 
			if(maxStartDelay > 0) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Start Delay", new String[]{ maxStartDelay + " hours" });
			} else {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Start Delay");
			}
				
			if(maxRunTime > 0) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Run Time", new String[]{ maxRunTime + " hours" });
			} else {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Run Time");
			}
				
			if(maxWaitTime > 0) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Wait Time", new String[]{ maxWaitTime + " hours" });
			} else {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Max Run Time");
			}
				
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Priority", new String[]{ String.valueOf(priority) });
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","jobs", jobName);
			throw _ex;
		}
		BackupOperator.reload();
	}
	
	public void setJobFileSet(String jobName, String fileset) throws Exception {
		File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
		BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "FileSet", new String[]{ fileset });
		BackupOperator.reload();
	}
	
	public void setJobNextJob(String jobName, String nextJob, boolean verifyPreviousJob) throws Exception {
		if (nextJob != null && jobName != null) {
			if (!nextJob.isEmpty() && !jobName.isEmpty()) {
				File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
				Map<String, String> _job = getProgrammedJob(jobName);
				if(_job != null) {
					StringBuilder _sb = new StringBuilder();
					_sb.append("\"/usr/sbin/bacula-job ");
					if(verifyPreviousJob) {
						_sb.append("-v ");
						_sb.append(jobName);
						_sb.append(" ");
					}
					_sb.append(nextJob);
					_sb.append("\"");
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", jobName, "Run After Job", new String[] { _sb.toString() });
				}
			}
		}
	}
	
	public void setJobScript(String name, String jobName, String command, boolean before, boolean onSuccess, boolean onFailure, boolean abortOnError, boolean windows, Map<String, String> variables) throws Exception {
		setJobScript(name, jobName, command, before, onSuccess, onFailure, abortOnError, windows, variables, true);
	}
	
	public void setJobScript(String name, String jobName, String command, boolean before, boolean onSuccess, boolean onFailure, boolean abortOnError, boolean windows, Map<String, String> variables, boolean onClient) throws Exception {
		if(name == null || name.isEmpty() ||
				jobName == null || jobName.isEmpty() ||
				command == null || command.isEmpty()) {
			throw new Exception("invalid script parameter");
		}
		
		if(name.contains(" ")) {
			throw new Exception("invalid script name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts");
		File _f_job = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + jobName + ".conf");
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		_f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + jobName + "_" +  name + ".conf");
		if(!_f.exists()) {
			BaculaConfiguration.addBaculaIncludeParameter(_f_job.getAbsolutePath(), "Job", jobName, _f.getAbsolutePath());
		}
		
		try {
			command = command.trim();
			
			if (command.startsWith("\""))
				command = command.substring(1);
			if (command.endsWith("\""))
				command = command.substring(0, command.length()-1);
			
			command = command.replaceAll("\r", "");
	
			if(windows) {					// Win
				if (command.contains(":\\")) {
					command = command.replace(":\\\\", "/");
				}
				command = command.replaceAll("\n", "&&");
				StringBuilder _sb = new StringBuilder();
				_sb.append("\"\\\"");
				_sb.append(command);
				_sb.append("\\\"\"");
				command = _sb.toString();
			} else {
				command = command.replaceAll("\n", ";");
				command = command.replaceAll("[;]{2,}?", ";");
				if(!command.startsWith("\"")) { 
					StringBuilder _sb = new StringBuilder();
					_sb.append("\"");
					_sb.append(command);
					_sb.append("\"");
					command = _sb.toString();
				}
			}
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeParameter(_f.getAbsolutePath(), "Job", jobName, _f.getAbsolutePath());
			throw _ex;
		}
		
		
		try {
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "Command", new String[] { command });	
			if (onClient)
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsOnClient", new String[] { "yes" });
			else
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsOnClient", new String[] { "no" });
			
			if(before) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsWhen", new String[] { "Before" });
			} else {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsWhen", new String[] { "After" });
				if(onSuccess) {
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsOnSuccess", new String[] { "yes" });
				} else {
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsOnSuccess", new String[] { "no" });
				}
				if(onFailure) {
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsOnFailure", new String[] { "yes" });
				} else {
					BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "RunsOnFailure", new String[] { "no" });
				}
			}
			if(!abortOnError) {
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "AbortJobOnError", new String[] { "no" });
			} else {
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "RunScript", null, "AbortJobOnError");
			}
			
			if (variables != null && variables.size()>0) {
				_f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + jobName + "_" +  name + ".vars.conf");
				Configuration vars = new Configuration(_f);
				for (String nameVar : variables.keySet()) {
					vars.setProperty(nameVar, variables.get(nameVar));
				}
				vars.store();
			}
			
		} catch(Exception _ex) {
			BaculaConfiguration.deleteBaculaIncludeParameter(_f.getAbsolutePath(), "Job", jobName, _f.getAbsolutePath());
			throw _ex;
		}
		BackupOperator.reload();
	}
}
