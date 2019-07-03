package com.pnrhunter.mvp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.pnrhunter.BuildConfig;
import com.pnrhunter.R;
import com.pnrhunter.di.DummyDataModule;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

public class SplashView extends Activity {

    @Inject
    DummyDataModule.DummyData data;

    @Inject
    SplashPresenter presenter;

    @BindView(R.id.sp_version) protected TextView versionView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_splash);
        ButterKnife.bind(this);
        setVersion();
    }

    private void setVersion(){
        versionView.setText(getResources().getString(R.string.was_version,  BuildConfig.VERSION_NAME, ""+ BuildConfig.VERSION_CODE));
    }
}
