package com.holler.app.di.app.components;

import com.holler.app.di.Presenter;
import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.main.modules.MainScreenModule;
import com.holler.app.di.app.components.splash.modules.SplashScreenModule;
import com.holler.app.mvp.main.MainPresenter;
import com.holler.app.mvp.main.MainView;
import com.holler.app.mvp.splash.SplashView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {MainScreenModule.class})
public interface MainComponent {
    void inject(MainView activityMainView);

    MainPresenter getPresenter();
}
