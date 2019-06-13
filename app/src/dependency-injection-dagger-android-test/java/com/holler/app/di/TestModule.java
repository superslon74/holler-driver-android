package com.holler.app.di;


import android.content.Context;

import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.TestView;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.splash.SplashPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;


@Module(subcomponents = TestViewSubcomponent.class)
public abstract class TestModule {
    @Binds
    @IntoMap
    @ClassKey(TestView.class)
    abstract AndroidInjector.Factory<?> bindYourActivityInjectorFactory(TestViewSubcomponent.Factory factory);



}

