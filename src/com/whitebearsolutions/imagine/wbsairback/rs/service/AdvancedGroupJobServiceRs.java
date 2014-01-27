package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import com.whitebearsolutions.imagine.wbsairback.advanced.GroupJobManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.RemoteStorageManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StepManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.StorageInventoryManager;
import com.whitebearsolutions.imagine.wbsairback.advanced.TemplateJobManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupJobConfigRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupStepRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.JobGroupRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobScheduledRs;

@Path("/groupjob")
public class AdvancedGroupJobServiceRs extends WbsImagineServiceRs{
	
	private JobManager jm = null;
	private BackupOperator bo = null;
	private ClientManager cm = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general, gestiona sesión y autenticación
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			jm = new JobManager(this.getConfig());
			bo = new BackupOperator(this.getConfig());
			cm = new ClientManager(this.getConfig());
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	/**
	 * Listado de grupos
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getGroupJobs() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, Object>> maps = GroupJobManager.listGroupJobs(this.getConfig());
			List<GroupRs> groups = GroupRs.listMapToObject(maps);
			response.setGroups(groups);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un grupo
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getGroupJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Object> map = GroupJobManager.getGroupJob(name, this.getConfig(), jm.getArchivedClientJobs(-1, null, 0, 0));
			if (map == null || map.isEmpty())
				throw new Exception("advanced.group.notexists");
			GroupRs group = GroupRs.mapToObject(map);
			response.setGroup(group);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene la configuracion un grupo
	 * @param name
	 * @return
	 */
	@Path("{name}/configuration")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getGroupJobConfiguration(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			GroupJobConfigRs group = GroupJobManager.getGroupJobConfiguration(name);
			if (group == null)
				throw new Exception("advanced.group.configuration.notexists");
			response.setGroupJobConfiguration(group);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Launch group job
	 * @param name
	 * @return
	 */
	@Path("{name}/launch")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response launchGroupJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			boolean launched = GroupJobManager.launchGroupJob(name, this.getConfig(), bo, cm);
			if (launched)
				response.setSuccess(getLanguageMessage("advanced.groupjob.launched"));
			else
				response.setSuccess(getLanguageMessage("advanced.groupjob.launched.nojobs"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Cancel group job
	 * @param name
	 * @return
	 */
	@Path("{name}/cancel")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response cancelGroupJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			GroupJobManager.cancelGroupJob(name, this.getConfig(), bo);
			response.setSuccess(getLanguageMessage("advanced.groupjob.canceled"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Stop group job 
	 * @param name
	 * @return
	 */
	@Path("{name}/stop")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response stopGroupJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			GroupJobManager.stopGroupJob(name, this.getConfig(), bo);
			response.setSuccess(getLanguageMessage("advanced.groupjob.stopped"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Restart group job
	 * @param name
	 * @return
	 */
	@Path("{name}/restart")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response restartGroupJob(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			GroupJobManager.restartGroupJob(name, this.getConfig(), bo, cm);
			response.setSuccess(getLanguageMessage("advanced.groupjob.restarted"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un grupo
	 * @param group
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewGroupJob(GroupRs group) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (GroupJobManager.existsGroupJob(group.getName()))
				throw new Exception("advanced.group.duplicated");
			
			Map<Integer, Map<String, Object>> jobs = new HashMap<Integer, Map<String, Object>>();
			if (group.getJobs() != null && !group.getJobs().isEmpty()) {
				for (JobGroupRs o : group.getJobs()) {
					Map<String, Object> job = new HashMap<String, Object>();
					job.put("name", o.getName());
					jobs.put(o.getOrder(), job);
				}
			} else
				throw new Exception(getLanguageMessage("advanced.groupjob.exception.jobs.empty"));
			
			GroupJobManager.saveGroupJob(group.getName(), GroupJobManager.TYPE_MANUAL_SELECTION, null, jobs, group.getSchedule(), null);
			uriCreated=group.getName();
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
	 * Configura un grupo de jobs
	 */
	@SuppressWarnings("unchecked")
	@Path("/configure")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response configureGroupJob(GroupJobConfigRs config) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ISCSIManager iscsim = new ISCSIManager(this.getConfig());
			
			if (GroupJobManager.existsGroupJob(config.getName()))
				throw new Exception("advanced.group.duplicated");
			
			String storage = null;
			if (config.getStorage() != null && !config.getStorage().isEmpty()) {
				storage = config.getStorage();
			} else {
				throw new Exception(getLanguageMessage("backup.jobs.exception.repository"));
			}
			
			if (!BackupOperator.isBlock_reload())
				BackupOperator.setBlock_reload(true);
			
			Map<Integer, Map<String, Object>> jobs = new TreeMap<Integer, Map<String, Object>>();
			boolean first = true;
			Map<String, Object> templateJob = TemplateJobManager.getTemplateJob(config.getTemplateJob());
			String templateStorageType = TemplateJobManager.getTypeStorageTemplate(config.getTemplateJob());
			Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
			for (GroupStepRs stepGroup : config.getSteps()) {
				Integer order = stepGroup.getOrder();
				Map<String, String> step = TemplateJobManager.getTemplateJobStep(config.getTemplateJob(), stepGroup.getName());
				if (step == null || step.isEmpty())
					throw new Exception("advanced.groupjob.step.notexists");
				String schedule = null;
				if (first && config.getSchedule() != null && !config.getSchedule().isEmpty()) {
					schedule = config.getSchedule();
					first = false;
				}
				
				String nameStep = step.get("name");
				String typeStep = step.get("type");
				String dataStep = step.get("data");
				String nameJob = null;
				String inventoryStep = null;
				String nextJob = getNextJob(order, config, steps, config.getName(),cm);
				
				if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
					Map<String, Object> storageInventory = null;
					Map<String, Object> remoteStorage = null;
					Map<String, String> variablesValues = new HashMap<String, String>();
					if (stepGroup.getStorageInventory() != null && !stepGroup.getStorageInventory().isEmpty()) {
						storageInventory = StorageInventoryManager.getStorage(stepGroup.getStorageInventory());
						remoteStorage = RemoteStorageManager.getRemoteStorage(dataStep);
						if (remoteStorage == null || remoteStorage.isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.groupjob.error.advancedstorage.remotestorage")+" :"+nameStep);
						}
						
					} else {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.advancedstorage.inventory.name")+" :"+nameStep);
					}
					inventoryStep = (String) storageInventory.get("name");
					nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), order, "airback-fd", nameStep);
					jm.setEmptyJobForScripts(nameJob, "airback-fd", nextJob, null, schedule, storage);
					RemoteStorageManager.generateScriptsJob(nameJob, nameStep, storageInventory, remoteStorage, variablesValues, jm, iscsim);
					
				} else if (typeStep.equals(StepManager.TYPE_STEP_BACKUP)) {
					boolean verifyPreviousJob = true, spoolData = false, accurate = false, enabled = true;
	    			int maxStartDelay = 0, maxRunTime = 0, maxWaitTime = 0, priority = 0, bandwith = 0;
	    			
					if (stepGroup.getClient() == null || stepGroup.getClient().isEmpty())
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.client")+" :"+nameStep);
					
					String clientName = stepGroup.getClient();
					JobScheduledRs job = stepGroup.getJob();
					
					if (job.getLevel() == null || job.getLevel().isEmpty()) 
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.level")+" :"+nameStep);
					
					if (!clientName.equals("airback-fd") && (job.getFileset() == null || job.getFileset().isEmpty())) 
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.fileset")+" :"+nameStep);
					
					if (job.getPool() == null || job.getPool().isEmpty())
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.poolDefault")+" :"+nameStep);
					
					if (job.getMaxStartDelay() != null)
						maxStartDelay = job.getMaxStartDelay();
					if (job.getMaxRunTime() != null)
						maxRunTime = job.getMaxRunTime();
					if (job.getMaxWaitTime() != null)
						maxWaitTime = job.getMaxWaitTime();
	    			if (job.getBandwith() != null)
	    				bandwith = job.getBandwith();
	    			
					if (job.getSpooldata() != null && !job.getSpooldata().booleanValue() == true)
						spoolData = true;
					
					nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), order, clientName, nameStep);
					Map<String, Object> storageInventory = null;
					String fileset = job.getFileset();
					if (clientName.equals("airback-fd") && stepGroup.getStorageInventory() != null && !stepGroup.getStorageInventory().isEmpty()) {
						storageInventory = StorageInventoryManager.getStorage(stepGroup.getStorageInventory());
						inventoryStep = (String)  storageInventory.get("name");
						fileset = StorageInventoryManager.getNameHiddenFileset((String) storageInventory.get("name"), templateStorageType);
						if (BaculaConfiguration.existBaculaInclude("/etc/bacula/bacula-dir.conf", "@/etc/bacula/" + "filesets" + "/" + fileset + ".conf"))
							FileSetManager.updateExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), templateStorageType, nameJob), FileSetManager.COMPRESSION_NONE);
						else
							FileSetManager.addExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), templateStorageType, nameJob), FileSetManager.COMPRESSION_NONE);
					}
					
					if (fileset == null) {
						if (clientName.equals("airback-fd"))
							throw new Exception(getLanguageMessage("advanced.group.jobs.backup.exception.inventory"));
						else
							throw new Exception(getLanguageMessage("advanced.group.jobs.backup.exception.fileset"));
					}
						
					jm.setJob(nameJob, clientName, job.getLevel(), schedule, fileset, storage, job.getPool(), null, null, null, null, nextJob, verifyPreviousJob, maxStartDelay, maxRunTime, maxWaitTime, spoolData, enabled, priority, 0, bandwith, accurate, false, 0, 0);
					
					if (clientName.equals("airback-fd") && stepGroup.getStorageInventory() != null && !stepGroup.getStorageInventory().isEmpty()) {
						RemoteStorageManager.generateBackupRemoteScripts(nameJob, storageInventory, templateStorageType, iscsim, jm);
					}
					
					
				} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP) || typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
					if (stepGroup.getClient() == null || stepGroup.getClient().isEmpty())
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.client")+" :"+nameStep);
					
					String clientName = stepGroup.getClient();
					
					ScriptProcessManager sp = new ScriptProcessManager();
					Map<String, Object> scriptProcess = sp.getScript(dataStep);
					nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), order, clientName, nameStep);
					
					jm.setEmptyJobForScripts(nameJob, clientName , nextJob, null, schedule, storage);
					
					Map<String, String> _variableValues=new HashMap<String, String>();
					if (scriptProcess!=null && scriptProcess.get("variables")!=null){
						List<Map<String, String>> _variables= (List<Map<String, String>>) scriptProcess.get("variables");
						for (Map<String, String> _variable: _variables){
							if (stepGroup.getVar(_variable.get("name"))!=null){
								_variableValues.put(_variable.get("name"),stepGroup.getVar(_variable.get("name")).getValue());
							} else{
								_variableValues.put(_variable.get("name"),"");
							}
						}
					}
					List<String> _scriptsOrder=new ArrayList<String>();
					TreeMap<String,Map<String, String>> _scriptsContent=new TreeMap<String, Map<String, String>>();
					if (scriptProcess!=null && scriptProcess.get("scripts")!=null){
						List<Map<String, String>> _scripts= (List<Map<String, String>>) scriptProcess.get("scripts");
						for (Map<String, String> _script: _scripts){
								Map<String, String> objScript = new HashMap<String, String>();
								objScript.put("order", _script.get("order"));
								objScript.put("script", ScriptProcessManager.getSmartContent(_script.get("content"), _variableValues));
								objScript.put("shell", _script.get("shell"));
								_scriptsContent.put(_script.get("order"),objScript);										
								_scriptsOrder.add(_script.get("order"));
						}
					}
					Collections.sort(_scriptsOrder);
					
					boolean before = false;
					if (scriptProcess.get("type") != null && Integer.valueOf((String) scriptProcess.get("type")) == ScriptProcessManager.BEFORE_EXECUTION)
						before = true;
					
					boolean abortOnError = false;
					if (scriptProcess.get("abortType") != null && Boolean.valueOf((String) scriptProcess.get("abortType")) == true)
						abortOnError = true;
					
					ScriptProcessManager.generateScriptsJob(nameJob, (String) scriptProcess.get("name"), jm, _scriptsContent, abortOnError, before, true, true, _variableValues);
				}
				
				if (nameJob != null) {
					Map<String, Object> job = new HashMap<String, Object>();
					job.put("name", nameJob);
					job.put("order", order);
					job.put("typeStep", typeStep);
					job.put("step", nameStep);
					if (inventoryStep != null && !inventoryStep.isEmpty())
						job.put("inventory", inventoryStep);
					jobs.put(order, job);
				}
			}
			
			GroupJobManager.saveGroupJob(config.getName(), GroupJobManager.TYPE_TEMPLATEJOB, config.getTemplateJob(), jobs, config.getSchedule(), config.getStorage());
			GroupJobManager.saveGroupJobConfig(config.getName(), config.getSchedule(), config.getStorage(), config.getTemplateJob(), config.getSteps());
			
			if (templateJob.get("groups_status") == null || templateJob.get("groups_status").equals(TemplateJobManager.GROUPS_STATUS_NO_GROUPS))
				TemplateJobManager.writeTemplateJobXml((String) templateJob.get("name"), (Map<Integer, Map<String, String>>)templateJob.get("steps"), TemplateJobManager.GROUPS_STATUS_ALL_UPDATED);

			uriCreated=config.getName()+"/configuration";
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
			// Si se produce algún fallo, borramos todos los jobs creados
			for (GroupStepRs step :  config.getSteps()) {
				try {
					String nameJob = null;
					String nameStep = step.getName();
					String typeStep = step.getType();
					if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
						nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), "airback-fd", nameStep);
					} else {
						String clientName = step.getClient();
						nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), clientName, nameStep);
					}
					
					GroupJobManager.removeHiddenFileSet(nameJob, jm);
					jm.removeJob(nameJob);
				} catch (Exception ex2) {}
			}
		} finally {
			try {
				if (BackupOperator.isBlock_reload())
					BackupOperator.setBlock_reload(false);
				BackupOperator.reload();
			} catch (Exception ex) {
				response.setError("Error: "+ex.getMessage());
				uriCreated = null;
				for (GroupStepRs step :  config.getSteps()) {
					try {
						String nameJob = null;
						String nameStep = step.getName();
						String typeStep = step.getType();
						if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
							nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), "airback-fd", nameStep);
						} else {
							String clientName = step.getClient();
							nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), clientName, nameStep);
						}
						
						GroupJobManager.removeHiddenFileSet(nameJob, jm);
						jm.removeJob(nameJob);
					} catch (Exception ex2) {}
				}
			}
		}
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Configura un grupo de jobs
	 */
	@SuppressWarnings("unchecked")
	@Path("/configure")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editGroupJobConfiguration(GroupJobConfigRs config) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ISCSIManager iscsim = new ISCSIManager(this.getConfig());
			
			if (!GroupJobManager.existsGroupJob(config.getName()))
				throw new Exception("advanced.group.notexists");
			
			if (GroupJobManager.existsGroupJob(config.getName()))
				GroupJobManager.removeGroupJob(config.getName(), true, jm, this.getConfig());
			
			String storage = null;
			if (config.getStorage() != null && !config.getStorage().isEmpty()) {
				storage = config.getStorage();
			} else {
				throw new Exception(getLanguageMessage("backup.jobs.exception.repository"));
			}
			
			if (!BackupOperator.isBlock_reload())
				BackupOperator.setBlock_reload(true);
			
			Map<Integer, Map<String, Object>> jobs = new TreeMap<Integer, Map<String, Object>>();
			boolean first = true;
			Map<String, Object> templateJob = TemplateJobManager.getTemplateJob(config.getTemplateJob());
			String templateStorageType = TemplateJobManager.getTypeStorageTemplate(config.getTemplateJob());
			Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
			for (GroupStepRs stepGroup : config.getSteps()) {
				Integer order = stepGroup.getOrder();
				Map<String, String> step = TemplateJobManager.getTemplateJobStep(config.getTemplateJob(), stepGroup.getName());
				if (step == null || step.isEmpty())
					throw new Exception("advanced.groupjob.step.notexists");
				String schedule = null;
				if (first && config.getSchedule() != null && !config.getSchedule().isEmpty()) {
					schedule = config.getSchedule();
					first = false;
				}
				
				String nameStep = step.get("name");
				String typeStep = step.get("type");
				String dataStep = step.get("data");
				String nameJob = null;
				String inventoryStep = null;
				String nextJob = getNextJob(order, config, steps, config.getName(),cm);
				
				if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
					Map<String, Object> storageInventory = null;
					Map<String, Object> remoteStorage = null;
					Map<String, String> variablesValues = new HashMap<String, String>();
					if (stepGroup.getStorageInventory() != null && !stepGroup.getStorageInventory().isEmpty()) {
						storageInventory = StorageInventoryManager.getStorage(stepGroup.getStorageInventory());
						remoteStorage = RemoteStorageManager.getRemoteStorage(dataStep);
						if (remoteStorage == null || remoteStorage.isEmpty()) {
							throw new Exception(getLanguageMessage("advanced.groupjob.error.advancedstorage.remotestorage")+" :"+nameStep);
						}
						
					} else {
						throw new Exception(getLanguageMessage("advanced.groupjob.error.advancedstorage.inventory.name")+" :"+nameStep);
					}
					inventoryStep = (String) storageInventory.get("name");
					nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), order, "airback-fd", nameStep);
					jm.setEmptyJobForScripts(nameJob, "airback-fd", nextJob, null, schedule, storage);
					RemoteStorageManager.generateScriptsJob(nameJob, nameStep, storageInventory, remoteStorage, variablesValues, jm, iscsim);
					
				} else if (typeStep.equals(StepManager.TYPE_STEP_BACKUP)) {
					boolean verifyPreviousJob = true, spoolData = false, accurate = false, enabled = true;
	    			int maxStartDelay = 0, maxRunTime = 0, maxWaitTime = 0, priority = 0, bandwith = 0;
	    			
					if (stepGroup.getClient() == null || stepGroup.getClient().isEmpty())
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.client")+" :"+nameStep);
					
					String clientName = stepGroup.getClient();
					JobScheduledRs job = stepGroup.getJob();
					
					if (job.getLevel() == null || job.getLevel().isEmpty()) 
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.level")+" :"+nameStep);
					
					if (!clientName.equals("airback-fd") && (job.getFileset() == null || job.getFileset().isEmpty())) 
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.fileset")+" :"+nameStep);
					
					if (job.getPool() == null || job.getPool().isEmpty())
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.poolDefault")+" :"+nameStep);
					
					if (job.getMaxStartDelay() != null)
						maxStartDelay = job.getMaxStartDelay();
					if (job.getMaxRunTime() != null)
						maxRunTime = job.getMaxRunTime();
					if (job.getMaxWaitTime() != null)
						maxWaitTime = job.getMaxWaitTime();
	    			if (job.getBandwith() != null)
	    				bandwith = job.getBandwith();
	    			
					if (job.getSpooldata() != null && !job.getSpooldata().booleanValue() == true)
						spoolData = true;
					
					nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), order, clientName, nameStep);
					Map<String, Object> storageInventory = null;
					String fileset = job.getFileset();
					if (clientName.equals("airback-fd") && stepGroup.getStorageInventory() != null && !stepGroup.getStorageInventory().isEmpty()) {
						storageInventory = StorageInventoryManager.getStorage(stepGroup.getStorageInventory());
						inventoryStep = (String)  storageInventory.get("name");
						fileset = StorageInventoryManager.getNameHiddenFileset((String) storageInventory.get("name"), templateStorageType);
						if (BaculaConfiguration.existBaculaInclude("/etc/bacula/bacula-dir.conf", "@/etc/bacula/" + "filesets" + "/" + fileset + ".conf"))
							FileSetManager.updateExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), templateStorageType, nameJob), FileSetManager.COMPRESSION_NONE);
						else
							FileSetManager.addExternalStorageFileSet(fileset, RemoteStorageManager.getMountPathRemoteStorage((String) storageInventory.get("name"), templateStorageType, nameJob), FileSetManager.COMPRESSION_NONE);
					}
					
					if (fileset == null) {
						if (clientName.equals("airback-fd"))
							throw new Exception(getLanguageMessage("advanced.group.jobs.backup.exception.inventory"));
						else
							throw new Exception(getLanguageMessage("advanced.group.jobs.backup.exception.fileset"));
					}
						
					jm.setJob(nameJob, clientName, job.getLevel(), schedule, fileset, storage, job.getPool(), null, null, null, null, nextJob, verifyPreviousJob, maxStartDelay, maxRunTime, maxWaitTime, spoolData, enabled, priority, 0, bandwith, accurate, false, 0, 0);
					
					if (clientName.equals("airback-fd") && stepGroup.getStorageInventory() != null && !stepGroup.getStorageInventory().isEmpty()) {
						RemoteStorageManager.generateBackupRemoteScripts(nameJob, storageInventory, templateStorageType, iscsim, jm);
					}
					
					
				} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP) || typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
					if (stepGroup.getClient() == null || stepGroup.getClient().isEmpty())
						throw new Exception(getLanguageMessage("advanced.groupjob.error.backup.client")+" :"+nameStep);
					
					String clientName = stepGroup.getClient();
					
					ScriptProcessManager sp = new ScriptProcessManager();
					Map<String, Object> scriptProcess = sp.getScript(dataStep);
					nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), order, clientName, nameStep);
					
					jm.setEmptyJobForScripts(nameJob, clientName , nextJob, null, schedule, storage);
					
					Map<String, String> _variableValues=new HashMap<String, String>();
					if (scriptProcess!=null && scriptProcess.get("variables")!=null){
						List<Map<String, String>> _variables= (List<Map<String, String>>) scriptProcess.get("variables");
						for (Map<String, String> _variable: _variables){
							if (stepGroup.getVar(_variable.get("name"))!=null){
								_variableValues.put(_variable.get("name"),stepGroup.getVar(_variable.get("name")).getValue());
							} else{
								_variableValues.put(_variable.get("name"),"");
							}
						}
					}
					List<String> _scriptsOrder=new ArrayList<String>();
					TreeMap<String,Map<String, String>> _scriptsContent=new TreeMap<String, Map<String, String>>();
					if (scriptProcess!=null && scriptProcess.get("scripts")!=null){
						List<Map<String, String>> _scripts= (List<Map<String, String>>) scriptProcess.get("scripts");
						for (Map<String, String> _script: _scripts){
								Map<String, String> objScript = new HashMap<String, String>();
								objScript.put("order", _script.get("order"));
								objScript.put("script", ScriptProcessManager.getSmartContent(_script.get("content"), _variableValues));
								objScript.put("shell", _script.get("shell"));
								_scriptsContent.put(_script.get("order"),objScript);										
								_scriptsOrder.add(_script.get("order"));
						}
					}
					Collections.sort(_scriptsOrder);
					
					boolean before = false;
					if (scriptProcess.get("type") != null && Integer.valueOf((String) scriptProcess.get("type")) == ScriptProcessManager.BEFORE_EXECUTION)
						before = true;
					
					boolean abortOnError = false;
					if (scriptProcess.get("abortType") != null && Boolean.valueOf((String) scriptProcess.get("abortType")) == true)
						abortOnError = true;
					
					ScriptProcessManager.generateScriptsJob(nameJob, (String) scriptProcess.get("name"), jm, _scriptsContent, abortOnError, before, true, true, _variableValues);
				}
				
				if (nameJob != null) {
					Map<String, Object> job = new HashMap<String, Object>();
					job.put("name", nameJob);
					job.put("order", order);
					job.put("typeStep", typeStep);
					job.put("step", nameStep);
					if (inventoryStep != null && !inventoryStep.isEmpty())
						job.put("inventory", inventoryStep);
					jobs.put(order, job);
				}
			}
			
			GroupJobManager.saveGroupJob(config.getName(), GroupJobManager.TYPE_TEMPLATEJOB, config.getTemplateJob(), jobs, config.getSchedule(), config.getStorage());
			GroupJobManager.saveGroupJobConfig(config.getName(), config.getSchedule(), config.getStorage(), config.getTemplateJob(), config.getSteps());
			
			if (templateJob.get("groups_status") == null || templateJob.get("groups_status").equals(TemplateJobManager.GROUPS_STATUS_NO_GROUPS))
				TemplateJobManager.writeTemplateJobXml((String) templateJob.get("name"), (Map<Integer, Map<String, String>>)templateJob.get("steps"), TemplateJobManager.GROUPS_STATUS_ALL_UPDATED);

			uriCreated=config.getName()+"/configuration";
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
			// Si se produce algún fallo, borramos todos los jobs creados
			for (GroupStepRs step :  config.getSteps()) {
				try {
					String nameJob = null;
					String nameStep = step.getName();
					String typeStep = step.getType();
					if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
						nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), "airback-fd", nameStep);
					} else {
						String clientName = step.getClient();
						nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), clientName, nameStep);
					}
					
					GroupJobManager.removeHiddenFileSet(nameJob, jm);
					jm.removeJob(nameJob);
				} catch (Exception ex2) {}
			}
		} finally {
			try {
				if (BackupOperator.isBlock_reload())
					BackupOperator.setBlock_reload(false);
				BackupOperator.reload();
			} catch (Exception ex) {
				response.setError("Error: "+ex.getMessage());
				uriCreated = null;
				for (GroupStepRs step :  config.getSteps()) {
					try {
						String nameJob = null;
						String nameStep = step.getName();
						String typeStep = step.getType();
						if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
							nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), "airback-fd", nameStep);
						} else {
							String clientName = step.getClient();
							nameJob = GroupJobManager.getGroupJobNameJob(config.getName(), step.getOrder(), clientName, nameStep);
						}
						
						GroupJobManager.removeHiddenFileSet(nameJob, jm);
						jm.removeJob(nameJob);
					} catch (Exception ex2) {}
				}
			}
		}
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	private static String getNextJob(Integer order, GroupJobConfigRs configGroup, Map<Integer, Map<String, String>> steps, String groupJob, ClientManager cm) throws Exception{
		int nextOrder = order+1;
		String nextJob = null;
		Map<String, String> nextStep = steps.get(nextOrder);
		GroupStepRs nextConfigStep = configGroup.getStep(nextOrder);
		if (nextStep != null && !nextStep.isEmpty()) {
			String typeStep = nextStep.get("type");
			String nameStep = nextStep.get("name");
			if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
				nextJob = GroupJobManager.getGroupJobNameJob(groupJob, nextOrder, "airback-fd", nameStep);
			} else {
				if (nextConfigStep.getClient() != null && !nextConfigStep.getClient().isEmpty()) {
					String clientName = nextConfigStep.getClient();
					nextJob = GroupJobManager.getGroupJobNameJob(groupJob, nextOrder, clientName, nameStep);
				}
			} 
		}
		return nextJob;
	}
	
	/**
	 * Edita un grupo
	 * @param group
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editGroupJob(GroupRs group) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			uriCreated=group.getName();
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
	 * Elimina un grupo
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteGroupJob(@PathParam("name") String name,@QueryParam("erase") String erase) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			boolean er = false;
			if (erase != null && (erase.equals("true") || erase.equals("si") || erase.equals("yes") || erase.equals("ok")))
				er = true;
			
			GroupJobManager.removeGroupJob(name, er, jm, this.getConfig());
			response.setSuccess(this.getLanguageMessage("advanced.group.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
