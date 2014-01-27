package com.whitebearsolutions.imagine.wbsairback.servlets;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;

public class Suscription extends WBSImagineServlet {
	static final long serialVersionUID = 20109871393791L;
	public final static int REGISTER = 2;
	public final static int REGISTER_STORE = 3;
	public final static int REGISTER_LICENSE_STORE = 4;
	private int type;
	public final static String baseUrl = "/admin/"+Suscription.class.getSimpleName();
	
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	    
	    response.setContentType("text/html");
	    PrintWriter _xhtml_out = response.getWriter();
	    
	    try {
	    	if(!this.securityManager.isLogged()) {
	    		response.sendRedirect("/admin/Login");
	    		this.redirected=true;
	    	}
	    	
	    	response.setContentType("text/html");
	    	
	    	this.type = 0;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}
			
			LicenseManager _lm = new LicenseManager();
			switch(this.type) {
    			default: {
    					writeDocumentHeader();
	    				String _serial = LicenseManager.getSystemSerial();
	    				_xhtml_out.print("<form action=\"/admin/Suscription\" name=\"suscription\" method=\"post\">");
    		            _xhtml_out.println("<h1>");
    					_xhtml_out.print("<img src=\"/images/shield_32.png\"/>");
    			    	_xhtml_out.print(getLanguageMessage("suscription"));
    					_xhtml_out.println("</h1>");
    					_xhtml_out.println("<div class=\"info\">");
    					_xhtml_out.print(getLanguageMessage("suscription.info"));
    					_xhtml_out.println("</div>");
	    				_xhtml_out.println("<input type=\"hidden\" name=\"type\" value=\"" + REGISTER_LICENSE_STORE + "\"/>");
						_xhtml_out.println("<div class=\"window\">");
	                    _xhtml_out.println("<h2>");
	                    _xhtml_out.print(getLanguageMessage("suscription.registry"));
	                    _xhtml_out.println("</h2>");
	                    _xhtml_out.println("<fieldset>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"uuid\">");
	        	    	_xhtml_out.print(getLanguageMessage("suscription.uuid"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<strong>");
	        	    	_xhtml_out.print(_lm.getUnitUUID());
	        	    	_xhtml_out.println("</strong>");
	                    _xhtml_out.println("</div>");
	                    _xhtml_out.println("<div class=\"standard_form\">");
	        	    	_xhtml_out.print("<label for=\"serial\">");
	        	    	_xhtml_out.print(getLanguageMessage("suscription.serial"));
	        	    	_xhtml_out.println(": </label>");
	        	    	_xhtml_out.print("<strong>");
	        	    	if(_serial != null) {
	        	    		_xhtml_out.print(_serial);
	        	    	}
	        	    	_xhtml_out.println("</strong>");
	        	    	_xhtml_out.println("</div>");
	        	    	_xhtml_out.println("<div class=\"standard_form\">");
		        	    _xhtml_out.print("<label for=\"registry\">");
		        	    _xhtml_out.print(getLanguageMessage("suscription.registry"));
		        	    _xhtml_out.println(": </label>");
        	    		_xhtml_out.println("<img src=\"/images/rosette_gray_16.png\"/>");
        	    		_xhtml_out.print("<strong>");
        	    		_xhtml_out.print(getLanguageMessage("community"));
        	    		_xhtml_out.print("</strong>");
		        	    _xhtml_out.println("</div>");
	                    _xhtml_out.println("</fieldset>");
	        	    	_xhtml_out.println("<div class=\"clear\"></div>");
	                    _xhtml_out.println("</div>");
    				}
    	            break;
			}
            _xhtml_out.print("</form>");
	    } catch(Exception _ex) {
	    	writeDocumentError(_ex.getMessage());
	    } finally {
	    	writeDocumentFooter();
	    }
	}
}