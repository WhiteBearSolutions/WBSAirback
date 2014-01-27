package com.whitebearsolutions.imagine.wbsairback.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.advanced.ScriptProcessManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.util.Configuration;


/**
 * Clase que se encarga de los procesos generales de migración de version en WBSAirback
 * @author jorge.gea
 *
 */
public class UpdateVersion {

	private Configuration c;
	private int configVersion;
	private int packageVersion;
	
	public static final String VERSION_PROPERTY = "wbsairback.version";
	
	private final static Logger logger = LoggerFactory.getLogger(UpdateVersion.class);
	
	/**
	 * Esta clase solamente requiere la llamada al constructor, que se encarga de todo
	 */
	public UpdateVersion() {
		try {
			logger.info("Obteniendo version del producto");
			String sVersion = GeneralSystemConfiguration.getVersion();
			File fileConf = new File(WBSAirbackConfiguration.getFileConfiguration());
			if (fileConf.exists()) {
				c = new Configuration(fileConf);
				if (sVersion.contains("-com"))
					sVersion = sVersion.replace("-com", "");
				if (sVersion.contains("-"))
					sVersion = sVersion.substring(0, sVersion.indexOf("-"));
				sVersion = sVersion.replace(".", "");
				sVersion = sVersion.replace("\n", "");
				sVersion = sVersion.replace("\r", "");
				sVersion = sVersion.trim();
				packageVersion = Integer.parseInt(sVersion);
				logger.info("La version del paquete instalado es: {}", packageVersion);
				
				if (!c.hasProperty(VERSION_PROPERTY))
					configVersion = 0;
				else {
					configVersion = Integer.parseInt(c.getProperty(VERSION_PROPERTY));
				}
				logger.info("La version en el config es: {}", configVersion);
				migrationProcess();
				c.setProperty(VERSION_PROPERTY, String.valueOf(packageVersion));
				logger.info("Version en config establecida a {}", packageVersion);
				c.store();
			}
		} catch (Exception ex) {
			logger.error("Error intentando establecer la version del producto! Ex: {}", ex.getMessage());
		}
	}
	
	/**
	 * Metodo general al que añadir todos los metodos particulares de migracion
	 * @throws Exception
	 */
	public void migrationProcess() throws Exception {
		logger.info("Ejecutamos proceso general de actualización ...");
		migrate140000();
	}
	
	
	/**
	 * Migracion a versiones anteriores a la 14.00.00
	 *  - Migracion de script a process scripts
	 *  - Establecimiento de refreservation y refquota para volumenes ZFS
	 * @throws Exception
	 */
	public void migrate140000() throws Exception {
		int minVersion = 140000;
		if (configVersion < minVersion) {
			logger.info("La version es menor que {}. Ejecutamos migracion esta versión.", minVersion);
			try {
				ScriptProcessManager.launchAllJobScriptTransformation(c);
				VolumeManager.setVolumeMissingRefValues();
			} catch (Exception ex) {
				logger.error("ERROR migrando a version {}. Ex: {}", minVersion, ex.getMessage());
				throw new Exception("ERROR migrando a version "+minVersion+". Ex:"+ ex.getMessage());
			}
		} else {
			logger.info("La version es mayor o igual que {}. NO ejecutamos migracion esta versión.", minVersion);
		}
	}
}
