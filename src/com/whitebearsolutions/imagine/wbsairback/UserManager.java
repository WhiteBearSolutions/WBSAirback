package com.whitebearsolutions.imagine.wbsairback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.security.PasswordHandler;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.xml.db.XMLAttribute;
import com.whitebearsolutions.xml.db.XMLDB;
import com.whitebearsolutions.xml.db.XMLDBException;
import com.whitebearsolutions.xml.db.XMLObject;

public class UserManager {
	private XMLDB _db;
	private TreeMap<String, Map<String, String>> users;
	
	private final String pathUsers = WBSAirbackConfiguration.getDirectoryUsers()+"/"+WBSAirbackConfiguration.getFileUsers();
	
	public UserManager() throws Exception {
		File users_dir = new File(WBSAirbackConfiguration.getDirectoryUsers());
		if(!users_dir.exists()) {
			users_dir.mkdirs();
		}
		moveOldPath();
		
		this.users = new TreeMap<String, Map<String, String>>();
		try {
			this._db = new XMLDB(new File(pathUsers));
			for(XMLObject o: this._db.getObjects()) {
				Map<String, String> user = new HashMap<String, String>();
				String username = null;
				for(XMLAttribute a : o.getAttributes()) {
					if("uid".equals(a.getName())) {
						username = a.getValue();
					}
					user.put(a.getName(), a.getValue());
				}
				if(username != null) { 
					this.users.put(username, user);
				}
			}
		} catch(XMLDBException _ex) {
			throw new Exception(_ex.getMessage());
		}
	}
	
	public void addLocalUser(String username) throws Exception {
		if(username == null || !username.matches("[0-9a-zA-Z-.]+")) {
			throw new Exception("invalid user name");
		}
		addSystemUser(username);
		setUser(username, null, null, null);
	}
	
	public static void addSystemUser(String name) throws Exception {
		addSystemUser(name, null, null, null, true);
	}
	
