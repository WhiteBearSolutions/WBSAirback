package com.whitebearsolutions.imagine.wbsairback.servlets;
	

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.util.Configuration;

public class BackupStorageRemote extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int REMOTE_ADD = 2;
	public final static int REMOTE_SAVE = 3;
	public final static int REMOTE_EDIT = 4;
	public final static int REMOTE_DELETE = 5;
	private int type;
	public final static String baseUrl = "/admin/"+BackupStorageRemote.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		 PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/Login");
	    		this.redirected=true;
	    	}
	    	
	    	this.type = 1;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			response.setContentType("text/html");
		    
		    Configuration _c = this.sessionManager.getConfiguration();
		    StorageManager _rm = new StorageManager(_c);
		    
		    switch(this.type) {
	    		default: {
	    				response.sendRedirect("/admin/BackupStorageDisk");
	    				this.redirected=true;
	    			}
        		    break;
	    		case REMOTE_ADD: {
	    				writeDocumentHeader();
		    			
	    				writeDocumentBack("/admin/BackupStorageDisk");
		    			_xhtml_out.println("<form action=\"/admin/BackupStorageRemote\" name=\"storage\" method=\"post\">");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + REMOTE_SAVE + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"no\"/>");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
		    			_xhtml_out.println("</div>");
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.remote.new_storage"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.storage.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	                	_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                	_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"devicetype\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"devicetype\">");
						_xhtml_out.print("<option value=\"File\">");
						_xhtml_out.print(getLanguageMessage("backup.storage.disk"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("<option value=\"Tape\">");
						_xhtml_out.print(getLanguageMessage("backup.storage.tape"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("<option value=\"Autochanger\">");
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"devicename\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.device_name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"devicename\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ip1\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.address"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip1\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip2\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip3\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"ip4\"/>");
	                    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.device.remote.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mediatype\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"mediatype\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	_xhtml_out.println("<option value=\"LTO1\">LTO1</option>");
						_xhtml_out.println("<option value=\"LTO2\">LTO2</option>");
						_xhtml_out.println("<option value=\"LTO3\">LTO3</option>");
						_xhtml_out.println("<option value=\"LTO4\">LTO4</option>");
						_xhtml_out.println("<option value=\"DLT\">DLT</option>");
						_xhtml_out.println("<option value=\"SDLT\">SDLT</option>");
						_xhtml_out.println("<option value=\"DDS1\">DDS1</option>");
						_xhtml_out.println("<option value=\"DDS2\">DDS2</option>");
						_xhtml_out.println("<option value=\"DDS3\">DDS3</option>");
						_xhtml_out.println("<option value=\"DDS4\">DDS4</option>");
						_xhtml_out.println("<option value=\"DAT72\">DAT72</option>");
						_xhtml_out.println("<option value=\"DAT160\">DAT160</option>");
						_xhtml_out.println("<option value=\"TR\">TR</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    _xhtml_out.println("</form>");
		    		}
	    			break;
	    		case REMOTE_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.name"));
		    			}
		    			String[] address = new String[] { request.getParameter("ip1"), request.getParameter("ip2"), request.getParameter("ip3"), request.getParameter("ip4") };
		    			if(request.getParameter("modify")==null || request.getParameter("modify").equals("no")){
		    				StorageManager.addRemoteDevice(request.getParameter("name"), request.getParameter("devicetype"), request.getParameter("devicename"), address, request.getParameter("password"), request.getParameter("mediatype"));
		    			} else {
		    				StorageManager.updateRemoteDevice(request.getParameter("name"), request.getParameter("devicetype"), request.getParameter("devicename"), address, request.getParameter("password"), request.getParameter("mediatype"));
		    			}
			    		
		    			response.sendRedirect("/admin/BackupStorageRemote");
		    			this.redirected=true;
	    			}
	    			break;
	    		case REMOTE_EDIT: {
	    				writeDocumentHeader();

		    			
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.repository"));
		    			}
		    			
		    			Map<String, String> storage = StorageManager.getStorageParameters(request.getParameter("storage"));
		    			String[] _address = NetworkManager.toAddress(storage.get("storage.address"));
	
		    			writeDocumentBack("/admin/BackupStorageRemote");
		    			_xhtml_out.println("<form action=\"/admin/BackupStorageRemote\" name=\"storage\" method=\"post\">");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + REMOTE_SAVE + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("storage") + "\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.remote.edit_storage")+request.getParameter("storage"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.storage.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	                	_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                	_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"name\" value=\"");
	        	    	_xhtml_out.print(request.getParameter("storage"));
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"devicetype\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"devicetype\">");
						_xhtml_out.print("<option value=\"File\"");
						if(storage.containsKey("storage.devicetype") && "File".equals(storage.get("storage.devicetype"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("backup.storage.disk"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("<option value=\"Tape\"");
						if(storage.containsKey("storage.devicetype") && "Tape".equals(storage.get("storage.devicetype"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("backup.storage.tape"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("<option value=\"Autochanger\"");
						if(storage.containsKey("storage.devicetype") && "Autochanger".equals(storage.get("storage.devicetype"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"devicename\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.device_name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"devicename\" value=\"");
	        	    	_xhtml_out.print(storage.get("storage.device"));
	        	    	_xhtml_out.println("\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ip1\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.address"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip1\" value=\"");
	        	    	_xhtml_out.print(_address[0]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip2\" value=\"");
	        	    	_xhtml_out.print(_address[1]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip3\" value=\"");
	        	    	_xhtml_out.print(_address[2]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"ip4\" value=\"");
	        	    	_xhtml_out.print(_address[3]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\" value=\"");
	        	    	_xhtml_out.print(storage.get("storage.password"));
	        	    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mediatype\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"mediatype\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	_xhtml_out.print("<option value=\"LTO1\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("LTO1")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO1</option>");
						_xhtml_out.print("<option value=\"LTO2\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("LTO2")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO2</option>");
						_xhtml_out.print("<option value=\"LTO3\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("LTO3")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO3</option>");
						_xhtml_out.println("<option value=\"LTO4\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("LTO4")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO4</option>");
						_xhtml_out.println("<option value=\"DLT\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("DLT")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DLT</option>");
						_xhtml_out.println("<option value=\"SDLT\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("SDLT")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">SDLT</option>");
						_xhtml_out.println("<option value=\"DDS1\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("DDS1")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS1</option>");
						_xhtml_out.println("<option value=\"DDS2\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("DDS2")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS2</option>");
						_xhtml_out.println("<option value=\"DDS3\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("DDS3")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS3</option>");
						_xhtml_out.println("<option value=\"DDS4\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("DDS4")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS4</option>");
						_xhtml_out.println("<option value=\"DAT72\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("DAT72")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DAT72</option>");
						_xhtml_out.println("<option value=\"DAT160\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("DAT160")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DAT160</option>");
						_xhtml_out.println("<option value=\"TR\"");
						if(storage.containsKey("storage.mediatype") && storage.get("storage.mediatype").equals("TR")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">TR</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    _xhtml_out.println("</form>");
		    		}
	    			break;
	    		case REMOTE_DELETE: {
	    			if(request.getParameter("confirm") != null) {
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.device"));
		    			}
		    			_rm.removeRemoteDevice(request.getParameter("storage"));
		    			response.sendRedirect("/admin/BackupStorageRemote");
		    			this.redirected=true;
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("backup.message.storage.remove.question"), "/admin/BackupStorageRemote?type=" + REMOTE_DELETE + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
	    			}
	    		}
	    			break;
	    	}
 		} catch(Exception _ex) {
 			switch (type) {
	 			case REMOTE_DELETE:
	 					writeDocumentError(_ex.getMessage(), "/admin/BackupStorageDisk");
	 				break;
	 			default:
	 					writeDocumentError(_ex.getMessage());
	 				break;
 			}
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}