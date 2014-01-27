package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.imagine.wbsairback.util.VolumeDaemon;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.io.FileLockAlreadyLockedException;
import com.whitebearsolutions.security.PasswordHandler;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class GeneralSystemConfiguration {
	private Configuration _c;
	private Configuration _replicatedHaConf;
	
	private final static Logger logger = LoggerFactory.getLogger(GeneralSystemConfiguration.class);
	
	public static final String BACULA_MAIL_LEVEL_ALL = "all";
	public static final String BACULA_MAIL_LEVEL_ONLY_ERROR = "only_error";
	public static final String BACULA_MAIL_LEVEL_ONLY_OK = "only_ok";
	
	public GeneralSystemConfiguration() throws Exception {
		this._c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		File f = new File(WBSAirbackConfiguration.getFileReplicationConfiguration());
		if (f.exists())
			this._replicatedHaConf = new Configuration(f);
		else {
			this._replicatedHaConf = this._c;
			try {
				Command.systemCommand("cp -f "+WBSAirbackConfiguration.getFileConfiguration()+" "+WBSAirbackConfiguration.getFileReplicationConfiguration());
			} catch (Exception ex){}
		}
	}
	
	public void setRootPassword(String password) throws Exception {
		if(password != null && !password.isEmpty()) {
			this._c.setProperty("system.password", PasswordHandler.generateDigest(password, null, "MD5"));
			this._c.store();
		}
	}
	
	public String getMailServer() {
		if(this._replicatedHaConf.hasProperty("system.mail.host")) {
			return this._replicatedHaConf.getProperty("system.mail.host");
		}
		return "";
	}
	
	public String getMailFromAccount() {
		if(this._replicatedHaConf.hasProperty("system.mail.from")) {
			return this._replicatedHaConf.getProperty("system.mail.from");
		} else if (getBaculaMailAccount() != null && !getBaculaMailAccount().isEmpty()) {
			return getBaculaMailAccount();
		}
		return "";
	}
	
	public String getMailReportAccount() {
		if(this._replicatedHaConf.hasProperty("system.mail.report")) {
			return this._replicatedHaConf.getProperty("system.mail.report");
		} else if (getBaculaMailAccount() != null && !getBaculaMailAccount().isEmpty()) {
			return getBaculaMailAccount();
		}
		return "";
	}
	
	public String getSnmpTrapServer() {
		if(this._c.hasProperty("system.snmp.trap.host")) {
			return this._c.getProperty("system.snmp.trap.host");
		}
		return "";
	}
	
	public boolean isRemoteSupportActive() {
		if(this._c.hasProperty("system.remotesupport.activate") && this._c.getProperty("system.remotesupport.activate").equals("true")) {
			return true;
		}
		return false;
	}
	
	public void setRemoteSupportActive(boolean value) throws Exception {
		if (value == true)
			this._c.setProperty("system.remotesupport.activate", "true");
		else
			this._c.setProperty("system.remotesupport.activate", "false");
		this._c.store();
	}
	
	public String getSnmpTrapVersion() {
		if(this._c.hasProperty("system.snmp.trap.version")) {
			return this._c.getProperty("system.snmp.trap.version");
		}
		return "";
	}
	
	public String getSnmpTrapMemory() {
		if(this._c.hasProperty("system.snmp.trap.memory")) {
			return this._c.getProperty("system.snmp.trap.memory");
		}
		return "";
	}
	
	public String getBaculaMailAccount() {
		try {
			if (!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Messages", "Standard", "mail on error").isEmpty())
				return BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Messages", "Standard", "mail on error");
			else if (!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Messages", "Standard", "mail on success").isEmpty())
				return BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Messages", "Standard", "mail on success");
			else
				return BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Messages", "Standard", "mail");
		} catch(Exception _ex) {
			logger.error("Error intentando obtener cuenta para mensajes de bacula. Ex: {}", _ex.getMessage());
			return "";
		}
	}
	
	public static String getBaculaMailLevel() {
		try {
			if (!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Messages", "Standard", "mail on error").isEmpty())
				return BACULA_MAIL_LEVEL_ONLY_ERROR;
			else if (!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Messages", "Standard", "mail on success").isEmpty())
				return BACULA_MAIL_LEVEL_ONLY_OK;
			else
				return BACULA_MAIL_LEVEL_ALL;
		} catch (Exception ex) {
			logger.error("Error intentando obtener el nivel de mensajes de bacula. Ex: {}", ex.getMessage());
			return BACULA_MAIL_LEVEL_ALL;
		}
	}
	
	public Map<String, String> getLDAPDirectory() throws Exception {
		Map<String, String> _values = new HashMap<String, String>();
		if(this._c.hasProperty("ldap.host")) {
			_values.put("server", this._c.getProperty("ldap.host"));
			if(this._c.hasProperty("ldap.port")) {
				_values.put("port", this._c.getProperty("ldap.port"));
			} else {
				_values.put("port", "");
			}
			if(this._c.hasProperty("ldap.basedn")) {
				_values.put("basedn", this._c.getProperty("ldap.basedn"));
			} else {
				_values.put("basedn", "");
			}
			if(this._c.hasProperty("ldap.auth.userID")) {
				_values.put("attribute", this._c.getProperty("ldap.auth.userID"));
			} else {
				_values.put("attribute", "");
			}
		} else {
			_values.put("server", "");
			_values.put("port", "");
			_values.put("basedn", "");
			_values.put("attribute", "");
		}
		return _values;
	}
	
	public static Map<String, Integer> getDataBaseConfiguration() throws Exception {
		Map<String, Integer> _data = new HashMap<String, Integer>();
		File _f = new File("/etc/postgresql/" + getDataBaseVersion() + "/main/postgresql.conf");
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
		try {
			for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
	        	if(_line.trim().startsWith("max_connections =")) {
	        		_line = _line.substring(_line.indexOf("=") + 1).trim();
	        		try {
	        			_data.put("max_connections", Integer.parseInt(_line));
	        		} catch(NumberFormatException _ex) {
	        			_data.put("max_connections", 500);
	        		}
	            } else if(_line.trim().startsWith("shared_buffers =")) {
	            	_line = _line.substring(_line.indexOf("=") + 1).trim();
	            	try {
	        			_data.put("shared_buffers", (int) Math.round(getSize(_line) / 1048576D));
	        		} catch(NumberFormatException _ex) {
	        			_data.put("shared_buffers", 0);
	        		}
	            } else if(_line.trim().startsWith("effective_cache_size =")) {
	            	_line = _line.substring(_line.indexOf("=") + 1).trim();
	            	try {
	        			_data.put("cache", (int) Math.round(getSize(_line) / 1048576D));
	        		} catch(NumberFormatException _ex) {
	        			_data.put("cache", 0);
	        		}
	            }
	        }
		} finally {
			_br.close();
		}
		if(_data.get("shared_buffers") == null) {
			_data.put("shared_buffers", 0);
		}
		if(_data.get("cache") == null) {
			_data.put("cache", 0);
		}
		return _data;
	}
	
	public static int getDataBaseRecommendedCache() {
		return (int) Math.round(getDataBaseRecommendedInternalCache() / 1048576D);
	}
	
	public static double getRecomendedSharedMemory() {
		double _memory = getSystemMemory();
		double min = 268435456d;
		logger.debug("Memory: {}", _memory);
		if(_memory > 0) {
			double limit = 4d*1024d*1024d*1024d;
			double shared = _memory*0.5d;
			if (shared > limit)
				shared = limit;
			if (shared < min)
				shared = min;
			logger.debug("Shared memory: {}", shared);
			return shared;
		} else {
			return min;
		}
	}
	
	private static double getDataBaseRecommendedInternalSharedBuffer() {
		double _memory = getSystemMemory();
		if(_memory > 0) {
			double _limit = 0.8D*getRecomendedSharedMemory();
			double _lowLimit = 33554432D;
			double _sb = _memory / 4.0d;
			if (_memory < 5294967296d) //4GB RAM
				_sb = _memory / 64.0d;
			else if (_memory < 9589934592d) //8GB RAM
				_sb = _memory / 32.0d;
			else if (_memory < 18179869184d) //16GB RAM
				_sb = _memory / 16.0d;
			else if (_memory < 35359738368d) //32GB RAM
				_sb = _memory / 8.0d;
			if (_sb > _limit)
				_sb = _limit;
			if (_sb < _lowLimit)
				_sb = _lowLimit;
			logger.debug("Shared buffer: {}", _sb);
			return _sb;
		} else {
			return 33554432D;
		}
	}
	
	private static double getDataBaseRecommendedInternalWalBuffers(double shared_buffer) {
		double _walbuffers = shared_buffer / 32.0d;
		double _lowLimit = 2097152d;
		if (_walbuffers < _lowLimit)
			_walbuffers = _lowLimit;
		logger.debug("Wal_buffer: {}", _walbuffers);
		return _walbuffers;
	}
	
	private static double getDataBaseRecommendedInternalCache() {
		double _memory = getSystemMemory();
		if(_memory > 0) {
			return (_memory / 4D);
		} else {
			return 209715200;
		}
	}
	
	public static int getDataBaseRecommendedSharedBuffer() {
		return (int) Math.round(getDataBaseRecommendedInternalSharedBuffer() / 1048576D);
	}
	
	public static String getDataBaseVersion() {
		double _postgres_version = 8.4;
		File _postgres_dir = new File("/etc/postgresql");
		for(File _f : _postgres_dir.listFiles()) {
			if(_f.isDirectory()) {
				try {
					_postgres_version = Double.parseDouble(_f.getName());
				} catch(NumberFormatException _ex) {}
			}
		}
		return String.valueOf(_postgres_version);
	}
	
	public static Map<String, Integer> getDiskLoad() throws Exception {
		Map<String, Integer> partitions = new HashMap<String, Integer>();
		try {
			String _output = null;
			try {
				_output = Command.systemCommand("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" df -P");
			} catch (Exception ex) {
				logger.error("Atención, se produjo un error en df -P. Aún así, recojemos la salida");
				_output = Command.systemCommandOutputOnError("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" df -lP");
			}
			if(_output != null && !_output.isEmpty()) {
				List<String> devices = new ArrayList<String>();
				File _f = new File("/proc/partitions");
				if(_f.exists()) {
					FileInputStream _fis = null;
					try {
						_fis = new FileInputStream(_f);
						for(String _line = readLine(_fis); _line != null; _line = readLine(_fis)) {
							if(_line.matches("\\ +[0-9]+\\ +[0-9]+\\ +[0-9]+\\ +[a-zA-Z0-9]+")) {
								if (!_line.contains("@")) {
									StringTokenizer _st = new StringTokenizer(_line.trim());
									for(int i = 3; i > 0; i--) {
										_st.nextToken();
									}
									devices.add(_st.nextToken());
								}
							}
						}
					} catch (Exception ex) {
						logger.error("Error leyendo /proc/partitions. Ex: {}", ex.getMessage());
					} finally {
						if(_fis != null) {
							_fis.close();
						}
					}
				}
				for(String _line : _output.split("\n")) {
					try {
						if (!_line.contains("@")) {
							if(_line.contains(WBSAirbackConfiguration.getDirectoryVolumeMount()) && !_line.contains("df:") && !_line.contains("No ")) {
								StringTokenizer _st = new StringTokenizer(_line, " ");
								String percent = _st.nextToken();
								while (!percent.contains("%"))
									percent = _st.nextToken();
								String _partition = _st.nextToken();
								try {
									int _t = Integer.parseInt(percent.replace("%", ""));
									partitions.put(_partition, _t);
								} catch(NumberFormatException _ex) {
									if(_partition != null) {
										partitions.put(_partition, 0);								
									}
								}
							} else {
								for(String device : devices) {
									if(_line.contains(device)) {
										StringTokenizer _st = new StringTokenizer(_line, " ");
										String _partition = _st.nextToken();
										String percent = _st.nextToken();
										while (!percent.contains("%"))
											percent = _st.nextToken();
										try {
											partitions.put(_partition, Integer.parseInt(percent.replace("%", "")));
										} catch(NumberFormatException _ex) {
											partitions.put(_partition, 0);
										}
										break;
									}
								}
							}
						}
					} catch(Exception _ex) {
						logger.error("Error interpretando output de df -P. Ex: {}", _ex.getMessage());
					}
				}
			}
		} catch(Exception _ex) {
			if(_ex.getMessage() == null || _ex.getMessage().trim().isEmpty()) {
				logger.error("unknown error while reading system partitions");
				throw new Exception("unknown error while reading system partitions");
			} else {
				logger.error("read partition error - " + _ex.getMessage());
				throw new Exception("read partition error - " + _ex.getMessage());
			}
		}
		return partitions;
	}
	
	public int getExportHour() {
		try {
			if(this._c.getProperty("system.export.hour") != null) {
				return Integer.parseInt(this._c.getProperty("system.export.hour"));
			}
		} catch(NumberFormatException _ex) {}
		return -1;
	}
	
	public int getExportRetention() {
		try {
			if(this._c.getProperty("system.export.retention") != null) {
				return Integer.parseInt(this._c.getProperty("system.export.retention"));
			}
		} catch(NumberFormatException _ex) {}
		return 0;
	}
	
	public String getExportShare() {
		if(this._c.getProperty("system.export.volume") != null) {
			return this._c.getProperty("system.export.volume");
		}
		return null;
	}
	
	private static String getFormattedSize(double size) {
		StringBuilder _sb = new StringBuilder();
		DecimalFormat _df = new DecimalFormat("#");
		_df.setDecimalSeparatorAlwaysShown(false);
		if(size >= 1099511627776D) {
			_sb.append(_df.format(Math.round(size / 1099511627776D)));
			_sb.append("TB");
		} else if(size >= 1073741824D) {
			_sb.append(_df.format(Math.round(size / 1073741824D)));
			_sb.append("GB");
		} else if(size >= 1048576D) {
			_sb.append(_df.format(Math.round(size / 1048576D)));
			_sb.append("MB");
		} else if(size >= 1024D) {
			_sb.append(_df.format(Math.round(size / 1024D)));
			_sb.append("KB");
		} else {
			_sb.append(_df.format(size));
		}
		return _sb.toString();
	}
	
	public static int getMemoryLoad() {
		double memory_total = 0, memory_free = 0, memory_cached = 0, buffers = 0;
		File _proc = new File("/proc/meminfo");
		if(_proc.exists()) {
			FileInputStream _fis = null;
			try {
				_fis = new FileInputStream(_proc);
				for(String _line = readLine(_fis); _line != null; _line = readLine(_fis)) {
					if(_line.contains("MemTotal")) {
						memory_total = Integer.parseInt(_line.substring(9, _line.lastIndexOf(" ")).trim());
					} else if(_line.contains("MemFree")) {
						memory_free = Integer.parseInt(_line.substring(8, _line.lastIndexOf(" ")).trim());
					} else if(_line.contains("Cached") && !_line.contains("SwapCached")) {
						memory_cached = Integer.parseInt(_line.substring(7, _line.lastIndexOf(" ")).trim());
					} else if(_line.contains("Buffers")) {
						buffers = Integer.parseInt(_line.substring(8, _line.lastIndexOf(" ")).trim());
					}
				}
				memory_free = memory_free + memory_cached + buffers;
				double percent = 100 - ((memory_free * 100) / memory_total);
				
				if (percent > 100)
					percent = 100;
				else if (percent < 0)
					percent = 0;
				logger.debug("Memory load {}. Total:{} Free:{} Cached:{} ", new Object[]{percent, memory_total, memory_free, memory_cached});
				return (int) percent;
			} catch(Exception _ex) {
				logger.error("Error al calcular memory load. Total:{} Free:{} Cached:{} Ex: {} ", new Object[]{memory_total, memory_free, memory_cached, _ex});
			} finally {
				if(_fis != null) {
					try {
						_fis.close();
					} catch(IOException _ex) {}
				}
			}
		}
		return -1;
	}
	
	public int getReportHour() {
		try {
			if(this._replicatedHaConf.getProperty("system.report.hour") != null) {
				return Integer.parseInt(this._replicatedHaConf.getProperty("system.report.hour"));
			}
		} catch(NumberFormatException _ex) {}
		return -1;
	}
	
	private static double getSize(String value) {
		double _size = 0;
		if(value == null || value.trim().isEmpty()) {
			return _size;
		}
		value = value.trim();
		if(value.matches("\\d+([KMGTP]B)$")) {
			value = value.substring(0, value.length() -1);
		}
		char unit = value.charAt(value.length() - 1);
		try {
			value = value.substring(0, value.length() - 1);
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
	
	private static double getSystemMemory() {
		File _proc = new File("/proc/meminfo");
		if(_proc.exists()) {
			FileInputStream _fis = null;
			try {
				_fis = new FileInputStream(_proc);
				for(String _line = readLine(_fis); _line != null; _line = readLine(_fis)) {
					if(_line.contains("MemTotal")) {
						return Double.parseDouble(_line.substring(9, _line.lastIndexOf(" ")).trim()) * 1024;
					}
				}
			} catch(Exception _ex) {
			} finally {
				if(_fis != null) {
					try {
						_fis.close();
					} catch(IOException _ex) {}
				}
			}
		}
		return -1;
	}
	
	public static String getVersion() {
		try {
			return Command.systemCommand("/usr/bin/dpkg -l | grep wbsairback-admin | awk '{print $3}'");
		} catch(Exception _ex) {
			return "unknown";
		}
	}
	
	public static void setDataBaseConfiguration(int max_connections, int shared_buffer, int cache) throws Exception {
		double _shared_buffer = shared_buffer * 1048576D;
		double _cache = cache * 1048576D;
		String _postgres_version = getDataBaseVersion();
		StringBuilder _sb = new StringBuilder();
		_sb.append("data_directory = '/rdata/database'\n");
		_sb.append("hba_file = '/etc/postgresql/");
		_sb.append(_postgres_version);
		_sb.append("/main/pg_hba.conf'\n");
		_sb.append("ident_file = '/etc/postgresql/");
		_sb.append(_postgres_version);
		_sb.append("/main/pg_ident.conf'\n");
		_sb.append("external_pid_file = '/var/run/postgresql/");
		_sb.append(_postgres_version);
		_sb.append("-main.pid'\n");
		_sb.append("\n");
		_sb.append("listen_addresses = 'localhost'\n");
		_sb.append("port = 5432\n");
		_sb.append("max_connections = ");
		_sb.append(max_connections);
		_sb.append("\n");
		_sb.append("unix_socket_directory = '/var/run/postgresql'           # (change requires restart)\n");
		_sb.append("\n");
		_sb.append("ssl = true\n");
		_sb.append("\n");
		if(shared_buffer > 0) {
			_sb.append("shared_buffers = ");
			_sb.append(getFormattedSize(_shared_buffer));
		} else {
			shared_buffer = getDataBaseRecommendedSharedBuffer();
			_sb.append("shared_buffers = ");
			_sb.append(getFormattedSize(shared_buffer*1048576D));
		}
		_sb.append("\n");
		if(cache > 0) {
			_sb.append("effective_cache_size = ");
			_sb.append(getFormattedSize(_cache));
		} else {
			_sb.append("effective_cache_size = ");
			_sb.append(getFormattedSize(getDataBaseRecommendedCache()*1048576D));
		}
		_sb.append("\n");
		_sb.append("wal_buffers = "+getFormattedSize(getDataBaseRecommendedInternalWalBuffers(shared_buffer))+"\n"); 
		_sb.append("work_mem = 64MB\n");
		_sb.append("maintenance_work_mem = 256MB\n");
		_sb.append("checkpoint_segments = 64\n"); 
		_sb.append("checkpoint_timeout = 20min\n");
		_sb.append("checkpoint_completion_target = 0.9\n");
		_sb.append("checkpoint_warning = 90s\n");
		_sb.append("log_checkpoints = on\n");
		_sb.append("log_temp_files = 80000\n");
		_sb.append("autovacuum_vacuum_cost_delay = 5ms\n");
		_sb.append("cursor_tuple_fraction = 1.0\n");
		_sb.append("\n");
		_sb.append("#archive_mode = off\n");
		_sb.append("#archive_command = ''\n");
		_sb.append("#archive_timeout = 0\n");
		_sb.append("\n");
		_sb.append("#log_destination = 'stderr'\n");
		_sb.append("#logging_collector = off\n");
		_sb.append("#log_directory = 'pg_log'\n");
		_sb.append("#log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'\n");
		_sb.append("#log_truncate_on_rotation = off\n");
		_sb.append("#log_rotation_age = 1d\n");
		_sb.append("#log_rotation_size = 10MB\n");
		_sb.append("#log_error_verbosity = VERBOSE\n");
		_sb.append("#log_connections = on\n");
		_sb.append("#log_disconnections = on\n");
		_sb.append("#log_duration = on\n");
		_sb.append("#syslog_facility = 'LOCAL0'\n");
		_sb.append("#syslog_ident = 'postgres'\n");
		_sb.append("log_line_prefix = '%t '\n");
		_sb.append("\n");
		_sb.append("datestyle = 'iso, dmy'\n");
		_sb.append("lc_messages = 'es_ES@euro'\n");
		_sb.append("lc_monetary = 'es_ES@euro'\n");
		_sb.append("lc_numeric = 'es_ES@euro'\n");
		_sb.append("lc_time = 'es_ES@euro'\n");
		_sb.append("\n");
		
		try {
			File _f = new File("/etc/postgresql/" + _postgres_version + "/main/postgresql.conf");
			FileSystem.writeFile(_f, _sb.toString());
			
			_sb = new StringBuilder();
			_sb.append("# Database administrative login by UNIX sockets\n");
			_sb.append("local   airback     bacula                            trust\n");
			_sb.append("local   all         postgres                          ident\n");
			_sb.append("\n");
			_sb.append("local   all         all                               ident\n");
			_sb.append("host    all         all         127.0.0.1/32          md5\n");
			_sb.append("host    all         all         ::1/128               md5\n");
			
			_f = new File("/etc/postgresql/" + _postgres_version + "/main/pg_hba.conf");
			FileSystem.writeFile(_f, _sb.toString());
		} catch(Exception _ex) {
			throw new Exception("SystemConfiguration::setDataBaseConfiguration: fail to save database configuration - " + _ex.getMessage());
		}
	}
	
	public void setExportHour(int hour) throws Exception {
		if(hour >= 0) {
			this._c.setProperty("system.export.hour", String.valueOf(hour));
			this._c.store();
		} else if(this._c.hasProperty("system.export.hour")) {
			this._c.removeProperty("system.export.hour");
			this._c.store();
		}
	}
	
	public void setExportRetention(int days) throws Exception {
		if(days >= 0) {
			this._c.setProperty("system.export.retention", String.valueOf(days));
			this._c.store();
		} else if(this._c.hasProperty("system.export.retention")) {
			this._c.removeProperty("system.export.retention");
			this._c.store();
		}
	}
	
	public void setExportShare(String name) throws Exception {
		if(name == null || !name.contains("@")) {
			setExportShare(null, null);
		} else {
			String[] _share = name.split("@");
			setExportShare(_share[0], _share[1]);
		}
	}
	
	public void setExportShare(String server, String share) throws Exception {
		if(server == null || share == null ||
				server.isEmpty() || share.isEmpty()) {
			if(this._c.hasProperty("system.export.volume")) {
				this._c.removeProperty("system.export.volume");
				this._c.store();
			}
		} else {
			Map<String, String> _share = ShareManager.getExternalShare(server, share);
			if(_share.get("name") == null) {
				throw new Exception("external volume does not exists");
			}
			this._c.setProperty("system.export.volume", server + "@" + share);
			this._c.store();
		}
	}
	
	public void setReportHour(int hour) throws Exception {
		if(hour > 0 && hour <= 24) {
			this._replicatedHaConf.setProperty("system.report.hour", String.valueOf(hour));
			this._replicatedHaConf.store();
		} else {
			this._replicatedHaConf.removeProperty("system.report.hour");
		}
	}
	
	public void setMaximumReloadRequests(int reloads) throws Exception {
		if(reloads > 0 && reloads <= 200) {
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Director", "airback-dir", "MaximumReloadRequests", new String[]{ String.valueOf(reloads) });
		} else {
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Director", "airback-dir", "MaximumReloadRequests");
		}
	}
	
	public String getMaximumReloadRequests() throws Exception {
		try {
			return BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Director", "airback-dir", "MaximumReloadRequests");
		} catch (Exception ex) {
			return "";
		}
	}
	
	public void setSnmpTrapMemory(String memory) throws Exception {
		if(memory != null && !memory.isEmpty()) {
			this._c.setProperty("system.snmp.trap.memory", String.valueOf((Integer.parseInt(memory)*1024)));
		} else {
			this._c.removeProperty("system.snmp.trap.memory");
		}
		this._c.store();
	}
	
	public void setSnmpTrapHost(String server, String version) throws Exception {
		try {
			if(server != null && !server.isEmpty()) {
				this._c.setProperty("system.snmp.trap.host", server);
			} else {
				this._c.removeProperty("system.snmp.trap.host");
				this._c.store();
			}
			if(version != null && !version.isEmpty()) {
				this._c.setProperty("system.snmp.trap.version", version);
			} else {
				this._c.removeProperty("system.snmp.trap.version");
				this._c.store();
			}
			this._c.store();
			
			ServiceManager.stop(ServiceManager.SNMP);
			ServiceManager.stop(ServiceManager.SNMPTRAP);
			
			String memory = null;
			if (this._c.hasProperty("system.snmp.trap.memory"))
				memory = this._c.getProperty("system.snmp.trap.memory");
			StringBuilder _s = new  StringBuilder();
			_s.append("com2sec readonly  default         public\n\n");
	
			_s.append("group MyROSystem v1        paranoid\n");
			_s.append("group MyROSystem v2c       paranoid\n");
			_s.append("group MyROSystem usm       paranoid\n");
			_s.append("group MyROGroup v1         readonly\n");
			_s.append("group MyROGroup v2c        readonly\n");
			_s.append("group MyROGroup usm        readonly\n");
			_s.append("group MyRWGroup v1         readwrite\n");
			_s.append("group MyRWGroup v2c        readwrite\n");
			_s.append("group MyRWGroup usm        readwrite\n\n");
	
			_s.append("view all        included        .1\n");
			_s.append("view system included  .1.3.6.1.2.1.1\n\n");
	
			_s.append("access MyROSystem \"\"     any       noauth    exact  system none   none\n");
			_s.append("access MyROGroup \"\"      any       noauth    exact  all    none   none\n");
			_s.append("access MyRWGroup \"\"      any       noauth    exact  all    all    none\n\n");
	
			_s.append("createUser    _internal MD5 \"what does your heart tell you\"\n");
			_s.append("iquerySecName _internal\n");
			_s.append("rouser        _internal\n\n");
	
			_s.append("sysName \"WBSAirback\"\n");
			_s.append("sysDescr \"ImagineWBS/WBSAirback v1.1\"\n");
			_s.append("sysContact \"Soporte ImagineWBS <soporte@wbsgo.com>\"\n\n");
	
			_s.append("load 90\n\n");
	
			_s.append("proc postgres\n");
			_s.append("proc bacula-dir\n");
			_s.append("proc bacula-sd\n");
			_s.append("proc bacula-fd\n");
			_s.append("proc iscsi-scstd\n");
			_s.append("proc smbd\n");
			_s.append("proc java 10 2\n");
			_s.append("procfix java /etc/init.d/wbs-watchdog restart\n\n");
	
			_s.append("disk / 100000\n");
			_s.append("disk /boot 10000\n");
			_s.append("disk /rdata 10%\n\n");
	
			if(server != null && !server.isEmpty()) {
				_s.append("trapcommunity public\n");
				if (version != null && version.equals("1"))
					_s.append("trapsink "+server+"\n");
				else
					_s.append("trap2sink "+server+"\n");
				_s.append("authtrapenable 1\n");
				_s.append("monitor -o dskPath -o dskErrorMsg -S \"Disk almost full\" dskErrorFlag != 0\n");
				_s.append("monitor -o dskPath -o dskErrorMsg -S \"Disk space recovered\" dskErrorFlag == 0\n");
				if (memory != null && !memory.isEmpty()) {
					_s.append("monitor \"Low free memory\" memTotalFree < "+memory+"\n");
				}
				_s.append("monitor -o prErrMessage \"Essential processes are not running\" prErrorFlag != 0\n");
				_s.append("monitor -o laNames -o laLoad -o LaErrMessage \"Hight system load\" laErrorFlag != 0\n");
			}

			FileOutputStream _fos = new FileOutputStream("/etc/snmp/snmpd.conf");
			_fos.write(_s.toString().getBytes());
			_fos.close();
			
			_s = new  StringBuilder();

			_s.append("SNMPDRUN=yes\n");
			_s.append("SNMPDOPTS='-Lsd -Lf /dev/null -u snmp -I -smux -p /var/run/snmpd.pid'\n");
			_s.append("TRAPDRUN=yes\n");
			_s.append("TRAPDOPTS='-Lsd -p /var/run/snmptrapd.pid'\n");
			_s.append("SNMPDCOMPAT=yes\n");
			
			_fos = new FileOutputStream("/etc/default/snmpd");
			_fos.write(_s.toString().getBytes());
			_fos.close();
			
			ServiceManager.start(ServiceManager.SNMP);
			ServiceManager.start(ServiceManager.SNMPTRAP);
		} catch (Exception ex) {
			logger.error("Error en la configuración snmp. Ex:{}",ex.getMessage());
			throw new Exception("Error on snmp configuration. Ex:"+ex.getMessage());
		}
	}
	
	public void setMailServer(String server) throws Exception {
		StringBuilder _s = new  StringBuilder();
		_s.append("smtpd_banner = $myhostname local SMTP\n");
		_s.append("biff = no\n");
		_s.append("append_dot_mydomain = no\n");
		_s.append("readme_directory = no\n");
		_s.append("myhostname = localhost\n");
		_s.append("alias_maps = hash:/etc/aliases\n");
		_s.append("alias_database = hash:/etc/aliases\n");
		_s.append("mydestination = airback, localhost, localhost.localdomain, localhost\n");
		if(server != null && !server.isEmpty()) {
			_s.append("relayhost = " + server + "\n");
		} else {
			_s.append("relayhost = \n");
		}
		_s.append("mynetworks = 127.0.0.0/8\n");
		_s.append("mailbox_size_limit = 0\n");
		_s.append("recipient_delimiter = +\n");
		_s.append("inet_interfaces = 127.0.0.1\n");
		
		FileOutputStream _fos = new FileOutputStream("/etc/postfix/main.cf");
		_fos.write(_s.toString().getBytes());
		_fos.close();
		
		if(server != null && !server.isEmpty()) {
			this._replicatedHaConf.setProperty("system.mail.host", server);
		} else {
			this._replicatedHaConf.removeProperty("system.mail.host");
		}
		this._replicatedHaConf.store();
		
		Command.systemCommand("/etc/init.d/postfix reload");
	}
	
	public void setBaculaMailAccount(String mail, String level) throws Exception {
		this.configureMail(mail, this.getMailFromAccount(), level);
		if(mail != null && !mail.isEmpty()) {
			this._replicatedHaConf.setProperty("system.mail", mail);
		} else if(this._replicatedHaConf.getProperty("system.mail") != null) {
			this._replicatedHaConf.removeProperty("system.mail");
		}
		BackupOperator.reload();
		this._replicatedHaConf.store();
	}
	
	public void setMailFromAccount(String mail) throws Exception {
		if(mail != null && !mail.isEmpty()) {
			this._replicatedHaConf.setProperty("system.mail.from", mail);
		} else if(this._replicatedHaConf.getProperty("system.mail.from") != null) {
			this._replicatedHaConf.removeProperty("system.mail.from");
		}
		this._replicatedHaConf.store();
	}
	
	public void setMailReportAccount(String mail) throws Exception {
		if(mail != null && !mail.isEmpty()) {
			this._replicatedHaConf.setProperty("system.mail.report", mail);
		} else if(this._replicatedHaConf.getProperty("system.mail.report") != null) {
			this._replicatedHaConf.removeProperty("system.mail.report");
		}
		this._replicatedHaConf.store();
	}
	
	public void configureMail(String mail, String fromMail, String level) throws Exception {
		BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Director", "airback-dir", "WorkingDirectory", new String[] { "/rdata/working" });
		if(mail == null || mail.isEmpty()) {
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "mail");
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "operator");
		} else {
			LicenseManager _lm = new LicenseManager();
			StringBuilder _sb = new StringBuilder();
			_sb.append(mail);
			_sb.append(" = all, !skipped");
			String levelMessagesDirective = "mail";
			if (level != null && !level.isEmpty()) {
				if (level.equals(BACULA_MAIL_LEVEL_ONLY_ERROR))
					levelMessagesDirective = "mail on error";
				else if (level.equals(BACULA_MAIL_LEVEL_ONLY_OK))
					levelMessagesDirective = "mail on success";
			}
			
			String from = fromMail;
			if (from == null || from.isEmpty())
				from = "%r";
			
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "mail");
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "mail on error");
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "mail on success");
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Daemon", "mail");
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Daemon", "mail on error");
			BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Daemon", "mail on success");
			
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", levelMessagesDirective, new String[] { _sb.toString() });
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Daemon", levelMessagesDirective, new String[] { _sb.toString() });
			_sb = new StringBuilder();
			_sb.append(mail);
			_sb.append(" = mount");
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "operator", new String[] { _sb.toString() });
			_sb = new StringBuilder();
			_sb.append("\"/opt/bacula/bin/bsmtp -h localhost -f \\\"WBSAirback\\(");
			_sb.append(_lm.getUnitUUID());
			_sb.append("\\) \\<"+from+"\\>\\\" -s \\\" %t %e of %c %l \\(");
			_sb.append(_lm.getUnitUUID());
			_sb.append("\\) \\\" %r\"");
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "mailcommand", new String[] { _sb.toString() });
			_sb = new StringBuilder();
			_sb.append("\"/opt/bacula/bin/bsmtp -h localhost -f \\\"WBSAirback\\(");
			_sb.append(_lm.getUnitUUID());
			_sb.append("\\) \\<"+from+"\\>\\\" -s \\\"");
			_sb.append(" Intervention needed for %j \\(");
			_sb.append(_lm.getUnitUUID());
			_sb.append("\\) \\\" %r\"");
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Standard", "operatorcommand", new String[] { _sb.toString() });
			_sb = new StringBuilder();
			_sb.append("\"/opt/bacula/bin/bsmtp -h localhost -f \\\"WBSAirback\\(");
			_sb.append(_lm.getUnitUUID());
			_sb.append("\\) \\<"+from+"\\>\\\" -s \\\"");
			_sb.append(" Daemon message \\(");
			_sb.append(_lm.getUnitUUID());
			_sb.append("\\) \\\" %r\"");
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Message", "Daemon", "mailcommand", new String[] { _sb.toString() });
		}
	}
	
	public void importConfigutation(String name) throws Exception {
		if(name == null || !name.contains("@")) {
			throw new Exception("invalid share name");
		}
		String[] _share = name.split("@");
		importConfigutation(_share[0], _share[1]);
	}
	
	public void importConfigutation(String server, String share) throws Exception {
		FileLock _fl = new FileLock(new File("/tmp/wbsairback-import.lock"));
		try {
			_fl.lock();
		} catch(Exception _ex) {
			if(_ex instanceof FileLockAlreadyLockedException) {
				throw new Exception("an import process already in progress, please wait to complete before performing any operation");
			} else {
				throw _ex;
			}
		}
		
		try {
			Map<String, String> _share = ShareManager.getExternalShare(server, share);
			if(_share.get("name") == null) {
				throw new Exception("external volume does not exists");
			}
			
			if(!VolumeManager.isSystemMounted(_share.get("path"))) {
				VolumeManager.mountSystemVolume(_share.get("path"));
			}
			
			File _f = new File(_share.get("path") + "/backup-wbsairback.0.tar");
	    	if(!_f.exists()) {
	    		throw new Exception("external volume has not an import file");
	    	}
	    	
	    	try {
	    		Command.systemCommand("/bin/rm -fr /rdata/bacula/* && /bin/rm -fr /rdata/working/*");
	    	} catch(Exception _ex) {
	        	throw new Exception("cannot clean current configuration");
			}
	    	
			try {
	        	Command.systemCommand("/bin/tar fxp " + _f.getAbsolutePath() + " -C /");
	        } catch(Exception _ex) {
	        	throw new Exception("corrupted import source file [" + _f.getName() + "]");
			}
	        Thread.sleep(2000);
	        
	        try {
	        	Command.systemCommand("/bin/su - postgres -c \"/bin/echo 'drop database airback' | psql\"");
	        	Command.systemCommand("/bin/su - postgres -c \"/bin/echo 'create database airback' | psql\"");
	        } catch(Exception _ex) {
	        	throw new Exception("cannot erase old database");
			}
	        
	        try{
	        	Command.systemCommand("/bin/su - postgres -c \"/usr/bin/pg_restore -d airback -F c /rdata/sqlexport/airback.dump\"");
	        	try {
					File _f_dump = new File("/rdata/sqlexport/airback.dump");
					if(_f_dump.exists()) {
						_f_dump.delete();
					}
				} catch(Exception _ex) {}
	        	VolumeDaemon.initBaculaPidFiles();
				ServiceManager.restart(ServiceManager.BACULA_DIR);
	        } catch(Exception _ex) {
	        	throw new Exception("cannot load new database");
			} finally {
				//
			}
		} finally {
			_fl.unlock();
		}
	}
	
	public void exportConfigutation(String name) throws Exception {
		if(name == null || !name.contains("@")) {
			throw new Exception("invalid share name");
		}
		String[] _share = name.split("@");
		exportConfigutation(_share[0], _share[1]);
	}
	
	public void exportConfigutation(String server, String share) throws Exception {
		FileLock _fl = new FileLock(new File("/tmp/wbsairback-export.lock"));
		try {
			_fl.lock();
		} catch(Exception _ex) {
			if(_ex instanceof FileLockAlreadyLockedException) {
				throw new Exception("an export process already in progress, please wait to complete before performing any operation");
			} else {
				throw _ex;
			}
		}
		
		try {
			Map<String, String> _share = ShareManager.getExternalShare(server, share);
			if(_share.get("name") == null) {
				throw new Exception("external volume does not exists");
			}
			
			if(!VolumeManager.isSystemMounted(_share.get("path"))) {
				VolumeManager.mountSystemVolume(_share.get("path"));
			}
			
			File _f;			
			int _retention = getExportRetention();
			if(_retention > 0) {
				while(--_retention >= 0) {
					_f = new File(_share.get("path") + "/backup-wbsairback." + _retention + ".tar");
					if(_f.exists()) {
						if(!_f.canWrite()) {
							throw new Exception("cannot write on share");
						}
					} else {
						try {
							if(!_f.createNewFile()) {
								throw new Exception();
							}
						} catch(Exception _ex) {
							throw new Exception("cannot write on share. Ex: "+_ex.getMessage());
						}
					}
					try {
			        	Command.systemCommand("/bin/mv " + _f.getAbsolutePath() + " " + _share.get("path") + "/backup-wbsairback." + (_retention + 1) + ".tar");
			        } catch(Exception _ex) {
			        	throw new Exception("cannot rename old export. Ex: "+_ex.getMessage());
					}
				}
			} else {
				_f = new File(_share.get("path") + "/backup-wbsairback.0.tar");
				if(_f.exists()) {
					if(!_f.canWrite()) {
						throw new Exception("cannot write on share");
					}
				} else {
					try {
						if(!_f.createNewFile()) {
							throw new Exception();
						}
					} catch(Exception _ex) {
						throw new Exception("cannot write on share. Ex: "+_ex.getMessage());
					}
				}
				try {
		        	Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		        } catch(Exception _ex) {
		        	throw new Exception("cannot remove old export. Ex: "+_ex.getMessage());
				}
			}
			
			_f = new File(_share.get("path") + "/backup-wbsairback.0.tar");
			
			try  {
				Command.systemCommand("mkdir -p /rdata/sqlexport/");
				Command.systemCommand("chmod 777 /rdata/sqlexport/");
			} catch (Exception ex) {}
			
			try {
	        	Command.systemCommand("/bin/su - postgres -c \"/usr/bin/pg_dump -b -F c -i -f /rdata/sqlexport/airback.dump airback\"");
	        } catch(Exception _ex) {
	        	throw new Exception("cannot make a database dump. Ex: "+_ex.getMessage());
			}
			StringBuilder _sb = new StringBuilder();
			_sb.append("/bin/tar fcp ");
			_sb.append(_f.getAbsolutePath());
			_sb.append(" /rdata/bacula/ /rdata/working/ /etc/wbsairback-admin/ /rdata/sqlexport/ --exclude \"/etc/wbsairback-admin/license\"");
			try {
	        	Command.systemCommand(_sb.toString());
	        } catch(Exception _ex) {
	        	throw new Exception("cannot make an export file. Ex: "+_ex.getMessage());
			}
			Thread.sleep(1000);
		} finally {
			_fl.unlock();
		}
	}
	
	private static String readLine(InputStream is) throws IOException {
		if(is == null) {
			return null;
		}
		
		int i;
		StringBuilder _sb = new StringBuilder();
    	for(i = is.read(); i != -1; i = is.read()) {
    		if(i == 13) { continue; }
    		if(i == 10) {
    			break;
    		}
    		_sb.append((char) i);
    	}
    	if(i == -1) {
    		return null;
    	}
    	return _sb.toString();
    }
}
