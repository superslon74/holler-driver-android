package com.holler.app.di;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SharedPreferencesModule {
    private static final String CACHE_STORAGE_NAME = "Cache";
    private static final String STRING_DEFAULT_VALUE = "";


    @Singleton
    @Provides
    public SharedPreferencesHelper provideSharedPreferencesHelper(Context context) {
        return new SharedPreferencesHelper(context);
    }

    public class SharedPreferencesHelper {

        public SharedPreferences preferences;
        public SharedPreferences.Editor editor;

        public SharedPreferencesHelper(Context context) {
            this.preferences = context.getSharedPreferences(CACHE_STORAGE_NAME, Context.MODE_PRIVATE);
            this.editor = preferences.edit();
        }

        public String get(String key) {
            return preferences.getString(key, STRING_DEFAULT_VALUE);
        }

        public void put(String key, String value) {
            if (value != null && !"".equals(value)) {
                editor.putString(key, value);
                editor.apply();
            }
        }
    }

}
