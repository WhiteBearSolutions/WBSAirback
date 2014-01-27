package com.whitebearsolutions.imagine.wbsairback.rs.model.subscription;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "package")
public class PackageRs {
	
	private String application;
	private String currentVersion;
	private String availableVersion;
	
	public PackageRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( PackageRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de agregados a objetos agregado
	 * @param mapPools
	 * @return
	 */
	public static List<PackageRs> listMapToObject(Map<String, List<String>> mapPackages) throws Exception {
		List<PackageRs> packs = new ArrayList<PackageRs>();
		if (mapPackages != null && mapPackages.size()>0) {
			for (String app : mapPackages.keySet()) {
				PackageRs pack= mapToObject(app, mapPackages.get(app));
				packs.add(pack);
			}
		}
		return packs;
	}
	
	public static PackageRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "package";
				JAXBContext jc = JAXBContext.newInstance( PackageRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					PackageRs o = (PackageRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<PackageRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "package";
			List<PackageRs> listObjects = new ArrayList<PackageRs>();
			
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
	 * Convierte un mapa de valores de un pool a un objeto Pool 
	 * @param mapPool
	 * @return
	 */
	public static PackageRs mapToObject(String app, List<String> list) throws Exception {
		PackageRs pack = new PackageRs();
		
		if (app != null && !app.isEmpty())
			pack.setApplication(app);
		
		if (list != null && !list.isEmpty()) {
			pack.setCurrentVersion(list.get(0));
			pack.setAvailableVersion(list.get(1));
		}
			
		return pack;
	}

	
	// GETTERS Y SETTERS
	
	
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getAvailableVersion() {
		return availableVersion;
	}

	public void setAvailableVersion(String availableVersion) {
		this.availableVersion = availableVersion;
	}


	

}
