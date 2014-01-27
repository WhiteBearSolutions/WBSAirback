package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.http.AbstractRequest;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
//import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.TomcatConfiguration;
import com.whitebearsolutions.imagine.wbsairback.util.MailReport;
import com.whitebearsolutions.mail.Mail;
import com.whitebearsolutions.util.Configuration;

public class SystemConfiguration extends WBSImagineServlet {
	static final long serialVersionUID = 20071109L;
	public final static int STORE = 2;
	public final static int SHUTDOWN = 3;
	public final static int REBOOT = 4;
	public final static int START_SERVICE = 11;
	public final static int STOP_SERVICE = 12;
	public final static int CHECK_EMAIL = 13;
	public final static int CHECK_REPORT = 14;
	private int type;
	public final static String baseUrl = "/admin/"+SystemConfiguration.class.getSimpleName();

	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
		PrintWriter _xhtml_out = response.getWriter();
	    try {
	    	
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	AbstractRequest _ar = new AbstractRequest(request);
	    	this.type = 1;
			if(_ar.getParameter("type") != null && !_ar.getParameter("type").isEmpty()) {
				try {
					this.type = Integer.parseInt(_ar.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			response.setContentType("text/html");
			writeDocumentHeader();
		    
	    	GeneralSystemConfiguration _sc = new GeneralSystemConfiguration();
	    	
	    	switch(this.type) {
	    		default: {
	    				_xhtml_out.println("<script>");
		    			_xhtml_out.println("<!--");
		    			_xhtml_out.println("function send() {");
		    			_xhtml_out.println("  if(document.system.https_certificate.value != \"\") {");
		    			_xhtml_out.println("    document.system.enctype=\"multipart/form-data\";");
		    			_xhtml_out.println("  }");
		    			_xhtml_out.println("  submitForm(document.system.submit());");
		    			_xhtml_out.println("}");
		    			_xhtml_out.println("//-->");
		    			_xhtml_out.println("</script>");
	    			
		    			_xhtml_out.println("<form action=\"/admin/SystemConfiguration\" name=\"system\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + STORE + "\"/>");
	    				_xhtml_out.print("<h1>");
		    			_xhtml_out.print("<img src=\"/images/configuration_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.system.general"));
		    			_xhtml_out.print("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.general.info"));
	                    _xhtml_out.print("</div>");
	                    
	                    _xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.general.administration"));
	                    _xhtml_out.print("<a href=\"javascript:send();\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
		                _xhtml_out.println("\"/></a>");
		                _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                _xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		                _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_password\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"passwordConfirmation\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.passwordConfirmation"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_password\" type=\"password\" name=\"passwordConfirmation\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.print("<br/><br/>");
	                    /*
	                     // TODO: uncomment with bacula
	                     * _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mail\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.mail"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"mail\"");
	                    if(_sc.getBaculaMailAccount() != null) {
	                    	_xhtml_out.print(" value=\"");
	                    	_xhtml_out.print(_sc.getBaculaMailAccount());
	                    	_xhtml_out.print("\"");
	                    }
	                    _xhtml_out.println("/>");
	                    _xhtml_out.print("<select class=\"form_select\" name=\"bacula_email_level\">");
	        	    	_xhtml_out.print("<option value=\""+GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ALL+"\" ");
	        	    	if(GeneralSystemConfiguration.getBaculaMailLevel() != null && !GeneralSystemConfiguration.getBaculaMailLevel().isEmpty() && GeneralSystemConfiguration.getBaculaMailLevel().equals(GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ALL)) {
	                    	_xhtml_out.print(" selected=\"selected\" ");
	                    }
	        	    	_xhtml_out.println(">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.bacula.email.level.all"));
	        	    	_xhtml_out.print("</option>");
	        	    	_xhtml_out.print("<option value=\""+GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ONLY_ERROR+"\" ");
	        	    	if(GeneralSystemConfiguration.getBaculaMailLevel() != null && !GeneralSystemConfiguration.getBaculaMailLevel().isEmpty() && GeneralSystemConfiguration.getBaculaMailLevel().equals(GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ONLY_ERROR)) {
	                    	_xhtml_out.print(" selected=\"selected\" ");
	                    }
	        	    	_xhtml_out.println(">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.bacula.email.level.only.error"));
	        	    	_xhtml_out.print("</option>");
	        	    	_xhtml_out.print("<option value=\""+GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ONLY_OK+"\" ");
	        	    	if(GeneralSystemConfiguration.getBaculaMailLevel() != null && !GeneralSystemConfiguration.getBaculaMailLevel().isEmpty() && GeneralSystemConfiguration.getBaculaMailLevel().equals(GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ONLY_OK)) {
	                    	_xhtml_out.print(" selected=\"selected\" ");
	                    }
	        	    	_xhtml_out.println(">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.bacula.email.level.only.ok"));
	        	    	_xhtml_out.print("</option>");
	                    _xhtml_out.println("</select>");
	                    if(_sc.getBaculaMailAccount() != null) {
	                    	_xhtml_out.println("<input type=\"button\" value=\""+getLanguageMessage("system.general.mailcheck")+"\" onClick=\"window.location.href='/admin/SystemConfiguration?type="+CHECK_EMAIL+"';\">");
	                    }
	        	    	_xhtml_out.println("</div>");*/
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mail\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.mail.from"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"mailfrom\"");
	        	    	if(_sc.getMailFromAccount() != null && !_sc.getMailFromAccount().isEmpty()) {
	        	    		_xhtml_out.print(" value=\"");
		                    _xhtml_out.print(_sc.getMailFromAccount());
		                    _xhtml_out.print("\"");
		                } else if (_sc.getBaculaMailAccount() != null && !_sc.getBaculaMailAccount().isEmpty()) {
		                   	_xhtml_out.print(" value=\"");
		                   	_xhtml_out.print(_sc.getBaculaMailAccount());
		                   	_xhtml_out.print("\"");
		                }
	                    _xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mailhost\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.relay"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"mailhost\"");
	                    if(_sc.getMailServer() != null) {
	                    	_xhtml_out.print(" value=\"");
	                    	_xhtml_out.print(_sc.getMailServer());
	                    	_xhtml_out.print("\"");
	                    }
	                    _xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"snmptraphost\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.snmptraphost"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"snmptraphost\"");
	                    if(_sc.getSnmpTrapServer() != null && !_sc.getSnmpTrapServer().isEmpty()) {
	                    	_xhtml_out.print(" value=\"");
	                    	_xhtml_out.print(_sc.getSnmpTrapServer());
	                    	_xhtml_out.print("\"");
	                    }
	                    _xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"snmptrapversion\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.snmptrapversion"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<select class=\"form_select\" name=\"snmptrapversion\">");
	        	    	_xhtml_out.print("<option value=\"2\" ");
	        	    	if(_sc.getSnmpTrapVersion() != null && !_sc.getSnmpTrapVersion().isEmpty() && _sc.getSnmpTrapVersion().equals("2")) {
	                    	_xhtml_out.print(" selected=\"selected\" ");
	                    }
	        	    	_xhtml_out.println(">V.2");
	        	    	_xhtml_out.print("</option>");
	        	    	_xhtml_out.print("<option value=\"1\" ");
	        	    	if(_sc.getSnmpTrapVersion() != null && !_sc.getSnmpTrapVersion().isEmpty() && _sc.getSnmpTrapVersion().equals("1")) {
	                    	_xhtml_out.print(" selected=\"selected\" ");
	                    }
	        	    	_xhtml_out.println(">V.1");
	        	    	_xhtml_out.print("</option>");
	                    _xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"snmptrapmemory\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.snmptrapmemory"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"snmptrapmemory\"");
	                    if(_sc.getSnmpTrapMemory() != null && !_sc.getSnmpTrapMemory().isEmpty()) {
	                    	_xhtml_out.print(" value=\"");
	                    	_xhtml_out.print(Integer.parseInt(_sc.getSnmpTrapMemory())/1024);
	                    	_xhtml_out.print("\"");
	                    }
	                    _xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	/*
	        	    	 // TODO: uncomment with bacula
	        	    	 * _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"maxreloadreq\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.maxreloadreq"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"maxreloadreq\"");
	        	    	String maxReloadReq = "";
	                    if(_sc.getMaximumReloadRequests() != null && !_sc.getMaximumReloadRequests().isEmpty()) 
	                    	maxReloadReq = _sc.getMaximumReloadRequests();
                    	_xhtml_out.print(" value=\"");
                    	_xhtml_out.print(maxReloadReq);
                    	_xhtml_out.print("\"");
	                    _xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");*/
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mail\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.mail.report"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"mailreport\"");
	        	    	if(_sc.getMailReportAccount() != null && !_sc.getMailReportAccount().isEmpty()) {
	        	    		_xhtml_out.print(" value=\"");
		                    _xhtml_out.print(_sc.getMailReportAccount());
		                    _xhtml_out.print("\"");
		                } else if (_sc.getBaculaMailAccount() != null && !_sc.getBaculaMailAccount().isEmpty()) {
		                   	_xhtml_out.print(" value=\"");
		                   	_xhtml_out.print(_sc.getBaculaMailAccount());
		                   	_xhtml_out.print("\"");
		                }
	                    _xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	/*
	        	    	 // TODO: uncomment with bacula
	        	    	 * _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"reporthour\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.report_hour"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"reporthour\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						int _hour = _sc.getReportHour();
						for(int i = 1; i <= 24; i++) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(i);
							_xhtml_out.print("\"");
							if(i == _hour) {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(StringFormat.getTwoCharTimeComponent(i));
							_xhtml_out.println(":00</option>");
						}_xhtml_out.println("</select>");
						if(_sc.getBaculaMailAccount() != null) {
	                    	_xhtml_out.println("<input type=\"button\" value=\""+getLanguageMessage("system.general.reportcheck")+"\" onClick=\"window.location.href='/admin/SystemConfiguration?type="+CHECK_REPORT+"';\">");
	                    }
						_xhtml_out.println("</div>");*/
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
		    			
	        	    	_xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.general.https"));
	                    _xhtml_out.print("<a href=\"javascript:send();\"><img src=\"/images/disk_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.save"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.save"));
		               	_xhtml_out.println("\"/></a>");
	                   	_xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		               	_xhtml_out.print("\" alt=\"");
		               	_xhtml_out.print(getLanguageMessage("common.message.refresh"));
		               	_xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.print("<div class=\"subinfo\">");
		    			_xhtml_out.print(getLanguageMessage("system.general.info_HTTPS"));
		    			_xhtml_out.print("</div>");
	                    _xhtml_out.println("<fieldset>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"https\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.https.check"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"https\" value=\"true\"");
	                    if(TomcatConfiguration.checkHTTPS()) {
	                    	_xhtml_out.print(" checked=\"checked\"");
	                    }
	                    _xhtml_out.print("/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"https_certificate\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.https.https_certificate"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_file\" type=\"file\" name=\"https_certificate\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"https_certificate_password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.https.https_certificate_password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"https_certificate_password\"/>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("<div class=\"subwarn\">");
	                    _xhtml_out.print("<strong>");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.warning"));
	        	    	_xhtml_out.print("</strong>: ");
		    			_xhtml_out.print(getLanguageMessage("system.general.info_enable"));
		    			_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.general.services"));
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.print("<div class=\"subinfo\">");
	                    _xhtml_out.print(getLanguageMessage("system.general.info_management"));
	                    _xhtml_out.print("</div>");
	                    _xhtml_out.println("<fieldset>");
	        	    	/*
	        	    	 // TODO: uncomment with bacula
	        	    	 * _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">BACKUP-DIRECTOR: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.BACULA_DIR + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.BACULA_DIR + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">BACKUP-STORAGE: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.BACULA_SD)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.BACULA_SD + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.BACULA_SD + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">BACKUP-FILE: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.BACULA_FD)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.BACULA_FD + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.BACULA_FD + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">BACKUP-DATABASE: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.POSTGRES)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.POSTGRES + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.POSTGRES + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");*/
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">ISCSI: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.ISCSI_TARGET)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.ISCSI_TARGET + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.ISCSI_TARGET + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">NFS: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.NFS)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.NFS + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.NFS + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">CIFS: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.SAMBA)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.SAMBA + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.SAMBA + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">FTP: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.FTP)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.FTP + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.FTP + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.println("<label for=\"https\">WATCHDOG: </label>");
	        	    	if(ServiceManager.isRunning(ServiceManager.WATCHDOG)) {
	                    	_xhtml_out.print("<span class=\"green\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.started"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stop"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + STOP_SERVICE + "&service=" + ServiceManager.WATCHDOG + "';\"/>");
	                    } else {
	                    	_xhtml_out.print("<span class=\"red\">");
	                    	_xhtml_out.print(getLanguageMessage("common.message.stopped"));
	                    	_xhtml_out.println("</span>");
	                    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	                    	_xhtml_out.print(getLanguageMessage("common.message.start"));
	                    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + START_SERVICE + "&service=" + ServiceManager.WATCHDOG + "';\"/>");
	                    }
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("<div class=\"subwarn\">");
	        	    	_xhtml_out.print("<strong>");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.warning"));
	        	    	_xhtml_out.print("</strong>: ");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.warning"));
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	        	    	
	        	    	_xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.general.system"));
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label>&nbsp;</label>");
	        	    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.reboot"));
	        	    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + REBOOT + "';\"/>");
	        	    	_xhtml_out.print("<input class=\"form_button\" type=\"button\" value=\"");
	        	    	_xhtml_out.print(getLanguageMessage("common.message.shutdown"));
	        	    	_xhtml_out.print("\" onClick=\"document.location.href='/admin/SystemConfiguration?type=" + SHUTDOWN + "';\"/>");
	        	    	_xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	                    
	                    _xhtml_out.println("</form>");
	    			}
                    break;
	    		case STORE: {
		    			StringBuilder reply = new StringBuilder();
		    			
		    			if(_ar.getParameter("password") != null && !_ar.getParameter("password").isEmpty()) {
		    				if(!_ar.getParameter("password").equals(_ar.getParameter("passwordConfirmation"))) {
		    					throw new Exception(getLanguageMessage("common.message.no_password"));
		    				}
		    				_sc.setRootPassword(_ar.getParameter("password"));
		    				reply.append(getLanguageMessage("common.login.password"));
		    			}
		    			
		    			if(_ar.getParameter("mailfrom") != null && !_ar.getParameter("mailfrom").isEmpty()) {
		    				if(!_ar.getParameter("mailfrom").matches("[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
		    					throw new Exception(getLanguageMessage("common.message.no_cc"));
		    				}
		    				
		    				_sc.setMailFromAccount(_ar.getParameter("mailfrom"));
		    				
		    				if(reply.length() > 0) {
		    					reply.append(getLanguageMessage("system.general.and_config_email_from"));
			    			} else {
			    				reply.append(getLanguageMessage("system.general.config_email_from"));
			    			}
		    			} else {
		    				_sc.setMailFromAccount(null);
		    			}
		    			
		    			/*
		    			 * TODO: uncomment with bacula
		    			 * String baculaMessagesLevel = GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ALL;
		    			if (_ar.getParameter("bacula_email_level") != null && !_ar.getParameter("bacula_email_level").isEmpty())
		    				baculaMessagesLevel = _ar.getParameter("bacula_email_level");
		    			
		    			if(_ar.getParameter("mail") != null && !_ar.getParameter("mail").isEmpty()) {
		    				if(!_ar.getParameter("mail").matches("[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
		    					throw new Exception(getLanguageMessage("common.message.no_cc"));
		    				}
		    				
		    				_sc.setBaculaMailAccount(_ar.getParameter("mail"), baculaMessagesLevel);
		    				
		    				if(reply.length() > 0) {
		    					reply.append(getLanguageMessage("system.general.and_config_email"));
			    			} else {
			    				reply.append(getLanguageMessage("system.general.config_email"));
			    			}
		    			} else {
		    				_sc.setBaculaMailAccount(null, baculaMessagesLevel);
		    			}*/
		    			
		    			if(_ar.getParameter("mailreport") != null && !_ar.getParameter("mailreport").isEmpty()) {
		    				if(!_ar.getParameter("mailreport").matches("[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
		    					throw new Exception(getLanguageMessage("common.message.no_cc"));
		    				}
		    				
		    				_sc.setMailReportAccount(_ar.getParameter("mailreport"));
		    				
		    				if(reply.length() > 0) {
		    					reply.append(getLanguageMessage("system.general.and_config_email_report"));
			    			} else {
			    				reply.append(getLanguageMessage("system.general.config_email_report"));
			    			}
		    			} else {
		    				_sc.setMailReportAccount(null);
		    			}
		    			
		    			
		    			if(_ar.getParameter("mailhost") != null && !_ar.getParameter("mailhost").isEmpty()) {
		    				_sc.setMailServer(_ar.getParameter("mailhost"));
	    				} else {
	    					_sc.setMailServer(null);
	    				}
		    			
		    			boolean snmpMessage = false;
		    			if(_ar.getParameter("snmptrapmemory") != null && !_ar.getParameter("snmptrapmemory").isEmpty()) {
		    				_sc.setSnmpTrapMemory(_ar.getParameter("snmptrapmemory"));
		    				snmpMessage = true;
	    				} else {
	    					_sc.setSnmpTrapMemory(null);
	    				}
		    			
		    			if(_ar.getParameter("snmptraphost") != null && !_ar.getParameter("snmptraphost").isEmpty()) {
		    				_sc.setSnmpTrapHost(_ar.getParameter("snmptraphost"), _ar.getParameter("snmptrapversion"));
		    				snmpMessage = true;
	    				} else {
	    					_sc.setSnmpTrapHost(null, null);
	    				}
		    			
		    			if (snmpMessage) {
			    			if(reply.length() > 0) {
		    					reply.append(getLanguageMessage("system.general.and_config_snmp"));
			    			} else {
			    				reply.append(getLanguageMessage("system.general.config_snmp"));
			    			}
		    			}
		    			
		    			if(_ar.getParameter("reporthour") != null && !_ar.getParameter("reporthour").isEmpty()) {
		    				try {
			    				_sc.setReportHour(Integer.parseInt(_ar.getParameter("reporthour")));
			    			} catch(NumberFormatException _ex) {}
		    			} else {
		    				_sc.setReportHour(-1);
		    			}
		    			
		    			/*
		    			 * TODO: uncomment with bacula
		    			 * if(_ar.getParameter("maxreloadreq") != null && !_ar.getParameter("maxreloadreq").isEmpty()) {
		    				try {
			    				_sc.setMaximumReloadRequests(Integer.parseInt(_ar.getParameter("maxreloadreq")));
			    				if(reply.length() > 0) {
			    					reply.append(getLanguageMessage("system.general.and_config_maxreloadreq"));
				    			} else {
				    				reply.append(getLanguageMessage("system.general.config_maxreloadreq"));
				    			}
			    			} catch(NumberFormatException _ex) {}
		    			} else {
		    				_sc.setMaximumReloadRequests(32);
		    			}*/
		    			
		    			this.sessionManager.reloadConfiguration();
	    				
		    			TomcatConfiguration _tc = new TomcatConfiguration(this.sessionManager.getConfiguration());
	    				if(_ar.hasParameter("https") && "true".equals(_ar.getParameter("https"))) {
	    					if(!TomcatConfiguration.checkHTTPS()) {
		    					_tc.setHTTPS(true);
		    					if(_ar.hasParameter("https_certificate") && !_ar.getParameter("https_certificate").isEmpty()) {
		    						if(!_ar.hasParameter("https_certificate_password") || _ar.getParameter("https_certificate_password").isEmpty()) {
			    						throw new Exception(getLanguageMessage("common.message.exception"));
			    					}
		    						byte[] _data = _ar.getFile("https_certificate");
			    					if(_data != null && _data.length > 0) {
			    						_tc.setPKCS12(_data, _ar.getParameter("https_certificate_password").toCharArray());
				    				}
			    				}
			    				_tc.store();
			    				
			    				if(reply.length() > 0) {
			    					reply.append(getLanguageMessage("system.general.and_acc_secure"));
				    			} else {
				    				reply.append(getLanguageMessage("system.general.acc_secure"));
				    			}
			    				ServiceManager.restartWebAdministration();
		    				}
		    			} else if(TomcatConfiguration.checkHTTPS()) {
		    				_tc.setHTTPS(false);
		    				_tc.store();
		    				
		    				if(reply.length() > 0) {
		    					reply.append(getLanguageMessage("system.general.and_acc_secure"));
			    			} else {
			    				reply.append(getLanguageMessage("system.general.acc_secure"));
			    			}
		    				ServiceManager.restartWebAdministration();
		    			}
	    				
	    				if(reply.length() > 0) {
	    					reply.append(getLanguageMessage("system.general.info_stored"));
	    					writeDocumentResponse(reply.toString(), "/admin/SystemConfiguration");
		    			} else {
		    				writeDocumentResponse(null, "/admin/SystemConfiguration");
		    			}
	    			}
	    			break;
	    		case SHUTDOWN: {
		    			if(request.getParameter("confirm") != null) {
			    			writeDocumentResponse(getLanguageMessage("system.general.info_shutdown"), "/admin/Login?logout=true");
			    			ServiceManager.shutdownSystem();
				    	} else {
							writeDocumentQuestion(getLanguageMessage("system.general.question_shutdown"), "/admin/SystemConfiguration?type=" + SHUTDOWN + "&confirm=true", null);
			    		}
	    			}
	    			break;
	    		case REBOOT: {
		    			if(request.getParameter("confirm") != null) {
		    				writeDocumentResponse(getLanguageMessage("system.general.info_restart"), "/admin/Login?logout=true");
			    			ServiceManager.restartSystem();
				    	} else {
				    		/*TODO: if(JobManager.hasRunningJobs()) {
				    			writeDocumentWarningQuestion(getLanguageMessage("system.general.question_restart"), getLanguageMessage("system.general.jobs.running"), "/admin/SystemConfiguration?type=" + REBOOT + "&confirm=true", null);
		    				} else {*/
		    					writeDocumentQuestion(getLanguageMessage("system.general.question_restart"), "/admin/SystemConfiguration?type=" + REBOOT + "&confirm=true", null);
		    				//}
			    		}
	    			}
	    			break;
	    		case START_SERVICE: {
		    			int service = -1;
		    			try {
		    				service = Integer.parseInt(request.getParameter("service"));
		    				if(service == ServiceManager.BACULA_DIR || service == ServiceManager.BACULA_SD || service == ServiceManager.BACULA_FD ||service == ServiceManager.POSTGRES
		    						|| service == ServiceManager.ISCSI_TARGET || service == ServiceManager.NFS || service == ServiceManager.SAMBA
		    						|| service == ServiceManager.FTP || service == ServiceManager.WATCHDOG) {
		    					ServiceManager.start(service);
		    				}
		    			} catch(NumberFormatException _ex) {}
		    			writeDocumentResponse(getLanguageMessage("system.general.info_started"), "/admin/SystemConfiguration");
	    			}
	    			break;
	    		case STOP_SERVICE: {
	    				int service = -1;
		    			try {
		    				service = Integer.parseInt(request.getParameter("service"));
		    				if(service == ServiceManager.BACULA_DIR || service == ServiceManager.BACULA_SD || service == ServiceManager.BACULA_FD || service == ServiceManager.POSTGRES
		    						|| service == ServiceManager.ISCSI_TARGET || service == ServiceManager.NFS || service == ServiceManager.SAMBA
		    						|| service == ServiceManager.FTP || service == ServiceManager.WATCHDOG) {
		    					ServiceManager.fullStop(service);
		    				}
		    			} catch(NumberFormatException _ex) {}
		    			writeDocumentResponse(getLanguageMessage("system.general.info_stopped"), "/admin/SystemConfiguration");
	    			}
	    			break;
	    		case CHECK_EMAIL: {
	    			try {
	    				Configuration _tc = new Configuration();
		    			_tc.setProperty("mail.host", "localhost");
		    			Mail _m = new Mail(_tc);
		    			String _account = _sc.getBaculaMailAccount();
		    			if(_sc.getBaculaMailAccount() != null) {
		    				_m.addTo(_account);
		    			} else {
		    				throw new Exception(getLanguageMessage("system.general.no_email.configured"));
		    			}
		    			_m.setFrom("WBSAIRBACK", _sc.getMailFromAccount());
		    			_m.setSubject("test email");
		    			_m.setHTML("<h3>"+getLanguageMessage("system.general.send.title")+"</h3> <p>"+getLanguageMessage("system.general.send.body")+"</p> <p>"+getLanguageMessage("system.general.send.final")+"</p> <br /> <b>WBSgo</b> Support team: <i>soporte@whitebearsolutions.com</i><br />");
		    			_m.send();
		    			writeDocumentResponse(getLanguageMessage("system.general.email_sended"), "/admin/SystemConfiguration");
	    			} catch (Exception ex) {
	    				writeDocumentError(getLanguageMessage("system.general.exception.email_sended")+": "+ex.getMessage());
	    			}
    			}
	    		break;
	    		case CHECK_REPORT: {
	    			try {
	    				MailReport m = new MailReport();
	    				m.sendMail();
		    			writeDocumentResponse(getLanguageMessage("system.general.report_sended"), "/admin/SystemConfiguration");
	    			} catch (Exception ex) {
	    				writeDocumentError(getLanguageMessage("system.general.exception.report_sended")+": "+ex.getMessage());
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