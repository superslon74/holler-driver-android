package com.pnrhunter.activity;


import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import com.pnrhunter.R;
import com.pnrhunter.utils.CustomActivity;


public class SignInActivity extends CustomActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }


}
