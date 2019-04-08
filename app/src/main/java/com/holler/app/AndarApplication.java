package com.holler.app;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.holler.app.Utilities.FontsOverride;
import com.holler.app.di.AppComponent;
import com.holler.app.di.AppModule;
import com.holler.app.di.DaggerAppComponent;
import com.holler.app.di.DeviceInfoModule;
import com.holler.app.di.RetrofitModule;
import com.holler.app.di.SharedPreferencesModule;
import com.holler.app.di.UserStorageModule;
import com.holler.app.utils.FloatingViewService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class AndarApplication extends Application implements  ComponentCallbacks2 {

//    TODO: hard inject component
    private AppComponent component;

    public AppComponent component(){
        return component;
    }

    public static AndarApplication get(Context context){
        return (AndarApplication) context.getApplicationContext();
    }

    private void setupDependencyGraph(){
        component = DaggerAppComponent
                .builder()
                .appModule(new AppModule(this))
                .retrofitModule(new RetrofitModule())
                .sharedPreferencesModule(new SharedPreferencesModule())
                .deviceInfoModule(new DeviceInfoModule())
                .userStorageModule(new UserStorageModule())
                .build();
        component.inject(this);
    }

    /******************************************************************************************/

    public static final String TAG = AndarApplication.class
            .getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private static AndarApplication mInstance;

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupDependencyGraph();
        /**********************/
        mInstance = this;
        FontsOverride.setDefaultFont(this, "MONOSPACE", "ClanPro-NarrBook.otf");

    }

    private void initCalligraphyConfig() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getResources().getString(R.string.bariol))
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    public static synchronized AndarApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the no_user tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    public static String trimMessage(String json){
        String trimmedString = "";

        try{
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> iter = jsonObject.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    JSONArray value = jsonObject.getJSONArray(key);
                    for (int i = 0, size = value.length(); i < size; i++) {
                        Log.e("Errors in Form",""+value.getString(i));
                        trimmedString += value.getString(i);
                        if(i < size-1) {
                            trimmedString += '\n';
                        }
                    }
                } catch (JSONException e) {

                    trimmedString += jsonObject.optString(key);
                }
            }
        } catch(JSONException e){
            e.printStackTrace();
            return null;
        }
        Log.e("Trimmed",""+trimmedString);

        return trimmedString;
    }




}
