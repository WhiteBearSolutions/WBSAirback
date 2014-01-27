package com.whitebearsolutions.imagine.wbsairback.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.whitebearsolutions.io.FileLock;
 
public class BaculaConfiguration {
	
	public static boolean addBaculaIncludeParameter(String fileName, String resource, String name, String include) throws Exception {
        if(fileName == null || fileName.isEmpty() ||
        		resource == null || resource.isEmpty() ||
        		include == null || include.isEmpty()) {
        	throw new Exception("invalid backup include configuration parameter");
        }
        File _file = new File(fileName);
        StringBuilder _include = new StringBuilder();
        if(include.trim().startsWith("/") && include.trim().endsWith(".conf")) {
        	_include.append(include);
        } else {
        	_include.append("/etc/bacula/");
            _include.append(resource.toLowerCase());
            _include.append("/");
            _include.append(include);
            _include.append(".conf");
        }
        File _include_file = new File(_include.toString());
        if(!_file.exists()) {
            StringBuilder _sb = new StringBuilder();
            _sb.append(resource);
            _sb.append(" {\n");
            if(name != null) {
            	_sb.append("  Name = ");
                _sb.append(name);
                _sb.append("\n");
            }
            if(_include_file.exists()) {
                _sb.append("  @");
                _sb.append(_include.toString());
                _sb.append("\n");
            }
            _sb.append("}\n\n");
            store(_file, _sb.toString().getBytes());
            return false;
        } else {
        	String _line;
	        StringBuilder _sb = new StringBuilder();
	        BufferedReader _br = new BufferedReader(new FileReader(fileName));
	        try {
	        	if(!existBaculaResource(fileName, resource, name)) {
        			throw new Exception("backup resource [" + resource + "] not found in configuration");
				}
        		while((_line = _br.readLine()) != null) {
    				if(_line.contains(resource) && _line.trim().endsWith("{")) {
    					boolean _not_updated = true;
    	        		for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
                        	 if(name == null || _line.replaceAll("\\s{2,}", " ").trim().equals("Name = " + name )) {
                        		 for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
                        			if(_line.trim().startsWith("@") && _line.contains(_include.toString())) {
     				                	if(_not_updated) {
	                        				_sb.append("  @");
	     				                    _sb.append(_include.toString());
	     				                    _sb.append("\n");
     				                	}
     				                   _not_updated = false;
     					            } else if(!_line.trim().isEmpty()) {
     					            	_sb.append(_line);
     			                        _sb.append("\n");
     					            }
                        		 }
                        		 if(_not_updated) {
                        			 _sb.append("  @");
                                     _sb.append(_include.toString());
                                     _sb.append("\n");
                                     _not_updated = false;
                        		 }
                        		 if (_line != null && _line.trim().endsWith("}"))
             	        			_sb.append("}\n");
                        	 } else if(!_line.trim().isEmpty()) {
                        		 _sb.append(_line);
                        		 _sb.append("\n");
                        	 }
                        }
    	        		if (_line != null && _line.trim().endsWith("}"))
    	        			_sb.append("}\n");
    				}
    			}
        		_br.close();
        		store(_file, _sb.toString().getBytes());
        	} finally {
        		try {
        			_br.close();
        		} catch(Exception _ex) {}
        	}
			return true;
        }
	}
	
	public static void addBaculaIncludeResource(String fileName, String resource, String name) throws Exception {
		if(existBaculaInclude(fileName, "@/etc/bacula/" + resource + "/" + name + ".conf")) {
			throw new Exception(resource +" "+ name + " already exists");
		}
		
		boolean _add = false;
		StringBuilder _sb = new StringBuilder();
		BufferedReader bufferLectura = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
		String _line = "";
		
	    while((_line = bufferLectura.readLine()) != null) {
	    	if(_line.equals("## WBSAIRBACK " + resource.toUpperCase() + " ##")) {
	    		_sb.append(_line);
	    		_sb.append("\n");
	    		_sb.append("@/etc/bacula/");
	    		_sb.append(resource);
	    		_sb.append("/");
	    		_sb.append(name);
	    		_sb.append(".conf\n");
	    		_add = true;
	     	} else {
	     		_sb.append(_line);
	    		_sb.append("\n");
	     	}
	    }
	    if(!_add) {
	    	_sb.append("\n");
	    	_sb.append("## WBSAIRBACK ");
	    	_sb.append(resource.toUpperCase());
	    	_sb.append(" ##\n");
	    	_sb.append("@/etc/bacula/");
    		_sb.append(resource);
    		_sb.append("/");
    		_sb.append(name);
    		_sb.append(".conf\n");
	    }
	    bufferLectura.close();
	    store(new File(fileName), _sb.toString().getBytes());
	}
    
	public static List<String> getBaculaResources(String fileName, String resource) throws Exception {
        List<String> resources = new ArrayList<String>();
        File _file = new File(fileName); 
		if(_file.exists()) {
			FileReader _fr = new FileReader(fileName);
	        BufferedReader _buffer = new BufferedReader(_fr);
	        String _line;
	        while((_line = _buffer.readLine()) != null) {
	        	if(_line.contains(resource) && _line.trim().endsWith("{")) {
     				for(_line = _buffer.readLine(); _line != null && !_line.trim().endsWith("}"); _line = _buffer.readLine()) {
	        			if(_line.contains("Name=") || _line.contains("Name =")) {
	         				resources.add(_line.substring(_line.indexOf("=") + 1).trim());
	        			}
	        		}
	            }
	        }
	        _buffer.close();
	    }
	    return resources;
	}
	
	public static String getBaculaParameter(String fileName, String resource, String parameter) throws Exception {
		List<String> _values = getBaculaParameters(fileName, resource, parameter);
		if(_values.size() > 0) {
			return _values.get(0);
		}
		return "";
	}
    
	public static String getBaculaParameter(String fileName, String resource, String name, String parameter) throws Exception {
		List<String> _values = getBaculaParameters(fileName, resource, name, parameter);
		if(_values.size() > 0) {
			return _values.get(0);
		}
		return "";
	}
	
	public static List<String> getBaculaParameters(String fileName, String resource, String parameter) throws Exception {
		List<String> values = new ArrayList<String>();
		try {
			 BufferedReader _br = new BufferedReader(new FileReader(fileName));
	         String _line;
	         while((_line = _br.readLine()) != null) {
	         	if(_line.contains(resource) && _line.trim().endsWith("{")) {
	         		for(_line = _br.readLine(); _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
	         			if(_line.contains("  " + parameter + " =") || _line.contains("  " + parameter + "=")) {
	         				_line = _line.substring(_line.indexOf("=") + 1).trim();
     						if(_line.startsWith("\"") && _line.endsWith("\"")) {
	         					_line = _line.substring(1, _line.length() - 1);
	         				} else if(_line.contains("=")) {
     							_line = _line.substring(0, _line.indexOf("=")).trim();
     						}
     						values.add(_line);
	         			}
	         		}
	             }
	         }
	         _br.close();
	       	 return values;
         } catch(java.io.FileNotFoundException _ex) {
        	 return values;
         }
	}
	
	public static List<String> getBaculaParameters(String fileName, String resource, String name, String parameter) throws Exception {
		List<String> values = new ArrayList<String>();
		try {
			BufferedReader _br = new BufferedReader(new FileReader(fileName));
	         String _line;
	         while((_line = _br.readLine()) != null) {
	         	if(_line.contains(resource) && _line.trim().endsWith("{")) {
	         		for(_line = _br.readLine(); _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
	         			if ( _line.contains("Name=") || _line.contains("Name =") ) {
	         				if (_line.trim().substring(_line.indexOf("=")).equals(name)) {
		         				for(_line = _br.readLine(); _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
		         					if(_line.contains("  " + parameter + " =") || _line.contains("  " + parameter + "=")) {
		    	         				_line = _line.substring(_line.indexOf("=") + 1).trim();
		        						if(_line.startsWith("\"") && _line.endsWith("\"")) {
		    	         					_line = _line.substring(1, _line.length() - 1);
		    	         				} else if(_line.contains("=")) {
		         							_line = _line.substring(0, _line.indexOf("=")).trim();
		         						}
		        						values.add(_line);
		    	         			}
		         				}
	         				}
	         			}
	         		}
	             }
	         }
	         _br.close();
	         return values;
         } catch(java.io.FileNotFoundException _ex) {
        	 return values;
         }
	}
	
    public static boolean setBaculaParameter(String fileName, String resource, String name, String parameter, String [] values) throws Exception {
    	if(fileName == null || fileName.isEmpty() ||
        		resource == null || resource.isEmpty() ||
        		parameter == null || parameter.isEmpty()) {
        	throw new Exception("invalid backup configuration parameter");
        }
    	File _file = new File(fileName);
        if(!_file.exists()) {
            StringBuilder _sb = new StringBuilder();
            _sb.append(resource);
            _sb.append(" {\n");
            if(name != null) {
            	_sb.append("  Name = ");
                _sb.append(name);
                _sb.append("\n");
            }
            for(String value : values) {
                _sb.append("  ");
                _sb.append(parameter);
                _sb.append(" = ");
                _sb.append(value);
                _sb.append("\n");
            }
            _sb.append("}\n");
            if (!resource.equals("Schedule")) _sb.append("\n");
            store(_file, _sb.toString().getBytes());
            return false;
        } else {
        	String _line;
	        StringBuilder _sb = new StringBuilder();
	        BufferedReader _br = new BufferedReader(new FileReader(fileName));
	        try {
	        	if(!existBaculaResource(fileName, resource, name)) {
	        		while((_line = _br.readLine()) != null) {
	        			_sb.append(_line);
	        			_sb.append("\n");
	        		}
	        		_sb.append(resource);
	                _sb.append(" {\n");
	                if(name != null) {
	                	_sb.append("  Name = ");
	                    _sb.append(name);
	                    _sb.append("\n");
	                }
	                for(String value : values) {
	                    _sb.append("  ");
	                    _sb.append(parameter);
	                    _sb.append(" = ");
	                    _sb.append(value);
	                    _sb.append("\n");
	                }
	                _sb.append("}\n\n");
				} else {
					while((_line = _br.readLine()) != null) {
	    				if(_line.contains(resource) && _line.trim().endsWith("{")) {
	    					boolean _not_updated = true, _close_bracket = false;
	    		        	for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
	    		        		_close_bracket = false;
	    		        		if(name == null || _line.replaceAll("\\s{2,}", " ").trim().equals("Name = " + name )) {
	                        		for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
	                        			if(_line.replaceAll("\\s{2,}", " ").trim().startsWith(parameter + " =") || _line.trim().startsWith(parameter + "=")) {
	                        				 for(int i = 0; (i < values.length) && _not_updated; i++) {
	                                         	_sb.append("  ");
	                                         	_sb.append(parameter);
	                                         	_sb.append(" = ");
	                                         	_sb.append(values[i]);
	                                         	_sb.append("\n");
	                                         }
	                        				 _not_updated = false;
	                        			} else if(!_line.trim().isEmpty()) {
	                        				 _sb.append(_line);
	    	                        		 _sb.append("\n");
	                        			}
	                        		}
	                        		if(_not_updated) {
	                        			 for(int i = 0; (i < values.length) && _not_updated; i++) {
	                                      	_sb.append("  ");
	                                      	_sb.append(parameter);
	                                      	_sb.append(" = ");
	                                      	_sb.append(values[i]);
	                                      	_sb.append("\n");
	                                     }
	                        			 _not_updated = false;
	                        		}
	                        		_sb.append("}\n");
	                        		_close_bracket = true;
	                        	} else if(!_line.trim().isEmpty()) {
	                        		_sb.append(_line);
	                        		_sb.append("\n");
	                        	}
	                        }
	    		        	if(!_close_bracket) {
	    		        		_sb.append("}\n");
	    		        	}
	    				} else if(!_line.trim().isEmpty()) {
	    					if(_line.startsWith("##")) {
	    						_sb.append("\n");
	    					}
	    					_sb.append(_line);
                    		_sb.append("\n");
	    				}
	    			}
				}
	        	_br.close();
        		store(_file, _sb.toString().getBytes());
        	} finally {
        		try {
        			_br.close();
        		} catch(Exception _ex) {}
        	}
			return true;
        }
	}
	
    public static void deleteBaculaParameter(String fileName, String resource, String name, String parameter) throws Exception {
		if(fileName == null || fileName.isEmpty() ||
        		resource == null || resource.isEmpty() ||
        		parameter == null || parameter.isEmpty()) {
        	throw new Exception("invalid backup configuration parameter");
        }
		
		String _line;
        StringBuilder _sb = new StringBuilder();
        File _file = new File(fileName);
    	if(!_file.exists()){
			throw new Exception("backup configuration file does not exists");
		}
		if(!existBaculaResource(fileName, resource, name)){
			throw new Exception("backup configuration resource does not exists");
		}
		
		BufferedReader _br = new BufferedReader(new FileReader(fileName));
        try {
        	if(!existBaculaResource(fileName, resource, name)) {
    			throw new Exception("backup resource [" + resource + "] not found in configuration");
			}
        	while((_line = _br.readLine()) != null) {
				if(_line.contains(resource) && _line.trim().endsWith("{")) {
                    for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
                    	 if(name == null || _line.replaceAll("\\s{2,}", " ").trim().equals("Name = " + name )) {
                    		 for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
                    			 if(!_line.replaceAll("\\s{2,}", " ").trim().startsWith(parameter + " =") || _line.trim().startsWith(parameter + "=")) {
                    				 _sb.append(_line);
	                        		 _sb.append("\n");
                    			 }
                    		 }
                    		 if (_line != null && _line.trim().endsWith("}"))
                             	_sb.append("}\n");
                    	 } else {
                    		 _sb.append(_line);
                    		 _sb.append("\n");
                    	 }
                    }
                    if (_line != null && _line.trim().endsWith("}"))
                    	_sb.append("}\n");
				} else {
	     			_sb.append(_line + "\n");
	             }
			}
    		_br.close();
    		store(_file, _sb.toString().getBytes());
    	} finally {
    		try {
    			_br.close();
    		} catch(Exception _ex) {}
    	}
	}
	
	public static String deleteBaculaResource(String fileName, String resource, String name) throws Exception {
		String nombre = "";
		StringBuilder _sb = new StringBuilder();
	     BufferedReader bufferLectura = new BufferedReader(new FileReader(fileName));
         String lineaDatos;
         while((lineaDatos = bufferLectura.readLine()) != null) {
         	if(lineaDatos.contains(resource) && lineaDatos.contains("{")) {
         		String lineaDatosTmp = lineaDatos;
         		lineaDatos = bufferLectura.readLine();
         		lineaDatosTmp = lineaDatosTmp + "\n";
         		while(!lineaDatos.contains("}")) {
         			if(lineaDatos.replaceAll("\\s{2,}", " ").trim().equals("Name = " + name)) {
         				lineaDatos = bufferLectura.readLine();
    	         		while(!lineaDatos.contains("}")) {
    	         			lineaDatos = bufferLectura.readLine();
    	         		}
         			} else {
       	         	    lineaDatos = lineaDatosTmp + lineaDatos;
       	         	    lineaDatosTmp = "";
       	         		_sb.append(lineaDatos + "\n");
       	         		lineaDatos = bufferLectura.readLine();
       	         		if( lineaDatos.contains("}")) {
       	         			_sb.append(lineaDatos + "\n");
       	         		}
       	         	}
         		}
             } else {
     			_sb.append(lineaDatos + "\n");
             }
         }
         bufferLectura.close();
         
        store(new File(fileName), _sb.toString().getBytes());
	    return nombre.toString();
	}
	
	public static void deleteBaculaIncludeParameter(String fileName, String resource, String name, String include) throws Exception {
		if(fileName == null || fileName.isEmpty() ||
        		resource == null || resource.isEmpty() ||
        		include == null || include.isEmpty()) {
        	return;
        }
        File _file = new File(fileName);
        StringBuilder _include = new StringBuilder();
        if(include.trim().startsWith("/") && include.trim().endsWith(".conf")) {
        	_include.append(include);
        } else {
        	_include.append("/etc/bacula/");
            _include.append(resource.toLowerCase());
            _include.append("/");
            _include.append(include);
            _include.append(".conf");
        }
        File _include_file = new File(_include.toString());
        if(_file.exists()) {
            String _line;
	        StringBuilder _sb = new StringBuilder();
	        BufferedReader _br = new BufferedReader(new FileReader(fileName));
	        try {
        		if(existBaculaInclude(fileName, include)) {
        			while((_line = _br.readLine()) != null) {
        				if(_line.contains(resource) && _line.trim().endsWith("{")) {
                            for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
                            	 if(name == null || _line.replaceAll("\\s{2,}", " ").trim().equals("Name = " + name )) {
                            		 for(; _line != null && !_line.trim().endsWith("}"); _line = _br.readLine()) {
                            			 if(_line.trim().startsWith("@") && _line.contains(_include.toString())) {
         				                	continue;
         					            } else {
         					            	_sb.append(_line);
         			                        _sb.append("\n");
         					            }
                            		 }
                            		 if (_line != null &&_line.trim().endsWith("}")) {
                                     	_sb.append("}");
                                     	_sb.append("\n");
                                     }
                            	 } else if(!_line.trim().isEmpty()) {
         			            	_sb.append(_line);
        	                        _sb.append("\n");
                            	 }
                            }
                            if (_line != null && _line.trim().endsWith("}")) {
                            	_sb.append("}");
                            	_sb.append("\n");
                            }
            			} else if(!_line.trim().isEmpty()) {
 			            	_sb.append(_line);
	                        _sb.append("\n");
                    	}
				    }
        			
			        if(_include_file.exists()) {
			        	_include_file.delete();
			        }
			        _br.close();
	        		store(_file, _sb.toString().getBytes());
				}
        	} finally {
        		try {
        			_br.close();
        		} catch(Exception _ex) {}
        	}
        }
	}
	
	public static void deleteBaculaIncludeResource(String fileName, String resource, String name) throws Exception {
		String _line;
		StringBuilder _sb = new StringBuilder();
		BufferedReader _br = new BufferedReader(new FileReader(fileName));
		try {
		    while((_line = _br.readLine()) != null) {
		    	if(!_line.contains("@/etc/bacula/" + resource + "/" + name + ".conf")) {
		    		_sb.append(_line);
	                _sb.append("\n");
		    	}
		    }
		} finally {
			_br.close();
		}
	    
	    File file = new File(fileName);
	    store(file, _sb.toString().getBytes());
	    
	    file = new File("/etc/bacula/" + resource + "/" + name +".conf");
		if(file.exists()) {
	        file.delete();
		}
		
	    file = new File("/etc/bacula/" + resource + "/" + name +".xml");
		if(file.exists()) {
	        file.delete();
		}
	}
	
	public static boolean existBaculaInclude(String fileName, String include) throws Exception {
		String _line;
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
		try {
			while((_line = _br.readLine()) != null) {
	        	if(_line.trim().startsWith("@") && _line.contains(include)) {
	        		return true;
	            }
	        }
		} finally {
			_br.close();
		}
	    return false;
	}
	
	public static List<String> getBaculaIncludes(String fileName) throws Exception {
		List<String> _includes = new ArrayList<String>();
		String _line;
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
		try {
			while((_line = _br.readLine()) != null) {
	        	if(_line.trim().startsWith("@")) {
	        		_includes.add(_line.trim().substring(1, _line.trim().length()));
	            }
	        }
		} finally {
			_br.close();
		}
	    return _includes;
	}
	
	private static boolean existBaculaResource(String fileName, String resource, String name) throws Exception {
		String _line = null;
		BufferedReader _br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
		try {
			while((_line = _br.readLine()) != null) {
	        	if(_line.contains(resource) && _line.trim().endsWith("{")) {
	        		while(_line != null && !_line.trim().endsWith("}")) {
	        			_line = _br.readLine();
	        			if (_line != null && !_line.isEmpty()) {
	        				if(name == null || _line.replaceAll("\\s{2,}", " ").trim().equals("Name = " + name )) {
	        					return true;
	        				}
	        			}
	        		}
	            }
	        }
		} finally {
			_br.close();
		}
	    return false;
	}
	
	public static void trimBaculaFile(String fileName) throws Exception {
		String prev = "";
		StringBuilder _sb = new StringBuilder();
		BufferedReader bufferLectura = new BufferedReader(new FileReader(fileName));
		String lineaDatos;
		while((lineaDatos = bufferLectura.readLine()) != null) {
			if(!prev.equals(lineaDatos)) {
				_sb.append(lineaDatos + "\n");
				prev = lineaDatos;
			}
		}
		bufferLectura.close();
		store(new File(fileName), _sb.toString().getBytes());
	}
	
	private static void store(File file, byte[] data) throws Exception {
		FileLock _fl = new FileLock(file);
		FileOutputStream _fos = new FileOutputStream(file);
        try {
        	_fl.lock();
			_fos.write(data);
        } finally {
        	try {
        		_fl.unlock();
        	} catch(Exception _ex) {}
        	try {
        		_fos.close();
        	} catch(Exception _ex) {}
        }
	}
}
