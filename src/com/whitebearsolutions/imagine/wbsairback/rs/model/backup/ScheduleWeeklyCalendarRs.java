package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "weeklyCalendar")
public class ScheduleWeeklyCalendarRs {

	private String day;
	private String level;
	private Integer hour;
	private Integer minute;
	
	public ScheduleWeeklyCalendarRs() {}
	
	public static List<Map<String, String>> fillValues(ScheduleWeeklyRs schedule) {
		DecimalFormat formater = new DecimalFormat("00");
		List<Map<String, String>> values = new ArrayList<Map<String, String>>();
		for (ScheduleWeeklyCalendarRs cal : schedule.getCalendar()) {
			Map<String, String> val = new HashMap<String, String>();
			if (cal.getLevel() != null)
				val.put("level", cal.getLevel());
			if (cal.getHour() != null)
				val.put("hour", formater.format(cal.getHour()));
			if (cal.getMinute() != null)
				val.put("min", formater.format(cal.getMinute()));
			if (cal.getDay() != null)
				val.put("day", cal.getDay());
				
			values.add(val);
		}
		return values;
	}
	
	@XmlElement(required=true)
	public String getDay() {
		return day;
	}
	
	public void setDay(String day) {
		if (day != null && !day.isEmpty()) {
			if (day.toLowerCase().equals("monday") || day.toLowerCase().equals("mon") || day.toLowerCase().contains("m")) {
				this.day = "mon";
			} else if (day.toLowerCase().equals("tuesday") || day.toLowerCase().equals("tue") || day.toLowerCase().equals("tu")) {
				this.day = "tue";
			} else if (day.toLowerCase().equals("wednesday") || day.toLowerCase().equals("wed") || day.toLowerCase().contains("w")) {
				this.day = "wed";
			} else if (day.toLowerCase().equals("thursday") || day.toLowerCase().equals("thu") || day.toLowerCase().equals("th")) {
				this.day = "thu";
			} else if (day.toLowerCase().equals("friday") || day.toLowerCase().equals("fri") || day.toLowerCase().contains("f")) {
				this.day = "fri";
			} else if (day.toLowerCase().equals("saturday") || day.toLowerCase().equals("sat") || day.toLowerCase().equals("sa")) {
				this.day = "sat";
			}  else if (day.toLowerCase().equals("sunday") || day.toLowerCase().equals("sun") || day.toLowerCase().equals("su")) {
				this.day = "sun";
			}
		}
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
