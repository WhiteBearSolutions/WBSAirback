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

@XmlRootElement(name = "poolVolume")
public class PoolVolumeRs {
	
	private String name;
	private String status;
	private String type;
	private String slot;
	private String files;
	private String size;
	private String retention;
	private String firstWrite;
	private String lastWrite;
	
	public PoolVolumeRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( PoolVolumeRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	public static PoolVolumeRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "poolVolume";
				JAXBContext jc = JAXBContext.newInstance( PoolVolumeRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					PoolVolumeRs o = (PoolVolumeRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<PoolVolumeRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "poolVolume";
			List<PoolVolumeRs> listObjects = new ArrayList<PoolVolumeRs>();
			
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
	 * Convierte una lista de mapas de poolVolumes a objetos pool
	 * @param mapPools
	 * @return
	 */
	public static List<PoolVolumeRs> listMapToObject(List<Map<String, String>> mapVolPools) {
		List<PoolVolumeRs> pools = new ArrayList<PoolVolumeRs>();
		if (mapVolPools != null && mapVolPools.size()>0) {
			for (Map<String, String> mapPool : mapVolPools) {
				PoolVolumeRs pool = mapToObject(mapPool);
				pools.add(pool);
			}
		}
		return pools;
	}
	
	public static PoolVolumeRs mapToObject(Map<String, String> mapVolPool) {
		PoolVolumeRs poolVolume = new PoolVolumeRs();
		
		if (mapVolPool.get("files") != null && !mapVolPool.get("files").equals(""))
			poolVolume.setFiles(mapVolPool.get("files"));
		if (mapVolPool.get("first-write") != null && !mapVolPool.get("first-write").equals(""))
			poolVolume.setFirstWrite(mapVolPool.get("first-write"));
		if (mapVolPool.get("last-write") != null && !mapVolPool.get("last-write").equals(""))
			poolVolume.setLastWrite(mapVolPool.get("last-write"));
		if (mapVolPool.get("name") != null && !mapVolPool.get("name").equals(""))
			poolVolume.setName(mapVolPool.get("name"));
		if (mapVolPool.get("retention") != null && !mapVolPool.get("retention").equals(""))
			poolVolume.setRetention(mapVolPool.get("retention"));
		if (mapVolPool.get("size") != null && !mapVolPool.get("size").equals(""))
			poolVolume.setSize(mapVolPool.get("size"));
		if (mapVolPool.get("slot") != null && !mapVolPool.get("slot").equals(""))
			poolVolume.setSlot(mapVolPool.get("slot"));
		if (mapVolPool.get("status") != null && !mapVolPool.get("status").equals(""))
			poolVolume.setStatus(mapVolPool.get("status"));
		if (mapVolPool.get("type") != null && !mapVolPool.get("type").equals(""))
			poolVolume.setType(mapVolPool.get("type"));
		
		return poolVolume;
	}

	// ############################   	GETTERS Y SETTERS 	######################################
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	public String getFiles() {
		return files;
	}

	public void setFiles(String files) {
		this.files = files;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getRetention() {
		return retention;
	}

	public void setRetention(String retention) {
		this.retention = retention;
	}

	public String getFirstWrite() {
		return firstWrite;
	}

	public void setFirstWrite(String firstWrite) {
		this.firstWrite = firstWrite;
	}

	public String getLastWrite() {
		return lastWrite;
	}

	public void setLastWrite(String lastWrite) {
		this.lastWrite = lastWrite;
	}

}
