package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DrbdCmanConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.net.HACommClient;
import com.whitebearsolutions.util.Configuration;

public class HA extends WBSImagineServlet {
	public final static long serialVersionUID = 91387498;
	public final static int REQUEST = 1;
	public final static int REQUEST_CONFIRM = 2;
	public final static int REQUEST_REJECT = 3;
	public final static int BREAK = 4;
	public final static int BREAK_CONFIRM = 5;
	public final static int BREAK_REJECT = 6;
	public final static int BREAK_FORCE = 7;
	public final static int FORGET = 8;
	private Configuration _c;
	private int type;
	public final static String baseUrl = "/admin/"+HA.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter _xhtml_out = response.getWriter();
	    
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
			response.setContentType("text/html");
			writeDocumentHeader();
		    
		    this.sessionManager.reloadConfiguration();
		    File _file_request = new File(WBSAirbackConfiguration.getFileImagineHaRequest());
	    	File _file_break = new File(WBSAirbackConfiguration.getFileImagineHaBreak());
	    	this._c = new Configuration(_file_request);
	    	HACommClient _hcc = new HACommClient(this._c);
	    	NetworkManager _nm = this.sessionManager.getNetworkManager();
	    	
	    	this.type = 0;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			switch(this.type) {
	    		default: {
		    			String _status = HAConfiguration.getStatus();
		    			_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
		    			_xhtml_out.print("<form action=\"/admin/HA\" name=\"discover\" id=\"discover\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + REQUEST + "\"/>");
	    				_xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/ha_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("ha"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.println("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("ha.info_prev"));
		    			_xhtml_out.print("<strong><span style=\"color: #c3c3c3\">WBS</span><span style=\"color: #00386e\">Airback</span></strong>");
		    			_xhtml_out.print(getLanguageMessage("ha.info_post"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("ha.status"));
	                    if(_file_request.exists() && !_file_break.exists()) {
	                    	if(this._c.checkProperty("cluster.request", "request") || this._c.checkProperty("cluster.request", "break")) {
	                    		_xhtml_out.print("<a href=\"javascript:goLoading('/admin/HA?type=");
	                    		_xhtml_out.print(FORGET);
	                    		_xhtml_out.print("');\"><img alt=\"");
				    	    	_xhtml_out.print(getLanguageMessage("ha.leave"));
				    			_xhtml_out.print("\" title=\"");
				    	    	_xhtml_out.print(getLanguageMessage("ha.leave"));
				    			_xhtml_out.print("\" src=\"/images/connect_error_16.png\"/></a>&nbsp;&nbsp;&nbsp;");
	                    	}
                    	
                    		_xhtml_out.print("<a href=\"/admin/HA?type=");
                    		_xhtml_out.print(BREAK);
                    		_xhtml_out.print("\"><img alt=\"");
			    	    	_xhtml_out.print(getLanguageMessage("ha.ungroup"));
			    			_xhtml_out.print("\" title=\"");
			    	    	_xhtml_out.print(getLanguageMessage("ha.ungroup"));
			    			_xhtml_out.print("\" src=\"/images/disconnect_16.png\"/></a>");
                    		_xhtml_out.print("<a href=\"/admin/HA?type=");
                    		_xhtml_out.print(BREAK_FORCE);
                    		_xhtml_out.print("\"><img alt=\"");
			    	    	_xhtml_out.print(getLanguageMessage("ha.break"));
			    			_xhtml_out.print("\" title=\"");
			    	    	_xhtml_out.print(getLanguageMessage("ha.break"));
			    			_xhtml_out.print("\" src=\"/images/break_16.png\"/></a>");
	                    }
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label>");
		    			_xhtml_out.print(getLanguageMessage("ha.status"));
		    			_xhtml_out.println(": </label>");
		    			_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"status\" value=\"");
		    			_xhtml_out.print(_status);
		    			_xhtml_out.println("\" disabled=\"disabled\"/>");
		    			_xhtml_out.println("</div>");
		    			_xhtml_out.println("</fieldset>");
		    			_xhtml_out.println("<div class=\"clear\"></div>");
		    			_xhtml_out.println("</div>");
		    			
		    			if(_file_request.exists() && this._c.checkProperty("cluster.request", "request") && this._c.hasProperty("cluster.remote") ) {
	                    	Map<String, String> values = HACommClient.getValues(_file_request.getAbsolutePath());
	                    	_xhtml_out.println("<div class=\"window\">");
	                        _xhtml_out.println("<h2>");
	                    	_xhtml_out.print(getLanguageMessage("ha.grouping_message"));
	                    	_xhtml_out.println("</h2>");
	                    	_xhtml_out.println("<div class=\"subinfo\">");
			                _xhtml_out.print(getLanguageMessage("ha.message.group_prev"));
	                    	_xhtml_out.print("<strong><span style=\"color: #c3c3c3\">WBS</span><span style=\"color: #00386e\">Airback</span></strong>,"); 
			                _xhtml_out.print(getLanguageMessage("ha.message.group_post"));

	                    	_xhtml_out.print(values.get("cluster.remote"));
		                    _xhtml_out.print(getLanguageMessage("ha.message.group_question"));
		                    _xhtml_out.print("</div>");
		                    _xhtml_out.println("<div class=\"subquestion\">");
			    			_xhtml_out.print("<a href=\"javascript:goLoading('/admin/HA?type=");
			    			_xhtml_out.print(REQUEST_CONFIRM);
			    			_xhtml_out.print("');\">");
					        _xhtml_out.print(getLanguageMessage("common.message.accept"));
					        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
					        _xhtml_out.print("<a href=\"javascript:goLoading('/admin/HA?type=");
					        _xhtml_out.print(REQUEST_REJECT);
					        _xhtml_out.print("');\">");
					        _xhtml_out.print(getLanguageMessage("common.message.cancel"));
					        _xhtml_out.print("<img src=\"/images/cross_16.png\"/></a>");
					        _xhtml_out.print("<a href=\"javascript:goLoading('/admin/HA?type=");
					        _xhtml_out.print(FORGET);
					        _xhtml_out.print("');\">");
					        _xhtml_out.print(getLanguageMessage("ha.leave"));
					        _xhtml_out.print("<img src=\"/images/connect_error_16.png\"/></a>");
					        _xhtml_out.print("</div>");
					        _xhtml_out.println("<div class=\"clear\"></div>");
			    			_xhtml_out.println("</div>");
		    			} else if(_file_break.exists()) {
		    				Map<String, String> values = HACommClient.getValues(_file_break.getAbsolutePath());
		    				_xhtml_out.println("<div class=\"window\">");
	                        _xhtml_out.println("<h2>");
	                    	_xhtml_out.print(getLanguageMessage("ha.ungroup_message"));
	                    	_xhtml_out.println("</h2>");
	                    	_xhtml_out.println("<div class=\"subinfo\">");
		                    _xhtml_out.print(getLanguageMessage("ha.message.ungroup_prev"));
		                    _xhtml_out.print("<strong><span style=\"color: #c3c3c3\">WBS</span><span style=\"color: #00386e\">Airback</span></strong>");
		                    _xhtml_out.print(getLanguageMessage("ha.message.ungroup_post"));

		                    _xhtml_out.print(values.get("address.real"));
		                    _xhtml_out.print(getLanguageMessage("ha.message.ungroup_question"));
		                    _xhtml_out.print("</div>");
		                    _xhtml_out.println("<div class=\"subquestion\">");
			    			_xhtml_out.print("<a href=\"javascript:goLoading('/admin/HA?type=");
			    			_xhtml_out.print(BREAK_CONFIRM);
			    			_xhtml_out.print("');\">");
					        _xhtml_out.print(getLanguageMessage("common.message.accept"));
					        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");

					        _xhtml_out.print("<a href=\"javascript:goLoading('/admin/HA?type=");
					        _xhtml_out.print(BREAK_REJECT);
					        _xhtml_out.print("');\">");
					        _xhtml_out.print(getLanguageMessage("common.message.cancel"));
					        _xhtml_out.print("<img src=\"/images/cross_16.png\"/></a>");
					        _xhtml_out.print("<a href=\"javascript:goLoading('/admin/HA?type=");
					        _xhtml_out.print(FORGET);
					        _xhtml_out.print("');\">");
					        _xhtml_out.print(getLanguageMessage("ha.leave"));
					        _xhtml_out.print("<img src=\"/images/connect_error_16.png\"/></a>");
					        _xhtml_out.print("</div>");
					        _xhtml_out.println("<div class=\"clear\"></div>");
			    			_xhtml_out.println("</div>");
		    			} 
		    			
		    			if(this._c.checkProperty("directory.request", "request") || this._c.checkProperty("directory.request", "break")) {
		    				_xhtml_out.println("<div class=\"warn\">");
		    				_xhtml_out.print(getLanguageMessage("ha.message.request"));
		    				_xhtml_out.println("</div>");
		    			} else if(_status.equals("standalone")) {
		    				_xhtml_out.println("<div class=\"window\">");
	                        _xhtml_out.println("<h2>");
	                        _xhtml_out.print(getLanguageMessage("ha.message.server_list_prev"));
		                    _xhtml_out.print("<a href=\"javascript:if ($('#discover').validationEngine('validate')) javascript:submitForm(document.discover.submit());\">");
		                    _xhtml_out.print("<img alt=\"");
				    	    _xhtml_out.print(getLanguageMessage("ha.grouping"));
				    		_xhtml_out.print("\" title=\"");
				    	    _xhtml_out.print(getLanguageMessage("ha.grouping"));
				    		_xhtml_out.print("\" src=\"/images/connect_16.png\"/></a>");
		                    _xhtml_out.println("</h2>");
		    	    		_xhtml_out.println("<table>");
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.print("<td>");
	                    	_xhtml_out.print(getLanguageMessage("common.network.address"));
	                    	_xhtml_out.print(": ");
		                    _xhtml_out.print("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip1\"/>");
		                    _xhtml_out.print("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip2\"/>");
		                    _xhtml_out.print("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip3\"/>");
		                    _xhtml_out.print("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip4\"/>");
		                    _xhtml_out.print("</td>");
		                    _xhtml_out.println("</tr>");
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
			    			_xhtml_out.println("</div>");
		    			}
	                    _xhtml_out.print("</form>");
					}
	    			break;
	    		case REQUEST:
	    			if(request.getParameter("confirm") != null) {
	    				if(request.getParameter("wbsairback") == null || request.getParameter("wbsairback").isEmpty()) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_IP_remote"));
	    				}
	    				if(request.getParameter("ipv1") == null || request.getParameter("ipv2") == null || request.getParameter("ipv3") == null || request.getParameter("ipv4") == null) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_IP_virtual"));
	    				}
	    				if(request.getParameter("fence_master_ip1") == null || request.getParameter("fence_master_ip2") == null || request.getParameter("fence_master_ip3") == null || request.getParameter("fence_master_ip4") == null) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_fence_master_ip"));
	    				}
	    				if(request.getParameter("fence_slave_ip1") == null || request.getParameter("fence_slave_ip2") == null || request.getParameter("fence_slave_ip3") == null || request.getParameter("fence_slave_ip4") == null) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_fence_slave_ip"));
	    				}
	    				
