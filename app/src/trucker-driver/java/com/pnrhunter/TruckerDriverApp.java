package com.pnrhunter;

import android.app.Activity;
import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.pnrhunter.di.DaggerTrackerAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;

public class TruckerDriverApp extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Override
    public void onCreate() {
        initLogger();
        setupDependencyGraphAndroid();
        super.onCreate();
    }

    private void setupDependencyGraphAndroid() {
        DaggerTrackerAppComponent
                .builder()
                .create(this)
                .inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    private void initLogger() {
        FormatStrategy tinyFormat = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                .methodCount(0)
                .methodOffset(0)
                .logStrategy(new LogcatLogStrategy())
                .tag("TRACKER_LOGGER_TINY")
                .build();

        FormatStrategy extendedFormat = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                .methodCount(3)
                .methodOffset(4)
                .logStrategy(new LogcatLogStrategy())
                .tag("TRACKER_LOGGER_EXTENDED")
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(tinyFormat));

//        Fabric.with(this, new Crashlytics());

        Logger.i("STARTING APPLICATION");
    }
}
