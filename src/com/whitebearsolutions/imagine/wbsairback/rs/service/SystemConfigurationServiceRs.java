package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.TomcatConfiguration;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.ServiceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.SystemConfigurationRs;
import com.whitebearsolutions.imagine.wbsairback.util.Watchdog;
import com.whitebearsolutions.mail.Mail;
import com.whitebearsolutions.util.Configuration;

@Path("/system/configuration")
public class SystemConfigurationServiceRs extends WbsImagineServiceRs  {

	GeneralSystemConfiguration sc = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			sc = new GeneralSystemConfiguration();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Devuelve La configuracion actual del sistema
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getConfiguration() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			SystemConfigurationRs config = SystemConfigurationRs.getObject(sc);
			
			response.setSystemConfiguration(config);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("{a:restart|reboot}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response rebootSystem() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ServiceManager.restartSystem();
			response.setSuccess(getLanguageMessage("system.general.info_restart"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("shutdown")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getClientsPassword() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ServiceManager.shutdownSystem();
			response.setSuccess(getLanguageMessage("system.general.info_shutdown"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	
	/**
	 * Establece la configuracion del sistema
	 * @param config
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setSystemConfiguration(SystemConfigurationRs config ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			StringBuilder reply = new StringBuilder();
			
			if(config.getPassword() != null && !config.getPassword().isEmpty()) {
				sc.setRootPassword(config.getPassword());
				reply.append(getLanguageMessage("common.login.password"));
			}
			
			if(config.getMail() != null && !config.getMail().isEmpty()) {
				if(!config.getMail().matches("[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
					throw new Exception(getLanguageMessage("common.message.no_cc"));
				}
				
				sc.setBaculaMailAccount(config.getMail(), config.getBaculaMailLevel());
				
				if(reply.length() > 0) {
					reply.append(getLanguageMessage("system.general.and_config_email"));
    			} else {
    				reply.append(getLanguageMessage("system.general.config_email"));
    			}
			} else {
				sc.setBaculaMailAccount(null, config.getBaculaMailLevel());
			}
			
			if(config.getMailFrom() != null && !config.getMailFrom().isEmpty()) {
				if(!config.getMailFrom().matches("[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
					throw new Exception(getLanguageMessage("common.message.no_cc"));
				}
				
				sc.setMailFromAccount(config.getMailFrom());
				
				if(reply.length() > 0) {
					reply.append(getLanguageMessage("system.general.and_config_email_from"));
    			} else {
    				reply.append(getLanguageMessage("system.general.config_email_from"));
    			}
			} else {
				sc.setMailFromAccount(null);
			}
			
			if(config.getReportMail() != null && !config.getReportMail().isEmpty()) {
				if(!config.getReportMail().matches("[a-zA-Z0-9._+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
					throw new Exception(getLanguageMessage("common.message.no_cc"));
				}
				
				sc.setMailReportAccount(config.getReportMail());
				
				if(reply.length() > 0) {
					reply.append(getLanguageMessage("system.general.and_config_email_report"));
    			} else {
    				reply.append(getLanguageMessage("system.general.config_email_report"));
    			}
			} else {
				sc.setMailReportAccount(null);
			}
			
			if(config.getMailHost() != null && !config.getMailHost().isEmpty()) {
				sc.setMailServer(config.getMailHost());
			} else {
				sc.setMailServer(null);
			}
			
			if(config.getSnmptraphost() != null && !config.getSnmptraphost().isEmpty()) {
				String version = "2";
				if (config.getSnmptrapversion() != null)
					version = config.getSnmptrapversion().toString();
				sc.setSnmpTrapHost(config.getSnmptraphost(), version);
				
				if(reply.length() > 0) {
					reply.append(getLanguageMessage("system.general.and_config_snmp"));
    			} else {
    				reply.append(getLanguageMessage("system.general.config_snmp"));
    			}
			} else {
				sc.setSnmpTrapHost(null, null);
			}
			
			if(config.getReporthour() != null && !config.getReporthour().isEmpty()) {
				try {
    				sc.setReportHour(Integer.parseInt(config.getReporthour()));
    			} catch(NumberFormatException _ex) {}
			} else {
				sc.setReportHour(-1);
			}
			
			this.sessionManager.reloadConfiguration();
			
			TomcatConfiguration _tc = new TomcatConfiguration(this.sessionManager.getConfiguration());
			if(config.getHttps() != null && true == config.getHttps().booleanValue()) {
				if(!TomcatConfiguration.checkHTTPS()) {
					_tc.setHTTPS(true);
					if(config.getHttpsCertificate() != null && !config.getHttpsCertificate().isEmpty()) {
						if(config.getHttpsPassword() == null || config.getHttpsPassword().isEmpty()) {
    						throw new Exception(getLanguageMessage("common.message.exception"));
    					}
						byte[] _data = config.getHttpsCertificate().getBytes("UTF-8");
    					if(_data != null && _data.length > 0) {
    						_tc.setPKCS12(_data, config.getHttpsPassword().toCharArray());
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
			
			response.setSuccess(reply.toString());
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el estado de todos los servicios del sistema
	 * @return
	 */
	@Path("service")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getServicesState() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<ServiceRs> services = new ArrayList<ServiceRs>();
			
			ServiceRs baculaDir = new ServiceRs();
			baculaDir.setName("BACKUP-DIRECTOR");
	    	if(ServiceManager.isRunning(ServiceManager.BACULA_DIR))
	    		baculaDir.setState(getLanguageMessage("common.message.started"));
            else
            	baculaDir.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(baculaDir);
	    	
	    	ServiceRs baculaSd = new ServiceRs();
	    	baculaSd.setName("BACKUP-STORAGE");
	    	if(ServiceManager.isRunning(ServiceManager.BACULA_SD))
	    		baculaSd.setState(getLanguageMessage("common.message.started"));
            else
            	baculaSd.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(baculaSd);
	    	
	    	ServiceRs baculaFd = new ServiceRs();
	    	baculaFd.setName("BACKUP-FILE");
	    	if(ServiceManager.isRunning(ServiceManager.BACULA_FD))
	    		baculaFd.setState(getLanguageMessage("common.message.started"));
            else
            	baculaFd.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(baculaFd);
	    	
	    	ServiceRs postgre = new ServiceRs();
	    	postgre.setName("BACKUP-DATABASE");
	    	if(ServiceManager.isRunning(ServiceManager.POSTGRES))
	    		postgre.setState(getLanguageMessage("common.message.started"));
            else
            	postgre.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(postgre);
	    	
	    	ServiceRs iscsi = new ServiceRs();
	    	iscsi.setName("ISCSI");
	    	if(ServiceManager.isRunning(ServiceManager.ISCSI_TARGET))
	    		iscsi.setState(getLanguageMessage("common.message.started"));
            else
            	iscsi.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(iscsi);
	    	
	    	ServiceRs nfs = new ServiceRs();
	    	nfs.setName("NFS");
	    	if(ServiceManager.isRunning(ServiceManager.NFS))
	    		nfs.setState(getLanguageMessage("common.message.started"));
            else
            	nfs.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(nfs);
	    	
	    	ServiceRs cifs = new ServiceRs();
	    	cifs.setName("CIFS");
	    	if(ServiceManager.isRunning(ServiceManager.SAMBA))
	    		cifs.setState(getLanguageMessage("common.message.started"));
            else
            	cifs.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(cifs);
	    	
	    	ServiceRs ftp = new ServiceRs();
	    	ftp.setName("FTP");
	    	if(ServiceManager.isRunning(ServiceManager.FTP))
	    		ftp.setState(getLanguageMessage("common.message.started"));
            else
            	ftp.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(ftp);
	    	
	    	ServiceRs watchdog = new ServiceRs();
	    	watchdog.setName("WATCHDOG");
	    	if(ServiceManager.isRunning(ServiceManager.WATCHDOG))
	    		watchdog.setState(getLanguageMessage("common.message.started"));
            else
            	watchdog.setState(getLanguageMessage("common.message.stopped"));
	    	services.add(watchdog);
	    	
			response.setServices(services);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el estado de un servicio concreto del sistema a partir de su nombre
	 * @param nameService
	 * @return
	 */
	@Path("service/{nameService}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getServiceState(@PathParam("nameService") String nameService) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String name = nameService.toUpperCase();
			ServiceRs service = new ServiceRs();
			service.setName(name);
			if (name.equals("BACKUP-DIRECTOR")) {
		    	if(ServiceManager.isRunning(ServiceManager.BACULA_DIR))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("BACKUP-STORAGE")) {
		    	if(ServiceManager.isRunning(ServiceManager.BACULA_SD))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("BACKUP-FILE")) {
		    	if(ServiceManager.isRunning(ServiceManager.BACULA_FD))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("BACKUP-DATABASE")) {
		    	if(ServiceManager.isRunning(ServiceManager.POSTGRES))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("ISCSI")) {
		    	if(ServiceManager.isRunning(ServiceManager.ISCSI_TARGET))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("NFS")) {
		    	if(ServiceManager.isRunning(ServiceManager.NFS))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("CIFS")) {
		    	if(ServiceManager.isRunning(ServiceManager.SAMBA))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("FTP")) {
		    	if(ServiceManager.isRunning(ServiceManager.FTP))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else if (name.equals("WATCHDOG")) {
		    	if(ServiceManager.isRunning(ServiceManager.WATCHDOG))
		    		service.setState(getLanguageMessage("common.message.started"));
	            else
	            	service.setState(getLanguageMessage("common.message.stopped"));
			} else {
				throw new Exception(getLanguageMessage("system.configuration.exception.service.notreconized")+" : "+"BACKUP-DIRECTOR,BACKUP-STORAGE,BACKUP-FILE,BACKUP-DATABASE,ISCSI,NFS,CIFS,FTP,WATCHDOG");
			}
			
			response.setService(service);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Inicia un servicio a partir de su nombre
	 * @param nameService
	 * @return
	 */
	@Path("service/{nameService}/start")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response serviceStart(@PathParam("nameService") String nameService) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String name = nameService.toUpperCase();
			if (name.equals("BACKUP-DIRECTOR")) {
		    	if(!ServiceManager.isRunning(ServiceManager.BACULA_DIR))
		    		ServiceManager.start(ServiceManager.BACULA_DIR);
			} else if (name.equals("BACKUP-STORAGE")) {
		    	if(!ServiceManager.isRunning(ServiceManager.BACULA_SD))
		    		ServiceManager.start(ServiceManager.BACULA_SD);
			} else if (name.equals("BACKUP-FILE")) {
		    	if(!ServiceManager.isRunning(ServiceManager.BACULA_FD))
		    		ServiceManager.start(ServiceManager.BACULA_FD);
			} else if (name.equals("BACKUP-DATABASE")) {
		    	if(!ServiceManager.isRunning(ServiceManager.POSTGRES))
		    		ServiceManager.start(ServiceManager.POSTGRES);
			} else if (name.equals("ISCSI")) {
		    	if(!ServiceManager.isRunning(ServiceManager.ISCSI_TARGET))
		    		ServiceManager.start(ServiceManager.ISCSI_TARGET);
			} else if (name.equals("NFS")) {
		    	if(!ServiceManager.isRunning(ServiceManager.NFS))
		    		ServiceManager.start(ServiceManager.NFS);
			} else if (name.equals("CIFS")) {
		    	if(!ServiceManager.isRunning(ServiceManager.SAMBA))
		    		ServiceManager.start(ServiceManager.SAMBA);
			} else if (name.equals("FTP")) {
		    	if(!ServiceManager.isRunning(ServiceManager.FTP))
		    		ServiceManager.start(ServiceManager.FTP);
			} else if (name.equals("WATCHDOG")) {
		    	if(!ServiceManager.isRunning(ServiceManager.WATCHDOG))
		    		ServiceManager.start(ServiceManager.WATCHDOG);
			} else {
				throw new Exception(getLanguageMessage("system.configuration.exception.service.notreconized")+" : "+"BACKUP-DIRECTOR,BACKUP-STORAGE,BACKUP-FILE,BACKUP-DATABASE,ISCSI,NFS,CIFS,FTP,WATCHDOG");
			}
			
			response.setSuccess(getLanguageMessage("system.general.info_started"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Inicia un servicio a partir de su nombre
	 * @param nameService
	 * @return
	 */
	@Path("service/{nameService}/stop")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response serviceStop(@PathParam("nameService") String nameService) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String name = nameService.toUpperCase();
			if (name.equals("BACKUP-DIRECTOR")) {
		    	if(ServiceManager.isRunning(ServiceManager.BACULA_DIR))
		    		ServiceManager.stop(ServiceManager.BACULA_DIR);
			} else if (name.equals("BACKUP-STORAGE")) {
		    	if(ServiceManager.isRunning(ServiceManager.BACULA_SD))
		    		ServiceManager.stop(ServiceManager.BACULA_SD);
			} else if (name.equals("BACKUP-FILE")) {
		    	if(ServiceManager.isRunning(ServiceManager.BACULA_FD))
		    		ServiceManager.stop(ServiceManager.BACULA_FD);
			} else if (name.equals("BACKUP-DATABASE")) {
		    	if(ServiceManager.isRunning(ServiceManager.POSTGRES))
		    		ServiceManager.stop(ServiceManager.POSTGRES);
			} else if (name.equals("ISCSI")) {
		    	if(ServiceManager.isRunning(ServiceManager.ISCSI_TARGET))
		    		ServiceManager.stop(ServiceManager.ISCSI_TARGET);
			} else if (name.equals("NFS")) {
		    	if(ServiceManager.isRunning(ServiceManager.NFS))
		    		ServiceManager.stop(ServiceManager.NFS);
			} else if (name.equals("CIFS")) {
		    	if(ServiceManager.isRunning(ServiceManager.SAMBA))
		    		ServiceManager.stop(ServiceManager.SAMBA);
			} else if (name.equals("FTP")) {
		    	if(ServiceManager.isRunning(ServiceManager.FTP))
		    		ServiceManager.stop(ServiceManager.FTP);
			} else if (name.equals("WATCHDOG")) {
		    	if(ServiceManager.isRunning(ServiceManager.WATCHDOG))
		    		ServiceManager.stop(ServiceManager.WATCHDOG);
			} else {
				throw new Exception(getLanguageMessage("system.configuration.exception.service.notreconized")+" : "+"BACKUP-DIRECTOR,BACKUP-STORAGE,BACKUP-FILE,BACKUP-DATABASE,ISCSI,NFS,CIFS,FTP,WATCHDOG");
			}
			
			response.setSuccess(getLanguageMessage("system.general.info_stopped"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	@Path("sendemail")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response sendTestingEmail() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Configuration _tc = new Configuration();
			_tc.setProperty("mail.host", "localhost");
			Mail _m = new Mail(_tc);
			String _account = sc.getBaculaMailAccount();
			if(sc.getBaculaMailAccount() != null) {
				_m.addTo(_account);
			} else {
				throw new Exception(getLanguageMessage("system.general.no_email.configured"));
			}
			_m.setFrom("WBSAIRBACK", Watchdog.SUPPORT_MAIL);
			_m.setSubject("test email");
			_m.setHTML("<h3>"+getLanguageMessage("system.general.send.title")+"</h3> <p>"+getLanguageMessage("system.general.send.body")+"</p> <p>"+getLanguageMessage("system.general.send.final")+"</p> <br /> <b>WBSgo</b> Support team: <i>soporte@whitebearsolutions.com</i><br />");
			_m.send();
			response.setSuccess(getLanguageMessage("system.general.email_sended"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
