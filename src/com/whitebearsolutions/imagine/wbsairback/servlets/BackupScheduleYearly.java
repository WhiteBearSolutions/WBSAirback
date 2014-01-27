package com.whitebearsolutions.imagine.wbsairback.servlets;
	

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ScheduleManager;
import com.whitebearsolutions.imagine.wbsairback.util.StringFormat;

public class BackupScheduleYearly extends WBSImagineServlet {
	static final long serialVersionUID = 242411242409L;
	public final static int SCHEDULE_ADD = 2;
	public final static int SCHEDULE_SAVE = 3;
	public final static int SCHEDULE_EDIT = 4;
	public final static int SCHEDULE_REMOVE = 5;
	private int type;
	public final static String baseUrl = "/admin/"+BackupScheduleYearly.class.getSimpleName();
	
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
	    			}
        		    break;
	    		case SCHEDULE_ADD: {
	    				writeDocumentHeader();
		    			
		    			int _days = 2, _offset = 0;
		    			if(request.getParameter("days") != null) {
	                    	try {
	                    		_days = Integer.parseInt(request.getParameter("days"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
		    			
		    			PoolManager _pm = new PoolManager(this.sessionManager.getConfiguration());
		    			List<String> _pools = _pm.getPoolNames();
		    			
		    			_xhtml_out.println("<script>");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("function AddDay() {");
		    			_xhtml_out.print("  document.schedule.type.value = ");
		    			_xhtml_out.print(SCHEDULE_ADD);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.schedule.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("//-->");
		    			_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\"/admin/BackupScheduleYearly\" name=\"schedule\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SCHEDULE_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"days\" value=\"" + (_days + 1) + "\"/>");
	    			    writeDocumentBack("/admin/BackupScheduleWeekly");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/calendar_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.schedule"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.new_schedule"));
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.schedule.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"name\"");
	    					if(request.getParameter("name") != null) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(request.getParameter("name"));
	    						_xhtml_out.print("\"");
	    					}
	    					_xhtml_out.print("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("common.message.days"));
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.schedule.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");

	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

	            		_xhtml_out.print("<a href=\"javascript:AddDay();\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.level")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.pool")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.month")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.day")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.time")+"</td>");
	                    for(int i = 1; i < _days; i++) {
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/calendar_view_day_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"level");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	    					_xhtml_out.print("<option value=\"Incremental\"");
	    					if(request.getParameter("level" + i) != null && "Incremental".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_incremental"));
		        	    	_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"Differential\"");
	    					if(request.getParameter("level" + i) != null && "Differential".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_differential"));
		        	    	_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"Full\"");
	    					if(request.getParameter("level" + i) != null && "Full".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_full"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.print("<option value=\"VirtualFull\"");
	    					if(request.getParameter("level" + i) != null && "VirtualFull".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_virtual"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"pool");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	    					for(String _pool: _pools) {
	    						_xhtml_out.print("<option value=\"");
	    						_xhtml_out.print(_pool);
	    						_xhtml_out.print("\"");
		    					if(request.getParameter("pool" + i) != null && _pool.equals(request.getParameter("pool" + i))) {
		    						_xhtml_out.print(" selected=\"selected\"");
		    					}
		    					_xhtml_out.print(">");
	    						_xhtml_out.print(_pool);
	    						_xhtml_out.println("</option>");
	    					}
	    					_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"month");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					_xhtml_out.print("<option value=\"0\"");
	    					if(request.getParameter("month" + i) != null && "0".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.january"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"1\"");
	    					if(request.getParameter("month" + i) != null && "1".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.february"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"2\"");
	    					if(request.getParameter("month" + i) != null && "2".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.march"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"3\"");
	    					if(request.getParameter("month" + i) != null && "3".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.april"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"4\"");
	    					if(request.getParameter("month" + i) != null && "4".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.may"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"5\"");
	    					if(request.getParameter("month" + i) != null && "5".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.june"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"6\"");
	    					if(request.getParameter("month" + i) != null && "6".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.july"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"7\"");
	    					if(request.getParameter("month" + i) != null && "7".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.august"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"8\"");
	    					if(request.getParameter("month" + i) != null && "8".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.september"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"9\"");
	    					if(request.getParameter("month" + i) != null && "9".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.october"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"10\"");
	    					if(request.getParameter("month" + i) != null && "10".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.november"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"11\"");
	    					if(request.getParameter("month" + i) != null && "11".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.december"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"day");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					for(int j = 1; j <= 31; j++) {
	    						_xhtml_out.print("<option value=\"");
	    						_xhtml_out.print(j);
	    						_xhtml_out.print("\"");
		    					if(request.getParameter("day" + i) != null && String.valueOf(j).equals(request.getParameter("day" + i))) {
		    						_xhtml_out.print(" selected=\"selected\"");
		    					}
		    					_xhtml_out.print(">");
	    						_xhtml_out.print(j);
	    						_xhtml_out.println("</option>");
	    					}
	    					_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.print("<td>");
	    					_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"hour");
	    					_xhtml_out.print(i);
	    					_xhtml_out.print("\"");
	    					if(request.getParameter("hour" + i) != null) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(request.getParameter("hour" + i));
	    						_xhtml_out.print("\"");
	    					}
	    					_xhtml_out.print("/> : <input class=\"network_octet\" type=\"text\" name=\"min");
	    					_xhtml_out.print(i);
	    					_xhtml_out.print("\"");
	    					if(request.getParameter("min" + i) != null) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(request.getParameter("min" + i));
	    						_xhtml_out.print("\"");
	    					}
	    					_xhtml_out.print("/> ");
	    					_xhtml_out.print(getLanguageMessage("common.message.hours"));
	    					_xhtml_out.println("</td>");
	                    	_xhtml_out.println("</tr>");
	                    	_offset++;
	                    }
	        	    	_xhtml_out.println("</table>");
			            _xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
				    	_xhtml_out.println("</form>");
		    		}
	    			break;
	    		case SCHEDULE_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.schedule.specify_name"));
		    			}
		    			List<Map<String, String>> _schedule_days = new ArrayList<Map<String,String>>();
		    			for(int i = 1; request.getParameter("level" + i) != null && request.getParameter("month" + i) != null && request.getParameter("day" + i) != null && request.getParameter("hour" + i) != null && request.getParameter("min" + i) != null; i++) {
		    				int hour = 0, minute = 0;
		    				try {
			    				Integer.parseInt(request.getParameter("month" + i));
			    			} catch(NumberFormatException _ex) {
			    				continue;
			    			}
		    				try {
			    				Integer.parseInt(request.getParameter("day" + i));
			    			} catch(NumberFormatException _ex) {
			    				continue;
			    			}
		    				try {
			    				hour = Integer.parseInt(request.getParameter("hour" + i));
			    			} catch(NumberFormatException _ex) {
			    				continue;
			    			}
			    			try {
			    				minute = Integer.parseInt(request.getParameter("min" + i));
			    			} catch(NumberFormatException _ex) {
			    				continue;
			    			}
			    			Map<String, String> _day = new HashMap<String, String>();
		    				_day.put("level", request.getParameter("level" + i));
		    				_day.put("pool", request.getParameter("pool" + i));
		    				_day.put("month", request.getParameter("month" + i));
		    				_day.put("day", request.getParameter("day" + i));
		    				_day.put("hour", StringFormat.getTwoCharTimeComponent(hour));
		    				_day.put("min", StringFormat.getTwoCharTimeComponent(minute));
		    				_schedule_days.add(_day);
		    			}
		    			
		    			if (_schedule_days.size()<=0)
		    				throw new Exception(getLanguageMessage("backup.schedule.specify_days"));
		    			
		    			ScheduleManager.setYearlyScheduler(request.getParameter("name"), _schedule_days);
		    			response.sendRedirect("/admin/BackupScheduleWeekly");
		    			this.redirected=true;
	    			}
	    			break;
	    		case SCHEDULE_EDIT: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.schedule.specify_name"));
		    			}
	    			
		    			int _days = 2, _offset = 0;
		    			if(request.getParameter("days") != null) {
	                    	try {
	                    		_days = Integer.parseInt(request.getParameter("days"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
		    			
		    			List<Map<String, String>> _schedules = ScheduleManager.getYearlySchedule(request.getParameter("name"));
		    			PoolManager _pm = new PoolManager(this.sessionManager.getConfiguration());
		    			List<String> _pools = _pm.getPoolNames();
		    			
		    			_xhtml_out.println("<script>");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("function AddDay() {");
		    			_xhtml_out.print("  document.schedule.type.value = ");
		    			_xhtml_out.print(SCHEDULE_EDIT);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.schedule.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("//-->");
		    			_xhtml_out.println("</script>");
		    			_xhtml_out.println("<form action=\"/admin/BackupScheduleYearly\" name=\"schedule\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SCHEDULE_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"days\" value=\"" + (_days + 1) + "\"/>");
		    			writeDocumentBack("/admin/BackupScheduleWeekly");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/calendar_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.schedule"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.edit.year"));
	   //               _xhtml_out.println("<a href=\"javascript:document.schedule.submit();\"><img src=\"/images/disk_16.png\"/></a>");
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.schedule.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
          
	   //               _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
           
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" disabled=\"disabled\"");
	    					if(request.getParameter("name") != null) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(request.getParameter("name"));
	    						_xhtml_out.print("\"");
	    					}
	    					_xhtml_out.print("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("common.message.days"));
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.schedule.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");

	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

	            		_xhtml_out.print("<a href=\"javascript:AddDay();\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
                
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.level")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.pool")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.month")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.day")+"</td>");
	                    _xhtml_out.println("<td class=\"title\">"+getLanguageMessage("backup.schedule.backup.time")+"</td>");
	                    for(int i = 1; i < _days || i <= _schedules.size(); i++) {
	                    	Map<String, String> _schedule = null;
	                    	if(i <= _schedules.size()) {
	                    		_schedule = _schedules.get(i - 1);
	                    	}
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/calendar_view_day_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"level");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	    					_xhtml_out.print("<option value=\"Incremental\"");
	    					if(request.getParameter("level" + i) != null && "Incremental".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("level") && "Incremental".equals(_schedule.get("level"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_incremental"));
		        	    	_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"Differential\"");
	    					if(request.getParameter("level" + i) != null && "Differential".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("level") && "Differential".equals(_schedule.get("level"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_differential"));
		        	    	_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"Full\"");
	    					if(request.getParameter("level" + i) != null && "Full".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("level") && "Full".equals(_schedule.get("level"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_full"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.print("<option value=\"VirtualFull\"");
	    					if(request.getParameter("level" + i) != null && "VirtualFull".equals(request.getParameter("level" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("level") && "VirtualFull".equals(_schedule.get("level"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_virtual"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"pool");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	    					for(String _pool: _pools) {
	    						_xhtml_out.print("<option value=\"");
	    						_xhtml_out.print(_pool);
	    						_xhtml_out.print("\"");
		    					if(request.getParameter("pool" + i) != null && _pool.equals(request.getParameter("pool" + i))) {
		    						_xhtml_out.print(" selected=\"selected\"");
		    					} else if(_schedule != null && _schedule.containsKey("pool") && _pool.equals(_schedule.get("pool"))) {
		    						_xhtml_out.print(" selected=\"selected\"");
		    					}
		    					_xhtml_out.print(">");
	    						_xhtml_out.print(_pool);
	    						_xhtml_out.println("</option>");
	    					}
	    					_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"month");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					_xhtml_out.print("<option value=\"0\"");
	    					if(request.getParameter("month" + i) != null && "0".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "jan".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.january"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"1\"");
	    					if(request.getParameter("month" + i) != null && "1".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "feb".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.february"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"2\"");
	    					if(request.getParameter("month" + i) != null && "2".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "mar".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.march"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"3\"");
	    					if(request.getParameter("month" + i) != null && "3".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "apr".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.april"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"4\"");
	    					if(request.getParameter("month" + i) != null && "4".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "may".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.may"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"5\"");
	    					if(request.getParameter("month" + i) != null && "5".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "jun".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.june"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"6\"");
	    					if(request.getParameter("month" + i) != null && "6".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "jul".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.july"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"7\"");
	    					if(request.getParameter("month" + i) != null && "7".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "aug".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.august"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"8\"");
	    					if(request.getParameter("month" + i) != null && "8".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "sep".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.september"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"9\"");
	    					if(request.getParameter("month" + i) != null && "9".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "oct".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.october"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"10\"");
	    					if(request.getParameter("month" + i) != null && "10".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "nov".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.november"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.print("<option value=\"11\"");
	    					if(request.getParameter("month" + i) != null && "11".equals(request.getParameter("month" + i))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					} else if(_schedule != null && _schedule.containsKey("month") && "dec".equals(_schedule.get("month"))) {
	    						_xhtml_out.print(" selected=\"selected\"");
	    					}
	    					_xhtml_out.print(">");
	    					_xhtml_out.print(getLanguageMessage("common.message.december"));
	    					_xhtml_out.println("</option>");
	    					_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.println("<td>");
	    					_xhtml_out.print("<select class=\"form_select\" name=\"day");
	    					_xhtml_out.print(i);
	    					_xhtml_out.println("\">");
	    					for(int j = 1; j <= 31; j++) {
	    						_xhtml_out.print("<option value=\"");
	    						_xhtml_out.print(j);
	    						_xhtml_out.print("\"");
		    					if(request.getParameter("day" + i) != null && String.valueOf(j).equals(request.getParameter("day" + i))) {
		    						_xhtml_out.print(" selected=\"selected\"");
		    					} else if(_schedule != null && _schedule.containsKey("day") && String.valueOf(j).equals(_schedule.get("day"))) {
		    						_xhtml_out.print(" selected=\"selected\"");
		    					}
		    					_xhtml_out.print(">");
	    						_xhtml_out.print(j);
	    						_xhtml_out.println("</option>");
	    					}
	    					_xhtml_out.println("</select>");
	    					_xhtml_out.println("</td>");
	    					_xhtml_out.print("<td>");
	    					_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"hour");
	    					_xhtml_out.print(i);
	    					_xhtml_out.print("\"");
	    					if(request.getParameter("hour" + i) != null) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(request.getParameter("hour" + i));
	    						_xhtml_out.print("\"");
	    					} else if(_schedule != null && _schedule.containsKey("hour")) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(_schedule.get("hour"));
	    						_xhtml_out.print("\"");
	    					}
	    					_xhtml_out.print("/> : <input class=\"network_octet\" type=\"text\" name=\"min");
	    					_xhtml_out.print(i);
	    					_xhtml_out.print("\"");
	    					if(request.getParameter("min" + i) != null) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(request.getParameter("min" + i));
	    						_xhtml_out.print("\"");
	    					} else if(_schedule != null && _schedule.containsKey("minute")) {
	    						_xhtml_out.print(" value=\"");
	    						_xhtml_out.print(_schedule.get("minute"));
	    						_xhtml_out.print("\"");
	    					}
	    					_xhtml_out.print("/> ");
	    					_xhtml_out.print(getLanguageMessage("common.message.hours"));
	    					_xhtml_out.println("</td>");
	                    	_xhtml_out.println("</tr>");
	                    	_offset++;
	                    }
	        	    	_xhtml_out.println("</table>");
			            _xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
				    	_xhtml_out.println("</form>");
		    		}
	    			break;
	    		case SCHEDULE_REMOVE: {
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.schedule.specify_name"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
			    			ScheduleManager.removeSchedule(request.getParameter("name"));
			    			response.sendRedirect("/admin/BackupScheduleWeekly");
			    			this.redirected=true;
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("backup.schedule.question"), "/admin/BackupScheduleYearly?type=" + SCHEDULE_REMOVE + "&name=" + request.getParameter("name") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    	}
 		} catch(Exception _ex) {
			writeDocumentError(getWBSLocalizedExMessage(_ex.getMessage()));
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}
	
	