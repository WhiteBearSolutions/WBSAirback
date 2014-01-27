package com.whitebearsolutions.imagine.wbsairback.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

public class XMLResourceBundle extends ResourceBundle {
	private Properties _properties;
	
    public XMLResourceBundle(InputStream stream) throws IOException {
        this._properties = new Properties();
        this._properties.loadFromXML(stream);
    }
    
    @Override
	public Enumeration<String> getKeys() {
		Vector<String> _keys = new Vector<String>();
		for(Object p : this._properties.keySet()) {
			_keys.add(String.valueOf(p));
		}
		return _keys.elements();
	}

	@Override
	protected Object handleGetObject(String key) {
		return this._properties.getProperty(key);
	}

}
