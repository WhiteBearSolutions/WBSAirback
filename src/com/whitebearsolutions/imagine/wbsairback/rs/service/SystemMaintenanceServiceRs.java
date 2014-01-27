package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.MultiPathManager;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.MaintenanceConfigurationRs;

@Path("/system/maintenance")
public class SystemMaintenanceServiceRs extends WbsImagineServiceRs  {

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
	 * Devuelve La configuracion actual de mantenimiento del sistema
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getMaintenanceConfiguration() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			MaintenanceConfigurationRs config = MaintenanceConfigurationRs.getObject(sc);
			
			response.setMaintenanceConfiguration(config);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve la configuracion de base de datos recomendada
	 * @return
	 */
	@Path("/db/recomended")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getMaintenanceConfigurationRecomended() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			MaintenanceConfigurationRs config = new MaintenanceConfigurationRs();
			config.setDbCache(GeneralSystemConfiguration.getDataBaseRecommendedCache());
			config.setDbMaxConnections(500);
			config.setDbSharedBuffers(GeneralSystemConfiguration.getDataBaseRecommendedSharedBuffer());
			
			response.setMaintenanceConfiguration(config);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Realiza una optimización/vacuum de la bd
	 * @return
	 */
	@Path("/db/vacuum")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response vacuumDb() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			DBConnection _db = new DBConnectionManager(this.sessionManager.getConfiguration()).getConnection();
			_db.query("VACUUM ANALYZE file");
			_db.query("VACUUM ANALYZE path");
			_db.query("VACUUM ANALYZE filename");
			_db.query("VACUUM ANALYZE job");
			
			response.setSuccess(getLanguageMessage("system.maintenance.vacuum.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Importa la configuración del sistema desde un share
	 * @param share
	 * @return
	 */
	@Path("/import/share/{share}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response importConfiguration(@PathParam("share") String share) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> _shares = ShareManager.getExternalShareNames();
			if (_shares == null || !_shares.contains(share))
				throw new Exception("system.maintenance.share.notfound");
			
			sc.importConfigutation(share);
			
			response.setSuccess(getLanguageMessage("system.manteinance.import_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Exporta la configuración del sistema a un share
	 * @param share
	 * @return
	 */
	@Path("/export/share/{share}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response exportConfiguration(@PathParam("share") String share) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> _shares = ShareManager.getExternalShareNames();
			if (_shares == null || !_shares.contains(share))
				throw new Exception("system.maintenance.share.notfound");
			
			sc.exportConfigutation(share);
			
			response.setSuccess(getLanguageMessage("system.manteinance.import_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/password/{a:storage|sd}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageDirectorPassword(@PathParam("share") String share) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String _sd_password = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Director", "airback-dir", "Password");
			
			response.setStoragePassword(_sd_password);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/password/{a:client|fd}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getClientsPassword(@PathParam("share") String share) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String _fd_password = BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-fd.conf", "Director", "airback-dir", "Password");
			
			response.setClientPassword(_fd_password);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	
	/**
	 * Establece la configuracion de mantenimiento del sistema
	 * @param config
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setMaintenanceConfiguration(MaintenanceConfigurationRs config ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			int max_connections = 500, shared_buffers = -1, cache = -1;
			if(config.getDbMaxConnections() != null) {
				max_connections = config.getDbMaxConnections();
			}
			if(config.getDbSharedBuffers() != null) {
				shared_buffers = config.getDbSharedBuffers();
			}
			if(config.getDbCache() != null) {
				cache = config.getDbCache();
			}
			if(config.getExportShare() != null && config.getExportHour() != null && !config.getExportShare().isEmpty()) {
				List<String> _shares = ShareManager.getExternalShareNames();
				if (_shares == null || !_shares.contains(config.getExportShare()))
					throw new Exception("system.maintenance.share.notfound");
				
				sc.setExportHour(config.getExportHour());
				sc.setExportShare(config.getExportShare());
			} else {
				sc.setExportHour(-1);
				sc.setExportShare(null);
			}
			if(config.getExportRetention() != null) {
				sc.setExportRetention(config.getExportRetention());
			} else{
				sc.setExportRetention(-1);
			}
			
			if(config.getMultipath() != null && config.getMultipath().booleanValue() == true) {
				if (!MultiPathManager.isMultipathEnabled())
					MultiPathManager.enableMultipath();
			}
			else {
				if (MultiPathManager.isMultipathEnabled())
					MultiPathManager.disableMultipath();
			}
			
			GeneralSystemConfiguration.setDataBaseConfiguration(max_connections, shared_buffers, cache);
			
			response.setSuccess(getLanguageMessage("system.manteinance.message.configuration_saved"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
