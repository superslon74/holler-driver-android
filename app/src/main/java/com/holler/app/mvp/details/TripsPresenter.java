package com.holler.app.mvp.details;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;

public class TripsPresenter {

    private Context context;
    private View view;
    private RouterModule.Router router;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private UserModel userModel;
    private RetrofitModule.ServerAPI serverAPI;

    public TripsPresenter(Context context,
                          View view,
                          RouterModule.Router router,
                          DeviceInfoModule.DeviceInfo deviceInfo,
                          UserModel userModel,
                          RetrofitModule.ServerAPI serverAPI) {

        this.context = context;
        this.view = view;
        this.router = router;
        this.deviceInfo = deviceInfo;
        this.userModel = userModel;
        this.serverAPI = serverAPI;
    }

    public Observable<Map<String, List<RetrofitModule.ServerAPI.OrderResponse>>> getTrips() {
        return Observable.create(emitter -> {
            Map<String, List<RetrofitModule.ServerAPI.OrderResponse>> result = new HashMap<>();
            getPastTrips()
                    .doOnSuccess(orders -> {
                        result.put(DetailsView.TYPE_PAST_TRIPS, orders);
                        if (result.keySet().size() == 2) {
                            emitter.onNext(result);
                            emitter.onComplete();
                        }
                    })
                    .doOnError(throwable -> {
                        emitter.onError(throwable);
                    })
                    .subscribe();
            getUpcomingTrips()
                    .doOnSuccess(orders -> {
                        result.put(DetailsView.TYPE_UPCOMING_TRIPS, orders);
                        if (result.keySet().size() == 2) {
                            emitter.onNext(result);
                            emitter.onComplete();
                        }
                    })
                    .doOnError(throwable -> {
                        emitter.onError(throwable);
                    })
                    .subscribe();
        });
    }

    private Single<List<RetrofitModule.ServerAPI.OrderResponse>> getPastTrips() {
        return serverAPI.getPastTrips(userModel.getAuthHeader());
    }

    private Single<List<RetrofitModule.ServerAPI.OrderResponse>> getUpcomingTrips() {
        return serverAPI.getUpcomingTrips(userModel.getAuthHeader());
    }

    public void goToDetailsScreen(String type, String orderId) {
        router.goToDetailsScreen(type, orderId);
        view.finish();
    }


    public interface View extends SpinnerShower, MessageDisplayer, Finishable {

    }
}
