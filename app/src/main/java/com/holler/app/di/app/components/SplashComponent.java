package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.Presenter;
import com.holler.app.di.app.components.splash.modules.SplashScreenModule;
import com.holler.app.mvp.splash.SplashView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {SplashScreenModule.class})
public interface SplashComponent {
    void inject(SplashView activitySplashView);

    Presenter getPresenter();
}
