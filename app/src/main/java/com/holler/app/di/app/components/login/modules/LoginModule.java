package com.holler.app.di.app.components.login.modules;

import android.content.Context;

import com.holler.app.di.app.WTF;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.login.EmailView;
import com.holler.app.mvp.login.LoginPresenter;
import com.holler.app.mvp.login.PasswordView;

import dagger.Module;
import dagger.Provides;

@Module
public class LoginModule {
    private static LoginPresenter loginPresenter = null;

    private LoginPresenter.View view;

    public LoginModule(EmailView view){
        this.view = (LoginPresenter.View) view;
    }

    public LoginModule(PasswordView view){
        this.view = (LoginPresenter.View) view;
    }

    @Provides
    public LoginPresenter.View provideLoginView(){
        return view;
    }

    @Provides
    public LoginPresenter provideLoginPresenter(LoginPresenter.View view,
                                                RouterModule.Router router,
                                                Context context,
                                                UserStorageModule.UserStorage userStorage,
                                                DeviceInfoModule.DeviceInfo deviceInfo,
                                                RetrofitModule.ServerAPI serverAPI){

        if(loginPresenter==null){
            loginPresenter = new LoginPresenter(router,context, userStorage,deviceInfo,serverAPI);
        }
        loginPresenter.setView(view);
        return loginPresenter;
    }
}
