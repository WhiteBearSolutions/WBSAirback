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

import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.StepRs;

@Path("/advanced/step")
public class AdvancedStepServiceRs extends WbsImagineServiceRs{
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general, gestiona sesión y autenticación
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
	
	private String checkType (StepRs step) throws Exception {
		String type = null;
		if (step.getType() != null && !step.getType().isEmpty()) {
			if (step.getType().trim().toLowerCase().contains("system"))
				type = StepManager.TYPE_STEP_SCRIPT_SYSTEM;
			else if (step.getType().trim().toLowerCase().contains("app"))
				type = StepManager.TYPE_STEP_SCRIPT_APP;
			else if (step.getType().trim().toLowerCase().contains("backup"))
				type = StepManager.TYPE_STEP_BACKUP;
			else if (step.getType().trim().toLowerCase().contains("storage") || step.getType().trim().toLowerCase().contains("advanced"))
				type = StepManager.TYPE_STEP_ADVANCED_STORAGE;
			else
				throw new Exception("advanced.step.type.notsupported");
		} else
			throw new Exception("advanced.step.type.notsupported");
		return type;
	}
	
	/**
	 * Listado de pasos
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSteps() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = StepManager.listSteps();
			List<StepRs> steps = StepRs.listMapToObject(maps);
			response.setSteps(steps);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Lista los pasos por tipo
	 * @param type
	 * @return
	 */
	@Path("/type/{type}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStepsByType(@PathParam("type") String type) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = StepManager.listSteps(type);
			List<StepRs> steps = StepRs.listMapToObject(maps);
			response.setSteps(steps);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un paso
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStep(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> map = StepManager.getStep(name);
			if (map == null || map.isEmpty())
				throw new Exception("advanced.step.notexists");
			
			StepRs step = StepRs.mapToObject(map);
			response.setStep(step);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un paso
	 * @param step
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewStep(StepRs step) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (StepManager.existsStep(step.getName()))
				throw new Exception("advanced.step.duplicated");
			
			String type = checkType(step);
			
			StepManager.saveStep(step.getName(), type, false);
			uriCreated=step.getName();
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
	 * Edita un paso
	 * @param step
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editStep(StepRs step) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!StepManager.existsStep(step.getName()))
				throw new Exception("advanced.step.notexists");
			
			String type = checkType(step);
			
			StepManager.saveStep(step.getName(), type, true);
			uriCreated=step.getName();
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
	 * Elimina un paso
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteStep(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!StepManager.existsStep(name))
				throw new Exception("advanced.step.notexists");
			
			StepManager.removeStep(name);
			response.setSuccess(this.getLanguageMessage("advanced.step.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
