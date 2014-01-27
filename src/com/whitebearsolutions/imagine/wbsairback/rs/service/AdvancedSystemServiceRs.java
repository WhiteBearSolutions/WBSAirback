package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.SysAppsInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;

@Path("/advanced/inventory/system")
public class AdvancedSystemServiceRs extends WbsImagineServiceRs{
	
	private SysAppsInventoryManager saim = null;
	private ScriptProcessManager spm = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general, gestiona sesión y autenticación
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			saim = new SysAppsInventoryManager();
			spm = new ScriptProcessManager();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	/**
	 * Listado de sistemas
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSystems() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<SystemRs> system = saim.listSystem();
			response.setSystems(system);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un sistema
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSystem(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			SystemRs system = saim.getSystem(name);
			if (system == null)
				throw new Exception("advanced.system.notexists");
			response.setSystem(system);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un sistema
	 * @param system
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewSystem(SystemRs system) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (saim.getSystem(system.getName()) != null)
				throw new Exception("advanced.system.duplicated");
			
			saim.saveSystem(system.getName(), system.getDescription(), false);
			uriCreated=system.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Edita un sistema
	 * @param system
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editSystem(SystemRs system) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (saim.getSystem(system.getName()) == null)
				throw new Exception("advanced.system.notexists");
			
			saim.saveSystem(system.getName(), system.getDescription(), true);
			uriCreated=system.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un sistema
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteSystem(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (saim.getSystem(name) == null)
				throw new Exception("advanced.system.notexists");
			
			saim.deleteSystem(name, spm);
			response.setSuccess(this.getLanguageMessage("advanced.system.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
