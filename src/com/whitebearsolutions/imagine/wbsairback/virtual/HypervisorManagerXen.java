package com.whitebearsolutions.imagine.wbsairback.virtual;

import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.http.ssl.HTTPSHostnameVerifier;
import com.whitebearsolutions.http.ssl.HTTPSTrustManager;
import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.xensource.xenapi.APIVersion;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.SR;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types.VmPowerState;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VBD;
import com.xensource.xenapi.VDI;
import com.xensource.xenapi.VIF;
import com.xensource.xenapi.VM;

public class HypervisorManagerXen extends HypervisorManager {
	private String _url;
	private String _user;
	private String _password;
	
	private final static Logger logger = LoggerFactory.getLogger(HypervisorManagerXen.class);
	
	public HypervisorManagerXen(String address, String user, String password) throws Exception {
		if(!NetworkManager.isValidAddress(address)) {
			throw new Exception("invalid network address");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("https://");
		_sb.append(address);
		this._url = _sb.toString();
		this._user = user;
		this._password = password;
		
		try {
			logger.debug("Connecting to xen at {} with user:{}", new Object[]{_url, _user});
			HostnameVerifier _hnv = new HTTPSHostnameVerifier();
	        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
	    	KeyStore _store = KeyStore.getInstance("JKS");
	    	TrustManagerFactory _tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    	_tmf.init(_store);
	    	X509TrustManager x509Manager = (X509TrustManager) _tmf.getTrustManagers()[0];
	    	HTTPSTrustManager _httpstm = new HTTPSTrustManager(x509Manager);
	    	SSLContext _ctx = SSLContext.getInstance("TLS");
	    	_ctx.init(null, new TrustManager[] { _httpstm }, null);
	    	HttpsURLConnection.setDefaultSSLSocketFactory(_ctx.getSocketFactory());
	    	logger.debug("Environment prepared to connect. Conecting...");
	    	Connection _connection = new Connection(new URL(this._url));
	    	logger.debug("Conected. Trying to log on ...");
			Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
			logger.debug("Logued. So logging out ...");
	        Session.logout(_connection);
	        logger.debug("Logged out successfully");
		} catch (Exception ex) {
			logger.error("Error connecting with XEN server. Ex: {}", ex.getMessage());
			throw new Exception ("Error connecting with XEN server. Ex: "+ex.getMessage());
		}
        
	}
	
	public void addNFSStore(String name, String host, String address, String path) throws Exception {
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        		
		try {
			Host _host = (Host) Host.getAll(_connection).toArray()[0];            // create config parameter for shared storage on nfs server
            Map<String, String> deviceConfig = new HashMap<String, String>();
            deviceConfig.put("server", address);
            deviceConfig.put("serverpath", path);

            String desc = "[" + address + ":" + path + "] Created by WBSAirback";
            SR.create(_connection, _host, deviceConfig, 100000L, name, desc, "nfs", "unused", true, new HashMap<String, String>());
		} catch(XenAPIException _ex) {
			throw new Exception("cannot create nfs datastore - already exists");
		} catch(Exception _ex) {
			if(_ex.getMessage() != null) {
				throw new Exception("cannot create nfs datastore - " + _ex.getMessage());
			} else {
				throw new Exception("cannot create nfs datastore - " + _ex.getClass().getName());
			}
		} finally {
			Session.logout(_connection);
		}
	}
	
	public void cloneVirtualMachine(String name, String store) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        String snapshotOldName = null;
        
		try {
			ArrayList<VM> _vm_list = new ArrayList<VM>(VM.getByNameLabel(_connection, name));
			ArrayList<SR> _sr_list = new ArrayList<SR>(SR.getByNameLabel(_connection, store));
		
			if(_vm_list.isEmpty()) {
				throw new Exception("virtual machine not found");
			}
			if(_sr_list.isEmpty()) {
				throw new Exception("data store not found");
			}
			VM _vm = _vm_list.get(0);
			SR _sr = _sr_list.get(0);
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("wbsairback-");
			_sb.append(name.replaceAll(" ", ""));
			logger.debug("Name of snapshot to be done: {}", _sb.toString());
			
			logger.debug("Searching old snapshots to remove...");
			for(VM _snapshot : _vm.getSnapshots(_connection)) {
				snapshotOldName = _snapshot.getNameLabel(_connection).trim().replaceAll(" ", "");
				logger.debug("Old snapshot found: {}", snapshotOldName);
				if(_sb.toString().equals(snapshotOldName)) {
					try {
						if(!_snapshot.getPowerState(_connection).equals(VmPowerState.HALTED)) {
							try {
								logger.debug("Machine {} is not halted, trying to power off..", _snapshot.getNameLabel(_connection));
								_snapshot.hardShutdown(_connection);
								logger.debug("Machine {} powered off", _snapshot.getNameLabel(_connection));
							} catch(Exception _ex) {
								logger.error("cannot shutdown the old clone virtual machine - " + _ex.getMessage());
								//throw new Exception("cannot shutdown the old clone virtual machine - " + _ex.getMessage());
							}
						}
						logger.debug("Deleting old snapshot ...");
						_snapshot.destroy(_connection);
						logger.debug("Old snapshot deleted ...");
					} catch(Exception _ex) {
						logger.error("cannot remove old snapshot from virtual machine - " + _ex.getMessage());
						throw new Exception("cannot remove old snapshot from virtual machine - " + _ex.getMessage());
					}
				}
			}
			
			logger.debug("Searching local disks at our shared storage repository ...");
			Map<String, String> machineVDisks = new HashMap<String, String>();
			ArrayList<VDI> disks = new ArrayList<VDI>(VDI.getAll(_connection));
			String mySRUuid = _sr.getUuid(_connection);
			for (VDI disk : disks) {
				String parentSRUUid = disk.getSR(_connection).getUuid(_connection);
				if (mySRUuid.equals(parentSRUUid)) {
					String diskUuid = disk.getUuid(_connection);
					ArrayList<VBD> vbds = new ArrayList<VBD>(disk.getVBDs(_connection));
					if (vbds.isEmpty()) {
						logger.debug("Disk: uuid:{} has no more associated machines. Removing ...", diskUuid);
						disk.destroy(_connection);
						logger.debug("Disk: uuid:{} removed", diskUuid);
					} else {
						for (VBD vbd : vbds) {
							String nameMachine = vbd.getVM(_connection).getNameLabel(_connection);
							//logger.debug("vdb: device: {} uuid: {} vmLabel: {} uuid: {}", new Object[]{vbd.getDevice(_connection), vbd.getUuid(_connection), vbd.getVM(_connection).getNameLabel(_connection), vbd.getVM(_connection).getUuid(_connection)});
							if (nameMachine.equals(_sb.toString())) {
								logger.debug("Disk: uuid:{} has associated machines. Storing reference, maybe {} is removed later.", diskUuid, vbd.getVM(_connection).getUuid(_connection));
								machineVDisks.put(vbd.getVM(_connection).getUuid(_connection), diskUuid);
							}
						}
					}
					
				}
			}
			
			
			logger.debug("Searching old clones to remove...");
			_vm_list = new ArrayList<VM>(VM.getByNameLabel(_connection, _sb.toString()));
			if(!_vm_list.isEmpty()) {
				for (VM machine : _vm_list) {
					String machineUUid = machine.getUuid(_connection);
					logger.debug("Machine found: {}", machineUUid);
					try {
						if(!machine.getPowerState(_connection).equals(VmPowerState.HALTED)) {
							try {
								logger.debug("Machine {} is not halted, trying to power off..", machine.getNameLabel(_connection));
								machine.hardShutdown(_connection);
								logger.debug("Machine {} powered off", machine.getNameLabel(_connection));
							} catch(Exception _ex) {
								logger.error("cannot shutdown the old clone virtual machine - " + _ex.getMessage());
								//throw new Exception("cannot shutdown the old clone virtual machine - " + _ex.getMessage());
							}
						}
						logger.debug("Removing machine {} ..", machineUUid);
						machine.destroy(_connection);
						logger.debug("Machine {} removed..", machineUUid);
					} catch(Exception _ex) {
						//logger.error("cannot remove the old clone virtual machine - " + _ex.getMessage());
						//throw new Exception("cannot remove the old clone virtual machine - " + _ex.getMessage());
					} finally {
						if (machineVDisks.containsKey(machineUUid)) {
							String diskUuid = machineVDisks.get(machineUUid);
							ArrayList<VDI> _disks = new ArrayList<VDI>(VDI.getAll(_connection));
							for (VDI disk : _disks) {
								if (disk.getUuid(_connection).equals(diskUuid)) {
									logger.debug("Removing disk {} associated to deleted machine {} ..", diskUuid, machineUUid);
									disk.destroy(_connection);
									logger.debug("Disk {} removed", diskUuid);
								}
							}
							
						}
					}
				}
			}

			VM _snapshot;
			try {
				logger.debug("Making snapshot of target machine {} named {} ..", _vm.getNameLabel(_connection), _sb.toString());
				_snapshot = _vm.snapshot(_connection, _sb.toString());
				_snapshot.setIsATemplate(_connection, false);
				logger.debug("Snapshot done");
			} catch(Exception _ex) {
				logger.error("cannot create an snapshot of virtual machine - " + _ex.getMessage());
				throw new Exception("cannot create an snapshot of virtual machine - " + _ex.getMessage());
			}
			try {
				logger.debug("Copying snapshot to our SR ..", _sr.getNameLabel(_connection));
				_snapshot.copy(_connection, _sb.toString(), _sr);
				logger.debug("Snapshot copyed");
			} catch(Exception _ex) {
				logger.error("copy failed - " + _ex.getMessage());
				throw new Exception("copy failed - " + _ex.getMessage());
			}
			
			try {
				logger.debug("Removing snapshot already copied ..");
				_snapshot.destroy(_connection);
				logger.debug("Snapshot removed");
			} catch(Exception _ex) {
				logger.error("cannot remove current snapshot from virtual machine - " + _ex.getMessage());
				throw new Exception("cannot remove current snapshot from virtual machine - " + _ex.getMessage());
			}
		} catch(Exception _ex) {
			if(_ex.getMessage() != null) {
				logger.error("cannot clone virtual machine - " + _ex.getMessage());
				throw new Exception("cannot clone virtual machine - " + _ex.getMessage());
			} else {
				logger.error("cannot clone virtual machine - " + _ex.getClass().getName());
				throw new Exception("cannot clone virtual machine - " + _ex.getClass().getName());
			}
		} finally {
			Session.logout(_connection);
		}
	}
	
