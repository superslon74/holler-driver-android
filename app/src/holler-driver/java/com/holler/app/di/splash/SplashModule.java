package com.holler.app.di.splash;

import android.content.Context;

import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.splash.SplashPresenter;
import com.holler.app.mvp.splash.SplashView;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = SplashSubcomponent.class)
public abstract class SplashModule {
    @Binds
    @IntoMap
    @ClassKey(SplashView.class)
    abstract AndroidInjector.Factory<?>
    bindYourActivityInjectorFactory(SplashSubcomponent.Factory factory);

}