	public static void addSystemUser(String name, String description, String directory, String shell, boolean cifs) throws Exception {
		if(name == null || name.isEmpty()) {
			return;
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/useradd --comment ");
		if(description == null || description.isEmpty()) {
			_sb.append("\"WBSAirback user\"");
		} else {
			description = description.trim();
			if(!description.startsWith("\"")) {
				description = "\"".concat(description);
			}
			if(!description.endsWith("\"")) {
				description = description.concat("\"");
			}
			_sb.append(description);
		}
		if(directory != null && !directory.isEmpty()) {
			_sb.append(" --home-dir ");
			_sb.append(directory);
			_sb.append(" -m");
		}
		_sb.append(" --shell ");
		if(shell == null || shell.isEmpty()) {
			_sb.append("/bin/bash");
		} else {
			_sb.append(shell);
		}
		_sb.append(" --no-user-group ");
		_sb.append(name);
		Command.systemCommand(_sb.toString());
		if(cifs) {
			_sb = new StringBuilder();
			_sb.append("/usr/bin/smbpasswd -a -n ");
			_sb.append(name);
			Command.systemCommand(_sb.toString());
		}
	}
	
	public boolean authenticate(String username, String password) throws Exception {
		if(!this.users.containsKey(username)) {
			throw new Exception("user does not exists");
		}
		
		Map<String, String> user = getUserAttributes(username);
		if(!PasswordHandler.verifyPassword(user.get("password"), password)) {
			throw new Exception("incorrect password");
		}
		return true;
	}
	
	public static void changeSystemUserPassword(String name, String password) throws Exception {
		boolean _cifs = true;
		if("wbsairback".equalsIgnoreCase(name)) {
			_cifs = false;
		}
		changeSystemUserPassword(name, password, _cifs);
	}
	
	public static void changeSystemUserPassword(String name, String password, boolean cifs) throws Exception {
		if(name == null || password == null || name.isEmpty() || password.isEmpty()) {
			return;
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"");
		_sb.append(name);
		_sb.append(":");
		_sb.append(password);
		_sb.append("\" | /usr/sbin/chpasswd");
		Command.systemCommand(_sb.toString());
		if(cifs) {
			_sb = new StringBuilder();
			_sb.append("/bin/echo \"");
			_sb.append(password);
			_sb.append("\n");
			_sb.append(password);
			_sb.append("\" | /usr/bin/smbpasswd -e -s ");
			_sb.append(name);
			Command.systemCommand(_sb.toString());
		}
	}
	
	public static boolean getRemotePasswordAccess() throws Exception {
		File _f = new File("/etc/ssh/sshd_config");
		if(_f.exists()) {
			BufferedReader _br = new BufferedReader(new FileReader(_f));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					_line = _line.trim();
					if(_line.startsWith("PasswordAuthentication ")) {
						if("yes".equalsIgnoreCase(_line.substring(_line.indexOf(" ") + 1))) {
							return true;
						}
					}
				}
			} finally {
				_br.close();
			}
		}
		return false;
	}
	
	public List<Map<String, String>> getUsers() {
		return new ArrayList<Map<String, String>>(this.users.values());
	}
	
	public Map<String, String> getUserAttributes(String username) {
		if(this.users.containsKey(username)) {
			return this.users.get(username);
		}
		return null;
	}
	
	public List<String> getUserCategories(String username) {
		List<String> categories = new ArrayList<String>(); 
		if(username == null) {
			return categories;
		}
		
		Map<String, String> user = getUserAttributes(username);
		if(user != null && user.containsKey("category")) {
			StringTokenizer _st = new StringTokenizer(user.get("category"), ",");
			while(_st.hasMoreTokens()) {
				categories.add(_st.nextToken());
			}
		}
		return categories;
	}
	
	public List<String> getUserRoles(String username) {
		List<String> roles = new ArrayList<String>(); 
		if(username == null) {
			return roles;
		}
		
		Map<String, String> user = getUserAttributes(username);
		if(user != null && user.containsKey("roles")) {
			StringTokenizer _st = new StringTokenizer(user.get("roles"), ",");
			while(_st.hasMoreTokens()) {
				roles.add(_st.nextToken());
			}
		}
		return roles;
	}
	
	private XMLObject getUserXMLObject(String username) {
		for(XMLObject user : this._db.getObjects()) {
			if(user.hasAttribute("uid") && username.equals(user.getAttribute("uid").getValue())) {
				return user;
			}
		}
		return null;
	}
	
	public void removeLocalUser(String username) throws Exception {
		this.users.remove(username);
		try {
			removeSystemUser(username, true);
		} catch(Exception _ex) {}
		store();
	}
	
	public static void removeSystemUser(String name, boolean cifs) throws Exception {
		StringBuilder _sb = new StringBuilder();
		if(cifs) {
			_sb.append("/usr/bin/smbpasswd -x ");
			_sb.append(name);
			Command.systemCommand(_sb.toString());
		}
		_sb = new StringBuilder();
		_sb.append("/usr/sbin/userdel -r -f ");
		_sb.append(name);
		try {
			Command.systemCommand(_sb.toString());
		} catch (Exception ex) {
			if (ex.getMessage().contains("warning") && ex.getMessage().contains("/var/mail"))
				return;
			else
				throw ex;
		}
	}
	
	public static void setRemotePasswordAccess(boolean passwordAuthentication) throws Exception {
		File _f = new File("/etc/ssh/sshd_config");
		StringBuilder _sb = new StringBuilder();
		_sb.append("# WBSAirback autoconfiguration\n");
		_sb.append("Port 22\n");
		_sb.append("Protocol 2\n");
		_sb.append("UsePrivilegeSeparation yes\n");
		_sb.append("KeyRegenerationInterval 3600\n");
		_sb.append("ServerKeyBits 768\n");
		_sb.append("SyslogFacility AUTH\n");
		_sb.append("LogLevel INFO\n");
		_sb.append("LoginGraceTime 120\n");
		_sb.append("PermitRootLogin yes\n");
		_sb.append("StrictModes yes\n");
		_sb.append("RSAAuthentication no\n");
		_sb.append("PubkeyAuthentication no\n");
		_sb.append("IgnoreRhosts yes\n");
		_sb.append("RhostsRSAAuthentication no\n");
		_sb.append("HostbasedAuthentication no\n");
		_sb.append("PermitEmptyPasswords no\n");
		_sb.append("ChallengeResponseAuthentication no\n");
		_sb.append("PasswordAuthentication yes\n");
		_sb.append("X11Forwarding no\n");
		_sb.append("X11DisplayOffset 10\n");
		_sb.append("PrintMotd no\n");
		_sb.append("PrintLastLog yes\n");
		_sb.append("TCPKeepAlive yes\n");
		_sb.append("AcceptEnv LANG LC_*\n");
		_sb.append("Subsystem sftp /usr/lib/openssh/sftp-server\n");
		_sb.append("UsePAM yes\n");
		FileSystem.writeFile(_f, _sb.toString());
		ServiceManager.restart(ServiceManager.SSH);
	}
	
	public void setUser(String username, List<String> roles, String description, String[] category) throws Exception {
		if(username == null || !username.matches("[0-9a-zA-Z-.]+")) {
			throw new Exception("invalid user name");
		}
		
		Map<String, String> user = this.users.get(username);
		if(user == null) {
			user = new HashMap<String, String>();
			user.put("uid", username);
		}
		
		if(description != null) {
			user.put("description", description);
		} else {
			user.put("description", "");
		}
		
		if(roles != null && !roles.isEmpty()) {
			List<String> _valid_roles = new RoleManager().getRoleNames();
			StringBuilder _sb = new StringBuilder();
			for(String role : roles) {
				if(!_valid_roles.contains(role)) {
					continue;
				}
				
				if(_sb.length() > 0) {
					_sb.append(",");
				}
				_sb.append(role);
			}
			user.put("roles", _sb.toString());
		} else {
			user.put("roles", "Usuario");
		}
		
		String cadCategories = "";
		if(category != null) {
			for (String cat : category) {
				if (cadCategories.isEmpty())
					cadCategories = cat;
				else
					cadCategories+=","+cat;
			}
		}
		
		user.put("category", cadCategories);
		
		this.users.put(username, user);
		store();
	}
	
	public void setLocalUserPassword(String username, String password) throws Exception {
		Map<String, String> user = this.users.get(username);
		if(user == null) {
			throw new Exception("user not found");
		}
		user.put("password", PasswordHandler.generateDigest(password, null, "MD5"));
		this.users.put(username, user);
		changeSystemUserPassword(username, password);
		store();
	}
	
	private void store() throws Exception {
		TreeMap<String, XMLObject> objects = new TreeMap<String, XMLObject>();
		for(Map<String, String> user : this.users.values()) {
			if(user.containsKey("uid")) {
				XMLObject xmluser = getUserXMLObject(user.get("uid"));
				if(xmluser == null) {
					xmluser = this._db.createXMLObject();
				}
				for(String attribute : user.keySet()) {
					XMLAttribute _a = new XMLAttribute(attribute);
					_a.setValue(user.get(attribute));
					xmluser.addAttribute(_a);
				}
				objects.put(user.get("uid"), xmluser);
			}
		}		
		this._db.setObjects(new ArrayList<XMLObject>(objects.values()));
		this._db.store();
	}
	
	public static boolean systemUserExists(String username) {
		if(username == null || username.isEmpty()) {
			return false;
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("getent passwd | grep \"");
		_sb.append(username);
		_sb.append("\"");
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null && _output.contains(username)) {
				return true;
			}
		} catch(Exception _ex) {}
		return false;
	}
	
	public static boolean systemGroupExists(String username) {
		if(username == null || username.isEmpty()) {
			return false;
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("getent group | grep \"");
		_sb.append(username);
		_sb.append("\"");
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null && _output.contains(username)) {
				return true;
			}
		} catch(Exception _ex) {}
		return false;
	}
	
	public boolean userExists(String username) {
		return this.users.containsKey(username);
	}
	
	private void moveOldPath() {
		try {
		if (!new File(pathUsers).exists()) {
			if (new File(WBSAirbackConfiguration.getFileUsers()).exists()) {
				Command.systemCommand("mv "+WBSAirbackConfiguration.getFileUsers()+" "+pathUsers);
			} else if (new File("/"+WBSAirbackConfiguration.getFileUsers()).exists()) {
				Command.systemCommand("mv /"+WBSAirbackConfiguration.getFileUsers()+" "+pathUsers);
			}
		}
		} catch (Exception ex) {}
	}
}