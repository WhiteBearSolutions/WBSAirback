
package com.whitebearsolutions.imagine.wbsairback.bacula;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.ServiceManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.BaculaConfiguration;
import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.disk.VolumeManager;
import com.whitebearsolutions.imagine.wbsairback.util.ObjectLock;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

public class BackupOperator {
	private Configuration _c;
	private ClientManager _cm;
	private static boolean block_reload = false;
	
	private final static Logger logger = LoggerFactory.getLogger(BackupOperator.class);
	
	public BackupOperator(Configuration conf) throws Exception {
		this._c = conf;
		this._cm = new ClientManager(this._c);
	}
	
	public void cancelJob(int jobId) throws Exception {
		String command = "echo \"cancel jobid=" + jobId + "\" | /usr/bin/bconsole";
		if (!Command.isRunning(command)) {
			Command.systemCommand(command);
			ObjectLock.block(ObjectLock.JOBS_TYPE_OBJECT, String.valueOf(jobId), "cancel");
		}
	}
	
	public void deleteJob(int jobId) throws Exception {
		Command.systemCommand("echo \"delete job jobid=" + jobId + "\" | /usr/bin/bconsole");
		ObjectLock.unblockAll(ObjectLock.JOBS_TYPE_OBJECT,  String.valueOf(jobId));
	}
	
