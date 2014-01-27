package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.RemoteStorageManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StorageInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.util.Configuration;


public class AdvancedRemoteStorage extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public final static int ADD_STORAGE = 2;
	public final static int EDIT_STORAGE = 3;
	public final static int REMOVE_STORAGE = 4;
	public final static int STORE_STORAGE = 5;
	
	public final static int ADD_STEP = 6;
	public final static int STORE_STEP = 7;
	public final static int EDIT_STEP = 8;
	public final static int REMOVE_STEP = 9;
	
	public final static String baseUrl = "/admin/"+AdvancedRemoteStorage.class.getSimpleName();
	
	Map<String, String> selectTypeConn = null;
	Map<String, String> selectTypeStorage = null;

	@Override
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
				    
    				printSectionHeader(_xhtml_out,  getLanguageMessage("advanced.remotestorage.info"));
		    			
    				List<Map<String, Object>> _storages = RemoteStorageManager.listRemoteStorages();
    				_xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
                    _xhtml_out.println("<div id=\"listadoStorages\" style=\"clear:both;width:100%;margin:auto;\"><table id=\"tablaStorages\" style=\"margin-left:0px;margin-right:0px;\"></table><div id='pager' ></div></div>");
	        	    _xhtml_out.print("</div>");
    				pageJS+=getJqGridJS("tablaStorages", "listadoStorages");
                 	pageJS+="reloadAll();\n";
                 	pageJSFuncs+=allLoad("tablaStorages", _storages);
                 	pageJSFuncs+=emptyGridFuncJS("tablaStorages");	
    			} break;
    			
    			case STORE_STORAGE: {
					if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.name"));
					}
					if (!request.getParameter("name").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.name.invalid"));
					}
					if (request.getParameter("typeConnection") == null || request.getParameter("typeConnection").isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.remotestorage.error.typeConnection"));
    				}
					if (request.getParameter("typeStorage") == null || request.getParameter("typeStorage").isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.remotestorage.error.typeStorage"));
    				}
					Map<String, Map<String, String>> vars = new TreeMap<String, Map<String, String>>();
					Integer numVars = Integer.parseInt(request.getParameter("numVars"));
					int i=0;
					while (i <= numVars) {
						if (request.getParameter("var-"+i+"-name") != null && !request.getParameter("var-"+i+"-name").isEmpty()) {
							Map<String, String> var = new HashMap<String, String>();
							var.put("name", request.getParameter("var-"+i+"-name"));
							if (request.getParameter("var-"+i+"-description") != null && !request.getParameter("var-"+i+"-description").isEmpty()) {
								var.put("description", request.getParameter("var-"+i+"-description"));
							} else
								var.put("description", "");
							if (request.getParameter("var-"+i+"-password") != null && request.getParameter("var-"+i+"-password").equals("true")) {
								var.put("password", request.getParameter("var-"+i+"-password"));
							} else
								var.put("password", "false");
							
							vars.put(request.getParameter("var-"+i+"-name"), var);
						}
						i++;
					}
					
					boolean edit = false;
					if (request.getParameter("edit") != null && !request.getParameter("edit").isEmpty()) {
    					edit = true;
    				}
					
					RemoteStorageManager.saveStorage(request.getParameter("name"), request.getParameter("typeConnection"), request.getParameter("typeStorage"), vars, edit);
					
	    			String messageUpdatedJobs = "";
	    			if (edit) {
	    				messageUpdatedJobs = updateRelatedJobs(request.getParameter("name"), _c);
	    			}
	    			
					String message = getLanguageMessage("advanced.remotestorage.stored");
					if (!messageUpdatedJobs.equals(""))
						message += ". "+getLanguageMessage("advanced.jobs.updated")+": "+messageUpdatedJobs;
					
					writeDocumentResponse(message, baseUrl+"?type="+EDIT_STORAGE+"&name="+request.getParameter("name"));
				} break;
    			
				case ADD_STORAGE: {
					printSetRemoteStorage(_xhtml_out, null);
				} break;
				
				case EDIT_STORAGE: {
					if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.name"));
					}				
					printSetRemoteStorage(_xhtml_out, request.getParameter("name"));
				} break;
    			
				case REMOVE_STORAGE: {
					if (request.getParameter("confirm") != null) {
						if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.remotestorage.error.name"));
						}
						RemoteStorageManager.removeRemoteStorage(request.getParameter("name"));
						writeDocumentResponse(getLanguageMessage("advanced.remotestorage.removed"), baseUrl);
					} else {
						writeDocumentQuestion(getLanguageMessage("advanced.remotestorage.question"), baseUrl+"?type=" + REMOVE_STORAGE + "&name=" + request.getParameter("name")+"&confirm=true", null);
					}
				} break;
				
				case ADD_STEP: {
					if(request.getParameter("nameStorage") == null || request.getParameter("nameStorage").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.name"));
					}
					printSetStep(_xhtml_out, request.getParameter("nameStorage"), null);
				} break;
				
				case EDIT_STEP: {
					if(request.getParameter("nameStorage") == null || request.getParameter("nameStorage").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.name"));
					}				
					if(request.getParameter("stepName") == null || request.getParameter("stepName").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.stepName"));
					}				

					printSetStep(_xhtml_out, request.getParameter("nameStorage"), request.getParameter("stepName"));
				} break;
				
				case REMOVE_STEP: {
					if (request.getParameter("confirm") != null) {
						if(request.getParameter("nameStorage") == null || request.getParameter("nameStorage").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.remotestorage.error.name"));
						}
						if(request.getParameter("stepName") == null || request.getParameter("stepName").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.remotestorage.error.stepName"));
						}			
						
						RemoteStorageManager.removeRemoteStorageStep(request.getParameter("nameStorage"),request.getParameter("stepName"));
						writeDocumentResponse(getLanguageMessage("advanced.remotestorage.step.removed"), baseUrl+"?type="+EDIT_STORAGE+"&name="+request.getParameter("nameStorage"));
					} else {
						writeDocumentQuestion(getLanguageMessage("advanced.remotestorage.step.question"), baseUrl+"?type=" + REMOVE_STEP + "&nameStorage=" + request.getParameter("nameStorage")+"&stepName=" + request.getParameter("stepName")+"&confirm=true", null);
					}
				} break;
				
				case STORE_STEP: {
					if(request.getParameter("nameStorage") == null || request.getParameter("nameStorage").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.name"));
					}
					
					if(request.getParameter("stepName") == null || request.getParameter("stepName").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.stepName"));
					}
					
					if (!request.getParameter("stepName").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
					}
					
					int i=0;
					Integer numScripts = Integer.parseInt(request.getParameter("numScripts"));
					Map<Integer, String> scripts = new TreeMap<Integer, String>();
					while (i<=numScripts) {
						if (request.getParameter("script-"+i+"-order") != null && !request.getParameter("script-"+i+"-order").isEmpty() && request.getParameter("script-"+i+"-value") != null && !request.getParameter("script-"+i+"-value").isEmpty()) {
							try {
								Integer order = Integer.parseInt(request.getParameter("script-"+i+"-order").trim());
								scripts.put(order, request.getParameter("script-"+i+"-value").trim());
							} catch (NumberFormatException ex) {
								throw new Exception(getLanguageMessage("advanced.remotestorage.error.steps.order"));
							}
						}
						i++;
					}
					
					if (scripts.size() <= 0) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.scripts"));
					}
					
					boolean edit = false;
					if (request.getParameter("edit") != null && !request.getParameter("edit").isEmpty()) {
    					edit = true;
    				}
					
					boolean mount = false;
					if (request.getParameter("mount") != null && request.getParameter("mount").equals("true")) {
						mount = true;
    				}
					
					RemoteStorageManager.saveRemoteStorageStep(request.getParameter("nameStorage"), request.getParameter("stepName"), scripts, mount, edit);
					
					
					String messageUpdatedJobs = updateRelatedStepJobs(request.getParameter("nameStorage"), request.getParameter("stepName"), _c);
					String message = getLanguageMessage("advanced.remotestorage.step.stored");
					if (!messageUpdatedJobs.equals(""))
						message += ". "+getLanguageMessage("advanced.jobs.updated")+": "+messageUpdatedJobs;
					
					writeDocumentResponse(message, baseUrl+"?type="+EDIT_STORAGE+"&name="+request.getParameter("nameStorage"));
				}
				
	    	}
	    } catch (Exception _ex) {
	    	if (type != STORE_STORAGE && type != STORE_STEP)
	    		writeDocumentError(_ex.getMessage(), baseUrl);
	    	else
	    		writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
	
	/**
	 * Actualiza los posibles jobs asociados al remoteStorage
	 * @param nameRemoteStorage
	 * @param _c
	 * @return
	 * @throws Exception
	 */
	public String updateRelatedJobs(String nameRemoteStorage, Configuration _c) throws Exception {
		String messageUpdatedJobs = "";
		List<Map<String, String>> steps = TemplateJobManager.getStepsWithProperty("advanced_storage", nameRemoteStorage);
		List<String> stepsChecked = new ArrayList<String>();
		if (steps != null && steps.size()>0) {
			ISCSIManager iscsim = new ISCSIManager(_c);
			JobManager jm = new JobManager(_c);
			for (Map<String, String> step : steps) {
				if (!stepsChecked.contains(step.get("name"))) {
					List<Map<String, Object>> jobs = GroupJobManager.getJobsWithProperty("step", step.get("name"), _c);
					if (jobs != null && !jobs.isEmpty()) {
						for (Map<String, Object> job : jobs) {
							Map<String, Object> storageInventory = StorageInventoryManager.getStorage((String)job.get("inventory"));
							if (job.containsKey("typeStep") && ((String)job.get("typeStep")).equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
								Map<String, String> variablesValues = JobManager.getScriptVars((String) job.get("name")+"_"+RemoteStorageManager.getNameRemoteStorageScript(1)); 
								Map<String, Object> remoteStorage = RemoteStorageManager.getRemoteStorage(step.get("data"));
								
								if (step != null) {
									RemoteStorageManager.generateScriptsJob((String) job.get("name"), (String) job.get("step"), storageInventory, remoteStorage, variablesValues, jm, iscsim);
									if (messageUpdatedJobs.equals(""))
										messageUpdatedJobs+=(String) job.get("name");
									else
										messageUpdatedJobs+=", "+(String) job.get("name");
								}
							}
						}
					}
					stepsChecked.add(step.get("name"));
				}
			}
		}
		return messageUpdatedJobs;
	}
	
	
	public String updateRelatedStepJobs(String nameRemoteStorage, String nameStep, Configuration _c) throws Exception {
		String messageUpdatedJobs = "";
		List<Map<String, String>> steps = TemplateJobManager.getStepsWithProperty("advanced_storage", nameRemoteStorage);
		List<String> stepsChecked = new ArrayList<String>();
		if (steps != null && steps.size()>0) {
			ISCSIManager iscsim = new ISCSIManager(_c);
			JobManager jm = new JobManager(_c);
			for (Map<String, String> step : steps) {
				if (!stepsChecked.contains(step.get("name"))) {
					List<Map<String, Object>> jobs = GroupJobManager.getJobsWithProperty("step", step.get("name"), _c);
					if (jobs != null && !jobs.isEmpty()) {
						for (Map<String, Object> job : jobs) {
							String name = (String)job.get("name");
							if (name.endsWith("-"+nameStep)) {
								Map<String, Object> storageInventory = StorageInventoryManager.getStorage((String)job.get("inventory"));
								if (job.containsKey("typeStep") && ((String)job.get("typeStep")).equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
									Map<String, String> variablesValues = JobManager.getScriptVars((String) job.get("name")+"_"+RemoteStorageManager.getNameRemoteStorageScript(1)); 
									Map<String, Object> remoteStorage = RemoteStorageManager.getRemoteStorage(step.get("data"));
									
									RemoteStorageManager.generateScriptsJob((String) job.get("name"), (String) job.get("step"), storageInventory, remoteStorage, variablesValues, jm, iscsim);
									if (messageUpdatedJobs.equals(""))
										messageUpdatedJobs+=(String) job.get("name");
									else
										messageUpdatedJobs+=", "+(String) job.get("name");
								}
							}
						}
					}
					stepsChecked.add(step.get("name"));
				}
			}
		}
		return messageUpdatedJobs;
	}
	
	public void printSetRemoteStorage(PrintWriter _xhtml_out, String nameStorage) throws Exception {
		
		Map<String, Object> storage = null;
		if (nameStorage != null && !nameStorage.equals("")) {
			storage = RemoteStorageManager.getRemoteStorage(nameStorage);
			if (storage == null) {
				throw new Exception("storage "+nameStorage+" does not exists");
			}
		}

		_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
		 if("es".equals(this.messagei18N.getLocale().getLanguage()))
			 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
		 else
			 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
		_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
	    
		writeDocumentBackForce(baseUrl);
		_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" id=\"form\" method=\"post\">");
		_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_STORAGE + "\"/>");
		
		String info = getLanguageMessage("advanced.remotestorage.info");
		if (storage == null)
			info+= ". " + getLanguageMessage("advanced.remotestorage.nosteps.info");
		
		printSectionHeader(_xhtml_out, info);
		_xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		if (storage != null && !storage.isEmpty()) {
			_xhtml_out.print(getLanguageMessage("advanced.remotestorage.edit"));
			_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"1\"/>");
		}
		else
			_xhtml_out.print(getLanguageMessage("advanced.remotestorage.new"));
		
		_xhtml_out.print(HtmlFormUtils.saveHeaderButtonEngineValidation("form", getLanguageMessage("common.message.save")));
        _xhtml_out.println("</h2>");
		_xhtml_out.println("<fieldset>");
		
		if (storage != null && !storage.isEmpty())
			_xhtml_out.print(HtmlFormUtils.inputTextObjReadOnly("name", getLanguageMessage("advanced.remotestorage.name"), storage, true));
		else
			_xhtml_out.print(HtmlFormUtils.inputTextObj("name", getLanguageMessage("advanced.remotestorage.name"), storage, true, "validate[required,custom[onlyLetterNumber]]"));
		_xhtml_out.print(HtmlFormUtils.selectOptionObj("typeConnection", getLanguageMessage("advanced.remotestorage.typeConnection"), storage, selectTypeConn, true));
		_xhtml_out.print(HtmlFormUtils.selectOptionObj("typeStorage", getLanguageMessage("advanced.remotestorage.typeStorage"), storage, selectTypeStorage, true));
    	
    	_xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
     	_xhtml_out.println("</div>");
     	
     	_xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		_xhtml_out.print(getLanguageMessage("advanced.remotestorage.variables"));
		_xhtml_out.print("<a href=\"javascript:addVar();\"><img src=\"/images/add_16.png\" title=\"");
    	_xhtml_out.print(getLanguageMessage("common.message.add"));
    	_xhtml_out.print("\" alt=\"");
    	_xhtml_out.print(getLanguageMessage("common.message.add"));
    	_xhtml_out.println("\"/></a>");
        _xhtml_out.println("</h2>");
        _xhtml_out.println("<fieldset>");
        _xhtml_out.println("<div class=\"standard_form\" style=\"width:100%;\">");
        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:150px;margin-right:50px;float:left;\">"+getLanguageMessage("advanced.remotestorage.nameVariables")+"</div>");
        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:450px;margin-right:50px;float:left;\">"+getLanguageMessage("advanced.remotestorage.descriptionVariables")+"</div>");
        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:100px;margin-right:50px;float:left;\">"+getLanguageMessage("advanced.remotestorage.typePasswordVariables")+"</div>");
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"clear\"></div>");
        
        _xhtml_out.println("<div name=\"variables\" id=\"variables\" style=\"width:100%\">");
        
        _xhtml_out.println("<div class=\"standard_form\" style=\"background: #e4e7ef;width:100%;\">");
        _xhtml_out.println("<div class=\"form_text\" style=\"width:150px;margin-right:50px;float:left;\">");
        _xhtml_out.println(StorageInventoryManager.iqnwwn_nameVar);
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"form_text\" style=\"width:450px;float:left;margin-right:50px;\">");
        _xhtml_out.println(getLanguageMessage("advanced.inventory.storage_iqn_var_storages"));
        _xhtml_out.println("</div>");
        _xhtml_out.print("<div class=\"form_text\" style=\"width:100px;float:left;\"></div>");
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"clear\"></div>");
        
        _xhtml_out.println("<div class=\"standard_form\" style=\"width:100%;\">");
        _xhtml_out.println("<div class=\"form_text\" style=\"width:150px;margin-right:50px;float:left;\">");
        _xhtml_out.println(StorageInventoryManager.airback_iqn_nameVar);
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"form_text\" style=\"width:450px;float:left;margin-right:50px;\">");
        _xhtml_out.println(getLanguageMessage("advanced.inventory.wbsairback_iqn_var"));
        _xhtml_out.println("</div>");
        _xhtml_out.print("<div class=\"form_text\" style=\"width:100px;float:left;\"></div>");
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"clear\"></div>");
        
        _xhtml_out.println("<div class=\"standard_form\" style=\"background: #e4e7ef;width:100%;\">");
        _xhtml_out.println("<div class=\"form_text\" style=\"width:150px;margin-right:50px;float:left;\">");
        _xhtml_out.println(StorageInventoryManager.airback_wwn_nameVar);
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"form_text\" style=\"width:450px;float:left;margin-right:50px;\">");
        _xhtml_out.println(getLanguageMessage("advanced.inventory.airback_wwn_nameVar"));
        _xhtml_out.println("</div>");
        _xhtml_out.print("<div class=\"form_text\" style=\"width:100px;float:left;\"></div>");
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"clear\"></div>");
        
        int x = 5;
        if (storage != null && storage.get("variables") != null) {
        	@SuppressWarnings("unchecked")
        	Map<String, Map<String, String>> vars = (Map<String, Map<String, String>>) storage.get("variables");
        	if (vars.size()>0) {
        		for (String varname : vars.keySet()){
        			Map<String, String> var = vars.get(varname);
        			boolean highlight = false;
        			if (x % 2 == 0)
        				highlight = true;
        			_xhtml_out.println(getHtmlInputVar("var-"+x, var.get("name"), var.get("description"), var.get("password"), highlight));
        			x++;
        		}
        	}
        }
        _xhtml_out.print("<input type=\"hidden\" name=\"numVars\" id=\"numVars\" value=\""+x+"\"/>");
        boolean highlight = false;
		if (x % 2 == 0)
			highlight = true;
        _xhtml_out.println(getHtmlInputVar("var-"+x, null, null, null, highlight));
        _xhtml_out.println("</div>");
        _xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	_xhtml_out.println("</div>");
    	
    	if (storage != null) {
    		printListSteps(_xhtml_out, storage);
    	}
    	
    	pageJSFuncs += getJSAddVars();
    	pageJSFuncs += getJSRemoveVars();
    	pageJS+= "$('#form').validationEngine();";
    	
    	_xhtml_out.print("</form>");
    }
	
	public void printSetStep(PrintWriter _xhtml_out, String nameStorage, String nameStep) throws Exception {
		Map<String, Object> step = null;
		if (nameStep != null && !nameStep.equals("") && nameStorage != null && !nameStorage.equals(""))
			step = RemoteStorageManager.getRemoteStorageStep(nameStorage, nameStep);

		writeDocumentBackForce(baseUrl+"?type="+EDIT_STORAGE+"&name="+nameStorage);
		printSectionHeader(_xhtml_out, getLanguageMessage("advanced.remotestorage.info"));
		
        
        List<Map<String, String>> stepsAdvanced = StepManager.listSteps(StepManager.TYPE_STEP_ADVANCED_STORAGE);
        if (stepsAdvanced != null && stepsAdvanced.size() > 0) {
        	_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" method=\"post\">");
    		_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_STEP + "\"/>");
    		_xhtml_out.print("<input type=\"hidden\" name=\"nameStorage\" value=\""+nameStorage+"\"/>");
    		
    		_xhtml_out.println("<div class=\"window\">");
    		_xhtml_out.println("<h2>");
    		if (step != null && !step.isEmpty()) {
    			_xhtml_out.print(getLanguageMessage("advanced.remotestorage.step.edit"));
    			_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"1\"/>");
    		}
    		else
    			_xhtml_out.print(getLanguageMessage("advanced.remotestorage.step.new"));
    		_xhtml_out.print(HtmlFormUtils.saveHeaderButton("form", getLanguageMessage("common.message.save")));
	        _xhtml_out.println("</h2>");
    		_xhtml_out.println("<fieldset>");
    		
	        Map<String, String> selectSteps = new TreeMap<String, String>();
	        for (Map<String, String> stepSelect : stepsAdvanced) {
	        	selectSteps.put(stepSelect.get("name"), stepSelect.get("name"));
	        }
	        Map<String, String> entityStep = null;
	        if (step != null) {
	        	entityStep = new HashMap<String, String>();
	        	entityStep.put("stepName", nameStep);
	        }
	        	
	        if (step != null)
	        	_xhtml_out.println(HtmlFormUtils.selectOptionReadOnly("stepName", getLanguageMessage("advanced.remotestorage.step.name"), entityStep, selectSteps, true));
	        else
	        	_xhtml_out.println(HtmlFormUtils.selectOption("stepName", getLanguageMessage("advanced.remotestorage.step.name"), entityStep, selectSteps, true));
	        
	        _xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"mount\">");
	    	_xhtml_out.print(getLanguageMessage("advanced.remotestorage.step.mount"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"mount\" value=\"true\" ");
	    	if (step != null && step.get("mount") != null && ((String)step.get("mount")).equals("true"))
	    		_xhtml_out.print(" checked=\"checked\"");	
	    	_xhtml_out.print("/>");
	    	_xhtml_out.println("</div>");
	    	
	        _xhtml_out.println("</fieldset>");
	    	_xhtml_out.println("<div class=\"clear\"></div>");
	    	_xhtml_out.println("</div>");
			
	    	_xhtml_out.println("<div class=\"window\">");
			_xhtml_out.println("<h2>");
			_xhtml_out.print(getLanguageMessage("advanced.remotestorage.steps"));
			_xhtml_out.print("<a href=\"javascript:addStep();\"><img src=\"/images/add_16.png\" title=\"");
	    	_xhtml_out.print(getLanguageMessage("common.message.add"));
	    	_xhtml_out.print("\" alt=\"");
	    	_xhtml_out.print(getLanguageMessage("common.message.add"));
	    	_xhtml_out.println("\"/></a>");
	        _xhtml_out.println("</h2>");
	        _xhtml_out.println("<fieldset>");
	        _xhtml_out.println("<div class=\"standard_form\">");
	        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:150px;margin-right:50px;float:left;\">"+getLanguageMessage("advanced.remotestorage.order")+"</div>");
	        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:300px;float:left;\">"+getLanguageMessage("advanced.remotestorage.script")+"</div>");
	        _xhtml_out.println("</div>");
	        _xhtml_out.println("<div name=\"scripts\" id=\"scripts\" >");
	        int x = 0;
	        if (step != null && step.get("scripts") != null) {
	        	@SuppressWarnings("unchecked")
	        	Map<Integer, String> scripts = (Map<Integer, String>) step.get("scripts");
	        	if (scripts.size()>0) {
	        		for (Integer ord : scripts.keySet()){
	        			_xhtml_out.println(getHtmlInputsScript("script-"+x, ord, scripts.get(ord)));
	        			x++;
	        		}
	        	}
	        }
	        _xhtml_out.print("<input type=\"hidden\" name=\"numScripts\" id=\"numScripts\" value=\""+x+"\"/>");
	        _xhtml_out.println(getHtmlInputsScript("script-"+x, null, null));
	        _xhtml_out.println("</div>");
	        _xhtml_out.println("</fieldset>");
	    	_xhtml_out.println("<div class=\"clear\"></div>");
	    	_xhtml_out.println("</div>");
        } else {
        	writeDocumentResponse(getLanguageMessage("advanced.remotestorage.steps.notype.advancedstorage"), baseUrl+"?type="+EDIT_STORAGE+"&name="+nameStorage);
        }
		
    	pageJSFuncs += getJSAddSteps();
    	pageJSFuncs += getJSRemoveSteps();
    	pageJSFuncs += checkOnlyNumbers();
    	
    	_xhtml_out.print("</form>");
	}
	
	@SuppressWarnings("unchecked")
	public void printListSteps(PrintWriter _xhtml_out, Map<String,Object> storage) throws Exception {
		_xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		_xhtml_out.print(getLanguageMessage("advanced.storages.steps"));
		_xhtml_out.print(HtmlFormUtils.addHeaderButton(baseUrl, ADD_STEP, getLanguageMessage("common.message.add"), "nameStorage="+storage.get("name")));
	    _xhtml_out.println("</h2>");
	    _xhtml_out.print("<br/>");
	    _xhtml_out.println("<fieldset>");
	    _xhtml_out.println("<table>");
		
	    Map<String, Object> steps = (Map<String, Object>) storage.get("steps");
		 if (steps != null && !steps.isEmpty()) {
        	_xhtml_out.println("<tr>");
        	_xhtml_out.println("<td>&nbsp;</td>");
            _xhtml_out.print("<td class=\"title\">");
            _xhtml_out.print(getLanguageMessage("advanced.remotestorage.step"));
            _xhtml_out.println("</td>");
            _xhtml_out.print("<td class=\"title\">");
            _xhtml_out.print(getLanguageMessage("advanced.remotestorage.step.total"));
            _xhtml_out.println("</td>");
            _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
            _xhtml_out.println("</tr>");
            int _offset=2;
            for (String stepName : steps.keySet()) {
            	Map<String,Object> step = (Map<String,Object>) steps.get(stepName);
        		_xhtml_out.print("<tr");
            	if(_offset % 2 == 0) {
            		_xhtml_out.print(" class=\"highlight\"");
            	}
            	_xhtml_out.println(">");
            	_xhtml_out.println("<td>&nbsp;</td>");
            	_xhtml_out.println("<td>");
            	_xhtml_out.print(step.get("name"));
            	_xhtml_out.println("</td>");
            	_xhtml_out.println("<td>");
            	Integer size = 0;
            	if (step.get("scripts") != null ) {
            		Map<Integer, String> scripts = (Map<Integer, String>) step.get("scripts");
            		if (scripts != null && scripts.size()>0)
            			size = scripts.size();
            	}
            	_xhtml_out.println(size);
            	_xhtml_out.println("</td>");
                _xhtml_out.println("<td>");
                String params = "&nameStorage="+storage.get("name")+"&stepName="+step.get("name");
                _xhtml_out.print(HtmlFormUtils.editButton(baseUrl, EDIT_STEP, getLanguageMessage("common.message.edit"), "edit_16.png", params));
                _xhtml_out.print(HtmlFormUtils.removeButton(baseUrl, REMOVE_STEP, getLanguageMessage("common.message.remove"), params));
                _xhtml_out.println("</td>");
                _xhtml_out.println("</tr>");
                _offset++;
        	}
        } else {
        	_xhtml_out.println("<tr>");
        	_xhtml_out.println("<td>");
        	_xhtml_out.println(getLanguageMessage("advanced.remotestorage.no_steps"));
        	_xhtml_out.println("</td>");
            _xhtml_out.println("</tr>");
        }
        
        _xhtml_out.println("</table>");
        _xhtml_out.print("<br/>");
        _xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.print("</div>");
	}
	
 	public void printSectionHeader(PrintWriter _xhtml_out, String info) throws Exception {
		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/remote_storage_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.remotestorage"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(info);
		_xhtml_out.println("</div>");
 	}
 	
 	public void fillSelects() {
 		selectTypeConn = new TreeMap<String, String>();
		selectTypeConn.put(RemoteStorageManager.CONNECTION_TYPE_SSH, getLanguageMessage("advanced.remotestorage.typeConnection.ssh"));
		
		selectTypeStorage = new TreeMap<String, String>();
		selectTypeStorage.put(RemoteStorageManager.STORAGE_TYPE_ISCSI, getLanguageMessage("advanced.remotestorage.typeStorage.iscsi"));
		selectTypeStorage.put(RemoteStorageManager.STORAGE_TYPE_FIBRE, getLanguageMessage("advanced.remotestorage.typeStorage.fibre"));
 	}
 	

 	
 	public static String getJSAddVars() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function addVar() {\n");
 		_sb.append("	var ni = document.getElementById('variables');\n");
 		_sb.append("	var numi = document.getElementById('numVars');\n");
 		_sb.append("	var num = parseInt(document.getElementById('numVars').value) + 1;\n");
 		_sb.append("	numi.value = num;\n");
 		_sb.append("	var divIdName = 'var-'+num;\n");
 		_sb.append("	var newdiv = document.createElement('div');\n");
 		_sb.append("	newdiv.setAttribute('id','div'+divIdName);\n");
 		_sb.append("	if (numi.value % 2 == 0)\n");
 		_sb.append("		newdiv.innerHTML = '"+getHtmlInputVarJS("divIdName", true)+"';\n");
 		_sb.append("	else\n");
 		_sb.append("		newdiv.innerHTML = '"+getHtmlInputVarJS("divIdName", false)+"';\n");
 		_sb.append("	ni.appendChild(newdiv);\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	}
 	
 	public static String getJSRemoveVars() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("	function removeVar(divNum) {\n");
 		_sb.append("		var d = document.getElementById('variables');\n");
 		_sb.append("		var olddiv = document.getElementById(divNum);\n");
 		_sb.append("		d.removeChild(olddiv);\n");
 		_sb.append("	}\n");
 		return _sb.toString();
 	}
 	
 	public static String getJSAddSteps() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function addStep() {\n");
 		_sb.append("	var ni = document.getElementById('scripts');\n");
 		_sb.append("	var numi = document.getElementById('numScripts');\n");
 		_sb.append("	var num = parseInt(document.getElementById('numScripts').value) + 1;\n");
 		_sb.append("	numi.value = num;\n");
 		_sb.append("	var divIdName = 'script-'+num;\n");
 		_sb.append("	var newdiv = document.createElement('div');\n");
 		_sb.append("	newdiv.setAttribute('id','div'+divIdName);\n");
 		_sb.append("	newdiv.innerHTML = '");
 		_sb.append(getHtmlInputsScriptJS("divIdName"));
 		_sb.append("';\n");
 		_sb.append("	ni.appendChild(newdiv);\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	}
 	
 	public static String getJSRemoveSteps() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("	function removeStep(divNum) {\n");
 		_sb.append("		var d = document.getElementById('steps');\n");
 		_sb.append("		var olddiv = document.getElementById(divNum);\n");
 		_sb.append("		d.removeChild(olddiv);\n");
 		_sb.append("	}\n");
 		return _sb.toString();
 	}
 	
 	public static String getHtmlInputVar(String inputId, String name, String description, String password, boolean highlight) {
 		StringBuilder _sb = new StringBuilder();
 		if (highlight)
 			_sb.append("<div class=\"standard_form\" style=\"background: #e4e7ef;width:100%;\">");
 		else
 			_sb.append("<div class=\"standard_form\" style=\"width:100%;\">");
 		_sb.append("<input class=\"form_text\" name=\""+inputId+"-name\" style=\"width:150px;margin-right:50px;\" id=\""+inputId+"-name\" value=\"");
		if (name != null && !name.isEmpty()) {
			_sb.append(name);
		}
    	_sb.append("\" />");
		_sb.append("<input class=\"form_text\" name=\""+inputId+"-description\" style=\"width:450px;margin-right:50px;\" id=\""+inputId+"-description\" value=\"");
		if (description != null && !description.isEmpty()) {
			_sb.append(description);
		}
    	_sb.append("\" />");
    	_sb.append("<input class=\"form_text\" type=\"checkbox\" name=\""+inputId+"-password\" style=\"width:100px;\" id=\""+inputId+"-password\" value=\"true\"");
    	if (password != null && password.equals("true")) {
			_sb.append(" checked=\"checked\" ");
		}
    	_sb.append("/>");
    	_sb.append("</div>");
    	return _sb.toString();
 	}
 	
 	public static String getHtmlInputVarJS(String inputId, boolean highlight) {
 		StringBuilder _sb = new StringBuilder();
 		if (highlight)
 			_sb.append("<div class=\"standard_form\" style=\"background: #e4e7ef;width:100%;\">");
 		else
 			_sb.append("<div class=\"standard_form\" style=\"width:100%;\">");
		_sb.append("<input class=\"form_text\" name=\"'+"+inputId+"+'-name\" style=\"width:150px;margin-right:50px;\" id=\"'+"+inputId+"+'-name\" value=\"\" />");
		_sb.append("<input class=\"form_text\" name=\"'+"+inputId+"+'-description\" style=\"width:450px;margin-right:50px;\" id=\"'+"+inputId+"+'-description\" value=\"\" />");
		_sb.append("<input class=\"form_text\" type=\"checkbox\" name=\"'+"+inputId+"+'-password\" style=\"width:100px;\" id=\"'+"+inputId+"+'-password\" value=\"true\" />");
    	_sb.append("</div>");
    	return _sb.toString();
 	}
 	
 	public static String getHtmlInputsScript(String inputId, Integer order, String value) {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("<div class=\"standard_form\">");
		_sb.append("<input class=\"form_text\" size=\"3\" style=\"width:30px;margin-right:160px;height:15px;vertical-align:top;\" onKeyPress=\"return checkIt(event)\" name=\""+inputId+"-order\" id=\""+inputId+"-order\"value=\"");
		if (order != null)
			_sb.append(order);
		_sb.append("\" /> &nbsp;&nbsp;");
		_sb.append("<textarea class=\"form_textarea\" name=\""+inputId+"-value\" style=\"height:100px;vertical-align:top;width:450px;\" id=\""+inputId+"-value\" cols=\"50\" rows=\"8\" wrap=\"off\">");
		if (value != null)
			_sb.append(value);
		_sb.append("</textarea>");
    	_sb.append("</div>");
    	return _sb.toString();
 	}
 	
 	public static String getHtmlInputsScriptJS(String inputId) {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("<div class=\"standard_form\" style=\"vertical-align:top;\">");
		_sb.append("<input class=\"form_text\" size=\"3\" style=\"width:30px;margin-right:160px;height:15px;vertical-align:top;\" onKeyPress=\"return checkIt(event)\" name=\"'+"+inputId+"+'-order\" id=\"'+"+inputId+"+'-order\" value=\"\" /> &nbsp;&nbsp;");
		_sb.append("<textarea class=\"form_textarea\" name=\"'+"+inputId+"+'-value\" id=\"'+"+inputId+"+'-value\" style=\"height:100px;vertical-align:top;width:450px;\" cols=\"50\" rows=\"8\" wrap=\"off\">");
		_sb.append("</textarea>");
    	_sb.append("</div>");
    	return _sb.toString();
 	}
 	
 	public static String checkOnlyNumbers() throws Exception {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function checkIt(evt) {\n");
 		_sb.append("	evt = (evt) ? evt : window.event;\n");
 		_sb.append("	var charCode = (evt.which) ? evt.which : evt.keyCode;\n");
 		_sb.append("	if (charCode > 31 && (charCode < 48 || charCode > 57)) {\n");
 		_sb.append("	status = \"This field accepts numbers only.\";\n");
 		_sb.append("	return false;\n");
 		_sb.append("	}\n");
 		_sb.append("    status = \"\";\n");
 		_sb.append("	return true;\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	}
 	
 	private String getJqGridJS(String tableId, String divId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid({\n");
 		sb.append("		datatype: \"local\",\n");
 		sb.append("		colNames:['"+getLanguageMessage("advanced.remotestorage.name")+"','"+getLanguageMessage("advanced.remotestorage.typeStorage")+"','"+getLanguageMessage("advanced.remotestorage.typeConnection")+"','--'],\n");
 		sb.append("		colModel:[ {name:'name',index:'name', width:50}, \n");
 		sb.append("					{name:'typeStorage',index:'typeStorage', width:20},\n");
 		sb.append("					{name:'typeConnection',index:'typeConnection', width:20},\n");
 		sb.append("					{name:'actions',index:'actions', width:10, sortable:false, search:false}],\n");
 		sb.append("		width: $('#"+divId+"').width(),\n");
 		sb.append("		height: 'auto',\n");
 		sb.append("		rownumbers: true,\n");
 		sb.append("		multiselect: false,\n");
 		sb.append("		hidegrid:false,\n");
 		sb.append("		rowNum: 10,\n");
 		sb.append("		rowList : [5,10,25,50],\n");
 		sb.append("		gridComplete: LoadComplete,\n");
 		sb.append("		pager: '#pager',\n");
 		sb.append("		caption: '"+getLanguageMessage("common.menu.advanced.remotestorage")+"',\n");
 		sb.append("		emptyDataText: '"+getLanguageMessage("advanced.remotestorage.no_remotestorage")+"',\n");
 		sb.append("		onSelectRow: function(rowid, status) {\n");
 		sb.append("			$('#"+tableId+"').resetSelection();\n");
 		sb.append("		}\n");
 		sb.append("	});\n");
 		sb.append("jQuery('#"+tableId+"')\n");
 		sb.append("		.navGrid('#pager',{edit:false,add:false,del:false,search:false,refresh:false})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'"+getLanguageMessage("common.message.add")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-add',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.href='"+baseUrl+"?type="+ADD_STORAGE+"';\n");
 		sb.append("			},\n"); 
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'"+getLanguageMessage("common.message.refresh")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-refresh',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.reload();\n");
 		sb.append("			},\n");
 		sb.append("			position:'last'\n");
 		sb.append("});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-add').css({'background-image':'url(\"/images/add_16.png\")', 'background-position':'0'});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-refresh').css({'background-image':'url(\"/images/arrow_refresh_16.png\")', 'background-position':'0'});\n");
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid('filterToolbar',{stringResult: true,searchOnEnter : false});\n");
 		return sb.toString();
 	}
 	
 	private String getJSONStoragesJS(Set<Map<String, Object>> storages) throws Exception {
 		StringBuilder sb = new StringBuilder();
		
		 if (!storages.isEmpty()) {
			 sb.append("[");
 			boolean first = true;
 			for (Map<String, Object> storage : storages) {
 				if (!first)
 					sb.append(",");
 				else
 					first=false;
 				
            	StringBuilder actions = new StringBuilder();
                
                actions.append("<div style='padding-top:5px;'><a href='"+baseUrl+"?type="+EDIT_STORAGE+"&name="+storage.get("name")+"'><img src='/images/edit_16.png' title='");
        		actions.append(getLanguageMessage("common.message.edit"));
        		actions.append("' alt='");
        		actions.append(getLanguageMessage("common.message.edit"));
        		actions.append("'/></a>  ");
        		actions.append("<a href='"+baseUrl+"?type="+REMOVE_STORAGE+"&name="+storage.get("name")+"'><img src='/images/delete_16.png' title='");
        		actions.append(getLanguageMessage("common.message.remove"));
        		actions.append("' alt='");
        		actions.append(getLanguageMessage("common.message.remove"));
        		actions.append("'/></a>");
        		actions.append("</div>");
            	
        		String typeStorage = "";
        		if (storage.get("typeStorage") != null && selectTypeStorage.containsKey(storage.get("typeStorage"))) {
        			typeStorage = selectTypeStorage.get(storage.get("typeStorage"));
            	}
        		
        		String typeConnection = "";
        		if (storage.get("typeConnection") != null && selectTypeConn.containsKey(storage.get("typeConnection"))) {
        			typeConnection = selectTypeConn.get(storage.get("typeConnection"));
            	}
        		
 				sb.append("{\"name\":\""+storage.get("name")+"\",\"typeStorage\":\""+typeStorage+"\",\"typeConnection\":\""+typeConnection+"\", \"actions\":\""+actions.toString()+"\"}");
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
 	
 	private String allLoad(String tableGridId, List<Map<String, Object>> storages) throws Exception{
 		StringBuilder sb = new StringBuilder();
 		sb.append("function reloadAll()\n");
 		sb.append("{\n");
		if (storages != null && !storages.isEmpty()) {
			HashSet<Map<String, Object>> listStorages = new HashSet<Map<String, Object>>();
			listStorages.addAll(storages);
			sb.append("	var json = '"+getJSONStoragesJS(listStorages).replace("'", "\\'")+"';\n");
			sb.append("	var alldata = jQuery.parseJSON(json);\n");
			sb.append("	if (alldata) {\n");
			sb.append("		for(var i=0;i<=alldata.length;i++) {jQuery(\"#"+tableGridId+"\").jqGrid('addRowData',i+1,alldata[i]);}\n");
			sb.append("	}\n");
		}
		sb.append("}\n");
		return sb.toString();
 	}
}
