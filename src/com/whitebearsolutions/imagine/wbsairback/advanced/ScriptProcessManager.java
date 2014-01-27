package com.whitebearsolutions.imagine.wbsairback.advanced;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.util.Configuration;

public class ScriptProcessManager {
	public static Configuration _config;
	private final static Logger logger = LoggerFactory.getLogger(ScriptProcessManager.class);
	private static File _directory_scriptProcess;
	private Map<String, Map<String, Object>> _scriptProcess; 
	public static final int AFTER_EXECUTION=0;
	public static final int BEFORE_EXECUTION=1;
	
	public static final String INTERPRET_BOURNE_SHELL= "sh";
	public static final String INTERPRET_BASH= "bash";
	public static final String INTERPRET_C_SHELL= "csh";
	public static final String INTERPRET_PERLL = "perl";
	public static final String INTERPRET_WINDOWS = "win";
	public static final String INTERPRET_OTHERS = "others";
	
	public static Map<String, String> supportedShells = null;
	
	static{
		_directory_scriptProcess = new File(WBSAirbackConfiguration.getDirectoryAdvancedScriptProcess());
		if(!_directory_scriptProcess.exists()) {
			_directory_scriptProcess.mkdirs();
		}
		
		supportedShells = new TreeMap<String, String>();
		supportedShells.put(INTERPRET_BOURNE_SHELL, "Bourne Shell (sh)");
		supportedShells.put(INTERPRET_BASH, "Bash (bash)");
		supportedShells.put(INTERPRET_C_SHELL, "C Shell (csh)");
		supportedShells.put(INTERPRET_PERLL, "Perl (perl)");
		supportedShells.put(INTERPRET_WINDOWS, "Windows (cmd)");
		supportedShells.put(INTERPRET_OTHERS, "Other");
	}
	
	public ScriptProcessManager(String path) throws Exception {
		loadScriptProcess(new File(path));
	}
	
	public ScriptProcessManager() throws Exception{
		loadScriptProcess();
	}
	
	public void loadScriptProcess() {
		loadScriptProcess(null);
	}
	
	public void loadScriptProcess(File f){
		if (f == null)
			f = _directory_scriptProcess;
		try {
			this._scriptProcess=new HashMap<String, Map<String,Object>>();
			DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			for(File _f : f.listFiles()) {
				if(_f.isFile() && _f.getName().endsWith(".xml")) {
					try {
						Map<String, Object> _script=new HashMap<String, Object>();
						Document _doc = _db.parse(_f);
						NodeList _nl = _doc.getElementsByTagName("name");
						_script.put("name", _nl.item(0).getTextContent());
						_nl = _doc.getElementsByTagName("type");
						_script.put("type", _nl.item(0).getTextContent());
						_nl = _doc.getElementsByTagName("abortType");
						_script.put("abortType", _nl.item(0).getTextContent());
						_nl = _doc.getElementsByTagName("step");
						_script.put("step", _nl.item(0).getTextContent());
						_nl = _doc.getElementsByTagName("app");
						if (_nl!=null && _nl.getLength()>0){
							_script.put("application", _nl.item(0).getTextContent());
						}
						_nl = _doc.getElementsByTagName("system");
						if (_nl!=null && _nl.getLength()>0){
							_script.put("system", _nl.item(0).getTextContent());
						}
						_nl = _doc.getElementsByTagName("variable");
						int x=0;
						List <Map<String, Object>> _variablesList=new ArrayList<Map<String, Object>>();
						while (_nl!=null && x<_nl.getLength()){
							NodeList _variable=_nl.item(x).getChildNodes();
							int y=0;
							Map<String, Object> _variableItem=new HashMap<String, Object>();
							while (_variable!=null && y<_variable.getLength()){
								if (_variable.item(y).getNodeType() == Node.ELEMENT_NODE && "name".equals(_variable.item(y).getNodeName())) {
									_variableItem.put("name", _variable.item(y).getTextContent());
								}
								if (_variable.item(y).getNodeType() == Node.ELEMENT_NODE && "description".equals(_variable.item(y).getNodeName())) {
									_variableItem.put("description", _variable.item(y).getTextContent());
								}
								if (_variable.item(y).getNodeType() == Node.ELEMENT_NODE && "password".equals(_variable.item(y).getNodeName())) {
									_variableItem.put("password", _variable.item(y).getTextContent());
								}
								y++;
							}
							if (_variableItem.size()>0){
								_variablesList.add(_variableItem);
							}
							x++;
						}
						_script.put("variables", _variablesList);
						_nl = _doc.getElementsByTagName("scriptItem");
						x=0;
						List <Map<String, Object>> _scriptList=new ArrayList<Map<String, Object>>();
						while (_nl!=null && x<_nl.getLength()){
							NodeList _scriptElment=_nl.item(x).getChildNodes();
							int y=0;
							Map<String, Object> _scriptItem=new HashMap<String, Object>();
							while (_scriptElment!=null && y<_scriptElment.getLength()){
								if (_scriptElment.item(y).getNodeType() == Node.ELEMENT_NODE && "order".equals(_scriptElment.item(y).getNodeName())) {
									_scriptItem.put("order", _scriptElment.item(y).getTextContent());
								}
								if (_scriptElment.item(y).getNodeType() == Node.ELEMENT_NODE && "content".equals(_scriptElment.item(y).getNodeName())) {
									_scriptItem.put("content", _scriptElment.item(y).getTextContent());
								}
								if (_scriptElment.item(y).getNodeType() == Node.ELEMENT_NODE && "shell".equals(_scriptElment.item(y).getNodeName())) {
									_scriptItem.put("shell", _scriptElment.item(y).getTextContent());
								}
								y++;
							}
							if (_scriptItem.size()>0){
								_scriptList.add(_scriptItem);
							}
							x++;
						}
						_script.put("scripts", _scriptList);
						if (_script.size()>0){
							this._scriptProcess.put((String)_script.get("name"), _script);
						}
					}catch(Exception _ex) {}
				}
			
			}
		}catch(Exception _ex) {}
	
	}
	
