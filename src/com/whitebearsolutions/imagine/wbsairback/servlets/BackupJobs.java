package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StorageInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ScheduleManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.disk.SCSIManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.imagine.wbsairback.util.ObjectLock;
import com.whitebearsolutions.imagine.wbsairback.virtual.HypervisorManager;
import com.whitebearsolutions.util.Configuration;

public class BackupJobs extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int CREATEJOB = 2;
	public final static int EDITJOB = 3;
	public final static int SAVEJOB = 4;
	public final static int LAUNCHJOB = 5;
	public final static int DELETEJOB = 6;
	public final static int REMOVEJOB = 7;
	public final static int CANCELJOB = 8;
	public final static int PRUNEJOBS = 9;
	public final static int JOBVOLUMES = 10;
	public final static int STOPJOB = 11;
	public final static int RESTARTJOB = 12;
	public final static int VIEWPROCESSSCRIPT = 13;
	public static final int JOB_CLIENT_LIST_JSON = 365245122;
	private int type;
	Map<String, String> selectTypeStep = null;
	public final static String baseUrl = "/admin/"+BackupJobs.class.getSimpleName();
	
	//private final static Logger logger = LoggerFactory.getLogger(BackupJobs.class);
	
	@SuppressWarnings("unchecked")
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
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
			PrintWriter _xhtml_out = response.getWriter();
	
		    Configuration _c = this.sessionManager.getConfiguration();
		    JobManager _jm = new JobManager(_c);
		    PoolManager _pm = new PoolManager(_c);
		    StorageManager _sm = new StorageManager(_c);
		    ClientManager _cm = new ClientManager(_c);

			boolean vmwareClient = false;
			String clientName = null, clientRealName = null;
	    	int clientId = 0;
	    	int clientIdReal = 0;
	    	if (type != JOBVOLUMES) {
				if (request.getParameter("clientId") != null && (((String)request.getParameter("clientId")).equals("--") || ((String)request.getParameter("clientId")).equals("0")) ) {
					vmwareClient = true;
					if (request.getParameter("clientName") == null || request.getParameter("clientName").isEmpty())
		    			throw new Exception(getLanguageMessage("common.message.no_client"));
					clientName = request.getParameter("clientName");
		    		clientId = 0;
		    		clientIdReal = _cm.getClientId("airback-fd");
		    		clientRealName = "airback-fd";
				} else {
					try {
						clientId = Integer.parseInt(request.getParameter("clientId"));
						clientIdReal = clientId;
					} catch(NumberFormatException _ex) {
						throw new Exception(getLanguageMessage("common.message.no_client"));
					}
					clientName = _cm.getClientName(clientId);
					clientRealName = clientName;
				}
	    	}
		    
			if (this.type == VIEWPROCESSSCRIPT && request.getParameter("name")!=null && !request.getParameter("name").isEmpty()){
				getGroupJobListJobs(_xhtml_out, request, clientName);
				return;
			} 
			
		    fillSelects();
		    if (this.type == JOB_CLIENT_LIST_JSON)
		    {
			    try {
					Integer page = null;
					String qtype = null;
					String query = null;
					Integer rp = null;
					String sortname = null;
					String sortorder = null;
					try {
						page = Integer.parseInt(request.getParameter("page"));
					} catch (Exception ex) {}
					try {
						qtype = request.getParameter("qtype").toString();
			    	} catch (Exception ex) {}
					try {
						query = request.getParameter("query").toString();
					} catch (Exception ex) {}
					try {
						rp = Integer.parseInt(request.getParameter("rp"));
					} catch (Exception ex) {}
					try {
						sortname = request.getParameter("sortname").toString();
					} catch (Exception ex) {}
					try {
						sortorder = request.getParameter("sortorder").toString();
					} catch (Exception ex) {}
					
					List<Map<String, String>> jobs = _jm.getArchivedClientJobsFlexGrid(clientId, clientName, page, rp, sortname, sortorder, query, qtype);
					Integer total = _jm.countTotalArchivedClientJobs(clientId, clientName);
					String jsonJobs = toJSON(jobs, page, total, clientId, clientIdReal, clientName);
					PrintWriter out = response.getWriter();
					out.flush();
					out.print(jsonJobs);
					return;
			    } catch (Exception ex) {
			    	return;
			    }
		    }
			
			
			response.setContentType("text/html");
		    
			switch(this.type) {
	    		default: {
	    				writeDocumentHeader();
	    				int _offset = 0;
		    			List<Map<String, String>> programmed_jobs = null;

		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
			    		
			    		if (!vmwareClient)
			    			programmed_jobs = _jm.getProgrammedClientJobs(clientId);
		    			else
		    				programmed_jobs = _jm.getProgrammedVmwareClientJobs(clientName);
		    			
		    			writeDocumentBack("/admin/BackupClients");
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"jobs\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + DELETEJOB + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"clientId\" value=\"" + clientId + "\"/>");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"clientName\" value=\"" + clientName + "\"/>");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"jobsIds\" id=\"jobsIds\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/cog_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.jobs.info"));
		    			_xhtml_out.println("</div>");
	                    
	                	_xhtml_out.println("<div style=\"margin:20px auto;width:94%;clear:both;\">");
	                    _xhtml_out.println("<table id=\"flexigridJobs"+clientId+"\" style=\"margin-left:0px;margin-right:0px;\"></table>");
	                    _xhtml_out.println("</div>");
	                    
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</form>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.jobs.programed"));
	            		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
						_xhtml_out.print("<a href=\""+baseUrl+"?type=");
						_xhtml_out.print(CREATEJOB);
						_xhtml_out.print("&clientName=");
						_xhtml_out.print(clientName);
						_xhtml_out.print("&clientId=");
						_xhtml_out.print(clientId);
						_xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!programmed_jobs.isEmpty()) {
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
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.schedule"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.fileset"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.default_pool"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.status"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> job : programmed_jobs) {
		                    	String _fileSet=job.get("fileset");
				    			if (_fileSet==null || _fileSet.equals("advancedEmptyFileSet")){
				    				continue;
				    			}
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(job.get("name"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(getLanguageMessage("backup.jobs."+job.get("type")));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(getLanguageMessage("backup.jobs."+job.get("level")));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	if (job.get("schedule") != null && !job.get("schedule").equals("") && ScheduleManager.getTypeSchedule(job.get("schedule")) != null) {
			                    	if (ScheduleManager.getTypeSchedule(job.get("schedule")).equals("weekly"))
			                    		_xhtml_out.print("<a href=\"/admin/BackupScheduleWeekly?type="+BackupScheduleWeekly.SCHEDULE_EDIT+"&name="+job.get("schedule")+"\">");
			                    	else if (ScheduleManager.getTypeSchedule(job.get("schedule")).equals("monthly"))
			                    		_xhtml_out.print("<a href=\"/admin/BackupScheduleMonthly?type="+BackupScheduleMonthly.SCHEDULE_EDIT+"&name="+job.get("schedule")+"\">");
			                    	else if (ScheduleManager.getTypeSchedule(job.get("schedule")).equals("daily"))
			                    		_xhtml_out.print("<a href=\"/admin/BackupScheduleDaily?type="+BackupScheduleDaily.SCHEDULE_EDIT+"&name="+job.get("schedule")+"\">");
			                    	else if (ScheduleManager.getTypeSchedule(job.get("schedule")).equals("yearly"))
			                    		_xhtml_out.print("<a href=\"/admin/BackupScheduleYearly?type="+BackupScheduleYearly.SCHEDULE_EDIT+"&name="+job.get("schedule")+"\">");
		                    	}
		                    	_xhtml_out.print(job.get("schedule"));
		                    	if (job.get("schedule") != null && !job.get("schedule").equals("") && ScheduleManager.getTypeSchedule(job.get("schedule")) != null)
		                    		_xhtml_out.print("</a>");
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	if (FileSetManager.getFileSet(job.get("fileset")) != null && FileSetManager.getFileSet(job.get("fileset")).get("remote_storage") != null && FileSetManager.getFileSet(job.get("fileset")).get("remote_storage").equals("yes")) 
		                    		_xhtml_out.print(job.get("fileset"));
		                    	else {
		                    		if (job.get("fileset") != null && !job.get("fileset").equals("")) {
		                    			if (!"advancedEmptyFileSet".equals(job.get("fileset"))) {
				                    		if (clientName.equals("airback-fd"))
				                    			_xhtml_out.print("<a href=\"/admin/BackupFilesetsLocal?type="+BackupFilesetsLocal.FILESET_LOCAL_EDIT+"&name="+job.get("fileset")+"\">");
				                    		else
				                    			_xhtml_out.print("<a href=\"/admin/BackupFilesetsClients?type="+BackupFilesetsClients.FILESET_EDIT+"&name="+job.get("fileset")+"\">");
				                    		_xhtml_out.print(job.get("fileset"));
		                    			}
			                    		_xhtml_out.print("</a>");
		                    		}
		                    	}
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(job.get("pool"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(getLanguageMessage("backup.jobs."+job.get("enabled")));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print("<a href=\""+baseUrl+"?type=");
								_xhtml_out.print(LAUNCHJOB);
								_xhtml_out.print("&jobName=");
								_xhtml_out.print(job.get("name"));
								_xhtml_out.print("&clientName=");
								_xhtml_out.print(clientName);
								_xhtml_out.print("&clientId=");
								_xhtml_out.print(clientId);
								_xhtml_out.print("\"><img src=\"/images/control_start_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\""+baseUrl+"?type=");
								_xhtml_out.print(EDITJOB);
								_xhtml_out.print("&jobName=");
								_xhtml_out.print(job.get("name"));
								_xhtml_out.print("&clientName=");
								_xhtml_out.print(clientName);
								_xhtml_out.print("&clientId=");
								_xhtml_out.print(clientId);
								_xhtml_out.print("\"><img src=\"/images/cog_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\""+baseUrl+"?type=");
								_xhtml_out.print(REMOVEJOB);
								_xhtml_out.print("&jobName=");
								_xhtml_out.print(job.get("name"));
								_xhtml_out.print("&clientName=");
								_xhtml_out.print(clientName);
								_xhtml_out.print("&clientId=");
								_xhtml_out.print(clientId);
								_xhtml_out.print("\"><img src=\"/images/cog_delete_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.println("\"></a>");
								
								_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
								_offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_planned_jobs"));
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
	        	    	_xhtml_out.println("<div id=\"viewVolumesDialog\" name=\"viewVolumesDialog\" style=\"font-size:12px;\"></div>");
	        	    	_xhtml_out.print("<input type=\"hidden\" name=\"idJobVolumes\" id=\"idJobVolumes\" value=\"\">");

	                 	
	        	    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/flexigrid.js\"></script>");
	        	    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.cookie.js\"></script>");
	        	    	pageJS+="$(\"#flexigridJobs"+clientId+"\").flexigrid({\n";
	        	    	pageJS+="	url: '"+baseUrl+"?type="+BackupJobs.JOB_CLIENT_LIST_JSON+"&clientId="+clientId+"&clientName="+clientName+"',\n";
	        	    	pageJS+="dataType: 'json',\n";
	        	    	pageJS+="colModel : [\n";
	        	    	pageJS+="{display: ' ', name : 'id', align: 'left', width : 25, sortable : true},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.name")+"', name : 'name', width : 108, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.type")+"', name : 'type', width : 25, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.level")+"', name : 'level', width : 25, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.status")+"', name : 'status', width : 145, sortable : false, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.start_date")+"', name : 'startdate', width : 96, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.end_date")+"', name : 'enddate', width : 96, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.total_files")+"', name : 'totalfiles', width : 55, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.total_size")+"', name : 'totalsize', width : 130, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: ' ', name : 'actions', width : 115, sortable : false, align: 'left'}\n";
	        	    	pageJS+="],\n";
	        	    	pageJS+="buttons : [\n";
	        	    	pageJS+="{name: '   "+getLanguageMessage("backup.jobs.select_all")+"', onpress : invertSelection, bclass : 'btnSelect'},\n";
	        	    	pageJS+="{name: '   "+getLanguageMessage("backup.jobs.prune")+"', onpress : pruneJobs, bclass : 'btnPrune'},\n";
	        	    	pageJS+="{name: '   "+getLanguageMessage("common.message.delete")+"', onpress : deleteJobs, bclass : 'btnDelete'}\n";
	        	    	pageJS+="],\n";
	        	    	pageJS+="searchitems : [\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.name")+"', name : 'name'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.type")+"', name : 'type'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.level")+"', name : 'level'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.date")+"', name : 'startdate'}\n";
	        	    	pageJS+="],\n";
	        	    	pageJS+="procmsg : '"+getLanguageMessage("backup.jobs.grid.loading")+"',\n";
	        	    	pageJS+="errormsg : '"+getLanguageMessage("backup.jobs.grid.error")+"',\n";
	        	    	pageJS+="tableId: 'flexigridJobs"+clientId+"',\n";
	        	    	pageJS+="sortname: 'startdate',\n";
	        	    	pageJS+="sortorder: 'desc',\n";
	        	    	pageJS+="usepager: true,\n";
	        	    	//pageJS+="colResize: false,\n";
	        	    	pageJS+="title: '"+getLanguageMessage("backup.jobs.executed")+"',\n";
	        	    	pageJS+="useRp: true,\n";
	        	    	pageJS+="onSuccess: checkColors,\n";
	        	    	pageJS+="rp: 10,\n";
	        	    	pageJS+="showTableToggleBtn: false,\n";
	        	    	pageJS+="findtext: '"+getLanguageMessage("common.message.search")+"',\n";
	        	    	pageJS+="width: 'auto',\n";
	        	    	pageJS+="height: 430\n";
	        	    	pageJS+="});\n";
	        	    	pageJSFuncs+="function deleteJobs() {\n";
	        	    	pageJSFuncs+="		var grid = $('#flexigridJobs"+clientId+"');\n";
	        	    	pageJSFuncs+="		var jobIds = [];\n";
	        	    	pageJSFuncs+="		$('.trSelected', grid).each(function() {\n";
	        	    	pageJSFuncs+="			var id = $(this).attr('id');\n";
	        	    	pageJSFuncs+="			id = id.substring(id.lastIndexOf('row')+3);\n";
	        	    	pageJSFuncs+="			jobIds.push(id)\n";
	        	    	pageJSFuncs+="		});\n";
	        	    	pageJSFuncs+="		var iJobs = document.getElementById('jobsIds');\n";
	        	    	pageJSFuncs+="		for (var i=0; i<jobIds.length;i++) {\n";
	        	    	pageJSFuncs+="			if (i == 0)\n";
	        	    	pageJSFuncs+="				iJobs.value=jobIds[i];\n";
	        	    	pageJSFuncs+="			else\n";
	        	    	pageJSFuncs+="				iJobs.value+='-'+jobIds[i];\n";
	        	    	pageJSFuncs+="		}\n";
	        	    	pageJSFuncs+="		document.jobs.submit();\n";
	        	    	pageJSFuncs+="}\n";
	        	    	pageJSFuncs+="function invertSelection() {\n";
	        	    	pageJSFuncs+="	var rows = $('table#flexigridJobs"+clientId+"').find('tr').get();\n";
	        	    	pageJSFuncs+="	$.each(rows,function(i,n) {\n";
	        	    	pageJSFuncs+="		$(n).toggleClass('trSelected');\n";
	        	    	pageJSFuncs+="	});\n";
	        	    	pageJSFuncs+="}\n";
	        	    	pageJSFuncs+="function pruneJobs() {\n";
	        	    	pageJSFuncs+="	document.location.href='"+baseUrl+"?type=" + PRUNEJOBS + "&clientId=" + clientId + "&clientName="+clientName+"'\n";
	        	    	pageJSFuncs+="}\n";
	        	    	pageJSFuncs+=getRowColorCheckingJS(clientId);
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
	                	pageJS+="					viewLog($('#idJobLog').val(), "+clientIdReal+");\n";
	                	pageJS+="				}\n";
	                	pageJS+="	}\n";
	                 	pageJS+="});\n";
	                 	pageJS+="$( '#viewVolumesDialog' ).dialog({\n";
	                 	pageJS+="   autoOpen: false,\n";
	                 	pageJS+="   height: dHeight,\n";
	                 	pageJS+="	modal: true,\n";
	                 	pageJS+="   width: dWidth,\n";
	                 	pageJS+="   hide: 'fade',\n";
	                	pageJS+="   buttons: {\n";
	                	pageJS+="		'"+getLanguageMessage("common.message.refresh")+"': function(event) {\n";
	                	pageJS+="					$('#viewVolumesDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n";
	                	pageJS+="					viewVolumes($('#idJobVolumes').val(), "+clientIdReal+");\n";
	                	pageJS+="				}\n";
	                	pageJS+="	}\n";
	                 	pageJS+="});\n";
	                 	pageJSFuncs+=getJSViewJobLog();
	                 	pageJSFuncs+=getJSViewJobVolumes();
					}
        		    break;
	    		case CREATEJOB: {
	    				writeDocumentHeader();
	    				_xhtml_out.print(HtmlFormUtils.printJsApprise());
		    			
		    			List<String> jobs = _jm.getAllProgrammedJobs();
		    			if (jobs != null && !jobs.isEmpty())
		    				Collections.sort(jobs);
		    			List<String> hypervisor_jobs = HypervisorManager.getInstance("").getAllHypervisorJobNames();
		    			if (hypervisor_jobs != null && !hypervisor_jobs.isEmpty())
		    				Collections.sort(hypervisor_jobs);
		    			List<String> pools = _pm.getPoolNames();
		    			
		    		    writeDocumentBack(""+baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    		    _xhtml_out.println("<form action=\""+baseUrl+"\" name=\"jobs\" id=\"jobs\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" id=\"type\" value=\"" + SAVEJOB + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"clientId\" value=\"" + clientId + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"clientName\" value=\"" + clientName + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"opType\" value=\"add\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"variableCount\" id=\"variableCount\" value=\"0\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"numMax\" id=\"numMax\" value=\"0\"/>");
	    			    _xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.alerts.js\"></script>");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/cog_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.jobs.info"));
		    			_xhtml_out.println("</div>");
			    		
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.jobs.new_job"));
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.jobs.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"job_type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"job_type\" id=\"job_type\">");
	        	    	_xhtml_out.println("<option value=\"backup\">Backup</option>");
	  	        	   	_xhtml_out.println("<option value=\"copy\">");
	  	        	   	_xhtml_out.print(getLanguageMessage("backup.jobs.copy"));
	  	        	    _xhtml_out.println("</option>");
	        	    	_xhtml_out.println("<option value=\"migrate\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.migrate"));
	  	        	    _xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"level\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_level"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"level\">");
	        	    	_xhtml_out.print("<option value=\"Full\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_full"));
	        	    	_xhtml_out.println("</option>");
	        	    	_xhtml_out.println("<option value=\"Incremental\" default>");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_incremental"));
	        	    	_xhtml_out.println("</option>");
						_xhtml_out.println("<option value=\"Differential\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_differential"));
	        	    	_xhtml_out.println("</option>");
	        	    	_xhtml_out.print("<option value=\"VirtualFull\">");
                        _xhtml_out.print(getLanguageMessage("backup.schedule.level_virtual"));
                        _xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"storage\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_storage"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"storage\">");
	        	    	for(String storage : _sm.getAvailableStorages()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(storage);
							_xhtml_out.print("\">");
							_xhtml_out.print(storage);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"schedule\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.schedule"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"schedule\">");
						for(String schedule : ScheduleManager.getScheduleNames()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(schedule);
							_xhtml_out.print("\">");
							_xhtml_out.print(schedule);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"fileset\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.fileset"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"fileset\" id=\"fileset\">");
	        	    	if("airback-fd".equals(clientName)) {
	        	    		for(String fileset: FileSetManager.getAllLocalFileSetNames()) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset);
								_xhtml_out.print("\">");
								_xhtml_out.print(fileset);
								_xhtml_out.println("</option>");
							}
	        	    		for(String fileset: FileSetManager.getAllNDMPFileSetNames()) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset);
								_xhtml_out.print("\">");
								_xhtml_out.print(fileset);
								_xhtml_out.println("</option>");
							}
	        	    	} else if (vmwareClient) {
	        	    		for(Map<String, String> fileset: FileSetManager.getVmwareFilesetsOfClient(clientName)) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.print("\">");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.println("</option>");
							}
	        	    	} else {
	        	    		for(String fileset: FileSetManager.getAllFileSetNames()) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset);
								_xhtml_out.print("\">");
								_xhtml_out.print(fileset);
								_xhtml_out.println("</option>");
							}
	        	    	}
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolDefault\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolDefault\">");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolFull\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.full_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolFull\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolDiff\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.differential_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolDiff\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolIncre\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.incremental_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolIncre\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"nextJob\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.nextJob"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"nextJob\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String _job : jobs) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_job);
							_xhtml_out.print("\">");
							_xhtml_out.print(_job);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"verifyPreviousJob\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.verifyPreviousJob"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"verifyPreviousJob\">");
						_xhtml_out.print("<option value=\"yes\">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	if("airback-fd".equals(clientName)) {
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"hypervisorJob\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"hypervisorJob\">");
		        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	for(String _hjob : hypervisor_jobs) {
		        	    		_xhtml_out.print("<option value=\"");
								_xhtml_out.print(_hjob);
								_xhtml_out.print("\">");
								_xhtml_out.print(_hjob);
								_xhtml_out.println("</option>");
							}
							_xhtml_out.println("</select>");
							_xhtml_out.println("</div>");
	        	    	}
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"maxStartDelay\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.delay_time"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"maxStartDelay\" value=\"10\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"maxRunTime\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.run_time"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"maxRunTime\" value=\"72\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"maxWaitTime\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.wait_time"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"maxWaitTime\" value=\"10\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"rescheduleOnError\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.rescheduleOnError"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"rescheduleOnError\" onChange=\"checkReschedule();\">");
						_xhtml_out.print("<option value=\"yes\">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\" selected = \"selected\">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"rescheduleInterval\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.rescheduleInterval"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"rescheduleInterval\" value=\"2\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"rescheduleTimes\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.rescheduleTimes"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"rescheduleTimes\" value=\"12\"/> ");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"accurate\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.accurate"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"accurate\">");
						_xhtml_out.print("<option value=\"yes\">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\" selected=\"selected\">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"spoolData\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.disk_spooling"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"spoolData\">");
						_xhtml_out.print("<option value=\"yes\" selected=\"selected\">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"bandwith\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.bandwith"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"bandwith\"/> Mb/s</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"priority\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.priority"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"priority\">");
						for(int p = 10; p > 0; p--) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(p);
							_xhtml_out.print("\">");
							_xhtml_out.print(p);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"enabled\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.enabled"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"enabled\">");
						_xhtml_out.print("<option value=\"yes\" selected=\"selected\">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	if(!"airback-fd".equals(clientName) && !vmwareClient) {
		        	    	_xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("backup.jobs.scripts"));
		            		_xhtml_out.print("<a href=\"javascript:submitForm(document.jobs.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.save"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.save"));
		                	_xhtml_out.println("\"/></a>");

		                	_xhtml_out.println(HtmlFormUtils.addHeaderButtonSubmit( -1, getLanguageMessage("advanced.template.job.addstep"),"noSubmitForm","addScriptProcesss();"));
		                    _xhtml_out.println("</h2>");
		                    
		                    _xhtml_out.println("<fieldset style=\"width: 100%;\">");
		            		_xhtml_out.print(HtmlFormUtils.selectOptionObj("processScript", getLanguageMessage("advanced.template.job.typeStep"), null, selectTypeStep, false,"conChangePScript(this.value);"));
		                	_xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div id=\"dynamicForm\">");
			    			_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<table id=\"tableProcessScript\" class=\"displayNone\">");
		        	    	_xhtml_out.println("<tbody id=\"scriptTable\">");
		        	    	_xhtml_out.println("<tr>");
		        	    	_xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.name"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.when"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.success"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.fail"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.variables"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.println("</td>");
			                _xhtml_out.println("</tr>");
		        	    	_xhtml_out.println("</tbody>");
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
		                    _xhtml_out.println("<div id=\"viewJobsDialog\" name=\"viewJobsDialog\"></div>");
		                    
	        	    	}
	        	    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.cookie.js\"></script>");
	        	    	ScriptProcessManager _scm = new ScriptProcessManager();
		    	    	Map<String, Map<String, Object>> _script = _scm.listScript();
		    	    	createFunctionVariablePS(_script,type);
		    	    	addScriptProcess(_script, request.getParameter("jobName"));
		    	    	deleteScriptProcess();
		    	    	getJSViewJobsDialog(clientId);
	                 	pageJS+="$('#viewJobsDialog').dialog({\n";
	                 	pageJS+="   autoOpen: false,\n";
	                 	pageJS+="   height: 'auto',\n";
	                 	pageJS+="	modal: true,\n";
	                 	pageJS+="   width: 1000,\n";
	                 	pageJS+="   hide: 'fade'\n";
	                 	pageJS+="});\n";
	        	    	pageJSFuncs+="function checkReschedule() {";
	        	    	pageJSFuncs+="	var rescheduleOnError = document.jobs.rescheduleOnError.value;";
	        	    	pageJSFuncs+="	var rescheduleInterval = document.jobs.rescheduleInterval;";
	        	    	pageJSFuncs+="	var rescheduleTimes = document.jobs.rescheduleTimes;";
	        	    	pageJSFuncs+="	if (rescheduleOnError == 'yes') {";
	        	    	pageJSFuncs+="		rescheduleInterval.removeAttribute('disabled');";
	        	    	pageJSFuncs+="		rescheduleTimes.removeAttribute('disabled');";
	        	    	pageJSFuncs+="	} else {";
	        	    	pageJSFuncs+="		rescheduleInterval.setAttribute('disabled','disabled');";
	        	    	pageJSFuncs+="		rescheduleTimes.setAttribute('disabled','disabled');";
	        	    	pageJSFuncs+="	}";
	        	    	pageJSFuncs+="}";
	        	    	pageJS+="checkReschedule();";
	        	    	
	        	    	_xhtml_out.println("</form>");
	    			}
	    			break;
	    		case EDITJOB: {
	    				writeDocumentHeader();
	    				_xhtml_out.print(HtmlFormUtils.printJsApprise());
		    			
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			if(request.getParameter("jobName") == null || request.getParameter("jobName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.name"));
		    			}
		    			
		    			Map<String, String> job = _jm.getProgrammedJob(request.getParameter("jobName"));
		    			if (job == null || job.isEmpty())
		    				throw new Exception(getLanguageMessage("backup.jobs.programed.not.exists"));
		    			
		    			String _fileSet=job.get("fileset");
		    			if (_fileSet==null || _fileSet.equals("advancedEmptyFileSet")){
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.no_editable"));
		    			}
		    			
		    			List<String> jobs = _jm.getAllProgrammedJobs();
		    			if (jobs != null && !jobs.isEmpty())
		    				Collections.sort(jobs);
		    			List<String> hypervisor_jobs = HypervisorManager.getInstance("").getAllHypervisorJobNames();
		    			if (hypervisor_jobs != null && !hypervisor_jobs.isEmpty())
		    				Collections.sort(hypervisor_jobs);
		    			List<String> pools = _pm.getPoolNames();
		    			
		    			List<Map<String, Object>> _scripts = JobManager.getJobProcessScripts(request.getParameter("jobName"), clientName);
		    			
		    			int _numMax=0;
		    			for (Map<String, Object> _scriptItem: _scripts){
		    				if (_scriptItem.get("name")!=null){
		    					String _nameScript=(String)_scriptItem.get("name");
		    					if (_nameScript.contains("--")){
			    					try{
			    						int _x=Integer.valueOf(_nameScript.substring(_nameScript.indexOf("--")+2));
			    						if (_x>_numMax){
			    							_numMax=_x;
			    						}
			    					}catch(Exception _ex){}
			    					
		    					}
		    				}
		    			}
		    			int _add_script = 1;
		    			if(request.getParameter("add-script") != null) {
	                    	try {
	                    		_add_script = Integer.parseInt(request.getParameter("add-script"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			} else if(_scripts.size() > 0) {
		    				_add_script = _scripts.size();
		    			}
		    			
		    			writeDocumentBack(baseUrl+"?clientId=" + clientId+"&clientName="+clientName);
		    			_xhtml_out.println("<form action=\""+baseUrl+"\" name=\"jobs\" id=\"jobs\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SAVEJOB + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"clientId\" value=\"" + clientId + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"clientName\" value=\"" + clientName + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + job.get("name") + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"job_type\" value=\"" + job.get("type").toLowerCase() + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"opType\" value=\"update\"/>");
	     			    _xhtml_out.println("<input type=\"hidden\" name=\"variableCount\" id=\"variableCount\" value=\""+_scripts.size()+"\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"numMax\" id=\"numMax\" value=\""+(_numMax+1)+"\"/>");
	    			    _xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.alerts.js\"></script>");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/cog_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.jobs.info"));
		    			_xhtml_out.println("</div>");
			    		
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(job.get("name"));
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.jobs.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"_name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    	_xhtml_out.print(job.get("name"));
	        	    	_xhtml_out.print("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    _xhtml_out.print("<label for=\"_job_type\">");
		        	    _xhtml_out.print(getLanguageMessage("backup.jobs.type"));
		        	    _xhtml_out.println(": </label>");
		        	    _xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_job_type\" value=\"");
		        	    _xhtml_out.print(job.get("type"));
		        	    _xhtml_out.print("\" disabled=\"disabled\"/>");
		        	    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    _xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"level\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_level"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"level\">");
	        	    	_xhtml_out.print("<option value=\"Full\"");
						if("Full".equals(job.get("level"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.print(">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_full"));
	        	    	_xhtml_out.println("</option>");
	        	    	_xhtml_out.print("<option value=\"Incremental\"");
						if("Incremental".equals(job.get("level"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.print(">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_incremental"));
	        	    	_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"Differential\"");
						if("Differential".equals(job.get("level"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.print(">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.schedule.level_differential"));
	        	    	_xhtml_out.println("</option>");
	        	    	_xhtml_out.print("<option value=\"VirtualFull\"");
                        if("VirtualFull".equals(job.get("level"))) {
                                _xhtml_out.print(" selected=\"selected\"");
                        }
                        _xhtml_out.print(">");
				        _xhtml_out.print(getLanguageMessage("backup.schedule.level_virtual"));
				        _xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"storage\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_storage"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"storage\">");
	        	    	for(String storage : _sm.getAvailableStorages()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(storage);
							_xhtml_out.print("\"");
							if(storage.equals(job.get("storage"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(storage);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"schedule\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.schedule"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"schedule\">");
						for(String schedule : ScheduleManager.getScheduleNames()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(schedule);
							_xhtml_out.print("\"");
							if(schedule.equals(job.get("schedule"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(schedule);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"fileset\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.fileset"));
	        	    	_xhtml_out.println(": </label>");	        	    	
	        	    	_xhtml_out.println("<select class=\"form_select\" ");
	        	    	if (_fileSet!=null && _fileSet.contains("---hidden")){
	        	    		_xhtml_out.println(" disabled=\"disabled\" ");
	        	    	}
	        	    	_xhtml_out.println("\" name=\"fileset\" id=\"fileset\">");
	        	    	if("airback-fd".equals(clientName)) {
	        	    		for(String fileset: FileSetManager.getAllLocalFileSetNames()) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset);
								_xhtml_out.print("\"");
								if(fileset.equals(job.get("fileset"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(fileset);
								_xhtml_out.println("</option>");
							}
	        	    		for(String fileset: FileSetManager.getAllNDMPFileSetNames()) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset);
								_xhtml_out.print("\"");
								if(fileset.equals(job.get("fileset"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(fileset);
								_xhtml_out.println("</option>");
							}
	        	    	} else if (vmwareClient) {
	        	    		for(Map<String, String> fileset: FileSetManager.getVmwareFilesetsOfClient(clientName)) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.print("\"");
								if(fileset.get("name").equals(job.get("fileset"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(fileset.get("name"));
								_xhtml_out.println("</option>");
							}
	        	    	} else {
	        	    		for(String fileset: FileSetManager.getAllFileSetNames()) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(fileset);
								_xhtml_out.print("\"");
								if(fileset.equals(job.get("fileset"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(fileset);
								_xhtml_out.println("</option>");
							}
	        	    	}
	        	    	
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolDefault\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.default_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolDefault\">");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
							if(pool.equals(job.get("pool"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolFull\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.full_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolFull\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
							if(pool.equals(job.get("pool-full"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolDiff\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.differential_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolDiff\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
							if(pool.equals(job.get("pool-differential"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"poolIncre\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.incremental_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"poolIncre\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : pools) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\"");
							if(pool.equals(job.get("pool-incremental"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"nextJob\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.nextJob"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"nextJob\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String _job : jobs) {
	        	    		if(_job.equals(job.get("name"))) {
	        	    			continue;
	        	    		}
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_job);
							_xhtml_out.print("\"");
							if(job.get("nextJob") != null && _job.equals(job.get("nextJob"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_job);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"verifyPreviousJob\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.verifyPreviousJob"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"verifyPreviousJob\">");
						_xhtml_out.print("<option value=\"yes\"");
						if(job.get("verifyPreviousJob") != null && "yes".contains(job.get("verifyPreviousJob"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if(job.get("verifyPreviousJob") == null || "no".contains(job.get("verifyPreviousJob"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
	        	    	if("airback-fd".equals(clientName)) {
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"hypervisorJob\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"hypervisorJob\">");
		        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	for(String _hjob : hypervisor_jobs) {
		        	    		_xhtml_out.print("<option value=\"");
								_xhtml_out.print(_hjob);
								_xhtml_out.print("\"");
								if(job.get("hypervisorJob") != null && _hjob.equals(job.get("hypervisorJob"))) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.print(">");
								_xhtml_out.print(_hjob);
								_xhtml_out.println("</option>");
							}
							_xhtml_out.println("</select>");
							_xhtml_out.println("</div>");
	        	    	}
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"maxStartDelay\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.delay_time"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"maxStartDelay\" value=\"");
	        	    	_xhtml_out.print(job.get("max-start-delay"));
	        	    	_xhtml_out.print("\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"maxRunTime\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.run_time"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"maxRunTime\" value=\"");
	        	    	_xhtml_out.print(job.get("max-run-time"));
	        	    	_xhtml_out.print("\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"maxWaitTime\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.wait_time"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"maxWaitTime\" value=\"");
	        	    	_xhtml_out.print(job.get("max-wait-time"));
	        	    	_xhtml_out.print("\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"rescheduleOnError\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.rescheduleOnError"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"rescheduleOnError\" onChange=\"checkReschedule();\">");
						_xhtml_out.print("<option value=\"yes\" ");
						if(job.get("reschedule-on-error") != null && "yes".contains(job.get("reschedule-on-error"))) {
							_xhtml_out.print(" selected = \"selected\" ");
						}
						_xhtml_out.println(" >");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\" ");
						if(job.get("reschedule-on-error") != null && "no".contains(job.get("reschedule-on-error"))) {
							_xhtml_out.print(" selected = \"selected\" ");
						}
						_xhtml_out.println(" >");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"rescheduleInterval\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.rescheduleInterval"));
	        	    	_xhtml_out.println(": </label>");
	        	    	int rescheduleInterval = 2;
	        	    	if (job.get("reschedule-interval") != null && !job.get("reschedule-interval").isEmpty())
	        	    		rescheduleInterval = Integer.parseInt(job.get("reschedule-interval")); 
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"rescheduleInterval\" value=\"");
	        	    	_xhtml_out.print(rescheduleInterval);
	        	    	_xhtml_out.print("\"/> ");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"rescheduleTimes\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.rescheduleTimes"));
	        	    	_xhtml_out.println(": </label>");
	        	    	int rescheduleTimes = 12;
	        	    	if (job.get("reschedule-times") != null && !job.get("reschedule-times").isEmpty())
	        	    		rescheduleTimes = Integer.parseInt(job.get("reschedule-times")); 
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"rescheduleTimes\" value=\"");
	        	    	_xhtml_out.print(rescheduleTimes);
	        	    	_xhtml_out.print("\"/> ");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"accurate\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.accurate"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"accurate\">");
						_xhtml_out.print("<option value=\"yes\"");
						if("yes".contains(job.get("accurate"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if("no".contains(job.get("accurate"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"spoolData\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.disk_spooling"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"spoolData\">");
						_xhtml_out.print("<option value=\"yes\"");
						if("yes".contains(job.get("spooldata"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if("no".contains(job.get("spooldata"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.print(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"bandwith\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.bandwith"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"bandwith\" value=\"");
	        	    	_xhtml_out.print(job.get("bandwith"));
	        	    	_xhtml_out.println("\"/> Mb/s</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"priority\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.priority"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"priority\">");
						for(int p = 10; p > 0; p--) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(p);
							_xhtml_out.print("\"");
							if(String.valueOf(p).equals(job.get("priority"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(p);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"enabled\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.enabled"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"enabled\">");
						_xhtml_out.print("<option value=\"yes\"");
						if("yes".contains(job.get("enabled"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.yes"));
						_xhtml_out.println("</option>");
						_xhtml_out.print("<option value=\"no\"");
						if("no".contains(job.get("enabled"))) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">");
						_xhtml_out.print(getLanguageMessage("common.message.no"));
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");

	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	if(!"airback-fd".equals(clientName)) {
		        	    	_xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("backup.jobs.scripts"));
		            		_xhtml_out.print("<a href=\"javascript:submitForm(document.jobs.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.save"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.save"));
		                	_xhtml_out.println("\"/></a>");

		                	_xhtml_out.println(HtmlFormUtils.addHeaderButtonSubmit( -1, getLanguageMessage("advanced.template.job.addstep"),"noSubmitForm","addScriptProcesss();"));
		                    _xhtml_out.println("</h2>");
		                    
		                    _xhtml_out.println("<fieldset style=\"width: 100%;\">");
		            		_xhtml_out.print(HtmlFormUtils.selectOptionObj("processScript", getLanguageMessage("advanced.template.job.typeStep"), null, selectTypeStep, false,"conChangePScript(this.value);"));
		                	_xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div id=\"dynamicForm\">");
			    			_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<table id=\"tableProcessScript\" ");
		        	    	if (_scripts.size()==0){
		        	    		_xhtml_out.println(" class=\"displayNone\" ");
		        	    	}
		        	    	_xhtml_out.println(">");
		        	    	_xhtml_out.println("<tbody id=\"scriptTable\">");
		        	    	_xhtml_out.println("<tr>");
		        	    	_xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.name"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.when"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.success"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("backup.jobs.scripts.fail"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.variables"));
			                _xhtml_out.println("</td>");
			                _xhtml_out.print("<td class=\"title\">");
			                _xhtml_out.println("</td>");
			                _xhtml_out.println("</tr>");
		        	    	Map<String, Object> _script = null;
		        	    	for(int r = 0; (r < _add_script) || (r < _scripts.size()); r++) {
		        	    		if(r < _scripts.size()) {
		        	    			_script = _scripts.get(r);
		        	    		}
		        	    		if (_scripts.size()>0){
			        	    		_xhtml_out.print("<tr id='itemPS-"+_script.get("name")+"' ");
			        	    		if(r % 2 == 0) {
			        	    			_xhtml_out.print(" class='highlight' ");
			        	    		}
			        	    		_xhtml_out.print(">");
			        	    		_xhtml_out.println("<td>");
			        	    		_xhtml_out.print("<input readonly='true' class='hidden' type='text'  id=\"script_name"+r+"\" name=\"script_name"+r+"\" ");
			        	    		  if(_script != null && _script.get("name") != null) {
					                    	_xhtml_out.print(" value=\"");
					                    	_xhtml_out.print(_script.get("name"));
					                    	_xhtml_out.print("\"");
					                    }
			        	    		
			        	    		  _xhtml_out.print("/> ");
			        	    		_xhtml_out.println("</td>");
			        	    		_xhtml_out.println("<td>");
			        	    		if ( _scripts.get(r).get("before")!=null && "true".equalsIgnoreCase((String)_scripts.get(r).get("before"))){
			        	    			_xhtml_out.println("<input readonly='true' class='hidden' type='hidden' name=\"type"+r+"\" value=\\\""+ScriptProcessManager.BEFORE_EXECUTION+"\"/> ");
			        	    			_xhtml_out.println("<input readonly='true' type='text' value=\""+getLanguageMessage("backup.jobs.scripts.before")+"\"/>");;
			        	    		}else{
			        	    			_xhtml_out.println("<input readonly='true' class='hidden' type='hidden' name=\"type"+r+"\" value=\\\""+ScriptProcessManager.AFTER_EXECUTION+"\"/> ");
			        	    			_xhtml_out.println("<input readonly='true' type='text' value=\""+getLanguageMessage("backup.jobs.scripts.after")+"\"/>");;
			        	    		}
			        	    		if (_script.get("scripts")!=null){
										List<Map<String, String>> _scriptsList= (List<Map<String, String>>) _script.get("scripts");
										for (Map<String, String> _scriptItem: _scriptsList){
											_xhtml_out.println("<input class='hidden' type='hidden' name=\"scriptItem_"+_scriptItem.get("order")+"_"+r+"\" value=\""+_scriptItem.get("content")+"\"/> ");
										}
			        	    		}
			        	    		_xhtml_out.println("</td>");
			        	    		_xhtml_out.println("<td>");
			        	    		if ( _scripts.get(r).get("before")!=null && !"true".equalsIgnoreCase((String)_scripts.get(r).get("before"))){
			        	    			_xhtml_out.print("<input class='form_checkbox' type='checkbox' name=\"script_success"+r+"\" value='true' ");
			        	    			if(_script != null && _script.get("success") != null && "true".equalsIgnoreCase((String)_script.get("success"))) {
			        	    				_xhtml_out.print(" checked=\"checked\"");
			        	    			}
			        	    			_xhtml_out.print("/>");
			        	    		}
				                    
			        	    		_xhtml_out.println("</td>");
			        	    		_xhtml_out.println("<td>");
			        	    		if ( _scripts.get(r).get("before")!=null && !"true".equalsIgnoreCase((String)_scripts.get(r).get("before"))){
			        	    			_xhtml_out.print("<input class='form_checkbox' type='checkbox' name=\"script_fail"+r+"\" value='true' ");
			        	    			if(_script != null && _script.get("fail") != null && "true".equalsIgnoreCase((String)_script.get("fail"))) {
			        	    				_xhtml_out.print(" checked=\"checked\"");
			        	    			}
			        	    			_xhtml_out.print("/>");
			        	    		}
			        	    		_xhtml_out.println("</td>");
			        	    		_xhtml_out.println("<td>");
			        	    		if (_script.get("variables") != null) {
			        	    			Map<String, String> variables = (Map<String, String>) _script.get("variables");
			        	    			if (!variables.isEmpty()) {
			        	    				boolean init = true;
			        	    				for (String var : variables.keySet()) {
			        	    					if (!var.equals(StorageInventoryManager.airback_iqn_nameVar) && !var.equals(StorageInventoryManager.airback_wwn_nameVar)) {
			        	    						if (!init)
			        	    							_xhtml_out.println(",");
			        	    						else
			        	    							init = false;
			        	    						_xhtml_out.println(var);
			        	    						_xhtml_out.println("=");
			        	    						_xhtml_out.println(variables.get(var));
			        	    					}
			        	    				}
			        	    			}
			        	    		}
			        	    		_xhtml_out.println("</td>");
			        	    		_xhtml_out.println("<td>");
			        	    		_xhtml_out.print("<a href='javascript:viewJobs(document.getElementById(\"script_name"+r+"\").value,\"&generated=true&jobName="+request.getParameter("jobName")+"\");'><img src='/images/group_job_16.png' title='");
			        	    		_xhtml_out.print(""+getLanguageMessage("backup.jobs.scripts.view_script")+"");
			        	    		_xhtml_out.print("' alt='");
			        	    		_xhtml_out.print(""+getLanguageMessage("backup.jobs.scripts.view_script")+"");
			        	    		_xhtml_out.print("'/></a>");
			        	    		
			        	    		_xhtml_out.println("<a href='javascript:deleteScriptProcess(document.getElementById(\"script_name"+r+"\").value);'><img alt=\"Eliminar\" title=\"Eliminar\" src=\"/images/cross_16.png\"></a>");
			        	    		_xhtml_out.println("</td>");
			        	    		_xhtml_out.println("</tr>");
		        	    		}
		        	    	}
		        	    	_xhtml_out.println("</tbody>");
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
		                    _xhtml_out.println("<div id=\"viewJobsDialog\" name=\"viewJobsDialog\"></div>");              
	        	    	}
	        	    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.cookie.js\"></script>");
	        	    	ScriptProcessManager _scm = new ScriptProcessManager();
		    	    	Map<String, Map<String, Object>> _scriptAux = _scm.listScript();
		    	    	createFunctionVariablePS(_scriptAux,type);
		    	    	addScriptProcess(_scriptAux, request.getParameter("jobName"));
		    	    	deleteScriptProcess();
		    	    	getJSViewJobsDialog(clientId);
	                 	pageJS+="$('#viewJobsDialog').dialog({\n";
	                 	pageJS+="   autoOpen: false,\n";
	                 	pageJS+="   height: 'auto',\n";
	                 	pageJS+="	modal: true,\n";
	                 	pageJS+="   width: 1000,\n";
	                 	pageJS+="   hide: 'fade'\n";
	                 	pageJS+="});\n";   
	        	    	_xhtml_out.println("</form>");
	        	    	
	        	    	pageJSFuncs+="function checkReschedule() {";
	        	    	pageJSFuncs+="	var rescheduleOnError = document.jobs.rescheduleOnError.value;";
	        	    	pageJSFuncs+="	var rescheduleInterval = document.jobs.rescheduleInterval;";
	        	    	pageJSFuncs+="	var rescheduleTimes = document.jobs.rescheduleTimes;";
	        	    	pageJSFuncs+="	if (rescheduleOnError == 'yes') {";
	        	    	pageJSFuncs+="		rescheduleInterval.removeAttribute('disabled');";
	        	    	pageJSFuncs+="		rescheduleTimes.removeAttribute('disabled');";
	        	    	pageJSFuncs+="	} else {";
	        	    	pageJSFuncs+="		rescheduleInterval.setAttribute('disabled','disabled');";
	        	    	pageJSFuncs+="		rescheduleTimes.setAttribute('disabled','disabled');";
	        	    	pageJSFuncs+="	}";
	        	    	pageJSFuncs+="}";
	        	    	pageJS+="checkReschedule();";
					}
	    			break;
	    		case SAVEJOB: {
		    			
		    			int type = JobManager.TYPE_BACKUP;
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			if(request.getParameter("opType") != null && "add".equalsIgnoreCase(request.getParameter("opType"))) {
		    				if(JobManager.existsjob(request.getParameter("name"))) {
		    					throw new Exception(getLanguageMessage("backup.jobs.exception.job_exist"));
		    				}
		    			}
		    			
		    			boolean verifyPreviousJob = false, spoolData = false, accurate = false, enabled = false, rescheduleOnError = false;
		    			int maxStartDelay, maxRunTime, maxWaitTime, priority, bandwith = 0, rescheduleInterval = 2, rescheduleTimes = 12;
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.name"));
		    			}
		    			if(request.getParameter("level") == null || request.getParameter("level").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.level"));
		    			}
		    			if(request.getParameter("fileset") == null || request.getParameter("fileset").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.pattern"));
		    			}
		    			if(request.getParameter("schedule") == null || request.getParameter("schedule").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.schedule"));
		    			}
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.repository"));
		    			}
		    			if(request.getParameter("poolDefault") == null || request.getParameter("poolDefault").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.pool"));
		    			}
		    			try {
		    				maxStartDelay = Integer.parseInt(request.getParameter("maxStartDelay"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.delay"));
		    			}
		    			try {
		    				maxRunTime = Integer.parseInt(request.getParameter("maxRunTime"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.runtime"));
		    			}
		    			try {
		    				maxWaitTime = Integer.parseInt(request.getParameter("maxWaitTime"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.timeout"));
		    			}
		    			try {
		    				priority = Integer.parseInt(request.getParameter("priority"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.priority"));
		    			}
		    			if(request.getParameter("bandwith") != null && !request.getParameter("bandwith").isEmpty()) {
		    				try {
			    				bandwith = Integer.parseInt(request.getParameter("bandwith"));
			    			} catch(NumberFormatException _ex) {
			    				throw new Exception(getLanguageMessage("backup.jobs.exception.bandwith"));
			    			}
		    			}
		    			if(request.getParameter("job_type") != null && "copy".equals(request.getParameter("job_type").toLowerCase())) {
		    				type = JobManager.TYPE_COPY;
		    			} else if(request.getParameter("job_type") != null && "migrate".equals(request.getParameter("job_type").toLowerCase())) {
		    				type = JobManager.TYPE_MIGRATE;
		    			} 
		    			if(request.getParameter("verifyPreviousJob") != null && "yes".equals(request.getParameter("verifyPreviousJob"))) {
		    				verifyPreviousJob = true;
		    			}
		    			if(request.getParameter("spoolData") != null && "yes".equals(request.getParameter("spoolData"))) {
		    				spoolData = true;
		    			}
		    			if(request.getParameter("accurate") != null && "yes".equals(request.getParameter("accurate"))) {
		    				accurate = true;
		    			}
		    			if(request.getParameter("enabled") != null && "yes".equals(request.getParameter("enabled"))) {
		    				enabled = true;
		    			}
		    			
		    			// Si el job se asocia con un sistema de almacenamiento externo, se coge el fileset asociado, que tiene el mismo nombre que el sistema
		    			String fileset = request.getParameter("fileset");
		    			
		    			if(request.getParameter("rescheduleOnError") != null && "yes".equals(request.getParameter("rescheduleOnError"))) {
		    				rescheduleOnError = true;
		    				if(request.getParameter("rescheduleInterval") != null && !request.getParameter("rescheduleInterval").isEmpty()) {
		    					try {
		    						rescheduleInterval = Integer.parseInt(request.getParameter("rescheduleInterval"));
		    					} catch (NumberFormatException _ex) {
		    						throw new Exception(getLanguageMessage("backup.jobs.exception.rescheduleInterval"));
		    					}
		    				}
		    				if(request.getParameter("rescheduleTimes") != null && !request.getParameter("rescheduleTimes").isEmpty()) {
		    					try {
		    						rescheduleTimes = Integer.parseInt(request.getParameter("rescheduleTimes"));
		    					} catch (NumberFormatException _ex) {
		    						throw new Exception(getLanguageMessage("backup.jobs.exception.rescheduleTimes"));
		    					}
		    				}
		    			}
		    			
		    			_jm.setJob(request.getParameter("name"), clientRealName, 
								request.getParameter("level"), request.getParameter("schedule"), fileset, request.getParameter("storage"), 
								request.getParameter("poolDefault"), request.getParameter("poolFull"), request.getParameter("poolIncre"), request.getParameter("poolDiff"),
								request.getParameter("hypervisorJob"), request.getParameter("nextJob"), verifyPreviousJob, maxStartDelay, maxRunTime, maxWaitTime,
								spoolData, enabled, priority, type, bandwith, accurate, rescheduleOnError, rescheduleInterval, rescheduleTimes);
						
						int _max=50;
						if (request.getParameter("numMax")!=null && !request.getParameter("numMax").isEmpty()){
							try{
								_max=Integer.valueOf(request.getParameter("numMax"));
							}catch(Exception ex){}
						}
						Map<Integer, Object> scriptsMap = new HashMap<Integer, Object>();
						for(int r = 0; request.getParameter("script_name" + r) != null || r<=_max; r++) {
							boolean before, success, fail, abort;
							if(request.getParameter("script_name" + r) == null || request.getParameter("script_name" + r).trim().isEmpty() || request.getParameter("script_name" + r).indexOf("--")==-1) {
								continue;
							}
							String _nameProcessScript=request.getParameter("script_name" + r);
							ScriptProcessManager _spm=new ScriptProcessManager();
							Map<String, Object> _scriptProcess=_spm.getScript(_nameProcessScript.substring(0, _nameProcessScript.indexOf("--")));
					        if(request.getParameter("script_success" + r) != null && "true".equalsIgnoreCase(request.getParameter("script_success" + r))) {
								success = true;
							} else {
								success = false;
							}
					        if(request.getParameter("script_fail" + r) != null && "true".equalsIgnoreCase(request.getParameter("script_fail" + r))) {
								fail = true;
							} else {
								fail = false;
							}
					        if(_scriptProcess.get("type")!=null && !((String)_scriptProcess.get("type")).isEmpty() && (Integer.valueOf((String)_scriptProcess.get("type"))==ScriptProcessManager.BEFORE_EXECUTION)) {
								before = true;
							} else {
								before = false;
							}
					        if("true".equals((String)_scriptProcess.get("abortType"))) {
					        	abort = true;
							} else {
								abort = false;
							}
					        boolean variablesSetted = false;
					        Map<String, String> _variableValues=new HashMap<String, String>();
							if (_scriptProcess!=null && _scriptProcess.get("variables")!=null){
								List<Map<String, String>> _variables= (List<Map<String, String>>) _scriptProcess.get("variables");
								for (Map<String, String> _variable: _variables){
									if (request.getParameter(_nameProcessScript+"-"+_variable.get("name"))!=null && !request.getParameter(_nameProcessScript+"-"+_variable.get("name")).isEmpty()){
										_variableValues.put(_variable.get("name"),request.getParameter(_nameProcessScript+"-"+_variable.get("name")));
										variablesSetted = true;
									} else{
										_variableValues.put(_variable.get("name"),"");
									}
								}
							}
							if (ISCSIManager.getClientInitiatorName() != null){
								_variableValues.put(StorageInventoryManager.airback_iqn_nameVar, ISCSIManager.getClientInitiatorName());
							}
							if (SCSIManager.getClientFibreChannelWWN() != null){
								_variableValues.put(StorageInventoryManager.airback_wwn_nameVar, SCSIManager.getClientFibreChannelWWN());
							}
							
							if (!variablesSetted) {
								Map<String, String> oldVars = JobManager.getScriptVars(request.getParameter("name")+"_"+request.getParameter("script_name" + r)+"_1");
								if (oldVars != null && !oldVars.isEmpty())
									_variableValues = oldVars;
							}
							
							List<String> _scriptsOrder=new ArrayList<String>();
							TreeMap<String,Map<String, String>> _scriptsContent=new TreeMap<String, Map<String, String>>();
							if (_scriptProcess!=null && _scriptProcess.get("scripts")!=null){
								List<Map<String, String>> _scripts= (List<Map<String, String>>) _scriptProcess.get("scripts");
								for (Map<String, String> _script: _scripts){
										Map<String, String> objScript = new HashMap<String, String>();
										objScript.put("order", _script.get("order"));
										objScript.put("script", getSmartContent(_script.get("content"), _variableValues));
										objScript.put("shell", _script.get("shell"));
										_scriptsContent.put(_script.get("order"),objScript);										
										_scriptsOrder.add(_script.get("order"));
								}
							}
							Collections.sort(_scriptsOrder);
							Map<String, Object> scriptMap = new HashMap<String, Object>();
							scriptMap.put("abort", abort);
							scriptMap.put("before", before);
							scriptMap.put("fail", fail);
							scriptMap.put("success", success);
							scriptMap.put("variableValues", _variableValues);
							scriptMap.put("scriptsContent", _scriptsContent);
							scriptsMap.put(r, scriptMap);
							//ScriptProcessManager.generateScriptsJob(request.getParameter("name"),request.getParameter("script_name" + r)  , _jm, _scriptsContent,abort,before,fail,success, _variableValues);
						}
						
						_jm.removeAllJobScripts(request.getParameter("name"));
						for(int r = 0; request.getParameter("script_name" + r) != null || r<=_max; r++) {
							if(request.getParameter("script_name" + r) == null || request.getParameter("script_name" + r).trim().isEmpty() || request.getParameter("script_name" + r).indexOf("--")==-1) {
								continue;
							}
							Map<String, Object> scriptMap = (Map<String, Object>) scriptsMap.get(r);
							ScriptProcessManager.generateScriptsJob(request.getParameter("name"),request.getParameter("script_name" + r)  , _jm, (TreeMap<String,Map<String, String>>) scriptMap.get("scriptsContent"), (Boolean)scriptMap.get("abort"), (Boolean)scriptMap.get("before"), (Boolean)scriptMap.get("fail"), (Boolean) scriptMap.get("success"), (Map<String, String>)scriptMap.get("variableValues"));
						}

						if (request.getParameter("deletedScriptProcess")!=null && !request.getParameter("deletedScriptProcess").isEmpty()){
							StringTokenizer _token=new StringTokenizer(request.getParameter("deletedScriptProcess"),";");
							while (_token.hasMoreTokens()){
								String _script=_token.nextToken();
								_jm.removeJobScript(request.getParameter("name"), _script);
							}
						}
						writeDocumentHeader();
		    			writeDocumentResponse(getLanguageMessage("backup.jobs.stored"), baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
						BackupOperator.reload();
	    			}
	    			break;
    			case LAUNCHJOB: {
    					if(request.getParameter("jobName") == null || request.getParameter("jobName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.name"));
		    			}
		    			
    					if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		    			_bo.runJob(clientIdReal, request.getParameter("jobName"));
		    			
		    			response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    			this.redirected=true;
    				}
	    			break;
    			case DELETEJOB: {
	    				if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			if(request.getParameterValues("jobsIds") == null && !request.getParameterValues("jobsIds").equals("")) {
		    				response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    				this.redirected=true;
		    			} else {
		    				if(request.getParameter("confirm") != null) {
		    					String [] ids = request.getParameterValues("jobsIds");
		    					int jobId;
				    			for(String id : ids) {
			    					try {
			    	    				jobId = Integer.parseInt(id);
			    	    			} catch(NumberFormatException _ex) {
			    	    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
			    	    			}
			    	    			
			    	    			BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
			    	    			_bo.deleteJob(jobId);
			    				}
				    			response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
				    			this.redirected=true;
			    			} else {
			    				String jobsIds = (String) request.getParameter("jobsIds");
		    					String [] ids = jobsIds.split("-");
		    					writeDocumentHeader();
			    				writeDocumentListQuestion(getLanguageMessage("backup.jobs.exception.question"), "jobsIds", Arrays.asList(ids),baseUrl+"?type=" + DELETEJOB + "&clientId=" + clientId + "&confirm=true&clientName=" + clientName, null);
			    			}
		    			}
    				}
	    			break;
	    		case REMOVEJOB: {
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			if(request.getParameter("jobName") == null || request.getParameter("jobName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.name"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				String isInGroup = GroupJobManager.isJobOnAnyGroup(request.getParameter("jobName"), _c);
		    				if (isInGroup != null)
		    					throw new Exception(getLanguageMessage("backup.jobs.exception.group")+" :"+isInGroup);
		    				
		    				_jm.removeJob(request.getParameter("jobName"));
		    				response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    				this.redirected=true;
		    			} else {
		    				writeDocumentHeader();
		    				writeDocumentQuestion(getLanguageMessage("backup.jobs.exception.question"), baseUrl+"?type=" + REMOVEJOB + "&clientName="+clientName+"&clientId=" + clientId + "&jobName=" + request.getParameter("jobName") +  "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case CANCELJOB: {
		    			int jobId;
		    			try {
		    				jobId = Integer.parseInt(request.getParameter("jobId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
		    			}
		    			
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		    			_bo.cancelJob(jobId);
		    			
		    			response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    			this.redirected=true;
	    			}
	    			break;
	    		case STOPJOB: {
		    			int jobId;
		    			try {
		    				jobId = Integer.parseInt(request.getParameter("jobId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
		    			}
		    			
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		    			_bo.stopJob(jobId);
		    			
		    			response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    			this.redirected=true;
	    			}
	    			break;
	    		case RESTARTJOB: {
		    			int jobId;
		    			
		    			try {
		    				jobId = Integer.parseInt(request.getParameter("jobId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
		    			}
		    			
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		    			_bo.restartIncompleteJob(jobId);
		    			
		    			response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    			this.redirected=true;
	    			}
	    			break;
	    		case PRUNEJOBS: {
		    			if(!this.securityManager.checkCategory(_cm.getClientCategories(clientName))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		    			_bo.pruneJobs(clientIdReal);
		    			response.sendRedirect(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    			this.redirected=true;
	    			}
	    			break;
	    		case JOBVOLUMES: {
		    			int jobId, _offset = 0;
		    			try {
		    				jobId = Integer.parseInt(request.getParameter("jobId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
		    			}
		    			
		    			List<Map<String, String>> volumes = _jm.getVolumesForJob(jobId);
		    			
		    			writeDocumentBack(baseUrl+"?clientName=" + clientName + "&clientId=" + clientId);
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/cog_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.jobs.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.jobs.volumes"));
	            		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");					
	                	_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<table>");
	                    if(!volumes.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.volume"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.jobs.pool"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> volume : volumes) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(volume.get("volume"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(volume.get("pool"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_job_volumes"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
		    		}
		    		break;
	    	}
 		} catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {	
	    	if (this.type != JOB_CLIENT_LIST_JSON &&  this.type!=VIEWPROCESSSCRIPT && this.type!=JOBVOLUMES)
	    		writeDocumentFooter();
	    }
	}
	
	@SuppressWarnings("unchecked")
	public void getGroupJobListJobs(PrintWriter _xhtml_out, HttpServletRequest request, String clientName) throws Exception {
		_xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		_xhtml_out.print(request.getParameter("name")+": "+getLanguageMessage("common.menu.advanced.script_process"));
        _xhtml_out.println("</h2>");
        _xhtml_out.print("<br/>");
        _xhtml_out.println("<fieldset>");
        _xhtml_out.println("<table style=\"font-size:12px;\">");
        ScriptProcessManager _spm=new ScriptProcessManager();
        Map<String, Object> _scriptProcess=null;
        if (request.getParameter("generated")==null || request.getParameter("jobName")==null){
        	_scriptProcess = _spm.getScript(request.getParameter("name"));
        }else{
        	 List<Map<String, Object>> _scriptProcessList=JobManager.getJobProcessScripts(request.getParameter("jobName"), clientName);
        	 for (Map<String, Object> _scriptProcessItem: _scriptProcessList){
        		 if (_scriptProcessItem.get("name")!=null 
        				 && (_scriptProcessItem.get("name").equals(request.getParameter("name")))){
        			 _scriptProcess=_scriptProcessItem;
        			 break;
        		 }
        	 }
        }
        if (_scriptProcess != null) {
        	if (_scriptProcess.get("scripts") != null) {
				List<Map<String, String>> _scripts = (List<Map<String, String>>) _scriptProcess.get("scripts");
        		Map<String, String> _variableValues = new HashMap<String, String>();
        		if (_scriptProcess.get("variables") != null) {
        			if (_scriptProcess.get("variables") instanceof Map<?, ?>)
        				_variableValues = (Map<String, String>) _scriptProcess.get("variables");
        			else {
        				List<Map<String, String>> _variables = (List<Map<String, String>>) _scriptProcess.get("variables");
        				_variableValues=new HashMap<String, String>();
        				if (_variables!=null && !_variables.isEmpty()){
        					for (Map<String, String> _variable: _variables){
        						if (request.getParameter(_variable.get("name"))!=null){
        							_variableValues.put(_variable.get("name"),request.getParameter(_variable.get("name")));
        						} else{
        							_variableValues.put(_variable.get("name"),"");
        						}
        					}
        				}
        			}
        		}
		      
        		if (!_scripts.isEmpty()) {
        			Map<String,Map<String, String>> _scriptOrdered=new TreeMap<String, Map<String, String>>();
        			for (Map<String, String> _scriptItem : _scripts) {
        				_scriptOrdered.put(_scriptItem.get("order"), _scriptItem);
        			}
	            	_xhtml_out.println("<tr>");
	            	_xhtml_out.println("<td>&nbsp;</td>");
                    _xhtml_out.print("<td class=\"title\">");
                    _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.order"));
                    _xhtml_out.println("</td>");
                    _xhtml_out.print("<td class=\"title\">");
                    _xhtml_out.print(getLanguageMessage("common.menu.advanced.script_process.content"));
                    _xhtml_out.println("</td>");
	                _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
	                _xhtml_out.println("</tr>");
	                int _offset=1;
	            	for (String _scriptKey : _scriptOrdered.keySet()) {
	            		Map<String, String> _scriptItem=_scriptOrdered.get(_scriptKey);
	            		_xhtml_out.print("<tr");
	                	if(_offset % 2 == 0) {
	                		_xhtml_out.print(" class=\"highlight\"");
	                	}
	                	_xhtml_out.println(">");
	                	_xhtml_out.println("<td>&nbsp;</td>");
	                	_xhtml_out.println("<td style=\"vertical-align: top;\">");
	                	_xhtml_out.print(_scriptItem.get("order"));
	                	_xhtml_out.println(" </td>");
	                	
                    	_xhtml_out.print("<td><textarea readonly=\"true\" rows=\"6\" cols=\"50\">");
                    	_xhtml_out.print(encodeHTML(getSmartContent(_scriptItem.get("content"),_variableValues)));
						_xhtml_out.println("</textarea></td>");
						_xhtml_out.println("<td/>");
	                    _xhtml_out.println("</tr>");
	                    _offset++;
	            	}
	            } else {
	            	_xhtml_out.println("<tr>");
	            	_xhtml_out.println("<td>");
	            	_xhtml_out.println(getLanguageMessage("common.menu.advanced.script_process.no_script_process"));
	            	_xhtml_out.println("</td>");
	                _xhtml_out.println("</tr>");
	            }
        	} else {
        		_xhtml_out.println("<tr>");
            	_xhtml_out.println("<td>");
            	_xhtml_out.println(getLanguageMessage("common.menu.advanced.script_process.no_script_process"));
            	_xhtml_out.println("</td>");
                _xhtml_out.println("</tr>");
        	}
        } else {
        	_xhtml_out.println("<tr>");
        	_xhtml_out.println("<td>");
        	_xhtml_out.println(getLanguageMessage("common.menu.advanced.script_process.no_script_process"));
        	_xhtml_out.println("</td>");
            _xhtml_out.println("</tr>");
        }
            
        _xhtml_out.println("</table>");
        _xhtml_out.print("<br/>");
        _xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.print("</div>");
	}
	
	public void createFunctionVariablePS(Map<String, Map<String, Object>> scripts,Integer type) throws Exception{
		pageJSFuncs+="function conChangePScript(comand) {\n";
		pageJSFuncs+="if (comand!=null && comand.length>0 && comand.search('--')==-1) {\n";
		pageJSFuncs+="	var contenido=\"\";\n";
		for (String _key: scripts.keySet()){
			pageJSFuncs+="if (comand=='"+_key+"') {\n";
			if (scripts.get(_key)!=null && scripts.get(_key).get("variables")!=null){
				@SuppressWarnings("unchecked")
				List<Map<String, String>> _variables= (List<Map<String, String>>) scripts.get(_key).get("variables");				
				for (Map<String, String> _variable: _variables){
					String desc = _variable.get("description");
					if (desc == null || desc.isEmpty())
						desc = _variable.get("name");
					boolean password = false;
	 				if (_variable.get("password") != null && ((String)_variable.get("password")).equals("true"))
	 					password = true;
					pageJSFuncs+="contenido=contenido+\""+HtmlFormUtils.inputTextLabelTitle("variableValue"+_variable.get("name"), desc, null, (String) _variable.get("name"), false, password).trim().replace("\"", "'").replace("\n", "")+"\";\n";
				}
				pageJSFuncs+="contenido=contenido+\"<input name='nameScript' id='nameScript' type='hidden' value='"+_key+"'/>\";\n";
			}
			pageJSFuncs+="}\n";
		}
		pageJSFuncs+="		$('#dynamicForm').show();\n";
		pageJSFuncs+="		$('#dynamicForm').html(contenido);\n";
		pageJSFuncs+="	}else{$('#dynamicForm').hide();};\n";
    	pageJSFuncs+="}\n";
	}
	
	public void addScriptProcess(Map<String, Map<String, Object>> scripts, String jobName) throws Exception{
		pageJSFuncs+="function addScriptProcesss() {\n";
		pageJSFuncs+="if (document.getElementById('variableCount')!=null && document.getElementById('variableCount').value.length>0 && document.getElementById('nameScript')!=null && document.getElementById('nameScript').value.length>0 && document.getElementById('nameScript').value != '--') {\n";
		pageJSFuncs+="$('#tableProcessScript').css('display', 'inline');";		
		pageJSFuncs+="	var contenido=\"\";\n";
		pageJSFuncs+="	var ocultoScripts=\"\";\n";
		pageJSFuncs+="	var numi = document.getElementById('variableCount').value;\n";	
		pageJSFuncs+="	var numMax = document.getElementById('numMax').value;\n";	
		pageJSFuncs+="	contenido=contenido+\"<tr id='itemPS-\"+document.getElementById('nameScript').value+\"' \";";
		pageJSFuncs+="	if(numi % 2 == 0) {";
		pageJSFuncs+="		contenido=contenido+\" class='highlight' \"; ";
		pageJSFuncs+="	}";
		pageJSFuncs+="	contenido=contenido+\">\";\n";
		pageJSFuncs+="	contenido=contenido+\"<td>\";\n";
		pageJSFuncs+="	contenido=contenido+\"<input readonly='true' class='hidden' type='text'  id=\\\"script_name\"+numMax+\"\\\" name=\\\"script_name\"+numMax+\"\\\" value=\\\"\"+document.getElementById('nameScript').value+\"--\"+numMax+\"\\\"/>\"+'';\n";
		pageJSFuncs+="	contenido=contenido+\"</td>\";\n";
		for (String _key: scripts.keySet()){
			if (scripts.get(_key)!=null && scripts.get(_key).get("type")!=null && !((String)scripts.get(_key).get("type")).isEmpty()){
					pageJSFuncs+="	if (document.getElementById('nameScript').value=='"+_key+"'){\n";
					pageJSFuncs+="		contenido=contenido+\"<td>\";\n";
					pageJSFuncs+="		contenido=contenido+\"<input readonly='true' class='hidden' type='hidden' name=\\\"script_when\"+numMax+\"\\\" value=\\\""+(String)scripts.get(_key).get("type")+"\\\"/>\"+'';\n";
					pageJSFuncs+="		contenido=contenido+\"<input readonly='true' type='text' value=\\\""+(ScriptProcessManager.AFTER_EXECUTION==Integer.valueOf((String)scripts.get(_key).get("type")) ? getLanguageMessage("backup.jobs.scripts.after") : getLanguageMessage("backup.jobs.scripts.before"))+"\\\"/>\"+'';\n";
					pageJSFuncs+="		contenido=contenido+\"</td>\";\n";
					pageJSFuncs+="	}\n";
			}
		}
		pageJSFuncs+="	contenido=contenido+\"<td>\";\n";
		
		pageJSFuncs+="	contenido=contenido+\"<input class='form_checkbox' type='checkbox' name=\\\"script_success\"+numMax+\"\\\" value='true' checked='checked'/>\";\n";
		pageJSFuncs+="	contenido=contenido+\"</td>\";\n";
		pageJSFuncs+="	contenido=contenido+\"<td>\";\n";
		pageJSFuncs+="	contenido=contenido+\"<input class='form_checkbox' type='checkbox' name=\\\"script_fail\"+numMax+\"\\\" value='true'/>\";\n";
		pageJSFuncs+="	contenido=contenido+\"</td>\";\n";
		pageJSFuncs+="	contenido=contenido+\"<td>\";\n";
		pageJSFuncs+="	var variableValues='';\n";
		for (String _key: scripts.keySet()){
			pageJSFuncs+="	if (document.getElementById('nameScript').value=='"+_key+"'){\n";
			if (scripts.get(_key)!=null && scripts.get(_key).get("variables")!=null){
				@SuppressWarnings("unchecked")
				List<Map<String, String>> _variables= (List<Map<String, String>>) scripts.get(_key).get("variables");
				for (Map<String, String> _variable: _variables){
					pageJSFuncs+="	contenido=contenido+\""+_variable.get("name")+"= \"+document.getElementById('variableValue"+_variable.get("name")+"').value+\";\";\n";
					pageJSFuncs+="	contenido=contenido+\"<input type='hidden' id=\\\""+_key+"--\"+numMax+\"-"+_variable.get("name")+"\\\" name=\\\""+_key+"--\"+numMax+\"-"+_variable.get("name")+"\\\" value=\\\"\"+document.getElementById('variableValue"+_variable.get("name")+"').value+\"\\\"/>\";\n";
					pageJSFuncs+="	variableValues=variableValues+\"&"+_variable.get("name")+"=\"+document.getElementById('variableValue"+_variable.get("name")+"').value;\n";
				}
			}
			pageJSFuncs+="	}\n";
		}
		pageJSFuncs+="	contenido=contenido+\"</td>\";\n";
		pageJSFuncs+="	contenido=contenido+\"<td>\";\n";
		
		pageJSFuncs+="	contenido=contenido+\"<a href='javascript:viewJobs(document.getElementById(\\\"nameScript\\\").value,\\\"&jobName="+jobName+"\"+variableValues+\"\\\");'><img src='/images/group_job_16.png' title='\";\n";
		pageJSFuncs+="	contenido=contenido+\""+getLanguageMessage("backup.jobs.scripts.view_script")+"\";\n";
		pageJSFuncs+="	contenido=contenido+\"' alt='\";\n";
		pageJSFuncs+="	contenido=contenido+\""+getLanguageMessage("backup.jobs.scripts.view_script")+"\";\n";
		pageJSFuncs+="	contenido=contenido+\"'/></a>\";\n";
		
		pageJSFuncs+="	contenido=contenido+\"<a href='javascript:deleteScriptProcess(document.getElementById(\\\"nameScript\\\").value);'><img alt='Eliminar' title='Eliminar' src='/images/cross_16.png'></a>\"\n";
		pageJSFuncs+="	contenido=contenido+\"</td>\";\n";
		pageJSFuncs+="	if (document.getElementById('variableCount').value=='')\n";
		pageJSFuncs+="	contenido=contenido+\"</tr>\";\n";		
		pageJSFuncs+="		$('#dynamicForm').hide();\n";
		pageJSFuncs+="	    $('#scriptTable').append(contenido);\n";
		pageJSFuncs+="	document.getElementById('variableCount').value=parseInt(numi)+1;\n";
		pageJSFuncs+="	document.getElementById('numMax').value=parseInt(numMax)+1;\n";
		pageJSFuncs+="	}else{$('#dynamicForm').hide();\n;";
		pageJSFuncs+="		Apprise('"+getLanguageMessage("backup.jobs.warning.no_type_scriptProcess")+"');\n";
		pageJSFuncs+="	}\n";
		pageJSFuncs+="	$('#processScript option[value=--]').attr('selected','selected');\n";
    	pageJSFuncs+="}\n"; 
	}
	
	public void deleteScriptProcess() throws Exception{
		pageJSFuncs+="function deleteScriptProcess(scriptprocess) {\n";
		pageJSFuncs+="	if (document.getElementById('itemPS-'+scriptprocess)!=null) {\n";			
		pageJSFuncs+="		$('#itemPS-'+scriptprocess).remove();\n";
		pageJSFuncs+="		document.getElementById('variableCount').value=parseInt(document.getElementById('variableCount').value)-1;\n";
		pageJSFuncs+="		if (document.getElementById('variableCount').value==0) {\n";			
		pageJSFuncs+="			$('#tableProcessScript').css('display', 'none');";	
    	pageJSFuncs+="		}\n"; 
    	pageJSFuncs+="	}\n";
    	pageJSFuncs+="	if (document.getElementById('deletedScriptProcess')!=null) {\n";
    	pageJSFuncs+="		document.getElementById('deletedScriptProcess').value=document.getElementById('deletedScriptProcess').value+';'+scriptprocess;\n";
    	pageJSFuncs+="	}\n";
    	pageJSFuncs+="}\n";
	}
	
	public String toJSON(List<Map<String, String>> jobs, Integer pag, Integer total, Integer clientId, Integer clientIdReal, String clientName) throws Exception
	{
		String json = "{\"page\":"+pag+",\"total\":"+total;
		
		if (jobs != null && jobs.size() > 0) {
			json += ",\"rows\":[";
			int a = 0;
			for (Map<String, String> job : jobs) {
				if (a == 0) {
					json += "{\"id\":\""+job.get("id")+"\",\"cell\":[";
					a++;
				} else
					json += ",{\"id\":\""+job.get("id")+"\",\"cell\":[";
				
				json += "\""+job.get("id")+"\"";
		    	String name = job.get("name");
				int limitCut = name.length();
				if (name != null && name.length() > 17) {
					while (limitCut > 17) {					
						name = name.substring(0, limitCut)+"-<br />"+name.substring(limitCut);
						limitCut-=17;
					}
				}
				if ((ObjectLock.isBlock(ObjectLock.JOBS_TYPE_OBJECT, job.get("id"), null) || job.get("canceled") != null) && "true".equals(job.get("run")))
					json += ",\""+name+" <input type='hidden' class='coloring'/>\"";
				else
					json += ",\""+name+"\"";
				json += ",\""+job.get("type")+"\"";
				json += ",\""+job.get("level")+"\"";
				json += ",\""+getErrorMsg(job)+"\""; 
				json += ",\""+job.get("start")+"\"";
				json += ",\""+job.get("end")+"\"";
				json += ",\""+job.get("files")+"\"";
				json += ",\""+job.get("size")+"\"";
				json += ",\""+this.getToolsJob(job, clientId, clientIdReal, clientName)+"\"";
				json += "]}";
			}
			json += "]";
		}
		json += "}";
		return json;
	}
	
	public String getErrorMsg(Map<String, String> job)
	{
		String message = "";
		if("error".equals(job.get("alert"))) {
			message+="<div class='grid_error'>";
			message+="&nbsp;";
			message+="&nbsp;";
			message+=job.get("status");
			message+="&nbsp;";
			if("true".equals(job.get("run"))) {
				message+="(spool: ";
				message+=job.get("spool");
				message+=")";
			} else {
				message+="(";
				message+=job.get("errors");
				message+=")";
			}
			message+="</div>";
		} else {
			message+="<div class='grid_";
			message+=job.get("alert");
			message+="'>";
			message+="&nbsp;";
			message+="&nbsp;";
			message+=job.get("status");
			message+="&nbsp;(";
			message+=job.get("errors");
			message+=")";
			message+="</div>";
		}
		
		return message;
	}
	
	public void getJSViewJobsDialog(Integer clientId) throws Exception {
		pageJSFuncs+="function viewJobs(name,variables) {\n";
		pageJSFuncs+="	$('#viewJobsDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n";
		pageJSFuncs+="	$('#viewJobsDialog').dialog( 'open' );\n";
		pageJSFuncs+="	$.ajax({\n";
		pageJSFuncs+="		url: '"+baseUrl+"?type="+VIEWPROCESSSCRIPT+"'+variables,\n";
		pageJSFuncs+="		cache: false,\n";
		pageJSFuncs+="		data: { name: name, clientId: "+clientId+"}\n";
		pageJSFuncs+="	}).done(function( html ) {\n";
		pageJSFuncs+="		$('#viewJobsDialog').html(html);\n";
		pageJSFuncs+="	});\n";
		pageJSFuncs+="};";
	}
	
	/**
	 * Obtiene la cadena xhtml de los botones de accion sobre un job
	 * @param job
	 * @param clientId
	 * @param clientName
	 * @return
	 */
	public String getToolsJob(Map<String, String> job, Integer clientId, Integer clientIdReal, String clientName) {
		String tools = "";
		tools+="<a href='javascript:viewLog(\\\""+job.get("id")+"\\\",\\\""+clientIdReal+"\\\");'";
    	tools+="><img src='/images/book_16.png' title='";
    	tools+=getLanguageMessage("common.message.view");
    	tools+="' alt='";
    	tools+=getLanguageMessage("common.message.view");
    	tools+="'/></a>&nbsp;";
		if("true".equals(job.get("run"))) {
			if (!ObjectLock.isBlock(ObjectLock.JOBS_TYPE_OBJECT, job.get("id"), null) && job.get("canceled") == null) {
		    	tools+="<a href='"+baseUrl+"?type=";
		    	tools+=STOPJOB;
		    	tools+="&nameJob=";
		    	tools+=job.get("name");
		        tools+="&clientName=";
		        tools+=clientName;
		        tools+="&clientId=";
		        tools+=clientId;
		        tools+="&jobId=";
		        tools+=job.get("id");
		        tools+="'><img src='/images/control_pause_16.png' title='";
		        tools+=getLanguageMessage("common.message.stop");
		    	tools+="' alt='";
		    	tools+=getLanguageMessage("common.message.stop");
		    	tools+="'/></a>&nbsp;";
		        tools+="<a href='"+baseUrl+"?type=";
		        tools+=CANCELJOB;
		        tools+="&nameJob=";
		        tools+=job.get("name");
		        tools+="&clientName=";
		        tools+=clientName;
		        tools+="&clientId=";
		        tools+=clientId;
		        tools+="&jobId=";
		        tools+=job.get("id");
		        tools+="'><img src='/images/control_stop_16.png' title='";
		        tools+=getLanguageMessage("common.message.cancel");
		    	tools+="' alt='";
		    	tools+=getLanguageMessage("common.message.cancel");
		    	tools+="'/></a>&nbsp;";
	    	}
	    } else {
	    	if(!"error".equals(job.get("alert"))) {
	    		tools+="<a href='javascript:viewVolumes(\\\""+job.get("id")+"\\\",\\\""+clientIdReal+"\\\");'";
	        	tools+="><img src='/images/database_gear_16.png' title='";
	        	tools+=getLanguageMessage("common.message.volumes");
	        	tools+="' alt='";
	        	tools+=getLanguageMessage("common.message.volumes");
	        	tools+="'/></a>&nbsp;";
	    	}
	    	if(!"OK".equals(job.get("return"))) {
	        	tools+="<a href='"+baseUrl+"?type=";
	        	tools+=RESTARTJOB;
	            tools+="&nameJob=";
	            tools+=job.get("name");
	            tools+="&clientName=";
	            tools+=clientName;
	            tools+="&clientId=";
	            tools+=clientId;
	            tools+="&jobId=";
	            tools+=job.get("id");
	            tools+="'><img src='/images/control_restart_16.png' title='";
	            tools+=getLanguageMessage("common.message.restart");
	        	tools+="' alt='";
	        	tools+=getLanguageMessage("common.message.restart");
	        	tools+="'/></a>&nbsp;";
	        }
	    }
		return tools;
	}
	
 	public void fillSelects() throws Exception {
    	ScriptProcessManager _sm = new ScriptProcessManager();
    	Map<String, Map<String, Object>> _scripts = _sm.listScript();
    	selectTypeStep = new TreeMap<String, String>();
    	selectTypeStep.put( "--", "--");
    	for (String _script: _scripts.keySet()){
			selectTypeStep.put( (String)_scripts.get(_script).get("name"), (String)_scripts.get(_script).get("name"));
    	}
    	
 	}
 	
	protected String getSmartContent(String content, Map<String, String> attributes)
			throws Exception {
		if (content == null || content.isEmpty()) {
			return null;
		}
		int _old_offset = 0;
		StringBuilder _sb = new StringBuilder();
		for (int _offset = content.indexOf("[[[", 0); _offset != -1; _offset = content
				.indexOf("[[[", _offset)) {
			_sb.append(content.substring(_old_offset, _offset));
			_offset += 3;
			if (content.indexOf("]]]", _offset) != -1) {
				String _name = content.substring(_offset, content.indexOf(
						"]]]", _offset));
				if (attributes != null && attributes.containsKey(_name)) {
						_sb.append(attributes
								.get(_name));
				} else {
					_sb.append("");
				}
				_old_offset = content.indexOf("]]]", _offset) + 3;
			}
		}
		_sb.append(content.substring(_old_offset, content.length()));
		return _sb.toString();
	}

	public String getRowColorCheckingJS(Integer clientId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("function checkColors() {\n");
		sb.append("	var rows = $('table#flexigridJobs"+clientId+"').find('tr').get();");
		sb.append("	$.each(rows,function(i,n) {\n");
		sb.append("		var st = $(n).find('.coloring').get()\n");
		sb.append("		if (st && st.length>0) {\n");
		sb.append("			$(n).css('background-color', '#F2C291');\n");
		sb.append("		}\n");
		sb.append("	});\n");
		sb.append("}\n");
		return sb.toString();
	}
	
	public String getJSViewJobLog() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function viewLog(jobId, clientId) {\n");
		_sb.append("	$('#idJobLog').val(jobId);\n");
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
	
	public String getJSViewJobVolumes() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function viewVolumes(jobId, clientId) {\n");
		_sb.append("	$('#idJobVolumes').val(jobId);\n");
		_sb.append("	$('#viewVolumesDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n");
		_sb.append("	$('#viewVolumesDialog').dialog( 'open' );\n");
		_sb.append("	$.ajax({\n");
	 	_sb.append("		url: '"+baseUrl+"?type="+JOBVOLUMES+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: {jobId : jobId, clientId: clientId}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#viewVolumesDialog').html(html);\n");
	 	_sb.append("	});\n");
	 	_sb.append("};");
	 	return _sb.toString();
	}
	
	public static String encodeHTML(String s)
	{
	    StringBuffer out = new StringBuffer();
	    for(int i=0; i<s.length(); i++)
	    {
	        char c = s.charAt(i);
	        if(c > 127 || c=='"' || c=='<' || c=='>')
	        {
	           out.append("&#"+(int)c+";");
	        }
	        else
	        {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
}
