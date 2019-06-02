package com.holler.app.mvp.details;

import android.content.Context;
import android.util.Log;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.OrderModel;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;

public class DetailsPresenter {

    private UserModel userModel;
    private Context context;
    private View view;
    private RouterModule.Router router;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;

    public DetailsPresenter(Context context,
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
        String requestType = null;
        switch (type){
            case DetailsView.TYPE_PAST_TRIPS: requestType= RetrofitModule.ServerAPI.ORDER_DETAILS_TYPE_PAST; break;
            case DetailsView.TYPE_UPCOMING_TRIPS: requestType= RetrofitModule.ServerAPI.ORDER_DETAILS_TYPE_UPCOMING; break;
            default: throw new IllegalStateException("Unrecognized type");
        }
        serverAPI.getOrderDetails(userModel.getAuthHeader(),requestType,id)
                .doOnSubscribe(disposable -> {view.showSpinner();})
                .doOnSuccess(orderResponse -> {
                    Log.d("AZAZA","ORDER");
                })
                .doOnError(throwable -> view.showMessage(throwable.getMessage()))
                .doFinally(() -> {view.hideSpinner();})
                .subscribe();
    }

    public interface View extends MessageDisplayer, SpinnerShower, Finishable{
        void setView(RetrofitModule.ServerAPI.OrderResponse order);
    }
}
