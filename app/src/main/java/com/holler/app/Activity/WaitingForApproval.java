package com.holler.app.Activity;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.Services.UserStatusChecker;
import com.holler.app.utils.CustomActivity;

import org.json.JSONObject;

import java.util.HashMap;

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
                Intent mainIntent = new Intent(WaitingForApproval.this, WelcomeScreenActivity.class);
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
