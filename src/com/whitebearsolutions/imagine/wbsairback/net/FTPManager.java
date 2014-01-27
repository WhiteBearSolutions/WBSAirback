package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.io.FileLock;

public class FTPManager {
	private List<Map<String, String>> shares;
	
	public FTPManager() throws Exception {
		initialize();
	}
	
	public void addShare(String volume, String group, boolean anonymous) {
		Map<String, String> share = new HashMap<String, String>();
		share.put("volume", volume);
		share.put("group", group);
		if(anonymous) {
			share.put("anonymous", "true");
		}
		this.shares.add(share);
	}
	
	public List<Map<String, String>> getShares() {
		return this.shares;
	}
	
	public void updateShare(String volume, String group, boolean anonymous) {
		removeShare(volume, group);
		Map<String, String> share = new HashMap<String, String>();
		share.put("volume", volume);
		share.put("group", group);
		if(anonymous) {
			share.put("anonymous", "true");
		}
		this.shares.add(share);
	}
	
	public void removeShare(String volume, String group) {
		List<Map<String, String>> _shares = new ArrayList<Map<String,String>>();
		for(Map<String, String> share : this.shares) {
			if((share.get("volume") != null && share.get("group") != null) && (!String.valueOf(share.get("volume")).equalsIgnoreCase(volume) ||
					 !String.valueOf(share.get("group")).equalsIgnoreCase(group))) {
				_shares.add(share);
			}
		}
		this.shares = _shares;
	}
	
	public void write() throws Exception {
		StringBuilder _sb = new StringBuilder();
		for(Map<String, String> share : this.shares) {
			if(!share.containsKey("volume") || !share.containsKey("group")) {
				continue;
			}
			try {
				boolean anonymous = false;
				if(share.get("anonymous") != null && share.get("anonymous").equalsIgnoreCase("true")) {
					anonymous = true;
				}
				_sb.append(getShareSyntax(share.get("volume"), share.get("group"), anonymous));
			} catch(Exception _ex) {}
		}
		
		File _f = new File(WBSAirbackConfiguration.getFileShareFtp());
		FileLock _fl = new FileLock(_f);
		
		FileOutputStream _fos = new FileOutputStream(_f);
		try {
			_fl.lock();
			_fos.write(_sb.toString().getBytes());
		} finally {
			_fl.unlock();
			_fos.close();
		}
		ServiceManager.restart(ServiceManager.FTP);
	}
	
	private void initialize() throws Exception {
		File _f = new File("/etc/proftpd/proftpd.conf");
		if(!isFormatted(_f)) {
			this.shares = new ArrayList<Map<String,String>>();
			StringBuilder _sb = new StringBuilder();
			_sb.append("#WBSAirback FTP autoconfiguration\n");
			_sb.append("ServerName\t\"WBSAirback FTP\"\n");
			_sb.append("ServerType\tstandalone\n");
			_sb.append("Port\t21\n");
			_sb.append("DefaultServer\ton\n");
			_sb.append("MultilineRFC2228\ton\n");
			_sb.append("RequireValidShell\toff\n");
			_sb.append("RootLogin\toff\n");
			_sb.append("ShowSymlinks\toff\n");
			_sb.append("TimeoutNoTransfer\t600\n");
			_sb.append("TimeoutStalled\t600\n");
			_sb.append("TimeoutIdle\t1200\n");
			_sb.append("MaxInstances\t30\n");
			_sb.append("DefaultRoot\t");
			_sb.append(WBSAirbackConfiguration.getDirectoryVolumeMount());
			_sb.append("\n");
			_sb.append("<Directory />\n");
			_sb.append("\t<Limit READ WRITE STOR>\n");
			_sb.append("\t\tDenyAll\n");
			_sb.append("\t</Limit>\n");
			_sb.append("</Directory>\n\n");
			_sb.append("Include\t");
			_sb.append(WBSAirbackConfiguration.getFileShareFtp());
			_sb.append("\n\n");
			
			FileLock _fl = new FileLock(_f);
			FileOutputStream _fos = new FileOutputStream(_f);
			try {
				_fl.lock();
				_fos.write(_sb.toString().getBytes());
			} finally {
				_fl.unlock();
				_fos.close();
			}
		} else {
			this.shares = loadShares();
		}
	}
	
	private boolean isFormatted(File _f) {
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				String _line = null;
				if((_line = _br.readLine()) != null) {
					_br.close();
					return _line.startsWith("#WBSAirback FTP");
				}
			} catch(Exception _ex) {}
		}
		return false;
	}
	
	private List<Map<String, String>> loadShares() {
		List<Map<String, String>> shares = new ArrayList<Map<String,String>>();
		File _f = new File(WBSAirbackConfiguration.getFileShareFtp());
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(!_line.trim().startsWith("<Directory ") && !_line.trim().startsWith("<Anonymous ")) {
							continue;
						}
						try {
							Map<String, String> share = new HashMap<String, String>();
							if(_line.trim().startsWith("<Anonymous ")) {
								share.put("anonymous", "true");
							} else {
								share.put("anonymous", "false");
							}
							_line = _line.trim().substring(_line.indexOf(" ") + 1, _line.length() - 1);
							_line = _line.replace(WBSAirbackConfiguration.getDirectoryVolumeMount() + "/", "");
							String[] volume = _line.split("/");
							share.put("volume", volume[1]);
							share.put("group", volume[0]);
							shares.add(share);
						} catch(Exception _ex) {}
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {}
		}
		return shares;
	}
	
	private String getShareSyntax(String volume, String group, boolean anonymous) throws Exception {
		String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
		StringBuilder _sb = new StringBuilder();
		if(anonymous) {
			_sb.append("<Anonymous ");
			_sb.append(_path);
			_sb.append(">\n");
			_sb.append("\tUser\tnobody\n");
			_sb.append("\tGroup\tnogroup\n");
			_sb.append("\tUserAlias\tanonymous nobody\n");
			_sb.append("\t<Limit READ>\n");
			_sb.append("\t\tAllowAll\n");
			_sb.append("\t</Limit>\n");
			_sb.append("\t<Limit WRITE>\n");
			_sb.append("\t\tDenyAll\n");
			_sb.append("\t</Limit>\n");
			_sb.append("</Anonymous>\n");
		} else {
			_sb.append("<Directory ");
			_sb.append(_path);
			_sb.append(">\n");
			_sb.append("\tUmask\t077 077\n");
			_sb.append("\tAllowOverwrite\ton\n");
			_sb.append("\t<Limit READ WRITE STOR>\n");
			_sb.append("\t\tAllowAll\n");
			_sb.append("\t</Limit>\n");
			_sb.append("</Directory>\n");
		}
		return _sb.toString();
	}
}