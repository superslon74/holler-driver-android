package com.holler.app.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.holler.app.R;

public class LoadingProgress extends Toast {
    private CustomActivity activity;

    public LoadingProgress(CustomActivity activity) {
        super(activity.getApplicationContext());
        LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.loding_spinner, null);
        setView(view);
        setGravity(Gravity.CENTER, 0, 0);
    }

    private CountDownTimer toastCountDown;

    public void startLoading() {
        int toastDurationInMilliSeconds = 100000;

        if (toastCountDown == null)
            toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 1000) {
                public void onTick(long millisUntilFinished) {
                    LoadingProgress.this.show();
                }

                public void onFinish() {
                    LoadingProgress.this.cancel();
                }
            };

        LoadingProgress.this.show();
        toastCountDown.start();
    }

    public void stopLoading() {
        toastCountDown.cancel();
        LoadingProgress.this.cancel();
    }
}