	    				if(request.getParameter("masterInterface") == null || request.getParameter("masterInterface").isEmpty()) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_cluster_interface"));
	    				}
	    				
	    				String deviceLun = null;
	    				if (request.getParameter("ha_type") != null && request.getParameter("ha_type").equals("lun")) {
	    					if (request.getParameter("device") != null)
	    						deviceLun = request.getParameter("device");
	    					else
	    						throw new Exception(getLanguageMessage("ha.exception.type.lun.nodevice"));
	    				}
	    				
	    				int _fenceType = DrbdCmanConfiguration.FENCE_NONE;
	    				Map<String, String> _fenceAttributes = new HashMap<String, String>();
	    				String[] virtualAddress = new String[4];
	    				String[] fenceMasterAddress = new String[4];
	    				String[] fenceSlaveAddress = new String[4];
	    				
	    				if(request.getParameter("fence_type") != null) {
	    					if("drac5".equalsIgnoreCase(request.getParameter("fence_type"))) {
	    						_fenceType = DrbdCmanConfiguration.FENCE_DRAC;
	    					} else if("ipmi".equalsIgnoreCase(request.getParameter("fence_type"))) {
	    						_fenceType = DrbdCmanConfiguration.FENCE_IPMI;
	    					} else if("vmware".equalsIgnoreCase(request.getParameter("fence_type"))) {
	    						_fenceType = DrbdCmanConfiguration.FENCE_VMWARE;
	    					} else {
	    						throw new Exception(getLanguageMessage("ha.exception.invalid_fence_type"));
	    					}
	    				}
	    				
	    				virtualAddress[0] = request.getParameter("ipv1");
	    				virtualAddress[1] = request.getParameter("ipv2");
	    				virtualAddress[2] = request.getParameter("ipv3");
	    				virtualAddress[3] = request.getParameter("ipv4");
	    				if(!NetworkManager.isValidAddress(virtualAddress)) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_IP_virtual"));
	    				}
	    				
	    				fenceMasterAddress[0] = request.getParameter("fence_master_ip1");
	    				fenceMasterAddress[1] = request.getParameter("fence_master_ip2");
	    				fenceMasterAddress[2] = request.getParameter("fence_master_ip3");
	    				fenceMasterAddress[3] = request.getParameter("fence_master_ip4");
	    				if(!NetworkManager.isValidAddress(fenceMasterAddress)) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_fence_master_ip"));
	    				}
	    				
	    				fenceSlaveAddress[0] = request.getParameter("fence_slave_ip1");
	    				fenceSlaveAddress[1] = request.getParameter("fence_slave_ip2");
	    				fenceSlaveAddress[2] = request.getParameter("fence_slave_ip3");
	    				fenceSlaveAddress[3] = request.getParameter("fence_slave_ip4");
	    				if(!NetworkManager.isValidAddress(fenceSlaveAddress)) {
	    					throw new Exception(getLanguageMessage("ha.exception.no_fence_slave_ip"));
	    				}
	    				
	    				if(request.getParameter("fence_master_user") != null) {
	    					_fenceAttributes.put("master_login", request.getParameter("fence_master_user"));
	    				}
	    				if(request.getParameter("fence_slave_user") != null) {
	    					_fenceAttributes.put("slave_login", request.getParameter("fence_slave_user"));
	    				}
	    				if(request.getParameter("fence_master_password") != null) {
	    					_fenceAttributes.put("master_password", request.getParameter("fence_master_password"));
	    				}
	    				if(request.getParameter("fence_slave_password") != null) {
	    					_fenceAttributes.put("slave_password", request.getParameter("fence_slave_password"));
	    				}
	    				if(request.getParameter("fence_master_vm") != null) {
	    					_fenceAttributes.put("master_vmname", request.getParameter("fence_master_vm"));
	    				}
	    				if(request.getParameter("fence_slave_vm") != null) {
	    					_fenceAttributes.put("slave_vmname", request.getParameter("fence_slave_vm"));
	    				}
	    				_fenceAttributes.put("master_address", NetworkManager.addressToString(fenceMasterAddress));
	    				_fenceAttributes.put("slave_address", NetworkManager.addressToString(fenceSlaveAddress));
	    				_fenceAttributes.put("virtual_address", NetworkManager.addressToString(virtualAddress));
	    				
	    				String master_cluster_iface = request.getParameter("masterInterface");
	    				String slave_cluster_iface = null;
	    				if (request.getParameter("slaveInterface") != null && !request.getParameter("slaveInterface").isEmpty())
	    					slave_cluster_iface = request.getParameter("slaveInterface");
	    				_hcc.request(master_cluster_iface, slave_cluster_iface, virtualAddress, request.getParameter("wbsairback"), _fenceType, _fenceAttributes, deviceLun);
	    				writeDocumentResponse(getLanguageMessage("ha.message.request_ok"), "/admin/HA");
	    			} else {
	    				if(request.getParameter("wbsairback") != null || (request.getParameter("ip1") != null && request.getParameter("ip2") != null && request.getParameter("ip3") != null && request.getParameter("ip4") != null)) {
	    					String address = null;
	    					
	    					if(request.getParameter("wbsairback") != null) {
	    						address = request.getParameter("wbsairback");
	    					} else {
		    					try {
			    					if(Integer.parseInt(request.getParameter("ip1")) > 255) {
			    						throw new Exception(getLanguageMessage("ha.exception.format_IP_incorrect"));
			    					}
			    					if(Integer.parseInt(request.getParameter("ip2")) > 255) {
			    						throw new Exception(getLanguageMessage("ha.exception.format_IP_incorrect"));
			    					}
			    					if(Integer.parseInt(request.getParameter("ip3")) > 255) {
			    						throw new Exception(getLanguageMessage("ha.exception.format_IP_incorrect"));
			    					}
			    					if(Integer.parseInt(request.getParameter("ip4")) > 255) {
			    						throw new Exception(getLanguageMessage("ha.exception.format_IP_incorrect"));
			    					}
			    				} catch(NumberFormatException _ex) {
			    					throw new Exception(getLanguageMessage("ha.exception.format_IP_incorrect"));
			    				}
			    				address = request.getParameter("ip1") + "." + request.getParameter("ip2") + "." + request.getParameter("ip3") + "." + request.getParameter("ip4");
	    					}
	    					
	    					_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    					_xhtml_out.print("<form action=\"/admin/HA?group\" name=\"ha\" id=\"ha\" method=\"post\">");
		    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + REQUEST + "\">");
		    				_xhtml_out.print("<input type=\"hidden\" name=\"wbsairback\" value=\"" + address + "\">");
		    				_xhtml_out.print("<input type=\"hidden\" name=\"confirm\" value=\"true\">");
		    				_xhtml_out.println("<h1>");
			    			_xhtml_out.print("<img src=\"/images/ha_32.png\"/>");
			    	    	_xhtml_out.print(getLanguageMessage("ha"));
			    			_xhtml_out.println("</h1>");
			    			_xhtml_out.print("<div class=\"info\">");
		    				_xhtml_out.print(getLanguageMessage("ha.info_create_group"));

		    				_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"window\">");
	                        _xhtml_out.println("<h2>");
		                    _xhtml_out.print(getLanguageMessage("ha.grouping_message"));
		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.print("<fieldset>");
		                    _xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"ha_type\">");
		                    _xhtml_out.print(getLanguageMessage("ha.type"));
		                    _xhtml_out.println(": </label>");
		    				_xhtml_out.println("<select class=\"form_select\" id=\"ha_type\" name=\"ha_type\" onchange=\"javascript:changeHaType()\">");
		    				_xhtml_out.print("<option value=\"drbd\" selected=\"selected\">");
		    				_xhtml_out.print(getLanguageMessage("ha.type.syncdrbd"));
		    				_xhtml_out.print("</option>");
		    				_xhtml_out.print("<option value=\"lun\">");
		    				_xhtml_out.print(getLanguageMessage("ha.type.sharedlun"));
		    				_xhtml_out.print("</option>");
		    				_xhtml_out.println("</select>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"masterInterface\">");
		                    _xhtml_out.print(getLanguageMessage("ha.master.interface"));
		                    _xhtml_out.println(": </label>");
		        	    	List<String> _interfaces =_nm.getConfiguredInterfaces();
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"masterInterface\">");
			                for(String _iface : _interfaces) {
			                  	_xhtml_out.print("<option value=\"");
			                   	_xhtml_out.print(_iface);
			                   	_xhtml_out.print("\">");
			                   	_xhtml_out.print(_iface);
			                   	_xhtml_out.print("</option>");
			                }
			                _xhtml_out.println("</select>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"slaveInterface\">");
		                    _xhtml_out.print(getLanguageMessage("ha.slave.interface"));
		                    _xhtml_out.println(": </label>");
		                    _xhtml_out.println("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" name=\"slaveInterface\"/>");
		                    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	
		                    _xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"ipv\">");
		                    _xhtml_out.print(getLanguageMessage("ha.ip.virtual"));
		                    _xhtml_out.println(": </label>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ipv1\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ipv2\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ipv3\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"ipv4\"/>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_type\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.type"));
		                    _xhtml_out.println(": </label>");
		    				_xhtml_out.println("<select class=\"form_select\" name=\"fence_type\">");
		    				_xhtml_out.println("<option value=\"drac5\" selected=\"selected\">Appliance Remote Access Card</option>");
		    				/*_xhtml_out.println("<option value=\"ipmi\">IPMI</option>");
		    				_xhtml_out.println("<option value=\"vmware\">VMWare</option>");*/
		    				_xhtml_out.println("</select>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_master_ip\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.master.ip"));
		                    _xhtml_out.println(": </label>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_master_ip1\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_master_ip2\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_master_ip3\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_master_ip4\"/>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_slave_ip\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.slave.ip"));
		                    _xhtml_out.println(": </label>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_slave_ip1\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_slave_ip2\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_slave_ip3\"/>");
		    				_xhtml_out.println("<input class=\"validate[required,custom[integer],max[255]] network_octet\" type=\"text\" name=\"fence_slave_ip4\"/>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_master_user\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.master.user"));
		                    _xhtml_out.print(": </label>");
		    				_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"text\" name=\"fence_master_user\"/>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		    				_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_master_password\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.master.password"));
		                    _xhtml_out.print(": </label>");
		    				_xhtml_out.print("<input class=\"validate[required] form_password\" type=\"password\" name=\"fence_master_password\"/>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		    				_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_slave_user\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.slave.user"));
		                    _xhtml_out.print(": </label>");
		    				_xhtml_out.print("<input class=\"validate[required] form_text\" type=\"text\" name=\"fence_slave_user\"/>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		    				_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_slave_password\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.slave.password"));
		                    _xhtml_out.print(": </label>");
		    				_xhtml_out.print("<input class=\"validate[required] form_password\" type=\"password\" name=\"fence_slave_password\"/>");
		    				_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		    				_xhtml_out.println("</div>");
		    				/*_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_master_vm\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.master.vm"));
		                    _xhtml_out.print(": </label>");
		    				_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"fence_master_vm\"/>");
		    				_xhtml_out.println("</div>");
		    				_xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"fence_slave_vm\">");
		                    _xhtml_out.print(getLanguageMessage("ha.fence.slave.vm"));
		                    _xhtml_out.print(": </label>");
		    				_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"fence_slave_vm\"/>");
		    				_xhtml_out.println("</div>");*/
		    				
		    				List<Map<String, String>> _disks = VolumeManager.getUnassignedLuns();
		    				int _offset = 1;
		    				_xhtml_out.println("<div class=\"clear\" style=\"height:10px;\"></div>");
		    				_xhtml_out.println("<div id=\"typeLun\" name=\"typeLun\" style=\"display:none;\">");
							_xhtml_out.println("<h3>");
							_xhtml_out.print(getLanguageMessage("device.volume_groups.pvs"));
		                    _xhtml_out.println("</h3>");
		                    _xhtml_out.println("<table>");
		                    if(_disks.isEmpty()) {
		                    	_xhtml_out.println("<tr>");
		                    	_xhtml_out.println("<td>");
		                    	_xhtml_out.println(getLanguageMessage("device.message.no_unassigned_disks"));
		                    	_xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
		                    } else {
		                    	_xhtml_out.println("<tr>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv.model"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
			                    _xhtml_out.println("</tr>");
			                    for(Map<String, String> _disk : _disks) {
			                    	_xhtml_out.print("<tr");
			                    	if(_offset % 2 == 0) {
			                    		_xhtml_out.print(" class=\"highlight\"");
			                    	}
			                    	_xhtml_out.println(">");
			                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
			                    	_xhtml_out.println("<td>");
			                    	if (_disk.get("deviceId") != null && !_disk.get("deviceId").isEmpty())
			                    		_xhtml_out.println(_disk.get("deviceId"));
			                    	else
			                    		_xhtml_out.println(_disk.get("device"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println(_disk.get("vendor"));
				                    _xhtml_out.println(" / ");
				                    _xhtml_out.println(_disk.get("model"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println(_disk.get("size"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.print("<td><input class=\"validate[required] form_radio\" type=\"radio\" name=\"device\" value=\"");
				                    _xhtml_out.print(_disk.get("device"));
				                    _xhtml_out.println("\"/></td>");
			                    	_xhtml_out.println("</tr>");
			                    	_offset++;
			                    }
		                    }
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		    				_xhtml_out.println("</div>");
		    				
		    				_xhtml_out.println("<div class=\"clear\"></div>");
		    				_xhtml_out.println("<br/>");
		        	    	_xhtml_out.print("<div class=\"subwarn\">");
		                    _xhtml_out.print("<strong>");
		        	    	_xhtml_out.print(getLanguageMessage("common.message.warning"));
		        	    	_xhtml_out.print("</strong>:");
		        	    	_xhtml_out.print(getLanguageMessage("ha.message.warning"));
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"subquestion\">");
		        	    	_xhtml_out.print(getLanguageMessage("ha.message.warning_question"));
		        	    	_xhtml_out.print(" ");
		        	    	_xhtml_out.print(address);
		    				_xhtml_out.print("?");
		    				_xhtml_out.print("<br/><br/>");
		    				_xhtml_out.print("<a href=\"javascript:if ($('#ha').validationEngine('validate')) submitForm(document.ha.submit());\">");
					        _xhtml_out.print(getLanguageMessage("common.message.accept"));
					        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
					        _xhtml_out.print("<a href=\"javascript:history.go(-1);\">");
					        _xhtml_out.print(getLanguageMessage("common.message.cancel"));
					        _xhtml_out.print("<img src=\"/images/cross_16.png\"/></a>");
					        _xhtml_out.println("</div>");
		    				_xhtml_out.print("</fieldset>");
		    				_xhtml_out.println("<div class=\"clear\"></div>");
			    			_xhtml_out.println("</div>");
			    			_xhtml_out.print("</form>");
			    			
			    			_xhtml_out.println("<script type=\"text/javascript\">");
			    			_xhtml_out.println("function changeHaType() {");
			    			_xhtml_out.println("	var type = document.getElementById('ha_type').value;");
			    			_xhtml_out.println("	if (type == 'drbd') {");
			    			_xhtml_out.println("		document.getElementById('typeLun').style.display = 'none';");
			    			_xhtml_out.println("	} else if (type == 'lun') {");
			    			_xhtml_out.println("		document.getElementById('typeLun').style.display = 'block';");
			    			_xhtml_out.println("	}");
			    			_xhtml_out.println("}");
			    			_xhtml_out.println("</script>");
		    			} else {
		    				writeDocumentResponse(getLanguageMessage("ha.message.select_product_prev") + "<strong><span style=\"color: #c3c3c3\">WBS</span><span style=\"color: #00386e\">Airback</span></strong>" + getLanguageMessage("ha.message.select_product_post"), "javascript:history.go(-1);");
		    			}
		    		}
	    			break;
	    		case REQUEST_CONFIRM: {
		    			_hcc.requestConfirm();
		    			writeDocumentResponse(getLanguageMessage("ha.message.request_confirm"), "/admin/HA");
	    			}
	    			break;
	    		case REQUEST_REJECT: {
		    			_hcc.requestReject();
		    			writeDocumentResponse(getLanguageMessage("ha.message.request_reject"), "/admin/HA");
	    			}
	    			break;
	    		case FORGET: {
		    			if(!this._c.checkProperty("cluster.request", "request") && !this._c.checkProperty("cluster.request", "break")) {
		    				throw new Exception(getLanguageMessage("ha.exception.forget"));
		    			}
		    			if(request.getParameter("confirm") != null) {
		    				HACommClient.forgetRequest();
			    			writeDocumentResponse(getLanguageMessage("ha.message.forget"), "/admin/HA");
		    			} else {
		    				writeDocumentWarningQuestion(getLanguageMessage("ha.message.forget_question"), getLanguageMessage("ha.message.forget_warning"), "/admin/HA?type=" + FORGET + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case BREAK: {
		    			if(!this._c.checkProperty("cluster.status", "master")) {
		    				throw new Exception(getLanguageMessage("ha.exception.break"));
		    			}
		    			if(request.getParameter("confirm") != null) {
		    				_hcc.breakRequest();
		    				writeDocumentResponse(getLanguageMessage("ha.message.break"), "/admin/HA");
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("ha.message.break_question"), "/admin/HA?type=" + BREAK + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case BREAK_FORCE: {
		    			if(request.getParameter("confirm") != null) {
		    				_hcc.forceBreak();
							writeDocumentResponse(getLanguageMessage("ha.message.break_force"), "/admin/HA");
		    			} else {
		    				writeDocumentWarningQuestion(getLanguageMessage("ha.message.break_force_question"), getLanguageMessage("ha.message.break_force_warning"), "/admin/HA?type=" + BREAK_FORCE + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case BREAK_CONFIRM: {
		    			_hcc.breakRequestConfirm();
		    			writeDocumentResponse(getLanguageMessage("ha.message.break_confirm"), "/admin/HA");
	    			}
	    			break;
	    		case BREAK_REJECT: {
		    			_hcc.breakRequestReject();
		    			writeDocumentResponse(getLanguageMessage("ha.message.break_reject"), "/admin/HA");
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