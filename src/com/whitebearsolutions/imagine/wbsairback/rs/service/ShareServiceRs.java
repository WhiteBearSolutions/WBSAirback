package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.net.URI;
import java.util.HashMap;
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

import org.apache.commons.codec.net.URLCodec;

import com.whitebearsolutions.imagine.wbsairback.disk.ShareManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ShareExternalAddRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ShareExternalRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.disk.ShareRs;

@Path("/shares")
public class ShareServiceRs extends WbsImagineServiceRs{

	private ShareManager shareManager;
	private URLCodec encoder;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category){
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			shareManager = new ShareManager(this.getConfig());
			encoder = new URLCodec("UTF-8");
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	

	/**
	 * Devuelve un share
	 * @param sType
	 * @param aggregateName
	 * @param volumeName
	 * @return
	 */
	@Path("/type/{type}/aggregate/{aggregateName}/volume/{volumeName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getShare(@PathParam("type") String sType, @PathParam("aggregateName") String aggregateName, @PathParam("volumeName") String volumeName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			int type = ShareManager.NFS;
			if (sType.equals("cifs")) {
				type = ShareManager.CIFS;
			} else if (sType.equals("ftp")) {
				type = ShareManager.FTP;
			}
			
			Map<String, String> mapShare = ShareManager.getShare(type, aggregateName, volumeName);
			ShareRs share = ShareRs.mapToObject(mapShare);
			response.setShare(share);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Lista los shares de cierto tipo
	 * @param sType
	 * @return
	 */
	@Path("/type/{type}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getShares(@PathParam("type") String sType) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			int type = ShareManager.NFS;
			if (sType.equals("cifs")) {
				type = ShareManager.CIFS;
			} else if (sType.equals("ftp")) {
				type = ShareManager.FTP;
			}
			
			List<Map<String, String>> listMapShares = ShareManager.getShares(type);
			List<ShareRs> shares = ShareRs.listMapToObject(listMapShares);
			response.setShares(shares);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo share
	 * @param share
	 * @return
	 */
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewShare(ShareRs share) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String sType = share.getProtocol().toLowerCase();
			int type = ShareManager.NFS;
			if (sType.equals("cifs")) {
				type = ShareManager.CIFS;
			} else if (sType.equals("ftp")) {
				type = ShareManager.FTP;
			}
			
			boolean exists = false;
			try {
				ShareManager.getShare(type, share.getVg(), share.getLv());
				exists = true;
			} catch (Exception ex) {}
			
			if (exists)
				throw new Exception(getLanguageMessage("shares.rs.duplicated"));
			
			Map<String, String> attributes = new HashMap<String, String>();
			if (share.getAnonymous() != null)
				attributes.put("anonymous", share.getAnonymous().toString());
			if (share.getAsync() != null)
				attributes.put("async", share.getAsync().toString());
			if (share.getSquash() != null)
				attributes.put("squash", share.getSquash().toString());
			if (share.getRecycle() != null)
				attributes.put("recycle", share.getRecycle().toString());
			if (share.getAddress() != null)
				attributes.put("address", share.getAddress().toString());
			if (share.getName() != null && !share.getName().isEmpty())
				attributes.put("name", share.getName());
			
			ShareManager.addShare(share.getVg(), share.getLv(), type, attributes);
			uriCreated = "/type/"+sType+"/aggregate/"+share.getVg()+"/volume/"+share.getLv();
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
	 * Edita un share
	 * @param share
	 * @return
	 */
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editShare(ShareRs share) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String sType = share.getProtocol().toLowerCase();
			int type = ShareManager.NFS;
			if (sType.equals("cifs")) {
				type = ShareManager.CIFS;
			} else if (sType.equals("ftp")) {
				type = ShareManager.FTP;
			}
			
			ShareManager.getShare(type, share.getVg(), share.getLv());
			
			Map<String, String> attributes = new HashMap<String, String>();
			if (share.getAnonymous() != null)
				attributes.put("anonymous", share.getAnonymous().toString());
			if (share.getAsync() != null)
				attributes.put("async", share.getAsync().toString());
			if (share.getSquash() != null)
				attributes.put("squash", share.getSquash().toString());
			if (share.getRecycle() != null)
				attributes.put("recycle", share.getRecycle().toString());
			if (share.getAddress() != null)
				attributes.put("address", share.getAddress().toString());
			if (share.getName() != null && !share.getName().isEmpty())
				attributes.put("name", share.getName());
			
			ShareManager.updateShare(share.getVg(), share.getLv(), type, attributes);
			uriCreated = "/type/"+sType+"/aggregate/"+share.getVg()+"/volume/"+share.getLv();
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
	 * Elimina un share
	 * @param sType
	 * @param aggregateName
	 * @param volumeName
	 * @return
	 */
	@Path("/type/{type}/aggregate/{aggregateName}/volume/{volumeName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeShare(@PathParam("type") String sType, @PathParam("aggregateName") String aggregateName, @PathParam("volumeName") String volumeName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			int type = ShareManager.NFS;
			if (sType.equals("cifs")) {
				type = ShareManager.CIFS;
			} else if (sType.equals("ftp")) {
				type = ShareManager.FTP;
			}
			
			ShareManager.removeShare(aggregateName, volumeName, type);
			response.setSuccess(getLanguageMessage("device.shares.removed_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}


	/**
	 * Devuelve un share externo
	 * @param server
	 * @param share
	 * @return
	 */
	@Path("/external/server/{server}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getShareExternal(@PathParam("server") String server, @QueryParam("share") String share) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (share == null || share.isEmpty())
				throw new Exception("shares.external.notexists");
			share = encoder.decode(share, "UTF-8");
			Map<String, String> mapShare = ShareManager.getExternalShare(server, share);
			if (mapShare == null || mapShare.isEmpty())
				throw new Exception("shares.external.notexists");
			ShareExternalRs shareExternal = ShareExternalRs.mapToObject(mapShare);
			response.setShareExternal(shareExternal);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Lista los shares externos
	 * @return
	 */
	@Path("/external")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getSharesExternal() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> listMapShares = ShareManager.getExternalShares();
			List<ShareExternalRs> shares = ShareExternalRs.listMapToObject(listMapShares);
			response.setSharesExternal(shares);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Crea un nuevo share
	 * @param share
	 * @return
	 */
	@Path("/external")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewShareExternal(ShareExternalAddRs share) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String sType = share.getType().toLowerCase();
			int type = ShareManager.NFS;
			if (sType.equals("cifs")) {
				type = ShareManager.CIFS;
			}
			
			if (!ShareManager.getExternalShare(share.getServer(), share.getShare()).isEmpty())
				throw new Exception(getLanguageMessage("shares.rs.duplicated"));
			
			ShareManager.addExtrenalShare(share.getServer(), share.getShare(), share.getUser(), share.getPassword(), share.getDomain(), type);
			uriCreated = "/external/server/"+share.getServer()+"/share/"+share.getShare();
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
	 * Edita un share
	 * @param share
	 * @return
	 */
	@Path("/external")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editShareExternal(ShareExternalAddRs share) {
		
		String uriCreated = null;
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String sType = share.getType().toLowerCase();
			int type = ShareManager.NFS;
			if (sType.equals("cifs")) {
				type = ShareManager.CIFS;
			}
			
			if (ShareManager.getExternalShare(share.getServer(), share.getShare()).isEmpty())
				throw new Exception(getLanguageMessage("shares.rs.not.exists"));
			
			ShareManager.addExtrenalShare(share.getServer(), share.getShare(), share.getUser(), share.getPassword(), share.getDomain(), type);
			uriCreated = "/external/server/"+share.getServer()+"/share/"+share.getShare();
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
	 * Elimina un share
	 * @param sType
	 * @param aggregateName
	 * @param volumeName
	 * @return
	 */
	@Path("/external/server/{server}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeShareExternal(@PathParam("server") String server, @QueryParam("share") String share, @QueryParam("force") Boolean force) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (share == null || share.isEmpty())
				throw new Exception("shares.external.notexists");
			share = encoder.decode(share, "UTF-8");
			Map<String, String> mapShare = ShareManager.getExternalShare(server, share);
			if (mapShare == null || mapShare.isEmpty())
				throw new Exception("shares.external.notexists");
			boolean f = false;
			if (force != null)
				f = force;
			shareManager.removeExternalShare(server, share, f);
			response.setSuccess(getLanguageMessage("device.shares.removed_ok"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
}
