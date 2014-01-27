package com.whitebearsolutions.imagine.wbsairback.servlets;
	

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;

public class LibraryOperations extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int DRIVE_MOUNT = 2;
	public final static int DRIVE_UMOUNT = 3;
	public final static int DRIVE_FORMAT = 4;
	private int type;
	public final static String baseUrl = "/admin/"+LibraryOperations.class.getSimpleName();
	
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
		    			List<Map<String, String>> _drives = LibraryManager.getStorageDrives(request.getParameter("storage"));
		    			List<Map<String, String>> _slots = LibraryManager.getStorageSlots(request.getParameter("storage"));
		    			
		    			writeDocumentBack("/admin/BackupStorageDisk");

		    		//	writeDocumentBack("javascript:history.back();");
		    			_xhtml_out.println("<script>");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("  function mountTape(index) {");
		    			_xhtml_out.println("     document.library.drive.value = index;");
		    			_xhtml_out.println("     submitForm(document.library.submit());");
		    			_xhtml_out.println("  }");
		    			_xhtml_out.println("//-->");
		    			_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\"/admin/LibraryOperations\" name=\"library\" method=\"get\">");
					    _xhtml_out.println("<input type=\"hidden\" name=\"storage\" value=\"" + request.getParameter("storage") + "\"/>");
					    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + DRIVE_MOUNT + "\"/>");
					    _xhtml_out.println("<input type=\"hidden\" name=\"drive\" value=\"0\"/>");
		                _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.slots"));
						_xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
						_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    for(_offset = 0; _offset < _slots.size(); _offset++) {
	                    	Map<String, String> _slot = _slots.get(_offset);
	                    	if(_offset % 2 == 0) {
								_xhtml_out.print("<tr");
								if(_offset % 4 == 0) {
									_xhtml_out.print(" class=\"highlight\"");
								}
								_xhtml_out.println(">");
								_xhtml_out.print("<td><input class=\"form_radio\" type=\"radio\" name=\"slot\" value=\"");
								_xhtml_out.print(_slot.get("name"));
								_xhtml_out.print("\"/>slot ");
								_xhtml_out.print(_slot.get("name"));
								_xhtml_out.print(":");
								_xhtml_out.print(_slot.get("value"));
								_xhtml_out.print("&nbsp;");
								if("unassigned".equals(_slot.get("status"))) {
									_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_unassigned"));
								} else {
									_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_assigned"));
								}
								_xhtml_out.println("</td>");
								if((_offset + 1) == _slots.size()) {
									_xhtml_out.println("<td>&nbsp;</td>");
									_xhtml_out.println("</tr>");
								}
							} else {
								_xhtml_out.print("<td><input class=\"form_radio\" type=\"radio\" name=\"slot\" value=\"");
								_xhtml_out.print(_slot.get("name"));
								_xhtml_out.print("\"/>slot ");
								_xhtml_out.print(_slot.get("name"));
								_xhtml_out.print(":");
								_xhtml_out.print(_slot.get("value"));
								_xhtml_out.print("&nbsp;");
								if("unassigned".equals(_slot.get("status"))) {
									_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_unassigned"));
								} else {
									_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_assigned"));
								}
								_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
							}
						}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
		    			
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.drives"));
						_xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
						_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    _offset = 0;
	                    for(Map<String, String> _drive : _drives) {
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
							_xhtml_out.print("<td>");
							_xhtml_out.print("<img src=\"/images/tape_16.png\"/>&nbsp;");
							_xhtml_out.print(_drive.get("name"));
							_xhtml_out.println("</td>");
							if("unassigned".equals(_drive.get("status"))) {
								_xhtml_out.println("<td>");
								_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_unassigned"));
								_xhtml_out.print(" (slot ");
								_xhtml_out.print(_drive.get("slot"));
								_xhtml_out.println(")</td>");
								_xhtml_out.println("<td>");
								if(request.getParameter("remote") == null || !request.getParameter("remote").equals("yes")){
									_xhtml_out.print("<a href=\"/admin/LibraryOperations?type=");
									_xhtml_out.print(DRIVE_FORMAT);
									_xhtml_out.print("&storage=");
									_xhtml_out.print(request.getParameter("storage"));
									_xhtml_out.print("&drive=");
									_xhtml_out.print(_drive.get("index"));
									_xhtml_out.print("\">");
									_xhtml_out.print("<img src=\"/images/tape_cross_16.png\" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.format"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.format"));
			                    	_xhtml_out.println("\"/></a>");
								}
								_xhtml_out.print("<a href=\"/admin/LibraryOperations?type=");
								_xhtml_out.print(DRIVE_UMOUNT);
								_xhtml_out.print("&storage=");
								_xhtml_out.print(request.getParameter("storage"));
								_xhtml_out.print("&drive=");
								_xhtml_out.print(_drive.get("index"));
								_xhtml_out.print("\">");
								_xhtml_out.print("<img src=\"/images/tape_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.umount"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.umount"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.println("</td>");
							} else if("assigned".equals(_drive.get("status"))) {
								_xhtml_out.println("<td>");
								_xhtml_out.print(_drive.get("label"));
								_xhtml_out.print(" (slot ");
								_xhtml_out.print(_drive.get("slot"));
								_xhtml_out.println(")</td>");
								_xhtml_out.println("<td>");
								if(request.getParameter("remote") == null || !request.getParameter("remote").equals("yes")){
									_xhtml_out.print("<a href=\"/admin/LibraryOperations?type=");
									_xhtml_out.print(DRIVE_FORMAT);
									_xhtml_out.print("&storage=");
									_xhtml_out.print(request.getParameter("storage"));
									_xhtml_out.print("&drive=");
									_xhtml_out.print(_drive.get("index"));
									_xhtml_out.print("\">");
									_xhtml_out.print("<img src=\"/images/tape_cross_16.png\" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.format"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.format"));
			                    	_xhtml_out.println("\"/></a>");
								}
								_xhtml_out.print("<a href=\"/admin/LibraryOperations?type=");
								_xhtml_out.print(DRIVE_UMOUNT);
								_xhtml_out.print("&storage=");
								_xhtml_out.print(request.getParameter("storage"));
								_xhtml_out.print("&drive=");
								_xhtml_out.print(_drive.get("index"));
								_xhtml_out.print("\">");
								_xhtml_out.print("<img src=\"/images/tape_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.umount"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.umount"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.println("</td>");
							} else {
								_xhtml_out.print("<td>");
		                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.empty"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.println("<td>");
		                    	_xhtml_out.print("<a href=\"javascript:mountTape(");
								_xhtml_out.print(_drive.get("index"));
								_xhtml_out.print(");\">");
								_xhtml_out.print("<img src=\"/images/tape_accept_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.mount"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.mount"));
		                    	_xhtml_out.println("\"/></a>");
		                    	_xhtml_out.println("</td>");
							}
							_xhtml_out.println("</tr>");
							_offset++;
						}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</form>");
					}
	    			break;
	    		case DRIVE_MOUNT: {
		    			int drive = -1, slot = 0;
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.storage"));
		    			}
		    			if(request.getParameter("slot") == null || request.getParameter("slot").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.slot"));
		    			}
		    			
		    			if(request.getParameter("drive") != null) {
			    			try {
			    				drive = Integer.parseInt(request.getParameter("drive"));
			    			} catch(NumberFormatException _ex) {}
		    			}
		    			try {
		    				slot = Integer.parseInt(request.getParameter("slot"));
		    			} catch(NumberFormatException _ex) {}
		    			
		    			LibraryManager.mountSlotToDrive(request.getParameter("storage"), drive, slot);
		    			response.sendRedirect("/admin/LibraryOperations?storage="+ request.getParameter("storage"));
		    			this.redirected=true;
	    			}
	    			break;
	    		case DRIVE_UMOUNT: {
		    			int drive = -1;
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.storage"));
		    			}
		    			
		    			if(request.getParameter("drive") != null) {
			    			try {
			    				drive = Integer.parseInt(request.getParameter("drive"));
			    			} catch(NumberFormatException _ex) {}
		    			}
		    			
		    			LibraryManager.umountDrive(request.getParameter("storage"), drive);
		    			
		    			response.sendRedirect("/admin/LibraryOperations?storage="+ request.getParameter("storage"));
		    			this.redirected=true;
	    			}
	    			break;
	    		case DRIVE_FORMAT: {
		    			int drive = -1;
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.storage"));
		    			}
		    			
		    			if(request.getParameter("drive") != null) {
			    			try {
			    				drive = Integer.parseInt(request.getParameter("drive"));
			    			} catch(NumberFormatException _ex) {}
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				LibraryManager.formatTape(request.getParameter("storage"), drive);
			    			response.sendRedirect("/admin/LibraryOperations?storage="+ request.getParameter("storage"));
			    			this.redirected=true;
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("backup.storage.question"), "/admin/LibraryOperations?type=" + DRIVE_FORMAT + "&storage=" + request.getParameter("storage") + "&drive=" + drive +  "&confirm=true", null);
		    			}
	    			}
	    			break;
	    	}
 		} catch (Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}
	
	