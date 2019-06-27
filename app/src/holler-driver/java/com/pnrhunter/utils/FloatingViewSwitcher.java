package com.pnrhunter.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class FloatingViewSwitcher  {

    private Context context;

    private static volatile int runningActivitiesCount = 0;

    public FloatingViewSwitcher(Context context){
        this.context=context;
    }

    private void toggleFloatingViewService(boolean isActivityRunning) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                final Intent intent = new Intent(context, FloatingViewService.class);
                if ((!isActivityRunning)) {
                    context.startService(intent);
                } else {
                    context.stopService(intent);
                }
            }
        } else {
            final Intent intent = new Intent(context, FloatingViewService.class);
            if ((!isActivityRunning)) {
                context.startService(intent);
            } else {
                context.stopService(intent);
            }
        }
    }


    private boolean isRunning() {
        return runningActivitiesCount > 0;
    }


    public void onActivityCountIncreased() {
        synchronized (FloatingViewSwitcher.class) {
            runningActivitiesCount++;
            toggleFloatingViewService(isRunning());
        }
    }



    public void onActivityCountDecreased() {
        synchronized (FloatingViewSwitcher.class) {
            runningActivitiesCount--;
            toggleFloatingViewService(isRunning());
        }
    }

}
