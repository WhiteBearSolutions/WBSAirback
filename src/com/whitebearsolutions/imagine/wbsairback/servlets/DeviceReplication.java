package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.net.ReplicationManager;
import com.whitebearsolutions.util.Configuration;


public class DeviceReplication extends WBSImagineServlet {
	static final long serialVersionUID = 2781204781L;
	public final static int NEW_SOURCE = 2;
	public final static int NEW_DESTINATION = 3;
	public final static int STORE_SOURCE = 4;
	public final static int STORE_DESTINATION = 5;
	public final static int REMOVE_SOURCE = 6;
	public final static int REMOVE_DESTINATION = 7;
	public final static int STORE_INTERVAL = 8;
	public final static int LOG_COMMAND = 9;
	public final static int RELAUNCH = 10;
	public final static int ERASEDB = 11;
	public final static int ERASE_COMMAND = 12;
	private int type;
	public final static String baseUrl = "/admin/"+DeviceReplication.class.getSimpleName();
	
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
	    	if (type != STORE_DESTINATION && type != STORE_SOURCE && type != REMOVE_DESTINATION && type != REMOVE_SOURCE && type != LOG_COMMAND)
	    		writeDocumentHeader();
			
	    	Configuration _c = this.sessionManager.getConfiguration();
	    	ReplicationManager _rm = new ReplicationManager();
	    	
