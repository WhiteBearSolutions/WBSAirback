package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.ArrayList;
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

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.FilesetLocalVolumeRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.FilesetNdmpVolumeRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.FilesetPluginRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.FilesetRs;

@Path("/filesets")
public class FilesetServiceRs extends WbsImagineServiceRs {
	
	FileSetManager filesetManager = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category){
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			filesetManager = new FileSetManager(this.config);
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	/**
	 * Checkeos comunes al guardar un fileset
	 */
	public void commonSaveFilesetCheckings(FilesetRs fileset, List<Integer> iPlugins) {
		
		if (fileset.getPlugins() != null && fileset.getPlugins().size()>0) {
			for (FilesetPluginRs plugin : fileset.getPlugins()) {
				if("systemstate".equals(plugin.getName())) {
					iPlugins.add(FileSetManager.PLUGIN_SYSTEMSTATE);
				}
				if("sharepoint".equals(plugin.getName())) {
					iPlugins.add(FileSetManager.PLUGIN_SHAREPOINT);
				}
				if("mssql".equals(plugin.getName())) {
					iPlugins.add(FileSetManager.PLUGIN_MSSQL);
				}
				if("exchange".equals(plugin.getName())) {
					iPlugins.add(FileSetManager.PLUGIN_EXCHANGE);
				}
			}
		}
		if (fileset.getMd5() == null)
			fileset.setMd5(false);
		if (fileset.getCompression() == null)
			fileset.setCompression(FileSetManager.COMPRESSION_NONE);
		if (fileset.getMultiplefs() == null)
			fileset.setMultiplefs(false);
		if (fileset.getAcl() == null)
			fileset.setAcl(false);
		if (fileset.getVss() == null)
			fileset.setVss(false);
	}
	
	/**
	 * Checkeos comunes al guardar un fileset
	 */
	public void commonSaveLocalFilesetCheckings(FilesetRs fileset, List<String> volumes) {
		if (fileset.getMd5() == null)
			fileset.setMd5(false);
		if (fileset.getCompression() == null)
			fileset.setCompression(FileSetManager.COMPRESSION_NONE);
		
		if (fileset.getVolumes() != null && fileset.getVolumes().size() > 0) {
			for (FilesetLocalVolumeRs vol : fileset.getVolumes()) {
				volumes.add(vol.getName());
			}
		}
	}
	
	
	/**
	 * Checkeos comunes al guardar un fileset
	 */
	public String[] commonSaveNdmpFilesetCheckings(FilesetRs fileset, Map<String, String> volumes, Integer authtype, Integer type) {
		String[] address = null;
		if (fileset.getNdmpAddress() != null && fileset.getNdmpAddress().length() > 0) {
			if (NetworkManager.isValidAddress(fileset.getNdmpAddress()))
				address = fileset.getNdmpAddress().split("\\.");
		}
		
		if (fileset.getNdmpAuth() != null && !fileset.getNdmpAuth().equals("")) {
			if (fileset.getNdmpAuth().equals("none"))
				authtype = FileSetManager.NDMP_AUTH_NONE;
			else if (fileset.getNdmpAuth().equals("text"))
				authtype = FileSetManager.NDMP_AUTH_TEXT;
			else if (fileset.getNdmpAuth().equals("md5"))
				authtype = FileSetManager.NDMP_AUTH_MD5;
		}
		
		if (fileset.getNdmpType() != null && !fileset.getNdmpType().equals("")) {
			if (fileset.getNdmpType().equals("dump"))
				type = FileSetManager.NDMP_TYPE_DUMP;
			else if (fileset.getNdmpType().equals("smtape"))
				type = FileSetManager.NDMP_TYPE_SMTAPE;
			else if (fileset.getNdmpType().equals("tar"))
				type = FileSetManager.NDMP_TYPE_TAR;
		}
		
		if (fileset.getNdmpVolumes() != null && fileset.getNdmpVolumes().size() > 0) {
			for (FilesetNdmpVolumeRs vol : fileset.getNdmpVolumes()) {
				volumes.put(vol.getVolume(), vol.getFile());
			}
		}
		
		return address;
	}

	
	/**
	 * Listado de todos los filesets
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getFilesets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapFilesets = FileSetManager.getAllFileSets();
			List<FilesetRs> filesets = new ArrayList<FilesetRs>();
			if (mapFilesets != null && mapFilesets.size() > 0)
				filesets = FilesetRs.listMapToObject(mapFilesets);
			response.setFilesets(filesets);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Listado de filesets de tipo local
	 * @return
	 */
	@Path("/local")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getLocalFilesets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapFilesets = FileSetManager.getAllLocalFileSets();
			List<FilesetRs> filesets = new ArrayList<FilesetRs>();
			if (mapFilesets != null && mapFilesets.size() > 0)
				filesets = FilesetRs.listMapToObject(mapFilesets);
			response.setFilesets(filesets);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Listado de filesets de tipo ndmp
	 * @return
	 */
	@Path("/ndmp")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getNdmpFilesets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapFilesets = FileSetManager.getAllNDMPFileSets();
			List<FilesetRs> filesets = new ArrayList<FilesetRs>();
			if (mapFilesets != null && mapFilesets.size() > 0)
				filesets = FilesetRs.listMapToObject(mapFilesets);
			response.setFilesets(filesets);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}

	
	/**
	 * Obtiene un fileset a partir de su nombre
	 * @return
	 */
	@Path("{filesetName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getFileset(@PathParam("filesetName") String filesetName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> mapFileset = FileSetManager.getFileSet(filesetName);
			FilesetRs filesets = new FilesetRs();
			if (mapFileset == null || mapFileset.isEmpty())
				throw new Exception(getLanguageMessage("backup.fileset.rs.notexists"));
			filesets = FilesetRs.mapToObject(mapFileset);
			response.setFileset(filesets);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Borra un fileset
	 * @param filesetName
	 * @return
	 */
	@Path("{filesetName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteFileset(@PathParam("filesetName") String filesetName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			FileSetManager.removeFileSet(filesetName);
			response.setSuccess(this.getLanguageMessage("backup.fileset.rs.successful.deleted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * @param filesetNew
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewFileset(FilesetRs filesetNew) {
		
		String uriCreated = null;
		
		try {		
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (filesetNew.getLocal() != null && filesetNew.getLocal().equals(true)) {
				List<String> volumes = new ArrayList<String>();
				FileSetManager.addLocalFileSet(filesetNew.getName(), volumes, filesetNew.getInclude(), filesetNew.getExclude(), filesetNew.getMd5(), filesetNew.getCompression());
			} else if (filesetNew.getNdmp() != null && filesetNew.getNdmp().equals(true)) {
				String[] address = new String[4];
				Map<String, String> volumes = new HashMap<String, String> ();
				Integer authtype = FileSetManager.NDMP_AUTH_NONE, type = FileSetManager.NDMP_TYPE_DUMP;
				address = this.commonSaveNdmpFilesetCheckings(filesetNew, volumes, authtype, type);
				FileSetManager.addNDMPFileSet(filesetNew.getName(), address, filesetNew.getNdmpPort(), authtype, filesetNew.getNdmpUser(), filesetNew.getNdmpPassword(), volumes, type);
			} else {
				List<Integer> iPlugins = new ArrayList<Integer>();
				this.commonSaveFilesetCheckings(filesetNew, iPlugins);
				FileSetManager.addFileSet(filesetNew.getName(), filesetNew.getInclude(), filesetNew.getExclude(), filesetNew.getExtension(), filesetNew.getMd5(), filesetNew.getCompression(), filesetNew.getAcl(), filesetNew.getMultiplefs(), filesetNew.getVss(), iPlugins);
			}
			
			uriCreated=filesetNew.getName();
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
	 * Añade un nuevo fileset
	 * @param filesetNew
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editNewFileset(FilesetRs filesetNew) {
		
		String uriCreated = null;
		
		try {		
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			// Check if exists
			FileSetManager.getFileSet(filesetNew.getName());
			
			if (filesetNew.getLocal() != null && filesetNew.getLocal().equals(true)) {
				List<String> volumes = new ArrayList<String>();
				FileSetManager.updateLocalFileSet(filesetNew.getName(), volumes, filesetNew.getInclude(), filesetNew.getExclude(), filesetNew.getMd5(), filesetNew.getCompression());
			} else if (filesetNew.getNdmp() != null && filesetNew.getNdmp().equals(true)) {
				Map<String, String> volumes = new HashMap<String, String> ();
				Integer authtype = FileSetManager.NDMP_AUTH_NONE, type = FileSetManager.NDMP_TYPE_DUMP;
				String[] address = this.commonSaveNdmpFilesetCheckings(filesetNew, volumes, authtype, type);
				FileSetManager.updateNDMPFileSet(filesetNew.getName(), address, filesetNew.getNdmpPort(), authtype, filesetNew.getNdmpUser(), filesetNew.getNdmpPassword(), volumes, type);
				
			} else {
				List<Integer> iPlugins = new ArrayList<Integer>();
				this.commonSaveFilesetCheckings(filesetNew, iPlugins);
				FileSetManager.updateFileSet(filesetNew.getName(), filesetNew.getInclude(), filesetNew.getExclude(), filesetNew.getExtension(), filesetNew.getMd5(), filesetNew.getCompression(), filesetNew.getAcl(), filesetNew.getMultiplefs(), filesetNew.getVss(), iPlugins);
			}
			
			uriCreated=filesetNew.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
}
