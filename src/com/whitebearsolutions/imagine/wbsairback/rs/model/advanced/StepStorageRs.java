package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

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

@XmlRootElement(name = "stepStorage")
public class StepStorageRs {

	private String name;
	private Boolean mount;
	private List<ScriptItemRs> scripts;
	
	public StepStorageRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( StepStorageRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de storages a objetos storage
	 * @param mapPools
	 * @return
	 */
	public static List<StepStorageRs> listMapToObject(Map<String, Object> mapStepStorages) throws Exception {
		List<StepStorageRs> steps = new ArrayList<StepStorageRs>();
		if (mapStepStorages != null && mapStepStorages.size()>0) {
			for (String nameScript : mapStepStorages.keySet()) {
				@SuppressWarnings("unchecked")
				StepStorageRs step = mapToObject((Map<String, Object>)mapStepStorages.get(nameScript));
				steps.add(step);
			}
		}
		return steps;
	}
	
	public static StepStorageRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "stepStorage";
				JAXBContext jc = JAXBContext.newInstance( StepStorageRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					StepStorageRs o = (StepStorageRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<StepStorageRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "stepStorage";
			List<StepStorageRs> listObjects = new ArrayList<StepStorageRs>();
			
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
	public static StepStorageRs mapToObject(Map<String, Object> map) throws Exception {
		StepStorageRs storage = new StepStorageRs();

		Object p = map.get("name");
		if (p != null && !((String)p).isEmpty())
			storage.setName((String) p);
		p = map.get("mount");
		if (p != null && !((String)p).isEmpty() && ((String)p).equals("true"))
			storage.setMount(true);
		else
			storage.setMount(false);
		p = map.get("scripts");
		if (p != null) {
			@SuppressWarnings("unchecked")
			Map<Integer, Object> scripts = (Map<Integer, Object>) map.get("scripts");
			if (!scripts.isEmpty())
				storage.setScripts(ScriptItemRs.listMapStringToObject(scripts));
		}
		
		return storage;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getMount() {
		return mount;
	}

	public void setMount(Boolean mount) {
		this.mount = mount;
	}

	public List<ScriptItemRs> getScripts() {
		return scripts;
	}

	public void setScripts(List<ScriptItemRs> scripts) {
		this.scripts = scripts;
	}

}
