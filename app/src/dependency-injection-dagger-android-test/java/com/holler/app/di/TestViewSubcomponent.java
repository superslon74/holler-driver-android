package com.holler.app.di;

import com.holler.app.mvp.TestView;
import com.holler.app.mvp.splash.SplashView;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface TestViewSubcomponent extends AndroidInjector<TestView> {

    @Subcomponent.Factory
    public interface Factory extends AndroidInjector.Factory<TestView> {}

}

