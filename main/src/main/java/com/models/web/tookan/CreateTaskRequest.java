/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.models.web.tookan;

import java.util.List;

/**
 *
 * @author dell
 */
public class CreateTaskRequest {

    private String api_key;
    private String order_id;
    private String job_description;
    private String customer_email;
    private String customer_username;
    private String customer_phone;
    private String customer_address;
    private String latitude;
    private String longitude;
    private String job_delivery_datetime;
    private String custom_field_template;
    private List<MetaData_CreateTask> meta_data = null;
    private String team_id;
    private String auto_assignment;
    private String has_pickup;
    private String has_delivery;
    private String layout_type;
    private int tracking_link;
    private String timezone;
    private String fleet_id;
    private String[] ref_images;
    private int notify;
    private String tags;
    private int geofence;

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
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

    public String getCustomer_username() {
        return customer_username;
    }

    public void setCustomer_username(String customer_username) {
        this.customer_username = customer_username;
    }

    public String getCustomer_phone() {
        return customer_phone;
    }

    public void setCustomer_phone(String customer_phone) {
        this.customer_phone = customer_phone;
    }

    public String getCustomer_address() {
        return customer_address;
    }

    public void setCustomer_address(String customer_address) {
        this.customer_address = customer_address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getJob_delivery_datetime() {
        return job_delivery_datetime;
    }

    public void setJob_delivery_datetime(String job_delivery_datetime) {
        this.job_delivery_datetime = job_delivery_datetime;
    }

    public String getCustom_field_template() {
        return custom_field_template;
    }

    public void setCustom_field_template(String custom_field_template) {
        this.custom_field_template = custom_field_template;
    }

    public List<MetaData_CreateTask> getMeta_data() {
        return meta_data;
    }

    public void setMeta_data(List<MetaData_CreateTask> meta_data) {
        this.meta_data = meta_data;
    }

    public String getTeam_id() {
        return team_id;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getAuto_assignment() {
        return auto_assignment;
    }

    public void setAuto_assignment(String auto_assignment) {
        this.auto_assignment = auto_assignment;
    }

    public String getHas_pickup() {
        return has_pickup;
    }

    public void setHas_pickup(String has_pickup) {
        this.has_pickup = has_pickup;
    }

    public String getHas_delivery() {
        return has_delivery;
    }

    public void setHas_delivery(String has_delivery) {
        this.has_delivery = has_delivery;
    }

    public String getLayout_type() {
        return layout_type;
    }

    public void setLayout_type(String layout_type) {
        this.layout_type = layout_type;
    }

    public int getTracking_link() {
        return tracking_link;
    }

    public void setTracking_link(int tracking_link) {
        this.tracking_link = tracking_link;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getFleet_id() {
        return fleet_id;
    }

    public void setFleet_id(String fleet_id) {
        this.fleet_id = fleet_id;
    }

    public String[] getRef_images() {
        return ref_images;
    }

    public void setRef_images(String[] ref_images) {
        this.ref_images = ref_images;
    }

    public int getNotify() {
        return notify;
    }

    public void setNotify(int notify) {
        this.notify = notify;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getGeofence() {
        return geofence;
    }

    public void setGeofence(int geofence) {
        this.geofence = geofence;
    }

    @Override
    public String toString() {
        return "CreateTaskRequest{" + "api_key=" + api_key + ", order_id=" + order_id + ", job_description=" + job_description + ", customer_email=" + customer_email + ", customer_username=" + customer_username + ", customer_phone=" + customer_phone + ", customer_address=" + customer_address + ", latitude=" + latitude + ", longitude=" + longitude + ", job_delivery_datetime=" + job_delivery_datetime + ", custom_field_template=" + custom_field_template + ", meta_data=" + meta_data + ", team_id=" + team_id + ", auto_assignment=" + auto_assignment + ", has_pickup=" + has_pickup + ", has_delivery=" + has_delivery + ", layout_type=" + layout_type + ", tracking_link=" + tracking_link + ", timezone=" + timezone + ", fleet_id=" + fleet_id + ", ref_images=" + ref_images + ", notify=" + notify + ", tags=" + tags + ", geofence=" + geofence + '}';
    }

  

}
