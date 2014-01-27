package com.whitebearsolutions.imagine.wbsairback.rs.model.subscription;

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

@XmlRootElement(name = "license")
public class LicenseRs {
	
	private String code;
	private String control;
	private String expiration;
	private String service;
	
	public LicenseRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( LicenseRs.class );
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
	public static List<LicenseRs> listMapToObject(List<Map<String, String>> mapLicenses) throws Exception {
		List<LicenseRs> licenses = new ArrayList<LicenseRs>();
		if (mapLicenses != null && mapLicenses.size()>0) {
			for (Map<String, String> mapAgg : mapLicenses) {
				LicenseRs license= mapToObject(mapAgg);
				licenses.add(license);
			}
		}
		return licenses;
	}
	
	public static LicenseRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "license";
				JAXBContext jc = JAXBContext.newInstance( LicenseRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					LicenseRs o = (LicenseRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<LicenseRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "license";
			List<LicenseRs> listObjects = new ArrayList<LicenseRs>();
			
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
	public static LicenseRs mapToObject(Map<String, String> map) throws Exception {
		LicenseRs license= new LicenseRs();
		
		if (map.get("code") != null && !map.get("code").isEmpty())
			license.setCode(map.get("code"));
		if (map.get("control") != null && !map.get("control").isEmpty())
			license.setControl(map.get("control"));
		if (map.get("expiration") != null && !map.get("expiration").isEmpty())
			license.setExpiration(map.get("expiration"));
		if (map.get("service") != null && !map.get("service").isEmpty())
			license.setService(map.get("service"));
		
		return license;
	}

	// GETTERS Y SETTERS
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	

}
