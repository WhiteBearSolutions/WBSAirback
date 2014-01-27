package com.whitebearsolutions.imagine.wbsairback.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;

public class BackupFilesetsClients extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int FILESET_ADD = 2;
	public final static int FILESET_EDIT = 5;
	public final static int FILESET_SAVE = 8;
	public final static int FILESET_DELETE = 11;
	private int type;
	public final static String baseUrl = "/admin/"+BackupFilesetsClients.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	response.setContentType("text/html");
	    	this.type = 1;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
		    
		    switch(this.type) {
	    		default: {
	    				writeDocumentHeader();
		    			int _offset = 0;
		    			List<Map<String, String>> _filesets = FileSetManager.getAllFileSets();
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/page_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.clients"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.clients.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.fileset.clients"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\""+baseUrl+"?type=");
	                    _xhtml_out.print(FILESET_ADD);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
		               	_xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_filesets.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.fileset.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.fileset.hash"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.fileset.compression"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> fileset : _filesets) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if(fileset.containsKey("md5")) {
									_xhtml_out.print(getLanguageMessage("common.message."+fileset.get("md5")));
								} else {
									_xhtml_out.print(getLanguageMessage("common.message.no"));
								}
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if(fileset.containsKey("compression")) {
									_xhtml_out.print(fileset.get("compression"));
								} else {
									_xhtml_out.print(FileSetManager.COMPRESSION_NONE);
								}
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print("<a href=\""+baseUrl+"?type=");
								_xhtml_out.print(FILESET_EDIT);
								_xhtml_out.print("&name=");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.print("\"><img src=\"/images/page_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\""+baseUrl+"?type=");
								_xhtml_out.print(FILESET_DELETE);
								_xhtml_out.print("&name=");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.print("\"><img src=\"/images/page_delete_16.png\" title=\"");
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
			            	_xhtml_out.println(getLanguageMessage("device.message.no_filesets"));
			            	_xhtml_out.println("</td>");
			                _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
			            _xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.print("</div>");
		    		}
        		    break;
	    		case FILESET_ADD: {
	    				writeDocumentHeader();
		    			
		    			int _add_plugins = 1, _offset = 0;
		    			if(request.getParameter("add-plugins") != null) {
	                    	try {
	                    		_add_plugins = Integer.parseInt(request.getParameter("add-plugins"));
	                    	} catch(NumberFormatException _ex) {}
		    			}
		    			
		    			writeDocumentBack(baseUrl);
		    			_xhtml_out.println("<script>");
	    				_xhtml_out.println("<!--");
	    				_xhtml_out.println("function AddPlugin() {");
	    				_xhtml_out.print("  document.fileset.type.value = ");
	    				_xhtml_out.print(FILESET_ADD);
	    				_xhtml_out.println(";");
	    				_xhtml_out.println("  submitForm(document.fileset.submit());");
	    				_xhtml_out.println("}");
	    				_xhtml_out.println("function checkPlugin() { ");
	                    _xhtml_out.println("	var disableVSS=false; ");
	                    _xhtml_out.println("	var i=0; ");
	                    _xhtml_out.println("	while (document.getElementById('plugin'+i)) { ");
	                    _xhtml_out.println(" 		var plugin = document.getElementById('plugin'+i).value;");
	                    _xhtml_out.println(" 		if (plugin != 'delta' && plugin != '') {");
	                    _xhtml_out.println("			disableVSS=true; ");
	                    _xhtml_out.println("		}");
	                    _xhtml_out.println("		i++; ");
	                    _xhtml_out.println("	} ");
	                    _xhtml_out.println("	if (disableVSS) {");
	                    _xhtml_out.println("		document.fileset.vss.value='no';");
	                    _xhtml_out.println("		document.fileset.vss.disabled='disabled';");
	                    _xhtml_out.println("		document.fileset.vss.style.backgroundColor='#E6E9EA';");
	                    _xhtml_out.println("	} else {");
	                    _xhtml_out.println("		document.fileset.vss.disabled='';");
	                    _xhtml_out.println("		document.fileset.vss.style.backgroundColor='#FFFFFF';");
	                    _xhtml_out.println("	} ");
	                    _xhtml_out.println("} ");
	    				_xhtml_out.println("-->");
	    				_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"fileset\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + FILESET_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"add-plugins\" value=\"" + (_add_plugins + 1) + "\"/>");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/page_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.clients"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.clients.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.fileset.new_fileset"));
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.fileset.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"name\"");
	        	    	if(request.getParameter("name") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(request.getParameter("name"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"include\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.include"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<textarea class=\"form_textarea\" name=\"include\" cols=\"50\" rows=\"5\" wrap=\"off\">");
	        	    	if(request.getParameter("include") != null) {
	        	    		_xhtml_out.print(request.getParameter("include"));
	        	    	}
	        	    	_xhtml_out.println("</textarea>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"exclude\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.exclude"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<textarea class=\"form_textarea\" name=\"exclude\" cols=\"50\" rows=\"5\" wrap=\"off\">");
	        	    	if(request.getParameter("exclude") != null) {
	        	    		_xhtml_out.print(request.getParameter("exclude"));
	        	    	}
	        	    	_xhtml_out.println("</textarea>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"extension\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.extensions"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"extension\"");
	        	    	if(request.getParameter("extension") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(request.getParameter("extension"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"md5\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.hash"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"md5\">");
						_xhtml_out.print("<option value=\"yes\"");
	        	    	if(request.getParameter("md5") != null &&
	        	    			"yes".equals(request.getParameter("md5"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
	        	    	if(request.getParameter("md5") != null &&
	        	    			"no".equals(request.getParameter("md5"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"compression\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.compression"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"compression\">");
	        	    	for (String compression : FileSetManager.SUPPORTED_COMPRESSIONS) {
	        	    		_xhtml_out.print("<option value=\""+compression+"\"");
		        	    	if(request.getParameter("compression") != null &&
		        	    			compression.equals(request.getParameter("compression"))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.println(">");
							_xhtml_out.print(compression);
							_xhtml_out.print("</option>");
	        	    	}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"acls\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.acl"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"acls\">");
						_xhtml_out.print("<option value=\"yes\"");
	        	    	if(request.getParameter("acls") != null &&
	        	    			"yes".equals(request.getParameter("acls"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
	        	    	if(request.getParameter("acls") != null &&
	        	    			"no".equals(request.getParameter("acls"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"multiplefs\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.multiplefs"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"multiplefs\">");
						_xhtml_out.print("<option value=\"yes\"");
	        	    	if(request.getParameter("multiplefs") != null &&
	        	    			"yes".equals(request.getParameter("multiplefs"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
	        	    	if(request.getParameter("multiplefs") != null &&
	        	    			"no".equals(request.getParameter("multiplefs"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	} else if(request.getParameter("multiplefs") == null ||
	        	    			!"yes".equals(request.getParameter("multiplefs"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"vss\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.vss"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"vss\">");
						_xhtml_out.print("<option value=\"yes\"");
	        	    	if(request.getParameter("vss") != null &&
	        	    			"yes".equals(request.getParameter("vss"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
	        	    	if(request.getParameter("vss") != null &&
	        	    			"no".equals(request.getParameter("vss"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.fileset.plugins"));
						_xhtml_out.println("<a href=\"javascript:submitForm(document.fileset.submit());\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.save"));
		               	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		               	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:AddPlugin();\"><img src=\"/images/add_16.png\"/ title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.add"));
		               	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    for(int i = 0; i < _add_plugins; i++) {
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/brick_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.print("<select class=\"form_select\" onChange=\"checkPlugin();\" name=\"plugin");
	                    	_xhtml_out.print(i);
	                    	_xhtml_out.print("\" id=\"plugin");
	                    	_xhtml_out.print(i);
	                    	_xhtml_out.print("\">");
		        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	_xhtml_out.print("<option value=\"systemstate\"");
		        	    	if(request.getParameter("plugin" + i) != null &&
		        	    			"systemstate".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.println(">Microsoft Windows Systemstate</option>");
		        	    	_xhtml_out.println("<option value=\"sharepoint\"");
		        	    	if(request.getParameter("plugin" + i) != null &&
		        	    			"sharepoint".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.println(">Microsoft Sharepoint</option>");
		        	    	_xhtml_out.println("<option value=\"mssql\"");
		        	    	if(request.getParameter("plugin" + i) != null &&
		        	    			"mssql".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.println(">Microsoft SQL Server</option>");
							_xhtml_out.println("<option value=\"exchange\"");
		        	    	if(request.getParameter("plugin" + i) != null &&
		        	    			"exchange".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.println(">Microsoft Exchange Server</option>");
							_xhtml_out.println("</select>");
	                    	_xhtml_out.println("</td>");
	                    	_xhtml_out.println("</tr>");
	                    	_offset++;
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
	                    _xhtml_out.println("<script>checkPlugin();</script>");
		    		}
	    			break;
	    		case FILESET_EDIT: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_filesets"));
		    			}
		    			
		    			int _add_plugins = 1, _offset = 0;
		    			if(request.getParameter("add-plugins") != null) {
	                    	try {
	                    		_add_plugins = Integer.parseInt(request.getParameter("add-plugins"));
	                    	} catch(NumberFormatException _ex) {}
		    			}
		    			
		    			Map<String, String> _fileset = FileSetManager.getFileSet(request.getParameter("name"));
		    			List<String> _fileset_plugins = FileSetManager.getFilesetPlugins(request.getParameter("name"));
		    			
		    			writeDocumentBack(baseUrl);
		    			_xhtml_out.println("<script>");
	    				_xhtml_out.println("<!--");
	    				_xhtml_out.println("function AddPlugin() {");
	    				_xhtml_out.print("  document.fileset.type.value = ");
	    				_xhtml_out.print(FILESET_EDIT);
	    				_xhtml_out.println(";");
	    				_xhtml_out.println("  submitForm(document.fileset.submit());");
	    				_xhtml_out.println("}");
	    				_xhtml_out.println("function checkPlugin() { ");
	                    _xhtml_out.println("	var disableVSS=false; ");
	                    _xhtml_out.println("	var i=0; ");
	                    _xhtml_out.println("	while (document.getElementById('plugin'+i)) { ");
	                    _xhtml_out.println(" 		var plugin = document.getElementById('plugin'+i).value;");
	                    _xhtml_out.println(" 		if (plugin != 'delta' && plugin != '') {");
	                    _xhtml_out.println("			disableVSS=true; ");
	                    _xhtml_out.println("		}");
	                    _xhtml_out.println("		i++; ");
	                    _xhtml_out.println("	} ");
	                    _xhtml_out.println("	if (disableVSS) {");
	                    _xhtml_out.println("		document.fileset.vss.value='no';");
	                    _xhtml_out.println("		document.fileset.vss.disabled='disabled';");
	                    _xhtml_out.println("		document.fileset.vss.style.backgroundColor='#E6E9EA';");
	                    _xhtml_out.println("	} else {");
	                    _xhtml_out.println("		document.fileset.vss.disabled='';");
	                    _xhtml_out.println("		document.fileset.vss.style.backgroundColor='#FFFFFF';");
	                    _xhtml_out.println("	} ");
	                    _xhtml_out.println("} ");
	    				_xhtml_out.println("-->");
	    				_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"fileset\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + FILESET_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"add-plugins\" value=\"" + (_add_plugins + 1) + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + _fileset.get("name") + "\"/>");
						_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/page_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.clients"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.clients.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.fileset.edit_fileset"));
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.fileset.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"_name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    	_xhtml_out.print(_fileset.get("name"));
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"include\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.include"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<textarea class=\"form_textarea\" name=\"include\" cols=\"50\" rows=\"5\" wrap=\"off\">");
	        	    	if(_fileset.get("include") != null) {
	        	    		_xhtml_out.print(_fileset.get("include"));
	        	    	}
	        	    	_xhtml_out.println("</textarea>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"exclude\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.exclude"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<textarea class=\"form_textarea\" name=\"exclude\" cols=\"50\" rows=\"5\" wrap=\"off\">");
	        	    	if(_fileset.get("exclude") != null) {
	        	    		_xhtml_out.print(_fileset.get("exclude"));
	        	    	}
    	    			_xhtml_out.println("</textarea>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"extension\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.extensions"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"extension\"");
	        	    	if(_fileset.get("extension") != null) {
	        	    		_xhtml_out.print(" value=\"");
		        	    	_xhtml_out.print(_fileset.get("extension"));
		        	    	_xhtml_out.print("\"");
	        	    	}
    	    			_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"md5\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.hash"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"md5\">");
						_xhtml_out.print("<option value=\"yes\"");
						if(_fileset.containsKey("md5") && _fileset.get("md5").equals("yes")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if(!_fileset.containsKey("md5") || _fileset.get("md5").equals("no")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"compression\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.compression"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"compression\">");
	        	    	for (String compression : FileSetManager.SUPPORTED_COMPRESSIONS) {
	        	    		_xhtml_out.print("<option value=\""+compression+"\"");
		        	    	if(request.getParameter("compression") != null &&
		        	    		compression.equals(request.getParameter("compression")) || 
		        	    		(_fileset.containsKey("compression") && _fileset.get("compression").equals(compression))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.println(">");
							_xhtml_out.print(compression);
							_xhtml_out.print("</option>");
	        	    	}
						_xhtml_out.println("</select>");

	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"acls\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.acl"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"acls\">");
						_xhtml_out.print("<option value=\"yes\"");
						if(_fileset.containsKey("acl") && _fileset.get("acl").equals("yes")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if(!_fileset.containsKey("acl") || _fileset.get("acl").equals("no")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"multiplefs\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.multiplefs"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"multiplefs\">");
						_xhtml_out.print("<option value=\"yes\"");
						if(_fileset.containsKey("multiplefs") && _fileset.get("multiplefs").equals("yes")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if(!_fileset.containsKey("multiplefs") || _fileset.get("multiplefs").equals("no")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"vss\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.vss"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"vss\">");
						_xhtml_out.print("<option value=\"yes\"");
						if(_fileset.containsKey("vss") && _fileset.get("vss").equals("yes")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if(!_fileset.containsKey("vss") || _fileset.get("vss").equals("no")) {
							_xhtml_out.print(" selected=\"selected\"");
			    		}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.print("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.fileset.plugins"));
						_xhtml_out.println("<a href=\"javascript:submitForm(document.fileset.submit());\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.save"));
		               	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		               	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:AddPlugin();\"><img src=\"/images/add_16.png\"/ title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.add"));
		               	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    for(int i = 0; i < _add_plugins || i < _fileset_plugins.size(); i++) {
	                    	String _plugin = null;
	                    	if(i < _fileset_plugins.size()) {
	                    		_plugin = _fileset_plugins.get(i);
	                    	}
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/brick_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.print("<select class=\"form_select\" onChange=\"checkPlugin();\" name=\"plugin");
	                    	_xhtml_out.print(i);
	                    	_xhtml_out.print("\" id=\"plugin");
	                    	_xhtml_out.print(i);
	                    	_xhtml_out.print("\">");
		        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	_xhtml_out.println("<option value=\"systemstate\"");
		        	    	if(request.getParameter("plugin" + i) != null &&
		        	    			"systemstate".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	} else if(_plugin != null && _plugin.equals("systemstate")) {
								_xhtml_out.print(" selected=\"selected\"");
				    		}
							_xhtml_out.print(">Microsoft Windows Systemstate</option>");
		        	    	_xhtml_out.println("<option value=\"sharepoint\"");
		        	    	if(request.getParameter("plugin" + i) != null &&
		        	    			"sharepoint".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	} else if(_plugin != null && _plugin.equals("sharepoint")) {
								_xhtml_out.print(" selected=\"selected\"");
				    		}
							_xhtml_out.print(">Microsoft Sharepoint</option>");
		        	    	_xhtml_out.println("<option value=\"mssql\"");
		        	    	if(request.getParameter("plugin" + i) != null &&
		        	    			"mssql".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	} else if(_plugin != null && _plugin.equals("mssql")) {
								_xhtml_out.print(" selected=\"selected\"");
				    		}
							_xhtml_out.print(">Microsoft SQL Server</option>");
							_xhtml_out.print("<option value=\"exchange\"");
							if(request.getParameter("plugin" + i) != null &&
		        	    			"exchange".equals(request.getParameter("plugin" + i))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	} else if(_plugin != null && _plugin.equals("exchange")) {
								_xhtml_out.print(" selected=\"selected\"");
				    		}
							_xhtml_out.print(">Microsoft Exchange Server</option>");
							_xhtml_out.println("</select>");
	                    	_xhtml_out.println("</td>");
	                    	_xhtml_out.println("</tr>");
	                    	_offset++;
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
	                    _xhtml_out.println("<script> checkPlugin();</script>");
		    		}
	    			break;
	    		case FILESET_SAVE: {
		    			
		    			boolean md5 = false;
		    			String compression = FileSetManager.COMPRESSION_NONE;
		    			boolean multiplefs = false;
		    			boolean acl = false;
		    			boolean vss = false;
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_filesets"));
		    			}
		    			if(request.getParameter("md5") != null && !request.getParameter("md5").isEmpty() && "yes".equals(request.getParameter("md5"))) {
		    				md5 = true;
		    			}
		    			if(request.getParameter("compression") != null && !request.getParameter("compression").isEmpty()) {
		    				if (FileSetManager.SUPPORTED_COMPRESSIONS.contains(request.getParameter("compression")))
		    					compression = request.getParameter("compression");
		    			}
		    			if(request.getParameter("acls") != null && !request.getParameter("acls").isEmpty() && "yes".equals(request.getParameter("acls"))) {
		    				acl = true;
		    			}
		    			if(request.getParameter("multiplefs") != null && !request.getParameter("multiplefs").isEmpty() && "yes".equals(request.getParameter("multiplefs"))) {
		    				multiplefs = true;
		    			}
		    			if(request.getParameter("vss") != null && !request.getParameter("vss").isEmpty() && "yes".equals(request.getParameter("vss"))) {
		    				vss = true;
		    			}
		    			
		    			List<Integer> _plugins = new ArrayList<Integer>();
		    			for(int i = 0; request.getParameter("plugin" + i) != null; i++) {
		    				if(!request.getParameter("plugin" + i).isEmpty()) {
		    					if("systemstate".equals(request.getParameter("plugin" + i))) {
			    					_plugins.add(FileSetManager.PLUGIN_SYSTEMSTATE);
			    				}
			    				if("sharepoint".equals(request.getParameter("plugin" + i))) {
			    					_plugins.add(FileSetManager.PLUGIN_SHAREPOINT);
			    				}
			    				if("mssql".equals(request.getParameter("plugin" + i))) {
			    					_plugins.add(FileSetManager.PLUGIN_MSSQL);
			    				}
			    				if("exchange".equals(request.getParameter("plugin" + i))) {
			    					_plugins.add(FileSetManager.PLUGIN_EXCHANGE);
			    				}
		    				}
		    			}
		    			
		    			if(request.getParameter("modify") == null || !"yes".equals(request.getParameter("modify"))){
		    				FileSetManager.addFileSet(request.getParameter("name"), request.getParameter("include"), request.getParameter("exclude"), removeSpaces(request.getParameter("extension")), md5, compression, acl, multiplefs, vss, _plugins);
		    			} else {
		    				FileSetManager.updateFileSet(request.getParameter("name"), request.getParameter("include"), request.getParameter("exclude"), removeSpaces(request.getParameter("extension")), md5, compression, acl, multiplefs, vss, _plugins);
		    			}
		    			this.redirected=true;
		    			response.sendRedirect(baseUrl);
	    			}
	    			break;
	    		case FILESET_DELETE: {
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_filesets"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				FileSetManager.removeFileSet(request.getParameter("name"));
		    				response.sendRedirect(baseUrl);
		    				this.redirected=true;
		    			} else {
		    				writeDocumentHeader();
		    				writeDocumentQuestion(getLanguageMessage("backup.fileset.question"), baseUrl+"?type=" + FILESET_DELETE + "&name=" + request.getParameter("name") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    	}
 		} catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
	
	private static String removeSpaces(String s) {
		if(s == null) {
			return "";
		}
		StringTokenizer st = new StringTokenizer(s, " ", false);
		String t="";
		while(st.hasMoreElements()) t += st.nextElement();
		return t;
	}
}
