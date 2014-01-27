package com.whitebearsolutions.imagine.wbsairback.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.db.DBConnection;
import com.whitebearsolutions.db.DBConnectionManager;
import com.whitebearsolutions.db.DBException;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class ClientManager {
	private Configuration _c;
	private DBConnection _db;
	
	private final static Logger logger = LoggerFactory.getLogger(ClientManager.class);
	
	public static final String baculaVersion = "6.4.5";
	public static final String baculaUnixVersion = baculaVersion+"-1";
	
	// DEBIAN
	public static final String SO_SQUEEZE_32 = "linux-debian-squeeze-32";
	public static final String SO_SQUEEZE_64 = "linux-debian-squeeze-64";
	public static final String SO_WHEEZE_32 = "linux-debian-wheeze-32";
	public static final String SO_WHEEZE_64 = "linux-debian-wheeze-64";
	public static final String SO_PRECISE_32 = "linux-debian-precise-32";
	public static final String SO_PRECISE_64 = "linux-debian-precise-64";
	
	// RED HAT
	public static final String SO_REDHAT5_32 = "linux-redhat-5-32";
	public static final String SO_REDHAT5_64 = "linux-redhat-5-64";
	public static final String SO_REDHAT6_32 = "linux-redhat-6-32";
	public static final String SO_REDHAT6_64 = "linux-redhat-6-64";
	
	// SUSE
	public static final String SO_SUSE10_32 = "linux-suse-10-32";
	public static final String SO_SUSE10_64 = "linux-suse-10-64";
	public static final String SO_SUSE11_32 = "linux-suse-11-32";
	public static final String SO_SUSE11_64 = "linux-suse-11-64";
	
	// OTHER LINUX
	public static final String SO_LINUX = "linux";
	
	// WIN
	public static final String SO_WIN_32 = "win32";
	public static final String SO_WIN_64 = "win64";
	
	// SOLARIS
	public static final String SO_SOLARIS_8_SPARC = "solaris-8-sparc";
	public static final String SO_SOLARIS_9_SPARC = "solaris-9-sparc";
	public static final String SO_SOLARIS_10_SPARC = "solaris-10-sparc";
	public static final String SO_SOLARIS_10_I86 = "solaris-10-i86";
	public static final String SO_SOLARIS_11_I86 = "solaris-11-i86";
	
	// MAC
	public static final String SO_MAC = "mac";
	
	public static Map<String, String> clientSupportedSOs;
	public static Map<String, String> clientInstaller1SOs;
	public static Map<String, String> clientInstaller2SOs;
	
	static {
		clientSupportedSOs = new LinkedHashMap<String, String>();
		clientSupportedSOs.put(SO_WIN_32, "Ms. Windows 2000/XP/2003/Vista/2008/7 (32 bit)");
		clientSupportedSOs.put(SO_WIN_64, "Ms. Windows 2000/XP/2003/Vista/2008/7 (64 bit)");
		
		clientSupportedSOs.put(SO_SQUEEZE_32, "Debian Squeeze Linux (32 bit)");
		clientSupportedSOs.put(SO_SQUEEZE_64, "Debian Squeeze Linux (64 bit)");
		clientSupportedSOs.put(SO_WHEEZE_32, "Debian Wheeze Linux (32 bit)");
		clientSupportedSOs.put(SO_WHEEZE_64, "Debian Wheeze Linux (64 bit)");
		clientSupportedSOs.put(SO_PRECISE_32, "Ubuntu Precise Pangolin Linux (32 bit)");
		clientSupportedSOs.put(SO_PRECISE_64, "Ubuntu Precise Pangolin Linux (64 bit)");

		clientSupportedSOs.put(SO_REDHAT5_32, "Red Hat Enterprise Linux 5 (32 bit)");
		clientSupportedSOs.put(SO_REDHAT5_64, "Red Hat Enterprise Linux 5 (64 bit)");
		clientSupportedSOs.put(SO_REDHAT6_32, "Red Hat Enterprise Linux 6 (32 bit)");
		clientSupportedSOs.put(SO_REDHAT6_64, "Red Hat Enterprise Linux 6 (64 bit)");
		
		clientSupportedSOs.put(SO_SUSE10_32, "Suse Linux Enterprise Server 10 (32 bit)");
		clientSupportedSOs.put(SO_SUSE10_64, "Suse Linux Enterprise Server 10 (64 bit)");
		clientSupportedSOs.put(SO_SUSE11_32, "Suse Linux Enterprise Server 11 (32 bit)");
		clientSupportedSOs.put(SO_SUSE11_64, "Suse Linux Enterprise Server 11 (64 bit)");
		
		clientSupportedSOs.put(SO_LINUX, "GNU/Linux");
		
		clientSupportedSOs.put(SO_SOLARIS_8_SPARC,"Solaris 8 (Sparc)");
		clientSupportedSOs.put(SO_SOLARIS_9_SPARC,"Solaris 9 (Sparc)");
		clientSupportedSOs.put(SO_SOLARIS_10_I86,"Solaris 9 (x86)");
		clientSupportedSOs.put(SO_SOLARIS_10_SPARC,"Solaris 10 (Sparc)");
		clientSupportedSOs.put(SO_SOLARIS_11_I86,"Solaris 10 (x86)");
		
		clientSupportedSOs.put(SO_MAC,"Mac OS");
		
		// ###############################################################################
		clientInstaller1SOs = new TreeMap<String, String>();
		clientInstaller1SOs.put(SO_SQUEEZE_32, "bacula-enterprise-client_squeeze_"+baculaUnixVersion+"_i386.deb");
		clientInstaller1SOs.put(SO_SQUEEZE_64, "bacula-enterprise-client_squeeze_"+baculaUnixVersion+"_amd64.deb");
		clientInstaller1SOs.put(SO_WHEEZE_32, "bacula-enterprise-client_wheezy_"+baculaUnixVersion+"_i386.deb");
		clientInstaller1SOs.put(SO_WHEEZE_64, "bacula-enterprise-client_wheezy_"+baculaUnixVersion+"_amd64.deb");
		clientInstaller1SOs.put(SO_PRECISE_32, "bacula-enterprise-client_precise_"+baculaUnixVersion+"_i386.deb");
		clientInstaller1SOs.put(SO_PRECISE_64, "bacula-enterprise-client_precise_"+baculaUnixVersion+"_amd64.deb");

		clientInstaller1SOs.put(SO_REDHAT5_32, "bacula-enterprise-client-"+baculaUnixVersion+".el5.i586.rpm");
		clientInstaller1SOs.put(SO_REDHAT5_64, "bacula-enterprise-client-"+baculaUnixVersion+".el5.x86_64.rpm");
		clientInstaller1SOs.put(SO_REDHAT6_32, "bacula-enterprise-client-"+baculaUnixVersion+".el6.i586.rpm");
		clientInstaller1SOs.put(SO_REDHAT6_64, "bacula-enterprise-client-"+baculaUnixVersion+".el6.x86_64.rpm");
		
		clientInstaller1SOs.put(SO_SUSE10_32, "bacula-enterprise-client-"+baculaUnixVersion+".su102.i586.rpm");
		clientInstaller1SOs.put(SO_SUSE10_64, "bacula-enterprise-client-"+baculaUnixVersion+".su103.x86_64.rpm");
		clientInstaller1SOs.put(SO_SUSE11_32, "bacula-enterprise-client-"+baculaUnixVersion+".su110.i586.rpm");
		clientInstaller1SOs.put(SO_SUSE11_64, "bacula-enterprise-client-"+baculaUnixVersion+".su110.x86_64.rpm");
		
		clientInstaller1SOs.put(SO_LINUX, "--");
		
		clientInstaller1SOs.put(SO_WIN_32, "bacula-enterprise-win32-"+baculaVersion+".tgz");
		clientInstaller1SOs.put(SO_WIN_64, "bacula-enterprise-win64-"+baculaVersion+".tgz");
		
		clientInstaller1SOs.put(SO_SOLARIS_8_SPARC,"bacula-enterprise-fd-"+baculaVersion+".sparc.sol8.pkg.tar.gz");
		clientInstaller1SOs.put(SO_SOLARIS_9_SPARC,"bacula-enterprise-fd-"+baculaVersion+".sparc.sol9.pkg.tar.gz");
		clientInstaller1SOs.put(SO_SOLARIS_10_I86,"bacula-enterprise-fd-"+baculaVersion+".i86.sol10.pkg.tar.gz");
		clientInstaller1SOs.put(SO_SOLARIS_10_SPARC,"bacula-enterprise-fd-"+baculaVersion+".sparc.sol10.pkg.tar.gz");
		clientInstaller1SOs.put(SO_SOLARIS_11_I86,"bacula-enterprise-fd-"+baculaVersion+".i86.sol11.pkg.tar.gz");
		
		clientInstaller1SOs.put(SO_MAC,"Bacula Enterprise File Daemon "+baculaVersion+".dmg");
		
		// ###############################################################################
		clientInstaller2SOs = new TreeMap<String, String>();

		clientInstaller2SOs.put(SO_SQUEEZE_32, "bacula-enterprise-common_squeeze_"+baculaUnixVersion+"_i386.deb");
		clientInstaller2SOs.put(SO_SQUEEZE_64, "bacula-enterprise-common_squeeze_"+baculaUnixVersion+"_amd64.deb");
		clientInstaller2SOs.put(SO_WHEEZE_32, "bacula-enterprise-common_wheezy_"+baculaUnixVersion+"_i386.deb");
		clientInstaller2SOs.put(SO_WHEEZE_64, "bacula-enterprise-common_wheezy_"+baculaUnixVersion+"_amd64.deb");
		clientInstaller2SOs.put(SO_PRECISE_32, "bacula-enterprise-common_precise_"+baculaUnixVersion+"_i386.deb");
		clientInstaller2SOs.put(SO_PRECISE_64, "bacula-enterprise-common_precise_"+baculaUnixVersion+"_amd64.deb");

		clientInstaller2SOs.put(SO_REDHAT5_32, "bacula-enterprise-libs-"+baculaUnixVersion+".el5.i586.rpm");
		clientInstaller2SOs.put(SO_REDHAT5_64, "bacula-enterprise-libs-"+baculaUnixVersion+".el5.x86_64.rpm");
		clientInstaller2SOs.put(SO_REDHAT6_32, "bacula-enterprise-libs-"+baculaUnixVersion+".el6.i586.rpm");
		clientInstaller2SOs.put(SO_REDHAT6_64, "bacula-enterprise-libs-"+baculaUnixVersion+".el6.x86_64.rpm");
		
		clientInstaller2SOs.put(SO_SUSE10_32, "bacula-enterprise-libs-"+baculaUnixVersion+".su102.i586.rpm");
		clientInstaller2SOs.put(SO_SUSE10_64, "bacula-enterprise-libs-"+baculaUnixVersion+".su103.x86_64.rpm");
		clientInstaller2SOs.put(SO_SUSE11_32, "bacula-enterprise-libs-"+baculaUnixVersion+".su110.i586.rpm");
		clientInstaller2SOs.put(SO_SUSE11_64, "bacula-enterprise-libs-"+baculaUnixVersion+".su110.x86_64.rpm");
		
		clientInstaller2SOs.put(SO_LINUX, "--");
		
		clientInstaller2SOs.put(SO_WIN_32, "bacula-enterprise-win32VssPlugin-"+baculaVersion+".tgz");
		clientInstaller2SOs.put(SO_WIN_64, "bacula-enterprise-win64VssPlugin-"+baculaVersion+".tgz");
		
		clientInstaller2SOs.put(SO_SOLARIS_8_SPARC,"bacula-enterprise-libs-"+baculaVersion+".sparc.sol8.pkg.tar.gz");
		clientInstaller2SOs.put(SO_SOLARIS_9_SPARC,"bacula-enterprise-libs-"+baculaVersion+".sparc.sol9.pkg.tar.gz");
		clientInstaller2SOs.put(SO_SOLARIS_10_I86,"bacula-enterprise-libs-"+baculaVersion+".i86.sol10.pkg.tar.gz");
		clientInstaller2SOs.put(SO_SOLARIS_10_SPARC,"bacula-enterprise-libs-"+baculaVersion+".sparc.sol10.pkg.tar.gz");
		clientInstaller2SOs.put(SO_SOLARIS_11_I86,"bacula-enterprise-libs-"+baculaVersion+".i86.sol11.pkg.tar.gz");
		
		clientInstaller2SOs.put(SO_MAC,"Bacula Enterprise File Daemon "+baculaVersion+".dmg");
	}

	public ClientManager(Configuration conf) throws Exception {
		this._c = conf;
		this._db = new DBConnectionManager(this._c).getConnection();
	}
	
	public void deleteClient(String name) throws Exception {
		if(JobManager.hasRunningJobs()) {
			throw new Exception("backup jobs are currently running");
		}
		
		BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","clients", name);
		try {
			this. _db.query("delete from client where client.name = '" + name + "'");
	    } catch (DBException _ex) {}
	    BackupOperator.reload();
	}
	
	/**
	 * Despliega automáticamente un agente en un cliente según su SO
	 * @param ip		[String]	IP del cliente
	 * @param username	[String]	Usuario para acceder al cliente
	 * @param password	[String]	Password para acceder al cliente
	 * @param os		[String]	SO del cliente
	 */
	public void deployClient(String ip, String name, String password, String deployUsername, String deployPassword, String os) throws Exception {
		String _pathAgent = null;
		String _pathAgent2 = null;
		
		if (clientInstaller1SOs.containsKey(os))
			_pathAgent = clientInstaller1SOs.get(os);
		if (clientInstaller2SOs.containsKey(os))
			_pathAgent2 = clientInstaller2SOs.get(os);
		
		this.cleanBaculaAgentInstallationFiles();
		
		/// Intentamos auto-desplegar los agentes
		if(os.contains("win")) {		// Comandos para sistemas windows
			logger.info("Desplegando agente en windows...");
			this.cleanPreviousBaculaAgentWinInstallation(ip, deployUsername, deployPassword);
			
			// Descomprimimos, creamos fichero configuracion y comprimimos de nuevo
			Command.systemCommand("tar xvf "+WBSAirbackConfiguration.getDirectoryBaculaAgents() + _pathAgent + " -C " + WBSAirbackConfiguration.getDirectoryBaculaAgents());
			this.setBaculaFdClient(name, password, "Bacula/", "C:\\\\Program Files\\\\Bacula\\\\", os);
			Command.systemCommand("tar cvf "+WBSAirbackConfiguration.getDirectoryBaculaAgents()+"tempAgent.tgz "+WBSAirbackConfiguration.getDirectoryBaculaAgents()+"Bacula");
			logger.info("Paquete con bacula fd construido correctamente ...");
			
			// Copiamos el fichero comprimido y damos de alta el servicio
			try {
				Command.systemCommand("cd "+WBSAirbackConfiguration.getDirectoryBaculaAgents()+" && smbclient //"+ip+"/c$ -U "+deployUsername+"%" + deployPassword + " -Tx tempAgent.tgz");
				logger.info("Descomprimido en destino correctamente");
				Command.systemCommand(WBSAirbackConfiguration.getDirWinexe() + "winexe -U " + deployUsername + "%" + deployPassword+" //" + ip + " 'cmd /C move /Y \"c:\\etc\\wbsairback-admin\\agents\\Bacula\" \"c:\\Bacula\"'");
				Command.systemCommand(WBSAirbackConfiguration.getDirWinexe() + "winexe -U " + deployUsername + "%" + deployPassword + " //" + ip + " 'cmd /C rd \"c:\\etc\" /s /q'");
				logger.info("Ficheros movidos en destino correctamente");
			} catch (Exception ex) { 
				throw new Exception("common.message.agentDeployError: " + ex.getMessage());
			}
			
			try {
				Command.systemCommand(WBSAirbackConfiguration.getDirWinexe()+"winexe -U "+deployUsername+"%"+deployPassword+" //"+ip+" 'cmd /C mkdir \"c:\\Program Files\\Bacula\"'");
			} catch (Exception ex) {
				logger.error("Error creando directorio ProgramFiles/Bacula. Ex: {}", ex.getMessage());
			}
			
			try {
				Command.systemCommand(WBSAirbackConfiguration.getDirWinexe()+"winexe -U "+deployUsername+"%"+deployPassword+" //"+ip+" 'cmd /C xcopy /E \"c:\\Bacula\" \"c:\\Program Files\\Bacula\"'");
				logger.info("Directorio Bacula correctamente creado y llenado en destino");
			} catch (Exception ex) {
				logger.error("Error copiando Program Files/Bacula a C:/Bacula. Ex:{}", ex.getMessage());
			}
			
			this.cleanBaculaAgentInstallationFiles();
			
			try {
				Command.systemCommand(WBSAirbackConfiguration.getDirWinexe()+"winexe -U "+deployUsername+"%"+deployPassword+" //"+ip+" 'sc create Bacula-fd start= auto binPath= \"C:\\Bacula\\bacula-fd.exe /service -c C:\\Bacula\\bacula-fd.conf\"'");
				logger.info("Servicio de bacula correctamente registrado");
				Command.systemCommand("net -U "+deployUsername+"%"+deployPassword+" -S "+ip+" rpc service start Bacula-fd");
				logger.info("Servicio de bacula correctamente iniciado");
			} catch (Exception ex) {
				throw new Exception("common.message.agentDeployError:"+ex.getMessage());
			}
			
		} else if (os.contains("debian")) {
			logger.info("Desplegando agente en sistema debian...");
			this.cleanPreviousBaculaAgentDebian(ip, deployUsername, deployPassword);
			scpCommand("/etc/wbsairback-admin/agents/"+_pathAgent, "/tmp", deployUsername, deployPassword, ip);
			scpCommand("/etc/wbsairback-admin/agents/"+_pathAgent2, "/tmp", deployUsername, deployPassword, ip);
			logger.info("Ficheros de agente {} y {} copiados en destino. Instalando ...", _pathAgent, _pathAgent2);
			
			// Primero el common y despues el normal
			sshCommand("sudo DEBIAN_FRONTEND=noninteractive dpkg -i /tmp/"+_pathAgent2, deployUsername, deployPassword, ip);
			sshCommand("sudo DEBIAN_FRONTEND=noninteractive dpkg -i /tmp/"+_pathAgent, deployUsername, deployPassword, ip);
			logger.info("Instalación finalizada. Configurando bacula-fd...");
			this.setBaculaFdClient(name, password, "", "/opt/bacula/", os);
			sshCommand("sudo /etc/init.d/bacula-fd stop", deployUsername, deployPassword, ip);
			scpCommand("/etc/wbsairback-admin/agents/bacula-fd.conf", "/tmp", deployUsername, deployPassword, ip);
			sshCommand("sudo mv /tmp/bacula-fd.conf /opt/bacula/etc/", deployUsername, deployPassword, ip);
			logger.info("Bacula-fd configurado y copiado en destino");
			this.cleanBaculaAgentInstallationFiles();
			try {
				sshCommand("sudo /etc/init.d/bacula-fd restart", deployUsername, deployPassword, ip);
			} catch (Exception ex) {
				throw new Exception("common.message.agentDeployError:"+ex.getMessage());
			}
			try {
				sshCommand("sudo rm /tmp/"+_pathAgent, deployUsername, deployPassword, ip);
				sshCommand("sudo rm /tmp/"+_pathAgent2, deployUsername, deployPassword, ip);
			} catch (Exception ex) {}
		} /*else if (os.contains("solaris")) {
			scpCommand("/etc/wbsairback-admin/agents/"+_pathAgent, "/etc/opt", deployUsername, deployPassword, ip);
			sshCommand("gtar /etc/opt/"+_pathAgent, deployUsername, deployPassword, ip);
			this.setBaculaFdClient(name, password, "", "/etc/opt/bacula/", os);
			scpCommand("/etc/wbsairback-admin/agents/bacula-fd.conf", "/etc/opt/bacula/", deployUsername, deployPassword, ip);
			sshCommand("echo \"svcadm enable svc:/application/backup/bacula-fd:default\" >> /etc/opt/bacula/bacula-fd.conf", deployUsername, password, ip);
			this.cleanBaculaAgentInstallationFiles();
		}*/
	}
	
	public static String getVMWareExcludeQuery(String tableJobsAlias) throws Exception {
		String vmwareExclude = "";
		List<String> vmwareFilesets = FileSetManager.getAllVmwareFileSetNames();
		vmwareExclude += " AND "+tableJobsAlias+".name != 'RestoreFilesVmware'";
		if (vmwareFilesets != null && vmwareFilesets.size()>0) {
			vmwareExclude += "AND "+tableJobsAlias+".filesetid not in (select f.filesetid from fileset f where f.fileset in (";
			boolean first = true;
			for (String fileset : vmwareFilesets) {
				if (!first)
					vmwareExclude+=",";
				else
					first = false;
				vmwareExclude+="'"+fileset+"'";
			}
			vmwareExclude += ")) ";
		}
		return vmwareExclude;
	}
	
	public static String getVMWareIncludeQuery(String client, String tableJobsAlias) throws Exception {
		String vmwareExclude = "";
		List<Map<String, String>> vmwareFilesets = FileSetManager.getVmwareFilesetsOfClient(client);
		vmwareExclude += " AND "+tableJobsAlias+".name != 'RestoreFiles'  AND "+tableJobsAlias+".name != 'SaveClient' AND "+tableJobsAlias+".type not in ('c','D') AND  (";
		if (vmwareFilesets != null && vmwareFilesets.size()>0) {
			vmwareExclude += tableJobsAlias+".filesetid in (select f.filesetid from fileset f where f.fileset in (";
			boolean first = true;
			for (Map<String, String> fileset : vmwareFilesets) {
				if (!first)
					vmwareExclude+=",";
				else
					first = false;
				vmwareExclude+="'"+fileset.get("name")+"'";
			}
			vmwareExclude += ")) OR ";
		}
		vmwareExclude += tableJobsAlias+".filesetid = 0)";
		return vmwareExclude;
	}
	
	public List<Map<String, String>> getAllClients(String match, List<String> categories, boolean completeInfo) throws Exception {
		List<Map<String, String>> clients = new ArrayList<Map<String,String>>();
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		String search = "";
		if (match != null && !match.isEmpty())
			search = "where cli.name ilike '%" + match + "%'";
		
		String vmwareExclude = getVMWareExcludeQuery("jo");
		
		String _q = new String ("select cli.name, cli.clientid, cli.uname, cli.starttime, cli.jobstatuslong from (select c.name, c.clientid, c.uname, j.starttime, s.jobstatuslong " + 
				 "from client as c LEFT OUTER JOIN job as j ON c.clientid = j.clientid " + 
				 "AND j.jobid = (select jo.jobid from job as jo, client as cl where jo.clientid = cl.clientid and c.clientid = cl.clientid "+vmwareExclude+" ORDER BY jo.starttime DESC limit 1) " +
				 " LEFT OUTER JOIN status as s ON j.jobstatus = s.jobstatus ORDER BY c.name) as cli "+search);
		 
		List<Map<String, Object>> result = this._db.query(_q);
		
		if(result == null || result.isEmpty()) {
			return clients;
		}
		
		for(Map<String, Object> row : result) {
				Map<String, String> client = buildClientFromBDResult(row, categories, dateFormat);
				if (client != null) {
					if (completeInfo)
						client.putAll(getClient(client.get("name")));
					if (!client.isEmpty())
						clients.add(client);
				}
			}
		
		return clients;
	}
	
	
	/**
	 * Obtiene todos los datos de un cliente a partir de un id
	 * @param clientId
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getCompleteClient(Integer clientId) throws Exception {
		Map<String, String> client = new HashMap<String, String>();
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		String _q = new String ("select cli.name, cli.clientid, cli.uname, cli.starttime, cli.jobstatuslong from (select c.name, c.clientid, c.uname, j.starttime, s.jobstatuslong " + 
				 "from client as c LEFT OUTER JOIN job as j ON c.clientid = j.clientid " + 
				 "AND j.jobid = (select jo.jobid from job as jo, client as cl where jo.clientid = cl.clientid and c.clientid = cl.clientid "+getVMWareExcludeQuery("jo")+" ORDER BY jo.starttime DESC limit 1) " +
				 " LEFT OUTER JOIN status as s ON j.jobstatus = s.jobstatus) as cli where cli.clientid = " + clientId);

		List<Map<String, Object>> result = this._db.query(_q);
		if(result == null || result.isEmpty()) {
			return client;
		}
		
		client = buildClientFromBDResult(result.get(0), null, dateFormat);
		client.putAll(getClient(client.get("name")));
		return client;
	}
	
	public Map<String, String> getBDClientVmware(String clientName) throws Exception {
		Map<String, String> client = new HashMap<String, String>();
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		String vmwareInclude = getVMWareIncludeQuery(clientName, "jo");
		
		if (vmwareInclude != null && !vmwareInclude.isEmpty()) {
			String _q = new String ("select cli.name, cli.clientid, cli.uname, cli.starttime, cli.jobstatuslong from (select c.name, c.clientid, c.uname, j.starttime, s.jobstatuslong " + 
					 "from client as c LEFT OUTER JOIN job as j ON c.clientid = j.clientid " + 
					 "AND j.jobid = (select jo.jobid from job as jo, client as cl where jo.clientid = cl.clientid and c.clientid = cl.clientid "+vmwareInclude+" ORDER BY jo.starttime DESC limit 1) " +
					 " LEFT OUTER JOIN status as s ON j.jobstatus = s.jobstatus) as cli where cli.name = 'airback-fd'");
			List<Map<String, Object>> result = this._db.query(_q);
			if(result == null || result.isEmpty()) {
				return client;
			}
			
			client = buildClientFromBDResult(result.get(0), null, dateFormat);
		}
		return client;
	}
	
	
	public Map<String, String> buildClientFromBDResult(Map<String, Object> row, List<String> categories, java.text.SimpleDateFormat dateFormat) {
		Map<String, String> client = new HashMap<String, String>();
		File _f = new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + row.get("name") + ".xml");
		if(_f.exists()) {
			try {
				Configuration _c = new Configuration(_f);
				boolean catFound = false;
				List<String> listCat = new ArrayList<String>();
				if (categories != null && !categories.isEmpty()) {
					if (_c.hasProperty("client.category") && !_c.getProperty("client.category").isEmpty()) {
						listCat = Arrays.asList(_c.getProperty("client.category").split(","));
						for (String cat : listCat) {
							if (categories.contains(cat))
								catFound = true;
						}
					}
				}
				if(categories == null || categories.equals("") || catFound){
					client.put("id", String.valueOf(row.get("clientid")));
					client.put("name", String.valueOf(row.get("name")));
					if("airback-fd".equals(String.valueOf(row.get("name")))) {
						client.put("uname", "WBSAirback local client");
					} else {
						client.put("uname", String.valueOf(row.get("uname")));
					}
					if(row.get("starttime") != null) {
						client.put("starttime", dateFormat.format(row.get("starttime")));
					} else {
						client.put("starttime", "");
					}
					if(row.get("jobstatuslong") != null) {
						client.put("status", String.valueOf(row.get("jobstatuslong")));
					} else {
						client.put("status", "");
					}
					if(client.get("status") != null && !client.get("status").isEmpty()) {
						client.put("alert", getAlertFromStatus(client.get("status")));
					} else {
						client.put("alert", "");
					}
					if(_c.getProperty("client.port") != null) {
						client.put("port", _c.getProperty("client.port"));
					} else {
						client.put("port", "9102");
					}
					if("airback-fd".equals(String.valueOf(row.get("name")))) {
						client.put("os", "wbsairback");
					} else if(_c.getProperty("client.os") != null) {
						client.put("os", _c.getProperty("client.os"));
					} else {
						client.put("os", "unknown");
					}

				} else {
					return null;
				}
			} catch(Exception _ex) {}
		}
		return client;
	}
	
	
	public List<String> getAllClientNames(String category) throws Exception {
		List<String> clients = new ArrayList<String>();
		File _dir = new File(WBSAirbackConfiguration.getDirectoryClients());
		String[] _files = _dir.list();
		if(_files != null) {
			for(String clientFile : _files) {
				if(clientFile.endsWith(".xml")) {
					if(category != null) {
						try {
							Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + clientFile));
							List<String> listCat = new ArrayList<String>();
							if (category != null && !category.isEmpty()) {
								if (_c.hasProperty("client.category") && !_c.getProperty("client.category").isEmpty()) {
									listCat = Arrays.asList(_c.getProperty("client.category").split(","));	
								}
							}
							if(category == null || category.equals("") || listCat.contains(category)){
								clients.add(clientFile.substring(0, clientFile.length() - 4));
							}
						} catch(Exception _ex) {}
					} else {
						clients.add(clientFile.substring(0, clientFile.length() - 4));
					}
				}
			}
		}
		
		Collections.sort(clients, String.CASE_INSENSITIVE_ORDER);
		return clients;
	}
	
	public static boolean existsClient(String name) {
		File _f = new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".xml");
		if(_f.exists()) {
			return true;
		} 
		return false;
	}
	
	public List<String> getClientCategories(String name) throws Exception {
		Map<String, String> client = getClient(name);
		List<String> categories = new ArrayList<String>();
		if (client.containsKey("category") && !client.get("category").isEmpty()) {
			categories = Arrays.asList(client.get("category").split(","));
		}
		return categories;
	}
	
	public boolean isClientOnCategories(String name, List<String> userCategories) throws Exception {
		List<String> categories = getClientCategories(name);
		if (categories != null && !categories.isEmpty()) {
			for (String ucat : userCategories) {
				if (categories.contains(ucat))
					return true;
			}
		}
		return false;
	}
	
	public Map<String, String> getClient(String name) throws Exception {
		Map<String, String> data = new HashMap<String, String>();
		data.put("name", name);
		boolean vmware = false;
		try {
			Configuration _c = new Configuration(new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".xml"));
			if(_c.getProperty("client.os") != null) {
				data.put("os", _c.getProperty("client.os"));
			} else {
				data.put("os", "unknown");
			}
			
			if (!vmware) {
				if(_c.getProperty("client.port") != null) {
					data.put("port", _c.getProperty("client.port"));
				} else {
					data.put("port", "9102");
				}
				if(_c.getProperty("client.charset") != null) {
					data.put("charset", _c.getProperty("client.charset"));
				} else {
					data.put("charset", "unknown");
				}
				if(_c.getProperty("client.category") != null) {
					data.put("category", _c.getProperty("client.category"));
				} else {
					data.put("category", "");
				}
			}
		} catch(Exception _ex) {}
		
		try {
			data.put("address", BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "Address"));
		} catch(Exception _ex) {
			data.put("address", "");
		}
		try {
			data.put("fdport", BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "FDPort"));
		} catch(Exception _ex) {
			data.put("fdport", "");
		}
		try {
			data.put("catalog", BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "Catalog"));
		} catch(Exception _ex) {
			data.put("catalog", "");
		}
		try {
			data.put("password", BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "Password"));
		} catch(Exception _ex) {
			data.put("password", "");
		}
		data.put("fileretention", "");
		data.put("fileretention-period", "");
		try {
			if(!BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "File Retention").isEmpty()) {
				StringTokenizer _st = new StringTokenizer(BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "File Retention"), " ");
				if(_st.hasMoreTokens()) {
					data.put("fileretention", _st.nextToken());
				}
				if(_st.hasMoreTokens()) {
					data.put("fileretention-period", _st.nextToken());
				}
			}
		} catch(Exception _ex) {}
		data.put("jobretention", "");
		data.put("jobretention-period", "");
		try {
			if(!BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "Job Retention").isEmpty()) {
				StringTokenizer _st = new StringTokenizer(BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "Job Retention"), " ");
				if(_st.hasMoreTokens()) {
					data.put("jobretention", _st.nextToken());
				}
				if(_st.hasMoreTokens()) {
					data.put("jobretention-period", _st.nextToken());
				}
			}
		} catch(Exception _ex) {}
		try {
			data.put("autoprune", BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client",name, "AutoPrune"));
		} catch(Exception _ex) {
			data.put("autoprune", "");
		}
		try {
			data.put("maximumconcurrentcobs", BaculaConfiguration.getBaculaParameter(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf", "Client", name, "Maximum Concurrent Jobs"));
		} catch(Exception _ex) {
			data.put("maximumconcurrentcobs", "");
		}
		return data;
	}
	
	public int getClientId(String name) throws Exception {
		String queryString = "select c.clientid as id " + 
							 "from client as c " + 
							 "where c.name like '" + name + "'";
		try {
			List<Map<String, Object>> result = this._db.query(queryString);
			if(result == null || result.isEmpty()) {
				throw new Exception("client [" + name + "] does not exists");
			}
			
			Map<String, Object> row = result.get(0);
			return (Integer) row.get("id");
		} catch(DBException _ex) {
			throw new Exception("database query error: " + _ex.getMessage());
		}
	}
	
	public String getClientName(int clientId) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("SELECT c.name AS name ");
		_sb.append("FROM client AS C WHERE ");
		_sb.append("c.clientid = ");
		_sb.append(clientId);
		try {
			List<Map<String, Object>> result = this. _db.query(_sb.toString());
			if(result == null || result.isEmpty()) {
				throw new Exception("client ID [" + clientId + "] does not exists in the database");
			}
			
			Map<String, Object> row = result.get(0);
			return String.valueOf(row.get("name"));
		} catch(DBException _ex) {
			throw new Exception("database query error: " + _ex.getMessage());
		}
	}
	
	public boolean isLocalClient(int clientId) throws Exception {
		try {
			String _name = getClientName(clientId);
			if(_name != null && "airback-fd".equals(_name)) {
				return true;
			}
		} catch(Exception _ex) {}
		return false;
	}
	
	public List<Map<String, String>> searchClients(String match, List<String> categories, Integer page, Integer rp, String sortname, String sortorder, String query, String qtype) throws Exception {
		List<Map<String, String>> clients = new ArrayList<Map<String,String>>();
		List<Map<String, Object>> result = null;
		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
    	
		if(!existsClient("airback-fd")) {
			setLocalClient(1, "months", 3, "months");
		}
		
		String order = "order BY c.name DESC";
		if (sortname != null && sortorder != null) {
			if (sortname.equals("starttime"))
				order = "ORDER BY j."+sortname+" "+sortorder;
			else if (sortname.equals("status"))
				order = "ORDER BY s.jobstatus "+sortorder;
			else
				order = "ORDER BY c."+sortname+" "+sortorder;
		}
		
	/*	int offset = (page * rp) - rp;
		String limit = "LIMIT " + rp;
		if(offset > 0) {
			limit += " OFFSET " + offset;
		}*/
		
		String whereQuery = "";
		if(query != null && !query.equals(""))	{
			whereQuery = " AND j."+qtype+" LIKE '%"+query+"%' ";
		}
		
		String vmwareExclude = getVMWareExcludeQuery("jo");
		
		if(match != null && !match.isEmpty()) {
			String _q = new String ("select cli.name, cli.clientid, cli.uname, cli.starttime, cli.jobstatuslong from (select c.name, c.clientid, c.uname, j.starttime, s.jobstatuslong " + 
					 "from client as c LEFT OUTER JOIN job as j ON c.clientid = j.clientid " + 
					 "AND j.jobid = (select jo.jobid from job as jo, client as cl where jo.clientid = cl.clientid and c.clientid = cl.clientid "+vmwareExclude+" ORDER BY jo.starttime DESC limit 1) " +
					 "LEFT OUTER JOIN status as s ON j.jobstatus = s.jobstatus " +whereQuery+" "+order+") as cli where cli.name ilike '%" + match + "%'");
		    result = this._db.query(_q);
		} else {
			String _q = new String ("select c.name, c.clientid, c.uname, j.starttime, s.jobstatuslong " + 
					 "from client as c LEFT OUTER JOIN job as j ON c.clientid = j.clientid " + 
					 "AND j.jobid = (select jo.jobid from job as jo, client as cl where jo.clientid = cl.clientid and c.clientid = cl.clientid "+vmwareExclude+" ORDER BY jo.starttime DESC limit 1) " +
					 " LEFT OUTER JOIN status as s ON j.jobstatus = s.jobstatus " +whereQuery+" "+order);
			result = this._db.query(_q);
		}
		
		if(result == null || result.isEmpty()) {
			return clients;
		}
		
		if(categories != null && !categories.isEmpty()) {
			for(Map<String, Object> row : result) {
				File _f = new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + row.get("name") + ".xml");
				if(_f.exists()) {
					try {
						Configuration _c = new Configuration(_f);
						List<String> listcat = new ArrayList<String>();
						boolean catFound = false;
						if (_c.hasProperty("client.category")) {
							if (!_c.getProperty("client.category").isEmpty())
								listcat = Arrays.asList(_c.getProperty("client.category").split(","));
							for (String cat : listcat) {
								if (categories.contains(cat))
									catFound = true;
							}
						}
						if(catFound){
							Map<String, String> client = new HashMap<String, String>();
							client.put("id", String.valueOf(row.get("clientid")));
							client.put("name", String.valueOf(row.get("name")));
							if("airback-fd".equals(String.valueOf(row.get("name")))) {
								client.put("uname", "WBSAirback local client");
							} else {
								client.put("uname", String.valueOf(row.get("uname")));
							}
							if(row.get("starttime") != null) {
								client.put("starttime", dateFormat.format(row.get("starttime")));
							} else {
								client.put("starttime", "");
							}
							if(row.get("starttime") != null) {
								client.put("status", String.valueOf(row.get("jobstatuslong")));
							} else {
								client.put("status", "");
							}
							if(client.get("status") != null && !client.get("status").isEmpty()) {
								client.put("alert", getAlertFromStatus(client.get("status")));
							} else {
								client.put("alert", "");
							}
							if("airback-fd".equals(String.valueOf(row.get("name")))) {
								client.put("os", "wbsairback");
							} else if(_c.getProperty("client.os") != null) {
								client.put("os", _c.getProperty("client.os"));
							} else {
								client.put("os", "unknown");
							}
							clients.add(client);
						}
					} catch(Exception _ex) {
						System.out.println("ClientManager::searchClients: " + _ex.getMessage());
					}
				} else {
					System.out.println("No existe");
				}
			}
		} else {
			for(Map<String, Object> row : result) {
				Map<String, String> client = new HashMap<String, String>();
				client.put("id", String.valueOf(row.get("clientid")));
				client.put("name", String.valueOf(row.get("name")));
				if("airback-fd".equals(String.valueOf(row.get("name")))) {
					client.put("uname", "WBSAirback local client");
				} else {
					client.put("uname", String.valueOf(row.get("uname")));
				}
				if(row.get("starttime") != null) {
					client.put("starttime", dateFormat.format(row.get("starttime")));
				} else {
					client.put("starttime", "");
				}
				if(row.get("starttime") != null) {
					client.put("status", String.valueOf(row.get("jobstatuslong")));
				} else {
					client.put("status", "");
				}
				if(client.get("status") != null && !client.get("status").isEmpty()) {
					client.put("alert", getAlertFromStatus(client.get("status")));
				} else {
					client.put("alert", "");
				}
				if("airback-fd".equals(String.valueOf(row.get("name")))) {
					client.put("os", "wbsairback");
				} else { 
					File _f = new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + client.get("name") + ".xml");
					if(_f.exists()) {
						Configuration _c = new Configuration(_f);
						if(_c.getProperty("client.os") != null) {
							client.put("os", _c.getProperty("client.os"));
						} else {
							client.put("os", "unknown");
						}
					}
				}
				clients.add(client);
			}
		}
		
		if (page != null && page > 0 && rp != null && rp > 0) {
			// Paginamos
			int first = page*rp -rp;
			int last = first+rp;
			
			if (first > clients.size())
				return clients;
			
			if (last > clients.size())
				last = clients.size();
				
			return clients.subList(first, last);
		}
		
		
		return clients;
	}
	
	
	public void setClient(String name, String address, int port, String os, String password, String charset, Integer fileRetention, String fileRetentionUnits, Integer jobRetention, String jobRetentionUnits, String [] categories) throws Exception {
		boolean newClient = false;
		if(name == null || !name.matches("[0-9a-zA-Z-._]+")) {
			throw new Exception("invalid client name");
		}
		
		List<String> _valid_units = new ArrayList<String>(Arrays.asList(new String[] { "days", "weeks", "months", "years" }));
		if(!_valid_units.contains(fileRetentionUnits)) {
			throw new Exception("invalid file retention unit");
		}
		if(!_valid_units.contains(jobRetentionUnits)) {
			throw new Exception("invalid job retention unit");
		}
		if("airback-fd".equalsIgnoreCase(name)) {
			if(address != null && !address.equalsIgnoreCase("localhost")) {
				/*boolean _internal_address = false;
				String[] _address = NetworkManager.toAddress(address);
				NetworkManager _nm = new NetworkManager(this._c);
				for(String _interface : _nm.getSystemInterfaces()) {
					if(NetworkManager.compareAddress(_address, _nm.getAddress(_interface))) {
						_internal_address = true;
					}
				}
				if(!_internal_address) {*/
					throw new Exception("airback-fd is a restricted backup client name");
				/*}*/
			}
		}
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".xml");
		if(!_f.exists()) {
			newClient = true;
			BaculaConfiguration.addBaculaIncludeResource("/etc/bacula/bacula-dir.conf", "clients", name);
		}
		
		try {
			Configuration _c = new Configuration(_f);
			_c.setProperty("client.os", os);
			_c.setProperty("client.charset", charset);
			_c.setProperty("client.port", String.valueOf(port));
			String cadCategories = "";
			if (categories != null) {
				for (String cat : categories) {
					if (cadCategories.isEmpty()) {
						cadCategories = cat;
					} else {
						cadCategories += ","+cat;
					}
				}
			}
			
			if(!cadCategories.isEmpty()) {
				_c.setProperty("client.category", cadCategories);
			} else {
				_c.removeProperty("client.category");
			}
			
			_f = new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf");
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "Address", new String[]{ address });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "FDPort", new String[]{ String.valueOf(port) });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "Catalog", new String[]{ this._c.getProperty("bacula.defaultCatalog") });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "Password", new String[]{ "\"" + password + "\"" });
			if (fileRetention != null && fileRetentionUnits != null && !fileRetentionUnits.isEmpty())
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "File Retention", new String[]{ fileRetention + " " + fileRetentionUnits });
			else
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Client",name, "File Retention");
			if (jobRetention != null && jobRetentionUnits != null && !jobRetentionUnits.isEmpty())
				BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "Job Retention", new String[]{ jobRetention + " " + jobRetentionUnits });
			else
				BaculaConfiguration.deleteBaculaParameter(_f.getAbsolutePath(), "Client",name, "Job Retention");
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "AutoPrune", new String[]{ this._c.getProperty("bacula.clientAutoPrune") });
			BaculaConfiguration.setBaculaParameter(_f.getAbsolutePath(), "Client",name, "Maximum Concurrent Jobs", new String[]{ this._c.getProperty("bacula.maxClientJobs") });
			
			_c.store();
		} catch(Exception _ex) {
			if(newClient) {
				BaculaConfiguration.deleteBaculaIncludeResource("/etc/bacula/bacula-dir.conf","clients", name);
				new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".conf").delete();
				new File(WBSAirbackConfiguration.getDirectoryClients() + "/" + name + ".xml").delete();
			}
			throw _ex;
		}
		
		if(!"airback-fd".equals(name) || ServiceManager.isRunning(ServiceManager.BACULA_DIR)) {
			BackupOperator.reload();
			if(newClient) {
				Command.systemCommand("/bin/echo \"run job=SaveClient yes client=" + name + "\" | /usr/bin/bconsole");
			}
		}
	}
	
	public void setLocalClient(Integer fileRetention, String fileRetentionUnits, Integer jobRetention, String jobRetentionUnits) throws Exception {
		String _fd_password = getRandomChars(20);
		StringBuilder _sb = new  StringBuilder();
		_sb.append("#\n");
		_sb.append("#  WBSAirback Central File Daemon\n");
		_sb.append("#\n");
		_sb.append("\n");
		_sb.append("Director {\n");
		_sb.append("  Name = airback-dir\n");
		_sb.append("  Password = \""+ _fd_password + "\"\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("FileDaemon {\n");
		_sb.append("  Name = airback-fd\n");
		_sb.append("  FDport = 9102\n");
		_sb.append("  WorkingDirectory = /rdata/working\n");
		_sb.append("  Pid Directory = /var/run\n");
		_sb.append("  Maximum Concurrent Jobs = 20\n");
		_sb.append("  Plugin Directory = \"/opt/bacula/plugins\"\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("Messages {\n");
		_sb.append("  Name = Standard\n");
		_sb.append("  director = airback-dir = all, !skipped, !restored\n");
		_sb.append("}\n");
		_sb.append("\n");
		
		File _f = new File("/etc/bacula/bacula-fd.conf");
		FileSystem.writeFile(_f, _sb.toString());
		setClient("airback-fd", "localhost", 9102, "WBSAirback", _fd_password, "UTF-8", fileRetention, fileRetentionUnits, jobRetention, jobRetentionUnits, null);
	}
	
	/**
	 * Desinstala los paquetes y elimina los ficheros asociados a un cliente Debian
	 * @param ip
	 * @param deployUsername
	 * @param deployPassword
	 * @throws Exception
	 */
	private void cleanPreviousBaculaAgentWinInstallation(String ip, String deployUsername, String deployPassword) throws Exception {
		try {
			Command.systemCommand("net -U " + deployUsername + "%" + deployPassword + " -S " + ip + " rpc service stop Bacula-fd");
		} catch (Exception ex) {}
		try {
			Command.systemCommand("net -U " + deployUsername + "%" + deployPassword + " -S " + ip + " rpc service start winexesvc");
		} catch (Exception ex) {}
		try {
			Command.systemCommand(WBSAirbackConfiguration.getDirWinexe() + "winexe -U " + deployUsername + "%" + deployPassword + " //" + ip + " 'sc delete Bacula-fd'"); 
		} catch (Exception ex) {}
		try {
			Command.systemCommand(WBSAirbackConfiguration.getDirWinexe() + "winexe -U " + deployUsername + "%" + deployPassword + " //" + ip + " 'cmd /C rd \"c:\\Bacula\" /s /q'");
		} catch (Exception ex) {}
		try {
			Command.systemCommand(WBSAirbackConfiguration.getDirWinexe() + "winexe -U " + deployUsername + "%" + deployPassword + " //" + ip + " 'cmd /C rd \"c:\\Program Files\\Bacula\" /s /q'");
		} catch (Exception ex) {}
		try {
			Command.systemCommand(WBSAirbackConfiguration.getDirWinexe() + "winexe -U " + deployUsername + "%" + deployPassword + " //" + ip + " 'cmd /C rd \"c:\\etc\" /s /q'");
		} catch (Exception ex) {}
	}
	
	/**
	 * Elimina los ficheros temporales generados en Airback para el auto-despliegue de un agente
	 * @throws Exception
	 */
	private void cleanBaculaAgentInstallationFiles() throws Exception {
		try {
			Command.systemCommand("rm  -r " + WBSAirbackConfiguration.getDirectoryBaculaAgents() + "Bacula");
		} catch (Exception ex) {}
		try {
			Command.systemCommand("rm " + WBSAirbackConfiguration.getDirectoryBaculaAgents() + "tempAgent.tgz");
		} catch (Exception ex) {}
		try {
			Command.systemCommand("rm " + WBSAirbackConfiguration.getDirectoryBaculaAgents() + "bacula-fd.conf");
		} catch (Exception ex) {}
	}
	
	/**
	 * Desinstala los paquetes y elimina los ficheros asociados a un cliente Debian
	 * @param ip
	 * @param deployUsername
	 * @param deployPassword
	 * @throws Exception
	 */
	private void cleanPreviousBaculaAgentDebian(String ip, String deployUsername, String deployPassword) throws Exception
	{
		try {
			sshCommand("/etc/init.d/bacula-fd stop", deployUsername, deployPassword, ip);
		} catch (Exception ex) {}
		try {
			sshCommand("rm -r /opt/bacula", deployUsername, deployPassword, ip);
		} catch (Exception ex) {}
	}
	
	/**
	 * Crea el fichero de configuración de bácula destinado a un cliente windows
	 * @param namesc 
	 * @param password
	 * @throws Exception
	 */
	public void setBaculaFdClient(String name, String password, String pathDestiny, String pathInstall, String os) throws Exception {
		StringBuilder _sb = new  StringBuilder();
		_sb.append("# Default  Bacula File Daemon Configuration file\n");
		_sb.append("#\n\n");
		_sb.append("#\n");
		_sb.append("# \"Global\" File daemon configuration specifications\n");
		_sb.append("#\n");
		_sb.append("FileDaemon {  \n");
		_sb.append("Name = "+name+"\n");
		_sb.append("FDport = 9102             # where we listen for the director\n");
		if (os.contains("win")) {
			_sb.append("WorkingDirectory = \""+pathInstall+"working\"\n");
			_sb.append("Pid Directory = \""+pathInstall+"working\"\n");
			_sb.append("Plugin Directory = \""+pathInstall+"plugins\"\n");
		} else {
			_sb.append("WorkingDirectory = "+pathInstall+"working\n");
			_sb.append("Pid Directory = "+pathInstall+"working\n");
			_sb.append("Plugin Directory = "+pathInstall+"plugins\n");
		}
		_sb.append("Maximum Concurrent Jobs = 10\n");
		_sb.append("}\n");
		_sb.append("\n");
		_sb.append("#\n");
		_sb.append("# List Directors who are permitted to contact this File daemon\n");
		_sb.append("#\n");
		_sb.append("Director {\n");
		_sb.append("Name = airback-dir\n");
		_sb.append("Password = \""+password+"\"        # Director must know this password\n");
		_sb.append("}\n\n");
		_sb.append("#\n");
		_sb.append("# Send all messages except skipped files back to Director\n");
		_sb.append("Messages {\n");
		_sb.append("Name = Standard\n");
		_sb.append("director = airback-dir = all, !skipped, !restored\n");
		_sb.append("}\n");
		
		File _f = new File(WBSAirbackConfiguration.getDirectoryBaculaAgents()+pathDestiny+"bacula-fd.conf");
		FileSystem.writeFile(_f, _sb.toString());
	}
	
	/**
	 * Ejecuta un comando remoto mediante ssh
	 * @param command
	 * @param user
	 * @param password
	 * @param host
	 * @return
	 * @throws Exception
	 */
	private String sshCommand(String command, String user, String password, String host) throws Exception {
	        StringBuilder _command = new StringBuilder();
	        StringBuilder _ssh_command = new StringBuilder();
	        _ssh_command.append("ssh -x -o StrictHostKeyChecking=no -o CheckHostIP=no");
	        _ssh_command.append(" ");
	        _ssh_command.append(user);
	        _ssh_command.append("@");
	        _ssh_command.append(host);
	        _ssh_command.append(" ");
	        _ssh_command.append(command);
            _command.append("expect -c 'set timeout 120 ; spawn ");
            _command.append(_ssh_command.toString());
            _command.append(" ; expect \"*?assword:*\" {send \"");
            _command.append(password);
            _command.append("\\r\"} ; interact'");
	        return Command.systemCommand(_command.toString());
	}
	
	/**
	 * Ejecuta un comando de transferencia de ficheros remota mediante scp
	 * @param command
	 * @param user
	 * @param password
	 * @param host
	 * @return
	 * @throws Exception
	 */
	private String scpCommand(String pathOrigin, String pathDestiny, String user, String password, String host) throws Exception {
        StringBuilder _command = new StringBuilder();
        StringBuilder _ssh_command = new StringBuilder();
        _ssh_command.append("scp ");
        _ssh_command.append(" ");
        _ssh_command.append(pathOrigin);
        _ssh_command.append(" ");
        _ssh_command.append(user);
        _ssh_command.append("@");
        _ssh_command.append(host);
        _ssh_command.append(":"+pathDestiny);
        _command.append("expect -c 'set timeout 120 ; spawn ");
        _command.append(_ssh_command.toString());
        _command.append(" ; expect \"*?assword:*\" {send \"");
        _command.append(password);
        _command.append("\\r\"} ; interact'");
        return Command.systemCommand(_command.toString());
	}
	
	/**
	 * Comprueba si un cliente está online
	 * @return
	 */
	public Boolean isOnlineClient(String client) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("/bin/echo \"status client=");
		_sb.append(client);
		_sb.append("\" | /opt/bacula/bin/bconsole");
		try {
			String _output = Command.systemCommand(_sb.toString());
			if(_output != null && !_output.contains("Failed to connect"))
				return true;
			else
				return false;
		} catch (Exception ex) {
			return false;
		}
	}

	public static String getRandomChars(int number) {
		char[] _characters = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		StringBuilder _chars = new StringBuilder();
		Random _r = new Random();
		for(int i = number; --i >= 0; ) {
			_chars.append(_characters[(int) _r.nextInt(36)]);
		}
		return _chars.toString();
	}
	
	private static String getAlertFromStatus(String message) {
		if(String.valueOf(message).equals("Running") ||
				String.valueOf(message).equals("Created, not yet running") ||
				String.valueOf(message).equals("Completed successfully")) {
			return "good";
		} else if(String.valueOf(message).equals("Incomplete Job")) {
			return "warning";
		} else {
			return "error";
		}
	}
	
	public static Map<String, String> getClientSupportedSOs() {
		return clientSupportedSOs;
	}
}
