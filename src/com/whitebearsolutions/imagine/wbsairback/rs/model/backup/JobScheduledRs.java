package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;

@XmlRootElement(name = "jobScheduled")
public class JobScheduledRs {

	private String jobName;
	private String clientName; 
	private String level; 
	private String schedule; 
	private String fileset; 
	private String storage; 
	private String pool; 
	private String poolFull; 
	private String poolIncremental; 
	private String poolDifferential; 
	private String hypervisorJob; 
	private String nextJob; 
	private Boolean verifyPreviousJob;
	private Integer maxStartDelay; 
	private Integer maxRunTime; 
	private Integer maxWaitTime;
	private Boolean spooldata; 
	private Boolean enabled; 
	private Integer priority; 
	private Integer type; 
	private Integer bandwith; 
	private Boolean accurate;
	private Boolean rescheduleOnError;
	private Integer rescheduleInterval;
	private Integer rescheduleTimes;
	
	/**
	 * Constructor vacío necesario para JAX
	 */
	public JobScheduledRs() {}
	
	
	/**
	 * Obtiene el string en xml del objeto
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( JobScheduledRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	
	/**
	 * Convierte una lista de mapas de jobs a una lista de objetos de jobrs 
	 * @param mapJobs
	 * @return
	 */
	public static List<JobScheduledRs> listMapToObject(List<Map<String, String>> mapJobs) {
		List<JobScheduledRs> jobs = new ArrayList<JobScheduledRs>();
		if (mapJobs != null && mapJobs.size()>0) {
			for (Map<String, String> mapJob : mapJobs) {
				JobScheduledRs j = mapToObject(mapJob);
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	public static JobScheduledRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "jobScheduled";
				JAXBContext jc = JAXBContext.newInstance( JobScheduledRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					JobScheduledRs o = (JobScheduledRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<JobScheduledRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "jobScheduled";
			List<JobScheduledRs> listObjects = new ArrayList<JobScheduledRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String deviceXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(deviceXml));
						if (list.length() > list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()) {
							list = list.substring(list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						} else {
							break;
						}
					}
				}
			}
			return listObjects;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Convierte un mapa de job a un objeto jobrs
	 * @param mapJob
	 * @return
	 */
	public static JobScheduledRs mapToObject(Map<String, String> mapJob) {
		JobScheduledRs job = new JobScheduledRs();
		if (mapJob.get("acurate") != null && !mapJob.get("acurate").equals(""))
			if (mapJob.get("acurate").equals("yes"))
				job.setAccurate(true);
			else
				job.setAccurate(false);
		if (mapJob.get("bandwith") != null && !mapJob.get("bandwith").equals(""))
			job.setBandwith(Integer.parseInt(mapJob.get("bandwith")));
		if (mapJob.get("client") != null && !mapJob.get("client").equals(""))
			job.setClientName(mapJob.get("client"));
		if (mapJob.get("enabled") != null && !mapJob.get("enabled").equals(""))
			if (mapJob.get("enabled").equals("yes"))
				job.setEnabled(true);
			else
				job.setEnabled(false);
		if (mapJob.get("fileset") != null && !mapJob.get("fileset").equals(""))
			job.setFileset(mapJob.get("fileset"));
		if (mapJob.get("hypervisorJob") != null && !mapJob.get("hypervisorJob").equals(""))
			job.setHypervisorJob(mapJob.get("hypervisorJob"));
		if (mapJob.get("name") != null && !mapJob.get("name").equals(""))
			job.setJobName(mapJob.get("name"));
		if (mapJob.get("level") != null && !mapJob.get("level").equals(""))
			job.setLevel(mapJob.get("level"));
		if (mapJob.get("max-run-time") != null && !mapJob.get("max-run-time").equals(""))
			job.setMaxRunTime(Integer.parseInt(mapJob.get("max-run-time")));
		if (mapJob.get("max-start-delay") != null && !mapJob.get("max-start-delay").equals(""))
			job.setMaxStartDelay(Integer.parseInt(mapJob.get("max-start-delay")));
		if (mapJob.get("max-wait-time") != null && !mapJob.get("max-wait-time").equals(""))
			job.setMaxWaitTime(Integer.parseInt(mapJob.get("max-wait-time")));
		if (mapJob.get("nextJob") != null && !mapJob.get("nextJob").equals(""))
			job.setNextJob(mapJob.get("nextJob"));
		if (mapJob.get("pool") != null && !mapJob.get("pool").equals(""))
			job.setPool(mapJob.get("pool"));
		if (mapJob.get("pool-differential") != null && !mapJob.get("pool-differential").equals(""))
			job.setPoolDifferential(mapJob.get("pool-differential"));
		if (mapJob.get("pool-full") != null && !mapJob.get("pool-full").equals(""))
			job.setPoolFull(mapJob.get("pool-full"));
		if (mapJob.get("pool-incremental") != null && !mapJob.get("pool-incremental").equals(""))
			job.setPoolIncremental(mapJob.get("pool-incremental"));
		if (mapJob.get("priority") != null && !mapJob.get("priority").equals(""))
			job.setPriority(Integer.parseInt(mapJob.get("priority")));
		if (mapJob.get("schedule") != null && !mapJob.get("schedule").equals(""))
			job.setSchedule(mapJob.get("schedule"));
		if (mapJob.get("spooldata") != null && !mapJob.get("spooldata").equals(""))
			if (mapJob.get("spooldata").equals("yes"))
				job.setSpooldata(true);
			else
				job.setSpooldata(false);
		if (mapJob.get("storage") != null && !mapJob.get("storage").equals(""))
			job.setStorage(mapJob.get("storage"));
		if (mapJob.get("type") != null && !mapJob.get("type").equals("")) {
			if (mapJob.get("type").equals("Migrate"))
				job.setType(JobManager.TYPE_MIGRATE);
			else if (mapJob.get("type").equals("Copy"))
				job.setType(JobManager.TYPE_COPY);
			else
				job.setType(JobManager.TYPE_BACKUP);
		} else {
			job.setType(JobManager.TYPE_BACKUP);
		}
		if (mapJob.get("verifyPreviousJob") != null && !mapJob.get("verifyPreviousJob").equals(""))
			if (mapJob.get("verifyPreviousJob").equals("yes"))
				job.setVerifyPreviousJob(true);
			else
				job.setVerifyPreviousJob(false);
		if (mapJob.get("reschedule-on-error") != null && !mapJob.get("reschedule-on-error").equals(""))
			if (mapJob.get("reschedule-on-error").equals("yes")) {
				job.setRescheduleOnError(true);
				if (mapJob.get("reschedule-interval") != null && !mapJob.get("reschedule-interval").equals("")) {
					job.setRescheduleInterval(Integer.parseInt(mapJob.get("reschedule-interval")));
				} else
					job.setRescheduleInterval(2);
				if (mapJob.get("reschedule-times") != null && !mapJob.get("reschedule-times").equals("")) {
					job.setRescheduleInterval(Integer.parseInt(mapJob.get("reschedule-times")));
				} else
					job.setRescheduleInterval(12);
			} else
				job.setVerifyPreviousJob(false);
			
		return job;
	}
	
	public static JobScheduledRs mapObjectToObject(Map<String, Object> mapJob) {
		JobScheduledRs job = new JobScheduledRs();
		if (mapJob.get("acurate") != null && !((String)mapJob.get("acurate")).equals(""))
			if (mapJob.get("acurate").equals("yes"))
				job.setAccurate(true);
			else
				job.setAccurate(false);
		if (mapJob.get("bandwith") != null && !((String)mapJob.get("bandwith")).equals(""))
			job.setBandwith(Integer.valueOf((String)mapJob.get("bandwith")));
		if (mapJob.get("client") != null && !((String)mapJob.get("client")).equals(""))
			job.setClientName((String)mapJob.get("client"));
		if (mapJob.get("enabled") != null && !mapJob.get("enabled").equals(""))
			if (mapJob.get("enabled").equals("yes"))
				job.setEnabled(true);
			else
				job.setEnabled(false);
		if (mapJob.get("fileset") != null && !((String)mapJob.get("fileset")).equals(""))
			job.setFileset((String)mapJob.get("fileset"));
		if (mapJob.get("hypervisorJob") != null && !((String)mapJob.get("hypervisorJob")).equals(""))
			job.setHypervisorJob((String)mapJob.get("hypervisorJob"));
		if (mapJob.get("name") != null && !((String)mapJob.get("name")).equals(""))
			job.setJobName((String)mapJob.get("name"));
		if (mapJob.get("level") != null && !((String)mapJob.get("level")).equals(""))
			job.setLevel((String)mapJob.get("level"));
		if (mapJob.get("max-run-time") != null && !((String)mapJob.get("max-run-time")).equals(""))
			job.setMaxRunTime(Integer.valueOf((String)mapJob.get("max-run-time")));
		if (mapJob.get("max-start-delay") != null && !((String)mapJob.get("max-start-delay")).equals(""))
			job.setMaxStartDelay(Integer.valueOf((String)mapJob.get("max-start-delay")));
		if (mapJob.get("max-wait-time") != null && !((String)mapJob.get("max-wait-time")).equals(""))
			job.setMaxWaitTime(Integer.valueOf((String)mapJob.get("max-wait-time")));
		if (mapJob.get("nextJob") != null && !((String)mapJob.get("nextJob")).equals(""))
			job.setNextJob((String)mapJob.get("nextJob"));
		if (mapJob.get("pool") != null && !((String)mapJob.get("pool")).equals(""))
			job.setPool((String)mapJob.get("pool"));
		if (mapJob.get("pool-differential") != null && !((String)mapJob.get("pool-differential")).equals(""))
			job.setPoolDifferential((String)mapJob.get("pool-differential"));
		if (mapJob.get("pool-full") != null && !((String)mapJob.get("pool-full")).equals(""))
			job.setPoolFull((String)mapJob.get("pool-full"));
		if (mapJob.get("pool-incremental") != null && !((String)mapJob.get("pool-incremental")).equals(""))
			job.setPoolIncremental((String)mapJob.get("pool-incremental"));
		if (mapJob.get("priority") != null && !((String)mapJob.get("priority")).equals(""))
			job.setPriority(Integer.parseInt((String)mapJob.get("priority")));
		if (mapJob.get("schedule") != null && !((String)mapJob.get("schedule")).equals(""))
			job.setSchedule((String)mapJob.get("schedule"));
		if (mapJob.get("spooldata") != null && !((String)mapJob.get("spooldata")).equals(""))
			if (mapJob.get("spooldata").equals("yes"))
				job.setSpooldata(true);
			else
				job.setSpooldata(false);
		if (mapJob.get("storage") != null && !((String)mapJob.get("storage")).equals(""))
			job.setStorage((String)mapJob.get("storage"));
		if (mapJob.get("type") != null && !((String)mapJob.get("type")).equals("")) {
			if (mapJob.get("type").equals("Migrate"))
				job.setType(JobManager.TYPE_MIGRATE);
			else if (mapJob.get("type").equals("Copy"))
				job.setType(JobManager.TYPE_COPY);
			else
				job.setType(JobManager.TYPE_BACKUP);
		} else {
			job.setType(JobManager.TYPE_BACKUP);
		}
		if (mapJob.get("verifyPreviousJob") != null && !((String)mapJob.get("verifyPreviousJob")).equals(""))
			if (mapJob.get("verifyPreviousJob").equals("yes"))
				job.setVerifyPreviousJob(true);
			else
				job.setVerifyPreviousJob(false);
			
		return job;
	}
	
	@XmlElement(required=true) 
	public String getJobName() {
		return jobName;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	@XmlElement(required=true) 
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	@XmlElement(required=true) 
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	
	@XmlElement(required=true) 
	public String getSchedule() {
		return schedule;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
	
	@XmlElement(required=true)
	public String getFileset() {
		return fileset;
	}
	public void setFileset(String fileset) {
		this.fileset = fileset;
	}
	
	@XmlElement(required=true) 
	public String getStorage() {
		return storage;
	}
	public void setStorage(String storage) {
		this.storage = storage;
	}
	
	@XmlElement 
	public String getPool() {
		return pool;
	}
	public void setPool(String pool) {
		this.pool = pool;
	}
	
	@XmlElement(required=false) 
	public String getPoolFull() {
		return poolFull;
	}
	public void setPoolFull(String poolFull) {
		this.poolFull = poolFull;
	}
	
	@XmlElement(required=false) 
	public String getPoolIncremental() {
		return poolIncremental;
	}
	public void setPoolIncremental(String poolIncremental) {
		this.poolIncremental = poolIncremental;
	}
	
	@XmlElement(required=false)
	public String getPoolDifferential() {
		return poolDifferential;
	}
	public void setPoolDifferential(String poolDifferential) {
		this.poolDifferential = poolDifferential;
	}
	
	@XmlElement(required=false)
	public String getHypervisorJob() {
		return hypervisorJob;
	}
	public void setHypervisorJob(String hypervisorJob) {
		this.hypervisorJob = hypervisorJob;
	}
	
	@XmlElement(required=false)
	public String getNextJob() {
		return nextJob;
	}
	public void setNextJob(String nextJob) {
		this.nextJob = nextJob;
	}
	
	@XmlElement(required=false)
	public Boolean getVerifyPreviousJob() {
		return verifyPreviousJob;
	}
	public void setVerifyPreviousJob(Boolean verifyPreviousJob) {
		this.verifyPreviousJob = verifyPreviousJob;
	}
	
	@XmlElement(required=false)
	public Integer getMaxStartDelay() {
		return maxStartDelay;
	}
	public void setMaxStartDelay(Integer maxStartDelay) {
		this.maxStartDelay = maxStartDelay;
	}
	
	@XmlElement(required=false)
	public Integer getMaxRunTime() {
		return maxRunTime;
	}
	public void setMaxRunTime(Integer maxRunTime) {
		this.maxRunTime = maxRunTime;
	}
	
	@XmlElement(required=false)
	public Integer getMaxWaitTime() {
		return maxWaitTime;
	}
	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
	
	@XmlElement(required=false)
	public Boolean getSpooldata() {
		return spooldata;
	}
	public void setSpooldata(Boolean spooldata) {
		this.spooldata = spooldata;
	}
	
	@XmlElement(required=false)
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	@XmlElement(required=false)
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	@XmlElement(required=false)
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	@XmlElement(required=false)
	public Integer getBandwith() {
		return bandwith;
	}
	public void setBandwith(Integer bandwith) {
		this.bandwith = bandwith;
	}
	
	@XmlElement(required=false) 
	public Boolean getAccurate() {
		return accurate;
	}
	public void setAccurate(Boolean accurate) {
		this.accurate = accurate;
	}


	public Boolean getRescheduleOnError() {
		return rescheduleOnError;
	}


	public void setRescheduleOnError(Boolean rescheduleOnError) {
		this.rescheduleOnError = rescheduleOnError;
	}


	public Integer getRescheduleInterval() {
		return rescheduleInterval;
	}


	public void setRescheduleInterval(Integer rescheduleInterval) {
		this.rescheduleInterval = rescheduleInterval;
	}


	public Integer getRescheduleTimes() {
		return rescheduleTimes;
	}


	public void setRescheduleTimes(Integer rescheduleTimes) {
		this.rescheduleTimes = rescheduleTimes;
	}
}
