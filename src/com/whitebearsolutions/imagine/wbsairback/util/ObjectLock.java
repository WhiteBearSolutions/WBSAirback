package com.whitebearsolutions.imagine.wbsairback.util;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.imagine.wbsairback.io.FileSystem;

public class ObjectLock {

	public final static String pathLock = WBSAirbackConfiguration.getDirectoryObjectLock();
	private final static Logger logger = LoggerFactory.getLogger(ObjectLock.class);
	private final static String extFileBlock = ".blk";
	
	private final static int GENERAL_LOOP_TIME = 30000;
	
	public final static int JOBS_TYPE_OBJECT = 1;
	private final static String JOBS_TYPE_OBJECT_NAME = "job";
	private final static long JOBS_TIME_BLOCK = 60000; //60 segundos
	private final static String pathJobs = pathLock+"/"+JOBS_TYPE_OBJECT_NAME;
	
	public final static int SNAPSHOTS_TYPE_OBJECT = 2;
	private final static String SNAPSHOTS_TYPE_OBJECT_NAME = "snapshots";
	private final static long SNAPSHOTS_TIME_BLOCK = 30*60*1000; //5 minutos
	private final static String pathSnapshots = pathLock+"/"+SNAPSHOTS_TYPE_OBJECT_NAME;
	
	public final static int GROUP_JOBS_TYPE_OBJECT = 3;
	private final static String GROUP_JOBS_TYPE_OBJECT_NAME = "groupjob";
	private final static long GROUP_JOBS_TIME_BLOCK = 60000; //60 segundos
	private final static String pathGroupJobs = pathLock+"/"+GROUP_JOBS_TYPE_OBJECT_NAME;
	
	public static Map<Integer, String> nameTypes;
	
	static {
		initPaths();
		nameTypes = new HashMap<Integer, String>();
		nameTypes.put(JOBS_TYPE_OBJECT, JOBS_TYPE_OBJECT_NAME);
		nameTypes.put(SNAPSHOTS_TYPE_OBJECT, SNAPSHOTS_TYPE_OBJECT_NAME);
		nameTypes.put(GROUP_JOBS_TYPE_OBJECT, GROUP_JOBS_TYPE_OBJECT_NAME);
	}
	
