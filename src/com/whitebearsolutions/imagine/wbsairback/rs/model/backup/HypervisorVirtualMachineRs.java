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

@XmlRootElement(name = "hypervisorVirtualMachine")
public class HypervisorVirtualMachineRs {

	private String name;
	private String datastore;
	private String network;
	private String snapshot;
	
	public HypervisorVirtualMachineRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorVirtualMachineRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static HypervisorVirtualMachineRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorVirtualMachineRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			HypervisorVirtualMachineRs o = (HypervisorVirtualMachineRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<hypervisorVirtualMachine>"), xml.indexOf("</hypervisorVirtualMachine>")+"</hypervisorVirtualMachine>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	public static List<HypervisorVirtualMachineRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "hypervisorVirtualMachine";
			List<HypervisorVirtualMachineRs> listObjects = new ArrayList<HypervisorVirtualMachineRs>();
			
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
	 * Convierte una lista de mapas de vm a objetos vm
	 * @param maps
	 * @return
	 */
	public static List<HypervisorVirtualMachineRs> listMapToObject(List<Map<String, String>> listMap) throws Exception {
		List<HypervisorVirtualMachineRs> vms = new ArrayList<HypervisorVirtualMachineRs>();
		if (listMap != null && listMap.size()>0) {
			for (Map<String, String> map : listMap) {
				HypervisorVirtualMachineRs vm = mapToObject(map);
				vms.add(vm);
			}
		}
		return vms;
	}
	
	
	/**
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static HypervisorVirtualMachineRs mapToObject(Map<String, String> map) throws Exception {
		HypervisorVirtualMachineRs vm = new HypervisorVirtualMachineRs();
		
		if (map.get("datastore") != null && map.get("datastore").isEmpty()) 
			vm.setDatastore(map.get("datastore"));
		if (map.get("name") != null && map.get("name").isEmpty())
			vm.setName(map.get("name"));
		if (map.get("network") != null && map.get("network").isEmpty())	
			vm.setNetwork(map.get("network"));
		if (map.get("snapshot") != null && map.get("snapshot").isEmpty())
			vm.setSnapshot(map.get("snapshot"));
				
		return vm;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatastore() {
		return datastore;
	}

	public void setDatastore(String datastore) {
		this.datastore = datastore;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}
	
}
