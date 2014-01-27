package com.whitebearsolutions.imagine.wbsairback.virtual;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.AlreadyExists;
import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.DatastoreInfo;
import com.vmware.vim25.FileNotFound;
import com.vmware.vim25.HostNasVolumeSpec;
import com.vmware.vim25.HttpNfcLeaseDeviceUrl;
import com.vmware.vim25.HttpNfcLeaseInfo;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.NasDatastoreInfo;
import com.vmware.vim25.OvfCreateDescriptorParams;
import com.vmware.vim25.OvfCreateDescriptorResult;
import com.vmware.vim25.OvfFile;
import com.vmware.vim25.PlatformConfigFault;
import com.vmware.vim25.RestrictedVersion;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer1BackingInfo;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskRawDiskMappingVer1BackingInfo;
import com.vmware.vim25.VirtualDiskSparseVer1BackingInfo;
import com.vmware.vim25.VirtualDiskSparseVer2BackingInfo;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineRelocateDiskMoveOptions;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualMachineRelocateSpecDiskLocator;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostDatastoreSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.HttpNfcLease;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;
import com.whitebearsolutions.http.ssl.HTTPSTrustManager;
import com.whitebearsolutions.imagine.wbsairback.NetworkManager;

public class HypervisorManagerVMware extends HypervisorManager {
	private String _url;
	private String _address;
	private String _user;
	private String _password;
	
	private final static Logger logger = LoggerFactory.getLogger(HypervisorManagerVMware.class);
	
	public HypervisorManagerVMware(String address, String user, String password) throws Exception {
		if(!NetworkManager.isValidAddress(address)) {
			throw new Exception("invalid network address");
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("https://");
		_sb.append(address);
		_sb.append("/sdk/VimService");
		this._url = _sb.toString();
		this._address = address;
		this._user = user;
		this._password = password;
		try {
			trustAllHttpsCertificates();
			logger.info("Vmware: connected at first attempt");
		} catch (SSLHandshakeException sslEx) {
			trustAllHttpsCertificatesWithoutKeys();
			logger.info("Vmware: connected at second attempt");
		}
		new ServiceInstance(new URL(this._url), this._user, this._password, true);
	}
	
	public void addNFSStore(String name, String host, String address, String path) throws Exception {
		logger.info("Adding NFS store => name:{} host:{} address:{} path:{}", new Object[]{name, host, address, path});
		ServiceInstance si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		if(host == null || host.isEmpty()) {
			si.getServerConnection().logout();
			logger.error("invalid host name");
			throw new Exception("invalid host name");
		}
		try {
			HostSystem _host = (HostSystem) new InventoryNavigator(si.getRootFolder()).searchManagedEntity("HostSystem", host);
			if(_host == null) {
				logger.error("host system not found");
				throw new Exception("host system not found");
			}
			
			HostDatastoreSystem _hds = _host.getHostDatastoreSystem();
			if(_hds == null) {
				logger.error("datastore system error for host [" + _host.getName() + "]");
				throw new Exception("datastore system error for host [" + _host.getName() + "]");
			}
			
			HostNasVolumeSpec _hnvs = new HostNasVolumeSpec();
			_hnvs.setRemoteHost(address);
			_hnvs.setRemotePath(path);
			_hnvs.setLocalPath(name);
			_hnvs.setAccessMode("readWrite");
			_hds.createNasDatastore(_hnvs);
		} catch(RestrictedVersion _ex) {
			logger.error("cannot create nfs datastore - vmware vsphere restricted version");
			throw new Exception("cannot create nfs datastore - vmware vsphere restricted version");
		} catch(AlreadyExists _ex) {
			logger.error("cannot create nfs datastore - already exists");
			throw new Exception("cannot create nfs datastore - already exists");
		} catch(PlatformConfigFault _ex) {
			logger.error("cannot create nfs datastore - vmware host platform configuration fault");
			throw new Exception("cannot create nfs datastore - vmware host platform configuration fault");
		} catch(Exception _ex) {
			if(_ex.getMessage() != null) {
				logger.error("cannot create nfs datastore - " + _ex.getMessage());
				throw new Exception("cannot create nfs datastore - " + _ex.getMessage());
			} else {
				logger.error("cannot create nfs datastore - " + _ex.getClass().getName());
				throw new Exception("cannot create nfs datastore - " + _ex.getClass().getName());
			}
		} finally {
			si.getServerConnection().logout();
			logger.info("Conexión con el servidor cerrada");
		}
	}
	
	public void cloneVirtualMachine(String name, String store) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		
		ServiceInstance si = null;
		Folder rootFolder = null;
		VirtualMachine _vm = null;
		String newName = null;
		
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
			rootFolder = si.getRootFolder();
			_vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", name);
			if(_vm == null) {
				logger.error("virtual machine not found");
				throw new Exception("virtual machine not found");
			}
			
			StringBuilder _sb = new StringBuilder();
			_sb.append("wbsairback-");
			_sb.append(name.replaceAll(" ", ""));
			
			newName = _sb.toString();
			logger.info("Begin to clone machine with new Name: {}", newName);
		} catch (Exception _ex) {
			if(_ex.getMessage() != null) {
				logger.error("cannot get service or machine instace - " + _ex.getMessage());
				throw new Exception("cannot get service or machine instace - " + _ex.getMessage());
			} else {
				logger.error("cannot get service or machine instace - " + _ex.getClass().getName());
				throw new Exception("cannot get service or machine instace - " + _ex.getClass().getName());
			}
		}
		
