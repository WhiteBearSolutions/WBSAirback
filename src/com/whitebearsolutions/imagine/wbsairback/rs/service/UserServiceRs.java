package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.RoleManager;
import com.whitebearsolutions.imagine.wbsairback.UserManager;
import com.whitebearsolutions.imagine.wbsairback.configuration.DirectoryConfiguration;
import com.whitebearsolutions.imagine.wbsairback.rs.exception.UnauthorizedException;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.RoleRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.UserRs;

@Path("/users")
public class UserServiceRs extends WbsImagineServiceRs{
	
	private UserManager userManager = null;
	private RoleManager roleManager = null;
	
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general, gestiona sesión y autenticación
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			userManager = new UserManager();
			roleManager = new RoleManager();

			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	/**
	 * Listado de usuarios
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getUsers() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = userManager.getUsers();
			List<UserRs> users = UserRs.listMapToObject(maps, userManager);
			response.setUsers(users);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Listado de roles
	 * @return
	 */
	@Path("/roles")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getRoles() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = roleManager.getRoles();
			List<RoleRs> roles = RoleRs.listMapToObject(maps);
			response.setRoles(roles);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Cambia el password del usuario root wbsairback
	 * @param newPassword
	 * @return
	 */
	@Path("/root/password/{newPassword}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response changeRootPassword(@PathParam("newPassword") String newPassword) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if(!UserManager.getRemotePasswordAccess()) {
				UserManager.setRemotePasswordAccess(true);
			}
			
			UserManager.changeSystemUserPassword("wbsairback", newPassword);
			response.setSuccess(getLanguageMessage("system.console.update_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Obtiene un usuario
	 * @param name
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getUser(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> map = userManager.getUserAttributes(name);
			if (map == null || map.isEmpty())
				throw new Exception("system.users.exception.rs.notexists");
			
			UserRs user = UserRs.mapToObject(map, userManager.getUserRoles(name));
			response.setUser(user);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade una nueva categoría
	 * @param category
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewUser(UserRs user ) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = userManager.getUsers();
			if (maps != null && !maps.isEmpty()) {
				List<UserRs> users = UserRs.listMapToObject(maps, userManager);
				for (UserRs u : users) {
					if (u.getName().equals(user.getName()))
						throw new Exception("system.users.exception.rs.name.duplicated");
				}
			}
			
			List<String> roles = new ArrayList<String>();
			if (user.getRoles() != null && !user.getRoles().isEmpty()) {
				for (RoleRs role : user.getRoles()) {
					roles.add(role.getName());
				}
			}
			String[] categories = null;
			if (user.getCategoriesList() != null && !user.getCategoriesList().isEmpty()) {
				categories = new String[user.getCategoriesList().size()];
				categories = user.getCategoriesList().toArray(categories);
			}
			
			Map<String, String> _directory = DirectoryConfiguration.getDirectory();
			
			if(_directory.get("type").isEmpty()) {
				if(!userManager.userExists(user.getName())) {
					UserManager.addSystemUser(user.getName());
				}
				userManager.setUser(user.getName(), roles, user.getDescription(),categories);
				if(user.getPassword() != null && !user.getPassword().isEmpty()) {
					userManager.setLocalUserPassword(user.getName(), user.getPassword());
    			}
			} else {
				userManager.setUser(user.getName(), roles, user.getDescription(), categories);
			}
			
			uriCreated=user.getName();
			
		} catch (UnauthorizedException ex) {
				return Response.status(401).header("WWW-Authenticate", "BASIC realm=\"WBSAirback\"").build();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Edita una categoria existente
	 * @param category
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editUser(UserRs user ) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = userManager.getUsers();
			if (maps != null && !maps.isEmpty()) {
				List<UserRs> users = UserRs.listMapToObject(maps, userManager);
				boolean exists = false;
				for (UserRs u : users) {
					if (u.getName().equals(user.getName())) {
						exists = true;
						break;
					}
				}
				if (!exists)
					throw new Exception("system.users.exception.rs.notfound");
			} else
				throw new Exception("system.users.exception.rs.notfound");
			
			List<String> roles = new ArrayList<String>();
			if (user.getRoles() != null && !user.getRoles().isEmpty()) {
				for (RoleRs role : user.getRoles()) {
					roles.add(role.getName());
				}
			}
			
			Map<String, String> _directory = DirectoryConfiguration.getDirectory();
			String[] categories = null;
			if (user.getCategoriesList() != null && !user.getCategoriesList().isEmpty()) {
				categories = new String[user.getCategoriesList().size()];
				categories = user.getCategoriesList().toArray(categories);
			}
			
			if(_directory.get("type").isEmpty()) {
				if(!userManager.userExists(user.getName())) {
					UserManager.addSystemUser(user.getName());
				}
				userManager.setUser(user.getName(), roles, user.getDescription(), categories);
				if(user.getPassword() != null && !user.getPassword().isEmpty()) {
					userManager.setLocalUserPassword(user.getName(), user.getPassword());
    			}
			} else {
				userManager.setUser(user.getName(), roles, user.getDescription(), categories);
			}
			
			uriCreated=user.getName();
			
		} catch (UnauthorizedException ex) {
				return Response.status(401).header("WWW-Authenticate", "BASIC realm=\"WBSAirback\"").build();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un usuario
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteUser(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = userManager.getUsers();
			if (maps != null && !maps.isEmpty()) {
				List<UserRs> users = UserRs.listMapToObject(maps, userManager);
				boolean exists = false;
				for (UserRs u : users) {
					if (u.getName().equals(name)) {
						exists = true;
						break;
					}
				}
				if (!exists)
					throw new Exception("system.users.exception.rs.notfound");
			} else
				throw new Exception("system.users.exception.rs.notfound");
			
			userManager.removeLocalUser(name);
			response.setSuccess(this.getLanguageMessage("system.users.rs.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
