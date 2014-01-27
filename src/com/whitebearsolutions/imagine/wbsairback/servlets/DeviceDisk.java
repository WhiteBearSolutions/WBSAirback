package com.whitebearsolutions.imagine.wbsairback.servlets;
	

import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.CloudManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.ZFSConfiguration;
import com.whitebearsolutions.imagine.wbsairback.frontend.HtmlFormUtils;
import com.whitebearsolutions.imagine.wbsairback.net.HACommClient;

public class DeviceDisk extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int NEW_VG = 2;
	public final static int EDIT_VG = 3;
	public final static int STORE_VG = 4;
	public final static int REMOVE_VG = 5;
	public final static int NEW_LV = 6;
	public final static int EDIT_LV = 8;
	public final static int STORE_LV = 9;
	public final static int STORE_EXTEND_LV = 10;
	public final static int REMOVE_LV = 11;
	public final static int SNAPSHOT_LV = 12;
	public final static int SNAPSHOT_REMOVE_LV = 13;
	public final static int REFRESH_ALL = 15;
	public final static int UMOUNT_VG = 16;
	public final static int UPDATELOCAL_VG = 17;
	public final static int REMOVE_POOL_DISK = 24;
	
	public final static int NEW_CLOUD_DISK = 18;
	public final static int STORE_CLOUD_DISK = 19;
	public final static int STATS_CLOUD_DISK = 20;
	public final static int REMOVE_CLOUD_DISK = 21;
	
	public final static int RECOVER_VG = 22;
	public final static int RECOVER_VG_STORE = 23;
	
	private int type;
	public final static String baseUrl = "/admin/"+DeviceDisk.class.getSimpleName();
	
	private final static Logger logger = LoggerFactory.getLogger(DeviceDisk.class);
	
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
			if (type != SNAPSHOT_LV && type != SNAPSHOT_REMOVE_LV && type != REFRESH_ALL && type != UMOUNT_VG)
				writeDocumentHeader();

			VolumeManager _vm = new VolumeManager(this.sessionManager.getConfiguration());
			//StorageManager _sm = new StorageManager(this.sessionManager.getConfiguration());
			CloudManager _cm = new CloudManager();
			
			switch(this.type) {
	    		default: {
	    				List<Map<String, String>> _vgs = VolumeManager.getVolumeGroups();
	    				List<Map<String, String>> _lvs = VolumeManager.getLogicalVolumes();
	    			
    	    			_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/highcharts.js\"></script>");
    	    			_xhtml_out.print("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />");
						if("es".equals(this.messagei18N.getLocale().getLanguage()))
							_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>");
						else
							_xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>");
					    _xhtml_out.print("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>");
					    
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.disk"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.volume_groups.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
		    			_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups"));
						
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");
					    
	                    _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
	                    _xhtml_out.print(NEW_VG);
	                    _xhtml_out.println("\"><img src=\"/images/add_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.println("\"/></a>");
				        
				        _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
	                    _xhtml_out.print(RECOVER_VG);
	                    _xhtml_out.println("\"><img src=\"/images/agg_recover_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("device.volume_groups.recover_vg"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("device.volume_groups.recover_vg"));
				        _xhtml_out.println("\"/></a>");
				        
				        _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
	                    _xhtml_out.print(REFRESH_ALL);
	                    _xhtml_out.print("\"><img src=\"/images/drive_check_16.png\" title=\"");
	                    _xhtml_out.print(getLanguageMessage("device.volume_groups.scan_devices"));
	                    _xhtml_out.print("\" alt=\"");
	                    _xhtml_out.print(getLanguageMessage("device.volume_groups.scan_devices"));
	                    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    int numAgg=0;
						if(!_vgs.isEmpty()) {
							_xhtml_out.print("<div id=\"accordionAgg\">");
							for(Map<String, String> _vg : _vgs) {
								_xhtml_out.print("<div class=\"aggr-title\">");
								_xhtml_out.print(_vg.get("name"));
								_xhtml_out.print(" <em>(");
								_xhtml_out.print(_vg.get("size"));
								_xhtml_out.print(" - ");
								_xhtml_out.print(_vg.get("pv"));
								_xhtml_out.print(" ");
								_xhtml_out.print(getLanguageMessage("device.volume_groups.pvs"));
								_xhtml_out.print(")</em>");
								if(_vg.get("type") != null && "lvm".equalsIgnoreCase(_vg.get("type") )) {
									_xhtml_out.print(" ");
									_xhtml_out.print(getLanguageMessage("device.volume_groups.vgtype.lvm"));
								} else if(_vg.get("type") != null && "zfs".equalsIgnoreCase(_vg.get("type") )) {
									_xhtml_out.print(" ");
									_xhtml_out.print(getLanguageMessage("device.volume_groups.vgtype.zfs"));
								}
								
								if (CloudManager.isCloudGroup(_vg.get("name"))) {
									_xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
				                    _xhtml_out.print(STATS_CLOUD_DISK);
				                    _xhtml_out.print("&name=");
				                    _xhtml_out.print(_vg.get("name"));
				                    _xhtml_out.print("\"><img src=\"/images/cloud_16.png\" title=\"");
				                    _xhtml_out.print(getLanguageMessage("device.cloud.stats"));
				                    _xhtml_out.print("\" alt=\"");
				                    _xhtml_out.print(getLanguageMessage("device.cloud.stats"));
				                    _xhtml_out.println("\"/></a>");
								}
								_xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
			                    _xhtml_out.print(REMOVE_VG);
			                    _xhtml_out.print("&name=");
			                    _xhtml_out.print(_vg.get("name"));
			                    _xhtml_out.print("\"><img src=\"/images/drive_delete_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
			                    _xhtml_out.println("\"/></a>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
			                    _xhtml_out.print(EDIT_VG);
			                    _xhtml_out.print("&name=");
			                    _xhtml_out.print(_vg.get("name"));
			                    _xhtml_out.print("\"><img src=\"/images/drive_link_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
			                    _xhtml_out.println("\"/></a>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
			                    _xhtml_out.print(UMOUNT_VG);
			                    _xhtml_out.print("&group=");
			                    _xhtml_out.print(_vg.get("name"));
			                    _xhtml_out.print("\"><img src=\"/images/drive_go_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("device.shares.umount"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("device.shares.umount"));
			                    _xhtml_out.println("\"/></a>");

			                    _xhtml_out.println("</div>");
			                    List<Map<String, String>> aggVols = new ArrayList<Map<String, String>>();
			                    for(Map<String, String> _lv : _lvs) {
									if(_lv.get("vg") != null && _lv.get("vg").equals(_vg.get("name"))) {
										aggVols.add(_lv);
									}
			                    }
			                    
			                    List<Map<String, String>> _vg_disks = VolumeManager.getPhysicalVolumes(_vg.get("name"));

			                    _xhtml_out.print("<div style=\"padding:0px;border:0px;\">");
			                    //pageJS += generateJSPieHighGroup(_xhtml_out, numAgg, aggVols, _vg, _vg_disks);
			                    
			                    String clasecss = "vol-vertical-container-bars";
			        	    	if (_vg.get("dedupAllocCompress-raw") != null &&  _vg.get("totalSavedSpace-raw") != null)
			        	    		clasecss = "vol-vertical-container-bars-three";
				        	    if (_vg.get("type").equals("zfs")) {
				        	    	pageJS+=generateJSPieHighGroupOcupedLogical(_xhtml_out, _vg, clasecss, numAgg);
				        	    	_xhtml_out.println("<div id=\"agg-dedup-bar"+numAgg+"\" class=\""+clasecss+"\" style=\"margin:0px;overflow:hidden;\"></div>");
				        	    	if (_vg.get("dedupAllocCompress-raw") != null &&  _vg.get("totalSavedSpace-raw") != null) {
				        	    		pageJS+=getDeduplicationBar("agg-dedup-bar"+numAgg, getLanguageMessage("device.volume_groups.dedupbar.title"), getLanguageMessage("device.volume_groups.dedupbar.title.space.nodedup"), _vg.get("name"), _vg.get("dedupAllocCompress"), _vg.get("dedupAllocCompress-raw"), _vg.get("totalSavedSpace"), _vg.get("totalSavedSpace-raw"),getLanguageMessage("device.volume_groups.ocuped.saved"), getLanguageMessage("device.volume_groups.dedupbar.used.dedup"));
				        	    	}
				        	    } else {
				        	    	clasecss = "";
				        	    }
				        	    pageJS+=generateJSPieHighGroupOcupedPhysical(_xhtml_out, _lvs, _vg, _vg_disks, clasecss, numAgg);
				        	    
			                    _xhtml_out.println("<div class=\"clear\"></div>");
			                    _xhtml_out.print("</div>");
			                    numAgg++;
							}
							_xhtml_out.print("</div>");
						} else {
							_xhtml_out.println("<fieldset>");
		                    _xhtml_out.println("<table>");
							_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_vgs"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.print("<br/>");
		                    _xhtml_out.println("</fieldset>");
						}
						_xhtml_out.println("<div class=\"clear\" style=\"margin-top:15px;\"/></div>");
	        	    	_xhtml_out.print("</div>");

	        	    	_xhtml_out.println("<div style=\"margin:20px auto;width:94%;\">");
	                    _xhtml_out.println("<div id=\"listadoVols\" style=\"clear:both;width:100%;margin:auto;\"><table id=\"tablaVols\" style=\"margin-left:0px;margin-right:0px;\"></table><div id='pager' ></div></div>");
		        	    _xhtml_out.print("</div>");
		        	    
	        	    	pageJSFuncs+=allLoad("tablaVols", _lvs);
	                 	pageJSFuncs+=emptyGridFuncJS("tablaVols");
		        	    pageJS+=getJqGridJS("tablaVols", "listadoVols");
	                 	pageJS+="reloadAll();\n";	
	        	    	
	        	    	 _xhtml_out.print("<div id=\"tooltip\"></div>");

	        	    	pageJS += "$( '#accordionAgg' ).accordion({collapsible:true, heightStyle: 'content'});\n";
	        	    	pageJS +="$('#accordionAgg div a').click(function() {\n";
	        	    	pageJS +="window.location = $(this).attr('href');\n";
	        	    	pageJS +="return false;\n";
	        	    	pageJS +="});\n";
					}
        		    break;
	    		case NEW_VG: {
	    				_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    				
	    				int _offset = 0;
	    				List<Map<String, String>> _disks = VolumeManager.getUnassignedPhysicalDisks();
	    				List<Map<String, String>> _cloud_disks = _cm.getUnassignedCloudDisks();
	    				
	    				writeDocumentBack("/admin/DeviceDisk");
	    				_xhtml_out.print("<form action=\"/admin/DeviceDisk\" name=\"device\" id=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_VG + "\"/>");
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.disk"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.volume_groups.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.new_vg"));
	                    _xhtml_out.print("<a href=\"javascript:if ($('#device').validationEngine('validate')) submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"name\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.volume_groups.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    _xhtml_out.print("<label for=\"name\">");
		        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.vgtype"));
		        	    _xhtml_out.println(": </label>");
		        	    _xhtml_out.println("<select class=\"form_select\" name=\"vgtype\" id=\"vgtype\" onChange=\"showZFS()\">");
		        	    _xhtml_out.print("<option value=\"lvm\">");
		        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.vgtype.lvm"));
		        	    _xhtml_out.println("</option>");
		        	    _xhtml_out.print("<option value=\"zfs\" selected=\"selected\">");
		        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.vgtype.zfs"));
		        	    _xhtml_out.print(" ("+getLanguageMessage("device.volume_groups.vgtype.zfs_allows_dedup")+")");
		        	    _xhtml_out.println("</option>");
		        	    _xhtml_out.println("</select>");
		        	    _xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    _xhtml_out.println("</div>");
		        	    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"local\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.volume_groups.local"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input type=\"checkbox\" value=\"yes\" class=\"form_checkbox\" name=\"local\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.pvs"));
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
	                    _xhtml_out.println("<table>");
	                    if(_disks.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_unassigned_disks"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    } else {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.println("<td></td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv.model"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print("<div class=\"zfs\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.cache"));
		                    _xhtml_out.print("</div>");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print("<div class=\"zfs\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.log"));
		                    _xhtml_out.print("</div>");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _disk : _disks) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("deviceId"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("vendor"));
			                    _xhtml_out.println(" / ");
			                    _xhtml_out.println(_disk.get("model"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td><input class=\"validate[minCheckbox[1]] form_checkbox\" type=\"checkbox\" name=\"device\" id=\"device_"+_offset+"\" onClick=\"uncheckBros('device_"+_offset+"','log_"+_offset+"','cache_"+_offset+"')\" value=\"");
			                    _xhtml_out.print(_disk.get("deviceId"));
			                    _xhtml_out.println("\"/></td>");
			                    _xhtml_out.print("<td><input class=\"form_checkbox zfs\" type=\"checkbox\" name=\"cache\" id=\"cache_"+_offset+"\" onClick=\"uncheckBros('cache_"+_offset+"','device_"+_offset+"','log_"+_offset+"')\" value=\"");
			                    _xhtml_out.print(_disk.get("deviceId"));
			                    _xhtml_out.println("\"/></td>");
			                    _xhtml_out.print("<td><input class=\"form_checkbox zfs\" type=\"checkbox\" name=\"log\" id=\"log_"+_offset+"\" onClick=\"uncheckBros('log_"+_offset+"','device_"+_offset+"','cache_"+_offset+"')\" value=\"");
			                    _xhtml_out.print(_disk.get("deviceId"));
			                    _xhtml_out.println("\"/></td>");
			                    
		                    	_xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.cloud.disks"));
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
				        _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
	                    _xhtml_out.print(NEW_CLOUD_DISK);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
                        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<table>");
	                    if(_cloud_disks.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_unassigned_cloud_disks"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    } else {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.println("<td></td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.cloud.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _disk : _cloud_disks) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("device"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print(_disk.get("type"));
			                    if (_disk.get("type").equals("S3"))
			                    	 _xhtml_out.print(" ("+_disk.get("bucket")+") ");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"device\" value=\"");
			                    _xhtml_out.print(_disk.get("device"));
			                    _xhtml_out.println("\"/></td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceDisk?type="+REMOVE_CLOUD_DISK+"&cld="+_disk.get("cld")+"&device="+_disk.get("device")+"&account="+_disk.get("account")+"\" >");
			                    _xhtml_out.print("<img src=\"/images/drive_delete_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
			                    _xhtml_out.println("\"/>");
		                    	_xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	    				_xhtml_out.print("</form>");
	    				pageJSFuncs+=getJSDisableDeviceChekcs();
	    				pageJSFuncs+=getJSShowZFS();
	    				pageJS+= "$('#device').validationEngine();"; 
	    			}
	    			break;
	    		case EDIT_VG: {
		    			int _offset = 1;
	    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
		    			
	    				List<Map<String, String>> _disks = VolumeManager.getUnassignedPhysicalDisks();
	    				List<Map<String, String>> _vg_disks = VolumeManager.getPhysicalVolumes(request.getParameter("name"));
	    				List<Map<String, String>> _cloud_disks = _cm.getUnassignedCloudDisks();
	    				Map<String, String> vgroup = VolumeManager.getVolumeGroup(request.getParameter("name"));
	    				List<Map<String, String>> _lvs = VolumeManager.getLogicalVolumes(request.getParameter("name"));
	    				DecimalFormat _df = new DecimalFormat("#.##");
	    				_df.setDecimalSeparatorAlwaysShown(false);
	    				
	    				_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/highcharts.js\"></script>");
	    				writeDocumentBack("/admin/DeviceDisk");
	    				_xhtml_out.print("<form action=\"/admin/DeviceDisk\" name=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_VG + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name")+ "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"vgtype\" value=\"" + vgroup.get("type") + "\"/>");
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.disk"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.volume_groups.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(vgroup.get("name"));
	                    _xhtml_out.println("</h2>");
	                    
						_xhtml_out.print("<div class=\"vol-vertical-container-info\">");
						_xhtml_out.print("<span>");
		        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.name"));
		        	    _xhtml_out.println(": </span>");
		        	    _xhtml_out.println(vgroup.get("name"));
		        	    _xhtml_out.println("<br/><br/>");
		        	    _xhtml_out.print("<span>");
		        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
		        	    _xhtml_out.println(": </span>");
		        	    _xhtml_out.println(vgroup.get("size"));
		        	    _xhtml_out.println("<br/><br/>");
		        	    _xhtml_out.print("<span>");
		        	    if (vgroup.get("type").equals("zfs"))
		        	    	_xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.ocuped.info")));
		        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.ocuped"));
		        	    _xhtml_out.println(": </span>");
		        	    if (vgroup.get("type").equals("zfs"))
		        	    	_xhtml_out.println(vgroup.get("allocated"));
		        	    else {
		        	    	_xhtml_out.println(VolumeManager.getFormatSize(VolumeManager.getXFSAggOcupped(_vg_disks, vgroup)+" B"));
		        	    }
		        	    _xhtml_out.println("<br/><br/>");
		        	    _xhtml_out.print("<span>");
		        	    if (vgroup.get("type").equals("zfs"))
		        	    	_xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.free.info")));
		        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.free"));
		        	    _xhtml_out.println(": </span>");
		        	    _xhtml_out.println(vgroup.get("free"));
		        	    _xhtml_out.println("<br/><br/>");
		        	    if (vgroup.get("type").equals("zfs")) {
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.ocuped.fs.info")));
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.ocuped.fs"));
			        	    _xhtml_out.println(": </span>");
			        	    _xhtml_out.println(vgroup.get("fs-used"));
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.free.fs.info")));
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.free.fs"));
			        	    _xhtml_out.println(": </span>");
			        	    _xhtml_out.println(vgroup.get("fs-free"));
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.println("</div>");
			        	    _xhtml_out.print("<div class=\"vol-vertical-container-info\">");
		        	    	String compressRatio = ZFSConfiguration.getGroupCompressRatio(_lvs);
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.dedup"));
			        	    _xhtml_out.println(": </span>");
			        	    _xhtml_out.println(vgroup.get("dedup-ratio")+"x");
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.compress"));
			        	    _xhtml_out.println(": </span>");
			        	    _xhtml_out.println(compressRatio+"x");
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.saved.ratio.info")));
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.saved.ratio"));
			        	    _xhtml_out.println(": </span>");
			        	    _xhtml_out.println(_df.format(Double.parseDouble(compressRatio.replace(",","."))*Double.parseDouble(vgroup.get("dedup-ratio")))+"x");
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.dedup.unique.info")));
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.dedup.unique"));
			        	    _xhtml_out.println(": </span>");
			        	    if (vgroup.get("dedupAllocCompress") != null)
			        	    	_xhtml_out.println(vgroup.get("dedupAllocCompress"));
			        	    else
			        	    	_xhtml_out.println("--");
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.ocuped.nodedup.info")));
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.ocuped.nodedup"));
			        	    _xhtml_out.println(": </span>");
			        	    if (vgroup.get("dedupReferenced") != null)
			        	    	_xhtml_out.println(vgroup.get("dedupReferenced"));
			        	    else
			        	    	_xhtml_out.println("--");
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.print("<span>");
			        	    _xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.volume_groups.ocuped.saved.info")));
			        	    _xhtml_out.print(getLanguageMessage("device.volume_groups.ocuped.saved"));
			        	    _xhtml_out.println(": </span>");
			        	    if (vgroup.get("totalSavedSpace") != null)
			        	    	_xhtml_out.println(vgroup.get("totalSavedSpace"));
			        	    else
			        	    	_xhtml_out.println("--");
			        	    _xhtml_out.println("<br/><br/>");
			        	    _xhtml_out.println("</div>");
		        	    } else
		        	    	_xhtml_out.println("</div>");
		        	    String clasecss = "vol-vertical-container-bars";
	        	    	if (vgroup.get("dedupAllocCompress-raw") != null &&  vgroup.get("totalSavedSpace-raw") != null)
	        	    		clasecss = "vol-vertical-container-bars-three";
		        	    if (vgroup.get("type").equals("zfs")) {
		        	    	pageJS+=generateJSPieHighGroupOcupedLogical(_xhtml_out, vgroup, clasecss, 1);
		        	    	_xhtml_out.println("<div id=\"agg-dedup-bar\" class=\""+clasecss+"\" style=\"margin:0px;overflow:hidden;\"></div>");
		        	    	if (vgroup.get("dedupAllocCompress-raw") != null &&  vgroup.get("totalSavedSpace-raw") != null) {
		        	    		pageJS+=getDeduplicationBar("agg-dedup-bar", getLanguageMessage("device.volume_groups.dedupbar.title"), getLanguageMessage("device.volume_groups.dedupbar.title.space.nodedup"), vgroup.get("name"), vgroup.get("dedupAllocCompress"), vgroup.get("dedupAllocCompress-raw"), vgroup.get("totalSavedSpace"), vgroup.get("totalSavedSpace-raw"),getLanguageMessage("device.volume_groups.ocuped.saved"), getLanguageMessage("device.volume_groups.dedupbar.used.dedup"));
		        	    	}
		        	    }
		        	    pageJS+=generateJSPieHighGroupOcupedPhysical(_xhtml_out, _lvs, vgroup, _vg_disks, clasecss, 1);
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.member_pvs"));
	                    _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<table>");
	                    _xhtml_out.println("<tr>");
	                    _xhtml_out.println("<td></td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("device.volume_groups.free"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.println("</tr>");
	                    for(Map<String, String> _disk : _vg_disks) {
	                    	_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td><img src=\"/images/drive_accept_16.png\"/></td>");
	                    	_xhtml_out.println("<td>");
		                    _xhtml_out.println(_disk.get("device"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("<td>");
		                    _xhtml_out.println(_disk.get("size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("<td>");
		                    _xhtml_out.println(_disk.get("free"));
		                    _xhtml_out.println("</td>");
	                    	_xhtml_out.println("</tr>");
	                    	_offset++;
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
		    			
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.pvs"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<table>");
	                    if(_disks.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_unassigned_disks"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    } else {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.println("<td></td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv.model"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    if (vgroup.get("type").equals("zfs")) {
		                    	_xhtml_out.print(getLanguageMessage("device.volume_groups.cache"));
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    if (vgroup.get("type").equals("zfs")) {
		                    	_xhtml_out.print(getLanguageMessage("device.volume_groups.log"));
		                    }
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset = 0;
		                    for(Map<String, String> _disk : _disks) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
		                    	_xhtml_out.println("<td>");
		                    	if (_disk.get("deviceId") != null && !_disk.get("deviceId").isEmpty())
		                    		_xhtml_out.println(_disk.get("deviceId"));
		                    	else
		                    		_xhtml_out.println(_disk.get("device"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("model"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"device\" id=\"device_"+_offset+"\" onClick=\"uncheckBros('device_"+_offset+"','log_"+_offset+"','cache_"+_offset+"')\"  value=\"");
			                    _xhtml_out.print(_disk.get("deviceId"));
			                    _xhtml_out.println("\"/></td>");
		                    	_xhtml_out.print("<td>");
		                    	if (vgroup.get("type").equals("zfs")) {
		                    		_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"cache\" id=\"cache_"+_offset+"\" onClick=\"uncheckBros('cache_"+_offset+"','device_"+_offset+"','log_"+_offset+"')\" value=\"");
		                    		_xhtml_out.print(_disk.get("deviceId"));
		                    		_xhtml_out.println("\"/>");
		                    	}
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td>");
		                    	if (vgroup.get("type").equals("zfs")) {
		                    		_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"log\" id=\"log_"+_offset+"\" onClick=\"uncheckBros('log_"+_offset+"','device_"+_offset+"','cache_"+_offset+"')\" value=\"");
		                    		_xhtml_out.print(_disk.get("deviceId"));
			                    	_xhtml_out.println("\"/>");
		                    	}
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    if (vgroup.get("type").equals("zfs")) {
	                    	_xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.volume_groups.member_cache"));
		                    _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
					        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					        _xhtml_out.print("\" alt=\"");
					        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					        _xhtml_out.println("\"/></a>");
		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.println("<table>");
		                    List<Map<String, String>> _vg_cache_disks = ZFSConfiguration.getVolumeGroupCacheDisks(vgroup.get("name"));
		                    if (_vg_cache_disks != null && !_vg_cache_disks.isEmpty()) {
			                    _xhtml_out.println("<tr>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print("--");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    
			                    for(Map<String, String> _disk : _vg_cache_disks) {
			                    	_xhtml_out.print("<tr");
			                    	if(_offset % 2 == 0) {
			                    		_xhtml_out.print(" class=\"highlight\"");
			                    	}
			                    	_xhtml_out.println(">");
			                    	_xhtml_out.println("<td><img src=\"/images/drive_accept_16.png\"/></td>");
			                    	_xhtml_out.println("<td>");
				                    _xhtml_out.println(_disk.get("device"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println(_disk.get("size"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println("<a href=\"/admin/DeviceDisk?type="+REMOVE_POOL_DISK+"&diskId="+_disk.get("device")+"&groupName="+vgroup.get("name")+"\">");
				                    _xhtml_out.println("<img src=\"/images/delete_16.png\" title=\"");
							        _xhtml_out.print(getLanguageMessage("common.message.remove"));
							        _xhtml_out.print("\" alt=\"");
							        _xhtml_out.print(getLanguageMessage("common.message.remove"));
							        _xhtml_out.println("\"/>");
				                    _xhtml_out.println("</a>");
				                    _xhtml_out.println("</td>");
			                    	_xhtml_out.println("</tr>");
			                    	_offset++;
			                    }
		                    } else {
		                    	_xhtml_out.println("<tr>");
		                    	_xhtml_out.println("<td>");
		                    	_xhtml_out.println(getLanguageMessage("device.message.no_cache_disks"));
		                    	_xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
		                    }
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
		                    
		                    _xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.volume_groups.member_log"));
		                    _xhtml_out.println("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
					        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					        _xhtml_out.print("\" alt=\"");
					        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					        _xhtml_out.println("\"/></a>");
		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.println("<table>");
		                    List<Map<String, String>> _vg_log_disks = ZFSConfiguration.getVolumeGroupLogDisks(vgroup.get("name"));
		                    if (_vg_log_disks != null && !_vg_log_disks.isEmpty()) {
			                    _xhtml_out.println("<tr>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print("--");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
			                    for(Map<String, String> _disk : _vg_log_disks) {
			                    	_xhtml_out.print("<tr");
			                    	if(_offset % 2 == 0) {
			                    		_xhtml_out.print(" class=\"highlight\"");
			                    	}
			                    	_xhtml_out.println(">");
			                    	_xhtml_out.println("<td><img src=\"/images/drive_accept_16.png\"/></td>");
			                    	_xhtml_out.println("<td>");
				                    _xhtml_out.println(_disk.get("device"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println(_disk.get("size"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println("<a href=\"/admin/DeviceDisk?type="+REMOVE_POOL_DISK+"&diskId="+_disk.get("device")+"&groupName="+vgroup.get("name")+"\">");
				                    _xhtml_out.println("<img src=\"/images/delete_16.png\" title=\"");
							        _xhtml_out.print(getLanguageMessage("common.message.remove"));
							        _xhtml_out.print("\" alt=\"");
							        _xhtml_out.print(getLanguageMessage("common.message.remove"));
							        _xhtml_out.println("\"/>");
				                    _xhtml_out.println("</a>");
				                    _xhtml_out.println("</td>");
			                    	_xhtml_out.println("</tr>");
			                    	_offset++;
			                    }
		                    } else {
		                    	_xhtml_out.println("<tr>");
		                    	_xhtml_out.println("<td>");
		                    	_xhtml_out.println(getLanguageMessage("device.message.no_log_disks"));
		                    	_xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
		                    }
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
	                    }

	                    _xhtml_out.print("</form>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.cloud.disks"));
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
				        _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
	                    _xhtml_out.print(NEW_CLOUD_DISK);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
                        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.add"));
				        _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<table>");
	                    if(_cloud_disks.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_unassigned_cloud_disks"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
	                    } else {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.println("<td></td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.pv"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.cloud.type"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("device.volume_groups.size"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> _disk : _cloud_disks) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.println("<td><img src=\"/images/drive_16.png\"/></td>");
		                    	_xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("device"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.print(_disk.get("type"));
			                    if (_disk.get("type").equals("S3"))
			                    	 _xhtml_out.print(" ("+_disk.get("bucket")+") ");
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.println("<td>");
			                    _xhtml_out.println(_disk.get("size"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td><input class=\"form_checkbox\" type=\"checkbox\" name=\"device\" value=\"");
			                    _xhtml_out.print(_disk.get("device"));
			                    _xhtml_out.println("\"/></td>");
			                    _xhtml_out.print("<td>");
			                    _xhtml_out.print("<a href=\"/admin/DeviceDisk?type="+REMOVE_CLOUD_DISK+"&cld="+_disk.get("cld")+"&device="+_disk.get("device")+"&account="+_disk.get("account")+"\" >");
			                    _xhtml_out.print("<img src=\"/images/drive_delete_16.png\" title=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
			                    _xhtml_out.print("\" alt=\"");
			                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
			                    _xhtml_out.println("\"/>");
			                    _xhtml_out.print("</a>");
			                    _xhtml_out.print("</td>");
		                    	_xhtml_out.println("</tr>");
		                    	_offset++;
		                    }
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    
	                    
	                    _xhtml_out.print("<form action=\"/admin/DeviceDisk\" name=\"devicelocal\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + UPDATELOCAL_VG + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name")+ "\"/>");
	    				
	                    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.attributes"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.devicelocal.submit());\"><img src=\"/images/disk_16.png\" title=\"");
				        _xhtml_out.print(getLanguageMessage("device.message.volume_groups.updated"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("device.message.volume_groups.updated"));
				        _xhtml_out.println("\"/></a>");
						_xhtml_out.println("</h2>");
		        	    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"local\">");
	        	    	_xhtml_out.print(getLanguageMessage("device.volume_groups.local"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input type=\"checkbox\" value=\"yes\" class=\"form_checkbox\" name=\"local\" ");
	        	    	if (VolumeManager.isLocalDeviceGroup(request.getParameter("name")))
	        	    		_xhtml_out.print("checked=\"checked\"");
	        	    	_xhtml_out.print(" />");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	
	        	    	_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.volume_groups.distribution.use.byvolume"));
	        	    	_xhtml_out.println("</h2>");
	        	    	if (_lvs != null && !_lvs.isEmpty())
							pageJS += getJSVolumeOcupationHorizontalBars("volsHorizontal", _lvs);
						else
							_xhtml_out.println(getLanguageMessage("device.message.no_lvs")+"<br />");
	        	    	_xhtml_out.println("<div id =\"volsHorizontal\"></div>");	
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	        	    	_xhtml_out.println("</div>");
	    				_xhtml_out.print("</form>");
	    				if (vgroup.get("type").equals("zfs"))
	    					pageJSFuncs+=getJSDisableDeviceChekcs();
	    			}
	    			break;
	    		case UPDATELOCAL_VG: {
		    			if (HAConfiguration.inCluster())
		    				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha_master"));
		    			
		    			if(request.getParameter("local") != null && "yes".equalsIgnoreCase(request.getParameter("local"))) {
	    					VolumeManager.setLocalDeviceGroup(request.getParameter("name"));
	    				} else {
	    					VolumeManager.setNoLocalDeviceGroup(request.getParameter("name"));
	    				}
		    			writeDocumentResponse(getLanguageMessage("device.message.volume_groups.updated"), "/admin/DeviceDisk");
	    			}
	    			break;
	    		case STORE_VG: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
	    				int _vg_type = VolumeManager.VG_LVM;
	    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
	    				if(request.getParameter("vgtype") == null || "zfs".equalsIgnoreCase(request.getParameter("vgtype"))) {
	    					_vg_type = VolumeManager.VG_ZFS;
	    				}
	    				
	    				if(request.getParameter("confirm") != null) {
		    				if(request.getParameter("local") != null && "yes".equalsIgnoreCase(request.getParameter("local"))) {
			    				if (HAConfiguration.inCluster()) {
			    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha_master"));
			    				}
		    					VolumeManager.setLocalDeviceGroup(request.getParameter("name"));
		    				} else {
		    					VolumeManager.setNoLocalDeviceGroup(request.getParameter("name"));
		    				}
		    				
		    				if(request.getParameterValues("device") != null && request.getParameterValues("device").length > 0) {
		    					VolumeManager.addVolumeGroupDevice(_vg_type, request.getParameter("name"), request.getParameterValues("device"));
		    				}
		    				if (_vg_type == VolumeManager.VG_ZFS) {
		    					if(request.getParameterValues("cache") != null && request.getParameterValues("cache").length > 0) {
		    						ZFSConfiguration.setPoolCacheDisks(request.getParameter("name"), Arrays.asList(request.getParameterValues("cache")));
		    					}
		    					if(request.getParameterValues("log") != null && request.getParameterValues("log").length > 0) {
		    						ZFSConfiguration.setPoolLogDisks(request.getParameter("name"), Arrays.asList(request.getParameterValues("log")));
		    					}
		    				}
		    				writeDocumentResponse(getLanguageMessage("device.message.volume_groups.added"), "/admin/DeviceDisk");
	    				} else {
	    					String query = "";
	    					if(request.getParameterValues("device") != null && request.getParameterValues("device").length > 0) {
	    						for (String device : Arrays.asList(request.getParameterValues("device"))) {
	    							query+="&device="+device;
	    						}
	    					}
	    					String message = getLanguageMessage("device.message.volume_groups.add.question");
	    					if (_vg_type == VolumeManager.VG_ZFS) {
		    					if( (request.getParameterValues("cache") != null && request.getParameterValues("cache").length > 0) || (request.getParameterValues("log") != null && request.getParameterValues("log").length > 0)) {
		    						message += " "+getLanguageMessage("device.message.volume_groups.add.cachelog.disclaimer");
		    						if (request.getParameterValues("cache") != null && request.getParameterValues("cache").length > 0) {
		    							for (String cache : Arrays.asList(request.getParameterValues("cache"))) {
		    								query+="&cache="+cache;
		    							}
		    						}
		    						if (request.getParameterValues("log") != null && request.getParameterValues("log").length > 0) {
		    							for (String log : Arrays.asList(request.getParameterValues("log"))) {
		    								query+="&log="+log;
		    							}
		    						}
		    							
		    					}
	    					}
	    					writeDocumentQuestion(message, "/admin/DeviceDisk?type=" + STORE_VG + "&name=" + request.getParameter("name") + "&confirm=true&vgtype="+request.getParameter("vgtype")+"&local="+request.getParameter("local")+query, null);
	    				}
	    			}
	    			break;
	    		case REMOVE_VG: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
	    				
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
		    			
	    				if (VolumeManager.isLocalDeviceGroup(request.getParameter("name")) && HAConfiguration.inCluster()) {
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha_master"));
	    				}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				VolumeManager.setNoLocalDeviceGroup(request.getParameter("name"));
		    				VolumeManager.removeVolumeGroup(request.getParameter("name"));
				    		writeDocumentResponse(getLanguageMessage("device.message.volume_groups.removed"), "/admin/DeviceDisk");
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("device.volume_groups.question_aggregate"), "/admin/DeviceDisk?type=" + REMOVE_VG + "&name=" + request.getParameter("name") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case NEW_LV: {
	    				_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    				
	    				List<String> _vgs = VolumeManager.getVolumeGroupNames();
	    				if(_vgs.size() == 0) {
	    					writeDocumentResponse(getLanguageMessage("device.message.no_vgs"), "/admin/DeviceDisk");
	    				} else {
	    					char units = 'M';
		    				if(request.getParameter("units") != null && !request.getParameter("units").isEmpty()) {
		    					units = request.getParameter("units").charAt(0); 
		    				}
		    				
		    				Map<String, String> _vg = null;
		    				if (request.getParameter("group") != null && !request.getParameter("group").isEmpty()) {
		    					_vg = VolumeManager.getVolumeGroup(request.getParameter("group"));
		    				} else {
		    					_vg = VolumeManager.getVolumeGroup(_vgs.get(0));
		    				}
		    				
		    				writeDocumentBack("/admin/DeviceDisk");
		    				_xhtml_out.print("<form action=\"/admin/DeviceDisk\" name=\"device\" id=\"device\" method=\"post\">");
		    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_LV + "\"/>");
		    				_xhtml_out.println("<h1>");
		    				_xhtml_out.print("<img src=\"/images/drive_32.png\"/>");
			    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.disk"));
		    				_xhtml_out.println("</h1>");
			    			_xhtml_out.print("<div class=\"info\">");
			    			_xhtml_out.print(getLanguageMessage("device.volume_groups.info_management"));
			    			_xhtml_out.println("</div>");
			    			
			    			_xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.logical_volumes.new_vl"));
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
		        	    	_xhtml_out.print("<label for=\"name\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vg"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"group\" onChange=\"document.location.href='/admin/DeviceDisk?type=" + NEW_LV + "&group='+this.value;\">");
		        	    	for(String vg : _vgs) {
		        	    		_xhtml_out.print("<option value=\"");
		        	    		_xhtml_out.print(vg);
		        	    		_xhtml_out.print("\" ");
		        	    		if (vg.equals(_vg.get("name")))
		        	    			_xhtml_out.print("selected=\"selected\" ");
		        	    		_xhtml_out.print(";>");
		        	    		_xhtml_out.print(vg);
		        	    		_xhtml_out.println("</option>");
		        	    	}
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	        	    	    _xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"name\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.name"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"validate[required,custom[onlyLetterNumber]] form_text\" name=\"name\"/>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"clear\"></div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"storage_type\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.storage_type"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"storage_type\">");
		        	    	_xhtml_out.println("<option value=\"N\">NAS</option>");
		        	    	_xhtml_out.println("<option value=\"S\">SAN</option>");
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"clear\"></div>");
		        	    	if (!_vg.get("type").equals("zfs")) {
			        	    	_xhtml_out.println("<div class=\"standard_form\">");
			        	    	_xhtml_out.print("<label for=\"sizelv_type\">");
			        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.lv_type"));
			        	    	_xhtml_out.println(": </label>");
			        	    	_xhtml_out.println("<select class=\"form_select\" name=\"lv_type\">");
			        	    	_xhtml_out.println("<option value=\"S\">Striped</option>");
			        	    	_xhtml_out.println("<option value=\"L\">Linear</option>");
			        	    	_xhtml_out.println("<option value=\"M\">Mirror</option>");
			        	    	_xhtml_out.println("</select>");
			        	    	_xhtml_out.println("</div>");
			        	    	_xhtml_out.println("<div class=\"clear\"></div>");
		        	    	}
		        	    	if (_vg.get("type").equals("zfs")) {
			        	    	_xhtml_out.println("<div class=\"standard_form\">");
			        	    	_xhtml_out.print("<label for=\"compression\">");
			        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.storage_fstype.compression"));
			        	    	_xhtml_out.println(": </label>");
			        	    	_xhtml_out.println("<select class=\"form_select\" name=\"compression\">");
			        	    	_xhtml_out.print("<option value=\"none\">");
			        	    	_xhtml_out.print(getLanguageMessage("common.message.noone"));
			        	    	_xhtml_out.print("</option>");
			        	    	_xhtml_out.print("<option value=\"gzip\">GZIP</option>");
			        	    	_xhtml_out.print("<option value=\"lzjb\">LZJB</option>");
			        	    	_xhtml_out.print("<option value=\"zle\">ZLE</option>");
			        	    	_xhtml_out.println("</select>");
			        	    	_xhtml_out.println("</div>");
	    					}
		        	    	_xhtml_out.println("<div class=\"standard_form\" style=\"width:480px\">");
		        	    	_xhtml_out.print("<label for=\"size\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.size"));
		        	    	_xhtml_out.println(": </label>");
		        	    	Double maxExtend = 0D;
		        	    	Double exSizeMaxMega = 0D;
	                		Double exSizeMaxGiga = 0D;
	        				Double exSizeMaxTera = 0D;
		        	    	if (_vg != null) {
			        	    	Double maxAggSpace = Double.parseDouble(_vg.get("free-raw"));
			        	    	if (_vg.get("type") != null && _vg.get("type").equals("zfs")) {
			        	    		Double fsFree =  Double.parseDouble(_vg.get("fs-free-raw"));
			        	    		if (fsFree < maxAggSpace)
			        	    			maxAggSpace = fsFree;
			        	    	}
			        	    	maxExtend = getMaxForSlider(maxAggSpace.toString(), units);
			        	    	exSizeMaxMega = getMaxForSlider(maxAggSpace.toString(), 'M');
			                	exSizeMaxGiga = getMaxForSlider(maxAggSpace.toString(), 'G');
			        			exSizeMaxTera = getMaxForSlider(maxAggSpace.toString(), 'T');	
		        	    	}
		        	    	_xhtml_out.println("<input class=\"validate[required,custom[integer],min[0],max["+maxExtend+"]] form_text\" name=\"size\" id=\"size\" style=\"border: 0; color: #f6931f; font-weight: bold;width:50px\" />");	        				
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"size_units\" id=\"size_units\" onChange=\"adjustSizeSlider()\">");
		        	    	_xhtml_out.println("<option value=\"M\">MB</option>");
		        	    	_xhtml_out.println("<option value=\"G\">GB</option>");
		        	    	_xhtml_out.println("<option value=\"T\">TB</option>");
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("<div class=\"clear\"></div>");
		        	    	_xhtml_out.println("<div id=\"slider\" style=\"margin-left:245px;margin-top:3px;margin-bottom:5px;\"> </div>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"clear\"></div>");
		        	    	
		        	    	if (_vg.get("type").equals("zfs")) {
			        	    	_xhtml_out.println("<div class=\"standard_form\" style=\"width:480px\">");
			        	    	_xhtml_out.print("<label for=\"size\">");
			        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.percent_snap"));
			        	    	_xhtml_out.println(": </label>");
			        	    	_xhtml_out.println("<input class=\"validate[required,custom[integer],min[0],max[60]] form_text\" name=\"percent_snap\" id=\"percent_snap\" value=\"25\" style=\"border: 0; color: #f6931f; font-weight: bold;width:15px\" /> % ");	        				
			        	    	_xhtml_out.println("<div class=\"clear\"></div>");
			        	    	_xhtml_out.println("<div id=\"sliderSnap\" style=\"margin-left:245px;margin-top:3px;margin-bottom:5px;\"> </div>");
			        	    	_xhtml_out.println("</div>");
			        	    	_xhtml_out.println("<div class=\"clear\"></div>");
			        	    	_xhtml_out.println("<div class=\"standard_form\" >");
			        	    	_xhtml_out.print("<label for=\"deduplication\">");
			        	    	_xhtml_out.print(HtmlFormUtils.getInfoTooltip(getLanguageMessage("device.logical_volumes.storage_deduplication.warning"))); 
			        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.storage_deduplication"));
			        	    	_xhtml_out.println(": </label>");
			        	    	_xhtml_out.println("<input class=\"form_checkbox\" name=\"deduplication\" type=\"checkbox\" value=\"true\"/>");
			        	    	_xhtml_out.println("</div>");
		        	    	}
		        	    	_xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
	                    
		        	    	pageJS += getSizeSliderSimpleJS("slider", "size", 0D, maxExtend, 0D);
		        	    	pageJS += getSizeSliderSimpleJS("sliderSnap", "percent_snap", 0D, 60D, 25D);
		        	    	pageJSFuncs += getAdjustSliderFuncSimple("adjustSizeSlider","slider", "size_units", "size", exSizeMaxMega, exSizeMaxGiga, exSizeMaxTera);
		        	    	pageJS+= "$('#device').validationEngine();";
		                    _xhtml_out.print("</form>");
		    			}
	    			}
	    			break;
	    		case STORE_LV: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
		    			int size = 0, storage_type = VolumeManager.LV_STORAGE_NAS, lv_type = VolumeManager.LV_STRIPED, size_units = VolumeManager.SIZE_LV_M, fs_type = FileSystemManager.FS_XFS;
		    			double percent_snap=25D;
	    				boolean _encryption = false;
	    				boolean _deduplication = false;
	    				
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
		    			String name = request.getParameter("name");
		    			
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
	    				String group = request.getParameter("group");
	    				
	    				Map<String, String> vg = VolumeManager.getVolumeGroup(group);
	    				
	    				if(vg.get("type").equals("zfs")) {
	    					fs_type = FileSystemManager.FS_ZFS;
	    				} 
	    				
	    				if(request.getParameter("percent_snap") != null && !request.getParameter("percent_snap").isEmpty()) {
	    					percent_snap = Integer.parseInt(request.getParameter("percent_snap"));
	    				}
	    				
    					try {
	    					size = Integer.parseInt(request.getParameter("size"));
	    				} catch(NumberFormatException _ex) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_virtual_size"));
	    				}
	    				if(request.getParameter("encryption") != null && "true".equalsIgnoreCase(request.getParameter("encryption"))) {
                        	_encryption = true;
                        }
	    				
	    				if(request.getParameter("deduplication") != null && "true".equalsIgnoreCase(request.getParameter("deduplication"))) {
                        	_deduplication = true;
                        }
                        
	    				if(request.getParameter("storage_type") == null) {
	    				} else if("S".equals(request.getParameter("storage_type"))) {
	    					storage_type = VolumeManager.LV_STORAGE_SAN;
	    				}
	    				
	    				if(request.getParameter("size_units") == null) {
	    				} else if("G".equals(request.getParameter("size_units"))) {
	    					size_units = VolumeManager.SIZE_LV_G;
	    				} else if("T".equals(request.getParameter("size_units"))) {
	    					size_units = VolumeManager.SIZE_LV_T;
	    				}
	    				
	    				if(request.getParameter("lv_type") == null) {
	    				} else if("L".equals(request.getParameter("lv_type"))) {
	    					lv_type = VolumeManager.LV_LINEAR;
	    				} else if("M".equals(request.getParameter("lv_type"))) {
	    					lv_type = VolumeManager.LV_MIRRROR;
	    				}
	    				
	    				VolumeManager.addLogicalVolume(storage_type, lv_type, fs_type, group, name, size, size_units, request.getParameter("compression"), _encryption, _deduplication, percent_snap);
	    				
	    				if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
	    					if (VolumeManager.isLocalDeviceGroup(group)) {
	    						HACommClient.sendAddVolume(storage_type, lv_type, fs_type, group, name, size, size_units, request.getParameter("compression"), _encryption, _deduplication, percent_snap);
	    					} else {
	    						HACommClient.sendFsTab();
	    					}
	    				}
	    				
	    				writeDocumentResponse(getLanguageMessage("device.message.logical_volume.added"), "/admin/DeviceDisk");
    				}	
	    			break;
	    		case EDIT_LV: {
		    			int _offset = 0;
	    				if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception("invalid logical volume name");
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("invalid volume group name"));
	    				}
	    				
	    				_xhtml_out.print(HtmlFormUtils.printJSValidationHeader(this.messagei18N.getLocale()));
	    				
	    				String operation = "extend";
	    				if(request.getParameter("operation") != null && !request.getParameter("operation").isEmpty()) {
	    					operation = request.getParameter("operation"); 
	    				}
	    				Map<String, String> _lv = VolumeManager.getLogicalVolume(request.getParameter("group"), request.getParameter("name"));
	    				Map<String, String> _vg = VolumeManager.getVolumeGroup(request.getParameter("group"));
	    				List<Map<String, String>> _snapshots = VolumeManager.getLogicalVolumeSnapshots(request.getParameter("group"), request.getParameter("name"));
    					Map<String, String> _hourly_snapshot = VolumeManager.getPlannedSnapshot(VolumeManager.LV_SNAPSHOT_HOURLY, request.getParameter("group"), request.getParameter("name"));
    					Map<String, String> _daily_snapshot = VolumeManager.getPlannedSnapshot(VolumeManager.LV_SNAPSHOT_DAILY, request.getParameter("group"), request.getParameter("name"));

    					char units = 'M';
    					double twentyG = 20*1024D*1024D*1024D;
    					double twentyT = 20*1024D*1024D*1024D*1024D;
	    				if(request.getParameter("units") != null && !request.getParameter("units").isEmpty()) {
	    					units = request.getParameter("units").charAt(0); 
	    				} else if (_lv != null && _lv.get("size") != null && (_lv.get("size").contains("T") && Double.parseDouble(_lv.get("size-raw")) >= twentyT)) {
	    					units = 'T';
	    				}else if (_lv != null && _lv.get("size") != null && (_lv.get("size").contains("T") || (_lv.get("size").contains("G") && Double.parseDouble(_lv.get("size-raw")) >= twentyG))) {
	    					units = 'G';
	    				}
	    				
	    				String stringUnits = units+"B";
	    				
    					writeDocumentBackForce("/admin/DeviceDisk");
    					_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/highcharts.js\"></script>");
						
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/drive_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.disk"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.volume_groups.info_management"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(request.getParameter("group"));
						_xhtml_out.print(" / ");
						_xhtml_out.print(request.getParameter("name"));
						_xhtml_out.print("<a  class=\"asubmit\" href=\"#\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    
						_xhtml_out.print("<div class=\"vol-vertical-container-info\">");
						_xhtml_out.print("<span>");
		        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.name"));
		        	    _xhtml_out.println(": </span>");
		        	    _xhtml_out.println(request.getParameter("name"));
		        	    _xhtml_out.println("<br/><br/>");
						_xhtml_out.print("<span>");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.vg"));
	        	    	_xhtml_out.println(": </span>");
	        	    	_xhtml_out.print(_lv.get("vg"));
	        	    	_xhtml_out.println("<br/><br/>");
	        	    	_xhtml_out.print("<span>");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.storage_fstype"));
	        	    	_xhtml_out.println(": </span>");
	        	    	if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype"))) {
                    		_xhtml_out.println(getLanguageMessage("device.logical_volumes.storage_fstype.performance"));
                    	} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
                    		_xhtml_out.println(getLanguageMessage("device.logical_volumes.storage_fstype.zfs"));
                    	} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _lv.get("fstype"))) {
                    		_xhtml_out.println(getLanguageMessage("device.logical_volumes.storage_fstype.btrfs"));
                    	} else {
                    		_xhtml_out.println(getLanguageMessage("common.message.unknown"));
                    	}
	        	    	_xhtml_out.println("<br/><br/>");
	        	    	if(!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
							_xhtml_out.print("<span>");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.stripes"));
		        	    	_xhtml_out.println(": </span>");
		        	    	_xhtml_out.print(_lv.get("stripes"));
		        	    	_xhtml_out.println("<br/><br/>");
	        	    	}
	        	    	_xhtml_out.print("<span>");
		        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.volume.size"));
		        	    if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
		        	    	_xhtml_out.print("("+getLanguageMessage("device.logical_volumes.quota.zfs")+")");
		        	    }
		        	    _xhtml_out.println(": </span>");
		        	    _xhtml_out.println(_lv.get("size"));
		        	    if (_lv.get("free") != null && !_lv.get("free").isEmpty()) { 
			        	    _xhtml_out.print("(");
			        	    _xhtml_out.println(_lv.get("free"));
			        	    _xhtml_out.print(" ");
			        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.available"));
			        	    _xhtml_out.print(")");
		        	    }
		        	    _xhtml_out.println("<br/><br/>");
		        	    _xhtml_out.print("<span>");
		        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.agg.size"));
		        	    _xhtml_out.println(": </span>");
		        	    _xhtml_out.println(_vg.get("size"));
	        	    	_xhtml_out.print("(");
	        	    	if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")))
	        	    		_xhtml_out.print(_vg.get("fs-free"));
	        	    	else
	        	    		_xhtml_out.print(_vg.get("free"));
	        	    	_xhtml_out.print(" ");
	        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.available"));
		        	    _xhtml_out.print(")");
		        	    _xhtml_out.println("<br/><br/>");
		        	    
	        	    	if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")) ||
	        	    			FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _lv.get("fstype"))) {
							_xhtml_out.print("<span>");
	        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.storage_fstype.compression"));
		        	    	_xhtml_out.println(": </span>");
		        	    	if(_lv.get("compressed") != null) {
		        	    		_xhtml_out.print(new Double(Double.parseDouble(_lv.get("compressed-raw"))).intValue()+" %");
		        	    	}
		        	    	_xhtml_out.print(" (<b>");
		        	    	_xhtml_out.print(_lv.get("compression"));
		        	    	_xhtml_out.println("</b>)<br/><br/>");
	        	    	}
	        	    	if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
							_xhtml_out.print("<span>");
	        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.storage_fstype.deduplication_ratio"));
		        	    	_xhtml_out.println(": </span>");
		        	    	_xhtml_out.print(" <b>");
		        	    	_xhtml_out.print(_lv.get("dedup"));
		        	    	_xhtml_out.print("</b>");
		        	    	_xhtml_out.println("<br/><br/>");
	        	    	}
	        	    	
	        	    	if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")) ||
	        	    			FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _lv.get("fstype"))) {
							_xhtml_out.print(" <hr /> <br /><span>");
	        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.reservation.total"));
		        	    	_xhtml_out.println(": </span>");
		        	    	if(_lv.get("reservation-raw") != null) {
		        	    		_xhtml_out.print(VolumeManager.getFormatSize(_lv.get("reservation-raw")+" B"));
		        	    	}
		        	    	_xhtml_out.println("<br/><br/>");
		        	    	_xhtml_out.print("<span>");
	        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.used.raw"));
		        	    	_xhtml_out.println(": </span>");
		        	    	if(_lv.get("used-raw") != null) {
		        	    		_xhtml_out.print(VolumeManager.getFormatSize(_lv.get("total-used-raw")+" B"));
		        	    	}
		        	    	_xhtml_out.println("<br/><br/>");
		        	    	_xhtml_out.print("<span>");
	        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.used.data"));
		        	    	_xhtml_out.println(": </span>");
		        	    	if(_lv.get("used-raw") != null) {
		        	    		_xhtml_out.print(VolumeManager.getFormatSize(_lv.get("used-raw")+" B"));
		        	    	}
		        	    	_xhtml_out.println("<br/><br/>");
		        	    	_xhtml_out.print("<span>");
	        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.used.snapshot"));
		        	    	_xhtml_out.println(": </span>");
		        	    	if(_lv.get("used-raw") != null) {
		        	    		_xhtml_out.print(VolumeManager.getFormatSize(_lv.get("snapshot-used-raw")+" B"));
		        	    	}
		        	    	_xhtml_out.println("<br/><br/>");
		        	    	_xhtml_out.print("<span>");
	        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.used.refreservation"));
		        	    	_xhtml_out.println(": </span>");
		        	    	if(_lv.get("used-raw") != null) {
		        	    		_xhtml_out.print(VolumeManager.getFormatSize(_lv.get("refreservation-used-raw")+" B"));
		        	    	}
		        	    	_xhtml_out.println("<br/><br/>");
	        	    	}

	        	    	_xhtml_out.println("</div>");
	        	    	
	        	    	
	        	    	Double compressed = null;
	        	    	if (_lv.get("compressed-raw")!=null) {
	        	    		compressed = 0D;
	        	    		compressed = Double.parseDouble(_lv.get("compressed-raw"));
	        	    	}
	        	    	
	        	    	/*Double deduplication = null;
	        	    	if (_lv.get("dedup") != null && !_lv.get("dedup").equals("off")) {
	        	    		deduplication = 0D;
	        	    		if (_vg.get("deduplicated-raw")!=null)
	        	    			deduplication = Double.parseDouble(_vg.get("deduplicated-raw"));
	        	    	}*/
	        	    	
	        	    	Boolean barOcuppation=true;
	        	    	Double ocupation = 0D;
	        	    	if (FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")) &&_lv.get("used-raw") != null) {
	        	    		ocupation = Double.parseDouble(_lv.get("used-raw"))*100 / Double.parseDouble(_lv.get("size-raw"));
	        	    	} else if (_lv.get("used-raw") != null){
	        	    		ocupation = Double.parseDouble(_lv.get("used-raw"));
	        	    	} else {
	        	    		barOcuppation=false;
	        	    	}

	        	    	pageJS+=getJSVolumeHighBars("bars", ocupation, compressed, request.getParameter("name"), barOcuppation);
	        	    	if (FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")) &&_lv.get("used-raw") != null) {
	        	    		pageJS+=getVolUsedBars("usedBars", _lv);
	        	    	}
	        	    	_xhtml_out.println("<div id=\"bars\" class=\"vol-vertical-container-bars\"></div>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("<div id=\"usedBars\" style=\"width:92%;margin:auto;\"></div>");
	        	    	
						_xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    
	                    _xhtml_out.print("<form action=\"/admin/DeviceDisk\" name=\"device\" id=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_EXTEND_LV + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"group\" value=\"" + request.getParameter("group") + "\"/>");
	                    if (! (FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype"))  && !VolumeManager.isMount(_lv.get("vg"), _lv.get("name")))) {
	                    	
		                    _xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.logical_volumes.extend_vl"));
		                    _xhtml_out.print("<a  class=\"asubmit\" href=\"#\"><img src=\"/images/disk_16.png\" title=\"");
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
		                    _xhtml_out.println("<fieldset style=\"width:500px;margin:auto;float:none;\">");
		                    _xhtml_out.println("<br />");
			        	    _xhtml_out.println("<h3>");
							_xhtml_out.print(getLanguageMessage("device.volume.select.unit"));
		                    _xhtml_out.println("</h3>");
		                    _xhtml_out.println("<br />");
		                    _xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"units\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.unit"));
		        	    	_xhtml_out.println(": </label>");
		                    if (!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype"))  && !_lv.get("fstype").equals("unknown")) {
		        	    		String s_operation = "";
		        	    		if (request.getParameter("operation") != null && !request.getParameter("operation").isEmpty())
		        	    			s_operation = "&operation="+request.getParameter("operation");
		        	    		_xhtml_out.println("<select class=\"form_select\" name=\"units\" id=\"units\" onChange=\"document.location.href='/admin/DeviceDisk?type=" + EDIT_LV + "&group=" + request.getParameter("group") + "&name=" + request.getParameter("name")+s_operation+"&units='+this.value \">");
		        	    	} else {
		        	    		_xhtml_out.println("<select class=\"form_select\" name=\"units\" id=\"units\" onChange=\"adjustSizeSlider();\">");
		        	    	}
		        	    	_xhtml_out.println("<option value=\"M\" ");
		        	    	if (units == 'M')
		        	    		_xhtml_out.println(" selected=\"selected\" ");
		        	    	_xhtml_out.println(">MB</option>");
		        	    	_xhtml_out.println("<option value=\"G\" ");
		        	    	if (units == 'G')
		        	    		_xhtml_out.println(" selected=\"selected\" ");
		        	    	_xhtml_out.println(">GB</option>");
		        	    	_xhtml_out.println("<option value=\"T\" ");
		        	    	if (units == 'T')
		        	    		_xhtml_out.println(" selected=\"selected\" ");
		        	    	_xhtml_out.println(">TB</option>");
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"clear\"></div>");
		        	    	
		        	    	_xhtml_out.println("<br />");
			        	    _xhtml_out.println("<h3>");
							_xhtml_out.print(getLanguageMessage("device.volume.extendreduce"));
		                    _xhtml_out.println("</h3>");
		                    _xhtml_out.println("<br />");
		                    
		        	    	
		                    _xhtml_out.println("<div class=\"standard_form\">");
		                    _xhtml_out.print("<label for=\"operation\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.operation"));
		        	    	_xhtml_out.println(": </label>");
		        	    	if (!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype"))  && !_lv.get("fstype").equals("unknown")) {
		        	    		String s_units = "";
		        	    		if (request.getParameter("units") != null && !request.getParameter("units").isEmpty())
		        	    			s_units = "&units="+request.getParameter("units");
			        	    	_xhtml_out.println("<select class=\"form_select\" name=\"operation\" id=\"operation\" onChange=\"document.location.href='/admin/DeviceDisk?type=" + EDIT_LV + "&group=" + request.getParameter("group") + "&name=" + request.getParameter("name")+s_units+"&operation='+this.value \">");
		        	    	} else {
			        	    	_xhtml_out.println("<select class=\"form_select\" name=\"operation\" id=\"operation\" onChange=\"adjustSizeSlider();\">");
		        	    	}
		        	    	_xhtml_out.println("<option value=\"extend\" ");
		        	    	if (operation.equals("extend"))
	        	    			_xhtml_out.println(" selected=\"selected\" ");
		        	    	_xhtml_out.println(">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.size_change.extend"));
		        	    	_xhtml_out.println("</option>");
		        	    	if (!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype"))  && !_lv.get("fstype").equals("unknown")) {
		        	    		_xhtml_out.println("<option value=\"shrink\" ");
		        	    		if (operation.equals("shrink"))
		        	    			_xhtml_out.println(" selected=\"selected\" ");
		        	    		_xhtml_out.println(">");
		        	    		_xhtml_out.print(getLanguageMessage("device.logical_volumes.size_change.shrink"));
		        	    		_xhtml_out.println("</option>");
		        	    	}
		        	    	_xhtml_out.println("</select>");
		        	    	 _xhtml_out.println("</div>");
		        	    	 _xhtml_out.println("<div class=\"clear\"></div>");
		        	    	 
		        	    	Double maxAggSpace = Double.parseDouble(_vg.get("free-raw"));
		        	    	Double snapOcuped = 0D;
		        	    	Double maxExtend = getMaxForSlider(maxAggSpace.toString(), units);
		        	    	if (!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype")) && !_lv.get("fstype").equals("unknown")) {
		        	    		maxExtend =  getMaxForSlider(_vg.get("freequota-raw"), units);
		        	    		snapOcuped =  Double.parseDouble(_lv.get("snapshot-used-raw"));
		        	    	}
		        	    	Double maxQuota = Double.parseDouble(_lv.get("size-raw"));
			        	    Double dataOcuped = 0D;
			        	    if (_lv.get("used-raw") != null)
			        	    	dataOcuped = Double.parseDouble(_lv.get("used-raw"));
			        	    Double maxReduce = Double.parseDouble(_lv.get("size-raw"))-dataOcuped-snapOcuped;
			        	    maxReduce = getMaxForSlider(maxReduce.toString(), units);
			        	    if (maxReduce < 0D)
			        	    	maxReduce = 0D;
			        	    
	        	    		if (FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype")) && !_lv.get("fstype").equals("unknown")) {
	        	    			if (operation.equals("extend"))
	        	    				pageJS += getSizeSliderJS("slider", "size", units, 0D, maxExtend, 0.0D, _lv.get("fstype"));
	        	    			else
	        	    				pageJS += getSizeSliderJS("slider", "size", units, 0D, maxReduce, 0.0D, _lv.get("fstype"));	
	        	    		}
		        	    	
		        	    	Double exSizeMaxMega = getMaxForSlider(_vg.get("free-raw"), 'M');
	                		Double exSizeMaxGiga = getMaxForSlider(_vg.get("free-raw"), 'G');
	        				Double exSizeMaxTera = getMaxForSlider(_vg.get("free-raw"), 'T');
	        				Double shSizeMaxMega = getMaxForSlider(_lv.get("free-raw"), 'M');
	                		Double shSizeMaxGiga = getMaxForSlider(_lv.get("free-raw"), 'G');
	        				Double shSizeMaxTera = getMaxForSlider(_lv.get("free-raw"), 'T');
	        				pageJSFuncs += getAdjustSliderFunc("adjustSizeSlider","slider", "units", "size", "operation", exSizeMaxMega, exSizeMaxGiga, exSizeMaxTera, shSizeMaxMega, shSizeMaxGiga, shSizeMaxTera);
			        	    
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"size\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.size_change"));
		        	    	if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
			        	    	_xhtml_out.print("("+getLanguageMessage("device.logical_volumes.quota.zfs")+")");
			        	    }
		        	    	_xhtml_out.println(": </label>");
		        	    	if (!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype")) && !_lv.get("fstype").equals("unknown")) {
		        	    		_xhtml_out.println("<input class=\"form_text\" name=\"size\" id=\"size\" size=\"5\" /> "+stringUnits);
		        	    	} else {
		        	    		_xhtml_out.println("<input class=\"form_text\" name=\"size\" id=\"size\" style=\"border: 0; color: #f6931f; font-weight: bold;\" />"+stringUnits);
		        	    	}
		        	    		
		        	    	_xhtml_out.println("<div id=\"slider\" style=\"margin-left:45px;\"> </div>");
		            	    _xhtml_out.println("</div>");
		            	    _xhtml_out.println("<div class=\"clear\"></div>");

	        				if (!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype"))  && !_lv.get("fstype").equals("unknown")) {
	
				        	    Double currentDataSize = Double.parseDouble(_lv.get("refquota-raw"));			        	    
				        	    Double currentReservation = Double.parseDouble(_lv.get("reservation-raw"));
				        	    Double currentDataReservation = Double.parseDouble(_lv.get("refreservation-raw"));
				        	    
				        	    _xhtml_out.println("<br />");
				        	    _xhtml_out.println("<h3>");
								_xhtml_out.print(getLanguageMessage("device.volume_zfs.params"));
			                    _xhtml_out.println("</h3>");
			                    _xhtml_out.println("<br />");
			                    
			        	    	Double min = getMinForSlider(String.valueOf(dataOcuped), units);
			        	    	Double max = getMaxForSlider(String.valueOf(maxQuota), units);
			        	    	Double current = getMaxForSlider(String.valueOf(currentDataSize), units);
			        	    	Integer minDataSize=0, minDataReservation=0, minReservation=0, dataSizeVal=0, reservationVal=0, dataReservationVal=0, maxDataSize=0, maxReservation=0, maxDataReservation=0;
			        	    	
			        	    	_xhtml_out.println("<div class=\"standard_form\">");
			        	    	_xhtml_out.print("<label for=\"current\">");
				        	    _xhtml_out.print(getLanguageMessage("device.logical_volumes.volume.size.current"));
				        	    if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype"))) {
				        	    	_xhtml_out.print("("+getLanguageMessage("device.logical_volumes.quota.zfs")+")");
				        	    }
				        	    _xhtml_out.println(": </label>");
				        	    _xhtml_out.println("<input class=\"form_text\" name=\"current\" id=\"current\" style=\"border: 0; color: #f6931f; font-weight: bold;\" readOnly=\"readOnly\" value=\""+getMinForSlider(_lv.get("size-raw"), units).intValue()+"\"/> "+stringUnits);
				        	    _xhtml_out.println("</div>");
			            	    _xhtml_out.println("<div class=\"clear\"></div>");
			            	    
			        	    	if (VolumeManager.isMount(_lv.get("vg"), _lv.get("name"))) {
				                    _xhtml_out.println("<div class=\"standard_form\">");
				                    _xhtml_out.print("<label for=\"data_size\">");
				        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.zfs.data_size"));
				        	    	_xhtml_out.println(": </label>");
				        	    	_xhtml_out.println("<input class=\"validate[required,custom[integer],min["+min.intValue()+"],max["+max.intValue()+"]] form_text\" data-prompt-position=\"inline\" name=\"data_size\" id=\"data_size\" style=\"border: 0; color: #f6931f; font-weight: bold;\" value=\""+current.intValue()+"\" /> "+stringUnits);
				        	    	_xhtml_out.println("<div id=\"slider_data_size\" style=\"margin-left:45px;\"> </div>");
				        	    	_xhtml_out.println("</div>");
				        	    	_xhtml_out.println("<div class=\"clear\"></div>");
				        	    	minDataSize = min.intValue();
				        	    	maxDataSize = max.intValue();
				        	    	dataSizeVal = current.intValue();
			        	    	}
			        	    	
			        	    	maxAggSpace = Double.parseDouble(_vg.get("fs-free-raw"));
			        	    	Double maxReservVol = getMaxForSlider(String.valueOf(maxAggSpace+currentReservation), units);
			        	    	min = 0D;
			        	    	max = getMaxForSlider(String.valueOf(maxQuota), units);
			        	    	current = getMaxForSlider(String.valueOf(currentReservation), units);
			        	    	if (maxReservVol < max)
			        	    		max = maxReservVol;
				        	    _xhtml_out.println("<div class=\"standard_form\">");
			        			_xhtml_out.print("<label for=\"reservation\">");
			        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.zfs.reservation"));
			        	    	_xhtml_out.println(": </label>");
			        	    	_xhtml_out.println("<input class=\"validate[required,custom[integer],min["+min.intValue()+"],max["+max.intValue()+"]] form_text\" data-prompt-position=\"inline\" name=\"reservation\" id=\"reservation\" style=\"border: 0; color: #f6931f; font-weight: bold;\" value=\""+current.intValue()+"\" /> "+stringUnits);
			        	    	_xhtml_out.println("<div id=\"slider_reservation\" style=\"margin-left:45px;\"> </div>");
			        	    	_xhtml_out.println("</div>");
			        	    	_xhtml_out.println("<div class=\"clear\"></div>");	        			
			        	    	 minReservation = min.intValue();
				        	     maxReservation = max.intValue();
				        	     reservationVal = current.intValue();
			        			
			        	    	maxAggSpace = Double.parseDouble(_vg.get("fs-free-raw"));
			        	    	maxReservVol = getMaxForSlider(String.valueOf(maxAggSpace+currentDataReservation), units);
			        	    	min = 0D;
			        	    	max = getMaxForSlider(String.valueOf(maxQuota), units);
			        	    	current = getMaxForSlider(String.valueOf(currentDataReservation), units);
			        	    	if (maxReservVol < max)
			        	    		max = maxReservVol;
				        	    _xhtml_out.println("<div class=\"standard_form\">");
			        			_xhtml_out.print("<label for=\"data_reservation\">");
			        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.zfs.data_reservation"));
			        	    	_xhtml_out.println(": </label>");
			        	    	_xhtml_out.println("<input class=\"validate[required,custom[integer],min["+min.intValue()+"],max["+max.intValue()+"]] form_text\" data-prompt-position=\"inline\" name=\"data_reservation\" id=\"data_reservation\" style=\"border: 0; color: #f6931f; font-weight: bold;\" value=\""+current.intValue()+"\" /> "+stringUnits);
			        	    	_xhtml_out.println("<div id=\"slider_data_reservation\" style=\"margin-left:45px;\"> </div>");
			        	    	_xhtml_out.println("</div>");
			        	    	_xhtml_out.println("<div class=\"clear\"></div>");	        			
			        	    	 minDataReservation = min.intValue();
				        	     maxDataReservation = max.intValue();
				        	     dataReservationVal = current.intValue();
			        			
				        	    boolean withQuota = false;
				        	    if (VolumeManager.isMount(_lv.get("vg"), _lv.get("name")))
				        	    	withQuota = true;
			        	    	getSlidersControlJs(minDataSize, minReservation, minDataReservation, dataSizeVal, reservationVal, dataReservationVal, maxDataSize, maxReservation, maxDataReservation, withQuota);
	        				
		        				_xhtml_out.println("<div class=\"clear\"></div>");
		        				_xhtml_out.println("<br />");
		        				if (VolumeManager.isMount(_lv.get("vg"), _lv.get("name"))) {
			        				_xhtml_out.println("<div class=\"subinfo\">");
				        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.zfs.warn.reservation"));
				        	    	_xhtml_out.println("</div>");
		        				}
		    				}
	        				_xhtml_out.println("<div class=\"clear\"></div>");
	        				_xhtml_out.println("<br /><br />");
		        	    	_xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
	                    }
	                    
	                    if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")) ||
	                    		FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _lv.get("fstype"))) {
	                    	_xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.manual"));
		                    _xhtml_out.println("<a class=\"asubmit\" href=\"#\"><img src=\"/images/disk_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.save"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.save"));
		                   	_xhtml_out.println("\"/></a>");
		                    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.println("\"/></a>");

		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.println("<fieldset>");
		                    _xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"snapshot_manual_remove\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.manual_remove"));
		        	    	_xhtml_out.println(": </label>");
		        	    	String removeChecked = "";
		        	    	if (VolumeManager.isSnapshotManualRemoveOn(_lv.get("vg"), _lv.get("name")))
		        	    		removeChecked = "checked=\"true\"";
		        	    	_xhtml_out.println("<input type=\"checkbox\" class=\"form_check\" value=\"true\" name=\"snapshot_manual_remove\" "+removeChecked+">");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
			        	    	
		                    _xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.hourly"));
		                    _xhtml_out.println("<a class=\"asubmit\" href=\"#\"><img src=\"/images/disk_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.save"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.save"));
		                   	_xhtml_out.println("\"/></a>");
		                    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.println("\"/></a>");

		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.println("<fieldset>");
		                    _xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"snapshot_hourly_status\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.status"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"snapshot_hourly_status\">");
		        	    	_xhtml_out.print("<option value=\"disabled\"");
		        	    	if(_hourly_snapshot.isEmpty()) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("common.message.inactive"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.print("<option value=\"enabled\"");
		        	    	if(!_hourly_snapshot.isEmpty()) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("common.message.active"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"snapshot_hourly_retention\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.retention"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.print("<input class=\"network_octet\" name=\"snapshot_hourly_retention\" value=\"");
		        	    	if(!_hourly_snapshot.isEmpty()) {
		        	    		_xhtml_out.print(_hourly_snapshot.get("retention"));
		        	    	}
		        	    	_xhtml_out.println("\"/> ");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.hours"));
		                    _xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
		                    
		                    _xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.daily"));
		                    _xhtml_out.print("<a class=\"asubmit\" href=\"#\"><img src=\"/images/disk_16.png\" title=\"");
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
		        	    	_xhtml_out.print("<label for=\"snapshot_daily_status\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.status"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"snapshot_daily_status\">");
		        	    	_xhtml_out.print("<option value=\"disabled\"");
		        	    	if(_daily_snapshot.isEmpty()) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("common.message.inactive"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.print("<option value=\"enabled\"");
		        	    	if(!_daily_snapshot.isEmpty()) {
		        	    		_xhtml_out.print(" selected=\"selected\"");
		        	    	}
		        	    	_xhtml_out.print(">");
		        	    	_xhtml_out.print(getLanguageMessage("common.message.active"));
		        	    	_xhtml_out.println("</option>");
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"snapshot_daily_hour\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.daily.hour"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"snapshot_daily_hour\">");
		        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
		        	    	for(int i = 0; i < 24 ; i++) {
		        	    		_xhtml_out.print("<option value=\"");
		        	    		_xhtml_out.print(i);
		        	    		_xhtml_out.print("\"");
			        	    	if(!_daily_snapshot.isEmpty() && String.valueOf(i).equals(_daily_snapshot.get("hour"))) {
			        	    		_xhtml_out.print(" selected=\"selected\"");
			        	    	}
			        	    	_xhtml_out.print(">");
			        	    	_xhtml_out.print(i);
			        	    	_xhtml_out.println(":00</option>");
		        	    	}
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"snapshot_daily_retention\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.retention"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"network_octet\" name=\"snapshot_daily_retention\" value=\"");
		        	    	if(!_daily_snapshot.isEmpty()) {
		        	    		_xhtml_out.print(_daily_snapshot.get("retention"));
		        	    	}
		        	    	_xhtml_out.println("\"/> ");
		        	    	_xhtml_out.print(getLanguageMessage("device.logical_volumes.replica.days"));
		        	    	_xhtml_out.println("</fieldset>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
		                    
		                    _xhtml_out.println("<div class=\"window\">");
							_xhtml_out.println("<h2>");
							_xhtml_out.println(getLanguageMessage("device.logical_volumes.snapshots"));
		                    _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
		                    _xhtml_out.print(SNAPSHOT_LV);
		                    _xhtml_out.print("&group=");
		                    _xhtml_out.print(request.getParameter("group"));
		                    _xhtml_out.print("&name=");
		                    _xhtml_out.print(request.getParameter("name"));
		                    _xhtml_out.print("\"><img src=\"/images/camera_add_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.add"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.add"));
		                    _xhtml_out.println("\"/></a>");

		                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                    _xhtml_out.println("\"/></a>");

		                    _xhtml_out.println("</h2>");
		                    _xhtml_out.println("<table>");
		                    if(_snapshots.isEmpty()) {
		                    	_xhtml_out.println("<tr>");
		                    	_xhtml_out.println("<td>");
		                    	_xhtml_out.println(getLanguageMessage("device.message.no_snapshots"));
		                    	_xhtml_out.println("</td>");
			                    _xhtml_out.println("</tr>");
		                    } else {
		                    	_xhtml_out.println("<tr>");
			                    _xhtml_out.println("<td></td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.name"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.schedule"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.used"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">");
			                    _xhtml_out.print(getLanguageMessage("device.logical_volumes.snapshots.referenced"));
			                    _xhtml_out.println("</td>");
			                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
			                    _xhtml_out.println("</tr>");
			                    _offset = 0;
			                    for(Map<String, String> _snapshot : _snapshots) {
			                    	_xhtml_out.print("<tr");
			                    	if(_offset % 2 == 0) {
			                    		_xhtml_out.print(" class=\"highlight\"");
			                    	}
			                    	_xhtml_out.println(">");
			                    	_xhtml_out.println("<td><img src=\"/images/camera_16.png\"/></td>");
			                    	_xhtml_out.println("<td>");
				                    _xhtml_out.println(_snapshot.get("name"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println(_snapshot.get("schedule"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println(_snapshot.get("used"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.println(_snapshot.get("referenced"));
				                    _xhtml_out.println("</td>");
				                    _xhtml_out.println("<td>");
				                    _xhtml_out.print("<a href=\"/admin/DeviceDisk?type=");
				                    _xhtml_out.print(SNAPSHOT_REMOVE_LV);
				                    _xhtml_out.print("&group=");
				                    _xhtml_out.print(request.getParameter("group"));
				                    _xhtml_out.print("&name=");
				                    _xhtml_out.print(request.getParameter("name"));
				                    _xhtml_out.print("&snapshot=");
				                    _xhtml_out.print(_snapshot.get("name"));
				                    _xhtml_out.print("\"><img src=\"/images/camera_delete_16.png\" title=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
				                    _xhtml_out.print("\" alt=\"");
				                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
				                    _xhtml_out.println("\"/></a>");
				                    _xhtml_out.println("</td>");
			                    	_xhtml_out.println("</tr>");
			                    	_offset++;
			                    }
		                    }
		                    _xhtml_out.println("</table>");
		                    _xhtml_out.println("<br/>");
		                    _xhtml_out.println("<div class=\"clear\"></div>");
		                    _xhtml_out.println("</div>");
	                    }
	                
	                    _xhtml_out.print("</form>");
	                    
	                    _xhtml_out.print("<div id=\"tooltip\"></div>");
	                    pageJS+="jQuery('.asubmit').click(function(event){if ($('#device').validationEngine('validate')) submitForm(document.device.submit());});";
	    			}
	    			break;
	    		case STORE_EXTEND_LV: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
	    			    int _size = 0, _size_units = VolumeManager.SIZE_LV_M;
	    				if(request.getParameter("name") == null || request.getParameter("name").trim().isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.logical_volume_name"));
	    				}
	    				if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
	    				
	    				String _type = VolumeManager.getLogicalVolumeFS(request.getParameter("group"), request.getParameter("name"));
	    				if(request.getParameter("size") != null && !request.getParameter("size").isEmpty()) {
	    					try {
		    					_size = Integer.parseInt(request.getParameter("size"));
		    				} catch(NumberFormatException _ex) {
		    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_extend_size"));
		    				}
	    				}
	    				
	    				if(request.getParameter("units") == null) {
	    				} else if("G".equals(request.getParameter("units"))) {
	    					_size_units = VolumeManager.SIZE_LV_G;
	    				} else if("T".equals(request.getParameter("units"))) {
	    					_size_units = VolumeManager.SIZE_LV_T;
	    				}
	    				
	    				if(request.getParameter("operation") == null) {
	    					_size = 0;
	    				} else if(request.getParameter("operation").equalsIgnoreCase("extend")) {
	    					_size = Math.abs(_size);
	    				} else if(request.getParameter("operation").equalsIgnoreCase("shrink")) {
	    					_size = Math.abs(_size) * -1;
	    				}
	    				
	    				if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type) ||
	    						FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _type)) {
	    					if("enabled".equals(request.getParameter("snapshot_hourly_status"))) {
		    					int _retention = 0;
		    					try {
		    						_retention = Integer.parseInt(request.getParameter("snapshot_hourly_retention"));
		    					} catch(NumberFormatException _ex) {
		    						throw new Exception(getLanguageMessage("device.volume_groups.exception.snapshot_retention"));
		    					}
		    					VolumeManager.setPlannedSnapshot(request.getParameter("group"), request.getParameter("name"), VolumeManager.LV_SNAPSHOT_HOURLY, 0, _retention);
		    				} else {
		    					VolumeManager.removePlannedSnapshot(request.getParameter("group"), request.getParameter("name"), VolumeManager.LV_SNAPSHOT_HOURLY);
		    				}
		    				
		    				if("enabled".equals(request.getParameter("snapshot_daily_status"))) {
		    					int _retention = 0, _hour = 0;
		    					try {
		    						_retention = Integer.parseInt(request.getParameter("snapshot_daily_retention"));
		    					} catch(NumberFormatException _ex) {
		    						throw new Exception(getLanguageMessage("device.volume_groups.exception.snapshot_retention"));
		    					}
		    					if(request.getParameter("snapshot_daily_hour") != null && !request.getParameter("snapshot_daily_hour").isEmpty()) {
		    						try {
			    						_hour = Integer.parseInt(request.getParameter("snapshot_daily_hour"));
			    					} catch(NumberFormatException _ex) {
			    						throw new Exception(getLanguageMessage("device.volume_groups.exception.daily_hour"));
			    					}
		    					}
		    					VolumeManager.setPlannedSnapshot(request.getParameter("group"), request.getParameter("name"), VolumeManager.LV_SNAPSHOT_DAILY, _hour, _retention);
		    				} else {
		    					VolumeManager.removePlannedSnapshot(request.getParameter("group"), request.getParameter("name"), VolumeManager.LV_SNAPSHOT_DAILY);
		    				}
		    				if (request.getParameter("snapshot_manual_remove") != null && "true".equals(request.getParameter("snapshot_manual_remove")))
		    					VolumeManager.setSnapshotManualRemove(request.getParameter("group"), request.getParameter("name"), true);
		    				else
		    					VolumeManager.setSnapshotManualRemove(request.getParameter("group"), request.getParameter("name"), false);
	    				}
	    				String _message = "";
	    				
	    				Double data_reservation = null;
	    				Double total_reservation = null;
	    				Double data_size = null;
	    				if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
		    				if(request.getParameter("data_size") != null && !request.getParameter("data_size").isEmpty()) {
		    					data_size = Double.parseDouble(request.getParameter("data_size"));
		    					data_size = VolumeManager.getSize(data_size.intValue(), _size_units);
		    				}
		    				
		    				if(request.getParameter("reservation") != null && !request.getParameter("reservation").isEmpty()) {
		    					total_reservation = Double.parseDouble(request.getParameter("reservation"));
		    					total_reservation = VolumeManager.getSize(total_reservation.intValue(), _size_units);
	    					}
		    				
		    				if(request.getParameter("data_reservation") != null && !request.getParameter("data_reservation").isEmpty()) {
		    					data_reservation = Double.parseDouble(request.getParameter("data_reservation"));
		    					data_reservation = VolumeManager.getSize(data_reservation.intValue(), _size_units); 
		    				}
	    				}
	    				
	    				if (VolumeManager.isMount(request.getParameter("group"), request.getParameter("name"))) {
		    				if (data_reservation != null && (data_size == null || (data_reservation > data_size))) {
		    					throw new Exception (getLanguageMessage("device.volume_groups.exception.datareservation"));
		    				}
	    				}
	    				
	    				if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
	    					HACommClient.sendExtendVolume(_type, request.getParameter("group"), request.getParameter("name"),_size, _size_units, data_size, total_reservation, data_reservation, request.getParameter("snapshot_hourly_status"), request.getParameter("snapshot_hourly_retention"), request.getParameter("snapshot_daily_status"), request.getParameter("snapshot_daily_retention"),request.getParameter("snapshot_daily_hour"), request.getParameter("snapshot_manual_remove"));
	    				}
	    				
	    				VolumeManager.extendLogicalVolume(request.getParameter("group"), request.getParameter("name"), _size, _size_units, data_size, total_reservation, data_reservation);
	    				if(_size > 0) {
		    				_message = "device.message.logical_volume.extended";
	    				} else if(_size < 0) {
	    					_message = "device.message.logical_volume.shrinked";
	    				} else {
	    					_message = "device.message.logical_volume.configured";
	    				}
	    				
	    				writeDocumentResponse(getLanguageMessage(_message), "/admin/DeviceDisk?type=" + EDIT_LV + "&group=" + request.getParameter("group") + "&name=" + request.getParameter("name"));
	    			}
    				break;
	    		case REMOVE_LV: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.logical_volume_name"));
	    				}
		    			if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				/*
		    				 // TODO: remove with bacula
		    				 * List<String> _linked_storages = _sm.findStorageByLogicalVolume(request.getParameter("group"), request.getParameter("name"));
		    				if (_linked_storages.size() > 0) {
		    					String _msg_linked_storages = "";
		    					for (String storage : _linked_storages)
		    						_msg_linked_storages += storage + " "; 
		    					throw new Exception(getLanguageMessage("device.message.logical_volume.linked_storages")+_msg_linked_storages);
		    				} else {
		    					
		    					if (VolumeManager.isHypervisorVolume(request.getParameter("group"), request.getParameter("name")))
		    						throw new Exception(getLanguageMessage("device.message.logical_volume.ishypervisorVol"));*/

		    					if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
		    						if (VolumeManager.isLocalDeviceGroup(request.getParameter("group")))
		    							HACommClient.sendRemoveVolume(request.getParameter("group"), request.getParameter("name"));
		    					}
		    					_vm.removeLogicalVolume(request.getParameter("group"), request.getParameter("name"));
		    					if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
		    						if (!VolumeManager.isLocalDeviceGroup(request.getParameter("group")))
		    							HACommClient.sendFsTab();
		    					}	
				    			writeDocumentResponse(getLanguageMessage("device.message.logical_volume.removed"), "/admin/DeviceDisk");
		    				//}
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("device.volume_groups.question_volume"), "/admin/DeviceDisk?type=" + REMOVE_LV + "&group=" + request.getParameter("group") + "&name=" + request.getParameter("name") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case SNAPSHOT_LV: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.logical_volume_name"));
	    				}
		    			if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
	    				VolumeManager.createLogicalVolumeSnapshot(request.getParameter("group"), request.getParameter("name"), VolumeManager.LV_SNAPSHOT_MANUAL);
	    				response.sendRedirect("/admin/DeviceDisk?type=" + EDIT_LV + "&group=" + request.getParameter("group") + "&name=" + request.getParameter("name"));
	    				this.redirected=true;
	    			}
	    			break;
	    		case SNAPSHOT_REMOVE_LV: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.logical_volume_name"));
	    				}
		    			if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
		    			if(request.getParameter("snapshot") == null || request.getParameter("snapshot").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_snapshot"));
	    				}
		    			if(request.getParameter("confirm") != null) {
		    				VolumeManager.removeLogicalVolumeSnapshot(request.getParameter("group"), request.getParameter("name"), request.getParameter("snapshot"));
		    				response.sendRedirect("/admin/DeviceDisk?type=" + EDIT_LV + "&group=" + request.getParameter("group") + "&name=" + request.getParameter("name"));
		    				this.redirected=true;
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("device.logical_volumes.question_snapshot"), "/admin/DeviceDisk?type=" + SNAPSHOT_REMOVE_LV + "&group=" + request.getParameter("group") + "&name=" + request.getParameter("name") + "&snapshot=" + request.getParameter("snapshot") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case REFRESH_ALL: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
		    			VolumeManager.refreshLogicalVolumes();
		    			response.sendRedirect("/admin/DeviceDisk");
		    			this.redirected=true;
	    			}
	    			break;
	    		case UMOUNT_VG: {
	    				if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
	    					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
		    			if(request.getParameter("group") == null || request.getParameter("group").isEmpty()) {
	    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
	    				}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				VolumeManager.umountAggregate(request.getParameter("group"));
		    				response.sendRedirect("/admin/DeviceDisk");
		    				this.redirected=true;
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("device.volume_groups.question_aggregate_umount"), "/admin/DeviceDisk?type=" + UMOUNT_VG + "&group=" + request.getParameter("group") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case NEW_CLOUD_DISK: {
	    				List<String> accounts = CloudManager.listAccountAliases();
		    			writeDocumentBack("/admin/DeviceDisk?type=2");
	    				_xhtml_out.print("<form action=\"/admin/DeviceDisk\" name=\"device\" method=\"post\">");
	    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_CLOUD_DISK + "\"/>");
	    				_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/clouds_32.jpg\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.cloud.disks"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("device.cloud.disks.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("device.cloud.disks.new"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    
	    				if (accounts != null && accounts.size()>0) {
		                    _xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"account\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.disks.cloud.disks.account"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"account\"/>");
		        	    	for (String ac : accounts) {
		        	    		Map<String, String> mapAccount = CloudManager.getAccount(ac);
		        	    		_xhtml_out.println("<option value=\""+ac+"\">"+ac+" ("+mapAccount.get("type")+")</option>");
		        	    	}
		        	    	_xhtml_out.println("</select>"); 
		        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	
		                    _xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"size\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.disks.cloud.disks.size"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"form_text\" name=\"size\"/>");
		        	    	_xhtml_out.println("<select class=\"form_select\" name=\"units\">");
		        	    	_xhtml_out.println("<option value=\"m\">MB</option>");
		        	    	_xhtml_out.println("<option value=\"g\">GB</option>");
		        	    	_xhtml_out.println("<option value=\"t\">TB</option>");
		        	    	_xhtml_out.println("</select>");
		        	    	_xhtml_out.println("</div>");
		        	    	
		                    _xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"bucket\">");
		        	    	_xhtml_out.print(getLanguageMessage("device.disks.cloud.disks.bucket"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"form_text\" name=\"bucket\"/> ");
		        	    	_xhtml_out.println(getLanguageMessage("device.disks.cloud.disks.infobucket"));
		        	    	_xhtml_out.println("</div>");

	    				} else {
	    					_xhtml_out.println("<div class=\"standard_form\">");
	    					_xhtml_out.println(getLanguageMessage("device.disks.cloud.disks.no_cloud_accounts"));
	    					_xhtml_out.println("</div>");
		                }
	                    
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
		                    	
	    			}
	    			break;
	    		case STORE_CLOUD_DISK: {
		    			if (request.getParameter("account") == null || request.getParameter("size") == null)
		    				throw new Exception(getLanguageMessage("device.disks.cloud.disks.error.saveparams"));
		    			
		    			Map<String, String> account = CloudManager.getAccount(request.getParameter("account"));
		    			if (account == null || account.size()<=0)
		    				throw new Exception(getLanguageMessage("device.disks.cloud.disks.error.accountnotfound"));
		    			
		    			if (account.get("type").equals("S3") && (request.getParameter("bucket") == null || request.getParameter("bucket").length()<=0))
		    				throw new Exception(getLanguageMessage("device.disks.cloud.disks.error.bucket"));
		    			int type = CloudManager.TYPE_S3;
		    			if (account.get("type").equals("Atmos"))
		    				type = CloudManager.TYPE_ATMOS;
		    			
		    			CloudManager.createCloudSystem(request.getParameter("account"), request.getParameter("size")+request.getParameter("units"), request.getParameter("bucket"), type);
		    			writeDocumentResponse(getLanguageMessage("device.disks.cloud.stored"), "/admin/DeviceDisk?type=2");
	    			}
	    			break;
	    		case STATS_CLOUD_DISK: {
	    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
    				}
	    			
	    			writeDocumentBack("/admin/DeviceDisk");
	    			Map<String, List<String>> listStats = CloudManager.getCloudSystemStats(request.getParameter("name"));
	    			_xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/book_32.png\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("device.disk.cloud.report"));
    				_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("device.disk.cloud.report.info"));
	    			_xhtml_out.println("</div>");
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(request.getParameter("name"));
            		_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
                	_xhtml_out.print("\" alt=\"");
                	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
                	_xhtml_out.println("\"/></a>");
	
                    _xhtml_out.println("</h2>");

                    
                    if (listStats != null && listStats.size()>0) {
                    	for (String deviceCloud: listStats.keySet()) {
                    		List<String> stats = listStats.get(deviceCloud);
                    		if (stats != null && stats.size()>0) {
                    			_xhtml_out.print("<h3>");
                    			_xhtml_out.print(deviceCloud);
                    			_xhtml_out.print("</h3>");
                    			_xhtml_out.println("<fieldset>");
                    			_xhtml_out.println("<table>");
                    			int _offset = 0;
	                    		for (String lineStat : stats) {
			                    	_xhtml_out.print("<tr"); 
			                    	if(_offset % 2 == 0) {
			                    		_xhtml_out.print(" class=\"highlight\"");
			                    	}
			                    	_xhtml_out.print(">");
									_xhtml_out.print("<td>");
									_xhtml_out.print(lineStat);
							        _xhtml_out.println("</td>");
							        _xhtml_out.println("</tr>");
							        _offset++;
	                    		}
                    		 } else {
                             	_xhtml_out.println("<tr>");
                             	_xhtml_out.println(getLanguageMessage("device.disk.cloud.report.nostats"));
                             	_xhtml_out.println("</tr>");
                             }
                    	}
                    }
                   
                    _xhtml_out.println("</table>");
                    _xhtml_out.println("<br/>");
                    _xhtml_out.println("</fieldset>");
                    _xhtml_out.println("<div class=\"clear\"/></div>");
        	    	_xhtml_out.print("</div>");
	    			}
	    			break;
	    	case REMOVE_CLOUD_DISK: {
		    		if(request.getParameter("confirm") != null) {
	    				CloudManager.removeCloudDisk(request.getParameter("account"), request.getParameter("device"), request.getParameter("cld"));
	    				writeDocumentResponse(getLanguageMessage("device.disks.cloud.disk.removed"), "/admin/DeviceDisk");
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("device.disks.cloud.disk.remove.question"), "/admin/DeviceDisk?type=" + REMOVE_CLOUD_DISK + "&account=" + request.getParameter("account") + "&device=" + request.getParameter("device") + "&cld=" + request.getParameter("cld") + "&confirm=true", null);
	    			}
	    		}
	    		break;
	    	case RECOVER_VG: {
		    		writeDocumentBack("/admin/DeviceDisk");
					_xhtml_out.print("<form action=\"/admin/DeviceDisk\" name=\"device\" method=\"post\">");
					_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + RECOVER_VG_STORE + "\"/>");
					_xhtml_out.println("<h1>");
					_xhtml_out.print("<img src=\"/images/drive_32.png\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.disk"));
					_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("device.volume_groups.info_recover"));
	    			_xhtml_out.println("</div>");
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("device.volume_groups.recover_vg"));
	                _xhtml_out.print("<a href=\"javascript:submitForm(document.device.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                _xhtml_out.print(getLanguageMessage("common.message.save"));
				    _xhtml_out.print("\" alt=\"");
				    _xhtml_out.print(getLanguageMessage("common.message.save"));
				    _xhtml_out.println("\"/></a>");
	                _xhtml_out.println("</h2>");
	                _xhtml_out.println("<fieldset>");
	                _xhtml_out.println("<div class=\"standard_form\">");
	    	    	_xhtml_out.print("<label for=\"name\">");
	    	    	_xhtml_out.print(getLanguageMessage("device.volume_groups.name"));
	    	    	_xhtml_out.println(": </label>");
	    	    	_xhtml_out.println("<input class=\"form_text\" name=\"name\"/>");
	    	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	    	    	_xhtml_out.println("</div>");
	    	    	_xhtml_out.println("</fieldset>");
	     	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	     	    	_xhtml_out.print("</div>");
	    		}
	    		break;
	    	case RECOVER_VG_STORE: {
    				if(request.getParameter("confirm") != null) {
    					ZFSConfiguration.recoverDamagedGroup(request.getParameter("name"));
    					writeDocumentResponse(getLanguageMessage("device.disks.recover.success"), "/admin/DeviceDisk");
    				} else {
    					writeDocumentQuestion(getLanguageMessage("device.disks.recover.question"), "/admin/DeviceDisk?type=" + RECOVER_VG_STORE + "&name=" + request.getParameter("name") + "&confirm=true", null);
    				}
    			}
    			break;
	    	case REMOVE_POOL_DISK: {
	    			if(request.getParameter("confirm") != null) {
			    		if(request.getParameter("groupName") == null || request.getParameter("groupName").isEmpty())
			    			throw new Exception(getLanguageMessage("device.volume_groups.exception.volume_name"));
			    		String groupName = request.getParameter("groupName");
			    		if(request.getParameter("diskId") == null || request.getParameter("diskId").isEmpty())
			    			throw new Exception(getLanguageMessage("device.volume_groups.exception.diskId"));
			    		String device = request.getParameter("diskId");
			    		ZFSConfiguration.removePookDisk(groupName, device);
			    		writeDocumentResponse(getLanguageMessage("device.disks.pool.remove.disk.success"),"/admin/DeviceDisk?type="+EDIT_VG+"&name="+groupName);
	    			} else {
	    				writeDocumentQuestion(getLanguageMessage("device.disks.pool.remove.disk.question"), "/admin/DeviceDisk?type=" + REMOVE_POOL_DISK + "&groupName=" + request.getParameter("groupName") + "&diskId="+request.getParameter("diskId")+"&confirm=true", null);
	    			}
		    	}
		    	break;
	    	}
 		} catch(Exception _ex) {
 			if (type == STORE_LV)
 				writeDocumentError(getWBSLocalizedExMessage(_ex.getMessage()),"/admin/DeviceDisk?type="+NEW_LV);
 			else if (type == STORE_EXTEND_LV || type == STORE_VG)
 				writeDocumentError(getWBSLocalizedExMessage(_ex.getMessage()));
 			else
 				writeDocumentError(getWBSLocalizedExMessage(_ex.getMessage()),"/admin/DeviceDisk");
	    } finally {
	    	writeDocumentFooter();
	    }
	}
	
	/*public String generateJSPieVol(PrintWriter _xhtml_out, Map<String, String> lv, Map<String, String> agg) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_xhtml_out.println("<div id=\"volPies\" style=\"margin:0px;overflow:hidden;width:100%;\">");
		List<String> vars = new ArrayList<String>();
		String nameVar = null;
		
		String name = "pieQuota";
		try {
			nameVar = "volQuota";
			vars.add(nameVar);
			_sb.append(getJSVarVolAggQuotaPie(nameVar, lv, agg));
			_sb.append(getJSPieHighGroup(name, getLanguageMessage("device.disks.plot.title.volquotas"), vars));
			_xhtml_out.println("<div id=\""+name+"\" style=\"margin:0px;padding:0px;margin-left:1%;overflow:hidden;width:49%;float:left;*display:inline;\"> </div>");
		} catch (Exception ex) {
			logger.error("Error al crear variables de ocupación. {}", ex);
		}
		
		vars = new ArrayList<String>();
		name = "pieReservation";
		try {
			nameVar = "volReservation";
			vars.add(nameVar);
			_sb.append(getJSVarReservationPie(nameVar, lv));
			_sb.append(getJSPieHighGroup(name, getLanguageMessage("device.disks.plot.title.reservation.volume"), vars));
			_xhtml_out.println("<div id=\""+name+"\" style=\"margin:0px;padding:0px;margin-right:1%;overflow:hidden;width:49%;float:left;*display:inline;\"> </div>");
		} catch (Exception ex) {
			logger.error("Error al crear variables de ocupación. {}", ex);
		}
	
		_xhtml_out.println("</div>");
		return _sb.toString();
	}*/
	
	public String generateJSPieHighGroupOcupedPhysical(PrintWriter _xhtml_out, List<Map<String, String>> lvs, Map<String, String> vg, List<Map<String, String>> _vg_disks, String clase, int numAgg) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_xhtml_out.println("<div id=\"agg-pie-physical"+numAgg+"\" class=\""+clase+"\" style=\"margin:0px;overflow:hidden;\">");
		List<String> vars = new ArrayList<String>();
		String nameVar = null;
		
		String title2 = "device.disks.plot.title.disks";
		
		vars = new ArrayList<String>();
		
		try {
			nameVar = "disksFree";
			vars.add(nameVar);
			if (!vg.containsKey("allocated")) {
				_sb.append(getJSVarDisksFree(nameVar, _vg_disks, vg));
			} else {
				_sb.append(getJSVarZpoolFree(nameVar, vg));
			}
		} catch (Exception ex) {
			logger.error("Error al crear gráficas de espacio libre en discos. {}", ex);
			vars.remove(nameVar);
		}
		
		try {
			if (vars != null && vars.size()>0) {
				String name = "pieDisksAggPhysical"+numAgg;
				_sb.append(getJSPieHighGroup(name, getLanguageMessage(title2), vars));
				_xhtml_out.println("<div style=\"padding:0px;overflow:hidden;width:100%;float:left;*display:inline;\"><div id=\""+name+"\" style=\"margin:0 auto;width:100%;height:100%\"></div></div>");
			}
		} catch (Exception ex) {
			logger.error("Error al crear gráficas de discos/discos libres. {}", ex);
		}
		
		_xhtml_out.println("</div>");
		return _sb.toString();
	}
	
	public String generateJSPieHighGroupOcupedLogical(PrintWriter _xhtml_out, Map<String, String> vg, String clase, int numAgg) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_xhtml_out.println("<div id=\"agg-pie-ocuped-logical"+numAgg+"\" class=\""+clase+"\" style=\"margin:0px;overflow:hidden;\">");
		List<String> vars = new ArrayList<String>();
		String nameVar = null;
		String title2 = "device.disks.plot.title.disks.logical";
		try {
			if (vg.containsKey("allocated")) {
				nameVar = "filesystemFree";
				vars.add(nameVar);
				_sb.append(getJSVarFilesystemFree(nameVar, vg));
			}
		} catch (Exception ex) {
			logger.error("Error al crear gráficas de espacio libre en discos. {}", ex);
			vars.remove(nameVar);
		}
		try {
			if (vars != null && vars.size()>0) {
				String name = "pieAggLogical"+numAgg;
				_sb.append(getJSPieHighGroup(name, getLanguageMessage(title2), vars));
				_xhtml_out.println("<div style=\"padding:0px;overflow:hidden;width:100%;float:left;*display:inline;\"><div id=\""+name+"\" style=\"margin:0 auto;width:100%;height:100%\"></div></div>");
			}
		} catch (Exception ex) {
			logger.error("Error al crear gráficas de discos/discos libres. {}", ex);
		}
		
		_xhtml_out.println("</div>");
		return _sb.toString();
	}
	
	
	public String generateJSPieHighGroup(PrintWriter _xhtml_out, int numAg, List<Map<String, String>> lvs, Map<String, String> vg, List<Map<String, String>> _vg_disks) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_xhtml_out.println("<div id=\"agg"+numAg+"\" style=\"margin:0px;overflow:hidden;width:100%;\">");
		List<String> vars = new ArrayList<String>();
		String nameVar = null;
		
		String title1 = "device.disks.plot.title.disks.logical";
		String title2 = "device.disks.plot.title.disks";
		
		String minTitSize = "device.disks.plot.quota";
		
		if (!vg.containsKey("allocated")) {
			title1 = "device.disks.plot.title.ocuppation.xfs";
			title2 = "device.disks.plot.title.disks.xfs";
			minTitSize = "device.disks.plot.size";
		}
		
		
		if (vg.containsKey("allocated")) {
			try {
				if (vg.containsKey("allocated")) {
					nameVar = "filesystemFree";
					vars.add(nameVar);
					_sb.append(getJSVarVolsUsed(nameVar, title1, lvs, vg));
					//_sb.append(getJSVarFilesystemFree(nameVar, vg));
				}
			} catch (Exception ex) {
				logger.error("Error al crear gráficas de espacio libre en discos. {}", ex);
				vars.remove(nameVar);
			}
		} else {
			try {
				nameVar = "volsOcuppation"+numAg;
				vars.add(nameVar);
				_sb.append(getJSVarVolsOcupation(nameVar, getLanguageMessage(minTitSize), lvs, vg));
			} catch (Exception ex) {
				logger.error("Error al crear variables de ocupación. {}", ex);
			}
		}
		
		String name = "pieVolsAgg"+numAg;
		try {
			_sb.append(getJSPieHighGroup(name, getLanguageMessage(title1), vars));
			if (vg.containsKey("allocated"))
				_xhtml_out.println("<div style=\"padding:0px;overflow:hidden;width:49%;float:left;*display:inline;\"><div id=\""+name+"\" style=\"margin:0 auto;width:100%;height:100%\"></div></div>");
			else
				_xhtml_out.println("<div style=\"padding:0px;overflow:hidden;width:98%;float:left;*display:inline;\"><div id=\""+name+"\" style=\"margin:0 auto;width:100%;height:100%\"></div></div>");
		} catch (Exception ex) {
			logger.error("Error al crear gráficas de ocupación/reservas. {}", ex);
		}
		
		vars = new ArrayList<String>();
		if (vg.containsKey("allocated")) {
			try {
				nameVar = "disksFree"+numAg;
				vars.add(nameVar);
				_sb.append(getJSVarZpoolFree(nameVar, vg));
			} catch (Exception ex) {
				logger.error("Error al crear gráficas de espacio libre en discos. {}", ex);
				vars.remove(nameVar);
			}
		}
		
		try {
			if (vars != null && vars.size()>0) {
				name = "pieDisksAgg"+numAg;
				_sb.append(getJSPieHighGroup(name, getLanguageMessage(title2), vars));
				_xhtml_out.println("<div style=\"padding:0px;overflow:hidden;width:49%;float:left;*display:inline;\"><div id=\""+name+"\" style=\"margin:0 auto;width:100%;height:100%\"></div></div>");
			}
		} catch (Exception ex) {
			logger.error("Error al crear gráficas de discos/discos libres. {}", ex);
		}
		
		_xhtml_out.println("</div>");
		return _sb.toString();
	}
	
	public String getJSVarVolsOcupation(String nameVar, String minTitle, List<Map<String, String>> lvs, Map<String, String> vg) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append(" var "+nameVar+" = [];\n");
		
		if (vg.get("freequota") != null) {
			_sb.append(" "+nameVar+".push({\n");
			_sb.append(" 	name: '"+getLanguageMessage("device.disks.plot.free")+": "+vg.get("freequota")+"',\n");
			_sb.append(" 	y: "+vg.get("freequota-raw")+",\n");
			_sb.append(" 	color: '#44aa00'\n");
			_sb.append(" });\n");
		}
		else {
			_sb.append(" "+nameVar+".push({\n");
			_sb.append(" 	name: '"+getLanguageMessage("device.disks.plot.free")+": "+vg.get("free")+"',\n");
			_sb.append(" 	y: "+vg.get("free-raw")+",\n");
			_sb.append(" 	color: '#44aa00'\n");
			_sb.append(" });\n");
		}
		
		int i=0;
		for (Map<String, String> lv: lvs) {
			String label = lv.get("name")+": "+lv.get("size");
			Double sizeraw=Double.parseDouble(lv.get("size-raw"));
			if(lv.get("type") != null && "MIRROR".equalsIgnoreCase(lv.get("type"))) {
				sizeraw*=2;
				label = lv.get("name")+" (mirrored): "+lv.get("size")+"*2'";
			} 
			_sb.append(" "+nameVar+".push({\n");
			_sb.append(" 	name: '"+label+"',\n");
			_sb.append(" 	y: "+sizeraw+",\n");
			_sb.append(" 	color: Highcharts.getOptions().colors["+i+"]\n");
			_sb.append(" });\n");
			i++;
		}
		
		return _sb.toString();
	}
	
	public String getJSVarVolsReservation(String nameVar, String minTitle, List<Map<String, String>> lvs, Map<String, String> vg) throws Exception {
		
		StringBuilder _sb = new StringBuilder();
		_sb.append(" var "+nameVar+" = [];\n");

		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+minTitle + " "+getLanguageMessage("device.disks.plot.free.reservation")+": "+vg.get("freereservation")+"',\n");
		_sb.append(" 	y: "+vg.get("freereservation-raw")+",\n");
		_sb.append(" 	color: 'grey'\n");
		_sb.append(" });\n");
		
		/*_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+minTitle + " "+getLanguageMessage("device.disks.plot.zfsys")+": "+vg.get("zfssys")+"',\n");
		_sb.append(" 	y: "+vg.get("zfssys-raw")+",\n");
		_sb.append(" 	color: '#98860f'\n");
		_sb.append(" });\n");*/
		
		int i=0;
		for (Map<String, String> lv: lvs) {
			String label = minTitle + " "+ lv.get("name")+": "+lv.get("reservation");
			_sb.append(" "+nameVar+".push({\n");
			_sb.append(" 	name: '"+label+"',\n");
			_sb.append(" 	y: "+lv.get("reservation-raw")+",\n");
			_sb.append(" 	color: Highcharts.getOptions().colors["+i+"]\n");
			_sb.append(" });\n");
			i++;
		}
		
		return _sb.toString();
	}
	
	public String getJSVarVolsUsed(String nameVar, String minTitle, List<Map<String, String>> lvs, Map<String, String> vg) throws Exception {
		
		StringBuilder _sb = new StringBuilder();
		_sb.append(" var "+nameVar+" = [];\n");

		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+getLanguageMessage("device.disks.plot.free")+": "+vg.get("fs-free")+"',\n");
		_sb.append(" 	y: "+vg.get("fs-free-raw")+",\n");
		_sb.append(" 	color: '#44aa00'\n");
		_sb.append(" });\n");
		
		/*_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+minTitle + " "+getLanguageMessage("device.disks.plot.zfsys")+": "+vg.get("zfssys")+"',\n");
		_sb.append(" 	y: "+vg.get("zfssys-raw")+",\n");
		_sb.append(" 	color: '#98860f'\n");
		_sb.append(" });\n");*/
		
		int i=0;
		Double totalUsed = 0d;
		Double totalReserved = 0d;
		for (Map<String, String> lv: lvs) {
			totalUsed = Double.parseDouble(lv.get("total-used-raw"));
			totalReserved = Double.parseDouble(lv.get("reservation-raw"));
			if (totalReserved > totalUsed)
				totalUsed=totalReserved;
			String label = lv.get("name")+": "+VolumeManager.getFormatSize(String.valueOf(totalUsed)+" B");
			_sb.append(" "+nameVar+".push({\n");
			_sb.append(" 	name: '"+label+"',\n");
			_sb.append(" 	y: "+String.valueOf(totalUsed)+",\n");
			_sb.append(" 	color: Highcharts.getOptions().colors["+i+"]\n");
			_sb.append(" });\n");
			i++;
		}
		
		return _sb.toString();
	}
	
	
	public String getJSVarDisks(String nameVar, List<Map<String, String>> _vg_disks) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(" var "+nameVar+" = [];\n");

		int i = 1;
		for (Map<String, String> disk: _vg_disks) {
			_sb.append(" "+nameVar+".push({\n");
			_sb.append(" 	name: '     "+disk.get("device")+": "+disk.get("size")+"',\n");
			_sb.append(" 	y: "+VolumeManager.getByteSizeFromHuman(disk.get("size-raw"))+",\n");
			_sb.append(" 	color: Highcharts.getOptions().colors[Highcharts.getOptions().colors.length-"+i+"]\n");
			_sb.append(" });\n");
			i++;
		}
		
		return _sb.toString();
	}
	
	
	public String getJSVarDisksFree(String nameVar, List<Map<String, String>> _vg_disks, Map<String, String> vg) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(" var "+nameVar+" = [];\n");
		
		double size = Double.parseDouble(vg.get("size-raw"));
		double occuped = size; 
		for (Map<String, String> disk: _vg_disks) {
			occuped-=VolumeManager.getByteSizeFromHuman(disk.get("free-raw"));
		}
		double free = size - occuped;
		if (free < 0)
			free = 0;
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+getLanguageMessage("device.disks.plot.free")+": "+VolumeManager.getFormatSize(String.valueOf(free)+" B")+"',\n");
		_sb.append(" 	y: "+free+",\n");
		_sb.append(" 	color: '#44aa00'\n");
		_sb.append(" });\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+getLanguageMessage("device.disks.plot.ocuped")+": "+VolumeManager.getFormatSize(String.valueOf(occuped)+" B")+"',\n");
		_sb.append(" 	y: "+occuped+",\n");
		_sb.append(" 	color: '#FF0000'\n");
		_sb.append(" });\n");
		
		return _sb.toString();
	}
	
	
	public String getJSVarFilesystemFree(String nameVar, Map<String, String> vg) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(" var "+nameVar+" = [];\n");
		double used = Double.parseDouble(vg.get("fs-used-raw"));
		double free = Double.parseDouble(vg.get("fs-free-raw"));
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+getLanguageMessage("device.disks.plot.free")+": "+vg.get("fs-free")+"',\n");
		_sb.append(" 	y: "+free+",\n");
		_sb.append(" 	color: '#44aa00'\n");
		_sb.append(" });\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '"+getLanguageMessage("device.disks.plot.ocuped")+": "+vg.get("fs-used")+"',\n");
		_sb.append(" 	y: "+used+",\n");
		_sb.append(" 	color: '#FF0000'\n");
		_sb.append(" });\n");
		
		return _sb.toString();
	}
	
	public String getJSVarZpoolFree(String nameVar, Map<String, String> vg) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(" var "+nameVar+" = [];\n");
		double allocated = Double.parseDouble(vg.get("allocated-raw"));
		double free = Double.parseDouble(vg.get("size-raw"))-Double.parseDouble(vg.get("allocated-raw"));
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.free")+": "+VolumeManager.getFormatSize(String.valueOf(free)+" B")+"',\n");
		_sb.append(" 	y: "+free+",\n");
		_sb.append(" 	color: '#44aa00'\n");
		_sb.append(" });\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.ocuped")+": "+vg.get("allocated")+"',\n");
		_sb.append(" 	y: "+allocated+",\n");
		_sb.append(" 	color: '#FF0000'\n");
		_sb.append(" });\n");
		
		return _sb.toString();
	}
	
	/*public String getJSVarReservationPie(String nameVar, Map<String, String> volume) throws Exception {
		StringBuilder _sb = new StringBuilder();
		Double currentDataReservation = Double.parseDouble(volume.get("refreservation-raw"));
	    Double currentSnapReservation = 0D;
	    String currentSnap = "0 B";
	    if (Double.parseDouble(volume.get("reservation-raw")) > 0 && currentDataReservation > 0) {
	    	currentSnapReservation = Double.parseDouble(volume.get("reservation-raw")) - currentDataReservation;
	    	if (currentSnapReservation > 0) {
	    		currentSnap = VolumeManager.getFormatSize(currentSnapReservation+" B");
	    	}
	    }
	    
	    Double freeReservation = Double.parseDouble(volume.get("size-raw"))-currentSnapReservation-currentDataReservation;
	    
		_sb.append(" var "+nameVar+" = [];\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.volume.free")+": "+VolumeManager.getFormatSize(freeReservation+" B")+"',\n");
		_sb.append(" 	y: "+freeReservation+",\n");
		_sb.append(" 	color: '#44aa00'\n");
		_sb.append(" });\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.volume.data")+": "+volume.get("refreservation")+"',\n");
		_sb.append(" 	y: "+currentDataReservation+",\n");
		_sb.append(" 	color: Highcharts.getOptions().colors[4]\n");
		_sb.append(" });\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.volume.snapshot")+": "+currentSnap+"',\n");
		_sb.append(" 	y: "+currentSnapReservation+",\n");
		_sb.append(" 	color: Highcharts.getOptions().colors[5]\n");
		_sb.append(" });\n");
		
		return _sb.toString();
	}*/
	
	/*public String getJSVarVolAggQuotaPie(String nameVar, Map<String, String> volume, Map<String, String> agg) throws Exception {
		StringBuilder _sb = new StringBuilder();
		Double dataquota = Double.parseDouble(volume.get("refquota-raw"));
	    Double snapshotquota = Double.parseDouble(volume.get("size-raw")) - Double.parseDouble(volume.get("refquota-raw"));
	    String snapshotquotaCad = "0 B";
	    if (snapshotquota > 0)
	    	snapshotquotaCad = VolumeManager.getFormatSize(snapshotquota+" B");

	    Double free = Double.parseDouble(agg.get("freequota-raw"));
	    Double aggOtherOcuped = Double.parseDouble(agg.get("quota-raw"))-dataquota-snapshotquota;
	    
		_sb.append(" var "+nameVar+" = [];\n");
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.agg.free")+": "+agg.get("freequota")+"',\n");
		_sb.append(" 	y: "+free+",\n");
		_sb.append(" 	color: '#44aa00'\n");
		_sb.append(" });\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.agg.others")+": "+VolumeManager.getFormatSize(aggOtherOcuped+" B")+"',\n");
		_sb.append(" 	y: "+aggOtherOcuped+",\n");
		_sb.append(" 	color: 'grey'\n");
		_sb.append(" });\n");
	    
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.volume.quota")+": "+volume.get("refquota")+"',\n");
		_sb.append(" 	y: "+dataquota+",\n");
		_sb.append(" 	color: Highcharts.getOptions().colors[4]\n");
		_sb.append(" });\n");
		
		_sb.append(" "+nameVar+".push({\n");
		_sb.append(" 	name: '     "+getLanguageMessage("device.disks.plot.volume.snapshotquota")+": "+snapshotquotaCad+"',\n");
		_sb.append(" 	y: "+snapshotquota+",\n");
		_sb.append(" 	color: Highcharts.getOptions().colors[5]\n");
		_sb.append(" });\n");

		return _sb.toString();
	}*/
	
	
	public String getJSPieHighGroup(String divChart, String title, List<String> vars) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("var chart = new Highcharts.Chart({\n");
		_sb.append("    chart: {\n");
		_sb.append("		renderTo: '"+divChart+"',\n");
		_sb.append("		type: 'pie'\n");
		_sb.append("	},\n");
		_sb.append("	title: {\n");
		_sb.append("		text: '"+title+"'\n");
		_sb.append("	},\n");
		_sb.append("	tooltip: {\n");
		_sb.append("		formatter: function() {\n");
		_sb.append("				return '<b>'+ this.point.name +'</b> '+ parseFloat(this.percentage).toFixed(2) +'%';\n");
		_sb.append("			}\n");
		_sb.append("	},\n");
		_sb.append("	plotOptions: {\n");
		_sb.append("		pie: {\n");
		_sb.append("			allowPointSelect: true,\n");
		_sb.append("			cursor: 'pointer',\n");
		_sb.append("			dataLabels: {\n");
		_sb.append("				enabled: false\n");
		_sb.append("			},\n");
		_sb.append("			showInLegend: true\n");
		_sb.append("		}\n");
		_sb.append("	},\n");
		_sb.append("	series: [\n");
		int i=0;
		int inner = 20;
		int size = 80;
		if (vars.size() == 1)
			size = 100;
		else if (vars.size() == 3)
			size = 60;
		for (String var : vars) {
			if (i != 0)
				_sb.append(",");
			_sb.append("	{\n");
			_sb.append("		name: '"+var+"',\n");
			_sb.append("		data: "+var+",\n");
			_sb.append("		size: '"+size+"%',\n");
			_sb.append("		innerSize: '"+inner+"%',\n");
			_sb.append("		dataLabels: {\n");
			/*if (i != 0 || vars.size() == 1) {
				_sb.append("           formatter: function() {\n");
				_sb.append("           if (this.point.name.length < 30 ) \n");
				_sb.append("				return this.percentage > 5 ? '<b>'+ this.point.name.substring(5) +'</b> ': null;\n");
				_sb.append("           else \n");
				_sb.append("           		return null;\n");
				_sb.append("			}\n");
				_sb.append("	    , distance: -55,\n");
				_sb.append("	    color: '#D4D2D2'\n");
			} else {
				_sb.append("           formatter: function() {\n");
				_sb.append("				return this.percentage > 0.15 ? '<b>'+ this.point.name.substring(5) +'</b> '+ parseFloat(this.percentage).toFixed(2) +'%'  : null;\n");
				_sb.append("			}\n");
			} else {*/
				_sb.append("           formatter: function() {\n");
				_sb.append("				return null;\n");
				_sb.append("			}\n");
			//}
			_sb.append("		}\n");
			_sb.append("	}\n");
			i++;
			inner = size;
			size+=20;
		}
		_sb.append("	]\n");
		_sb.append("});\n");
		return _sb.toString();
	}
	
	public String getJSVolumeHighBars(String divBars, Double volumeOcupation, Double compression, String nameVol, boolean barOcuppation) {
		StringBuilder _sb = new StringBuilder();
		if (barOcuppation || compression != null) {
			_sb.append("var serie = [");
			if (barOcuppation)
				_sb.append(volumeOcupation.intValue());
			if (compression != null)
				_sb.append(","+compression.intValue());
			_sb.append("];\n");
			
			_sb.append("var ticks = ['"+getLanguageMessage("device.disks.plot.volume.ocupation")+"'");
			if (compression != null)
				_sb.append(",'"+getLanguageMessage("device.disks.plot.volume.compression")+"'");
			_sb.append("];\n");
			
			_sb.append("var chart = new Highcharts.Chart({\n");
			_sb.append("	chart: {\n");
			_sb.append("			renderTo: '"+divBars+"',\n");
			_sb.append("			type: 'column'\n");
			_sb.append("		},\n");
			_sb.append("		title: {\n");
			_sb.append("			text: ''\n");
			_sb.append("		},\n");
			_sb.append("		xAxis: {\n");
			_sb.append("			categories: ticks\n");
			_sb.append("		},\n");
			_sb.append("		yAxis: {\n");
			_sb.append("			min: 0,\n");
			_sb.append("			title: {\n");
			_sb.append("			text: '"+getLanguageMessage("device.disks.plot.volume.percentage")+"'\n");
			_sb.append("		},\n");
			_sb.append("		stackLabels: {\n");
			_sb.append("			enabled: true,\n");
			_sb.append("    	    style: {\n");
			_sb.append("				fontWeight: 'bold',\n");
			_sb.append("				color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'\n");
			_sb.append("			}\n");
			_sb.append("		}\n");
			_sb.append("	},\n");
			_sb.append("	tooltip: {\n");
			_sb.append("		formatter: function() {\n");
			_sb.append("                    return '<b>'+ this.x +'</b><br/>'+this.series.name +': '+ this.y+ ' %';\n");
			_sb.append("		}\n");
			_sb.append("	},\n");
			_sb.append("	plotOptions: {\n");
			_sb.append("		column: {\n");
			_sb.append("              stacking: 'normal',\n");
			_sb.append("              dataLabels: {\n");
			_sb.append("				enabled: true,\n");
			_sb.append("				color: (Highcharts.theme && Highcharts.theme.dataLabelsColor) || 'white'\n");
			_sb.append("               }\n");
			_sb.append("		}\n");
			_sb.append("	},\n");
			_sb.append("	series: [{\n");
			_sb.append("		name: '"+nameVol+"',\n");
			_sb.append("		data: serie\n");
			_sb.append("	}]\n");
			_sb.append("});\n");
		}	
		return _sb.toString();
	}
	
	public String getDeduplicationBar(String divName, String title, String titleSpaceWithoutDedup, String aggName, String usedByDedup, String usedByDedupRaw, String saved, String savedRaw, String savedLabel, String usedByDedupLabel) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("	var chartDeupBar = new Highcharts.Chart({\n");
		_sb.append("		chart: {\n");
		_sb.append("			renderTo: '"+divName+"',\n");
		_sb.append("			type: 'column'\n");
		_sb.append("		},\n");
		_sb.append("		title: {\n");
		_sb.append("	    	text: '"+title+"'\n");
		_sb.append("		},\n");
		_sb.append("		xAxis: {\n");
		_sb.append("	    	categories: ['"+aggName+"']\n");
		_sb.append("		},\n");
		_sb.append("		yAxis: {\n");
		_sb.append("	    	min: 0,\n");
		_sb.append("	        title: {\n");
		_sb.append("				text: '"+titleSpaceWithoutDedup+"'\n");
		_sb.append("	        }\n");
		_sb.append("		},\n");
		_sb.append("		tooltip: {\n");
		_sb.append("			formatter: function() {\n");
		_sb.append("                    return '<b>'+ this.x +'</b><br/>'+this.series.name +': '+ parseFloat(this.percentage).toFixed(2)+ ' %';\n");
		_sb.append("			}\n");
		_sb.append("		},\n");
		_sb.append("		plotOptions: {\n");
		_sb.append("	    	column: {\n");
		_sb.append("				stacking: 'percent'\n");
		_sb.append("	        }\n");
		_sb.append("		},\n");
		_sb.append("		series: [ 	{\n");
		_sb.append("	                	name: '"+savedLabel+" "+saved+"',\n");
		_sb.append("	                	data: ["+savedRaw+"],\n");
		_sb.append("	                	color: '#44aa00'\n");
		_sb.append("					},{\n");
		_sb.append("	                	name: '"+usedByDedupLabel+" "+usedByDedup+"',\n");
		_sb.append("	                	data: ["+usedByDedupRaw+"],\n");
		_sb.append("	                	color: '#FF0000'\n");
		_sb.append("				}]\n");
		_sb.append("});\n");
		return _sb.toString();
	}
	
	public String getVolUsedBars(String divName, Map<String, String> volume) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("	var chartVolUsedBars = new Highcharts.Chart({\n");
		_sb.append("		chart: {\n");
		_sb.append("			renderTo: '"+divName+"',\n");
		_sb.append("		    type: 'column'\n");
		_sb.append("	    },\n");
		_sb.append("		title: {\n");
		_sb.append("			text: '"+getLanguageMessage("device.logical_volumes.title.volused.bars")+" <b>"+volume.get("name")+"</b>'\n");
		_sb.append("		},\n");
		_sb.append("		xAxis: {\n");
		_sb.append("	        categories: ['']\n");
		_sb.append("		},\n");
		_sb.append("		yAxis: {\n");
		_sb.append("			allowDecimals: true,\n");
		_sb.append("			min: 0,\n");
		_sb.append("			title: {\n");
		_sb.append("				text: '"+getLanguageMessage("common.space")+"'\n");
		_sb.append("			},\n");
		_sb.append("			stackLabels: {\n");
		_sb.append("				enabled: true,\n");
		_sb.append("				style: {\n");
		_sb.append("					fontWeight: 'bold',\n");
		_sb.append("					color: 'gray'\n");
		_sb.append("				},\n");
		_sb.append("				formatter: function() {\n");
		_sb.append("					return  this.stack;\n");
		_sb.append("				}\n");
		_sb.append("			}\n");
		_sb.append("		},\n");
		_sb.append("		tooltip: {\n");
		_sb.append("			formatter: function() {\n");
		_sb.append("				return  this.series.name+': '+ parseFloat(this.percentage).toFixed(2)+ ' %';\n");
		_sb.append("			}\n");
		_sb.append("		},\n");
		_sb.append("		plotOptions: {\n");
		_sb.append("			column: {\n");
		_sb.append("				stacking: 'normal'\n");
		_sb.append("			}\n");
		_sb.append("		},\n");
		_sb.append("		series: [");
		if (VolumeManager.isMount(volume.get("vg"), volume.get("name"))) {
			_sb.append("				{\n");
			_sb.append("					name: '"+getLanguageMessage("common.data.quota")+" "+volume.get("refquota")+"',\n");
			_sb.append("					data: ["+volume.get("refquota-raw")+"],\n");
			_sb.append("					stack: '"+getLanguageMessage("common.quota")+"'\n");
			_sb.append("				}, {\n");
			_sb.append("					name: '"+getLanguageMessage("common.snap.quota")+" "+VolumeManager.getFormatSize((Double.parseDouble(volume.get("size-raw"))-Double.parseDouble(volume.get("refquota-raw")))+" B")+"',\n");
			_sb.append("					data: ["+(Double.parseDouble(volume.get("size-raw"))-Double.parseDouble(volume.get("refquota-raw")))+"],\n");
			_sb.append("					stack: '"+getLanguageMessage("common.quota")+"'\n");
			_sb.append("				},");
		}
		_sb.append("				{\n");
		_sb.append("					name: '"+getLanguageMessage("common.data.reservation")+" "+volume.get("refreservation")+"',\n");
		_sb.append("					data: ["+volume.get("refreservation-raw")+"],\n");
		_sb.append("					stack: '"+getLanguageMessage("common.reservation")+"'\n");
		_sb.append("				}, {\n");
		_sb.append("					name: '"+getLanguageMessage("common.snap.reservation")+" "+VolumeManager.getFormatSize((Double.parseDouble(volume.get("reservation-raw"))-Double.parseDouble(volume.get("refreservation-raw")))+" B")+"',\n");
		_sb.append("					data: ["+(Double.parseDouble(volume.get("reservation-raw"))-Double.parseDouble(volume.get("refreservation-raw")))+"],\n");
		_sb.append("					stack: '"+getLanguageMessage("common.reservation")+"'\n");
		_sb.append("				},{\n");
		_sb.append("					name: '"+getLanguageMessage("common.data.used")+" "+VolumeManager.getFormatSize(volume.get("used-raw")+" B")+"',\n");
		_sb.append("					data: ["+volume.get("used-raw")+"],\n");
		_sb.append("					stack: '"+getLanguageMessage("common.used")+"'\n");
		_sb.append("				}, {\n");
		_sb.append("					name: '"+getLanguageMessage("common.snap.used")+" "+VolumeManager.getFormatSize(volume.get("snapshot-used-raw")+" B")+"',\n");
		_sb.append("					data: ["+volume.get("snapshot-used-raw")+"],\n");
		_sb.append("					stack: '"+getLanguageMessage("common.used")+"'\n");
		_sb.append("				},{\n");
		_sb.append("					name: '"+getLanguageMessage("common.reservation.used")+" "+VolumeManager.getFormatSize(volume.get("refreservation-used-raw")+" B")+"',\n");
		_sb.append("					data: ["+volume.get("refreservation-used-raw")+"],\n");
		_sb.append("					stack: '"+getLanguageMessage("common.used")+"'\n");
		_sb.append("			}]\n");
		_sb.append("	});\n");
		return _sb.toString();
	}
	
	public String getSizeSliderSimpleJS(String divSlider, String divText, Double min, Double max, Double value) throws Exception {
		int step = 1;
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("$( '#"+divSlider+"' ).slider({\n");
		_sb.append("value: "+value.intValue()+",\n");
		_sb.append("min: "+min.intValue()+",\n");
		_sb.append("max: "+max.intValue()+",\n");
		_sb.append("step: "+step+",\n");
		_sb.append("slide: function( event, ui ) {\n");
		_sb.append("$( '#"+divText+"' ).val( ui.value );\n");
		_sb.append("}\n});\n");
		
		_sb.append("$( '#"+divText+"' ).change(function() {\n");
		_sb.append("	$( '#"+divSlider+"' ).slider( 'value', $(this).val() );\n");
		_sb.append("});\n");
		
		return _sb.toString();
	}
	
	public String getSizeSliderJS(String divSlider, String divText, char unit, Double min, Double max, Double value, String typeFS) throws Exception {
		
		int step = 1;
		
		if (value<0D)
			value = 0D;
		if (min<0D)
			min = 0D;
		if (max<0D)
			max = 0D;

		StringBuilder _sb = new StringBuilder();
		
		StringBuilder _sbGlobal = new StringBuilder();
		_sbGlobal.append("var val"+divText+"= "+value.intValue()+";\n");
		pageGlobalJS+=_sbGlobal.toString();
		
		_sb.append("$( '#"+divSlider+"' ).slider({\n");
		_sb.append("value: "+value.intValue()+",\n");
		_sb.append("min: "+min.intValue()+",\n");
		_sb.append("max: "+max.intValue()+",\n");
		_sb.append("step: "+step+",\n");
		_sb.append("slide: function( event, ui ) {\n");
		_sb.append("$( '#"+divText+"' ).val( ui.value );\n");
		
		if (!FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, typeFS) && !typeFS.equals("unknown")) {
			 if (divSlider.equals("slider_data_reservation") || divSlider.equals("slider_snap_reservation")) {
				_sb.append("if (ui.value > val"+divText+") {\n");
				_sb.append(" decreaseMaxRefSliders(ui.value-val"+divText+", '"+divSlider+"' );\n");
				_sb.append(" val"+divText+"= ui.value;\n");
				_sb.append("} else if (ui.value < val"+divText+") {\n");
				_sb.append(" increaseMaxRefSliders(val"+divText+"-ui.value, '"+divSlider+"');\n");
				_sb.append(" val"+divText+"= ui.value;\n");
				_sb.append("}");
			} else if (divSlider.equals("slider_data_size") ) {
				_sb.append("if (ui.value > val"+divText+") {\n");
				_sb.append(" decreaseMaxSizeSliders(ui.value-val"+divText+", '"+divSlider+"' );\n");
				_sb.append(" val"+divText+"= ui.value;\n");
				_sb.append("} else if (ui.value < val"+divText+") {\n");
				_sb.append(" increaseMaxSizeSliders(val"+divText+"-ui.value, '"+divSlider+"');\n");
				_sb.append(" val"+divText+"= ui.value;\n");
				_sb.append("}\n");
			}
		}
		_sb.append("}\n});\n");
		

		return _sb.toString();
	}
	
	public static String getAdjustSliderFuncSimple(String nameFunc, String divSlider, String selectUnit, String divText, Double exMaxMega, Double exMaxGiga, Double exMaxTera) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function "+nameFunc+"() {\n");
		_sb.append("	var unit = $( '#"+selectUnit+"').val();\n");
		_sb.append("	var slider = $( '#"+divSlider+"' ).slider();\n");
		_sb.append(" 	val"+divText+"= 0.0;\n");
		_sb.append("	slider.slider({value:0.0});\n");
		_sb.append("	$( '#"+divText+"' ).val( 0.0 );\n");
		_sb.append("	if (unit == 'M') {\n");
		_sb.append("		slider.slider('option', 'max',"+exMaxMega+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+exMaxMega+"]] form_text');\n");
		_sb.append("	} else if (unit == 'G') {\n");
		_sb.append("		slider.slider('option', 'max',"+exMaxGiga+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+exMaxGiga+"]] form_text');\n");
		_sb.append("	} else if (unit == 'T') {\n");
		_sb.append("		slider.slider('option', 'max',"+exMaxTera+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+exMaxTera+"]] form_text');\n");
		_sb.append("	}\n");
		_sb.append("	 slider.slider({value : slider.slider('option','value')}); \n");
		_sb.append("}\n");
		return _sb.toString();
	}
	
	public static String getAdjustSliderFunc(String nameFunc, String divSlider, String selectUnit, String divText, String selectOperation, Double exMaxMega, Double exMaxGiga, Double exMaxTera, Double shMaxMega, Double shMaxGiga, Double shMaxTera) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("function "+nameFunc+"() {\n");
		_sb.append("	var unit = $( '#"+selectUnit+"').val();\n");
		_sb.append("	var operation = $( '#"+selectOperation+"').val();\n");
		_sb.append("	var slider = $( '#"+divSlider+"' ).slider();\n");
		_sb.append(" 	val"+divText+"= 0.0;\n");
		_sb.append("	slider.slider({value:0.0});\n");
		_sb.append("	if (operation == 'extend') {\n");
		_sb.append("	$( '#"+divText+"' ).val( 0.0 );\n");
		_sb.append("	if (unit == 'M') {\n");
		_sb.append("		slider.slider('option', 'max',"+exMaxMega+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+exMaxMega+"]] form_text');\n");
		_sb.append("	} else if (unit == 'G') {\n");
		_sb.append("		slider.slider('option', 'max',"+exMaxGiga+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+exMaxGiga+"]] form_text');\n");
		_sb.append("	} else if (unit == 'T') {\n");
		_sb.append("		slider.slider('option', 'max',"+exMaxTera+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+exMaxTera+"]] form_text');\n");
		_sb.append("	}\n");
		_sb.append("	} else {\n");
		_sb.append("	if (unit == 'M') {\n");
		_sb.append("		slider.slider('option', 'max',"+shMaxMega+");\n");
		_sb.append("		$( '#"+divText+"' ).val( 0.0 );\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+shMaxMega+"]] form_text');\n");
		_sb.append("	} else if (unit == 'G') {\n");
		_sb.append("		slider.slider('option', 'max',"+shMaxGiga+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+shMaxGiga+"]] form_text');\n");
		_sb.append("	} else if (unit == 'T') {\n");
		_sb.append("		slider.slider('option', 'max',"+shMaxTera+");\n");
		_sb.append("		$( '#"+divText+"' ).attr('class', 'validate[required,custom[integer],min[0],max["+shMaxTera+"]] form_text');\n");
		_sb.append("	}\n");
		_sb.append("	}\n");
		_sb.append("	slider.slider({value : slider.slider('option','value')}); \n");
		_sb.append("}\n");
		return _sb.toString();
	}
	
	public static Double getMaxForSlider(String val, char unit) throws Exception {
		DecimalFormat _df = new DecimalFormat("##");
		_df.setRoundingMode(RoundingMode.FLOOR);
		
		return Double.valueOf(_df.format(Double.valueOf(VolumeManager.getSizeOnUnit(val, unit))).replaceAll(",", "\\."));
	}
	
	public static Double getMinForSlider(String val, char unit) throws Exception {
		DecimalFormat _df = new DecimalFormat("##");
		_df.setRoundingMode(RoundingMode.CEILING);
		return Double.valueOf(_df.format(Double.valueOf(VolumeManager.getSizeOnUnit(val, unit))).replaceAll(",", "\\."));
	}
	
	public static String getJSDisableDeviceChekcs() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function uncheckBros(meId, uncheck1Id, uncheck2Id) {\n");
		_sb.append("	var me = $('#'+meId);\n");
		_sb.append("	var uncheck1Id = $('#'+uncheck1Id);\n");
		_sb.append("	var uncheck2Id = $('#'+uncheck2Id);\n");
		_sb.append("	if (me.is(':checked')) {\n");
		_sb.append("		uncheck1Id.attr('checked', false);\n");
		_sb.append("		uncheck2Id.attr('checked', false);\n");
		_sb.append("	}\n");
		_sb.append("}\n");
		return _sb.toString();
	}
	
	public static String getJSShowZFS() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("function showZFS() {\n");
		_sb.append("	var type = $('#vgtype');\n");
		_sb.append("	if (type.val() == 'zfs')\n");
		_sb.append("		$('.zfs').css('display','');\n");
		_sb.append("	else\n");
		_sb.append("		$('.zfs').css('display','none');\n");
		_sb.append("}\n");
		return _sb.toString();
	}
	
	public void getSlidersControlJs(Integer minDataSize, Integer minReservation, Integer minDataReservation, Integer dataSizeVal, Integer reservationVal, Integer dataReservationVal, Integer maxDataSize, Integer maxReservation, Integer maxDataReservation, boolean withQuota) throws Exception {
		pageGlobalJS+="var minDataSize = "+minDataSize+";\n";
		pageGlobalJS+="var minReservation = "+minReservation+";\n";
		pageGlobalJS+="var minDataReservation = "+minDataReservation+";\n";

		if (withQuota) {
			pageJS+="$('#slider_data_size').slider({\n";
			pageJS+="    value: "+dataSizeVal+",\n";
			pageJS+="    min: ~~minDataSize,\n";
			pageJS+="    max: "+maxDataSize+",\n";
			pageJS+="    step: 1,\n";
			pageJS+="    slide: function (event, ui) {\n";
			pageJS+="       $('#data_size').val(ui.value);\n";
			
			pageJS+="    }\n";
			pageJS+="});\n";
		}
		pageJS+="$('#slider_reservation').slider({\n";
		pageJS+="    value: "+reservationVal+",\n";
		pageJS+="    min: ~~minReservation,\n";
		pageJS+="    max: "+maxReservation+",\n";
		pageJS+="    step: 1,\n";
		pageJS+="    slide: function (event, ui) {\n";
		pageJS+="       $('#reservation').val(ui.value);\n";
		pageJS+="    }\n";
		pageJS+="});\n";
		
		pageJS+="$('#slider_data_reservation').slider({\n";
		pageJS+="    value: "+dataReservationVal+",\n";
		pageJS+="    min: ~~minDataReservation,\n";
		pageJS+="    max: "+maxDataReservation+",\n";
		pageJS+="    step: 1,\n";
		pageJS+="    slide: function (event, ui) {\n";
		pageJS+="       $('#data_reservation').val(ui.value);\n";
		pageJS+="    }\n";
		pageJS+="});\n";
		
		pageJS+="$('#size').change(function () {\n";
		pageJS+="    var increment = ~~$('#size').val();\n";
		pageJS+=" 	 if ($('#operation').val() == 'extend') {\n";
		pageJS+=" 	     var currentMax = 0;\n";
		pageJS+="	 	 $( '#current' ).val(~~$( '#current' ).val()+~~increment);\n";
		if (withQuota) {
			pageJS+="    currentMax = ~~$('#slider_data_size').slider('option', 'max');\n";
			pageJS+="    var newMax = currentMax + increment;\n";
			pageJS+="    if (newMax < minDataSize) newMax = minDataSize;\n";
			pageJS+="    $('#slider_data_size').slider('option', 'max', ~~newMax);\n";
			pageJS+="	 $( '#data_size' ).attr('class', 'validate[required,custom[integer],min[0],max['+~~newMax+']] form_text');\n";
	
		}
		pageJS+="    	currentMax = ~~$('#slider_reservation').slider('option', 'max');\n";
		pageJS+="    	newMax = currentMax + increment;\n";
		pageJS+="    	if (newMax < minReservation) newMax = minReservation;\n";
		pageJS+="    	$('#slider_reservation').slider('option', 'max', ~~newMax);\n";
		pageJS+="	 	$( '#reservation' ).attr('class', 'validate[required,custom[integer],min[0],max['+~~newMax+']] form_text');\n";
		pageJS+="    	$('#slider_reservation').slider({\n";
		pageJS+="        	value: $('#slider_reservation').slider('option', 'value')\n";
		pageJS+="    	});\n";
		
		pageJS+="    	currentMax = ~~$('#slider_data_reservation').slider('option', 'max');\n";
		pageJS+="    	newMax = currentMax + increment;\n";
		pageJS+="    	if (newMax < minDataReservation) newMax = minDataReservation;\n";
		pageJS+="    	$('#slider_data_reservation').slider('option', 'max', ~~newMax);\n";
		pageJS+="	 	$( '#data_reservation' ).attr('class', 'validate[required,custom[integer],min[0],max['+~~newMax+']] form_text');\n";
		pageJS+="    	$('#slider_data_reservation').slider({\n";
		pageJS+="        	value: $('#slider_data_reservation').slider('option', 'value')\n";
		pageJS+="    	});\n";
		pageJS+="    } else {\n";
		pageJS+=" 	     var currentMax = 0;\n";
		pageJS+="		 var newCurrent = ~~$( '#current' ).val()-~~increment;\n";
		pageJS+="	 	 if (newCurrent < 0)\n";
		pageJS+="	 	 	newCurrent = 0;\n";
		pageJS+="	 	 $( '#current' ).val(~~newCurrent);\n";
		if (withQuota) {
			pageJS+="    currentMax = ~~$('#slider_data_size').slider('option', 'max');\n";
			pageJS+="    var newMax = currentMax - increment;\n";
			pageJS+="    if (newMax < minDataSize) newMax = minDataSize;\n";
			pageJS+="    $('#slider_data_size').slider('option', 'max', ~~newMax);\n";
			pageJS+="	 $( '#data_size' ).attr('class', 'validate[required,custom[integer],min[0],max['+~~newMax+']] form_text');\n";
		}
		
		pageJS+="    currentMax = ~~$('#slider_reservation').slider('option', 'max');\n";
		pageJS+="    newMax = currentMax - increment;\n";
		pageJS+="    if (newMax < minReservation) newMax = minReservation;\n";
		pageJS+="    data = ~~$('#reservation').val();\n";
		pageJS+="    if (newMax < data) {\n";
		pageJS+="        $('#slider_reservation').slider({\n";
		pageJS+="            value: ~~newMax\n";
		pageJS+="        });\n";
		pageJS+="        $('#reservation').val(~~newMax);\n";
		pageJS+="    }\n";
		pageJS+="    $('#slider_reservation').slider('option', 'max', ~~newMax);\n";
		pageJS+="	 $( '#reservation' ).attr('class', 'validate[required,custom[integer],min[0],max['+~~newMax+']] form_text');\n";
		pageJS+="    $('#slider_reservation').slider({\n";
		pageJS+="        value: $('#slider_reservation').slider('option', 'value')\n";
		pageJS+="    });\n";

		pageJS+="    currentMax = ~~$('#slider_data_reservation').slider('option', 'max');\n";
		pageJS+="    newMax = currentMax - increment;\n";
		pageJS+="    if (newMax < minDataReservation) newMax = minDataReservation;\n";
		pageJS+="    data = ~~$('#data_reservation').val();\n";
		pageJS+="    if (newMax < data) {\n";
		pageJS+="        $('#slider_data_reservation').slider({\n";
		pageJS+="            value: ~~newMax\n";
		pageJS+="        });\n";
		pageJS+="        $('#data_reservation').val(~~newMax);\n";
		pageJS+="    }\n";
		pageJS+="    $('#slider_data_reservation').slider('option', 'max', ~~newMax);\n";
		pageJS+="	 $( '#data_reservation' ).attr('class', 'validate[required,custom[integer],min[0],max['+~~newMax+']] form_text');\n";
		pageJS+="    $('#slider_data_reservation').slider({\n";
		pageJS+="        value: $('#slider_data_reservation').slider('option', 'value')\n";
		pageJS+="    });\n";

		pageJS+="    } ;";
		pageJS+="});\n";

		pageJS+="$( '#data_size' ).change(function() {\n";
		pageJS+="	$( '#slider_data_size' ).slider( 'value', $(this).val() );\n";
		pageJS+="});\n";
		pageJS+="$( '#reservation' ).change(function() {\n";
		pageJS+="	$( '#slider_reservation' ).slider( 'value', $(this).val() );\n";
		pageJS+="});\n";
		pageJS+="$( '#data_reservation' ).change(function() {\n";
		pageJS+="	$( '#slider_data_reservation' ).slider( 'value', $(this).val() );\n";
		pageJS+="});\n";
	}
	
	private String getJqGridJS(String tableId, String divId) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("jQuery(\"#"+tableId+"\").jqGrid({\n");
 		sb.append("		datatype: \"local\",\n");
 		sb.append("		colNames:['"+getLanguageMessage("device.logical_volumes.name")+"','"+getLanguageMessage("device.logical_volumes.vg")+"','"+getLanguageMessage("device.logical_volumes.size")+"','"+getLanguageMessage("device.logical_volumes.used")+" (%)','"+getLanguageMessage("device.logical_volumes.storage_type")+"','"+getLanguageMessage("device.logical_volumes.storage_fstype")+"','--'],\n");
 		sb.append("		colModel:[ {name:'name',index:'name', width:40}, \n");
 		sb.append("					{name:'vg',index:'vg', width:30},\n");
 		sb.append("					{name:'size',index:'size', sortable:false, width:20},\n");
 		sb.append("					{name:'used',index:'used',sorttype:'int', firstsortorder: 'desc', width:15},\n");
 		sb.append("					{name:'storage_type',index:'storage_type', width:15},\n");
 		sb.append("					{name:'storage_fstype',index:'storage_fstype', width:15},\n");
 		sb.append("					{name:'actions',index:'actions', width:15, sortable:false, search:false}],\n");
 		sb.append("		width: $('#"+divId+"').width(),\n");
 		sb.append("		height: 'auto',\n");
 		sb.append("		rownumbers: false,\n");
 		sb.append("		multiselect: false,\n");
 		sb.append("		hidegrid:false,\n");
 		sb.append("		rowNum: 10,\n");
 		sb.append("		rowList : [5,10,25,50],\n");
 		sb.append("		gridComplete: LoadComplete,\n");
 		sb.append("		pager: '#pager',\n");
 		sb.append("		caption: '"+getLanguageMessage("device.logical_volumes")+"',\n");
 		sb.append("		emptyDataText: '"+getLanguageMessage("device.message.no_lvs")+"',\n");
 		sb.append("		onSelectRow: function(rowid, status) {\n");
 		sb.append("			$('#"+tableId+"').resetSelection();\n");
 		sb.append("		}\n");
 		sb.append("	});\n");
 		sb.append("jQuery('#"+tableId+"')\n");
 		sb.append("		.navGrid('#pager',{edit:false,add:false,del:false,search:false,refresh:false})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'',\n"); 
 		sb.append("			title:'"+getLanguageMessage("common.message.add")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-add',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.href='/admin/DeviceDisk?type="+NEW_LV+"';\n");
 		sb.append("			},\n"); 
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("		.navButtonAdd('#pager',{\n");
 		sb.append("			caption:'',\n"); 
 		sb.append("			title:'"+getLanguageMessage("common.message.refresh")+"',\n"); 
 		sb.append("			buttonicon:'jq-ui-icon-refresh',\n"); 
 		sb.append("			onClickButton: function(){\n"); 
 		sb.append("				window.location.reload();\n");
 		sb.append("			},\n");
 		sb.append("			position:'last'\n");
 		sb.append("		})\n");
 		sb.append("$('#pager').find('.jq-ui-icon-refresh').css({'background-image':'url(\"/images/arrow_refresh_16.png\")', 'background-position':'0'});\n");
 		sb.append("$('#pager').find('.jq-ui-icon-add').css({'background-image':'url(\"/images/add_16.png\")', 'background-position':'0'});\n");

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
 	
 	private String allLoad(String tableGridId, List<Map<String, String>> volumes) throws Exception{
 		StringBuilder sb = new StringBuilder();
 		sb.append("function reloadAll()\n");
 		sb.append("{\n");
		if (volumes != null && !volumes.isEmpty()) {
			sb.append("	var json = '"+getJSONVolumesJS(volumes).replace("'", "\\'")+"';\n");
			sb.append("	var alldata = jQuery.parseJSON(json);\n");
			sb.append("	if (alldata) {\n");
			sb.append("		for(var i=0;i<=alldata.length;i++) {jQuery(\"#"+tableGridId+"\").jqGrid('addRowData',i+1,alldata[i]);}\n");
			sb.append("		jQuery(\"#"+tableGridId+"\").sortGrid('used');");
			sb.append("	}\n");
		}
		sb.append("}\n");
		return sb.toString();
 	}
 	
 	private String getJSONVolumesJS(List<Map<String, String>> volumes) throws Exception{
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		sb.append("[");
 		if (volumes != null && !volumes.isEmpty()) {
	 		for (Map<String, String> _lv : volumes) {
					if (!first)
						sb.append(",");
					else
						first=false;
	        	
					String name = "<span";
					if (VolumeManager.isOnRemovingProcess(_lv.get("vg"), _lv.get("name"))) {
						name+=" style='background-color:#F2C291;' ";
                	}
					name+=" > ";
                	
					String used = "";
                    if (_lv.get("used") != null) {
                    	used = _lv.get("used");
                    	if (used.contains("%"))
                    		used = used.replace("%", "");
                    	used = used.trim();
                    }
                    
                    if(VolumeManager.isMount(_lv.get("vg"), _lv.get("name")) && (_lv.get("mount") == null || !"true".equals(_lv.get("mount")))) {
                    	name+="<img src='/images/exclamation_16.png' class='tooltip' title='"+getLanguageMessage("device.logical_volumes.not_mounted")+"'/>&nbsp;";
                    }
                    
                    if (used != null && !used.isEmpty()) {
                    	Double dUsed = Double.parseDouble(used);
                    	if (dUsed >= 90d) {
                    		name+="<img src='/images/full_volume_16.png' class='tooltip' title='"+getLanguageMessage("device.logical_volumes.quota_exceded")+"'/>&nbsp;";
                    	}
                    }
                    
                    if (_lv.get("name") != null)
                    	name += _lv.get("name");
                    name+=" </span>";
                    
                    String vg = "";
                    if (_lv.get("vg") != null)
                    	vg = _lv.get("vg");
                    String size = "";
                    if (_lv.get("size") != null)
                    	size = _lv.get("size");
                    
                    String storage_type = "";
                    if(VolumeManager.isMount(_lv.get("vg"), _lv.get("name"))) {
                    	storage_type="NAS";
                    } else {
                    	storage_type="SAN";
                    }
                    String storage_fstype = "";
                    if(_lv.get("fstype") != null && "xfs".equals(_lv.get("fstype"))) {
                    	storage_fstype = getLanguageMessage("device.logical_volumes.storage_fstype.performance");
                	} else if(_lv.get("fstype") != null && "zfs".equals(_lv.get("fstype"))) {
                		storage_fstype = getLanguageMessage("device.logical_volumes.storage_fstype.zfs");
                	} else if(_lv.get("fstype") != null && "btrfs".equals(_lv.get("fstype"))) {
                		storage_fstype = getLanguageMessage("device.logical_volumes.storage_fstype.btrfs");
                	}
	            
		        	StringBuilder actions = new StringBuilder();
		            
		        	if (!VolumeManager.isOnRemovingProcess(_lv.get("vg"), _lv.get("name"))) {
		        		 actions.append("<div style='padding-top:5px;'>");
		                    if(VolumeManager.isMount(_lv.get("vg"), _lv.get("name")) && 
		                    		(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, _lv.get("fstype")) ||
		                    				FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _lv.get("fstype")))) {
		                    	actions.append("<a href='/admin/DeviceQuota?name=");
		                    	actions.append(_lv.get("name"));
		                    	actions.append("&group=");
		                    	actions.append(_lv.get("vg"));
		                    	actions.append("'><img src='/images/drive_user_16.png' title='");
		                    	actions.append(getLanguageMessage("common.message.quota"));
		                    	actions.append("' alt='");
		                    	actions.append(getLanguageMessage("common.message.quota"));
		                    	actions.append("'/></a>&nbsp;&nbsp;");
		                    }
		                    
		                    actions.append("<a href='/admin/DeviceDisk?type=");
		                    actions.append(EDIT_LV);
		                    actions.append("&name=");
		                    actions.append(_lv.get("name"));
		                    actions.append("&group=");
		                    actions.append(_lv.get("vg"));
		                    actions.append("'><img src='/images/drive_edit_16.png' title='");
		                    actions.append(getLanguageMessage("common.message.edit"));
		                    actions.append("' alt='");
		                    actions.append(getLanguageMessage("common.message.edit"));
		                    actions.append("'/></a>&nbsp;&nbsp;");
		                    actions.append("<a href='/admin/DeviceDisk?type=");
		                    actions.append(REMOVE_LV);
		                    actions.append("&name=");
		                    actions.append(_lv.get("name"));
		                    actions.append("&group=");
		                    actions.append(_lv.get("vg"));
		                    actions.append("'><img src='/images/drive_delete_16.png' title='");
		                    actions.append(getLanguageMessage("common.message.delete"));
		                    actions.append("' alt='");
		                    actions.append(getLanguageMessage("common.message.delete"));
		                    actions.append("'/></a>");
	            }
		        actions.append("</div>");
		        
		    	sb.append("{\"name\":\""+name+"\", \"vg\":\""+vg+"\", \"size\":\""+size+"\", \"used\":\""+used+"\", \"storage_type\":\""+storage_type+"\", \"storage_fstype\":\""+storage_fstype+"\", \"actions\":\""+actions.toString()+"\"}");
	 		}
 		}
 		sb.append("]");
 		return sb.toString();
 	}
 	
 	public String getJSVolumeOcupationHorizontalBars(String divName, List<Map<String, String>> lvs) throws Exception {
 		StringBuilder _sb = new StringBuilder();
 		String categories = "";
 		String data = "";
 		if (lvs != null && !lvs.isEmpty()) {
 			boolean first = true;
 			for (Map<String, String> lv : lvs) {
 				Double totalUsed = 0d;
 				if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, lv.get("fstype"))) {
	 				totalUsed = Double.parseDouble(lv.get("total-used-raw"));
	 				Double totalReserved = Double.parseDouble(lv.get("reservation-raw"));
	 				if (totalReserved > totalUsed)
	 					totalUsed=totalReserved;
 				} else if (lv.get("size-raw") != null) {
 					totalUsed = Double.parseDouble(lv.get("size-raw"));
 				}
 				
 				//String label = lv.get("name")+": "+VolumeManager.getFormatSize(String.valueOf(totalUsed)+" B");
 				if (totalUsed > 0) {
	 				if (first) {
	 					categories = "'"+lv.get("name")+"'";
	 					data = totalUsed.toString();
	 					first = false;
	 				} else {
	 					categories += ",'"+lv.get("name")+"'";
	 					data += "," +totalUsed.toString();
	 				}
 				}
 			}
 			
	 		_sb.append("var chartVolUsedBars = new Highcharts.Chart({\n");
			_sb.append("	chart: {\n");
			_sb.append("		renderTo: '"+divName+"',\n");
			_sb.append("        type: 'bar',\n");
			_sb.append("        height: "+(lvs.size()*40+100)+"\n");
			_sb.append("    },\n");
			_sb.append("    title: {text:''},\n");
			_sb.append("    xAxis: {\n");
			_sb.append("        categories: [");
			_sb.append(categories);
			_sb.append("		],\n");
			_sb.append("        title: {\n");
			_sb.append("            text: null\n");
			_sb.append("        }\n");
			_sb.append("    },\n");
			_sb.append("    yAxis: {\n");
			//_sb.append("        height: "+lvs.size()*50+",\n");
			_sb.append("        min: 0,\n");
			_sb.append("        title: {\n");
			_sb.append("            text: '"+getLanguageMessage("common.space")+"',\n");
			_sb.append("            align: 'high'\n");
			_sb.append("        },\n");
			_sb.append("        labels: {\n");
			_sb.append("            overflow: 'justify',\n");
			_sb.append("        }\n");
			_sb.append("    },\n");
			_sb.append("    tooltip: {\n");
			_sb.append("            	formatter: function() {\n");
			_sb.append("            		result = parseFloat(this.y);\n");
			_sb.append("            		if (this.y > (1024*1024*1024*1024)) { result = (this.y / (1024*1024*1024*1024)).toFixed(2) + 'T' }\n");
			_sb.append("            		else if (this.y > 1073741824) { result = (this.y / 1073741824).toFixed(2) + 'G' }\n");
			_sb.append("            		else if (this.y > 1048576) { result = (this.y / 1048576).toFixed(2) + 'M' }\n");
			_sb.append("            		else if (this.y > 1024) { result = (this.y / 1024).toFixed(2) + 'K' }\n");
			_sb.append("            		return result;\n");
			_sb.append("            	}\n");
			_sb.append("    },\n");
			_sb.append("    plotOptions: {\n");
			_sb.append("        bar: {\n");
			_sb.append("            dataLabels: {\n");
			_sb.append("                enabled: true,\n");
			_sb.append("            	formatter: function() {\n");
			_sb.append("            		result = parseFloat(this.y);\n");
			_sb.append("            		if (this.y > (1024*1024*1024*1024)) { result = (this.y / (1024*1024*1024*1024)).toFixed(2) + 'T' }\n");
			_sb.append("            		else if (this.y > 1073741824) { result = (this.y / 1073741824).toFixed(2) + 'G' }\n");
			_sb.append("            		else if (this.y > 1048576) { result = (this.y / 1048576).toFixed(2) + 'M' }\n");
			_sb.append("            		else if (this.y > 1024) { result = (this.y / 1024).toFixed(2) + 'K' }\n");
			_sb.append("            		return result;\n");
			_sb.append("            	}\n");
			_sb.append("            }\n");
			_sb.append("        }\n");
			_sb.append("    },\n");
			_sb.append("    credits: {\n");
			_sb.append("        enabled: false\n");
			_sb.append("    },\n");
			_sb.append("    series: [{\n");
			_sb.append("        name: '"+getLanguageMessage("device.volume_groups.ocuped.fs")+"',\n");
			_sb.append("        data: ["+data+"]\n");
			//_sb.append("        color: '#FF0000'\n");
			_sb.append("    }]\n");
			_sb.append("});\n");
 		}
 		return _sb.toString();
 	}
	
}
