package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name="networkInterfacesConfig")
public class NetworkInterfacesConfigRs {

	private List<NetworkInterfaceRs> interfaces;
	
	public NetworkInterfacesConfigRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkInterfacesConfigRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static NetworkInterfacesConfigRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkInterfacesConfigRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			NetworkInterfacesConfigRs o = (NetworkInterfacesConfigRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<networkInterfacesConfig>"), xml.indexOf("</networkInterfacesConfig>")+"</networkInterfacesConfig>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public List<NetworkInterfaceRs> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<NetworkInterfaceRs> interfaces) {
		this.interfaces = interfaces;
	}
}
