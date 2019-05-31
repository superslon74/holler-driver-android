package com.holler.app.mvp.details;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.OrderModel;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;

public class DetailsPresenter {

    private Context context;
    private View view;
    private RouterModule.Router router;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;

    public DetailsPresenter(Context context,
                            View view,
                            RouterModule.Router router,
                            DeviceInfoModule.DeviceInfo deviceInfo,
                            RetrofitModule.ServerAPI serverAPI) {
        this.context=context;
        this.view=view;
        this.router=router;
        this.deviceInfo=deviceInfo;
        this.serverAPI=serverAPI;
    }

    public void requestData(String type, String id) {

    }

    public interface View extends MessageDisplayer, SpinnerShower, Finishable{
        void setView(RetrofitModule.ServerAPI.OrderResponse order);
    }
}
