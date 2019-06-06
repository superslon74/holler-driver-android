package com.holler.app.di.app;


import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.GoogleApiClientModule;
import com.holler.app.di.app.modules.OrderModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.SharedPreferencesModule;
import com.holler.app.di.app.modules.UserModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.main.OrderModel;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.utils.FloatingViewService;
import com.holler.app.utils.FragmentHeaderBig;
import com.holler.app.utils.FragmentHeaderMini;

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
