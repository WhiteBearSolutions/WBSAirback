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

@XmlRootElement(name = "hypervisorDataStore")
public class HypervisorDataStoreRs {

	private String name;
	private String free;
	private Boolean directoryBased;
	private String path;
	private String mode;
	
	public HypervisorDataStoreRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorDataStoreRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static HypervisorDataStoreRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorDataStoreRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			HypervisorDataStoreRs o = (HypervisorDataStoreRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<hypervisorDataStore>"), xml.indexOf("</hypervisorDataStore>")+"</hypervisorDataStore>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	public static List<HypervisorDataStoreRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "hypervisorDataStore";
			List<HypervisorDataStoreRs> listObjects = new ArrayList<HypervisorDataStoreRs>();
			
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
	 * Convierte una lista de mapas de ds a objetos ds
	 * @param maps
	 * @return
	 */
	public static List<HypervisorDataStoreRs> listMapToObject(List<Map<String, String>> listMap) throws Exception {
		List<HypervisorDataStoreRs> dss = new ArrayList<HypervisorDataStoreRs>();
		if (listMap != null && listMap.size()>0) {
			for (Map<String, String> map : listMap) {
				HypervisorDataStoreRs ds = mapToObject(map);
				dss.add(ds);
			}
		}
		return dss;
	}
	
	
	/**
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static HypervisorDataStoreRs mapToObject(Map<String, String> map) throws Exception {
		HypervisorDataStoreRs ds = new HypervisorDataStoreRs();
		
		if (map.get("directory-based") != null && !map.get("directory-based").isEmpty())
			ds.setDirectoryBased(Boolean.parseBoolean(map.get("directory-based")));
		if (map.get("free") != null && !map.get("free").isEmpty())
			ds.setFree(map.get("free"));
		if (map.get("mode") != null && !map.get("mode").isEmpty())
			ds.setMode(map.get("mode"));
		if (map.get("name") != null && !map.get("name").isEmpty())
			ds.setName(map.get("name"));
		if (map.get("path") != null && !map.get("path").isEmpty())
			ds.setPath(map.get("path"));
		
		return ds;
	}

	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFree() {
		return free;
	}

	public void setFree(String free) {
		this.free = free;
	}

	public Boolean getDirectoryBased() {
		return directoryBased;
	}

	public void setDirectoryBased(Boolean directoryBased) {
		this.directoryBased = directoryBased;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
}
