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

@XmlRootElement(name = "hypervisor")
public class HypervisorRs {
	
	private String name;
	private String os;
	private String address;
	private String user;
	private String password;
	
	public HypervisorRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de hypervisor a objetos hypervisor
	 * @param maps
	 * @return
	 */
	public static List<HypervisorRs> listMapToObject(List<Map<String, String>> mapHypervisors) throws Exception {
		List<HypervisorRs> hypervisors = new ArrayList<HypervisorRs>();
		if (mapHypervisors != null && mapHypervisors.size()>0) {
			for (Map<String, String> mapHypervisor : mapHypervisors) {
				HypervisorRs hypervisor = mapToObject(mapHypervisor);
				hypervisors.add(hypervisor);
			}
		}
		return hypervisors;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static HypervisorRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			HypervisorRs o = (HypervisorRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<hypervisor>"), xml.indexOf("</hypervisor>")+"</hypervisor>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	/**
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static HypervisorRs mapToObject(Map<String, String> map) throws Exception {
		HypervisorRs hypervisor = new HypervisorRs();
		
		if (map.get("address") != null && !map.get("address").isEmpty())
			hypervisor.setAddress(map.get("address"));
		if (map.get("name") != null && !map.get("name").isEmpty())
			hypervisor.setName(map.get("name"));
		if (map.get("os") != null && !map.get("os").isEmpty())
			hypervisor.setOs(map.get("os"));
		if (map.get("password") != null && !map.get("password").isEmpty())
			hypervisor.setPassword(map.get("password"));
		if (map.get("user") != null && !map.get("user").isEmpty())
			hypervisor.setUser(map.get("user"));
		
		return hypervisor;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(required=true)
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	@XmlElement(required=true)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	

}
