package com.holler.app;

import android.app.Activity;
import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.pnrhunter.di.DaggerTestAppComponent;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;

public class TestApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Override
    public void onCreate() {
        initLogger();
        setupDependencyGraphAndroid();
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }

    private void setupDependencyGraphAndroid() {
        DaggerTestAppComponent
                .create()
                .inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }


    private void initLogger() {
        FormatStrategy prettyFormat = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                .methodCount(0)
                .methodOffset(0)
                .logStrategy(new LogcatLogStrategy())
                .tag("HOLLER_LOGGER")
                .build();

        FormatStrategy cvsFormat = CsvFormatStrategy.newBuilder()
                .tag("HOLLER_FILE_LOGGER")
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(prettyFormat));

        Logger.i("STARTING APPLICATION");
    }
}
