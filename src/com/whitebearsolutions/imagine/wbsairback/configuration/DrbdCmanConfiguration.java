package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.util.Command;

public class DrbdCmanConfiguration {
	public final static int FENCE_NONE = 0;
	public final static int FENCE_IPMI = 1;
	public final static int FENCE_DRAC = 2;
	public final static int FENCE_VMWARE = 3;
	
	public final static String tmpRdata = "/tmpRdataDrbd";
	
	private final static Logger logger = LoggerFactory.getLogger(DrbdCmanConfiguration.class);
	
	public static void cmanInit(int _fenceType, Map<String, String> _fenceAttributes) throws Exception {
		ServiceManager.fullStop(ServiceManager.CLUSTER);
        try {
        	cmanSetClusterConfiguration(_fenceType, _fenceAttributes);
        } catch (Exception ex) {
        	throw new Exception("slave: "+ex.getMessage());
        }
	}
	
	public static void cmanSetClusterConfiguration(int _fenceType, Map<String, String> _fenceAttributes) throws Exception {
		StringBuilder _sb = new StringBuilder();
		String domainId = null;
		if (_fenceType != FENCE_NONE) {
			domainId = "wbsab"+_fenceAttributes.get("virtual_address").substring(_fenceAttributes.get("virtual_address").indexOf(".", _fenceAttributes.get("virtual_address").indexOf(".")+1)+1);
			_sb.append("<?xml version=\"1.0\"?>\n");
			_sb.append("<cluster config_version=\"27\" name=\""+domainId+"\">\n");
			_sb.append("\t<cman broadcast=\"yes\" two_node=\"1\" expected_votes=\"1\" />\n");
			_sb.append("\t<fence_daemon clean_start=\"0\" post_fail_delay=\"3\" post_join_delay=\"300\"/>\n");
			_sb.append("\t<fencedevices>\n");
		}
		switch(_fenceType) {
		    default:
		    	throw new Exception("invalid fence type");
		    case FENCE_NONE: {
			    	_sb = new StringBuilder();
					_sb.append("<?xml version=\"1.0\"?>\n");
					_sb.append("<cluster alias=\"wbsairback\" config_version=\"27\" name=\"wbsairback\">\n");
					_sb.append("</cluster>\n");
					
					FileSystem.writeFile(new File(WBSAirbackConfiguration.FILE_CLUSTER), _sb.toString());
					return;
		    	}
		    case FENCE_DRAC: {
		    		if(_fenceAttributes.get("master_address") == null || !NetworkManager.isValidAddress(_fenceAttributes.get("master_address"))) {
		    			throw new Exception("incorrect master address");
		    		}
		    		if(_fenceAttributes.get("slave_address") == null || !NetworkManager.isValidAddress(_fenceAttributes.get("master_address"))) {
		    			throw new Exception("incorrect slave address");
		    		}
		    		if(_fenceAttributes.get("master_login") == null || _fenceAttributes.get("master_password") == null ||
		    				_fenceAttributes.get("master_login").isEmpty() || _fenceAttributes.get("master_password").isEmpty()) {
		    			throw new Exception("incorrect master login/password");
		    		}
		    		if(_fenceAttributes.get("slave_login") == null || _fenceAttributes.get("slave_password") == null ||
		    				_fenceAttributes.get("slave_login").isEmpty() || _fenceAttributes.get("slave_password").isEmpty()) {
		    			throw new Exception("incorrect slave login/password");
		    		}
					_sb.append("\t\t<fencedevice agent=\"fence_drac5\" cmd_prompt=\"admin1->\" secure=\"1\" action=\"off\" ipaddr=\"");
					_sb.append(_fenceAttributes.get("master_address"));
					_sb.append("\" login=\"");
					_sb.append(_fenceAttributes.get("master_login"));
					_sb.append("\" name=\"masterfence\" passwd=\"");
					_sb.append(_fenceAttributes.get("master_password"));
					_sb.append("\"/>\n");
					_sb.append("\t\t<fencedevice agent=\"fence_drac5\" cmd_prompt=\"admin1->\" secure=\"1\" action=\"off\" ipaddr=\"");
					_sb.append(_fenceAttributes.get("slave_address"));
					_sb.append("\" login=\"");
					_sb.append(_fenceAttributes.get("slave_login"));
					_sb.append("\" name=\"slavefence\" passwd=\"");
					_sb.append(_fenceAttributes.get("slave_password"));
					_sb.append("\"/>\n");
				}
				break;
			case FENCE_IPMI: {
					if(_fenceAttributes.get("master_address") == null || !NetworkManager.isValidAddress(_fenceAttributes.get("master_address"))) {
		    			throw new Exception("incorrect master address");
		    		}
		    		if(_fenceAttributes.get("slave_address") == null || !NetworkManager.isValidAddress(_fenceAttributes.get("master_address"))) {
		    			throw new Exception("incorrect slave address");
		    		}
		    		if(_fenceAttributes.get("master_login") == null || _fenceAttributes.get("master_password") == null ||
		    				_fenceAttributes.get("master_login").isEmpty() || _fenceAttributes.get("master_password").isEmpty()) {
		    			throw new Exception("incorrect master login/password");
		    		}
		    		if(_fenceAttributes.get("slave_login") == null || _fenceAttributes.get("slave_password") == null ||
		    				_fenceAttributes.get("slave_login").isEmpty() || _fenceAttributes.get("slave_password").isEmpty()) {
		    			throw new Exception("incorrect slave login/password");
		    		}
					_sb.append("\t\t<fencedevice agent=\"fence_ipmilan\" ipaddr=\"");
					_sb.append(_fenceAttributes.get("master_address"));
					_sb.append("\" login=\"");
					_sb.append(_fenceAttributes.get("master_login"));
					_sb.append("\" name=\"masterfence\" passwd=\"");
					_sb.append(_fenceAttributes.get("master_password"));
					_sb.append("\"/>\n");
					_sb.append("\t\t<fencedevice agent=\"fence_ipmilan\" ipaddr=\"");
					_sb.append(_fenceAttributes.get("slave_address"));
					_sb.append("\" login=\"");
					_sb.append(_fenceAttributes.get("slave_login"));
					_sb.append("\" name=\"slavefence\" passwd=\"");
					_sb.append(_fenceAttributes.get("slave_password"));
					_sb.append("\"/>\n");
				}
				break;
			case FENCE_VMWARE: {
					if(_fenceAttributes.get("master_address") == null || !NetworkManager.isValidAddress(_fenceAttributes.get("master_address"))) {
		    			throw new Exception("incorrect master address");
		    		}
		    		if(_fenceAttributes.get("slave_address") == null || !NetworkManager.isValidAddress(_fenceAttributes.get("master_address"))) {
		    			throw new Exception("incorrect slave address");
		    		}
		    		if(_fenceAttributes.get("master_login") == null || _fenceAttributes.get("master_password") == null ||
		    				_fenceAttributes.get("master_login").isEmpty() || _fenceAttributes.get("master_password").isEmpty()) {
		    			throw new Exception("incorrect master login/password");
		    		}
		    		if(_fenceAttributes.get("slave_login") == null || _fenceAttributes.get("slave_password") == null ||
		    				_fenceAttributes.get("slave_login").isEmpty() || _fenceAttributes.get("slave_password").isEmpty()) {
		    			throw new Exception("incorrect slave login/password");
		    		}
		    		if(_fenceAttributes.get("master_vmname") == null || _fenceAttributes.get("master_vmname") == null) {
		    			throw new Exception("incorrect master virtual machine name");
		    		}
		    		if(_fenceAttributes.get("slave_vmname") == null || _fenceAttributes.get("slave_vmname") == null) {
		    			throw new Exception("incorrect slave virtual machine name");
		    		}
					_sb.append("\t\t<fencedevice agent=\"fence_vmware\" ipaddr=\"");
					_sb.append(_fenceAttributes.get("master_address"));
					_sb.append("\" login=\"");
					_sb.append(_fenceAttributes.get("master_login"));
					_sb.append("\" name=\"masterfence\" passwd=\"");
					_sb.append(_fenceAttributes.get("master_password"));
					_sb.append("\" vmname=\"");
					_sb.append(_fenceAttributes.get("master_vmname"));
					_sb.append("\"/>\n");
					_sb.append("\t\t<fencedevice agent=\"fence_vmware\" ipaddr=\"");
					_sb.append(_fenceAttributes.get("slave_address"));
					_sb.append("\" login=\"");
					_sb.append(_fenceAttributes.get("slave_login"));
					_sb.append("\" name=\"slavefence\" passwd=\"");
					_sb.append(_fenceAttributes.get("slave_password"));
					_sb.append("\" vmname=\"");
					_sb.append(_fenceAttributes.get("slave_vmname"));
					_sb.append("\"/>\n");
				}
				break;
		}
		_sb.append("\t</fencedevices>\n");
		_sb.append("\t<clusternodes>\n");
		_sb.append("\t\t<clusternode name=\"master\" nodeid=\"1\" votes=\"1\">\n");
		_sb.append("\t\t\t<fence>\n");
		_sb.append("\t\t\t\t<method name=\"1\">\n");
		_sb.append("\t\t\t\t\t<device name=\"masterfence\"/>\n");
		_sb.append("\t\t\t\t</method>\n");
		_sb.append("\t\t\t</fence>\n");
		_sb.append("\t\t</clusternode>\n");
		_sb.append("\t\t<clusternode name=\"slave\" nodeid=\"2\" votes=\"1\">\n");
		_sb.append("\t\t\t<fence>\n");
		_sb.append("\t\t\t\t<method name=\"1\">\n");
		_sb.append("\t\t\t\t\t<device name=\"slavefence\"/>\n");
		_sb.append("\t\t\t\t</method>\n");
		_sb.append("\t\t\t</fence>\n");
		_sb.append("\t\t</clusternode>\n");
		_sb.append("\t</clusternodes>\n");
	    _sb.append("\t<rm>\n");
	    _sb.append("\t\t<failoverdomains>\n");
	    _sb.append("\t\t\t<failoverdomain name=\""+domainId+"\" nofailback=\"0\" ordered=\"0\" restricted=\"0\">\n");
	    _sb.append("\t\t\t\t<failoverdomainnode name=\"master\" priority=\"1\"/>\n");
	    _sb.append("\t\t\t\t<failoverdomainnode name=\"slave\" priority=\"1\"/>\n");
	    _sb.append("\t\t\t</failoverdomain>\n");
	    _sb.append("\t\t</failoverdomains>\n");
	    _sb.append("\t\t<resources>\n");
	    _sb.append("\t\t\t<script file=\"/etc/init.d/wbsairback-volumes-ha\" name=\"wbsairback-volumes-ha\"/>\n");
	    _sb.append("\t\t</resources>\n");
	    _sb.append("\t\t<service autostart=\"1\" domain=\""+domainId+"\" exclusive=\"0\" name=\"wbsairback-volumes-ha\" recovery=\"relocate\">\n");
	    _sb.append("\t\t\t<script ref=\"wbsairback-volumes-ha\"/>\n");
	    _sb.append("\t\t</service>\n");
	    _sb.append("\t</rm>\n");
	    _sb.append("</cluster>\n");
	    
	    FileSystem.writeFile(new File(WBSAirbackConfiguration.FILE_CLUSTER), _sb.toString());
	    DrbdCmanConfiguration.killCmanAssociatedProcesses();
	}
	
