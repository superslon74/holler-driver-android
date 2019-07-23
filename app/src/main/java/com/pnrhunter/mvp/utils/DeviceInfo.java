package com.pnrhunter.mvp.utils;

public class DeviceInfo {

    public static final String TYPE_ANDROID = "android";
    public static final String ID_NOT_FOUND = "COULD NOT GET ID";
    public static final String TOKEN_NOT_FOUND = "COULD NOT GET TOKEN";

    private String type;
    private String id;
    private String token;



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
