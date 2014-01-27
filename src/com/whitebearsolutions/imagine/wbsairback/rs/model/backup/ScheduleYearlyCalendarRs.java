package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.whitebearsolutions.imagine.wbsairback.backup.ScheduleManager;

@XmlRootElement(name = "yearlyCalendar")
public class ScheduleYearlyCalendarRs {

	private String month;
	private Integer day;
	private String level;
	private Integer hour;
	private Integer minute;
	
	public ScheduleYearlyCalendarRs() {}
	
	public static List<Map<String, String>> fillValues(ScheduleYearlyRs schedule) throws Exception {
		DecimalFormat formater = new DecimalFormat("00");
		List<Map<String, String>> values = new ArrayList<Map<String, String>>();
		for (ScheduleYearlyCalendarRs cal : schedule.getCalendar()) {
			Map<String, String> val = new HashMap<String, String>();
			if (cal.getLevel() != null)
				val.put("level", cal.getLevel());
			if (cal.getHour() != null)
				val.put("hour", formater.format(cal.getHour()));
			if (cal.getMinute() != null)
				val.put("min", formater.format(cal.getMinute()));
			if (cal.getDay() != null) {
				if (cal.getDay() > 0 && cal.getDay() < 32)
					val.put("day", formater.format(cal.getDay()));
				else
					throw new Exception("invalid day");
			}
			if (cal.getMonth() != null) {
				if (ScheduleManager._months.containsValue(cal.getMonth())) {
					Integer month = 1;
					for (Integer key : ScheduleManager._months.keySet()) {
						if (ScheduleManager._months.get(key).equals(cal.getMonth())) {
							month = key;
							break;
						}
					}
					val.put("month", String.valueOf(month));
					
				} else
					throw new Exception("invalid month");
			}
				
			values.add(val);
		}
		return values;
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

	@XmlElement(required=true)
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@XmlElement(required=true)
	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}
	
}
