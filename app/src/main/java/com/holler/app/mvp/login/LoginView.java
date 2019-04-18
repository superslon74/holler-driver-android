package com.holler.app.mvp.login;

import android.os.Bundle;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;

import com.holler.app.utils.CustomActivity;

import javax.inject.Inject;

public class LoginView extends CustomActivity implements LoginPresenter.View{

    @Inject public LoginPresenter presenter;

    private void setupComponent(){
        AppComponent appComponent = (AppComponent) AndarApplication.getInstance().component();
//        DaggerLoginComponent.builder()
//                .appComponent(appComponent)
//                .loginModule(new LoginModule(this))
//                .build()
//                .inject(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

    }
}