	public static void initPaths() {
		try {
			File dir = new File(pathLock);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			dir = new File(pathJobs);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			dir = new File(pathSnapshots);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			dir = new File(pathGroupJobs);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		} catch (Exception ex) {
			logger.warn("Atención, se produjo un error intentando crear los paths para bloqueos");
		}
	}
		
		
	/**
	 * Devuelve el tiempo de bloqueo asociado a un tipo de objeto
	 * @param typeObject
	 * @return
	 * @throws Exception
	 */
	public static long getTimeTypeObject(int typeObject) throws Exception {
		long time = 0;
		switch (typeObject) {
			case JOBS_TYPE_OBJECT : time =  JOBS_TIME_BLOCK; break;
			case SNAPSHOTS_TYPE_OBJECT : time = SNAPSHOTS_TIME_BLOCK; break;
			case GROUP_JOBS_TYPE_OBJECT : time = GROUP_JOBS_TIME_BLOCK; break;
		}
		return time;
	}
	
	
	/**
	 * Obtiene el path de cierto tipo de objetos a bloquear
	 * @param typeObject
	 * @return
	 * @throws Exception
	 */
	public static String getPathTypeObject(int typeObject) throws Exception {
		String path = "";
		switch (typeObject) {
			case JOBS_TYPE_OBJECT : path =  pathJobs; break;
			case SNAPSHOTS_TYPE_OBJECT: path = pathSnapshots; break;
			case GROUP_JOBS_TYPE_OBJECT : path = pathGroupJobs; break;
		}
		return path;
	}
	
	
	/**
	 * Obtiene el nombre del fichero de bloquer de cierto objeto
	 * @param typeObject
	 * @param idObject
	 * @param operation
	 * @return
	 * @throws Exception
	 */
	public static String getNameBlock(int typeObject, String idObject, String operation) throws Exception {
		String op = "";
		if (operation != null && !operation.equals(""))
			op = operation+".";
		
		return op+idObject+"."+nameTypes.get(typeObject)+extFileBlock;
	}
	
	
	/**
	 * Obtiene el path completo del fichero de bloqueo
	 * @param typeObject
	 * @param idObject
	 * @param operation
	 * @return
	 * @throws Exception
	 */
	private static String getPathBlock(int typeObject, String idObject, String operation) throws Exception {		
		return getPathTypeObject(typeObject)+"/"+getNameBlock(typeObject, idObject, operation);
	}
	
	
	/**
	 * Genera el fichero de bloqueo del objeto
	 * @param typeObject
	 * @param idObject
	 * @param operation
	 * @throws Exception
	 */
	public static void block(int typeObject, String idObject, String operation) throws Exception {
		initPaths();
		File fileBlock = new File(getPathBlock(typeObject, idObject, operation));
		if (!fileBlock.exists())
			FileSystem.writeFile(fileBlock, "--");
	}
	
	
	/**
	 * Desbloquea una operacion de cierto objeto
	 * @param typeObject
	 * @param idObject
	 * @param operation
	 * @throws Exception
	 */
	public static void unblock(int typeObject, String idObject, String operation) throws Exception {
		initPaths();
		File fileBlock = new File(getPathBlock(typeObject, idObject, operation));
		if (fileBlock.exists())
			fileBlock.delete();
	}
	
	
	/**
	 * Elimina todos los bloqueos asociados a un objeto
	 * @param typeObject
	 * @param idObject
	 * @throws Exception
	 */
	public static void unblockAll(int typeObject, String idObject) throws Exception {
		File dirObjects = new File(getPathTypeObject(typeObject));
		String[] listDir = dirObjects.list();
		if (listDir != null) {
    		for (String el : listDir) {
    			if (el.contains(getNameBlock(typeObject, idObject, null))) {
    				File fileBlock = new File(getPathTypeObject(typeObject)+"/"+el);
    				if (fileBlock.exists())
    					fileBlock.delete();
    			}
    		}
		}
	}
	
	
	
	/**
	 * Indica si un objeto está bloqueado
	 * @param typeObject
	 * @param idObject
	 * @param operation
	 * @return
	 */
	public static boolean isBlock(int typeObject, String idObject, String operation) {
		try {
			File dirObjects = new File(getPathTypeObject(typeObject));
			String[] listDir = dirObjects.list();
			if (listDir != null) {
	    		for (String el : listDir) {
	    			if (el.contains(getNameBlock(typeObject, idObject, operation))) {
	    				return true;
	    			}
	    		}
			}
		} catch (Exception ex) {
			return false;
		}
		return false;
	}
	
	
	
	/**
	 * Lanza el proceso de control-eliminación de ficheros asociados a ciertos tipos de objeto
	 * @param types
	 * @throws Exception
	 */
	public static void runControlBlockFiles(final List<Integer> types) throws Exception {
		Runnable r = new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(GENERAL_LOOP_TIME);
						for (Integer type : nameTypes.keySet()) {
							if (types == null || types.size() <= 0 || types.contains(type)) {
								File dirBlocks = new File(getPathTypeObject(type));
								String[] listDir = dirBlocks.list();
								if (listDir != null) {
						    		for (String el : listDir) {
						    			if (el.contains(extFileBlock)) {
						    				Long now = (new Date()).getTime();
						    				File opFile = new File(getPathTypeObject(type)+"/"+el);
						    				Long timeElapsed = now - opFile.lastModified();
						    				if ( timeElapsed >= getTimeTypeObject(type)) {
						    					logger.info("El fichero de bloqueo de operaciones {} del objeto de tipo {} superó el tiempo de retención {}. Eliminamos ...", new Object[]{type, el, getTimeTypeObject(type)});
						    					opFile.delete();
						    				}
						    			}
						    		}
								}
							}
						}
					} catch (Exception ex) {
						logger.error("Se produjo un error en el proceso de comprobación de ficheros de bloqueo. Ex: {}",ex.getMessage());
					}
					
				}
			}
		};
		Thread internalThread = new Thread(r);
		internalThread.setName("objectBlockControl");
		internalThread.start();
	}
	
}
