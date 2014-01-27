package com.whitebearsolutions.imagine.wbsairback.advanced;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;

public class StepManager {
	
	public static final String TYPE_STEP_SCRIPT_SYSTEM = "script_system";
	public static final String TYPE_STEP_SCRIPT_APP = "script_app";
	public static final String TYPE_STEP_ADVANCED_STORAGE = "advanced_storage";
	public static final String TYPE_STEP_BACKUP = "backup";
	
	public static final String GENERIC_STEP_NAME = "GenericStep";
	
	public static String path = WBSAirbackConfiguration.getDirectoryAdvancedStep();
	
	private final static Logger logger = LoggerFactory.getLogger(StepManager.class);
	
	public static List<String> supportedTypeStep = null;
	static {
		supportedTypeStep = new ArrayList<String>();
		supportedTypeStep.add(TYPE_STEP_SCRIPT_SYSTEM);
		supportedTypeStep.add(TYPE_STEP_SCRIPT_APP);
		supportedTypeStep.add(TYPE_STEP_ADVANCED_STORAGE);
		supportedTypeStep.add(TYPE_STEP_BACKUP);
		
		if ( !(new File(path).exists()) ) {
			new File(path).mkdirs();
		}
		
		try {
			if (!existsStep(GENERIC_STEP_NAME)) {
				saveStep(GENERIC_STEP_NAME, TYPE_STEP_SCRIPT_SYSTEM, false);
			}
		} catch (Exception ex) {
			logger.error("Atención: Error creando paso genérico. Ex: {}", ex.getMessage());
		}
	}
	
