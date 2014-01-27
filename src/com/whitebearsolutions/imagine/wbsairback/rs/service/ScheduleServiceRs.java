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

import com.whitebearsolutions.imagine.wbsairback.backup.ScheduleManager;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleDailyCalendarRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleDailyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleMonthlyCalendarRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleMonthlyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleWeeklyCalendarRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleWeeklyRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleYearlyCalendarRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.ScheduleYearlyRs;

@Path("/schedules")
public class ScheduleServiceRs extends WbsImagineServiceRs {

	/**
	 * Metodo que inicializa la configuracion y la respuesta general
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
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
	
	private void commonSaveMonthlyCheckings(ScheduleMonthlyRs schedule) throws Exception {
		if (schedule.getCalendarFull() != null && !schedule.getCalendarFull().isEmpty()) {
			if (schedule.getFullHour() == null)
				throw new Exception("backup.schedule.rs.exception.fullhour.empty");
			if (schedule.getFullMinute() == null)
				throw new Exception("backup.schedule.rs.exception.fullminute.empty");
			if (schedule.getFullPool() == null)
				throw new Exception("backup.schedule.rs.exception.fullpool.empty");
		}
		
		if (schedule.getCalendarVirtual() != null && !schedule.getCalendarVirtual().isEmpty()) {
			if (schedule.getVirtualHour() == null)
				throw new Exception("backup.schedule.rs.exception.virtualhour.empty");
			if (schedule.getVirtualMinute() == null)
				throw new Exception("backup.schedule.rs.exception.virtualminute.empty");
			if (schedule.getVirtualPool() == null)
				throw new Exception("backup.schedule.rs.exception.virtualpool.empty");
		}
		
		if (schedule.getCalendarDifferential() != null && !schedule.getCalendarDifferential().isEmpty()) {
			if (schedule.getDifferentialHour() == null)
				throw new Exception("backup.schedule.rs.exception.differentialhour.empty");
			if (schedule.getDifferentialMinute() == null)
				throw new Exception("backup.schedule.rs.exception.differentialminute.empty");
			if (schedule.getDifferentialPool() == null)
				throw new Exception("backup.schedule.rs.exception.differentialpool.empty");
		}
		
		if (schedule.getCalendarIncremental() != null && !schedule.getCalendarIncremental().isEmpty()) {
			if (schedule.getIncrementalHour() == null)
				throw new Exception("backup.schedule.rs.exception.incrementalhour.empty");
			if (schedule.getIncrementalMinute() == null)
				throw new Exception("backup.schedule.rs.exception.incrementalminute.empty");
			if (schedule.getIncrementalPool() == null)
				throw new Exception("backup.schedule.rs.exception.incrementalpool.empty");
		}
	}
	
	@Path("/monthly")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listMonthlySchedules() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			List<String> scheduleNames = ScheduleManager.getMonthlyScheduleNames();
			List<ScheduleMonthlyRs> schedules = new ArrayList<ScheduleMonthlyRs>();
			for (String name : scheduleNames) {
				Map<String, String> mapObj = ScheduleManager.getMonthlySchedule(name);
				ScheduleMonthlyRs schedule = ScheduleMonthlyRs.mapToObject(mapObj);
				schedules.add(schedule);
			}
			
			response.setSchedulesMonthly(schedules);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/monthly/{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getMonthlySchedule(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			Map<String, String> mapSchedule = ScheduleManager.getMonthlySchedule(name);
			if (mapSchedule == null)
				throw new Exception(getLanguageMessage("backup.schedule.not.exists"));
			
			ScheduleMonthlyRs schedule = ScheduleMonthlyRs.mapToObject(mapSchedule);
			
			response.setScheduleMonthly(schedule);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/monthly")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewMonthlySchedule(ScheduleMonthlyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			commonSaveMonthlyCheckings(schedule);
			int fullh = 0, fullm = 0, increh = 0, increm = 0, difh = 0, difm = 0, virh = 0, virm = 0; 
			if (schedule.getFullHour() != null)
				fullh = schedule.getFullHour();
			if (schedule.getFullMinute() != null)
				fullm = schedule.getFullMinute();
			if (schedule.getVirtualHour() != null)
				virh = schedule.getVirtualHour();
			if (schedule.getVirtualMinute() != null)
				virm = schedule.getVirtualMinute();
			if (schedule.getIncrementalHour() != null)
				increh = schedule.getIncrementalHour();
			if (schedule.getIncrementalMinute() != null)
				increm = schedule.getIncrementalMinute();
			if (schedule.getDifferentialHour() != null)
				difh = schedule.getDifferentialHour();
			if (schedule.getDifferentialMinute() != null)
				difm = schedule.getDifferentialMinute();
			
			Map<String, String> values = ScheduleMonthlyCalendarRs.fillValues(schedule);
			ScheduleManager.setMonthlyScheduler(schedule.getName(), values, schedule.getFullPool(), fullh, fullm, schedule.getIncrementalPool(), increh, increm, schedule.getDifferentialPool(), difh, difm, schedule.getVirtualPool(), virh, virm);
			uriCreated="/schedule/"+schedule.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	@Path("/monthly")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editMonthlySchedule(ScheduleMonthlyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			// Comprobamos que existe
			ScheduleManager.getMonthlySchedule(schedule.getName());
			
			commonSaveMonthlyCheckings(schedule);
			int fullh = 0, fullm = 0, increh = 0, increm = 0, difh = 0, difm = 0, virh = 0, virm = 0; 
			if (schedule.getFullHour() != null)
				fullh = schedule.getFullHour();
			if (schedule.getFullMinute() != null)
				fullm = schedule.getFullMinute();
			if (schedule.getVirtualHour() != null)
				virh = schedule.getVirtualHour();
			if (schedule.getVirtualMinute() != null)
				virm = schedule.getVirtualMinute();
			if (schedule.getIncrementalHour() != null)
				increh = schedule.getIncrementalHour();
			if (schedule.getIncrementalMinute() != null)
				increm = schedule.getIncrementalMinute();
			if (schedule.getDifferentialHour() != null)
				difh = schedule.getDifferentialHour();
			if (schedule.getDifferentialMinute() != null)
				difm = schedule.getDifferentialMinute();
			
			Map<String, String> values = ScheduleMonthlyCalendarRs.fillValues(schedule);
			ScheduleManager.setMonthlyScheduler(schedule.getName(), values, schedule.getFullPool(), fullh, fullm, schedule.getIncrementalPool(), increh, increm, schedule.getDifferentialPool(), difh, difm, schedule.getVirtualPool(), virh, virm);
			uriCreated="/schedule/"+schedule.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	@Path("/daily")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listDaylySchedules() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			List<String> scheduleNames = ScheduleManager.getDailyScheduleNames();
			List<ScheduleDailyRs> schedules = new ArrayList<ScheduleDailyRs>();
			for (String name : scheduleNames) {
				List<Map<String, String>> maps = ScheduleManager.getDailySchedule(name);
				ScheduleDailyRs schedule = ScheduleDailyRs.mapToObject(name, maps);
				schedules.add(schedule);
			}
			
			response.setSchedulesDaily(schedules);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/daily/{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getDailyySchedule(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			List<Map<String, String>> mapSchedule = ScheduleManager.getDailySchedule(name);
			if (mapSchedule == null)
				throw new Exception(getLanguageMessage("backup.schedule.not.exists"));
			
			ScheduleDailyRs schedule = ScheduleDailyRs.mapToObject(name, mapSchedule);
			
			response.setScheduleDaily(schedule);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/daily")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewDailySchedule(ScheduleDailyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> values = ScheduleDailyCalendarRs.fillValues(schedule);
			ScheduleManager.setDailyScheduler(schedule.getName(), values);
			uriCreated="/schedule/"+schedule.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	@Path("/daily")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editDaylySchedule(ScheduleDailyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			// Comprobamos que existe
			ScheduleManager.getDailySchedule(schedule.getName());
			
			List<Map<String, String>> values = ScheduleDailyCalendarRs.fillValues(schedule);
			ScheduleManager.setDailyScheduler(schedule.getName(), values);
			uriCreated="/schedule/"+schedule.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	@Path("/weekly")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listWeeklySchedules() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			List<String> scheduleNames = ScheduleManager.getWeeklyScheduleNames();
			List<ScheduleWeeklyRs> schedules = new ArrayList<ScheduleWeeklyRs>();
			for (String name : scheduleNames) {
				List<Map<String, String>> maps = ScheduleManager.getWeeklyScheduleDays(name);
				ScheduleWeeklyRs schedule = ScheduleWeeklyRs.mapToObject(name, maps);
				schedules.add(schedule);
			}
			
			response.setSchedulesWeekly(schedules);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/weekly/{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getWeeklySchedule(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			List<Map<String, String>> mapSchedule = ScheduleManager.getWeeklyScheduleDays(name);
			if (mapSchedule == null)
				throw new Exception(getLanguageMessage("backup.schedule.not.exists"));
			
			ScheduleWeeklyRs schedule = ScheduleWeeklyRs.mapToObject(name, mapSchedule);
			
			response.setScheduleWeekly(schedule);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/weekly")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewWeeklySchedule(ScheduleWeeklyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> values = ScheduleWeeklyCalendarRs.fillValues(schedule);
			ScheduleManager.setWeeklyScheduleDays(schedule.getName(), values);
			uriCreated="/schedule/"+schedule.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	@Path("/weekly")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editWeeklySchedule(ScheduleWeeklyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			// Comprobamos que existe
			ScheduleManager.getWeeklyScheduleDays(schedule.getName());
			
			List<Map<String, String>> values = ScheduleWeeklyCalendarRs.fillValues(schedule);
			ScheduleManager.setWeeklyScheduleDays(schedule.getName(), values);
			uriCreated="/schedule/"+schedule.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	@Path("/yearly")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response listYearlySchedules() {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			List<String> scheduleNames = ScheduleManager.getYearlyScheduleNames();
			List<ScheduleYearlyRs> schedules = new ArrayList<ScheduleYearlyRs>();
			for (String name : scheduleNames) {
				List<Map<String, String>> maps = ScheduleManager.getYearlySchedule(name);
				ScheduleYearlyRs schedule = ScheduleYearlyRs.mapToObject(name, maps);
				schedules.add(schedule);
			}
			
			response.setSchedulesYearly(schedules);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/yearly/{name}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getYearlySchedule(@PathParam("name") String name) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			List<Map<String, String>> mapSchedule = ScheduleManager.getYearlySchedule(name);
			if (mapSchedule == null)
				throw new Exception(getLanguageMessage("backup.schedule.not.exists"));
			
			ScheduleYearlyRs schedules = ScheduleYearlyRs.mapToObject(name, mapSchedule);
			
			response.setScheduleYearly(schedules);
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("/yearly")
	@PUT
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response addNewYearlySchedule(ScheduleYearlyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			List<Map<String, String>> values = ScheduleYearlyCalendarRs.fillValues(schedule);
			ScheduleManager.setYearlyScheduler(schedule.getName(), values);
			uriCreated="/schedule/"+schedule.getName();
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		
		if (uriCreated != null)
			return Response.created(URI.create(uriCreated)).build();
		else
			return Response.ok(airbackRs).build();
	}
	
	
	@Path("/yearly")
	@POST
	@Consumes(value={MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_ATOM_XML})
	@Produces(MediaType.TEXT_XML)
	public Response editYearlySchedule(ScheduleYearlyRs schedule) {
		String uriCreated = null;
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			// Comprobamos que existe
			ScheduleManager.getYearlySchedule(schedule.getName());
			
			List<Map<String, String>> values = ScheduleYearlyCalendarRs.fillValues(schedule);
			ScheduleManager.setYearlyScheduler(schedule.getName(), values);
			uriCreated="/schedule/"+schedule.getName();
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
	 * Elimina una planificación
	 * @param scheduleName
	 * @return
	 */
	@Path("{scheduleName}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Response deleteSchedule(@PathParam("scheduleName") String scheduleName) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			ScheduleManager.removeSchedule(scheduleName);
			
			response.setSuccess(this.getLanguageMessage("backup.message.schedule.removed"));
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		}
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
		
	}
	
}
