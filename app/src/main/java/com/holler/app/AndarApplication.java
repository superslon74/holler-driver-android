package com.holler.app;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.holler.app.Utilities.FontsOverride;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.DaggerAppComponent;
import com.holler.app.di.app.modules.AppModule;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.OrderModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.SharedPreferencesModule;
import com.holler.app.di.app.modules.UserModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.utils.GPSTracker;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import androidx.multidex.MultiDex;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class AndarApplication extends Application implements  ComponentCallbacks2 {


    @Override
    public void onCreate() {
        initLogger();
        setupDependencyGraph();
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        /**********************/
        instance = this;
        FontsOverride.setDefaultFont(this, "MONOSPACE", "ClanPro-NarrBook.otf");

    }



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
                .routerModule(new RouterModule())
                .userModule(new UserModule())
                .orderModule(new OrderModule())
                .build();
        component.inject(this);
        Logger.d("Dependency Graph Set Up");

//        Disposable serviceSubscriber = new ReactiveServiceBindingFactory()
//                .bind(getApplicationContext(),new Intent(getApplicationContext(), GPSTracker.class))
//                .subscribe();

    }

    private static AndarApplication instance;

    public static synchronized AndarApplication getInstance() {
        return instance;
    }

    private void initLogger() {
        FormatStrategy prettyFormat = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                .methodCount(0)
                .methodOffset(0)
                .logStrategy(new LogcatLogStrategy())
                .tag("HOLLER_LOGGER")
                .build();

        FormatStrategy cvsFormat = CsvFormatStrategy.newBuilder()
                .tag("HOLLER_FILE_LOGGER")
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(prettyFormat));

        Logger.i("STARTING APPLICATION");
    }

    /******************************************************************************************/

    public static final String TAG = AndarApplication.class
            .getSimpleName();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Deprecated
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Deprecated
    private void initCalligraphyConfig() {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getResources().getString(R.string.bariol))
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    @Deprecated
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    @Deprecated
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the no_user tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    @Deprecated
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    @Deprecated
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    @Deprecated
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
