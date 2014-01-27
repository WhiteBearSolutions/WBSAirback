package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

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

@XmlRootElement(name="role")
public class RoleRs {

	private String name;
	private String description;
	
	public RoleRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( RoleRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static RoleRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( RoleRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			RoleRs o = (RoleRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<role>"), xml.indexOf("</role>")+"</role>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<RoleRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "role";
			List<RoleRs> listObjects = new ArrayList<RoleRs>();
			
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
	 * Convierte una lista de mapas de rolee a objetos rolers
	 * @param mapRoles
	 * @return
	 */
	public static List<RoleRs> listMapToObject(List<Map<String, String>> mapRoles) {
		List<RoleRs> jobs = new ArrayList<RoleRs>();
		if (mapRoles != null && mapRoles.size()>0) {
			for (Map<String, String> mapRole : mapRoles) {
				RoleRs j = mapToObject(mapRole);
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	
	/**
	 * Convierte un mapa de rolee a un objeto rolers
	 * @param mapRole
	 * @return
	 */
	public static RoleRs mapToObject(Map<String, String> mapRole) {
		RoleRs role = new RoleRs();
		
		if (mapRole.get("description") != null && !mapRole.get("description").equals(""))
			role.setDescription(mapRole.get("description"));
		if (mapRole.get("name") != null && !mapRole.get("name").equals(""))
			role.setName(mapRole.get("name"));
		
		return role;
	}

	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}