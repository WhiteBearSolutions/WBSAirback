package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "monthlyCalendar")
public class ScheduleMonthlyCalendarRs {
	private String day;
	private Integer numWeek;
	
	public ScheduleMonthlyCalendarRs() {}

	public static Map<String, String> fillValues(ScheduleMonthlyRs sc) throws Exception {
		Map<String, String> values = new HashMap<String, String>();
		
		List<ScheduleMonthlyCalendarRs> cals = sc.getCalendarFull();
		if (cals != null && !cals.isEmpty()) {
			for (ScheduleMonthlyCalendarRs cal : cals) {
				values.put("fd"+cal.getNumRepresentation(), cal.getNumWeekString()+" "+cal.getDay());
			}
		}
		
		cals = sc.getCalendarDifferential();
		if (cals != null && !cals.isEmpty()) {
			for (ScheduleMonthlyCalendarRs cal : cals) {
				values.put("dd"+cal.getNumRepresentation(), cal.getNumWeekString()+" "+cal.getDay());
			}
		}
		
		cals = sc.getCalendarIncremental();
		if (cals != null && !cals.isEmpty()) {
			for (ScheduleMonthlyCalendarRs cal : cals) {
				values.put("id"+cal.getNumRepresentation(), cal.getNumWeekString()+" "+cal.getDay());
			}
		}
		
		cals = sc.getCalendarVirtual();
		if (cals != null && !cals.isEmpty()) {
			for (ScheduleMonthlyCalendarRs cal : cals) {
				values.put("vd"+cal.getNumRepresentation(), cal.getNumWeekString()+" "+cal.getDay());
			}
		}
		
		return values;
	}
	
	public void fromNumRepresentation(Integer num) {
		if (num != null && num > 0 && num<=35) {
			this.numWeek = num / 7;
			Integer numDay = num % 7;
			if (numDay == 1)
				this.day="mon";
			if (numDay == 2)
				this.day="tue";
			if (numDay == 3)
				this.day="wed";
			if (numDay == 4)
				this.day="thu";
			if (numDay == 5)
				this.day="fri";
			if (numDay == 6)
				this.day="sat";
			if (numDay == 0)
				this.day="sun";
		}
	}
	
	public String getNumWeekString() throws Exception {
		if (this.numWeek == 1)
			return numWeek+"st";
		else if (this.numWeek == 2)
			return numWeek+"nd";
		else if (this.numWeek == 3)
			return numWeek+"rd";
		else
			return numWeek+"th";
	}
	
	public Integer getNumRepresentation() throws Exception {
		Integer num = 0;
		if (this.day != null && !this.day.equals("") && this.numWeek != null && this.numWeek>0) {
			num = (this.numWeek*7 -7) + getNumDay();
		}
		return num;
	}
	
	public Integer getNumDay() throws Exception{
		Integer num = 0;
		if (this.day != null && !this.day.equals("")) {
			if (this.day.toLowerCase().equals("monday") || this.day.toLowerCase().equals("mon")) {
				num = 1;
			} else if (this.day.toLowerCase().equals("tuesday") || this.day.toLowerCase().equals("tue")) {
				num = 2;
			} else if (this.day.toLowerCase().equals("wednesday") || this.day.toLowerCase().equals("wed")) {
				num = 3;
			} else if (this.day.toLowerCase().equals("thursday") || this.day.toLowerCase().equals("thu")) {
				num = 4;
			} else if (this.day.toLowerCase().equals("friday") || this.day.toLowerCase().equals("fri")) {
				num = 5;
			} else if (this.day.toLowerCase().equals("saturday") || this.day.toLowerCase().equals("sat")) {
				num = 6;
			}  else if (this.day.toLowerCase().equals("sunday") || this.day.toLowerCase().equals("sun")) {
				num = 7;
			}  else {
				throw new Exception ("invalid week day");
			}
		}
		return num;
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
	public Integer getNumWeek() {
		return numWeek;
	}

	public void setNumWeek(Integer numWeek) {
		this.numWeek = numWeek;
	}

	
}
