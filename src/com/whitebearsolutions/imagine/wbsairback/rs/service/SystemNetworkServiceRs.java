package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.NetworkManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkInterfaceRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkInterfacesConfigRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkNameServerRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkNameServersConfigRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkRouteRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkRoutesConfigRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.NetworkSlaveInterfaceRs;
import com.whitebearsolutions.util.Command;
import com.whitebearsolutions.util.Configuration;

@Path("/system/network")
public class SystemNetworkServiceRs extends WbsImagineServiceRs {

	private NetworkManager nm;
	private Configuration c;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			nm = this.sessionManager.getNetworkManager();
			c = this.sessionManager.getConfiguration();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Establece la configuración de servidores de nombres del sistema
	 * @param nameServersConfig
	 * @return
	 */
	@Path("/nameservers")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setNameServersConfig(NetworkNameServersConfigRs nameServersConfig) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if(c.getProperty("directory.status") != null && !c.getProperty("directory.status").equals("standalone")) 
				throw new Exception("system.network.status.notstandalone");
			
			List<String[]> serversAddress = new ArrayList<String[]>();
			if(nameServersConfig.getNameServers() != null && !nameServersConfig.getNameServers().isEmpty()) {
				for (NetworkNameServerRs nameServer : nameServersConfig.getNameServers()) {
					if (nameServer.getAddress() != null && !nameServer.getAddress().isEmpty())
						if (NetworkManager.isValidAddress(nameServer.getAddress()))
							serversAddress.add(NetworkManager.toAddress(nameServer.getAddress()));
						else
							throw new Exception(getLanguageMessage("sytem.network.nameservers.invalid.address")+" : "+nameServer.getAddress());
				}
			} else {
				throw new Exception(getLanguageMessage("sytem.network.nameservers.empty"));
			}
			
			if (serversAddress.isEmpty())
				throw new Exception(getLanguageMessage("sytem.network.nameservers.empty"));
			
			
			nm.setNameservers(serversAddress);
			String error = networkReload();
			
			response.setSuccess(getLanguageMessage("system.network.config_server")+error);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene los servidores de nombres configurados en el sistema
	 * @return
	 */
	@Path("/nameservers")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getNameServers() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String[]> list = nm.getNameservers();
			List<NetworkNameServerRs> nameservers = NetworkNameServerRs.listToObjects(list);
			
			response.setNetworkNameServers(nameservers);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene todas las interfaces del sistema
	 * @return
	 */
	@Path("/interfaces")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAllInterfaces() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> list = nm.getAvailableInterfaces();
			List<NetworkInterfaceRs> interfaces = NetworkInterfaceRs.listToObjects(list, nm);
			
			response.setNetworkInterfaces(interfaces);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene todas las interfaces del sistema
	 * @return
	 */
	@Path("/interfaces/available")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAvailableInterfaces() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> list = nm.getAvailableInterfaces();
			list.removeAll(nm.getConfiguredInterfaces());
			List<NetworkInterfaceRs> interfaces = NetworkInterfaceRs.listToObjects(list, nm);
			
