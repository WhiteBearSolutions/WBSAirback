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
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.util.Configuration;

public class BackupStorageTape extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int TAPE_ADD = 2;
	public final static int TAPE_SAVE = 3;
	public final static int TAPE_EDIT = 4;
	public final static int TAPE_DELETE = 5;
	private int type;
	public final static String baseUrl = "/admin/"+BackupStorageTape.class.getSimpleName();
	
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
		    Configuration _c = this.sessionManager.getConfiguration();
		    TapeManager _tm = new TapeManager(_c);
			NetworkManager _nm = this.sessionManager.getNetworkManager();
			List<String> _netInterfaces=_nm.getConfiguredInterfaces();
			switch(this.type) {
	    		default: {
	    				response.sendRedirect("/admin/BackupStorageTape");
	    				this.redirected=true;
	    			}
        		    break;
	    		case TAPE_ADD: {
	    				writeDocumentHeader();
		    			
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
		    			List<Map<String, String>> _tapes = TapeManager.getTapeDevices();
		    			List<String> _listUsedTapes=new ArrayList<String>();
	    				List<Map<String, String>> _used_autochangersTapes = StorageManager.getAutochangerDevices();
		    			if (_used_autochangersTapes!=null){
			    			for (Map<String,String> _autoChangersUsed: _used_autochangersTapes){
			    				for (int x=0;true; x++){
			    					if (_autoChangersUsed.get("drive"+x)!=null){
			    						if (!_listUsedTapes.contains(_autoChangersUsed.get("drive"+x))){
			    							_listUsedTapes.add(_autoChangersUsed.get("drive"+x));
			    						}
			    					}else{
			    						break;
			    					}
			    				}
			    			}
		    			}
		    			List<Map<String, String>> _usedTapes = StorageManager.getTapeDevices();
		    			if (_usedTapes!=null){
			    			for (Map<String,String> _tapUsed: _usedTapes){
			    				String aux=_tapUsed.get("device").substring(_tapUsed.get("device").lastIndexOf("/")+1,_tapUsed.get("device").length());
			    				if (!_listUsedTapes.contains(aux)) {
			    					_listUsedTapes.add(aux);
			    				}
			    			}
		    			}
		    			List<Map<String, String>> _tapesAux = new ArrayList<Map<String,String>>();
		    			for (Map<String,String> _tap: _tapes){
		    				if (!_listUsedTapes.contains(_tap.get("description"))) {
		    					_tapesAux.add(_tap);
		    				}
		    			}
		    			_tapes=_tapesAux;
	    				
	    				writeDocumentBack("/admin/BackupStorageDisk");
		    			_xhtml_out.println("<form action=\"/admin/BackupStorageTape\" name=\"storage\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + TAPE_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"no\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.tape.new_storage"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.storage.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"drive\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"drive\">");
						for(Map<String, String> tape :_tapes) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(tape.get("device"));
							_xhtml_out.print("\">");
							_xhtml_out.print(tape.get("vendor"));
							_xhtml_out.print(" / ");
							_xhtml_out.print(tape.get("model"));
							_xhtml_out.print(" (");
							_xhtml_out.print(tape.get("serial"));
							_xhtml_out.print(")");
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mediatype\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"mediatype\">");
	        	    	_xhtml_out.println("<option value=\"LTO1\">LTO1</option>");
						_xhtml_out.println("<option value=\"LTO2\">LTO2</option>");
						_xhtml_out.println("<option value=\"LTO3\">LTO3</option>");
						_xhtml_out.println("<option value=\"LTO4\">LTO4</option>");
						_xhtml_out.println("<option value=\"DLT\">DLT</option>");
						_xhtml_out.println("<option value=\"SDLT\">SDLT</option>");
						_xhtml_out.println("<option value=\"DDS1\">DDS1</option>");
						_xhtml_out.println("<option value=\"DDS2\">DDS2</option>");
						_xhtml_out.println("<option value=\"DDS3\">DDS3</option>");
						_xhtml_out.println("<option value=\"DDS4\">DDS4</option>");
						_xhtml_out.println("<option value=\"DAT72\">DAT72</option>");
						_xhtml_out.println("<option value=\"DAT160\">DAT160</option>");
						_xhtml_out.println("<option value=\"TR\">TR</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"spool\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_spool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"spool\">");
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
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"spool_size\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_spool_size"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"spool_size\"/>");
	        	    	_xhtml_out.println("MB");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"auto_mount\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_auto_mount"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input type=\"checkbox\" class=\"form_checkbox\" name=\"auto_mount\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"netInterface\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.interface"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"netInterface\">");
	        	    	_netInterfaces.add(getLanguageMessage("backup.storage.all"));
						for(String _interface : _netInterfaces) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_interface);
							_xhtml_out.print("\">");
							_xhtml_out.print(_interface);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    _xhtml_out.println("</form>");
					}
	    			break;
	    		case TAPE_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.storage_name"));
		    			}
		    			if(request.getParameter("spool") == null || request.getParameter("spool").isEmpty()) {
	    					throw new Exception(getLanguageMessage("backup.storage.exception.storage_volume"));
	    				}
	    				String[] _volume = request.getParameter("spool").split("/");
		    			if(_volume.length < 2) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.storage_volume"));
		    			}
		    			StringBuilder address=new StringBuilder();
		    			if(getLanguageMessage("backup.storage.all").equals(request.getParameter("netInterface"))){
		    				address.append("wbsairback");
		    			} else {
		    				String[] _interfaceAdd=_nm.getAddress(request.getParameter("netInterface"));
		    				for (int x=0;_interfaceAdd.length>x;x++){
		    					address.append((x==0 ? "" : ".")+_interfaceAdd[x]);
		    				}
		    			}
		    			int volume_size = 0;
		    			if(request.getParameter("spool_size") != null && !request.getParameter("spool_size").isEmpty()) {
		    				try {
		    					volume_size = Integer.parseInt(request.getParameter("spool_size"));
		    				} catch(NumberFormatException _ex) {}
		    			}
		    			
		    			boolean auto_mount = false;
		    			if (request.getParameter("auto_mount")!=null){
		    				auto_mount = true;
		    			}
		    			
		    			if(request.getParameter("modify") == null || request.getParameter("modify").contains("no")){
		    				StorageManager.addTapeDevice(request.getParameter("name"), request.getParameter("drive"), request.getParameter("mediatype"), _volume[_volume.length - 2], _volume[_volume.length - 1], volume_size,address.toString(), auto_mount);
		    			} else {
		    				StorageManager.updateTapeDevice(request.getParameter("name"), request.getParameter("drive"), request.getParameter("mediatype"), _volume[_volume.length - 2], _volume[_volume.length - 1], volume_size,address.toString(), auto_mount);
		    			}
		    			
		    			response.sendRedirect("/admin/BackupStorageDisk");
		    			this.redirected=true;
		    		}
	    			break;
	    		case TAPE_EDIT: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.storage_name"));
		    			}
		    			Map<String, String> _tape = StorageManager.getTapeDevice(request.getParameter("storage"));
						List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
						List<Map<String, String>> _tapes = TapeManager.getTapeDevices();
		    			List<String> _listUsedTapes=new ArrayList<String>();
		    			List<Map<String, String>> _used_autochangersTapes = StorageManager.getAutochangerDevices();
		    			if (_used_autochangersTapes!=null){
			    			for (Map<String,String> _autoChangersUsed: _used_autochangersTapes){
			    				for (int x=0;true; x++){
			    					if (_autoChangersUsed.get("drive"+x)!=null){
			    						if (!_listUsedTapes.contains(_autoChangersUsed.get("drive"+x))){
			    							_listUsedTapes.add(_autoChangersUsed.get("drive"+x));
			    						}
			    					}else{
			    						break;
			    					}
			    				}
			    			}
		    			}
		    			List<Map<String, String>> _usedTapes = StorageManager.getTapeDevices();
		    			if (_usedTapes!=null){
			    			for (Map<String,String> _tapUsed: _usedTapes){
			    				String aux=_tapUsed.get("device").substring(_tapUsed.get("device").lastIndexOf("/")+1,_tapUsed.get("device").length());
			    				if (!_listUsedTapes.contains(aux)) {
			    					_listUsedTapes.add(aux);
			    				}
			    			}
		    			}
		    			List<Map<String, String>> _tapesAux = new ArrayList<Map<String,String>>();
		    			for (Map<String,String> _tap: _tapes){
		    				if (!_listUsedTapes.contains(_tap.get("description")) || _tap.get("device").equals(_tape.get("device"))) {
		    					_tapesAux.add(_tap);
		    				}
		    			}
		    			_tapes=_tapesAux;
						
						writeDocumentBack("/admin/BackupStorageDisk");
		    			_xhtml_out.println("<form action=\"/admin/BackupStorageTape\" name=\"storage\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + TAPE_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("storage") + "\"/>");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"yes\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.jobs.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.tape.edit_storage")+_tape.get("name"));
						_xhtml_out.print("<a href=\"javascript:submitForm(document.storage.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    	_xhtml_out.print(_tape.get("name"));
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"drive\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"drive\">");
						for(Map<String, String> tape : _tapes) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(tape.get("device"));
							_xhtml_out.print("\"");
							if(tape.get("device").equals(_tape.get("device"))) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(tape.get("vendor"));
							_xhtml_out.print(" / ");
							_xhtml_out.print(tape.get("model"));
							_xhtml_out.print(" (");
							_xhtml_out.print(tape.get("serial"));
							_xhtml_out.print(")");
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mediatype\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"mediatype\">");
	        	    	_xhtml_out.print("<option value=\"LTO1\"");
						if(_tape.get("type").equals("LTO1")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO1</option>");
						_xhtml_out.print("<option value=\"LTO2\"");
						if(_tape.get("type").equals("LTO2")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO2</option>");
						_xhtml_out.print("<option value=\"LTO3\"");
						if(_tape.get("type").equals("LTO3")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO3</option>");
						_xhtml_out.print("<option value=\"LTO4\"");
						if(_tape.get("type").equals("LTO4")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO4</option>");
						_xhtml_out.print("<option value=\"DLT\"");
						if(_tape.get("type").equals("DLT")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DLT</option>");
						_xhtml_out.print("<option value=\"SDLT\"");
						if(_tape.get("type").equals("SDLT")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">SDLT</option>");
						_xhtml_out.print("<option value=\"DDS1\"");
						if(_tape.get("type").equals("DDS1")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS1</option>");
						_xhtml_out.print("<option value=\"DDS2\"");
						if(_tape.get("type").equals("DDS2")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS2</option>");
						_xhtml_out.print("<option value=\"DDS3\"");
						if(_tape.get("type").equals("DDS3")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS3</option>");
						_xhtml_out.print("<option value=\"DDS4\"");
						if(_tape.get("type").equals("DDS4")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS4</option>");
						_xhtml_out.print("<option value=\"DAT72\"");
						if(_tape.get("type").equals("DAT72")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DAT72</option>");
						_xhtml_out.print("<option value=\"DAT160\"");
						if(_tape.get("type").equals("DAT160")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DAT160</option>");
						_xhtml_out.println("<option value=\"TR\"");
						if(_tape.get("type").equals("TR")) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">TR</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"spool\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_spool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"spool\">");
	        	    	for(Map<String, String> _lv : _lvs) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.print("\"");
							if(_tape.get("spool").equalsIgnoreCase(_lv.get("vg") + "/" + _lv.get("name"))) {
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
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"spool_size\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_spool_size"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"spool_size\"");
	        	    	if(_tape.get("spool-size") != null) {
		        	    	_xhtml_out.print(" value=\"");
		        	    	_xhtml_out.print(_tape.get("spool-size"));
		        	    	_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("MB");
	        	    	_xhtml_out.println("</div>");
	        	    	String auto_mount_checked = "";
	        	    	if (_tape.get("auto_mount").equals(String.valueOf(true)))
	        	    		auto_mount_checked = " checked=\"true\" ";
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"auto_mount\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_auto_mount"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input type=\"checkbox\" class=\"form_checkbox\" name=\"auto_mount\" "+auto_mount_checked+" />");
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
							if (_tape.get("address").equals(ip.toString()))
							{
								_xhtml_out.print(" selected=\"selected\"");
								_selected=true;
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_interface);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.print("<option value=\""+getLanguageMessage("backup.storage.all"));						
						if (!_selected)
						{
							_xhtml_out.print("\" selected=\"selected\">"+getLanguageMessage("backup.storage.all"));
						} 
						else{
							_xhtml_out.print("\">"+getLanguageMessage("backup.storage.all"));
						}						
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    _xhtml_out.println("</form>");
					}
	    			break;
	    		case TAPE_DELETE: {
	    			if(request.getParameter("confirm") != null) {
		    			_tm.removeTape(request.getParameter("storage"));
		    			response.sendRedirect("/admin/BackupStorageDisk");
		    			this.redirected=true;
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("backup.message.storage.remove.question"), "/admin/BackupStorageTape?type=" + TAPE_DELETE + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
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
	
	