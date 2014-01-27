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
import com.whitebearsolutions.imagine.wbsairback.disk.LibraryManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageDriveRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageLibraryRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageLibraryTapeRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.StorageSlotRs;

@Path("/storage/libraries")
public class StorageLibraryServiceRs extends WbsImagineServiceRs{
	
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
	private String[] commonStorageSaveCheckings(StorageLibraryRs storage) throws Exception {

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
			storage.setAddress(address.toString());
		} else if (storage.getNetInterface() != null){
			String[] _interfaceAdd=networkManager.getAddress(storage.getNetInterface());
			for (int x=0;_interfaceAdd.length>x;x++){
				address.append((x==0 ? "" : ".")+_interfaceAdd[x]);
			}
			storage.setAddress(address.toString());
		} 
		
		return _volume;
	}
	
	/**
	 * Listado de storageLibraries
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageLibraries() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> map = StorageManager.getAutochangerDevices();
			List<StorageLibraryRs> objects = new ArrayList<StorageLibraryRs>();
			if (map != null && map.size() > 0)
				objects = StorageLibraryRs.listMapToObject(map);
			response.setStorageLibrarys(objects);
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
	public Response getStorageLibrary(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> map = StorageManager.getAutochangerDevice(storageName);
			if (map  == null || map.isEmpty())
				throw new Exception(getLanguageMessage("backup.storage.not.exists"));
			StorageLibraryRs object = StorageLibraryRs.mapToObject(map);
			response.setStorageLibrary(object);
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
	public Response addNewStorageLibrary(StorageLibraryRs storage) {
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			String[] volume = this.commonStorageSaveCheckings(storage);
			List<String> tapes = new ArrayList<String>();
			for (StorageLibraryTapeRs tape : storage.getTapes()) {
				tapes.add(tape.getName());
			}
			
			int spoolSize = 0;
			if (storage.getSpoolSize() != null)
				spoolSize = storage.getSpoolSize();
			
			String unitsSize = "M";
			String tmp = storage.getUnitsSize();
			if (tmp != null) {
				tmp = tmp.toLowerCase();
				if (tmp.equals("g") || tmp.equals("gb") || tmp.equals("giga") || tmp.equals("gigabyte"))
					unitsSize = "G";
				else if (tmp.equals("t") || tmp.equals("tb") || tmp.equals("tera") || tmp.equals("terabyte"))
					unitsSize = "T";
			}
			
			StorageManager.addAutochangerDevice(storage.getName(), tapes, storage.getDrive(), storage.getFormat(), volume[0], volume[1], spoolSize, unitsSize);
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
	public Response editStorageLibrary(StorageLibraryRs storage) {
		String uriCreated = null;
				
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getAutochangerDevice(storage.getName());
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			String[] volume = this.commonStorageSaveCheckings(storage);
			
			List<String> tapes = new ArrayList<String>();
			for (StorageLibraryTapeRs tape : storage.getTapes()) {
				tapes.add(tape.getName());
			}
			
			int spoolSize = 0;
			if (storage.getSpoolSize() != null)
				spoolSize = storage.getSpoolSize();
			
			String unitsSize = "M";
			String tmp = storage.getUnitsSize();
			if (tmp != null) {
				tmp = tmp.toLowerCase();
				if (tmp.equals("g") || tmp.equals("gb") || tmp.equals("giga") || tmp.equals("gigabyte"))
					unitsSize = "G";
				else if (tmp.equals("t") || tmp.equals("tb") || tmp.equals("tera") || tmp.equals("terabyte"))
					unitsSize = "T";
			}
			
			StorageManager.updateAutochangerDevice(storage.getName(), tapes, storage.getDrive(), storage.getFormat(), volume[0], volume[1], spoolSize, unitsSize);
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
	public Response deleteStorageLibrary(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapStorage = StorageManager.getAutochangerDevice(storageName);
			if (mapStorage == null || mapStorage.size()<=0)
				throw new Exception("backup.storage.rs.not.exists");
			
			storageManager.removeAutochangerDevice(storageName);
			response.setSuccess(this.getLanguageMessage("backup.storage.rs.successfull.deleted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de drives de cierto storage
	 * @param storageName
	 * @return
	 */
	@Path("{storageName}/drives")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageDrives(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> maps =  LibraryManager.getStorageDrives(storageName);
			
			List<StorageDriveRs> drives = StorageDriveRs.listMapToObject(maps);
			response.setStorageDrives(drives);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de slots de cierto storage
	 * @param storageName
	 * @return
	 */
	@Path("{storageName}/slots")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getStorageSlots(@PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> maps =  LibraryManager.getStorageSlots(storageName);
			
			List<StorageSlotRs> slots = StorageSlotRs.listMapToObject(maps);
			response.setStorageSlots(slots);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Monta un drive de un storage en cierto slot
	 * @param storageName
	 * @param numSlot
	 * @param numDrive
	 * @return
	 */
	@Path("{storageName}/mount/drive/{numDrive}/slot/{numSlot}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response mountDriveToSlot(@PathParam("storageName") String storageName, @PathParam("numSlot") Integer numSlot, @PathParam("numDrive") Integer numDrive ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> maps = LibraryManager.getStorageSlots(storageName);
			List<StorageSlotRs> slots = StorageSlotRs.listMapToObject(maps);
			boolean found = false;
			for (StorageSlotRs slot : slots) {
				if (slot.getValue() != null && (Integer.parseInt(slot.getValue()) == numSlot.intValue())) {
					found = true;
					break;
				}
			}
			
			if (!found)
				throw new Exception("backup.storage.library.rs.slot.notexists");
			
			maps = LibraryManager.getStorageDrives(storageName);
			List<StorageDriveRs> drives = StorageDriveRs.listMapToObject(maps);
			found = false;
			for (StorageDriveRs drive : drives) {
				if (drive.getIndex() != null && (Integer.parseInt(drive.getIndex()) == numDrive.intValue())) {
					found = true;
					break;
				}
			}
			
			if (!found)
				throw new Exception("backup.storage.library.rs.drive.notexists");
			
			LibraryManager.mountSlotToDrive(storageName, numDrive, numSlot);
			response.setSuccess(this.getLanguageMessage("backup.storage.rs.successfull.mounted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Desmonta un drive de cierto storage
	 * @param storageName
	 * @param numDrive
	 * @return
	 */
	@Path("{storageName}/umount/drive/{numDrive}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response umountDrive(@PathParam("storageName") String storageName, @PathParam("numDrive") Integer numDrive ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = LibraryManager.getStorageDrives(storageName);
			List<StorageDriveRs> drives = StorageDriveRs.listMapToObject(maps);
			boolean found = false;
			for (StorageDriveRs drive : drives) {
				if (drive.getIndex() != null && (Integer.parseInt(drive.getIndex()) == numDrive.intValue())) {
					found = true;
					break;
				}
			}
			
			if (!found)
				throw new Exception("backup.storage.library.rs.drive.notexists");
			
			LibraryManager.umountDrive(storageName, numDrive);
			response.setSuccess(this.getLanguageMessage("backup.storage.rs.successfull.mounted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Formatea un drive
	 * @param storageName
	 * @param numDrive
	 * @return
	 */
	@Path("{storageName}/format/drive/{numDrive}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response formatDrive(@PathParam("storageName") String storageName, @PathParam("numDrive") Integer numDrive ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = LibraryManager.getStorageDrives(storageName);
			List<StorageDriveRs> drives = StorageDriveRs.listMapToObject(maps);
			boolean found = false;
			for (StorageDriveRs drive : drives) {
				if (drive.getIndex() != null && (Integer.parseInt(drive.getIndex()) == numDrive.intValue())) {
					found = true;
					break;
				}
			}
			
			if (!found)
				throw new Exception("backup.storage.library.rs.drive.notexists");
			
			LibraryManager.formatTape(storageName, numDrive);
			response.setSuccess(this.getLanguageMessage("backup.storage.rs.successfull.mounted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}

}
