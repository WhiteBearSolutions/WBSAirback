package com.whitebearsolutions.imagine.wbsairback.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DrbdCmanConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.NTPConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.MultiPathManager;
import com.whitebearsolutions.imagine.wbsairback.disk.RaidManager;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.net.ReplicationManager;
import com.whitebearsolutions.io.FileUtils;
import com.whitebearsolutions.util.Command;

public class Watchdog {
	public final static int RECOVERY_GENERAL = 2569835;
	public final static int RECOVERY_SYSTEM = 4284716;
	public final static int RECOVERY_SERVICE = 2290873;
	public final static int ERROR_GENERAL = 4234457;
	public final static int ERROR_TASK = 2840928;
	public final static int ERROR_SYSTEM = 9864124;
	public final static int ERROR_SERVICE = 5738633;
	public final static int SYSTEM_CPU = 2972972;
	public final static int SYSTEM_MEMORY = 37863833;
	public final static int SYSTEM_DISK = 98309583;
	public final static int SYSTEM_AGGREGATE_USED = 9358462;
	public final static int SYSTEM_AGGREGATE_QUOTAS = 4917034;
	public final static int SYSTEM_LICENSE = 8982742;
	public final static int SYSTEM_SYNCHRONIZATION = 87624826;
	public final static int SYSTEM_VOLUME_SYNCHRONIZATION = 89375933;
	public final static int SYSTEM_VOLUME_MOUNT = 36970546;
	public final static int SYSTEM_EXTERNAL_VOLUME_MOUNT = 20902383;
	public final static int SYSTEM_VOLUME_REMOVE = 47179902;
	public final static int SYSTEM_VOLUME_MOUNT_NFS = 10009278;
	public final static int SYSTEM_HYPERVISORS_TRUST_RELATION = 19002112;
	public final static int RAID_ADAPTER_ERROR = 98726402;
	private final static int REPORT_MINIMUM_PERIOD = 43200000;
	private final static String SYSTEMLOG_SYSLOG= "/var/log/syslog";
	private final static String SYSTEMLOG_MESSAGES= "/var/log/messages";
	private final static String SYSTEMLOG_DAEMON= "/var/log/daemon.log";
	private final static String SYSTEMLOG_SAMBA= "/var/log/samba";
	private final static String SYSTEMLOG_POSTGRES= "/var/log/postgresql";
	private final static String SYSTEMLOG_WBSAIRBACK_ADMIN = "/var/log/wbsairback-admin.log";
	public final static String SUPPORT_MAIL = "soporte@whitebearsolutions.com";
	private Map<Integer, String> services;
	private Map<Integer, Calendar> service_alerts;
	private Map<Integer, Calendar> system_alerts;
	private Boolean nfsProblems = false;
	
	private final static double AGG_UNUSED_LIMIT = 5D;
	
	private final static Logger logger = LoggerFactory.getLogger(Watchdog.class);
	
	public Watchdog() throws Exception {
		this.service_alerts = new HashMap<Integer, Calendar>();
		this.system_alerts = new HashMap<Integer, Calendar>();
		this.services = new HashMap<Integer, String>();
		//this.services.put(ServiceManager.BACULA_DIR, "BACKUP_DIRECTOR");
		//this.services.put(ServiceManager.BACULA_SD, "BACKUP_STORAGE");
		//this.services.put(ServiceManager.POSTGRES, "BACKUP_CATALOG");
		//this.services.put(ServiceManager.BACULA_FD, "BACKUP_FILE");
		this.services.put(ServiceManager.ISCSI_TARGET, "ISCSI_TARGET");
		this.services.put(ServiceManager.ISCSI_INITIATOR, "ISCSI_INITATOR");
		this.services.put(ServiceManager.NTP, "NTP");
		this.services.put(ServiceManager.NFS, "NFS");
		this.services.put(ServiceManager.SAMBA, "CIFS");
		this.services.put(ServiceManager.FTP, "FTP");
		this.services.put(ServiceManager.SSH, "SSH");
		this.services.put(ServiceManager.SNMP, "SNMP");
		this.services.put(ServiceManager.SNMPTRAP, "SNMP_TRAPS");
		this.services.put(ServiceManager.RSYNC, "SYNC");
		this.services.put(ServiceManager.MULTIPATHD, "MULTIPATHD");
		this.services.put(ServiceManager.MONGODB, "COMMAND_HISTORY_DB");
		nfsProblems = false;
		boolean init = true;
		
		try {
			activateSwap();
			if (ReplicationManager.hasToLaunch()) {
				ReplicationManager _rm = new ReplicationManager();
				logger.info("Lanzando procesos de replicación, si los hay ...");
				_rm.runAllSourceReplication(false);
			}
			this.services.remove(ServiceManager.NFS);
			logger.info("Lanzando proceso de control de ficheros de bloqueo ...");
			ObjectLock.runControlBlockFiles(null);
			try {
				Command.systemCommand("chmod 777 /usr/share/wbsairback/tomcat/logs/daemons.log");
			} catch (Exception ex) {}
		} catch(Exception _ex) {}
		for(Thread.sleep(60000); true; Thread.sleep(120000)) {
			// Primera ejecución de watchdog (reinicio), reiniciamos NFS
			if (init) {
				if (ServiceManager.isRunning(ServiceManager.NFS))
					ServiceManager.restart(ServiceManager.NFS);
				init = false;
			}
			Calendar _cal = Calendar.getInstance();
			if(_cal.get(Calendar.MINUTE) == 15 || _cal.get(Calendar.MINUTE) == 16) {
				if(_cal.get(Calendar.HOUR_OF_DAY) == 3) {
					logRotate();
				}
				try {
					new CronTasks();
				} catch(Exception _ex) {
					try {
						sendMail(ERROR_TASK, 0, _ex.getMessage());
					} catch(Exception _ex2) {
						logger.error("Error enviando mail tras fallar la creación de CronTasks. Ex:{}", _ex2.getMessage());
					}
				}
			} else if(_cal.get(Calendar.MINUTE) == 1 || _cal.get(Calendar.MINUTE) == 2 ||
					_cal.get(Calendar.MINUTE) == 11 || _cal.get(Calendar.MINUTE) == 12 ||
					_cal.get(Calendar.MINUTE) == 21 || _cal.get(Calendar.MINUTE) == 22 ||
					_cal.get(Calendar.MINUTE) == 31 || _cal.get(Calendar.MINUTE) == 32 ||
					_cal.get(Calendar.MINUTE) == 41 || _cal.get(Calendar.MINUTE) == 42 ||
					_cal.get(Calendar.MINUTE) == 51 || _cal.get(Calendar.MINUTE) == 52) {
				try {
					if (ReplicationManager.hasToLaunch()) {
						logger.info("Lanzando procesos de replicación, si los hay ...");
						ReplicationManager _rm = new ReplicationManager();
						_rm.runAllSourceReplication(false);
					}
				} catch(Exception _ex) {
					logger.error("Error lanzando procesos de replicación. Ex:{}", _ex.getMessage());
				}
			}
			
			new UpdateVersion();
			NTPConfiguration.resyncClock();
			monitorSystem();
			monitorServices();
			monitorSynchronization();
			monitorVolumeRemovingProcesses();
			monitorRaid();
			monitorVolumes();
			monitorExternalShares();
			monitorAggregates();
			garbageCollection();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("/var/www/webadministration/WEB-INF/classes/log4j.lcf");
			logger.info("*********************Init [wbs-watchdog]***********************");
			new Watchdog();
		} catch(Exception _ex) {
			logger.error("Error on Watchdog: " + _ex.getMessage());
		} finally {
			logger.info("*********************END [wbs-watchdog]********************");
		}

	}
	
