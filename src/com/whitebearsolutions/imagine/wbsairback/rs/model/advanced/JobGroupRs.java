package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobArchievedRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobScheduledRs;

@XmlRootElement(name = "jobGroup")
public class JobGroupRs {

	private String name;
	private Integer order;
	private JobArchievedRs lastJob;
	private JobScheduledRs jobConfiguration;
	
	public JobGroupRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( JobGroupRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de jobs a objetos job
	 * @param mapPools
	 * @return
	 */
	public static List<JobGroupRs> listMapToObject(Map<Integer, Map<String, Object>> mapJobOfGroups) throws Exception {
		List<JobGroupRs> jobs = new ArrayList<JobGroupRs>();
		if (mapJobOfGroups != null && mapJobOfGroups.size()>0) {
			for (Integer order : mapJobOfGroups.keySet()) {
				JobGroupRs job = mapToObject(order, mapJobOfGroups.get(order));
				jobs.add(job);
			}
		}
		return jobs;
	}
	
	public static JobGroupRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "jobGroup";
				JAXBContext jc = JAXBContext.newInstance( JobGroupRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					JobGroupRs o = (JobGroupRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<JobGroupRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "jobGroup";
			List<JobGroupRs> listObjects = new ArrayList<JobGroupRs>();
			
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
	 * Convierte un mapa de valores de un pool a un objeto Pool 
	 * @param mapPool
	 * @return
	 */
	public static JobGroupRs mapToObject(Integer order, Map<String, Object> map) throws Exception {
		JobGroupRs job = new JobGroupRs();
		job.setOrder(order);
		job.setLastJob(JobArchievedRs.mapObjectToObject(map));
		job.setJobConfiguration(JobScheduledRs.mapObjectToObject(map));
		return job;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public JobArchievedRs getLastJob() {
		return lastJob;
	}

	public void setLastJob(JobArchievedRs lastJob) {
		this.lastJob = lastJob;
	}

	public JobScheduledRs getJobConfiguration() {
		return jobConfiguration;
	}

	public void setJobConfiguration(JobScheduledRs jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
