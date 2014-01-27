package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

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

@XmlRootElement(name = "replicationDestination")
public class ReplicationDestinationRs {

	private String vg;
	private String lv;
	private String address;
	private String password;
	private Boolean filesystem;
	
	public ReplicationDestinationRs(){}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ReplicationDestinationRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de replicationDestination a objetos replicationDestination
	 * @param maps
	 * @return
	 */
	public static List<ReplicationDestinationRs> listMapToObject(List<Map<String, String>> maps) throws Exception {
		List<ReplicationDestinationRs> replicationDestinations = new ArrayList<ReplicationDestinationRs>();
		if (maps != null && maps.size()>0) {
			for (Map<String, String> map : maps) {
				ReplicationDestinationRs replicationDestination = mapToObject(map);
				replicationDestinations.add(replicationDestination);
			}
		}
		return replicationDestinations;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static ReplicationDestinationRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( ReplicationDestinationRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			ReplicationDestinationRs o = (ReplicationDestinationRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<replicationDestination>"), xml.indexOf("</replicationDestination>")+"</replicationDestination>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	
	public static List<ReplicationDestinationRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "replicationDestination";
			List<ReplicationDestinationRs> listObjects = new ArrayList<ReplicationDestinationRs>();
			
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
	 * Convierte un mapa de valores a un objeto  
	 * @param map
	 * @return
	 */
	public static ReplicationDestinationRs mapToObject(Map<String, String> map) throws Exception {
		ReplicationDestinationRs replicationDestination = new ReplicationDestinationRs();
		
		if (map.get("address") != null && !map.get("address").isEmpty())
			replicationDestination.setAddress(map.get("address"));
		if (map.get("filesystem") != null && !map.get("filesystem").isEmpty() && map.get("filesystem").equals("yes"))
			replicationDestination.setFilesystem(true);
		else
			replicationDestination.setFilesystem(false);
		if (map.get("lv") != null && !map.get("lv").isEmpty())
			replicationDestination.setLv(map.get("lv"));
		if (map.get("password") != null && !map.get("password").isEmpty())
			replicationDestination.setPassword(map.get("password"));
		if (map.get("vg") != null && !map.get("vg").isEmpty())
			replicationDestination.setVg(map.get("vg"));
		
		return replicationDestination;
	}

	
	// ####### GETTERS Y SETTERS #################################
	

	@XmlElement(required=true)
	public String getVg() {
		return vg;
	}

	public void setVg(String vg) {
		this.vg = vg;
	}

	@XmlElement(required=true)
	public String getLv() {
		return lv;
	}

	public void setLv(String lv) {
		this.lv = lv;
	}

	@XmlElement(required=true)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@XmlElement(required=true)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getFilesystem() {
		return filesystem;
	}

	public void setFilesystem(Boolean filesystem) {
		this.filesystem = filesystem;
	}
}
