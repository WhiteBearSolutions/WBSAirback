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

@XmlRootElement(name = "iscsiTarget")
public class IscsiTargetRs {
	
	private String iqn;
	private String user;
	private String password;
	private String disk;
	private String scsi;
	private String type;
	private String vg;
	private String lv;
	private String device;
	private String tapeVendor;
	private String tapeModel;
	private String tapeSerial;
	
	
	public IscsiTargetRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiTargetRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de target a objetos target
	 * @param maps
	 * @return
	 */
	public static List<IscsiTargetRs> listMapToObject(List<Map<String, String>> mapIscsiTargets) throws Exception {
		List<IscsiTargetRs> targets = new ArrayList<IscsiTargetRs>();
		if (mapIscsiTargets != null && mapIscsiTargets.size()>0) {
			for (Map<String, String> mapIscsiTarget : mapIscsiTargets) {
				IscsiTargetRs target = mapToObject(mapIscsiTarget);
				targets.add(target);
			}
		}
		return targets;
	}
	
	public static List<IscsiTargetRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "iscsiTarget";
			List<IscsiTargetRs> listObjects = new ArrayList<IscsiTargetRs>();
			
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
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static IscsiTargetRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiTargetRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			IscsiTargetRs o = (IscsiTargetRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<iscsiTarget>"), xml.indexOf("</iscsiTarget>")+"</iscsiTarget>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	/**
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static IscsiTargetRs mapToObject(Map<String, String> mapIscsiTarget) throws Exception {
		IscsiTargetRs target = new IscsiTargetRs();
		
		if (mapIscsiTarget.get("lv") != null && !mapIscsiTarget.get("lv").isEmpty())
			target.setLv(mapIscsiTarget.get("lv"));
		if (mapIscsiTarget.get("vg") != null && !mapIscsiTarget.get("vg").isEmpty())
			target.setVg(mapIscsiTarget.get("vg"));
		if (mapIscsiTarget.get("disk") != null && !mapIscsiTarget.get("disk").isEmpty())
			target.setDisk(mapIscsiTarget.get("disk"));
		if (mapIscsiTarget.get("iqn") != null && !mapIscsiTarget.get("iqn").isEmpty())
			target.setIqn(mapIscsiTarget.get("iqn"));
		if (mapIscsiTarget.get("password") != null && !mapIscsiTarget.get("password").isEmpty())
			target.setPassword(mapIscsiTarget.get("password"));
		if (mapIscsiTarget.get("scsi") != null && !mapIscsiTarget.get("scsi").isEmpty())
			target.setScsi(mapIscsiTarget.get("scsi"));
		if (mapIscsiTarget.get("model") != null && !mapIscsiTarget.get("model").isEmpty())
			target.setTapeModel(mapIscsiTarget.get("model"));
		if (mapIscsiTarget.get("serial") != null && !mapIscsiTarget.get("serial").isEmpty())
			target.setTapeSerial(mapIscsiTarget.get("serial"));
		if (mapIscsiTarget.get("vendor") != null && !mapIscsiTarget.get("vendor").isEmpty())
			target.setTapeVendor(mapIscsiTarget.get("vendor"));
		if (mapIscsiTarget.get("type") != null && !mapIscsiTarget.get("type").isEmpty())
			target.setType(mapIscsiTarget.get("type"));
		if (mapIscsiTarget.get("user") != null && !mapIscsiTarget.get("user").isEmpty())
			target.setUser(mapIscsiTarget.get("user"));
		
		return target;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getIqn() {
		return iqn;
	}
	public void setIqn(String iqn) {
		this.iqn = iqn;
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
	public String getDisk() {
		return disk;
	}
	public void setDisk(String disk) {
		this.disk = disk;
	}
	public String getScsi() {
		return scsi;
	}
	public void setScsi(String scsi) {
		this.scsi = scsi;
	}
	
	@XmlElement(required=true)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@XmlElement(required=true)
	public String getVg() {
		return vg;
	}
	public void setVg(String vg) {
		this.vg = vg;
	}
	
	@XmlElement(required=true)
	public String getLv() {
		return lv;
	}
	public void setLv(String lv) {
		this.lv = lv;
	}
	public String getTapeVendor() {
		return tapeVendor;
	}
	public void setTapeVendor(String tapeVendor) {
		this.tapeVendor = tapeVendor;
	}
	public String getTapeModel() {
		return tapeModel;
	}
	public void setTapeModel(String tapeModel) {
		this.tapeModel = tapeModel;
	}
	public String getTapeSerial() {
		return tapeSerial;
	}
	public void setTapeSerial(String tapeSerial) {
		this.tapeSerial = tapeSerial;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

}
