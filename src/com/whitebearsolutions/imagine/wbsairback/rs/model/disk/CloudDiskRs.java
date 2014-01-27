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

@XmlRootElement(name = "cloudDisk")
public class CloudDiskRs {
	
	private String account;
	private String cld;
	private String pathCldSys;
	private String size;
	private Double sizeRaw;
	private String type;
	private String bucket;
	private String device;
	
	
	public CloudDiskRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudDiskRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de cloudDisk a objetos cloudDisk
	 * @param maps
	 * @return
	 */
	public static List<CloudDiskRs> listMapToObject(List<Map<String, String>> mapCloudDisks) throws Exception {
		List<CloudDiskRs> cloudDisks = new ArrayList<CloudDiskRs>();
		if (mapCloudDisks != null && mapCloudDisks.size()>0) {
			for (Map<String, String> mapCloudDisk : mapCloudDisks) {
				CloudDiskRs cloudDisk = mapToObject(mapCloudDisk);
				cloudDisks.add(cloudDisk);
			}
		}
		return cloudDisks;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static CloudDiskRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudDiskRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			CloudDiskRs o = (CloudDiskRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<cloudDisk>"), xml.indexOf("</cloudDisk>")+"</cloudDisk>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	public static List<CloudDiskRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "cloudDisk";
			List<CloudDiskRs> listObjects = new ArrayList<CloudDiskRs>();
			
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
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static CloudDiskRs mapToObject(Map<String, String> map) throws Exception {
		CloudDiskRs cloudDisk = new CloudDiskRs();
		
		if (map.get("account") != null && !map.get("account").isEmpty())
			cloudDisk.setAccount(map.get("account"));
		if (map.get("bucket") != null && !map.get("bucket").isEmpty())
			cloudDisk.setBucket(map.get("bucket"));
		if (map.get("cld") != null && !map.get("cld").isEmpty())
			cloudDisk.setCld(map.get("cld"));
		if (map.get("device") != null && !map.get("device").isEmpty())
			cloudDisk.setDevice(map.get("device").substring(map.get("device").indexOf("dev/")+"dev/".length()));
		if (map.get("path-clsys") != null && !map.get("path-clsys").isEmpty())
			cloudDisk.setPathCldSys(map.get("path-clsys"));
		if (map.get("size") != null && !map.get("size").isEmpty())
			cloudDisk.setSize(map.get("size"));
		if (map.get("size-raw") != null && !map.get("size-raw").isEmpty())
			cloudDisk.setSizeRaw(Double.parseDouble(map.get("size-raw")));
		if (map.get("type") != null && !map.get("type").isEmpty())
			cloudDisk.setType(map.get("type"));
		
		return cloudDisk;
	}


	// ####### GETTERS Y SETTERS #################################
	
	
	public String getAccount() {
		return account;
	}


	public void setAccount(String account) {
		this.account = account;
	}


	public String getCld() {
		return cld;
	}


	public void setCld(String cld) {
		this.cld = cld;
	}


	public String getPathCldSys() {
		return pathCldSys;
	}


	public void setPathCldSys(String pathCldSys) {
		this.pathCldSys = pathCldSys;
	}


	public String getSize() {
		return size;
	}


	public void setSize(String size) {
		this.size = size;
	}


	public Double getSizeRaw() {
		return sizeRaw;
	}


	public void setSizeRaw(Double sizeRaw) {
		this.sizeRaw = sizeRaw;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getBucket() {
		return bucket;
	}


	public void setBucket(String bucket) {
		this.bucket = bucket;
	}


	public String getDevice() {
		return device;
	}


	public void setDevice(String device) {
		this.device = device;
	}
}
