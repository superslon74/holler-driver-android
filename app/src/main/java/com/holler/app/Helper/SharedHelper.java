package com.holler.app.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.holler.app.di.app.modules.SharedPreferencesModule;

@Deprecated
public class SharedHelper {

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    @Deprecated
    public static void putKey(Context context, String Key, String Value) {
        sharedPreferences = context.getSharedPreferences(SharedPreferencesModule.CACHE_STORAGE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(Key, Value);
        editor.commit();

    }

    @Deprecated
    public static String getKey(Context contextGetKey, String Key) {
        sharedPreferences = contextGetKey.getSharedPreferences(SharedPreferencesModule.CACHE_STORAGE_NAME, Context.MODE_PRIVATE);
        String Value = sharedPreferences.getString(Key, "");
        return Value;

    }

    @Deprecated
    public static void clearSharedPreferences(Context context)
    {
        sharedPreferences = context.getSharedPreferences(SharedPreferencesModule.CACHE_STORAGE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }


}
