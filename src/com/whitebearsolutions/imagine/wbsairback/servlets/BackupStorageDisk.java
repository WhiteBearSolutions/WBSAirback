package com.whitebearsolutions.imagine.wbsairback.servlets;
	

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.util.Configuration;

public class BackupStorageDisk extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int DISK_ADD = 2;
	public final static int DISK_SAVE = 3;
	public final static int DISK_EDIT = 4;
	public final static int DISK_DELETE = 5;
	public final static int SCSI_RESCAN = 6;
	private int type;
	public final static String baseUrl = "/admin/"+BackupStorageDisk.class.getSimpleName();
	
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
			if (type != DISK_SAVE)
				writeDocumentHeader();
	    	
		    Configuration _c = this.sessionManager.getConfiguration();
		    StorageManager _sm = new StorageManager(_c);
			NetworkManager _nm = this.sessionManager.getNetworkManager();
			List<String> _netInterfaces=_nm.getConfiguredInterfaces();
		    switch(this.type) {
	    		default: {
		    			int _offset = 0;
		    			List<Map<String, String>> _disk_devices = StorageManager.getDiskVolumeDevices();
		    			List<Map<String, String>> _tape_devices = StorageManager.getTapeDevices();
		    			List<Map<String, String>> _autochanger_devices = StorageManager.getAutochangerDevices();
		    			List<String> _remote_devices = StorageManager.getRemoteDevices();
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.storage.disk_storages"));
						_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
						_xhtml_out.print("<a href=\"/admin/BackupStorageDisk?type=" + DISK_ADD + "\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_disk_devices.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.volume"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> device : _disk_devices) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(device.get("name"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(device.get("type"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(device.get("device"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print("<a href=\"/admin/BackupStorageDisk?type=");
		                    	_xhtml_out.print(DISK_EDIT);
		                    	_xhtml_out.print("&storage=");
		                    	_xhtml_out.print(device.get("name"));
		                    	_xhtml_out.print("\"><img src=\"/images/brick_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								if(!device.get("name").equals("SystemStorage")) {
									_xhtml_out.print("<a href=\"/admin/BackupStorageDisk?type=");
									_xhtml_out.print(DISK_DELETE);
									_xhtml_out.print("&storage=");
									_xhtml_out.print(device.get("name"));
									_xhtml_out.print("\"><img src=\"/images/brick_delete_16.png\" title=\"");
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
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_storages"));
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
						_xhtml_out.print(getLanguageMessage("backup.storage.tape_storages"));
						_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
						_xhtml_out.print("<a href=\"/admin/BackupStorageTape?type=" + BackupStorageTape.TAPE_ADD + "\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_tape_devices.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.volume"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> device : _tape_devices) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(device.get("name"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(TapeManager.getTapeDeviceDescription(device.get("device")));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print("<a href=\"/admin/TapeOperations?storage=");
								_xhtml_out.print(device.get("name"));
								_xhtml_out.print("\"><img src=\"/images/tape_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("backup.storage.tape_operations"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("backup.storage.tape_operations"));
			                    _xhtml_out.println("\"/></a>");
		                    	_xhtml_out.print("<a href=\"/admin/BackupStorageTape?type=");
		                    	_xhtml_out.print(BackupStorageTape.TAPE_EDIT);
		                    	_xhtml_out.print("&storage=");
		                    	_xhtml_out.print(device.get("name"));
		                    	_xhtml_out.print("\"><img src=\"/images/brick_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/BackupStorageTape?type=");
								_xhtml_out.print(BackupStorageTape.TAPE_DELETE);
								_xhtml_out.print("&storage=");
								_xhtml_out.print(device.get("name"));
								_xhtml_out.print("\"><img src=\"/images/brick_delete_16.png\" title=\"");
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
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_storages"));
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
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger_storages"));
						_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
						_xhtml_out.print("<a href=\"/admin/BackupStorageLibrary?type=" + BackupStorageLibrary.LIBRARY_ADD + "\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_autochanger_devices.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.volume"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.autochanger.drives"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> device : _autochanger_devices) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(device.get("name"));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(LibraryManager.getAutochangerDescription(device.get("device")));
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	for(int j = 0; device.containsKey("drive" + j); j++) {
									if(j > 0) {
										_xhtml_out.print(",");
									}
									_xhtml_out.print(TapeManager.getTapeDeviceDescription(device.get("drive" + j)));
								}
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print("<a href=\"/admin/LibraryOperations?storage=");
								_xhtml_out.print(device.get("name"));
								_xhtml_out.print("\"><img src=\"/images/tape_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("backup.storage.tape_operations"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("backup.storage.tape_operations"));
			                    _xhtml_out.println("\"/></a>");
		                    	_xhtml_out.print("<a href=\"/admin/BackupStorageLibrary?type=");
		                    	_xhtml_out.print(BackupStorageLibrary.LIBRARY_EDIT);
		                    	_xhtml_out.print("&storage=");
		                    	_xhtml_out.print(device.get("name"));
		                    	_xhtml_out.print("\"><img src=\"/images/brick_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/BackupStorageLibrary?type=");
								_xhtml_out.print(BackupStorageLibrary.LIBRARY_DELETE);
								_xhtml_out.print("&storage=");
								_xhtml_out.print(device.get("name"));
								_xhtml_out.print("\"><img src=\"/images/brick_delete_16.png\" title=\"");
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
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_storages"));
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
						_xhtml_out.print(getLanguageMessage("backup.storage.remote_storages"));
						_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
						_xhtml_out.print("<a href=\"/admin/BackupStorageRemote?type=" + BackupStorageRemote.REMOTE_ADD + "\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_remote_devices.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.storage.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(String device : _remote_devices) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print(device);
		                    	_xhtml_out.println("</td>");
		                    	_xhtml_out.print("<td>");
		                    	_xhtml_out.print("<a href=\"/admin/BackupStorageRemote?type=");
		                    	_xhtml_out.print(BackupStorageRemote.REMOTE_EDIT);
		                    	_xhtml_out.print("&storage=");
		                    	_xhtml_out.print(device);
		                    	_xhtml_out.print("\"><img src=\"/images/brick_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/BackupStorageRemote?type=");
								_xhtml_out.print(BackupStorageRemote.REMOTE_DELETE);
								_xhtml_out.print("&storage=");
								_xhtml_out.print(device);
								_xhtml_out.print("\"><img src=\"/images/brick_delete_16.png\" title=\"");
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
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_storages"));
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
	    		case DISK_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.name"));
		    			}
		    			StringBuilder address=new StringBuilder();
		    			if (getLanguageMessage("backup.storage.all").equals(request.getParameter("netInterface"))){
		    				address.append("wbsairback");
		    			}else{
		    				String[] _interfaceAdd=_nm.getAddress(request.getParameter("netInterface"));
		    				for (int x=0;_interfaceAdd.length>x;x++){
		    					address.append((x==0 ? "" : ".")+_interfaceAdd[x]);
		    				}
		    			}
		    			
		    			boolean aligned = false;
		    			if (request.getParameter("aligned")!=null){
		    				aligned = true;
		    			}
		    			
		    			Integer paralelJobs = 0;
		    			if (request.getParameter("paralelJobs")!=null && !request.getParameter("paralelJobs").isEmpty()){
		    				paralelJobs = Integer.parseInt(request.getParameter("paralelJobs"));
		    			}
		    			
		    			if(request.getParameter("lv") != null && !request.getParameter("lv").isEmpty()) {
		    				String[] _volume = request.getParameter("lv").split("/");
			    			if(_volume.length != 2) {
			    				throw new Exception(getLanguageMessage("backup.storage.exception.logical_volume"));
			    			}
			    			if(request.getParameter("modify") != null && "update".equalsIgnoreCase(request.getParameter("modify"))) {
			    				StorageManager.updateLogicalVolumeDevice(request.getParameter("name"), _volume[_volume.length - 2], _volume[_volume.length - 1],address.toString(), aligned, paralelJobs, false);
			    			} else {
			    				StorageManager.addLogicalVolumeDevice(request.getParameter("name"), _volume[_volume.length - 2], _volume[_volume.length - 1],address.toString(), aligned, paralelJobs);
			    			}
		    			} else if(request.getParameter("share") != null && !request.getParameter("share").isEmpty()) {
		    				if(!request.getParameter("share").contains("@")) {
			    				throw new Exception(getLanguageMessage("backup.storage.exception.external_share"));
			    			}
		    				String[] _share = request.getParameter("share").split("@");
			    			if(_share.length != 2) {
			    				throw new Exception(getLanguageMessage("backup.storage.exception.external_share"));
			    			}
			    			if(request.getParameter("modify") != null && "update".equalsIgnoreCase(request.getParameter("modify"))) {
			    				StorageManager.updateExternalShareDevice(request.getParameter("name"), _share[_share.length - 2], _share[_share.length - 1],address.toString(), paralelJobs, false);
			    			} else {
			    				StorageManager.addExternalShareDevice(request.getParameter("name"), _share[_share.length - 2], _share[_share.length - 1],address.toString(), paralelJobs);
			    			}
			    			
		    			} else {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.specify_share"));
		    			}
		    			response.sendRedirect("/admin/BackupStorageDisk");
		    			this.redirected=true;
	    			}
	    			break;
	    		
	    		case DISK_EDIT: {
		    			
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.repository"));
		    			}
	    		}
	    		case DISK_ADD: {
		    			
	    				_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    			
		    			Map<String, String> _device = StorageManager.getDiskVolumeDevice(request.getParameter("storage"));
		    			List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
		    			List<String> _shares = ShareManager.getExternalShareNames();
		    			
		    			writeDocumentBack("/admin/BackupStorageDisk");
		    			_xhtml_out.println("<form action=\"/admin/BackupStorageDisk\" name=\"storage\" id=\"storage\" method=\"post\">");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + DISK_SAVE + "\"/>");
	                    
	                    if (_device != null && !_device.isEmpty()) {
	                    	_xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("storage") + "\"/>");
	                    	_xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"update\"/>");
	                    }
		    			_xhtml_out.print("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.jobs.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						if (_device != null && !_device.isEmpty())
							_xhtml_out.println(getLanguageMessage("backup.storage.disk.edit_storage")+" "+request.getParameter("storage"));
						else
							_xhtml_out.println(getLanguageMessage("backup.storage.disk.new_storage"));
						_xhtml_out.print("<a href=\"javascript:if ($('#storage').validationEngine('validate')) submitForm(document.storage.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	if (_device != null && !_device.isEmpty()) { 
	        	    		_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    		_xhtml_out.print(request.getParameter("storage"));
	        	    		_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	} else {
	        	    		_xhtml_out.println("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" type=\"text\" id=\"name\" name=\"name\"/>");
	        	    		_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	}
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\" id=\"lvDiv\">");
	        	    	_xhtml_out.print("<label for=\"lv\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.lv"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"validate[groupRequired[device]] form_select\" name=\"lv\" id=\"lv\" onChange=\"changeLv()\">");
	        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(Map<String, String> _lv : _lvs) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.print("\"");
							if(_device != null && !_device.isEmpty() && "LV".equals(_device.get("type")) && _device.get("device").equals(_lv.get("vg") + "/" + _lv.get("name"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\" id=\"shareDiv\">");
	        	    	_xhtml_out.print("<label for=\"share\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.external_share"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"validate[groupRequired[device]] form_select\" name=\"share\" id=\"share\" onChange=\"changeShare()\">");
	        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String share : _shares) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(share);
							_xhtml_out.print("\"");
							if (_device != null && !_device.isEmpty()) {
								String tmp = _device.get("device");
								if (!tmp.endsWith("/") && share.endsWith("/"))
									tmp+="/";
								if("EXT".equals(_device.get("type")) && tmp.equals(share)) {
									_xhtml_out.print(" selected=\"selected\"");
								}
							}
							_xhtml_out.print(">");
							_xhtml_out.print(share);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"netInterface\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.interface"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"netInterface\">");
	        	    	boolean _selected=false;
						for(String _interface : _netInterfaces) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_interface);
							_xhtml_out.print("\"");
							StringBuilder ip=new StringBuilder();
							if (_nm.getAddress(_interface).length==4){
								ip.append(_nm.getAddress(_interface)[0]+"."+_nm.getAddress(_interface)[1]+"."+_nm.getAddress(_interface)[2]+"."+_nm.getAddress(_interface)[3]);
							}
							if (_device != null && !_device.isEmpty() && _device.get("address").equals(ip.toString()))
							{
								_xhtml_out.print(" selected=\"selected\"");
								_selected=true;
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_interface);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.print("<option value=\""+getLanguageMessage("backup.storage.all"));						
						if (_device != null && !_device.isEmpty() && !_selected)
						{
							_xhtml_out.print("\" selected=\"selected\">"+getLanguageMessage("backup.storage.all"));
						} 
						else{
							_xhtml_out.print("\">"+getLanguageMessage("backup.storage.all"));
						}						
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"paralelJobs\">");
	        	    	_xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("backup.storage.disk.paralelJobs.info")));
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.disk.paralelJobs"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input type=\"text\" class=\"validate[custom[integer],min[2],max[50]] form_text\" name=\"paralelJobs\" id=\"paralelJobs\" style=\"width:40px;\" value=\"");
	        	    	String paralelJobs = "";
	        	    	if (_device != null && !_device.isEmpty() && _device.get("paralelJobs") != null && !_device.get("paralelJobs").isEmpty())
	        	    		paralelJobs = _device.get("paralelJobs");
	        	    	_xhtml_out.print(paralelJobs);
	        	    	_xhtml_out.print("\" />");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\" id=\"alignedDiv\">");
	        	    	_xhtml_out.print("<label for=\"aligned\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.disk.aligned"));
	        	    	_xhtml_out.println(": </label>");
	        	    	boolean aligned = false;
	        	    	if (_device != null && !_device.isEmpty() && _device.get("deviceType") != null && _device.get("deviceType").equals("Aligned"))
	        	    		aligned = true;
	        	    	_xhtml_out.println("<input type=\"checkbox\" class=\"form_checkbox\" name=\"aligned\" id=\"aligned\" onClick=\"changeAligned()\"");
	        	    	if (aligned)
	        	    		_xhtml_out.println("checked=\"checked\"");
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    _xhtml_out.println("</form>");
		        	    
	                    pageJSFuncs+=" function changeLv() {";
	                    pageJSFuncs+=" 	var lv = $('#lv').val();";
	                    pageJSFuncs+=" 	if (lv != '') {";
	                    pageJSFuncs+=" 		$('#share').attr('disabled', 'disabled');\n";
	                    pageJSFuncs+=" 		$('#shareDiv').css('display','none');\n";
	                    pageJSFuncs+=" 		$('#aligned').removeAttr('disabled');\n";
	                    pageJSFuncs+=" 		$('#alignedDiv').css('display','');\n";
	                    pageJSFuncs+="    } else {";
	                    pageJSFuncs+=" 		$('#share').removeAttr('disabled');\n";
	                    pageJSFuncs+=" 		$('#shareDiv').css('display','');\n";
	                    pageJSFuncs+="  }";
	                    pageJSFuncs+=" }";
	                    pageJSFuncs+=" function changeShare() {";
	                    pageJSFuncs+=" 	var share = $('#share').val();";
	                    pageJSFuncs+=" 	if (share != '') {";
	                    pageJSFuncs+=" 		$('#lv').attr('disabled', 'disabled');\n";
	                    pageJSFuncs+=" 		$('#lvDiv').css('display','none');\n";
	                    pageJSFuncs+=" 		$('#aligned').attr('checked', false);\n";
	                    pageJSFuncs+=" 		$('#aligned').attr('disabled', 'disabled');\n";
	                    pageJSFuncs+=" 		$('#alignedDiv').css('display','none');\n";
	                    pageJSFuncs+="    } else {";
	                    pageJSFuncs+=" 		$('#lv').removeAttr('disabled');\n";
	                    pageJSFuncs+=" 		$('#lvDiv').css('display','');\n";
	                    pageJSFuncs+=" 		$('#aligned').removeAttr('disabled');\n";
	                    pageJSFuncs+=" 		$('#alignedDiv').css('display','');\n";
	                    pageJSFuncs+="  }";
	                    pageJSFuncs+=" }";
	    				pageJS+="changeLv();\n";
	    				pageJS+="changeShare();\n";
		    		}
	    			break;
	    		case DISK_DELETE: {
		    			if(request.getParameter("confirm") != null) {
		    				if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
			    				throw new Exception(getLanguageMessage("backup.storage.exception.device"));
			    			}
			    			_sm.removeVolumeDevice(request.getParameter("storage"));
			    			writeDocumentResponse(getLanguageMessage("backup.message.storage.removed"), "/admin/BackupStorageDisk");
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("backup.message.storage.remove.question"), "/admin/BackupStorageDisk?type=" + DISK_DELETE + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case SCSI_RESCAN: {
	    			if(request.getParameter("confirm") != null) {
		    			//SCSIManager.rescanSCSIBus();
		    			writeDocumentResponse(getLanguageMessage("backup.message.storage.scsi.rescan.launched"), "/admin/BackupStorageDisk");
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("backup.message.storage.scsi.rescan.question"), "/admin/BackupStorageDisk?type=" + SCSI_RESCAN + "&confirm=true", null);
	    			}
    			}
    			break;
	    	}
 		} catch(Exception _ex) {
 			switch (type) {
 			case DISK_DELETE:
 					writeDocumentError(_ex.getMessage(), "/admin/BackupStorageDisk");
 				break;
 			default:
 					writeDocumentError(_ex.getMessage());
 				break;
			}
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}
	
	