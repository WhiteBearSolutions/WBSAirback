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

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;

@XmlRootElement(name = "storageRemote")
public class StorageRemoteRs {

	private String name;
	private String type;
	private String device;
	private String address;
	private String password;
	private String mediatype;
	
	
	/**
	 * Constructor 
	 */
	public StorageRemoteRs() {}
	
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( StorageRemoteRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	public static StorageRemoteRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "storageRemote";
				JAXBContext jc = JAXBContext.newInstance( StorageRemoteRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					StorageRemoteRs o = (StorageRemoteRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<StorageRemoteRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "storageRemote";
			List<StorageRemoteRs> listObjects = new ArrayList<StorageRemoteRs>();
			
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
	 * Convierte una lista de mapas de storage a objetos storageRs
	 * @param mapStorages
	 * @return
	 */
	public static List<StorageRemoteRs> listMapToObject(List<Map<String, String>> mapStorages) {
		List<StorageRemoteRs> storages = new ArrayList<StorageRemoteRs>();
		if (mapStorages != null && mapStorages.size()>0) {
			for (Map<String, String> mapSt : mapStorages) {
				StorageRemoteRs j = mapToObject(mapSt);
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
	public static StorageRemoteRs mapToObject(Map<String, String> mapStorage) {
		StorageRemoteRs storage = new StorageRemoteRs();

		if (mapStorage.get("storage.address") != null && !mapStorage.get("storage.address").equals("")) {
			String[] address = NetworkManager.toAddress(mapStorage.get("storage.address"));
			String add = address[0];
			for (int i=1;i<address.length;i++)
				add+="."+address[i];
			storage.setAddress(add);
		}
		if (mapStorage.get("storage.device") != null && !mapStorage.get("storage.device").equals(""))
			storage.setDevice(mapStorage.get("storage.device"));
		if (mapStorage.get("storage.mediatype") != null && !mapStorage.get("storage.mediatype").equals(""))
			storage.setMediatype(mapStorage.get("storage.mediatype"));
		if (mapStorage.get("name") != null && !mapStorage.get("name").equals(""))
			storage.setName(mapStorage.get("name"));
		if (mapStorage.get("storage.password") != null && !mapStorage.get("storage.password").equals(""))
			storage.setPassword(mapStorage.get("storage.password"));
		if (mapStorage.get("storage.devicetype") != null && !mapStorage.get("storage.devicetype").equals(""))
			storage.setType(mapStorage.get("storage.devicetype"));
		
		return storage;
	}

	
	// #####################  	 GETTERS Y SETTERS 	#############################

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

	@XmlElement(required=true)
	public String getDevice() {
		return device;
	}


	public void setDevice(String device) {
		this.device = device;
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


	public String getMediatype() {
		return mediatype;
	}


	public void setMediatype(String mediatype) {
		this.mediatype = mediatype;
	}
}
