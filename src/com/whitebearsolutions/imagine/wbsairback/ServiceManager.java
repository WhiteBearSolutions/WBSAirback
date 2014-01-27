package com.whitebearsolutions.imagine.wbsairback;


import java.io.File;

import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.util.Command;

public class ServiceManager {
	public static final int BACULA_SD = 1;
	public static final int BACULA_DIR = 2;
	public static final int BACULA_FD = 3;
	public static final int POSTGRES = 4;
	public static final int HEARTBEAT = 5;
	public static final int DRBD = 6;
	public static final int NTP = 7;
	public static final int ISCSI_TARGET = 8;
	public static final int ISCSI_INITIATOR = 9;
	public static final int SAMBA = 10;
	public static final int NFS = 11;
	public static final int SSH = 12;
	public static final int SNMP = 13;
	public static final int WATCHDOG = 14;
	public static final int WINBIND = 15;
	public static final int FTP = 16;
	public static final int RSYNC = 17;
	public static final int WGUI = 18;
	public static final int VTL = 19;
	public static final int CLUSTER = 20;
	public static final int WBSAIRBACK_DRBD = 21;
	public static final int MULTIPATHD = 22;
	public static final int MONGODB = 23;
	public static final int SNMPTRAP = 24;
	
	public static void stop(int service) throws Exception {
		try {
			switch(service) {
				case BACULA_SD:
					if(isRunning(BACULA_SD)) {
	    				Command.systemCommand("/etc/init.d/bacula-sd stop");
	    			}
					break;
				case BACULA_DIR:
					if(isRunning(BACULA_DIR)) {
	    				Command.systemCommand("/etc/init.d/bacula-dir stop");
	    			}
					break;
				case BACULA_FD:
					if(isRunning(BACULA_FD)) {
	    				Command.systemCommand("/etc/init.d/bacula-fd stop");
	    			}
					break;
				case POSTGRES:
					if(isRunning(POSTGRES)) {
	    				Command.systemCommand("/etc/init.d/postgresql stop");
	    			}
					break;
				case DRBD:
					if(isRunning(DRBD)) {
	    				Command.systemCommand("/etc/init.d/drbd stop");
	    			}
					break;
				case WBSAIRBACK_DRBD:
					if(isRunning(WBSAIRBACK_DRBD)) {
	    				Command.systemCommand("/etc/init.d/wbsairback-drbd stop");
	    			}
					break;
				case NTP:
					if(isRunning(NTP)) {
						Command.systemCommand("/etc/init.d/ntp stop");
					}
					break;
				case SAMBA:
					if(isRunning(SAMBA)) {
						Command.systemCommand("/etc/init.d/samba stop");
					}
					break;
				case WINBIND:
					if(isRunning(WINBIND)) {
						Command.systemCommand("/etc/init.d/winbind stop");
					}
					break;
				case NFS:
					if(isRunning(NFS)) {
						Command.systemCommand("/etc/init.d/nfs-kernel-server stop");
					}
					break;
				case FTP:
					if(isRunning(FTP)) {
						Command.systemCommand("/etc/init.d/proftpd stop");
					}
					break;
				case ISCSI_TARGET:
					if(isRunning(ISCSI_TARGET)) {
						Command.systemCommand("/etc/init.d/iscsi-scst stop");
					}
					break;
				case ISCSI_INITIATOR:
					if(isRunning(ISCSI_INITIATOR)) {
						Command.systemCommand("/etc/init.d/open-iscsi stop");
					}
					break;
				case HEARTBEAT:
					if(isRunning(HEARTBEAT)) {
	    					Command.systemCommand("/etc/init.d/heartbeat stop");
	    				}
					break;
				case CLUSTER:
					if(isRunning(CLUSTER)) {
	    					Command.systemCommand("/etc/init.d/cman stop");
	    				}
					break;
				case SSH:
					if(isRunning(SSH)) {
						Command.systemCommand("/etc/init.d/ssh stop");
					}
					break;
				case SNMP:
					if(isRunning(SNMP)) {
						Command.systemCommand("/etc/init.d/snmpd stop");
					}
					break;
				case RSYNC:
					if(isRunning(RSYNC)) {
						Command.systemCommand("/etc/init.d/wbsairback-sync stop");
					}
					break;
				case WATCHDOG:
					if(isRunning(WATCHDOG)) {
	    					Command.systemCommand("/etc/init.d/wbs-watchdog stop");
					}
					break;
				case VTL:
					if(isRunning(VTL)) {
	    					Command.systemCommand("/etc/init.d/mhvtl stop");
					}
					break;
				case WGUI:
					Command.systemCommand("/etc/init.d/wbsairback-admin restart");
					break;
				case MULTIPATHD:
					Command.systemCommand("/usr/bin/killall multipathd");
					break;
				case MONGODB:
					if(isRunning(MONGODB)) {
						Command.systemCommand("/etc/init.d/mongodb stop");
					}
					break;
				case SNMPTRAP:
					if(isRunning(SNMPTRAP)) {
						Command.systemCommand("/usr/bin/killall snmptrapd");
					}
					break;
				default:
					throw new Exception("undefined service"); 
			}
		} catch(Exception _ex) {}
	}
	
