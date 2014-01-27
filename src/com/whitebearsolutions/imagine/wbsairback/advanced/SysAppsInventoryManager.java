package com.whitebearsolutions.imagine.wbsairback.advanced;

import java.io.File;
import java.util.List;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ApplicationRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.InventoryAppSo;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.ReferenceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.advanced.SystemRs;
import com.whitebearsolutions.io.FileUtils;

public class SysAppsInventoryManager {
	
	//private final static Logger logger = LoggerFactory.getLogger(SysAppsInventoryManager.class);
	private final static String path = WBSAirbackConfiguration.getDirectoryAdvancedAppSo();
	private final static String pathBackup = WBSAirbackConfiguration.getDirectoryAdvancedAppSo()+".backup";
	private InventoryAppSo inventory;
	
	public final static String GENERIC_SYSTEM_NAME = "GenericSystem";
	
	public SysAppsInventoryManager() throws Exception{
		init(null, true);
	}
	
	public SysAppsInventoryManager(String path) throws Exception {
		init(path, false);
	}
	
	public void init(String _path, boolean backup) throws Exception {
		if (_path == null || _path.isEmpty())
			_path= getPath();
		File _f=new File(_path);		
		if (!_f.exists()){
			try {
				File _fDirectory=new File(_path.substring(0, _path.lastIndexOf("/")));
				if (!_fDirectory.exists()){
					_fDirectory.mkdir();
				}
			} catch (Exception ex){
				if (backup) {
					File _fDirectory=new File(pathBackup.substring(0, _path.lastIndexOf("/")));
					if (!_fDirectory.exists()){
						_fDirectory.mkdir();
					}
				}
			}
			inventory = new InventoryAppSo();
		} else {
			String xml = "";
			try {
				 xml = FileUtils.fileToString(_path);
			} catch (Exception ex) {
				if (backup) {
					xml = FileUtils.fileToString(pathBackup);
				}
			}
			inventory = InventoryAppSo.fromXML(xml);
		}
		if (getSystem(GENERIC_SYSTEM_NAME) == null)
			saveSystem(GENERIC_SYSTEM_NAME, GENERIC_SYSTEM_NAME, false);
	}
	
	public static String getPath() throws Exception {
		return path;
	}
	
	public static String getPathBackup() throws Exception {
		return pathBackup;
	}
	
	
	public void saveApps(String name, String description, List<String> systems, boolean edit) throws Exception {
		if (name == null || name.isEmpty())
			throw new Exception("name is required");
		if (description == null || description.isEmpty())
			throw new Exception("description is required");
		if (systems == null || systems.isEmpty())
			throw new Exception("some system is required");
		
		ApplicationRs app = new ApplicationRs();
		app.setName(name);
		app.setDescription(description);
		for (String sys : systems)
			app.addSystem(sys);
		
		if (!edit) {
			inventory.addApplication(app);
		} else {
			inventory.updateApplication(app);
		}
		inventory.updateReferencesApplication(name, systems);
		update();
	}
	
	public void addScript(String scriptName, String application, String system) throws Exception {
		if (application != null && !application.isEmpty() ) {
			ApplicationRs app = inventory.getApplication(application);
			app.addScript(system, scriptName);
		} else {
			SystemRs sys = inventory.getSystem(system);
			sys.addScript(scriptName);
		}
		
		update();
	}
	
	public void deleteScript(String scriptName, String application, String system) throws Exception {
		if (application != null && !application.isEmpty() ) {
			ApplicationRs app = inventory.getApplication(application);
			app.removeScript(scriptName);
		} else {
			SystemRs sys = inventory.getSystem(system);
			sys.removeScript(scriptName);
		}
		update();
	}
	
	public void update() throws Exception {
		FileSystem.writeFile(new File(getPath()), inventory.getXML());
		FileSystem.writeFile(new File(getPathBackup()), inventory.getXML());
	}
	
	
	/**
	 * Lista las aplicaciones definidas en formato de mapas de valores
	 * @return
	 */
	public List<ApplicationRs> listApps() throws Exception {
		return inventory.getApplications();
	}
	
