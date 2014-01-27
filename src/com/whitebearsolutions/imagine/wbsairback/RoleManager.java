package com.whitebearsolutions.imagine.wbsairback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whitebearsolutions.imagine.wbsairback.configuration.WBSAirbackConfiguration;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.xml.db.XMLAttribute;
import com.whitebearsolutions.xml.db.XMLDB;
import com.whitebearsolutions.xml.db.XMLObject;

public class RoleManager {
	private XMLDB _db;
	private Map<String, Map<String, String>> roles;
	
	public static final String roleAdmin = "Administrator";
	public static final String roleGlobalOperator = "Global_Operator";
	public static final String roleOperator = "Operator";
	public static final String roleCoordinator = "Backup_Coordinator";
	public static final String roleUser = "User";
	public static final String roleAdInventoryAppSo = "Admin_Inventory_Apps";
	public static final String roleAdInventoryRemote = "Admin_Inventory_Remote_Storage";
	public static final String roleAdAdvancedStorage = "Admin_Advanced_Storage";
	public static final String roleAdCoordinator = "Advanced_Coordinator";
	public static final String roleStorageManager = "Storage_Manager";
	public static final String pathRoles = WBSAirbackConfiguration.getDirectoryRoles()+"/"+WBSAirbackConfiguration.getFileRoles();
	
	
	public RoleManager() throws Exception {
		File roles_dir = new File(WBSAirbackConfiguration.getDirectoryRoles());
		if(!roles_dir.exists()) {
			roles_dir.mkdirs();
		}
		moveOldPath();
		
		File file = new File(pathRoles);
		this._db = new XMLDB(file);
		List<XMLObject> objects = new ArrayList<XMLObject>();
			
		XMLObject xmlrole = this._db.createXMLObject();
		XMLAttribute _a = new XMLAttribute("name");
		_a.setValue(roleAdmin);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Administrador del Sistema de Backup");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);

		/*
		 * TODO: uncomment with bacula
		 * xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleGlobalOperator);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Operador Global del Sistema de Backup");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);
		
		xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleOperator);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Operador del Sistema de Backup");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);*/
		
		xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleUser);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Usuario del Sistema de Backup");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);
		
		/*
		 * TODO: uncomment with bacula
		 * xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleAdCoordinator);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Backup Avanzado: Coordinador de pasos y plantillas");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);

		xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleCoordinator);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Coordinador de backup");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);
		
		xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleAdInventoryAppSo);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Backup Avanzado: Administrador de aplicaciones y sistemas de Inventario");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);
		
		xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleAdInventoryRemote);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Backup Avanzado: Administrador de sistemas de almacenamiento remoto");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);
			
		xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleAdAdvancedStorage);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Backup Avanzado: Administrador de tipos de sistemas de almacenamiento avanzado");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);
		
		xmlrole = this._db.createXMLObject();
		_a = new XMLAttribute("name");
		_a.setValue(roleStorageManager);
		xmlrole.addAttribute(_a);
		_a = new XMLAttribute("description");
		_a.setValue("Gestor de almacenamiento");
		xmlrole.addAttribute(_a);
		objects.add(xmlrole);*/
		
		this._db.setObjects(objects);
		this._db.store();
		
		this.roles = new HashMap<String, Map<String, String>>();
		for(XMLObject o: this._db.getObjects()) {
			Map<String, String> role = new HashMap<String, String>();
			String name = null;
			for(XMLAttribute a : o.getAttributes()) {
				if("name".equals(a.getName())) {
					name = a.getValue();
				}
				role.put(a.getName(), a.getValue());
			}
			if(name != null) { 
				this.roles.put(name, role);
			}
		}
	}
	
	public void removeUser(String name) throws Exception {
		this.roles.remove(name);
		store();
	}
	
	public List<String> getRoleNames() {
		return new ArrayList<String>(this.roles.keySet());
	}
	
	public List<Map<String, String>> getRoles() {
		return new ArrayList<Map<String, String>>(this.roles.values());
	}
	
	public Map<String, String> getRole(String name) {
		if(this.roles.containsKey(name)) {
			return this.roles.get(name);
		}
		return null;
	}
	
	public void setRole(String name, String description) throws Exception {
		Map<String, String> role = this.roles.get(name);
		if(role == null) {
			role = new HashMap<String, String>();
			role.put("name", name);
		}
		role.put("roles", description);
		this.roles.put(name, role);
		store();
	}
	
	private XMLObject getRoleXMLObject(String name) {
		for(XMLObject role : this._db.getObjects()) {
			if(role.hasAttribute("name") && name.equals(role.getAttribute("name").getValue())) {
				return role;
			}
		}
		return null;
	}
	
	private void store() throws Exception {
		List<XMLObject> objects = new ArrayList<XMLObject>();
		for(Map<String, String> role : this.roles.values()) {
			if(role.containsKey("name")) {
				XMLObject xmlrole = getRoleXMLObject(role.get("name"));
				if(xmlrole == null) {
					xmlrole = this._db.createXMLObject();
					xmlrole.addAttribute(new XMLAttribute("name", role.get("name")));
				}				
				xmlrole.addAttribute(new XMLAttribute("description", role.get("description")));
				objects.add(xmlrole);
			}
		}		
		this._db.setObjects(objects);
		this._db.store();
	}
	
	private void moveOldPath() {
		try {
		if (!new File(pathRoles).exists()) {
			if (new File(WBSAirbackConfiguration.getFileRoles()).exists()) {
				Command.systemCommand("mv "+WBSAirbackConfiguration.getFileRoles()+" "+pathRoles);
			} else if (new File("/"+WBSAirbackConfiguration.getFileRoles()).exists()) {
				Command.systemCommand("mv /"+WBSAirbackConfiguration.getFileRoles()+" "+pathRoles);
			}
		}
		} catch (Exception ex) {}
	}

}
