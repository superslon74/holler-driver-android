package com.holler.app.mvp.splash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.google.gson.JsonObject;
import com.holler.app.activity.MainActivity;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class SplashPresenter implements Presenter {
    private Context context;
    private RouterModule.Router router;
    private View view;
    private RetrofitModule.ServerAPI serverAPI;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private UserStorageModule.UserStorage userStorage;

    public SplashPresenter(
            Context context,
            RouterModule.Router router,
            View view,
            RetrofitModule.ServerAPI serverAPI,
            DeviceInfoModule.DeviceInfo deviceInfo,
            UserStorageModule.UserStorage userStorage){
        this.context=context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.deviceInfo = deviceInfo;
        this.userStorage = userStorage;
    }

    @Override
    public void onResume() {
        boolean userLoggedIn = userStorage.getLoggedIn();
        if(userLoggedIn){

        }else{
            router.goToWelcomeScreen();
        }

    }


    private Single accessTokenUpdating(){
        User user = userStorage.getUser();

        user.deviceType = "android";
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;

        Single<JsonObject> signInProcess = serverAPI.getAccessToken(user);
        signInProcess
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Logger.d("Access token updating disposed");

                    }

                    @Override
                    public void onSuccess(JsonObject jsonObject) {
                        String accessToken = jsonObject.get("access_token").getAsString();
                        userStorage.setAccessToken(accessToken);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e.getMessage());
                    }
                });

        return signInProcess;

//        serverAPI
//                .signIn(user)
//                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(null) {
//                    @Override
//                    public void onSuccessfulResponse(Response<JsonObject> response) {
//                        String accessToken = response.body().get("access_token").getAsString();
//                        userStorage.setAccessToken(accessToken);
//                        updateProfile();
//                    }
//
//                    @Override
//                    public void onUnsuccessfulResponse(Response<JsonObject> response) {
//                        super.onUnsuccessfulResponse(response);
//                        if(signInRetryCount>0){
//                            signInRetryCount--;
//                            updateAccessToken();
//                        }else{
//                            userStorage.setLoggedIn("false");
//                            router.goToWelcomeScreen();
//                        }
//                    }
//
//                    @Override
//                    public void onDisplayMessage(String message) {
//                        view.onMessage(message);
//                    }
//                });
    }

    private Single profileUpdating(){
        String authHeader = "Bearer " + userStorage.getAccessToken();

        Single<User> loadingProfileProcess = serverAPI.getUserProfile(authHeader);
        loadingProfileProcess.subscribe(new SingleObserver<User>() {
            @Override
            public void onSubscribe(Disposable d) {
                Logger.d("Profile updating disposed");
            }

            @Override
            public void onSuccess(User user) {
                userStorage.putUser(user);
                startTrackingLocation();
            }

            @Override
            public void onError(Throwable e) {
                Logger.e(e.getMessage());
            }
        });


        return loadingProfileProcess;
//
//
//        serverAPI
//                .profile(authHeader)
//                .enqueue(new OrderServerApi.CallbackErrorHandler<User>(null) {
//                    @Override
//                    public void onSuccessfulResponse(Response<User> response) {
//                        User user = response.body();
//                        userStorage.putUser(user);
//                        startTrackingLocation();
//                    }
//
//                    @Override
//                    public void onUnsuccessfulResponse(Response<User> response) {
//                        super.onUnsuccessfulResponse(response);
//                        router.goToWelcomeScreen();
//                    }
//
//                    @Override
//                    public void onDisplayMessage(String message) {
//                        view.onMessage(message);
//                    }
//                });

    }

    private void startTrackingLocation(){
        Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                GPSTracker.GPSTrackerBinder service = (GPSTracker.GPSTrackerBinder)binder;
                service.startTracking();
                router.goToMainScreen();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        context.bindService(gpsTrackerBinding,gpsTrackerConnection,Context.BIND_AUTO_CREATE);
    }


    public interface View extends SpinnerShower, MessageDisplayer {

    }
}
