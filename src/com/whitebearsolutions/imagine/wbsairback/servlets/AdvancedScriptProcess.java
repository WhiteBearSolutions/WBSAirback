package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StorageInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.SysAppsInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.util.Configuration;


public class AdvancedScriptProcess extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public static final int SCRIPT_EDIT=2;
	public static final int SCRIPT_STORE=3;
	public static final int SCRIPT_REMOVE=4;
	public static final int SCRIPT_COMBO_BY_STEP=9482638;
	
	private final static Logger logger = LoggerFactory.getLogger(AdvancedScriptProcess.class);
	
	public final static String baseUrl = "/admin/"+AdvancedScriptProcess.class.getSimpleName();

	@SuppressWarnings("unchecked")
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
			
			if (type == SCRIPT_COMBO_BY_STEP) {
				printComboScriptsByStep(_xhtml_out, (String) request.getParameter("step"), (String) request.getParameter("typeScript"));
				return;
			}
			
			response.setContentType("text/html");
			writeDocumentHeader();
			
			Configuration _c = this.sessionManager.getConfiguration();
	    	ScriptProcessManager _sm = new ScriptProcessManager();
	    	SysAppsInventoryManager saim = new SysAppsInventoryManager();
	    	
	    	Map<String, Map<String, Object>> _scripts = _sm.listScript();
	    	switch(this.type) {	    		
    			default: {
					_xhtml_out.print("<form action=\"/admin/AdvancedScriptProcess\" name=\"scriptProcess\" method=\"post\">");
	    			_xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/script_process_32.png\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script"));
    				_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("advanced.script.info"));
	    			_xhtml_out.println("</div>");
					_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SCRIPT_STORE + "\"/>");
					_xhtml_out.println("<div class=\"window\">");
	                _xhtml_out.println("<h2>");
	                _xhtml_out.println(getLanguageMessage("common.menu.advanced.script_process"));
	                _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	            	_xhtml_out.print("\" alt=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	            	_xhtml_out.println("\"/></a>");
					_xhtml_out.print("<a href=\"/admin/AdvancedScriptProcess?type=" + SCRIPT_EDIT + "\"><img src=\"/images/add_16.png\" title=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.add"));
	            	_xhtml_out.print("\" alt=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.add"));
	            	_xhtml_out.println("\"/></a>");
	                _xhtml_out.println("</h2>");
	                _xhtml_out.print("<br/>");
	                _xhtml_out.println("<fieldset>");
	                _xhtml_out.println("<table>");
	                if(_scripts!=null && !_scripts.isEmpty()) {
	                	_xhtml_out.println("<tr>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.name"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.type"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.abort_type"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.step"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.system_app"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td>&nbsp;-&nbsp;-&nbsp;</td>");
	                    _xhtml_out.println("</tr>");
	                	int _offset = 0;
	                	Set<String> _keySet=_scripts.keySet();
	                	Iterator<String> _keySetIterator=_keySet.iterator();
	                	while (_keySetIterator.hasNext()){
	                		String _key=_keySetIterator.next();
	                		_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.print("<td>");
		                    _xhtml_out.print(_key);
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td>");
		                    if (_scripts.get(_key).get("type") != null && _scripts.get(_key).get("type").equals("1"))
		                    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.before_execution"));
		                    else
		                    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.after_execution"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td>");
		                    if (_scripts.get(_key).get("abortType") != null && _scripts.get(_key).get("abortType").equals("true"))
		                    	_xhtml_out.print(getLanguageMessage("common.message.yes"));
		                    else
		                    	_xhtml_out.print(getLanguageMessage("common.message.no"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.print(_scripts.get(_key).get("step"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.print(_scripts.get(_key).get("system")!=null && !((String)_scripts.get(_key).get("system")).isEmpty() ? _scripts.get(_key).get("system") : _scripts.get(_key).get("application"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td>");
		    				_xhtml_out.print("<a href=\"/admin/AdvancedScriptProcess?type=" + SCRIPT_EDIT + "&scriptName="+_key+"\"><img src=\"/images/edit_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                	_xhtml_out.println("\"/></a>");
							_xhtml_out.print("<a href=\"/admin/AdvancedScriptProcess?type=");
							_xhtml_out.print(SCRIPT_REMOVE);
							_xhtml_out.print("&scriptName=");
							_xhtml_out.print(_key);
							_xhtml_out.print("\"><img src=\"/images/delete_16.png\" title=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
	                    	_xhtml_out.print("\" alt=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
	                    	_xhtml_out.println("\"/></a>");
		                	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset++;
	                    }
		                } else {
		                	_xhtml_out.println("<tr>");
		                	_xhtml_out.println("<td>");
		                	_xhtml_out.println(getLanguageMessage("common.menu.advanced.script_process.no_script_process"));
		                	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                }
		                _xhtml_out.println("</table>");
		                
		                _xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"></div>");
		                _xhtml_out.println("</div>");
				}
    			break;
    			case SCRIPT_EDIT: {    
					boolean _new=false;
					List<Map<String, String>> _variables=new ArrayList<Map<String, String>>();
					List<Map<String, String>> _scriptsItems=new ArrayList<Map<String, String>>();
					Map<String, Object> _script=null;
					if(request.getParameter("scriptName") == null || request.getParameter("scriptName").isEmpty()) {
						_new=true;
					} else{
						_script=_sm.getScript(request.getParameter("scriptName"));
						_variables=(List<Map<String, String>>) _script.get("variables");
						_scriptsItems=(List<Map<String, String>>) _script.get("scripts");
					}
					 
					writeDocumentBack(null);
					_xhtml_out.println("<h1>");
					_xhtml_out.print("<img src=\"/images/script_process_32.png\"/>");
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script"));
					_xhtml_out.println("</h1>");
					_xhtml_out.print("<div class=\"info\">");
					_xhtml_out.print(getLanguageMessage("advanced.script.info"));
					_xhtml_out.println("</div>");
					
					_xhtml_out.println("<form action=\"/admin/AdvancedScriptProcess\" name=\"scriptProcess\" method=\"post\">");
					_xhtml_out.println("<input type=\"hidden\" id=\"type\" name=\"type\" value=\"" + SCRIPT_STORE + "\"/>");
				    _xhtml_out.println("<input type=\"hidden\" naidme=\"modify\" name=\"modify\" value=\"yes\"/>");
				    String system = null;
				    if (_script != null && _script.get("system") != null && !((String)_script.get("system")).isEmpty())
				    	system = (String) _script.get("system");
				    else if (request.getParameter("system")!=null && !request.getParameter("system").isEmpty())
				    	system = request.getParameter("system");
				    if (system != null)
				    	_xhtml_out.println("<input type=\"hidden\" name=\"system\" value=\""+system+"\"/>");
				    String application = null;
				    if (_script != null && _script.get("application") != null && !((String)_script.get("application")).isEmpty())
				    	application = (String) _script.get("application");
				    else if (request.getParameter("application")!=null && !request.getParameter("application").isEmpty())
				    	application = request.getParameter("application");
				    if (application != null)
				    	_xhtml_out.println("<input type=\"hidden\" name=\"application\" value=\""+application+"\"/>");

				    List<Map<String, String>> _listStep = null;
					if (application!=null && !application.isEmpty())
						_listStep = StepManager.listSteps(StepManager.TYPE_STEP_SCRIPT_APP);
					else
						_listStep = StepManager.listSteps(StepManager.TYPE_STEP_SCRIPT_SYSTEM);
					
				    if (request.getParameter("servletSource")!=null && !request.getParameter("servletSource").isEmpty())
				    	_xhtml_out.println("<input type=\"hidden\" name=\"servletSource\" value=\""+request.getParameter("servletSource")+"\"/>");
				    
		            int _maxVariable=request.getParameter("variableCount")!=null ? Integer.valueOf(request.getParameter("variableCount")) : (_variables!=null && _variables.size()>0 ? _variables.size() : 1);
		            int _maxScripts=request.getParameter("scriptsCount")!=null ? Integer.valueOf(request.getParameter("scriptsCount")) : (_scriptsItems!=null && _scriptsItems.size()>0 ? _scriptsItems.size() : 1);
				    _xhtml_out.println("<input type=\"hidden\" id=\"variableCount\" name=\"variableCount\" value=\""+_maxVariable+"\"/>");
				    _xhtml_out.println("<input type=\"hidden\" id=\"scriptsCount\" name=\"scriptsCount\" value=\""+_maxScripts+"\"/>");
				    if (!_new){
				    	_xhtml_out.println("<input type=\"hidden\" name=\"scriptName\" value=\"" + request.getParameter("scriptName") + "\"/>");
				    }
		            _xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					if (!_new){
						_xhtml_out.print(request.getParameter("scriptName"));
					}else{
						_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.new"));
						}
					_xhtml_out.print("<a href=\"javascript:submitForm(document.scriptProcess.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.name"));
			    	_xhtml_out.println(": </label>");
			    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"scriptName\" value=\"");
			    	_xhtml_out.print(request.getParameter("scriptName")!=null && !request.getParameter("scriptName").isEmpty() ? request.getParameter("scriptName") : "");
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
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.type"));
			    	_xhtml_out.println(": </label>");
			    	_xhtml_out.println("<select class=\"form_select\" name=\"typeScript\"/>");
			    	_xhtml_out.print("<option value=\""+ScriptProcessManager.BEFORE_EXECUTION+"\" ");
			    	String _typeScript=request.getParameter("typeScript")!=null ? request.getParameter("typeScript") : (_script!=null && _script.get("type")!=null ? (String)_script.get("type") : "");
			    	if (_typeScript != null && !_typeScript.isEmpty()) {
			    		if (_typeScript.equals(String.valueOf(ScriptProcessManager.BEFORE_EXECUTION)))
				    		_xhtml_out.print("selected=\"selected\" ");
			    	}
			    	_xhtml_out.print(">"+getLanguageMessage("common.menu.advanced.script_process.before_execution")+"</option>");
			    	_xhtml_out.print("<option value=\""+ScriptProcessManager.AFTER_EXECUTION+"\" ");
			    	if (_typeScript != null && !_typeScript.isEmpty()) {
			    		if (_typeScript.equals(String.valueOf(ScriptProcessManager.AFTER_EXECUTION)))
				    		_xhtml_out.print("selected=\"selected\" ");
			    	}
			    	_xhtml_out.print(">"+getLanguageMessage("common.menu.advanced.script_process.after_execution")+"</option>");
			    	_xhtml_out.println("</select>");
			    	_xhtml_out.println("</div>");	
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("<div class=\"standard_form\">");
			    	_xhtml_out.print("<label for=\"port\">");
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.abort_type"));
			    	_xhtml_out.println(": </label>");
			    	String _abortType=request.getParameter("abortType")!=null ? request.getParameter("abortType") : (_script!=null && _script.get("abortType")!=null ? (String)_script.get("abortType") : "");
			    	_xhtml_out.print("<input class=\"form_text\" type=\"checkbox\" name=\"abortType\" value=\"true\" ");
			    	if (_abortType != null && !_abortType.isEmpty()) {
			    		if (_abortType.equals("true"))
				    		_xhtml_out.print("checked=\"checked\" ");
			    	}			    	
			    	_xhtml_out.print("/>");
			    	_xhtml_out.println("</div>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("<div class=\"standard_form\">");
			    	_xhtml_out.print("<label for=\"port\">");
			    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.step"));
			    	_xhtml_out.println(": </label>");
			    	_xhtml_out.println("<select class=\"form_select\" onchange=\"changeAbilityAppSys(this.value);\" name=\"step\"/>");
			    	String _stepSeletect=request.getParameter("step")!=null ? request.getParameter("step") : (_script!=null && _script.get("step")!=null ? (String)_script.get("step") : "");
			    	for (Map<String, String> _step: _listStep){
			    		_xhtml_out.print("<option value=\""+_step.get("name")+"---&&&---"+_step.get("type")+"\" ");
				    	if (_stepSeletect != null && !_stepSeletect.isEmpty()) {
				    		if (_stepSeletect.equals(_step.get("name")))
					    		_xhtml_out.print("selected=\"selected\" ");
				    	}
				    	_xhtml_out.print(">"+_step.get("name")+"</option>");
			    	}
			    	_xhtml_out.println("</select>");
			    	_xhtml_out.println("</div>");	
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("</fieldset>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("</div>");
		            _xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.variables"));
					_xhtml_out.print("<a href=\"javascript:addVariable();\"><img src=\"/images/add_16.png\" title=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.add"));
	            	_xhtml_out.print("\" alt=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.add"));
	            	_xhtml_out.println("\"/></a>");
		            _xhtml_out.println("</h2>");
		            _xhtml_out.println("<fieldset>");
		            String _variableName=null;
		            String _variableDescription=null;
		            String _variablePassword=null;
		            int _max=request.getParameter("variableCount")!=null ? Integer.valueOf(request.getParameter("variableCount")) : (_variables!=null && _variables.size()>0 ? _variables.size() : 1);
			    	_xhtml_out.print("<table >");
			    	_xhtml_out.print("<tbody name=\"variables\" id=\"variables\">");
		            for (int x=0; x<_max ; x++){
		            	_variableName=request.getParameter("variableName"+x)!=null ? request.getParameter("variableName"+x) : (_variables.size()>x ? _variables.get(x).get("name") : "");
				    	if (x==0){
					    	_xhtml_out.print("<tr>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.print("<b>"+getLanguageMessage("common.menu.advanced.script_process.name")+"</b>");
					    	_xhtml_out.println(": </label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.print("<b>"+getLanguageMessage("common.menu.advanced.script_process.description")+"</b>");
					    	_xhtml_out.println(": </label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.print("<b>"+getLanguageMessage("common.menu.advanced.script_process.type.password")+"</b>");
					    	_xhtml_out.println(": </label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("</tr>");
					    	_xhtml_out.print("<tr class=\"highlight\">");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.println(StorageInventoryManager.airback_iqn_nameVar);
					    	_xhtml_out.println("</label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.println(getLanguageMessage("advanced.inventory.wbsairback_iqn_var"));
					    	_xhtml_out.println("</label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td></td>");
					    	_xhtml_out.print("</tr>");
					    	_xhtml_out.print("<tr>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.println(StorageInventoryManager.airback_wwn_nameVar);
					    	_xhtml_out.println("</label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.println(getLanguageMessage("advanced.inventory.airback_wwn_nameVar"));
					    	_xhtml_out.println("</label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td></td>");
					    	_xhtml_out.print("</tr>");
				    	}
				    	_xhtml_out.print("<tr");
                    	if(x % 2 == 0) {
                    		_xhtml_out.print(" class=\"highlight\"");
                    	}
				    	_xhtml_out.print(">");
				    	_xhtml_out.print("<td>");
				    	_xhtml_out.println("<div class=\"standard_form\">");
				    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"variableName"+x+"\" id=\"variableName"+x+"\" value=\"");
				    	_xhtml_out.print(_variableName);
				    	_xhtml_out.print("\" ");
				    	_xhtml_out.print("/>");
				    	_xhtml_out.println("</div>");
				    	_xhtml_out.print("</td>");
				    	_xhtml_out.print("<td>");
				    	_xhtml_out.println("<div class=\"standard_form\">");
		            	_variableDescription=request.getParameter("variableDescription"+x)!=null ? request.getParameter("variableDescription"+x) : (_variables.size()>x ? _variables.get(x).get("description") : "");
				    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"variableDescription"+x+"\" value=\"");
				    	_xhtml_out.print(_variableDescription);
				    	_xhtml_out.print("\" ");
				    	_xhtml_out.print("/>");
				    	_xhtml_out.println("</div>");
				    	_xhtml_out.print("</td>");
				    	_xhtml_out.print("<td>");
				    	_xhtml_out.print("<div class=\"standard_form\">");
				    	_variablePassword=request.getParameter("variablePassword"+x)!=null ? request.getParameter("variablePassword"+x) : (_variables.size()>x ? _variables.get(x).get("password") : "");
				    	_xhtml_out.print("<input class=\"form_text vertical-align_top\" type=\"checkbox\" name=\"variablePassword"+x+"\" value=\"true\" ");
				    	if (_variablePassword.equals("true"))
				    		_xhtml_out.print(" checked=\"checked\"");
				    	_xhtml_out.print("/> ");
				    	_xhtml_out.print("</div>");
				    	_xhtml_out.print("</td>");
				    	_xhtml_out.print("</tr>");
		            }
			    	_xhtml_out.print("</tbody>");
			    	_xhtml_out.print("</table>");
			    	_xhtml_out.println("</fieldset>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("</div>");
		            _xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.scripts"));
					_xhtml_out.print("<a href=\"javascript:addScript();\"><img src=\"/images/add_16.png\" title=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.add"));
	            	_xhtml_out.print("\" alt=\"");
	            	_xhtml_out.print(getLanguageMessage("common.message.add"));
	            	_xhtml_out.println("\"/></a>");
		            _xhtml_out.println("</h2>");
		            _xhtml_out.println("<fieldset>");
		            String _scriptOrder=null;
		            String _scriptContent=null;
		            String _shell = null;
		            _max=request.getParameter("scriptsCount")!=null ? Integer.valueOf(request.getParameter("scriptsCount")) : (_scriptsItems!=null && _scriptsItems.size()>0 ? _scriptsItems.size() : 1);
			    	_xhtml_out.print("<table>");
			    	_xhtml_out.print("<tbody  name=\"scripts\" id=\"scripts\">");
		            for (int x=0; x<_max ; x++){
		            	_scriptOrder=request.getParameter("scriptOrder"+x)!=null ? request.getParameter("scriptOrder"+x) : (_scriptsItems.size()>x ? _scriptsItems.get(x).get("order") : "");
				    	_scriptContent=request.getParameter("scriptContent"+x)!=null ? request.getParameter("scriptContent"+x) : (_scriptsItems.size()>x ? _scriptsItems.get(x).get("content") : "");
				    	_shell=request.getParameter("scriptShell"+x)!=null ? request.getParameter("scriptShell"+x) : (_scriptsItems.size()>x ? _scriptsItems.get(x).get("shell") : "");
				    	if (x==0){
					    	_xhtml_out.print("<tr>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.print("<b>"+getLanguageMessage("common.menu.advanced.script_process.order")+"</b>");
					    	_xhtml_out.println(": </label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.print("<b>"+getLanguageMessage("common.menu.advanced.script_process.content")+"</b>");
					    	_xhtml_out.println(": </label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("<td>");
					    	_xhtml_out.print("<label for=\"port\">");
					    	_xhtml_out.print("<b>"+getLanguageMessage("common.menu.advanced.script_process.shell")+"</b>");
					    	_xhtml_out.println(": </label>");
					    	_xhtml_out.print("</td>");
					    	_xhtml_out.print("</tr>");
				    	}
				    	_xhtml_out.print("<tr>");
				    	_xhtml_out.print("<td>");
				    	_xhtml_out.println("<div class=\"standard_form\" style=\"height: 106px; width: 155px;\">");
				    	_xhtml_out.print("<input class=\"form_text vertical-align_top\"  onKeyPress=\"return checkIt(event)\" size=\"3\" type=\"text\" name=\"scriptOrder"+x+"\" value=\"");
				    	_xhtml_out.print(_scriptOrder);
				    	_xhtml_out.print("\" ");
				    	_xhtml_out.print("/>");
				    	_xhtml_out.println("</div>");
				    	_xhtml_out.print("</td>");
				    	_xhtml_out.print("<td>");
				    	_xhtml_out.println("<div class=\"standard_form\">");
				    	_xhtml_out.print("<textarea rows=\"6\" cols=\"50\" class=\"form_text\"  name=\"scriptContent"+x+"\" value=\""+_scriptContent+"\">");
				    	_xhtml_out.print(_scriptContent);
				    	_xhtml_out.print("</textarea>");
				    	_xhtml_out.println("</div>");
				    	_xhtml_out.print("</td>");
				    	_xhtml_out.print("<td>");
				    	_xhtml_out.println("<div class=\"standard_form\" style=\"height: 106px; width: 155px;\">");
				    	_xhtml_out.print("<select class=\"form_select vertical-align_top\" name=\"scriptShell"+x+"\" >");
						for (String key : ScriptProcessManager.supportedShells.keySet()) {
							String text = ScriptProcessManager.supportedShells.get(key);
							_xhtml_out.print("<option value=\""+key+"\" ");
							if (key != null && key.equals(_shell))
								_xhtml_out.print(" selected=\"selected\" ");
							_xhtml_out.print(">"+text+"</option>");
						}
				    	
						_xhtml_out.print("</select>");
						_xhtml_out.println("</div>");
				    	_xhtml_out.print("</td>");
				    	_xhtml_out.print("</tr>");			    	
		            }
			    	_xhtml_out.print("</tbody>");
			    	_xhtml_out.print("</table>");	
			    	_xhtml_out.println("</fieldset>");
			    	_xhtml_out.println("<div class=\"clear\"></div>");
			    	_xhtml_out.println("</div>");
			    	_xhtml_out.println("</form>");
			    	pageJSFuncs += getJSAddVars();
			    	pageJSFuncs += getJSAddScript();
			    	pageJSFuncs +=checkOnlyNumbers();
			    	pageJSFuncs +=changeAbilityAppSys();
		    	}
				break;
	    		case SCRIPT_STORE: {
	    			if (request.getParameter("scriptName") == null || request.getParameter("scriptName").isEmpty()) {
	    				throw new Exception(getLanguageMessage("common.menu.advanced.script_process.no_name"));
	    			}
					if (!request.getParameter("scriptName").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid"));
					}
					if (request.getParameter("scriptName").contains("--")) {
						throw new Exception(getLanguageMessage("advanced.inventory.error.name.invalid.special"));
					}
	    			if (request.getParameter("step") == null || request.getParameter("step").isEmpty()) {
	    				throw new Exception(getLanguageMessage("common.menu.advanced.script_process.no_step"));
	    			}
	    			if ((request.getParameter("system") == null || request.getParameter("system").isEmpty())) {
	    				throw new Exception(getLanguageMessage("common.menu.advanced.script_process.no_system"));
	    			}
	    			if (request.getParameter("scriptContent0") == null || request.getParameter("scriptContent0").isEmpty()) {
	    				throw new Exception(getLanguageMessage("common.menu.advanced.script_process.no_script"));
	    			}
	    			Map<String, Object> _script=new HashMap<String, Object>();
	    			_script.put("name", request.getParameter("scriptName"));
	    			_script.put("type", request.getParameter("typeScript"));
	    			_script.put("abortType", request.getParameter("abortType")!=null && !request.getParameter("abortType").isEmpty() ? request.getParameter("abortType") : "false");
	    			if (request.getParameter("step")!=null && request.getParameter("step").contains("---&&&---")){
	    				_script.put("step", request.getParameter("step").substring(0,request.getParameter("step").indexOf("---&&&---")));
	    			}else{
	    				_script.put("step", request.getParameter("step"));
	    			}
	    			if (request.getParameter("system") != null)
	    				_script.put("system", request.getParameter("system"));
	    			if (request.getParameter("application") != null)
	    				_script.put("application", request.getParameter("application"));
	    			List<Map<String, String>> _variables=new ArrayList<Map<String, String>>();	    			
	    			for (int x=0; request.getParameter("variableName"+x)!=null;x++){	
	    				Map<String, String> _variable=new HashMap<String, String>();
	    				if (!request.getParameter("variableName"+x).isEmpty()){
	    					_variable.put("name", request.getParameter("variableName"+x));
	    					if (request.getParameter("variableDescription"+x) != null && !request.getParameter("variableDescription"+x).isEmpty())
	    						_variable.put("description", request.getParameter("variableDescription"+x));
	    					else
	    						_variable.put("description", "");
	    					if (request.getParameter("variablePassword"+x) != null && request.getParameter("variablePassword"+x).equals("true")) 
	    						_variable.put("password", request.getParameter("variablePassword"+x));
	    					else
	    						_variable.put("password", "false");
	    					_variables.add(_variable);
	    				}
	    			}
	    			_script.put("variables", _variables);
	    			List<Map<String, String>> _scriptsItems=new ArrayList<Map<String, String>>();
	    			List<Integer> _orderList=new ArrayList<Integer>();
	    			for (int x=0; request.getParameter("scriptContent"+x)!=null;x++){	  
	    				Map<String, String> _scriptItems=new HashMap<String, String>();
	    				if (!request.getParameter("scriptContent"+x).isEmpty()){
	    					if (request.getParameter("scriptContent"+x).contains("'"))
	    						throw new Exception(getLanguageMessage("common.menu.advanced.script_process.quotes"));
		    				_scriptItems.put("order", generateOrderScript(_orderList,request.getParameter("scriptOrder"+x)!=null && !request.getParameter("scriptOrder"+x).isEmpty() ? Integer.valueOf(request.getParameter("scriptOrder"+x)) : null));
		    				_scriptItems.put("content", request.getParameter("scriptContent"+x));
		    				_scriptItems.put("shell", request.getParameter("scriptShell"+x));
		    				_scriptsItems.add(_scriptItems);
	    				}
	    			}
	    			_script.put("scripts", _scriptsItems);
	    			_sm.saveScript(request.getParameter("scriptName"), _script, saim);	
	    			
	    			String messageUpdatedJobs = updateRelatedJobs(_sm.getScript(request.getParameter("scriptName")), _c);
					String message = getLanguageMessage("advanced.scriptprocess.stored");
					if (!messageUpdatedJobs.equals(""))
						message+= ". "+getLanguageMessage("advanced.jobs.updated")+": "+messageUpdatedJobs;
					
	    			writeDocumentResponse(message, request.getParameter("servletSource"));
	    		}
				break;
	    		case SCRIPT_REMOVE: {
	    			if (request.getParameter("scriptName") == null || request.getParameter("scriptName").isEmpty()) {
	    				throw new Exception(getLanguageMessage("common.menu.advanced.script_process.no_name"));
	    			}
	    			if(request.getParameter("confirm") != null) {
	    				_sm.deleteScript(request.getParameter("scriptName"), saim);
	    				
			    		writeDocumentResponse(getLanguageMessage("common.menu.advanced.script_process.script_deleted"),  request.getParameter("servletSource"));
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("common.menu.advanced.script_process.removed_question"), "/admin/AdvancedScriptProcess?type=" + SCRIPT_REMOVE + "&scriptName=" + request.getParameter("scriptName") + "&confirm=true&servletSource="+ request.getParameter("servletSource"), null);
	    			}
	    		}
				break;
	    	}	    
	    } catch (Exception _ex) {
	    	if (type == SCRIPT_STORE)
	    		writeDocumentError(_ex.getMessage(), request.getParameter("servletSource"));
	    	else
	    		writeDocumentError(_ex.getMessage());
	    } finally {
	    	if (type != SCRIPT_COMBO_BY_STEP)
	    		writeDocumentFooter();
	    }
		
	}

	public String generateOrderScript(List<Integer> _orderList,Integer _orderInt){
		Integer _order=_orderInt!=null ? _orderInt : 0;
		boolean _notFound=false;
		while(!_notFound){
			if (_orderList.contains(_order)){
				_order++;
			}
			else{
				_orderList.add(_order);
				_notFound=true;
			}
		}
		return _order.toString();
	}
	
 	public void printSectionHeader(PrintWriter _xhtml_out) throws Exception {
		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/remote_storage_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.remotestorage"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(getLanguageMessage("advanced.remotestorage.info"));
		_xhtml_out.println("</div>");
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
 	
 	public static String changeAbilityAppSys() throws Exception {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function changeAbilityAppSys(value) {\n");
 		_sb.append("	if (value.indexOf('"+StepManager.TYPE_STEP_SCRIPT_APP+"') > -1){\n");
 		_sb.append("		$(\"#system\").attr(\"disabled\",\"disabled\");\n");
 		_sb.append("		$(\"#application\").removeAttr(\"disabled\");\n");
 		_sb.append("	}else{\n");
 		_sb.append("		$(\"#application\").attr(\"disabled\",\"disabled\");\n");
 		_sb.append("		$(\"#system\").removeAttr(\"disabled\");\n");
 		_sb.append("	}\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	} 	
 	
 	public static String getJSAddVars() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function addVariable() {\n");
 		_sb.append("	var ni = document.getElementById('variables');\n");
 		_sb.append("	var numi = document.getElementById('variableCount').value;\n");
 		_sb.append("	var num = parseInt(document.getElementById('variableCount').value) + 1;\n");
 		_sb.append("	numi.value = num;\n");
 		_sb.append("	var variableName = 'variableName'+numi;\n");
 		_sb.append("	var variableDescription = 'variableDescription'+numi;\n");
 		_sb.append("	var variablePassword = 'variablePassword'+numi;\n");
 		_sb.append("	if (numi % 2 == 0)\n");
 		_sb.append("     	$('#variables').append('"+getHtmlInputVarJS("variableName","variableDescription", "variablePassword", true)+"');\n");
 		_sb.append("	else\n");
 		_sb.append("     	$('#variables').append('"+getHtmlInputVarJS("variableName","variableDescription", "variablePassword", false)+"');\n");
 		_sb.append("	document.getElementById('variableCount').value=num;\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	}
 	
 	public static String getHtmlInputVarJS(String varId,String descriptionId, String passwordId, boolean highlight) {
 		StringBuilder _sb = new StringBuilder();
 		if (highlight)
 			_sb.append("<tr class=\"highlight\">");
 		else
 			_sb.append("<tr>");
    	_sb.append("<td>");
    	_sb.append("<div class=\"standard_form\">");
    	_sb.append("<input class=\"form_text\" type=\"text\" name=\"'+"+varId+"+'\" id=\"'+"+varId+"+'\" value=\"\" ");
    	_sb.append("/>");
    	_sb.append("</div>");
    	_sb.append("</td>");
    	_sb.append("<td>");
    	_sb.append("<div class=\"standard_form\">");
    	_sb.append("<input class=\"form_text\" type=\"text\" name=\"'+"+descriptionId+"+'\" value=\"\" ");
    	_sb.append("/>");
    	_sb.append("</div>");
    	_sb.append("</td>");
    	_sb.append("<td>");
    	_sb.append("<div class=\"standard_form\">");
    	_sb.append("<input class=\"form_text vertical-align_top\" type=\"checkbox\" name=\"'+"+passwordId+"+'\" value=\"true\" />");
    	_sb.append("</div>");
    	_sb.append("</td>");
    	_sb.append("</tr>");
    	return _sb.toString();
 	}
 	
 	public static String getJSAddScript() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function addScript() {\n");
 		_sb.append("	var ni = document.getElementById('scripts');\n");
 		_sb.append("	var numi = document.getElementById('scriptsCount').value;\n");
 		_sb.append("	var num = parseInt(document.getElementById('scriptsCount').value) + 1;\n");
 		_sb.append("	numi.value = num;\n");
 		_sb.append("	var scriptOrder = 'scriptOrder'+numi;\n");
 		_sb.append("	var scriptContent = 'scriptContent'+numi;\n");
 		_sb.append("	var scriptShell = 'scriptShell'+numi;\n");
 		_sb.append("     $('#scripts').append('"+getHtmlInputScriptJS("scriptOrder","scriptContent", "scriptShell")+"');\n");
 		_sb.append("	document.getElementById('scriptsCount').value=num;\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	}
 	
 	public static String getHtmlInputScriptJS(String varId,String descriptionId, String shellId) {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("<tr>");
    	_sb.append("<td>");
    	_sb.append("<div class=\"standard_form\" style=\"height: 106px; width: 155px;\">");
    	_sb.append("<input class=\"form_text vertical-align_top\" onKeyPress=\"return checkIt(event)\" size=\"3\" type=\"text\" name=\"'+"+varId+"+'\" value=\"\" ");
    	_sb.append("/>");
    	_sb.append("</div>");
    	_sb.append("</td>");
    	_sb.append("<td>");
    	_sb.append("<div class=\"standard_form\">");
    	_sb.append("<textarea rows=\"6\" cols=\"50\" class=\"form_text\"  name=\"'+"+descriptionId+"+'\" value=\"\">");
    	_sb.append("</textarea>");
    	_sb.append("</div>");
    	_sb.append("</td>");
    	_sb.append("<td>");
    	_sb.append("<div class=\"standard_form\" style=\"height: 106px; width: 155px;\">");
    	_sb.append("<select class=\"form_select vertical-align_top\"  name=\"'+"+shellId+"+'\" >");
		for (String key : ScriptProcessManager.supportedShells.keySet()) {
			String text = ScriptProcessManager.supportedShells.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	_sb.append(">"+text+"</option>");
		}
    	
		_sb.append("</select>");
		_sb.append("</div>");
    	_sb.append("</td>");
    	_sb.append("</tr>");    	
    	return _sb.toString();
 	}
 	
 	public static String getHtmlInputVar(String inputId, String value) {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("<div class=\"standard_form\">");
		_sb.append("<input class=\"form_text\" name=\""+inputId+"\" id=\""+inputId+"\"value=\"");
		if (value != null && !value.isEmpty()) {
			_sb.append(value);
		}
    	_sb.append("\" />");
    	_sb.append("</div>");
    	return _sb.toString();
 	}
 	
 	public void printComboScriptsByStep(PrintWriter _xhtml_out, String step, String type) {
 		try {
	 		ScriptProcessManager sp = new ScriptProcessManager();
	 		Map<String, String> entity = new HashMap<String, String>();
	 		if (step != null && !step.isEmpty() && type != null && !type.isEmpty()) {
		 		Map<String, Map<String, Object>> scripts = sp.getScriptByStepAndType(step, type);
				for (String nameScript : scripts.keySet()) {
					entity.put(nameScript, nameScript);
				}
	 		} else {
	 			Map<String, Map<String, Object>> scripts = sp.listScript();
				for (String nameScript : scripts.keySet()) {
					entity.put(nameScript, nameScript);
				}
	 		}
	 		String selectMessage = getLanguageMessage("advanced.templatejob.script.system");
	 		if (type.equals("application"))
	 			selectMessage = getLanguageMessage("advanced.templatejob.script.app");
	 		
	 		if (entity != null && !entity.isEmpty())
	 			_xhtml_out.println(HtmlFormUtils.selectOption("data", selectMessage, null, entity, true));
	 		else {
	 			_xhtml_out.println("<div class=\"subinfo\">");
        		_xhtml_out.println(getLanguageMessage("advanced.templatejob.steps.entities.nooftypestep"));
				_xhtml_out.println("</div>");
	 		}
 		} catch (Exception ex) {
 			logger.error("Error cargando combo dinámico de script processes con step:{} type:{}", step, type);
 		}
 	}
 	
 	protected String getSmartContent(String content, Map<String, String> attributes)
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
 	
 	/**
	 * Actualiza los posibles jobs asociados al remoteStorage
	 * @param nameRemoteStorage
	 * @param _c
	 * @return
	 * @throws Exception
	 */
	public String updateRelatedJobs(Map<String, Object> scriptProcess, Configuration _c) throws Exception {
		String messageUpdatedJobs = "";
		String nameScriptProcess = (String) scriptProcess.get("name");
		List<Map<String, String>> steps = TemplateJobManager.getStepsWithProperty("name", (String)scriptProcess.get("step"));
		List<String> stepsChecked = new ArrayList<String>();
		JobManager jm = new JobManager(_c);
		if (steps != null && steps.size()>0) {
			for (Map<String, String> step : steps) {
				if (!stepsChecked.contains(step.get("name"))) {
					List<Map<String, Object>> jobs = GroupJobManager.getJobsWithProperty("step", step.get("name"), _c);
					if (jobs != null && !jobs.isEmpty()) {
						for (Map<String, Object> job : jobs) {
							if (job.containsKey("typeStep") && (((String)job.get("typeStep")).equals(StepManager.TYPE_STEP_SCRIPT_APP) || job.get("typeStep").equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM))) {
								List<Map<String, Object>> scripts = JobManager.getJobScripts((String)job.get("name"), true);
								if (scripts != null && scripts.size()>0) {
									if (!messageUpdatedJobs.contains((String) job.get("name"))) {
										for (Map<String, Object> s : scripts) {
											String name = (String) s.get("name");
											name = name.substring(0, name.lastIndexOf("_"));
											if (name.equals(nameScriptProcess)) {
												Map<String, String> variablesValues = JobManager.getScriptVars((String) job.get("name")+"_"+s.get("name")); 
												Map<String, Object> jobScript = JobManager.getJobScript((String) job.get("name"),(String) job.get("name")+"_"+s.get("name"));
												boolean before = true;
												if (scriptProcess.get("type") != null && (Integer.parseInt((String)scriptProcess.get("type"))) == ScriptProcessManager.AFTER_EXECUTION)
													before = false;
												boolean abortOnError = false;
												if (scriptProcess.get("abortType") != null && ((String)scriptProcess.get("abortType")).equals("true"))
													abortOnError = true;
												
												TreeMap<String,Map<String, String>> _scriptsContent=new TreeMap<String, Map<String, String>>();
												if (scriptProcess!=null && scriptProcess.get("scripts")!=null){
													@SuppressWarnings("unchecked")
													List<Map<String, String>> _ss= (List<Map<String, String>>) scriptProcess.get("scripts");
													for (Map<String, String> _s: _ss){
															Map<String, String> objScript = new HashMap<String, String>();
															objScript.put("order", _s.get("order"));
															objScript.put("script", getSmartContent(_s.get("content"), variablesValues));
															objScript.put("shell", _s.get("shell"));
															_scriptsContent.put(objScript.get("order"), objScript);
													}
												}
												
												ScriptProcessManager.generateScriptsJob((String) job.get("name"), (String) scriptProcess.get("name"), jm, _scriptsContent, abortOnError, before, Boolean.valueOf((String)jobScript.get("fail")), Boolean.valueOf((String)jobScript.get("success")), variablesValues);
												if (messageUpdatedJobs.equals(""))
													messageUpdatedJobs+=(String) job.get("name");
												else if (!messageUpdatedJobs.contains((String) job.get("name")))
													messageUpdatedJobs+=", "+(String) job.get("name");
											}
										}
									}
								}
							}
						}
					}
					stepsChecked.add(step.get("name"));
				}
			}
		}
		
		
		List<String> jobs = JobManager.getJobsForScriptProcess(nameScriptProcess);
		for (String jobName : jobs) {
			if (!messageUpdatedJobs.contains(jobName)) {
				List<Map<String, Object>> scripts = JobManager.getJobScripts(jobName, true);
				if (scripts != null && scripts.size()>0) {
					for (Map<String, Object> s : scripts) {
						String name = (String) s.get("name");
						name = name.substring(0, name.indexOf("_"));
						if (name.contains(nameScriptProcess+"--")) {
							Map<String, String> variablesValues = JobManager.getScriptVars((String) jobName+"_"+s.get("name")); 
							Map<String, Object> jobScript = JobManager.getJobScript(jobName, (String) jobName+"_"+s.get("name"));
							boolean before = true;
							if (scriptProcess.get("type") != null && (Integer.parseInt((String)scriptProcess.get("type"))) == ScriptProcessManager.AFTER_EXECUTION)
								before = false;
							boolean abortOnError = false;
							if (scriptProcess.get("abortType") != null && scriptProcess.get("abortType").equals("true"))
								abortOnError = true;
							
							TreeMap<String,Map<String, String>> _scriptsContent=new TreeMap<String, Map<String, String>>();
							if (scriptProcess!=null && scriptProcess.get("scripts")!=null){
								@SuppressWarnings("unchecked")
								List<Map<String, String>> _ss= (List<Map<String, String>>) scriptProcess.get("scripts");
								for (Map<String, String> _s: _ss){
										Map<String, String> objScript = new HashMap<String, String>();
										objScript.put("order", _s.get("order"));
										objScript.put("script", getSmartContent(_s.get("content"), variablesValues));
										objScript.put("shell", _s.get("shell"));
										_scriptsContent.put(objScript.get("order"), objScript);
								}
							}
							
							ScriptProcessManager.generateScriptsJob((String) jobName, name, jm, _scriptsContent, abortOnError, before, Boolean.valueOf((String)jobScript.get("fail")), Boolean.valueOf((String)jobScript.get("success")), variablesValues);
							if (messageUpdatedJobs.equals(""))
								messageUpdatedJobs+=(String) jobName;
							else if (!messageUpdatedJobs.contains(jobName))
								messageUpdatedJobs+=", "+(String) jobName;
						}
					}
				}
			}
		}
		return messageUpdatedJobs;
	}
}
