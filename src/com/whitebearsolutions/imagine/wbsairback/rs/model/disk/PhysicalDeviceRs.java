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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "physicalDevice")
public class PhysicalDeviceRs {

	private String device;
	private String deviceId;
	private String vg;
	private String size;
	private String sizeRaw;
	private String free;
	private String freeRaw;
	private String model;
	private String vendor;
	
	public PhysicalDeviceRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( PhysicalDeviceRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	/**
	 * Convierte una lista de mapas de dispositivos a objetos dispositivo
	 * @param mapPools
	 * @return
	 */
	public static List<PhysicalDeviceRs> listMapToObject(List<Map<String, String>> mapDevs) throws Exception {
		List<PhysicalDeviceRs> devs = new ArrayList<PhysicalDeviceRs>();
		if (mapDevs != null && mapDevs.size()>0) {
			for (Map<String, String> mapDev : mapDevs) {
				PhysicalDeviceRs dev = mapToObject(mapDev);
				devs.add(dev);
			}
		}
		return devs;
	}
	
	public static PhysicalDeviceRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "physicalDevice";
				JAXBContext jc = JAXBContext.newInstance( PhysicalDeviceRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					PhysicalDeviceRs o = (PhysicalDeviceRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<PhysicalDeviceRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "physicalDevice";
			List<PhysicalDeviceRs> listObjects = new ArrayList<PhysicalDeviceRs>();
			
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
	 * Convierte un mapa a un objeto
	 * @param mapPool
	 * @return
	 */
	public static PhysicalDeviceRs mapToObject(Map<String, String> map) throws Exception {
		PhysicalDeviceRs dev = new PhysicalDeviceRs();

		if (map.get("device") != null && !map.get("device").isEmpty())
			dev.setDevice(map.get("device"));
		if (map.get("deviceId") != null && !map.get("deviceId").isEmpty())
			dev.setDeviceId(map.get("deviceId"));
		if (map.get("free") != null && !map.get("free").isEmpty())
			dev.setFree(map.get("free"));
		if (map.get("free-raw") != null && !map.get("free-raw").isEmpty())
			dev.setFreeRaw(map.get("free-raw"));
		if (map.get("model") != null && !map.get("model").isEmpty())
			dev.setModel(map.get("model"));
		if (map.get("size") != null && !map.get("size").isEmpty())
			dev.setSize(map.get("size"));
		if (map.get("size-raw") != null && !map.get("size-raw").isEmpty())
			dev.setSizeRaw(map.get("size-raw"));
		if (map.get("vendor") != null && !map.get("vendor").isEmpty())
			dev.setVendor(map.get("vendor"));
		if (map.get("vg") != null && !map.get("vg").isEmpty())
			dev.setVg(map.get("vg"));
		
		return dev;
	}
	
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getVg() {
		return vg;
	}
	public void setVg(String vg) {
		this.vg = vg;
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

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	
}
