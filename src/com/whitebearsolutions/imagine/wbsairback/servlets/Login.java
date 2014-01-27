package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;

public class Login extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static String baseUrl = "/admin/"+Login.class.getSimpleName();

	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter _xhtml_out = response.getWriter();
	    
	    try {
	    	String message = null;
	    	if(request.getParameter("type") != null && request.getParameter("type").equals("logout")) {
	    		this.securityManager.setLogout();
	    	}
	    	
	    	this.securityManager.validateAddress(request.getRemoteAddr().toString());
			
			if(request.getParameter("user") != null && !request.getParameter("user").isEmpty() &&
					request.getParameter("password") != null && !request.getParameter("password").isEmpty()) {
				try {
					if(!this.securityManager.checkAddress(request.getRemoteAddr())) {
						throw new Exception(getLanguageMessage("login.exception"));
					}
					this.securityManager.userLogin(request.getParameter("user"), request.getParameter("password"));
					request.getSession(true);
					request.getUserPrincipal();
					if(HAConfiguration.isActiveNode()) {
						if(this.securityManager.isAdministrator()) {
							response.sendRedirect("/admin/SystemConfiguration");
							this.redirected=true;
						} else {
							List<String> roles = this.securityManager.getUserRoles();
							if (roles != null & !roles.isEmpty()) {
								response.sendRedirect(permissions.getDefaultUrl(roles));
							}
							response.sendRedirect("/admin/SystemConfiguration");
							this.redirected=true;
						}
			    	} else {
						response.sendRedirect("/admin/HA");
						this.redirected=true;
			    	}
				} catch(Exception _ex) {
					message = _ex.getMessage();
				}
			}
			
			writeDocumentHeader();
			_xhtml_out.println("<form name=\"login\" action=\"/admin/Login\" method=\"post\">");
			_xhtml_out.println("<input type=\"hidden\" name=\"type\"/>");
	    	_xhtml_out.println("<div class=\"login_logo\">");
	    	_xhtml_out.println("<img src=\"/images/product_logo.png\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"login_fields\">");
	    	_xhtml_out.println("<fieldset class=\"login_fields\">");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"user\">");
	    	_xhtml_out.print(getLanguageMessage("common.login.user"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"user\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"password\">");
	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<input class=\"form_password\" type=\"password\" name=\"password\"/>");
	    	_xhtml_out.println("</div>");
	    	if(message != null) {
	    		_xhtml_out.println("<div class=\"clear\"/></div>");
	    		_xhtml_out.println("<div class=\"error\">");
		    	_xhtml_out.print(message);
	    		_xhtml_out.println("</div>");
	    	} else if(request.getParameter("password") != null) {
	    		_xhtml_out.println("<div class=\"clear\"/></div>");
	    		_xhtml_out.println("<div class=\"error\">");
		    	_xhtml_out.println(getLanguageMessage("login.message.no_password"));
		    	_xhtml_out.println("</div>");
	    	}
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<input class=\"form_submit\" type=\"submit\" value=\"");
	    	_xhtml_out.print(getLanguageMessage("common.login.submit"));
	    	_xhtml_out.println("\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</fieldset>");
	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</form>");
	    } catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}