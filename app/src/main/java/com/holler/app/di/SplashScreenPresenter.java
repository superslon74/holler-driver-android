package com.holler.app.di;

import android.util.Log;

import com.google.gson.JsonObject;
import com.holler.app.Activity.BeginScreen;
import com.holler.app.Activity.MainActivity;
import com.holler.app.Activity.RegisterActivity;
import com.holler.app.Activity.WelcomeScreenActivity;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.CustomActivity;

import java.util.HashMap;

import retrofit2.Response;

public class SplashScreenPresenter implements Presenter {
    private View view;
    private RetrofitModule.ServerAPI serverAPI;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private UserStorageModule.UserStorage userStorage;

    public SplashScreenPresenter(
            View view,
            RetrofitModule.ServerAPI serverAPI,
            DeviceInfoModule.DeviceInfo deviceInfo,
            UserStorageModule.UserStorage userStorage){
        this.view = view;
        this.serverAPI = serverAPI;
        this.deviceInfo = deviceInfo;
        this.userStorage = userStorage;
    }

    @Override
    public void onResume() {
        boolean userLoggedIn = userStorage.getLoggedIn();
        if(userLoggedIn){
            updateAccessToken();
        }else{
            view.gotoActivity(WelcomeScreenActivity.class);
        }

    }

    private static int signInRetryCount = 3;

    private void updateAccessToken(){
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");

        User user = userStorage.getUser();

        user.deviceType = "android";
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;

        serverAPI
                .signIn(headers, user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<JsonObject> response) {
                        String accessToken = response.body().get("access_token").getAsString();
                        userStorage.setAccessToken(accessToken);
                        updateProfile();
                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<JsonObject> response) {
                        super.onUnsuccessfulResponse(response);
                        if(signInRetryCount>0){
                            signInRetryCount--;
                            updateAccessToken();
                        }else{
                            userStorage.setLoggedIn("false");
                            view.gotoActivity(WelcomeScreenActivity.class);
                        }
                    }

                    @Override
                    public void onDisplayMessage(String message) {
                        view.showMessage(message);
                    }
                });
    }

    private void updateProfile(){
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + userStorage.getAccessToken());

        serverAPI
                .profile(headers)
                .enqueue(new OrderServerApi.CallbackErrorHandler<User>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<User> response) {
                        User user = response.body();
                        userStorage.putUser(user);
                        view.gotoActivity(MainActivity.class);
                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<User> response) {
                        super.onUnsuccessfulResponse(response);
                        view.gotoActivity(WelcomeScreenActivity.class);
                    }

                    @Override
                    public void onDisplayMessage(String message) {
                        view.showMessage(message);
                    }
                });

    }





    public interface View {
        void showProgress();
        void hideProgress();
        void showMessage(String message);
        void gotoActivity(Class<? extends CustomActivity> redirectTo);
    }
}
