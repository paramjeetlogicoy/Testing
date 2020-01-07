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
public class MetaData_CreateTask {

    private String label;
    private String data;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MetaData_CreateTask{" + "label=" + label + ", data=" + data + '}';
    }
    
    
}
