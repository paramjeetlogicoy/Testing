package com.luvbrite.web.models;

import java.util.List;

public class SaveSurvey {
	
	private SurveyResponse sr;
	private List<SurveyFormatted> sfs;
	
	public SurveyResponse getSr() {
		return sr;
	}
	public void setSr(SurveyResponse sr) {
		this.sr = sr;
	}
	public List<SurveyFormatted> getSfs() {
		return sfs;
	}
	public void setSfs(List<SurveyFormatted> sfs) {
		this.sfs = sfs;
	}
}
