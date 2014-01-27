package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ScriptProcessRs;

@Path("/advanced/script")
public class AdvancedScriptProcessServiceRs extends WbsImagineServiceRs{
	
	private ScriptProcessManager spm = null;
	private SysAppsInventoryManager saim = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general, gestiona sesión y autenticación
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			spm = new ScriptProcessManager();
			saim = new SysAppsInventoryManager();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	/**
	 * Listado de scripts
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getScripts() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Map<String, Object>> maps = spm.listScript();
			List<ScriptProcessRs> scripts = ScriptProcessRs.listMapToObject(maps);
			response.setScriptsProcesses(scripts);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un script
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getScript(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Object> map = spm.getScript(name);
			if (map == null || map.isEmpty())
				throw new Exception("advanced.script.notexists"); 
			ScriptProcessRs script = ScriptProcessRs.mapToObject(map);
			response.setScriptProcess(script);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un script
	 * @param script
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewScriptProcess(ScriptProcessRs script) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (spm.getScript(script.getName()) != null)
				throw new Exception("advanced.script.duplicated");
			
			spm.saveScript(script.getName(), ScriptProcessRs.objectToMap(script), saim);
			uriCreated=script.getName();
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
	 * Edita un script
	 * @param script
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editScriptProcess(ScriptProcessRs script) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (spm.getScript(script.getName()) == null)
				throw new Exception("advanced.script.notexists");
			
			spm.saveScript(script.getName(), ScriptProcessRs.objectToMap(script), saim);
			uriCreated=script.getName();
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
	 * Elimina un script
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteScriptProcess(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (spm.getScript(name) == null)
				throw new Exception("advanced.script.notexists");
			
			spm.deleteScript(name, saim);
			response.setSuccess(this.getLanguageMessage("advanced.script.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
