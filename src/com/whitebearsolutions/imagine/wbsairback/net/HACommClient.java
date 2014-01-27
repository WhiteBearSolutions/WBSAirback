package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.http.HTTPClient;
import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DrbdCmanConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.io.FileUtils;
import com.whitebearsolutions.util.Configuration;

public class HACommClient {
	private Configuration _c;
	private NetworkManager _nm;
	private final static Logger logger = LoggerFactory.getLogger(HACommClient.class);

	
	public HACommClient(Configuration conf) throws Exception {
		this._c = conf;
		this._nm = new NetworkManager(this._c);
	}
	
	public void request(String masterClusterIface, String slaveClusterIface, String[] virtualAddress, String remoteAddress, int _fenceType, Map<String, String> _fenceAttributes, String deviceLun) throws Exception {
		LicenseManager _lm = new LicenseManager();
		if(this._c.getProperty("cluster.request") != null)  {
			throw new Exception("an active request already exists");
		}
		
		boolean inRangeRemote = false;
		boolean inRangeVirtual = false;
		for (String _iface : this._nm.getAvailableInterfaces()) {
			if(this._nm.isNetworkAddress(_iface, NetworkManager.toAddress(remoteAddress))) {
				inRangeRemote=true;
			}
			if(this._nm.isNetworkAddress(_iface, virtualAddress)) {
				inRangeVirtual=true;
			}
		}
		if (!inRangeRemote) {
			throw new Exception("remote address is not in the network range");
		}
		
		if (!inRangeVirtual) {
			throw new Exception("virtual address is not in the network range");
		}
		
		// Obtenemos el id de la lun
		String lunId = null;
		if (deviceLun != null) {
			lunId = VolumeManager.findDiskPathById(deviceLun);
			logger.info("Vamos a configurar un cluster de tipo lun compartida en {} ",lunId);
		} else {
			logger.info("Vamos a configurar un cluster de tipo drbd ");
		}
			
		String masterClusterIp = _nm.getStringAddress(masterClusterIface);
		logger.info("Generando fichero de configuración ha-request.xml ... ");
		StringBuilder xml_content = new StringBuilder();
		xml_content.append("<ha><address.real>");
		xml_content.append(masterClusterIp);
		xml_content.append("</address.real><address.virtual>");
		xml_content.append(virtualAddress[0] + "." + virtualAddress[1] + "." + virtualAddress[2] + "." + virtualAddress[3]);
		xml_content.append("</address.virtual>");
		xml_content.append("<master.iface>");
		xml_content.append(masterClusterIface);
		xml_content.append("</master.iface>");
		if (slaveClusterIface != null && !slaveClusterIface.isEmpty()) {
			xml_content.append("<slave.iface>");
			xml_content.append(slaveClusterIface);
			xml_content.append("</slave.iface>");
		}
		xml_content.append("<fence.type>");
		xml_content.append(_fenceType);
		xml_content.append("</fence.type>");
		if(_fenceAttributes != null) {
			for(String _key : _fenceAttributes.keySet()) {
				xml_content.append("<fence.");
				xml_content.append(_key);
				xml_content.append(">");
				xml_content.append(_fenceAttributes.get(_key));
				xml_content.append("</fence.");
				xml_content.append(_key);
				xml_content.append(">");
			}
		}
		xml_content.append("<pair.uuid>");
		xml_content.append(_lm.getUnitUUID());
		xml_content.append("</pair.uuid>");
		xml_content.append("<pair.address>");
		xml_content.append(remoteAddress);
		xml_content.append("</pair.address>");
		
		// Si tenemos cluster de tipo lun, añadimos la info al fichero xml y creamos el sistema de ficheros ext3 con el contenido de /rdata en la lun
		if (lunId != null && !lunId.equals("")) {
			xml_content.append("<lun>");
			xml_content.append(lunId);
			xml_content.append("</lun>");
			logger.info("Configuración de lun compartida añadida al fichero de configuracion ha-request.xml ");
		}
		
		
		List<String> localgroups = VolumeManager.getLocalDeviceVolumes();
		int i=0;
		if (localgroups != null && localgroups.size() > 0) {
			for (String group : localgroups) {
				xml_content.append("<vloc"+i+">");
				xml_content.append(group);
				xml_content.append("</vloc"+i+">");
				i++;
			}
			logger.info("Configuración de discos locales añadida al fichero de configuracion ha-request.xml ");
		}
		
		xml_content.append("</ha>");
		
		HTTPClient _hc = new HTTPClient(remoteAddress);
		HashMap<String, String> _parameters = new HashMap<String, String>();
		HashMap<String, byte[]> _files = new HashMap<String, byte[]>();
		_parameters.put("type", String.valueOf(CommResponse.TYPE_HA));
		_parameters.put("command", String.valueOf(CommResponse.COMMAND_REQUEST));
		
		_files.put("ha-request.xml", xml_content.toString().getBytes());
		logger.info("ha-request.xml generado con exito. Enviamos al cliente el fichero de configuracion ha-request.xml ..");
		_hc.multipartLoad("/admin/Comm", _parameters, _files);
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			logger.error("El esclavo no devolvió la respuesta esperada (done), devolvio: {}", _reply);
			throw new Exception(_reply);
		}
		
