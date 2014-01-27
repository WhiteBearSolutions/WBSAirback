package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.http.MultiPartRequest;
import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DrbdCmanConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Configuration;

public class CommResponse {
	public static final int TYPE_HA = 87536936;
	public static final int TYPE_POOL_MANAGER = 832467836;
	public static final int COMMAND_REQUEST = 482768542;
	public static final int COMMAND_REQUEST_CONFIRM_STAGE1 = 482769827;
	public static final int COMMAND_REQUEST_CONFIRM_STAGE2 = 482769828;
	public static final int COMMAND_REQUEST_REJECT = 482768735;
	public static final int COMMAND_BREAK = 642768542;
	public static final int COMMAND_BREAK_CONFIRM = 642769827;
	public static final int COMMAND_BREAK_REJECT = 642768735;
	public static final int COMMAND_SEND_FSTAB = 546789013;
	
	public static final int COMMAND_ADD_VOLUME = 156887406;
	public static final int COMMAND_DEL_VOLUME = 623768542;
	public static final int COMMAND_EXTEND_VOLUME = 303012058;
	public static final int COMMAND_LOGIN_EXTERNAL_TARGET = 632032014;
	public static final int COMMAND_LOGOUT_EXTERNAL_TARGET = 63879544;
	
	public static final int COMMAND_REMOVE_POOL_VOL = 352367012;
	
	private Configuration _c;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ServletOutputStream response_output;
	private MultiPartRequest multipart_request;
	private int type;
	private int command;
	
	public CommResponse(HttpServletRequest request, HttpServletResponse response) throws Exception {
		this._c = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		this.request = request;
		this.response = response;
		this.response_output = response.getOutputStream();
		if(this.request.getContentType() != null && this.request.getContentType().contains("multipart/form-data")) {
        	this.multipart_request = new MultiPartRequest(this.request);
        	if(this.multipart_request.getParameter("type") != null && !this.multipart_request.getParameter("type").isEmpty()) {
            	try {
                    this.type = Integer.parseInt(this.multipart_request.getParameter("type"));
                } catch(NumberFormatException _ex) {}
            }
            if(this.multipart_request.getParameter("command") != null && !this.multipart_request.getParameter("command").isEmpty()) {
            	try {
                    this.command = Integer.parseInt(this.multipart_request.getParameter("command"));
                } catch(NumberFormatException _ex) {}
            }
        } else {
        	if(this.request.getParameter("type") != null && !this.request.getParameter("type").isEmpty()) {
        		try {
                    this.type = Integer.parseInt(this.request.getParameter("type"));
                } catch(NumberFormatException _ex) {}
        	}
        	if(this.request.getParameter("command") != null && !this.request.getParameter("command").isEmpty()) {
        		try {
                    this.command = Integer.parseInt(this.request.getParameter("command"));
                } catch(NumberFormatException _ex) {}
        	}
        }
	}
	
	public void process() throws IOException {
		switch(this.type) {
			default:
				this.response_output.println("unknown communication request type");
				break;
			case TYPE_HA:
				this.response.setContentType("text/plain");
				try {
					this.response_output.println(processHA());
				} catch(Exception _ex) {
					this.response_output.println(_ex.getMessage());
				}
				break;
			case TYPE_POOL_MANAGER:
				try {
					this.response_output.println(processPoolManager());
				} catch(Exception _ex) {
					this.response_output.println(_ex.getMessage());
				}
				break;
		}
		this.response_output.flush();
	}
	
	private String processPoolManager() throws Exception {
		switch(this.command) {
		default:
			return "unknown communication request command";
		case COMMAND_REMOVE_POOL_VOL:
			PoolManager.removePoolVolumeFromRemote(this.request.getParameter("device"), this.request.getParameter("label"));
			return "done";
		}
	}
	
