package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.ProxyRs;

@Path("/system/proxy")
public class SystemProxyServiceRs extends WbsImagineServiceRs {

	
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
	 * Devuelve el proxy actual del sistema
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getProxy() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> map = NetworkManager.getProxyServer();
			if (map == null || map.isEmpty())
				throw new Exception("system.proxy.noproxy");
			ProxyRs proxy = ProxyRs.mapToObject(map);
			
			response.setProxy(proxy);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Establece un proxy para el sistema
	 * @param proxy
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setProxy(ProxyRs proxy ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			int port = 80;
			if (proxy.getPort() != null)
				port = proxy.getPort();
			
			NetworkManager.setProxyServer(proxy.getServer(), port, proxy.getUser(), proxy.getPassword());
			
			response.setSuccess(getLanguageMessage("system.network.proxy.message.saved"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina el proxy del sistema
	 * @return
	 */
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeProxy() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			NetworkManager.removeProxyServer();
			response.setSuccess(getLanguageMessage("system.network.proxy.message.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
