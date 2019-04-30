package com.holler.app.mvp.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;

import com.google.gson.JsonObject;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.splash.SplashPresenter;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;

public class MainPresenter {

    private Context context;
    private RouterModule.Router router;
    private MainPresenter.View view;
    private RetrofitModule.ServerAPI serverAPI;
    private UserModel userModel;

    private GPSTracker.GPSTrackerBinder gpsTrackerService;

    public MainPresenter(Context context,
                         RouterModule.Router router,
                         MainPresenter.View view,
                         RetrofitModule.ServerAPI serverAPI,
                         UserModel userModel) {

        this.context = context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.userModel = userModel;

        serviceBinding()
                .flatMap(service -> {
                    gpsTrackerService = service;
                    return statusRequesting();
                })
                .flatMap(checkStatusResponse -> {
                    Logger.d(checkStatusResponse.toString());
                    userModel.updateStatus(checkStatusResponse);
                    return Observable.just(true);
                })
                .doOnError(throwable -> {
                    Logger.e("shit2",throwable);
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe();

        userModel
                .statusSource
                .doOnNext(newStatus -> view.onStatusChanged(newStatus))
                .doOnError(throwable -> {
                    Logger.e(throwable.getMessage());
                })
                .subscribe();

        view.onStatusChanged(userModel.getCurrentStatus());

    }

    private Subject<RetrofitModule.ServerAPI.CheckStatusResponse> statusRequesting() {
        final Subject<RetrofitModule.ServerAPI.CheckStatusResponse> subject = UnicastSubject.create();

        Flowable
                .interval(3, TimeUnit.SECONDS)
                .flatMapSingle(__-> {

                    Location location = gpsTrackerService.getLocation();
                    String authHeader = userModel.getAuthHeader();
                    String latitude = "" + location.getLatitude();
                    String longitude = "" + location.getLongitude();

                    return serverAPI
                            .checkStatus(authHeader, latitude, longitude)
                            .doOnSuccess(subject::onNext)
                            .onErrorReturnItem(new RetrofitModule.ServerAPI.CheckStatusResponse());
//                            .subscribeOn(Schedulers.io());
                })
                .doOnError(throwable -> {
                    Logger.e("shit4",throwable);
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();

        return subject;
    }

    //TODO: make static move to service
    //TODO: change subject type
    private Subject<GPSTracker.GPSTrackerBinder> serviceBinding() {
        Subject<GPSTracker.GPSTrackerBinder> subject = UnicastSubject.create();

        Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                subject.onNext((GPSTracker.GPSTrackerBinder) binder);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                subject.onError(new Throwable("Service disconnected"));
            }
        };
        context.bindService(gpsTrackerBinding, gpsTrackerConnection, Context.BIND_AUTO_CREATE);

        return subject;
    }

    public void goOffline() {
        userModel.goOffline();
    }

    public void goOnline() {
        userModel.goOnline();
    }

    public ObservableSource<Boolean> logout() {
        return userModel
                .logout()
                .doOnSubscribe(disposable -> {
                    view.showSpinner();
                })
                .doOnComplete(() -> {
                    view.hideSpinner();
                });
    }

    public void goToWelcomeScreen() {
        router.goToWelcomeScreen();
        view.finish();
    }

    public interface View extends SpinnerShower, MessageDisplayer, Finishable {

        void onStatusChanged(UserModel.Status newStatus);
    }

    private interface IncomingOrderListener {
        void onOrderReceived(Order incomingOrder);
    }

    private class Order {

    }

}