	public static void restartCmanServices() {
		killCmanAssociatedProcesses();
	    try {
	    	ServiceManager.start(ServiceManager.CLUSTER);
	    } catch (Exception ex) {}
	}
	
	public static void killCmanAssociatedProcesses() {
	    try {
	    	ServiceManager.fullStop(ServiceManager.CLUSTER);
	    } catch (Exception ex) {}
		try {
			Command.systemCommand("killall -9 corosync");
		} catch (Exception ex){}
		try {
			Command.systemCommand("killall -9 fenced");
		} catch (Exception ex){}
		try {
			Command.systemCommand("killall -9 fence_tool");
		} catch (Exception ex){}
	}
	
	public static void drbdConfigureAll(String node, String myAddress, String pairAddress) throws Exception {
		// DRBD: cargamos el modulo
    	drbdLoadModules();
    	// DRBD: Creamos metadatos y generamos la configuración
    	drbdInitMetaData();
    	drbdWriteDrbdConf(myAddress, pairAddress, node);
    	// DRBD: ejecutamos secuencia para inicializar el servicio drbd
        drbdInitAfterMetadata(node);
	}

	public static String getDRBDSynchronization() {
		File _f = new File("/proc/drbd");
		if(!_f.exists()) {
			return new String();
		}
		
		try {
			BufferedReader _reader = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
	        String _line;
			while((_line = _reader.readLine()) != null) {
				if(_line.contains("ds:UpToDate/UpToDate")) {
					return new String();
				}
				if(_line.contains(" sync'ed:")) {
					_line = _line.substring(_line.indexOf(" sync'ed:") + 10);
					return _line.substring(0, _line.indexOf("%") + 1);
				}
			}
		} catch(IOException _ex) {
			return new String();
		}
		return new String();
	}
	
