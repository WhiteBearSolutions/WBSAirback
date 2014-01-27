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

import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.rs.exception.JobNotExistsException;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobArchievedRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobScheduledRs;

@Path("/jobs")
public class JobServiceRs extends WbsImagineServiceRs {

	private JobManager jobManager = null;
	private ClientManager clientManager = null;
	private BackupOperator backupOperator = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			jobManager = new JobManager(this.config);
			clientManager = new ClientManager(this.config);
			backupOperator = new BackupOperator(this.config);
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Checkeos comunes al guardar un job
	 * @param jobNew
	 * @param maxStartDelay
	 * @param maxRunTime
	 * @param maxWaitTime
	 * @param priority
	 * @param bandwith
	 * @param type
	 * @param verfyPreviousJob
	 * @param spoolData
	 * @param accurate
	 * @param enabled
	 */
	public void commonSaveJobCheckings(JobScheduledRs jobNew, Integer maxStartDelay, Integer maxRunTime, Integer maxWaitTime, Integer priority, 
			Integer bandwith, Integer type, Boolean verifyPreviousJob,  Boolean spoolData, Boolean accurate, Boolean enabled, Boolean rescheduleOnError,
			Integer rescheduleInterval, Integer rescheduleTimes) throws Exception {
		if (jobNew.getLevel() == null || jobNew.getLevel().length()<=0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.level"));
		}
		if (jobNew.getFileset() == null || jobNew.getFileset().length()<=0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.pattern"));
		}
		if (jobNew.getSchedule() == null || jobNew.getSchedule().length()<=0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.schedule"));
		}
		if (jobNew.getStorage() == null || jobNew.getStorage().length()<=0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.repository"));
		}
		if (jobNew.getPool() == null || jobNew.getPool().length()<=0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.pool"));
		}
		if (jobNew.getMaxStartDelay() != null && jobNew.getMaxStartDelay()<0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.delay"));
		} else if (jobNew.getMaxStartDelay() != null) {
			maxStartDelay = jobNew.getMaxStartDelay();
		}
		if (jobNew.getMaxRunTime() != null && jobNew.getMaxRunTime()<0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.runtime"));
		} else if (jobNew.getMaxRunTime() != null) {
			maxRunTime = jobNew.getMaxRunTime();
		}
		if (jobNew.getMaxWaitTime() != null && jobNew.getMaxWaitTime()<0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.timeout"));
		} else if (jobNew.getMaxWaitTime() != null) {
			maxWaitTime = jobNew.getMaxWaitTime();
		}
		if (jobNew.getPriority() != null && (jobNew.getPriority()<0 || jobNew.getPriority()>10) ) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.priority"));
		} else if (jobNew.getPriority() != null) {
			priority = jobNew.getPriority();
		}
		if (jobNew.getBandwith() != null && jobNew.getBandwith()<0) {
			throw new Exception(getLanguageMessage("backup.jobs.exception.bandwith"));
		} else if (jobNew.getBandwith() != null) {
			bandwith = jobNew.getBandwith();
		}
		if (jobNew.getType() != null && (jobNew.getType().equals(JobManager.TYPE_BACKUP) || jobNew.getType().equals(JobManager.TYPE_COPY) || jobNew.getType().equals(JobManager.TYPE_MIGRATE)) ) {
			type = jobNew.getType();
		}
	}
	
	
	/**
	 * Devuelve un job archivado
	 * @param jobname
	 * @return
	 */
	@Path("/archieved/{jobname}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getArchievedJob(@PathParam("jobname") String jobname) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> mapJob = jobManager.getLastArchivedJob(jobname);
			if (mapJob == null)
				throw new Exception(getLanguageMessage("backup.jobs.exception.job_not_exists"));
			
			JobArchievedRs job = JobArchievedRs.mapToObject(mapJob);
		
			response.setJobArchieved(job);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Listado de Jobs archivados de un cliente
	 * @param clientId
	 * @return
	 */
	@Path("/archieved/client/{clientid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getArchievedJobs (@PathParam("clientid") Integer clientId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, Map<String, String>> mapJobs = jobManager.getArchivedClientJobs(clientId, null, 0, 0);
			List<JobArchievedRs> jobs = new ArrayList<JobArchievedRs>();
			if (mapJobs != null && mapJobs.size()>0)
				jobs = JobArchievedRs.listMapToObject(mapJobs);
			response.setJobsArchieved(jobs);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve un job archivado
	 * @param jobname
	 * @return
	 */
	@Path("/scheduled/{jobname}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getScheduledJob(@PathParam("jobname") String jobname) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> mapJob = jobManager.getProgrammedJob(jobname);
			if (mapJob == null)
				throw new Exception(getLanguageMessage("backup.jobs.exception.job_not_exists"));
			JobScheduledRs job = JobScheduledRs.mapToObject(mapJob);
			response.setJobScheduled(job);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Listado de jobs Programados (definiciones de jobs) de un cliente
	 * @param clientId
	 * @return
	 */
	@Path("/scheduled/client/{clientid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getScheduledJobs (@PathParam("clientid") Integer clientId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			List<Map<String, String>> mapJobs = jobManager.getProgrammedClientJobs(clientId);
			List<JobScheduledRs> jobs = new ArrayList<JobScheduledRs>();
			if (mapJobs != null && mapJobs.size() > 0)
				jobs = JobScheduledRs.listMapToObject(mapJobs);
			response.setJobsScheduled(jobs);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo job
	 * @param jobNew
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewJob(JobScheduledRs jobNew) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Boolean verifyPreviousJob = false, spoolData = false, accurate = false, enabled = false, rescheduleOnError = false;
			Integer maxStartDelay = 10, maxRunTime = 72, maxWaitTime = 10, priority = 0, bandwith = 0, rescheduleInterval = 2, rescheduleTimes = 12;
			Integer type = JobManager.TYPE_BACKUP; 	
			
			// Procesamos 
			if (jobNew.getJobName() == null || jobNew.getJobName().length()<=0) {
				throw new Exception(getLanguageMessage("backup.jobs.exception.name"));
			}
			if(JobManager.existsjob(jobNew.getJobName())) {
				throw new Exception(getLanguageMessage("backup.jobs.exception.job_exist"));
			}
			
			this.commonSaveJobCheckings(jobNew, maxStartDelay, maxRunTime, maxWaitTime, priority, bandwith, type, verifyPreviousJob, spoolData, accurate, enabled, rescheduleOnError, rescheduleInterval, rescheduleTimes);
			
			if (jobNew.getVerifyPreviousJob() != null) {
				verifyPreviousJob = jobNew.getVerifyPreviousJob();
			}
			if (jobNew.getSpooldata() != null) {
				spoolData = jobNew.getSpooldata();
			}
			if (jobNew.getAccurate() != null) {
				accurate = jobNew.getAccurate();
			}
			if (jobNew.getEnabled() != null) {
				enabled = jobNew.getEnabled();
			}
			if (jobNew.getRescheduleOnError() != null) {
				rescheduleOnError = jobNew.getRescheduleOnError();
				if (rescheduleOnError) {
					if (jobNew.getRescheduleInterval() != null)
						rescheduleInterval = jobNew.getRescheduleInterval();
					if (jobNew.getRescheduleTimes() != null)
						rescheduleTimes = jobNew.getRescheduleTimes();
				}
			}
			
			jobManager.setJob(jobNew.getJobName(), jobNew.getClientName(), jobNew.getLevel(), jobNew.getSchedule(), 
					jobNew.getFileset(), jobNew.getStorage(), jobNew.getPool(), jobNew.getPoolFull(), jobNew.getPoolIncremental(),
					jobNew.getPoolDifferential(), jobNew.getHypervisorJob(), jobNew.getNextJob(), verifyPreviousJob, 
					maxStartDelay, maxRunTime, maxWaitTime, spoolData, 
					enabled, priority, type, bandwith, accurate, rescheduleOnError, rescheduleInterval, rescheduleTimes);
			
			uriCreated="/scheduled/"+jobNew.getJobName();
			
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
	 * Edita un job
	 * @param jobNew
	 * @param clientId
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editJob(JobScheduledRs jobNew) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Boolean verifyPreviousJob = false, spoolData = false, accurate = false, enabled = false, rescheduleOnError = false;
			Integer maxStartDelay = 10, maxRunTime = 72, maxWaitTime = 10, priority = 0, bandwith = 0, rescheduleInterval = 2, rescheduleTimes = 12;
			Integer type = JobManager.TYPE_BACKUP; 	
			
			// Procesamos 
			if (jobNew.getJobName() == null || jobNew.getJobName().length()<=0) {
				throw new Exception(getLanguageMessage("backup.jobs.exception.name"));
			}
			if(!JobManager.existsjob(jobNew.getJobName())) {
				throw new Exception(getLanguageMessage("backup.jobs.exception.job_not_exists"));
			}
			
			this.commonSaveJobCheckings(jobNew, maxStartDelay, maxRunTime, maxWaitTime, priority, bandwith, type, verifyPreviousJob, spoolData, accurate, enabled, rescheduleOnError, rescheduleInterval, rescheduleTimes);
			
			if (jobNew.getVerifyPreviousJob() != null) {
				verifyPreviousJob = jobNew.getVerifyPreviousJob();
			}
			if (jobNew.getSpooldata() != null) {
				spoolData = jobNew.getSpooldata();
			}
			if (jobNew.getAccurate() != null) {
				accurate = jobNew.getAccurate();
			}
			if (jobNew.getEnabled() != null) {
				enabled = jobNew.getEnabled();
			}
			if (jobNew.getRescheduleOnError() != null) {
				rescheduleOnError = jobNew.getRescheduleOnError();
				if (rescheduleOnError) {
					if (jobNew.getRescheduleInterval() != null)
						rescheduleInterval = jobNew.getRescheduleInterval();
					if (jobNew.getRescheduleTimes() != null)
						rescheduleTimes = jobNew.getRescheduleTimes();
				}
			}
			
			jobManager.setJob(jobNew.getJobName(), jobNew.getClientName(), jobNew.getLevel(), jobNew.getSchedule(), 
					jobNew.getFileset(), jobNew.getStorage(), jobNew.getPool(), jobNew.getPoolFull(), jobNew.getPoolIncremental(),
					jobNew.getPoolDifferential(), jobNew.getHypervisorJob(), jobNew.getNextJob(), verifyPreviousJob, 
					maxStartDelay, maxRunTime, maxWaitTime, spoolData, 
					enabled, priority, type, bandwith, accurate, rescheduleOnError, rescheduleInterval, rescheduleTimes);
			
			uriCreated="/scheduled/"+jobNew.getJobName();
			
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
	 * Ejecuta un job
	 * @return
	 */
	@Path("/run/{jobName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response runJob(@PathParam("jobName") String jobName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			Map<String, String> job = jobManager.getProgrammedJob(jobName);
			if (job != null && job.size()>0) {
				backupOperator.runJob(clientManager.getClientId(job.get("client")), jobName);
				response.setSuccess(getLanguageMessage("backup.jobs.rs.successfull.run"));
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un job ejecutado
	 * @return
	 */
	@Path("/archieved/{jobid}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteScheduledJob(@PathParam("jobid") Integer jobId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> job = jobManager.getJob(jobId);
			if (job != null) {
				backupOperator.deleteJob(jobId);
				response.setSuccess(this.getLanguageMessage("backup.jobs.rs.successfull.scheduled.deleted"));
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Cancela un job
	 * @return
	 */
	@Path("/cancel/{jobid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response cancelJob(@PathParam("jobid") Integer jobId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> job = jobManager.getJob(jobId);
			if (job != null) {
				backupOperator.cancelJob(jobId);
				response.setSuccess(this.getLanguageMessage("backup.jobs.rs.successfull.cancel"));
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Borra un job
	 * @return
	 */
	@Path("/scheduled/{jobname}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeJob(@PathParam("jobname") String jobName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> job = jobManager.getProgrammedJob(jobName);
			if (job != null) {
				jobManager.removeJob(jobName);
				response.setSuccess(this.getLanguageMessage("backup.jobs.rs.successfull.remove"));
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Borra un job
	 * @return
	 */
	@Path("/log/{jobid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response logJob(@PathParam("jobid") Integer jobId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> job = jobManager.getJob(jobId);
			if (job != null) {
				String slog = "";
				for(Map<String, String> log : jobManager.getJobLog(String.valueOf(job.get("clientid")), jobId)) {
					slog +="<line";
					if(log.get("error") != null && "true".equals(log.get("error"))) {
						 slog += " error=\"true\" ";
					}
					slog += " time=\""+log.get("time")+"\"";
					slog +=">";
					try {
						for(String line : log.get("text").split("\n")) {
							slog += line + "\n";
				        }
					} catch(Exception _ex) {
						throw new Exception(getLanguageMessage("backup.jobs.exception"));
					}
				    slog+="</line>";
				}
				response.setSuccess(slog);
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Stop un job
	 * @return
	 */
	@Path("/stop/{jobid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response stopJob(@PathParam("jobid") Integer jobId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> job = jobManager.getJob(jobId);
			if (job != null) {
				backupOperator.stopJob(jobId);
				response.setSuccess(this.getLanguageMessage("backup.jobs.rs.successfull.stop"));
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Reinicia un job incompleto
	 * @return
	 */
	@Path("/restart/{jobid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response restartJob(@PathParam("jobid") Integer jobId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> job = jobManager.getJob(jobId);
			if (job != null) {
				backupOperator.restartIncompleteJob(jobId);
				response.setSuccess(this.getLanguageMessage("backup.jobs.rs.successfull.restart"));
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina todos los jbos asociados a un cliente
	 * @param jobId
	 * @return
	 */
	@Path("/prune/{clientId}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response pruneJobs(@PathParam("clientId") Integer clientId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String clientName = clientManager.getClientName(clientId);
			if (clientName != null && !clientName.isEmpty()) {
				backupOperator.pruneJobs(clientId);
				response.setSuccess(this.getLanguageMessage("job.rs.successfull.prune"));
			} else {
				throw new JobNotExistsException();
			}
		} catch (JobNotExistsException ex) {
			response.setError(getLanguageMessage("backup.jobs.exception.job_not_exists"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
