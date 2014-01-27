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

@XmlRootElement(name = "file")
public class BackupFileRs {
	
	private String path;
	private String name;
	private String size;
	private String lastModified;
	private Boolean isDirectory;
	
	public BackupFileRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( BackupFileRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de backup file a objetos file
	 * @param mapPools
	 * @return
	 */
	public static List<BackupFileRs> listMapToObject(List<Map<String, String>> mapFiles) {
		List<BackupFileRs> files = new ArrayList<BackupFileRs>();
		if (mapFiles != null && mapFiles.size()>0) {
			for (Map<String, String> mapFile : mapFiles) {
				BackupFileRs file = mapToObject(mapFile);
				files.add(file);
			}
		}
		return files;
	}
	
	public static BackupFileRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "file";
				JAXBContext jc = JAXBContext.newInstance( BackupFileRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					BackupFileRs o = (BackupFileRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<BackupFileRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "file";
			List<BackupFileRs> listObjects = new ArrayList<BackupFileRs>();
			
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
	 * Convierte un mapa de valores de un pool a un objeto Pool 
	 * @param mapPool
	 * @return
	 */
	public static BackupFileRs mapToObject(Map<String, String> map) {
		BackupFileRs obj = new BackupFileRs();
		if (map.get("path") != null)
			obj.setPath(map.get("path"));
		if (map.get("name") != null)
			obj.setName(map.get("name"));
		if (map.get("size") != null)
			obj.setSize(map.get("size"));
		if (map.get("last-modified") != null)
			obj.setLastModified(map.get("last-modified"));
		boolean directory = true;
		if (map.get("directory") != null && !map.get("directory").equals("true"))
			directory = false;
		obj.setIsDirectory(directory);
		if (directory && !obj.getPath().endsWith("/"))
			obj.setPath(obj.getPath()+"/");

		return obj;
	}
	
	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	public Boolean getIsDirectory() {
		return isDirectory;
	}
	public void setIsDirectory(Boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

}
