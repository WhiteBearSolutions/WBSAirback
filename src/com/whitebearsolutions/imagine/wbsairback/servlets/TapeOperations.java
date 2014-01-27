package com.whitebearsolutions.imagine.wbsairback.servlets;
	

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;
import com.whitebearsolutions.util.Command;

public class TapeOperations extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int MOUNTTAPE = 2;
	public final static int UMOUNTTAPE = 3;
	public final static int FORMATTAPE = 4;
	private int type;
	public final static String baseUrl = "/admin/"+TapeOperations.class.getSimpleName();
	
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
	    		default:
	    			boolean noMounted=false;
	    			writeDocumentHeader();
	    			writeDocumentBack("/admin/BackupStorageDisk");
	    			_xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/tag_32.png\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
    				_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
	    			_xhtml_out.println("</div>");
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("backup.storage.tape_Pperations_in") + (String) request.getParameter("storage") );
                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		            _xhtml_out.print("\" alt=\"");
		            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		            _xhtml_out.println("\"/></a>");
		            _xhtml_out.println("</h2>");
	    		    _xhtml_out.println("<table style=\"width: 90%;\">");
	                _xhtml_out.println("<tr>");
	                _xhtml_out.println("  <td colspan=\"2\">");
	                _xhtml_out.println("    <form action=\"/BackupPools\" name=\"poolAdd\" method=\"get\">");
			  // 	_xhtml_out.println("    <input type=\"hidden\" name=\"type\" value=\"" + POOLVOLADDTAPE + "\"/>");
				    _xhtml_out.println("    <input type=\"hidden\" name=\"storage\" value=" + (String) request.getParameter("storage") + ">");
			  // 	_xhtml_out.println("    <input type=\"hidden\" name=\"pool\" value=" + (String) request.getParameter("poolName") + ">");
	                _xhtml_out.println("      <table width=\"100%\">");
			    	_xhtml_out.println("        <tr><td colspan=\"2\">&nbsp;</td></tr>");
	                _xhtml_out.println("        <tr>");
	                _xhtml_out.println("          <td colspan=\"2\">");
	                _xhtml_out.println("            <table class=\"fields\">");
	                _xhtml_out.println("      		  <tr class=\"title\"><td class=\"title_center\" colspan=\"2\"></td></tr>");
					_xhtml_out.println("              <tr><td colspan=\"2\">&nbsp;</td></tr>");
					String _output = Command.systemCommand("/bin/echo status storage=" + request.getParameter("storage") + " | /usr/bin/bconsole | grep \"" + (String) request.getParameter("storage") + "\" | grep Device");
					_xhtml_out.println("              <tr>"); 
					_xhtml_out.println("                <td align=\"right\"><b>"+getLanguageMessage("backup.storage.stateOfDevice")+"&nbsp;</b></td>");
					
					if(_output.contains("not open")){
						_xhtml_out.println("                <td align=\"left\">"+getLanguageMessage("backup.storage.noTapeOrunmounted")+"</td>");
					}
					else if(_output.contains("open but no Bacula volume is currently mounted")) {
						_xhtml_out.println("                <td align=\"left\">"+getLanguageMessage("backup.storage.insertedButNotAsigned")+"</td>");
						_xhtml_out.println("           </tr>");
						noMounted=true;
					} else if(_output.contains("mounted with")) {
						String _label = Command.systemCommand("echo status storage=" + request.getParameter("storage") + " | bconsole | grep -A1 'Device.*"+(String) request.getParameter("storage")+"' \"$@\" | grep Volume | tr -s \" \" | cut -d \" \" -f 3");
						_xhtml_out.println("                <td align=\"left\">"+getLanguageMessage("backup.storage.tapeWithLabel")+" "+_label+getLanguageMessage("backup.storage.insertedInUnit")+"</td>");
						_xhtml_out.println("           </tr>");
						//_xhtml_out.println("           <tr><td colspan=\"2\">&nbsp;</td></tr>");
						//_xhtml_out.println("           <tr>");
						//_xhtml_out.println("                <td width=\"50%\" align=\"right\"><b>Reetiquetar cinta:&nbsp;</b></td>");
						//_xhtml_out.println("                <td align=\"left\"><input type=\"text\" size=\"25\" name=\"label\" value=\"\"/></td>");
					} else {
						_xhtml_out.println("                <td align=\"left\">"+getLanguageMessage("backup.storage.unkownState")+".</td>");
						noMounted=true;
					}
					_xhtml_out.println("              </tr>");
					_xhtml_out.println("              <tr><td colspan=\"2\">&nbsp;</td></tr>");
					_xhtml_out.println("            </table>");
					_xhtml_out.println("          </td>");
					_xhtml_out.println("        </tr>");
					
					_xhtml_out.println("        <tr><td colspan=\"2\">&nbsp;</td></tr>");
					_xhtml_out.println("        <tr>");
					_xhtml_out.println("          <td colspan=\"2\" align=\"center\">");
	                _xhtml_out.println("            <table class=\"fields\">");
	                _xhtml_out.println("            <tbody>");
	                _xhtml_out.println("            <tr>");
	                _xhtml_out.println("            <td>");	               
	                	if (!noMounted){
	                		_xhtml_out.println("            <input type=\"button\" value=\"Montar\" onclick=\"javascript:sendForm('/admin/TapeOperations?type=" + MOUNTTAPE + "&storage=" + request.getParameter("storage") + "')\">");
	                	}
						_xhtml_out.println("            <input type=\"button\" value=\"Desmontar\" onclick=\"javascript:sendForm('/admin/TapeOperations?type=" + UMOUNTTAPE + "&storage=" + request.getParameter("storage") + "')\">");
						if((request.getParameter("remote")==null || !request.getParameter("remote").equals("yes"))){
							_xhtml_out.println("            <input type=\"button\" value=\"Formatear\" onclick=\"javascript:sendForm('/admin/TapeOperations?type=" + FORMATTAPE + "&storage=" + request.getParameter("storage") + "')\">");
						}
					_xhtml_out.println("            </td>");
					_xhtml_out.println("            </tr>");
					_xhtml_out.println("            </tbody>");
					_xhtml_out.println("            </table>");
					_xhtml_out.println("          </td>");
					_xhtml_out.println("        </tr>");
					_xhtml_out.println("        <tr><td colspan=\"2\">&nbsp;</td></tr>");
					_xhtml_out.println("      </table>");
	                _xhtml_out.println("    </form>");
					_xhtml_out.println("  </td>");
					_xhtml_out.println("</tr>");
	                _xhtml_out.println("</table>");
	                _xhtml_out.println("<div class=\"clear\"/></div>"); 
	    			break;
	    		case MOUNTTAPE: {
	    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
	    				throw new Exception(getLanguageMessage("backup.storage.tape_idNecessary"));
	    			}
    			
	    			if(request.getParameter("confirm") != null) {
		    			TapeManager.mountTape(request.getParameter("storage"));
		    		   	response.sendRedirect("/admin/TapeOperations?storage="+ request.getParameter("storage"));
		    		   	this.redirected=true;
	    			} else {
	    				writeDocumentHeader();
	    				writeDocumentQuestion(getLanguageMessage("backup.storage.tape_mountConfirm"), "/admin/TapeOperations?type=" + MOUNTTAPE + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
	    			}
		    			
	    			}
	    			break;
	    		case UMOUNTTAPE: {
	    			
	    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
	    				throw new Exception(getLanguageMessage("backup.storage.tape_idNecessary"));
	    			}
    			
	    			if(request.getParameter("confirm") != null) {
	    				TapeManager.umountTape(request.getParameter("storage"));
		    		   	response.sendRedirect("/admin/TapeOperations?storage="+ request.getParameter("storage"));
		    		   	this.redirected=true;
	    			} else {
	    				writeDocumentHeader();
	    				writeDocumentQuestion(getLanguageMessage("backup.storage.tape_unmountConfirm"), "/admin/TapeOperations?type=" + UMOUNTTAPE + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
	    				}
		    			
	    			}
	    			break;
	    		case FORMATTAPE: {	    
	    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
	    				throw new Exception(getLanguageMessage("backup.storage.tape_idNecessary"));
	    			}
	    			Map<String, String> _device=StorageManager.getTapeDevice(request.getParameter("storage"));
	    			if(request.getParameter("confirm") != null) {
	    				TapeManager.formatTape(request.getParameter("storage"), _device.get("device"));
		    			response.sendRedirect("/admin/TapeOperations?storage="+ request.getParameter("storage"));
		    			this.redirected=true;
	    			} else {
	    				writeDocumentHeader();
	    				writeDocumentQuestion(getLanguageMessage("backup.storage.tape_formatConfirm"), "/admin/TapeOperations?type=" + FORMATTAPE + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
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
	
	