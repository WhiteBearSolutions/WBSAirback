package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.ArrayList;
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
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ApplicationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;

@Path("/advanced/inventory/application")
public class AdvancedApplicationServiceRs extends WbsImagineServiceRs{
	
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
	
	private List<String> checkSystems(ApplicationRs app) throws Exception {
		if (app.getSystems() == null || app.getSystems().isEmpty())
			throw new Exception ("advanced.application.nosystems");
		List<String> nameSystems = new ArrayList<String>();
		for (SystemRs sys : app.getSystems()) {
			if (sys.getName() != null && !sys.getName().isEmpty())
				nameSystems.add(sys.getName());
		}
		
		return nameSystems;
	}
	
	/**
	 * Listado de aplicaciones
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getApplications() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<ApplicationRs> application = saim.listApps();
			response.setApplications(application);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un aplicacion
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getApplication(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ApplicationRs application = saim.getApplication(name);
			if (application == null)
				throw new Exception("advanced.application.notexists");
			response.setApplication(application);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un aplicacion
	 * @param application
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewApplication(ApplicationRs application) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (saim.getApplication(application.getName()) != null)
				throw new Exception("advanced.application.duplicated");
			
			List<String> systems = checkSystems(application);
			
			saim.saveApps(application.getName(), application.getDescription(), systems, false);
			uriCreated=application.getName();
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
	 * Edita un aplicacion
	 * @param application
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editApplication(ApplicationRs application) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (saim.getApplication(application.getName()) == null)
				throw new Exception("advanced.application.notexists");
			
			List<String> systems = checkSystems(application);
			
			saim.saveApps(application.getName(), application.getDescription(), systems, true);
			uriCreated=application.getName();
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
	 * Elimina un aplicacion
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteApplication(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (saim.getApplication(name) == null)
				throw new Exception("advanced.application.notexists");
			
			saim.deleteApplication(name, spm);
			response.setSuccess(this.getLanguageMessage("advanced.application.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
