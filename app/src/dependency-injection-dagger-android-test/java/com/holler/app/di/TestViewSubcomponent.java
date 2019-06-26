package com.pnrhunter.di;

import com.pnrhunter.mvp.TestView;
import com.pnrhunter.mvp.splash.SplashView;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface TestViewSubcomponent extends AndroidInjector<TestView> {

    @Subcomponent.Factory
    public interface Factory extends AndroidInjector.Factory<TestView> {}

}