	public void pruneJobs(int clientId) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("echo \"prune jobs yes client=");
		_sb.append(this._cm.getClientName(clientId));
		_sb.append("\" | /usr/bin/bconsole");
		Command.systemCommand(_sb.toString());
	}
	
	public static void reload() throws Exception {
		if (!block_reload) {
			try {
				ServiceManager.start(ServiceManager.BACULA_DIR);
				ServiceManager.start(ServiceManager.BACULA_FD);
				ServiceManager.start(ServiceManager.BACULA_SD);
				Command.systemCommand("/bin/echo \"reload\" | /usr/bin/bconsole");
				//logger.debug("bconsole reloaded");
			} catch (Exception ex) {
				throw new Exception("Error reinitializing Bacula Services");
			}
		}
	}
	
	public void restartIncompleteJob(int jobId) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("echo \"restart incomplete jobid=");
		_sb.append(jobId);
		_sb.append("\" | /usr/bin/bconsole");
		Command.systemCommand(_sb.toString());
		ObjectLock.unblockAll(ObjectLock.JOBS_TYPE_OBJECT,  String.valueOf(jobId));
		Thread.sleep(1000);
	}
	
	public void restoreFile(int clientId, int jobId, String file, int destinationClientId, String lv, String lvshare, String destinationPath, String postscript, String prescript) throws Exception {
		try {
			String client = this._cm.getClientName(clientId);
			String destinationClient = this._cm.getClientName(destinationClientId);
			destinationPath = getRestoreDestinationPath(file, destinationPath, client, destinationClientId, destinationClient, lv, lvshare);
			restoreFile(client, String.valueOf(jobId), file, destinationClient, destinationPath, postscript, prescript);
		} catch (Exception ex) {
			String msg = "Error restoring single file [client:"+clientId+" jobId:"+jobId+" file:"+file+" destinationClientId:"+destinationClientId+" lv:"+lv+" lvshare:"+lvshare+" destinationPath:"+destinationPath+". Ex: "+ex.getMessage();
			logger.error(msg);
			throw new Exception(msg);
		}
	}
	
	public void restoreFiles(int clientId, List<String> files, int destinationClientId, String lv, String lvshare, String destinationPath, Calendar date, String postscript, String prescript) throws Exception {
		try {
			//Map<String, List<String>> _filesets = new HashMap<String, List<String>>();
			if(files == null || files.isEmpty()) {
				return;
			}
			String fileCheck = files.iterator().next();
			String client = this._cm.getClientName(clientId);
			String destinationClient = this._cm.getClientName(destinationClientId);
			destinationPath = getRestoreDestinationPath(fileCheck, destinationPath, client, destinationClientId, destinationClient, lv, lvshare);
			
			FileManager fm = new FileManager(this._c);
			Map<String, List<String>> filesetsOfFiles = new HashMap<String, List<String>>();
			
			for (String file : files) {
				logger.debug("Buscamos fileset mas reciente para cliente:{} file:{} fecha:{}", new Object[]{clientId, file, date});
				String fileset = fm.getMostRecentFileset(clientId,file , date);
				logger.debug("Obtenemos fileset: {}", fileset);
				if (fileset != null) {
					if (filesetsOfFiles.containsKey(fileset)) {
						filesetsOfFiles.get(fileset).add(file);
					} else {
						List<String> list = new ArrayList<String>();
						list.add(file);
						filesetsOfFiles.put(fileset, list);
					}
				}
			}
			
			logger.debug("Mapa de filesets {} tiene size {}", filesetsOfFiles, filesetsOfFiles.size());
			
			if (filesetsOfFiles != null && !filesetsOfFiles.isEmpty()) {
				for(String fileset : filesetsOfFiles.keySet()) {
					List<String> filesFileSet = filesetsOfFiles.get(fileset);
					logger.debug("Restore Files: tenemos filesFileset: {} del fileset {}", filesFileSet, fileset);
					if (filesFileSet != null && filesFileSet.size() > 0) {
						logger.debug("Lanzamos restore para filesFileset {}", filesFileSet);
						restoreFiles(client, destinationClient, fileset, filesFileSet, destinationPath, date, postscript, prescript);
					}
				}
			} else {
				StringBuilder _sbDate = new StringBuilder();
				_sbDate.append(date.get(Calendar.YEAR));
				_sbDate.append("-");
				_sbDate.append(twoCharFormat(date.get(Calendar.MONTH) + 1));
				_sbDate.append("-");
				_sbDate.append(twoCharFormat(date.get(Calendar.DAY_OF_MONTH)));
				_sbDate.append(" ");
				_sbDate.append(twoCharFormat(date.get(Calendar.HOUR_OF_DAY)));
				_sbDate.append(":");
				_sbDate.append(twoCharFormat(date.get(Calendar.MINUTE)));
				_sbDate.append(":00");
				throw new Exception("No versions found for file selection before selected date: "+_sbDate.toString());
			}
		} catch (Exception ex) {
			logger.error("Error restoring files by date clientId:{}, files:{}, destinationClientId:{}, lv:{}, lvshare:{}, destinationPath:{}, date:{}. Ex: {}", new Object[]{clientId, files, destinationClientId, lv, lvshare, destinationPath, ex.getMessage()});
			throw new Exception ("Error restoring files by date. Ex: "+ex.getMessage());
		}
	}
	
	
	
	private String getRestoreDestinationPath(String fileCheck, String destinationPath, String client, int clientId, String destinationClient, String lv, String lvshare) throws Exception {
		if("airback-fd".equalsIgnoreCase(destinationClient)) {
			if (lv != null && !lv.isEmpty()) {
				Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(lv);
				if(!VolumeManager.isLogicalVolumeMounted(_lv.get("vg"), _lv.get("lv"))) {
					throw new Exception("destination volume [" + _lv.get("vg") + "/" + _lv.get("lv") + "] is not mounted");
				}
				if (!destinationPath.startsWith("/"))
					destinationPath="/"+destinationPath;
				String volPath = WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+_lv.get("vg")+"/"+_lv.get("lv");
				if (fileCheck.contains(volPath)) {
					if (destinationPath.isEmpty())
						destinationPath = "/";
					else if (!destinationPath.startsWith("/"))
						destinationPath="/"+destinationPath;
					if (!destinationPath.equals("/"))
						destinationPath = volPath+destinationPath;
				} else
					destinationPath = volPath + destinationPath;
			} else if (lvshare != null && !lvshare.isEmpty()) {
				String server = lvshare.substring(0, lvshare.indexOf("@"));
				String share = lvshare.substring(lvshare.indexOf("@")+1);
				if (!ShareManager.isExternalShareMounted(server, share))
					throw new Exception("destination shared volume [" +server + "/" + share + "] is not mounted");
				String volPath = ShareManager.getExternalShareMountPath(server, share);
				if (fileCheck.contains(volPath)) {
					if (destinationPath.isEmpty())
						destinationPath = "/";
					else if (!destinationPath.startsWith("/"))
						destinationPath="/"+destinationPath;
					if (!destinationPath.equals("/"))
						destinationPath = volPath+destinationPath;
				} else
					destinationPath = volPath + destinationPath;
			} else {
				//FileSetManager _fsm = new FileSetManager(this._c);
				String path = "";
				//for(String _path : _fsm.getFileSetIncludesForClient(clientId)) {
				//	if (fileCheck.contains(path)) {
						if (fileCheck.contains(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/shares/")) {
							String subPath = fileCheck.substring(fileCheck.indexOf(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/shares/")+(WBSAirbackConfiguration.getDirectoryVolumeMount()+"/shares/").length());
							String server = subPath.substring(0, subPath.indexOf("/"));
							String share = subPath.substring(subPath.indexOf("/")+1);
							String loop = share;
							for (int i=0;i<5;i++)
								loop = loop.substring(loop.indexOf("/")+1);
							share = share.substring(0, share.indexOf("/"+loop));
							path = ShareManager.getExternalShareMountPath(server, share);
							if (!ShareManager.isExternalShareMounted(server, share))
								throw new Exception("destination shared volume [" +server + "/" + share + "] is not mounted");
						//	break;
							
						} else if (fileCheck.contains(WBSAirbackConfiguration.getDirectoryVolumeMount())) {
							Map<String, String> _lv = VolumeManager.getLogicalVolumeFromPath(fileCheck);
							path = WBSAirbackConfiguration.getDirectoryVolumeMount()+"/"+_lv.get("vg")+"/"+_lv.get("lv");
							if(!VolumeManager.isLogicalVolumeMounted(_lv.get("vg"), _lv.get("lv"))) {
								throw new Exception("destination volume [" + _lv.get("vg") + "/" + _lv.get("lv") + "] is not mounted");
							}
						//	break;
						}
				//	}
				//}
				if (destinationPath.isEmpty())
					destinationPath = "/";
				else if (!destinationPath.startsWith("/"))
					destinationPath="/"+destinationPath;
				if (!destinationPath.equals("/")) {
					destinationPath = path+destinationPath;
				}
			}
		} else {
			if(destinationPath.contains(":\\\\")) {
				destinationPath = destinationPath.replace("\\\\", "/");
			} else if(destinationPath.contains(":\\")) {
				destinationPath = destinationPath.replace("\\", "/");
			}
		}
		return destinationPath;
	}
	
	private void restoreFiles(String client, String destinationClient, String fileSet, List<String> files, String destinationPath, Calendar date, String postscript, String prescript) throws Exception {
		if(prescript != null && !prescript.isEmpty()) {
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run Before Job", new String[]{ "\"" + prescript + "\"" });
		} else if(postscript != null && !postscript.isEmpty()) {
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run After Job", new String[]{ "\"" + postscript + "\"" });
		} else {
			if(!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Job", "\"RestoreFiles\"", "Client Run Before Job").isEmpty()) {
				BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run Before Job");
			} else if(!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Job", "\"RestoreFiles\"", "Client Run After Job").isEmpty()) {
				BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run After Job");
			}
		}
		BackupOperator.reload();
		
		StringBuilder _sb = new StringBuilder(); 
        _sb.append("/tmp/wbsairback-restore-");
        Random _r = new Random();
        for(int i = 15; i >= 0; --i) {
        	_sb.append(_r.nextInt(10));
        }
        
        if(destinationPath != null && destinationPath.contains(" ")) {
        	destinationPath = destinationPath.trim();
        	if(!destinationPath.startsWith("\"")) {
        		destinationPath = "\"".concat(destinationPath);
        	}
        	if(!destinationPath.endsWith("\"")) {
        		destinationPath = destinationPath.concat("\"");
        	}
        }
        if(destinationPath != null && destinationPath.contains("\\")) {
        	destinationPath = destinationPath.trim();
        	destinationPath = destinationPath.replace("\\", "/");
        }
        
        File _f = new File(_sb.toString());
        try {
	        _sb = new StringBuilder();
			_sb.append("restore client=");
			_sb.append(client);
			_sb.append(" restorejob=RestoreFiles restoreclient=");
			_sb.append(destinationClient);
			if(destinationPath != null && !destinationPath.isEmpty()) {
				_sb.append(" where=");
				_sb.append(destinationPath);
			}
			if(fileSet != null && !fileSet.isEmpty()) {
				if(fileSet.contains(" ")) {
					fileSet = fileSet.trim();
		        	if(!fileSet.startsWith("\"")) {
		        		fileSet = "\"".concat(fileSet);
		        	}
		        	if(!fileSet.endsWith("\"")) {
		        		fileSet = fileSet.concat("\"");
		        	}
		        }
				_sb.append(" fileset=");
				_sb.append(fileSet);
			}
			if(date != null) {
				_sb.append(" before=");
				_sb.append(date.get(Calendar.YEAR));
				_sb.append("-");
				_sb.append(twoCharFormat(date.get(Calendar.MONTH) + 1));
				_sb.append("-");
				_sb.append(twoCharFormat(date.get(Calendar.DAY_OF_MONTH)));
				_sb.append("\\ ");
				_sb.append(twoCharFormat(date.get(Calendar.HOUR_OF_DAY)));
				_sb.append(":");
				_sb.append(twoCharFormat(date.get(Calendar.MINUTE)));
				_sb.append(":00");
			} else {
				_sb.append(" current");
			}
			_sb.append(" select");
			_sb.append("\n");
			
			if(files != null && !files.isEmpty()) {
				for(String file : files) {
					file = file.replace(" ", "\\ ");
					_sb.append("cd ");
			    	if(file.contains("/")) {
				    	_sb.append(file.substring(0, file.lastIndexOf("/")).trim());
				    } else if(file.contains("\\")) {
				    	_sb.append(file.substring(0, file.lastIndexOf("\\")).trim());
				    }
			    	_sb.append("\n");
				    _sb.append("mark ");
				    if(file.contains("/")) {
				    	if(!file.substring(file.lastIndexOf("/") + 1).isEmpty()) {
				    		_sb.append(file.substring(file.lastIndexOf("/") + 1));
				    	} else {
				    		_sb.append("*");
				    	}
				    } else if(file.contains("\\")) {
				    	if(!file.substring(file.lastIndexOf("\\") + 1).isEmpty()) {
				    		_sb.append(file.substring(file.lastIndexOf("\\") + 1));
				    	} else {
				    		_sb.append("*");
				    	}
				    } else {
				    	_sb.append(file);
				    }
				    _sb.append("\n");
				}
			} else {
				_sb.append(" mark *");
				_sb.append("\n");
			}		
			_sb.append("done\nyes\n");
			
			try {
	        	FileOutputStream _fos = new FileOutputStream(_f);
				_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
				_fos.close();
	        } catch(IOException ex) {
	            throw new Exception("fail to write temporary file: " + _sb.toString());
	        }
	        
			logger.debug("Restore file contents: {}", _sb.toString());
	        _sb = new StringBuilder();
			_sb.append("/usr/bin/bconsole <");
			_sb.append(_f.getAbsolutePath());
			_sb.append(" && sleep 2");
			
			Command.systemCommand(_sb.toString());
        } catch (Exception ex) {
			String msg = "Error restoring files [client:"+client+" file:"+files+" destinationClientId:"+destinationClient+" destinationPath:"+destinationPath+". Ex: "+ex.getMessage();
			logger.error(msg);
			throw new Exception(msg);
		} finally {
        	if(_f != null && _f.exists()) {
                _f.delete();
        	}
        }
	}
	
	private void restoreFile(String client, String job, String file, String destinationClient, String destinationPath, String postscript, String prescript) throws Exception {
		if(prescript != null && !prescript.isEmpty()) {
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run Before Job", new String[]{ "\"" + prescript + "\"" });
		} else if(postscript != null && !postscript.isEmpty()) {
			BaculaConfiguration.setBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run After Job", new String[]{ "\"" + postscript + "\"" });
		} else {
			if(!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Job", "\"RestoreFiles\"", "Client Run Before Job").isEmpty()) {
				BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run Before Job");
			} else if(!BaculaConfiguration.getBaculaParameter("/etc/bacula/bacula-dir.conf", "Job", "\"RestoreFiles\"", "Client Run After Job").isEmpty()) {
				BaculaConfiguration.deleteBaculaParameter("/etc/bacula/bacula-dir.conf", "Job","\"RestoreFiles\"", "Client Run After Job");
			}
		}
		BackupOperator.reload();
		
		StringBuilder _sb = new StringBuilder(); 
        _sb.append("/tmp/wbsairback-restore-");
        Random _r = new Random();
        for(int i = 15; i >= 0; --i) {
                _sb.append(_r.nextInt(10));
        }
        
        if(destinationPath != null && destinationPath.contains(" ")) {
        	destinationPath = destinationPath.trim();
        	if(!destinationPath.startsWith("\"")) {
        		destinationPath = "\"".concat(destinationPath);
        	}
        	if(!destinationPath.endsWith("\"")) {
        		destinationPath = destinationPath.concat("\"");
        	}
        }
        if(destinationPath != null && destinationPath.contains("\\")) {
        	destinationPath = destinationPath.trim();
        	destinationPath = destinationPath.replace("\\", "/");
        }
        
        File _f = new File(_sb.toString());
        try {
	        _sb = new StringBuilder();
			_sb.append("restore client=");
			_sb.append(client);
			_sb.append(" restorejob=RestoreFiles restoreclient=");
			_sb.append(destinationClient);
			if(destinationPath != null && !destinationPath.isEmpty()) {
				_sb.append(" where=");
				_sb.append(destinationPath);
			}
			_sb.append(" jobid=");
			_sb.append(job);
			_sb.append("\n");
			
			if(file != null && !file.isEmpty()) {
				file = file.replace(" ", "\\ ");
				_sb.append("cd ");
		    	if(file.contains("/")) {
			    	_sb.append(file.substring(0, file.lastIndexOf("/")));
			    } else if(file.contains("\\")) {
			    	_sb.append(file.substring(0, file.lastIndexOf("\\")));
			    }
		    	_sb.append("\n");
			    _sb.append("mark ");
			    if(file.contains("/")) {
			    	if(!file.substring(file.lastIndexOf("/") + 1).isEmpty()) {
			    		_sb.append(file.substring(file.lastIndexOf("/") + 1));
			    	} else {
			    		_sb.append("*");
			    	}
			    } else if(file.contains("\\")) {
			    	if(!file.substring(file.lastIndexOf("\\") + 1).isEmpty()) {
			    		_sb.append(file.substring(file.lastIndexOf("\\") + 1));
			    	} else {
			    		_sb.append("*");
			    	}
			    } else {
			    	_sb.append(file);
			    }
			    _sb.append("\n");
			} else {
				_sb.append(" mark *");
				_sb.append("\n");
			}
			_sb.append("done\nyes\n");
			
			try {
	        	FileOutputStream _fos = new FileOutputStream(_f);
				_fos.write(_sb.toString().getBytes(Charset.forName("UTF-8")));
				_fos.close();
	        } catch(IOException ex) {
	            throw new Exception("fail to write temporary file: " + _sb.toString());
	        }
	        
			logger.debug("Restore command string: {}", _sb.toString());
			
	        _sb = new StringBuilder();
			_sb.append("/usr/bin/bconsole <");
			_sb.append(_f.getAbsolutePath());
			_sb.append(" && sleep 2");

			Command.systemCommand(_sb.toString());
        } catch (Exception ex) {
			String msg = "Error restoring single file [client:"+client+" job:"+job+" file:"+file+" destinationClientId:"+destinationClient+" destinationPath:"+destinationPath+". Ex: "+ex.getMessage();
			logger.error(msg);
			throw new Exception(msg);
		} finally {
        	if(_f != null && _f.exists()) {
                _f.delete();
        	}
        }
	}
	
	public void runJob(int clientId, String jobName) throws Exception {
		String clientName = this._cm.getClientName(clientId);
		Command.systemCommand("echo \"run job=" + jobName + " yes Client=" + clientName + "\" | /usr/bin/bconsole");
		Thread.sleep(1000);
	}
	
	public void runJob(String jobName) throws Exception {
		System.out.println("Running job "+jobName+" ...");
		Command.systemCommand("echo \"run job=" + jobName + " yes\" | /usr/bin/bconsole");
		Thread.sleep(1000);
		System.out.println("Job launched");
	}
	
	public void runNextJob(String previousJob, String nextJob) throws Exception {
		System.out.println("Checking previousJob:"+previousJob);
		if(previousJob != null && !previousJob.isEmpty()) {
			if(!JobManager.existsjob(previousJob)) {
				System.err.println("previous job"+previousJob+"does not exists:");
				throw new Exception("previous job does not exists");
			}
			JobManager _jm = new JobManager(this._c);
			Map<String, String> _job = _jm.getLastArchivedJob(previousJob);
			if(_job == null || _job.get("alert") == null || !"good".equals(_job.get("alert"))) {
				System.out.println("State of previousJob ["+previousJob+"] is not good:"+_job.get("alert")+". Aborting ...");
				return;
			}
		}
		runJob(nextJob);
	}
	
	public void stopJob(int jobId) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("echo \"stop jobid=");
		_sb.append(jobId);
		_sb.append("\" | /usr/bin/bconsole");
		if (!Command.isRunning(_sb.toString())) {
			Command.systemCommand(_sb.toString());
			ObjectLock.block(ObjectLock.JOBS_TYPE_OBJECT, String.valueOf(jobId), "stop");
		}
		Thread.sleep(1000);
	}
	
	public static String twoCharFormat(Integer value) {
		StringBuilder _sb = new StringBuilder();
		if(value < 10) {
			_sb.append("0");
		}
		_sb.append(value);
		return _sb.toString();
	}

	public static boolean isBlock_reload() {
		return block_reload;
	}

	public static void setBlock_reload(boolean block_reload) {
		BackupOperator.block_reload = block_reload;
	}
}