	private static void garbageCollection() {
		Runtime.getRuntime().gc();
	}
	
	private void logRotate() {
		logger.info("Rotando logs ...");
		try {
			if("slave".equals(HAConfiguration.getStatus())) {
				return;
			}
			
			List<String> _logFiles=new ArrayList<String>();
			_logFiles.add(SYSTEMLOG_MESSAGES);
			_logFiles.add(SYSTEMLOG_SYSLOG);
			_logFiles.add(SYSTEMLOG_DAEMON);
			_logFiles.add(SYSTEMLOG_SAMBA);
			_logFiles.add(SYSTEMLOG_POSTGRES);
			_logFiles.add(SYSTEMLOG_WBSAIRBACK_ADMIN);
			for(String _file : _logFiles) {
				logRotate(new File(_file));
			}
			logger.info("Fin de proceso de rotacion de logs");
		} catch(Exception _ex) {
			logger.error("Error desconocido al rotar logs {}", _ex);
		}
	}
	
	private void logRotate(File _log) throws Exception {
		if(_log.exists()) {
			if(_log.isDirectory()) {
				for(File _f : _log.listFiles()) {
					logRotate(_f);
				}
			} else if(_log.isFile()) {
				if(_log.length() > 1048576) {
					for(int i = 1; i <= 5; i++) {
						File _f =  new File(_log.getAbsolutePath() + "." + i + ".gz");
						if(_f.exists()) {
							FileUtils.copyFile(_f, new File(_log.getAbsolutePath() + "." + (i + 1) + ".gz"));
						}
					}
					FileUtils.copyFile(_log, new File(_log.getAbsolutePath() + ".1"));
					FileUtils.gzip(new File(_log.getAbsolutePath() + ".1"));
					FileUtils.empty(_log);
				}
			}
		}
	}
	
	/**
	 * Monitoriza las conexiones ssl con
	 */
	/*private void monitorHypervisorSSL() {
		try {
			boolean _error = false;
			HypervisorManager _hm = null;
			logger.info("Monitorizando conexiones SSL de hypervisores ...");
			try {
				_hm = HypervisorManager.getInstance(HypervisorManager.GENERIC, null, null, null);
				logger.info("Instancia genérica OK");
			} catch (Exception ex) {
				logger.error("Error al cargar la primera instancia genérica de los hipervisores. Reiniciamos Tomcat ...");
				registerError(ERROR_SYSTEM, SYSTEM_HYPERVISORS_TRUST_RELATION, "Error in SSL Hypervisors trust relationship" );
				_error = true;
				ServiceManager.restartWebAdministration();
			}
		
			List<Map<String, String>> clients = _hm.getAllHypervisors();
			if(!clients.isEmpty()) {
				for(Map<String, String> _client : clients) {
					String name = _client.get("name");
					try {
						logger.debug("Monitorizando hypervisor {}", name);
						HypervisorManager _hmi = HypervisorManager.getInstance(_hm.getHypervisor(name));
		    			_hmi.getStoreNames();
		    			_hmi.getVirtualMachineNames();
						logger.info("Instancia de '{}' OK", name);
					} catch (Exception ex) {
						logger.error("Error en la instancia de '{}'. Reiniciamos Tomcat ...");
						registerError(ERROR_SYSTEM, SYSTEM_HYPERVISORS_TRUST_RELATION, "Error in SSL Hypervisors trust relationship" );
						_error = true;
						ServiceManager.restartWebAdministration();
					}
				}
			} else
				logger.info("No hay ningún hypervisor definido");
			
			if (!_error) {
				unregisterError(ERROR_SYSTEM, SYSTEM_HYPERVISORS_TRUST_RELATION);
				logger.info("Ningún error de hypervisores");
			}
			logger.info("Fin de monitorización de hypervisores");
		} catch (Exception ex) {
			logger.error("Error desconocido al monitorizar hypervisores");
		}
	}*/
	
