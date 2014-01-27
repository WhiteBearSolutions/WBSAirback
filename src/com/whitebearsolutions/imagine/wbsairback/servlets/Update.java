package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.service.UpdateManager;

public class Update extends WBSImagineServlet {
	static final long serialVersionUID = 20080110L;
	public final static int UPDATE = 1;
	public final static int UPDATE_CONFIRM = 2;
	private int type;
	public final static String baseUrl = "/admin/"+Update.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	    
	    response.setContentType("text/html");
	    PrintWriter _xhtml_out = response.getWriter();
	    
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	response.setContentType("text/html");
	    	writeDocumentHeader();
	    	
	    	this.type = 0;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			String _test_result = "OK";
			if(this.type == 0 &&
            		request.getParameter("update") != null &&
            		"check".equalsIgnoreCase(request.getParameter("update"))) {
            	_test_result = UpdateManager.checkSignature();
        		if(!_test_result.equals("OK")) {
					throw new Exception(_test_result);
				}
	        }
            
            _xhtml_out.print("<form action=\"/admin/Update\" name=\"update\" method=\"post\">");
            _xhtml_out.println("<h1>");
			_xhtml_out.print("<img src=\"/images/package_32.png\"/>");
	    	_xhtml_out.print(getLanguageMessage("update"));
			_xhtml_out.println("</h1>");
			_xhtml_out.println("<div class=\"info\">");
			_xhtml_out.print(getLanguageMessage("update.message.info"));
			_xhtml_out.println("</div>");
            switch(this.type) {
    			default: {
	    				Map<String, List<String>> _packages = new HashMap<String, List<String>>();
						if(request.getParameter("update") != null &&
	    	            		"check".equalsIgnoreCase(request.getParameter("update"))) {
								_packages = UpdateManager.getUpgradeData();
	    				}
    				
	    				_xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("update.updates"));
	                    if(!_packages.isEmpty()) {
		                    _xhtml_out.print("<a href=\"javascript:sendForm('/admin/Update?type=");
		                    _xhtml_out.print(UPDATE_CONFIRM);
		                    _xhtml_out.print("');\">");
		                    _xhtml_out.print("<img alt=\"");
		                    _xhtml_out.print(getLanguageMessage("update.update"));
		                    _xhtml_out.print("\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("update.update"));
		                    _xhtml_out.print("\" src=\"/images/package_go_16.png\"/></a>");
	                    } else {
	                    	_xhtml_out.print("<a href=\"javascript:sendForm('/admin/Update?update=check');\">");
		                    _xhtml_out.print("<img alt=\"");
		                    _xhtml_out.print(getLanguageMessage("update.find_updates"));
		                    _xhtml_out.print("\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("update.find_updates"));
		                    _xhtml_out.print("\" src=\"/images/find_16.png\"/></a>");
	                    }
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<table>");
	                    if(request.getParameter("update") != null &&
	    	            		"check".equalsIgnoreCase(request.getParameter("update"))) {
		                    if(!_packages.isEmpty()) {
		    					_xhtml_out.println("<tr>");
		    					_xhtml_out.print("<td class=\"title\">");
		    					_xhtml_out.print(getLanguageMessage(""));
		    					_xhtml_out.println("</td>");
		    					_xhtml_out.print("<td class=\"title\">");
		    					_xhtml_out.print(getLanguageMessage("update.actual_version"));
		    					_xhtml_out.println("</td>");
		    					_xhtml_out.print("<td class=\"title\">");
		    					_xhtml_out.print(getLanguageMessage("update.available_version"));
		    					_xhtml_out.println("</td>");
		    					_xhtml_out.println("</tr>");
			    				for(String _application : _packages.keySet()) {
			    					List<String> _versions = _packages.get(_application);
			    					_xhtml_out.println("<tr>");
			    					_xhtml_out.print("<td>");
			    					_xhtml_out.print(_application);
			    					_xhtml_out.println("</td>");
			    					_xhtml_out.print("<td>");
			    					_xhtml_out.print(_versions.get(0));
			    					_xhtml_out.println("</td>");
			    					_xhtml_out.print("<td>");
			    					_xhtml_out.print(_versions.get(1));
			    					_xhtml_out.println("</td>");
			    					_xhtml_out.println("</tr>");
			    				}
		    				} else {
		    					_xhtml_out.println("<tr>");
		    					_xhtml_out.print("<td>");
		    					_xhtml_out.print(getLanguageMessage("update.no_updates"));
		    					_xhtml_out.println("</td>");
		    					_xhtml_out.println("</tr>");
		    				}
	                    } else {
	                    	_xhtml_out.println("<tr>");
	    					_xhtml_out.println("<td>&nbsp;</td>");
	    					_xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
    				}
    	            break;
	    		case UPDATE_CONFIRM: {
						UpdateManager.upgrade();
						writeDocumentResponse(getLanguageMessage("update.message.update_ok"), "/admin/Update");
					}
	    			break;
			}
            _xhtml_out.print("</form>");
	    } catch(Exception _ex) {
	    	writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}