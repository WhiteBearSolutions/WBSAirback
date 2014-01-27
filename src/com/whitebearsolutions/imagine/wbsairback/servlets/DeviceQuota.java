package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.disk.QuotaManager;

public class DeviceQuota extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int NEW_USER_QUOTA = 2;
	public final static int NEW_GROUP_QUOTA = 3;
	public final static int EDIT_USER_QUOTA = 4;
	public final static int EDIT_GROUP_QUOTA = 5;
	public final static int REMOVE_USER_QUOTA = 6;
	public final static int REMOVE_GROUP_QUOTA = 7;
	public final static int STORE_USER_QUOTA = 8;
	public final static int STORE_GROUP_QUOTA = 9;
	private int type;
	public final static String baseUrl = "/admin/"+DeviceQuota.class.getSimpleName();
	
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

			switch(this.type) {
	    		default: {
	    				writeDocumentHeader();
	    				int _offset = 0, p = 0;
	    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception("device.logical_volumes.exception.lv_name");
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				if(request.getParameter("p") != null && !request.getParameter("p").isEmpty()) {
	    					try {
	    						p = Integer.parseInt(request.getParameter("p"));
	    						_offset = (p * 10);
	    					} catch(NumberFormatException _ex) {}
	    				}
	    				
	    				Map<String, Map<String, String>> _user_quotas = QuotaManager.searchVolumeUserQuotas(request.getParameter("match"), request.getParameter("group"), request.getParameter("name"));
	    				Map<String, Map<String, String>> _group_quotas = QuotaManager.searchVolumeGroupQuotas(request.getParameter("match"), request.getParameter("group"), request.getParameter("name"));
	    				
	    				writeDocumentBack("/admin/DeviceDisk");
	    				_xhtml_out.print("<form action=\"/admin/DeviceQuota\" name=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"group\" value=\"" + request.getParameter("group") + "\"/>");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.search"));
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"match\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.search"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"match\"/>");
	        	    	_xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\">");
	        	    	_xhtml_out.print("<img src=\"/images/find_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.find"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.find"));
	                	_xhtml_out.println("\"/></a>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.print("<br/>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
		    			
						_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota_user"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/DeviceQuota?type=");
	                    _xhtml_out.print(NEW_USER_QUOTA);
	                    _xhtml_out.print("&name=");
	                    _xhtml_out.print(request.getParameter("name"));
	                    _xhtml_out.print("&group=");
	                    _xhtml_out.print(request.getParameter("group"));
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_user_quotas.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("common.message.user"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.used"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.soft"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.hard"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    String[] _users = _user_quotas.keySet().toArray(new String[_user_quotas.size()]);
		                    for(int i = _offset; _offset < _users.length && _offset < (_offset + 10); i++) {
		                    	Map<String, String> _quota = _user_quotas.get(_users[i]);
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_users[i]);
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_quota.get("used"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_quota.get("soft"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_quota.get("hard"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceQuota?type=");
			                    _xhtml_out.print(EDIT_USER_QUOTA);
			                    _xhtml_out.print("&username=");
			                    _xhtml_out.print(_users[i]);
			                    _xhtml_out.print("&name=");
			                    _xhtml_out.print(request.getParameter("name"));
			                    _xhtml_out.print("&group=");
			                    _xhtml_out.print(request.getParameter("group"));
			                    _xhtml_out.print("\"><img src=\"/images/drive_user_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    _xhtml_out.println("\"/></a>");
			                    if(!"0".equals(_quota.get("hard").trim()) && !_quota.get("hard").isEmpty()) {
				                    _xhtml_out.print("<a href=\"/admin/DeviceQuota?type=");
				                    _xhtml_out.print(REMOVE_USER_QUOTA);
				                    _xhtml_out.print("&username=");
				                    _xhtml_out.print(_users[i]);
				                    _xhtml_out.print("&name=");
				                    _xhtml_out.print(request.getParameter("name"));
				                    _xhtml_out.print("&group=");
				                    _xhtml_out.print(request.getParameter("group"));
				                    _xhtml_out.print("\"><img src=\"/images/cross_16.png\" title=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.remove"));
				                    _xhtml_out.print("\" alt=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.remove"));
				                    _xhtml_out.println("\"/></a>");
			                    }
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_user_quotas"));
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
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota_group"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/DeviceQuota?type=");
	                    _xhtml_out.print(NEW_GROUP_QUOTA);
	                    _xhtml_out.print("&name=");
	                    _xhtml_out.print(request.getParameter("name"));
	                    _xhtml_out.print("&group=");
	                    _xhtml_out.print(request.getParameter("group"));
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_group_quotas.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("common.message.group"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.used"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.soft"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.hard"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(String _group : _group_quotas.keySet()) {
		                    	Map<String, String> _quota = _group_quotas.get(_group);
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_group);
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_quota.get("used"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_quota.get("soft"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_quota.get("hard"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceQuota?type=");
			                    _xhtml_out.print(EDIT_GROUP_QUOTA);
			                    _xhtml_out.print("&groupname=");
			                    _xhtml_out.print(_group);
			                    _xhtml_out.print("&name=");
			                    _xhtml_out.print(request.getParameter("name"));
			                    _xhtml_out.print("&group=");
			                    _xhtml_out.print(request.getParameter("group"));
			                    _xhtml_out.print("\"><img src=\"/images/drive_user_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    _xhtml_out.println("\"/></a>");
			                    if(!"0".equals(_quota.get("hard").trim()) && !_quota.get("hard").isEmpty()) {
				                    _xhtml_out.print("<a href=\"/admin/DeviceQuota?type=");
				                    _xhtml_out.print(REMOVE_GROUP_QUOTA);
				                    _xhtml_out.print("&groupname=");
				                    _xhtml_out.print(_group);
				                    _xhtml_out.print("&name=");
				                    _xhtml_out.print(request.getParameter("name"));
				                    _xhtml_out.print("&group=");
				                    _xhtml_out.print(request.getParameter("group"));
				                    _xhtml_out.print("\"><img src=\"/images/cross_16.png\" title=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.remove"));
				                    _xhtml_out.print("\" alt=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.remove"));
				                    _xhtml_out.println("\"/></a>");
			                    }
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_group_quotas"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	_xhtml_out.print("</form>");
					}
        		    break;
	    		case NEW_USER_QUOTA: {
	    				writeDocumentHeader();
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				
	    				writeDocumentBack("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
		    			_xhtml_out.print("<form action=\"/admin/DeviceQuota\" name=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_USER_QUOTA + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"group\" value=\"" + request.getParameter("group") + "\"/>");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota_user"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"username\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"username\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"size\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.hard"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"size\"/> MB");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.print("</form>");
	    			}
	    			break;
	    		case EDIT_USER_QUOTA: {
	    				writeDocumentHeader();
		    			if(request.getParameter("username") == null || request.getParameter("username").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.user_name"));
	    				}	
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				
	    				Map<String, String> _quota = QuotaManager.getVolumeUserQuota(request.getParameter("username"), request.getParameter("group"), request.getParameter("name"));
					
	    				writeDocumentBack("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
	    				_xhtml_out.print("<form action=\"/admin/DeviceQuota\" name=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_USER_QUOTA + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"group\" value=\"" + request.getParameter("group") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"username\" value=\"" + request.getParameter("username") + "\"/>");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota_user"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"_user\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" name=\"_user\" value=\"");
	        	    	_xhtml_out.print(request.getParameter("username"));
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"size\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.hard"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" name=\"size\" value=\"");
	        	    	_xhtml_out.print(_quota.get("mb_hard"));
	        	    	_xhtml_out.println("\"/> MB");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	_xhtml_out.print("</form>");
	    			}
	    			break;
	    		case STORE_USER_QUOTA: {
		    			double _size;
	    				if(request.getParameter("username") == null || request.getParameter("username").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.user_name"));
	    				}	
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				if(request.getParameter("size") == null || request.getParameter("size").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.quota"));
	    				}
	    				try {
	    					_size = Double.parseDouble(request.getParameter("size")) * 1048576D;
	    				} catch(NumberFormatException _ex) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.quota"));
	    				}
	    				
	    				QuotaManager.setVolumeUserQuota(request.getParameter("username"), request.getParameter("group"), request.getParameter("name"), _size);
	    				response.sendRedirect("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
	    				this.redirected=true;
	    			}
	    			break;
	    		case NEW_GROUP_QUOTA: {
	    				writeDocumentHeader();
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
					
	    				writeDocumentBack("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
	    				_xhtml_out.print("<form action=\"/admin/DeviceQuota\" name=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_GROUP_QUOTA + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"group\" value=\"" + request.getParameter("group") + "\"/>");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota_group"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"groupname\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.group"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"groupname\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"size\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.hard"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" name=\"size\"/> MB");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	_xhtml_out.print("</form>");
	    			}
	    			break;
	    		case EDIT_GROUP_QUOTA: {
	    				writeDocumentHeader();
		    			if(request.getParameter("groupname") == null || request.getParameter("groupname").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.user_name"));
	    				}	
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				
	    				Map<String, String> _quota = QuotaManager.getVolumeGroupQuota(request.getParameter("groupname"), request.getParameter("group"), request.getParameter("name"));
					
	    				writeDocumentBack("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
	    				_xhtml_out.print("<form action=\"/admin/DeviceQuota\" name=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_GROUP_QUOTA + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"group\" value=\"" + request.getParameter("group") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"groupname\" value=\"" + request.getParameter("groupname") + "\"/>");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota_group"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"_group\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.group"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" name=\"_group\" value=\"");
	        	    	_xhtml_out.print(request.getParameter("groupname"));
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"size\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.quota.hard"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" name=\"size\" value=\"");
	        	    	_xhtml_out.print(_quota.get("mb_hard"));
	        	    	_xhtml_out.println("\"/> MB");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	_xhtml_out.print("</form>");
	    			}
	    			break;
    			case STORE_GROUP_QUOTA: {
		    			double _size;
	    				if(request.getParameter("groupname") == null || request.getParameter("groupname").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.user_name"));
	    				}	
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				if(request.getParameter("size") == null || request.getParameter("size").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.quota"));
	    				}
	    				try {
	    					_size = Double.parseDouble(request.getParameter("size")) * 1048576D;
	    				} catch(NumberFormatException _ex) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.quota"));
	    				}
	    				
	    				QuotaManager.setVolumeGroupQuota(request.getParameter("groupname"), request.getParameter("group"), request.getParameter("name"), _size);
	    				response.sendRedirect("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
	    				this.redirected=true;
	    			}
	    			break;
    			case REMOVE_USER_QUOTA: {
	    				if(request.getParameter("username") == null || request.getParameter("username").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.user_name"));
	    				}	
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				QuotaManager.setVolumeUserQuota(request.getParameter("username"), request.getParameter("group"), request.getParameter("name"), 0);
	    				response.sendRedirect("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
	    				this.redirected=true;
	    			}
	    			break;
    			case REMOVE_GROUP_QUOTA: {
	    				if(request.getParameter("groupname") == null || request.getParameter("groupname").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.user_name"));
	    				}	
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.exception.vg_name"));
	    				}
	    				QuotaManager.setVolumeGroupQuota(request.getParameter("groupname"), request.getParameter("group"), request.getParameter("name"), 0);
	    				response.sendRedirect("/admin/DeviceQuota?name=" + request.getParameter("name") + "&group=" + request.getParameter("group"));
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
