package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "quotaAdd")
public class QuotaAddRs {

	private String name;
	private String groupName;
	private String volumeName;
	private Double size;
	
	public QuotaAddRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( QuotaAddRs.class );
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
	public static QuotaAddRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( QuotaAddRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			QuotaAddRs o = (QuotaAddRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<quotaAdd>"), xml.indexOf("</quotaAdd>")+"</quotaAdd>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
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
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@XmlElement(required=true)
	public String getVolumeName() {
		return volumeName;
	}

	public void setVolumeName(String volumeName) {
		this.volumeName = volumeName;
	}

	@XmlElement(required=true)
	public Double getSize() {
		return size;
	}

	public void setSize(Double size) {
		this.size = size;
	}
	


	
}
