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
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.net.HACommClient;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.SnapshotRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.VolumeAddRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.VolumeExtendRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.VolumeRs;

@Path("/volumes")
public class VolumeServiceRs extends WbsImagineServiceRs {

	//private StorageManager storageManager;
	private VolumeManager volumeManager;
	
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			//storageManager = new StorageManager(this.getConfig());
			volumeManager = new VolumeManager(this.getConfig());
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Devuelve un volumen
	 * @param volumeName
	 * @return
	 */
	@Path("/aggregate/{aggregateName}/volume/{volumeName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getVolume(@PathParam("volumeName") String volumeName, @PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> mapVol = VolumeManager.getLogicalVolume(aggregateName, volumeName);
			VolumeRs vol = VolumeRs.mapToObject(mapVol);
			Map<String, String> _hourly_snapshot = VolumeManager.getPlannedSnapshot(VolumeManager.LV_SNAPSHOT_HOURLY, aggregateName, volumeName);
			Map<String, String> _daily_snapshot = VolumeManager.getPlannedSnapshot(VolumeManager.LV_SNAPSHOT_DAILY, aggregateName, volumeName);
			
			vol.setSnapshotHourly(false);
			if (!_hourly_snapshot.isEmpty()) {
				vol.setSnapshotHourly(true);
				int retention = 0;
				try {
					retention = Integer.parseInt(_hourly_snapshot.get("retention"));
				} catch (Exception ex) {}
				vol.setSnapshotHourlyRetention(retention);
			}
			
			vol.setSnapshotDaily(false);
			if (!_daily_snapshot.isEmpty()) {
				vol.setSnapshotDaily(true);
				int retention = 0;
				try {
					retention = Integer.parseInt(_daily_snapshot.get("retention"));
				} catch (Exception ex) {}
				int hour = 0;
				try {
					hour = Integer.parseInt(_daily_snapshot.get("hour"));
				} catch (Exception ex) {}
				vol.setSnapshotDailyRetention(retention);
				vol.setSnapshotDailyHour(hour);
			}
			
			response.setVolume(vol);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve la lista total de volumenes
	 * @return
	 */
	@Path("/aggregate/{aggregateName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listVolumesOfAggregate(@PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapTotal = VolumeManager.getLogicalVolumes();
			List<Map<String, String>> mapVols = new ArrayList<Map<String, String>>();
			for (Map<String, String> vol : mapTotal) {
				if (vol.get("vg").equals(aggregateName))
					mapVols.add(vol);
			}
			List<VolumeRs> volumes = VolumeRs.listMapToObject(mapVols);
			response.setVolumes(volumes);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve la lista total de volumenes
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listVolumes() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapVols = VolumeManager.getLogicalVolumes();
			List<VolumeRs> volumes = VolumeRs.listMapToObject(mapVols);
			response.setVolumes(volumes);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo volumen
	 * @param volNew
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewVolume(VolumeAddRs volNew) {
		
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
			
			Map<String, String> existingVol = null;
			try {
				existingVol = VolumeManager.getLogicalVolume(volNew.getAggregate(), volNew.getName());
			} catch (Exception ex) {}
			if (existingVol != null)
				throw new Exception(getLanguageMessage("volumes.rs.name.duplicated"));
			
			int storage_type = VolumeManager.LV_STORAGE_NAS, lv_type = VolumeManager.LV_STRIPED, size_units = VolumeManager.SIZE_LV_M, fs_type = FileSystemManager.FS_XFS;

			String tmp = null;
			if (volNew.getStorage_type() != null && !volNew.getStorage_type().isEmpty()) {
				tmp = volNew.getStorage_type().toLowerCase();
				if (tmp.equals("san") || tmp.equals("s"))
					storage_type = VolumeManager.LV_STORAGE_SAN;
			}
			
			if (volNew.getLv_type() != null && !volNew.getLv_type().isEmpty()) {
				tmp = volNew.getLv_type().toLowerCase(); 
				if (tmp.equals("l") || tmp.equals("linear"))
					lv_type = VolumeManager.LV_LINEAR;
				else if (tmp.equals("m") || tmp.equals("mirror"))
					lv_type = VolumeManager.LV_MIRRROR;
			}
			
			if (volNew.getSize_units() != null && !volNew.getSize_units().isEmpty()) {
				tmp = volNew.getSize_units().toLowerCase();
				if (tmp.equals("g") || tmp.equals("giga") || tmp.equals("gigas") || tmp.equals("gb") || tmp.equals("gigabyte") || tmp.equals("gigabytes"))
					size_units = VolumeManager.SIZE_LV_G;
				else if (tmp.equals("t") || tmp.equals("tera") || tmp.equals("teras") || tmp.equals("tb") || tmp.equals("terabyte") || tmp.equals("terabytes"))
					size_units = VolumeManager.SIZE_LV_T;
			}
			
			Map<String, String> vg = VolumeManager.getVolumeGroup(volNew.getAggregate());
			if (vg.get("type").equals("zfs"))
				fs_type = FileSystemManager.FS_ZFS;
			
			boolean dedup = false;
			if (volNew.getDeduplication() != null)
				dedup = volNew.getDeduplication();
			
			boolean encryption = false;
			if (volNew.getEncryption() != null)
				encryption = volNew.getEncryption();
			
			double percent_snap = 25D;
			if (volNew.getPercent_snapshot() != null)
				percent_snap = volNew.getPercent_snapshot();
			
			VolumeManager.addLogicalVolume(storage_type, lv_type, fs_type, volNew.getAggregate(), volNew.getName(), volNew.getSize().intValue(), size_units, volNew.getCompression(), encryption, dedup, percent_snap);
			
			if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
				if (VolumeManager.isLocalDeviceGroup(volNew.getAggregate())) {
					HACommClient.sendAddVolume(storage_type, lv_type, fs_type,volNew.getAggregate(), volNew.getName(), volNew.getSize().intValue(), size_units, volNew.getCompression(), volNew.getEncryption(), volNew.getDeduplication(), percent_snap);
				} else {
					HACommClient.sendFsTab();
				}
			}
			
			uriCreated=volNew.getName()+"?aggregateName="+volNew.getAggregate();
			
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
	 * Extiende un volumen
	 * @param vol
	 * @return
	 */
	@Path("/extend")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response extendVolume(VolumeExtendRs vol) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String hourly_status = "disabled";
			String daily_status = "disabled";
			String _type = VolumeManager.getLogicalVolumeFS(vol.getAggregate(), vol.getName());
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type) ||
					FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _type)) {
				if ( vol.getSnapshotHourly() != null && vol.getSnapshotHourly().booleanValue() == true) {
					VolumeManager.setPlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_HOURLY, 0, vol.getSnapshotHourlyRetention());
					hourly_status = "enabled";
				} else {
					VolumeManager.removePlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_HOURLY);
				}
				
				if(vol.getSnapshotDaily() != null && vol.getSnapshotDaily().booleanValue() == true) {
					if (vol.getSnapshotDailyHour() != null && vol.getSnapshotDailyRetention() != null) {
						VolumeManager.setPlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_DAILY, vol.getSnapshotDailyHour(), vol.getSnapshotDailyRetention());
						daily_status = "enabled";
					}
				} else {
					VolumeManager.removePlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_DAILY);
				}
			}
			
			VolumeManager.getLogicalVolume(vol.getAggregate(), vol.getName());
			
			int size_type = VolumeManager.SIZE_LV_M;
			String tmp = null;
			if (vol.getSize_type() != null && !vol.getSize_type().isEmpty()) {
				tmp = vol.getSize_type().toLowerCase();
				if (tmp.equals("g") || tmp.equals("giga") || tmp.equals("gigas") || tmp.equals("gb") || tmp.equals("gigabyte") || tmp.equals("gigabytes"))
					size_type = VolumeManager.SIZE_LV_G;
				else if (tmp.equals("t") || tmp.equals("tera") || tmp.equals("teras") || tmp.equals("tb") || tmp.equals("terabyte") || tmp.equals("terabytes"))
					size_type = VolumeManager.SIZE_LV_T;
			}
			
			Double data_reservation = null;
			Double total_reservation = null;
			Double data_size = null;
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
				if (vol.getData_reservation() == null || vol.getData_size() == null || vol.getTotal_reservation() == null)
					throw new Exception (getLanguageMessage("device.volume_groups.exception.zfs.incomplete.parameters"));
				
				data_size = vol.getData_size();
				data_size = VolumeManager.getSize(data_size.intValue(), size_type);
								
				data_reservation = vol.getData_reservation();
				data_reservation = VolumeManager.getSize(data_reservation.intValue(), size_type); 

				total_reservation = vol.getTotal_reservation();
				total_reservation = VolumeManager.getSize(total_reservation.intValue(), size_type);
			}
			
			if (vol.getData_reservation() != null && (vol.getSize() == null || (vol.getData_reservation() > vol.getData_size()))) {
				throw new Exception (getLanguageMessage("device.volume_groups.exception.datareservation"));
			}
			
			if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
				String hourly_retention = null;
				if (vol.getSnapshotHourlyRetention() != null)
					hourly_retention = vol.getSnapshotHourlyRetention().toString();
				
				String daily_retention = null;
				if (vol.getSnapshotDailyRetention() != null)
					daily_retention = vol.getSnapshotDailyRetention().toString();
				
				String daily_hour = null;
				if (vol.getSnapshotDailyHour() != null)
					daily_hour = vol.getSnapshotDailyHour().toString();
				
				String manual_remove = null;
				if (vol.getSnapshotManualRemove() != null)
					manual_remove = vol.getSnapshotManualRemove().toString();
				
				HACommClient.sendExtendVolume(_type, vol.getAggregate(), vol.getName(), Math.abs(vol.getSize().intValue()), size_type, data_size, total_reservation, data_reservation, hourly_status, hourly_retention, daily_status, daily_retention, daily_hour, manual_remove);
			}
			
			VolumeManager.extendLogicalVolume(vol.getAggregate(), vol.getName(), Math.abs(vol.getSize().intValue()), size_type, data_size, total_reservation, data_reservation);
			
			String message = "";
			if(vol.getSize() != 0) {
				message = "device.message.logical_volume.extended";
			} else {
				message = "device.message.logical_volume.configured";
			}
			
			response.setSuccess(getLanguageMessage(message));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Reduce un volumen
	 * @param vol
	 * @return
	 */
	@Path("/reduce")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response reduceVolume(VolumeExtendRs vol) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String hourly_status = "disabled";
			String daily_status = "disabled";
			String _type = VolumeManager.getLogicalVolumeFS(vol.getAggregate(), vol.getName());
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type) ||
					FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, _type)) {
				if ( vol.getSnapshotHourly() != null && vol.getSnapshotHourly().booleanValue() == true) {
					VolumeManager.setPlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_HOURLY, 0, vol.getSnapshotHourlyRetention());
					hourly_status = "enabled";
				} else {
					VolumeManager.removePlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_HOURLY);
				}
				
				if(vol.getSnapshotDaily() != null && vol.getSnapshotDaily().booleanValue() == true) {
					if (vol.getSnapshotDailyHour() != null && vol.getSnapshotDailyRetention() != null) {
						VolumeManager.setPlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_DAILY, vol.getSnapshotDailyHour(), vol.getSnapshotDailyRetention());
						daily_status = "enabled";
					}
				} else {
					VolumeManager.removePlannedSnapshot(vol.getAggregate(), vol.getName(), VolumeManager.LV_SNAPSHOT_DAILY);
				}
			}
			
			VolumeManager.getLogicalVolume(vol.getAggregate(), vol.getName());
			
			int size_type = VolumeManager.SIZE_LV_M;
			String tmp = null;
			if (vol.getSize_type() != null && !vol.getSize_type().isEmpty()) {
				tmp = vol.getSize_type().toLowerCase();
				if (tmp.equals("g") || tmp.equals("giga") || tmp.equals("gigas") || tmp.equals("gb") || tmp.equals("gigabyte") || tmp.equals("gigabytes"))
					size_type = VolumeManager.SIZE_LV_G;
				else if (tmp.equals("t") || tmp.equals("tera") || tmp.equals("teras") || tmp.equals("tb") || tmp.equals("terabyte") || tmp.equals("terabytes"))
					size_type = VolumeManager.SIZE_LV_T;
			}
			
			Double data_reservation = null;
			Double total_reservation = null;
			Double data_size = null;
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, _type)) {
				if (vol.getData_reservation() == null || vol.getData_size() == null || vol.getTotal_reservation() == null)
					throw new Exception (getLanguageMessage("device.volume_groups.exception.zfs.incomplete.parameters"));
				
				data_size = vol.getData_size();
				data_size = VolumeManager.getSize(data_size.intValue(), size_type);
								
				data_reservation = vol.getData_reservation();
				data_reservation = VolumeManager.getSize(data_reservation.intValue(), size_type); 

				total_reservation = vol.getTotal_reservation();
				total_reservation = VolumeManager.getSize(total_reservation.intValue(), size_type);
			}
			
			if (vol.getData_reservation() != null && (vol.getSize() == null || (vol.getData_reservation() > vol.getData_size()))) {
				throw new Exception (getLanguageMessage("device.volume_groups.exception.datareservation"));
			}
			
			if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
				String hourly_retention = null;
				if (vol.getSnapshotHourlyRetention() != null)
					hourly_retention = vol.getSnapshotHourlyRetention().toString();
				
				String daily_retention = null;
				if (vol.getSnapshotDailyRetention() != null)
					daily_retention = vol.getSnapshotDailyRetention().toString();
				
				String daily_hour = null;
				if (vol.getSnapshotDailyHour() != null)
					daily_hour = vol.getSnapshotDailyHour().toString();
				
				String manual_remove = null;
				if (vol.getSnapshotManualRemove() != null)
					manual_remove = vol.getSnapshotManualRemove().toString();
				
				HACommClient.sendExtendVolume(_type, vol.getAggregate(), vol.getName(), -Math.abs(vol.getSize().intValue()), size_type, data_size, total_reservation, data_reservation, hourly_status, hourly_retention, daily_status, daily_retention, daily_hour,manual_remove);
			}
			
			VolumeManager.extendLogicalVolume(vol.getAggregate(), vol.getName(), -Math.abs(vol.getSize().intValue()), size_type, data_size, total_reservation, data_reservation);
			
			String message = "";
			if(vol.getSize() != 0) {
				message = "device.message.logical_volume.reduced";
			} else {
				message = "device.message.logical_volume.configured";
			}
			
			response.setSuccess(getLanguageMessage(message));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un volumen 
	 * @param volumeName
	 * @param aggregateName
	 * @return
	 */
	@Path("/aggregate/{aggregateName}/volume/{volumeName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteVolume(@PathParam("volumeName") String volumeName, @PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			/*List<String> _linked_storages = storageManager.findStorageByLogicalVolume(aggregateName, volumeName);
			if (_linked_storages.size() > 0) {
				String _msg_linked_storages = "";
				for (String storage : _linked_storages)
					_msg_linked_storages += storage + " "; 
				throw new Exception(getLanguageMessage("device.message.logical_volume.linked_storages")+_msg_linked_storages);
			} else {*/
	
				if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
					if (VolumeManager.isLocalDeviceGroup(aggregateName))
						HACommClient.sendRemoveVolume(aggregateName, volumeName);
				}
				volumeManager.removeLogicalVolume(aggregateName, volumeName);
				if (HAConfiguration.inCluster() && !HAConfiguration.isSlaveNode()) {
					if (!VolumeManager.isLocalDeviceGroup(aggregateName))
						HACommClient.sendFsTab();
				}	
			//}
			response.setSuccess(getLanguageMessage("device.message.logical_volume.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Lista los snapshots de un volumen
	 * @param volumeName
	 * @param aggregateName
	 * @return
	 */
	@Path("/aggregate/{aggregateName}/volume/{volumeName}/snapshot")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listSnapshot(@PathParam("volumeName") String volumeName, @PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> mapSnapshots = VolumeManager.getLogicalVolumeSnapshots(aggregateName, volumeName);
			List<SnapshotRs> snapshots = SnapshotRs.listMapToObject(mapSnapshots);
			
			response.setSnapshots(snapshots);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un snapshot
	 * @param snapshotName
	 * @param volumeName
	 * @param aggregateName
	 * @return
	 */
	@Path("/aggregate/{aggregateName}/volume/{volumeName}/snapshot/make")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response makeSnapshot(@PathParam("volumeName") String volumeName, @PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
			
			VolumeManager.createLogicalVolumeSnapshot(aggregateName, volumeName, VolumeManager.LV_SNAPSHOT_MANUAL);
			response.setSuccess(getLanguageMessage("device.message.logical_volume.snapshot.created"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un snapshot
	 * @param snapshotName
	 * @param volumeName
	 * @param aggregateName
	 * @return
	 */
	@Path("/aggregate/{aggregateName}/volume/{volumeName}/snapshot/{snapshotName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteSnapshot(@PathParam("snapshotName") String snapshotName, @PathParam("volumeName") String volumeName, @PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (!HAConfiguration.isActiveNode() || HAConfiguration.isSlaveNode())
				throw new Exception(getLanguageMessage("common.message.no_privilegios_ha"));
			
			VolumeManager.removeLogicalVolumeSnapshot(aggregateName, volumeName, snapshotName);
			response.setSuccess(getLanguageMessage("device.message.logical_volume.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un snapshot
	 * @param snapshotName
	 * @param volumeName
	 * @param aggregateName
	 * @return
	 */
	@Path("/aggregate/{aggregateName}/volume/{volumeName}/snapshot/{snapshotName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSnapshot(@PathParam("snapshotName") String snapshotName, @PathParam("volumeName") String volumeName, @PathParam("aggregateName") String aggregateName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapSnap = VolumeManager.getLogicalVolumeSnapshot(aggregateName, volumeName, snapshotName);
			SnapshotRs snapshot = SnapshotRs.mapToObject(mapSnap);
			response.setSnapshot(snapshot);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
