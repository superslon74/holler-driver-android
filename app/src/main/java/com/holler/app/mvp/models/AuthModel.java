package com.holler.app.mvp.models;

public class AuthModel {




    private interface Authenticatable{
        void login();
        void register();
        void logout();
    }
}

