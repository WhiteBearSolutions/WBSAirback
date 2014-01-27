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

@XmlRootElement(name="timeServer")
public class TimeServerRs {

	private String server;
	
	public TimeServerRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeServerRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static TimeServerRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeServerRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			TimeServerRs o = (TimeServerRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<timeServer>"), xml.indexOf("</timeServer>")+"</timeServer>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<TimeServerRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "timeServer";
			List<TimeServerRs> listObjects = new ArrayList<TimeServerRs>();
			
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
	 * Convierte una lista de mapas de timeServere a objetos timeServerrs
	 * @param mapTimeServers
	 * @return
	 */
	public static List<TimeServerRs> listMapToObject(List<String> listServers) {
		List<TimeServerRs> servers = new ArrayList<TimeServerRs>();
		if (listServers != null && listServers.size()>0) {
			for (String server : listServers) {
				TimeServerRs j = mapToObject(server);
				servers.add(j);
			}
		}
		return servers;
	}
	
	
	/**
	 * Convierte un mapa de timeServere a un objeto timeServerrs
	 * @param mapTimeServer
	 * @return
	 */
	public static TimeServerRs mapToObject(String name) {
		TimeServerRs timeServer = new TimeServerRs();
		timeServer.setServer(name);
		return timeServer;
	}
	
	// ################# GETTERS Y SETTERS #################################
	
	
	@XmlElement(required=true)
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	
}
