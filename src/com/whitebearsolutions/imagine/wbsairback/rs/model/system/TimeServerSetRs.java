package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name="timeServerSet")
public class TimeServerSetRs {

	private TimeServerRs server1;
	private TimeServerRs server2;
	private TimeServerRs server3;
	
	public TimeServerSetRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeServerSetRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static TimeServerSetRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeServerSetRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			TimeServerSetRs o = (TimeServerSetRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<timeServerSet>"), xml.indexOf("</timeServerSet>")+"</timeServerSet>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public TimeServerRs getServer1() {
		return server1;
	}

	public void setServer1(TimeServerRs server1) {
		this.server1 = server1;
	}

	public TimeServerRs getServer2() {
		return server2;
	}

	public void setServer2(TimeServerRs server2) {
		this.server2 = server2;
	}

	public TimeServerRs getServer3() {
		return server3;
	}

	public void setServer3(TimeServerRs server3) {
		this.server3 = server3;
	}
	
}
