package com.holler.app.di;

import android.app.Application;

import com.holler.app.HollerDriverApplication;
import com.holler.app.di.app.modules.AppModule;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.GoogleApiClientModule;
import com.holler.app.di.app.modules.OrderModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.SharedPreferencesModule;
import com.holler.app.di.app.modules.UserModule;
import com.holler.app.di.app.modules.UserStorageModule;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,

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
public interface HollerDriverApplicationComponent {
    void inject(HollerDriverApplication app);
}
