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
public class GetJobInfo_Request {
  

private  String api_key = "2b997be77e2cc22becfd4c66426ef504";
private List<Integer> job_ids;
private int include_task_history;

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public List<Integer> getJob_ids() {
        return job_ids;
    }

    public void setJob_ids(List<Integer> job_ids) {
        this.job_ids = job_ids;
    }

    public int getInclude_task_history() {
        return include_task_history;
    }

    public void setInclude_task_history(int include_task_history) {
        this.include_task_history = include_task_history;
    }


}