	public static void fullStop(int service) throws Exception {
		stop(service);
		for(int i = 0; isRunning(service) && i < 80; i++) {
			Thread.sleep(250);
		}
		if(isRunning(service)) {
			kill(service);
		}
	}
	
	private static void kill(int service) throws Exception {
		try {
			switch(service) {
				case BACULA_SD:
					Command.systemCommand("/usr/bin/killall bacula-sd");
					break;
				case BACULA_DIR:
					Command.systemCommand("/usr/bin/killall bacula-dir");
					break;
				case BACULA_FD:
					Command.systemCommand("/usr/bin/killall bacula-fd");
					break;
				case POSTGRES:
					Command.systemCommand("/usr/bin/killall postgres");
					break;
				case DRBD:
					Command.systemCommand("/usr/bin/killall drbd");
					break;
				case WBSAIRBACK_DRBD:
					Command.systemCommand("/usr/bin/killall wbsairback-drbd");
					break;
				case NTP:
					Command.systemCommand("/usr/bin/killall ntpd");
					break;
				case SAMBA:
					Command.systemCommand("/usr/bin/killall smbd");
					break;
				case WINBIND:
					Command.systemCommand("/usr/bin/killall winbindd");
					break;
				case NFS:
					/*
					 * none
					 */
					break;
				case FTP:
					Command.systemCommand("/usr/bin/killall proftpd");
					break;
				case ISCSI_TARGET:
					Command.systemCommand("/usr/bin/killall -9 iscsi-scstd");
					break;
				case ISCSI_INITIATOR:
					Command.systemCommand("/usr/bin/killall -9 iscsid");
					break;
				case HEARTBEAT:
					Command.systemCommand("/usr/bin/killall heartbeat");
					break;
				case CLUSTER:
					Command.systemCommand("/usr/bin/killall cman");
					break;
				case SSH:
					/*
					 * none
					 */
					break;
				case SNMP:
					Command.systemCommand("/usr/bin/killall snmpd");
					break;
				case WATCHDOG:
					Command.systemCommand("/usr/bin/killall wbs-watchdog");
					break;
				case RSYNC:
					/*
					 * none
					 */
					break;
				case VTL: {
						Command.systemCommand("/usr/bin/killall -9 vtltape");
						Command.systemCommand("/usr/bin/killall -9 vtllibrary");
					}
					break;
				case MULTIPATHD:
					Command.systemCommand("/usr/bin/killall multipathd");
					break;
				case MONGODB:
					Command.systemCommand("/usr/bin/killall mongod");
					break;
				case SNMPTRAP:
					Command.systemCommand("/usr/bin/killall snmptrapd");
					break;
				default:
					throw new Exception("undefined service");
			}
		} catch(Exception _ex) {}
	}
	
