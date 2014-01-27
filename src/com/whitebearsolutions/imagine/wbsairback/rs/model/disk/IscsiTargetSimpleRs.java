package com.whitebearsolutions.imagine.wbsairback.rs.model.disk;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "iscsiTargetSimple")
public class IscsiTargetSimpleRs {

	private String target;
	
	public IscsiTargetSimpleRs() {}

	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiTargetSimpleRs.class );
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
	public static IscsiTargetSimpleRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( IscsiTargetSimpleRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			IscsiTargetSimpleRs o = (IscsiTargetSimpleRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<iscsiTargetSimple>"), xml.indexOf("</iscsiTargetSimple>")+"</iscsiTargetSimple>".length()).toString() ) ) );
			return o;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static List<IscsiTargetSimpleRs> xmlToList(String xml, String idList) {
		try {
			String nameEntity = "iscsiTargetSimple";
			List<IscsiTargetSimpleRs> listObjects = new ArrayList<IscsiTargetSimpleRs>();
			
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
	 * Convierte una lista de mapas de target a objetos target
	 * @param maps
	 * @return
	 */
	public static List<IscsiTargetSimpleRs> listMapToObject(List<String> mapIscsiTargets) throws Exception {
		List<IscsiTargetSimpleRs> targets = new ArrayList<IscsiTargetSimpleRs>();
		if (mapIscsiTargets != null && mapIscsiTargets.size()>0) {
			for (String s : mapIscsiTargets) {
				IscsiTargetSimpleRs target = new IscsiTargetSimpleRs();
				target.setTarget(s);
				targets.add(target);
			}
		}
		return targets;
	}
	
	// ####### GETTERS Y SETTERS #################################
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}