		logger.info("Respuesta correcta del nodo esclavo");
		if (lunId != null && !lunId.equals("")) {
			logger.info("Generamos /rdata en lun compartida");
			HAConfiguration.createRdataSharedLun(lunId);
		}
		
		logger.info("Guardamos fichero de configuracion ...");
		this._c.setProperty("cluster.request", "request");
		this._c.setProperty("pair.address", remoteAddress);
		this._c.setProperty("address.real", masterClusterIp);
		this._c.setProperty("master.iface", masterClusterIface);
		if (lunId != null && !lunId.equals(""))
			this._c.setProperty("lun", lunId);
		
        this._c.store();
        logger.info("Proceso de petición de cluster COMPLETADO.");
	}
	
	
	/**
	 * Envía el fstab: de cluster maestro a su esclavo
	 * @throws Exception
	 */
	public static void sendFsTab() throws Exception {
		LicenseManager _lm = new LicenseManager();
		Configuration _auxc = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		
		HTTPClient _hc = new HTTPClient(_auxc.getProperty("pair.address"));
		
		HashMap<String, String> _parameters = new HashMap<String, String>();
		HashMap<String, byte[]> _files = new HashMap<String, byte[]>();
		_parameters.put("type", String.valueOf(CommResponse.TYPE_HA));
		_parameters.put("command", String.valueOf(CommResponse.COMMAND_SEND_FSTAB));
		_parameters.put("uuid", _lm.getUnitUUID());
		String fstab = FileUtils.fileToString(WBSAirbackConfiguration.getFileFstab());
		_files.put("fstab", fstab.getBytes());
		_hc.multipartLoad("/admin/Comm", _parameters, _files);
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
	}
	
	public static void sendAddVolume(int storage_type, int volume_type, int fs_type, String group, String name, int size, int size_units, String compression, boolean encryption, boolean dedup, double percent_snap) throws Exception {
		sendFsTab();
		
		LicenseManager _lm = new LicenseManager();
		Configuration _auxc = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		
		HTTPClient _hc = new HTTPClient(_auxc.getProperty("pair.address"));
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/admin/Comm?type=");
		_sb.append(CommResponse.TYPE_HA);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_ADD_VOLUME);
		_sb.append("&uuid=");
		_sb.append(_lm.getUnitUUID());
		_sb.append("&storage_type=");
		_sb.append(storage_type);
		_sb.append("&lv_type=");
		_sb.append(volume_type);
		_sb.append("&fs_type=");
		_sb.append(fs_type);
		_sb.append("&group=");
		_sb.append(group);
		_sb.append("&name=");
		_sb.append(name);
		_sb.append("&size=");
		_sb.append(size);	
		_sb.append("&size_units=");
		_sb.append(size_units);
		_sb.append("&compression=");
		_sb.append(compression);
		_sb.append("&encryption=");
		_sb.append(encryption);
		_sb.append("&deduplication=");
		_sb.append(dedup);
		_sb.append("&percent_snap=");
		_sb.append(percent_snap);
		_hc.load(_sb.toString());
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
	}
	
	public static void sendLoginExternalTarget(int[] address, String target, String method, String user, String password) throws Exception {
		LicenseManager _lm = new LicenseManager();
		Configuration _auxc = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		
		HTTPClient _hc = new HTTPClient(_auxc.getProperty("pair.address"));
		
		String sAddress = "";
		for (int i=0;i<address.length;i++) {
			if (i == 0)
				sAddress+=address[i];
			else 
				sAddress+=":"+address[i];
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/admin/Comm?type=");
		_sb.append(CommResponse.TYPE_HA);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_LOGIN_EXTERNAL_TARGET);
		_sb.append("&uuid=");
		_sb.append(_lm.getUnitUUID());
		_sb.append("&address=");
		_sb.append(sAddress);
		_sb.append("&target=");
		_sb.append(target);
		_sb.append("&method=");
		_sb.append(method);
		_sb.append("&user=");
		_sb.append(user);
		_sb.append("&password=");
		_sb.append(password);
		_hc.load(_sb.toString());
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
	}
	
	public static void sendLogoutExternalTarget(int[] address, String iqn) throws Exception {
		LicenseManager _lm = new LicenseManager();
		Configuration _auxc = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		
		HTTPClient _hc = new HTTPClient(_auxc.getProperty("pair.address"));
		
		String sAddress = "";
		for (int i=0;i<address.length;i++) {
			if (i == 0)
				sAddress+=address[i];
			else 
				sAddress+=":"+address[i];
		}
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/admin/Comm?type=");
		_sb.append(CommResponse.TYPE_HA);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_LOGOUT_EXTERNAL_TARGET);
		_sb.append("&uuid=");
		_sb.append(_lm.getUnitUUID());
		_sb.append("&address=");
		_sb.append(sAddress);
		_sb.append("&iqn=");
		_sb.append(iqn);
		_hc.load(_sb.toString());
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
	}
	
	public static void sendExtendVolume(String _type, String group, String name, Integer _size, Integer _size_units, Double data_size, Double total_reservation, Double data_reservation, String snapshot_hourly_status, String snapshot_hourly_retention, String snapshot_daily_status, String snapshot_daily_retention, String snapshot_daily_hour, String snapshot_manual_remove) throws Exception {
		LicenseManager _lm = new LicenseManager();
		Configuration _auxc = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		
		HTTPClient _hc = new HTTPClient(_auxc.getProperty("pair.address"));
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/admin/Comm?type=");
		_sb.append(CommResponse.TYPE_HA);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_EXTEND_VOLUME);
		_sb.append("&uuid=");
		_sb.append(_lm.getUnitUUID());
		_sb.append("&typeVol=");
		_sb.append(_type);
		_sb.append("&group=");
		_sb.append(group);
		_sb.append("&name=");
		_sb.append(name);
		_sb.append("&size=");
		_sb.append(_size);
		_sb.append("&size_units=");
		_sb.append(_size_units);
		if (data_size != null) {
			_sb.append("&data_size=");
			_sb.append(data_size);
		}
		if (total_reservation != null) {
			_sb.append("&total_reservation=");
			_sb.append(total_reservation);
		}
		if (data_reservation != null) {
			_sb.append("&data_reservation=");
			_sb.append(data_reservation);
		}
		
		if (snapshot_hourly_status != null) {
			_sb.append("&snapshot_hourly_status=");
			_sb.append(snapshot_hourly_status);
		}
		if (snapshot_hourly_retention != null) {
			_sb.append("&snapshot_hourly_retention=");
			_sb.append(snapshot_hourly_retention);
		}
		if (snapshot_daily_status != null) {
			_sb.append("&snapshot_daily_status="); 
			_sb.append(snapshot_daily_status);
		}
		if (snapshot_daily_retention != null) {
			_sb.append("&snapshot_daily_retention=");
			_sb.append(snapshot_daily_retention);
		}
		if (snapshot_daily_hour != null) {
			_sb.append("&snapshot_daily_hour=");
			_sb.append(snapshot_daily_hour);
		}
		if (snapshot_manual_remove != null) {
			_sb.append("&snapshot_manual_remove=");
			_sb.append(snapshot_manual_remove);
		}
		_hc.load(_sb.toString());
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
	}
	
	
	public static void sendRemoveVolume(String group, String name) throws Exception {
		HACommClient.sendFsTab();
		
		LicenseManager _lm = new LicenseManager();
		Configuration _auxc = new Configuration(new File(WBSAirbackConfiguration.getFileImagineHaRequest()));
		
		HTTPClient _hc = new HTTPClient(_auxc.getProperty("pair.address"));
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/admin/Comm?type=");
		_sb.append(CommResponse.TYPE_HA);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_DEL_VOLUME);
		_sb.append("&uuid=");
		_sb.append(_lm.getUnitUUID());
		_sb.append("&group=");
		_sb.append(group);
		_sb.append("&name=");
		_sb.append(name);
		_hc.load(_sb.toString());
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
	}
	
	
	public void requestConfirm() throws Exception {
		if(!this._c.checkProperty("cluster.request", "request")) {
			throw new Exception("product has no active request");
		}
		
		if(!new File(WBSAirbackConfiguration.getFileImagineHaRequest()).canWrite()) {
			throw new Exception("cannot remove request from system");
		}
		
		int _fenceType = DrbdCmanConfiguration.FENCE_DRAC;
		Map<String, String> _fenceAttributes = new HashMap<String, String>();
		Map<String, String> values = getValues(WBSAirbackConfiguration.getFileImagineHaRequest());
		if(!values.containsKey("address.virtual")) {
			throw new Exception("failed to determine the virtual address");
		}		
		if(!values.containsKey("address.real")) {
			throw new Exception("failed to determine the remote address");
		}
		if(!values.containsKey("fence.type")) {
			throw new Exception("failed to determine fencing type on slave");
		}
		
		try {
			_fenceType = Integer.parseInt(values.get("fence.type"));
		} catch(NumberFormatException _ex) {
			throw new Exception("failed to determine fencing type on slave");
		}
		
		LicenseManager _lm = new LicenseManager();
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("type=");
		_sb.append(CommResponse.TYPE_HA);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_REQUEST_CONFIRM_STAGE1);
		_sb.append("&virtual=");
		_sb.append(values.get("address.virtual"));
		_sb.append("&fence.type=");
		_sb.append(_fenceType);
		_sb.append("&uuid=");
		_sb.append(_lm.getUnitUUID());
		for(String _key : values.keySet()) {
			if(_key.startsWith("fence.") && !_key.equalsIgnoreCase("fence.type")) {
				_fenceAttributes.put(_key.substring(6), values.get(_key));
				_sb.append("&");
				_sb.append(_key);
				_sb.append("=");
				_sb.append(values.get(_key));
			}
		}
		
		HTTPClient _hc = new HTTPClient(values.get("address.real"));
		_hc.load(HTTPClient.POST, "/admin/Comm", _sb.toString(), null);
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
		
		_sb = new StringBuilder();
		_sb.append("/admin/Comm?type=");
		_sb.append(CommResponse.TYPE_HA);
		_sb.append("&command=");
		_sb.append(CommResponse.COMMAND_REQUEST_CONFIRM_STAGE2);
		_sb.append("&virtual=");
		_sb.append(values.get("address.virtual"));
		HAConfiguration.setSlave(values.get("address.virtual"), values.get("address.real"), _fenceType, _fenceAttributes);
		_hc.load(_sb.toString());
		
		_reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
		
		this._c.removeProperty("cluster.request");
		this._c.setProperty("cluster.virtual", values.get("address.virtual"));
		this._c.setProperty("cluster.status", "slave");
		
		this._c.store();		
	}
	
	public void requestReject() throws Exception {
		if(!this._c.checkProperty("cluster.request", "request")) {
			throw new Exception("product has no active request");
		}
		
		if(!new File(WBSAirbackConfiguration.getFileImagineHaRequest()).canWrite()) {
			throw new Exception("cannot remove request from system");
		}

		HashMap<String, String> values = getValues(WBSAirbackConfiguration.getFileImagineHaRequest());
		if(!values.containsKey("pair.address")) {
			throw new Exception("failed to determine the remote address");
		}
		
		HTTPClient _hc = new HTTPClient(values.get("pair.address"));
		_hc.load("/admin/Comm?type=" + CommResponse.TYPE_HA + "&command=" + CommResponse.COMMAND_REQUEST_REJECT);
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
		
		new File(WBSAirbackConfiguration.getFileImagineHaRequest()).delete();
	}
	
	public void breakRequest() throws Exception {
		if(!this._c.checkProperty("cluster.status", "master") && !this._c.checkProperty("cluster.status", "slave")) {
			throw new Exception("this product is not currently grouped");
		}		
		
		StringBuilder xml_content = new StringBuilder();
		xml_content.append("<ha><address.real>");
		xml_content.append(this._c.getProperty("address.real"));
		xml_content.append("</address.real></ha>");
		
		HTTPClient _hc = new HTTPClient(this._c.getProperty("pair.address"));
		
		HashMap<String, String> _parameters = new HashMap<String, String>();
		HashMap<String, byte[]> _files = new HashMap<String, byte[]>();
		_parameters.put("type", String.valueOf(CommResponse.TYPE_HA));
		_parameters.put("command", String.valueOf(CommResponse.COMMAND_BREAK));
		_files.put("ha-break.xml", xml_content.toString().getBytes());
		_hc.multipartLoad("/admin/Comm", _parameters, _files);
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
		
		this._c.setProperty("cluster.request", "break");
		this._c.store();
	}
	
	public void breakRequestConfirm() throws Exception {
		if(!this._c.checkProperty("cluster.request", "break")) {
			throw new Exception("this product has no active break request");
		}
		
		if(!new File(WBSAirbackConfiguration.getFileImagineHaBreak()).canWrite()) {
			throw new Exception("cannot remove break request from system");
		}
		
		HashMap<String, String> values = getValues(WBSAirbackConfiguration.getFileImagineHaBreak());
		if(!values.containsKey("address.real")) {
			throw new Exception("failed to determine the remote address");
		}
		
		DrbdCmanConfiguration.killCmanAssociatedProcesses();
		HTTPClient _hc = new HTTPClient(values.get("address.real"));
		_hc.load("/admin/Comm?type=" + CommResponse.TYPE_HA + "&command=" + CommResponse.COMMAND_BREAK_CONFIRM);
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
		    try {
		    	ServiceManager.start(ServiceManager.CLUSTER);
		    } catch (Exception ex) {}
			throw new Exception(_reply);
		}

		HAConfiguration.setStandalone("slave");
	}
	
	public void breakRequestReject() throws Exception {
		if(!this._c.checkProperty("cluster.request", "break")) {
			throw new Exception("this product has no active break request");
		}
		
		if(!new File(WBSAirbackConfiguration.getFileImagineHaBreak()).canWrite()) {
			throw new Exception("cannot remove break request from system");
		}
		
		HashMap<String, String> values = getValues(WBSAirbackConfiguration.getFileImagineHaBreak());
		if(!values.containsKey("address.real")) {
			throw new Exception("failed to determine the remote address");
		}
		
		HTTPClient _hc = new HTTPClient(values.get("address.real"));
		_hc.load("/admin/Comm?type=" + CommResponse.TYPE_HA + "&command=" + CommResponse.COMMAND_BREAK_REJECT);
		
		String _reply = new String(_hc.getContent());
		if(!_reply.contains("done")) {
			throw new Exception(_reply);
		}
		
		if(new File(WBSAirbackConfiguration.getFileImagineHaRequest()).canWrite()) {
			new File(WBSAirbackConfiguration.getFileImagineHaRequest()).delete();
		}
		
		new File(WBSAirbackConfiguration.getFileImagineHaBreak()).delete();
	}
	
	public void forceBreak() throws Exception {
		HAConfiguration.setStandalone(null);
	}
	
	public static void forgetRequest() throws Exception {
		
		if(new File(WBSAirbackConfiguration.getFileImagineHaRequest()).canWrite()) {
			new File(WBSAirbackConfiguration.getFileImagineHaRequest()).delete();
			logger.info("Fichero de configuracion ha-request.xml borrado");
		}
		if(new File(WBSAirbackConfiguration.getFileImagineHaBreak()).canWrite()) {
			new File(WBSAirbackConfiguration.getFileImagineHaBreak()).delete();
			logger.info("Fichero de configuracion ha-break.xml borrado");
		}
		
		Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
		_c.removeProperty("cluster.request");
		_c.removeProperty("cluster.remote");
	}
	
	public static HashMap<String, String> getValues(String file) throws Exception {
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
