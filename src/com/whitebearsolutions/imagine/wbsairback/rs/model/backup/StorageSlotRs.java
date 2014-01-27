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

@XmlRootElement(name="storageSlot")
public class StorageSlotRs {

	private String status;
	private String name;
	private String value;
	
	public StorageSlotRs() {}
	
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
	
	
	public static List<StorageSlotRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "storageSlot";
			List<StorageSlotRs> listObjects = new ArrayList<StorageSlotRs>();
			
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
	 * Obtiene el objeto desde un xml valido
	 * @param xml
	 * @return
	 */
	public static StorageSlotRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( StorageSlotRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			StorageSlotRs o = (StorageSlotRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<storageSlot>"), xml.indexOf("</storageSlot>")+"</storageSlot>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	/**
	 * Convierte una lista de mapas de slot a objetos slot
	 * @param mapslots
	 * @return
	 */
	public static List<StorageSlotRs> listMapToObject(List<Map<String, String>> mapslots) {
		List<StorageSlotRs> slots = new ArrayList<StorageSlotRs>();
		if (mapslots != null && mapslots.size()>0) {
			for (Map<String, String> mapSt : mapslots) {
				StorageSlotRs j = mapToObject(mapSt);
				slots.add(j);
			}
		}
		return slots;
	}
	
	
	/**
	 * Convierte un mapa de información de un slot en un objeto slotRs
	 * @param mapslot
	 * @return
	 */
	public static StorageSlotRs mapToObject(Map<String, String> mapslot) {
		StorageSlotRs slot = new StorageSlotRs();
		if (mapslot.get("name") != null && !mapslot.get("name").equals(""))
			slot.setName(mapslot.get("name"));
		if (mapslot.get("status") != null && !mapslot.get("status").equals(""))
			slot.setStatus(mapslot.get("status"));
		if (mapslot.get("value") != null && !mapslot.get("value").equals(""))
			slot.setValue(mapslot.get("value"));
		return slot;
	}

	
	// ################# GETTERS Y SETTERS #################################
	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}



}
