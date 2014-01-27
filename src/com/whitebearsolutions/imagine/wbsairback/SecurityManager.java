package com.whitebearsolutions.imagine.wbsairback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.whitebearsolutions.directory.Authenticator;
import com.whitebearsolutions.imagine.wbsairback.configuration.PermissionsConfiguration;
import com.whitebearsolutions.security.PasswordHandler;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class SecurityManager {
	private SessionManager session;
	private UserManager _um;
	private List<String> _roles;
	private List<String> _addresses;
	
	@SuppressWarnings("unchecked")
	public SecurityManager(SessionManager session) throws Exception {
		this.session = session;
		this._um = new UserManager();
		
		if(this.session.hasObjectSession("roles")) {
			this._roles = (List<String>) this.session.getObjectSession("roles");
		} else {
			this._roles = new ArrayList<String>();
		}
		
		this._addresses = new ArrayList<String>();
		Configuration _c = getConfiguration();
		if(_c.getProperty("address.allowed") != null) {
			StringTokenizer _st = new StringTokenizer(_c.getProperty("address.allowed"), ",");
			while(_st.hasMoreTokens()) {
				this._addresses.add(_st.nextToken());
			}
		}
	}
	
	private Configuration getConfiguration() throws Exception {
		return this.session.getConfiguration();
	}
	
	public void validateAddress(String address) throws Exception {
		if(this._addresses.isEmpty()) {
			return;
		}
		
		if(!this._addresses.contains(address)) {
			throw new Exception("insuficient access rights by address");
		}
	}
	
	public void userLogin(String user, String password) throws Exception {
		if(user == null || password == null) {
			throw new Exception("invalid user credentials");
		}
		
		if(this.session.hasObjectSession("roles")) {
			this.session.removeObjectSession("roles");
		}
		
		Configuration _c = getConfiguration();
		
		if("root".equals(user) || "admin".equals(user)) {
			if(!PasswordHandler.verifyPassword(_c.getProperty("system.password"), password)) {
				throw new Exception("invalid user credentials");
    		}
			
			this.session.loadObjectSession("roles", new ArrayList<String>(Arrays.asList(new String[] { "Administrator" })));
			setLogin("root");
			try {
				String _output = Command.systemCommand("aptitude show airback-central-webadministration | grep Versi | cut -d \" \" -f 2");
				if(_output != null && !_output.isEmpty()) { 
					_c.setProperty("wbsairback.version", _output);
					_c.store();
				}
			} catch(Exception _ex) {}
		} else if(_c.getProperty("ldap.host") != null) {
			Authenticator _a = new Authenticator(_c);
			setUser(_a.authenticate(user, password));
			if(this._um.userExists(user)) {
				this.session.loadObjectSession("roles", this._um.getUserRoles(user));
			} else {
				throw new Exception("user is not WBSAirback user");
			}
			setLogin(user);
		} else if(this._um.userExists(user)) {
			this._um.authenticate(user, password);
			this.session.loadObjectSession("roles", this._um.getUserRoles(user));
			setLogin(user);
		} else {
			throw new Exception("invalid user credentials");
		}
	}
	
	public String getLogin() throws Exception {
		if(this.session.hasObjectSession("logged")) {
			return String.valueOf(this.session.getObjectSession("logged"));
		}
		return null;
	}
	
	public void setLogin(String user) throws Exception {
		this.session.loadObjectSession("logged", user);
	}
	
	public void setLogout() throws Exception {
		if(this.session.hasObjectSession("roles")) {
			this.session.removeObjectSession("roles");
		}
		if(this.session.hasObjectSession("logged")) {
			this.session.removeObjectSession("logged");
		}
		if(this.session.hasObjectSession("securityManager")) {
			this.session.removeObjectSession("securityManager");
		}
		this.session.reloadConfiguration();
		this.session.reloadNetworkManager();
	}
	
	public boolean isLogged() {
		if(this.session != null && this.session.hasObjectSession("logged")) {
			return true;
		}
		return false;
	}
	
	private void setUser(String userDN) throws Exception {
		this.session.loadObjectSession("user", userDN);
	}
	
	public String getUser() throws Exception {
		if(this.session.hasObjectSession("user")) {
			return (String) this.session.getObjectSession("user");
		}
		return null;
	}
	
	public boolean checkCategory(String category) throws Exception {
		if (isAdministrator())
			return true;
		else if (isRole(RoleManager.roleGlobalOperator))
			return true;
		else if (isRole(RoleManager.roleOperator) || isRole(RoleManager.roleUser)) {
			if (category != null && !category.isEmpty()) {
				String [] categories = category.split(",");
				if (hasUserAnyCategory(Arrays.asList(categories)))
					return true;
			}
		}
		return false;
	}
	
	public boolean checkCategory(List<String> categories) throws Exception {
		if (isAdministrator())
			return true;
		else if (isRole(RoleManager.roleGlobalOperator))
			return true;
		else if (isRole(RoleManager.roleOperator) || isRole(RoleManager.roleUser)) {
			if (categories != null && !categories.isEmpty()) {
				if (hasUserAnyCategory(categories))
					return true;
			}
		}
		return false;
	}
	
	public boolean hasUserCategory() throws Exception {
		if(this.session.hasObjectSession("logged")) {
			List<String> categories = this._um.getUserCategories(String.valueOf(this.session.getObjectSession("logged")));
			if (categories != null && !categories.isEmpty())
				return true;
		}
		return false;
	}
	
	public boolean hasUserCategory(String category) throws Exception {
		if(this.session.hasObjectSession("logged")) {
			List<String> categories = this._um.getUserCategories(String.valueOf(this.session.getObjectSession("logged")));
			if (categories != null && !categories.isEmpty() && categories.contains(category))
				return true;
		}
		return false;
	}
	
	public boolean hasUserAnyCategory(List<String> cats) throws Exception {
		if(this.session.hasObjectSession("logged")) {
			List<String> categories = this._um.getUserCategories(String.valueOf(this.session.getObjectSession("logged")));
			if (categories != null && !categories.isEmpty()) {
				for (String cat : categories)
					if (cats.contains(cat))
						return true;
			}
		}
		return false;
	}
	
	public List<String> getUserCategories() throws Exception {
		if(this.session.hasObjectSession("logged")) {
			List<String> categories = this._um.getUserCategories(String.valueOf(this.session.getObjectSession("logged")));
			return categories;
		}
		return null;
	}
	
	public boolean hasUserClient() throws Exception {
		if(this.session.hasObjectSession("logged")) {
			Map<String, String> _attributes = this._um.getUserAttributes(String.valueOf(this.session.getObjectSession("logged")));
			return _attributes.containsKey("client");
		}
		return false;
	}
	
	public String getUserClientName() throws Exception {
		if(this.session.hasObjectSession("logged")) {
			Map<String, String> _attributes = this._um.getUserAttributes(String.valueOf(this.session.getObjectSession("logged")));
			if(_attributes.containsKey("client")) {
				return _attributes.get("client");
			}
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public boolean isAdministrator() throws Exception {
		if(this.session.hasObjectSession("logged")) {
			if("root".equals(this.session.getObjectSession("logged")) || "admin".equals(this.session.getObjectSession("logged"))) {
				return true;
			}
		}
		if(this.session.hasObjectSession("roles")) {
			this._roles = (List<String>) this.session.getObjectSession("roles");
		}
		return this._roles.contains("Administrator");
	}
	
	@SuppressWarnings("unchecked")
	public boolean isRole(String role) throws Exception {
		if(!this.session.hasObjectSession("logged")) {
			return false;
		}
		if(this.session.hasObjectSession("roles")) {
			this._roles = (List<String>) this.session.getObjectSession("roles");
		}
		return this._roles.contains(role);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getUserRoles() throws Exception {
		if(!this.session.hasObjectSession("logged")) {
			return null;
		}
		if(this.session.hasObjectSession("roles")) {
			return (List<String>) this.session.getObjectSession("roles");
		}
		return null;
	}
	
	public boolean checkAddress(String address) throws Exception {
		File _f = new File("/etc/webadministration/security.xml");
		if(!_f.exists()) {
			return true;
		}
		Configuration _c = new Configuration(_f);
		if(_c.getProperty("addr.allowed") == null) {
			return true;
		}
		
		StringTokenizer _st = new StringTokenizer(_c.getProperty("addr.allowed"), ",");
		while(_st.hasMoreTokens()) {
			if(_st.nextToken().trim().equalsIgnoreCase(address)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isUserAllowedTo(String path, Integer type, PermissionsConfiguration permissions) {
		if (path == null || path.isEmpty())
			return true;
		try {
			if (path.contains("Login") || path.contains("Wizard"))
				return true;
			if (isAdministrator())
				return true;
		} catch (Exception ex) {
			return true;
		}
		
		if (permissions.isAllowed(this._roles, path, type))
			return true;
		return false;
	}
	
	public boolean isBackupFromCommunity(String path, PermissionsConfiguration permissions) {
		if (path == null || path.isEmpty())
			return false;
		try {
			if (path.contains("Login") || path.contains("Wizard"))
				return false;
		} catch (Exception ex) {
			return false;
		}
		
		return permissions.isBackup(path);
	}
}
