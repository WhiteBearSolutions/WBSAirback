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
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageDiskRs;

@Path("/storage/disks")
public class StorageDiskServiceRs extends WbsImagineServiceRs{
	
	private StorageManager storageManager = null;
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
			storageManager = new StorageManager(this.config);
			networkManager = this.sessionManager.getNetworkManager();
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
	private String commonStorageSaveCheckings(StorageDiskRs storage) throws Exception {
		StringBuilder address=new StringBuilder();
		if (storage.getNetInterface() != null && !storage.getNetInterface().equals("")) {
			if (getLanguageMessage("backup.storage.all").equals(storage.getNetInterface())){
				address.append("wbsairback");
			} else{
				String[] _interfaceAdd = networkManager.getAddress(storage.getNetInterface());
				for (int x=0;_interfaceAdd.length>x;x++){
					address.append((x==0 ? "" : ".")+_interfaceAdd[x]);
				}
			}
		} else {
			throw new Exception(getLanguageMessage("backup.storage.disks.interface.invalid"));
		}
		return address.toString();
	}
	
	/**
	 * Listado de storageDisks
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageDisks() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> map = StorageManager.getDiskVolumeDevices();
			List<StorageDiskRs> objects = new ArrayList<StorageDiskRs>();
			if (map != null && map.size() > 0)
				objects = StorageDiskRs.listMapToObject(map);
			response.setStorageDisks(objects);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}

	
	/**
	 * Obtiene un storage disk
	 * @param storageName
	 * @return
	 */
	@Path("{storageName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageDisk(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> map = StorageManager.getDiskVolumeDevice(storageName);
			if (map  == null || map.isEmpty())
				throw new Exception(getLanguageMessage("backup.storage.not.exists"));
			StorageDiskRs object = StorageDiskRs.mapToObject(map);
			response.setStorageDisk(object);
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
	public Response addNewStorageDisk(StorageDiskRs storage) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			String address = this.commonStorageSaveCheckings(storage);
			boolean aligned = false;
			if (storage.getAligned() != null && storage.getAligned().booleanValue() == true)
				aligned = true;
			
			Integer paralelJobs = null;
			if (storage.getParalelJobs() != null)
				paralelJobs = storage.getParalelJobs();
			
			if (storage.getType().equals("LV")) {
				if (storage.getGroup() != null && !storage.getGroup().equals("") && storage.getVolume() != null && !storage.getVolume().equals("")) {
					StorageManager.addLogicalVolumeDevice(storage.getName(), storage.getGroup(), storage.getVolume(), address, aligned, paralelJobs);
				}
				else
					throw new Exception(getLanguageMessage("backup.storage.exception.logical_volume"));
			} else if (storage.getType().equals("EXT")) {
				if (storage.getServer() != null && !storage.getServer().equals("") && storage.getShare() != null && !storage.getShare().equals("")) {
					StorageManager.addExternalShareDevice(storage.getName(), storage.getServer(), storage.getShare(), address, paralelJobs);
				}
				else
					throw new Exception(getLanguageMessage("backup.storage.exception.external_share"));
			} else {
				throw new Exception("backup.storage.disks.type");
			}
			
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
	public Response editStorageDisk(StorageDiskRs storage) {
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			if (BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", storage.getName(), "Media Type").equals(""))
				throw new Exception(getLanguageMessage("backup.storage.disk.not.exist"));
			String address = this.commonStorageSaveCheckings(storage);
			boolean aligned = false;
			if (storage.getAligned() != null && storage.getAligned().booleanValue() == true)
				aligned = true;
			Integer paralelJobs = null;
			if (storage.getParalelJobs() != null)
				paralelJobs = storage.getParalelJobs();
			
			if (storage.getType().equals("LV")) {
				if (storage.getGroup() != null && !storage.getGroup().equals("") && storage.getVolume() != null && !storage.getVolume().equals("")) {
					StorageManager.updateLogicalVolumeDevice(storage.getName(), storage.getGroup(), storage.getVolume(), address, aligned, paralelJobs, false);
				}
				else
					throw new Exception(getLanguageMessage("backup.storage.exception.logical_volume"));
			} else if (storage.getType().equals("EXT")) {
				if (storage.getServer() != null && !storage.getServer().equals("") && storage.getShare() != null && !storage.getShare().equals("")) {
					StorageManager.updateExternalShareDevice(storage.getName(), storage.getServer(), storage.getShare(), address, paralelJobs, false);
				}
				else
					throw new Exception(getLanguageMessage("backup.storage.exception.external_share"));
			} else {
				throw new Exception("backup.storage.disks.type");
			}
			
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
			Map<String, String> map = StorageManager.getDiskVolumeDevice(storageName);
			if (map  == null || map.isEmpty())
				throw new Exception(getLanguageMessage("backup.storage.not.exists"));
			storageManager.removeVolumeDevice(storageName);
			response.setSuccess(this.getLanguageMessage("backup.storage.rs.successfull.deleted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
