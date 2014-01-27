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

@XmlRootElement(name = "cloudDiskStat")
public class CloudDiskStatRs {

	private String device;
	private String stat;
	
	public CloudDiskStatRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudDiskStatRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de cloudDiskStat a objetos cloudDiskStat
	 * @param maps
	 * @return
	 */
	public static List<CloudDiskStatRs> listMapToObject(Map<String, List<String>> mapCloudDiskStats) throws Exception {
		List<CloudDiskStatRs> cloudDiskStats = new ArrayList<CloudDiskStatRs>();
		if (mapCloudDiskStats != null && mapCloudDiskStats.size()>0) {
			for (String device : mapCloudDiskStats.keySet()) {
				List<String> diskStats = mapCloudDiskStats.get(device);
				for (String stat : diskStats) {
					CloudDiskStatRs cloudDiskStat = new CloudDiskStatRs();
					cloudDiskStat.setDevice(device);
					cloudDiskStat.setStat(stat);
					cloudDiskStats.add(cloudDiskStat);
				}
			}
		}
		return cloudDiskStats;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static CloudDiskStatRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( CloudDiskStatRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			CloudDiskStatRs o = (CloudDiskStatRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<cloudDiskStat>"), xml.indexOf("</cloudDiskStat>")+"</cloudDiskStat>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	
	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}
}
