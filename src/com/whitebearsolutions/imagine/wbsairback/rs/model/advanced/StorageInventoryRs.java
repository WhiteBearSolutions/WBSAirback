package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

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

@XmlRootElement(name = "storageInventory")
public class StorageInventoryRs {
	
	private String name;
	private String iqnwwn;
	private String address;
	private Integer port;
	private String user;
	private String password;
	private String certificate;
	private List<TypeAdvancedRs> typesAdvanced;
	
	public StorageInventoryRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( StorageInventoryRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	/**
	 * Convierte una lista de mapas de storages a objetos storage
	 * @param mapPools
	 * @return
	 */
	public static List<StorageInventoryRs> listMapToObject(List<Map<String, Object>> mapStorageInventorys) throws Exception {
		List<StorageInventoryRs> storages = new ArrayList<StorageInventoryRs>();
		if (mapStorageInventorys != null && mapStorageInventorys.size()>0) {
			for (Map<String, Object> mapDev : mapStorageInventorys) {
				StorageInventoryRs storage = mapToObject(mapDev);
				storages.add(storage);
			}
		}
		return storages;
	}
	
	public static StorageInventoryRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "storageInventory";
				JAXBContext jc = JAXBContext.newInstance( StorageInventoryRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					StorageInventoryRs o = (StorageInventoryRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<StorageInventoryRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "storageInventory";
			List<StorageInventoryRs> listObjects = new ArrayList<StorageInventoryRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String storageXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(storageXml));
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
	 * Convierte un mapa a un objeto
	 * @param map
	 * @return
	 */
	public static StorageInventoryRs mapToObject(Map<String, Object> map) throws Exception {
		StorageInventoryRs storage = new StorageInventoryRs();
		Object p = map.get("name");
		if (p != null && !((String) p).isEmpty())
			storage.setName((String) p);
		p = map.get("address");
		if (p != null && !((String) p).isEmpty())
			storage.setAddress((String) p);
		p = map.get("certificate");
		if (p != null && !((String) p).isEmpty())
			storage.setCertificate((String) p);
		p = map.get("iqnwwn");
		if (p != null && !((String) p).isEmpty())
			storage.setIqnwwn((String) p);
		p = map.get("password");
		if (p != null && !((String) p).isEmpty())
			storage.setPassword((String) p);
		p = map.get("port");
		if (p != null && !((String) p).isEmpty())
			storage.setPort(Integer.parseInt((String) p));
		p = map.get("typesAdvanced");
		if (p != null) {
			@SuppressWarnings("unchecked")
			List<String> types = (List<String>) map.get("typesAdvanced");
			if (!types.isEmpty())
				storage.setTypesAdvanced(TypeAdvancedRs.listMapToObject(types));
		}
		p = map.get("user");
		if (p != null && !((String) p).isEmpty())
			storage.setUser((String) p);
		
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
	public String getIqnwwn() {
		return iqnwwn;
	}

	public void setIqnwwn(String iqnwwn) {
		this.iqnwwn = iqnwwn;
	}

	@XmlElement(required=true)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@XmlElement(required=true)
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlElementWrapper(name="typesAdvanced",required=true)
	@XmlElementRef()
	public List<TypeAdvancedRs> getTypesAdvanced() {
		return typesAdvanced;
	}

	public void setTypesAdvanced(List<TypeAdvancedRs> typesAdvanced) {
		this.typesAdvanced = typesAdvanced;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

}
