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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "snapshot")
public class SnapshotRs {

	private String name;
	private String used;
	private Double usedRaw;
	private String schedule;
	
	public SnapshotRs(){}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( SnapshotRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de snapshots a objetos snapshot
	 * @param maps
	 * @return
	 */
	public static List<SnapshotRs> listMapToObject(List<Map<String, String>> mapSnapshots) throws Exception {
		List<SnapshotRs> vols = new ArrayList<SnapshotRs>();
		if (mapSnapshots != null && mapSnapshots.size()>0) {
			for (Map<String, String> mapSnap : mapSnapshots) {
				SnapshotRs vol = mapToObject(mapSnap);
				vols.add(vol);
			}
		}
		return vols;
	}
	
	public static SnapshotRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "snapshot";
				JAXBContext jc = JAXBContext.newInstance( SnapshotRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					SnapshotRs o = (SnapshotRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<SnapshotRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "snapshot";
			List<SnapshotRs> listObjects = new ArrayList<SnapshotRs>();
			
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
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static SnapshotRs mapToObject(Map<String, String> mapSnap) throws Exception {
		SnapshotRs snapshot = new SnapshotRs();
		
		if (mapSnap.get("name") != null && !mapSnap.get("name").isEmpty())
			snapshot.setName(mapSnap.get("name"));
		if (mapSnap.get("schedule") != null && !mapSnap.get("schedule").isEmpty())
			snapshot.setSchedule(mapSnap.get("schedule"));
		if (mapSnap.get("used") != null && !mapSnap.get("used").isEmpty())
			snapshot.setUsed(mapSnap.get("used"));
		if (mapSnap.get("used-raw") != null && !mapSnap.get("used-raw").isEmpty())
			snapshot.setUsedRaw(Double.parseDouble(mapSnap.get("used-raw")));
		
		return snapshot;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsed() {
		return used;
	}

	public void setUsed(String used) {
		this.used = used;
	}

	public Double getUsedRaw() {
		return usedRaw;
	}

	public void setUsedRaw(Double usedRaw) {
		this.usedRaw = usedRaw;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
}
