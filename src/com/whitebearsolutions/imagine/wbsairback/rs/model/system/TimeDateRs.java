package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name="timeDate")
public class TimeDateRs {

	private Integer year;
	private Integer month;
	private Integer day;
	private Integer hour;
	private Integer minute;
	private Integer second;
	private String timeOfDay;
	
	public TimeDateRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeDateRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static TimeDateRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( TimeDateRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			TimeDateRs o = (TimeDateRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<timeDate>"), xml.indexOf("</timeDate>")+"</timeDate>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	/**
	 * Convierte un mapa de timeDatee a un objeto timeDaters
	 * @param mapTimeDate
	 * @return
	 */
	public static TimeDateRs calToObject(Calendar cal) {
		TimeDateRs timeDate = new TimeDateRs();
		
		if (cal != null) {
			timeDate.setDay(cal.get(Calendar.DAY_OF_MONTH));
			timeDate.setMonth(cal.get(Calendar.MONTH));
			timeDate.setYear(cal.get(Calendar.YEAR));
			if (cal.get(Calendar.AM_PM) == Calendar.AM)
				timeDate.setTimeOfDay("am");
			else if (cal.get(Calendar.PM) == Calendar.PM)
				timeDate.setTimeOfDay("pm");
			else
				timeDate.setTimeOfDay("am_pm");
			timeDate.setHour(cal.get(Calendar.HOUR));
			timeDate.setMinute(cal.get(Calendar.MINUTE));
			timeDate.setSecond(cal.get(Calendar.SECOND));
		}

		return timeDate;
	}
	
	// ################# GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	@XmlElement(required=true)
	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	@XmlElement(required=true)
	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	@XmlElement(required=true)
	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	@XmlElement(required=true)
	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	public Integer getSecond() {
		return second;
	}

	public void setSecond(Integer second) {
		this.second = second;
	}

	public String getTimeOfDay() {
		return timeOfDay;
	}

	public void setTimeOfDay(String timeOfDay) {
		this.timeOfDay = timeOfDay;
	}
	
}
