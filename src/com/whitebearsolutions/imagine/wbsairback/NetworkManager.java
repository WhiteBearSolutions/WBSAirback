package com.whitebearsolutions.imagine.wbsairback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
//import com.whitebearsolutions.imagine.wbsairback.bacula.StorageManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.HAConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.network.IpNetworkCalculator;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class NetworkManager {
	public static final int BONDING_RR = 1;
	public static final int BONDING_LACP = 2;
	
	private TreeMap<String, Map<String, String[]>> _interfaces;
	private Map<String, List<Map<String, String[]>>> _routes;
	private Map<String, List<String>> _slaves;
	private Map<String, Integer> _slaves_types;
	private IpNetworkCalculator _inc;
	private List<String[]> _nameservers;
	
	private final static Logger logger = LoggerFactory.getLogger(NetworkManager.class);
	
	public NetworkManager(Configuration conf) throws Exception {
		this._inc = new IpNetworkCalculator();
		this._interfaces = new TreeMap<String, Map<String,String[]>>(new NetworkComparator());
		this._routes = new HashMap<String, List<Map<String, String[]>>>();
		this._slaves = new HashMap<String, List<String>>();
		this._slaves_types = new HashMap<String, Integer>();
		this._nameservers = new ArrayList<String[]>();
        parseAddresses();
	}
	
	public static String addressToString(String[] _address) {
		if(_address == null || _address.length != 4) {
			return "";
		}
		StringBuilder address = new StringBuilder();
		for(int i = 0; i < _address.length; i++) {
			if(i > 0) {
				address.append(".");
			}
			address.append(_address[i]);
		}
		return address.toString();
	}
	
	private void checkInterfaces() throws Exception {
		for(String _interface : this._interfaces.keySet()) {
			if(_interface.startsWith("bond")) {
				if(hasSlaves(_interface)) {
					for(String _slave : getSlaves(_interface)) {
						removeNetworkInterface(_slave);
					}
				} else {
					removeNetworkInterface(_interface);
				}
			}
		}
	}
	
	public static boolean compareAddress(String[] address1, String[] address2) {
		if(address1 == null || address2 == null) {
			return false;
		}
		if(address1.length != address2.length) {
			return false;
		}
		for(int i = address1.length; --i >= 0; ) {
			if(!address1[i].equals(address2[i])) {
				return false;
			}
		}
		return true;
	}
	
	public String[] getAddress(String _interface) {
		if(!this._interfaces.containsKey(_interface)) {
			return new String[] { "", "", "", "" };
		}
		Map<String, String[]> _data = this._interfaces.get(_interface);
		if(_data == null) {
			return new String[] { "", "", "", "" };
		}
		if(_data.get("address") == null) {
			return new String[] { "", "", "", "" };
		}
		return _data.get("address");
	}
	
	public List<String> getAvailableInterfaces() {
		List<String> _interfaces = new ArrayList<String>();
		for(String _interface : getSystemInterfaces()) {
			if(!isSlaveInterface(_interface)) {
				_interfaces.add(_interface);
			}
		}
		return _interfaces;
	}
	
	public int getBondType(String _interface) {
		if(this._slaves_types.containsKey(_interface)) {
			return this._slaves_types.get(_interface);
		}
		return -1;
	}
	
	public String[] getBroadcast(String _interface) throws Exception {
		if(!this._interfaces.containsKey(_interface)) {
			return new String[] { "", "", "", "" };
		}
		this._inc.setAddresses(getAddress(_interface), getNetmask(_interface));
		this._inc.calculate();
		return this._inc.getBroadcastAddress();
	}
	
	public List<String> getConfiguredInterfaces() {
		return new ArrayList<String>(this._interfaces.keySet());
	}
	
	public String[] getGateway(String _interface) {
		if(!this._interfaces.containsKey(_interface)) {
			return new String[] { "", "", "", "" };
		}
		Map<String, String[]> _data = this._interfaces.get(_interface);
		if(_data == null) {
			return new String[] { "", "", "", "" };
		}
		if(_data.get("gateway") == null) {
			return new String[] { "", "", "", "" };
		}
		return _data.get("gateway");
	}
	
	public String getInterface(int number) {
		List<String> _interfaces = new ArrayList<String>(this._interfaces.keySet());
		return _interfaces.get(number);
	}
	
	public static String getLocalAddress() throws Exception {
		return Command.systemCommand("/sbin/ifconfig | grep \"inet addr\" | grep -v \"127.0.0.1\" | awk '{ print $2; }' | awk 'BEGIN { RS = \"\"; } ; { print $1; }' | awk 'BEGIN { FS = \":\"; } ; { print $2; }'").trim();
	}
	
	public List<String[]> getNameservers() {
		return this._nameservers;
	}
	
	public String[] getNetmask(String _interface) {
		if(!this._interfaces.containsKey(_interface)) {
			return new String[] { "", "", "", "" };
		}
		Map<String, String[]> _data = this._interfaces.get(_interface);
		if(_data == null) {
			return new String[] { "", "", "", "" };
		}
		if(_data.get("netmask") == null) {
			return new String[] { "", "", "", "" };
		}
		return _data.get("netmask");
	}
	
	public int getNetmaskBits(String _interface) throws Exception {
		if(!this._interfaces.containsKey(_interface)) {
			return 32;
		}
		this._inc.setAddresses(getAddress(_interface), getNetmask(_interface));
		this._inc.calculate();
		return this._inc.getMaskBits();
	}
	
	public String[] getNetwork(String _interface) throws Exception {
		if(!this._interfaces.containsKey(_interface)) {
			return new String[] { "", "", "", "" };
		}
		this._inc.setAddresses(getAddress(_interface), getNetmask(_interface));
		this._inc.calculate();
		return this._inc.getNetworkAddress();
	}
	
	public String getNextBondInterface() {
		int _number = 0;
		StringBuilder _sb = new StringBuilder();
		for(String _interface : this._interfaces.keySet()) {
			if(_interface.startsWith("bond")) {
				try {
					if(_number == Integer.parseInt(_interface.substring(4))) {
						_number++;
					}
				} catch(NumberFormatException _ex) {}
			}
		}
		_sb.append("bond");
		_sb.append(_number);
		return _sb.toString();
	}
	
	public static Map<String, String> getProxyServer() throws IOException {
		File _f = new File(WBSAirbackConfiguration.getFileAptProxy());
		if(_f.exists()) {
        	Map<String, String> _proxy = new HashMap<String, String>();
			FileReader _fr = new FileReader(_f);
        	StringBuilder _sb = new StringBuilder();
        	char[] _buffer = new char[255];
        	while(_fr.read(_buffer) != -1) {
        		_sb.append(_buffer);
        	}
        	int index = _sb.indexOf("Acquire::http::Proxy");
        	_sb = new StringBuilder(_sb.substring(index + 20, _sb.indexOf(";", index + 20)));
        	try {
        		String _url = _sb.toString().replaceAll("\"", "").trim();
        		_url = _url.substring(_url.indexOf("://") + 3);
        		if(_url.contains("@")) {
        			_proxy.put("server", _url.substring(_url.indexOf("@") + 1));
        			_proxy.put("user", _url.substring(0, _url.indexOf("@")));
        		} else {
        			_proxy.put("server", _url);
        		}
        		if(_proxy.get("server") != null && _proxy.get("server").contains(":")) {
        			try {
        				_proxy.put("port", String.valueOf(Integer.parseInt(_proxy.get("server").substring(_proxy.get("server").indexOf(":") + 1))));
        			} catch(NumberFormatException _ex) {}
        			_proxy.put("server", _proxy.get("server").substring(0, _proxy.get("server").indexOf(":")));
        			
        		}
        		if(_proxy.get("port") == null) {
        			_proxy.put("port", "80");
        		}
        		if(_proxy.get("user") != null && _proxy.get("user").contains(":")) {
        			_proxy.put("password", _proxy.get("user").substring(_proxy.get("user").indexOf(":") + 1));
        			_proxy.put("user", _proxy.get("user").substring(0, _proxy.get("user").indexOf(":")));
        		}
        		return _proxy;
        	} catch(Exception _ex) {
        		System.out.println("NetworkManager::getProxyServer: " + _ex.toString());
        	}
        }
		return null;
	}
	
	public static String getPublicAddress() throws Exception {
		String _status = HAConfiguration.getStatus();
		if(_status.equals("master") || _status.equals("slave")) {
			File _f = new File("/etc/ha.d/haresources");
			if(_f.exists()) {
				BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
				for(String _line = _br.readLine(); _line != null; _line = _br.readLine()) {
					if(_line.startsWith("master ")) {
						StringTokenizer _st = new StringTokenizer(_line, " ");
						while(_st.hasMoreTokens()) {
							String _tok = _st.nextToken();
							if(_tok.startsWith("IPaddr::")) {
								return _tok.substring(9);
							}
						}
					}
				}
			}
		}
		return getLocalAddress();
	}
	
	public List<String> getSlaves(String _interface) {
		if(this._slaves.containsKey(_interface)) {
			return this._slaves.get(_interface);
		}
		return new ArrayList<String>();
	}
	
	public List<Map<String, String[]>> getStaticRoutes(String _interface) {
		if(this._routes.containsKey(_interface)) {
			return this._routes.get(_interface);
		}
		return new ArrayList<Map<String,String[]>>();
	}
	
	public void reinitStaticRoutes() {
		this._routes.clear();
	}
	
	public String getStringAddress(String _interface) {
		return addressToString(getAddress(_interface));
	}
	
	public List<String> getSystemInterfaces() {
		List<String> interfaces = new ArrayList<String>();
		try {
			StringTokenizer _st = new StringTokenizer(Command.systemCommand("/bin/cat /proc/net/dev | /bin/grep -o \"[a-z][a-z]*[0-9]\""));
			while(_st.hasMoreTokens()) {
				String _tok = _st.nextToken();
				if(_tok.startsWith("tun")) {
					continue;
				}
				interfaces.add(_tok);
			}
		} catch(Exception _ex) {}		
		return interfaces;
	}
	
	public boolean hasAnotherGateway(String _iface, String[] gateway) {
		for(String _interface : getConfiguredInterfaces()) {
			if (!_interface.equals(_iface)) {
				if(hasGateway(_interface) && !Arrays.equals(getGateway(_interface),gateway)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasGateway(String _interface) {
		if(!this._interfaces.containsKey(_interface)) {
			return false;
		}
		Map<String, String[]> _data = this._interfaces.get(_interface);
		if(_data == null) {
			return false;
		}
		if(_data.get("gateway") != null) {
			return true;
		}
		return false;
	}
	
	public boolean hasSlaves(String _interface) {
		if(!this._slaves.containsKey(_interface)) {
			return false;
		}
		List<String> _data = this._slaves.get(_interface);
		if(_data != null && _data.size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean hasStaticRoutes(String _interface) {
		if(!this._routes.containsKey(_interface)) {
			return false;
		}
		List<Map<String, String[]>> _data = this._routes.get(_interface);
		if(_data != null && _data.size() > 0) {
			return true;
		}
		return false;
	}
	
	public static String[] inverseAddress(String[] address) throws Exception {
		int count = 0;
		String[] address_array = new String[address.length];
		for(int i = address.length; --i >= 0; count++) {
			address_array[count] = address[i];
		}
		return address_array;
	}
	
	public boolean isBondInterface(String _interface) {
		if(this._slaves.containsKey(_interface)) {
			return true;
		}
		return false;
	}
	
	public boolean isConfiguredAddress(String[] address) {
		if(address == null || !isValidAddress(address)) {
			return false;
		}
		for(String _interface : getConfiguredInterfaces()) {
			if(compareAddress(getAddress(_interface), address)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isConfiguredInterface(String _interface) {
		if(_interface != null && this._interfaces.containsKey(_interface)) {
			return true;
		}
		return false;
	}
	
	public boolean isNetworkAddress(String _interface, String[] address) {
		try {
			this._inc.setAddresses(getAddress(_interface), getNetmask(_interface));
		} catch(Exception _ex) {
			return false;
		}
		this._inc.calculate();
		return this._inc.isNetworkAddress(address);
	}
	
	public boolean isSlaveInterface(String _interface) {
		for(List<String> _slaves : this._slaves.values()) {
			if(_slaves != null && _slaves.contains(_interface)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSystemInterface(String _interface) {
		if(_interface != null && getSystemInterfaces().contains(_interface)) {
			return true;
		} else if(_interface != null && _interface.matches("bond[0-9]")) {
			return true;
		}
		return false;
	}
	
	public static boolean isValidAddress(String address) {
		if(address == null || address.isEmpty()) {
			return false;
		}
		
		if(!address.contains(".")) {
			return false;
		}
		
		return isValidAddress(address.split("\\."));
	}
	
	public static boolean isValidAddress(String[] address) {
		if(address == null || address.length != 4) {
			return false;
		}
		
		for(String octet : address) {
			if(octet == null || octet.trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	private void parseAddresses() throws IOException {		
		File _f = new File("/etc/network/interfaces");
		if(_f.exists()) {
			String line = "", _name = "";
			StringTokenizer _st;
			Map<String, String[]> _interface = new HashMap<String, String[]>();;
			List<Map<String, String[]>> _routes = new ArrayList<Map<String,String[]>>();
			List<String> _slave_interfaces = new ArrayList<String>();
			int _slave_type = BONDING_RR;
			BufferedReader _reader = new BufferedReader(new InputStreamReader(new FileInputStream(_f)));
			try {
				while(true) {
					line = _reader.readLine();
					if(line == null || line.isEmpty()) {
						if(!_name.isEmpty() && !"lo".equals(_name)) {
							if(!this._interfaces.containsKey(_name)) {
								this._interfaces.put(_name, _interface);
								if(!_slave_interfaces.isEmpty() && _name.startsWith("bond")) {
									this._slaves.put(_name, _slave_interfaces);
									this._slaves_types.put(_name, _slave_type);
								}
								if(!_routes.isEmpty()) {
									this._routes.put(_name, _routes);
								}
							}
						}
						if(line == null) {
							break;
						}
					} else if(line.startsWith("iface ")) {
						_slave_type = BONDING_RR;
						_interface = new HashMap<String, String[]>();
						_slave_interfaces = new ArrayList<String>();
						_routes = new ArrayList<Map<String,String[]>>();
						_name = line.substring(6, line.indexOf(" ", 6)).trim();
					} else if(line.trim().startsWith("address ")) {
						_interface.put("address", toAddress(line.substring(line.indexOf("address ") + 8)));
					} else if(line.trim().startsWith("netmask ")) {
						_interface.put("netmask", toAddress(line.substring(line.indexOf("netmask ") + 8)));
					} else if(line.trim().startsWith("gateway ")) {
						_interface.put("gateway", toAddress(line.substring(line.indexOf("gateway ") + 8)));
					} else if(line.contains("up route add -net ")) {
						try {
							Map<String, String[]> _route = new HashMap<String, String[]>();
							_st = new StringTokenizer(line.substring(line.indexOf("up route add -net ") + 18).trim(), " ");
							_route.put("address", toAddress(_st.nextToken()));
							_st.nextToken();
							_route.put("netmask", toAddress(_st.nextToken()));
							_st.nextToken();
							_route.put("gateway", toAddress(_st.nextToken()));
							_routes.add(_route);
						} catch(Exception _ex) {}
					} else if(line.trim().startsWith("slaves ")) {
						_st = new StringTokenizer(line.substring(line.indexOf("slaves ") + 7).trim(), " ");
						while(_st.hasMoreTokens()) {
							_slave_interfaces.add(_st.nextToken());
						}
						for(line = _reader.readLine(); line != null && line.trim().startsWith("bond_"); line = _reader.readLine()) {
							line = line.trim();
							if(line.startsWith("bond_mode ")) {
								if("balance-rr".equalsIgnoreCase(line.substring(line.indexOf(" ") + 1))) {
									_slave_type = BONDING_RR;
								} else if("4".equalsIgnoreCase(line.substring(line.indexOf(" ") + 1))) {
									_slave_type = BONDING_LACP;
								}
								break;
							}
						}
					}
				}
			} finally {
				_reader.close();
			}
			
			_reader = new BufferedReader(new InputStreamReader(new FileInputStream("/etc/resolv.conf")));
			try {
		    	while((line = _reader.readLine()) != null) {
					if(line.startsWith("nameserver")) {
						_st = new StringTokenizer(line.substring(line.indexOf("nameserver") + 11).trim(), " ");
						while(_st.hasMoreTokens()) {
							String _server = _st.nextToken();
							String[] _address = new String[4];
							StringTokenizer _st2 = new StringTokenizer(_server.trim(), ".");
							_address[0] = _st2.nextToken();
							_address[1] = _st2.nextToken();
							_address[2] = _st2.nextToken();
							_address[3] = _st2.nextToken();
							this._nameservers.add(_address);
						}
					}
		    	}
			} finally {
				_reader.close();
			}
		}
		updateFileNetworkStatistics();
	}
	
	public void removeAllStaticRoutes(String _interface) {
		this._routes.remove(_interface);
	}
	
	public void updateFileNetworkStatistics() {
		try {
			File f = new File(WBSAirbackConfiguration.getFileStatisticsNetworkInterfaces());
			StringBuilder _sb = new StringBuilder();
			for (String iface : this._interfaces.keySet()) {
				_sb.append(iface);
				_sb.append("\n");
			}
			
			FileOutputStream _fos = new FileOutputStream(f);
			_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
			_fos.close();
		} catch (Exception ex) {
			logger.error("Error al intentar actualizar el fichero de estadisticas de interfaces de red {}",WBSAirbackConfiguration.getFileStatisticsNetworkInterfaces());
		}
	}
	
	public void removeBondInterface(String _interface) throws Exception {
		if(_interface == null) {
			throw new Exception("invalid network interface");
		}
		
		if(!_interface.startsWith("bond")) {
			throw new Exception("invalid network interface");
		}
		
		if(isConfiguredInterface(_interface)) {
			if(hasSlaves(_interface)) {
				Map<String, String[]> _iface = this._interfaces.get(_interface);
				for(String _slave : getSlaves(_interface)) {
					this._interfaces.put(_slave, _iface);
					break;
				}
				this._slaves.remove(_interface);
			}
			this._slaves_types.remove(_interface);
			removeAllStaticRoutes(_interface);
			this._interfaces.remove(_interface);
		} 
	}
	
	public void removeNetworkInterface(String _interface) throws Exception {
		if(_interface == null || _interface.isEmpty()) {
			throw new Exception("invalid network arguments");
		}
		removeAllStaticRoutes(_interface);
		this._interfaces.remove(_interface);
	}
	
	public void removeStaticRoute(String _interface, String[] address, String[] netmask, String[] gateway) {
		if(this._routes.containsKey(_interface)) {
			List<Map<String, String[]>> _routes = new ArrayList<Map<String, String[]>>();
			for(Map<String, String[]> _route : getStaticRoutes(_interface)) {
				if(!compareAddress(_route.get("address"), address) ||
						!compareAddress(_route.get("netmask"), netmask) ||
						!compareAddress(_route.get("gateway"), gateway)) {
					_routes.add(_route);
				}
			}
			if(_routes.size() > 0) {
				this._routes.put(_interface, _routes);
			} else {
				this._routes.remove(_interface);
			}
		}
	}
	
	public static void removeProxyServer() throws Exception {
		File _f = new File(WBSAirbackConfiguration.getFileAptProxy());
		if(_f.exists()) {
			_f.delete();
		}
	}
	
	public void setBondInterface(String _interface, int type, List<String> _slaves) throws Exception {
		
		if(_interface == null || _slaves == null) {
			throw new Exception("invalid interface arguments");
		}
		if(!_interface.startsWith("bond")) {
			throw new Exception("invalid network interface");
		}
		
		if(type != BONDING_RR &&
				type != BONDING_LACP) {
			throw new Exception("invalid bonding type");
		}
		
		Map<String, String[]> _iface = null;
		if(isConfiguredInterface(_interface)) {
			_iface = this._interfaces.get(_interface);
			
			if(hasSlaves(_interface)) {
				List<String> _slave_list = getSlaves(_interface);
				if(_slave_list.size() != _slaves.size() &&
						!_slave_list.containsAll(_slaves)) {
					for(String _slave : _slaves) {
						if(!isSystemInterface(_slave)) {
							throw new Exception("invalid slave interface [" + _slave + "]");
						}
					}
				}
			}
		} else {
			for(String _slave : _slaves) {
				if(!isSystemInterface(_slave)) {
					throw new Exception("invalid slave interface [" + _slave + "]");
				}
				
				if(_iface == null && isConfiguredInterface(_slave)) {
					_iface = this._interfaces.get(_slave);
				}
				
				try {
					Command.systemCommand("/sbin/ifdown "+_slave);
				} catch (Exception ex){}
				try {
					String [] ad = this._interfaces.get(_slave).get("address");
					if (ad != null && ad.length > 0) {
						String address = ad[0];
						for (int i=1; i<ad.length;i++)
							address+="."+ad[i];
						Command.systemCommand("/bin/ip addr del "+address+" dev "+_slave);	
					}
					
				} catch (Exception ex){}
			
			}
			
			if(_iface == null) {
				throw new Exception("at least one of the slave interfaces must have a network address");
			}
		}
		
		for(String _slave : _slaves) {
			if(isConfiguredInterface(_slave)) {
				removeNetworkInterface(_slave);
			}
		}
		this._interfaces.put(_interface, _iface);
		this._slaves.put(_interface, _slaves);
		this._slaves_types.put(_interface, type);
	}
	
	public void setBondInterfaceType(String _interface, int type) throws Exception {
		if(!_interface.startsWith("bond")) {
			throw new Exception("invalid network interface");
		}
		if(type != BONDING_RR &&
				type != BONDING_LACP) {
			throw new Exception("invalid bonding type");
		}
		this._slaves_types.put(_interface, type);
	}
	
	public void setMasterNetwork(String groupAddress, String slaveAddress, String iface) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("127.0.0.1\tlocalhost\n");
		String _interface = getInterface(0);
		if (iface != null && !iface.isEmpty())
			_interface = iface;
		_sb.append(addressToString(getAddress(_interface)) + "\twbsairback master\n");
		_sb.append(slaveAddress + "\tslave\n");
		
		FileOutputStream _fos = new FileOutputStream("/etc/hosts");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		
		_sb = new StringBuilder();
		_sb.append("master");
		
		_fos = new FileOutputStream("/etc/hostname");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		
		Command.systemCommand("/bin/hostname master");
	}
	
	public void setNameservers(List<String[]> nameservers) throws Exception {
		if(nameservers != null) {
			for(String[] _address : this._nameservers) {
				for(int j = _address.length; --j >= 0; ) {
					if(_address[j] == null || _address[j].isEmpty()) {
						throw new Exception("invalid nameserver address [" + addressToString(_address) + "]");
					}
					int _octet = Integer.parseInt(_address[j]);
					if(_octet > 255 || _octet < 0) {
						throw new Exception("invalid nameserver address [" + addressToString(_address) + "]");
					}
				}
			}
			this._nameservers = nameservers;
		}
	}
	
	public void setNetworkInterface(String _interface, String[] address, String[] netmask, String[] gateway) throws Exception {
		if(_interface == null || address == null || netmask == null) {
			throw new Exception("invalid network arguments");
		}
		
		if(!isValidAddress(address)) {
			throw new Exception("invalid network arguments");
		}
		if(!isValidAddress(netmask)) {
			throw new Exception("invalid network arguments");
		}
		
		Map<String, String[]> _iface = new HashMap<String, String[]>();
		try {
			for(int j = address.length; --j >= 0; ) {
				int _octet = Integer.parseInt(address[j]);
				if(_octet > 255 || _octet < 0) {
					throw new Exception("invalid address octect");
				}
			}
			_iface.put("address", address);
			for(int j = netmask.length; --j >= 0; ) {
				int _octet = Integer.parseInt(netmask[j]);
				if(_octet > 255 || _octet < 0) {
					throw new Exception("invalid netmask octect");
				}
			}
			_iface.put("netmask", netmask);
			if(isValidAddress(gateway)) {
				for(int j = gateway.length; --j >= 0; ) {
					int _octet = Integer.parseInt(gateway[j]);
					if(_octet > 255 || _octet < 0) {
						throw new Exception("invalid gateway octect");
					}
				}
				_iface.put("gateway", gateway);
			}
		} catch(NumberFormatException _ex) {
			throw new Exception("invalid network arguments");
		}
		
		if(!isSystemInterface(_interface)) {
			throw new Exception("invalid network interface");
		}
		if(this._interfaces.get(_interface)!=null && this._interfaces.get(_interface).get("address")!=null) {
			StringBuilder oldAddress=new StringBuilder();
			StringBuilder newAddress=new StringBuilder();
			for (int x=0;this._interfaces.get(_interface).get("address").length>x;x++){
				oldAddress.append((x==0 ? "" : ".")+this._interfaces.get(_interface).get("address")[x]);
			}
			for (int x=0;address.length>x;x++){
				newAddress.append((x==0 ? "" : ".")+address[x]);
			}
			/*if (!oldAddress.toString().isEmpty() && !newAddress.toString().isEmpty())
			StorageManager.updateAllStorageByInterface(oldAddress.toString(),newAddress.toString());
			BackupOperator.reload();*/
		}
		this._interfaces.put(_interface, _iface);
	}
	
	public static void setProxyServer(String server, int port, String user, String password) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("http://");
		if(user != null && !user.isEmpty()) {
			_sb.append(user);
			if(password != null && !password.isEmpty()) {
				_sb.append(":");
				_sb.append(password);							
			}
			_sb.append("@");
		}
		try {
			InetAddress.getByName(server);
		} catch(UnknownHostException _ex) {
			throw new Exception("invalid proxy server");
		}
		_sb.append(server);
		if(port > 0 && port != 80) {
			_sb.append(":");
			_sb.append(port);
		}
		
		new URL(_sb.toString());
		StringBuilder _content = new StringBuilder();
		_content.append("//Proxy configuration by WBSAirback\n");
		_content.append("Acquire::http::Proxy \"");
		_content.append(_sb.toString());
		_content.append("\";\n");
		
		File dirProxy = new File(WBSAirbackConfiguration.getDirectoryProxy());
		if (!dirProxy.exists())
			dirProxy.mkdirs();
		
		FileSystem.writeFile(new File(WBSAirbackConfiguration.getFileAptProxy()), _content.toString());
	}
	
	public void setStaticRoute(String _interface, String[] address, String[] netmask, String[] gateway) throws Exception {
		if(_interface == null || address == null || netmask == null || gateway == null) {
			throw new Exception("invalid route arguments");
		}
		if(!isValidAddress(address)) {
			throw new Exception("invalid route arguments");
		}
		if(!isValidAddress(netmask)) {
			throw new Exception("invalid route arguments");
		}
		if(!isValidAddress(gateway)) {
			throw new Exception("invalid route arguments");
		}
		
		Map<String, String[]> _route = new HashMap<String, String[]>();
		try {
			for(int j = address.length; --j >= 0; ) {
				int _octet = Integer.parseInt(address[j]);
				if(_octet > 255 || _octet < 0) {
					throw new Exception("invalid address octect");
				}
			}
			_route.put("address", address);
			for(int j = netmask.length; --j >= 0; ) {
				int _octet = Integer.parseInt(netmask[j]);
				if(_octet > 255 || _octet < 0) {
					throw new Exception("invalid netmask octect");
				}
			}
			_route.put("netmask", netmask);
			for(int j = gateway.length; --j >= 0; ) {
				int _octet = Integer.parseInt(gateway[j]);
				if(_octet > 255 || _octet < 0) {
					throw new Exception("invalid gateway octect");
				}
			}
			_route.put("gateway", gateway);
		} catch(NumberFormatException _ex) {
			throw new Exception("invalid route arguments");
		}
		
		List<Map<String, String[]>> _routes = getStaticRoutes(_interface);
		for(Map<String, String[]> _st_route : _routes) {
			if(compareAddress(_st_route.get("address"), _route.get("address")) &&
					compareAddress(_st_route.get("netmask"), _route.get("netmask")) &&
					compareAddress(_st_route.get("gateway"), _route.get("gateway"))) {
				return;
			}
		}
		_routes.add(_route);
		this._routes.put(_interface, _routes);
	}
	
	public void setStandaloneNetwork() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("127.0.0.1\tlocalhost\n");
		_sb.append(addressToString(getAddress(getInterface(0))) + "\twbsairback\n");
		
		FileOutputStream _fos = new FileOutputStream("/etc/hosts");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		
		_sb = new StringBuilder();
		_sb.append("wbsairback");
		
		_fos = new FileOutputStream("/etc/hostname");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		
		Command.systemCommand("/bin/hostname wbsairback");
	}
	
	public void setSlaveNetwork(String masterAddress, String iface) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("127.0.0.1\tlocalhost\n");
		String _interface = getInterface(0);
		if (iface != null && !iface.isEmpty())
			_interface = iface;
		_sb.append(addressToString(getAddress(_interface)) + "\twbsairback slave\n");
		_sb.append(masterAddress + "\tmaster\n");
		
		FileOutputStream _fos = new FileOutputStream("/etc/hosts");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		
		_sb = new StringBuilder();
		_sb.append("slave");
		
		_fos = new FileOutputStream("/etc/hostname");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		
		Command.systemCommand("/bin/hostname slave");
	}
	
	public static String[] toAddress(String address) {
		if(address == null || address.isEmpty()) {
			return new String[] { "", "", "", "" };
		}
		
		StringTokenizer _st = new StringTokenizer(address.trim(), ".");
		if(_st.countTokens() < 4) {
			return new String[] { "", "", "", "" };
		}
		
		String[] address_array = new String[_st.countTokens()];
		for(int i = 0; _st.hasMoreTokens() && i < 4; i++) {
			address_array[i] = _st.nextToken();
		}
		return address_array;
	}
	
	public void update() throws Exception {
		checkInterfaces();
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("auto lo\n");
		_sb.append("iface lo inet loopback\n\n");
		for(String _interface : this._interfaces.keySet()) {
			_sb.append("auto ");
			_sb.append(_interface);
			_sb.append("\n");
			_sb.append("iface ");
			_sb.append(_interface);
			_sb.append(" inet static\n");
			_sb.append("\taddress ");
			_sb.append(addressToString(getAddress(_interface)));
			_sb.append("\n");
			_sb.append("\tnetmask ");
			_sb.append(addressToString(getNetmask(_interface)));
			_sb.append("\n");
			if(hasGateway(_interface)) {
				_sb.append("\tgateway ");
				_sb.append(addressToString(getGateway(_interface)));
				_sb.append("\n");
			}
			if(this._slaves.containsKey(_interface)) {
				List<String> slaves = getSlaves(_interface);
				if(slaves != null && slaves.size() > 0) {
					int _type = BONDING_RR;
					_sb.append("\tslaves");
					for(String slave_iface : slaves) {
						_sb.append(" ");
						_sb.append(slave_iface);
					}
					_sb.append("\n");
					
					if(this._slaves_types.containsKey(_interface)) {
						_type = this._slaves_types.get(_interface); 
					}
					switch(_type) {
						default: {
								_sb.append("\tbond_mode balance-rr\n");
								_sb.append("\tbond_miimon 100\n");
							}
							break;
						case BONDING_LACP: {
								_sb.append("\tbond_mode 4\n");
								_sb.append("\tbond_miimon 100\n");
								_sb.append("\tbond_xmit_hash_policy layer2+3\n");
								_sb.append("\tbond_lacp_rate slow\n");
							}
							break;
					}
				}
			}
			if(hasStaticRoutes(_interface)) {
				for(Map<String, String[]> route : getStaticRoutes(_interface)) {
					_sb.append("\tup route add -net ");
					_sb.append(addressToString(route.get("address")));
					_sb.append(" netmask ");
					_sb.append(addressToString(route.get("netmask")));
					_sb.append(" gw ");
					_sb.append(addressToString(route.get("gateway")));
					_sb.append(" ");
					_sb.append(_interface);
					_sb.append("\n");
				}
			}
			_sb.append("\n");
		}
		
		Command.systemCommand("/etc/init.d/networking stop");
		try {
			FileOutputStream _fos = null;
			try {
				_fos = new FileOutputStream("/etc/network/interfaces");
				_fos.write(_sb.toString().getBytes());
			} finally {
				if(_fos != null) {
					_fos.close();
				}
			}
			
			_sb = new StringBuilder();
			_sb.append("# WBSAirback automatic nameserver configuration\n");
			
			for(String[] address : getNameservers()) {
				_sb.append("nameserver ");
				_sb.append(addressToString(address));
				_sb.append("\n");
			}
			
			try {
				_fos = new FileOutputStream("/etc/resolv.conf");
				_fos.write(_sb.toString().getBytes());
			} finally {
				if(_fos != null) {
					_fos.close();
				}
			}
		} finally {
			Command.systemCommand("/etc/init.d/networking start");
		}
		
		ServiceManager.restart(ServiceManager.POSTGRES);
		ServiceManager.restart(ServiceManager.BACULA_SD);
		ServiceManager.restart(ServiceManager.BACULA_DIR);
	}
}
