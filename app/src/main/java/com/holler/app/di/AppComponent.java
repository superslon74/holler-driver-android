package com.holler.app.di;

import android.content.Context;

import com.holler.app.Activity.Offline;
import com.holler.app.AndarApplication;
import com.holler.app.FCM.MyFirebaseInstanceIDService;
import com.holler.app.Fragment.Map;
import com.holler.app.utils.GPSTracker;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        RetrofitModule.class,
        DeviceInfoModule.class,
        SharedPreferencesModule.class,
        UserStorageModule.class,
        GoogleApiClientModule.class
})
public interface AppComponent {
    void inject(AndarApplication app);
    void inject(MyFirebaseInstanceIDService firebaseIdChecker);
    void inject(GPSTracker tracker);

    //TODO: remove shit above
    void inject(Map mapFragment);
    void inject(Offline offlineFragment);


    Context getContext();
    RetrofitModule.ServerAPI getRetrofitClient();
    DeviceInfoModule.DeviceInfo getDeviceInfoObject();
    SharedPreferencesModule.SharedPreferencesHelper getSharedPreferencesHalper();
    UserStorageModule.UserStorage getUserStorage();


}
