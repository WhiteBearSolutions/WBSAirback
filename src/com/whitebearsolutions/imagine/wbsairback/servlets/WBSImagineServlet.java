package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.RoleManager;
import com.whitebearsolutions.imagine.wbsairback.SecurityManager;
import com.whitebearsolutions.imagine.wbsairback.SessionManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DrbdCmanConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.PermissionsConfiguration;
import com.whitebearsolutions.imagine.wbsairback.i18n.WBSAirbackResourceBundle;

public abstract class WBSImagineServlet extends HttpServlet {
	static final long serialVersionUID = 20080902L;
	private HttpServletRequest request;
	private HttpServletResponse response;
	protected SessionManager sessionManager;
	protected SecurityManager securityManager;
	protected PermissionsConfiguration permissions;
	protected String pageJSFuncs;
	protected String pageJS;
	protected String pageGlobalJS;
	protected ResourceBundle messagei18N;
	protected static Map<String, String> roleMap;
	protected static Map<String, String> roleDescriptions;
	private String section;
	protected boolean headerWrote = false;
	protected boolean redirected = false;
	
	public static List<String> blackListBackUrls;
	
	static {
		blackListBackUrls = new ArrayList<String>();
		blackListBackUrls.add("BackupJobs?type="+BackupJobs.REMOVEJOB);
		blackListBackUrls.add("BackupStorageDisk?type="+BackupStorageDisk.DISK_DELETE);
	}
	
	public static boolean isOnBlackList(String url) {
		try {
			for (String black : blackListBackUrls)
				if (url.contains(black))
					return true;
			return false;
		} catch (Exception ex) {
			return false;
		}
	}
	
	public String getLanguageMessage(String message) {
		try {
			return this.messagei18N.getString(message);
		} catch(MissingResourceException _ex) {
			return message; 
		}
	} 
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.request = request;
		this.response = response;
		this.headerWrote = false;
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		this.permissions = new PermissionsConfiguration();
		this.sessionManager = new SessionManager(request);
		try {
			if(!this.sessionManager.isConfigured() && !this.request.getServletPath().equals("/admin/Wizard")) {
				response.sendRedirect("/admin/Wizard");
			}
			this.securityManager = this.sessionManager.getSecurityManager();
			if(this.sessionManager.isConfigured() && request.getParameter("language") != null) {
				try {
					Locale _l = new Locale(request.getParameter("language"));
					this.messagei18N = ResourceBundle.getBundle("wbsairback", _l, new WBSAirbackResourceBundle());
					this.sessionManager.loadObjectSession("language", this.messagei18N);
				} catch(Exception _ex) {
					System.out.println("WBSImagineServlet::Language: " + _ex.toString());
				}
			} else {
				if(this.sessionManager.hasObjectSession("language")) {
					this.messagei18N = (ResourceBundle) this.sessionManager.getObjectSession("language");
				} else {
					this.messagei18N = ResourceBundle.getBundle("wbsairback", this.request.getLocale(), new WBSAirbackResourceBundle());
					this.sessionManager.loadObjectSession("language", this.messagei18N);
				}
			}
			if(!this.sessionManager.hasObjectSession("version")) {
				this.sessionManager.loadObjectSession("version", GeneralSystemConfiguration.getVersion());
			}
		} catch(Exception _ex) {
			System.out.println(_ex.toString());
		}
		pageJS="";
		pageJSFuncs="";
		pageGlobalJS="";
		fillRoles();
		
