package com.holler.app.utils;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

public class CustomActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        if (!isRunning(this))
            startFloatingViewService();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isRunning(this))
            startFloatingViewService();
    }

    private boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    private void startFloatingViewService(){
        final Intent intent = new Intent(this, FloatingViewService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Notificator(this)
                .cancelAllNotifications();

        final Intent intent = new Intent(this, FloatingViewService.class);
        stopService(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // something
        // for home listen
        InnerRecevier innerReceiver = new InnerRecevier();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(innerReceiver, intentFilter);

    }


    // for home listen
    class InnerRecevier extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        final String SYSTEM_DIALOG_REASON_RECENTAPPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) || reason.equals((SYSTEM_DIALOG_REASON_RECENTAPPS))) {
                        startFloatingViewService();
                    }
                }
            }
        }
    }
}
