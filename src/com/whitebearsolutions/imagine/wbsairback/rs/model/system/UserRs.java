package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.whitebearsolutions.imagine.wbsairback.UserManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.CategoryRs;

@XmlRootElement(name = "user")
public class UserRs {

	private String name;
	private String password;
	private String description;
	private List<CategoryRs> categories;
	private List<RoleRs> roles;
	
	public UserRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( UserRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de  a objetos 
	 * @param mapPools
	 * @return
	 */
	public static List<UserRs> listMapToObject(List<Map<String, String>> maps, UserManager um) throws Exception {
		List<UserRs> users = new ArrayList<UserRs>();
		if (maps != null && maps.size()>0) {
			for (Map<String, String> map : maps) {
				UserRs agg = mapToObject(map, um.getUserRoles(map.get("uid")));
				users.add(agg);
			}
		}
		return users;
	}
	
	public static UserRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "user";
				JAXBContext jc = JAXBContext.newInstance( UserRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					UserRs o = (UserRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<UserRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "user";
			List<UserRs> listObjects = new ArrayList<UserRs>();
			
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
	public static UserRs mapToObject(Map<String, String> map, List<String> roles) throws Exception {
		UserRs user = new UserRs();
		if (map.get("uid") != null && !map.get("uid").isEmpty())
			user.setName(map.get("uid"));
		if (map.get("category") != null && !map.get("category").equals("")) {
			List<CategoryRs> categories = new ArrayList<CategoryRs>();
			List<String> cats = Arrays.asList(map.get("category").split(","));
			for (String cat : cats) {
				CategoryRs c = new CategoryRs();
				c.setName(cat);
				categories.add(c);
			}
			user.setCategories(categories);
		}
		if (map.get("description") != null && !map.get("description").isEmpty())
			user.setDescription(map.get("description"));
		if (map.get("password") != null && !map.get("password").isEmpty())
			user.setPassword(map.get("password"));
		if (roles != null && !roles.isEmpty()) {
			List<RoleRs> rolesRs = new ArrayList<RoleRs>();
			for (String nameRole : roles) {
				RoleRs role = new RoleRs();
				role.setName(nameRole);
				rolesRs.add(role);
			}
			user.setRoles(rolesRs);
		}
		return user;
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


	@XmlElementWrapper(name="roles")
	@XmlElementRef()
	public List<RoleRs> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleRs> roles) {
		this.roles = roles;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlElementWrapper(name="categories")
	@XmlElementRef()
	public List<CategoryRs> getCategories() {
		return categories;
	}

	public void setCategories(List<CategoryRs> categories) {
		this.categories = categories;
	}
	
	public List<String> getCategoriesList() {
		List<String> listCat = new ArrayList<String>();
		if (categories != null && !categories.isEmpty()) {
			for (CategoryRs cat : categories)
				listCat.add(cat.getName());
		}
		return listCat;
	}
	
}
