package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;

public class SystemProxy extends WBSImagineServlet {
	static final long serialVersionUID = 20080902L;
	public final static int SAVE = 2;
	private int type;
	public final static String baseUrl = "/admin/"+SystemProxy.class.getSimpleName();
	
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
	    	
			switch(this.type) {
	    		default: {
	    				Map<String, String> _proxy = NetworkManager.getProxyServer();
	    			
		    			_xhtml_out.println("<form action=\"/admin/SystemProxy\" id=\"proxy\" name=\"proxy\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SAVE + "\"/>");
	                    _xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/server_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.network.proxy"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.network.proxy.info"));
	                    _xhtml_out.print("</div>");
	                    
	                    _xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.network.proxy"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.proxy.submit());\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"server\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.network.proxy.server"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"server\"");
	        	    	if(_proxy != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_proxy.get("server"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"port\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.network.proxy.port"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"port\"");
	        	    	if(_proxy != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_proxy.get("port"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.network.proxy.user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\"");
	        	    	if(_proxy != null && _proxy.get("user") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_proxy.get("user"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.network.proxy.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"");
	        	    	if(_proxy != null && _proxy.get("password") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_proxy.get("password"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>"); 
	                    _xhtml_out.println("</form>");
	    			}
        		    break;
	    		case SAVE: {
		    			if(request.getParameter("server") != null && !request.getParameter("server").isEmpty() && !request.getParameter("server").trim().equals("")) {
		    				int _port = 80;
		    				if(request.getParameter("port") != null && !request.getParameter("port").isEmpty() && !request.getParameter("port").trim().equals("")) {
		    					try {
			    					_port = Integer.parseInt(request.getParameter("port"));
			    				} catch(NumberFormatException _ex) {}
		    				}
		    				NetworkManager.setProxyServer(request.getParameter("server"), _port, request.getParameter("user"), request.getParameter("password"));
		    				writeDocumentResponse(getLanguageMessage("system.network.proxy.message.saved"), "/admin/SystemProxy");
		    			} else {
		    				NetworkManager.removeProxyServer();
		    				writeDocumentResponse(getLanguageMessage("system.network.proxy.message.removed"), "/admin/SystemProxy");
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
	
	