package com.whitebearsolutions.imagine.wbsairback.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.whitebearsolutions.imagine.wbsairback.RoleManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.util.ObjectLock;
import com.whitebearsolutions.imagine.wbsairback.util.jQgridParameters;
import com.whitebearsolutions.util.Configuration;


public class BackupJobsExtended extends WBSImagineServlet {
	
	static final long serialVersionUID = 20071109L;
	public final static int CANCELJOB = 2;
	public final static int JOBVOLUMES = 3;
	public final static int STOPJOB = 4;
	public final static int RESTARTJOB = 5;
	public final static int COPYJOBS = 6;
	public static final int JOB_LIST_JSON = 3654332;
	
	private int type;
	Map<String, String> selectTypeStep = null;
	public final static String baseUrl = "/admin/"+BackupJobsExtended.class.getSimpleName();


	@SuppressWarnings("unchecked")
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (!this.securityManager.isLogged()) {
			response.sendRedirect("/admin/Login");
			this.redirected = true;
		}

		// Compreba que el tipo sea numérico y no nulo
		// si no, devuelve 1 por defecto
		this.type = (request.getParameter("type") != null && request
				.getParameter("type").matches("\\d+")) ? Integer
				.parseInt(request.getParameter("type")) : 1;
		try {
			response.setContentType("text/html");
			PrintWriter _xhtml_out = response.getWriter();
			Configuration _c = this.sessionManager.getConfiguration();
			JobManager _jm = new JobManager(_c);
			ClientManager _cm = new ClientManager(_c);
			List<String> categories = null;
			if(!this.securityManager.isAdministrator() && !this.securityManager.isRole(RoleManager.roleGlobalOperator)) {
				if((this.securityManager.isRole(RoleManager.roleOperator) || this.securityManager.isRole(RoleManager.roleUser) ) && this.securityManager.hasUserCategory()) {
					categories = this.securityManager.getUserCategories();
				}
				if (categories == null || categories.isEmpty()) {
					throw new Exception(getLanguageMessage("common.message.no_privilegios"));
				}
			}
			List<String> clients = new ArrayList<String>();
			for(Map<String, String> client : _cm.getAllClients(null, categories, false)) if (!client.get("id").equals("--")) clients.add(client.get("id"));

			// Inicializamos los parámetros
			jQgridParameters params = new jQgridParameters();
			
			// Metemos los parámetros en la clase
			try{params.setPage(Integer.parseInt(request.getParameter("page")));} catch(Exception ex){}
			try{params.setRows(Integer.parseInt(request.getParameter("rows")));} catch(Exception ex){}
			try{params.setSearch(Boolean.parseBoolean(request.getParameter("_search")));} catch(Exception ex){}
			try{params.setSidx(request.getParameter("sidx").toString());} catch(Exception ex){}
			try{params.setSord(request.getParameter("sord").toString());} catch(Exception ex){}
			try{params.setSearchString(request.getParameter("searchOper").toString());} catch(Exception ex){}
			try{params.setSearchField(request.getParameter("searchField").toString());} catch(Exception ex){}
			try{params.setSearchOper(request.getParameter("searchOper").toString());} catch(Exception ex){}	
			
			switch (this.type) {
				case JOB_LIST_JSON: {
					
					// Inicializamos la clase Json de Google
					Gson gson = new GsonBuilder().create();
					
					// Convertimos el QueryString "Filters" a JSON para poder meterlo en la clase
					jQgridParameters.Filters filters = gson.fromJson(request.getParameter("filters"), jQgridParameters.Filters.class);
					
					// Lo metemos en la clase de parámetros
					params.setFilters(filters);
					
					// Obtenemos el listado de Jobs en formato JSON
					String json = gson.toJson(_jm.getArchivedJobs(0, clients, params));
					
					response.setContentType("application/json");
					PrintWriter out = response.getWriter();
					
					out.print(json);
					
					out.flush();
				}
					break;
				case CANCELJOB: {
					Integer jobId;
	    			try {
	    				jobId = Integer.parseInt(request.getParameter("jobId"));
	    			} catch(NumberFormatException _ex) {
	    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
	    			}

					Map<String, Object> lista =_jm.getArchivedJobs(jobId, clients, params);
					List<Map<String, String>> job = (List<Map<String, String>>) lista.get("rows");
	    			if(!this.securityManager.checkCategory(_cm.getClientCategories(job.get(0).get("c.name")))) {
		    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
	    			}
	    			
	    			if("true".equals(job.get(0).get("run"))) {
		    			if (!ObjectLock.isBlock(ObjectLock.JOBS_TYPE_OBJECT, jobId.toString(), null) 
		    					&& job.get(0).get("canceled") == null) {
		        			BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		        			_bo.cancelJob(jobId);
		        			_xhtml_out.println(getLanguageMessage("common.message.canceled"));
		    			} else {
		    				_xhtml_out.println(getLanguageMessage("backup.jobsextended.alreadycanceled"));
		    			}
	    			} else {
	    				_xhtml_out.println(getLanguageMessage("backup.jobsextended.alreadycanceled"));
	    			}
				}
					break;
				case STOPJOB: {
	    			Integer jobId = null;
	    			try {
	    				jobId = Integer.parseInt(request.getParameter("jobId"));
	    			} catch(NumberFormatException _ex) {
	    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
	    			}

					Map<String, Object> lista =_jm.getArchivedJobs(jobId, clients, params);
					List<Map<String, String>> job = (List<Map<String, String>>) lista.get("rows");
	    			if(!this.securityManager.checkCategory(_cm.getClientCategories(job.get(0).get("c.name")))) {
		    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
	    			}
	    			
	    			if("true".equals(job.get(0).get("run"))) {
		    			if (job.get(0).get("run").toLowerCase().equals("true")
		    					&& !ObjectLock.isBlock(ObjectLock.JOBS_TYPE_OBJECT, jobId.toString(), null) 
		    					&& job.get(0).get("canceled") == null) {
		    				BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		        			_bo.stopJob(jobId);
		        			_xhtml_out.println(getLanguageMessage("common.message.stopped"));
		    			} else {
		    				_xhtml_out.println(getLanguageMessage("backup.jobsextended.alreadystopped"));
		    			}
	    			} else {
	    				_xhtml_out.println(getLanguageMessage("backup.jobsextended.alreadystopped"));
	    			}
				}
					break;
	    		case RESTARTJOB: {
	    			int jobId;
	    			
	    			try {
	    				jobId = Integer.parseInt(request.getParameter("jobId"));
	    			} catch(NumberFormatException _ex) {
	    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
	    			}

					Map<String, Object> lista =_jm.getArchivedJobs(jobId, clients, params);
					List<Map<String, String>> job = (List<Map<String, String>>) lista.get("rows");
	    			if(!this.securityManager.checkCategory(_cm.getClientCategories(job.get(0).get("c.name")))) {
		    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
	    			}
	    			
	    			if(!"OK".equals(job.get(0).get("return"))) {
	    				BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
	    				_bo.restartIncompleteJob(jobId);
	    				_xhtml_out.println(getLanguageMessage("common.message.started"));
	    			} else {
	    				_xhtml_out.println(getLanguageMessage("backup.jobsextended.cannotrestart"));
	    			}
				}
				break;
	    		case JOBVOLUMES: {
	    			int jobId = 0, _offset = 0;
	    			try {
	    				jobId = Integer.parseInt(request.getParameter("jobId"));
	    			} catch(NumberFormatException _ex) {
	    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
	    			}
	    			List<Map<String, String>> volumes = _jm.getVolumesForJob(jobId);
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
        	    	_xhtml_out.flush();
	    			break;
	    		}
	    		case COPYJOBS: {
	    			int jobId = 0, _offset = 0;
	    			try {
	    				jobId = Integer.parseInt(request.getParameter("jobId"));
	    			} catch(NumberFormatException _ex) {
	    				_xhtml_out.println(getLanguageMessage("backup.jobs.exception.invalid_job"));
	    				_xhtml_out.flush();
		    			break;
	    			}
	    			
	    			String type = request.getParameter("jobtype");
	    			
	    			if (type == null || !type.equals("c")) {
	    				_xhtml_out.println(getLanguageMessage("backup.jobs.not.copy.job"));
	    				_xhtml_out.flush();
		    			break;
	    			}
	    				
	    			Map<String, String> _job = _jm.getCopyJobOfCoordinator(jobId);
	    			
                    _xhtml_out.println("<table>");
                    if(_job != null && !_job.isEmpty()) {
                    	_xhtml_out.println("<tr>");
    	            	_xhtml_out.println("<td>&nbsp;</td>");
                        _xhtml_out.print("<td class=\"title\">");
                        _xhtml_out.print(getLanguageMessage("backup.jobs.name"));
                        _xhtml_out.println("</td>");
                        _xhtml_out.print("<td class=\"title\">");
                        _xhtml_out.print(getLanguageMessage("backup.jobs.level"));
                        _xhtml_out.println("</td>");
                        _xhtml_out.print("<td class=\"title\">");
                        _xhtml_out.print(getLanguageMessage("backup.jobs.type"));
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
                        _xhtml_out.print(getLanguageMessage("backup.jobs.start_date"));
                        _xhtml_out.println("</td>");
                        _xhtml_out.print("<td class=\"title\">");
                        _xhtml_out.print(getLanguageMessage("backup.jobs.end_date"));
                        _xhtml_out.println("</td>");
                        _xhtml_out.print("<td class=\"title\">");
                        _xhtml_out.print(getLanguageMessage("backup.summary.client"));
                        _xhtml_out.println("</td>");
    	                _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
    	                _xhtml_out.println("</tr>");
	            		_xhtml_out.print("<tr");
	                	if (_job.get("level") == null || ((String)_job.get("level")).isEmpty() || _job.get("client") == null || ((String)_job.get("client")).isEmpty())
	                		_xhtml_out.print(" style=\"background-color:#F2C291;\" ");
	                	else if(_offset % 2 == 0) {
	                		_xhtml_out.print(" class=\"highlight\"");
	                	}

	                	_xhtml_out.println(">");
	                	_xhtml_out.println("<td>");
	                	_xhtml_out.print(_job.get("id"));
	                	_xhtml_out.println("</td>");
                    	_xhtml_out.print("<td>");
                    	_xhtml_out.print(_job.get("name"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if (_job.get("level") != null && !((String)_job.get("level")).isEmpty())
							_xhtml_out.print(_job.get("level"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						if (_job.get("type") != null && !((String)_job.get("type")).isEmpty())
							_xhtml_out.print(_job.get("type"));
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
						if (_job.get("start") != null && !((String)_job.get("start")).isEmpty() && !((String)_job.get("start")).equals("null"))
							_xhtml_out.print(_job.get("start"));
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
    	            } else {
    	            	_xhtml_out.println("<tr>");
    	            	_xhtml_out.println("<td>");
    	            	_xhtml_out.println(getLanguageMessage("advanced.groupjob.no_jobs"));
    	            	_xhtml_out.println("</td>");
    	                _xhtml_out.println("</tr>");
    	            }
                    _xhtml_out.println("</table>");
                    _xhtml_out.println("<br/>");
        	    	_xhtml_out.flush();
	    			break;
	    		}
				default: {
					// Pinta la cabecera
					writeDocumentHeader();
	                _xhtml_out.println("<h1>");
					_xhtml_out.print("<img src=\"/images/cog_32.png\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("backup.extended.jobs"));
					_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("backup.extended.jobs.info"));
	    			_xhtml_out.println("</div>");
	    			_xhtml_out.print(HtmlFormUtils.printJsApprise());
					_xhtml_out.print(HtmlFormUtils.printJSJqgridHeader(this.messagei18N.getLocale()));
					if (this.messagei18N.getLocale().getLanguage().equals("es")) {
						_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.ui.datepicker-es.js\"></script>\n");
						pageJS+="$.datepicker.setDefaults( $.datepicker.regional[ 'es' ] );\n";
					}
					
					// jQuery Dialog para uso variado
	    	    	pageJS+="var wHeight = $(window).height();\n";
	    	    	pageJS+="var dHeight = wHeight * 0.8;\n";
	    	    	pageJS+="var wWidth = $(window).width();\n";
	    	    	pageJS+="var dWidth = wWidth * 0.9;\n";
					pageJS += "var $popupDialog = $('<div></div>').dialog({\n"
								+ "autoOpen: false,"
								+ "width: 700,"
								+ "height: 'auto',"
								+ "modal: true,"
								+ "hide: 'fade',"
								+ "beforeClose: function(event, ui){$(this).empty();},"
					+ "});\n";
					
					_xhtml_out.println("<div id='listadoJobs' style=\"margin:20px auto;width:94%;clear:both;\">"
								+ "<table id='tablaJobs' style='margin-left:0px;margin-right:0px;'></table>"
								+ "<div id='pager'></div>"
					+ "</div>");
					
					_xhtml_out.println("<script type='text/javascript'>$.jgrid.no_legacy_api = true;$.jgrid.useJSON = true;</script>");
					// Obtiene el log de un job
					pageJS += "function getLog(_url) {\n"
							+ "$('#logs').remove();\n"
							+ "$('#ViewTbl_tablaJobs').after('<div id=\"logs\" style=\""
							+ "height: 380px;"
							+ "width: 100%;"
							+ "overflow: auto;"
							+ "font-family:Courier;"
							+ "\"></div>');\n"
							+ "$.getJSON(_url, function(data){\n"
								+ "var items = [];\n"
								+ "$.each(data, function(key, val) {\n"
									+ "items.push('<tr><td width=\"180px\">'+val[\"time\"]+'</td><td>'+val[\"logtext\"]+'</td></tr>');\n"
								+ "});\n"
								+ "$('<table/>', {\n"
									+ "'id': 'job-log',\n"
									+ "html: '<thead><tr><th colspan=\"2\">LOG</th></tr></thead><tbody>'+items.join('')+'</tbody>'\n"
								+ "}).appendTo('#logs');\n"
						+ "});"
					+ "}";
					/*
					 * jQgrid
					 */
					pageJS += "$estados = '"
							+ "Canceled by user:Canceled by user;"
							+ "Completed successfully:Completed successfully;"
							+ "Terminated with errors:Terminated with errors;"
							+ "Fatal error:Fatal error;"
							+ "Running:Running"
							+ "';";
					pageJS += "$niveles = '"
							+ "F:F;"
							+ "I:I;"
							+ "D:D;"
							+ "B:B"
							+ "';";
					pageJS += "$tipos = '"
							+ "B:B;"
							+ "R:R;"
							+ "c:c;"
							+ "C:C;"
							+ "M:M"
							+ "g:g"
							+ "';";
					pageJS += "$tabla = $('#tablaJobs');\n";
					pageJS += "$tabla.jqGrid({\n"
							+ "url: '" + baseUrl + "?type=" + JOB_LIST_JSON + "'\n,"
							+ "datatype: 'json',\n"
					// Nombres de columnas jQgrid
							+ "colNames: ['"
								+ getLanguageMessage("Id") + "','"
								+ getLanguageMessage("backup.summary.client") + "','"
								+ getLanguageMessage("backup.jobs.name") + "','"
								+ getLanguageMessage("backup.jobs.type") + "','"
								+ getLanguageMessage("backup.jobs.level") + "','"
								+ getLanguageMessage("backup.jobs.status") + "','"
								+ getLanguageMessage("backup.jobs.total_size") + "','"
								+ getLanguageMessage("backup.jobs.speed") + "','"
								+ getLanguageMessage("backup.jobs.start_date") + "','"
								+ getLanguageMessage("backup.jobs.end_date") + "','"
								+ getLanguageMessage("backup.jobs.exp_date") + "','"
								+ "','"
								// hidden clientId, realClientId
							+ "'],\n"
					// Parámetros de columnas jQgrid
							+ "colModel:["
								+ "{ name: 'j.jobid', width: 25, sortable: true, align: 'center', key: true, sorttype: 'int'},"
								+ "{ name: 'c.name', width: 95, sortable: true, align: 'left'},"
								+ "{ name: 'j.name', width: 120, sortable: true, align: 'left'},"
								+ "{ name: 'j.type', width: 40, sortable: true, align: 'center', stype: 'select', searchoptions: {sopt: ['cn'], value: ':Any;' + $tipos}},"
								+ "{ name: 'j.level', width: 40, sortable: true, align: 'center', stype: 'select', searchoptions: {sopt: ['cn'], value: ':Any;' + $niveles}},"
								+ "{ name: 's.jobstatuslong', width: 135, align: 'center', stype: 'select', searchoptions: {sopt: ['cn'], value: ':Any;' + $estados}},"
								+ "{ name: 'size', width: 40, sortable: true, sorttype:'float', align: 'left', search: false, formatter:sizeFormatter},"
								+ "{ name: 'speed', width: 40, sortable: true, sorttype:'float', align: 'left', search: false, formatter:speedFormatter},"
								+ "{ name: 'j.starttime', width: 85, sortable: true, sorttype: 'date', align: 'center', formatoptions: {newformat:'d/m/Y H:i:s'}, datefmt: 'd/M/Y H:i:s', searchoptions: { sopt: ['eq', 'ne', 'ge', 'le'], dataInit: function (elem) { $(elem).datepicker({ dateFormat: 'dd/mm/yy',showButtonPanel: false}) }, dataEvents: [{ type: 'change', fn: function(e) {$tabla[0].triggerToolbar();}}]}},"
								+ "{ name: 'j.endtime', width: 85, sortable: true, sorttype: 'date', align: 'center', formatoptions: {newformat:'d/m/Y H:i:s'}, datefmt: 'd/M/Y H:i:s', searchoptions: { sopt: ['eq', 'ne', 'ge', 'le'], dataInit: function (elem) { $(elem).datepicker({ dateFormat: 'dd/mm/yy',showButtonPanel: false }) }, dataEvents: [{ type: 'change', fn: function(e) {$tabla[0].triggerToolbar();}}]}},"
								+ "{ name: 'exp', width: 85, sortable: true, sorttype: 'date', align: 'center', search: false, formatoptions: {newformat:'d/m/Y H:i:s'}, datefmt: 'd/M/Y H:i:s', searchoptions: { sopt: ['eq', 'ne', 'ge', 'le'], dataInit: function (elem) { $(elem).datepicker({ dateFormat: 'dd/mm/yy',showButtonPanel: false }) }, dataEvents: [{ type: 'change', fn: function(e) {$tabla[0].triggerToolbar();}}]}},"
								+ "{ name: 'realClientId', hidden: true},"
								+ "{ name: 'clientId', hidden: true}"
							+ "],\n"
							+ "postData: {"
								+ "filters: JSON.stringify({"
									+ "groupOp: 'AND',"
									+ "rules: ["
										+ "{field:'j.starttime', op:'ge', data:''},"
										+ "{field:'j.starttime', op:'le', data:''}"
									+ "]"
								+ "})"
							+ "},"
					// Ajustes del jQgrid
							+ "ignoreCase: true,\n"
							+ "paging: true,\n"
							+ "pager: '#pager',\n"
							+ "multiselect: false,\n"
							+ "toppager: true,\n"
							+ "sortname: 'j.starttime',\n"
							+ "sortorder: 'desc',\n"
							+ "width: $('#listadoJobs').width(),\n"
							+ "height: 'auto',\n"
							+ "hidegrid:false,"
							+ "rowNum: 10,\n"
							+ "rowList: [5, 10, 25, 50, 100],\n"
							+ "jsonReader: { id: 'j.jobid', total: 'total', records: 'records', root: 'rows', page: 'page', repeatitems: false },\n"
							+ "caption:'"+getLanguageMessage("backup.jobs")+"',\n"
							+ "emptyDataText:''\n"
					+ "\n});\n";
					
					/*
					 * Configuración del Toolbar jQgrid
					 */
					pageJS += "$tabla.jqGrid('navGrid', '#tablaJobs_toppager',\n"
							+ "{\n"
								// Botones por defecto habilitados
								+ "view: false, add: false, edit: false, search: true, del: false, refresh: true,"
								// Texto de los botones por defecto
								+ "viewtext: 'Log', searchtext: '"+getLanguageMessage("common.message.filter")+"',deltext: '"+getLanguageMessage("common.message.delete")+"',refreshtext: '"+getLanguageMessage("common.message.refresh")+"',\n"
								// Reemplazamos los iconos de los botones por defecto
								+ "viewicon: 'wbs-icon-16-book',searchicon: 'wbs-icon-16-find',refreshicon: 'wbs-icon-16-arrow_refresh',\n"
								// Cambia la función de reset filters por reload grid, de ésta forma se carga el grid de nuevo
								+ "beforeRefresh: function() {\n"
									+ "$tabla.setGridParam({datatype:'json'}).trigger('reloadGrid');\n"
								+ "}\n"
							+ "},\n"
								// El orden es: 
								// edit, add, delete, search, view
							+ "{},{},{},"
							+ "{multipleSearch: true, multipleGroup: false, stringResult: true, ignoreCase: true},\n"
							// LOG
							+ "{})\n"
							+ ".navSeparatorAdd('#tablaJobs_toppager',{})\n"
							+ ".navSeparatorAdd('#tablaJobs_toppager',{})\n"
							+ ".navSeparatorAdd('#tablaJobs_toppager',{})\n"
							// LOG de siempre
							+ ".navButtonAdd('#tablaJobs_toppager',{"
								+ "caption:'"+getLanguageMessage("common.message.view")+"',"
								+ "buttonicon:'wbs-icon-16-book',"
								+ "position: 'last',"
								+ "onClickButton: function(e) {"
									+ "$jobId = $tabla.jqGrid ('getGridParam', 'selrow');\n"
									+ "if($jobId) {\n"
										+ "$clientId = $tabla.jqGrid('getCell', $jobId, 'realClientId');\n"
										+ "viewLog($jobId, $clientId);"
									+ "} \n"
								+ "}"
								+ "})\n"
							// VOLUMENES
							+ ".navButtonAdd('#tablaJobs_toppager',{"
								+ "caption:'"+getLanguageMessage("common.message.volumes")+"',"
								+ "buttonicon:'wbs-icon-16-database_gear',"
								+ "position: 'last',"
								+ "onClickButton: function(e) {"
									+ "$jobId = $tabla.jqGrid ('getGridParam', 'selrow');\n"
									+ "if($jobId) {\n"
										+ "	$.ajax({\n"
										+ "		url: '"+baseUrl+"?type="+JOBVOLUMES+"',\n"
										+ "		cache: false,\n"
										+ "		data: {'jobId' : $jobId}\n"
										+ "	}).done(function( html ) {\n"
										+ "		$popupDialog.html(html);\n"
										+ "		$popupDialog.dialog('open').dialog('option', 'title', '"+getLanguageMessage("common.message.volumes")+"');\n"
										+ "	});\n"
									+ "} \n"
									+ "} \n"
								+ "})\n"
							// Copy Jobs
							+ ".navButtonAdd('#tablaJobs_toppager',{"
								+ "caption:'"+getLanguageMessage("common.message.copy.jobs")+"',"
								+ "buttonicon:'wbs-icon-16-copy_jobs',"
								+ "position: 'last',"
								+ "onClickButton: function(e) {"
									+ "$jobId = $tabla.jqGrid ('getGridParam', 'selrow');\n"
									+ "if($jobId) {\n"
										+ "$type = $tabla.jqGrid('getCell', $jobId, 'j.type');\n"
										+ "	$.ajax({\n"
										+ "		url: '"+baseUrl+"?type="+COPYJOBS+"',\n"
										+ "		cache: false,\n"
										+ "		data: {'jobId' : $jobId, 'jobtype' : $type}\n"
										+ "	}).done(function( html ) {\n"
										+ "		$popupDialog.html(html);\n"
										+ "		$popupDialog.dialog('open').dialog('option', 'title', '"+getLanguageMessage("common.message.copy.jobs.info")+"');\n"
										+ "	});\n"
									+ "} \n"
								+ "} \n"
								+ "})\n"
							// EDITAR JOB
							+ ".navButtonAdd('#tablaJobs_toppager',{"
								+ "caption:'"+getLanguageMessage("common.message.edit")+"',"
								+ "buttonicon:'wbs-icon-16-cog_edit',"
								+ "position: 'last',"
								+ "onClickButton: function(e) {"
										+ "$jobId = $tabla.jqGrid ('getGridParam', 'selrow');\n"
										+ "if($jobId) {\n"
											+ "$clientId = $tabla.jqGrid('getCell', $jobId, 'clientId');\n"
											+ "$clientName = $tabla.jqGrid('getCell', $jobId, 'c.name');\n"
											+ "$jobName = $tabla.jqGrid('getCell', $jobId, 'j.name');\n"
											+ "window.location.href = '"+BackupJobs.baseUrl+"?type="+ BackupJobs.EDITJOB +"&jobName='+$jobName+'&clientId='+$clientId+'&clientName='+$clientName;\n"
										+ "}"
									+ "}"
								+ "})\n"
							+ ".navSeparatorAdd('#tablaJobs_toppager',{})\n"
							+ ".navSeparatorAdd('#tablaJobs_toppager',{})\n"
							+ ".navSeparatorAdd('#tablaJobs_toppager',{})\n"
							// CANCEL
							+ ".navButtonAdd('#tablaJobs_toppager',{"
								+ "caption:'"+getLanguageMessage("common.message.cancel")+"',"
								+ "buttonicon:'wbs-icon-16-control_stop',"
								+ "position: 'last',"
								+ "onClickButton: function(e) {"
									+ "$jobId = $tabla.jqGrid ('getGridParam', 'selrow');\n"
									+ "if($jobId) {\n"
										+ "$.ajax({\n"
											+ "url: '"+baseUrl+"',\n"
											+ "type: 'GET',\n"
											+ "timeout: 30000,\n"	// Si no recibimos respuesta en 30 seg, error.
											+ "data: {'type':"+CANCELJOB+", 'jobId':$jobId},\n"
											+ "error: function() {Apprise('"+getLanguageMessage("backup.jobsextended.error.oncancel")+"')},"
											+ "beforeSend: function() {showLoadingPage();}, complete: function() {loadImages();},"
											+ "success: function(result) {\n"
												+ "$tabla.setGridParam({datatype:'json'}).trigger('reloadGrid');\n"
												+ "Apprise(result);"
											+ "}\n"
										+ "});"
									+ "}\n"
								+ "},"
								+ "})\n"
							// STOP
							+ ".navButtonAdd('#tablaJobs_toppager',{"
								+ "caption:'"+getLanguageMessage("common.message.stop")+"',"
								+ "buttonicon:'wbs-icon-16-control_pause',"
								+ "position: 'last',"
								+ "onClickButton: function(e) {"
									+ "$jobId = $tabla.jqGrid ('getGridParam', 'selrow');\n"
									+ "if($jobId) {\n"
										+ "$.ajax({\n"
											+ "url: '"+baseUrl+"',\n"
											+ "type: 'GET',\n"
											+ "data: {'type':"+STOPJOB+", 'jobId':$jobId},\n"
											+ "error: function() {Apprise('"+getLanguageMessage("backup.jobsextended.error.onstop")+"')},"
											+ "beforeSend: function() {showLoadingPage();}, complete: function() {loadImages();},"
											+ "success: function(result) {\n"
												+ "$tabla.setGridParam({datatype:'json'}).trigger('reloadGrid');\n"
												+ "Apprise(result);"
											+ "}\n"
										+ "});"
									+ "}\n"
								+ "},"
								+ "})\n"
							// RESTART JOB
							+ ".navButtonAdd('#tablaJobs_toppager',{"
								+ "caption:'"+getLanguageMessage("common.message.restart")+"',"
								+ "buttonicon:'wbs-icon-16-control_restart',"
								+ "position: 'last',"
								+ "onClickButton: function(e) {"
									+ "$jobId = $tabla.jqGrid ('getGridParam', 'selrow');\n"
									+ "if($jobId) {\n"
										+ "$.ajax({\n"
											+ "url: '"+baseUrl+"',\n"
											+ "type: 'post',\n"
											+ "data: {'type':"+RESTARTJOB+", 'jobId':$jobId},\n"
											+ "error: function() {Apprise('"+getLanguageMessage("backup.jobsextended.error.onrestart")+"')},"
											+ "beforeSend: function() {showLoadingPage();}, complete: function() {loadImages();},"
											+ "success: function(result) {\n"
												+ "$tabla.setGridParam({datatype:'json'}).trigger('reloadGrid');\n"
												+ "Apprise(result);"
											+ "}\n"
										+ "});"
									+ "}\n"
								+ "},"
								+ "})\n"
					+ ";\n";
					pageJS += "$tabla.jqGrid('filterToolbar', {stringResult: true, searchOnEnter: false, defaultSearch: 'cn'});\n";
					pageJS += "$('#tablaJobs_toppager_center').remove();\n";
					pageJS += "$('#pg_tablaJobs_toppager').find('.ui-icon').removeClass('ui-icon').addClass('wbs-icon-16').css('margin', '-1px 5px 0 5px');\n";
					pageJSFuncs += "function sizeFormatter(cellvalue, options, rowObject) {"
							+ "result = parseFloat(cellvalue);\n"
							+ "if (result > (1024*1024*1024*1024)) { result = (result / (1024*1024*1024*1024)).toFixed(2) + ' TB' }\n"
							+ "else if (result > 1073741824) { result = (result / 1073741824).toFixed(2) + ' GB' }\n"
							+ "else if (result > 1048576) { result = (result / 1048576).toFixed(2) + ' MB' }\n"
							+ "else if (result > 1024) { result = (result / 1024).toFixed(2) + ' KB' }\n"
							+ "else { result = (result / 1024).toFixed(2) + ' B' }\n"
							+ "return result;\n"
							+ "}";
					pageJSFuncs += "function speedFormatter(cellvalue, options, rowObject) {"
							+ "result = parseFloat(cellvalue);\n"
							+ "if (result > (1024*1024*1024*1024)) { result = (result / (1024*1024*1024*1024)).toFixed(2) + ' TB' }\n"
							+ "else if (result > 1073741824) { result = (result / 1073741824).toFixed(2) + ' GB' }\n"
							+ "else if (result > 1048576) { result = (result / 1048576).toFixed(2) + ' MB' }\n"
							+ "else if (result > 1024) { result = (result / 1024).toFixed(2) + ' KB' }\n"
							+ "else if (result > 0) { result = (result / 1024).toFixed(2) + ' B' }\n"
							+ "else {return '';}\n" 
							+ "return result+'/s';\n"
							+ "}";

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
		         	
					// Pinta el pie
					writeDocumentFooter();
				}
					break;
			}
		} catch (Exception _ex) {
			writeDocumentError(_ex.getMessage());
		}
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