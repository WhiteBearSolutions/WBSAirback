package com.whitebearsolutions.imagine.wbsairback.virtual;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Configuration;

public abstract class HypervisorManager {
	public static final int GENERIC = 1;
	public static final int VMWARE_VSPHERE = 2;
	public static final int XEN = 3;
	
	private final static Logger logger = LoggerFactory.getLogger(HypervisorManager.class);
	
	public static HypervisorManager getInstance(int type, String address, String user, String password) throws Exception {
		switch(type) {
			case GENERIC: {
					return new HypervisorManagerGeneric();
				}
			case VMWARE_VSPHERE: {
					return new HypervisorManagerVMware(address, user, password);
				}
			case XEN: {
					return new HypervisorManagerXen(address, user, password);
				}
			default: {
					throw new Exception("invalid hypervisor type");
				}
		}
	}
	
	public static HypervisorManager getInstanceWithRetrys(int type, String address, String user, String password, int retrys) throws Exception {
		int trys = 0;
		HypervisorManager _hm = null;
		boolean success=false;
		while (trys < retrys && !success) {
			try {
				logger.debug("Buscamos conexión con el api para el cliente {}. Usuario: {} pass: {} Intento {} ...", new Object[]{address, user, password, trys});
				_hm = HypervisorManager.getInstance(type, address, user, password);
				success=true;
			} catch (Exception ex) {
				trys++;
				if (trys < retrys) {
					logger.warn("Conexión con el api para el cliente {} FALLO en intento {}. Reintentamos ...", address, trys);
				} else {
					logger.error("No se pudo conectar con {}. Ex: {}", address, ex.getMessage());
					throw new Exception("Could not connect with "+address+". Ex: "+ex.getMessage());
				}
			}
		}
		return _hm;
	}
	
	public static HypervisorManager getInstance(Map<String, String> hypervisor) throws Exception {
		if(hypervisor == null || hypervisor.isEmpty()) {
			return new HypervisorManagerGeneric();
		} else if("vmware".equalsIgnoreCase(hypervisor.get("os"))) {
			return new HypervisorManagerVMware(hypervisor.get("address"), hypervisor.get("user"), hypervisor.get("password"));
		} else if("xen".equalsIgnoreCase(hypervisor.get("os"))) {
			return new HypervisorManagerXen(hypervisor.get("address"), hypervisor.get("user"), hypervisor.get("password"));
		}
		return new HypervisorManagerGeneric();
	}
	
	public static HypervisorManager getInstance(String hypervisor) throws Exception {
		if(hypervisor == null || hypervisor.isEmpty()) {
			return new HypervisorManagerGeneric();
		}
		HypervisorManager _hm = new HypervisorManagerGeneric();
		return getInstance(_hm.getHypervisor(hypervisor));
	}
	
	public abstract void addNFSStore(String name, String hostName, String address, String path) throws Exception;
	
	public abstract void cloneVirtualMachine(String name, String store) throws Exception;
	
	public void deleteHypervisor(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid hypervisor name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryHypervisors() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("hypervisor does not exists");
		}
		
