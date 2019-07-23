package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.Presenter;
import com.pnrhunter.di.app.components.splash.modules.SplashScreenModule;
import com.pnrhunter.mvp.splash.SplashView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {SplashScreenModule.class})
public interface SplashComponent {
    void inject(SplashView activitySplashView);

    Presenter getPresenter();
}
