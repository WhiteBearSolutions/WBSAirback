package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.configuration.DefaultConfiguration;

public class Wizard extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int SAVE = 2;
	private int type;
	public final static String baseUrl = "/admin/"+Wizard.class.getSimpleName();

	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter _xhtml_out = response.getWriter();
	    try {		
			if(this.sessionManager.isConfigured() && !this.securityManager.isLogged()) {
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
	    	
			if(this.sessionManager.isConfigured() && !this.securityManager.isAdministrator()) {
	    		throw new Exception(getLanguageMessage("common.message.no_privilegios"));
	    	}
	    	
	    	switch(this.type) {
	    		default: {
		    			_xhtml_out.println("<form action=\"/admin/Wizard\" name=\"wizard\" method=\"post\">");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SAVE + "\"/>");
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/wizard_32.png\"/>");
	    		    	_xhtml_out.print(getLanguageMessage("wizard"));
	    				_xhtml_out.println("</h1>");
	    				_xhtml_out.println("<div class=\"info\">");
	    				if(!this.sessionManager.isConfigured()) {
	                    	_xhtml_out.print(getLanguageMessage("wizard.install_ok"));
	                    	_xhtml_out.print(" " + "<strong><span style=\"color: #c3c3c3\">WBS</span><span style=\"color: #00386e\">Airback</span></strong>. ");

	    				} else {
	                    	_xhtml_out.print("<strong>");
		        	    	_xhtml_out.print(getLanguageMessage("common.message.warning"));
		        	    	_xhtml_out.print("</strong>");
		        	    	_xhtml_out.print(getLanguageMessage("wizard.warning_prev")); 
		        	    	_xhtml_out.print("<strong><span style=\"color: #c3c3c3\">WBS</span><span style=\"color: #00386e\">Airback</span></strong>. ");
		        	    	_xhtml_out.print(getLanguageMessage("wizard.warning_post"));
	                    }
	                    _xhtml_out.print(getLanguageMessage("wizard.message.define_parameters"));
	    				_xhtml_out.println("</div>");
	    				_xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("wizard"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.wizard.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	                    _xhtml_out.print("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"passwordConfirmation\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.passwordConfirmation"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"passwordConfirmation\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	                    _xhtml_out.print("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"email\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.mail"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"email\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	                    _xhtml_out.print("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	                    _xhtml_out.print("</form>");
	    			}
	    			break;
	    		case SAVE: {
		    			if(request.getParameter("email") == null || request.getParameter("email").equals("")) {
		    				throw new Exception(getLanguageMessage("wizard.exception.e-mail"));
			    		}
		    			if(request.getParameter("password") != null && !request.getParameter("password").isEmpty()) {
		    				if(request.getParameter("passwordConfirmation") != null && !request.getParameter("password").equals(request.getParameter("passwordConfirmation"))) {
		    					throw new Exception(getLanguageMessage("wizard.exception.password"));
		    				}
		    			} else {
		    				throw new Exception(getLanguageMessage("wizard.exception.admin_password"));
		    			}
		    			
		    			DefaultConfiguration.init(request.getParameter("password"), request.getParameter("email"));
		    			this.sessionManager.reloadConfiguration();
		    			this.securityManager.setLogout();
						writeDocumentResponse(getLanguageMessage("wizard.configured_ok"), "/admin/Login");
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
