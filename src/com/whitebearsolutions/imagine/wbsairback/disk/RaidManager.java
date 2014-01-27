package com.whitebearsolutions.imagine.wbsairback.disk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.util.Command;

public class RaidManager {
	
	private final static Logger logger = LoggerFactory.getLogger(RaidManager.class);
	
	private static List<Map<String, String>> supportedModels = null;
	static {
		String manufacturer = "DELL";
		supportedModels = new ArrayList<Map<String, String>>();
		
		Map<String, String> h700 = new HashMap<String, String>();
		h700.put("manufacturer", manufacturer);
		h700.put("model", "H700");
		supportedModels.add(h700);
		
		Map<String, String> h710 = new HashMap<String, String>();
		h710.put("manufacturer", manufacturer);
		h710.put("model", "H710");
		supportedModels.add(h710);
		
		Map<String, String> h800 = new HashMap<String, String>();
		h800.put("manufacturer", manufacturer);
		h800.put("model", "H800");
		supportedModels.add(h800);
		
		Map<String, String> h810 = new HashMap<String, String>();
		h810.put("manufacturer", manufacturer);
		h810.put("model", "H810");
		supportedModels.add(h810);
	}
	
	
	
	/**
	 * Lista de pares clave-valor Ok para cada uno de los slots que obtiene el comando pdList
	 */
	private static Map<String, List<String>> pdList_slotOk;
	static {
		pdList_slotOk = new HashMap<String, List<String>>();
		pdList_slotOk.put("media_error_count", Arrays.asList(new String[]{"0"}));
		pdList_slotOk.put("other_error_count", Arrays.asList(new String[]{"0"}));
		pdList_slotOk.put("predictive_failure_count", Arrays.asList(new String[]{"0"}));
		pdList_slotOk.put("firmware_state", Arrays.asList(new String[]{"Online, Spun Up","Hotspare, Spun Up"}));
		pdList_slotOk.put("needs_ekm_attention", Arrays.asList(new String[]{"No"}));
		pdList_slotOk.put("foreign_state", Arrays.asList(new String[]{"None"}));
		pdList_slotOk.put("port_status", Arrays.asList(new String[]{"Active"}));;
		pdList_slotOk.put("drive_has_flagged_a_smart_alert", Arrays.asList(new String[]{"No"}));
	}
	
	
	/**
	 * Lista de pares clave-valor Ok para cada adapter que obtiene adpAllInfo
	 */
	private static Map<String,Map<String, List<String>>> adpInfoOk;
	static {
		adpInfoOk = new HashMap<String, Map<String, List<String>>>();
		
		Map<String, List<String>> devicePresentOk = new HashMap<String, List<String>>();
		devicePresentOk.put("degraded", Arrays.asList(new String[]{"0"}));
		devicePresentOk.put("offline", Arrays.asList(new String[]{"0"}));
		devicePresentOk.put("failed_disks", Arrays.asList(new String[]{"0"}));
		adpInfoOk.put("device_present", devicePresentOk);
		
		Map<String, List<String>> errorCountersOk = new HashMap<String, List<String>>();
		errorCountersOk.put("memory_correctable_errors", Arrays.asList(new String[]{"0"}));
		errorCountersOk.put("memory_uncorrectable_errors", Arrays.asList(new String[]{"0"}));
		adpInfoOk.put("error_counters", errorCountersOk);
		
		Map<String, List<String>> statusOk = new HashMap<String, List<String>>();
		statusOk.put("ecc_bucket_count", Arrays.asList(new String[]{"0"}));
		adpInfoOk.put("status", statusOk);
	}

	
	/**
	 * Lista de pares clave-valor Ok para cada adapter que obtiene adpAllInfo
	 */
	private static Map<String, List<String>> cfgDsplVdriveOk;
	private static Map<String, List<String>> cfgDsplPhysicalOk;
	static {
		cfgDsplVdriveOk = new HashMap<String, List<String>>();
		cfgDsplVdriveOk.put("state", Arrays.asList(new String[]{"Optimal"}));
		cfgDsplVdriveOk.put("bad_blocks_exist", Arrays.asList(new String[]{"No"}));
		
		cfgDsplPhysicalOk = new HashMap<String, List<String>>();
		cfgDsplPhysicalOk.put("media_error_count", Arrays.asList(new String[]{"0"}));
		cfgDsplPhysicalOk.put("other_error_count", Arrays.asList(new String[]{"0"}));
		cfgDsplPhysicalOk.put("predictive_failure_count", Arrays.asList(new String[]{"0"}));
		cfgDsplPhysicalOk.put("firmware_state", Arrays.asList(new String[]{"Online, Spun Up","Hotspare, Spun Up"}));
		cfgDsplPhysicalOk.put("locked", Arrays.asList(new String[]{"Unlocked"}));
		cfgDsplPhysicalOk.put("needs_ekm_attention", Arrays.asList(new String[]{"No"}));
		cfgDsplPhysicalOk.put("foreign_state", Arrays.asList(new String[]{"None"}));
		cfgDsplPhysicalOk.put("port_status", Arrays.asList(new String[]{"Active"}));
		cfgDsplPhysicalOk.put("drive_has_flagged_a_smart_alert", Arrays.asList(new String[]{"No"}));
	}
	
	
	/**
	 * Lista de pares clave-valor Ok para cada adapter que obtiene adpAllInfo
	 * Battery State     : Operational
	 * Remaining Time Alarm    : No
  	 * Remaining Capacity Alarm: No
  	 * Relative State of Charge: 84 %
	 */
	private static Map<String, List<String>> adpBbuCmdOk;
	static {
		adpBbuCmdOk = new HashMap<String, List<String>>();
		adpBbuCmdOk.put("battery_state", Arrays.asList(new String[]{"Operational"}));
		//adpBbuCmdOk.put("remaining_time_alarm", Arrays.asList(new String[]{"No"}));
		//adpBbuCmdOk.put("remaining_capacity_alarm", Arrays.asList(new String[]{"No"}));
		adpBbuCmdOk.put("relative_state_of_charge", Arrays.asList(new String[]{"10"}));
	}
	
	
	/**
	 * Comprueba si está presente el modulo de raid (los controladores PERC)
	 * @return
	 * @throws Exception
	 */
	public static boolean hasRaidController() throws Exception {
		try {
			String _output = Command.systemCommand("lsscsi");
			//String _output = Command.systemCommand("cat /lsscsi");
			StringTokenizer _st = new StringTokenizer(_output, "\n");
			while (_st.hasMoreTokens()) {
				String line = _st.nextToken();
				for (Map<String, String> model : supportedModels) {
					if (line.contains(model.get("manufacturer")) && line.contains(model.get("model"))) {
						logger.info("Raid controller is present => lsscsi: {} {}", model.get("manufacturer"), model.get("model"));
						return true;
					}
				}
			}
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si el equipo tiene el controlador raid. Ex: {}", ex.getMessage());
			return false;
		}
	}
	
	
	/**
	 * Comprueba los valores que devuelve el comando adpallinfo, si todo está correcto, devuelve null, si hay algún error
	 * devuelve el mapa con clave-valor de lo que en ese momento ha devuelto el comando. Además, en el parámetro por valor
	 * se queda toda la información obtenida
	 * 
	 * @param adaptersInfo
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> checkadpBbuCmd(List<Map<String, String>> adaptersInfo) throws Exception {
		List<Map<String, String>> errors = null;
		if (adaptersInfo != null)
			adaptersInfo.addAll(adpBbuCmd());
		else
			adaptersInfo = adpBbuCmd();
		
		if (adaptersInfo != null) {
			for (Map<String, String> adapter : adaptersInfo) {
				String adapterId = adapter.get("id");
				for (String checkingSection : adpBbuCmdOk.keySet()) {
					List<String> valuesCheck = adpBbuCmdOk.get(checkingSection);
					String valueAdapter = adapter.get(checkingSection);
					if (valueAdapter != null) {
						if (!checkingSection.equals("relative_state_of_charge")) {
							if (!valuesCheck.contains(valueAdapter)) {
									logger.info("Found wrong state on disk [{}]: {} = {} ",new Object[]{adapterId, checkingSection,valueAdapter});
									if (errors == null)
										errors = new ArrayList<Map<String, String>>();
									Map<String, String> error = new HashMap<String, String>();
									error.put("key", checkingSection);
									error.put("val-ok", listToString(valuesCheck));
									error.put("val-obtained", valueAdapter);
									error.put("adapterId", adapterId);
									errors.add(error);
							}
						} else {
							if (Double.parseDouble(valueAdapter) <= Double.parseDouble(valuesCheck.get(0))) {
								logger.info("Found wrong state on disk [{}]: {} = {} ",new Object[]{adapterId, checkingSection,valueAdapter});
								if (errors == null)
									errors = new ArrayList<Map<String, String>>();
								Map<String, String> error = new HashMap<String, String>();
								error.put("key", checkingSection);
								error.put("val-ok", listToString(valuesCheck));
								error.put("val-obtained", valueAdapter);
								error.put("adapterId", adapterId);
								errors.add(error);
							}
						}
					} else {
						logger.error("Checking adpBbuCmd - Section "+checkingSection+" requested for checking not found in adapter:"+adapterId);
						throw new Exception("Checking adpBbuCmd - Section "+checkingSection+" requested for checking not found in adapter:"+adapterId);
					}
				}
			}
		}
		
		return errors;
	}
	
	
	/**
	 * Comprueba los valores que devuelve el comando adpallinfo, si todo está correcto, devuelve null, si hay algún error
	 * devuelve el mapa con clave-valor de lo que en ese momento ha devuelto el comando. Además, en el parámetro por valor
	 * se queda toda la información obtenida
	 * 
	 * @param adaptersInfo
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> checkAdpAllInfo(List<Map<String, Map<String, String>>> adaptersInfo) throws Exception {
		List<Map<String, String>> errors = null;
		if (adaptersInfo != null)
			adaptersInfo.addAll(adpAllInfo());
		else
			adaptersInfo = adpAllInfo();
		
		if (adaptersInfo != null) {
			for (Map<String, Map<String, String>> adapter : adaptersInfo) {
				String adapterId = adapter.get("id").get("id");
				for (String checkingSection : adpInfoOk.keySet()) {
					Map<String, List<String>> valuesCheck = adpInfoOk.get(checkingSection);
					Map<String, String> valuesAdapter = adapter.get(checkingSection);
					if (valuesAdapter != null) {
						for (String keyVal : valuesCheck.keySet()) {
							if (valuesAdapter.get(keyVal) != null) {
								if (!valuesCheck.get(keyVal).contains(valuesAdapter.get(keyVal))) {
									logger.info("Found wrong state on disk [{}]: {} = {} ",new Object[]{adapterId, keyVal,valuesAdapter.get(keyVal)});
									if (errors == null)
										errors = new ArrayList<Map<String, String>>();
									Map<String, String> error = new HashMap<String, String>();
									error.put("key", keyVal);
									error.put("val-ok", listToString(valuesCheck.get(keyVal)));
									error.put("val-obtained", valuesAdapter.get(keyVal));
									error.put("adapterId", adapterId);
									errors.add(error);
								}
							} else {
								logger.error("Checking adpAllInfo - Param "+keyVal+" requested for checking not found in adapter:"+adapterId);
								throw new Exception("Checking adpAllInfo - Param "+keyVal+" requested for checking not found in adapter:"+adapterId);
							}
						}
					} else {
						logger.error("Checking adpAllInfo - Section "+checkingSection+" requested for checking not found in adapter:"+adapterId);
						throw new Exception("Checking adpAllInfo - Section "+checkingSection+" requested for checking not found in adapter:"+adapterId);
					}
				}
			}
		}
		
		return errors;
	}
	
	
	/**
	 * Comprueba los valores del comando pdlist, devuelve un listado de mapas de errores en caso de encontrar o null si todo esta ok. Rellena el objeto que recibe
	 * para transmitir toda la información
	 * @param adaptersInfo
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> checkPdListAll(List<Map<String, Map<String, String>>> slotsInfo) throws Exception {
		List<Map<String, String>> errors = null;
		if (slotsInfo != null)
			slotsInfo.addAll(pdListAll());
		else
			slotsInfo = pdListAll();
		
		if (slotsInfo != null) {
			for (Map<String, Map<String, String>> adapter : slotsInfo) {
				String adapterId = adapter.get("id").get("id");
				for (String keyVal : pdList_slotOk.keySet()) {
					for (String slotId : adapter.keySet()) {
						if (!slotId.equals("id")) {
							Map<String, String> slot = adapter.get(slotId);
							if (slot.get(keyVal) != null) {
								if (!pdList_slotOk.get(keyVal).contains(slot.get(keyVal))) {
									if (errors == null)
										errors = new ArrayList<Map<String, String>>();
									Map<String, String> error = new HashMap<String, String>();
									logger.info("Found wrong state on disk [{}]: {} = {} ",new Object[]{adapterId, keyVal,slot.get(keyVal)});
									error.put("key", keyVal);
									error.put("val-ok", listToString(pdList_slotOk.get(keyVal)));
									error.put("val-obtained", slot.get(keyVal));
									error.put("adapterId", adapterId);
									error.put("slot_number", slotId);
									error.put("enclosure_device_id", slot.get("enclosure_device_id"));
									error.put("enclosure_position", slot.get("enclosure_position"));
									errors.add(error);
								}
							} else {
								logger.error("Checking pdList - Param "+keyVal+" requested for checking not found in adapter:"+adapterId);
								throw new Exception("Checking pdList - Param "+keyVal+" requested for checking not found in adapter:"+adapterId);
							}
						}
					}
				}
			}
		}
		
		return errors;
	}
	
	
	/**
	 * Comprueba los valores del comando cfgdisplay
	 * @param drivesInfo
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, String>> checkCfgDsplay(List<Map<String, Map<String, Object>>> drivesInfo) throws Exception {
		List<Map<String, String>> errors = null;
		if (drivesInfo != null)
			drivesInfo.addAll(cfgDisplay());
		else
			drivesInfo = cfgDisplay();
		
		if (drivesInfo != null) {
			for (Map<String, Map<String, Object>> adapter : drivesInfo) {
				String adapterId = (String) adapter.get("id").get("id");
				for (String diskGroupId : adapter.keySet()) {
					if (!diskGroupId.equals("id")) {
						Map<String, Object> diskGroup = adapter.get(diskGroupId);
						@SuppressWarnings("unchecked")
						List<Map<String, String>> vDrives = (List<Map<String, String>>) diskGroup.get("virtual_drives");
						if (vDrives != null) {
							for (Map<String, String> vDrive : vDrives) {
								for (String key : cfgDsplVdriveOk.keySet()) {
									if (vDrive.get(key) != null) {
										if (!cfgDsplVdriveOk.get(key).contains(vDrive.get(key))) {
											if (errors == null)
												errors = new ArrayList<Map<String, String>>();
											Map<String, String> error = new HashMap<String, String>();
											logger.info("Found wrong state on vdrive [{}]: {} = {} ",new Object[]{adapterId, key,vDrive.get(key)});
											error.put("key", key);
											error.put("val-ok", listToString(cfgDsplVdriveOk.get(key)));
											error.put("val-obtained", vDrive.get(key));
											error.put("adapterId", adapterId);
											error.put("virtual_drive", vDrive.get("virtual_drive"));
											error.put("disk_group", diskGroupId);
											error.put("raid_level", vDrive.get("raid_level"));
											errors.add(error);
										}
									} else {
										logger.error("Checking cfgDisplay - Param "+key+" requested for checking not found in adapter:"+adapterId+" vDrive: "+vDrive.get("virtual_drive"));
										throw new Exception("Checking cfgDisplay - Param "+key+" requested for checking not found in adapter:"+adapterId+" vDrive: "+vDrive.get("virtual_drive"));
									}
								}
							}
						}
						@SuppressWarnings("unchecked")
						List<Map<String, String>> phDrives = (List<Map<String, String>>) diskGroup.get("physical_drives");
						if (phDrives != null) {
							for (Map<String, String> phDrive : phDrives) {
								for (String key : cfgDsplPhysicalOk.keySet()) {
									if (phDrive.get(key) != null) {
										if (!cfgDsplPhysicalOk.get(key).contains(phDrive.get(key))) {
											if (errors == null)
												errors = new ArrayList<Map<String, String>>();
											Map<String, String> error = new HashMap<String, String>();
											logger.info("Found wrong state on disk [{}]: {} = {} ",new Object[]{adapterId, key,phDrive.get(key)});
											error.put("key", key);
											error.put("val-ok", listToString(cfgDsplPhysicalOk.get(key)));
											error.put("val-obtained", phDrive.get(key));
											error.put("adapterId", adapterId);
											error.put("physical_disk", phDrive.get("physical_disk"));
											error.put("disk_group", diskGroupId);
											error.put("enclosure_device_id", phDrive.get("enclosure_device_id"));
											error.put("enclosure_position", phDrive.get("enclosure_position"));
											error.put("slot_number", phDrive.get("slot_number"));
											error.put("drives_position", phDrive.get("drives_position"));
											error.put("sequence_number", phDrive.get("sequence_number"));
											error.put("pd_type", phDrive.get("pd_type"));
											error.put("sas_address(0)", phDrive.get("sas_address(0)"));
											errors.add(error);
										}
									} else {
										logger.error("Checking cfgDisplay - Param "+key+" requested for checking not found in adapter:"+adapterId+" vDrive: "+phDrive.get("virtual_drive"));
										throw new Exception("Checking cfgDisplay - Param "+key+" requested for checking not found in adapter:"+adapterId+" vDrive: "+phDrive.get("virtual_drive"));
									}
								}
							}
						}
					}
				}
			}
		}
		
		return errors;
	}
	
	/**
	 * Este comando muestra el estado de todo
	 * La función interpreta el comando y lo guarda de la siguiente manera:
	 * 	adapter_1
	 * 		name_sec1 => key_1 : value 1
	 * 					 key_2 : value 2
	 * 						...
	 * 		name_sec2 => key_1 : value 1
	 * 						...
	 * 		...
	 *  adapter 2
	 *  	name_sec1 => key_1 : value 1
	 *  ...
	 * 		
	 * @throws Exception
	 */
	public static List<Map<String, Map<String, String>>> adpAllInfo() throws Exception {
		//String _s = "cat /AdpAllInfo_aAll";
		String _s = "megacli -AdpAllInfo -aAll";
		List<Map<String, Map<String, String>>> adapters = null;
		try {
			String _output = Command.systemCommand(_s);
			if (_output != null && _output.length()>0) {
				adapters = new ArrayList<Map<String, Map<String, String>>>();
				StringTokenizer _st = new StringTokenizer(_output, "\n");
				Map<String, Map<String, String>> adapter = null;
				Map<String, String> section = null;
				String _key = null, _value = null;
				while (_st.hasMoreTokens()) {
					String _line = _st.nextToken();
					if (_line.trim().length()>1 && !_line.contains("Exit Code")) {
						if (_line.contains("#") && _line.contains("Adapter")) {		//New Adapter
							if (adapter != null)
								adapters.add(adapter);
							adapter = new HashMap<String, Map<String, String>>();
							section = new HashMap<String, String> ();
							section.put("name_section", "id");
							section.put("id", _line.trim().substring(_line.indexOf("#")+1));
							adapter.put("id", section);
							section = null;
							do {
								_line = _st.nextToken();
							} while (!_line.contains("====="));
						} else if ((!_line.contains(":") || _line.contains("Image Versions in Flash")) && !_line.contains("None") && !_line.contains("Mix in Enclosure Allowed")) {	//Guardo la anterior seccion y creo la nueva
							if (_st.hasMoreTokens()) {
								String last = _line;
								_line = _st.nextToken();
								if (_line.contains("====")){
									last = normalize(last);
									if (section != null)
										adapter.put(section.get("name_section"), section);
									section = new HashMap<String, String>();
									section.put("name_section", last);
								}
							}
						} else if (_line.contains(":")) {
							_key = _line.substring(0, _line.indexOf(":"));
							if ( _line.length() > _line.indexOf(":")+1) {
								_value = _line.substring(_line.indexOf(":")+1);
								_value = trimLeftRight(_value);
								_key = normalize(_key);		
								if (_key.equals("port") && _value.equals("Address")) {		// Caso especial
									do {
										_line = _st.nextToken();
										if (!_line.contains("HW")) {
											StringTokenizer _stb = new StringTokenizer(_line, " ");
											String portNumber = _stb.nextToken();
											portNumber = trimLeftRight(portNumber);
											String address = _stb.nextToken();
											address = trimLeftRight(address);
											_key = "port_"+portNumber;
											section.put(_key, address);
										} else {
											if (_st.hasMoreTokens()) {
												String last = _line;
												_line = _st.nextToken();
												if (_line.contains("====")){
													last = normalize(last);
													if (section != null)
														adapter.put(section.get("name_section"), section);
													section = new HashMap<String, String>();
													section.put("name_section", last);
												}
											}
										}
									} while (!(_line.contains("HW") || _line.contains("===")));
								} 
								else
									section.put(_key, _value);
							}
						}
					}
				}
				adapter.put(section.get("name_section"), section);
				adapters.add(adapter);
			}
			return adapters;
		} catch (Exception ex) {
			logger.error("Error obtaining megacli -AdpAllInfo data: "+ex.getMessage());
			throw new Exception("Error obtaining megacli -AdpAllInfo data: "+ex.getMessage());
		}
	}
	
	
	/**
	 * El comando pdList muestra la información de los discos físicos
	 * La función interpreta el comando de la siguiente manera:
	 * adapter_1
	 *      id		  =>  id    : id
	 * 		num_slot1 => key_1 : value 1
	 * 					 key_2 : value 2
	 * 						...
	 * 		num_slot2 => key_1 : value 1
	 * 						...
	 * 		...
	 *  adapter 2
	 *  	num_slot1 => key_1 : value 1
	 *  ...
	 * 		
	 */
	public static List<Map<String, Map<String, String>>> pdListAll() throws Exception {
		//String _s = "cat /PDList_aAll";
		String _s = "megacli -PDList -aAll";
		List<Map<String, Map<String, String>>> adapters = null;
		try {
			String _output = Command.systemCommand(_s);
			if (_output != null && _output.length()>0) {
				adapters = new ArrayList<Map<String, Map<String, String>>>();
				StringTokenizer _st = new StringTokenizer(_output, "\n");
				Map<String, Map<String, String>> adapter = null;
				Map<String, String> slot = null;
				String _key = null, _value = null;
				while (_st.hasMoreTokens()) {
					String _line = _st.nextToken();
					if (_line.trim().length()>1 && !_line.contains("Exit Code")) {
						if (_line.contains("Adapter") && _line.contains("#")) {			// new adapter
							if (adapter != null)
								adapters.add(adapter);
							adapter = new HashMap<String, Map<String, String>>();
							slot = new HashMap<String, String> ();
							slot.put("id", _line.trim().substring(_line.indexOf("#")+1));
							adapter.put("id", slot);
							slot = null;
						} else if (_line.contains(":")) {
							_key = _line.substring(0, _line.indexOf(":"));
							if ( _line.length() > _line.indexOf(":")+1) {
								_value = _line.substring(_line.indexOf(":")+1);
								if (_value.trim() != null && _value.trim().length() > 0) {
									_key = normalize(_key);
									_value = trimLeftRight(_value);
									if (_key.equals("enclosure_device_id")) {
										if (slot != null)
											adapter.put(slot.get("slot_number"), slot);
										slot = new HashMap<String, String>();
									}
									slot.put(_key, _value);
								}
							}
						}
					}
				}
				if (slot != null)
					adapter.put(slot.get("slot_number"), slot);
				if (adapter != null)
					adapters.add(adapter);
			}
		} catch (Exception ex) {
			logger.error("Error obtaining megacli -PDList data: "+ex.getMessage());
			throw new Exception("Error obtaining megacli -PDList data: "+ex.getMessage());
		}
		return adapters;
	}
	
	
	/**
	 * El comando ldInfoAll muestra la información de los virtual disk
	 * La función interpreta la salida de la forma mostrada. Hay un vdrive especial, el primero, que contiene el id del dispositivo
	 * 	adapter_1
	 * 		id		 =>  id    : id
	 * 		vdrive_1 =>  key_1 : value 1
	 * 					 key_2 : value 2
	 * 						...
	 * 		vdrive_2 =>  key_1 : value 1
	 * 						...
	 * 		...
	 *  adapter_2
	 *  	vdrive_1 => key_1 : value 1
	 *  ...
	 * 		
	 */
	public static List<Map<String, Map<String, String>>> ldInfoAll() throws Exception {
		//String _s = "cat /LDInfo_Lall_aAll";
		String _s = "megacli -LDInfo -Lall -aAll";
		List<Map<String, Map<String, String>>> adapters = null;
		try {
			String _output = Command.systemCommand(_s);
			if (_output != null && _output.length()>0) {
				adapters = new ArrayList<Map<String, Map<String, String>>>();
				StringTokenizer _st = new StringTokenizer(_output, "\n");
				Map<String, Map<String, String>> adapter = null;
				Map<String, String> vdrive = null;
				String _key = null, _value = null;
				while (_st.hasMoreTokens()) {
					String _line = _st.nextToken();
					if (_line.trim().length()>1 && !_line.contains("Exit Code")) {
						if (_line.contains("Adapter") && _line.contains("--")) {			// new adapter
							if (adapter != null)
								adapters.add(adapter);
							adapter = new HashMap<String, Map<String, String>>();
							vdrive = new HashMap<String, String> ();
							vdrive.put("id", _line.trim().substring(_line.indexOf("Adapter")+8,_line.indexOf("Adapter")+9));
							adapter.put("id", vdrive);
							vdrive = null;
						} else if (_line.contains(":")) {
							_key = _line.substring(0, _line.indexOf(":"));
							if ( _line.length() > _line.indexOf(":")+1) {
								_value = _line.substring(_line.indexOf(":")+1);
								if (_value.trim() != null && _value.trim().length() > 0) {
									_key = normalize(_key);
									_value = trimLeftRight(_value);
									if (_value.contains(":"))
										_value = _value.trim().substring(0,1);
									if (_key.equals("virtual_drive")) {
										if (vdrive != null)
											adapter.put(vdrive.get("virtual_drive"), vdrive);
										vdrive = new HashMap<String, String>();
									}
									vdrive.put(_key, _value);
								}
							}
						}
					}
				}
				if (vdrive != null)
					adapter.put(vdrive.get("virtual_drive"), vdrive);
				if (adapter != null)
					adapters.add(adapter);
			}
		} catch (Exception ex) {
			logger.error("Error obtaining megacli -LDInfo data: "+ex.getMessage());
			throw new Exception("Error obtaining megacli -LDInfo data: "+ex.getMessage());
		}
		return adapters;
	}
	
	
	/**
	 * El comando cfgDisplay muestra toda la configuración elativa a grupos de discos - virtual drives - physical drives
	 * La función interpreta la salida obteniendo la información en un mapa representado así:
	 * 	adapter_1
	 * 		id		    => info sobre adapter
	 * 		disk_group0 => key_1 : value 1
	 * 					   key_2 : value 2
	 * 					   virtual_drives  => vdrive_0 => key_1 : value_1
	 * 													  key_2 : value_2
	 * 										  vdrive_1
	 * 											...
	 * 					   physical_drives => phdrive_0 => key_1 : value_1
	 * 													   key_2 : value_2
	 * 										  phdrive_1
	 * 										  ...
	 * 						...
	 * 		disk_group1 => key_1 : value 1
	 * 						...
	 * 		...
	 *  adapter 2
	 *  ...
	 * 		
	 */
	public static List<Map<String, Map<String, Object>>> cfgDisplay() throws Exception {
		//String _s = "cat /CfgDsply_aAll";
		String _s = "megacli -CfgDsply -aAll";
		List<Map<String, Map<String, Object>>> adapters = null;
		try {
			String _output = Command.systemCommand(_s);
			if (_output != null && _output.length()>0) {
				adapters = new ArrayList<Map<String, Map<String, Object>>>();
				StringTokenizer _st = new StringTokenizer(_output, "\n");
				Map<String, Map<String, Object>> adapter = null;
				Map<String, Object> diskGroup = null;
				Map<String, String> vDrive = null;
				Map<String, String> physicalDrive = null;
				String _key = null, _value = null;
				List<Map<String, String>> diskGroupVdrives = null;
				List<Map<String, String>> diskGroupPhysicalDrives = null;
				String lastType = null;
				while (_st.hasMoreTokens()) {
					String _line = _st.nextToken();
					if (_line.trim().length()>1 && !_line.contains("Exit Code")) {
						
						if (_line.contains("=====")) {			// new adapter
							if (adapter != null)
								adapters.add(adapter);
							adapter = new HashMap<String, Map<String, Object>>();
							diskGroup = new HashMap<String, Object> ();
							do {
								_line = _st.nextToken();
								if (_line.contains(":")) {
									_key = _line.substring(0, _line.indexOf(":"));
									if ( _line.length() > _line.indexOf(":")+1) {
										_value = _line.substring(_line.indexOf(":")+1);
										if (_value.trim() != null && _value.trim().length() > 0) {
											_value = trimLeftRight(_value);
											_key = normalize(_key);
											diskGroup.put(_key, _value);
										}
									}
								}
							} while (!_line.contains("====="));
							_line = _st.nextToken();
							_key = _line.substring(0, _line.indexOf(":"));
							if ( _line.length() > _line.indexOf(":")) {			// Number of Disk groups
								_value = _line.substring(_line.indexOf(":")+1);
								if (_value.trim() != null && _value.trim().length() > 0) {
									_value = trimLeftRight(_value);
									_key = normalize(_key);
									diskGroup.put(_key, _value);
								}
							}
							
							adapter.put("id", diskGroup);
							diskGroup=null;
						} else if (_line.contains(":")) {
							_key = _line.substring(0, _line.indexOf(":"));
							if ( _line.length() > _line.indexOf(":")+1) {
								_value = _line.substring(_line.indexOf(":")+1);
								if (_value.trim() != null && _value.trim().length() > 0) {
									_value = trimLeftRight(_value);
									_key = normalize(_key);
									if (_key.equals("disk_group")) {
										if (diskGroupVdrives != null && diskGroupVdrives.size()>0)
											diskGroup.put("virtual_drives", diskGroupVdrives);
										if (diskGroupPhysicalDrives != null && diskGroupPhysicalDrives.size()>0)
											diskGroup.put("physical_drives", diskGroupPhysicalDrives);
										if (diskGroup != null)
											adapter.put((String) diskGroup.get("disk_group"), diskGroup);
										diskGroup = new HashMap<String, Object>();
										diskGroup.put(_key, _value);
										lastType = "disk_group";
									} else if (_key.equals("virtual_drive")) {
										if (vDrive != null)
											diskGroupVdrives.add(vDrive);
										else
											diskGroupVdrives = new ArrayList<Map<String, String>>();
										vDrive = new HashMap<String, String>();
										vDrive.put(_key, _value);
										lastType = "virtual_drive";
									} else if (_key.equals("physical_disk")) {
										if (physicalDrive != null) 
											diskGroupPhysicalDrives.add(physicalDrive);
										else
											diskGroupPhysicalDrives = new ArrayList<Map<String, String>>();
										physicalDrive = new HashMap<String, String>();
										physicalDrive.put(_key, _value);
										lastType = "physical_disk";
									} else if (lastType.equals("disk_group")) {
										diskGroup.put(_key, _value);
									} else if (lastType.equals("virtual_drive")) {
										vDrive.put(_key, _value);
									} else if (lastType.equals("physical_disk")) {
										physicalDrive.put(_key, _value);
									}
								}
							}
						}
					}
				}
				// Añadimos el último elemento
				if (adapter != null) {
					if (vDrive != null)
						diskGroupVdrives.add(vDrive);
					if (physicalDrive != null)
						diskGroupPhysicalDrives.add(physicalDrive);
					if (diskGroup != null) {
						if (diskGroupPhysicalDrives != null && diskGroupPhysicalDrives.size()>0)
							diskGroup.put("physical_drives", diskGroupPhysicalDrives);
						if (diskGroupVdrives != null && diskGroupVdrives.size()>0)
							diskGroup.put("virtual_drives", diskGroupVdrives);
						adapter.put((String) diskGroup.get("disk_group"), diskGroup);
					}
					adapters.add(adapter);
				}
			}
		} catch (Exception ex) {
			logger.error("Error obtaining megacli -CfgDsply data: "+ex.getMessage());
			throw new Exception("Error obtaining megacli -CfgDsply data: "+ex.getMessage());
		}
		return adapters;
	}
	
