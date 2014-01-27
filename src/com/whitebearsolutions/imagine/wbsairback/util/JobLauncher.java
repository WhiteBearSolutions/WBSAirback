package com.whitebearsolutions.imagine.wbsairback.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.imagine.wbsairback.virtual.HypervisorManager;
import com.whitebearsolutions.util.Configuration;


public class JobLauncher {
	
	public static void runPreviousJob(String _job, String _previous_job) throws Exception {
		File _f = new File(WBSAirbackConfiguration.getFileConfiguration());
		if(!_f.exists()) {
			System.err.println("WBSAirback configuration file not found");
			throw new Exception("WBSAirback configuration file not found");
		}
		
		BackupOperator _bc = new BackupOperator(new Configuration(_f));
		_bc.runNextJob(_previous_job, _job);
	}
	
	public static int runHypervisorJob(String _job, String _hypervisor) throws Exception {
		int _status = 0, _mode = 0;
		HypervisorManager _hm = HypervisorManager.getInstance(_hypervisor);
		Map<String, Object> _hjob = _hm.getHypervisorJob(_job);
		if(!_hjob.containsKey("storage") || _hjob.get("storage") == null || !String.valueOf(_hjob.get("storage")).contains("/")) {
			throw new Exception("invalid hypervisor job storage parameter");
		}
		
		String[] _value = String.valueOf(_hjob.get("storage")).split("/");
		if(!VolumeManager.isMount(_value[0], _value[1])) {
			throw new Exception("logical volume is not a NAS type");
		}
		String address = NetworkManager.getPublicAddress();
		String _path = VolumeManager.getLogicalVolumeMountPath(_value[0], _value[1]);
		if(_hjob.containsKey("mode")) {
			if("store".equalsIgnoreCase(String.valueOf(_hjob.get("mode")))) {
				_mode = 1;
			}
		}
		System.out.println("Hypervisor: " + _hypervisor);
		System.out.println("Hypervisor-job: " + _job);
		System.out.println("Hypervisor-job storage: " + _hjob.get("storage"));
		System.out.println("Hypervisor-job backup mode: " + _hjob.get("mode"));
		/*try {
			Command.systemCommand("find "+WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+_value[0]+"/"+_value[1]+"/ -iname \"*.vhd\" | xargs sudo rm");
			System.out.println("Old backed up vhds removed");
		} catch (Exception ex) {}*/
		if(_hjob.containsKey("ds")) {
			@SuppressWarnings("unchecked")
			List<String> _job_dss = (List<String>) _hjob.get("ds");
			for(String _ds : _job_dss) {
				System.out.println("  Backing up datastore [" + _ds + "]");
				try {
					for(String _vm : _hm.getVirtualMachineNames(_ds)) {
						_status = backupMachine(_vm, _hm, _value[0], _value[1], _path, address, _mode);
					}
				} catch(Exception _ex) {
					System.out.println("    datastore backup error:" + _ex.getMessage());
				}
			}
		}
		if(_hjob.containsKey("vm")) {
			@SuppressWarnings("unchecked")
			List<String> _job_vms = (List<String>) _hjob.get("vm");
			for(String _vm : _job_vms) {
				_status = backupMachine(_vm, _hm, _value[0], _value[1], _path, address, _mode);
			}
		}
		return _status;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean _hypervisor = false;
		String _job = null, _value = null;
		try {
			Thread.sleep(20000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		for(int j = 0; j < args.length; j++) {
			if(args[j].equals("-v")) {
				if((j + 1) < args.length) {
					_value = args[j + 1];
					System.out.println("Previous Job is: "+_value);
					j++;
				} else {
					System.out.println("error: previous job not defined");
					System.exit(1);
				}
			} else if(args[j].equals("-h")) {
				_hypervisor = true;
				if((j + 1) < args.length) {
					_value = args[j + 1];
					j++;
				} else {
					System.out.println("error: hypervisor not defined");
					System.exit(1);
				}
			} else if(args[j] != null) {
				_job = args[j];
				System.out.println("Next Job is: "+_job);
			}
		}
		if(_job != null) {
			try {
				if(!_hypervisor) {
					runPreviousJob(_job, _value);
				} else {
					int _status = runHypervisorJob(_job, _value);
					if(_status > 0) {
						System.exit(_status);
					}
				}
			} catch(Exception _ex) {
				System.out.println("error: " + _ex.getMessage());
				System.exit(1);
			}
		} else {
			System.err.println("Error: No job specified");
		}
	}
	
	private static int backupMachine(String _vm, HypervisorManager _hm, String group, String volume, String _path, String address, int _mode) {
		System.out.print("  Backing up [" + _vm + "]: ");
		switch(_mode) {
			default: {
					try {
						_hm.downloadVirtualMachineOVF(_vm, _path);
						System.out.println("done");
						return 0;
					} catch(Exception _ex) {
						System.out.println(_ex.getMessage());
						return 1;
					}
				}
			case 1: {
					try {
						LicenseManager _lm = new LicenseManager();
						StringBuilder _sb = new StringBuilder();
						_sb.append(_lm.getUnitUUID());
						_sb.append("-");
						_sb.append(group);
						_sb.append("-");
						_sb.append(volume);
						String _host = _hm.getHostForVirtualMachine(_vm);
						if(!_hm.existsNFSStore(_sb.toString(), _host, address, _path)) {
							if(!ShareManager.isShare(ShareManager.NFS, group, volume)) {
								/*
								Map<String, String> _attributes = new HashMap<String, String>();
								_attributes.put("squash", "true");
								ShareManager.addShare(group, volume, ShareManager.NFS, null);
								*/
								StringBuilder _exception = new StringBuilder();
								_exception.append("logical volume [");
								_exception.append(group);
								_exception.append("/");
								_exception.append(volume);
								_exception.append("] is not an NFS share");
								throw new Exception(_exception.toString());
							}
							_hm.addNFSStore(_sb.toString(), _host, address, _path);
						}

						_hm.cloneVirtualMachine(_vm, _sb.toString());
						System.out.println("done");
						return 0;
					} catch(Exception _ex) {
						System.out.println(_ex.getMessage());
						return 1;
					}
				}
		}
	}
}
