package com.whitebearsolutions.imagine.wbsairback.util;

import java.io.File;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.CloudManager;
import com.whitebearsolutions.imagine.wbsairback.disk.MultiPathManager;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.ZFSConfiguration;
import com.whitebearsolutions.util.Command;

public class VolumeDaemon {
	
	private final static Logger logger = LoggerFactory.getLogger(VolumeDaemon.class);
	
	/**
	 * Variable de control de nfs. Si algún volumen nfs no se pudo montar, no se lanzara el servicio nfs
	 */
	
	/**
	 * Al igual que un demonio, puede recibir start | stop | restart
	 * @param type
	 */
	public VolumeDaemon(String type, String ha) {
		if (type.contains("start"))
			this.initVolumes(ha);
		else if (type.contains("multipath"))
			this.initMultiPath();
		else
			this.stopVolumes(ha);
	}
	
	/**
	 * Por defecto, si no recibimos parámetro, inicializamos los volumenes
	 */
	public VolumeDaemon() {
		this.initVolumes(null);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("/var/www/webadministration/WEB-INF/classes/log4j.lcf");
			logger.info("*********************Init [wbsairback-volumes]***********************");
			logger.info("Iniciamos VolumeDaemon con {}", args[0]);
			if (args != null && args.length>1) {
				new VolumeDaemon(args[0], args[1]);
			} else if (args != null && args.length>0) {
				new VolumeDaemon(args[0], null);
			} else
				new VolumeDaemon("start", null);
		} catch(Exception _ex) {
			logger.error("Error on VolumeDaemon: " + _ex.getMessage());
		} finally {
			logger.info("*********************END [wbsairback-volumes]********************");
		}
	}
	
	/**
	 * Monta un sistema de ficheros
	 * @param fileSystem
	 * @param mountPoint
	 * @param type
	 * @param op
	 * @return
	 */
	public static boolean mountSystem(String fileSystem, String mountPoint, String type, String op) throws Exception{
		boolean _mounted = false;
		String _output = null;
		
		logger.info("Montando sistema [fs:{}, mountpoint:{}, tipo:{}, opciones:{}]", new Object[]{fileSystem, mountPoint, type, op});
		try {
			StringBuffer _sb = null;
			if (!VolumeManager.isSystemMounted(mountPoint)) {
				File _f = new File(mountPoint);
				if(!_f.exists()) {
					_f.mkdirs();
				}
				if (type.equals("zfs")) {
					Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(mountPoint);
					File mountDir = new File(mountPoint);
					if (!mountDir.exists())
						mountDir.mkdirs();
					
					StringBuffer _sbAux = new StringBuffer();
					String out = null;
					try {
						_sbAux.append("/sbin/zpool list | grep ");
						_sbAux.append(_lv.get("vg"));
						out = Command.systemCommand(_sbAux.toString());
					} catch (Exception ex) {
						logger.debug("El pool {} no estaba importado", _lv.get("vg"));
						out = null;
					}
					if (out != null && out.contains(_lv.get("vg"))) {
						logger.debug("Zpool esta montado, montando volumen ..");
						_mounted = ZFSConfiguration.mountFileSystem(_lv.get("vg"), _lv.get("lv"));
					} else {
						_sbAux = new StringBuffer();
						_sbAux.append("zpool import -f ");
						_sbAux.append(_lv.get("vg"));
						_sbAux.append(" > /dev/null 2>&1 && echo \"done\" || echo \"fail\"");
						logger.debug("Importando zpool {}  ...", _lv.get("vg"));
						out = Command.systemCommand(_sbAux.toString());
						logger.debug("Resultado de importacion: {}", out);
						if (out != null && out.contains("fail"))
							throw new Exception("Fail mounting zpool "+_lv.get("vg"));
						else if (out != null && out.contains("done")){
							_mounted = ZFSConfiguration.mountFileSystem(_lv.get("vg"), _lv.get("lv"));
						}
					}
					VolumeManager.mountLogicalVolumeSnapshotsOfVolume(_lv.get("vg"), _lv.get("lv"));
				} else if (type.equals("nfs")) {
					_sb = new StringBuffer();
					_sb.append("timeout ");
					_sb.append(WBSAirbackConfiguration.getTimeoutDfCommand());
					_sb.append(" ");
					_sb.append("/sbin/mount.nfs ");
					_sb.append(fileSystem);
					_sb.append(" ");
					_sb.append(mountPoint);
					_sb.append(" -o ");
					_sb.append(op);
					_sb.append(" > /dev/null 2>&1 && echo \"done\" || echo \"fail\"");
					_output = Command.systemCommand(_sb.toString());
					if (_output.contains("done"))
						_mounted = true;
				} else if (type.equals("cifs")) {
					_sb = new StringBuffer();
					_sb.append("timeout ");
					_sb.append(WBSAirbackConfiguration.getTimeoutDfCommand());
					_sb.append(" ");
					_sb.append("/sbin/mount.cifs ");
					_sb.append(fileSystem);
					_sb.append(" ");
					_sb.append(mountPoint);
					_sb.append(" -o ");
					_sb.append(op);
					_sb.append(" > /dev/null 2>&1 && echo \"done\" || echo \"fail\"");
					_output = Command.systemCommand(_sb.toString());
					if (_output.contains("done"))
						_mounted = true;
				} else {
					_sb = new StringBuffer();
					_sb.append("/bin/mount -t ");
					_sb.append(type);
					_sb.append(" ");
					_sb.append(fileSystem);
					_sb.append(" ");
					_sb.append(mountPoint);
					_sb.append(" -o ");
					_sb.append(op);
					_sb.append(" > /dev/null 2>&1 && echo \"done\" || echo \"fail\"");
					_output = Command.systemCommand(_sb.toString());
					if (_output.contains("done"))
						_mounted = true;
				}
				
				try {
					if (!type.equals("nfs")) {
						_sb = new StringBuffer();
						_sb.append("/bin/chmod 777 ");
						_sb.append(mountPoint);
						Command.systemCommand(_sb.toString());
						logger.info("Permisos de escritura aplicados sobre '{}'",mountPoint);
					}
				} catch (Exception ex) {
					logger.error("Error al aplicar permisos de escritura sobre '{}'",mountPoint);
				}
				
				if (_mounted)
					logger.info("Sistema [fs:{}, mountpoint:{}, tipo:{}, opciones:{}] MONTADO CORRECTAMENTE", new Object[]{fileSystem, mountPoint, type, op});
				else
					logger.info("Sistema [fs:{}, mountpoint:{}, tipo:{}, opciones:{}] no se pudo montar", new Object[]{fileSystem, mountPoint, type, op});
				
				return _mounted;
			}
			else {
				logger.info("'{}' ya estaba montado. No se hace nada.",mountPoint);
				if (type.equals("zfs")) {
					logger.info("'{}' es tipo zfs. Montamos sus snapshots, si los hay",mountPoint);
					Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(mountPoint);
					VolumeManager.mountLogicalVolumeSnapshotsOfVolume(_lv.get("vg"), _lv.get("lv"));
				}
				return true;
			}
		} catch (Exception ex) {
			logger.error("Error montando sistema '{}' en '{}'", fileSystem, mountPoint);
			throw new Exception("Error mounting filesystem "+fileSystem+" on "+mountPoint+": "+ex.getMessage());
		}
	}
	
	
	/**
	 * Desmonta un sistema
	 * @param mountPoint
	 * @throws Exception
	 */
	public void umountSystem(String mountPoint) throws Exception {
		if (VolumeManager.isSystemMounted(mountPoint)) {
			try {
				Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(mountPoint);
				VolumeManager.umountLogicalVolumeSnapshotsOfVolume(_lv.get("vg"), _lv.get("lv"));
			} catch (Exception ex) {
				logger.error("Error desmontando snapshots de {}. Ex: {}", mountPoint, ex.getMessage());
			}
			
			StringBuffer _sb = new StringBuffer();
			_sb.append("/bin/umount ");
			_sb.append(mountPoint);
			_sb.append(" > /dev/null 2>&1 && echo \"done\" || echo \"fail\"");
			String output = Command.systemCommand(_sb.toString());
	        if (output != null && output.contains("done"))
	        	logger.info("{} DESMONTADO CORRECTAMENTE", mountPoint);
	        else
	        	logger.info("{} no se pudo desmontar", mountPoint);
		} else {
			logger.info("{} ya estaba desmontado. No se hace nada", mountPoint);
		}
	}
	
	
	/**
	 * Lee FSTAB e intenta montar, si es que no están montados, los volúmenes
	 */
	public void initVolumes(String ha) {
		logger.info("Iniciamos proceso de incialización y montaje de volumenes");
		try {
			Boolean nfsProblems = false;
			
			logger.info("Inicialización de Multipath ...");
			this.initMultiPath();
			
			logger.info("Inicialización de pids de procesos de bacula ...");
			initBaculaPidFiles();
			
			try {
				logger.info("Inicialización discos Cloud ...");
				CloudManager.initCloudDisks();
			} catch (Exception ex) {
				logger.error("Algún error al inicializar discos cloud: "+ex.getMessage());
			}
			
			logger.info("Inicialización de lvm ...");
			StringBuffer _sb = new StringBuffer();
			_sb.append("vgchange -a y");
			try {
				Command.systemCommand(_sb.toString());
			} catch (Exception ex){
				logger.error("Error inicializando lvm: "+ex.getMessage());
			}
			
			try {
				logger.info("Inicialización de reiserfs ...");
				Command.systemCommand("modprobe reiserfs");
			} catch (Exception ex) {
				logger.error("Algún error al inicializar reiserfs: "+ex.getMessage());
			}
			
			
			Map<String, String[]> _fstab = readAnyFsTab();
			if (_fstab != null && _fstab.size() > 0) {
				for (String key : _fstab.keySet()) {
					String[] _item = _fstab.get(key);
					try {
						if (haCheckMount(ha, _item[1])) {
							boolean mounted = mountSystem(_item[0], _item[1], _item[2], _item[3]);
							if (!mounted) {
								if (ShareManager.isShareFromPath(_item[1]))
									nfsProblems = true;	
							}
						}
					} catch (Exception ex) {}
				}
				logger.info("Proceso de inicialización finalizado CORRECTAMENTE.");
			} else {
				logger.error("Error, no se encuentra ningún fstab, ni el habitual {}, ni el temporal {}", WBSAirbackConfiguration.getFileFstab(), WBSAirbackConfiguration.getFileTmpFstab());
			}
			
			// Si hay algún problema con algun volumen NFS, no arrancamos el servicio, en caso contrario, lo hacemos, pero con un delay
			if (VolumeManager.isSystemMounted("/rdata") || ha == null) {
				if (nfsProblems) {
					logger.warn("Atención, algún volumen NFS no se pudo montar, por lo que paramos si está activos y no lanzamos los servicios NFS");
					MailReport mr = new MailReport();
					mr.sendInfoMail(Watchdog.ERROR_SYSTEM, "Some shared volumes could not be mounted. Share services stopped", "WBSAIRBACK-WD SHARED VOLUMES error");
					ShareManager.stopAllShareServices();
					StringBuilder sb = new StringBuilder();
					sb.append("/tmp/");
					sb.append((Watchdog.ERROR_SYSTEM * 1000) + Watchdog.SYSTEM_VOLUME_MOUNT_NFS);
					sb.append(".wderror");
					File _f = new File(_sb.toString());
					_f.createNewFile();
				} else {
					logger.info("No hubo ningún problema con los volúmenes NFS. Incializamos los servicios...");
					ShareManager.startAllShareServices();
				}
			}
		} catch (Exception ex) {
			logger.error("ERROR en la inicialización de volumenes: "+ex.getMessage());
		}
	}
	
	/**
	 * Inicializa multipath si está activado
	 */
	public void initMultiPath() {
		try {
			if (MultiPathManager.isMultipathEnabled()) {
				logger.info("Multipath está activado, lo inicializamos");
				MultiPathManager.initializeMultipath();
			}
		} catch (Exception ex){
			logger.error("Algún error inicializando multipath: "+ex.getMessage());
		}
	}
	
