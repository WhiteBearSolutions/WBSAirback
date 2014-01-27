package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.util.StringFormat;

public class BackupFiles extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int RESTORE = 2;
	public final static int VIEW_JOBS = 3;
	public final static int SHOW_VOLUMES = 4;
	public final static int SEARCH_JSON_CLIENTS = 490230209;
	public final static int DEFAULT_2 = 9;
	public final static int LIST_AJAX_FILES = 10;
	public final static int LIST_AJAX_JOBS = 11;
	public final static int FILESET_AJAX_TREE_LOAD = 12;
	public final static int RESTORE_MACHINES_AJAX_LOAD = 13;
	public final static long LIMIT_RELOAD_TREE_TIME = 18000000; // 5 horas
	
	private final static Logger logger = LoggerFactory.getLogger(BackupFiles.class);
	private int type;
	public final static String baseUrl = "/admin/"+BackupFiles.class.getSimpleName();
	
	private URLCodec encoder; 
    
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter _xhtml_out = response.getWriter();
		response.setContentType("text/html");
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

			ClientManager _cm = new ClientManager(this.sessionManager.getConfiguration());
			
			if (this.type == SEARCH_JSON_CLIENTS && request.getParameter("term") != null && !request.getParameter("term").isEmpty()) {
				boolean withLocal = true;
				if (request.getParameter("withLocal") != null && !request.getParameter("withLocal").isEmpty() && request.getParameter("withLocal").equals("false"))
					withLocal = false;
					_xhtml_out.print(printJsonSearch(request.getParameter("term").toLowerCase(), withLocal, _cm));
				return;
			}
			
			if (this.type != RESTORE && this.type != LIST_AJAX_FILES && this.type != LIST_AJAX_JOBS  && type != FILESET_AJAX_TREE_LOAD && type != RESTORE_MACHINES_AJAX_LOAD)
				writeDocumentHeader();
			
			String clientName = null, clientRealName = null;
	    	int clientId = 0;
	    	int clientIdReal = 0;
			if (request.getParameter("clientId") != null && (((String)request.getParameter("clientId")).equals("--") || ((String)request.getParameter("clientId")).equals("0")) ) {
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
			
			FileManager _fm = new FileManager(this.sessionManager.getConfiguration());
			
		    JobManager _jm = new JobManager(this.sessionManager.getConfiguration());
		    
		    int TOTAL_ENTRIES = 0;
		    int MAX_ENTRIES = 10;
		    int pag = 1;
		    if(request.getParameter("MAX_ENTRIES") != null) {
		    	try {
		    		MAX_ENTRIES = Integer.parseInt(request.getParameter("MAX_ENTRIES"));
		    	} catch(NumberFormatException _ex) {}
		    }
		    if(request.getParameter("pag") != null) {
		    	try {
		    		pag = Integer.valueOf(request.getParameter("pag")).intValue();
		    	} catch(NumberFormatException _ex) {}
		    }
		    int _offset = (pag * MAX_ENTRIES) - MAX_ENTRIES;
		    
		    encoder = new URLCodec("UTF-8");
		    switch(this.type) {
	    		default: {
	    			if(!this.securityManager.checkCategory(_cm.getClient(clientName).get("category"))) {
		    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
	    			}
	    			
	    			String directory = null;
    				if (request.getParameter("directory") != null && !request.getParameter("directory").isEmpty()) {
    					if (request.getQueryString() != null && !request.getQueryString().isEmpty() && request.getQueryString().contains("directory=")) {
    						directory = encoder.decode(request.getQueryString().substring(request.getQueryString().indexOf("directory=")+"directory=".length()), "UTF-8");
    						if (directory.contains("&") && directory.contains("="))
    							directory = directory.substring(0, directory.indexOf("&"));
    					}
    					else
    						directory = encoder.decode(request.getParameter("directory"), "UTF-8");
    				}
	    			
	    			writeDocumentBack(null);
	    			
				    _xhtml_out.println("<script>");
	    			_xhtml_out.println("<!--");
	    			_xhtml_out.println("function search() {");
	    			_xhtml_out.println("  document.files.type.value = 1;");
	    			_xhtml_out.println("  submitForm(document.files.submit());");
	    			_xhtml_out.println("}");
	    			_xhtml_out.println("//-->");
	    			_xhtml_out.println("</script>");
	    			
	    			_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    			
	    			_xhtml_out.println("<form name=\"files\" id=\"files\" method=\"post\" action=\"/admin/BackupFiles\">");
	    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + RESTORE + "\"/>");
    				_xhtml_out.println("<input type=\"hidden\" name=\"clientId\" value=\"" + clientIdReal + "\"/>");
    				if(request.getParameter("directory") != null && !request.getParameter("directory").isEmpty()) {
    					_xhtml_out.println("<input type=\"hidden\" name=\"directory\" value=\"" + request.getParameter("directory") + "\"/>");
    				}

    				printSectionHeader(_xhtml_out, clientName);
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("backup.files.search"));
					_xhtml_out.print("<a href=\"javascript:search();\"><img src=\"/images/find_16.png\"  alt=\"");
					_xhtml_out.print(getLanguageMessage("common.message.search"));
					_xhtml_out.print("\" title=\"");
					_xhtml_out.print(getLanguageMessage("common.message.search"));
					_xhtml_out.println("\"/></a>");
                    _xhtml_out.println("</h2>");
                    _xhtml_out.println("<fieldset>");
                    _xhtml_out.print("<label for=\"search\">");
        	    	_xhtml_out.print(getLanguageMessage("common.message.search"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"match\"");
        	    	if(request.getParameter("match") != null && !request.getParameter("match").isEmpty()) { 
        	    		_xhtml_out.print(" value=\"");
			    		_xhtml_out.print(request.getParameter("match"));
			    		_xhtml_out.print("\"");
                    }
			    	_xhtml_out.println("/>");
                    _xhtml_out.println("</fieldset>");
        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
        	    	_xhtml_out.print("</div>");
        	    	
        	    	_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print("<img src=\"/images/folder_16.png\"/>");
                	_xhtml_out.print("&nbsp;");
                	if(request.getParameter("directory") != null && !request.getParameter("directory").isEmpty()) {
                    	_xhtml_out.print(request.getParameter("directory"));
                    } else {
                    	_xhtml_out.print(getLanguageMessage("backup.files.root"));
                    }
					_xhtml_out.print("<select class=\"form_select\" name=\"MAX_ENTRIES\" onChange=\"search();\">");
					/*_xhtml_out.print("<option value=\"2\"");
                    if(MAX_ENTRIES == 2) {
                    	_xhtml_out.print(" selected=\"selected\"");
                    }
                    _xhtml_out.print(">2</option>");*/
					_xhtml_out.print("<option value=\"5\"");
                    if(MAX_ENTRIES == 5) {
                    	_xhtml_out.print(" selected=\"selected\"");
                    }
                    _xhtml_out.print(">5</option>");
                    _xhtml_out.print("<option value=\"10\"");
                    if(MAX_ENTRIES == 10) {
                    	_xhtml_out.print(" selected=\"selected\"");
                    }
                    _xhtml_out.print(">10</option>");
                    _xhtml_out.print("<option value=\"20\"");
                    if(MAX_ENTRIES == 20) {
                    	_xhtml_out.print(" selected=\"selected\"");
                    }
                    _xhtml_out.print(">20</option>");
                    _xhtml_out.print("<option value=\"30\"");
                    if(MAX_ENTRIES == 30) {
                    	_xhtml_out.print(" selected=\"selected\"");
                    }
                    _xhtml_out.print(">30</option>");
                    _xhtml_out.print("<option value=\"40\"");
                    if(MAX_ENTRIES == 40) {
                    	_xhtml_out.print(" selected=\"selected\"");
                    }
                    _xhtml_out.print(">40</option>");
                    _xhtml_out.print("<option value=\"50\"");
                    if(MAX_ENTRIES == 50) {
                    	_xhtml_out.print(" selected=\"selected\"");
                    }
                    _xhtml_out.print(">50</option>");
                    _xhtml_out.print("</select>");
                    if((request.getParameter("directory") != null && !request.getParameter("directory").isEmpty()) ||
                    		(request.getParameter("match") != null && !request.getParameter("match").isEmpty())) {
                    	_xhtml_out.print("<a href=\"/admin/BackupFiles?clientId=" + clientId + "&clientName="+clientName+"\"");
                    	_xhtml_out.print(" title=\"");
                    	_xhtml_out.print(getLanguageMessage("backup.files.root"));
                    	_xhtml_out.print("\" alt=\"");
                    	_xhtml_out.print(getLanguageMessage("backup.files.root"));
                    	_xhtml_out.print("\"><img src=\"/images/folder_explore_16.png\"/>");
						_xhtml_out.println("</a>");
                    }
					_xhtml_out.println("</h2>");
                    _xhtml_out.print("<br/>");
                    _xhtml_out.println("<fieldset>");
                    _xhtml_out.println("<div id=\"tableFiles\" name=\"tableFiles\"></div>");
    				_xhtml_out.println("</fieldset>");
        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
        	    	_xhtml_out.print("</div>");
        	    	
        	    	_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("backup.files.recover_by_date"));
					_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
					_xhtml_out.print(getLanguageMessage("common.message.refresh"));
                	_xhtml_out.print("\" alt=\"");
                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
                	_xhtml_out.println("\"/></a>");
					_xhtml_out.print("<a href=\"javascript:submitForm(document.files.submit());\"");
                	_xhtml_out.print(" title=\"");
                	_xhtml_out.print(getLanguageMessage("backup.files.recover"));
                	_xhtml_out.print("\" alt=\"");
                	_xhtml_out.print(getLanguageMessage("backup.files.recover"));
                	_xhtml_out.print("\"><img src=\"/images/arrow_undo_16.png\"/>");
					_xhtml_out.println("</a>");
                    _xhtml_out.println("</h2>");
                    _xhtml_out.println("<fieldset>");
                    _xhtml_out.println("<div class=\"standard_form\">");
                    _xhtml_out.print("<label for=\"destinationClient\">");
        	    	_xhtml_out.print(getLanguageMessage("backup.files.restore.client"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<input id=\"destinationClient\" name=\"destinationClient\" class=\"validate[required] form_text\" type=\"text\" value=\""+clientRealName+"\"/>");
        	    	_xhtml_out.println("<input id=\"destinationClientId\" name=\"destinationClientId\" type=\"hidden\" value=\""+clientIdReal+"\"/>");
        	    	_xhtml_out.println("</div>");
                    printRestoreForm(_xhtml_out, clientRealName, clientIdReal, _cm.getClientId("airback-fd"), true);
        	    	_xhtml_out.println("</form>");
        	    	pageJS+= "$('#files').validationEngine();";
        	    	pageJS+=getJSAjaxFiles("tableFiles", directory, request.getParameter("pag"), request.getParameter("match"), String.valueOf(clientId), clientName, request.getParameter("MAX_ENTRIES"));
    			}
       			break;
	    		case LIST_AJAX_FILES: {
	    			int j = 0, top = 9;
	    			String directory = null;
    				if (request.getParameter("directory") != null && !request.getParameter("directory").isEmpty()) {
    					if (request.getQueryString() != null && !request.getQueryString().isEmpty() && request.getQueryString().contains("directory=")) {
    						directory = encoder.decode(request.getQueryString().substring(request.getQueryString().indexOf("directory=")+"directory=".length()), "UTF-8");
    						if (directory.contains("&") && directory.contains("="))
    							directory = directory.substring(0, directory.indexOf("&"));
    					}
    					else
    						directory = encoder.decode(request.getParameter("directory"), "UTF-8");
    				}
	    			
	    			List<Map<String, String>> files = new ArrayList<Map<String,String>>();
	    			if(request.getParameter("pag") != null && this.sessionManager.hasObjectSession("total_entries")) {
	    				TOTAL_ENTRIES = (Integer) this.sessionManager.getObjectSession("total_entries");
	    				if(request.getParameter("match") == null || request.getParameter("match").isEmpty()) {
		    				files = _fm.getClientFiles(clientId, clientName, directory, MAX_ENTRIES, _offset);
		    			} else {
		    				files = _fm.searchClientFiles(clientId, clientName, request.getParameter("match"), MAX_ENTRIES, _offset);
		    			}
	    			} else {
		    			if(request.getParameter("match") == null || request.getParameter("match").isEmpty()) {
		    				files = _fm.getClientFiles(clientId, clientName, directory, MAX_ENTRIES, _offset);
		    				TOTAL_ENTRIES = _fm.getClientFiles(clientId, clientName, directory, 0, 0).size();
		    			} else {
		    				files = _fm.searchClientFiles(clientId, clientName, request.getParameter("match"), MAX_ENTRIES, _offset);
		    				TOTAL_ENTRIES = _fm.searchClientFiles(clientId, clientName, request.getParameter("match"), 0, 0).size();
		    			}
		    			this.sessionManager.loadObjectSession("total_entries", TOTAL_ENTRIES);
	    			}
	    			
	    			// Caso del primer nivel de ficheros, en el que no hay paginación por BD
	    			if (files.size()>MAX_ENTRIES) {
		    			if (pag > 0 && MAX_ENTRIES > 0) {
		    				// Paginamos
		    				int first = pag*MAX_ENTRIES -MAX_ENTRIES;
		    				int last = first+MAX_ENTRIES;
		    				if (first <= files.size()) {
		    					if (last > files.size())
		    						last = files.size();
		    					
		    					files = files.subList(first, last);
		    				}
		    			}
	    			}
	    			_xhtml_out.println("<table>");
                    if(!files.isEmpty()) {
                    	_xhtml_out.println("<tr>");
                    	_xhtml_out.print("<td>&nbsp;</td>");
                    	_xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.files.name"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.files.path"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.files.size"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.files.date"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
	                    _xhtml_out.println("</tr>");
	                    int _offset2 = 1;
                    	for(Map<String, String> file : files){
	                    	StringBuilder _directory = new StringBuilder();
	                    	if(file.get("path") != null && file.get("path").length() > 40) {
								_directory.append(file.get("path").substring(0, 19));
								_directory.append("..");
								_directory.append(file.get("path").substring(file.get("path").length() - 19));
							} else if(file.get("path") != null) {
								_directory.append(file.get("path"));
							}
	                    	_xhtml_out.print("<tr");
	                    	if(_offset2 % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
							_xhtml_out.println("<td>");
							_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"files\" value=\"");
							_xhtml_out.print(URLEncoder.encode(file.get("path"), "UTF-8"));
							_xhtml_out.print("\"/>");
							_xhtml_out.println("</td>");
							_xhtml_out.print("<td>");
							if("true".equals(file.get("directory"))) {
								_xhtml_out.print("<img src=\"/images/folder_16.png\"/>");
								_xhtml_out.print("&nbsp;");
								_xhtml_out.print("<a href=\"/admin/BackupFiles?directory=" + encoder.encode(file.get("path"), "UTF-8")+"&clientId=" + clientId + "&clientName="+clientName+"\"");
								_xhtml_out.print(" alt=\"");
								_xhtml_out.print(file.get("path"));
								_xhtml_out.print("\"");
								_xhtml_out.print(" title=\"");
								_xhtml_out.print(file.get("path"));
								_xhtml_out.print("\">");
								_xhtml_out.print(file.get("name"));
								_xhtml_out.print("</a>");
							} else {
								_xhtml_out.println("<img src=\"/images/page_16.png\"/>");
								_xhtml_out.print(file.get("name"));
							}
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(_directory.toString());
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							if (!file.get("size").equals("unknown") && !"true".equals(file.get("directory"))) 
								_xhtml_out.print(file.get("size"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							if (!file.get("last-modified").equals("unknown"))
								_xhtml_out.print(file.get("last-modified"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print("<a href=\"/admin/BackupFiles?&clientId=");
							_xhtml_out.print(clientId);
							_xhtml_out.print("&clientName="+clientName+"&type=");
							_xhtml_out.print(VIEW_JOBS);
							_xhtml_out.print("&file=");
							_xhtml_out.print(encoder.encode(file.get("path"), "UTF-8"));
							if (!file.get("path").endsWith("/") && file.get("directory").equals("true"))
								_xhtml_out.print("/");
							_xhtml_out.print("\"><img src=\"/images/layers_16.png\"");
	                    	_xhtml_out.print(" title=\"");
	                    	_xhtml_out.print(getLanguageMessage("backup.files.recover_by_version"));
	                    	_xhtml_out.print("\" alt=\"");
	                    	_xhtml_out.print(getLanguageMessage("backup.files.recover_by_version"));
	                    	_xhtml_out.println("\"/></a>");
							_xhtml_out.println("</td>");
							_xhtml_out.println("</tr>");
							_offset2++;
	       		        }
                    	_xhtml_out.println("</table>");
                    	_xhtml_out.println("<table>");
	                    if(pag > 5) {
				    		j = pag - 5;
				    		top = top + j;
				    		_xhtml_out.println("<td>...</td>");
				    	}
	                    Integer max = TOTAL_ENTRIES / MAX_ENTRIES;
						if (TOTAL_ENTRIES % MAX_ENTRIES != 0)
							max++;
				    	for(; j < max && j <= top; j++) {
					    	if(j == (pag - 1)) {
					    		_xhtml_out.println("<td><a>" + (j + 1) + "</a></td>");
					    	} else {
					    		if(request.getParameter("match") != null) {
					    			_xhtml_out.print("<td class=\"page_box\"><a href=\"/admin/BackupFiles?pag=" + (j + 1));
					    			_xhtml_out.print("&clientName="+clientName+"&match=" + request.getParameter("match"));
					    			_xhtml_out.print("&clientId=" + request.getParameter("clientId") + "&TOTAL_ENTRIES=" + TOTAL_ENTRIES + "&MAX_ENTRIES=" + MAX_ENTRIES);
					    			if(request.getParameter("directory") != null &&
					    					!request.getParameter("directory").isEmpty()) {
					    				_xhtml_out.print("&directory=" + encoder.encode(directory, "UTF-8"));
					    			}
					    			_xhtml_out.print("\">" + (j + 1) + "</a></td>");
					    			
					    		} else {
					    			_xhtml_out.print("<td class=\"page_box\"><a href=\"/admin/BackupFiles?pag=" + (j + 1));
					    			_xhtml_out.print("&clientName="+clientName+"&clientId=" + request.getParameter("clientId") + "&TOTAL_ENTRIES=" + TOTAL_ENTRIES +  "&MAX_ENTRIES=" + MAX_ENTRIES);
					    			if(request.getParameter("directory") != null &&
					    					!request.getParameter("directory").isEmpty()) {
					    				_xhtml_out.print("&directory=" + encoder.encode(directory, "UTF-8"));
					    			}
					    			_xhtml_out.print("\">" + (j + 1) + "</a></td>");
					    		}
					    	}
					    }
					    if((TOTAL_ENTRIES / MAX_ENTRIES -1) > top) {
				    		_xhtml_out.println("<td>...</td>");
				    	}
                    } else {
                    	_xhtml_out.println("<tr>");
                    	_xhtml_out.println("<td>");
                    	_xhtml_out.println(getLanguageMessage("device.message.no_files"));
                    	_xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
                    }
                    _xhtml_out.println("</table>");
	    			}
	    		break;
	    		case RESTORE: {
		    			int destinationClientId, jobId = -1;
		    			
		    			if(!this.securityManager.checkCategory(_cm.getClient(clientName).get("category"))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			
		    			destinationClientId = clientId;
		    			try {
		    				destinationClientId = Integer.parseInt(request.getParameter("destinationClientId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("common.message.no_client"));
		    			}
		    			
		    			if(request.getParameter("jobid") != null && !request.getParameter("jobid").isEmpty()) {
			    			try {
			    				jobId = Integer.parseInt(request.getParameter("jobid"));
			    			} catch(NumberFormatException _ex) {
			    				throw new Exception(getLanguageMessage("common.message.no_client"));
			    			}
		    			}
		    			
		    			String lv = null;
		    			String share = null;
		    			if (request.getParameter("lv") != null && !request.getParameter("lv").isEmpty()) {
		    				lv = request.getParameter("lv");
		    			} else if (request.getParameter("share") != null && !request.getParameter("share").isEmpty()) {
		    				share = request.getParameter("share");
		    			}
		    			
		    			String destinationPath = "";
		    			if (request.getParameter("destinationPath") != null && !request.getParameter("destinationPath").isEmpty()) {
		    				destinationPath = request.getParameter("destinationPath");
		    			}
		    			
		    			if(jobId >= 0 && request.getParameter("file") != null) {
		    				BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
		    				String file = encoder.decode(request.getParameter("file"), "UTF-8");
		    				_bo.restoreFile(clientId, jobId, file, destinationClientId, lv, share, destinationPath, request.getParameter("postscript"), request.getParameter("prescript"));
		    				
		    			} else if(request.getParameter("files") != null) {
		    				Calendar _cal = Calendar.getInstance();
		    				try {
			    				_cal.set(Calendar.YEAR, Integer.parseInt(request.getParameter("year")));
			    				_cal.set(Calendar.MONTH, Integer.parseInt(request.getParameter("month")) - 1);
			    				_cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(request.getParameter("day")));
			    				_cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(request.getParameter("hour")));
			    				_cal.set(Calendar.MINUTE, Integer.parseInt(request.getParameter("minute")));
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("common.message.no_date"));
		    				}
		    				
		    				List<String> _files = new ArrayList<String>();
		    				for(String file : request.getParameterValues("files")) {
		    					_files.add(encoder.decode(file, "UTF-8"));
		    				}
		    				
		    				BackupOperator _bo = new BackupOperator(this.sessionManager.getConfiguration());
			    			_bo.restoreFiles(clientId, _files, destinationClientId, lv, share, destinationPath, _cal, request.getParameter("postscript"), request.getParameter("prescript"));
			    		}
		    			response.sendRedirect("/admin/BackupJobs?clientId=" + destinationClientId+"&clientName="+clientName+"");
		    			this.redirected=true;
	    			}
	    			break;
	    		case VIEW_JOBS: {
	    				if(!this.securityManager.checkCategory(_cm.getClient(clientName).get("category"))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
	    				String file = null;
	    				if (request.getParameter("file") != null && !request.getParameter("file").isEmpty()) {
	    					if (request.getQueryString() != null && !request.getQueryString().isEmpty() && request.getQueryString().contains("file=")) {
	    						file = encoder.decode(request.getQueryString().substring(request.getQueryString().indexOf("file=")+"file=".length()), "UTF-8");
	    						if (file.contains("&") && file.contains("="))
	    							file = file.substring(0, file.indexOf("&"));
	    					} else
	    						file = encoder.decode(request.getParameter("file"), "UTF-8");
	    				}
	    				
	    				if(file == null || file.isEmpty()) {
	    					throw new Exception(getLanguageMessage("backup.files.specify_file"));
	    				}
	    				
		    			writeDocumentBack("/admin/BackupFiles?clientId=" + clientId+"&clientName="+clientName+"&type=1&file="+encoder.encode(file, "UTF-8"));
					    _xhtml_out.println("<script>");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("function changeResults() {");
		    			_xhtml_out.println("  document.files.type.value = 1;");
		    			_xhtml_out.println("  submitForm(document.files.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("//-->");
		    			_xhtml_out.println("</script>");
		    			
		    			_xhtml_out.println("<form name=\"files\" id=\"files\" method=\"post\" action=\"/admin/BackupFiles\">");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + RESTORE + "\"/>");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"clientId\" value=\"" + clientIdReal + "\"/>");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"clientName\" value=\"" + clientRealName + "\"/>");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"file\" value=\"" + encoder.encode(file, "UTF-8") + "\"/>");
	    				printSectionHeader(_xhtml_out, clientName);
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						if(request.getParameter("file") != null && !request.getParameter("file").isEmpty()) {
	                    	_xhtml_out.print(file);
	                    } else {
	                    	_xhtml_out.print(getLanguageMessage("backup.files.recover_by_version"));
	                    }
						_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div id=\"tableJobs\" name=\"tableJobs\"></div>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.files.recover_by_version"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.files.submit());\"");
                    	_xhtml_out.print(" title=\"");
                    	_xhtml_out.print(getLanguageMessage("backup.files.recover"));
                    	_xhtml_out.print("\" alt=\"");
                    	_xhtml_out.print(getLanguageMessage("backup.files.recover"));
                    	_xhtml_out.print("\"><img src=\"/images/arrow_undo_16.png\"/>");
						_xhtml_out.println("</a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	                    _xhtml_out.print("<label for=\"destinationClient\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.files.restore.client"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input id=\"destinationClient\" name=\"destinationClient\" class=\"validate[required] form_text\" type=\"text\" value=\""+clientRealName+"\"/>");
	        	    	_xhtml_out.println("<input id=\"destinationClientId\" name=\"destinationClientId\" type=\"hidden\" value=\""+clientIdReal+"\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	printRestoreForm(_xhtml_out, clientRealName, clientIdReal, _cm.getClientId("airback-fd"), false);
        	    	
	        	    	_xhtml_out.println("</form>");
	        	    	
	        	    	_xhtml_out.println("<div id=\"viewLogDialog\" name=\"viewLogDialog\" style=\"font-size:12px;\"></div>");
	        	    	_xhtml_out.print("<input type=\"hidden\" name=\"idJobLog\" id=\"idJobLog\" value=\"\">");
	        	    	_xhtml_out.println("<div id=\"viewVolumesDialog\" name=\"viewVolumesDialog\" style=\"font-size:12px;\"></div>");
	        	    	_xhtml_out.print("<input type=\"hidden\" name=\"idJobVolumes\" id=\"idJobVolumes\" value=\"\">");
	        	    	printJSDialogs(clientIdReal);
	        	    	pageJS+=getJSAjaxJobs("tableJobs", file, request.getParameter("pag_jobs"), String.valueOf(clientId), clientName, request.getParameter("MAX_ENTRIES"));
	    			}
	       			break;
	    		case LIST_AJAX_JOBS: {
	    			int j, top;
	    			String file = null;
    				if (request.getParameter("file") != null && !request.getParameter("file").isEmpty()) {
    					if (request.getQueryString() != null && !request.getQueryString().isEmpty() && request.getQueryString().contains("file=")) {
    						file = encoder.decode(request.getQueryString().substring(request.getQueryString().indexOf("file=")+"file=".length()), "UTF-8");
    						if (file.contains("&") && file.contains("="))
    							file = file.substring(0, file.indexOf("&"));
    					} else
    						file = encoder.decode(request.getParameter("file"), "UTF-8");
    				}
    				
    				if (request.getParameter("pag_jobs") != null && !request.getParameter("pag_jobs").isEmpty()) {
	    				try { 
	    					pag = Integer.parseInt(request.getParameter("pag_jobs"));
	    				} catch (Exception ex) {}
	    			}
	    				
	    			_offset = (pag * MAX_ENTRIES) - MAX_ENTRIES;
	    			List<Map<String, String>> jobs = null;
	    			if(request.getParameter("pag_jobs") != null && this.sessionManager.hasObjectSession("total_entries_jobs")) {
	    				TOTAL_ENTRIES = (Integer) this.sessionManager.getObjectSession("total_entries_jobs");
	    				jobs = _fm.getJobsForFile(clientId, file, MAX_ENTRIES, _offset);
	    			} else {
	    				jobs = _fm.getJobsForFile(clientId, file, MAX_ENTRIES, _offset);
		    			TOTAL_ENTRIES = _fm.getJobsForFile(clientId, file, 0, 0).size();
		    			this.sessionManager.loadObjectSession("total_entries_jobs", TOTAL_ENTRIES);
	    			}
	    			
                    _xhtml_out.println("<table>");
                    if(!jobs.isEmpty()) {
                    	_xhtml_out.println("<tr>");
                    	_xhtml_out.print("<td>&nbsp;</td>");
                    	_xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.name"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.level"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.start_date"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.end_date"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.type"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.status"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.total_size"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.total_files"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
	                    _xhtml_out.println("</tr>");
	                    int _offset2 = 1;
                    	for(Map<String, String> job : jobs){
	                    	_xhtml_out.print("<tr");
	                    	if(_offset2 % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
							_xhtml_out.println("<td>");
							_xhtml_out.print("<input class=\"form_radio\" type=\"radio\" name=\"jobid\" value=\"");
							_xhtml_out.print(job.get("id"));
							_xhtml_out.print("\"/>");
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("name"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("level"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("start"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("end"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("type"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("status"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("size"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print(job.get("files"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							
							_xhtml_out.print("<a href=\"javascript:viewLog('"+job.get("id")+"','"+clientIdReal+"');\" >");
							_xhtml_out.print("<img src=\"/images/book_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.view"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.view"));
		                	_xhtml_out.println("\"/></a>");
		                	_xhtml_out.print("<a href=\"javascript:viewVolumes('"+job.get("id")+"','"+clientIdReal+"');\" >");
                            _xhtml_out.print("<img src=\"/images/database_gear_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.volumes"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.volumes"));
		                	_xhtml_out.println("\"/></a>");
							_xhtml_out.println("</td>");
							_xhtml_out.println("</tr>");
							_offset2++;
	       		        }
                    } else {
                    	_xhtml_out.println("<tr>");
                    	_xhtml_out.println("<td>");
                    	_xhtml_out.println(getLanguageMessage("device.message.no_file_versions"));
                    	_xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
                    }
                    _xhtml_out.println("</table>");
                    _xhtml_out.println("<table>");
					_xhtml_out.println("<tr>");

					j=0;
					top=8;
					if(pag > 5) {
			    		j = pag - 5;
			    		top = top + j;
			    		_xhtml_out.println("<td>...</td>");
			    	}
				    
					Integer max = TOTAL_ENTRIES / MAX_ENTRIES;
					if (TOTAL_ENTRIES % MAX_ENTRIES != 0)
						max++;
				    for(; j < max && j <= top; j++) {
					    	if(j == (pag - 1)) {
					    		_xhtml_out.println("<td><a>" + (j + 1) + "</a></td>");
					    	} else {
					    		_xhtml_out.print("<td class=\"page_box\"><a href=\"/admin/BackupFiles?pag_jobs=");
					    		_xhtml_out.print(j + 1);
					    		_xhtml_out.print("&type=");
					    		_xhtml_out.print(String.valueOf(VIEW_JOBS));
				    			_xhtml_out.println("&clientName="+clientName+"&clientId=");
				    			_xhtml_out.print(request.getParameter("clientId"));
				    			_xhtml_out.print("&TOTAL_ENTRIES_JOBS=");
				    			_xhtml_out.print(TOTAL_ENTRIES);
				    			_xhtml_out.print("&MAX_ENTRIES=");
				    			_xhtml_out.print(MAX_ENTRIES);
				    			if(request.getParameter("file") != null &&
				    					!request.getParameter("file").isEmpty()) {
				    				_xhtml_out.print("&file=" + encoder.encode(file, "UTF-8"));
				    			}
				    			_xhtml_out.print("\">");
				    			_xhtml_out.print(j + 1);
				    			_xhtml_out.println("</a></td>");
				    	}
				    }
				    if((TOTAL_ENTRIES / MAX_ENTRIES -1) > top) {
				    	_xhtml_out.println("<td>...</td>");
				    }
				    
					_xhtml_out.println("</tr>");
					_xhtml_out.println("</table>");
                    _xhtml_out.println("<br/>");
	    			}
	    			break;
			    case SHOW_VOLUMES: {
			    	int jobId, _offset2 = 0;
	    			try {
	    				jobId = Integer.parseInt(request.getParameter("jobId"));
	    			} catch(NumberFormatException _ex) {
	    				throw new Exception(getLanguageMessage("backup.jobs.exception.invalid_job"));
	    			}
	    			
	    			String file = null;
	    			if (request.getParameter("file") != null && !request.getParameter("file").isEmpty()) {
						if (request.getQueryString() != null && !request.getQueryString().isEmpty() && request.getQueryString().contains("file=")) {
							file = encoder.decode(request.getQueryString().substring(request.getQueryString().indexOf("file=")+"file=".length()), "UTF-8");
							if (file.contains("&") && file.contains("="))
								file = file.substring(0, file.indexOf("&"));
	    				} else
							file = encoder.decode(request.getParameter("file"), "UTF-8");
					}
					
	    			
	    			List<Map<String, String>> volumes = _jm.getVolumesForJob(jobId);
	    			
	    			writeDocumentBack("/admin/BackupFiles?clientId=" + clientId+"&clientName="+clientName+"&type="+VIEW_JOBS+"&file="+encoder.encode(file, "UTF-8"));
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
	                    	if(_offset2 % 2 == 0) {
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
	                    	_offset2++;
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
			if (type != SEARCH_JSON_CLIENTS  && type != LIST_AJAX_FILES && type != LIST_AJAX_JOBS && type != FILESET_AJAX_TREE_LOAD && type != RESTORE_MACHINES_AJAX_LOAD)
				writeDocumentError(_ex.getMessage());
	    } finally {
	    	if (type != SEARCH_JSON_CLIENTS && type != LIST_AJAX_FILES && type != LIST_AJAX_JOBS && type != FILESET_AJAX_TREE_LOAD && type != RESTORE_MACHINES_AJAX_LOAD)
	    		writeDocumentFooter();
	    }
	}
	
	private String getAutoCompleteJS(String divId, Integer localClientId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("$( \"#"+divId+"\" ).autocomplete({\n");
 		sb.append("		source: \"/admin/BackupFiles?type="+SEARCH_JSON_CLIENTS+"\",\n");
 		sb.append("		minLength: 1,\n");
 		sb.append("		select: function( event, ui ) {\n");
 		sb.append("			if (ui.item) {\n");
 		sb.append("					$(\"#destinationClientId\").val(ui.item.id);\n");
 		sb.append("			} else {\n");
 		sb.append("					$(\"#destinationClientId\").val($(\"#defaultClientId\").val());\n");
 		sb.append("			}\n");
	 	sb.append("			if ( $(\"#destinationClientId\").val() == "+localClientId+" ) {\n");
	 	sb.append("				$('.localclient').removeAttr('disabled');\n");
	 	sb.append("				$('.localclient').css('display','');\n");
	 	sb.append("			} else {\n");
	 	sb.append("				$('.localclient').css('display','none');\n\n");
	 	sb.append("				$('.localclient').attr('disabled', 'disabled');\n");
	 	sb.append("			}\n");
 		sb.append("		},\n");
 		sb.append("		change: function( event, ui ) {\n");
 		sb.append("			if (ui.item) {\n");
 		sb.append("					$(\"#destinationClientId\").val(ui.item.id);\n");
 		sb.append("			} else {\n");
 		sb.append("					$(\"#destinationClientId\").val($(\"#defaultClientId\").val());\n");
 		sb.append("			}\n");
	 	sb.append("			if ( $(\"#destinationClientId\").val() == "+localClientId+" ) {\n");
	 	sb.append("				$('.localclient').removeAttr('disabled');\n");
	 	sb.append("				$('.localclient').css('display','');\n");
	 	sb.append("			} else {\n");
	 	sb.append("				$('.localclient').css('display','none');\n\n");
	 	sb.append("				$('.localclient').attr('disabled', 'disabled');\n");
	 	sb.append("			}\n");
 		sb.append("		}\n");
 		sb.append("	});\n");
 		return sb.toString();
	}
	
	private void printSectionHeader(PrintWriter _xhtml_out, String clientName) throws Exception {
		_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/folder_explore_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("backup.files"));
    	_xhtml_out.print(" <i>");
    	_xhtml_out.print(clientName);
		_xhtml_out.println("</i></h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(getLanguageMessage("backup.files.info"));
		_xhtml_out.println("</div>");
	}
	
	private String printJsonSearch(String term, boolean withLocal, ClientManager cm) throws Exception {
		StringBuilder sb = new StringBuilder();
 		List<Map<String, String>> searchList = new ArrayList<Map<String, String>>();
		try {
			List<Map<String, String>> clients = cm.getAllClients(null, null, false);
			for (Map<String, String> client : clients) {
				if (client.get("name").contains(term) && (withLocal == true || (withLocal == false && !client.get("name").equals("airback-fd"))))
					searchList.add(client);
			}
			
			if (!searchList.isEmpty()) {
	 			sb.append("[");
	 			boolean first = true;
	 			for (Map<String, String> item : searchList) {
	 				if (!first)
	 					sb.append(",");
	 				else
	 					first=false;
	 				sb.append("{\"id\":\""+item.get("id")+"\",\"label\":\""+item.get("name")+"\",\"value\":\""+item.get("name")+"\"}");
	 			}
	 			sb.append("]");
	 		} 
 		
		} catch (Exception ex) {
			logger.error("Error obteniendo datos para búsqueda en backup files {}", term);
		}
		
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
	 	_sb.append("		url: '"+BackupJobs.baseUrl+"?type="+BackupJobs.JOBVOLUMES+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: {jobId : jobId, clientId: clientId}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#viewVolumesDialog').html(html);\n");
	 	_sb.append("	});\n");
	 	_sb.append("};");
	 	return _sb.toString();
	}
	
	public void printJSDialogs(int clientId) throws Exception {
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
    	pageJS+="					viewLog($('#idJobLog').val(), "+clientId+");\n";
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
    	pageJS+="					viewVolumes($('#idJobVolumes').val(), "+clientId+");\n";
    	pageJS+="				}\n";
    	pageJS+="	}\n";
     	pageJS+="});\n";
     	pageJSFuncs+=getJSViewJobLog();
     	pageJSFuncs+=getJSViewJobVolumes();
	}
	
	public String showDateJS() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function showDate() {\n");
		_sb.append("	$('#recoverDate').css('display','');\n");
		_sb.append("	$('#recoverVersion').css('display','none');\n");
		_sb.append("	$('#typeRecover').val('date');\n");
	 	_sb.append("};\n");
	 	return _sb.toString();
	}
	
	public String showVersionJS() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function showVersion() {\n");
		_sb.append("	$('#recoverDate').css('display','none');\n");
		_sb.append("	$('#recoverVersion').css('display','');\n");
		_sb.append("	$('#recoverVersion').css('visibility','visible');\n");
		_sb.append("	$('#typeRecover').val('version');\n");
	 	_sb.append("};\n");
	 	return _sb.toString();
	}
	
	public String printRestoreJs(String gridId, String treeId) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function restore() {\n");
		_sb.append("	var typeRecover = $('#typeRecover').val();\n");
		_sb.append("	var machines = $('#machinesHid').val();\n");
		_sb.append("	if (typeRecover != 'version' && typeRecover != 'date') {\n");
		_sb.append("		Apprise('"+getLanguageMessage("backup.files.recover.error.typeRestore")+"');\n");
		_sb.append("	} else if (typeof $('#"+treeId+"').jstree('get_selected') == \"undefined\") {\n");
		_sb.append("		Apprise('"+getLanguageMessage("backup.files.restore.vmware.selectdatastore")+"');\n");
		_sb.append("	} else if (typeof machines == \"undefined\" || machines == null || machines.length < 1) {\n");
		_sb.append("		Apprise('"+getLanguageMessage("backup.files.restore.vmware.select.restoremachines")+"');\n");
		_sb.append("	} else {\n");
		_sb.append("		var datastore = $('#"+treeId+"').jstree('get_selected').attr('id');\n");
		_sb.append("		if (typeof datastore == \"undefined\" || datastore == \"undefined\" || !datastore) {\n");
		_sb.append("			Apprise('"+getLanguageMessage("backup.files.restore.vmware.selectdatastore")+"');\n");
		_sb.append("		} else {\n");
		_sb.append("			var hostId =  host.attr('id');\n");
		_sb.append("			$('#datastore').val(datastore);\n");
		_sb.append("			$('#host').val(hostId);\n");
		_sb.append("			if (typeRecover == 'version') {\n");
		_sb.append("				var grid = jQuery('#"+gridId+"');\n");
		_sb.append("				var sel_id = grid.jqGrid('getGridParam', 'selrow');\n");
		_sb.append("				var jobId = grid.jqGrid('getCell', sel_id, 'jobid');\n");
		_sb.append("				if (!jobId) {\n");
		_sb.append("					Apprise('"+getLanguageMessage("backup.files.restore.vmware.selectjobversion")+"');\n");
		_sb.append("				} else {\n");
		_sb.append("					$('#jobId').val(jobId);\n");
		_sb.append("					submitForm(document.fileset.submit());\n");
		_sb.append("				}\n");
		_sb.append("			} else {\n");
		_sb.append("				var date = $('#date').val();\n");
		_sb.append("				if (typeof date == \"undefined\" || !date) {\n");
		_sb.append("					Apprise('"+getLanguageMessage("backup.files.restore.vmware.selectdate")+"');\n");
		_sb.append("				} else {\n");
		_sb.append("					submitForm(document.fileset.submit());\n");
		_sb.append("				}\n");
		_sb.append("			}\n");
		_sb.append("		}\n");
		_sb.append("	}\n");
	 	_sb.append("};\n");
	 	return _sb.toString();
	}
	
	public void printJsDatepicker() throws Exception {
		pageJS+="$( '#date' ).datetimepicker({'hour' : '23', 'minute' : '50'});";
     	pageJS+="$( '#date' ).datetimepicker('option', 'showAnim', 'slideDown');\n";
     	pageJS+="$( '#date' ).datetimepicker('option', 'dateFormat', 'dd/mm/yy');\n";
     	pageJS+="$( '#date' ).datetimepicker('option', 'timeFormat', 'HH:mm');\n";
     	if (this.messagei18N.getLocale().getLanguage().equals("es")) {
     		pageJS+="$( '#date' ).datetimepicker( 'option', $.datepicker.regional[ 'es' ] );\n";
     		pageJS+="$( '#date' ).datetimepicker( 'option', $.timepicker.regional[ 'es' ] );\n";
     	}
	}
	
	public void printRestoreForm(PrintWriter _xhtml_out, String clientName, Integer clientId, Integer localClientId, boolean byDate) throws Exception {
		int j=0;
		Calendar _cal = Calendar.getInstance();
		String[] _months = new String[] { "common.message.january", "common.message.february", "common.message.march", "common.message.april", "common.message.may", "common.message.june", "common.message.july", "common.message.august", "common.message.september", "common.message.october", "common.message.november", "common.message.december" };
		
		String disabledLocal = " style=\"display:none;\"";
		if (clientName.equals("airback-fd"))
			disabledLocal = "";
		
	 	pageJS += getAutoCompleteJS("destinationClient", localClientId);
		_xhtml_out.println("<input type=\"hidden\" name=\"defaultClientId\" id=\"defaultClientId\" value=\""+clientId+"\" />");
		_xhtml_out.println("<div class=\"standard_form localclient\" "+disabledLocal+">");
		_xhtml_out.print("<label for=\"lv\">");
		_xhtml_out.print(getLanguageMessage("backup.storage.lv"));
		_xhtml_out.println(": </label>");
		_xhtml_out.println("<select class=\"form_select localclient\" "+disabledLocal+" name=\"lv\" id=\"lv\">");
		_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
		for(Map<String, String> _lv : _lvs) {
			_xhtml_out.print("<option value=\"");
			_xhtml_out.print(_lv.get("vg"));
			_xhtml_out.print("/");
			_xhtml_out.print(_lv.get("name"));
			_xhtml_out.print("\">");
			_xhtml_out.print(_lv.get("vg"));
			_xhtml_out.print("/");
			_xhtml_out.print(_lv.get("name"));
			_xhtml_out.println("</option>");
		}
		_xhtml_out.println("</select>");
		_xhtml_out.println("</div>");
		_xhtml_out.println("<div class=\"standard_form localclient\" "+disabledLocal+">");
		_xhtml_out.print("<label for=\"share\">");
		_xhtml_out.print(getLanguageMessage("backup.storage.external_share"));
		_xhtml_out.println(": </label>");
		_xhtml_out.println("<select class=\"form_select localclient\" "+disabledLocal+" name=\"share\" id=\"share\">");
		_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		List<String> _shares = ShareManager.getExternalShareNames();
		for(String _share : _shares) {
			_xhtml_out.print("<option value=\"");
			_xhtml_out.print(_share);
			_xhtml_out.print("\">");
			_xhtml_out.print(_share);
			_xhtml_out.println("</option>");
		}
		_xhtml_out.println("</select>");
		_xhtml_out.println("</div>");
		
		if (byDate) {
			_xhtml_out.println("<div class=\"standard_form\">");
			_xhtml_out.print("<label for=\"day\">");
			_xhtml_out.print(getLanguageMessage("backup.files.restore.date"));
			_xhtml_out.println(": </label>");
			_xhtml_out.println("<select class=\"form_select\" name=\"day\">");
			for(j = 1; j < 32; j++) {
			 	_xhtml_out.print("<option value=\"" + j + "\"");
			 	if(_cal.get(Calendar.DAY_OF_MONTH) == j) {
			 		_xhtml_out.print(" selected=\"selected\"");
			 	}
			 	_xhtml_out.print(">");
			 	_xhtml_out.print(j);
				_xhtml_out.print("</option>");
			}
			_xhtml_out.println("</select> / ");
			_xhtml_out.println("<select class=\"form_select\" name=\"month\">");
			for(j = 0; j < 12; j++) {
				_xhtml_out.print("<option value=\"");
				_xhtml_out.print(j + 1);
				_xhtml_out.print("\"");
				if(_cal.get(Calendar.MONTH) == j) {
					_xhtml_out.print(" selected=\"selected\"");
				}
				_xhtml_out.print(">");
				_xhtml_out.print(getLanguageMessage(_months[j]));
				_xhtml_out.println("</option>");
			}
			_xhtml_out.print("</select> / ");
			_xhtml_out.print("<select class=\"form_select\" name=\"year\">");
			for(j = _cal.get(Calendar.YEAR); j > _cal.get(Calendar.YEAR) - 10; j--) {
				_xhtml_out.print("<option value=\"");
				_xhtml_out.print(j);
				_xhtml_out.print("\"");
				if(_cal.get(Calendar.YEAR) == j) {
					_xhtml_out.print(" selected=\"selected\"");
				}
				_xhtml_out.print(">");
				_xhtml_out.print(j);
				_xhtml_out.println("</option>");
			}
			_xhtml_out.println("</select>");
			_xhtml_out.println("<select class=\"form_select\" name=\"hour\">");
			for(j = 0; j < 24; j++) {
				_xhtml_out.print("<option value=\"" + j + "\"");
				if (j == 23)
					_xhtml_out.print(" selected=\"selected\" ");
				_xhtml_out.print(">");
				_xhtml_out.print(StringFormat.getTwoCharTimeComponent(j));
				_xhtml_out.print("</option>");
			}
			_xhtml_out.println("</select>");
			_xhtml_out.println(":");
			_xhtml_out.println("<select class=\"form_select\" name=\"minute\">");
			for(j = 0; j < 60; j++) {
			 	_xhtml_out.print("<option value=\"");
				_xhtml_out.print(j);
				_xhtml_out.print("\">");
				_xhtml_out.print(BackupOperator.twoCharFormat(j));
				_xhtml_out.println("</option>");
			}
			_xhtml_out.println("</select>");
			_xhtml_out.println("</div>");
		}
		_xhtml_out.println("<div class=\"standard_form\">");
		_xhtml_out.print("<label for=\"destinationPath\">");
		_xhtml_out.print(getLanguageMessage("backup.files.restore.destination_path"));
		_xhtml_out.println(": </label>");
		_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"destinationPath\" value=\"/tmp/\"/>");
		_xhtml_out.println("</div>");
		_xhtml_out.println("<div class=\"standard_form\">");
		_xhtml_out.print("<label for=\"prescript\">");
		_xhtml_out.print(getLanguageMessage("backup.files.restore.prescript"));
		_xhtml_out.println(": </label>");
		_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"prescript\" value=\"\"/>");
		_xhtml_out.println("</div>");
		_xhtml_out.println("<div class=\"standard_form\">");
		_xhtml_out.print("<label for=\"search\">");
		_xhtml_out.print(getLanguageMessage("backup.files.restore.postscript"));
		_xhtml_out.println(": </label>");
		_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"postscript\"  value=\"\"/>");
		_xhtml_out.println("</div>");
		_xhtml_out.println("</fieldset>");
		_xhtml_out.println("<div class=\"clear\"/></div>");
		if (clientName != null && clientName.equals("airback-fd")) {
			_xhtml_out.println("<div class=\"subinfo localclient\">");
			_xhtml_out.println(getLanguageMessage("backup.files.recover.warning.volumes"));
			_xhtml_out.println("</div>");
		}
		_xhtml_out.print("</div>");
		_xhtml_out.println("</fieldset>");
		_xhtml_out.println("<div class=\"clear\"/></div>");
		
		_xhtml_out.print("</div>");
	}
	
	public String getJSAjaxFiles(String div, String directory, String pag, String match, String clientId, String clientName, String maxEntries) throws Exception {
		String data = "";
		if (directory != null && !directory.isEmpty())
			data+="directory : '"+directory+"'";
		if (pag != null && !pag.isEmpty()) {
			if (!data.isEmpty()) {
				data+=", ";
			}
			data+="pag : '"+pag+"'";
		}
		if (match != null && !match.isEmpty()) {
			if (!match.isEmpty()) {
				match+=", ";
			}
			match+="match : '"+match+"'";
		}
		if (maxEntries != null && !maxEntries.isEmpty()) {
			if (!maxEntries.isEmpty()) {
				maxEntries+=", ";
			}
			maxEntries+="MAX_ENTRIES : '"+maxEntries+"'";
		}
	    StringBuilder _sb = new StringBuilder();
	    _sb.append("$('#"+div+"').html('<img src=\"/images/loadingGrey.gif\"> "+getLanguageMessage("common.loading.waiting")+"');\n");
	    _sb.append("	$.ajax({\n");
	    _sb.append("		url: '"+baseUrl+"?type="+LIST_AJAX_FILES+"&clientId="+clientId+"&clientName="+clientName+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: {"+data+"}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#"+div+"').html(html);\n");
	 	_sb.append("	});\n");
	    return _sb.toString();
	}
	
	public String getJSAjaxJobs(String div, String file, String pag_jobs, String clientId, String clientName, String maxEntries) throws Exception {
		String data = "";
		if (file != null && !file.isEmpty())
			data+="file : '"+file+"'";
		if (pag_jobs != null && !pag_jobs.isEmpty()) {
			if (!data.isEmpty()) {
				data+=", ";
			}
			data+="pag_jobs : '"+pag_jobs+"'";
		}
		if (maxEntries != null && !maxEntries.isEmpty()) {
			if (!maxEntries.isEmpty()) {
				maxEntries+=", ";
			}
			maxEntries+="MAX_ENTRIES : '"+maxEntries+"'";
		}
	    StringBuilder _sb = new StringBuilder();
	    _sb.append("$('#"+div+"').html('<img src=\"/images/loadingGrey.gif\"> "+getLanguageMessage("common.loading.waiting")+"');\n");
	    _sb.append("	$.ajax({\n");
	    _sb.append("		url: '"+baseUrl+"?type="+LIST_AJAX_JOBS+"&clientId="+clientId+"&clientName="+clientName+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: {"+data+"}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#"+div+"').html(html);\n");
	 	_sb.append("	});\n");
	    return _sb.toString();
	}
}