	public void downloadVirtualMachineOVF(String name, String local_path) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		if(!local_path.endsWith("/")) {
			local_path = local_path.concat("/");
		}
		
		throw new Exception("cannot download virtual machine - not implemented in XenServer");
	}
	
	public boolean existsNFSStore(String name,String host, String address, String path) throws Exception {
		if(address == null || path == null || address.isEmpty() || path.isEmpty()) {
			return false;
		}
		
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			Map<SR, SR.Record> _srs = SR.getAllRecords(_connection);
	        for(SR _sr : _srs.keySet()) {
	            if(name.equals(_sr.getNameLabel(_connection))) {
	            	return true;
	            }
	        }

		} catch(Exception _ex) {
			System.out.println("HypervisorManagerXen::existsNFSStore:error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return false;
	}
	
	public List<String> getStoreNames() throws Exception {
		List<String> _stores = new ArrayList<String>();
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			for(SR _sr : SR.getAllRecords(_connection).keySet()) {
				_stores.add(_sr.getNameLabel(_connection));
			}
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return _stores;
	}
	
	public List<Map<String, String>> getStores() throws Exception {
		List<Map<String, String>> _stores = new ArrayList<Map<String, String>>();
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			for(SR _sr : SR.getAllRecords(_connection).keySet()) {
				Map<String,String> _store = new HashMap<String, String>();
		    	_store.put("name", _sr.getNameLabel(_connection));
		    	_store.put("free", String.valueOf(_sr.getPhysicalSize(_connection) - _sr.getPhysicalUtilisation(_connection)));
		    	_store.put("directory-based", "");
		    	_store.put("path", "");
	    		_store.put("mode", "");
		    	_stores.add(_store);
			}
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return _stores;
	}
	
	public String getHostForVirtualMachine(String vmName) throws Exception {
		if(vmName == null || vmName.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			VM _vm = new ArrayList<VM>(VM.getByNameLabel(_connection, vmName)).get(0);
			if(_vm == null) {
				throw new Exception("virtual machine not found");
			} else {
				return vmName;
			}
//			Host _host = _vm.getResidentOn(_connection);
//			if(_host == null) {
//				return null;
//			}
//			return _host.getNameLabel(_connection);
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
	}
	
	public List<String> getVirtualMachineNames() throws Exception {
		List<String> _vms = new ArrayList<String>();
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			Map<VM, VM.Record> _vm_records = VM.getAllRecords(_connection);
			for(VM _vm_record : _vm_records.keySet()) {
				if(_vm_record.getIsATemplate(_connection) || _vm_record.getNameLabel(_connection).startsWith("wbsairback-")) {
		    		continue;
		    	}
				_vms.add(_vm_record.getNameLabel(_connection));
			}
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return _vms;
	}
	
	public List<String> getVirtualMachineNames(String storage) throws Exception {
		List<String> _vms = new ArrayList<String>();
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			for(SR _sr : SR.getByNameLabel(_connection, storage)) {
				for(VDI _vdi : _sr.getVDIs(_connection)) {
					for(VBD _vbd : _vdi.getVBDs(_connection)) {
						VM _vm_record = _vbd.getVM(_connection);
						_vms.add(_vm_record.getNameLabel(_connection));
					}
				}
			}
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return _vms;
	}
	
	public List<Map<String, String>> getVirtualMachines() throws Exception {
		List<Map<String, String>> _vms = new ArrayList<Map<String, String>>();
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			Map<VM, VM.Record> _vm_records = VM.getAllRecords(_connection);
			for(VM _vm_record : _vm_records.keySet()) {
				if(_vm_record.getNameLabel(_connection).startsWith("wbsairback-")) {
		    		continue;
		    	}
				Map<String, String> _vm = new HashMap<String, String>();
				_vm.put("name", _vm_record.getNameLabel(_connection));
				StringBuilder _sb = new StringBuilder();
				for(VBD _vbd : _vm_record.getVBDs(_connection)) {
					if(_sb.length() > 0) {
		    			_sb.append(":");
		    		}
					_sb.append(_vbd.getVDI(_connection).getSR(_connection).getNameLabel(_connection));
				}
				_vm.put("datastore", _sb.toString());
				_sb = new StringBuilder();
				for(VIF _vif : _vm_record.getVIFs(_connection)) {
					if(_sb.length() > 0) {
		    			_sb.append(":");
		    		}
					_sb.append(_vif.getDevice(_connection));
				}
				_vm.put("network", _sb.toString());
				_vms.add(_vm);
			}
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return _vms;
	}
	
	public List<Map<String, String>> getVirtualMachines(String storage) throws Exception {
		List<Map<String, String>> _vms = new ArrayList<Map<String, String>>();
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
        Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		try {
			for(SR _sr : SR.getByNameLabel(_connection, storage)) {
				for(VDI _vdi : _sr.getVDIs(_connection)) {
					for(VBD _vbd : _vdi.getVBDs(_connection)) {
						VM _vm_record = _vbd.getVM(_connection);
						StringBuilder _sb = new StringBuilder();
						Map<String, String> _vm = new HashMap<String, String>();
						_vm.put("name", _vm_record.getNameLabel(_connection));
						_vm.put("datastore", storage);
						_sb = new StringBuilder();
						for(VIF _vif : _vm_record.getVIFs(_connection)) {
							if(_sb.length() > 0) {
				    			_sb.append(":");
				    		}
							_sb.append(_vif.getDevice(_connection));
						}
						_vm.put("network", _sb.toString());
						_vms.add(_vm);
					}
				}
			}
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return _vms;
	}
	
	public Map<String, String> getVirtualMachine(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		
		HostnameVerifier _hnv = new HTTPSHostnameVerifier();
        HttpsURLConnection.setDefaultHostnameVerifier(_hnv);
        
		Connection _connection = new Connection(new URL(this._url));
        Session.loginWithPassword(_connection, this._user, this._password, APIVersion.latest().toString());
        
		Map<String,String> _machine = new HashMap<String, String>();
		try {
			VM _vm = new ArrayList<VM>(VM.getByNameLabel(_connection, name)).get(0);
			if(_vm == null) {
				throw new Exception("virtual machine not found");
			}
			
			_machine.put("name", _vm.getNameLabel(_connection));
			StringBuilder _sb = new StringBuilder();
			for(VBD _vbd : _vm.getVBDs(_connection)) {
				if(_sb.length() > 0) {
	    			_sb.append(":");
	    		}
				_sb.append(_vbd.getVDI(_connection).getSR(_connection).getNameLabel(_connection));
			}
			_machine.put("datastore", _sb.toString());
			_sb = new StringBuilder();
			for(VIF _vif : _vm.getVIFs(_connection)) {
				if(_sb.length() > 0) {
	    			_sb.append(":");
	    		}
				_sb.append(_vif.getDevice(_connection));
			}
			_machine.put("network", _sb.toString());
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			Session.logout(_connection);
		}
		return _machine;
	}

	@Override
	public List<String> getHostsNames() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getDatastoreNames(String host) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, List<String>>> getVirtualTree()
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<String>> getVirtualTreeNoMachines()
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
