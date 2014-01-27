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

@XmlRootElement(name = "iscsiExternalTarget")
public class IscsiExternalTargetRs {

	private Integer port;
	private String iqn;
	private String address;
	private Boolean outdated;
	
	public IscsiExternalTargetRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiExternalTargetRs.class );
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
	public static List<IscsiExternalTargetRs> listMapToObject(List<Map<String, String>> mapIscsiExternalTargets) throws Exception {
		List<IscsiExternalTargetRs> targets = new ArrayList<IscsiExternalTargetRs>();
		if (mapIscsiExternalTargets != null && mapIscsiExternalTargets.size()>0) {
			for (Map<String, String> mapIscsiExternalTarget : mapIscsiExternalTargets) {
				IscsiExternalTargetRs target = mapToObject(mapIscsiExternalTarget);
				targets.add(target);
			}
		}
		return targets;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static IscsiExternalTargetRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiExternalTargetRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			IscsiExternalTargetRs o = (IscsiExternalTargetRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<iscsiExternalTarget>"), xml.indexOf("</iscsiExternalTarget>")+"</iscsiExternalTarget>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<IscsiExternalTargetRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "iscsiExternalTarget";
			List<IscsiExternalTargetRs> listObjects = new ArrayList<IscsiExternalTargetRs>();
			
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
	public static IscsiExternalTargetRs mapToObject(Map<String, String> mapIscsiExternalTarget) throws Exception {
		IscsiExternalTargetRs target = new IscsiExternalTargetRs();
		
		if (mapIscsiExternalTarget.get("address") != null && !mapIscsiExternalTarget.get("address").isEmpty())
			target.setAddress(mapIscsiExternalTarget.get("address"));
		if (mapIscsiExternalTarget.get("outdated") != null && !mapIscsiExternalTarget.get("outdated").isEmpty())
			target.setOutdated(Boolean.parseBoolean(mapIscsiExternalTarget.get("outdated")));
		else
			target.setOutdated(false);
		if (mapIscsiExternalTarget.get("port") != null && !mapIscsiExternalTarget.get("port").isEmpty())
			target.setPort(Integer.parseInt(mapIscsiExternalTarget.get("port")));
		if (mapIscsiExternalTarget.get("iqn") != null && !mapIscsiExternalTarget.get("iqn").isEmpty())
			target.setIqn(mapIscsiExternalTarget.get("iqn"));
		
		return target;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getIqn() {
		return iqn;
	}

	public void setIqn(String iqn) {
		this.iqn = iqn;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Boolean getOutdated() {
		return outdated;
	}

	public void setOutdated(Boolean outdated) {
		this.outdated = outdated;
	}
	
	
}
