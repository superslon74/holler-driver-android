package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.login.modules.LoginModule;
import com.pnrhunter.mvp.login.LoginPresenter;
import com.pnrhunter.mvp.login.EmailView;
import com.pnrhunter.mvp.login.PasswordView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {LoginModule.class})
public interface LoginComponent {
    void inject(EmailView activityEmailView);
    void inject(PasswordView activityPasswordView);

    LoginPresenter getPresenter();
}
