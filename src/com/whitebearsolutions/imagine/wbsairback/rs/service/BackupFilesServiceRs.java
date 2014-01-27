package com.whitebearsolutions.imagine.wbsairback.rs.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.net.URLCodec;

import com.whitebearsolutions.imagine.wbsairback.backup.ClientManager;
import com.whitebearsolutions.imagine.wbsairback.backup.FileManager;
import com.whitebearsolutions.imagine.wbsairback.backup.JobManager;
import com.whitebearsolutions.imagine.wbsairback.bacula.BackupOperator;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.BackupFileRs;
import com.whitebearsolutions.imagine.wbsairback.rs.model.backup.JobArchievedRs;


@Path("/files")
public class BackupFilesServiceRs extends WbsImagineServiceRs {

	private FileManager fm = null;
	private BackupOperator bo = null;
	private URLCodec encoder = null;
	private JobManager jm = null;
	private ClientManager cm = null;
	
	/**
	 * Metodo que inicializa la configuracion y la respuesta general, gestiona sesión y autenticación
	 * @throws Exception
	 */
	private Response initParams(List<String> category) {
		Response r = this.init(category);
		if (this.init(category) != null)
			return r;
		try {
			fm = new FileManager(this.sessionManager.getConfiguration());
			bo = new BackupOperator(this.sessionManager.getConfiguration());
			jm = new JobManager(this.sessionManager.getConfiguration());
			cm = new ClientManager(this.sessionManager.getConfiguration());
			encoder = new URLCodec("UTF-8"); 
			return null;
		} catch (Exception ex) {
			response.setError("Error initializing requested section: "+ex.getMessage());
			airbackRs.setResponse(response);
			return Response.ok(airbackRs).build();
		}
	}
	
	
	/**
	 * Listado de archivos en copia de seguridad de un cliente
	 * @return
	 */
	/*@Path("{clientId}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getAllFiles(@PathParam("clientId") Integer clientId, @QueryParam("max_entries") Integer max_entries, @QueryParam("pag") Integer pag ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (clientId != null && clientId > 0) {
				String clientName = cm.getClientName(clientId);
				if (clientName == null || clientName.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_client"));
			}
			
			int max = 0;
			int offset = 0;
			if (max_entries != null && max_entries > 0 && pag != null && pag > 0) {
				max = max_entries;
				offset = (pag * max) - max;
			}
			
			List<BackupFileRs> files = new ArrayList<BackupFileRs>();
			List<Map<String, String>> mapFiles = fm.getClientFiles(clientId, null, max, offset);
			if (mapFiles != null && mapFiles.size()>0)
				files = BackupFileRs.listMapToObject(mapFiles);
				
			response.setFiles(files);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}*/
	
