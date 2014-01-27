package com.whitebearsolutions.imagine.wbsairback.advanced;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

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
import com.whitebearsolutions.imagine.wbsairback.disk.SCSIManager;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.util.Configuration;

public class RemoteStorageManager {
	public static final String CONNECTION_TYPE_SSH = "ssh";
	
	public static final String STORAGE_TYPE_ISCSI = "iSCSI";
	public static final String STORAGE_TYPE_FIBRE = "FC";
	
	public static final String path = WBSAirbackConfiguration.getDirectoryAdvancedRemoteStorage();
	
	private final static Logger logger = LoggerFactory.getLogger(RemoteStorageManager.class);
	
	public static List<String> supportedTypeConn = null;
	public static List<String> supportedTypeStorage = null;
	static {
		supportedTypeConn = new ArrayList<String>();
		supportedTypeConn.add(CONNECTION_TYPE_SSH);

		supportedTypeStorage = new ArrayList<String>();
		supportedTypeStorage.add(STORAGE_TYPE_ISCSI);
		supportedTypeStorage.add(STORAGE_TYPE_FIBRE);
		
		if ( !(new File(path).exists()) ) {
			new File(path).mkdirs();
		}
		
		if ( !(new File(WBSAirbackConfiguration.getDirectoryRemoteStorageMount()).exists()) ) {
			new File(WBSAirbackConfiguration.getDirectoryRemoteStorageMount()).mkdirs();
		}
	}
	
