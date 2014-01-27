package com.whitebearsolutions.imagine.wbsairback.i18n;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;

public class WBSAirbackResourceBundle extends ResourceBundle.Control {
	public List<String> getFormats(String name) {
		if(name == null) {
			throw new NullPointerException();
		}
		return Arrays.asList("xml");
	}

	public ResourceBundle newBundle(String name, Locale locale, String format,
			ClassLoader loader, boolean reload) throws IllegalAccessException,
			InstantiationException, IOException {
		if(name == null || locale == null || format == null || loader == null) {
			throw new NullPointerException();
		}
		ResourceBundle bundle = null;
		try {
			if(format.equals("xml")) {
				String _name = toBundleName(name, locale);
				String _resource = toResourceName(_name, format);
				InputStream _is = null;
				
				File _f = new File(WBSAirbackConfiguration.getDirectoryI18n());
				if(_f.isDirectory()) {
					for(File _path : _f.listFiles()) {
						if(_path.getName().equals(_resource)) {
							_is = new FileInputStream(_path);
							break;
						}
					}
				}
				if(_is != null) {
					BufferedInputStream bis = new BufferedInputStream(_is);
					bundle = new XMLResourceBundle(bis);
					bis.close();
				}
			}
		} catch(Exception _ex) {
			if(_ex instanceof NullPointerException) {
				System.out.println("WBSAirback resource bundle error - unknown error");
				throw (NullPointerException) _ex;
			}
			
			System.out.println("WBSAirback resource bundle error - " + _ex.getMessage());
			if(_ex instanceof InstantiationException) {
				throw (InstantiationException) _ex;
			} else if(_ex instanceof IOException) {
				throw (IOException) _ex;
			} else if(_ex instanceof IllegalAccessException) {
				throw (IllegalAccessException) _ex;
			}
		}
		return bundle;
	}
}
