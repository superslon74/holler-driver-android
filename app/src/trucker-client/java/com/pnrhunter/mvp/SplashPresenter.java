package com.pnrhunter.mvp;

import android.content.Context;

import com.orhanobut.logger.Logger;

public class SplashPresenter {

    public SplashPresenter() {
        Logger.d("SplashPresenter created");
    }

    public SplashPresenter(Context context) {
        Logger.d("SplashPresenter created with context");
    }
}
