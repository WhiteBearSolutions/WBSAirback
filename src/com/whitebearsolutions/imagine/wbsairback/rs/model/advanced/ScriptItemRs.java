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

@XmlRootElement(name = "scriptItem")
public class ScriptItemRs {
	
	private Integer order;
	private String content;
	private String shell;
	
	public ScriptItemRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ScriptItemRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	/**
	 * Convierte una lista de mapas de scriptItems a objetos scriptItem
	 * @param mapPools
	 * @return
	 */
	public static List<ScriptItemRs> listMapToObject(List<Map<String, Object>> mapScripts) throws Exception {
		List<ScriptItemRs> scripts = new ArrayList<ScriptItemRs>();
		if (mapScripts != null && mapScripts.size()>0) {
			for (Map<String, Object> mapDev : mapScripts) {
				ScriptItemRs script = mapToObject(mapDev);
				scripts.add(script);
			}
		}
		return scripts;
	}
	
	public static List<ScriptItemRs> listMapStringToObject(List<Map<String, String>> mapScripts) throws Exception {
		List<ScriptItemRs> scripts = new ArrayList<ScriptItemRs>();
		if (mapScripts != null && mapScripts.size()>0) {
			for (Map<String, String> mapDev : mapScripts) {
				ScriptItemRs script = mapStringToObject(mapDev);
				scripts.add(script);
			}
		}
		return scripts;
	}
	
	public static List<ScriptItemRs> listMapStringToObject(Map<Integer, Object> mapScripts) throws Exception {
		List<ScriptItemRs> scripts = new ArrayList<ScriptItemRs>();
		if (mapScripts != null && mapScripts.size()>0) {
			for (Integer order : mapScripts.keySet()) {
				ScriptItemRs script = mapIntegerToObject(order, mapScripts.get(order));
				scripts.add(script);
			}
		}
		return scripts;
	}
	
	public static ScriptItemRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "scriptItem";
				JAXBContext jc = JAXBContext.newInstance( ScriptItemRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					ScriptItemRs o = (ScriptItemRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<ScriptItemRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "scriptItem";
			List<ScriptItemRs> listObjects = new ArrayList<ScriptItemRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String scriptItemXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(scriptItemXml));
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
	public static ScriptItemRs mapStringToObject(Map<String, String> map) throws Exception {
		ScriptItemRs script = new ScriptItemRs();
		String p = map.get("order");
		if (p != null && !p.isEmpty())
			script.setOrder(Integer.parseInt(p));
		p = map.get("content");
		if (p != null && !p.isEmpty())
			script.setContent(p);
		p = map.get("shell");
		if (p != null && !p.isEmpty())
			script.setShell(p);
		return script;
	}
	
	public static ScriptItemRs mapIntegerToObject(Integer order, Object content) throws Exception {
		ScriptItemRs script = new ScriptItemRs();
		script.setOrder(order);
		script.setContent((String) content);
		return script;
	}
	
	public static ScriptItemRs mapToObject(Map<String, Object> map) throws Exception {
		ScriptItemRs script = new ScriptItemRs();
		Object p = map.get("order");
		if (p != null && !((String)p).isEmpty())
			script.setOrder(Integer.parseInt((String)p));
		p = map.get("content");
		if (p != null && !((String)p).isEmpty())
			script.setContent((String)p);
		p = map.get("shell");
		if (p != null && !((String)p).isEmpty())
			script.setShell((String)p);
		return script;
	}
	
	public static Map<String, Object> objectToMap(ScriptItemRs o) throws Exception {
		Map<String, Object> si = new HashMap<String, Object>();
		if (o.getOrder() != null)
			si.put("order", o.getOrder().toString());
		if (o.getContent() != null && !o.getContent().isEmpty())
			si.put("content", o.getContent());
		if (o.getShell() != null && !o.getShell().isEmpty())
			si.put("shell", o.getShell());
		return si;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@XmlElement(required=true)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getShell() {
		return shell;
	}

	public void setShell(String shell) {
		this.shell = shell;
	}	
}