		for(String _job : getHypervisorJobNames(name)) {
			deleteHypervisoJob(_job);
		}
		_f.delete();
	}
	
	public void deleteHypervisoJob(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid hypervisor job name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("hypervisor job does not exists");
		}
		_f.delete();
	}
	
	public abstract void downloadVirtualMachineOVF(String name, String local_path) throws Exception;
	
	public abstract boolean existsNFSStore(String name, String _host, String address, String path) throws Exception;
	
	public abstract String getHostForVirtualMachine(String vmName) throws Exception;
	
	public List<String> getAllHypervisorNames() {
		List<String> hypervisors = new ArrayList<String>();
		File _dir = new File(WBSAirbackConfiguration.getDirectoryHypervisors());
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		String[] _files = _dir.list();
		if(_files != null) {
			for(String clientFile : _files) {
				if(clientFile.endsWith(".xml")) {
					hypervisors.add(clientFile.substring(0, clientFile.length() - 4));
				}
			}
		}
		
		return hypervisors;
	}
	
	public List<Map<String, String>> getAllHypervisors() {
		List<Map<String, String>> hypervisors = new ArrayList<Map<String,String>>();
		File _dir = new File(WBSAirbackConfiguration.getDirectoryHypervisors());
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		String[] _files = _dir.list();
		if(_files != null) {
			for(String clientFile : _files) {
				if(clientFile.endsWith(".xml")) {
					Map<String, String> hypervisor = new HashMap<String, String>();
					try {
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectoryHypervisors() + "/" + clientFile));
						hypervisor.put("name", clientFile.substring(0, clientFile.length() - 4));
						hypervisor.put("os", _c.getProperty("hypervisor.os"));
						hypervisor.put("address", _c.getProperty("hypervisor.address"));
						if(_c.hasProperty("hypervisor.user")) {
							hypervisor.put("user", _c.getProperty("hypervisor.user"));
							hypervisor.put("password", _c.getProperty("hypervisor.password"));
						}
						hypervisors.add(hypervisor);
					} catch(Exception _ex) {}
				}
			}
		}
		
		return hypervisors;
	}
	
	public Map<String, String> getHypervisor(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid hypervisor name");
		}
		
		Map<String, String> hypervisor = new HashMap<String,String>();
		File _f = new File(WBSAirbackConfiguration.getDirectoryHypervisors() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("hypervisor does not exists");
		}
		
		Configuration _c = new Configuration(_f);
		hypervisor.put("name", name);
		hypervisor.put("os", _c.getProperty("hypervisor.os"));
		hypervisor.put("address", _c.getProperty("hypervisor.address"));
		if(_c.hasProperty("hypervisor.user")) {
			hypervisor.put("user", _c.getProperty("hypervisor.user"));
			hypervisor.put("password", _c.getProperty("hypervisor.password"));
		}
		return hypervisor;
	}
	
	public List<String> getAllHypervisorJobNames() {
		List<String> hypervisor_jobs = new ArrayList<String>();
		File _dir = new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs());
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		String[] _files = _dir.list();
		if(_files != null) {
			for(String jobFile : _files) {
				if(jobFile.endsWith(".xml")) {
					hypervisor_jobs.add(jobFile.substring(0, jobFile.length() - 4));
				}
			}
		}
		return hypervisor_jobs;
	}
	
	public List<Map<String, Object>> getAllHypervisorJobs() throws Exception{
		List<Map<String, Object>> hypervisor_jobs = new ArrayList<Map<String, Object>>();
		List<String> hypervisor_job_names = getAllHypervisorNames();
		if (hypervisor_job_names != null && !hypervisor_job_names.isEmpty()) {
			for (String name : hypervisor_job_names)
				hypervisor_jobs.add(getHypervisorJob(name));
		}
		return hypervisor_jobs;
	}
	
	public boolean isVolumeOnHypervisorJob(String vg, String lv) throws Exception {
		List<Map<String, Object>> hypervisor_jobs = getAllHypervisorJobs();
		String storage = vg+"/"+lv;
		if (hypervisor_jobs != null && !hypervisor_jobs.isEmpty()) {
			for (Map<String, Object> job : hypervisor_jobs) {
				if (job.containsKey("storage") && job.get("storage").equals(storage))
					return true;
			}
		}
		return false;
	}
	
	public List<Map<String, Object>> getHypervisorJobs(String name) {
		List<Map<String, Object>> hypervisor_jobs = new ArrayList<Map<String, Object>>();
		File _dir = new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs());
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		String[] _files = _dir.list();
		if(_files != null) {
			for(String jobFile : _files) {
				if(jobFile.endsWith(".xml")) {
					Map<String, Object> hypervisor_job = new HashMap<String, Object>();
					try {
						List<String> _vms = new ArrayList<String>();
						List<String> _dss = new ArrayList<String>();
						Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs() + "/" + jobFile));
						if(_c.checkProperty("job.hypervisor", name)) {
							hypervisor_job.put("name", jobFile.substring(0, jobFile.length() - 4));
							hypervisor_job.put("hypervisor", name);
							hypervisor_job.put("mode", _c.getProperty("job.mode"));
							if(_c.hasProperty("job.storage")) {
								hypervisor_job.put("storage", _c.getProperty("job.storage"));
							} else {
								hypervisor_job.put("storage", "");
							}
							if(_c.hasProperty("job.vm")) {
								String _aux=_c.getProperty("job.vm");
								while (_aux.length()>0){
									if (_aux.contains(":::")){
										_vms.add(_aux.substring(0, _aux.indexOf(":::")));
										_aux=_aux.substring(_aux.indexOf(":::")+3,_aux.length());
									}else{
										_vms.add(_aux);
										break;
									}
								}
							}
							if(_c.hasProperty("job.ds")) {
								String _aux=_c.getProperty("job.ds");
								while (_aux.length()>0){
									if (_aux.contains(":::")){
										_dss.add(_aux.substring(0, _aux.indexOf(":::")));
										_aux=_aux.substring(_aux.indexOf(":::")+3,_aux.length());
									}else{
										_dss.add(_aux);
										break;
									}
								}
							}
							hypervisor_job.put("ds", _dss);
							hypervisor_job.put("vm", _vms);
							hypervisor_jobs.add(hypervisor_job);
						}
					} catch(Exception _ex) {}
				}
			}
		}
		return hypervisor_jobs;
	}
	
	public List<String> getHypervisorJobNames(String name) {
		List<String> hypervisor_jobs = new ArrayList<String>();
		File _dir = new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs());
		if(!_dir.exists()) {
			_dir.mkdirs();
		}
		String[] _files = _dir.list();
		if(_files != null) {
			for(String jobFile : _files) {
				if(jobFile.endsWith(".xml")) {
					hypervisor_jobs.add(jobFile.substring(0, jobFile.length() - 4));
				}
			}
		}
		return hypervisor_jobs;
	}
	
	public Map<String, Object> getHypervisorJob(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid hypervisor job");
		}
		
		Map<String, Object> hypervisor_job = new HashMap<String, Object>();
		File _f = new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs() + "/" + name + ".xml");
		if(!_f.exists()) {
			throw new Exception("hypervisor job does not exists");
		}
		
		Configuration _c = new Configuration(_f);
		List<String> _vms = new ArrayList<String>();
		List<String> _dss = new ArrayList<String>();
		
		hypervisor_job.put("name", name);
		hypervisor_job.put("hypervisor", _c.getProperty("job.hypervisor"));
		hypervisor_job.put("mode", _c.getProperty("job.mode"));
		if(_c.hasProperty("job.storage")) {
			hypervisor_job.put("storage", _c.getProperty("job.storage"));
		} else {
			hypervisor_job.put("storage", "");
		}
		if(_c.hasProperty("job.vm")) {
			String _aux=_c.getProperty("job.vm");
			while (_aux.length()>0){
				if (_aux.contains(":::")){
					_vms.add(_aux.substring(0, _aux.indexOf(":::")));
					_aux=_aux.substring(_aux.indexOf(":::")+3,_aux.length());
				}else{
					_vms.add(_aux);
					break;
				}
			}
		}
		hypervisor_job.put("vm", _vms);
		if(_c.hasProperty("job.ds")) {
			String _aux=_c.getProperty("job.ds");
			while (_aux.length()>0){
				if (_aux.contains(":::")){
					_dss.add(_aux.substring(0, _aux.indexOf(":::")));
					_aux=_aux.substring(_aux.indexOf(":::")+3,_aux.length());
				}else{
					_dss.add(_aux);
					break;
				}
			}
		}
		hypervisor_job.put("ds", _dss);
		return hypervisor_job;
	}
	
	public abstract List<String> getStoreNames() throws Exception;
	
	public abstract List<String> getHostsNames() throws Exception;
	
	public abstract List<Map<String, String>> getStores() throws Exception;
	
	public abstract List<String> getDatastoreNames(String host) throws Exception;
	
	public abstract Map<String, String> getVirtualMachine(String name) throws Exception;
	
	public abstract List<String> getVirtualMachineNames() throws Exception;

	public abstract List<String> getVirtualMachineNames(String storage) throws Exception;
	
	public abstract List<Map<String, String>> getVirtualMachines() throws Exception;
	
	public abstract List<Map<String, String>> getVirtualMachines(String storage) throws Exception;
	
	public abstract Map<String, Map<String, List<String>>> getVirtualTree() throws Exception;
	
	public abstract Map<String, List<String>> getVirtualTreeNoMachines() throws Exception;
	
	public void setHypervisor(String name, int type, String address, String user, String password) throws Exception {
		boolean newHypervisor = false;
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid hypervisor name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryHypervisors());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		_f = new File(WBSAirbackConfiguration.getDirectoryHypervisors() + "/" + name + ".xml");
		if(!_f.exists()) {
			newHypervisor = true;
		}
		
		try {
			Configuration _c = new Configuration(_f);
			switch(type) {
				case VMWARE_VSPHERE: {
						_c.setProperty("hypervisor.os", "vmware");
						getInstance(HypervisorManager.VMWARE_VSPHERE, address, user, password);
					}
					break;
				case XEN: {
						_c.setProperty("hypervisor.os", "xen");
						getInstance(HypervisorManager.XEN, address, user, password);
					}
				break;
				default: {
						throw new Exception("invalid hypervisor type");
					}
			}
			_c.setProperty("hypervisor.address", address);
			if(user != null && !user.isEmpty()) {
				_c.setProperty("hypervisor.user", user);
				_c.setProperty("hypervisor.password", password);
			}
			_c.store();
		} catch(Exception _ex) {
			if(newHypervisor) {
				new File(WBSAirbackConfiguration.getDirectoryHypervisors() + "/" + name + ".xml").delete();
			}
			throw _ex;
		}
	}
	
	public void setHypervisorJob(String name, String hypervisor, String local_storage, String mode, List<String> vms, List<String> dss) throws Exception {
		boolean newHypervisor = false;
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid hypervisor job name");
		}
		if(mode == null || (!mode.equalsIgnoreCase("http") && !mode.equalsIgnoreCase("store"))) {
			throw new Exception("invalid hypervisor job mode");
		}
		if(local_storage == null || local_storage.isEmpty()) {
			throw new Exception("invalid hypervisor job storage");
		}
		if(!getAllHypervisorNames().contains(hypervisor)) {
			throw new Exception("invalid hypervisor name");
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs());
		if(!_f.exists()) {
			_f.mkdirs();
		}
		
		_f = new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs() + "/" + name + ".xml");
		if(!_f.exists()) {
			newHypervisor = true;
		}
		
		try {
			Configuration _c = new Configuration(_f);
			_c.setProperty("job.name", name);
			_c.setProperty("job.hypervisor", hypervisor);
			_c.setProperty("job.storage", local_storage);
			_c.setProperty("job.mode", mode.toLowerCase());
			if(vms != null) {
				StringBuilder _sb = new StringBuilder();
				for(String vm : vms) {
					if(vm != null && !vm.trim().isEmpty()) {
						if(_sb.length() > 0) {
							_sb.append(":::");
						}
						_sb.append(vm);
					}
				}
				if(_sb.length() > 0) {
					_c.setProperty("job.vm", _sb.toString());					
				} else {
					_c.removeProperty("job.vm");
				}
			}
			if(dss != null) {
				StringBuilder _sb = new StringBuilder();
				for(String ds : dss) {
					if(ds != null && !ds.trim().isEmpty()) {
						if(_sb.length() > 0) {
							_sb.append(":::");
						}
						_sb.append(ds);
					}
				}
				if(_sb.length() > 0) {
					_c.setProperty("job.ds", _sb.toString());					
				} else {
					_c.removeProperty("job.ds");
				}
			}
			_c.store();
		} catch(Exception _ex) {
			if(newHypervisor) {
				new File(WBSAirbackConfiguration.getDirectoryHypervisorJobs() + "/" + name + ".xml").delete();
			}
			throw _ex;
		}
	}
}