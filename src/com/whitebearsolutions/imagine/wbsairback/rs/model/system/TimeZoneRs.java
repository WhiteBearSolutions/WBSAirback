package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name="timeZone")
public class TimeZoneRs {

	private String zone;
	
	public TimeZoneRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeZoneRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static TimeZoneRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeZoneRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			TimeZoneRs o = (TimeZoneRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<timeZone>"), xml.indexOf("</timeZone>")+"</timeZone>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<TimeZoneRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "timeZone";
			List<TimeZoneRs> listObjects = new ArrayList<TimeZoneRs>();
			
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
	 * Convierte una lista de mapas de timeZonee a objetos timeZoners
	 * @param mapTimeZones
	 * @return
	 */
	public static List<TimeZoneRs> listMapToObject(List<String> listZones) {
		List<TimeZoneRs> zones = new ArrayList<TimeZoneRs>();
		if (listZones != null && listZones.size()>0) {
			for (String zone : listZones) {
				TimeZoneRs j = mapToObject(zone);
				zones.add(j);
			}
		}
		return zones;
	}
	
	
	/**
	 * Convierte un mapa de timeZonee a un objeto timeZoners
	 * @param mapTimeZone
	 * @return
	 */
	public static TimeZoneRs mapToObject(String name) {
		TimeZoneRs timeZone = new TimeZoneRs();
		timeZone.setZone(name);
		return timeZone;
	}
	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
	
	
}
