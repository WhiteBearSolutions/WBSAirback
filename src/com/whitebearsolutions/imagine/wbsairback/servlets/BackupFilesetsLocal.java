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
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;

public class BackupFilesetsLocal extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int FILESET_LOCAL_ADD = 3;
	public final static int FILESET_LOCAL_EDIT = 6;
	public final static int FILESET_LOCAL_SAVE = 8;
	public final static int FILESET_DELETE = 11;
	private int type;
	
	public final static String baseUrl = "/admin/"+BackupFilesetsLocal.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
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
		    			List<Map<String, String>> _local_filesets = FileSetManager.getAllLocalFileSets();
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/fileset_local_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.local"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.local.info"));
		    			_xhtml_out.println("</div>");
				    	
				    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.fileset.local_fileset"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                _xhtml_out.println("\"/></a>");
		                _xhtml_out.print("<a href=\""+baseUrl+"?type=");
	                    _xhtml_out.print(FILESET_LOCAL_ADD);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_local_filesets.isEmpty()) {
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
		                    for(Map<String, String> fileset : _local_filesets) {
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
								_xhtml_out.print(FILESET_LOCAL_EDIT);
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
	    		case FILESET_LOCAL_ADD: {
	    				writeDocumentHeader();
		    			
		    			int _add_volumes = 1, _offset = 0;
		    			if(request.getParameter("add-volumes") != null) {
	                    	try {
	                    		_add_volumes = Integer.parseInt(request.getParameter("add-volumes"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
	    				List<Map<String, String>> _external_shares = ShareManager.getExternalShares();
	    				
	    				writeDocumentBack(baseUrl);
		    			_xhtml_out.println("<script>");
	    				_xhtml_out.println("<!--");
	    				_xhtml_out.println("function AddVolume() {");
	    				_xhtml_out.print("  document.fileset.type.value = ");
	    				_xhtml_out.print(FILESET_LOCAL_ADD);
	    				_xhtml_out.println(";");
	    				_xhtml_out.println("  submitForm(document.fileset.submit());");
	    				_xhtml_out.println("}");
	    				_xhtml_out.println("-->");
	    				_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"fileset\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + FILESET_LOCAL_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"add-volumes\" value=\"" + (_add_volumes + 1) + "\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/fileset_local_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.local"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.local.info"));
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.include_dir"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<textarea class=\"form_textarea\" name=\"include\" cols=\"50\" rows=\"5\" wrap=\"off\">");
	        	    	if(request.getParameter("include") != null) {
	        	    		_xhtml_out.print(request.getParameter("include"));
	        	    	}
	        	    	_xhtml_out.println("</textarea>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"extension\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.extensions"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"extension\"");
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
	        	    	if(request.getParameter("md5") != null && "yes".equals(request.getParameter("md5"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
	        	    	if(request.getParameter("md5") != null && "no".equals(request.getParameter("md5"))) {
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
		        	    			compression.equals(request.getParameter("compression"))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.println(">");
							_xhtml_out.print(compression);
							_xhtml_out.print("</option>");
	        	    	}
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
	                    _xhtml_out.print(getLanguageMessage("device.shares.lv"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("device.shares.external"));
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
	                    	_xhtml_out.println("<select class=\"form_select\" name=\"lv" + i + "\">");
		        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
							for(Map<String, String> _lv : _lvs) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(_lv.get("vg"));
								_xhtml_out.print("/");
								_xhtml_out.print(_lv.get("name"));
								_xhtml_out.print("\"");
								if(request.getParameter("lv" + i) != null && request.getParameter("lv" + i).equals(_lv.get("vg") + "/" + _lv.get("name"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(_lv.get("vg"));
								_xhtml_out.print("/");
								_xhtml_out.print(_lv.get("name"));
								_xhtml_out.println("</option>");
							}
							_xhtml_out.println("</select>");
	                    	_xhtml_out.println("</td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println("<select class=\"form_select\" name=\"share" + i + "\">");
		        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	for(Map<String, String> _share : _external_shares) {
		                    	_xhtml_out.print("<option value=\"");
		                    	_xhtml_out.print(_share.get("server"));
		                    	if (!_share.get("server").endsWith("/") && !_share.get("share").startsWith("/"))
		                    		_xhtml_out.print("/");
								_xhtml_out.print(_share.get("share"));
								_xhtml_out.print("\"");
								if(request.getParameter("share" + i) != null && request.getParameter("share" + i).equals(_share.get("server") + _share.get("share"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(_share.get("server"));
								if (!_share.get("server").endsWith("/") && !_share.get("share").startsWith("/"))
		                    		_xhtml_out.print("/");
								_xhtml_out.print(_share.get("share"));
								_xhtml_out.println("</option>");
		                    }
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
		    		}
	    			break;
	    		case FILESET_LOCAL_EDIT: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_filesets"));
		    			}
		    			int _add_volumes = 1, _offset = 0;
		    			if(request.getParameter("add-volumes") != null) {
	                    	try {
	                    		_add_volumes = Integer.parseInt(request.getParameter("add-volumes"));
	                    	} catch(NumberFormatException _ex) {}
		    			}
		    			Map<String, String> _fileset = FileSetManager.getLocalFileSet(request.getParameter("name"));
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
	    				List<Map<String, String>> _external_shares = ShareManager.getExternalShares();
	    				List<String> _fileset_volumes = FileSetManager.getLocalFilesetVolumes(request.getParameter("name"));
	    				if (_add_volumes == 1 && _fileset_volumes != null && _fileset_volumes.size()>0)
	    					_add_volumes = _fileset_volumes.size();
	    				
	    				writeDocumentBack(baseUrl);
		    			
	    				_xhtml_out.println("<script>");
	    				_xhtml_out.println("<!--");
	    				_xhtml_out.println("function AddVolume() {");
	    				_xhtml_out.print("  document.fileset.type.value = ");
	    				_xhtml_out.print(FILESET_LOCAL_EDIT);
	    				_xhtml_out.println(";");
	    				_xhtml_out.println("  submitForm(document.fileset.submit());");
	    				_xhtml_out.println("}");
	    				_xhtml_out.println("-->");
	    				_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"fileset\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + FILESET_LOCAL_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"add-volumes\" value=\"" + (_add_volumes + 1) + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + _fileset.get("name") + "\"/>");
						_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/fileset_local_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.fileset.local"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.fileset.local.info"));
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.include_dir"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<textarea class=\"form_textarea\" name=\"include\" cols=\"50\" rows=\"5\" wrap=\"off\">");
	        	    	if(_fileset.get("include") != null) {
	        	    		_xhtml_out.print(_fileset.get("include"));
	        	    	} else if (request.getParameter("include") != null) {
	        	    		_xhtml_out.print(request.getParameter("include"));
	        	    	}
	        	    	_xhtml_out.println("</textarea>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"extension\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.fileset.extensions"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"extension\"");
	        	    	if(request.getParameter("extension") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(request.getParameter("extension"));
	        	    		_xhtml_out.print("\"");
	        	    	} else if(_fileset.get("extension") != null) {
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
	        	    	if(request.getParameter("md5") != null && "yes".equals(request.getParameter("md5"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	} else if(_fileset.get("md5") != null && "yes".equals(_fileset.get("md5"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.print("</option>");
						_xhtml_out.print("<option value=\"no\"");
	        	    	if(request.getParameter("md5") != null && "no".equals(request.getParameter("md5"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	} else if((request.getParameter("md5") == null && _fileset.get("md5") == null) || "no".equals(_fileset.get("md5"))) {
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
	                    _xhtml_out.println("<a href=\"javascript:AddVolume();\"><img src=\"/images/add_16.png\"/ title=\"");
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
	                    _xhtml_out.print(getLanguageMessage("device.shares.lv"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("device.shares.external"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	                    for(int i = 0; i < _add_volumes || i < _fileset_volumes.size(); i++) {
	                    	String _volume = null;
	                    	if(i < _fileset_volumes.size()) {
	                    		_volume = _fileset_volumes.get(i);
	                    	}
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println("<select class=\"form_select\" name=\"lv" + i + "\">");
		        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
							for(Map<String, String> _lv : _lvs) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(_lv.get("vg"));
								_xhtml_out.print("/");
								_xhtml_out.print(_lv.get("name"));
								_xhtml_out.print("\"");
								if (request.getParameter("lv" + i) != null && request.getParameter("lv" + i).equals(_lv.get("vg") + "/" + _lv.get("name"))) {
									_xhtml_out.print(" selected=\"selected\"");
								} else if(_volume != null && _volume.equals(_lv.get("vg")+"/"+_lv.get("name"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(_lv.get("vg"));
								_xhtml_out.print("/");
								_xhtml_out.print(_lv.get("name"));
								_xhtml_out.println("</option>");
							}
							_xhtml_out.println("</select>");
	                    	_xhtml_out.println("</td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println("<select class=\"form_select\" name=\"share" + i + "\">");
		        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
							for(Map<String, String> _share : _external_shares) {
		                    	_xhtml_out.print("<option value=\"");
								_xhtml_out.print(_share.get("server"));
								if (!_share.get("server").endsWith("/") && !_share.get("share").startsWith("/"))
		                    		_xhtml_out.print("/");
								_xhtml_out.print(_share.get("share"));
								_xhtml_out.print("\"");
								if(request.getParameter("share" + i) != null && request.getParameter("share" + i).equals(_share.get("server") + _share.get("share"))) {
									_xhtml_out.print(" selected=\"selected\"");
								} else if(_volume != null && _volume.equals(_share.get("server")+_share.get("share"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(_share.get("server"));
								if (!_share.get("server").endsWith("/") && !_share.get("share").startsWith("/"))
		                    		_xhtml_out.print("/");
								_xhtml_out.print(_share.get("share"));
								_xhtml_out.println("</option>");
		                    }
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
		    		}
	    			break;
	    		case FILESET_LOCAL_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_filesets"));
		    			}
		    			boolean md5 = false;
		    			if(request.getParameter("md5") != null && !request.getParameter("md5").isEmpty() && "yes".equals(request.getParameter("md5"))) {
		    				md5 = true;
		    			}
		    			String compression = FileSetManager.COMPRESSION_NONE;
		    			if(request.getParameter("compression") != null && !request.getParameter("compression").isEmpty()) {
		    				if (FileSetManager.SUPPORTED_COMPRESSIONS.contains(request.getParameter("compression")))
		    					compression = request.getParameter("compression");
		    			}
		    			
		    			List<String> _volumes = new ArrayList<String>();
		    			for(int i = 0; request.getParameter("lv" + i) != null; i++) {
		    				if(!request.getParameter("lv" + i).isEmpty()) {
		    					_volumes.add(request.getParameter("lv" + i));
		    				}
		    			}
		    			for(int i = 0; request.getParameter("share" + i) != null; i++) {
		    				if(!request.getParameter("share" + i).isEmpty()) {
		    					_volumes.add(request.getParameter("share" + i));
		    				}
		    			}
		    			if(_volumes.isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.fileset.specify_volume"));
		    			}
		    			
		    			if(request.getParameter("modify") == null || !"yes".equals(request.getParameter("modify"))){
		    				FileSetManager.addLocalFileSet(request.getParameter("name"), _volumes, request.getParameter("include"), removeSpaces(request.getParameter("extension")), md5, compression);
		    			} else {
		    				FileSetManager.updateLocalFileSet(request.getParameter("name"), _volumes, request.getParameter("include"), removeSpaces(request.getParameter("extension")), md5, compression);
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