	public static void start(int service) throws Exception {
		if(isRunning(service)) {
			return;
		}
		switch(service) {
			case BACULA_SD: {
					File _f = new File("/var/run/bacula-sd.9103.pid");
					if(!_f.exists()) {
						FileSystem.writeFile(_f, "19103");
						Command.systemCommand("/bin/chown bacula:bacula /var/run/bacula-sd.9103.pid");
					}
					Command.systemCommand("/etc/init.d/bacula-sd start");
					Thread.sleep(500L);
				}
				break;
			case BACULA_DIR: {
					File _f = new File("/var/run/bacula-dir.9101.pid");
					if(!_f.exists()) {
						FileSystem.writeFile(_f, "19101");
					    Command.systemCommand("/bin/chown bacula:bacula /var/run/bacula-dir.9101.pid");
					}
				    Command.systemCommand("/etc/init.d/bacula-dir start");
					Thread.sleep(500L);
				}
				break;
			case BACULA_FD:
				Command.systemCommand("/etc/init.d/bacula-fd start");
				Thread.sleep(500L);
				break;
			case POSTGRES:
				Command.systemCommand("/etc/init.d/postgresql start");
				Thread.sleep(2000L);
				break;
			case DRBD:
				Command.systemCommand("/etc/init.d/drbd start");
				Thread.sleep(500L);
				break;
			case WBSAIRBACK_DRBD:
				Command.systemCommand("/etc/init.d/wbsairback-drbd start");
				Thread.sleep(500L);
				break;
			case NTP:
				Command.systemCommand("/etc/init.d/ntp start");
				Thread.sleep(500L);
				break;
			case SAMBA:
				Command.systemCommand("/etc/init.d/samba start");
				Thread.sleep(500L);
				break;
			case WINBIND:
				Command.systemCommand("/etc/init.d/winbind start");
				Thread.sleep(500L);
				break;
			case NFS:
				Command.systemCommand("/etc/init.d/nfs-kernel-server start");
				Thread.sleep(500L);
				break;
			case FTP:
				Command.systemCommand("/etc/init.d/proftpd start");
				Thread.sleep(500L);
				break;
			case ISCSI_TARGET:
				Command.systemCommand("/etc/init.d/iscsi-scst start");
				Thread.sleep(500L);
				break;
			case ISCSI_INITIATOR:
				Command.systemCommand("/etc/init.d/open-iscsi start");
				Thread.sleep(500L);
				break;
			case HEARTBEAT:
				Command.systemCommand("/etc/init.d/heartbeat start");
				Thread.sleep(500L);
				break;
			case CLUSTER:
				Command.systemCommand("/etc/init.d/cman start");
				Thread.sleep(500L);
				break;
			case SSH:
				Command.systemCommand("/etc/init.d/ssh start");
				break;
			case SNMP:
				Command.systemCommand("/etc/init.d/snmpd start");
				break;
			case RSYNC:
				Command.systemCommand("/etc/init.d/wbsairback-sync start");
				break;
			case WATCHDOG:
				Command.systemCommand("/etc/init.d/wbs-watchdog start");
				break;
			case WGUI:
				Command.systemCommand("/etc/init.d/wbsairback-admin start");
				break;
			case VTL:
				Command.systemCommand("/etc/init.d/mhvtl start");
				break;
			case MULTIPATHD:
				Command.systemCommand("multipathd");
				break;
			case MONGODB:
				Command.systemCommand("/etc/init.d/mongodb start");
				break;
			case SNMPTRAP:
				Command.systemCommand("/usr/sbin/snmptrapd");
				break;
			default:
				throw new Exception("undefined service");
		}
	}
	
	public static void restart(int service) throws Exception {
		fullStop(service);
		start(service);
	}
	
	public static void initialize(int service) throws Exception {
		remove(service);
		switch(service) {
			case BACULA_SD:
				Command.systemCommand("insserv bacula-sd");
				break;
			case BACULA_DIR:
				Command.systemCommand("insserv bacula-dir");
				break;
			case BACULA_FD:
				Command.systemCommand("insserv bacula-fd");
				break;
			case POSTGRES:
				Command.systemCommand("insserv postgresql");
				break;
			case SAMBA:
				Command.systemCommand("insserv samba");
				break;
			case WINBIND:
				Command.systemCommand("insserv winbind");
				break;
			case DRBD:
				Command.systemCommand("insserv drbd");
				break;
			case WBSAIRBACK_DRBD:
				Command.systemCommand("insserv wbsairback-drbd");
				break;
			case HEARTBEAT:
				Command.systemCommand("insserv heartbeat");
				break;
			case CLUSTER:
				Command.systemCommand("insserv cman");
				break;
			case ISCSI_INITIATOR:
				Command.systemCommand("insserv open-iscsi");
				break;
			case ISCSI_TARGET:
				Command.systemCommand("insserv iscsi-scst");
				break;
			case SNMP:
				Command.systemCommand("insserv snmpd");
				break;
			case SSH:
				Command.systemCommand("insserv ssh");
				break;
			case RSYNC:
				Command.systemCommand("insserv wbsairback-sync");
				break;
			case WATCHDOG:
				Command.systemCommand("insserv wbs-watchdog");
				break;
			case NFS:
				Command.systemCommand("insserv nfs-kernel-server");
				break;
			case FTP:
				Command.systemCommand("insserv proftpd");
				break;
			case MONGODB:
				Command.systemCommand("insserv mongodb");
				break;
			default:
				throw new Exception("undefined service");
		}
		start(service);
	}
	
	public static void shutdownSystem() throws Exception {
		Command.systemCommand("/sbin/poweroff");
	}
	
	public static void restartSystem() throws Exception {
		Command.systemCommand("/sbin/reboot");
	}
	
	public static void restartWebAdministration() throws Exception {
		Command.asyncSystemCommand("/etc/init.d/wbsairback-admin restart > /dev/null &");
	}
	