	/**
	 * Monitoriza la ocupación y las quotas de los agregados
	 */
	private void monitorAggregates() {
		logger.info("Monitorizando espacio libre en agregados ...");
		try {
			List<Map<String, String>> vgs = VolumeManager.getVolumeGroups();
			Double unused = null;
			for (Map<String, String> vg : vgs) {
				try {
					unused = null;
					logger.info("Monitorizando espacio libre de agregado [{}]. Tipo [{}]", vg.get("name"), vg.get("type"));
					if (vg.get("type").equals("zfs")) {
						if (vg.get("unused-percent-raw") != null) {
							unused = Double.parseDouble(vg.get("unused-percent-raw"));
						}
						if (vg.get("quota-raw") != null && vg.get("size-raw") != null) {
							Double quota = Double.parseDouble(vg.get("quota-raw"));
							Double size = Double.parseDouble(vg.get("size-raw"));
							logger.debug("Agg [{}]: size: {} - quota: {}", new Object[]{vg.get("name"), size, quota});
							/*if (quota > size) {
								registerErrorVolume(ERROR_SYSTEM, SYSTEM_AGGREGATE_QUOTAS, vg.get("name"), "Group quota is greater than group size" );
							} else {
								unregisterErrorVolume(ERROR_SYSTEM, SYSTEM_AGGREGATE_QUOTAS, vg.get("name"), null);
							}*/
						}
					} else if (vg.get("type").equals("lvm")) {
						List<String> volumes = VolumeManager.getLogicalVolumeNames(vg.get("name"));
						List<String> dupped = new ArrayList<String>();
						Double used = 0D;
						for (String volname : volumes) {
							if (VolumeManager.isMount(vg.get("name"), volname)) {
								if (!dupped.contains(volname)) {
									Map<String, String> vol = FileSystemManager.getFileSystemParameters(vg.get("name"), volname);
									if (vol.get("used-raw") != null && !vol.get("used-raw").isEmpty()) {
										logger.debug("vol [{}] used: {}", volname, vol.get("used-raw"));
										used += Double.parseDouble(vol.get("used-raw"));
										dupped.add(volname);
									}
								}
							}
						}
						unused = (Double.parseDouble(vg.get("size-raw")) - used)*100/Double.parseDouble(vg.get("size-raw"));
					}
					
					if (unused != null) {
						if (unused < 0)
							unused = 0D;
						else if (unused > 100D)
							unused = 100D;
						logger.debug("Agg [{}]: unused: {} - percent: {}", new Object[]{vg.get("name"), unused, String.format("%.2f", unused)});
						if (unused <= AGG_UNUSED_LIMIT) {
							registerErrorVolume(ERROR_SYSTEM, SYSTEM_AGGREGATE_USED, vg.get("name"), "Maximun aggregate rate achieved - ["+vg.get("name")+"]: "+String.format("%.2f", unused)+"%");
						} else {
							unregisterErrorVolume(ERROR_SYSTEM, SYSTEM_AGGREGATE_USED, vg.get("name"), null);
						}
					} else {
						unregisterErrorVolume(ERROR_SYSTEM, SYSTEM_AGGREGATE_USED, vg.get("name"), null);
					}
				} catch (Exception ex) {
					logger.error("Error monitorizando espacio libre en agregado [{}]. Ex: {}", vg.get("name"), ex.getMessage());
				}
			}
			
		} catch (Exception ex) {
			logger.error("Error monitorizando espacio libre en agregados. Ex: {}", ex.getMessage());
		}
	}
	
	
	/**
	 * Monitoriza los dispositivos RAID
	 */
	private void monitorRaid() {
		boolean _error = false;
		logger.info("Monitorizando Raid ...");
		try {
			if (RaidManager.hasRaidController()) {
				logger.info("La maquina tiene controladores raid");
				StringBuilder errorMsg = new StringBuilder();
				List<Map<String, Map<String, String>>> adaptersInfo = new ArrayList<Map<String, Map<String, String>>>();
				List<Map<String, String>> adpErrors = RaidManager.checkAdpAllInfo(adaptersInfo);
				if (adpErrors != null) {
					logger.warn("Se encontraron valores no optimos con el comando adp");
					_error = true;
					errorMsg.append(RaidManager.errorsToString(adpErrors));
				}
				
				List<Map<String, Map<String, Object>>> drivesInfo = new ArrayList<Map<String, Map<String, Object>>>();
				List<Map<String, String>> drvErrors = RaidManager.checkCfgDsplay(drivesInfo);
				if (drvErrors != null) {
					logger.warn("Se encontraron valores no optimos con el comando drv");
					_error = true;
					errorMsg.append(RaidManager.errorsToString(adpErrors));
				}
				
				List<Map<String, Map<String, String>>> slotsInfo = new ArrayList<Map<String, Map<String, String>>>();
				List<Map<String, String>> slotsErrors = RaidManager.checkPdListAll(slotsInfo);
				if (slotsErrors != null) {
					logger.warn("Se encontraron valores no optimos con el comando pdl");
					_error = true;
					errorMsg.append(RaidManager.errorsToString(slotsErrors));
				}
				
				List<Map<String, String>> batteriesInfo = new ArrayList<Map<String, String>>();
				List<Map<String, String>> batteriesErrors = RaidManager.checkadpBbuCmd(batteriesInfo);
				if (batteriesErrors != null) {
					logger.warn("Se encontraron valores no optimos con el comando bbu");
					_error = true;
					errorMsg.append(RaidManager.errorsToString(batteriesErrors));
				}
				
				if (_error) {
					logger.warn("Se econtro algún error RAID del que informamos mediante email");
					registerError(ERROR_SYSTEM, RAID_ADAPTER_ERROR, errorMsg.toString());
				} else if(!_error){
					logger.info("No se econtro ningun error en los discos RAID");
					unregisterError(ERROR_SYSTEM, RAID_ADAPTER_ERROR);
				}
			} else {
				logger.info("La maquina no tiene controladores raid");
			}
			logger.info("Fin de proceso de monitorización de Raid ...");
		} catch(Exception _ex) {
			logger.error("Error desconocido al monitorizar discos raid: {}", _ex);
		} finally {
			logger.info("Removing megaclie log files ...");
			deleteFileIgnoreCase("megasas.log", "/");
			deleteFileIgnoreCase("megasas.log", "/root/");
			
		}
	}
	
