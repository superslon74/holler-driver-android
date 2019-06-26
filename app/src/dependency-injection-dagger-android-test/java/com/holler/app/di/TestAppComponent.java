package com.pnrhunter.di;

import com.holler.app.TestApplication;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        TestModule.class,
        DummyDataModule.class
})
public interface TestAppComponent {
    void inject(TestApplication app);
}
