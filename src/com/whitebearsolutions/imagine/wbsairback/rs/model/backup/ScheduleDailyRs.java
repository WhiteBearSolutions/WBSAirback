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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;


@XmlRootElement(name = "scheduleDaily")
public class ScheduleDailyRs {

	private String name;
	private List<ScheduleDailyCalendarRs> calendar;
	
	public ScheduleDailyRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ScheduleDailyRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	public static ScheduleDailyRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "scheduleDaily";
				JAXBContext jc = JAXBContext.newInstance( ScheduleDailyRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					ScheduleDailyRs o = (ScheduleDailyRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<ScheduleDailyRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "scheduleDaily";
			List<ScheduleDailyRs> listObjects = new ArrayList<ScheduleDailyRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String deviceXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(deviceXml));
						if (list.length() > list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()) {
							list = list.substring(list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						} else {
							break;
						}
					}
				}
			}
			return listObjects;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Convierte una lista de mapas de  a objetos 
	 * @param mapPools
	 * @return
	 */
	public static ScheduleDailyRs mapToObject(String name, List<Map<String, String>> mapSchedules) throws Exception{
		ScheduleDailyRs schedule = new ScheduleDailyRs();
		schedule.setName(name);
		List<ScheduleDailyCalendarRs> calendar = new ArrayList<ScheduleDailyCalendarRs>();
		if (mapSchedules != null && mapSchedules.size()>0) {
			for (Map<String, String> mapSchedule : mapSchedules) {
				ScheduleDailyCalendarRs cal = mapCalendarToObject(mapSchedule);
				calendar.add(cal);
			}
		}
		schedule.setCalendar(calendar);
		return schedule;
	}
	
	public static ScheduleDailyCalendarRs mapCalendarToObject(Map<String, String> map) throws Exception {
		ScheduleDailyCalendarRs cal = new ScheduleDailyCalendarRs();
		String p = map.get("level");
		if (p != null && !p.isEmpty())
			cal.setLevel(p);
		p = map.get("");
		if (p != null && !p.isEmpty())
			cal.setPool(p);
		p = map.get("minute");
		if (p != null && !p.isEmpty())
			cal.setMinute(Integer.parseInt(p));
		p = map.get("hour");
		if (p != null && !p.isEmpty()) 
			cal.setMinute(Integer.parseInt(p));
	
		return cal;
	}

	
	// ############################   	GETTERS Y SETTERS 	######################################
	
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElementWrapper(name="dailyCalendars", required=true)
	@XmlElementRef()
	public List<ScheduleDailyCalendarRs> getCalendar() {
		return calendar;
	}

	public void setCalendar(List<ScheduleDailyCalendarRs> calendar) {
		this.calendar = calendar;
	}
}
