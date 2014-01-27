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

import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.net.VTLManager;


public class VTLConfiguration extends WBSImagineServlet {
	static final long serialVersionUID = 2781204791L;
	public final static int NEW_TAPE = 2;
	public final static int NEW_LIBRARY = 3;	
	public final static int STORE_TAPE = 4;
	public final static int STORE_LIBRARY = 5;
	public final static int REMOVE_TAPE = 6;
	public final static int REMOVE_LIBRARY = 7;
	public final static int EDIT_TAPE = 8;
	public final static int NEW_DRIVE=9;
	public final static int ADD_TAPES=10;
	public final static int CHANGE_NUM_SLOT=11;
	public final static int REMOVE_DRIVE = 12;
	public final static int EDIT_LIBRARY= 13;
	private int type;
	public final static String baseUrl = "/admin/"+VTLConfiguration.class.getSimpleName();
	
	@SuppressWarnings("unchecked")
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	response.setContentType("text/html");
	    	
			this.type = 1;
			if(request.getParameter("type") != null && !request.getParameter("type").isEmpty()) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			if (type != REMOVE_LIBRARY)
				writeDocumentHeader();
			
	    	VTLManager _vtlm = new VTLManager();
	    	int _num_tapes = 0;
	    	String title="";
		    switch(this.type) {
	    		default: {
	    				int _offset = 0;	    				
	    				List<Map<String,Object>> _libraries = _vtlm.getLibraries();
	    				int _nextId = _vtlm.getNextId(_libraries);
	    				
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/tape_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.libraries"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("<a href=\"/admin/VTLConfiguration?type=");
	                    _xhtml_out.print(NEW_LIBRARY);
	                    _xhtml_out.print("&nextId=");
	                    _xhtml_out.print(_nextId);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset id=\"fieldset35Left\">");
	                    _xhtml_out.println("<table>");
	                    if(!_libraries.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.name"));
		                    _xhtml_out.println("</td>");
	                    	_xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.path"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.id"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String,Object> _destination : _libraries) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_destination.get("name"));
			                    _xhtml_out.println("</td>");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_destination.get("path"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_destination.get("id"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print("<a href=\"/admin/VTLConfiguration?type=");
			                    _xhtml_out.print(EDIT_LIBRARY);
			                    _xhtml_out.print("&id=");
			                    _xhtml_out.print(_destination.get("id"));
			                    _xhtml_out.print("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\"><img src=\"/images/tape_edit_16.png\"/></a>");
			                    _xhtml_out.print("<a href=\"/admin/VTLConfiguration?type=");
			                    _xhtml_out.print(REMOVE_LIBRARY);
			                    _xhtml_out.print("&id=");
			                    _xhtml_out.print(_destination.get("id"));
			                    _xhtml_out.print("&unitSerialNumber=");
			                    _xhtml_out.print(_destination.get("unitSerialNumber"));
			                    _xhtml_out.print("\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.remove"));
		                    	_xhtml_out.print("\"><img src=\"/images/tape_cross_16.png\"/></a>");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset++;
		                    }
	                    } else {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.logical_volumes.vtl.noLibraries"));
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
	    		case EDIT_LIBRARY : {	    			
		    			String _library_id = null;
		    			int _num_drives = 1;
		    			if(request.getParameter("id") == null && request.getParameter("id").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.vtl.specify_id"));
		    			}
		    			int _numLibraries=-1;
		    			if(request.getParameter("numLibraries") != null && !request.getParameter("numLibraries").isEmpty()) {
		    				_numLibraries=Integer.valueOf(request.getParameter("numLibraries"));
		    			}else{
		    				_numLibraries=_vtlm.getLibraries().size();	
		    			}
		    			_library_id = request.getParameter("id");
		    			Map<String, Object> _library = _vtlm.getLibrary(_library_id);
		    			List<Map<String, String>> _library_drives = null;
		    			if(_library.get("drives") != null) {
		    				_library_drives = (List<Map<String, String>>) _library.get("drives");
		    				_num_drives = _library_drives.size();
		    			}
		    			if(_library.get("numTapes") != null) {
		    				_num_tapes = Integer.valueOf((String)_library.get("numTapes"));
		    			}
		    			if(request.getParameter("numTapes") != null) {
		    				_num_tapes = Integer.parseInt(request.getParameter("numTapes"));
		    			}
		    			List<String> _slots = (List<String>) _library.get("slotTapes");
		    			List<String> _drives = new ArrayList<String>(32);
		    			_library.put("device", _vtlm.getLibraryChangerDevice(_library_id));
		    			_library.put("slotsNum", String.valueOf(_slots.size()));
		    			if(_library.get("device") != null && _library_drives != null) {
		    				for(Map<String, String> drive : _library_drives){
		    					if(drive.get("unitSerialNumber") != null) {
	    							_drives.add(drive.get("unitSerialNumber"));
	    						}
		    				}	    					
		    			}
		    			title=getLanguageMessage("device.logical_volumes.vtl.editLibrary");
		    			editLibrary(createParameters(_library, _vtlm, _slots, _num_tapes, _num_drives, _drives), _xhtml_out, null,title,_numLibraries);
	    			}
	    			break;
	    		case NEW_LIBRARY: {
		    			int _num_drives = 1;
	    				if(request.getParameter("drives") != null) {
		    				try {
		    					_num_drives = Integer.parseInt(request.getParameter("drives"));
		                	} catch(NumberFormatException _ex) {}
	    				}
		    			int _numLibraries=-1;
		    			if(request.getParameter("numLibraries") != null && !request.getParameter("numLibraries").isEmpty()) {
		    				_numLibraries=Integer.valueOf(request.getParameter("numLibraries"));
		    			}else{
		    				_numLibraries=_vtlm.getLibraries().size();	
		    			}
	    				List<String> _slots = new ArrayList<String>(32);
		    			List<String> _drives = new ArrayList<String>(32);
						for(int x=0; x < _num_drives; x++){
							if(request.getParameter("unitSerialNumberDevice" + x) != null) {
								_drives.add(x, request.getParameter("unitSerialNumberDevice" + x));
							} else {
								_drives.add(x, "");
							}
						}
						if(request.getParameter("numTapes") != null) {
							try {
		    					_num_tapes = Integer.parseInt(request.getParameter("numTapes"));
							} catch(NumberFormatException _ex) {}
		    			}
		    			for(int x = 0; x < 8; x++) {		
		    				_slots.add(x, "");	    				
		    			}
		    			title=getLanguageMessage("device.logical_volumes.vtl.newLibrary");
		    			editLibrary(createParameters(request.getParameterMap(), _vtlm, _slots, _num_tapes, _num_drives, _drives), _xhtml_out, null,title,_numLibraries);
	    			}
	    			break;
	    		case EDIT_TAPE : {
		    			int _num_drives = 0, _num_slots = 8;
		    			if(request.getParameter("library") == null && request.getParameter("library").isEmpty()) {
		    				throw new Exception("");
		    			}
		    			if(request.getParameter("drives") != null) {
		    				try {
		    					_num_drives = Integer.parseInt(request.getParameter("drives"));
		                	} catch(NumberFormatException _ex) {}
	    				}
		    			List<String> _slots = new ArrayList<String>(32);
		    			List<String> _devices = new ArrayList<String>(32);
		    			String _nextId = request.getParameter("nextId");
		    			String _density = "L4";
		    			for(int x = 0; x < _num_drives; x++) {
							if(request.getParameter("unitSerialNumberDevice" + x) != null) {
								_devices.add(x, request.getParameter("unitSerialNumberDevice" + x));
							} else {
								_devices.add(x, "");
							}
						}
		    			int _numLibraries=-1;
		    			if(request.getParameter("numLibraries") != null && !request.getParameter("numLibraries").isEmpty()) {
		    				_numLibraries=Integer.valueOf(request.getParameter("numLibraries"));
		    			}else{
		    				_numLibraries=_vtlm.getLibraries().size();	
		    			}
		    			if(request.getParameter("density") != null) {
		    				_density = request.getParameter("density");
		    			}
		    			if(request.getParameter("numTapes") != null) {
		    				_num_tapes = Integer.parseInt(request.getParameter("numTapes"));
		    			}
		    			if(request.getParameter("slotsNum") != null) {
		    				_num_slots = Integer.parseInt(request.getParameter("slotsNum"));
		    			}
		    			for(int x = 0; x < _num_slots; x++) {
		    				if(request.getParameter("slot" + x) != null){
		    					if("".equals(request.getParameter("slot" + x))) {
		    						_slots.add(x, "E" + _nextId + _num_tapes + _density);
		    						_num_tapes++;
		    					} else {
		    						_slots.add(x, request.getParameter("slot" + x));
		    					}
		    				} else {
		    					_slots.add(x, "");
		    				}
		    			}
						if(request.getParameter("title") != null) {
							title=request.getParameter("title");
						}
		    			editLibrary(createParameters(request.getParameterMap(), _vtlm, _slots, _num_tapes, _num_drives, _devices), _xhtml_out, null,title,_numLibraries);
	    			}
	    			break;    			
	    		case NEW_DRIVE: {
	    				int _num_drives = 1, _num_slots = 8;
	    				if(request.getParameter("drives") != null) {
		    				try {
		    					_num_drives = Integer.parseInt(request.getParameter("drives"));
		                	} catch(NumberFormatException _ex) {}
	    				}
	    				String _nextId = request.getParameter("nextId");
		    			List<String> _slots = new ArrayList<String>(32);
						List<String> _drives = new ArrayList<String>(32);
						String _density = "L4";
						for(int x = 0; x < _num_drives; x++) {
							if(request.getParameter("unitSerialNumberDevice" + x) != null){
								_drives.add(x, request.getParameter("unitSerialNumberDevice" + x));
							} else {
								_drives.add(x, "");
							}
						}
		    			int _numLibraries=-1;
		    			if(request.getParameter("numLibraries") != null && !request.getParameter("numLibraries").isEmpty()) {
		    				_numLibraries=Integer.valueOf(request.getParameter("numLibraries"));
		    			}else{
		    				_numLibraries=_vtlm.getLibraries().size();	
		    			}
						if(request.getParameter("density") != null) {
		    				_density = request.getParameter("density");
		    			}
						if(request.getParameter("numTapes") != null) {
		    				_num_tapes = Integer.parseInt(request.getParameter("numTapes"));
		    			}
						if(request.getParameter("slotsNum") != null) {
		    				_num_slots = Integer.parseInt(request.getParameter("slotsNum"));
		    			}
		    			for(int x = 0; x < _num_slots; x++) {
		    				if(request.getParameter("slot"+x) != null){
		    					if(request.getParameter("slot" + x).isEmpty()) {
		    						_slots.add(x, "E" + _nextId + _num_tapes + _density);
		    						_num_tapes++;
		    					} else {
		    						_slots.add(x, request.getParameter("slot" + x));
		    					}
		    				}
		    				else{
		    					_slots.add(x, "");
		    				}
		    			}
						if(request.getParameter("title") != null) {
							title=request.getParameter("title");
						}
		    			editLibrary(createParameters(request.getParameterMap(),_vtlm,_slots,_num_tapes,_num_drives,_drives),_xhtml_out,null,title,_numLibraries);
	    			}
	    			break;
	    		case NEW_TAPE: {
	    			}
	    			break;
	    		case STORE_LIBRARY: {
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.storage.exception.name"));
		    			}
		    			if(request.getParameter("drives") == null || request.getParameter("drives").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.vtl.exception.noDrivers"));
		    			}
		    			if(request.getParameter("numTapes")==null || Integer.valueOf(request.getParameter("numTapes")).intValue()==0) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.vtl.exception.noTapes"));
		    			}
		    			if(request.getParameter("path")==null || request.getParameter("path").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.vtl.exception.nopath"));
		    			}
		    			if(request.getParameter("capacity")==null || request.getParameter("capacity").isEmpty() && request.getParameter("capacityMag")==null || request.getParameter("capacityMag").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.vtl.exception.nocapacity"));
		    			}
		    			Map<String, Object> library=new HashMap<String, Object>();
		    			library.put("name", request.getParameter("name"));
		    			library.put("density", request.getParameter("density"));
		    			library.put("compression", request.getParameter("compression"));
		    			library.put("path", request.getParameter("path"));
		    			library.put("id", request.getParameter("nextId"));
		    			library.put("numTapes",request.getParameter("numTapes"));
		    			Float aux="M".equals(request.getParameter("capacityMag")) ? (Float.valueOf(request.getParameter("capacity"))) : (Float.valueOf(request.getParameter("capacity"))*1000);
		    			library.put("capacity",String.valueOf(aux.longValue()));
		    			library.put("unitSerialNumber",request.getParameter("unitSerialNumber"));
		    			ArrayList<String> _tapesToRemove=new ArrayList<String>();
		    			if(request.getParameter("tapesToRemove")!=null && !request.getParameter("tapesToRemove").isEmpty()){
		    				StringTokenizer tokens=new StringTokenizer(request.getParameter("tapesToRemove"), "--");
		    				while(tokens.hasMoreTokens()){
		    			            String str=tokens.nextToken();
		    			            _tapesToRemove.add(str);
		    			        }
		    			}
		    			library.put("tapesToRemove",_tapesToRemove);	    			
		    			Map<String, String> drive=null;
		    			List<Map<String, String>> drives=new ArrayList<Map<String, String>>();
		    			for (int x=1;x<=Integer.valueOf(request.getParameter("drives")).intValue();x++){
		    				drive=new HashMap<String, String>();
		    				drive.put("id", request.getParameter("idDrive"+x));
		    				drive.put("unitSerialNumber", request.getParameter("unitSerialNumberDrive"+x));
		    				drives.add(drive);
		    			}
		    			library.put("drives",drives);
		    			List<String> slotTapes=new ArrayList<String>(Integer.valueOf(request.getParameter("slotsNum")).intValue());
		    			for (int x=0; x<(request.getParameter("slotsNum")!=null ? Integer.valueOf(request.getParameter("slotsNum")).intValue() : 8); x++){
		    				if(request.getParameter("slotValue"+x)!=null && !request.getParameter("slotValue"+x).isEmpty()) {
		    					slotTapes.add(x,request.getParameter("slotValue"+x).contains("L") ? request.getParameter("slotValue"+x) : request.getParameter("slotValue"+x)+request.getParameter("density"));
		    				}else{
		    					slotTapes.add(x,"");
		    				}
		    			}
		    			library.put("slotTapes", slotTapes);
		    			int _result=_vtlm.storeLibrary(library);	  
		    			if(_result!=-1) {
		    				writeDocumentResponse(getLanguageMessage("device.logical_volumes.vtl.added"), "/admin/VTLConfiguration");
		    			} else {
		    				writeDocumentResponse(getLanguageMessage("device.logical_volumes.vtl.errorWhenAdd"), "/admin/VTLConfiguration");
		    			}
	    			}
	    			break;
	    		case STORE_TAPE: {
		    		
	    			}
	    			break;
	    		case REMOVE_TAPE: {
		    			int _drives = request.getParameter("drives")!=null && request.getParameter("drives")!="" ? Integer.valueOf(request.getParameter("drives")).intValue() : 1;
		    			StringBuilder _tapesToRemove=new StringBuilder(request.getParameter("tapesToRemove")!=null ? request.getParameter("tapesToRemove") :""); 
						if(request.getParameter("library") != null) {
			            	try {
			            		_drives = 0;
			            	} catch(NumberFormatException _ex) {
			            		
			            	}
						}
		    			int _numLibraries=-1;
		    			if(request.getParameter("numLibraries") != null && !request.getParameter("numLibraries").isEmpty()) {
		    				_numLibraries=Integer.valueOf(request.getParameter("numLibraries"));
		    			}else{
		    				_numLibraries=_vtlm.getLibraries().size();	
		    			}
						List<String> _deviceDrives = new ArrayList<String>(32);
						for (int x=0; x<_drives; x++){
							if (request.getParameter("unitSerialNumberDevice"+x)!=null){
								_deviceDrives.add(x, request.getParameter("unitSerialNumberDevice"+x) !=null ? request.getParameter("unitSerialNumberDevice"+x) : "");
							}else{
								_deviceDrives.add(x, "");
							}
						}
						List<String> slots = new ArrayList<String>(request.getParameter("slotsNum")!=null ? Integer.valueOf(request.getParameter("slotsNum")).intValue() : 8);
						_num_tapes=request.getParameter("numTapes")!=null ? Integer.valueOf(request.getParameter("numTapes")).intValue() : 0;
						for (int x=0; x<(request.getParameter("slotsNum")!=null ? Integer.valueOf(request.getParameter("slotsNum")).intValue() : 8); x++){
							if (Integer.valueOf(request.getParameter("removeTape")).intValue()==x ){
								//_vm.removeTape(request.getParameter("slotValue"+x), request.getParameter("path"));
								_tapesToRemove.append(_tapesToRemove.length()!=0 ? "--"+request.getParameter("slotValue"+x) : request.getParameter("slotValue"+x));
								slots.add(x,"");
							}
							else{
								slots.add(x, request.getParameter("slotValue"+x) !=null ? request.getParameter("slotValue"+x) : "");
							}
						}
						if(request.getParameter("title") != null) {
							title=request.getParameter("title");
						}
						editLibrary(createParameters(request.getParameterMap(),_vtlm,slots,_num_tapes,_drives,_deviceDrives),_xhtml_out,_tapesToRemove.toString(),title,_numLibraries);
	    			}
	    			break;
	    		case REMOVE_LIBRARY: {
		    			if(request.getParameter("id") == null || request.getParameter("id").isEmpty()) {
		    				throw new Exception(getLanguageMessage("device.logical_volumes.vtl.specify_id"));
		    			}	    			
		    			if(request.getParameter("confirm") != null) {
		    				if (_vtlm.removeLibrary(request.getParameter("id"))!=-1){
		    					response.sendRedirect("/admin/VTLConfiguration");	
		    					this.redirected=true;
		    				}else{
		    					throw new Exception(getLanguageMessage("device.logical_volumes.vtl.error"));
		    				}
			    			
		    			} else {
//		    				String _device = _vtlm.getLibraryChangerDevice(request.getParameter("id"));
		    				String _device = request.getParameter("unitSerialNumber");
		    				List<Map<String, String>> _autochanger_devices =_device!=null && !_device.isEmpty() ? StorageManager.getAutochangerDevices() : null;
		    				if (_autochanger_devices != null){
		    					for (Map<String, String> _lib: _autochanger_devices){	    						
//		    						if (_device.trim().equals(_lib.get("device").trim())){
		    						if (_lib.get("device").contains(_device)){
		    							throw new Exception(getLanguageMessage("device.logical_volumes.vtl.libraryIsUsed"));
		    						}
		    					}
		    				}
							List<String> drives = new ArrayList<String>(32);
							for (int x=0; request.getParameter("unitSerialNumberDevice"+x)!=null; x++){
								drives.add(x, request.getParameter("unitSerialNumberDevice"+x) !=null ? request.getParameter("unitSerialNumberDevice"+x) : "");						
							}
		    				List<String> _drivesCom = _vtlm.getLibraryDeviceDrives(request.getParameter("id"));
//		    				List<Map<String, String>> _tape_devices = _drivesCom!=null && !_drivesCom.isEmpty() ? StorageManager.getTapeDevices() : null;
		    				List<Map<String, String>> _tape_devices = StorageManager.getTapeDevices();
		    				if (_tape_devices!=null){
		    					for (Map<String, String> _lib: _tape_devices){	    						
		    						if (_drivesCom.contains(_lib.get("device")!=null ? _lib.get("device").replace("n", "").trim() : null)){
		    							throw new Exception(getLanguageMessage("device.logical_volumes.vtl.libraryIsUsed"));
		    						}
		    					}
		    				}
		    				writeDocumentQuestion(getLanguageMessage("device.logical_volumes.vtl.question"), "/admin/VTLConfiguration?type=" + REMOVE_LIBRARY + "&id=" + request.getParameter("id") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case ADD_TAPES: {
		    			int _drives = request.getParameter("drives")!=null && request.getParameter("drives")!="" ? Integer.valueOf(request.getParameter("drives")).intValue() : 1;
		    			if(request.getParameter("library") != null) {
		                	try {
		                		_drives = 0;
		                	} catch(NumberFormatException _ex) {
		                		
		                	}
		    			}
						List<String> _deviceDrives = new ArrayList<String>(32);
						for (int x=0; x<_drives; x++){
							if (request.getParameter("unitSerialNumberDevice"+x)!=null){
								_deviceDrives.add(x, request.getParameter("unitSerialNumberDevice"+x) !=null ? request.getParameter("unitSerialNumberDevice"+x) : "");
							}else{
								_deviceDrives.add(x, "");
							}
						}
		    			int _numLibraries=-1;
		    			if(request.getParameter("numLibraries") != null && !request.getParameter("numLibraries").isEmpty()) {
		    				_numLibraries=Integer.valueOf(request.getParameter("numLibraries"));
		    			}else{
		    				_numLibraries=_vtlm.getLibraries().size();	
		    			}
		    			String _nextId=request.getParameter("nextId");
		    			List<String> slots = new ArrayList<String>(32);	    			
		    			_num_tapes=request.getParameter("numTapes")!=null ? Integer.valueOf(request.getParameter("numTapes")).intValue() : 0;
		    			for (int x=0; x<(request.getParameter("slotsNum")!=null ? Integer.valueOf(request.getParameter("slotsNum")).intValue() : 8); x++){
		    				if (request.getParameter("slot"+x)!=null){
		    					if ("".equals(request.getParameter("slot"+x))){
		    						slots.add(x, "E"+_nextId+(_num_tapes<10 ? "00" : "0")+_num_tapes);
		    						_num_tapes++;
		    					}else{
		    						slots.add(x, request.getParameter("slot"+x));
		    					}
		    				} else {
		    					slots.add(x, request.getParameter("slotValue"+x) !=null ? request.getParameter("slotValue"+x) : "");
		    				}
		    			}
						if(request.getParameter("title") != null) {
							title=request.getParameter("title");
						}
		    			editLibrary(createParameters(request.getParameterMap(),_vtlm,slots,_num_tapes,_drives,_deviceDrives),_xhtml_out,null,title,_numLibraries);
		    		}	
	    			break;
				case CHANGE_NUM_SLOT: {
						int _drives = request.getParameter("drives")!=null && request.getParameter("drives")!="" ? Integer.valueOf(request.getParameter("drives")).intValue() : 1;
						if(request.getParameter("library") != null) {
			            	try {
			            		_drives = 0;
			            	} catch(NumberFormatException _ex) {
			            		
			            	}
						}
		    			int _numLibraries=-1;
		    			if(request.getParameter("numLibraries") != null && !request.getParameter("numLibraries").isEmpty()) {
		    				_numLibraries=Integer.valueOf(request.getParameter("numLibraries"));
		    			}else{
		    				_numLibraries=_vtlm.getLibraries().size();	
		    			}
						List<String> _deviceDrives = new ArrayList<String>(32);
						for (int x=0; x<_drives; x++){
							if (request.getParameter("unitSerialNumberDevice"+x)!=null){
								_deviceDrives.add(x, request.getParameter("unitSerialNumberDevice"+x) !=null ? request.getParameter("unitSerialNumberDevice"+x) : "");
							}else{
								_deviceDrives.add(x, "");
							}
						}
						List<String> slots = new ArrayList<String>(32);
						_num_tapes=request.getParameter("numTapes")!=null ? Integer.valueOf(request.getParameter("numTapes")).intValue() : 0;
						for (int x=0; x<(request.getParameter("slotsNum")!=null ? Integer.valueOf(request.getParameter("slotsNum")).intValue() : 8); x++){
							slots.add(x, request.getParameter("slotValue"+x) !=null ? request.getParameter("slotValue"+x) : "");
							
						}
						if(request.getParameter("title") != null) {
							title=request.getParameter("title");
						}
						editLibrary(createParameters(request.getParameterMap(),_vtlm,slots,_num_tapes,_drives,_deviceDrives),_xhtml_out,null,title,_numLibraries);
					}	
					break;
				case REMOVE_DRIVE: {
					int _drives = request.getParameter("drives")!=null && request.getParameter("drives")!="" ? Integer.valueOf(request.getParameter("drives")).intValue() : 1;
					if(request.getParameter("library") != null) {
		            	try {
		            		_drives = 0;
		            	} catch(NumberFormatException _ex) {
		            		
		            	}
					}
					if(request.getParameter("title") != null) {
						title=request.getParameter("title");
					}
					List<String> _deviceDrives = new ArrayList<String>(32);
					for (int x=0; x<_drives; x++){
						if (request.getParameter("unitSerialNumberDevice"+x)!=null){
							_deviceDrives.add(x, request.getParameter("unitSerialNumberDevice"+x) !=null ? request.getParameter("unitSerialNumberDevice"+x) : "");
						}else{
							_deviceDrives.add(x, "");
						}
					}
					List<String> slots = new ArrayList<String>(32);
					_num_tapes = request.getParameter("numTapes")!=null ? Integer.valueOf(request.getParameter("numTapes")).intValue() : 0;
					for(int x=0; x<(request.getParameter("slotsNum")!=null ? Integer.valueOf(request.getParameter("slotsNum")).intValue() : 8); x++) {
						slots.add(x, request.getParameter("slotValue"+x) !=null ? request.getParameter("slotValue"+x) : "");						
					}
					editLibrary(createParameters(request.getParameterMap(),_vtlm,slots,_num_tapes,_drives,_deviceDrives),_xhtml_out,null,title,_vtlm.getLibraries().size());
				}	
				break;
		    }
	    } catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
	
	private Map<String, Object> createParameters(Map<String, Object> request,VTLManager _vm,List<String> slots,int _numTapes,int _drives,List<String> _deviceDrives){
		Map<String, Object> _result=new HashMap<String, Object>();
		_result.put("unitSerialNumber", request.get("unitSerialNumber")!=null ? (request.get("unitSerialNumber").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("unitSerialNumber"))[0] : (String)request.get("unitSerialNumber")) : VTLManager.generateLabel(6));
		_result.put("nextId", request.get("id")!=null ? request.get("id") :( request.get("nextId").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("nextId"))[0] : (String)request.get("nextId")));
		_result.put("name", request.get("name")!=null ? (request.get("name").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("name"))[0] : (String)request.get("name")) : "");
		_result.put("tapesToRemove", request.get("tapesToRemove")!=null ? (request.get("tapesToRemove").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("tapesToRemove"))[0] : (String)request.get("tapesToRemove")) : "");
		_result.put("capacity", request.get("capacity")!=null ? (request.get("capacity").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("capacity"))[0] : (String)request.get("capacity")) : "");
		_result.put("capacityMag", request.get("capacityMag")!=null ? (request.get("capacityMag").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("capacityMag"))[0] : (String)request.get("capacityMag")) : "");
		_result.put("path", request.get("name")!=null ? (request.get("path").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("path"))[0] : (String)request.get("path")) : "");
		_result.put("density", request.get("density")!=null ? (request.get("density").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("density"))[0] : (String)request.get("density")) : "");
		_result.put("device", request.get("device")!=null ? (request.get("device").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("device"))[0] : (String)request.get("device")) : "");
		_result.put("compression", request.get("compression")!=null ? (request.get("compression").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("compression"))[0] : (String)request.get("compression")) : "");		
		_result.put("slots", slots); 
		_result.put("deviceDrives", _deviceDrives);
		_result.put("slotsNum", request.get("slotsNum")!=null ? (request.get("slotsNum").getClass().getName().equals("[Ljava.lang.String;") ? ((String[]) request.get("slotsNum"))[0] : (String)request.get("slotsNum")) : "");
		_result.put("_numTapes", String.valueOf(_numTapes)); 
		_result.put("_drives", String.valueOf(_drives));
		return _result;
	}
	
	@SuppressWarnings("unchecked")
	private void editLibrary(Map<String, Object> _params,PrintWriter _xhtml_out, String _tapesToRemove,String title, int _numLibraries) throws Exception{
			String _capacity = "800";
			String idLibrary = String.valueOf(_params.get("unitSerialNumber"));
			List<Map<String, String>> _lvs = VolumeManager.getMountableLogicalVolumes();
			String _nextId=(String) _params.get("nextId");
			if(_params.get("capacity") != null && !((String)_params.get("capacity")).isEmpty()) {
				_capacity = String.valueOf(_params.get("capacity"));
			} 
		    _xhtml_out.println("<script>");
   			_xhtml_out.println("");
   			_xhtml_out.println("function addTapes() {");
   			_xhtml_out.println("  document.vtlConfig.type.value = "+ADD_TAPES+";");
   			_xhtml_out.println("  submitForm(document.vtlConfig.submit());");
   			_xhtml_out.println("}");
   			_xhtml_out.println("function saveLibrary() {");
   			_xhtml_out.println("  document.vtlConfig.type.value = "+STORE_LIBRARY+";");
   			_xhtml_out.println("  submitForm(document.vtlConfig.submit());");
   			_xhtml_out.println("}");
   			_xhtml_out.println("function changeSlotNum(num) {");
   			_xhtml_out.println("  document.vtlConfig.type.value = "+CHANGE_NUM_SLOT+";");
   			_xhtml_out.println("  document.vtlConfig.removeTape.value = "+CHANGE_NUM_SLOT+";");
   			_xhtml_out.println("  submitForm(document.vtlConfig.submit());");
   			_xhtml_out.println("}");
   			_xhtml_out.println("function removeTape(num) {");
   			_xhtml_out.println("  document.vtlConfig.type.value = "+REMOVE_TAPE+";");
   			_xhtml_out.println("  document.vtlConfig.removeTape.value = num;");
   			_xhtml_out.println("  submitForm(document.vtlConfig.submit());");
   			_xhtml_out.println("}");
   			_xhtml_out.println("function updateDrive(num) {");
   			_xhtml_out.println("  document.vtlConfig.type.value = "+REMOVE_DRIVE+";");
   			_xhtml_out.println("  document.vtlConfig.drives.value = num;");
   			_xhtml_out.println("  submitForm(document.vtlConfig.submit());");
   			_xhtml_out.println("}");
   			_xhtml_out.println("function selectSlots() {");
   			_xhtml_out.println("    var all = document.vtlConfig.selectAll;");
   			_xhtml_out.println("	if (all.checked) {");
   			_xhtml_out.println("		for (var i=0;i<"+((List<String>) _params.get("slots")).size()+";i++) {");
   			_xhtml_out.println("			document.getElementById(\"slot\"+i).checked=true;");
   			_xhtml_out.println(" 		}");
			_xhtml_out.println("	} else {");
			_xhtml_out.println("		for (var i=0;i<"+((List<String>) _params.get("slots")).size()+";i++) {");
			_xhtml_out.println("			document.getElementById(\"slot\"+i).checked=false;");
			_xhtml_out.println(" 		}");
			_xhtml_out.println("	}");
   			_xhtml_out.println("}");
   			if (!_lvs.isEmpty()) {
   				_xhtml_out.println("function updateCapacity() {");
   				_xhtml_out.println("var pathSel = document.vtlConfig.path.value;");
   				_xhtml_out.println("if (pathSel) {");
   				_xhtml_out.println("var lvSizes = [];");
   				for(Map<String, String> _lv : _lvs) {
   					_xhtml_out.println("lvSizes[\""+_lv.get("vg")+"/"+_lv.get("name")+"\"]="+_lv.get("size-raw")+";");
   				}
   				_xhtml_out.println("  document.vtlConfig.capacity.value = lvSizes[pathSel]/"+((List<String>) _params.get("slots")).size()+"/1024;");
   				_xhtml_out.println("  document.vtlConfig.capacityMag.value = 'M';");
   				_xhtml_out.println(" }");
   				_xhtml_out.println("}");
   			}
   			_xhtml_out.println("</script>");
			writeDocumentBack("/admin/VTLConfiguration");
			_xhtml_out.println("<form action=\"/admin/VTLConfiguration\" id=\"vtlConfig\" name=\"vtlConfig\" method=\"post\">");
		    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE_LIBRARY + "\"/>");
		    _xhtml_out.println("<input type=\"hidden\" name=\"modify\" value=\"no\"/>");
		    _xhtml_out.println("<input type=\"hidden\" name=\"numTapes\" value=\""+_params.get("_numTapes")+"\"/>");
			_xhtml_out.println("<input type=\"hidden\" name=\"drive\" value=\"0\"/>");
			_xhtml_out.println("<input type=\"hidden\" name=\"title\" value=\""+title+"\"/>");
			_xhtml_out.println("<input type=\"hidden\" name=\"nextId\" value=\""+(String) _params.get("nextId")+"\"/>");
			_xhtml_out.println("<input type=\"hidden\" name=\"tapesToRemove\" value=\""+(_tapesToRemove!=null ? _tapesToRemove : (_params.get("tapesToRemove")!=null ? (String) _params.get("tapesToRemove") : ""))+"\"/>");
			_xhtml_out.println("<input type=\"hidden\" name=\"removeTape\" />");
			_xhtml_out.println("<input type=\"hidden\" name=\"numLibraries\" value=\""+_numLibraries+"\"/>");
			_xhtml_out.println("<input type=\"hidden\" name=\"drives\" value=\""+_params.get("_drives")+"\"/>");
			
            _xhtml_out.println("<h1>");
			_xhtml_out.print("<img src=\"/images/tape_32.png\"/>");
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl"));
			_xhtml_out.println("</h1>");
			_xhtml_out.print("<div class=\"info\">");
			_xhtml_out.println(getLanguageMessage("device.logical_volumes.vtl.info"));
			_xhtml_out.println("</div>");
			
			if (_numLibraries>0 && getLanguageMessage("device.logical_volumes.vtl.newLibrary").equals(title))
			{
				_xhtml_out.print("<div class=\"warn\">");
				_xhtml_out.print("</br>"+getLanguageMessage("device.logical_volumes.vtl.infoWarning"));	
				_xhtml_out.println("</div>");
			}
			
			_xhtml_out.println("<div class=\"window\">");
			_xhtml_out.println("<h2>");
			_xhtml_out.println(title);
			_xhtml_out.print("<a href=\"javascript:saveLibrary();\"><img src=\"/images/disk_16.png\" title=\"");
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
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.name"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"name\" value=\"");
	    	_xhtml_out.println((String) _params.get("name"));
	    	_xhtml_out.println("\"/>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");    	
	    	if(_params.get("device") != null && !String.valueOf(_params.get("device")).isEmpty()) {
	            _xhtml_out.println("<div class=\"standard_form\">");
		    	_xhtml_out.print("<label for=\"device\">");
		    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.device"));
		    	_xhtml_out.println(": </label>");
		    	_xhtml_out.println("<input type=\"hidden\" name=\"device\" value=\"");
		    	_xhtml_out.println((String) _params.get("device"));
		    	_xhtml_out.println("\"/>");
		    	_xhtml_out.println("<input class=\"form_text\" disabled=\"true\" type=\"text\" value=\"");
		    	_xhtml_out.println((String) _params.get("device"));
		    	_xhtml_out.println("\"/>");
		    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		    	_xhtml_out.println("</div>");    	    		
	    	}
            _xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"path\">");
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.path"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<select class=\"form_select\" name=\"path\" onchange=\"updateCapacity()\">");
	    	_xhtml_out.print("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
			for(Map<String, String> _lv : _lvs) {
				_xhtml_out.print("<option value=\"");
				_xhtml_out.print(_lv.get("vg"));
				_xhtml_out.print("/");
				_xhtml_out.print(_lv.get("name"));
				_xhtml_out.print("\"");
				if( _params.get("path") != null && ((String) _params.get("path")).equals(_lv.get("vg") + "/" + _lv.get("name"))) {
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
	    	_xhtml_out.print("<label for=\"unitSerialNumber\">");
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.unitSerialNumber"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<input class=\"form_text\" readonly=\"true\" type=\"text\" name=\"unitSerialNumber\" value=\""+idLibrary+"\"/>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"density\">");
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.density"));
	    	_xhtml_out.println(": </label>");
	    	if(tapesAreAvailable((List<String>) _params.get("slots"))){
	    		_xhtml_out.println("<input type=\"hidden\" name=\"density\" value=\""+_params.get("density")+"\"/>");
	    		_xhtml_out.println("<select class=\"form_select\"");
	    		_xhtml_out.println(" disabled=\"true\" ");
	    	} else { 
	    		_xhtml_out.println("<select class=\"form_select\"");
	    		_xhtml_out.println(" name=\"density\" ");
	    	}
	    	_xhtml_out.println(">");
			_xhtml_out.println("<option value=\"L4\" "+("L4".equals(_params.get("density")) ? "selected=\"selected\"" : "")+">LTO4</option>");
			_xhtml_out.println("<option value=\"L5\" "+("L5".equals(_params.get("density")) ? "selected=\"selected\"" : "")+">LTO5</option>");
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	
	    	_xhtml_out.println("<div class=\"standard_form\">");
		    _xhtml_out.print("<label for=\"capacity\">");
		    _xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.capacity"));
		    _xhtml_out.println(": </label>");
		    
	    	if(tapesAreAvailable((List<String>) _params.get("slots"))) {
    			_xhtml_out.println("<input class=\"form_text\" readonly=\"true\" type=\"text\" name=\"capacity\" value=\""+_capacity+"\"/>");
	    		_xhtml_out.println("<input readonly=\"true\" type=\"hidden\" name=\"capacityMag\" value=\""+_params.get("capacityMag")+"\"/>");
	    		_xhtml_out.println("<select class=\"form_select\"");
	    		_xhtml_out.println(" disabled=\"true\" ");
		    } else { 
	    		_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"capacity\" value=\""+_capacity+"\"/>");
	    		_xhtml_out.println("<select class=\"form_select\"");
	    		_xhtml_out.println(" name=\"capacityMag\" ");
		    }
	    	_xhtml_out.println(">");
	    	_xhtml_out.println("<option value=\"M\" "+("M".equals(_params.get("capacityMag")) ? "selected=\"selected\"" : "")+">M</option>");
			_xhtml_out.println("<option value=\"G\" "+("G".equals(_params.get("capacityMag")) ? "selected=\"selected\"" : "")+">G</option>");				
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"compression\">");
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.compression"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<select class=\"form_select\" name=\"compression\">");
	    	_xhtml_out.println("<option value=\"1\" "+("1".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">1</option>");
			_xhtml_out.println("<option value=\"2\" "+("2".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">2</option>");
			_xhtml_out.println("<option value=\"3\" "+("3".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">3</option>");
			_xhtml_out.println("<option value=\"4\" "+("4".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">4</option>");
			_xhtml_out.println("<option value=\"5\" "+("5".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">5</option>");
			_xhtml_out.println("<option value=\"6\" "+("6".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">6</option>");
			_xhtml_out.println("<option value=\"7\" "+("7".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">7</option>");
			_xhtml_out.println("<option value=\"8\" "+("8".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">8</option>");
			_xhtml_out.println("<option value=\"9\" "+("9".equals(_params.get("compression")) ? "selected=\"selected\"" : "")+">9</option>");
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	
	    	_xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"slotsNum\">");
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.slotsNum"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<select class=\"form_select\" name=\"slotsNum\" onchange=\"changeSlotNum(this.value);\">");
	    	_xhtml_out.println("<option value=\"8\" "+("8".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">8</option>");
			_xhtml_out.println("<option value=\"16\" "+("16".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">16</option>");
			_xhtml_out.println("<option value=\"24\" "+("24".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">24</option>");
			_xhtml_out.println("<option value=\"32\" "+("32".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">32</option>");
			_xhtml_out.println("<option value=\"40\" "+("40".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">40</option>");
			_xhtml_out.println("<option value=\"48\" "+("48".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">28</option>");
			_xhtml_out.println("<option value=\"56\" "+("56".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">56</option>");
			_xhtml_out.println("<option value=\"64\" "+("64".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">64</option>");
			_xhtml_out.println("<option value=\"72\" "+("72".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">72</option>");
			_xhtml_out.println("<option value=\"80\" "+("80".equals(_params.get("slotsNum")) ? "selected=\"selected\"" : "")+">80</option>");
			
			
			_xhtml_out.println("</select>");
	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	_xhtml_out.println("</div>");
	    	
	    	_xhtml_out.println("</fieldset>");
            _xhtml_out.println("<div class=\"clear\"/></div>");
    	    _xhtml_out.print("</div>");
    	    
    	    _xhtml_out.println("<div class=\"window\">");
			_xhtml_out.println("<h2>");
			_xhtml_out.println(getLanguageMessage("device.logical_volumes.vtl.drives"));
			_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
        	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
        	_xhtml_out.print("\" alt=\"");
        	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
        	_xhtml_out.println("\"/></a>");
	
          	_xhtml_out.print("<a href=\"javascript:updateDrive("+(Integer.valueOf((String) _params.get("_drives"))+1)+");");
			_xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
        	_xhtml_out.print(getLanguageMessage("common.message.add"));
        	_xhtml_out.print("\" alt=\"");
        	_xhtml_out.print(getLanguageMessage("common.message.add"));
        	_xhtml_out.println("\"/></a>");
        	if(Integer.valueOf((String) _params.get("_drives"))>1) {
	           	_xhtml_out.print("<a href=\"javascript:updateDrive("+(Integer.valueOf((String) _params.get("_drives"))-1)+");");
				_xhtml_out.print("\"><img src=\"/images/delete_16.png\" title=\"");
	        	_xhtml_out.print(getLanguageMessage("common.message.delete"));
	        	_xhtml_out.print("\" alt=\"");
	        	_xhtml_out.print(getLanguageMessage("common.message.delete"));
	        	_xhtml_out.println("\"/></a>");
        	}
            _xhtml_out.println("</h2>");
            _xhtml_out.println("<fieldset>");
            _xhtml_out.println("<table>");
            for(int i = 1; i <= Integer.valueOf((String) _params.get("_drives")); i++) {
            	_xhtml_out.print("<tr>");
            	_xhtml_out.print("<td>");
            	_xhtml_out.println("<div class=\"strict_form\">");
    	    	_xhtml_out.print("<label for=\"drive\" class=\"labelSort\">");
    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.id"));
    	    	_xhtml_out.println(": </label>");
    	    	_xhtml_out.println("<input class=\"form_text\" readonly=\"true\" type=\"text\" name=\"idDrive"+i+"\" value=\""+_nextId+i+"\"/>");
    	    	_xhtml_out.println("</div>");
    	    	_xhtml_out.print("</td>");
    	    	_xhtml_out.print("<td>");
              	_xhtml_out.println("<div class=\"strict_form\">");
    	    	_xhtml_out.print("<label for=\"drive\" class=\"labelSort\">");
    	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.unitSerialNumber"));
    	    	_xhtml_out.println(": </label>");
    	    	_xhtml_out.println("<input class=\"form_text\" readonly=\"true\" type=\"text\" name=\"unitSerialNumberDrive"+i+"\" value=\""+idLibrary+"_"+i+"\"/>");
    	    	_xhtml_out.println("</div>");
    	    	_xhtml_out.print("</td>");
    	    	if(_params.get("deviceDrives")!=null && ((List<String>)_params.get("deviceDrives")).size()>0 && ((List<String>)_params.get("deviceDrives")).get(i-1)!=null && !((List<String>)_params.get("deviceDrives")).get(i-1).isEmpty()) { 
    	    		_xhtml_out.print("<td>");
    	            _xhtml_out.println("<div class=\"strict_form\" >");
    		    	_xhtml_out.print("<label for=\"device\" >");
    		    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.device"));
    		    	_xhtml_out.println(": </label>");
    		    	_xhtml_out.println("<input type=\"hidden\" name=\"unitSerialNumberDevice"+(i-1)+"\" value=\"");
    		    	_xhtml_out.println(((List<String>)_params.get("deviceDrives")).get(i-1));
    		    	_xhtml_out.println("\"/>");
    		    	_xhtml_out.println("<input class=\"form_text\" disabled=\"true\" type=\"text\" value=\"");
    		    	_xhtml_out.println(((List<String>)_params.get("deviceDrives")).get(i-1));
    		    	_xhtml_out.println("\"/>");
    		    	_xhtml_out.println("</div>");    	
    		    	_xhtml_out.print("</td>");
    	    	}
            }
            _xhtml_out.println("</table>");
            
            _xhtml_out.println("</fieldset>");
            _xhtml_out.println("<div class=\"clear\"/></div>");
    	    _xhtml_out.print("</div>");
    	    _xhtml_out.println("<div class=\"window\">");
			_xhtml_out.println("<h2>");
			_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.slots"));
			_xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
	        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
	       	_xhtml_out.print("\" alt=\"");
	       	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
	       	_xhtml_out.println("\"/></a>");
			_xhtml_out.print("<a href=\"javascript:addTapes();");
			_xhtml_out.print("\"><img src=\"/images/tape_accept_16.png\" title=\"");
        	_xhtml_out.print(getLanguageMessage("backup.storage.tape.mount"));
        	_xhtml_out.print("\" alt=\"");
        	_xhtml_out.print(getLanguageMessage("backup.storage.tape.mount"));
        	_xhtml_out.println("\"/></a>");
			_xhtml_out.println("</h2>");
            _xhtml_out.println("<fieldset>");
            
            _xhtml_out.println("<div class=\"standard_form\">");
	    	_xhtml_out.print("<label for=\"selectAll\">");
	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vtl.selectAll"));
	    	_xhtml_out.println(": </label>");
	    	_xhtml_out.println("<input class=\"form_checkbox\" type=\"checkbox\" name=\"selectAll\" onclick=\"selectSlots()\"/>");
	    	_xhtml_out.println("</div>");
            
	    	_xhtml_out.println("<div class=\"standard_form\">");
            _xhtml_out.println("<table>");
            
            for(int _offset = 0; _offset < ((List<String>) _params.get("slots")).size(); _offset++) {
            	String _tapeName= ((List<String>) _params.get("slots")).get(_offset);
				if(_tapeName!=null && !_tapeName.isEmpty() && _tapeName.contains("L")) {
					_tapeName=_tapeName.substring(0,_tapeName.length());
				}
				
            	if(_offset % 4 == 0) {
					_xhtml_out.print("<tr");
					if(_offset % 8 == 0) {
						_xhtml_out.print(" class=\"highlight\"");
					}					
					_xhtml_out.println(">");
					_xhtml_out.print("<input type=\"hidden\" name=\"slotValue"+_offset+"\" value=\"");
					_xhtml_out.print(_tapeName);
					_xhtml_out.print("\"/>");
					_xhtml_out.print("<td><input class=\"form_radio\" type=\"radio\" name=\"slot"+_offset+"\" id=\"slot"+_offset+"\" value=\"");
					_xhtml_out.print(_tapeName);
					_xhtml_out.print("\"/>slot ");
					_xhtml_out.print(_offset);
					_xhtml_out.print(":");
					_xhtml_out.print(_tapeName);
					_xhtml_out.print("&nbsp;");
					if(_tapeName==null || "".equals(_tapeName)) {
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_unassigned"));
					} else {
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_assigned"));
						_xhtml_out.print("<a href=\"javascript:removeTape("+_offset+");");
		    			_xhtml_out.print("\"><img src=\"/images/tape_cross_16.png\" title=\"");
		            	_xhtml_out.print(getLanguageMessage("common.message.delete"));
		            	_xhtml_out.print("\" alt=\"");
		            	_xhtml_out.print(getLanguageMessage("common.message.delete"));
		            	_xhtml_out.println("\"/></a>");    	   
					}	    	    		               	
					_xhtml_out.println("</td>");
					if((_offset + 1) == ((List<String>) _params.get("slots")).size()) {
						_xhtml_out.println("<td>&nbsp;</td>");
						_xhtml_out.println("</tr>");
					}
				} else {
					_xhtml_out.print("<td>");
					_xhtml_out.print("<input type=\"hidden\" name=\"slotValue"+_offset+"\" value=\"");
					_xhtml_out.print(_tapeName);
					_xhtml_out.print("\"/>");
					_xhtml_out.print("<input class=\"form_radio\" type=\"radio\" name=\"slot"+_offset+"\" id=\"slot"+_offset+"\"  value=\"");
					_xhtml_out.print(_tapeName);
					_xhtml_out.print("\"/>slot ");
					_xhtml_out.print(_offset);
					_xhtml_out.print(":");
					_xhtml_out.print(_tapeName);
					_xhtml_out.print("&nbsp;");
					if(_tapeName==null || "".equals(_tapeName)) {
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_unassigned"));
					} else {
						_xhtml_out.print(getLanguageMessage("backup.storage.autochanger.tape_assigned"));
						_xhtml_out.print("<a href=\"javascript:removeTape("+_offset+");");
		    			_xhtml_out.print("\"><img src=\"/images/tape_cross_16.png\" title=\"");
		            	_xhtml_out.print(getLanguageMessage("common.message.delete"));
		            	_xhtml_out.print("\" alt=\"");
		            	_xhtml_out.print(getLanguageMessage("common.message.delete"));
		            	_xhtml_out.println("\"/></a>");	 
					}
					_xhtml_out.println("</td>");
					if(_offset % 4 == 3) {
						_xhtml_out.println("</tr>");
					}
				}
			}
            _xhtml_out.println("</table>");
            _xhtml_out.print("</div>");
            _xhtml_out.println("</fieldset>");
	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	    	_xhtml_out.print("</div>");
    	    _xhtml_out.println("</form>");
    	    
    	    _xhtml_out.println("<script type=\"text/javascript\">");
   			_xhtml_out.println("updateCapacity();");
   			_xhtml_out.println("</script>");
	}
	
	private static boolean tapesAreAvailable(List<String> slots){		  
        for(int _offset = 0; _offset < slots.size(); _offset++) {
        	String _tapeName= slots.get(_offset);
        	if(_tapeName!=null && !_tapeName.isEmpty()) {
        		return true;
        	}
        }
        return false;
	}
}
