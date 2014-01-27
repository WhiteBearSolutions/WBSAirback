package com.whitebearsolutions.imagine.wbsairback.mongodb;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamProcess extends Thread {
	
	private InputStream is;
	private ObjectId processId;
	private CommandEntryDAO comDAO;
	private String type;
	private String output;
	private final static Logger logger = LoggerFactory.getLogger(StreamProcess.class);
	
    public StreamProcess(ObjectId processId, InputStream is, String type, CommandEntryDAO comDAO, String nameStream) {
        this.is = is;
        this.comDAO = comDAO;
        this.processId=processId;
        this.setName(nameStream);
    }
    
    public void run() {
		BufferedReader _input = null;
		StringWriter _output = new StringWriter();
		try {
			_input = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = _input.readLine()) != null) {
				if (comDAO != null) {
					CommandEntry newCe = comDAO.get(processId);
					if (type != null && type.equals("err")) {
						if (newCe.getErrOutput() != null && !newCe.getErrOutput().isEmpty())
							newCe.setErrOutput(newCe.getErrOutput()+line+"\n");
						else
							newCe.setErrOutput(line+"\n");
					} else {
						if (newCe.getStOutput() != null && !newCe.getStOutput().isEmpty())
							newCe.setStOutput(newCe.getStOutput()+line+"\n");
						else
							newCe.setStOutput(line+"\n");
					}
					comDAO.save(newCe);
				} else {
					_output.append(line+"\n");
				}
			}
		} catch (Exception ex) {
			logger.error("Error leyendo salida estandar asíncrona :"+ex.getMessage());
		} finally {
			try {
				if(_input != null) {
					_input.close();
				}
				_output.close();
				if (comDAO == null)
					this.output = _output.toString();
			} catch (Exception ex) {
				logger.error("Error cerrando Streams en lectura de salida estandar asíncrona :"+ex.getMessage());
			}
		}
    }

	public String getOutput() {
		return output;
	}
}
