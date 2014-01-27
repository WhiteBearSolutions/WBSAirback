package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.disk.CloudManager;


public class CloudAccounts extends WBSImagineServlet{

	private static final long serialVersionUID = -1977407770557695974L;
	public final static int ADD_ACCOUNT = 2;
	public final static int EDIT_ACCOUNT = 3;
	public final static int REMOVE_ACCOUNT = 4;
	public final static int STORE_ACCOUNT = 5;
	
	private int type;
	public final static String baseUrl = "/admin/"+CloudAccounts.class.getSimpleName();

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
			writeDocumentHeader();
			
			switch (this.type) {
				default: {
					List<String> _accountNames = CloudManager.listAccountAliases();
    				_xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/clouds_32.jpg\"/>");
	    	    	_xhtml_out.print(getLanguageMessage("common.menu.device.cloud.accounts"));
    				_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("device.cloud.accounts.info"));
	    			_xhtml_out.println("</div>");
					_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("device.cloud.accounts"));
                    _xhtml_out.print("<a href=\"/admin/CloudAccounts?type=");
                    _xhtml_out.print(ADD_ACCOUNT);
                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
	                _xhtml_out.print(getLanguageMessage("common.message.add"));
				    _xhtml_out.print("\" alt=\"");
				    _xhtml_out.print(getLanguageMessage("common.message.add"));
			        _xhtml_out.println("\"/></a>");
                    _xhtml_out.println("</h2>");
                    _xhtml_out.print("<br/>");
                    _xhtml_out.println("<fieldset>");
                    _xhtml_out.println("<table>");
                    
                    if (!_accountNames.isEmpty()) {
                    	_xhtml_out.println("<tr>");
                    	_xhtml_out.println("<td>&nbsp;</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("device.cloudAccounts.name"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("device.cloud.account.type"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
	                    _xhtml_out.println("</tr>");
	                    int _offset=1;
                    	for (String account : _accountNames) {
                    		_xhtml_out.print("<tr");
	                    	if(_offset % 2 == 0) {
	                    		_xhtml_out.print(" class=\"highlight\"");
	                    	}
	                    	_xhtml_out.println(">");
	                    	_xhtml_out.println("<td>&nbsp;</td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.print(account);
	                    	_xhtml_out.println("</td>");
	                    	_xhtml_out.println("<td>");
	                    	_xhtml_out.print(CloudManager.getAccount(account).get("type"));
	                    	_xhtml_out.println("</td>");
		                    _xhtml_out.println("<td>");
		                    _xhtml_out.print("<a href=\"/admin/CloudAccounts?type=");
		                    _xhtml_out.print(EDIT_ACCOUNT);
		                    _xhtml_out.print("&name=");
		                    _xhtml_out.print(account);
		                    _xhtml_out.print("\"><img src=\"/images/user_edit_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    _xhtml_out.println("\"/></a>");
		                    _xhtml_out.print("<a href=\"/admin/CloudAccounts?type=");
		                    _xhtml_out.print(REMOVE_ACCOUNT);
		                    _xhtml_out.print("&name=");
		                    _xhtml_out.print(account);
		                    _xhtml_out.print("\"><img src=\"/images/user_delete_16.png\" title=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
		                    _xhtml_out.print("\" alt=\"");
		                    _xhtml_out.print(getLanguageMessage("common.message.delete"));
		                    _xhtml_out.println("\"/></a>");
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.println("</tr>");
		                    _offset++;
                    	}
                    } else {
                    	_xhtml_out.println("<tr>");
                    	_xhtml_out.println("<td>");
                    	_xhtml_out.println(getLanguageMessage("device.message.no_accounts"));
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
				
				case ADD_ACCOUNT: {
					writeDocumentBack("/admin/CloudAccounts");
    				_xhtml_out.print("<form action=\"/admin/CloudAccounts\" name=\"cloudform\" method=\"post\">");
    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_ACCOUNT + "\"/>");
    				_xhtml_out.print("<input type=\"hidden\" name=\"new\" value=\"true\"/>");
    				_xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/clouds_32.jpg\"/>");
    				_xhtml_out.print(getLanguageMessage("common.menu.device.cloud.accounts"));
    				_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("device.cloud.accounts.info"));
	    			_xhtml_out.println("</div>");
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("device.cloud.accounts.new"));
                    _xhtml_out.print("<a href=\"javascript:submitForm(document.cloudform.submit());\"><img src=\"/images/disk_16.png\" title=\"");
			        _xhtml_out.print(getLanguageMessage("common.message.save"));
			        _xhtml_out.print("\" alt=\"");
			        _xhtml_out.print(getLanguageMessage("common.message.save"));
			        _xhtml_out.println("\"/></a>");
			        _xhtml_out.println("</h2>");
	    			_xhtml_out.println("<fieldset>");
	    			
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"name\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.name"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<input class=\"form_text\" name=\"name\"/>");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"userid\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.userid"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<input class=\"form_text\" name=\"userid\" />");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"userkey\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.userkey"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<input class=\"form_text\" name=\"userkey\" />");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"atmosserver\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.atmosserver"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<input class=\"form_text\" name=\"atmosserver\" />");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"typeAccount\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.type"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<select class=\"form_select\" name=\"typeAccount\"/>");
        	    	_xhtml_out.println("<option value=\""+CloudManager.TYPE_S3+"\" selected=\"selected\">S3</option>");
        	    	_xhtml_out.println("<option value=\""+CloudManager.TYPE_ATMOS+"\">Atmos</option>");
        	    	_xhtml_out.println("</select>");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	_xhtml_out.println("</fieldset>");
        	    	
                    _xhtml_out.println("<div class=\"clear\"></div>");
                    _xhtml_out.println("</div>");
                    _xhtml_out.print("</form>");
				}
				break;
				
				case EDIT_ACCOUNT: {
					if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.cloud.account.error.name"));
    				}
					
					Map<String, String> account = CloudManager.getAccount(request.getParameter("name"));
					writeDocumentBack("/admin/CloudAccounts");
    				_xhtml_out.print("<form action=\"/admin/CloudAccounts\" name=\"cloudform\" method=\"post\">");
    				_xhtml_out.print("<input type=\"hidden\" name=\"type\" value=\"" + STORE_ACCOUNT + "\"/>");
    				_xhtml_out.println("<h1>");
    				_xhtml_out.print("<img src=\"/images/clouds_32.jpg\"/>");
    				_xhtml_out.print(getLanguageMessage("common.menu.device.cloud.accounts"));
    				_xhtml_out.println("</h1>");
	    			_xhtml_out.print("<div class=\"info\">");
	    			_xhtml_out.print(getLanguageMessage("device.cloud.accounts.info"));
	    			_xhtml_out.println("</div>");
	    			
	    			_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("device.cloud.accounts.edit"));
                    _xhtml_out.print("<a href=\"javascript:submitForm(document.cloudform.submit());\"><img src=\"/images/disk_16.png\" title=\"");
			        _xhtml_out.print(getLanguageMessage("common.message.save"));
			        _xhtml_out.print("\" alt=\"");
			        _xhtml_out.print(getLanguageMessage("common.message.save"));
			        _xhtml_out.println("\"/></a>");
			        _xhtml_out.println("</h2>");
	    			_xhtml_out.println("<fieldset>");
	    			
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"name\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.name"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.print("<input class=\"form_text\" name=\"name\" value=\"");
        	    	if (account.get("name") != null)
        	    		_xhtml_out.print(account.get("name"));
        	    	_xhtml_out.print("\" />");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"userid\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.userid"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.print("<input class=\"form_text\" name=\"userid\" value=\"");
        	    	if (account.get("userid") != null)
        	    		_xhtml_out.print(account.get("userid"));
        	    	_xhtml_out.print("\" />");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"userkey\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.userkey"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.print("<input class=\"form_text\" name=\"userkey\" value=\"");
        	    	if (account.get("userkey") != null)
        	    		_xhtml_out.print(account.get("userkey"));
        	    	_xhtml_out.print("\" />");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"atmosserver\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.atmosserver"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.print("<input class=\"form_text\" name=\"atmosserver\" value=\"");
        	    	if (account.get("atmosserver") != null)
        	    		_xhtml_out.print(account.get("atmosserver"));
        	    	_xhtml_out.print("\" />");
        	    	_xhtml_out.println("</div>");
        	    	
                    _xhtml_out.println("<div class=\"standard_form\">");
        	    	_xhtml_out.print("<label for=\"typeAccount\">");
        	    	_xhtml_out.print(getLanguageMessage("device.cloud.account.type"));
        	    	_xhtml_out.println(": </label>");
        	    	_xhtml_out.println("<select class=\"form_select\" name=\"typeAccount\"/>");
        	    	_xhtml_out.print("<option value=\""+CloudManager.TYPE_S3+"\" ");
        	    	if (account.get("type") != null && account.get("type").equals("S3"))
        	    		_xhtml_out.print("selected=\"selected\" ");
        	    	_xhtml_out.print(">S3</option>");
        	    	_xhtml_out.print("<option value=\""+CloudManager.TYPE_ATMOS+"\" ");
        	    	if (account.get("type") != null && account.get("type").equals("Atmos"))
        	    		_xhtml_out.print("selected=\"selected\" ");
        	    	_xhtml_out.print(">Atmos</option>");
        	    	_xhtml_out.println("</select>");
        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
        	    	_xhtml_out.println("</div>");
        	    	_xhtml_out.println("</fieldset>");
        	    	
                    _xhtml_out.println("<div class=\"clear\"></div>");
                    _xhtml_out.println("</div>");
                    _xhtml_out.print("</form>");
				}
				break;
				
				case REMOVE_ACCOUNT: {
					if (request.getParameter("confirm") != null) {
						if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
    						throw new Exception(getLanguageMessage("device.cloud.account.error.name"));
    					}
						CloudManager.removeAccount(request.getParameter("name"), false);
						writeDocumentResponse(getLanguageMessage("device.cloud.account.removed"), "/admin/CloudAccounts");
					} else {
						writeDocumentQuestion(getLanguageMessage("device.cloud.account.question"), "/admin/CloudAccounts?type=" + REMOVE_ACCOUNT + "&name=" + request.getParameter("name")+"&confirm=true", null);
					}
				}
				break;
				
				case STORE_ACCOUNT: {
					if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.cloud.account.error.name"));
    				}
					if(request.getParameter("userid") == null || request.getParameter("userid").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.cloud.account.error.userid"));
    				}
					if(request.getParameter("userkey") == null || request.getParameter("userkey").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.cloud.account.error.userkey"));
    				}
					if(request.getParameter("typeAccount") == null || request.getParameter("typeAccount").isEmpty()) {
    					throw new Exception(getLanguageMessage("device.cloud.account.error.type"));
    				}
					int typeAccount = Integer.parseInt(request.getParameter("typeAccount"));
					if (typeAccount == CloudManager.TYPE_ATMOS && (request.getParameter("atmosserver") == null || request.getParameter("atmosserver").isEmpty())){
						throw new Exception(getLanguageMessage("device.cloud.account.error.atmosserver"));
					}
					
					if(request.getParameter("new") != null) {
						List<String> accountNames = CloudManager.listAccountAliases();
						if (accountNames.contains(request.getParameter("name"))) {
							writeDocumentError(getLanguageMessage("device.cloud.account.duplicated"));
							return;
						}
					}
					
					CloudManager.createAccount(request.getParameter("name"), request.getParameter("userid"), request.getParameter("userkey"), typeAccount, request.getParameter("atmosserver"));
					writeDocumentResponse(getLanguageMessage("device.cloud.account.stored"), "/admin/CloudAccounts");
				}
				break;
			}
			
	    } catch (Exception _ex) {
	    	writeDocumentError(getWBSLocalizedExMessage(_ex.getMessage()));
	    } finally {
	    	writeDocumentFooter();
	    }
		
	}

}
