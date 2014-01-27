package com.whitebearsolutions.imagine.wbsairback.disk;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.io.FileUtils;
import com.whitebearsolutions.util.Command;

public class CloudManager {
    private static File _cloud_directory;
    private static final String fileSystemName = "system";
    private static final String fileCredentialsS3 = ".s3backer_passwd";
    private static final String fileCredentialsAtmos = ".s3backer_atmos_passwd";
    private static final String fileServerAtmos = ".s3backer_server";
    private static final String blockSize="128k";
    
    public static final Integer TYPE_S3 = 0;
    public static final Integer TYPE_ATMOS = 1;
    
    private final static Logger logger = LoggerFactory.getLogger(CloudManager.class);
    
    static {
    	_cloud_directory = new File(WBSAirbackConfiguration.getDirectoryCloud());
        if(!_cloud_directory.exists()) {
        	_cloud_directory.mkdirs();
        }
    }
    
    
    /**
     * Crea la configuración necesaria para una cuenta ==> /etc/cloud/nombreCuenta/
     * 																				.sbacker_passwd
     * 																				/system				==> Directorio donde se crea el sistema de ficheros		
     * @throws Exception
     */
    public static void createAccount(String aliasAccount, String userUid, String userKey, Integer typeAccount, String atmosserver) throws Exception{
 
    	StringBuilder _sb = new StringBuilder();
    	_sb.append(userUid);
    	_sb.append(":");
    	_sb.append(userKey);

    	File _accountDir = new File(getAccountPath(aliasAccount));
    	if (!_accountDir.exists())
    		_accountDir.mkdirs();
    	
    	try {
    		Command.systemCommand("rm "+getAccountPath(aliasAccount)+"/.s3*");
    	} catch (Exception ex){}
    	
    	
    	FileOutputStream _fos = new FileOutputStream(new File(getPassFilePath(aliasAccount, typeAccount)));
		_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
		_fos.close();
		
    	if (typeAccount.equals(TYPE_ATMOS) && atmosserver != null) {
    		 _sb = new StringBuilder();
    		_sb.append(atmosserver);
    		_fos = new FileOutputStream(new File(getAtmosServerFilePath(aliasAccount, typeAccount)));
    		_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
    		_fos.close();	
    	}
    	
    	File _accountSystemDir = new File(getFileSystemAccountPath(aliasAccount));
    	if (!_accountSystemDir.exists())
    		_accountSystemDir.mkdirs();
    }
    
    
    /**
     * Devuelve el path para el sistema de una cuenta cloud: /etc/cloud/cuenta/system
     * @param account
     * @return
     * @throws Exception
     */
    public static String getFileSystemAccountPath(String account) throws Exception {
    	return getAccountPath(account) + "/" + fileSystemName;
    }
    
    
    /**
     * Devuelve el path general de una cuenta cloud: /etc/cloud/cuenta
     * @param account
     * @return
     * @throws Exception
     */
    public static String getAccountPath(String account) throws Exception {
    	return WBSAirbackConfiguration.getDirectoryCloud() + "/" + account;
    }
    
    
    /**
     * Devuelve el path del archivo de credenciales de una cuenta cloud: /etc/cloud/cuenta/.s3pass ...
     * @param account
     * @param type
     * @return
     * @throws Exception
     */
    public static String getPassFilePath(String account, Integer type) throws Exception {
    	String pathFile = fileCredentialsS3;
    	if (type.equals(TYPE_ATMOS))
    		pathFile = fileCredentialsAtmos;
    	return WBSAirbackConfiguration.getDirectoryCloud() + "/" + account + "/" + pathFile;
    }
    
    
    /**
     * Devuelve el path del archivo de servidor atmos de una cuenta cloud: /etc/cloud/cuenta/.s3server ...
     * @param account
     * @param type
     * @return
     * @throws Exception
     */
    public static String getAtmosServerFilePath(String account, Integer type) throws Exception {
    	String pathFile = fileServerAtmos;
    	return WBSAirbackConfiguration.getDirectoryCloud() + "/" + account + "/" + pathFile;
    }
    
