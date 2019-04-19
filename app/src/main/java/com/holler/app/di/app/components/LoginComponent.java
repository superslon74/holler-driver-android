package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.login.modules.LoginModule;
import com.holler.app.mvp.login.LoginPresenter;
import com.holler.app.mvp.login.EmailView;
import com.holler.app.mvp.login.PasswordView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {LoginModule.class})
public interface LoginComponent {
    void inject(EmailView activityEmailView);
    void inject(PasswordView activityPasswordView);

    LoginPresenter getPresenter();
}
