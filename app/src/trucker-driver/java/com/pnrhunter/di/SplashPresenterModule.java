package com.pnrhunter.di;


import android.content.Context;

import com.orhanobut.logger.Logger;
import com.pnrhunter.mvp.SplashPresenter;

import dagger.Module;
import dagger.Provides;


@Module
public class SplashPresenterModule {

    @ActivityScope
    @Provides
    public SplashPresenter providePresenter(Context c){
        Logger.d("SplashPresenter provided");
        return new SplashPresenter(c);
    }

}

