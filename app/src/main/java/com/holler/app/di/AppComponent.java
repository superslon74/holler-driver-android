package com.holler.app.di;

import com.holler.app.AndarApplication;
import com.holler.app.FCM.MyFirebaseInstanceIDService;
import com.holler.app.utils.GPSTracker;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        RetrofitModule.class,
        DeviceInfoModule.class,
        SharedPreferencesModule.class,
        UserStorageModule.class
})
public interface AppComponent {
    void inject(AndarApplication app);
    void inject(MyFirebaseInstanceIDService firebaseIdChecker);
    void inject(GPSTracker tracker);

    RetrofitModule.ServerAPI getRetrofitClient();
    DeviceInfoModule.DeviceInfo getDeviceInfoObject();
    SharedPreferencesModule.SharedPreferencesHelper getSharedPreferencesHalper();
    UserStorageModule.UserStorage getUserStorage();


}
