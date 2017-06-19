package com.luvbrite.web.controller.admin;

import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.SurveyDAO;
import com.luvbrite.dao.SurveyFormattedDAO;
import com.luvbrite.dao.SurveyQuestionDAO;
import com.luvbrite.dao.SurveyResponseDAO;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Survey;


@Controller
@RequestMapping(value = "/admin/surveys")
public class SurveysController {
	
	private static Logger logger = Logger.getLogger(SurveysController.class);
		
	@Autowired
	private LogDAO logDao;

	@Autowired
	private SurveyDAO surveyDao;

	@Autowired
	private SurveyQuestionDAO surveyQuestionDao;

	@Autowired
	private SurveyResponseDAO surveyResponseDao;

	@Autowired
	private SurveyFormattedDAO surveyFormattedDao;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String mainPage(ModelMap model){		
		return "admin/surveys";		
	}
	
	
	@RequestMapping(value = "/json/", method = RequestMethod.GET)
	public @ResponseBody GenericResponse elements(){
		
		GenericResponse r = new GenericResponse();
			
		List<Survey> surveys = surveyDao.createQuery().order("-_id").asList();
		
		r.setSuccess(true);
		r.setResults(surveys);
		
		return r;		
	}
}
