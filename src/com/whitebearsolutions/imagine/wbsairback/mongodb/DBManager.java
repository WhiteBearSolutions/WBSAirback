package com.whitebearsolutions.imagine.wbsairback.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.whitebearsolutions.imagine.wbsairback.ServiceManager;

public class DBManager {

	private static String host = "127.0.0.1";
	private static String db = "airback";
	
	private static Mongo mongo;
	private static Morphia morphia;
	
	private final static Logger logger = LoggerFactory.getLogger(DBManager.class);
	
	public DBManager() throws Exception {
		initDB();
	}
	
	public DBManager(String _host, String _db) throws Exception {
		host = _host;
		db = _db;
		initDB();
	}
	
	public void initDB() throws Exception {
		try {
			ServiceManager.initialize(ServiceManager.MONGODB);
			morphia = new Morphia();
			morphia.map(CommandEntry.class);
			mongo = new Mongo(host);
			mongo.getMongoOptions().setConnectionsPerHost(200);
			mongo.getMongoOptions().setMaxWaitTime(5000);
			mongo.getMongoOptions().setSocketKeepAlive(true);
			mongo.getMongoOptions().setThreadsAllowedToBlockForConnectionMultiplier(100);
			mongo.getMongoOptions().setSafe(true);
			mongo.getMongoOptions().setAutoConnectRetry(true);
			//mongo.getMongoOptions().setJ(true);
			mongo.getMongoOptions().setMaxAutoConnectRetryTime(5000);
			
			Datastore ds = morphia.createDatastore(mongo, db);
			ds.ensureIndexes(); 
			ds.ensureCaps();
		} catch (Exception ex) {
			logger.error("Error inicializando MongoDB en [{}] bd: [{}]. Ex: {}", new Object[]{host, db, ex.getMessage()});
			throw ex;
		}
	}

	// Getter AND Setters
	
	public static String getDb() {
		return db;
	}

	public static void setDb(String db) {
		DBManager.db = db;
	}

	public static String getHost() {
		return host;
	}

	public static void setHost(String host) {
		DBManager.host = host;
	}

	public Mongo getMongo() {
		return mongo;
	}

	public void setMongo(Mongo m) {
		mongo = m;
	}

	public Morphia getMorphia() {
		return morphia;
	}

	public void setMorphia(Morphia m) {
		morphia = m;
	}
}
