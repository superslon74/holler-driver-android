package com.holler.app.di.app.components.login;

import com.holler.app.di.app.WTF;
import com.holler.app.mvp.login.LoginPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginModule {
    private LoginPresenter.View view;

    public LoginModule(LoginPresenter.View view){
        this.view = view;
    }

    @Provides
    public LoginPresenter.View provideLoginView(){
        return view;
    }

    @Provides
    public LoginPresenter provideLoginPresenter(){
        return new LoginPresenter();
    }
}
