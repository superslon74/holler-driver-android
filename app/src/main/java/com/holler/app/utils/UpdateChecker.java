package com.holler.app.utils;


import android.content.Context;
import android.os.Build;

import com.google.firebase.BuildConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.orhanobut.logger.Logger;

import java.util.Map;

import io.reactivex.Observable;

public class UpdateChecker {
    private static final String KEY_ACTUAL_APP_VERSION = "actual_version";
    private Context context;

    public UpdateChecker(Context context) {
        this.context=context;
    }

    public  Observable<Boolean> checkForNewVersion(){
        return Observable.<Boolean>create(emitter -> {
            FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            firebaseRemoteConfig.reset().addOnCompleteListener(resetCommand -> {
                firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(fetchCommand -> {
                    if(fetchCommand.isSuccessful()){
                        Map<String, FirebaseRemoteConfigValue> remoteConfig = firebaseRemoteConfig.getAll();
                        try{
                            int actualVersionNumber = Integer.parseInt(remoteConfig.get(KEY_ACTUAL_APP_VERSION).asString());
                            int currentVersionNumber = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                            if(actualVersionNumber>currentVersionNumber){
                                emitter.onNext(true);
                            }else{
                                emitter.onNext(false);
                            }
                            emitter.onComplete();
                        }catch (Exception e){
                            Logger.e(e.getMessage(), e);
                            emitter.onError(new Throwable("Error while parsing actual version code"));
                        }

                    }else{
                        emitter.onError(new Throwable("Error while fetching actual version code"));
                    }
                });
            });

        });
    }
}
