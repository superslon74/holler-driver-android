package com.holler.app.di;

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
    public Presenter providePresenter(SplashScreenPresenter.View splashScreenView,
                                      RetrofitModule.ServerAPI serverAPI,
                                      DeviceInfoModule.DeviceInfo deviceInfo){

        return new SplashScreenPresenter(splashScreenView,serverAPI, deviceInfo);
    }
}
