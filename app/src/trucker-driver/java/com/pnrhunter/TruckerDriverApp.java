package com.pnrhunter;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogcatLogStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.pnrhunter.di.DI;
import com.pnrhunter.di.DaggerDI_ApplicationComponent;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;

public class TruckerDriverApp extends DaggerApplication {


    @Override
    public void onCreate() {
        test();
        initLogger();
        super.onCreate();
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        DI.ApplicationComponent component = DaggerDI_ApplicationComponent
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

    static class StringExtraBehaviourWrapper{
        private String s;

        public StringExtraBehaviourWrapper(String s){
            this.s = s;
        }

        public StringExtraBehaviourWrapper applySomeSpecificModification(){
            this.s = s + " modification";
            return this;
        }

        public String getString(){
            return s;
        }

        public static String applySomeSpecificModification(String toString){
            return toString + " modification";
        }
    }

    static class SomeLibrary{
        public void method(String o){
            Log.d("LOL",o);
        }
    }

    public void test(){
        new SomeLibrary().method(StringExtraBehaviourWrapper.applySomeSpecificModification("smth"));
        new SomeLibrary().method(new StringExtraBehaviourWrapper("smth").applySomeSpecificModification().getString());
        return;
    }

    static class CommonObject{
        public String doSomething(){
            return "something";
        }
    }

    static class CommonObjectElseBehaviourDecorator extends CommonObject{

        @Override
        public String doSomething() {
            return "something else";
        }
    }



}
