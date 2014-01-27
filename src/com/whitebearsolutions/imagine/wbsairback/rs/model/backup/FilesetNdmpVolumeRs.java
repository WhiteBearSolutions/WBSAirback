package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="volumeNdmp")
public class FilesetNdmpVolumeRs {

	
	private String volume;
	private String file;
	
	public FilesetNdmpVolumeRs() {}

	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( FilesetNdmpVolumeRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	
	/**
	 * Convierte un listado de mapas de ndmpvolumes en objetos
	 * @param mapVolumes
	 * @return
	 */
	public static List<FilesetNdmpVolumeRs> listMapToObject(Map<String, String> mapVolumes) {
		List<FilesetNdmpVolumeRs> volumes = new ArrayList<FilesetNdmpVolumeRs>();
		for (String vol : mapVolumes.keySet()) {
			FilesetNdmpVolumeRs filesetvol = new FilesetNdmpVolumeRs();
			filesetvol.setVolume(vol);
			filesetvol.setFile(mapVolumes.get(vol));
			volumes.add(filesetvol);
		}
		return volumes;
	}
	
	
	// ###################### 	GETTERS AND SETTERS 	#################################3
	
	@XmlElement(required=true)
	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	@XmlElement(required=true)
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	
}
