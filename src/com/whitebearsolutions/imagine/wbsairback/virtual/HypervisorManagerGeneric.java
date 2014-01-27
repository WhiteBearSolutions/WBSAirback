package com.whitebearsolutions.imagine.wbsairback.virtual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HypervisorManagerGeneric extends HypervisorManager {
	
	protected HypervisorManagerGeneric() throws Exception {
		super();
	}
	
	public void addNFSStore(String name, String hostName, String address, String path) throws Exception {
		
	}
	
	public void cloneVirtualMachine(String name, String store) throws Exception {
		
	}
	
	public void downloadVirtualMachineOVF(String name, String local_path) throws Exception {
		
	}
	
	public boolean existsNFSStore(String name, String host, String address, String path) throws Exception {
		return false;
	}
	
	public String getHostForVirtualMachine(String vmName) throws Exception {
		return null;
	}
	
	public List<String> getStoreNames() throws Exception {
		return new ArrayList<String>();
	}
	
	public List<String> getHostsNames() throws Exception {
		return new ArrayList<String>();
	}
	
	public List<Map<String, String>> getStores() throws Exception {
		return new ArrayList<Map<String, String>>();
	}
	
	public List<String> getVirtualMachineNames() throws Exception {
		return new ArrayList<String>();
	}
	
	public List<String> getVirtualMachineNames(String storage) throws Exception {
		return new ArrayList<String>();
	}
	
	public List<Map<String, String>> getVirtualMachines() throws Exception {
		return new ArrayList<Map<String, String>>();
	}
	
	public List<Map<String, String>> getVirtualMachines(String storage) throws Exception {
		return new ArrayList<Map<String, String>>();
	}
	
	public Map<String, String> getVirtualMachine(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		
		return new HashMap<String, String>();
	}
	
	public void makeVirtualMachineSnapshot(String name, String store) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
	}

	public List<String> getDatastoreNames(String host) throws Exception {
		return new ArrayList<String>();
	}

	public Map<String, Map<String, List<String>>> getVirtualTree()
			throws Exception {
		return null;
	}

	public Map<String, List<String>> getVirtualTreeNoMachines()
			throws Exception {
		return null;
	}
}
