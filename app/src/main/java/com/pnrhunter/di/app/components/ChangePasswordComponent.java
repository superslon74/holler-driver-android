package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.password.modules.PasswordModule;
import com.pnrhunter.mvp.password.ChangePasswordPresenter;
import com.pnrhunter.mvp.password.ChangePasswordView;
import com.pnrhunter.mvp.password.ForgotPasswordView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {PasswordModule.class})
public interface ChangePasswordComponent {
    void inject(ChangePasswordView activityChangePasswordView);
    void inject(ForgotPasswordView activityForgotPasswordView);

    ChangePasswordPresenter getPresenter();
}
