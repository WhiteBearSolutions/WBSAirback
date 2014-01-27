package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.util.Command;

public class NTPConfiguration extends WBSAirbackConfiguration {
	private String timezone;
	private boolean server;
	private ArrayList<String> servers;
	
	public NTPConfiguration() throws Exception {
		
		this.servers = new ArrayList<String>();
		this.server = false;
		
		ByteArrayOutputStream _baos = new ByteArrayOutputStream();
		try {
			FileInputStream _fis = new FileInputStream(FILE_CONFIGURATION_NTP_TIMEZONE);
			for(int i = _fis.read(); i != -1; i = _fis.read()) {
				_baos.write(i);
			}
		} catch(IOException _ex) {}
		this.timezone = new String(_baos.toByteArray());
		
		File _f = new File(FILE_CONFIGURATION_NTP);
		BufferedReader _reader = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
		StringTokenizer _st;
		String line;
		
		if(_f.exists()) {
			while((line = _reader.readLine()) != null) {
				if(line.startsWith("server")) {
					_st = new StringTokenizer(line);
					_st.nextToken();
					this.servers.add(_st.nextToken());
				} else if(line.startsWith("broadcast")) {
					this.server = true;
				}
			}
		}
		_reader.close();
	}
	
	public List<String> getServers() {
		return this.servers;
	}
	
	public String getTimeZone() {
		if(this.timezone == null) {
			return "Etc/UTC";
		}
		return this.timezone.trim();
	}
	
	public boolean isServerActive() {
		return this.server;
	}
	
	public static String[] getAvailableTimeZones() {
		String[] timezones = TimeZone.getAvailableIDs();
        Arrays.sort(timezones);
        return timezones;
	}
	
	public void setTimeZone(String tz) throws Exception {
		List<String> tzs = Arrays.asList(TimeZone.getAvailableIDs());
		if(!tzs.contains(tz)) {
			throw new Exception("invalid timezone");
		}
		
		if(!tz.endsWith("\n")) {
			tz += "\n";
		}		
		this.timezone = tz;
		
		FileOutputStream _fos = null;
		try {
			_fos = new FileOutputStream(FILE_CONFIGURATION_NTP_TIMEZONE);
			_fos.write(tz.getBytes());
		} catch(IOException _ex) {
		} finally {
			if(_fos != null) {
				try {
					_fos.close();
				} catch(IOException _ex) {}
			}
		}
	}
	
	public void setServer(String server1, String server2, String server3) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("driftfile /var/lib/ntp/ntp.drift\n\n");
		_sb.append("filegen loopstats file loopstats type day enable\n");
		_sb.append("filegen peerstats file peerstats type day enable\n");
		_sb.append("filegen clockstats file clockstats type day enable\n\n");
		if(server1 != null && server1.length() > 0) {
			_sb.append("server " + server1 + "\n");
		}
		if(server2 != null && server2.length() > 0) {
			_sb.append("server " + server2 + "\n");
		}
		if(server3 != null && server3.length() > 0) {
			_sb.append("server " + server3 + "\n\n");
		}
		_sb.append("restrict -4 default kod notrap nomodify nopeer noquery\n");
		_sb.append("restrict -6 default kod notrap nomodify nopeer noquery\n");
		
		FileOutputStream _fos = new FileOutputStream(FILE_CONFIGURATION_NTP);
		_fos.write(_sb.toString().getBytes());
		_fos.close();		
	}
	
	public void setDate(int year, int month, int day, int hour, int minute, String timeOfDay) throws Exception {
		Calendar _cal;
		if(getTimeZone().isEmpty()) {
			_cal = Calendar.getInstance();
		} else {
			_cal = Calendar.getInstance(TimeZone.getTimeZone(getTimeZone()));
		}
		month = month - 1;
		if(timeOfDay != null && "PM".equals(timeOfDay)) {
			if(hour < 12) {
				hour = hour + 12;
			}
		}
		_cal.set(Calendar.YEAR , year);
		_cal.set(Calendar.MONTH , month);
		_cal.set(Calendar.DAY_OF_MONTH , day);
		_cal.set(Calendar.HOUR_OF_DAY , hour);
		_cal.set(Calendar.MINUTE , minute);
		_cal.set(Calendar.SECOND , 0);
		_cal.set(Calendar.MILLISECOND , 0);
		
		StringBuilder _date = new StringBuilder();
		if(_cal.get(Calendar.MONTH) < 9) {
			_date.append("0");
		}
		_date.append(_cal.get(Calendar.MONTH) + 1);
		if(_cal.get(Calendar.DAY_OF_MONTH) < 10) {
			_date.append("0");
		}
		_date.append(_cal.get(Calendar.DAY_OF_MONTH));
		if(_cal.get(Calendar.HOUR_OF_DAY) < 10) {
			_date.append("0");
		}
		_date.append(_cal.get(Calendar.HOUR_OF_DAY));
		if(_cal.get(Calendar.MINUTE) < 10) {
			_date.append("0");
		}
		_date.append(_cal.get(Calendar.MINUTE));
		if(_cal.get(Calendar.YEAR) < 10) {
			_date.append("0");
		}
		_date.append(_cal.get(Calendar.YEAR));
		
		if(!getTimeZone().isEmpty()) {
			File _f = new File("/usr/share/zoneinfo/" + getTimeZone());
			if(_f.exists()) {
				Command.systemCommand("/bin/ln -sf " + _f.getAbsolutePath() + " /etc/localtime");
			} else {
				Command.systemCommand("/bin/ln -sf /usr/share/zoneinfo/Etc/UTC /etc/localtime");
			}
		} else {
			Command.systemCommand("/bin/ln -sf /usr/share/zoneinfo/Etc/UTC /etc/localtime");
		}
		
		Command.systemCommand("/bin/date " + _date.toString());		
		ServiceManager.restart(ServiceManager.NTP);		
		Command.systemCommand("/sbin/hwclock -w");
	}
	
	/**
     * Resync of system clock with ntpdate using the configured servers
     * @param _servers List<String> List of the configured servers
     * */
    public static void resyncClock(){
        try {
            NTPConfiguration _nc = new NTPConfiguration();
            List<String> _servers = _nc.getServers();
            ServiceManager.stop(ServiceManager.NTP);
            for (int x=0;x<3;x++){
            	try{
            		Command.systemCommand("ntpdate "+_servers.get(x));
                                break;
                }catch(Exception _ex){}
            }
            ServiceManager.start(ServiceManager.NTP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
