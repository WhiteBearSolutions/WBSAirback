package com.whitebearsolutions.imagine.wbsairback.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Configuration;
import com.whitebearsolutions.xml.db.XMLAttribute;
import com.whitebearsolutions.xml.db.XMLDB;
import com.whitebearsolutions.xml.db.XMLObject;

public class CategoryManager {
	private XMLDB _db;
	private TreeMap<String, Map<String, String>> categories;

	public CategoryManager() throws Exception {
		if(!new File(WBSAirbackConfiguration.getDirectoryCategories()).exists()) {
			new File(WBSAirbackConfiguration.getDirectoryCategories()).mkdir();
		}
		
		this._db = new XMLDB(new File(WBSAirbackConfiguration.getFileCategories()));
		this.categories = new TreeMap<String, Map<String,String>>();
		
		for(XMLObject o : this._db.getObjects()) {
			if(o.hasAttribute("name")) {
				Map<String, String> category = new HashMap<String, String>();
				category.put("name", o.getAttribute("name").getValue());
				if(o.hasAttribute("description")) {
					category.put("description", o.getAttribute("description").getValue());
				}
				if(o.hasAttribute("mail")) {
					category.put("mail", o.getAttribute("mail").getValue());
				}
				this.categories.put(category.get("name"), category);
			}
		}
	}
	
	public boolean categoryNameExists(String name) {
		if(this.categories.containsKey(name)) {
			return true;
		}
		return false;
	}
	
	public List<String> getCategoryNames() {
		return new ArrayList<String>(this.categories.keySet());
	}
	
	public List<Map<String, String>> getCategories() {
		return new ArrayList<Map<String, String>>(this.categories.values());
	}
	
	public Map<String, String> getCategory(String name) {
		return this.categories.get(name);
	}
	
	public void setCategory(String name, String description, String mail) throws Exception {
		Map<String, String> category = this.categories.get(name);
		if(category == null) {
			category = new HashMap<String, String>();
			category.put("name", name);
		}
		if(description != null && !description.isEmpty()) {
			category.put("description", description);
		}
		if(mail != null && !mail.isEmpty()) {
			category.put("mail", mail);
		}
		
		this.categories.put(name, category);
		store();
	}
	
	public void removeCategory(String name) throws Exception {
		ClientManager _clm = new ClientManager(new Configuration(new File(WBSAirbackConfiguration.getFileConfiguration())));
		List<String> listCat = new ArrayList<String>();
		listCat.add(name);
		List<Map<String, String>> _clients = _clm.getAllClients(null, listCat, false);
		if(!_clients.isEmpty()) {
			throw new Exception("category still associated with the client [" + _clients.get(0).get("name") + "]");
		}
		
		this.categories.remove(name);
		store();
	}
	
	private XMLObject getCategoryXMLObject(String name) {
		for(XMLObject category : this._db.getObjects()) {
			if(category.hasAttribute("name") && name.equals(category.getAttribute("name").getValue())) {
				return category;
			}
		}
		return null;
	}
	
	private void store() throws Exception {
		List<XMLObject> objects = new ArrayList<XMLObject>();
		for(Map<String, String> category : this.categories.values()) {
			if(category.containsKey("name")) {
				XMLObject xmlcategory = getCategoryXMLObject(category.get("name"));
				if(xmlcategory == null) {
					xmlcategory = this._db.createXMLObject();
				}
				for(String attribute : category.keySet()) {
					XMLAttribute _a = new XMLAttribute(attribute);
					_a.setValue(category.get(attribute));
					xmlcategory.addAttribute(_a);
				}
				objects.add(xmlcategory);
			}
		}		
		this._db.setObjects(objects);
		this._db.store();
	}
}