	private void deleteFileIgnoreCase(String nameFile, String path) {
		try {
			File log = new File(path);
			if (log.exists()) {
				String [] dirs = log.list();
				if (dirs != null && dirs.length>0) {
					for (String f : dirs) {
						if (f.equalsIgnoreCase(nameFile)) {
							log = new File(path+f);
							if (log.exists()) {
								log.delete();
								logger.info("File [{}][{}] deleted", path, nameFile);
							}
						}
							
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error trying to delete [{}][{}]. Ex: {}", new Object[]{path, nameFile, ex.getMessage()});
		}
	}
	
	private void monitorVolumeRemovingProcesses() {
		logger.info("Monitorizando procesos de borrado de volumenes ...");
		try {
			boolean _recovery = true;
			File _dir = new File("/tmp/");
			if(_dir.exists() && _dir.isDirectory()) {
				for(File _f : _dir.listFiles()) {
					if(_f.getName().endsWith(".rv")) {
						StringBuilder _sb = new StringBuilder();
						_sb.append("Could not remove volume:  ");
						_sb.append(_f.getName().substring(_f.getName().indexOf("-")+1, _f.getName().length() - 3));
						registerError(ERROR_SYSTEM, SYSTEM_VOLUME_REMOVE, _sb.toString());
						_recovery = false;
					}
				}
				if(_recovery) {
					logger.info("Ningun problema detectado con el borrado de volumenes");
					unregisterError(ERROR_SYSTEM, SYSTEM_VOLUME_REMOVE);
				}
			}
			logger.info("Fin de proceso de monitorización de borrado de de volumenes ...");
		} catch (Exception ex) {
			logger.error("Error monitorizando proceso de borrado de volumenes. Ex: {}", ex.getMessage());
		}
	}
	
	
	/**
	 * Monitorización de volúmenes del sistema para evitar llenado del raiz
	 */
	private void monitorVolumes() {
		try {
			logger.info("Monitorizando montaje de volumenes ...");
			Map<String, String[]> fstab = FileSystemManager.readFstab();
			if (!HAConfiguration.inCluster() || (HAConfiguration.inCluster() && HAConfiguration.isActiveNode())) {
				logger.debug("Monitorizamos montaje de volumenes en modo: No-cluster | nodo-activo");
				remountFsTab(fstab, false);
			} else if(HAConfiguration.inCluster() && !HAConfiguration.isActiveNode()) {
				logger.debug("Monitorizamos montaje de volumenes en modo: cluster-pasivo");
				remountFsTab(fstab, true);
			}
			logger.info("Fin de monitorización de montaje de volumenes.");
		} catch (Exception _ex) {
			logger.error("Error monitorizando volumenes: "+_ex);
		}
	}
	
	
	/**
	 * Monitorización de los volúmenes externos compartidos nfs, para re-montarlos si es necesario
	 */
	private void monitorExternalShares() {
		try {
			logger.info("Monitorizando volúmenes externos ...");
			List<Map<String, String>> shares = ShareManager.getExternalShares();
			if (shares != null && !shares.isEmpty()) {
				String output = null;
				try {
					 output = Command.systemCommand("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" df -kP");
				} catch (Exception ex) {
					output = ex.getMessage();
				}
				//logger.debug("Salida de df: {}", output);
				
				String line = null;
				String path = null;
				String idError = null;
				List<String> problemPaths = new ArrayList<String>();
				if (output != null && output.contains("df:")) {
					output = output.replace("\r", "");
					StringTokenizer st = new StringTokenizer(output, "\n");
					while (st.hasMoreTokens()) {
						line = st.nextToken();
						path = line.substring(line.indexOf("df: ")+"df: ".length()+1);
						path = path.substring(0, path.indexOf(":")-1);
						problemPaths.add(path);
						logger.debug("Path problemático por desconexión-permisos: {}", path);
					}
				}
				for (Map<String, String> share : shares) {
					if (!problemPaths.contains(share.get("path"))) {
						try {
							Command.systemCommand("timeout "+WBSAirbackConfiguration.getTimeoutDfCommand()+" ls "+share.get("path"));
						} catch (Exception ex) {
							problemPaths.add(share.get("path"));
							logger.debug("Path problemático por ls: {}", share.get("path"));
						}
					}
					
					//logger.debug("Path de share: {}", share.get("path"));
					idError = share.get("server")+"_"+share.get("name");
					idError = idError.replace("//", "/");
					idError = idError.replace("\\", "/");
					idError = idError.replace("/", "-");
					if (problemPaths.contains(share.get("path"))) {
						logger.info("Encontrado recurso externo problematico en {}. Desmontamos...", share.get("path"));
						try {
							ShareManager.umountExternalShare(share.get("path"));
						} catch (Exception ex) {
							logger.info("El desmontaje falló...");
						}
						logger.info("Desmontado, intentamos montar...");
						try {
							ShareManager.mountExternalShare(share.get("server"), share.get("share"));
							logger.error("Share se pudo montar de nuevo!");
							unregisterErrorVolume(ERROR_SYSTEM, SYSTEM_EXTERNAL_VOLUME_MOUNT, idError, "External volume "+share.get("path")+" was re-mounted");
						} catch (Exception ex) {
							logger.error("No se pudo remontar el share");
							File f = new File(share.get("path"));
							if (f.exists()) {
								String[] contents = f.list();
								if (contents == null || contents.length == 0) {
									f.delete();
									registerErrorVolume(ERROR_SYSTEM, SYSTEM_EXTERNAL_VOLUME_MOUNT,idError, "External volume "+share.get("path")+" cannot mount, so local folder was removed");
									logger.debug("path de external share {} eliminado", share.get("path"));
								} else {
									registerErrorVolume(ERROR_SYSTEM, SYSTEM_EXTERNAL_VOLUME_MOUNT,idError, "External volume "+share.get("path")+" cannot mount and cannot remove local folder because it is not empty!");
									logger.debug("path de external share {} o", share.get("path"));
								}
							} else {
								registerErrorVolume(ERROR_SYSTEM, SYSTEM_EXTERNAL_VOLUME_MOUNT,idError, "External volume "+share.get("path")+" cannot mount, so local folder was removed");
								logger.debug("path de external share {} ya estaba eliminado", share.get("path"));
							}
							
						}
					} else {
						unregisterErrorVolume(ERROR_SYSTEM, SYSTEM_EXTERNAL_VOLUME_MOUNT, idError, null);
					}
				}
			}
			logger.info("Fin de monitorización de volúmenes externos ...");
		} catch (Exception _ex) {
			logger.error("Error monitorizando volúmenes externos: "+_ex);
		}
	}
	
	private void remountFsTab(Map<String, String[]> _fstab, boolean onlyLocal) {
		if (_fstab != null && _fstab.size() > 0) {
			nfsProblems = false;
			for (String key : _fstab.keySet()) {
				String[] _item = _fstab.get(key);
				int remount = 1;
				if (_item.length>6 &&_item[6] != null)
					remount = Integer.parseInt(_item[6]);
				try {
					boolean isLocal = false;
					Map<String, String> vol = VolumeManager.getLogicalVolumeFromPath(_item[1]);
					if (onlyLocal) {
						if (vol != null && !vol.isEmpty()) {
							String volGroup = vol.get("vg");
							if (VolumeManager.isLocalDeviceGroup(volGroup)) {
								isLocal = true;
							}
						}
					}
					
					if (remount > 0) {
						if (!onlyLocal || isLocal == true) {
							String idError = _item[0].replaceAll("/", "-");
							if (!VolumeManager.isSystemMounted(_item[1])) {
								logger.warn("Encontrado volumen no montado [fs:"+_item[0]+", type:"+ _item[2]+"]");
								try {
									if (VolumeDaemon.mountSystem(_item[0], _item[1], _item[2], _item[3])) {
										logger.info("Volumen re-montado [fs:"+_item[0]+", type:"+ _item[2]+"]");
										unregisterErrorVolume(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT, idError, "Volume [fs:"+_item[0]+", type:"+ _item[2]+"] was re-mounted");
									} else {
										logger.error("Volumen NO se pudo re-montar [fs:"+_item[0]+", type:"+ _item[2]+"]");
										if (ShareManager.isShareFromPath(_item[1])) {
											nfsProblems = true;
										}
										File f = new File(_item[1]);
										if (f.exists()) {
											String[] contents = f.list();
											if (contents == null || contents.length == 0) {
												f.delete();
												registerErrorVolume(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT, idError, "Volume [fs:"+_item[0]+", type:"+ _item[2]+"] cannot mount, so local folder was removed");
												logger.debug("Directorio de volumen [fs:"+_item[0]+", type:"+ _item[2]+"] eliminado");
											} else {
												registerErrorVolume(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT,  idError, "Volume [fs:"+_item[0]+", type:"+ _item[2]+"] cannot mount, and cannot remove local folder because it is not empty!");
												logger.warn("Directorio de volumen [fs:"+_item[0]+", type:"+ _item[2]+"] no se pudo eliminar porque tiene contenido!!");
											}
										}
										
									}
								} catch (Exception ex) {
									logger.error("Volumen NO se pudo re-montar [fs:"+_item[0]+", type:"+ _item[2]+"]");
									File f = new File(_item[1]);
									if (f.exists()) {
										String[] contents = f.list();
										if (contents == null || contents.length == 0) {
											f.delete();
											registerErrorVolume(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT,idError, "Volume [fs:"+_item[0]+", type:"+ _item[2]+"] cannot mount, so local dir was removed");
											logger.debug("Directorio de volumen [fs:"+_item[0]+", type:"+ _item[2]+"] eliminado");
										} else {
											registerErrorVolume(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT,  idError, "Volume [fs:"+_item[0]+", type:"+ _item[2]+"] cannot mount, and cannot remove local dir because it is not empty");
											logger.warn("Directorio de volumen [fs:"+_item[0]+", type:"+ _item[2]+"] no se pudo eliminar porque tiene contenido!!");
										}
									}
								}
							} else {
								unregisterErrorVolume(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT, idError, null);
							}
						}
					}
				} catch (Exception ex) {
					logger.error("Error intentando remontar Sistema [fs:{} tipo:{}] ex: {}", new Object[]{_item[0], _item[2], ex});
					
				}
			}
			if (nfsProblems) {
				logger.error("Hay problemas NFS. Un volumen exportado no pudo montarse.");
				ShareManager.stopAllShareServices();
				registerError(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT_NFS, "Some shared volumes could not be mounted. Share services stopped");
			} else {
				logger.info("No hay o se recuperaron los problemas NFS-CIFS");
				ShareManager.startAllShareServices();
				unregisterError(ERROR_SYSTEM, SYSTEM_VOLUME_MOUNT_NFS);
			}
		}
	}
	
	private void monitorSynchronization() {
		logger.info("Monitorizando sincronizacion de volumenes ...");
		boolean _recovery = true;
		File _dir = new File("/tmp/");
		ReplicationManager.removeOldCommands();
		if(_dir.exists() && _dir.isDirectory()) {
			for(File _f : _dir.listFiles()) {
				if(_f.getName().endsWith(".sync")) {
					StringBuilder _sb = new StringBuilder();
					_sb.append(_f.getName().substring(0, _f.getName().length() - 5));
					_sb.append(" - ");
					try {
						ByteArrayOutputStream _baos = new ByteArrayOutputStream();
						FileInputStream _fis = new FileInputStream(_f);
						try {
							for(int c = _fis.read(); c != -1; c = _fis.read()) {
								_baos.write(c);
							}
							_sb.append(_baos.toString());
						} finally {
							try {
								_fis.close();
							} catch(Exception _ex) {}
							_baos.close();
						}
					} catch(Exception _ex) {
						_sb.append("unknown replication error");
						logger.error("Algun error en la replicacion de volumenes");
					}
					registerError(ERROR_SYSTEM, SYSTEM_VOLUME_SYNCHRONIZATION, _sb.toString());
					_recovery = false;
				}
			}
			if(_recovery) {
				logger.info("Ningun problema detectado con la replicacion de volumenes");
				unregisterError(ERROR_SYSTEM, SYSTEM_VOLUME_SYNCHRONIZATION);
			}
		}
		logger.info("Fin de proceso de monitorizacion de sincronizacion de volumenes ...");
	}
	
	private void monitorSystem() {
		logger.info("Monitorizando sistema ...");
		int _value = GeneralSystemConfiguration.getMemoryLoad();
		if(_value > 90) {
			StringBuilder _sb = new StringBuilder();
			_sb.append("Maximum memory rate achieved (");
			_sb.append(_value);
			_sb.append(" %)");
			logger.warn("Maximum memory rate achieved {}%", _value);
			registerError(ERROR_SYSTEM, SYSTEM_MEMORY, _sb.toString());
		} else {
			unregisterError(ERROR_SYSTEM, SYSTEM_MEMORY);
		}
		try {
			boolean disk_alert = false;
			Map<String, Integer> partitions = GeneralSystemConfiguration.getDiskLoad();
			for(String partition : partitions.keySet()) {
				try {
					logger.debug("Comprogando ocupación de {}",partition);
					Map<String, String> volume = null;
					try {
						if (!partition.contains("/shares/"))
							volume = VolumeManager.getLogicalVolumeFromPath(partition);
						if (volume != null && !volume.isEmpty())
							volume = VolumeManager.getLogicalVolume(volume.get("vg"), volume.get("lv"));
					} catch (Exception ex) {}
					if (volume != null && volume.get("fstype") != null && FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, volume.get("fstype"))) {
						double used = Double.parseDouble(volume.get("used-raw"));
						double percent = used * 100D / Double.parseDouble(volume.get("size-raw"));
						logger.debug("Es zfs y tiene ocupado {}", used);
						if (percent > 90d) {
							StringBuilder _sb = new StringBuilder();
							_sb.append("Maximum disk rate achieved (");
							_sb.append(volume.get("vg")+"/"+volume.get("name"));
							_sb.append(" - ");
							_sb.append(volume.get("used"));
							_sb.append(" )");
							logger.warn("Maximum disk rate achieved: {} - {}%",partition, percent);
							registerError(ERROR_SYSTEM, SYSTEM_DISK, _sb.toString());
							disk_alert = true;
						}
					} else if(partitions.get(partition) > 90) {
						StringBuilder _sb = new StringBuilder();
						_sb.append("Maximum disk rate achieved (");
						if(partition.contains(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
							String _path = partition.replace(WBSAirbackConfiguration.getDirectoryVolumeMount(), "");
							if(_path.startsWith("/shares/")) {
								_path = _path.substring(8);
							} else if(_path.startsWith("/")) {
								_path = _path.substring(1);
							}
							_sb.append(_path);
						} else if(partition.startsWith("/dev/")) {
							String _path = partition.replace("/dev/", "");
							_sb.append(_path);
						} else {
							_sb.append(partition);
						}
						_sb.append(" - ");
						_sb.append(partitions.get(partition));
						_sb.append(" %)");
						logger.warn("Maximum disk rate achieved: {} - {}%",partition, partitions.get(partition));
						registerError(ERROR_SYSTEM, SYSTEM_DISK, _sb.toString());
						disk_alert = true;
					}
				} catch (Exception _ex1) {
					logger.error("Error comprobando ocupacion de disco {}. Ex: {}",partition, _ex1.getMessage());
				}
			}
			if(!disk_alert) {
				unregisterError(ERROR_SYSTEM, SYSTEM_DISK);
			}
		} catch(Exception _ex) {
			logger.error("Error desconocido comprobando ocupacion de discos. Ex: {}", _ex.getMessage());
		}
		try {
			String _status = HAConfiguration.getStatus();
			if(_status.equals("master") || _status.equals("slave")) {
				if (HAConfiguration.getConfiguredClusterDisk() == null) {
					_status = DrbdCmanConfiguration.drbdGetSynchronizationStatus();
					if(!_status.contains("UpToDate/UpToDate")) {
						StringBuilder _sb = new StringBuilder();
						_sb.append("Status (");
						_sb.append(_status);
						_sb.append(")");
						registerError(ERROR_SYSTEM, SYSTEM_SYNCHRONIZATION, _sb.toString());
						logger.warn("El estado del disco DRBD no es el correcto: {}", _status);
					} else {
						unregisterError(ERROR_SYSTEM, SYSTEM_SYNCHRONIZATION);
					}
				} else {
					unregisterError(ERROR_SYSTEM, SYSTEM_SYNCHRONIZATION);
				}
			} else {
				unregisterError(ERROR_SYSTEM, SYSTEM_SYNCHRONIZATION);
			}
		} catch(Exception _ex) {
			logger.error("Error desconocido comprobando sincronización DRBD");
		}
		logger.info("Fin de monitorización de sistema.");
	}
	
	private void monitorServices() {
		logger.info("Monitorizando servicios ...");
		try {
			String _status = HAConfiguration.getStatus();
			if(_status.equals("slave")) {
				/*
				 // TODO: uncoment with bacula version
				 * if(this.services.containsKey(ServiceManager.BACULA_DIR)) {
					this.services.remove(ServiceManager.BACULA_DIR);
				}
				if(this.services.containsKey(ServiceManager.BACULA_SD)) {
					this.services.remove(ServiceManager.BACULA_SD);
				}
				if(this.services.containsKey(ServiceManager.POSTGRES)) {
					this.services.remove(ServiceManager.POSTGRES);
				}*/
				if(this.services.containsKey(ServiceManager.ISCSI_TARGET)) {
					this.services.remove(ServiceManager.ISCSI_TARGET);
				}
				if(this.services.containsKey(ServiceManager.NFS)) {
					this.services.remove(ServiceManager.NFS);
				}
				if(this.services.containsKey(ServiceManager.SAMBA)) {
					this.services.remove(ServiceManager.SAMBA);
				}
				if(this.services.containsKey(ServiceManager.FTP)) {
					this.services.remove(ServiceManager.FTP);
				}
				if(this.services.containsKey(ServiceManager.RSYNC)) {
					this.services.remove(ServiceManager.RSYNC);
				}
				if(this.services.containsKey(ServiceManager.SNMP)) {
					this.services.remove(ServiceManager.SNMP);
				}
				if(this.services.containsKey(ServiceManager.SNMPTRAP)) {
					this.services.remove(ServiceManager.SNMPTRAP);
				}
			} else {
				/*
				 // TODO: uncoment with bacula version
				 * if(!this.services.containsKey(ServiceManager.BACULA_DIR)) {
					this.services.put(ServiceManager.BACULA_DIR, "BACKUP_DIRECTOR");
				}
				if(!this.services.containsKey(ServiceManager.BACULA_SD)) {
					this.services.put(ServiceManager.BACULA_SD, "BACKUP_STORAGE");
				}
				if(!this.services.containsKey(ServiceManager.POSTGRES)) {
					this.services.put(ServiceManager.POSTGRES, "BACKUP_CATALOG");
				}*/
				if(!this.services.containsKey(ServiceManager.ISCSI_TARGET)) {
					this.services.put(ServiceManager.ISCSI_TARGET, "ISCSI_TARGET");
				}
				if(!this.services.containsKey(ServiceManager.NFS)) {
					this.services.put(ServiceManager.NFS, "NFS");
				}
				if(!this.services.containsKey(ServiceManager.SAMBA)) {
					this.services.put(ServiceManager.SAMBA, "CIFS");
				}
				if(!this.services.containsKey(ServiceManager.FTP)) {
					this.services.put(ServiceManager.FTP, "FTP");
				}
				if(!this.services.containsKey(ServiceManager.RSYNC)) {
					this.services.put(ServiceManager.RSYNC, "SYNC");
				}
				if(!this.services.containsKey(ServiceManager.SNMP)) {
					this.services.put(ServiceManager.SNMP, "SNMP");
				}
				if(!this.services.containsKey(ServiceManager.SNMPTRAP)) {
					this.services.put(ServiceManager.SNMPTRAP, "SNMP_TRAPS");
				}
			}
			if(_status.equals("master") || _status.equals("slave")) {
				if(!this.services.containsKey(ServiceManager.HEARTBEAT)) {
					this.services.put(ServiceManager.HEARTBEAT, "HEARTBEAT");
				}
				if(!this.services.containsKey(ServiceManager.DRBD) && HAConfiguration.getConfiguredClusterDisk() == null) {
					this.services.put(ServiceManager.DRBD, "DRBD");
				}
			} else {
				if(this.services.containsKey(ServiceManager.HEARTBEAT)) {
					this.services.remove(ServiceManager.HEARTBEAT);
				}
				if(this.services.containsKey(ServiceManager.DRBD)) {
					this.services.remove(ServiceManager.DRBD);
				}
			}
		} catch(Exception _ex) {
			logger.error("Error al comprobar algun servicio. ex: {}", _ex);
		}
		
		for(int service : this.services.keySet()) {
			try {
				if (service != ServiceManager.MULTIPATHD && service != ServiceManager.NFS  && service != ServiceManager.SAMBA || (service == ServiceManager.MULTIPATHD && MultiPathManager.isMultipathEnabled()) || (service == ServiceManager.NFS && !nfsProblems) || (service == ServiceManager.SAMBA && !nfsProblems)) {
					if(ServiceManager.isRunning(service)) {
						unregisterError(ERROR_SERVICE, service);
					} else {
						logger.error("El servicio: {} no está corriendo!", service);
						registerError(ERROR_SERVICE, service, null);
						try {
							if (service == ServiceManager.MULTIPATHD)
								MultiPathManager.initializeMultipath();
							else {
								logger.error("Intentando reiniciar servicio: {} que no estaba corriendo...", service);
								ServiceManager.restart(service);
								logger.error("Servicio {} reiniciado correctamente", service);
							}
						} catch(Exception _ex) {
							logger.error("No se pudo reiniciar el servicio: "+this.services.get(service));
						}
					}
				}
			} catch(Exception _ex) {
				registerError(ERROR_GENERAL, service, _ex.getMessage());
				logger.error("Error general con el servicio {}. ex: {}",service, _ex);
			}
		}
		logger.info("Fin de proceso de monitorizacion de servicios");
	}
	
	private void registerErrorVolume(int type, int subtype, String vol, String message) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/tmp/");
		_sb.append((type * 1000) + subtype);
		_sb.append(vol);
		_sb.append(".wderror");
		
		File _f = new File(_sb.toString());
		try {
			if(!_f.exists() && _f.createNewFile()) {
				try {
					sendMail(type, subtype, message);
				} catch(Exception _ex) {
					_f.delete();
					throw _ex;
				}
			}
		} catch(Exception _ex) {
			logger.error("Error intentando enviar email de registro de error de volumen con vol:{} tipo:{} subtipo:{} mensaje:{}. ex:{}", new Object[]{vol, type, subtype, message, _ex.getMessage()});
		}
	}
	
	private void unregisterErrorVolume(int type, int subtype, String vol, String message) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/tmp/");
		_sb.append((type * 1000) + subtype);
		_sb.append(vol);
		_sb.append(".wderror");
		
		File _f = new File(_sb.toString());
		
		try {
			if(_f.exists() && _f.delete()) {
				sendMail(RECOVERY_SYSTEM, subtype, message);
			} else if (message != null) {
				sendMail(RECOVERY_SYSTEM, subtype, message);
			}
		} catch (Exception ex) {
			logger.error("Error intentando enviar email de unregister de volumen con vol:{} tipo:{} subtipo:{} mensaje:{}. ex:{}", new Object[]{vol, type, subtype, message, ex.getMessage()});
		}
	}
	
	private void registerError(int type, int subtype, String message) {
		if(type == ERROR_SERVICE) {
			if(this.service_alerts.containsKey(subtype)) {
				Calendar _cal = this.service_alerts.get(subtype);
				if((Calendar.getInstance().getTimeInMillis() - _cal.getTimeInMillis()) < REPORT_MINIMUM_PERIOD) {
					return;
				} else {
					this.service_alerts.remove(subtype);
					this.service_alerts.put(subtype, Calendar.getInstance());
				}
			} else {
				this.service_alerts.put(subtype, Calendar.getInstance());
			}
		} else if(type == ERROR_SYSTEM) {
			if(this.system_alerts.containsKey(subtype)) {
				Calendar _cal = this.system_alerts.get(subtype);
				if((Calendar.getInstance().getTimeInMillis() - _cal.getTimeInMillis()) < REPORT_MINIMUM_PERIOD) {
					return;
				} else {
					this.system_alerts.remove(subtype);
					this.system_alerts.put(subtype, Calendar.getInstance());
				}
			} else {
				this.system_alerts.put(subtype, Calendar.getInstance());
			}
		}
		
		if(type != ERROR_TASK) {
			StringBuilder _sb = new StringBuilder();
			_sb.append("/tmp/");
			_sb.append((type * 1000) + subtype);
			_sb.append(".wderror");
			
			File _f = new File(_sb.toString());
			try {
				if(!_f.exists() && _f.createNewFile()) {
					try {
						sendMail(type, subtype, message);
					} catch(Exception _ex) {
						_f.delete();
						throw _ex;
					}
				}
			} catch(Exception _ex) {
				logger.error("Error intentando enviar email de registro de error con tipo:{} subtipo:{} mensaje:{}. ex:{}", new Object[]{type, subtype, message, _ex.getMessage()});
			}
		} else {
			try {
				sendMail(type, subtype, message);
			} catch(Exception _ex) {
				logger.error("Error intentando enviar email de registro de error con tipo:{} subtipo:{} mensaje:{}. ex:{}", new Object[]{type, subtype, message, _ex.getMessage()});
			}
		}
	}
	
	private void unregisterError(int type, int subtype) {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/tmp/");
		_sb.append((type * 1000) + subtype);
		_sb.append(".wderror");
		
		File _f = new File(_sb.toString());
		try {
			switch(type) {
				case ERROR_GENERAL:
					type = RECOVERY_GENERAL;
					break;
				case ERROR_SERVICE:
					type = RECOVERY_SERVICE;
					break;
				case ERROR_SYSTEM:
					type = RECOVERY_SYSTEM;
					break;
			}
			if(_f.exists() && _f.delete()) {
				try {
					sendMail(type, subtype, null);
				} catch(Exception _ex) {
					throw _ex;
				}
			}
		} catch(Exception _ex) {
			logger.error("No se pudo desregistrar el error tipo:{} subtipo:{}. ex:", new Object[]{type, subtype, _ex.getMessage()});
		}
	}
	
	private void sendMail(int type, int subtype, String text) throws Exception {
		try {
			StringBuilder _subject = new StringBuilder();

			if(type == ERROR_SERVICE || type == RECOVERY_SERVICE) {
				if(this.services.containsKey(subtype)) {
					_subject.append(this.services.get(subtype));
					_subject.append(" ");
				} else {
					_subject.append("UNKNOWN ");
				}
				_subject.append("service ");
				if(type == ERROR_SERVICE) {
					_subject.append("error");
				} else {
					_subject.append("recovery");
				}
			} else if(type == ERROR_SYSTEM || type == RECOVERY_SYSTEM) {
				switch(subtype) {
					case SYSTEM_CPU: {
							_subject.append("CPU ");
							_subject.append("use percent ");
						}
						break;
					case SYSTEM_MEMORY: {
							_subject.append("MEMORY ");
							_subject.append("use percent ");
						}
						break;
					case SYSTEM_DISK: {
							_subject.append("DISK ");
							_subject.append("use percent ");
						}
						break;
					case SYSTEM_LICENSE: {
							_subject.append("LICENSE ");
						}
						break;
					case SYSTEM_AGGREGATE_QUOTAS: {
							_subject.append("GROUP volume quotas ");
						}
						break;
					case SYSTEM_AGGREGATE_USED: {
							_subject.append("GROUP use percent ");
						}
						break;
					case SYSTEM_VOLUME_SYNCHRONIZATION: {
							_subject.append("VOLUME SYNCHRONIZATION ");
						}
						break;
					case SYSTEM_VOLUME_REMOVE: {
							_subject.append("VOLUME REMOVE ");
						}
						break;
					case SYSTEM_SYNCHRONIZATION: {
							_subject.append("HA SYNCHRONIZATION ");
						}
						break;
					case SYSTEM_VOLUME_MOUNT: {
							_subject.append("MOUNTED VOLUMES ");
						}
						break;
					case SYSTEM_EXTERNAL_VOLUME_MOUNT: {
							_subject.append("EXTERNAL MOUNTED VOLUMES ");
						}
						break;
					case SYSTEM_VOLUME_MOUNT_NFS: {
						_subject.append("SHARED VOLUMES ");
						}
						break;
					case SYSTEM_HYPERVISORS_TRUST_RELATION: {
							_subject.append("HYPERVISOR SSL ");
						}
						break;
					case RAID_ADAPTER_ERROR: {
							_subject.append("RAID DEVICE ");
						}
						break;
					default: {
							_subject.append("UNKNOWN(");
							_subject.append(subtype);
							_subject.append(") ");
						}
						break;
				}
				
				if(type == ERROR_SYSTEM) {
					_subject.append("error");
				} else {
					_subject.append("recovery");
				}
			} else if(type == ERROR_TASK) {
				_subject.append("Automated task error");
			} else {
				_subject.append("general ");
				if(type == ERROR_GENERAL) {
					_subject.append("error");
				} else {
					_subject.append("recovery");
				}
			}
			_subject.insert(0, "WBSAIRBACK-WD ");
			
			MailReport mr = new MailReport();
			mr.sendInfoMail(type, text,_subject.toString());
			
			logger.info("Email enviado correctamente con subject:{} text: {}", _subject, text);
		} catch (Exception ex) {
			logger.error("Error al intentar enviar un mail con type:{}, subtype:{} y text:{}. Exception: {}", new Object[]{type, subtype, text, ex.getMessage()});
			throw ex;
		}
	}
	
	public void activateSwap() throws Exception {
		String swapPartition = "/dev/sda5";
		try {
			if (new File (swapPartition).exists()) {
				String output = Command.systemCommand("swapon -s");
				if (output != null && !output.isEmpty() && !output.contains(swapPartition)) {
					logger.debug("No hay partición swap activa. Formateando {} como swap ...", swapPartition);
					Command.systemCommand("mkswap "+swapPartition);
					logger.debug("Formateada. Activando particion swap ...");
					Command.systemCommand("swapon "+swapPartition);
					logger.debug("Particion swap {} activada",swapPartition);
				}
				
				if (new File ("/etc/sysctl.conf").exists()) {
					output = Command.systemCommand("cat /etc/sysctl.conf");
					if (output != null && !output.isEmpty() && !output.contains("vm.swappiness")) {
						Command.systemCommand("echo vm.swappiness=10 >> /etc/sysctl.conf");
						Command.systemCommand("sysctl -w vm.swappiness=10");
						logger.debug("Estableciedo uso de swap al 10%");
					}
				}
				
			}
		} catch (Exception ex) {
			logger.error("No se pudo activar la swap. Ex: {}", ex.getMessage());
		}
	}
}
