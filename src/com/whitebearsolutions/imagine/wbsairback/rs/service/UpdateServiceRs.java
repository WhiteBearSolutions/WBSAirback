package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.rs.model.subscription.PackageRs;
import com.whitebearsolutions.imagine.wbsairback.service.UpdateManager;

@Path("/update")
public class UpdateServiceRs extends WbsImagineServiceRs {
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			String _test_result = "OK";
			_test_result = UpdateManager.checkSignature();
        	if(!_test_result.equals("OK")) {
				throw new Exception(_test_result);
			}
			
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Obtiene el listado de paquetes actualizables del sistema
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getLicenses() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
	
			Map<String, List<String>> listPackages = UpdateManager.getUpgradeData();
			List<PackageRs> packages = PackageRs.listMapToObject(listPackages);
			
			response.setPackages(packages);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de paquetes actualizables del sistema
	 * @return
	 */
	@Path("/upgrade")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response update() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
	
			UpdateManager.upgrade();
			
			response.setSuccess(getLanguageMessage("update.message.update_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
