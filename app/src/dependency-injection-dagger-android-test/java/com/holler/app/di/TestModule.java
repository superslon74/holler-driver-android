package com.pnrhunter.di;


import android.content.Context;

import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.TestView;
import com.pnrhunter.mvp.main.UserModel;
import com.pnrhunter.mvp.splash.SplashPresenter;

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

