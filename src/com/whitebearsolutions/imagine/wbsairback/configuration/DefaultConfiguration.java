package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.disk.CloudManager;
import com.whitebearsolutions.imagine.wbsairback.disk.MultiPathManager;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.imagine.wbsairback.net.VTLManager;
import com.whitebearsolutions.security.PasswordHandler;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class DefaultConfiguration extends WBSAirbackConfiguration {
	
	public static void init(String password, String mail) throws Exception {
		String _message = "fail to erase data";
		try {
			String passBacula = "bacula";
			String passPostgres = "postgres";
			
			_message = "fail to create default configuration file";
			Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration()));
			defaultConfiguration(_c, passBacula, password, passPostgres);
			
			_message = "fail stopping services";
			stopServices();
			
			/*
			 // TODO: uncoment with bacula version
			 * _message = "fail to create backup configuration";
			configureDefaultBacula(passBacula, password, mail, _c);*/
			if (mail != null && !mail.isEmpty()) {
				GeneralSystemConfiguration sc = new GeneralSystemConfiguration();
				sc.setMailReportAccount(mail);
				sc.setMailFromAccount(mail);
			}
			
			_message = "fail to reinitializing system volumes";
			reinizializationVolumes(_c);
			
			_message = "fail erasing system dirs";
			reinitSystemDirs();
  		    
  			_message = "fail to configure base system";
  			configureHostName();
			createSystemConfiguration(_c);
			
			_message = "fail to configure kernel system params";
  			configureSystem();

			/*
			 // TODO: uncoment with bacula version
			 * _message = "fail to configure or create database";
			configureAndcreateDataBase(passPostgres, passBacula);
			
			_message = "fail to create database indexes";
			createDataBaseIndexes(_c);*/
			
			_message = "fail to create mailing configuration";
			createMailingConfiguration();

			_message = "fail to start services";
			restartServices();
		} catch(Exception _ex) {
			File _f = new File(WBSAirbackConfiguration.getFileConfiguration());
			if(_f.exists()) {
				_f.delete();
			}
			throw new Exception(_message + ": " + _ex.getMessage());
		}
	}
	
	public static void stopServices() throws Exception {
		ServiceManager.fullStop(ServiceManager.WATCHDOG);
		// TOOD: uncomment with bacula
		//ServiceManager.fullStop(ServiceManager.BACULA_FD);
		//ServiceManager.fullStop(ServiceManager.BACULA_SD);
		//ServiceManager.fullStop(ServiceManager.BACULA_DIR);
		ServiceManager.fullStop(ServiceManager.POSTGRES);
		Command.systemCommand("/usr/bin/killall -9 postgres || /bin/echo \"\"");
	}
	
	public static void createSystemConfiguration(Configuration _c) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("127.0.0.1\tlocalhost wbsairback");
		FileOutputStream _fos = new FileOutputStream("/etc/hosts");
		_fos.write(_sb.toString().getBytes());
		_fos.close();
		// TOOD: uncomment with bacula
		//ClientManager _cm = new ClientManager(_c);
		//_cm.setLocalClient(1, "months", 3, "months");
	}
	
	public static void configureAndcreateDataBase(String passPostgres, String passBacula) throws Exception {
		
		GeneralSystemConfiguration.setDataBaseConfiguration(500, -1, -1);
		
		Command.systemCommand("/bin/su - postgres -c \"/usr/lib/postgresql/" + GeneralSystemConfiguration.getDataBaseVersion() + "/bin/initdb -D /rdata/database --encoding=SQL_ASCII --locale=es_ES.euro\"");
		Command.systemCommand("/bin/cp /usr/share/wbsairback/database/server.* /rdata/database/");
		Command.systemCommand("/bin/chown postgres:postgres /rdata/database/server.* && /bin/chmod 600 /rdata/database/server.key");
		
		ServiceManager.start(ServiceManager.POSTGRES);
		Command.systemCommand("/bin/su - postgres -c \"/bin/bash /usr/share/wbsairback/bacula/drop_postgresql_database\" || /bin/echo \"\"");
		Command.systemCommand("/bin/su - postgres -c \"/bin/bash /usr/share/wbsairback/bacula/create_postgresql_database\"");
		Command.systemCommand("/bin/su - postgres -c \"/bin/bash /usr/share/wbsairback/bacula/make_postgresql_tables\"");
		Command.systemCommand("/bin/su - postgres -c \"/bin/bash /usr/share/wbsairback/bacula/grant_postgresql_privileges\"");
		
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/su - postgres -c \"/usr/lib/postgresql/" + GeneralSystemConfiguration.getDataBaseVersion() + "/bin/psql -f - -d airback <<END-OF-DATA\n");
		_sb.append("ALTER USER postgres WITH PASSWORD '");
		_sb.append(passPostgres);
		_sb.append("';\n");
		_sb.append("ALTER USER bacula WITH PASSWORD '");
		_sb.append(passBacula);
		_sb.append("';\n");
		_sb.append("END-OF-DATA\n\"");
		Command.systemCommand(_sb.toString());
	}
	
	public static void createDataBaseIndexes(Configuration _c) throws Exception {
		DBConnection _db = new DBConnectionManager(_c).getConnection();
		//_db.query("CREATE INDEX file_jobid_idx on file(jobid)");
		//_db.query("CREATE INDEX file_jpfid_idx on file (jobid, pathid, filenameid)");
		_db.query("CREATE INDEX file_filenameid_idx on file(filenameid)");
		_db.query("CREATE INDEX file_pathid_idx on file(pathid)");
		_db.query("CREATE INDEX path_pathid_idx on path(pathid)");
		_db.query("CREATE INDEX filename_filenameid_idx on filename(filenameid)");
		_db.query("CREATE INDEX job_clientid_idx on job(clientid)");
		_db.query("CREATE INDEX job_jobid_idx on job(jobid)");
	}
	
	public static void createMailingConfiguration() throws Exception {
		StringBuilder _sb = new  StringBuilder();
		_sb.append("smtpd_banner = $myhostname local SMTP\n");
		_sb.append("biff = no\n");
		_sb.append("append_dot_mydomain = no\n");
		_sb.append("readme_directory = no\n");
		_sb.append("myhostname = localhost\n");
		_sb.append("alias_maps = hash:/etc/aliases\n");
		_sb.append("alias_database = hash:/etc/aliases\n");
		_sb.append("mydestination = airback, localhost, localhost.localdomain, localhost\n");
		_sb.append("relayhost =\n");
		_sb.append("mynetworks = 127.0.0.0/8\n");
		_sb.append("mailbox_size_limit = 0\n");
		_sb.append("recipient_delimiter = +\n");
		_sb.append("inet_interfaces = 127.0.0.1\n");
		
		FileSystem.writeFile(new File("/etc/postfix/main.cf"), _sb.toString());
	}
	
	public static void restartServices() throws Exception {
		
		Command.systemCommand("/etc/init.d/postfix restart");
		ServiceManager.restart(ServiceManager.ISCSI_INITIATOR);
		ServiceManager.remove(ServiceManager.DRBD);
		ServiceManager.remove(ServiceManager.HEARTBEAT);
		Command.systemCommand("insserv rrdstats");
		Command.systemCommand("/etc/init.d/rrdstats restart");
		/*
		 // TODO: uncoment with bacula version
		 * ServiceManager.initialize(ServiceManager.POSTGRES);
		ServiceManager.initialize(ServiceManager.BACULA_SD);
		ServiceManager.initialize(ServiceManager.BACULA_DIR);
		ServiceManager.initialize(ServiceManager.BACULA_FD);*/
		ServiceManager.initialize(ServiceManager.ISCSI_TARGET);
		Command.systemCommand("insserv wbsairback-volumes");
		ServiceManager.start(ServiceManager.FTP);
		ServiceManager.start(ServiceManager.WATCHDOG);
		
		// TODO: uncoment with bacula version
		//Command.systemCommand("/bin/echo \"run job=SaveClient yes client=airback-fd\" | /usr/bin/bconsole");
	}
	
	public static void reinizializationVolumes(Configuration _c) throws Exception {
		VolumeManager _vm = new VolumeManager(_c);
		ShareManager _sm = new ShareManager(_c);
		
		for(Map<String, String> _share : ShareManager.getExternalShares()) {
			_sm.removeExternalShare(_share.get("server"), _share.get("share"), true);
		}
		for(Map<String, String> _share : ShareManager.getShares(ShareManager.NFS)) {
			ShareManager.removeShare(_share.get("vg"), _share.get("lv"), ShareManager.NFS);
		}
		for(Map<String, String> _share : ShareManager.getShares(ShareManager.CIFS)) {
			ShareManager.removeShare(_share.get("vg"), _share.get("lv"), ShareManager.CIFS);
		}
		for(Map<String, String> _share : ShareManager.getShares(ShareManager.FTP)) {
			ShareManager.removeShare(_share.get("vg"), _share.get("lv"), ShareManager.FTP);
		}
		/**
		 * TODO
		 * WEBDAV
		 */
		
		for(Map<String, String> volume : VolumeManager.getLogicalVolumes()) {
			VolumeManager.removeAllLogicalVolumeSnapshot(volume.get("vg"), volume.get("name"), VolumeManager.LV_SNAPSHOT_DAILY);
			VolumeManager.removeAllLogicalVolumeSnapshot(volume.get("vg"), volume.get("name"), VolumeManager.LV_SNAPSHOT_HOURLY);
			VolumeManager.removeAllLogicalVolumeSnapshot(volume.get("vg"), volume.get("name"), VolumeManager.LV_SNAPSHOT_MANUAL);
			_vm.removeLogicalVolume(volume.get("vg"), volume.get("name"));
			Thread.sleep(100);
		}
		
		try {
			for(String _name : VolumeManager.getVolumeGroupNames()) {
				VolumeManager.removeVolumeGroup(_name);
			}
		} catch (Exception ex){}
		
		Command.systemCommand("/bin/cat /dev/null > /etc/bacula/fstab");
		ISCSIManager.removeAllTargets();
	}
	
	public static void configureDefaultBacula(String passBacula, String password, String mail, Configuration _c) throws Exception {
		String passDir = ClientManager.getRandomChars(20);
		String passSd = ClientManager.getRandomChars(20);
		
		configBaculaDir(passDir, passSd, passBacula, password, mail, _c);
		GeneralSystemConfiguration _sc = new GeneralSystemConfiguration();
		_sc.configureMail(mail, mail, GeneralSystemConfiguration.BACULA_MAIL_LEVEL_ALL);
		
		StringBuilder _sb = new  StringBuilder();
		_sb.append("\n");
		FileSystem.writeFile(new File("/etc/bacula/bacula-pools.conf"), _sb.toString());
		
		configBaculaSd(passSd, _c);
		configBConsole(passDir);
		
		File _f = new File("/rdata/working/bacula-dir.9101.pid");
		FileSystem.writeFile(_f, "19101");
		
		_f = new File("/rdata/working/bacula-sd.9103.pid");
		FileSystem.writeFile(_f, "19103");
	}
	
	public static void configBaculaDir(String passDir, String passSd, String passBacula, String password, String mail, Configuration _c) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("#\n");
		_sb.append("#  Airback Central Director\n");
		_sb.append("#\n");
		_sb.append("\n");
		_sb.append("Director {\n");
		_sb.append("  Name = airback-dir\n");
		_sb.append("  DIRport = 9101\n");
		_sb.append("  QueryFile = \"/etc/bacula/query.sql\"\n");
		_sb.append("  WorkingDirectory = \"/rdata/working\"\n");
		_sb.append("  PidDirectory = \"/var/run\"\n");
		_sb.append("  Maximum Concurrent Jobs = 100\n");
		_sb.append("  Password = \"" + passDir + "\"\n");
		_sb.append("  Messages = Daemon\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Job {\n");
		_sb.append("  Name = \"SaveClient\"\n");
		_sb.append("  Type = Admin\n");
		_sb.append("  Level = Incremental\n");
		_sb.append("  Client = airback-fd \n");
		_sb.append("  FileSet = \"Catalog\"\n");
		_sb.append("  Storage = SystemStorage\n");
		_sb.append("  Messages = Standard\n");
		_sb.append("  Pool = DefaultPool\n");
		_sb.append("  Priority = 10\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Job {\n");
		_sb.append("  Name = \"RestoreFiles\"\n");
		_sb.append("  Type = Restore\n");
		_sb.append("  Client = airback-fd\n");
		_sb.append("  FileSet=\"Catalog\"\n");
		_sb.append("  Storage = SystemStorage  \n");
		_sb.append("  Pool = DefaultPool\n");
		_sb.append("  Messages = Standard\n");
		_sb.append("  Where = /tmp/bacula-restores\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Job {\n");
		_sb.append("  Name = \"RestoreFilesVmware\"\n");
		_sb.append("  Type = Restore\n");
		_sb.append("  Client = airback-fd\n");
		_sb.append("  FileSet=\"Catalog\"\n");
		_sb.append("  Storage = SystemStorage  \n");
		_sb.append("  Pool = DefaultPool\n");
		_sb.append("  Messages = Standard\n");
		_sb.append("  Where = /tmp/bacula-restores\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Storage {\n");
		_sb.append("  Name = " + _c.getProperty("bacula.defaultStorage") + "\n");
		_sb.append("  Address = 127.0.0.1\n");
		_sb.append("  SDPort = 9103\n");
		_sb.append("  Password = \""+ passSd + "\"\n");
		_sb.append("  Device = SystemStorage\n");
		_sb.append("  Media Type = File\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Catalog {\n");
		_sb.append("  Name = AirbackCatalog\n");
		_sb.append("  dbname = airback; user = bacula; password = \"" + passBacula + "\"\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Messages {\n");
		_sb.append("  Name = Standard\n");
		_sb.append("  mailcommand = \"/usr/sbin/bsmtp -h localhost -f \\\"\\(Airback\\) \\<%r\\>\\\" -s \\\"WBSAirback: %t %e of %c %l\\\" %r\"\n");
		_sb.append("  operatorcommand = \"/usr/sbin/bsmtp -h localhost -f \\\"\\(Airback\\) \\<%r\\>\\\" -s \\\"WBSAirback: Intervention needed for %j\\\" %r\"\n");
		if(password != null) {
			_sb.append("  mail = " + mail + " = error, fatal, !skipped\n");
			_sb.append("  operator = " + mail + " = mount\n");
		}
		_sb.append("  console = all, !skipped, !saved\n");
		_sb.append("  catalog = all, !skipped, !saved\n");
		_sb.append("  append = \"/var/bacula/working/log\" = all, !skipped\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Messages {\n");
		_sb.append("  Name = Daemon\n");
		_sb.append("  mailcommand = \"/usr/sbin/bsmtp -h localhost -f \\\"\\(Airback\\) \\<%r\\>\\\" -s \\\"WBSAirback daemon message\\\" %r\"\n");
		if(password != null) {
			_sb.append("  mail = " + mail + " = error, fatal, !skipped\n");
		}
		_sb.append("  console = all, !skipped, !saved\n");
		_sb.append("  catalog = all, !skipped\n");
		_sb.append("  append = \"/var/bacula/working/log\" = all, !skipped\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Pool {\n");
		_sb.append("  Name = " + _c.getProperty("bacula.defaultPool") + "\n");
		_sb.append("  Pool Type = Backup\n");
		_sb.append("  Recycle = yes\n");
		_sb.append("  AutoPrune = yes\n");
		_sb.append("  Volume Retention = 5 days\n");
		_sb.append("  LabelFormat = \"Vol\"\n");
		_sb.append("  Maximum Volume Bytes = 2g\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("FileSet {\n");
		_sb.append("  Name = \"" + _c.getProperty("bacula.defaultFileset") + "\"\n");
		_sb.append("  Include {\n");
		_sb.append("    Options {\n");
		_sb.append("      signature = MD5\n");
		_sb.append("    }\n");
		_sb.append("    File = /rdata/sqlexport/\n");
		_sb.append("  }\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("## WBSAIRBACK CLIENTS ##\n");
		_sb.append("\n");
		_sb.append("## WBSAIRBACK JOBS ##\n");
		_sb.append("\n");
		_sb.append("## WBSAIRBACK FILESETS ##\n");
		_sb.append("\n");
		_sb.append("## WBSAIRBACK POOLS ##\n");
		_sb.append("\n");
		_sb.append("## WBSAIRBACK SCHEDULES ##\n");
		_sb.append("\n");
		_sb.append("## WBSAIRBACK STORAGES ##\n");
		_sb.append("\n");
		
		FileSystem.writeFile(new File("/etc/bacula/bacula-dir.conf"), _sb.toString());
	}
	
	public static void configBaculaSd(String passSd, Configuration _c) throws Exception {
		File _f = new File("/rdata/" + _c.getProperty("bacula.defaultStorage")); 
	    if(_f.exists()) {
	    	Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
	    }
	    _f.mkdir();
		    
		StringBuilder _sb = new  StringBuilder();
		_sb.append("#\n");
		_sb.append("#  Airback Central Storage Daemon\n");
		_sb.append("#\n");
		_sb.append("\n");
		_sb.append("Storage {\n");
			_sb.append("  Name = airback-sd\n");
			_sb.append("  SDPort = 9103\n");
			_sb.append("  WorkingDirectory = \"/rdata/working\"\n");
			_sb.append("  Pid Directory = \"/var/run\"\n");
			_sb.append("  Maximum Concurrent Jobs = 100\n");
			_sb.append("  Plugin Directory = \"/opt/bacula/plugins\"\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Director {\n");
			_sb.append("  Name = airback-dir\n");
			_sb.append("  Password = \""+ passSd + "\"\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Device {\n");
			_sb.append("  Name = " + _c.getProperty("bacula.defaultStorage") + "\n");
			_sb.append("  Media Type = File\n");
			_sb.append("  Archive Device = "+ _f.getAbsolutePath() + "\n");
			_sb.append("  LabelMedia = yes\n");
			_sb.append("  Random Access = Yes\n");
			_sb.append("  AutomaticMount = yes\n");
			_sb.append("  RemovableMedia = no\n");
			_sb.append("  AlwaysOpen = no\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Messages {\n");
		_sb.append("  Name = Standard\n");
		_sb.append("  director = airback-dir = all\n");
		_sb.append("}\n");
		_sb.append("\n");
		
		FileSystem.writeFile(new File("/etc/bacula/bacula-sd.conf"), _sb.toString());
	}
	
	public static void configBConsole(String passDir) throws Exception {
		StringBuilder _sb = new  StringBuilder();
		_sb.append("#\n");
		_sb.append("#  Airback Central Console\n");
		_sb.append("#\n");
		_sb.append("\n");
		_sb.append("Director {\n");
		_sb.append("  Name = airback-dir\n");
		_sb.append("  DIRport = 9101\n");
		_sb.append("  address = localhost\n");
		_sb.append("  Password = \"");
		_sb.append(passDir);
		_sb.append("\"\n");
		_sb.append("}");
		_sb.append("\n");
		
		FileSystem.writeFile(new File("/etc/bacula/bconsole.conf"), _sb.toString());
	}
	
	public static void configureHostName() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("wbsairback");
		FileSystem.writeFile(new File("/etc/hostname"), _sb.toString());
		
		_sb = new StringBuilder();
		_sb.append("127.0.0.1\tlocalhost wbsairback\n");
		_sb.append("::1\tlocalhost ip6-localhost ip6-loopback\n");
		_sb.append("fe00::0\tip6-localnet\n");
		_sb.append("ff00::0\tip6-mcastprefix\n");
		_sb.append("ff02::1\tip6-allnodes\n");
		_sb.append("ff02::2\tip6-allrouters\n");
		FileSystem.writeFile(new File("/etc/hosts"), _sb.toString());
		
		Command.systemCommand("/bin/hostname wbsairback");
	}
	
	public static void configureSystem() throws Exception {
		Double shmmax = GeneralSystemConfiguration.getRecomendedSharedMemory();
		StringBuilder _sb = new StringBuilder();
		_sb.append("kernel.shmmax="+shmmax.longValue()+"\n");
		_sb.append("net.ipv4.tcp_fin_timeout=30\n");
		_sb.append("net.ipv4.tcp_keepalive_time=300\n");
		_sb.append("net.ipv4.tcp_keepalive_intvl=30\n");
		_sb.append("net.ipv4.tcp_keepalive_probes=5\n");
		_sb.append("net.core.wmem_max=6553600\n");
		_sb.append("net.core.rmem_max=8738000\n");
		_sb.append("net.ipv4.tcp_rmem=8192 873800 8738000\n");
		_sb.append("net.ipv4.tcp_wmem=4096 655360 6553600\n");
		_sb.append("vm.min_free_kbytes=65536\n");
				
		FileSystem.writeFile(new File("/etc/sysctl.conf"), _sb.toString());
		
		Command.systemCommand("/sbin/sysctl -w kernel.shmmax="+shmmax.longValue());
		Command.systemCommand("/sbin/sysctl -w net.ipv4.tcp_keepalive_time=300");
		Command.systemCommand("/sbin/sysctl -w net.ipv4.tcp_keepalive_intvl=30");
		Command.systemCommand("/sbin/sysctl -w net.ipv4.tcp_keepalive_probes=5");
		Command.systemCommand("/sbin/sysctl -w net.core.wmem_max=6553600");
		Command.systemCommand("/sbin/sysctl -w net.core.rmem_max=8738000");
		Command.systemCommand("/sbin/sysctl -w net.ipv4.tcp_rmem=\"8192 8738000 8738000\"");
		Command.systemCommand("/sbin/sysctl -w net.ipv4.tcp_wmem=\"4096 655360 6553600\"");
		Command.systemCommand("/sbin/sysctl -w vm.min_free_kbytes=65536");
	}
	
	public static void reinitSystemDirs() throws Exception {
		CloudManager.eraseAllCloudData();
		MultiPathManager.disableMultipath();
		VTLManager _vm = new VTLManager();
		_vm.deleteAllLibraries();
		
		File _f = new File("/opt/imagine/");
		if(!_f.exists()) {
			_f.mkdir();
		}
		_f = new File("/etc/wbsairback-admin/");
		if(!_f.exists()) {
			_f.mkdir();
		}
		_f = new File("/etc/bacula/filesets/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/clients/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/jobs/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/hypervisors/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/pools/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/schedules/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/storages/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/users/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/roles/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/etc/bacula/categories/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		_f = new File("/rdata/sqlexport/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		Command.systemCommand("/bin/chown postgres:postgres " + _f.getAbsolutePath());
		
		_f = new File("/rdata/database/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		Command.systemCommand("/bin/chown postgres:postgres " + _f.getAbsolutePath());
		
		_f = new File("/rdata/SystemStorage/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		
		_f = new File("/rdata/working/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath());
		}
		_f.mkdir();
		//TODO: uncomment with bacula 
		//Command.systemCommand("/bin/chown bacula:bacula " + _f.getAbsolutePath());
		
		_f = new File("/etc/iscsi/send_targets/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath() + "/*");
		}
		_f = new File("/etc/iscsi/nodes/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath() + "/*");
		}
		
		_f = new File("/etc/wbsairback-admin/replication/");
		if(_f.exists()) {
			Command.systemCommand("/bin/rm -fr " + _f.getAbsolutePath() + "/*");
		}
		
		_f = new File("/etc/wbsairback-admin/sync/sources.xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		_f = new File("/etc/wbsairback-admin/sync/destinations.xml");
		if(_f.exists()) {
			_f.delete();
		}
		
		_f = new File("/etc/wbsairback-admin/local-devices.xml");
		if(_f.exists()) {
			_f.delete();
		}
	}
	
	public static void defaultConfiguration(Configuration _c, String passBacula, String password, String passPostgres) throws Exception{
		_c.setProperty("system.password", PasswordHandler.generateDigest(password, null, "MD5"));
		_c.setProperty("system.version", "2.0");
		_c.setProperty("system.name", "wbsairback-central");
		_c.setProperty("database.url", "jdbc:postgresql://127.0.0.1/airback");
		_c.setProperty("database.user", "bacula");
		_c.setProperty("database.password", passBacula);
		_c.setProperty("database.driver", "org.postgresql.Driver");
		_c.setProperty("clients.maxentries", "20");
		_c.setProperty("bacula.defaultStorage", "SystemStorage");
		_c.setProperty("bacula.defaultPool", "DefaultPool");
		_c.setProperty("bacula.defaultFileset", "Catalog");
		_c.setProperty("bacula.defaultCatalog", "AirbackCatalog");
		_c.setProperty("bacula.maxClientJobs", "25");
		_c.setProperty("bacula.clientAutoPrune", "yes");
		_c.setProperty("webadministration.wwwdir", "/var/www/webadministration/");
		_c.setProperty("database.user", "postgres");
		_c.setProperty("database.password", passPostgres);
		_c.store();
	}
}
