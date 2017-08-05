package com.luvbrite.web.controller.admin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.mongodb.morphia.aggregation.Accumulator;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.luvbrite.web.models.SurveyFormatted;
import com.luvbrite.web.models.SurveyQuestion;
import com.luvbrite.web.models.pipelines.surveys.SurveyAggregate;

import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.aggregation.Group.id;
import static org.mongodb.morphia.aggregation.Group.sum;

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
	
	
	@RequestMapping(value = "/questions/{surveyId}", method = RequestMethod.GET)
	public @ResponseBody GenericResponse questions(@PathVariable Long surveyId){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		if(surveyId == null){
			r.setMessage("Invalid Survey ID");
			return r;
		}
			
		List<SurveyQuestion> surveyQuestions = surveyQuestionDao.createQuery().order("_id").asList();
		
		r.setSuccess(true);
		r.setResults(surveyQuestions);
		
		return r;		
	}
	
	
	@RequestMapping(value = "/aggregate/{surveyId}", method = RequestMethod.GET)
	public @ResponseBody GenericResponse surveyAggregate(@PathVariable Long surveyId){
		
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		if(surveyId == null){
			r.setMessage("Invalid Survey ID");
			return r;
		}
			
		List<SurveyAggregate> saList = new ArrayList<>();
		AggregationPipeline pipeline = surveyFormattedDao.getDatastore()
				.createAggregation(SurveyFormatted.class)
				.group(id(grouping("sid"), grouping("qid"),grouping("resp")), 
						grouping("sum", new Accumulator("$sum", "value")))
				.sort(new Sort("_id.qid", 1));
		
		long totalSurveys = surveyResponseDao.createQuery()
					.field("sid").equal(surveyId).countAll();
		
		/**
		 * db.surveyformatted.aggregate([
		 * 	{$group : {_id : {sid:"$sid", qid : "$qid" , resp : "$resp"}, sum : {$sum : "$value"}}}, 
		 * 	{$sort : {"_id.qid" : 1}}
		 * ]);
		 */
		
		Iterator<SurveyAggregate> iterator = pipeline.aggregate(SurveyAggregate.class);
		while(iterator.hasNext()) {
			saList.add(iterator.next());
		}
		
		r.setResults(saList);
		r.setSuccess(true);
		r.setMessage(totalSurveys+"");
		
		return r;		
	}
}
