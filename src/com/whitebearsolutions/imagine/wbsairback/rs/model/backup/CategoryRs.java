package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

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

@XmlRootElement(name="category")
public class CategoryRs {

	private String name;
	private String description;
	private String email;
	
	public CategoryRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( CategoryRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static CategoryRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( CategoryRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			CategoryRs o = (CategoryRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<category>"), xml.indexOf("</category>")+"</category>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	/**
	 * Convierte una lista de mapas de categorye a objetos categoryrs
	 * @param mapCategorys
	 * @return
	 */
	public static List<CategoryRs> listMapToObject(List<Map<String, String>> mapCategorys) {
		List<CategoryRs> jobs = new ArrayList<CategoryRs>();
		if (mapCategorys != null && mapCategorys.size()>0) {
			for (Map<String, String> mapCategory : mapCategorys) {
				CategoryRs j = mapToObject(mapCategory);
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	
	/**
	 * Convierte un mapa de categorye a un objeto categoryrs
	 * @param mapCategory
	 * @return
	 */
	public static CategoryRs mapToObject(Map<String, String> mapCategory) {
		CategoryRs category = new CategoryRs();
		
		if (mapCategory.get("description") != null && !mapCategory.get("description").equals(""))
			category.setDescription(mapCategory.get("description"));
		if (mapCategory.get("name") != null && !mapCategory.get("name").equals(""))
			category.setName(mapCategory.get("name"));
		if (mapCategory.get("mail") != null && !mapCategory.get("mail").equals(""))
			category.setEmail(mapCategory.get("mail"));
		
		return category;
	}

	
	// ################# GETTERS Y SETTERS #################################
	
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	
}
