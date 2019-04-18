package com.holler.app.mvp.splash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.gson.JsonObject;
import com.holler.app.activity.MainActivity;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.GPSTracker;

import retrofit2.Response;

public class SplashPresenter implements Presenter {
    private Context context;
    private View view;
    private RetrofitModule.ServerAPI serverAPI;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private UserStorageModule.UserStorage userStorage;

    public SplashPresenter(
            Context context,
            View view,
            RetrofitModule.ServerAPI serverAPI,
            DeviceInfoModule.DeviceInfo deviceInfo,
            UserStorageModule.UserStorage userStorage){
        this.context=context;
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
            view.gotoActivity(WelcomeView.class);
        }

    }

    private static int signInRetryCount = 3;

    private void updateAccessToken(){
        User user = userStorage.getUser();

        user.deviceType = "android";
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;

        serverAPI
                .signIn(user)
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
                            view.gotoActivity(WelcomeView.class);
                        }
                    }

                    @Override
                    public void onDisplayMessage(String message) {
                        view.showMessage(message);
                    }
                });
    }

    private void updateProfile(){
        String authHeader = "Bearer " + userStorage.getAccessToken();

        serverAPI
                .profile(authHeader)
                .enqueue(new OrderServerApi.CallbackErrorHandler<User>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<User> response) {
                        User user = response.body();
                        userStorage.putUser(user);
                        startTrackingLocation();
                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<User> response) {
                        super.onUnsuccessfulResponse(response);
                        view.gotoActivity(WelcomeView.class);
                    }

                    @Override
                    public void onDisplayMessage(String message) {
                        view.showMessage(message);
                    }
                });

    }

    private void startTrackingLocation(){
        Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                GPSTracker.GPSTrackerBinder service = (GPSTracker.GPSTrackerBinder)binder;
                service.startTracking();
                view.gotoActivity(MainActivity.class);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        context.bindService(gpsTrackerBinding,gpsTrackerConnection,Context.BIND_AUTO_CREATE);
    }


    public interface View {
        void showProgress();
        void hideProgress();
        void showMessage(String message);
        void gotoActivity(Class<? extends CustomActivity> redirectTo);
    }
}
