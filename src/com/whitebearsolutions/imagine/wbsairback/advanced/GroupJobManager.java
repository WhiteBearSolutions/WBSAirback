package com.whitebearsolutions.imagine.wbsairback.advanced;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupJobConfigRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.GroupStepRs;
import com.whitebearsolutions.io.FileUtils;
import com.whitebearsolutions.util.Configuration;

public class GroupJobManager {
	public static final String path = WBSAirbackConfiguration.getDirectoryAdvancedGroupJob();
	
	private final static Logger logger = LoggerFactory.getLogger(GroupJobManager.class);
	public final static String TYPE_TEMPLATEJOB = "template_job";
	public final static String TYPE_MANUAL_SELECTION = "manual_selection";
	
	static {		
		if ( !(new File(path).exists()) ) {
			new File(path).mkdirs();
		}
	}
	
	/**
	 * Comprueba si ya existe un groupJob
	 * @param groupJobName
	 * @return
	 */
	public static boolean existsGroupJob(String groupJobName) throws Exception {
		try {
			logger.debug("Comprobando si groupJob {} existe ...",groupJobName);
			List<String> list = listGroupJobNames();
			if (list.contains(groupJobName)) {
				logger.debug("El groupJob {} existe",groupJobName);
				return true;
			}
			logger.debug("El groupJob {} no existe",groupJobName);
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si existe el groupJob: {}.Ex: {}", groupJobName, ex.getMessage());
			throw new Exception("Error checking if exists groupJobName "+groupJobName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Lista los nombres de los group jobs definidos
	 * @return
	 */
	public static List<String> listGroupJobNames() throws Exception {
		try {
			logger.debug("Listando nombres de groupJobs ...");
			List<String> groupJobs = new ArrayList<String>();
	    	String[] listDir = new File(path).list();
	    	if (listDir != null) {
	    		for (String el : listDir) {
	    			if (el.contains(".xml") && !el.contains(".config"))
	    				groupJobs.add(el.substring(0, el.indexOf(".xml")));
	    		}
	    	}
	    	logger.debug("Encontrados {} nombres de groupJobs", groupJobs.size());
	    	return groupJobs;
		} catch (Exception ex) {
			logger.error("Error listando nombres de groupJobs. Ex: {}", ex.getMessage());
			throw new Exception("Error listing groupJobs. Ex:"+ex.getMessage());
		}
	}
	
	/**
	 * Lista los group jobs definidos en formato de mapas de valores
	 * @return
	 */
	public static List<Map<String, Object>> listGroupJobs(Configuration c) throws Exception {
		try {
			logger.debug("Listando groupJobs ...");
			List<Map<String, Object>> groupJobs = new ArrayList<Map<String, Object>>();
			List<String> groupJobNames = listGroupJobNames();
			JobManager jm = new JobManager(c);
			Map<String, Map<String, String>> allArchivedJobs = jm.getArchivedClientJobs(-1, null, 0, 0);
			if (groupJobNames != null && groupJobNames.size()>0) {
				for (String name : groupJobNames) {
					Map<String, Object> groupJobValues = getGroupJob(name, c, allArchivedJobs);
					if (groupJobValues != null && groupJobValues.size()>0)
						groupJobs.add(groupJobValues);
				}
			}
			logger.debug("Encontrados {} groupJobs", groupJobs.size());
			return groupJobs;
		} catch (Exception ex) {
			logger.error("Error listando groupJobs. Ex: {}", ex.getMessage());
			throw new Exception("Error listing groupJobs. Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Obtiene el path asociado a un groupJob
	 * @param groupJobName
	 * @return
	 * @throws Exception
	 */
	public static String getPathGroupJob(String groupJobName) throws Exception {
		return path+"/"+groupJobName+".xml";
	}
	
	public static String getPathGroupJobConfig(String groupJobName) throws Exception {
		return path+"/"+groupJobName+".config.xml";
	}
	
	
	/**
	 * Obtiene los valores de un group job dado
	 * @param groupJobName
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> getGroupJob(String groupJobName, Configuration conf, Map<String, Map<String, String>> allArchivedJobs) throws Exception {
		try {
			JobManager jm = null;
			ClientManager cm = null;
			if (conf != null) {
				cm = new ClientManager(conf);
				jm = new JobManager(conf);
			}
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			Map<String, Object> groupJob = new HashMap<String, Object>();
			File file = new File (getPathGroupJob(groupJobName));
			if (file.exists()) {
				DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document _doc = _db.parse(file);
				Node groupJobNode = _doc.getElementsByTagName("groupJob").item(0);
				NodeList list = groupJobNode.getChildNodes();
				if (allArchivedJobs == null) {
					allArchivedJobs = jm.getArchivedClientJobs(-1, null, 0, 0);
				}
				for (int i=0;i < list.getLength();i++) {
					if(list.item(i).getNodeType() == Node.ELEMENT_NODE) {
			    		Element e = (Element) list.item(i);
			    		if (e.getNodeName() != null && (e.getNodeName().equals("name") || e.getNodeName().equals("type") || e.getNodeName().equals("schedule") || e.getNodeName().equals("storage") || e.getNodeName().equals("templateJob")))
			    			groupJob.put(e.getNodeName(), e.getTextContent());
			    		else if (e.getNodeName() != null && e.getNodeName().equals("jobs") ) {
			    			NodeList listJobs = e.getChildNodes();
			    			Map<Integer, Map<String, Object>> jobs = new TreeMap<Integer, Map<String, Object>> ();
			    			if (listJobs.getLength()>0) {
			    				Date groupStart = null;
			    				for (int j=0;j < listJobs.getLength();j++) {
			    					if(listJobs.item(j).getNodeType() == Node.ELEMENT_NODE) {
				    					Element v = (Element) listJobs.item(j);
				    					if (v.getNodeName() != null && v.getNodeName().equals("job")) {
				    						Map<String, Object> job = new HashMap<String, Object>();
				    						if (v.hasAttribute("order"))
				    							job.put("order", Integer.parseInt(v.getAttribute("order")));
				    						NodeList listData = v.getChildNodes();
				    						String name = null;
				    						for (int x=0;x < listData.getLength();x++) {
				    							if(listData.item(x).getNodeType() == Node.ELEMENT_NODE) {
				    								Element d = (Element) listData.item(x);
				    								if (d.getNodeName() != null && (d.getNodeName().equals("name") || d.getNodeName().equals("typeStep") || d.getNodeName().equals("step") || d.getNodeName().equals("inventory"))) {
				    									job.put(d.getNodeName(), d.getTextContent());
				    									if (d.getNodeName().equals("name"))
				    										name = d.getTextContent();
				    								} 
				    							}
				    						}
				    						if (conf != null) {
				    							Map<String, String> jobValues = jm.getProgrammedJob(name);
					    						if (jobValues != null && jobValues.size()>0)
					    							job.putAll(jobValues);
					    						
					    						if (allArchivedJobs.containsKey(name) && allArchivedJobs.get(name) != null) {
					    							Map<String, String> archivedJob = allArchivedJobs.get(name);
					    							if (archivedJob.get("start") != null) {
					    								Date start = dateFormat.parse(archivedJob.get("start"));
					    								if (j == 1) {
					    									job.putAll(archivedJob);
					    									groupStart = start;
					    								} else if (groupStart != null) {
					    									if (start.getTime() >= groupStart.getTime())
					    										job.putAll(archivedJob);
					    								}
					    							}
					    						}
					    						if ((!job.containsKey("clientid") || ((String)job.get("clientid")).isEmpty() || ((String)job.get("clientid")).equals("null")) && job.containsKey("client")) {
					    							job.put("clientid", String.valueOf(cm.getClientId((String) job.get("client"))));
					    						}
					    							
				    						}
				    						job.put("templateJob", groupJob.get("templateJob"));
				    						job.put("groupJob", groupJob.get("name"));
			    							jobs.put((Integer) job.get("order"), job);
				    					}
			    					}
			    				}
			    			
				    			String endtime = "";
				    			String starttime = "";
				    			String status = "";
				    			String alert = "";
				    			if (jobs != null && jobs.size()>0) {
				    				int cj = 0;
				    				for (Integer order : jobs.keySet()) {
				    					Map<String, Object> job = jobs.get(order);
				    					if ( (job.get("alert") != null && alert.equals("")) || (job.get("alert") != null && isWorstAlert((String) job.get("alert"), alert))) {
				    						status = (String) job.get("status");
				    						alert = (String)  job.get("alert");
				    					}
				    					
				    					if ((job.get("start") != null && starttime.equals("")) || (job.get("start") != null &&  isEarlierTime( (String) job.get("start"), starttime))) {
				    						starttime = (String)  job.get("start");
				    					}
				    					
				    					if ((job.get("end") != null && endtime.equals("")) || (job.get("end") != null &&  !isEarlierTime( (String) job.get("end"), endtime))) {
				    						endtime = (String)  job.get("end");
				    					}
				    					
				    					if (job.get("run") != null && job.get("run").equals("true"))
				    						groupJob.put("run", "true");
				    					
				    					if (job.get("return") != null && !groupJob.containsKey("return"))
				    						groupJob.put("return", job.get("return"));
				    					else if (job.get("return") != null && !job.get("return").equals("OK"))
				    						groupJob.put("return", job.get("return"));
				    					
				    					cj++;
				    					if (cj == jobs.size()) {
				    						if ((groupJob.get("run") != null && groupJob.get("run").equals("true")) || (status != null && !status.isEmpty() && job.get("status") == null && !alert.equals("error")))
				    							status = "Running";
				    					}
				    				}
				    			}
				    			
				    			if (!groupJob.containsKey("schedule"))
				    				groupJob.put("schedule", "");
				    			
				    			if (!groupJob.containsKey("run"))
				    				groupJob.put("run", "");
				    			
				    			groupJob.put("start", starttime);
				    			groupJob.put("end", endtime);
				    			groupJob.put("status", status);
				    			groupJob.put("alert", alert);
				    			groupJob.put("jobs", jobs);
			    			} 
			    		}
					}
				}
			}
			return groupJob;
		} catch (Exception ex) {
			logger.error("Error obteniedo datos de group job: {}. Ex: {}", groupJobName, ex.getMessage());
			throw new Exception("Error obtaining group job data of "+groupJobName+". Ex:"+ex.getMessage());
		}
	}
	
	public static GroupJobConfigRs getGroupJobConfiguration(String nameGroupJob) throws Exception {
		File file = new File (getPathGroupJobConfig(nameGroupJob));
		if (file.exists()) {
			String xml = "";
			try {
				 xml = FileUtils.fileToString(getPathGroupJobConfig(nameGroupJob));
				 return GroupJobConfigRs.fromXML(xml);
			} catch (Exception ex) {
				logger.error("Error reading group configuration file: {}. Ex:{}",getPathGroupJobConfig(nameGroupJob), ex.getMessage());
				throw ex;
			}
		}
		return null;
	}
	
	public static void saveGroupJobConfig(String name, String schedule, String storage, String templateJob, List<GroupStepRs> steps) throws Exception {
		try {
			GroupJobConfigRs config = new GroupJobConfigRs();
			config.setName(name);
			if (schedule != null && !schedule.isEmpty())
				config.setSchedule(schedule);
			if (storage != null && !storage.isEmpty())
				config.setStorage(storage);
			if (templateJob != null && !templateJob.isEmpty())
				config.setTemplateJob(templateJob);
			if (steps != null && !steps.isEmpty())
				config.setSteps(steps);
			
			FileOutputStream _fos = new FileOutputStream(new File(getPathGroupJobConfig(name)));
			_fos.write(config.getXML().getBytes(Charset.forName("UTF-8")));
			_fos.close();
		} catch (Exception ex) {
			logger.error("Error saving groupJob configuration for: {}. Ex: {}", name, ex.getMessage());
			throw new Exception("Error saving groupJob configuration for: "+name+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Elimina un group job
	 * @param groupJobName
	 * @throws Exception
	 */
	public static void removeGroupJob(String groupJobName, boolean erase, JobManager jm, Configuration conf) throws Exception {
		try {
			if (!BackupOperator.isBlock_reload())
				BackupOperator.setBlock_reload(true);
			
			logger.info("Eliminando GroupJob {} ...", groupJobName);
			String msgErrorDeleteJobs = null;
			if (existsGroupJob(groupJobName)) {
				File f = new File (getPathGroupJob(groupJobName));
				if (f.exists()) {
					Map<String, Object> groupJob = getGroupJob(groupJobName, conf, null);
					if (erase) {
						if (groupJob.get("jobs") != null) {
							@SuppressWarnings("unchecked")
							Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
							if (jobs.size()>0) {
								for (Integer order : jobs.keySet()) {
									Map<String, Object> job = jobs.get(order);
									try {
										logger.info("Eliminando job {} ...", job.get("name"));
										removeHiddenFileSet((String) job.get("name"), jm);
										jm.removeJob((String) job.get("name"));
									} catch (Exception ex) {
										msgErrorDeleteJobs+="<br />"+ex.getMessage();
									}
								}
							}
						}
					}
						
					f.delete();
					logger.info("GroupJob {} eliminado.", groupJobName);
				}
				f = new File (getPathGroupJobConfig(groupJobName));
				if (f.exists()) {
					f.delete();
				}
			}
			
			if (msgErrorDeleteJobs != null) {
				throw new Exception("Some jobs could not be removed. Please, remove them yourself: "+msgErrorDeleteJobs);
			}
		} catch (Exception ex) {
			logger.error("Error borrando GroupJob: {}. Ex: {}", groupJobName, ex.getMessage());
			throw new Exception("Error removing GroupJob: "+groupJobName+".Ex:"+ex.getMessage());
		} finally {
			if (BackupOperator.isBlock_reload())
				BackupOperator.setBlock_reload(false);
			BackupOperator.reload();
		}
	}
	
	
	/**
	 * Guarda un groupjob con sus datos correspondientes
	 * @param nameGroupjob
	 * @param typeGroupjob
	 * @throws Exception
	 */
	public static void saveGroupJob(String groupJobName, String typeGroupJob, String templateJobName, Map<Integer, Map<String, Object>> jobs, String schedule, String storage) throws Exception {
		try {
			logger.info("Guardando groupJob {}...", new Object[]{groupJobName});
			writeGroupJobXml(groupJobName, typeGroupJob, templateJobName, jobs, schedule, storage);
			logger.info("Guardado group job {}", groupJobName);
		} catch (Exception ex) {
			logger.error("Error guardando group job: {}. Ex: {}", groupJobName, ex.getMessage());
			throw new Exception("Error saving group job: "+groupJobName+". Ex:"+ex.getMessage());
		}
	}
	
	
	/**
	 * Filtra grupos de jobs por todos los parámetros
	 * @param page
	 * @param rp
	 * @param sortname
	 * @param sortorder
	 * @param query
	 * @param qtype
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> searchGroupJobs(Integer page, Integer rp, final String sortname, final String sortorder, String query, String qtype, Configuration c) throws Exception {
		List<Map<String, Object>> allGroupJobs = listGroupJobs(c);
		try {
			List<Map<String, Object>> groupJobs = new ArrayList<Map<String, Object>>();
			if (allGroupJobs != null && allGroupJobs.size()>0) {
				groupJobs.addAll(allGroupJobs);
				
				// Buscamos
				if (query != null && !query.isEmpty() && qtype != null && !qtype.isEmpty())
					groupJobs = searchGroupJobs(allGroupJobs, query, qtype);
				
				// Ordenamos
				if (sortname != null && !sortname.isEmpty() && sortorder != null && !sortorder.isEmpty()) {
					Collections.sort(groupJobs,new Comparator<Map<String, Object>>() {
			            public int compare(Map<String, Object> o1, Map<String, Object> o2){
			               DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			               if ((o1.containsKey(sortname) && o1.get(sortname) != null && !((String)o1.get(sortname)).isEmpty() && !o1.get(sortname).equals("null"))  && (!o2.containsKey(sortname) || o2.get(sortname) == null || ((String)o2.get(sortname)).isEmpty() || o2.get(sortname).equals("null")))
			            	   return -1;
			               else if ((o1.containsKey(sortname) && o2.get(sortname) != null && !((String)o2.get(sortname)).isEmpty() && !o2.get(sortname).equals("null")) && (!o1.containsKey(sortname) || o1.get(sortname) == null || ((String)o1.get(sortname)).isEmpty() || o1.get(sortname).equals("null")))
			            	   return 1;
			               else {
				               if (sortname.equals("name") || sortname.equals("status") || sortname.equals("type") || sortname.equals("schedule")) {
				            	   String q1 = (String) o1.get(sortname);
				            	   String q2 = (String) o2.get(sortname);
				            	   if (sortorder.equals("asc"))
				            		   return q1.compareTo(q2);
				            	   else
				            		   return q2.compareTo(q1);
				               } else if (sortname.equals("numjobs")) {
				            	   @SuppressWarnings("unchecked")
				            	   Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) o1.get("jobs");
				            	   Integer q1 = jobs.size();
				            	   
				            	   @SuppressWarnings("unchecked")
				            	   Map<Integer, Map<String, Object>> jobs2 = (Map<Integer, Map<String, Object>>) o2.get("jobs");
				            	   Integer q2 = jobs2.size();

				            	   if (sortorder.equals("asc"))
				            		   return q1.compareTo(q2);
				            	   else
				            		   return q2.compareTo(q1);
				               } else if (sortname.equals("start") || sortname.equals("end")) {
				            	   String q1aux = (String) o1.get(sortname);
				            	   String q2aux = (String) o2.get(sortname);
				            	   try {
				            		   Date q1 = df.parse(q1aux);
				            		   Date q2 = df.parse(q2aux);
				            		   Long l1 = Long.valueOf(q1.getTime());
				            		   Long l2 = Long.valueOf(q2.getTime());
					            	   if (sortorder.equals("asc"))
					            		   return l1.compareTo(l2);
					            	   else
					            		   return l2.compareTo(l1);
				            	   } catch (Exception ex) {
				            		   logger.error("Error parseando fechas al comparar en searchGroups");
				            		   return 0;
				            	   }
				               } else
				            	   return 0;
			            	}
			            }
					});
				}
				
				if (page != null && page > 0 && rp != null && rp > 0) {
					// Paginamos
					int first = page*rp -rp;
					int last = first+rp;
					
					if (last > groupJobs.size())
						last = groupJobs.size();
					return groupJobs.subList(first, last);
				}
				return groupJobs;
			}
			return groupJobs;
		} catch (Exception ex) {
			logger.error("Error buscando grupos para el grid de grupos de jobs con page: {} rp: {} sortname: {} sortorder: {} query: {} qtype: {}. Ex: {}", new Object[]{page, rp, sortname, sortorder, query, qtype, ex.getMessage()});
			return allGroupJobs;
		}
	}
	
	
	/**
	 * Obtiene el total de grupos de jobs para el grid
	 * @param query
	 * @param qtype
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public static Integer getTotalGroupJobs(String query, String qtype, Configuration c) throws Exception {
		try {
			List<Map<String, Object>> allGroupJobs = listGroupJobs(c);
			List<Map<String, Object>> groupJobs = new ArrayList<Map<String, Object>>();
			if (allGroupJobs != null && allGroupJobs.size()>0) {
				groupJobs.addAll(allGroupJobs);
				
				// Buscamos
				if (query != null && !query.isEmpty() && qtype != null && !qtype.isEmpty())
					groupJobs = searchGroupJobs(allGroupJobs, query, qtype);
			}
			return groupJobs.size();
		} catch (Exception ex) {
			logger.error("Error obteniendo numero total de grupos de jobs para: query: {} qytpe: {}", query , qtype);
			return 0;
		}
	}
	
	
	/**
	 * Busca grupos de jobs
	 * @param allGroupJobs
	 * @param query
	 * @param qtype
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> searchGroupJobs(List<Map<String, Object>> allGroupJobs, String query, String qtype) throws Exception {
		try {
			List<Map<String, Object>> groupJobs = new ArrayList<Map<String, Object>>();
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			if (query != null && !query.isEmpty() && qtype != null && !qtype.isEmpty()) {
				if (allGroupJobs != null && !allGroupJobs.isEmpty()) {
					for (Map<String, Object> groupJob : allGroupJobs) {
						if (qtype.equals("name") || qtype.equals("type")) {
							if (groupJob.containsKey(qtype) && ((String)groupJob.get(qtype)).contains(query)) {
								groupJobs.add(groupJob);
							}
						} else if (qtype.equals("start")) {
							if (groupJob.get("start") != null && !((String) groupJob.get("start")).isEmpty() && groupJob.get("end") != null && !((String) groupJob.get("end")).isEmpty()) {
								Date dquery = df.parse(query);
								Date start = df.parse((String) groupJob.get("start"));
								Date end = df.parse((String) groupJob.get("end"));
								if (dquery.getTime() >= start.getTime() && dquery.getTime() <= end.getTime())
									groupJobs.add(groupJob);
							}
						}
					}
				}
			}
			return groupJobs;
		} catch (Exception ex) {
			logger.error("Error buscando grupos de jobs con: query: {} qtype: {}", query, qtype);
			return null;
		}
	}
	
	
	/**
	 * Save group job XML
	 * @param groupJobName
	 * @param steps
	 * @throws Exception
	 */
	public static void writeGroupJobXml(String groupJobName, String typeGroupJob, String templateJob, Map<Integer, Map<String, Object>> jobs, String schedule, String storage) throws Exception {
		try {
			StringBuilder _sb = new StringBuilder();
			_sb.append("<?xml version=\"1.0\"?>\n");
			_sb.append("<groupJob>\n");
			_sb.append("	<name>"+groupJobName.trim()+"</name>\n");
			_sb.append("	<type>"+typeGroupJob.trim()+"</type>\n");
			if (templateJob != null && !templateJob.trim().isEmpty() && typeGroupJob.equals(TYPE_TEMPLATEJOB))
				_sb.append("	<templateJob>"+templateJob.trim()+"</templateJob>\n");
			if (schedule != null && !schedule.isEmpty())
				_sb.append("	<schedule>"+schedule+"</schedule>\n");
			if (storage != null && !storage.isEmpty())
				_sb.append("	<storage>"+storage+"</storage>\n");
			_sb.append("	<jobs>\n");
			if (jobs != null && jobs.size()>0) {
				for (Integer order : jobs.keySet()) {
					Map<String, Object> job = (Map<String, Object>) jobs.get(order);
					_sb.append("		<job order='"+order+"'>\n");
					_sb.append("			<name>");
					_sb.append(job.get("name"));
					_sb.append("</name>\n");
					if (job.get("typeStep") != null && !((String)job.get("typeStep")).isEmpty()) {
						_sb.append("			<typeStep>");
						_sb.append(job.get("typeStep"));
						_sb.append("</typeStep>\n");
					}
					if (job.get("step") != null && !((String)job.get("step")).isEmpty()) {
						_sb.append("			<step>");
						_sb.append(job.get("step"));
						_sb.append("</step>\n");
					}
					if (job.get("inventory") != null && !((String)job.get("inventory")).isEmpty()) {
						_sb.append("			<inventory>");
						_sb.append(job.get("inventory"));
						_sb.append("</inventory>\n");
					}
					
					_sb.append("		</job>\n");
				}
			}
			_sb.append("	</jobs>\n");
			_sb.append("</groupJob>");
			
			FileOutputStream _fos = new FileOutputStream(new File(getPathGroupJob(groupJobName)));
			_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
			_fos.close();
		} catch (Exception ex) {
			logger.error("Error escribiendo xml de groupJob: {} type: {}", groupJobName, typeGroupJob);
			throw new Exception("Error writting xml file of groupJob "+groupJobName);
		}
	}
	
	
	/**
	 * Obtiene el nombre autogenerado para cierto job en un grupo de job de cierto paso
	 * @param groupJobName
	 * @param order
	 * @param clientName
	 * @param nameStep
	 * @return
	 * @throws Exception
	 */
	public static String getGroupJobNameJob(String groupJobName, Integer order, String clientName, String nameStep) throws Exception {
		return groupJobName+"-"+order+"-"+clientName+"-"+nameStep;
	}
	
	
	/**
	 * Elimina un 
	 * @param jobName
	 * @param groupJobName
	 * @param erase
	 * @param jm
	 * @param _c
	 */
	public static void removeJobFromGroupJob(String jobName, String groupJobName, boolean erase, JobManager jm, Configuration conf) throws Exception {
		try {
			Map<String, Object> groupJob = getGroupJob(groupJobName, conf, null);
			if (groupJob.get("jobs") != null) {
				@SuppressWarnings("unchecked")
				Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
				if (jobs.size()>0) {
					Map<Integer, Map<String, Object>> newListJobs = new TreeMap<Integer, Map<String, Object>>();
					for (Integer order : jobs.keySet()) {
						Map<String, Object> job = jobs.get(order);
						if (job.get("name").equals(jobName)) {
							if (erase) {
								logger.info("Eliminando job {} ...", job.get("name"));
								jm.removeJob((String) job.get("name"));
								removeHiddenFileSet(jobName, jm);
							}
							
						} else {
							newListJobs.put(order, job);
						}
					}
					String schedule = null;
					if (groupJob.get("schedule") != null)
						schedule = (String) groupJob.get("schedule");
					
					String templateJob = null;
					if (groupJob.get("templateJob") != null)
						templateJob = (String) groupJob.get("templateJob");
					
					String storage = null;
					if (groupJob.get("storage") != null)
						storage = (String) groupJob.get("storage");
					
					writeGroupJobXml(groupJobName, (String) groupJob.get("type"), templateJob, newListJobs, schedule, storage);
				}
			}
		} catch (Exception ex) {
			logger.error("Error borrando job: {} del grupo: {}", jobName, groupJobName);
			throw new Exception("Could not remove job "+jobName+" from group "+groupJobName);
		}
	}
	
	
	/**
	 * Elimina el fileset oculto, si lo hay de un job
	 * @param nameJob
	 * @param jm
	 * @throws Exception
	 */
	public static void removeHiddenFileSet(String nameJob, JobManager jm) throws Exception {
		try {
			boolean filesetExistsInOtherJob = false;
			Map<String, String> job = jm.getProgrammedJob(nameJob);
			if (job != null) {
				if (job.get("fileset") != null && !job.get("fileset").isEmpty()) {
					String fileset = job.get("fileset"); 
					if (fileset.contains("---") && fileset.contains("hidden")) {
						List<String> jobs = jm.getAllProgrammedJobs();
						for (String njob : jobs) {
							if (!njob.equals(nameJob)) {
								Map<String, String> _job = jm.getProgrammedJob(njob);
								if (_job.get("fileset") != null && !_job.get("fileset").isEmpty() && _job.get("fileset").equals(fileset)) {
									filesetExistsInOtherJob = true;
									break;
								}
							}
						}
						if (!filesetExistsInOtherJob) {
							BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "filesets", fileset);
						    BaculaConfiguration.trimBaculaFile("/etc/bacula/bacula-dir.conf");
						    
							File _f = new File(WBSAirbackConfiguration.getDirectoryFilesets() + "/" + fileset + ".conf");
							if (_f.exists())
								_f.delete();
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error borrando fileset oculto de: {}", nameJob);
		}
	}
	
	
	/**
	 * Obtiene los grupos que se han generado a partir de cierto templateJob
	 * @param templateJobName
	 * @param conf
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getGroupJobsFromTemplate(String templateJobName, Configuration conf) throws Exception {
		try {
			List<String> groupJobNames = listGroupJobNames();
			List<Map<String, Object>> groups = new ArrayList<Map<String, Object>>();
			if (groupJobNames != null && groupJobNames.size()>0) {
				JobManager jm = new JobManager(conf);
				Map<String, Map<String, String>> allArchivedJobs = jm.getArchivedClientJobs(-1, null, 0, 0);
				for (String groupJobName : groupJobNames) {
					Map<String, Object> groupJob = getGroupJob(groupJobName, conf, allArchivedJobs);
					if (groupJob.get("templateJob") != null && groupJob.get("templateJob").equals(templateJobName)) {
						groups.add(groupJob);
					}
				}
			}
			return groups;
		} catch (Exception ex) {
			logger.error("Error buscando grupos de jobs con el template {} Ex: {}", new Object[]{templateJobName, ex.getMessage()});
			throw new Exception("Could not obtain groups with template: "+templateJobName+" . Ex:"+ex.getMessage());
		}
	}
	
	/**
	 * Busca los jobs que tienen asociada cierta propiedad
	 * @param property
	 * @param value
	 * @param conf
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getJobsWithProperty(String property, String value, Configuration conf) throws Exception {
		try {
			List<String> groupJobNames = listGroupJobNames();
			List<Map<String, Object>> jobsImplied = new ArrayList<Map<String, Object>>();
			if (groupJobNames != null && groupJobNames.size()>0) {
				JobManager jm = new JobManager(conf);
				Map<String, Map<String, String>> allArchivedJobs = jm.getArchivedClientJobs(-1, null, 0, 0); 
				for (String groupJobName : groupJobNames) {
					Map<String, Object> groupJob = getGroupJob(groupJobName, conf, allArchivedJobs);
					if (groupJob.get("jobs") != null) {
						String templateStorageType = null;
						if (groupJob.get("templateJob") != null && !((String)groupJob.get("templateJob")).isEmpty()) {
							templateStorageType = TemplateJobManager.getTypeStorageTemplate((String)groupJob.get("templateJob"));
						}
							
						@SuppressWarnings("unchecked")
						Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
						if (jobs.size()>0) {
							for (Integer order : jobs.keySet()) {
								Map<String, Object> job = jobs.get(order);
								if (job.get(property) != null && ((String)job.get(property)).equals(value)) {
									if (templateStorageType != null)
										job.put("typeStorage", templateStorageType);
									jobsImplied.add(job);
								}
							}
						}
					}
				}
			}
			return jobsImplied;
		} catch (Exception ex) {
			logger.error("Error obteniendo jobs con la propiedad {} = {}. Ex: {}", new Object[]{property, value, ex.getMessage()});
			throw new Exception("Could not obtain jobs in groups with "+property+" = "+value+". Ex:"+ex.getMessage());
		}
	}
	
	/**
	 * 
	 * @param jobName
	 * @return
	 * @throws Exception
	 */
	public static String isJobOnAnyGroup(String jobName, Configuration conf) throws Exception {
		try {
			List<String> groupJobNames = listGroupJobNames();
			if (groupJobNames != null && groupJobNames.size()>0) {
				JobManager jm = new JobManager(conf);
				Map<String, Map<String, String>> allArchivedJobs = jm.getArchivedClientJobs(-1, null, 0, 0);
				for (String groupJobName : groupJobNames) {
					Map<String, Object> groupJob = getGroupJob(groupJobName, conf, allArchivedJobs);
					if (groupJob.get("jobs") != null) {
						@SuppressWarnings("unchecked")
						Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
						if (jobs.size()>0) {
							for (Integer order : jobs.keySet()) {
								Map<String, Object> job = jobs.get(order);
								if (job.get("name") != null && ((String)job.get("name")).equals(jobName))
									return groupJobName;
							}
						}
					}
				}
			}
			return null;
		} catch (Exception ex) {
			logger.error("Error comprobando si el job {} pertenece a algún grupo. Ex: {}", jobName, ex);
			return null;
		}
	}
	
	
	/**
	 * Comprueba si una alerta de job (good, warning, error) es peor que otra
	 * @param a1
	 * @param a2
	 * @return
	 * @throws Exception
	 */
	private static boolean isWorstAlert(String a1, String a2) throws Exception {
		try {
			if (a1 != null && !a1.equals("")) {
				if (a1.equals("error"))
					return true;
				else if (a2 != null && !a2.equals("")) {
					if (!a2.equals("error") && a1.equals("warning"))
						return true;
					else
						return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception ex) {
			logger.error("Error comparando alertas ... Ex: {}", ex);
			return false;
		}
	}
	
	
	/**
	 * Indica si una fecha dada de un job es anterior a otra
	 * @param time1
	 * @param time2
	 * @return
	 * @throws Exception
	 */
	private static boolean isEarlierTime(String time1, String time2) throws Exception {
		try {
			if (time1 != null && !time1.equals("")) {
				if (time2 != null && !time2.equals("")) {
					DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					Date date1 = df.parse(time1);
					Date date2 = df.parse(time2);
					return date1.getTime()<=date2.getTime();
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception ex) {
			logger.error("Error comparando fechas de jobs ... Ex: {}", ex.getMessage());
			return false;
		}
	}
	
	public static void restartGroupJob(String name, Configuration c, BackupOperator bo, ClientManager cm) throws Exception {
		Map<String, Object> groupJob = GroupJobManager.getGroupJob(name, c, null);
		if (groupJob != null) {
	       	if (groupJob.get("jobs") != null) {
	       		@SuppressWarnings("unchecked")
				Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
	       		if (!jobs.isEmpty()) {
	       			for (Integer order : jobs.keySet()) {
	       				Map<String, Object> job = jobs.get(order);
	       				if (job.get("id") != null) {
	       					bo.restartIncompleteJob(Integer.parseInt((String) job.get("id")));
	       					return;
	       				} else {
	       					bo.runJob(cm.getClientId((String)job.get("client")), (String) job.get("name"));
	       					return;
	       				}
	       			}
	       		}
	       	}
		 }
	}
	public static void cancelGroupJob(String name, Configuration c, BackupOperator bo) throws Exception {
		Map<String, Object> groupJob = GroupJobManager.getGroupJob(name, c, null);
		if (groupJob != null) {
	       	if (groupJob.get("jobs") != null) {
	       		@SuppressWarnings("unchecked")
				Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
	       		if (!jobs.isEmpty()) {
	       			for (Integer order : jobs.keySet()) {
	       				Map<String, Object> job = jobs.get(order);
	       				if (job.get("run") != null && job.get("run").equals("true") && job.get("id") != null)
	       					bo.cancelJob(Integer.parseInt((String) job.get("id")));
	       			}
	       		}
	       	}
		 }
	}
	
	public static void stopGroupJob(String name, Configuration c, BackupOperator bo) throws Exception {
		Map<String, Object> groupJob = GroupJobManager.getGroupJob(name, c, null);
		if (groupJob != null) {
	       	if (groupJob.get("jobs") != null) {
	       		@SuppressWarnings("unchecked")
				Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
	       		if (!jobs.isEmpty()) {
	       			for (Integer order : jobs.keySet()) {
	       				Map<String, Object> job = jobs.get(order);
	       				if (job.get("run") != null && job.get("run").equals("true") && job.get("id") != null)
	       					bo.stopJob( Integer.parseInt((String) job.get("id")));
	       			}
	       		}
	       	}
		 }
	}
	
	public static boolean launchGroupJob(String name, Configuration c, BackupOperator bo, ClientManager cm) throws Exception {
		Map<String, Object> groupJob = GroupJobManager.getGroupJob(name, c, null);
		if (groupJob != null) {
	       	if (groupJob.get("jobs") != null) {
	       		@SuppressWarnings("unchecked")
				Map<Integer, Map<String, Object>> jobs = (Map<Integer, Map<String, Object>>) groupJob.get("jobs");
	       		if (!jobs.isEmpty()) {
	       			for (Integer order : jobs.keySet()) {
	       				Map<String, Object> firstJob = jobs.get(order);
	       				if (firstJob.get("level") != null) { // Comprobamos que no sea un job eliminado
	       					try {
	       						bo.runJob(cm.getClientId((String)firstJob.get("client")), (String) firstJob.get("name"));
	       						return true;
	       					} catch (Exception ex) {
	       						throw new Exception("Error launching job "+(String) firstJob.get("name")+". "+ex.getMessage());
	       					}
	       				}
	       			}
	       		}
	       	}
		 }
		return false;
	}
}