		try {
			Datastore _ds = (Datastore) new InventoryNavigator(rootFolder).searchManagedEntity("Datastore", store);
			if(_ds == null) {
				logger.error("datastore [" + store + "] does not exists");
				throw new Exception("datastore [" + store + "] does not exists");
			}
			
			int count = 0;
			VirtualMachineRelocateSpec _vmrs = new VirtualMachineRelocateSpec();
			List<Integer> _keys = getIndependentVirtualDiskKeys(_vm);
			VirtualMachineRelocateSpecDiskLocator[] diskLocator = new VirtualMachineRelocateSpecDiskLocator[_keys.size()];
			for(Integer _key : _keys) {
				 diskLocator[count] = new VirtualMachineRelocateSpecDiskLocator();
				 diskLocator[count].setDatastore(_ds.getMOR());
				 diskLocator[count].setDiskMoveType(VirtualMachineRelocateDiskMoveOptions.moveAllDiskBackingsAndAllowSharing.toString());
				 diskLocator[count].setDiskId(_key);
				 count++;
			}
			_vmrs.setDatastore(_ds.getMOR());
			_vmrs.setDiskMoveType(VirtualMachineRelocateDiskMoveOptions.moveAllDiskBackingsAndAllowSharing.toString());
			_vmrs.setDisk(diskLocator);
			
			removeVirtualMachineSnapshot(_vm, newName);
		    
			Task _task = _vm.createSnapshot_Task(newName, newName, true, false);
			logger.info("Creating snapshot task to {} ...", newName);
		    _task.waitForTask();
		    if(_task.getTaskInfo().getState() == TaskInfoState.error) {
		    	logger.error("Error on snapshot task. Ex: {}", _task.getTaskInfo().getError().getLocalizedMessage());
		    	throw new Exception(_task.getTaskInfo().getError().getLocalizedMessage());
			}
		    logger.info("Snapshot done ");
		    
		    VirtualMachine[] _vms = _ds.getVms();
		    if(_vms != null) {
		    	for(VirtualMachine _vmt : _vms) {
		    		if(_vmt.getName().equals(newName)) {
		    			_task = _vmt.destroy_Task();
		    			logger.info("Deleting virtual machine {} ...", newName);
			    		_task.waitForTask();
						if(_task.getTaskInfo().getState() == TaskInfoState.error) {
							logger.error("Cannot remove target virtual machine [" + _vmt.getName() + "]");
							throw new Exception("cannot remove target virtual machine [" + _vmt.getName() + "]");
						}
						logger.info("Virtual machine removed");
		    		}
		    	}
		    }
			
		    VirtualMachineCloneSpec _cs = new VirtualMachineCloneSpec();
			_cs.setPowerOn(false);
			_cs.setTemplate(false);
			_cs.setLocation(_vmrs);
			
			try {
				VirtualMachineSnapshotTree[] _stree = _vm.getSnapshot().getRootSnapshotList();
			    if(_stree != null) {
			    	for(VirtualMachineSnapshotTree _st : _stree) {
			    		if(_st.getName().equals(newName)) {
			    			_cs.setSnapshot(_st.getSnapshot());
			    		}
			    	}
			    }
			} catch(Exception _ex) {
				throw new Exception("cannot set snapshot for clone");
			}
			
			Folder parent = (Folder) _vm.getParent();
			_task = _vm.cloneVM_Task(parent, newName, _cs);
			logger.info("Cloning ...");
			_task.waitForTask();
			if(_task.getTaskInfo().getState() == TaskInfoState.error) {
				throw new Exception(_task.getTaskInfo().getError().getLocalizedMessage());
			}
			logger.info("Clone done");
			
		} catch(RestrictedVersion _ex) {
			logger.error("cannot clone virtual machine - vmware vsphere restricted version");
			throw new Exception("cannot clone virtual machine - vmware vsphere restricted version");
		} catch(TaskInProgress _ex) {
			logger.error("cannot clone virtual machine - task in progress");
			throw new Exception("cannot clone virtual machine - task in progress");
		} catch(FileNotFound _ex) {
			logger.error("cannot clone virtual machine - file not found");
			throw new Exception("cannot clone virtual machine - file not found");
		} catch(NullPointerException _ex) {
			logger.error("cannot clone virtual machine - uknown error");
			throw new Exception("cannot clone virtual machine - uknown error");
		} catch(Exception _ex) {
			if(_ex.getMessage() != null) {
				logger.error("cannot clone virtual machine - " + _ex.getMessage());
				throw new Exception("cannot clone virtual machine - " + _ex.getMessage());
			} else {
				logger.error("cannot clone virtual machine - " + _ex.getClass().getName());
				throw new Exception("cannot clone virtual machine - " + _ex.getClass().getName());
			}
		} finally {
			try {
				removeVirtualMachineSnapshot(_vm, newName);
			} catch (Exception ex) {
				throw ex;
			} finally {
				si.getServerConnection().logout();
			}
		}
	}
	
	public void removeVirtualMachineSnapshot(VirtualMachine vm, String nameVm) throws Exception {
		logger.info("Launching old snapshot removing process for {}", nameVm);
		if(vm.getSnapshot() != null) {
			logger.info("Deleting snapshot ...");
			VirtualMachineSnapshotTree[] _stree = vm.getSnapshot().getRootSnapshotList();
		    if(_stree != null) {
		    	for(VirtualMachineSnapshotTree _st : _stree) {
		    		if(_st.getName().equals(nameVm)) {
		    			logger.info("Old snahpot {} found");
		    			VirtualMachineSnapshot _vmsSnap = new VirtualMachineSnapshot(vm.getServerConnection(), _st.getSnapshot());
		    			Task _taskSnap = _vmsSnap.removeSnapshot_Task(true);
		    			logger.info("Removing process launched...");
			    		if(_taskSnap.waitForTask() != Task.SUCCESS) {
			    			logger.error("Error on snapshot removing process. {}",_taskSnap.getTaskInfo().getError().getLocalizedMessage());
			    			throw new Exception(_taskSnap.getTaskInfo().getError().getLocalizedMessage());
			    		}
			    		logger.info("Snapshot removed successfully");
		    		}
		    	}
		    }
		}
	}
	
	public void downloadVirtualMachineOVF(String name, String local_path) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		if(!local_path.endsWith("/")) {
			local_path = local_path.concat("/");
		}
		
		ServiceInstance si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		try {
			Folder rootFolder = si.getRootFolder();
			VirtualMachine _vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", name);
			if(_vm == null) {
				throw new Exception("virtual machine not found");
			}
			
			local_path = local_path.concat(_vm.getName()).concat("/");
			File _f = new File(local_path);
			if(!_f.exists()) {
				_f.mkdir();
			}
			HttpNfcLease _hl = _vm.exportVm();
			HttpNfcLeaseState _hls;
		    while(true) {
				_hls = _hl.getState();
			    if(_hls == HttpNfcLeaseState.ready || _hls == HttpNfcLeaseState.error) {
			    	break;
			    }
		    }
		    
		    HttpNfcLeaseInfo _hlinfo = _hl.getInfo();
			if(_hls == HttpNfcLeaseState.error) {
		        throw new Exception(_hl.getError().getLocalizedMessage());
		    }
			_hlinfo.setLeaseTimeout(300 * 1000 * 1000);
		    long _diskCapacity = (_hlinfo.getTotalDiskCapacityInKB()) * 1024;
		    long _writtenBytes = 0;
		    HttpNfcLeaseDeviceUrl[] _deviceUrl = _hlinfo.getDeviceUrl();
		    if(_deviceUrl != null) {
		    	OvfFile[] _ovfFiles = new OvfFile[_deviceUrl.length];
		    	for (int i = 0; i < _deviceUrl.length; i++) {
		    		String _deviceId = _deviceUrl[i].getKey();
			        String _deviceURL = _deviceUrl[i].getUrl();
			        String _fileName = _deviceURL.substring(_deviceURL.lastIndexOf("/") + 1);
			        String _diskLocalFile = local_path + _fileName;
			        String cookie = si.getServerConnection().getVimService().getWsc().getCookie();
			        long _fileSize = writeVMDKFile(_diskLocalFile, _deviceURL.replace("*", this._address), cookie, _writtenBytes, _diskCapacity);
			        _writtenBytes += _fileSize;
			        OvfFile _ovfFile = new OvfFile();
			        _ovfFile.setPath(_fileName);
			        _ovfFile.setDeviceId(_deviceId);
			        _ovfFile.setSize(_fileSize);
			        _ovfFiles[i] = _ovfFile;
		    	}
		    	
		    	OvfCreateDescriptorParams _ovfParams = new OvfCreateDescriptorParams();
		    	_ovfParams.setOvfFiles(_ovfFiles);
			    OvfCreateDescriptorResult ovfCreateDescriptorResult = si.getOvfManager().createDescriptor(_vm, _ovfParams);

			    StringBuilder _ovf = new StringBuilder();
			    _ovf.append(local_path);
			    _ovf.append(_vm.getName().replace(" ", ""));
			    _ovf.append(".ovf");
			    FileWriter out = new FileWriter(_ovf.toString());
			    out.write(ovfCreateDescriptorResult.getOvfDescriptor());
			    out.close();
			    _hl.httpNfcLeaseComplete();
		    }
		} catch(RestrictedVersion _ex) {
			throw new Exception("cannot download virtual machine - vmware vsphere restricted version");
		} catch(FileNotFound _ex) {
			throw new Exception("cannot download virtual machine - file not found");
		} catch(TaskInProgress _ex) {
			throw new Exception("cannot download virtual machine - task in progress");
		} catch(Exception _ex) {
			if(_ex.getMessage() != null) {
				throw new Exception("cannot download virtual machine - " + _ex.getMessage());
			} else {
				throw new Exception("cannot download virtual machine - " + _ex.getClass().getName());
			}
		} finally {
			si.getServerConnection().logout();
		}
	}
	
	public boolean existsNFSStore(String name, String host, String address, String path) throws Exception {
		if(address == null || path == null || address.isEmpty() || path.isEmpty()) {
			return false;
		}
		ServiceInstance si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		if(host == null || host.isEmpty()) {
			si.getServerConnection().logout();
			throw new Exception("invalid host name");
		}
		try {
			HostSystem _host = (HostSystem) new InventoryNavigator(si.getRootFolder()).searchManagedEntity("HostSystem", host);
			if(_host == null) {
				si.getServerConnection().logout();
				throw new Exception("host system not found");
			}
			
			HostDatastoreSystem _hds = _host.getHostDatastoreSystem();
			for(Datastore _ds : _hds.getDatastores()) {
				DatastoreInfo _info = _ds.getInfo();
				if(_info instanceof NasDatastoreInfo) {
					NasDatastoreInfo _nasinfo = (NasDatastoreInfo) _info;
					if(name.equalsIgnoreCase(_nasinfo.getNas().getName())) {
						return true;
					} else if(address.equalsIgnoreCase(_nasinfo.getNas().getRemoteHost()) &&
							path.equalsIgnoreCase(_nasinfo.getNas().getRemotePath())) {
						return true;
					}
				}
			}
		} catch(Exception _ex) {
		} finally {
			si.getServerConnection().logout();
		}
		return false;
	}
	
	public List<String> getStoreNames() throws Exception {
		List<String> _stores = new ArrayList<String>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return _stores;
		}
		try {
			Folder rootFolder = si.getRootFolder();
			ManagedEntity[] _entities = new InventoryNavigator(rootFolder).searchManagedEntities("Datastore");
			if(_entities == null) {
		    	return _stores;
		    }
			for(ManagedEntity _entity : _entities) {
		    	Datastore _ds = (Datastore) _entity;
		    	if(!_ds.getName().startsWith("wbsairback-")) {
		    		_stores.add(_ds.getName());
		    	}
		    }
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			si.getServerConnection().logout();
		}
		return _stores;
	}
	
	public List<Map<String, String>> getStores() throws Exception {
		List<Map<String, String>> _stores = new ArrayList<Map<String, String>>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return _stores;
		}
		try {
			Folder rootFolder = si.getRootFolder();
			ManagedEntity[] _entities = new InventoryNavigator(rootFolder).searchManagedEntities("Datastore");
			if(_entities == null) {
		    	return _stores;
		    }
			for(ManagedEntity _entity : _entities) {
		    	Datastore _ds = (Datastore) _entity;
		    	if(_ds.getName().startsWith("wbsairback-")) {
		    		continue;
		    	}
		    	Map<String,String> _store = new HashMap<String, String>();
		    	_store.put("name", _ds.getName());
		    	_store.put("free", String.valueOf(_ds.getInfo().getFreeSpace()));
		    	if(_ds.getCapability().isDirectoryHierarchySupported()) {
		    		_store.put("directory-based", "true");
		    	} else {
		    		_store.put("directory-based", "false");
		    	}
		    	for(DatastoreHostMount _dshm : _ds.getHost()) {
		    		_store.put("path", _dshm.getMountInfo().getPath());
		    		_store.put("mode", _dshm.getMountInfo().getAccessMode());
		    	}
		    	_stores.add(_store);
		    }
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			si.getServerConnection().logout();
		}
		return _stores;
	}
	
	public String getHostForVirtualMachine(String vmName) throws Exception {
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return null;
		}
		ManagedEntity[] _hosts = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		if(_hosts != null) {
			for(ManagedEntity _entity : _hosts) {
				HostSystem _host = (HostSystem) _entity;
				VirtualMachine[] _vms = _host.getVms();
				if(_vms != null) {
					for(VirtualMachine _vm : _vms) {
						if(vmName.equalsIgnoreCase(_vm.getName())) {
							return _host.getName();
						}
					}
				}
			}
		}
		return null;
	}
	
	public List<String> getHostsNames() throws Exception {
		List<String> hostNames = new ArrayList<String>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return null;
		}
		ManagedEntity[] _hosts = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		if(_hosts != null) {
			for(ManagedEntity _entity : _hosts) {
				HostSystem _host = (HostSystem) _entity;
				hostNames.add(_host.getName());
			}
		}
		return hostNames;
	}
	
	public List<String> getDatastoreNames(String host) throws Exception {
		List<String> datastores = new ArrayList<String>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return null;
		}
		ManagedEntity[] _hosts = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		if(_hosts != null) {
			for(ManagedEntity _entity : _hosts) {
				HostSystem _host = (HostSystem) _entity;
				if (host.equals(_host.getName())) {
					for(Datastore _ds : _host.getDatastores()) {
						datastores.add(_ds.getName());
					}
				}
			}
		}
		return datastores;
	}
	
	public Map<String, Map<String, List<String>>> getVirtualTree() throws Exception {
		Map<String, Map<String, List<String>>> tree = new TreeMap<String, Map<String, List<String>>>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return null;
		}
		ManagedEntity[] _hosts = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		Map<String, List<String>> globalDatastoreMachines = new HashMap<String, List<String>>();
		List<String> _hostDatastoresNames = null;
		List<String> _hostMachines = null;
		List<String> _matchedHostMachines = null;
		HostSystem _host = null;
		Datastore[] _hostDatastores = null;
		Map<String, List<String>> datastores = null;
		String datastoreName = null;
		String hostName = null;
		List<String> _datastoreMachines = null;
		
		if(_hosts != null) {
			logger.info("Obtenidos {} hosts para url:{} user:{} ", new Object[]{_hosts.length, _url, _user});
			for(ManagedEntity _entity : _hosts) {
				_host = (HostSystem) _entity;
				hostName = _host.getName();
				_hostDatastoresNames = new ArrayList<String>();
				_hostMachines = new ArrayList<String>();
				_matchedHostMachines = new ArrayList<String>();
				_hostDatastores = _host.getDatastores();
				if (_hostDatastores != null) {
					logger.info("Obtenidos {} datastores para host {} ", new Object[]{_hostDatastores.length, hostName});
					for(Datastore _ds : _hostDatastores) {
						if (!_ds.getName().startsWith("wbsairback"))
							_hostDatastoresNames.add(_ds.getName());
					}
				
					for(VirtualMachine _ds : _host.getVms()) {
						_hostMachines.add(_ds.getName());
					}
					logger.info("Obtenidos {} vms para host {} ", new Object[]{_hostMachines.size(), hostName});
					
					datastores = new HashMap<String, List<String>>();
					for(Datastore _ds : _hostDatastores) {
						datastoreName = _ds.getName();
						if (!datastoreName.startsWith("wbsairback")) {
							if (!globalDatastoreMachines.containsKey(datastoreName)) {
								_datastoreMachines = new ArrayList<String>();
								for(VirtualMachine _vm : _ds.getVms()) {
									if(!_vm.getConfig().isTemplate()) {
										if (_hostMachines.contains(_vm.getName())) {
											_datastoreMachines.add(_vm.getName());
											if (!_matchedHostMachines.contains(_vm.getName()))
												_matchedHostMachines.add(_vm.getName());
										}
							    	}
							    }
								logger.info("Obtenidas {} vms para datastore {} ", new Object[]{_datastoreMachines.size(), datastoreName});
								datastores.put(datastoreName, _datastoreMachines);
								globalDatastoreMachines.put(datastoreName, _datastoreMachines);
							} else {
								datastores.put(datastoreName, globalDatastoreMachines.get(_ds.getName()));
							}
						}
					}
					
					if (!_hostMachines.isEmpty()) {
						if (!_matchedHostMachines.isEmpty())
							_hostMachines.removeAll(_matchedHostMachines);
						datastores.put("No-datastore", _hostMachines);
					}
					tree.put(hostName, datastores);
				}
			}
		}
		return tree;
	}
	
	public Map<String, List<String>> getVirtualTreeNoMachines() throws Exception {
		Map<String, List<String>> tree = new TreeMap<String, List<String>>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return null;
		}
		ManagedEntity[] _hosts = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		List<String> _hostDatastoresNames = null;
		HostSystem _host = null;
		
		if(_hosts != null) {
			for(ManagedEntity _entity : _hosts) {
				_host = (HostSystem) _entity;
				_hostDatastoresNames = new ArrayList<String>();
				for(Datastore _ds : _host.getDatastores()) {
					_hostDatastoresNames.add(_ds.getName());
				}
				logger.info("Obtenidos {} datastores para host {} ", new Object[]{_hostDatastoresNames.size(), _host.getName()});
				tree.put(_host.getName(), _hostDatastoresNames);
			}
		}
		return tree;
	}
	
	public List<String> getVirtualMachineNamesForHost(String host) throws Exception {
		List<String> machines = new ArrayList<String>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return null;
		}
		ManagedEntity[] _hosts = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
		if(_hosts != null) {
			logger.info("Obtenidos {} hosts para url:{} user:{} ", new Object[]{_hosts.length, _url, _user});
			for(ManagedEntity _entity : _hosts) {
				HostSystem _host = (HostSystem) _entity;
				if (host.equals(_host.getName())) {
					for(VirtualMachine _vm : _host.getVms()) {
						machines.add(_vm.getName());
					}
				}
			}
		}
		return machines;
	}
	
	
	public List<String> getVirtualMachineNames() throws Exception {
		List<String> _vms = new ArrayList<String>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return _vms;
		}
		try {
			Folder rootFolder = si.getRootFolder();
			ManagedEntity[] _entities = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
			if(_entities == null) {
		    	return _vms;
		    }
			for(ManagedEntity _entity : _entities) {
		    	VirtualMachine _vm = (VirtualMachine) _entity;
		    	if(_vm.getName().startsWith("wbsairback-")) {
		    		continue;
		    	}
		    	_vms.add(_vm.getName());
		    }
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			si.getServerConnection().logout();
		}
		return _vms;
	}
	
	public List<String> getVirtualMachineNames(String storage) throws Exception {
		List<String> _vms = new ArrayList<String>();
		ServiceInstance si = null;
		if(storage == null || storage.isEmpty()) {
			return _vms;
		}
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return _vms;
		}
		try {
			Folder rootFolder = si.getRootFolder();
			Datastore _ds = (Datastore) new InventoryNavigator(rootFolder).searchManagedEntity("Datastore", storage);
			if(_ds == null) {
				return _vms;
		    }
			for(VirtualMachine _vm : _ds.getVms()) {
				if(_vm.getName().startsWith("wbsairback-")) {
		    		continue;
		    	}
				if(!_vm.getConfig().isTemplate()) {
		    		_vms.add(_vm.getName());
		    	}
		    }
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			si.getServerConnection().logout();
		}
		return _vms;
	}
	
	public List<Map<String, String>> getVirtualMachines() throws Exception {
		List<Map<String, String>> _vms = new ArrayList<Map<String, String>>();
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return _vms;
		}
		try {
			Folder rootFolder = si.getRootFolder();
			ManagedEntity[] _entities = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		    if(_entities == null) {
		    	return _vms;
		    }
			for(ManagedEntity _entity : _entities) {
		    	VirtualMachine _vm = (VirtualMachine) _entity;
		    	if(_vm.getName().startsWith("wbsairback-")) {
		    		continue;
		    	}
		    	if(!_vm.getConfig().isTemplate()) {
		    		StringBuilder _sb = new StringBuilder();
			    	Map<String,String> _machine = new HashMap<String, String>();
			    	_machine.put("name", _vm.getName());
			    	for(Datastore _ds : _vm.getDatastores()) {
			    		if(_sb.length() > 0) {
			    			_sb.append(":");
			    		}
			    		_sb.append(_ds.getName());
			    	}
			    	_machine.put("datastore", _sb.toString());
			    	_sb = new StringBuilder();
			    	for(Network _nw : _vm.getNetworks()) {
			    		if(_sb.length() > 0) {
			    			_sb.append(":");
			    		}
			    		_sb.append(_nw.getName());
			    	}
			    	_machine.put("network", _sb.toString());
			    	_sb = new StringBuilder();
			    	for(VirtualMachineSnapshotTree _st : _vm.getSnapshot().getRootSnapshotList()) {
			    		if(_sb.length() > 0) {
			    			_sb.append(":");
			    		}
			    		_sb.append(_st.getName());
			    	}
			    	_machine.put("snapshot", _sb.toString());
			    	_vms.add(_machine);
		    	}
		    }
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			si.getServerConnection().logout();
		}
		return _vms;
	}
	
	public List<Map<String, String>> getVirtualMachines(String storage) throws Exception {
		List<Map<String, String>> _vms = new ArrayList<Map<String, String>>();
		ServiceInstance si = null;
		if(storage == null || storage.isEmpty()) {
			return _vms;
		}
		try {
			si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		} catch(Exception _ex) {
			return _vms;
		}
		try {
			Folder rootFolder = si.getRootFolder();
			Datastore _ds = (Datastore) new InventoryNavigator(rootFolder).searchManagedEntity("Datastore", storage);
			if(_ds == null) {
				return _vms;
		    }
			for(VirtualMachine _vm : _ds.getVms()) {
				if(_vm.getName().startsWith("wbsairback-")) {
		    		continue;
		    	}
				if(!_vm.getConfig().isTemplate()) {
		    		StringBuilder _sb = new StringBuilder();
			    	Map<String,String> _machine = new HashMap<String, String>();
			    	_machine.put("name", _vm.getName());
			    	for(Datastore _vmds : _vm.getDatastores()) {
			    		if(_sb.length() > 0) {
			    			_sb.append(":");
			    		}
			    		_sb.append(_vmds.getName());
			    	}
			    	_machine.put("datastore", _sb.toString());
			    	_sb = new StringBuilder();
			    	for(Network _nw : _vm.getNetworks()) {
			    		if(_sb.length() > 0) {
			    			_sb.append(":");
			    		}
			    		_sb.append(_nw.getName());
			    	}
			    	_machine.put("network", _sb.toString());
			    	_sb = new StringBuilder();
			    	for(VirtualMachineSnapshotTree _st : _vm.getSnapshot().getRootSnapshotList()) {
			    		if(_sb.length() > 0) {
			    			_sb.append(":");
			    		}
			    		_sb.append(_st.getName());
			    	}
			    	_machine.put("snapshot", _sb.toString());
			    	_vms.add(_machine);
		    	}
		    }
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			si.getServerConnection().logout();
		}
		return _vms;
	}
	
	public Map<String, String> getVirtualMachine(String name) throws Exception {
		if(name == null || name.isEmpty()) {
			throw new Exception("invalid virtual machine name");
		}
		
		Map<String,String> _machine = new HashMap<String, String>();
		ServiceInstance si = new ServiceInstance(new URL(this._url), this._user, this._password, true);
		try {
			Folder rootFolder = si.getRootFolder();
			VirtualMachine _vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", name);
			if(_vm == null) {
				throw new Exception("virtual machine not found");
			}
			
			StringBuilder _sb = new StringBuilder();
	    	_machine.put("name", _vm.getName());
	    	for(Datastore _ds : _vm.getDatastores()) {
	    		if(_sb.length() > 0) {
	    			_sb.append(":");
	    		}
	    		_sb.append(_ds.getName());
	    	}
	    	_machine.put("datastore", _sb.toString());
	    	_sb = new StringBuilder();
	    	for(Network _nw : _vm.getNetworks()) {
	    		if(_sb.length() > 0) {
	    			_sb.append(":");
	    		}
	    		_sb.append(_nw.getName());
	    	}
	    	_machine.put("network", _sb.toString());
	    	_sb = new StringBuilder();
	    	VirtualMachineSnapshotTree[] _stree = _vm.getSnapshot().getRootSnapshotList();
		    if(_stree != null) {
		    	for(VirtualMachineSnapshotTree _st : _stree) {
		    		if(_sb.length() > 0) {
		    			_sb.append(":");
		    		}
		    		_sb.append(_st.getName());
		    	}
		    	_machine.put("snapshot", _sb.toString());
		    } else {
		    	_machine.put("snapshot", "");
		    }
	    	
		} catch(Exception _ex) {
			throw new Exception("hypervisor error - " + _ex.getMessage());
		} finally {
			si.getServerConnection().logout();
		}
		return _machine;
	}
	
	private static ArrayList<Integer> getIndependentVirtualDiskKeys(VirtualMachine vm) throws Exception {
		ArrayList<Integer> _diskKeys = new ArrayList<Integer>();
		VirtualDevice[] devices = (VirtualDevice[]) vm.getPropertyByPath("config.hardware.device");
		
		for(int i=0; i<devices.length; i++) {
			if(devices[i] instanceof VirtualDisk) {
				VirtualDisk vDisk = (VirtualDisk) devices[i];
				String diskMode = "";
				VirtualDeviceBackingInfo vdbi = vDisk.getBacking();
				
				if(vdbi instanceof VirtualDiskFlatVer1BackingInfo) {
					diskMode = ((VirtualDiskFlatVer1BackingInfo) vdbi).getDiskMode();
				} else if(vdbi instanceof VirtualDiskFlatVer2BackingInfo) {
					diskMode = ((VirtualDiskFlatVer2BackingInfo)vdbi).getDiskMode();
				} else if(vdbi instanceof VirtualDiskRawDiskMappingVer1BackingInfo) {
					diskMode = ((VirtualDiskRawDiskMappingVer1BackingInfo)vdbi).getDiskMode();
				} else if(vdbi instanceof VirtualDiskSparseVer1BackingInfo) {
					diskMode = ((VirtualDiskSparseVer1BackingInfo)vdbi).getDiskMode();
				} else if(vdbi instanceof VirtualDiskSparseVer2BackingInfo) {
					diskMode = ((VirtualDiskSparseVer2BackingInfo)vdbi).getDiskMode();
				}
				
				if(diskMode.indexOf("independent") != -1) {
					_diskKeys.add(vDisk.getKey());
				}
			}
		}
		return _diskKeys;
	}
	
	private static void trustAllHttpsCertificates() throws Exception {
		KeyStore _store = KeyStore.getInstance("JKS");
    	TrustManagerFactory _tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    	_tmf.init(_store);
    	X509TrustManager x509Manager = (X509TrustManager) _tmf.getTrustManagers()[0];
    	HTTPSTrustManager _httpstm = new HTTPSTrustManager(x509Manager);
    	SSLContext _ctx = SSLContext.getInstance("TLS");
    	javax.net.ssl.SSLSessionContext _sslsctx = _ctx.getServerSessionContext();
    	_sslsctx.setSessionTimeout(0);
    	_ctx.init(null, new TrustManager[] { _httpstm }, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(_ctx.getSocketFactory());
    }
	
	
	private static void trustAllHttpsCertificatesWithoutKeys() throws Exception {
        // Trust manager que no valida nada
        TrustManager[] trustAllCerts = new TrustManager[]{  
                new X509TrustManager() {  
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
                        return null;  
                    }  
  
                    public void checkClientTrusted(  
                            java.security.cert.X509Certificate[] certs, String authType) {  
                    }  
  
                    public void checkServerTrusted(  
                            java.security.cert.X509Certificate[] certs, String authType) {  
                    }  
                }  
        };  
        // Instalamos manager
        SSLContext sc = SSLContext.getInstance("TLS");  
        javax.net.ssl.SSLSessionContext _sslsctx = sc.getServerSessionContext();
        _sslsctx.setSessionTimeout(0);
        sc.init(null, trustAllCerts, new java.security.SecureRandom());  
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());  
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
              return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	
	private static long writeVMDKFile(String localPath, String diskUrl, String cookie, 
			long writtenBytes, long totalBytes) throws IOException {
		HttpsURLConnection _conn = (HttpsURLConnection) new URL(diskUrl).openConnection();
		_conn.setDoInput(true);
		_conn.setDoOutput(true);
		_conn.setAllowUserInteraction(true);
		_conn.setRequestProperty("Cookie",	cookie);
		_conn.connect();
		
		InputStream _in = _conn.getInputStream();
		OutputStream _out = new FileOutputStream(new File(localPath));
		byte[] buf = new byte[102400];
		int len = 0;
		long bytesWritten = 0;
		while((len = _in.read(buf)) > 0) {
			_out.write(buf, 0, len);
			bytesWritten += len;
		}
		_in.close();
		_out.close();
		return bytesWritten;
	}
}