/*	public void iscsiReLogin() {
		try {
			Command.systemCommand("iscsiadm -m node --logoutall=all");
			Command.systemCommand("iscsiadm -m node --loginall=all");
			logger.info("Iscis: Relogin ok");
		} catch (Exception ex) {
			logger.error("Iscsi: Algún error al hacer logout y login en los targets externos iscsi: "+ex.getMessage());
		}
	}*/
	
	/**
	 * Lee FSTAB e intenta desmontar los dispositivos asociados
	 */
	public void stopVolumes(String ha) {
		try {
			logger.info("Iniciamos proceso de desmontaje de volumenes");
			
			Map<String, String[]> _fstab = readAnyFsTab();
			
			if (_fstab != null && _fstab.size() > 0) {
				for (String key : _fstab.keySet()) {
					String[] _item = _fstab.get(key);
					try {
						if (haCheckUMount(ha, _item[1]))
							this.umountSystem(_item[1]);
					} catch (Exception ex) {
						logger.error("Error al intentar desmontar {}", _item[1]);
					}
				}
				logger.info("Proceso de desmontaje finalizado CORRECTAMENTE.");
			} else {
				logger.error("Error, no se encuentra ningún fstab, ni el habitual {}, ni el temporal {}", WBSAirbackConfiguration.getFileFstab(), WBSAirbackConfiguration.getFileTmpFstab());
			}
		} catch (Exception ex) {
			logger.error("ERROR en el desmontaje de volumenes: "+ex.getMessage());
		}
	}
	
	
	/**
	 * Si !ha ==> Se monta
	 * Si ha ==> si start y local ==> se monta, sino no se monta
	 * Si ha ==> si stop y local ==> no se desmonta, sino se desmonta
	 * @param ha
	 * @param mountPoint
	 * @return
	 * @throws Exception
	 */
	public boolean haCheckMount(String ha, String mountPoint) throws Exception {
		boolean mount = true;
		if (ha != null && ha.equals("ha") && !VolumeManager.isSystemMounted("/rdata")) {
			Map<String, String> vol = VolumeManager.getLogicalVolumeFromPath(mountPoint);
			String volGroup = vol.get("vg");
			if (VolumeManager.isLocalDeviceGroup(volGroup)) {
				logger.info("Cluster sin /rdata montado. '{}' SI es local => SI montamos", mountPoint);
				mount=true;
			} else { 
				logger.info("Cluster sin /rdata montado. '{}' NO es local => NO montamos", mountPoint);
				mount=false;
			}
		} else
			logger.info("No cluster, o no se ha llamado al script ha => SI montamos '{}'", mountPoint);
		return mount;
	}
	
	
	public boolean haCheckUMount(String ha, String mountPoint) throws Exception {
		boolean umount = true;
		if (ha != null && ha.equals("ha")) {
			Map<String, String> vol = VolumeManager.getLogicalVolumeFromPath(mountPoint);
			String volGroup = vol.get("vg");
			if (VolumeManager.isLocalDeviceGroup(volGroup)) {
				logger.info("Cluster. '{}' SI es local => NO desmontamos. Además, si está desmontado intentamos montarlo", mountPoint);
				try {
					if (!VolumeManager.isLogicalVolumeMounted(volGroup, vol.get("lv"))) {
						VolumeManager.mountLogicalVolume(volGroup, vol.get("lv"));
					}
				} catch (Exception ex) {
					logger.info("No se pudo montar el volumen: {} ", mountPoint);
				}
				umount=false;
			} else {
				logger.info("Cluster. '{}' NO es local => SI desmontamos", mountPoint);
				umount=true;
			}
		} else
			logger.info("No cluster, o no se ha llamado al script ha => SI desmontamos '{}'", mountPoint);
		return umount;
	}
	
	/**
	 * Lee el fichero fstab, ya sea el habitual o el temporal
	 * @return
	 * @throws Exception
	 */
	public Map<String, String[]> readAnyFsTab() throws Exception {
		Map<String, String[]> _fstab = null;
		File _f = new File(WBSAirbackConfiguration.getFileFstab());
		if (_f.exists() && !(HAConfiguration.isSlaveNode() && !VolumeManager.isSystemMounted("/rdata")) ) {
			_fstab = FileSystemManager.readFstab();
			logger.info("Leemos fstab de {}",WBSAirbackConfiguration.getFileFstab());
		} else if (new File(WBSAirbackConfiguration.getFileTmpFstab()).exists()) {
			_fstab = FileSystemManager.readTempFstab();
			logger.info("Leemos fstab de {}",WBSAirbackConfiguration.getFileTmpFstab());
		} else {
			logger.error("Error, no se encuentra ningún fstab, ni el habitual {}, ni el temporal de esclavo {}", WBSAirbackConfiguration.getFileFstab(), WBSAirbackConfiguration.getFileTmpFstab());
		}
		return _fstab;
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public static void initBaculaPidFiles() throws Exception {
		File fdir = null;
		File fsd = null;
		try {
			fdir = new File("/var/run/bacula-dir.9101.pid");
			if (!fdir.exists())
				 Command.systemCommand("echo '19101' > /var/run/bacula-dir.9101.pid");
			
			fsd = new File("/var/run/bacula-sd.9103.pid");
			if (!fsd.exists())
				 Command.systemCommand("echo '19103' > /var/run/bacula-sd.9103.pid");
			
			Command.systemCommand("/bin/chown bacula:bacula /var/run/bacula-dir.9101.pid");
			Command.systemCommand("/bin/chown bacula:bacula /var/run/bacula-sd.9103.pid");
			logger.info("Pids en /var/run correctamente inicializados");
		} catch (Exception ex) {
			logger.error("Algún error al inicializar pids de procesos de bacula en /var/run");
		}
		try {
			fdir = new File("/rdata/working/bacula-dir.9101.pid");
			if (!fdir.exists())
				 Command.systemCommand("echo '19101' > /rdata/working/bacula-dir.9101.pid");
			
			fsd = new File("/rdata/working/bacula-sd.9103.pid");
			if (!fsd.exists())
				 Command.systemCommand("echo '19103' > /rdata/working/bacula-sd.9103.pid");
			
			Command.systemCommand("/bin/chown bacula:bacula /rdata/working/bacula-dir.9101.pid");
			Command.systemCommand("/bin/chown bacula:bacula /rdata/working/bacula-sd.9103.pid");
			logger.info("Pids en /rdata/working correctamente inicializados");
		} catch (Exception ex) {
			logger.error("Algún error al inicializar pids de procesos de bacula en /rdata/working");
		}
	}
	
	
}
