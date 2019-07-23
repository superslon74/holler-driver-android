package com.pnrhunter;


import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.pnrhunter.di.DaggerTruckerDriverDI_Test_ApplicationComponent;
import com.pnrhunter.di.TruckerDriverDI_Test;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import io.reactivex.Scheduler;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


public class TruckerDriverApp extends DaggerApplication {


    @Override
    public void onCreate() {
        initLogger();
        super.onCreate();

        Scheduler scheduler = Schedulers.newThread();

        RxJava2CallAdapterFactory rxAdapter =
                RxJava2CallAdapterFactory
                        .createWithScheduler(scheduler);

        RxJavaPlugins.setErrorHandler(throwable -> {
            Logger.wtf("Rx plugin error handler",throwable);
            throwable.printStackTrace();
        });
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        TruckerDriverDI_Test.ApplicationComponent component = DaggerTruckerDriverDI_Test_ApplicationComponent
                .builder()
                .bindApplicationContext(this.getApplicationContext())
                .build();
        component.inject(this);

        return component;
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
