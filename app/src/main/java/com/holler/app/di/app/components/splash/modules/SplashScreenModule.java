package com.holler.app.di.app.components.splash.modules;

import android.content.Context;

import com.holler.app.di.Presenter;
import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
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
                                      RouterModule.Router router,
                                      SplashPresenter.View splashScreenView,
                                      RetrofitModule.ServerAPI serverAPI,
                                      DeviceInfoModule.DeviceInfo deviceInfo,
                                      UserStorageModule.UserStorage userStorage){

        return new SplashPresenter(context, router,splashScreenView,serverAPI, deviceInfo, userStorage);
    }
}
