package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="volume")
public class FilesetLocalVolumeRs {

	private String name;
	
	public FilesetLocalVolumeRs(){}

	@XmlElement(required=true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
