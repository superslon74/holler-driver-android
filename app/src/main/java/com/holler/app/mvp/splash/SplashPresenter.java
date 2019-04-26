package com.holler.app.mvp.splash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;

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

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
            UserStorageModule.UserStorage userStorage) {
        this.context = context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.deviceInfo = deviceInfo;
        this.userStorage = userStorage;
    }

    @Override
    public void onResume() {
        boolean userLoggedIn = userStorage.getLoggedIn();
        if (userLoggedIn) {
//            Observable.concat(accessTokenUpdating(),profileUpdating());
            updateUserData();
        } else {
            router.goToMainScreen();
        }

    }

    private void updateUserData() {
        User user = userStorage.getUser();

        user.deviceType = deviceInfo.deviceType;
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;

        Single accessTokenSource =
                serverAPI.getAccessToken(user)
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                view.showSpinner();
                            }
                        })
                        .doOnSuccess(new Consumer<JsonObject>() {
                            @Override
                            public void accept(JsonObject jsonObject) throws Exception {
                                String accessToken = jsonObject.get("access_token").getAsString();
                                userStorage.setAccessToken(accessToken);
                            }
                        })
                        .flatMap(new Function<JsonObject, SingleSource<?>>() {
                            @Override
                            public SingleSource<?> apply(JsonObject jsonObject) throws Exception {
                                String authHeader = "Bearer " + userStorage.getAccessToken();

                                Single userProfileSource =
                                        serverAPI.getUserProfile(authHeader)
                                                .doOnSuccess(new Consumer<User>() {
                                                    @Override
                                                    public void accept(User user) throws Exception {
                                                        userStorage.putUser(user);
                                                        startTrackingLocation();
                                                    }
                                                });

                                return userProfileSource;
                            }
                        });



        accessTokenSource
                .subscribeOn(Schedulers.io())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        view.hideSpinner();
                    }
                })
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        Logger.i("onNext" + o.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        view.onMessage("Error");
                        router.goToWelcomeScreen();
                        Logger.e("Error: " + throwable.getMessage());
                    }
                });
    }


    private void startTrackingLocation() {
        Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                GPSTracker.GPSTrackerBinder service = (GPSTracker.GPSTrackerBinder) binder;
                service.startTracking();
                router.goToMainScreen();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        context.bindService(gpsTrackerBinding, gpsTrackerConnection, Context.BIND_AUTO_CREATE);
    }


    public interface View extends SpinnerShower, MessageDisplayer {

    }
}