	public static void drbdWriteDrbdConf(String myAddress, String pairAddress, String node) throws Exception {
		StringBuilder _sb = new StringBuilder();
        _sb.append("global {\n");
        _sb.append(" usage-count no;\n");
        _sb.append("}\n\n");
        _sb.append("resource rdata {\n");
        _sb.append("  protocol C;\n\n");
        _sb.append("  handlers {\n");
        _sb.append("    pri-on-incon-degr \"echo 'DRBD: primary requested but inconsistent!' | wall; /etc/init.d/heartbeat stop; reboot -f\";\n");
        _sb.append("    pri-lost-after-sb \"echo 'DRBD: primary requested but lost!' | wall; /etc/init.d/heartbeat stop; reboot -f\";\n");
        _sb.append("  }\n\n");
        _sb.append("  startup {\n");
        _sb.append("    degr-wfc-timeout 10;\n");
        _sb.append("    wfc-timeout 10;\n");
        _sb.append("  }\n\n");
        _sb.append("  disk {\n");
        _sb.append("    on-io-error   detach;\n");
        _sb.append("  }\n\n");
        _sb.append("  net {\n");
        _sb.append("    timeout 120;\n");
        _sb.append("    connect-int 20;\n");
        _sb.append("    ping-int 20;\n");
        _sb.append("    max-buffers     2048;\n");
        _sb.append("    max-epoch-size  2048;\n");
        _sb.append("    ko-count 30;\n");
        _sb.append("    cram-hmac-alg sha1;\n");
        _sb.append("    shared-secret \"4ir84ck\";\n");
        _sb.append("    after-sb-0pri discard-zero-changes; # If both nodes are secondary, just make one of them primary\n");
        _sb.append("    after-sb-1pri discard-secondary; # If one is primary, one is not, trust the primary node\n");
        _sb.append("    after-sb-2pri call-pri-lost-after-sb; # If there are two primaries, make the unchanged one secondary\n");
        _sb.append("  }\n\n");
        _sb.append("  syncer {\n");
        _sb.append("    rate 330M;\n");
        _sb.append("    al-extents 257;\n");
        _sb.append("  }\n\n");
        _sb.append("  on master {\n");
        _sb.append("    device    /dev/drbd0;\n");
        _sb.append("    disk      " + HAConfiguration.systemDevice() + "3;\n");
        if (node.equals("master")) {
        	 _sb.append("    address   " + myAddress + ":7788;\n");
        } else {
        	_sb.append("    address   " + pairAddress + ":7788;\n");
        }
        _sb.append("    meta-disk internal;\n");
        _sb.append("  }\n\n");
        _sb.append("  on slave {\n");
        _sb.append("    device    /dev/drbd0;\n");
        _sb.append("    disk      " + HAConfiguration.systemDevice() + "3;\n");
        if (node.equals("master")) {
        	_sb.append("    address   " + pairAddress + ":7788;\n");
        } else {
        	_sb.append("    address   " + myAddress + ":7788;\n");
        }
        _sb.append("    meta-disk internal;\n");
        _sb.append("  }\n\n");
        
        FileOutputStream _fos = new FileOutputStream("/etc/drbd.conf");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
	}
	
	
	public static void drbdInitAfterMetadata(String node) throws Exception {
		Command.systemCommand("/sbin/depmod -a");
        Command.systemCommand("/sbin/modprobe drbd");
        try {
        	Command.systemCommand("/etc/init.d/drbd restart");
        } catch (Exception ex) {
        	throw new Exception ("Error restarting drbd on"+node);
        }
        try {
        	Command.systemCommand("/sbin/drbdadm detach rdata");
        } catch (Exception ex) {}
        try {	
        	Command.systemCommand("/sbin/drbdadm disconnect rdata");
		} catch (Exception ex) {}
        try {
        	Command.systemCommand("/sbin/drbdadm down rdata");
        } catch (Exception ex) {}
        Command.systemCommand("/bin/echo -e \"yes\" | /sbin/drbdadm -- --force create-md rdata");
	}
	
