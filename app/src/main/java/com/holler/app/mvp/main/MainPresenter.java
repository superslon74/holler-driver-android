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
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter {

    private Context context;
    private RouterModule.Router router;
    private MainPresenter.View view;
    private RetrofitModule.ServerAPI serverAPI;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private UserStorageModule.UserStorage userStorage;

    private GPSTracker.GPSTrackerBinder gpsTrackerService;
    private OrderObserver orderObserver;

    public MainPresenter(Context context,
                         RouterModule.Router router,
                         MainPresenter.View view,
                         RetrofitModule.ServerAPI serverAPI,
                         DeviceInfoModule.DeviceInfo deviceInfo,
                         UserStorageModule.UserStorage userStorage) {

        this.context = context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.deviceInfo = deviceInfo;
        this.userStorage = userStorage;

        bindService();
    }

    private void bindService() {
        Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                gpsTrackerService =  (GPSTracker.GPSTrackerBinder) binder;
                orderObserver = new OrderObserver();
                orderObserver.startSendingRequests();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        context.bindService(gpsTrackerBinding, gpsTrackerConnection, Context.BIND_AUTO_CREATE);
    }


    public interface View extends SpinnerShower, MessageDisplayer{

    }

    private class OrderObserver{
        private static final long UPDATE_TIME = 1000;
        private Handler handler = new Handler();
        private Runnable looper = new Runnable() {
            @Override
            public void run() {
                try{
                    sendRequest();
                }finally {
                    handler.postDelayed(looper,UPDATE_TIME);
                }
            }
        };

        private void sendRequest(){
            Location location = gpsTrackerService.getLocation();
            String authHeader = "Bearer "+userStorage.getAccessToken();
            String latitude = ""+location.getLatitude();
            String longitude = ""+location.getLongitude();

            Single statusSource =
                    serverAPI.checkStatus(authHeader, latitude,longitude)
                            .doOnSuccess(new Consumer<RetrofitModule.ServerAPI.CheckStatusResponse>() {
                                @Override
                                public void accept(RetrofitModule.ServerAPI.CheckStatusResponse jsonObject) throws Exception {
                                    Logger.i("STATUS received: "+jsonObject.toString());
                                }
                            });



            statusSource
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer() {
                        @Override
                        public void accept(Object o) throws Exception {
                            Logger.i("STATUS accept" + o.toString());
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Logger.e("STATUS Error: " + throwable.getMessage());
                        }
                    });
        }

        public void startSendingRequests(){
            handler.postDelayed(looper,UPDATE_TIME);
        }
        public void stopSendingRequests(){
            handler.removeCallbacksAndMessages(null);
        }
    }

    private interface IncomingOrderListener{
        void onOrderReceived(Order incomingOrder);
    }

    private class Order{

    }

}
