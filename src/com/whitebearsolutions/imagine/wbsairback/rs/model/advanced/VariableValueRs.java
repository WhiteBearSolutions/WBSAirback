package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "variableValue")
public class VariableValueRs {
	
	private String name;
	private String value;
	
	public VariableValueRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( VariableValueRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	

	public static List<VariableValueRs> listMapToObject(List<Map<String, String>> mapVars) throws Exception {
		List<VariableValueRs> vars = new ArrayList<VariableValueRs>();
		if (mapVars != null && mapVars.size()>0) {
			for (Map<String, String> mapDev : mapVars) {
				VariableValueRs var = mapToObject(mapDev);
				vars.add(var);
			}
		}
		return vars;
	}
	
	public static VariableValueRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "variableValue";
				JAXBContext jc = JAXBContext.newInstance( VariableValueRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					VariableValueRs o = (VariableValueRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<VariableValueRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "variableValue";
			List<VariableValueRs> listObjects = new ArrayList<VariableValueRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String variableXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(variableXml));
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
	
	
	
	public static Map<String, String> objectToMap(VariableValueRs o) throws Exception {
		Map<String, String> var = new HashMap<String, String>();
		if (o.getName() != null && !o.getName().isEmpty())
			var.put("name", o.getName());
		if (o.getValue() != null && !o.getValue().isEmpty())
			var.put("name", o.getValue());
		return var;
	}
	
	/**
	 * Convierte un mapa a un objeto
	 * @param mapPool
	 * @return
	 */
	public static VariableValueRs mapToObject(Map<String, String> map) throws Exception {
		VariableValueRs var = new VariableValueRs();
		Object p = map.get("name");
		if (p != null && !((String)p).isEmpty())
			var.setName((String) p);
		p = map.get("value");
		if (p != null && !((String)p).isEmpty())
			var.setValue((String) p);
		return var;
	}
	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
