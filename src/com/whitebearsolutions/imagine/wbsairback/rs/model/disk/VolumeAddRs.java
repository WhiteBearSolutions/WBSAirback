package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "volumeAdd")
public class VolumeAddRs {
	
	private String aggregate;
	private String name;
	private String storage_type;
	private String lv_type;
	private Long size;
	private String size_units;
	private String compression;
	private Boolean encryption;
	private Boolean deduplication;
	private Double percent_snapshot;
	
	public VolumeAddRs(){}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( VolumeAddRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getAggregate() {
		return aggregate;
	}

	public void setAggregate(String aggregate) {
		this.aggregate = aggregate;
	}

	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLv_type() {
		return lv_type;
	}

	public void setLv_type(String lv_type) {
		this.lv_type = lv_type;
	}

	@XmlElement(required=true)
	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	@XmlElement(required=true)
	public String getSize_units() {
		return size_units;
	}

	public void setSize_units(String size_units) {
		this.size_units = size_units;
	}

	public String getCompression() {
		return compression;
	}

	public void setCompression(String compression) {
		this.compression = compression;
	}

	public Boolean getEncryption() {
		return encryption;
	}

	public void setEncryption(Boolean encryption) {
		this.encryption = encryption;
	}

	public Boolean getDeduplication() {
		return deduplication;
	}

	public void setDeduplication(Boolean deduplication) {
		this.deduplication = deduplication;
	}

	public String getStorage_type() {
		return storage_type;
	}

	public void setStorage_type(String storage_type) {
		this.storage_type = storage_type;
	}

	public Double getPercent_snapshot() {
		return percent_snapshot;
	}

	public void setPercent_snapshot(Double percent_snapshot) {
		this.percent_snapshot = percent_snapshot;
	}	
}
