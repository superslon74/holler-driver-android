package com.pnrhunter.di.app.modules;

import android.content.ContentResolver;
import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DeviceInfoModule {
    private static final String DEVICE_TYPE = "android";


    @Provides
    @Singleton
    public DeviceInfoModule.DeviceInfo provideDeviceInfo(Context context, SharedPreferencesModule.SharedPreferencesHelper storage){
        ContentResolver resolver = context.getContentResolver();
        DeviceInfo info = new DeviceInfo();
        info.deviceType = DEVICE_TYPE;
        try {
            info.deviceId = android.provider.Settings.Secure.getString(resolver, android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            info.deviceId = "COULD NOT GET UDID";
        }


        try {
            info.deviceToken = FirebaseInstanceId.getInstance().getToken(info.deviceId, "aaaaa");
        } catch (IOException e) {
            info.deviceToken = "COULD NOT GET UDID";
        }
//        storage.get("device_token");



        return info;
    }

    public class DeviceInfo{
        public String deviceType;
        public String deviceId;
        public String deviceToken;
    }
}
