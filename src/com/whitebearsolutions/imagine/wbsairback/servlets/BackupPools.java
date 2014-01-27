package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;

public class BackupPools extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int POOL_ADD = 12;
	public final static int POOL_EDIT = 11;
	public final static int POOL_SAVE = 2;
	public final static int POOL_DELETE = 3;
	public final static int POOL_VOLS = 4;
	public final static int POOL_VOLS_AVAILABLE_TAPE = 9;
	public final static int POOL_VOLS_ADD_TAPE = 10;
	public final static int POOL_VOLS_AVAILABLE_AUTOCHANGER = 5;
	public final static int POOL_VOLS_ADD_AUTOCHANGER = 6;
	public final static int POOL_VOLS_PURGE = 7;
	public final static int POOL_VOLS_DELETE = 8;
	public final static int POOL_MODIFY = 15;
	public final static int POOL_JOBS = 13;
	public final static int POOL_JOBS_REMOVE = 14;
	private int type;
	public final static String baseUrl = "/admin/"+BackupPools.class.getSimpleName();
	
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
		    
		    PoolManager _pm = new PoolManager(this.sessionManager.getConfiguration());
		    StorageManager _sm = new StorageManager(this.sessionManager.getConfiguration());
		    
		    switch(this.type) {
	    		default: {
	    				writeDocumentHeader();
		    			int _offset = 1;
		    			List<Map<String, String>> _pools = _pm.getPools();
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/database_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.pool"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.pool.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.pool"));
		        		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
		
						_xhtml_out.print("<a href=\"/admin/BackupPools?type=");
	                    _xhtml_out.print(POOL_ADD);
	            		_xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_pools.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.storage"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.retention"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _pool : _pools) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
								_xhtml_out.print(_pool.get("name"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_pool.get("storage"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_pool.get("retention"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_pm.getPoolVolumesSizeFormated(Integer.parseInt(_pool.get("id"))));
								_xhtml_out.println("</td>");
								_xhtml_out.println("<td>");
								_xhtml_out.print("<a href=\"/admin/BackupPools?poolName=");
								_xhtml_out.print(_pool.get("name"));
								_xhtml_out.print("&type=");
								_xhtml_out.print(POOL_JOBS);
								_xhtml_out.print("\"><img src=\"/images/database_jobs_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("backup.jobs"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/BackupPools?poolId=");
								_xhtml_out.print(_pool.get("id"));
								_xhtml_out.print("&poolName=");
								_xhtml_out.print(_pool.get("name"));
								_xhtml_out.print("&storage=");
								_xhtml_out.print(_pool.get("storage"));
								_xhtml_out.print("&type=");
								_xhtml_out.print(POOL_VOLS);
								_xhtml_out.print("\"><img src=\"/images/database_gear_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.volumes"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.volumes"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/BackupPools?pool=");
								_xhtml_out.print(_pool.get("name"));
								_xhtml_out.print("&type=");
								_xhtml_out.print(POOL_EDIT);
								_xhtml_out.print("&storage=");
								_xhtml_out.print(_pool.get("storage"));
								_xhtml_out.print("\"><img src=\"/images/database_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/BackupPools?type=");
								_xhtml_out.print(POOL_DELETE);
								_xhtml_out.print("&pool=");
								_xhtml_out.print(_pool.get("name"));
								_xhtml_out.print("\"><img src=\"/images/database_delete_16.png\" title=\"");
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
			            	_xhtml_out.println(getLanguageMessage("device.message.no_pools"));
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
	    		case POOL_ADD: {
	    				writeDocumentHeader();
		    			
		    			writeDocumentBack("/admin/BackupPools");
		    			_xhtml_out.println("<form action=\"/admin/BackupPools\" name=\"pool\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + POOL_SAVE + "\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/database_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.pool"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.pool.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.pool.new_pool"));
		        		_xhtml_out.print("<a href=\"javascript:submitForm(document.pool.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"storage\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.storage"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"storage\">");
						for(String storage : _sm.getAvailableStorages()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(storage);
							_xhtml_out.print("\">");
							_xhtml_out.print(storage);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"retention\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.retention"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"retention\" value=\"90\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"durationHours\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.use_duration"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"durationHours\"/>");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"copy_destination\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.copy_destination"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"copy_destination\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : _pm.getPoolNames()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	          	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"scratch_pool\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.scratchpool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"scratch_pool\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : _pm.getPoolNames()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	          	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"recycle_pool\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.recycle_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"recycle_pool\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String pool : _pm.getPoolNames()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(pool);
							_xhtml_out.print("\">");
							_xhtml_out.print(pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"migrationHours\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.migration_hours"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"migrationHours\"/>");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
	    			}
	    			break;
	    		case POOL_SAVE: {
		    			
		    			int duration = 0, retention = 0, migrationHours = 0;
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception("backup.pool.specify_repository");
		    			}
		    			if(request.getParameter("durationHours") != null && !request.getParameter("durationHours").isEmpty()) {
			    			try {
			    				duration = Integer.parseInt(request.getParameter("durationHours"));
			    			} catch(NumberFormatException _ex) {
			    				throw new Exception(getLanguageMessage("backup.pool.no_hour_duracion"));
			    			}
		    			}
		    			try {
		    				retention = Integer.parseInt(request.getParameter("retention"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception(getLanguageMessage("backup.pool.no_hour_retencion"));
		    			}
		    			if(request.getParameter("migrationHours") != null && !request.getParameter("migrationHours").isEmpty()) {
			    			try {
			    				migrationHours = Integer.parseInt(request.getParameter("migrationHours"));
			    			} catch(NumberFormatException _ex) {}
		    			}
		    			_pm.setPool(request.getParameter("name"), request.getParameter("storage"), retention, duration, request.getParameter("copy_destination"), migrationHours,request.getParameter("scratch_pool"),request.getParameter("recycle_pool"));
		    			response.sendRedirect("/admin/BackupPools");
		    			this.redirected=true;
	    			}
	    			break;
	    		case POOL_EDIT: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("pool") == null || request.getParameter("pool").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			
		    			Map<String, String> pool = _pm.getPool(request.getParameter("pool"));
		    			
		    			writeDocumentBack("/admin/BackupPools");
		    			_xhtml_out.println("<form action=\"/admin/BackupPools\" name=\"pool\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + POOL_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("pool") + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"device\" value=\"" + pool.get("storage") + "\"/>");
						_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/database_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.pool"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.pool.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.pool.new_pool"));
		          		_xhtml_out.print("<a href=\"javascript:submitForm(document.pool.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print("<label for=\"_name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    	_xhtml_out.print(request.getParameter("pool"));
	        	    	_xhtml_out.print("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"storage\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.storage"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"storage\">");
						for(String storage : _sm.getAvailableStorages()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(storage);
							_xhtml_out.print("\"");
							if(storage.equals(pool.get("storage"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(storage);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"retention\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.retention"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"retention\" value=\"");
	        	    	_xhtml_out.print(pool.get("retention"));
	        	    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"durationHours\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.use_duration"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"durationHours\"");
	        	    	if(pool.get("volume-duration") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(pool.get("volume-duration"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"copy_destination\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.copy_destination"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"copy_destination\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String _pool : _pm.getPoolNames()) {
							if(_pool.equals(request.getParameter("pool"))) {
								continue;
							}
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_pool);
							_xhtml_out.print("\"");
							if(_pool.equals(pool.get("copy"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	          	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"scratch_pool\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.scratchpool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"scratch_pool\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String _pool : _pm.getPoolNames()) {
	        	    		if(_pool.equals(request.getParameter("pool"))) {
								continue;
							}
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_pool);
							_xhtml_out.print("\"");
							if(_pool.equals(pool.get("scratch_pool"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	          	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"recycle_pool\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.recycle_pool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"recycle_pool\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(String _pool : _pm.getPoolNames()) {
	        	    		if(_pool.equals(request.getParameter("pool"))) {
								continue;
							}
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_pool);
							_xhtml_out.print("\"");
							if(_pool.equals(pool.get("recycle_pool"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_pool);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"migrationHours\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.pool.migration_hours"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"migrationHours\"");
	        	    	if(pool.get("migration-hours") != null) {
	        	    		_xhtml_out.print(" value=\"");
	        	    		_xhtml_out.print(pool.get("migration-hours"));
	        	    		_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.print("/>");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.hours"));
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
	    			}
	    			break;
	    		case POOL_DELETE: {
		    			if(request.getParameter("pool") == null || request.getParameter("pool").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				PoolManager.removePool(request.getParameter("pool"));
			    			response.sendRedirect("/admin/BackupPools");
			    			this.redirected=true;
		    			} else {
		    				writeDocumentHeader();
		    				writeDocumentQuestion(getLanguageMessage("backup.pool.question_erase"), "/admin/BackupPools?type=" + POOL_DELETE + "&pool=" + request.getParameter("pool") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case POOL_VOLS: {
	    				writeDocumentHeader();
		    			int poolId, _offset = 0;
		    			try {
		    				poolId = Integer.parseInt(request.getParameter("poolId"));
		    			} catch(NumberFormatException _ex) {
		    				throw new Exception("backup.pool.no_pool");
		    			}
		    			
		    			String _type = PoolManager.getPoolType(request.getParameter("poolName"));
		    			List<Map<String, String>> _volumes = _pm.getPoolVolumes(poolId);
		       			
		    			writeDocumentBack("/admin/BackupPools");
		    		    _xhtml_out.println("<form action=\"/admin/BackupPools\" name=\"editForm\" method=\"post\">\n");
		 				_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + POOL_VOLS_ADD_AUTOCHANGER + "\"/>\n");
		 				_xhtml_out.println("<input type=\"hidden\" name=\"storage\" value=\"" + request.getParameter("storage") + "\"/>\n");
		 				_xhtml_out.println("<input type=\"hidden\" name=\"poolName\" value=\"" + request.getParameter("poolName") + "\"/>\n");
		 				_xhtml_out.println("<input type=\"hidden\" name=\"poolId\" value=\"" + request.getParameter("poolId") + "\"/>\n");
		                _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/database_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.pool"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.pool.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.pool"));
		        		_xhtml_out.print("<a href=\"javascript:document.editForm.type.value="+POOL_VOLS_DELETE+";submitForm(document.editForm.submit());\"><img src=\"/images/database_delete_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.remove"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.remove"));
	                	_xhtml_out.println("\"/></a>");
		        		_xhtml_out.print("<a href=\"javascript:document.editForm.type.value="+POOL_VOLS_PURGE+";submitForm(document.editForm.submit());\"><img src=\"/images/database_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("backup.pool.volume.purge"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("backup.pool.volume.purge"));
	                	_xhtml_out.println("\"/></a>");
		        		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
		
						if("tape".equals(_type)) {
	                    	_xhtml_out.print("<a href=\"/admin/BackupPools?type=");
	                        _xhtml_out.print(POOL_VOLS_AVAILABLE_TAPE);
	                        _xhtml_out.print("&pool=");
	                        _xhtml_out.print(request.getParameter("poolName"));
	                		_xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.add"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.add"));
		                	_xhtml_out.println("\"/></a>");
		    			} else if("autochanger".equals(_type)) {
		    				_xhtml_out.print("<a href=\"/admin/BackupPools?type=");
		                    _xhtml_out.print(POOL_VOLS_AVAILABLE_AUTOCHANGER);
		                    _xhtml_out.print("&pool=");
	                        _xhtml_out.print(request.getParameter("poolName"));
	                  		_xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.add"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.add"));
		                	_xhtml_out.println("\"/></a>");
		    			}
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_volumes.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                	_xhtml_out.print("<td>");
		                	_xhtml_out.println("<input type=\"checkbox\" name=\"checkall\" onclick=\"checkUncheckAll(this);\" />");
		                	_xhtml_out.print("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.status"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.slot"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.files"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.retention"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.first-write"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.pool.volume.last-write"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _volume : _volumes) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td>");
		                    	_xhtml_out.println("<input type=\"checkbox\" name=\"volId\" value=\"" + _volume.get("name") + "\"/>");
								_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(_volume.get("name"));
		                    	_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("status"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("size"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("type"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("slot"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("files"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("retention"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("first-write"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(_volume.get("last-write"));
								_xhtml_out.print("</td>");
								_xhtml_out.println("<td>");
								_xhtml_out.println("</td>");
		                    	_xhtml_out.println("</tr>");
			                    _offset++;
		                    }
					    } else {
			            	_xhtml_out.println("<tr>");
			            	_xhtml_out.println("<td>");
			            	_xhtml_out.println(getLanguageMessage("device.message.no_pool_volumes"));
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
	    		case POOL_VOLS_AVAILABLE_TAPE: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("pool") == null || request.getParameter("pool").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			
		    			Map<String, String> _pool = _pm.getPool(request.getParameter("pool"));
		    			TapeManager.mountTape(_pool.get("storage"));
		    			Map<String, String> _info = PoolManager.getTapeStatus(_pool.get("storage"));
						
		    			writeDocumentBack("/admin/BackupPools");
		    			_xhtml_out.println("<form action=\"/admin/BackupPools\" name=\"pool\" method=\"get\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + POOL_VOLS_ADD_TAPE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"storage\" value=" + request.getParameter("storage") + ">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"poolName\" value=" + request.getParameter("pool") + ">");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/database_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.pool"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.pool.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.pool"));
						if("mounted".equals(_info.get("status")) || "unassigned".equals(_info.get("status"))) {
			        		_xhtml_out.print("<a href=\"javascript:submitForm(document.pool.submit());\"><img src=\"/images/accept_16.png\" title=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.save"));
		                	_xhtml_out.print("\" alt=\"");
		                	_xhtml_out.print(getLanguageMessage("common.message.save"));
		                	_xhtml_out.println("\"/></a>");
			
						}
						_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
		
						_xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    if("empty".equals(_info.get("status"))) {
	                    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"status\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.pool.volume.status"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"status\" value=\"");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.empty"));
		        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
		        	    	_xhtml_out.println("</div>");
						} else if("unassigned".equals(_info.get("status"))) {
							_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"status\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.pool.volume.status"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"status\" value=\"");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.unassigned"));
		        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"label\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.label"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"label\"/>");
		        	    	_xhtml_out.println("</div>");
						} else if("mounted".equals(_info.get("status"))) {
							_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"status\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.pool.volume.status"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"status\" value=\"");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.assigned"));
		        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"label\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape.label"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"label\" value=\"");
		        	    	_xhtml_out.print(_info.get("label"));
		        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
		        	    	_xhtml_out.println("</div>");
						} else {
							_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"status\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.pool.volume.status"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"status\" value=\"");
		        	    	_xhtml_out.print(getLanguageMessage("common.message.unknown"));
		        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
		        	    	_xhtml_out.println("</div>");
						}
			            _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.print("</div>");
				    	_xhtml_out.print("</form>");
		    		}
	    			break;
	    		case POOL_VOLS_ADD_TAPE: {
		    			
		    			if(request.getParameter("poolName") == null || request.getParameter("poolName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			if(request.getParameter("label") == null || request.getParameter("label").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.volume.specify_label"));
		    			}
		    			
		    			_pm.addTapeVolume(request.getParameter("poolName"), request.getParameter("label"));
		    			
		    			response.sendRedirect("/admin/BackupPools");
		    			this.redirected=true;
	    			}
	    			break;
	    		case POOL_VOLS_AVAILABLE_AUTOCHANGER: {
	    				writeDocumentHeader();
		    			
		    			int _offset = 0;
		    			if(request.getParameter("pool") == null || request.getParameter("pool").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			Map<String, String> _pool = _pm.getPool(request.getParameter("pool"));
		    			List<Map<String, String>> _volumes = _pm.getAvailableAutochangerVolumes(_pool.get("storage"));
		    			
		    		    writeDocumentBack("/admin/BackupPools");
		    		    _xhtml_out.println("<form action=\"/admin/BackupPools\" name=\"pool\" method=\"get\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + POOL_VOLS_ADD_AUTOCHANGER + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"storage\" value=\"" + _pool.get("storage") + "\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"poolName\" value=\"" + _pool.get("name") + "\">");
	    			    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/database_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.pool"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.pool.info"));
		    			_xhtml_out.println("</div>");
		    		    
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.pool"));
		        		_xhtml_out.print("<a href=\"javascript:submitForm(document.pool.submit());\"><img src=\"/images/accept_16.png\" title=\"");
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
	                    _xhtml_out.println("<br/>");
	                    
	                    List<Map<String, String>> _drives = LibraryManager.getStorageDrives(_pool.get("storage"));
	                    if (_drives != null && !_drives.isEmpty()) {
		                    _xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"drive\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.pool.drive"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<select class=\"form_select\" name=\"drive\" >");
		        	    	for (Map<String, String> _drive : _drives) {
		        	    		_xhtml_out.print("<option value=\"");	        	    		
		        	    		_xhtml_out.print(_drive.get("index"));
		        	    		_xhtml_out.print("\"");
		        	    		if (Integer.parseInt(_drive.get("index")) == 0) {
		        	    			_xhtml_out.print(" selected=\"selected\" ");	
		        	    		}
		        	    		_xhtml_out.print(" >");
		        	    		_xhtml_out.print(_drive.get("index"));
		        	    		_xhtml_out.print(":");
		        	    		_xhtml_out.print(_drive.get("name"));
		        	    		_xhtml_out.print("</option>");
		        	    	}
		        	    	_xhtml_out.print("</select>");
		        	    	_xhtml_out.println("</div>");
	                    } else {			// Caso de remote storage library
	                    	_xhtml_out.print("<input type=\"hidden\" name=\"drive\" value=\"0\" >");
	                    }
	        	    	
	                    _xhtml_out.println("<table>");
	                    for(Map<String, String> _volume : _volumes) {
							if(_offset % 2 == 0) {
								_xhtml_out.print("<tr");
								if(_offset % 4 == 0) {
									_xhtml_out.print(" class=\"highlight\"");
								}
								_xhtml_out.println(">");
								_xhtml_out.print("<td><input class=\"form_radio\" type=\"radio\" name=\"slot\" value=\"");
								_xhtml_out.print(_volume.get("slot"));
								_xhtml_out.print(":");
								_xhtml_out.print(_volume.get("name"));
								_xhtml_out.print("\"/>slot ");
								_xhtml_out.print(_volume.get("slot"));
								_xhtml_out.print(":");
								_xhtml_out.print(_volume.get("name"));
								_xhtml_out.print("&nbsp;");
								_xhtml_out.println("</td>");
								if((_offset + 1) == _volumes.size()) {
									_xhtml_out.println("<td>&nbsp;</td>");
									_xhtml_out.println("</tr>");
								}
							} else {
								_xhtml_out.print("<td><input class=\"form_radio\" type=\"radio\" name=\"slot\" value=\"");
								_xhtml_out.print(_volume.get("slot"));
								_xhtml_out.print(":");
								_xhtml_out.print(_volume.get("name"));
								_xhtml_out.print("\"/>slot ");
								_xhtml_out.print(_volume.get("slot"));
								_xhtml_out.print(":");
								_xhtml_out.print(_volume.get("name"));
								_xhtml_out.print("&nbsp;");
								_xhtml_out.println("</td>");
								_xhtml_out.println("</tr>");
							}
							_offset++;
						}
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	        	    	
	                    _xhtml_out.println("</fieldset>");
				    	_xhtml_out.println("<div class=\"clear\"/></div>");
				    	_xhtml_out.print("</div>");
				    	_xhtml_out.print("</form>");
		    		}
                    break;
                case POOL_VOLS_ADD_AUTOCHANGER: {
	                	
	                	if(request.getParameter("poolName") == null || request.getParameter("poolName").isEmpty()) {

	                	throw new Exception(getLanguageMessage("backup.pool.name_not_found"));
		    			}
	                	if(request.getParameter("slot") == null || request.getParameter("slot").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.volume.specify_slot"));
		    			}
	                	if(!request.getParameter("slot").contains(":")) {
		    				throw new Exception(getLanguageMessage("backup.pool.volume.no_slot_label"));
		    			}
	                	String drive = "all";
	                	if(request.getParameter("drive") != null && !request.getParameter("drive").isEmpty()) {
	                		drive = request.getParameter("drive");
	                	}
	                	
	                	String[] _value = request.getParameter("slot").split(":");
	                	_pm.addAutochangerVolume(request.getParameter("poolName"), _value[1], _value[0], drive);
	                	response.sendRedirect("/admin/BackupPools?poolId=" + _pm.getPoolId(request.getParameter("poolName")) + "&poolName=" + request.getParameter("poolName") + "&storage=" + request.getParameter("storage") + "&type=" + POOL_VOLS);
	                	this.redirected=true;
                	}
	    			break;
    			case POOL_VOLS_DELETE: {
	    				if(request.getParameter("poolName") == null || request.getParameter("poolName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			if(request.getParameterValues("volId")  == null || request.getParameter("volId").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.volume.specify_label"));
		    			}
		   
			    		if(request.getParameter("confirm") != null) {
				    		for(String volName : request.getParameterValues("volId")) {
			    	    		PoolManager.removePoolVolume(request.getParameter("poolName"), volName);
			    			} 
	//		    			PoolManager.removePoolVolume(request.getParameter("poolName"), request.getParameter("label"));
				    		response.sendRedirect("/admin/BackupPools?poolId=" + request.getParameter("poolId") + "&poolName=" + request.getParameter("poolName") + "&storage=" + request.getParameter("storage") + "&type=" + POOL_VOLS);
				    		this.redirected=true;
			    		} else {
			    			writeDocumentHeader();
			    			writeDocumentListQuestion(getLanguageMessage("backup.pool.question_erase"),"volId", Arrays.asList(request.getParameterValues("volId")), "/admin/BackupPools?type=" + POOL_VOLS_DELETE + "&poolId=" + request.getParameter("poolId") + "&poolName=" + request.getParameter("poolName") + "&label=" + request.getParameter("label") + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
			    		}		    			
    				}
	    			break;
	    		case POOL_VOLS_PURGE: {
		    			
		    			if(request.getParameter("poolName") == null || request.getParameter("poolName").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
		    			}
		    			if(request.getParameter("volId") == null || request.getParameter("volId").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.pool.volume.specify_label"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
			    			for(String volName : request.getParameterValues("volId")) {
			    				PoolManager.purgePoolVolume(volName);
		    				}
//		    				PoolManager.purgePoolVolume(request.getParameter("label"));
			    			response.sendRedirect("/admin/BackupPools?poolId="+request.getParameter("poolId")+"&poolName="+request.getParameter("poolName")+"&storage="+request.getParameter("storage")+"&type=" + POOL_VOLS);
			    			this.redirected=true;
		    			} else {
		    				writeDocumentHeader();
		    				writeDocumentListQuestion(getLanguageMessage("backup.pool.question_purge"),"volId", Arrays.asList(request.getParameterValues("volId")),  "/admin/BackupPools?type=" + POOL_VOLS_PURGE + "&poolId=" + request.getParameter("poolId") + "&poolName=" + request.getParameter("poolName") + "&label=" + request.getParameter("label") + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case POOL_JOBS: {
	    			writeDocumentHeader();
	    			
	    			if(request.getParameter("poolName") == null || request.getParameter("poolName").isEmpty()) {
	    				throw new Exception(getLanguageMessage("backup.pool.specify_name"));
	    			}
	    			String level = (request.getParameter("level") == null || request.getParameter("level").isEmpty())?"ALL":request.getParameter("level");
	    			Map<String, String> _pool = _pm.getPool(request.getParameter("poolName"));
	    			List<Map<String,String>> _jobs = _pm.getPendantPoolJobs(_pool.get("name"), level);
	    			
	    			int MAX_ENTRIES = 10;
	    			int pag = 1;
	    		    if(request.getParameter("pag") != null) {
	    		    	try {
	    		    		pag = Integer.valueOf(request.getParameter("pag")).intValue();
	    		    	} catch(NumberFormatException _ex) {}
	    		    }
	    		    int offset = (pag * MAX_ENTRIES) - MAX_ENTRIES;
	    		    int TOTAL_JOBS = _jobs.size();
	    			
	    			writeDocumentBack("/admin/BackupPools");
    			    _xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/database_32.png\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("backup.pool"));
    				_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("backup.pool.jobs.info"));
	    			_xhtml_out.println("</div>");
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("backup.pool.jobs"));
	        		_xhtml_out.print("<a href=\"javascript:submitForm(document.pool.submit());\"><img src=\"/images/cross_16.png\" title=\"");
                	_xhtml_out.print(getLanguageMessage("common.message.remove"));
                	_xhtml_out.print("\" alt=\"");
                	_xhtml_out.print(getLanguageMessage("common.message.remove"));
                	_xhtml_out.println("\"/></a>");
	
            		_xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
                	_xhtml_out.print("\" alt=\"");
                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
                	_xhtml_out.println("\"/></a>");
	
                	_xhtml_out.println("</h2>");
                    _xhtml_out.println("<fieldset>");
                    _xhtml_out.println("<br/>");
                    _xhtml_out.println("<table>");
                    
                    _xhtml_out.println("<form action=\"/admin/BackupPools\" name=\"filter\" method=\"get\">");
    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + POOL_JOBS + "\"/>");
    			    _xhtml_out.println("<input type=\"hidden\" name=\"poolId\" value=\"" + _pool.get("id") + "\">");
    			    _xhtml_out.println("<input type=\"hidden\" name=\"poolName\" value=\"" + _pool.get("name") + "\">");
    			    
    			    _xhtml_out.println("<tr>");
    			    _xhtml_out.println("<td colspan=\"8\">");
    			    _xhtml_out.println("<h3>"+ _pool.get("name") + "</h3>");
    			    _xhtml_out.println("</td>");
    			    _xhtml_out.println("<td colspan=\"1\">");
    			    _xhtml_out.println("<label for=\"level\">");
    			    _xhtml_out.println(getLanguageMessage("backup.jobs.level") + ":&nbsp;&nbsp;");
    			    _xhtml_out.println("</label>");
    			    _xhtml_out.println("<select name=\"level\" onchange=\"javascript:this.form.submit();\">");
    			    _xhtml_out.println("<option value=\"ALL\"");
    			    if (level.equals("ALL")) _xhtml_out.println(" selected=\"selected\"");
    			    _xhtml_out.println(">");
    			    _xhtml_out.println(getLanguageMessage("common.message.all"));
    			    _xhtml_out.println("</option>");
    			    _xhtml_out.println("<option value=\"F\"");
    			    if (level.equals("F")) _xhtml_out.println(" selected=\"selected\"");
    			    _xhtml_out.println(">");
    			    _xhtml_out.println(getLanguageMessage("backup.schedule.level_full"));
    			    _xhtml_out.println("</option>");
    			    _xhtml_out.println("<option value=\"I\"");
    			    if (level.equals("I")) _xhtml_out.println(" selected=\"selected\"");
    			    _xhtml_out.println(">");
    			    _xhtml_out.println(getLanguageMessage("backup.schedule.level_incremental"));
    			    _xhtml_out.println("</option>");
    			    _xhtml_out.println("<option value=\"D\"");
    			    if (level.equals("D")) _xhtml_out.println(" selected=\"selected\"");
    			    _xhtml_out.println(">");
    			    _xhtml_out.println(getLanguageMessage("backup.schedule.level_differential"));
    			    _xhtml_out.println("</option>");
    			    _xhtml_out.println("</select>");
    			    _xhtml_out.println("</td>");
    			    _xhtml_out.println("</tr>");
    			    _xhtml_out.println("</form>");
                    
                    if (! _jobs.isEmpty()) {
		    		    _xhtml_out.println("<form action=\"/admin/BackupPools\" name=\"pool\" method=\"get\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + POOL_JOBS_REMOVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"poolName\" value=\"" + _pool.get("name") + "\">");
	                    
	                    _xhtml_out.println("<tr>");
	                	_xhtml_out.print("<td>");
	                	_xhtml_out.println("<input type=\"checkbox\" name=\"checkall\" onclick=\"checkUncheckAll(this);\" />");
	                	_xhtml_out.print("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print("ID");
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.name"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.type"));
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
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.total_size"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("backup.jobs.status"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	    			    
	                    int _offset = 0;
	                    int end =  (offset+MAX_ENTRIES)  > TOTAL_JOBS ?  TOTAL_JOBS : offset+MAX_ENTRIES;
		    			for (int i = offset; i < end; i++) {
		    				Map<String,String> job = _jobs.get(i);
		    				_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println("<input type=\"checkbox\" name=\"jobId\" value=\"" + job.get("jobid") + "\"/>");
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(job.get("jobid"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(job.get("name"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(job.get("type"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(job.get("level"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(job.get("starttime"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(job.get("endtime"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(job.get("size"));
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.println(getLanguageMessage(job.get("status")));
							_xhtml_out.println("</td>");
							_xhtml_out.println("</tr>");
							_offset++;
						}
		    			_xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td colspan=\"9\" class=\"pages\">");
	                    for(int j = 0; j <= (TOTAL_JOBS / MAX_ENTRIES); j++) {
					    	if(j == (pag - 1)) {
					    		_xhtml_out.print("<a class=\"page_box\">");
					    		_xhtml_out.print(j + 1);
					    		_xhtml_out.println("</a>");
					    	} else {
					    		_xhtml_out.print("<a class=\"page_box\" href=\"/admin/BackupPools?poolName=");
					    		_xhtml_out.print(_pool.get("name"));
					    		_xhtml_out.print("&type=");
					    		_xhtml_out.print(POOL_JOBS);
                                _xhtml_out.print("&pag=");
					    		_xhtml_out.print(j + 1);
					    		_xhtml_out.print("\">");
					    		_xhtml_out.print(j + 1);
					    		_xhtml_out.println("</a>");
					    	}
					    }
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
                    } else {
                    	_xhtml_out.println("<tr><td><strong>");
                    	_xhtml_out.println(getLanguageMessage("backup.pool.no_jobs"));
                    	_xhtml_out.println("</strong></td><td colspan=\"8\">&nbsp;</td></tr>");
                    }
                    _xhtml_out.println("</table>");
                    _xhtml_out.println("<br/>");
                    _xhtml_out.println("</fieldset>");
			    	_xhtml_out.println("<div class=\"clear\"/></div>");
			    	_xhtml_out.print("</div>");
			    	_xhtml_out.print("</form>");
    			}
    			break;
	    	case POOL_JOBS_REMOVE: {
	    			
	    			if(request.getParameterValues("jobId") == null) {
	    				response.sendRedirect("/admin/BackupPools?poolName=" + request.getParameter("poolName")+"&type="+POOL_JOBS);
	    				this.redirected=true;
	    			} else {
	    				if(request.getParameter("confirm") != null && request.getParameter("confirm").equals("true")) {
			    			for(String jobId : request.getParameterValues("jobId")) {
		    	    			_pm.markJobAsCopied(jobId);
		    				}
			    			response.sendRedirect("/admin/BackupPools?poolName=" + request.getParameter("poolName")+"&type="+POOL_JOBS);
			    			this.redirected=true;
		    			} else {
		    				writeDocumentListQuestion(getLanguageMessage("backup.jobs.exception.mark_as_copied"), "jobId", Arrays.asList(request.getParameterValues("jobId")),"/admin/BackupPools?type=" + POOL_JOBS_REMOVE + "&poolName=" + request.getParameter("poolName") + "&confirm=true", null);
		    			}
	    			}
				}
    			break;
	    	}
 		} catch(Exception _ex) {
 			writeDocumentError(getWBSLocalizedExMessage(_ex.getMessage()), "/admin/BackupPools");
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}
