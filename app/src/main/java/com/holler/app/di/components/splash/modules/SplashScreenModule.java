package com.holler.app.di.components.splash.modules;

import android.content.Context;

import com.holler.app.di.Presenter;
import com.holler.app.di.components.app.modules.UserStorageModule;
import com.holler.app.di.components.app.modules.DeviceInfoModule;
import com.holler.app.di.components.app.modules.RetrofitModule;
import com.holler.app.mvp.splash.SplashPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class SplashScreenModule {
    private SplashPresenter.View view;

    public SplashScreenModule(SplashPresenter.View view){
        this.view = view;
    }

    @Provides
    public SplashPresenter.View provideView(){
        return view;
    }

    @Provides
    public Presenter providePresenter(Context context,
                                      SplashPresenter.View splashScreenView,
                                      RetrofitModule.ServerAPI serverAPI,
                                      DeviceInfoModule.DeviceInfo deviceInfo,
                                      UserStorageModule.UserStorage userStorage){

        return new SplashPresenter(context,splashScreenView,serverAPI, deviceInfo, userStorage);
    }
}
