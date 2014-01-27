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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.backup.CategoryManager;
import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.rs.exception.UnauthorizedException;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ClientRs;

@Path("/clients")
public class ClientServiceRs extends WbsImagineServiceRs{
	
	private CategoryManager categoryManager = null;
	private ClientManager clientManager = null;
	
	
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
			clientManager = new ClientManager(this.config);
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Checkeos comunes antes de guardar un cliente
	 * @param client
	 * @throws Exception
	 */
	private void commonSaveClientCheckings(ClientRs client) throws Exception {
		
		if(!NetworkManager.isValidAddress(client.getAddress())) {
			if (client.getDnsname() != null && !client.getDnsname().equals(""))
				client.setAddress(client.getDnsname());
			else
				throw new Exception(getLanguageMessage("common.mesage.invalid.ipaddress"));
		}
		
		boolean categoryFound = false;
		if (client.getCategories() != null && !client.getCategories().isEmpty()) {
			for(String cat : categoryManager.getCategoryNames()) {
				if (client.getCategories().contains(cat)) {
					categoryFound = true;
					continue;
				}
			}
		}
		else
			categoryFound = true;
		if (!categoryFound)
			throw new Exception(getLanguageMessage("backup.client.rs.category.not.found"));
		
		boolean soSupported = false;
		for (String _osKey : ClientManager.getClientSupportedSOs().keySet()) {
			if (_osKey.equals(client.getOs())) {
				soSupported = true;
				continue;
			}
		}
		if (!soSupported)
			throw new Exception(getLanguageMessage("backup.client.rs.so.not.supported"));
		
		if (client.getJobretentionPeriod() != null && client.getJobretention() != null) {
			if (client.getJobretentionPeriod().equals("days") && client.getJobretentionPeriod().equals("months") && client.getJobretentionPeriod().equals("years") && client.getJobretentionPeriod().equals("weeks"))
				throw new Exception (getLanguageMessage("backup.client.rs.invalid.jobretentionparams"));
		} else {
			client.setJobretention(3);
			client.setJobretentionPeriod("months");
		}
		
		if (client.getFileretentionPeriod() != null && client.getFileretention() != null) {
			if (client.getFileretentionPeriod().equals("days") && client.getFileretentionPeriod().equals("months") && client.getFileretentionPeriod().equals("years") && client.getFileretentionPeriod().equals("weeks"))
				throw new Exception (getLanguageMessage("backup.client.rs.invalid.fileretentionparams"));
		} else {
			client.setFileretention(3);
			client.setFileretentionPeriod("months");
		}
		
		if (client.getPort() == null)
			client.setPort(9102);
		
		if (client.getCharset() != null) {
			if (!client.getCharset().equals("UTF-8") && !client.getCharset().equals("ISO-8859-1") && !client.getCharset().equals("ISO-8859-15"))
				throw new Exception (getLanguageMessage("backup.client.rs.invalid.charset"));
		}
		else
			client.setCharset("UTF-8");
	}
	
	
	/**
	 * Listado de clientes
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getClients() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> mapClients = clientManager.getAllClients(null, null, true);
			List<ClientRs> clients = new ArrayList<ClientRs>();
			if (mapClients != null && mapClients.size() > 0)
				clients = ClientRs.listMapToObject(mapClients);
			response.setClients(clients);
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
	@Path("{clientId}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getClient(@PathParam("clientId") Integer clientId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			clientManager.getClientName(clientId);
			Map<String, String> mapClient = clientManager.getCompleteClient(clientId);
			ClientRs client = ClientRs.mapToObject(mapClient);
			response.setClient(client);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene un cliente en base a su nombre
	 * @param clientName
	 * @return
	 */
	@Path("/find/name/{clientName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getClientByName(@PathParam("clientName") String clientName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Integer id = clientManager.getClientId(clientName);
			Map<String, String> mapClient = clientManager.getCompleteClient(id);
			ClientRs client = ClientRs.mapToObject(mapClient);
			response.setClient(client);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un nuevo cliente, si recibe usuario y password, puede efectuar el despliegue automático
	 * @param client
	 * @param depUsername
	 * @param depPassword
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewClient(ClientRs client, @QueryParam("deployUsername") String depUsername, @QueryParam("deployPassword") String depPassword ) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(client.getCategoriesList());
			if (rError != null)
				return rError;
			this.commonSaveClientCheckings(client);
			if (depUsername != null && !depUsername.equals("")) { 
				if (depPassword != null  && !depPassword.equals("")) {
					clientManager.deployClient(client.getAddress(), client.getName(), client.getPassword(), depUsername, depPassword, client.getOs());
				} else {
					throw new Exception(getLanguageMessage("common.message.no_deploy_params"));
				}
			}
	        
			String[] categories = null;
			if (client.getCategoriesList() != null && !client.getCategoriesList().isEmpty()) {
				categories = new String[client.getCategoriesList().size()];
				categories = client.getCategoriesList().toArray(categories);
			}
			
			clientManager.setClient(client.getName(), client.getAddress(), client.getPort(), client.getOs(), client.getPassword(), client.getCharset(), client.getFileretention(), client.getFileretentionPeriod(), client.getJobretention(), client.getFileretentionPeriod(), categories);
			Map<String, String> mapNewClient = clientManager.getClient(client.getName());
			uriCreated=mapNewClient.get("name");
			
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
	 * Edita cliente, si recibe usuario y password, puede efectuar el despliegue automático
	 * @param client
	 * @param depUsername
	 * @param depPassword
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editClient(ClientRs client, @QueryParam("deployUsername") String depUsername, @QueryParam("deployPassword") String depPassword ) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(client.getCategoriesList());
			if (rError != null)
				return rError;
			
			// Bucamos el cliente, sino se encuentra lanza la excepcion el propio metodo
			if (client.getId() == null || client.getId() < 0)
				throw new Exception("backup.client.invalid.clienid");
			
			clientManager.getClientName(client.getId());
			this.commonSaveClientCheckings(client);
			
			// Deploy si procede
			if (depUsername != null && !depUsername.equals("")) { 
				if (depPassword != null  && !depPassword.equals("")) {
					clientManager.deployClient(client.getAddress(), client.getName(), client.getPassword(), depUsername, depPassword, client.getOs());
				} else {
					throw new Exception(getLanguageMessage("common.message.no_deploy_params"));
				}
			}
			
			String[] categories = null;
			if (client.getCategoriesList() != null && !client.getCategoriesList().isEmpty()) {
				categories = new String[client.getCategoriesList().size()];
				categories = client.getCategoriesList().toArray(categories);
			}
	        
			// Seteamos y devolvemos
			clientManager.setClient(client.getName(), client.getAddress(), client.getPort(), client.getOs(), client.getPassword(), client.getCharset(), client.getFileretention(), client.getFileretentionPeriod(), client.getJobretention(), client.getFileretentionPeriod(), categories);
			Map<String, String> mapNewClient = clientManager.getClient(client.getName());
			uriCreated=mapNewClient.get("name");
			
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
	 * Elimina un cliente
	 * @return
	 */
	@Path("{clientid}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteClient(@PathParam("clientid") Integer clientId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			// Bucamos el cliente, sino se encuentra lanza la excepcion el propio metodo
			String clientName = clientManager.getClientName(clientId);
			clientManager.deleteClient(clientName);
			
			response.setSuccess(this.getLanguageMessage("backup.message.client.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Comprueba si un cliente está online
	 * @return
	 */
	@Path("/checkonline/{clientid}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response checkOnlineClient(@PathParam("clientid") Integer clientId) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			// Bucamos el cliente, sino se encuentra lanza la excepcion el propio metodo
			String clientName = clientManager.getClientName(clientId);
			Boolean isOnline = clientManager.isOnlineClient(clientName);
			if (isOnline)
				response.setSuccess(this.getLanguageMessage("backup.message.client.isonline"));
			else
				response.setSuccess(this.getLanguageMessage("backup.message.client.isoffline"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
