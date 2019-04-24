package com.holler.app.di.app.components.main.modules;

import android.content.Context;

import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.main.MainPresenter;
import com.holler.app.mvp.splash.SplashPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class MainScreenModule {
    private MainPresenter.View view;

    public MainScreenModule(MainPresenter.View view) {
        this.view = view;
    }

    @Provides
    public MainPresenter.View provideView() {
        return view;
    }


    @Provides
    public MainPresenter providePresenter(Context context,
                                          RouterModule.Router router,
                                          MainPresenter.View view,
                                          RetrofitModule.ServerAPI serverAPI,
                                          DeviceInfoModule.DeviceInfo deviceInfo,
                                          UserStorageModule.UserStorage userStorage) {

        return new MainPresenter(context, router, view, serverAPI, deviceInfo, userStorage);
    }
}
