package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.TemplateJobRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.TemplateStepRs;

@Path("/advanced/templatejob")
public class AdvancedTemplateJobServiceRs extends WbsImagineServiceRs{
	
	
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
	
	/**
	 * Listado de plantillas
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getTemplateJobs() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, Object>> maps = TemplateJobManager.listTemplateJobs();
			List<TemplateJobRs> templateJobs = TemplateJobRs.listMapToObject(maps);
			response.setTemplateJobs(templateJobs);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un plantilla
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getTemplateJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Object> map = TemplateJobManager.getTemplateJob(name);
			if (map == null || map.isEmpty())
				throw new Exception(getLanguageMessage("advanced.templateJob.notexists"));
			TemplateJobRs templateJob = TemplateJobRs.mapToObject(map);
			response.setTemplateJob(templateJob);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un plantilla
	 * @param templateJob
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewTemplateJob(TemplateJobRs templateJob) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (TemplateJobManager.existsTemplateJob(templateJob.getName()))
				throw new Exception(getLanguageMessage("advanced.templateJob.duplicated"));
			
			TemplateJobManager.saveTemplateJob(templateJob.getName(), false);
			
			if (templateJob.getSteps() == null || templateJob.getSteps().isEmpty())
				throw new Exception(getLanguageMessage("advanced.templateJob.nosteps"));
			
			for (TemplateStepRs step : templateJob.getSteps()) {
				if (!step.getType().equals(StepManager.TYPE_STEP_BACKUP) && (step.getData() == null || step.getData().isEmpty()) )
					throw new Exception(getLanguageMessage("advanced.templatejob.step.emptydata"));
				TemplateJobManager.saveTemplateJobStep(templateJob.getName(), step.getType(), step.getName(), step.getData(), this.getConfig());
			}
			
			uriCreated=templateJob.getName();
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
	 * Edita un plantilla
	 * @param templateJob
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editTemplateJob(TemplateJobRs templateJob) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!TemplateJobManager.existsTemplateJob(templateJob.getName()))
				throw new Exception(getLanguageMessage("advanced.templateJob.notexists"));
			
			TemplateJobManager.saveTemplateJob(templateJob.getName(), true);
			
			if (templateJob.getSteps() == null || templateJob.getSteps().isEmpty())
				throw new Exception(getLanguageMessage("advanced.templateJob.nosteps"));
			
			Set<String> oldSteps = new HashSet<String>();
			Map<String, Object> mapTemplate = TemplateJobManager.getTemplateJob(templateJob.getName());
			TemplateJobRs oldTemplate = TemplateJobRs.mapToObject(mapTemplate);
			if (oldTemplate.getSteps() != null) {
				for (TemplateStepRs step : oldTemplate.getSteps()) {
					oldSteps.add(step.getName());
				}
			}
			
			for (TemplateStepRs step : templateJob.getSteps()) {
				if (!TemplateJobManager.existsTemplateJobStep(templateJob.getName(),  step.getName(), step.getData())) {
					TemplateJobManager.saveTemplateJobStep(templateJob.getName(), step.getType(), step.getName(), step.getData(), this.getConfig());
				} else
					oldSteps.remove(step.getName());
			}
			
			for (String step : oldSteps) {
				TemplateJobManager.removeTemplateJobStep(templateJob.getName(), step, this.getConfig());
			}
			uriCreated=templateJob.getName();
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
	 * Elimina un plantilla
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteTemplateJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!TemplateJobManager.existsTemplateJob(name))
				throw new Exception(getLanguageMessage("advanced.templateJob.notexists"));
			
			TemplateJobManager.removeTemplateJob(name);
			response.setSuccess(this.getLanguageMessage("advanced.templateJob.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
