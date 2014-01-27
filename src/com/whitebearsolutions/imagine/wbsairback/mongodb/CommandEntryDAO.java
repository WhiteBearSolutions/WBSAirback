package com.whitebearsolutions.imagine.wbsairback.mongodb;

import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.morphia.Key;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.dao.BasicDAO;
import com.google.code.morphia.query.Query;
import com.mongodb.Mongo;

public class CommandEntryDAO extends BasicDAO<CommandEntry, ObjectId> {
	
	public static final Integer mongodb_retrys = 10;
	private final static Logger logger = LoggerFactory.getLogger(CommandEntryDAO.class);
	
	public CommandEntryDAO(Morphia morphia, Mongo mongo ) {
        super(mongo, morphia, DBManager.getDb());
    }
	
	public List<CommandEntry> findByCommandBin(String commandBin) throws Exception {
		if (commandBin != null && !commandBin.isEmpty()) {
			int retry = 0;
			while (retry < mongodb_retrys) {
				try {
					List<CommandEntry> list = ds.find( CommandEntry.class ).field( "commandBin" ).contains( commandBin ).order( "-dateFinished, -dateLaunched" ).asList();
					return list;
				} catch (Exception ex) {
					retry++;
					logger.debug("MongoDB error. Retry: {}. Ex: {}", retry, ex.getMessage());
					if (retry >= mongodb_retrys)
						throw ex;
					try {
						Thread.sleep(200);
					} catch (Exception _ex){}
				}
			}
			logger.error("Error. findByCommandBin failed after {} retrys", retry);
			return null;
		} else
			return null;
    }
	
	public List<CommandEntry> findByCommandBins(List<String> commandBins) throws Exception {
		if (commandBins != null && !commandBins.isEmpty()) {
			int retry = 0;
			while (retry < mongodb_retrys) {
				try {
					Query<CommandEntry> query = ds.createQuery(CommandEntry.class);
					List<CommandEntry> list = query.field("commandBin").hasAnyOf(commandBins).order( "-dateFinished, -dateLaunched" ).asList(); 
					return list;
				} catch (Exception ex) {
					retry++;
					logger.debug("MongoDB error. Retry: {}. Ex: {}", retry, ex.getMessage());
					if (retry >= mongodb_retrys)
						throw ex;
					try {
						Thread.sleep(200);
					} catch (Exception _ex){}
				}
			}
			logger.error("Error. findByCommandBin failed after {} retrys", retry);
			return null;
		} else
			return null;
    }
	
	public List<CommandEntry> findByCommandStringAndStatus(String commandString, int status) throws Exception{
		if (commandString != null && !commandString.isEmpty() && status > -1) {
			int retry = 0;
			while (retry < mongodb_retrys) {
				try {
					List<CommandEntry> list = ds.find( CommandEntry.class ).field( "commandString" ).equal( commandString ).field( "status" ).equal(status).asList();
					return list;
				} catch (Exception ex) {
					retry++;
					logger.debug("MongoDB error. Retry: {}. Ex: {}", retry, ex.getMessage());
					if (retry >= mongodb_retrys)
						throw ex;
					try {
						Thread.sleep(200);
					} catch (Exception _ex){}
				}
			}
			logger.error("Error. findByCommandStringAndStatus failed after {} retrys", retry);
			return null;
		}
		else
			return null;
    }
	
	public List<CommandEntry> findByCommandBinAndStatus(String commandBin, int status) throws Exception{
		if (commandBin != null && !commandBin.isEmpty() && status > -1) {
			int retry = 0;
			while (retry < mongodb_retrys) {
				try {
					List<CommandEntry> list = ds.find( CommandEntry.class ).field( "commandBin" ).contains( commandBin ).field( "status" ).equal(status).asList();
					return list;
				} catch (Exception ex) {
					retry++;
					logger.debug("MongoDB error. Retry: {}. Ex: {}", retry, ex.getMessage());
					if (retry >= mongodb_retrys)
						throw ex;
					try {
						Thread.sleep(200);
					} catch (Exception _ex){}
				}
			}
			logger.error("Error. findByCommandBinAndStatus failed after {} retrys", retry);
			return null;
		}
		else
			return null;
    }
	
	@Override
	public Key<CommandEntry> save(CommandEntry entity) {
		int retry = 0;
		while (retry < mongodb_retrys) {
			try {
				Key<CommandEntry> key = super.save(entity);
				return key;
			} catch (Exception ex) {
				retry++;
				logger.debug("MongoDB error. Retry: {}. Ex: {}", retry, ex.getMessage());
				try {
					Thread.sleep(200);
				} catch (Exception _ex){}
			}
		}
		logger.error("Error. save failed after {} retrys", retry);
		return super.save(entity);
	}
	
	@Override
	public CommandEntry get(ObjectId id) {
		int retry = 0;
		while (retry < mongodb_retrys) {
			try {
				CommandEntry command = super.get(id);
				return command;
			} catch (Exception ex) {
				retry++;
				logger.debug("MongoDB error. Retry: {}. Ex: {}", retry, ex.getMessage());
				try {
					Thread.sleep(200);
				} catch (Exception _ex){}
			}
		}
		logger.error("Error. get(id) failed after {} retrys", retry);
		return super.get(id);
	}
	
	@Override
	public void delete(CommandEntry entity) {
		int retry = 0;
		while (retry < mongodb_retrys) {
			try {
				super.delete(entity);
				return;
			} catch (Exception ex) {
				retry++;
				logger.debug("MongoDB error. Retry: {}. Ex: {}", retry, ex.getMessage());
				try {
					Thread.sleep(200);
				} catch (Exception _ex){}
			}
		}
		logger.error("Error. delete(entity) failed after {} retrys", retry);
		super.delete(entity);
	}
}