	/**
	 * Obtiene los valores de una aplicacion dado
	 * @param stepName
	 * @return
	 * @throws Exception
	 */
	public ApplicationRs getApplication(String appName) throws Exception {
		return inventory.getApplication(appName);		
	}
	
	/**
	 * Elimina una aplicacion cuyo nombre entra por parametro
	 * @param appName
	 * @return
	 * @throws Exception
	 */
	public void deleteApplication(String appName, ScriptProcessManager sm) throws Exception {
		String script = sm.isAppOnScriptProcess(appName);
		if (script != null)
			throw new Exception("Application used in script "+script);
		inventory.deleteApplication(appName);
		update();
	}
	
	
	/**
	 * 
	 * @param nameSyst
	 * @param desSyst
	 * @throws Exception
	 */
	public void saveSystem(String name, String description, boolean edit) throws Exception {
		if (name == null || name.isEmpty())
			throw new Exception("name is required");
		if (description == null || description.isEmpty())
			throw new Exception("description is required");
		
		SystemRs sys = new SystemRs();
		sys.setName(name);
		sys.setDescription(description);
		
		if (!edit) {
			inventory.addSystem(sys);
		} else {
			inventory.updateSystem(sys);
		}
		update();
	}
	
	
	/**
	 * Lista los sistemas definidas en formato de mapas de valores
	 * @return
	 */
	public List<SystemRs> listSystem() throws Exception {
		return inventory.getSystems();
	}
	
	
	/**
	 * Obtiene los scripts de cierta aplicacion
	 * @param nameApp
	 * @param nameSys
	 * @return
	 * @throws Exception
	 */
	public List<ReferenceRs> getApplicationScripts(String nameApp, String nameSys) throws Exception {
		if (nameApp == null || nameApp.isEmpty() || nameSys == null || nameSys.isEmpty())
			return null;
		
		ApplicationRs app = getApplication(nameApp);
		if (app == null || app.getSystems() == null || app.getSystems().isEmpty())
			return null;
		for (SystemRs sys : app.getSystems()) {
			if (sys.getName().equals(nameSys)) {
				if (sys.getScripts() != null && !sys.getScripts().isEmpty())
					return sys.getScripts();
			}
		}
		
		return null;
	}
	
	
	/**
	 * Obtiene los scripts de un sistema dado
	 * @param nameSys
	 * @return
	 * @throws Exception
	 */
	public List<ReferenceRs> getSystemScripts(String nameSys) throws Exception {
		if (nameSys == null || nameSys.isEmpty())
			return null;
		
		SystemRs sys = getSystem(nameSys);
		if (sys == null || sys.getScripts() == null || sys.getScripts().isEmpty())
			return null;
		else
			return sys.getScripts();
	}
	
	
	/**
	 * Obtiene el objeto sistema para el nombre recibido
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public SystemRs getSystem(String name) throws Exception {
		return inventory.getSystem(name);		
	}
	
	
	/**
	 * Elimina un systema dado su nombre
	 * @param systemName
	 * @return
	 * @throws Exception
	 */
	public void deleteSystem(String systemName, ScriptProcessManager sm) throws Exception {
		String script = sm.isSystemOnScriptProcess(systemName);
		if (script != null)
			throw new Exception("System used in script "+script);

		if (inventory.getApplications() != null && !inventory.getApplications().isEmpty()) {
			for (ApplicationRs app : inventory.getApplications()) {
				if (app.getSystems() != null && !app.getSystems().isEmpty()) {
					for (SystemRs sys : app.getSystems()) {
						if (sys.getName().equals(systemName))
							throw new Exception("System used on application: "+app.getName());
					}
				}
			}
		}
			
		inventory.deleteSystem(systemName);
		update();
	}
}