	public static List<Map<String, String>> adpBbuCmd() throws Exception {
		//String _s = "cat /bateries";
		String _s = "megacli -AdpBbuCmd -aALL";
		List<Map<String, String>> info = null;
		try {
			String _output = Command.systemCommand(_s);
			if (_output != null && _output.length()>0) {
				Map<String, String> adapter = null;
				String _key = null;
				String _value = null;
				info = new ArrayList<Map<String, String>>();
				StringTokenizer _st = new StringTokenizer(_output, "\n");
				while (_st.hasMoreTokens()) {
					String _line = _st.nextToken();
					if (_line.trim().length()>1 && !_line.contains("Exit Code")) {
						if (_line.contains("BBU status for Adapter:")) {
							if (adapter != null)
								info.add(adapter);
							adapter = new HashMap<String, String>();
							if ( _line.length() > _line.indexOf(":")+1) {
								_value = _line.substring(_line.indexOf(":")+1);
								_value = trimLeftRight(_value);
							}
							adapter.put("id", _value);
						} else if (_line.contains(":")) {
							_key = _line.substring(0, _line.indexOf(":"));
							if ( _line.length() > _line.indexOf(":")+1) {
								_value = _line.substring(_line.indexOf(":")+1);
								if (_value.trim() != null && _value.trim().length() > 0) {
									_value = trimLeftRight(_value);
									_key = normalize(_key);
									if (!adapter.containsKey(_key)) {
										if (_key.equals("relative_state_of_charge")) {
											if (_value.indexOf("%") > 0) {
												_value = _value.substring(0, _value.indexOf("%")).trim();
											}
										}
										adapter.put(_key, _value);
									}
								}
							}
						} else if (_line.contains("=")) {
							_key = _line.substring(0, _line.indexOf("="));
							if ( _line.length() > _line.indexOf("=")+1) {
								_value = _line.substring(_line.indexOf("=")+1);
								if (_value.trim() != null && _value.trim().length() > 0) {
									_value = trimLeftRight(_value);
									_key = normalize(_key);
									if (!adapter.containsKey(_key)) {
										adapter.put(_key, _value);
									}
								}
							}
						}
					}
				}
				if (adapter != null)
					info.add(adapter);
			}
			return info;
		} catch (Exception ex) {
			logger.error("Error obtaining megacli -AdpBbuCmd -aALL data: "+ex.getMessage());
			throw new Exception("Error obtaining megacli -AdpBbuCmd -aALL data: "+ex.getMessage());
		}
	}
	
