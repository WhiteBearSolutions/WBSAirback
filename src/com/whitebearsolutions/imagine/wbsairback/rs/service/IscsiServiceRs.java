package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.net.ISCSIManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.IscsiExternalTargetLoginRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.IscsiExternalTargetRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.IscsiTargetRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.IscsiTargetSimpleRs;

@Path("/iscsi")
public class IscsiServiceRs extends WbsImagineServiceRs {

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
	 * Devuelve la lista de todos los targets
	 * @return
	 */
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAllTargets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listTargets = ISCSIManager.getAllTargets();
			List<IscsiTargetRs> targets = IscsiTargetRs.listMapToObject(listTargets);
			response.setIscsiTargets(targets);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve los targets de cierto tipo (changer, tape, volume)
	 * @param type
	 * @return
	 */
	@Path("/type/{type}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getTargetsOfType(@PathParam("type") String type) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listTargets = new ArrayList<Map<String, String>>();
			String t = type.toLowerCase();
			if (t.equals("changer") || t.equals("autochanger") || t.equals("changers") || t.equals("autochangers"))
				listTargets = ISCSIManager.getChangerTargets();
			else if (t.equals("tape") || t.equals("cinta") || t.equals("tapes") || t.equals("cintas"))
				listTargets = ISCSIManager.getTapeTargets();
			else if (t.equals("volume") || t.equals("volumen") || t.equals("volumes") || t.equals("volumenes"))
				listTargets = ISCSIManager.getVolumeTargets();
			else
				throw new Exception(getLanguageMessage("iscsi.rs.type.invalid"));
			
			List<IscsiTargetRs> targets = IscsiTargetRs.listMapToObject(listTargets);
			response.setIscsiTargets(targets);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve un target a partir del agregado y volumen de origen
	 * @param vg
	 * @param lv
	 * @return
	 */
	@Path("/vg/{vg}/lv/{lv}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getTarget(@PathParam("vg") String vg, @PathParam("lv") String lv) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listTargets = ISCSIManager.getAllTargets();
			IscsiTargetRs t = null;
			for (Map<String, String> target : listTargets) {
				if(vg.equals(target.get("vg")) && lv.equals(target.get("lv"))) {
					t = IscsiTargetRs.mapToObject(target);
				}
			}
			
			if (t == null)
				throw new Exception (getLanguageMessage("iscsi.rs.target.not.found"));
			
			response.setIscsiTarget(t);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Obtiene un target de tipo snapshot a partir de su agregado, volumen y snapshot
	 * @param vg
	 * @param lv
	 * @param snapshot
	 * @return
	 */
	@Path("/vg/{vg}/lv/{lv}/snapshot/{snapshot}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getTargetSnapshot(@PathParam("vg") String vg, @PathParam("lv") String lv, @PathParam("snapshot") String snapshot) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Map<String, String> mapTarget= ISCSIManager.getSnapshotTarget(vg, lv, snapshot);
			
			if (mapTarget == null || mapTarget.isEmpty())
				throw new Exception (getLanguageMessage("iscsi.rs.target.not.found"));
			
			IscsiTargetRs target = IscsiTargetRs.mapToObject(mapTarget);
			response.setIscsiTarget(target);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el nombre del iniciador iscsi para el cliente airback
	 * @return
	 */
	@Path("/airback/name")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getClientInitiatorName() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			String name = ISCSIManager.getClientInitiatorName();
			response.setAirbackIscsiName(name);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un target Iscsi
	 * @param target
	 * @return
	 */
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response addTarget(IscsiTargetRs target) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			target.setType(target.getType().toLowerCase());
			if (target.getType().equals("volume")) {
				ISCSIManager.addVolumeTargets(target.getVg(), target.getLv(), target.getIqn(), target.getUser(), target.getPassword());
			} else if (target.getType().equals("tape")) {
				if (target.getDevice() != null && !target.getDevice().isEmpty())
					ISCSIManager.addTapeTarget(target.getDevice(), target.getIqn(), target.getUser(), target.getPassword());
				else
					throw new Exception("iscsi.rs.device.required");
			} else if (target.getType().equals("autochanger")) {
				if (target.getDevice() != null && !target.getDevice().isEmpty())
					ISCSIManager.addChangerTarget(target.getDevice(), target.getIqn(), target.getUser(), target.getPassword());
				else
					throw new Exception("iscsi.rs.device.required");
			}
			
			response.setSuccess(getLanguageMessage("iscsi.rs.target.added.success"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade un target de tipo snapshot
	 * @param vg
	 * @param lv
	 * @param snapshot
	 * @return
	 */
	@Path("/vg/{vg}/lv/{lv}/snapshot/{snapshot}")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response addTargetSnapshot(@PathParam("vg") String vg, @PathParam("lv") String lv, @PathParam("snapshot") String snapshot) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ISCSIManager.addVolumeSnapshotTargets(vg, lv, snapshot);
			response.setSuccess(getLanguageMessage("iscsi.rs.target.added.success"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina todos los targets iscsi
	 * @return
	 */
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeAllTargets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ISCSIManager.removeAllTargets();
			response.setSuccess(getLanguageMessage("iscsi.rs.deleteall.success"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina un target isci en base a su iqn
	 * @param iqn
	 * @return
	 */
	@Path("{iqn}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeTarget(@PathParam("iqn") String iqn) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ISCSIManager.removeTarget(iqn);
			response.setSuccess(getLanguageMessage("iscsi.rs.deletetarget.success"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene los targets externos de sesión
	 * @return
	 */
	@Path("/external")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getExternalSessionTargets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listMap = ISCSIManager.getExternalSessionTargets();
			List<IscsiExternalTargetRs> targets = IscsiExternalTargetRs.listMapToObject(listMap);
			response.setIscsiExternalTargets(targets);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene los targets externos caducados
	 * @return
	 */
	@Path("/external/outdated")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getExternalOutdatedTargets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listMap = ISCSIManager.getOldTargets();
			List<IscsiExternalTargetRs> targets = IscsiExternalTargetRs.listMapToObject(listMap);
			for (IscsiExternalTargetRs t : targets)
				t.setOutdated(true);
			response.setIscsiExternalTargets(targets);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene los targets externos guardados para hacer login en cada reinicio
	 * @return
	 */
	@Path("/external/send")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getExternalSendTargets() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listMap = ISCSIManager.getSendTargets();
			List<IscsiExternalTargetRs> targets = IscsiExternalTargetRs.listMapToObject(listMap);
			response.setIscsiExternalTargets(targets);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Busca un listado de targets en cierta dirección ip
	 * @param address
	 * @return
	 */
	@Path("/external/search/address/{address}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response searchExternalTargets(@PathParam("address") String address) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String[] add = NetworkManager.toAddress(address);
			int[] iAdd = new int[add.length];
			for (int i=0;i<add.length;i++)
				iAdd[i]=Integer.parseInt(add[i]);
			
			List<String> list = ISCSIManager.searchExternalTargets(iAdd);
			List<IscsiTargetSimpleRs> targets = IscsiTargetSimpleRs.listMapToObject(list);
			response.setIscsiTargetSimples(targets);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Realiza login en targets externo
	 * @param target
	 * @return
	 */
	@Path("/external/login")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Response loginExternalTarget(IscsiExternalTargetLoginRs target) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String[] add = NetworkManager.toAddress(target.getAddress());
			int[] iAdd = new int[add.length];
			for (int i=0;i<add.length;i++)
				iAdd[i]=Integer.parseInt(add[i]);
			
			if (target.getTargets() == null || target.getTargets().size() == 0) 
				throw new Exception(getLanguageMessage("iscsi.rs.logintarget.notargets"));
			
			for (IscsiTargetSimpleRs t : target.getTargets()) {
				ISCSIManager.loginExternalTarget(iAdd, t.getTarget() , target.getMethod(), target.getUser(), target.getPassword());
			}
			
			response.setSuccess(getLanguageMessage("iscsi.rs.login.success"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Realiza logout de targets externo
	 * @param target
	 * @return
	 */
	@Path("/external/logout")
	@POST
	@Produces(MediaType.TEXT_XML)
	public Response logoutExternalTarget(IscsiExternalTargetLoginRs target) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String[] add = NetworkManager.toAddress(target.getAddress());
			int[] iAdd = new int[add.length];
			for (int i=0;i<add.length;i++)
				iAdd[i]=Integer.parseInt(add[i]);
			
			if (target.getTargets() == null || target.getTargets().size() == 0) 
				throw new Exception(getLanguageMessage("iscsi.rs.logintarget.notargets"));
			
			for (IscsiTargetSimpleRs t : target.getTargets()) {
				ISCSIManager.logoutExternalTarget(iAdd, t.getTarget());
			}
			
			response.setSuccess(getLanguageMessage("iscsi.rs.logout.success"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Realiza logout de targets externo
	 * @param target
	 * @return
	 */
	@Path("/external/outdated/remove/address/{address}/port/{port}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeOldExternalTarget(@PathParam("address") String address, @PathParam("port") String port) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String[] add = NetworkManager.toAddress(address);
			int[] iAdd = new int[add.length];
			for (int i=0;i<add.length;i++)
				iAdd[i]=Integer.parseInt(add[i]);
			
			ISCSIManager.removeOldExternalTarget(address, port);
						
			response.setSuccess(getLanguageMessage("iscsi.rs.logout.success"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
}
