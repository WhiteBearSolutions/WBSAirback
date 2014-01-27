package com.whitebearsolutions.imagine.wbsairback.rs.model.advanced;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobScheduledRs;

@XmlRootElement(name = "groupStep")
public class GroupStepRs {

	private String name;
	private Integer order;
	private String type;
	private String storageInventory;
	private String client;
	private List<VariableValueRs> variables;
	private JobScheduledRs job;

	public GroupStepRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( GroupStepRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	public static GroupStepRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "groupStep";
				JAXBContext jc = JAXBContext.newInstance( GroupStepRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					GroupStepRs o = (GroupStepRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<GroupStepRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "groupStep";
			List<GroupStepRs> listObjects = new ArrayList<GroupStepRs>();
			
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
	
	public VariableValueRs getVar(String nameVar) throws Exception {
		if (this.variables != null && nameVar != null && !nameVar.isEmpty() && !this.variables.isEmpty()) {
			for (VariableValueRs var : this.variables) {
				if (var.getName() != null && var.getName().equals(nameVar))
					return var;
			}
		}
		return null;
	}
	
	// GETTERS AND SETTERS

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStorageInventory() {
		return storageInventory;
	}

	public void setStorageInventory(String storageInventory) {
		this.storageInventory = storageInventory;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@XmlElementWrapper(name="variables")
	@XmlElementRef()
	public List<VariableValueRs> getVariables() {
		return variables;
	}

	public void setVariables(List<VariableValueRs> variables) {
		this.variables = variables;
	}

	public JobScheduledRs getJob() {
		return job;
	}

	public void setJob(JobScheduledRs job) {
		this.job = job;
	}
}
