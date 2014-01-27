package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "groupJobConfig")
public class GroupJobConfigRs {

	private String name;
	private String schedule;
	private String storage;
	private String templateJob;
	private List<GroupStepRs> steps;
	
	
	public GroupJobConfigRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( GroupJobConfigRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	public static GroupJobConfigRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "groupJobConfig";
				JAXBContext jc = JAXBContext.newInstance( GroupJobConfigRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					GroupJobConfigRs o = (GroupJobConfigRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<GroupJobConfigRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "groupJobConfig";
			List<GroupJobConfigRs> listObjects = new ArrayList<GroupJobConfigRs>();
			
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
	
	public GroupStepRs getStep(Integer order) throws Exception {
		if (this.steps != null && !this.steps.isEmpty() && order != null && order > 0) {
			for (GroupStepRs step : this.steps) {
				if (step.getOrder() != null && step.getOrder().intValue() == order.intValue()) {
					return step;
				}
			}
		}
		return null;
	}

	// GETTERS AND SETTERS
	
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


	@XmlElementWrapper(name="steps")
	@XmlElementRef()
	public List<GroupStepRs> getSteps() {
		return steps;
	}

	public void setSteps(List<GroupStepRs> steps) {
		this.steps = steps;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	public String getTemplateJob() {
		return templateJob;
	}

	public void setTemplateJob(String templateJob) {
		this.templateJob = templateJob;
	}
}
