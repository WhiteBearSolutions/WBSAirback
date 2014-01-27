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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.backup.PoolManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobArchievedRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.PoolRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.PoolVolumeRs;

@Path("/pools")
public class PoolServiceRs extends WbsImagineServiceRs {
	
	private PoolManager poolManager = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category){
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			poolManager = new PoolManager(this.config);
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Checkeos comunues al guardar un pool
	 * @param pool
	 */
	void commonSaveChecks(PoolRs pool) {
		if (pool.getDurationHours() == null)
			pool.setDurationHours(0);
		if (pool.getMigrationHours() == null)
			pool.setMigrationHours(0);
		if (pool.getRetention() == null)
			pool.setRetention(0);
	}
	
	
	/**
	 * Devuelve un pool
	 * @param poolname
	 * @return
	 */
	@Path("{poolName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getPool(@PathParam("poolName") String poolName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			poolManager.getPoolId(poolName);	
			Map<String, String> mapPool = poolManager.getPool(poolName);
			PoolRs pool = PoolRs.mapToObject(mapPool);
			response.setPool(pool);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve un pool
	 * @param poolname
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getPools() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapPools = poolManager.getPools();
			List<PoolRs> pools = PoolRs.listMapToObject(mapPools);
			response.setPools(pools);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo pool
	 * @param poolNew
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewPool(PoolRs poolNew) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			boolean exists = false;
			try {
				poolManager.getPoolId(poolNew.getName());
				exists = true;
			} catch (Exception ex){}

			if(exists) {
				throw new Exception(getLanguageMessage("backup.pools.exception.pool_exist"));
			}
			this.commonSaveChecks(poolNew);
			poolManager.setPool(poolNew.getName(), poolNew.getStorage(), poolNew.getRetention(), poolNew.getDurationHours(), poolNew.getCopyDestination(), poolNew.getMigrationHours(), poolNew.getScratchPool(), poolNew.getRecyclePool());
			uriCreated=poolNew.getName();
			
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
	 * Edita un pool
	 * @param poolNew
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editPool(PoolRs pool) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			poolManager.getPoolId(pool.getName());		
			this.commonSaveChecks(pool);
			poolManager.setPool(pool.getName(), pool.getStorage(), pool.getRetention(), pool.getDurationHours(), pool.getCopyDestination(), pool.getMigrationHours(), pool.getScratchPool(), pool.getRecyclePool());
			uriCreated=pool.getName();
			
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
	 * Elimina un pool
	 * @param poolName
	 * @return
	 */
	@Path("{poolName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deletePool(@PathParam("poolName") String poolName) {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			poolManager.getPoolId(poolName);			
			PoolManager.removePool(poolName);
			response.setSuccess(this.getLanguageMessage("backup.pools.success.delete"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve los jobs de un pool
	 * @param poolName
	 * @param level
	 * @return
	 */
	@Path("{poolName}/jobs")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getPoolJobs(@PathParam("poolName") String poolName, @QueryParam("level") String level) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			poolManager.getPoolId(poolName);
			List<Map<String, String>> mapJobs = poolManager.getPendantPoolJobs(poolName, level);
			List<JobArchievedRs> jobs = new ArrayList<JobArchievedRs>();
			if (mapJobs != null && mapJobs.size() > 0) {
				jobs = JobArchievedRs.listMapToObject(mapJobs);
				for (JobArchievedRs job : jobs) {
					if (job.getStatus() != null && !job.getStatus().equals(""))
						job.setStatus(getLanguageMessage(job.getStatus()));
				}
			}
			response.setJobsArchieved(jobs);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Purga los volumenes de un pool
	 * @param poolName
	 * @return
	 */
	@Path("{poolName}/volumes/purge")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response purgePoolVolumes(@PathParam("poolName") String poolName) {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Integer poolId = poolManager.getPoolId(poolName);
			List<Map<String, String>> poolVolumes = poolManager.getPoolVolumes(poolId);
			for (Map<String, String> poolVol : poolVolumes)
				PoolManager.purgePoolVolume(poolVol.get("name"));
			response.setSuccess(this.getLanguageMessage("backup.pools.success.volumes.purge"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Borra un poolVolume
	 * @param poolName
	 * @param label
	 * @return
	 */
	@Path("{poolName}/volumes/{label}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removePoolVolume(@PathParam("poolName") String poolName, @PathParam("label") String label) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			PoolManager.removePoolVolume(poolName, label);
			response.setSuccess(this.getLanguageMessage("backup.pools.success.volumes.purge"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de volumenes asociados a un pool
	 * @param poolName
	 * @return
	 */
	@Path("{poolName}/volumes")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getPoolVolumes(@PathParam("poolName") String poolName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Integer poolId = poolManager.getPoolId(poolName);
			List<Map<String, String>> mapPoolsVol = poolManager.getPoolVolumes(poolId);
			List<PoolVolumeRs> poolVolumes = new ArrayList<PoolVolumeRs>();
			if (mapPoolsVol != null && mapPoolsVol.size()>0)
				poolVolumes = PoolVolumeRs.listMapToObject(mapPoolsVol);
			response.setPoolVolumes(poolVolumes);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un volumen
	 * @param poolName
	 * @param nameVol
	 * @param slot
	 * @return
	 */
	@Path("{poolName}/volumes/add/volume/{nameVol}/slot/{slot}/drive/{drive}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response addAutochargerVolume(@PathParam("poolName") String poolName, @PathParam("nameVol") String nameVol, @PathParam("slot") String slot, @PathParam("drive") String drive) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			poolManager.addAutochangerVolume(poolName, nameVol, slot, drive);
			response.setSuccess(this.getLanguageMessage("backup.pools.success.volumes.autocharger.added"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un volumen de tipo cinta
	 * @param poolName
	 * @param nameVol
	 * @param slot
	 * @return
	 */
	@Path("{poolName}/volumes/add/volume/{nameVol}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response addTapeVolume(@PathParam("poolName") String poolName, @PathParam("nameVol") String nameVol) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			poolManager.addTapeVolume(poolName, nameVol);
			response.setSuccess(this.getLanguageMessage("backup.pools.success.volumes.tape.added"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
