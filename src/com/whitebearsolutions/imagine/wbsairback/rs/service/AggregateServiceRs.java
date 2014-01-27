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

import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.ZFSConfiguration;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.AggregateRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.PhysicalDeviceRs;

@Path("/aggregates")
public class AggregateServiceRs extends WbsImagineServiceRs {
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
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
	 * Devuelve un agregado
	 * @param aggregateName
	 * @return
	 */
	@Path("{aggregateName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAggregate(@PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> mapAgg = VolumeManager.getVolumeGroup(aggregateName);
			List<Map<String, String>> disks = VolumeManager.getPhysicalVolumes(aggregateName);
			AggregateRs agg = AggregateRs.mapToObject(mapAgg, disks);
			response.setAggregate(agg);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve la lista de agregados
	 * @param poolname
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listAggregates() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapAggs = VolumeManager.getVolumeGroups();
			List<AggregateRs> aggs = AggregateRs.listMapToObject(mapAggs);
			response.setAggregates(aggs);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo aggregado
	 * @param aggNew
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewAggregate(AggregateRs aggNew) {
		
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
			
			Map<String, String> existingGroup = null;
			try {
				existingGroup = VolumeManager.getVolumeGroup(aggNew.getName());
			} catch (Exception ex) {}
			if (existingGroup != null)
				throw new Exception(getLanguageMessage("aggregates.rs.name.duplicated"));
			
			List<String> devices = new ArrayList<String>();
			for (PhysicalDeviceRs dev : aggNew.getDevices()) {
				if (dev.getDeviceId() != null && !dev.getDeviceId().isEmpty())
					devices.add(dev.getDeviceId());
				else if (dev.getDevice() != null && !dev.getDevice().isEmpty())
					devices.add(dev.getDevice());
			}
			int type = VolumeManager.VG_LVM;
			if (aggNew.getType().equals("zfs"))
				type = VolumeManager.VG_ZFS;
			else if (!aggNew.getType().equals("lvm"))
				throw new Exception(getLanguageMessage("aggregates.rs.type.notsupported"));
			
			if (aggNew.isLocal()) {
				if (HAConfiguration.inCluster()) {
					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha_master"));
				}
				VolumeManager.setLocalDeviceGroup(aggNew.getName());
			} else
				VolumeManager.setNoLocalDeviceGroup(aggNew.getName());
			
			VolumeManager.addVolumeGroupDevice(type, aggNew.getName(), devices.toArray(new String[devices.size()]));
			uriCreated=aggNew.getName();
			
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
	 * Edita un agregado
	 * @param agg
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editAggregate(AggregateRs agg) {
		
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			VolumeManager.getVolumeGroup(agg.getName());
			
			List<String> devices = new ArrayList<String>();
			if (agg.getDevices() != null && !agg.getDevices().isEmpty()) {
				for (PhysicalDeviceRs dev : agg.getDevices()) {
					devices.add(dev.getDevice());
				}
			}
			int type = VolumeManager.VG_LVM;
			if (agg.getType().equals("zfs"))
				type = VolumeManager.VG_ZFS;
			else if (!agg.getType().equals("lvm"))
				throw new Exception(getLanguageMessage("aggregates.rs.type.notsupported"));
			
			if (agg.isLocal()) {
				if (HAConfiguration.inCluster()) {
					throw new Exception(getLanguageMessage("common.message.no_privilegios_ha_master"));
				}
				VolumeManager.setLocalDeviceGroup(agg.getName());
			} else
				VolumeManager.setNoLocalDeviceGroup(agg.getName());
			
			if (!devices.isEmpty())
				VolumeManager.addVolumeGroupDevice(type, agg.getName(), devices.toArray(new String[devices.size()]));
			
			uriCreated=agg.getName();
			
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
	 * Desmonta todos los volúmenes de un agregado
	 * @param aggregateName
	 * @return
	 */
	@Path("{aggregateName}/umount")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response umountAgg(@PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
			
			VolumeManager.getVolumeGroup(aggregateName);
			VolumeManager.umountAggregate(aggregateName);
			response.setSuccess(this.getLanguageMessage("aggregates.rs.umount.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Intenta montar todos los volúmenes de todos los agregados
	 * @param aggregateName
	 * @return
	 */
	@Path("/refresh")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response refresh() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
			
			VolumeManager.refreshLogicalVolumes();
			response.setSuccess(this.getLanguageMessage("aggregates.rs.refresh.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Desmonta todos los volúmenes de un agregado
	 * @param aggregateName
	 * @return
	 */
	@Path("/devices")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response unnasignedDevices() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> disks = VolumeManager.getUnassignedPhysicalDisks();
			List<PhysicalDeviceRs> devices = PhysicalDeviceRs.listMapToObject(disks);
			
			response.setDevices(devices);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Intenta recuperar un agregado zfs con cierto nombre
	 * @param aggregateName
	 * @return
	 */
	@Path("/recover/{aggregateName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response recoverAggregate(@PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ZFSConfiguration.recoverDamagedGroup(aggregateName);
			response.setSuccess(this.getLanguageMessage("aggregates.rs.recover.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un agregado
	 * @return
	 */
	@Path("{aggregateName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteAggregate(@PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (VolumeManager.isLocalDeviceGroup(aggregateName) && HAConfiguration.inCluster()) {
				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha_master"));
			}
			
			VolumeManager.setNoLocalDeviceGroup(aggregateName);
			VolumeManager.removeVolumeGroup(aggregateName);
			response.setSuccess(this.getLanguageMessage("aggregates.rs.delete.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
