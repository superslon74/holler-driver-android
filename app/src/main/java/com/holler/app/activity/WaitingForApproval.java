package com.holler.app.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.Services.UserStatusChecker;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.utils.CustomActivity;

public class WaitingForApproval extends CustomActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_waiting_for_approval);

        Button logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedHelper.putKey(WaitingForApproval.this, "loggedIn", getString(R.string.False));
//                starting main activity
                Intent mainIntent = new Intent(WaitingForApproval.this, WelcomeView.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
//                stopping service
                Intent intent = new Intent(WaitingForApproval.this, UserStatusChecker.class);
                stopService(intent);
//                finishing this activity
                WaitingForApproval.this.finish();

            }
        });


        Intent intent = new Intent(this, UserStatusChecker.class);
        intent.putExtra(
                UserStatusChecker.UserServerAPI.ARG_AUTH_HEADER,
                "Bearer " + SharedHelper.getKey(this, "access_token"));
        intent.putExtra(
                UserStatusChecker.UserServerAPI.ARG_REQUESTED_HEADER,
                "XMLHttpRequest");
        intent.putExtra(
                UserStatusChecker.UserServerAPI.ARG_DEVICE_TYPE,
                "android");
        intent.putExtra(
                UserStatusChecker.UserServerAPI.ARG_DEVICE_TOKEN,
                SharedHelper.getKey(this, "device_token"));
        String deviceId;
        try {
            deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception o) {
            deviceId = "COULD NOT GET UDID";
        }
        intent.putExtra(
                UserStatusChecker.UserServerAPI.ARG_DEVICE_ID,
                deviceId);

        startService(intent);

    }

    @Override
    public void onBackPressed() {

    }

}
