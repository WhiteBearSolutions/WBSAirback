package com.whitebearsolutions.imagine.wbsairback.net;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;

import com.whitebearsolutions.util.Command;

public class Bonjour {
	
	public static HashMap<String, String> getProducts(String name) throws Exception {
		if(name == null) {
			return new HashMap<String, String>();
		}
		return findProduct(Command.systemCommand("/usr/bin/avahi-browse -trl _airback-dir._tcp | grep = || echo \"\""), name);
        
	}
	
	private static HashMap<String, String> findProduct(String output, String name) throws Exception {
	    HashMap<String, String> _products = new HashMap<String, String>();
	    if(output == null || output.indexOf(name) == -1) {
	    	return _products;
	    }
	    BufferedReader _reader = new BufferedReader(new StringReader(output));
	    String line;
	    while(_reader.ready() && (line = _reader.readLine()) != null) {
	    	if(line.startsWith("=") && line.indexOf("IPv4") != -1 && line.indexOf(name) != -1) {
			    String[] product = new String[2];
			    product[0] = line.substring(line.indexOf("IPv4") + 8, line.indexOf("        ")!=-1?line.indexOf("        "):line.length());
			    while(_reader.ready() && (line = _reader.readLine()) != null) {
			    	if (line.indexOf("address") != -1) {
			    		product[1] = line.substring(line.indexOf("=") + 1).trim();
			    		product[1] = product[1].replace("[", "");
			    		product[1] = product[1].replace("]", "");
			    		break;
			    	}
			    }
	            if(!_products.containsKey(product[1]) || !_products.containsValue(product[0])) {
	              _products.put(product[1],product[0]); 
	            }
	    	}
	    }
        return _products;
	}
}
