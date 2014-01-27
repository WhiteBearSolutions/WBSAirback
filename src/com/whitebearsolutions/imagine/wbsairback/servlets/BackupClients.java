package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.RoleManager;
import com.whitebearsolutions.imagine.wbsairback.backup.CategoryManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.util.Configuration;

public class BackupClients extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int NEW_CLIENT = 2;
	public final static int EDIT_CLIENT = 3;
	public final static int STORE_CLIENT = 4;
	public final static int REMOVE_CLIENT = 5;
	public final static int DOWNLOAD_AGENT = 6;
	public final static int DOWNLOAD_AGENT_2 = 8;
	public final static int CHECK_ONLINE_CLIENT = 7;
	public final static int JSON_CLIENTS = 23658475;
	private int type;
	public final static String baseUrl = "/admin/"+BackupClients.class.getSimpleName();
	
	private LicenseManager _lm = null;
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		this.type = 1;
		if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
			try {
				this.type = Integer.parseInt(request.getParameter("type"));
			} catch(NumberFormatException _ex) {}
		}
		
		if (this.type == DOWNLOAD_AGENT || this.type == DOWNLOAD_AGENT_2) {
			String os = request.getParameter("os").toString();
			String _pathAgent = ClientManager.clientInstaller1SOs.get(os);
			if (this.type == DOWNLOAD_AGENT_2)
				_pathAgent = ClientManager.clientInstaller2SOs.get(os);
			this.downloadFile(response, "/etc/wbsairback-admin/agents/"+_pathAgent);
		}
	    
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	Configuration _c = this.sessionManager.getConfiguration();
	    	CategoryManager _cm = new CategoryManager();
	    	ClientManager _clm = new ClientManager(_c);
	    	_lm = new LicenseManager();
	    	
			PrintWriter _xhtml_out = response.getWriter();
			
			List<String> categories = null;
			if(!this.securityManager.isAdministrator() && !this.securityManager.isRole(RoleManager.roleGlobalOperator)) {
				if(this.securityManager.hasUserCategory()) {
					categories = this.securityManager.getUserCategories();
				} else {
					throw new Exception(getLanguageMessage("common.message.no_privilegios"));
				}
			} else if(request.getParameter("categories") != null && !request.getParameter("categories").isEmpty()) {
				categories = new ArrayList<String>();
				categories.add(request.getParameter("categories"));
			}
			
			if (this.type == JSON_CLIENTS)
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
					if (! ((this.securityManager.isRole(RoleManager.roleUser) || this.securityManager.isRole(RoleManager.roleOperator) ) && (categories == null || categories.isEmpty()))) {
						List<Map<String, String>> clients = _clm.searchClients(request.getParameter("search"), categories, page, rp, sortname, sortorder, query, qtype );
						List<Map<String, String>> all = _clm.getAllClients(request.getParameter("search"), categories, false);
						String jsonClients = this.clientsToJSON(clients, page, all.size(), _lm);
						PrintWriter out = response.getWriter();
						out.flush();
						out.print(jsonClients);
					} else {
						PrintWriter out = response.getWriter();
						out.flush();
						out.print("{\"page\":1,\"total\":1,\"rows\":[]}");
					}
					
					return;
			    } catch (Exception ex) {
			    	return;
			    }
		    }
			response.setContentType("text/html");
			
			if (type != STORE_CLIENT)
				writeDocumentHeader();	
			
		    switch(this.type) {
	    		default: {
	    				printHeader(_xhtml_out);
	    				_xhtml_out.println("<form action=\"/admin/BackupClients\" name=\"search\" id=\"search\" method=\"post\">");
	                	_xhtml_out.println("<div style=\"margin:20px auto;width:94%;clear:both;\">");
	                    _xhtml_out.println("<table id=\"flexigridClients\" style=\"margin-left:0px;margin-right:0px;\"></table>");
	                    _xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.clients.search"));
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"search\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.search"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"search\"");
	        	    	if(request.getParameter("search") != null && !request.getParameter("search").isEmpty()) {
				    		_xhtml_out.print(" value=\"");
				    		_xhtml_out.print(request.getParameter("search"));
				    		_xhtml_out.print("\"");
			    		}
				    	_xhtml_out.println("/>");
				    	_xhtml_out.println("<select class=\"form_select\" name=\"categories\">");
				    	if(((categories == null || categories.isEmpty()) && (this.securityManager.isAdministrator() || this.securityManager.isRole(RoleManager.roleGlobalOperator)))) {
				    		_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
					    	for(String cat : _cm.getCategoryNames()) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(cat);
								_xhtml_out.print("\"");
								if(categories != null && categories.contains(cat)) {
									_xhtml_out.print(" selected=\"selected\"");
								}
								_xhtml_out.println(">");
								_xhtml_out.print(cat);
								_xhtml_out.println("</option>");
							}
				    	} else {
				    		for(String cat : categories) {
					    		_xhtml_out.print("<option value=\"");
								_xhtml_out.print(cat);
								_xhtml_out.print("\"");
					    		_xhtml_out.print(">");
								_xhtml_out.print(cat);
								_xhtml_out.println("</option>");
				    		}
				    	}
				    	_xhtml_out.println("</select>");
				    	_xhtml_out.print("<a href=\"javascript:submitForm(document.search.submit());\"><img src=\"/images/find_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.search"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.search"));
				        _xhtml_out.println("\"/>");
				        _xhtml_out.print("</a>");
			    		_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	_xhtml_out.println("</form>");
	        	    	
	        	    	String qSearch = "";
	        	    	if (request.getParameter("search") != null)
	        	    		qSearch = request.getParameter("search");
	        	    	String qCategories = "";
	        	    	if (request.getParameter("categories") != null)
	        	    		qCategories = request.getParameter("categories");
	        	    	
	        	    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/flexigrid.js\"></script>");
	        	    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.cookie.js\"></script>");
	        	    	pageJS+="var myFlex = $(\"#flexigridClients\").flexigrid({\n";
	        	    	pageJS+="	url: '/admin/BackupClients?type="+JSON_CLIENTS+"&search="+qSearch+"&categories="+qCategories+"',\n";
	        	    	pageJS+="dataType: 'json',\n";
	        	    	pageJS+="colModel : [\n";
	        	    	pageJS+="{display: ' ', name : 'clientid', align: 'left', width : 15, sortable : true},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.clients.name")+"', name : 'name', width : 110, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.clients.os")+"', name : 'os', width :95, sortable : false, align: 'center'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.clients.version")+"', name : 'version', width : 210, sortable : false, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.clients.start_date")+"', name : 'starttime', width : 110, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: '"+getLanguageMessage("backup.clients.status")+"', name : 'status', width : 135, sortable : true, align: 'left'},\n";
	        	    	pageJS+="{display: ' ', name : 'actions', width : 135, sortable : false, align: 'left'}\n";
	        	    	pageJS+="],\n";
	        	    	pageJS+="buttons : [\n";
	        	    	pageJS+="{name: '   "+getLanguageMessage("common.message.add")+"', onpress : goNewClient, bclass : 'btnAdd'},\n";	
	        	    	pageJS+="{name: '   "+getLanguageMessage("backup.jobs.select_all")+"', onpress : invertSelection, bclass : 'btnSelect'}\n";
	        	    	pageJS+="],\n";
	        	    	pageJS+="procmsg : '"+getLanguageMessage("backup.jobs.grid.loading")+"',\n";
	        	    	pageJS+="errormsg : '"+getLanguageMessage("backup.jobs.grid.error")+"',\n";
	        	    	pageJS+="tableId: 'flexigridClients',\n";
	        	    	pageJS+="sortname: 'name',\n";
	        	    	pageJS+="sortorder: 'asc',\n";
	        	    	pageJS+="usepager: true,\n";
	        	    	//pageJS+="colResize: false,\n";
	        	    	pageJS+="title: '"+getLanguageMessage("backup.clients.backup_clients")+"',\n";
	        	    	pageJS+="useRp: true,\n";
	        	    	pageJS+="rp: 10,\n";
	        	    	pageJS+="showTableToggleBtn: false,\n";
	        	    	pageJS+="width: 'auto',\n";
	        	    	pageJS+="height: 430\n";
	        	    	pageJS+="});\n";
	        	    	pageJSFuncs+="function deleteJobs() {\n";
	        	    	pageJSFuncs+="		var grid = $('#flexigridClients');\n";
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
	        	    	pageJSFuncs+="	var rows = $('table#flexigridClients').find('tr').get();\n";
	        	    	pageJSFuncs+="	$.each(rows,function(i,n) {\n";
	        	    	pageJSFuncs+="		$(n).toggleClass('trSelected');\n";
	        	    	pageJSFuncs+="	});\n";
	        	    	pageJSFuncs+="}\n";
	        	    	pageJSFuncs+="function goNewClient() {\n";
	        	    	pageJSFuncs+="	document.location.href='/admin/BackupClients?type="+NEW_CLIENT+"'\n";
	        	    	pageJSFuncs+="}\n";
		    		}
	    			break;
	    		case NEW_CLIENT: {
		    			
	        		    writeDocumentBack("/admin/BackupClients");
	        		    printClientForm(_xhtml_out, _cm, null);
		    		}
		    		break;
	    		case EDIT_CLIENT: {
		    			if(request.getParameter("clientName") == null || request.getParameter("clientName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.clients.client"));
		    			}
		    			
		    			if (request.getParameter("clientName").equals("airback-fd") && !this.securityManager.isAdministrator())
		    				throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			
		    			Map<String, String> client = _clm.getClient(request.getParameter("clientName"));
		    			if(!this.securityManager.checkCategory(client.get("category"))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    					    			
		    			writeDocumentBack("/admin/BackupClients");
		    			printClientForm(_xhtml_out, _cm, client);
			    	}
		    		break;
	    		case STORE_CLIENT: {
		    			
		    			if(!this.securityManager.checkCategory(request.getParameter("categories"))) {
			    			throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			}
		    			
		    			String clientName = null;
		    			if (request.getParameter("name") == null || request.getParameter("name").trim().isEmpty()) {
		    				throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.name"));
		    			}
		    			clientName = request.getParameter("name");
		    			
		    			if (clientName.equals("airback-fd") && !this.securityManager.isAdministrator())
		    				throw new Exception(getLanguageMessage("common.message.no_privilegios"));
		    			
		    			String os = null;
		    			if (request.getParameter("os") != null && !request.getParameter("os").isEmpty()) {
		    				os = request.getParameter("os");
		    			}
		    			
		    			if (os == null && !clientName.equals("airback-fd")) {
		    				throw new Exception(getLanguageMessage("backup.clients.os"));
		    			}
		    			
		    			String password = null;
		    			if (request.getParameter("password") != null && !request.getParameter("password").trim().isEmpty()) {
		    				password = request.getParameter("password").trim();
		    			}
		    			
		    			Integer fileRetention = null;
		    			if (request.getParameter("fileRetention") != null && !request.getParameter("fileRetention").trim().isEmpty()) {
		    				try {
		    					fileRetention = Integer.parseInt(request.getParameter("fileRetention").trim());
		    				} catch (Exception ex) {
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.fileRetention"));
		    				}
		    			}
		    			
		    			Integer jobRetention = null;
		    			if (request.getParameter("jobRetention") != null && !request.getParameter("jobRetention").trim().isEmpty()) {
		    				try {
		    					jobRetention = Integer.parseInt(request.getParameter("jobRetention").trim());
		    				} catch (Exception ex) {
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.jobRetention"));
		    				}
		    			}
		    			
		    			Integer port = null;
		    			if (request.getParameter("port") != null && !request.getParameter("port").trim().isEmpty()) {
		    				try {
								port = Integer.parseInt(request.getParameter("port").trim());
							} catch(NumberFormatException _ex) {
								throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.port"));
							}
		    			}
		    			
		    			StringBuilder _address = new StringBuilder();
	                    if(request.getParameter("ip1") != null &&
	                    		request.getParameter("ip2") != null && 
	                    		request.getParameter("ip3") != null &&
	                    		request.getParameter("ip4") != null &&
	                    		!request.getParameter("ip1").isEmpty() &&
	                    		!request.getParameter("ip2").isEmpty() &&
	                    		!request.getParameter("ip3").isEmpty() &&
	                    		!request.getParameter("ip4").isEmpty()) {
	                    	_address.append(request.getParameter("ip1"));
		                    _address.append(".");
		                    _address.append(request.getParameter("ip2"));
		                    _address.append(".");
		                    _address.append(request.getParameter("ip3"));
		                    _address.append(".");
		                    _address.append(request.getParameter("ip4"));
		                    if (!NetworkManager.isValidAddress(_address.toString())) {
		                    	throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.address"));
		                    }
	                    } else if(!clientName.equals("airback-fd") && request.getParameter("dns_name") != null && !request.getParameter("dns_name").isEmpty()) {
	                    	_address.append(request.getParameter("dns_name"));
	                    }
						
		    			if (clientName.equals("airback-fd")) {
		    				if (fileRetention == null)
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.fileRetention"));
		    				if (jobRetention == null)
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.jobRetention"));
		    				_clm.setLocalClient(fileRetention, request.getParameter("fileRetentionUnits"), jobRetention, request.getParameter("jobRetentionUnits"));
		    			} else {
		    				if (password == null || password.isEmpty())
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.password"));
		    				if (fileRetention == null)
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.fileRetention"));
		    				if (jobRetention == null)
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.jobRetention"));
		    				if (port == null)
		    					throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.port"));
		    				if (_address.toString().trim().isEmpty())
		                    	throw new Exception(getLanguageMessage("advanced.groupjob.client.exception.address"));
		    				_clm.setClient(clientName, _address.toString(), port, os, password, request.getParameter("charset"), fileRetention, request.getParameter("fileRetentionUnits"), jobRetention, request.getParameter("jobRetentionUnits"), request.getParameterValues("categories"));
		    			}
		    			response.sendRedirect("/admin/BackupClients");
		    			this.redirected=true;
		    		}
	    			break;
	    		case REMOVE_CLIENT: {
		    			if(request.getParameter("confirm") != null) {
		    				_clm.deleteClient(request.getParameter("clientName"));
				    		writeDocumentResponse(getLanguageMessage("backup.message.client.removed"), "/admin/BackupClients");
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("backup.message.client.question"), "/admin/BackupClients?type=" + REMOVE_CLIENT + "&clientName=" + request.getParameter("clientName") + "&confirm=true", null);
		    			}
		    		}
	    			break;
	    		case CHECK_ONLINE_CLIENT: {
	    			if(request.getParameter("clientId") != null && request.getParameter("clientName") != null) {
	    				if (_clm.isOnlineClient(request.getParameter("clientName")).equals(Boolean.valueOf(true)))
	    					writeDocumentResponse(getLanguageMessage("backup.message.client.isonline"), "/admin/BackupClients");
	    				else
	    					writeDocumentError(getLanguageMessage("backup.message.client.isoffline"));
	    			} else {
		    				throw new Exception(getLanguageMessage("backup.clients.client"));
	    			}
	    		}
	    			break;
	    	}
		} catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	if (type != JSON_CLIENTS)
	    		writeDocumentFooter();
	    }
	}
	
	public String clientsToJSON(List<Map<String, String>> clients, Integer pag, Integer total, LicenseManager lm) throws Exception
	{
		String json = "{\"page\":"+pag+",\"total\":"+total;
		
		
		if (clients != null && clients.size() > 0) {
			json += ",\"rows\":[";
			int a = 0;
			for (Map<String, String> client : clients) {
				if (a == 0) {
					json += "{\"id\":\""+client.get("id")+"\",\"cell\":[";
					a++;
				} else
					json += ",{\"id\":\""+client.get("id")+"\",\"cell\":[";
				
				json += "\""+client.get("id")+"\"";
				String name = client.get("name");
				int limitCutn = name.length();
				if (name != null && name.length() > 18) {
					while (limitCutn > 18) {					
						name = name.substring(0, limitCutn)+"-<br />"+name.substring(limitCutn);
						limitCutn-=18;
					}
				}
				json += ",\""+name+"\"";
				String os = "unix";
				if (client.get("os").contains("win"))
					os = "win32";
				else if (client.get("os").contains("vmware"))
					os = "vmware";
				else if (client.get("os").contains("linux"))
					os = "linux";
				else if (client.get("os").contains("wbsairback"))
					os = "wbsairback";
				json += ",\"<img src='/images/os_"+os+".png' />\"";
				String version = client.get("uname");
				int limitCut = version.length();
				if (version != null && version.length() > 30) {
					while (limitCut > 30) {					
						version = version.substring(0, limitCut)+"-<br />"+version.substring(limitCut);
						limitCut-=30;
					}
				}
				json += ",\""+version+"\"";
				json += ",\""+client.get("starttime")+"\"";
				json += ",\"<div class='grid_"+client.get("alert")+"'>"+client.get("status")+"</div>\"";
				json += ",\""+this.getToolsClient(client, lm)+"\"";
				json += "]}";
			}
			json += "]";
		}
		json += "}";
		return json;
	}
	
	public String getToolsClient(Map<String, String> client, LicenseManager lm) throws Exception
	{
		String tools = "";
		tools+="<a href='/admin/BackupClients?clientId=";
		tools+=client.get("id");
		tools+="&clientName=";
		tools+=client.get("name");
		tools+="&type=";
		tools+=EDIT_CLIENT;
		tools+="'><img src='/images/computer_edit_16.png' title='";
       	tools+=getLanguageMessage("common.message.edit");
       	tools+="' alt='";
       	tools+=getLanguageMessage("common.message.edit");
       	tools+="'/></a>&nbsp;";
		if(!"airback-fd".equals(client.get("name"))) {
				tools+="<a href='/admin/BackupClients?clientId=";
				tools+=client.get("id");
				tools+="&clientName=";
				tools+=client.get("name");
				tools+="&type=";
				tools+=REMOVE_CLIENT;
				tools+="'><img src='/images/cross_16.png' title='";
            	tools+=getLanguageMessage("common.message.remove");
            	tools+="' alt='";
            	tools+=getLanguageMessage("common.message.remove");
            	tools+="'/></a>&nbsp;";
		}
		tools+="<a href='/admin/BackupJobs?clientId=";
		tools+=client.get("id");
		tools+="&clientName=";
		tools+=client.get("name");
		tools+="'><img src='/images/cog_16.png' title='";
    	tools+=getLanguageMessage("backup.jobs");
    	tools+="' alt='";
    	tools+=getLanguageMessage("backup.jobs");
    	tools+="'/></a>&nbsp;";
    	tools+="<a href='/admin/BackupFiles?clientId=";
		tools+=client.get("id");
		tools+="&clientName=";
		tools+=client.get("name");
		tools+="'><img src='/images/folder_explore_16.png' title='";
    	tools+=getLanguageMessage("backup.files");
    	tools+="' alt='";
    	tools+=getLanguageMessage("backup.files");
    	tools+="'/></a>&nbsp;";
    	tools+="<a href='/admin/BackupClients?clientId=";
		tools+=client.get("id");
		tools+="&clientName=";
		tools+=client.get("name");
		tools+="&type=";
		tools+=CHECK_ONLINE_CLIENT;
		tools+="'><img src='/images/connect_16.png' title='";
    	tools+=getLanguageMessage("backup.check_online_client");
    	tools+="' alt='";
    	tools+=getLanguageMessage("backup.check_online_client");
    	tools+="'/></a>";
    	
    	return tools;
	}
	
	public void printHeader(PrintWriter _xhtml_out) throws Exception {
    	_xhtml_out.println("<h1>");
		_xhtml_out.print("<img src=\"/images/computer_32.png\"/>");
    	_xhtml_out.print(getLanguageMessage("common.menu.backup.clients"));
		_xhtml_out.println("</h1>");
		_xhtml_out.print("<div class=\"info\">");
		_xhtml_out.print(getLanguageMessage("backup.clients.info"));
		_xhtml_out.println("</div>");
	}
	
	public void printJSAutoDeploy() throws Exception {
    	pageJSFuncs+="	function checkAutoDeploy()\n";
    	pageJSFuncs+="	{\n";
    	pageJSFuncs+="		var autoDeploy = document.getElementById(\"autoDeploy\");\n";
    	pageJSFuncs+="		var username = document.getElementById(\"autoDeployUsername\");\n";
    	pageJSFuncs+="		var password = document.getElementById(\"autoDeployPassword\");\n";
    	pageJSFuncs+="		if (autoDeploy.checked)\n";
    	pageJSFuncs+="		{\n";
    	pageJSFuncs+="			username.disabled=false;\n";
    	pageJSFuncs+="			password.disabled=false;\n";
    	pageJSFuncs+="		}\n";
    	pageJSFuncs+="		else\n";
    	pageJSFuncs+="		{\n";
    	pageJSFuncs+="			username.disabled=true;\n";
    	pageJSFuncs+="			password.disabled=true;\n";
    	pageJSFuncs+="		}\n";
    	pageJSFuncs+="	}\n";
    	pageJSFuncs+="	function checkOS()\n";
    	pageJSFuncs+="	{\n";
    	pageJSFuncs+="		var so = document.getElementById(\"os\");\n";
	    pageJSFuncs+="		var downloadAgent = document.getElementById(\"downloadAgent\");\n";
	    pageJSFuncs+="		var autoDeploy = document.getElementById(\"autoDeploy\");\n";
	    pageJSFuncs+="		var username = document.getElementById(\"autoDeployUsername\");\n";
    	pageJSFuncs+="		var password = document.getElementById(\"autoDeployPassword\");\n";
		pageJSFuncs+="		if (so.value == 'mac' || so.value.indexOf('redhat') > -1 || so.value.indexOf('suse') > -1 || so.value.indexOf('solaris') > -1) \n";
		pageJSFuncs+="		{\n";
		pageJSFuncs+="			autoDeploy.disabled=true;\n";
		pageJSFuncs+="			username.disabled=true;\n";
    	pageJSFuncs+="			password.disabled=true;\n";
		pageJSFuncs+="			document.getElementById('downloadAgent').removeAttribute('style');\n";
		pageJSFuncs+="			document.getElementById('downloadAgent').setAttribute('style','margin-left:-250px;');\n";
		pageJSFuncs+="			document.getElementById('autoDeployDiv').setAttribute('style','display:none;');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="		else if (so.value.indexOf('win') > -1 || so.value.indexOf('debian') > -1) \n";
		pageJSFuncs+="		{\n";
		pageJSFuncs+="			autoDeploy.disabled=false;\n";
		//pageJSFuncs+="			username.disabled=false;\n";
    	//pageJSFuncs+="			password.disabled=false;\n";
		pageJSFuncs+="			downloadAgent.disabled=true;\n";
		pageJSFuncs+="			document.getElementById('downloadAgent').setAttribute('style','display:none;');\n";
		pageJSFuncs+="			document.getElementById('autoDeployDiv').removeAttribute('style');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="		else\n";
		pageJSFuncs+="		{\n";
		pageJSFuncs+="			autoDeploy.disabled=true;\n";
		pageJSFuncs+="			username.disabled=true;\n";
		pageJSFuncs+="			password.disabled=true;\n";
		pageJSFuncs+="			document.getElementById('downloadAgent').setAttribute('style','display:none;');\n";
		pageJSFuncs+="			document.getElementById('autoDeployDiv').setAttribute('style','display:none;');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="		if (so.value == 'vmware') {\n";
		pageJSFuncs+="				$('.notvmware').css('display','none');\n";
		pageJSFuncs+="				$('.notvmware').attr('disabled', 'disabled');\n";
		pageJSFuncs+="				$('.vmware').css('display','');\n";
		pageJSFuncs+="				$('.vmware').removeAttr('disabled');\n";
		pageJSFuncs+="		} else {\n";
		pageJSFuncs+="				$('.notvmware').css('display','');\n";
		pageJSFuncs+="				$('.notvmware').removeAttr('disabled');\n";
		pageJSFuncs+="				$('.vmware').css('display','none');\n";
		pageJSFuncs+="				$('.vmware').attr('disabled', 'disabled');\n";
		pageJSFuncs+="		}\n";
		pageJSFuncs+="	}\n";
		pageJSFuncs+="	function urlDownload() {\n";
		pageJSFuncs+="		os = document.getElementById('os').value;\n";
		pageJSFuncs+="		document.location.href  = '/admin/BackupClients?type="+DOWNLOAD_AGENT+"&&os='+os;\n";
		pageJSFuncs+="	}\n";
		pageJSFuncs+="	function urlDownload2() {\n";
		pageJSFuncs+="		os = document.getElementById('os').value;\n";
		pageJSFuncs+="		document.location.href  = '/admin/BackupClients?type="+DOWNLOAD_AGENT_2+"&&os='+os;\n";
		pageJSFuncs+="	}\n";
	}
	
	public void printClientForm(PrintWriter _xhtml_out, CategoryManager cm, Map<String, String> client) throws Exception {
		printHeader(_xhtml_out);
		_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
		_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.multiselect.min.js\"></script>");
		_xhtml_out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery.multiselect.css\" />");
		
		_xhtml_out.println("<form action=\"/admin/BackupClients\" name=\"client\" id=\"client\" method=\"post\">");
		_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_CLIENT + "\"/>");
		if (client != null)
			_xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
	    if (client != null)
	    	_xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + client.get("name") + "\"/>");
        _xhtml_out.println("<div class=\"window\">");
		_xhtml_out.println("<h2>");
		if (client != null)
			_xhtml_out.print(client.get("name"));
		else
			_xhtml_out.print(getLanguageMessage("backup.clients.new_client"));
		
		_xhtml_out.print("<a href=\"javascript:if ($('#client').validationEngine('validate')) submitForm(document.client.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
        
		
		if (client != null && client.get("name").equals("airback-fd")) {
			_xhtml_out.println("<fieldset>");
			_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"fileRetention\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.file_retention"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"fileRetention\" value=\"");
	    	_xhtml_out.print(client.get("fileretention"));
	    	_xhtml_out.println("\"/>");
			_xhtml_out.println("<select class=\"form_select\" name=\"fileRetentionUnits\">");
			_xhtml_out.print("<option value=\"days\"");
			if(client.get("fileretention-period").equals("days")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.days"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"weeks\"");
			if(client.get("fileretention-period").equals("weeks")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.weeks"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"months\" ");
			if(client.get("fileretention-period").equals("months")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.months"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"years\"");
			if(client.get("fileretention-period").equals("years")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.years"));
			_xhtml_out.println("</option>");
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"jobRetention\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.job_retention"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"jobRetention\" value=\"");
	    	_xhtml_out.print(client.get("jobretention"));
	    	_xhtml_out.println("\"/>");
			_xhtml_out.println("<select class=\"form_select\" name=\"jobRetentionUnits\">");
			_xhtml_out.print("<option value=\"days\"");
			if(client.get("jobretention-period").equals("days")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print(">");
			_xhtml_out.print(getLanguageMessage("common.message.days"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"weeks\"");
			if(client.get("jobretention-period").equals("weeks")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print(">");
			_xhtml_out.print(getLanguageMessage("common.message.weeks"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"months\" ");
			if(client.get("jobretention-period").equals("months")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.months"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"years\"");
			if(client.get("jobretention-period").equals("years")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print(">");
			_xhtml_out.print(getLanguageMessage("common.message.years"));
			_xhtml_out.println("</option>");
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</fieldset>");
            _xhtml_out.println("<div class=\"clear\"/></div>");
	    	_xhtml_out.println("</form>");
		} else {
			String dns_name = "";
			String[] _address = new String[] { "", "", "", ""};
			if (client != null) {
				if (NetworkManager.isValidAddress(client.get("address"))) {
					_address = NetworkManager.toAddress(client.get("address"));
				} else {
					dns_name = client.get("address");
				}
			}
			_xhtml_out.println("<fieldset>");
            _xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"_name\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.name"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" ");
	    	if(client != null) {
	    		_xhtml_out.print("name=\"_name\" value=\""+client.get("name")+"\" disabled=\"disabled\" ");
	    	} else
	    		_xhtml_out.print("name=\"name\" value=\"\" />");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"categories\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.category"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<select class=\"form_select\" name=\"categories\" id=\"categories\" multiple=\"multiple\">");
	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	    	List<String> categories = new ArrayList<String>();
	    	if(client != null && client.containsKey("category")) {
	    		categories = Arrays.asList(client.get("category").split(","));
	    	}
	    	for(String cat : cm.getCategoryNames()) {
				_xhtml_out.print("<option value=\"");
				_xhtml_out.print(cat);
				_xhtml_out.print("\"");
				if(client != null && categories.contains(cat)) {
					_xhtml_out.print(" selected=\"selected\"");
				}
				_xhtml_out.print(">");
				_xhtml_out.print(cat);
				_xhtml_out.println("</option>");
			}
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"ip1r\">");
	    	_xhtml_out.print(getLanguageMessage("common.network.address"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[groupRequired[ip],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip1\" id=\"ip1\" value=\"");
	    	_xhtml_out.print(_address[0]);
	    	_xhtml_out.print("\"/>");
            _xhtml_out.print(".");
            _xhtml_out.print("<input class=\"validate[condRequired[ip1],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip2\" id=\"ip2\" value=\"");
	    	_xhtml_out.print(_address[1]);
	    	_xhtml_out.print("\"/>");
            _xhtml_out.print(".");
            _xhtml_out.print("<input class=\"validate[condRequired[ip2],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip3\" id=\"ip3\" value=\"");
	    	_xhtml_out.print(_address[2]);
	    	_xhtml_out.print("\"/>");
            _xhtml_out.print(".");
            _xhtml_out.print("<input class=\"validate[condRequired[ip3],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip4\" id=\"ip4\" value=\"");
	    	_xhtml_out.print(_address[3]);
	    	_xhtml_out.print("\"/>");
	    	_xhtml_out.println(" <img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
	    	_xhtml_out.print("<label for=\"dns_name\">");
	    	_xhtml_out.print(getLanguageMessage("common.network.dns_name"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"form_text validate[groupRequired[ip]]\" type=\"text\" name=\"dns_name\" value=\"");
	    	_xhtml_out.print(dns_name);
	    	_xhtml_out.print("\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form vmware\"");
	    	if (client != null && client.get("os").equals("vmware"))
	    		_xhtml_out.println("");
	    	else
	    		_xhtml_out.println(" style=\"display:none;\" ");
	    	_xhtml_out.print(">");
	    	_xhtml_out.print("<label for=\"username\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.username"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumberUsername]] form_text\" type=\"text\" name=\"username\" value=\"");
	    	if (client != null)
	    		_xhtml_out.print(client.get("username"));
	    	_xhtml_out.println("\"/>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"password\">");
	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"password\" name=\"password\" value=\"");
	    	if (client != null)
	    		_xhtml_out.print(client.get("password"));
	    	_xhtml_out.println("\"/>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
	    	_xhtml_out.print("<label for=\"port\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.port"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"port\" value=\"");
	    	if (client != null && client.containsKey("port"))
	    		_xhtml_out.print(client.get("port"));
	    	else 
	    		_xhtml_out.print("9102");
	    	_xhtml_out.print("\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"os\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.os"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<select class=\"form_select\" name=\"os\" id=\"os\" onchange=\"checkOS()\">");
	    	for (String _osKey : ClientManager.getClientSupportedSOs().keySet()) {
    			_xhtml_out.println("<option value=\""+_osKey+"\"");
	    		if((client != null && client.get("os").equals(_osKey)) || (client == null && _osKey.equals("win32"))) {
					_xhtml_out.print(" selected=\"selected\"");
				}
	    		_xhtml_out.println(">"+ClientManager.getClientSupportedSOs().get(_osKey)+"</option>");
	    	}
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
	    	_xhtml_out.print("<label for=\"charset\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.charset"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<select class=\"form_select\" name=\"charset\">");
	    	_xhtml_out.print("<option value=\"UTF-8\"");
			if(client == null || (client != null &&  client.get("charset") != null && client.get("charset").equals("UTF-8"))) {
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print(">UTF-8/Unicode</option>");
			_xhtml_out.print("<option value=\"ISO-8859-1\"");
			if(client != null && client.get("charset") != null && client.get("charset").equals("ISO-8859-1")) {
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.println(">ISO-8859-1</option>");
			_xhtml_out.print("<option value=\"ISO-8859-15\"");
			if(client != null && client.get("charset") != null && client.get("charset").equals("ISO-8859-15")) {
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.println(">ISO-8859-15</option>");
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
	    	_xhtml_out.print("<label for=\"fileRetention\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.file_retention"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"fileRetention\" value=\"");
	    	if (client != null && client.containsKey("fileretention"))
	    		_xhtml_out.print(client.get("fileretention"));
	    	else
	    		_xhtml_out.print("3");
	    	_xhtml_out.println("\"/>");
			_xhtml_out.println("<select class=\"form_select\" name=\"fileRetentionUnits\">");
			_xhtml_out.print("<option value=\"days\"");
			if(client != null && client.containsKey("fileretention-period") && client.get("fileretention-period").equals("days")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.days"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"weeks\"");
			if(client != null && client.containsKey("fileretention-period") && client.get("fileretention-period").equals("weeks")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.weeks"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"months\" ");
			if(client == null || (client != null && client.containsKey("fileretention-period") && client.get("fileretention-period").equals("months"))) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.months"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"years\"");
			if(client != null && client.containsKey("fileretention-period") && client.get("fileretention-period").equals("years")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.years"));
			_xhtml_out.println("</option>");
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form notvmware\">");
	    	_xhtml_out.print("<label for=\"jobRetention\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.job_retention"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<input class=\"validate[required,custom[integer]] form_text\" type=\"text\" name=\"jobRetention\" value=\"");
	    	if (client != null && client.containsKey("jobretention"))
	    		_xhtml_out.print(client.get("jobretention"));
	    	else
	    		_xhtml_out.print("3");
	    	_xhtml_out.println("\"/>");
			_xhtml_out.println("<select class=\"form_select\" name=\"jobRetentionUnits\">");
			_xhtml_out.print("<option value=\"days\"");
			if(client != null && client.containsKey("jobretention-period") && client.get("jobretention-period").equals("days")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print(">");
			_xhtml_out.print(getLanguageMessage("common.message.days"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"weeks\"");
			if(client != null && client.containsKey("jobretention-period") && client.get("jobretention-period").equals("weeks")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print(">");
			_xhtml_out.print(getLanguageMessage("common.message.weeks"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"months\" ");
			if(client == null || (client != null && client.containsKey("jobretention-period") && client.get("jobretention-period").equals("months"))) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print("\">");
			_xhtml_out.print(getLanguageMessage("common.message.months"));
			_xhtml_out.println("</option>");
			_xhtml_out.print("<option value=\"years\"");
			if(client != null && client.containsKey("jobretention-period") && client.get("jobretention-period").equals("years")) { 
				_xhtml_out.print(" selected=\"selected\"");
			}
			_xhtml_out.print(">");
			_xhtml_out.print(getLanguageMessage("common.message.years"));
			_xhtml_out.println("</option>");
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("</div>");
	    	
	    	_xhtml_out.println("<div class=\"standard_form\" id=\"autoDeployDiv\" ");
	    	if (client != null && (client.get("os").equals(ClientManager.SO_MAC) || client.get("os").contains("redhat")  || client.get("os").contains("suse") || client.get("os").contains("solaris")))
	    		_xhtml_out.println("style=\"display:none;\"");
	    	_xhtml_out.print(">");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"autoDeploy\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.autoDeploy"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"form_text\" type=\"checkbox\" name=\"autoDeploy\" id=\"autoDeploy\" onClick=\"checkAutoDeploy();\" />");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"username\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.clientUsername"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"text\" name=\"autoDeployUsername\" id=\"autoDeployUsername\" disabled=\"disabled\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"password\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.clientPassword"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"password\" name=\"autoDeployPassword\" id=\"autoDeployPassword\" disabled=\"disabled\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</fieldset>");
            _xhtml_out.println("<div class=\"clear\"/></div>");
	    	_xhtml_out.println("</form>");
	    	
	    	_xhtml_out.println("<div id=\"downloadAgent\" ");
	    	if (client == null || (client != null && (!client.get("os").contains("redhat") && !client.get("os").contains("suse") && !client.get("os").contains("solaris") && !client.get("os").equals("linux") && !client.get("os").equals("vmware"))))
	    		_xhtml_out.println("style=\"display:none;\"");
	    	_xhtml_out.println(">");
	    	_xhtml_out.print("<label for=\"agent_down\">");
	    	_xhtml_out.print(getLanguageMessage("backup.clients.downloadAgent"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("1.<a name=\"agent_down\" id =\"agent_down\" href=\"javascript:urlDownload2();\">");
	    	_xhtml_out.println("<img alt=\"Download common\" title=\"Download common\" src=\"/images/arrow_down_32.png\">");
	    	_xhtml_out.println("</a>");
	    	_xhtml_out.println("2.<a name=\"agent_down\" id =\"agent_down\" href=\"javascript:urlDownload();\">");
	    	_xhtml_out.println("<img alt=\"Download package\" title=\"Download package\" src=\"/images/arrow_down_32.png\">");
	    	_xhtml_out.println("</a>");
	    	_xhtml_out.println("<div class=\"clear\" style=\"height:15px;\"/></div>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</div>");
	    	printJSAutoDeploy();
	    	pageJS+="checkOS();\n";
	    	pageJS+="checkAutoDeploy();\n";
	    	pageJS+="$('#categories').multiselect({height: 120, checkAllText:'"+getLanguageMessage("common.message.all")+"',uncheckAllText:'"+getLanguageMessage("common.message.none")+"',noneSelectedText:'"+getLanguageMessage("common.message.selectOptions")+"',selectedText:'# "+getLanguageMessage("common.message.selected")+"', selectedList: 2});\n";
		}
	}
}
