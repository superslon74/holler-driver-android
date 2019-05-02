package com.holler.app.di.app.modules;

import com.holler.app.mvp.main.OrderModel;
import com.holler.app.mvp.main.UserModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class OrderModule {


    @Provides
    @Singleton
    public OrderModel provideOrderModel(RetrofitModule.ServerAPI serverAPI,
                                        UserStorageModule.UserStorage userStorage,
                                        DeviceInfoModule.DeviceInfo deviceInfo){
        return new OrderModel(serverAPI, userStorage, deviceInfo);
    }

}
