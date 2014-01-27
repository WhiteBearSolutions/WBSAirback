package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.disk.fs.FileSystemManager;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class HACommServer {
	private Configuration _c;
	
	protected HACommServer(Configuration conf) {
		this._c = conf;
	}
	
	protected String request(byte[] request) throws Exception {
		if(this._c.checkProperty("cluster.status", "master") || this._c.checkProperty("cluster.status", "slave")) {
			return "remote product is already grouped";
		}
		
		if(this._c.checkProperty("cluster.request", "request")) {
			return "remote product has another active request";
		}
		
		ByteArrayInputStream _bais = new ByteArrayInputStream(request);
		FileOutputStream _fos = new FileOutputStream(WBSAirbackConfiguration.getFileImagineHaRequest());
		while(_bais.available() > 0) {
			_fos.write(_bais.read());
		}
		_fos.close();
		_bais.close();
		
		HashMap<String, String> values = getValues(WBSAirbackConfiguration.getFileImagineHaRequest());
		Configuration _auxc = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		
		for (String key : values.keySet())
			_auxc.setProperty(key, values.get(key));
		
		_auxc.setProperty("cluster.request", "request");
		_auxc.setProperty("cluster.remote", values.get("address.real"));
		_auxc.setProperty("pair.address", values.get("address.real"));
		if (values.get("slave.iface") != null && !values.get("slave.iface").isEmpty()) {
			Configuration conf = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
			NetworkManager nm = new NetworkManager(conf);
			if (!nm.isConfiguredInterface(values.get("slave.iface")))
				return values.get("slave.iface")+" is not a configured interface on remote product";
			
			_auxc.setProperty("slave.iface", values.get("slave.iface"));
		}
		
		int i=0;
		List<String> remote_vols = new ArrayList<String>();
		while (_auxc.hasProperty("vloc"+i)) {
			remote_vols.add(_auxc.getProperty("vloc"+i));
			i++;
		}
		List<String> local_vols = VolumeManager.getLocalDeviceVolumes();
		
		if ((remote_vols != null && remote_vols.size()>0) && (local_vols == null || local_vols.size()<=0))
			return "remote product does not have the same local device group and volumes";
		
		if ((local_vols != null && local_vols.size()>0) && remote_vols.size()<=0)
			return "remote product does not have the same local device groups and volumes";
		
		if (remote_vols.size() != local_vols.size())
			return "remote product does not have the same local device groups and volumes";
		
		remote_vols.removeAll(local_vols);
		if (remote_vols.size()>0)
			return "remote product does not have the same local device groups and volumes";
		
		_auxc.store();
		
		return "done";
	}
	
	
	/**
	 * Recibe un fstab remoto y lo almacena en un directorio temporal => esclavo en cluster
	 * @param remoteAddress
	 * @param request
	 * @throws Exception
	 */
	protected String receiveFsTab(String remoteAddress, String uuid, byte[] request) throws Exception {
		Configuration haConf = null;
		try {
			haConf = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		} catch (Exception ex) {
			return "Error receiving fstab. Cannot load imagine ha configuration file";
		}
		if(!haConf.checkProperty("pair.uuid", uuid)) {
			return "Cannot receive fstab, uuid of sender is not equivalent to the grouped product";
		}
		ByteArrayInputStream _bais = new ByteArrayInputStream(request);
		FileOutputStream _fos = new FileOutputStream(WBSAirbackConfiguration.getFileTmpFstab());
		while(_bais.available() > 0) {
			_fos.write(_bais.read());
		}
		_fos.close();
		_bais.close();
		
		if (!HAConfiguration.isActiveNode()) {
			try {
				Command.systemCommand("/etc/wbsairback-volumes-ha restart");
			} catch (Exception ex) {}
		}
		return "done";
	}
	
	
	/**
	 * Añade un volumen en un esclavo, al igual que se haya hecho en el nodo maestro asociado
	 * @param remoteAddr
	 * @param uuid
	 * @param storage_type
	 * @param lv_type
	 * @param fs_type
	 * @param group
	 * @param name
	 * @param size
	 * @param size_units
	 * @param compression
	 * @param encryption
	 * @param deduplication
	 * @return
	 */
	protected String addVolume(String remoteAddr, String uuid, String storage_type, String lv_type, String fs_type, String group, String name, String size, String size_units, String compression, String encryption, String deduplication, String percent_snap) {
		Configuration haConf = null;
		try {
			haConf = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		} catch (Exception ex) {
			return "Error replicating volume on slave. Cannot load imagine ha configuration file";
		}
		if(!haConf.checkProperty("pair.uuid", uuid)) {
			return "Error replicating volume on slave: uuid of sender is not equivalent to the grouped product";
		}
		try {
			VolumeManager.addLogicalVolume(Integer.parseInt(storage_type), Integer.parseInt(lv_type), Integer.parseInt(fs_type), group, name, Integer.parseInt(size), Integer.parseInt(size_units), compression, Boolean.parseBoolean(encryption), Boolean.parseBoolean(deduplication), Double.parseDouble(percent_snap));
			if (!VolumeManager.isLocalDeviceGroup(group))
				VolumeManager.umountLogicalVolume(group, name);
		} catch (Exception ex) {
			return "Error replicating volume on slave: "+ex.getMessage();
		}
		return "done";
	}
	
	/**
	 * 
	 * @param remoteAddr
	 * @param uuid
	 * @param group
	 * @param name
	 * @return
	 */
	protected String delVolume(String remoteAddr, String uuid, String group, String name) {
		Configuration haConf = null;
		try {
			haConf = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		} catch (Exception ex) {
			return "Error deleting volume on slave. Cannot load imagine ha configuration file";
		}
		if(!haConf.checkProperty("pair.uuid", uuid)) {
			return "Error deleting volume on slave: uuid of sender is not equivalent to the grouped product";
		}
		try {
			VolumeManager _vm = new VolumeManager(this._c);
			_vm.removeLogicalVolume(group, name);
		} catch (Exception ex) {
			return "Error deleting volume on slave: "+ex.getMessage();
		}
		return "done";
	}
	
	protected String expandVolume(String remoteAddr, String uuid, String type, String group, String name, String size, String data_size, String total_reservation, String data_reservation, String size_units, String snapshot_hourly_status, String snapshot_hourly_retention , String snapshot_daily_status, String snapshot_daily_retention , String snapshot_daily_hour, String snapshot_manual_remove) {
		Configuration haConf = null;
		try {
			haConf = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		} catch (Exception ex) {
			return "Error extending volume on slave. Cannot load imagine ha configuration file";
		}
		if(!haConf.checkProperty("pair.uuid", uuid)) {
			return "Error extending volume on slave: uuid of sender is not equivalent to the grouped product";
		}
		
		try {
			if(FileSystemManager.equalsFilesystemType(FileSystemManager.FS_ZFS, type) ||
					FileSystemManager.equalsFilesystemType(FileSystemManager.FS_BTRFS, type)) {
				if("enabled".equals(snapshot_hourly_status)) {
					int _retention = 0;
					try {
						_retention = Integer.parseInt(snapshot_hourly_retention);
					} catch(NumberFormatException _ex) {
						return "Error extending volume on slave: Incorrect hourly retention";
					}
					VolumeManager.setPlannedSnapshot(group, name, VolumeManager.LV_SNAPSHOT_HOURLY, 0, _retention);
				} else {
					VolumeManager.removePlannedSnapshot(group, name, VolumeManager.LV_SNAPSHOT_HOURLY);
				}
				
				if("enabled".equals(snapshot_daily_status)) {
					int _retention = 0, _hour = 0;
					try {
						_retention = Integer.parseInt(snapshot_daily_retention);
					} catch(NumberFormatException _ex) {
						return "Error extending volume on slave: incorrect snapshot retention";
					}
					if(snapshot_daily_hour != null && !snapshot_daily_hour.isEmpty()) {
						try {
							_hour = Integer.parseInt(snapshot_daily_hour);
						} catch(NumberFormatException _ex) {
							throw new Exception();
						}
					}
					VolumeManager.setPlannedSnapshot(group, name, VolumeManager.LV_SNAPSHOT_DAILY, _hour, _retention);
				} else {
					VolumeManager.removePlannedSnapshot(group, name, VolumeManager.LV_SNAPSHOT_DAILY);
				}
				
				if (snapshot_manual_remove != null && "true".equals(snapshot_manual_remove))
					VolumeManager.setSnapshotManualRemove(group, name, true);
				else
					VolumeManager.setSnapshotManualRemove(group, name, false);
			}
			Double data_size_d = null;
			if (data_size != null && !data_size.equals(""))
				data_size_d = Double.parseDouble(data_size);
			Double total_reservation_d = null;
			if (total_reservation != null && !total_reservation.equals(""))
				total_reservation_d = Double.parseDouble(total_reservation);
			Double data_reservation_d = null;
			if (data_reservation != null && !data_reservation.equals(""))
				data_reservation_d = Double.parseDouble(data_reservation);
			
			Integer _size = Integer.parseInt(size);
			VolumeManager.extendLogicalVolume(group, name, _size, Integer.parseInt(size_units), data_size_d, total_reservation_d, data_reservation_d);
			 
			return "done";
		} catch (Exception ex) {
			return "Error extending volume on slave: "+ex.getMessage(); 
		}
	}
	
	
	protected String loginExternalVolume(String remoteAddr, String uuid, String address, String target, String method, String user, String password) {
		Configuration haConf = null;
		try {
			haConf = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		} catch (Exception ex) {
			return "Error on login on external volume on slave. Cannot load imagine ha configuration file";
		}
		if(!haConf.checkProperty("pair.uuid", uuid)) {
			return "Error on login on external volume on slave: uuid of sender is not equivalent to the grouped product";
		}
		
		try {
			String[] sAddr = address.split(":");
			int[] addr = new int[4];
			for (int i=0;i<sAddr.length;i++)
				addr[i]=Integer.parseInt(sAddr[i]);
		
			ISCSIManager.loginExternalTarget(addr, target, method, user, password);
	
			return "done";
		} catch (Exception ex) {
			return "Error extending volume on slave: "+ex.getMessage(); 
		}
	}
	
	
	protected String logoutExternalVolume(String remoteAddr, String uuid, String address, String iqn) {
		Configuration haConf = null;
		try {
			haConf = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		} catch (Exception ex) {
			return "Error on login on external volume on slave. Cannot load imagine ha configuration file";
		}
		if(!haConf.checkProperty("pair.uuid", uuid)) {
			return "Error on login on external volume on slave: uuid of sender is not equivalent to the grouped product";
		}
		
		try {
			String[] sAddr = address.split(":");
			int[] addr = new int[4];
			for (int i=0;i<sAddr.length;i++)
				addr[i]=Integer.parseInt(sAddr[i]);
		
			ISCSIManager.logoutExternalTarget(addr, iqn);
	
			return "done";
		} catch (Exception ex) {
			return "Error extending volume on slave: "+ex.getMessage(); 
		}
	}
	
	
	protected String requestConfirmStage1(String virtualAddress, String remoteAddress, int _fenceType, Map<String, String> _fenceAttributes, String uuid) {
		if(!this._c.checkProperty("cluster.request", "request")) {
			return "remote product has no active request";
		}
		if(virtualAddress == null || virtualAddress.isEmpty()) {
			return "virtual address has not provided";
		}
		try {
			Configuration haConf = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
			haConf.setProperty("pair.uuid", uuid);
			HAConfiguration.setMasterStage1(virtualAddress, haConf.getProperty("pair.address"), _fenceType, _fenceAttributes);
			return "done";				    				
		} catch(Exception _ex) {
			return _ex.getMessage();
		}
	}
	
	protected String requestConfirmStage2(String virtualAddress, String remoteAddress) {
		if(!this._c.checkProperty("cluster.request", "request")) {
			return "remote product has no active request";
		}
		if(virtualAddress == null || virtualAddress.isEmpty()) {
			return "virtual address has not provided";
		}
		try {
			HAConfiguration.setMasterStage2(virtualAddress, remoteAddress);
			
			this._c.removeProperty("cluster.request");
			this._c.setProperty("cluster.virtual", virtualAddress);
			this._c.setProperty("cluster.status", "master");
			this._c.store();
			
			return "done";				    				
		} catch(Exception _ex) {
			return _ex.getMessage();
		}
	}
	
	protected String requestReject(String remoteAddress) {
		if(!this._c.checkProperty("cluster.request", "request")) {
			return "remote product has no active request";
		}
		new File(WBSAirbackConfiguration.getFileImagineHaRequest()).delete();
		return "done";
	}
	
	protected String breakRequest(String remoteAddress, byte[] request) throws Exception {
		if(!this._c.checkProperty("cluster.status", "master") && !this._c.checkProperty("cluster.status", "slave")) {
			return "remote product is not currently grouped";
		}
		
		ByteArrayInputStream _bais = new ByteArrayInputStream(request);
		FileOutputStream _fos = new FileOutputStream(WBSAirbackConfiguration.getFileImagineHaBreak());
		while(_bais.available() > 0) {
			_fos.write(_bais.read());
		}
		_fos.close();
		_bais.close();
		
		this._c.setProperty("cluster.request", "break");
        this._c.store();
        
        return "done";
	}
	
	protected String breakRequestConfirm(String remoteAddress) {
		if(!this._c.checkProperty("cluster.request", "break")) {
			return "remote product has no active break request";
		}
		try {
			HAConfiguration.setStandalone("master");
			return "done";				    				
		} catch(Exception _ex) {
			return _ex.getMessage();
		}
	}
	
	protected String breakRequestReject(String remoteAddress) {
		if(!this._c.checkProperty("cluster.request", "break")) {
			return "remote product has no active break request";
		}
		try {
	        this._c.removeProperty("cluster.request");
	        this._c.store();
		} catch(Exception _ex) {
			return _ex.getMessage();
		}
		new File(WBSAirbackConfiguration.getFileImagineHaBreak()).delete();
		return "done";
	}
	
	private HashMap<String, String> getValues(String file) throws Exception {
		HashMap<String, String> values = new HashMap<String, String>(); 
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document _doc = db.parse(new File(file));
        NodeList nl = _doc.getDocumentElement().getChildNodes();
	    for(int i = nl.getLength(); --i >= 0 ; ) {
	    	if(nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
	    		Element e = (Element) nl.item(i);
	    		values.put(e.getNodeName(), e.getTextContent());	    		
	    	}
        }
	    return values;
	}
}
