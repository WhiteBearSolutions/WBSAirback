package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

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

import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;

@XmlRootElement(name = "aggregate")
public class AggregateRs {

	private String name;
	private String type;
	private String pv;
	private String lv;
	private String size;
	private String sizeRaw;
	private String free;
	private String freeRaw;
	private String usedPercent;
	private String unused;
	private String unusedRaw;
	private String quota;
	private String quotaRaw;
	private String reservation;
	private String reservationRaw;
	private boolean local;
	
	private List<PhysicalDeviceRs> devices;
	
	public AggregateRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( AggregateRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de agregados a objetos agregado
	 * @param mapPools
	 * @return
	 */
	public static List<AggregateRs> listMapToObject(List<Map<String, String>> mapAggregates) throws Exception {
		List<AggregateRs> aggs = new ArrayList<AggregateRs>();
		if (mapAggregates != null && mapAggregates.size()>0) {
			for (Map<String, String> mapAgg : mapAggregates) {
				List<Map<String, String>> disks = VolumeManager.getPhysicalVolumes(mapAgg.get("name"));
				AggregateRs agg = mapToObject(mapAgg, disks);
				aggs.add(agg);
			}
		}
		return aggs;
	}
	
	public static AggregateRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "aggregate";
				JAXBContext jc = JAXBContext.newInstance( AggregateRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					AggregateRs o = (AggregateRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<AggregateRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "aggregate";
			List<AggregateRs> listObjects = new ArrayList<AggregateRs>();
			
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
	public static AggregateRs mapToObject(Map<String, String> mapAgg, List<Map<String, String>> devices) throws Exception {
		AggregateRs agg = new AggregateRs();
		if (mapAgg.get("free") != null && !mapAgg.get("free").isEmpty())
			agg.setFree(mapAgg.get("free"));
		if (mapAgg.get("free-raw") != null && !mapAgg.get("free-raw").isEmpty())
			agg.setFreeRaw(mapAgg.get("free-raw"));
		if (mapAgg.get("lv") != null && !mapAgg.get("lv").isEmpty())
			agg.setLv(mapAgg.get("lv"));
		if (mapAgg.get("name") != null && !mapAgg.get("name").isEmpty())
			agg.setName(mapAgg.get("name"));
		if (mapAgg.get("pv") != null && !mapAgg.get("pv").isEmpty())
			agg.setPv(mapAgg.get("pv"));
		if (mapAgg.get("quota") != null && !mapAgg.get("quota").isEmpty())
			agg.setQuota(mapAgg.get("quota"));
		if (mapAgg.get("quota-raw") != null && !mapAgg.get("quota-raw").isEmpty())
			agg.setQuotaRaw(mapAgg.get("quota-raw"));
		if (mapAgg.get("reservation") != null && !mapAgg.get("reservation").isEmpty())
			agg.setReservation(mapAgg.get("reservation"));
		if (mapAgg.get("reservation-raw") != null && !mapAgg.get("reservation-raw").isEmpty())
			agg.setReservationRaw(mapAgg.get("reservation-raw"));
		if (mapAgg.get("size") != null && !mapAgg.get("size").isEmpty())
			agg.setSize(mapAgg.get("size"));
		if (mapAgg.get("size-raw") != null && !mapAgg.get("size-raw").isEmpty())
			agg.setSizeRaw(mapAgg.get("size-raw"));
		if (mapAgg.get("type") != null && !mapAgg.get("type").isEmpty())
			agg.setType(mapAgg.get("type"));
		if (mapAgg.get("unused") != null && !mapAgg.get("unused").isEmpty())
			agg.setUnused(mapAgg.get("unused"));
		if (mapAgg.get("unused-raw") != null && !mapAgg.get("unused-raw").isEmpty())
			agg.setUnusedRaw(mapAgg.get("unused-raw"));
		if (mapAgg.get("used") != null && !mapAgg.get("used").isEmpty())
			agg.setUsedPercent(mapAgg.get("used"));
		
		if (devices != null && devices.size()>0)
			agg.setDevices(PhysicalDeviceRs.listMapToObject(devices));

		if (VolumeManager.isLocalDeviceGroup(agg.getName()))
			agg.setLocal(true);
		else
			agg.setLocal(false);
		
		return agg;
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

	public String getPv() {
		return pv;
	}

	public void setPv(String pv) {
		this.pv = pv;
	}

	public String getLv() {
		return lv;
	}

	public void setLv(String lv) {
		this.lv = lv;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getSizeRaw() {
		return sizeRaw;
	}

	public void setSizeRaw(String sizeRaw) {
		this.sizeRaw = sizeRaw;
	}

	public String getFree() {
		return free;
	}

	public void setFree(String free) {
		this.free = free;
	}

	public String getFreeRaw() {
		return freeRaw;
	}

	public void setFreeRaw(String freeRaw) {
		this.freeRaw = freeRaw;
	}

	public String getUsedPercent() {
		return usedPercent;
	}

	public void setUsedPercent(String usedPercent) {
		this.usedPercent = usedPercent;
	}

	public String getUnused() {
		return unused;
	}

	public void setUnused(String unused) {
		this.unused = unused;
	}

	public String getUnusedRaw() {
		return unusedRaw;
	}

	public void setUnusedRaw(String unusedRaw) {
		this.unusedRaw = unusedRaw;
	}

	public String getQuota() {
		return quota;
	}

	public void setQuota(String quota) {
		this.quota = quota;
	}

	public String getQuotaRaw() {
		return quotaRaw;
	}

	public void setQuotaRaw(String quotaRaw) {
		this.quotaRaw = quotaRaw;
	}

	public String getReservation() {
		return reservation;
	}

	public void setReservation(String reservation) {
		this.reservation = reservation;
	}

	public String getReservationRaw() {
		return reservationRaw;
	}

	public void setReservationRaw(String reservationRaw) {
		this.reservationRaw = reservationRaw;
	}
	
	@XmlElement(required=true)
	public List<PhysicalDeviceRs> getDevices() {
		return devices;
	}

	public void setDevices(List<PhysicalDeviceRs> devices) {
		this.devices = devices;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}
	
}
