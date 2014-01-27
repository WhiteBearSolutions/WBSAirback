package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
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

import com.whitebearsolutions.imagine.wbsairback.backup.FileSetManager;

@XmlRootElement(name="fileset")
public class FilesetRs {

	private String name;
	private Boolean local;
	private Boolean ndmp;
	private String include;
	private String exclude;
	private Boolean md5;
	private String compression;
	private String extension;
	private Boolean acl;
	private Boolean multiplefs;
	private Boolean vss;
	private List<FilesetPluginRs> plugins;
	private List<FilesetLocalVolumeRs> volumes;
	private String ndmpAddress;
	private Integer ndmpPort;
	private String ndmpAuth;
	private String ndmpUser;
	private String ndmpPassword;
	private String ndmpType;
	private List<FilesetNdmpVolumeRs> ndmpVolumes;
	
	public FilesetRs(){}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( FilesetRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	
	/**
	 * Convierte una lista de mapas de filesets a objetos filesets
	 * @param mapClients
	 * @return
	 */
	public static List<FilesetRs> listMapToObject(List<Map<String, String>> mapFilesets) {
		List<FilesetRs> filesets = new ArrayList<FilesetRs>();
		if (mapFilesets != null && mapFilesets.size()>0) {
			for (Map<String, String> mapFileset : mapFilesets) {
				FilesetRs j = mapToObject(mapFileset);
				filesets.add(j);
			}
		}
		return filesets;
	}
	
	public static FilesetRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "fileset";
				JAXBContext jc = JAXBContext.newInstance( FilesetRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					FilesetRs o = (FilesetRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<FilesetRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "fileset";
			List<FilesetRs> listObjects = new ArrayList<FilesetRs>();
			
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
	 * Transforma un mapa de valores de un fileset al objeto fileset
	 * @param mapFileset
	 * @return
	 */
	public static FilesetRs mapToObject(Map<String, String> mapFileset) {
		FilesetRs fileset = new FilesetRs();
		if (mapFileset.get("acl") != null && !mapFileset.get("acl").equals("")) {
			if (mapFileset.get("acl").equals("yes"))
				fileset.setAcl(true);
			else
				fileset.setAcl(false);
		}
		if (mapFileset.get("compression") != null && !mapFileset.get("compression").equals("")) {
			fileset.setCompression(mapFileset.get("compression"));
		}
		if (mapFileset.get("exclude") != null && !mapFileset.get("exclude").equals("")) {
			fileset.setExclude(mapFileset.get("exclude"));
		}
		if (mapFileset.get("extension") != null && !mapFileset.get("extension").equals("")) {
			fileset.setExtension(mapFileset.get("extension"));
		}
		if (mapFileset.get("include") != null && !mapFileset.get("include").equals("")) {
			fileset.setInclude(mapFileset.get("include"));
		}
		if (mapFileset.get("local") != null && !mapFileset.get("local").equals("")) {
			if (mapFileset.get("local").equals("yes"))
				fileset.setLocal(true);
			else
				fileset.setLocal(false);
		}
		if (mapFileset.get("md5") != null && !mapFileset.get("md5").equals("")) {
			if (mapFileset.get("md5").equals("yes"))
				fileset.setMd5(true);
			else
				fileset.setMd5(false);
		}
		if (mapFileset.get("multiplefs") != null && !mapFileset.get("multiplefs").equals("")) {
			if (mapFileset.get("multiplefs").equals("yes"))
				fileset.setMultiplefs(true);
			else
				fileset.setMultiplefs(false);
		}
		if (mapFileset.get("name") != null && !mapFileset.get("name").equals("")) {
			fileset.setName(mapFileset.get("name"));
		}
		if (mapFileset.get("ndmp") != null && !mapFileset.get("ndmp").equals("")) {
			if (mapFileset.get("ndmp").equals("yes"))
				fileset.setNdmp(true);
			else
				fileset.setNdmp(false);
		}
		if (mapFileset.get("ndmp.address") != null && !mapFileset.get("ndmp.address").equals("")) {
			fileset.setNdmpAddress(mapFileset.get("ndmp.address"));
		}
		if (mapFileset.get("ndmp.auth") != null && !mapFileset.get("ndmp.auth").equals("")) {
			fileset.setNdmpAuth(mapFileset.get("ndmp.auth"));
		}
		if (mapFileset.get("ndmp.password") != null && !mapFileset.get("ndmp.password").equals("")) {
			fileset.setNdmpPassword(mapFileset.get("ndmp.password"));
		}
		if (mapFileset.get("ndmp.port") != null && !mapFileset.get("ndmp.port").equals("")) {
			fileset.setNdmpPort(Integer.parseInt(mapFileset.get("ndmp.port")));
		}
		if (mapFileset.get("ndmp.type") != null && !mapFileset.get("ndmp.type").equals("")) {
			fileset.setNdmpType(mapFileset.get("ndmp.type"));
		}
		if (mapFileset.get("ndmpUser") != null && !mapFileset.get("ndmp.user").equals("")) {
			fileset.setNdmpUser(mapFileset.get("ndmp.user"));
		}
		try {
			Map<String, String> volumes = FileSetManager.getNDMPFilesetVolumes(mapFileset.get("name"));
			if (volumes != null && volumes.size()>0)
				fileset.setNdmpVolumes(FilesetNdmpVolumeRs.listMapToObject(volumes));
		} catch (Exception ex){}
		
		try {
			List<String> plugins = FileSetManager.getFilesetPlugins(mapFileset.get("name"));
			if (plugins != null && plugins.size()>0) {
				List<FilesetPluginRs> listp = new ArrayList<FilesetPluginRs>();
				for (String plug : plugins) {
					FilesetPluginRs p = new FilesetPluginRs();
					p.setName(plug);
					listp.add(p);
				}
				fileset.setPlugins(listp);
			}
		} catch (Exception ex) {}
		
		if (mapFileset.get("vss") != null && !mapFileset.get("vss").equals("")) {
			if (mapFileset.get("vss").equals("yes"))
				fileset.setVss(true);
			else
				fileset.setVss(false);
		} else {
			fileset.setVss(false);
		}
		return fileset;
	}

	// ################ GETTERS AND SETTERS ####################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getLocal() {
		return local;
	}

	public void setLocal(Boolean local) {
		this.local = local;
	}

	public Boolean getNdmp() {
		return ndmp;
	}

	public void setNdmp(Boolean ndmp) {
		this.ndmp = ndmp;
	}

	public String getInclude() {
		return include;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	public String getExclude() {
		return exclude;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	public Boolean getMd5() {
		return md5;
	}

	public void setMd5(Boolean md5) {
		this.md5 = md5;
	}

	public String getCompression() {
		return compression;
	}

	public void setCompression(String compression) {
		this.compression = compression;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public Boolean getAcl() {
		return acl;
	}

	public void setAcl(Boolean acl) {
		this.acl = acl;
	}

	public Boolean getMultiplefs() {
		return multiplefs;
	}

	public void setMultiplefs(Boolean multiplefs) {
		this.multiplefs = multiplefs;
	}

	public Boolean getVss() {
		return vss;
	}

	public void setVss(Boolean vss) {
		this.vss = vss;
	}

	@XmlElementWrapper(name="plugins")
	@XmlElementRef()
	public List<FilesetPluginRs> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<FilesetPluginRs> plugins) {
		this.plugins = plugins;
	}

	public String getNdmpAddress() {
		return ndmpAddress;
	}

	public void setNdmpAddress(String ndmpAddress) {
		this.ndmpAddress = ndmpAddress;
	}

	public Integer getNdmpPort() {
		return ndmpPort;
	}

	public void setNdmpPort(Integer ndmpPort) {
		this.ndmpPort = ndmpPort;
	}

	public String getNdmpAuth() {
		return ndmpAuth;
	}

	public void setNdmpAuth(String ndmpAuth) {
		this.ndmpAuth = ndmpAuth;
	}

	public String getNdmpUser() {
		return ndmpUser;
	}

	public void setNdmpUser(String ndmpUser) {
		this.ndmpUser = ndmpUser;
	}

	public String getNdmpPassword() {
		return ndmpPassword;
	}

	public void setNdmpPassword(String ndmpPassword) {
		this.ndmpPassword = ndmpPassword;
	}

	public String getNdmpType() {
		return ndmpType;
	}

	public void setNdmpType(String ndmpType) {
		this.ndmpType = ndmpType;
	}

	@XmlElementWrapper(name="volumesNdmp")
	@XmlElementRef()
	public List<FilesetNdmpVolumeRs> getNdmpVolumes() {
		return ndmpVolumes;
	}

	public void setNdmpVolumes(List<FilesetNdmpVolumeRs> ndmpVolumes) {
		this.ndmpVolumes = ndmpVolumes;
	}

	public void setVolumes(List<FilesetLocalVolumeRs> volumes) {
		this.volumes = volumes;
	}

	@XmlElementWrapper(name="volumes")
	@XmlElementRef()
	public List<FilesetLocalVolumeRs> getVolumes() {
		return volumes;
	}
	
}
