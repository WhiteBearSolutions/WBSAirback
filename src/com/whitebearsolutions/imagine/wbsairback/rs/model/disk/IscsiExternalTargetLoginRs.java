package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "iscsiExternalTargetLogin")
public class IscsiExternalTargetLoginRs {
	
	private String address;
	private String method;
	private String user;
	private String password;
	private String port;
	private List<IscsiTargetSimpleRs> targets;
	
	public IscsiExternalTargetLoginRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXml() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiExternalTargetLoginRs.class );
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
	public static IscsiExternalTargetLoginRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiExternalTargetLoginRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			IscsiExternalTargetLoginRs o = (IscsiExternalTargetLoginRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<iscsiExternalTargetLogin>"), xml.indexOf("</iscsiExternalTargetLogin>")+"</iscsiExternalTargetLogin>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
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

	@XmlElementWrapper(name="targets")
	@XmlElementRef()
	public List<IscsiTargetSimpleRs> getTargets() {
		return targets;
	}

	public void setTargets(List<IscsiTargetSimpleRs> targets) {
		this.targets = targets;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	

}
