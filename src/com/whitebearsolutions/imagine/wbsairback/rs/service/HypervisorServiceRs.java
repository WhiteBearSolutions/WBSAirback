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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorDataStoreRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorJobRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.HypervisorVirtualMachineRs;
import com.whitebearsolutions.imagine.wbsairback.virtual.HypervisorManager;

@Path("/hypervisors")
public class HypervisorServiceRs extends WbsImagineServiceRs{

	private HypervisorManager hyperVisorManager;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category){
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			hyperVisorManager = HypervisorManager.getInstance(HypervisorManager.GENERIC, null, null, null);
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Obtiene todos los hypervisores configurados
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisors() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = hyperVisorManager.getAllHypervisors();
			List<HypervisorRs> hypervisors = HypervisorRs.listMapToObject(maps);
			response.setHypervisors(hypervisors);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo hypervisor
	 * @param visor
	 * @return
	 */
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response createHyperVisor(HypervisorRs visor) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> names = hyperVisorManager.getAllHypervisorNames();
			if (names != null && names.contains(visor.getName()))
				throw new Exception(getLanguageMessage("backup.hypervisors.name.duplicated"));
			
			int hypervisor_type = HypervisorManager.GENERIC;
			String os = visor.getOs().toLowerCase();
            if("vmware".equalsIgnoreCase(os)) {
            	hypervisor_type = HypervisorManager.VMWARE_VSPHERE;
            } else if("xen".equalsIgnoreCase(os)) {
            	hypervisor_type = HypervisorManager.XEN;
            }
            
            if (visor.getAddress().contains(".")) {
             if (!NetworkManager.isValidAddress(visor.getAddress()))
            	 throw new Exception(getLanguageMessage("backup.hypervisors.address.invalid"));
            }
            
            String password = "";
            if (visor.getPassword() != null && !visor.getPassword().isEmpty())
            	password = visor.getPassword();
            
            String user = "";
            if (visor.getUser() != null && !visor.getUser().isEmpty())
            	user = visor.getUser();
            
            hyperVisorManager.setHypervisor(visor.getName(), hypervisor_type, visor.getAddress(),user, password);
			
			response.setSuccess(getLanguageMessage("backup.client.stored"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Edita un hypervisor
	 * @param visor
	 * @return
	 */
	@POST
	@Produces(MediaType.TEXT_XML)
	public Response editHyperVisor(HypervisorRs visor) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> names = hyperVisorManager.getAllHypervisorNames();
			if (names == null || !names.contains(visor.getName()))
				throw new Exception(getLanguageMessage("backup.hypervisors.not.exists"));
			
			int hypervisor_type = HypervisorManager.GENERIC;
			String os = visor.getOs().toLowerCase();
            if("vmware".equalsIgnoreCase(os)) {
            	hypervisor_type = HypervisorManager.VMWARE_VSPHERE;
            } else if("xen".equalsIgnoreCase(os)) {
            	hypervisor_type = HypervisorManager.XEN;
            }
            
            if (visor.getAddress().contains(".")) {
             if (!NetworkManager.isValidAddress(visor.getAddress()))
            	 throw new Exception(getLanguageMessage("backup.hypervisors.address.invalid"));
            }
             
            String password = "";
            if (visor.getPassword() != null && !visor.getPassword().isEmpty())
            	password = visor.getPassword();
            
            String user = "";
            if (visor.getUser() != null && !visor.getUser().isEmpty())
            	user = visor.getUser();
            
            hyperVisorManager.setHypervisor(visor.getName(), hypervisor_type, visor.getAddress(),user, password);
			
			response.setSuccess(getLanguageMessage("backup.client.stored"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un hypervisor a partir de su nombre
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisor(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> map = hyperVisorManager.getHypervisor(name);
			HypervisorRs hypervisor = HypervisorRs.mapToObject(map);
			response.setHypervisor(hypervisor);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un hypervisor con cierto nombre
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteHypervisor(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			hyperVisorManager.deleteHypervisor(name);
			response.setSuccess(getLanguageMessage("backup.message.client.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene la lista de todos los jobs de tipo hypervisor
	 * @return
	 */
	@Path("/jobs")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisorsJobs() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<HypervisorJobRs> hJobs = new ArrayList<HypervisorJobRs>();
			List<String> names = hyperVisorManager.getAllHypervisorJobNames();
			if (names != null && !names.isEmpty()) {
				for (String name : names) {
					Map<String, Object> job = hyperVisorManager.getHypervisorJob(name);
					HypervisorJobRs hj = HypervisorJobRs.mapToObject(job);
					hJobs.add(hj);
				}
			}
			
			response.setHypervisorJobs(hJobs);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo job de hypervisor
	 * @param job
	 * @return
	 */
	@Path("/jobs")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response createHyperVisorJob(HypervisorJobRs job) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> names = hyperVisorManager.getAllHypervisorJobNames();
			if (names != null && names.contains(job.getName()))
				throw new Exception(getLanguageMessage("backup.hypervisors.jobs.name.duplicated"));
			
			List<String> vms = new ArrayList<String>();
			if (job.getVirtualMachines() != null && !job.getVirtualMachines().isEmpty()) {
				for (HypervisorVirtualMachineRs vm: job.getVirtualMachines()) {
					vms.add(vm.getName());
				}
			}
			
			List<String> dss = new ArrayList<String>();
			if (job.getDatastores() != null && !job.getDatastores().isEmpty()) {
				for (HypervisorDataStoreRs ds: job.getDatastores()) {
					dss.add(ds.getName());
				}
			}
             
            hyperVisorManager.setHypervisorJob(job.getName(), job.getHypervisor(), job.getStorage(), job.getMode(), vms, dss);
			response.setSuccess(getLanguageMessage("backup.hypervisors.jobs.stored"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Edita un job de hypervisor
	 * @param job
	 * @return
	 */
	@Path("/jobs")
	@POST
	@Produces(MediaType.TEXT_XML)
	public Response editHyperVisorJob(HypervisorJobRs job) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> names = hyperVisorManager.getAllHypervisorJobNames();
			if (names == null || !names.contains(job.getName()))
				throw new Exception(getLanguageMessage("backup.hypervisors.jobs.not.exists"));
			
			List<String> vms = new ArrayList<String>();
			if (job.getVirtualMachines() != null && !job.getVirtualMachines().isEmpty()) {
				for (HypervisorVirtualMachineRs vm: job.getVirtualMachines()) {
					vms.add(vm.getName());
				}
			}
			
			List<String> dss = new ArrayList<String>();
			if (job.getDatastores() != null && !job.getDatastores().isEmpty()) {
				for (HypervisorDataStoreRs ds: job.getDatastores()) {
					dss.add(ds.getName());
				}
			}
             
            hyperVisorManager.setHypervisorJob(job.getName(), job.getHypervisor(), job.getStorage(), job.getMode(), vms, dss);
			response.setSuccess(getLanguageMessage("backup.hypervisors.jobs.stored"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un job de hypervisor a partir de su nombre
	 * @param name
	 * @return
	 */
	@Path("/jobs/{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisorJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Object> map = hyperVisorManager.getHypervisorJob(name);
			HypervisorJobRs job = HypervisorJobRs.mapToObject(map);
			response.setHypervisorJob(job);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	
	/**
	 * Obtiene los jobs hypervisores de cierto hypervisor a partir del nombre del mismo
	 * @param name
	 * @return
	 */
	@Path("{name}/jobs")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisorJobByHypervisor(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, Object>> map = hyperVisorManager.getHypervisorJobs(name);
			List<HypervisorJobRs> job = HypervisorJobRs.listMapToObject(map);
			response.setHypervisorJobs(job);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un job de hypervisor a partir de su nombre
	 * @param name
	 * @return
	 */
	@Path("/jobs/{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteHypervisorJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			hyperVisorManager.deleteHypervisoJob(name);
			response.setSuccess(getLanguageMessage("hypervisor.jobs.removed.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un listado de las máquinas virtuales accesibles de un hypervisor dado
	 * @return
	 */
	@Path("{name}/machines")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisorVirtualMachines(@PathParam("{name}") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			HypervisorManager hmi = HypervisorManager.getInstance(hyperVisorManager.getHypervisor(name));
			
			List<Map<String, String>> maps = hmi.getVirtualMachines();
			List<HypervisorVirtualMachineRs> vms = HypervisorVirtualMachineRs.listMapToObject(maps);
			response.setHypervisorVirtualMachines(vms);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene una máquina virtual de cierto hypervisor a partir de su nombre
	 * @param hypervisorName
	 * @param machineName
	 * @return
	 */
	@Path("{hypervisorName}/machines/{machineName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisorVirtualMachine(@PathParam("hypervisorName") String hypervisorName, @PathParam("machineName") String machineName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			HypervisorManager hmi = HypervisorManager.getInstance(hyperVisorManager.getHypervisor(hypervisorName));
			Map<String, String> map = hmi.getVirtualMachine(machineName);
			HypervisorVirtualMachineRs vm = HypervisorVirtualMachineRs.mapToObject(map);
			response.setHypervisorVirtualMachine(vm);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene las maquinas virtuales de cierto hypervisor asociadas a cierto storage
	 * @param name
	 * @return
	 */
	@Path("{hypervisorName}/machines/storage/{storageName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisorVirtualMachinesByStorage(@PathParam("hypervisorName") String hypervisorName, @PathParam("storageName") String storageName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			HypervisorManager hmi = HypervisorManager.getInstance(hyperVisorManager.getHypervisor(hypervisorName));
			List<Map<String, String>> maps = hmi.getVirtualMachines(storageName);
			List<HypervisorVirtualMachineRs> vms = HypervisorVirtualMachineRs.listMapToObject(maps);
			response.setHypervisorVirtualMachines(vms);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene los datastores accesibles de cierto hypervisor
	 * @param hypervisorName
	 * @return
	 */
	@Path("{hypervisorName}/stores")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getHypervisorDataStores(@PathParam("hypervisorName") String hypervisorName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			HypervisorManager hmi = HypervisorManager.getInstance(hyperVisorManager.getHypervisor(hypervisorName));
			List<Map<String, String>> maps = hmi.getStores();
			List<HypervisorDataStoreRs> dss = HypervisorDataStoreRs.listMapToObject(maps);
			response.setHypervisorDataStores(dss);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
