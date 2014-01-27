package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ScheduleManager;

public class BackupScheduleMonthly extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int SCHEDULE_ADD = 1;
	public final static int SCHEDULE_EDIT = 2;
	public final static int SCHEDULE_SAVE = 3;
	public final static int SCHEDULE_DELETE = 4;
	private int type;
	public final static String baseUrl = "/admin/"+BackupScheduleMonthly.class.getSimpleName();
	
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
		    
		    PoolManager _pm = new PoolManager(this.sessionManager.getConfiguration());
		    
		    switch(this.type) {
	    		default: {
	    				response.sendRedirect("/admin/BackupScheduleWeekly");
	    				this.redirected=true;
	    			}
        		    break;
	    		case SCHEDULE_ADD: {
	    				writeDocumentHeader();
		    			
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
		    			_xhtml_out.println("<form action=\"/admin/BackupScheduleMonthly\" name=\"schedule\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SCHEDULE_SAVE + "\"/>");
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
		        		_xhtml_out.print("<a href=\"javascript:document.schedule.submit();\"><img src=\"/images/disk_16.png\" title=\"");
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
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_full"));
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
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_full.info"));
		    			_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"fullh\"/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"fullm\"/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolFull\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	                    _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd1\" value=\"1st mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd2\" value=\"1st tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd3\" value=\"1st wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd4\" value=\"1st thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd5\" value=\"1st fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd6\" value=\"1st sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd7\" value=\"1st sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd8\" value=\"2nd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd9\" value=\"2nd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd10\" value=\"2nd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd11\" value=\"2nd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd12\" value=\"2nd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd13\" value=\"2nd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd14\" value=\"2nd sun\"/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd15\" value=\"3rd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd16\" value=\"3rd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd17\" value=\"3rd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd18\" value=\"3rd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd19\" value=\"3rd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd20\" value=\"3rd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd21\" value=\"3rd sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd22\" value=\"4th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd23\" value=\"4th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd24\" value=\"4th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd25\" value=\"4th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd26\" value=\"4th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd27\" value=\"4th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd28\" value=\"4th sun\"/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd29\" value=\"5th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd30\" value=\"5th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd31\" value=\"5th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd32\" value=\"5th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd33\" value=\"5th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd34\" value=\"5th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd35\" value=\"5th sun\"/></td>");
						_xhtml_out.println("</tr>");
	        	    	_xhtml_out.println("</table>");
	        	    	_xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");	    	
				    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_incremental"));
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
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_incremental.info"));
		    			_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"increh\"/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"increm\"/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolIncre\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	        _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id1\" value=\"1st mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id2\" value=\"1st tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id3\" value=\"1st wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id4\" value=\"1st thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id5\" value=\"1st fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id6\" value=\"1st sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id7\" value=\"1st sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id8\" value=\"2nd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id9\" value=\"2nd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id10\" value=\"2nd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id11\" value=\"2nd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id12\" value=\"2nd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id13\" value=\"2nd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id14\" value=\"2nd sun\"/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id15\" value=\"3rd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id16\" value=\"3rd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id17\" value=\"3rd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id18\" value=\"3rd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id19\" value=\"3rd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id20\" value=\"3rd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id21\" value=\"3rd sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id22\" value=\"4th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id23\" value=\"4th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id24\" value=\"4th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id25\" value=\"4th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id26\" value=\"4th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id27\" value=\"4th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id28\" value=\"4th sun\"/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id29\" value=\"5th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id30\" value=\"5th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id31\" value=\"5th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id32\" value=\"5th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id33\" value=\"5th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id34\" value=\"5th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id35\" value=\"5th sun\"/></td>");
						_xhtml_out.println("</tr>");
	        	    	_xhtml_out.println("</table>");
	        	    	_xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
				    	
				    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_differential"));
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
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_differential.info"));
		    			_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"diffh\"/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"diffm\"/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolDiff\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	                    _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
	                    _xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd1\" value=\"1st mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd2\" value=\"1st tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd3\" value=\"1st wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd4\" value=\"1st thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd5\" value=\"1st fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd6\" value=\"1st sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd7\" value=\"1st sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd8\" value=\"2nd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd9\" value=\"2nd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd10\" value=\"2nd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd11\" value=\"2nd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd12\" value=\"2nd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd13\" value=\"2nd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd14\" value=\"2nd sun\"/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd15\" value=\"3rd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd16\" value=\"3rd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd17\" value=\"3rd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd18\" value=\"3rd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd19\" value=\"3rd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd20\" value=\"3rd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd21\" value=\"3rd sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd22\" value=\"4th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd23\" value=\"4th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd24\" value=\"4th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd25\" value=\"4th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd26\" value=\"4th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd27\" value=\"4th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd28\" value=\"4th sun\"/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd29\" value=\"5th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd30\" value=\"5th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd31\" value=\"5th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd32\" value=\"5th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd33\" value=\"5th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd34\" value=\"5th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd35\" value=\"5th sun\"/></td>");
						_xhtml_out.println("</tr>");
	        	    	_xhtml_out.println("</table>");
			            _xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
				    	
				    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_virtual"));
		        		_xhtml_out.print("<a href=\"javascript:document.schedule.submit();\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
			
	            		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
		    
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_virtual.info"));
		    			_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"virth\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"virth\"/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"virtm\"/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolVirtual\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	                    _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd1\" value=\"1st mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd2\" value=\"1st tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd3\" value=\"1st wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd4\" value=\"1st thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd5\" value=\"1st fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd6\" value=\"1st sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd7\" value=\"1st sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd8\" value=\"2nd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd9\" value=\"2nd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd10\" value=\"2nd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd11\" value=\"2nd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd12\" value=\"2nd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd13\" value=\"2nd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd14\" value=\"2nd sun\"/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd15\" value=\"3rd mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd16\" value=\"3rd tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd17\" value=\"3rd wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd18\" value=\"3rd thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd19\" value=\"3rd fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd20\" value=\"3rd sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd21\" value=\"3rd sun\"/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd22\" value=\"4th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd23\" value=\"4th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd24\" value=\"4th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd25\" value=\"4th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd26\" value=\"4th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd27\" value=\"4th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd28\" value=\"4th sun\"/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd29\" value=\"5th mon\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd30\" value=\"5th tue\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd31\" value=\"5th wed\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd32\" value=\"5th thu\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd33\" value=\"5th fri\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd34\" value=\"5th sat\"/></td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd35\" value=\"5th sun\"/></td>");
						_xhtml_out.println("</tr>");
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
		    			
		    			int fullh = 0, fullm = 0, increh = 0, increm = 0, difh = 0, difm = 0, vifh = 0, vifm = 0; 
		    			Map<String, String> values = new HashMap<String, String>();
		    			
		    			if(request.getParameter("fullh") != null && request.getParameter("fullm") != null &&
		    					!request.getParameter("fullh").trim().isEmpty() && !request.getParameter("fullm").trim().isEmpty()) {
		    				try {
		    					fullh = Integer.parseInt(request.getParameter("fullh"));
				    			if(fullh<0 || fullh>23) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_hour"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.full_no_hour"));
		    				}
		    				try {
		    					fullm = Integer.parseInt(request.getParameter("fullm"));
				    			if(fullm<0 || fullm>59) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_minute"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.full_no_minute"));
		    				}
		    				
		    				for(int r = 1; r <= 35; r++) {
			    				if(request.getParameter("fd" + r) != null && !request.getParameter("fd" + r).isEmpty()) {
			    					values.put("fd" + r, request.getParameter("fd" + r));
			    				}
			    			}
		    			}
		    			if(request.getParameter("increh") != null && request.getParameter("increm") != null &&
		    					!request.getParameter("increh").trim().isEmpty() && !request.getParameter("increm").trim().isEmpty()) {
		    				try {
		    					increh = Integer.parseInt(request.getParameter("increh"));
				    			if(increh<0 || increh>23) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_hour"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.incremental_no_hour"));
		    				}
		    				try {
		    					increm = Integer.parseInt(request.getParameter("increm"));
				    			if(increm<0 || increm>59) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_minute"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.incremental_no_minute"));
		    				}
		    				
		    				for(int r = 1; r <= 35; r++) {
			    				if(request.getParameter("id" + r) != null && !request.getParameter("id" + r).isEmpty()) {
			    					values.put("id" + r, request.getParameter("id" + r));
			    				}
			    			}
		    			}
		    			if(request.getParameter("diffh") != null && request.getParameter("diffm") != null &&
		    					!request.getParameter("diffh").trim().isEmpty() && !request.getParameter("diffm").trim().isEmpty()) {
		    				try {
		    					difh = Integer.parseInt(request.getParameter("diffh"));
				    			if(difh<0 || difh>23) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_hour"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.diferencial_no_hour"));
		    				}
		    				try {
		    					difm = Integer.parseInt(request.getParameter("diffm"));
				    			if(difm<0 || difm>59) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_minute"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.diferencial_no_minute"));
		    				}
		    				
		    				for(int r = 1; r <= 35; r++) {
			    				if(request.getParameter("dd" + r) != null && !request.getParameter("dd" + r).isEmpty()) {
			    					values.put("dd" + r, request.getParameter("dd" + r));
			    				}
			    			}
		    			}
		    			if(request.getParameter("virth") != null && request.getParameter("virtm") != null &&
		    					!request.getParameter("virth").trim().isEmpty() && !request.getParameter("virtm").trim().isEmpty()) {
		    				try {
		    					vifh = Integer.parseInt(request.getParameter("virth"));
				    			if(vifh<0 || vifh>23) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_hour"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.virtual_no_hour"));
		    				}
		    				try {
		    					vifm = Integer.parseInt(request.getParameter("virtm"));
				    			if(vifm<0 || vifm>59) {
				    				throw new Exception(getLanguageMessage("backup.schedule._wrong_minute"));
				    			}
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("backup.schedule.virtual_no_minute"));
		    				}
		    				
		    				for(int r = 1; r <= 35; r++) {
			    				if(request.getParameter("vd" + r) != null && !request.getParameter("vd" + r).isEmpty()) {
			    					values.put("vd" + r, request.getParameter("vd" + r));
			    				}
			    			}
		    			}
		    			
		    			ScheduleManager.setMonthlyScheduler(request.getParameter("name"), values, request.getParameter("poolFull"), fullh, fullm, request.getParameter("poolIncre"), increh, increm, request.getParameter("poolDiff"), difh, difm, request.getParameter("poolVirtual"), vifh, vifm);
		    			response.sendRedirect("/admin/BackupScheduleWeekly");
		    			this.redirected=true;
	    			}
	    			break;
	    		case SCHEDULE_EDIT: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.schedule.specify_name"));
		    			}
		    			
	    				List<String> _pools = _pm.getPoolNames();
	    				Map<String, String> _schedule = ScheduleManager.getMonthlySchedule(request.getParameter("name"));
	    				
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
		    			_xhtml_out.println("<form action=\"/admin/BackupScheduleMonthly\" name=\"schedule\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SCHEDULE_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
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
						_xhtml_out.print(getLanguageMessage("backup.schedule.edit.month"));
		        		_xhtml_out.print("<a href=\"javascript:document.schedule.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
				
	            		_xhtml_out.print("<a href=\"javascript:document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
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
    					_xhtml_out.print(" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_full"));
		        		_xhtml_out.print("<a href=\"javascript:document.schedule.submit();\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
					
	            		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
		          
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_full.info"));
		    			_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"fullh\"");
	        	    	if(_schedule.containsKey("full.hour")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("full.hour"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"fullm\"");
	        	    	if(_schedule.containsKey("full.minute")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("full.minute"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.print("/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolFull\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
		        	    	if(_schedule.containsKey("full.pool") && pool.equals(_schedule.get("full.pool"))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	                    _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd1\" value=\"1st mon\"");
	        	    	if(_schedule.containsKey("calendar.fd1")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd2\" value=\"1st tue\"");
	        	    	if(_schedule.containsKey("calendar.fd2")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd3\" value=\"1st wed\"");
	        	    	if(_schedule.containsKey("calendar.fd3")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd4\" value=\"1st thu\"");
	        	    	if(_schedule.containsKey("calendar.fd4")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd5\" value=\"1st fri\"");
	        	    	if(_schedule.containsKey("calendar.fd5")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd6\" value=\"1st sat\"");
	        	    	if(_schedule.containsKey("calendar.fd6")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd7\" value=\"1st sun\"");
	        	    	if(_schedule.containsKey("calendar.fd7")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd8\" value=\"2nd mon\"");
	        	    	if(_schedule.containsKey("calendar.fd8")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd9\" value=\"2nd tue\"");
	        	    	if(_schedule.containsKey("calendar.fd9")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd10\" value=\"2nd wed\"");
	        	    	if(_schedule.containsKey("calendar.fd10")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd11\" value=\"2nd thu\"");
	        	    	if(_schedule.containsKey("calendar.fd11")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd12\" value=\"2nd fri\"");
	        	    	if(_schedule.containsKey("calendar.fd12")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd13\" value=\"2nd sat\"");
	        	    	if(_schedule.containsKey("calendar.fd13")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd14\" value=\"2nd sun\"");
	        	    	if(_schedule.containsKey("calendar.fd14")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd15\" value=\"3rd mon\"");
	        	    	if(_schedule.containsKey("calendar.fd15")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd16\" value=\"3rd tue\"");
	        	    	if(_schedule.containsKey("calendar.fd16")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd17\" value=\"3rd wed\"");
	        	    	if(_schedule.containsKey("calendar.fd17")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd18\" value=\"3rd thu\"");
	        	    	if(_schedule.containsKey("calendar.fd18")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd19\" value=\"3rd fri\"");
	        	    	if(_schedule.containsKey("calendar.fd19")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd20\" value=\"3rd sat\"");
	        	    	if(_schedule.containsKey("calendar.fd20")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd21\" value=\"3rd sun\"");
	        	    	if(_schedule.containsKey("calendar.fd21")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd22\" value=\"4th mon\"");
	        	    	if(_schedule.containsKey("calendar.fd22")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd23\" value=\"4th tue\"");
	        	    	if(_schedule.containsKey("calendar.fd23")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd24\" value=\"4th wed\"");
	        	    	if(_schedule.containsKey("calendar.fd24")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd25\" value=\"4th thu\"");
	        	    	if(_schedule.containsKey("calendar.fd25")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd26\" value=\"4th fri\"");
	        	    	if(_schedule.containsKey("calendar.fd26")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd27\" value=\"4th sat\"");
	        	    	if(_schedule.containsKey("calendar.fd27")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd28\" value=\"4th sun\"");
	        	    	if(_schedule.containsKey("calendar.fd28")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd29\" value=\"5th mon\"");
	        	    	if(_schedule.containsKey("calendar.fd29")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd30\" value=\"5th tue\"");
	        	    	if(_schedule.containsKey("calendar.fd30")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd31\" value=\"5th wed\"");
	        	    	if(_schedule.containsKey("calendar.fd31")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd32\" value=\"5th thu\"");
	        	    	if(_schedule.containsKey("calendar.fd32")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd33\" value=\"5th fri\"");
	        	    	if(_schedule.containsKey("calendar.fd33")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd34\" value=\"5th sat\"");
	        	    	if(_schedule.containsKey("calendar.fd34")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"fd35\" value=\"5th sun\"");
	        	    	if(_schedule.containsKey("calendar.fd35")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
	        	    	_xhtml_out.println("</table>");
	        	    	_xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_incremental"));
						
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
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_incremental.info"));
		    			_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"increh\"");
	        	    	if(_schedule.containsKey("incremental.hour")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("incremental.hour"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"increm\"");
	        	    	if(_schedule.containsKey("incremental.minute")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("incremental.minute"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolIncre\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
		        	    	if(_schedule.containsKey("incremental.pool") && pool.equals(_schedule.get("incremental.pool"))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	                    _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
	                    _xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id1\" value=\"1st mon\"");
	        	    	if(_schedule.containsKey("calendar.id1")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id2\" value=\"1st tue\"");
	        	    	if(_schedule.containsKey("calendar.id2")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id3\" value=\"1st wed\"");
	        	    	if(_schedule.containsKey("calendar.id3")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id4\" value=\"1st thu\"");
	        	    	if(_schedule.containsKey("calendar.id4")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id5\" value=\"1st fri\"");
	        	    	if(_schedule.containsKey("calendar.id5")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id6\" value=\"1st sat\"");
	        	    	if(_schedule.containsKey("calendar.id6")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id7\" value=\"1st sun\"");
	        	    	if(_schedule.containsKey("calendar.id7")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id8\" value=\"2nd mon\"");
	        	    	if(_schedule.containsKey("calendar.id8")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id9\" value=\"2nd tue\"");
	        	    	if(_schedule.containsKey("calendar.id9")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id10\" value=\"2nd wed\"");
	        	    	if(_schedule.containsKey("calendar.id10")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id11\" value=\"2nd thu\"");
	        	    	if(_schedule.containsKey("calendar.id11")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id12\" value=\"2nd fri\"");
	        	    	if(_schedule.containsKey("calendar.id12")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id13\" value=\"2nd sat\"");
	        	    	if(_schedule.containsKey("calendar.id13")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id14\" value=\"2nd sun\"");
	        	    	if(_schedule.containsKey("calendar.id14")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id15\" value=\"3rd mon\"");
	        	    	if(_schedule.containsKey("calendar.id15")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id16\" value=\"3rd tue\"");
	        	    	if(_schedule.containsKey("calendar.id16")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id17\" value=\"3rd wed\"");
	        	    	if(_schedule.containsKey("calendar.id17")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id18\" value=\"3rd thu\"");
	        	    	if(_schedule.containsKey("calendar.id18")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id19\" value=\"3rd fri\"");
	        	    	if(_schedule.containsKey("calendar.id19")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id20\" value=\"3rd sat\"");
	        	    	if(_schedule.containsKey("calendar.id20")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id21\" value=\"3rd sun\"");
	        	    	if(_schedule.containsKey("calendar.id21")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id22\" value=\"4th mon\"");
	        	    	if(_schedule.containsKey("calendar.id22")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id23\" value=\"4th tue\"");
	        	    	if(_schedule.containsKey("calendar.id23")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id24\" value=\"4th wed\"");
	        	    	if(_schedule.containsKey("calendar.id24")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id25\" value=\"4th thu\"");
	        	    	if(_schedule.containsKey("calendar.id25")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id26\" value=\"4th fri\"");
	        	    	if(_schedule.containsKey("calendar.id26")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id27\" value=\"4th sat\"");
	        	    	if(_schedule.containsKey("calendar.id27")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id28\" value=\"4th sun\"");
	        	    	if(_schedule.containsKey("calendar.id28")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id29\" value=\"5th mon\"");
	        	    	if(_schedule.containsKey("calendar.id29")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id30\" value=\"5th tue\"");
	        	    	if(_schedule.containsKey("calendar.id30")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id31\" value=\"5th wed\"");
	        	    	if(_schedule.containsKey("calendar.id31")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id32\" value=\"5th thu\"");
	        	    	if(_schedule.containsKey("calendar.id32")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id33\" value=\"5th fri\"");
	        	    	if(_schedule.containsKey("calendar.id33")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id34\" value=\"5th sat\"");
	        	    	if(_schedule.containsKey("calendar.id34")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"id35\" value=\"5th sun\"");
	        	    	if(_schedule.containsKey("calendar.id35")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
	        	    	_xhtml_out.println("</table>");
	        	    	_xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
				    	
				    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_differential"));
		        		_xhtml_out.print("<a href=\"javascript:submitForm(document.schedule.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
			
	            		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
		
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_differential.info"));
		    			_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"diffh\"");
	        	    	if(_schedule.containsKey("differential.hour")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("differential.hour"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"diffm\"");
	        	    	if(_schedule.containsKey("differential.minute")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("differential.minute"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolDiff\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
		        	    	if(_schedule.containsKey("differential.pool") && pool.equals(_schedule.get("differential.pool"))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	                    _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
	                    _xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd1\" value=\"1st mon\"");
	        	    	if(_schedule.containsKey("calendar.dd1")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd2\" value=\"1st tue\"");
	        	    	if(_schedule.containsKey("calendar.dd2")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd3\" value=\"1st wed\"");
	        	    	if(_schedule.containsKey("calendar.dd3")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd4\" value=\"1st thu\"");
	        	    	if(_schedule.containsKey("calendar.dd4")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd5\" value=\"1st fri\"");
	        	    	if(_schedule.containsKey("calendar.dd5")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd6\" value=\"1st sat\"");
	        	    	if(_schedule.containsKey("calendar.dd6")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd7\" value=\"1st sun\"");
	        	    	if(_schedule.containsKey("calendar.dd7")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd8\" value=\"2nd mon\"");
	        	    	if(_schedule.containsKey("calendar.dd8")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd9\" value=\"2nd tue\"");
	        	    	if(_schedule.containsKey("calendar.dd9")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd10\" value=\"2nd wed\"");
	        	    	if(_schedule.containsKey("calendar.dd10")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd11\" value=\"2nd thu\"");
	        	    	if(_schedule.containsKey("calendar.dd11")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd12\" value=\"2nd fri\"");
	        	    	if(_schedule.containsKey("calendar.dd12")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd13\" value=\"2nd sat\"");
	        	    	if(_schedule.containsKey("calendar.dd13")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd14\" value=\"2nd sun\"");
	        	    	if(_schedule.containsKey("calendar.dd14")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd15\" value=\"3rd mon\"");
	        	    	if(_schedule.containsKey("calendar.dd15")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd16\" value=\"3rd tue\"");
	        	    	if(_schedule.containsKey("calendar.dd16")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd17\" value=\"3rd wed\"");
	        	    	if(_schedule.containsKey("calendar.dd17")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd18\" value=\"3rd thu\"");
	        	    	if(_schedule.containsKey("calendar.dd18")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd19\" value=\"3rd fri\"");
	        	    	if(_schedule.containsKey("calendar.dd19")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd20\" value=\"3rd sat\"");
	        	    	if(_schedule.containsKey("calendar.dd20")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd21\" value=\"3rd sun\"");
	        	    	if(_schedule.containsKey("calendar.dd21")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd22\" value=\"4th mon\"");
	        	    	if(_schedule.containsKey("calendar.dd22")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd23\" value=\"4th tue\"");
	        	    	if(_schedule.containsKey("calendar.dd23")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd24\" value=\"4th wed\"");
	        	    	if(_schedule.containsKey("calendar.dd24")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd25\" value=\"4th thu\"");
	        	    	if(_schedule.containsKey("calendar.dd25")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd26\" value=\"4th fri\"");
	        	    	if(_schedule.containsKey("calendar.dd26")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd27\" value=\"4th sat\"");
	        	    	if(_schedule.containsKey("calendar.dd27")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd28\" value=\"4th sun\"");
	        	    	if(_schedule.containsKey("calendar.dd28")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd29\" value=\"5th mon\"");
	        	    	if(_schedule.containsKey("calendar.dd29")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd30\" value=\"5th tue\"");
	        	    	if(_schedule.containsKey("calendar.dd30")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd31\" value=\"5th wed\"");
	        	    	if(_schedule.containsKey("calendar.dd31")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd32\" value=\"5th thu\"");
	        	    	if(_schedule.containsKey("calendar.dd32")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd33\" value=\"5th fri\"");
	        	    	if(_schedule.containsKey("calendar.dd33")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd34\" value=\"5th sat\"");
	        	    	if(_schedule.containsKey("calendar.dd34")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"dd35\" value=\"5th sun\"");
	        	    	if(_schedule.containsKey("calendar.dd35")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
	        	    	_xhtml_out.println("</table>");
			            _xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
				    	
				    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.schedule.days_planning_virtual"));
	            		_xhtml_out.print("<a href=\"javascript:document.schedule.submit();\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	
	            		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
          
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("backup.schedule.level_virtual.info"));
		    			_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"virth\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"virth\"");
	        	    	if(_schedule.containsKey("virtual.hour")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("virtual.hour"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.print(":");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"virtm\"");
	        	    	if(_schedule.containsKey("virtual.hour")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(_schedule.get("virtual.minute"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
						_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolVirtual\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String pool : _pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
		        	    	if(_schedule.containsKey("virtual.pool") && pool.equals(_schedule.get("virtual.pool"))) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	                    _xhtml_out.println("<table class=\"schedule_calendar\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td class=\"title\">&nbsp;</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.monday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.tuesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.wednesday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.thursday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.friday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.saturday"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.sunday"));
						_xhtml_out.println("</td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 1</td>");
						_xhtml_out.println("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd1\" value=\"1st mon\"");
	        	    	if(_schedule.containsKey("calendar.vd1")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd2\" value=\"1st tue\"");
	        	    	if(_schedule.containsKey("calendar.vd2")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd3\" value=\"1st wed\"");
	        	    	if(_schedule.containsKey("calendar.vd3")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd4\" value=\"1st thu\"");
	        	    	if(_schedule.containsKey("calendar.vd4")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd5\" value=\"1st fri\"");
	        	    	if(_schedule.containsKey("calendar.vd5")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd6\" value=\"1st sat\"");
	        	    	if(_schedule.containsKey("calendar.vd6")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd7\" value=\"1st sun\"");
	        	    	if(_schedule.containsKey("calendar.vd7")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 2</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd8\" value=\"2nd mon\"");
	        	    	if(_schedule.containsKey("calendar.vd8")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd9\" value=\"2nd tue\"");
	        	    	if(_schedule.containsKey("calendar.vd9")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd10\" value=\"2nd wed\"");
	        	    	if(_schedule.containsKey("calendar.vd10")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd11\" value=\"2nd thu\"");
	        	    	if(_schedule.containsKey("calendar.vd11")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd12\" value=\"2nd fri\"");
	        	    	if(_schedule.containsKey("calendar.vd12")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd13\" value=\"2nd sat\"");
	        	    	if(_schedule.containsKey("calendar.vd13")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd14\" value=\"2nd sun\"");
	        	    	if(_schedule.containsKey("calendar.vd14")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");			
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 3</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd15\" value=\"3rd mon\"");
	        	    	if(_schedule.containsKey("calendar.vd15")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd16\" value=\"3rd tue\"");
	        	    	if(_schedule.containsKey("calendar.vd16")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd17\" value=\"3rd wed\"");
	        	    	if(_schedule.containsKey("calendar.vd17")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd18\" value=\"3rd thu\"");
	        	    	if(_schedule.containsKey("calendar.vd18")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd19\" value=\"3rd fri\"");
	        	    	if(_schedule.containsKey("calendar.vd19")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd20\" value=\"3rd sat\"");
	        	    	if(_schedule.containsKey("calendar.vd20")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd21\" value=\"3rd sun\"");
	        	    	if(_schedule.containsKey("calendar.vd21")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 4</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd22\" value=\"4th mon\"");
	        	    	if(_schedule.containsKey("calendar.vd22")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd23\" value=\"4th tue\"");
	        	    	if(_schedule.containsKey("calendar.vd23")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd24\" value=\"4th wed\"");
	        	    	if(_schedule.containsKey("calendar.vd24")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd25\" value=\"4th thu\"");
	        	    	if(_schedule.containsKey("calendar.vd25")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd26\" value=\"4th fri\"");
	        	    	if(_schedule.containsKey("calendar.vd26")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd27\" value=\"4th sat\"");
	        	    	if(_schedule.containsKey("calendar.vd27")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd28\" value=\"4th sun\"");
	        	    	if(_schedule.containsKey("calendar.vd28")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");	
						_xhtml_out.println("<tr>");
						_xhtml_out.println("<td class=\"title\">");
						_xhtml_out.print(getLanguageMessage("common.message.week"));
						_xhtml_out.println(" 5</td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd29\" value=\"5th mon\"");
	        	    	if(_schedule.containsKey("calendar.vd29")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd30\" value=\"5th tue\"");
	        	    	if(_schedule.containsKey("calendar.vd30")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd31\" value=\"5th wed\"");
	        	    	if(_schedule.containsKey("calendar.vd31")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd32\" value=\"5th thu\"");
	        	    	if(_schedule.containsKey("calendar.vd32")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd33\" value=\"5th fri\"");
	        	    	if(_schedule.containsKey("calendar.vd33")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd34\" value=\"5th sat\"");
	        	    	if(_schedule.containsKey("calendar.vd34")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"vd35\" value=\"5th sun\"");
	        	    	if(_schedule.containsKey("calendar.vd35")) {
	        	    		_xhtml_out.print(" checked=\"checked\"");
	        	    	}
	        	    	_xhtml_out.println("/></td>");
						_xhtml_out.println("</tr>");
	        	    	_xhtml_out.println("</table>");
			            _xhtml_out.print("<br/>");
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.println("</div>");
				    	_xhtml_out.println("</form>");
	    			}
	    			break;
	    		case SCHEDULE_DELETE: {
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.schedule.specify_name"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
			    			ScheduleManager.removeSchedule(request.getParameter("name"));
			    			response.sendRedirect("/admin/BackupScheduleWeekly");
			    			this.redirected=true;
		    			} else {
		    				writeDocumentHeader();
		    				writeDocumentQuestion(getLanguageMessage("backup.schedule.question"), "/admin/BackupScheduleMonthly?type=" + SCHEDULE_DELETE + "&name=" + request.getParameter("name") + "&confirm=true", null);
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