	@SuppressWarnings("unchecked")
	private String processHA() throws Exception {
		HACommServer _hc = new HACommServer(this._c);
		switch(this.command) {
			default:
				return "unknown communication request command";
			case COMMAND_REQUEST:
				if(this.multipart_request == null) {
					return "request is not multipart type";
				}
				
				if(!this.multipart_request.hasFile("ha-request.xml")) {
					return "request does not contain the correct data format";
				}
				
				return _hc.request(this.multipart_request.getFile("ha-request.xml"));
			case COMMAND_REQUEST_CONFIRM_STAGE1: {
					int _fenceType = DrbdCmanConfiguration.FENCE_NONE;
					Map<String, String> _fenceAttributes = new HashMap<String, String>();
					if(this.request.getParameter("fence.type") == null) {
						throw new Exception("failed to determine fencing type on master");
					}
					try {
						_fenceType = Integer.parseInt(this.request.getParameter("fence.type"));
					} catch(NumberFormatException _ex) {}
					for(Object _parameter : Collections.list(this.request.getParameterNames())) {
						if(String.valueOf(_parameter).startsWith("fence.") && !String.valueOf(_parameter).equalsIgnoreCase("fence.type")) {
							_fenceAttributes.put(String.valueOf(_parameter).substring(6), this.request.getParameter(String.valueOf(_parameter)));
						}
					}
					return _hc.requestConfirmStage1(this.request.getParameter("virtual"), this.request.getRemoteAddr(), _fenceType, _fenceAttributes, this.request.getParameter("uuid"));
				}
			case COMMAND_REQUEST_CONFIRM_STAGE2:
				return _hc.requestConfirmStage2(this.request.getParameter("virtual"), this.request.getRemoteAddr());
			case COMMAND_REQUEST_REJECT:
				return _hc.requestReject(this.request.getRemoteAddr());
			case COMMAND_BREAK:
				if(this.multipart_request.hasFile("ha-break.xml")) {
					return _hc.breakRequest(this.request.getRemoteAddr(), this.multipart_request.getFile("ha-break.xml"));
				}
				return "request does not contain the correct data format";
			case COMMAND_BREAK_CONFIRM:
				return _hc.breakRequestConfirm(this.request.getRemoteAddr());
			case COMMAND_BREAK_REJECT:
				return _hc.breakRequestReject(this.request.getRemoteAddr());
			case COMMAND_SEND_FSTAB:
				return _hc.receiveFsTab(this.request.getRemoteAddr(), this.multipart_request.getParameter("uuid"), this.multipart_request.getFile("fstab"));
			case COMMAND_ADD_VOLUME:
				return _hc.addVolume(this.request.getRemoteAddr(), this.request.getParameter("uuid"), this.request.getParameter("storage_type"), this.request.getParameter("lv_type"), this.request.getParameter("fs_type"), this.request.getParameter("group"), this.request.getParameter("name"), this.request.getParameter("size"), this.request.getParameter("size_units"), this.request.getParameter("compression"), this.request.getParameter("encryption"), this.request.getParameter("deduplication"), this.request.getParameter("percent_snap"));
			case COMMAND_DEL_VOLUME:
				return _hc.delVolume(this.request.getRemoteAddr(), this.request.getParameter("uuid"), this.request.getParameter("group"), this.request.getParameter("name"));
			case COMMAND_EXTEND_VOLUME:
				return _hc.expandVolume(this.request.getRemoteAddr(), this.request.getParameter("uuid"), this.request.getParameter("typeVol"), this.request.getParameter("group"), this.request.getParameter("name"), this.request.getParameter("size"), this.request.getParameter("data_size"), this.request.getParameter("total_reservation"), this.request.getParameter("data_reservation"), this.request.getParameter("size_units"), this.request.getParameter("snapshot_hourly_status"), this.request.getParameter("snapshot_hourly_retention"), this.request.getParameter("snapshot_daily_status"), this.request.getParameter("snapshot_daily_retention") , this.request.getParameter("snapshot_daily_hour"), this.request.getParameter("snapshot_manual_remove"));
			case COMMAND_LOGIN_EXTERNAL_TARGET:
				return _hc.loginExternalVolume(this.request.getRemoteAddr(), this.request.getParameter("uuid"), this.request.getParameter("address"), this.request.getParameter("target"), this.request.getParameter("method"), this.request.getParameter("user"), this.request.getParameter("password"));
			case COMMAND_LOGOUT_EXTERNAL_TARGET:
				return _hc.logoutExternalVolume(this.request.getRemoteAddr(), this.request.getParameter("uuid"), this.request.getParameter("address"), this.request.getParameter("iqn"));
		}
	}
}
