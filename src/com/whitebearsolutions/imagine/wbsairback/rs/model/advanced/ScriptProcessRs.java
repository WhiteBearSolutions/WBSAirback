package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "scriptProcess")
public class ScriptProcessRs {

	private String name;
	private String type;
	private Boolean abortType;
	private String step;
	private String application;
	private String system;
	
	private List<VariableRs> variables;
	private List<ScriptItemRs> scripts;
	
	
	public ScriptProcessRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ScriptProcessRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de scripts a objetos script
	 * @param mapPools
	 * @return
	 */
	public static List<ScriptProcessRs> listMapToObject(Map<String, Map<String, Object>> mapScriptProcesss) throws Exception {
		List<ScriptProcessRs> scripts = new ArrayList<ScriptProcessRs>();
		if (mapScriptProcesss != null && mapScriptProcesss.size()>0) {
			for (String nameScript : mapScriptProcesss.keySet()) {
				ScriptProcessRs script = mapToObject(mapScriptProcesss.get(nameScript));
				scripts.add(script);
			}
		}
		return scripts;
	}
	
	public static ScriptProcessRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "scriptProcess";
				JAXBContext jc = JAXBContext.newInstance( ScriptProcessRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					ScriptProcessRs o = (ScriptProcessRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<ScriptProcessRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "scriptProcess";
			List<ScriptProcessRs> listObjects = new ArrayList<ScriptProcessRs>();
			
			if (xml != null && xml.length()>0) {
				int iInitList = xml.indexOf("<"+idList+">");
				int iEndList = xml.indexOf("</"+idList+">");
				if ( iInitList > 0 && iEndList > -1) { 
					String list = xml.substring(iInitList+("<"+idList+">").length(), iEndList);
					while (list.indexOf("<"+nameEntity+">")>-1) {
						String deviceXml = list.substring(list.indexOf("<"+nameEntity+">"), list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						listObjects.add(fromXML(deviceXml));
						if (list.length() > list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()) {
							list = list.substring(list.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length());
						} else {
							break;
						}
					}
				}
			}
			return listObjects;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Convierte un mapa de valores de un script a un objeto ScriptProcess 
	 * @param mapPool
	 * @return
	 */
	public static ScriptProcessRs mapToObject(Map<String, Object> mapScript) throws Exception {
		ScriptProcessRs script = new ScriptProcessRs();

		Object p = mapScript.get("name");
		if (p != null && !((String)p).isEmpty())
			script.setName((String) p);
		p = mapScript.get("abortType");
		if (p != null && !((String)p).isEmpty() && ((String)p).equals("true"))
			script.setAbortType(true);
		else
			script.setAbortType(false);
		p = mapScript.get("application");
		if (p != null && !((String)p).isEmpty())
			script.setApplication((String) p);
		p = mapScript.get("scripts");
		if (p != null) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> scripts = (List<Map<String, Object>>) mapScript.get("scripts");
			if (!scripts.isEmpty())
				script.setScripts(ScriptItemRs.listMapToObject(scripts));
		}
		p = mapScript.get("step");
		if (p != null && !((String)p).isEmpty())
			script.setStep((String) p);
		p = mapScript.get("system");
		if (p != null && !((String)p).isEmpty())
			script.setSystem((String) p);
		p = mapScript.get("type");
		if (p != null && !((String)p).isEmpty())
			script.setType((String) p);
		p = mapScript.get("variables");
		if (p != null) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> variables = (List<Map<String, Object>>) mapScript.get("variables");
			if (!variables.isEmpty())
				script.setVariables(VariableRs.listMapToObject(variables));
		}
		
		return script;
	}
	
	public static Map<String, Object> objectToMap(ScriptProcessRs o) throws Exception {
		Map<String, Object> script = new HashMap<String, Object>();

		if (o.getName() != null && !o.getName().isEmpty())
			script.put("name", o.getName());
		if (o.getAbortType() != null && o.getAbortType().booleanValue() == true)
			script.put("abortType", "true");
		else
			script.put("abortType", "false");
		if (o.getApplication() != null && !o.getApplication().isEmpty())
			script.put("application", o.getApplication());
		if (o.getSystem() != null && !o.getSystem().isEmpty())
			script.put("system", o.getSystem());
		else
			throw new Exception("advanced.script.exception.nosystem");
		if (o.getStep() != null && !o.getStep().isEmpty())
			script.put("step", o.getStep());
		else
			throw new Exception("advanced.script.exception.nostep");
		if (o.getType() != null && !o.getType().isEmpty())
			script.put("type", o.getType());
		
		if (o.getScripts() != null && !o.getScripts().isEmpty()) {
			List<Map<String, Object>> scripts = new ArrayList<Map<String, Object>>();
			for (ScriptItemRs s : o.getScripts()) {
				Map<String, Object> si = ScriptItemRs.objectToMap(s);
				scripts.add(si);
			}
			script.put("scripts", scripts);
		} else {
			throw new Exception("advanced.script.exception.noscripts");
		}
		
		if (o.getVariables() != null && !o.getVariables().isEmpty()) {
			List<Map<String, Object>> variables = new ArrayList<Map<String, Object>>();
			for (VariableRs s : o.getVariables()) {
				Map<String, Object> var = VariableRs.objectToMap(s);
				variables.add(var);
			}
			script.put("variables", variables);
		}
		
		return script;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getAbortType() {
		return abortType;
	}

	public void setAbortType(Boolean abortType) {
		this.abortType = abortType;
	}

	@XmlElement(required=true)
	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	@XmlElement(required=true)
	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	@XmlElementWrapper(name="variables")
	@XmlElementRef()
	public List<VariableRs> getVariables() {
		return variables;
	}

	public void setVariables(List<VariableRs> variables) {
		this.variables = variables;
	}

	@XmlElementWrapper(name="scriptItems",required=true)
	@XmlElementRef()
	public List<ScriptItemRs> getScripts() {
		return scripts;
	}

	public void setScripts(List<ScriptItemRs> scripts) {
		this.scripts = scripts;
	}
	
}
