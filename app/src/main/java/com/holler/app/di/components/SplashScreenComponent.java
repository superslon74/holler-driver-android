package com.holler.app.di.components;

import com.holler.app.di.ActivityScope;
import com.holler.app.di.Presenter;
import com.holler.app.di.components.splash.modules.SplashScreenModule;
import com.holler.app.mvp.splash.SplashView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {SplashScreenModule.class})
public interface SplashScreenComponent {
    void inject(SplashView activitySplashView);

    Presenter getPresenter();
}
