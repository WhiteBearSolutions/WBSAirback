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

@XmlRootElement(name = "variable")
public class VariableRs {
	
	private String name;
	private String description;
	private Boolean password;
	
	public VariableRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( VariableRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	/**
	 * Convierte una lista de mapas de variables a objetos variable
	 * @param mapPools
	 * @return
	 */
	public static List<VariableRs> listMapStringToObject(List<Map<String, String>> mapVars) throws Exception {
		List<VariableRs> vars = new ArrayList<VariableRs>();
		if (mapVars != null && mapVars.size()>0) {
			for (Map<String, String> mapDev : mapVars) {
				VariableRs var = mapStringToObject(mapDev);
				vars.add(var);
			}
		}
		return vars;
	}
	
	public static List<VariableRs> listMapToObject(List<Map<String, Object>> mapVars) throws Exception {
		List<VariableRs> vars = new ArrayList<VariableRs>();
		if (mapVars != null && mapVars.size()>0) {
			for (Map<String, Object> mapDev : mapVars) {
				VariableRs var = mapToObject(mapDev);
				vars.add(var);
			}
		}
		return vars;
	}
	
	public static List<VariableRs> listMapToObject(Map<String, Map<String, String>> mapVars) throws Exception {
		List<VariableRs> vars = new ArrayList<VariableRs>();
		if (mapVars != null && mapVars.size()>0) {
			for (String varName : mapVars.keySet()) {
				VariableRs var = mapStringToObject(mapVars.get(varName));
				vars.add(var);
			}
		}
		return vars;
	}
	
	public static VariableRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "variable";
				JAXBContext jc = JAXBContext.newInstance( VariableRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					VariableRs o = (VariableRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<VariableRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "variable";
			List<VariableRs> listObjects = new ArrayList<VariableRs>();
			
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
	
	/**
	 * Convierte un mapa a un objeto
	 * @param mapPool
	 * @return
	 */
	public static VariableRs mapStringToObject(Map<String, String> map) throws Exception {
		VariableRs var = new VariableRs();
		String p = map.get("name");
		if (p != null && !p.isEmpty())
			var.setName(p);
		p = map.get("description");
		if (p != null && !p.isEmpty())
			var.setDescription(p);
		p = map.get("password");
		if (p != null && !p.isEmpty() && p.equals("true"))
			var.setPassword(true);
		else
			var.setPassword(false);
		
		return var;
	}
	
	
	public static Map<String, Object> objectToMap(VariableRs o) throws Exception {
		Map<String, Object> var = new HashMap<String, Object>();
		if (o.getName() != null && !o.getName().isEmpty())
			var.put("name", o.getName());
		if (o.getDescription() != null && !o.getDescription().isEmpty())
			var.put("description", o.getDescription());
		if (o.getPassword() != null && o.getPassword().booleanValue() == true)
			var.put("password", "true");
		else
			var.put("password", "false");
		return var;
	}
	
	/**
	 * Convierte un mapa a un objeto
	 * @param mapPool
	 * @return
	 */
	public static VariableRs mapToObject(Map<String, Object> map) throws Exception {
		VariableRs var = new VariableRs();
		Object p = map.get("name");
		if (p != null && !((String)p).isEmpty())
			var.setName((String) p);
		p = map.get("description");
		if (p != null && !((String)p).isEmpty())
			var.setDescription((String) p);
		p = map.get("password");
		if (p != null && !((String)p).isEmpty() && ((String)p).equals("true"))
			var.setPassword(true);
		else
			var.setPassword(false);
		
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getPassword() {
		return password;
	}

	public void setPassword(Boolean password) {
		this.password = password;
	}
}