		if(this.securityManager.isLogged() && !this.request.getServletPath().equals("/admin/Wizard") && !this.request.getServletPath().equals("/admin/Comm")) {
			Integer type = null;
			if (request.getParameter("type") != null) {
				try {
					type = Integer.parseInt(request.getParameter("type"));
				} catch (Exception ex){}
			}
			
			if (!this.securityManager.isUserAllowedTo(this.request.getServletPath(), type, permissions)) {
				errorPage(response, getLanguageMessage("common.message.no_privilegios"));
				return;
			} else if (this.securityManager.isBackupFromCommunity(this.request.getServletPath(), permissions)) {
				errorPage(response, getLanguageMessage("common.message.community.backup.section.unavaliable"));
				return;
			}
		}
		handle(request, response);
	}
	
	public void errorPage(HttpServletResponse response, String message) {
		response.setContentType("text/html");
		try {
			writeDocumentHeader();
			writeDocumentError(message);
			writeDocumentFooter();
		} catch (Exception ex) {}
	}
	
	public abstract void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	
	protected void writeDocumentHeader() throws Exception {
		String _status = DrbdCmanConfiguration.getDRBDSynchronization();
		PrintWriter _xhtml_out = response.getWriter();
	    
		_xhtml_out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	    _xhtml_out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
	    _xhtml_out.println("<html><head>");
	    _xhtml_out.println("<meta name=\"generator\" content=\"WBSAirback\"/>");
	    _xhtml_out.println("<meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\"/>");
	    _xhtml_out.println("<link media=\"screen\" type=\"text/css\" href=\"/css/style.css\" rel=\"stylesheet\" />");
	    _xhtml_out.println("<link media=\"screen\" type=\"text/css\" href=\"/css/wbs-icons.css\" rel=\"stylesheet\" />");
	    _xhtml_out.println("<link media=\"screen\" type=\"text/css\" href=\"/css/flexigrid.css\" rel=\"stylesheet\" />");
	    _xhtml_out.println("<link media=\"screen\" type=\"text/css\" href=\"/css/jquery.alerts.css\" rel=\"stylesheet\" />");
	    _xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/wbs.js\">");
	    _xhtml_out.println("<!--");
	    _xhtml_out.println("//-->");
	    _xhtml_out.println("</script>");
	    _xhtml_out.println("</head>");
	    _xhtml_out.println("<body onunload=\"showLoadingPage();\"  onload=\"loadImages();\" onscroll=\"clavar('hidepage', 100);\">");
    	_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.min.js\"></script>");
		_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery-ui.min.js\"></script>");
		_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.layout.min.js\"></script>");
		_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.jstree.js\"></script>");
		_xhtml_out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery-ui.css\" />");	
	    _xhtml_out.println("<div id=\"hidepage\" class=\"hidepage\" >");
	    _xhtml_out.println("<table");
	    _xhtml_out.println(" >");
	    _xhtml_out.println("<tr>");
	    _xhtml_out.println("<td valign=\"center\"><img width=\"60\"  height=\"60\"");
	    _xhtml_out.println(" src=\"/images/loading.gif\" /></td>");
	    _xhtml_out.println("</tr>");
	    _xhtml_out.println("<tr>");
	    _xhtml_out.println("<td><h1>"+getLanguageMessage("common.loading.waiting")+"</h1></td>");
	    _xhtml_out.println("</tr>");
	    _xhtml_out.println("</table>");
	    _xhtml_out.println("</div>");
	    
	    _xhtml_out.println("<div class=\"ui-layout-north\" >");
	    _xhtml_out.println("<div id=\"product_header\" class=\"product_header\">");
	    
	    this.section = this.request.getServletPath().substring(this.request.getServletPath().indexOf("/admin/")+"/admin/".length());
	    if (this.section.equals("CDPUsers") && this.request.getParameter("type") != null && this.request.getParameter("type").equals("7")) {
	    	this.section="CDPUserstype7";
	    }
	    
	    List<String> roles = null;
	    if(this.securityManager.isLogged() && !this.request.getServletPath().equals("/admin/Wizard")) {
	    	_xhtml_out.println("<div id=\"product_user\" class=\"product_user\">");
		    _xhtml_out.println("<label>");
		    _xhtml_out.print(getLanguageMessage("common.login.user"));
	    	_xhtml_out.print(": </label>");
		    _xhtml_out.println(this.securityManager.getLogin());
		    roles = this.securityManager.getUserRoles();
		    if (roles != null && !roles.isEmpty()) {
		    	_xhtml_out.print(" (");
		    	boolean init=true;
		    	for (String role : roles) {
		    		if (init) {
		    			_xhtml_out.print("<span class=\"tooltip\" title=\""+getLanguageMessage(roleDescriptions.get(role))+"\">"+roleMap.get(role)+"</span>");
		    			init = false;
		    		} else
		    			_xhtml_out.print(", <span class=\"tooltip\" title=\""+getLanguageMessage(roleDescriptions.get(role))+"\">"+roleMap.get(role)+"</span>");
		    	}
		    	_xhtml_out.print(")");
		    }
		    
		    _xhtml_out.println("</div>");
		    if(!_status.isEmpty()) {
		    	_xhtml_out.println("<div id=\"product_synchronization\" class=\"product_synchronization\">");
			    _xhtml_out.print("<label>");
		    	_xhtml_out.print(getLanguageMessage("common.message.synchronization"));
		    	_xhtml_out.print(": </label>");
		    	_xhtml_out.print(_status);
		    	_xhtml_out.println("</div>");
		    }
		    _xhtml_out.println("<div id=\"product_version\" class=\"product_version\">");
		    _xhtml_out.print("<label>");
		    _xhtml_out.print(getLanguageMessage("update.connection_version"));
	    	_xhtml_out.print(": </label>");
	    	String version = String.valueOf(this.sessionManager.getObjectSession("version"));
	    	if (version != null && !version.isEmpty() && version.contains("-rc"))
	    		version = version.substring(0, version.indexOf("-rc"));
		    _xhtml_out.print(version);
		    _xhtml_out.println("</div>");
		    
		    _xhtml_out.println("<div id=\"product_language\" class=\"product_language\">");
		    _xhtml_out.print("<form action=\"");
		    _xhtml_out.print(this.request.getServletPath());
		    _xhtml_out.println("\" name=\"lang_form\" method=\"post\">");
	        @SuppressWarnings("unchecked")
			Set<String> _parameters= this.request.getParameterMap().keySet();
	           for (String _parameter: _parameters){
	        	  if (!"language".equals(_parameter)) 
	        	   if (!"language".equals(_parameter))
	        		   _xhtml_out.println("<input name=\""+_parameter+"\" type=\"hidden\" value=\""+this.request.getParameter(_parameter)+"\"/>");
	           } 
	        _xhtml_out.print("<label>"+getLanguageMessage("common.language")+": </label>");
		    _xhtml_out.print("<select class=\"form_select\" name=\"language\" onChange=\"changeLanguage();\" style=\"margin:0px;margin-left:4px;\">");
		    _xhtml_out.println("<option value=\"en\">English </option>");
		    _xhtml_out.print("<option value=\"es\"");
		    if("es".equals(this.messagei18N.getLocale().getLanguage())) {
		    	_xhtml_out.print(" selected=\"selected\"");
		    }
		    _xhtml_out.println(">Spanish </option>");
		    _xhtml_out.println("</select>");

		    _xhtml_out.println("</form>");
		    _xhtml_out.println("</div>");
	    }
	    _xhtml_out.println("<div id=\"product_subheader\" class=\"product_subheader\">");
	    if(this.securityManager.isLogged()) {
	    	_xhtml_out.println("<div class=\"subheader_button\">");
		    _xhtml_out.print("<a class=\"product_logout\" title=\""+getLanguageMessage("common.logout")+"\" href=\"javascript:sendForm('/admin/Login?type=logout');\">");
	    	_xhtml_out.print("<img src=\"/images/door_out_32.png\"/>");
	    	_xhtml_out.print(getLanguageMessage("common.logout"));
	    	_xhtml_out.println("</a>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("<div class=\"subheader_button\">");
	    	_xhtml_out.print("<a class=\"product_help\" title=\""+getLanguageMessage("common.help")+"\" target=\"_blank\" href=\"ftp://ftp.whitebearsolutions.com/public/Manual_Administrador_WBSAirback_V12_12.4.pdf\">");
	    	_xhtml_out.print("<img src=\"/images/help_32.png\"/>");
	    	_xhtml_out.print(getLanguageMessage("common.help"));
	    	_xhtml_out.println("</a>");
	    	_xhtml_out.println("</div>");
	    }
    	_xhtml_out.println("</div>");
	    _xhtml_out.println("<div class=\"clear\"/></div>");
    	_xhtml_out.println("</div>");
    	_xhtml_out.println("</div>");
    	
    	/*
    	 * Menu
    	 */
    	if(this.securityManager.isLogged() && !this.request.getServletPath().equals("/admin/Wizard") && this.section != null && !this.section.equals("Login") && !this.section.equals("") && !this.section.contains(".html")) {
    		_xhtml_out.println("<div class=\"ui-layout-west\">");
    		_xhtml_out.println("<div id=\"menu\">");
	    	_xhtml_out.println("<ul id=\"menu_tabs\" class=\"menu_tabs\">");
	    	String page = "";
	    	if(HAConfiguration.isActiveNode()) {
	    		if (permissions.hasSection(roles, "System")) { 
			    	_xhtml_out.print("<li rel=\"System\" id=\"System\">");
			    	page = com.whitebearsolutions.imagine.wbsairback.servlets.SystemConfiguration.baseUrl;
			    	_xhtml_out.print("<a href=\""+page+"\">");
			    	_xhtml_out.print(getLanguageMessage("common.menu.system"));
			    	_xhtml_out.println("</a>");
		    		_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"SystemConfiguration\" id=\"SystemConfiguration\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.system.general"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = SystemNetwork.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"SystemNetwork\" id=\"SystemNetwork\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.system.network"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = SystemProxy.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"SystemProxy\" id=\"SystemProxy\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.system.proxy"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = SystemClock.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"SystemClock\" id=\"SystemClock\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.system.clock"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = SystemUsers.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"SystemUsers\" id=\"SystemUsers\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.system.users"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = SystemMaintenance.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"SystemMaintenance\" id=\"SystemMaintenance\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.system.manteinance"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = WebServices.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"WebServices\" id=\"WebServices\">");
			    		_xhtml_out.println("<a target=\"_blank\" href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.system.webservices"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		_xhtml_out.println("</ul>\n");
		    		_xhtml_out.println("</li>");
		    	}
	    	}
	    	if (permissions.hasSection(roles, "Storage")) {
	    		page = DeviceDisk.baseUrl;
		    	_xhtml_out.print("<li rel=\"Storage\" id=\"Storage\"><a href=\""+page+"\">");
		    	_xhtml_out.print(getLanguageMessage("common.menu.storage"));
		    	_xhtml_out.println("</a>");
	    		_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
	    		if (permissions.isAllowed(roles, page, null)) {
		    		_xhtml_out.println("<li rel=\"DeviceDisk\" id=\"DeviceDisk\">");
		    		_xhtml_out.println("<a href=\""+page+"\">");
		    		_xhtml_out.println(getLanguageMessage("common.menu.device.disk"));
		    		_xhtml_out.println("</a>");
		    		_xhtml_out.println("</li>\n");
	    		}
	    		if (HAConfiguration.isActiveNode()) {
	    			page = DeviceNAS.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"DeviceNAS\" id=\"DeviceNAS\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.device.shares"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
	    		}
		    	page = DeviceReplication.baseUrl;
		    	if (permissions.isAllowed(roles, page, null)) {
		    		_xhtml_out.println("<li rel=\"DeviceReplication\" id=\"DeviceReplication\">");
		    		_xhtml_out.println("<a href=\""+page+"\">");
		    		_xhtml_out.println(getLanguageMessage("common.menu.device.replica"));
		    		_xhtml_out.println("</a>");
		    		_xhtml_out.println("</li>\n");
		    	}
	    		if (HAConfiguration.isActiveNode()) {
		    		/*_xhtml_out.println("<li rel=\"VTLConfiguration\" id=\"VTLConfiguration\">");
		    		_xhtml_out.println("<a href=\"/admin/VTLConfiguration\">");
		    		_xhtml_out.println(getLanguageMessage("common.menu.device.vtlConfiguration"));
		    		_xhtml_out.println("</a>");
		    		_xhtml_out.println("</li>\n");*/
	    			page = DeviceISCSI.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"DeviceISCSI\" id=\"DeviceISCSI\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.device.iscsi"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = CloudAccounts.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"CloudAccounts\" id=\"CloudAccounts\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.device.cloud.accounts"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
	    		}
	    		_xhtml_out.println("</ul>\n");
	    		_xhtml_out.println("</li>");
	    	}
	    	
	    	
	    	if(HAConfiguration.isActiveNode()) {
	    		if (permissions.hasSection(roles, "Backup")) {
	    			page = BackupSummary.baseUrl;
			    	_xhtml_out.print("<li rel=\"Backup\" id=\"Backup\"><a href=\""+page+"\">");
			    	_xhtml_out.print(getLanguageMessage("common.menu.backup"));
			    	_xhtml_out.println("</a>");
			    	
			    	// TODO: uncoment with bacula version
		    		/*_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"BackupSummary\" id=\"BackupSummary\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
		    			_xhtml_out.println(getLanguageMessage("common.menu.backup.summary"));
		    			_xhtml_out.println("</a>");
		    			_xhtml_out.println("</li>\n");
		    		}
		    		page = BackupHypervisors.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"BackupHypervisors\" id=\"BackupHypervisors\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.backup.hypervisors"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		page = BackupClients.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"BackupClients\" id=\"BackupClients\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
		    			_xhtml_out.println(getLanguageMessage("common.menu.backup.clients"));
		    			_xhtml_out.println("</a>");
		    			_xhtml_out.println("</li>\n");
		    		}
		    		page = BackupJobsExtended.baseUrl;
		    		if(permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"BackupJobsExtended\" id=\"BackupJobsExtended\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
		    			_xhtml_out.println(getLanguageMessage("common.menu.backup.extended.jobs"));
		    			_xhtml_out.println("</a>");
		    			_xhtml_out.println("</li>\n");
		    			
		    		}
		    		page = BackupCategories.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"BackupCategories\" id=\"BackupCategories\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
		    			_xhtml_out.println(getLanguageMessage("common.menu.backup.categories"));
		    			_xhtml_out.println("</a>");
		    			_xhtml_out.println("</li>\n");
		    		}
		    		page = BackupScheduleWeekly.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
			    		_xhtml_out.println("<li rel=\"BackupScheduleWeekly\" id=\"BackupScheduleWeekly\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.backup.schedules"));
			    		_xhtml_out.println("</a>");
			    		_xhtml_out.println("</li>\n");
		    		}
		    		if (permissions.hasSection(roles, "Filesets")) {
		    			page = BackupFilesetsClients.baseUrl;
		    			_xhtml_out.println("<li rel=\"BackupFilesets\" id=\"BackupFilesets\">");
			    		_xhtml_out.println("<a href=\""+page+"\">");
			    		_xhtml_out.println(getLanguageMessage("common.menu.backup.fileset"));
			    		_xhtml_out.println("</a>");
		    			_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
			    		if (permissions.isAllowed(roles, page, null)) {
			    			_xhtml_out.println("<li rel=\"BackupFilesetsClients\" id=\"BackupFilesetsClients\">");
			    			_xhtml_out.println("<a href=\""+page+"\">");
			    			_xhtml_out.println(getLanguageMessage("common.menu.backup.fileset.clients"));
			    			_xhtml_out.println("</a>");
			    			_xhtml_out.println("</li>\n");
			    		}
			    		page = BackupFilesetsLocal.baseUrl;
			    		if (permissions.isAllowed(roles, page, null)) {
				    		_xhtml_out.println("<li rel=\"BackupFilesetsLocal\" id=\"BackupFilesetsLocal\">");
				    		_xhtml_out.println("<a href=\"/admin/BackupFilesetsLocal\">");
				    		_xhtml_out.println(getLanguageMessage("common.menu.backup.fileset.local"));
				    		_xhtml_out.println("</a>");
				    		_xhtml_out.println("</li>\n");
			    		}
			    		page = BackupFilesetsNDMP.baseUrl;
			    		if (permissions.isAllowed(roles, page, null)) {
				    		_xhtml_out.println("<li rel=\"BackupFilesetsNDMP\" id=\"BackupFilesetsNDMP\">");
				    		_xhtml_out.println("<a href=\""+page+"\">");
				    		_xhtml_out.println(getLanguageMessage("common.menu.backup.fileset.ndmp"));
				    		_xhtml_out.println("</a>");
				    		_xhtml_out.println("</li>\n");
			    		}
		    			_xhtml_out.println("</ul>\n");
			    		_xhtml_out.println("</li>\n");
	    			}
		    		page = BackupPools.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"BackupPools\" id=\"BackupPools\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
		    			_xhtml_out.println(getLanguageMessage("common.menu.backup.pool"));
		    			_xhtml_out.println("</a>");
		    			_xhtml_out.println("</li>\n");
		    		}
		    		page = BackupStorageDisk.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"BackupStorageDisk\" id=\"BackupStorageDisk\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
		    			_xhtml_out.println(getLanguageMessage("common.menu.backup.storage"));
		    			_xhtml_out.println("</a>");
		    			_xhtml_out.println("</li>\n");
		    		}
		    		page = AdvancedGroupJob.baseUrl;
		    		if (permissions.isAllowed(roles, page, null)) {
		    			_xhtml_out.println("<li rel=\"AdvancedGroupJob\" id=\"AdvancedGroupJob\">");
		    			_xhtml_out.println("<a href=\""+page+"\">");
		    			_xhtml_out.println(getLanguageMessage("common.menu.advanced.groupjob"));
		    			_xhtml_out.println("</a>");
		    			_xhtml_out.println("</li>\n");
		    		}
		    		if (permissions.hasSection(roles, "Advanced")) {
		    			page = AdvancedBackup.baseUrl;
				    	_xhtml_out.print("<li rel=\"Advanced\" id=\"Advanced\"><a href=\""+page+"\">");
				    	_xhtml_out.print(getLanguageMessage("common.menu.advanced"));
				    	_xhtml_out.println("</a>");
				    	_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
				    	if (permissions.isAllowed(roles, page, null)) {
				    		_xhtml_out.println("<li rel=\"AdvancedBackup\" id=\"AdvancedBackup\">");
				    		_xhtml_out.println("<a href=\""+page+"\">");
				    		_xhtml_out.println(getLanguageMessage("common.menu.advanced.backup"));
				    		_xhtml_out.println("</a>");
				    		_xhtml_out.println("</li>\n");
				    	}
				    	if (permissions.hasSection(roles, "AdvancedTemplateStep")) {
				    		page = AdvancedTemplateJob.baseUrl;
			    			_xhtml_out.println("<li rel=\"AdvancedTemplateStep\" id=\"AdvancedTemplateStep\">");
			    			_xhtml_out.println("<a href=\""+page+"\">");
			    			_xhtml_out.println(getLanguageMessage("common.menu.advanced.administration"));
			    			_xhtml_out.println("</a>");
			    			_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
			    			if (permissions.isAllowed(roles, page, null)) {
				    			_xhtml_out.println("<li rel=\"AdvancedTemplateJob\" id=\"AdvancedTemplateJob\">");
				    			_xhtml_out.println("<a href=\""+page+"\">");
						    	_xhtml_out.println(getLanguageMessage("common.menu.advanced.template"));
						    	_xhtml_out.println("</a>");
						    	_xhtml_out.println("</li>\n");
			    			}
			    			page = AdvancedStep.baseUrl;
			    			if (permissions.isAllowed(roles, page, null)) {
				    			_xhtml_out.println("<li rel=\"AdvancedStep\" id=\"AdvancedStep\">");
				    			_xhtml_out.println("<a href=\""+page+"\">");
						    	_xhtml_out.println(getLanguageMessage("common.menu.advanced.step"));
						    	_xhtml_out.println("</a>");
						    	_xhtml_out.println("</li>\n");
			    			}
					    	_xhtml_out.println("</ul>\n");
			    			_xhtml_out.println("</li>\n");
			    		}
				    	if (permissions.hasSection(roles, "AdvancedConfiguration")) {
				    		page = AdvancedRemoteStorage.baseUrl;
				    		_xhtml_out.println("<li rel=\"AdvancedConfiguration\" id=\"AdvancedConfiguration\">");
				    		_xhtml_out.println("<a href=\""+page+"\">");
				    		_xhtml_out.println(getLanguageMessage("common.menu.advanced.configuration"));
				    		_xhtml_out.println("</a>");
				    		_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
				    		if (permissions.isAllowed(roles, page, null)) {
					    		_xhtml_out.println("<li rel=\"AdvancedRemoteStorage\" id=\"AdvancedRemoteStorage\">");
					    		_xhtml_out.println("<a href=\""+page+"\">");
					    		_xhtml_out.println(getLanguageMessage("common.menu.advanced.remotestorage"));
					    		_xhtml_out.println("</a>");
					    		_xhtml_out.println("</li>\n");
				    		}
				    		if (permissions.hasSection(roles, "AdvancedInventory")) {
				    			page = AdvancedRemoteInventory.baseUrl;
				    			_xhtml_out.println("<li rel=\"AdvancedInventory\" id=\"AdvancedInventory\">");
				    			_xhtml_out.println("<a href=\""+page+"\">");
				    			_xhtml_out.println(getLanguageMessage("common.menu.advanced.inventory"));
				    			_xhtml_out.println("</a>");
				    			_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
				    			if (permissions.isAllowed(roles, page, null)) {
				    				_xhtml_out.println("<li rel=\"AdvancedRemoteInventory\" id=\"AdvancedRemoteInventory\">");
				    				_xhtml_out.println("<a href=\""+page+"\">");
						    		_xhtml_out.println(getLanguageMessage("common.menu.advanced.remoteinventory"));
						    		_xhtml_out.println("</a>");
						    		_xhtml_out.println("</li>\n");
				    			}
				    			page = AdvancedApplication.baseUrl;
				    			if (permissions.isAllowed(roles, page, null)) {
					    			_xhtml_out.println("<li rel=\"AdvancedApplication\" id=\"AdvancedApplication\">");
					    			_xhtml_out.println("<a href=\""+page+"\">");
							    	_xhtml_out.println(getLanguageMessage("common.menu.advanced.applications"));
							    	_xhtml_out.println("</a>");
							    	_xhtml_out.println("</li>\n");
				    			}
				    			page = AdvancedSystem.baseUrl;
				    			if (permissions.isAllowed(roles, page, null)) {
					    			_xhtml_out.println("<li rel=\"AdvancedSystem\" id=\"AdvancedSystem\">");
					    			_xhtml_out.println("<a href=\""+page+"\">");
							    	_xhtml_out.println(getLanguageMessage("common.menu.advanced.systems"));
							    	_xhtml_out.println("</a>");
							    	_xhtml_out.println("</li>\n");
						    	}
						    	_xhtml_out.println("</ul>\n");
						    	_xhtml_out.println("</li>\n");
				    		}
			    			_xhtml_out.println("</ul>\n");
			    			_xhtml_out.println("</li>\n");
			    		}
			    		_xhtml_out.println("</ul>\n");
			    		_xhtml_out.println("</li>");
		    		}
		    		_xhtml_out.println("</ul>\n");*/
		    		_xhtml_out.println("</li>");
	    		}
	    	}
	    	/*
	    	 * TODO: uncomment with bacula
	    	 * if (permissions.hasSection(roles, "HA")) {
	    		page = HA.baseUrl;
	    		_xhtml_out.print("<li rel=\"HA\" id=\"HA\"><a href=\""+page+"\">");
		    	_xhtml_out.print(getLanguageMessage("common.menu.ha"));
		    	_xhtml_out.println("</a></li>");
	    	}*/
	    	if (permissions.hasSection(roles, "Subscribe")) {
	    		page = Suscription.baseUrl;
		    	_xhtml_out.print("<li rel=\"Subscribe\" id=\"Subscribe\"><a href=\""+page+"\">");
		    	_xhtml_out.print(getLanguageMessage("common.menu.suscription"));
		    	_xhtml_out.println("</a>");
	    		_xhtml_out.println("<ul id=\"submenu_tabs\" class=\"submenu_tabs\">\n");
	    		if (permissions.isAllowed(roles, page, null)) {
		    		_xhtml_out.println("<li rel=\"Suscription\" id=\"Suscription\">");
	    			_xhtml_out.println("<a href=\""+page+"\">");
	    			_xhtml_out.println(getLanguageMessage("suscription"));
	    			_xhtml_out.println("</a>");
		    		_xhtml_out.println("</li>\n");
	    		}
	    		page = Update.baseUrl;
	    		if (permissions.isAllowed(roles, page, null)) {
		    		_xhtml_out.println("<li rel=\"Update\" id=\"Update\">");
	    			_xhtml_out.println("<a href=\""+page+"\">");
	    			_xhtml_out.println(getLanguageMessage("update"));
	    			_xhtml_out.println("</a>");
		    		_xhtml_out.println("</li>\n");
	    		}
	    		_xhtml_out.println("</ul>\n");
	    		_xhtml_out.println("</li>");
	    	}
	    	if (permissions.hasSection(roles, "SystemStatistics")) {
	    		if (permissions.isAllowed(roles, page, null)) {
	    			page = SystemStatistics.baseUrl;
	    			_xhtml_out.print("<li rel=\"SystemStatistics\" id=\"SystemStatistics\"><a href=\""+page+"\">");
	    			_xhtml_out.print(getLanguageMessage("common.menu.statistics"));
	    			_xhtml_out.println("</a>");
	    			_xhtml_out.println("</li>");
	    		}
	    	}
	    	_xhtml_out.println("</ul>");
	    	_xhtml_out.println("</div>");
	    	_xhtml_out.println("</div>");
	    }
    	_xhtml_out.println("<div class=\"ui-layout-center\">");
    	
    	if (this.section == null || this.section.equals("Wizard") || this.section.equals("Login") || this.section.equals("") || this.section.contains(".html"))
    		_xhtml_out.println("<div id=\"content\" class=\"content\">");
    	
    	headerWrote = true;
	}
	
	/*private String parameterMapToString(Map<String, String[]> parameters){
		StringBuilder _result=new StringBuilder();
		Iterator<Map.Entry<String, String[]>> it=parameters.entrySet().iterator();
		boolean first=true;
		while (it.hasNext()){
			Map.Entry<String, String[]> param=it.next();
			if (first){
				_result.append("?");
				first=false;
			}else{
				_result.append("&");
			}
			_result.append(param.getKey()+"="+param.getValue()!=null ? param.getValue()[0] : "");
		}
		return _result.toString();
	}*/
	
	protected void writeDocumentFooter() throws IOException {
		if (!redirected) {
			PrintWriter _xhtml_out = response.getWriter();
			if (this.section == null || this.section.equals("Wizard") || this.section.equals("Login") || this.section.equals("") || this.section.contains(".html"))
				_xhtml_out.println("</div>");
			_xhtml_out.println("</div>");
			printPageJs();
			_xhtml_out.println("</body></html>");
		}
		redirected = false;
	}
	
	protected void printPageJs() throws IOException  {
		PrintWriter _xhtml_out = response.getWriter();
		_xhtml_out.print("<script type=\"text/javascript\">");
		if (pageGlobalJS != null && !pageGlobalJS.isEmpty())
    		_xhtml_out.print(pageGlobalJS);
    	if (pageJSFuncs != null && !pageJSFuncs.isEmpty())
    		_xhtml_out.print(pageJSFuncs);
    	_xhtml_out.println("var myLayout;");
    	_xhtml_out.println("$(function() {");
    	if (this.section != null && !this.section.equals("Wizard") && !this.section.equals("Login") && !this.section.equals("") && !this.section.contains(".html")) {
    		_xhtml_out.println("myLayout = $('body').layout({stateManagement__enabled: true});");
			_xhtml_out.println("$('#menu').jstree({'themes' : {'theme' : 'default', 'dots': false, 'icons': true, url : '../css/jstree.style.css'}, 'ui' : { 'initially_select' : ['"+this.section+"'], 'select_limit' : 1}, "+getMenuIconConfig()+"  'plugins' : [ 'themes', 'types', 'html_data', 'ui' ]});\n");
			_xhtml_out.println("$(document).on('click','#menu li a', function(event) { javascript:sendForm($(this).attr('href'));});");
    	}
    	pageJS+="$('.tooltip').tooltip();\n";
		//$(data.args[0].parentElement).attr('id'))
    	if (pageJS != null && !pageJS.isEmpty())
    		_xhtml_out.print(pageJS);
		_xhtml_out.print("});");
    	_xhtml_out.print("</script>");
	}
	
	protected String getMenuIconConfig() {
		StringBuilder sb = new StringBuilder();
		sb.append("'types' : {\n");
		sb.append("    'types' : {\n");
		sb.append("			'SystemConfiguration' 	: {'icon' : {'image' : '/images/configuration_16.png'} },\n");
		sb.append("			'SystemNetwork' 		: {'icon' : {'image' : '/images/network_16.png'} },\n");
		sb.append("			'SystemProxy' 			: {'icon' : {'image' : '/images/server_16.png'} },\n");
		sb.append("			'SystemClock' 			: {'icon' : {'image' : '/images/clock_16.png'} },\n");
		sb.append("			'SystemUsers' 			: {'icon' : {'image' : '/images/user_16.png'} },\n");
		sb.append("			'SystemMaintenance' 	: {'icon' : {'image' : '/images/lorry_16.png'} },\n");
		sb.append("			'WebServices' 			: {'icon' : {'image' : '/images/web_services_16.png'} },\n");
		sb.append("			'DeviceDisk' 			: {'icon' : {'image' : '/images/drive_16.png'} },\n");
		sb.append("			'DeviceReplication'		: {'icon' : {'image' : '/images/copy_16.png'} },\n");
		sb.append("			'DeviceNAS' 			: {'icon' : {'image' : '/images/share_16.png'} },\n");
		sb.append("			'VTLConfiguration' 		: {'icon' : {'image' : '/images/tape_16.png'} },\n");
		sb.append("			'DeviceISCSI' 			: {'icon' : {'image' : '/images/drive_network_16.png'} },\n");
		sb.append("			'CloudAccounts' 		: {'icon' : {'image' : '/images/clouds_16.png'} },\n");
		sb.append("			'BackupSummary' 		: {'icon' : {'image' : '/images/book_16.png'} },\n");
		sb.append("			'BackupHypervisors' 	: {'icon' : {'image' : '/images/hypervisor_16.png'} },\n");
		sb.append("			'BackupClients' 		: {'icon' : {'image' : '/images/computer_16.png'} },\n");
		sb.append("			'BackupJobsExtended' 	: {'icon' : {'image' : '/images/cog_16.png'} },\n");
		sb.append("			'BackupCategories' 		: {'icon' : {'image' : '/images/tag_16.png'} },\n");
		sb.append("			'BackupScheduleWeekly' 	: {'icon' : {'image' : '/images/calendar_16.png'} },\n");
		sb.append("			'BackupFilesetsClients'	: {'icon' : {'image' : '/images/page_16.png'} },\n");
		sb.append("			'BackupFilesetsLocal' 	: {'icon' : {'image' : '/images/fileset_local_16.png'} },\n");
		sb.append("			'BackupFilesetsNDMP' 	: {'icon' : {'image' : '/images/fileset_ndmp_16.png'} },\n");
		sb.append("			'BackupFilesetsVmware' 	: {'icon' : {'image' : '/images/hypervisor_16.png'} },\n");
		sb.append("			'BackupFilesets'		: {'icon' : {'image' : '/images/page_16.png'} },\n");
		sb.append("			'BackupPools' 			: {'icon' : {'image' : '/images/database_16.png'} },\n");
		sb.append("			'BackupStorageDisk' 	: {'icon' : {'image' : '/images/brick_16.png'} },\n");
		sb.append("			'AdvancedBackup' 		: {'icon' : {'image' : '/images/advanced_backup_16.png'} },\n");
		sb.append("			'AdvancedGroupJob' 		: {'icon' : {'image' : '/images/group_job_16.png'} },\n");
		sb.append("			'AdvancedTemplateStep' 	: {'icon' : {'image' : '/images/advanced_coordination_16.png'} },\n");
		sb.append("			'AdvancedTemplateJob' 	: {'icon' : {'image' : '/images/template_job_16.png'} },\n");
		sb.append("			'AdvancedStep'	 		: {'icon' : {'image' : '/images/step_16.png'} },\n");
		sb.append("			'AdvancedRemoteStorage' : {'icon' : {'image' : '/images/remote_storage_16.png'} },\n");
		sb.append("			'AdvancedScriptProcess' : {'icon' : {'image' : '/images/script_process_16.png'} },\n");
		sb.append("			'AdvancedInventory' 	: {'icon' : {'image' : '/images/storage_inventory_16.png'} },\n");
		sb.append("			'AdvancedApplication' 	: {'icon' : {'image' : '/images/application_16.png'} },\n");
		sb.append("			'AdvancedSystem'	 	: {'icon' : {'image' : '/images/system_16.png'} },\n");
		sb.append("			'AdvancedRemoteInventory' : {'icon' : {'image' : '/images/advanced_inventory_16.png'} },\n");
		sb.append("			'AdvancedConfiguration' : {'icon' : {'image' : '/images/advanced_configuration_16.png'} },\n");
		sb.append("			'Update' 				: {'icon' : {'image' : '/images/package_16.png'} },\n");
		sb.append("			'Suscription' 			: {'icon' : {'image' : '/images/shield_16.png'} },\n");
		sb.append("			'System'			 	: {'icon' : {'image' : '/images/system_config_16.png'} },\n");
		sb.append("			'Storage'			 	: {'icon' : {'image' : '/images/almacenamiento_16.png'} },\n");
		sb.append("			'Backup'			 	: {'icon' : {'image' : '/images/backup_16.png'} },\n");
		sb.append("			'Advanced'			 	: {'icon' : {'image' : '/images/advanced_16.png'} },\n");
		sb.append("			'Cdp'				 	: {'icon' : {'image' : '/images/cdp_16.png'} },\n");
		sb.append("			'HA'				 	: {'icon' : {'image' : '/images/ha_16.png'} },\n");
		sb.append("			'Subscribe'			 	: {'icon' : {'image' : '/images/subscription_16.png'} },\n");
		sb.append("			'SystemStatistics'		: {'icon' : {'image' : '/images/chart_16.png'} }\n");
		sb.append("		}\n");
		sb.append("},\n");
		return sb.toString();
	}
	
	protected void writeDocumentError(String message) throws IOException {
		if (!headerWrote) {
			try {
				writeDocumentHeader();
			} catch (Exception ex){}
		}
		PrintWriter _xhtml_out = response.getWriter();
		_xhtml_out.print("<br/><br/>");
		_xhtml_out.print("<div class=\"window_message\">");
        _xhtml_out.print("<h2>");
        _xhtml_out.print(getLanguageMessage("common.message.error"));
        _xhtml_out.print("</h2>");
        _xhtml_out.print("<div class=\"suberror\">");
        if(message != null) {
			_xhtml_out.print(getLanguageMessage(message));
		} else {
			_xhtml_out.print(getLanguageMessage("common.error.unknown"));
		}
        _xhtml_out.print("</div>");
        _xhtml_out.print("<div class=\"clear\"></div>");
        _xhtml_out.print("<a onclick=\"showLoadingPage();\" class=\"button\" href=\"javascript:history.go(-1);\">");
        _xhtml_out.print(getLanguageMessage("common.message.accept"));
        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
		_xhtml_out.print("</div>");
	}
	
	protected void writeDocumentError(String message, String url) throws IOException {
		if (!headerWrote) {
			try {
				writeDocumentHeader();
			} catch (Exception ex){}
		}
		PrintWriter _xhtml_out = response.getWriter();
		_xhtml_out.print("<br/><br/>");
		_xhtml_out.print("<div class=\"window_message\">");
        _xhtml_out.print("<h2>");
        _xhtml_out.print(getLanguageMessage("common.message.error"));
        _xhtml_out.print("</h2>");
        _xhtml_out.print("<div class=\"suberror\">");
        if(message != null) {
			_xhtml_out.print(getLanguageMessage(message));
		} else {
			_xhtml_out.print(getLanguageMessage("common.error.unknown"));
		}
        _xhtml_out.print("</div>");
        _xhtml_out.print("<div class=\"clear\"></div>");
        _xhtml_out.print("<a onclick=\"showLoadingPage();\" class=\"button\" href=\"javascript:sendForm('");
		_xhtml_out.print(url);
		_xhtml_out.print("');\">");
        _xhtml_out.print(getLanguageMessage("common.message.accept"));
        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
		_xhtml_out.print("</div>");
	}
	 
	protected void writeDocumentBack(String url) throws IOException {
		if (request.getHeader("referer") != null) {
			String refUrl = (String) request.getHeader("referer");
			refUrl =  refUrl.substring(refUrl.indexOf("/admin"));
			if (!isOnBlackList(refUrl))
				url = refUrl;
		}
		
		PrintWriter _xhtml_out = response.getWriter();
		_xhtml_out.print("<div class=\"back\">");
		if (url != null && !url.contains("history.go")){
			_xhtml_out.print("<a href=\"javascript:sendForm('");
		    _xhtml_out.print(url);
		    _xhtml_out.print("');\"><img src=\"/images/resultset_previous_16.png\" title=\"");
		}
		else{
			_xhtml_out.print("<a href=\"");
		    _xhtml_out.print(url);
		    _xhtml_out.print("\"><img src=\"/images/resultset_previous_16.png\" title=\"");
		}
    	_xhtml_out.print(getLanguageMessage("common.message.back"));
    	_xhtml_out.print("\" alt=\"");
    	_xhtml_out.print(getLanguageMessage("common.message.back"));
    	_xhtml_out.println("\"></a>");
    	_xhtml_out.print("</div>");
	}
	
	protected void writeDocumentBackForce(String url) throws IOException {
		
		PrintWriter _xhtml_out = response.getWriter();
		_xhtml_out.print("<div class=\"back\">");
		if (url != null && !url.contains("history.go")){
			_xhtml_out.print("<a href=\"javascript:sendForm('");
		    _xhtml_out.print(url);
		    _xhtml_out.print("');\"><img src=\"/images/resultset_previous_16.png\" title=\"");
		}
		else{
			_xhtml_out.print("<a href=\"");
		    _xhtml_out.print(url);
		    _xhtml_out.print("\"><img src=\"/images/resultset_previous_16.png\" title=\"");
		}
    	_xhtml_out.print(getLanguageMessage("common.message.back"));
    	_xhtml_out.print("\" alt=\"");
    	_xhtml_out.print(getLanguageMessage("common.message.back"));
    	_xhtml_out.println("\"></a>");
    	_xhtml_out.print("</div>");
	}
	
	private void writeDocumentGenericResponse(String message, List<String> values, String href, String continue_href) throws IOException {
		if (!headerWrote) {
			try {
				writeDocumentHeader();
			} catch (Exception ex){}
		}
		
		PrintWriter _xhtml_out = response.getWriter();
		if(href == null || href.isEmpty()) {
			href = "javascript:history.go(-1);";
		}
		else{
			href="javascript:sendForm('"+href+"');";
		}
		_xhtml_out.print("<br/><br/>");
		_xhtml_out.print("<div class=\"window_message\">");
        _xhtml_out.print("<h2>");
        _xhtml_out.print(getLanguageMessage("common.message.response"));
        _xhtml_out.print("</h2>");
        _xhtml_out.print("<div class=\"subinfo\">");
		if(message != null) {
			_xhtml_out.print(message);
		} else {
			_xhtml_out.print(getLanguageMessage("common.message.nochanges"));
		}
		if(values != null && !values.isEmpty()) {
			_xhtml_out.println(": <br/>");
			_xhtml_out.println("<ul>");
			for(String value : values) {
				_xhtml_out.print("<li>");
				_xhtml_out.print(value);
				_xhtml_out.println("</li>");
			}
			_xhtml_out.println("</ul>");
		}
		_xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"clear\"></div>");
        _xhtml_out.println("<a class=\"button\" href=\"");
        _xhtml_out.print(href);
        _xhtml_out.print("\">");
        _xhtml_out.print(getLanguageMessage("common.message.accept"));
        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
        if(continue_href != null && !continue_href.isEmpty()) {
        	_xhtml_out.print("<a class=\"button\" href=\"javascript:sendForm('");
            _xhtml_out.print(continue_href);
            _xhtml_out.print("');\">");
            _xhtml_out.print(getLanguageMessage("common.message.continue"));
            _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
        }
		_xhtml_out.print("</div>");
	}
	
	protected void writeJSFuncResponse(String message, String js) {
		try {
			
			PrintWriter _xhtml_out = response.getWriter();
			_xhtml_out.print("<div class=\"window_message\" style=\"font-size:12px;\">");
	        _xhtml_out.print("<h2>");
	        _xhtml_out.print(getLanguageMessage("common.message.response"));
	        _xhtml_out.print("</h2>");
	        _xhtml_out.print("<div class=\"subinfo\">");
			_xhtml_out.print(message);
			_xhtml_out.println("</div>");
	        _xhtml_out.println("<div class=\"clear\"></div>");
	        _xhtml_out.println("<a class=\"button\" style=\"color: #749C9C;\" href=\"");
	        _xhtml_out.print(js);
	        _xhtml_out.print("\">");
	        _xhtml_out.print(getLanguageMessage("common.message.accept"));
	        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
	        _xhtml_out.print("<br/>");
	        _xhtml_out.print("</div>");
		} catch (Exception ex) {}
	}
	
	protected void writeDocumentResponse(String message, String href) throws IOException {
		writeDocumentGenericResponse(message, null, href, null);
	}
	
	protected void writeDocumentListResponse(String message, List<String> values, String href) throws IOException {
		writeDocumentGenericResponse(message, values, href, null);
	}
	
	protected void writeDocumentContinueResponse(String message, String href, String continue_href) throws IOException {
		writeDocumentGenericResponse(message, null, href, continue_href);
	}
	
	protected void writeDocumentPartialContinueResponse(String message, List<String> values, String href, String continue_href) throws IOException {
		writeDocumentGenericResponse(message, values, href, continue_href);
	}
	
	private void writeDocumentGenericQuestion(String message, String warning, Map<String, String> attributes, String attribute, List<String> values, String href_accept, String href_deny) throws IOException {
		if (!headerWrote) {
			try {
				writeDocumentHeader();
			} catch (Exception ex){}
		}
		PrintWriter _xhtml_out = response.getWriter();
		if(href_accept == null || href_accept.isEmpty()) {
			href_accept = "#";
		}
		if(href_deny == null || href_deny.isEmpty()) {
			href_deny = "javascript:history.go(-1);";
		}
		else{
			href_deny="javascript:sendForm('"+href_deny+"');";
		}
		
		if(attributes != null && !attributes.isEmpty()) {
			String _path = href_accept;
			String _query = "";
			if(_path.contains("?")) {
				_query = _path.substring(_path.indexOf("?") + 1);
				_path = _path.substring(0, _path.indexOf("?"));
			}
			_xhtml_out.print("<form action=\"");
			_xhtml_out.print(_path);
			_xhtml_out.println("\" name=\"question_autocreated\" method=\"post\">");
			if(!_query.isEmpty()) {
				StringTokenizer _st = new StringTokenizer(_query, "&");
				while(_st.hasMoreTokens()) {
					String _value = _st.nextToken();
					_xhtml_out.print("<input type=\"hidden\" name=\"");
					_xhtml_out.print(_value.substring(0, _value.indexOf("=")));
					_xhtml_out.print("\" value=\"");
					_xhtml_out.print(_value.substring(_value.indexOf("=") + 1));
					_xhtml_out.println("\"/>");
				}
			}
			for(String _key : attributes.keySet()) {
				_xhtml_out.print("<input type=\"hidden\" name=\"");
				_xhtml_out.print(_key);
				_xhtml_out.print("\" value=\"");
				_xhtml_out.print(attributes.get(_key));
				_xhtml_out.println("\"/>");
			}
			_xhtml_out.print("</form>");
		} else if(values != null && !values.isEmpty() &&
				attribute != null && !attribute.isEmpty()) {
			String _path = href_accept;
			String _query = "";
			if(_path.contains("?")) {
				_query = _path.substring(_path.indexOf("?") + 1);
				_path = _path.substring(0, _path.indexOf("?"));
			}
			_xhtml_out.print("<form action=\"");
			_xhtml_out.print(_path);
			_xhtml_out.println("\" name=\"question_autocreated\" method=\"post\">");
			if(!_query.isEmpty()) {
				StringTokenizer _st = new StringTokenizer(_query, "&");
				while(_st.hasMoreTokens()) {
					String _value = _st.nextToken();
					_xhtml_out.print("<input type=\"hidden\" name=\"");
					_xhtml_out.print(_value.substring(0, _value.indexOf("=")));
					_xhtml_out.print("\" value=\"");
					_xhtml_out.print(_value.substring(_value.indexOf("=") + 1));
					_xhtml_out.println("\"/>");
				}
			}
			for(String value : values) {
				_xhtml_out.print("<input type=\"hidden\" name=\"");
				_xhtml_out.print(attribute);
				_xhtml_out.print("\" value=\"");
				_xhtml_out.print(value);
				_xhtml_out.println("\"/>");
			}
			_xhtml_out.print("</form>");
		}
		_xhtml_out.println("<br/><br/>");
		_xhtml_out.println("<div class=\"window_message\">");
        _xhtml_out.print("<h2>");
        _xhtml_out.print(getLanguageMessage("common.message.question"));
        _xhtml_out.println("</h2>");
        if(warning != null && !warning.isEmpty()) {
        	 _xhtml_out.print("<div class=\"subwarn\">");
        	 _xhtml_out.print(warning);
        	 _xhtml_out.print("</div>");
        }
        _xhtml_out.print("<div class=\"subinfo\">");
		if(message != null) {
			_xhtml_out.print(message);
		} else {
			_xhtml_out.print(" -- ");
		}
		if(values != null && !values.isEmpty() &&
				attribute != null && !attribute.isEmpty()) {
			_xhtml_out.println("<ul>");
			for(String value : values) {
				_xhtml_out.print("<li>");
				_xhtml_out.print(value);
				_xhtml_out.println("</li>");
			}
			_xhtml_out.println("</ul>");
		}
        _xhtml_out.println("</div>");
        _xhtml_out.println("<div class=\"clear\"></div>");
        _xhtml_out.print("<a class=\"button\" href=\"");
        if((attributes != null && !attributes.isEmpty()) ||
        		(values != null && !values.isEmpty() &&
				attribute != null && !attribute.isEmpty())) {
        	_xhtml_out.print("javascript:document.question_autocreated.submit();");
        } else {
        	_xhtml_out.print("javascript:sendForm('"+href_accept+"');");
        }
        _xhtml_out.print("\">");
        _xhtml_out.print(getLanguageMessage("common.message.accept"));
        _xhtml_out.print("<img src=\"/images/accept_16.png\"/></a>");
        _xhtml_out.print("<a class=\"button\" href=\"");
        _xhtml_out.print(href_deny);
        _xhtml_out.print("\">");
        _xhtml_out.print(getLanguageMessage("common.message.cancel"));
        _xhtml_out.print("<img src=\"/images/cross_16.png\"/></a>");
		_xhtml_out.print("</div>");
	}
	
	protected void downloadFile(HttpServletResponse response, String filePath) throws ServletException, IOException {  
		 File fileToDownload = new File(filePath);  
		 FileInputStream fileInputStream = new FileInputStream(fileToDownload);  
		  
		 ServletOutputStream out = response.getOutputStream();     
		 String mimeType =  new MimetypesFileTypeMap().getContentType(filePath);   
		  
		 response.setContentType(mimeType);   
		 response.setContentLength(fileInputStream.available());  
		 response.setHeader( "Content-Disposition", "attachment; filename=\""+ fileToDownload.getName() + "\"" );  
		  
		 int c;  
		 while((c=fileInputStream.read()) != -1){  
		  out.write(c);  
		 }  
		 out.flush();  
		 out.close();  
		 fileInputStream.close();  
	}  
	
	protected String getWBSLocalizedExMessage(String message) {
		try {
			if (message != null && message.length()>0) {
				StringBuffer _wbMsg = new StringBuffer();
				String _tmp = message;
				while (_tmp != null && _tmp.indexOf("<m>") > -1 && _tmp.indexOf("</m>") > -1) {
					String _loc = getLanguageMessage(_tmp.substring(_tmp.indexOf("<m>")+3, _tmp.indexOf("</m>")));
					if (_tmp.indexOf("<m>") > 0)
						_wbMsg.append(_tmp.substring(0, _tmp.indexOf("<m>")));
					_wbMsg.append(_loc);
					if (_tmp.length() > _tmp.indexOf("</m>")+4)
						_tmp = _tmp.substring(_tmp.indexOf("</m>")+4);
					else
						_tmp = null;
				}
				if (_tmp!=null && _tmp.length()>0)
					_wbMsg.append(_tmp);
				return _wbMsg.toString();
			} else {
				return message;
			}
		} catch (Exception ex) {
			return message;
		}
	}
	
	protected void writeDocumentQuestion(String message, String href_accept, String href_deny) throws IOException {
		writeDocumentGenericQuestion(message, null, null, null, null, href_accept, href_deny);
	}
	
	protected void writeDocumentWarningQuestion(String message, String warning, String href_accept, String href_deny) throws IOException {
		writeDocumentGenericQuestion(message, warning, null, null, null, href_accept, href_deny);
	}
	
	protected void writeDocumentListQuestion(String message, String attribute, List<String> values, String href_accept, String href_deny) throws IOException {
		writeDocumentGenericQuestion(message, null, null, attribute, values, href_accept, href_deny);
	}
	
	protected void writeDocumentMapQuestion(String message, Map<String, String> values, String href_accept, String href_deny) throws IOException {
		writeDocumentGenericQuestion(message, null, values, null, null, href_accept, href_deny);
	}
	
	private void fillRoles() {
		roleMap = new HashMap<String, String>();
		roleMap.put(RoleManager.roleAdAdvancedStorage, getLanguageMessage("common.role.advancedstorage"));
		roleMap.put(RoleManager.roleAdCoordinator, getLanguageMessage("common.role.advancedcoordinator"));
		roleMap.put(RoleManager.roleAdInventoryAppSo, getLanguageMessage("common.role.inventoryappso"));
		roleMap.put(RoleManager.roleAdInventoryRemote, getLanguageMessage("common.role.inventoryremote"));
		roleMap.put(RoleManager.roleAdmin, getLanguageMessage("common.role.administrator"));
		roleMap.put(RoleManager.roleOperator, getLanguageMessage("common.role.operator"));
		roleMap.put(RoleManager.roleUser, getLanguageMessage("common.role.user"));
		roleMap.put(RoleManager.roleGlobalOperator, getLanguageMessage("common.role.globaloperator"));
		roleMap.put(RoleManager.roleStorageManager, getLanguageMessage("common.role.storagemanager"));
		roleMap.put(RoleManager.roleCoordinator, getLanguageMessage("common.role.coordinator"));
		
		roleDescriptions = new HashMap<String, String>();
		roleDescriptions.put(RoleManager.roleAdAdvancedStorage, getLanguageMessage("common.role.advancedstorage.description"));
		roleDescriptions.put(RoleManager.roleAdCoordinator, getLanguageMessage("common.role.advancedcoordinator.description"));
		roleDescriptions.put(RoleManager.roleAdInventoryAppSo, getLanguageMessage("common.role.inventoryappso.description"));
		roleDescriptions.put(RoleManager.roleAdInventoryRemote, getLanguageMessage("common.role.inventoryremote.description"));
		roleDescriptions.put(RoleManager.roleAdmin, getLanguageMessage("common.role.administrator.description"));
		roleDescriptions.put(RoleManager.roleOperator, getLanguageMessage("common.role.operator.description"));
		roleDescriptions.put(RoleManager.roleUser, getLanguageMessage("common.role.user.description"));
		roleDescriptions.put(RoleManager.roleGlobalOperator, getLanguageMessage("common.role.globaloperator.description"));
		roleDescriptions.put(RoleManager.roleStorageManager, getLanguageMessage("common.role.storagemanager.description"));
		roleDescriptions.put(RoleManager.roleCoordinator, getLanguageMessage("common.role.coordinator.description"));
	}
}