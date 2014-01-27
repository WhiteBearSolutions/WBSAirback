package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

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

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;

@XmlRootElement(name="networkNameServer")
public class NetworkNameServerRs {

	private String address;
	
	public NetworkNameServerRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkNameServerRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static NetworkNameServerRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkNameServerRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			NetworkNameServerRs o = (NetworkNameServerRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<networkNameServer>"), xml.indexOf("</networkNameServer>")+"</networkNameServer>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<NetworkNameServerRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "networkNameServer";
			List<NetworkNameServerRs> listObjects = new ArrayList<NetworkNameServerRs>();
			
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
	 * Convierte una lista de mapas de networkNameServere a objetos networkNameServerrs
	 * @param mapNetworkNameServers
	 * @return
	 */
	public static List<NetworkNameServerRs> listToObjects(List<String []> listServers) {
		List<NetworkNameServerRs> jobs = new ArrayList<NetworkNameServerRs>();
		if (listServers != null && listServers.size()>0) {
			for (String[] server : listServers) {
				NetworkNameServerRs j = getObject(NetworkManager.addressToString(server));
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	
	/**
	 * Convierte un mapa de networkNameServere a un objeto networkNameServerrs
	 * @param mapNetworkNameServer
	 * @return
	 */
	public static NetworkNameServerRs getObject(String address) {
		NetworkNameServerRs networkNameServer = new NetworkNameServerRs();
		networkNameServer.setAddress(address);
		return networkNameServer;
	}


	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	
}
