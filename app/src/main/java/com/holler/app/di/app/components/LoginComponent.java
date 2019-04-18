package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.login.LoginModule;
import com.holler.app.mvp.login.LoginPresenter;
import com.holler.app.mvp.login.LoginView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {LoginModule.class})
public interface LoginComponent {
//    void inject(LoginView activityLoginView);
//
//    LoginPresenter getPresenter();
}
