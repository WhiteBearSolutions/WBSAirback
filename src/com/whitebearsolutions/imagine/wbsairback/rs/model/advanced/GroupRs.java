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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "group")
public class GroupRs {

	private String name;
	private String schedule;
	private String storage;
	private String start;
	private String end;
	private String status;
	private String alert;
	private List<JobGroupRs> jobs;
	
	
	public GroupRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( GroupRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de groups a objetos group
	 * @param mapPools
	 * @return
	 */
	public static List<GroupRs> listMapToObject(List<Map<String, Object>> mapGroupJobs) throws Exception {
		List<GroupRs> groups = new ArrayList<GroupRs>();
		if (mapGroupJobs != null && mapGroupJobs.size()>0) {
			for (Map<String, Object> mapGroup : mapGroupJobs) {
				GroupRs group = mapToObject(mapGroup);
				groups.add(group);
			}
		}
		return groups;
	}
	
	public static GroupRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "group";
				JAXBContext jc = JAXBContext.newInstance( GroupRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					GroupRs o = (GroupRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<GroupRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "group";
			List<GroupRs> listObjects = new ArrayList<GroupRs>();
			
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
	public static GroupRs mapToObject(Map<String, Object> map) throws Exception {
		GroupRs group = new GroupRs();
		
		Object p = map.get("alert");
		if (p != null && !((String)p).isEmpty())
			group.setAlert((String) p);
		p = map.get("end");
		if (p != null && !((String)p).isEmpty())
			group.setEnd((String) p);
		p = map.get("jobs");
		if (p != null) {
			@SuppressWarnings("unchecked")
			Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) map.get("jobs");
			group.setJobs(JobGroupRs.listMapToObject(jobs));
		}
		p = map.get("name");
		if (p != null && !((String)p).isEmpty())
			group.setName((String) p);
		p = map.get("storage");
		if (p != null && !((String)p).isEmpty())	
			group.setStorage((String) p);
		p = map.get("schedule");
		if (p != null && !((String)p).isEmpty())
			group.setSchedule((String) p);
		p = map.get("start");
		if (p != null && !((String)p).isEmpty())
			group.setStart((String) p);
		p = map.get("status");
		if (p != null && !((String)p).isEmpty())
			group.setStatus((String) p);
		
		return group;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}

	@XmlElementWrapper(name="jobs")
	@XmlElementRef()
	public List<JobGroupRs> getJobs() {
		return jobs;
	}

	public void setJobs(List<JobGroupRs> jobs) {
		this.jobs = jobs;
	}
}
