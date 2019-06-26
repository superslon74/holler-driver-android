package com.pnrhunter.di.app.modules;

import com.pnrhunter.mvp.main.OrderModel;
import com.pnrhunter.mvp.main.UserModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class OrderModule {


    @Provides
    @Singleton
    public OrderModel provideOrderModel(RetrofitModule.ServerAPI serverAPI,
                                        UserStorageModule.UserStorage userStorage,
                                        DeviceInfoModule.DeviceInfo deviceInfo,
                                        UserModel userModel){
        return new OrderModel(serverAPI, userStorage, deviceInfo, userModel);
    }

}
