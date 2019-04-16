package com.holler.app.di;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class SplashScreenModule {
    private SplashScreenPresenter.View view;

    public SplashScreenModule(SplashScreenPresenter.View view){
        this.view = view;
    }

    @Provides
    public SplashScreenPresenter.View provideView(){
        return view;
    }

    @Provides
    public Presenter providePresenter(Context context,
                                      SplashScreenPresenter.View splashScreenView,
                                      RetrofitModule.ServerAPI serverAPI,
                                      DeviceInfoModule.DeviceInfo deviceInfo,
                                      UserStorageModule.UserStorage userStorage){

        return new SplashScreenPresenter(context,splashScreenView,serverAPI, deviceInfo, userStorage);
    }
}
