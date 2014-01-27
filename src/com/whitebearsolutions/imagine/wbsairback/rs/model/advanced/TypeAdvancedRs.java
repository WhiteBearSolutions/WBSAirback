package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "typeAdvanced")
public class TypeAdvancedRs {
	
	private String name;
	
	public TypeAdvancedRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( TypeAdvancedRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	public static TypeAdvancedRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "typeAdvanced";
				JAXBContext jc = JAXBContext.newInstance( TypeAdvancedRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					TypeAdvancedRs o = (TypeAdvancedRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<TypeAdvancedRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "typeAdvanced";
			List<TypeAdvancedRs> listObjects = new ArrayList<TypeAdvancedRs>();
			
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
	 * Convierte una lista de mapas de typeAdvanceds a objetos pool
	 * @param mapPools
	 * @return
	 */
	public static List<TypeAdvancedRs> listMapToObject(List<String> listTypes) {
		List<TypeAdvancedRs> types = new ArrayList<TypeAdvancedRs>();
		if (listTypes != null && listTypes.size()>0) {
			for (String type : listTypes) {
				TypeAdvancedRs adType = mapToObject(type);
				types.add(adType);
			}
		}
		return types;
	}
	
	public static TypeAdvancedRs mapToObject(String type) {
		TypeAdvancedRs typeAdvanced = new TypeAdvancedRs();
		
		if (type != null && !type.isEmpty())
			typeAdvanced.setName(type);
		
		return typeAdvanced;
	}

	// ############################   	GETTERS Y SETTERS 	######################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
