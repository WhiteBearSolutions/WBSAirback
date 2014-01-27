package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

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


@XmlRootElement(name = "proxy")
public class ProxyRs {

	private String server;
	private String user;
	private String password;
	private Integer port;
	
	public ProxyRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ProxyRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas a objetos
	 * @param mapPools
	 * @return
	 */
	public static List<ProxyRs> listMapToObject(List<Map<String, String>> maps) throws Exception {
		List<ProxyRs> proxys = new ArrayList<ProxyRs>();
		if (maps != null && maps.size()>0) {
			for (Map<String, String> map : maps) {
				ProxyRs prox = mapToObject(map);
				proxys.add(prox);
			}
		}
		return proxys;
	}
	
	public static ProxyRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "proxy";
				JAXBContext jc = JAXBContext.newInstance( ProxyRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					ProxyRs o = (ProxyRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<ProxyRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "proxy";
			List<ProxyRs> listObjects = new ArrayList<ProxyRs>();
			
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
	public static ProxyRs mapToObject(Map<String, String> map) throws Exception {
		ProxyRs proxy = new ProxyRs();
		if (map != null && !map.isEmpty()) {
			if (map.get("password") != null && !map.get("password").isEmpty())
				proxy.setPassword(map.get("password"));
			if (map.get("user") != null && !map.get("user").isEmpty())
				proxy.setUser(map.get("user"));
			if (map.get("server") != null && !map.get("server").isEmpty())
				proxy.setServer(map.get("server"));
			if (map.get("port") != null)
				proxy.setPort(Integer.parseInt(map.get("port")));
		}
		
		return proxy;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
}
