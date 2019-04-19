package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.login.modules.LoginModule;
import com.holler.app.di.app.components.register.modules.RegisterModule;
import com.holler.app.mvp.login.EmailView;
import com.holler.app.mvp.login.LoginPresenter;
import com.holler.app.mvp.login.PasswordView;
import com.holler.app.mvp.register.RegisterPresenter;
import com.holler.app.mvp.register.RegisterView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {RegisterModule.class})
public interface RegisterComponent {
    void inject(RegisterView activityRegisterView);

    RegisterPresenter getPresenter();
}
