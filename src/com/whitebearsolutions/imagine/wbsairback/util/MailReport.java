package com.whitebearsolutions.imagine.wbsairback.util;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.backup.CategoryManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.RaidManager;
import com.whitebearsolutions.imagine.wbsairback.service.LicenseManager;
import com.whitebearsolutions.mail.Mail;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class MailReport {
	private Configuration _c;
	private GeneralSystemConfiguration _sc;
	private Calendar _end;
	private ClientManager _cm;

	
	private final static Logger logger = LoggerFactory.getLogger(MailReport.class);

	public MailReport() throws Exception {
		this._c = new Configuration(new File(
				WBSAirbackConfiguration.getFileConfiguration()));
		this._sc = new GeneralSystemConfiguration();
		this._end = Calendar.getInstance();
		this._cm = new ClientManager(this._c);
	}

	public static void main(String[] args) {
		try {
			new MailReport();
			//m.sendMail();
		} catch (Exception _ex) {
			logger.error("Error on MailReport: {}", _ex.getMessage());
		}
	}

	public void sendMail() throws Exception {
		try {
			logger.info("Sending report mails ...");
			CategoryManager _cm = new CategoryManager();
			for (Map<String, String> _category : _cm.getCategories()) {
				if(_category.get("mail") != null && !_category.get("mail").isEmpty()) {
					sendMail("WBSAirback daily report [" + _category.get("name") + "]",
							_category.get("mail"),
							writeDayReport(_category.get("name")));
				}
			}
			sendMail("WBSAirback daily report", this._sc.getMailReportAccount(), writeDayReport(null));
		} catch (Exception ex) {
			logger.error("Error sending report mails. Ex: {}", ex.getMessage());
			throw ex;
		}
	}

	public void sendMail(String subject, String mail_address, String text)
			throws Exception {
		try {
			if (mail_address == null || mail_address.isEmpty()) {
				throw new Exception("mail account undefined");
			}
			Configuration _tc = new Configuration(new File("/tmp/config"));
			_tc.setProperty("mail.host", "localhost");
	
			
			Mail _m = new Mail(_tc);
			_m.addTo(mail_address);
			com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration _sc = new com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration();
			_m.setFrom("WBSAirback", _sc.getMailFromAccount());
			_m.setSubject(subject);
			_m.setHTML(text);
			_m.send();
			logger.info("Email sended successfully with address:{} subject:{}", mail_address, subject);
		} catch (Exception ex) {
			logger.error("Error sending email with address:{} subject:{}. Ex: {}", new Object[]{mail_address, subject, ex.getMessage()});
			throw ex;
		}
	}

	private String writeDayReport(String category) throws Exception {
		try {
			logger.debug("Generating daily report, category [{}]", category);
			StringBuilder _report = new StringBuilder();
			DBConnectionManager _dbm = new DBConnectionManager(this._c);
			DBConnection connection = _dbm.getConnection();
			java.text.SimpleDateFormat _dateFormat = new java.text.SimpleDateFormat(
					"dd/MM/yyyy HH:mm");
	
			int offset = 0;
			this._end.set(Calendar.SECOND, 0);
			this._end.set(Calendar.MILLISECOND, 0);
			Calendar _start = (Calendar) this._end.clone();
			_start.add(Calendar.DAY_OF_MONTH, -1);
	
			StringBuilder _sb = new StringBuilder();
			_sb.append("SELECT DISTINCT Client.Name as Clientname, Job.name as jobname,");
			_sb.append(" Job.joberrors as joberrors, Level, starttime, EndTime, Job.jobstatus as Status,");
			_sb.append(" Status.jobstatuslong as StatusName, JobFiles, JobBytes");
			_sb.append(" FROM Client, Job, Status");
			_sb.append(" WHERE Client.ClientId = Job.ClientId");
			_sb.append(" AND Job.jobstatus = Status.jobstatus");
			_sb.append(" AND Job.StartTime BETWEEN ?");
			_sb.append(" AND ?");
			_sb.append(" ORDER BY job.joberrors DESC, job.jobstatus, job.jobbytes ASC");
	
			connection.setObject(0, _start.getTime());
			connection.setObject(1, this._end.getTime());
			List<Map<String, Object>> result = connection.query(_sb.toString());
	
			Map<String, Integer> _partitions = GeneralSystemConfiguration.getDiskLoad();
	
			_report.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
			_report.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
			_report.append("<head></head>\n");
			_report.append("<body>\n");
			_report.append("<table style=\"width: 95%; border: 1px solid #585f64;\">\n");
			_report.append("<tr>\n");
			_report.append("<td colspan=\"2\" style=\"text-align: center; font-size: 14px;\"><strong>WBSAIRBACK DAILY REPORT</strong></td>");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td colspan=\"2\">&nbsp;</td>");
			_report.append("</tr>\n");
	
			_report.append("<tr>\n");
			_report.append("<td style=\"width: 150px; vertical-align: top; text-align: center; font-size: 14px; background-color: #6e8898;\" colspan=\"2\"><strong>Storage status</strong></td>");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td colspan=\"2\">&nbsp;</td>");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td style=\"vertical-align: top; font-size: 14px;\" colspan=\"2\">");
	
			/*
			 * Storage
			 */
			_report.append("<table style=\"width: 90%; border: 0px; font-size: 14px; text-align: center;\" align=\"center\">");
			_report.append("<tr>");
			_report.append("<td></td>");
			_report.append("<td><strong>Volume</strong></td>");
			_report.append("<td><strong>Type</strong></td>");
			_report.append("<td><strong>Used percent</strong></td>");
			_report.append("</tr>");
	
			offset = 0;
			for (String _path : _partitions.keySet()) {
				int _p = _partitions.get(_path);
				_report.append("<tr");
				if (_p > 90) {
					_report.append(" style=\"background: #e9f0ba;\"");
				} else if (offset % 2 == 0) {
					_report.append(" style=\"background: #e2e3e8;\"");
				}
				_report.append(">");
				_report.append("<td>");
				_report.append("</td>");
				if (_path.contains(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
					_path = _path.replace(
							WBSAirbackConfiguration.getDirectoryVolumeMount(), "");
					if (_path.startsWith("/shares/")) {
						_path = _path.substring(8);
						_report.append("<td>");
						_report.append(_path);
						_report.append("</td>");
						_report.append("<td>External volume</td>");
					} else {
						if (_path.startsWith("/")) {
							_path = _path.substring(1);
						}
						_report.append("<td>");
						_report.append(_path);
						_report.append("</td>");
						_report.append("<td>Logical volume</td>");
					}
				} else if (_path.startsWith("/dev/")) {
					_path = _path.replace("/dev/", "");
					_report.append("<td>");
					_report.append(_path);
					_report.append("</td>");
					_report.append("<td>Internal disk</td>");
				}
				_report.append("<td>");
				_report.append(_p);
				_report.append(" %</td>");
				_report.append("</tr>");
				offset++;
			}
	
			_report.append("</table>");
			_report.append("</td>\n");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td colspan=\"2\">&nbsp;</td>");
			_report.append("</tr>\n");
	
			_report.append("<tr>\n");
			_report.append("<td style=\"width: 150px; vertical-align: top; text-align: center; font-size: 14px; background-color: #6e8898;\" colspan=\"2\"><strong>Backup status</strong></td>");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td colspan=\"2\">&nbsp;</td>");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Start time</strong></td>");
			_report.append("</td><td style=\"text-align: left; font-size: 14px;\">");
			if(this._end.get(Calendar.HOUR_OF_DAY) < 10) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.HOUR_OF_DAY));
			_report.append(":");
			if(this._end.get(Calendar.MINUTE) < 10) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.MINUTE));
			_report.append(" ");
			if(this._end.get(Calendar.DAY_OF_MONTH) < 10) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.DAY_OF_MONTH));
			_report.append("/");
			if(this._end.get(Calendar.MONTH) < 9) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.MONTH) + 1);
			_report.append("/");
			_report.append(this._end.get(Calendar.YEAR));
			_report.append("</td>\n");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>End time</strong></td>");
			_report.append("</td><td style=\"text-align: left; font-size: 14px;\">");
			if(this._end.get(Calendar.HOUR_OF_DAY) < 10) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.HOUR_OF_DAY));
			_report.append(":");
			if(this._end.get(Calendar.MINUTE) < 10) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.MINUTE));
			_report.append(" ");
			if(this._end.get(Calendar.DAY_OF_MONTH) < 10) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.DAY_OF_MONTH));
			_report.append("/");
			if(this._end.get(Calendar.MONTH) < 9) {
				_report.append("0");
			}
			_report.append(this._end.get(Calendar.MONTH) + 1);
			_report.append("/");
			_report.append(this._end.get(Calendar.YEAR));
			_report.append("</td>\n");
			_report.append("</tr>\n");
			_report.append("<tr>\n");
			_report.append("<td colspan=\"2\">&nbsp;</td>");
			_report.append("</tr>\n");
	
			if(!result.isEmpty()) {
				_report.append("<tr>\n");
				_report.append("<td style=\"vertical-align: top; font-size: 14px;\" colspan=\"2\">");
				_report.append("<table style=\"width: 90%; border: 0px; font-size: 14px; text-align: center;\" align=\"center\">");
				_report.append("<tr>");
				_report.append("<td></td>");
				_report.append("<td><strong>Job</strong></td>");
				_report.append("<td><strong>Client</strong></td>");
				_report.append("<td><strong>Type</strong></td>");
				_report.append("<td><strong>Start date</strong></td>");
				_report.append("<td><strong>End date</strong></td>");
				_report.append("<td><strong>Status</strong></td>");
				_report.append("<td><strong>Errors</strong></td>");
				_report.append("<td><strong>Files</strong></td>");
				_report.append("<td><strong>Size</strong></td>");
				_report.append("</tr>");
	
				offset = 0;
				for (Map<String, Object> row : result) {
					Map<String, String> _client = _cm.getClient(String.valueOf(row.get("clientname")));
					if (category != null && !category.isEmpty()
							&& !category.equals(_client.get("category"))) {
						continue;
					}
	
					long size = 0;
					_sb = new StringBuilder();
					_report.append("<tr");
					if (!"0".equals(String.valueOf(row.get("joberrors")))
							|| !"T".equals(String.valueOf(row.get("status")))) {
						_report.append(" style=\"background: #fdbebe;\"");
					} else if (!"0".equals(String.valueOf(row.get("joberrors")))) {
						_report.append(" style=\"background: #e9f0ba;\"");
					} else if (offset % 2 == 0) {
						_report.append(" style=\"background: #e2e3e8;\"");
					}
					_report.append(">");
					_report.append("<td>");
					_report.append("</td>");
					_report.append("<td>");
					_report.append(String.valueOf(row.get("jobname")));
					_report.append("</td>");
					_report.append("<td>");
					_report.append(String.valueOf(row.get("clientname")));
					_report.append("</td>");
					_report.append("<td>");
					_report.append(String.valueOf(row.get("level")));
					_report.append("</td>");
					_report.append("<td>");
					try {
						_report.append(_dateFormat.format(row.get("starttime")));
					} catch (Exception _ex) {
						_report.append("&nbsp;");
					}
					_report.append("</td>");
					_report.append("<td>");
					try {
						_report.append(_dateFormat.format(row.get("endtime")));
					} catch (Exception _ex) {
						_report.append("&nbsp;");
					}
					_report.append("</td>");
					_report.append("<td>");
					_report.append(String.valueOf(row.get("statusname")));
					_report.append("</td>");
					_report.append("<td>");
					_report.append(String.valueOf(row.get("joberrors")));
					_report.append("</td>");
					_report.append("<td>");
					_report.append(String.valueOf(row.get("jobfiles")));
					_report.append("</td>");
					if (row.get("jobbytes") == null) {
						size = 0;
					} else if (row.get("jobbytes") instanceof Double) {
						size = ((Double) row.get("jobbytes")).longValue();
					} else if (row.get("jobbytes") instanceof Long) {
						size = (Long) row.get("jobbytes");
					} else if (row.get("jobbytes") instanceof Integer) {
						size = ((Integer) row.get("jobbytes")).longValue();
					}
					if (size >= 1073741824) {
						_sb.append(Math.rint((size / 1073741824) * 100) / 100);
						_sb.append(" GB");
					} else if (size >= 1048576) {
						_sb.append(Math.rint((size / 1048576) * 100) / 100);
						_sb.append(" MB");
					} else if (size >= 1024) {
						_sb.append(Math.rint((size / 1024) * 100) / 100);
						_sb.append(" KB");
					} else {
						_sb.append(size);
						_sb.append(" B");
					}
					_report.append("<td>");
					_report.append(_sb.toString());
					_report.append("</td>");
					_report.append("</tr>");
					offset++;
				}
				_report.append("</table>");
				_report.append("</td>\n");
				_report.append("</tr>\n");
			}
			_report.append("<tr>\n");
			_report.append("<td colspan=\"2\">&nbsp;</td>");
			_report.append("</tr>\n");
			_report.append("</body>\n");
			_report.append("</html>\n");
			logger.debug("Daily report, category [{}] generated successfully", category);
			return _report.toString();
		} catch (Exception ex) {
			logger.error("Error generation daily report, category [{}]. Ex: {}", category, ex.getMessage());
			throw ex;
		}
	}
	
	public void sendInfoMail(int type, String text, String subject) throws Exception {
		LicenseManager _lm = new LicenseManager();
		String _account = this._sc.getMailReportAccount();
		if((_account == null || _account.isEmpty())) {
			return;
		}
		
		Configuration _tc = new Configuration();
		NetworkManager _nm = new NetworkManager(this._c);
		_tc.setProperty("mail.host", "localhost");
		
		Mail _m = new Mail(_tc);
		if(this._sc.getMailReportAccount() != null) {
			_m.addTo(_account);
		} else {
			return;
		}
		com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration _sc = new com.whitebearsolutions.imagine.wbsairback.configuration.GeneralSystemConfiguration();
		_m.setFrom("WBSAIRBACK-WD", _sc.getMailFromAccount());
		_m.setSubject(subject.toString());
		_m.setHTML(buildHtmlReportEmail(subject, text, type, _lm, _nm));
		_m.send();
	}
	
	public static String buildHtmlReportEmail(String _subject, String text, int type, LicenseManager _lm, NetworkManager _nm) throws Exception {
		try {
			StringBuilder _text = new StringBuilder();
			_text.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
			_text.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"es\">\n");
			_text.append("<head></head>\n");
			_text.append("<body>\n");
			_text.append("<table style=\"width: 95%; border: 1px solid #585f64;\">\n");
			_text.append("<tr>\n");
			_text.append("<td colspan=\"2\" style=\"text-align: center; font-size: 14px;\"><strong>WATCHDOG ");
			if(type == Watchdog.ERROR_GENERAL || type == Watchdog.ERROR_SERVICE || type == Watchdog.ERROR_SYSTEM || type == Watchdog.ERROR_TASK) {
				_text.append("ERROR");
			} else {
				_text.append("RECOVERY");
			}
			_text.append(" REPORT</strong></td>");
			_text.append("</tr>\n");
			_text.append("<tr>\n");
			_text.append("<td colspan=\"2\">&nbsp;</td>");
			_text.append("</tr>\n");
			_text.append("<tr>\n");
			_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Product</strong></td>");
			_text.append("</td><td style=\"text-align: left; font-size: 14px;\"><strong><span style=\"color: #c3c3c3\">WBS</span><span style=\"color: #00386e\">Airback</span></strong></td>\n");
			_text.append("</tr>\n");
			_text.append("<tr>\n");
			_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>UUID</strong></td>");
			_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
			_text.append(_lm.getUnitUUID());
			_text.append(" (community)");
			_text.append("</td>\n");
			_text.append("</tr>\n");
			_text.append("<tr>\n");
			_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Date and time</strong></td>");
			_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
			Calendar _cal = Calendar.getInstance();
			_text.append(_cal.get(Calendar.HOUR_OF_DAY));
			_text.append(":");
			_text.append(_cal.get(Calendar.MINUTE));
			_text.append(" ");
			_text.append(_cal.get(Calendar.DAY_OF_MONTH));
			_text.append("/");
			_text.append(_cal.get(Calendar.MONTH) + 1);
			_text.append("/");
			_text.append(_cal.get(Calendar.YEAR));
			_text.append("</td>\n");
			_text.append("</tr>\n");
			_text.append("<tr>\n");
			_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Error</strong></td>");
			_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
			_text.append(_subject.toString());
			_text.append("</td>\n");
			_text.append("</tr>\n");
			if(text != null) {
				_text.append("<tr>\n");
				_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Error description</strong></td>");
				_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
				_text.append(text);
				_text.append("</td>\n");
				_text.append("</tr>\n");
			}
			_text.append("<tr>\n");
			_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Memory</strong></td>");
			_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
			_text.append(GeneralSystemConfiguration.getMemoryLoad());
			_text.append(" %</td>\n");
			_text.append("</tr>\n");
			try {
				Map<String, Integer> partitions = GeneralSystemConfiguration.getDiskLoad();
				_text.append("<tr>\n");
				_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Partitions</strong></td>");
				_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
				for(String partition : partitions.keySet()) {
					if(partition.contains(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
						String _path = partition.replace(WBSAirbackConfiguration.getDirectoryVolumeMount(), "");
						if(_path.startsWith("/shares/")) {
							_path = _path.substring(8);
						} else if(_path.startsWith("/")) {
							_path = _path.substring(1);
						}
						_text.append(_path);
					} else if(partition.startsWith("/dev/")) {
						String _path = partition.replace("/dev/", "");
						_text.append(_path);
					} else {
						_text.append(partition);
					}
					_text.append(" (");
					_text.append(partitions.get(partition));
					_text.append(" %)<br/>");
				}
				_text.append("</td>\n");
				_text.append("</tr>\n");
			} catch(Exception _ex) {}
			_text.append("<tr>\n");
			_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Network interfaces</strong></td>");
			_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
			for(String iface : _nm.getSystemInterfaces()) {
				_text.append(iface);
				_text.append(" (");
				_text.append(NetworkManager.addressToString(_nm.getAddress(iface)));
				_text.append("/");
				_text.append(NetworkManager.addressToString(_nm.getNetmask(iface)));
				_text.append(")<br/>");
				try {
					String _value = Command.systemCommand("mii-tool " + iface);
					if(_value != null && !_value.isEmpty()) {
						_text.append("&nbsp;&nbsp;<span style=\"color: #c3c3c3\">");
						_text.append(_value.substring(_value.indexOf(":") + 1).trim());
						_text.append("</span><br/>");
					}
				} catch(Exception _ex) {}
			}
			_text.append("</td>\n");
			_text.append("</tr>\n");
			
			// RAID REPORT
			if (RaidManager.hasRaidController()) {
				_text.append("<tr>\n");
				_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Raid Info</strong></td>");
				_text.append("</td><td style=\"text-align: left; font-size: 14px;\">");
				_text.append(RaidManager.getHtmlReport());
				_text.append("</td>\n");
				_text.append("</tr>\n");
			}
			
			_text.append("<tr>\n");
			_text.append("<td style=\"width: 150px; vertical-align: top; text-align: right; font-size: 14px; background-color: #6e8898;\"><strong>Package list</strong></td>");
			_text.append("</td><td style=\"text-align: left; font-size: 12px;\">");
			try {
				String _list = Command.systemCommand("dpkg -l | awk '{ print $2, $3 }'");
				if(_list == null || _list.isEmpty()) {
					throw new Exception();
				}
				for(String _line : _list.split("\n")) {
					if(_line.matches("^[a-zA-Z0-9-._+]+\\ [0-9][a-zA-Z0-9-._+:~]+")) {
						_text.append(_line);
						_text.append("<br/>");
					}
				}
			} catch(Exception _ex) {
				logger.error("Error al intentar interpretar el comando: dpkg -l | awk '{ print $2, $3 }'");
				_text.append("packages cannot be displayed");
			}
			_text.append("</td>\n");
			_text.append("</tr>\n");
			_text.append("</body>\n");
			_text.append("</html>\n");
			return _text.toString();
		} catch (Exception ex) {
			logger.error("Error construyendo un mail con type:{}, subject:{}. Exception: {}", new Object[]{type,_subject, ex.getMessage()});
			throw ex;
		}
	}
}
