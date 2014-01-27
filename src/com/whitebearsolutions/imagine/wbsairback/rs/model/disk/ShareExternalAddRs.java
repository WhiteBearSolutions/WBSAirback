package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "shareExternalAdd")
public class ShareExternalAddRs {
	
	private String server;
	private String share;
	private String user;
	private String password;
	private String domain;
	private String type;
	
	public ShareExternalAddRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ShareExternalAddRs.class );
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
	public static ShareExternalAddRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( ShareExternalAddRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			ShareExternalAddRs o = (ShareExternalAddRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<shareExternalAdd>"), xml.indexOf("</shareExternalAdd>")+"</shareExternalAdd>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}

	@XmlElement(required=true)
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@XmlElement(required=true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(required=true)
	public String getShare() {
		return share;
	}

	public void setShare(String share) {
		this.share = share;
	}

}