	public static boolean isRunning(int service) throws Exception {
		switch(service) {
			case BACULA_SD:
				if(Command.isRunning("/opt/bacula/bin/bacula-sd")) {
					return true;
				}
				break;
			case BACULA_DIR:
				if(Command.isRunning("/opt/bacula/bin/bacula-dir")) {
					return true;
				}
				break;
			case BACULA_FD:
				if(Command.isRunning("/opt/bacula/bin/bacula-fd")) {
					return true;
				}
				break;
			case POSTGRES:
				if(Command.isRunning("postgres")) {
					return true;
				}
				break;
			case WBSAIRBACK_DRBD:
				if(Command.isRunning("wbsairback-drbd")) {
					return true;
				}
				break;
			case DRBD:
				if(Command.isRunning("drbd")) {
					return true;
				}
				break;
			case NTP:
				if(Command.isRunning("/usr/sbin/ntpd")) {
					return true;
				}
				break;
			case SAMBA:
				if(Command.isRunning("/usr/sbin/smbd")) {
					return true;
				}
				break;
			case WINBIND:
				if(Command.isRunning("/usr/sbin/winbindd")) {
					return true;
				}
				break;
			case NFS:
				if(Command.isRunning("/usr/sbin/rpc.mountd")) {
					return true;
				}
				break;
			case FTP:
				if(Command.isRunning("proftpd")) {
					return true;
				}
				break;
			case ISCSI_TARGET:
				if(Command.isRunning("/usr/sbin/iscsi-scstd")) {
					return true;
				}
				break;
			case ISCSI_INITIATOR:
				if(Command.isRunning("/usr/sbin/iscsid")) {
					return true;
				}
				break;
			case HEARTBEAT:
				if(Command.isRunning("heartbeat")) {
					return true;
				}
				break;
			case CLUSTER:
				if(Command.isRunning("cman")) {
					return true;
				}
				break;
			case SSH:
				if(Command.isRunning("/usr/sbin/sshd")) {
					return true;
				}
				break;
			case SNMP:
				if(Command.isRunning("/usr/sbin/snmpd")) {
					return true;
				}
				break;
			case RSYNC:
				if(Command.isRunning("/usr/bin/rsync --daemon")) {
					return true;
				}
				break;
			case WATCHDOG:
				if(Command.isRunning("/usr/share/wbsairback/java/bin/java -DWD")) {
					return true;
				}
				break;
			case VTL: {
					if(Command.isRunning("vtltape")) {
						return true;
					} else if(Command.isRunning("vtllibrary")) {
						return true;
					}
				}
				break;
			case MULTIPATHD:
				if(Command.isRunning("multipathd")) {
					return true;
				}
				break;
			case MONGODB:
				if(Command.isRunning("/usr/bin/mongod")) {
					return true;
				}
				break;
			case SNMPTRAP:
				if(Command.isRunning("/usr/sbin/snmptrapd")) {
					return true;
				}
				break;
			default:
				throw new Exception("undefined service");
		}
		return false;
	}
	
	public static void remove(int service) throws Exception {
		fullStop(service);
		switch(service) {
			case BACULA_SD:
				Command.systemCommand("insserv -r bacula-sd");
				break;
			case BACULA_DIR:
				Command.systemCommand("insserv -r bacula-dir");
				break;
			case BACULA_FD:
				Command.systemCommand("insserv -r bacula-fd");
				break;
			case POSTGRES:
				Command.systemCommand("insserv -r postgresql");
				break;
			case SAMBA:
				Command.systemCommand("insserv -r samba");
				break;
			case WINBIND:
				Command.systemCommand("insserv -r winbind");
				break;
			case DRBD:
				Command.systemCommand("insserv -r drbd");
				break;
			case WBSAIRBACK_DRBD:
				Command.systemCommand("insserv -r wbsairback-drbd");
				break;
			case HEARTBEAT:
				Command.systemCommand("insserv -r heartbeat");
				break;
			case CLUSTER:
				Command.systemCommand("insserv -r cman");
				break;
			case ISCSI_TARGET:
				Command.systemCommand("insserv -r iscsi-scst");
				break;
			case ISCSI_INITIATOR:
				Command.systemCommand("insserv -r open-iscsi");
				break;
			case SSH:
				Command.systemCommand("insserv -r ssh");
				break;
			case SNMP:
				Command.systemCommand("insserv -r snmpd");
				break;
			case RSYNC:
				Command.systemCommand("insserv -r wbsairback-sync");
				break;
			case WATCHDOG:
				Command.systemCommand("insserv -r wbs-watchdog");
				break;
			case NFS:
				Command.systemCommand("insserv -r nfs-kernel-server");
				break;
			case FTP:
				Command.systemCommand("insserv -r proftpd");
				break;
			case VTL:
				Command.systemCommand("insserv -r mhvtl");
				break;
			case MONGODB:
				Command.systemCommand("insserv -r mongodb");
				break;
			default:
				throw new Exception("undefined service");
		}
	}
}
