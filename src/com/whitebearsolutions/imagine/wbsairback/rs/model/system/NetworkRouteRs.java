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

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;

@XmlRootElement(name="networkRoute")
public class NetworkRouteRs {

	private String iface;
	private String address;
	private String netmask;
	private String gateway;
	
	public NetworkRouteRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkRouteRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static NetworkRouteRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkRouteRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			NetworkRouteRs o = (NetworkRouteRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<networkRoute>"), xml.indexOf("</networkRoute>")+"</networkRoute>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<NetworkRouteRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "networkRoute";
			List<NetworkRouteRs> listObjects = new ArrayList<NetworkRouteRs>();
			
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
	 * Convierte una lista de mapas de networkInterfacee a objetos networkInterfacers
	 * @param mapNetworkInterfaces
	 * @return
	 */
	public static List<NetworkRouteRs> listToObjects(List<Map<String, String[]>> maps, String nameIface) {
		List<NetworkRouteRs> routes = new ArrayList<NetworkRouteRs>();
		if (maps != null && maps.size()>0) {
			for (Map<String, String[]> map : maps) {
				NetworkRouteRs j = mapToObject(nameIface, map);
				routes.add(j);
			}
		}
		return routes;
	}
	
	
	/**
	 * Convierte un mapa de networkInterfacee a un objeto networkInterfacers
	 * @param mapNetworkInterface
	 * @return
	 */
	public static NetworkRouteRs mapToObject(String nameIface, Map<String, String[]> map) {
		NetworkRouteRs route = new NetworkRouteRs();
		route.setIface(nameIface);
		
		if (map.get("address") != null)
			route.setAddress(NetworkManager.addressToString(map.get("address")));
		if (map.get("netmask") != null)
			route.setNetmask(NetworkManager.addressToString(map.get("netmask")));
		if (map.get("gateway") != null)
			route.setGateway(NetworkManager.addressToString(map.get("gateway")));
	
		return route;
	}

	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@XmlElement(required=true)
	public String getNetmask() {
		return netmask;
	}

	public void setNetmask(String netmask) {
		this.netmask = netmask;
	}

	@XmlElement(required=true)
	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	@XmlElement(required=true)
	public String getIface() {
		return iface;
	}

	public void setIface(String iface) {
		this.iface = iface;
	}
	
}
