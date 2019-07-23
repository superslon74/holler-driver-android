package com.pnrhunter.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pnrhunter.Helper.URLHelper;
import com.pnrhunter.mvp.main.MainView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

public class UserStatusChecker extends Service {
    private UserServerAPI serverApiClient;
    private final int repeatInterval = 1000 * 10;
    private Runnable checker;
    private Handler handler;


    private Map<String, String> headers;
    private String deviceType;
    private String deviceId;
    private String deviceToken;

    public UserStatusChecker() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        initUserData(intent);
        initServerAPIClient();
        initChecker();
        startRepeatingTask();
        Log.e("AZAZAZ", "STARTED : " + flags);

        return START_REDELIVER_INTENT;
    }

    private void initUserData(Intent intent) {
        Bundle intentData = intent.getExtras();
        headers = new HashMap<>();
        headers.put(UserServerAPI.ARG_AUTH_HEADER, intentData.getString(UserServerAPI.ARG_AUTH_HEADER));
        headers.put(UserServerAPI.ARG_REQUESTED_HEADER, intentData.getString(UserServerAPI.ARG_REQUESTED_HEADER));
        deviceType = intentData.getString(UserServerAPI.ARG_DEVICE_TYPE);
        deviceId = intentData.getString(UserServerAPI.ARG_DEVICE_ID);
        deviceToken = intentData.getString(UserServerAPI.ARG_DEVICE_TOKEN);
    }

    private void initChecker() {
        handler = new Handler();
        checker = new Runnable() {
            @Override
            public void run() {
                try {
                    requestUserAndCheckStatus();
                } finally {
                    handler.postDelayed(checker, repeatInterval);
                }
            }
        };
    }

    private void requestUserAndCheckStatus() {


        Call<User> documentsListCall = serverApiClient.getProfile(
                headers,
                deviceType,
                deviceId,
                deviceToken
        );

        documentsListCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user.isApproved()) {
                        launchSplashScreenActivity();
                        stopRepeatingTask();
                    }
                } else {
                    Log.e("UnhandledApiError", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("AZAZA", "API Error ", t);
            }
        });
    }

    private void launchSplashScreenActivity() {
        Intent i = new Intent(this, MainView.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        stopSelf();
    }

    private void startRepeatingTask() {
        checker.run();
    }

    private void stopRepeatingTask() {
        handler.removeCallbacks(checker);
    }


    private void initServerAPIClient() {
        ConnectionPool pool = new ConnectionPool(10, 10000, TimeUnit.MILLISECONDS);

        OkHttpClient httpClient = new OkHttpClient
                .Builder()
                .cache(null)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(pool)
                .build();

        serverApiClient = new Retrofit
                .Builder()
                .client(httpClient)
                .baseUrl(URLHelper.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UserServerAPI.class);
    }



    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static class User {
        @Expose(serialize = false)
        @SerializedName("status")
        String status;

        public boolean isApproved() {
            return "approved".equals(this.status);
        }
    }

    public interface UserServerAPI {
        String ARG_AUTH_HEADER = "Authorization";
        String ARG_REQUESTED_HEADER = "X-Requested-With";
        String ARG_DEVICE_TYPE = "device_type";
        String ARG_DEVICE_ID = "device_id";
        String ARG_DEVICE_TOKEN = "device_token";


        @GET("api/provider/profile")
        Call<User> getProfile(
                @HeaderMap Map<String, String> headers,
                @Query(ARG_DEVICE_TYPE) String devicetype,
                @Query(ARG_DEVICE_ID) String deviceId,
                @Query(ARG_DEVICE_TOKEN) String deviceToken
        );
    }
}
