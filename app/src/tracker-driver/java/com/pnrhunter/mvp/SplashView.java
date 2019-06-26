package com.pnrhunter.mvp;

import android.app.Activity;
import android.os.Bundle;

import com.pnrhunter.R;
import com.orhanobut.logger.Logger;
import com.pnrhunter.di.DummyDataModule;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SplashView extends Activity {

    @Inject
    DummyDataModule.DummyData data;

    @Inject
    SplashPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.d("Splash view on create");
        AndroidInjection.inject(this);
        Logger.d("Splash view injected");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_splash);
        Logger.d("Splash view created");
        Logger.d(data.getData());
    }
}
