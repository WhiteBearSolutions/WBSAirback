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
import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.util.Configuration;

public class BackupStorageLibrary extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int LIBRARY_ADD = 2;
	public final static int LIBRARY_SAVE = 3;
	public final static int LIBRARY_EDIT = 4;
	public final static int LIBRARY_DELETE = 5;
	private int type;
	public final static String baseUrl = "/admin/"+BackupStorageLibrary.class.getSimpleName();
	
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
		    StorageManager _sm = new StorageManager(_c);
			NetworkManager _nm = this.sessionManager.getNetworkManager();
			List<String> _netInterfaces=_nm.getConfiguredInterfaces();
		    switch(this.type) {
	    		default: {
	    				response.sendRedirect("/admin/BackupStorageDisk");
	    				this.redirected=true;
	    			}
        		    break;
	    		case LIBRARY_ADD: {
	    				writeDocumentHeader();
		    			
		    			int _drives = 1;
		    			if(request.getParameter("drives") != null) {
	                    	try {
	                    		_drives = Integer.parseInt(request.getParameter("drives"));
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
		    			List<Map<String, String>> _autochangers = LibraryManager.getAutochangers();
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
		    			_xhtml_out.println("<form action=\"/admin/BackupStorageLibrary\" name=\"storage\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" id=\"type\" name=\"type\" value=\"" + LIBRARY_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"no\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" id=\"drives\" name=\"drives\" value=\""+_drives+"\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.autochanger.new_storage"));
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
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"name\" id=\"name\" value=\"");
	        	    	_xhtml_out.println(request.getParameter("name")!=null ? request.getParameter("name") : "");
	        	    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"changer1\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.autochanger"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"changer1\">");
						for(Map<String, String> _autochanger : _autochangers) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_autochanger.get("device"));
							_xhtml_out.print("\"");
							_xhtml_out.println(request.getParameter("changer1")!=null ? (request.getParameter("changer1").equals(_autochanger.get("device")) ? "selected=\"true\"" : "") : "");
							_xhtml_out.print(">");
							_xhtml_out.print(_autochanger.get("vendor"));
							_xhtml_out.print(" / ");
							_xhtml_out.print(_autochanger.get("model"));
							_xhtml_out.print(" (");
							_xhtml_out.print(_autochanger.get("serial"));
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
	        	    	_xhtml_out.println("<option value=\"LTO1\"");
	        	    	_xhtml_out.println(request.getParameter("mediatype")!=null ? ("LTO1".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("LTO1</option>");
						_xhtml_out.println("<option value=\"LTO2\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("LTO2".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("LTO2</option>");
						_xhtml_out.println("<option value=\"LTO3\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("LTO3".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("LTO3</option>");
						_xhtml_out.println("<option value=\"LTO4\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("LTO4".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("LTO4</option>");
						_xhtml_out.println("<option value=\"DLT\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("DLT".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("DLT</option>");
						_xhtml_out.println("<option value=\"SDLT\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("SDLT".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("SDLT</option>");
						_xhtml_out.println("<option value=\"DDS1\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("DDS1".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("DDS1</option>");
						_xhtml_out.println("<option value=\"DDS2\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("DDS2".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("DDS2</option>");
						_xhtml_out.println("<option value=\"DDS3\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("DDS3".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("DDS3</option>");
						_xhtml_out.println("<option value=\"DDS4\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("DDS4".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("DDS4</option>");
						_xhtml_out.println("<option value=\"DAT72\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("DAT72".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("DAT72</option>");
						_xhtml_out.println("<option value=\"DAT160\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("DAT160".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("DAT160</option>");
						_xhtml_out.println("<option value=\"TR\"");
						_xhtml_out.println(request.getParameter("mediatype")!=null ? ("TR".equals(request.getParameter("mediatype")) ? "selected=\"true\"" : "") : "");
						_xhtml_out.print(">");
						_xhtml_out.println("TR</option>");
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"spool\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_spool"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"spool\">");
	        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(Map<String, String> _lv : _lvs) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.print("\"");
							_xhtml_out.println(request.getParameter("spool")!=null ? (request.getParameter("spool").equals(_lv.get("vg")+"/"+_lv.get("name")) ? "selected=\"true\"" : "") : "");
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
	        	    	_xhtml_out.print("<label for=\"spool_size\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_spool_size"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"network_octet\" type=\"text\" name=\"spool_size\" value=\"");
	        	    	_xhtml_out.println(request.getParameter("spool_size")!=null ? request.getParameter("spool_size") : "");
	        	    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"units\">");
	        	    	_xhtml_out.println("<option value=\"K\">KB</option>");
	        	    	_xhtml_out.println("<option selected=\"selected\" value=\"M\">MB</option>");
	        	    	_xhtml_out.println("<option value=\"G\">GB</option>");
	        	    	_xhtml_out.println("<option value=\"T\">TB</option>");
	        	    	_xhtml_out.println("</select>");
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
							_xhtml_out.print("\"");
							_xhtml_out.println(request.getParameter("netInterface")!=null ? (request.getParameter("netInterface").equals(_interface) ? "selected=\"true\"" : "") : "");
							_xhtml_out.print(">");
							_xhtml_out.print(_interface);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    
		        	    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.autochanger.drives"));
						_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
				
						_xhtml_out.print("<a href=\"javascript:document.getElementById('drives').value="+(_drives+1)+";document.getElementById('type').value="+LIBRARY_ADD+";submitForm(document.storage.submit());\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    for(int i = 0; i < _drives; i++) {
	                    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"drive\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<select class=\"form_select\" name=\"drive");
		        	    	_xhtml_out.print(i);
        	    			_xhtml_out.println("\">");
        	    			if(i > 0) {
	    	    				_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	    	    			}
							for(Map<String, String> tape : _tapes) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(tape.get("device"));
								_xhtml_out.print("\"");
								_xhtml_out.println(request.getParameter("drive"+i)!=null ? (request.getParameter("drive"+i).equals(tape.get("device")) ? "selected=\"true\"" : "") : "");
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
							if(i == 0) {
								_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
							}
		        	    	_xhtml_out.println("</div>");
	                    }
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    _xhtml_out.println("</form>");
					}
	    			break;
	    		case LIBRARY_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.name"));
		    			}
		    			if(request.getParameter("changer1") == null || request.getParameter("changer1").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.changer"));
		    			}
		    			if(request.getParameter("drive0") == null || request.getParameter("drive0").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.first_drive"));
		    			}
		    			StringBuilder address=new StringBuilder();
		    			if (getLanguageMessage("backup.storage.all").equals(request.getParameter("netInterface"))){
		    				address.append("wbsairback");
		    			} else {
		    				String[] _interfaceAdd = _nm.getAddress(request.getParameter("netInterface"));
		    				for (int x=0; _interfaceAdd.length>x; x++){
		    					address.append((x==0 ? "" : ".")+_interfaceAdd[x]);
		    				}
		    			}
		    			int volume_size = 0;
		    			List<String> devices = new ArrayList<String>();
		    			String[] _volume = request.getParameter("spool").split("/");
		    			if(_volume.length < 2) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.logical_volume"));
		    			}
		    			
		    			for(int i = 0; request.getParameter("drive" + i) != null && !request.getParameter("drive" + i).isEmpty(); i++) {
		    				if (!devices.contains(request.getParameter("drive" + i))){
		    					devices.add(request.getParameter("drive" + i));
		    				}
		    			}
		    			
		    			if(request.getParameter("spool_size") != null && !request.getParameter("spool_size").isEmpty()) {
		    				try {
		    					volume_size = Integer.parseInt(request.getParameter("spool_size"));
		    				} catch(NumberFormatException _ex) {}
		    			}
		    			
		    			String units = "M";
		    			if(request.getParameter("units") != null && !request.getParameter("units").isEmpty()) {
		    				units = request.getParameter("units");
		    			}
		    			
		    			if(request.getParameter("modify") == null || request.getParameter("modify").contains("no")){
		    				StorageManager.addAutochangerDevice(request.getParameter("name"), devices, request.getParameter("changer1"), request.getParameter("mediatype"), _volume[_volume.length - 2], _volume[_volume.length - 1], volume_size, units, address.toString());
		    			} else {
		    				StorageManager.updateAutochangerDevice(request.getParameter("name"), devices, request.getParameter("changer1"), request.getParameter("mediatype"), _volume[_volume.length - 2], _volume[_volume.length - 1], volume_size, units, address.toString());
		    			}
		    			
			    		response.sendRedirect("/admin/BackupStorageLibrary");
			    		this.redirected=true;
		    		}
	    			break;
	    		case LIBRARY_EDIT: {
	    				writeDocumentHeader();
		    			
		    			int _drives = 1;
		    			if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.repository"));
		    			}
		    			Map<String, String> autochanger = StorageManager.getAutochangerDevice(request.getParameter("storage"));
	    				List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
		    			List<Map<String, String>> _autochangers = LibraryManager.getAutochangers();
		    			List<Map<String, String>> _tapes = TapeManager.getTapeDevices();
		    			List<String> _listUsedTapes=new ArrayList<String>();
		    			List<Map<String, String>> _used_autochangersTapes = StorageManager.getAutochangerDevices();
		    			if (_used_autochangersTapes!=null){
			    			for (Map<String,String> _autoChangersUsed: _used_autochangersTapes){
			    				for (int x=0;true; x++){
			    					if (_autoChangersUsed.get("drive"+x)!=null && !_autoChangersUsed.get("name").equals(request.getParameter("storage"))){
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
		    			
		    			if(request.getParameter("drives") != null) {
	                    	try {
	                    		_drives = Integer.parseInt(request.getParameter("drives"));	                    		
	                    	} catch(NumberFormatException _ex) {
	                    		
	                    	}
		    			}else{
		    				 for(int i = 0; autochanger.containsKey("drive" + i); i++) {
	                    		 _drives=i+1;
                    		 }
		    			}
		    			
		    			writeDocumentBack("/admin/BackupStorageDisk");
		    			_xhtml_out.println("<form action=\"/admin/BackupStorageLibrary\" name=\"storage\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" id=\"type\" name=\"type\" value=\"" + LIBRARY_SAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" id=\"modify\" name=\"modify\" value=\"yes\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" id=\"name\" name=\"name\" value=\"" + request.getParameter("storage") + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" id=\"storage\" name=\"storage\" value=\"" + request.getParameter("storage") + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" id=\"drives\" name=\"drives\" value=\""+_drives+"\"/>");
	                    _xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/brick_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("backup.storage"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.storage.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.autochanger.edit_storage")+(request.getParameter("name")!=null ? request.getParameter("name") :autochanger.get("name")));
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
	        	    	_xhtml_out.print("<label for=\"_name\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    	_xhtml_out.print(request.getParameter("name")!=null ? request.getParameter("name") :autochanger.get("name"));
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"changer1\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.autochanger"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"changer1\">");
	        	    	String changer=request.getParameter("changer1")!=null ? request.getParameter("changer1") : LibraryManager.getAutochangerDeviceById(autochanger.get("device"));
						for(Map<String, String> _autochanger : _autochangers) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_autochanger.get("device"));
							_xhtml_out.print("\"");
							if(_autochanger.get("device").equalsIgnoreCase(changer)) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_autochanger.get("vendor"));
							_xhtml_out.print(" / ");
							_xhtml_out.print(_autochanger.get("model"));
							_xhtml_out.print(" (");
							_xhtml_out.print(_autochanger.get("serial"));
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
	        	    	String mediatype=request.getParameter("mediatype")!=null ? request.getParameter("mediatype") : autochanger.get("type");
	        	    	_xhtml_out.print("<option value=\"LTO1\"");
						if("LTO1".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO1</option>");
						_xhtml_out.print("<option value=\"LTO2\"");
						if("LTO2".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO2</option>");
						_xhtml_out.print("<option value=\"LTO3\"");
						if("LTO3".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO3</option>");
						_xhtml_out.print("<option value=\"LTO4\"");
						if("LTO4".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">LTO4</option>");
						_xhtml_out.print("<option value=\"DLT\"");
						if("DLT".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DLT</option>");
						_xhtml_out.print("<option value=\"SDLT\"");
						if("SDLT".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">SDLT</option>");
						_xhtml_out.print("<option value=\"DDS1\"");
						if("DDS1".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS1</option>");
						_xhtml_out.print("<option value=\"DDS2\"");
						if("DDS2".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS2</option>");
						_xhtml_out.print("<option value=\"DDS3\"");
						if("DDS3".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS3</option>");
						_xhtml_out.print("<option value=\"DDS4\"");
						if("DDS4".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DDS4</option>");
						_xhtml_out.print("<option value=\"DAT72\"");
						if("DAT72".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DAT72</option>");
						_xhtml_out.print("<option value=\"DAT160\"");
						if("DAT160".equalsIgnoreCase(mediatype)) {
							_xhtml_out.print(" selected=\"selected\"");
						}
						_xhtml_out.println(">DAT160</option>");
						_xhtml_out.println("<option value=\"TR\"");
						if("TR".equalsIgnoreCase(mediatype)) {
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
	        	    	String spool=request.getParameter("spool")!=null ? request.getParameter("spool") : autochanger.get("spool");
	        	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	for(Map<String, String> _lv : _lvs) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_lv.get("vg"));
							_xhtml_out.print("/");
							_xhtml_out.print(_lv.get("name"));
							_xhtml_out.print("\"");
							if(spool.equalsIgnoreCase(_lv.get("vg") + "/" + _lv.get("name"))) {
								_xhtml_out.print(" selected=\"selected\"");
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
	        	    	_xhtml_out.print("<label for=\"spool_size\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape_spool_size"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"spool_size\"");
	        	    	String units = "M";
	        	    	if(autochanger.get("spool-size") != null) {
	        	    		String spool_size = request.getParameter("spool_size")!=null ? request.getParameter("spool_size") : autochanger.get("spool-size");
	        	    		Integer _size = Integer.parseInt(spool_size);
	        	    		if (_size >= 1048576) {
	        	    			_size = Math.round(((_size/1048576)*100)/100);
								units = "T";
	        	    		} else if(_size >= 1024) {
								_size = Math.round(((_size/1024)*100)/100);
								units = "G";
							} else {
								_size = Math.round((_size*100)/100);
								units = "M";
							} 
		        	    	_xhtml_out.print(" value=\"");
		        	    	
		        	    	_xhtml_out.print(_size);
		        	    	_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"units\">");
	        	    	_xhtml_out.println("<option value=\"K\"");
	        	    	if (units.equals("K"))
	        	    		_xhtml_out.println(" selected=\"selected\" ");
	        	    	_xhtml_out.println(">KB</option>");
	        	    	_xhtml_out.println("<option value=\"M\"");
	        	    	if (units.equals("M"))
	        	    		_xhtml_out.println(" selected=\"selected\" ");
	        	    	_xhtml_out.println(">MB</option>");
	        	    	_xhtml_out.println("<option value=\"G\"");
	        	    	if (units.equals("G"))
	        	    		_xhtml_out.println(" selected=\"selected\" ");
	        	    	_xhtml_out.println(">GB</option>");
	        	    	_xhtml_out.println("<option value=\"T\"");
	        	    	if (units.equals("T"))
	        	    		_xhtml_out.println(" selected=\"selected\" ");
	        	    	_xhtml_out.println(">TB</option>");
	        	    	_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"netInterface\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.network.interface"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"netInterface\">");
	        	    	String ipAux=request.getParameter("netInterface")!=null ? getIpNetInterface(request.getParameter("netInterface"),this.sessionManager.getNetworkManager()) : autochanger.get("address");
	        	    	boolean _selected=false;
						for(String _interface : _netInterfaces) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(_interface);
							_xhtml_out.print("\"");
							StringBuilder ip = new StringBuilder();
							if(_nm.getAddress(_interface).length == 4) {
								ip.append(_nm.getAddress(_interface)[0]+"."+_nm.getAddress(_interface)[1]+"."+_nm.getAddress(_interface)[2]+"."+_nm.getAddress(_interface)[3]);
							}
							if(ipAux.equals(ip.toString())) {
								_xhtml_out.print(" selected=\"selected\"");
								_selected=true;
							}
							_xhtml_out.print(">");
							_xhtml_out.print(_interface);
							_xhtml_out.println("</option>");
						}
						_xhtml_out.print("<option value=\"");
						_xhtml_out.print(getLanguageMessage("backup.storage.all"));
						if(!_selected) {
							_xhtml_out.print("\" selected=\"selected\">"+getLanguageMessage("backup.storage.all"));
						} else {
							_xhtml_out.print("\">");
							_xhtml_out.print(getLanguageMessage("backup.storage.all"));
						}						
						_xhtml_out.println("</option>");
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    
		        	    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.println(getLanguageMessage("backup.storage.autochanger.drives"));
			         	_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");
			
						_xhtml_out.print("<a href=\"javascript:document.getElementById('drives').value="+(_drives+1)+";document.getElementById('type').value="+LIBRARY_EDIT+";submitForm(document.storage.submit());\"><img src=\"/images/add_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");
			
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    for(int i = 0; autochanger.containsKey("drive" + i) || i < _drives; i++) {
	                    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"drive\">");
		        	    	_xhtml_out.print(getLanguageMessage("backup.storage.tape"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<select class=\"form_select\" name=\"drive");
		        	    	_xhtml_out.print(i);
	    	    			_xhtml_out.println("\">");
	    	    			if(i > 0) {
	    	    				_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	    	    			}
							for(Map<String, String> tape : _tapes) {
								_xhtml_out.print("<option value=\"");
								_xhtml_out.print(tape.get("device"));
								_xhtml_out.print("\"");
								if(request.getParameter("drive"+i)!=null ? tape.get("device").equals(request.getParameter("drive"+i)) : (autochanger.containsKey("drive" + i) && tape.get("device").equals(TapeManager.getTapeDevicePath(autochanger.get("drive" + i))))) {
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
							if(i == 0) {
								_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
							}
		        	    	_xhtml_out.println("</div>");
	                    }
	                	_xhtml_out.println("</fieldset>"); 
		                _xhtml_out.println("<div class=\"clear\"/></div>");
		        	    _xhtml_out.print("</div>");
		        	    _xhtml_out.println("</form>");
					}
	    			break;
	    		case LIBRARY_DELETE: {
	    			if(request.getParameter("confirm") != null) {
	    				if(request.getParameter("storage") == null || request.getParameter("storage").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.device"));
		    			}
		    			_sm.removeAutochangerDevice(request.getParameter("storage"));
		    			response.sendRedirect("/admin/BackupStorageLibrary");
		    			this.redirected=true;
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("backup.message.storage.remove.question"), "/admin/BackupStorageLibrary?type=" + LIBRARY_DELETE + "&storage=" + request.getParameter("storage") + "&confirm=true", null);
	    			}
	    		}
	    			break;
	    	}
 		} catch (Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
	
	private String getIpNetInterface(String netInterface,NetworkManager _nm){
		StringBuilder address=new StringBuilder();
		String[] _interfaceAdd = _nm.getAddress(netInterface);
		for (int x=0; _interfaceAdd.length>x; x++){
			address.append((x==0 ? "" : ".")+_interfaceAdd[x]);
		}
		return address.toString();
	}
}