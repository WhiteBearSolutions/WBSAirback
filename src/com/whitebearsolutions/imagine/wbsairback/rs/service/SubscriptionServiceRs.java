package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;

@Path("/subscription")
public class SubscriptionServiceRs extends WbsImagineServiceRs {
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
		
	
	/**
	 * Obtiene el numero de serie del producto
	 * @return
	 */
	@Path("/serial")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSystemSerialNumber() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String serial = LicenseManager.getSystemSerial();
			if (serial == null)
				serial = "";
			
			response.setSerialNumber(serial);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}

}
