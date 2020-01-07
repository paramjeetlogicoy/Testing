/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.models.web.tookan;

/**
 *
 * @author dell
 */
public class Webhook_TaskHistory {
//    "id": 315555475,
//      "job_id": 50725730,
//      "fleet_id": 419408,
//      "fleet_name": "Hemraj Shaqawal",
//      "latitude": "13.0057483",
//      "longitude": "77.6507701",
//      "type": "signature_image_added",
//      "description": "https://tookan.s3.amazonaws.com/acknowledgement_images/tQXp1570706007113-tasksignature.png",
//      "extra_fields": "",
//      "creation_datetime": "2019-10-10T11:13:27.000Z",
//      "creation_date": "2019-10-10T00:00:00.000Z",
//      "label_description": "<span class='uploaded'>UPLOADED</span>by Hemraj Shaqawal"

    private long id;
    private long job_id;
    private long fleet_id;
    private String fleet_name;
    private String latitude;
    private String longitude;
    private String type;
    private String description;
    private String extra_fields;
    private String creation_datetime;
    private String creation_date;
    private String label_description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getJob_id() {
        return job_id;
    }

    public void setJob_id(long job_id) {
        this.job_id = job_id;
    }

    public long getFleet_id() {
        return fleet_id;
    }

    public void setFleet_id(long fleet_id) {
        this.fleet_id = fleet_id;
    }

    public String getFleet_name() {
        return fleet_name;
    }

    public void setFleet_name(String fleet_name) {
        this.fleet_name = fleet_name;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtra_fields() {
        return extra_fields;
    }

    public void setExtra_fields(String extra_fields) {
        this.extra_fields = extra_fields;
    }

    public String getCreation_datetime() {
        return creation_datetime;
    }

    public void setCreation_datetime(String creation_datetime) {
        this.creation_datetime = creation_datetime;
    }

    public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    public String getLabel_description() {
        return label_description;
    }

    public void setLabel_description(String label_description) {
        this.label_description = label_description;
    }

    @Override
    public String toString() {
        return "Webhook_TaskHistory{" + "id=" + id + ", job_id=" + job_id + ", fleet_id=" + fleet_id + ", fleet_name=" + fleet_name + ", latitude=" + latitude + ", longitude=" + longitude + ", type=" + type + ", description=" + description + ", extra_fields=" + extra_fields + ", creation_datetime=" + creation_datetime + ", creation_date=" + creation_date + ", label_description=" + label_description + '}';
    }

}
