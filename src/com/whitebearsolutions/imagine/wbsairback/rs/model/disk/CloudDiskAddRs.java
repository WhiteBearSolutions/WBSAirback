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

@XmlRootElement(name = "cloudDisk")
public class CloudDiskAddRs {

	private String accountAlias;
	private Integer size;
	private String sizeUnit;
	private String type;
	private String bucket;
	
	public CloudDiskAddRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudDiskAddRs.class );
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
	public static CloudDiskAddRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudDiskAddRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			CloudDiskAddRs o = (CloudDiskAddRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<cloudDiskAdd>"), xml.indexOf("</cloudDiskAdd>")+"</cloudDiskAdd>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getAccountAlias() {
		return accountAlias;
	}

	public void setAccountAlias(String accountAlias) {
		this.accountAlias = accountAlias;
	}

	@XmlElement(required=true)
	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@XmlElement(required=true)
	public String getSizeUnit() {
		return sizeUnit;
	}

	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}

	@XmlElement(required=true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
}
