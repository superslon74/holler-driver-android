package com.holler.app.mvp;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.holler.app.R;
import com.holler.app.di.DummyDataModule;
import com.orhanobut.logger.Logger;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class TestView extends Activity {

    @Inject
    DummyDataModule.DummyData data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.d("Tets view on create");
        AndroidInjection.inject(this);
        Logger.d("Tets view injected");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Logger.d("Tets view created");
        Logger.d(data.getData());
    }
}
