package com.pnrhunter.di.app;


import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.GoogleApiClientModule;
import com.pnrhunter.di.app.modules.OrderModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.di.app.modules.SharedPreferencesModule;
import com.pnrhunter.di.app.modules.UserModule;
import com.pnrhunter.di.app.modules.UserStorageModule;
import com.pnrhunter.mvp.main.OrderModel;
import com.pnrhunter.mvp.main.UserModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        TestAppModule.class,
        RetrofitModule.class,
        DeviceInfoModule.class,
        SharedPreferencesModule.class,
        UserStorageModule.class,
        GoogleApiClientModule.class,
        RouterModule.class,
        UserModule.class,
        OrderModule.class
})
public interface TestAppComponent {
    void inject(Context appContext);
    void inject(CreateOrderFromDriverTest createOrderFromDriverTest);

    Context getContext();

    RouterModule.Router getRouter();
    RetrofitModule.ServerAPI getRetrofitClient();
    DeviceInfoModule.DeviceInfo getDeviceInfoObject();
    SharedPreferencesModule.SharedPreferencesHelper getSharedPreferencesHalper();
    UserStorageModule.UserStorage getUserStorage();
    UserModel getUserModel();
    OrderModel orderModel();

}
