package com.whitebearsolutions.imagine.wbsairback.disk.fs;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.UserManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.util.Command;

public class XFSConfiguration {
	private final static Logger logger = LoggerFactory.getLogger(XFSConfiguration.class);
	
	public static void createFileSystem(String group, String name, String compression, boolean encryption) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/sbin/mkfs.xfs -f ");
		_sb.append(VolumeManager.getLogicalVolumeDevicePath(group, name));
		Command.systemCommand(_sb.toString());
		logger.info("Creado sistema XFS: {}", _sb.toString());
	}
	
	public static Map<String, String> getFileSystemParameters(String group, String name) throws Exception {
		Map<String, String> _parameters = new HashMap<String, String>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" df -klP | grep -w ");
		_sb.append(VolumeManager.getLogicalVolumeMountPath(group, name));
		_sb.append("$ | awk '{ print $2,$4; }'");
		String _output = Command.systemCommand(_sb.toString());
		if(_output != null && !_output.trim().isEmpty())  {
			try {
				double _size = Double.parseDouble(_output.substring(0, _output.indexOf(" "))) * 1024D;
				double _free = Double.parseDouble(_output.substring(_output.indexOf(" ") + 1)) * 1024D;
				_parameters.put("size-raw", getFormattedPlain(_size));
				_parameters.put("size", getFormattedSize(_size));
				_parameters.put("used", getFormattedPlain((_size - _free) * 100 / _size));
				_parameters.put("used-raw", String.valueOf((_size - _free)));
				_parameters.put("free-raw", getFormattedPlain(_free));
				_parameters.put("free", getFormattedSize(_free));
			} catch(NumberFormatException _ex) {}
		}
		return _parameters;
	}
	
	private static String getFormattedInternalMB(double size) {
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		_sb.append(_df.format(size / 1048576D));
		return _sb.toString();
	}
	
	private static String getFormattedInternalSize(double size) {
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		StringBuilder _sb = new StringBuilder();
		if(size >= 1125899906842620D) {
			_sb.append(_df.format(size / 1125899906842620D));
			_sb.append("p");
		} else if(size >= 1099511627776D) {
			_sb.append(_df.format(size / 1099511627776D));
			_sb.append("t");
		} else if(size >= 1073741824D) {
			_sb.append(_df.format(size / 1073741824D));
			_sb.append("g");
		} else if(size >= 1048576D) {
			_sb.append(_df.format(size / 1048576D));
			_sb.append("m");
		} else if(size >= 1024D) {
			_sb.append(_df.format(size / 1024D));
			_sb.append("k");
		} else {
			_sb.append(_df.format(size));
		}
		return _sb.toString();
	}
	
	private static String getFormattedSize(double size) {
		StringBuilder _sb = new StringBuilder();
		DecimalFormat _df = new DecimalFormat("#.##");
		_df.setDecimalSeparatorAlwaysShown(false);
		if(size >= 1125899906842620D) {
			_sb.append(_df.format(size / 1125899906842620D));
			_sb.append(" PB");
		} else if(size >= 1099511627776D) {
			_sb.append(_df.format(size / 1099511627776D));
			_sb.append(" TB");
		} else if(size >= 1073741824D) {
			_sb.append(_df.format(size / 1073741824D));
			_sb.append(" GB");
		} else if(size >= 1048576D) {
			_sb.append(_df.format(size / 1048576D));
			_sb.append(" MB");
		} else if(size >= 1024D) {
			_sb.append(_df.format(size / 1024D));
			_sb.append(" KB");
		} else {
			_sb.append(_df.format(size));
		}
		return _sb.toString();
	}
	
	private static String getFormattedPlain(double size) {
		DecimalFormat _df = new DecimalFormat("#");
		_df.setDecimalSeparatorAlwaysShown(false);
		return _df.format(size);
	}
	
	public static Map<String, String> getGroupQuota(String groupname, String group, String volume) throws Exception {
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		Map<String, String> _group = new HashMap<String, String>();
		if(mountPath == null || groupname == null || mountPath.isEmpty() || groupname.isEmpty()) {
			return _group;
		} else if(groupname.equalsIgnoreCase("root") || groupname.equalsIgnoreCase("bacula")  || groupname.equalsIgnoreCase("tape")) {
			return _group;
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/xfs_quota -x -c \"report -g -aN\" ");
		_sb.append(mountPath);
		try {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					StringTokenizer _st = new StringTokenizer(_line, " ");
					if(!_st.hasMoreTokens()) {
						continue;
					}
					String _groupname = _st.nextToken();
					if(!_groupname.equals(groupname)) {
						continue;
					}
					try {
						if(_st.hasMoreTokens()) {
							_group.put("used", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							double _size = getSize(_st.nextToken()) * 1024;
							_group.put("soft", getFormattedSize(_size));
							_group.put("mb_soft", getFormattedInternalMB(_size));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							double _size = getSize(_st.nextToken()) * 1024;
							_group.put("hard", getFormattedSize(_size));
							_group.put("mb_hard", getFormattedInternalMB(_size));
						}
					} catch(NumberFormatException _ex) {}
					
					if(_group.get("used") == null) {
						_group.put("used", "");
					}
					if(_group.get("soft") == null) {
						_group.put("soft", "");
					}
					if(_group.get("hard") == null) {
						_group.put("hard", "");
					}
					
					return _group;
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read group quotas : " + _ex.getMessage());
		}
		throw new Exception("group quota not found");
	}
	
	private static double getSize(String value) {
		double _size = 0;
		if(value == null || value.trim().isEmpty()) {
			return _size;
		}
		value = value.trim();
		if(value.matches("[0-9.]+([KMGTP]B)$")) {
			value = value.substring(0, value.length() -1);
		}
		char unit = 'B';
		if(value.matches("[0-9.]+([KMGTP])$")) {
			unit = value.charAt(value.length() - 1);
			value = value.substring(0, value.length() - 1).trim();
		}
		try {
			if(value.contains(",")) {
				value = value.replace(",", ".");
			}
			_size = Double.parseDouble(value);
		} catch(NumberFormatException _ex) {}
		switch(unit) {
			case 'B': {
					// nothing
				}
				break;
			case 'K': {
					_size = _size * 1024D;
				}
				break;
			case 'M': {
					_size = _size * 1048576D;
				}
				break;
			case 'G': {
					_size = _size * 1073741824D;
				}
				break;
			case 'T': {
					_size = _size * 1099511627776D;
				}
				break;
			case 'P': {
					_size = _size * 1125899906842620D;
				}
				break;
		}
		return _size;
	}
	
	public static Map<String, String> getUserQuota(String group, String volume, String user) throws Exception {
		Map<String, String> _user = new HashMap<String, String>();
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		if(mountPath == null || user == null || mountPath.isEmpty() || user.isEmpty()) {
			return _user;
		} else if(user.equalsIgnoreCase("root") || user.equalsIgnoreCase("bacula")) {
			return _user;
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/xfs_quota -x -c \"report -u -aN\" ");
		_sb.append(mountPath);
		try {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					StringTokenizer _st = new StringTokenizer(_line, " ");
					if(!_st.hasMoreTokens()) {
						continue;
					}
					String _username = _st.nextToken();
					if(!_username.equals(user)) {
						continue;
					}
					try {
						if(_st.hasMoreTokens()) {
							_user.put("used", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							double _size = getSize(_st.nextToken()) * 1024;
							_user.put("soft", getFormattedSize(_size));
							_user.put("mb_soft", getFormattedInternalMB(_size));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							double _size = getSize(_st.nextToken()) * 1024;
							_user.put("hard", getFormattedSize(_size));
							_user.put("mb_hard", getFormattedInternalMB(_size));
						}
					} catch(NumberFormatException _ex) {}
					
					if(_user.get("used") == null) {
						_user.put("used", "");
					}
					if(_user.get("soft") == null) {
						_user.put("soft", "");
					}
					if(_user.get("hard") == null) {
						_user.put("hard", "");
					}
					
					return _user;
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read user quotas : " + _ex.getMessage());
		}
		throw new Exception("user quota not found");
	}
	
	public static void resizeFileSystem(String group, String name) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/xfs_growfs ");
		_sb.append(VolumeManager.getMountPath(group, name));
		Command.systemCommand(_sb.toString());
	}
	
	public static Map<String, Map<String, String>> searchGroupQuotas(String match, String group, String volume) throws Exception {
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		Map<String, Map<String, String>> _groups = new HashMap<String, Map<String, String>>();
		if(mountPath == null || mountPath.isEmpty()) {
			return _groups;
		}
		if(match == null) {
			match = "";
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/xfs_quota -x -c \"report -g -aN\" ");
		_sb.append(mountPath);
		try {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					StringTokenizer _st = new StringTokenizer(_line);
					if(!_st.hasMoreTokens()) {
						continue;
					}
					Map<String, String> _group = new HashMap<String, String>();
					String _groupname = _st.nextToken();
					if(!match.isEmpty() && !_groupname.startsWith(match)) {
						continue;
					} else if(_groupname.equalsIgnoreCase("root") ||
							_groupname.equalsIgnoreCase("tape")) {
						continue;
					}
					try {
						if(_st.hasMoreTokens()) {
							_group.put("used", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							_group.put("soft", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							_group.put("hard", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					
					if(_group.get("used") == null) {
						_group.put("used", "");
					}
					if(_group.get("soft") == null) {
						_group.put("soft", "");
					}
					if(_group.get("hard") == null) {
						_group.put("hard", "");
					}
					_groups.put(_groupname, _group);
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read user quotas : " + _ex.getMessage());
		}
		return _groups;
	}
	
	public static Map<String, Map<String, String>> searchUserQuotas(String match, String group, String volume) throws Exception {
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		Map<String, Map<String, String>> _users = new HashMap<String, Map<String, String>>();
		if(mountPath == null || mountPath.isEmpty()) {
			return _users;
		}
		if(match == null) {
			match = "";
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/xfs_quota -x -c \"report -u -aN\" ");
		_sb.append(mountPath);
		try {
			BufferedReader _br = new BufferedReader(new StringReader(Command.systemCommand(_sb.toString())));
			try {
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_users.size() > 10) {
						break;
					}
					StringTokenizer _st = new StringTokenizer(_line);
					if(!_st.hasMoreTokens()) {
						continue;
					}
					Map<String, String> _user = new HashMap<String, String>();
					String _username = _st.nextToken();
					if(!match.isEmpty() && !_username.startsWith(match)) {
						continue;
					} else if(_username.equalsIgnoreCase("root") || _username.equalsIgnoreCase("bacula")) {
						continue;
					}
					try {
						if(_st.hasMoreTokens()) {
							_user.put("used", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							_user.put("soft", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					try {
						if(_st.hasMoreTokens()) {
							_user.put("hard", getFormattedSize(getSize(_st.nextToken()) * 1024));
						}
					} catch(NumberFormatException _ex) {}
					
					if(_user.get("used") == null) {
						_user.put("used", "");
					}
					if(_user.get("soft") == null) {
						_user.put("soft", "");
					}
					if(_user.get("hard") == null) {
						_user.put("hard", "");
					}
					_users.put(_username, _user);
				}
			} finally {
				_br.close();
			}
		} catch(Exception _ex) {
			throw new Exception("cannot read user quotas : " + _ex.getMessage());
		}
		return _users;
	}
	
	public static void setGroupQuota(String groupname, String group, String volume, double size) throws Exception {
		if(!UserManager.systemGroupExists(groupname)) {
			throw new Exception("group does not exists");
		}
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		if(mountPath == null || groupname == null || mountPath.isEmpty() || groupname.isEmpty()) {
			return;
		} else if(groupname.equalsIgnoreCase("root") || groupname.equalsIgnoreCase("bacula")  || groupname.equalsIgnoreCase("tape")) {
			return;
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/xfs_quota -x -c \"limit -g bsoft=");
		if(size <= 0) {
			_sb.append("0 ");
		} else {
			_sb.append(getFormattedInternalSize(size));
			_sb.append(" ");
		}
		_sb.append("bhard=");
		if(size <= 0) {
			_sb.append("0 ");
		} else {
			_sb.append(getFormattedInternalSize(size));
			_sb.append(" ");
		}
		_sb.append(groupname);
		_sb.append("\" ");
		_sb.append(mountPath);
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			throw new Exception("cannot write group quotas : " + _ex.getMessage());
		}
	}
	
	public static void setUserQuota(String username, String group, String volume, double size) throws Exception {
		if(!UserManager.systemUserExists(username)) {
			throw new Exception("user does not exists");
		}
		String mountPath = VolumeManager.getLogicalVolumeMountPath(group, volume);
		if(mountPath == null || username == null || mountPath.isEmpty() || username.isEmpty()) {
			return;
		} else if(username.equalsIgnoreCase("root") || username.equalsIgnoreCase("bacula")) {
			return;
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/sbin/xfs_quota -x -c \"limit -u bsoft=");
		if(size <= 0) {
			_sb.append("0 ");
		} else {
			_sb.append(getFormattedInternalSize(size));
			_sb.append(" ");
		}
		_sb.append("bhard=");
		if(size <= 0) {
			_sb.append("0 ");
		} else {
			_sb.append(getFormattedInternalSize(size));
			_sb.append(" ");
		}
		_sb.append(username);
		_sb.append("\" ");
		_sb.append(mountPath);
		System.out.println("Command: " + _sb.toString());
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			throw new Exception("cannot write user quotas : " + _ex.getMessage());
		}
	}
}
