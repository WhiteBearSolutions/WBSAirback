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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "step")
public class StepRs {
	
	private String name;
	private String type;
	
	public StepRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( StepRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	/**
	 * Convierte una lista de mapas de steps a objetos step
	 * @param mapPools
	 * @return
	 */
	public static List<StepRs> listMapToObject(List<Map<String, String>> mapSteps) throws Exception {
		List<StepRs> steps = new ArrayList<StepRs>();
		if (mapSteps != null && mapSteps.size()>0) {
			for (Map<String, String> mapDev : mapSteps) {
				StepRs step = mapToObject(mapDev);
				steps.add(step);
			}
		}
		return steps;
	}
	
	public static StepRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "step";
				JAXBContext jc = JAXBContext.newInstance( StepRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					StepRs o = (StepRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<StepRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "step";
			List<StepRs> listObjects = new ArrayList<StepRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String stepXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(stepXml));
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
	 * Convierte un mapa a un objeto
	 * @param mapPool
	 * @return
	 */
	public static StepRs mapToObject(Map<String, String> map) throws Exception {
		StepRs step = new StepRs();
		String p = map.get("name");
		if (p != null && !p.isEmpty())
			step.setName(p);
		p = map.get("type");
		if (p != null && !p.isEmpty())
			step.setType(p);
		
		return step;
	}
	

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(required=true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