    /**
     * Devuelve el path del archivo de credenciales de una cuenta cloud: /etc/cloud/cuenta/.s3pass ...
     * @param account
     * @param type
     * @return
     * @throws Exception
     */
    public static String getConfigFilePath(String account, String cloudSystem) throws Exception {
    	return getFileSystemAccountPath(account) + "/" + cloudSystem +".config";
    }
    
    
    /**
     * Devuelve el path de un sistema cloud concreto
     * @param account
     * @param cloudSystem
     * @return
     * @throws Exception
     */
    public static String getCloudSystemPath(String account, String cloudSystem) throws Exception {
    	return getFileSystemAccountPath(account) + "/" + cloudSystem;
    }
    
    
    /**
     * Devuelve el path de las estadisticas de un sistema cloud
     * @param account
     * @param cloudSystem
     * @return
     * @throws Exception
     */
    public static String getCloudSystemStatsPath(String account, String cloudSystem) throws Exception {
    	return getCloudSystemPath(account, cloudSystem) + "/stats";
    }
    
    
    /**
     * Crea un sistema cloud, tal y como si se tratara de una nueva partición
     * @throws Exception
     */
    public static void createCloudSystem(String aliasAccount, String size, String bucket, Integer type) throws Exception {
    	String pathNewSystem = createDirCloudSystem(aliasAccount);
    	String cld = pathNewSystem.substring(pathNewSystem.indexOf("system/")+"system/".length());
    	String newDevice = findFreeLoopDevice();
    	try {
    		
    		launchS3Device(aliasAccount, size, bucket, type, pathNewSystem);
    		
    		// Asociamos el sistema de ficheros 'virtual' gestionado por s3backer con un sistema 'real' de loop
    		Command.systemCommand("sudo losetup "+newDevice+" "+pathNewSystem+"/file");
    		
    		// Si todo ha ido bien, almacenamos la configuracion: archivo /dev/loop y 
    		String config = newDevice;
    		config+=":"+size;
    		if (bucket != null && bucket.length()>0)
    			config+=":::"+bucket;
    		
    		FileOutputStream _fos = new FileOutputStream(getConfigFilePath(aliasAccount, cld));
    		_fos.write(config.getBytes(Charset.forName("UTF-8")));
    		_fos.close();
    		
    	} catch (Exception ex) {
    		forceDeleteSystem(pathNewSystem, newDevice, aliasAccount, cld, size, bucket, type, true);
    		throw new Exception("Error creating cloud system [alias:"+aliasAccount+" size:"+size+"]: "+ex.getMessage());
    	}
    }
    
    /**
     * 
     * @param aliasAccount
     * @param size
     * @param bucket
     * @param type
     * @throws Exception
     */
    public static void eraseCloudBlocksSystem(String aliasAccount, String size, String bucket, Integer type, String cld) throws Exception {
    	String prefix = aliasAccount+cld;
    	StringBuilder _sb = new StringBuilder();
    	_sb.append(WBSAirbackConfiguration.getBinS3backer());
    	_sb.append(" ");
    	_sb.append("--erase --force");
    	_sb.append(" ");
    	_sb.append("--blockSize=");
    	_sb.append(blockSize);
    	_sb.append(" --size=");
    	_sb.append(size);
    	_sb.append(" --prefix=");
    	_sb.append(prefix);
    	if (type.equals(TYPE_S3)) {
        	_sb.append(" --prefix=");
        	_sb.append(prefix);
	    	_sb.append(" --listBlocks ");
	    	if (bucket != null)
	    		_sb.append(bucket);
	    	else
	    		throw new Exception("Error deleting cloud system: missing bucket name");
    	} else {
    		_sb.append(" --atmos ");
    		Map<String, String> account = getAccount(aliasAccount);
    		_sb.append(" --baseURL=");
    		String url = account.get("atmosserver");
    		if (url.charAt(url.length()-1) == '/') {
    			url = url.substring(0, url.length()-2);
    		}
    		if (!account.get("atmosserver").contains("rest"))
    			_sb.append(account.get("atmosserver")+"/rest/namespace/");
    		_sb.append(" --prefixAtmos=");
        	_sb.append(prefix);
    	}
    	_sb.append(" ");
    	_sb.append("--accessFile=");
    	_sb.append(getPassFilePath(aliasAccount, type));
    	_sb.append(" ");
    	
    	// Ejecutamos el demonio de s3backer
		Command.systemCommand(_sb.toString());
    }
    
    
    /**
     * Elimina un disco cloud
     * @param account
     * @param device
     * @param cld
     * @throws Exception
     */
    public static void removeCloudDisk(String account, String device, String cld)  throws Exception {
    	Map<String, String> disk = getCloudDiskByDevice(device);
    	Integer type = TYPE_S3;
    	if (disk.get("type") != null && !disk.get("type").equals("S3"))
    		type = TYPE_ATMOS;	
    	forceDeleteSystem(getCloudSystemPath(account, cld), device, account, cld, disk.get("size-backer"), disk.get("bucket"), type, false);
    }
    
