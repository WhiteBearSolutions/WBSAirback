package com.whitebearsolutions.imagine.wbsairback.advanced;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ApplicationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;
import com.whitebearsolutions.io.FileUtils;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class TemplateJobManager {

	public static final String path = WBSAirbackConfiguration.getDirectoryAdvancedTemplateJob();
	
	private final static Logger logger = LoggerFactory.getLogger(TemplateJobManager.class);
	
	public final static String GROUPS_STATUS_NO_GROUPS = "no_groups";
	public final static String GROUPS_STATUS_ALL_UPDATED = "all_updated";
	public final static String GROUPS_STATUS_NOT_UPDATED = "not_updated";
	public final static String GROUPS_STATUS_NEW_STEPS_NOT_INCLUDED = "steps_not_included";
	
	static {		
		if ( !(new File(path).exists()) ) {
			new File(path).mkdirs();
		}
	}
	
	/**
	 * Comprueba si ya existe un templateJob
	 * @param templateJobName
	 * @return
	 */
	public static boolean existsTemplateJob(String templateJobName) throws Exception {
		try {
			logger.debug("Comprobando si templateJob {} existe ...",templateJobName);
			List<String> list = listTemplateJobNames();
			if (list.contains(templateJobName)) {
				logger.debug("El templateJob {} existe",templateJobName);
				return true;
			}
			logger.debug("El templateJob {} no existe",templateJobName);
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si existe el templateJob: {}.Ex: {}", templateJobName, ex.getMessage());
			throw new Exception("Error checking if exists templateJobName "+templateJobName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Lista los nombres de los template jobs definidos
	 * @return
	 */
	public static List<String> listTemplateJobNames(String file) throws Exception {
		try {
			if (file == null || file.isEmpty())
				file = path;
			logger.debug("Listando nombres de templateJobs ...");
			List<String> templateJobs = new ArrayList<String>();
	    	String[] listDir = new File(file).list();
	    	if (listDir != null) {
	    		for (String el : listDir) {
	    			if (el.contains(".xml"))
	    				templateJobs.add(el.substring(0, el.indexOf(".xml")));
	    		}
	    	}
	    	logger.debug("Encontrados {} nombres de templateJobs", templateJobs.size());
	    	return templateJobs;
		} catch (Exception ex) {
			logger.error("Error listando nombres de templateJobs. Ex: {}", ex.getMessage());
			throw new Exception("Error listing templateJobs. Ex:"+ex.getMessage());
		}
	}
	
	
	public static List<String> listTemplateJobNames() throws Exception {
		return listTemplateJobNames(null);
	}
	
	
	/**
	 * Lista los template jobs definidos en formato de mapas de valores
	 * @return
	 */
	public static List<Map<String, Object>> listTemplateJobs(String file) throws Exception {
		try {
			logger.debug("Listando templateJobs ...");
			List<Map<String, Object>> templateJobs = new ArrayList<Map<String, Object>>();
			List<String> templateJobNames = listTemplateJobNames(file);
			if (templateJobNames != null && templateJobNames.size()>0) {
				for (String name : templateJobNames) {
					Map<String, Object> templateJobValues = getTemplateJob(name, file);
					if (templateJobValues != null && templateJobValues.size()>0)
						templateJobs.add(templateJobValues);
				}
			}
			logger.debug("Encontrados {} templateJobs", templateJobs.size());
			return templateJobs;
		} catch (Exception ex) {
			logger.error("Error listando templateJobs. Ex: {}", ex.getMessage());
			throw new Exception("Error listing templateJobs. Ex:"+ex.getMessage());
		}
	}
	
	public static List<Map<String, Object>> listTemplateJobs() throws Exception {
		return listTemplateJobs(null);
	}
	
	
	/**
	 * Obtiene el path asociado a un templateJob
	 * @param templateJobName
	 * @return
	 * @throws Exception
	 */
	public static String getPathTemplateJob(String templateJobName) throws Exception {
		return getPathTemplateJob(templateJobName, null);
	}
	
	public static String getPathTemplateJob(String templateJobName, String file) throws Exception {
		if (file == null || file.isEmpty())
			file = path;
		return file+"/"+templateJobName+".xml";
	}
	
	
	public static Map<String, Object> getTemplateJob(String templateJobName) throws Exception {
		return getTemplateJob(templateJobName, null);
	}
	/**
	 * Obtiene los valores de un template job dado
	 * @param templateJobName
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getTemplateJob(String templateJobName, String pathFile) throws Exception {
		try {
			Map<String, Object> templateJob = new HashMap<String, Object>();
			File file = new File (getPathTemplateJob(templateJobName, pathFile));
			if (file.exists()) {
				DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document _doc = _db.parse(file);
				Node templateJobNode = _doc.getElementsByTagName("templateJob").item(0);
				NodeList list = templateJobNode.getChildNodes();
				for (int i=0;i < list.getLength();i++) {
					if(list.item(i).getNodeType() == Node.ELEMENT_NODE) {
			    		Element e = (Element) list.item(i);
			    		if (e.getNodeName() != null && (e.getNodeName().equals("name")  || e.getNodeName().equals("groups_status")))
			    			templateJob.put(e.getNodeName(), e.getTextContent());
			    		else if (e.getNodeName() != null && e.getNodeName().equals("steps") ) {
			    			NodeList listSteps = e.getChildNodes();
			    			Map<Integer, Map<String, String>> steps = new TreeMap<Integer, Map<String, String>> ();
			    			if (listSteps.getLength()>0) {
			    				for (int j=0;j < listSteps.getLength();j++) {
			    					if(listSteps.item(j).getNodeType() == Node.ELEMENT_NODE) {
				    					Element v = (Element) listSteps.item(j);
				    					if (v.getNodeName() != null && v.getNodeName().equals("step")) {
				    						Map<String, String> step = new HashMap<String, String>();
				    						Integer order = null;
				    						if (v.getAttribute("order") != null) {
				    							order = Integer.parseInt(v.getAttribute("order"));
				    						}
				    						NodeList listData = v.getChildNodes();
				    						for (int x=0;x < listData.getLength();x++) {
				    							if(listData.item(x).getNodeType() == Node.ELEMENT_NODE) {
				    								Element d = (Element) listData.item(x);
				    								if (d.getNodeName() != null && (d.getNodeName().equals("system") || d.getNodeName().equals("app") || d.getNodeName().equals("advanced_storage") )) {
				    									step.put(d.getNodeName(), d.getTextContent());
				    									step.put("data", d.getTextContent());
				    								} else if (d.getNodeName() != null && d.getNodeName().equals("name")) {
				    									step.put("name", d.getTextContent());
				    								} else if (d.getNodeName() != null && d.getNodeName().equals("type")) {
				    									step.put("type", d.getTextContent());
				    								}
				    							}
				    						}
				    						step.put("order", order.toString());
				    						step.put("type", step.get("type"));
			    							steps.put(order, step);
				    					}
			    					}
			    				}
			    			templateJob.put("steps", steps);
			    			} 
			    		}
					}
				}
			}
			return templateJob;
		} catch (Exception ex) {
			logger.error("Error obteniedo datos de template job: {}. Ex: {}", templateJobName, ex.getMessage());
			throw new Exception("Error obtaining template job data of "+templateJobName+". Ex:"+ex.getMessage());
		}
	}
	
	public static Map<String, String> getTemplateJobStep(String templateJobName, String templateJobStep) throws Exception {
		try {
			Map<String, Object> templateJob = getTemplateJob(templateJobName);
			if (templateJob != null) {
				if (templateJob.get("steps") != null) {
					@SuppressWarnings("unchecked")
					Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>)templateJob.get("steps");
					if (!steps.isEmpty()) {
						for (Integer order : steps.keySet()) {
							Map<String, String> step = steps.get(order);
							if (step.get("name").equals(templateJobStep))
								return step;
						}
					}
				}
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error buscando step {} en templateJob {}. Ex: {}", new Object[]{templateJobStep, templateJobName, ex.getMessage()});
			return null;
		}
		
	}
	
	
	/**
	 * Elimina un template job
	 * @param templateJobName
	 * @throws Exception
	 */
	public static void removeTemplateJob(String templateJobName) throws Exception {
		try {
			logger.info("Eliminando TemplateJob {} ...", templateJobName);
			if (existsTemplateJob(templateJobName)) {
				File f = new File (getPathTemplateJob(templateJobName));
				if (f.exists()) {
					f.delete();
					logger.info("TemplateJob {} eliminado.", templateJobName);
				}
			}
		} catch (Exception ex) {
			logger.error("Error borrando TemplateJob: {}. Ex: {}", templateJobName, ex.getMessage());
			throw new Exception("Error removing TemplateJob: "+templateJobName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Guarda un templateJob con sus datos correspondientes
	 * @param templateJobName
	 * @param edit
	 * @throws Exception
	 */
	public static void saveTemplateJob(String templateJobName, boolean edit) throws Exception {
		
		boolean exists = existsTemplateJob(templateJobName);
		if (!edit && exists) {
			throw new Exception ("Another template job already exists with that name");
		}
		
		String status = GROUPS_STATUS_NO_GROUPS;
		
		Map<Integer, Map<String, String>> steps = null;
		Map<String, Object> templateJob = null;
		if (exists) {
			templateJob = getTemplateJob(templateJobName);
			if (templateJob.get("steps") != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Map<String, String>> mapSteps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
				if (mapSteps != null && mapSteps.size()>0)
					steps = mapSteps;
			}
			if (templateJob.get("groups_status") != null)
				status = (String) templateJob.get("groups_status");
		}
		
		try {
			logger.info("Guardando templateJob {}...", new Object[]{templateJobName});
			writeTemplateJobXml(templateJobName, steps, status);
			logger.info("Guardado template job {}", templateJobName);
		} catch (Exception ex) {
			logger.error("Error guardando template job: {}. Ex: {}", templateJobName, ex.getMessage());
			throw new Exception("Error saving template job: "+templateJobName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Comprueba si existe un step de cierto templateJob
	 * @param templateJob
	 * @param stepName
	 * @return
	 * @throws Exception
	 */
	public static boolean existsTemplateJobStep(String templateJobName, String stepName, String stepData) throws Exception {
		try {
			Map<String,Object> templateJob = getTemplateJob(templateJobName);
			if (templateJob != null && !templateJob.isEmpty()) {
				if (templateJob.get("steps") != null) {
					@SuppressWarnings("unchecked")
					Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
					if (!steps.isEmpty()) {
						for (Integer order: steps.keySet()) {
							Map<String, String> step = steps.get(order);
							if (step.get("name").equals(stepName) && (stepData == null || stepData.isEmpty() || step.get("data") == null || step.get("data").isEmpty() || stepData.equals(step.get("data"))) )
								return true;
						}
					}
				}
			}
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si existe template job step: storage: {} step: {}. Ex: {}", new Object[]{templateJobName, stepName, ex.getMessage()});
			throw new Exception("Error checking if  template job step exists: storage:"+templateJobName+" step:"+stepName+". Ex:"+ex.getMessage());
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static void saveTemplateJobStep(String templateJobName, String typeStep, String stepName, String data, Configuration conf) throws Exception {
		boolean exists = false;
		exists = existsTemplateJobStep(templateJobName, stepName, data);
		
		if (exists) {
			throw new Exception ("Another template job step already exists with that name");
		}
		
		if (!typeStep.equals(StepManager.TYPE_STEP_BACKUP) && (data == null || data.isEmpty()) )
			throw new Exception ("Is required to specify an entity to associate with the selected step");
		
		try {
			
			Map<Integer, Map<String, String>> steps = null;
			Map<String, Object> templateJob = getTemplateJob(templateJobName);
			Integer order = 1;
			if (templateJob.get("steps") != null) {
				steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
				for (Integer o : steps.keySet()) {
					Map<String, String> step = steps.get(o);
					step.put("order", order.toString());
					order++;
				}
			} else {
				steps = new TreeMap<Integer, Map<String, String>>();
			}
			
			Map<String, String> step = new HashMap<String, String>();
			step.put("name", stepName);
			step.put("type", typeStep);
			step.put("order", order.toString());
			if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
				String templateStorageType = getTypeStorageTemplate(templateJobName);
				if (templateStorageType != null) {
					Map<String, Object> remoteStorage = RemoteStorageManager.getRemoteStorage(data);
					if (!((String) remoteStorage.get("typeStorage")).equals(templateStorageType))
						throw new Exception("It is only allowed one storage type of advanced storage steps by template job (iSCIS or FC)");
				}
				step.put("advanced_storage", data);
			} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP))
				step.put("app", data);
			else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM))
				step.put("system", data);
			steps.put(order, step);
			
			String status = GROUPS_STATUS_NO_GROUPS;
			if (templateJob.get("groups_status") != null)
				status = (String) templateJob.get("groups_status");
			List<Map<String, Object>> groups = GroupJobManager.getGroupJobsFromTemplate(templateJobName, conf);
			if (groups != null && groups.size()>0)
				status = GROUPS_STATUS_NEW_STEPS_NOT_INCLUDED;
			
			writeTemplateJobXml(templateJobName, steps, status);
		} catch (Exception ex) {
			logger.error("Error guardando template job step: templateJob: {} step: {}. Ex: {}", new Object[]{templateJobName, stepName, ex.getMessage()});
			throw new Exception("Error saving  template job step: templateJob:"+templateJobName+" step:"+stepName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Elimina un step de templateJob
	 * @param templateJob
	 * @param stepName
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void removeTemplateJobStep(String templateJobName, String stepName, Configuration conf) throws Exception {
		try {
			Map<String, Object> templateJob = getTemplateJob(templateJobName);
			if (templateJob != null && !templateJob.isEmpty()) {
				if (templateJob.get("steps") != null) {
					Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
					Map<Integer, Map<String, String>> newSteps = new TreeMap<Integer, Map<String, String>>();
					if (!steps.isEmpty()) {
						Map<String, String> stepFound = null;
						for (Integer order : steps.keySet()) {
							Map<String, String> step = steps.get(order);
							if (step.get("name").equals(stepName))
								stepFound = step;
						}
						if (stepFound != null) {
							steps.remove(Integer.parseInt(stepFound.get("order")));
							Integer order = 1;
							for (Integer o : steps.keySet()) {
								Map<String, String> step = steps.get(o);
								step.put("order", order.toString());
								newSteps.put(order, step);
								order++;
							}
							
							String status = GROUPS_STATUS_NO_GROUPS;
							if (templateJob.get("groups_status") != null)
								status = (String) templateJob.get("groups_status");
							
							List<Map<String, Object>> groups = GroupJobManager.getGroupJobsFromTemplate(templateJobName, conf);
							if (groups != null && groups.size()>0)
								status = GROUPS_STATUS_NOT_UPDATED;
							
							writeTemplateJobXml(templateJobName, newSteps, status);
							logger.debug("Eliminado step: nombre {}", stepFound.get("name"));
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error eliminando template job step: storage: {}, stepName:{} . Ex: {}", new Object[]{templateJobName, stepName, ex.getMessage()});
			throw new Exception("Error deleting template job step of: storage: "+templateJobName+", stepName:"+stepName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Indica si cierta entidad está presente en algún paso de algún template job. Devuelve null si no está o el nombre de templatejob:step asociado
	 * @param templateJobName
	 * @param nameEntity
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static String isEntityInAnyTemplateJob(String nameEntity, String type) throws Exception {
		List<String> templateJobNames = listTemplateJobNames();
		for (String templateJobName : templateJobNames) {
			String step = isEntityInTemplateJob(templateJobName, nameEntity, type);
			if (step != null)
				return templateJobName+" -- "+step;
		}
		return null;
	}
	
	/**
	 * Indica si cierta entidad está presente en algún paso de algún template job. Devuelve null si no está o el nombre de templatejob:step asociado
	 * @param templateJobName
	 * @param nameEntity
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> getRelatedTemplateJobs(String nameEntity, String type) throws Exception {
		List<Map<String,Object>> templateJobNames = listTemplateJobs();
		List<Map<String,Object>> relatedTemplateJobs = new ArrayList<Map<String,Object>>();
		for (Map<String, Object> templateJob : templateJobNames) {
			String step = isEntityInTemplateJob(templateJob, nameEntity, type);
			if (step != null)
				relatedTemplateJobs.add(templateJob);
		}
		return relatedTemplateJobs;
	}
	
	
	/**
	 * Comprueba si cierta entidad esta presente en algún paso de un template job. Devuelve null si no está o el nombre del step asociado
	 * @param templateJobName
	 * @param nameEntity
	 * @param type
	 * @return String 
	 * @throws Exception
	 */
	public static String isEntityInTemplateJob(String templateJobName, String nameEntity, String type) throws Exception {
		Map<String, Object> templateJob = getTemplateJob(templateJobName);
		if (templateJob != null && !templateJob.isEmpty()) {
			if (templateJob.get("steps") != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
				if (!steps.isEmpty()) {
					for (Integer order : steps.keySet()) {
						Map<String, String> step = steps.get(order);
						if (step.get("type") != null && !step.get("type").isEmpty() && ((String) step.get("type")).equals(type)) {
							if (step.get("data") != null && !step.get("data").isEmpty() && ((String) step.get("data")).equals(nameEntity)) {
								return step.get("name");
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public static String isEntityInTemplateJob(Map<String, Object> templateJob, String nameEntity, String type) throws Exception {
		if (templateJob != null && !templateJob.isEmpty()) {
			if (templateJob.get("steps") != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
				if (!steps.isEmpty()) {
					for (Integer order : steps.keySet()) {
						Map<String, String> step = steps.get(order);
						if (step.get("type") != null && !step.get("type").isEmpty() && ((String) step.get("type")).equals(type)) {
							if (step.get("data") != null && !step.get("data").isEmpty() && ((String) step.get("data")).equals(nameEntity)) {
								return step.get("name");
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * @param nameStep
	 * @return
	 * @throws Exception
	 */
	public static String isStepInAnyTemplateJob(String nameStep) throws Exception {
		try {
			List<String> templateJobNames = listTemplateJobNames();
			if (templateJobNames != null && templateJobNames.size()>0) {
				for (String templateJobName : templateJobNames) {
					Map<String, Object> templateJob = getTemplateJob(templateJobName);
					if (templateJob != null && !templateJob.isEmpty()) {
						if (templateJob.get("steps") != null) {
							@SuppressWarnings("unchecked")
							Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
							if (!steps.isEmpty()) {
								for (Integer order : steps.keySet()) {
									Map<String, String> step = steps.get(order);
									if (step.get("name") != null && step.get("name").equals(nameStep))
										return templateJobName;
								}
							}
						}
					}
				}
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error comprobando si el step {} existe en algún template job", nameStep);
			return null;
		}
	}
	
	
	/**
	 * 
	 * @param nameStep
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getTemplateJobsWithStep(String nameStep) throws Exception {
		try {
			List<Map<String, Object>> templateJobs = listTemplateJobs();
			if (templateJobs != null && templateJobs.size()>0) {
				List<Map<String, Object>> relatedTemplates = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> templateJob : templateJobs) {
					if (templateJob != null && !templateJob.isEmpty()) {
						if (templateJob.get("steps") != null) {
							@SuppressWarnings("unchecked")
							Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
							if (!steps.isEmpty()) {
								for (Integer order : steps.keySet()) {
									Map<String, String> step = steps.get(order);
									if (step.get("name") != null && step.get("name").equals(nameStep))
										relatedTemplates.add(templateJob);
								}
							}
						}
					}
				}
				return relatedTemplates;
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error comprobando si el step {} existe en algún template job", nameStep);
			return null;
		}
	}
	
	/**
	 * Busca los steps que tienen asociada cierta propiedad
	 * @param property
	 * @param value
	 * @param conf
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> getStepsWithProperty(String property, String value) throws Exception {
		try {
			List<String> templateJobNames = listTemplateJobNames();
			List<Map<String, String>> stepsImplied = new ArrayList<Map<String, String>>();
			if (templateJobNames != null && templateJobNames.size()>0) {
				for (String templateJobName : templateJobNames) {
					Map<String, Object> templateJob = getTemplateJob(templateJobName);
					if (templateJob.get("steps") != null) {
						@SuppressWarnings("unchecked")
						Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
						if (steps.size()>0) {
							for (Integer order : steps.keySet()) {
								Map<String, String> step = steps.get(order);
								if (step.get(property) != null && step.get(property).equals(value)) {
									stepsImplied.add(step);
								}
							}
						}
					}
				}
			}
			return stepsImplied;
		} catch (Exception ex) {
			logger.error("Error obteniendo steps con la propiedad {} = {}. Ex: {}", new Object[]{property, value, ex.getMessage()});
			throw new Exception("Could not obtain steps in template jobs with "+property+" = "+value+". Ex:"+ex.getMessage());
		}
	}
	
	public static String getTypeStorageTemplate(String templateName) throws Exception {
		try {
			Map<String, Object> templateJob = getTemplateJob(templateName);
			if (templateJob != null && !templateJob.isEmpty()) {
				if (templateJob.get("steps") != null) {
					@SuppressWarnings("unchecked")
					Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
					if (steps.size()>0) {
						for (Integer order : steps.keySet()) {
							Map<String, String> step = steps.get(order);
							if (step.get("type") != null && step.get("type").equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
								if (step.get("data") != null && !step.get("data").isEmpty()) {
									Map<String, Object> remoteStorage = RemoteStorageManager.getRemoteStorage(step.get("data"));
									if (remoteStorage.get("typeStorage") != null)
										return (String) remoteStorage.get("typeStorage");
								}
							}
						}
					}
				}
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error obtaining type storage of template. Ex:{}", ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Genera el xml asociado al template job 
	 * @param templateJobName
	 * @param steps
	 * @throws Exception
	 */
	public static void writeTemplateJobXml(String templateJobName, Map<Integer, Map<String, String>> steps, String groups_status) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<?xml version=\"1.0\"?>\n");
		_sb.append("<templateJob>\n");
		_sb.append("	<name>"+templateJobName.trim()+"</name>\n");
		if (groups_status != null && !groups_status.isEmpty()) {
			_sb.append("			<groups_status>");
			_sb.append(groups_status);
			_sb.append("</groups_status>\n");
		}
		_sb.append("	<steps>\n");
		if (steps != null && steps.size()>0) {
			for (Integer order : steps.keySet()) {
				Map<String, String> step = (Map<String, String>) steps.get(order);
				_sb.append("		<step order=\""+order+"\">\n");
				_sb.append("			<name>");
				_sb.append(step.get("name"));
				_sb.append("</name>\n");
				_sb.append("			<type>");
				_sb.append(step.get("type"));
				_sb.append("</type>\n");
				if (step.get("system") != null) {
					_sb.append("			<system>");
					_sb.append(step.get("system"));
					_sb.append("</system>\n");
				} else if(step.get("app") != null) {
					_sb.append("			<app>");
					_sb.append(step.get("app"));
					_sb.append("</app>\n");
				} else if(step.get("advanced_storage") != null) {
					_sb.append("			<advanced_storage>");
					_sb.append(step.get("advanced_storage"));
					_sb.append("</advanced_storage>\n");
				} else if(step.get("backup") != null) {
					_sb.append("			<backup></backup>\n");
				}
				_sb.append("	</step>\n");
			}
		}
		_sb.append("	</steps>\n");
		_sb.append("</templateJob>");
		
		FileOutputStream _fos = new FileOutputStream(new File(getPathTemplateJob(templateJobName)));
		_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
		_fos.close();
	}
	
	
	public static String generateExport(List<String> templateJobNames) throws Exception {
		try {
			if (templateJobNames != null && !templateJobNames.isEmpty()) {
				DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HHmmss");
				File fileEntity = null;
				
				String nameExport = "templateExp["+df.format(new Date())+"]";
				String pathExport = "/tmp/"+nameExport;
				String fileExport = pathExport+"/"+nameExport+".tar";
				
				logger.info("Exporting templates on {} ...", pathExport);
				
				String pathTemplates = pathExport+"/template_job";
				String pathSteps = pathExport+"/step";
				String pathRemoteStorage = pathExport+"/remote_storage";
				String pathScripts = pathExport+"/scripts";
				String pathAppsSo = pathExport+"/appsAndSo.xml";
				
				new File(pathExport).mkdirs();
				new File(pathTemplates).mkdirs();
				new File(pathSteps).mkdirs();
				new File(pathRemoteStorage).mkdirs();
				new File(pathScripts).mkdirs();
				
				fileEntity = new File(SysAppsInventoryManager.getPath());
				if (fileEntity.exists())
					FileUtils.copyFile(fileEntity, new File(pathAppsSo));
				for (String templateJobName : templateJobNames) {
					Map<String, Object> templateJob = getTemplateJob(templateJobName);
					if (templateJob != null && !templateJob.isEmpty()) {
						if (templateJob.get("steps") != null) {
							@SuppressWarnings("unchecked")
							Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) templateJob.get("steps");
							if (!steps.isEmpty()) {
								for (Integer order : steps.keySet()) {
									Map<String, String> step = steps.get(order);
			    					String nameStep = step.get("name");
			    					String typeStep = step.get("type");
			    					String dataStep = step.get("data");
									if (typeStep.equals(StepManager.TYPE_STEP_ADVANCED_STORAGE)) {
										fileEntity = new File(RemoteStorageManager.getPathRemoteStorage(dataStep));
										FileUtils.copyFile(fileEntity, new File(pathRemoteStorage+"/"+dataStep+".xml"));
									} else if (typeStep.equals(StepManager.TYPE_STEP_SCRIPT_APP) || typeStep.equals(StepManager.TYPE_STEP_SCRIPT_SYSTEM)) {
										fileEntity = new File(ScriptProcessManager.getPathScript(dataStep));
										FileUtils.copyFile(fileEntity, new File(pathScripts+"/"+dataStep+".xml"));
									}
									fileEntity = new File(StepManager.getPathStep(nameStep));
									FileUtils.copyFile(fileEntity, new File(pathSteps+"/"+nameStep+".xml"));
								}
							}
						}
						fileEntity = new File(TemplateJobManager.getPathTemplateJob((String) templateJob.get("name")));
						FileUtils.copyFile(fileEntity, new File(pathTemplates+"/"+(String) templateJob.get("name")+".xml"));
					}
				}
				Command.systemCommand("cd /tmp/ && tar cvf "+fileExport+" "+nameExport); 
				logger.info("Templates exported on {} ...", fileExport);
				return fileExport;
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error exporting templates. Ex: {}"+ex.getMessage());
			throw ex;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void importData(byte[] data, Configuration conf) throws Exception {		
		
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HHmmss");
		
		String nameImport = "templateExp["+df.format(new Date())+"]";
		String fileImport = "/tmp/"+nameImport+".tar";
		
		try {
			OutputStream out = new FileOutputStream(fileImport);
			out.write(data);
			out.close();
			
			Command.systemCommand("cd /tmp/ && tar xvf "+fileImport);
			
			File tmp = new File("/tmp/");
			for (String f: tmp.list()) {
				if (f.contains("templateExp") && !f.contains(".tar")) {
					String pathTemplates = "/tmp/"+f+"/template_job";
					String pathSteps = "/tmp/"+f+"/step";
					String pathRemoteStorage = "/tmp/"+f+"/remote_storage";
					String pathScripts = "/tmp/"+f+"/scripts";
					String pathInventory = "/tmp/"+f+"/appsAndSo.xml";
					
					List<String> sysTemplates = listTemplateJobNames();
					List<Map<String, Object>> importTemplates = listTemplateJobs(pathTemplates);
					if (importTemplates != null && !importTemplates.isEmpty()) {
						for (Map<String, Object> template : importTemplates) {
							if (sysTemplates.contains((String) template.get("name")))
								throw new Exception("There already exists a template whith name ["+template.get("name")+"]");
						}
					}
					
					List<String> sysSteps = StepManager.listStepNames();
					List<Map<String, String>> importSteps = StepManager.listStepsOn(pathSteps);
					if (importSteps != null && !importSteps.isEmpty()) {
						for (Map<String, String> step : importSteps) {
							if (sysSteps.contains(step.get("name")))
								throw new Exception("There already exists a step whith name ["+step.get("name")+"]");
						}
					}
					
					List<String> sysStorages = RemoteStorageManager.listRemoteStorageNames();
					List<Map<String, Object>> importStorages = RemoteStorageManager.listRemoteStorages(pathRemoteStorage);
					if (importStorages != null && !importStorages.isEmpty()) {
						for (Map<String, Object> storage : importStorages) {
							if (sysStorages.contains((String) storage.get("name")))
								throw new Exception("There already exists an advanced storage whith name ["+storage.get("name")+"]");
						}
					}
					
					ScriptProcessManager sysSpm = new ScriptProcessManager();
					ScriptProcessManager spm = new ScriptProcessManager(pathScripts);
					Map<String, Map<String, Object>> importScripts = spm.listScript();
					Map<String, Map<String, Object>> sysScripts = sysSpm.listScript();
					if (importScripts != null && !importScripts.isEmpty()) {
						for (String script : importScripts.keySet()) {
							if (sysScripts.containsKey(script))
								throw new Exception("There already exists an script whith name ["+script+"]");
						}
					}
					
					SysAppsInventoryManager sysSaim = new SysAppsInventoryManager();
					if (new File(pathInventory).exists()) {
						SysAppsInventoryManager saim = new SysAppsInventoryManager(pathInventory);
						
						List<SystemRs> systems = saim.listSystem();
						if (systems != null && !systems.isEmpty())
						for (SystemRs system : systems) {
							if (sysSaim.getSystem(system.getName()) == null) {
								sysSaim.saveSystem(system.getName(), system.getDescription(), false);
							}
						}
						
						List<ApplicationRs> apps = saim.listApps();
						if (apps != null && !apps.isEmpty())
						for (ApplicationRs app : apps) {
							if (sysSaim.getApplication(app.getName()) == null) {
								List<String> listSystems = new ArrayList<String>();
								if (app.getSystems() != null && !app.getSystems().isEmpty()) {
									for (SystemRs sys : app.getSystems()) {
										listSystems.add(sys.getName());
									}
								}
								sysSaim.saveApps(app.getName(), app.getDescription(), listSystems, false);
							}
						}
					}
					
					if (importSteps != null && !importSteps.isEmpty()) {
						for (Map<String, String> step : importSteps) {
							StepManager.saveStep(step.get("name"), step.get("type"), false);
							logger.info("Step [] imported", step.get("name"));
						}
					}
					
					if (importScripts != null && !importScripts.isEmpty()) {
						for (String script : importScripts.keySet()) {
							sysSpm.saveScript(script, importScripts.get(script), sysSaim);
							logger.info("Script [] imported", script);
						}
					}
					
					if (importStorages != null && !importStorages.isEmpty()) {
						for (Map<String, Object> storage : importStorages) {
							Map<String, Map<String, String>> vars = null;
							if (storage.get("variables") != null)
								vars = (Map<String, Map<String, String>>) storage.get("variables");
							RemoteStorageManager.saveStorage((String) storage.get("name"), (String) storage.get("typeConnection"), (String) storage.get("typeStorage"), vars, false);
							if (storage.get("steps") != null) {
								Map<String, Object> steps = (Map<String, Object>) storage.get("steps");
								for (String stepName : steps.keySet()) {
									Map<String, Object> step = (Map<String, Object>) steps.get(stepName);
									Map<Integer, String> scripts = null;
									if (step.get("scripts") != null)
										scripts = (Map<Integer, String>) step.get("scripts");
									boolean mount = false;
									if (step.get("mount") != null && ((String)step.get("mount")).equals("true"))
										mount = true;
									RemoteStorageManager.saveRemoteStorageStep((String) storage.get("name"), stepName, scripts, mount, false);
									logger.info("Advanced storage [] imported", (String) storage.get("name"));
								}
							}
							
						}
					}
					
					if (importTemplates != null && !importTemplates.isEmpty()) {
						for (Map<String, Object> template : importTemplates) {
							TemplateJobManager.saveTemplateJob((String) template.get("name"), false);
							
							if (template.get("steps") != null) {
								Map<Integer, Map<String, String>> steps = (Map<Integer, Map<String, String>>) template.get("steps");
								for (Integer order : steps.keySet()) {
									Map<String, String> step = steps.get(order);
									TemplateJobManager.saveTemplateJobStep((String) template.get("name"), step.get("type"), step.get("name"), step.get("data"), conf);
								}
							}
							logger.info("TemplateJob [] imported", (String) template.get("name"));
						}
					}
					return;
				}
			}
			
		} catch (Exception ex) {
			logger.error("Se produjo un error al exportar plantillas. Ex: {}"+ex.getMessage());
			throw ex;
		} finally {
			try {
				Command.systemCommand("rm -r /tmp/templateExp*");
			} catch (Exception ex){}
			try {
				Command.systemCommand("rm -r /tmp/*.tar");
			} catch (Exception ex) {}
			
			File f = new File(fileImport);
			if ( f.exists())
				f.delete();
			
			File dir = new File("/tmp/"+nameImport);
			if (dir.exists())
				dir.delete();
		}
	}
}