	/**
	 * Obtiene el log de eventos
	 */
	public static String adpEventLog() throws Exception {
		//String _s = "cat /AdpEventLog_GetEvents_f_events_aAll";
		String _s = "megacli -AdpEventLog -GetEvents -f events -aAll";
		String _output = null;
		try {
			 _output = Command.systemCommand(_s);
		} catch (Exception ex) {
			logger.error("Error obtaining megacli log output: "+ex.getMessage());
			throw new Exception("Error obtaining megacli log output: "+ex.getMessage());
		}
		return _output; 
	}
	
	
	/**
	 * Obtiene el fragmento html para el informe del watchdog
	 * @return
	 * @throws Exception
	 */
	public static String getHtmlReport() throws Exception {
		String html = "";
		try {
			List<Map<String, Map<String, Object>>> adapters = cfgDisplay();
			List<Map<String, String>> adBattery = adpBbuCmd();
			
			logger.info("Generating html raid info report");
			int i=0;
			for (Map<String, Map<String, Object>> adapter : adapters) {
				if (i>0)
					html+="===================<br />";
				Map<String, Object> idInfo = adapter.get("id");
				html+="Adapter #"+i+" ["+idInfo.get("product_name")+" -- "+idInfo.get("memory")+"]<br />";
				for (Map<String, String> ad : adBattery) {
					if (ad.get("id").equals(String.valueOf(i))) {
						html+=" Battery Charge:"+ad.get("relative_state_of_charge")+"% <br/>";
					}
				}
				for (String idGroup : adapter.keySet()) {
					if (!"id".equals(idGroup)) {
						Map<String, Object> diskGroup = adapter.get(idGroup);
						html+="++Disk Group:"+idGroup+" <br/>";
						html+="----Physical Disks:"+diskGroup.get("number_of_pds")+" <br/>";
						html+="----Virtual Disks:"+diskGroup.get("number_of_vds")+" <br/>";
						html+="----Hot Spares:"+diskGroup.get("number_of_dedicated_hotspares")+" <br/>";
					}
				}
				i++;
			}
		} catch (Exception ex) {
			logger.error("Error generando html RAID report. Ex: {}", ex.getMessage());
		}
		
		return html;
	}
	
	
	/**
	 * Pasa una lista de errores a String
	 * @param errors
	 * @return
	 * @throws Exception
	 */
	public static String errorsToString(List<Map<String, String>> errors) throws Exception {
		String sb = "";
		List<String> report = new ArrayList<String>();
		if (errors !=  null && errors.size()>0) {
			for (Map<String, String> error: errors) {
				if (!report.contains(error.get("adapterId")+"-"+error.get("key"))) {
					sb+="Error in adapter ["+error.get("adapterId")+"]. Detected "+unormalize(error.get("key"))+"="+error.get("val-obtained")+". It should be "+error.get("val-ok")+"<br/>";
					report.add(error.get("adapterId")+"-"+error.get("key"));
				}
			}
		}
		logger.info("Errors found: {}",sb);
		return sb;
	}
	
	
	/**
	 * Normaliza un string:
	 *  	origen: 	  Val. de Tal  
	 *  	resultado:	val_de_tal
	 * @param _s
	 * @throws Exception
	 */
	public static String normalize(String _s) throws Exception {
		String _n = _s;
		_n = _n.replaceAll(" ", "_");
		_n = _n.replaceAll("\\.", "");
		_n = _n.toLowerCase();
		while (_n.lastIndexOf("_")+1 == _n.length())
			_n = _n.substring(0, _n.length()-1);
		while (_n.indexOf("_") == 0)
			_n = _n.substring(1);
		return _n;
	}
	
	public static String unormalize(String _s) throws Exception {
		String _n = _s;
		_n = _n.replaceAll("_", " ");
		return _n;
	}
	
	
	/**
	 * Elimina los espacios a izquierda y derecha de una cadena
	 * @param _s
	 * @throws Exception
	 */
	public static String trimLeftRight(String _s) throws Exception {
		String _n = _s;
		while (_n.lastIndexOf(" ")+1 == _n.length())
			_n = _n.substring(0, _n.length()-1);
		while (_n.indexOf(" ") == 0)
			_n = _n.substring(1);
		return _n;
	}
	
	
	/**
	 * Devuelve una cadena a partir de una lista
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static String listToString(List<String> list) throws Exception {
		StringBuilder sb = new StringBuilder();
		if (list != null && list.size() > 0) {
			sb.append("[");
			int c = 0;
			for (String el : list) {
				if (c != 0)
					sb.append(", ");
				else
					c++;
				sb.append(el);
			}
			sb.append("]");
		}
		return sb.toString();
	}
}
