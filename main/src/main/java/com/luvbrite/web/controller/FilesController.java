package com.luvbrite.web.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import com.luvbrite.dao.UploadDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.FileUploadResponse;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Upload;


@Controller
@RequestMapping(value = "/files")
@PropertySource("classpath:/env.properties")
public class FilesController {
	
	private static Logger logger = LoggerFactory.getLogger(FilesController.class);
	
	@Autowired
	private Environment env;

	@Autowired
	private UploadDAO dao;	
	
	
	@RequestMapping(value = "/list/", method = RequestMethod.GET)
	public @ResponseBody List<Upload> listFiles(
			@RequestParam(value="c", required=false) Integer offset,
			@RequestParam(value="l", required=false) Integer limit){
		
		if(limit==null) limit = 50;
		if(offset==null) offset = 0;
		
		return dao.find(
				dao.getDs()
					.createQuery(dao.getEntityClass())
					//.field("_id").greaterThan(6887) //Comment this line in production
					.order("-_id")
					.limit(limit)
					.offset(offset)
				).asList();	

	}
	
	
	@Cacheable("files")
	@RequestMapping(value = "/view/**", method = RequestMethod.GET)
	public void viewFile(HttpServletRequest req, HttpServletResponse resp){
		
		processFile(req, resp, false);
	}	
	
	
	@RequestMapping(value = "/get/**", method = RequestMethod.GET)
	public void downloadFile(HttpServletRequest req, HttpServletResponse resp){
		
		processFile(req, resp, true);
	}		
	
	
	@RequestMapping(value = "/delete/id/{id}", method = RequestMethod.POST)
	public @ResponseBody GenericResponse deleteWithId(@PathVariable Long id){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		r.setMessage("");
		
		if(id == null || id == 0){
			r.setMessage("Invalid file id");
			return r;
		}
		
		try {
			
			Upload u = dao.get(id);
			if(u != null){

				String basePath = env.getProperty("ftpEasyPath");			
				
				if(u.getLocType() != null && u.getLocType().equals("internal")) 
					basePath = env.getProperty("ftpControlledPath");
				
				File f = new File(basePath + u.getLocation());
				if(f.exists() && f.isFile()){
					
					if(f.delete()){
						r.setSuccess(true);
					}
				}
			}
			else{
				r.setMessage("File not found");
			}
			
			
		}catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}	
		
		
		return r;		
	}
	
	
	private void processFile(HttpServletRequest req, HttpServletResponse resp, boolean download){
		
		int contextPathLength = "/files/view".length();
		String basePath = env.getProperty("ftpControlledPath");
		
		String fullPath = (String) req.getAttribute(
		        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String filePath = fullPath.substring(contextPathLength);
		
		try {
			//Get the file
			File f = new File(basePath + filePath);
			//System.out.println("FilesController: FilePath - " + basePath + filePath);
			if(f.exists() && f.isFile()){
				
				if(download) {
					resp.setContentType("application/force-download");
				
				} else {
					
					String mimeType= URLConnection.guessContentTypeFromName(f.getName());
			        if(mimeType==null) { 
			        	mimeType = "application/octet-stream";		        
			        }
			        
					resp.setContentType(mimeType);
				}
				
				resp.setContentLength((int) f.length());
				
				FileInputStream inStream = new FileInputStream(f);
				FileCopyUtils.copy(inStream, resp.getOutputStream());
				
				resp.flushBuffer();
			}
			else{
				System.out.println("FilesController: File- " + basePath + filePath + " not reachable");
			}
			
			
		}catch(Exception e){
			logger.error(Exceptions.giveStackTrace(e));
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upload")
	public @ResponseBody FileUploadResponse handleFileUpload(
			@RequestParam("file") MultipartFile file, 
			@RequestParam(value="ctrl", required=false) String ctrl, 
			@RequestParam(value="path", required=false) String path) {
		
		
		FileUploadResponse r = new FileUploadResponse();
		r.setSuccess(false);
		

		if(!file.isEmpty()) {

			String name = file.getOriginalFilename();
			String newFileName = name.replaceAll("[^A-Za-z0-9._-]","");
			String basePath = env.getProperty("ftpEasyPath");			
			
			if(ctrl != null && ctrl.equals("controlled")) 
				basePath = env.getProperty("ftpControlledPath");
			
			
			if(path==null) path = "generic/";
			String fileLoc = basePath + path;
			
			//Check if the folder path exists, if not create it
			File dir = new File(fileLoc);
			if(!dir.exists()) dir.mkdirs();
			
			String nameWithOutExtn = newFileName.substring(0, newFileName.lastIndexOf(".")),
					extn = newFileName.substring(newFileName.lastIndexOf("."));
			
			File f = new File(fileLoc + newFileName);
			int i = 1;

			//Check if file already exists, if yes, create version numbers
			while(f.exists()){
				newFileName = nameWithOutExtn + "-" + (i++) + extn;
				f = new File(fileLoc + newFileName);
				
				//Safety 
				if(i>35) break;
			}

			try {
				
				BufferedOutputStream stream = 
						new BufferedOutputStream(new FileOutputStream(f));
				FileCopyUtils.copy(file.getInputStream(), stream);
				stream.close();


				r.setSuccess(true);
				r.setMessage("You successfully uploaded " + name + "!");				
				
				//Save the upload info into DB
				long fileId = dao.getNextSeq();
				
				Upload upload = new Upload();
				upload.set_id(fileId);
				upload.setDate(Calendar.getInstance().getTime());
				upload.setFilename(newFileName);
				upload.setLocation(path + newFileName);
				
				String mimeType= file.getContentType();
		        if(mimeType==null) { 
		        	mimeType = "application/octet-stream";		        
		        }
		        upload.setMime(mimeType);
				
				if(ctrl != null && ctrl.equals("controlled")){
					upload.setLocType("internal");
				}
				else{
					upload.setLocType("cdn");
				}
				
				dao.save(upload);
				
				r.setResults(Arrays.asList(upload));
				
			}
			catch (Exception e) {
				r.setMessage("Upload failed " + name + " => " + e.getMessage());
				logger.error(Exceptions.giveStackTrace(e));
			}
		}
		else {
			r.setMessage("Upload failed because the file was empty");
		}

		return r;
	}
}
