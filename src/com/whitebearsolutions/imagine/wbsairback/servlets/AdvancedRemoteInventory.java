package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.RemoteStorageManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StorageInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.util.Configuration;


public class AdvancedRemoteInventory extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public final static int ADD_STORAGE = 2;
	public final static int EDIT_STORAGE = 3;
	public final static int REMOVE_STORAGE = 4;
	public final static int STORE_STORAGE = 5;
	
	public final static String baseUrl = "/admin/"+AdvancedRemoteInventory.class.getSimpleName();

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
			
	    	switch(this.type) {
    		
				default: {
					_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />");
					if("es".equals(this.messagei18N.getLocale().getLanguage()))
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>");
					else
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>");
				    _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>");
				    
    				printSectionHeader(_xhtml_out);
		    			
    				List<Map<String, Object>> _storages = StorageInventoryManager.listStorages();
	    			 
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
						throw new Exception(getLanguageMessage("advanced.inventory.error.name"));
					}
					if(request.getParameter("iqnwwn") == null || request.getParameter("iqnwwn").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.iqnwwn"));
					}
					if (!request.getParameter("name").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
					}
					if (request.getParameter("address") == null || request.getParameter("address").isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.inventory.error.address"));
    				}
					if (!NetworkManager.isValidAddress(request.getParameter("address"))) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.address.invalid"));
					}
					if (request.getParameter("port") == null || request.getParameter("port").isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.inventory.error.port"));
    				}
					
					if (request.getParameterValues("typeAdvanced") == null || request.getParameterValues("typeAdvanced").length==0) {
						throw new Exception(getLanguageMessage("advanced.remotestorage.error.typeAdvanced"));
					}
					
					List<String> typesAdvanced = Arrays.asList(request.getParameterValues("typeAdvanced"));
					
					boolean edit = false;
					if (request.getParameter("edit") != null && !request.getParameter("edit").isEmpty()) {
    					edit = true;
    				}
				
					StorageInventoryManager.saveStorage(request.getParameter("name"), typesAdvanced, request.getParameter("iqnwwn"), request.getParameter("address"), request.getParameter("port"), request.getParameter("user"), request.getParameter("password"), request.getParameter("certificate"), edit);
					
					String messageUpdatedJobs = "";
					if (edit) {
						List<Map<String, Object>> jobs = GroupJobManager.getJobsWithProperty("inventory", request.getParameter("name"), _c);
						ISCSIManager iscsim = new ISCSIManager(_c);
						if (jobs != null && !jobs.isEmpty()) {
							JobManager jm = new JobManager(_c);
							for (Map<String, Object> job : jobs) {
								Map<String, Object> storageInventory = StorageInventoryManager.getStorage(request.getParameter("name"));
								if (job.containsKey("typeStep") && ((String)job.get("typeStep")).equals(StepManager.TYPE_STEP_BACKUP)) {
									String typeStorage = (String) job.get("typeStorage");
		    						String fileset = StorageInventoryManager.getNameHiddenFileset((String) storageInventory.get("name"), typeStorage);
		    						if (BaculaConfiguration.existBaculaInclude("/etc/bacula/bacula-dir.conf", "@/etc/bacula/" + "filesets" + "/" + fileset + ".conf"))
		    							FileSetManager.updateExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), typeStorage, (String) job.get("name")), FileSetManager.COMPRESSION_NONE);
		    						else
		    							FileSetManager.addExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), typeStorage, (String) job.get("name")), FileSetManager.COMPRESSION_NONE);
		    						
		    						jm.setJobFileSet((String) job.get("name"), fileset);
		    						RemoteStorageManager.generateBackupRemoteScripts((String) job.get("name"), storageInventory, typeStorage, iscsim, jm);
		    						if (messageUpdatedJobs.equals(""))
		    							messageUpdatedJobs+=(String) job.get("name");
		    						else
		    							messageUpdatedJobs+=", "+(String) job.get("name");
								} else if (job.containsKey("typeStep") && ((String)job.get("typeStep")).equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
									Map<String, String> variablesValues = JobManager.getScriptVars((String) job.get("name")+"_"+RemoteStorageManager.getNameRemoteStorageScript(1)); 
									Map<String, String> step = TemplateJobManager.getTemplateJobStep((String) job.get("templateJob"), (String) job.get("step"));
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
					}
					String message = getLanguageMessage("advanced.inventory.stored");
					if (!messageUpdatedJobs.equals(""))
						message = getLanguageMessage("advanced.inventory.stored")+". "+getLanguageMessage("advanced.jobs.updated")+": "+messageUpdatedJobs;
					writeDocumentResponse(message, baseUrl);
				} break;
				
				case ADD_STORAGE: {
					printSetStorage(_xhtml_out, null);
				} break;
				
				case EDIT_STORAGE: {
					if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name"));
					}				
					printSetStorage(_xhtml_out, request.getParameter("name"));
				} break;
				
				case REMOVE_STORAGE: {
					if (request.getParameter("confirm") != null) {
						if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.inventory.error.name"));
						}
						StorageInventoryManager.removeStorage(request.getParameter("name"));
						writeDocumentResponse(getLanguageMessage("advanced.inventory.removed"), baseUrl);
					} else {
						writeDocumentQuestion(getLanguageMessage("advanced.inventory.question"), baseUrl+"?type=" + REMOVE_STORAGE + "&name=" + request.getParameter("name")+"&confirm=true", null);
					}
				} break;
				
		    }
	    } catch (Exception _ex) {
	    	if (type != STORE_STORAGE)
	    		writeDocumentError(_ex.getMessage(), baseUrl);
	    	else
	    		writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
		
	}
	
	@SuppressWarnings("unchecked")
	public void printSetStorage(PrintWriter _xhtml_out, String nameStorage) throws Exception {
		
		_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
		 if("es".equals(this.messagei18N.getLocale().getLanguage()))
			 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
		 else
			 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
		_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
		_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.multiselect.min.js\"></script>");
		_xhtml_out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery.multiselect.css\" />");
	    
		Map<String, Object> storage = null;
		if (nameStorage != null && !nameStorage.equals(""))
			storage = StorageInventoryManager.getStorage(nameStorage);

		writeDocumentBack(baseUrl);
		_xhtml_out.print("<form action=\""+baseUrl+"\" id=\"form\" name=\"form\" method=\"post\">");
		_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_STORAGE + "\"/>");
		
		printSectionHeader(_xhtml_out);
		_xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		if (storage != null && !storage.isEmpty()) {
			_xhtml_out.print(getLanguageMessage("advanced.inventory.edit"));
			_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"1\"/>");
		}
		else
			_xhtml_out.print(getLanguageMessage("advanced.inventory.new"));
		
		_xhtml_out.print("<a href=\"javascript:if ($('#form').validationEngine('validate')) submitForm(document.form.submit());\"><img src=\"/images/disk_16.png\" title=\"");
        _xhtml_out.print(getLanguageMessage("common.message.save"));
        _xhtml_out.print("\" alt=\"");
        _xhtml_out.print(getLanguageMessage("common.message.save"));
        _xhtml_out.println("\"/></a>");
        _xhtml_out.println("</h2>");
		_xhtml_out.println("<fieldset>");
		
		_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"name\">");
    	_xhtml_out.print(getLanguageMessage("advanced.inventory.name"));
    	_xhtml_out.println(": </label>");
    	String readOnly = "";
    	if (storage != null && !storage.isEmpty())
    		readOnly= " readOnly=\"readOnly\" style=\"background-color:#E6E9EA;\" ";
    	
    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" name=\"name\" "+readOnly+" value=\"");
    	if (storage != null && !storage.isEmpty()) {
    		if (storage.get("name") != null)
    			_xhtml_out.print(storage.get("name"));
    	}
    	_xhtml_out.print("\" />");
    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	
    	_xhtml_out.print("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"typeAdvanced\">");
    	_xhtml_out.print(getLanguageMessage("advanced.remotestorage.typeAdvanced"));
    	_xhtml_out.print(": </label>\n");
    	_xhtml_out.print("<select class=\"form_select\" name=\"typeAdvanced\" id=\"typeAdvanced\"  multiple=\"multiple\"  />\n");
		for (String key : RemoteStorageManager.listRemoteStorageNames()) {
			_xhtml_out.print("<option value=\""+key+"\" ");
	    	if (storage != null && !storage.isEmpty()) {
	    		if (storage.get("typesAdvanced") != null && ((List<String>)storage.get("typesAdvanced")).contains(key))
	    			_xhtml_out.print("selected=\"selected\" ");
	    	}
	    	_xhtml_out.print(">"+key+"</option>");
		}
    	
		_xhtml_out.print("</select>");
		_xhtml_out.print("<img src=\"/images/asterisk_orange_16.png\"/>");
		_xhtml_out.print("</div>");
		_xhtml_out.print("<div class=\"clear\"></div>");
    	
		_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"iqnwwn\">");
    	_xhtml_out.print(getLanguageMessage("advanced.inventory.iqnwwn"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<input class=\"validate[required] form_text\" name=\"iqnwwn\" value=\"");
    	if (storage != null && !storage.isEmpty()) {
    		if (storage.get("iqnwwn") != null)
    			_xhtml_out.print(storage.get("iqnwwn"));
    	}
    	_xhtml_out.print("\" />");
    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.print("<i>"+getLanguageMessage("advanced.inventory.storage_iqn_var")+": <b>"+StorageInventoryManager.iqnwwn_nameVar+"</b>");
    	_xhtml_out.println("</i></div>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"address\">");
    	_xhtml_out.print(getLanguageMessage("advanced.inventory.address"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<input class=\"validate[required,custom[ipv4]] form_text\" name=\"address\" value=\"");
    	if (storage != null && !storage.isEmpty()) {
    		if (storage.get("address") != null)
    			_xhtml_out.print(storage.get("address"));
    	}
    	_xhtml_out.print("\" />");
    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"port\">");
    	_xhtml_out.print(getLanguageMessage("advanced.inventory.port"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<input class=\"validate[required,custom[number]] form_text\" name=\"port\" value=\"");
    	if (storage != null && !storage.isEmpty()) {
    		if (storage.get("port") != null)
    			_xhtml_out.print(storage.get("port"));
    	}
    	_xhtml_out.print("\" />");
    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"user\">");
    	_xhtml_out.print(getLanguageMessage("advanced.inventory.user"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<input class=\"form_text\" name=\"user\" value=\"");
    	if (storage != null && !storage.isEmpty()) {
    		if (storage.get("user") != null)
    			_xhtml_out.print(storage.get("user"));
    	}
    	_xhtml_out.print("\" />");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"password\">");
    	_xhtml_out.print(getLanguageMessage("advanced.inventory.password"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.println("<input class=\"form_text\" type=\"password\" name=\"password\" value=\"");
    	if (storage != null && !storage.isEmpty()) {
    		if (storage.get("password") != null)
    			_xhtml_out.print(storage.get("password"));
    	}
    	_xhtml_out.print("\" />");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	
    	_xhtml_out.println("<div class=\"standard_form\">");
    	_xhtml_out.print("<label for=\"certificate\">");
    	_xhtml_out.print(getLanguageMessage("advanced.inventory.certificate"));
    	_xhtml_out.println(": </label>");
    	_xhtml_out.print("<textarea class=\"form_textarea\" name=\"certificate\" cols=\"50\" rows=\"5\" wrap=\"off\">");
    	if (storage != null && storage.get("certificate")!=null)
    		_xhtml_out.print(storage.get("certificate"));
    	_xhtml_out.print("</textarea>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
    	
    	_xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"></div>");
     	_xhtml_out.println("</div>");
     	_xhtml_out.print("</form>");
     	
     	pageJS+= "$('#form').validationEngine();";
     	pageJS+="$('#typeAdvanced').multiselect({height: 120, checkAllText:'"+getLanguageMessage("common.message.all")+"',uncheckAllText:'"+getLanguageMessage("common.message.none")+"',noneSelectedText:'"+getLanguageMessage("common.message.selectOptions")+"',selectedText:'# "+getLanguageMessage("common.message.selected")+"', selectedList: 2});\n";
    }
	
 	public void printSectionHeader(PrintWriter _xhtml_out) throws Exception {
 		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/advanced_inventory_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.remoteinventory"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(getLanguageMessage("advanced.iventory.info"));
		_xhtml_out.println("</div>");
 	}
 	
 	private String getJqGridJS(String tableId, String divId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid({\n");
 		sb.append("		datatype: \"local\",\n");
 		sb.append("		colNames:['"+getLanguageMessage("advanced.inventory.name")+"','"+getLanguageMessage("advanced.remotestorage.typeAdvanced")+"','"+getLanguageMessage("advanced.inventory.address")+"','"+getLanguageMessage("advanced.inventory.port")+"','"+getLanguageMessage("advanced.inventory.iqnwwn")+"','--'],\n");
 		sb.append("		colModel:[ {name:'name',index:'name', width:50}, \n");
 		sb.append("					{name:'typeAdvanced',index:'type', width:30},\n");
 		sb.append("					{name:'address',index:'address', width:20},\n");
 		sb.append("					{name:'port',index:'port', width:10, sorttype:'int'},\n");
 		sb.append("					{name:'iqnwwn',index:'iqnwwn', width:20},\n");
 		sb.append("					{name:'actions',index:'actions', width:10, sortable:false, search:false}],\n");
 		sb.append("		width: $('#"+divId+"').width(),\n");
 		sb.append("		height: 'auto',\n");
 		sb.append("		rownumbers: true,\n");
 		sb.append("		hidegrid:false,\n");
 		sb.append("		multiselect: false,\n");
 		sb.append("		rowNum: 10,\n");
 		sb.append("		rowList : [5,10,25,50],\n");
 		sb.append("		gridComplete: LoadComplete,\n");
 		sb.append("		pager: '#pager',\n");
 		sb.append("		caption: '"+getLanguageMessage("advanced.inventories")+"',\n");
 		sb.append("		emptyDataText: '"+getLanguageMessage("advanced.inventory.no_inventory")+"',\n");
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
        		
        		String typeAdvanced = "";
        		if (storage.get("typesAdvanced") != null) {
        			@SuppressWarnings("unchecked")
					List<String> typesAdvanced = (List<String>)storage.get("typesAdvanced");
        			if (!typesAdvanced.isEmpty()) {
        				for (String t : typesAdvanced) {
        					if (typeAdvanced.equals(""))
        						typeAdvanced+=t;
        					else
        						typeAdvanced+=", "+t;
        				}
        			}
        		}
        		
 				sb.append("{\"name\":\""+storage.get("name")+"\",\"typeAdvanced\":\""+typeAdvanced+"\",\"address\":\""+storage.get("address")+"\",\"port\":\""+storage.get("port")+"\",\"iqnwwn\":\""+storage.get("iqnwwn")+"\", \"actions\":\""+actions.toString()+"\"}");
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
