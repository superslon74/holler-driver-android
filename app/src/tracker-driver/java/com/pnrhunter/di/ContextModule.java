package com.pnrhunter.di;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.Logger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    @Singleton
    @Provides
    public Context provideContext(Application app){
        Logger.d("Providing context");
        return app.getApplicationContext();
    }

}
