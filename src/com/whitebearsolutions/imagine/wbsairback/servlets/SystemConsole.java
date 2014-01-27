package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.UserManager;
import com.whitebearsolutions.util.Command;

public class SystemConsole extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	static final int SAVE_PASSWORD = 2;
	private int type;
	public final static String baseUrl = "/admin/"+SystemConsole.class.getSimpleName();

	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
		response.setContentType("text/html");
		PrintWriter _xhtml_out = response.getWriter();
	    
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	writeDocumentHeader();
	    	
	    	this.type = 1;
	    	try {
	    		this.type = Integer.parseInt(request.getParameter("type"));
	    	} catch(NumberFormatException _ex) {}
	    	
	    	
	    	switch(this.type) {
    			default: {
    					String _address = NetworkManager.getLocalAddress();
    					
    					_xhtml_out.println("<form name=\"console\" method=\"post\" action=\"/admin/SystemConsole\">");
		                _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SAVE_PASSWORD + "\"/>");
		                _xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/application_terminal_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.console"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.println("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.console.info"));
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.console.password"));
	       //           _xhtml_out.print("<a href=\"javascript:submitForm(document.console.submit());\"><img src=\"/images/disk_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.console.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");

	       //           _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.println(getLanguageMessage("system.console"));
	       //           _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<applet codebase=\"/applet\" codetype=\"application/java-archive\" archive=\"/applet/mindterm241.weaversigned.jar\" code=\"com.mindbright.application.MindTerm.class\""); 
		                _xhtml_out.println(" width=\"590\" height=\"360\">");
		                _xhtml_out.println("<param name=\"protocol\" value=\"ssh2\"/>");
		                _xhtml_out.print("<param name=\"server\" value=\"");
		                _xhtml_out.print(_address);
		                _xhtml_out.println("\"/>");
		                _xhtml_out.println("<param name=\"port\" value=\"22\"/>");
		                _xhtml_out.println("<param name=\"sepframe\" value=\"false\"/>");
		                _xhtml_out.println("<param name=\"debug\" value=\"true\"/>");
		                _xhtml_out.println("<param name=\"username\" value=\"wbsairback\"/>");
		                _xhtml_out.println("<param name=\"alive\" value=\"60\"/>");
		                _xhtml_out.println("<param name=\"menus\" value=\"no\"/>");
		                _xhtml_out.println("<param name=\"savepasswords\" value=\"false\"/>");
		                _xhtml_out.println("<param name=\"quiet\" value=\"true\"/>");
		                _xhtml_out.println("<param name=\"allow-new-server\" value=\"false\"/>");
		                _xhtml_out.println("<param name=\"geometry\" value=\"132x35\"/>");
		                _xhtml_out.println("<param name=\"exit-on-logout\" value=\"true\"/>");
		                _xhtml_out.println("</applet>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
    				}
	                break;
				case SAVE_PASSWORD: {
						if(request.getParameter("password") == null || request.getParameter("password").isEmpty()) {
							throw new Exception(getLanguageMessage("system.console.exception"));
						}
						
						StringBuilder _sb = new StringBuilder();
						_sb.append("# WBSAirback autoconfiguration\n");
						_sb.append("Port 22\n");
						_sb.append("Protocol 2\n");
						_sb.append("HostKey /etc/ssh/ssh_host_rsa_key\n");
						_sb.append("HostKey /etc/ssh/ssh_host_dsa_key\n");
						_sb.append("UsePrivilegeSeparation yes\n");
						_sb.append("AllowUsers wbsairback root\n");
						_sb.append("KeyRegenerationInterval 3600\n");
						_sb.append("ServerKeyBits 768\n");
						_sb.append("SyslogFacility AUTH\n");
						_sb.append("LogLevel INFO\n");
						_sb.append("LoginGraceTime 120\n");
						_sb.append("PermitRootLogin yes\n");
						_sb.append("StrictModes yes\n");
						_sb.append("RSAAuthentication yes\n");
						_sb.append("PubkeyAuthentication yes\n");
						_sb.append("IgnoreRhosts yes\n");
						_sb.append("RhostsRSAAuthentication no\n");
						_sb.append("HostbasedAuthentication no\n");
						_sb.append("PermitEmptyPasswords no\n");
						_sb.append("ChallengeResponseAuthentication no\n");
						_sb.append("PasswordAuthentication yes\n");
						_sb.append("X11Forwarding yes\n");
						_sb.append("X11DisplayOffset 10\n");
						_sb.append("PrintMotd no\n");
						_sb.append("PrintLastLog yes\n");
						_sb.append("TCPKeepAlive yes\n");
						_sb.append("AcceptEnv LANG LC_*\n");
						_sb.append("Subsystem sftp /usr/lib/openssh/sftp-server\n");
						_sb.append("UsePAM yes\n");
						
						try {
							FileOutputStream _fos = new FileOutputStream("/etc/ssh/sshd_config");
							_fos.write(_sb.toString().getBytes());
							_fos.close();
							
							Command.systemCommand("/etc/init.d/ssh restart");
						} catch(IOException _ex) {}
						
						UserManager.changeSystemUserPassword("wbsairback", request.getParameter("password"));
						
						writeDocumentResponse(getLanguageMessage("system.console.update_ok"), "/admin/SystemConsole");
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