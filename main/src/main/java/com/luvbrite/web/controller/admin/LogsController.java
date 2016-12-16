package com.luvbrite.web.controller.admin;

import java.util.Calendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.utils.PaginationLogic;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.ResponseWithPg;


@Controller
@RequestMapping(value = "/admin/logs")
public class LogsController {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LogsController.class);
	
	
	@Autowired
	private LogDAO logDao;
	
	
	@RequestMapping(value = "/{collection}/{key}", method = RequestMethod.GET)
	public @ResponseBody ResponseWithPg logs(
			@PathVariable String collection,
			@PathVariable long key,
			@RequestParam(value="p", required=false) Integer page){

		ResponseWithPg rpg = new ResponseWithPg();		
		
		int offset = 0,
			limit = 5; //itemsPerPage
		
		if(page==null) page = 1;
		if(page >1) offset = (page-1)*limit;
		
		Query<Log> query = logDao.createQuery();
		query.filter("key", key).filter("collection", collection);
		
		
		PaginationLogic pgl = new PaginationLogic((int)query.countAll(), limit, page);
		List<Log> logs = query.limit(limit).offset(offset).order("-$natural").asList();

		rpg.setSuccess(true);
		rpg .setPg(pgl.getPg());
		rpg.setRespData(logs);
		
		return rpg;	
	}
	
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public @ResponseBody String createEntry(@RequestBody Log log){
		
		if(log != null){
			
			log.setDate(Calendar.getInstance().getTime());
			logDao.save(log);
			
			return "success";
		}
		
		return "failure";	
	}
}