			response.setNetworkInterfaces(interfaces);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	/**
	 * Obtiene una interfaz a partir de su identificador
	 * @param idInterface
	 * @return
	 */
	@Path("/interfaces/{idInterface}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getInterface(@PathParam("idInterface") String idInterface) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> list = nm.getAvailableInterfaces();
			if (list == null || list.isEmpty())
				throw new Exception(getLanguageMessage("system.network.interface.notfound"));
			
			NetworkInterfaceRs _interface = null;
			for (String iface : list) {
				if (iface.equals(idInterface)) {
					_interface = NetworkInterfaceRs.getObject(iface, nm);
				}
			}
			
			if (_interface == null)
				throw new Exception(getLanguageMessage("system.network.interface.notfound"));
			
			response.setNetworkInterface(_interface);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene el listado de las interfaces configuradas en el sistema
	 * @return
	 */
	@Path("/interfaces/configured")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getConfiguredInterfaces() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			 List<String> list = nm.getConfiguredInterfaces();
			 List<NetworkInterfaceRs> interfaces = NetworkInterfaceRs.listToObjects(list, nm);
			
			 response.setNetworkInterfaces(interfaces);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Configura el conjunto de interfaces de red del sistema
	 * @param iface
	 * @return
	 */
	@Path("/interfaces")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setNetworkInterface(NetworkInterfacesConfigRs ifaceConfig) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if(c.getProperty("directory.status") != null && !c.getProperty("directory.status").equals("standalone")) 
				throw new Exception("system.network.status.notstandalone");
			
			List<String> systemIfaces = nm.getAvailableInterfaces();
			
			if (systemIfaces == null || systemIfaces.isEmpty())
				throw new Exception("system.network.fatalerror.system.ifaces.empty");
			
			if (ifaceConfig.getInterfaces() == null || ifaceConfig.getInterfaces().isEmpty())
				throw new Exception("system.network.interfaces.empty");
			
			
			// Comprobamos los datos
			ArrayList<String> listGateways = new ArrayList<String>();
			List<String> listSlaves = new ArrayList<String>();
			
			List<String> ifaces = new ArrayList<String>();
			List<String[]> addresses = new ArrayList<String[]>();
			List<String[]> masks = new ArrayList<String[]>();
			List<String[]> gateways = new ArrayList<String[]>();
			Map<String, List<String>> slaves = new HashMap<String, List<String>>();
			Map<String, Integer> bondTypes = new HashMap<String, Integer>();
			
			for (NetworkInterfaceRs iface : ifaceConfig.getInterfaces()) {
				if (iface.getIface() != null && !iface.getIface().isEmpty() && systemIfaces.contains(iface.getIface())) {
					if (iface.getGateway() != null && !iface.getGateway().isEmpty()) {
						if (NetworkManager.isValidAddress(iface.getGateway())) {
							if (!listGateways.contains(iface.getGateway()))
								listGateways.add(iface.getGateway());
							if (listGateways.size()>1)
								throw new Exception(getLanguageMessage("system.network.interfaces.gateway.onlyone"));
						} else
							throw new Exception(getLanguageMessage("system.network.interfaces.gateway.invalid")+": "+iface.getIface()+" ["+iface.getGateway()+"]");
					}
					
					if (iface.getSlaves() != null && !iface.getSlaves().isEmpty()) {
						if (!nm.isConfiguredInterface(iface.getIface()))
								throw new Exception(getLanguageMessage("system.network.interfaces.slaves.iface.notconfigured"));
						List<String> ifaceSlaves = new ArrayList<String>();
						for (NetworkSlaveInterfaceRs slave : iface.getSlaves()) {
							if (listSlaves.contains(slave.getIface()))
								throw new Exception(getLanguageMessage("system.network.interfaces.slave.severlinterfaces"));
							listSlaves.add(slave.getIface());
							ifaceSlaves.add(slave.getIface());
						}
						slaves.put(iface.getIface(), listSlaves);
						
						int bond_type = NetworkManager.BONDING_RR;
						if (iface.getBondtype() != null && iface.getBondtype().toLowerCase().equals("lacp"))
							bond_type = NetworkManager.BONDING_LACP;
						bondTypes.put(iface.getIface(), bond_type);
					}
					
					if (iface.getAddress() != null && !iface.getAddress().isEmpty() && NetworkManager.isValidAddress(iface.getAddress())) {
						if (iface.getNetmask() != null && !iface.getNetmask().isEmpty() && NetworkManager.isValidAddress(iface.getNetmask())) {
							ifaces.add(iface.getIface());
							addresses.add(NetworkManager.toAddress(iface.getAddress()));
							masks.add(NetworkManager.toAddress(iface.getNetmask()));
							gateways.add(NetworkManager.toAddress(iface.getGateway()));
						} else
							throw new Exception(getLanguageMessage("system.network.interfaces.netmask.invalid")+": "+iface.getNetmask()+" ["+iface.getNetmask()+"]");
					} else
						throw new Exception(getLanguageMessage("system.network.interfaces.address.invalid")+": "+iface.getIface()+" ["+iface.getAddress()+"]");
				} else
					throw new Exception(getLanguageMessage("system.network.interfaces.iface.notexists")+": "+iface.getIface());
			}
			
			// Ejecutamos realmente la configuracion
			int i=0;
			for (String iface : ifaces) {
				if (slaves.containsKey(iface) && bondTypes.containsKey(iface)) {
					List<String> slavesIface = slaves.get(iface);
					slavesIface.add(iface);
					
					nm.setBondInterface(nm.getNextBondInterface(), bondTypes.get(iface), slavesIface);
				}
				
				if(nm.isConfiguredInterface(iface)) {
					if (bondTypes.containsKey(iface)) {
						if(bondTypes.get(iface).intValue() != nm.getBondType(iface)) {
							nm.setBondInterfaceType(iface, bondTypes.get(iface).intValue());
						}
					}
					if(!NetworkManager.compareAddress(addresses.get(i), nm.getAddress(iface)) ||
	                        !NetworkManager.compareAddress(masks.get(i), nm.getNetmask(iface)) ||
	                        (gateways.get(i) != null && !NetworkManager.compareAddress(gateways.get(i), nm.getGateway(iface)))) {
						nm.setNetworkInterface(iface, addresses.get(i), masks.get(i), gateways.get(i));
					}
				} else {
					nm.setNetworkInterface(iface, addresses.get(i), masks.get(i), gateways.get(i));
				}
				i++;
			}
			
			String error = networkReload();
			
			response.setSuccess(getLanguageMessage("system.network") + error);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Elimina una interfaz de red a partir de su identificador
	 * @return
	 */
	@Path("/interfaces/{idInterface}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response removeNetworkInterface(@PathParam("idInterface") String idInterface) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			StringBuilder reply = new StringBuilder();
			Configuration _c = this.sessionManager.getConfiguration();
			
			if((_c.getProperty("directory.status") == null || _c.getProperty("directory.status").equals("standalone")) ) {
				nm.removeNetworkInterface(idInterface);
				reply.append(getLanguageMessage("system.network.config_route"));
				nm.update();
			}
			reply.append(getLanguageMessage("system.network.info_stored"));
			
			response.setSuccess(reply.toString());
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene todas las rutas de red configuradas
	 * @return
	 */
	@Path("/routes")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getRoutes() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<NetworkRouteRs> routes = new ArrayList<NetworkRouteRs>();
			for (String _interface : nm.getConfiguredInterfaces()) {
                if(nm.hasStaticRoutes(_interface)) {
                	List<Map<String, String[]>> listRoutes = nm.getStaticRoutes(_interface);
                	for (Map<String, String[]> mapRoute : listRoutes)
                		routes.add(NetworkRouteRs.mapToObject(_interface, mapRoute));
                }
			}
			
			response.setNetworkRoutes(routes);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene las rutas de red configuradas para cierta interfaz
	 * @param iface
	 * @return
	 */
	@Path("/routes/interface/{interface}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getRoutesOfInterface(@PathParam("interface") String iface) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<NetworkRouteRs> routes = new ArrayList<NetworkRouteRs>();
            if(nm.hasStaticRoutes(iface)) {
               	List<Map<String, String[]>> listRoutes = nm.getStaticRoutes(iface);
               	for (Map<String, String[]> mapRoute : listRoutes)
               		routes.add(NetworkRouteRs.mapToObject(iface, mapRoute));
            }
			
			response.setNetworkRoutes(routes);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Añade o modifica una nueva ruta de red
	 * @param route
	 * @return
	 */
	@Path("/routes")
	@POST
	@Produces(MediaType.TEXT_XML)
	public Response setRoutesConfig(NetworkRoutesConfigRs routeConfig) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if(c.getProperty("directory.status") != null && !c.getProperty("directory.status").equals("standalone")) 
				throw new Exception("system.network.status.notstandalone");
			
			List<String> systemIfaces = nm.getAvailableInterfaces();
			
			if (systemIfaces == null || systemIfaces.isEmpty())
				throw new Exception("system.network.fatalerror.system.ifaces.empty");
			
			List<String> ifaces = new ArrayList<String>();
			List<String[]> addresses = new ArrayList<String[]>();
			List<String[]> masks = new ArrayList<String[]>();
			List<String[]> gateways = new ArrayList<String[]>();
			
			if (routeConfig.getRoutes() != null && !routeConfig.getRoutes().isEmpty()) {
				for (NetworkRouteRs route : routeConfig.getRoutes()) {
					if(route.getIface() != null && !route.getIface().isEmpty() && systemIfaces.contains(route.getIface()) && nm.isConfiguredInterface(route.getIface())) {
						if (route.getAddress() != null && !route.getAddress().isEmpty() && NetworkManager.isValidAddress(route.getAddress())) {
							if (route.getNetmask() != null && !route.getNetmask().isEmpty() && NetworkManager.isValidAddress(route.getNetmask())) {
								if (route.getGateway() != null && !route.getGateway().isEmpty() && NetworkManager.isValidAddress(route.getGateway())) {
									ifaces.add(route.getIface());
									addresses.add(NetworkManager.toAddress(route.getAddress()));
									masks.add(NetworkManager.toAddress(route.getNetmask()));
									gateways.add(NetworkManager.toAddress(route.getGateway()));
								} else {
									throw new Exception (getLanguageMessage("system.network.routes.gateway.notvalid")+": "+route.getIface()+" ["+route.getGateway()+"]");
								}
							} else {
								throw new Exception (getLanguageMessage("system.network.routes.mask.notvalid")+": "+route.getIface()+" ["+route.getNetmask()+"]");
							}
						} else {
							throw new Exception (getLanguageMessage("system.network.routes.address.notvalid")+": "+route.getIface()+" ["+route.getAddress()+"]");
						}
					} else {
						throw new Exception (getLanguageMessage("system.network.routes.iface.notconfigured")+": "+route.getIface());
					}
				}
			} else{
				throw new Exception (getLanguageMessage("system.network.routes.empty"));
			}
			
			
			nm.reinitStaticRoutes();
			int i = 0;
			for (String iface : ifaces) {
				nm.setStaticRoute(iface, addresses.get(i), masks.get(i), gateways.get(i));
				i++;
			}
			String error = networkReload();
					
			response.setSuccess(getLanguageMessage("system.network.config_route")+error);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	public String networkReload() throws Exception {
		String error = "";
		nm.update();
		this.sessionManager.reloadConfiguration();
		this.sessionManager.reloadNetworkManager();
		try {
			Command.systemCommand("/etc/init.d/networking restart");
		} catch (Exception ex) {
			error = ". Error on networking restart: "+ex.getMessage();
		}
		return error;
	}
}
