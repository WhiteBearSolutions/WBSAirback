package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.util.Configuration;

public class BackupLog extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	private int type;
	public final static String baseUrl = "/admin/"+BackupLog.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		 PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    	}
	    	
	    	this.type = 1;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			//response.setContentType("text/html");
		   // writeDocumentHeader();
		    
		    Configuration _c = this.sessionManager.getConfiguration();
		    ClientManager _cm = new ClientManager(_c);
		    JobManager _jm = new JobManager(_c);
		    
		    switch(this.type) {
	    		default: {
		    			int clientId, jobId, _offset = 0;
		    			try {
		    				jobId = Integer.parseInt(request.getParameter("jobId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("common.message.no_job"));
		    			}
		    			try {
		    				clientId = Integer.parseInt(request.getParameter("clientId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("common.message.no_client"));
		    			}
		    			
		    			String clientName = _cm.getClientName(clientId);
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			String jobName = _jm.getJobName(jobId);
		    			
		    			//writeDocumentBack("/admin/BackupJobs?clientId=" + clientId + "&clientName=" + clientName);
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/book_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.jobs.report"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.jobs.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(jobName);
	            		/*_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");*/
		
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    for(Map<String, String> log : _jm.getJobLog(clientName, jobId)) {
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
							_xhtml_out.print("<td><nobr>");
							_xhtml_out.print(log.get("time"));
							_xhtml_out.println("</nobr></td>");
							if(log.get("error") != null && "true".equals(log.get("error"))) {
								_xhtml_out.print("<td class=\"highlight_error\">");
							} else {
								_xhtml_out.print("<td>");
							}
							try {
								for(String line : log.get("text").split("\n")) {
									try {
										_xhtml_out.print(line);
										_xhtml_out.print("<br/>");
									} catch(Exception _ex) {
										_xhtml_out.print(line.getBytes());
										_xhtml_out.print("<br/>");
									}
						        }
							} catch(Exception _ex) {
								_xhtml_out.print(getLanguageMessage("backup.jobs.exception"));
							}
					        _xhtml_out.println("</td>");
					        _xhtml_out.println("</tr>");
					        _offset++;
	       		        }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	    			}
	    			break;
	    	}
		} catch(Exception _ex) {
			writeDocumentError(_ex.toString());
	    } finally {
	    	//writeDocumentFooter();
	    }
	}
}