	/**
	 * Listado de archivos en copia de seguridad de un cliente
	 * @return
	 */
	@Path("{clientId}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getFiles(@PathParam("clientId") Integer clientId, @QueryParam("directory") String directory, @QueryParam("max_entries") Integer max_entries, @QueryParam("pag") Integer pag ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			String clientName = null;
			if (clientId != null && clientId > 0) {
				clientName = cm.getClientName(clientId);
				if (clientName == null || clientName.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_client"));
			}
			
			int max = 0;
			int offset = 0;
			if (max_entries != null && max_entries > 0 && pag != null && pag > 0) {
				max = max_entries;
				offset = (pag * max) - max;
			}
			
			if (directory != null)
				directory = encoder.decode(directory, "UTF-8");
			
			List<BackupFileRs> files = new ArrayList<BackupFileRs>();
			List<Map<String, String>> mapFiles = fm.getClientFiles(clientId, clientName, directory, max, offset);
			if (mapFiles != null && mapFiles.size()>0)
				files = BackupFileRs.listMapToObject(mapFiles);
				
			response.setFiles(files);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Listado de jobs que han hecho backup de cierto archivo de cierto cliente
	 * @param clientId
	 * @param file
	 * @param max_entries
	 * @param pag
	 * @return
	 */
	@Path("{clientId}/jobs")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getJobs(@PathParam("clientId") Integer clientId, @QueryParam("file") String file, @QueryParam("max_entries") Integer max_entries, @QueryParam("pag") Integer pag ) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;

			if (clientId != null && clientId > 0) {
				String clientName = cm.getClientName(clientId);
				if (clientName == null || clientName.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_client"));
			}
			
			int max = 0;
			int offset = 0;
			if (max_entries != null && max_entries > 0 && pag != null && pag > 0) {
				max = max_entries;
				offset = (pag * max) - max;
			}
			
			if (file == null || file.isEmpty()) {
				throw new Exception(getLanguageMessage("common.message.no_file"));
			}
			
			file = encoder.decode(file, "UTF-8");
			
			List<JobArchievedRs> jobs = new ArrayList<JobArchievedRs>();
			List<Map<String, String>> mapJobs = fm.getJobsForFile(clientId, file, max, offset);
			if (mapJobs != null && mapJobs.size()>0)
				jobs = JobArchievedRs.listMapToObject(mapJobs);
			
			response.setJobsArchieved(jobs);
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	/**
	 * Lanza una restauración por versión de job
	 * @param clientId
	 * @param file
	 * @param jobId
	 * @param destinationClientId
	 * @param destinationPath
	 * @param lv
	 * @param share
	 * @param prescript
	 * @param postscript
	 * @return
	 */
	@Path("{clientId}/restore/version/{jobId}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response restoreByVersion(@PathParam("clientId") Integer clientId, @PathParam("jobId") Integer jobId, @QueryParam("file") String file, @QueryParam("destinationClientId") Integer destinationClientId, @QueryParam("destinationPath") String destinationPath, @QueryParam("lv") String lv, @QueryParam("share") String share, @QueryParam("prescript") String prescript, @QueryParam("postscript") String postscript) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (clientId != null && clientId > 0) {
				String clientName = cm.getClientName(clientId);
				if (clientName == null || clientName.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_client"));
			}
			
			if (destinationClientId != null && destinationClientId > 0) {
				String clientName = cm.getClientName(destinationClientId);
				if (clientName == null || clientName.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_client")+"(destination client)");
			}
			
			if (jobId != null && jobId > 0) {
				Map<String, String> job = jm.getJob(jobId);
				if (job == null || job.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_job"));
			}
			
			if (file == null || file.isEmpty()) {
				throw new Exception(getLanguageMessage("common.message.no_file"));
			}
			
			file = encoder.decode(file, "UTF-8");
			
			if (destinationPath != null && !destinationPath.isEmpty())
				destinationPath = encoder.decode(destinationPath, "UTF-8");
			if (postscript != null && !postscript.isEmpty())
				postscript = encoder.decode(postscript, "UTF-8");
			if (destinationPath != null && !destinationPath.isEmpty())
				prescript = encoder.decode(prescript, "UTF-8");
			
			bo.restoreFile(clientId, jobId, file, destinationClientId, lv, share, destinationPath, postscript, prescript);
			
			response.setSuccess(getLanguageMessage("backup.files.rs.successfull.restore"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
	@Path("{clientId}/restore/date")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Response restoreByDate(@PathParam("clientId") Integer clientId, @QueryParam("date") String date, @QueryParam("file") List<String> files, @QueryParam("destinationClientId") Integer destinationClientId, @QueryParam("destinationPath") String destinationPath, @QueryParam("lv") String lv, @QueryParam("share") String share, @QueryParam("prescript") String prescript, @QueryParam("postscript") String postscript) {
		try {
			Response rError = this.initParams(null);
			if (rError != null)
				return rError;
			
			if (clientId != null && clientId > 0) {
				String clientName = cm.getClientName(clientId);
				if (clientName == null || clientName.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_client"));
			}
			
			if (destinationClientId != null && destinationClientId > 0) {
				String clientName = cm.getClientName(destinationClientId);
				if (clientName == null || clientName.isEmpty())
					throw new Exception(getLanguageMessage("common.message.no_client")+"(destination client)");
			}
			
			if (files == null || files.isEmpty()) {
				throw new Exception(getLanguageMessage("common.message.no_file"));
			}
			
			List<String> decodedFiles = new ArrayList<String>();
			for (String file : files)
				decodedFiles.add(encoder.decode(file, "UTF-8"));
			
			Calendar cal = Calendar.getInstance();
			if (date != null && !date.isEmpty()) {
				DateFormat df_in = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
				try {
					Date formatedDate = df_in.parse(encoder.decode(date, "UTF-8"));
					cal.setTime(formatedDate);
				} catch(NumberFormatException _ex) {
					throw new Exception(getLanguageMessage("common.message.no_date")+". format: yyyy-MM-dd_HH:mm:ss");
				}
			} else {
				throw new Exception(getLanguageMessage("common.message.no_date")+". format: yyyy-MM-dd_HH:mm:ss");
			}
			
			if (destinationPath != null && !destinationPath.isEmpty())
				destinationPath = encoder.decode(destinationPath, "UTF-8");
			if (postscript != null && !postscript.isEmpty())
				postscript = encoder.decode(postscript, "UTF-8");
			if (destinationPath != null && !destinationPath.isEmpty())
				prescript = encoder.decode(prescript, "UTF-8");

			bo.restoreFiles(clientId, decodedFiles, destinationClientId, lv, share, destinationPath, cal, postscript, prescript);
			
			response.setSuccess(getLanguageMessage("backup.files.rs.successfull.restore"));
			
		} catch (Exception ex) {
			response.setError("Error: "+ex.getMessage());
		} 
		airbackRs.setResponse(response);
		return Response.ok(airbackRs).build();
	}
	
	
}
