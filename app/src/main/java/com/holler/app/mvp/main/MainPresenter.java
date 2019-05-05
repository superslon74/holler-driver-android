package com.holler.app.mvp.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.holler.app.AndarApplication;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.splash.SplashPresenter;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.ReactiveServiceBindingFactory;
import com.holler.app.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import org.reactivestreams.Publisher;

import java.util.HashMap;
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
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;
import retrofit2.Response;

public class MainPresenter {

    private Context context;
    private RouterModule.Router router;
    private MainPresenter.View view;
    private RetrofitModule.ServerAPI serverAPI;
    public UserModel userModel;
    public OrderModel orderModel;

    private GPSTracker.GPSTrackerBinder gpsTrackerService;

    public MainPresenter(Context context,
                         RouterModule.Router router,
                         MainPresenter.View view,
                         RetrofitModule.ServerAPI serverAPI,
                         UserModel userModel,
                         OrderModel orderModel) {

        this.context = context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.userModel = userModel;
        this.orderModel = orderModel;

        GPSTracker.serviceConnection(context)
                .flatMap(service -> {
                    gpsTrackerService = (GPSTracker.GPSTrackerBinder)service;
                    return statusRequesting();
                })
                .flatMap(checkStatusResponse -> {
                    Logger.d(checkStatusResponse.toString());
                    userModel.updateStatus(checkStatusResponse);
                    orderModel.updateRequestOrder(checkStatusResponse.requests);
                    return Observable.empty();
                })
                .doOnError(throwable -> {
//                    Logger.e("shit2",throwable);
                })
                .subscribe();

        userModel
                .statusSource
                .doOnNext(newStatus -> view.onStatusChanged(newStatus))
                .subscribe();

        orderModel
                .orderSource
                .doOnNext(order -> {
                    view.onOrderChanged(order);
                })
                .subscribe();

        view.onStatusChanged(userModel.getCurrentStatus());

    }



    private Subject<RetrofitModule.ServerAPI.CheckStatusResponse> statusRequesting() {
        final Subject<RetrofitModule.ServerAPI.CheckStatusResponse> subject = UnicastSubject.create();

        Flowable
                .interval(3, TimeUnit.SECONDS)
                .flatMapSingle(time -> {
                    Location location = gpsTrackerService.getLocation();
                    String authHeader = userModel.getAuthHeader();
                    String latitude = "" + location.getLatitude();
                    String longitude = "" + location.getLongitude();

                    return serverAPI
                            .checkStatus(authHeader, latitude, longitude)
                            .doOnSuccess(subject::onNext)
                            .onErrorReturnItem(new RetrofitModule.ServerAPI.CheckStatusResponse());
                })
                .subscribe();

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

    public void createAndSendOrder(){

        Location location = gpsTrackerService.getLocation();
        String latitude = ""+location.getLatitude();
        String longitude = ""+location.getLongitude();

        String messageSuccess = context.getString(R.string.successfully_created_order);
        String messageError = context.getString(R.string.error_creating_order);

        serverAPI
                .createOrder(userModel.getAuthHeader(), new RetrofitModule.ServerAPI.CreateOrderRequestBody(latitude,longitude))
                .doOnSubscribe(disposable -> view.showSpinner())
                .doFinally(() -> view.hideSpinner())
                .doOnSuccess(creteOrderResponse -> {
                    if(creteOrderResponse.isSuccessfullyCreated()){
                        view.onMessage(creteOrderResponse.message);
                    }else{
                        view.onMessage(creteOrderResponse.message);
                    }
                })
                .doOnError(throwable -> view.onMessage(messageError))
                .subscribe();

    }

    public interface View extends SpinnerShower, MessageDisplayer, Finishable {

        void onStatusChanged(UserModel.Status newStatus);
        void onOrderChanged(OrderModel.Order order);
    }

}
