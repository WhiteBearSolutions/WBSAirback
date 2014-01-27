package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DefaultConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.MultiPathManager;

public class SystemMaintenance extends WBSImagineServlet {
	static final long serialVersionUID = 20080902L;
	public final static int VACUUM_DATABASE = 2;
	public final static int EXPORTCONF = 3;
	public final static int IMPORTCONF = 4;
	public final static int EXPORTCONF_PLAN = 5;
	public final static int BACKUP_CLIENT = 6;
	public final static String baseUrl = "/admin/"+SystemMaintenance.class.getSimpleName();

	private int type;
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter _xhtml_out = response.getWriter();
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

	    	com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration _sc = new com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration();
	    	
			switch(this.type) {
	    		default: {
	    			// TODO: uncomment with bacula
	    			//	List<String> _shares = ShareManager.getExternalShareNames();
		    		//	String _sd_password = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Director", "airback-dir", "Password");
		    		//	String _fd_password = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-fd.conf", "Director", "airback-dir", "Password");
		    		//	Map<String, Integer> _db_parameters = GeneralSystemConfiguration.getDataBaseConfiguration();
	    			
		    			_xhtml_out.println("<script type=\"text/javascript\" language=\"javascript\">");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("function importConfiguration() {");
		    			_xhtml_out.print("    document.maintenance.type.value = ");
						_xhtml_out.print(IMPORTCONF);
						_xhtml_out.println(";");
						_xhtml_out.println("  submitForm(document.maintenance.submit());");
						_xhtml_out.println("}");
						_xhtml_out.println("function exportConfiguration() {");
						_xhtml_out.print("    document.maintenance.type.value = ");
						_xhtml_out.print(EXPORTCONF);
						_xhtml_out.println(";");
						_xhtml_out.println("  submitForm(document.maintenance.submit());");
						_xhtml_out.println("}");
						_xhtml_out.println("function removeConfiguration() {");
						_xhtml_out.print("    document.getElementById('external_share').value = '';");
						_xhtml_out.print("    document.getElementById('external_share_noValue').selected = 'true';");
						_xhtml_out.print("    document.getElementById('hour').value = '';");
						_xhtml_out.print("    document.getElementById('hour_noValue').selected = 'true';");
						_xhtml_out.print("    document.getElementById('retention').value = '';");						
						_xhtml_out.println("}");
                        _xhtml_out.println("function showDbInfo() {");
                        _xhtml_out.println("  var dbinfo = document.getElementById(\"dbinfo\");");
                        _xhtml_out.println("  if(dbinfo != null) {");
                        _xhtml_out.println("    if(dbinfo.style.visibility == \"visible\") {");
                        _xhtml_out.println("      dbinfo.style.visibility = \"hidden\";");
                        _xhtml_out.println("    } else {");
                        _xhtml_out.println("      dbinfo.style.visibility = \"visible\";");
                        _xhtml_out.println("    }");
                        _xhtml_out.println("  }");
                        _xhtml_out.println("}");
						_xhtml_out.println("//-->");
						_xhtml_out.println("</script>");
						_xhtml_out.println("<form action=\"/admin/SystemMaintenance\" name=\"maintenance\" id=\"maintenance\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + EXPORTCONF_PLAN + "\"/>");
	                    _xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/lorry_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.manteinance"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.manteinance.info"));
	                    _xhtml_out.print("</div>");
	                    
