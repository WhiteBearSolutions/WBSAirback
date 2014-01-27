package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

@XmlRootElement(name="client")
public class ClientRs {

	private Integer id;
	private String name;
	private String uname;
	private String starttime;
	private String status;
	private String alert;
	private String address;
	private String dnsname;
	private Integer port;
	private Integer fdport;
	private String catalog;
	private String password;
	private String os;
	private String charset;
	private Integer fileretention;
	private String fileretentionPeriod;
	private Integer jobretention;
	private String jobretentionPeriod;
	private Boolean autoprune;
	private Integer maximumconcurrentjobs;
	private List<CategoryRs> categories;
	
	
	/**
	 * Constructor vacío necesario para JAX
	 */
	public ClientRs() {}
	
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( ClientRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {
			return "";
		}
		return xml;
	}
	
	public static ClientRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "client";
				JAXBContext jc = JAXBContext.newInstance( ClientRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					ClientRs o = (ClientRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<ClientRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "client";
			List<ClientRs> listObjects = new ArrayList<ClientRs>();
			
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
	 * Convierte una lista de mapas de cliente a objetos clientrs
	 * @param mapClients
	 * @return
	 */
	public static List<ClientRs> listMapToObject(List<Map<String, String>> mapClients) {
		List<ClientRs> jobs = new ArrayList<ClientRs>();
		if (mapClients != null && mapClients.size()>0) {
			for (Map<String, String> mapClient : mapClients) {
				ClientRs j = mapToObject(mapClient);
				jobs.add(j);
			}
		}
		return jobs;
	}
	
	
	/**
	 * Convierte un mapa de cliente a un objeto clientrs
	 * @param mapClient
	 * @return
	 */
	public static ClientRs mapToObject(Map<String, String> mapClient) {
		ClientRs client = new ClientRs();
		
		if (mapClient.get("address") != null && !mapClient.get("address").equals(""))
			client.setAddress(mapClient.get("address"));
		if (mapClient.get("dnsname") != null && !mapClient.get("dnsname").equals(""))
			client.setAddress(mapClient.get("dnsname"));
		if (mapClient.get("alert") != null && !mapClient.get("alert").equals(""))
			client.setAlert(mapClient.get("alert"));
		if (mapClient.get("autoprune") != null && mapClient.get("autoprune").equals("yes"))
			client.setAutoprune(true);
		else
			client.setAutoprune(false);
		if (mapClient.get("catalog") != null && !mapClient.get("catalog").equals(""))
			client.setCatalog(mapClient.get("catalog"));
		if (mapClient.get("category") != null && !mapClient.get("category").equals("")) {
			List<CategoryRs> categories = new ArrayList<CategoryRs>();
			List<String> cats = Arrays.asList(mapClient.get("category").split(","));
			for (String cat : cats) {
				CategoryRs c = new CategoryRs();
				c.setName(cat);
				categories.add(c);
			}
			client.setCategories(categories);
		}
		if (mapClient.get("charset") != null && !mapClient.get("charset").equals(""))
			client.setCharset(mapClient.get("charset"));
		if (mapClient.get("fdport") != null && !mapClient.get("fdport").equals(""))
			client.setFdport(Integer.parseInt(mapClient.get("fdport")));
		if (mapClient.get("fileretention") != null && !mapClient.get("fileretention").equals(""))
			client.setFileretention(Integer.parseInt(mapClient.get("fileretention")));
		if (mapClient.get("fileretention-period") != null && !mapClient.get("fileretention-period").equals(""))
			client.setFileretentionPeriod(mapClient.get("fileretention-period"));
		if (mapClient.get("id") != null && !mapClient.get("id").equals("")) {
			try {
				client.setId(Integer.parseInt(mapClient.get("id")));
			} catch (Exception ex) {
				client.setId(0);
			}
		}
		if (mapClient.get("jobretention") != null && !mapClient.get("jobretention").equals(""))
			client.setJobretention(Integer.parseInt(mapClient.get("jobretention")));
		if (mapClient.get("jobretention-period") != null && !mapClient.get("jobretention-period").equals(""))
			client.setJobretentionPeriod(mapClient.get("jobretention-period"));
		if (mapClient.get("maximumconcurrentcobs") != null && !mapClient.get("maximumconcurrentcobs").equals(""))
			client.setMaximumconcurrentjobs(Integer.parseInt(mapClient.get("maximumconcurrentcobs")));
		if (mapClient.get("name") != null && !mapClient.get("name").equals(""))
			client.setName(mapClient.get("name"));
		if (mapClient.get("os") != null && !mapClient.get("os").equals(""))
			client.setOs(mapClient.get("os"));
		if (mapClient.get("passsword") != null && !mapClient.get("passsword").equals(""))
			client.setPassword(mapClient.get("passsword"));
		if (mapClient.get("fdport") != null && !mapClient.get("fdport").equals(""))
			client.setPort(Integer.parseInt(mapClient.get("fdport")));
		if (mapClient.get("starttime") != null && !mapClient.get("starttime").equals(""))
			client.setStarttime(mapClient.get("starttime"));
		if (mapClient.get("status") != null && !mapClient.get("status").equals(""))
			client.setStatus(mapClient.get("status"));
		if (mapClient.get("uname") != null && !mapClient.get("uname").equals(""))
			client.setUname(mapClient.get("uname"));
		
		return client;
	}

	
	// ################# GETTERS Y SETTERS #################################
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = alert;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getFdport() {
		return fdport;
	}

	public void setFdport(Integer fdport) {
		this.fdport = fdport;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	@XmlElement(required=true)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlElement(required=true)
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Integer getFileretention() {
		return fileretention;
	}

	public void setFileretention(Integer fileretention) {
		this.fileretention = fileretention;
	}

	public String getFileretentionPeriod() {
		return fileretentionPeriod;
	}

	public void setFileretentionPeriod(String fileretentionPeriod) {
		this.fileretentionPeriod = fileretentionPeriod;
	}

	public Integer getJobretention() {
		return jobretention;
	}

	public void setJobretention(Integer jobretention) {
		this.jobretention = jobretention;
	}

	public String getJobretentionPeriod() {
		return jobretentionPeriod;
	}

	public void setJobretentionPeriod(String jobretentionPeriod) {
		this.jobretentionPeriod = jobretentionPeriod;
	}

	public Boolean getAutoprune() {
		return autoprune;
	}

	public void setAutoprune(Boolean autoprune) {
		this.autoprune = autoprune;
	}

	public Integer getMaximumconcurrentjobs() {
		return maximumconcurrentjobs;
	}

	public void setMaximumconcurrentjobs(Integer maximumconcurrentjobs) {
		this.maximumconcurrentjobs = maximumconcurrentjobs;
	}


	public String getDnsname() {
		return dnsname;
	}


	public void setDnsname(String dnsname) {
		this.dnsname = dnsname;
	}

	@XmlElementWrapper(name="categories")
	@XmlElementRef()
	public List<CategoryRs> getCategories() {
		return categories;
	}
	
	public List<String> getCategoriesList() {
		List<String> listCat = new ArrayList<String>();
		if (categories != null && !categories.isEmpty()) {
			for (CategoryRs cat : categories)
				listCat.add(cat.getName());
		}
		return listCat;
	}


	public void setCategories(List<CategoryRs> categories) {
		this.categories = categories;
	}
	
}
