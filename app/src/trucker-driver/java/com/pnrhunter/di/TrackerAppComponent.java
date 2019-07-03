package com.pnrhunter.di;


import com.pnrhunter.TruckerDriverApp;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        ContextModule.class,
        AppModule.class,
        DummyDataModule.class
})
public interface TrackerAppComponent extends AndroidInjector<TruckerDriverApp> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<TruckerDriverApp> {}
}