	/**
	 * Comprueba si ya existe un step 
	 * @param stepName
	 * @return
	 */
	public static boolean existsStep(String stepName) throws Exception {
		try {
			logger.debug("Comprobando si step {} existe ...",stepName);
			List<String> list = listStepNames();
			if (list.contains(stepName)) {
				logger.debug("El step {} existe",stepName);
				return true;
			}
			logger.debug("El step {} no existe",stepName);
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si existe el step: {}.Ex: {}", stepName, ex.getMessage());
			throw new Exception("Error checking if exists stepName "+stepName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Lista los nombres de los pasos definidos
	 * @return
	 */
	public static List<String> listStepNames(String pathFile) throws Exception {
		try {
			if (pathFile == null || pathFile.isEmpty())
				pathFile = path;
			logger.debug("Listando nombres de steps ...");
			List<String> steps = new ArrayList<String>();
	    	String[] listDir = new File(pathFile).list();
	    	if (listDir != null) {
	    		for (String el : listDir) {
	    			if (el.contains(".xml"))
	    				steps.add(el.substring(0, el.indexOf(".xml")));
	    		}
	    	}
	    	logger.debug("Encontrados {} nombres de steps", steps.size());
	    	return steps;
		} catch (Exception ex) {
			logger.error("Error listando nombres de steps. Ex: {}", ex.getMessage());
			throw new Exception("Error listing step names. Ex:"+ex.getMessage());
		}
	}
	
	public static List<String> listStepNames() throws Exception {
		return listStepNames(path);
	}
	
	
	/**
	 * Lista los pasos definidos en formato de mapas de valores
	 * @return
	 */
	public static List<Map<String, String>> listSteps() throws Exception {
		return listStepsOn(null);
	}
	
	
	/**
	 * Lista los pasos definidos en formato de mapas de valores
	 * @return
	 */
	public static List<Map<String, String>> listSteps(String type) throws Exception {
		try {
			logger.debug("Listando steps de tipo {}...", type);
			List<Map<String, String>> steps = new ArrayList<Map<String, String>>();
			List<String> stepNames = listStepNames();
			if (stepNames != null && stepNames.size()>0) {
				for (String name : stepNames) {
					Map<String, String> stepValues = getStep(name);
					if (stepValues != null && stepValues.size()>0) {
						if (stepValues.get("type").equals(type))
							steps.add(stepValues);	
					}
				}
			}
			logger.debug("Encontrados {} steps de tipo {}", steps.size(), type);
			return steps;
		} catch (Exception ex) {
			logger.error("Error listando steps. Ex: {}", ex.getMessage());
			throw new Exception("Error listing steps. Ex:"+ex.getMessage());
		}
	}
	
	public static List<Map<String, String>> listStepsOn(String file) throws Exception {
		try {
			logger.debug("Listando steps ...");
			List<Map<String, String>> steps = new ArrayList<Map<String, String>>();
			List<String> stepNames = listStepNames(file);
			if (stepNames != null && stepNames.size()>0) {
				for (String name : stepNames) {
					Map<String, String> stepValues = getStep(name, file);
					if (stepValues != null && stepValues.size()>0)
						steps.add(stepValues);
				}
			}
			logger.debug("Encontrados {} steps", steps.size());
			return steps;
		} catch (Exception ex) {
			logger.error("Error listando steps. Ex: {}", ex.getMessage());
			throw new Exception("Error listing steps. Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Obtiene el path asociado a un step 
	 * @param nameStep
	 * @return
	 * @throws Exception
	 */
	public static String getPathStep(String nameStep) throws Exception {
		return getPathStep(nameStep, null);
	}
	
	public static String getPathStep(String nameStep, String file) throws Exception {
		if (file == null || file.isEmpty())
			file = path;
		return file+"/"+nameStep+".xml";
	}
	
	
	/**
	 * Obtiene los valores de un step dado
	 * @param stepName
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> getStep(String stepName, String pathFile) throws Exception {
		try {
			Map<String, String> step = new HashMap<String, String>();
			File file = new File (getPathStep(stepName, pathFile));
			if (file.exists()) {
				DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document _doc = _db.parse(file);
				Node storageNode = _doc.getElementsByTagName("step").item(0);
				NodeList list = storageNode.getChildNodes();
				for (int i=0;i < list.getLength();i++) {
					if(list.item(i).getNodeType() == Node.ELEMENT_NODE) {
			    		Element e = (Element) list.item(i);
			    		if (e.getNodeName() != null && (e.getNodeName().equals("name") || e.getNodeName().equals("type")))
						step.put(e.getNodeName(), e.getTextContent());
					}
				}
			}
			return step;
		} catch (Exception ex) {
			logger.error("Error obteniedo datos de step: {}. Ex: {}", stepName, ex.getMessage());
			throw new Exception("Error obtaining step data of "+stepName+". Ex:"+ex.getMessage());
		}
	}
	
	public static Map<String, String> getStep(String stepName) throws Exception {
		return getStep(stepName, null);
	}
	
	/**
	 * Elimina un step
	 * @param nameStep
	 * @throws Exception
	 */
	public static void removeStep(String nameStep) throws Exception {
		try {
			logger.info("Eliminando step {} ...", nameStep);
			if (existsStep(nameStep)) {
				String isInTemplate = TemplateJobManager.isStepInAnyTemplateJob(nameStep);
				if (isInTemplate != null) {
					throw new Exception("Step used in template job "+isInTemplate);
				}
				
				String isInStorage = RemoteStorageManager.isStepInAnyRemoteStorage(nameStep);
				if (isInStorage != null) {
					throw new Exception("Step used in remote storage "+isInStorage);
				}
				
				File f = new File (getPathStep(nameStep));
				if (f.exists()) {
					f.delete();
					logger.info("Step {} eliminado.", nameStep);
				}
			}
		} catch (Exception ex) {
			logger.error("Error borrando step: {}. Ex: {}", nameStep, ex.getMessage());
			throw new Exception("Error removing step: "+nameStep+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Guarda un step con sus datos correspondientes
	 * @param nameStep
	 * @param typeStep
	 * @throws Exception
	 */
	public static void saveStep(String nameStep, String typeStep, boolean edit) throws Exception {
		
		if (!supportedTypeStep.contains(typeStep)) {
			throw new Exception ("Unsupported type step");
		}
		
		if (!edit && existsStep(nameStep)) {
			throw new Exception ("Another step already exists with that name");
		}
		
		try {
			logger.info("Guardando step {} de tipo {}...", nameStep, typeStep);
			
			String isInTemplate = TemplateJobManager.isStepInAnyTemplateJob(nameStep);
			if (isInTemplate != null) {
				throw new Exception("Step used in template job "+isInTemplate);
			}
			
			String isInStorage = RemoteStorageManager.isStepInAnyRemoteStorage(nameStep);
			if (isInStorage != null) {
				throw new Exception("Step used in remote storage "+isInStorage);
			}
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("<?xml version=\"1.0\"?>\n");
			_sb.append("<step>\n");
			_sb.append("	<name>");
			_sb.append(nameStep);
			_sb.append("</name>\n");
			_sb.append("	<type>");
			_sb.append(typeStep);
			_sb.append("</type>\n");
			_sb.append("</step>");
			
			FileOutputStream _fos = new FileOutputStream(new File(getPathStep(nameStep)));
			_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
			_fos.close();
			
			logger.info("Guardado step {} de tipo {}.", nameStep, typeStep);
		} catch (Exception ex) {
			logger.error("Error guardando step: {}. Ex: {}", nameStep, ex.getMessage());
			throw new Exception("Error saving step: "+nameStep+". Ex:"+ex.getMessage());
		}
	}

}
