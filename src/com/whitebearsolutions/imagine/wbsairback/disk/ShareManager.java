package com.whitebearsolutions.imagine.wbsairback.disk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.db.DBException;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.net.FTPManager;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class ShareManager {
	public final static int NFS = 1;
	public final static int CIFS = 2;
	public final static int FTP = 3;
	private Configuration _c;
	
	private final static Logger logger = LoggerFactory.getLogger(ShareManager.class);
	
	public ShareManager(Configuration conf) {
		this._c = conf;
	}
	
	public static void addShare(String group, String volume, int type, Map<String, String> attributes) throws Exception {
		if(attributes == null) {
			attributes = new HashMap<String, String>();
		}
		switch(type) {
			case NFS:{
					try {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						List<Map<String, String>> _shares = getShares(NFS);
						Map<String, String> _share = new HashMap<String, String>();
						_share.put("fstype", VolumeManager.getLogicalVolumeFS(group, volume));
						_share.put("path", _path);
						if(attributes.get("squash") != null && "true".equals(attributes.get("squash"))) {
							_share.put("squash", "true");
						}
						if(attributes.get("async") != null && "true".equals(attributes.get("async"))) {
							_share.put("async", "true");
						}
						if(attributes.get("address") != null) {
							_share.put("address", attributes.get("address"));
						}
						_share.put("fstype", VolumeManager.getLogicalVolumeFS(group, volume));
						_shares.add(_share);
						writeShares(NFS, _shares);
					} catch(Exception _ex) {
						throw _ex;
					}
				}
				break;
			case CIFS: {
					try {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						List<Map<String, String>> _shares = getShares(CIFS);
						Map<String, String> _share = new HashMap<String, String>();
						if(attributes.containsKey("name") && attributes.get("name") != null) {
							if(!attributes.get("name").matches("[a-zA-Z0-9_/-]+")) {
								throw new Exception("invalid share name format");
							}
							_share.put("name", attributes.get("name"));
						} else {
							_share.put("name", group + "/" + volume);
						}
						_share.put("path", _path);
						if(attributes.get("recycle") != null && "true".equalsIgnoreCase(attributes.get("recycle"))) {
							_share.put("recycle", "true");
						}
						_share.put("fstype", VolumeManager.getLogicalVolumeFS(group, volume));
						_shares.add(_share);
						writeShares(CIFS, _shares);
					} catch(Exception _ex) {
						throw _ex;
					}
				}
				break;
			case FTP: {
					boolean anonymous = false;
					FTPManager _ftpm = new FTPManager();
					_ftpm.addShare(volume, group, anonymous);
					_ftpm.write();
				}
				break;
		}
	}
	
	public static void addExtrenalShare(String server, String share, String user, String password, String domain, int type) throws Exception {
		File _f = new File(getExternalMountPath(server, share));
		if(_f.isDirectory()) {
			if(isExternalShare(server, share)) {
				throw new Exception("external share name already exists");
			}
		} else {
			_f.mkdirs();
		}
		
		if (!share.startsWith("/"))
			share="/"+share;
		
		share = share.trim();
		if (share.contains(" "))
			share = share.replace(" ", "\\ ");
		
		try {
			switch(type) {
				case NFS: {
						mountExternalShare(NFS, server, share, user, password);
						addNFSShareToFstab(server, share);
					}
					break;
				case CIFS: {
						try {
							addCIFSCredentials(server, share, user, password);
							mountExternalShare(CIFS, server, share, user, password);
							addCIFSShareToFstab(server, share, user, password, domain);
						} catch(Exception _ex) {
							removeCIFSCredentials(server, share);
							throw _ex;
						}
					}
					break;
			}
		} catch(Exception _ex) {
			if(!VolumeManager.isSystemMounted(_f.getAbsolutePath())) {
				Command.systemCommand("/bin/rm -rf " + _f.getAbsolutePath());
			}
			throw _ex;
		}
	}
	
	public static List<Map<String, String>> getAllShares() throws Exception {
		List<Map<String, String>> _shares = new ArrayList<Map<String,String>>();
		_shares.addAll(getShares(NFS));
		_shares.addAll(getShares(CIFS));
		_shares.addAll(getShares(FTP));
		/**
		 * TODO
		 */
		//_shares.addAll(getShares(WEBDAV));
		return _shares;
	}
	
	public static List<Map<String, String>> getShares(int type) throws Exception {
		List<Map<String, String>> _shares = new ArrayList<Map<String,String>>();
		switch(type) {
			case NFS: {
				File _f = new File(WBSAirbackConfiguration.getFileShareNfs());
				if(_f.exists()) {
					BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
					try {
						for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.trim().isEmpty() || _line.trim().startsWith("#")) {
								continue;
							}
							Map<String, String> _share = new HashMap<String, String>();
							StringBuilder _address = new StringBuilder();
							StringTokenizer _st = new StringTokenizer(_line, " ");
							_share.put("protocol", "NFS");
							_share.put("path", _st.nextToken());
							try {
								String[] _volume = _share.get("path").split("/");
								VolumeManager.getLogicalVolume(_volume[_volume.length - 2], _volume[_volume.length - 1]);
								_share.put("vg", _volume[_volume.length - 2]);
								_share.put("lv", _volume[_volume.length - 1]);
								_share.put("fstype", VolumeManager.getLogicalVolumeFS(_share.get("vg"), _share.get("lv")));
							} catch(Exception _ex) {}
							while(_st.hasMoreTokens()) {
								String _token = _st.nextToken();
								if(!_share.containsKey("squash") && _token.contains(",no_root_squash,")) {
									_share.put("squash", "true");
								}
								if(_token.contains(",fsid=")) {
									String _value = _token.substring(_token.indexOf(",fsid=") + 6);
									_value = _value.substring(0, _value.indexOf(","));
									if(!_value.isEmpty()) {
										_share.put("fsid", _value);
									}
								}
								if(!_share.containsKey("async") && _token.contains(",async,")) {
									_share.put("async", "true");
								}
								_token = _token.substring(0, _token.indexOf("("));
								if(_token.trim().equals("*")) {
									continue;
								}
								
								if(_address.length() > 0) {
									_address.append(",");
								}
								_address.append(_token);
							}
							if(_address.length() > 0) {
								_share.put("address", _address.toString());
							}
							_shares.add(_share);
						}
					} finally {
						_br.close();
					}
				}
			}
			break;
		case CIFS: {
				File _f = new File(WBSAirbackConfiguration.getFileShareCifs());
				if(_f.exists()) {
					BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
					try {
						for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
							if(_line.startsWith("[")) {
								try {
									Map<String, String> _share = new HashMap<String, String>();
									_share.put("name", _line.substring(1, _line.length() - 1));
									_share.put("protocol", "CIFS");
									for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
										if(_line.trim().isEmpty()) {
											break;
										} else if(_line.contains("path =")) {
											_share.put("path", _line.substring(_line.indexOf("=") + 1).trim());
											try {
												String[] _volume = _share.get("path").split("/");
												VolumeManager.getLogicalVolume(_volume[_volume.length - 2], _volume[_volume.length - 1]);
												_share.put("vg", _volume[_volume.length - 2]);
												_share.put("lv", _volume[_volume.length - 1]);
											} catch(Exception _ex) {}
										} else if(_line.contains("vfs objects =")) {
											if(_line.contains("recycle")) {
												_share.put("recycle", "true");
											} else {
												_share.put("recycle", "false");
											}
										}
									}
									_shares.add(_share);
								} catch(Exception _ex) {}
							}
						}
					} finally {
						_br.close();
					}
				}
			}
			break;
		case FTP: {
				FTPManager _ftpm = new FTPManager();
				for(Map<String, String> share : _ftpm.getShares()) {
					Map<String, String> _share = new HashMap<String, String>();
					_share.put("name", share.get("group") + "/" + share.get("volume"));
					_share.put("protocol", "FTP");
					_share.put("path", share.get("group") + "/" + share.get("volume"));
					_share.put("vg", share.get("group"));
					_share.put("lv", share.get("volume"));
					_shares.add(_share);
				}
			}
			break;
		}
		return _shares;
	}
	
	public static Map<String, String> getShare(int type, String group, String volume) throws Exception {
		Map<String, String> _share = new HashMap<String,String>();
		switch(type) {
			case NFS: {
					File _f = new File(WBSAirbackConfiguration.getFileShareNfs());
					if(_f.exists()) {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
						try {
							for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
								if(_line.contains(_path)) {
									StringBuilder _address = new StringBuilder();
									StringTokenizer _st = new StringTokenizer(_line, " ");
									_share.put("protocol", "NFS");
									_share.put("path", _st.nextToken());
									if(!_path.equals(_share.get("path"))) {
										continue;
									}
									try {
										String[] _volume = _share.get("path").split("/");
										VolumeManager.getLogicalVolume(_volume[_volume.length - 2], _volume[_volume.length - 1]);
										_share.put("vg", _volume[_volume.length - 2]);
										_share.put("lv", _volume[_volume.length - 1]);
									} catch(Exception _ex) {}
									while(_st.hasMoreTokens()) {
										String _token = _st.nextToken();
										if(!_share.containsKey("squash") && _token.contains(",no_root_squash,")) {
											_share.put("squash", "true");
										}
										if(_token.contains(",fsid=")) {
											String _value = _token.substring(_token.indexOf(",fsid=") + 6);
											_value = _value.substring(0, _value.indexOf(","));
											_share.put("fsid", _value);
										}
										if(!_share.containsKey("async") && _token.contains(",async,")) {
											_share.put("async", "true");
										}
										_token = _token.substring(0, _token.indexOf("("));
										if(_token.trim().equals("*")) {
											continue;
										}
										
										if(_address.length() > 0) {
											_address.append(",");
										}
										_address.append(_token);
									}
									if(_address.length() > 0) {
										_share.put("address", _address.toString());
									}
									return _share;
								}
							}
						} finally {
							_br.close();
						}
					}
				}
				break;
			case CIFS: {
					File _f = new File(WBSAirbackConfiguration.getFileShareCifs());
					if(_f.exists()) {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
						try {
							for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
								if(_line.startsWith("[")) {
									try {
										_share.put("name", _line.substring(1, _line.length() - 1));
										_share.put("protocol", "CIFS");
										for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
											if(_line.trim().isEmpty()) {
												break;
											} else if(_line.contains("path =")) {
												if(!_path.equals(_line.substring(_line.indexOf("=") + 1).trim())) {
													continue;
												}
												_share.put("path", _line.substring(_line.indexOf("=") + 1).trim());
												try {
													String[] _volume = _share.get("path").split("/");
													VolumeManager.getLogicalVolume(_volume[_volume.length - 2], _volume[_volume.length - 1]);
													_share.put("vg", _volume[_volume.length - 2]);
													_share.put("lv", _volume[_volume.length - 1]);
												} catch(Exception _ex) {}
											} else if(_line.contains("vfs objects =")) {
												if(_line.contains("recycle")) {
													_share.put("recycle", "true");
												} else {
													_share.put("recycle", "false");
												}
											}
										}
										if(_share.containsKey("path")) {
											return _share;
										}
									} catch(Exception _ex) {}
								}
							}
						} finally {
							_br.close();
						}
					}
				}
				break;
			case FTP: {
					FTPManager _ftpm = new FTPManager();
					for(Map<String, String> share : _ftpm.getShares()) {
						if(share.get("volume") != null && share.get("group") != null &&
								share.get("volume").equalsIgnoreCase(volume) && share.get("group").equalsIgnoreCase(group)) {
							share.put("protocol", "FTP");
							share.put("name", share.get("group") + "/" + share.get("volume"));
							share.put("path", share.get("group") + "/" + share.get("volume"));
							return share;
						}
					}
				}
				break;
		}
		throw new Exception("volume share not found");
	}
	
	public static Map<String, String> getExternalShare(String server, String share) throws Exception {
		Map<String, String> _share = new HashMap<String,String>();
		if(server == null || server.isEmpty() || share == null || share.isEmpty()) {
	    	return _share;
	    }
		File _f = new File(WBSAirbackConfiguration.getFileFstab()); 
	    if(!_f.exists()) {
	    	return _share;
	    }
		 
		for(String[] _values : readFstab().values()) {
			if("nfs".equals(_values[2]) || "cifs".equals(_values[2])) {
				
				if("cifs".equals(_values[2])) {
					String _value = _values[0].replaceAll("//", "");
					_share.put("server", _value.split("/")[0]);
					_share.put("share", _value.substring(_value.indexOf("/") + 1));
				} else if("nfs".equals(_values[2])) {
					_share.put("server", _values[0].split(":")[0]);
					_share.put("share", _values[0].split(":")[1]);					
				}
				
				if(!server.equals(_share.get("server")) || !share.equals(_share.get("share"))) {
					_share = new HashMap<String, String>();
					continue;
				}
				
				_share.put("name", getNameFromMountPath(_values[1]));
				_share.put("path", _values[1]);
				
				double _size = getExternalShareSize(getServerFromMountPath(_values[1]), getShareFromMountPath(_values[1]));
				StringBuilder _sb =new StringBuilder();
				if(_size >= 1073741824) {
					_sb.append(Math.rint(_size/1073741824));
					_sb.append(" TB");
				} else if(_size >= 1048576) {
					_sb.append(Math.rint(_size/1048576));
					_sb.append(" GB");
				} else if(_size >= 1024) {
					_sb.append(Math.rint(_size/1024));
					_sb.append(" MB");
				} else {
					_sb.append(Math.rint(_size));
					_sb.append(" KB");
				}
				
				_share.put("size", _sb.toString());
				_share.put("type", _values[2]); 
				_share.put("mount", String.valueOf(VolumeManager.isSystemMounted(_values[1])));
				break;
			}
		}
		return _share;
	}
	
	public static Map<String, String> getExternalShareTotals(String server, String share) throws Exception {
		Map<String, String> _share = new HashMap<String,String>();
		Map<String, Integer> _partitions = GeneralSystemConfiguration.getDiskLoad();
		String _percent=new String();
		if(server == null || server.isEmpty() || share == null || share.isEmpty()) {
	    	return _share;
	    }
		File _f = new File(WBSAirbackConfiguration.getFileFstab()); 
	    if(!_f.exists()) {
	    	return _share;
	    }
		 
		for(String[] _values : readFstab().values()) {
			if("nfs".equals(_values[2]) || "cifs".equals(_values[2])) {
				if("cifs".equals(_values[2])) {
					String _value = _values[0].replaceAll("//", "");
					_share.put("server", _value.split("/")[0]);
					_share.put("share", _value.substring(_value.indexOf("/") + 1));
				} else if("nfs".equals(_values[2])) {
					_share.put("server", _values[0].split(":")[0]);
					_share.put("share", _values[0].split(":")[1]);					
				}
				if(!server.equals(_share.get("server")) || !share.equals(_share.get("share"))) {
					continue;
				}
				
				_share.put("name", getNameFromMountPath(_values[1]));
				_share.put("path", _values[1]);
				
				double _size = getExternalShareSize(getServerFromMountPath(_values[1]), getShareFromMountPath(_values[1]));
				StringBuilder _sb =new StringBuilder();
				if(_size >= 1073741824) {
					_sb.append(Math.rint(_size/1073741824));
					_sb.append(" TB");
				} else if(_size >= 1048576) {
					_sb.append(Math.rint(_size/1048576));
					_sb.append(" GB");
				} else if(_size >= 1024) {
					_sb.append(Math.rint(_size/1024));
					_sb.append(" MB");
				} else {
					_sb.append(Math.rint(_size));
					_sb.append(" KB");
				}
				
				_share.put("size", _sb.toString());
				_share.put("size-raw", String.valueOf(_size*1024));
				if (_partitions.containsKey(_values[1])){
					_percent = String.valueOf(_partitions.get(_values[1]));
					_share.put("used", _percent.concat("%"));
				}
				_share.put("type", _values[2]); 
				_share.put("mount", String.valueOf(VolumeManager.isSystemMounted(_values[1])));
				break;
			}
		}
		return _share;
	}
	
	public static String getExternalShareMountPath(String server, String share) throws Exception {
		String _server = "", _share = "";
		if(server == null || server.isEmpty() || share == null || share.isEmpty()) {
	    	throw new Exception("invalid server or share");
	    }
		File _f = new File(WBSAirbackConfiguration.getFileFstab()); 
	    if(!_f.exists()) {
	    	throw new Exception("cannot found mounts");
	    }
		
	    for(String[] _values : readFstab().values()) {
			if("nfs".equals(_values[2]) || "cifs".equals(_values[2])) {
				if("cifs".equals(_values[2])) {
					String _value = _values[0].replaceAll("//", "");
					_server =  _value.split("/")[0];
					_share =  _value.substring(_value.indexOf("/"));
					if(!share.trim().startsWith("/")) {
						share = "/".concat(share);
					}
				} else if("nfs".equals(_values[2])) {
					_server = _values[0].split(":")[0];
					_share = _values[0].split(":")[1];
					if(!share.trim().startsWith("/")) {
						share = "/".concat(share);
					}
					if (!share.endsWith("/"))
						share+="/";
					if (!_share.endsWith("/"))
						_share+="/";
				}
				if(!server.equals(_server) || !share.equals(_share)) {
					continue;
				}
				
				return _values[1];
			}
		}
		throw new Exception("external share does not exists");
	}
	
	public static List<Map<String, String>> getExternalShares() throws Exception {
		List<Map<String, String>> volumes = new ArrayList<Map<String,String>>();
		File _f = new File(WBSAirbackConfiguration.getFileFstab()); 
	    if(!_f.exists()) {
	    	_f.createNewFile();
	    }
		 
		for(String[] _values : readFstab().values()) {
			if("nfs".equals(_values[2]) || "cifs".equals(_values[2])) {
				Map<String, String> volume = new HashMap<String, String>();
				volume.put("name", getNameFromMountPath(_values[1]));
				volume.put("path", _values[1]);
				volume.put("type", _values[2]);
				try {
					double _size = getExternalShareSize(getServerFromMountPath(_values[1]), getShareFromMountPath(_values[1]));
					StringBuilder _sb =new StringBuilder();
					if(_size >= 1073741824) {
						_sb.append(Math.rint(_size/1073741824));
						_sb.append(" TB");
					} else if(_size >= 1048576) {
						_sb.append(Math.rint(_size/1048576));
						_sb.append(" GB");
					} else if(_size >= 1024) {
						_sb.append(Math.rint(_size/1024));
						_sb.append(" MB");
					} else {
						_sb.append(Math.rint(_size));
						_sb.append(" KB");
					}
					
					volume.put("size", _sb.toString());
				} catch(Exception _ex) {
					volume.put("size", "");
					System.out.println("ShareManager::getExternalShares::size: " + _ex.getMessage());
				}
				try {
					if("cifs".equals(_values[2])) {
						String _value = _values[0].replaceAll("//", "");
						volume.put("server", _value.split("/")[0]);
						volume.put("share", _value.substring(_value.indexOf("/")+1));
					} else if("nfs".equals(_values[2])) {
						volume.put("server", _values[0].split(":")[0]);
						volume.put("share", _values[0].split(":")[1]);					
					}
				} catch(Exception _ex) {
					volume.put("server", "");
					volume.put("share", "");
					System.out.println("ShareManager::getExternalShares::server " + _ex.getMessage());
				}
				try {
					volume.put("mount", String.valueOf(VolumeManager.isSystemMounted(_values[1])));
				} catch(Exception _ex) {
					System.out.println("ShareManager::getExternalShares::mount " + _ex.getMessage());
				}
				volumes.add(volume);
			}
		}
		return volumes;
	}
	
	public static List<String> getExternalShareNames() throws Exception {
		String _server = "", _share = "";
		List<String> volumes = new ArrayList<String>();
		File _f = new File(WBSAirbackConfiguration.getFileFstab()); 
	    if(!_f.exists()) {
	    	_f.createNewFile();
	    }
		 
		for(String[] _values : readFstab().values()) {
			if("nfs".equals(_values[2]) || "cifs".equals(_values[2])) {
				if("cifs".equals(_values[2])) {
					String _value = _values[0].replaceAll("//", "");
					_server =  _value.split("/")[0];
					_share =  _value.substring(_value.indexOf("/")+1);
				} else if("nfs".equals(_values[2])) {
					_server = _values[0].split(":")[0];
					_share = _values[0].split(":")[1];					
				}
				volumes.add(_server + "@" + _share);
			}
		}
		return volumes;
	}
	
	public static double getExternalShareSize(String server, String share) throws Exception {
		String _path = null, _special_path;
		String[] SPECIAL = new String[] { "$", "?", "^", ".", "[", "]" };
		if(server == null || share == null || server.isEmpty() || share.isEmpty()) {
			throw new Exception("invalid share");
		}
		
		_path = getExternalShareMountPath(server, share);
		_special_path = new String(_path);
		if(_special_path.contains("\\ ")) {
			_special_path = _special_path.replace("\\ ", " ");
		}
		if(_special_path.contains("\\")) {
			_special_path = _special_path.replace("\\", "\\\\");
		}
		for(String _char : SPECIAL) {
			if(_special_path.contains(_char)) {
				_special_path = _special_path.replace(_char, "\\".concat(_char));
			}
		}
		if(!VolumeManager.isSystemMounted(_path)) {
			return 0;
		}
		
		String _output;
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" /bin/df -P ");
			_sb.append(_path);
			_sb.append(" | /bin/grep '");
			_sb.append(_special_path);
			int numberSpaces = _special_path.length() - _special_path.replace(" ", "").length();
			_sb.append("' | /usr/bin/awk '{print $"+(2+numberSpaces)+"}'");
			_output = Command.systemCommand(_sb.toString());
			if(_output == null || _output.isEmpty() && _special_path.endsWith("/")) {
				_special_path = _special_path.substring(0, _special_path.length()-1);
				 _sb = new StringBuilder();
				_sb.append("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" /bin/df -P ");
				_sb.append(_path);
				_sb.append(" | /bin/grep '");
				_sb.append(_special_path);
				_sb.append("' | /usr/bin/awk '{print $"+(2+numberSpaces)+"}'");
				_output = Command.systemCommand(_sb.toString());
				if(_output == null || _output.isEmpty()) {
					throw new Exception(_path);
				}
			}
			try {
				return Double.parseDouble(_output);
			} catch(NumberFormatException _ex) {
				throw new Exception("invalid volume size format found");
			}
		} catch(Exception _ex) {
			throw new Exception("fail to get volume size - " + _ex.getMessage());
	    }
	}
	
	public static String getNameFromMountPath(String path) {
		if(path.startsWith(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
			path = path.substring(WBSAirbackConfiguration.getDirectoryVolumeMount().length());
		}
		
		StringBuilder _sb = new StringBuilder();
		String[] _tokens = path.split("/");
		if(_tokens.length > 1) {
			_sb.append(_tokens[_tokens.length - 2]);
			_sb.append("/");
			_sb.append(_tokens[_tokens.length - 1]);
		} else if(_tokens.length == 1) {
			_sb.append(_tokens[0]);
		}
		return _sb.toString();
	}
	
	private static String getServerFromMountPath(String path) {
		if(path.startsWith(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
			path = path.substring(WBSAirbackConfiguration.getDirectoryVolumeMount().length());
		}
		
		String[] _tokens = path.split("/");
		if(_tokens.length > 1) {
			return _tokens[2];
		} else {
			return "";
		}
	}
	
	public static String getShareFromMountPath(String path) {
		if(path.startsWith(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
			path = path.substring(WBSAirbackConfiguration.getDirectoryVolumeMount().length());
		}
		
		String[] _tokens = path.split("/");
		if(_tokens.length > 1) {
			StringBuilder _sb = new StringBuilder();
			for(int i = 3; i < _tokens.length; i++) {
				if(_sb.length() > 0) {
					_sb.append("/");
				}
				_sb.append(_tokens[i]);
			}
			return _sb.toString();
		} else {
			return "";
		}
	}
	
	public static boolean isExternalShare(String server, String share) {
		try {
			getExternalShareMountPath(server, share);
			return true;
		} catch(Exception _ex) {
			return false;
		}
	}
	
	public static boolean isExternalShareMounted(String server, String share) {
		return VolumeManager.isSystemMounted(getExternalMountPath(server, share));
	}
	
	public static boolean isShare(String group, String volume) throws Exception {
		if(isShare(NFS, group, volume)) {
			return true;
		} else if(isShare(CIFS, group, volume)) {
			return true;
		} else if(isShare(FTP, group, volume)) {
			return true;
		}
		return false;
	}
	
	public static boolean isShareFromPath(String path) throws Exception {
		try {
			String group = path.substring(path.indexOf(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/")+(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/").length());
			if (group.endsWith("/"))
				group = group.substring(group.length()-1);
			String volume = group.substring(group.indexOf("/")+1);
			group = group.substring(0, group.indexOf("/"));
			return isShare(group, volume);
		} catch (Exception ex) {
			logger.error("Error checking share from path: {}. Ex: {}", path, ex.getMessage());
			return false;
		}
	}
	
	public static boolean isShare(int type, String group, String volume) throws Exception {
		switch(type) {
			case NFS: {
					File _f = new File(WBSAirbackConfiguration.getFileShareNfs());
					if(_f.exists()) {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
						try {
							for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
								if(_line.contains(_path)) {
									return true;
								}
							}
						} finally {
							_br.close();
						}
					}
				}
				break;
			case CIFS: {
					File _f = new File(WBSAirbackConfiguration.getFileShareCifs());
					if(_f.exists()) {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
						try {
							for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
								if(_line.startsWith("[")) {
									try {
										for(_line = _br.readLine(); _line != null; _line = _br.readLine()) {
											if(_line.trim().isEmpty()) {
												break;
											} else if(_line.contains("path =") && _line.contains(_path)) {
												return true;
											}
										}
									} catch(Exception _ex) {}
								}
							}
						} finally {
							_br.close();
						}
					}
				}
				break;
			case FTP: {
					FTPManager _ftpm = new FTPManager();
					for(Map<String, String> share : _ftpm.getShares()) {
						if(share.get("volume") != null && share.get("group") != null &&
								share.get("volume").equalsIgnoreCase(volume) && share.get("group").equalsIgnoreCase(group)) {
							return true;
						}
					}
				}
				break;
		}
		return false;
	}
	
	public static void mountExternalShare(int type, String server, String share, String user, String password) throws Exception {
		StringBuilder _sb = new StringBuilder();
		StringBuilder _sbWithNtlmv2 = null;
		if(!isExternalShareMounted(server, share)) {
			if (!new File(getExternalMountPath(server, share)).exists()) {
				new File(getExternalMountPath(server, share)).mkdirs();
			}
			switch(type) {
				case NFS: {
						_sb.append("timeout ");
						_sb.append(WBSAirbackConfiguration.getTimeoutDfCommand());
						_sb.append(" ");
						_sb.append("/sbin/mount.nfs ");
						_sb.append(server);
						_sb.append(":");
						_sb.append(share);
						_sb.append(" ");
						_sb.append(getExternalMountPath(server, share));
						_sb.append(" -o rw,hard,intr,rsize=8192,wsize=8192");
					}
					break;
				case CIFS: {
						_sbWithNtlmv2 = new StringBuilder();
						_sb.append("timeout ");
						_sb.append(WBSAirbackConfiguration.getTimeoutDfCommand());
						_sb.append(" ");
						_sb.append("/sbin/mount.cifs //");
						if (server.endsWith("/"))
							server = server.substring(0, server.length()-1);
						_sb.append(server);
						if (!share.startsWith("/"))
							share = "/"+share;
						_sb.append(share);
						_sb.append(" ");
						_sb.append(getExternalMountPath(server, share));
						_sb.append(" -o rw,");
						
						_sbWithNtlmv2.append(_sb);
						_sbWithNtlmv2.append("sec=ntlmv2,");
						
						if(user != null && !user.isEmpty() &&
								password != null && !password.isEmpty()) {
							_sb.append("user=");
							_sb.append(user);
							_sb.append(",password=");
							_sb.append(password);
							
							_sbWithNtlmv2.append("user=");
							_sbWithNtlmv2.append(user);
							_sbWithNtlmv2.append(",password=");
							_sbWithNtlmv2.append(password);
						} else {
							_sb.append("credentials=/etc/cifspw/");
							_sb.append(getNameFromMountPath(getExternalMountPath(server, share)));
							
							_sbWithNtlmv2.append("credentials=/etc/cifspw/");
							_sbWithNtlmv2.append(getNameFromMountPath(getExternalMountPath(server, share)));
						}
						_sb.append(",file_mode=0777,dir_mode=0777,user_xattr,acl");
						_sbWithNtlmv2.append(",file_mode=0777,dir_mode=0777,user_xattr,acl");
					}
					break;
			}
			try {
				Command.systemCommand(_sb.toString());
			} catch(Exception _ex) {
				if (_sbWithNtlmv2 != null) {
					try {
						Command.systemCommand(_sbWithNtlmv2.toString());		
					} catch (Exception ex) {
						if (new File(getExternalMountPath(server, share)).exists()) {
							try {
								new File(getExternalMountPath(server, share)).delete();
							} catch (Exception ex2){}
						}
						throw new Exception("cannot mount external share. Ex: "+_ex.getMessage());
					}
				} else {
					if (new File(getExternalMountPath(server, share)).exists()) {
						try {
							new File(getExternalMountPath(server, share)).delete();
						} catch (Exception ex2){}
					}
					throw new Exception("cannot mount external share. Ex: "+_ex.getMessage());
				}
				
			}
		}
	}
	
	public static void mountExternalShare(String server, String share) throws Exception {
		for(String[] _values : readFstab().values()) {
			if(_values[1].equals(getExternalMountPath(server, share))) {
				if("cifs".equals(_values[2])) {
					mountExternalShare(CIFS, server, share, null, null);
					return;
				} else if("nfs".equals(_values[2])) {
					mountExternalShare(NFS, server, share, null, null);
					return;
				}
			}
		}
		throw new Exception("external share not previously defined");
	}
	
	public static void umountExternalShare(String server, String share) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/umount -l ");
		_sb.append(getExternalMountPath(server, share));
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			throw new Exception("cannot umount external share");
		}
	}
	
	public static void umountExternalShare(String path) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/umount -l ");
		_sb.append(path);
		try {
			Command.systemCommand(_sb.toString());
		} catch(Exception _ex) {
			throw new Exception("cannot umount external share");
		}
	}
	
	public void removeExternalShare(String server, String share, boolean forceRemove) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		String _path = getExternalMountPath(server, share);
		/*
		 * TODO: uncomment with bacula
		 * if (!forceRemove) {
			try {
				DBConnection connection = new DBConnectionManager(this._c).getConnection();
				for(Map<String, Object> row : connection.query("SELECT g.name FROM storage AS g WHERE g.name <> '" + this._c.getProperty("bacula.defaultStorage") + "'")) {
					if(BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-sd.conf", "Device", String.valueOf(row.get("name")), "Archive Device").contains(_path)) {
						throw new Exception("volume are currently used by storage [" + row.get("name") + "]");
					}
				}
			} catch(DBException _ex){
				throw new DBException("cannot verify the asociated storages");
			}
		}*/
			
		if(isExternalShareMounted(server, share)) {
			umountExternalShare(server, share);
		}
		
		removeShareFromFstab(_path);
		File _dir = new File(_path); 
	    if(_dir.exists() && !isExternalShareMounted(server, share)) {
	    	_dir.delete();
	    }
	    removeCIFSCredentials(server, share);
	}
	
	public static void removeShare(String group, String volume, int type) {
		switch(type) {
			case NFS:{
					try {
						String _path = VolumeManager.getMountPath(group, volume);
						List<Map<String, String>> _shares = new ArrayList<Map<String,String>>();
						for(Map<String, String> _share : getShares(NFS)) {
							if(!_path.equals(_share.get("path"))) {
								_shares.add(_share);
							}
						}
						writeShares(NFS, _shares);
					} catch(Exception _ex) {}
				}
				break;
			case CIFS:{
					try {
						String _path = VolumeManager.getMountPath(group, volume);
						List<Map<String, String>> _shares = new ArrayList<Map<String,String>>();
						for(Map<String, String> _share : getShares(CIFS)) {
							if(!_path.equals(_share.get("path"))) {
								_shares.add(_share);
							}
						}
						writeShares(CIFS, _shares);
					} catch(Exception _ex) {}
				}
				break;
			case FTP: {
					try {
						System.out.println("removeShare: 1");
						FTPManager _ftpm = new FTPManager();
						System.out.println("removeShare: 2");
						_ftpm.removeShare(volume, group);
						System.out.println("removeShare: 3");
						_ftpm.write();
					} catch(Exception _ex) {}
				}
				break;
		}
	}
	
	public static void updateShare(String group, String volume, int type, Map<String, String> attributes) throws Exception {
		switch(type) {
			case NFS: {
					try {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						List<Map<String, String>> _shares = new ArrayList<Map<String,String>>();
						List<Map<String, String>> _tshares = getShares(NFS);
						for(Map<String, String> _tshare : _tshares) {
							if(!_path.equals(_tshare.get("path"))) {
								_shares.add(_tshare);
								continue;
							}
							Map<String, String> _share = new HashMap<String, String>();
							_share.put("path", _path);
							_share.put("fstype", VolumeManager.getLogicalVolumeFS(group, volume));
							if(attributes.get("squash") != null && "true".equals(attributes.get("squash"))) {
								_share.put("squash", "true");
							}
							if(attributes.get("async") != null && "true".equals(attributes.get("async"))) {
								_share.put("async", "true");
							}
							if(attributes.get("address") != null) {
								_share.put("address", attributes.get("address"));
							}
							_share.put("fstype", VolumeManager.getLogicalVolumeFS(group, volume));
							_shares.add(_share);
						}
						writeShares(NFS, _shares);
					} catch(Exception _ex) {}
				}
				break;
			case CIFS: {
					try {
						String _path = VolumeManager.getLogicalVolumeMountPath(group, volume);
						List<Map<String, String>> _shares = new ArrayList<Map<String,String>>();
						List<Map<String, String>> _tshares = getShares(CIFS);
						for(Map<String, String> _tshare : _tshares) {
							if(!_path.equals(_tshare.get("path"))) {
								_shares.add(_tshare);
								continue;
							}
							
							Map<String, String> _share = new HashMap<String, String>();
							if(attributes.containsKey("name") && attributes.get("name") != null) {
								if(!attributes.get("name").matches("^\\w+[\\-\\_\\w]*$")) {
									throw new Exception("invalid share name format");
								}
								_share.put("name", attributes.get("name"));
							} else {
								_share.put("name", group + "-" + volume);
							}
							_share.put("path", _path);
							if(attributes.get("recycle") != null && "true".equals(attributes.get("recycle"))) {
								_share.put("recycle", "true");
							}
							_share.put("fstype", VolumeManager.getLogicalVolumeFS(group, volume));
							_shares.add(_share);
						}
						writeShares(CIFS, _shares);
					} catch(Exception _ex) {}
				}
				break;
			case FTP: {
					try {	
						boolean anonymous = false;
						FTPManager _ftpm = new FTPManager();
						_ftpm.updateShare(volume, group, anonymous);
						_ftpm.write();
					} catch(Exception _ex) {}
				}
				break;
		}
	}
	
	public static void storeShare(String group, String volume, int type, Map<String, String> attributes) throws Exception {
		if(isShare(type, group, volume)) {
			updateShare(group, volume, type, attributes);
		} else {
			addShare(group, volume, type, attributes);
		}
	}
	
	private static void writeShares(int type, List<Map<String, String>> shares) throws Exception {
		switch(type) {
			case NFS: {
					int _fsid = 1;
					for(Map<String, String> share : shares) {
						if(share.containsKey("fsid")) {
							try {
								int _value = Integer.parseInt(share.get("fsid"));
								if(_fsid <= _value) {
									_fsid = _value + 1;
								}
							} catch(NumberFormatException _ex) {}
						}
					}
					StringBuilder _sb = new StringBuilder();
					for(Map<String, String> share : shares) {
						StringBuilder _options = new StringBuilder();
						_options.append("(rw");
						if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, share.get("fstype"))) {
							_options.append(",fsid=");
							if(share.containsKey("fsid")) {
								try {
									_options.append(String.valueOf(Integer.parseInt(share.get("fsid"))));
								} catch(NumberFormatException _ex) {
									_options.append(_fsid);
									_fsid++;
								}
							} else {
								_options.append(_fsid);
								_fsid++;
							}
						}
						if(share.containsKey("async") && "true".equals(share.get("async"))) {
							_options.append(",async");
						} else {
							_options.append(",sync");
						}
						if(share.containsKey("squash") && "true".equals(share.get("squash"))) {
							_options.append(",no_root_squash");
						}
						_options.append(",no_subtree_check,crossmnt)");
						
						_sb.append(share.get("path"));
						if(share.containsKey("address")) {
							StringTokenizer _st = new StringTokenizer(share.get("address"), ",");
							while(_st.hasMoreTokens()) {
								_sb.append(" ");
								_sb.append(_st.nextToken());
								_sb.append(_options.toString());
							}
						} else {
							_sb.append(" *");
							_sb.append(_options.toString());
						}
						_sb.append("\n");
					}
					
					File _f = new File(WBSAirbackConfiguration.getFileShareNfs());
					FileLock _fl = new FileLock(_f);
					try {
						_fl.lock();
						FileOutputStream _fos = new FileOutputStream(_f, false);
						try {
							_fos.write(_sb.toString().getBytes());
						} finally {
							_fos.close();
						}
					} finally {
						_fl.unlock();
					}
					ServiceManager.restart(ServiceManager.NFS);
				}
				break;
			case CIFS: {
					StringBuilder _sb = new StringBuilder();
					for(Map<String, String> share : shares) {
						if(share.get("name") == null || share.get("path") == null) {
							continue;
						}
						_sb.append("[");
						_sb.append(share.get("name"));
						_sb.append("]\n");
						_sb.append("\tpath = ");
						_sb.append(share.get("path"));
						_sb.append("\n");
						if(share.get("description") != null) {
							_sb.append("\tcomment = ");
							_sb.append(share.get("description"));
							_sb.append("\n");
						}
						_sb.append("\tread only = no\n");
						_sb.append("\tbrowseable = yes\n");
						_sb.append("\tforce create mode = 0664\n");
						_sb.append("\tforce directory mode = 0775\n");
						_sb.append("\twriteable = yes\n");
						if(share.get("recycle") != null && "true".equals(share.get("recycle"))) {
							if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_XFS, share.get("fstype"))) {
								_sb.append("\tvfs objects = recycle\n");
								_sb.append("\trecycle:repository = .recycle/%U\n");
								_sb.append("\trecycle:touch = yes\n");
							} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, share.get("fstype")) ||
									FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, share.get("fstype"))) {
								_sb.append("\tvfs objects = shadow_copy2 recycle\n");
								_sb.append("\tshadow: snapdir = .snapshots\n");
								_sb.append("\tshadow: sort = desc\n");
								_sb.append("\trecycle:repository = .recycle/%U\n");
								_sb.append("\trecycle:touch = yes\n");
							}
						} else if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, share.get("fstype")) ||
								FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, share.get("fstype"))) {
							_sb.append("\tvfs objects = shadow_copy2\n");
							_sb.append("\tshadow: snapdir = .snapshots\n");
							_sb.append("\tshadow: sort = desc\n");
						}
						_sb.append("\n");
					}
					
					File _f = new File(WBSAirbackConfiguration.getFileShareCifs());
					FileLock _fl = new FileLock(_f);
					try {
						_fl.lock();
						FileOutputStream _fos = new FileOutputStream(_f, false);
						try {
							_fos.write(_sb.toString().getBytes());
						} finally {
							_fos.close();
						}
					} finally {
						_fl.unlock();
					}
					ServiceManager.restart(ServiceManager.SAMBA);
				}
				break;
		}
	}
	
	private static void addNFSShareToFstab(String server, String share) throws Exception {
		StringBuilder _name = new StringBuilder();
		Map<String, String[]> _mounts = readFstab();
		_name.append(server);
		_name.append(":");
		_name.append(share);
		if(!_mounts.containsKey(_name.toString())) {
			List<String> _values = new ArrayList<String>();
			_values.add(_name.toString());
			_values.add(getExternalMountPath(server, share));
			_values.add("nfs");
			_values.add("rw,hard,intr,rsize=8192,wsize=8192");
			_values.add("0");
			_values.add("0");
			_mounts.put(_values.get(0), _values.toArray(new String[] {}));
			writeFstab(_mounts);
		}
	}
	
	private static void addCIFSCredentials(String server, String share, String user, String password) throws Exception {
		String _name = getNameFromMountPath(getExternalMountPath(server, share));
		StringBuilder _sb = new StringBuilder();
		_sb = new StringBuilder();
		_sb.append("username=");
		_sb.append(user);
		_sb.append("\npassword=");
		_sb.append(password);
		_sb.append("\n");
		
		File _f = new File("/etc/cifspw");
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		_name = "/etc/cifspw/".concat(_name);
		if (_name.trim().endsWith("/"))
			_name=_name.trim().substring(0, _name.length()-1);
		_f = new File(_name.substring(0, _name.lastIndexOf("/")));
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		FileOutputStream _fos = new FileOutputStream(_name);
		try {
			_fos.write(_sb.toString().getBytes());
		} finally {
			_fos.close();
		}
		Command.systemCommand("/bin/chmod 600 " + _name);
	}
	
	private static void removeCIFSCredentials(String server, String share) throws Exception {
		String _name = getNameFromMountPath(getExternalMountPath(server, share));
		File _f = new File("/etc/cifspw/" + _name);
		if(_f.exists()) {
			_f.delete();
		}
	}
	
	private static void addCIFSShareToFstab(String server, String share, String user, String password, String domain) throws Exception {
		String _name = getNameFromMountPath(getExternalMountPath(server, share));
		
		StringBuilder _sb = new StringBuilder();
		StringBuilder _path = new StringBuilder();
		Map<String, String[]> _mounts = readFstab();
		_path.append("//");
		if (server.endsWith("/"))
			server = server.substring(0, server.length()-1);
		_path.append(server);
		if (!share.startsWith("/"))
			_path.append("/");
		_path.append(share);
		if(!_mounts.containsKey(_name.toString())) {
			_sb = new StringBuilder();
			List<String> _values = new ArrayList<String>();
			_sb.append("rw,credentials=/etc/cifspw/");
			_sb.append(_name);
			if(domain != null && !domain.isEmpty()) {
				_sb.append(",domain=");
				_sb.append(domain);
			}
			_sb.append(",file_mode=0777,dir_mode=0777,user_xattr,acl");
			_values.add(_path.toString());
			_values.add(getExternalMountPath(server, share));
			_values.add("cifs");
			_values.add(_sb.toString());
			_values.add("0");
			_values.add("0");
			_mounts.put(_values.get(0), _values.toArray(new String[] {}));
			writeFstab(_mounts);
		}
	}
	
	private static void removeShareFromFstab(String path) throws Exception {
		Map<String, String[]> _mounts = readFstab();
		if(_mounts.containsKey(path)) {
			_mounts.remove(path);
			writeFstab(_mounts);
		}
	}
	
	private static Map<String, String[]> readFstab() {
		Map<String, String[]> _mounts = new HashMap<String, String[]>();
		File _f = new File(WBSAirbackConfiguration.getFileFstab());
		if(_f.exists()) {
			try {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						String[] _value = _line.split("(?<!\\\\) +");
						_mounts.put(_value[1], _value);
					}
				} finally {
					_br.close();
				}
			} catch(Exception _ex) {}
		}
		return _mounts;
	}
	
	private static void writeFstab(Map<String, String[]> mounts) throws Exception {
		File _f = new File(WBSAirbackConfiguration.getFileFstab());
		FileLock _fl = new FileLock(_f);
		
		FileOutputStream _fos = new FileOutputStream(_f);
		try {
			_fl.lock();
			for(String[] _mount : mounts.values()) {
				for(String _value : _mount) {
					_fos.write(_value.getBytes());
					_fos.write(" ".getBytes());
				}
				_fos.write("\n".getBytes());
			}
		} finally {
			_fl.unlock();
			_fos.close();
		}
	}
	
	private static String getExternalMountPath(String server, String share) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(WBSAirbackConfiguration.getDirectoryVolumeMount());
		_sb.append("/shares/");
		if (server.endsWith("/"))
			server = server.substring(0, server.length()-1);
		_sb.append(server);
		if(!share.trim().startsWith("/")) {
			_sb.append("/");
		}
		_sb.append(share);
		return _sb.toString();
	}
	
	@SuppressWarnings("unused")
	private static String getExternalMountServerPath(String server) {
		StringBuilder _sb = new StringBuilder();
		_sb.append(WBSAirbackConfiguration.getDirectoryVolumeMount());
		_sb.append("/shares/");
		_sb.append(server);
		return _sb.toString();
	}
	
	public static void startAllShareServices(){
		try {
			Command.systemCommand("/etc/init.d/nfs-common stop");
			Command.systemCommand("/etc/init.d/nfs-common start");
			ServiceManager.start(ServiceManager.NFS);
			ServiceManager.start(ServiceManager.SAMBA);
		} catch (Exception ex) {
			logger.error("ERROR Mientras se iniciaban los servicios NFS: "+ex.getMessage());
		}
	}
	
	public static void stopAllShareServices(){
		try {
			Command.systemCommand("/etc/init.d/nfs-common stop");
			ServiceManager.fullStop(ServiceManager.NFS);
			ServiceManager.fullStop(ServiceManager.SAMBA);
		} catch (Exception ex) {
			logger.error("ERROR Mientras se paraban los servicios NFS: "+ex.getMessage());
		}
	}
}
