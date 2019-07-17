package com.pnrhunter.di.app.components.login.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.di.app.modules.UserStorageModule;
import com.pnrhunter.mvp.login.EmailView;
import com.pnrhunter.mvp.login.LoginPresenter;
import com.pnrhunter.mvp.login.PasswordView;
import com.pnrhunter.mvp.main.UserModel;

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
                                                RetrofitModule.ServerAPI serverAPI,
                                                UserModel userModel){

        if(loginPresenter==null){
            loginPresenter = new LoginPresenter(router,context, userStorage,deviceInfo,serverAPI, userModel);
        }
        loginPresenter.setView(view);
        return loginPresenter;
    }
}
