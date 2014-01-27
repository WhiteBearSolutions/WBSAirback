package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.whitebearsolutions.imagine.wbsairback.configuration.NTPConfiguration;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.TimeDateRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.TimeServerRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.TimeServerSetRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.system.TimeZoneRs;

@Path("/system/time")
public class SystemClockServiceRs extends WbsImagineServiceRs {

	private NTPConfiguration nc;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			nc = new NTPConfiguration();
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Devuelve el listado de servidores ntp configurados
	 * @return
	 */
	@Path("/servers")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getTimeServers() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> list = nc.getServers();
			List<TimeServerRs> servers = TimeServerRs.listMapToObject(list);
			
			response.setTimeServers(servers);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Establece los servidores ntp
	 * @param zone
	 * @return
	 */
	@Path("/servers")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setTimeServers(TimeServerSetRs serverSet ) {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String server1 = null;
			if (serverSet.getServer1() != null)
				if (serverSet.getServer1().getServer() != null && !serverSet.getServer1().getServer().isEmpty())
					server1 = serverSet.getServer1().getServer();
			String server2 = null;
			if (serverSet.getServer2() != null)
				if (serverSet.getServer2().getServer() != null && !serverSet.getServer2().getServer().isEmpty())
					server2 = serverSet.getServer2().getServer();
			String server3 = null;
			if (serverSet.getServer3() != null)
				if (serverSet.getServer3().getServer() != null && !serverSet.getServer3().getServer().isEmpty())
					server3 = serverSet.getServer3().getServer();
			
			nc.setServer(server1, server2, server3);
			
			response.setSuccess(getLanguageMessage("system.clock.servers.configured"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve el listado de zonas ntp configurables
	 * @param user
	 * @return
	 */
	@Path("/zones")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAvailabeTimeZones() {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> list = Arrays.asList(NTPConfiguration.getAvailableTimeZones());
			List<TimeZoneRs> zones = TimeZoneRs.listMapToObject(list);
			
			response.setTimeZones(zones);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	

	/**
	 * Establece la zona
	 * @param zone
	 * @return
	 */
	@Path("/zones")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setTimeZone(TimeZoneRs zone ) {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<String> list = Arrays.asList(NTPConfiguration.getAvailableTimeZones());
			if (!list.contains(zone.getZone()))
				throw new Exception("system.clock.exception.notfound");

			nc.setTimeZone(zone.getZone());
			
			response.setSuccess(getLanguageMessage("system.clock.zone.configured"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Devuelve la zona configurada actualmente en airback
	 * @param user
	 * @return
	 */
	@Path("/zones/configured")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getConfiguredTimeZones() {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String timezone = nc.getTimeZone();
			TimeZoneRs zone = TimeZoneRs.mapToObject(timezone);
			response.setTimeZone(zone);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Obtiene la fecha actual del sistema 
	 * @return
	 */
	@Path("/date")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getDate() {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(nc.getTimeZone()));
			response.setTimeDate(TimeDateRs.calToObject(cal));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Establece la fecha del sistema
	 * @param date
	 * @return
	 */
	@Path("/date")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response setDate(TimeDateRs date) {
		
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String timeOfDay = "pm";
			if (date.getTimeOfDay() != null)
				timeOfDay = date.getTimeOfDay();
			nc.setDate(date.getYear(), date.getMonth(), date.getDay(), date.getHour(), date.getMinute(), timeOfDay);
			response.setSuccess(getLanguageMessage("system.clock.date.configured"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
}
