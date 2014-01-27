package com.whitebearsolutions.imagine.wbsairback.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import com.whitebearsolutions.io.FileLock;
import com.whitebearsolutions.io.FileLockException;
import com.whitebearsolutions.io.FileUtils;

public class FileSystem {

	public FileSystem() {
		// TODO Auto-generated constructor stub
	}
	
	public static void copyFile(File src, File dst) throws Exception {
		FileUtils.copyFile(src, dst);
	}
	
	public static void delete(File f) throws IOException {
		if(f.isDirectory()) {
			for(File _f : f.listFiles()) {
				delete(_f);
			}
		}
		if(!f.delete()) {
			throw new FileNotFoundException("delete failed: " + f.getAbsolutePath());
		}
	}
	
	public static void writeFile(File f, String content) throws IOException, FileLockException {
		FileWriter _fw = null;
		FileLock _fl = new FileLock(f);
		try {
			_fl.lock();
			_fw = new FileWriter(f);
			_fw.write(content);
		} finally {
			try {
				_fl.unlock();
			} catch(Exception _ex) {}
			try {
				if(_fw != null) {
					_fw.close();
				}
			} catch(Exception _ex) {}
		}
	}
	
	public static void writeAppendFile(File f, String content) throws IOException, FileLockException {
		FileWriter _fw = null;
		FileLock _fl = new FileLock(f);
		try {
			_fl.lock();
			_fw = new FileWriter(f, true);
			_fw.write(content);
		} finally {
			try {
				_fl.unlock();
			} catch(Exception _ex) {}
			try {
				if(_fw != null) {
					_fw.close();
				}
			} catch(Exception _ex) {}
		}
	}
	
	public static void writeFileCharset(File f, String content, String charset) throws IOException, FileLockException {
		FileOutputStream _fos = new FileOutputStream(f);
		FileLock _fl = new FileLock(f);
		try {
			_fl.lock();
			_fos.write(content.getBytes(Charset.forName(charset)));
		} finally {
			try {
				_fl.unlock();
			} catch(Exception _ex) {}
			try {
				if(_fos != null) {
					_fos.close();
				}
			} catch(Exception _ex) {}
		}
	}
}
