package com.holler.app.di;

import com.holler.app.activity.SplashScreen;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {SplashScreenModule.class})
public interface SplashScreenComponent {
    void inject(SplashScreen splashScreenActivity);

    Presenter getPresenter();
}
