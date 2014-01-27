package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.SysAppsInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ApplicationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ReferenceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;


public class AdvancedApplication extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public static final int APPLICATIONS_EDIT = 2;
	private static final int APPLICATIONS_STORE = 3;
	public static final int APPLICATIONS_REMOVE = 4;
	
	public final static String baseUrl = "/admin/"+AdvancedApplication.class.getSimpleName();
	public final static String addrScriptProcess = "/admin/AdvancedScriptProcess";
	
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
	    	
	    	SysAppsInventoryManager _sam = new SysAppsInventoryManager();
	    	ScriptProcessManager _spm = new ScriptProcessManager();
			
	    	switch(this.type) {
    		
				default: {
					writeDocumentHeader();
					printSectionHeader(_xhtml_out);
					
					_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />");
					if("es".equals(this.messagei18N.getLocale().getLanguage()))
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>");
					else
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>");
				    _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>");
					
        	    	List<ApplicationRs> _apps = _sam.listApps();
        			_xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
                    _xhtml_out.print("<div id=\"applications\" style=\"clear:both;width:100%;margin:auto;\"><table id='tableApp' style=\"margin-left:0px;margin-right:0px;\"></table></div>");
                    _xhtml_out.println("</div>");
                    pageJS +="$.jgrid.no_legacy_api = true;\n$.jgrid.useJSON = true;";
         	        printJSON(_apps, _sam);
         	        printJQQridJs();
         	        pageJSFuncs+=emptyGridFuncJS("tableApp");
				} break;
				
				case APPLICATIONS_EDIT: { 
					writeDocumentHeader();
	    			boolean _new=false;
	    			ApplicationRs app = null;
	    			if(request.getParameter("appName") == null || request.getParameter("appName").isEmpty()) {
	    				_new=true;
	    			} else
	    				app = _sam.getApplication(request.getParameter("appName"));
	    			
	    			_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
	    			 if("es".equals(this.messagei18N.getLocale().getLanguage()))
	    				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
	    			 else
	    				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
	    			_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
	    			_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.multiselect.min.js\"></script>");
	    			_xhtml_out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery.multiselect.css\" />");
	    			
	    			writeDocumentBack(baseUrl);
	    			printSectionHeader(_xhtml_out);
	    			
	    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"applications\" id=\"applications\" method=\"post\">");
	    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + APPLICATIONS_STORE + "\"/>");
				    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
				    if (!_new){
				    	_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"1\"/>");
				    	_xhtml_out.println("<input type=\"hidden\" name=\"appName\" value=\"" + request.getParameter("appName") + "\"/>");
				    }
	                _xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					if (!_new){
						_xhtml_out.print(request.getParameter("appName"));
					}else{
						_xhtml_out.print(getLanguageMessage("common.menu.advanced.applications.new"));
						}
					_xhtml_out.print("<a href=\"javascript:if ($('#applications').validationEngine('validate')) submitForm(document.applications.submit());\"><img src=\"/images/disk_16.png\" title=\"");
					_xhtml_out.print(getLanguageMessage("common.message.save"));
	               	_xhtml_out.print("\" alt=\"");
	               	_xhtml_out.print(getLanguageMessage("common.message.save"));
	               	_xhtml_out.println("\"/></a>");
	                _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	            	_xhtml_out.print("\" alt=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	            	_xhtml_out.println("\"/></a>");
	                _xhtml_out.println("</h2>");
	                _xhtml_out.println("<fieldset>");
	    	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	    	_xhtml_out.print("<label for=\"port\">");
	    	    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.applications.name"));
	    	    	_xhtml_out.println(": </label>");
	    	    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" name=\"appName\" value=\"");
	    	    	_xhtml_out.print(request.getParameter("appName")!=null && !request.getParameter("appName").isEmpty() ? request.getParameter("appName") : "");
	    	    	_xhtml_out.print("\" ");
	    	    	if (!_new){
	    	    		_xhtml_out.print(" disabled=\"disabled\" ");
	    	    	}
	    	    	_xhtml_out.print("/>");
	    	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	    	_xhtml_out.println("</div>");
	    	    	_xhtml_out.println("<div class=\"clear\"></div>");
	    	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	    	_xhtml_out.print("<label for=\"port\">");
	    	    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.applications.description"));
	    	    	_xhtml_out.println(": </label>");
	    	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"description\" value=\"");
	    	    	_xhtml_out.print(request.getParameter("description")!=null ? request.getParameter("description") : (app != null && app.getDescription() != null ? app.getDescription() : "" ));
	    	    	_xhtml_out.print("\"/>");
			    	_xhtml_out.println("</div>");	
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	
			    	List<SystemRs>_listSystem=_sam.listSystem();
			    	_xhtml_out.println("<div class=\"standard_form\">");
			    	_xhtml_out.print("<label for=\"system\">");
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.applications.system"));
			    	_xhtml_out.println(": </label>");
			    	_xhtml_out.println("<select class=\"form_select\" id=\"system\" name=\"system\" multiple=\"multiple\"/>");
			    	if (_listSystem!=null && !_listSystem.isEmpty()){
				    	for (SystemRs sys : _listSystem) {
				    		String _systemName=sys.getName();
				    		_xhtml_out.print("<option value=\""+_systemName+"\" ");
					    	if (app != null) {
					    		if (app.hasSystem(sys.getName()))
						    		_xhtml_out.print("selected=\"selected\" ");
					    	}
					    	_xhtml_out.print(">"+_systemName+"</option>");
				    	}
			    	}
			    	_xhtml_out.println("</select>");
			    	_xhtml_out.println("</div>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	
			    	_xhtml_out.println("</fieldset>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("</div>");
	    	    	_xhtml_out.println("</form>");
	    	    	
	    	    	pageJS+= "$('#applications').validationEngine();";
	    	    	pageJS+="$('#system').multiselect({height: 120, checkAllText:'"+getLanguageMessage("common.message.all")+"',uncheckAllText:'"+getLanguageMessage("common.message.none")+"',noneSelectedText:'"+getLanguageMessage("common.message.selectOptions")+"',selectedText:'# "+getLanguageMessage("common.message.selected")+"', selectedList: 2});\n";
		    	}
	    		break;
	    		case APPLICATIONS_STORE: {
		    			if (request.getParameter("appName") == null || request.getParameter("appName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("common.menu.advanced.applications.no_name"));
		    			}
						if (!request.getParameter("appName").matches("[0-9a-zA-Z-._]+")) {
							throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
						}
						boolean edit = false;
						if (request.getParameter("edit") != null && !request.getParameter("edit").isEmpty()) {
	    					edit = true;
	    				}
						
						if (request.getParameterValues("system") == null || request.getParameterValues("system").length==0) {
							throw new Exception(getLanguageMessage("advanced.inventory.applications.system"));
						}
						
						List<String> systems = Arrays.asList(request.getParameterValues("system"));
						
						_sam.saveApps(request.getParameter("appName"), request.getParameter("description")!=null ? request.getParameter("description") : "", systems, edit);
		    			response.sendRedirect(baseUrl);
		    			this.redirected=true;
	    		}
				break;
	    		case APPLICATIONS_REMOVE: {
	    				writeDocumentHeader();
		    			if(request.getParameter("confirm") != null) {
		    				_sam.deleteApplication(request.getParameter("appName"), _spm);
				    		writeDocumentResponse(getLanguageMessage("common.menu.advanced.applications.app_removed"), baseUrl);
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("common.menu.advanced.applications.removed_question"), baseUrl+"?type=" + APPLICATIONS_REMOVE + "&appName=" + request.getParameter("appName") + "&confirm=true", null);
		    			}
	    			}
				break;
	    	}
	    } catch (Exception _ex) {
	    	if (type != APPLICATIONS_STORE)
	    		writeDocumentError(_ex.getMessage(), baseUrl);
	    	else 
	    		writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
		
	}
	
 	public void printSectionHeader(PrintWriter _xhtml_out) throws Exception {
 		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/application_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.applications"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(getLanguageMessage("common.menu.advanced.applications.info"));
		_xhtml_out.println("</div>");
 	}

 	public void printJQQridJs() throws Exception {

 		pageJS+="grid = $('#tableApp');\n";
 		pageJS+="grid.jqGrid({\n";
 		pageJS+="datastr: jsonapps,\n";
        pageJS+="datatype: 'jsonstring',\n";
 		pageJS+="height: 'auto',\n";
 		pageJS+="colNames: ['"+getLanguageMessage("common.menu.advanced.applications.name")+"','"+getLanguageMessage("common.menu.advanced.applications.description")+"','--'],\n";
 		pageJS+="colModel: [\n";
 		pageJS+="{name: 'name', width:250, resizable: true},\n";
 		pageJS+="{name: 'description',width:450,resizable:true},\n";
 		pageJS+="{name: 'actions',width:100,resizable:false, sortable:false}\n";
 		pageJS+="],\n";
 		pageJS+="width: $('#applications').width(),\n";
 		pageJS+="treeGrid: true,\n";
 		pageJS+="treeGridModel: 'adjacency',\n";
 		pageJS+="ExpandColumn: 'name',\n";
 		pageJS+="ExpandColClick: true,\n";
 		pageJS+="jsonReader: {\n";
 		pageJS+="repeatitems: false,\n";
 		pageJS+="root: 'response',\n";
 		pageJS+="},\n";
 		pageJS+="toppager:true,\n";
 		pageJS+="hidegrid:false,\n";
 		pageJS+="caption:'"+getLanguageMessage("common.menu.advanced.applications")+"',\n";
 		pageJS+="gridComplete: LoadComplete,\n";
 		pageJS+="emptyDataText: '"+getLanguageMessage("common.menu.advanced.applications.no_applications")+"',\n";
 		pageJS+="onSelectRow: function(rowid, status) {\n";
 		pageJS+="$('#tableApp').resetSelection();\n";
 		pageJS+="}\n";
 		pageJS+="});\n";
 		pageJS+="jQuery('#tableApp')\n";
 		pageJS+="		.navGrid('#tableApp_toppager',{edit:false,add:false,del:false,search:false,refresh:false,position: 'right'})\n";
 		pageJS+="		.navButtonAdd('#tableApp_toppager_right',{\n";
 		pageJS+="			caption:'"+getLanguageMessage("common.message.add")+"',\n"; 
 		pageJS+="			buttonicon:'jq-ui-icon-add',\n"; 
 		pageJS+="			onClickButton: function(){\n"; 
 		pageJS+="				window.location.href='"+baseUrl+"?type="+APPLICATIONS_EDIT+"';\n";
 		pageJS+="			},\n";
 		pageJS+="			position:'first'\n";
 		pageJS+="		})\n";
 		pageJS+="		.navButtonAdd('#tableApp_toppager',{\n";
 		pageJS+="			caption:'"+getLanguageMessage("common.message.refresh")+"',\n"; 
 		pageJS+="			buttonicon:'jq-ui-icon-refresh',\n"; 
 		pageJS+="			onClickButton: function(){\n"; 
 		pageJS+="				document.location.reload();\n";
 		pageJS+="			},\n";
 		pageJS+="			position:'first'\n";
 		pageJS+="});\n";
 		pageJS+="$('#tableApp_toppager').find('.jq-ui-icon-add').css({'background-image':'url(\"/images/add_16.png\")', 'background-position':'0'});\n";
 		pageJS+="$('#tableApp_toppager').find('.jq-ui-icon-refresh').css({'background-image':'url(\"/images/arrow_refresh_16.png\")', 'background-position':'0'});\n";
 		pageJS+="$('#tableApp_toppager').find('.navtable').css({'float':'right'});\n";
 	}
 	
 	public String getActionsApp(String appName) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<div style=\"padding-top:5px;\"><a href=\""+baseUrl+"?type=" + APPLICATIONS_EDIT + "&appName="+appName+"\"><img src=\"/images/edit_16.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\"/></a> ");
 		sb.append("<a href=\""+baseUrl+"?type=");
 		sb.append(APPLICATIONS_REMOVE);
 		sb.append("&appName=");
 		sb.append(appName);
 		sb.append("\"><img src=\"/images/delete_16.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\"/></a></div>");
 		return sb.toString();
 	}
 	
 	public String getActionsSystem(String appName, String sysName) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<div style=\"padding-top:5px;\"><a href=\""+AdvancedSystem.baseUrl+"?type=" + AdvancedSystem.SYSTEM_EDIT + "&systemName="+sysName+"&servletSource="+baseUrl+"\"><img src=\"/images/edit_16.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\"/></a> ");
 		sb.append("<a href=\""+baseUrl+"?type=");
 		sb.append(AdvancedSystem.SYSTEM_REMOVE);
 		sb.append("&systemName=");
 		sb.append(sysName);
 		sb.append("\"><img src=\"/images/delete_16.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\"/></a> ");
 		sb.append("<a href=\""+addrScriptProcess+"?type=" + AdvancedScriptProcess.SCRIPT_EDIT +"&application="+appName+"&system="+sysName+"&servletSource="+baseUrl+"\"><img src=\"/images/script_process_add.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.add"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.add"));
 		sb.append("\"/></a></div>");
 		return sb.toString();
 	}
 	
 	public String getActionsScript(String scriptName) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<div style=\"padding-top:5px;\"><a href=\""+addrScriptProcess+"?type=" + AdvancedScriptProcess.SCRIPT_EDIT + "&scriptName="+scriptName+"&servletSource="+baseUrl+"\"><img src=\"/images/script_process_edit.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\"/></a> ");
 		sb.append("<a href=\""+addrScriptProcess+"?type=");
 		sb.append(AdvancedScriptProcess.SCRIPT_REMOVE);
 		sb.append("&scriptName=");
 		sb.append(scriptName);
 		sb.append("&servletSource="+baseUrl+"\"><img src=\"/images/script_process_delete.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\"/></a></div>");
 		return sb.toString();
 	}
 	
 	
 	public void printJSON(List<ApplicationRs> apps, SysAppsInventoryManager saim) throws Exception {
 		if(apps != null && !apps.isEmpty()) {
 			pageJS+="var jsonapps={\n";
 	 		pageJS+="	'response': [\n";
 	 		int idApp = 1;
        	for (ApplicationRs app : apps) {
        		pageJS+="		{\n";
         		pageJS+="			'id': '"+idApp+"',\n";
         		pageJS+="			'name': '<img src=\"/images/application_16.png\" />  "+app.getName()+"',\n";
         		pageJS+="			'description': '"+app.getDescription()+"',\n";
         		pageJS+="			'actions': '"+getActionsApp(app.getName())+"',\n";
         		String isLeaf = "false";
 				if (app.getSystems() == null || app.getSystems().isEmpty())
 					isLeaf = "true";
         		pageJS+="			level:'0', parent:null, isLeaf:"+isLeaf+", expanded:false, loaded:true\n";
         		pageJS+="		},\n";
         		
         		if (app.getSystems() != null && !app.getSystems().isEmpty()) {
         			int idSys = 1;
         			for (SystemRs sys : app.getSystems()) {
         				pageJS+="		{\n";
         				pageJS+="			'id': '"+idApp+"_"+idSys+"',\n";
         				pageJS+="			'name': '<img src=\"/images/system_16.png\" />  "+sys.getName()+"',\n";
         				String description = "";
         				if (sys.getDescription() != null && !sys.getDescription().isEmpty())
         					description = sys.getDescription();
         				pageJS+="			'description': '"+description+"',\n";
         				pageJS+="			'actions': '"+getActionsSystem(app.getName(), sys.getName())+"',\n";
         				isLeaf = "false";
         				if (sys.getScripts() == null || sys.getScripts().isEmpty())
         					isLeaf = "true";
         				pageJS+="			level:'1', parent:'"+idApp+"', isLeaf:"+isLeaf+", expanded:false, loaded:true\n";
         				pageJS+="		},\n";
         				
         				if (sys.getScripts() != null && !sys.getScripts().isEmpty()) {
         					int idScript = 1;
                			for (ReferenceRs script : sys.getScripts()) {
                 				pageJS+="		{\n";
                 				pageJS+="			'id': '"+idApp+"_"+idSys+"_"+idScript+"',\n";
                 				pageJS+="			'name': '<img src=\"/images/script_process_16.png\" />  "+script.getName()+"',\n";
                 				pageJS+="			'description': '',\n";
                 				pageJS+="			'actions': '"+getActionsScript(script.getName())+"',\n";
                 				pageJS+="			level:'2', parent:'"+idApp+"_"+idSys+"', isLeaf:true, expanded:false, loaded:true\n";
                 				pageJS+="		},\n";
                 				idScript++;
                			}
         				}
         				idSys++;
         			}
         			
         		}
         		idApp++;
        	}
        	pageJS+="	]\n";
     		pageJS+="}, grid;\n";
 		} else {
 			pageJS+="var jsonapps={};\n";
 		}
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
}
