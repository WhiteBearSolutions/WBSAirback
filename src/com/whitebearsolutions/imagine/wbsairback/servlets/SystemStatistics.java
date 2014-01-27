package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;

public class SystemStatistics extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int DETAILEDSTATS = 2;
	public final static int RELOAD = 3;
	private int type;
	public final static String baseUrl = "/admin/"+SystemStatistics.class.getSimpleName();

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
	    	this.type = 1;
	    	try {
	    		this.type = Integer.parseInt(request.getParameter("type"));
	    	} catch(NumberFormatException _ex) {}
	    	
	    	switch(type) {
	    		default:{
		    			NetworkManager _nm = new NetworkManager(this.sessionManager.getConfiguration());
		    			
		    			_xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/chart_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("statistics"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.println("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("statistics.message.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			for(String iface : _nm.getConfiguredInterfaces()) {
			    			_xhtml_out.println("<div class=\"window\">");
		                    _xhtml_out.println("<h2>");
		                    _xhtml_out.print(iface);
		             //     _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
		                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.println("\"/></a>"); 

		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.print("<a href=\"/admin/SystemStatistics?type=");
		                    _xhtml_out.print(DETAILEDSTATS);
		                    _xhtml_out.print("&resource=");
		                    _xhtml_out.print(iface);
		                    _xhtml_out.print("\"><img src=\"/graphics/");
		                    _xhtml_out.print(iface);
		                    _xhtml_out.print("_hour.png\"/></a>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
		    			}
		    			_xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.cpu"));
	          //        _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<a href=\"/admin/SystemStatistics?type=");
	                    _xhtml_out.print(DETAILEDSTATS);
	                    _xhtml_out.print("&resource=cpu\"><img src=\"/graphics/cpu_hour.png\"/></a>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.mem"));
	           //       _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<a href=\"/admin/SystemStatistics?type=");
	                    _xhtml_out.print(DETAILEDSTATS);
	                    _xhtml_out.print("&resource=mem\"><img src=\"/graphics/mem_hour.png\"/></a>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.diskspace"));
	            //      _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<a href=\"/admin/SystemStatistics?type=");
	                    _xhtml_out.print(DETAILEDSTATS);
	                    _xhtml_out.print("&resource=diskspace\"><img src=\"/graphics/diskspace_hour.png\"/></a>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.process"));
	             //     _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<a href=\"/admin/SystemStatistics?type=");
	                    _xhtml_out.print(DETAILEDSTATS);
	                    _xhtml_out.print("&resource=process\"><img src=\"/graphics/process_hour.png\"/></a>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.uptime"));
	             //     _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<a href=\"/admin/SystemStatistics?type=");
	                    _xhtml_out.print(DETAILEDSTATS);
	                    _xhtml_out.print("&resource=uptime\"><img src=\"/graphics/uptime_week.png\"/></a>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	    			}
                    break;
	    		case DETAILEDSTATS: {
		    			String resource = request.getParameter("resource");
		    			
		    			_xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/chart_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("statistics"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.println("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("statistics.message.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			if(!resource.equals("uptime")) {
		    				_xhtml_out.println("<div class=\"window\">");
		                    _xhtml_out.println("<h2>");
		                    _xhtml_out.print(getLanguageMessage("statistics.last_6_hours"));
		         //         _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
		                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.println("\"/></a>"); 

		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.print("<img src=\"/graphics/");
		                    _xhtml_out.print(resource);
		                    _xhtml_out.print("_hour.png\"/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
		                    _xhtml_out.println("<div class=\"window\">");
		                    _xhtml_out.println("<h2>");
		                    _xhtml_out.print(getLanguageMessage("statistics.last_24_hours"));
		         //         _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
		                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.println("\"/></a>"); 

		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.print("<img src=\"/graphics/");
		                    _xhtml_out.print(resource);
		                    _xhtml_out.print("_daily.png\"/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
	                    }
		    			_xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.last_week"));
	             //     _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<img src=\"/graphics/");
	                    _xhtml_out.print(resource);
	                    _xhtml_out.print("_week.png\"/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.last_month"));
	              //    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<img src=\"/graphics/");
	                    _xhtml_out.print(resource);
	                    _xhtml_out.print("_month.png\"/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("statistics.last_year"));
	             //     _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>"); 

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<img src=\"/graphics/");
	                    _xhtml_out.print(resource);
	                    _xhtml_out.print("_year.png\"/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
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