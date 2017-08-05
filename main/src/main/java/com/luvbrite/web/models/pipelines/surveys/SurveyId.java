package com.luvbrite.web.models.pipelines.surveys;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class SurveyId {

	private Long sid;
	private Long qid;
	private String resp;
	
	public Long getSid() {
		return sid;
	}
	public void setSid(Long sid) {
		this.sid = sid;
	}
	public Long getQid() {
		return qid;
	}
	public void setQid(Long qid) {
		this.qid = qid;
	}
	public String getResp() {
		return resp;
	}
	public void setResp(String resp) {
		this.resp = resp;
	}
}
