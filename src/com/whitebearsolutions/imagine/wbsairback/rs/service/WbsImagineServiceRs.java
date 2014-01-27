package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.SecurityManager;
import com.whitebearsolutions.imagine.wbsairback.SessionManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.PermissionsConfiguration;
import com.whitebearsolutions.imagine.wbsairback.i18n.WBSAirbackResourceBundle;
import com.whitebearsolutions.imagine.wbsairback.rs.exception.LicenseException;
import com.whitebearsolutions.imagine.wbsairback.rs.exception.NoPrivilegesException;
import com.whitebearsolutions.imagine.wbsairback.rs.exception.UnauthorizedException;
import com.whitebearsolutions.imagine.wbsairback.rs.model.core.AirbackResponseRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.core.AirbackRs;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.security.Base64;
import com.whitebearsolutions.util.Configuration;

public class WbsImagineServiceRs {
	protected AirbackRs airbackRs;
	protected AirbackResponseRs response;
	
	protected String user;
	protected Configuration config;
	protected LicenseManager licenseManager;
	protected SessionManager sessionManager;
	protected SecurityManager securityManager;
	protected PermissionsConfiguration permissions;
	
	protected ResourceBundle messagei18N;
	protected final String language = "en";
	
	@Context
	protected HttpServletRequest httpRequest;
	@Context
	protected HttpServletResponse httpResponse;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	protected Response init(List<String> category) {
		airbackRs = new AirbackRs();
		response = new AirbackResponseRs();
		
		// Check general permissions
		try {
			licenseManager = new LicenseManager();
			this.checkSecurity(category);
			return null;
		} catch (UnauthorizedException ex) {
			return Response.status(401).header("WWW-Authenticate", "BASIC realm=\"WBSAirback\"").build();
		} catch (NoPrivilegesException ex) {
			response.setError(getLanguageMessage("common.message.no_privilegios"));
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		} catch (LicenseException ex) {
			response.setError(getLanguageMessage("cdp.license.infoNeedLicence"));
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		} catch (Exception ex) {
			response.setError("Server Configuration Error: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	/**
	 * Obtiene un mensaje de un fichero xml según el lenguaje
	 * @param message
	 * @return
	 */
	public String getLanguageMessage(String message) {
		try {
			return this.messagei18N.getString(message);
		} catch(MissingResourceException _ex) {
			return message; 
		}
	}
	
	/**
	 * Realiza las comprobaciones oportunas de seguridad 
	 */
	private void checkSecurity(List<String> category) throws UnauthorizedException, NoPrivilegesException, Exception{
		this.sessionManager = new SessionManager(httpRequest);
		this.securityManager = this.sessionManager.getSecurityManager();
		this.permissions = new PermissionsConfiguration();
		if(this.sessionManager.isConfigured() && httpRequest.getParameter("language") != null) {
			try {
				Locale _l = new Locale(httpRequest.getParameter("language"));
				this.messagei18N = ResourceBundle.getBundle("wbsairback", _l, new WBSAirbackResourceBundle());
				this.sessionManager.loadObjectSession("language", this.messagei18N);
			} catch(Exception _ex) {
				System.out.println("WBSImagineServlet::Language: " + _ex.toString());
			}
		} else {
			if(this.sessionManager.hasObjectSession("language")) {
				this.messagei18N = (ResourceBundle) this.sessionManager.getObjectSession("language");
			} else {
				this.messagei18N = ResourceBundle.getBundle("wbsairback", this.httpRequest.getLocale(), new WBSAirbackResourceBundle());
				this.sessionManager.loadObjectSession("language", this.messagei18N);
			}
		}
		this.validateHTTPUser();
		this.validatePermissions(category);
		if(!this.sessionManager.hasObjectSession("version")) {
			this.sessionManager.loadObjectSession("version", GeneralSystemConfiguration.getVersion());
		}
		this.config = this.sessionManager.getConfiguration();
	}
	
	/**
	 * Valida si un usario tiene permisos o no para la operacion solicitada
	 * @param category
	 * @throws NoPrivilegesException
	 * @throws Exception
	 */
	private void validatePermissions(List<String> categories) throws NoPrivilegesException, Exception{
		String uri = this.httpRequest.getRequestURI();
		if (uri.contains("/resources/"))
			uri = uri.substring(uri.indexOf("/resources/")+"/resources/".length());
		
		if (!this.securityManager.isAdministrator()) {
			if (!permissions.isAllowed(this.securityManager.getUserRoles(), uri, null)) {
				throw new NoPrivilegesException();
			}
		} 
		
		String uriCheck = uri;
		if (uriCheck.contains("/"))
			uriCheck = uriCheck.substring(0, uriCheck.indexOf("/"));
		if (this.securityManager.isBackupFromCommunity(uriCheck, permissions)) {
			throw new Exception(getLanguageMessage("common.message.community.backup.section.unavaliable"));
		}
	}
	
	/**
	 * Validate httpUser
	 * @throws UnauthorizedException
	 * @throws Exception
	 */
	private void validateHTTPUser() throws UnauthorizedException, Exception {
	    try {
            String _auth = this.httpRequest.getHeader("Authorization");
            if(_auth != null) {
                if(_auth.toUpperCase().startsWith("BASIC ")) {
                
                	if(this.securityManager.isLogged())
                		return;
                	try {
                		String _password = _auth.substring(6);
                		_password = new String(Base64.decode(_password.toCharArray()));
	                 
	                 
	                	 this.user = _password.substring(0, _password.indexOf(':'));
		                _password = _password.substring(_password.indexOf(':') + 1);
	
		                if(this.user == null || this.user.isEmpty())
		                	throw new UnauthorizedException();
		                
		                this.securityManager.userLogin(this.user, _password);
		                this.securityManager.setLogin(this.user);
		                return;
	                 } catch (Exception ex) {
	                	 throw new UnauthorizedException();
	                 }
                } else {
                	throw new UnauthorizedException();
                }
            } else {
            	throw new UnauthorizedException();
            }
        } finally {
        	this.user = null;
        }
	}
	

	public AirbackRs getAirbackRs() {
		return airbackRs;
	}

	public void setAirbackRs(AirbackRs airbackRs) {
		this.airbackRs = airbackRs;
	}

	public AirbackResponseRs getResponse() {
		return response;
	}

	public void setResponse(AirbackResponseRs response) {
		this.response = response;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public ResourceBundle getMessagei18N() {
		return messagei18N;
	}

	public void setMessagei18N(ResourceBundle messagei18n) {
		messagei18N = messagei18n;
	}

	public String getLanguage() {
		return language;
	} 
	
	
}
