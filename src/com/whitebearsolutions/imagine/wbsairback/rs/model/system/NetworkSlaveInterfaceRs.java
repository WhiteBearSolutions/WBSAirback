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

@XmlRootElement(name="networkSlaveInterface")
public class NetworkSlaveInterfaceRs {

	private String iface;
	
	public NetworkSlaveInterfaceRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkSlaveInterfaceRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static NetworkSlaveInterfaceRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkSlaveInterfaceRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			NetworkSlaveInterfaceRs o = (NetworkSlaveInterfaceRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<networkSlaveInterface>"), xml.indexOf("</networkSlaveInterface>")+"</networkSlaveInterface>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<NetworkSlaveInterfaceRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "networkSlaveInterface";
			List<NetworkSlaveInterfaceRs> listObjects = new ArrayList<NetworkSlaveInterfaceRs>();
			
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
	 * Convierte una lista de mapas de slavee a objetos slavers
	 * @param listIfaces
	 * @return
	 */
	public static List<NetworkSlaveInterfaceRs> listMapToObject(List<String> listIfaces) {
		List<NetworkSlaveInterfaceRs> listSlaves = new ArrayList<NetworkSlaveInterfaceRs>();
		if (listIfaces != null && listIfaces.size()>0) {
			for (String nameIface : listIfaces) {
				NetworkSlaveInterfaceRs j = toObject(nameIface);
				listSlaves.add(j);
			}
		}
		return listSlaves;
	}
	
	
	/**
	 * Convierte un mapa de slavee a un objeto slavers
	 * @param nameIface
	 * @return
	 */
	public static NetworkSlaveInterfaceRs toObject(String nameIface) {
		NetworkSlaveInterfaceRs slave = new NetworkSlaveInterfaceRs();
		slave.setIface(nameIface);

		return slave;
	}

	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getIface() {
		return iface;
	}

	public void setIface(String iface) {
		this.iface = iface;
	}


	
}
