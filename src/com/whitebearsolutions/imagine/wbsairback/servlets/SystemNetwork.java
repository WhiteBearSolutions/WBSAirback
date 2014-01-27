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

import com.whitebearsolutions.http.AbstractRequest;
import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class SystemNetwork extends WBSImagineServlet {
	static final long serialVersionUID = 9624916948716L;
	public final static int SAVE = 2;
	public final static int REMOVE_INTERFACE = 3;
	public final static int REMOVE_ROUTE = 4;
	private int type;
	public final static String baseUrl = "/admin/"+SystemNetwork.class.getSimpleName();

	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    PrintWriter _xhtml_out = response.getWriter();
	    
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	Configuration _c = this.sessionManager.getConfiguration();
	    	AbstractRequest _ar = new AbstractRequest(request);
	    	
	    	this.type = 1;
	    	try {
	    		this.type = Integer.parseInt(_ar.getParameter("type"));
	    	} catch(NumberFormatException _ex) {}
	    	    	
	    	response.setContentType("text/html");
	    	writeDocumentHeader();
	    	
	    	NetworkManager _nm = this.sessionManager.getNetworkManager();
	    	
	    	switch(this.type) {
	    		default: {
		    			int _add_interfaces = 0, _add_routes = 0, _add_nameserver = 0;
		    			if(request.getParameter("add-interfaces") != null) {
	                    	try {
	                    		_add_interfaces = Integer.parseInt(request.getParameter("add-interfaces"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
		    			if(request.getParameter("add-routes") != null) {
	                    	try {
	                    		_add_routes = Integer.parseInt(request.getParameter("add-routes"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
		    			if(request.getParameter("add-nameserver") != null) {
	                    	try {
	                    		_add_nameserver = Integer.parseInt(request.getParameter("add-nameserver"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
		    			
		    			List<String> _interfaces = _nm.getAvailableInterfaces();
		                    
		    			_xhtml_out.println("<script>");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("function send() {");
		    			_xhtml_out.println("  submitForm(document.network.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("function showSlaveInfo(interface_layer) {");
                        _xhtml_out.println("  var slaveinfo = document.getElementById(interface_layer);");
                        _xhtml_out.println("  if(slaveinfo != null) {");
                        _xhtml_out.println("    if(slaveinfo.style.visibility == \"visible\") {");
                        _xhtml_out.println("      slaveinfo.style.visibility = \"hidden\";");
                        _xhtml_out.println("    } else {");
                        _xhtml_out.println("      slaveinfo.style.visibility = \"visible\";");
                        _xhtml_out.println("    }");
                        _xhtml_out.println("  }");
                        _xhtml_out.println("}");
		    			_xhtml_out.println("//-->");
		    			_xhtml_out.println("</script>");
		    			_xhtml_out.print("<form action=\"/admin/SystemNetwork\" name=\"network\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + SAVE + "\"/>");
	    				_xhtml_out.print("<h1>");
		    			_xhtml_out.print("<img src=\"/images/network_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.network"));
		    			_xhtml_out.print("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.network.info"));
	                    _xhtml_out.print("</div>");
		    			
	                    _xhtml_out.println("<div class=\"window\" style=\"min-width:800px\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.println(getLanguageMessage("system.network.interfaces"));
	                    _xhtml_out.println("<a href=\"javascript:send();\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/SystemNetwork?add-interfaces=");
	                    _xhtml_out.print(_add_interfaces + 1);
	                    _xhtml_out.print("&add-routes=");
	                    _xhtml_out.print(_add_routes);
	                    _xhtml_out.print("&add-nameserver=");
	                    _xhtml_out.print(_add_nameserver);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset style=\"margin-left:20px;\">");
	                    
	                    _xhtml_out.println("<table style=\"width:800px;\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.interface"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.address"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.netmask"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.gateway"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.bonding"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
	                    _xhtml_out.println("</tr>");
	                    int r = 0;
	                    for(String _interface : _nm.getConfiguredInterfaces()) {
		                    _xhtml_out.println("<tr>");
		                    _xhtml_out.println("<td><select class=\"form_select\" name=\"interface" + r + "\">");
		                    for(String _iface : _interfaces) {
		                    	_xhtml_out.print("<option value=\"");
		                    	_xhtml_out.print(_iface);
		                    	_xhtml_out.print("\"");
		                    	if(_iface.equals(_interface)) {
		                    		_xhtml_out.print(" selected=\"selected\"");
		                    	}
		                    	_xhtml_out.print(">");
		                    	_xhtml_out.print(_iface);
		                    	_xhtml_out.print("</option>");
		                    }
		                    _xhtml_out.println("</select></td>");
		                    _xhtml_out.println("<td>");
		                    String[] _value = _nm.getAddress(_interface);
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip" + r + "1\" value=\"" + _value[0] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "2\" value=\"" + _value[1] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "3\" value=\"" + _value[2] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "4\" value=\"" + _value[3] + "\"/>");
		                    _xhtml_out.println("</td>");
		                    _value = _nm.getNetmask(_interface);
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "1\" value=\"" + _value[0] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "2\" value=\"" + _value[1] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "3\" value=\"" + _value[2] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "4\" value=\"" + _value[3] + "\"/>");
		                    _xhtml_out.println("</td>");
		                    _value = _nm.getGateway(_interface);
		                    _xhtml_out.print("<td>");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "1\" value=\"" + _value[0] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "2\" value=\"" + _value[1] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "3\" value=\"" + _value[2] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "4\" value=\"" + _value[3] + "\"/>");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("<td>");
	                    	if(_nm.hasSlaves(_interface)) {
		                    	_xhtml_out.println("<select class=\"form_select\" name=\"group_type_" + _interface + "\">");
			                    _xhtml_out.print("<option value=\"rr\"");
			                    if(_nm.getBondType(_interface) == NetworkManager.BONDING_RR) {
			                    	_xhtml_out.print(" selected=\"selected\"");
			                    }
			                    _xhtml_out.println(">Round-Robin</option>");
			                    _xhtml_out.print("<option value=\"lacp\"");
			                    if(_nm.getBondType(_interface) == NetworkManager.BONDING_LACP) {
			                    	_xhtml_out.print(" selected=\"selected\"");
			                    }
			                    _xhtml_out.println(">LACP</option>");
			                    _xhtml_out.println("</select>");
		                    	_xhtml_out.print("<input type=\"checkbox\" name=\"break\" value=\"" + _interface + "\"/>");
		                    } else {
		                    	_xhtml_out.println("<select class=\"form_select\" name=\"group_type_" + _interface + "\">");
			                    _xhtml_out.println("<option value=\"rr\">Round-Robin</option>");
			                    _xhtml_out.println("<option value=\"lacp\">LACP</option>");
			                    _xhtml_out.println("</select>");
		                    	_xhtml_out.println("<input type=\"checkbox\" name=\"slaves\" value=\"" + _interface + "\"/>");
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("<td>");
		                    if(r > 0) {
		                    	_xhtml_out.println("<a href=\"/admin/SystemNetwork?type=" + REMOVE_INTERFACE + "&interface=" + _interface + "\"><img src=\"/images/network_delete_16.png\"/></a>");
		                    }
		                    if(_nm.hasSlaves(_interface)) {
		                    	List<String> _slaves = _nm.getSlaves(_interface);
		                    	_xhtml_out.print("<a href=\"javascript:showSlaveInfo('slave_");
		                    	_xhtml_out.print(_interface);
		                    	_xhtml_out.println("')\"><img src=\"/images/eye_16.png\"/></a>");
		                    	_xhtml_out.print("<div id=\"slave_");
		                    	_xhtml_out.print(_interface);
		                    	_xhtml_out.print("\" style=\"position: absolute;background-color: #ffffff;visibility: hidden;width: 40px;");
		                    	_xhtml_out.print("padding: 10px;border: 1px solid #afbec7;text-align: left;height: ");
		                    	_xhtml_out.print(_slaves.size() * 15);
		                    	_xhtml_out.println("px;\">");
		                    	for(String _slave : _slaves) {
			                    	_xhtml_out.print(_slave);
			                    	_xhtml_out.print("<br/>");
			                    }
		                    	_xhtml_out.println("</div>");
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    r++;
	                    }
	                    
	                    _interfaces.removeAll(_nm.getConfiguredInterfaces());
	                    if(_add_interfaces > 0) {
	                    	for(int i = 0; i < _add_interfaces; i++) {
	                    		_xhtml_out.println("<tr>");
	                    		_xhtml_out.println("<td><select class=\"form_select\" name=\"interface" + r + "\">");
			                    for(String _iface : _interfaces) {
			                    	_xhtml_out.print("<option value=\"");
			                    	_xhtml_out.print(_iface);
			                    	_xhtml_out.print("\">");
			                    	_xhtml_out.print(_iface);
			                    	_xhtml_out.print("</option>");
			                    }
			                    _xhtml_out.println("</select>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip" + r + "1\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip" + r + "2\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip" + r + "3\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip" + r + "4\"/>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "1\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "2\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "3\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "4\"/>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "1\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "2\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "3\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"gw" + r + "4\"/>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.println("</tr>");
			                    r++;
	                    	}
	                    }
	                    _xhtml_out.println("</table>");
	                    
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.print("<div class=\"window\" style=\"min-width:800px;\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.network.routes"));
	                    _xhtml_out.print("<a href=\"javascript:send();\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
					    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
                        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/SystemNetwork?add-interfaces=");
	                    _xhtml_out.print(_add_interfaces);
	                    _xhtml_out.print("&add-routes=");
	                    _xhtml_out.print(_add_routes + 1);
	                    _xhtml_out.print("&add-nameserver=");
	                    _xhtml_out.print(_add_nameserver);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset style=\"margin-left:20px;\">");
	                     
	                    _xhtml_out.println("<table style=\"width:800px;\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.interface"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.address"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.netmask"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.gateway"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
	                    _xhtml_out.println("</tr>");
	                    
	                    r = 0;
		                for(String _interface : _nm.getConfiguredInterfaces()) {
		                    if(_nm.hasStaticRoutes(_interface)) {
		                    	for(Map<String, String[]> _route : _nm.getStaticRoutes(_interface)) {
		                    		_xhtml_out.println("<tr>");
		                    		_xhtml_out.println("<td><select class=\"form_select\" name=\"route_interface" + r + "\">");
				                    for(String _iface : _nm.getConfiguredInterfaces()) {
				                    	_xhtml_out.print("<option value=\"");
				                    	_xhtml_out.print(_iface);
				                    	_xhtml_out.print("\"");
				                    	if(_iface.equals(_interface)) {
				                    		_xhtml_out.print(" selected=\"selected\"");
				                    	}
				                    	_xhtml_out.print(">");
				                    	_xhtml_out.print(_iface);
				                    	_xhtml_out.print("</option>");
				                    }
				                    _xhtml_out.println("</select></td>");
				                    
				                    String[] _value = _route.get("address");
				                    _xhtml_out.print("<td>");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "1\" value=\"" + _value[0] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "2\" value=\"" + _value[1] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "3\" value=\"" + _value[2] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "4\" value=\"" + _value[3] + "\"/>");
				                    _xhtml_out.println("</td>");
				                    _value = _route.get("netmask");
				                    _xhtml_out.print("<td>");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "1\" value=\"" + _value[0] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "2\" value=\"" + _value[1] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "3\" value=\"" + _value[2] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "4\" value=\"" + _value[3] + "\"/>");
				                    _xhtml_out.println("</td>");
				                    _value = _route.get("gateway");
				                    _xhtml_out.print("<td>");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "1\" size=\"3\" value=\"" + _value[0] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "2\" size=\"3\" value=\"" + _value[1] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "3\" size=\"3\" value=\"" + _value[2] + "\"/>");
				                    _xhtml_out.print(".");
				                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "4\" size=\"3\" value=\"" + _value[3] + "\"/>");
				                    _xhtml_out.println("</td>");
				                    
				                    StringBuilder _url = new StringBuilder();
				                    _url.append("/admin/SystemNetwork?type=");
				                    _url.append(REMOVE_ROUTE);
				                    _url.append("&interface=");
				                    _url.append(_interface);
				                    _url.append("&address=");
				                    _url.append(NetworkManager.addressToString(_route.get("address")));
				                    _url.append("&netmask=");
				                    _url.append(NetworkManager.addressToString(_route.get("netmask")));
				                    _url.append("&gateway=");
				                    _url.append(NetworkManager.addressToString(_route.get("gateway")));
				                    
				                    _xhtml_out.print("<td>");
				                    _xhtml_out.print("<a href=\"");
				                    _xhtml_out.print(_url.toString());
				                    _xhtml_out.print("\"><img src=\"/images/cross_16.png\" title=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.remove"));
							        _xhtml_out.print("\" alt=\"");
							        _xhtml_out.print(getLanguageMessage("common.message.remove"));
							        _xhtml_out.println("\"/></a>");
				                    _xhtml_out.print("</td>");
				                    _xhtml_out.print("</tr>");
				                    r++;
		                    	}
		                    }
	                    }
	                    
	                    if(_add_routes > 0) {
	                    	for(int i = 0; i < _add_routes; i++) {
	                    		_xhtml_out.println("<tr>");
	                    		_xhtml_out.println("<td><select class=\"form_select\" name=\"route_interface" + r + "\">");
			                    for(String _iface : _nm.getConfiguredInterfaces()) {
			                    	_xhtml_out.print("<option value=\"");
			                    	_xhtml_out.print(_iface);
			                    	_xhtml_out.print("\">");
			                    	_xhtml_out.print(_iface);
			                    	_xhtml_out.print("</option>");
			                    }
			                    _xhtml_out.println("</select></td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "1\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "2\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "3\"/>");
			                    _xhtml_out.print(".");
				                _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_ip" + r + "4\"/>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "1\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "2\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "3\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_mask" + r + "4\"/>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "1\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "2\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "3\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"route_gw" + r + "4\"/>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.println("</tr>");
			                    r++;
	                    	}
	                    }
	                    
	                    if(r == 0) {
	                    	_xhtml_out.print("<tr><td colspan=\"5\">");
	                    	_xhtml_out.print(getLanguageMessage("system.netwotk.no_route"));
	                    	_xhtml_out.println("</td></tr>");	                    	
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	
	        	    	_xhtml_out.print("<div class=\"window\" style=\"min-width:800px;\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.network.nameservers"));
	                    _xhtml_out.print("<a href=\"javascript:send();\"><img src=\"/images/disk_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.save"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.save"));
				        _xhtml_out.println("\"/></a>");
				        _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/SystemNetwork?add-interfaces=");
	                    _xhtml_out.print(_add_interfaces);
	                    _xhtml_out.print("&add-routes=");
	                    _xhtml_out.print(_add_routes);
	                    _xhtml_out.print("&add-nameserver=");
	                    _xhtml_out.print(_add_nameserver + 1);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset style=\"margin-left:20px;\">");
	                     
	                    _xhtml_out.println("<table style=\"width:800px;\">");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("common.network.address"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	                    
	                    r = 0;
		                for(String[] _address : _nm.getNameservers()) {
		                	_xhtml_out.println("<tr>");
		                	_xhtml_out.print("<td>");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "1\" value=\"" + _address[0] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "2\" value=\"" + _address[1] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "3\" value=\"" + _address[2] + "\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "4\" value=\"" + _address[3] + "\"/>");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    r++;
	                    }
	                    
	                    if(_add_nameserver > 0) {
	                    	for(int i = 0; i < _add_nameserver; i++) {
	                    		_xhtml_out.println("<tr>");
	                    		_xhtml_out.print("<td>");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "1\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "2\"/>");
			                    _xhtml_out.print(".");
			                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "3\"/>");
			                    _xhtml_out.print(".");
				                _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"nameserver_ip" + r + "4\"/>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.println("</tr>");
			                    r++;
	                    	}
	                    }
	                    
	                    if(r == 0) {
	                    	_xhtml_out.println("<tr><td>");
	                    	_xhtml_out.print(getLanguageMessage("system.netwotk.no_server"));
	                    	_xhtml_out.println("</td></tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.println("</form>");
	    			}
                    break;
	    		case SAVE: {
		    			StringBuilder reply = new StringBuilder();
		    			if((_c.getProperty("directory.status") == null || _c.getProperty("directory.status").equals("standalone")) ) {
		    				List<String[]> _ns_servers = new ArrayList<String[]>();
		    				String[] slaves = request.getParameterValues("slaves");
		    				if(slaves != null && slaves.length <= 1) {
		    					throw new Exception(getLanguageMessage("system.network.info_combine"));
		    				} else if(slaves != null) {
		    					int _bond_type = -1;
		    					for(String _slave : slaves) {
		    						if(request.getParameter("group_type_" + _slave) != null) {
		    							if(request.getParameter("group_type_" + _slave).equalsIgnoreCase("rr")) {
		    								if(_bond_type != -1 && _bond_type != NetworkManager.BONDING_RR) {
		    									throw new Exception("group interface types must be the same");
		    								}
		    								_bond_type = NetworkManager.BONDING_RR;
		    							} else if(request.getParameter("group_type_" + _slave).equalsIgnoreCase("lacp")) {
		    								if(_bond_type != -1 && _bond_type != NetworkManager.BONDING_LACP) {
		    									throw new Exception("group interface types must be the same");
		    								}
		    								_bond_type = NetworkManager.BONDING_LACP;
		    							}
		    						}
		    					}
		    					if(_bond_type == -1) {
		    						_bond_type = NetworkManager.BONDING_RR;
		    					}
		    					_nm.setBondInterface(_nm.getNextBondInterface(), _bond_type, Arrays.asList(slaves));
		    				}
		    				
		    				String[] gatewayDefault = null;
		    				List<String> gateways = new ArrayList<String>();
		    				for(int i = 0; request.getParameter("interface" + i) != null; i++) {
		    					gatewayDefault = null;
			    				if(request.getParameter("gw" + i + "1") != null && !request.getParameter("gw" + i + "1").trim().isEmpty() &&
		    							request.getParameter("gw" + i + "2") != null && !request.getParameter("gw" + i + "2").trim().isEmpty() &&
		    							request.getParameter("gw" + i + "3") != null && !request.getParameter("gw" + i + "3").trim().isEmpty() &&
		    							request.getParameter("gw" + i + "4") != null && !request.getParameter("gw" + i + "4").trim().isEmpty()) {
			    					gatewayDefault = new String[] { request.getParameter("gw" + i + "1"), request.getParameter("gw" + i + "2"), request.getParameter("gw" + i + "3"), request.getParameter("gw" + i + "4") };
			    					if (gateways.size()>0 && !gateways.contains(NetworkManager.addressToString(gatewayDefault)))
			    						throw new Exception("only one default gateway must be configured");
			    					else
			    						gateways.add(NetworkManager.addressToString(gatewayDefault));
			    				}
		    				}
		    				
		    				gatewayDefault = NetworkManager.toAddress(gateways.iterator().next());
		    				
		    				for(int i = 0; request.getParameter("interface" + i) != null; i++) {
		    					String[] gateway = null;
			    				if(request.getParameter("gw" + i + "1") != null && !request.getParameter("gw" + i + "1").trim().isEmpty() &&
		    							request.getParameter("gw" + i + "2") != null && !request.getParameter("gw" + i + "2").trim().isEmpty() &&
		    							request.getParameter("gw" + i + "3") != null && !request.getParameter("gw" + i + "3").trim().isEmpty() &&
		    							request.getParameter("gw" + i + "4") != null && !request.getParameter("gw" + i + "4").trim().isEmpty()) {
			    					gateway = new String[] { request.getParameter("gw" + i + "1"), request.getParameter("gw" + i + "2"), request.getParameter("gw" + i + "3"), request.getParameter("gw" + i + "4") };
			    				}
			    				
		    					String _interface = request.getParameter("interface" + i);
		    					if(request.getParameter("ip" + i + "1") == null || request.getParameter("ip" + i + "1").trim().isEmpty() ||
		    							request.getParameter("ip" + i + "2") == null || request.getParameter("ip" + i + "2").trim().isEmpty() ||
		    							request.getParameter("ip" + i + "3") == null || request.getParameter("ip" + i + "3").trim().isEmpty() ||
		    							request.getParameter("ip" + i + "4") == null || request.getParameter("ip" + i + "4").trim().isEmpty()) {
		    						continue;
		    					}
		    					
		    					if(request.getParameter("mask" + i + "1") == null || request.getParameter("mask" + i + "1").trim().isEmpty() ||
		    							request.getParameter("mask" + i + "2") == null || request.getParameter("mask" + i + "2").trim().isEmpty() ||
		    							request.getParameter("mask" + i + "3") == null || request.getParameter("mask" + i + "3").trim().isEmpty() ||
		    							request.getParameter("mask" + i + "4") == null || request.getParameter("mask" + i + "4").trim().isEmpty()) {
		    						continue;
		    					}
		    					
		    					String[] address = new String[] { request.getParameter("ip" + i + "1"), request.getParameter("ip" + i + "2"), request.getParameter("ip" + i + "3"), request.getParameter("ip" + i + "4") };
			    				String[] netmask = new String[] { request.getParameter("mask" + i + "1"), request.getParameter("mask" + i + "2"), request.getParameter("mask" + i + "3"), request.getParameter("mask" + i + "4") };
				    			
			    				if(_nm.isConfiguredInterface(_interface)) {
			    					if(_nm.isBondInterface(_interface) &&
			    							request.getParameter("group_type_" + _interface) != null) {
		    							int _bond_type = NetworkManager.BONDING_RR;
		    							if(request.getParameter("group_type_" + _interface).equalsIgnoreCase("rr")) {
		    								_bond_type = NetworkManager.BONDING_RR;
		    							} else if(request.getParameter("group_type_" + _interface).equalsIgnoreCase("lacp")) {
		    								_bond_type = NetworkManager.BONDING_LACP;
		    							}
		    							if(_bond_type != _nm.getBondType(_interface)) {
		    								_nm.setBondInterfaceType(_interface, _bond_type);
		    							}
		    						}
			    					if(request.getParameterValues("break") != null && request.getParameterValues("break").length > 0) {
			    						for(String _iface : request.getParameterValues("break")) {
			    							_nm.removeBondInterface(_iface);
			    						}
			    						reply.append(getLanguageMessage("system.network"));
			    					} else if(!NetworkManager.compareAddress(address, _nm.getAddress(_interface)) ||
	                                        !NetworkManager.compareAddress(netmask, _nm.getNetmask(_interface)) ||
	                                        (gatewayDefault != null && !NetworkManager.compareAddress(gateway, _nm.getGateway(_interface)))) {
			    						_nm.setNetworkInterface(_interface, address, netmask, gateway);
			    						reply.append(getLanguageMessage("system.network"));
			    					}
			    				} else {
			    					_nm.setNetworkInterface(_interface, address, netmask, gateway);
			    					reply.append(getLanguageMessage("system.network"));
			    				}
		    				}
		    				
		    				_nm.reinitStaticRoutes();
		    				for(int i = 0; request.getParameter("route_interface" + i) != null; i++) {
		    					String _interface = request.getParameter("route_interface" + i);
		    					if(request.getParameter("route_ip" + i + "1") == null || request.getParameter("route_ip" + i + "1").trim().isEmpty() ||
		    							request.getParameter("route_ip" + i + "2") == null || request.getParameter("route_ip" + i + "2").trim().isEmpty() ||
		    							request.getParameter("route_ip" + i + "3") == null || request.getParameter("route_ip" + i + "3").trim().isEmpty() ||
		    							request.getParameter("route_ip" + i + "4") == null || request.getParameter("route_ip" + i + "4").trim().isEmpty()) {
		    						continue;
		    					}
		    					
		    					if(request.getParameter("route_mask" + i + "1") == null || request.getParameter("route_mask" + i + "1").trim().isEmpty() ||
		    							request.getParameter("route_mask" + i + "2") == null || request.getParameter("route_mask" + i + "2").trim().isEmpty() ||
		    							request.getParameter("route_mask" + i + "3") == null || request.getParameter("route_mask" + i + "3").trim().isEmpty() ||
		    							request.getParameter("route_mask" + i + "4") == null || request.getParameter("route_mask" + i + "4").trim().isEmpty()) {
		    						continue;
		    					}
		    					
		    					if(request.getParameter("route_gw" + i + "1") == null || request.getParameter("route_gw" + i + "1").trim().isEmpty() ||
		    							request.getParameter("route_gw" + i + "2") == null || request.getParameter("route_gw" + i + "2").trim().isEmpty() ||
		    							request.getParameter("route_gw" + i + "3") == null || request.getParameter("route_gw" + i + "3").trim().isEmpty() ||
		    							request.getParameter("route_gw" + i + "4") == null || request.getParameter("route_gw" + i + "4").trim().isEmpty()) {
			    					continue;
		    					} 
		    					
		    					String[] address = new String[] { request.getParameter("route_ip" + i + "1"), request.getParameter("route_ip" + i + "2"), request.getParameter("route_ip" + i + "3"), request.getParameter("route_ip" + i + "4") };
			    				String[] netmask = new String[] { request.getParameter("route_mask" + i + "1"), request.getParameter("route_mask" + i + "2"), request.getParameter("route_mask" + i + "3"), request.getParameter("route_mask" + i + "4") };
			    				String[] gateway = new String[] { request.getParameter("route_gw" + i + "1"), request.getParameter("route_gw" + i + "2"), request.getParameter("route_gw" + i + "3"), request.getParameter("route_gw" + i + "4") };
		    					
			    				if(request.getParameter("route_gw" + i + "1") == null || request.getParameter("route_gw" + i + "1").trim().isEmpty() ||
		    							request.getParameter("route_gw" + i + "2") == null || request.getParameter("route_gw" + i + "2").trim().isEmpty() ||
		    							request.getParameter("route_gw" + i + "3") == null || request.getParameter("route_gw" + i + "3").trim().isEmpty() ||
		    							request.getParameter("route_gw" + i + "4") == null || request.getParameter("route_gw" + i + "4").trim().isEmpty()) {
			    					gateway = new String[] { request.getParameter("route_gw" + i + "1"), request.getParameter("route_gw" + i + "2"), request.getParameter("route_gw" + i + "3"), request.getParameter("route_gw" + i + "4") };
		    					} 
				    			
			    				if(_nm.isConfiguredInterface(_interface)) {
			    					_nm.setStaticRoute(_interface, address, netmask, gateway);
			    					
			    					if(reply.length() > 0) {
			    						reply.append(getLanguageMessage("system.network.and_config_route"));
					    			} else {
					    				reply.append(getLanguageMessage("system.network.config_route"));
					    			}
			    				}
		    				}
		    				
		    				for(int i = 0; request.getParameter("nameserver_ip" + i + "1") != null; i++) {
		    					if(request.getParameter("nameserver_ip" + i + "1") == null || request.getParameter("nameserver_ip" + i + "1").trim().isEmpty() ||
		    							request.getParameter("nameserver_ip" + i + "2") == null || request.getParameter("nameserver_ip" + i + "2").trim().isEmpty() ||
		    							request.getParameter("nameserver_ip" + i + "3") == null || request.getParameter("nameserver_ip" + i + "3").trim().isEmpty() ||
		    							request.getParameter("nameserver_ip" + i + "4") == null || request.getParameter("nameserver_ip" + i + "4").trim().isEmpty()) {
		    						continue;
		    					}
		    					
		    					_ns_servers.add(new String[] { request.getParameter("nameserver_ip" + i + "1"), request.getParameter("nameserver_ip" + i + "2"), request.getParameter("nameserver_ip" + i + "3"), request.getParameter("nameserver_ip" + i + "4") });
		    				}
		    				
		    				if(!_ns_servers.isEmpty()) {
		    					_nm.setNameservers(_ns_servers);
		    					
		    					if(reply.length() > 0) {
		    						reply.append(getLanguageMessage("system.network.and_config_server"));
				    			} else {
				    				reply.append(getLanguageMessage("system.network.config_server"));
				    			}
		    				}
		    				
		    				if(reply != null || slaves != null) {
		    					_nm.update();
		    				}
		    			}
		    			
	    				this.sessionManager.reloadConfiguration();
	    				this.sessionManager.reloadNetworkManager();
	    				
	    				try {
		    				Command.systemCommand("/etc/init.d/networking restart");
	    				} catch (Exception ex) {}
		    			
		    			if(reply.length() > 0) {
		    				reply.append(getLanguageMessage("system.network.info_stored"));
		    				writeDocumentResponse(reply.toString(), "/admin/SystemNetwork");
		    			} else {
		    				writeDocumentResponse(null, "/admin/SystemNetwork");
		    			}
		    			
	    			}
	    			break;
	    		case REMOVE_INTERFACE: {
		    			StringBuilder reply = new StringBuilder();
		    			if((_c.getProperty("directory.status") == null || _c.getProperty("directory.status").equals("standalone")) ) {
		    				if(request.getParameter("interface") != null && !request.getParameter("interface").isEmpty()) {
		    					_nm.removeNetworkInterface(request.getParameter("interface"));
		    					reply.append(getLanguageMessage("system.network.config_route"));
		    				}
		    			}
		    			if(reply.length() > 0) {
		    				_nm.update();
		    				reply.append(getLanguageMessage("system.network.info_stored"));
		    				writeDocumentResponse(reply.toString(), "/admin/SystemNetwork");
		    			} else {
		    				writeDocumentResponse(null, "/admin/SystemNetwork");
		    			}
		    		}
	    			break;
	    		case REMOVE_ROUTE: {
	    				StringBuilder reply = new StringBuilder();
		    			if((_c.getProperty("directory.status") == null || _c.getProperty("directory.status").equals("standalone")) ) {
		    				if(request.getParameter("interface") != null && !request.getParameter("interface").isEmpty() &&
		    					request.getParameter("address") != null && !request.getParameter("address").isEmpty() &&
		    					request.getParameter("netmask") != null && !request.getParameter("netmask").isEmpty() &&
		    					request.getParameter("gateway") != null && !request.getParameter("gateway").isEmpty()) {
		    					_nm.removeStaticRoute(request.getParameter("interface"), NetworkManager.toAddress(request.getParameter("address")), NetworkManager.toAddress(request.getParameter("netmask")), NetworkManager.toAddress(request.getParameter("gateway")));
		    					reply.append(getLanguageMessage("system.network.config_route"));
		    				}
		    			}
		    			if(reply.length() > 0) {
		    				_nm.update();
		    				reply.append(getLanguageMessage("system.network.info_stored"));
		    				writeDocumentResponse(reply.toString(), "/admin/SystemNetwork");
		    			} else {
		    				writeDocumentResponse(null, "/admin/SystemNetwork");
		    			}
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