	public static void buildTmpRdata() throws Exception {
		Command.systemCommand("mkdir -p "+tmpRdata);
		logger.info("Ejecutado: mkdir -p "+tmpRdata);
		
		try {
			Command.systemCommand("rm -r "+tmpRdata+"/*");
			logger.info("Ejecutado: rm -r "+tmpRdata+"/*");
		} catch (Exception ex){}
		
		try {
			logger.info("Copiando datos ...");
			Command.systemCommand("cp -p -r /rdata/* "+tmpRdata+"/");
			logger.info("Ejecutado: cp -p -r /rdata/* "+tmpRdata+"/");
		} catch (Exception ex) {
			logger.error("Error al ejecutar: cp -p -r /rdata/* "+tmpRdata+"/ ex:"+ex.getMessage());
			throw new Exception("Error copying data to tmpRdata lun: "+tmpRdata+"/ -- ex:"+ex.getMessage());
		}
	}
	
	public static void setDrbdDiskAsRdata() throws Exception {
		logger.info("Inicio de creacion de nuevo /rdata en el disco drbd compartido");
		try {
			logger.info("Creamos el filesystem ...");
			Command.systemCommand("mkfs.ext3 -F /dev/drbd0 >> /dev/null");
			logger.info("Ejecutado: mkfs.ext3 -F /dev/drbd0");
		} catch (Exception ex) {
			logger.info("Error al ejecutar: mkfs.ext3 -F /dev/drbd0 ex:"+ex.getMessage());
			throw new Exception("Error trying to generate new ext3 filesystem on: /dev/drbd0. Ex:"+ex.getMessage());
		}
		
		try {
			Command.systemCommand("mount /dev/drbd0 /rdata/");
			logger.info("Ejecutado: mount /dev/drbd0 /rdata/");
		} catch (Exception ex) {
			logger.error("Error al ejecutar : mount /dev/drbd0 /rdata/ ex:"+ex.getMessage());
			throw new Exception("Error mounting drbddisk -- ex:"+ex.getMessage());
		}
		
		try {
			logger.info("Copiando datos de directorio temporal...");
			Command.systemCommand("cp -p -r "+tmpRdata+"/* /rdata/");
			logger.info("cp -p -r "+tmpRdata+"/* /rdata/");
		} catch (Exception ex) {
			logger.error("Error al ejecutar: cp -p -r "+tmpRdata+"/* /rdata/ ex:"+ex.getMessage());
			throw new Exception("Error copying tmpRdata to new rdata on drbd disk -- ex:"+ex.getMessage());
		}
		
		try {
			logger.info("Desmontamos rdata...");
			HAConfiguration.rdataForceUmount();
			logger.info("Rdata desmontado.");
		} catch (Exception ex) {
			logger.error("Error desmontando rdata. Ex: {}", ex.getMessage());
			throw new Exception ("Error umounting rdata. Ex: "+ ex.getMessage());
		}
	}
	
