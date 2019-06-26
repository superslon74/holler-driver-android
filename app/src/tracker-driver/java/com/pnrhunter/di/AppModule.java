package com.pnrhunter.di;


import android.app.Application;
import android.content.Context;

import com.facebook.accountkit.Tracker;
import com.pnrhunter.TrackerDriverApp;
import com.pnrhunter.mvp.SplashPresenter;
import com.pnrhunter.mvp.SplashView;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;


@Module(subcomponents = SplashViewSubcomponent.class, includes = AndroidInjectionModule.class)
public abstract class AppModule {

    @Binds
    @Singleton
    abstract Application application(TrackerDriverApp app);

    @ActivityScope
    @ContributesAndroidInjector(modules = {SplashPresenterModule.class})
    abstract SplashView contributeYourActivityInjector();



}