	                    /*
	                     // TODO: uncomment with bacula
	                     * _xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.manteinance.backup_passwords"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"_password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.sd_password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_password\" value=\"");
	        	    	_xhtml_out.print(_sd_password);
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"_password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.fd_password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_password\" value=\"");
	        	    	_xhtml_out.print(_fd_password);
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.manteinance.db"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.maintenance.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");
					    _xhtml_out.print("<a href=\"javascript:showDbInfo();\"><img src=\"/images/eye_16.png\" title=\"");
                        _xhtml_out.print(getLanguageMessage("common.message.eye"));
                        _xhtml_out.print("\" alt=\"");
                        _xhtml_out.print(getLanguageMessage("common.message.eye"));
                        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<div id=\"dbinfo\">");
	                    _xhtml_out.print("<label class=\"strong\">");
	                    _xhtml_out.print(getLanguageMessage("system.manteinance.db.max_connections"));
	                    _xhtml_out.print("</label>: ");
	                    _xhtml_out.println("500");
	                    _xhtml_out.println("<br/><br/>");
	                    _xhtml_out.print("<label class=\"strong\">");
	                    _xhtml_out.print(getLanguageMessage("system.manteinance.db.shared_buffers"));
	                    _xhtml_out.print("</label>: ");
	                    _xhtml_out.print(GeneralSystemConfiguration.getDataBaseRecommendedSharedBuffer());
	                    _xhtml_out.println(" MB");
	                    _xhtml_out.println("<br/><br/>");
	                    _xhtml_out.print("<label class=\"strong\">");
	                    _xhtml_out.print(getLanguageMessage("system.manteinance.db.cache"));
	                    _xhtml_out.print("</label>: ");
	                    _xhtml_out.print(GeneralSystemConfiguration.getDataBaseRecommendedCache());
	                    _xhtml_out.println(" MB");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"max_connections\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.db.max_connections"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"max_connections\" value=\"");
	        	    	_xhtml_out.print(_db_parameters.get("max_connections"));
	        	    	_xhtml_out.println("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"shared_buffers\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.db.shared_buffers"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"shared_buffers\" value=\"");
	        	    	_xhtml_out.print(_db_parameters.get("shared_buffers"));
	        	    	_xhtml_out.println("\"/>&nbsp;MB");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"cache\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.db.cache"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"cache\" value=\"");
	        	    	_xhtml_out.print(_db_parameters.get("cache"));
	        	    	_xhtml_out.println("\"/>&nbsp;MB");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label></label>");
	        	    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" name=\"optimize\" value=\"");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.db.optimize"));
	        	    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemMaintenance?type=");
	        	    	_xhtml_out.print(VACUUM_DATABASE);
	        	    	_xhtml_out.println("';\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.println(getLanguageMessage("system.manteinance.backup"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.maintenance.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:importConfiguration();\"><img src=\"/images/arrow_up_16.png\" alt=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.import"));
	        	    	_xhtml_out.print("\" title=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.import"));
	        	    	_xhtml_out.println("\"/></a>");
	        	    	_xhtml_out.print("<a href=\"javascript:exportConfiguration();\"><img src=\"/images/arrow_down_16.png\" alt=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.export"));
	        	    	_xhtml_out.print("\" title=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.export"));
	        	    	_xhtml_out.println("\"/></a>");
	        	    	_xhtml_out.print("<a href=\"javascript:removeConfiguration();\"><img src=\"/images/calendar_delete_16.png\" alt=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
	        	    	_xhtml_out.print("\" title=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
	        	    	_xhtml_out.println("\"/></a>");	        	    	
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"external_share\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.backup.external_volume"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"external_share\" id=\"external_share\">");
	        	    	_xhtml_out.println("<option id=\"external_share\" value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	                    for(String _share : _shares) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_share);
							_xhtml_out.print("\"");
							if(_sc.getExportShare() != null && _sc.getExportShare().equals(_share)) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_share);
							_xhtml_out.println("</option>");
						}
	                    _xhtml_out.println("</select>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"hour\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.backup.export_hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"hour\" id=\"hour\">");
	                    _xhtml_out.println("<option id=\"hour\" value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	                    for(int i = 0; i < 24; i++) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(i);
							_xhtml_out.print("\"");
							if(_sc.getExportHour() == i) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(i);
							_xhtml_out.println(":00</option>");
						}
	                    _xhtml_out.println("</select>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"retention\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.backup.day_retention"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"retention\" id=\"retention\" value=\"");
	                    _xhtml_out.print(_sc.getExportRetention()!=0 ? _sc.getExportRetention() : "");
	                    _xhtml_out.print("\"/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");*/
	                    
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.println(getLanguageMessage("system.manteinance.diskconfigurations"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.maintenance.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"retention\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.manteinance.multipath"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"multipath\" id=\"multipath\" value=\"true\"");
	        	    	if (MultiPathManager.isMultipathEnabled())
	        	    		_xhtml_out.print(" checked=\"checked\" ");
	        	   		_xhtml_out.print(" />");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("</form>");
	    			}
        		    break;
	    		case VACUUM_DATABASE: {
		    			DBConnection _db = new DBConnectionManager(this.sessionManager.getConfiguration()).getConnection();
		    			_db.query("VACUUM ANALYZE file");
	    				_db.query("VACUUM ANALYZE path");
	    				_db.query("VACUUM ANALYZE filename");
	    				_db.query("VACUUM ANALYZE job");
	    				writeDocumentResponse("Optimizaci&oacute;n realizada correctamente.", "/admin/SystemMaintenance");
	    			}
	    			break;
	    		case EXPORTCONF: {
		    			if(request.getParameter("external_share") == null || request.getParameter("external_share").isEmpty()) {
		    				throw new Exception(getLanguageMessage("system.manteinance.exception_to_volume"));
		    			}
		    			_sc.exportConfigutation(request.getParameter("external_share"));
		    			writeDocumentResponse(getLanguageMessage("system.manteinance.message.configuration_export"), "/admin/SystemMaintenance");
	    			}
	    			break;
	    		case IMPORTCONF: {
		    			if(request.getParameter("external_share") == null || request.getParameter("external_share").isEmpty()) {
		    				throw new Exception(getLanguageMessage("system.manteinance.exception_from_volume"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				_sc.importConfigutation(request.getParameter("external_share"));
		    				writeDocumentResponse(getLanguageMessage("system.manteinance.import_ok"), "/admin/SystemMaintenance");
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("system.manteinance.question"), "/admin/SystemMaintenance?type=" + IMPORTCONF + "&external_share=" + request.getParameter("external_share") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case EXPORTCONF_PLAN: {
		    			int max_connections = 500, shared_buffers = -1, cache = -1;
	    				if(request.getParameter("max_connections") != null) {
		    				try {
		    					max_connections = Integer.parseInt(request.getParameter("max_connections"));
		    				} catch(NumberFormatException _ex) {}
		    			}
		    			if(request.getParameter("shared_buffers") != null) {
		    				try {
		    					shared_buffers = Integer.parseInt(request.getParameter("shared_buffers"));
		    				} catch(NumberFormatException _ex) {}
		    			}
		    			if(request.getParameter("cache") != null) {
		    				try {
		    					cache = Integer.parseInt(request.getParameter("cache"));
		    				} catch(NumberFormatException _ex) {}
		    			}
		    			if(request.getParameter("external_share") != null && request.getParameter("hour") != null &&
		    					!request.getParameter("external_share").isEmpty() && !request.getParameter("hour").isEmpty()) {
		    				try {
		    					_sc.setExportHour(Integer.parseInt(request.getParameter("hour")));
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("system.manteinance.no_format_time"));
		    				}
		    				_sc.setExportShare(request.getParameter("external_share"));
		    			} else {
		    				_sc.setExportHour(-1);
		    				_sc.setExportShare(null);
		    			}
		    			if(request.getParameter("retention") != null && !request.getParameter("retention").isEmpty()) {
		    				try {
		    					_sc.setExportRetention(Integer.parseInt(request.getParameter("retention")));
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("system.manteinance.no_format"));
		    				}
		    			} else{
		    				_sc.setExportRetention(-1);
		    			}
		    			
		    			if(request.getParameter("multipath") != null && request.getParameter("multipath").equals("true")) {
		    				if (!MultiPathManager.isMultipathEnabled())
		    					MultiPathManager.enableMultipath();
		    			}
		    			else {
		    				if (MultiPathManager.isMultipathEnabled())
		    					MultiPathManager.disableMultipath();
		    			}
		    			DefaultConfiguration.configureSystem();
		    			GeneralSystemConfiguration.setDataBaseConfiguration(max_connections, shared_buffers, cache);
		    			writeDocumentResponse(getLanguageMessage("system.manteinance.message.configuration_saved"), "/admin/SystemMaintenance");
	    			}
	    			break;
	    	}
 		} catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}
	
	