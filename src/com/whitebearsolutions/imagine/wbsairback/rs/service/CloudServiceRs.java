package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.whitebearsolutions.imagine.wbsairback.disk.CloudManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.CloudAccountRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.CloudDiskAddRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.CloudDiskRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.CloudDiskStatRs;

@Path("/cloud")
public class CloudServiceRs extends WbsImagineServiceRs{

	private CloudManager cm; 
	
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category){
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			cm = new CloudManager();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Obtiene el listado de los discos cloud presentes en el sistema
	 * @return
	 */
	@Path("/disks")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getDisks() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> mapDisks = CloudManager.getCloudDisks();
			List<CloudDiskRs> disks = CloudDiskRs.listMapToObject(mapDisks);
			response.setCloudDisks(disks);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de los discos cloud sin asignar
	 * @return
	 */
	@Path("/disks/unassigned")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getUnassignedDisks() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> mapDisks = cm.getUnassignedCloudDisks();
			List<CloudDiskRs> disks = CloudDiskRs.listMapToObject(mapDisks);
			response.setCloudDisks(disks);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene las estadísticas de los discos de un grupo
	 * @return
	 */
	@Path("/disks/group/{group}/stats")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getCloudGroupStats(@PathParam("group") String group) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, List<String>> mapDisks = CloudManager.getCloudSystemStats(group);
			List<CloudDiskStatRs> stats = CloudDiskStatRs.listMapToObject(mapDisks);
			response.setCloudGroupStats(stats);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de los discos asignados a un agregado
	 * @return
	 */
	@Path("/disks/group/{group}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getDisksByGroup(@PathParam("group") String group) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> mapDisks = CloudManager.getCloudDisksByGroup(group);
			List<CloudDiskRs> disks = CloudDiskRs.listMapToObject(mapDisks);
			response.setCloudDisks(disks);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de los discos asignados a un dispositivo
	 * @return
	 */
	@Path("/disks/device/{device}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getDiskByDevice(@PathParam("device") String device) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (device.contains("/"))
				device = "/dev/"+device.substring(device.lastIndexOf("/")+1);
			else
				device = "/dev/"+device;
			
			Map<String, String> mapDisk = CloudManager.getCloudDiskByDevice(device);
			if (mapDisk == null || mapDisk.isEmpty())
				throw new Exception(getLanguageMessage("device.disks.cloud.disk.notfound"));
			CloudDiskRs disk = CloudDiskRs.mapToObject(mapDisk);
			response.setCloudDisk(disk);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo disco cloud
	 * @param disk
	 * @return
	 */
	@Path("/disks")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response createDisk(CloudDiskAddRs disk) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Integer type = CloudManager.TYPE_S3;
			String t = disk.getType().toLowerCase();
			if (t.equals("atmos"))
				type = CloudManager.TYPE_ATMOS;
			else if (!t.equals("s3"))
				throw new Exception(getLanguageMessage("cloud.disks.type.not.supported"));
			else if (t.equals("s3") && (disk.getBucket() == null || disk.getBucket().isEmpty()))
				throw new Exception(getLanguageMessage("device.disks.cloud.disks.error.bucket"));
			
			Map<String, String> account = CloudManager.getAccount(disk.getAccountAlias());
			if (account == null || account.size()<=0)
				throw new Exception(getLanguageMessage("device.disks.cloud.disks.error.accountnotfound"));
			
			t = disk.getSizeUnit().toLowerCase();
			if (!t.equals("m") && !t.equals("g") && !t.equals("t") && !t.equals("mega") && !t.equals("giga") && !t.equals("tera") && !t.equals("mb") && !t.equals("gb") && !t.equals("tb") && !t.equals("megabytes") && !t.equals("gigabytes") && !t.equals("terabytes"))
				throw new Exception(getLanguageMessage("device.disks.cloud.disks.error.unit.invalid"));
			
			String size = disk.getSize() + disk.getSizeUnit();
			CloudManager.createCloudSystem(disk.getAccountAlias(), size,  disk.getBucket(), type);
			response.setSuccess(getLanguageMessage("device.disks.cloud.stored"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un disco cloud a partir del dispositivo en el que se encuentra
	 * @param device
	 * @return
	 */
	@Path("/disks/device/{device}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeCloudDisk(@PathParam("device") String device) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (device.contains("/"))
				device = "/dev/"+device.substring(device.lastIndexOf("/")+1);
			else
				device = "/dev/"+device;
			
			Map<String, String> mapDisk = CloudManager.getCloudDiskByDevice(device);
			if (mapDisk == null || mapDisk.isEmpty())
				throw new Exception(getLanguageMessage("device.disks.cloud.disk.notfound"));
			CloudDiskRs disk = CloudDiskRs.mapToObject(mapDisk);
			CloudManager.removeCloudDisk(disk.getAccount(), device, disk.getCld());
			
			response.setSuccess(getLanguageMessage("device.disks.cloud.disk.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Obtiene el listado de todas las cuentas cloud
	 * @return
	 */
	@Path("/accounts")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAccounts() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<CloudAccountRs> accounts = new ArrayList<CloudAccountRs>();
			for (String alias : CloudManager.listAccountAliases()) {
				Map<String, String> account = CloudManager.getAccount(alias);
				CloudAccountRs ac = CloudAccountRs.mapToObject(account);
				accounts.add(ac);
			}
			response.setCloudAccounts(accounts);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene los datos de cierta cuenta cloud
	 * @param alias
	 * @return
	 */
	@Path("/accounts/{alias}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAccount(@PathParam("alias") String alias) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> account = CloudManager.getAccount(alias);
			if (account == null || account.isEmpty())
				throw new Exception("device.cloud.account.not.exists");
			CloudAccountRs ac = CloudAccountRs.mapToObject(account);
			
			response.setCloudAccount(ac);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina una cuenta cloud, el parámetro force indica si se fuerza el eliminado de discos o no (discos en uso y demás)
	 * @param alias
	 * @param force
	 * @return
	 */
	@Path("/accounts/{alias}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeAccount(@PathParam("alias") String alias, @QueryParam("force") Boolean force) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			boolean f = false;
			if (force != null && force.booleanValue() == true)
				f = true;
			
			CloudManager.removeAccount(alias, f);
			response.setSuccess(getLanguageMessage("device.cloud.account.removed"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea una nueva cuenta cloud
	 * @param account
	 * @return
	 */
	@Path("/accounts")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response createAccount(CloudAccountRs account) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> accountNames = CloudManager.listAccountAliases();
			if (accountNames.contains(account.getName())) {
				throw new Exception(getLanguageMessage("device.cloud.account.duplicated"));
			}
			
			Integer type = CloudManager.TYPE_S3;
			String t = account.getType().toLowerCase();
			if (t.equals("atmos"))
				type = CloudManager.TYPE_ATMOS;
			else if (!t.equals("s3"))
				throw new Exception(getLanguageMessage("cloud.accounts.type.not.supported"));
			
			CloudManager.createAccount(account.getName(), account.getId(), account.getKey(), type, account.getServer());
			response.setSuccess(getLanguageMessage("device.cloud.account.stored"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Edita una nueva cuenta cloud
	 * @param account
	 * @return
	 */
	@Path("/accounts")
	@POST
	@Produces(MediaType.TEXT_XML)
	public Response editAccount(CloudAccountRs account) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> accountNames = CloudManager.listAccountAliases();
			if (!accountNames.contains(account.getName())) {
				throw new Exception(getLanguageMessage("device.cloud.account.not.exists"));
			}
			
			Integer type = CloudManager.TYPE_S3;
			String t = account.getType().toLowerCase();
			if (t.equals("atmos"))
				type = CloudManager.TYPE_ATMOS;
			else if (!t.equals("s3"))
				throw new Exception(getLanguageMessage("cloud.accounts.type.not.supported"));
			
			CloudManager.createAccount(account.getName(), account.getId(), account.getKey(), type, account.getServer());
			response.setSuccess(getLanguageMessage("device.cloud.account.stored"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