	/*public static void rdataRecoverFromDrbdDisk() throws Exception {
		logger.info("Inicio de recuperacion de /rdata en formato drbd a disco original");
		try {
			String originalDisk = HAConfiguration.systemDevice() + "3";

			File f = new File("/rdata");
			if (!f.exists() || f.listFiles() == null || f.listFiles().length <=0) {
				logger.error("Atencion, la particion rdata en /dev/drbd0 que deberia contener los datos de /rdata para traspasarlos al disco original, parece estar vacia!!");
				throw new Exception("Error, partition /rdata with system data is empty!!");
			}
				
			Command.systemCommand("mkdir -p "+tmpRdata);
			logger.info("Ejecutado: mkdir -p "+tmpRdata);
			
			try {
				Command.systemCommand("rm -r "+tmpRdata+"/*");
				logger.info("Ejecutado: rm -r "+tmpRdata+"/*");
			} catch (Exception ex){}
			
			try {
				logger.info("Copiando datos a temporal ...");
				Command.systemCommand("cp -p -r /rdata/* "+tmpRdata);
				logger.info("Ejecutado: cp -p -r /rdata/* "+tmpRdata);
			} catch (Exception ex) {
				logger.error("Error al ejecutar: cp -p -r /rdata/* "+tmpRdata+" ex:"+ex.getMessage());
				throw new Exception("Error copying data to tmp directory: "+tmpRdata+"-- ex:"+ex.getMessage());
			}
			
			drbdUnconfigure();
			
			try {
				logger.info("Creado filesystem ");
				Command.systemCommand("mkfs.ext3 -F "+originalDisk+" >> /dev/null");
				logger.info("Ejecutado: mkfs.ext3 -F "+originalDisk);
			} catch (Exception ex) {
				logger.info("Error al ejecutar: mkfs.ext3 -F "+originalDisk+" ex:"+ex.getMessage());
				throw new Exception("Error trying to generate new ext3 filesystem on: /dev/sda3 -- ex:"+ex.getMessage());
			}
			
			try {
				logger.info("Montando rdata.. ");
				Command.systemCommand("mount "+originalDisk+" /rdata");
				logger.info("Ejecutado: mount "+originalDisk+" /rdata");
			} catch (Exception ex) {
				logger.info("Error al ejecutar: mount "+originalDisk+" /rdata ex:"+ex.getMessage());
				throw new Exception("Error mounting disk /dev/sda3 on /rdata-- ex:"+ex.getMessage());
			}
			
			try {
				logger.info("Copiando datos de temporal a /rdata...");
				Command.systemCommand("cp -p -r "+tmpRdata+"/* /rdata/");
				logger.info("Ejecutado: cp -p -r "+tmpRdata+"/* /rdata");
			} catch (Exception ex) {
				logger.error("Error al ejecutar: cp -p -r "+tmpRdata+"/* /rdata/ ex:"+ex.getMessage());
				throw new Exception("Error copying data from tmp directory: "+tmpRdata+"-- ex:"+ex.getMessage());
			}
			
		} catch (Exception ex) {
			logger.error("No se pudo recuperar /rdata a la particion original "+ex.getMessage());
		}
	}*/
	
