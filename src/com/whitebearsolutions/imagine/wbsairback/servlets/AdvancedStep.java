package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;


public class AdvancedStep extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public final static int ADD_STEP = 9;
	public final static int EDIT_STEP = 10;
	public final static int REMOVE_STEP = 11;
	public final static int STORE_STEP = 12;
	public final static int UPDATE_STEP_ORDER = 13;
	
	public final static String baseUrl = "/admin/"+AdvancedStep.class.getSimpleName();
	
	Map<String, String> selectTypeStep = null;

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
			
			fillSelects();
			
	    	switch(this.type) {
	    		
    			default: {
    				_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />");
    				if("es".equals(this.messagei18N.getLocale().getLanguage()))
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>");
					else
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>");
				    _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>");
				    
    				printSectionHeader(_xhtml_out, getLanguageMessage("advanced.step.info"));
		    			
	    			List<Map<String, String>> _steps = StepManager.listSteps();
	    			_xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
	                _xhtml_out.println("<div id=\"listadoSteps\" style=\"clear:both;width:100%;margin:auto;\"><table id=\"tablaSteps\" style=\"margin-left:0px;margin-right:0px;\"></table><div id='pager' ></div></div>");
		        	_xhtml_out.print("</div>");
	    			pageJS+=getJqGridJS("tablaSteps", "listadoSteps");
	                pageJS+="reloadAll();\n";
	                pageJSFuncs+=allLoad("tablaSteps", _steps);
	                pageJSFuncs+=emptyGridFuncJS("tablaSteps");	
    			} break;
				
				case STORE_STEP: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.step.error.name"));
    				}
    				
					if (!request.getParameter("name").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
					}
    				
					if(request.getParameter("typeStep") == null || request.getParameter("typeStep").isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.step.error.type"));
    				}
					
					boolean edit = false;
					if (request.getParameter("edit") != null && !request.getParameter("edit").isEmpty()) {
    					edit = true;
    				}
					
					StepManager.saveStep(request.getParameter("name"), request.getParameter("typeStep"), edit);
					writeDocumentResponse(getLanguageMessage("advanced.step.stored"), baseUrl);
    			} break;
    			
    			case ADD_STEP: {
    				printSetStep(_xhtml_out, null);
    			} break;
    			
    			case EDIT_STEP: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
    					throw new Exception(getLanguageMessage("advanced.step.error.name"));
    				}				
    				printSetStep(_xhtml_out, request.getParameter("name"));
				} break;
				
				case REMOVE_STEP: {
					if (request.getParameter("confirm") != null) {
						if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
    						throw new Exception(getLanguageMessage("advanced.step.error.name"));
    					}
						StepManager.removeStep(request.getParameter("name"));
						writeDocumentResponse(getLanguageMessage("advanced.step.removed"), baseUrl);
					} else {
						writeDocumentQuestion(getLanguageMessage("advanced.step.question"), baseUrl+"?type=" + REMOVE_STEP + "&name=" + request.getParameter("name")+"&confirm=true", null);
					}
				} break;
	    	}
	    } catch (Exception _ex) {
	    	if (type != UPDATE_STEP_ORDER) {
		    	if (type == REMOVE_STEP)
		    		writeDocumentError(_ex.getMessage(), baseUrl);
		    	else
		    		writeDocumentError(_ex.getMessage());
	    	}
	    } finally {
	    	writeDocumentFooter();
	    }
		
	}
	
 	public void printSectionHeader(PrintWriter _xhtml_out, String info) throws Exception {
		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/step_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.step"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(info);
		_xhtml_out.println("</div>");
 	}
 	
 	
 	public void fillSelects() {
 		selectTypeStep = new TreeMap<String, String>();
		selectTypeStep.put(StepManager.TYPE_STEP_ADVANCED_STORAGE, getLanguageMessage("advanced.step.type.advanced_storage"));
		selectTypeStep.put(StepManager.TYPE_STEP_BACKUP, getLanguageMessage("advanced.step.type.backup"));
		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_APP, getLanguageMessage("advanced.step.type.script_app"));
		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_SYSTEM, getLanguageMessage("advanced.step.type.script_system"));
 	}
 	
 	 public void printSetStep(PrintWriter _xhtml_out, String nameStep) throws Exception {
			
			Map<String, String> step = null;
			if (nameStep != null && !nameStep.equals(""))
				step = StepManager.getStep(nameStep);
			
			_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />");
			 if("es".equals(this.messagei18N.getLocale().getLanguage()))
				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>");
			 else
				 _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>");
			_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>");
			
			writeDocumentBack(baseUrl);
			_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" id=\"form\" method=\"post\">");
			_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_STEP + "\"/>");
			printSectionHeader(_xhtml_out, getLanguageMessage("advanced.step.info"));
			
			_xhtml_out.println("<div class=\"window\">");
			_xhtml_out.println("<h2>");
			if (step != null && !step.isEmpty()) {
				_xhtml_out.print(getLanguageMessage("advanced.step.edit"));
				_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"1\"/>");
			} else
				_xhtml_out.print(getLanguageMessage("advanced.step.new"));
			
			_xhtml_out.print("<a href=\"javascript:if ($('#form').validationEngine('validate')) submitForm(document.form.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	        _xhtml_out.print(getLanguageMessage("common.message.save"));
	        _xhtml_out.print("\" alt=\"");
	        _xhtml_out.print(getLanguageMessage("common.message.save"));
	        _xhtml_out.println("\"/></a>");
	        _xhtml_out.println("</h2>");
			_xhtml_out.println("<fieldset>");
			
			_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"name\">");
	    	_xhtml_out.print(getLanguageMessage("advanced.step.name"));
	    	_xhtml_out.println(": </label>");
	    	
	    	if (step != null && !step.isEmpty()) {
	    		if (step.get("name") != null) {
	    			_xhtml_out.print("<input class=\"form_text\" name=\"name\" readOnly=\"readOnly\" style=\"background-color:#E6E9EA;\" value=\"");
	    			_xhtml_out.print(step.get("name"));
	    		} else {
	    			_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" name=\"name\" value=\"");
	    		}
	    	} else
	    		_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" name=\"name\" value=\"");
	    	
	    	_xhtml_out.print("\" />");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"typeAccount\">");
	    	_xhtml_out.print(getLanguageMessage("advanced.step.type"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<select class=\"form_select\" name=\"typeStep\"/>");
	    	_xhtml_out.print("<option value=\""+StepManager.TYPE_STEP_ADVANCED_STORAGE+"\" ");
	    	if (step != null && !step.isEmpty()) {
	    		if (step.get("type") != null && step.get("type").equals(StepManager.TYPE_STEP_ADVANCED_STORAGE))
		    		_xhtml_out.print("selected=\"selected\" ");
	    	}
	    	_xhtml_out.print(">"+getLanguageMessage("advanced.step.type.advanced_storage")+"</option>");
	    	_xhtml_out.print("<option value=\""+StepManager.TYPE_STEP_BACKUP+"\" ");
	    	if (step != null && !step.isEmpty()) {
	    		if (step.get("type") != null && step.get("type").equals(StepManager.TYPE_STEP_BACKUP))
		    		_xhtml_out.print("selected=\"selected\" ");
	    	}
	    	_xhtml_out.print(">"+getLanguageMessage("advanced.step.type.backup")+"</option>");
	    	_xhtml_out.print("<option value=\""+StepManager.TYPE_STEP_SCRIPT_APP+"\" ");
	    	if (step != null && !step.isEmpty()) {
	    		if (step.get("type") != null && step.get("type").equals(StepManager.TYPE_STEP_SCRIPT_APP))
		    		_xhtml_out.print("selected=\"selected\" ");
	    	}
	    	_xhtml_out.print(">"+getLanguageMessage("advanced.step.type.script_app")+"</option>");
	    	_xhtml_out.print("<option value=\""+StepManager.TYPE_STEP_SCRIPT_SYSTEM+"\" ");
	    	if (step != null && !step.isEmpty()) {
	    		if (step.get("type") != null && step.get("type").equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM))
		    		_xhtml_out.print("selected=\"selected\" ");
	    	}
	    	_xhtml_out.print(">"+getLanguageMessage("advanced.step.type.script_system")+"</option>");
	    	_xhtml_out.println("</select>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</fieldset>");
	    	
      _xhtml_out.println("<div class=\"clear\"></div>");
      _xhtml_out.println("</div>");
      _xhtml_out.print("</form>");
      
      pageJS+= "$('#form').validationEngine();";
	}
 	 
 	private String getJqGridJS(String tableId, String divId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid({\n");
 		sb.append("		datatype: \"local\",\n");
 		sb.append("		colNames:['"+getLanguageMessage("advanced.step.name")+"','"+getLanguageMessage("advanced.step.type")+"','--'],\n");
 		sb.append("		colModel:[ {name:'name',index:'name', width:50}, \n");
 		sb.append("					{name:'type',index:'type', width:20},\n");
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
 		sb.append("		caption: '"+getLanguageMessage("advanced.steps")+"',\n");
 		sb.append("		emptyDataText: '"+getLanguageMessage("advanced.step.no_step")+"',\n");
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
 		sb.append("				window.location.href='"+baseUrl+"?type="+ADD_STEP+"';\n");
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
 	
 	private String getJSONStepsJS(Set<Map<String, String>> steps) throws Exception {
 		StringBuilder sb = new StringBuilder();
		
		 if (!steps.isEmpty()) {
			 sb.append("[");
 			boolean first = true;
 			for (Map<String, String> step : steps) {
 				if (!step.get("name").equals(StepManager.GENERIC_STEP_NAME)) {
	 				if (!first)
	 					sb.append(",");
	 				else
	 					first=false;
	 				
	            	StringBuilder actions = new StringBuilder();
	                
	                actions.append("<div style='padding-top:5px;'><a href='"+baseUrl+"?type="+EDIT_STEP+"&name="+step.get("name")+"'><img src='/images/edit_16.png' title='");
	        		actions.append(getLanguageMessage("common.message.edit"));
	        		actions.append("' alt='");
	        		actions.append(getLanguageMessage("common.message.edit"));
	        		actions.append("'/></a>  ");
	        		actions.append("<a href='"+baseUrl+"?type="+REMOVE_STEP+"&name="+step.get("name")+"'><img src='/images/delete_16.png' title='");
	        		actions.append(getLanguageMessage("common.message.remove"));
	        		actions.append("' alt='");
	        		actions.append(getLanguageMessage("common.message.remove"));
	        		actions.append("'/></a>");
	        		actions.append("</div>");
	                
	        		String type = "";
	        		if (step.get("type") != null) {
	            		if (step.get("type").equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
	            			type = getLanguageMessage("advanced.step.type.advanced_storage");
	            		} else if (step.get("type").equals(StepManager.TYPE_STEP_BACKUP)) {
	            			type = getLanguageMessage("advanced.step.type.backup");
	            		} else if (step.get("type").equals(StepManager.TYPE_STEP_SCRIPT_APP)) {
	            			type = getLanguageMessage("advanced.step.type.script_app");
	            		} else if (step.get("type").equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
	            			type = getLanguageMessage("advanced.step.type.script_system");
	            		}
	            	}
	        		
	 				sb.append("{\"name\":\""+step.get("name")+"\",\"type\":\""+type+"\", \"actions\":\""+actions.toString()+"\"}");
 				}
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
 	
 	
 	private String allLoad(String tableGridId, List<Map<String, String>> steps) throws Exception{
 		StringBuilder sb = new StringBuilder();
		sb.append("function reloadAll()\n");
 		sb.append("{\n");
		if (steps != null && !steps.isEmpty()) {
			HashSet<Map<String, String>> listSteps = new HashSet<Map<String, String>>();
			listSteps.addAll(steps);

			sb.append("	var json = '"+getJSONStepsJS(listSteps).replace("'", "\\'")+"';\n");
			sb.append("	var alldata = jQuery.parseJSON(json);\n");
			sb.append("	if (alldata) {\n");
			sb.append("		for(var i=0;i<=alldata.length;i++) {jQuery(\"#"+tableGridId+"\").jqGrid('addRowData',i+1,alldata[i]);}\n");
			sb.append("	}\n");
		}
		sb.append("}\n");
		return sb.toString();
 	}

}
