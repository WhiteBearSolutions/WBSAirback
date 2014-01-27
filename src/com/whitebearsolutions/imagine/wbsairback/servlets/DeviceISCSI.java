package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.SCSIManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.net.HACommClient;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;

public class DeviceISCSI extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int NEW_VOLUME_TARGET = 1;
	public final static int NEW_TAPE_TARGET = 2;
	public final static int NEW_EXTERNAL_TARGET_STEP1 = 3;
	public final static int NEW_EXTERNAL_TARGET_STEP2 = 4;
	public final static int LOGIN_TARGET = 5;
	public final static int LOGOUT_TARGET = 6;
	public final static int STORE_CONFIGURATION = 7;
	public final static int UPDATE_TARGETS = 8;
	public final static int STORE_TARGET = 9;
	public final static int REMOVE_TARGET = 10;
	public final static int LOGOUT_TARGET_OLD = 11;
	private int type;
	public final static String baseUrl = "/admin/"+DeviceISCSI.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}

	    	this.type = -1;
			if(request.getParameter("type") != null && !request.getParameter("type").isEmpty()) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
	    	response.setContentType("text/html");
	    	if (type != STORE_CONFIGURATION && type != UPDATE_TARGETS && type != LOGOUT_TARGET)
	    		writeDocumentHeader();
		    
			ISCSIManager _iscsim = new ISCSIManager(this.sessionManager.getConfiguration());
			switch(this.type) {
				default: {
						int _offset = 0;
						List<Map<String, String>> _volume_targets = ISCSIManager.getVolumeTargets();
						List<Map<String, String>> _tape_targets = ISCSIManager.getTapeTargets();
						_tape_targets.addAll(ISCSIManager.getChangerTargets());
						List<Map<String, String>> _external_targets = ISCSIManager.getExternalSessionTargets();
						List<Map<String, String>> _old_targets = ISCSIManager.getOldTargets();
						List<String> listwwn = SCSIManager.getWWNList();
						
						_xhtml_out.println("<form action=\"/admin/DeviceISCSI\" name=\"iscsi\" id=\"iscsi\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_CONFIGURATION + "\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_network_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.iscsi"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.iscsi.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.iscsi.local_configuration"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.iscsi.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\"/></a>");

						_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

						_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.client_iqn"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" name=\"name\" value=\"");
	        	    	_xhtml_out.print(ISCSIManager.getClientInitiatorName());
	        	    	_xhtml_out.println("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<table>");
	        	    	if (listwwn != null && !listwwn.isEmpty()) {
	        	    		_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.iscsi.wwn"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for (String wwn : listwwn) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(wwn);
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("</tr>");
		                    }
	        	    	} else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.wwwn.message.empty"));
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
						_xhtml_out.print(getLanguageMessage("device.iscsi.targets.volumes"));
						_xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
						_xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
	                    _xhtml_out.print(NEW_VOLUME_TARGET);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_volume_targets.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.iscsi.iqn"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.vg"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.lv"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _target : _volume_targets) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("iqn"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("vg"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("lv"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
			                    _xhtml_out.print(REMOVE_TARGET);
			                    _xhtml_out.print("&iqn=");
			                    _xhtml_out.print(_target.get("iqn"));
			                    _xhtml_out.println("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/drive_network_delete_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_targets"));
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
						_xhtml_out.print(getLanguageMessage("device.iscsi.targets.tapes"));
						_xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
						_xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
	                    _xhtml_out.print(NEW_TAPE_TARGET);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_tape_targets.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.iscsi.iqn"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.iscsi.targets.tapes.device"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.iscsi.targets.tapes.serial"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _target : _tape_targets) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("iqn"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print(_target.get("vendor"));
			                    _xhtml_out.print("/");
			                    _xhtml_out.print(_target.get("model"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("serial"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
			                    _xhtml_out.print(REMOVE_TARGET);
			                    _xhtml_out.print("&iqn=");
			                    _xhtml_out.print(_target.get("iqn"));
			                    _xhtml_out.println("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/drive_network_delete_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_targets"));
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
						_xhtml_out.print(getLanguageMessage("device.iscsi.external_targets"));
						_xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
	                    _xhtml_out.print(UPDATE_TARGETS);
	                    _xhtml_out.print("\"><img src=\"/images/find_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.find"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.find"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
	                    _xhtml_out.print(NEW_EXTERNAL_TARGET_STEP1);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_external_targets.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("common.network.address"));
		                    _xhtml_out.println("</td>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.iscsi.iqn"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _target : _external_targets) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("address"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("iqn"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
			                    _xhtml_out.print(LOGOUT_TARGET);
			                    _xhtml_out.print("&address=");
			                    _xhtml_out.print(_target.get("address"));
			                    _xhtml_out.print("&iqn=");
			                    _xhtml_out.print(_target.get("iqn"));
			                    _xhtml_out.println("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/drive_network_delete_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_external_targets"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
						_xhtml_out.println("</form>");
	                    
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.iscsi.old_targets"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_old_targets.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("common.network.address"));
		                    _xhtml_out.println("</td>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("common.network.port"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _target : _old_targets) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("address"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_target.get("port"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceISCSI?type=");
			                    _xhtml_out.print(LOGOUT_TARGET_OLD);
			                    _xhtml_out.print("&address=");
			                    _xhtml_out.print(_target.get("address"));
			                    _xhtml_out.print("&port=");
			                    _xhtml_out.print(_target.get("port"));
			                    _xhtml_out.println("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/drive_network_delete_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_external_targets"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
						_xhtml_out.println("</form>");
					}
					break;
				case NEW_VOLUME_TARGET: {
						List<Map<String, String>> _lvs = VolumeManager.getNoMountableLogicalVolumes();
						if (_lvs.isEmpty()) {
	    					writeDocumentResponse(getLanguageMessage("device.message.no_lvs"), "/admin/DeviceDisk");
	    				} else {
							writeDocumentBack("/admin/DeviceISCSI");
							_xhtml_out.println("<form action=\"/admin/DeviceISCSI\" name=\"iscsi\" id=\"iscsi\" method=\"post\">");
			    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" +  STORE_TARGET + "\"/>");
			    			_xhtml_out.println("<input type=\"hidden\" name=\"device_type\" value=\"volume\"/>");
		                    _xhtml_out.println("<h1>");
		    				_xhtml_out.print("<img src=\"/images/drive_network_32.png\"/>");
			    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.iscsi"));
		    				_xhtml_out.println("</h1>");
			    			_xhtml_out.print("<div class=\"info\">");
			    			_xhtml_out.print(getLanguageMessage("device.iscsi.info_management"));
			    			_xhtml_out.println("</div>");
			    			
			    			_xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.iscsi.new_target"));
							_xhtml_out.print("<a href=\"javascript:submitForm(document.iscsi.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
		        	    	_xhtml_out.print("<label for=\"iqn\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.iqn"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"iqn\" value=\"");
		        	    	_xhtml_out.print(ISCSIManager.getRandomIQN());
		        	    	_xhtml_out.print("\"/>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"volume\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.lv"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"lv\">");
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
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"user\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.chap_user"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"password\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.chap_password"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
		        	    	_xhtml_out.println("</div>");
		                    _xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    	_xhtml_out.print("</div>");
		        	    	_xhtml_out.println("</form>");
	    				}
					}
					break;
				case NEW_TAPE_TARGET: {
						List<Map<String, String>> _changer_devices = SCSIManager.getDevices(SCSIManager.CHANGER);
						List<Map<String, String>> _tape_devices = SCSIManager.getDevices(SCSIManager.TAPE);
						if(_tape_devices.isEmpty() && _changer_devices.isEmpty()) {
	    					writeDocumentResponse(getLanguageMessage("device.message.no_tapes"), "/admin/DeviceDisk");
	    				} else {
							writeDocumentBack("/admin/DeviceISCSI");
							_xhtml_out.println("<form action=\"/admin/DeviceISCSI\" name=\"iscsi\" id=\"iscsi\" method=\"post\">");
			    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" +  STORE_TARGET + "\"/>");
			    			_xhtml_out.println("<input type=\"hidden\" name=\"device_type\" value=\"tape\"/>");
		                    _xhtml_out.println("<h1>");
		    				_xhtml_out.print("<img src=\"/images/drive_network_32.png\"/>");
			    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.iscsi"));
		    				_xhtml_out.println("</h1>");
			    			_xhtml_out.print("<div class=\"info\">");
			    			_xhtml_out.print(getLanguageMessage("device.iscsi.info_management"));
			    			_xhtml_out.println("</div>");
			    			
			    			_xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.iscsi.new_target"));
							_xhtml_out.print("<a href=\"javascript:submitForm(document.iscsi.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
		        	    	_xhtml_out.print("<label for=\"iqn\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.iqn"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"iqn\" value=\"");
		        	    	_xhtml_out.print(ISCSIManager.getRandomIQN());
		        	    	_xhtml_out.print("\"/>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"changer\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.autochanger"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"changer\">");
		        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	for(Map<String, String> _device : _changer_devices) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(_device.get("device"));
								_xhtml_out.print("\">");
								_xhtml_out.print(_device.get("vendor"));
								_xhtml_out.print("/");
								_xhtml_out.print(_device.get("model"));
								_xhtml_out.println("</option>");
		        	    	}
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"tape\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.drives"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"tape\">");
		        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	for(Map<String, String> _device : _tape_devices) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(_device.get("device"));
								_xhtml_out.print("\">");
								_xhtml_out.print(_device.get("vendor"));
								_xhtml_out.print("/");
								_xhtml_out.print(_device.get("model"));
								_xhtml_out.print(" [");
								_xhtml_out.print(_device.get("device"));
								_xhtml_out.print("]");
								_xhtml_out.println("</option>");
		        	    	}
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"user\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.chap_user"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"password\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.chap_password"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
		        	    	_xhtml_out.println("</div>");
		                    _xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    	_xhtml_out.print("</div>");
		        	    	_xhtml_out.println("</form>");
	    				}
					}
					break;
				case STORE_TARGET: {
						if(request.getParameter("device_type") != null && "tape".equals(request.getParameter("device_type"))) {
							if((request.getParameter("iqn") == null || request.getParameter("iqn").isEmpty()) ||
									(request.getParameter("changer") == null || request.getParameter("changer").isEmpty()) &&
									(request.getParameter("tape") == null || request.getParameter("tape").isEmpty())) {
		    					throw new Exception(getLanguageMessage("device.iscsi.exception.attributes"));
		    				}
							if(request.getParameter("changer") != null && !request.getParameter("changer").isEmpty()) {
								ISCSIManager.addChangerTarget(request.getParameter("changer"), request.getParameter("iqn"), request.getParameter("user"), request.getParameter("password"));
							} else if(request.getParameter("tape") != null && !request.getParameter("tape").isEmpty()) {
								ISCSIManager.addTapeTarget(request.getParameter("tape"), request.getParameter("iqn"), request.getParameter("user"), request.getParameter("password"));
							}
						} else {
							if((request.getParameter("lv") == null || request.getParameter("lv").isEmpty()) ||
									(request.getParameter("iqn") == null || request.getParameter("iqn").isEmpty())) {
		    					throw new Exception(getLanguageMessage("device.iscsi.exception.attributes"));
		    				}
							String[] _volume = request.getParameter("lv").split("/");
			    			if(_volume.length < 2) {
			    				throw new Exception(getLanguageMessage("device.iscsi.exception.lv"));
			    			}
			    			ISCSIManager.addVolumeTargets(_volume[_volume.length - 2], _volume[_volume.length - 1], request.getParameter("iqn"), request.getParameter("user"), request.getParameter("password"));
						}	
		    			writeDocumentResponse(getLanguageMessage("device.iscsi.added_ok"), "/admin/DeviceISCSI");
					}
					break;
				case REMOVE_TARGET: {
						if(request.getParameter("iqn") == null || request.getParameter("iqn").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.iscsi.exception.attributes"));
	    				}
						
		    			if(request.getParameter("confirm") != null) {
		    				ISCSIManager.removeTarget(request.getParameter("iqn"));
			    			writeDocumentResponse(getLanguageMessage("device.iscsi.removed_ok"), "/admin/DeviceISCSI");
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("device.iscsi.question"), "/admin/DeviceISCSI?type=" + REMOVE_TARGET + "&iqn=" + request.getParameter("iqn") + "&confirm=true", null);
		    			}
					}
					break;
				case LOGOUT_TARGET_OLD: {
					if(request.getParameter("address") == null || request.getParameter("address").isEmpty() || 
							request.getParameter("port") == null || request.getParameter("port").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.iscsi.exception.attributes"));
    				}
					
	    			if(request.getParameter("confirm") != null) {
	    				ISCSIManager.removeOldExternalTarget(request.getParameter("address"),request.getParameter("port"));
		    			writeDocumentResponse(getLanguageMessage("device.iscsi.removed_ok"), "/admin/DeviceISCSI");
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("device.iscsi.question"), "/admin/DeviceISCSI?type=" + LOGOUT_TARGET_OLD + "&address=" + request.getParameter("address") + "&port=" + request.getParameter("port") + "&confirm=true", null);
	    			}
				}
				break;
				case NEW_EXTERNAL_TARGET_STEP1: {
						writeDocumentBack("/admin/DeviceISCSI");
						_xhtml_out.println("<form action=\"/admin/DeviceISCSI\" name=\"iscsi\" id=\"iscsi\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" +  NEW_EXTERNAL_TARGET_STEP2 + "\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_network_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.iscsi"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.iscsi.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.iscsi.new_external_target"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.iscsi.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"ip1\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.server_address"));
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
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	_xhtml_out.println("</form>");
					}
					break;
				case NEW_EXTERNAL_TARGET_STEP2: {
						int[] address = new int[4];
						for(int i = 1; i <= 4; i++) {
							if(request.getParameter("ip" + i) == null || request.getParameter("ip" + i).isEmpty()) {
								throw new Exception(getLanguageMessage("device.iscsi.exception.IP"));
							}
							try {
								address[i - 1] = Integer.parseInt(request.getParameter("ip" + i));
							} catch(NumberFormatException _ex) {
								throw new Exception(getLanguageMessage("device.iscsi.exception.IP"));
							}
						}
						
						List<String> _targets = ISCSIManager.searchExternalTargets(address);
						
						_xhtml_out.println("<form action=\"/admin/DeviceISCSI\" name=\"iscsi\" id=\"iscsi\" method=\"post\">");
						_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + LOGIN_TARGET + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"ip1\" value=\"" + request.getParameter("ip1") + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"ip2\" value=\"" + request.getParameter("ip2") + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"ip3\" value=\"" + request.getParameter("ip3") + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"ip4\" value=\"" + request.getParameter("ip4") + "\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_network_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.iscsi"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.iscsi.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.iscsi.new_external_target"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.iscsi.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    _xhtml_out.println("<table>");
	                    if(!_targets.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td class=\"title\">&nbsp;</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.iscsi.iqn"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    for(String _target : _targets) {
			                    _xhtml_out.println("<tr>");
			                    _xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"target\" value=\"");
			                    _xhtml_out.print(_target);
			                    _xhtml_out.print("\"/></td>");
								_xhtml_out.println("<td>");
			                    _xhtml_out.println(_target);
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_external_targets"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.iscsi.new_external_target"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.iscsi.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"method\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.iscsi.auth_type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"method\">");
						_xhtml_out.println("<option value=\"CHAP\">CHAP</option>");
						_xhtml_out.println("<option value=\"PAP\">PAP</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"user\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	                    
		    			_xhtml_out.println("</form>");
					}
        		    break;
	    		case STORE_CONFIGURATION: {
		    			if(request.getParameter("iqn") != null && !request.getParameter("iqn").isEmpty()) {
		    				_iscsim.setClientInitiatorName(request.getParameter("iqn"));
		    			}
		    			response.sendRedirect("/admin/DeviceISCSI");
		    			this.redirected=true;
		    		}
	    			break;
	    		case LOGIN_TARGET: {
		    			int[] address = new int[4];
						for(int i = 1; i <= 4; i++) {
							if(request.getParameter("ip" + i) == null || request.getParameter("ip" + i).isEmpty()) {
								throw new Exception(getLanguageMessage("device.iscsi.exception.IP"));
							}
							try {
								address[i - 1] = Integer.parseInt(request.getParameter("ip" + i));
							} catch(NumberFormatException _ex) {
								throw new Exception(getLanguageMessage("device.iscsi.exception.IP"));
							}
						}
		    			if(request.getParameter("target") == null) {
		    				throw new Exception(getLanguageMessage("device.iscsi.exception.target"));
		    			}
		    			
		    			for(String target : request.getParameterValues("target")) {
		    				ISCSIManager.loginExternalTarget(address, target, request.getParameter("method"), request.getParameter("user"), request.getParameter("password"));
		    				if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
		    					HACommClient.sendLoginExternalTarget(address, target, request.getParameter("method"), request.getParameter("user"), request.getParameter("password"));
		    				}
		    			}
		    			
		    			writeDocumentResponse(getLanguageMessage("device.iscsi.login_ok"), "/admin/DeviceISCSI");
	    			}
	    			break;
	    		case LOGOUT_TARGET: {
		    			if(request.getParameter("iqn") != null && !request.getParameter("iqn").isEmpty() &&
		    					request.getParameter("address") != null && !request.getParameter("address").isEmpty()) {
		    				int[] _address = new int[4];
		    				String[] address = NetworkManager.toAddress(request.getParameter("address"));
		    				for(int i = 0; i < _address.length; i++) {
		    					_address[i] = Integer.parseInt(address[i]);
		    				}
		    				
		    				ISCSIManager.logoutExternalTarget(_address, request.getParameter("iqn"));
		    				if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
		    					HACommClient.sendLogoutExternalTarget(_address, request.getParameter("iqn"));
		    				}
		    			}
		    			response.sendRedirect("/admin/DeviceISCSI");
		    			this.redirected=true;
	    			}
	    			break;
	    		case UPDATE_TARGETS: {
		    			ServiceManager.restart(ServiceManager.ISCSI_INITIATOR);
		    			response.sendRedirect("/admin/DeviceISCSI");
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
