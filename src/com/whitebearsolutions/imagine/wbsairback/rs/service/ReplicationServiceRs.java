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

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.net.ReplicationManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ReplicationDestinationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ReplicationSourceRs;

@Path("/replication")
public class ReplicationServiceRs extends WbsImagineServiceRs {

	private ReplicationManager replicationManager;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category){
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			replicationManager = new ReplicationManager();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Lista todos los fuentes de replicación
	 * @return
	 */
	@Path("/sources")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSources() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listMap = replicationManager.getSources();
			List<ReplicationSourceRs> objects = ReplicationSourceRs.listMapToObject(listMap);
			
			response.setReplicationSources(objects);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Lista los destinos de replicacion
	 * @return
	 */
	@Path("/destinations")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getDestinations() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listMap = replicationManager.getDestinations();
			List<ReplicationDestinationRs> objects = ReplicationDestinationRs.listMapToObject(listMap);
			
			response.setReplicationDestinations(objects);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un nuevo destino de replicación
	 * @param destination
	 * @return
	 */
	@Path("/destinations")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewDestination(ReplicationDestinationRs destination) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String address [] = new String[4];
			try {
				address = NetworkManager.toAddress(destination.getAddress());
			} catch (Exception ex) {
				throw new Exception(getLanguageMessage("device.logical_volumes.exception.network_address"));
			}
			
			List<Map<String, String>> listMap = replicationManager.getDestinations();
			if (listMap != null && !listMap.isEmpty()) {
				List<ReplicationDestinationRs> objects = ReplicationDestinationRs.listMapToObject(listMap);
				for (ReplicationDestinationRs dest : objects) {
					if (dest.getVg().equals(destination.getVg()) && dest.getLv().equals(destination.getLv()) && dest.getAddress().equals(destination.getAddress()))
						throw new Exception(getLanguageMessage("device.replication.destination.duplicated"));
				}
			}
			
			replicationManager.setDestination(destination.getVg(), destination.getLv(), address, destination.getPassword(), destination.getFilesystem());
			uriCreated = "/destinations";
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
	 * Edita un destino de replicación
	 * @param destination
	 * @return
	 */
	@Path("/destinations")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editDestination(ReplicationDestinationRs destination) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String address [] = new String[4];
			try {
				address = NetworkManager.toAddress(destination.getAddress());
			} catch (Exception ex) {
				throw new Exception(getLanguageMessage("device.logical_volumes.exception.network_address"));
			}
			
			boolean exists = false;
			List<Map<String, String>> listMap = replicationManager.getDestinations();
			if (listMap != null && !listMap.isEmpty()) {
				List<ReplicationDestinationRs> objects = ReplicationDestinationRs.listMapToObject(listMap);
				for (ReplicationDestinationRs dest : objects) {
					if (dest.getVg().equals(destination.getVg()) && dest.getLv().equals(destination.getLv()) && dest.getAddress().equals(destination.getAddress())) {
						exists = true;
						break;
					}
				}
			}
			
			if (!exists) {
				throw new Exception("device.replication.destination.notexists");
			}
			
			replicationManager.setDestination(destination.getVg(), destination.getLv(), address, destination.getPassword(), destination.getFilesystem());
			uriCreated = "/destinations";
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
	 * Crea un nuevo share
	 * @param share
	 * @return
	 */
	@Path("/sources")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewSource(ReplicationSourceRs source) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String address [] = new String[4];
			try {
				address = NetworkManager.toAddress(source.getAddress());
			} catch (Exception ex) {
				throw new Exception("address: "+getLanguageMessage("device.logical_volumes.exception.network_address"));
			}
			
			String addressLocal [] = new String[4];
			try {
				addressLocal = NetworkManager.toAddress(source.getLocalAddress());
			} catch (Exception ex) {
				throw new Exception("local address: "+getLanguageMessage("device.logical_volumes.exception.network_address"));
			}
			
			List<Map<String, String>> listMap = replicationManager.getSources();
			if (listMap != null && !listMap.isEmpty()) {
				List<ReplicationSourceRs> objects = ReplicationSourceRs.listMapToObject(listMap);
				for (ReplicationSourceRs so : objects) {
					if (so.getVg().equals(source.getVg()) && so.getLv().equals(source.getLv()) && so.getAddress().equals(source.getAddress()))
						throw new Exception(getLanguageMessage("device.replication.destination.duplicated"));
				}
			}
			boolean _delete = false, _filesystem = false, _checksum = false, _compress = false, _append = false, _delta = false;
			if (source.getDelete() != null)
				_delete = source.getDelete();
			if (source.getFilesystem() != null)
				_filesystem = source.getFilesystem();
			if (source.getChecksum() != null)
				_checksum = source.getChecksum();
			if (source.getCompress() != null)
				_compress = source.getCompress();
			if (source.getAppend() != null)
				_append = source.getAppend();
			if (source.getDelta() != null)
				_delta = source.getDelta();
			
			int mbs = 0;
			if (source.getMbs() != null)
				mbs = source.getMbs();
			
			replicationManager.setSource(source.getVg(), source.getLv(), address, addressLocal, source.getDestinationVg(), source.getDestinationLv(), source.getPassword(), mbs, _delete, _filesystem, _checksum, _compress, _append, _delta);
			uriCreated = "/sources";
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
	 * Crea un nuevo share
	 * @param share
	 * @return
	 */
	@Path("/sources")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editSource(ReplicationSourceRs source) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String address [] = new String[4];
			try {
				address = NetworkManager.toAddress(source.getAddress());
			} catch (Exception ex) {
				throw new Exception("address: "+getLanguageMessage("device.logical_volumes.exception.network_address"));
			}
			
			String addressLocal [] = new String[4];
			try {
				addressLocal = NetworkManager.toAddress(source.getLocalAddress());
			} catch (Exception ex) {
				throw new Exception("local address: "+getLanguageMessage("device.logical_volumes.exception.network_address"));
			}
			
			boolean exists = false;
			List<Map<String, String>> listMap = replicationManager.getSources();
			if (listMap != null && !listMap.isEmpty()) {
				List<ReplicationSourceRs> objects = ReplicationSourceRs.listMapToObject(listMap);
				for (ReplicationSourceRs so : objects) {
					if (so.getVg().equals(source.getVg()) && so.getLv().equals(source.getLv()) && so.getAddress().equals(source.getAddress())){
						exists = true;
						break;
					}
				}
			}
			
			if (!exists) {
				throw new Exception("device.replication.source.notexists");
			}
			
			int mbs = 0;
			if (source.getMbs() != null)
				mbs = source.getMbs();
			
			replicationManager.setSource(source.getVg(), source.getLv(), address, addressLocal, source.getDestinationVg(), source.getDestinationLv(), source.getPassword(), mbs, source.getDelete(), source.getFilesystem(), source.getChecksum(), source.getCompress(), source.getAppend(), source.getDelete());
			uriCreated = "/sources";
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
	 * Obtiene un source
	 * @param vg
	 * @param lv
	 * @param address
	 * @return
	 */
	@Path("/sources/vg/{vg}/lv/{lv}/address/{address}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSource(@PathParam("vg") String vg, @PathParam("lv") String lv, @PathParam("address") String address) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ReplicationSourceRs source = null;
			List<Map<String, String>> listMap = replicationManager.getSources();
			if (listMap != null && !listMap.isEmpty()) {
				List<ReplicationSourceRs> objects = ReplicationSourceRs.listMapToObject(listMap);
				for (ReplicationSourceRs so : objects) {
					if (so.getVg().equals(vg) && so.getLv().equals(lv) && so.getAddress().equals(address)){
						source = so;
					}
				}
			}
			
			if (source == null) {
				throw new Exception("device.replication.source.notexists");
			}
			
			response.setReplicationSource(source);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Elimina un source
	 * @param vg
	 * @param lv
	 * @param address
	 * @return
	 */
	@Path("/sources/vg/{vg}/lv/{lv}/address/{address}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeSource(@PathParam("vg") String vg, @PathParam("lv") String lv, @PathParam("address") String address) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			replicationManager.removeSource(vg, lv, address);
			response.setSuccess(getLanguageMessage("replication.sources.removed_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un destination
	 * @param vg
	 * @param lv
	 * @param address
	 * @return
	 */
	@Path("/destinations/vg/{vg}/lv/{lv}/address/{address}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getDestination(@PathParam("vg") String vg, @PathParam("lv") String lv, @PathParam("address") String address) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ReplicationDestinationRs dest = null;
			List<Map<String, String>> listMap = replicationManager.getDestinations();
			if (listMap != null && !listMap.isEmpty()) {
				List<ReplicationDestinationRs> objects = ReplicationDestinationRs.listMapToObject(listMap);
				for (ReplicationDestinationRs so : objects) {
					if (so.getVg().equals(vg) && so.getLv().equals(lv) && so.getAddress().equals(address)){
						dest = so;
					}
				}
			}
			
			if (dest == null) {
				throw new Exception("device.replication.source.notexists");
			}
			
			response.setReplicationDestination(dest);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un destination
	 * @param vg
	 * @param lv
	 * @param address
	 * @return
	 */
	@Path("/destinations/vg/{vg}/lv/{lv}/address/{address}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeDestination(@PathParam("vg") String vg, @PathParam("lv") String lv, @PathParam("address") String address) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			replicationManager.removeDestination(vg, lv, address);
			response.setSuccess(getLanguageMessage("replication.destinations.removed_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el intervalo de replicación rsync
	 * @param minutes
	 * @return
	 */
	@Path("/interval")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getRsyncInterval() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String interval = this.getConfig().getProperty("rsync_interval");
				
			response.setRsyncInterval(interval);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Establece el intervalo de replicación rsync
	 * @param minutes
	 * @return
	 */
	@Path("/interval/{minutes}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response setRsyncInterval(@PathParam("value") Long minutes) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			this.getConfig().setProperty("rsync_interval", String.valueOf(minutes*60*1000));
			this.getConfig().store();
			
			response.setSuccess(getLanguageMessage("replication.interval.established"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
