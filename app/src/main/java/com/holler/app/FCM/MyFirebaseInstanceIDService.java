package com.holler.app.FCM;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.JsonObject;
import com.holler.app.AndarApplication;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Utilities.Utilities;
import com.holler.app.di.DeviceInfoModule;
import com.holler.app.di.RetrofitModule;
import com.holler.app.di.User;
import com.holler.app.di.UserStorageModule;
import com.holler.app.server.OrderServerApi;

import java.util.HashMap;

import javax.inject.Inject;

import retrofit2.Response;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    @Inject
    public DeviceInfoModule.DeviceInfo deviceInfo;

    @Inject
    public RetrofitModule.ServerAPI serverAPI;

    @Inject
    public UserStorageModule.UserStorage userStorage;

    public MyFirebaseInstanceIDService() {
        AndarApplication.getInstance().component().inject(this);
    }

    @Override
    public void onTokenRefresh() {
//        TODO: send token to server
        super.onTokenRefresh();
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        User user = userStorage.getUser();

        user.deviceType = "android";
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;

        serverAPI
                .signIn(user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<JsonObject> response) {
                        SharedHelper.putKey(getApplicationContext(),"device_token",""+refreshedToken);

                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<JsonObject> response) {
                        super.onUnsuccessfulResponse(response);
                        Toast.makeText(AndarApplication.getInstance(), "Device token updating failed..", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
