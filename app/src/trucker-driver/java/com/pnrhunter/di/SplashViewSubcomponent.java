package com.pnrhunter.di;


import com.pnrhunter.mvp.SplashView;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface SplashViewSubcomponent extends AndroidInjector<SplashView> {

    @Subcomponent.Factory
    public interface Factory extends AndroidInjector.Factory<SplashView> {}

}

