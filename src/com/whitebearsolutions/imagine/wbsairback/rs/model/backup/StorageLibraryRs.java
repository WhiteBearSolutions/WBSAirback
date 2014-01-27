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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "storageLibrary")
public class StorageLibraryRs {

	private String name;
	private String drive;
	private String format;
	private String spool;
	private Integer spoolSize;
	private String unitsSize;
	private String netInterface;
	private String address;
	private List<StorageLibraryTapeRs> tapes;
	
	/**
	 * Constructor vacío requerido por JAX
	 */
	public StorageLibraryRs() {}
	
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( StorageLibraryRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	public static StorageLibraryRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "storageLibrary";
				JAXBContext jc = JAXBContext.newInstance( StorageLibraryRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					StorageLibraryRs o = (StorageLibraryRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<StorageLibraryRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "storageLibrary";
			List<StorageLibraryRs> listObjects = new ArrayList<StorageLibraryRs>();
			
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
	 * Convierte una lista de mapas de storage a objetos storage
	 * @param mapStorages
	 * @return
	 */
	public static List<StorageLibraryRs> listMapToObject(List<Map<String, String>> mapStorages) {
		List<StorageLibraryRs> storages = new ArrayList<StorageLibraryRs>();
		if (mapStorages != null && mapStorages.size()>0) {
			for (Map<String, String> mapSt : mapStorages) {
				StorageLibraryRs j = mapToObject(mapSt);
				storages.add(j);
			}
		}
		return storages;
	}
	
	
	/**
	 * Convierte un mapa de información de un storage en un objeto storageRs
	 * @param mapStorage
	 * @return
	 */
	public static StorageLibraryRs mapToObject(Map<String, String> mapStorage) {
		StorageLibraryRs storage = new StorageLibraryRs();
		if (mapStorage.get("address") != null && !mapStorage.get("address").equals(""))
			storage.setAddress(mapStorage.get("address"));
		if (mapStorage.get("netInterface") != null && !mapStorage.get("netInterface").equals(""))
			storage.setAddress(mapStorage.get("netInterface"));
		if (mapStorage.get("device") != null && !mapStorage.get("device").equals(""))
			storage.setDrive(mapStorage.get("device"));
		if (mapStorage.get("type") != null && !mapStorage.get("type").equals(""))
			storage.setFormat(mapStorage.get("type"));
		if (mapStorage.get("name") != null && !mapStorage.get("name").equals(""))
			storage.setName(mapStorage.get("name"));
		if (mapStorage.get("spool") != null && !mapStorage.get("spool").equals(""))
			storage.setSpool(mapStorage.get("spool"));
		if (mapStorage.get("spool-size") != null)
			storage.setSpoolSize(Integer.parseInt(mapStorage.get("spool-size")));
		
		List<StorageLibraryTapeRs> tapes = new ArrayList<StorageLibraryTapeRs>();
		for (int i=0;mapStorage.get("drive"+i) != null;i++) {
			StorageLibraryTapeRs tape = new StorageLibraryTapeRs();
			tape.setName(mapStorage.get("drive"+i));
			tapes.add(tape);
		}
		storage.setTapes(tapes);

		return storage;
	}


	// ########### GETTERS y SETTERS ##############################################
	@XmlElement(required=true)
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(required=true)
	public String getDrive() {
		return drive;
	}


	public void setDrive(String drive) {
		this.drive = drive;
	}

	@XmlElement(required=true)
	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}

	@XmlElement(required=true)
	public String getSpool() {
		return spool;
	}


	public void setSpool(String spool) {
		this.spool = spool;
	}


	public Integer getSpoolSize() {
		return spoolSize;
	}


	public void setSpoolSize(Integer spoolSize) {
		this.spoolSize = spoolSize;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	@XmlElement(required=true)
	public String getNetInterface() {
		return netInterface;
	}


	public void setNetInterface(String netInterface) {
		this.netInterface = netInterface;
	}


	@XmlElementWrapper(name="tapes", required=true)
	@XmlElementRef()
	public List<StorageLibraryTapeRs> getTapes() {
		return tapes;
	}


	public void setTapes(List<StorageLibraryTapeRs> tapes) {
		this.tapes = tapes;
	}


	public String getUnitsSize() {
		return unitsSize;
	}


	public void setUnitsSize(String unitsSize) {
		this.unitsSize = unitsSize;
	}
}
