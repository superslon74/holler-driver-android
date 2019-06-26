package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.main.modules.MainScreenModule;
import com.pnrhunter.mvp.main.MainPresenter;
import com.pnrhunter.mvp.main.MainView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {MainScreenModule.class})
public interface MainComponent {
    void inject(MainView activityMainView);

    MainPresenter getPresenter();
}
