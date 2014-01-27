package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;

public class DeviceNAS extends WBSImagineServlet {
	static final long serialVersionUID = 20080226L;
	public final static int NEW_SHARE = 2;
	public final static int NEW_SHARE_NFS = 3;
	public final static int NEW_SHARE_CIFS = 4;
	public final static int NEW_SHARE_FTP = 6;
	public final static int EDIT_SHARE = 7;
	public final static int STORE_SHARE = 8;
	public final static int REMOVE_SHARE = 9;
	public final static int NEW_EXTERNAL_SHARE = 10;
	public final static int NEW_EXTERNAL_SHARE_STORE = 11;
	public final static int REMOVE_EXTERNAL_SHARE = 12;
	public final static int MOUNT_EXTERNAL_SHARE = 13;
	public final static int UMOUNT_EXTERNAL_SHARE = 14;
	private int type;
	public final static String baseUrl = "/admin/"+DeviceNAS.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
			this.type = 1;
			if(request.getParameter("type") != null && !request.getParameter("type").isEmpty()) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
	    	response.setContentType("text/html");
	    	if (type != MOUNT_EXTERNAL_SHARE && type != UMOUNT_EXTERNAL_SHARE)
	    		writeDocumentHeader();

		    switch(this.type) {
	    		default: {
		    			int _offset = 0;	
		    			List<Map<String, String>> _shares = ShareManager.getAllShares();
	    				List<Map<String, String>> _external_shares = ShareManager.getExternalShares();
	    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/share_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.shares"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.shares.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.shares"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
	                    _xhtml_out.print(NEW_SHARE);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_shares.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.path"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.lv"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.vg"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _share : _shares) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_share.get("protocol"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    if("cifs".equals(_share.get("protocol").toLowerCase())) {
			                    	_xhtml_out.println(_share.get("name"));
			                    } else {
			                    	_xhtml_out.println(_share.get("path"));
			                    }
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    if(_share.containsKey("lv")) {
			                    	_xhtml_out.println(_share.get("lv"));
			                    }
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    if(_share.containsKey("vg")) {
			                    	_xhtml_out.println(_share.get("vg"));
			                    }
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    if(!"ftp".equals(_share.get("protocol").toLowerCase())) {
				                    _xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
				                    _xhtml_out.print(EDIT_SHARE);
				                    _xhtml_out.print("&vg=");
				                    _xhtml_out.print(_share.get("vg"));
				                    _xhtml_out.print("&lv=");
				                    _xhtml_out.print(_share.get("lv"));
				                    _xhtml_out.print("&protocol=");
				                    _xhtml_out.print(_share.get("protocol").toLowerCase());
				                    _xhtml_out.print("\" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    	_xhtml_out.print("\"><img src=\"/images/share_edit_16.png\"/></a>");
			                    }
			                    _xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
			                    _xhtml_out.print(REMOVE_SHARE);
			                    _xhtml_out.print("&vg=");
			                    if (_share.get("vg")!=null)
			                    	_xhtml_out.print(_share.get("vg"));
			                    else { 
			                    	String share = ShareManager.getNameFromMountPath(_share.get("path"));
			                    	_xhtml_out.print(share.substring(0, share.indexOf("/")));
			                    }
			                    _xhtml_out.print("&lv=");
			                    if (_share.get("lv")!=null)
			                    	_xhtml_out.print(_share.get("lv"));
			                    else { 
			                    	String share = ShareManager.getNameFromMountPath(_share.get("path"));
			                    	_xhtml_out.print(share.substring(share.indexOf("/")+1));
			                    }
			                    _xhtml_out.print("&protocol=");
			                    _xhtml_out.print(_share.get("protocol").toLowerCase());
			                    _xhtml_out.println("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/share_delete_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_shares"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
		    			
						_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.shares.external"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
	                    _xhtml_out.print(NEW_EXTERNAL_SHARE);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_external_shares.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.server"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.path"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.shares.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _share : _external_shares) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_share.get("server"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_share.get("share"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_share.get("size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_share.get("type"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
			                    if("true".equals(_share.get("mount"))) {
			                    	_xhtml_out.print(UMOUNT_EXTERNAL_SHARE);
			                    } else {
			                    	_xhtml_out.print(MOUNT_EXTERNAL_SHARE);
			                    }
			                    _xhtml_out.print("&server=");
			                    _xhtml_out.print(_share.get("server"));
			                    _xhtml_out.print("&share=");
			                    _xhtml_out.print(_share.get("share"));
			                    _xhtml_out.println("\"");
			                    if("true".equals(_share.get("mount"))) {
			                    	_xhtml_out.print(" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("device.shares.umount"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("device.shares.umount"));
			                    	_xhtml_out.print("\"");
			                    } else {
			                    	_xhtml_out.print(" title=\"");
			                    	_xhtml_out.print(getLanguageMessage("device.shares.mount"));
			                    	_xhtml_out.print("\" alt=\"");
			                    	_xhtml_out.print(getLanguageMessage("device.shares.mount"));
			                    	_xhtml_out.print("\"");
			                    }
			                    _xhtml_out.println(">");
			                    if("true".equals(_share.get("mount"))) {
			                    	_xhtml_out.println("<img src=\"/images/drive_go_16.png\"/>");
			                    } else {
			                    	_xhtml_out.println("<img src=\"/images/drive_network_16.png\"/>");
			                    }
			                    _xhtml_out.println("</a>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
			                    _xhtml_out.print(REMOVE_EXTERNAL_SHARE);
			                    _xhtml_out.print("&server=");
			                    _xhtml_out.print(_share.get("server"));
			                    _xhtml_out.print("&share=");
			                    _xhtml_out.print(_share.get("share"));
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
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_external_shares"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	    			}
        	    	break;
	    		case NEW_SHARE: {
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
	    				
		    			writeDocumentBack("/admin/DeviceNAS");
	    				_xhtml_out.println("<form action=\"/admin/DeviceNAS\" name=\"share\" id=\"share\" method=\"post\">");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/share_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.shares.new_share_step1"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.shares.info_management"));
		    			_xhtml_out.println("</div>");
	    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.shares.new_external_share"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"lv\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.lv"));
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
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"type\">");
						_xhtml_out.print("<option value=\"");
						_xhtml_out.print(NEW_SHARE_NFS);
						_xhtml_out.println("\">NFS</option>");
						_xhtml_out.print("<option value=\"");
						_xhtml_out.print(NEW_SHARE_CIFS);
						_xhtml_out.println("\">CIFS</option>");
						_xhtml_out.print("<option value=\"");
						_xhtml_out.print(NEW_SHARE_FTP);
						_xhtml_out.println("\">FTP</option>");
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("</form>");
		    		}
	    			break;
	    		case NEW_SHARE_NFS: {
		    			if(request.getParameter("lv") == null) {
	    					throw new Exception(getLanguageMessage("device.shares.exception.attributes"));
	    				}
	    				
		    			int _add_address = 1;
		    			String[] _volume = request.getParameter("lv").split("/");
		    			if(_volume.length < 2) {
		    				throw new Exception(getLanguageMessage("device.shares.exception.logical_volume"));
		    			}
		    			
		    			if(ShareManager.isShare(ShareManager.NFS, _volume[_volume.length - 2], _volume[_volume.length - 1])) {
		    				throw new Exception(getLanguageMessage("device.shares.exception.NFS_volume"));
		    			}
		    			
		    			if(request.getParameter("add-address") != null) {
	                    	try {
	                    		_add_address = Integer.parseInt(request.getParameter("add-address"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			} 
		    			
		    			_xhtml_out.println("<form action=\"/admin/DeviceNAS\" name=\"share\" id=\"share\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_SHARE + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"protocol\" value=\"nfs\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"vg\" value=\"" + _volume[_volume.length - 2] + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"lv\" value=\"" + _volume[_volume.length - 1] + "\"/>");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/share_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.shares.new_share_step2"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.shares.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.shares.nfs.options"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");

						_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.nfs.squash"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_checkbox\" type=\"checkbox\" name=\"squash\" value=\"true\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.nfs.async"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_checkbox\" type=\"checkbox\" name=\"async\" value=\"true\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	        	    	
	                    _xhtml_out.print("<div class=\"warn\">");
		    			_xhtml_out.print(getLanguageMessage("device.shares.exception.writings"));
		    			_xhtml_out.print("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.shares.nfs.address"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.save"));
	                	_xhtml_out.println("\"/></a>");

						_xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
	                    _xhtml_out.print(NEW_SHARE_NFS);
	                    _xhtml_out.print("&lv=");
	                    _xhtml_out.print(request.getParameter("lv"));
	                    _xhtml_out.print("&add-address=");
	                    _xhtml_out.print(_add_address + 1);
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	        	    	_xhtml_out.println("<table>");
	        	    	_xhtml_out.println("<tr>");
	        	    	_xhtml_out.print("<td class=\"title\">");
		                _xhtml_out.print(getLanguageMessage("common.network.address"));
		                _xhtml_out.println("</td>");
		                _xhtml_out.print("<td class=\"title\">");
		                _xhtml_out.print(getLanguageMessage("common.network.netmask"));
		                _xhtml_out.println("</td>");
		                _xhtml_out.println("</tr>");
	        	    	for(int r = 0; r < _add_address; r++) {
	        	    		_xhtml_out.println("<tr>");
		                    _xhtml_out.println("<td>");
		                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip" + r + "1\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "2\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "3\"/>");
		                    _xhtml_out.print(".");
		                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "4\"/>");
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
		                    _xhtml_out.println("</tr>");
	        	    	}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("</form>");
		    		}
		    		break;
	    		case NEW_SHARE_CIFS: {
		    			if(request.getParameter("lv") == null) {
	    					throw new Exception(getLanguageMessage("device.shares.exception.attributes"));
	    				}
	    				
		    			String[] _volume = request.getParameter("lv").split("/");
		    			if(_volume.length < 2) {
		    				throw new Exception(getLanguageMessage("device.shares.exception.logical_volume"));
		    			}
		    			
		    			if(ShareManager.isShare(ShareManager.CIFS, _volume[_volume.length - 2], _volume[_volume.length - 1])) {
		    				throw new Exception(getLanguageMessage("device.shares.exception.CIFS_volume"));
		    			}
		    			
		    			_xhtml_out.println("<form action=\"/admin/DeviceNAS\" name=\"share\" id=\"share\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_SHARE + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"protocol\" value=\"cifs\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"vg\" value=\"" + _volume[_volume.length - 2] + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"lv\" value=\"" + _volume[_volume.length - 1] + "\"/>");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/share_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.shares.new_share_step2"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.shares.info_management"));
		    			_xhtml_out.println("</div>");
			    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.shares.cifs.options"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("device.shares.info_trash_prev"));
		    			_xhtml_out.print("<strong>.recycle.</strong>");
		    			_xhtml_out.print(getLanguageMessage("device.shares.info_trash_post"));
		    		
		    			_xhtml_out.print("</div>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.cifs.recycle"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_checkbox\" type=\"checkbox\" name=\"recycle\" value=\"true\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("</form>");
		    		}
		    		break;
	    		case NEW_SHARE_FTP: {
		    			if(request.getParameter("lv") == null) {
	    					throw new Exception(getLanguageMessage("device.shares.exception.logical_volume"));
	    				}
	    				
		    			String[] _volume = request.getParameter("lv").split("/");
		    			if(_volume.length < 2) {
		    				throw new Exception(getLanguageMessage("device.shares.exception.logical_volume"));
		    			}
		    			
		    			if(ShareManager.isShare(ShareManager.FTP, _volume[_volume.length - 2], _volume[_volume.length - 1])) {
		    				throw new Exception(getLanguageMessage("device.shares.exception.FTP_volume"));
		    			}
		    			ShareManager.storeShare(_volume[_volume.length - 2], _volume[_volume.length - 1], ShareManager.FTP, new HashMap<String, String>());
		    			writeDocumentResponse(getLanguageMessage("device.shares.settings_ok"), "/admin/DeviceNAS");
		    		}
		    		break;
	    		case STORE_SHARE: {
		    			int _type = ShareManager.NFS;
	    				if(request.getParameter("protocol") == null || request.getParameter("lv") == null || request.getParameter("vg") == null) {
	    					throw new Exception(getLanguageMessage("common.message.invalid_attributes"));
	    				}
	    				
	    				if("cifs".equals(request.getParameter("protocol").toLowerCase())) {
	    					_type = ShareManager.CIFS;
	    				}
	    				
	    				Map<String, String> _attributes = new HashMap<String, String>();
	    				switch(_type) {
		    				case ShareManager.NFS: {
		    						StringBuilder _addresses = new StringBuilder();
			    					for(int i = 0; request.getParameter("ip" + i + "1") != null && request.getParameter("ip" + i + "2") != null && request.getParameter("ip" + i + "3") != null && request.getParameter("ip" + i + "4") != null; i++) {	
				    					if(request.getParameter("ip" + i + "1").trim().isEmpty() ||
				    							request.getParameter("ip" + i + "2").trim().isEmpty() ||
				    							request.getParameter("ip" + i + "3").trim().isEmpty() ||
				    							request.getParameter("ip" + i + "4").trim().isEmpty()) {
				    						continue;
				    					}
				    					
				    					StringBuilder _address = new StringBuilder();
				    					_address.append(request.getParameter("ip" + i + "1").trim());
				    					_address.append(".");
				    					_address.append(request.getParameter("ip" + i + "2").trim());
				    					_address.append(".");
				    					_address.append(request.getParameter("ip" + i + "3").trim());
				    					_address.append(".");
				    					_address.append(request.getParameter("ip" + i + "4").trim());
				    					
				    					if(request.getParameter("mask" + i + "1") != null && !request.getParameter("mask" + i + "1").trim().isEmpty() &&
					    							request.getParameter("mask" + i + "2") != null && !request.getParameter("mask" + i + "2").trim().isEmpty() &&
					    							request.getParameter("mask" + i + "3") != null && !request.getParameter("mask" + i + "3").trim().isEmpty() &&
					    							request.getParameter("mask" + i + "4") != null && !request.getParameter("mask" + i + "4").trim().isEmpty()) {
				    						_address.append("/");
				    						_address.append(request.getParameter("mask" + i + "1").trim());
					    					_address.append(".");
					    					_address.append(request.getParameter("mask" + i + "2").trim());
					    					_address.append(".");
					    					_address.append(request.getParameter("mask" + i + "3").trim());
					    					_address.append(".");
					    					_address.append(request.getParameter("mask" + i + "4").trim());
				    					}
				    					
				    					if(_addresses.length() > 0) {
				    						_addresses.append(",");
				    					}
				    					_addresses.append(_address.toString());
			    					}
			    					if(_addresses.length() > 0) {
			    						_attributes.put("address", _addresses.toString());
			    					}
			    					if(request.getParameter("squash") != null && "true".equals(request.getParameter("squash"))) {
			    						_attributes.put("squash", "true");
			    					}
			    					if(request.getParameter("async") != null && "true".equals(request.getParameter("async"))) {
			    						_attributes.put("async", "true");
			    					}
		    					}
		    					break;
		    				case ShareManager.CIFS: {
		    						if(request.getParameter("name") != null) {
		    							_attributes.put("name", request.getParameter("name"));
		    						}
		    						if(request.getParameter("recycle") != null && "true".equals(request.getParameter("recycle"))) {
		    							_attributes.put("recycle", "true");
		    						}
		    					}
		    					break;
	    				}
	    				
	    				ShareManager.storeShare(request.getParameter("vg"), request.getParameter("lv"), _type, _attributes);
	    				writeDocumentResponse(getLanguageMessage("device.shares.settings_ok"), "/admin/DeviceNAS");
	    			}
	    			break;
	    		case EDIT_SHARE: {
		    			int _type = ShareManager.NFS;
	    				if(request.getParameter("protocol") == null || request.getParameter("lv") == null || request.getParameter("vg") == null) {
	    					throw new Exception(getLanguageMessage("common.message.invalid_attributes"));
	    				}
	    				
	    				if("cifs".equals(request.getParameter("protocol").toLowerCase())) {
	    					_type = ShareManager.CIFS;
	    				} else if("ftp".equals(request.getParameter("protocol").toLowerCase())) {
	    					_type = ShareManager.FTP;
	    				}
	    				
		    			Map<String, String> _share = ShareManager.getShare(_type, request.getParameter("vg"), request.getParameter("lv"));
		    			
		    			switch(_type) {
		    				case ShareManager.NFS: {
			    					int _add_address = 1;
					    			if(request.getParameter("add-address") != null) {
				                    	try {
				                    		_add_address = Integer.parseInt(request.getParameter("add-address"));
				                    	} catch(NumberFormatException _ex) {
				                    		
				                    	}
					    			}
					    			
					    			List<String> _addresses = new ArrayList<String>();
					    			if(_share.containsKey("address")) {
					    				StringTokenizer _st = new StringTokenizer(_share.get("address"), ",");
					    				while(_st.hasMoreTokens()) {
					    					_addresses.add(_st.nextToken().trim());
					    				}
					    			}
					    			
					    			if(_add_address < _addresses.size()) {
					    				_add_address = _addresses.size();
					    			}
					    			
					    			writeDocumentBack("/admin/DeviceNAS");
				    				_xhtml_out.println("<form action=\"/admin/DeviceNAS\" name=\"share\" id=\"share\" method=\"post\">");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_SHARE + "\"/>");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"protocol\" value=\"nfs\"/>");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"vg\" value=\"" + _share.get("vg") + "\"/>");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"lv\" value=\"" + _share.get("lv") + "\"/>");
					    			_xhtml_out.println("<h1>");
				    				_xhtml_out.print("<img src=\"/images/share_32.png\"/>");
					    	    	_xhtml_out.print(getLanguageMessage("device.shares.edit_share"));
				    				_xhtml_out.println("</h1>");
					    			_xhtml_out.print("<div class=\"info\">");
					    			_xhtml_out.print(getLanguageMessage("device.shares.info_management"));
					    			_xhtml_out.println("</div>");
					    			
					    			_xhtml_out.println("<div class=\"window\">");
									_xhtml_out.println("<h2>");
									_xhtml_out.print(getLanguageMessage("device.shares.nfs.options"));
									_xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
									_xhtml_out.print(getLanguageMessage("common.message.save"));
									_xhtml_out.print("\" alt=\"");
									_xhtml_out.print(getLanguageMessage("common.message.save"));
									_xhtml_out.println("\"/></a>");
				                    _xhtml_out.println("</h2>");
				                    _xhtml_out.println("<fieldset>");
				                    _xhtml_out.println("<div class=\"standard_form\">");
				        	    	_xhtml_out.print("<label for=\"type\">");
				        	    	_xhtml_out.print(getLanguageMessage("device.shares.nfs.squash"));
				        	    	_xhtml_out.println(": </label>");
				        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"squash\" value=\"true\"");
				        	    	if(_share.get("squash") != null && "true".equals(_share.get("squash"))) {
				        	    		_xhtml_out.print(" checked=\"checked\"");
				        	    	}
				        	    	_xhtml_out.println("/>");
				        	    	_xhtml_out.println("</div>");
				        	    	_xhtml_out.println("<div class=\"standard_form\">");
				        	    	_xhtml_out.print("<label for=\"type\">");
				        	    	_xhtml_out.print(getLanguageMessage("device.shares.nfs.async"));
				        	    	_xhtml_out.println(": </label>");
				        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"async\" value=\"true\"");
				        	    	if(_share.get("async") != null && "true".equals(_share.get("async"))) {
				        	    		_xhtml_out.print(" checked=\"checked\"");
				        	    	}
				        	    	_xhtml_out.println("/>");
				        	    	_xhtml_out.println("</div>");
				        	    	_xhtml_out.println("</fieldset>");
				        	    	_xhtml_out.println("<div class=\"clear\"></div>");
				                    _xhtml_out.println("</div>");
				        	    	
				                    _xhtml_out.print("<div class=\"warn\">");
					    			_xhtml_out.print(getLanguageMessage("device.shares.exception.writings"));
					    			_xhtml_out.print("</div>");
				                    
				                    _xhtml_out.println("<div class=\"window\">");
									_xhtml_out.println("<h2>");
									_xhtml_out.print(getLanguageMessage("device.shares.nfs.address"));
									_xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
									_xhtml_out.print(getLanguageMessage("common.message.save"));
									_xhtml_out.print("\" alt=\"");
									_xhtml_out.print(getLanguageMessage("common.message.save"));
									_xhtml_out.println("\"/></a>");

									_xhtml_out.print("<a href=\"/admin/DeviceNAS?type=");
				                    _xhtml_out.print(EDIT_SHARE);
				                    _xhtml_out.print("&vg=");
				                    _xhtml_out.print(request.getParameter("vg"));
				                    _xhtml_out.print("&lv=");
				                    _xhtml_out.print(request.getParameter("lv"));
				                    _xhtml_out.print("&protocol=nfs");
				                    _xhtml_out.print("&add-address=");
				                    _xhtml_out.print(_add_address + 1);
				                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.add"));
				                    _xhtml_out.print("\" alt=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.add"));
				                    _xhtml_out.println("\"/></a>");

