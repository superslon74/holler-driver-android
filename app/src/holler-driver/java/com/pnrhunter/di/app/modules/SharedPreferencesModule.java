package com.pnrhunter.di.app.modules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;


import com.orhanobut.logger.Logger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedPreferencesModule {
    public static final String CACHE_STORAGE_NAME = "com.holler.app.Cache";
    private static final String STRING_DEFAULT_VALUE = "";


    @Singleton
    @Provides
    public SharedPreferencesHelper provideSharedPreferencesHelper(Context context) {
        return new SharedPreferencesHelper(context);
    }

    public class SharedPreferencesHelper {

        public SharedPreferences preferences;

        public SharedPreferencesHelper(Context context) {
            this.preferences = context.getSharedPreferences(CACHE_STORAGE_NAME, Context.MODE_PRIVATE);
        }

        public String get(String key) {
            return preferences.getString(key, STRING_DEFAULT_VALUE);
        }

        @SuppressLint("ApplySharedPref")
        public void put(String key, String value) {
            if (value != null && !"".equals(value)) {
                preferences
                        .edit()
                        .putString(key, value)
                        .commit();
            }else{
                Logger.e("trying to save empty key: "+key);
            }
        }
    }

}
