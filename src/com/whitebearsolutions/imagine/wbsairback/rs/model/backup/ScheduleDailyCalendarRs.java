package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dailyCalendar")
public class ScheduleDailyCalendarRs {

	private String pool;
	private String level;
	private Integer hour;
	private Integer minute;
	
	public ScheduleDailyCalendarRs() {}
	
	public static List<Map<String, String>> fillValues(ScheduleDailyRs schedule) {
		DecimalFormat formater = new DecimalFormat("00");
		List<Map<String, String>> values = new ArrayList<Map<String, String>>();
		for (ScheduleDailyCalendarRs cal : schedule.getCalendar()) {
			Map<String, String> val = new HashMap<String, String>();
			if (cal.getLevel() != null)
				val.put("level", cal.getLevel());
			if (cal.getHour() != null)
				val.put("hour", formater.format(cal.getHour()));
			if (cal.getMinute() != null)
				val.put("min", formater.format(cal.getMinute()));
			if (cal.getPool() != null)
				val.put("pool", cal.getPool());
				
			values.add(val);
		}
		return values;
	}
	
	@XmlElement(required=true)
	public String getPool() {
		return pool;
	}
	public void setPool(String pool) {
		this.pool = pool;
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

	@XmlElement(required=true)
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
	
}
