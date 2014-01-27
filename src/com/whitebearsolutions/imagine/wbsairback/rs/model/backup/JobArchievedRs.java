package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name="jobArchieved")
public class JobArchievedRs {

	private String id;
	private String name;
	private String type;
	private String level;
	private String status;
	private String alert;
	private String returned;
	private String spool;
	private String errors;
	private String starttime;
	private String endtime;
	private Integer files;
	private String size;
	
	public JobArchievedRs(){}
	
	public static List<JobArchievedRs> listMapToObject(Map<String, Map<String, String>> mapJobs) {
		List<JobArchievedRs> jobs = new ArrayList<JobArchievedRs>();
		if (mapJobs != null && mapJobs.size()>0) {
			for (Map<String, String> mapJob : mapJobs.values()) {
				JobArchievedRs j = mapToObject(mapJob);
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	public static List<JobArchievedRs> listMapToObject(List<Map<String, String>> mapJobs) {
		List<JobArchievedRs> jobs = new ArrayList<JobArchievedRs>();
		if (mapJobs != null && mapJobs.size()>0) {
			for (Map<String, String> mapJob : mapJobs) {
				JobArchievedRs j = mapToObject(mapJob);
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	public static JobArchievedRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "jobArchieved";
				JAXBContext jc = JAXBContext.newInstance( JobArchievedRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					JobArchievedRs o = (JobArchievedRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<JobArchievedRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "jobArchieved";
			List<JobArchievedRs> listObjects = new ArrayList<JobArchievedRs>();
			
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
	
	public static JobArchievedRs mapToObject(Map<String, String> mapJob) {
		JobArchievedRs j = new JobArchievedRs();
		if (mapJob.get("id") != null)
			j.setId(mapJob.get("id"));
		if (mapJob.get("name") != null)
			j.setName(mapJob.get("name"));
		if (mapJob.get("type") != null)
			j.setType(mapJob.get("type"));
		if (mapJob.get("level") != null)
			j.setLevel(mapJob.get("level"));
		if (mapJob.get("status") != null)
			j.setStatus(mapJob.get("status"));
		if (mapJob.get("alert") != null)
			j.setAlert(mapJob.get("alert"));
		if (mapJob.get("return") != null)
			j.setReturned(mapJob.get("return"));
		if (mapJob.get("spool") != null)
			j.setSpool(mapJob.get("spool"));
		if (mapJob.get("errors") != null)
			j.setErrors(mapJob.get("errors"));
		if (mapJob.get("starttime") != null)
			j.setStarttime(mapJob.get("starttime"));
		if (mapJob.get("endtime") != null)
			j.setEndtime(mapJob.get("endtime"));
		if (mapJob.get("files") != null)
			j.setFiles(Integer.parseInt(mapJob.get("files")));
		if (mapJob.get("size") != null)
			j.setSize(mapJob.get("size"));
		return j;
	}
	
	public static JobArchievedRs mapObjectToObject(Map<String, Object> mapJob) {
		JobArchievedRs j = new JobArchievedRs();
		if (mapJob.get("id") != null)
			j.setId((String) mapJob.get("id"));
		if (mapJob.get("name") != null)
			j.setName((String) mapJob.get("name"));
		if (mapJob.get("type") != null)
			j.setType((String) mapJob.get("type"));
		if (mapJob.get("level") != null)
			j.setLevel((String) mapJob.get("level"));
		if (mapJob.get("status") != null)
			j.setStatus((String) mapJob.get("status"));
		if (mapJob.get("alert") != null)
			j.setAlert((String) mapJob.get("alert"));
		if (mapJob.get("return") != null)
			j.setReturned((String) mapJob.get("return"));
		if (mapJob.get("spool") != null)
			j.setSpool((String) mapJob.get("spool"));
		if (mapJob.get("errors") != null)
			j.setErrors((String) mapJob.get("errors"));
		if (mapJob.get("starttime") != null)
			j.setStarttime((String) mapJob.get("starttime"));
		if (mapJob.get("endtime") != null)
			j.setEndtime((String) mapJob.get("endtime"));
		if (mapJob.get("files") != null)
			j.setFiles(Integer.parseInt((String) mapJob.get("files")));
		if (mapJob.get("size") != null)
			j.setSize((String) mapJob.get("size"));
		return j;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getAlert() {
		return alert;
	}
	public void setAlert(String alert) {
		this.alert = alert;
	}
	public String getReturned() {
		return returned;
	}
	public void setReturned(String returned) {
		this.returned = returned;
	}
	public String getSpool() {
		return spool;
	}
	public void setSpool(String spool) {
		this.spool = spool;
	}
	public String getErrors() {
		return errors;
	}
	public void setErrors(String errors) {
		this.errors = errors;
	}
	public Integer getFiles() {
		return files;
	}
	public void setFiles(Integer files) {
		this.files = files;
	}
}
