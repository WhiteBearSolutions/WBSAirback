package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.disk.QuotaManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.QuotaAddRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.QuotaGroupRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.QuotaUserRs;

@Path("/quota")
public class QuotaServiceRs extends WbsImagineServiceRs {

	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category){
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Devuelve el listado de quotas de un grupo y un volumen, se puede buscar por un string
	 * @param group
	 * @param volume
	 * @param match
	 * @return
	 */
	@Path("/users/group/{group}/volume/{volume}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getUserQuotasOfVolume(@PathParam("group") String group, @PathParam("volume") String volume, @QueryParam("match") String match ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Map<String, String>> maps = QuotaManager.searchVolumeUserQuotas(match, group, volume);
			List<QuotaUserRs> quotas = QuotaUserRs.listMapToObject(maps);
			response.setQuotasUsers(quotas);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene la quota de un volumen para un usuario
	 * @param group
	 * @param volume
	 * @param user
	 * @return
	 */
	@Path("/users/group/{group}/volume/{volume}/username/{username}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getUserQuotaOfVolume(@PathParam("group") String group, @PathParam("volume") String volume, @PathParam("username") String user ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> map = QuotaManager.getVolumeUserQuota(user, group, volume);
			QuotaUserRs quota = QuotaUserRs.mapToObject(user, map);
			response.setQuotaUser(quota);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina una quota de usuario 
	 * @param group
	 * @param volume
	 * @param user
	 * @return
	 */
	@Path("/users/group/{group}/volume/{volume}/username/{username}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeUserQuotaOfVolume(@PathParam("group") String group, @PathParam("volume") String volume, @PathParam("username") String user ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			QuotaManager.setVolumeUserQuota(user, group, volume, 0);
			response.setSuccess(getLanguageMessage("device.quota.removed.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Añade una nueva cota de tipo usuario
	 * @param quota
	 * @return
	 */
	@Path("/users")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response addUserQuota(QuotaAddRs quota) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			QuotaManager.setVolumeUserQuota(quota.getName(), quota.getGroupName(), quota.getVolumeName(), quota.getSize());
			uriCreated = "/users/group/"+quota.getGroupName()+"/volume/"+quota.getVolumeName()+"/username/"+quota.getName();
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
	 * Añade una nueva cota de tipo grupo
	 * @param quota
	 * @return
	 */
	@Path("/groups")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response addGroupQuota(QuotaAddRs quota) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			QuotaManager.setVolumeGroupQuota(quota.getName(), quota.getGroupName(), quota.getVolumeName(), quota.getSize());
			uriCreated = "/groups/group/"+quota.getGroupName()+"/volume/"+quota.getVolumeName()+"/groupname/"+quota.getName();
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
	 * Devuelve el listado de quotas de un grupo y un volumen, se puede buscar por un string
	 * @param group
	 * @param volume
	 * @param match
	 * @return
	 */
	@Path("/groups/group/{group}/volume/{volume}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getGroupQuotasOfVolume(@PathParam("group") String group, @PathParam("volume") String volume, @QueryParam("match") String match ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, Map<String, String>> maps = QuotaManager.searchVolumeGroupQuotas(match, group, volume);
			List<QuotaGroupRs> quotas = QuotaGroupRs.listMapToObject(maps);
			response.setQuotasGroups(quotas);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene la quota de un volumen para un grupo
	 * @param group
	 * @param volume
	 * @param user
	 * @return
	 */
	@Path("/groups/group/{group}/volume/{volume}/groupname/{groupname}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getGroupQuotaOfVolume(@PathParam("group") String group, @PathParam("volume") String volume, @PathParam("groupname") String groupname ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> map = QuotaManager.getVolumeGroupQuota(groupname, group, volume);
			QuotaGroupRs quota = QuotaGroupRs.mapToObject(groupname, map);
			response.setQuotaGroup(quota);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina una quota de grupo
	 * @param group
	 * @param volume
	 * @param groupname
	 * @return
	 */
	@Path("/groups/group/{group}/volume/{volume}/groupname/{groupname}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteGroupQuotaOfVolume(@PathParam("group") String group, @PathParam("volume") String volume, @PathParam("groupname") String groupname ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			QuotaManager.setVolumeGroupQuota(groupname, group, volume, 0);
			response.setSuccess(getLanguageMessage("device.quota.removed.success"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