	public static void drbdInitMetaData() throws Exception {
		File _f = new File("/var/lib/drbd/meta-disk.raw");
        if(_f.exists()) {
        	_f.delete();
        }
        Command.systemCommand("/bin/dd if=/dev/zero of=/dev/sda3 bs=1k count=307200");
	}
	
	
	public static void drbdLoadModules() throws Exception {
		BufferedReader _reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/etc/modules"))));
		StringBuilder _sb = new StringBuilder();
		String line;
		while((line = _reader.readLine()) != null) {
			if(!line.contains("drbd")) {	
				_sb.append(line + "\n");
			}
		}
        _sb.append("drbd\n");
    
        FileOutputStream _fos = new FileOutputStream("/etc/modules");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
	}
	
	public static void drbdUnLoadModules() throws Exception {
		BufferedReader _reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/etc/modules"))));
		StringBuilder _sb = new StringBuilder();
		String line;
		while((line = _reader.readLine()) != null) {
			if(!line.contains("drbd")) {	
				_sb.append(line + "\n");
			}
		}
    
        FileOutputStream _fos = new FileOutputStream("/etc/modules");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
        
        try {
        	Command.systemCommand("modprobe -r drbd");
		} catch (Exception ex){}
	}
	
	
	
	public static String drbdGetSynchronizationStatus() throws Exception {
		return Command.systemCommand("/bin/echo \"\n\" | /sbin/drbdadm dstate rdata");
	}
	
	
	public static void drbdUnconfigure() throws Exception {
        try {
        	Command.systemCommand("/sbin/drbdadm detach rdata");
        } catch (Exception ex){}
        try {
        	Command.systemCommand("/sbin/drbdadm disconnect rdata");
		} catch (Exception ex){}
        try {
        	Command.systemCommand("/sbin/drbdadm down rdata");
		} catch (Exception ex){}
        
        Command.systemCommand("/etc/init.d/drbd stop");
        ServiceManager.remove(ServiceManager.DRBD);
		ServiceManager.remove(ServiceManager.WBSAIRBACK_DRBD);
        drbdUnLoadModules();
	}
}
