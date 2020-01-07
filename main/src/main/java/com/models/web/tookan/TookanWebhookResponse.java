/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.models.web.tookan;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 *
 * @author dell
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TookanWebhookResponse {

	private long job_id;
	private String order_id;
	private long team_id;
	private String job_hash;

	

	private String job_description;
	private String customer_email;
	private String customer_username;
	private String customer_phone;
	private String job_address;
	private String creation_datetime;
	private long fleet_id;
	private int job_status;

	private long customer_id;
	private String fleet_name;
	private String fleet_email;
	private String fleet_phone;
	private List<Webhook_TaskHistory> task_history;
	private String job_state;
	private String task_state;
	private String completed_datetime_formatted;
	private String started_datetime_formatted;
	private String arrived_datetime_formatted;
	private String acknowledged_datetime_formatted;
	private String job_token;
	private String job_time_formatted;
	private String total_time_spent_at_task_till_completion;
	private String job_pickup_datetime_formatted;
	private String job_delivery_datetime_formatted;
	private String total_distance;
	private int webhook_type;
	private String format;
	private String template_key;
	private String is_internal;
	private String full_tracking_link;
    
	public String getCustomer_username() {
		return customer_username;
	}

	public void setCustomer_username(String customer_username) {
		this.customer_username = customer_username;
	}
	public int getJob_status() {
		return job_status;
	}

	public void setJob_status(int job_status) {
		this.job_status = job_status;
	}

	public String getCustomer_phone() {
		return customer_phone;
	}

	public void setCustomer_phone(String customer_phone) {
		this.customer_phone = customer_phone;
	}

	public String getJob_address() {
		return job_address;
	}

	public void setJob_address(String job_address) {
		this.job_address = job_address;
	}

	public String getCreation_datetime() {
		return creation_datetime;
	}

	public void setCreation_datetime(String creation_datetime) {
		this.creation_datetime = creation_datetime;
	}

	public long getFleet_id() {
		return fleet_id;
	}

	public void setFleet_id(long fleet_id) {
		this.fleet_id = fleet_id;
	}

	public long getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(long customer_id) {
		this.customer_id = customer_id;
	}

	public String getFleet_name() {
		return fleet_name;
	}

	public void setFleet_name(String fleet_name) {
		this.fleet_name = fleet_name;
	}

	public String getFleet_email() {
		return fleet_email;
	}

	public void setFleet_email(String fleet_email) {
		this.fleet_email = fleet_email;
	}

	public String getFleet_phone() {
		return fleet_phone;
	}

	public void setFleet_phone(String fleet_phone) {
		this.fleet_phone = fleet_phone;
	}

	public List<Webhook_TaskHistory> getTask_history() {
		return task_history;
	}

	public void setTask_history(List<Webhook_TaskHistory> task_history) {
		this.task_history = task_history;
	}

	public String getJob_state() {
		return job_state;
	}

	public void setJob_state(String job_state) {
		this.job_state = job_state;
	}

	public String getTask_state() {
		return task_state;
	}

	public void setTask_state(String task_state) {
		this.task_state = task_state;
	}

	public String getCompleted_datetime_formatted() {
		return completed_datetime_formatted;
	}

	public void setCompleted_datetime_formatted(String completed_datetime_formatted) {
		this.completed_datetime_formatted = completed_datetime_formatted;
	}

	public String getStarted_datetime_formatted() {
		return started_datetime_formatted;
	}

	public void setStarted_datetime_formatted(String started_datetime_formatted) {
		this.started_datetime_formatted = started_datetime_formatted;
	}

	public String getArrived_datetime_formatted() {
		return arrived_datetime_formatted;
	}

	public void setArrived_datetime_formatted(String arrived_datetime_formatted) {
		this.arrived_datetime_formatted = arrived_datetime_formatted;
	}

	public String getAcknowledged_datetime_formatted() {
		return acknowledged_datetime_formatted;
	}

	public void setAcknowledged_datetime_formatted(String acknowledged_datetime_formatted) {
		this.acknowledged_datetime_formatted = acknowledged_datetime_formatted;
	}

	public String getJob_token() {
		return job_token;
	}

	public void setJob_token(String job_token) {
		this.job_token = job_token;
	}

	public String getJob_time_formatted() {
		return job_time_formatted;
	}

	public void setJob_time_formatted(String job_time_formatted) {
		this.job_time_formatted = job_time_formatted;
	}

	public String getTotal_time_spent_at_task_till_completion() {
		return total_time_spent_at_task_till_completion;
	}

	public void setTotal_time_spent_at_task_till_completion(String total_time_spent_at_task_till_completion) {
		this.total_time_spent_at_task_till_completion = total_time_spent_at_task_till_completion;
	}

	public String getJob_pickup_datetime_formatted() {
		return job_pickup_datetime_formatted;
	}

	public void setJob_pickup_datetime_formatted(String job_pickup_datetime_formatted) {
		this.job_pickup_datetime_formatted = job_pickup_datetime_formatted;
	}

	public String getJob_delivery_datetime_formatted() {
		return job_delivery_datetime_formatted;
	}

	public void setJob_delivery_datetime_formatted(String job_delivery_datetime_formatted) {
		this.job_delivery_datetime_formatted = job_delivery_datetime_formatted;
	}

	public String getTotal_distance() {
		return total_distance;
	}

	public void setTotal_distance(String total_distance) {
		this.total_distance = total_distance;
	}

	public int getWebhook_type() {
		return webhook_type;
	}

	public void setWebhook_type(int webhook_type) {
		this.webhook_type = webhook_type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getTemplate_key() {
		return template_key;
	}

	public void setTemplate_key(String template_key) {
		this.template_key = template_key;
	}

	public String getIs_internal() {
		return is_internal;
	}

	public void setIs_internal(String is_internal) {
		this.is_internal = is_internal;
	}

	public String getFull_tracking_link() {
		return full_tracking_link;
	}

	public void setFull_tracking_link(String full_tracking_link) {
		this.full_tracking_link = full_tracking_link;
	}

	public long getJob_id() {
		return job_id;
	}

	public void setJob_id(long job_id) {
		this.job_id = job_id;
	}

	public String getOrder_id() {
		return order_id;
	}

	public void setOrder_id(String order_id) {
		this.order_id = order_id;
	}

	public long getTeam_id() {
		return team_id;
	}

	public void setTeam_id(long team_id) {
		this.team_id = team_id;
	}

	public String getJob_hash() {
		return job_hash;
	}

	public void setJob_hash(String job_hash) {
		this.job_hash = job_hash;
	}

	public String getJob_description() {
		return job_description;
	}

	public void setJob_description(String job_description) {
		this.job_description = job_description;
	}

	public String getCustomer_email() {
		return customer_email;
	}

	public void setCustomer_email(String customer_email) {
		this.customer_email = customer_email;
	}

	@Override
	public String toString() {
		return "TookanWebhookResponse{" + "job_id=" + job_id + ", order_id=" + order_id + ", team_id=" + team_id
				+ ", job_hash=" + job_hash + ", job_description=" + job_description + ", customer_email="
				+ customer_email + ", customer_phone=" + customer_phone + ", job_address=" + job_address
				+ ", creation_datetime=" + creation_datetime + ", fleet_id=" + fleet_id + ", customer_id=" + customer_id
				+ ", fleet_name=" + fleet_name + ", fleet_email=" + fleet_email + ", fleet_phone=" + fleet_phone
				+ ", task_history=" + task_history + ", job_state=" + job_state + ", task_state=" + task_state
				+ ", completed_datetime_formatted=" + completed_datetime_formatted + ", started_datetime_formatted="
				+ started_datetime_formatted + ", arrived_datetime_formatted=" + arrived_datetime_formatted
				+ ", acknowledged_datetime_formatted=" + acknowledged_datetime_formatted + ", job_token=" + job_token
				+ ", job_time_formatted=" + job_time_formatted + ", total_time_spent_at_task_till_completion="
				+ total_time_spent_at_task_till_completion + ", job_pickup_datetime_formatted="
				+ job_pickup_datetime_formatted + ", job_delivery_datetime_formatted=" + job_delivery_datetime_formatted
				+ ", total_distance=" + total_distance + ", webhook_type=" + webhook_type + ", format=" + format
				+ ", template_key=" + template_key + ", is_internal=" + is_internal + ", full_tracking_link="
				+ full_tracking_link + '}';
	}

}
