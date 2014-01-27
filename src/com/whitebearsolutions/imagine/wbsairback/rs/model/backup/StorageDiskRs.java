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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "storageDisk")
public class StorageDiskRs {

	private String name;
	private String type;
	private String group;
	private String volume;
	private String netInterface;
	// Remote Storage params
	private String server;
	private String share;
	private Boolean aligned;
	private Integer paralelJobs;
	
	/**
	 * Constructor vacío requerido por JAX
	 */
	public StorageDiskRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( StorageDiskRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	public static StorageDiskRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "storageDisk";
				JAXBContext jc = JAXBContext.newInstance( StorageDiskRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					StorageDiskRs o = (StorageDiskRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<StorageDiskRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "storageDisk";
			List<StorageDiskRs> listObjects = new ArrayList<StorageDiskRs>();
			
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
	 * Convierte una lista de mapas de cliente a objetos clientrs
	 * @param mapClients
	 * @return
	 */
	public static List<StorageDiskRs> listMapToObject(List<Map<String, String>> mapStorages) {
		List<StorageDiskRs> storages = new ArrayList<StorageDiskRs>();
		if (mapStorages != null && mapStorages.size()>0) {
			for (Map<String, String> mapSt : mapStorages) {
				StorageDiskRs j = mapToObject(mapSt);
				storages.add(j);
			}
		}
		return storages;
	}
	
	
	/**
	 * Covierte un mapa de información de un storage en un objeto storagers
	 * @param mapStorage
	 * @return
	 */
	public static StorageDiskRs mapToObject(Map<String, String> mapStorage) {
		StorageDiskRs storage = new StorageDiskRs();
		if (mapStorage.get("name") != null && !mapStorage.get("name").equals(""))
			storage.setName(mapStorage.get("name"));
		if (mapStorage.get("address") != null && !mapStorage.get("address").equals(""))
			storage.setNetInterface(mapStorage.get("address"));
		if (mapStorage.get("type") != null && !mapStorage.get("type").equals("")) {
			storage.setType(mapStorage.get("type"));
			if (mapStorage.get("type").equals("EXT")) {
				if (mapStorage.get("server") != null && !mapStorage.get("server").equals(""))
					storage.setServer(mapStorage.get("server"));
				if (mapStorage.get("share") != null && !mapStorage.get("share").equals(""))
					storage.setShare(mapStorage.get("share"));
			} else {
				if (mapStorage.get("vg") != null && !mapStorage.get("vg").equals(""))
					storage.setGroup(mapStorage.get("vg"));
				if (mapStorage.get("lv") != null && !mapStorage.get("lv").equals(""))
					storage.setVolume(mapStorage.get("lv"));
			}
		}
		if (mapStorage.get("deviceType") != null && mapStorage.get("deviceType").equals("Aligned")) {
			storage.setAligned(true);
		} else
			storage.setAligned(false);
		if (mapStorage.get("paralelJobs") != null && !mapStorage.get("paralelJobs").equals(""))
			storage.setParalelJobs(Integer.parseInt(mapStorage.get("paralelJobs")));
		
		return storage;
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
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getShare() {
		return share;
	}
	public void setShare(String share) {
		this.share = share;
	}

	public String getNetInterface() {
		return netInterface;
	}

	public void setNetInterface(String netInterface) {
		this.netInterface = netInterface;
	}

	public Boolean getAligned() {
		return aligned;
	}

	public void setAligned(Boolean aligned) {
		this.aligned = aligned;
	}

	public Integer getParalelJobs() {
		return paralelJobs;
	}

	public void setParalelJobs(Integer paralelJobs) {
		this.paralelJobs = paralelJobs;
	}
	
	
}
