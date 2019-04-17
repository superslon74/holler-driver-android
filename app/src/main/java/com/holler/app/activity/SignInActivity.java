package com.holler.app.activity;


import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import com.holler.app.R;
import com.holler.app.utils.CustomActivity;


public class SignInActivity extends CustomActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);
        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }


}
