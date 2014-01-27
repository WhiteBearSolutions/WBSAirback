package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.ArrayList;
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
import com.whitebearsolutions.imagine.wbsairback.advanced.StorageInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.StorageInventoryRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.TypeAdvancedRs;

@Path("/advanced/inventory/storage")
public class AdvancedStorageInventoryServiceRs extends WbsImagineServiceRs{
	
	
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
	
	private List<String> checkTypes(StorageInventoryRs storage) throws Exception {
		if (storage.getTypesAdvanced() == null || storage.getTypesAdvanced().isEmpty())
			throw new Exception ("advanced.inventory.storage.noadvancedtypes");
		List<String> types = new ArrayList<String>();
		for (TypeAdvancedRs type : storage.getTypesAdvanced()) {
			if (type.getName() != null && !type.getName().isEmpty()) {
				if (!RemoteStorageManager.existsStorage(type.getName()))
					throw new Exception(getLanguageMessage("advanced.inventory.storage.type.notexists"));
				types.add(type.getName());
			}
		}
		
		return types;
	}
	
	/**
	 * Listado de storagees
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageInventories() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, Object>> maps = StorageInventoryManager.listStorages();
			List<StorageInventoryRs> storages = StorageInventoryRs.listMapToObject(maps);
			response.setStorageInventories(storages);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Obtiene un storage
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageInventory(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Object> map = StorageInventoryManager.getStorage(name);
			if (map == null || map.isEmpty())
				throw new Exception(getLanguageMessage("advanced.inventory.storage.notexists"));
			StorageInventoryRs storage = StorageInventoryRs.mapToObject(map);
			response.setStorageInventory(storage);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un storage
	 * @param storage
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewStorageInventory(StorageInventoryRs storage) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (StorageInventoryManager.existsStorage(storage.getName()))
				throw new Exception(getLanguageMessage("advanced.inventory.storage.duplicated"));
			
			List<String> types = checkTypes(storage);
			
			String user = "";
			String password = "";
			String certificate = "";
			
			if (storage.getUser() != null && !storage.getUser().isEmpty())
				user = storage.getUser();
			if (storage.getPassword() != null && !storage.getPassword().isEmpty())
				password = storage.getPassword();
			if (storage.getCertificate() != null && !storage.getCertificate().isEmpty())
				certificate = storage.getCertificate();
			
			StorageInventoryManager.saveStorage(storage.getName(), types, storage.getIqnwwn(), storage.getAddress(), storage.getPort().toString(), user, password, certificate, false);
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
	 * Edita un storage
	 * @param storage
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editStorageInventory(StorageInventoryRs storage) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!StorageInventoryManager.existsStorage(storage.getName()))
				throw new Exception(getLanguageMessage("advanced.inventory.storage.notexists"));
			
			List<String> types = checkTypes(storage);
			
			String user = "";
			String password = "";
			String certificate = "";
			
			if (storage.getUser() != null && !storage.getUser().isEmpty())
				user = storage.getUser();
			if (storage.getPassword() != null && !storage.getPassword().isEmpty())
				password = storage.getPassword();
			if (storage.getCertificate() != null && !storage.getCertificate().isEmpty())
				certificate = storage.getCertificate();
			
			StorageInventoryManager.saveStorage(storage.getName(), types, storage.getIqnwwn(), storage.getAddress(), storage.getPort().toString(), user, password, certificate, true);
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
	 * Elimina un storage
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteStorageInventory(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!StorageInventoryManager.existsStorage(name))
				throw new Exception(getLanguageMessage("advanced.inventory.storage.notexists"));
			
			StorageInventoryManager.removeStorage(name);
			response.setSuccess(this.getLanguageMessage("advanced.inventory.storage.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
