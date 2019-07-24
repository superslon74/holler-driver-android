package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.register.modules.RegisterModule;
import com.pnrhunter.mvp.register.RegisterPresenter;
import com.pnrhunter.mvp.register.RegisterView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {RegisterModule.class})
public interface RegisterComponent {
    void inject(RegisterView activityRegisterView);

    RegisterPresenter getPresenter();
}
