package com.whitebearsolutions.imagine.wbsairback.servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Command;


public class WebServices extends WBSImagineServlet {

	private static final long serialVersionUID = 463604782865932313L;
	public final static int GET_SERVICE = 2;
	private int type;
	public final static String baseUrl = "/admin/"+WebServices.class.getSimpleName();
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter _xhtml_out = response.getWriter();
	    try {		
			this.type = 1;
			if(request.getParameter("type") != null && request.getParameter("type").length() > 0) {
				try {
					this.type = Integer.parseInt(request.getParameter("type"));
				} catch(NumberFormatException _ex) {}
			}

			switch(this.type) {
				case GET_SERVICE: {
					String nameService = null;
					if(request.getParameter("service") != null && request.getParameter("service").length() > 0) {
						try {
							nameService = request.getParameter("service");
						} catch(Exception _ex) {
							throw new Exception(getLanguageMessage("wsdl.services.ex.no.service"));
						}
					}
					response.setContentType("text/xml");
					String wsdl = getService(nameService);
					_xhtml_out.flush();
					_xhtml_out.print(wsdl);
					return;
				}
				
	    		default: {
	    			response.setContentType("text/html");
	    			writeDocumentHeader();
	    			List<String> _services = getServicesList();
					_xhtml_out.println("<h1>");
					_xhtml_out.print("<img src=\"/images/web_services_32.png\"/>");
			    	_xhtml_out.print(getLanguageMessage("wsdl.title"));
					_xhtml_out.println("</h1>");
					_xhtml_out.print("<div class=\"info\">");
					_xhtml_out.print(getLanguageMessage("wsdl.info"));
					_xhtml_out.println("</div>");
					
					_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("wsdl.services.wadl"));
		            _xhtml_out.println("</h2>");
		            _xhtml_out.print("<br/>");
		            _xhtml_out.println("<fieldset>");
		            _xhtml_out.println("<table>");
		            _xhtml_out.println("<tr>");
	                _xhtml_out.print("<td class=\"title\">");
	                _xhtml_out.print(getLanguageMessage("wsdl.services.name"));
	                _xhtml_out.println("</td>");
	                _xhtml_out.println("</tr>");
		            _xhtml_out.print("<tr class=\"highlight\" >");
		            _xhtml_out.print("<td>");
		            _xhtml_out.println("<a href=\"http://"+NetworkManager.getLocalAddress()+"/resources/application.wadl\" target=\"_blank\">WBSAirback WADL</a>");
		            _xhtml_out.print("</td>");
		            _xhtml_out.print("</tr>");
		            _xhtml_out.print("<tr>");
		            _xhtml_out.print("<td>");
		            _xhtml_out.println("<a href=\"http://"+NetworkManager.getLocalAddress()+"/resources/application.wadl/xsd0.xsd\" target=\"_blank\">WBSAirback Xsd</a>");
		            _xhtml_out.print("</td>");
		            _xhtml_out.print("</tr>");
		            _xhtml_out.println("</table>");
		            _xhtml_out.println("<br/>");
		            _xhtml_out.println("</fieldset>");
		            _xhtml_out.println("<div class=\"clear\"/></div>");
		            _xhtml_out.println("</div>");
		            
					_xhtml_out.println("<div class=\"window\">");
					_xhtml_out.println("<h2>");
					_xhtml_out.print(getLanguageMessage("wsdl.services.list"));
		            _xhtml_out.println("</h2>");
		            _xhtml_out.print("<br/>");
		            _xhtml_out.println("<fieldset>");
		            _xhtml_out.println("<table>");
		            int _offset=1;
		            if(!_services.isEmpty()) {
		            	_xhtml_out.println("<tr>");
		                _xhtml_out.print("<td class=\"title\">");
		                _xhtml_out.print(getLanguageMessage("wsdl.services.name"));
		                _xhtml_out.println("</td>");
		                _xhtml_out.println("</tr>");
		                for(String _service : _services) {
		                	_xhtml_out.print("<tr");
		                	if(_offset % 2 == 0) {
		                		_xhtml_out.print(" class=\"highlight\"");
		                	}
		                	_xhtml_out.println(">");
		                	_xhtml_out.print("<td>");
		                	_xhtml_out.print("<a href=\"/admin/WebServices?type="+GET_SERVICE+"&service=");
		                	_xhtml_out.print(_service);
		                	_xhtml_out.print("\" target=\"_blank\">");
		                	_xhtml_out.print(_service);
		                	_xhtml_out.print("</a>");
							_xhtml_out.println("</td>");
							_xhtml_out.println("</tr>");
							_offset++;
		                }
		            } else {
		            	_xhtml_out.println("<tr>");
		            	_xhtml_out.print(getLanguageMessage("wsdl.services.empty"));
		            	_xhtml_out.println("</tr>");
		            }
		            _xhtml_out.println("</table>");
		            _xhtml_out.println("<br/>");
		            _xhtml_out.println("</fieldset>");
			    	_xhtml_out.println("<div class=\"clear\"/></div>");
			    	_xhtml_out.print("</div>");
	    		}
	    		break;
			}

	    } catch(Exception _ex) {
			writeDocumentError(_ex.getMessage());
	    } finally {
	    	if (this.type != GET_SERVICE)
	    		writeDocumentFooter();
	    }
	}
	
	public static List<String> getServicesList() throws Exception {
		List<String> services = new ArrayList<String>();
		String output = Command.systemCommand("ls "+WBSAirbackConfiguration.getDirectoryWsdl());
		StringTokenizer _st = new StringTokenizer(output, "\n");
		while (_st.hasMoreTokens()) {
			String serv = _st.nextToken();
			if (serv.endsWith("wsdl") && serv.contains("Rs"))
				services.add(serv);
		}
		return services;
	}
	
	public static String getService(String name) throws Exception {
		String output = Command.systemCommand("cat "+WBSAirbackConfiguration.getDirectoryWsdl()+"/"+name);
		return output;
	}

}
