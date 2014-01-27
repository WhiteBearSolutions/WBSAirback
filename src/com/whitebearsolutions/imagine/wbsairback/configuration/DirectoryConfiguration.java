package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class DirectoryConfiguration {
	public static final int DIRECTORY_NONE = 1;
	public static final int DIRECTORY_LDAPv3 = 2;
	public static final int DIRECTORY_ACTIVE_DIRECTORY = 3;
	public static final int CIFS_MEMBER_NONE = 1;
	public static final int CIFS_MEMBER_NT = 2;
	public static final int CIFS_MEMBER_ACTIVE_DIRECTORY = 3;
	
	public static Map<String, String> getDirectory() throws Exception {
		Map<String, String> _directory = new HashMap<String, String>();
		Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		
		if(_c.getProperty("ldap.host") != null) {
			_directory.put("host", _c.getProperty("ldap.host"));
		} else {
			_directory.put("host", "");
		}
		if(_c.getProperty("ldap.port") != null) {
			_directory.put("port", _c.getProperty("ldap.port"));
		} else {
			_directory.put("port", "");
		}
		if(_c.getProperty("ldap.basedn") != null) {
			_directory.put("basedn", _c.getProperty("ldap.basedn"));
		} else if(_c.getProperty("cifs.domain") != null) {
			_directory.put("basedn", _c.getProperty("cifs.domain"));
		} else {
			_directory.put("basedn", "");
		}
		if(_c.getProperty("ldap.manager") != null) {
			_directory.put("manager", _c.getProperty("ldap.manager"));
		} else {
			_directory.put("manager", "");
		}
		if(_c.getProperty("ldap.password") != null) {
			_directory.put("password", _c.getProperty("ldap.password"));
		} else {
			_directory.put("password", "");
		}
		if(_c.getProperty("domain.admin") != null) {
			_directory.put("domain.admin", _c.getProperty("domain.admin"));
		} else {
			_directory.put("domain.admin", "");
		}
		if(_c.getProperty("domain.admin.pass") != null) {
			_directory.put("domain.admin.pass", _c.getProperty("domain.admin.pass"));
		} else {
			_directory.put("domain.admin.pass", "");
		}
		
		if(_c.getProperty("ldap.auth.userID") != null && "uid".equals(_c.getProperty("ldap.auth.userID"))) {
			_directory.put("type", "ldapv3");
		} else if(_c.getProperty("ldap.auth.userID") != null && "sAMAccountName".equals(_c.getProperty("ldap.auth.userID"))) {
			_directory.put("type", "msad");
			if(!_directory.get("basedn").isEmpty()) {
				_directory.put("basedn", getDomain(_directory.get("basedn")));
			}
		} else {
			_directory.put("type", "");
		}
		return _directory;
	}
	
	public static void setDirectory(String[] address, int port, String basedn, String user_dn, String user_dn_password, int type, String domain_admin, String domain_admin_pass) throws Exception {
		String _domain = "DOMAIN";
		if(basedn == null || basedn.isEmpty()) {
			throw new Exception("invalid basedn or domain name");
		} else if(basedn.matches("^[0-9a-zA-Z]{1,8}=[-0-9a-zA-Z]+(,[0-9a-zA-Z]{1,8}=[-0-9a-zA-Z]+)+$")) {
			_domain = getDomain(basedn);
		} else if(basedn.matches("^([a-zA-Z0-9.-]+\\.)+[a-zA-Z]{2,4}$")) {
			_domain = basedn;
			basedn = getBaseDN(basedn);
		} else if(type == DIRECTORY_NONE) {
			_domain = basedn;
		} else {
			throw new Exception("invalid basedn or domain name");
		}
		if(type != DIRECTORY_NONE && address == null) {
			throw new Exception("invalid directory server address");
		}
		
		Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		File _f = new File("/etc/nsswitch.conf");
		FileLock _fl = new FileLock(_f);
		FileOutputStream _fos = null;
		StringBuilder _sb = new StringBuilder();
		
		switch(type) {
			case DIRECTORY_LDAPv3: {
					_sb.append("passwd:\tfiles ldap\n");
					_sb.append("group:\tfiles ldap\n");
					_sb.append("shadow:\tfiles ldap\n\n");
					_sb.append("hosts:\tfiles dns\n");
					
					_c.setProperty("ldap.host", NetworkManager.addressToString(address));
					if(port > 0 && port < 65555) {
						_c.setProperty("ldap.port", String.valueOf(port));
					}
					_c.setProperty("ldap.basedn", basedn);
					_c.setProperty("ldap.auth.userID", "uid");
					if(user_dn != null && user_dn_password != null && !user_dn.isEmpty() && !user_dn_password.isEmpty()) {
						_c.setProperty("ldap.manager", user_dn);
						_c.setProperty("ldap.password", user_dn_password);
					}
				}
				break;
			case DIRECTORY_ACTIVE_DIRECTORY: {
					_sb.append("passwd:\tfiles winbind\n");
					_sb.append("group:\tfiles winbind\n");
					_sb.append("shadow:\tfiles winbind\n\n");
					_sb.append("hosts:\tfiles dns wins\n");
					
					_c.setProperty("ldap.host", NetworkManager.addressToString(address));
					if(port > 0 && port < 65555) {
						_c.setProperty("ldap.port", String.valueOf(port));
					} else {
						_c.removeProperty("ldap.port");
					}
					_c.setProperty("ldap.basedn", basedn);
					_c.setProperty("ldap.auth.userID", "sAMAccountName");
					if(user_dn != null && user_dn_password != null && !user_dn.isEmpty() && !user_dn_password.isEmpty()) {
						_c.setProperty("ldap.manager", user_dn);
						_c.setProperty("ldap.password", user_dn_password);
					}
					if (domain_admin != null && !domain_admin.isEmpty())
						_c.setProperty("domain.admin", domain_admin.trim());
					if (domain_admin_pass != null && !domain_admin_pass.isEmpty())
						_c.setProperty("domain.admin.pass", domain_admin_pass.trim());
				}
				break;
			default: {
					_sb.append("passwd:\tfiles\n");
					_sb.append("group:\tfiles\n");
					_sb.append("shadow:\tfiles\n\n");
					_sb.append("hosts:\tfiles dns\n");
					
					_c.removeProperty("ldap.host");
					_c.removeProperty("ldap.port");
					_c.removeProperty("ldap.basedn");
					_c.removeProperty("ldap.auth.userID");
					_c.removeProperty("ldap.manager");
					_c.removeProperty("ldap.password");
					_c.removeProperty("domain.admin");
					_c.removeProperty("domain.admin.pass");
					_c.setProperty("cifs.domain", _domain);
				}
				break;
		}
		_sb.append("networks:\tfiles\n\n");
		_sb.append("protocols:\tdb files\n");
		_sb.append("services:\tdb files\n");
		_sb.append("ethers:\tdb files\n");
		_sb.append("rpc:\tdb files\n\n");
		_sb.append("netgroup:\tnis\n");
		
		try {
			_fl.lock();
			_fos = new FileOutputStream(_f);
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}

		_c.store();
		
		_sb = new StringBuilder();
		_sb.append("auth\tsufficient\tpam_unix.so nullok_secure\n");
		switch(type) {
			case DIRECTORY_LDAPv3: {
					_sb.append("auth\tsufficient\tpam_ldap.so use_first_pass\n");
				}
				break;
			case DIRECTORY_ACTIVE_DIRECTORY: {
					_sb.append("auth\tsufficient\tpam_winbind.so use_first_pass\n");
				}
				break;
		}
		_sb.append("auth\trequired\tpam_deny.so\n");
		
		_f = new File("/etc/pam.d/common-auth");
		_fl = new FileLock(_f);
		try {
			_fl.lock();
			_fos = new FileOutputStream(_f);
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}

		_sb = new StringBuilder();
		_sb.append("account\tsufficient\tpam_unix.so\n");
		switch(type) {
			case DIRECTORY_LDAPv3: {
					_sb.append("account\tsufficient\tpam_ldap.so\n");
				}
				break;
			case DIRECTORY_ACTIVE_DIRECTORY: {
					_sb.append("account\tsufficient\tpam_winbind.so\n");
				}
				break;
		}
		_sb.append("account\trequired\tpam_permit.so\n");
		
		_f = new File("/etc/pam.d/common-account");
		_fl = new FileLock(_f);
		try {
			_fl.lock();
			_fos = new FileOutputStream(_f);
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}
		
		_sb = new StringBuilder();
		_sb.append("password\tsufficient\tpam_unix.so nullok_secure md5 shadow\n");
		switch(type) {
			case DIRECTORY_LDAPv3: {
					_sb.append("password\tsufficient\tpam_ldap.so use_first_pass\n");
				}
				break;
			case DIRECTORY_ACTIVE_DIRECTORY: {
					_sb.append("password\tsufficient\tpam_winbind.so use_first_pass\n");
				}
				break;
		}
		_sb.append("password\trequired\tpam_deny.so\n");
		
		_f = new File("/etc/pam.d/common-password");
		_fl = new FileLock(_f);
		try {
			_fl.lock();
			_fos = new FileOutputStream(_f);
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}
		
		_sb = new StringBuilder();
		_sb.append("session\trequired\tpam_limits.so\n");
		_sb.append("session\tsufficient\tpam_unix.so\n");
		switch(type) {
			case DIRECTORY_LDAPv3: {
					_sb.append("session\toptional\tpam_ldap.so use_first_pass\n");
				}
				break;
			case DIRECTORY_ACTIVE_DIRECTORY: {
					_sb.append("session\toptional\tpam_winbind.so use_first_pass\n");
				}
				break;
		}
		
		_f = new File("/etc/pam.d/common-session");
		_fl = new FileLock(_f);
		try {
			_fl.lock();
			_fos = new FileOutputStream(_f);
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}
		
		_sb = new StringBuilder();
		switch(type) {
			case DIRECTORY_LDAPv3: {
					_sb.append("base ");
					_sb.append(basedn);
					_sb.append("\n");
					_sb.append("uri ldap://");
					_sb.append(NetworkManager.addressToString(address));
					if(port > 0 && port < 65555) {
						_sb.append(":");
						_sb.append(port);
					}
					_sb.append("/\n");
					_sb.append("ldap_version 3\n");
					if(_c.hasProperty("ldap.manager")) {
						_sb.append("binddn ");
						_sb.append(_c.getProperty("ldap.manager"));
						_sb.append("\n");
						if(_c.hasProperty("ldap.password")) {
							_sb.append("bindpw ");
							_sb.append(_c.getProperty("ldap.password"));
							_sb.append("\n");
						}
					}
					_sb.append("bind_policy soft\n");
					_sb.append("pam_filter objectclass=posixAccount\n");
					_sb.append("pam_login_attribute uid\n");
					_sb.append("pam_member_attribute uniquemember\n");
					_sb.append("pam_password exop\n");
				}
				break;
			case DIRECTORY_ACTIVE_DIRECTORY: {
					_sb.append("base ");
					_sb.append(basedn);
					_sb.append("\n");
					_sb.append("uri ldap://");
					_sb.append(NetworkManager.addressToString(address));
					if(port > 0 && port < 65555) {
						_sb.append(":");
						_sb.append(port);
					}
					_sb.append("/\n");
					_sb.append("ldap_version 3\n");
					if(_c.hasProperty("ldap.manager")) {
						_sb.append("binddn ");
						_sb.append(_c.getProperty("ldap.manager"));
						_sb.append("\n");
						if(_c.hasProperty("ldap.password")) {
							_sb.append("bindpw ");
							_sb.append(_c.getProperty("ldap.password"));
							_sb.append("\n");
						}
					}
					_sb.append("bind_policy soft\n");
					_sb.append("nss_map_objectclass posixAccount User\n");
					_sb.append("nss_map_objectclass shadowAccount User\n");
					_sb.append("nss_map_objectclass posixGroup Group\n");
					_sb.append("nss_map_attribute uid sAMAccountName\n");
					_sb.append("nss_map_attribute uniqueMember member\n");
					//_sb.append("pam_filter objectclass=User\n");
					_sb.append("pam_login_attribute sAMAccountName\n");
					_sb.append("pam_member_attribute member\n");
					_sb.append("pam_password ad\n");
				}
				break;
		}
		
		_f = new File("/etc/pam_ldap.conf");
		_fl = new FileLock(_f);
		try {
			_fl.lock();
			_fos = new FileOutputStream(_f);
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}
		
		_f = new File("/etc/libnss-ldap.conf");
		_fl = new FileLock(_f);
		try {
			_fl.lock();
			_fos = new FileOutputStream(_f);
			_fos.write(_sb.toString().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}
	}
	
	public static void setCIFSDomainMember(int type, String localName, String[] serverAddress, String basedn, String user, String password) throws Exception {
		String _domain;
		if(basedn == null || basedn.isEmpty()) {
			throw new Exception("invalid basedn or domain name");
		} else if(basedn.matches("^[0-9a-zA-Z]{1,8}=[-0-9a-zA-Z]+(,[0-9a-zA-Z]{1,8}=[-0-9a-zA-Z]+)+$")) {
			_domain = getDomain(basedn);
		} else if(basedn.matches("^([a-zA-Z0-9.-]+\\.)+[a-zA-Z]{2,4}$")) {
			_domain = basedn;
			basedn = getBaseDN(basedn);
		} else if(type != CIFS_MEMBER_NONE) {
			throw new Exception("invalid basedn or domain name");
		} else {
			_domain = basedn;
			basedn = null;
		}
		if(type != CIFS_MEMBER_NONE && (serverAddress == null || serverAddress.length != 4)) {
			throw new Exception("invalid directory server address");
		}
		
		StringBuilder _sb = new StringBuilder();
		switch(type) {
			case CIFS_MEMBER_NONE: {
					File _f = new File(WBSAirbackConfiguration.getFileSamba());
					FileLock _fl = new FileLock(_f);
					FileOutputStream _fos = null;
					_sb = new StringBuilder();
					_sb.append("[global]\n");
					_sb.append("\tworkgroup = ");
					_sb.append(_domain);
					_sb.append("\n");
					_sb.append("\tnetbios name = ");
					_sb.append(localName);
					_sb.append("\n");
					_sb.append("\tserver string = WBSAirback server\n");
					_sb.append("\tsecurity = user\n");
					_sb.append("\tdns proxy = no\n");
					_sb.append("\tlog file = /var/log/samba/log.%m\n");
					_sb.append("\tmax log size = 1000\n");
					_sb.append("\tsyslog = 0\n");
					_sb.append("\tsocket options = TCP_NODELAY SO_RCVBUF=8192 SO_SNDBUF=8192\n");
					_sb.append("\tencrypt passwords = true\n");
					_sb.append("\tpassdb backend = tdbsam\n");
					_sb.append("\tobey pam restrictions = no\n");
					_sb.append("\tpam password change = no\n");
					_sb.append("\tload printers = no\n");
					_sb.append("\tdomain logons = no\n\n");
					_sb.append("include = /etc/samba/shares.conf\n");
					try {
						_fl.lock();
						_fos = new FileOutputStream(_f);
						_fos.write(_sb.toString().getBytes());
					} finally {
						try {
							_fos.close();
						} catch(Exception _ex) {}
						_fl.unlock();
					}
					
					ServiceManager.remove(ServiceManager.WINBIND);
					ServiceManager.restart(ServiceManager.SAMBA);
				}
				break;
			case CIFS_MEMBER_NT: {
					File _f = new File(WBSAirbackConfiguration.getFileSamba());
					FileLock _fl = new FileLock(_f);
					FileOutputStream _fos = null;
					_sb = new StringBuilder();
					_sb.append("[global]\n");
					_sb.append("\tworkgroup = ");
					_sb.append(_domain);
					_sb.append("\n");
					_sb.append("\tnetbios name = ");
					_sb.append(localName);
					_sb.append("\n");
					_sb.append("\tserver string = WBSAirback server\n");
					_sb.append("\tpreferred master = no\n");
					_sb.append("\tdns proxy = no\n");
					_sb.append("\tsecurity = domain\n");
					_sb.append("\tpassword server = ");
					_sb.append(NetworkManager.addressToString(serverAddress));
					_sb.append("\n");
					_sb.append("\tlog file = /var/log/samba/log.%m\n");
					_sb.append("\tmax log size = 1000\n");
					_sb.append("\tsyslog = 0\n");
					_sb.append("\tsocket options = TCP_NODELAY SO_RCVBUF=8192 SO_SNDBUF=8192\n");
					_sb.append("\tencrypt passwords = true\n");
					_sb.append("\tpassdb backend = tdbsam\n");
					_sb.append("\tobey pam restrictions = yes\n");
					_sb.append("\tpam password change = no\n");
					_sb.append("\tdomain logons = no\n\n");
					_sb.append("\tnt acl support = yes\n");
					_sb.append("\tmap acl inherit = yes\n");
					_sb.append("\tcase sensitive = yes\n");
					_sb.append("\tdelete readonly = yes\n"); 
					_sb.append("\tea support = yes\n");
					_sb.append("include = /etc/samba/shares.conf\n");
					try {
						_fl.lock();
						_fos = new FileOutputStream(_f);
						_fos.write(_sb.toString().getBytes());
					} finally {
						try {
							_fos.close();
						} catch(Exception _ex) {}
						_fl.unlock();
					}
					
					ServiceManager.remove(ServiceManager.WINBIND);
					ServiceManager.restart(ServiceManager.SAMBA);
					
					_sb = new StringBuilder();
					_sb.append("/usr/bin/net join -U ");
					_sb.append(user);
					_sb.append("%");
					_sb.append(password);
					Command.systemCommand(_sb.toString());
				}
				break;
			case CIFS_MEMBER_ACTIVE_DIRECTORY: {
					File _f = new File(WBSAirbackConfiguration.getFileConfiguration());
					if(_f.exists()) {
						boolean _exists = false;
						NetworkManager _nm = new NetworkManager(new Configuration(_f));
						List<String[]> _nameservers = _nm.getNameservers();
						for(String[] _address : _nameservers) {
							if(NetworkManager.compareAddress(_address, serverAddress))  {
								_exists = true;
							}
						}
						if(!_exists) {
							_nameservers.add(0, serverAddress);
							_nm.setNameservers(_nameservers);
							_nm.update();
						}
					}
				
					_f = new File(WBSAirbackConfiguration.getFileSamba());
					FileLock _fl = new FileLock(_f);
					FileOutputStream _fos = null;
					_sb = new StringBuilder();
					_sb.append("[global]\n");
					_sb.append("\tworkgroup = ");
					_sb.append(_domain.substring(0, _domain.indexOf(".")).toUpperCase());
					_sb.append("\n");
					_sb.append("\tnetbios name = ");
					_sb.append(localName);
					_sb.append("\n");
					_sb.append("\tserver string = WBSAirback server\n");
					_sb.append("\tpreferred master = no\n");
					_sb.append("\tdns proxy = no\n");
					_sb.append("\tsecurity = ADS\n");
					_sb.append("\trealm = ");
					_sb.append(_domain.toUpperCase());
					_sb.append("\n");
					_sb.append("\tpassword server = ");
					_sb.append(NetworkManager.addressToString(serverAddress));
					_sb.append("\n");
					_sb.append("\tlog file = /var/log/samba/log.%m\n");
					_sb.append("\tmax log size = 1000\n");
					_sb.append("\tsyslog = 0\n");
					_sb.append("\tsocket options = TCP_NODELAY SO_RCVBUF=8192 SO_SNDBUF=8192\n");
					_sb.append("\tencrypt passwords = true\n");
					_sb.append("\tpassdb backend = tdbsam\n");
					_sb.append("\tobey pam restrictions = yes\n");
					_sb.append("\tpam password change = no\n");
					_sb.append("\tkerberos method = system keytab\n");
					_sb.append("\tdomain logons = no\n");
					_sb.append("\tallow trusted domains = no\n");
					_sb.append("\tnt acl support = yes\n");
					_sb.append("\tmap acl inherit = yes\n");
					_sb.append("\tcase sensitive = yes\n");
					_sb.append("\tdelete readonly = yes\n"); 
					_sb.append("\tea support = yes\n");
					_sb.append("\tidmap backend = rid:");
					_sb.append(_domain.substring(0, _domain.indexOf(".")).toUpperCase());
					_sb.append("=1000-4000000000\n");
					/*_sb.append("\tidmap config ");
					_sb.append(_domain.substring(0, _domain.indexOf(".")).toUpperCase());
					_sb.append(" : backend = ad\n");*/
					_sb.append("\tidmap config ");
					_sb.append(_domain.substring(0, _domain.indexOf(".")).toUpperCase());
					_sb.append(" : readonly = yes\n");
					_sb.append("\tidmap config ");
					_sb.append(_domain.substring(0, _domain.indexOf(".")).toUpperCase());
					_sb.append(" : schema_mode = rfc2307\n");
					_sb.append("\tidmap config ");
					_sb.append(_domain.substring(0, _domain.indexOf(".")).toUpperCase());
					_sb.append(" : range = 1000-4000000000\n");
					_sb.append("\tidmap uid = 1000-4000000000\n");
					_sb.append("\tidmap gid = 1000-4000000000\n");
					_sb.append("\twinbind nss info = rfc2307\n");
					_sb.append("\twinbind enum users = yes\n");
					_sb.append("\twinbind enum groups = yes\n");
					_sb.append("\twinbind use default domain = yes\n");
					_sb.append("\twinbind offline logon = true\n");
					_sb.append("\twinbind cache time = 15\n");
					_sb.append("\twinbind nested groups = yes\n");
					_sb.append("\twinbind refresh tickets = true\n");
					_sb.append("\twinbind separator = +\n");
					_sb.append("\ttemplate homedir = /home/%u\n");
					_sb.append("\ttemplate shell = /bin/false\n\n");
					_sb.append("include = /etc/samba/shares.conf\n");
					try {
						_fl.lock();
						_fos = new FileOutputStream(_f);
						_fos.write(_sb.toString().getBytes());
					} finally {
						try {
							_fos.close();
						} catch(Exception _ex) {}
						_fl.unlock();
					}
					
					_f = new File("/etc/krb5.conf");
					_fl = new FileLock(_f);
					_sb = new StringBuilder();
					_sb.append("[logging]\n");
					_sb.append("\tdefault = FILE:/var/log/krb5libs.log\n");
					_sb.append("\tkdc = FILE:/var/log/krb5kdc.log\n");
					_sb.append("\tadmin_server = FILE:/var/log/kadmind.log\n\n");
					_sb.append("[libdefaults]\n");
					_sb.append("\tdefault_realm = ");
					_sb.append(_domain.toUpperCase());
					_sb.append("\n");
					_sb.append("\tdns_lookup_realm = false\n");
					_sb.append("\tdns_lookup_kdc = false\n");
					_sb.append("\tticket_lifetime = 24h\n");
					_sb.append("\tforwardable = yes\n\n");
					_sb.append("[realms]\n");
					_sb.append("\t");
					_sb.append(_domain.toUpperCase());
					_sb.append(" = {\n");
					_sb.append("\tkdc = ");
					_sb.append(NetworkManager.addressToString(serverAddress));
					_sb.append("\n");
					_sb.append("\tadmin_server = ");
					_sb.append(NetworkManager.addressToString(serverAddress));
					_sb.append("\n");
					_sb.append("\tdefault_domain = ");
					_sb.append(_domain);
					_sb.append("\n");
					_sb.append("\t}\n\n");
					_sb.append("[domain_realm]\n");
					_sb.append("\t.kerberos.server = ");
					_sb.append(_domain.toUpperCase());
					_sb.append("\n");
					_sb.append("\t.");
					_sb.append(_domain);
					_sb.append(" = ");
					_sb.append(_domain.toUpperCase());
					_sb.append("\n\n");
					_sb.append("[kdc]\n");
					_sb.append("\tprofile = /var/kerberos/krb5kdc/kdc.conf\n\n");
					_sb.append("[appdefaults]\n");
					_sb.append("\tpam = {\n");
					_sb.append("\t\tdebug = false\n");
					_sb.append("\t\tticket_lifetime = 36000\n");
					_sb.append("\t\trenew_lifetime = 36000\n");
					_sb.append("\t\tforwardable = true\n");
					_sb.append("\t\tkrb4_convert = false\n");
				    _sb.append("\t}\n");
				    try {
						_fl.lock();
						_fos = new FileOutputStream(_f);
						_fos.write(_sb.toString().getBytes());
					} finally {
						try {
							_fos.close();
						} catch(Exception _ex) {}
						_fl.unlock();
					}
					
					ServiceManager.restart(ServiceManager.SAMBA);
					
					String _name = getNetBIOSName(serverAddress);
					_sb = new StringBuilder();
					_sb.append("/usr/bin/net ads join");
					if(_name != null && !_name.isEmpty()) {
						_sb.append(" -S ");
						_sb.append(_name);						
					}
					_sb.append(" -U ");
					_sb.append(user);
					_sb.append("%");
					_sb.append(password);
					try {
						Command.systemCommand(_sb.toString());
					} catch(Exception _ex) {
						StringBuilder _error = new StringBuilder();
						_error.append("cannot login into ADS domain");
						if(_ex.getMessage() != null && !_ex.getMessage().isEmpty()) {
							_error.append(" - ");
							_error.append(_ex.getMessage());
						}
						throw new Exception(_error.toString());
					}
					
					ServiceManager.initialize(ServiceManager.WINBIND);
				}
				break;
		}
	}
	
	private static String getDomain(String DN) {
		if(DN == null)  {
			return "";
		}
		String domain = DN.toLowerCase();
		domain = domain.replaceAll(",dc=", ".");
        return domain.replaceAll("dc=", "");
	}
	
	private static String getBaseDN(String domain) {
		if(domain == null)  {
			return "";
		}
		String baseDN = new String(domain);
		if(baseDN.startsWith(".")) {
			baseDN = baseDN.substring(1, baseDN.length());
		}
		baseDN = "dc=".concat(baseDN);
		return baseDN.replaceAll("\\.", ",dc=");
	}
	
	private static String getNetBIOSName(String[] address) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("nmblookup -A ");
		_sb.append(NetworkManager.addressToString(address));
		_sb.append(" | grep \"<00>\" | grep -v \"<GROUP>\"");
		try {
			String _value = Command.systemCommand(_sb.toString());
			if(_value == null || _value.isEmpty()) {
				return "";
			}
			_value = _value.trim();
			return _value.substring(0, _value.indexOf(" "));
		} catch(Exception _ex) {
			return "";
		}
	}
}