package com.whitebearsolutions.imagine.wbsairback;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;

public class NetworkComparator implements Comparator<Object> {
	
    public int compare(Object value1, Object value2) {
    	if(value1 instanceof String && value2 instanceof String) {
    		String _key1 = String.valueOf(value1).trim();
    		String _key2 = String.valueOf(value2).trim();
    		if(_key1.startsWith("bond") && !_key2.startsWith("bond")) {
    			return 1;
    		} else if(_key2.startsWith("bond") && !_key1.startsWith("bond")) {
    			return -1;
    		} else if(_key1.startsWith("eth") && !_key2.startsWith("eth")) {
    			return 1;
    		} else if(_key2.startsWith("eth") && !_key1.startsWith("eth")) {
    			return -1;
    		}
    		return _key1.compareTo(_key2);
    	} else {
    		Collator collator = Collator.getInstance();
	    	CollationKey key1 = collator.getCollationKey(value1.toString());
	    	CollationKey key2 = collator.getCollationKey(value2.toString());
	    	return key1.compareTo(key2);
    	}
    }
}
