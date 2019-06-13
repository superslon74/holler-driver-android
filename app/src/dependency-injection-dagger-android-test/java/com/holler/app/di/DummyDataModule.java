package com.holler.app.di;


import com.orhanobut.logger.Logger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DummyDataModule {
    @Singleton
    @Provides
    public DummyData provideData(){
        Logger.d("DummyData provided");

        return new DummyData() {
            @Override
            public String getData() {
                Logger.d("DummyData extracted");
                return "Dummy data";
            }
        };
    }

    public interface DummyData{
        String getData();
    }
}

