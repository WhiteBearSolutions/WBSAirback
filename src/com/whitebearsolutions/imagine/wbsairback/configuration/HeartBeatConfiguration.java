package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.FileOutputStream;

import com.whitebearsolutions.util.Command;

public class HeartBeatConfiguration {

	
	public static void heartbeatConfigureAll(String pairNode, String myAddress, String virtual_address, String deviceLun, String iface) throws Exception {
		heartbeatRemoveInitFiles();
		heartbeatWriteHaCf(pairNode, myAddress);
		heartbeatWriteHaResources(virtual_address, deviceLun, iface);
		heartbeatWriteHAAuthKeys();
	}
	
	public static void heartbeatRemoveInitFiles() throws Exception {
		 try {
	        	Command.systemCommand("rm /var/lib/heartbeat/hb_uuid");
	        } catch (Exception ex){}
	        try {
	        	Command.systemCommand("rm /var/lib/heartbeat/hb_generation");
	        } catch (Exception ex){}
	}
	
	
	public static void heartbeatWriteHAAuthKeys() throws Exception {
		StringBuilder _sb = new StringBuilder();
        _sb.append("auth 1\n");
        _sb.append("1 md5 12fefdsfvg09444444444444gvn3345\n");
        
        FileOutputStream _fos = new FileOutputStream("/etc/ha.d/authkeys");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
        
        Command.systemCommand("/bin/chmod 600 /etc/ha.d/authkeys");
	}
	
	
	public static void heartbeatWriteHaResources(String virtual_address, String deviceLun, String iface) throws Exception {
		StringBuilder _sb = new StringBuilder();
        if (deviceLun == null || deviceLun.equals("")) {
        	_sb.append("master drbddisk::rdata ");
        	_sb.append("Filesystem::/dev/drbd0::/rdata::ext3 ");
        } else {
        	_sb.append("master ");
        	_sb.append("Filesystem::");
        	_sb.append(deviceLun);
        	_sb.append("::/rdata::ext3 ");
        }
        _sb.append("wbsairback-volumes-ha ");
        _sb.append("PostgreIfRdata ");
        _sb.append("iscsi-scst ");
        //_sb.append("nfs-kernel-server ");
        //_sb.append("samba ");
        _sb.append("proftpd ");
        _sb.append("bacula-sd ");
        _sb.append("bacula-fd ");
        _sb.append("bacula-dir ");
        if (iface != null && !iface.isEmpty())
        	_sb.append("IPaddr2::" + virtual_address + "/24/"+iface+":0\n");
        else
        	_sb.append("IPaddr::" + virtual_address + "/24\n");
        FileOutputStream _fos = new FileOutputStream("/etc/ha.d/haresources");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
	}
	
	
	public static void heartbeatWriteHaCf(String pairNode, String networkIface) throws Exception {
		StringBuilder _sb = new StringBuilder();
        _sb.append("logfile /var/log/heartbeat.log\n");
        _sb.append("logfacility local0\n");
        _sb.append("keepalive 2\n");
        _sb.append("warntime 5\n");
        _sb.append("deadtime 60\n");
        _sb.append("auto_failback on\n");
        _sb.append("initdead 120\n");
        _sb.append("hopfudge 1\n");
        _sb.append("udpport 694\n");
        _sb.append("ucast " + networkIface + " "+pairNode+"\n");
        _sb.append("node master\n");
        _sb.append("node slave");
        
        FileOutputStream _fos = new FileOutputStream("/etc/ha.d/ha.cf");
        _fos.write(_sb.toString().getBytes());
        _fos.close();
	}
}
