package com.holler.app.di.splash;

import com.holler.app.mvp.splash.SplashView;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface SplashSubcomponent extends AndroidInjector<SplashView> {

    @Subcomponent.Factory
    public interface Factory extends AndroidInjector.Factory<SplashView> {}

}

