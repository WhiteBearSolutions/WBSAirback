package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.RemoteStorageManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StorageInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.SysAppsInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.CategoryManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ScheduleManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ApplicationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupJobConfigRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupStepRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.VariableValueRs;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.util.Configuration;


public class AdvancedBackup extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public final static int ADD_GROUPJOB_BY_TEMPLATEJOB = 2;
	public final static int STORE_GROUPJOB_BY_TEMPLATEJOB = 3;
	public final static int STORE_CLIENT = 4;
	public final static int SELECT_CLIENTS = 74210458;
	public final static int SEARCH_JSON = 62974502;
	public final static int TEMPLATE_LIST = 15697845;
	
	public final static String baseUrl = "/admin/"+AdvancedBackup.class.getSimpleName();
	
	private final static Logger logger = LoggerFactory.getLogger(AdvancedBackup.class);
	public static Map<String, String> selectTemplateJob = null;
	public static Map<String, String> selectClients = null;
	public static Map<String, String> selectTypeStep = null;
	public static Map<String, String> msgTypeGroupJob = null;
	
	public LicenseManager _lm;

	@SuppressWarnings("unchecked")
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter _xhtml_out=response.getWriter();
		
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	this.type = 1;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
				throw new Exception("backup director service is not running");
			}
			
			Configuration _c = this.sessionManager.getConfiguration();
			_lm = new LicenseManager();
			JobManager jm = new JobManager(_c);
			ClientManager cm = new ClientManager(_c);
			ISCSIManager iscsim = new ISCSIManager(_c);
			
			
			if (this.type == SELECT_CLIENTS) {
				boolean onChange = false;
				boolean withLocal = true;
				if (request.getParameter("typeStep") != null && request.getParameter("typeStep").equals(StepManager.TYPE_STEP_BACKUP))
					onChange = true;
				else if (request.getParameter("typeStep") != null && !request.getParameter("typeStep").equals(StepManager.TYPE_STEP_BACKUP))
					withLocal = false;
					
				printSelectClient(_xhtml_out, request.getParameter("step"), Integer.parseInt(request.getParameter("order")), cm, onChange, withLocal, null);
				return;
			} else if (this.type == SEARCH_JSON && request.getParameter("entity") != null && !request.getParameter("entity").isEmpty() && request.getParameter("term") != null && !request.getParameter("term").isEmpty()) {
				_xhtml_out.print(printJsonSearch(request.getParameter("entity"), request.getParameter("term").toLowerCase()));
				return;
			} else if (this.type == TEMPLATE_LIST && request.getParameter("element") != null && !request.getParameter("element").isEmpty() && request.getParameter("typeEntity") != null && !request.getParameter("typeEntity").isEmpty()) {
				_xhtml_out.print(printTemplateList(request.getParameter("element"), request.getParameter("typeEntity")));
				return;
			}
			
			if (this.type != STORE_CLIENT) {
				response.setContentType("text/html");
				writeDocumentHeader();
			}

	    	fillSelects(cm);
	    	switch(this.type) {
	    		
    			default: {
					_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
	    			 if("es".equals(this.messagei18N.getLocale().getLanguage()))
	    				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
	    			 else
	    				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
	    			_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
	    			
					_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />");
					if("es".equals(this.messagei18N.getLocale().getLanguage()))
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>");
					else
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>");
				    _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>");
				    
    				_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" id=\"form\" method=\"post\">");
    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\""+ADD_GROUPJOB_BY_TEMPLATEJOB+"\">");
    				_xhtml_out.print("<input type=\"hidden\" name=\"templateJobName\" value=\"\">");
    				
    				printSectionHeader(_xhtml_out, getLanguageMessage("advanced.backup.info"));
	    			
    				 if (selectTemplateJob != null && selectTemplateJob.size()>0) {
	    				_xhtml_out.println("<div class=\"window\">");
	            		_xhtml_out.println("<h2>");
	            		_xhtml_out.print(getLanguageMessage("advanced.groupjob.addbytemplate"));
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
                    	_xhtml_out.print(HtmlFormUtils.inputTextObj("name", getLanguageMessage("advanced.groupjob.name"), null, true,"validate[required,custom[onlyLetterNumber]]"));
                    	_xhtml_out.println("<div class=\"clear\"></div>");
                    	_xhtml_out.println("</fieldset>");
                    	_xhtml_out.println("<div class=\"clear\"></div>");
                    	_xhtml_out.println("</div>");
                    }
                	
                 	_xhtml_out.println(" <style> .ui-autocomplete-loading {background: white url('../images/ui-anim_basic_16x16.gif') right center no-repeat;} </style>");
                 	_xhtml_out.println("<div id=\"buscador\" style=\"margin:20px auto;width:94%;\">");
                 	_xhtml_out.println("	<ul>");
                 	_xhtml_out.println("		<li><a href=\"#buscadorGeneral\">"+getLanguageMessage("advanced.backup.search.general")+"</a></li>");
                 	_xhtml_out.println("		<li><a href=\"#buscadorInventario\">"+getLanguageMessage("advanced.backup.search.inventory")+"</a></li>");
                 	_xhtml_out.println("		<li><a href=\"#buscadorScripts\">"+getLanguageMessage("advanced.backup.search.scripts")+"</a></li>");
                 	_xhtml_out.println("		<li><a href=\"#buscadorConceptos\">"+getLanguageMessage("advanced.backup.search.templates")+"</a></li>");
                 	_xhtml_out.println("	</ul>");
                 	_xhtml_out.println("	<div id=\"buscadorGeneral\" style=\"height:30px;\">");
                 	_xhtml_out.println("		<div id=\"buscadorAll\" class=\"ui-widget\">");
                 	_xhtml_out.println(" 			<label for=\"all\">"+getLanguageMessage("advanced.backup.search.all")+": </label>");
                 	_xhtml_out.println(" 			<input id=\"all\" size=\"35\" class=\"form_text\" type=\"text\"/>");
                 	_xhtml_out.println("		</div>");
                 	_xhtml_out.println("	</div>");
                 	_xhtml_out.println("	<div id=\"buscadorInventario\" style=\"height:30px;\">");
                 	_xhtml_out.println("		<div id=\"buscadorSistemas\" class=\"ui-widget\" style=\"width:50%;float:left;\">");
                 	_xhtml_out.println(" 			<label for=\"system\">"+getLanguageMessage("advanced.backup.search.system")+": </label>");
                 	_xhtml_out.println(" 			<input id=\"system\" class=\"form_text\" type=\"text\"/>");
                 	_xhtml_out.println("		</div>");
                 	_xhtml_out.println("		<div id=\"buscadorAplicaciones\" class=\"ui-widget\" style=\"width:50%;float:left;\">");
                 	_xhtml_out.println(" 			<label for=\"application\">"+getLanguageMessage("advanced.backup.search.application")+": </label>");
                 	_xhtml_out.println(" 			<input id=\"application\" class=\"form_text\" type=\"text\"/>");
                 	_xhtml_out.println("		</div>");
                 	_xhtml_out.println("	</div>");
                 	_xhtml_out.println("	<div id=\"buscadorScripts\" style=\"height:30px;\">");
                 	_xhtml_out.println("		<div id=\"buscadorScripts\" class=\"ui-widget\" style=\"width:50%;float:left;\">");
                 	_xhtml_out.println(" 			<label for=\"script\">"+getLanguageMessage("advanced.backup.search.script")+": </label>");
                 	_xhtml_out.println(" 			<input id=\"script\" class=\"form_text\" type=\"text\"/>");
                 	_xhtml_out.println("		</div>");
                 	_xhtml_out.println("		<div id=\"buscadorAdvancedStorage\" class=\"ui-widget\" style=\"width:50%;float:left;\">");
                 	_xhtml_out.println(" 			<label for=\"advancedstorage\">"+getLanguageMessage("advanced.backup.search.advancedstorage")+": </label>");
                 	_xhtml_out.println(" 			<input id=\"advancedstorage\" class=\"form_text\" type=\"text\"/>");
                 	_xhtml_out.println("		</div>");
                 	_xhtml_out.println("	</div>");
                 	_xhtml_out.println("	<div id=\"buscadorConceptos\" style=\"height:30px;\">");
                 	_xhtml_out.println("		<div id=\"buscadorPlantillas\" class=\"ui-widget\" style=\"width:50%;float:left;\">");
                 	_xhtml_out.println(" 			<label for=\"template\">"+getLanguageMessage("advanced.backup.search.template")+": </label>");
                 	_xhtml_out.println(" 			<input id=\"template\" class=\"form_text\" type=\"text\"/>");
                 	_xhtml_out.println("		</div>");
                 	_xhtml_out.println("		<div id=\"buscadorPasos\" class=\"ui-widget\" style=\"width:50%;float:left;\">");
                 	_xhtml_out.println(" 			<label for=\"step\">"+getLanguageMessage("advanced.backup.search.step")+": </label>");
                 	_xhtml_out.println(" 			<input id=\"step\" class=\"form_text\" type=\"text\"/>");
                 	_xhtml_out.println("		</div>");
                 	_xhtml_out.println("	</div>");
                 	_xhtml_out.println("</div>");
                 	
                 	if (selectTemplateJob != null && selectTemplateJob.size()>0) {
                 		_xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
                 		_xhtml_out.println("<div id=\"listadoPlantillas\" style=\"clear:both;width:100%;margin:auto;\"><table id=\"tablaPlantillas\" style=\"margin-left:0px;margin-right:0px;\"></table>");
                 		_xhtml_out.println("</div>");
                     	pageJS+=getJqGridJS("tablaPlantillas", "listadoPlantillas");
                     	pageJSFuncs+=getSubmitTemplateFormJS();
                     	pageJSFuncs+=emptyGridFuncJS("tablaPlantillas");
                 	} else
  	    	    		_xhtml_out.println(HtmlFormUtils.getNoResultsWindow(getLanguageMessage("advanced.templatejobs"), getLanguageMessage("advanced.backup.notemplatejobs.info")));
                	
                 	_xhtml_out.print("</form>");
                 	
                 	pageJS+=getAutoCompleteJS("all", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("system", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("application", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("script", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("advancedstorage", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("template", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("step", "tablaPlantillas");
                 	pageJS+="$( \"#buscador\" ).tabs()\n";
                 	pageJS+= "$('#form').validationEngine();";
    			} break;
    			
    			case ADD_GROUPJOB_BY_TEMPLATEJOB: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
    				
					if (!request.getParameter("name").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name.invalid"));
					}
    				
    				if (request.getParameter("edit") == null && GroupJobManager.existsGroupJob(request.getParameter("name"))) {
    					throw new Exception(getLanguageMessage("advanced.groupjob.error.duplicatedname"));
    				}
    				
    				if(request.getParameter("templateJobName") == null || request.getParameter("templateJobName").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.templateJobName"));
					}
    				
    				Map<String, Object> templateJob = TemplateJobManager.getTemplateJob(request.getParameter("templateJobName"));
    				if (templateJob == null || templateJob.isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.groupjob.error.templateJob"));
    				}
    				
    				Map<Integer, Map<String, String>> steps = null;
    				if (templateJob.get("steps") != null) {
    					steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
    				}
    				
    				if (steps == null || steps.isEmpty())
    					throw new Exception(getLanguageMessage("advanced.groupjob.error.nosteps"));
    				
    				printTemplateJobWizard(_xhtml_out, request.getParameter("name") , (String) templateJob.get("name"), steps, _c);
    				
    			} break;
    			
    			case STORE_GROUPJOB_BY_TEMPLATEJOB: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
    				
					if (!request.getParameter("name").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
					}
    				
    				if(request.getParameter("nameTemplateJob") == null || request.getParameter("nameTemplateJob").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.nameTemplateJob"));
					}
    				
    				String groupJobName = request.getParameter("name");
    				
    				Map<String, Object> templateJob = TemplateJobManager.getTemplateJob(request.getParameter("nameTemplateJob"));
    				if (templateJob == null || templateJob.isEmpty())
    					throw new Exception(getLanguageMessage("advanced.groupjob.error.templateJob"));
    				
    				String templateStorageType = TemplateJobManager.getTypeStorageTemplate(request.getParameter("nameTemplateJob"));
    						
    				Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
    				if (steps == null || steps.isEmpty())
    					throw new Exception(getLanguageMessage("advanced.groupjob.error.templateJob.steps"));
    				
    				try {
						String storage = null;
						if (request.getParameter("storage") != null && !request.getParameter("storage").isEmpty()) {
							storage = request.getParameter("storage");
						} else {
							throw new Exception(getLanguageMessage("backup.jobs.exception.repository"));
						}
						
						if (!BackupOperator.isBlock_reload())
    						BackupOperator.setBlock_reload(true);
						
	    				Map<Integer, Map<String, Object>> jobs = new TreeMap<Integer, Map<String, Object>>();
	    				List<GroupStepRs> configSteps = new ArrayList<GroupStepRs>();
	    				boolean first = true;
	    				for (Integer order : steps.keySet()) {
	    					GroupStepRs configStep = new GroupStepRs();
    						String schedule = null;
    						if (first && request.getParameter("schedule") != null && !request.getParameter("schedule").isEmpty()) {
    							schedule = request.getParameter("schedule");
    							first = false;
    						}
    						
	    					Map<String, String> step = steps.get(order);
	    					String nameStep = step.get("name");
	    					String typeStep = step.get("type");
	    					String dataStep = step.get("data");
	    					String nameJob = null;
	    					String inventoryStep = null;
	    					String nextJob = getNextJob(request, order, steps, groupJobName, cm);
	    					
	    					configStep.setName(nameStep);
	    					configStep.setOrder(order);
	    					configStep.setType(typeStep);
	    					
	    					if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
	    						Map<String, Object> storageInventory = null;
	    						Map<String, Object> remoteStorage = null;
	    						Map<String, String> variablesValues = new HashMap<String, String>();
	    						if (request.getParameter(nameStep+"storage_inventory") != null && !request.getParameter(nameStep+"storage_inventory").isEmpty()) {
		    						storageInventory = StorageInventoryManager.getStorage(request.getParameter(nameStep+"storage_inventory"));
		    						remoteStorage = RemoteStorageManager.getRemoteStorage(dataStep);
		    						if (remoteStorage != null && !remoteStorage.isEmpty()) {
			    		    			Map<String, Map<String, String>> variables = (Map<String, Map<String, String>>) remoteStorage.get("variables");
			    		    			List<VariableValueRs> vars = new ArrayList<VariableValueRs>();
			    						for (String nameVar : variables.keySet()) {
			    							VariableValueRs var = new VariableValueRs();
			    							var.setName(nameVar);
			    							String value = "";
			    							if (request.getParameter(nameStep+"var"+nameVar) != null && !request.getParameter(nameStep+"var"+nameVar).isEmpty()) {
			    								value = request.getParameter(nameStep+"var"+nameVar);
			    							}
			    							variablesValues.put(nameVar, value);
			    							var.setValue(value);
			    							vars.add(var);
			    						}
			    						if (!vars.isEmpty())
			    							configStep.setVariables(vars);
		    						} else {
		    							throw new Exception(getLanguageMessage("advanced.groupjob.error.advancedstorage.remotestorage")+" :"+nameStep);
		    						}
		    						
	    						} else {
	    							throw new Exception(getLanguageMessage("advanced.groupjob.error.advancedstorage.inventory.name")+" :"+nameStep);
	    						}
	    						inventoryStep = (String) storageInventory.get("name");
	    						nameJob = GroupJobManager.getGroupJobNameJob(groupJobName, order, "airback-fd", nameStep);
	    						jm.setEmptyJobForScripts(nameJob, "airback-fd", nextJob, null, schedule, storage);
	    						RemoteStorageManager.generateScriptsJob(nameJob, nameStep, storageInventory, remoteStorage, variablesValues, jm, iscsim);
	    						
	    					} else if (typeStep.equals(StepManager.TYPE_STEP_BACKUP)) {
	    						boolean verifyPreviousJob = true, spoolData = false, accurate = false, enabled = true;
	    		    			int maxStartDelay = 0, maxRunTime = 0, maxWaitTime = 0, priority = 0, bandwith = 0;
	    		    			
	    						if (request.getParameter(nameStep+"client") == null || request.getParameter(nameStep+"client").isEmpty())
	    							throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.client")+" :"+nameStep);
	    						
	    						String clientName = cm.getClientName(Integer.parseInt(request.getParameter(nameStep+"client")));
	    						configStep.setClient(clientName);
	    						
	    						if (request.getParameter(nameStep+"level") == null || request.getParameter(nameStep+"level").isEmpty()) 
	    							throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.level")+" :"+nameStep);
	    						
	    						if (!clientName.equals("airback-fd") && (request.getParameter(nameStep+"fileset") == null || request.getParameter(nameStep+"fileset").isEmpty())) 
	    							throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.fileset")+" :"+nameStep);
	    						
	    						if (request.getParameter(nameStep+"poolDefault") == null || request.getParameter(nameStep+"poolDefault").isEmpty())
	    							throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.poolDefault")+" :"+nameStep);
	    						
	    						try {
	    		    				maxStartDelay = Integer.parseInt(request.getParameter(nameStep+"maxStartDelay"));
	    		    			} catch(NumberFormatException _ex) {
	    		    				throw new Exception(getLanguageMessage("backup.jobs.exception.delay"));
	    		    			}
	    		    			try {
	    		    				maxRunTime = Integer.parseInt(request.getParameter(nameStep+"maxRunTime"));
	    		    			} catch(NumberFormatException _ex) {
	    		    				throw new Exception(getLanguageMessage("backup.jobs.exception.runtime"));
	    		    			}
	    		    			try {
	    		    				maxWaitTime = Integer.parseInt(request.getParameter(nameStep+"maxWaitTime"));
	    		    			} catch(NumberFormatException _ex) {
	    		    				throw new Exception(getLanguageMessage("backup.jobs.exception.timeout"));
	    		    			}
	    		    			
	    		    			if(request.getParameter(nameStep+"bandwith") != null && !request.getParameter(nameStep+"bandwith").isEmpty()) {
	    		    				try {
	    			    				bandwith = Integer.parseInt(request.getParameter(nameStep+"bandwith"));
	    			    			} catch(NumberFormatException _ex) {
	    			    				throw new Exception(getLanguageMessage("backup.jobs.exception.bandwith"));
	    			    			}
	    		    			}
	    		    			
	    						if (request.getParameter(nameStep+"spoolData") != null && !request.getParameter(nameStep+"spoolData").isEmpty() && request.getParameter(nameStep+"spoolData").equals("yes"))
	    							spoolData = true;
	    						
	    						nameJob = GroupJobManager.getGroupJobNameJob(groupJobName, order, clientName, nameStep);
	    						Map<String, Object> storageInventory = null;
	    						String fileset = request.getParameter(nameStep+"fileset");
	    						if (clientName.equals("airback-fd") && request.getParameter(nameStep+"storage_inventory") != null && !request.getParameter(nameStep+"storage_inventory").isEmpty()) {
		    						storageInventory = StorageInventoryManager.getStorage(request.getParameter(nameStep+"storage_inventory"));
		    						inventoryStep = (String)  storageInventory.get("name");
		    						fileset = StorageInventoryManager.getNameHiddenFileset((String) storageInventory.get("name"), templateStorageType);
		    						if (BaculaConfiguration.existBaculaInclude("/etc/bacula/bacula-dir.conf", "@/etc/bacula/" + "filesets" + "/" + fileset + ".conf"))
		    							FileSetManager.updateExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), templateStorageType, nameJob), FileSetManager.COMPRESSION_NONE);
		    						else
		    							FileSetManager.addExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), templateStorageType, nameJob), FileSetManager.COMPRESSION_NONE);
	    						}
	    						
	    						if (fileset == null) {
	    							if (clientName.equals("airback-fd"))
	    								throw new Exception(getLanguageMessage("advanced.group.jobs.backup.exception.inventory"));
	    							else
	    								throw new Exception(getLanguageMessage("advanced.group.jobs.backup.exception.fileset"));
	    						}
	    							
	    						jm.setJob(nameJob, clientName, request.getParameter(nameStep+"level"), schedule, fileset, storage, request.getParameter(nameStep+"poolDefault"), null, null, null, null, nextJob, verifyPreviousJob, maxStartDelay, maxRunTime, maxWaitTime, spoolData, enabled, priority, 0, bandwith, accurate, false, 0, 0);
	    						
	    						if (clientName.equals("airback-fd") && request.getParameter(nameStep+"storage_inventory") != null && !request.getParameter(nameStep+"storage_inventory").isEmpty()) {
	    							RemoteStorageManager.generateBackupRemoteScripts(nameJob, storageInventory, templateStorageType, iscsim, jm);
	    						}
	    						
	    						
	    					} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP) || typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
	    						if (request.getParameter(nameStep+"client") == null || request.getParameter(nameStep+"client").isEmpty())
	    							throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.client")+" :"+nameStep);
	    						
	    						String clientName = cm.getClientName(Integer.parseInt(request.getParameter(nameStep+"client")));
	    						configStep.setClient(clientName);
	    						
	    						ScriptProcessManager sp = new ScriptProcessManager();
	    						Map<String, Object> scriptProcess = sp.getScript(dataStep);
	    						nameJob = GroupJobManager.getGroupJobNameJob(groupJobName, order, clientName, nameStep);
	    						
	    						jm.setEmptyJobForScripts(nameJob, clientName , nextJob, null, schedule, storage);
	    						
	    						Map<String, String> _variableValues=new HashMap<String, String>();
								if (scriptProcess!=null && scriptProcess.get("variables")!=null){
									List<Map<String, String>> _variables= (List<Map<String, String>>) scriptProcess.get("variables");
									List<VariableValueRs> vars = new ArrayList<VariableValueRs>();
									for (Map<String, String> _variable: _variables){
										if (request.getParameter(nameStep+"var"+_variable.get("name"))!=null && !request.getParameter(nameStep+"var"+_variable.get("name")).isEmpty()){
											_variableValues.put(_variable.get("name"),request.getParameter(nameStep+"var"+_variable.get("name")));
											VariableValueRs var = new VariableValueRs();
											var.setName(_variable.get("name"));
											var.setValue(request.getParameter(nameStep+"var"+_variable.get("name")));
											vars.add(var);
										} else{
											_variableValues.put(_variable.get("name"),"");
										}
									}
									if (!vars.isEmpty())
										configStep.setVariables(vars);
								}
								List<String> _scriptsOrder=new ArrayList<String>();
								TreeMap<String,Map<String, String>> _scriptsContent=new TreeMap<String, Map<String, String>>();
								if (scriptProcess!=null && scriptProcess.get("scripts")!=null){
									List<Map<String, String>> _scripts= (List<Map<String, String>>) scriptProcess.get("scripts");
									for (Map<String, String> _script: _scripts){
											Map<String, String> objScript = new HashMap<String, String>();
											objScript.put("order", _script.get("order"));
											objScript.put("script", ScriptProcessManager.getSmartContent(_script.get("content"), _variableValues));
											objScript.put("shell", _script.get("shell"));
											_scriptsContent.put(_script.get("order"),objScript);										
											_scriptsOrder.add(_script.get("order"));
									}
								}
								Collections.sort(_scriptsOrder);
								
								boolean before = false;
								if (scriptProcess.get("type") != null && Integer.valueOf((String) scriptProcess.get("type")) == ScriptProcessManager.BEFORE_EXECUTION)
									before = true;
								
								boolean abortOnError = false;
								if (scriptProcess.get("abortType") != null && Boolean.valueOf((String) scriptProcess.get("abortType")) == true)
									abortOnError = true;
								
	    						ScriptProcessManager.generateScriptsJob(nameJob, (String) scriptProcess.get("name"), jm, _scriptsContent, abortOnError, before, true, true, _variableValues);
	    					}
	    					
	    					if (inventoryStep != null && !inventoryStep.isEmpty()) {
	    						configStep.setStorageInventory(inventoryStep);
	    					}
	    					
	    					if (nameJob != null) {
	    						Map<String, Object> job = new HashMap<String, Object>();
	    						job.put("name", nameJob);
	    						job.put("order", order);
	    						job.put("typeStep", typeStep);
	    						job.put("step", nameStep);
	    						if (inventoryStep != null && !inventoryStep.isEmpty())
	    							job.put("inventory", inventoryStep);
	    						jobs.put(order, job);
	    					}
	    					configSteps.add(configStep);
	    				}
	    				
	    				//if (GroupJobManager.existsGroupJob(groupJobName))
	    				//	GroupJobManager.removeGroupJob(groupJobName, true, jm, _c);
	    				GroupJobManager.saveGroupJob(groupJobName, GroupJobManager.TYPE_TEMPLATEJOB, request.getParameter("nameTemplateJob"), jobs, request.getParameter("schedule"), request.getParameter("storage"));
	    				GroupJobManager.saveGroupJobConfig(groupJobName, request.getParameter("schedule"), request.getParameter("storage"), request.getParameter("nameTemplateJob"), configSteps);
	    				
	    				if (templateJob.get("groups_status") == null || templateJob.get("groups_status").equals(TemplateJobManager.GROUPS_STATUS_NO_GROUPS))
	    					TemplateJobManager.writeTemplateJobXml((String) templateJob.get("name"), (Map<Integer, Map<String, String>>)templateJob.get("steps"), TemplateJobManager.GROUPS_STATUS_ALL_UPDATED);
	    				
	    				if (request.getParameter("edit") != null && request.getParameter("edit").equals("true"))
	    					writeDocumentResponse(getLanguageMessage("advanced.groupjob.stored"), AdvancedGroupJob.baseUrl);
	    				else
	    					writeDocumentResponse(getLanguageMessage("advanced.groupjob.stored"), baseUrl);
    				} catch (Exception ex) {
						// Si se produce algún fallo, borramos todos los jobs creados
    					for (Integer order : steps.keySet()) {
							try {
    							String nameJob = null;
    							Map<String, String> step = steps.get(order);
    							String nameStep = step.get("name");
    	    					String typeStep = step.get("type");
    	    					if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
    	    						nameJob = GroupJobManager.getGroupJobNameJob(groupJobName, order, "airback-fd", nameStep);
    	    					} else {
    	    						String clientName = cm.getClientName(Integer.parseInt(request.getParameter(nameStep+"client")));
    	    						nameJob = GroupJobManager.getGroupJobNameJob(groupJobName, order, clientName, nameStep);
    	    					}
    	    					
    	    					GroupJobManager.removeHiddenFileSet(nameJob, jm);
    	    					jm.removeJob(nameJob);
	    					} catch (Exception ex2) {}
						}
    					throw new Exception(ex.getMessage());
    				} finally {
    					try {
	    					if (BackupOperator.isBlock_reload())
	    						BackupOperator.setBlock_reload(false);
	    					BackupOperator.reload();
    					} catch (Exception ex) {
	    					// Si se produce algún fallo, borramos todos los jobs creados
	    					for (Integer order : steps.keySet()) {
								try {
	    							String nameJob = null;
	    							Map<String, String> step = steps.get(order);
	    							String nameStep = step.get("name");
	    	    					String typeStep = step.get("type");
	    	    					if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
	    	    						nameJob = GroupJobManager.getGroupJobNameJob(groupJobName, order, "airback-fd", nameStep);
	    	    					} else {
	    	    						String clientName = cm.getClientName(Integer.parseInt(request.getParameter(nameStep+"client")));
	    	    						nameJob = GroupJobManager.getGroupJobNameJob(groupJobName, order, clientName, nameStep);
	    	    					}
	    	    					
	    	    					GroupJobManager.removeHiddenFileSet(nameJob, jm);
	    	    					jm.removeJob(nameJob);
		    					} catch (Exception ex2) {}
							}
	    					throw new Exception(ex.getMessage());
	    				}
    				}
    			} break;
    			
    			case STORE_CLIENT: {
    				
    				String step = request.getParameter("step");
    				
    				if (step == null || step.equals(""))
    					throw new Exception (getLanguageMessage("advanced.client.error.step.undefined"));
	    			
	    			if(!this.securityManager.checkCategory(request.getParameter("category"))) {
		    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
	    			}
	    			
	    			String clientName = null;
	    			if (request.getParameter("name") == null || request.getParameter("name").trim().isEmpty()) {
	    				throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.name"));
	    			}
	    			clientName = request.getParameter("name");
	    			
	    			if (clientName.equals("airback-fd") && !this.securityManager.isAdministrator())
	    				throw new Exception(getLanguageMessage("common.message.no_privilegios"));
	    			
	    			String os = null;
	    			if (request.getParameter("os") != null && !request.getParameter("os").isEmpty()) {
	    				os = request.getParameter("os");
	    			}
	    			
	    			if (os == null && !clientName.equals("airback-fd")) {
	    				throw new Exception(getLanguageMessage("backup.clients.os"));
	    			}
	    			
	    			String password = null;
	    			if (request.getParameter("password") != null && !request.getParameter("password").trim().isEmpty()) {
	    				password = request.getParameter("password").trim();
	    			}
	    			
	    			Integer fileRetention = null;
	    			if (request.getParameter("fileRetention") != null && !request.getParameter("fileRetention").trim().isEmpty()) {
	    				try {
	    					fileRetention = Integer.parseInt(request.getParameter("fileRetention").trim());
	    				} catch (Exception ex) {
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.fileRetention"));
	    				}
	    			}
	    			
	    			Integer jobRetention = null;
	    			if (request.getParameter("jobRetention") != null && !request.getParameter("jobRetention").trim().isEmpty()) {
	    				try {
	    					jobRetention = Integer.parseInt(request.getParameter("jobRetention").trim());
	    				} catch (Exception ex) {
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.jobRetention"));
	    				}
	    			}
	    			
	    			Integer port = null;
	    			if (request.getParameter("port") != null && !request.getParameter("port").trim().isEmpty()) {
	    				try {
							port = Integer.parseInt(request.getParameter("port").trim());
						} catch(NumberFormatException _ex) {
							throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.port"));
						}
	    			}
	    			
	    			StringBuilder _address = new StringBuilder();
                    if(request.getParameter("ip1") != null &&
                    		request.getParameter("ip2") != null && 
                    		request.getParameter("ip3") != null &&
                    		request.getParameter("ip4") != null &&
                    		!request.getParameter("ip1").isEmpty() &&
                    		!request.getParameter("ip2").isEmpty() &&
                    		!request.getParameter("ip3").isEmpty() &&
                    		!request.getParameter("ip4").isEmpty()) {
                    	_address.append(request.getParameter("ip1"));
	                    _address.append(".");
	                    _address.append(request.getParameter("ip2"));
	                    _address.append(".");
	                    _address.append(request.getParameter("ip3"));
	                    _address.append(".");
	                    _address.append(request.getParameter("ip4"));
	                    if (!NetworkManager.isValidAddress(_address.toString())) {
	                    	throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.address"));
	                    }
                    } else if(!clientName.equals("airback-fd") && request.getParameter("dns_name") != null && !request.getParameter("dns_name").isEmpty()) {
                    	_address.append(request.getParameter("dns_name"));
                    }
					
	    			if (clientName.equals("airback-fd")) {
	    				if (fileRetention == null)
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.fileRetention"));
	    				if (jobRetention == null)
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.jobRetention"));
	    				cm.setLocalClient(fileRetention, request.getParameter("fileRetentionUnits"), jobRetention, request.getParameter("jobRetentionUnits"));
	    			} else {
	    				if (password == null || password.isEmpty())
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.password"));
	    				if (fileRetention == null)
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.fileRetention"));
	    				if (jobRetention == null)
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.jobRetention"));
	    				if (port == null)
	    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.port"));
	    				if (_address.toString().trim().isEmpty())
	                    	throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.address"));
	    				if (request.getParameter("autoDeploy") != null && request.getParameter("autoDeploy").equals("true")) {
	                    	if (request.getParameter("autoDeployUsername") != null && !request.getParameter("autoDeployUsername").isEmpty() &&
	                    			request.getParameter("autoDeployPassword") != null && !request.getParameter("autoDeployPassword").isEmpty() &&
	                    			request.getParameter("os") != null &&
	                    			_address.length() > 0) {
	                    		cm.deployClient(_address.toString(), request.getParameter("name"), request.getParameter("password"), request.getParameter("autoDeployUsername"), request.getParameter("autoDeployPassword"), request.getParameter("os"));
	                    	} else {
			    				throw new Exception(getLanguageMessage("common.message.no_deploy_params"));
	                    	}
	                    }
	    				cm.setClient(clientName, _address.toString(), port, os, password, request.getParameter("charset"), fileRetention, request.getParameter("fileRetentionUnits"), jobRetention, request.getParameter("jobRetentionUnits"), request.getParameterValues("categories"));
	    			}
					writeJSFuncResponse(getLanguageMessage("advanced.groupjob.client.stored"), "javascript:closeClientDialog"+request.getParameter("order")+"();");

    			} break;
	    	}
	    } catch (Exception _ex) {
	    	if (type != SEARCH_JSON && type != TEMPLATE_LIST) {
		    	if (type != STORE_CLIENT)
		    		writeDocumentError(_ex.getMessage());
		    	else	
		    		writeJSFuncResponse(_ex.getMessage(), "javascript:closeClientDialog"+request.getParameter("order")+"();");
	    	}
	    } finally {
	    	if (type != STORE_CLIENT && type != SELECT_CLIENTS && type != SEARCH_JSON && type != TEMPLATE_LIST)
	    		writeDocumentFooter();
	    }		
	}
	
	public static String getNextJob(HttpServletRequest request, Integer order, Map<Integer, Map<String, String>> steps, String groupJob, ClientManager cm) throws Exception{
		int nextOrder = order+1;
		String nextJob = null;
		Map<String, String> nextStep = steps.get(nextOrder);
		if (nextStep != null && !nextStep.isEmpty()) {
			String typeStep = nextStep.get("type");
			String nameStep = nextStep.get("name");
			if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
				nextJob = GroupJobManager.getGroupJobNameJob(groupJob, nextOrder, "airback-fd", nameStep);
			} else {
				if (request.getParameter(nameStep+"client") != null && !request.getParameter(nameStep+"client").isEmpty()) {
					String clientName = cm.getClientName(Integer.parseInt(request.getParameter(nameStep+"client")));
					nextJob = GroupJobManager.getGroupJobNameJob(groupJob, nextOrder, clientName, nameStep);
				}
			} 
		}
		return nextJob;
	}
	
	
 	public void printSectionHeader(PrintWriter _xhtml_out, String info) throws Exception {
		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/advanced_backup_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.backup"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(info);
		_xhtml_out.println("</div>");
 	}
 	
 	public void fillSelects(ClientManager cm) throws Exception {
 		selectTemplateJob = new TreeMap<String, String>();
 		List<Map<String, Object>> templateJobs = TemplateJobManager.listTemplateJobs();
 		if (templateJobs != null && templateJobs.size()>0) {
 			for (Map<String, Object> templateJob : templateJobs) {
 				String name = (String) templateJob.get("name");
 				selectTemplateJob.put(name, name);	
 			}
 		}

 	 	selectTypeStep = new TreeMap<String, String>();
 		selectTypeStep.put(StepManager.TYPE_STEP_ADVANCED_STORAGE, getLanguageMessage("advanced.step.type.advanced_storage"));
 		selectTypeStep.put(StepManager.TYPE_STEP_BACKUP, getLanguageMessage("advanced.step.type.backup"));
 		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_APP, getLanguageMessage("advanced.step.type.script_app"));
 		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_SYSTEM, getLanguageMessage("advanced.step.type.script_system"));
 		
 		msgTypeGroupJob = new TreeMap<String, String>();
 		msgTypeGroupJob.put(GroupJobManager.TYPE_TEMPLATEJOB, getLanguageMessage("advanced.groupjob.type.templatejob"));
 		msgTypeGroupJob.put(GroupJobManager.TYPE_MANUAL_SELECTION, getLanguageMessage("advanced.groupjob.type.manual"));
 		
 		List<String> clientNames = cm.getAllClientNames(null);
 		selectClients = new TreeMap<String, String>();
 		selectClients.put("", "--");
 		for (String name : clientNames) {
 			selectClients.put(name, name);
 		}
 		
 	}
 	
 	@SuppressWarnings("unchecked")
	public void printTemplateJobWizard(PrintWriter _xhtml_out, String nameGroupJob, String nameTemplateJob, Map<Integer, Map<String, String>> steps, Configuration conf) throws Exception {
 		GroupJobConfigRs groupConfiguration = GroupJobManager.getGroupJobConfiguration(nameGroupJob);
 		JobManager jm = new JobManager(this.sessionManager.getConfiguration());
 		
 		_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
 		
 		StorageManager _sm = new StorageManager(conf);
 		writeDocumentBack(null);
 		printSectionHeader(_xhtml_out, getLanguageMessage("advanced.groupjob.wizard.topinfo"));
 		
		_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" method=\"post\">");
		
		_xhtml_out.print("<input type=\"hidden\" name=\"type\" id=\"type\" value=\"" + STORE_GROUPJOB_BY_TEMPLATEJOB + "\"/>");
		_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + nameGroupJob + "\"/>");
		_xhtml_out.print("<input type=\"hidden\" name=\"nameTemplateJob\" value=\"" + nameTemplateJob + "\"/>");
		if (groupConfiguration != null)
			_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"true\"/>");
 		
 		_xhtml_out.println("<div class=\"window\" style=\"margin: 20px auto 10px;width: 83%;\">");
		_xhtml_out.println("<h2>");
		_xhtml_out.print(getLanguageMessage("advanced.groupjob.wizard.generalinfo"));
		_xhtml_out.print(HtmlFormUtils.saveHeaderButton("form", getLanguageMessage("common.message.save")));
        _xhtml_out.println("</h2>");
        _xhtml_out.println("<fieldset>");
        _xhtml_out.println("<div class=\"standard_form\">");
        _xhtml_out.print("<label>");
	    _xhtml_out.print(getLanguageMessage("advanced.groupjob.name"));
	    _xhtml_out.println(": </label>");
	    _xhtml_out.println("<div style=\"float:left;padding-top:5px;\">");
	    _xhtml_out.println(nameGroupJob);
	    _xhtml_out.println("</div>");
	    _xhtml_out.println("</div>");
	    _xhtml_out.println("<div class=\"clear\"/></div>");
	    _xhtml_out.println("<div class=\"standard_form\">");
	    _xhtml_out.print("<label>");
    	_xhtml_out.print(getLanguageMessage("advanced.groupjob.templateJobName"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<div style=\"float:left;padding-top:5px;\">");
    	_xhtml_out.print(nameTemplateJob);
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"storage\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_storage"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\"storage\">");
    	for(String storage : _sm.getAvailableStorages()) {
			_xhtml_out.print("<option value=\"");
			_xhtml_out.print(storage);
			_xhtml_out.print("\" ");
			if (groupConfiguration != null && groupConfiguration.getStorage() != null && groupConfiguration.getStorage().equals(storage))
				_xhtml_out.print(" selected=\"selected\" ");
			_xhtml_out.print(">");
			_xhtml_out.print(storage);
			_xhtml_out.println("</option>");
		}
		_xhtml_out.println("</select>");
		_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
        _xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"schedule\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.schedule"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\"schedule\">");
    	_xhtml_out.print("<option value=\"\">--</option>");
		for(String schedule : ScheduleManager.getScheduleNames()) {
			_xhtml_out.print("<option value=\"");
			_xhtml_out.print(schedule);
			_xhtml_out.print("\"");
			if (groupConfiguration != null && groupConfiguration.getSchedule() != null && groupConfiguration.getSchedule().equals(schedule))
				_xhtml_out.print(" selected=\"selected\" ");
			_xhtml_out.print(">");
			_xhtml_out.print(schedule);
			_xhtml_out.println("</option>");
		}
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("</div>");
 		
    	ScriptProcessManager sp = new ScriptProcessManager();
    	ClientManager cm = new ClientManager(conf);
    	PoolManager _pm = new PoolManager(conf);
    	List<String> pools = _pm.getPoolNames();
    	
    	_xhtml_out.println("<div id=\"accordionStep\" name=\"accordionStep\">");
    	StringBuilder sbFuncUpdateSelectClients = new StringBuilder();
    	sbFuncUpdateSelectClients.append("function updateSelectClients() {\n");
    	for (Integer order : steps.keySet()) {
    		Map<String, String> step = steps.get(order);
    		GroupStepRs configStep = null;
    		if (groupConfiguration != null && groupConfiguration.getStep(order) != null)
    			configStep = groupConfiguration.getStep(order);
    		
    		String nameStep = step.get("name");
    		String type = step.get("type");
    		String data = step.get("data");
    		
    		_xhtml_out.println("<div class=\"aggr-title\">");
    		_xhtml_out.println("<h2>");
    		_xhtml_out.print(getLanguageMessage("advanced.groupjob.wizard.step")+" "+order+": "+nameStep);
            _xhtml_out.println("</h2>");
            _xhtml_out.println("</div>"); 
            _xhtml_out.println("<div style=\"margin:0px;overflow:hidden;width:100%;padding:0px;border:0px;\">"); 
            _xhtml_out.println("<div class=\"window\" style=\"margin: -2px auto 2px;width: 83%;\">");
           /* _xhtml_out.println("<h2>");
            _xhtml_out.println(selectTypeStep.get(type));
            _xhtml_out.println("</h2>");*/
            
            _xhtml_out.println("<fieldset>");
            
    		if (type.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
    			Map<String, Object> remoteStorage = RemoteStorageManager.getRemoteStorage(data);
    			
    			if (remoteStorage != null && remoteStorage.get("name") != null) {
    				// Nombre de script
    				_xhtml_out.println("<div style=\"float: left;width: 250px;margin-bottom: 5px;margin-right: 5px;text-align: right;\">");  
    				_xhtml_out.print(getLanguageMessage("advanced.groupjob.remoteStorage.name"));
    				_xhtml_out.println(": </div>");
    				_xhtml_out.println("<div style=\"float: left;text-align:left;\">");
    				_xhtml_out.println((String) remoteStorage.get("name"));
    				_xhtml_out.println("</div>");
    				_xhtml_out.println("<div class=\"clear\"></div>\n");
    			}
    			
    			// StorageInventory
    			List<String> inventories = StorageInventoryManager.listStorageNamesByAdvanced((String) remoteStorage.get("name"));
    			String inventoryConfigured = null;
    			if (configStep != null && configStep.getStorageInventory() != null)
    				inventoryConfigured = configStep.getStorageInventory();
    			_xhtml_out.println(HtmlFormUtils.selectOptionSimple(nameStep+"storage_inventory", getLanguageMessage("advanced.groupjob.storageinventory"), inventoryConfigured, inventories, false));
    			
    			// Variables
    			Map<String, Map<String, String>> variables = (Map<String, Map<String, String>>) remoteStorage.get("variables");
    			variables = RemoteStorageManager.removeUnrelatedStepVariables(remoteStorage, variables, nameStep);
    			printVariablesForm(_xhtml_out, nameStep, variables, configStep);
    		} else if (type.equals(StepManager.TYPE_STEP_BACKUP)) {
    			sbFuncUpdateSelectClients.append(getJSUpdateClientSelect(nameStep, type, order));
    			
    			String configuredClient = null;
    			if (configStep != null && configStep.getClient() != null)
    				configuredClient = (String) configStep.getClient();
    			
       			// Datos cliente
    			printClientForm(_xhtml_out, nameStep, order, cm, true, true, configuredClient);
    			
    			Map<String, String> configuredJob = null;
    			if (configStep != null) {
    				String nameJob = GroupJobManager.getGroupJobNameJob(nameGroupJob, order, configuredClient, nameStep);
    				Map<String, String> job = jm.getProgrammedJob(nameJob);
    				if (job != null && !job.isEmpty())
    					configuredJob = job;
    			}
    			
    			// Datos backup
    			printBackupForm(_xhtml_out, nameStep, pools, configuredJob);
    			
    			// Asociar con storage remoto y storage inventory
    			Map<String, String> inventories = new HashMap<String, String>();
    			inventories.put("", "--");
    			List<String> inventoryNames = StorageInventoryManager.listStorageNames();
    			for (String name : inventoryNames) {
    				inventories.put(name, name);
    			}
    			String inventoryConfigured = null;
    			if (configStep != null && configStep.getStorageInventory() != null)
    				inventoryConfigured = configStep.getStorageInventory();
    			_xhtml_out.println(HtmlFormUtils.selectOptionSimple(nameStep+"storage_inventory", getLanguageMessage("advanced.groupjob.storageinventory"), inventoryConfigured, inventories, false));

    		} else if (type.equals(StepManager.TYPE_STEP_SCRIPT_APP)) {
    			sbFuncUpdateSelectClients.append(getJSUpdateClientSelect(nameStep, type, order));
    			
    			Map<String, Object> script = sp.getScript(data);
    			if (script != null && script.get("application") != null) {
    				// Datos applicacion
    				_xhtml_out.println("<div style=\"float: left;width: 250px;margin-bottom: 5px;margin-right: 5px;text-align: right;\">");  
    				_xhtml_out.print(getLanguageMessage("advanced.groupjob.script.app"));
    				_xhtml_out.println(": </div>");
    				_xhtml_out.println("<div style=\"float: left;text-align:left;\">");
    				_xhtml_out.println((String) script.get("application"));
    				_xhtml_out.println("</div>");
    				_xhtml_out.println("<div class=\"clear\"></div>\n");
    			}
    			
    			String configuredClient = null;
    			if (configStep != null && configStep.getClient() != null)
    				configuredClient = (String) configStep.getClient();
    			
       			// Datos cliente
    			printClientForm(_xhtml_out, nameStep, order, cm, false, false, configuredClient);
    			
    			// Variables
    			if (script != null && script.get("variables") != null) {
    				List <Map<String, Object>> variables = (List <Map<String, Object>>) script.get("variables");
    				printVariablesForm(_xhtml_out, nameStep, variables, configStep);
    			}
    		} else if (type.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
    			sbFuncUpdateSelectClients.append(getJSUpdateClientSelect(nameStep, type, order));
    			
    			Map<String, Object> script = sp.getScript(data);
    			if (script != null && script.get("name") != null) {
    				// Nombre de script
    				_xhtml_out.println("<div style=\"float: left;width: 250px;margin-bottom: 5px;margin-right: 5px;text-align: right;\">");  
    				_xhtml_out.print(getLanguageMessage("advanced.groupjob.script.name"));
    				_xhtml_out.println(": </div>");
    				_xhtml_out.println("<div style=\"float: left;text-align:left;\">");
    				_xhtml_out.println((String) script.get("name"));
    				_xhtml_out.println("</div>");
    				_xhtml_out.println("<div class=\"clear\"></div>\n");
    			}
    			
    			if (script != null && script.get("system") != null) {
    				// Datos applicacion
    				_xhtml_out.println("<div style=\"float: left;width: 250px;margin-bottom: 5px;margin-right: 5px;text-align: right;\">");  
    				_xhtml_out.print(getLanguageMessage("advanced.groupjob.script.system"));
    				_xhtml_out.println(": </div>");
    				_xhtml_out.println("<div style=\"float: left;text-align:left;\">");
    				_xhtml_out.println((String) script.get("system"));
    				_xhtml_out.println("</div>");
    				_xhtml_out.println("<div class=\"clear\"></div>\n");
    			}
    			
    			String configuredClient = null;
    			if (configStep != null && configStep.getClient() != null)
    				configuredClient = (String) configStep.getClient();
    			
       			// Datos cliente
    			printClientForm(_xhtml_out, nameStep, order, cm, false, false, configuredClient);
    			
    			// Variables
    			if (script != null && script.get("variables") != null) {
    				List <Map<String, Object>> variables = (List <Map<String, Object>>) script.get("variables");
    				printVariablesForm(_xhtml_out, nameStep, variables, configStep);
    			}
    		}
    		_xhtml_out.println("</fieldset>");
    		_xhtml_out.println("<div class=\"clear\"></div>");
        	_xhtml_out.print("</div>");
        	_xhtml_out.print("</div>");
    	}
    	sbFuncUpdateSelectClients.append("}\n");
    	_xhtml_out.println("</div>"); 
      	_xhtml_out.print("</form>");
    	
    	pageJS += "$( '#accordionStep' ).accordion({collapsible:true, heightStyle: 'content'});\n";
    	pageJS +="$('#accordionStep div a').click(function() {\n";
    	pageJS +="window.location = $(this).attr('href');\n";
    	pageJS +="return false;\n";
    	pageJS +="});\n";
    	
    	pageJSFuncs += getJSDisableFileset();
    	pageJSFuncs += sbFuncUpdateSelectClients.toString();
 	}
 	
 	public void printClientForm(PrintWriter _xhtml_out, String step, Integer order, ClientManager cm, boolean onChange, boolean withLocal, String configuredClient) throws Exception {
 		printSelectClient(_xhtml_out, step, order, cm, onChange, withLocal, configuredClient);
		
		CategoryManager cam = new CategoryManager();
		pageJSFuncs+=getJSNewClientDialog(step, order);
     	_xhtml_out.println("<div id=\"newClientDialog"+step+"\" name=\"newClientDialog"+step+"\">");
     	printFormNewClient(_xhtml_out, cam, step, order);
     	_xhtml_out.println("</div>");
     	
      	pageJS+="$( '#newClientDialog"+step+"' ).dialog({\n";
     	pageJS+="   autoOpen: false,\n";
     	pageJS+="   height: 'auto',\n";
     	pageJS+="	modal: true,\n";
     	pageJS+="   width: 1000,\n";
     	pageJS+="   hide: 'fade'\n";
     	pageJS+="});\n";
 	}
 	
 	
 	public void printSelectClient(PrintWriter _xhtml_out, String step, Integer order, ClientManager cm, boolean onChange, boolean withLocal, String configuredClient) throws Exception {
 		_xhtml_out.println("<div name=\""+step+"divClient\" id=\""+step+"divClient\" >");
 		_xhtml_out.println("<div class=\"standard_form\">");
 		_xhtml_out.print("<label for=\""+step+"client\">");
    	_xhtml_out.print(getLanguageMessage("advanced.groupjob.wizard.client"));
    	_xhtml_out.println(": </label>");
    	String sOnChange = "";
    	if (onChange)
    		sOnChange="onChange=\"disableFileset('"+step+"');\"";
    	
        _xhtml_out.println("<select class=\"form_select\" name=\""+step+"client\" id=\""+step+"client\" "+sOnChange+" >");
        boolean init=false;
		for(Map<String, String> client : cm.getAllClients(null, null, false)) {
			if (withLocal || !client.get("name").equals("airback-fd")) {
				_xhtml_out.print("<option value=\"");
				_xhtml_out.print(client.get("id"));
				_xhtml_out.print("\"");
				if ( (configuredClient == null || configuredClient.isEmpty() && !init) || (configuredClient != null && configuredClient.equals(client.get("name"))) ) {
					_xhtml_out.print(" selected=\"selected\" ");
					init=true;
				}
				_xhtml_out.print(">");
				_xhtml_out.print(client.get("name"));
				_xhtml_out.println("</option>");
			}
			
		}
		_xhtml_out.print("</select> &nbsp;");
		_xhtml_out.print("<a href=\"javascript:newClient"+order+"();\"><img src=\"/images/add_16.png\" alt=\""+getLanguageMessage("common.message.add")+"\" title=\""+getLanguageMessage("common.message.add")+"\" /></a>\n");
		_xhtml_out.println("</div>");
		_xhtml_out.println("<div class=\"clear\"/></div>");
		_xhtml_out.println("</div>");
		
		if (onChange)
			pageJS+=" disableFileset('"+step+"');\n";
 	}
 	
 	public void printBackupForm(PrintWriter _xhtml_out, String step, List<String> pools, Map<String, String> job) throws Exception {
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\""+step+"level\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_level"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\""+step+"level\">");
    	_xhtml_out.print("<option value=\"Full\">");
    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_full"));
    	_xhtml_out.println("</option>");
    	_xhtml_out.println("<option value=\"Incremental\" default>");
    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_incremental"));
    	_xhtml_out.println("</option>");
		_xhtml_out.println("<option value=\"Differential\">");
    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_differential"));
    	_xhtml_out.println("</option>");
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
		_xhtml_out.println("<div name=\""+step+"divFileset\" id=\""+step+"divFileset\" class=\"standard_form\">");
		_xhtml_out.print("<label for=\""+step+"fileset\">");
		_xhtml_out.print(getLanguageMessage("backup.jobs.fileset"));
		_xhtml_out.println(": </label>");
		String configuredFileSet=null;
		if (job != null && job.get("fileset") != null)
			configuredFileSet = job.get("fileset");
		_xhtml_out.println("<select class=\"form_select\" name=\""+step+"fileset\" id=\""+step+"fileset\"");
		if (configuredFileSet!=null && configuredFileSet.contains("---hidden")){
    		_xhtml_out.println(" disabled=\"disabled\" ");
    	}
		_xhtml_out.println(" >");
		for(String fileset: FileSetManager.getAllFileSetNames()) {
			_xhtml_out.print("<option value=\"");
			_xhtml_out.print(fileset);
			_xhtml_out.print("\" ");
			if (configuredFileSet != null && configuredFileSet.equals(fileset))
				_xhtml_out.print(" selected=\"selected\" ");	
			_xhtml_out.print(">");
			_xhtml_out.print(fileset);
			_xhtml_out.println("</option>");
		}
		_xhtml_out.println("</select>");
		_xhtml_out.println("</div>");
		_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\""+step+"poolDefault\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_pool"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\""+step+"poolDefault\">");
    	String configuredPool=null;
		if (job != null && job.get("pool") != null)
			configuredPool = job.get("pool");
    	for(String pool : pools) {
			_xhtml_out.print("<option value=\"");
			_xhtml_out.print(pool);
			_xhtml_out.print("\" ");
			if (configuredPool != null && configuredPool.equals(pool))
				_xhtml_out.print(" selected=\"selected\" ");	
			_xhtml_out.print(">");;
			_xhtml_out.print(pool);
			_xhtml_out.println("</option>");
		}
		_xhtml_out.println("</select>");
		_xhtml_out.println("</div>");
		_xhtml_out.println("<div class=\"clear\"/></div>");
		_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\""+step+"maxStartDelay\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.delay_time"));
    	_xhtml_out.println(": </label>");
    	String maxStartDelay = "10";
    	if (job != null && job.get("max-start-delay") != null)
    		maxStartDelay=job.get("max-start-delay");
    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\""+step+"maxStartDelay\" value=\""+maxStartDelay+"\"/> ");
    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\""+step+"maxRunTime\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.run_time"));
    	_xhtml_out.println(": </label>");
    	String maxRunTime = "72";
    	if (job != null && job.get("max-run-time") != null)
    		maxRunTime=job.get("max-run-time");
    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\""+step+"maxRunTime\" value=\""+maxRunTime+"\"/> ");
    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\""+step+"maxWaitTime\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.wait_time"));
    	_xhtml_out.println(": </label>");
    	String maxWaitTime = "10";
    	if (job != null && job.get("max-wait-time") != null)
    		maxWaitTime=job.get("max-wait-time");
    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\""+step+"maxWaitTime\" value=\""+maxWaitTime+"\"/> ");
    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\""+step+"spoolData\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.disk_spooling"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\""+step+"spoolData\">");
		_xhtml_out.print("<option value=\"yes\" ");
		if(job != null && job.get("spooldata") != null && "yes".contains(job.get("spooldata"))) {
			_xhtml_out.print(" selected=\"selected\" ");
		}
		_xhtml_out.print(">");
		_xhtml_out.print(getLanguageMessage("common.message.yes"));
		_xhtml_out.println("</option>");
		_xhtml_out.print("<option value=\"no\" ");
		if(job != null && job.get("spooldata") != null && "no".contains(job.get("spooldata"))) {
			_xhtml_out.print(" selected=\"selected\" ");
		}
		_xhtml_out.print(">");
		_xhtml_out.print(getLanguageMessage("common.message.no"));
		_xhtml_out.println("</option>");
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\""+step+"bandwith\">");
    	_xhtml_out.print(getLanguageMessage("backup.jobs.bandwith"));
    	_xhtml_out.println(": </label>");
    	String bandwidth = "";
    	if (job != null && job.get("bandwith") != null && !job.get("bandwith").isEmpty() )
    		bandwidth = job.get("bandwith");
    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\""+step+"bandwith\" value=\""+bandwidth+"\"/> Mb/s</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
 	}

 	
 	public void printVariablesForm(PrintWriter _xhtml_out, String step, List <Map<String, Object>> variables, GroupStepRs configStep) throws Exception {
 		if (variables != null && variables.size() > 0) {
 			for (Map<String, Object> var : variables) {
 				Object text = var.get("description");
 				if (text == null || ((String)text).trim().isEmpty())
 					text = var.get("name");
 				boolean password = false;
 				if (var.get("password") != null && ((String)var.get("password")).equals("true"))
 					password = true;
 				String value = null;
 				if (configStep != null && configStep.getVar((String) var.get("name")) != null)
 					value = configStep.getVar((String) var.get("name")).getValue();
 				_xhtml_out.print(HtmlFormUtils.inputTextLabelTitleSimple(step+"var"+var.get("name"), (String) text, value, (String) var.get("name"), false, password));
 			}
	 	} else {
	        _xhtml_out.println("<div class=\"subinfo\">");
			_xhtml_out.println(getLanguageMessage("advanced.groupjob.wizard.novariables"));
			_xhtml_out.println("</div>");
	    }
 	}
 	
 	public void printVariablesForm(PrintWriter _xhtml_out, String step, Map<String, Map<String, String>> variables, GroupStepRs configStep) throws Exception {
 		if (variables != null && variables.size() > 0) {
 			for (String varname : variables.keySet()) {
 				Map<String, String> var = variables.get(varname);
 				String text = var.get("description");
 				if (text == null || text.trim().isEmpty())
 					text = var.get("name");
 				boolean password = false;
 				if (var.get("password") != null && ((String)var.get("password")).equals("true"))
 					password = true;
 				String value = null;
 				if (configStep != null && configStep.getVar(var.get("name")) != null)
 					value = configStep.getVar(var.get("name")).getValue();
 				_xhtml_out.println(HtmlFormUtils.inputTextLabelTitleSimple(step+"var"+var.get("name"), text, value, (String) var.get("name"), false, password));
 			}
	 	} else {
	        _xhtml_out.println("<div class=\"subinfo\">");
			_xhtml_out.println(getLanguageMessage("advanced.groupjob.wizard.novariables"));
			_xhtml_out.println("</div>");
	    }
 	}
 	
	
	public String getJSUpdateClientSelect(String step, String type, Integer order) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("	$.ajax({\n");
	 	_sb.append("		url: '"+baseUrl+"?type="+SELECT_CLIENTS+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: { step: '"+step+"', typeStep : '"+type+"', order : '"+order+"'}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#"+step+"divClient').html(html);\n");
	 	_sb.append("	});\n");
		return _sb.toString();
	}
	
	public String getJSNewClientDialog(String step, Integer order) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function newClient"+order+"() {\n");
		_sb.append("	$( '#form"+step+"' ).css('display','');\n");
		_sb.append("	$( '#form"+step+"' ).find('input:text, input:password, input:file, select, textarea').val('');\n");
		_sb.append("	$( '#fileRetention"+step+"' ).val('3');\n");
		_sb.append("	$( '#fileRetentionUnits"+step+"' ).val('months');\n");
		_sb.append("	$( '#jobRetention"+step+"' ).val('3');\n");
		_sb.append("	$( '#port"+step+"' ).val('9102');\n");
		_sb.append("	$( '#jobRetentionUnits"+step+"' ).val('months');\n");
		_sb.append("	document.getElementById('autoDeployDiv"+step+"').setAttribute('style','display:none;');\n");
	 	_sb.append("	$( '#msg"+step+"' ).empty();\n");
	 	_sb.append("	$('#newClientDialog"+step+"').dialog( 'open' );\n");
	 	_sb.append("	if (document.getElementById) { // DOM3 = IE5, NS6\n");
	 	_sb.append("		document.getElementById('hidepage"+order+"').style.visibility = 'hidden';\n");
	 	_sb.append("	} else {\n");
	 	_sb.append("		if (document.layers) { // Netscape 4\n");
	 	_sb.append("			document.hidepage"+order+".visibility = 'hidden';\n");
	 	_sb.append("		} else { // IE 4\n");
	 	_sb.append("			document.all.hidepage"+order+".style.visibility = 'hidden';\n");
	 	_sb.append("		}\n");
	 	_sb.append("	}\n");
	 	_sb.append("	document.disabled = 'true';\n");
	 	_sb.append("}\n");
	 	return _sb.toString();
	}
	
	public String getJSDisableFileset() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function disableFileset(step) {\n");
		_sb.append("  if ($('#'+step+'client :selected').html() == 'airback-fd') {\n");
		_sb.append("  	$('#'+step+'fileset').attr('disabled', 'disabled');\n");
		_sb.append("  	$('#'+step+'fileset').attr('style', 'background-color:#E6E9EA;');\n");
		_sb.append("  	$('#'+step+'storage_inventory').removeAttr('disabled');\n");
		_sb.append("  	$('#'+step+'storage_inventory').removeAttr('style');\n");
		_sb.append("  } else {\n");
		_sb.append("  	$('#'+step+'fileset').removeAttr('disabled');\n");
		_sb.append("  	$('#'+step+'fileset').removeAttr('style');\n");
		_sb.append("  	$('#'+step+'storage_inventory').attr('disabled', 'disabled');\n");
		_sb.append("  	$('#'+step+'storage_inventory').attr('style', 'background-color:#E6E9EA;');\n");
		_sb.append("  }\n");
		_sb.append("}\n");
		return _sb.toString();
	}
	
	public String getJSSubmitFormClient(String step, Integer order) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function submitFormClient"+order+"() {\n");
		_sb.append("	if (document.getElementById) { // DOM3 = IE5, NS6\n");
		_sb.append("		document.getElementById('hidepage"+order+"').style.visibility = 'visible';\n");
		_sb.append("	} else {\n");
		_sb.append("		if (document.layers) { // Netscape 4\n");
		_sb.append("			document.hidepage"+order+".visibility = 'show';\n");
		_sb.append("		} else { // IE 4\n");
		_sb.append("			document.all.hidepage"+order+".style.visibility = 'visible';\n");
		_sb.append("		}\n");
		_sb.append("	}\n");
		_sb.append("$.ajax({\n");
	 	_sb.append("		url: '"+baseUrl+"?type="+STORE_CLIENT+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: {name : $('#name"+step+"').val(), categories : $('#categories"+step+"').val(), ip1 : $('#ip1"+step+"').val(), ip2 : $('#ip2"+step+"').val(), ip3 : $('#ip3"+step+"').val(), ip4 : $('#ip4"+step+"').val(), dns_name : $('#dns_name"+step+"').val(), password : $('#password"+step+"').val(), port : $('#port"+step+"').val(), os : $('#os"+step+"').val(), charset : $('#charset"+step+"').val(), fileRetention : $('#fileRetention"+step+"').val(), fileRetentionUnits : $('#fileRetentionUnits"+step+"').val(), jobRetention : $('#jobRetention"+step+"').val(), jobRetentionUnits : $('#jobRetentionUnits"+step+"').val(), autoDeploy : document.getElementById(\"autoDeploy"+step+"\").checked, autoDeployUsername : $('#autoDeployUsername"+step+"').val(), autoDeployPassword : $('#autoDeployPassword"+step+"').val(), step: $('#step"+step+"').val(), order: $('#order"+step+"').val(), username: $('#username"+step+"').val()}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$( '#form"+step+"' ).css('display','none');\n");
	 	_sb.append("		$( '#msg"+step+"' ).empty().append( html );\n");
	 	_sb.append("	});\n");
	 	_sb.append("}\n");
		_sb.append("function closeClientDialog"+order+"() {\n");
		_sb.append("	updateSelectClients();\n");
		_sb.append("	$( '#form"+step+"' ).find('input:text, input:password, input:file, select, textarea').val('');\n");
		_sb.append("	$( '#fileRetention"+step+"' ).val('3');\n");
		_sb.append("	$( '#port"+step+"' ).val('9102');\n");
		_sb.append("	$( '#fileRetentionUnits"+step+"' ).val('months');\n");
		_sb.append("	$( '#jobRetention"+step+"' ).val('3');\n");
		_sb.append("	$( '#jobRetentionUnits"+step+"' ).val('months');\n");
		_sb.append("	document.getElementById('autoDeployDiv"+step+"').setAttribute('style','display:none;');\n");
	 	_sb.append("	$( '#form"+step+"' ).css('display','none');\n");
	 	_sb.append("	$( '#msg"+step+"' ).empty();\n");
	 	_sb.append("	$('#newClientDialog"+step+"').dialog( 'close' );\n");
		_sb.append("}\n");
		return _sb.toString();
	}

	public void printFormNewClient(PrintWriter _xhtml_out, CategoryManager cm, String step, Integer order) throws Exception {
		_xhtml_out.println("<div name=\"form"+step+"\" id=\"form"+step+"\">");
	    _xhtml_out.println("<div id=\"hidepage"+order+"\" class=\"hidepage\" >");
	    _xhtml_out.println("<table");
	    _xhtml_out.println(" >");
	    _xhtml_out.println("<tr>");
	    _xhtml_out.println("<td valign=\"center\"><img width=\"60\"  height=\"60\"");
	    _xhtml_out.println(" src=\"/images/loading.gif\" /></td>");
	    _xhtml_out.println("</tr>");
	    _xhtml_out.println("<tr>");
	    _xhtml_out.println("<td><h1>"+getLanguageMessage("common.loading.waiting")+"</h1></td>");
	    _xhtml_out.println("</tr>");
	    _xhtml_out.println("</table>");
	    _xhtml_out.println("</div>");
		_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_CLIENT + "\"/>");
		_xhtml_out.println("<input type=\"hidden\" name=\"step"+step+"\" id=\"step"+step+"\" value=\"" + step + "\"/>");
		_xhtml_out.println("<input type=\"hidden\" name=\"order"+step+"\" id=\"order"+step+"\" value=\"" + order + "\"/>");
        _xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		_xhtml_out.print(getLanguageMessage("backup.clients.new_client"));
		_xhtml_out.print("<a href=\"javascript:if ($('#client"+order+"').validationEngine('validate')) submitFormClient"+order+"();\"><img src=\"/images/disk_16.png\" alt=\"");
    	_xhtml_out.print(getLanguageMessage("common.message.save"));
    	_xhtml_out.println("\"/></a>");
        _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" alt=\"");
    	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
    	_xhtml_out.println("\"/></a>");
        _xhtml_out.println("</h2>");
		String dns_name = "";
		String[] _address = new String[] { "", "", "", ""};
		_xhtml_out.println("<form action=\"/admin/AdvancedBackup\" name=\"client"+order+"\" id=\"client"+order+"\" method=\"post\">");
		_xhtml_out.println("<fieldset style=\"font-size:12px;\">");
        _xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"_name\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.name"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" ");
    	_xhtml_out.print("name=\"name"+step+"\" id=\"name"+step+"\" value=\"\" />");
    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"categories\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.category"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\"categories"+step+"\" id=\"categories"+step+"\">");
    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
    	for(String cat : cm.getCategoryNames()) {
			_xhtml_out.print("<option value=\"");
			_xhtml_out.print(cat);
			_xhtml_out.print("\"");
			_xhtml_out.print(">");
			_xhtml_out.print(cat);
			_xhtml_out.println("</option>");
		}
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"ip1r\">");
    	_xhtml_out.print(getLanguageMessage("common.network.address"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[groupRequired[ip],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip1"+step+"\" id=\"ip1"+step+"\" value=\"");
    	_xhtml_out.print(_address[0]);
    	_xhtml_out.print("\"/>");
        _xhtml_out.print(".");
        _xhtml_out.print("<input class=\"validate[condRequired[ip1"+step+"],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip2"+step+"\" id=\"ip2"+step+"\" value=\"");
    	_xhtml_out.print(_address[1]);
    	_xhtml_out.print("\"/>");
        _xhtml_out.print(".");
        _xhtml_out.print("<input class=\"validate[condRequired[ip2"+step+"],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip3"+step+"\" id=\"ip3"+step+"\" value=\"");
    	_xhtml_out.print(_address[2]);
    	_xhtml_out.print("\"/>");
        _xhtml_out.print(".");
        _xhtml_out.print("<input class=\"validate[condRequired[ip3"+step+"],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip4"+step+"\" id=\"ip4"+step+"\" value=\"");
    	_xhtml_out.print(_address[3]);
    	_xhtml_out.print("\"/>");
    	_xhtml_out.println(" <img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
    	_xhtml_out.print("<label for=\"dns_name\">");
    	_xhtml_out.print(getLanguageMessage("common.network.dns_name"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"form_text validate[groupRequired[ip]]\" type=\"text\" name=\"dns_name"+step+"\" id=\"dns_name"+step+"\" value=\"");
    	_xhtml_out.print(dns_name);
    	_xhtml_out.print("\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form vmware\"");
    	_xhtml_out.println(" style=\"display:none;\" ");
    	_xhtml_out.print(">");
    	_xhtml_out.print("<label for=\"username\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.username"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" name=\"username"+step+"\" id=\"username"+step+"\" value=\"");
    	_xhtml_out.println("\"/>");
    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"password\">");
    	_xhtml_out.print(getLanguageMessage("common.login.password"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"password\" name=\"password"+step+"\" id=\"password"+step+"\" value=\"");
    	_xhtml_out.println("\"/>");
    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
    	_xhtml_out.print("<label for=\"port\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.port"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"port"+step+"\" id=\"port"+step+"\" value=\"");
    	_xhtml_out.print("9102");
    	_xhtml_out.print("\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"os\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.os"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\"os"+step+"\" id=\"os"+step+"\" onchange=\"checkOS"+order+"()\">");
    	for (String _osKey : ClientManager.getClientSupportedSOs().keySet()) {
    			_xhtml_out.println("<option value=\""+_osKey+"\"");
	    		_xhtml_out.println(">"+ClientManager.getClientSupportedSOs().get(_osKey)+"</option>");
    	}
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
    	_xhtml_out.print("<label for=\"charset\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.charset"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<select class=\"form_select\" name=\"charset"+step+"\" id=\"charset"+step+"\">");
    	_xhtml_out.print("<option value=\"UTF-8\"");
		_xhtml_out.print(" selected=\"selected\"");
		_xhtml_out.print(">UTF-8/Unicode</option>");
		_xhtml_out.print("<option value=\"ISO-8859-1\"");
		_xhtml_out.println(">ISO-8859-1</option>");
		_xhtml_out.print("<option value=\"ISO-8859-15\"");
		_xhtml_out.println(">ISO-8859-15</option>");
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
    	_xhtml_out.print("<label for=\"fileRetention\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.file_retention"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"fileRetention"+step+"\" id=\"fileRetention"+step+"\" value=\"");
    	_xhtml_out.print("3");
    	_xhtml_out.println("\"/>");
		_xhtml_out.println("<select class=\"form_select\" name=\"fileRetentionUnits"+step+"\" id=\"fileRetentionUnits"+step+"\">");
		_xhtml_out.print("<option value=\"days\"");
		_xhtml_out.print("\">");
		_xhtml_out.print(getLanguageMessage("common.message.days"));
		_xhtml_out.println("</option>");
		_xhtml_out.print("<option value=\"weeks\"");
		_xhtml_out.print("\">");
		_xhtml_out.print(getLanguageMessage("common.message.weeks"));
		_xhtml_out.println("</option>");
		_xhtml_out.print("<option value=\"months\" ");
		_xhtml_out.print(" selected=\"selected\"");
		_xhtml_out.print("\">");
		_xhtml_out.print(getLanguageMessage("common.message.months"));
		_xhtml_out.println("</option>");
		_xhtml_out.print("<option value=\"years\"");
		_xhtml_out.print("\">");
		_xhtml_out.print(getLanguageMessage("common.message.years"));
		_xhtml_out.println("</option>");
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
    	_xhtml_out.print("<label for=\"jobRetention\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.job_retention"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"jobRetention"+step+"\" id=\"jobRetention"+step+"\" value=\"");
    	_xhtml_out.print("3");
    	_xhtml_out.println("\"/>");
		_xhtml_out.println("<select class=\"form_select\" name=\"jobRetentionUnits"+step+"\" id=\"jobRetentionUnits"+step+"\">");
		_xhtml_out.print("<option value=\"days\"");
		_xhtml_out.print(">");
		_xhtml_out.print(getLanguageMessage("common.message.days"));
		_xhtml_out.println("</option>");
		_xhtml_out.print("<option value=\"weeks\"");
		_xhtml_out.print(">");
		_xhtml_out.print(getLanguageMessage("common.message.weeks"));
		_xhtml_out.println("</option>");
		_xhtml_out.print("<option value=\"months\" ");
		_xhtml_out.print(" selected=\"selected\"");
		_xhtml_out.print("\">");
		_xhtml_out.print(getLanguageMessage("common.message.months"));
		_xhtml_out.println("</option>");
		_xhtml_out.print("<option value=\"years\"");
		_xhtml_out.print(">");
		_xhtml_out.print(getLanguageMessage("common.message.years"));
		_xhtml_out.println("</option>");
		_xhtml_out.println("</select>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\" id=\"autoDeployDiv"+step+"\" ");
    	_xhtml_out.println("style=\"display:none;\"");
    	_xhtml_out.print(">");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"autoDeploy"+step+"\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.autoDeploy"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"form_text\" type=\"checkbox\" name=\"autoDeploy"+step+"\" id=\"autoDeploy"+step+"\" onClick=\"checkAutoDeploy"+order+"();\" />");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"username"+step+"\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.clientUsername"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"text\" name=\"autoDeployUsername"+step+"\" id=\"autoDeployUsername"+step+"\" disabled=\"disabled\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"password"+step+"\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.clientPassword"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"password\" name=\"autoDeployPassword"+step+"\" id=\"autoDeployPassword"+step+"\" disabled=\"disabled\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("</fieldset>");
        _xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("</form>");
    	
    	_xhtml_out.println("<div id=\"downloadAgent"+step+"\" ");
    	_xhtml_out.println("style=\"display:none;\"");
    	_xhtml_out.println(">");
    	_xhtml_out.print("<label for=\"agent_down\">");
    	_xhtml_out.print(getLanguageMessage("backup.clients.downloadAgent"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("1.<a name=\"agent_down"+step+"\" id =\"agent_down"+step+"\" href=\"javascript:urlDownload2"+order+"();\">");
    	_xhtml_out.println("<img alt=\"Download common\" title=\"Download common\" src=\"/images/arrow_down_32.png\">");
    	_xhtml_out.println("</a>");
    	_xhtml_out.println("2.<a name=\"agent_down"+step+"\" id =\"agent_down"+step+"\" href=\"javascript:urlDownload"+order+"();\">");
    	_xhtml_out.println("<img alt=\"Download package\" title=\"Download package\" src=\"/images/arrow_down_32.png\">");
    	_xhtml_out.println("</a>");
    	_xhtml_out.println("<div class=\"clear\" style=\"height:15px;\"/></div>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("</div>");
    	
    	_xhtml_out.println("<div name=\"msg"+step+"\" id=\"msg"+step+"\">&nbsp;</div>");
		
    	pageJSFuncs+="	function checkAutoDeploy"+order+"()\n";
    	pageJSFuncs+="	{\n";
    	pageJSFuncs+="		var autoDeploy = document.getElementById(\"autoDeploy"+step+"\");\n";
    	pageJSFuncs+="		var username = document.getElementById(\"autoDeployUsername"+step+"\");\n";
    	pageJSFuncs+="		var password = document.getElementById(\"autoDeployPassword"+step+"\");\n";
    	pageJSFuncs+="		if (autoDeploy.checked)\n";
    	pageJSFuncs+="		{\n";
    	pageJSFuncs+="			username.disabled=false;\n";
    	pageJSFuncs+="			password.disabled=false;\n";
    	pageJSFuncs+="		}\n";
    	pageJSFuncs+="		else\n";
    	pageJSFuncs+="		{\n";
    	pageJSFuncs+="			username.disabled=true;\n";
    	pageJSFuncs+="			password.disabled=true;\n";
    	pageJSFuncs+="		}\n";
    	pageJSFuncs+="	}\n";
    	pageJSFuncs+="	function checkOS"+order+"()\n";
    	pageJSFuncs+="	{\n";
    	pageJSFuncs+="		var so = document.getElementById(\"os"+step+"\");\n";
	    pageJSFuncs+="		var downloadAgent = document.getElementById(\"downloadAgent"+step+"\");\n";
	    pageJSFuncs+="		var autoDeploy = document.getElementById(\"autoDeploy"+step+"\");\n";
	    pageJSFuncs+="		var username = document.getElementById(\"autoDeployUsername"+step+"\");\n";
    	pageJSFuncs+="		var password = document.getElementById(\"autoDeployPassword"+step+"\");\n";
		pageJSFuncs+="		if (so.value == 'mac' || so.value.indexOf('redhat') > -1 || so.value.indexOf('suse') > -1 || so.value.indexOf('solaris') > -1) \n";
		pageJSFuncs+="		{\n";
		pageJSFuncs+="			autoDeploy.disabled=true;\n";
		pageJSFuncs+="			username.disabled=true;\n";
    	pageJSFuncs+="			password.disabled=true;\n";
		pageJSFuncs+="			document.getElementById('downloadAgent"+step+"').removeAttribute('style');\n";
		pageJSFuncs+="			document.getElementById('downloadAgent"+step+"').setAttribute('style','margin-left:-250px;');\n";
		pageJSFuncs+="			document.getElementById('autoDeployDiv"+step+"').setAttribute('style','display:none;');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="		else if (so.value.indexOf('win') > -1 || so.value.indexOf('debian') > -1) \n";
		pageJSFuncs+="		{\n";
		pageJSFuncs+="			autoDeploy.disabled=false;\n";
		//pageJSFuncs+="			username.disabled=false;\n";
    	//pageJSFuncs+="			password.disabled=false;\n";
		pageJSFuncs+="			downloadAgent.disabled=true;\n";
		pageJSFuncs+="			document.getElementById('downloadAgent"+step+"').setAttribute('style','display:none;');\n";
		pageJSFuncs+="			document.getElementById('autoDeployDiv"+step+"').removeAttribute('style');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="		else\n";
		pageJSFuncs+="		{\n";
		pageJSFuncs+="			autoDeploy.disabled=true;\n";
		pageJSFuncs+="			username.disabled=true;\n";
		pageJSFuncs+="			password.disabled=true;\n";
		pageJSFuncs+="			document.getElementById('downloadAgent"+step+"').setAttribute('style','display:none;');\n";
		pageJSFuncs+="			document.getElementById('autoDeployDiv"+step+"').setAttribute('style','display:none;');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="		if (so.value == 'vmware') {\n";
		pageJSFuncs+="				$('.notvmware').css('display','none');\n";
		pageJSFuncs+="				$('.notvmware').attr('disabled', 'disabled');\n";
		pageJSFuncs+="				$('.vmware').css('display','');\n";
		pageJSFuncs+="				$('.vmware').removeAttr('disabled');\n";
		pageJSFuncs+="		} else {\n";
		pageJSFuncs+="				$('.notvmware').css('display','');\n";
		pageJSFuncs+="				$('.notvmware').removeAttr('disabled');\n";
		pageJSFuncs+="				$('.vmware').css('display','none');\n";
		pageJSFuncs+="				$('.vmware').attr('disabled', 'disabled');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="	}\n";
		pageJSFuncs+="	function urlDownload"+order+"() {\n";
		pageJSFuncs+="		os = document.getElementById('os"+step+"').value;\n";
		pageJSFuncs+="		document.location.href  = '/admin/BackupClients?type="+BackupClients.DOWNLOAD_AGENT+"&&os='+os;\n";
		pageJSFuncs+="	}\n";
		pageJSFuncs+="	function urlDownload2"+order+"() {\n";
		pageJSFuncs+="		os = document.getElementById('os"+step+"').value;\n";
		pageJSFuncs+="		document.location.href  = '/admin/BackupClients?type="+BackupClients.DOWNLOAD_AGENT_2+"&&os='+os;\n";
		pageJSFuncs+="	}\n";
		
		pageJSFuncs+=getJSSubmitFormClient(step, order);
	}
 	
 	private String getAutoCompleteJS(String divId, String tableGridId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("$( \"#"+divId+"\" ).autocomplete({\n");
 		sb.append("		source: \""+baseUrl+"?type="+SEARCH_JSON+"&entity="+divId+"\",\n");
 		sb.append("		minLength: 2,\n");
 		sb.append("		select: function( event, ui ) {\n");
 		sb.append("			if (ui.item) {\n");
		sb.append("				$.ajax({\n");
	 	sb.append("					url: '"+baseUrl+"?type="+TEMPLATE_LIST+"',\n");
	 	sb.append("					cache: false,\n");
	 	if (!divId.equals("all"))
	 		sb.append("					data: { element: ui.item.id, typeEntity : '"+divId+"'}\n");
	 	else
	 		sb.append("					data: { element: ui.item.id, typeEntity : ui.item.label.substring(ui.item.label.indexOf('[')+1,ui.item.label.indexOf(']'))}\n");
	 	sb.append("				}).done(function( html ) {\n");
	 	sb.append("					jQuery(\"#"+tableGridId+"\").jqGrid('clearGridData');\n");
	 	sb.append("					if (html != null && html != \"\") {\n");
	 	sb.append("						var mydata = jQuery.parseJSON(html);\n");
	 	sb.append("						if (mydata) {\n");
	 	sb.append("							for(var i=0;i<=mydata.length;i++) jQuery(\"#"+tableGridId+"\").jqGrid('addRowData',i+1,mydata[i]);\n");
	 	sb.append("						}\n");
	 	sb.append("					}\n");
	 	sb.append("				});\n");	
 		sb.append("			}\n");
 		sb.append("		}\n");
 		sb.append("	});\n");
 		return sb.toString();
 	}
 	
 	private String printJsonSearch(String entity, String term) {
 		StringBuilder sb = new StringBuilder();
 		List<String> searchList = new ArrayList<String>();
 		Map<String, String> totalList = new HashMap<String, String>();
 		try {
 			if (entity.equals("all")) {
 				SysAppsInventoryManager saim = new SysAppsInventoryManager();
	 			List<SystemRs> systems = saim.listSystem();
	 			if (systems != null && !systems.isEmpty()) {
	 				for (SystemRs sys : systems) {
	 					if (sys.getName().toLowerCase().contains(term)) {
	 						totalList.put(sys.getName(), "system");
	 					}
	 				}
	 			}
	 			
	 			List<ApplicationRs> applications = saim.listApps();
	 			if (applications != null && !applications.isEmpty()) {
	 				for (ApplicationRs app : applications) {
	 					if (app.getName().toLowerCase().contains(term)) {
	 						totalList.put(app.getName(), "application");
	 					}
	 				}
	 			}
	 			
	 			List<String> remoteStorages = RemoteStorageManager.listRemoteStorageNames();
	 			if (remoteStorages != null && !remoteStorages.isEmpty()) {
	 				for (String remote : remoteStorages) {
	 					if (remote.toLowerCase().contains(term))
	 						totalList.put(remote, "advancedstorage");
	 				}
	 			}
	 			
	 			ScriptProcessManager spm = new ScriptProcessManager();
	 			Map<String, Map<String, Object>> scripts = spm.listScript();
	 			if (scripts != null && !scripts.isEmpty()) {
	 				for (String nameScript : scripts.keySet()) {
	 					if (nameScript.toLowerCase().contains(term))
	 						totalList.put(nameScript, "script");
	 				}
	 			}
	 			
	 			List<String> nameSteps = StepManager.listStepNames();
	 			if (nameSteps != null && !nameSteps.isEmpty()) {
		 			for (String name : nameSteps) {
		 				if (name.toLowerCase().contains(term))
		 					totalList.put(name, "step");
		 			}
	 			}
	 			
	 			List<String> nameTemplates = TemplateJobManager.listTemplateJobNames();
	 			if (nameTemplates != null && !nameTemplates.isEmpty()) {
	 				for (String template : nameTemplates) {
	 					if (template.toLowerCase().contains(term)) {
	 						totalList.put(template, "template");
	 					}
	 				}
	 			}
 			} else if (entity.equals("system")) {
	 			SysAppsInventoryManager saim = new SysAppsInventoryManager();
	 			List<SystemRs> systems = saim.listSystem();
	 			if (systems != null && !systems.isEmpty()) {
	 				for (SystemRs sys : systems) {
	 					if (sys.getName().toLowerCase().contains(term)) {
	 						searchList.add(sys.getName());
	 					}
	 				}
	 			}
	 		} else if (entity.equals("application")) {
	 			SysAppsInventoryManager saim = new SysAppsInventoryManager();
	 			List<ApplicationRs> applications = saim.listApps();
	 			if (applications != null && !applications.isEmpty()) {
	 				for (ApplicationRs app : applications) {
	 					if (app.getName().toLowerCase().contains(term)) {
	 						searchList.add(app.getName());
	 					}
	 				}
	 			}
	 		} else if (entity.equals("advancedstorage")) {
	 			List<String> remoteStorages = RemoteStorageManager.listRemoteStorageNames();
	 			if (remoteStorages != null && !remoteStorages.isEmpty()) {
	 				for (String remote : remoteStorages) {
	 					if (remote.toLowerCase().contains(term))
	 						searchList.add(remote);
	 				}
	 			}
	 		} else if (entity.equals("script")) {
	 			ScriptProcessManager spm = new ScriptProcessManager();
	 			Map<String, Map<String, Object>> scripts = spm.listScript();
	 			if (scripts != null && !scripts.isEmpty()) {
	 				for (String nameScript : scripts.keySet()) {
	 					if (nameScript.toLowerCase().contains(term))
	 						searchList.add(nameScript);
	 				}
	 			}
	 		} else if (entity.equals("step")) {
	 			List<String> nameSteps = StepManager.listStepNames();
	 			if (nameSteps != null && !nameSteps.isEmpty()) {
		 			for (String name : nameSteps) {
		 				if (name.toLowerCase().contains(term))
		 					searchList.add(name);
		 			}
	 			}
	 		} else if (entity.equals("template")) {
	 			List<String> nameTemplates = TemplateJobManager.listTemplateJobNames();
	 			if (nameTemplates != null && !nameTemplates.isEmpty()) {
	 				for (String template : nameTemplates) {
	 					if (template.toLowerCase().contains(term)) {
	 						searchList.add(template);
	 					}
	 				}
	 			}
	 		}
	 		
	 		if (!searchList.isEmpty()) {
	 			sb.append("[");
	 			boolean first = true;
	 			for (String item : searchList) {
	 				if (!first)
	 					sb.append(",");
	 				else
	 					first=false;
	 				sb.append("{\"id\":\""+item+"\",\"label\":\""+item+"\",\"value\":\""+item+"\"}");
	 			}
	 			sb.append("]");
	 		} else if (!totalList.isEmpty()) {
	 			sb.append("[");
	 			boolean first = true;
	 			for (String item : totalList.keySet()) {
	 				if (!first)
	 					sb.append(",");
	 				else
	 					first=false;
	 				sb.append("{\"id\":\""+item+"\",\"label\":\""+item+" ["+totalList.get(item)+"]\",\"value\":\""+item+"\"}");
	 			}
	 			sb.append("]");
	 		}
	 		
 		} catch (Exception ex) {
 			logger.error("Error obteniendo datos para búsqueda en advanced backup de entidad {}", entity);
 		}
 		
 		return sb.toString();
 	}
 	
 	private String printTemplateList(String element, String typeEntity) {
 		StringBuilder sb = new StringBuilder();
 		HashSet<Map<String, Object>> listTemplates = new HashSet<Map<String, Object>>();
 		try {
 			if (typeEntity.equals("system")) {
 				ScriptProcessManager spm = new ScriptProcessManager();
 				List<String> listScripts = spm.getScriptProcessWithSystem(element);
 				if (listScripts != null && !listScripts.isEmpty()) {
 					for (String script : listScripts) {
 						List<Map<String, Object>> relatedTemplates = TemplateJobManager.getRelatedTemplateJobs(script, StepManager.TYPE_STEP_SCRIPT_SYSTEM);
 						if (relatedTemplates != null && !relatedTemplates.isEmpty())
 							listTemplates.addAll(relatedTemplates);
 					}
 				}
 			} else if (typeEntity.equals("application")) {
 				ScriptProcessManager spm = new ScriptProcessManager();
 				List<String> listScripts = spm.getScriptProcessWithApp(element);
 				if (listScripts != null && !listScripts.isEmpty()) {
 					for (String script : listScripts) {
 						List<Map<String, Object>> relatedTemplates = TemplateJobManager.getRelatedTemplateJobs(script, StepManager.TYPE_STEP_SCRIPT_APP);
 						if (relatedTemplates != null && !relatedTemplates.isEmpty())
 							listTemplates.addAll(relatedTemplates);
 					}
 				}
 			} else if (typeEntity.equals("advancedstorage")) {
 				List<Map<String, Object>> relatedTemplates = TemplateJobManager.getRelatedTemplateJobs(element, StepManager.TYPE_STEP_ADVANCED_STORAGE);
				if (relatedTemplates != null && !relatedTemplates.isEmpty())
					listTemplates.addAll(relatedTemplates);
 			} else if (typeEntity.equals("script")) {
 				List<Map<String, Object>> relatedTemplates = TemplateJobManager.getRelatedTemplateJobs(element, StepManager.TYPE_STEP_SCRIPT_SYSTEM);
				if (relatedTemplates != null && !relatedTemplates.isEmpty())
					listTemplates.addAll(relatedTemplates);
 				relatedTemplates = TemplateJobManager.getRelatedTemplateJobs(element, StepManager.TYPE_STEP_SCRIPT_APP);
				if (relatedTemplates != null && !relatedTemplates.isEmpty())
					listTemplates.addAll(relatedTemplates);
 			} else if (typeEntity.equals("step")) {
 				List<Map<String, Object>> relatedTemplates = TemplateJobManager.getTemplateJobsWithStep(element);
 				if (relatedTemplates != null && !relatedTemplates.isEmpty())
 					listTemplates.addAll(relatedTemplates);
 			} else if (typeEntity.equals("template")) {
 				Map<String, Object> templateJob = TemplateJobManager.getTemplateJob(element);
 				if (templateJob != null)
 					listTemplates.add(templateJob);
 			}
 			
 			if (!listTemplates.isEmpty()) {
 				sb.append(getTableTemplateJobsJS(listTemplates));
 			}
 			
 		} catch (Exception ex) {
 			logger.error("Error obteniendo datos de lista de plantillas en advanced backup para elmento {} de tipo {}", element, typeEntity);
 		}
 		
 		return sb.toString();
 	}

 	private String getTableTemplateJobsJS(Set<Map<String, Object>> templateJobs) throws Exception {
 		StringBuilder sb = new StringBuilder();
		
		 if (!templateJobs.isEmpty()) {
			 sb.append("[");
 			boolean first = true;
 			for (Map<String, Object> templateJob : templateJobs) {
 				if (!first)
 					sb.append(",");
 				else
 					first=false;
 				Integer numSteps = 0;
            	if (templateJob.get("steps") != null) {
            		@SuppressWarnings("unchecked")
					Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
            		if (steps.size()>0) {
            			numSteps = steps.size();
            		}
            	}
            	StringBuilder actions = new StringBuilder();
            	actions.append("<div style='padding-top:5px;'><a href='javascript:submitTemplateForm(\\\""+templateJob.get("name")+"\\\")'><img src='/images/advanced_backup_16.png' title='");
        		actions.append(getLanguageMessage("common.menu.advanced.backup"));
        		actions.append("' alt='");
        		actions.append(getLanguageMessage("common.menu.advanced.backup"));
        		actions.append("'/></a></div>");
        		
 				sb.append("{\"name\":\""+templateJob.get("name")+"\",\"numSteps\":\""+numSteps+"\",\"actions\":\""+actions.toString()+"\"}");
 			}
	 		sb.append("]");
        }
     
        return sb.toString();
 	}
 	
 	public String getJqGridJS(String tableId, String divId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid({\n");
 		sb.append("		datatype: \"local\",\n");
 		sb.append("		colNames:['"+getLanguageMessage("advanced.templatejob.name")+"','"+getLanguageMessage("advanced.templatejob.steps.total")+"','--'],\n");
 		sb.append("		colModel:[ {name:'name',index:'name', width:60, sortable:false}, \n");
 		sb.append("					{name:'numSteps',index:'numSteps', width:20, sortable:false},\n");
 		sb.append("					{name:'actions',index:'actions', width:20, sortable:false}],\n");
 		sb.append("		width: $('#"+divId+"').width(),\n");
 		sb.append("		height: 'auto',\n");
 		sb.append("		multiselect: false,");
 		sb.append("		gridComplete: LoadComplete,");
 		sb.append("		emptyDataText: '"+getLanguageMessage("advanced.templatejob.no_results")+"',");
 		sb.append("		onSelectRow: function(rowid, status) {\n");
 		sb.append("			$('#"+tableId+"').resetSelection();\n");
 		sb.append("		}\n");
 		sb.append("	});\n");
 		return sb.toString();
 	}
 	
 	public String getSubmitTemplateFormJS() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("function submitTemplateForm(linkElement) {\n");
 		sb.append("	if ($('#form').validationEngine('validate')) {\n"); 
 		sb.append("		document.form.templateJobName.value=linkElement;\n");
 		sb.append("		submitForm(document.form.submit());\n");
 		sb.append("	 }\n");
 		sb.append("}\n");
 		return sb.toString();
 	}
 	
 	public String emptyGridFuncJS(String tableId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("function LoadComplete()\n");
 		sb.append("{\n");
 		sb.append("		if ($('#"+tableId+"').jqGrid('getGridParam', 'reccount') == 0) \n");
 		sb.append("			DisplayEmptyText(true);\n");
 		sb.append("		else\n");
 		sb.append("			DisplayEmptyText(false);\n");
 		sb.append("}\n");
 		sb.append("function DisplayEmptyText( display)\n");
 		sb.append("{\n");
 		sb.append("		var grid = $('#"+tableId+"');\n");
 		sb.append("		var emptyText = grid.getGridParam('emptyDataText'); \n");
 		sb.append("		var container = grid.parents('.ui-jqgrid-view'); \n");
 		sb.append("		$('.EmptyData').remove(); \n");
 		sb.append("		if (display) {\n");
 		sb.append("			container.find('.ui-jqgrid-hdiv, .ui-jqgrid-bdiv').hide(); \n");
 		sb.append("			container.find('.ui-jqgrid-titlebar').after('<div class=\"EmptyData\" style=\"padding:10px;\">' + emptyText + '</div>'); \n");
 		sb.append("		}\n");
 		sb.append("		else {\n");
 		sb.append("			container.find('.ui-jqgrid-hdiv, .ui-jqgrid-bdiv').show(); \n");
 		sb.append("			$('.EmptyData').remove(); \n");
 		sb.append("		}\n");
 		sb.append("}\n");
 		return sb.toString();
 	}
}
