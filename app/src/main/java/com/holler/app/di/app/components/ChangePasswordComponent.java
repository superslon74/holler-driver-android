package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.login.modules.LoginModule;
import com.holler.app.di.app.components.password.modules.PasswordModule;
import com.holler.app.mvp.login.EmailView;
import com.holler.app.mvp.login.LoginPresenter;
import com.holler.app.mvp.login.PasswordView;
import com.holler.app.mvp.password.ChangePasswordPresenter;
import com.holler.app.mvp.password.ChangePasswordView;
import com.holler.app.mvp.password.ForgotPasswordView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {PasswordModule.class})
public interface ChangePasswordComponent {
    void inject(ChangePasswordView activityChangePasswordView);
    void inject(ForgotPasswordView activityForgotPasswordView);

    ChangePasswordPresenter getPresenter();
}
