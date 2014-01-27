package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="plugin")
public class FilesetPluginRs {

	private String name;
	
	public FilesetPluginRs(){}

	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
