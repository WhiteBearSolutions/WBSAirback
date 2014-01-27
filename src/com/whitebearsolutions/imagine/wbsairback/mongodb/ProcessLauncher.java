package com.whitebearsolutions.imagine.wbsairback.mongodb;

import java.io.File;
import java.util.Date;
import java.util.Random;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessLauncher extends Thread {
	
	private String command;
	private CommandEntryDAO comDAO;
	private ObjectId processId;
	private String fileErase;
	private final static Logger logger = LoggerFactory.getLogger(ProcessLauncher.class);
	private Integer exitCode;
	private String output;
    
    public ProcessLauncher(ObjectId processId, CommandEntryDAO comDAO, String nameStream, String command, String fileErase) throws Exception {
    	this.command = command;
        this.comDAO = comDAO;
        this.processId=processId;
        this.fileErase = fileErase;
        this.setName(nameStream);
    }
    
    public void run() {
		try {
			// Lanzamos el comando
			String[] format_command = new String[] { "/bin/bash", "-c", command }; 
			Process p = Runtime.getRuntime().exec(format_command);
			
			// Ponemos en la BD que el comando está lanzado
			CommandEntry ce = null;
			if (comDAO != null) {
				logger.info("Comando {} lanzado. id: {}", command, processId);
				ce = comDAO.get(processId);
				ce.setDateLaunched(new Date());
				ce.setStatus(CommandManager.STATUS_COMAND_RUNING);
				ce.setProcessPid(CommandManager.getProcessPid(p));
				comDAO.save(ce);
			}
			
			// Lanzamos los dos threads de lectura 
			Random r = new Random();
			StreamProcess streamSt = new StreamProcess(processId, p.getInputStream(), "st", comDAO, "readSt_"+r.nextInt(100));
			StreamProcess errSt = new StreamProcess(processId, p.getErrorStream(), "err", comDAO, "readErr_"+r.nextInt(100));
			streamSt.start();
			errSt.start();
			if (ce != null)
				logger.debug("Lecturas de salida st y err para {} lanzadas", processId);
			
			// Esperamos a que termine el proceso y le ponemos los datos de salida
			int exitCode = p.waitFor();
			Thread.sleep(400);
			// Esperamos a que se cierren las salidas estandar y de error
			if (ce != null) {
				ce = comDAO.get(processId);
				ce.setExitCode(exitCode);
				ce.setStatus(CommandManager.STATUS_COMAND_FINISHED);
				ce.setDateFinished(new Date());
				comDAO.save(ce);
			}
			if (exitCode != 0) {
				if (errSt.getOutput() != null && !errSt.getOutput().isEmpty())
					output = errSt.getOutput();
				else if (streamSt.getOutput() != null && !streamSt.getOutput().isEmpty())
					output = streamSt.getOutput();
				else
					output = "";
			} else
				output = streamSt.getOutput();
			this.exitCode = exitCode;
			
			if (ce != null)
				logger.debug("Comando {} termino con {} y se almaceno en BD", processId, exitCode);
		} catch (Exception ex) {
			logger.error("Error en waitFor process :"+ex.getMessage());
		} finally {
			try {
				if (this.fileErase != null) {
					File f = new File(this.fileErase);
					if(f.exists()) {
						f.delete();
					}
				}
			} catch (Exception ex){}
		}
    }

	public Integer getExitCode() {
		return exitCode;
	}

	public String getOutput() {
		return output;
	}
}
