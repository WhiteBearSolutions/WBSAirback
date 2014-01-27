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

@XmlRootElement(name="networkNameServersConfig")
public class NetworkNameServersConfigRs {
	
	private List<NetworkNameServerRs> nameServers;
	
	public NetworkNameServersConfigRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkNameServersConfigRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static NetworkNameServersConfigRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkNameServersConfigRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			NetworkNameServersConfigRs o = (NetworkNameServersConfigRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<networkNameServersConfig>"), xml.indexOf("</networkNameServersConfig>")+"</networkNameServersConfig>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public List<NetworkNameServerRs> getNameServers() {
		return nameServers;
	}

	public void setNameServers(List<NetworkNameServerRs> nameServers) {
		this.nameServers = nameServers;
	}
}
