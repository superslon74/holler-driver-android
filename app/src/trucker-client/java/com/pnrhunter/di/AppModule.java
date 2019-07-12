package com.pnrhunter.di;


import android.app.Application;

import com.pnrhunter.TruckerClientApp;
import com.pnrhunter.mvp.SplashView;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjectionModule;
import dagger.android.ContributesAndroidInjector;


@Module(subcomponents = SplashViewSubcomponent.class, includes = AndroidInjectionModule.class)
public abstract class AppModule {

    @Binds
    @Singleton
    abstract Application application(TruckerClientApp app);

    @ActivityScope
    @ContributesAndroidInjector(modules = {SplashPresenterModule.class})
    abstract SplashView contributeYourActivityInjector();



}

