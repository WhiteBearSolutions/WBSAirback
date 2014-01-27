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

@XmlRootElement(name = "application")
public class ApplicationRs {

	private String name;
	private String description;
	private List<SystemRs> systems;
	
	public ApplicationRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ApplicationRs.class );
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
	public static ApplicationRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( ApplicationRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			ApplicationRs o = (ApplicationRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<application>"), xml.indexOf("</application>")+"</application>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public boolean hasSystem(String nameSystem) {
		if (getSystems() != null && !getSystems().isEmpty()) {
			for (SystemRs system : getSystems()) {
				if (system.getName().equals(nameSystem))
					return true;
			}
		}
		return false;
	}
	
	public void addSystem(String system) {
		SystemRs ref = new SystemRs();
		ref.setName(system);
		ref.setScripts(null);
		
		List<SystemRs> systemsRef = null;
		if (getSystems() == null || getSystems().isEmpty()) {
			systemsRef = new ArrayList<SystemRs>();
		} else {
			systemsRef = getSystems();
		}
		systemsRef.add(ref);
		setSystems(systemsRef);
	}
	
	public void removeSystem(String system) {
		List<SystemRs> systems = getSystems();
		if (systems != null && !systems.isEmpty()) {
			List<SystemRs> systemsNew = new ArrayList<SystemRs>();
			for (SystemRs sys : systems) {
				if (!sys.getName().equals(system))
					systemsNew.add(sys);
			}
			setSystems(systemsNew);
		}
	}
	
	public void removeScript(String script) {
		List<SystemRs> systems = getSystems();
		List<SystemRs> systemsNew = new ArrayList<SystemRs>();
		for (SystemRs sys : systems) {
			if (sys.getScripts() != null && !sys.getScripts().isEmpty()) {
				List<ReferenceRs> newScripts = new ArrayList<ReferenceRs>();
				for (ReferenceRs _script : sys.getScripts()) {
					if (!_script.getName().equals(script))
						newScripts.add(_script);
				}
				sys.setScripts(newScripts);
			}
			systemsNew.add(sys);
		}
		setSystems(systemsNew);
	}
	
	/**
	 * Añade un script
	 * @param name
	 */
	public void addScript(String nameSys, String nameScript) {
		List<SystemRs> systems = getSystems();
		for (SystemRs sys : systems) {
			if (sys.getName().equals(nameSys))
				sys.addScript(nameScript);
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

	@XmlElementWrapper(name="systems",required=true)
	@XmlElementRef()
	public List<SystemRs> getSystems() {
		return systems;
	}

	public void setSystems(List<SystemRs> systems) {
		this.systems = systems;
	}
}
