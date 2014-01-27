package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.imagine.wbsairback.net.HACommClient;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class HAConfiguration {

	private static Configuration _c = null;
	private static NetworkManager _nm = null;
	private final static Logger logger = LoggerFactory.getLogger(HAConfiguration.class);
	
	/**
	 * Indica si un nodo esta en cluster, comprobando si rdata esta montado
	 * @return
	 */
	public static boolean isActiveNode() {
		try {
	    	return VolumeManager.isSystemMounted("/rdata");
	    } catch(Exception e) {
	    	return false;
	    }
	}
	
	
	/**
	 * Comprueba si un nodo esta en cluster y, ademas, es el esclavo según el fichero de configuracion ha
	 * @return
	 */
	public static boolean isSlaveNode() {
		try {
			if (inCluster())
				return getStatus().equals("slave");
			else
				return false;
		} catch (Exception ex) {
			return false;
		}
	}
	
	
	/**
	 * Indica si un nodo esta en cluster a partir de su fichero de configuracion ha 
	 * @return
	 */
	public static boolean inCluster() {
		try {
			if  (getStatus().equals("master") || getStatus().equals("slave"))
				return true;
			else
				return false;
		} catch (Exception ex) {
			return false;
		}
	}
	
	
	/**
	 * Devuelve el estado de un nodo (standalone, slave, master)
	 * @return
	 * @throws Exception
	 */
	public static String getStatus() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getFileImagineHaRequest());
		if(!_f.exists()) {
			return "standalone";
		}
		
		loadHAConfiguration();
		
		if(_c.checkProperty("cluster.status", "master")) {
    		return "master";
    	} else if(_c.checkProperty("cluster.status", "slave")) {
    		return "slave";
    	} else if(_c.checkProperty("cluster.request", "request") || _c.checkProperty("cluster.request", "break")) {
    		return "request";
    	} else {
    		return "standalone";
    	}
	}

	
	/**
	 * Restablece la configuracion de un nodo para dejarlo independiente de ningún cluster
	 * @throws Exception
	 */
	public static void setStandalone(String type) throws Exception {
		logger.info("#### Cluster: Reestableciendo modo standalone #######");
		String _exMessage = "";
		// Cargamos configuracion ha
		loadHAConfiguration();
		logger.info("Configuracion ha cargada");
		
		// Establecemos la configuracion de cman como 'none'
		logger.info("Estableciendo configuracion de CMAN como standalone y parando el servicio ..");
		DrbdCmanConfiguration.cmanSetClusterConfiguration(DrbdCmanConfiguration.FENCE_NONE, new HashMap<String, String>());
		logger.info("Parando los servicios asociados a cman");
		DrbdCmanConfiguration.killCmanAssociatedProcesses();
        
		// Paramos los servicios y eliminamos del inicio los de cluster
		logger.info("Parando todos los servicios incluyendo heartbeat y drbd (si esta activo)");
        ServiceManager.fullStop(ServiceManager.WATCHDOG);
		ServiceManager.remove(ServiceManager.HEARTBEAT);
		try {
			ServiceManager.stop(ServiceManager.POSTGRES);
			logger.info("Servicio postgre parado correctamente");
		} catch (Exception ex) {
			logger.error("Atencion, no se pudo detener el servico postgre");
		}
        ServiceManager.fullStop(ServiceManager.BACULA_SD);
		ServiceManager.fullStop(ServiceManager.BACULA_FD);
		ServiceManager.fullStop(ServiceManager.BACULA_DIR);
        ServiceManager.fullStop(ServiceManager.ISCSI_TARGET);
        ServiceManager.fullStop(ServiceManager.NFS);
        ServiceManager.fullStop(ServiceManager.SAMBA);
        ServiceManager.fullStop(ServiceManager.FTP);
        ServiceManager.fullStop(ServiceManager.VTL);
        ServiceManager.remove(ServiceManager.CLUSTER);
        try {
        	Command.systemCommand("insserv -r wbsairback-volumes-multipath");
        	logger.info("Eliminado del inico el script wbsairback-volumes-multipath");
        } catch (Exception ex) {
        	logger.error("No se pudo eliminar del inico el script wbsairback-volumes-multipath");
        }
        
        // Eliminamos los ficheros de heartbeat
        HeartBeatConfiguration.heartbeatRemoveInitFiles();
        logger.info("Archivos de heartbeat eliminados correctamente");
        
        if (_c.hasProperty("lun")) {
        	rdataRecoverToOriginalDisk(_c.getProperty("lun"));
        	logger.info("Proceso de recuperacion de /rdata a disco original finalizado.");
        } else {
        	// Eliminamos la configuracion de DRBD
            DrbdCmanConfiguration.drbdUnconfigure();
            logger.info("Configuracion de drbd eliminada");
        }
        // Desmontamos rdata
        rdataForceUmount();
       
        // Restablecemos rdata en /etc/fstab
        rdataReAddToEtcFstab();
     	logger.info("/rdata reanyadido a etc/fstab");
     	
        
        // Ahora montamos rdata
        try {
        	if (!VolumeManager.isSystemMounted("/rdata"))
        		Command.systemCommand("mount -t ext3 "+systemDevice()+"3 /rdata");
          	logger.info("/rdata montado en disco original");
        } catch (Exception ex){
        	_exMessage+="Unable to mount rdata after stopping services. ";
        	logger.error("Atencion, no se pudo montar /rdata en el disco original");
        }
        
        // Tras montar rdata, podemos reinicializar los servicios
        logger.info("Reiniciando servicios ...");
        _exMessage += reinitClusterImpliedServices();
        logger.info("Servicios reiniciados");
        
        // Recuperamos el wbsairback-volumes para el inicio de la maquina
	    Command.systemCommand("/usr/sbin/update-rc.d wbsairback-volumes defaults 89 19");
	    logger.info("wbsairback-volumes reanyadido al inicio (rc3.d)");
	    
		// Eliminamos todos los ficheros de configuracion de cluster
    	deleteHaConfigurationFiles();
    	logger.info("Archivos de cluster eliminados");
    	
        // Restablecemos la configuracion de red
        _nm.setStandaloneNetwork();
        logger.info("Red configurada como standalone");
        
        // Eliminamos ficheros xml asociados del cluster y re-construimos fstab en el nodo esclavo
        try {
        	HACommClient.forgetRequest();
        	if (type != null && type.equals("slave")) {
        		buildSlaveFstab();
        	}
        	Command.systemCommand("/etc/init.d/wbsairback-volumes restart");
        	logger.info("wbsairback-volumes restart ejecutado");
        } catch (Exception ex){
        	logger.error("Se produjo algún error al reincializar wbsairback-volumes ex: "+ ex.getMessage());
        	_exMessage+="Some error restarting wbsairback-volumes. ";
        }
        
        // Reinicializamos el watchdobg  y el airback-volumes
        try {
        	ServiceManager.initialize(ServiceManager.WATCHDOG);
        	logger.info("Watchdog lanzado correctamente");
        } catch (Exception ex) {
        	logger.info("Se produjo algún error al reincializar watchdog ex: "+ ex.getMessage());
        	_exMessage+="Some Error restarting watchdog. ";
        }
        
        try {
			Command.systemCommand("rm -r "+DrbdCmanConfiguration.tmpRdata);
			logger.info("Ejecutado: rm -r "+DrbdCmanConfiguration.tmpRdata);
		} catch (Exception ex){}
        
        if (!_exMessage.equals("")) {
        	logger.info("Cluster deshecho, pero se produjeron errores" + _exMessage);
        	throw new Exception("Done. But errors found: "+_exMessage);
        } else {
        	logger.info("### Cluster deshecho CORRECTAMENTE ###");
        }
	}
	
	
	/**
	 * Establece la configuracion de un nodo esclavo en el cluster, en el proceso, se llegaria aqui tras el paso1 del master
	 * @param virtual_address
	 * @param master_address
	 * @param _fenceType
	 * @param _fenceAttributes
	 * @throws Exception
	 */
	public static void setSlave(String virtual_address, String master_address, int _fenceType, Map<String, String> _fenceAttributes) throws Exception {
		logger.info("#### Cluster: Configuracion de esclavo #######");
		// Cargamos configuracion de ha
		loadHAConfiguration();
		logger.info("Configuracion ha cargada");
		
		// Si tenemos lun, tenemos un cluster de lun compartida
		String deviceLun = _c.getProperty("lun");
		if (deviceLun != null)
			logger.info("Tenemos cluster de tipo lun compartida en "+deviceLun);
		else
			logger.info("Tenemos cluster de tipo DRBD");
		
		// Establecemos la informacion del nodo y configuramos la red
        nodeInfoWrite("slave");
        logger.info("Informacion de nodo escrita");
        _nm.setSlaveNetwork(master_address, _c.getProperty("slave.iface"));
        logger.info("Configurada la red");
        
        // Creamos la configuracion de cman
        logger.info("Configurando cman ...");
        DrbdCmanConfiguration.cmanInit(_fenceType, _fenceAttributes);
        logger.info("Configurado e iniciado cman");
        
        // Paramos todos los servicios implicados en el cluster, Paramos rdata y la quitamos de etc/fstab
        logger.info("Parando servicios ...");
        servicesClusterImpliedStop();
        logger.info("Servicios detenidos");
        rdataForceUmount();
        logger.info("Rdata Desmontado");
        rdataRemoveFromEtcFstab();
        logger.info("Rdata eliminado de /etc/fstab");
        
        String slaveIface = _nm.getInterface(0);
        if (_c.hasProperty("slave.iface"))
        	slaveIface = _c.getProperty("slave.iface");
        	
    	// Configuramos drbd
        if (deviceLun == null || deviceLun.equals("")) {
        	DrbdCmanConfiguration.drbdConfigureAll("slave", _nm.getStringAddress(slaveIface), master_address);
        	logger.info("Configuracion drbd ejecutada");
        } else {
        	ServiceManager.initialize(ServiceManager.ISCSI_INITIATOR);
        	logger.info("Iniciado iniciador ISCSI");
        }
        
        // Configuramos heartbeat
        logger.info("Configurando heartbeat ...");
        HeartBeatConfiguration.heartbeatConfigureAll("master", slaveIface, virtual_address, deviceLun, slaveIface);
        logger.info("Configuracion heartbeat creada");
        
        // Si es un cluster DRBD lanzamos el proceso de sincronizacion, en caso contrario, lanzamos solamente la ultima secuencia
        Runnable r = new Runnable() {
			public void run() {
				try {
					waitToSynchronizeSlave();
				} catch (Exception _ex) {
					_ex.printStackTrace(); 
				}
			}
		};
		Thread internalThread = new Thread(r);
		internalThread.start();
	}
	
	
	/**
	 * Establece el primer paso de la configuracion de un nodo maestro, en el que se configuran todos los servicios, a falta de lanzar la sincronizacion drbd (si la hay)
	 * @param virtual_address
	 * @param slave_address
	 * @param _fenceType
	 * @param _fenceAttributes
	 * @throws Exception
	 */
	public static void setMasterStage1(String virtual_address, String slave_address, int _fenceType, Map<String, String> _fenceAttributes) throws Exception {
		logger.info("#### Cluster: Fase 1 en maestro #######");
		// Cargamos configuracion de ha
		loadHAConfiguration();
		logger.info("Configuracion ha cargada");
		
		// Antes de nada, enviamos el fstab actual al esclavo
		HACommClient.sendFsTab();		
		logger.info("fstab enviado al esclavo");
		
		// Si tenemos lun, tenemos un cluster de lun compartida
		String deviceLun = _c.getProperty("lun");
		if (deviceLun != null)
			logger.info("Tenemos cluster de tipo lun compartida en "+deviceLun);
		else
			logger.info("Tenemos cluster de tipo DRBD");
		
		// Establecemos la informacion del nodo y configuramos la red
		nodeInfoWrite("master");
		logger.info("Informacion de nodo escrita");
        _nm.setMasterNetwork(virtual_address, slave_address, _c.getProperty("master.iface"));
        logger.info("Configurada la red");
        
        // Creamos la configuracion de cman y actualizamos los storages con la nueva configuracion de red
        logger.info("Configurando cman ...");
        DrbdCmanConfiguration.cmanInit(_fenceType, _fenceAttributes);
        logger.info("Configurado e iniciado cman");
        StorageManager.updateAllStoragePublicAddress();
        logger.info("Actualizada la direccion publica de los storages");
      
        // Paramos todos los servicios implicados en el cluster, Paramos rdata y la quitamos de etc/fstab
        logger.info("Parando servicios ...");
        servicesClusterImpliedStop();
        logger.info("Servicios detenidos");
        logger.info("Copiando contenido de rdata en directorio temporal ...");
        if (deviceLun == null || deviceLun.equals("")) {
        	DrbdCmanConfiguration.buildTmpRdata();
        }
        logger.info("Copia temporal de rdata completa.");
        rdataForceUmount();
        logger.info("Rdata Desmontado");
        rdataRemoveFromEtcFstab();
        logger.info("Rdata eliminado de /etc/fstab");
        
        // Configuramos drbd
        if (deviceLun == null || deviceLun.equals("")) {
        	DrbdCmanConfiguration.drbdConfigureAll("master", _nm.getStringAddress(_c.getProperty("master.iface")), slave_address);
        	logger.info("Configuracion drbd ejecutada");
        } else {
        	ServiceManager.initialize(ServiceManager.ISCSI_INITIATOR);
        	logger.info("Iniciado iniciador ISCSI");
        }
        
        // Configuramos heartbeat
        logger.info("Configurando heartbeat ...");
        HeartBeatConfiguration.heartbeatConfigureAll("slave", _c.getProperty("master.iface"), virtual_address, deviceLun, _c.getProperty("master.iface"));
        logger.info("Configuracion heartbeat creada");
        logger.info("### Final del paso 1 en el maestro ###");
	}

	
	/**
	 * Lanza el paso 2 del maestro, basicamente, lanza la sincronizacion drbd
	 * @param virtual_address
	 * @param slave_address
	 * @throws Exception
	 */
	public static void setMasterStage2(String virtual_address, String slave_address) throws Exception {
		logger.info("#### Cluster: Fase 2 en maestro #######");
		if (_c == null)
			loadHAConfiguration();
		if (!_c.hasProperty("lun")) {
			try {
				ServiceManager.fullStop(ServiceManager.DRBD);
				ServiceManager.start(ServiceManager.DRBD);
				Command.systemCommand("/sbin/drbdsetup /dev/drbd0 primary -o");
				logger.info("El master indico que el disco drbd0 es suyo. Comienza la sincronización ...");
			} catch (Exception ex) {
				throw new Exception("Error configuring drbd stage 2 on master");
			}
			Thread.sleep(500);
		}
		Runnable r = new Runnable() {
			public void run() {
				try {
					waitToSynchronizeMaster();
				} catch (Exception _ex) {
					_ex.printStackTrace(); 
				}
			}
		};
		Thread internalThread = new Thread(r);
		internalThread.start();
	}
	
	
	/**
	 * Proceso encargado de monitorizar la sincronizacion drbd en el maestro y llamar a la secuencia de comandos final al terminar
	 * @throws Exception
	 */
	private static void waitToSynchronizeMaster() throws Exception {
		String status = new String();
		if (_c == null)
			loadHAConfiguration();
		if (!_c.hasProperty("lun")) {
	        for(int i = 45000; !status.contains("UpToDate/UpToDate"); --i) {
	        	if(i <= 0) {
	        		try {
	        			ServiceManager.stop(ServiceManager.DRBD);
	        		} catch(Exception _ex) {}
	        		throw new Exception("incorrect DRBD device status (master) [" + status + "] after 24 hours");
	            }
	        	status = DrbdCmanConfiguration.drbdGetSynchronizationStatus();
	            Thread.sleep(2000);
	        }
	        DrbdCmanConfiguration.setDrbdDiskAsRdata();
	        lastClusterStep(true);
	        try {
	        	logger.info("Borramos directorio temporal de rdata ...");
	        	Command.systemCommand("rm -r "+DrbdCmanConfiguration.tmpRdata);
	        	logger.info("Directorio temporal eliminado");
	        } catch (Exception ex) {
	        	logger.warn("Atencion, no se pudo borra el directorio temporal "+DrbdCmanConfiguration.tmpRdata);
	        }
	        ServiceManager.initialize(ServiceManager.WATCHDOG);
        	logger.info("Watchdog lanzado ...");
		} else {
			lastClusterStep(false);
			ServiceManager.initialize(ServiceManager.WATCHDOG);
        	logger.info("Watchdog lanzado ...");
		}
        
        logger.info("Lanzamos servicios asociados a CMAN ...");
        DrbdCmanConfiguration.restartCmanServices();
		logger.info("### CLUSTER configurado satisfactoriamente en el maestro ###");
	}
	
	
	/**
	 * Proceso encargado de monitorizar la sincronizacion drbd en el esclavo y llamar a la secuencia de comandos final al terminar
	 * @throws Exception
	 */
	private static void waitToSynchronizeSlave() throws Exception {
		String status = new String();
		if (_c == null)
			loadHAConfiguration();
		if (!_c.hasProperty("lun")) {
			logger.info("Esperamos un poco para que el maestro diga que él es el primario ...");
			Command.systemCommand("/sbin/drbdadm up rdata");
			logger.info("Recurso rdata levantado. Comienza la sincronizacion ...");
			Thread.sleep(5000);
	        for(int i = 45000; !status.contains("UpToDate/UpToDate"); --i) {
	        	if(i <= 0) {
	        		try {
	        			ServiceManager.stop(ServiceManager.DRBD);
	        		} catch(Exception _ex) {}
	        		throw new Exception("incorrect DRBD device status (slave) [" + status + "] after 24 hours");
	            }
	        	status = DrbdCmanConfiguration.drbdGetSynchronizationStatus();
	            Thread.sleep(2000);
	        }
	        lastClusterStep(true);
	        ServiceManager.initialize(ServiceManager.WATCHDOG);
        	logger.info("Watchdog lanzado ...");
		} else {
			lastClusterStep(false);
        	logger.info("Esperamos 5s ...");
            Thread.sleep(5000);
            logger.info("Relanzamos heartbeat ...");
            Command.systemCommand("/etc/init.d/heartbeat restart");
            ServiceManager.initialize(ServiceManager.WATCHDOG);
        	logger.info("Watchdog lanzado ...");
		}
        
        logger.info("Lanzamos servicios asociados a CMAN ...");
        DrbdCmanConfiguration.restartCmanServices();
		logger.info("### CLUSTER configurado satisfactoriamente en el esclavo ###");
	}
	
	
	/**
	 * Lanza la última secuencia de comandos que finaliza la creacion de un cluster, se ha de lanzar tanto en el esclavo como en el maestro
	 * @param drbd
	 * @throws Exception
	 */
	private static void lastClusterStep(boolean drbd) throws Exception {
		
		logger.info("Parando y quitando del inicio (rc3.d) los servicios ...");
		Command.systemCommand("insserv -r avahi-daemon");
		Command.systemCommand("/etc/init.d/avahi-daemon stop");
		logger.info("Avahi daemon parado y quitado");
		
		ServiceManager.stop(ServiceManager.WATCHDOG);
        ServiceManager.remove(ServiceManager.BACULA_DIR);
        ServiceManager.remove(ServiceManager.BACULA_SD);
        ServiceManager.remove(ServiceManager.BACULA_FD);
        if(ServiceManager.isRunning(ServiceManager.POSTGRES)) {
        	try {
        		Command.systemCommand("/etc/init.d/postgresql stop");
        		logger.info("Postgresql parado correctamente");
        	} catch (Exception ex) {
        		logger.error("Atencion, no se pudo detener postgresql, ex:"+ex.getMessage());
        	}
		}
        Command.systemCommand("insserv -r postgresql");
        ServiceManager.remove(ServiceManager.ISCSI_TARGET);
        ServiceManager.remove(ServiceManager.NFS);
        ServiceManager.remove(ServiceManager.SAMBA);
        ServiceManager.remove(ServiceManager.FTP);
        ServiceManager.remove(ServiceManager.VTL);
        Command.systemCommand("/usr/sbin/update-rc.d -f wbsairback-volumes remove");
        logger.info("Quitados del inicio (rc3.d) los servicios de bacula, postgre, iscsi-target, nvs, samba, ftp, vtl y wbsairback-volumes");
        
        Command.systemCommand("insserv wbsairback-volumes-multipath");
        logger.info("Anyadido al inicio (rc3.d) wbsairback-volumes-multipath");
        
        if (drbd) {
        	//ServiceManager.initialize(ServiceManager.WBSAIRBACK_DRBD);
        	ServiceManager.initialize(ServiceManager.DRBD);
        }
        Command.systemCommand("insserv heartbeat");
        Command.systemCommand("insserv cman");
        logger.info("Introducimos heartbeat  y cman al inicio (rc3.d)");
        
        logger.info("Lanzamos heartbeat ");
        Command.systemCommand("/etc/init.d/heartbeat restart");
        logger.info("Heartbeat lanzado ...");
        
        if (drbd) {
        	Thread.sleep(10000);
        	Command.systemCommand("/etc/init.d/heartbeat restart");
        }
        ServiceManager.start(ServiceManager.MONGODB);
	}

	
	/**
	 * Para todos los servicios que pueden entrar en conflicto durante la configuracion del cluster
	 * @throws Exception
	 */
	public static void servicesClusterImpliedStop() throws Exception{
	    try {
	        ServiceManager.fullStop(ServiceManager.WATCHDOG);
	        ServiceManager.fullStop(ServiceManager.HEARTBEAT);
	        ServiceManager.fullStop(ServiceManager.DRBD);
	        ServiceManager.fullStop(ServiceManager.BACULA_DIR);
	        ServiceManager.fullStop(ServiceManager.BACULA_FD);
	        ServiceManager.fullStop(ServiceManager.BACULA_SD);
	        if(ServiceManager.isRunning(ServiceManager.POSTGRES)) {
	        	try {
	        		Command.systemCommand("/etc/init.d/postgresql stop");
	        		logger.info("Postgresql parado correctamente");
	        	} catch (Exception ex) {
	        		logger.error("Atencion, no se pudo detener postgresql, ex:"+ex.getMessage());
	        	}
			}
	        ServiceManager.fullStop(ServiceManager.ISCSI_TARGET);
	        ServiceManager.fullStop(ServiceManager.NFS);
	        ServiceManager.fullStop(ServiceManager.SAMBA);
	        ServiceManager.fullStop(ServiceManager.FTP);
	        ServiceManager.fullStop(ServiceManager.VTL);
	        ServiceManager.fullStop(ServiceManager.MONGODB);
	    } catch (Exception ex) {
	    	throw new Exception ("Error stopping slave services");
	    }
	}
	
	
	/**
	 * Intenta forzar el desmontaje de /rdata
	 * @throws Exception
	 */
	public static void rdataForceUmount() throws Exception {
		forceUmount("/rdata");
	}
	
	public static void forceUmount(String path) throws Exception {
		if(VolumeManager.isSystemMounted(path)) {
			try {
				VolumeManager.umountSystemVolume(path);
				logger.info("Desmontado {}",path);
			} catch (Exception ex) {
				logger.warn("No se pudo desmontar {}, intentando forzarlo ...",path);
				if (VolumeManager.isSystemMounted(path)) {
					String status = Command.systemCommand("fuser -m "+path+" | grep [0-9] >> /dev/null && /bin/echo yes || /bin/echo no");
					if(!status.startsWith("no")) {
						Command.systemCommand("/bin/kill -9 `fuser -m "+path+" | tr -d [a-z] | tr -s \" \"` 2> /dev/null");
					}
					try {
						VolumeManager.umountSystemVolume(path);
						logger.info("Desmontado {}",path);
					} catch (Exception ex2) {
						if (VolumeManager.isSystemMounted("/rdata")) {
							logger.error("Imposible desmontar {}"+path);
							throw new Exception("Error umounting "+path);
						}
					}
				}
			}
		}
	}
	
	
	
	/**
	 * Anyade rdata a /etc/fstab si estaba comentada su linea
	 * @throws Exception
	 */
	public static void rdataReAddToEtcFstab() throws Exception {
		BufferedReader _reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/etc/fstab"))));
	    StringBuilder _sb = new StringBuilder();
	    String line;
		while((line = _reader.readLine()) != null) {
			if(!line.contains(systemDevice() + "3")) {	
				_sb.append(line + "\n");
			} else {
	            _sb.append(line.replaceAll("#", "") + "\n");
			}
		}
	    
		FileOutputStream _fos = new FileOutputStream("/etc/fstab");
	    _fos.write(_sb.toString().getBytes());
	    _fos.close();
	}
	
	
	/**
	 * Comenta la linea de /rdata del /etc/fstab
	 * @throws Exception
	 */
	public static void rdataRemoveFromEtcFstab() throws Exception {
		BufferedReader _reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/etc/fstab"))));
        StringBuilder _sb = new StringBuilder();
        String line;
		while((line = _reader.readLine()) != null) {
			if(line.startsWith("#") || !line.contains("" + systemDevice() + "3")) {	
				_sb.append(line + "\n");
			} else {
	            _sb.append("#"+line+"\n");
			}
		}
		FileOutputStream _fos = new FileOutputStream("/etc/fstab");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
	}
	
	
	/**
	 * Escribe el nombre del nodo ha
	 * @param node
	 * @throws Exception
	 */
	public static void nodeInfoWrite(String node) throws Exception {
		StringBuilder _sb = new StringBuilder();
        _sb.append(node);
        
        FileOutputStream _fos = new FileOutputStream("/etc/ha.d/nodeinfo");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
	}
	
	
	/**
	 * Devuelve el systemDevice
	 */
	public static String systemDevice() throws Exception {
		try {
			String _output = Command.systemCommand("/bin/mount | /bin/grep -w / |  awk '{ print $1; }' | tr -d [0-9]");
			if(_output.isEmpty()) {
				return "unknown";
			} else {
				StringTokenizer _st = new StringTokenizer(_output, "\n");
				return _st.nextToken().replaceAll("\n", "");
			}
		} catch(Exception _ex) {
			return "unknown";
	    }
	}
	
	
	/**
	 * Carga la configuracion de ha
	 * @throws Exception
	 */
	public static void loadHAConfiguration() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getFileImagineHaRequest());
		if(!_f.exists()) {
			throw new Exception("no se encuentra el fichero de configuraci&oacute;n");
		}
		_c = new Configuration(_f);
		
		if (_nm == null)
			_nm = new NetworkManager(_c);
	}
	
	
	/**
	 * Reinicializa los servicios eliminados tras una configuracion de cluster
	 * @return
	 * @throws Exception
	 */
	public static String reinitClusterImpliedServices() throws Exception {
		String _exMessage = "";
		try {
        	ServiceManager.initialize(ServiceManager.POSTGRES);
        } catch (Exception ex){
        	_exMessage+="Unable to start postgresql service. ";
        }
        try {
        	ServiceManager.initialize(ServiceManager.BACULA_DIR);
        } catch (Exception ex){
        	_exMessage+="Unable to start bacula-dir service. ";
        }
        try {
        	ServiceManager.initialize(ServiceManager.BACULA_SD);
		} catch (Exception ex){
			_exMessage+="Unable to start bacula-sd service. ";
		}
        try {
        	ServiceManager.initialize(ServiceManager.BACULA_FD);
		} catch (Exception ex){
			_exMessage+="Unable to start bacula-fd service. ";
		}
        
        StorageManager.updateAllStoragePublicAddress();
        try { 
        	ServiceManager.initialize(ServiceManager.ISCSI_TARGET);
		} catch (Exception ex){
			_exMessage+="Unable to start iscsi-target services. ";
		}
        try {
        	ServiceManager.initialize(ServiceManager.ISCSI_INITIATOR);
        } catch (Exception ex){
        	_exMessage+="Unable to start iscsi-initiator services. ";
        }
        try {
        	ServiceManager.start(ServiceManager.NFS);
        } catch (Exception ex){
        	_exMessage+="Unable to start nfs service. ";
        }
        
        try {
        	ServiceManager.start(ServiceManager.SAMBA);
        } catch (Exception ex){
        	_exMessage+="Unable to start samba service. ";
        }
        try {
        	ServiceManager.initialize(ServiceManager.FTP);
        } catch (Exception ex){
        	_exMessage+="Unable to start ftp service. ";
        }
        try {
        	ServiceManager.initialize(ServiceManager.VTL);
        } catch (Exception ex) {};
        return _exMessage;
	}
	
	
	/**
	 * Elimina los ficheros generados por una configuracion de cluster
	 * @throws Exception
	 */
	public static void deleteHaConfigurationFiles() throws Exception {
		File _f = new File("/etc/ha.d/nodeinfo");
    	if (_f.exists())
    		_f.delete();
        
        _f = new File("/etc/ha.d/ha.cf");
        if (_f.exists())
        	_f.delete();
        
        _f = new File("/etc/ha.d/haresources");
        if (_f.exists())
        	_f.delete();
        
        _f = new File("/etc/drbd.conf");
        if (_f.exists())
        	_f.delete();
	}
	
	
	/**
	 * Devuelve el disco (en formato /dev/sdx) asociado a un cluster de tipo lun compartida
	 * @return
	 * @throws Exception
	 */
	public static String getConfiguredClusterDisk() throws Exception {
		String disk = null;
		if ( new File(WBSAirbackConfiguration.getFileImagineHaRequest()).exists()) {
			loadHAConfiguration();
			String lunId = _c.getProperty("lun");
			if (lunId != null && !lunId.equals("")) {
				disk = VolumeManager.findDevSdFromDeviceId(lunId);
			}
		}
		return disk;
	}
	
	
	/**
	 * Crea el sistema de ficheros ext3 en la lun que recibe como parametros, para que heartbeat pueda montar satisfactoriamente rdata
	 * @throws Exception
	 */
	public static void createRdataSharedLun(String lunId) throws Exception {
		String tmpRdata = "/rdataTmp";
		logger.info("Inicio de creacion de /rdata lun en "+lunId);
		try {
			Command.systemCommand("mkfs.ext3 -F "+lunId+" >> /dev/null");
			logger.info("Ejecutado: mkfs.ext3 -F "+lunId);
		} catch (Exception ex) {
			logger.info("Error al ejecutar: mkfs.ext3 -F "+lunId+ " ex:"+ex.getMessage());
			throw new Exception("Error trying to generate new ext3 filesystem on: "+lunId+"-- ex:"+ex.getMessage());
		}
		
		Command.systemCommand("mkdir -p "+tmpRdata);
		logger.info("Ejecutado: mkdir -p "+tmpRdata);
		
		try {
			Command.systemCommand("rm -r "+tmpRdata+"/*");
			logger.info("Ejecutado: rm -r "+tmpRdata+"/*");
		} catch (Exception ex){}
		
		try {
			forceUmount(tmpRdata);
		} catch (Exception ex){}
		
		try {
			Command.systemCommand("mount "+lunId+" "+tmpRdata);
			logger.info("Ejecutado: mount "+lunId+" "+tmpRdata);
		} catch (Exception ex) {
			logger.error("Error al ejecutar : mount "+lunId+" "+tmpRdata+" ex:"+ex.getMessage());
			throw new Exception("Error mounting new lun: "+lunId+"-- ex:"+ex.getMessage());
		}
		
		try {
			logger.info("Copiando datos ...");
			Command.systemCommand("cp -p -r /rdata/* "+tmpRdata);
			logger.info("Ejecutado: cp -p -r /rdata/* "+tmpRdata);
		} catch (Exception ex) {
			logger.error("Error al ejecutar: cp -p -r /rdata/* "+tmpRdata+" ex:"+ex.getMessage());
			throw new Exception("Error copying data to new shared lun: "+lunId+"-- ex:"+ex.getMessage());
		}
		
		try {
			forceUmount(tmpRdata);
		} catch (Exception ex){
			logger.error("Error umounting "+tmpRdata);
		}
		
		try {
			Command.systemCommand("rm -r "+tmpRdata);
			logger.info("Ejecutado: rm -r "+tmpRdata);
		} catch (Exception ex){
			logger.error("Error deleting "+tmpRdata);
		}
	}
	
	
	public static void rdataRecoverToOriginalDisk(String lunId) throws Exception {
		String tmpRdata = "/rdataRecover";
		logger.info("Inicio de recuperacion de /rdata almacenada en "+lunId+" a disco original");
		try {
			String originalDisk = systemDevice() + "3";
			
			if(!VolumeManager.isSystemMounted("/rdata")) {
				try {
					Command.systemCommand("mount "+lunId+" /rdata");
					logger.info("Ejecutado: mount "+lunId+" /rdata");
				} catch (Exception ex) {
					logger.error("Atencion, no se pudo montar la lun '{}' que deberia contener los datos de /rdata para traspasarlos al disco original", lunId);
					throw new Exception("Error mounting lun "+lunId+" on /rdata");
				}
			}
				
			File f = new File("/rdata");
			if (!f.exists() || f.listFiles() == null || f.listFiles().length <=0) {
				logger.error("Atencion, la lun '{}' que deberia contener los datos de /rdata para traspasarlos al disco original, parece estar vacia!!", lunId);
				throw new Exception("Error, lun "+lunId+" whith system data is empty!!");
			}
				
			
			Command.systemCommand("mkdir -p "+tmpRdata);
			logger.info("Ejecutado: mkdir -p "+tmpRdata);
			
			
			try {
				Command.systemCommand("rm -r "+tmpRdata+"/*");
				logger.info("Ejecutado: rm -r "+tmpRdata+"/*");
			} catch (Exception ex){}
			
			try {
				forceUmount(tmpRdata);
			} catch (Exception ex){}
			
				try {
				Command.systemCommand("mount "+originalDisk+" "+tmpRdata);
				logger.info("Ejecutado: mount "+originalDisk+" "+tmpRdata);
			} catch (Exception ex) {
				logger.error("Error al ejecutar : mount "+originalDisk+" "+tmpRdata+" ex:"+ex.getMessage());
				throw new Exception("Error mounting disk: "+originalDisk+"-- ex:"+ex.getMessage());
			}
			
			try {
				logger.info("Copiando datos ...");
				Command.systemCommand("cp -p -r /rdata/* "+tmpRdata);
				logger.info("Ejecutado: cp -p -r /rdata/* "+tmpRdata);
			} catch (Exception ex) {
				logger.error("Error al ejecutar: cp -p -r /rdata/* "+tmpRdata+" ex:"+ex.getMessage());
				throw new Exception("Error copying data to new shared lun: "+lunId+"-- ex:"+ex.getMessage());
			}
			
			try {
				forceUmount(tmpRdata);
			} catch (Exception ex){
				logger.error("Error umounting "+tmpRdata);
			}
			
			try {
				Command.systemCommand("rm -r "+tmpRdata);
				logger.info("Ejecutado: rm -r "+tmpRdata);
			} catch (Exception ex){
				logger.error("Error deleting "+tmpRdata);
			}
			
		} catch (Exception ex) {
			logger.error("No se pudo recuperar /rdata a la particion original "+ex.getMessage());
		}
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	public static void buildSlaveFstab() throws Exception {
		logger.info("Reconstruyendo Fstab en esclavo ...");
		
		try {
			Map<String, String[]> _fstab = FileSystemManager.readOriginalFstab();
			if (_fstab != null && _fstab.size()>0) {
				for (String key : _fstab.keySet()) {
					String[] _item = _fstab.get(key);
					if (_item[2].equals("xfs") || _item[2].equals("zfs")) {
						Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(_item[1]);
						FileSystemManager.removeMountFromFstab(_lv.get("vg"), _lv.get("lv"));
						logger.info("Eliminamos del fstab original: "+_lv.get("vg")+"/"+_lv.get("lv"));
					}
				}
			}
			logger.info("Eliminados volúmenes antiguos de /etc/bacula/fstab correctamente");
		} catch (Exception ex) {
			logger.error("Algún error al tratar de eliminar volumenes del fstab original");
		}

		try {
			Map<String, String[]> _fstab = FileSystemManager.readTempFstab();
			
			File _tmpFstab = new File(WBSAirbackConfiguration.getFileTmpFstab());
			if (_tmpFstab.exists())
				_tmpFstab.delete();
			logger.info(WBSAirbackConfiguration.getFileTmpFstab()+" eliminado");
			
			if (_fstab != null && _fstab.size()>0) {
				for (String key : _fstab.keySet()) {
					String[] _item = _fstab.get(key);
					Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(_item[1]);
					if (_item[2].equals("xfs") || _item[2].equals("zfs")) {
						if (VolumeManager.isLocalDeviceGroup(_lv.get("vg"))) {
							int type = FileSystemManager.FS_XFS;
							if (_item[2].equals("zfs"))
								type = FileSystemManager.FS_ZFS;
							FileSystemManager.addMountToFstab(type, _lv.get("vg"), _lv.get("lv"), _item[3]);
							logger.info("Añadimos al fstab original: "+_lv.get("vg")+"/"+_lv.get("lv"));
						}
					}
				}
			}
			logger.info("Añadidos volúmenes "+WBSAirbackConfiguration.getFileTmpFstab()+" a /etc/bacula/fstab correctamente");
		} catch (Exception ex) {
			logger.error("Algún error al tratar de agregar volumenes del fstabtmp al original");
		}
	}
}
