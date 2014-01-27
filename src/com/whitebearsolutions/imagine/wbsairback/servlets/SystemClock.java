package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.configuration.NTPConfiguration;

public class SystemClock extends WBSImagineServlet {
	static final long serialVersionUID = 20090917L;
	public final static int SAVE = 2;
	private int type;
	public final static String baseUrl = "/admin/"+SystemClock.class.getSimpleName();

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
	    	
	    	NTPConfiguration _nc = new NTPConfiguration();
	    	
	    	switch(this.type) {
	    		default: {
		    			Calendar _cal = Calendar.getInstance(TimeZone.getTimeZone(_nc.getTimeZone()));
		    			String[] _months = new String[] { getLanguageMessage("common.message.january"), getLanguageMessage("common.message.february"), getLanguageMessage("common.message.march"), getLanguageMessage("common.message.april"), getLanguageMessage("common.message.may"), getLanguageMessage("common.message.june"), getLanguageMessage("common.message.july"), getLanguageMessage("common.message.august"), getLanguageMessage("common.message.september"), getLanguageMessage("common.message.october"), getLanguageMessage("common.message.november"), getLanguageMessage("common.message.december") };
		    			List<String> _servers = _nc.getServers();
	                    
		    			_xhtml_out.println("<script type=\"text/javascript\">");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("function clock(time) {");
		    			_xhtml_out.println("\tvar deltaTime = new Date(new Date().getTime() - time).getTime();");
		    			_xhtml_out.println("\tsetInterval('updateClock(' + deltaTime + ')', 1000 );");
		    			_xhtml_out.println("}\n");
		    			_xhtml_out.println("function updateClock(deltaTime) {");
		    			_xhtml_out.println("\ttime = new Date(new Date().getTime() - deltaTime);");
		    			_xhtml_out.println("\tvar _months = new Array(\"January\", \"February\", \"March\", \"April\", \"May\", \"June\", \"July\", \"August\", \"September\", \"October\", \"November\", \"December\");");
		    			_xhtml_out.println("\tvar hours = time.getHours();");
		    			_xhtml_out.println("\tvar minutes = time.getMinutes();");
		    			_xhtml_out.println("\tvar seconds = time.getSeconds();");
		    			_xhtml_out.println("\tminutes = ( minutes < 10 ? \"0\" : \"\" ) + minutes;");
		    			_xhtml_out.println("\tseconds = ( seconds < 10 ? \"0\" : \"\" ) + seconds;");
		    			_xhtml_out.println("\tvar timeOfDay = ( hours < 12 ) ? \"AM\" : \"PM\";");
		    			_xhtml_out.println("\thours = ( hours > 12 ) ? hours - 12 : hours;");
		    			_xhtml_out.println("\thours = ( hours == 0 ) ? 12 : hours;");
		    			_xhtml_out.println("\thours = ( hours < 10 ? \"0\" : \"\" ) + hours;");
		    			_xhtml_out.println("\tdocument.getElementById(\"date\").value = time.getDate() + \" / \" + _months[time.getMonth()] + \" / \" + time.getFullYear() +\" \" + hours + \":\" + minutes + \":\" + seconds + \" \" + timeOfDay;");
		    			_xhtml_out.println("\t");
		    			_xhtml_out.println("\t");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("// -->");
		    			_xhtml_out.println("</script>");
		    			
		    			_xhtml_out.print("<form action=\"/admin/SystemClock\" name=\"system\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + SAVE + "\"/>");
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/clock_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.clock"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.clock.info"));
		    			_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println("<a href=\"javascript:submitForm(document.system.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"now\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.clock.now"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" id=\"date\" name=\"date\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"timezone\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.clock.timezone"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<select class=\"form_select\" name=\"timezone\">");
	                    _xhtml_out.print("<option></option>");
	                    for(String tz : NTPConfiguration.getAvailableTimeZones()) {
	                    	_xhtml_out.print("<option value=\"" + tz);
	                    	_xhtml_out.print("\"");
	                    	if(_nc.getTimeZone().equals(tz)) {
	                    		_xhtml_out.print(" selected=\"selected\"");
	                    	}
	                    	_xhtml_out.print(">" + tz + " </option>");
	                    }
	                    _xhtml_out.print("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"date\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.clock.date"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<select class=\"form_select\" name=\"day\">");
	                    for(int j = 1; j < 32; j++) {
	                    	_xhtml_out.print("<option value=\"" + j + "\"");
	                    	if(_cal.get(Calendar.DAY_OF_MONTH) == j) {
	                    		_xhtml_out.print(" selected");
	                    	}
	                    	_xhtml_out.print(">" + j + " </option>");
	                    }
	                    _xhtml_out.print("</select> / ");
	                    _xhtml_out.print("<select class=\"form_select\" name=\"month\">");
	                    for(int j = 0; j < 12; j++) {
	                    	_xhtml_out.print("<option value=\"" + (j + 1) + "\"");
	                    	if(_cal.get(Calendar.MONTH) == j) {
	                    		_xhtml_out.print(" selected");
	                    	}
	                    	_xhtml_out.print(">" + _months[j] + " </option>");
	                    }
	                    _xhtml_out.print("</select> / ");
	                    _xhtml_out.print("<select class=\"form_select\" name=\"year\">");
	                    for(int j = _cal.get(Calendar.YEAR) - 5; j < _cal.get(Calendar.YEAR) + 5; j++) {
	                    	_xhtml_out.print("<option value=\"" + j + "\"");
	                    	if(_cal.get(Calendar.YEAR) == j) {
	                    		_xhtml_out.print(" selected");
	                    	}
	                    	_xhtml_out.print(">" + j + "</option>");
	                    }
	                    _xhtml_out.print("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"date\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.clock.time"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"clock_value\" type=\"text\" name=\"hours\" value=\"");
	        	    	_xhtml_out.print(_cal.get(Calendar.HOUR));
	        	    	_xhtml_out.println("\"/>");
	        	    	_xhtml_out.print("<input class=\"clock_value\" type=\"text\" name=\"minutes\" value=\"");
	        	    	_xhtml_out.print(_cal.get(Calendar.MINUTE));
	        	    	_xhtml_out.println("\"/>");
	        	    	_xhtml_out.print("<input class=\"clock_value\" type=\"text\" name=\"seconds\" value=\"");
	        	    	_xhtml_out.print(_cal.get(Calendar.SECOND));
	        	    	_xhtml_out.println("\"/>");
	                    _xhtml_out.println("<select class=\"form_select\" name=\"timeOfDay\">");
	                    _xhtml_out.print("<option value=\"AM\">AM</option>");
	                    _xhtml_out.print("<option");
	                    if(_cal.get(Calendar.AM_PM) == Calendar.PM) {
	                    	_xhtml_out.print(" selected=\"selected\"");
	                    }
	                    _xhtml_out.print(">PM</option>");
	                    _xhtml_out.print("</select>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.system.submit());\"><img src=\"/images/disk_16.png\" title=\"");	                   
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
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("system.clock.info_NTP"));
		    			_xhtml_out.println("</div>");
	                    _xhtml_out.println("<fieldset>");
	        	    	for(int i = 0; i < 3; i++) {
	        	    		_xhtml_out.println("<div class=\"standard_form\">");
	            	    	_xhtml_out.print("<label for=\"date\">");
	            	    	_xhtml_out.print(getLanguageMessage("system.clock.server"));
	            	    	_xhtml_out.println(": </label>");
	            	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"ntp_server_");
	            	    	_xhtml_out.print(i);
	            	    	_xhtml_out.print("\"");
	            	    	if(i < _servers.size() && _servers.get(i) != null) {
	            	    		_xhtml_out.print(" value=\"");
	            	    		_xhtml_out.print(_servers.get(i));
	            	    		_xhtml_out.print("\"");
	            	    	}
	            	    	_xhtml_out.print("/>");
	    	    			_xhtml_out.println("</div>");
	        	    	}
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	    				_xhtml_out.print("</form>");
	                    
	                    _xhtml_out.println("<script type=\"text/javascript\">");
	                    _xhtml_out.println("<!--");
	                    _xhtml_out.println("\tclock(" + new Date().getTime() + ");");
	                    _xhtml_out.println("// -->");
		    			_xhtml_out.println("</script>");
	    			}
                    break;
	    		case SAVE: {
		    			if(request.getParameter("timezone") != null && !request.getParameter("timezone").isEmpty()) {
		    				_nc.setTimeZone(request.getParameter("timezone"));
		    			}
		    			_nc.setServer(request.getParameter("ntp_server_0"), request.getParameter("ntp_server_1"), request.getParameter("ntp_server_2"));	    			
		    			_nc.setDate(Integer.parseInt(request.getParameter("year")), Integer.parseInt(request.getParameter("month")), Integer.parseInt(request.getParameter("day")), Integer.parseInt(request.getParameter("hours")), Integer.parseInt(request.getParameter("minutes")), request.getParameter("timeOfDay"));
		    			
		    			writeDocumentResponse(getLanguageMessage("common.message.update_ok"), "/admin/SystemClock");
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