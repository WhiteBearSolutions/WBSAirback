package com.whitebearsolutions.imagine.wbsairback;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Configuration;

public class SessionManager {
	private HttpSession session;
	
	public SessionManager(HttpServletRequest request) {
		this.session = request.getSession();
	}
	
	public Object getObjectSession(String name) {
		return this.session.getAttribute(name);
	}
	
	public Configuration getConfiguration() throws Exception {
		if(hasObjectSession("config")) {
			return (Configuration) getObjectSession("config");
		} else {
			File _f = new File(WBSAirbackConfiguration.getFileConfiguration());
			Configuration _c = new Configuration(_f);
			loadObjectSession("config", _c);
			return _c;
		}
	}
	
	public boolean isConfigured() {
		File _f = new File(WBSAirbackConfiguration.getFileConfiguration());
		if(_f.exists()) {
			return true;
		}
		return false;
	}
	
	public NetworkManager getNetworkManager() throws Exception {
		if(hasObjectSession("networkManager")) {
			return (NetworkManager) getObjectSession("networkManager");
		} else {
			NetworkManager _manager = new NetworkManager(getConfiguration());
			loadObjectSession("networkManager", _manager);
			return _manager;
		}
	}
	
	public SecurityManager getSecurityManager() throws Exception {
		if(hasObjectSession("securityManager")) {
			return (SecurityManager) getObjectSession("securityManager");
		} else {
			SecurityManager _manager = new SecurityManager(this);
			loadObjectSession("securityManager", _manager);
			return _manager;
		}
	}
	
	public boolean hasObjectSession(String name) {
		return this.session.getAttribute(name) != null;
	}
	
	public void loadObjectSession(String name, Object object) {
		if(hasObjectSession(name)) {
			removeObjectSession(name);
		}
		this.session.setAttribute(name, object);
	}
	
	public void reloadConfiguration() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getFileConfiguration());
		loadObjectSession("config", new Configuration(_f));
	}
	
	public void reloadNetworkManager() throws Exception {
		NetworkManager _manager = new NetworkManager(getConfiguration());
		loadObjectSession("networkManager", _manager);
	}
	
	public void removeObjectSession(String name) {
		this.session.removeAttribute(name);
	}
}