		    switch(this.type) {
	    		default: {
						_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />");
						if("es".equals(this.messagei18N.getLocale().getLanguage()))
							_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>");
						else
							_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>");
					    _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>");
				    
	    				int _offset = 0;
	    				List<Map<String, String>> _destinations = _rm.getDestinations();
	    				List<Map<String, String>> _sources = _rm.getSources();
	    				Map<String, List<Map<String, String>>> list = _rm.getCurrentSyncronizations();
	    				
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/copy_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.destination"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("<a href=\"/admin/DeviceReplication?type=");
	                    _xhtml_out.print(NEW_DESTINATION);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset style=\"margin-left:30px;\">");
	                    _xhtml_out.println("<table>");
	                    if(!_destinations.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("common.network.address"));
		                    _xhtml_out.println("</td>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.destination.vg"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.destination.lv"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.filesystem"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _destination : _destinations) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_destination.get("address"));
			                    _xhtml_out.println("</td>");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_destination.get("vg"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_destination.get("lv"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(getLanguageMessage("common.message."+_destination.get("filesystem")));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceReplication?type=");
			                    _xhtml_out.print(NEW_DESTINATION);
			                    _xhtml_out.print("&vg=");
			                    _xhtml_out.print(_destination.get("vg"));
			                    _xhtml_out.print("&lv=");
			                    _xhtml_out.print(_destination.get("lv"));
			                    _xhtml_out.print("&address=");
			                    _xhtml_out.print(_destination.get("address"));
			                    _xhtml_out.print("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\"><img src=\"/images/replication_edit_16.png\"/></a> ");
			                    _xhtml_out.print("<a href=\"/admin/DeviceReplication?type=");
			                    _xhtml_out.print(REMOVE_DESTINATION);
			                    _xhtml_out.print("&vg=");
			                    _xhtml_out.print(_destination.get("vg"));
			                    _xhtml_out.print("&lv=");
			                    _xhtml_out.print(_destination.get("lv"));
			                    _xhtml_out.print("&address=");
			                    _xhtml_out.print(_destination.get("address"));
			                    _xhtml_out.print("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/copy_delete_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_replication_destinations"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_offset = 0;
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.source"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("<a href=\"/admin/DeviceReplication?type=");
	                    _xhtml_out.print(NEW_SOURCE);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset style=\"margin-left:30px;\">");
	                    _xhtml_out.println("<table>");
	                    if(!_sources.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.running.source"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.running.destination"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.source.delete"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.filesystem"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.checksum"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.append"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.compress"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print("D.Delta");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _source : _sources) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print(_source.get("local-address"));
			                    _xhtml_out.print(":: ");
			                    _xhtml_out.print(_source.get("vg"));
			                    _xhtml_out.print("/");
			                    _xhtml_out.print(_source.get("lv"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print(_source.get("address"));
			                    _xhtml_out.print(":: ");
			                    _xhtml_out.print(_source.get("dest-vg"));
			                    _xhtml_out.print("/");
			                    _xhtml_out.print(_source.get("dest-lv"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(getLanguageMessage("common.message."+_source.get("delete")));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(getLanguageMessage("common.message."+_source.get("filesystem")));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(getLanguageMessage("common.message."+_source.get("checksum")));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(getLanguageMessage("common.message."+_source.get("append")));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(getLanguageMessage("common.message."+_source.get("compress")));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(getLanguageMessage("common.message."+_source.get("delta")));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceReplication?type=");
			                    _xhtml_out.print(NEW_SOURCE);
			                    _xhtml_out.print("&vg=");
			                    _xhtml_out.print(_source.get("vg"));
			                    _xhtml_out.print("&lv=");
			                    _xhtml_out.print(_source.get("lv"));
			                    _xhtml_out.print("&address=");
			                    _xhtml_out.print(_source.get("address"));
			                    _xhtml_out.print("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\"><img src=\"/images/replication_edit_16.png\"/></a>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceReplication?type=");
			                    _xhtml_out.print(REMOVE_SOURCE);
			                    _xhtml_out.print("&vg=");
			                    _xhtml_out.print(_source.get("vg"));
			                    _xhtml_out.print("&lv=");
			                    _xhtml_out.print(_source.get("lv"));
			                    _xhtml_out.print("&address=");
			                    _xhtml_out.print(_source.get("address"));
			                    _xhtml_out.print("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/copy_delete_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_replication_sources"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.print("<form action=\"/admin/DeviceReplication\" name=\"config\" method=\"post\">");
	        	    	_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_INTERVAL + "\"/>");
	        	    	_xhtml_out.println("<div class=\"window\">");
		                _xhtml_out.println("<h2>");
		                _xhtml_out.println(getLanguageMessage("device.logical_volumes.replica.config.interval"));
		                _xhtml_out.print("<a href=\"javascript:submitForm(document.config.submit());\"><img src=\"/images/disk_16.png\" title=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.save"));
						_xhtml_out.print("\" alt=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
						_xhtml_out.println("\"/></a>");
		                _xhtml_out.println("</h2>");
		                _xhtml_out.println("<fieldset style=\"margin-left:30px;\">");
		                _xhtml_out.println("<div class=\"standard_form\">");
		        	    _xhtml_out.print("<label for=\"rsync_launching_interval\">");
		        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.rsync_launching_interval"));
		        	    _xhtml_out.println(": </label>");
		        	    _xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"rsync_launching_interval\" id=\"rsync_launching_interval\" value=\"");
		        	    int launchingInterval = ReplicationManager.DEFAULT_LAUNCHING_INTERVAL;
		        	    if (_c.hasProperty("rsync_launching_interval"))
		        	    	launchingInterval = Integer.parseInt(_c.getProperty("rsync_launching_interval"));
		        	    launchingInterval = launchingInterval/1000/60;
		        	    _xhtml_out.print(launchingInterval);
		        	   	_xhtml_out.print("\" />");
		        	   	_xhtml_out.println(getLanguageMessage("device.logical_volumes.replica.minutes"));
		                _xhtml_out.println("</div>");
		                _xhtml_out.println("<div class=\"standard_form\">");
		        	    _xhtml_out.print("<label for=\"rsync_interval\">");
		        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.interval"));
		        	    _xhtml_out.println(": </label>");
		        	    _xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"rsync_interval\" id=\"rsync_interval\" value=\"");
		        	    int interval = ReplicationManager.DEFAULT_MAX_EXECUTION_TIME;
		        	    if (_c.hasProperty("rsync_interval"))
		        	    	interval = Integer.parseInt(_c.getProperty("rsync_interval"));
		        	    interval = interval/1000/60;
		        	    _xhtml_out.print(interval);
		        	   	_xhtml_out.print("\" />");
		        	   	_xhtml_out.println(getLanguageMessage("device.logical_volumes.replica.minutes"));
		                _xhtml_out.println("</div>");
		                _xhtml_out.println("<div class=\"standard_form\">");
		        	    _xhtml_out.print("<label for=\"command_history_retention\">");
		        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.command_history_retention"));
		        	    _xhtml_out.println(": </label>");
		        	    _xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"command_history_retention\" id=\"command_history_retention\" value=\"");
		        	    int retention = ReplicationManager.DEFAULT_RETENTION;
		        	    if (_c.hasProperty("command_history_retention"))
		        	    	retention = Integer.parseInt(_c.getProperty("command_history_retention"));
		        	    retention = retention/1000/60/60;
		        	    _xhtml_out.print(retention);
		        	   	_xhtml_out.print("\" />");
		        	   	_xhtml_out.println(getLanguageMessage("device.logical_volumes.replica.hours"));
		                _xhtml_out.println("</div>");
		                _xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"></div>");
		                _xhtml_out.println("</div>");
		                _xhtml_out.print("</form>");
		                
	        	    	_xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
	                    _xhtml_out.println("<div id=\"listadoRunning\" style=\"clear:both;width:100%;margin:auto;\"><table id=\"tablaRunning\" style=\"margin-left:0px;margin-right:0px;\"></table><div id='pager' ></div></div>");
		        	    _xhtml_out.print("</div>");

		        	    _xhtml_out.println("<div id=\"viewLogDialog\" name=\"viewLogDialog\" title=\""+getLanguageMessage("device.logical_volumes.replica.log")+"\" style=\"font-size:12px;\"></div>");
	        	    	_xhtml_out.print("<input type=\"hidden\" name=\"idLog\" id=\"idLog\" value=\"\">");
	        	    	
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
	                	pageJS+="					viewLog($('#idLog').val());\n";
	                	pageJS+="				}\n";
	                	pageJS+="	}\n";
	                 	pageJS+="});\n";
	                 	
		        	    pageJSFuncs+=getJSViewJobLog();
		        	    pageJSFuncs+=allLoad("tablaRunning", list);
	                 	pageJSFuncs+=emptyGridFuncJS("tablaRunning");
		        	    pageJS+=getJqGridJS("tablaRunning", "listadoRunning");
	                 	pageJS+="reloadAll();\n";	
	    			}
	    			break;
	    		case NEW_DESTINATION: {
	    				Map<String, String> destination = null;
	    				if(request.getParameter("lv") != null && !request.getParameter("lv").isEmpty() ||
		    					request.getParameter("vg") != null && !request.getParameter("vg").isEmpty() ||
		    					request.getParameter("address") != null && !request.getParameter("address").isEmpty()) {
	    					destination = _rm.getDestination(request.getParameter("vg"), request.getParameter("lv"), request.getParameter("address")); 
	    				}
	    					
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
	    				
	    				_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    				writeDocumentBack("/admin/DeviceReplication");
	    				_xhtml_out.print("<form action=\"/admin/DeviceReplication\" name=\"device\" id=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_DESTINATION + "\"/>");
    					if (destination != null) {
		    				_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"true\"/>");
		    				_xhtml_out.print("<input type=\"hidden\" name=\"editvg\" value=\""+destination.get("vg")+"\"/>");
		    				_xhtml_out.print("<input type=\"hidden\" name=\"editlv\" value=\""+destination.get("lv")+"\"/>");
		    				_xhtml_out.print("<input type=\"hidden\" name=\"editip\" value=\""+destination.get("address")+"\"/>");
	    				}
	    				
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/copy_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.destination"));
	                    _xhtml_out.print("<a href=\"javascript:if ($('#device').validationEngine('validate')) submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"filesystem\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.filesystem"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"filesystem\" id=\"filesystem\" onClick=\"changeFilesystem();\" value=\"true\" ");
	        	    	if (destination != null && destination.get("filesystem") != null && destination.get("filesystem").equals("yes"))
	        	    		_xhtml_out.print(" checked=\"checked\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	                    _xhtml_out.print("<label for=\"lv\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.destination.lv"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"lv\">");
	        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(Map<String, String> _lv : _lvs) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.print("\" ");
							if (destination != null && destination.get("vg") != null && destination.get("lv") != null) {
								if (destination.get("vg").equals(_lv.get("vg")) && destination.get("lv").equals(_lv.get("name")))
									_xhtml_out.print(" selected =\"selected\" ");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ip1\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.address"));
	        	    	_xhtml_out.println(": </label>");
	        	    	String [] address = null;
	        	    	if (destination != null && destination.get("address") != null && !destination.get("address").isEmpty())
	        	    		address = NetworkManager.toAddress(destination.get("address"));
	        	    	_xhtml_out.print("<input class=\"validate[groupRequired[ip],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip1\" id=\"ip1\"");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[0]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[ip1],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip2\" id=\"ip2\"");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[1]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[ip2],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip3\" id=\"ip3\"");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[2]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[ip3],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip4\" id=\"ip4\"");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[3]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"password\" name=\"password\" ");
	        	    	if (destination != null && destination.get("password") != null && !destination.get("password").isEmpty())
	        	    		_xhtml_out.print(" value=\""+destination.get("password")+"\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	    				_xhtml_out.print("</form>");
	    				
	    				_xhtml_out.println("<script type=\"text/javascript\">");
	                    _xhtml_out.println(" function changeFilesystem() {");
	                    _xhtml_out.println(" 	var filesystem = document.getElementById('filesystem');");
	                    _xhtml_out.println(" 	if (filesystem.checked) {");
	                    _xhtml_out.println(" 		$('.notfilesystem').css('display','none');\n");
	                    _xhtml_out.println(" 		$('.notfilesystem').attr('disabled', 'disabled');\n");
	                    _xhtml_out.println("    } else {");
	                    _xhtml_out.println(" 		$('.notfilesystem').css('display','');\n");
	                    _xhtml_out.println(" 		$('.notfilesystem').removeAttr('disabled');\n");
	                    _xhtml_out.println("    }");
	                    _xhtml_out.println(" }");
	                    _xhtml_out.println("</script>");
	    				_xhtml_out.print("</form>");
	    				pageJS+="changeFilesystem();\n";
	    			}
	    			break;
	    		case NEW_SOURCE: {
		    			Map<String, String> source = null;
	    				if(request.getParameter("lv") != null && !request.getParameter("lv").isEmpty() ||
		    					request.getParameter("vg") != null && !request.getParameter("vg").isEmpty() ||
		    					request.getParameter("address") != null && !request.getParameter("address").isEmpty()) {
	    					source = _rm.getSource(request.getParameter("vg"), request.getParameter("lv"), request.getParameter("address")); 
	    				}
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
	    				
	    				_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    				writeDocumentBack("/admin/DeviceReplication");
	    				_xhtml_out.print("<form action=\"/admin/DeviceReplication\" name=\"device\" id=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_SOURCE + "\"/>");
	    				if (source != null) {
	    					_xhtml_out.print("<input type=\"hidden\" name=\"edit\" value=\"true\"/>");
	    					_xhtml_out.print("<input type=\"hidden\" name=\"editvg\" value=\""+source.get("vg")+"\"/>");
	    					_xhtml_out.print("<input type=\"hidden\" name=\"editlv\" value=\""+source.get("lv")+"\"/>");
	    					_xhtml_out.print("<input type=\"hidden\" name=\"editip\" value=\""+source.get("address")+"\"/>");
	    				}
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/copy_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.source"));
	                    _xhtml_out.print("<a href=\"javascript:if ($('#device').validationEngine('validate')) submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"filesystem\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.filesystem"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"filesystem\" id=\"filesystem\" onClick=\"changeFilesystem()\" value=\"true\" ");
	        	    	if (source != null && source.get("filesystem") != null && source.get("filesystem").equals("yes"))
	        	    		_xhtml_out.print(" checked=\"checked\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	                    _xhtml_out.print("<label for=\"lv\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.source.lv"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"lv\">");
	        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(Map<String, String> _lv : _lvs) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.print("\" ");
							if (source != null && source.get("vg") != null && source.get("lv") != null) {
								if (source.get("vg").equals(_lv.get("vg")) && source.get("lv").equals(_lv.get("name")))
									_xhtml_out.print(" selected =\"selected\" ");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	String [] address = null;
	        	    	if (source != null && source.get("address") != null && !source.get("address").isEmpty())
	        	    		address = NetworkManager.toAddress(source.get("address"));
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"ip1\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.address.destination"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"validate[groupRequired[ip],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip1\" id=\"ip1\" maxlength=\"3\" ");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[0]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[ip1],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip2\" id=\"ip2\" maxlength=\"3\" ");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[1]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[ip2],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip3\" id=\"ip3\" maxlength=\"3\" ");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[2]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[ip3],custom[integer],max[255]] network_octet\" type=\"text\" name=\"ip4\" id=\"ip4\" maxlength=\"3\" ");
	        	    	if (address != null)
	        	    		_xhtml_out.print(" value=\""+address[3]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.println(" <img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	String [] addressLocal = null;
	        	    	if (source != null && source.get("local-address") != null && !source.get("local-address").isEmpty())
	        	    		addressLocal = NetworkManager.toAddress(source.get("local-address"));
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"iploc1\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.address.local"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"validate[groupRequired[iploc],custom[integer],max[255]] network_octet\" type=\"text\" name=\"iploc1\" id=\"iploc1\" maxlength=\"3\" ");
	        	    	if (addressLocal != null)
	        	    		_xhtml_out.print(" value=\""+addressLocal[0]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[iploc1],custom[integer],max[255]] network_octet\" type=\"text\" name=\"iploc2\" id=\"iploc2\" maxlength=\"3\" ");
	        	    	if (addressLocal != null)
	        	    		_xhtml_out.print(" value=\""+addressLocal[1]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[iploc2],custom[integer],max[255]] network_octet\" type=\"text\" name=\"iploc3\" id=\"iploc3\" maxlength=\"3\" ");
	        	    	if (addressLocal != null)
	        	    		_xhtml_out.print(" value=\""+addressLocal[2]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"validate[condRequired[iploc3],custom[integer],max[255]] network_octet\" type=\"text\" name=\"iploc4\" id=\"iploc4\" maxlength=\"3\" ");
	        	    	if (addressLocal != null)
	        	    		_xhtml_out.print(" value=\""+addressLocal[3]+"\"");	
	        	    	_xhtml_out.print("/>");
	                    _xhtml_out.println(" <img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	                    _xhtml_out.print("<label for=\"dst_vg\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.destination.vg"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" name=\"dst_vg\" ");
	        	    	if (source != null && source.get("dest-vg") != null && !source.get("dest-vg").isEmpty())
	        	    		_xhtml_out.print(" value=\""+source.get("dest-vg")+"\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	                    _xhtml_out.print("<label for=\"dst_lv\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.destination.lv"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" name=\"dst_lv\" ");
	        	    	if (source != null && source.get("dest-lv") != null && !source.get("dest-lv").isEmpty())
	        	    		_xhtml_out.print(" value=\""+source.get("dest-lv")+"\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"password\" name=\"password\" ");
	        	    	if (source != null && source.get("password") != null && !source.get("password").isEmpty())
	        	    		_xhtml_out.print(" value=\""+source.get("password")+"\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form notfilesystem\">");
	                    _xhtml_out.print("<label for=\"mbs\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.source.mbs"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"mbs\" ");
	        	    	if (source != null && source.get("mbs") != null && !source.get("mbs").isEmpty())
	        	    		_xhtml_out.print(" value=\""+source.get("mbs")+"\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form notfilesystem\">");
	        	    	_xhtml_out.print("<label for=\"delete\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.source.delete"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"delete\" value=\"yes\" ");
	        	    	if (source != null && source.get("delete") != null && source.get("delete").equals("yes"))
	        	    		_xhtml_out.print(" checked=\"checked\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form notfilesystem\">");
	        	    	_xhtml_out.print("<label for=\"checksum\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.checksum"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox notfilesystem\" type=\"checkbox\" name=\"checksum\" value=\"true\" ");
	        	    	if (source != null && source.get("checksum") != null && source.get("checksum").equals("yes"))
	        	    		_xhtml_out.print(" checked=\"checked\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form notfilesystem\">");
	        	    	_xhtml_out.print("<label for=\"append\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.append"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"append\" id=\"append\" onClick=\"changeDelta();\" value=\"true\" ");
	        	    	if (source != null && source.get("append") != null && source.get("append").equals("yes"))
	        	    		_xhtml_out.print(" checked=\"checked\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form notfilesystem\">");
	        	    	_xhtml_out.print("<label for=\"compress\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.compress"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"compress\" value=\"true\" ");
	        	    	if (source != null && source.get("compress") != null && source.get("compress").equals("yes"))
	        	    		_xhtml_out.print(" checked=\"checked\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form notfilesystem\">");
	        	    	_xhtml_out.print("<label for=\"delta\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.delta"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"delta\" id=\"delta\" onClick=\"changeAppend();\" value=\"true\" ");
	        	    	if (source != null && source.get("delta") != null && source.get("delta").equals("yes"))
	        	    		_xhtml_out.print(" checked=\"checked\"");	
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	        	    	_xhtml_out.println("<div class=\"subinfo notfilesystem\">"+getLanguageMessage("device.logical_volumes.replica.deltaAndAppend")+"</div>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<script type=\"text/javascript\">");
	                    _xhtml_out.println(" function changeDelta() {");
	                    _xhtml_out.println(" 	var append = document.getElementById('append');");
	                    _xhtml_out.println(" 	var delta = document.getElementById('delta');");
	                    _xhtml_out.println(" 	if (append.checked) delta.checked=false;");
	                    _xhtml_out.println(" }");
	                    _xhtml_out.println(" function changeAppend() {");
	                    _xhtml_out.println(" 	var append = document.getElementById('append');");
	                    _xhtml_out.println(" 	var delta = document.getElementById('delta');");
	                    _xhtml_out.println(" 	if (delta.checked) append.checked=false;");
	                    _xhtml_out.println(" }");
	                    _xhtml_out.println(" function changeFilesystem() {");
	                    _xhtml_out.println(" 	var filesystem = document.getElementById('filesystem');");
	                    _xhtml_out.println(" 	if (filesystem.checked) {");
	                    _xhtml_out.println(" 		$('.notfilesystem').css('display','none');\n");
	                    _xhtml_out.println(" 		$('.notfilesystem').attr('disabled', 'disabled');\n");
	                    _xhtml_out.println("    } else {");
	                    _xhtml_out.println(" 		$('.notfilesystem').css('display','');\n");
	                    _xhtml_out.println(" 		$('.notfilesystem').removeAttr('disabled');\n");
	                    _xhtml_out.println("    }");
	                    _xhtml_out.println(" }");
	                    _xhtml_out.println("</script>");
	    				_xhtml_out.print("</form>");
	    				pageJS+="changeFilesystem();\n";
	    			}
	    			break;
	    		case STORE_DESTINATION: {
		    			if(request.getParameter("lv") == null || request.getParameter("lv").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv"));
		    			}
		    			
		    			boolean _filesystem = false;
		    			String[] _address = new String[4];
		    			String[] _volume = request.getParameter("lv").split("/");
		    			if(_volume.length != 2) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv"));
		    			}
		    			if(request.getParameter("ip1") == null ||
	                    		request.getParameter("ip2") == null || 
	                    		request.getParameter("ip3") == null ||
	                    		request.getParameter("ip4") == null ||
	                    		request.getParameter("ip1").isEmpty() ||
	                    		request.getParameter("ip2").isEmpty() ||
	                    		request.getParameter("ip3").isEmpty() ||
	                    		request.getParameter("ip4").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.network_address"));
	                    }
		    			_address[0] = request.getParameter("ip1");
		    			_address[1] = request.getParameter("ip2");
		    			_address[2] = request.getParameter("ip3");
		    			_address[3] = request.getParameter("ip4");
		    			
		    			if(request.getParameter("filesystem") != null) {
	    					_filesystem = true;
	    				}
		    			
		    			_rm.setDestination(_volume[0], _volume[1], _address, request.getParameter("password"), _filesystem);
		    			if(request.getParameter("edit") != null && "true".equalsIgnoreCase(request.getParameter("edit"))) {
		    				String ip = request.getParameter("editip");
		    				String vg = request.getParameter("editvg");
	    					String lv = request.getParameter("editlv");
	    					if (!ip.equals(NetworkManager.addressToString(_address)) || !_volume[0].equals(vg) || !_volume[1].equals(lv))
	    						_rm.removeDestination(vg, lv, ip);
	    				}
		    			response.sendRedirect("/admin/DeviceReplication");
		    			this.redirected=true;
	    			}
	    			break;
	    		case STORE_SOURCE: {
		    			int _mbs = 0;
	    				boolean _delete = false, _filesystem = false, _checksum = false, _compress = false, _append = false, _delta = false;
		    			if(request.getParameter("lv") == null || request.getParameter("lv").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv"));
		    			}
		    			
		    			String[] _address = new String[4];
		    			String[] _addressLocal = new String[4];
		    			String[] _volume = request.getParameter("lv").split("/");
		    			if(_volume.length != 2) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv"));
		    			}
		    			if(request.getParameter("ip1") == null ||
	                    		request.getParameter("ip2") == null || 
	                    		request.getParameter("ip3") == null ||
	                    		request.getParameter("ip4") == null ||
	                    		request.getParameter("ip1").isEmpty() ||
	                    		request.getParameter("ip2").isEmpty() ||
	                    		request.getParameter("ip3").isEmpty() ||
	                    		request.getParameter("ip4").isEmpty() ||
	                    		request.getParameter("iploc1") == null ||
	                    		request.getParameter("iploc2") == null || 
	                    		request.getParameter("iploc3") == null ||
	                    		request.getParameter("iploc4") == null ||
	                    		request.getParameter("iploc1").isEmpty() ||
	                    		request.getParameter("iploc2").isEmpty() ||
	                    		request.getParameter("iploc3").isEmpty() ||
	                    		request.getParameter("iploc4").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.network_address"));
	                    }
		    			if(request.getParameter("delete") != null &&
		    					"yes".equalsIgnoreCase(request.getParameter("delete"))) {
		    				_delete = true;
		    			}
		    			if(request.getParameter("filesystem") != null && "true".equalsIgnoreCase(request.getParameter("filesystem"))) {
	    					_filesystem = true;
	    				}
	    				if(request.getParameter("checksum") != null && "true".equalsIgnoreCase(request.getParameter("checksum"))) {
	    					_checksum = true;
	    				}
	    				if(request.getParameter("append") != null && "true".equalsIgnoreCase(request.getParameter("append"))) {
	    					_append = true;
	    				}
	    				if(request.getParameter("compress") != null && "true".equalsIgnoreCase(request.getParameter("compress"))) {
	    					_compress = true;
	    				}
	    				if(request.getParameter("delta") != null && "true".equalsIgnoreCase(request.getParameter("delta"))) {
	    					_delta = true;
	    				}
		    			if(request.getParameter("mbs") != null && !request.getParameter("mbs").isEmpty()) {
			    			try {
			    				_mbs = Integer.parseInt(request.getParameter("mbs"));
			    			} catch(NumberFormatException _ex) {
			    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.transfer_limit"));
			    			}
		    			}
		    			
		    			_address[0] = request.getParameter("ip1");
		    			_address[1] = request.getParameter("ip2");
		    			_address[2] = request.getParameter("ip3");
		    			_address[3] = request.getParameter("ip4");
		    			
		    			_addressLocal[0] = request.getParameter("iploc1");
		    			_addressLocal[1] = request.getParameter("iploc2");
		    			_addressLocal[2] = request.getParameter("iploc3");
		    			_addressLocal[3] = request.getParameter("iploc4");
		    			
	    				_rm.setSource(_volume[0], _volume[1], _address, _addressLocal, request.getParameter("dst_vg"), request.getParameter("dst_lv"), request.getParameter("password"), _mbs, _delete, _filesystem, _checksum, _compress, _append, _delta);
		    			if(request.getParameter("edit") != null && "true".equalsIgnoreCase(request.getParameter("edit"))) {
		    				String ip = request.getParameter("editip");
		    				String vg = request.getParameter("editvg");
	    					String lv = request.getParameter("editlv");
	    					if (!ip.equals(NetworkManager.addressToString(_address)) || !_volume[0].equals(vg) || !_volume[1].equals(lv))
	    						_rm.removeSource(vg, lv, ip);
	    				}
	    				_rm.runAllSourceReplication(true);
	    				response.sendRedirect("/admin/DeviceReplication");
	    				this.redirected=true;
	    			}
	    			break;
	    		case REMOVE_DESTINATION: {
		    			if(request.getParameter("lv") == null || request.getParameter("lv").isEmpty() ||
		    					request.getParameter("vg") == null || request.getParameter("vg").isEmpty() ||
		    					request.getParameter("address") == null || request.getParameter("address").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv"));
		    			}
		    			if(request.getParameter("confirm") != null) {
		    				_rm.removeDestination(request.getParameter("vg"), request.getParameter("lv"), request.getParameter("address"));
		    				response.sendRedirect("/admin/DeviceReplication");
		    				this.redirected=true;
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("device.logical_volumes.quota.question_destination"), "/admin/DeviceReplication?type=" + REMOVE_DESTINATION + "&vg=" + request.getParameter("vg") + "&lv=" + request.getParameter("lv") + "&address=" + request.getParameter("address")+ "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case REMOVE_SOURCE: {
		    			if(request.getParameter("lv") == null || request.getParameter("lv").isEmpty() ||
		    					request.getParameter("vg") == null || request.getParameter("vg").isEmpty() ||
		    					request.getParameter("address") == null || request.getParameter("address").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.exception.lv"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				_rm.removeSource(request.getParameter("vg"), request.getParameter("lv"), request.getParameter("address"));
		    				response.sendRedirect("/admin/DeviceReplication");
		    				this.redirected=true;
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("device.logical_volumes.quota.question_source"), "/admin/DeviceReplication?type=" + REMOVE_SOURCE + "&vg=" + request.getParameter("vg") + "&lv=" + request.getParameter("lv") + "&address=" + request.getParameter("address")+ "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case STORE_INTERVAL: {
	    				if(request.getParameter("rsync_interval") == null || request.getParameter("rsync_interval").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.replica.exception.rsync_interval"));
	    				}
	    				if(request.getParameter("rsync_launching_interval") == null || request.getParameter("rsync_launching_interval").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.replica.exception.rsync_launching_interval"));
	    				}
	    				if(request.getParameter("command_history_retention") == null || request.getParameter("command_history_retention").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.logical_volumes.replica.exception.command_history_retention"));
	    				}
	    			
	    				Long retention = Long.parseLong(request.getParameter("command_history_retention"));
	    				Long interval = Long.parseLong(request.getParameter("rsync_interval"));
	    				Long launchingInterval = Long.parseLong(request.getParameter("rsync_launching_interval"));
	    				
	    				retention = retention*60*60*1000;
	    				interval = interval*60*1000;
	    				launchingInterval = launchingInterval*60*1000;
	    				
	    				if (retention < interval)
	    					throw new Exception(getLanguageMessage("device.logical_volumes.replica.exception.retention.smaller.interval"));
	    				_c.setProperty("command_history_retention", String.valueOf(retention));
	    				_c.setProperty("rsync_interval", String.valueOf(interval));
	    				_c.setProperty("rsync_launching_interval", String.valueOf(launchingInterval));
	    				_c.store();
	    				writeDocumentResponse(getLanguageMessage("device.logical_volumes.replica.interval.saved"), "/admin/DeviceReplication");
	    			} break;
	    		case LOG_COMMAND: {
	    			if(request.getParameter("id") == null || request.getParameter("id").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.logical_volumes.replica.exception.processid"));
    				}
	    			
	    			_xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/copy_32.png\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica"));
    				_xhtml_out.println("</h1>");
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.log"));
                    _xhtml_out.println("</h2>");
                    _xhtml_out.println(_rm.getRsyncLog(request.getParameter("id")));
                    _xhtml_out.println("<div class=\"clear\"/></div>");
        	    	_xhtml_out.print("</div>");
	    		} break;
	    		case RELAUNCH: {
    				_rm.runAllSourceReplication(true);
    				writeDocumentResponse(getLanguageMessage("device.logical_volumes.replica.relaunched"), "/admin/DeviceReplication");
	    		} break;
	    		case ERASEDB: {
    				_rm.eraseCommandDB();
    				writeDocumentResponse(getLanguageMessage("device.logical_volumes.replica.db.erased"), "/admin/DeviceReplication");
	    		} break;
	    		case ERASE_COMMAND: {
	    			if(request.getParameter("id") == null || request.getParameter("id").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.logical_volumes.replica.exception.processid"));
    				}
	    			_rm.eraseCommandById(request.getParameter("id"));
	    			writeDocumentResponse(getLanguageMessage("device.logical_volumes.replica.db.command_erased"), "/admin/DeviceReplication");
	    		} break;
		    }
	    } catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	if (type != LOG_COMMAND)
	    		writeDocumentFooter();
	    }
	}
	
	private String getJqGridJS(String tableId, String divId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid({\n");
 		sb.append("		datatype: \"local\",\n");
 		sb.append("		colNames:['"+getLanguageMessage("device.logical_volumes.replica.running.status")+"','"+getLanguageMessage("device.logical_volumes.replica.running.type")+"','"+getLanguageMessage("device.logical_volumes.replica.running.launched")+"','"+getLanguageMessage("device.logical_volumes.replica.running.finished")+"','"+getLanguageMessage("device.logical_volumes.replica.running.source")+"','"+getLanguageMessage("device.logical_volumes.replica.running.destination")+"','"+getLanguageMessage("device.logical_volumes.replica.running.speed")+"','"+getLanguageMessage("device.logical_volumes.replica.running.time")+"','"+getLanguageMessage("device.logical_volumes.replica.running.left")+"','--'],\n");
 		sb.append("		colModel:[ {name:'status',index:'status', width:15}, \n");
 		sb.append("					{name:'desc',index:'desc', width:15},\n");
 		sb.append("					{name:'launched',index:'launched', width:15},\n");
 		sb.append("					{name:'finished',index:'finished', width:15},\n");
 		sb.append("					{name:'source',index:'source', width:35},\n");
 		sb.append("					{name:'destination',index:'destination', width:30},\n");
 		sb.append("					{name:'speed',index:'speed', width:15},\n");
 		sb.append("					{name:'time',index:'time', width:15},\n");
 		sb.append("					{name:'left',index:'left', width:15},\n");
 		sb.append("					{name:'actions',index:'actions', width:10, sortable:false, search:false}],\n");
 		sb.append("		width: $('#"+divId+"').width(),\n");
 		sb.append("		height: 'auto',\n");
 		sb.append("		rownumbers: false,\n");
 		sb.append("		multiselect: false,\n");
 		sb.append("		hidegrid:false,\n");
 		sb.append("		rowNum: 10,\n");
 		sb.append("		rowList : [5,10,25,50],\n");
 		sb.append("		gridComplete: LoadComplete,\n");
 		sb.append("		pager: '#pager',\n");
 		sb.append("		caption: '"+getLanguageMessage("device.logical_volumes.replica.running")+"',\n");
 		sb.append("		emptyDataText: '"+getLanguageMessage("device.message.no_replication_running")+"',\n");
 		sb.append("		onSelectRow: function(rowid, status) {\n");
 		sb.append("			$('#"+tableId+"').resetSelection();\n");
 		sb.append("		}\n");
 		sb.append("	});\n");
 		sb.append("jQuery('#"+tableId+"')\n");
 		sb.append("		.navGrid('#pager',{edit:false,add:false,del:false,search:false,refresh:false})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'',\n"); 
 		sb.append("			title:'"+getLanguageMessage("common.message.refresh")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-refresh',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.reload();\n");
 		sb.append("			},\n");
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'',\n"); 
 		sb.append("			title:'"+getLanguageMessage("device.logical_volumes.replica.relaunch")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-relaunch',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.href='/admin/DeviceReplication?type="+RELAUNCH+"';\n");
 		sb.append("			},\n"); 
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'',\n"); 
 		sb.append("			title:'"+getLanguageMessage("device.logical_volumes.replica.erase")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-erase',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.href='/admin/DeviceReplication?type="+ERASEDB+"';\n");
 		sb.append("			},\n"); 
 		sb.append("			position:'last'\n");
 		sb.append("});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-refresh').css({'background-image':'url(\"/images/arrow_refresh_16.png\")', 'background-position':'0'});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-relaunch').css({'background-image':'url(\"/images/control_start_16.png\")', 'background-position':'0'});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-erase').css({'background-image':'url(\"/images/delete_16.png\")', 'background-position':'0'});\n");
 		
 		return sb.toString();
 	}
 	
 	private String getJSONRunningJS(Map<String, List<Map<String, String>>> running) throws Exception {
 		StringBuilder sb = new StringBuilder();

		 if (!running.isEmpty()) {
			 List<Map<String, String>> _running = running.get("running");
			 List<Map<String, String>> _notRunning = running.get("notRunning");
			
			sb.append("[");
			String cadRunning = getStringRunning(_running, true);
			if (!cadRunning.isEmpty()) {
				sb.append(cadRunning);
				sb.append(getStringRunning(_notRunning, false));	
			} else
				sb.append(getStringRunning(_notRunning, true));
	 		sb.append("]");
        }
     
        return sb.toString();
 	}

 	private String getStringRunning(List<Map<String, String>> running, boolean init) {
 		StringBuilder sb = new StringBuilder();
 		boolean first = init;
 		if (running != null && !running.isEmpty()) {
	 		for (Map<String, String> run : running) {
					if (!first)
						sb.append(",");
					else
						first=false;
					
					String status = "<div";
	        	if (run.get("exit_status") == null || run.get("exit_status").equals("0"))
	        		status+=" class = 'grid_good'";
	        	else
	        		status+=" class = 'grid_warning'";
	        	status+=">";
	        			
	        	if (run.get("status") != null) {
	        		status+=run.get("status");
	        		if (run.get("exit_status") != null)
	        			status+=" ("+run.get("exit_status")+")";
	        	}
	        	status+="</div>";
	        	
	            String launched = "";
	            if (run.get("launched") != null)
	            	launched = run.get("launched");
	            
	            String finished = "";
	            if (run.get("finished") != null)
	            	finished = run.get("finished");
	            
	            String source = "";
	            if (run.get("source") != null)
	            	source = run.get("source");
	            
	            String destination = "";
	            if (run.get("destination") != null)
	            	destination = run.get("destination");
	            
	            String speed = "";
	            if (run.get("speed") != null)
	            	speed = run.get("speed");
	            
	            String time = "";
	            if (run.get("time") != null)
	            	time = run.get("time");
	            
	            String left = "";
	            if (run.get("left") != null)
	            	left = run.get("left");
	            
	            String desc = "";
	            if (run.get("desc") != null)
	            	desc = run.get("desc");
	            
	        	StringBuilder actions = new StringBuilder();
	            
	            actions.append("<div style='padding-top:5px;'><a href='javascript:viewLog(\\\\\""+run.get("id")+"\\\\\");' title='");
	            actions.append(getLanguageMessage("device.logical_volumes.replica.viewlog"));
	            actions.append("' alt='");
	            actions.append(getLanguageMessage("device.logical_volumes.replica.viewlog"));
	            actions.append("'><img src='/images/book_16.png'/></a>  ");
	    		actions.append("<a href='/admin/DeviceReplication?type=");
	    		actions.append(ERASE_COMMAND);
	    		actions.append("&id=");
	    		actions.append(run.get("id"));
	    		actions.append("' title='");
	    		actions.append(getLanguageMessage("device.logical_volumes.replica.erasecommand"));
	    		actions.append("' alt='");
	    		actions.append(getLanguageMessage("device.logical_volumes.replica.erasecommand"));
	    		actions.append("'><img src='/images/delete_16.png'/></a>");
	    		actions.append("</div>");
				sb.append("{\"status\":\""+status+"\", \"desc\":\""+desc+"\", \"launched\":\""+launched+"\", \"finished\":\""+finished+"\", \"source\":\""+source+"\", \"destination\":\""+destination+"\", \"speed\":\""+speed+"\", \"time\":\""+time+"\", \"left\":\""+left+"\", \"actions\":\""+actions.toString()+"\"}");
	 		}
 		}
 		return sb.toString();
 	}
 	
 	private String emptyGridFuncJS(String tableId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("function LoadComplete()\n");
 		sb.append("{\n");
 		sb.append("	 $('#"+tableId+"').trigger('reloadGrid');\n");
 		sb.append("		if ($('#"+tableId+"').jqGrid('getGridParam', 'reccount') == 0) \n");
 		sb.append("			DisplayEmptyText(true);\n");
 		sb.append("		else\n");
 		sb.append("			DisplayEmptyText(false);\n");
 		sb.append("}\n");
 		sb.append("function DisplayEmptyText( display)\n");
 		sb.append("{\n");
 		sb.append("		var grid = $('#"+tableId+"');\n");
 		sb.append("		var emptyText = grid.getGridParam('emptyDataText'); \n");
 		sb.append("		var container = grid.parents('.ui-jqgrid-view'); \n");
 		sb.append("		$('.EmptyData').remove(); \n");
 		sb.append("		if (display) {\n");
 		sb.append("			container.find('.ui-jqgrid-hdiv, .ui-jqgrid-bdiv').hide(); \n");
 		sb.append("			container.find('.ui-jqgrid-titlebar').after('<div class=\"EmptyData\" style=\"padding:10px;\">' + emptyText + '</div>'); \n");
 		sb.append("		}\n");
 		sb.append("		else {\n");
 		sb.append("			container.find('.ui-jqgrid-hdiv, .ui-jqgrid-bdiv').show(); \n");
 		sb.append("			$('.EmptyData').remove(); \n");
 		sb.append("		}\n");
 		sb.append("}\n");
 		return sb.toString();
 	}
 	
 	private String allLoad(String tableGridId, Map<String, List<Map<String, String>>> running) throws Exception{
 		StringBuilder sb = new StringBuilder();
 		sb.append("function reloadAll()\n");
 		sb.append("{\n");
		if (running != null && !running.isEmpty()) {
			sb.append("	var json = '"+getJSONRunningJS(running).replace("'", "\\'")+"';\n");
			sb.append("	var alldata = jQuery.parseJSON(json);\n");
			sb.append("	if (alldata) {\n");
			sb.append("		for(var i=0;i<=alldata.length;i++) {jQuery(\"#"+tableGridId+"\").jqGrid('addRowData',i+1,alldata[i]);}\n");
			sb.append("	}\n");
		}
		sb.append("}\n");
		return sb.toString();
 	}
 	
 	public String getJSViewJobLog() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function viewLog(id) {\n");
		_sb.append("	$('#idLog').val(id);\n");
		_sb.append("	$('#viewLogDialog').html('<div style=\"margin:20px;\">"+getLanguageMessage("advanced.groupjob.grid.loading")+" ...</div>');\n");
		_sb.append("	$('#viewLogDialog').dialog( 'open' );\n");
		_sb.append("	$.ajax({\n");
	 	_sb.append("		url: '/admin/DeviceReplication?type="+DeviceReplication.LOG_COMMAND+"',\n");
	 	_sb.append("		cache: false,\n");
	 	_sb.append("		data: {id : id}\n");
	 	_sb.append("	}).done(function( html ) {\n");
	 	_sb.append("		$('#viewLogDialog').html(html);\n");
	 	_sb.append("	});\n");
	 	_sb.append("};");
	 	return _sb.toString();
	}
}