				                    _xhtml_out.println("</h2>");
				        	    	_xhtml_out.println("<table>");
				        	    	_xhtml_out.println("<tr>");
				        	    	_xhtml_out.print("<td class=\"title\">");
					                _xhtml_out.print(getLanguageMessage("common.network.address"));
					                _xhtml_out.println("</td>");
					                _xhtml_out.print("<td class=\"title\">");
					                _xhtml_out.print(getLanguageMessage("common.network.netmask"));
					                _xhtml_out.println("</td>");
					                _xhtml_out.println("</tr>");
				        	    	for(int r = 0; r < _add_address; r++) {
				        	    		String[] _address = null, _netmask = null;
				        	    		if(r < _addresses.size()) {
				        	    			if(_addresses.get(r).contains("/")) {
				        	    				_address = NetworkManager.toAddress(_addresses.get(r).split("/")[0]);
				        	    				_netmask = NetworkManager.toAddress(_addresses.get(r).split("/")[1]);
				        	    			} else {
				        	    				_address = NetworkManager.toAddress(_addresses.get(r));
				        	    			}
				        	    		}
				        	    		_xhtml_out.println("<tr>");
					                    _xhtml_out.println("<td>");
					                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip" + r + "1\"");
					                    if(_address != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_address[0]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.print(".");
					                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "2\"");
					                    if(_address != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_address[1]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.print(".");
					                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "3\"");
					                    if(_address != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_address[2]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.print(".");
					                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip" + r + "4\"");
					                    if(_address != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_address[3]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.println("</td>");
					                    _xhtml_out.print("<td>");
					                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "1\"");
					                    if(_netmask != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_netmask[0]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.print(".");
					                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "2\"");
					                    if(_netmask != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_netmask[1]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.print(".");
					                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "3\"");
					                    if(_netmask != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_netmask[2]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.print(".");
					                    _xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mask" + r + "4\"");
					                    if(_netmask != null) {
					                    	 _xhtml_out.print(" value=\"");
					                    	 _xhtml_out.print(_netmask[3]);
					                    	 _xhtml_out.print("\"");
					                    }
					                    _xhtml_out.print("/>");
					                    _xhtml_out.println("</td>");
					                    _xhtml_out.println("</tr>");
				        	    	}
				                    _xhtml_out.println("</table>");
				                    _xhtml_out.println("<div class=\"clear\"></div>");
				                    _xhtml_out.println("</div>");
				                    
				                    _xhtml_out.println("</form>");
			    				}
			    				break;
		    				case ShareManager.CIFS: {
			    					writeDocumentBack("/admin/DeviceNAS");
				    				_xhtml_out.println("<form action=\"/admin/DeviceNAS\" name=\"share\" id=\"share\" method=\"post\">");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_SHARE + "\"/>");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"protocol\" value=\"cifs\"/>");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"vg\" value=\"" + _share.get("vg") + "\"/>");
					    			_xhtml_out.println("<input type=\"hidden\" name=\"lv\" value=\"" + _share.get("lv") + "\"/>");
					    			_xhtml_out.println("<h1>");
				    				_xhtml_out.print("<img src=\"/images/share_32.png\"/>");
					    	    	_xhtml_out.print(getLanguageMessage("device.shares.edit_share"));
				    				_xhtml_out.println("</h1>");
					    			_xhtml_out.print("<div class=\"info\">");
					    			_xhtml_out.print(getLanguageMessage("device.shares.info_management"));
					    			_xhtml_out.println("</div>");
						    			
					    			_xhtml_out.println("<div class=\"window\">");
									_xhtml_out.println("<h2>");
									_xhtml_out.print(getLanguageMessage("device.shares.cifs.options"));
									_xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
				                    _xhtml_out.print("<div class=\"subinfo\">");
					    			_xhtml_out.print(getLanguageMessage("device.shares.info_trash_prev"));
					    			_xhtml_out.print("<strong>.recycle.</strong>");
					    			_xhtml_out.print(getLanguageMessage("device.shares.info_trash_post"));
					    			_xhtml_out.print("</div>");
				                    _xhtml_out.println("<fieldset>");
				                    _xhtml_out.println("<div class=\"standard_form\">");
				        	    	_xhtml_out.print("<label for=\"name\">");
				        	    	_xhtml_out.print(getLanguageMessage("device.shares.name"));
				        	    	_xhtml_out.println(": </label>");
				        	    	_xhtml_out.print("<input class=\"form_text\" name=\"name\" value=\"");
				        	    	_xhtml_out.print(_share.get("name"));
				        	    	_xhtml_out.println("\"/>");
				        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
				        	    	_xhtml_out.println("</div>");
				                    _xhtml_out.println("<div class=\"standard_form\">");
				        	    	_xhtml_out.print("<label for=\"type\">");
				        	    	_xhtml_out.print(getLanguageMessage("device.shares.cifs.recycle"));
				        	    	_xhtml_out.println(": </label>");
				        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"recycle\" value=\"true\"");
				        	    	if(_share.get("recycle") != null && "true".equals(_share.get("recycle"))) {
				        	    		_xhtml_out.print(" checked=\"checked\"");
				        	    	}
				        	    	_xhtml_out.println("/>");
				        	    	_xhtml_out.println("</div>");
				        	    	_xhtml_out.println("</fieldset>");
				        	    	_xhtml_out.println("<div class=\"clear\"></div>");
				                    _xhtml_out.println("</div>");
				                    
				                    _xhtml_out.println("</form>");
			    				}
			    				break;
		    			}
	    			}
	    			break;
	    		case REMOVE_SHARE: {
		    			int _type = ShareManager.NFS;
		    			if(request.getParameter("protocol") == null || request.getParameter("lv") == null || request.getParameter("vg") == null) {
	    					throw new Exception(getLanguageMessage("common.message.invalid_attributes"));
	    				}
	    				if(request.getParameter("confirm") != null) {
	    					if("cifs".equals(request.getParameter("protocol").toLowerCase())) {
		    					_type = ShareManager.CIFS;
		    				} else if("ftp".equals(request.getParameter("protocol").toLowerCase())) {
		    					_type = ShareManager.FTP;
		    				}
		    				ShareManager.removeShare(request.getParameter("vg"), request.getParameter("lv"), _type);
		    				writeDocumentResponse(getLanguageMessage("device.shares.removed_ok"), "/admin/DeviceNAS");
	    				} else {
	    					Map<String, String> _attributes = new HashMap<String, String>();
	    					_attributes.put("type", String.valueOf(REMOVE_SHARE));
	    					_attributes.put("protocol", request.getParameter("protocol"));
	    					_attributes.put("confirm", request.getParameter("true"));
	    					_attributes.put("lv", request.getParameter("lv"));
	    					_attributes.put("vg", request.getParameter("vg"));
	    					writeDocumentMapQuestion(getLanguageMessage("device.shares.question_removed"), _attributes, "/admin/DeviceNAS", null);
	    				}
	    			}
	    			break;
	    		case NEW_EXTERNAL_SHARE: {
		    			writeDocumentBack("/admin/DeviceNAS");
	    				_xhtml_out.println("<form action=\"/admin/DeviceNAS\" name=\"share\" id=\"share\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + NEW_EXTERNAL_SHARE_STORE + "\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/share_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.shares"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.shares.info_management"));
		    			_xhtml_out.println("</div>");
	    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.shares.new_external_share"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.share.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"server\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.server"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"server\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"path\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.path"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"path\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"type\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"protocol\" onChange=\"form_cifs_disabled(''+this.value)\">");
						_xhtml_out.println("<option value=\"cifs\">CIFS</option>");
						_xhtml_out.println("<option value=\"nfs\">NFS</option>");
						_xhtml_out.println("</select>");
						_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"user\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"domain\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.shares.domain"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"domain\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("</form>");
		    		}
	    			break;
	    		case NEW_EXTERNAL_SHARE_STORE: {
	    				int _type = ShareManager.NFS;
	    				if(request.getParameter("server") == null || request.getParameter("path") == null || request.getParameter("protocol") == null) {
	    					throw new Exception(getLanguageMessage("device.shares.exception.attributes"));
	    				}
	    				if("cifs".equals(request.getParameter("protocol").toLowerCase())) {
	    					_type = ShareManager.CIFS;
	    				}
	    				ShareManager.addExtrenalShare(request.getParameter("server"), request.getParameter("path"), request.getParameter("user"), request.getParameter("password"), request.getParameter("domain"), _type);
	    				writeDocumentResponse(getLanguageMessage("device.shares.created_ok"), "/admin/DeviceNAS");
	    			}
	    			break;
	    		case REMOVE_EXTERNAL_SHARE: {
	    				if(request.getParameter("server") == null || request.getParameter("share") == null) {
	    					throw new Exception(getLanguageMessage("device.shares.exception.attributes"));
	    				}
	    				ShareManager _sm = new ShareManager(this.sessionManager.getConfiguration());
	    				
	    				_sm.removeExternalShare(request.getParameter("server"), request.getParameter("share"), false);
	    				writeDocumentResponse(getLanguageMessage("device.shares.removed_ok"), "/admin/DeviceNAS");
	    			}
	    			break;
	    		case MOUNT_EXTERNAL_SHARE: {
		    			if(request.getParameter("server") == null || request.getParameter("share") == null) {
	    					throw new Exception(getLanguageMessage("device.shares.exception.attributes"));
	    				}
		    			ShareManager.mountExternalShare(request.getParameter("server"), request.getParameter("share"));
		    			response.sendRedirect("/admin/DeviceNAS");
		    			this.redirected=true;
	    			}
	    			break;
	    		case UMOUNT_EXTERNAL_SHARE: {
		    			if(request.getParameter("server") == null || request.getParameter("share") == null) {
	    					throw new Exception(getLanguageMessage("device.shares.exception.attributes"));
	    				}
		    			ShareManager.umountExternalShare(request.getParameter("server"), request.getParameter("share"));
		    			response.sendRedirect("/admin/DeviceNAS");
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