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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "templateJob")
public class TemplateJobRs {
	
	private String name;
	private String groupStatus;
	private List<TemplateStepRs> steps;

	public TemplateJobRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( TemplateJobRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de templates a objetos template
	 * @param mapPools
	 * @return
	 */
	public static List<TemplateJobRs> listMapToObject(List<Map<String, Object>> mapTemplateJobs) throws Exception {
		List<TemplateJobRs> templates = new ArrayList<TemplateJobRs>();
		if (mapTemplateJobs != null && mapTemplateJobs.size()>0) {
			for (Map<String, Object> mapTemplate : mapTemplateJobs) {
				TemplateJobRs template = mapToObject(mapTemplate);
				templates.add(template);
			}
		}
		return templates;
	}
	
	public static TemplateJobRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "templateJob";
				JAXBContext jc = JAXBContext.newInstance( TemplateJobRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					TemplateJobRs o = (TemplateJobRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<TemplateJobRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "templateJob";
			List<TemplateJobRs> listObjects = new ArrayList<TemplateJobRs>();
			
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
	public static TemplateJobRs mapToObject(Map<String, Object> map) throws Exception {
		TemplateJobRs template = new TemplateJobRs();
		Object p = map.get("name");
		if (p != null && !((String)p).isEmpty())
			template.setName((String)p);
		p = map.get("group_status");
		if (p != null && !((String)p).isEmpty())
			template.setGroupStatus((String)p);
		p = map.get("steps");
		if (p != null) {
			@SuppressWarnings("unchecked")
			Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) map.get("steps");
			if (!steps.isEmpty()) {
				template.setSteps(TemplateStepRs.listMapToObject(steps));
			}
		}
		
		return template;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupStatus() {
		return groupStatus;
	}

	public void setGroupStatus(String groupStatus) {
		this.groupStatus = groupStatus;
	}

	@XmlElementWrapper(name="templateSteps",required=true)
	@XmlElementRef()
	public List<TemplateStepRs> getSteps() {
		return steps;
	}

	public void setSteps(List<TemplateStepRs> steps) {
		this.steps = steps;
	}
}
