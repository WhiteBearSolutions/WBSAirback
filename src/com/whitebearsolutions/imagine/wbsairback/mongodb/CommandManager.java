package com.whitebearsolutions.imagine.wbsairback.mongodb;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandManager {

	private final static Logger logger = LoggerFactory.getLogger(CommandManager.class);
	
	private static DBManager dbmanager;
	private static CommandEntryDAO comDAO;
	
	public static final Integer STATUS_COMAND_UNLAUNCHED = 0;
	public static final Integer STATUS_COMAND_RUNING = 1;
	public static final Integer STATUS_COMAND_FINISHED = 2;
	public static final Integer STATUS_COMAND_TIME_OUT = 3;
	
	public CommandManager() {
		try {
    		dbmanager = new DBManager();
    		comDAO = new CommandEntryDAO(dbmanager.getMorphia(),dbmanager.getMongo());
		} catch(Exception _ex) {
			logger.error("Error inicializando la BD Mongo. Ex: "+_ex.getMessage());
		}   
	}
	
	private CommandEntry storeDBInitProcess(String command, String commandType) throws Exception {
		CommandEntry ce = new CommandEntry();
		if (command.contains(" | tee"))
			command = command.substring(0, command.indexOf(" | tee")).trim();
		if (command.contains("| /sbin/mbuffer")) {
			command = command.replace("\"", "");
			while (command.contains("| /sbin/mbuffer"))
				command = command.substring(0, command.indexOf("| /sbin/mbuffer"))+command.substring(command.indexOf("/dev/null ")+"/dev/null ".length());
		}
		logger.debug("Comando filtrado: {}", command);
		ce.setCommandString(command);
		ce.setTypeCommand(commandType);
		if (command.indexOf(" ") > -1) {
			String bin = command;
			if (bin.contains("pipefail &&  "))
				bin = bin.substring(bin.indexOf("pipefail &&  ")+"pipefail &&  ".length());
			if (bin.contains("sudo "))
				bin = bin.substring(bin.indexOf("sudo ")+"sudo ".length());
			bin = bin.substring(0, bin.indexOf(" "));
			ce.setCommandBin(bin);
			ce.setCommandParameters(command.substring(command.indexOf(bin+" ")+(bin+" ").length()));
		} else {
			ce.setCommandBin(command);
			ce.setCommandParameters("");
		}
		ce.setStatus(STATUS_COMAND_UNLAUNCHED);
		comDAO.save(ce);
		return ce;
	}
	
	private CommandEntry storeDBLaunchProcess(CommandEntry ce, Process p) throws Exception {
		ce.setDateLaunched(new Date());
		ce.setStatus(STATUS_COMAND_RUNING);
		ce.setProcessPid(getProcessPid(p));
		comDAO.save(ce);
		return ce;
	}
	
	private CommandEntry storeDBEndProcess(CommandEntry ce, Process p) throws Exception {
		ce.setExitCode(p.waitFor());
		ce.setStatus(STATUS_COMAND_FINISHED);
		ce.setDateFinished(new Date());
		ce.setErrOutput(readProcessStream(p.getErrorStream()));
		ce.setStOutput(readProcessStream(p.getInputStream()));
		p.getOutputStream();
		comDAO.save(ce);
		return ce;
	}
	
	public static String launchNoBDCommand(String command) throws Exception {
		Random r = new Random();
		ProcessLauncher pLaunch = new ProcessLauncher(null, null, "pLauncher_"+r.nextInt(100), command, null);
		pLaunch.start();
		pLaunch.join();
		if (pLaunch.getExitCode() != null && pLaunch.getExitCode() != 0) {
			if (pLaunch.getOutput() != null && !pLaunch.getOutput().isEmpty())
				throw new Exception("Error on command ["+command+"] | error output: " + pLaunch.getOutput());
			else
				throw new Exception("Unknown error on command ["+command+"]");
		}
					
		return pLaunch.getOutput();
	}
	
	
	private void storeDBEndProcessAsync(CommandEntry ce, String command, String eraseFile) throws Exception {
		Random r = new Random();
		ProcessLauncher pLaunch = new ProcessLauncher(ce.getId(), comDAO, "pLauncher_"+ce.getCommandBin()+"_"+r.nextInt(100), command, eraseFile);
		pLaunch.start();
	}
	
	private void storeDBEndProcessAsyncAndWait(CommandEntry ce, String command, String eraseFile) throws Exception {
		Random r = new Random();
		ProcessLauncher pLaunch = new ProcessLauncher(ce.getId(), comDAO, "pLauncher_"+ce.getCommandBin()+"_"+r.nextInt(100), command, eraseFile);
		pLaunch.start();
		pLaunch.join();
	}
	
	public static Long getProcessPid(Process p) {
		Long pid = null;
		try {
		    Field f = p.getClass().getDeclaredField("pid");
		    f.setAccessible(true);
		    pid = new Long(f.getInt(p));
		    return pid;
		} catch (Exception _ex) {
			logger.error("Error obteniendo el pid de cierto proceso. Ex: "+_ex.getMessage());
			return null;
		}
	}
	
	private String readProcessStream(InputStream is) throws Exception {
		if (is == null)
			return "";
		
		BufferedReader _input = null;
		StringWriter _output = new StringWriter();
		try {
			_input = new BufferedReader(new InputStreamReader(is));
			String line;
			while(_input.ready()) {
				line = _input.readLine();
				if(line == null) { break; }
				_output.write(line + "\n");
			}
			return _output.toString();
		} finally {
			if(_input != null) {
				_input.close();
			}
			_output.close();
		}
	}
	
	public CommandEntry getCommandById(ObjectId id) throws Exception {
		try {
			return comDAO.get(id);
		} catch (Exception ex) {
			logger.error("Error buscando comando por id: {} en la bd. Ex: {}", id, ex.getMessage());
			return null;
		}
	}
	
	
	public CommandEntry execute(String command, String commandType) throws Exception {
		
		CommandEntry ce = storeDBInitProcess(command, commandType);
		String[] format_command = new String[] { "/bin/bash", "-c", command }; 
		Process p = Runtime.getRuntime().exec(format_command);
		ce = storeDBLaunchProcess(ce, p);
		ce = storeDBEndProcess(ce, p);
		if (ce.getExitCode() != 0) {
			if (ce.getErrOutput() != null && !ce.getErrOutput().isEmpty())
				throw new Exception("Error on command ["+ce.getCommandString()+"] | error output: " + ce.getErrOutput());
			else if (ce.getStOutput() != null && !ce.getStOutput().isEmpty())
				throw new Exception("Unknonw error on command ["+ce.getCommandString()+"] | output:" + ce.getStOutput());
			else
				throw new Exception("Unknown error on command ["+ce.getCommandString()+"]");
		}

		return ce;
	}
	
	public void updateUnfinishedCommand(CommandEntry ce) throws Exception {
		ce.setDateFinished(new Date());
		ce.setStatus(STATUS_COMAND_TIME_OUT);
		comDAO.save(ce);
	}
	
	public void deleteCommands(List<CommandEntry> list) throws Exception {
		try {
			if (list != null && !list.isEmpty())
				for (CommandEntry ce : list)
					deleteCommand(ce);
		} catch (Exception ex) {
			logger.error("Error eliminando lista de comandos de la bd. Ex: {}", ex.getMessage());
			throw new Exception("Error deleting command list: "+ex.getMessage());
		}
	}
	
	
	public void deleteCommand(CommandEntry ce) throws Exception {
		try {
			if (ce != null)
				comDAO.delete(ce);
		} catch (Exception ex) {
			logger.error("Error eliminando comando: {} de la bd. Ex: {}", ce.getId(), ex.getMessage());
			throw new Exception("Error deleting comand: "+ex.getMessage());
		}
	}
	
	
	public List<CommandEntry> findCommandsByCommandBin(String bin) {
		try {
			return comDAO.findByCommandBin(bin);
		} catch (Exception ex) {
			logger.error("Error buscando comandos por bin: {} en la bd. Ex: {}", bin, ex.getMessage());
			return null;
		}
	}
	
	public List<CommandEntry> findCommandsByCommandBinOptions(List<String> bins) {
		try {
			return comDAO.findByCommandBins(bins);
		} catch (Exception ex) {
			logger.error("Error buscando comandos por bins: {} en la bd. Ex: {}", bins, ex.getMessage());
			return null;
		}
	}
	
	public boolean isRunning(String command) throws Exception {
		try {
			List<CommandEntry> listcommands = findCommandsByCommandStringAndStatus(command, STATUS_COMAND_RUNING);
			if (listcommands != null && !listcommands.isEmpty())
				return true;
			return false;
		} catch (Exception ex) {
			logger.error("Error comprobando si el comando: {} esta running en la bd. Ex: {}", command, ex.getMessage());
			return false;
		}
	}
	
	public List<CommandEntry> getZFSSendRunning(String agg, String vol) {
		try {
			List<CommandEntry> listcommands = findCommandsByCommandBinAndStatus("/sbin/zfs", STATUS_COMAND_RUNING);
			List<CommandEntry> running = new ArrayList<CommandEntry>();
			if (listcommands != null && !listcommands.isEmpty()) {
				for (CommandEntry cm : listcommands) {
					if (cm.getCommandString() != null  && cm.getCommandString().contains("send") && cm.getCommandString().contains(agg) && cm.getCommandString().contains(vol))
						running.add(cm);
				}
			}
			return running;
		} catch (Exception ex) {
			logger.error("Error comprobando si algun comando zfs send para {}/{}: esta running en la bd. Ex: {}", new Object[]{agg, vol, ex.getMessage()});
			return null;
		}
	}
	
	public List<CommandEntry> findCommandsByCommandStringAndStatus(String command, int status) {
		try {
			return comDAO.findByCommandStringAndStatus(command, status);
		} catch (Exception ex) {
			logger.error("Error buscando comandos por string: {} y estado: {}. Ex: {}", new Object[]{command, status, ex.getMessage()});
			return null;
		}
	}
	
	public List<CommandEntry> findCommandsByCommandBinAndStatus(String command, int status) {
		try {
			return comDAO.findByCommandBinAndStatus(command, status);
		} catch (Exception ex) {
			logger.error("Error buscando comandos por bin: {} y estado: {}. Ex: {}", new Object[]{command, status, ex.getMessage()});
			return null;
		}
	}
	
	public CommandEntry asyncExecuteAndWait(String command, String commandType, String pathEraseFile) throws Exception {
		CommandEntry ce = storeDBInitProcess(command, commandType);
		storeDBEndProcessAsyncAndWait(ce, command, pathEraseFile);
		if (ce.getExitCode() != null && ce.getExitCode() != 0) {
			if (ce.getErrOutput() != null && !ce.getErrOutput().isEmpty())
				throw new Exception("Error on command ["+ce.getCommandString()+"] | error output: " + ce.getErrOutput());
			else if (ce.getStOutput() != null && !ce.getStOutput().isEmpty())
				throw new Exception("Unknonw error on command ["+ce.getCommandString()+"] | output:" + ce.getStOutput());
			else
				throw new Exception("Unknown error on command ["+ce.getCommandString()+"]");
		}
					
		return ce;
	}
	
	public CommandEntry asyncExecute(String command, String commandType, String pathEraseFile) throws Exception {
		CommandEntry ce = storeDBInitProcess(command, commandType);
		storeDBEndProcessAsync(ce, command, pathEraseFile);
		if (ce.getExitCode() != null && ce.getExitCode() != 0) {
			if (ce.getErrOutput() != null && !ce.getErrOutput().isEmpty())
				throw new Exception("Error on command ["+ce.getCommandString()+"] | error output: " + ce.getErrOutput());
			else if (ce.getStOutput() != null && !ce.getStOutput().isEmpty())
				throw new Exception("Unknonw error on command ["+ce.getCommandString()+"] | output:" + ce.getStOutput());
			else
				throw new Exception("Unknown error on command ["+ce.getCommandString()+"]");
		}
					
		return ce;
	}
}
