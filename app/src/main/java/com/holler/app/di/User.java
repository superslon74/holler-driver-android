package com.holler.app.di;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User{
    @Expose
    @SerializedName("id")
    public String id;
    @Expose
    @SerializedName("device_type")
    public String deviceType;
    @Expose
    @SerializedName("device_id")
    public String deviceId;
    @Expose
    @SerializedName("device_token")
    public String deviceToken;
    @Expose
    @SerializedName("login_by")
    public String loggedBy;
    @Expose
    @SerializedName("first_name")
    public String firstName;
    @Expose
    @SerializedName("last_name")
    public String lastName;
    @Expose
    @SerializedName("gender")
    public String gender;
    @Expose
    @SerializedName("mobile")
    public String mobile;
    @Expose
    @SerializedName("avatar")
    public String avatar;

    @Expose
    @SerializedName("status")
    public String status;
    @Expose
    @SerializedName("currency")
    public String currency;
    @Expose
    @SerializedName("sos")
    public String sos;
    @Expose
    @SerializedName("service")
    public ServiceType service;

    @Expose
    @SerializedName("email")
    public String email;
    @Expose
    @SerializedName("password")
    public String password;
    @Expose
    @SerializedName("password_confirmation")
    public String passwordConfirmation;


    public void setServiceName(String name){
        if(service == null)
            service = new ServiceType();
        service.name = name;
    }

    public String getServiceName(){
        if(service == null)
            return null;
        return service.name;
    }

    public String getCurrency(){
        if(currency!=null && !currency.isEmpty()) return currency;
        return "$";
    }
}