    /**
     * Fuerza el borrado de un sistema cloud
     * @param path
     * @param device
     * @throws Exception
     */
    public static void forceDeleteSystem(String path, String device, String account, String cld, String size, String bucket, Integer type, Boolean force) throws Exception {
		logger.info("Eliminando sistema cloud [path:{}, device:{}, account:{}, cld:{}, size:{}, bucket:{}, type:{}]", new Object[]{path, device, account, cld, size, bucket, type});
		try {
			Command.systemCommand("umount -l "+path);
		} catch (Exception ex) {
			logger.error("Error desmontando cloud: {}. Ex:{}", path, ex.getMessage());
			if (!force)
				throw new Exception("Error umounting cloud: "+path+". Ex: " + ex.getMessage());
		}
		
		boolean success = false;
		int i=0;
		while (i<5 && !success) {
	    	try {
	    		Thread.sleep(3000); //Esperamos a q se desmonte correctamente
	    		Command.systemCommand("losetup -d "+device);
	    		success=true;
			} catch (Exception ex){
				i++;
				if (i>=5) {
					logger.error("Error al desasignar el dispositivo cloud: {}. Ex:{}", device, ex.getMessage());
					if (!force)
						throw new Exception("Error unassigning cloud device: "+device+". Ex: " + ex.getMessage());
				}
			}
		}
		
		try {
			killS3ofPath(path);
		} catch (Exception ex) {
			logger.error("Error al matar s3backer de: {}. Ex:{}", path, ex.getMessage());
		}
		try {
    		eraseCloudBlocksSystem(account, size, bucket, type, cld);
    	} catch (Exception ex) {
    		logger.error("Error borrando datos cloud de cuenta:{} bucket:{} cld:{}. Ex:{}", new Object[]{account, bucket, cld, ex});
    	}
		try {
			Command.systemCommand("rm -r "+path);
		} catch (Exception ex) {
			logger.error("Error eliminando archivos cloud: {}. Ex:{}", path, ex.getMessage());
		}
		try {
			Command.systemCommand("rm "+getConfigFilePath(account, cld));
		} catch (Exception ex) {
			logger.error("Error eliminando archivo de configuracion cloud de cuenta: {} cld:{}. Ex:{}", new Object[]{account, cld, ex});
		}
    }
    
    
    public static void eraseAllCloudData() throws Exception {
    	List<String> accounts = listAccountAliases();
    	for (String ac : accounts) {
    		try {
    			removeAccount(ac, true);
    		} catch (Exception ex) {
    			logger.error("No se pudo borrar la cuenta {}. ex: {}", ac, ex.getMessage());
    		}
    	}
    	
		File _f = new File("/etc/cloud/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath() + "/*");
		}
    }
    
    /**
     * Elimina el proces s3backer de cierto path cloud asociado
     * @param path
     * @throws Exception
     */
    public static void killS3ofPath(String path) throws Exception {
    	String output = Command.systemCommand("ps axww | grep "+path+" | awk '{print $1}'");
    	if (output != null && output.length()>0) {
    		StringTokenizer st = new StringTokenizer(output, "\n");
    		while (st.hasMoreTokens()) {
    			String line = st.nextToken();
    			String pid = line.trim();
    			logger.debug("Matando s3backer con pid: {}", pid);
    			Command.systemCommand("kill -9 "+pid.trim());
    			return;
    		}
    	}
    }
    
    
    /**
     * Indica si s3backer esta ejecutandose contra cierto path
     * @param path
     * @return
     * @throws Exception
     */
    public static boolean isS3RuningOfPath(String path) throws Exception {
    	try {
	    	String output = Command.systemCommand("ps axww | grep s3backer");
	    	if (output != null && output.length()>0) {
	    		StringTokenizer st = new StringTokenizer(output, "\n");
	    		while (st.hasMoreTokens()) {
	    			String line = st.nextToken();
	    			if (line.contains(path)) {
	    				return true;
	    			}
	    		}
	    	}
	    	return false;
    	} catch (Exception ex) {
    		return false;
    	}
    }
    
    
    /**
     * Indica si un dispositivo loop esta configurado 
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean isLoopConfigured(String device) throws Exception {
    	try {
    		String output = Command.systemCommand("losetup -a");
    		if (output != null && output.length()>0 && output.contains(device))
    			return true;
    		else
    			return false;
    	} catch (Exception ex) {
    		return false;
    	}
    }
    
    
    /**
     * Indica si un dispositivo loop esta configurado 
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean isSystemConfiguredonLoop(String system) throws Exception {
    	try {
    		String output = Command.systemCommand("losetup -a");
    		StringTokenizer _st = new StringTokenizer(output,"\n");
    		while (_st.hasMoreTokens()) {
    			String line = _st.nextToken();
    			if (line != null && line.length()>0 && line.contains(system))
    				return true;
    		}
    		return false;
    	} catch (Exception ex) {
    		return false;
    	}
    }
    
    
    /**
     * Re-abre un sistema cloud que ya estuviese creado
     * @param aliasAccount
     * @param size
     * @param bucket
     * @param type
     * @param pathCloud
     * @throws Exception
     */
    public static void relaunchCloudSystem(String aliasAccount, String cldDir) throws Exception {
    	
    	try {
			String config = FileUtils.fileToString(getConfigFilePath(aliasAccount, cldDir));
			Integer type = TYPE_S3;
			String device = config.substring(0, config.indexOf(":"));
			String bucket = null;
			String size = null;
			if (config.indexOf(":::") > 0) {
				size = config.substring(config.indexOf(":")+1, config.indexOf(":::"));
				bucket = config.substring(config.indexOf(":::")+3);
			} else {
				size = config.substring(config.indexOf(":")+1);
				type = TYPE_ATMOS;
			}
			
			if (!isS3RuningOfPath(getCloudSystemPath(aliasAccount, cldDir))) {
				launchS3Device(aliasAccount, size, bucket, type, getCloudSystemPath(aliasAccount, cldDir));
				if (!isSystemConfiguredonLoop(cldDir)) {
					String newDevice = findFreeLoopDevice();
					Command.systemCommand("losetup "+newDevice+" "+getCloudSystemPath(aliasAccount, cldDir)+"/file");
					
					config.replace(device, newDevice);
					FileOutputStream _fos = new FileOutputStream(getConfigFilePath(aliasAccount, cldDir));
		    		_fos.write(config.getBytes(Charset.forName("UTF-8")));
		    		_fos.close();
				} else {
					String newDevice = findDeviceOfCloudPath(getCloudSystemPath(aliasAccount, cldDir));
					
					config.replace(device, newDevice);
					FileOutputStream _fos = new FileOutputStream(getConfigFilePath(aliasAccount, cldDir));
		    		_fos.write(config.getBytes(Charset.forName("UTF-8")));
		    		_fos.close();
				}
			}
			
			logger.info("Sistema cloud relanzado [cuenta:{}, cldDir:{}]", aliasAccount, cldDir);
    	} catch (Exception ex) {
    		logger.error("No se pudo relanzar el sistema cloud [cuenta:{}, cldDir:{}]", aliasAccount, cldDir);
    		throw new Exception("Error relaunching cloud system [alias:"+aliasAccount+" cldDir:"+cldDir+"]: "+ex.getMessage());
    	}
    }
    
    
    /**
     * Ejecuta le comando S3backer para iniciar el un sistema cloud
     * @param aliasAccount
     * @param size
     * @param bucket
     * @param type
     * @param pathSystem
     * @throws Exception
     */
    public static void launchS3Device(String aliasAccount, String size, String bucket, Integer type, String pathSystem) throws Exception {
    	String prefix = aliasAccount+pathSystem.substring(pathSystem.indexOf("cld"));
    	StringBuilder _sb = new StringBuilder();
    	_sb.append(WBSAirbackConfiguration.getBinS3backer());
    	_sb.append(" ");
    	_sb.append("--blockSize=");
    	_sb.append(blockSize);
    	_sb.append(" --size=");
    	_sb.append(size);
    	if (type.equals(TYPE_S3)) {
        	_sb.append(" --prefix=");
        	_sb.append(prefix);
	    	_sb.append(" --listBlocks ");
	    	if (bucket != null)
	    		_sb.append(bucket);
	    	else
	    		throw new Exception("Error creating cloud system: missing bucket name");
    	} else {
    		_sb.append(" --prefixAtmos=");
        	_sb.append(prefix);
    		Map<String, String> account = getAccount(aliasAccount);
    		_sb.append(" --baseURL=");
    		String url = account.get("atmosserver");
    		if (url.charAt(url.length()-1) == '/') {
    			url = url.substring(0, url.length()-2);
    		}
    		if (!account.get("atmosserver").contains("rest"))
    			_sb.append(account.get("atmosserver")+"/rest/namespace/");
    		_sb.append(" --atmos");
    	}
    	_sb.append(" ");
    	_sb.append("--accessFile=");
    	_sb.append(getPassFilePath(aliasAccount, type));
    	_sb.append(" ");
    	_sb.append(pathSystem);
    	
    	logger.debug("Lanzamos s3backer: {}", _sb.toString());
    	// Ejecutamos el demonio de s3backer
		Command.systemCommand(_sb.toString());
    }
    
    /**
     * Busca un dispositivo /dev/loop libre para montar un disco cloud
     * @return
     * @throws Exception
     */
    public static String findFreeLoopDevice() throws Exception {
    	try {
    		String device = Command.systemCommand("losetup -f");
    		if (device == null || device.equals(""))
    			throw new Exception("No free loop device");
    		return device.replaceAll("\n", "");
    	} catch (Exception ex) {
    		throw new Exception("Unable to find free local loop device");
    	}
    }
    
    
    /**
     * Busca el dispositivo /dev/loop asociado a un path de cloud
     * @param path
     * @return
     * @throws Exception
     */
    public static String findDeviceOfCloudPath(String path) throws Exception {
    	try {
    		String device = null;
    		String output = Command.systemCommand("losetup -a");
    		if (output != null && output.length()>0) {
        		StringTokenizer _st = new StringTokenizer(output, "\n");
        		while (_st.hasMoreTokens()) {
        			String line = _st.nextToken();
        			if (line.contains(path)){
        				device = line.substring(0, line.indexOf(":"));
        				return device;
        			}
        		}
    		}
    		return device;
    	} catch (Exception ex) {
    		return null;
    	}
    }
    
    
    /**
     * Crea un directorio de sistema donde se almacenara la información de la partición cloud 
     * @param aliasAccount
     * @return
     * @throws Exception
     */
    public static String createDirCloudSystem(String aliasAccount) throws Exception {
    	String pathSystem=getFileSystemAccountPath(aliasAccount)+"/cld";
    	File systemDir = new File(getFileSystemAccountPath(aliasAccount));
    	if (!systemDir.exists())
    		systemDir.mkdirs();
    	int i=1;
    	while (new File(pathSystem+i).exists())
    		i++;
    
    	File newDir = new File(pathSystem+i);
    	newDir.mkdirs();
    	return pathSystem+i;
    }
    
    
    /**
     * Elimina los archivos de credenciales de una cuenta
     * @param aliasAccount
     * @throws Exception
     */
    public static void removeAccount(String aliasAccount, boolean force) throws Exception {
    	try {
    		logger.info("Eliminando cuenta:{}, forzar borrado de discos:{} ...", aliasAccount, force);
    		List<Map<String, String>> disks = getAccountDisks(aliasAccount);
    		if (disks != null && disks.size()>0) {
    			if (!force)
    				throw new Exception("Existen discos asociados a esta cuenta. Elimínelos primero");
    			else {
    				logger.debug("La cuenta tiene discos y vamos a borrarlos ...");
    				for (Map<String, String> disk : disks) {
    					int type = TYPE_ATMOS;
    					if (disk.get("type").equals("S3"))
    						type = TYPE_S3;
    					forceDeleteSystem(getCloudSystemPath(aliasAccount, disk.get("cld")), disk.get("device"), aliasAccount, disk.get("cld"), disk.get("size-backer"), disk.get("bucket"), type, force);
    				}
    			}
    		}
    		
    		Command.systemCommand("rm -r "+getAccountPath(aliasAccount));
    	} catch (Exception ex) {
    		throw new Exception("No se pudo borrar la cuenta. ex:"+ ex.getMessage());
    	}
    }
    
    
    public static List<Map<String, String>> getAccountDisks(String account) throws Exception {
    	List<Map<String, String>> disks = getCloudDisks();
    	List<Map<String, String>> accountDisks = new ArrayList<Map<String, String>>();
    	for (Map<String, String> disk: disks) {
    		logger.debug("Disco cloud: cuenta:{}", disk.get("account"));
    		if (disk.get("account").equals(account)) {
    			accountDisks.add(disk);
    		}
    	}
    	return accountDisks;
    }
    
    
    /**
     * Lista las cuentas cloud registradas
     * @throws Exception
     */
    public static List<String> listAccountAliases() throws Exception {
    	List<String> accounts = new ArrayList<String>();
    	File cloudDir = new File(WBSAirbackConfiguration.getDirectoryCloud());
    	String[] listDir = cloudDir.list();
    	if (listDir != null) {
    		for (String el : listDir) {
    			File dirAccount = new File(getAccountPath(el));
    			if (dirAccount.isDirectory()) {
    				String[] listAccount = dirAccount.list();
    				if (listAccount != null) {
    					accounts.add(el);
    				}
    			}
    		}
    	}
    	return accounts;
    }
    
    
    /**
     * Lee el archivo de configuracion de una cuenta
     * @param alias
     * @return
     * @throws Exception
     */
    public static Map<String, String> getAccount(String alias) throws Exception {
    	Map<String, String> account = new HashMap<String, String>();
    	File cloudDir = new File(WBSAirbackConfiguration.getDirectoryCloud());
    	String[] listDir = cloudDir.list();
    	if (listDir != null) {
    		for (String el : listDir) {
    			if (el.equals(alias)) {
	    			File dirAccount = new File(getAccountPath(el));
	    			if (dirAccount.isDirectory()) {
	    				String[] listAccount = dirAccount.list();
	    				if (listAccount != null) {
	    					for (int i=0;i<listAccount.length;i++) {
	    						String file = listAccount[i];
	    						if (new File(WBSAirbackConfiguration.getDirectoryCloud()+"/"+el+"/"+file).isFile()) {
		    						if (file.equals(fileCredentialsS3)) {
		    							String cadAuth = FileUtils.fileToString(WBSAirbackConfiguration.getDirectoryCloud()+"/"+el+"/"+file);
		    							account.put("type", "S3");
		    							account.put("userkey", cadAuth.substring(cadAuth.indexOf(":")+1));
			    						account.put("userid", cadAuth.substring(0, cadAuth.indexOf(":")));
		    						} else if (file.equals(fileCredentialsAtmos)){
		    							String cadAuth = FileUtils.fileToString(WBSAirbackConfiguration.getDirectoryCloud()+"/"+el+"/"+file);
		    							account.put("type", "Atmos");
		    							account.put("userkey", cadAuth.substring(cadAuth.indexOf(":")+1));
			    						account.put("userid", cadAuth.substring(0, cadAuth.indexOf(":")));
		    						} else if (file.equals(fileServerAtmos)) {
		    							String cad = FileUtils.fileToString(WBSAirbackConfiguration.getDirectoryCloud()+"/"+el+"/"+file);
		    							account.put("atmosserver", cad.trim());
		    							account.put("type", "Atmos");
		    						}
	    						}
	    					}
	    					account.put("name", el);
    						return account;
	    				}
	    			}
    			}
    		}
    	}
    	return account;
    }
    
    
    /**
     * Comprueba si se está ejecutando el s3backer 
     * @return
     * @throws Exception
     */
    public boolean isCloudRunning() throws Exception {
    	return Command.isRunning("s3backer");
    }

    
    /**
     * Obtiene los discos en la nube que están sin asignar
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> getUnassignedCloudDisks() throws Exception {
    	List<Map<String, String>> _cloud_disks = getCloudDisks();
    	List<String> _assigned_disks = VolumeManager.getPhysicalVolumeNames();
    	    	
    	List<Map<String, String>> _unassigned_cloud_disks = new ArrayList<Map<String, String>>();
    	if (_cloud_disks != null && _cloud_disks.size()>0) {
    		logger.debug("Encontrados {} cloud disks", _cloud_disks.size());
    		if (_assigned_disks != null && _assigned_disks.size()>0) {
    			for (Map<String, String> _cloud_disk : _cloud_disks) {
    				if (!_assigned_disks.contains(_cloud_disk.get("device"))) {
    					logger.debug("Cloud disk {} sin asignar", _cloud_disk.get("device"));
    					_unassigned_cloud_disks.add(_cloud_disk);
    				}
    			}
    		} else {
    			_unassigned_cloud_disks.addAll(_cloud_disks);
    		}
    	} else {
    		logger.debug("Ningún cloud disk");
    	}
    	
    	return _unassigned_cloud_disks;
    }
    
    
    /**
     * Devuelve el listado de discos que s3backer esta gestionando actualmente
     * @return
     * @throws Exception
     */
    public static List<Map<String, String>> getCloudDisks() throws Exception {
    	List<Map<String, String>> _disks = new ArrayList<Map<String, String>>();
    	String output = Command.systemCommand("ps axww | grep s3backer");
    	if (output != null && output.length()>0) {
    		StringTokenizer _st = new StringTokenizer(output, "\n");
    		while (_st.hasMoreTokens()) {
    			String line = _st.nextToken();
    			if (line.contains(WBSAirbackConfiguration.getBinS3backer())) {
    				try {
						Map<String, String> disk = new HashMap<String, String>();
						String account = line.substring(line.indexOf(WBSAirbackConfiguration.getDirectoryCloud()+"/")+(WBSAirbackConfiguration.getDirectoryCloud()+"/").length(), line.indexOf("/.s3backer_"));
						disk.put("account", account);
						String pathCloud = line.substring(line.indexOf(getFileSystemAccountPath(account)+"/cld"));
						String cld = pathCloud.substring(pathCloud.indexOf("cld"));
						pathCloud+="/file";
						disk.put("path-clsys", pathCloud);
						disk.put("cld", cld);
						String tmpSize = line.substring(line.indexOf("--size=")+"--size=".length());
						tmpSize = tmpSize.substring(0, tmpSize.indexOf(" "));
						disk.put("size", VolumeManager.getFormatSize(tmpSize));
						disk.put("size-raw", VolumeManager.getByteSizeFromHuman(tmpSize).toString());
						disk.put("size-backer", tmpSize);
						if (line.contains("--atmos")) {
							disk.put("type", "Atmos");
						} else {
							disk.put("type", "S3");
							String bucket = line.substring(line.indexOf("--listBlocks ")+"--listBlocks ".length());
							bucket = bucket.substring(0, bucket.indexOf(" "));
							disk.put("bucket", bucket);
						}
						String device = findDeviceOfCloudPath(pathCloud);
						if (device != null)
							disk.put("device", device);
						_disks.add(disk);
    				} catch (Exception ex) {
    					logger.error("Ocurrio un error en getCloudDisks. Ex: {}", ex.getMessage());
    				}
    			}
    		}
    	}
    	return _disks;
    }
    
    
    /**
     * Inializa los discos cloud que hubiesen creados, se debe invocar al inicio del sistema, antes de que lvm vea sus particiones
     * @throws Exception
     */
    public static void initCloudDisks() throws Exception {
    	List<String> accounts = listAccountAliases();
    	for (String account : accounts) {
    		File sysDir = new File (getFileSystemAccountPath(account));
    		if (sysDir.exists()) {
    			String[] cloudSystems = sysDir.list();
    			for (String cldDir : cloudSystems) {
    				File fileConfig = new File(getConfigFilePath(account, cldDir));
    				if (fileConfig.exists()) {
    					logger.info("Encontrado sistema cloud [cuenta:{}, cld:{}] intentamos relanzarlo ...", account, cldDir);
    					try {
    						relaunchCloudSystem(account, cldDir);
    					} catch (Exception ex) {
    						logger.error("Error: "+ex.getMessage());
    					}
    				}
    			}
    		}
    	}
    }
    
    
    /**
     * Indica si cierto dispositivo es un disco cloud o no
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean isCloudDevice(String device) throws Exception {
    	try {
    		List<Map<String, String>> cloudDisks = getCloudDisks();
    		for (Map<String, String> cld : cloudDisks) {
    			if (cld.get("device").equals(device))
    				return true;
    		}
    		return false;
    	} catch (Exception ex) {
    		logger.error("Ocurrio un error en isCloudDevice. Ex: {}", ex.getMessage());
    		return false;
    	}
    }
    
    
    /**
     * Indica si un grupo es tipo cloud o no
     * @param group
     * @return
     * @throws Exception
     */
    public static boolean isCloudGroup(String group) throws Exception {
    	try {
	    	List<String> disks = VolumeManager.getPhysicalVolumeNames(group);
	    	if (disks != null && disks.size()>0) {
	    		for (String disk : disks)
	    			if (isCloudDevice(disk))
	    				return true;
	    	}
	    	return false;
    	} catch (Exception ex) {
    		logger.error("Ocurrio un error en isCloudGroup. Ex: {}", ex.getMessage());
    		return false;
    	}
    }
    
    
    /**
     * Obtiene un listado de discos cloud a partir de su dispositivo fisico
     * @param device
     * @return
     * @throws Exception
     */
    public static List<Map<String, String>> getCloudDisksByGroup(String group) throws Exception {
    	List<Map<String, String>> cloudDisks = new ArrayList<Map<String, String>>();
		List<String> disks = VolumeManager.getPhysicalVolumeNames(group);
    	if (disks != null && disks.size()>0) {
    		for (String disk : disks) {
    			if (isCloudDevice(disk)) {
    				cloudDisks.add(getCloudDiskByDevice(disk));
    			}
    		}
    	}
    	return cloudDisks;
    }
    
    
    /**
     * Obtiene un disco cloud a partir de su dispositivo fisico
     * @param device
     * @return
     * @throws Exception
     */
    public static Map<String, String> getCloudDiskByDevice(String device) throws Exception {
    	List<Map<String, String>> cloudDisks = getCloudDisks();
    	for (Map<String, String> cld : cloudDisks) {
    		if (cld.get("device").equals(device))
    			return cld;
    	}
    	return null;
    }
    
    
    /**
     * Devuelve un listado de líneas con las estadísticas de cada uno de los discos cloud de un agregado
     * @param account
     * @param cldSystem
     * @return
     * @throws Exception
     */
    public static Map<String, List<String>> getCloudSystemStats(String group) throws Exception {
    	Map<String, List<String>> statistics = new HashMap<String, List<String>>();
    	List<Map<String, String>> cloudDisks = getCloudDisksByGroup(group);
    	if (cloudDisks != null && cloudDisks.size()>0) {
    		for (Map<String, String> disk : cloudDisks) {
    			List<String> stat = getDeviceCloudStats(disk.get("account"), disk.get("cld"));
    			statistics.put(disk.get("device"), stat);
    		}
    	}
    	return statistics;
    }
    
    
    /**
     * Devuelve las estadísticas de un sistema
     * @param account
     * @param cldSystem
     * @return
     * @throws Exception
     */
    public static List<String> getDeviceCloudStats(String account, String cldSystem) throws Exception {
    	List<String> lineStats = new ArrayList<String>();
    	String stats = FileUtils.fileToString(getCloudSystemStatsPath(account, cldSystem));
    	StringTokenizer st = new StringTokenizer(stats, "\n");
    	while (st.hasMoreTokens()) {
    		String line = st.nextToken();
    		lineStats.add(line);
    	}
    	return lineStats;
    }
}
