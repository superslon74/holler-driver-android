package com.pnrhunter.mvp.splash;

import android.os.Bundle;
import android.widget.TextView;

import com.pnrhunter.BuildConfig;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashView extends ExtendedActivity {

    @Inject protected SplashPresenter presenter;

    @BindView(R.id.sp_version) protected TextView versionView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_splash);
        ButterKnife.bind(this);
        setVersion();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.checkLoggedIn();
    }

    private void setVersion(){
        versionView.setText(getResources().getString(R.string.was_version,  BuildConfig.VERSION_NAME, ""+ BuildConfig.VERSION_CODE));
    }
}
