package com.whitebearsolutions.imagine.wbsairback.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;

public class BackupFilesetsNDMP extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int FILESET_NDMP_ADD = 4;
	public final static int FILESET_NDMP_EDIT = 7;
	public final static int FILESET_NDMP_SAVE = 10;
	public final static int FILESET_DELETE = 11;
	private int type;
	
	public final static String baseUrl = "/admin/"+BackupFilesetsNDMP.class.getSimpleName();
	
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
		    
		    switch(this.type) {
	    		default: {
	    				writeDocumentHeader();
		    			int _offset = 0;
		    			List<Map<String, String>> _ndmp_filesets = FileSetManager.getAllNDMPFileSets();
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/fileset_ndmp_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.info"));
		    			_xhtml_out.println("</div>");
				    	
				    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp_filesets"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                _xhtml_out.println("\"/></a>");
		                _xhtml_out.print("<a href=\""+baseUrl+"?type=");
	                    _xhtml_out.print(FILESET_NDMP_ADD);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_ndmp_filesets.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.fileset.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.address"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> fileset : _ndmp_filesets) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if(fileset.containsKey("ndmp.address")) {
									_xhtml_out.print(fileset.get("ndmp.address"));
								}
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if(fileset.containsKey("ndmp.type")) {
									_xhtml_out.print(fileset.get("ndmp.type"));
								}
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print("<a href=\""+baseUrl+"?type=");
								_xhtml_out.print(FILESET_NDMP_EDIT);
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
	    		case FILESET_NDMP_ADD: {
	    				writeDocumentHeader();
		    			
		    			int _add_volumes = 1, _offset = 0;
		    			if(request.getParameter("add-volumes") != null) {
	                    	try {
	                    		_add_volumes = Integer.parseInt(request.getParameter("add-volumes"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
		    			
		    			writeDocumentBack(baseUrl);
		    			_xhtml_out.println("<script>");
	    				_xhtml_out.println("<!--");
	    				_xhtml_out.println("function AddVolume() {");
	    				_xhtml_out.print("  document.fileset.type.value = ");
	    				_xhtml_out.print(FILESET_NDMP_ADD);
	    				_xhtml_out.println(";");
	    				_xhtml_out.println("  submitForm(document.fileset.submit());");
	    				_xhtml_out.println("}");
	    				_xhtml_out.println("-->");
	    				_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"fileset\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + FILESET_NDMP_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"add-volumes\" value=\"" + (_add_volumes + 1) + "\"/>");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/fileset_ndmp_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.info"));
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
	        	    	_xhtml_out.print("<label for=\"addess1\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.address"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address1\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address2\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address3\"/>");
	                    _xhtml_out.print(".");
		                _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address4\"/>");
	                    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"port\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.port"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" style=\"width:50px\" type=\"text\" name=\"port\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\"/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ndmp_auth\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.auth"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"ndmp_auth\">");
	        	    	_xhtml_out.print("<option value=\"none\">NONE</option>");
	        	    	_xhtml_out.print("<option value=\"text\">TEXT</option>");
	        	    	_xhtml_out.print("<option value=\"md5\">MD5</option>");
	        	    	_xhtml_out.println("</select>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ndmp_type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"ndmp_type\">");
	        	    	_xhtml_out.print("<option value=\"dump\">DUMP</option>");
	        	    	/*_xhtml_out.println("<option value=\"tar\">TAR</option>");
	        	    	_xhtml_out.println("<option value=\"smtape\">SMTAPE</option>");*/
	        	    	_xhtml_out.println("</select>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
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
	                    _xhtml_out.println("<a href=\"javascript:AddVolume();\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    _xhtml_out.println("<tr>");
                    	_xhtml_out.print("<td></td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.volume"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.volume.file"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	                    for(int i = 0; i < _add_volumes; i++) {
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"volume" + i + "\"/>");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("<td>");
	                    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"volume_file" + i + "\"/>");
		                    _xhtml_out.println("</td>");
	                    	_xhtml_out.println("</tr>");
	                    	_offset++;
	                    }
	                    _xhtml_out.println("</table>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
		    		}
	    			break;
	    		case FILESET_NDMP_EDIT: {
	    				writeDocumentHeader();
		    			
		    			int _add_volumes = 1, _offset = 0;
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_filesets"));
		    			}
		    			
		    			if(request.getParameter("add-volumes") != null) {
	                    	try {
	                    		_add_volumes = Integer.parseInt(request.getParameter("add-volumes"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
		    			
		    			boolean _auth_defined = false, _type_defined = false;
		    			Map<String, String> _fileset = FileSetManager.getFileSet(request.getParameter("name"));
		    			Map<String, String> _fileset_volumes = FileSetManager.getNDMPFilesetVolumes(request.getParameter("name"));
		    			List<String> _fileset_volumes_names = new ArrayList<String>(_fileset_volumes.keySet());
		    			
		    			String[] _address = new String[] { "", "", "", "" };
		    			if(_fileset.get("ndmp.address") != null) {
	        	    		try {
	        	    			_address = NetworkManager.toAddress(_fileset.get("ndmp.address"));
	        	    		} catch(Exception _ex) {}
	        	    	}
		    			if(_add_volumes < _fileset_volumes_names.size()) {
		    				_add_volumes = _fileset_volumes_names.size();
		    			}
		    			
		    			writeDocumentBack(baseUrl);
		    			_xhtml_out.println("<script>");
	    				_xhtml_out.println("<!--");
	    				_xhtml_out.println("function AddVolume() {");
	    				_xhtml_out.print("  document.fileset.type.value = ");
	    				_xhtml_out.print(FILESET_NDMP_EDIT);
	    				_xhtml_out.println(";");
	    				_xhtml_out.println("  submitForm(document.fileset.submit());");
	    				_xhtml_out.println("}");
	    				_xhtml_out.println("-->");
	    				_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"fileset\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + FILESET_NDMP_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"add-volumes\" value=\"" + (_add_volumes + 1) + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/fileset_ndmp_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.info"));
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
	        	    	_xhtml_out.print("<label for=\"addess1\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.address"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address1\" value=\"");
	        	    	_xhtml_out.print(_address[0]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address2\" value=\"");
	        	    	_xhtml_out.print(_address[1]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address3\" value=\"");
	        	    	_xhtml_out.print(_address[2]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
		                _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"address4\" value=\"");
	        	    	_xhtml_out.print(_address[3]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"port\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.port"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" style=\"width:50px\" type=\"text\" name=\"port\"");
	        	    	if(request.getParameter("port") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(request.getParameter("port"));
	        	    		_xhtml_out.print("\"");
	        	    	} else if(_fileset.get("ndmp.port") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_fileset.get("ndmp.port"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\"");
	        	    	if(request.getParameter("user") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(request.getParameter("user"));
	        	    		_xhtml_out.print("\"");
	        	    	} else if(_fileset.get("ndmp.user") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_fileset.get("ndmp.user"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"");
	        	    	if(request.getParameter("password") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(request.getParameter("password"));
	        	    		_xhtml_out.print("\"");
	        	    	} else if(_fileset.get("ndmp.password") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_fileset.get("ndmp.password"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ndmp_auth\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.auth"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"ndmp_auth\">");
	        	    	_xhtml_out.print("<option value=\"none\"");
	        	    	if(request.getParameter("ndmp_auth") != null &&
	        	    			request.getParameter("ndmp_auth").equalsIgnoreCase("none")) {
	        	    		_auth_defined = true;
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	} else if(!_auth_defined && _fileset.get("ndmp.auth") != null &&
	        	    			_fileset.get("ndmp.auth").equalsIgnoreCase("none")) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">NONE</option>");
	        	    	_xhtml_out.print("<option value=\"text\"");
	        	    	if(request.getParameter("ndmp_auth") != null &&
	        	    			request.getParameter("ndmp_auth").equalsIgnoreCase("text")) {
	        	    		_auth_defined = true;
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	} else if(!_auth_defined && _fileset.get("ndmp.auth") != null &&
	        	    			_fileset.get("ndmp.auth").equalsIgnoreCase("text")) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">TEXT</option>");
	        	    	_xhtml_out.print("<option value=\"md5\"");
	        	    	if(request.getParameter("ndmp_auth") != null &&
	        	    			request.getParameter("ndmp_auth").equalsIgnoreCase("md5")) {
	        	    		_auth_defined = true;
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	} else if(!_auth_defined && _fileset.get("ndmp.auth") != null &&
	        	    			_fileset.get("ndmp.auth").equalsIgnoreCase("md5")) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">MD5</option>");
	        	    	_xhtml_out.println("</select>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ndmp_type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"ndmp_type\">");
	        	    	_xhtml_out.print("<option value=\"dump\"");
	        	    	if(request.getParameter("ndmp_type") != null &&
	        	    			request.getParameter("ndmp_type").equalsIgnoreCase("dump")) {
	        	    		_type_defined = true;
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	} else if(!_type_defined && _fileset.get("ndmp.type") != null &&
	        	    			_fileset.get("ndmp.type").equalsIgnoreCase("dump")) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">DUMP</option>");
	        	    	_xhtml_out.println("</select>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
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
	                    _xhtml_out.println("<a href=\"javascript:AddVolume();\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    _xhtml_out.println("<tr>");
	                	_xhtml_out.print("<td></td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.volume"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.fileset.ndmp.volume.file"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	                    for(int i = 0; i < _add_volumes; i++) {
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"volume");
	                    	_xhtml_out.print(i);
	                    	_xhtml_out.print("\"");
	                    	if(_offset < _fileset_volumes_names.size() &&
	                    			_fileset_volumes_names.get(_offset) != null) {
	                    		_xhtml_out.print(" value=\"");
	                    		_xhtml_out.print(_fileset_volumes_names.get(_offset));
	                    		_xhtml_out.print("\"");
	                    	}
	                    	_xhtml_out.println("/>");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("<td>");
	                    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"volume_file");
	                    	_xhtml_out.print(i);
	                    	_xhtml_out.print("\"");
	                    	if(_offset < _fileset_volumes_names.size() &&
	                    			_fileset_volumes_names.get(_offset) != null &&
	                    			_fileset_volumes.get(_fileset_volumes_names.get(_offset)) != null) {
	                    		_xhtml_out.print(" value=\"");
	                    		_xhtml_out.print(_fileset_volumes.get(_fileset_volumes_names.get(_offset)));
	                    		_xhtml_out.print("\"");
	                    	}
	                    	_xhtml_out.println("/>");
		                    _xhtml_out.println("</td>");
	                    	_xhtml_out.println("</tr>");
	                    	_offset++;
	                    }
	                    _xhtml_out.println("</table>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
	    			}
	    			break;
	    		case FILESET_NDMP_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_filesets"));
		    			}
		    			
		    			if(request.getParameter("port") == null || request.getParameter("port").isEmpty()) {
		    				throw new Exception("backup.fileset.error.invalid_port");
		    			}
		    			
		    			int _port, _auth_type = FileSetManager.NDMP_AUTH_NONE, _ndmp_type = FileSetManager.NDMP_TYPE_DUMP;
		    			String[] _address;
		    			Map<String, String> _volumes = new HashMap<String, String>();
		    			for(int i = 0; request.getParameter("volume" + i) != null; i++) {
		    				if(!request.getParameter("volume" + i).isEmpty()) {
		    					if(request.getParameter("volume_file" + i) != null &&
		    							!request.getParameter("volume_file" + i).isEmpty()) {
		    						_volumes.put(request.getParameter("volume" + i), request.getParameter("volume_file" + i));
		    					} else {
		    						_volumes.put(request.getParameter("volume" + i), null);
		    					}
		    				}
		    			}
		    			
		    			if(_volumes.isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_volume"));
		    			}
		    			
		    			if(request.getParameter("address1") == null || request.getParameter("address1").trim().isEmpty() ||
    							request.getParameter("address2") == null || request.getParameter("address2").trim().isEmpty() ||
    							request.getParameter("address3") == null || request.getParameter("address3").trim().isEmpty() ||
    							request.getParameter("address4") == null || request.getParameter("address4").trim().isEmpty()) {
    						throw new Exception("backup.fileset.error.invalid_address");
    					}
		    			
		    			try {
		    				_port = Integer.parseInt(request.getParameter("port"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception("backup.fileset.error.invalid_port");
		    			}
    					
    					_address = new String[] { request.getParameter("address1"), request.getParameter("address2"), request.getParameter("address3"), request.getParameter("address4") };
	    				
    					if(request.getParameter("ndmp_auth") != null && "text".equalsIgnoreCase(request.getParameter("ndmp_auth"))) {
    						_auth_type = FileSetManager.NDMP_AUTH_TEXT;
    					} else if(request.getParameter("ndmp_auth") != null && "md5".equalsIgnoreCase(request.getParameter("ndmp_auth"))) {
    						_auth_type = FileSetManager.NDMP_AUTH_MD5;
    					}
    					
    					if(request.getParameter("ndmp_type") != null && "tar".equalsIgnoreCase(request.getParameter("ndmp_type"))) {
    						_ndmp_type = FileSetManager.NDMP_TYPE_TAR;
    					} else if(request.getParameter("ndmp_type") != null && "smtape".equalsIgnoreCase(request.getParameter("ndmp_type"))) {
    						_ndmp_type = FileSetManager.NDMP_TYPE_SMTAPE;
    					}
    					
		    			if(request.getParameter("modify") == null || !"yes".equals(request.getParameter("modify"))){
		    				FileSetManager.addNDMPFileSet(request.getParameter("name"), _address, _port, _auth_type, request.getParameter("user"), request.getParameter("password"), _volumes, _ndmp_type);
		    			} else {
		    				FileSetManager.updateNDMPFileSet(request.getParameter("name"), _address, _port, _auth_type, request.getParameter("user"), request.getParameter("password"), _volumes, _ndmp_type);
		    			}
		    			response.sendRedirect(baseUrl);
		    			this.redirected=true;
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
}
