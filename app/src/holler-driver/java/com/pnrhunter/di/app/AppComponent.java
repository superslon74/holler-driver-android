package com.pnrhunter.di.app;

import android.content.Context;

import com.pnrhunter.HollerApplication;
import com.pnrhunter.di.app.modules.AppModule;
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
import com.pnrhunter.mvp.welcome.WelcomeView;
import com.pnrhunter.utils.FloatingViewService;
import com.pnrhunter.utils.FragmentHeaderBig;
import com.pnrhunter.utils.FragmentHeaderMini;
import com.pnrhunter.utils.GPSTracker;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        RetrofitModule.class,
        DeviceInfoModule.class,
        SharedPreferencesModule.class,
        UserStorageModule.class,
        GoogleApiClientModule.class,
        RouterModule.class,
        UserModule.class,
        OrderModule.class
})
public interface AppComponent {
    void inject(HollerApplication app);
    void inject(GPSTracker tracker);
    void inject(FloatingViewService floating);

    void inject(WelcomeView welcomeView);
    void inject(FragmentHeaderBig headerBigView);
    void inject(FragmentHeaderMini headerMiniView);
    //TODO: remove shit above


    Context getContext();

    RouterModule.Router getRouter();
    RetrofitModule.ServerAPI getRetrofitClient();
    DeviceInfoModule.DeviceInfo getDeviceInfoObject();
    SharedPreferencesModule.SharedPreferencesHelper getSharedPreferencesHalper();
    UserStorageModule.UserStorage getUserStorage();
    UserModel getUserModel();
    OrderModel orderModel();

}
