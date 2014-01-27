package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

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

@XmlRootElement(name = "storageDrive")
public class StorageDriveRs {

	private String name;
	private String label;
	private String index;
	private String status;
	private String slot;
	
	/**
	 * Constructor vacío requerido por JAX
	 */
	public StorageDriveRs() {}
	
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( StorageDriveRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	public static StorageDriveRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "storageDrive";
				JAXBContext jc = JAXBContext.newInstance( StorageDriveRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					StorageDriveRs o = (StorageDriveRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<StorageDriveRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "storageDrive";
			List<StorageDriveRs> listObjects = new ArrayList<StorageDriveRs>();
			
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
	 * Convierte una lista de mapas de drive a objetos drive
	 * @param mapdrives
	 * @return
	 */
	public static List<StorageDriveRs> listMapToObject(List<Map<String, String>> mapdrives) {
		List<StorageDriveRs> drives = new ArrayList<StorageDriveRs>();
		if (mapdrives != null && mapdrives.size()>0) {
			for (Map<String, String> mapSt : mapdrives) {
				StorageDriveRs j = mapToObject(mapSt);
				drives.add(j);
			}
		}
		return drives;
	}
	
	
	/**
	 * Convierte un mapa de información de un drive en un objeto driveRs
	 * @param mapdrive
	 * @return
	 */
	public static StorageDriveRs mapToObject(Map<String, String> mapdrive) {
		StorageDriveRs drive = new StorageDriveRs();
		if (mapdrive.get("index") != null && !mapdrive.get("index").equals(""))
			drive.setIndex(mapdrive.get("index"));
		if (mapdrive.get("label") != null && !mapdrive.get("label").equals(""))
			drive.setLabel(mapdrive.get("label"));
		if (mapdrive.get("name") != null && !mapdrive.get("name").equals(""))
			drive.setName(mapdrive.get("name"));
		if (mapdrive.get("slot") != null && !mapdrive.get("slot").equals(""))
			drive.setSlot(mapdrive.get("slot"));
		if (mapdrive.get("status") != null && !mapdrive.get("status").equals(""))
			drive.setStatus(mapdrive.get("status"));
		return drive;
	}


	// ################# GETTERS Y SETTERS #################################
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public String getIndex() {
		return index;
	}


	public void setIndex(String index) {
		this.index = index;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getSlot() {
		return slot;
	}


	public void setSlot(String slot) {
		this.slot = slot;
	}


	
}
