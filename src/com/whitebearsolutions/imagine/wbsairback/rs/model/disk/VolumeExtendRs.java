package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "volumeExtend")
public class VolumeExtendRs {
	
	private String aggregate;
	private String name;
	private Integer size;
	private String size_type;
	private Double data_size;
	private Double data_reservation;
	private Double total_reservation;
	
	private Boolean snapshotHourly;
	private Integer snapshotHourlyRetention;
	
	private Boolean snapshotDaily;
	private Integer snapshotDailyHour;
	private Integer snapshotDailyRetention;
	
	private Boolean snapshotManualRemove;
	
	public VolumeExtendRs(){}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( VolumeExtendRs.class );
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

	@XmlElement(required=true)
	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@XmlElement(required=true)
	public String getSize_type() {
		return size_type;
	}

	public void setSize_type(String size_type) {
		this.size_type = size_type;
	}

	public Double getData_reservation() {
		return data_reservation;
	}

	public void setData_reservation(Double data_reservation) {
		this.data_reservation = data_reservation;
	}

	public Integer getSnapshotHourlyRetention() {
		return snapshotHourlyRetention;
	}

	public void setSnapshotHourlyRetention(Integer snapshotHourlyRetention) {
		this.snapshotHourlyRetention = snapshotHourlyRetention;
	}

	public Integer getSnapshotDailyHour() {
		return snapshotDailyHour;
	}

	public void setSnapshotDailyHour(Integer snapshotDailyHour) {
		this.snapshotDailyHour = snapshotDailyHour;
	}

	public Integer getSnapshotDailyRetention() {
		return snapshotDailyRetention;
	}

	public void setSnapshotDailyRetention(Integer snapshotDailyRetention) {
		this.snapshotDailyRetention = snapshotDailyRetention;
	}

	public Boolean getSnapshotHourly() {
		return snapshotHourly;
	}

	public void setSnapshotHourly(Boolean snapshotHourly) {
		this.snapshotHourly = snapshotHourly;
	}

	public Boolean getSnapshotDaily() {
		return snapshotDaily;
	}

	public void setSnapshotDaily(Boolean snapshotDaily) {
		this.snapshotDaily = snapshotDaily;
	}

	public Double getData_size() {
		return data_size;
	}

	public void setData_size(Double data_size) {
		this.data_size = data_size;
	}

	public Double getTotal_reservation() {
		return total_reservation;
	}

	public void setTotal_reservation(Double total_reservation) {
		this.total_reservation = total_reservation;
	}

	public Boolean getSnapshotManualRemove() {
		return snapshotManualRemove;
	}

	public void setSnapshotManualRemove(Boolean snapshotManualRemove) {
		this.snapshotManualRemove = snapshotManualRemove;
	}
}
