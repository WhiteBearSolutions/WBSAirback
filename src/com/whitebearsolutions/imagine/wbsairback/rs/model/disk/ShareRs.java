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

@XmlRootElement(name = "share")
public class ShareRs {
	
	private String protocol;
	private String path;
	private String vg;
	private String lv;
	private String name;
	private String fstype;
	private Boolean squash;
	private Boolean async;
	private Boolean anonymous;
	private String address;
	private Boolean recycle;
	
	public ShareRs() {}
	
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
	public static List<ShareRs> listMapToObject(List<Map<String, String>> mapShares) throws Exception {
		List<ShareRs> shares = new ArrayList<ShareRs>();
		if (mapShares != null && mapShares.size()>0) {
			for (Map<String, String> mapShare : mapShares) {
				ShareRs share = mapToObject(mapShare);
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
	public static ShareRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( ShareRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			ShareRs o = (ShareRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<share>"), xml.indexOf("</share>")+"</share>".length()).toString() ) ) );
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
	public static ShareRs mapToObject(Map<String, String> mapShare) throws Exception {
		ShareRs share = new ShareRs();
		
		if (mapShare.get("address") != null && !mapShare.get("address").isEmpty())
			share.setAddress(mapShare.get("address"));
		if (mapShare.get("anonymous") != null && !mapShare.get("anonymous").isEmpty())
			share.setAnonymous(Boolean.valueOf(mapShare.get("anonymous")));
		else
			share.setAnonymous(false);
		if (mapShare.get("async") != null && !mapShare.get("async").isEmpty())
			share.setAsync(Boolean.valueOf(mapShare.get("async")));
		else
			share.setAsync(false);
		if (mapShare.get("fstype") != null && !mapShare.get("fstype").isEmpty())
			share.setFstype(mapShare.get("fstype"));
		if (mapShare.get("lv") != null && !mapShare.get("lv").isEmpty())
			share.setLv(mapShare.get("lv"));
		else if (mapShare.get("volume") != null && !mapShare.get("volume").isEmpty())
			share.setLv(mapShare.get("volume"));
		if (mapShare.get("name") != null && !mapShare.get("name").isEmpty())
			share.setName(mapShare.get("name"));
		if (mapShare.get("path") != null && !mapShare.get("path").isEmpty())
			share.setPath(mapShare.get("path"));
		if (mapShare.get("protocol") != null && !mapShare.get("protocol").isEmpty())
			share.setProtocol(mapShare.get("protocol"));
		if (mapShare.get("recycle") != null && !mapShare.get("recycle").isEmpty())
			share.setRecycle(Boolean.valueOf(mapShare.get("recycle")));
		else
			share.setRecycle(false);
		if (mapShare.get("squash") != null && !mapShare.get("squash").isEmpty())
			share.setSquash(Boolean.valueOf(mapShare.get("squash")));
		else
			share.setSquash(false);
		if (mapShare.get("vg") != null && !mapShare.get("vg").isEmpty())
			share.setVg(mapShare.get("vg"));
		else if (mapShare.get("group") != null && !mapShare.get("group").isEmpty())
			share.setVg(mapShare.get("group"));
		return share;
	}

	
	// ####### GETTERS Y SETTERS #################################

	@XmlElement(required=true)
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

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

	public String getFstype() {
		return fstype;
	}

	public void setFstype(String fstype) {
		this.fstype = fstype;
	}

	public Boolean getSquash() {
		return squash;
	}

	public void setSquash(Boolean squash) {
		this.squash = squash;
	}

	public Boolean getAsync() {
		return async;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}

	public Boolean getAnonymous() {
		return anonymous;
	}

	public void setAnonymous(Boolean anonymous) {
		this.anonymous = anonymous;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Boolean getRecycle() {
		return recycle;
	}

	public void setRecycle(Boolean recycle) {
		this.recycle = recycle;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
