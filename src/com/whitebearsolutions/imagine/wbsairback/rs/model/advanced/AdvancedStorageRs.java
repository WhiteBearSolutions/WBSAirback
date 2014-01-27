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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "advancedStorage")
public class AdvancedStorageRs {

	private String name;
	private String typeConnection;
	private String typeStorage;
	
	private List<VariableRs> variables;
	private List<StepStorageRs> steps;
	
	
	public AdvancedStorageRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( AdvancedStorageRs.class );
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
	public static List<AdvancedStorageRs> listMapToObject(Map<String, Map<String, Object>> mapAdvancedStorages) throws Exception {
		List<AdvancedStorageRs> storages = new ArrayList<AdvancedStorageRs>();
		if (mapAdvancedStorages != null && mapAdvancedStorages.size()>0) {
			for (String nameScript : mapAdvancedStorages.keySet()) {
				AdvancedStorageRs storage = mapToObject(mapAdvancedStorages.get(nameScript));
				storages.add(storage);
			}
		}
		return storages;
	}
	
	public static List<AdvancedStorageRs> listMapToObject(List<Map<String, Object>> mapAdvancedStorages) throws Exception {
		List<AdvancedStorageRs> storages = new ArrayList<AdvancedStorageRs>();
		if (mapAdvancedStorages != null && mapAdvancedStorages.size()>0) {
			for (Map<String, Object> map : mapAdvancedStorages) {
				AdvancedStorageRs storage = mapToObject(map);
				storages.add(storage);
			}
		}
		return storages;
	}
	
	public static AdvancedStorageRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "advancedStorage";
				JAXBContext jc = JAXBContext.newInstance( AdvancedStorageRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					AdvancedStorageRs o = (AdvancedStorageRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<AdvancedStorageRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "advancedStorage";
			List<AdvancedStorageRs> listObjects = new ArrayList<AdvancedStorageRs>();
			
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
	public static AdvancedStorageRs mapToObject(Map<String, Object> map) throws Exception {
		AdvancedStorageRs storage = new AdvancedStorageRs();

		Object p = map.get("name");
		if (p != null && !((String)p).isEmpty())
			storage.setName((String) p);
		p = map.get("typeConnection");
		if (p != null && !((String)p).isEmpty())
			storage.setTypeConnection((String) p);
		p = map.get("typeStorage");
		if (p != null && !((String)p).isEmpty())
			storage.setTypeStorage((String) p);
		p = map.get("steps");
		if (p != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> steps = (Map<String, Object>) map.get("steps");
			if (!steps.isEmpty())
				storage.setSteps(StepStorageRs.listMapToObject(steps));
		}
		p = map.get("variables");
		if (p != null) {
			@SuppressWarnings("unchecked")
			Map<String, Map<String, String>> variables = (Map<String, Map<String, String>>) map.get("variables");
			if (!variables.isEmpty())
				storage.setVariables(VariableRs.listMapToObject(variables));
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
	
	@XmlElementWrapper(name="variables")
	@XmlElementRef()
	public List<VariableRs> getVariables() {
		return variables;
	}

	public void setVariables(List<VariableRs> variables) {
		this.variables = variables;
	}

	public String getTypeConnection() {
		return typeConnection;
	}

	public void setTypeConnection(String typeConnection) {
		this.typeConnection = typeConnection;
	}

	public String getTypeStorage() {
		return typeStorage;
	}

	public void setTypeStorage(String typeStorage) {
		this.typeStorage = typeStorage;
	}

	@XmlElementWrapper(name="steps", required=true)
	@XmlElementRef()
	public List<StepStorageRs> getSteps() {
		return steps;
	}

	public void setSteps(List<StepStorageRs> steps) {
		this.steps = steps;
	}
	
}
