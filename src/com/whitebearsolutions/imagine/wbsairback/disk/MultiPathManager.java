package com.whitebearsolutions.imagine.wbsairback.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.util.Command;

public class MultiPathManager {
    private static File _multipath_directory;
    
    static {
    	_multipath_directory = new File(WBSAirbackConfiguration.getDirectoryMultipath());
        if(!_multipath_directory.exists()) {
        	_multipath_directory.mkdirs();
        }
    }
    
    /**
     * Crea el fichero de multipath
     * @throws Exception
     */
    public static void enableMultipath() throws Exception{
    	Document _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    	Element _multipath = _doc.createElement("multipath");
		Element _enabled = _doc.createElement("enabled");
		_enabled.setTextContent("true");
		_multipath.appendChild(_enabled);
    	_doc.appendChild(_multipath);
    	Source source = new DOMSource(_doc);
    	File _f = new File(_multipath_directory + "/multipath.xml");
        Result result = new StreamResult(_f);
    	FileLock _fl = new FileLock(_f);
        Transformer _t = TransformerFactory.newInstance().newTransformer();
    	try {
    		_fl.lock();
	    	_t.transform(source, result);
    	} finally {
    		_fl.unlock();
    	}
    	
    	initializeMultipath();
    }
    
    
    /**
     * Desactiva el multipath
     * @throws Exception
     */
    public static void disableMultipath() throws Exception{
    	File _f = new File(_multipath_directory + "/multipath.xml");
    	if (_f.exists())
    		_f.delete();
    	try {
    		Command.systemCommand("multipath -F");
    		ServiceManager.stop(ServiceManager.MULTIPATHD);
    		Command.systemCommand("modprobe -r dm-multipath");
    	} catch (Exception ex) {}
    }
    
    
    /**
     * Comprueba si está activado el multipath 
     * @return
     * @throws Exception
     */
    public static boolean isMultipathEnabled() throws Exception {
    	boolean enb = false;
    	File _f = new File(_multipath_directory + "/multipath.xml");
    	if (_f.exists()) {
    		DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document _doc = _db.parse(_f);
			NodeList _nl = _doc.getElementsByTagName("multipath");
			Node enabled = _nl.item(0);
			if (enabled.getTextContent().equals("true"))
				enb = true;
    	}
    	return enb;
    }
    
    
    /**
	 * Mapea los dispositivo multipath asociando sus numeros de serie con los dispositivos asociados
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> getMultiPathVolumes() throws Exception {
		
		List<Map<String, Object>> multiPathVolumes = new ArrayList<Map<String, Object>>();
		
		//String _s = "cat /multi1";
		//String _s = "cat /multi2";
		int minPaths = 0;
		String _s = "multipath -d -l";
		try {
			String _output = Command.systemCommand(_s);
			if (_output != null && _output.length()>0) {
				StringTokenizer _st = new StringTokenizer(_output, "\n");
				
				List<String> _mp_vols = new ArrayList<String>();
				
				Map<String, Object> _mp_device = null;
				while (_st.hasMoreTokens()) {
					String line = _st.nextToken();				
					StringTokenizer _st_line = new StringTokenizer(line, " ");
					if (!line.contains("+") && !line.contains("|") && !line.contains(":") && !line.contains("=")) {
						if (_mp_vols.size()>minPaths) {							// Cuando encontramos un nuevo identificador, hemos terminado de recopilar el anterior, por lo que lo añadimos
							_mp_device.put("disks", _mp_vols);
							multiPathVolumes.add(_mp_device);
						}
						_mp_device = new HashMap<String, Object>();
						String _serial = _st_line.nextToken();
						_mp_device.put("dev", "/dev/mapper/"+_serial);
						_mp_device.put("devId", getDiskId(_serial));
						_mp_device.put("serial", _serial);
						_mp_device.put("dm-device", _st_line.nextToken());
						_mp_device.put("model", _st_line.nextToken());
						_mp_vols = new ArrayList<String>();
					} else if (!line.contains("policy") && !line.contains("size") && !line.contains("features") && line.contains("-")) {
						String _tok = (String) _st_line.nextToken();
						while (!_tok.contains(":"))				// Nos quedamos con el elemento que hay despues de x:x:x:x
							_tok = _st_line.nextToken();
						_mp_vols.add("/dev/"+_st_line.nextToken());
					} else if (line.contains("size") && line.contains("features")) {
						String size = _st_line.nextToken();
						_mp_device.put("size", VolumeManager.getFormatSize(size.substring(size.indexOf("=")+1)));
						_mp_device.put("features", line.substring(line.indexOf("features='")+"features='".length(), line.indexOf("'", line.indexOf("features='")+"features='".length())));
					}
				}
				if (_mp_vols.size()>minPaths) {
					_mp_device.put("disks", _mp_vols);		// Añadimos el último
					multiPathVolumes.add(_mp_device);
				}
			}
			return multiPathVolumes;
		} catch (Exception ex) {
			throw new Exception("Error obtaining multipath devices: "+ex);
		}
	}
	
	public static String getDiskId(String device) throws Exception {
		// Resto
		try {
			String id = null;
			if (device.contains("/dev/"))
				device = device.substring(device.indexOf("/dev/")+"/dev/".length());
			String output = Command.systemCommand("ls -l /dev/disk/by-id/ | grep "+device+" | awk '{print $9}'");
			if (output != null && output.length()>0) {
				id = "/dev/disk/by-id/"+output.trim().replaceAll("\n", "");
			}
			return id;
		} catch (Exception ex) {
			return device;
		}
	}
	
	/**
	 * Comprueba si el descriptor de un disco se encuentra en un multipath
	 * @param dev
	 * @return
	 * @throws Exception
	 */
	public static boolean isDiskonMultiPath(String dev) throws Exception {		
		List<Map<String, Object>> multiPathVolumes = MultiPathManager.getMultiPathVolumes();
		
		for (Map<String, Object> _mp_device : multiPathVolumes) {
			@SuppressWarnings("unchecked")
			List<String> disks = (List<String>) _mp_device.get("disks");
			if (disks.contains(dev))
				return true;
		}
		return false;
	}
	
	/**
	 * Ejecuta el comando necesario para que se inicialice el multipath
	 * @throws Exception
	 */
	public static void initializeMultipath() throws Exception {
		Command.systemCommand("modprobe dm-multipath");
		try {
			Command.systemCommand("multipath -F");
		} catch (Exception ex) {
			System.out.println("multipath -F devuelve algun error:"+ex.getMessage());
		}
		Command.systemCommand("multipath -d");
		Command.systemCommand("multipath -v2");
		Command.systemCommand("multipathd");
	}
}
