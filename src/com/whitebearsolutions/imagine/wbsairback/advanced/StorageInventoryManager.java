package com.whitebearsolutions.imagine.wbsairback.advanced;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.io.FileUtils;
import com.whitebearsolutions.util.Configuration;

public class StorageInventoryManager {

	private final static Logger logger = LoggerFactory.getLogger(StorageInventoryManager.class);
	public final static String path = WBSAirbackConfiguration.getDirectoryAdvancedRemoteInventory();
	
	public final static String iqnwwn_nameVar = "storage_iqn_wwn";
	public final static String airback_iqn_nameVar = "wbsairback_iqn";
	public final static String airback_wwn_nameVar = "wbsairback_wwn";	
	
	static {
		if ( !(new File(path).exists()) ) {
			new File(path).mkdirs();
		}
	}
	
	
	public static String getNameHiddenFileset(String inventory, String type) throws Exception {
		return type+"---"+inventory+"---hidden";
	}
	
	/**
	 * Comprueba si ya existe un storage 
	 * @param storageName
	 * @return
	 */
	public static boolean existsStorage(String storageName) throws Exception {
		try {
			logger.debug("Comprobando si storage {} existe ...",storageName);
			List<String> list = listStorageNames();
			if (list.contains(storageName)) {
				logger.debug("El storage {} existe",storageName);
				return true;
			}
			logger.debug("El storage {} no existe",storageName);
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si existe el storage: {}.Ex: {}", storageName, ex.getMessage());
			throw new Exception("Error checking if exists storageName "+storageName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Lista los nombres de los storages definidos
	 * @return
	 */
	public static List<String> listStorageNames() throws Exception {
		try {
			logger.debug("Listando nombres de storages ...");
			List<String> storages = new ArrayList<String>();
	    	String[] listDir = new File(path).list();
	    	if (listDir != null) {
	    		for (String el : listDir) {
	    			if (el.contains(".xml"))
	    				storages.add(el.substring(0, el.indexOf(".xml")));
	    		}
	    	}
	    	logger.debug("Encontrados {} nombres de storages", storages.size());
	    	return storages;
		} catch (Exception ex) {
			logger.error("Error listando nombres de storages. Ex: {}", ex.getMessage());
			throw new Exception("Error listing storage names. Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Lista los storages definidos en formato de mapas de valores
	 * @return
	 */
	public static List<Map<String, Object>> listStorages() throws Exception {
		try {
			logger.debug("Listando storages ...");
			List<Map<String, Object>> storages = new ArrayList<Map<String, Object>>();
			List<String> storageNames = listStorageNames();
			if (storageNames != null && storageNames.size()>0) {
				for (String name : storageNames) {
					Map<String, Object> storageValues = getStorage(name);
					if (storageValues != null && storageValues.size()>0)
						storages.add(storageValues);
				}
			}
			logger.debug("Encontrados {} storages", storages.size());
			return storages;
		} catch (Exception ex) {
			logger.error("Error listando storages. Ex: {}", ex.getMessage());
			throw new Exception("Error listing storages. Ex:"+ex.getMessage());
		}
	}
	
	public static List<String> listStorageNamesByAdvanced(String typeAdvanced) throws Exception {
		try {
			logger.debug("Listando storages de tipo advanced {} ...");
			List<String> storages = new ArrayList<String>();
			List<String> storageNames = listStorageNames();
			if (storageNames != null && storageNames.size()>0) {
				for (String name : storageNames) {
					Map<String, Object> storageValues = getStorage(name);
					if (storageValues != null && storageValues.size()>0 && storageValues.containsKey("typesAdvanced")) {
						@SuppressWarnings("unchecked")
						List<String> storagesNames = (List<String>) storageValues.get("typesAdvanced");
						if (storagesNames.contains(typeAdvanced))
							storages.add(name);
					}
				}
			}
			logger.debug("Encontrados {} storages", storages.size());
			return storages;
		} catch (Exception ex) {
			logger.error("Error listando storages. Ex: {}", ex.getMessage());
			throw new Exception("Error listing storages. Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Obtiene el path asociado a un storage 
	 * @param nameStorage
	 * @return
	 * @throws Exception
	 */
	public static String getPathStorage(String nameStorage) throws Exception {
		return path+"/"+nameStorage+".xml";
	}
	
	
	/**
	 * Obtiene el path asociado al certificado ssh, si lo hay 
	 * @param nameStorage
	 * @return
	 * @throws Exception
	 */
	public static String getSshCertificatePath(String storageName) {
		return path+"/"+storageName+"_cert.crt";
	}
	
	
	/**
	 * Obtiene los valores de un storage dado
	 * @param storageName
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getStorage(String storageName) throws Exception {
		try {
			Map<String, Object> storage = new HashMap<String, Object>();
			File file = new File (getPathStorage(storageName));
			if (file.exists()) {
				DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document _doc = _db.parse(file);
				Node storageNode = _doc.getElementsByTagName("storage").item(0);
				NodeList list = storageNode.getChildNodes();
				for (int i=0;i < list.getLength();i++) {
					if(list.item(i).getNodeType() == Node.ELEMENT_NODE) {
			    		Element e = (Element) list.item(i);
			    		if (e.getNodeName() != null && (e.getNodeName().equals("name") || e.getNodeName().equals("address") || e.getNodeName().equals("port") || e.getNodeName().equals("user") || e.getNodeName().equals("password") || e.getNodeName().equals("iqnwwn")))
			    			storage.put(e.getNodeName(), e.getTextContent());
			    		else if (e.getNodeName().equals("typesAdvanced")) {
			    			NodeList listTypes = e.getChildNodes();
			    			List<String> typesAdvanced = new ArrayList<String>();
			    			for (int j=0;j < listTypes.getLength();j++) {
			    				if(listTypes.item(j).getNodeType() == Node.ELEMENT_NODE) {
			    					Element ad = (Element) listTypes.item(j);
			    					if (ad.getNodeName().equals("typeAdvanced")) {
			    						typesAdvanced.add(ad.getTextContent());
			    					}
			    				}
			    			}
			    			if (typesAdvanced.size()>0)
			    				storage.put("typesAdvanced", typesAdvanced);
			    		}
					}
				}
			}
			
			File fileCert = new File (getSshCertificatePath(storageName));
			if (fileCert.exists()) {
				String certificate = FileUtils.fileUTF8ToString(getSshCertificatePath(storageName));
				storage.put("certificate", certificate);
			}
			
			return storage;
		} catch (Exception ex) {
			logger.error("Error obteniedo datos de storage: {}. Ex: {}", storageName, ex.getMessage());
			throw new Exception("Error obtaining storage data of "+storageName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Elimina un storage
	 * @param nameStorage
	 * @throws Exception
	 */
	public static void removeStorage(String nameStorage) throws Exception {
		try {
			logger.info("Eliminando storage {} ...", nameStorage);
			if (existsStorage(nameStorage)) {
				if (hasStorageJobsAssociated(nameStorage)){
					throw new Exception("The storage cannot delete, there are jobs associated to this storage.");
				}
				File f = new File (getPathStorage(nameStorage));
				if (f.exists()) {
					f.delete();
					logger.info("Storage {} eliminado.", nameStorage);
				}
				
				File fileCert = new File (getSshCertificatePath(nameStorage));
				if (fileCert.exists()) {
					fileCert.delete();
				}
			}
		} catch (Exception ex) {
			logger.error("Error borrando storage: {}. Ex: {}", nameStorage, ex.getMessage());
			throw new Exception("Error removing storage: "+nameStorage+". Ex:"+ex.getMessage());
		}
	}
	

	/**
	 * Comprueba si hay jos asociados al storageRemote
	 * @remoteStorageName
	 * @return
	 */

	public static boolean hasStorageJobsAssociated(String remoteStorageName) throws Exception {
		try {
			logger.debug("recorre jobs ...");
			boolean result=false;
			JobManager _jm=new JobManager(new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration())));
			List<String> _jobs=_jm.getAllProgrammedJobs();
			if (_jobs!=null && !_jobs.isEmpty()){
    			String _storageInventory=null;
    			Map<String, String> job=null;
    			String _fileSet=null;
				for (String _jobName: _jobs){
					job = _jm.getProgrammedJob(_jobName);
					if (job!=null && job.get("fileset")!=null){
		    			_fileSet=job.get("fileset");
		    			if (_fileSet!=null && _fileSet.contains("---hidden")){
		    				StringTokenizer _stoke=new StringTokenizer(_fileSet,"---");
		    				_stoke.nextToken();
		    				if (_stoke.hasMoreTokens()){
		    					_storageInventory=_stoke.nextToken();
		    				}
		    			}
		    			if (_storageInventory!=null && _storageInventory.equals(remoteStorageName)){
		    				result=true;
		    			}
					}
				}
			}
	    	logger.debug("Devuelve el resultado de la comprobacion");
	    	return result;
		} catch (Exception ex) {
			logger.error("Error listando nombres de remote storages. Ex: {}", ex.getMessage());
			throw new Exception("Error listing remote storages. Ex:"+ex.getMessage());
		}
	}
	
	/**
	 * Guarda un storage con sus datos correspondientes
	 * @param nameStorage
	 * @param typeStorage
	 * @throws Exception
	 */
	public static void saveStorage(String nameStorage, List<String> typesAdvanced, String iqnwwn, String address, String port, String user, String password, String certificate, boolean edit) throws Exception {
		
		if (!edit && existsStorage(nameStorage)) {
			throw new Exception ("Another storage already exists with that name");
		}
		
		try {
			logger.info("Guardando storage {}. Address {}, Port {} User {} Password {} ...", new Object[]{nameStorage, address, port, user, password});
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("<?xml version=\"1.0\"?>\n");
			_sb.append("<storage>\n");
			_sb.append("	<name>"+nameStorage.trim()+"</name>\n");
			_sb.append("	<iqnwwn>"+iqnwwn.trim()+"</iqnwwn>\n");
			_sb.append("	<typesAdvanced>\n");
			for (String typeAdvanced : typesAdvanced)
				_sb.append("		<typeAdvanced>"+typeAdvanced.trim()+"</typeAdvanced>\n");
			_sb.append("	</typesAdvanced>\n");
			_sb.append("	<address>"+address.trim()+"</address>\n");
			_sb.append("	<port>"+port.trim()+"</port>\n");
			if (user != null && !user.equals(""))
				_sb.append("	<user>"+user.trim()+"</user>\n");
			if (password != null && !password.equals(""))
				_sb.append("	<password>"+password.trim()+"</password>\n");
			_sb.append("</storage>");
			
			
			FileOutputStream _fos = new FileOutputStream(new File(getPathStorage(nameStorage)));
			_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
			_fos.close();
			
			// Si hay certificado, lo guardamos en el fichero oportuno
			if (certificate != null && !certificate.equals("")) {
				File fileCert = new File(getSshCertificatePath(nameStorage));
				if (fileCert.exists())
					fileCert.delete();
				
				_fos = new FileOutputStream(fileCert);
				_fos.write(certificate.toString().getBytes(Charset.forName("UTF-8")));
	    		_fos.close();
			}
			
			logger.info("Guardado storage {}", nameStorage);
		} catch (Exception ex) {
			logger.error("Error guardando storage: {}. Ex: {}", nameStorage, ex.getMessage());
			throw new Exception("Error saving storage: "+nameStorage+". Ex:"+ex.getMessage());
		}
	}
}
