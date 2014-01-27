package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.backup.CategoryManager;

public class BackupCategories extends WBSImagineServlet {
	static final long serialVersionUID = 20080902L;
	public final static int CATEGORY_ADD = 2;
	public final static int CATEGORY_EDIT = 3;
	public final static int CATEGORY_SAVE = 4;
	public final static int CATEGORY_DELETE = 5;
	private int type;
	public final static String baseUrl = "/admin/"+BackupCategories.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter _xhtml_out=response.getWriter();
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
		    
		    CategoryManager _cm = new CategoryManager();
		    
			switch(this.type) {
	    		default: {
	    				writeDocumentHeader();
		    			int _offset = 0;
		    			List<Map<String, String>> _categories = _cm.getCategories();
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/tag_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.backup.categories"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.categories.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.categories"));
	                    _xhtml_out.print("<a href=\"javascript:document.location.reload();\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.print("\" alt=\"");
			            _xhtml_out.print(getLanguageMessage("common.message.refresh"));
			            _xhtml_out.println("\"/></a>");
		                _xhtml_out.print("<a href=\"/admin/BackupCategories?type=");
		                _xhtml_out.print(CATEGORY_ADD);
		            	_xhtml_out.print("\"><img src=\"/images/add_16.png\" title=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.print("\" alt=\"");
		                _xhtml_out.print(getLanguageMessage("common.message.add"));
		                _xhtml_out.println("\"/></a>");

	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.print("<br/>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<table>");
	                    if(!_categories.isEmpty()) {
	                    	_xhtml_out.println("<tr>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.categories.name"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">");
		                    _xhtml_out.print(getLanguageMessage("backup.categories.description"));
		                    _xhtml_out.println("</td>");
		                    _xhtml_out.print("<td class=\"title\">&nbsp;-&nbsp;-&nbsp;</td>");
		                    _xhtml_out.println("</tr>");
		                    for(Map<String, String> category : _categories) {
		                    	_xhtml_out.print("<tr");
		                    	if(_offset % 2 == 0) {
		                    		_xhtml_out.print(" class=\"highlight\"");
		                    	}
		                    	_xhtml_out.println(">");
		                    	_xhtml_out.print("<td>");
								_xhtml_out.print(category.get("name"));
								_xhtml_out.println("</td>");
								_xhtml_out.print("<td>");
								_xhtml_out.print(category.get("description"));
								_xhtml_out.println("</td>");
								_xhtml_out.println("<td>");
								_xhtml_out.print("<a href=\"/admin/BackupCategories?type=");
								_xhtml_out.print(CATEGORY_EDIT);
								_xhtml_out.print("&name=");
								_xhtml_out.print(category.get("name"));
								_xhtml_out.print("\"><img src=\"/images/tag_edit_16.png\" title=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.print("\" alt=\"");
		                    	_xhtml_out.print(getLanguageMessage("common.message.edit"));
		                    	_xhtml_out.println("\"/></a>");
								_xhtml_out.print("<a href=\"/admin/BackupCategories?type=");
								_xhtml_out.print(CATEGORY_DELETE);
								_xhtml_out.print("&name=");
								_xhtml_out.print(category.get("name"));
								_xhtml_out.print("\"><img src=\"/images/tag_delete_16.png\" title=\"");
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
	                    	_xhtml_out.println(getLanguageMessage("device.message.no_categories"));
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
	    		case CATEGORY_ADD: {
	    				writeDocumentHeader();
	    			
		    			writeDocumentBack("/admin/BackupCategories");
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/tag_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.backup.categories"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.categories.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<form action=\"/admin/BackupCategories\" name=\"category\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + CATEGORY_SAVE + "\"/>");
	    			    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.categories.new_category"));
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.category.submit());\"><img src=\"/images/disk_16.png\" title=\"");
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.categories.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"name\"/>");
	        	    	_xhtml_out.println("<img src=\"/images/asterisk_orange_16.png\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"description\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.categories.description"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"description\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mail\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.mail"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"mail\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</form>");
	    			}
	    			break;
	    		case CATEGORY_EDIT: {
	    				writeDocumentHeader();
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception("debe indicar una categoria a editar");
		    			}
		    			
		    			writeDocumentBack("/admin/BackupCategories");
		    			Map<String, String> category = _cm.getCategory(request.getParameter("name"));
		    			
		    			_xhtml_out.println("<h1>");
	    				_xhtml_out.print("<img src=\"/images/tag_32.png\"/>");
		    	    	_xhtml_out.print(getLanguageMessage("common.menu.backup.categories"));
	    				_xhtml_out.println("</h1>");
		    			_xhtml_out.print("<div class=\"info\">");
		    			_xhtml_out.print(getLanguageMessage("backup.categories.info"));
		    			_xhtml_out.println("</div>");
		    			
		    			_xhtml_out.println("<form action=\"/admin/BackupCategories\" name=\"category\" method=\"post\">");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + CATEGORY_SAVE + "\"/>");
		    			_xhtml_out.println("<input type=\"hidden\" name=\"name\" value=\"" + request.getParameter("name") + "\"/>");
	    			    _xhtml_out.println("<div class=\"window\">");
						_xhtml_out.println("<h2>");
						_xhtml_out.print(getLanguageMessage("backup.categories.new_category"));
	            		_xhtml_out.print("<a href=\"javascript:submitForm(document.category.submit());\"><img src=\"/images/disk_16.png\" title=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
	                	_xhtml_out.print("\" alt=\"");
	                	_xhtml_out.print(getLanguageMessage("common.message.add"));
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
	        	    	_xhtml_out.print(getLanguageMessage("backup.categories.name"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"_name\" value=\"");
	        	    	_xhtml_out.print(category.get("name"));
	        	    	_xhtml_out.print("\" disabled=\"disabled\"/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"description\">");
	        	    	_xhtml_out.print(getLanguageMessage("backup.categories.description"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"description\"");
	        	    	if(category.get("description") != null) {
	        	    		_xhtml_out.print(" value=\"");
		        	    	_xhtml_out.print(category.get("description"));
		        	    	_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"mail\">");
	        	    	_xhtml_out.print(getLanguageMessage("system.general.administration.mail"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<input class=\"form_text\" type=\"text\" name=\"mail\"");
	        	    	if(category.get("mail") != null) {
	        	    		_xhtml_out.print(" value=\"");
		        	    	_xhtml_out.print(category.get("mail"));
		        	    	_xhtml_out.print("\"");
	        	    	}
	        	    	_xhtml_out.println("/>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</fieldset>");
	                    _xhtml_out.println("<div class=\"clear\"/></div>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("</form>");
					}
	    			break;
	    		case CATEGORY_SAVE: {
		    			
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.categories.specify_name"));
		    			}
		    			
		    			_cm.setCategory(request.getParameter("name"), request.getParameter("description"), request.getParameter("mail"));	
		    			response.sendRedirect("/admin/BackupCategories");
		    			this.redirected=true;
		    		}
	    			break;
	    		case CATEGORY_DELETE: {
		    			if(request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
		    				throw new Exception(getLanguageMessage("backup.categories.specify_name"));
		    			}
		    			
		    			if(request.getParameter("confirm") != null) {
		    				_cm.removeCategory(request.getParameter("name"));	    			
			    			response.sendRedirect("/admin/BackupCategories");
			    			this.redirected=true;
		    			} else {
		    				writeDocumentHeader();
		    				writeDocumentQuestion(getLanguageMessage("backup.categories.question"), "/admin/BackupCategories?type=" + CATEGORY_DELETE + "&name=" + request.getParameter("name") + "&confirm=true", null);

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
	
	public String removeSpaces(String s) {
		StringTokenizer st = new StringTokenizer(s," ",false);
		String t="";
		while(st.hasMoreElements()) t += st.nextElement();
		return t;
	}
}