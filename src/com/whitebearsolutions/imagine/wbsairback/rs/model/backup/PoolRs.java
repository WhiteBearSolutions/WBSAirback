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

@XmlRootElement(name = "pool")
public class PoolRs {

	private String id;
	private String name;
	private String storage;
	private Integer retention;
	private Integer durationHours;
	private String copyDestination;
	private String scratchPool;
	private String recyclePool;
	private Integer migrationHours;
	private String type;
	
	public PoolRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( PoolRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de pools a objetos pool
	 * @param mapPools
	 * @return
	 */
	public static List<PoolRs> listMapToObject(List<Map<String, String>> mapPools) {
		List<PoolRs> pools = new ArrayList<PoolRs>();
		if (mapPools != null && mapPools.size()>0) {
			for (Map<String, String> mapPool : mapPools) {
				PoolRs pool = mapToObject(mapPool);
				pools.add(pool);
			}
		}
		return pools;
	}
	
	
	public static PoolRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "pool";
				JAXBContext jc = JAXBContext.newInstance( PoolRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					PoolRs o = (PoolRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<PoolRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "pool";
			List<PoolRs> listObjects = new ArrayList<PoolRs>();
			
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
	public static PoolRs mapToObject(Map<String, String> mapPool) {
		PoolRs pool = new PoolRs();
		if (mapPool.get("id") != null)
			pool.setId(mapPool.get("id"));
		if (mapPool.get("copy") != null && !mapPool.get("copy").equals(""))
			pool.setCopyDestination(mapPool.get("copy"));
		if (mapPool.get("volume-duration") != null && !mapPool.get("volume-duration").equals(""))
			if (mapPool.get("volume-duration").indexOf(" ") > -1)
				pool.setDurationHours(Integer.parseInt(mapPool.get("volume-duration").substring(0, mapPool.get("volume-duration").indexOf(" "))));
			else
				pool.setDurationHours(Integer.parseInt(mapPool.get("volume-duration")));
		if (mapPool.get("migration-hours") != null && !mapPool.get("migration-hours").equals(""))
			if (mapPool.get("migration-hours").indexOf(" ") > -1)
				pool.setMigrationHours(Integer.parseInt(mapPool.get("migration-hours").substring(0, mapPool.get("migration-hours").indexOf(" "))));
			else
				pool.setMigrationHours(Integer.parseInt(mapPool.get("migration-hours")));
		if (mapPool.get("name") != null)
			pool.setName(mapPool.get("name"));
		if (mapPool.get("recycle_pool") != null && !mapPool.get("recycle_pool").equals(""))
			pool.setRecyclePool(mapPool.get("recycle_pool"));
		if (mapPool.get("retention") != null && !mapPool.get("retention").equals(""))
			if (mapPool.get("retention").indexOf(" ") > -1)
				pool.setRetention(Integer.parseInt(mapPool.get("retention").substring(0, mapPool.get("retention").indexOf(" "))));
			else
				pool.setRetention(Integer.parseInt(mapPool.get("retention")));
		if (mapPool.get("scratch_pool") != null && !mapPool.get("scratch_pool").equals(""))
			pool.setScratchPool(mapPool.get("scratch_pool"));
		if (mapPool.get("storage") != null && !mapPool.get("storage").equals(""))
			pool.setStorage(mapPool.get("storage"));
		if (mapPool.get("type") != null && !mapPool.get("type").equals(""))
			pool.setType(mapPool.get("type"));
		return pool;
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
	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	@XmlElement(required=true)
	public Integer getRetention() {
		return retention;
	}

	public void setRetention(Integer retention) {
		this.retention = retention;
	}

	@XmlElement(required=false)
	public Integer getDurationHours() {
		return durationHours;
	}

	public void setDurationHours(Integer durationHours) {
		this.durationHours = durationHours;
	}

	@XmlElement(required=false)
	public String getCopyDestination() {
		return copyDestination;
	}

	public void setCopyDestination(String copyDestination) {
		this.copyDestination = copyDestination;
	}

	@XmlElement(required=false)
	public String getScratchPool() {
		return scratchPool;
	}

	public void setScratchPool(String scratchPool) {
		this.scratchPool = scratchPool;
	}

	@XmlElement(required=false)
	public String getRecyclePool() {
		return recyclePool;
	}

	public void setRecyclePool(String recyclePool) {
		this.recyclePool = recyclePool;
	}

	@XmlElement(required=false)
	public Integer getMigrationHours() {
		return migrationHours;
	}

	public void setMigrationHours(Integer migrationHours) {
		this.migrationHours = migrationHours;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
