package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ScheduleManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.imagine.wbsairback.util.ObjectLock;
import com.whitebearsolutions.util.Configuration;


public class AdvancedGroupJob extends WBSImagineServlet {
	
	private static final long serialVersionUID = -1529373929499077723L;
	private int type;
	public final static int ADD_GROUPJOB_GROUPING = 2;
	public final static int REMOVE_GROUPJOB = 3;
	public final static int STORE_GROUPJOB_GROUPING = 4;
	public final static int LAUNCH_GROUPJOB = 5;
	public final static int STOP_GROUPJOB = 6;
	public final static int CANCEL_GROUPJOB = 7;
	public final static int RESTART_GROUPJOB = 8;
	public final static int REMOVE_JOB = 9;
	public final static int VIEW_GROUPJOB = 52355210;
	public final static int JSON_GROUPJOBS = 68453126;
	public final static int SELECT_JOBS = 83978923;
	
	public final static String baseUrl = "/admin/"+AdvancedGroupJob.class.getSimpleName();
	
	private final static Logger logger = LoggerFactory.getLogger(AdvancedGroupJob.class);
	public static Map<String, String> selectTemplateJob = null;
	public static Map<String, String> selectClients = null;
	public static Map<String, String> selectTypeStep = null;
	public static Map<String, String> msgTypeGroupJob = null;

	@Override
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
			
