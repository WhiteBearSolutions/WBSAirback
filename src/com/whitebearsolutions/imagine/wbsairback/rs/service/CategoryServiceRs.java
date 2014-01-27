package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
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

import com.whitebearsolutions.imagine.wbsairback.backup.CategoryManager;
import com.whitebearsolutions.imagine.wbsairback.rs.exception.UnauthorizedException;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.CategoryRs;

@Path("/categories")
public class CategoryServiceRs extends WbsImagineServiceRs{
	
	private CategoryManager categoryManager = null;
	
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general, gestiona sesión y autenticación
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			categoryManager = new CategoryManager();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	/**
	 * Listado de categorias
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getCategories() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = categoryManager.getCategories();
			List<CategoryRs> categories = CategoryRs.listMapToObject(maps);
			response.setCategories(categories);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un cliente
	 * @param clientId
	 * @return
	 */
	@Path("{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getCategory(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> map = categoryManager.getCategory(name);
			if (map == null || map.isEmpty())
				throw new Exception("backup.categories.rs.notexists");
			
			CategoryRs category = CategoryRs.mapToObject(map);
			response.setCategory(category);
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
	public Response addNewCategory(CategoryRs category ) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> maps = categoryManager.getCategories();
			List<CategoryRs> categories = CategoryRs.listMapToObject(maps);
			for (CategoryRs cat : categories) {
				if (cat.getName().equals(category.getName()))
					throw new Exception("backup.categories.rs.name.duplicated");
			}
			
			categoryManager.setCategory(category.getName(), category.getDescription(), category.getEmail());
			
			uriCreated=category.getName();
			
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
	public Response editCategory(CategoryRs category ) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			boolean exists = false;
			List<Map<String, String>> maps = categoryManager.getCategories();
			List<CategoryRs> categories = CategoryRs.listMapToObject(maps);
			for (CategoryRs cat : categories) {
				if (cat.getName().equals(category.getName())) {
					exists = true;
					break;
				}
			}
			
			if (!exists)
				throw new Exception("backup.categories.rs.notexists");
			
			categoryManager.setCategory(category.getName(), category.getDescription(), category.getEmail());
			
			uriCreated=category.getName();
			
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
	 * Elimina una categoria
	 * @return
	 */
	@Path("{name}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteCategory(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			boolean exists = false;
			List<Map<String, String>> maps = categoryManager.getCategories();
			List<CategoryRs> categories = CategoryRs.listMapToObject(maps);
			for (CategoryRs cat : categories) {
				if (cat.getName().equals(name)) {
					exists = true;
					break;
				}
			}
			
			if (!exists)
				throw new Exception("backup.categories.rs.notexists");
			
			categoryManager.removeCategory(name);
			
			response.setSuccess(this.getLanguageMessage("backup.categories.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
