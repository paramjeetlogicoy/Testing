package com.luvbrite.web.models;

import java.util.List;

public class Email {

	private String recipientName;
	private String recipientEmail;
	private String subject;
	private String emailTemplate;
	private String fromEmail;	
	private String emailTitle = "Email";
	private String emailInfo = "Info about your Luvbrite Account";
	
	private List<String> ccs;
	private List<String> bccs;
	
	private Object email;

	public String getRecipientName() {
		return recipientName;
	}
	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}
	public String getRecipientEmail() {
		return recipientEmail;
	}
	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getEmailTemplate() {
		return emailTemplate;
	}
	public void setEmailTemplate(String emailTemplate) {
		this.emailTemplate = emailTemplate;
	}
	public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public String getEmailTitle() {
		return emailTitle;
	}
	public void setEmailTitle(String emailTitle) {
		this.emailTitle = emailTitle;
	}
	public String getEmailInfo() {
		return emailInfo;
	}
	public void setEmailInfo(String emailInfo) {
		this.emailInfo = emailInfo;
	}
	public Object getEmail() {
		return email;
	}
	public void setEmail(Object email) {
		this.email = email;
	}
	public List<String> getCcs() {
		return ccs;
	}
	public void setCcs(List<String> ccs) {
		this.ccs = ccs;
	}
	public List<String> getBccs() {
		return bccs;
	}
	public void setBccs(List<String> bccs) {
		this.bccs = bccs;
	}
}
