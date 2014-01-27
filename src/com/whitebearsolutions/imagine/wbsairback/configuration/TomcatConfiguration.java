package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.security.X509CA;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class TomcatConfiguration extends WBSAirbackConfiguration {
	private X509CA _ca;
	private BigInteger _serial;
	private byte[] _pkcs12_data;
	private char[] _pkcs12_password;
	private boolean _https;
	private boolean _obs;
	
	public TomcatConfiguration(Configuration conf) throws Exception {
		this._https = false;
		Configuration _c = new Configuration(new File(getFileConfiguration()));
		if(!_c.hasProperty("pki.store")) {
			_c.setProperty("pki.store", getFileKeystore());
		}
		this._ca = new X509CA(_c);
		this._pkcs12_password = "wbsairback123".toCharArray();
		load();
	}
	
	public static boolean checkHTTPS() {
		try {
			if(Integer.parseInt(Command.systemCommand("/bin/netstat -putan | /bin/grep \":443 \" | /bin/grep LISTEN | /usr/bin/wc -l").trim()) == 1) {
				return true;
			}
		} catch(Exception _ex) {}
		return false;
	}
	
	public boolean isHTTPSEnable() {
		return this._https;
	}
	
	public boolean isOBSEnable() {
		return this._obs;
	}
	
	public void setHTTPS(boolean value) {
		this._https = value;
	}
	
	public void setPKCS12(byte[] data, char[] password) throws Exception {
		if(data == null || data.length == 0) {
			throw new Exception("invalid PKCS#12 data");
		}
		if(!X509CA.checkPKCS12(data, password)) {
			throw new Exception("invalid PKCS#12 data or paswword");
		}
		this._pkcs12_data = data;
		this._pkcs12_password = password;
	}
	
	public void store() throws Exception {
		File _tomcat_server_file = new File(FILE_TOMCAT_CONF);
		FileLock _fl = new FileLock(_tomcat_server_file);
		FileOutputStream _fos = new FileOutputStream(_tomcat_server_file);
		
		if(this._https) {
			this._serial = this._ca.getFirstSerial();
			if(this._serial == null) {
				this._serial = this._ca.generateX509Certificate("ES", "Madrid", "Las Rozas", "WHITEBEARSOLUTIONS", "Research and development", "wbsairback", "info@whitebearsolutions.com");
			}
		}
		
		try {
			_fl.lock();
			_fos.write(getConfigurationContent().getBytes());
		} finally {
			try {
				_fos.close();
			} catch(Exception _ex) {}
			_fl.unlock();
		}
	}
	
	private void load() {
		try {
			DocumentBuilder _db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document _doc = _db.parse(new File(FILE_TOMCAT_CONF));
			NodeList _nl = _doc.getElementsByTagName("Connector");
			for(int i = _nl.getLength(); --i >= 0; ) {
				Element _e = (Element) _nl.item(i);
				if(_e.hasAttribute("port") && "443".equals(_e.getAttribute("port"))) {
					this._https = true;
				}
			}
			_nl = _doc.getElementsByTagName("Context");
			for(int i = _nl.getLength(); --i >= 0; ) {
				Element _e = (Element) _nl.item(i);
				if(_e.hasAttribute("path") && "/obs".equals(_e.getAttribute("path"))) {
					this._obs = true;
				}
			}
		} catch(Exception _ex) {
			System.out.println("TomcatConfiguration::load: " + _ex.getMessage());
		}
	}
	
	private String getConfigurationContent() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<Server port=\"8005\" shutdown=\"SHUTDOWN\">\n");
		_sb.append("  <Listener className=\"org.apache.catalina.core.AprLifecycleListener\" SSLEngine=\"on\" />\n");
		_sb.append("  <Listener className=\"org.apache.catalina.core.JasperListener\" />\n");
		_sb.append("  <Listener className=\"org.apache.catalina.core.JreMemoryLeakPreventionListener\" />\n");
		_sb.append("  <Listener className=\"org.apache.catalina.mbeans.GlobalResourcesLifecycleListener\" />\n");
		_sb.append("  <Listener className=\"org.apache.catalina.core.ThreadLocalLeakPreventionListener\" />\n");
		
		_sb.append("  <Service name=\"Catalina\">\n");
		if(this._https) {
			_sb.append("    <Connector port=\"80\" protocol=\"HTTP/1.1\" connectionTimeout=\"20000\" redirectPort=\"443\" />\n");
			_sb.append("    <Connector port=\"443\" protocol=\"HTTP/1.1\" SSLEnabled=\"true\"");
			_sb.append(" scheme=\"https\" secure=\"true\" clientAuth=\"false\"");
			if(this._pkcs12_data == null) {
				_sb.append(" keystoreFile=\"");
				_sb.append(this._ca.getStoreFile().getAbsolutePath());
				_sb.append("\" keystorePass=\"");
				_sb.append(this._ca.getStorePassword());
				_sb.append("\" keyAlias=\"");
				_sb.append(this._serial);
				_sb.append("\"");
			} else {
				try {
					this._ca.setStorePassword(String.valueOf(this._pkcs12_password));
					X509CA.createKeyStoreFromPKCS12(this._ca.getStoreFile(), this._pkcs12_data, this._pkcs12_password);
					_sb.append(" keystoreFile=\"");
					_sb.append(this._ca.getStoreFile());
					_sb.append("\" keystorePass=\"");
					_sb.append(this._pkcs12_password);
					_sb.append("\"");
				} catch(Exception _ex) {
					throw new Exception("cannot build JKS keystore: " + _ex.getMessage());
				}
			}
			_sb.append(" sslProtocol=\"TLS\" />\n");
		} else {
			_sb.append("    <Connector port=\"80\" protocol=\"HTTP/1.1\" connectionTimeout=\"20000\" redirectPort=\"443\" />\n");
		}
		_sb.append("    <Engine name=\"Catalina\" defaultHost=\"localhost\">\n");
		_sb.append("      <Host name=\"localhost\" appBase=\"/var/www\" unpackWARs=\"true\" autoDeploy=\"true\">\n");
		_sb.append("        <Context path=\"\" reloadable=\"true\" docBase=\"/var/www/webadministration\" />\n");
		_sb.append("        <Context path=\"/obs\" reloadable=\"true\" docBase=\"/var/www/obs\" />\n");
		_sb.append("      </Host>\n");
		_sb.append("    </Engine>\n");
		_sb.append("  </Service>\n");
		_sb.append("</Server>");
		return _sb.toString();
	}
}
