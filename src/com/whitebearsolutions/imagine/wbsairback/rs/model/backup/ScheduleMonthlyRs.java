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

@XmlRootElement(name = "scheduleMonthly")
public class ScheduleMonthlyRs {

	private String name;
	
	private String fullPool;
	private Integer fullHour;
	private Integer fullMinute;
	private List<ScheduleMonthlyCalendarRs> calendarFull;
	
	private String incrementalPool;
	private Integer incrementalHour;
	private Integer incrementalMinute;
	private List<ScheduleMonthlyCalendarRs> calendarIncremental;
	
	private String differentialPool;
	private Integer differentialHour;
	private Integer differentialMinute;
	private List<ScheduleMonthlyCalendarRs> calendarDifferential;
	
	private String virtualPool;
	private Integer virtualHour;
	private Integer virtualMinute;
	private List<ScheduleMonthlyCalendarRs> calendarVirtual;
	
	
	public ScheduleMonthlyRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ScheduleMonthlyRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	public static ScheduleMonthlyRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "scheduleMonthly";
				JAXBContext jc = JAXBContext.newInstance( ScheduleMonthlyRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					ScheduleMonthlyRs o = (ScheduleMonthlyRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<ScheduleMonthlyRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "scheduleMonthly";
			List<ScheduleMonthlyRs> listObjects = new ArrayList<ScheduleMonthlyRs>();
			
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
	public static List<ScheduleMonthlyRs> listMapToObject(List<Map<String, String>> mapSchedules) throws Exception{
		List<ScheduleMonthlyRs> schedules = new ArrayList<ScheduleMonthlyRs>();
		if (mapSchedules != null && mapSchedules.size()>0) {
			for (Map<String, String> mapSchedule : mapSchedules) {
				ScheduleMonthlyRs schedule = mapToObject(mapSchedule);
				schedules.add(schedule);
			}
		}
		return schedules;
	}
	
	public static ScheduleMonthlyRs mapToObject(Map<String, String> map) throws Exception {
		ScheduleMonthlyRs object = new ScheduleMonthlyRs();
		
		String p = map.get("name");
		if (p != null && !p.isEmpty())
			object.setName(p);
		
		p = map.get("full.pool");
		if (p != null && !p.isEmpty())
			object.setFullPool(p);
		
		p = map.get("full.hour");
		if (p != null && !p.isEmpty())
			object.setFullHour(Integer.parseInt(p));
		
		p = map.get("full.minute");
		if (p != null && !p.isEmpty())
			object.setFullMinute(Integer.parseInt(p));
		
		List<ScheduleMonthlyCalendarRs> calendarFull = new ArrayList<ScheduleMonthlyCalendarRs>();
		for(int r = 1; r <= 35; r++) {
			p = map.get("calendar.fd" + r);
			if (p != null && !p.isEmpty()) {
				ScheduleMonthlyCalendarRs cal = new ScheduleMonthlyCalendarRs();
				cal.fromNumRepresentation(r);
				calendarFull.add(cal);
			}
				
		}
		if (calendarFull.size()>0)
			object.setCalendarFull(calendarFull);
		
		
		p = map.get("incremental.pool");
		if (p != null && !p.isEmpty())
			object.setIncrementalPool(p);
		
		p = map.get("incremental.hour");
		if (p != null && !p.isEmpty())
			object.setIncrementalHour(Integer.parseInt(p));
		
		p = map.get("incremental.minute");
		if (p != null && !p.isEmpty())
			object.setIncrementalMinute(Integer.parseInt(p));
		
		List<ScheduleMonthlyCalendarRs> calendarIncremental = new ArrayList<ScheduleMonthlyCalendarRs>();
		for(int r = 1; r <= 35; r++) {
			p = map.get("calendar.id" + r);
			if (p != null && !p.isEmpty()) {
				ScheduleMonthlyCalendarRs cal = new ScheduleMonthlyCalendarRs();
				cal.fromNumRepresentation(r);
				calendarIncremental.add(cal);
			}
				
		}
		if (calendarIncremental.size()>0)
			object.setCalendarIncremental(calendarIncremental);
		
		p = map.get("differential.pool");
		if (p != null && !p.isEmpty())
			object.setDifferentialPool(p);
		
		p = map.get("differential.hour");
		if (p != null && !p.isEmpty())
			object.setDifferentialHour(Integer.parseInt(p));
		
		p = map.get("differential.minute");
		if (p != null && !p.isEmpty())
			object.setDifferentialMinute(Integer.parseInt(p));
		
		List<ScheduleMonthlyCalendarRs> calendarDifferential = new ArrayList<ScheduleMonthlyCalendarRs>();
		for(int r = 1; r <= 35; r++) {
			p = map.get("calendar.dd" + r);
			if (p != null && !p.isEmpty()) {
				ScheduleMonthlyCalendarRs cal = new ScheduleMonthlyCalendarRs();
				cal.fromNumRepresentation(r);
				calendarDifferential.add(cal);
			}
				
		}
		if (calendarDifferential.size()>0)
			object.setCalendarDifferential(calendarDifferential);
		
		p = map.get("virtual.pool");
		if (p != null && !p.isEmpty())
			object.setVirtualPool(p);
		
		p = map.get("virtual.hour");
		if (p != null && !p.isEmpty())
			object.setVirtualHour(Integer.parseInt(p));
		
		p = map.get("virtual.minute");
		if (p != null && !p.isEmpty())
			object.setVirtualMinute(Integer.parseInt(p));
		
		List<ScheduleMonthlyCalendarRs> calendarVirtual = new ArrayList<ScheduleMonthlyCalendarRs>();
		for(int r = 1; r <= 35; r++) {
			p = map.get("calendar.vd" + r);
			if (p != null && !p.isEmpty()) {
				ScheduleMonthlyCalendarRs cal = new ScheduleMonthlyCalendarRs();
				cal.fromNumRepresentation(r);
				calendarVirtual.add(cal);
			}
				
		}
		if (calendarVirtual.size()>0)
			object.setCalendarVirtual(calendarVirtual);
		
		return object;
	}

	
	// ############################   	GETTERS Y SETTERS 	######################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getFullPool() {
		return fullPool;
	}


	public void setFullPool(String fullPool) {
		this.fullPool = fullPool;
	}


	public Integer getFullHour() {
		return fullHour;
	}


	public void setFullHour(Integer fullHour) {
		this.fullHour = fullHour;
	}


	public Integer getFullMinute() {
		return fullMinute;
	}


	public void setFullMinute(Integer fullMinute) {
		this.fullMinute = fullMinute;
	}

	@XmlElementWrapper(name="calendarsFull")
	@XmlElementRef()
	public List<ScheduleMonthlyCalendarRs> getCalendarFull() {
		return calendarFull;
	}


	public void setCalendarFull(List<ScheduleMonthlyCalendarRs> calendarFull) {
		this.calendarFull = calendarFull;
	}


	public String getIncrementalPool() {
		return incrementalPool;
	}


	public void setIncrementalPool(String incrementalPool) {
		this.incrementalPool = incrementalPool;
	}


	public Integer getIncrementalHour() {
		return incrementalHour;
	}


	public void setIncrementalHour(Integer incrementalHour) {
		this.incrementalHour = incrementalHour;
	}


	public Integer getIncrementalMinute() {
		return incrementalMinute;
	}


	public void setIncrementalMinute(Integer incrementalMinute) {
		this.incrementalMinute = incrementalMinute;
	}


	@XmlElementWrapper(name="calendarsIncremental")
	@XmlElementRef()
	public List<ScheduleMonthlyCalendarRs> getCalendarIncremental() {
		return calendarIncremental;
	}


	public void setCalendarIncremental(
			List<ScheduleMonthlyCalendarRs> calendarIncremental) {
		this.calendarIncremental = calendarIncremental;
	}


	public String getDifferentialPool() {
		return differentialPool;
	}


	public void setDifferentialPool(String differentialPool) {
		this.differentialPool = differentialPool;
	}


	public Integer getDifferentialHour() {
		return differentialHour;
	}


	public void setDifferentialHour(Integer differentialHour) {
		this.differentialHour = differentialHour;
	}


	public Integer getDifferentialMinute() {
		return differentialMinute;
	}


	public void setDifferentialMinute(Integer differentialMinute) {
		this.differentialMinute = differentialMinute;
	}

	
	@XmlElementWrapper(name="calendarsDifferential")
	@XmlElementRef()
	public List<ScheduleMonthlyCalendarRs> getCalendarDifferential() {
		return calendarDifferential;
	}


	public void setCalendarDifferential(
			List<ScheduleMonthlyCalendarRs> calendarDifferential) {
		this.calendarDifferential = calendarDifferential;
	}


	public String getVirtualPool() {
		return virtualPool;
	}


	public void setVirtualPool(String virtualPool) {
		this.virtualPool = virtualPool;
	}


	public Integer getVirtualHour() {
		return virtualHour;
	}


	public void setVirtualHour(Integer virtualHour) {
		this.virtualHour = virtualHour;
	}


	public Integer getVirtualMinute() {
		return virtualMinute;
	}


	public void setVirtualMinute(Integer virtualMinute) {
		this.virtualMinute = virtualMinute;
	}


	@XmlElementWrapper(name="calendarsVirtual")
	@XmlElementRef()
	public List<ScheduleMonthlyCalendarRs> getCalendarVirtual() {
		return calendarVirtual;
	}


	public void setCalendarVirtual(List<ScheduleMonthlyCalendarRs> calendarVirtual) {
		this.calendarVirtual = calendarVirtual;
	}
}
