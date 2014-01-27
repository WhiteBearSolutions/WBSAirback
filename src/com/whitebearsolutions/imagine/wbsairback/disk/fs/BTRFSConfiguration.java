package com.whitebearsolutions.imagine.wbsairback.disk.fs;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.util.Command;

public class BTRFSConfiguration {
	public static List<String> SUPPORTED_COMPRESSION;
	
	static {
		SUPPORTED_COMPRESSION = new ArrayList<String>();
		SUPPORTED_COMPRESSION.add("lzo");
	}
	
	public static void createFileSystem(String group, String name, String compression, boolean encryption) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/mkfs.btrfs ");
		_sb.append(VolumeManager.getLogicalVolumeDevicePath(group, name));
		Command.systemCommand(_sb.toString());
	}
	
	public static void createFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/btrfs subvolume snapshot ");
		_sb.append(VolumeManager.getLogicalVolumeMountPath(group, name));
		_sb.append(" ");
		_sb.append(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot));
		Command.systemCommand(_sb.toString());
	}
	
	public static Map<String, String> getFileSystemParameters(String group, String name) throws Exception {
		Map<String, String> _parameters = new HashMap<String, String>();
		_parameters.putAll(getFileSystemDataUsed(VolumeManager.getLogicalVolumeMountPath(group, name)));
		return _parameters;
	}
	
	public static List<Map<String, String>> getFileSystemSnapshots(String group, String name) throws Exception {
		List<Map<String, String>> _snapshots = new ArrayList<Map<String,String>>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/btrfs subvolume list ");
		_sb.append(VolumeManager.getLogicalVolumeMountPath(group, name));
		List<String> _hourly_snapshots = VolumeManager.getLogicalVolumeSnapshotIndexes(VolumeManager.LV_SNAPSHOT_HOURLY, group, name);
		List<String> _daily_snapshots = VolumeManager.getLogicalVolumeSnapshotIndexes(VolumeManager.LV_SNAPSHOT_DAILY, group, name);
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null) {
				BufferedReader _br = new BufferedReader(new StringReader(_output));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						Map<String, String> _snapshot = new HashMap<String, String>();
						_snapshot.put("name", _line.substring(_line.indexOf(" path ") + 6));
						if(_snapshot.get("name").startsWith(".snapshots/@")) {
							_snapshot.put("name", _snapshot.get("name").substring(12));
							_snapshot.putAll(getFileSystemSnapshotDataUsed(VolumeManager.getLogicalVolumeMountPath(group, name)));
							if(_hourly_snapshots.contains(_snapshot.get("name"))) {
								_snapshot.put("schedule", "H");
							} else if(_daily_snapshots.contains(_snapshot.get("name"))) {
								_snapshot.put("schedule", "D");
							} else {
								_snapshot.put("schedule", "M");
							}
							_snapshots.add(_snapshot);
						}
					}
				} finally {
					_br.close();
				}
			}
		} catch(Exception _ex) {
			System.out.println("BTRFS::getFileSystemSnapshot error - " + _ex.getMessage());
		}
		return _snapshots;
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
	
	private static Map<String, String> getFileSystemDataUsed(String path) throws Exception {
		Map<String, String> _data = new HashMap<String, String>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/btrfs filesystem df ");
		_sb.append(path);
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null) {
				Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(path);
				double _size = VolumeManager.getLogicalVolumeSize(_lv.get("vg"), _lv.get("lv")) * 1024;
				_data.put("size-raw", getFormattedPlain(_size));
				_data.put("size", getFormattedSize(_size));
				BufferedReader _br = new BufferedReader(new StringReader(_output));
				try {
					double _used = 0;
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(_line.startsWith("Data+Metadata: ")) {
							try {
								StringTokenizer _st = new StringTokenizer(_line);
								while(_st.hasMoreTokens()) {
									String _value = _st.nextToken();
									if(_value.startsWith("total=")) {
										_data.put("reservation", _value.substring(_value.indexOf("=") + 1));
									} else if(_value.startsWith("used=")) {
										_used = getSize(_value.substring(_value.indexOf("=") + 1));
									}
								}
							} catch(Exception _ex) {
								System.out.println("BTRFSConfiguration::getFileSystemDataUsed:error - " + _ex.toString());
							}
							break;
						} else if(_line.startsWith("Data: ")) {
							try {
								StringTokenizer _st = new StringTokenizer(_line);
								while(_st.hasMoreTokens()) {
									String _value = _st.nextToken();
									if(_value.startsWith("total=")) {
										_data.put("reservation", _value.substring(_value.indexOf("=") + 1));
									} else if(_value.startsWith("used=")) {
										_used += getSize(_value.substring(_value.indexOf("=") + 1));
									}
								}
							} catch(Exception _ex) {
								System.out.println("BTRFSConfiguration::getFileSystemDataUsed:error - " + _ex.toString());
							}
						} else if(_line.startsWith("Metadata: ")) {
							try {
								StringTokenizer _st = new StringTokenizer(_line);
								while(_st.hasMoreTokens()) {
									String _value = _st.nextToken();
									if(_value.startsWith("used=")) {
										_used += getSize(_value.substring(_value.indexOf("=") + 1));
									}
								}
							} catch(Exception _ex) {
								System.out.println("BTRFSConfiguration::getFileSystemDataUsed:error - " + _ex.toString());
							}
						}
					}
					_data.put("used", getFormattedPlain(_used * 100 / _size));
					_data.put("free-raw", getFormattedPlain(_size - _used));
					_data.put("free", getFormattedSize(_size - _used));
				} finally {
					_br.close();
				}
			}
		} catch(Exception _ex) {}
		return _data;
	}
	
	private static Map<String, String> getFileSystemSnapshotDataUsed(String path) throws Exception {
		Map<String, String> _data = new HashMap<String, String>();
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/btrfs filesystem df ");
		_sb.append(path);
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null) {
				BufferedReader _br = new BufferedReader(new StringReader(_output));
				try {
					for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
						if(_line.startsWith("Data+Metadata:")) {
							StringTokenizer _st = new StringTokenizer(_line);
							while(_st.hasMoreTokens()) {
								String _value = _st.nextToken();
								if(_value.startsWith("total=")) {
									_data.put("reservation", getFormattedSize(getSize(_value.substring(_value.indexOf("=") + 1))));
								} else if(_value.startsWith("used=")) {
									_data.put("used", getFormattedSize(getSize(_value.substring(_value.indexOf("=") + 1))));
								}
							}
							break;
						}
					}
				} finally {
					_br.close();
				}
			}
		} catch(Exception _ex) {}
		return _data;
	}
	
	private static double getSize(String value) {
		double _size = 0;
		char unit = 'B';
		if(value == null || value.isEmpty()) {
			return _size;
		}
		if(value.endsWith("KB") ||
				value.endsWith("MB") ||
				value.endsWith("GB") ||
				value.endsWith("TB") ||
				value.endsWith("PB")) {
			unit = value.charAt(value.length() - 2);
			value = value.substring(0, value.length() - 2);
		}
		try {
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
	
	public static boolean isSupportedCompression(String compression) {
		if(compression == null) {
			return false;
		}
		return SUPPORTED_COMPRESSION.contains(compression);
	}
	
	public static void removeFileSystem(String group, String name) throws Exception {
		/**
		 * TODO
		 */
	}
	
	public static void removeFileSystemSnapshot(String group, String name, String snapshot) throws Exception {
		if(snapshot == null || snapshot.isEmpty()) {
			throw new Exception("invalid snapshot name");
		}
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/btrfs subvolume delete ");
		_sb.append(VolumeManager.getLogicalVolumeSnapshotMountPath(group, name, snapshot).replace("@", "\\@"));
		Command.systemCommand(_sb.toString());
	}
	
	public static void resizeFileSystem(String group, String name, double size) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/btrfs filesystem resize ");
		if(size > 0) {
			_sb.append("max");
		} else {
			_sb.append(getFormattedPlain(size));
		}
		_sb.append(" ");
		_sb.append(VolumeManager.getMountPath(group, name));
		Command.systemCommand(_sb.toString());
	}
}
