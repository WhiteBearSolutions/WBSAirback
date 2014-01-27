package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.RoleManager;
import com.whitebearsolutions.imagine.wbsairback.UserManager;
import com.whitebearsolutions.imagine.wbsairback.backup.CategoryManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DirectoryConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;

public class SystemUsers extends WBSImagineServlet {
	static final long serialVersionUID = 20080902L;
	public final static int SAVE = 2;
	public final static int USERADD = 3;
	public final static int USEREDIT = 4;
	public final static int USERSAVE = 5;
	public final static int USERDEL = 6;
	public final static int EDIT_ROOT = 7;
	public final static int SAVE_PASSWORD_ROOT = 8;
	
	private int type;
	public final static String baseUrl = "/admin/"+SystemUsers.class.getSimpleName();
	
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
			if (type != USERSAVE && type != USERDEL)
				writeDocumentHeader();
	    	
			UserManager _um = new UserManager();
		    RoleManager _rm = new RoleManager();
		    CategoryManager _catm = new CategoryManager();
		    
			switch(this.type) {
	    		default: {
	    				Map<String, String> _directory = DirectoryConfiguration.getDirectory();
	    				List<Map<String, String>> _users = _um.getUsers();
	    				String[] _address = new String[] { "", "", "", "" };
	    				if(!_directory.get("host").isEmpty()) {
	    					_address = NetworkManager.toAddress(_directory.get("host"));
	    				}
	    				
		    			_xhtml_out.println("<form name=\"users\" id=\"users\" method=\"post\" action=\"/admin/SystemUsers\">");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SAVE + "\"/>");
	                    _xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.users"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.users.info"));
	                    _xhtml_out.print("</div>");
	                    
	                    _xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.users.directory"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.users.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
			            _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.save"));
				        _xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.print("\" alt=\"");
				        _xhtml_out.print(getLanguageMessage("common.message.refresh"));
				        _xhtml_out.println("\"/></a>");

	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"server\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.type"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"directory_type\">");
	        	    	_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
	        	    	_xhtml_out.print("<option value=\"ldapv3\"");
	        	    	if("ldapv3".equals(_directory.get("type"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">LDAPv3</option>");
	        	    	_xhtml_out.println("<option value=\"msad\"");
	        	    	if("msad".equals(_directory.get("type"))) {
	        	    		_xhtml_out.print(" selected=\"selected\"");
	        	    	}
	        	    	_xhtml_out.println(">Active Directory</option>");
	        	    	_xhtml_out.println("</select>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"basedn\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.basedn"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"basedn\" value=\"");
                    	_xhtml_out.print(_directory.get("basedn"));
                    	_xhtml_out.print("\"/>");
                    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"server\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.server"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"ip1\" value=\"");
	        	    	_xhtml_out.print(_address[0]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip2\" value=\"");
	                    _xhtml_out.print(_address[1]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip3\" value=\"");
	                    _xhtml_out.print(_address[2]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.print(".");
	                    _xhtml_out.print("<input class=\"network_octet\"type=\"text\" name=\"ip4\" value=\"");
	                    _xhtml_out.print(_address[3]);
	        	    	_xhtml_out.print("\"/>");
	                    _xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"port\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.port"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"network_octet\" type=\"text\" name=\"port\" value=\"");
                    	_xhtml_out.print(_directory.get("port"));
                    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user_dn\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.read_user_dn"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user_dn\" value=\"");
                    	_xhtml_out.print(_directory.get("manager"));
                    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user_dn_password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.read_user_dn_password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"user_dn_password\" value=\"");
                    	_xhtml_out.print(_directory.get("password"));
                    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"user\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.domain_user"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\" value=\"");
                    	_xhtml_out.print(_directory.get("domain.admin"));
                    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.directory.domain_user_password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_password\" type=\"password\" name=\"password\" value=\"");
                    	_xhtml_out.print(_directory.get("domain.admin.pass"));
                    	_xhtml_out.print("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.print("</div>");
	                    
	                    _xhtml_out.print("<div class=\"window\">");
	                    _xhtml_out.print("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.users"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("<a href=\"/admin/SystemUsers?type=");
	                    _xhtml_out.print(USERADD);
	                    _xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
                    	_xhtml_out.println("<tr>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("system.users.uid"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("system.users.roles"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("system.users.description"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">");
	                    _xhtml_out.print(getLanguageMessage("system.users.category"));
	                    _xhtml_out.println("</td>");
	                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
	                    _xhtml_out.println("</tr>");
	                    _xhtml_out.print("<tr style=\"background-color:#ffa0a0;\">");
						_xhtml_out.print("<td>");
						_xhtml_out.print("wbsairback");
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						_xhtml_out.print(getLanguageMessage("system.users.airbackuserrole"));
						_xhtml_out.print("</td>");
						_xhtml_out.print("<td>");
						_xhtml_out.print(getLanguageMessage("system.users.airbackuserdescripcion"));
						_xhtml_out.println("</td>");
						_xhtml_out.print("<td>");
						_xhtml_out.println("</td>");
						_xhtml_out.println("<td>");
						_xhtml_out.print("<a href=\"/admin/SystemUsers?type=");
						_xhtml_out.print(EDIT_ROOT);
						_xhtml_out.println("\">");
						_xhtml_out.print("<img src=\"/images/user_edit_16.png\" alt=\"");
						_xhtml_out.print(getLanguageMessage("common.message.edit"));
						_xhtml_out.print("\" title=\"");
						_xhtml_out.print(getLanguageMessage("common.message.edit"));
						_xhtml_out.print("\"/></a>");
						_xhtml_out.println("</td>");		
						_xhtml_out.println("</tr>");
						if(UserManager.systemUserExists("replica")) {
							_xhtml_out.print("<tr style=\"background-color:#ffa0a0;\">");
							_xhtml_out.print("<td>");
							_xhtml_out.print("replica");
							_xhtml_out.println("</td>");
							_xhtml_out.print("<td>");
							_xhtml_out.print(getLanguageMessage("system.users.airbackuserrole"));
							_xhtml_out.print("</td>");
							_xhtml_out.print("<td>");
							_xhtml_out.print(getLanguageMessage("system.users.airbackuserdescripcion"));
							_xhtml_out.println("</td>");
							_xhtml_out.print("<td>");
							_xhtml_out.println("</td>");
							_xhtml_out.println("<td>");
							_xhtml_out.print("<a href=\"/admin/SystemUsers?type=");
							_xhtml_out.print(EDIT_ROOT);
							_xhtml_out.println("&user=replica\">");
							_xhtml_out.print("<img src=\"/images/user_edit_16.png\" alt=\"");
							_xhtml_out.print(getLanguageMessage("common.message.edit"));
							_xhtml_out.print("\" title=\"");
							_xhtml_out.print(getLanguageMessage("common.message.edit"));
							_xhtml_out.print("\"/></a>");
							_xhtml_out.println("</td>");		
							_xhtml_out.println("</tr>");
						}
						if(!_users.isEmpty()) {
		                    int i = 0;
							for(Map<String, String>user : _um.getUsers()) {
								_xhtml_out.print("<tr");
								if(i % 2 == 0) {
									_xhtml_out.print(" class=\"table_row_highlight\""); 
								}
								_xhtml_out.println(">");
								_xhtml_out.print("<td>");
								_xhtml_out.print(user.get("uid"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								String cadRol = "";
								if (user.get("roles").contains(",")) {
									String[] roles = user.get("roles").split(",");
									boolean init = true;
									for (String rol : roles) {
										if (init) {
											cadRol = roleMap.get(rol);
											init = false;
										} else 
											cadRol += ", "+roleMap.get(rol);
									}
								} else
									cadRol = roleMap.get(user.get("roles"));
								_xhtml_out.print(cadRol);
								_xhtml_out.print("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(user.get("description"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(user.get("category"));
								_xhtml_out.println("</td>");
								_xhtml_out.println("<td>");
								_xhtml_out.print("<a href=\"/admin/SystemUsers?type=");
								_xhtml_out.print(USEREDIT);
								_xhtml_out.print("&username=");
								_xhtml_out.print(user.get("uid"));
								_xhtml_out.println("\">");
								_xhtml_out.print("<img src=\"/images/user_edit_16.png\" alt=\"");
								_xhtml_out.print(getLanguageMessage("common.message.edit"));
								_xhtml_out.print("\" title=\"");
								_xhtml_out.print(getLanguageMessage("common.message.edit"));
								_xhtml_out.print("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/SystemUsers?type=");
								_xhtml_out.print(USERDEL);
								_xhtml_out.print("&username=");
								_xhtml_out.print(user.get("uid"));
								_xhtml_out.print("\">");
								_xhtml_out.print("<img src=\"/images/user_delete_16.png\" alt=\"");
								_xhtml_out.print(getLanguageMessage("common.message.delete"));
								_xhtml_out.print("\" title=\"");
								_xhtml_out.print(getLanguageMessage("common.message.delete"));
								_xhtml_out.print("\"/></a>");
								_xhtml_out.println("</td>");		
								_xhtml_out.println("</tr>");
								i++;
							}
	                    } else {
	                    	_xhtml_out.println("<tr>");
			            	_xhtml_out.println("<td>");
			            	_xhtml_out.println(getLanguageMessage("system.message.no_users"));
			            	_xhtml_out.println("</td>");
			                _xhtml_out.println("</tr>");
	                    }
	                    _xhtml_out.println("</table>");
	                    _xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	    				_xhtml_out.print("</form>");
	    			}
        		    break;
	    		case SAVE: {
		    			int directory_type = DirectoryConfiguration.DIRECTORY_NONE, port = 0;
		    			String[] _address = null;
	    				if(request.getParameter("directory_type") != null && "ldapv3".equals(request.getParameter("directory_type"))) {
	    					directory_type = DirectoryConfiguration.DIRECTORY_LDAPv3;
	    				} else if(request.getParameter("directory_type") != null && "msad".equals(request.getParameter("directory_type"))) {
	    					directory_type = DirectoryConfiguration.DIRECTORY_ACTIVE_DIRECTORY;
	    				}
	    				if(directory_type != DirectoryConfiguration.DIRECTORY_NONE) {
	    					if(request.getParameter("ip1") == null || request.getParameter("ip1").trim().isEmpty() ||
									request.getParameter("ip2") == null || request.getParameter("ip2").trim().isEmpty() ||
									request.getParameter("ip3") == null || request.getParameter("ip3").trim().isEmpty() ||
									request.getParameter("ip4") == null || request.getParameter("ip4").trim().isEmpty()) {
								throw new Exception(getLanguageMessage("system.users.exception.no_directory"));
							}
	    					_address = new String[] { request.getParameter("ip1"), request.getParameter("ip2"), request.getParameter("ip3"), request.getParameter("ip4") };
	    				}
	    				try {
	    					port = Integer.parseInt(request.getParameter("port"));
	    				} catch(NumberFormatException _ex) {}
		    			DirectoryConfiguration.setDirectory(_address, port, request.getParameter("basedn"), request.getParameter("user_dn"), request.getParameter("user_dn_password"), directory_type, request.getParameter("user"), request.getParameter("password"));
	    				if(directory_type == DirectoryConfiguration.DIRECTORY_ACTIVE_DIRECTORY) {
	    					DirectoryConfiguration.setCIFSDomainMember(DirectoryConfiguration.CIFS_MEMBER_ACTIVE_DIRECTORY, "WBSAirback", _address, request.getParameter("basedn"), request.getParameter("user"), request.getParameter("password"));
	    				} else {
	    					DirectoryConfiguration.setCIFSDomainMember(DirectoryConfiguration.CIFS_MEMBER_NONE, "WBSAirback", _address, request.getParameter("basedn"), null, null);
	    				}
	    				writeDocumentResponse(getLanguageMessage("system.users.update_ok"), "/admin/SystemUsers");
	    			}
	    			break;
	    		case USERADD: {
	    				_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.multiselect.min.js\"></script>");
	    				_xhtml_out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery.multiselect.css\" />");
		    			GeneralSystemConfiguration _sc = new GeneralSystemConfiguration();
	    				_xhtml_out.println("<form action=\"/admin/SystemUsers\" name=\"users\" id=\"users\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + USERSAVE + "\"/>");
	                    _xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.users"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.users.info"));
	                    _xhtml_out.print("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.println(getLanguageMessage("system.users.new_user"));
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.users.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"username\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.uid"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"username\"/>");
	        	    	_xhtml_out.println(" <img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"role\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.roles"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("</div>");
	        	    	for(String role : _rm.getRoleNames()) {
	        	    		_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label></label>");
							_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"roles\" value=\"");
							_xhtml_out.print(role);
							_xhtml_out.print("\"><span class=\"tooltip\" title=\""+getLanguageMessage(roleDescriptions.get(role))+"\">");
							_xhtml_out.print(roleMap.get(role));
							_xhtml_out.println("</span></div>");
						}
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"description\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.description"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"description\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"category\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.category"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"category\" id=\"category\" multiple=\"multiple\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String category : _catm.getCategoryNames()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(category);
							_xhtml_out.print("\">");
							_xhtml_out.print(category);
							_xhtml_out.print("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						if(_sc.getLDAPDirectory().get("server") == null || _sc.getLDAPDirectory().get("server").isEmpty()){
							_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"passwd1\">");
		        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.password"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"form_password\" type=\"password\" name=\"passwd1\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"passwd2\">");
		        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.passwordConfirmation"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"form_password\" type=\"password\" name=\"passwd2\"/>");
		        	    	_xhtml_out.println("</div>");
						}
						_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	    				_xhtml_out.println("</form>");
	    				pageJS+="$('#category').multiselect({height: 120, checkAllText:'"+getLanguageMessage("common.message.all")+"',uncheckAllText:'"+getLanguageMessage("common.message.none")+"',noneSelectedText:'"+getLanguageMessage("common.message.selectOptions")+"',selectedText:'# "+getLanguageMessage("common.message.selected")+"', selectedList: 2});\n";
	    			}
	    			break;
	    		case USEREDIT: {
		    			if(request.getParameter("username") == null || request.getParameter("username").isEmpty()) {
		    				throw new Exception(getLanguageMessage("system.users.exception.user"));
		    			}
		    			_xhtml_out.println("<script type=\"text/javascript\" src=\"/jscript/jquery.multiselect.min.js\"></script>");
		    			_xhtml_out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery.multiselect.css\" />");
		    			
		    			GeneralSystemConfiguration _sc = new GeneralSystemConfiguration();
		    			Map<String, String> user = _um.getUserAttributes(request.getParameter("username"));
		    			List<String> roles = _um.getUserRoles(request.getParameter("username"));
		    			List<String> categories = _um.getUserCategories(request.getParameter("username"));
		    			
		    			_xhtml_out.println("<form action=\"/admin/SystemUsers\" name=\"users\" id=\"users\" method=\"post\">");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + USERSAVE + "\"/>");
	    			    _xhtml_out.println("<input type=\"hidden\" name=\"username\" value=\"" + request.getParameter("username") + "\"/>");
	                    _xhtml_out.println("<h1>");
		    			_xhtml_out.print("<img src=\"/images/user_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("system.users"));
		    			_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("system.users.info"));
	                    _xhtml_out.print("</div>");
	                    
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.println(getLanguageMessage("system.users.edit_user"));
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.users.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.add"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.refresh"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"username\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.uid"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"user\" value=\"");
	        	    	_xhtml_out.print(request.getParameter("username"));
	        	    	_xhtml_out.println("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println(" <img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"role\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.roles"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("</div>");
	        	    	for(String role : _rm.getRoleNames()) {
	        	    		_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label></label>");
							_xhtml_out.print("<input class=\"form_checkbox\" type=\"checkbox\" name=\"roles\" value=\"");
							_xhtml_out.print(role);
							_xhtml_out.print("\"");
							if(roles.contains(role))  {
								_xhtml_out.print(" checked=\"checked\"");
							}
							_xhtml_out.print("><span class=\"tooltip\" title=\""+getLanguageMessage(roleDescriptions.get(role))+"\">");
							_xhtml_out.print(roleMap.get(role));
							_xhtml_out.println("</span></div>");
						}
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"description\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.description"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<input class=\"form_text\" type=\"text\" name=\"description\" value=\"");
	        	    	_xhtml_out.print(user.get("description"));
	        	    	_xhtml_out.println("\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"category\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.users.category"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.println("<select class=\"form_select\" name=\"category\" id=\"category\" multiple=\"multiple\">");
						_xhtml_out.println("<option value=\"\">&nbsp;-&nbsp;-&nbsp;</option>");
						for(String category : _catm.getCategoryNames()) {
							_xhtml_out.print("<option value=\"");
							_xhtml_out.print(category);
							_xhtml_out.print("\"");
							if(categories.contains(category))  {
								_xhtml_out.print(" selected=\"selected\"");
							}
							_xhtml_out.print(">");
							_xhtml_out.print(category);
							_xhtml_out.print("</option>");
						}
						_xhtml_out.println("</select>");
						_xhtml_out.println("</div>");
						if(_sc.getLDAPDirectory().get("server") == null || _sc.getLDAPDirectory().get("server").isEmpty()){
							_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"passwd1\">");
		        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.password"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"form_password\" type=\"password\" name=\"passwd1\"/>");
		        	    	_xhtml_out.println("</div>");
		        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    	_xhtml_out.print("<label for=\"passwd2\">");
		        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.passwordConfirmation"));
		        	    	_xhtml_out.println(": </label>");
		        	    	_xhtml_out.println("<input class=\"form_password\" type=\"password\" name=\"passwd2\"/>");
		        	    	_xhtml_out.println("</div>");
						}
						_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	    				_xhtml_out.println("</form>");
	    				pageJS+="$('#category').multiselect({height: 120, checkAllText:'"+getLanguageMessage("common.message.all")+"',uncheckAllText:'"+getLanguageMessage("common.message.none")+"',noneSelectedText:'"+getLanguageMessage("common.message.selectOptions")+"',selectedText:'# "+getLanguageMessage("common.message.selected")+"', selectedList: 2});\n";
	    			}
	    			break;
	    		case USERSAVE: {
	    				Map<String, String> _directory = DirectoryConfiguration.getDirectory();
	    				if(request.getParameter("username") == null || request.getParameter("username").isEmpty()) {
		    				throw new Exception(getLanguageMessage("system.users.exception.user"));
		    			}
		    			
		    			if((request.getParameter("passwd1") != null && !request.getParameter("passwd1").isEmpty()) ||
		    					(request.getParameter("passwd2") != null && !request.getParameter("passwd2").isEmpty())) {
		    				if(request.getParameter("passwd1") == null) {
		    					throw new Exception(getLanguageMessage("system.users.exception.no_passwd"));
		    				} else if(request.getParameter("passwd2") == null) {
		    					throw new Exception(getLanguageMessage("system.users.exception.no_passwd"));
		    				} else if(!request.getParameter("passwd1").equals(request.getParameter("passwd2"))) {
		    					throw new Exception(getLanguageMessage("system.users.exception.no_passwd"));
		    				}
		    			}
		    			
		    			List<String> roles = new ArrayList<String>();
		    			if(request.getParameter("roles") != null && request.getParameterValues("roles").length > 0) {
		    				roles.addAll(Arrays.asList(request.getParameterValues("roles")));
		    			} else {
		    				throw new Exception(getLanguageMessage("system.users.exception.no_roles"));
		    			}
		    			
		    			if(_directory.get("type").isEmpty()) {
		    				if(!_um.userExists(request.getParameter("username"))) {
		    					UserManager.addSystemUser(request.getParameter("username"));
		    				}
		    				_um.setUser(request.getParameter("username"), roles, request.getParameter("description"), request.getParameterValues("category"));
		    				if(request.getParameter("passwd1") != null && !request.getParameter("passwd1").isEmpty()) {
			    				_um.setLocalUserPassword(request.getParameter("username"), request.getParameter("passwd1"));
			    			}
		    			} else {
		    				_um.setUser(request.getParameter("username"), roles, request.getParameter("description"), request.getParameterValues("category"));
		    			}
		    			response.sendRedirect("/admin/SystemUsers");
		    			this.redirected=true;
	    			}
	    			break;
	    		case USERDEL: {
		    			if(request.getParameter("username") == null || request.getParameter("username").isEmpty()) {
		    				throw new Exception(getLanguageMessage("system.users.exception.user"));
		    			}
	    			
	    				if(request.getParameter("confirm") != null) {
	    					_um.removeLocalUser(request.getParameter("username"));
	    					response.sendRedirect("/admin/SystemUsers");
	    					this.redirected=true;
		    			} else {
		    				writeDocumentQuestion(getLanguageMessage("system.users.question") + " <strong>" + request.getParameter("username") + "</strong>?", "/admin/SystemUsers?type=" + USERDEL + "&username=" + request.getParameter("username") + "&confirm=true", null);
		    			}
	    			}
	    			break;
	    		case EDIT_ROOT: {
		    			String _user = "wbsairback";
		    			if(request.getParameter("user") != null && !request.getParameter("user").isEmpty()) {
							_user = request.getParameter("user");
						}
		    			
		    			_xhtml_out.println("<form name=\"root\" method=\"post\" action=\"/admin/SystemUsers\">");
		    			_xhtml_out.print("<input type=\"hidden\" name=\"user\" value=\"" + _user + "\"/>");
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("system.console.password"));
	                    _xhtml_out.print("<a href=\"javascript:submitForm(document.root.submit());\"><img src=\"/images/disk_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.print("\" alt=\"");
					    _xhtml_out.print(getLanguageMessage("common.message.save"));
					    _xhtml_out.println("\"/></a>");
	                    _xhtml_out.print("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	                    _xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + SAVE_PASSWORD_ROOT + "\"/>");
	        	    	_xhtml_out.print("<label for=\"password\">");
	        	    	_xhtml_out.print(getLanguageMessage("common.login.password"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"password\" name=\"password\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</form>");
	    			}
	    			break;
	    		case SAVE_PASSWORD_ROOT: {
		    			String _user = "wbsairback";
		    			if(request.getParameter("password") == null || request.getParameter("password").isEmpty()) {
							throw new Exception(getLanguageMessage("system.console.exception"));
						}
		    			if(request.getParameter("user") != null && !request.getParameter("user").isEmpty()) {
							_user = request.getParameter("user");
						}
						
		    			if(_user.equals("wbsairback")) {
		    				if(!UserManager.getRemotePasswordAccess()) {
		    					UserManager.setRemotePasswordAccess(true);
		    				}
		    			}
						UserManager.changeSystemUserPassword(_user, request.getParameter("password"));
						writeDocumentResponse(getLanguageMessage("system.console.update_ok"), "/admin/SystemUsers");
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