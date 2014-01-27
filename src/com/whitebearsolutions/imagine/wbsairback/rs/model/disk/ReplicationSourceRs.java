package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "replicationSource")
public class ReplicationSourceRs {

	private String vg;
	private String lv;
	private String destinationLv;
	private String destinationVg;
	private String address;
	private String localAddress;
	private String password;
	private Boolean filesystem;
	private Boolean delete;
	private Boolean checksum;
	private Boolean append;
	private Boolean compress;
	private Boolean delta;
	private Integer mbs;
	
	public ReplicationSourceRs(){}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ReplicationSourceRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de replicationSource a objetos replicationSource
	 * @param maps
	 * @return
	 */
	public static List<ReplicationSourceRs> listMapToObject(List<Map<String, String>> maps) throws Exception {
		List<ReplicationSourceRs> replicationSources = new ArrayList<ReplicationSourceRs>();
		if (maps != null && maps.size()>0) {
			for (Map<String, String> map : maps) {
				ReplicationSourceRs replicationSource = mapToObject(map);
				replicationSources.add(replicationSource);
			}
		}
		return replicationSources;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static ReplicationSourceRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( ReplicationSourceRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			ReplicationSourceRs o = (ReplicationSourceRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<replicationSource>"), xml.indexOf("</replicationSource>")+"</replicationSource>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	public static List<ReplicationSourceRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "replicationSource";
			List<ReplicationSourceRs> listObjects = new ArrayList<ReplicationSourceRs>();
			
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
	 * Convierte un mapa de valores a un objeto  
	 * @param map
	 * @return
	 */
	public static ReplicationSourceRs mapToObject(Map<String, String> map) throws Exception {
		ReplicationSourceRs replicationSource = new ReplicationSourceRs();
		
		if (map.get("address") != null && !map.get("address").isEmpty())
			replicationSource.setAddress(map.get("address"));
		if (map.get("delete") != null && !map.get("delete").isEmpty() && map.get("delete").equals("yes"))
			replicationSource.setDelete(true);
		else
			replicationSource.setDelete(false);
		if (map.get("checksum") != null && !map.get("checksum").isEmpty() && map.get("checksum").equals("yes"))
			replicationSource.setChecksum(true);
		else
			replicationSource.setChecksum(false);
		if (map.get("append") != null && !map.get("append").isEmpty() && map.get("append").equals("yes"))
			replicationSource.setAppend(true);
		else
			replicationSource.setAppend(false);
		if (map.get("compress") != null && !map.get("compress").isEmpty() && map.get("compress").equals("yes"))
			replicationSource.setCompress(true);
		else
			replicationSource.setCompress(false);
		if (map.get("delta") != null && !map.get("delta").isEmpty() && map.get("delta").equals("yes"))
			replicationSource.setDelta(true);
		else
			replicationSource.setDelta(false);
		if (map.get("dest-lv") != null && !map.get("dest-lv").isEmpty())
			replicationSource.setDestinationLv(map.get("dest-lv"));
		if (map.get("dest-vg") != null && !map.get("dest-vg").isEmpty())
			replicationSource.setDestinationVg(map.get("dest-vg"));
		if (map.get("filesystem") != null && !map.get("filesystem").isEmpty() && map.get("filesystem").equals("yes"))
			replicationSource.setFilesystem(true);
		else
			replicationSource.setFilesystem(false);
		if (map.get("local-address") != null && !map.get("local-address").isEmpty())
			replicationSource.setLocalAddress(map.get("local-address"));
		if (map.get("lv") != null && !map.get("lv").isEmpty())
			replicationSource.setLv(map.get("lv"));
		if (map.get("mbs") != null && !map.get("mbs").isEmpty())
			replicationSource.setMbs(Integer.parseInt(map.get("mbs")));
		else
			replicationSource.setMbs(0);
		if (map.get("password") != null && !map.get("password").isEmpty())
			replicationSource.setPassword(map.get("password"));
		if (map.get("vg") != null && !map.get("vg").isEmpty())
			replicationSource.setVg(map.get("vg"));
		
		return replicationSource;
	}

	
	// ####### GETTERS Y SETTERS #################################

	
	@XmlElement(required=true)
	public String getVg() {
		return vg;
	}

	public void setVg(String vg) {
		this.vg = vg;
	}

	@XmlElement(required=true)
	public String getLv() {
		return lv;
	}

	public void setLv(String lv) {
		this.lv = lv;
	}

	@XmlElement(required=true)
	public String getDestinationLv() {
		return destinationLv;
	}

	public void setDestinationLv(String destinationLv) {
		this.destinationLv = destinationLv;
	}

	@XmlElement(required=true)
	public String getDestinationVg() {
		return destinationVg;
	}

	public void setDestinationVg(String destinationVg) {
		this.destinationVg = destinationVg;
	}

	@XmlElement(required=true)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@XmlElement(required=true)
	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

	@XmlElement(required=true)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getFilesystem() {
		return filesystem;
	}

	public void setFilesystem(Boolean filesystem) {
		this.filesystem = filesystem;
	}

	public Boolean getDelete() {
		return delete;
	}

	public void setDelete(Boolean delete) {
		this.delete = delete;
	}

	public Integer getMbs() {
		return mbs;
	}

	public void setMbs(Integer mbs) {
		this.mbs = mbs;
	}

	public Boolean getChecksum() {
		return checksum;
	}

	public void setChecksum(Boolean checksum) {
		this.checksum = checksum;
	}

	public Boolean getAppend() {
		return append;
	}

	public void setAppend(Boolean append) {
		this.append = append;
	}

	public Boolean getCompress() {
		return compress;
	}

	public void setCompress(Boolean compress) {
		this.compress = compress;
	}

	public Boolean getDelta() {
		return delta;
	}

	public void setDelta(Boolean delta) {
		this.delta = delta;
	}
	
}
