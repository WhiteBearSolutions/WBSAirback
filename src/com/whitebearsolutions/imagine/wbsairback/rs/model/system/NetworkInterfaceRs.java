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

@XmlRootElement(name="networkInterface")
public class NetworkInterfaceRs {

	private String iface;
	private String address;
	private String netmask;
	private String gateway;
	
	private String bondtype;
	private List<NetworkSlaveInterfaceRs> slaves;
	
	public NetworkInterfaceRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkInterfaceRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static NetworkInterfaceRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkInterfaceRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			NetworkInterfaceRs o = (NetworkInterfaceRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<networkInterface>"), xml.indexOf("</networkInterface>")+"</networkInterface>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<NetworkInterfaceRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "networkInterface";
			List<NetworkInterfaceRs> listObjects = new ArrayList<NetworkInterfaceRs>();
			
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
	public static List<NetworkInterfaceRs> listToObjects(List<String> listNetworkInterfaces, NetworkManager nm) {
		List<NetworkInterfaceRs> jobs = new ArrayList<NetworkInterfaceRs>();
		if (listNetworkInterfaces != null && listNetworkInterfaces.size()>0) {
			for (String nameIface : listNetworkInterfaces) {
				NetworkInterfaceRs j = getObject(nameIface, nm);
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	
	/**
	 * Convierte un mapa de networkInterfacee a un objeto networkInterfacers
	 * @param mapNetworkInterface
	 * @return
	 */
	public static NetworkInterfaceRs getObject(String _interface, NetworkManager nm) {
		NetworkInterfaceRs iface = new NetworkInterfaceRs();
		iface.setIface(_interface);
		
		iface.setAddress(NetworkManager.addressToString(nm.getAddress(_interface)));
		iface.setNetmask(NetworkManager.addressToString(nm.getNetmask(_interface)));
		String [] gateway = nm.getGateway(_interface);
		if (gateway[0] != "")
			iface.setGateway(NetworkManager.addressToString(gateway));
		if(nm.hasSlaves(_interface)) {
			List<String> slaves = nm.getSlaves(_interface);
			List<NetworkSlaveInterfaceRs> netSlaves = new ArrayList<NetworkSlaveInterfaceRs>();
			for (String slave : slaves) {
				NetworkSlaveInterfaceRs netSlave = new NetworkSlaveInterfaceRs();
				netSlave.setIface(slave);
				netSlaves.add(netSlave);
			}
			if (nm.getBondType(_interface) == NetworkManager.BONDING_RR)
				iface.setBondtype("roundrobin");
			else if (nm.getBondType(_interface) == NetworkManager.BONDING_LACP)
				iface.setBondtype("lacp");
			iface.setSlaves(netSlaves);
		}
		
		return iface;
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

	public List<NetworkSlaveInterfaceRs> getSlaves() {
		return slaves;
	}

	public void setSlaves(List<NetworkSlaveInterfaceRs> slaves) {
		this.slaves = slaves;
	}

	public String getBondtype() {
		return bondtype;
	}

	public void setBondtype(String bondtype) {
		this.bondtype = bondtype;
	}
	
}
