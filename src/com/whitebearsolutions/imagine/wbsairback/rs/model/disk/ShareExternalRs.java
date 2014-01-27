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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "shareExternal")
public class ShareExternalRs {

	private String name;
	private String path;
	private String size;
	private String type;
	private String server;
	private String share;
	private Boolean mount;
	
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ShareRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de share a objetos share
	 * @param maps
	 * @return
	 */
	public static List<ShareExternalRs> listMapToObject(List<Map<String, String>> mapShares) throws Exception {
		List<ShareExternalRs> shares = new ArrayList<ShareExternalRs>();
		if (mapShares != null && mapShares.size()>0) {
			for (Map<String, String> mapShare : mapShares) {
				ShareExternalRs share = mapToObject(mapShare);
				shares.add(share);
			}
		}
		return shares;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static ShareExternalRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( ShareExternalRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			ShareExternalRs o = (ShareExternalRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<shareExternal>"), xml.indexOf("</shareExternal>")+"</shareExternal>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	
	/**
	 * Convierte un mapa de valores de un  a un objeto  
	 * @param map
	 * @return
	 */
	public static ShareExternalRs mapToObject(Map<String, String> mapShare) throws Exception {
		ShareExternalRs share = new ShareExternalRs();
		
		if (mapShare.get("mount") != null && !mapShare.get("mount").isEmpty())
			share.setMount(Boolean.valueOf(mapShare.get("mount")));
		else
			share.setMount(false);
		if (mapShare.get("name") != null && !mapShare.get("name").isEmpty())
			share.setName(mapShare.get("name"));
		if (mapShare.get("path") != null && !mapShare.get("path").isEmpty())
			share.setPath(mapShare.get("path"));
		if (mapShare.get("server") != null && !mapShare.get("server").isEmpty())
			share.setServer(mapShare.get("server"));
		if (mapShare.get("share") != null && !mapShare.get("share").isEmpty())
			share.setShare(mapShare.get("share"));
		if (mapShare.get("size") != null && !mapShare.get("size").isEmpty())
			share.setSize(mapShare.get("size"));
		if (mapShare.get("type") != null && !mapShare.get("type").isEmpty())
			share.setType(mapShare.get("type"));
		return share;
	}


	// ####### GETTERS Y SETTERS #################################
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public String getSize() {
		return size;
	}


	public void setSize(String size) {
		this.size = size;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getServer() {
		return server;
	}


	public void setServer(String server) {
		this.server = server;
	}


	public String getShare() {
		return share;
	}


	public void setShare(String share) {
		this.share = share;
	}


	public Boolean getMount() {
		return mount;
	}


	public void setMount(Boolean mount) {
		this.mount = mount;
	}
	
}
