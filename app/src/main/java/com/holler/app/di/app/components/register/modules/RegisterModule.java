package com.holler.app.di.app.components.register.modules;

import android.content.Context;

import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.register.RegisterPresenter;
import com.holler.app.mvp.splash.SplashPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class RegisterModule {
    private RegisterPresenter.View view;

    public RegisterModule(RegisterPresenter.View view){
        this.view = view;
    }

    @Provides
    public RegisterPresenter.View provideView(){
        return view;
    }


    @Provides
    public RegisterPresenter providePresenter(Context context,
                                              RegisterPresenter.View view,
                                              RouterModule.Router router,
                                              UserStorageModule.UserStorage userStorage,
                                              DeviceInfoModule.DeviceInfo deviceInfo,
                                              RetrofitModule.ServerAPI serverAPI){

        return new RegisterPresenter(context,view,router,userStorage,deviceInfo,serverAPI);
    }
}