	/**
	 * Genera los scripts concretos del job recibido
	 * @param nameJob
	 * @param variablesValues
	 * @param scriptProcess
	 * @param jm
	 * @throws Exception
	 */
	public static void generateScriptsJob(String nameJob, String scriptProcessName, JobManager jm, TreeMap<String, Map<String, String>> scriptProcess,boolean abort, boolean before, boolean fail, boolean success, Map<String, String> variableValues) throws Exception {
		try {
			if (scriptProcess != null && !scriptProcess.isEmpty()) {
				Iterator<String> it = scriptProcess.keySet().iterator();
				while (it.hasNext()) {
					String orderScript=it.next();
					Map<String, String> scriptObj = scriptProcess.get(orderScript);
					if (scriptObj != null && !scriptObj.isEmpty()) {
						String command = scriptObj.get("script");
						command = command.replaceAll("\"", "\\\\\"");
						command = command.replaceAll("'", "\\\\\"");
						command = translateCommandsByShell(command, scriptObj.get("shell"));
						boolean win = false;
						if (scriptObj.get("shell") != null && scriptObj.get("shell").equals(INTERPRET_WINDOWS))
							win=true;
						jm.setJobScript(getNameScript(scriptProcessName, orderScript), nameJob, command, before, success, fail, abort, win, variableValues);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error generando scripts para el job: {}. Ex: {}", nameJob, ex.getMessage());
			throw new Exception("Error generating script jobs for: "+nameJob+". Ex:"+ex.getMessage());
		}
	}
	
	public static String translateCommandsByShell(String command, String shell) throws Exception {
		String shellCommand = "";
		if (shell != null) {
				if (shell.equals(INTERPRET_BASH) || shell.equals(INTERPRET_BOURNE_SHELL) || shell.equals(INTERPRET_C_SHELL) || shell.equals(INTERPRET_PERLL)) 
					shellCommand = shell+" -c '"+command+"'";
				else
					shellCommand = command;
				return shellCommand;
		} else {
			return command;
		}
	}
	
	public static String getNameScript(String scriptProcessName, String order) throws Exception {
		return scriptProcessName+ "_"+order;
	}
	
	
	public static String getNameRemoteStorageScript(String nameJob, Integer order) throws Exception {
		return nameJob + "_" +RemoteStorageManager.getNameRemoteStorageScript(order);
	}
	
	public Map<String, Map<String, Object>> getScriptByStepAndType(String nameStep, String type) throws Exception {
		Map<String, Map<String, Object>> scriptProcesses = listScript();
		Map<String, Map<String, Object>> list = new HashMap<String, Map<String, Object>>();
		for (String nameScript : scriptProcesses.keySet()) {
			Map<String, Object> script = scriptProcesses.get(nameScript);
			if ( script.get("step") != null && ((String)script.get("step")).equals(nameStep) && script.containsKey(type) && script.get(type) != null && !((String)script.get(type)).isEmpty())
				list.put((String) script.get("name"), script);
		}
		return list;
	}
	
	
	public static String getPathScript(String nameScript) throws Exception  {
		return WBSAirbackConfiguration.getDirectoryAdvancedScriptProcess() + File.separator + nameScript +  ".xml";
	}
	
	/**
	 * Guarda un script en el disco
	 * @param nameScript
	 * @param _values
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void saveScript(String nameScript, Map<String, Object> _values, SysAppsInventoryManager saim) throws Exception {
		DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		File _f = new File(WBSAirbackConfiguration.getDirectoryAdvancedScriptProcess() + File.separator + nameScript +  ".xml");
		Document _doc = _db.newDocument();
		Element _e_script = _doc.createElement("script");
		Element _e_name = _doc.createElement("name");
		_e_name.setTextContent(nameScript);
		_e_script.appendChild(_e_name);
		_e_name = _doc.createElement("abortType");
		_e_name.setTextContent((String)_values.get("abortType"));
		_e_script.appendChild(_e_name);
		_e_name = _doc.createElement("type");
		_e_name.setTextContent((String)_values.get("type"));
		_e_script.appendChild(_e_name);
		_e_name = _doc.createElement("step");
		_e_name.setTextContent((String)_values.get("step"));
		_e_script.appendChild(_e_name);
		_e_name = _doc.createElement("app");
		_e_name.setTextContent((String)_values.get("application"));
		_e_script.appendChild(_e_name);
		_e_name = _doc.createElement("system");
		_e_name.setTextContent((String)_values.get("system"));
		_e_script.appendChild(_e_name);
		_e_name = _doc.createElement("variables");
		_e_script.appendChild(_e_name);
		List<Map<String, String>> _variables=(List<Map<String, String>>)_values.get("variables");
		if (_variables!=null){
			for (Map<String, String> _variable: _variables){
				if (_variable.size()>0){
					Element _e_variableItem = _doc.createElement("variable");
					Element _e_name_variable = _doc.createElement("name");
					_e_name_variable.setTextContent(_variable.get("name"));
					_e_variableItem.appendChild(_e_name_variable);
					Element _e_descripcion_variable = _doc.createElement("description");
					_e_descripcion_variable.setTextContent(_variable.get("description"));
					_e_variableItem.appendChild(_e_descripcion_variable);
					Element _e_password_variable = _doc.createElement("password");
					_e_password_variable.setTextContent(_variable.get("password"));
					_e_variableItem.appendChild(_e_password_variable);
					_e_name.appendChild(_e_variableItem);
				}
			}
		}		
		_e_name = _doc.createElement("scripts");
		_e_script.appendChild(_e_name);
		List<Map<String, String>> _scripts=(List<Map<String, String>>)_values.get("scripts");
		if (_scripts!=null){
			for (Map<String, String> _script: _scripts){
				if (_script.size()>0){
					Element _e_scriptItem = _doc.createElement("scriptItem");
					Element _e_name_script = _doc.createElement("order");
					_e_name_script.setTextContent(_script.get("order"));
					_e_scriptItem.appendChild(_e_name_script);
					Element _e_descripcion_script = _doc.createElement("content");
					_e_descripcion_script.setTextContent(_script.get("content"));
					_e_scriptItem.appendChild(_e_descripcion_script);
					_e_name.appendChild(_e_scriptItem);
					Element _e_shell_script = _doc.createElement("shell");
					_e_shell_script.setTextContent(_script.get("shell"));
					_e_scriptItem.appendChild(_e_shell_script);
					_e_name.appendChild(_e_scriptItem);
				}
			}
		}
		_doc.appendChild(_e_script);
		Source source = new DOMSource(_doc);        
        Result result = new StreamResult(_f);
    	FileLock _fl = new FileLock(_f);
        Transformer _t = TransformerFactory.newInstance().newTransformer();
    	try {
    		_fl.lock();
	    	_t.transform(source, result);
    	} finally {
    		_fl.unlock();
    	}
		
    	String app = null;
    	if (_values.get("application") != null)
    		app = (String) _values.get("application");
    	String sys = (String) _values.get("system");
    	saim.addScript(nameScript, app, sys);
		loadScriptProcess();
	}
	
	
	/**
	 * Lista los scriptprocess definidos en formato de mapas de valores
	 * @return
	 */
	public Map<String, Map<String, Object>> listScript() throws Exception {
		return this._scriptProcess;
		
	}
	
	/**
	 * Obtiene los valores de un script dado
	 * @param stepName
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getScript(String scriptName) throws Exception {
		return _scriptProcess.get(scriptName);		
	}
	
	/**
	 * Elimina un script cuyo nombre entra por parametro
	 * @param appName
	 * @return
	 * @throws Exception
	 */
	public void deleteScript(String scriptName, SysAppsInventoryManager saim) throws Exception {
		String isInTemplate = TemplateJobManager.isEntityInAnyTemplateJob(scriptName, StepManager.TYPE_STEP_SCRIPT_APP);
		if (isInTemplate == null) {
			isInTemplate = TemplateJobManager.isEntityInAnyTemplateJob(scriptName, StepManager.TYPE_STEP_SCRIPT_SYSTEM);
			if (isInTemplate == null) {
				String job = isScriptProcessUsedOnJob(scriptName);
				if (job == null) {
					Map<String, Object> _script = getScript(scriptName);
					String application = null;
					String system = null;
					if (_script.get("application") != null)
						application = (String) _script.get("application");
					if (_script.get("system") != null)
						system = (String)_script.get("system");
					
					_scriptProcess.remove(scriptName);
					File _f = new File(WBSAirbackConfiguration.getDirectoryAdvancedScriptProcess() + File.separator + scriptName +  ".xml");
					if (_f.exists()){
						_f.delete();
					}
					saim.deleteScript(scriptName,application, system);
				} else {
					logger.info("Script {} utilizado en job {}. No se puede eliminar", scriptName, job);
					throw new Exception("Script is used in job: "+job);
				}
			} else {
				logger.info("Script {} relacionado con template {}. No se puede eliminar", scriptName, isInTemplate);
				throw new Exception("Script is associated with a tempate job step: "+isInTemplate);
				
			}
		} else {
			logger.info("Script {} relacionado con template {}. No se puede eliminar", scriptName, isInTemplate);
			throw new Exception("Script is associated with a tempate job step: "+isInTemplate);
			
		}	
	}
	
	public String isScriptProcessUsedOnJob(String name) throws Exception {
		File _dir = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/scripts");
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		File[] _list = _dir.listFiles();
		for(File _f : _list) {
			if(_f.getName().endsWith(".conf") && _f.getName().contains(name)) {
				return _f.getName().substring(0,_f.getName().indexOf("_"));
			}
		}
		return null;
	}
	
	public String isAppOnScriptProcess(String app) throws Exception {
		if (this._scriptProcess != null && this._scriptProcess.size()>0) {
			for (String name : _scriptProcess.keySet()) {
				Map<String, Object> script = _scriptProcess.get(name);
				if (script.get("application") != null && script.get("application").equals(app)) {
					return name;
				}
			}
		}
		return null;
	}
	
	public List<String> getScriptProcessWithApp(String app) throws Exception {
		if (this._scriptProcess != null && this._scriptProcess.size()>0) {
			List<String> scripts = new ArrayList<String>();
			for (String name : _scriptProcess.keySet()) {
				Map<String, Object> script = _scriptProcess.get(name);
				if (script.get("application") != null && script.get("application").equals(app)) {
					scripts.add(name);
				}
			}
			return scripts;
		}
		return null;
	}
	
	public List<String> getScriptProcessWithSystem(String system) throws Exception {
		if (this._scriptProcess != null && this._scriptProcess.size()>0) {
			List<String> scripts = new ArrayList<String>();
			for (String name : _scriptProcess.keySet()) {
				Map<String, Object> script = _scriptProcess.get(name);
				if (script.get("system") != null && script.get("system").equals(system)) {
					scripts.add(name);
				}
			}
			return scripts;
		}
		return null;
	}
	
	public String isSystemOnScriptProcess(String system) throws Exception {
		if (this._scriptProcess != null && this._scriptProcess.size()>0) {
			for (String name : _scriptProcess.keySet()) {
				Map<String, Object> script = _scriptProcess.get(name);
				if (script.get("system") != null && script.get("system").equals(system)) {
					return name;
				}
			}
		}
		return null;
	}
	
 	public static String getSmartContent(String content, Map<String, String> attributes)
			throws Exception {
		if (content == null || content.isEmpty()) {
			return null;
		}
		int _old_offset = 0;
		StringBuilder _sb = new StringBuilder();
		for (int _offset = content.indexOf("[[[", 0); _offset != -1; _offset = content
				.indexOf("[[[", _offset)) {
			_sb.append(content.substring(_old_offset, _offset));
			_offset += 3;
			if (content.indexOf("]]]", _offset) != -1) {
				String _name = content.substring(_offset, content.indexOf(
						"]]]", _offset));
				if (attributes != null && attributes.containsKey(_name)) {
						_sb.append(attributes
								.get(_name));
				} else {
					_sb.append("");
				}
				_old_offset = content.indexOf("]]]", _offset) + 3;
			}
		}
		_sb.append(content.substring(_old_offset, content.length()));
		return _sb.toString();
	}
 	
 	public static void launchAllJobScriptTransformation(Configuration c) throws Exception {
 		try {
	 		JobManager jm = new JobManager(c);
	 		ScriptProcessManager spm = new ScriptProcessManager();
	 		SysAppsInventoryManager saim = new SysAppsInventoryManager();
	 		if (!StepManager.existsStep(StepManager.GENERIC_STEP_NAME)) {
	 			StepManager.saveStep(StepManager.GENERIC_STEP_NAME, StepManager.TYPE_STEP_SCRIPT_SYSTEM, false);
			}
	 		
	 		List<String> jobs = jm.getAllProgrammedJobs();
	 		for (String jobName : jobs) {
	 			Map<String, String> job = jm.getProgrammedJob(jobName);
	 			List<Map<String, Object>> scripts = JobManager.getJobScripts(jobName, true);
	 			transformScriptsToScriptProcessess(scripts, jobName, job.get("client"), jm, spm, saim);
	 		}
 		} catch (Exception ex) {
 			logger.error("Error on process to transform old script to new script process ... Ex: {}", ex.getMessage());
 		}
 	}
 	
 	private static void transformScriptsToScriptProcessess(List<Map<String, Object>> _scripts, String jobName, String clientName, JobManager _jm, ScriptProcessManager spm, SysAppsInventoryManager saim) throws Exception {
 		List<Map<String, String>> _scriptsItems=new ArrayList<Map<String, String>>();
		Integer order = 1;
		for (Map<String, Object> script : _scripts) {
			if (script.get("command") != null && script.get("scripts") == null) {
				Map<String, Object> _script=new HashMap<String, Object>();
    			for (String key : script.keySet())
    				_script.put(key, script.get(key));
    			
    			String name = jobName+"-"+(String)_script.get("name");
    			try {
	    			if (name != null && !name.contains("--")) {
	    				logger.debug("Reinterpretando script antiguo {} a processScript: {} ... ", (String)_script.get("name"), name);
						Map<String, String> _scriptNew=new HashMap<String, String>();
						_scriptNew.put("order", "1");
		    			String command = ((String)script.get("command"));
						boolean win = false;
						if (command.contains(":/")) {
							win = true;
							_scriptNew.put("shell", ScriptProcessManager.INTERPRET_WINDOWS);
							logger.debug("Script tipo windows");
						} else {
							_scriptNew.put("shell", ScriptProcessManager.INTERPRET_OTHERS);
							logger.debug("Script tipo shell");
						}
						logger.debug("El comando es {}", command);
						_scriptNew.put("content", command);
						_scriptsItems.add(_scriptNew);
						
						boolean before = false;
						if (((String)script.get("before")) != null) {
							before = Boolean.valueOf(((String)script.get("before")));
						}
						if (before)
							_script.put("type", String.valueOf(ScriptProcessManager.BEFORE_EXECUTION));
						else
							_script.put("type", String.valueOf(ScriptProcessManager.AFTER_EXECUTION));
						
						boolean abort = false;  
						if ((String)script.get("abort") != null) {
							abort = Boolean.valueOf(((String)script.get("abort")));
						}
						_script.put("abortType", String.valueOf(abort));
		    			_script.put("scripts", _scriptsItems);
		    			_script.put("step", StepManager.GENERIC_STEP_NAME);
		    			_script.put("system", SysAppsInventoryManager.GENERIC_SYSTEM_NAME);
						spm.saveScript(name, _script, saim);
						
						boolean success = false;  
						if ((String)script.get("success") != null)
							success = Boolean.valueOf(((String)script.get("success")));
						boolean failure = false;  
						if ((String)script.get("failure") != null)
							failure = Boolean.valueOf(((String)script.get("failure")));
						
						_jm.setJobScript(name+"--"+order+"_1", jobName, command, before, success, failure, abort, win, null);
						_jm.removeJobScript(jobName, (String)_script.get("name"));
						_jm.deleteJobScript(jobName, (String)_script.get("name"));
						logger.debug("Habia script antiguo {} y lo hemos reinterpretado a processScript: {} ", (String)_script.get("name"), name);
	    			}
    			} catch (Exception ex) {
    				logger.error("Atencion. Error reinterpretando script antiguo {} a processScript: {} ", (String)_script.get("name"), name);
    			}
			}
			order++;
		}
 	}
}



