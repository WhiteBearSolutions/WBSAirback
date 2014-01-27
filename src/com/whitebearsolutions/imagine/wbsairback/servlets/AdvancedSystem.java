package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.net.URLCodec;

import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.SysAppsInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ReferenceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;


public class AdvancedSystem extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public static final int SYSTEM_EDIT = 2;
	private static final int SYSTEM_STORE = 3;
	public static final int SYSTEM_REMOVE = 4;
	
	public final static String baseUrl = "/admin/"+AdvancedSystem.class.getSimpleName();
	public final static String addrScriptProcess = "/admin/AdvancedScriptProcess";

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
	    	
	    	URLCodec encoder = new URLCodec("UTF-8");
			
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
				    
	                List<SystemRs> _systems = _sam.listSystem();
	                _xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
	                _xhtml_out.print("<div id=\"systems\" style=\"clear:both;width:100%;margin:auto;\"><table id='tableSys' style=\"margin-left:0px;margin-right:0px;\"></table></div>");
	                _xhtml_out.println("</div>");
                    
    	    		pageJS +="$.jgrid.no_legacy_api = true;\n$.jgrid.useJSON = true;";
    	    		printJSON(_systems, _sam);
    	    		printJQQridJs();
    	    		pageJSFuncs+=emptyGridFuncJS("tableSys");
				} break;

	    		case SYSTEM_EDIT: {    
	    			writeDocumentHeader();
					boolean _new=false;
					if(request.getParameter("systemName") == null || request.getParameter("systemName").isEmpty()) {
						_new=true;
					}
					
					_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
	    			 if("es".equals(this.messagei18N.getLocale().getLanguage()))
	    				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
	    			 else
	    				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
	    			_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
	    			
					writeDocumentBack(baseUrl);
					printSectionHeader(_xhtml_out);
					
					_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"systems\" id=\"systems\" method=\"post\">");
					_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SYSTEM_STORE + "\"/>");
				    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
				    if (!_new){
				    	_xhtml_out.println("<input type=\"hidden\" name=\"systemName\" value=\"" + request.getParameter("systemName") + "\"/>");
				    	_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"1\"/>");
				    }
		            _xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					if (!_new){
						_xhtml_out.print(request.getParameter("systemName"));
					}else{
						_xhtml_out.print(getLanguageMessage("common.menu.advanced.systems.new"));
						}
					_xhtml_out.print("<a href=\"javascript:if ($('#systems').validationEngine('validate')) submitForm(document.systems.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.systems.name"));
			    	_xhtml_out.println(": </label>");
			    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" name=\"systemName\" value=\"");
			    	_xhtml_out.print(request.getParameter("systemName")!=null && !request.getParameter("systemName").isEmpty() ? request.getParameter("systemName") : "");
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
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.systems.description"));
			    	_xhtml_out.println(": </label>");
			    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"description\" value=\"");
			    	_xhtml_out.print(request.getParameter("description")!=null ? request.getParameter("description") : (_sam.getSystem(request.getParameter("systemName"))!=null && _sam.getSystem(request.getParameter("systemName")).getDescription() != null ? _sam.getSystem(request.getParameter("systemName")).getDescription() : "" ));
			    	_xhtml_out.print("\"/>");
			    	_xhtml_out.println("</div>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("</fieldset>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("</div>");
			    	_xhtml_out.println("</form>");
			    	
			    	pageJS+= "$('#systems').validationEngine();";
		    	}
				break;
	    		case SYSTEM_STORE: {
	    			if (request.getParameter("systemName") == null || request.getParameter("systemName").isEmpty()) {
	    				throw new Exception(getLanguageMessage("common.menu.advanced.systems.no_name"));
	    			}
					if (!request.getParameter("systemName").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
					}
					boolean edit = false;
					if (request.getParameter("edit") != null && !request.getParameter("edit").isEmpty()) {
    					edit = true;
    				}
					_sam.saveSystem(request.getParameter("systemName"), request.getParameter("description")!=null ? request.getParameter("description") : "", edit);
	    			response.sendRedirect(baseUrl);
	    			this.redirected=true;
	    		}
				break;
	    		case SYSTEM_REMOVE : {
	    			writeDocumentHeader();
	    			if(request.getParameter("confirm") != null) {
	    				String _finalName=new String (encoder.decode(request.getParameter("systemName"),"UTF-8"));
	    				_sam.deleteSystem(_finalName, _spm);
			    		writeDocumentResponse(getLanguageMessage("common.menu.advanced.systems.sys_removed"), baseUrl);
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("common.menu.advanced.systems.removed_question"), baseUrl+"?type=" + SYSTEM_REMOVE + "&systemName=" + request.getParameter("systemName") + "&confirm=true", null);
	    			}
    			}
	    		break;
		    	}
	    } catch (Exception _ex) {
	    	if (type != SYSTEM_STORE)
	    		writeDocumentError(_ex.getMessage(), baseUrl);
	    	else
	    		writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
		
	}
	
 	public void printSectionHeader(PrintWriter _xhtml_out) throws Exception {
 		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/system_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.systems"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(getLanguageMessage("common.menu.advanced.systems.info"));
		_xhtml_out.println("</div>");
 	}
 	
 	public void printJQQridJs() throws Exception {

 		pageJS+="grid = $('#tableSys');\n";
 		pageJS+="grid.jqGrid({\n";
 		pageJS+="datastr: jsonsys,\n";
        pageJS+="datatype: 'jsonstring',\n";
 		pageJS+="height: 'auto',\n";
 		pageJS+="colNames: ['"+getLanguageMessage("common.menu.advanced.systems.name")+"','"+getLanguageMessage("common.menu.advanced.systems.description")+"','--'],\n";
 		pageJS+="colModel: [\n";
 		pageJS+="{name: 'name', width:250, resizable: true},\n";
 		pageJS+="{name: 'description',width:450,resizable:true},\n";
 		pageJS+="{name: 'actions',width:100,resizable:false, sortable:false}\n";
 		pageJS+="],\n";
 		pageJS+="width: $('#systems').width(),\n";
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
 		pageJS+="caption:'"+getLanguageMessage("common.menu.advanced.systems")+"',\n";
 		pageJS+="gridComplete: LoadComplete,\n";
 		pageJS+="emptyDataText: '"+getLanguageMessage("common.menu.advanced.systems.no_systems")+"',\n";
 		pageJS+="onSelectRow: function(rowid, status) {\n";
 		pageJS+="$('#tableSys').resetSelection();\n";
 		pageJS+="}\n";
 		pageJS+="});\n";
 		pageJS+="jQuery('#tableSys')\n";
 		pageJS+="		.navGrid('#tableSys_toppager',{edit:false,add:false,del:false,search:false,refresh:false,position: 'right'})\n";
 		pageJS+="		.navButtonAdd('#tableSys_toppager_right',{\n";
 		pageJS+="			caption:'"+getLanguageMessage("common.message.add")+"',\n"; 
 		pageJS+="			buttonicon:'jq-ui-icon-add',\n"; 
 		pageJS+="			onClickButton: function(){\n"; 
 		pageJS+="				window.location.href='"+baseUrl+"?type="+SYSTEM_EDIT+"';\n";
 		pageJS+="			},\n";
 		pageJS+="			position:'first'\n";
 		pageJS+="		})\n";
 		pageJS+="		.navButtonAdd('#tableSys_toppager',{\n";
 		pageJS+="			caption:'"+getLanguageMessage("common.message.refresh")+"',\n"; 
 		pageJS+="			buttonicon:'jq-ui-icon-refresh',\n"; 
 		pageJS+="			onClickButton: function(){\n"; 
 		pageJS+="				document.location.reload();\n";
 		pageJS+="			},\n";
 		pageJS+="			position:'first'\n";
 		pageJS+="});\n";
 		pageJS+="$('#tableSys_toppager').find('.jq-ui-icon-add').css({'background-image':'url(\"/images/add_16.png\")', 'background-position':'0'});\n";
 		pageJS+="$('#tableSys_toppager').find('.jq-ui-icon-refresh').css({'background-image':'url(\"/images/arrow_refresh_16.png\")', 'background-position':'0'});\n";
 		pageJS+="$('#tableSys_toppager').find('.navtable').css({'float':'right'});\n";
 	}
 	
 	public String getActionsSys(String name) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<div style=\"padding-top:5px;\">");
 		if (!name.equals(SysAppsInventoryManager.GENERIC_SYSTEM_NAME)) {
	 		sb.append("<a href=\""+baseUrl+"?type=" + SYSTEM_EDIT + "&systemName="+name+"&servletSource="+baseUrl+"\"><img src=\"/images/edit_16.png\" title=\"");
	 		sb.append(getLanguageMessage("common.message.edit"));
	 		sb.append("\" alt=\"");
	 		sb.append(getLanguageMessage("common.message.edit"));
	 		sb.append("\"/></a> ");
	 		sb.append("<a href=\""+baseUrl+"?type=");
	 		sb.append(SYSTEM_REMOVE);
	 		sb.append("&systemName=");
	 		sb.append(name);
	 		sb.append("\"><img src=\"/images/delete_16.png\" title=\"");
	 		sb.append(getLanguageMessage("common.message.remove"));
	 		sb.append("\" alt=\"");
	 		sb.append(getLanguageMessage("common.message.remove"));
	 		sb.append("\"/></a> ");
 		}
 		sb.append("<a href=\""+addrScriptProcess+"?type=" + AdvancedScriptProcess.SCRIPT_EDIT +"&system="+name+"&servletSource="+baseUrl+"\"><img src=\"/images/script_process_add.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.add"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.add"));
 		sb.append("\"/></a></div>");
 		return sb.toString();
 	}
 	
 	public String getActionsApp(String appName, String sysName) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("<div style=\"padding-top:5px;\"><a href=\""+AdvancedApplication.baseUrl+"?type=" + AdvancedApplication.APPLICATIONS_EDIT + "&appName="+appName+"\"><img src=\"/images/edit_16.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.edit"));
 		sb.append("\"/></a> ");
 		sb.append("<a href=\""+AdvancedApplication.baseUrl+"?type=");
 		sb.append(AdvancedApplication.APPLICATIONS_REMOVE);
 		sb.append("&appName=");
 		sb.append(appName);
 		sb.append("\"><img src=\"/images/delete_16.png\" title=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\" alt=\"");
 		sb.append(getLanguageMessage("common.message.remove"));
 		sb.append("\"/></a> <a href=\""+addrScriptProcess+"?type=" + AdvancedScriptProcess.SCRIPT_EDIT +"&application="+appName+"&system="+sysName+"&servletSource="+baseUrl+"\"><img src=\"/images/script_process_add.png\" title=\"");
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
 	
 	
 	public void printJSON(List<SystemRs> systems, SysAppsInventoryManager saim) throws Exception {
 		if(systems != null && !systems.isEmpty()) {
 			pageJS+="var jsonsys={\n";
 	 		pageJS+="	'response': [\n";
 	 		int idSys = 1;
        	for (SystemRs sys : systems) {
	        		pageJS+="		{\n";
	         		pageJS+="			'id': '"+idSys+"',\n";
	         		pageJS+="			'name': '<img src=\"/images/system_16.png\" />  "+sys.getName()+"',\n";
	         		pageJS+="			'description': '"+sys.getDescription()+"',\n";
	         		pageJS+="			'actions': '"+getActionsSys(sys.getName())+"',\n";
	         		String isLeaf = "false";
	 				if ( (sys.getApplications() == null || sys.getApplications().isEmpty()) && (sys.getScripts() == null || sys.getScripts().isEmpty()) )
	 					isLeaf = "true";
	         		pageJS+="			level:'0', parent:null, isLeaf:"+isLeaf+", expanded:false, loaded:true\n";
	         		pageJS+="		},\n";
	         		
	         		int idScript = 1;
	 				if (sys.getScripts() != null && !sys.getScripts().isEmpty()) {
	        			for (ReferenceRs script : sys.getScripts()) {
	         				pageJS+="		{\n";
	         				pageJS+="			'id': '"+idSys+"_"+idScript+"',\n";
	         				pageJS+="			'name': '<img src=\"/images/script_process_16.png\" />  "+script.getName()+"',\n";
	         				pageJS+="			'description': '',\n";
	         				pageJS+="			'actions': '"+getActionsScript(script.getName())+"',\n";
	         				pageJS+="			level:'1', parent:'"+idSys+"', isLeaf:true, expanded:false, loaded:true\n";
	         				pageJS+="		},\n";
	         				idScript++;
	        			}
	 				}
	 				
	 				int idApp = idScript;
	         		if (sys.getApplications() != null && !sys.getApplications().isEmpty()) {
	         			for (ReferenceRs app : sys.getApplications()) {
	         				pageJS+="		{\n";
	         				pageJS+="			'id': '"+idSys+"_"+idApp+"',\n";
	         				pageJS+="			'name': '<div style=\"padding-top:5px;padding-bottom:5px;\"><img src=\"/images/application_16.png\" />  "+app.getName()+"</div>',\n";
	         				pageJS+="			'description': ' ',\n";
	         				pageJS+="			'actions': '"+getActionsApp(app.getName(), sys.getName())+"',\n";
	         				isLeaf = "false";
	         				List<ReferenceRs> scripts = saim.getApplicationScripts(app.getName(), sys.getName());
	         				if (scripts == null || scripts.isEmpty() )
	         					isLeaf = "true";
	         				pageJS+="			level:'1', parent:'"+idSys+"', isLeaf: "+isLeaf+", expanded:false, loaded:true\n";
	         				pageJS+="		},\n";
	         				
	         				
	         				if (scripts != null && !scripts.isEmpty()) {
	         					int idAppScript = 1;
	         					for (ReferenceRs script : scripts) {
	                 				pageJS+="		{\n";
	                 				pageJS+="			'id': '"+idSys+"_"+idApp+"_"+idAppScript+"',\n";
	                 				pageJS+="			'name': '<div style=\"padding-top:5px;padding-bottom:5px;\"><img src=\"/images/script_process_16.png\" />  "+script.getName()+"</div>',\n";
	                 				pageJS+="			'description': ' ',\n";
	                 				pageJS+="			'actions': '"+getActionsScript(script.getName())+"',\n";
	                 				pageJS+="			level:'2', parent:'"+idSys+"_"+idApp+"', isLeaf:true, expanded:false, loaded:true\n";
	                 				pageJS+="		},\n";
	                 				idAppScript++;
	         					}
	         				}
	
	         				idApp++;
	         			}
	         		}
	         		
	         		idSys++;
        	}
        	pageJS+="	]\n";
     		pageJS+="}, grid;\n";
 		} else {
 			pageJS+="var jsonsys={};\n";
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
