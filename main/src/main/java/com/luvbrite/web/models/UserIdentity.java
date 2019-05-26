package com.luvbrite.web.models;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class UserIdentity {
	
	private String idCard;
	private String recomendation;
	private Date recoExpiry;
	private String stateMarijuanaCard;

        public String getStateMarijuanaCard() {
            return stateMarijuanaCard;
        }

        public void setStateMarijuanaCard(String stateMarijuanaCard) {
            this.stateMarijuanaCard = stateMarijuanaCard;
        }
        
	public String getIdCard() {
		return idCard;
	}
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	public String getRecomendation() {
		return recomendation;
	}
	public void setRecomendation(String recomendation) {
		this.recomendation = recomendation;
	}
	public Date getRecoExpiry() {
		return recoExpiry;
	}
	public void setRecoExpiry(Date recoExpiry) {
		this.recoExpiry = recoExpiry;
	}
}
