package com.whitebearsolutions.imagine.wbsairback.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.util.StringFormat;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.util.Configuration;

public class ScheduleManager {
	public static Map<Integer, String> _months = new HashMap<Integer, String>();
	
	static {
		_months.put(0, "jan");
		_months.put(1, "feb");
		_months.put(2, "mar");
		_months.put(3, "apr");
		_months.put(4, "may");
		_months.put(5, "jun");
		_months.put(6, "jul");
		_months.put(7, "aug");
		_months.put(8, "sep");
		_months.put(9, "oct");
		_months.put(10, "nov");
		_months.put(11, "dec");
	}
	
	public ScheduleManager() throws Exception {
	}
	
	public static Map<String, String> getMonthlySchedule(String name) throws Exception {
		Map<String, String> _schedule = new HashMap<String,String>();
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("schedule does not exists");
		}
		try {
			Configuration _c = new Configuration(_f);
			if(!_c.checkProperty("schedule.type", "monthly")) {
				throw new Exception("schedule is not monthly");
			}
			_schedule.put("name", _c.getProperty("schedule.name"));
			if(_c.getProperty("schedule.full.hour") != null && _c.getProperty("schedule.full.minute") != null) {
				if(_c.getProperty("schedule.full.pool") != null) {
					_schedule.put("full.pool", _c.getProperty("schedule.full.pool"));
				}
				_schedule.put("full.hour", _c.getProperty("schedule.full.hour"));
				_schedule.put("full.minute", _c.getProperty("schedule.full.minute"));
				for(int r = 1; r <= 35; r++) {
					if(_c.getProperty("calendar.fd" + r) != null) {
						_schedule.put("calendar.fd" + r, _c.getProperty("calendar.fd" + r));
					}
				}
			}
			if(_c.getProperty("schedule.incremental.hour") != null && _c.getProperty("schedule.incremental.minute") != null) {
				if(_c.getProperty("schedule.incremental.pool") != null) {
					_schedule.put("incremental.pool", _c.getProperty("schedule.incremental.pool"));
				}
				_schedule.put("incremental.hour", _c.getProperty("schedule.incremental.hour"));
				_schedule.put("incremental.minute", _c.getProperty("schedule.incremental.minute"));
				for(int r = 1; r <= 35; r++) {
					if(_c.getProperty("calendar.id" + r) != null) {
						_schedule.put("calendar.id" + r, _c.getProperty("calendar.id" + r));
					}
				}
			}
			if(_c.getProperty("schedule.differential.hour") != null && _c.getProperty("schedule.differential.minute") != null) {
				if(_c.getProperty("schedule.differential.pool") != null) {
					_schedule.put("differential.pool", _c.getProperty("schedule.differential.pool"));
				}
				_schedule.put("differential.hour", _c.getProperty("schedule.differential.hour"));
				_schedule.put("differential.minute", _c.getProperty("schedule.differential.minute"));
				for(int r = 1; r <= 35; r++) {
					if(_c.getProperty("calendar.dd" + r) != null) {
						_schedule.put("calendar.dd" + r, _c.getProperty("calendar.dd" + r));
					}
				}
			}
			if(_c.getProperty("schedule.virtual.hour") != null && _c.getProperty("schedule.virtual.minute") != null) {
				if(_c.getProperty("schedule.virtual.pool") != null) {
					_schedule.put("virtual.pool", _c.getProperty("schedule.virtual.pool"));
				}
				_schedule.put("virtual.hour", _c.getProperty("schedule.virtual.hour"));
				_schedule.put("virtual.minute", _c.getProperty("schedule.virtual.minute"));
				for(int r = 1; r <= 35; r++) {
					if(_c.getProperty("calendar.vd" + r) != null) {
						_schedule.put("calendar.vd" + r, _c.getProperty("calendar.vd" + r));
					}
				}
			}
		} catch(Exception _ex) {
			throw new Exception("cannot load schedule parameters");
		};
		return _schedule;
	}
	
	public static List<String> getDailyScheduleNames() {
		List<String> schedules = new ArrayList<String>();
		
		File dir = new File(WBSAirbackConfiguration.getDirectorySchedules());
		String[] _files = dir.list();
		if(_files != null) {
			for(String scheduleFile : _files) {
				if(scheduleFile.contains(".xml")) {
					try {
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + scheduleFile));
						if(_c.checkProperty("schedule.type", "daily")) {
							schedules.add(_c.getProperty("schedule.name"));
						}
					} catch(Exception _ex) {}
				}
			}
		}
		
		Collections.sort(schedules, String.CASE_INSENSITIVE_ORDER);
		return schedules;
	}
	
	public static List<String> getWeeklyScheduleNames() {
		List<String> schedules = new ArrayList<String>();
		
		File dir = new File(WBSAirbackConfiguration.getDirectorySchedules());
		String[] _files = dir.list();
		if(_files != null) {
			for(String scheduleFile : _files) {
				if(scheduleFile.contains(".xml")) {
					try {
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + scheduleFile));
						if(_c.checkProperty("schedule.type", "weekly")) {
							schedules.add(_c.getProperty("schedule.name"));
						}
					} catch(Exception _ex) {}
				}
			}
		}
		
		Collections.sort(schedules, String.CASE_INSENSITIVE_ORDER);
		return schedules;
	}
	
	public static List<String> getYearlyScheduleNames() {
		List<String> schedules = new ArrayList<String>();
		
		File dir = new File(WBSAirbackConfiguration.getDirectorySchedules());
		String[] _files = dir.list();
		if(_files != null) {
			for(String scheduleFile : _files) {
				if(scheduleFile.contains(".xml")) {
					try {
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + scheduleFile));
						if(_c.checkProperty("schedule.type", "yearly")) {
							schedules.add(_c.getProperty("schedule.name"));
						}
					} catch(Exception _ex) {}
				}
			}
		}
		
		Collections.sort(schedules, String.CASE_INSENSITIVE_ORDER);
		return schedules;
	}
	
	public static List<String> getMonthlyScheduleNames() {
		List<String> schedules = new ArrayList<String>();
		
		File dir = new File(WBSAirbackConfiguration.getDirectorySchedules());
		String[] _files = dir.list();
		if(_files != null) {
			for(String scheduleFile : _files) {
				if(scheduleFile.contains(".xml")) {
					try {
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + scheduleFile));
						if(_c.checkProperty("schedule.type", "monthly")) {
							schedules.add(_c.getProperty("schedule.name"));
						}
					} catch(Exception _ex) {}
				}
			}
		}
		
		Collections.sort(schedules, String.CASE_INSENSITIVE_ORDER);
		return schedules;
	}
	
	public static List<Map<String, String>> getWeeklyScheduleDays(String name) throws Exception {
		List<Map<String, String>> schedules = new ArrayList<Map<String, String>>();
		
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("schedule does not exist");
		}
		
		Configuration _c = new Configuration(_f);
		if(!_c.checkProperty("schedule.type", "weekly")) {
			throw new Exception("schedule is not weekly");
		}
		
		for(int schedule_number = 1; _c.getProperty("schedule" + schedule_number + ".level") != null; schedule_number++) {
			Map<String, String> schedule = new HashMap<String, String>();
			if(_c.getProperty("schedule" + schedule_number + ".level") != null) {
				schedule.put("level", _c.getProperty("schedule" + schedule_number + ".level"));
			} else {
				schedule.put("level", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".pool") != null) {
				schedule.put("pool", _c.getProperty("schedule" + schedule_number + ".pool"));
			} else {
				schedule.put("pool", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".day") != null) {
				schedule.put("day", _c.getProperty("schedule" + schedule_number + ".day"));
			} else {
				schedule.put("day", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".hour") != null) {
				schedule.put("hour", _c.getProperty("schedule" + schedule_number + ".hour"));
			} else {
				schedule.put("hour", "00");
			}
			if(_c.getProperty("schedule" + schedule_number + ".min") != null) {
				schedule.put("min", _c.getProperty("schedule" + schedule_number + ".min"));
			} else {
				schedule.put("min", "00");
			}
			schedules.add(schedule);
		}
		return schedules;
	}
	
	public static List<Map<String, String>> getYearlySchedule(String name) throws Exception {
		List<Map<String, String>> schedules = new ArrayList<Map<String, String>>();
		
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("schedule [" + name + "] does not exist");
		}
		
		Configuration _c = new Configuration(_f);
		if(!_c.checkProperty("schedule.type", "yearly")) {
			throw new Exception("schedule [" + name + "] is not yearly type");
		}
		
		int schedule_number = 1;
		for(; _c.getProperty("schedule" + schedule_number + ".level") != null; schedule_number++) {
			Map<String, String> schedule = new HashMap<String, String>();
			schedule.put("number", String.valueOf(schedule_number));
			if(_c.getProperty("schedule" + schedule_number + ".level") != null) {
				schedule.put("level", _c.getProperty("schedule" + schedule_number + ".level"));
			} else {
				schedule.put("level", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".pool") != null) {
				schedule.put("pool", _c.getProperty("schedule" + schedule_number + ".pool"));
			} else {
				schedule.put("pool", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".month") != null) {
				schedule.put("month", _c.getProperty("schedule" + schedule_number + ".month"));
			} else {
				schedule.put("month", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".day") != null) {
				schedule.put("day", _c.getProperty("schedule" + schedule_number + ".day"));
			} else {
				schedule.put("day", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".hour") != null) {
				schedule.put("hour", _c.getProperty("schedule" + schedule_number + ".hour"));
			} else {
				schedule.put("hour", "00");
			}
			if(_c.getProperty("schedule" + schedule_number + ".min") != null) {
				schedule.put("minute", _c.getProperty("schedule" + schedule_number + ".min"));
			} else {
				schedule.put("minute", "00");
			}
			schedules.add(schedule);
		}
		return schedules;
	}
	
	public static String getTypeSchedule(String name) throws Exception {
		String type = null;
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("schedule [" + name + "] does not exist");
		}
		
		Configuration _c = new Configuration(_f);
		if(_c.checkProperty("schedule.type", "weekly")) {
			type = "weekly";
		} else if(_c.checkProperty("schedule.type", "monthly")) {
			type = "monthly";
		} else if(_c.checkProperty("schedule.type", "daily")) {
			type = "daily";
		} else if(_c.checkProperty("schedule.type", "yearly")) {
			type = "yearly";
		}
		return type;
	}
	
	public static List<Map<String, String>> getDailySchedule(String name) throws Exception {
		List<Map<String, String>> schedules = new ArrayList<Map<String, String>>();
		
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("schedule [" + name + "] does not exist");
		}
		
		Configuration _c = new Configuration(_f);
		if(!_c.checkProperty("schedule.type", "daily")) {
			throw new Exception("schedule [" + name + "] is not yearly type");
		}
		
		int schedule_number = 1;
		for(; _c.getProperty("schedule" + schedule_number + ".level") != null; schedule_number++) {
			Map<String, String> schedule = new HashMap<String, String>();
			schedule.put("number", String.valueOf(schedule_number));
			if(_c.getProperty("schedule" + schedule_number + ".level") != null) {
				schedule.put("level", _c.getProperty("schedule" + schedule_number + ".level"));
			} else {
				schedule.put("level", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".pool") != null) {
				schedule.put("pool", _c.getProperty("schedule" + schedule_number + ".pool"));
			} else {
				schedule.put("pool", "");
			}
			if(_c.getProperty("schedule" + schedule_number + ".hour") != null) {
				schedule.put("hour", _c.getProperty("schedule" + schedule_number + ".hour"));
			} else {
				schedule.put("hour", "00");
			}
			if(_c.getProperty("schedule" + schedule_number + ".min") != null) {
				schedule.put("minute", _c.getProperty("schedule" + schedule_number + ".min"));
			} else {
				schedule.put("minute", "00");
			}
			schedules.add(schedule);
		}
		return schedules;
	}
	
	public static List<String> getScheduleNames() {
		List<String> schedules = new ArrayList<String>();
		File dir = new File(WBSAirbackConfiguration.getDirectorySchedules());
		String[] _files = dir.list();
		if(_files != null) {
			for(String scheduleFile : _files) {
				if(scheduleFile.contains(".xml")) {
					try {
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + scheduleFile));
						schedules.add(_c.getProperty("schedule.name"));
					} catch(Exception _ex) {}
				}
			}
		}
		Collections.sort(schedules, String.CASE_INSENSITIVE_ORDER);
		return schedules;
	}
	
	public static void removeYearlyScheduleDay(String name, int index) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid schedule name");
		}
		
		name = name.replaceAll("\\b\\s+\\b", "");
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		
		if(!_f.exists()) {
			throw new Exception("schedule [" + name + "] does not exists");
		}
		
		Configuration _c = new Configuration(_f);
		if(!_c.checkProperty("schedule.type", "yearly")) {
			throw new Exception("schedule [" + name + "] is not yearly type");
		}
		
		int schedules = 1, offset = 1;
		for(;  _c.getProperty("schedule" + offset + ".level") != null; offset++) {
			if(offset != index) {
				schedules++;
			}
		}
		
		if(offset > schedules) {
			_c.removeProperty("schedule" + (offset - 1) + ".level");
			_c.removeProperty("schedule" + (offset - 1) + ".pool");
			_c.removeProperty("schedule" + (offset - 1) + ".month");
			_c.removeProperty("schedule" + (offset - 1) + ".day");
			_c.removeProperty("schedule" + (offset - 1) + ".hour");
			_c.removeProperty("schedule" + (offset - 1) + ".min");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("Schedule {\n");
		_sb.append("  Name = \"" + name + "\"\n");
		schedules = 1;
		for(; _c.getProperty("schedule" + schedules + ".level") != null; schedules++) {
			StringBuilder _sb2 = new StringBuilder();
			_sb2.append(_c.getProperty("schedule" + schedules + ".level"));
			_sb2.append(" ");
			if(_c.getProperty("schedule" + schedules + ".pool") != null) {
				_sb2.append(" Pool=");
				_sb2.append(_c.getProperty("schedule" + schedules + ".pool"));
				_sb2.append(" ");
			}
			_sb2.append(_c.getProperty("schedule" + schedules + ".month"));
			_sb2.append(" ");
			_sb2.append(_c.getProperty("schedule" + schedules + ".day"));
			_sb2.append(" at ");
			_sb2.append(_c.getProperty("schedule" + schedules + ".hour"));
			_sb2.append(":");
			_sb2.append(_c.getProperty("schedule" + schedules + ".min"));
			
			_sb.append("  Run = Level=" + _sb2.toString() +"\n");
		}
		_sb.append("}\n");
		
		FileOutputStream _fos = new FileOutputStream(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".conf");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		
		_c.store();
		BackupOperator.reload();
	}
	
	public static void removeSchedule(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid schedule name");
		}
		
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		List<String> _jobs = JobManager.getJobsForSchedule(name);
		if(!_jobs.isEmpty()) {
			throw new Exception("schedule still used by job [" + _jobs.get(0) + "]");
		}
		
		BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
	    
	    File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".conf");
		if(_f.exists()) {
			_f.delete();
		}
		_f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		BackupOperator.reload();
	}
	
	public static void setMonthlyScheduler(String name, Map<String, String> values, String poolFull, int fullh, int fullm, String poolIncremental, int increh, int increm, String poolDifferential, int difh, int difm, String poolVirtual, int vifh, int vifm) throws Exception {
		boolean newScheduler = false;
		if(name == null || !name.matches("[0-9a-zA-Z-._:]+")) {
			throw new Exception("invalid schedule name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()){
			newScheduler = true;
			BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
		}
		
		if(newScheduler && ScheduleManager.getMonthlyScheduleNames().contains(name)) {
			throw new Exception("already have a monthly schedule with the same name");
		}
		
		if(ScheduleManager.getWeeklyScheduleNames().contains(name)) {
			throw new Exception("already have a weekly schedule with the same name");
		}
		
		if(ScheduleManager.getYearlyScheduleNames().contains(name)) {
			throw new Exception("already have a yearly schedule with the same name");
		}
		
		try {
			List<String> scheduleValues = new ArrayList<String>();
			Configuration _c = new Configuration(_f, false);
			_c.setProperty("schedule.name", name);
			_c.setProperty("schedule.type", "monthly");
			if(poolFull != null && !poolFull.trim().isEmpty()) {
				_c.setProperty("schedule.full.pool", poolFull);
			}
			_c.setProperty("schedule.full.hour", StringFormat.getTwoCharTimeComponent(fullh));
			_c.setProperty("schedule.full.minute", StringFormat.getTwoCharTimeComponent(fullm));
			
			StringBuilder _conf = new StringBuilder();
			StringBuilder _sb = new StringBuilder();
			for(int r = 1; r <= 35; r++) {
				if(values.containsKey("fd" + r)) {
					if(_sb.length() > 0) {
						_sb.append(",");
					}
					_sb.append(values.get("fd" + r));
					_c.setProperty("calendar.fd" + r, "checked");
				} else {
					_c.removeProperty("calendar.fd" + r);
				}
			}
			
			if(_sb.length() > 0) {
				_conf.append("Level=Full ");
				if(poolFull != null && !poolFull.trim().isEmpty()) {
					_conf.append("Pool=");
					_conf.append(poolFull);
				}
				_conf.append(" ");
				_conf.append(_sb.toString());
				_conf.append(" at ");
				_conf.append(StringFormat.getTwoCharTimeComponent(fullh));
				_conf.append(":");
				_conf.append(StringFormat.getTwoCharTimeComponent(fullm));
				scheduleValues.add(_conf.toString());
			} else {
				_c.removeProperty("schedule.full.pool");
				_c.removeProperty("schedule.full.hour");
				_c.removeProperty("schedule.full.minute");
				for(int r = 1; r <= 35; r++) {
					_c.removeProperty("calendar.fd" + r);
				}
			}
			if(poolIncremental != null && !poolIncremental.trim().isEmpty()) {
				_c.setProperty("schedule.incremental.pool", poolIncremental);
			}
			_c.setProperty("schedule.incremental.hour", StringFormat.getTwoCharTimeComponent(increh));
			_c.setProperty("schedule.incremental.minute", StringFormat.getTwoCharTimeComponent(increm));
			
			_conf = new StringBuilder();
			_sb = new StringBuilder();
			for(int r = 1; r <= 35; r++) {
				if(values.containsKey("id" + r)) {
					if(_sb.length() > 0) {
						_sb.append(",");
					}
					_sb.append(values.get("id" + r));
					_c.setProperty("calendar.id" + r, "checked");
				} else {
					_c.removeProperty("calendar.id" + r);
				}
			}
			
			if(_sb.length() > 0) {
				_conf.append("Level=Incremental ");
				if(poolIncremental != null && !poolIncremental.trim().isEmpty()) {
					_conf.append("Pool=");
					_conf.append(poolIncremental);
				}
				_conf.append(" ");
				_conf.append(_sb.toString());
				_conf.append(" at ");
				_conf.append(StringFormat.getTwoCharTimeComponent(increh));
				_conf.append(":");
				_conf.append(StringFormat.getTwoCharTimeComponent(increm));
				scheduleValues.add(_conf.toString());
			} else {
				_c.removeProperty("schedule.incremental.pool");
				_c.removeProperty("schedule.incremental.hour");
				_c.removeProperty("schedule.incremental.minute");
				for(int r = 1; r <= 35; r++) {
					_c.removeProperty("calendar.id" + r);
				}
			}
			if(poolDifferential != null && !poolDifferential.trim().isEmpty()) {
				_c.setProperty("schedule.differential.pool", poolDifferential);
			}
			_c.setProperty("schedule.differential.hour", StringFormat.getTwoCharTimeComponent(difh));
			_c.setProperty("schedule.differential.minute", StringFormat.getTwoCharTimeComponent(difm));
			
			_conf = new StringBuilder();
			_sb = new StringBuilder();
			for(int r = 1; r <= 35; r++) {
				if(values.containsKey("dd" + r)) {
					if(_sb.length() > 0) {
						_sb.append(",");
					}
					_sb.append(values.get("dd" + r));
					_c.setProperty("calendar.dd" + r, "checked");
				} else {
					_c.removeProperty("calendar.dd" + r);
				}
			}

			if(_sb.length() > 0) {
				_conf.append("Level=Differential ");
				if(poolDifferential != null && !poolDifferential.trim().isEmpty()) {
					_conf.append("Pool=");
					_conf.append(poolDifferential);
				}
				_conf.append(" ");
				_conf.append(_sb.toString());
				_conf.append(" at ");
				_conf.append(StringFormat.getTwoCharTimeComponent(difh));
				_conf.append(":");
				_conf.append(StringFormat.getTwoCharTimeComponent(difm));
				scheduleValues.add(_conf.toString());
			} else {
				_c.removeProperty("schedule.differential.pool");
				_c.removeProperty("schedule.differential.hour");
				_c.removeProperty("schedule.differential.minute");
				for(int r = 1; r <= 35; r++) {
					_c.removeProperty("calendar.dd" + r);
				}
			}
			if(poolVirtual != null && !poolVirtual.trim().isEmpty()) {
				_c.setProperty("schedule.virtual.pool", poolVirtual);
			}
			_c.setProperty("schedule.virtual.hour", StringFormat.getTwoCharTimeComponent(vifh));
			_c.setProperty("schedule.virtual.minute", StringFormat.getTwoCharTimeComponent(vifm));
			
			_conf = new StringBuilder();
			_sb = new StringBuilder();
			for(int r = 1; r <= 35; r++) {
				if(values.containsKey("vd" + r)) {
					if(_sb.length() > 0) {
						_sb.append(",");
					}
					_sb.append(values.get("vd" + r));
					_c.setProperty("calendar.vd" + r, "checked");
				} else {
					_c.removeProperty("calendar.vd" + r);
				}
			}

			if(_sb.length() > 0) {
				_conf.append("Level=VirtualFull ");
				if(poolVirtual != null && !poolVirtual.trim().isEmpty()) {
					_conf.append("Pool=");
					_conf.append(poolVirtual);
				}
				_conf.append(" ");
				_conf.append(_sb.toString());
				_conf.append(" at ");
				_conf.append(StringFormat.getTwoCharTimeComponent(vifh));
				_conf.append(":");
				_conf.append(StringFormat.getTwoCharTimeComponent(vifm));
				scheduleValues.add(_conf.toString());
			} else {
				_c.removeProperty("schedule.virtual.pool");
				_c.removeProperty("schedule.virtual.hour");
				_c.removeProperty("schedule.virtual.minute");
				for(int r = 1; r <= 35; r++) {
					_c.removeProperty("calendar.vd" + r);
				}
			}
			
			_c.store();
			BaculaConfiguration.setBaculaParameter(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".conf", "Schedule", name, "Run", scheduleValues.toArray(new String[scheduleValues.size()]));
		} catch(Exception _ex) {
			if(newScheduler) {
				BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
				new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml").delete();
			}
			throw _ex;
		}
		BackupOperator.reload();
	}
	
	public static void setWeeklyScheduleDays(String name, List<Map<String, String>> _schedule_days) throws Exception {
		boolean newScheduler = false;
		List<String> _valid_days = new ArrayList<String>(Arrays.asList(new String[] { "mon", "tue", "wed", "thu", "fri", "sat", "sun" }));
		if(name == null || !name.matches("[0-9a-zA-Z-._:]+")) {
			throw new Exception("invalid schedule name");
		}
		if(_schedule_days == null) {
			throw new Exception("invalid schedule value");
		}
		
		name = name.replaceAll("\\b\\s+\\b", "");
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		
		if(!_f.exists()) {
			newScheduler = true;
			BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
		}
		
		if(newScheduler && ScheduleManager.getWeeklyScheduleNames().contains(name)) {
			throw new Exception("already have a weekly schedule with the same name");
		}
		
		if(newScheduler && ScheduleManager.getMonthlyScheduleNames().contains(name)) {
			throw new Exception("already have a monthly schedule with the same name");
		}
		
		if(newScheduler && ScheduleManager.getYearlyScheduleNames().contains(name)) {
			throw new Exception("already have a yearly schedule with the same name");
		}
		
		try {
			File _conf = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".conf");
			Configuration _c = new Configuration(_f, false);
			_c.setProperty("schedule.name", name);
			_c.setProperty("schedule.type", "weekly");
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("Schedule {\n");
			_sb.append("  Name = \"");
			_sb.append(name);
			_sb.append("\"\n");
			for(int i = 0; i < _schedule_days.size(); i++) {
				Map<String, String> _schedule =  _schedule_days.get(i);
				StringBuilder _sb2 = new StringBuilder();
				if(_schedule.get("level") == null || _schedule.get("level").isEmpty() ||
						_schedule.get("day") == null || _schedule.get("day").isEmpty()||
						_schedule.get("hour") == null || _schedule.get("hour").isEmpty() ||
						_schedule.get("min") == null || _schedule.get("min").isEmpty()) {
					continue;
				}
				if(!_valid_days.contains(_schedule.get("day"))) {
					continue;
				}
				
				_c.setProperty("schedule" + (i + 1) + ".level", _schedule.get("level"));
				_c.setProperty("schedule" + (i + 1) + ".day", _schedule.get("day"));
				_c.setProperty("schedule" + (i + 1) + ".hour", _schedule.get("hour"));
				_c.setProperty("schedule" + (i + 1) + ".min", _schedule.get("min"));
				
				_sb2.append(_schedule.get("level"));
				_sb2.append(" ");
				if(_schedule.containsKey("pool") && _schedule.get("pool") != null && !_schedule.get("pool").trim().isEmpty()) {
					_c.setProperty("schedule" + (i + 1) + ".pool", _schedule.get("pool"));
					_sb2.append("Pool=");
					_sb2.append(_schedule.get("pool"));
					_sb2.append(" ");
				}
				_sb2.append(_schedule.get("day"));
				_sb2.append(" at ");
				_sb2.append(_schedule.get("hour"));
				_sb2.append(":");
				_sb2.append(_schedule.get("min"));
				_sb.append("  Run = Level=");
				_sb.append(_sb2.toString());
				_sb.append("\n");
			}
			_sb.append("}\n");
			
			FileOutputStream _fos = new FileOutputStream(_conf);
			FileLock _fl = new FileLock(_conf);
			try {
				_fl.lock();
				_fos.write(_sb.toString().getBytes());
			} finally {
				_fos.close();
				_fl.unlock();
			}
			_fos.close();
			
			_c.store();
			
			BackupOperator.reload();
		} catch(Exception _ex) {
			if(newScheduler) {
				BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
				new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml").delete();
				new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".conf").delete();
			}
			throw _ex;
		}
	}
	
	public static void setDailyScheduler(String name, List<Map<String, String>> _schedule_hours) throws Exception {
		boolean newScheduler = false;
		if(name == null || !name.matches("[0-9a-zA-Z-._:]+")) {
			throw new Exception("invalid schedule name");
		}
		
		if(_schedule_hours == null) {
			throw new Exception("invalid schedule value");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()){
			newScheduler = true;
			BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
		}
		
		if(newScheduler && ScheduleManager.getYearlyScheduleNames().contains(name)) {
			throw new Exception("already have a yearly schedule with the same name");
		}
		
		if(ScheduleManager.getWeeklyScheduleNames().contains(name)) {
			throw new Exception("already have a weekly schedule with the same name");
		}
		
		if(ScheduleManager.getMonthlyScheduleNames().contains(name)) {
			throw new Exception("already have a monthly schedule with the same name");
		}
		
		try {
			File _conf = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".conf");
			Configuration _c = new Configuration(_f, false);
			_c.setProperty("schedule.name", name);
			_c.setProperty("schedule.type", "daily");
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("Schedule {\n");
			_sb.append("  Name = \"" + name + "\"\n");
			for(int i = 0; i < _schedule_hours.size(); i++) {
				Map<String, String> _schedule =  _schedule_hours.get(i);
				StringBuilder _sb2 = new StringBuilder();
				if(_schedule.get("level") == null || _schedule.get("level").isEmpty() ||
						_schedule.get("hour") == null || _schedule.get("hour").isEmpty() ||
						_schedule.get("min") == null || _schedule.get("min").isEmpty()) {
					continue;
				}
				
			
				_c.setProperty("schedule" + (i + 1) + ".level", _schedule.get("level"));
				_c.setProperty("schedule" + (i + 1) + ".hour", _schedule.get("hour"));
				_c.setProperty("schedule" + (i + 1) + ".min", _schedule.get("min"));
				
				_sb2.append(_schedule.get("level"));
				_sb2.append(" ");
				if(_schedule.containsKey("pool") && _schedule.get("pool") != null && !_schedule.get("pool").trim().isEmpty()) {
					_c.setProperty("schedule" + (i + 1) + ".pool", _schedule.get("pool"));
					_sb2.append("Pool=");
					_sb2.append(_schedule.get("pool"));
					_sb2.append(" ");
				}
				_sb2.append("daily at ");
				_sb2.append(_schedule.get("hour"));
				_sb2.append(":");
				_sb2.append(_schedule.get("min"));
				_sb.append("  Run = Level=");
				_sb.append(_sb2.toString());
				_sb.append("\n");
			}
			_sb.append("}\n");
			
			FileOutputStream _fos = new FileOutputStream(_conf);
			FileLock _fl = new FileLock(_conf);
			try {
				_fl.lock();
				_fos.write(_sb.toString().getBytes());
			} finally {
				_fos.close();
				_fl.unlock();
			}
			
			_c.store();
		} catch(Exception _ex) {
			if(newScheduler) {
				BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
				new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml").delete();
			}
			throw _ex;
		}
		BackupOperator.reload();
	}
	
	public static void setYearlyScheduler(String name, List<Map<String, String>> _schedule_days) throws Exception {
		boolean newScheduler = false;
		if(name == null || !name.matches("[0-9a-zA-Z-._:]+")) {
			throw new Exception("invalid schedule name");
		}
		
		if(_schedule_days == null) {
			throw new Exception("invalid schedule value");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml");
		if(!_f.exists()){
			newScheduler = true;
			BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
		}
		
		if(newScheduler && ScheduleManager.getYearlyScheduleNames().contains(name)) {
			throw new Exception("already have a yearly schedule with the same name");
		}
		
		if(ScheduleManager.getWeeklyScheduleNames().contains(name)) {
			throw new Exception("already have a weekly schedule with the same name");
		}
		
		if(ScheduleManager.getMonthlyScheduleNames().contains(name)) {
			throw new Exception("already have a monthly schedule with the same name");
		}
		
		try {
			File _conf = new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".conf");
			Configuration _c = new Configuration(_f, false);
			_c.setProperty("schedule.name", name);
			_c.setProperty("schedule.type", "yearly");
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("Schedule {\n");
			_sb.append("  Name = \"" + name + "\"\n");
			for(int i = 0; i < _schedule_days.size(); i++) {
				Map<String, String> _schedule =  _schedule_days.get(i);
				StringBuilder _sb2 = new StringBuilder();
				if(_schedule.get("level") == null || _schedule.get("level").isEmpty() ||
						_schedule.get("month") == null || _schedule.get("month").isEmpty()||
						_schedule.get("day") == null || _schedule.get("day").isEmpty()||
						_schedule.get("hour") == null || _schedule.get("hour").isEmpty() ||
						_schedule.get("min") == null || _schedule.get("min").isEmpty()) {
					continue;
				}
				
				int _month = 0;
				try {
					_month = Integer.parseInt(_schedule.get("month"));
				} catch(NumberFormatException _ex) {}
				
				_c.setProperty("schedule" + (i + 1) + ".level", _schedule.get("level"));
				_c.setProperty("schedule" + (i + 1) + ".month", _months.get(_month));
				_c.setProperty("schedule" + (i + 1) + ".day", _schedule.get("day"));
				_c.setProperty("schedule" + (i + 1) + ".hour", _schedule.get("hour"));
				_c.setProperty("schedule" + (i + 1) + ".min", _schedule.get("min"));
				
				_sb2.append(_schedule.get("level"));
				_sb2.append(" ");
				if(_schedule.containsKey("pool") && _schedule.get("pool") != null && !_schedule.get("pool").trim().isEmpty()) {
					_c.setProperty("schedule" + (i + 1) + ".pool", _schedule.get("pool"));
					_sb2.append("Pool=");
					_sb2.append(_schedule.get("pool"));
					_sb2.append(" ");
				}
				_sb2.append(_months.get(_month));
				_sb2.append(" ");
				_sb2.append(_schedule.get("day"));
				_sb2.append(" at ");
				_sb2.append(_schedule.get("hour"));
				_sb2.append(":");
				_sb2.append(_schedule.get("min"));
				_sb.append("  Run = Level=");
				_sb.append(_sb2.toString());
				_sb.append("\n");
			}
			_sb.append("}\n");
			
			FileOutputStream _fos = new FileOutputStream(_conf);
			FileLock _fl = new FileLock(_conf);
			try {
				_fl.lock();
				_fos.write(_sb.toString().getBytes());
			} finally {
				_fos.close();
				_fl.unlock();
			}
			
			_c.store();
		} catch(Exception _ex) {
			if(newScheduler) {
				BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "schedules", name);
				new File(WBSAirbackConfiguration.getDirectorySchedules() + "/" + name + ".xml").delete();
			}
			throw _ex;
		}
		BackupOperator.reload();
	}
	
	public static List<String> getSchedulesForPool(String poolName) {
		List<String> _schedules = new ArrayList<String>();
		if(poolName == null || poolName.isEmpty()) {
			return _schedules;
		}
		
		File _dir = new File(WBSAirbackConfiguration.getDirectorySchedules());
		File[] _list = _dir.listFiles();
		
		for(File _f :_list) {
			if(_f.getName().endsWith(".xml")) {
				try {
					Configuration _c = new Configuration(_f);
					if(_c.checkProperty("schedule.poolFull", poolName)) {
						_schedules.add(_f.getName().replaceAll(".xml[^ ]*", ""));
					} else if(_c.checkProperty("schedule.poolIncre", poolName)) {
						_schedules.add(_f.getName().replaceAll(".xml[^ ]*", ""));
					} else if(_c.checkProperty("schedule.poolDiff", poolName)) {
						_schedules.add(_f.getName().replaceAll(".xml[^ ]*", ""));
					} else {
						int schedule_number = 1;
						for(; _c.getProperty("schedule" + schedule_number + ".level") != null; schedule_number++) {
							if (_c.checkProperty("schedule"+schedule_number+".pool", poolName)) {
								_schedules.add(_f.getName().replaceAll(".xml[^ ]*", ""));
							}
						}
					}
				} catch(Exception _ex) {}
			}
		}
		return _schedules;
	}
}
