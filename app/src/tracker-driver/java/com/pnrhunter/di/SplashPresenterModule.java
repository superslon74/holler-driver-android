package com.pnrhunter.di;


import android.content.Context;

import com.orhanobut.logger.Logger;
import com.pnrhunter.TrackerDriverApp;
import com.pnrhunter.mvp.SplashPresenter;
import com.pnrhunter.mvp.SplashView;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;


@Module
public class SplashPresenterModule {

    @ActivityScope
    @Provides
    public SplashPresenter providePresenter(Context c){
        Logger.d("SplashPresenter provided");
        return new SplashPresenter(c);
    }

}

