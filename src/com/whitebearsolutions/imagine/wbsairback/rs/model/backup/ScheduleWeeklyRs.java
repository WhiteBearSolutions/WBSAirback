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

@XmlRootElement(name = "scheduleWeekly")
public class ScheduleWeeklyRs {

	private String name;
	
	private List<ScheduleWeeklyCalendarRs> calendar;
	
	public ScheduleWeeklyRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ScheduleWeeklyRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	public static ScheduleWeeklyRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "scheduleWeekly";
				JAXBContext jc = JAXBContext.newInstance( ScheduleWeeklyRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					ScheduleWeeklyRs o = (ScheduleWeeklyRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<ScheduleWeeklyRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "scheduleWeekly";
			List<ScheduleWeeklyRs> listObjects = new ArrayList<ScheduleWeeklyRs>();
			
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
	 * Convierte una lista de mapas de poolVolumes a objetos pool
	 * @param mapPools
	 * @return
	 */
	public static ScheduleWeeklyRs mapToObject(String name, List<Map<String, String>> mapSchedules) throws Exception{
		ScheduleWeeklyRs schedule = new ScheduleWeeklyRs();
		schedule.setName(name);
		List<ScheduleWeeklyCalendarRs> calendar = new ArrayList<ScheduleWeeklyCalendarRs>();
		if (mapSchedules != null && mapSchedules.size()>0) {
			for (Map<String, String> mapSchedule : mapSchedules) {
				ScheduleWeeklyCalendarRs cal = mapCalendarToObject(mapSchedule);
				calendar.add(cal);
			}
		}
		schedule.setCalendar(calendar);
		return schedule;
	}
	
	public static ScheduleWeeklyCalendarRs mapCalendarToObject(Map<String, String> map) throws Exception {
		
		ScheduleWeeklyCalendarRs cal = new ScheduleWeeklyCalendarRs();
		String p = map.get("level");
		if (p != null && !p.isEmpty())
			cal.setLevel(p);
		p = map.get("day");
		if (p != null && !p.isEmpty())
			cal.setDay(p);
		p = map.get("min");
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

	@XmlElementWrapper(name="weeklyCalendars", required=true)
	@XmlElementRef()
	public List<ScheduleWeeklyCalendarRs> getCalendar() {
		return calendar;
	}

	public void setCalendar(List<ScheduleWeeklyCalendarRs> calendar) {
		this.calendar = calendar;
	}
}
