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

@XmlRootElement(name = "cloudAccount")
public class CloudAccountRs {
	
	private String name;
	private String id;
	private String key;
	private String type;
	private String server;
	
	public CloudAccountRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudAccountRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de cloudAccount a objetos cloudAccount
	 * @param maps
	 * @return
	 */
	public static List<CloudAccountRs> listMapToObject(List<Map<String, String>> mapCloudAccounts) throws Exception {
		List<CloudAccountRs> cloudAccounts = new ArrayList<CloudAccountRs>();
		if (mapCloudAccounts != null && mapCloudAccounts.size()>0) {
			for (Map<String, String> mapCloudAccount : mapCloudAccounts) {
				CloudAccountRs cloudAccount = mapToObject(mapCloudAccount);
				cloudAccounts.add(cloudAccount);
			}
		}
		return cloudAccounts;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static CloudAccountRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudAccountRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			CloudAccountRs o = (CloudAccountRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<cloudAccount>"), xml.indexOf("</cloudAccount>")+"</cloudAccount>".length()).toString() ) ) );
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
	public static CloudAccountRs mapToObject(Map<String, String> map) throws Exception {
		CloudAccountRs cloudAccount = new CloudAccountRs();

		if (map.get("userid") != null && !map.get("userid").isEmpty())
			cloudAccount.setId(map.get("userid"));
		if (map.get("userkey") != null && !map.get("userkey").isEmpty())
			cloudAccount.setKey(map.get("userkey"));
		if (map.get("name") != null && !map.get("name").isEmpty())
			cloudAccount.setName(map.get("name"));
		if (map.get("atmosserver") != null && !map.get("atmosserver").isEmpty())
			cloudAccount.setServer(map.get("atmosserver"));
		if (map.get("type") != null && !map.get("type").isEmpty())
			cloudAccount.setType(map.get("type"));
		
		return cloudAccount;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(required=true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement(required=true)
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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


}
