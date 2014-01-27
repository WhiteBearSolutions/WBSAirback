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

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageRemoteRs;

@Path("/storage/remotes")
public class StorageRemoteServiceRs extends WbsImagineServiceRs{
	
	private StorageManager storageManager = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			storageManager = new StorageManager(this.config);
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Checkeos comunes antes de guardar un storage
	 * @param storage
	 * @throws Exception
	 */
	private String[] commonStorageSaveCheckings(StorageRemoteRs storage) throws Exception {

		if(storage.getName() == null || storage.getName().isEmpty()) {
			throw new Exception(getLanguageMessage("backup.storage.exception.storage_name"));
		}
		
		String[] address = null;
		if (NetworkManager.isValidAddress(storage.getAddress())) {
			address = NetworkManager.toAddress(storage.getAddress());
		}
		
		return address;
	}
	
	
	/**
	 * Listado de storage remote
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageRemotes() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<String> list = StorageManager.getRemoteDevices();
			List<StorageRemoteRs> objects = new ArrayList<StorageRemoteRs>();
			for (String name : list) {
				Map<String, String> map = StorageManager.getStorageParameters(name);
				if (map != null && map.size()>0)
					objects.add(StorageRemoteRs.mapToObject(map));
			}

			response.setStorageRemotes(objects);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}

	
	/**
	 * Obtiene un storage remote
	 * @param storageName
	 * @return
	 */
	@Path("{storageName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageRemote(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> map = StorageManager.getStorageParameters(storageName);
			if (map  == null || map.isEmpty())
				throw new Exception(getLanguageMessage("backup.storage.not.exists"));
			StorageRemoteRs object = StorageRemoteRs.mapToObject(map);
			response.setStorageRemote(object);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}

	
	/**
	 * Añade un nuevo storage
	 * @param storage
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewStorageRemote(StorageRemoteRs storage) {
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			String[] address = this.commonStorageSaveCheckings(storage);
			StorageManager.addRemoteDevice(storage.getName(), storage.getType(), storage.getDevice(), address, storage.getPassword(), storage.getMediatype());
			uriCreated = storage.getName();
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
	public Response editStorageRemote(StorageRemoteRs storage) {
		String uriCreated = null;
				
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getStorageParameters(storage.getName());
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			String[] address = this.commonStorageSaveCheckings(storage);
			StorageManager.updateRemoteDevice(storage.getName(), storage.getType(), storage.getDevice(), address, storage.getPassword(), storage.getMediatype());
			uriCreated = storage.getName();
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
	 * @return
	 */
	@Path("{storageName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteStorage(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getStorageParameters(storageName);
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			storageManager.removeRemoteDevice(storageName);
			response.setSuccess(this.getLanguageMessage("backup.storage.rs.successfull.deleted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
