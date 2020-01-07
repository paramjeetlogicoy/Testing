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
public class TookanResponse {
    private String message;
    private String status;
    private CreateTaskResponse_Data data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CreateTaskResponse_Data getData() {
        return data;
    }

    public void setData(CreateTaskResponse_Data data) {
        this.data = data;
    }
    
    
}
