package com.holler.app.di.app.components.password.modules;

import android.content.Context;

import com.holler.app.activity.ChangePassword;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.login.EmailView;
import com.holler.app.mvp.login.LoginPresenter;
import com.holler.app.mvp.login.PasswordView;
import com.holler.app.mvp.password.ChangePasswordPresenter;
import com.holler.app.mvp.password.ChangePasswordView;
import com.holler.app.mvp.password.ForgotPasswordView;

import dagger.Module;
import dagger.Provides;

@Module
public class PasswordModule {
    private static ChangePasswordPresenter presenter = null;

    private ChangePasswordPresenter.View view;

    public PasswordModule(ChangePasswordView view){
        this.view = (ChangePasswordPresenter.View) view;
    }

    public PasswordModule(ForgotPasswordView view){
        this.view = (ChangePasswordPresenter.View) view;
    }

    @Provides
    public ChangePasswordPresenter.View provideLoginView(){
        return view;
    }

    @Provides
    public ChangePasswordPresenter provideLoginPresenter(ChangePasswordPresenter.View view,
                                                RouterModule.Router router,
                                                Context context,
                                                UserStorageModule.UserStorage userStorage,
                                                DeviceInfoModule.DeviceInfo deviceInfo,
                                                RetrofitModule.ServerAPI serverAPI){

        if(presenter==null){
            presenter = new ChangePasswordPresenter(router,context, userStorage,deviceInfo,serverAPI);
        }
        presenter.setView(view);
        return presenter;
    }
}
