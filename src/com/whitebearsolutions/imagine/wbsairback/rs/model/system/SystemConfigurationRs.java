package com.whitebearsolutions.imagine.wbsairback.rs.model.system;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.TomcatConfiguration;
import com.whitebearsolutions.imagine.wbsairback.util.StringFormat;

@XmlRootElement(name = "systemConfiguration")
public class SystemConfigurationRs {
	
	private String password;
	private String mail;
	private String baculaMailLevel;
	private String reportMail;
	private String mailHost;
	private String mailFrom;
	private String snmptraphost;
	private Integer snmptrapversion;
	private String reporthour;
	
	private Boolean https;
	private String httpsCertificate;
	private String httpsPassword;
	
	public SystemConfigurationRs() {}

	
	/**
	 * Obtiene el xml que representa a este objeto 
	 * @return
	 */
	public String getXML() {
		String xml = "";
		try {
			JAXBContext jc = JAXBContext.newInstance( SystemConfigurationRs.class );
			Marshaller m = jc.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			m.marshal(this, stringWriter);
			xml = stringWriter.toString();
		} catch (JAXBException ex) {}
		return xml;
	}
	
	public static SystemConfigurationRs fromXML(String xml) {
		try {
			if (xml != null && xml.length()>0) {
				String nameEntity = "systemConfiguration";
				JAXBContext jc = JAXBContext.newInstance( ProxyRs.class );
				Unmarshaller um = jc.createUnmarshaller();
				if (xml.indexOf("<"+nameEntity+">") > -1 && xml.indexOf("</"+nameEntity+">") > -1) {
					SystemConfigurationRs o = (SystemConfigurationRs) um.unmarshal( new StreamSource( new StringReader( xml.substring(xml.indexOf("<"+nameEntity+">"), xml.indexOf("</"+nameEntity+">")+("</"+nameEntity+">").length()).toString() ) ) );
					return o;
				}
			}
			return null;
		} catch (JAXBException ex) {
			return null;
		}
	}
	
	public static SystemConfigurationRs getObject(GeneralSystemConfiguration sc) throws Exception {
		SystemConfigurationRs config = new SystemConfigurationRs();
		if (sc.getBaculaMailAccount() != null && !sc.getBaculaMailAccount().isEmpty())
			config.setMail(sc.getBaculaMailAccount());
		if (sc.getMailServer() != null && !sc.getMailServer().isEmpty())
			config.setMailHost(sc.getMailServer());
		if (sc.getSnmpTrapServer() != null && !sc.getSnmpTrapServer().isEmpty())
			config.setSnmptraphost(sc.getSnmpTrapServer());
		if (sc.getSnmpTrapVersion() != null && !sc.getSnmpTrapVersion().isEmpty())
			config.setSnmptrapversion(Integer.parseInt(sc.getSnmpTrapVersion()));
		if (sc.getReportHour() > -1)
			config.setReporthour(StringFormat.getTwoCharTimeComponent(sc.getReportHour()));
		config.setHttps(TomcatConfiguration.checkHTTPS());
		if (GeneralSystemConfiguration.getBaculaMailLevel() != null && !GeneralSystemConfiguration.getBaculaMailLevel().isEmpty()) {
			config.setBaculaMailLevel(GeneralSystemConfiguration.getBaculaMailLevel());
		} else {
			config.setBaculaMailLevel(GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ALL);
		}
		if (sc.getMailFromAccount() != null && !sc.getMailFromAccount().isEmpty())
			config.setMailFrom(sc.getMailFromAccount());
		if (sc.getMailReportAccount() != null && !sc.getMailReportAccount().isEmpty())
			config.setReportMail(sc.getMailReportAccount());
		
		return config;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getMail() {
		return mail;
	}


	public void setMail(String mail) {
		this.mail = mail;
	}


	public String getMailHost() {
		return mailHost;
	}


	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}


	public String getSnmptraphost() {
		return snmptraphost;
	}


	public void setSnmptraphost(String snmptraphost) {
		this.snmptraphost = snmptraphost;
	}


	public String getReporthour() {
		return reporthour;
	}


	public void setReporthour(String reporthour) {
		this.reporthour = reporthour;
	}


	public Boolean getHttps() {
		return https;
	}


	public void setHttps(Boolean https) {
		this.https = https;
	}


	public String getHttpsCertificate() {
		return httpsCertificate;
	}


	public void setHttpsCertificate(String httpsCertificate) {
		this.httpsCertificate = httpsCertificate;
	}


	public String getHttpsPassword() {
		return httpsPassword;
	}


	public void setHttpsPassword(String httpsPassword) {
		this.httpsPassword = httpsPassword;
	}


	public Integer getSnmptrapversion() {
		return snmptrapversion;
	}


	public void setSnmptrapversion(Integer snmptrapversion) {
		this.snmptrapversion = snmptrapversion;
	}


	public String getBaculaMailLevel() {
		return baculaMailLevel;
	}


	public void setBaculaMailLevel(String baculaMailLevel) {
		this.baculaMailLevel = baculaMailLevel;
	}


	public String getReportMail() {
		return reportMail;
	}


	public void setReportMail(String reportMail) {
		this.reportMail = reportMail;
	}


	public String getMailFrom() {
		return mailFrom;
	}


	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
	

}
