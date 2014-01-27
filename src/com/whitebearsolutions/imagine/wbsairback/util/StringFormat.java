package com.whitebearsolutions.imagine.wbsairback.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StringFormat {
	public static String getTwoCharTimeComponent(int value) {
		StringBuilder _sb = new StringBuilder();
		value = Math.abs(value);
		
		if(value < 10) {
			_sb.append("0");
		}
		_sb.append(value);
		return _sb.toString();
	}
	
	public static String fileToString(String file) {
        String result = null;
        DataInputStream in = null;

        try {
            File f = new File(file);
            byte[] buffer = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            in.readFully(buffer);
            result = new String(buffer);
        } catch (IOException e) {
            throw new RuntimeException("IO problem in fileToString", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) { /* ignore it */
            }
        }
        return result;
    }
	

   public static List<String> getList(String text) throws Exception {
        List<String> _list = new ArrayList<String>();
        if(text == null) {
            return _list;
        } else if(text.contains(",")) {
            for(String _token : text.split(",")) {
                _token = _token.trim();
                if(!_token.isEmpty()) {
                    _list.add(_token);
                }
            }
        } else if(text.contains(" ")) {
            for(String _token : text.split("\\ ")) {
                _token = _token.trim();
                if(!_token.isEmpty()) {
                    _list.add(_token);
                }
            }
        } else if(!text.isEmpty()) {
            _list.add(text);
        }
        return _list;
    }
}
