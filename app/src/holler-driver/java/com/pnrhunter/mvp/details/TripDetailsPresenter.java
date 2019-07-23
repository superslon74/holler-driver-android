package com.pnrhunter.mvp.details;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.main.UserModel;
import com.pnrhunter.utils.Finishable;
import com.pnrhunter.utils.MessageDisplayer;
import com.pnrhunter.utils.SpinnerShower;

public class TripDetailsPresenter {

    private UserModel userModel;
    private Context context;
    private View view;
    private RouterModule.Router router;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;

    private String orderType;
    private String orderId;

    public TripDetailsPresenter(Context context,
                                View view,
                                RouterModule.Router router,
                                DeviceInfoModule.DeviceInfo deviceInfo,
                                UserModel userModel,
                                RetrofitModule.ServerAPI serverAPI) {
        this.context=context;
        this.view=view;
        this.router=router;
        this.deviceInfo=deviceInfo;
        this.userModel=userModel;
        this.serverAPI=serverAPI;
    }

    public void requestData(String type, String id) {
        orderType=type;
        orderId=id;
        String requestType = null;
        switch (type){
            case DetailsView.TYPE_PAST_TRIPS: requestType= RetrofitModule.ServerAPI.ORDER_DETAILS_TYPE_PAST; break;
            case DetailsView.TYPE_UPCOMING_TRIPS: requestType= RetrofitModule.ServerAPI.ORDER_DETAILS_TYPE_UPCOMING; break;
            default: throw new IllegalStateException("Unrecognized type");
        }
        serverAPI.getOrderDetails(userModel.getAuthHeader(),requestType,id)
                .doOnSubscribe(disposable -> {view.showSpinner();})
                .doOnSuccess(orderResponse -> {
                    view.setView(orderResponse.get(0));
//                    Log.d("AZAZA","ORDER");
                })
                .doOnError(throwable -> view.showMessage(throwable.getMessage()))
                .doFinally(() -> {
                    view.hideSpinner();
                })
                .subscribe();
    }

    public void startRide() {
        serverAPI
                .startOrder(userModel.getAuthHeader(),orderId)
                .doOnSubscribe(disposable -> {
                    view.showSpinner();
                })
                .doOnSuccess(jsonElement -> {
                    router.goToMainScreen();
                    view.finish();
                })
                .doOnError(throwable -> {
                    view.showMessage(throwable.getMessage());
                })
                .doFinally(() -> {
                    view.hideSpinner();
                })
                .subscribe();
    }

    public void cancelRide() {
        serverAPI
                .cancelOrder(userModel.getAuthHeader(), new RetrofitModule.ServerAPI.CancelOrderRequestBody(orderId))
                .doOnSubscribe(disposable -> {
                    view.showSpinner();
                })
                .doOnSuccess(jsonObject -> {
                    router.goToTripsScreen();
                    view.finish();
                })
                .doOnError(throwable -> {
                    view.showMessage(throwable.getMessage());
                })
                .doFinally(() -> {
                    view.hideSpinner();
                })
                .subscribe();
    }

    public interface View extends MessageDisplayer, SpinnerShower, Finishable{
        void setView(RetrofitModule.ServerAPI.OrderResponse order);
    }

}
