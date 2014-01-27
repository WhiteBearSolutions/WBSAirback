package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.HashMap;
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

import com.whitebearsolutions.imagine.wbsairback.advanced.RemoteStorageManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.AdvancedStorageRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ScriptItemRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.StepStorageRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.VariableRs;

@Path("/advanced/storage")
public class AdvancedStorageServiceRs extends WbsImagineServiceRs{
	
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
	
	private String getTypeStorage(AdvancedStorageRs storage) throws Exception {
		String typeStorage = RemoteStorageManager.STORAGE_TYPE_ISCSI;
		
		String tmp = storage.getTypeStorage();
		if (tmp != null && !tmp.isEmpty()) {
			tmp = tmp.toLowerCase().trim();
			if (tmp.contains("fibre") || tmp.contains("fc"))
				typeStorage = RemoteStorageManager.STORAGE_TYPE_FIBRE;
		}
		
		return typeStorage;
	}
	
	private Map<String, Map<String, String>> getVars(AdvancedStorageRs storage) throws Exception {
		Map<String, Map<String, String>> vars = new HashMap<String, Map<String, String>>();
		if (storage.getVariables() != null && !storage.getVariables().isEmpty()) {
			for (VariableRs v : storage.getVariables()) {
				Map<String, String> var = new HashMap<String, String>();
				var.put("name", v.getName());
				String description = "";
				if (v.getDescription() != null && !v.getDescription().isEmpty())
					description = v.getDescription(); 
				var.put("description", description);
				String pass = "false";
				if (v.getPassword() != null && v.getPassword().booleanValue() == true)
					pass = "true";
				var.put("password", pass);
				
				vars.put(v.getName(), var);
			}
		}
		return vars;
	}
	
	private void checkSteps(AdvancedStorageRs storage) throws Exception {
		if (storage.getSteps() != null && !storage.getSteps().isEmpty()) {
			for (StepStorageRs step : storage.getSteps()) {
				if (step.getScripts() == null || step.getScripts().isEmpty())
					throw new Exception(getLanguageMessage("advanced.remotestorage.step.noscripts"));				
			}
		} else
			throw new Exception(getLanguageMessage("advanced.remotestorage.step.nosteps"));
	}
	
	private void saveSteps(AdvancedStorageRs storage, boolean edit) throws Exception {
		for (StepStorageRs step : storage.getSteps()) {
			Map<Integer, String> scripts = new HashMap<Integer, String>();
			for (ScriptItemRs script : step.getScripts()) {
				scripts.put(script.getOrder(), script.getContent());
			}
			
			boolean mount = false;
			if (step.getMount() != null && step.getMount().booleanValue() == true)
				mount = true;
			
			RemoteStorageManager.saveRemoteStorageStep(storage.getName(), step.getName(), scripts, mount, edit);					
		}
	}
	
	/**
	 * Lista los advanced storages
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAdvancedStorages() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, Object>> listMap = RemoteStorageManager.listRemoteStorages();
			List<AdvancedStorageRs> storages = AdvancedStorageRs.listMapToObject(listMap);
			response.setAdvancedStorages(storages);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un advanced storage
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAdvancedStorage(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Object> map = RemoteStorageManager.getRemoteStorage(name);
			if (map == null || map.isEmpty())
				throw new Exception(getLanguageMessage("advanced.remotestorage.notexists"));
			AdvancedStorageRs storage = AdvancedStorageRs.mapToObject(map);
			response.setAdvancedStorage(storage);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	

	/**
	 * Añade un advanced storage
	 * @param storage
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewAdvancedStorage(AdvancedStorageRs storage) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (RemoteStorageManager.existsStorage(storage.getName()))
				throw new Exception(getLanguageMessage("advanced.remotestorage.duplicated"));
			
			String typeConnection = RemoteStorageManager.CONNECTION_TYPE_SSH;
			String typeStorage = getTypeStorage(storage);
			
			Map<String, Map<String, String>> vars = getVars(storage);
			checkSteps(storage);
			
			RemoteStorageManager.saveStorage(storage.getName(), typeConnection, typeStorage, vars, false);
			saveSteps(storage, false);
			uriCreated=storage.getName();
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
	 * Edita un advanced storage
	 * @param storage
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editAdvancedStorage(AdvancedStorageRs storage) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!RemoteStorageManager.existsStorage(storage.getName()))
				throw new Exception(getLanguageMessage("advanced.remotestorage.notexists"));
			
			String typeConnection = RemoteStorageManager.CONNECTION_TYPE_SSH;
			String typeStorage = getTypeStorage(storage);
			
			Map<String, Map<String, String>> vars = getVars(storage);
			checkSteps(storage);
			
			RemoteStorageManager.saveStorage(storage.getName(), typeConnection, typeStorage, vars, true);
			saveSteps(storage, true);
			
			uriCreated=storage.getName();
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
	 * Elimina un advanced storage
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteAdvancedStorage(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!RemoteStorageManager.existsStorage(name))
				throw new Exception(getLanguageMessage("advanced.remotestorage.notexists"));
			
			RemoteStorageManager.removeRemoteStorage(name);
			response.setSuccess(this.getLanguageMessage("advanced.remotestorage.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Elimina un advanced storage
	 * @param name
	 * @return
	 */
	@Path("{storageName}/step/{stepName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteAdvancedStorageStep(@PathParam("storageName") String nameStorage, @PathParam("stepName") String stepName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!RemoteStorageManager.existsStorageStep(nameStorage, stepName))
				throw new Exception(getLanguageMessage("advanced.remotestorage.step.notexists"));
			
			RemoteStorageManager.removeRemoteStorageStep(nameStorage, stepName);
			response.setSuccess(this.getLanguageMessage("advanced.remotestorage.step.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
