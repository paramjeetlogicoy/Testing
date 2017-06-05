package com.luvbrite.web.controller;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.dao.SurveyDAO;
import com.luvbrite.dao.SurveyFormattedDAO;
import com.luvbrite.dao.SurveyQuestionDAO;
import com.luvbrite.dao.SurveyResponseDAO;
import com.luvbrite.services.PostOrderMeta;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.InventoryUpdates;
import com.luvbrite.web.models.SaveSurvey;
import com.luvbrite.web.models.Survey;
import com.luvbrite.web.models.SurveyFormatted;
import com.luvbrite.web.models.SurveyQuestion;
import com.luvbrite.web.models.SurveyResponse;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
@RequestMapping(value = "/survey")
public class SurveyController {

	@Autowired
	private SurveyDAO surveyDao;

	@Autowired
	private SurveyQuestionDAO surveyQuestionDao;

	@Autowired
	private SurveyResponseDAO surveyResponseDao;

	@Autowired
	private SurveyFormattedDAO surveyFormattedDao;
	
	@Autowired
	private PostOrderMeta postOrderMeta;
	
	
	@RequestMapping(value = "/savesurvey", method = RequestMethod.POST)
	public @ResponseBody GenericResponse savesurvey(@RequestBody SaveSurvey ss){
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		if(ss == null || 
				ss.getSfs() == null || ss.getSfs().isEmpty() || 
				ss.getSr() == null){
			gr.setMessage("Invalid Survey Responses");
			return gr;
		}
		
		SurveyResponse sr = ss.getSr();
		if(sr.getOrderNumber() != 0){
			if(surveyResponseDao.createQuery()
					.field("orderNumber").equal(sr.getOrderNumber())
					.countAll() > 0){

				gr.setMessage("Duplicate survey response! Survey has been already received for this Order. "
						+ "If you haven't received your free gram, please contact customer service.");
				return gr;
			}
		}
		
		surveyResponseDao.save(sr);
		
		List<SurveyFormatted> sfs = ss.getSfs();
		for(SurveyFormatted sf : sfs){
			surveyFormattedDao.save(sf);
		}
		
		gr.setSuccess(true);
		
		
		//Sent update to Inv
		try {
			
			InventoryUpdates invUpdts = new InventoryUpdates();
			invUpdts.setOrderNumber(sr.getOrderNumber());
			invUpdts.setAttr("additional_info");
			invUpdts.setValue("Add a free gram to the order (Completed Survey) ");
			
			postOrderMeta.updateInfo(invUpdts);
			
		}catch(Exception e){}
		
		return gr;
	}
	
	@RequestMapping(value = "/createsurvey/{surveyId}")
	public @ResponseBody GenericResponse createSurvey(
			@AuthenticationPrincipal UserDetailsExt user, 
			@PathVariable Long surveyId,
			ModelMap model) {
		
		GenericResponse gr = new GenericResponse();
		gr.setSuccess(false);
		
		if(user != null && surveyId != null){
			
			Set<String> authorities = AuthorityUtils.authorityListToSet(user.getAuthorities());
			if (authorities.contains("ROLE_EDIT")){
				
				if(surveyDao.get(surveyId) != null){
					gr.setMessage("Survery with that ID already exists!");
					return gr;
				}
				
				Survey s = new Survey();
				s.setDescription("New after checkout survey");
				s.setStarted(Calendar.getInstance().getTime());
				s.set_id(surveyId);
				
				surveyDao.save(s);
				
				long questionId = 1;
				
				SurveyQuestion sq = new SurveyQuestion();
				sq.set_id(questionId++);
				sq.setSid(surveyId);
				sq.setQuestion("Use numbers 1-5 to let us know how each quality plays a roll when shopping for flowers. "
						+ "The number 1 will represent the most important, 2 represents the second most important, and so on.");
				surveyQuestionDao.save(sq);
				
				
				sq = new SurveyQuestion();
				sq.set_id(questionId++);
				sq.setSid(surveyId);
				sq.setQuestion("Bud appearance means a lot to me.");
				surveyQuestionDao.save(sq);
				
				
				sq = new SurveyQuestion();
				sq.set_id(questionId++);
				sq.setSid(surveyId);
				sq.setQuestion("If you could choose only one category to see more of, which would it be? "
						+ "You can select 1 bolded category and up to 2 subcategories within that chosen category.");
				surveyQuestionDao.save(sq);
				
				
				sq = new SurveyQuestion();
				sq.set_id(questionId++);
				sq.setSid(surveyId);
				sq.setQuestion("The site is easy to navigate.");
				surveyQuestionDao.save(sq);
				
				
				sq = new SurveyQuestion();
				sq.set_id(questionId++);
				sq.setSid(surveyId);
				sq.setQuestion("I plan to continue purchasing through Luvbrite.");
				surveyQuestionDao.save(sq);
				
				
				sq = new SurveyQuestion();
				sq.set_id(questionId++);
				sq.setSid(surveyId);
				sq.setQuestion("I am happy with the service provided by Luvbrite. "
						+ "This includes condition of products delivered, delivery times, staff knowledge and courtesy.");
				surveyQuestionDao.save(sq);
				
				
				sq = new SurveyQuestion();
				sq.set_id(questionId++);
				sq.setSid(surveyId);
				sq.setQuestion("Luvbrite is or will be the only place where I plan on acquiring my cannabis.");
				surveyQuestionDao.save(sq);
				
				gr.setSuccess(true);
				gr.setMessage("Surveys created with ID " + surveyId);
			}
		}
		
		return gr;
	}	
	
}
