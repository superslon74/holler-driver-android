package com.holler.app.mvp.main;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.UserStorageModule;

import java.util.ArrayList;
import java.util.List;

public class OrderModel {

    private RetrofitModule.ServerAPI serverAPI;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;

    public OrderModel(RetrofitModule.ServerAPI serverAPI,
                             UserStorageModule.UserStorage userStorage,
                             DeviceInfoModule.DeviceInfo deviceInfo) {

        this.serverAPI = serverAPI;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
    }

    public List<RetrofitModule.ServerAPI.OrderResponse> updateOrderStatus(List<RetrofitModule.ServerAPI.RequestedOrderResponse> requests) {
        List<RetrofitModule.ServerAPI.OrderResponse> result = new ArrayList<>();
        for(RetrofitModule.ServerAPI.RequestedOrderResponse r : requests){
            result.add(r.order);
        }
        return result;
    }
}
