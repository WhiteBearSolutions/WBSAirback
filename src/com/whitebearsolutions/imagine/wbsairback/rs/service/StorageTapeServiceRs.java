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
import com.whitebearsolutions.imagine.wbsairback.disk.TapeManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageTapeRs;

@Path("/storage/tapes")
public class StorageTapeServiceRs extends WbsImagineServiceRs{
	
	private TapeManager tapeManager = null;
	private NetworkManager networkManager = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			//storageManager = new StorageManager(this.config);
			networkManager = this.sessionManager.getNetworkManager();
			tapeManager = new TapeManager(this.config);
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
	private String[] commonStorageSaveCheckings(StorageTapeRs storage) throws Exception {

		if(storage.getName() == null || storage.getName().isEmpty()) {
			throw new Exception(getLanguageMessage("backup.storage.exception.storage_name"));
		}
		if(storage.getSpool() == null || storage.getSpool().isEmpty()) {
			throw new Exception(getLanguageMessage("backup.storage.exception.storage_volume"));
		}
		String[] _volume = storage.getSpool().split("/");
		if(_volume.length < 2) {
			throw new Exception(getLanguageMessage("backup.storage.exception.storage_volume"));
		}
		StringBuilder address=new StringBuilder();
		if(storage.getNetInterface() != null && getLanguageMessage("backup.storage.all").equals(storage.getNetInterface())){
			address.append("wbsairback");
		} else if (storage.getNetInterface() != null){
			String[] _interfaceAdd=networkManager.getAddress(storage.getNetInterface());
			for (int x=0;_interfaceAdd.length>x;x++){
				address.append((x==0 ? "" : ".")+_interfaceAdd[x]);
			}
		}
		storage.setAddress(address.toString());
		
		if (storage.getSpoolSize() == null)
			storage.setSpoolSize(0);
		
		if (storage.getAutoMount() == null)
			storage.setAutoMount(false);
		
		return _volume;
	}
	
	/**
	 * Listado de storageDisks
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageTapes() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> map = StorageManager.getTapeDevices();
			List<StorageTapeRs> objects = new ArrayList<StorageTapeRs>();
			if (map != null && map.size() > 0)
				objects = StorageTapeRs.listMapToObject(map);
			response.setStorageTapes(objects);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}

	
	/**
	 * Obtiene un storage tape
	 * @param storageName
	 * @return
	 */
	@Path("{storageName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageTape(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> map = StorageManager.getTapeDevice(storageName);
			if (map  == null || map.isEmpty())
				throw new Exception(getLanguageMessage("backup.storage.not.exists"));
			StorageTapeRs object = StorageTapeRs.mapToObject(map);
			response.setStorageTape(object);
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
	public Response addNewStorageTape(StorageTapeRs storage) {
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			String[] volume = this.commonStorageSaveCheckings(storage);
			StorageManager.addTapeDevice(storage.getName(), storage.getDrive(), storage.getFormat(), volume[0], volume[1], storage.getSpoolSize(), storage.getAddress(), storage.getAutoMount());
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
	public Response editStorageTape(StorageTapeRs storage) {
		String uriCreated = null;
				
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getTapeDevice(storage.getName());
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			String[] volume = this.commonStorageSaveCheckings(storage);
			StorageManager.updateTapeDevice(storage.getName(), storage.getDrive(), storage.getFormat(), volume[0], volume[1], storage.getSpoolSize(), storage.getAddress(), storage.getAutoMount());
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
			
			Map<String, String> mapStorage = StorageManager.getTapeDevice(storageName);
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			tapeManager.removeTape(storageName);
			response.setSuccess(this.getLanguageMessage("backup.storage.rs.successfull.deleted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Monta una cita
	 * @param storageName
	 * @return
	 */
	@Path("/mount/{storageName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response mountTape(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getTapeDevice(storageName);
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			TapeManager.mountTape(storageName);
			response.setSuccess(this.getLanguageMessage("backup.storage.tape.rs.successfull.mounted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Desmonta una cita
	 * @param storageName
	 * @return
	 */
	@Path("/umount/{storageName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response umountTape(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getTapeDevice(storageName);
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			TapeManager.umountTape(storageName);
			response.setSuccess(this.getLanguageMessage("backup.storage.tape.rs.successfull.umounted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Formatea una cita
	 * @param storageName
	 * @return
	 */
	@Path("/format/{storageName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response formatTape(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getTapeDevice(storageName);
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			Map<String, String> _device=StorageManager.getTapeDevice(storageName);
			TapeManager.formatTape(storageName, _device.get("device"));
			response.setSuccess(this.getLanguageMessage("backup.storage.tape.rs.successfull.formated"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
}