			if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
				throw new Exception("backup director service is not running");
			}
			
			Configuration _c = this.sessionManager.getConfiguration();
			LicenseManager _lm = new LicenseManager();
			JobManager jm = new JobManager(_c);
			ClientManager cm = new ClientManager(_c);
			BackupOperator _bo = new BackupOperator(_c);
			
			if (this.type == JSON_GROUPJOBS) {
				getJsonGroupJobs(request, response, _lm, _c);
				return;
			} else if (this.type == VIEW_GROUPJOB) {
				getGroupJobListJobs(_xhtml_out, request.getParameter("name"), _c, _lm);
				return;
			} else if (this.type == SELECT_JOBS) {
				getSelectJobs(_xhtml_out,  request.getParameter("clientName"), request.getParameter("id"), jm, cm);
				return;
			} 

			response.setContentType("text/html");
			writeDocumentHeader();
			
	    	fillSelects(cm);
	    	switch(this.type) {
	    		
    			default: {
    				_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" method=\"post\">");
    				
    				printSectionHeader(_xhtml_out, getLanguageMessage("advanced.groupjob.info"));
                    _xhtml_out.println("<div style=\"margin:20px auto;width:94%;clear:both;\">");
                    _xhtml_out.println("<table id=\"flexigridGroups\" style=\"margin-left:0px;margin-right:0px;\"></table>");
                    _xhtml_out.println("</div>");
        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
        	    	_xhtml_out.print("</div>");
                    printJSGrid(_xhtml_out, _lm);
                 	_xhtml_out.print("</form>");
                 	
                 	_xhtml_out.println("<div id=\"viewJobsDialog\" name=\"viewJobsDialog\" title=\""+getLanguageMessage("advanced.groupjob.jobs")+"\" style=\"font-size:12px;\"></div>");
                 	_xhtml_out.print("<input type=\"hidden\" name=\"nameGroup\" id=\"nameGroup\" value=\"\">");
                 	pageJSFuncs+=getJSViewJobsDialog();
                 	pageJS+="var wHeight = $(window).height();\n";
        	    	pageJS+="var dHeight = wHeight * 0.8;\n";
        	    	pageJS+="var wWidth = $(window).width();\n";
        	    	pageJS+="var dWidth = wWidth * 0.9;\n";
                 	pageJS+="$( '#viewJobsDialog' ).dialog({\n";
                 	pageJS+="   autoOpen: false,\n";
                 	pageJS+="   height: dHeight,\n";
                 	pageJS+="	modal: true,\n";
                 	pageJS+="   width: dWidth,\n";
                 	pageJS+="   hide: 'fade',\n";
                	pageJS+="   buttons: {\n";
                	pageJS+="		'"+getLanguageMessage("common.message.refresh")+"': function(event) {\n";
                	pageJS+="					$('#viewJobsDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n";
                	pageJS+="					viewJobs($('#nameGroup').val());\n";
                	pageJS+="				}\n";
                	pageJS+="	}\n";
                 	pageJS+="});\n";
                 	
                 	_xhtml_out.println("<div id=\"viewLogDialog\" name=\"viewLogDialog\" title=\"Log\" style=\"font-size:12px;\"></div>");
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
                 	pageJSFuncs+="function addGroupJobGrouping() {\n";
        	    	pageJSFuncs+="	document.location.href='"+baseUrl+"?type="+ADD_GROUPJOB_GROUPING+"'\n";
        	    	pageJSFuncs+="}\n";
                 	
    			} break;
    			
    			case ADD_GROUPJOB_GROUPING: {   

    				writeDocumentBack(baseUrl);
    				
    				StorageManager _sm = new StorageManager(_c);
    				
    				printSectionHeader(_xhtml_out, getLanguageMessage("advanced.groupjob.info"));
    				
    				_xhtml_out.print("<form action=\""+baseUrl+"\" name=\"form\" method=\"post\">");
    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" id=\"type\" value=\"" + STORE_GROUPJOB_GROUPING + "\"/>");
    				
    				_xhtml_out.println("<div class=\"window\">");
    				_xhtml_out.println("<h2>");
    				_xhtml_out.print(getLanguageMessage("advanced.groupjob.manual.add"));
    				_xhtml_out.print(HtmlFormUtils.saveHeaderButton("form", getLanguageMessage("common.message.save")));
    		        _xhtml_out.println("</h2>");
                    _xhtml_out.println("<fieldset>");
                    _xhtml_out.print(HtmlFormUtils.inputTextObj("name", getLanguageMessage("advanced.groupjob.name"), null, true));
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
                	_xhtml_out.println("<div class=\"clear\"/></div>");
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"schedule\">");
        	    	_xhtml_out.print(getLanguageMessage("backup.jobs.schedule"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<select class=\"form_select\" name=\"schedule\">");
        	    	_xhtml_out.print("<option value=\"\">--</option>");
					for(String schedule : ScheduleManager.getScheduleNames()) {
						_xhtml_out.print("<option value=\"");
						_xhtml_out.print(schedule);
						_xhtml_out.print("\"");
						_xhtml_out.print(">");
						_xhtml_out.print(schedule);
						_xhtml_out.println("</option>");
					}
					_xhtml_out.println("</select>");
        	    	_xhtml_out.println("</div>");
        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
                	_xhtml_out.println("</fieldset>");
                	_xhtml_out.println("<div class=\"clear\"></div>");
                 	_xhtml_out.println("</div>");
                 	
    		     	_xhtml_out.println("<div class=\"window\">");
    				_xhtml_out.println("<h2>");
    				_xhtml_out.print(getLanguageMessage("advanced.groupjob.jobs"));
    				_xhtml_out.print("<a href=\"javascript:addJob();\"><img src=\"/images/add_16.png\" title=\"");
    		    	_xhtml_out.print(getLanguageMessage("common.message.add"));
    		    	_xhtml_out.print("\" alt=\"");
    		    	_xhtml_out.print(getLanguageMessage("common.message.add"));
    		    	_xhtml_out.println("\"/></a>");
    		        _xhtml_out.println("</h2>");
    		        _xhtml_out.println("<fieldset>");
    		        _xhtml_out.println("<div class=\"standard_form\">");
    		        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:150px;margin-right:50px;float:left;\"> &nbsp; </div>");
    		        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:150px;margin-right:50px;float:left;\">"+getLanguageMessage("advanced.groupjob.create.clients")+"</div>");
    		        _xhtml_out.print("<div class=\"titleDiv\" style=\"width:150px;margin-right:50px;float:left;\">"+getLanguageMessage("advanced.groupjob.create.jobs")+"</div>");
    		        _xhtml_out.println("</div>");
    		        _xhtml_out.println("<div name=\"jobs\" id=\"jobs\" style=\"width:100%\">");
    		        int x = 1;
    		        _xhtml_out.print("<input type=\"hidden\" name=\"numJobs\" id=\"numJobs\" value=\""+x+"\"/>");
    		        _xhtml_out.println(getHtmlSelectJobs("clijo"+x, x));
    		        _xhtml_out.println("</div>");
    		        _xhtml_out.println("</fieldset>");
    		    	_xhtml_out.println("<div class=\"clear\"></div>");
    		    	_xhtml_out.println("</div>");
    		    	
    		    	_xhtml_out.println("</form>");
    				
    				pageJSFuncs+=getJSAddJob();
    				pageJSFuncs+=getJSUpdateScriptCombo();
    				
    			} break;
    			
    			case STORE_GROUPJOB_GROUPING: {
    				
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
					if (!request.getParameter("name").matches("[0-9a-zA-Z-._]+")) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
    				Map<Integer, Map<String, Object>> jobs = new TreeMap<Integer, Map<String, Object>>();
    				Integer numJobs = Integer.parseInt(request.getParameter("numJobs"));
					int i=1;
					while (i <= numJobs) {
						if (request.getParameter("clijo"+i+"-job") != null && !request.getParameter("clijo"+i+"-job").isEmpty()) {
							Map<String, Object> job = new HashMap<String, Object>();
							job.put("name", request.getParameter("clijo"+i+"-job"));
							job.put("order", i);
							jobs.put(i, job);
						}
						i++;
					}
					
					if (jobs.size()<1) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.numjobs"));
					}
					
					GroupJobManager.saveGroupJob(request.getParameter("name"),  GroupJobManager.TYPE_MANUAL_SELECTION, null, jobs, request.getParameter("schedule"), null);
					
					if (request.getParameter("schedule") != null && !request.getParameter("schedule").isEmpty()) {
						for (Integer order : jobs.keySet()) {
							Map<String, Object> job = (Map<String, Object>) jobs.get(order);
							File _f = new File(WBSAirbackConfiguration.getDirectoryJobs() + "/" + (String) job.get("name") + ".conf");
							BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Job", (String) job.get("name"), "Schedule", new String[]{ request.getParameter("schedule") });
							break;
						}
					}
					
    				writeDocumentResponse(getLanguageMessage("advanced.groupjob.stored"), baseUrl);
					
    			} break;

    			case REMOVE_GROUPJOB: {
    				if (request.getParameter("confirm") != null) {
						if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
						}
						
						boolean erase = false;
						if(request.getParameter("erase") != null && !request.getParameter("erase").isEmpty() && request.getParameter("erase").equals("true")) {
							erase = true;
						}
						
						GroupJobManager.removeGroupJob(request.getParameter("name"), erase, jm, _c);
						ObjectLock.unblockAll(ObjectLock.GROUP_JOBS_TYPE_OBJECT,  request.getParameter("name"));
						writeDocumentResponse(getLanguageMessage("advanced.groupjob.removed"), baseUrl);
					} else {
						writeDocumentQuestionYesNoCancel(_xhtml_out, getLanguageMessage("advanced.groupjob.question"), baseUrl+"?type=" + REMOVE_GROUPJOB + "&name=" + request.getParameter("name")+"&confirm=true&erase=true", baseUrl+"?type=" + REMOVE_GROUPJOB + "&name=" + request.getParameter("name")+"&confirm=true&erase=false", null);
					}
    			} break;
    			
    			case LAUNCH_GROUPJOB: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
    				boolean launched = GroupJobManager.launchGroupJob(request.getParameter("name"), _c, _bo, cm);
    				if (launched)
    					writeDocumentResponse(getLanguageMessage("advanced.groupjob.launched"), baseUrl);
    				else
    					writeDocumentResponse(getLanguageMessage("advanced.groupjob.launched.nojobs"), baseUrl);
    					
    			} break;
    			
    			case STOP_GROUPJOB: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
    				GroupJobManager.stopGroupJob(request.getParameter("name"), _c, _bo);
    				ObjectLock.block(ObjectLock.GROUP_JOBS_TYPE_OBJECT, request.getParameter("name"), "stop");
    				writeDocumentResponse(getLanguageMessage("advanced.groupjob.stopped"), baseUrl);
    			} break;
    			
    			case CANCEL_GROUPJOB: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
    				GroupJobManager.cancelGroupJob(request.getParameter("name"), _c, _bo);
    				ObjectLock.block(ObjectLock.GROUP_JOBS_TYPE_OBJECT, request.getParameter("name"), "cancel");
    				writeDocumentResponse(getLanguageMessage("advanced.groupjob.canceled"), baseUrl);
    			} break;
    			
    			case RESTART_GROUPJOB: {
    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.name"));
					}
    				GroupJobManager.restartGroupJob(request.getParameter("name"), _c, _bo, cm);
    				ObjectLock.unblockAll(ObjectLock.GROUP_JOBS_TYPE_OBJECT,  request.getParameter("name"));
    				writeDocumentResponse(getLanguageMessage("advanced.groupjob.restarted"), baseUrl);
    			} break;
	    	}
	    } catch (Exception _ex) {
	    	if (type == 1)
	    		writeDocumentError(_ex.getMessage());
	    	else if (type == STORE_GROUPJOB_GROUPING)
	    		writeDocumentError(_ex.getMessage(), baseUrl+"?type="+ADD_GROUPJOB_GROUPING);
	    	else	
	    		writeJSFuncResponse(_ex.getMessage(), "javascript:closeClientDialog"+request.getParameter("order")+"();");
	    } finally {
	    	if (type != JSON_GROUPJOBS && type != VIEW_GROUPJOB&& type != SELECT_JOBS)
	    		writeDocumentFooter();
	    }		
	}
	
 	public void printSectionHeader(PrintWriter _xhtml_out, String info) throws Exception {
		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/group_job_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.advanced.groupjob"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(info);
		_xhtml_out.println("</div>");
 	}
 	
 	public void fillSelects(ClientManager cm) throws Exception {
 		selectTemplateJob = new TreeMap<String, String>();
 		List<Map<String, Object>> templateJobs = TemplateJobManager.listTemplateJobs();
 		if (templateJobs != null && templateJobs.size()>0) {
 			for (Map<String, Object> templateJob : templateJobs) {
 				String name = (String) templateJob.get("name");
 				selectTemplateJob.put(name, name);	
 			}
 		}

 	 	selectTypeStep = new TreeMap<String, String>();
 		selectTypeStep.put(StepManager.TYPE_STEP_ADVANCED_STORAGE, getLanguageMessage("advanced.step.type.advanced_storage"));
 		selectTypeStep.put(StepManager.TYPE_STEP_BACKUP, getLanguageMessage("advanced.step.type.backup"));
 		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_APP, getLanguageMessage("advanced.step.type.script_app"));
 		selectTypeStep.put(StepManager.TYPE_STEP_SCRIPT_SYSTEM, getLanguageMessage("advanced.step.type.script_system"));
 		
 		msgTypeGroupJob = new TreeMap<String, String>();
 		msgTypeGroupJob.put(GroupJobManager.TYPE_TEMPLATEJOB, getLanguageMessage("advanced.groupjob.type.templatejob"));
 		msgTypeGroupJob.put(GroupJobManager.TYPE_MANUAL_SELECTION, getLanguageMessage("advanced.groupjob.type.manual"));
 		
 		List<String> clientNames = cm.getAllClientNames(null);
 		selectClients = new TreeMap<String, String>();
 		selectClients.put("", "--");
 		for (String name : clientNames) {
 			selectClients.put(name, name);
 		}
 		
 	}
 	
 	public void getJsonGroupJobs(HttpServletRequest request, HttpServletResponse response, LicenseManager lm, Configuration c) throws Exception {
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
 		try {
			List<Map<String, Object>> groupJobs = GroupJobManager.searchGroupJobs(page, rp, sortname, sortorder, query, qtype, c);
			Integer total = GroupJobManager.getTotalGroupJobs(query, qtype, c);
			String json = this.groupJobsToJSON(groupJobs, page, total, lm);
			PrintWriter out = response.getWriter();
			out.flush();
			out.print(json);
 		} catch (Exception ex) {
 			logger.error("Error intentando obtener el listado de group jobs con page:{} rp:{} sortname:{} sortorder:{} query:{} qtype:{}", new Object[]{page, rp, sortname, sortorder, query, qtype});
	    }
 	}
 	
 	public void printJSGrid(PrintWriter _xhtml_out, LicenseManager _lm) throws Exception {
 		_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/flexigrid.js\"></script>");
    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.cookie.js\"></script>");
    	pageJS+="var myFlex = $(\"#flexigridGroups\").flexigrid({\n";
    	pageJS+="	url: '"+baseUrl+"?type="+JSON_GROUPJOBS+"',\n";
    	pageJS+="dataType: 'json',\n";
    	pageJS+="colModel : [\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.name")+"', name : 'name', width : 140, sortable : true, align: 'left'},\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.type")+"', name : 'type', width : 70, sortable : true, align: 'left'},\n";
    	pageJS+="{display: '"+getLanguageMessage("backup.jobs.schedule")+"', name : 'schedule', width : 70, sortable : true, align: 'left'},\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.numjobs")+"', name : 'numjobs', width :85, sortable : true, align: 'center'},\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.start_date")+"', name : 'start', width : 90, sortable : true, align: 'left'},\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.end_date")+"', name : 'end', width : 90, sortable : true, align: 'left'},\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.status")+"', name : 'status', width : 135, sortable : true, align: 'left'},\n";
    	pageJS+="{display: ' ', name : 'actions', width : 130, sortable : false, align: 'left'}\n";
    	pageJS+="],\n";
    	pageJS+="buttons : [\n";
    	pageJS+="{name: '   "+getLanguageMessage("common.message.add")+"', onpress : addGroupJobGrouping, bclass : 'btnAdd'},\n";	
    	pageJS+="{name: '   "+getLanguageMessage("advanced.groupjob.select_all")+"', onpress : invertSelection, bclass : 'btnSelect'}\n";
    	pageJS+="],\n";
    	pageJS+="searchitems : [\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.name")+"', name : 'name'},\n";
    	pageJS+="{display: '"+getLanguageMessage("advanced.groupjob.date")+"', name : 'start'}\n";
    	pageJS+="],\n";
    	pageJS+="findtext: '"+getLanguageMessage("common.message.search")+"',\n";
    	pageJS+="procmsg : '"+getLanguageMessage("advanced.groupjob.grid.loading")+"',\n";
    	pageJS+="errormsg : '"+getLanguageMessage("advanced.groupjob.grid.error")+"',\n";
    	pageJS+="tableId: 'flexigridGroups',\n";
    	pageJS+="title: '"+getLanguageMessage("advanced.groupjobs")+"',\n";
    	pageJS+="sortname: 'name',\n";
    	pageJS+="sortorder: 'asc',\n";
    	pageJS+="usepager: true,\n";
    	pageJS+="useRp: true,\n";
    	pageJS+="onSuccess: checkColors,\n";
    	pageJS+="rp: 10,\n";
    	pageJS+="showTableToggleBtn: false,\n";
    	pageJS+="width: 'auto',\n";
    	pageJS+="height: 430\n";
    	pageJS+="});\n";
    	
    	pageJSFuncs+="function deleteGroupJobs() {\n";
    	pageJSFuncs+="		var grid = $('#flexigridGroups');\n";
    	pageJSFuncs+="		var groupJobNames = [];\n";
    	pageJSFuncs+="		$('.trSelected', grid).each(function() {\n";
    	pageJSFuncs+="			var id = $(this).attr('name');\n";
    	pageJSFuncs+="			id = id.substring(id.lastIndexOf('row')+3);\n";
    	pageJSFuncs+="			groupJobNames.push(id)\n";
    	pageJSFuncs+="		});\n";
    	pageJSFuncs+="		var iJobs = document.getElementById('groupJobNames');\n";
    	pageJSFuncs+="		for (var i=0; i<groupJobNames.length;i++) {\n";
    	pageJSFuncs+="			if (i == 0)\n";
    	pageJSFuncs+="				iJobs.value=jobIds[i];\n";
    	pageJSFuncs+="			else\n";
    	pageJSFuncs+="				iJobs.value+='-'+jobIds[i];\n";
    	pageJSFuncs+="		}\n";
    	pageJSFuncs+="		document.jobs.submit();\n";
    	pageJSFuncs+="}\n";
    	pageJSFuncs+="function invertSelection() {\n";
    	pageJSFuncs+="	var rows = $('table#flexigridGroups').find('tr').get();\n";
    	pageJSFuncs+="	$.each(rows,function(i,n) {\n";
    	pageJSFuncs+="		$(n).toggleClass('trSelected');\n";
    	pageJSFuncs+="	});\n";
    	pageJSFuncs+="}\n";
    	pageJSFuncs+=getRowColorCheckingJS();
 	}
 	
 	public String groupJobsToJSON(List<Map<String, Object>> groupJobs, Integer pag, Integer total, LicenseManager lm) throws Exception
	{
		String json = "{\"page\":"+pag+",\"total\":"+total;
		
		
		if (groupJobs != null && groupJobs.size() > 0) {
			json += ",\"rows\":[";
			int a = 0;
			for (Map<String, Object> groupJob : groupJobs) {
				if (a == 0) {
					json += "{\"id\":\""+groupJob.get("name")+"\"";
					a++;
				} else
					json += ",{\"id\":\""+groupJob.get("name")+"\"";
				
				if (ObjectLock.isBlock(ObjectLock.GROUP_JOBS_TYPE_OBJECT, (String) groupJob.get("name"), null) && "true".equals(groupJob.get("run")))
					json += "<input type='hidden' class='coloring'/>\"";
				json +=",\"cell\":[";
				json += "\""+groupJob.get("name")+"\"";
				json += ",\""+msgTypeGroupJob.get(groupJob.get("type"))+"\"";
				String schedule = "";
				if (groupJob.get("schedule") != null && !((String)groupJob.get("schedule")).isEmpty())
					schedule = (String) groupJob.get("schedule");
				json += ",\""+schedule+"\"";
				int numJobs = 0;
				if (groupJob.get("jobs") != null) {
					@SuppressWarnings("unchecked")
					Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
					numJobs = jobs.size();
				}
				json += ",\""+numJobs+"\"";
				String endtime = "";
				String startime = "";
				String alert = "";
				String status = "";
				if (groupJob.get("start") != null) {
					startime = (String) groupJob.get("start");
				}
				if (groupJob.get("end") != null) {
					endtime = (String) groupJob.get("end");
				}
				if (groupJob.get("alert") != null) {
					alert = (String)groupJob.get("alert");
				}
				if (groupJob.get("status") != null) {
					status = (String) groupJob.get("status");
				}
				json += ",\""+startime+"\"";
				json += ",\""+endtime+"\"";
				json += ",\"<div class='grid_"+alert+"'>"+status+"</div>\"";
				json += ",\""+this.getToolsGroupJob(groupJob, lm)+"\"";
				json += "]}";
			}
			json += "]";
		}
		json += "}";
		return json;
	}
	
	public String getToolsGroupJob(Map<String, Object> groupJob, LicenseManager lm) throws Exception
	{
		String tools = "";
		tools+="<a href='"+baseUrl+"?name=";
		tools+=groupJob.get("name");
		tools+="&type=";
		tools+=REMOVE_GROUPJOB;
		tools+="'><img src='/images/cross_16.png' title='";
    	tools+=getLanguageMessage("common.message.remove");
    	tools+="' alt='";
    	tools+=getLanguageMessage("common.message.remove");
    	tools+="'/></a>&nbsp;";
    	if(groupJob.get("run") == null || !"true".equals(groupJob.get("run"))) {
    		tools+="<a href='"+AdvancedBackup.baseUrl+"?type=";
	    	tools+=AdvancedBackup.ADD_GROUPJOB_BY_TEMPLATEJOB;
	    	tools+="&edit=true&templateJobName=";
	    	tools+=groupJob.get("templateJob");
	    	tools+="&name=";
	    	tools+=groupJob.get("name");
	        tools+="'><img src='/images/configuration_16.png' title='";
	        tools+=getLanguageMessage("common.message.edit");
	    	tools+="' alt='";
	    	tools+=getLanguageMessage("common.message.edit");
	    	tools+="'/></a>&nbsp;";
		}
    	
    	if(groupJob.get("run") != null && "true".equals(groupJob.get("run"))) {
    		if (!ObjectLock.isBlock(ObjectLock.GROUP_JOBS_TYPE_OBJECT, (String) groupJob.get("name"), null)) {
    	    	tools+="<a href='"+baseUrl+"?type=";
    	    	tools+=STOP_GROUPJOB;
    	    	tools+="&name=";
    	    	tools+=groupJob.get("name");
    	        tools+="'><img src='/images/control_pause_16.png' title='";
    	        tools+=getLanguageMessage("common.message.stop");
    	    	tools+="' alt='";
    	    	tools+=getLanguageMessage("common.message.stop");
    	    	tools+="'/></a>&nbsp;";
    	        tools+="<a href='"+baseUrl+"?type=";
    	        tools+=CANCEL_GROUPJOB;
    	        tools+="&name=";
    	        tools+=groupJob.get("name");
    	        tools+="'><img src='/images/control_stop_16.png' title='";
    	        tools+=getLanguageMessage("common.message.cancel");
    	    	tools+="' alt='";
    	    	tools+=getLanguageMessage("common.message.cancel");
    	    	tools+="'/></a>&nbsp;";
    		}
	    } else if(groupJob.get("return") != null && !"OK".equals(groupJob.get("return"))) {
	    	tools+="<a href='"+baseUrl+"?type=";
	        tools+=RESTART_GROUPJOB;
	        tools+="&name=";
	        tools+=groupJob.get("name");
	        tools+="'><img src='/images/control_restart_16.png' title='";
	        tools+=getLanguageMessage("common.message.restart");
	        tools+="' alt='";
	        tools+=getLanguageMessage("common.message.restart");
	        tools+="'/></a>&nbsp;";
	    } else {
        	tools+="<a href='"+baseUrl+"?type=";
        	tools+=LAUNCH_GROUPJOB;
        	tools+="&name=";
        	tools+=groupJob.get("name");
        	tools+="'><img src='/images/control_start_16.png' title='";
        	tools+=getLanguageMessage("common.message.start");
        	tools+="' alt='";
        	tools+=getLanguageMessage("common.message.start");
        	tools+="'/></a>&nbsp;";
	    }
		tools+="<a href='javascript:viewJobs(\\\""+groupJob.get("name")+"\\\");'><img src='/images/group_job_16.png' title='";
    	tools+=getLanguageMessage("advanced.groupjob.viewjobs");
    	tools+="' alt='";
    	tools+=getLanguageMessage("advanced.groupjob.viewjobs");
    	tools+="'/></a>";
    	
    	return tools;
	}
	
	public String getJSViewJobsDialog() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function viewJobs(name) {\n");
		_sb.append("	$('#nameGroup').val(name);\n");
		_sb.append("	$('#viewJobsDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n");
		_sb.append("	$('#viewJobsDialog').dialog( 'open' );\n");
		_sb.append("	$.ajax({\n");
	 	_sb.append("		url: '"+baseUrl+"?type="+VIEW_GROUPJOB+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: { name: name}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#viewJobsDialog').html(html);\n");
	 	_sb.append("	});\n");
	 	_sb.append("};");
	 	return _sb.toString();
	}
	
	
	public void getGroupJobListJobs(PrintWriter _xhtml_out, String nameGroupJob, Configuration conf, LicenseManager _lm) throws Exception {
		/*_xhtml_out.println("<div class=\"window\" style=\"width:100%;\">");
		_xhtml_out.println("<h2>");
		_xhtml_out.print(nameGroupJob+": "+getLanguageMessage("advanced.groupjob.jobs"));
        _xhtml_out.println("</h2>");
        _xhtml_out.print("<br/>");*/
        _xhtml_out.println("<fieldset style=\"margin-left:-30px;\">");
        _xhtml_out.println("<table style=\"font-size:12px;\">");
		
        Map<String, Object> groupJob = GroupJobManager.getGroupJob(nameGroupJob, conf, null);
        if (groupJob != null) {
        	if (groupJob.get("jobs") != null) {
        		@SuppressWarnings("unchecked")
				Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
        		if (!jobs.isEmpty()) {
	            	_xhtml_out.println("<tr>");
	            	_xhtml_out.println("<td>&nbsp;</td>");
	                _xhtml_out.print("<td>");
	                _xhtml_out.print("&nbsp;");
	                _xhtml_out.println("</td>");
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
	                int _offset=1;
	            	for (Integer order : jobs.keySet()) {
	            		Map<String, Object> _job = jobs.get(order);
	            		_xhtml_out.print("<tr");
	                	if (_job.get("level") == null || ((String)_job.get("level")).isEmpty() || _job.get("client") == null || ((String)_job.get("client")).isEmpty())
	                		_xhtml_out.print(" style=\"background-color:#F2C291;\" ");
	                	else if(_offset % 2 == 0) {
	                		_xhtml_out.print(" class=\"highlight\"");
	                	}

	                	_xhtml_out.println(">");
	                	_xhtml_out.println("<td>&nbsp;</td>");
	                	_xhtml_out.println("<td>");
	                	_xhtml_out.print(order);
	                	_xhtml_out.println(". </td>");
                    	_xhtml_out.print("<td>");
                    	_xhtml_out.print(_job.get("name"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if (_job.get("level") != null && !((String)_job.get("level")).isEmpty())
							_xhtml_out.print(_job.get("level"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if (_job.get("files") != null)
							_xhtml_out.print(_job.get("files"));
						else {
							_xhtml_out.print(0);	
						}
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if (_job.get("size") != null)
							_xhtml_out.print(_job.get("size"));
						else {
							_xhtml_out.print(0);	
						}
						_xhtml_out.println("</td>");
						if (_job.get("status") != null) {
							_xhtml_out.print("<td class='"+_job.get("alert")+"'>");
							_xhtml_out.print(_job.get("status"));
						} else {
							_xhtml_out.print("<td>");
							_xhtml_out.print("--");
						}
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if (_job.get("end") != null && !((String)_job.get("end")).isEmpty() && !((String)_job.get("end")).equals("null"))
							_xhtml_out.print(_job.get("end"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if (_job.get("client") != null)
							_xhtml_out.print(_job.get("client"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if(_job.get("clientid") != null && _job.get("client") != null &&
								!((String)_job.get("clientid")).isEmpty() && !((String)_job.get("client")).isEmpty()) {
							_xhtml_out.print("<a href=\"/admin/BackupJobs?clientId=");
							_xhtml_out.print(_job.get("clientid"));
							_xhtml_out.print("&clientName=");
							_xhtml_out.print(_job.get("client"));
							_xhtml_out.print("\"><img src=\"/images/cog_16.png\" title=\"");
	                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
	                    	_xhtml_out.print("\" alt=\"");
	                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
	                    	_xhtml_out.println("\"/></a>");
                    		_xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
							_xhtml_out.print(BackupJobs.EDITJOB);
							_xhtml_out.print("&jobName=");
							_xhtml_out.print(_job.get("name"));
							_xhtml_out.print("&clientName=");
							_xhtml_out.print(_job.get("client"));
							_xhtml_out.print("&clientId=");
							_xhtml_out.print(_job.get("clientid"));
							_xhtml_out.print("\"><img src=\"/images/cog_edit_16.png\" title=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
	                    	_xhtml_out.print("\" alt=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
	                    	_xhtml_out.println("\"/></a>");
	                    	if(!"OK".equals(_job.get("status")) && _job.get("id") != null) {
                    			_xhtml_out.print("<a href=\"/admin/BackupJobs?type=");
                                _xhtml_out.print(BackupJobs.RESTARTJOB);
                                _xhtml_out.print("&nameJob=");
                                _xhtml_out.print(_job.get("name"));
                                _xhtml_out.print("&clientName=");
								_xhtml_out.print(_job.get("client"));
								_xhtml_out.print("&clientId=");
								_xhtml_out.print(_job.get("clientid"));
                                _xhtml_out.print("&jobId=");
                                _xhtml_out.print(_job.get("id"));
                                _xhtml_out.print("\"><img src=\"/images/control_restart_16.png\" title=\"");
			                	_xhtml_out.print(getLanguageMessage("common.message.restart"));
			                	_xhtml_out.print("\" alt=\"");
			                	_xhtml_out.print(getLanguageMessage("common.message.restart"));
			                	_xhtml_out.println("\"/></a>");
							}
	                    	if (_job.get("id") != null) {
	                    		_xhtml_out.print("<a href='javascript:viewLog(\""+_job.get("id")+"\",\""+_job.get("clientid")+"\");' >");
		                    	_xhtml_out.print("<img src=\"/images/book_16.png\" title=\"");
			                	_xhtml_out.print(getLanguageMessage("common.message.view"));
			                	_xhtml_out.print("\" alt=\"");
			                	_xhtml_out.print(getLanguageMessage("common.message.view"));
			                	_xhtml_out.println("\"/></a>");
	                    	}
						}
						_xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	                    _offset++;
	            	}
	            } else {
	            	_xhtml_out.println("<tr>");
	            	_xhtml_out.println("<td>");
	            	_xhtml_out.println(getLanguageMessage("advanced.groupjob.no_jobs"));
	            	_xhtml_out.println("</td>");
	                _xhtml_out.println("</tr>");
	            }
        	} else {
        		_xhtml_out.println("<tr>");
            	_xhtml_out.println("<td>");
            	_xhtml_out.println(getLanguageMessage("advanced.groupjob.no_jobs"));
            	_xhtml_out.println("</td>");
                _xhtml_out.println("</tr>");
        	}
        } else {
        	_xhtml_out.println("<tr>");
        	_xhtml_out.println("<td>");
        	_xhtml_out.println(getLanguageMessage("advanced.groupjob.notfound"));
        	_xhtml_out.println("</td>");
            _xhtml_out.println("</tr>");
        }
            
        _xhtml_out.println("</table>");
        _xhtml_out.print("<br/>");
        //_xhtml_out.println("</fieldset>");
    	_xhtml_out.println("<div class=\"clear\"/></div>");
    	//_xhtml_out.print("</div>");
	}
	
 	public static String getJSAddJob() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function addJob() {\n");
 		_sb.append("	var ni = document.getElementById('jobs');\n");
 		_sb.append("	var numi = document.getElementById('numJobs');\n");
 		_sb.append("	var num = parseInt(document.getElementById('numJobs').value) + 1;\n");
 		_sb.append("	numi.value = num;\n");
 		_sb.append("	var divIdName = 'clijo'+num;\n");
 		_sb.append("	var newdiv = document.createElement('div');\n");
 		_sb.append("	newdiv.setAttribute('id','div'+divIdName);\n");
 		_sb.append("	newdiv.innerHTML = '");
 		_sb.append(getHtmlSelectJobsJS("divIdName", "num"));
 		_sb.append("';\n");
 		_sb.append("	ni.appendChild(newdiv);\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	}

 	public static String getHtmlSelectJobs(String inputId, Integer order) {
 		StringBuilder _sb = new StringBuilder();
 		
 		_sb.append("<div style=\"width:150px;margin-right:50px;float:left;\" >"+order+".</div>");
 		
 		_sb.append("<select style=\"width:150px;margin-right:50px;float:left;\" class=\"form_select\" name=\""+inputId+"-client\" style=\"width:150px;margin-right:50px;\" id=\""+inputId+"-client\" onChange=\"updateScriptCombo(this.value, '"+inputId+"');\" >\n");
		for (String key : selectClients.keySet()) {
			String text = selectClients.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	_sb.append(">"+text+"</option>\n");
		}
		_sb.append("</select>\n");
		
		_sb.append("<div style=\"width:150px;margin-right:50px;float:left;\" id=\"divjob"+inputId+"\" name=\"divjob"+inputId+"\">");
    	_sb.append("</div>");
    	
    	_sb.append("<div class=\"clear\"/></div>");
    	return _sb.toString();
 	}
 	
 	public static String getHtmlSelectJobsJS(String inputId, String order) {
 		StringBuilder _sb = new StringBuilder();
 		
 		_sb.append("<div style=\"width:150px;margin-right:50px;float:left;\" >'+parseInt("+order+")+'.</div>");
 		
 		_sb.append("<select style=\"width:150px;margin-right:50px;float:left;\" class=\"form_select\" name=\"'+"+inputId+"+'-client\" style=\"width:150px;margin-right:50px;\" id=\"'+"+inputId+"+'-client\" onChange=\"updateScriptCombo(this.value, this.id);\">");
		for (String key : selectClients.keySet()) {
			String text = selectClients.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	_sb.append(">"+text+"</option>");
		}
		_sb.append("</select>");
		
		_sb.append("<div style=\"width:150px;margin-right:50px;float:left;\" id=\"divjob'+"+inputId+"+'\" name=\"divjob'+"+inputId+"+'\">");
    	_sb.append("</div>");
    	
    	_sb.append("<div class=\"clear\"/></div>");
    	return _sb.toString();
 	}
 	
 	public static String getJSUpdateScriptCombo() {
 		StringBuilder _sb = new StringBuilder();
 		_sb.append("function updateScriptCombo(clientName, id) {\n");
 		_sb.append("	if (id.indexOf('-') > -1)\n");
 		_sb.append("		id = id.substr(0,id.indexOf('-'));\n");
 		_sb.append("	$.ajax({\n");
 		_sb.append("		url: '/admin/AdvancedGroupJob?type="+AdvancedGroupJob.SELECT_JOBS+"',\n");
 		_sb.append("		cache: false,\n");
 		_sb.append("		data: { clientName: clientName, id : id+'-job' }\n");
 		_sb.append("	}).done(function( html ) {\n");
 		_sb.append("		$('#divjob'+id).html(html);\n");
 		_sb.append("	});\n");
 		_sb.append("}\n");
 		return _sb.toString();
 	}
 	
 	public static void getSelectJobs(PrintWriter _xhtml_out,  String clientName, String id, JobManager jm, ClientManager cm) {
 		try {
 			Integer clientId = cm.getClientId(clientName);
 			List<Map<String, String>> jobs = jm.getProgrammedClientJobs(clientId);
 			_xhtml_out.print("<select class=\"form_select\" name=\""+id+"\" id=\""+id+"\"  >\n");
 			for (Map<String, String> job : jobs) {
 				_xhtml_out.print("<option value=\""+job.get("name")+"\" ");
 				_xhtml_out.print(">"+job.get("name")+"</option>\n");
 			}
 			_xhtml_out.print("</select>\n");
 		} catch (Exception ex) {
 			logger.error("Error obteniendo el select dinámico de jobs");
 		}
 	}
 	
 	private void writeDocumentQuestionYesNoCancel(PrintWriter _xhtml_out, String message, String href_yes, String href_no, String href_deny) throws IOException {
		if(href_yes == null || href_yes.isEmpty()) {
			href_yes = "#";
		}
		if(href_deny == null || href_deny.isEmpty()) {
			href_deny = "javascript:history.go(-1);";
		}
		else{
			href_deny="javascript:sendForm('"+href_deny+"');";
		}
		
		_xhtml_out.println("<br/><br/>");
		_xhtml_out.println("<div class=\"window_message\">");
        _xhtml_out.print("<h2>");
        _xhtml_out.print(getLanguageMessage("common.message.question"));
        _xhtml_out.println("</h2>");
        _xhtml_out.print("<div class=\"subinfo\">");
		if(message != null) {
			_xhtml_out.print(message);
		} else {
			_xhtml_out.print(" -- ");
		}
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"clear\"></div>");
        _xhtml_out.print("<a class=\"button\" href=\"");
        _xhtml_out.print(href_yes);
        _xhtml_out.print("\">");
        _xhtml_out.print(getLanguageMessage("common.message.yes"));
        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
        _xhtml_out.print("<a class=\"button\" href=\"");
        _xhtml_out.print(href_no);
        _xhtml_out.print("\">");
        _xhtml_out.print(getLanguageMessage("common.message.no"));
        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
        _xhtml_out.print("<a class=\"button\" href=\"");
        _xhtml_out.print(href_deny);
        _xhtml_out.print("\">");
        _xhtml_out.print(getLanguageMessage("common.message.cancel"));
        _xhtml_out.print("<img src=\"/images/cross_16.png\"/></a>");
        _xhtml_out.println("<div class=\"clear\"></div>");
		_xhtml_out.print("</div>");
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
 	
	public String getRowColorCheckingJS() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("function checkColors() {\n");
		sb.append("	var rows = $('table#flexigridGroups').find('tr').get();");
		sb.append("	$.each(rows,function(i,n) {\n");
		sb.append("		var st = $(n).find('.coloring').get()\n");
		sb.append("		if (st && st.length>0) {\n");
		sb.append("			$(n).css('background-color', '#F2C291');\n");
		sb.append("		}\n");
		sb.append("	});\n");
		sb.append("}\n");
		return sb.toString();
	}
}
