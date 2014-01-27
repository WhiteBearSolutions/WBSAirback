package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "system")
public class SystemRs {

	private String name;
	private String description;
	private List<ReferenceRs> scripts;
	private List<ReferenceRs> applications;
	
	
	public SystemRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( SystemRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static SystemRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( SystemRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			SystemRs o = (SystemRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<system>"), xml.indexOf("</system>")+"</system>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public void addApplication(String name) {
		List<ReferenceRs> apps = this.getApplications();
		if (apps == null)
			apps = new ArrayList<ReferenceRs>();
		for (ReferenceRs app : apps) {
			if (app.getName().equals(name))
				return;
		}
		ReferenceRs app = new ReferenceRs();
		app.setName(name);
		apps.add(app);
		this.setApplications(apps);
	}
	
	public void removeApplication(String name) {
		List<ReferenceRs> apps = this.getApplications();
		List<ReferenceRs> newApps = new ArrayList<ReferenceRs>();
		if (apps != null && !apps.isEmpty()) {
			for (ReferenceRs app : apps) {
				if (!app.getName().equals(name))
					newApps.add(app);
			}
			setApplications(newApps);
		}
	}
	
	public void addScript(String name) {
		List<ReferenceRs> scripts = this.getScripts();
		if (scripts == null)
			scripts = new ArrayList<ReferenceRs>();
		for (ReferenceRs script : scripts) {
			if (script.getName().equals(name))
				return;
		}
		ReferenceRs script = new ReferenceRs();
		script.setName(name);
		scripts.add(script);
		this.setScripts(scripts);
	}
	
	public void removeScript(String name) {
		List<ReferenceRs> scripts = this.getScripts();
		List<ReferenceRs> scriptsNew = new ArrayList<ReferenceRs>();
		if (scripts != null && !scripts.isEmpty()) {
			for (ReferenceRs script : scripts) {
				if (!script.getName().equals(name))
					scriptsNew.add(script);
			}
			this.setScripts(scriptsNew);
		}
	}

	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElementWrapper(name="scripts")
	@XmlElementRef()
	public List<ReferenceRs> getScripts() {
		return scripts;
	}

	public void setScripts(List<ReferenceRs> scripts) {
		this.scripts = scripts;
	}

	@XmlElementWrapper(name="applications")
	@XmlElementRef()
	public List<ReferenceRs> getApplications() {
		return applications;
	}

	public void setApplications(List<ReferenceRs> applications) {
		this.applications = applications;
	}
}
