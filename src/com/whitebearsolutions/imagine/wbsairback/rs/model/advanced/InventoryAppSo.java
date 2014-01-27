package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "inventoryAppSo")
public class InventoryAppSo {

	private List<ApplicationRs> applications;
	private List<SystemRs> systems;
	
	public InventoryAppSo() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() throws Exception{
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( InventoryAppSo.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			throw new Exception("Error getting XML:"+ex.getMessage());
		}
		return xml;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static InventoryAppSo fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( InventoryAppSo.class );
			Unmarshaller um = jc.createUnmarshaller();
			InventoryAppSo o = (InventoryAppSo) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<inventoryAppSo>"), xml.indexOf("</inventoryAppSo>")+"</inventoryAppSo>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	/**
	 * Comprueba si un sistema existe en el inventario
	 * @param nameSystem
	 * @return
	 * @throws Exception
	 */
	public boolean existsSystem(String nameSystem) throws Exception {
		if (this.getSystems() != null && !this.getSystems().isEmpty()) { 
			for (SystemRs sys : this.getSystems()) {
				if (sys.getName().equals(nameSystem))
					return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Añade una nuevo sistema al inventario
	 * @param sys
	 * @return
	 * @throws Exception
	 */
	public boolean addSystem(SystemRs sys) throws Exception {
		if (sys != null) {
			if (sys.getName() != null && !sys.getName().isEmpty()) {
				if (this.getSystems() != null && !this.getSystems().isEmpty()) { 
					if (existsSystem(sys.getName())) {
						throw new Exception("there exists another system with name "+ sys.getName());
					}
					List<SystemRs> syss = this.getSystems();
					syss.add(sys);
					this.setSystems(syss);
					return true;
				} else {
					List<SystemRs> syss = new ArrayList<SystemRs>();
					syss.add(sys);
					this.setSystems(syss);
					return true;
				}
			} else
				throw new Exception("name is required fields to add an system");
		} else
			throw new Exception("tried to add empty system");
	}
	
	
	/**
	 * Edita un sistema del inventario
	 * @param sys
	 * @return
	 * @throws Exception
	 */
	public boolean updateSystem(SystemRs sys) throws Exception {
		if (sys != null) {
			if (sys.getName() != null && !sys.getName().isEmpty()) {
				if (this.getSystems() != null && !this.getSystems().isEmpty()) { 
					SystemRs sysOld = getSystem(sys.getName());
					if (sysOld == null)
						throw new Exception("there is no system with name "+ sys.getName());
					List<SystemRs> syss = this.getSystems();
					if (sysOld.getApplications() != null)
						sys.setApplications(sysOld.getApplications());
					if (sysOld.getScripts() != null)
						sys.setApplications(sysOld.getScripts());
					syss.remove(sysOld);
					syss.add(sys);
					this.setSystems(syss);
					return true;
				} else {
					throw new Exception("there is no system with name "+ sys.getName());
				}
			} else
				throw new Exception("name and some system are required fields to update an system");
		} else
			throw new Exception("tried to update empty system");
	}
	
	
	/**
	 * Obtiene una aplicacion a partir de su nombre
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public SystemRs getSystem(String name) throws Exception {
		if (this.getSystems() != null && !this.getSystems().isEmpty()) { 
			for (SystemRs sys : this.getSystems()) {
				if (sys.getName().equals(name))
					return sys;
			}
		}
		return null;
	}
	
	
	/**
	 * Comprueba si una sistema existe en el inventario
	 * @param nameSystem
	 * @return
	 * @throws Exception
	 */
	public boolean existsApplication(String nameApplication) throws Exception {
		if (this.getApplications() != null && !this.getApplications().isEmpty()) { 
			for (ApplicationRs app : this.getApplications()) {
				if (app.getName().equals(nameApplication))
					return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Elimina un sistema del inventario
	 * @param nameSystem
	 * @throws Exception
	 */
	public void deleteSystem(String nameSystem) throws Exception {
		SystemRs sysRemove = this.getSystem(nameSystem);
		deleteReferencesSystems(nameSystem);
		this.getSystems().remove(sysRemove);
	}
	
	
	/**
	 * Elimina una aplicación del inventario
	 * @param nameApplication
	 * @throws Exception
	 */
	public void deleteApplication(String nameApplication) throws Exception {
		ApplicationRs appRemove = this.getApplication(nameApplication);
		updateReferencesApplication(nameApplication, null);
		this.getApplications().remove(appRemove);
	}
	
	
	/**
	 * Elimina las posibles referencias a un sistema
	 * @param nameSystem
	 * @throws Exception
	 */
	private void deleteReferencesSystems(String nameSystem) throws Exception {
		if (this.getApplications() != null && !this.getApplications().isEmpty()) {
			for (ApplicationRs app : this.getApplications()) {
				List<SystemRs> systems = app.getSystems();
				SystemRs sysRemove = null;
				for (SystemRs sys : systems) {
					if (sys.getName().equals(nameSystem)) {
						sysRemove = sys;
					}
				}
				if (sysRemove != null) {
					systems.remove(sysRemove);
					app.setSystems(systems);
				}
			}
		}
	}
	
	
	/**
	 * Elimina las posibles referencias a una aplicación y añade éstas a cierto conjuto de sistemas
	 * @param nameApp
	 * @param nameSystems
	 * @throws Exception
	 */
	public void updateReferencesApplication(String nameApp, List<String> nameSystems) throws Exception {
		if (this.getSystems() != null && !this.getSystems().isEmpty()) {
			for (SystemRs system : this.getSystems()) {
				List<ReferenceRs> apps = system.getApplications();
				if (apps != null && apps.size()>0) {
					ReferenceRs appRemove = null;
					for (ReferenceRs app : apps) {
						if (app.getName().equals(nameApp)) {
							appRemove = app;
						}
					}
					if (appRemove != null) {
						apps.remove(appRemove);
						system.setApplications(apps);
					}
				}
			}
		}
		
		if (nameSystems != null && !nameSystems.isEmpty()) {
			for (String nameSys : nameSystems) {
				SystemRs system = getSystem(nameSys);
				if (system != null)
					system.addApplication(nameApp);
			}
		}
	}
	
	
	/**
	 * Añade una nueva aplicacion al inventario
	 * @param app
	 * @return
	 * @throws Exception
	 */
	public boolean addApplication(ApplicationRs app) throws Exception {
		if (app != null) {
			if (app.getName() != null && !app.getName().isEmpty() && app.getSystems() != null && !app.getSystems().isEmpty()) {
				if (this.getApplications() != null && !this.getApplications().isEmpty()) { 
					if (existsApplication(app.getName())) {
						throw new Exception("there exists another application with name "+ app.getName());
					}
					List<ApplicationRs> apps = this.getApplications();
					apps.add(app);
					this.setApplications(apps);
					return true;
				} else {
					List<ApplicationRs> apps = new ArrayList<ApplicationRs>();
					apps.add(app);
					this.setApplications(apps);
					return true;
				}
			} else
				throw new Exception("name and some system are required fields to add an application");
		} else
			throw new Exception("tried to add empty application");
	}
	
	
	/**
	 * Edita una aplicacion del inventario
	 * @param app
	 * @return
	 * @throws Exception
	 */
	public boolean updateApplication(ApplicationRs app) throws Exception {
		if (app != null) {
			if (app.getName() != null && !app.getName().isEmpty() && app.getSystems() != null && !app.getSystems().isEmpty()) {
				if (this.getApplications() != null && !this.getApplications().isEmpty()) { 
					ApplicationRs appOld = getApplication(app.getName());
					if (appOld == null)
						throw new Exception("there is no application with name "+ app.getName());
					List<ApplicationRs> apps = this.getApplications();
					apps.remove(appOld);
					apps.add(app);
					this.setApplications(apps);
					return true;
				} else {
					throw new Exception("there is no application with name "+ app.getName());
				}
			} else
				throw new Exception("name and some system are required fields to update an application");
		} else
			throw new Exception("tried to update empty application");
	}
	
	
	/**
	 * Obtiene una aplicacion a partir de su nombre
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ApplicationRs getApplication(String name) throws Exception {
		if (this.getApplications() != null && !this.getApplications().isEmpty()) { 
			for (ApplicationRs app : this.getApplications()) {
				if (app.getName().equals(name))
					return app;
			}
		}
		return null;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElementWrapper(name="applications")
	@XmlElementRef()
	public List<ApplicationRs> getApplications() {
		return applications;
	}

	public void setApplications(List<ApplicationRs> applications) {
		this.applications = applications;
	}

	@XmlElementWrapper(name="systems")
	@XmlElementRef()
	public List<SystemRs> getSystems() {
		return systems;
	}

	public void setSystems(List<SystemRs> systems) {
		this.systems = systems;
	}
}
