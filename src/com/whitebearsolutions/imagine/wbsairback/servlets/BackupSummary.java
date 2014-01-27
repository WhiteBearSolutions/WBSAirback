package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.RoleManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.util.ObjectLock;

public class BackupSummary extends WBSImagineServlet {
	static final long serialVersionUID = 20080902L;
	private int type;
	public final static String baseUrl = "/admin/"+BackupSummary.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter _xhtml_out=response.getWriter();
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
	    	
	    	
	    	List<String> categories = null;
			if(!this.securityManager.isAdministrator() && !this.securityManager.isRole(RoleManager.roleGlobalOperator)) {
				if((this.securityManager.isRole(RoleManager.roleOperator) || this.securityManager.isRole(RoleManager.roleUser) ) && this.securityManager.hasUserCategory()) {
					categories = this.securityManager.getUserCategories();
				}
				if (categories == null || categories.isEmpty()) {
					throw new Exception(getLanguageMessage("common.message.no_privilegios"));
				}
			}
		    
	    	JobManager _jm = new JobManager(this.sessionManager.getConfiguration());
	    	ClientManager _cm = new ClientManager(this.sessionManager.getConfiguration()); 
		    Map<String, Map<String, String>> vmwareJobs = JobManager.getProgrammedVmwareJobs();
		    switch(this.type) {
	    		default: {
		    			int _offset = 0;
		    			Map<String, List<Map<String, String>>> _summary_jobs = _jm.getSummaryJobs(categories);
		    			List<Map<String, String>> _running_jobs = _summary_jobs.get("running");
		    			List<Map<String, String>> _scheduled_jobs = _summary_jobs.get("scheduled");
		    			List<Map<String, String>> _terminated_jobs = _summary_jobs.get("terminated");
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/book_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.backup.summary"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.summary.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.summary.running_jobs"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.print("\" alt=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_running_jobs.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.level"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.status"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.start_date"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.total_files"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.total_size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.errors"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _job : _running_jobs) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	if (ObjectLock.isBlock(ObjectLock.JOBS_TYPE_OBJECT, _job.get("id"), null) && _job.get("canceled") != null)
		                    		_xhtml_out.print(" style=\"background-color:#F2C291;\"");
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("name"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if (_job.get("level") != null && !_job.get("level").isEmpty()) 
									_xhtml_out.print(_job.get("level"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("status"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("date"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("files"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("bytes"));
								if(_job.get("speed") != null &&
										!_job.get("speed").isEmpty()) {
									_xhtml_out.print(" (");
									_xhtml_out.print(_job.get("speed"));
									_xhtml_out.print(")");
								}
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("errors"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if(_job.get("clientid") != null && _job.get("client") != null &&
										!_job.get("clientid").isEmpty() && !_job.get("client").isEmpty()) {
									_xhtml_out.print("<a href=\"/admin/BackupJobs?clientId=");
									if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
										Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
										_xhtml_out.print("0");
										_xhtml_out.print("&clientName=");
										_xhtml_out.print(vmwareJob.get("client"));
									} else {
										_xhtml_out.print(_job.get("clientid"));
										_xhtml_out.print("&clientName=");
										_xhtml_out.print(_job.get("client"));
									}
									_xhtml_out.print("\"><img src=\"/images/cog_16.png\" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
			                    	_xhtml_out.println("\"/></a>");
		                    		if (!_job.get("name").contains("RestoreFiles") && !_job.get("name").contains("SaveClient") && !_job.get("name").contains("RestoreFilesVmware")) {
			                    		_xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
										_xhtml_out.print(BackupJobs.EDITJOB);
										_xhtml_out.print("&jobName=");
										_xhtml_out.print(_job.get("name"));
										if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
											Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(vmwareJob.get("client"));
											_xhtml_out.print("&clientId=0");
										} else {
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(_job.get("client"));
											_xhtml_out.print("&clientId=");
											_xhtml_out.print(_job.get("clientid"));
										}
										_xhtml_out.print("\"><img src=\"/images/cog_edit_16.png\" title=\"");
				                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
				                    	_xhtml_out.print("\" alt=\"");
				                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
				                    	_xhtml_out.println("\"/></a>");
		                    		}

			                    	if (!ObjectLock.isBlock(ObjectLock.JOBS_TYPE_OBJECT, _job.get("id"), null) && _job.get("canceled") == null) {
				                    	_xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
	                                    _xhtml_out.print(BackupJobs.STOPJOB);
	                                    _xhtml_out.print("&nameJob=");
	                                    _xhtml_out.print(_job.get("name"));
	                                    if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
											Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(vmwareJob.get("client"));
											_xhtml_out.print("&clientId=0");
										} else {
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(_job.get("client"));
											_xhtml_out.print("&clientId=");
											_xhtml_out.print(_job.get("clientid"));
										}
	                                    _xhtml_out.print("&jobId=");
	                                    _xhtml_out.print(_job.get("id"));
	                                    _xhtml_out.print("\"><img src=\"/images/control_pause_16.png\" title=\"");
					                	_xhtml_out.print(getLanguageMessage("common.message.stop"));
					                	_xhtml_out.print("\" alt=\"");
					                	_xhtml_out.print(getLanguageMessage("common.message.stop"));
					                	_xhtml_out.println("\"/></a>");
				                	
	                                    _xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
	                                    _xhtml_out.print(BackupJobs.CANCELJOB);
	                                    _xhtml_out.print("&nameJob=");
	                                    _xhtml_out.print(_job.get("name"));
	                                    if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
											Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(vmwareJob.get("client"));
											_xhtml_out.print("&clientId=0");
										} else {
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(_job.get("client"));
											_xhtml_out.print("&clientId=");
											_xhtml_out.print(_job.get("clientid"));
										}
	                                    _xhtml_out.print("&jobId=");
	                                    _xhtml_out.print(_job.get("id"));
	                                    _xhtml_out.print("\"><img src=\"/images/control_stop_16.png\" title=\"");
					                	_xhtml_out.print(getLanguageMessage("common.message.cancel"));
					                	_xhtml_out.print("\" alt=\"");
					                	_xhtml_out.print(getLanguageMessage("common.message.cancel"));
					                	_xhtml_out.println("\"/></a>");
				                	}
			                    	_xhtml_out.print("<a href='javascript:viewLog(\""+_job.get("id")+"\",\""+_job.get("clientid")+"\");' >");
			                    	_xhtml_out.print("<img src=\"/images/book_16.png\" title=\"");
				                	_xhtml_out.print(getLanguageMessage("common.message.view"));
				                	_xhtml_out.print("\" alt=\"");
				                	_xhtml_out.print(getLanguageMessage("common.message.view"));
				                	_xhtml_out.println("\"/></a>");
								}
			                	_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("backup.summary.message.no_running_jobs"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.summary.scheduled_jobs"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.print("\" alt=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_scheduled_jobs.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.level"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.priority"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.start_date"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.volume"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _job : _scheduled_jobs) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("name"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("type"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("level"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("priority"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("date"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								String vol = _job.get("volume");
								if (vol == null || vol.contains("unknow"))
									vol = getLanguageMessage("backup.jobs.volume.removed");
								_xhtml_out.print(vol);
								_xhtml_out.println("</td>");
								_xhtml_out.println("<td>");
								if (!_job.get("name").contains("RestoreFiles") && !_job.get("name").contains("SaveClient") && !_job.get("name").contains("RestoreFilesVmware")) {
		                    		_xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
									_xhtml_out.print(BackupJobs.EDITJOB);
									_xhtml_out.print("&jobName=");
									_xhtml_out.print(_job.get("name"));
									if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
										Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
										_xhtml_out.print("&clientName=");
										_xhtml_out.print(vmwareJob.get("client"));
										_xhtml_out.print("&clientId=0");
									} else {
										_xhtml_out.print("&clientName=");
										_xhtml_out.print(_job.get("client"));
										_xhtml_out.print("&clientId=");
										_xhtml_out.print(_cm.getClientId(_job.get("client")));
									}
									_xhtml_out.print("\"><img src=\"/images/cog_edit_16.png\" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    	_xhtml_out.println("\"/></a>");
								}
								_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("backup.summary.message.no_scheduled_jobs"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.summary.terminated_jobs"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.print("\" alt=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_terminated_jobs.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.level"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.total_files"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.total_size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.status"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.end_date"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.summary.client"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _job : _terminated_jobs) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	if (_job.get("eliminated") != null && _job.get("eliminated").equals("yes"))
		                    		_xhtml_out.print(" style=\"background-color:#F2C291;\"");
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(_job.get("name"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if (_job.get("level") != null && !_job.get("level").isEmpty()) 
									_xhtml_out.print(_job.get("level"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("files"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("size"));
								_xhtml_out.println("</td>");
								if("ERROR".equals(_job.get("status")) ||
										"CANCEL".equals(_job.get("status"))) {
									_xhtml_out.print("<td class=\"error\">");
								} else if("INCOMPLETE".equals(_job.get("status"))) {
									_xhtml_out.print("<td class=\"warning\">");
								} else if("OK".equals(_job.get("status"))) {
									_xhtml_out.print("<td class=\"good\">");
								} else {
									_xhtml_out.print("<td>");
								}
								_xhtml_out.print(_job.get("status"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_job.get("date"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
									Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
									if (vmwareJob.get("client") != null)
										_xhtml_out.print(vmwareJob.get("client"));
									else
										_xhtml_out.print(_job.get("client"));
								} else {
									_xhtml_out.print(_job.get("client"));
								}
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								if(_job.get("clientid") != null && _job.get("client") != null &&
										!_job.get("clientid").isEmpty() && !_job.get("client").isEmpty()) {
									_xhtml_out.print("<a href=\"/admin/BackupJobs?clientId=");
									 if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
										Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
										_xhtml_out.print("0");
										_xhtml_out.print("&clientName=");
										_xhtml_out.print(vmwareJob.get("client"));
										
									} else {
										_xhtml_out.print(_job.get("clientid"));
										_xhtml_out.print("&clientName=");
										_xhtml_out.print(_job.get("client"));
									}
									_xhtml_out.print("\"><img src=\"/images/cog_16.png\" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
			                    	_xhtml_out.println("\"/></a>");
		                    		if (!_job.get("name").contains("RestoreFiles") && !_job.get("name").contains("SaveClient") && !_job.get("name").contains("RestoreFilesVmware")) {
			                    		_xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
										_xhtml_out.print(BackupJobs.EDITJOB);
										_xhtml_out.print("&jobName=");
										_xhtml_out.print(_job.get("name"));
										if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
											Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(vmwareJob.get("client"));
											_xhtml_out.print("&clientId=0");
										} else {
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(_job.get("client"));
											_xhtml_out.print("&clientId=");
											_xhtml_out.print(_job.get("clientid"));
										}
										_xhtml_out.print("\"><img src=\"/images/cog_edit_16.png\" title=\"");
				                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
				                    	_xhtml_out.print("\" alt=\"");
				                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
				                    	_xhtml_out.println("\"/></a>");
		                    		}
			                    	if(!"OK".equals(_job.get("status"))) {
		                    			_xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
	                                    _xhtml_out.print(BackupJobs.RESTARTJOB);
	                                    _xhtml_out.print("&nameJob=");
	                                    _xhtml_out.print(_job.get("name"));
	                                    if (vmwareJobs != null && !vmwareJobs.isEmpty() && vmwareJobs.containsKey(_job.get("name"))) {
											Map<String, String> vmwareJob = vmwareJobs.get(_job.get("name"));
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(vmwareJob.get("client"));
											_xhtml_out.print("&clientId=0");
										} else {
											_xhtml_out.print("&clientName=");
											_xhtml_out.print(_job.get("client"));
											_xhtml_out.print("&clientId=");
											_xhtml_out.print(_job.get("clientid"));
										}
	                                    _xhtml_out.print("&jobId=");
	                                    _xhtml_out.print(_job.get("id"));
	                                    _xhtml_out.print("\"><img src=\"/images/control_restart_16.png\" title=\"");
					                	_xhtml_out.print(getLanguageMessage("common.message.restart"));
					                	_xhtml_out.print("\" alt=\"");
					                	_xhtml_out.print(getLanguageMessage("common.message.restart"));
					                	_xhtml_out.println("\"/></a>");
									}
									_xhtml_out.print("<a href='javascript:viewLog(\""+_job.get("id")+"\",\""+_job.get("clientid")+"\");' >");
			                    	_xhtml_out.print("<img src=\"/images/book_16.png\" title=\"");
				                	_xhtml_out.print(getLanguageMessage("common.message.view"));
				                	_xhtml_out.print("\" alt=\"");
				                	_xhtml_out.print(getLanguageMessage("common.message.view"));
				                	_xhtml_out.println("\"/></a>");
								}
								_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("backup.summary.message.no_terminated_jobs"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
			    		_xhtml_out.println("<div id=\"viewLogDialog\" name=\"viewLogDialog\" style=\"font-size:12px;\"></div>");
				    	_xhtml_out.print("<input type=\"hidden\" name=\"idJobLog\" id=\"idJobLog\" value=\"\">");
				    	_xhtml_out.print("<input type=\"hidden\" name=\"idJobLogClient\" id=\"idJobLogClient\" value=\"\">");
			         	pageJS+="var wHeight = $(window).height();\n";
				    	pageJS+="var dHeight = wHeight * 0.8;\n";
				    	pageJS+="var wWidth = $(window).width();\n";
				    	pageJS+="var dWidth = wWidth * 0.9;\n";
			         	pageJS+="$( '#viewLogDialog' ).dialog({\n";
			         	pageJS+="   autoOpen: false,\n";
			         	pageJS+="   height: dHeight,\n";
			         	pageJS+="	modal: true,\n";
			         	pageJS+="   width: dWidth,\n";
			         	pageJS+="   hide: 'fade',\n";
			        	pageJS+="   buttons: {\n";
			        	pageJS+="		'"+getLanguageMessage("common.message.refresh")+"': function(event) {\n";
			        	pageJS+="					$('#viewLogDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n";
			        	pageJS+="					viewLog($('#idJobLog').val(), $('#idJobLogClient').val());\n";
			        	pageJS+="				}\n";
			        	pageJS+="	}\n";
			         	pageJS+="});\n";
			         	pageJSFuncs+=getJSViewJobLog();
					}
        		    break;
	    	}
 		} catch (Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
	
	public String removeSpaces(String s) {
		StringTokenizer st = new StringTokenizer(s," ",false);
		String t="";
		while(st.hasMoreElements()) t += st.nextElement();
		return t;
	}
	
 	public String getJSViewJobLog() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function viewLog(jobId, clientId) {\n");
		_sb.append("	$('#idJobLog').val(jobId);\n");
		_sb.append("	$('#idJobLogClient').val(clientId);\n");
		_sb.append("	$('#viewLogDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n");
		_sb.append("	$('#viewLogDialog').dialog( 'open' );\n");
		_sb.append("	$.ajax({\n");
	 	_sb.append("		url: '/admin/BackupLog',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: {jobId : jobId, clientId: clientId}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#viewLogDialog').html(html);\n");
	 	_sb.append("	});\n");
	 	_sb.append("};");
	 	return _sb.toString();
	}
}