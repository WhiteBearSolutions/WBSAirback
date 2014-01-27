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

@XmlRootElement(name = "quotaUser")
public class QuotaUserRs {
	
	private String user;
	private String used;
	private String soft;
	private String hard;
	
	public QuotaUserRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( QuotaUserRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de quota a objetos quota
	 * @param maps
	 * @return
	 */
	public static List<QuotaUserRs> listMapToObject(Map<String, Map<String, String>> mapQuotaUsers) throws Exception {
		List<QuotaUserRs> quotas = new ArrayList<QuotaUserRs>();
		if (mapQuotaUsers != null && mapQuotaUsers.size()>0) {
			for (String user : mapQuotaUsers.keySet()) {
				QuotaUserRs quota = mapToObject(user, mapQuotaUsers.get(user));
				quotas.add(quota);
			}
		}
		return quotas;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static QuotaUserRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( QuotaUserRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			QuotaUserRs o = (QuotaUserRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<quotaUser>"), xml.indexOf("</quotaUser>")+"</quotaUser>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<QuotaUserRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "quotaUser";
			List<QuotaUserRs> listObjects = new ArrayList<QuotaUserRs>();
			
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
	public static QuotaUserRs mapToObject(String user, Map<String, String> map) throws Exception {
		QuotaUserRs quota = new QuotaUserRs();
		if (map.get("hard") != null && !map.get("hard").isEmpty())
			quota.setHard(map.get("hard"));
		if (map.get("soft") != null && !map.get("soft").isEmpty())
			quota.setSoft(map.get("soft"));
		if (map.get("used") != null && !map.get("used").isEmpty())
			quota.setUsed(map.get("used"));
		if (user != null && !user.isEmpty())
			quota.setUser(user);
		return quota;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUsed() {
		return used;
	}

	public void setUsed(String used) {
		this.used = used;
	}

	public String getSoft() {
		return soft;
	}

	public void setSoft(String soft) {
		this.soft = soft;
	}

	public String getHard() {
		return hard;
	}

	public void setHard(String hard) {
		this.hard = hard;
	}

	

}