	public static String getMountPathRemoteStorage(String storageName, String typeStorage, String nameJob) {
		return WBSAirbackConfiguration.getDirectoryRemoteStorageMount()+"/"+typeStorage+"/"+storageName+"/"+nameJob;
	}
	
	
	/**
	 * Comprueba si ya existe un remoteStorage
	 * @param remoteStorageName
	 * @return
	 */
	public static boolean existsStorage(String remoteStorageName) throws Exception {
		try {
			logger.debug("Comprobando si remoteStorage {} existe ...",remoteStorageName);
			List<String> list = listRemoteStorageNames();
			if (list.contains(remoteStorageName)) {
				logger.debug("El remoteStorage {} existe",remoteStorageName);
				return true;
			}
			logger.debug("El remoteStorage {} no existe",remoteStorageName);
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si existe el remoteStorage: {}.Ex: {}", remoteStorageName, ex.getMessage());
			throw new Exception("Error checking if exists remoteStorageName "+remoteStorageName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Comprueba si hay jos asociados al storageRemote
	 * @remoteStorageName
	 * @return
	 */
	
	public static boolean hasRemoteStorageJobsAssociated(String remoteStorageName) throws Exception {
		try {
			logger.debug("recorre jobs ...");
			boolean result=false;
			JobManager _jm=new JobManager(new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration())));
			List<String> _jobs=_jm.getAllProgrammedJobs();
			if (_jobs!=null && !_jobs.isEmpty()){
    			String _advancedStorage=null;
    			Map<String, String> job=null;
    			String _fileSet=null;
				for (String _jobName: _jobs){
					job = _jm.getProgrammedJob(_jobName);
					if (job!=null && job.get("fileset")!=null){
		    			_fileSet=job.get("fileset");
		    			if (_fileSet!=null && _fileSet.contains("---hidden")){
		    				StringTokenizer _stoke=new StringTokenizer(_fileSet,"---");
		    				if (_stoke.hasMoreTokens()){
		    					_advancedStorage=_stoke.nextToken();
		    				}
		    			}
		    			if (_advancedStorage!=null && _advancedStorage.equals(remoteStorageName)){
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
	 * Metodo que nos indica si un remote storage tiene jobs asociados
	 * @return
	 */
	public static List<String> listRemoteStorageNames(String file) throws Exception {
		try {
			if (file == null || file.isEmpty())
				file = path;
			logger.debug("Listando nombres de remoteStorages ...");
			List<String> storages = new ArrayList<String>();
	    	String[] listDir = new File(file).list();
	    	if (listDir != null) {
	    		for (String el : listDir) {
	    			if (el.contains(".xml"))
	    				storages.add(el.substring(0, el.indexOf(".xml")));
	    		}
	    	}
	    	logger.debug("Encontrados {} nombres de remote storages", storages.size());
	    	return storages;
		} catch (Exception ex) {
			logger.error("Error listando nombres de remote storages. Ex: {}", ex.getMessage());
			throw new Exception("Error listing remote storages. Ex:"+ex.getMessage());
		}
	}
	
	public static List<String> listRemoteStorageNames() throws Exception {
		return listRemoteStorageNames(null);
	}
	
	
	/**
	 * Lista los remote storages definidos en formato de mapas de valores
	 * @return
	 */
	public static List<Map<String, Object>> listRemoteStorages(String file) throws Exception {
		try {
			logger.debug("Listando remote storages ...");
			List<Map<String, Object>> storages = new ArrayList<Map<String, Object>>();
			List<String> storageNames = listRemoteStorageNames(file);
			if (storageNames != null && storageNames.size()>0) {
				for (String name : storageNames) {
					Map<String, Object> storageValues = getRemoteStorage(name, file);
					if (storageValues != null && storageValues.size()>0)
						storages.add(storageValues);
				}
			}
			logger.debug("Encontrados {} remote storages", storages.size());
			return storages;
		} catch (Exception ex) {
			logger.error("Error listando remote storages. Ex: {}", ex.getMessage());
			throw new Exception("Error listing remote storages. Ex:"+ex.getMessage());
		}
	}
	
	public static List<Map<String, Object>> listRemoteStorages() throws Exception {
		return listRemoteStorages(null);
	}
	
	
	/**
	 * Obtiene el path asociado a un remote storage 
	 * @param nameStorage
	 * @return
	 * @throws Exception
	 */
	public static String getPathRemoteStorage(String nameRemoteStorage) throws Exception {
		return getPathRemoteStorage(nameRemoteStorage, null);
	}
	
	public static String getPathRemoteStorage(String nameRemoteStorage, String file) throws Exception {
		if (file == null || file.isEmpty())
			file = path;
		return file+"/"+nameRemoteStorage+".xml";
	}
	
	
	public static Map<String, Object> getRemoteStorage(String storageName) throws Exception {
		return getRemoteStorage(storageName, null);
	}
	
	
	/**
	 * Obtiene los valores de un storage dado
	 * @param storageName
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getRemoteStorage(String storageName, String pathFile) throws Exception {
		try {
			Map<String, Object> storage = new HashMap<String, Object>();
			File file = new File (getPathRemoteStorage(storageName, pathFile));
			if (file.exists()) {
				DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document _doc = _db.parse(file);
				Node storageNode = _doc.getElementsByTagName("storage").item(0);
				NodeList list = storageNode.getChildNodes();
				for (int i=0;i < list.getLength();i++) {
					if(list.item(i).getNodeType() == Node.ELEMENT_NODE) {
			    		Element e = (Element) list.item(i);
			    		if (e.getNodeName() != null && (e.getNodeName().equals("name") || e.getNodeName().equals("typeConnection") || e.getNodeName().equals("typeStorage")))
			    			storage.put(e.getNodeName(), e.getTextContent());
			    		else if (e.getNodeName() != null && e.getNodeName().equals("variables") ) {
			    			NodeList listVars = e.getChildNodes();
			    			Map<String, Map<String, String>> vars = new TreeMap<String, Map<String, String>> ();
			    			if (listVars.getLength()>0) {
			    				for (int j=0;j < listVars.getLength();j++) {
			    					if(listVars.item(j).getNodeType() == Node.ELEMENT_NODE) {
				    					Element v = (Element) listVars.item(j);
				    					if (v.getNodeName() != null && v.getNodeName().equals("var")) {
				    						String nameVar = null;
		    								String descriptionVar = null;
		    								String passwordVar = null;
		    								Map<String, String> var = new HashMap<String, String>();
				    						NodeList listElemsVar = v.getChildNodes();
				    						for (int k=0;k < listElemsVar.getLength();k++) {
				    							if(listElemsVar.item(k).getNodeType() == Node.ELEMENT_NODE) {
				    								Element elemVar = (Element) listElemsVar.item(k);
				    								if (elemVar.getNodeName() != null && (elemVar.getNodeName().equals("name"))) {
				    									nameVar = elemVar.getTextContent();
				    								} else if (elemVar.getNodeName() != null && (elemVar.getNodeName().equals("description"))) {
				    									descriptionVar = elemVar.getTextContent();
				    								} else if (elemVar.getNodeName() != null && (elemVar.getNodeName().equals("password"))) {
				    									passwordVar = elemVar.getTextContent();
				    								}
				    							}
				    						}
				    						var.put("name", nameVar);
				    						if (descriptionVar != null) {
				    							var.put("description", descriptionVar);
				    						}
				    						if (passwordVar != null) {
				    							var.put("password", passwordVar);
				    						}
				    						if (var != null && !var.isEmpty())
				    							vars.put(nameVar, var);
				    					}
			    					}
			    				}
			    			}
			    			storage.put("variables", vars);
			    			
			    		} else if (e.getNodeName() != null && e.getNodeName().equals("steps") ) {
			    			NodeList listSteps = e.getChildNodes();
			    			Map<String, Object> steps = new TreeMap<String, Object> ();
			    			if (listSteps.getLength()>0) {
			    				for (int j=0;j < listSteps.getLength();j++) {
			    					if(listSteps.item(j).getNodeType() == Node.ELEMENT_NODE) {
				    					Element v = (Element) listSteps.item(j);
				    					Map<String, Object> step = new HashMap<String, Object> ();
				    					if (v.getNodeName() != null && v.getNodeName().equals("step")) {
			    							NodeList childsStep = v.getChildNodes();
			    							for (int z=0;z < childsStep.getLength();z++) {
			    								if (childsStep.item(z).getNodeType() == Node.ELEMENT_NODE) {
			    									Element s = (Element) childsStep.item(z);
			    									if (s.getNodeName() != null && (s.getNodeName().equals("scripts"))) {
			    										NodeList listScripts = s.getChildNodes();
			    										Map<Integer, String> scripts = new TreeMap<Integer, String> ();
			    										if (listScripts.getLength()>0) {
			    											for (int y=0;y < listScripts.getLength();y++) {
			    												if(listScripts.item(y).getNodeType() == Node.ELEMENT_NODE) {
			    													Element sc = (Element) listScripts.item(y);
			    													if (sc.getNodeName() != null && (sc.getNodeName().equals("script"))) {
			    														if (sc.getAttribute("order") != null) {
			    															scripts.put(Integer.parseInt(sc.getAttribute("order")), sc.getTextContent());
			    														}
			    													}
			    												}
			    											}
			    										}
			    										step.put("scripts", scripts);
			    									} else if (s.getNodeName() != null && (s.getNodeName().equals("name") || s.getNodeName().equals("mount"))) {
			    										step.put(s.getNodeName(), s.getTextContent());
			    									}
			    								}
			    							}
				    					}
				    					steps.put((String) step.get("name"), step); 
			    					}
			    				}
			    			}
			    			storage.put("steps", steps);
			    		}
					}
				}
			}
			
			return storage;
		} catch (Exception ex) {
			logger.error("Error obteniedo datos de storage: {}. Ex: {}", storageName, ex.getMessage());
			throw new Exception("Error obtaining storage data of "+storageName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Obtiene un step concreto
	 * @param nameStorage
	 * @param nameStep
	 * @param orderStep
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getRemoteStorageStep(String nameStorage, String nameStep) throws Exception {
		try {
			Map<String, Object> step = null;
			Map<String, Object> remoteStorage = getRemoteStorage(nameStorage);
			if (remoteStorage != null && !remoteStorage.isEmpty()) {
				if (remoteStorage.get("steps") != null) {
					Map<Integer, Object> steps = (Map<Integer, Object>) remoteStorage.get("steps");
					if (!steps.isEmpty()) {
						Map<String, Object> stepFound = (Map<String, Object>) steps.get(nameStep);
						if (stepFound != null) {
							logger.debug("Encontrado step: nombre {}, orden: {}", stepFound.get("name"), stepFound.get("order"));
							return stepFound;
						}
					}
				}
			}
			return step;
		} catch (Exception ex) {
			logger.error("Error obteniedo datos de remote step: storage: {}, nameStep:{}, order:{} . Ex: {}", new Object[]{nameStorage, nameStep, ex.getMessage()});
			throw new Exception("Error obtaining remote storage step data of storage: "+nameStorage+", nameStep:"+nameStep+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Elimina un step de remote storage
	 * @param nameStorage
	 * @param nameStep
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void removeRemoteStorageStep(String nameStorage, String nameStep) throws Exception {
		try {
			Map<String, Object> remoteStorage = getRemoteStorage(nameStorage);
			if (remoteStorage != null && !remoteStorage.isEmpty()) {
				if (remoteStorage.get("steps") != null) {
					List<Map<String, String>> stepsTemplate = TemplateJobManager.getStepsWithProperty("name", nameStep);
					if (stepsTemplate != null && stepsTemplate.size()>0) {
						for (Map<String, String> step : stepsTemplate) {
							if (step.containsKey("advanced_storage") && step.get("advanced_storage") != null && step.get("advanced_storage").equals(nameStorage))
								throw new Exception("Step is used in some template jobs");
						}
					}
					
					Map<String, Object> steps = (Map<String, Object>) remoteStorage.get("steps");
					if (!steps.isEmpty()) {
						Map<String, Map<String, String>> vars = null;
						Map<String, Object> stepFound = (Map<String, Object>) steps.get(nameStep);
						if (stepFound != null) {
							steps.remove(nameStep);
							if (remoteStorage.get("variables") != null) {
								vars = (Map<String, Map<String, String>>) remoteStorage.get("variables");
							}
							writeRemoteStorageXML(nameStorage, (String) remoteStorage.get("typeConnection"), (String) remoteStorage.get("typeStorage"), vars, steps);
							logger.debug("Eliminado step: nombre {}, orden: {}", stepFound.get("name"), stepFound.get("order"));
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error eliminando remote storage step: storage: {}, nameStep:{}, order:{} . Ex: {}", new Object[]{nameStorage, nameStep, ex.getMessage()});
			throw new Exception("Error deleting remote storage step of: storage: "+nameStorage+", nameStep:"+nameStep+". Ex:"+ex.getMessage());
		}
	}
	
	/**
	 * Elimina un remote storage
	 * @param nameStorage
	 * @throws Exception
	 */
	public static void removeRemoteStorage(String nameStorage) throws Exception {
		try {
			logger.info("Eliminando remote storage {} ...", nameStorage);
			if (existsStorage(nameStorage)) {
				File f = new File (getPathRemoteStorage(nameStorage));
				if (f.exists()) {
					if (RemoteStorageManager.hasRemoteStorageJobsAssociated(nameStorage)){
						throw new Exception("The remote storage cannot delete, there are jobs associated to this");
					}
					String isInTemplate = TemplateJobManager.isEntityInAnyTemplateJob(nameStorage, StepManager.TYPE_STEP_ADVANCED_STORAGE);
					if (isInTemplate == null) {
						f.delete();
						logger.info("Storage {} eliminado.", nameStorage);
					} else {
						logger.info("Storage {} relacionado con {}. No se puede eliminar", nameStorage, isInTemplate);
						throw new Exception("Storage is associated with a tempate job step: "+isInTemplate);
						
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error borrando remote storage: {}. Ex: {}", nameStorage, ex.getMessage());
			throw new Exception("Error removing remote storage: "+nameStorage+". Ex:"+ex.getMessage());
		}
	}
	

	/**
	 * Almacena un step con sus scripts en un remote storage
	 * @param nameStorage
	 * @param nameStep
	 * @param scripts
	 * @param edit
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void saveRemoteStorageStep(String nameStorage, String nameStep, Map<Integer, String> scripts, boolean mount, boolean edit) throws Exception {
		try {
			boolean exists = false;
			exists = existsStorageStep(nameStorage, nameStep);
			
			if (!edit && exists) {
				throw new Exception ("Another storage step already exists with that name");
			}
			
			Map<String, Map<String, String>> vars = null;
			Map<String, Object> steps = null;
			Map<String,Object> storage = getRemoteStorage(nameStorage);
			if (storage.get("steps") != null) {
				steps = (Map<String, Object>) storage.get("steps");
			} else {
				steps = new TreeMap<String, Object>();
			}
			
			if (storage.get("variables") != null) {
				vars = (Map<String, Map<String, String>>) storage.get("variables");
			}
			
			Map<String, Object> step = new HashMap<String, Object>();
			step.put("name", nameStep);
			if (mount)
				step.put("mount", "true");
			else
				step.put("mount", "false");
			step.put("scripts", scripts);
			
			steps.put(nameStep, step);
			
			writeRemoteStorageXML(nameStorage, (String) storage.get("typeConnection"), (String) storage.get("typeStorage"), vars, steps);		
		} catch (Exception ex) {
			logger.error("Error guardando remote storage step: storage: {} step: {}. Ex: {}", new Object[]{nameStorage, nameStep, ex.getMessage()});
			throw new Exception("Error saving remote storage step: storage:"+nameStorage+" step:"+nameStep+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Comprueba si existe un step de cierto remote storage
	 * @param nameStorage
	 * @param nameStep
	 * @return
	 * @throws Exception
	 */
	public static boolean existsStorageStep(String nameStorage, String nameStep) throws Exception {
		try {
			Map<String,Object> storage = getRemoteStorage(nameStorage);
			if (storage != null && !storage.isEmpty()) {
				if (storage.get("steps") != null) {
					@SuppressWarnings("unchecked")
					Map<String, Object> steps = (Map<String, Object>) storage.get("steps");
					if (!steps.isEmpty() && steps.containsKey(nameStep))
						return true;
					}
			}
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si existe remote storage step: storage: {} step: {}. Ex: {}", new Object[]{nameStorage, nameStep, ex.getMessage()});
			throw new Exception("Error checking if remote storage step exists: storage:"+nameStorage+" step:"+nameStep+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Guarda un storage con sus datos correspondientes
	 * @param nameStorage
	 * @param typeStorage
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void saveStorage(String nameStorage, String typeConnection, String typeStorage, Map<String, Map<String, String>> vars, boolean edit) throws Exception {
		
		boolean exists = false;
		exists = existsStorage(nameStorage);
		
		if (!edit && exists) {
			throw new Exception ("Another storage already exists with that name");
		}
		
		if (!supportedTypeConn.contains(typeConnection)) {
			throw new Exception ("Unsupported type connection");
		}
		
		if (!supportedTypeStorage.contains(typeStorage)) {
			throw new Exception ("Unsupported type Storage");
		}
		
		Map<String,Object> steps = null;
		Map<String,Object> storage = null;
		if (exists) {
			storage = getRemoteStorage(nameStorage);
			steps = (Map<String,Object>) storage.get("steps");
		}
		
		try {
			logger.info("Guardando storage {}. typeConnection {}, typeStorage {}...", new Object[]{nameStorage, typeConnection, typeStorage});
			writeRemoteStorageXML(nameStorage, typeConnection, typeStorage, vars, steps);
			logger.info("Guardado remote storage {}", nameStorage);
		} catch (Exception ex) {
			logger.error("Error guardando storage: {}. Ex: {}", nameStorage, ex.getMessage());
			throw new Exception("Error saving storage: "+nameStorage+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Genera el xml completo correspondiente a un remote storage
	 * @param nameStorage
	 * @param typeConnection
	 * @param typeStorage
	 * @param vars
	 * @param steps
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void writeRemoteStorageXML(String nameStorage, String typeConnection, String typeStorage, Map<String, Map<String, String>> vars, Map<String, Object> steps) throws Exception {
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("<?xml version=\"1.0\"?>\n");
			_sb.append("<storage>\n");
			_sb.append("	<name>"+nameStorage.trim()+"</name>\n");
			_sb.append("	<typeConnection>"+typeConnection.trim()+"</typeConnection>\n");
			_sb.append("	<typeStorage>"+typeStorage.trim()+"</typeStorage>\n");
			_sb.append("	<variables>\n");
			if (vars != null && vars.size()>0) {
				for (String nameVar : vars.keySet()) {
					Map<String, String> var = vars.get(nameVar);
					_sb.append("	<var>\n");
					_sb.append("	<name>");
					_sb.append(var.get("name"));
					_sb.append("</name>\n");
					_sb.append("	<description>");
					_sb.append(var.get("description"));
					_sb.append("</description>\n");
					_sb.append("	<password>");
					_sb.append(var.get("password"));
					_sb.append("</password>\n");
					_sb.append("	</var>\n");
				}
			}
			_sb.append("	</variables>\n");
			_sb.append("	<steps>\n");
			if (steps != null && steps.size()>0) {
				for (String nameStep : steps.keySet()) {
					_sb.append("	<step>");
					_sb.append("	<name>"+nameStep+"</name>\n");
					if (steps.get(nameStep) != null) {
						Map<String, Object> step = (Map<String, Object>) steps.get(nameStep);
						_sb.append("	<mount>"+(String) step.get("mount")+"</mount>\n");
						Map<Integer, Object> scripts = (Map<Integer, Object>) step.get("scripts");
						_sb.append("	<scripts>\n");
						if (scripts != null && scripts.size()>0) {
							for (Integer orderScript : scripts.keySet()) {
								_sb.append("	<script order=\""+orderScript+"\">");
								_sb.append(scripts.get(orderScript));
								_sb.append("</script>\n");
							}
						}
						_sb.append("	</scripts>\n");
					}
					_sb.append("</step>\n");
				}
			}
			_sb.append("	</steps>\n");
			_sb.append("</storage>");
			
			FileOutputStream _fos = new FileOutputStream(new File(getPathRemoteStorage(nameStorage)));
			_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
			_fos.close();
		} catch (Exception ex) {
			logger.error("Error guardando xml de storage: {}. Ex: {}", nameStorage, ex.getMessage());
			throw new Exception("Error saving remote storage xml: "+nameStorage+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Genera los scripts concretos del remoteStorage recibido, a adjuntar en el job recibido
	 * @param nameJob
	 * @param storageInventory
	 * @param remoteStorage
	 * @param variablesValues
	 * @param jm
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void generateScriptsJob(String nameJob, String stepName, Map<String, Object> storageInventory, Map<String, Object> remoteStorage, Map<String, String> variablesValues, JobManager jm, ISCSIManager ism) throws Exception {
		try {
			boolean blockSCSI = false;
			Map<String, Object> stepStorage = RemoteStorageManager.getRemoteStorageStep((String) remoteStorage.get("name"), stepName);
			if (stepStorage != null && !stepStorage.isEmpty()) {
				if (stepStorage.get("mount") != null && ((String)stepStorage.get("mount")).equals("true"))
					blockSCSI = true;
			}
			Map<String, Object> steps = (Map<String, Object>) remoteStorage.get("steps");
			if (steps != null && !steps.isEmpty()) {
				for (String nameStep : steps.keySet()) {
					if (nameStep.equals(stepName)) {
						Map<String, Object> step = (Map<String, Object>) steps.get(nameStep);
						
						if (blockSCSI) {
							jm.setJobScript(getNameRemoteStorageBlockingScript(), nameJob, getRemoteStorageScriptBlockingCommand(), true, true, true, true, false, null, false);
						}
						
						if (step.get("scripts") != null) {
							Map<Integer, Object> scripts = (Map<Integer, Object>) step.get("scripts");
							if (!scripts.isEmpty()) {
								for (Integer order : scripts.keySet()) {
									String command = (String) scripts.get(order);
									
									variablesValues.put(StorageInventoryManager.iqnwwn_nameVar, (String) storageInventory.get("iqnwwn"));
									if (ISCSIManager.getClientInitiatorName() != null){
										variablesValues.put(StorageInventoryManager.airback_iqn_nameVar, ISCSIManager.getClientInitiatorName());
									}
									if (SCSIManager.getClientFibreChannelWWN() != null){
										variablesValues.put(StorageInventoryManager.airback_wwn_nameVar, SCSIManager.getClientFibreChannelWWN());
									}

									command = getSmartContent(command, variablesValues);
									
									if ( ((String)remoteStorage.get("typeConnection")).equals(CONNECTION_TYPE_SSH)) {
										String path_cert = StorageInventoryManager.getSshCertificatePath((String) storageInventory.get("name"));
										File cert = new File(path_cert);
										if (!cert.exists())
											path_cert = null;
										command = generateRemoteCommand((String) storageInventory.get("address"), (String) storageInventory.get("user"), (String) storageInventory.get("password"), path_cert, command);
									}
									
									String pathScript = WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + nameJob + "_" + getNameRemoteStorageScript(order)+".sh";
									FileOutputStream _fos = new FileOutputStream(new File(pathScript));
									_fos.write(command.getBytes(Charset.forName("UTF-8")));
						    		_fos.close();
									jm.setJobScript(getNameRemoteStorageScript(order), nameJob, getRemoteStorageScriptCommand(pathScript), true, true, true, true, false, variablesValues);
								}
							}
						}
						return;
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error generando scripts para el job: {}. Ex: {}", nameJob, ex.getMessage());
			throw new Exception("Error generating script jobs for: "+nameJob+". Ex:"+ex.getMessage());
		}
	}
		
	
	/**
	 * Genera los comandos asociados a un job de backup que esta asociado a un remote storage
	 * @param nameJob
	 * @param remoteStorage
	 * @param storageInventory
	 * @param iscsim
	 * @param jm
	 * @throws Exception
	 */
	public static void generateBackupRemoteScripts(String nameJob, Map<String, Object> storageInventory, String typeStorage, ISCSIManager iscsim, JobManager jm) throws Exception {
		String mountPath = getMountPathRemoteStorage((String) storageInventory.get("name"), typeStorage, nameJob);
		
		File f = new File(mountPath);
		if (!f.exists())
			f.mkdirs();
		
		// Generamos el comando de montaje como pre-script
		String mountCommand = getRemoteStorageBackupMountCommands(mountPath, typeStorage, (String) storageInventory.get("iqnwwn"), (String) storageInventory.get("user"), (String) storageInventory.get("password"), (String) storageInventory.get("address"));
		String pathScript = WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + nameJob + "_" + getNameRemoteStorageBackupScriptName(true)+".sh";
		FileOutputStream _fos = new FileOutputStream(new File(pathScript));
		_fos.write(mountCommand.getBytes(Charset.forName("UTF-8")));
		_fos.close();
		jm.setJobScript(getNameRemoteStorageBackupScriptName(true), nameJob, getRemoteStorageScriptMountCommand(pathScript), true, true, false, true, false, null, false);
		
		// Generamos el comando de desmontaje como postScript
		String umountCommand = getRemoteStorageBackupUmountCommands(mountPath, (String) storageInventory.get("iqnwwn"), (String) storageInventory.get("address"), typeStorage);
		pathScript = WBSAirbackConfiguration.getDirectoryJobs() + "/scripts/" + nameJob + "_" +getNameRemoteStorageBackupScriptName(false)+".sh";
		_fos = new FileOutputStream(new File(pathScript));
		_fos.write(umountCommand.getBytes(Charset.forName("UTF-8")));
		_fos.close();
		jm.setJobScript(getNameRemoteStorageBackupScriptName(false), nameJob, getRemoteStorageScriptMountCommand(pathScript), false, true, false, true, false, null, false);
	}
	
	public static String getRemoteStorageScriptBlockingCommand() throws Exception {
		return "/usr/bin/sudo /usr/sbin/wbsairback-scsi-block";
	}
		
	/**
	 * Obtiene el nombre de un script asociado a un job de un remote storage
	 * @param nameJob
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public static String getNameRemoteStorageScript(Integer order) throws Exception {
		return "remote_"+order;
	}
	
	
	public static String getNameRemoteStorageBlockingScript() throws Exception {
		return "remote___blocking";
	}
	
	/**
	 * Obtiene el nombre de un script asociado a un job de backup remote storage
	 * @param nameJob
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public static String getNameRemoteStorageBackupScriptName(boolean mount) throws Exception {
		if (mount)
			return "remotebackup_mount";
		else
			return "remotebackup_umount";
	}
	
	
	/**
	 * Obtiene el comando 'final' a partir del path de un script
	 * @param pathScript
	 * @return
	 * @throws Exception
	 */
	public static String getRemoteStorageScriptCommand(String pathScript) throws Exception {
		return "/usr/bin/expect -f "+pathScript;
	}
	
	public static String getRemoteStorageScriptMountCommand(String pathScript) throws Exception {
		return "bash "+pathScript;
	}
	
	/**
	 * Transforma un comando 'simple' en uno completo para ejecutar sobre ssh.
	 * Puede ser con user+password o con certificate_path+password
	 * @param host
	 * @param user
	 * @param password
	 * @param certificate
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public static String generateRemoteCommand(String host, String user, String password, String certificate_path, String command) throws Exception {
        StringBuilder _command = new StringBuilder();
        _command.append("#!/usr/bin/expect -f\n");
        _command.append("set timeout 60\n");
        _command.append("spawn ssh -x -oStrictHostKeyChecking=no -oCheckHostIP=no ");
        if (certificate_path != null && !certificate_path.isEmpty()) {
        	_command.append("-i ");
        	_command.append(certificate_path);
        	_command.append(" ");
        }
        _command.append(user);
        _command.append("@");
        _command.append(host);
        _command.append(" -C \"");
        _command.append(command);
        _command.append("\"\n");
        if (password != null && !password.isEmpty()) {
	        _command.append("expect {\n");
	        _command.append("   default {exit 2}\n");
	        _command.append("   \"*?assword:*\"\n");
	        _command.append("}\n");
	        _command.append("send ");
	        _command.append(password);
	        _command.append("\\n\"\n");
        }
        _command.append("interact\n");
        _command.append("catch wait reason\n");
        _command.append("set exit_status [lindex $reason 3]\n");
        _command.append("exit $exit_status\n");
        
        return _command.toString();
	}
	
	
	/**
	 * Mapea y monta una lun de storage remoto externo
	 * @param mountPath
	 * @param storage_type
	 * @param volume
	 * @param snapshot
	 * @param iqn_wwn
	 * @param iscsim
	 * @return
	 * @throws Exception
	 */
	public static String getRemoteStorageBackupMountCommands(String mountPath, String storage_type, String iqn_wwn, String user, String password, String address) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		String msg = "iscsi device mount error";
		if (storage_type.equals(RemoteStorageManager.STORAGE_TYPE_FIBRE)) {
			_sb.append("DEVICE=$(/usr/bin/sudo /usr/sbin/wbsairback-scsi-login "+address+" "+iqn_wwn+" "+RemoteStorageManager.STORAGE_TYPE_FIBRE+")\n");
			msg = "fc device mount error";
		} else {
			_sb.append("sleep 2\n");
			if (user != null && !user.isEmpty() && password != null && !password.isEmpty())
				_sb.append("DEVICE=$(/usr/bin/sudo /usr/sbin/wbsairback-scsi-login "+address+" "+iqn_wwn+" "+RemoteStorageManager.STORAGE_TYPE_ISCSI+")\n");
			else
				_sb.append("DEVICE=$(/usr/bin/sudo /usr/sbin/wbsairback-scsi-login "+address+" "+iqn_wwn+" "+RemoteStorageManager.STORAGE_TYPE_ISCSI+" CHAP "+user+" "+password+")\n");
		}
		_sb.append("/usr/bin/sudo /bin/mount -o rw /dev/$DEVICE "+mountPath+"\n");
		_sb.append("if [ $? -gt 0 ]; then\n");
		_sb.append("	echo \"ERROR: "+msg+"\"\n");
		_sb.append("	exit 1;\n");
		_sb.append("fi\n");

		return _sb.toString();
	}
	
	
	public static String getIscsiLogoutCommand(String target, String address) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/sudo /usr/bin/iscsiadm -m node --targetname \"");
		_sb.append(target);
		_sb.append("\" --portal \"");
		_sb.append(address.toString());
		_sb.append(":3260\" --logout");
		_sb.append("\n");
		return _sb.toString();
	}
	
	public static String getFibreLogoutCommand() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/usr/bin/sudo /usr/sbin/wbsairback-scsi-rescan");
		return _sb.toString();
	}
	
	/**
	 * Comprueba si un step existe en algún remote storage
	 * @param nameStep
	 * @return
	 * @throws Exception
	 */
	public static String isStepInAnyRemoteStorage(String nameStep) throws Exception {
		try {
			List<String> storageNames = listRemoteStorageNames();
			if (storageNames != null && storageNames.size()>0) {
				for (String nameStorage : storageNames) {
					if (getRemoteStorageStep(nameStorage, nameStep) != null)
						return nameStorage;
				}
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error comprobando si un step llamado {} existe en algun remote storage", nameStep);
			return null;
		}
	}
	
	/**
	 * Desmounta cierta lun externa
	 * @param mountPath
	 * @param volume
	 * @param snapshot
	 * @return
	 * @throws Exception
	 */
	public static String getRemoteStorageBackupUmountCommands(String mountPath, String iqnwwwn, String address, String storage_type) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		// Primero desmontamos
		_sb.append("/usr/bin/sudo /bin/umount -l "+mountPath+"\n");
		
		// Hacemos logout iscsi
		if (storage_type.equals(RemoteStorageManager.STORAGE_TYPE_ISCSI))
			_sb.append(getIscsiLogoutCommand(iqnwwwn, address));
		else
			_sb.append(getFibreLogoutCommand());
		
		return _sb.toString();
	}
	
	public static Map<String, Map<String, String>> removeUnrelatedStepVariables(Map<String, Object> remoteStorage, Map<String, Map<String, String>> variables, String nameStep) throws Exception {
		Map<String, Map<String, String>> newVars = new TreeMap<String, Map<String, String>>();
		if (remoteStorage.get("steps") != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> steps = (Map<String, Object>) remoteStorage.get("steps");
			if (!steps.isEmpty()) {
				for (String name : steps.keySet()) {
					if (name.equals(nameStep)) {
						@SuppressWarnings("unchecked")
						Map<String, Object> step = (Map<String, Object>) steps.get(name);
						if (step.get("scripts") != null) {
							@SuppressWarnings("unchecked")
							Map<Integer, String> scripts = (Map<Integer, String>) step.get("scripts");
							for (String script : scripts.values()) {
								for (String nameVar : variables.keySet()) {
									if (script.contains("[[["+nameVar+"]]]")) {
										if (!newVars.containsKey(nameVar)) {
											newVars.put(nameVar, variables.get(nameVar));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return newVars;
	}
	
	protected static String getSmartContent(String content, Map<String, String> attributes)
			throws Exception {
		if (content == null || content.isEmpty()) {
			return null;
		}
		int _old_offset = 0;
		StringBuilder _sb = new StringBuilder();
		for (int _offset = content.indexOf("[[[", 0); _offset != -1; _offset = content
				.indexOf("[[[", _offset)) {
			_sb.append(content.substring(_old_offset, _offset));
			_offset += 3;
			if (content.indexOf("]]]", _offset) != -1) {
				String _name = content.substring(_offset, content.indexOf(
						"]]]", _offset));
				if (attributes != null && attributes.containsKey(_name)) {
						_sb.append(attributes
								.get(_name));
				} else {
					_sb.append("");
				}
				_old_offset = content.indexOf("]]]", _offset) + 3;
			}
		}
		_sb.append(content.substring(_old_offset, content.length()));
		return _sb.toString();
	}
}
