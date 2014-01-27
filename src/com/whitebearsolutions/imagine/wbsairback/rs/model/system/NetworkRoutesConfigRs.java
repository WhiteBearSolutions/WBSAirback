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

@XmlRootElement(name="networkRoutesConfig")
public class NetworkRoutesConfigRs {
	
	private List<NetworkRouteRs> routes;
	
	public NetworkRoutesConfigRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkRoutesConfigRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static NetworkRoutesConfigRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( NetworkRoutesConfigRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			NetworkRoutesConfigRs o = (NetworkRoutesConfigRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<networkRoutesConfig>"), xml.indexOf("</networkRoutesConfig>")+"</networkRoutesConfig>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public List<NetworkRouteRs> getRoutes() {
		return routes;
	}

	public void setRoutes(List<NetworkRouteRs> routes) {
		this.routes = routes;
	}
}
