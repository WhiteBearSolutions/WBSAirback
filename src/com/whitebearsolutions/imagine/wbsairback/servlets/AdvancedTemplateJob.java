package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.whitebearsolutions.http.AbstractRequest;
import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.RemoteStorageManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.SysAppsInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ApplicationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;


public class AdvancedTemplateJob extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public final static int ADD_TEMPLATEJOB = 2;
	public final static int ADD_TEMPLATE_STEP = 3;
	public final static int REMOVE_TEMPLATE_STEP = 4;
	public final static int STORE_TEMPLATE_STEP = 5;
	public final static int EDIT_TEMPLATEJOB = 6;
	public final static int REMOVE_TEMPLATEJOB = 7;
	public final static int STORE_TEMPLATEJOB = 8;
	public final static int UPDATE_STEP_ORDER = 13;
	public final static int IMPORT_TEMPLATE_JOB = 14;
	public final static int IMPORT_TEMPLATE_JOB_STORE = 15;
	public final static int EXPORT_TEMPLATE_JOB = 16;
	public final static int SEARCH_JSON = 62974502;
	public final static int TEMPLATE_LIST = 15697845;
	
	public final static String baseUrl = "/admin/"+AdvancedTemplateJob.class.getSimpleName();
	
	private final static Logger logger = LoggerFactory.getLogger(AdvancedTemplateJob.class);
	
	Map<String, String> selectTypeStep = null;
	Map<String, String> status = null;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
			
	    	AbstractRequest _ar = new AbstractRequest(request);
	    	this.type = 1;
			if(_ar.getParameter("type") != null && _ar.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(_ar.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			if (this.type == EXPORT_TEMPLATE_JOB) {
				String[] templateNames = request.getParameterValues("templateName");
				if (templateNames == null || templateNames.length == 0)
					throw new Exception(getLanguageMessage("advanced.templatejob.export.select.template"));
				try {
					String pathExport = TemplateJobManager.generateExport(Arrays.asList(templateNames));
					this.downloadFile(response, pathExport);
				} catch (Exception ex) {
					logger.error("Error exporting templates. Ex: {}",ex.getMessage());
					throw ex;
				} finally {
					try {
						Command.systemCommand("rm -r /tmp/templateExp*");
					} catch (Exception ex){}
					try {
						Command.systemCommand("rm -r /tmp/*.tar");
					} catch (Exception ex) {}
				}
			}
			
			PrintWriter _xhtml_out=response.getWriter();
			
			if (this.type == UPDATE_STEP_ORDER) {
				updateOrder(request.getParameter("nameTemplateJob"), request.getParameter("order"));
				return;
			} else if (this.type == SEARCH_JSON && request.getParameter("entity") != null && !request.getParameter("entity").isEmpty() && request.getParameter("term") != null && !request.getParameter("term").isEmpty()) {
				_xhtml_out.print(printJsonSearch(request.getParameter("entity"), request.getParameter("term").toLowerCase()));
				return;
			} else if (this.type == TEMPLATE_LIST && request.getParameter("element") != null && !request.getParameter("element").isEmpty() && request.getParameter("typeEntity") != null && !request.getParameter("typeEntity").isEmpty()) {
				_xhtml_out.print(printTemplateList(request.getParameter("element"), request.getParameter("typeEntity")));
				return;
			} 
			
			response.setContentType("text/html");
			writeDocumentHeader();
			
			Configuration _c = this.sessionManager.getConfiguration();
	    	fillSelects();
	    	switch(this.type) {
	    		
    			default: {
    				_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />");
    				if("es".equals(this.messagei18N.getLocale().getLanguage()))
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>");
					else
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>");
				    _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>");
				    
				    List<Map<String, Object>> templateJobs = TemplateJobManager.listTemplateJobs();
				    
    				printSectionHeader(_xhtml_out, getLanguageMessage("advanced.templatejob.info"));
	        	    
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
                 	
                 	_xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
                    _xhtml_out.println("<div id=\"listadoPlantillas\" style=\"clear:both;width:100%;margin:auto;\"><table id=\"tablaPlantillas\" style=\"margin-left:0px;margin-right:0px;\"></table><div id='pager' ></div></div>");
	        	    _xhtml_out.print("</div>");
                 	pageJS+=getJqGridJS("tablaPlantillas", "listadoPlantillas", templateJobs);
                 	pageJS+="reloadAll();\n";
                 	pageJSFuncs+=allLoad("tablaPlantillas", templateJobs);
                 	pageJSFuncs+=emptyGridFuncJS("tablaPlantillas");
	        	    
                 	pageJS+=getAutoCompleteJS("all", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("system", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("application", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("script", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("advancedstorage", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("template", "tablaPlantillas");
                 	pageJS+=getAutoCompleteJS("step", "tablaPlantillas");
                 	pageJS+="$( \"#buscador\" ).tabs()\n";
	                 	
    			} break;
    			
    			case ADD_TEMPLATE_STEP: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.name"));
					}
    				
    				if(request.getParameter("typeStep") == null || request.getParameter("typeStep").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.typeStep"));
					}
    				
    				printSetTemplateJobStep(_xhtml_out, request.getParameter("name"), request.getParameter("typeStep"));
    			} break;
    			
    			
    			case STORE_TEMPLATE_STEP: {
    				if(request.getParameter("templateJobName") == null || request.getParameter("templateJobName").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.name"));
					}
    				
					if (!request.getParameter("templateJobName").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
					}
    				
    				if(request.getParameter("typeStep") == null || request.getParameter("typeStep").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.typeStep"));
					}
    				
    				if(request.getParameter("stepName") == null || request.getParameter("stepName").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.stepName"));
					}
    				
    				TemplateJobManager.saveTemplateJobStep(request.getParameter("templateJobName"), request.getParameter("typeStep"), request.getParameter("stepName"), request.getParameter("data"), _c);
    				writeDocumentResponse(getLanguageMessage("advanced.templatejob.step.stored"), baseUrl+"?type="+EDIT_TEMPLATEJOB+"&name="+request.getParameter("templateJobName"));
    				
    			} break;
    			
				case REMOVE_TEMPLATE_STEP: {
					if (request.getParameter("confirm") != null) {
	    				if(request.getParameter("templateJobName") == null || request.getParameter("templateJobName").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.templatejob.error.name"));
						}
	    				if(request.getParameter("stepName") == null || request.getParameter("stepName").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.templatejob.error.stepName"));
						}
	    				
						TemplateJobManager.removeTemplateJobStep(request.getParameter("templateJobName"), request.getParameter("stepName"), _c);
						writeDocumentResponse(getLanguageMessage("advanced.templatejob.step.removed"), baseUrl+"?type="+EDIT_TEMPLATEJOB+"&name="+request.getParameter("templateJobName"));
					} else {
						writeDocumentQuestion(getLanguageMessage("advanced.templatejob.step.question"), baseUrl+"?type=" + REMOVE_TEMPLATE_STEP + "&templateJobName=" + request.getParameter("templateJobName")+"&stepName=" + request.getParameter("stepName")+"&confirm=true", null);
					}
				} break;
    			
    			case STORE_TEMPLATEJOB: {
					if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.name"));
					}
					if (!request.getParameter("name").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.name.invalid"));
					}
					
					boolean edit = false;
					if (request.getParameter("edit") != null && !request.getParameter("edit").isEmpty()) {
    					edit = true;
    				}
					
					TemplateJobManager.saveTemplateJob(request.getParameter("name"), edit);
					
					String messageUpdatedGroups = "";
					if (edit) {
						JobManager jm = new JobManager(_c);
						//ClientManager cm = new ClientManager(_c);
						
						// Recogemos los group job y el template con los datos
						List<Map<String, Object>> groups = GroupJobManager.getGroupJobsFromTemplate(request.getParameter("name"), _c);
						Map<String, Object> templateJob = TemplateJobManager.getTemplateJob(request.getParameter("name"));
						
						@SuppressWarnings("unchecked")
						Map<Integer, Map<String, String>> stepsTemplate = (Map<Integer, Map<String, String>>) templateJob.get("steps");
						
						boolean moreSteps = false;
						if (groups != null && groups.size()>0) {
							for (Map<String, Object> group : groups) {
								
								// Eliminamos del group job aquellos pasos que ya no están en el template
								@SuppressWarnings("unchecked")
								Map<Integer, Map<String, Object>> groupJobs = (Map<Integer, Map<String, Object>>) group.get("jobs");
								Map<Integer, Map<String, Object>> newJobs = new HashMap<Integer, Map<String, Object>>();
								Map<String, String> jobsToRemove = new HashMap<String, String>();
								Map<Integer, Map<String, Object>> jobsIndexedByNewOrder = new HashMap<Integer,  Map<String, Object>>();
								if (groupJobs != null && groupJobs.size()>0) { 
									for (Integer oGroup : groupJobs.keySet()) {
										Map<String, Object> job = groupJobs.get(oGroup);
										boolean found = false;
										for (Integer oStep : stepsTemplate.keySet()) {
											Map<String, String> step = stepsTemplate.get(oStep);
											if (step.get("name").equals(job.get("step"))) {
												found = true;
												jobsIndexedByNewOrder.put(oStep, job);
											}
										}
										if (!found)
											jobsToRemove.put((String) job.get("name"), String.valueOf((Integer) job.get("order")));
									}
									
									if (!jobsToRemove.isEmpty()) {
										for (String jobRemove : jobsToRemove.keySet()) {
											GroupJobManager.removeJobFromGroupJob(jobRemove, (String) group.get("name"), true, jm, _c);
											groupJobs.remove(Integer.parseInt(jobsToRemove.get(jobRemove)));
										}
									}
								
									if (stepsTemplate != null && stepsTemplate.size()>0) {
										if (stepsTemplate.size() != groupJobs.size())
											moreSteps = true;
									
										// Recorremos los pasos de template job en orden y buscamos en el group job el paso-job asociado
										for (Integer oStep : stepsTemplate.keySet()) {
											//Map<String, String> step = stepsTemplate.get(oStep);
											if (jobsIndexedByNewOrder.containsKey(oStep)) {	//Actualizamos
												Map<String, Object> job = jobsIndexedByNewOrder.get(oStep);
												String nextJob = getNextJob(jobsIndexedByNewOrder, oStep);
												if (nextJob != null)
													jm.setJobNextJob((String) job.get("name"), nextJob, true);
												
												newJobs.put(oStep, job);
											}
										}
									}
									
									GroupJobManager.saveGroupJob((String) group.get("name"), GroupJobManager.TYPE_TEMPLATEJOB, request.getParameter("name"), newJobs, request.getParameter("schedule"), request.getParameter("storage"));
									if (messageUpdatedGroups.equals(""))
										messageUpdatedGroups = (String) group.get("name");
									else
										messageUpdatedGroups += ", " + (String) group.get("name");
								}
							}
							String status = TemplateJobManager.GROUPS_STATUS_ALL_UPDATED;
							if (moreSteps)
								status = TemplateJobManager.GROUPS_STATUS_NEW_STEPS_NOT_INCLUDED;
							
							TemplateJobManager.writeTemplateJobXml(request.getParameter("name"), stepsTemplate, status);
						}
					}
					
					String message = getLanguageMessage("advanced.templatejob.stored");
					if (!messageUpdatedGroups.equals(""))
						message +=". "+getLanguageMessage("advanced.groups.updated")+": "+messageUpdatedGroups;
					
					writeDocumentResponse(message, baseUrl+"?type="+EDIT_TEMPLATEJOB+"&name="+request.getParameter("name"));
				} break;
    			
				case ADD_TEMPLATEJOB: {
					printSetTemplateJob(_xhtml_out, null);
				} break;
				
				case EDIT_TEMPLATEJOB: {
					if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.templatejob.error.name"));
					}				
					printSetTemplateJob(_xhtml_out, request.getParameter("name"));
				} break;
    			
				case REMOVE_TEMPLATEJOB: {
					if (request.getParameter("confirm") != null) {
						if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.templatejob.error.name"));
						}
						TemplateJobManager.removeTemplateJob(request.getParameter("name"));
						writeDocumentResponse(getLanguageMessage("advanced.templatejob.removed"), baseUrl);
					} else {
						writeDocumentQuestion(getLanguageMessage("advanced.templatejob.question"), baseUrl+"?type=" + REMOVE_TEMPLATEJOB + "&name=" + request.getParameter("name")+"&confirm=true", null);
					}
				} break;
				
				case IMPORT_TEMPLATE_JOB: {
					_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
					 if("es".equals(this.messagei18N.getLocale().getLanguage()))
						 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
					 else
						 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
					_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
					
					printSectionHeader(_xhtml_out, getLanguageMessage("advanced.templatejob.info"));
					_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" id=\"form\" method=\"post\" enctype=\"multipart/form-data\">");
					_xhtml_out.println("<input name=\"type\" type=\"hidden\" value=\""+IMPORT_TEMPLATE_JOB_STORE+"\" />");
					_xhtml_out.println("<div class=\"window\">");
		    		_xhtml_out.println("<h2>");
		    		_xhtml_out.print(getLanguageMessage("advanced.templatejob.import"));
		    		_xhtml_out.print(HtmlFormUtils.saveHeaderButton("form", getLanguageMessage("advanced.templatejob.import")));
		            _xhtml_out.println("</h2>");
		            _xhtml_out.println("<fieldset>");
					_xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"import_file\">");
        	    	_xhtml_out.print(getLanguageMessage("advanced.templatejob.import_file"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.print("<input class=\"form_file\" type=\"file\" class=\"validate[required]\"name=\"import_file\"/>");
        	    	_xhtml_out.println("</div>");
        	    	_xhtml_out.println("</fieldset>");
        	    	_xhtml_out.println("<div class=\"clear\"></div>");
        	    	_xhtml_out.println("</div>");
        	    	_xhtml_out.print("</form>");
        	    	pageJS+= "$('#form').validationEngine();";
        	    	
				} break;
				case IMPORT_TEMPLATE_JOB_STORE: {
					if(_ar.hasParameter("import_file") && !_ar.getParameter("import_file").isEmpty()) {
						byte[] _data = _ar.getFile("import_file");
    					if(_data != null && _data.length > 0) {
    						TemplateJobManager.importData(_data, _c);
	    				}
    				} else
    					throw new Exception(getLanguageMessage("advanced.templatejob.import.exception.file"));
					writeDocumentResponse(getLanguageMessage("advanced.templatejob.import.success"), baseUrl);
				} break;
	    	}
	    } catch (Exception _ex) {
	    	if (type != UPDATE_STEP_ORDER && type != SEARCH_JSON && type != TEMPLATE_LIST) {
		    	writeDocumentError(_ex.getMessage());
	    	}
	    } finally {
	    	if (type != UPDATE_STEP_ORDER && type != SEARCH_JSON && type != TEMPLATE_LIST) {
	    		writeDocumentFooter();
	    	}
	    }
		
	}
	
 	public void printSectionHeader(PrintWriter _xhtml_out, String info) throws Exception {
		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/template_job_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.templatejob"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(info);
		_xhtml_out.println("</div>");
 	}
 	
 	public static String getNextJob(Map<Integer, Map<String, Object>> jobs, Integer order) throws Exception{
		int nextOrder = order+1;
		Map<String, Object> nextJob = jobs.get(nextOrder);
		if (nextJob != null && !nextJob.isEmpty()) {
			return (String) nextJob.get("name");
		}
		return null;
	}
 	
 	public void printSetTemplateJob(PrintWriter _xhtml_out, String templateJobName) throws Exception {
		Map<String, Object> templateJob = null;
		if (templateJobName != null && !templateJobName.equals(""))
			templateJob = TemplateJobManager.getTemplateJob(templateJobName);

		_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
		 if("es".equals(this.messagei18N.getLocale().getLanguage()))
			 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
		 else
			 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
		_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
		
		writeDocumentBackForce(baseUrl);
		_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" id=\"form\" method=\"post\">");
		_xhtml_out.print("<input type=\"hidden\" name=\"type\" id=\"type\" value=\"" + STORE_TEMPLATEJOB + "\"/>");
		
		String info = getLanguageMessage("advanced.templatejob.info");
		if (templateJob == null)
			info+=". "+getLanguageMessage("advanced.templatejob.nosteps.info");
		else
			info+=". "+getLanguageMessage("advanced.templatejob.reorder.info");
		printSectionHeader(_xhtml_out, info);
		_xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		if (templateJob != null && !templateJob.isEmpty()) {
			_xhtml_out.print(getLanguageMessage("advanced.templatejob.edit"));
			_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"1\"/>");
		}
		else
			_xhtml_out.print(getLanguageMessage("advanced.templatejob.new"));
		
		_xhtml_out.print(HtmlFormUtils.saveHeaderButtonEngineValidation("form", getLanguageMessage("common.message.save")));
        _xhtml_out.println("</h2>");
		_xhtml_out.println("<fieldset>");
		if (templateJob == null)
			_xhtml_out.print(HtmlFormUtils.inputTextObj("name", getLanguageMessage("advanced.templatejob.name"), templateJob, true, "validate[required,custom[onlyLetterNumber]]"));
		else
			_xhtml_out.print(HtmlFormUtils.inputTextObjReadOnly("name", getLanguageMessage("advanced.templatejob.name"), templateJob, true));
    	_xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
     	_xhtml_out.println("</div>");
     	
     	if (templateJob != null) {
     		_xhtml_out.println("<div class=\"window\">");
    		_xhtml_out.println("<h2>");
    		_xhtml_out.print(getLanguageMessage("advanced.templatejob.steps"));
    		_xhtml_out.print(HtmlFormUtils.addHeaderButtonSubmit(baseUrl, ADD_TEMPLATE_STEP, getLanguageMessage("advanced.template.job.addstep"), "form"));
            _xhtml_out.println("</h2>");
            _xhtml_out.println("<fieldset>");
    		_xhtml_out.print(HtmlFormUtils.selectOptionObj("typeStep", getLanguageMessage("advanced.template.job.typeStep"), null, selectTypeStep, false));
        	_xhtml_out.println("</fieldset>");
        	_xhtml_out.println("<div class=\"clear\"></div>");
         	_xhtml_out.println("</div>");
         	
     		_xhtml_out.println("<div class=\"window\">");
    		_xhtml_out.println("<h2>");
    		_xhtml_out.print(getLanguageMessage("advanced.templatejob.list.steps"));
            _xhtml_out.println("</h2>");
            _xhtml_out.println("<fieldset>");
    		printListSteps(_xhtml_out, templateJob);
        	_xhtml_out.println("</fieldset>");
        	_xhtml_out.println("<div class=\"clear\"></div>");
         	_xhtml_out.println("</div>");   
     	}
     	
     	pageJS+= "$('#form').validationEngine();";
 	}
 	
 	public void printSetTemplateJobStep(PrintWriter _xhtml_out, String templateJobName, String typeStep) throws Exception {
		
 		writeDocumentBackForce(baseUrl+"?type="+EDIT_TEMPLATEJOB+"&name="+templateJobName);
		
		List<Map<String, String>> advancedSteps = StepManager.listSteps(typeStep);
		if (advancedSteps != null && advancedSteps.size() > 0) {
			printSectionHeader(_xhtml_out, getLanguageMessage("advanced.templatejob.info"));
			String selectMessage =  null;
			Map<String, String> selectedStep = advancedSteps.get(0);
			Map<String, String> entity = new HashMap<String, String>();
			if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
				List<String> remoteStorages = RemoteStorageManager.listRemoteStorageNames();
				for (String name : remoteStorages) {
					entity.put(name, name);
				}
				selectMessage = getLanguageMessage("advanced.templatejob.remotestorage");
			} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP)) {
				ScriptProcessManager _sp = new ScriptProcessManager();
				Map<String, Map<String, Object>> scripts = _sp.getScriptByStepAndType(selectedStep.get("name"), "application");
				for (String nameScript : scripts.keySet()) {
					entity.put(nameScript, nameScript);
				}
				selectMessage = getLanguageMessage("advanced.templatejob.script.app");
			} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
				ScriptProcessManager _sp = new ScriptProcessManager();
				Map<String, Map<String, Object>> scripts = _sp.getScriptByStepAndType(selectedStep.get("name"), "system");
				for (String nameScript : scripts.keySet()) {
					entity.put(nameScript, nameScript);
				}
				selectMessage = getLanguageMessage("advanced.templatejob.script.system");
			}
			
			_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" method=\"post\">");
			_xhtml_out.print("<input type=\"hidden\" name=\"type\" id=\"type\" value=\"" + STORE_TEMPLATE_STEP + "\"/>");
			_xhtml_out.print("<input type=\"hidden\" name=\"typeStep\" value=\"" + typeStep + "\"/>");
			_xhtml_out.print("<input type=\"hidden\" name=\"templateJobName\" value=\"" + templateJobName + "\"/>");
			
			
			Map<String, String> selectSteps = new HashMap<String, String>();
			for (Map<String, String> advancedStep : advancedSteps) {
				selectSteps.put(advancedStep.get("name"), advancedStep.get("name"));
			}
			_xhtml_out.println("<div class=\"window\">");
			_xhtml_out.println("<h2>");
			if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
				_xhtml_out.print(getLanguageMessage("advanced.templatejob.step.new.second.step.remote"));	
			} else if (typeStep.equals(StepManager.TYPE_STEP_BACKUP)) {
				_xhtml_out.print(getLanguageMessage("advanced.templatejob.step.new.second.step.backup"));
			} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP)) {
				_xhtml_out.print(getLanguageMessage("advanced.templatejob.step.new.second.step.script_app"));
			} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
				_xhtml_out.print(getLanguageMessage("advanced.templatejob.step.new.second.step.script_sys"));
			}
			_xhtml_out.print(HtmlFormUtils.saveHeaderButton("form", getLanguageMessage("common.message.save")));
	        _xhtml_out.println("</h2>");
			_xhtml_out.println("<fieldset>");
			_xhtml_out.println("<div class=\"standard_form\">");		
			String onChange="";
			if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP) || typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
				String type = "system";
				if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP))
					type = "application";
				onChange="updateScriptCombo(this.value, '"+type+"');";
				pageJSFuncs+=getJSUpdateScriptCombo();
			}
    		Map<String, String> selected = new HashMap<String, String>();
    		selected.put("stepName", selectedStep.get("name"));
	        _xhtml_out.println(HtmlFormUtils.selectOption("stepName", getLanguageMessage("advanced.templatejob.step.name"), selected, selectSteps, true, onChange));
	        
	        if (!typeStep.equals(StepManager.TYPE_STEP_BACKUP)) {
	        	_xhtml_out.println("<div id=\"divCombo\" name=\"divCombo\">");
	        	if (!entity.isEmpty() || typeStep.equals(StepManager.TYPE_STEP_BACKUP)) {
	        		_xhtml_out.println(HtmlFormUtils.selectOption("data", selectMessage, null, entity, true));
	        	} else {
	        		_xhtml_out.println("<div class=\"subinfo\">");
	        		_xhtml_out.println(getLanguageMessage("advanced.templatejob.steps.entities.nooftypestep"));
					_xhtml_out.println("</div>");
				} 
	        	_xhtml_out.println("</div>");
	        }
	        _xhtml_out.println("</div>");
	        _xhtml_out.println("</fieldset>");
	    	_xhtml_out.println("<div class=\"clear\"></div>");
	    	_xhtml_out.println("</div>");
			
		} else {
			writeDocumentResponse(getLanguageMessage("advanced.templatejob.steps.nooftype"), baseUrl+"?type="+EDIT_TEMPLATEJOB+"&name="+templateJobName);
		}
 	} 
 	
 	public void printListSteps(PrintWriter _xhtml_out, Map<String, Object> templateJob) throws Exception{
 		 	_xhtml_out.println("<table id=\"sortable\">");
			
 		 	if (templateJob.get("steps") != null) {
 		 		@SuppressWarnings("unchecked")
				Map<Integer, Map<String, String>> _steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
 		 		if (!_steps.isEmpty()) {
 		 			_xhtml_out.println("<thead>");
 		 			_xhtml_out.println("<tr>");
 		 			_xhtml_out.println("<td>&nbsp;</td>");
 		 			_xhtml_out.println("<td>&nbsp;</td>");
 		 			_xhtml_out.print("<td class=\"title\">");
 		 			_xhtml_out.print(getLanguageMessage("advanced.templatejob.step.name"));
 		 			_xhtml_out.println("</td>");
 		 			_xhtml_out.print("<td class=\"title\">");
 		 			_xhtml_out.print(getLanguageMessage("advanced.templatejob.step.type"));
 		 			_xhtml_out.println("</td>");
 		 			_xhtml_out.print("<td class=\"title\">");
 		 			_xhtml_out.print(getLanguageMessage("advanced.templatejob.step.data"));
 		 			_xhtml_out.println("</td>");
 		 			_xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
 		 			_xhtml_out.println("</tr>");
 		 			_xhtml_out.println("</thead>");
 		 			_xhtml_out.println("<tbody>");
 		 			for (Integer order : _steps.keySet()) {
 		 				Map<String,String> _step = (Map<String, String>) _steps.get(order);
 		 				_xhtml_out.print("<tr class=\"ui-state-default\" style=\"cursor:pointer\" id = \""+_step.get("name")+"___"+_step.get("data")+"\" ");	
	                 	_xhtml_out.println(">");
	                 	_xhtml_out.println("<td>&nbsp;</td>");
	                 	_xhtml_out.println("<td id = \""+_step.get("name")+"___"+_step.get("data")+"-order\">");
	                 	_xhtml_out.print(_step.get("order"));
	                 	_xhtml_out.println("</td>");
	                 	_xhtml_out.println("<td>");
	                 	_xhtml_out.print(_step.get("name"));
	                 	_xhtml_out.println("</td>");
	                 	_xhtml_out.println("<td>");
	                 	if (_step.get("type") != null && selectTypeStep.containsKey(_step.get("type"))) {
	                 		_xhtml_out.print(selectTypeStep.get(_step.get("type")));
	                 	}
	                 	_xhtml_out.println("</td>");
	                 	_xhtml_out.println("<td>");
	                 	if (_step.get("data") != null)
	                 		_xhtml_out.println(_step.get("data"));
	                 	else
	                 		_xhtml_out.println("--");
                 		_xhtml_out.println("</td>");
	                    _xhtml_out.println("<td>");
	                    String params = "&stepName="+_step.get("name")+"&templateJobName="+templateJob.get("name")+"&typeStep="+_step.get("type");
	                    _xhtml_out.print(HtmlFormUtils.removeButton(baseUrl, REMOVE_TEMPLATE_STEP, getLanguageMessage("common.message.remove"), params));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	             	}
 		 			_xhtml_out.println("</tbody>");
 		 		
 		 			pageJS+="var fixHelper = function(e, ui) {\n";
 		 			pageJS+="ui.children().each(function() {\n";
 		 			pageJS+="$(this).width($(this).width());\n";
 		 			pageJS+="});\n";
 		 			pageJS+="return ui;\n";
 		 			pageJS+="};\n";

 		 			pageJS+="$(\"#sortable tbody\").sortable({\n";
 		 			pageJS+="helper: fixHelper\n";
 		 			pageJS+="}).disableSelection();\n";
 		 			
 		 			pageJS+="$(\"#sortable tbody\").sortable({   update: function(event, ui) {\n";
 		 			pageJS+="	var order = $(this).sortable('toArray').toString();\n";
 		 			pageJS+="	$.ajax({\n";
 		 			pageJS+="		url:'"+baseUrl+"?type="+UPDATE_STEP_ORDER+"',\n";
 		 			pageJS+="		cache: false,\n";
 		 			pageJS+="		data: {order:order, nameTemplateJob: '"+templateJob.get("name")+"'}\n";
 		 			pageJS+="	}).done(function( html ) {\n";
 		 			pageJS+="		var steps = order.split(',');\n";
 		 			pageJS+="		for(var i=0;i<steps.length;i++) {\n";
 		 			pageJS+="			$(\"#\"+steps[i]+\"-order\").html(i+1);\n";
 		 			pageJS+="		}\n";
 		 			pageJS+="	});\n";
 		 			pageJS+="	}});\n";
 		 		} else {
	             	_xhtml_out.println("<tr>");
	             	_xhtml_out.println("<td>");
	             	_xhtml_out.println(getLanguageMessage("advanced.templatejob.no_steps"));
	             	_xhtml_out.println("</td>");
	                _xhtml_out.println("</tr>");
 		 		}
 		 		_xhtml_out.println("</table>");
 		 		_xhtml_out.print("<br/>");
 		 	}
 	}
 	
 	public static void updateOrder(String templateJobName, String order) throws Exception {
 		Map<String, Object> templateJob = TemplateJobManager.getTemplateJob(templateJobName);
 		if (templateJob != null) {
	 		@SuppressWarnings("unchecked")
			Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
			Map<Integer, Map<String, String>> newSteps = new TreeMap<Integer, Map<String, String>>();
	 		if (steps != null && steps.size()>0) {
		 		String[] arraySteps = order.split(",");
		 		int i=1;
		 		for (String s : arraySteps) {
		 			String name = s.substring(0,s.indexOf("___"));
		 			 Map<String, String> step = new HashMap<String, String>();
		 			for (Integer o : steps.keySet()) {
		 				Map<String, String> searchStep = steps.get(o);
		 				if (searchStep.get("name").equals(name))
		 					step.putAll(searchStep);
		 			}
		 			if (!step.isEmpty()) {
		 				step.put("order", String.valueOf(i));
		 				newSteps.put(i, step);
		 			}
		 			i++;
		 		}
		 		String status = TemplateJobManager.GROUPS_STATUS_NO_GROUPS;
		 		if (templateJob.get("groups_status") != null && !templateJob.get("groups_status").equals(TemplateJobManager.GROUPS_STATUS_NO_GROUPS))
		 			status = TemplateJobManager.GROUPS_STATUS_NOT_UPDATED;
		 		
		 		TemplateJobManager.writeTemplateJobXml(templateJobName, newSteps, status);
	 		}
 		}
 	}
 	
 	public void fillSelects() {
 		selectTypeStep = new TreeMap<String, String>();
		selectTypeStep.put(StepManager.TYPE_STEP_ADVANCED_STORAGE, getLanguageMessage("advanced.step.type.advanced_storage"));
		selectTypeStep.put(StepManager.TYPE_STEP_BACKUP, getLanguageMessage("advanced.step.type.backup"));
		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_APP, getLanguageMessage("advanced.step.type.script_app"));
		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_SYSTEM, getLanguageMessage("advanced.step.type.script_system"));
		
		status = new TreeMap<String, String>();
		status.put(TemplateJobManager.GROUPS_STATUS_NO_GROUPS, getLanguageMessage("advanced.templatejob.groups.nogroups"));
		status.put(TemplateJobManager.GROUPS_STATUS_NEW_STEPS_NOT_INCLUDED, getLanguageMessage("advanced.templatejob.groups.newnotincluded"));
		status.put(TemplateJobManager.GROUPS_STATUS_ALL_UPDATED, getLanguageMessage("advanced.templatejob.groups.allupdated"));
		status.put(TemplateJobManager.GROUPS_STATUS_NOT_UPDATED, getLanguageMessage("advanced.templatejob.groups.notupdated"));
 	}
 	
 	public String getJSUpdateScriptCombo() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function updateScriptCombo(step, type) {\n");
 		_sb.append("	$.ajax({\n");
 		_sb.append("		url: '/admin/AdvancedScriptProcess?type="+AdvancedScriptProcess.SCRIPT_COMBO_BY_STEP+"',\n");
 		_sb.append("		cache: false,\n");
 		_sb.append("		data: { step: step, typeScript: type}\n");
 		_sb.append("	}).done(function( html ) {\n");
 		_sb.append("		$('#divCombo').html(html);\n");
 		_sb.append("	});\n");
 		_sb.append("}\n");
 		return _sb.toString();
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
	 	sb.append("					if (html) {\n");
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
 			logger.error("Error obteniendo datos de lista de plantillas en advanced template job para elmento {} de tipo {}", element, typeEntity);
 		}
 		
 		return sb.toString();
 	}
 	
 	private String getJqGridJS(String tableId, String divId,  List<Map<String, Object>> templateJobs) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid({\n");
 		sb.append("		datatype: \"local\",\n");
 		sb.append("		colNames:['"+getLanguageMessage("advanced.templatejob.name")+"','"+getLanguageMessage("advanced.templatejob.steps.total")+"','"+getLanguageMessage("advanced.templatejob.steps.groups_status")+"','--'],\n");
 		sb.append("		colModel:[ {name:'name',index:'name', width:50}, \n");
 		sb.append("					{name:'numSteps',index:'numSteps', width:20, sorttype:'int'},\n");
 		sb.append("					{name:'groups_status',index:'groups_status', width:20, sortable:false},\n");
 		sb.append("					{name:'actions',index:'actions', width:10, sortable:false}],\n");
 		sb.append("		width: $('#"+divId+"').width(),\n");
 		sb.append("		height: 'auto',\n");
 		sb.append("		multiselect: true,\n");
 		sb.append("		hidegrid:false,\n");
 		sb.append("		rowNum: 10,\n");
 		sb.append("		hidegrid: false,\n");
 		sb.append("		rowList : [5,10,25,50],\n");
 		sb.append("		gridComplete: LoadComplete,\n");
 		sb.append("		pager: '#pager',\n");
 		sb.append("		caption: '"+getLanguageMessage("advanced.templatejobs")+"',\n");
 		sb.append("		emptyDataText: '"+getLanguageMessage("advanced.backup.notemplatejobs.info")+"'\n");
 		sb.append("	});\n");
 		sb.append("jQuery('#"+tableId+"')\n");
 		sb.append("		.navGrid('#pager',{edit:false,add:false,del:false,search:false,refresh:false})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'"+getLanguageMessage("common.message.add")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-add',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.href='"+baseUrl+"?type="+ADD_TEMPLATEJOB+"';\n");
 		sb.append("			},\n"); 
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'"+getLanguageMessage("common.message.refresh")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-refresh',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				jQuery(\"#"+tableId+"\").jqGrid('clearGridData');\n");
 		sb.append("				reloadAll();\n");
 		sb.append("			},\n");
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'"+getLanguageMessage("advanced.templatejob.import")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-import',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.href='"+baseUrl+"?type="+IMPORT_TEMPLATE_JOB+"';\n");
 		sb.append("			},\n");
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'"+getLanguageMessage("advanced.templatejob.export")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-export',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				var ids = jQuery(\"#"+tableId+"\").jqGrid('getGridParam', 'selarrrow');\n");
 		sb.append("				if (ids != null && ids != \"\" && ids.length > 0) {\n");
 		sb.append("					var queryString = \"\";\n");
 		sb.append("					for (var i = 0 ;i< ids.length;i++) {\n");
 		sb.append("						var data = jQuery(\"#"+tableId+"\").getRowData(ids[i]);\n");  
 		sb.append("						queryString+=\"&templateName=\"+data.name;\n");
 		sb.append("						window.location.href='"+baseUrl+"?type="+EXPORT_TEMPLATE_JOB+"'+queryString;\n");
 		sb.append("					}\n");
 		sb.append("				} else {\n");
 		sb.append("					alert(\""+getLanguageMessage("advanced.templatejob.export.select.template")+"\");\n");
 		sb.append("				}\n");
 		sb.append("			},\n");
 		sb.append("			position:'last'\n");
 		sb.append("});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-add').css({'background-image':'url(\"/images/add_16.png\")', 'background-position':'0'});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-refresh').css({'background-image':'url(\"/images/arrow_refresh_16.png\")', 'background-position':'0'});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-import').css({'background-image':'url(\"/images/template_import_16.png\")', 'background-position':'0'});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-export').css({'background-image':'url(\"/images/template_export_16.png\")', 'background-position':'0'});\n");
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
            	
            	String groups_status = "";
            	if (templateJob.get("groups_status") != null && !((String)templateJob.get("groups_status")).isEmpty() && !((String)templateJob.get("groups_status")).equals("null")) 
            		groups_status = status.get(templateJob.get("groups_status"));
                
                actions.append("<div style='padding-top:5px;'><a href='"+baseUrl+"?type="+EDIT_TEMPLATEJOB+"&name="+templateJob.get("name")+"'><img src='/images/edit_16.png' title='");
        		actions.append(getLanguageMessage("common.message.edit"));
        		actions.append("' alt='");
        		actions.append(getLanguageMessage("common.message.edit"));
        		actions.append("'/></a>  ");
        		actions.append("<a href='"+baseUrl+"?type="+REMOVE_TEMPLATEJOB+"&name="+templateJob.get("name")+"'><img src='/images/delete_16.png' title='");
        		actions.append(getLanguageMessage("common.message.remove"));
        		actions.append("' alt='");
        		actions.append(getLanguageMessage("common.message.remove"));
        		actions.append("'/></a>");
        		actions.append("</div>");
                
 				sb.append("{\"name\":\""+templateJob.get("name")+"\",\"numSteps\":\""+numSteps+"\",\"groups_status\":\""+groups_status+"\", \"actions\":\""+actions.toString()+"\"}");
 			}
	 		sb.append("]");
        }
     
        return sb.toString();
 	}
 	
 	private String emptyGridFuncJS(String tableId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("function LoadComplete()\n");
 		sb.append("{\n");
 		sb.append("	 $('#"+tableId+"').trigger('reloadGrid');\n");
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
 	
 	private String allLoad(String tableGridId, List<Map<String, Object>> templateJobs) throws Exception{
 		StringBuilder sb = new StringBuilder();
		sb.append("function reloadAll()\n");
 		sb.append("{\n");
 		if (templateJobs != null && !templateJobs.isEmpty()) {
			HashSet<Map<String, Object>> listTemplates = new HashSet<Map<String, Object>>();
			listTemplates.addAll(templateJobs);

			sb.append("	var json = '"+getTableTemplateJobsJS(listTemplates).replace("'", "\\'")+"';\n");
			sb.append("	var alldata = jQuery.parseJSON(json);\n");
			sb.append("	if (alldata) {\n");
			sb.append("		for(var i=0;i<=alldata.length;i++) {jQuery(\"#"+tableGridId+"\").jqGrid('addRowData',i+1,alldata[i]);}\n");
			sb.append("	}\n");
		}
 		sb.append("}\n");
		return sb.toString();
 	}
}
