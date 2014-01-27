package com.whitebearsolutions.imagine.wbsairback.rs.model.backup;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement(name = "hypervisorJob")
public class HypervisorJobRs {
	
	private String name;
	private String hypervisor;
	private String mode;
	private String storage;
	private List<HypervisorDataStoreRs> datastores;
	private List<HypervisorVirtualMachineRs> virtualMachines;
	
	public HypervisorJobRs() {}
	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorJobRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}

	
	/**
	 * Convierte una lista de mapas de hypervisorJob a objetos hypervisorJob
	 * @param maps
	 * @return
	 */
	public static List<HypervisorJobRs> listMapToObject(List<Map<String, Object>> mapHypervisorJobs) throws Exception {
		List<HypervisorJobRs> hypervisorJobs = new ArrayList<HypervisorJobRs>();
		if (mapHypervisorJobs != null && mapHypervisorJobs.size()>0) {
			for (Map<String, Object> mapHypervisorJob : mapHypervisorJobs) {
				HypervisorJobRs hypervisorJob = mapToObject(mapHypervisorJob);
				hypervisorJobs.add(hypervisorJob);
			}
		}
		return hypervisorJobs;
	}
	
	
	/**
	 * Obtiene un objeto a partir de su xml
	 * @param xml
	 * @return
	 */
	public static HypervisorJobRs fromXML(String xml) {
		try {
			JAXBContext jc = JAXBContext.newInstance( HypervisorJobRs.class );
			Unmarshaller um = jc.createUnmarshaller();
			HypervisorJobRs o = (HypervisorJobRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<hypervisorJob>"), xml.indexOf("</hypervisorJob>")+"</hypervisorJob>".length()).toString() ) ) );
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
	public static HypervisorJobRs mapToObject(Map<String, Object> map) throws Exception {
		HypervisorJobRs hypervisorJob = new HypervisorJobRs();
		
		if (map.get("ds") != null) {
			@SuppressWarnings("unchecked")
			List<String> dsNameList = (List<String>)map.get("ds");
			if (!dsNameList.isEmpty()) {
				List<HypervisorDataStoreRs> dsList = new ArrayList<HypervisorDataStoreRs>();
				for (String name : dsNameList) {
					HypervisorDataStoreRs ds = new HypervisorDataStoreRs();
					ds.setName(name);
					dsList.add(ds);
				}
				hypervisorJob.setDatastores(dsList);
			}
		}
		if (map.get("hypervisor") != null && !((String)map.get("hypervisor")).isEmpty())
			hypervisorJob.setHypervisor((String)map.get("hypervisor"));
		if (map.get("mode") != null && !((String)map.get("mode")).isEmpty())
			hypervisorJob.setMode((String)map.get("mode"));
		if (map.get("name") != null && !((String)map.get("name")).isEmpty())
			hypervisorJob.setName((String)map.get("name"));
		if (map.get("storage") != null && !((String)map.get("storage")).isEmpty())
			hypervisorJob.setStorage((String)map.get("storage"));
		if (map.get("vm") != null) {
			@SuppressWarnings("unchecked")
			List<String> dsNameList = (List<String>)map.get("vm");
			if (!dsNameList.isEmpty()) {
				List<HypervisorVirtualMachineRs> vmList = new ArrayList<HypervisorVirtualMachineRs>();
				for (String name : dsNameList) {
					HypervisorVirtualMachineRs vm = new HypervisorVirtualMachineRs();
					vm.setName(name);
					vmList.add(vm);
				}
				hypervisorJob.setVirtualMachines(vmList);
			}
		}
		
		return hypervisorJob;
	}

	
	// ####### GETTERS Y SETTERS #################################
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHypervisor() {
		return hypervisor;
	}

	public void setHypervisor(String hypervisor) {
		this.hypervisor = hypervisor;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	@XmlElementWrapper(name="datastores")
	@XmlElementRef()
	public List<HypervisorDataStoreRs> getDatastores() {
		return datastores;
	}

	public void setDatastores(List<HypervisorDataStoreRs> datastores) {
		this.datastores = datastores;
	}

	@XmlElementWrapper(name="virtualMachines")
	@XmlElementRef()
	public List<HypervisorVirtualMachineRs> getVirtualMachines() {
		return virtualMachines;
	}

	public void setVirtualMachines(List<HypervisorVirtualMachineRs> virtualMachines) {
		this.virtualMachines = virtualMachines;
	}
}
