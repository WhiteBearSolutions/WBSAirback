package com.whitebearsolutions.imagine.wbsairback.mongodb;

import java.util.Date;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;

@Indexes(@Index(name = "ComposedDate", value = "-dateFinished, -dateLaunched"))
public class CommandEntry {
	
	@Id ObjectId id;
	
	@Indexed
	private String typeCommand;

	@Indexed
	private Date dateLaunched;

	@Indexed
	private Date dateFinished;
	
	@Indexed
	private Long processPid;

	@Indexed
	private Integer exitCode;

	@Indexed
	private Integer status;
	
	@Indexed
	private String commandString;

	@Indexed
	private String commandBin;

	private String commandParameters;
	
	private String stOutput;

	private String errOutput;
	
	public CommandEntry() {} 
	
	// GETTERS Y SETTERS
	
	public String getTypeCommand() {
		return typeCommand;
	}
	public void setTypeCommand(String typeCommand) {
		this.typeCommand = typeCommand;
	}
	public Date getDateLaunched() {
		return dateLaunched;
	}
	public void setDateLaunched(Date dateLaunched) {
		this.dateLaunched = dateLaunched;
	}
	public Date getDateFinished() {
		return dateFinished;
	}
	public void setDateFinished(Date dateFinished) {
		this.dateFinished = dateFinished;
	}
	public Long getProcessPid() {
		return processPid;
	}
	public void setProcessPid(Long processPid) {
		this.processPid = processPid;
	}
	public Integer getExitCode() {
		return exitCode;
	}
	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}
	public String getCommandString() {
		return commandString;
	}
	public void setCommandString(String commandString) {
		this.commandString = commandString;
	}
	public String getStOutput() {
		return stOutput;
	}
	public void setStOutput(String stOutput) {
		this.stOutput = stOutput;
	}
	public String getErrOutput() {
		return errOutput;
	}
	public void setErrOutput(String errOutput) {
		this.errOutput = errOutput;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCommandBin() {
		return commandBin;
	}

	public void setCommandBin(String commandBin) {
		this.commandBin = commandBin;
	}

	public String getCommandParameters() {
		return commandParameters;
	}

	public void setCommandParameters(String commandParameters) {
		this.commandParameters = commandParameters;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
}
