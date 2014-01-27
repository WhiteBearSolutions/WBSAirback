package com.whitebearsolutions.imagine.wbsairback.service;

import java.io.File;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.util.Command;

public class LicenseManager {
	private static char[] VALID_CHARS;
	private static String _serial;
	private String UUID;
	private Map<String, String> control;
	
	static {
		VALID_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		getSystemSerial();
	}
	
	public LicenseManager() throws Exception {
		this.control = new HashMap<String, String>();
	    load();
	}
	
	public boolean checkProductUUID() throws Exception {
		if(!checkUUID()) {
			throw new Exception("invalid product uuid");
		}
        return false;
	}
	
	public static Date expirationToDate(String date) throws ParseException {
		DateFormat _df = new SimpleDateFormat("dd/MM/yyyy");
		return _df.parse(date);
	}
	
	public String getUnitUUID() {
		return this.UUID;
	}
	
	public static String getSystemSerial() {
		File _sudoers = new File("/etc/sudoers.d/license");
		if(!_sudoers.exists()) {
			try {
				StringBuilder _sb = new StringBuilder();
				_sb.append("ALL ALL = (ALL) NOPASSWD: /usr/sbin/dmidecode -s system-serial-number\n");
				FileSystem.writeFile(_sudoers, _sb.toString());
				Command.systemCommand("chmod 440 " + _sudoers.getAbsolutePath());
			} catch(Exception _ex) {}
		} 
		for(int i = 25; i > 0 && (_serial == null || _serial.isEmpty()); i--) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException _ex) {}
			try {
				if(_sudoers.exists()) {
					_serial = Command.systemCommand("sudo /usr/sbin/dmidecode -s system-serial-number");
				} else {
					_serial = Command.systemCommand("/usr/sbin/dmidecode -s system-serial-number");
				}
			} catch(Exception _ex) {}
		}
		return _serial;
	}
	
	
	public boolean isValidUnitUUID() {
		return checkUUID();
	}
	
	
	private boolean checkUUID() {
		if(this.UUID == null) {
			return false;
		}
		if(!this.control.containsKey("uuid")) {
			return false;
		}
		try {
			if(this.control.get("uuid").equals(getSystemControl(this.UUID))) {
				return true;
			}
		} catch(Exception _ex) {
		}
		return false;
	}
	
	private static String generateUnitUUID() {
		try {
			StringBuilder _sb = new StringBuilder();
			Random _r = new Random();
			for(int i = 20; i > 0; --i) {
				_sb.append(VALID_CHARS[_r.nextInt(VALID_CHARS.length)]);
			}
			return _sb.toString();
		}  catch(Exception _ex) {}
		return null;
	}
	
	private static String generateDigest(byte[] data) throws Exception {
		MessageDigest algorithm = MessageDigest.getInstance("MD5");
		algorithm.reset();
		algorithm.update(data);
		byte[] _md = algorithm.digest();
		StringBuilder _sb = new StringBuilder();
		for (int i = 0; i < _md.length; i++) {
			_sb.append(Integer.toHexString(0xFF & _md[i]));
		}
		return _sb.toString();
	}
	
	private static String getSystemControl(String value) {
		if(_serial == null) {
			return null;
		}
		if(value == null) {
			return null;
		}
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append(value);
			_sb.append("/");
			_sb.append(_serial);
			return generateDigest(_sb.toString().getBytes());
		} catch(Exception _ex) {}
		return null;
	}
	
	private void load() {
		this.UUID = generateUnitUUID();
		if(this.UUID != null) {
			this.control.put("uuid", getSystemControl(this.UUID));
		}
	}
}
