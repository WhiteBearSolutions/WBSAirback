package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.MultiPathManager;

@XmlRootElement(name = "maintenanceConfiguration")
public class MaintenanceConfigurationRs {
	
	private Integer dbMaxConnections;
	private Integer dbSharedBuffers;
	private Integer dbCache;
	
	private String exportShare;
	private Integer exportHour;
	private Integer exportRetention;
	
	private Boolean multipath;
	
	public MaintenanceConfigurationRs() {}

	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( MaintenanceConfigurationRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	public static MaintenanceConfigurationRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "maintenanceConfiguration";
				JAXBContext jc = JAXBContext.newInstance( ProxyRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					MaintenanceConfigurationRs o = (MaintenanceConfigurationRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static MaintenanceConfigurationRs getObject(GeneralSystemConfiguration sc) throws Exception {
		MaintenanceConfigurationRs config = new MaintenanceConfigurationRs();
		
		Map<String, Integer> _db_parameters = GeneralSystemConfiguration.getDataBaseConfiguration();
		
		config.setDbMaxConnections(_db_parameters.get("max_connections"));
		config.setDbSharedBuffers(_db_parameters.get("shared_buffers"));
		config.setDbCache(_db_parameters.get("cache"));
		
		if(sc.getExportShare() != null) {
			config.setExportShare(sc.getExportShare());
		}
		
		if (sc.getExportHour() > -1) {
			config.setExportHour(sc.getExportHour());
		}
		
		if (sc.getExportRetention() > 0) {
			config.setExportRetention(sc.getExportRetention());
		}
		
		if (MultiPathManager.isMultipathEnabled()) {
			config.setMultipath(true);
		} else
			config.setMultipath(false);
		
		return config;
	}

	
	public Integer getDbMaxConnections() {
		return dbMaxConnections;
	}

	public void setDbMaxConnections(Integer dbMaxConnections) {
		this.dbMaxConnections = dbMaxConnections;
	}

	public Integer getDbSharedBuffers() {
		return dbSharedBuffers;
	}

	public void setDbSharedBuffers(Integer dbSharedBuffers) {
		this.dbSharedBuffers = dbSharedBuffers;
	}

	public Integer getDbCache() {
		return dbCache;
	}

	public void setDbCache(Integer dbCache) {
		this.dbCache = dbCache;
	}

	public String getExportShare() {
		return exportShare;
	}

	public void setExportShare(String exportShare) {
		this.exportShare = exportShare;
	}

	public Integer getExportHour() {
		return exportHour;
	}

	public void setExportHour(Integer exportHour) {
		this.exportHour = exportHour;
	}

	public Integer getExportRetention() {
		return exportRetention;
	}

	public void setExportRetention(Integer exportRetention) {
		this.exportRetention = exportRetention;
	}

	public Boolean getMultipath() {
		return multipath;
	}

	public void setMultipath(Boolean multipath) {
		this.multipath = multipath;
	}
	
	

}
