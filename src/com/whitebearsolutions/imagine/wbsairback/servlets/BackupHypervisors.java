package com.whitebearsolutions.imagine.wbsairback.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.virtual.HypervisorManager;

public class BackupHypervisors extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int NEW_HYPERVISOR = 2;
	public final static int EDIT_HYPERVISOR = 3;
	public final static int STORE_HYPERVISOR = 4;
	public final static int REMOVE_HYPERVISOR = 5;
	public final static int HYPERVISOR_JOBS = 6;
	public final static int NEW_HYPERVISOR_JOB = 7;
	public final static int EDIT_HYPERVISOR_JOB = 8;
	public final static int STORE_HYPERVISOR_JOB = 9;
	public final static int REMOVE_HYPERVISOR_JOB = 10;
	private int type;
	public final static String baseUrl = "/admin/"+BackupHypervisors.class.getSimpleName();
	
	@SuppressWarnings("unchecked")
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
			if (type != STORE_HYPERVISOR && type != STORE_HYPERVISOR_JOB && type != REMOVE_HYPERVISOR_JOB)
				writeDocumentHeader();
		    
	    	HypervisorManager _hm = HypervisorManager.getInstance(HypervisorManager.GENERIC, null, null, null);
			
			switch(this.type) {
	    		default: {
		    			int _offset = 0;
		    			List<Map<String, String>> clients = _hm.getAllHypervisors();
	       				
	       				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/hypervisor_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.backup.hypervisors"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.hypervisors.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.hypervisors"));
	                    _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/BackupHypervisors?type=");
	                    _xhtml_out.print(NEW_HYPERVISOR);
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                    _xhtml_out.print("\" alt=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!clients.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.hypervisors.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.clients.os"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("common.network.address"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _client : clients) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(_client.get("name"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td><img src=\"/images/os_");
		                    	_xhtml_out.print(_client.get("os"));
		                    	_xhtml_out.println(".png\"/></td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(_client.get("address"));
		                    	_xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/BackupHypervisors?clientName=");
								_xhtml_out.print(_client.get("name"));
								_xhtml_out.print("&type=");
								_xhtml_out.print(EDIT_HYPERVISOR);
								_xhtml_out.print("\"><img src=\"/images/hypervisor_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\"/></a>");
		                    	_xhtml_out.print("<a href=\"/admin/BackupHypervisors?type=");
								_xhtml_out.print(HYPERVISOR_JOBS);
								_xhtml_out.print("&clientName=");
								_xhtml_out.print(_client.get("name"));
								_xhtml_out.print("\"><img src=\"/images/hypervisor_job_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
		                    	_xhtml_out.println("\"/></a>");
		                    	_xhtml_out.print("<a href=\"/admin/BackupHypervisors?clientName=");
								_xhtml_out.print(_client.get("name"));
								_xhtml_out.print("&type=");
								_xhtml_out.print(REMOVE_HYPERVISOR);
								_xhtml_out.print("\"><img src=\"/images/cross_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
								_offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_hypervisors"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
		    		}
	    			break;
	    		case NEW_HYPERVISOR: {
		    			
	        		    writeDocumentBack("/admin/BackupHypervisors");
	        		    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/hypervisor_32.png\"/>");
	    				_xhtml_out.print(getLanguageMessage("common.menu.backup.hypervisors"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.hypervisors.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<form action=\"/admin/BackupHypervisors\" name=\"client\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_HYPERVISOR + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"no\"/>");
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.hypervisors.new_hypervisor"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.client.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"hypervisor_type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"hypervisor_type\">");
				    	_xhtml_out.println("<option value=\"vmware\">VMware vSphere</option>");
				    	_xhtml_out.println("<option value=\"xen\">XenServer</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ip1\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.address"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip1\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip2\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip3\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip4\"/>");
	                    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.user"));
	        	    	_xhtml_out.println("</label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</form>");
		    		}
		    		break;
	    		case EDIT_HYPERVISOR: {
		    			
		    			if(request.getParameter("clientName") == null || request.getParameter("clientName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.hypervisors.client"));
		    			}
		    			
		    			Map<String, String> client = _hm.getHypervisor(request.getParameter("clientName"));
		    			
		    			String[] _address = new String[] { "", "", "", ""};
		    			if(NetworkManager.isValidAddress(client.get("address"))) {
		    				_address = NetworkManager.toAddress(client.get("address"));
		    			}
		    			
		    			writeDocumentBack("/admin/BackupHypervisors");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/hypervisor_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.backup.hypervisors"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.hypervisors.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<form action=\"/admin/BackupHypervisors\" name=\"client\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_HYPERVISOR + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + client.get("name") + "\"/>");
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(client.get("name"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.client.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"_name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.clients.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    	_xhtml_out.print(client.get("name"));
	        	    	_xhtml_out.print("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"hypervisor_type\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"hypervisor_type\">");
				    	_xhtml_out.print("<option value=\"vmware\"");
				    	if("vmware".equalsIgnoreCase(client.get("os"))) {
				    		_xhtml_out.print(" selected=\"selected\"");
				    	}
				    	_xhtml_out.print(">VMware vSphere</option>");
				    	_xhtml_out.print("<option value=\"xen\"");
				    	if("xen".equalsIgnoreCase(client.get("os"))) {
				    		_xhtml_out.print(" selected=\"selected\"");
				    	}
				    	_xhtml_out.print(">XenServer</option>");
				    	_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ip1r\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.address"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip1\" value=\"");
	        	    	_xhtml_out.print(_address[0]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip2\" value=\"");
	        	    	_xhtml_out.print(_address[1]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip3\" value=\"");
	        	    	_xhtml_out.print(_address[2]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip4\" value=\"");
	        	    	_xhtml_out.print(_address[3]);
	        	    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.user"));
	        	    	_xhtml_out.println("</label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\"");
	        	    	if(client.containsKey("user")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(client.get("user"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"");
	        	    	if(client.containsKey("password")) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(client.get("password"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</form>");
			    	}
		    		break;
	    		case STORE_HYPERVISOR: {
		    			
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
	                    } else if(request.getParameter("dns_name") != null &&
	                    		!request.getParameter("dns_name").isEmpty()) {
	                    	_address.append(request.getParameter("dns_name"));
	                    }
						
	                    int _hypervisor_type = HypervisorManager.GENERIC;
	                    if("vmware".equalsIgnoreCase(request.getParameter("hypervisor_type"))) {
	                    	_hypervisor_type = HypervisorManager.VMWARE_VSPHERE;
	                    } else if("xen".equalsIgnoreCase(request.getParameter("hypervisor_type"))) {
	                    	_hypervisor_type = HypervisorManager.XEN;
	                    }
	                    
						_hm.setHypervisor(request.getParameter("name"), _hypervisor_type, _address.toString(), request.getParameter("user"), request.getParameter("password"));
		    			response.sendRedirect("/admin/BackupHypervisors");
		    			this.redirected=true;
		    		}
	    			break;
	    		case REMOVE_HYPERVISOR: {
		    			if(request.getParameter("confirm") != null) {
		    				_hm.deleteHypervisor(request.getParameter("clientName"));
				    		writeDocumentResponse(getLanguageMessage("backup.message.client.removed"), "/admin/BackupHypervisors");
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("backup.hypervisors.job.question"), "/admin/BackupHypervisors?type=" + REMOVE_HYPERVISOR + "&clientName=" + request.getParameter("clientName") + "&confirm=true", null);
		    			}
		    		}
	    			break;
	    		case HYPERVISOR_JOBS: {
		    			int _offset = 0;
		    			if(request.getParameter("clientName") == null || request.getParameter("clientName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.hypervisors.invalid_name"));
		    			}
		    			
		    			List<Map<String, Object>> _hypervisor_jobs = _hm.getHypervisorJobs(request.getParameter("clientName"));
		    			
		    			writeDocumentBack("/admin/BackupHypervisors");
	       				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/hypervisor_job_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(request.getParameter("clientName"));
	                    _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/BackupHypervisors?type=");
	                    _xhtml_out.print(NEW_HYPERVISOR_JOB);
	                    _xhtml_out.print("&clientName=");
	                    _xhtml_out.print(request.getParameter("clientName"));
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_hypervisor_jobs.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.hypervisors.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.hypervisors.job.vms"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.hypervisors.job.storage"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, Object> _hypervisor_job : _hypervisor_jobs) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(String.valueOf(_hypervisor_job.get("name")));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	if(_hypervisor_job.containsKey("vm")) {
		                    		_xhtml_out.print(((List<String>) _hypervisor_job.get("vm")).size());
		                    	} else {
		                    		_xhtml_out.print("0");
		                    	} 
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(String.valueOf(_hypervisor_job.get("storage")));
		                    	_xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/BackupHypervisors?name=");
			                    _xhtml_out.print(String.valueOf(_hypervisor_job.get("name")));
								_xhtml_out.print("&type=");
								_xhtml_out.print(EDIT_HYPERVISOR_JOB);
								_xhtml_out.print("\"><img src=\"/images/hypervisor_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\"/></a>");
		                    	_xhtml_out.print("<a href=\"/admin/BackupHypervisors?name=");
		                    	_xhtml_out.print(String.valueOf(_hypervisor_job.get("name")));
								_xhtml_out.print("&type=");
								_xhtml_out.print(REMOVE_HYPERVISOR_JOB);
								_xhtml_out.print("\"><img src=\"/images/cross_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
								_offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_hypervisor_jobs"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
		    		}
	    			break;
	    		case NEW_HYPERVISOR_JOB: {
		    			
	    				int _add_vm = 1, _add_ds = 1;
		    			
		    			if(request.getParameter("clientName") == null || request.getParameter("clientName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.hypervisors.invalid_name"));
		    			}
		    			if(request.getParameter("add-ds") != null && !request.getParameter("add-ds").isEmpty()) {
		    				try {
		    					_add_ds = Integer.parseInt(request.getParameter("add-ds"));
		    				} catch(NumberFormatException _ex) {}
		    			}
		    			if(request.getParameter("add-vm") != null && !request.getParameter("add-vm").isEmpty()) {
		    				try {
		    					_add_vm = Integer.parseInt(request.getParameter("add-vm"));
		    				} catch(NumberFormatException _ex) {}
		    			}
		    			List<String> _job_dss = new ArrayList<String>();
		    			List<String> _job_vms = new ArrayList<String>();
		    			if (request.getParameter("ds0")!=null){
			    			for(int r = 0; request.getParameter("ds" + r) != null; r++) {
								if(!request.getParameter("ds" + r).isEmpty()) {
									_job_dss.add(request.getParameter("ds" + r));
								}
							}
		    			}
		    			if (request.getParameter("vm0")!=null){
		    				for(int r = 0; request.getParameter("vm" + r) != null; r++) {
								if(!request.getParameter("vm" + r).isEmpty()) {
									_job_vms.add(request.getParameter("vm" + r));
								}
							}
		    			}
		    			
		    			List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
		    			HypervisorManager _hmi = HypervisorManager.getInstance(_hm.getHypervisor(request.getParameter("clientName")));
		    			List<String> _dss = _hmi.getStoreNames();
		    			List<String> _vms = _hmi.getVirtualMachineNames();
		    			
		    			_xhtml_out.println("<script>");
		    			_xhtml_out.println("function AddDS() {");
		    			_xhtml_out.print("  document.getElementById('add-ds').value = ");
		    			_xhtml_out.print(_add_ds+1);
		    			_xhtml_out.println(";");
		    			_xhtml_out.print("  document.getElementById('add-vm').value = 0;");
		    			_xhtml_out.print("  var i=0;");
		    			_xhtml_out.print("  while (document.getElementById(\"vm\"+i) != null) {");
		    			_xhtml_out.print("  		document.getElementById(\"vm\"+i).value='';");
		    			_xhtml_out.print("  		i++;");
		    			_xhtml_out.println("}");
		    			_xhtml_out.print("  document.getElementById('type').value = ");
		    			_xhtml_out.print(NEW_HYPERVISOR_JOB);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.hypervisor_job.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("function AddVM() {");
		    			_xhtml_out.print("  document.getElementById('add-vm').value = ");
		    			_xhtml_out.print(_add_vm+1);
		    			_xhtml_out.println(";");
		    			_xhtml_out.print("  var i=0;");
		    			_xhtml_out.print("  while (document.getElementById(\"ds\"+i) != null) {");
		    			_xhtml_out.print("  		document.getElementById(\"ds\"+i).value='';");
		    			_xhtml_out.print("  		i++;");
		    			_xhtml_out.println("}");
		    			_xhtml_out.print("  document.getElementById('add-ds').value = 0;");
		    			_xhtml_out.print("  document.getElementById('type').value = ");
		    			_xhtml_out.print(NEW_HYPERVISOR_JOB);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.hypervisor_job.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("function cleanDS() {");
		    			_xhtml_out.print("  if (document.getElementById('add-ds').value > 0) {");
		    			_xhtml_out.print("  	document.getElementById('add-ds').value = 0;");
		    			_xhtml_out.print("  	var i=0;");
		    			_xhtml_out.print("  	while (document.getElementById(\"ds\"+i) != null) {");
		    			_xhtml_out.print("  		document.getElementById(\"ds\"+i).value='';");
		    			_xhtml_out.print("  		i++;");
		    			_xhtml_out.println("}");
		    			_xhtml_out.print("  document.getElementById('type').value = ");
		    			_xhtml_out.print(NEW_HYPERVISOR_JOB);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.hypervisor_job.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("function cleanVM() {");
		    			_xhtml_out.print("  if (document.getElementById('add-vm').value > 0) {");
		    			_xhtml_out.print("  	document.getElementById('add-vm').value = 0;");
		    			_xhtml_out.print("  var i=0;");
		    			_xhtml_out.print("  while (document.getElementById(\"vm\"+i) != null) {");
		    			_xhtml_out.print("  		document.getElementById(\"vm\"+i).value='';");
		    			_xhtml_out.print("  		i++;");
		    			_xhtml_out.println("}");
		    			_xhtml_out.print("  document.getElementById('type').value = ");
		    			_xhtml_out.print(NEW_HYPERVISOR_JOB);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.hypervisor_job.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("</script>");
	        		    writeDocumentBack("/admin/BackupHypervisors?type=" + HYPERVISOR_JOBS + "&clientName=" + request.getParameter("clientName"));
	        		    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/hypervisor_job_32.png\"/>");
	    				_xhtml_out.print(getLanguageMessage("common.menu.backup.hypervisors"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.hypervisors.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<form action=\"/admin/BackupHypervisors\" name=\"hypervisor_job\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" id=\"type\" value=\"" + STORE_HYPERVISOR_JOB + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"clientName\" value=\"" + request.getParameter("clientName") + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"no\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"add-vm\" id=\"add-vm\" value=\"" + _add_vm + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"add-ds\" id=\"add-ds\" value=\"" + _add_ds + "\"/>");
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.new_job"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.hypervisor_job.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"name\" value=\"");
        	    		_xhtml_out.print(request.getParameter("name")!=null && !request.getParameter("name").isEmpty() ? request.getParameter("name") : "");
        	    		_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mode\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.mode"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"mode\">");
				    	_xhtml_out.print("<option value=\"http\"");
				    	if("http".equals(request.getParameter("mode"))) {
				    		_xhtml_out.print(" selected=\"selected\"");
				    	}
				    	_xhtml_out.print(">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.mode_http"));
	        	    	_xhtml_out.println("</option>");
	        	    	_xhtml_out.print("<option value=\"store\"");
				    	if("store".equals(request.getParameter("mode"))) {
				    		_xhtml_out.print(" selected=\"selected\"");
				    	}
				    	_xhtml_out.print(">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.mode_store"));
	        	    	_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"storage\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.storage"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"storage\">");
	        	    	for(Map<String, String> _lv : _lvs) {
	        	    		_xhtml_out.print("<option value=\"");
	        	    		_xhtml_out.print(_lv.get("vg"));
	        	    		_xhtml_out.print("/");
	        	    		_xhtml_out.print(_lv.get("name"));
	        	    		_xhtml_out.print("\">");
	        	    		_xhtml_out.print(_lv.get("vg"));
	        	    		_xhtml_out.print("/");
	        	    		_xhtml_out.print(_lv.get("name"));
					    	if(_lv.get("vg").equals(request.getParameter("storage"))) {
					    		_xhtml_out.print(" selected=\"selected\"");
					    	}
	        	    		_xhtml_out.println("</option>");
	        	    	}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.dss"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.hypervisor_job.submit());\"><img src=\"/images/disk_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.save"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.save"));
				        _xhtml_out.println("\"/></a>");
	                	_xhtml_out.print("<a href=\"javascript:AddDS();");
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	        	    	_xhtml_out.println("<table>");
	        	    	for(int r = 0; (r < _add_ds) || (r < _job_dss.size()); r++) {
	        	    		String _value = "";
	        	    		if(r < _job_dss.size()) {
	        	    			_value = _job_dss.get(r);
	        	    		}
	        	    		_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.println("<select class=\"form_select\" name=\"ds" + r + "\" id=\"ds" + r + "\" onChange=\"cleanVM();\" >");
		                    _xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		                    for(String _ds : _dss) {
		                    	_xhtml_out.print("<option value=\"");
		                    	_xhtml_out.print(_ds);
		                    	_xhtml_out.print("\"");
						    	if(_value.equals(_ds)) {
						    		_xhtml_out.print(" selected=\"selected\"");
						    	}
						    	_xhtml_out.println(">");
		                    	_xhtml_out.print(_ds);
		                    	_xhtml_out.println("</option>");
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	        	    	}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.vms"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.hypervisor_job.submit());\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	                	_xhtml_out.print("<a href=\"javascript:AddVM();");
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	        	    	_xhtml_out.println("<table>");
	        	    	for(int r = 0; (r < _add_vm) || (r < _job_vms.size()); r++) {
	        	    		String _value = "";
	        	    		if(r < _job_vms.size()) {
	        	    			_value = _job_vms.get(r);
	        	    		}
	        	    		_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.println("<select class=\"form_select\" name=\"vm" + r + "\" id=\"vm" + r + "\" onChange=\"cleanDS();\">");
		                    _xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		                    for(String _vm : _vms) {
		                    	_xhtml_out.print("<option value=\"");
		                    	_xhtml_out.print(_vm);
		                    	_xhtml_out.print("\"");
						    	if(_value.equals(_vm)) {
						    		_xhtml_out.print(" selected=\"selected\"");
						    	}
						    	_xhtml_out.println(">");
		                    	_xhtml_out.print(_vm);
		                    	_xhtml_out.println("</option>");
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	        	    	}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("</form>");
		    		}
		    		break;
	    		case STORE_HYPERVISOR_JOB: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.hypervisors.job.invalid_name"));
		    			}
	    				if(request.getParameter("clientName") == null || request.getParameter("clientName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.hypervisors.invalid_name"));
		    			}
		    			
	    				List<String> _dss = new ArrayList<String>();
	    				List<String> _vms = new ArrayList<String>();
	    				for(int r = 0; request.getParameter("ds" + r) != null; r++) {
							if(!request.getParameter("ds" + r).isEmpty()) {
								_dss.add(request.getParameter("ds" + r));
							}
						}
	    				for(int r = 0; request.getParameter("vm" + r) != null; r++) {
							if(!request.getParameter("vm" + r).isEmpty()) {
								_vms.add(request.getParameter("vm" + r));
							}
						}
		    			
		    			_hm.setHypervisorJob(request.getParameter("name"), request.getParameter("clientName"), request.getParameter("storage"), request.getParameter("mode"), _vms, _dss);
		    			response.sendRedirect("/admin/BackupHypervisors?type=" + HYPERVISOR_JOBS + "&clientName=" + request.getParameter("clientName"));
		    			this.redirected=true;
	    			}
	    			break;
	    		case EDIT_HYPERVISOR_JOB: {
	    			
	    				int _add_vm = 1, _add_ds = 1;
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.hypervisors.job.invalid_name"));
		    			}
		    			Map<String, Object> _hypervisor_job = _hm.getHypervisorJob(request.getParameter("name"));
		    			List<String> _job_dss = new ArrayList<String>();
		    			List<String> _job_vms = new ArrayList<String>();
		    			List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
		    			HypervisorManager _hmi = HypervisorManager.getInstance(_hm.getHypervisor(String.valueOf(_hypervisor_job.get("hypervisor"))));
		    			List<String> _dss = _hmi.getStoreNames();
		    			List<String> _vms = _hmi.getVirtualMachineNames();
		    			if(request.getParameter("ds0")!=null){
			    			for(int r = 0; request.getParameter("ds" + r) != null; r++) {
								if(!request.getParameter("ds" + r).isEmpty()) {
									_job_dss.add(request.getParameter("ds" + r));
								}
							}
		    			} else if(_hypervisor_job.containsKey("ds")) {
		    				_job_dss.addAll((List<String>) _hypervisor_job.get("ds"));				    			
			    		}
		    			if(request.getParameter("vm0")!=null){
		    				for(int r = 0; request.getParameter("vm" + r) != null; r++) {
								if(!request.getParameter("vm" + r).isEmpty()) {
									_job_vms.add(request.getParameter("vm" + r));
								}
							}
		    			} else if(_hypervisor_job.containsKey("vm")) {
		    				_job_vms.addAll((List<String>) _hypervisor_job.get("vm"));		    				
		    			}
		    			
		    			if(request.getParameter("add-vm") != null && !request.getParameter("add-vm").isEmpty()) {
		    				try {
		    					_add_vm = Integer.parseInt(request.getParameter("add-vm"));
		    				} catch(NumberFormatException _ex) {}
		    			} else if(_job_vms.size() > 0) {
		    				_add_vm = _job_vms.size();
		    			} else {
		    				_add_vm = 1;
		    			}
		    			if(request.getParameter("add-ds") != null && !request.getParameter("add-ds").isEmpty()) {
		    				try {
		    					_add_ds = Integer.parseInt(request.getParameter("add-ds"));
		    				} catch(NumberFormatException _ex) {}
		    			} else if(_job_dss.size() > 0) {
		    				_add_ds = _job_dss.size();
		    			} else {
		    				_add_ds = 1;
		    			}
		    			_xhtml_out.println("<script>");
		    			_xhtml_out.println("function AddDS() {");
		    			_xhtml_out.print("  document.getElementById('add-ds').value = ");
		    			_xhtml_out.print(_add_ds+1);
		    			_xhtml_out.println(";");
		    			_xhtml_out.print("  document.getElementById('type').value = ");
		    			_xhtml_out.print(EDIT_HYPERVISOR_JOB);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.hypervisor_job.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("function AddVM() {");
		    			_xhtml_out.print("  document.getElementById('add-vm').value = ");
		    			_xhtml_out.print(_add_vm+1);
		    			_xhtml_out.println(";");
		    			_xhtml_out.print("  document.getElementById('type').value = ");
		    			_xhtml_out.print(EDIT_HYPERVISOR_JOB);
		    			_xhtml_out.println(";");
		    			_xhtml_out.println("  submitForm(document.hypervisor_job.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("</script>");
	        		    writeDocumentBack("/admin/BackupHypervisors?type=" + HYPERVISOR_JOBS + "&clientName=" + _hypervisor_job.get("hypervisor"));
	        		    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/hypervisor_job_32.png\"/>");
	    				_xhtml_out.print(getLanguageMessage("common.menu.backup.hypervisors"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.hypervisors.info"));
		    			_xhtml_out.println("</div>");		    			
		    			_xhtml_out.println("<form action=\"/admin/BackupHypervisors\" name=\"hypervisor_job\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" id=\"type\" value=\"" + STORE_HYPERVISOR_JOB + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + _hypervisor_job.get("name") + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"add-vm\" id=\"add-vm\" value=\"" + _add_vm + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"add-ds\" id=\"add-ds\" value=\"" + _add_ds + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"clientName\" value=\"" + _hypervisor_job.get("hypervisor") + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"no\"/>");
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(request.getParameter("name"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.hypervisor_job.submit());\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"_name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
        	    		_xhtml_out.print(request.getParameter("name")!=null && !request.getParameter("name").isEmpty() ? request.getParameter("name") : String.valueOf(_hypervisor_job.get("name")));
        	    		_xhtml_out.print("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mode\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.mode"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"mode\">");
				    	_xhtml_out.print("<option value=\"http\"");
				    	if("http".equals(request.getParameter("mode")!=null && !request.getParameter("mode").isEmpty() ? request.getParameter("mode") : _hypervisor_job.get("mode"))) {
				    		_xhtml_out.print(" selected=\"selected\"");
				    	}
				    	_xhtml_out.print(">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.mode_http"));
	        	    	_xhtml_out.println("</option>");
	        	    	_xhtml_out.print("<option value=\"store\"");
				    	if("store".equals(request.getParameter("mode")!=null && !request.getParameter("mode").isEmpty() ? request.getParameter("mode") : _hypervisor_job.get("mode"))) {
				    		_xhtml_out.print(" selected=\"selected\"");
				    	}
				    	_xhtml_out.print(">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.mode_store"));
	        	    	_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"storage\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.storage"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"storage\">");
	        	    	for(Map<String, String> _lv : _lvs) {
	        	    		String _value = _lv.get("vg").concat("/").concat(_lv.get("name"));
	        	    		_xhtml_out.print("<option value=\"");
	        	    		_xhtml_out.print(_value);
	        	    		_xhtml_out.print("\"");
					    	if(_value.equals(request.getParameter("storage")!=null && !request.getParameter("storage").isEmpty() ? request.getParameter("storage") : _hypervisor_job.get("storage"))) {
					    		_xhtml_out.print(" selected=\"selected\"");
					    	}
					    	_xhtml_out.println(">");
	        	    		_xhtml_out.print(_value);
	        	    		_xhtml_out.println("</option>");
	        	    	}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.dss"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.hypervisor_job.submit());\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:AddDS();");
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	        	    	_xhtml_out.println("<table>");
	        	    	for(int r = 0; (r < _add_ds) || (r < _job_dss.size()); r++) {
	        	    		String _value = "";
	        	    		if(r < _job_dss.size()) {
	        	    			_value = _job_dss.get(r);
	        	    		}
	        	    		_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.println("<select class=\"form_select\" name=\"ds" + r + "\">");
		                    _xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		                    for(String _ds : _dss) {
		                    	_xhtml_out.print("<option value=\"");
		                    	_xhtml_out.print(_ds);
		                    	_xhtml_out.print("\"");
						    	if(_value.equals(_ds)) {
						    		_xhtml_out.print(" selected=\"selected\"");
						    	}
						    	_xhtml_out.println(">");
		                    	_xhtml_out.print(_ds);
		                    	_xhtml_out.println("</option>");
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	        	    	}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.hypervisors.job.vms"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.hypervisor_job.submit());\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");
	                	_xhtml_out.print("<a href=\"javascript:AddVM();");
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	        	    	_xhtml_out.println("<table>");
	        	    	for(int r = 0; (r < _add_vm) || (r < _job_vms.size()); r++) {
	        	    		String _value = "";
	        	    		if(r < _job_vms.size()) {
	        	    			_value = _job_vms.get(r);
	        	    		}
	        	    		_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.println("<select class=\"form_select\" name=\"vm" + r + "\">");
		                    _xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		                    for(String _vm : _vms) {
		                    	_xhtml_out.print("<option value=\"");
		                    	_xhtml_out.print(_vm);
		                    	_xhtml_out.print("\"");
						    	if(_value.equals(_vm)) {
						    		_xhtml_out.print(" selected=\"selected\"");
						    	}
						    	_xhtml_out.println(">");
		                    	_xhtml_out.print(_vm);
		                    	_xhtml_out.println("</option>");
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	        	    	}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("</form>");
		    		}
		    		break;
	    		case REMOVE_HYPERVISOR_JOB: {
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.hypervisors.job.invalid_name"));
		    			}
		    			
		    			Map<String, Object> _hypervisor_job = _hm.getHypervisorJob(request.getParameter("name"));
		    			_hm.deleteHypervisoJob(request.getParameter("name"));
		    			response.sendRedirect("/admin/BackupHypervisors?type=" + HYPERVISOR_JOBS + "&clientName=" + _hypervisor_job.get("hypervisor"));
		    			this.redirected=true;
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
