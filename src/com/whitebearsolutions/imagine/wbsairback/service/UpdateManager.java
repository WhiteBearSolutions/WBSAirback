package com.whitebearsolutions.imagine.wbsairback.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.util.Command;

public class UpdateManager {
	public static String checkSignature() throws Exception {
		saveSourcesConfiguration();
		String _out = "OK";
		try {
			Command.systemCommand("apt-get update 2> /tmp/error.update");
		} catch (Exception _ex) {}
		
		File _error_file = new File("/tmp/error.update");
		if(_error_file.exists()) {
			BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_error_file)));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.indexOf("Failed to fetch") != -1) {
						_out = "Can't contact WBSImagine repository, check your network";
					}
					if(_line.indexOf("GPG error") != -1) {
						_out = "Invalid repository signature";
					}
				}
			} finally {
				_br.close();
			}
			_error_file.delete();
		}
		return _out;
	}
	
	public static Map<String, List<String>> getUpgradeData() throws Exception {
		Map<String, List<String>> _packages = new HashMap<String, List<String>>();
		try {
			Command.systemCommand("rm /var/cache/apt-show-versions/*");
		} catch (Exception ex) {}
		String _data = Command.systemCommand("apt-show-versions -u");
		if(!_data.isEmpty()) {
			BufferedReader _br = new BufferedReader(new StringReader(_data));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.contains("upgradeable")) {
						String[] _tokens = _line.split(" ");
						if(_tokens.length != 6) {
							continue;
						}
						
						String _application = _tokens[0];
						if(_application.contains("/")) {
							_application = _application.substring(0, _application.indexOf("/"));
						}
						_packages.put(_application, Arrays.asList(new String[] { _tokens[3], _tokens[5] }));
					}
				}
			} finally {
				_br.close();
			}
		}
		return _packages;
	}
	
	private static void saveSourcesConfiguration() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("deb http://");
		_sb.append(WBSAirbackConfiguration.getAptSourceServer());
		_sb.append("/imagine/openairback ");
		_sb.append(WBSAirbackConfiguration.getAptSourceVersion());
		_sb.append(" main\n");
		FileSystem.writeFile(new File(WBSAirbackConfiguration.getFileAptSources()), _sb.toString());
	}
	
	public static void upgrade() throws Exception {
		Command.systemCommand("DEBIAN_FRONTEND=noninteractive /usr/bin/apt-get dist-upgrade --force-yes -y -o Dpkg::Options::=\"--force-confnew\"");
	}
}
