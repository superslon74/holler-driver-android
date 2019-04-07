package com.holler.app.di;

import com.holler.app.AndarApplication;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        RetrofitModule.class,
        DeviceInfoModule.class,
        SharedPreferencesModule.class
})
public interface AppComponent {
    void inject(AndarApplication app);

    RetrofitModule.ServerAPI getRetrofitClient();
    DeviceInfoModule.DeviceInfo getDeviceInfoObject();
    SharedPreferencesModule.SharedPreferencesHelper getSharedPreferencesHalper();


}
