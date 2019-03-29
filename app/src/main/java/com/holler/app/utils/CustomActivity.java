package com.holler.app.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import java.util.HashMap;
import java.util.Map;

public class CustomActivity extends AppCompatActivity {

    private static int runningActivitiesCount = 0;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        synchronized (CustomActivity.class){
            runningActivitiesCount--;
        }
        if (!isRunning())
            startFloatingViewService();
    }

    private boolean isRunning() {
        return runningActivitiesCount > 0;
    }

    private void startFloatingViewService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(CustomActivity.this)){
                final Intent intent = new Intent(this, FloatingViewService.class);
                startService(intent);
            }
        }else{
            final Intent intent = new Intent(this, FloatingViewService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        new Notificator(this)
//                .cancelAllNotifications();

    }

    @Override
    protected void onStart() {
        super.onStart();
        synchronized (CustomActivity.class){
            runningActivitiesCount++;
        }
        if(isRunning()){
            final Intent intent = new Intent(this, FloatingViewService.class);
            stopService(intent);
        }
    }


    public interface OnActivityResultListener{
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private OnActivityResultListener listener = null;

    public void startActivityForResult(Intent intent, int requestCode, OnActivityResultListener listener) {
        if(listener!=null)
            this.listener = listener;
        startActivityForResult(intent,requestCode);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if(requestCode!=-1) {
            synchronized (CustomActivity.class) {
                runningActivitiesCount++;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
//                        startFloatingViewService();
                    }
                }
            }
        }
    }

    /**
     * Checking permissions asynchronously with handler
     */
    public static final int PERMISSION_GRANTED = 1;
    public static final int PERMISSION_DENID = 2;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 6027;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 7064;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 6311;
    private static final int PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 5757;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 7349;
    private static final int PERMISSIONS_REQUEST_CAMERA = 6482;

    private static Map<String, Integer> permissionRequestCodes = new HashMap<>();
    static{
        permissionRequestCodes.put(Manifest.permission.READ_CONTACTS, PERMISSIONS_REQUEST_READ_CONTACTS);
        permissionRequestCodes.put(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        permissionRequestCodes.put(Manifest.permission.CALL_PHONE, PERMISSIONS_REQUEST_CALL_PHONE);
        permissionRequestCodes.put(Manifest.permission.SYSTEM_ALERT_WINDOW, PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW);
        permissionRequestCodes.put(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        permissionRequestCodes.put(Manifest.permission.CAMERA, PERMISSIONS_REQUEST_CAMERA);
    }
    private static Map<Integer, RequestPermissionHandler> permissionHandlers = new HashMap<>();
    private static Map<String, String> permissionExplanation = new HashMap<>();

    public void checkPermissionAsynchronously(String permission, RequestPermissionHandler handler){

        int code = permissionRequestCodes.get(permission);
        permissionHandlers.put(code,handler);

        ///
        switch (permission){
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(CustomActivity.this)){
                        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + CustomActivity.this.getPackageName()));
                        startActivityForResult(intent, code);
                    }else{
                        handler.onPermissionGranted();
                    }
                }else{
                    handler.onPermissionDenied();
                }
                break;

            default:
                if (ContextCompat
                        .checkSelfPermission(CustomActivity.this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(CustomActivity.this, permission)) {
//                TODO: show explanation based on Manifest.permission.<name>
                        Log.e("AZAZA","permission explanation needed");
                    } else {
                        ActivityCompat.requestPermissions(CustomActivity.this, new String[]{permission}, code);
                    }

                } else {
                    handler.onPermissionGranted();
                }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
//        TODO: shitcode here
        if(listener!=null){
            listener.onActivityResult(requestCode,resultCode, data);
        }
        synchronized (CustomActivity.class){
            runningActivitiesCount--;
        }
        RequestPermissionHandler handler = permissionHandlers.get(requestCode);
        String requestedPermission = "";
        for(String s : permissionRequestCodes.keySet()){
            if(requestCode == permissionRequestCodes.get(s)){
                requestedPermission = s;
                break;
            }
        }
        if(Manifest.permission.SYSTEM_ALERT_WINDOW == requestedPermission){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(CustomActivity.this)){
                    handler.onPermissionGranted();
                }else{
                    handler.onPermissionDenied();
                }
            }else{
                handler.onPermissionDenied();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i =0; i<permissions.length; i++){
            String permission = permissions[i];
            if(permissionRequestCodes.get(permission) == requestCode){
                RequestPermissionHandler handler = permissionHandlers.get(requestCode);
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    handler.onPermissionGranted();
                }else{
                    handler.onPermissionDenied();
                }
            }
        }
    }

    public static abstract class RequestPermissionHandler{
        public abstract void onPermissionGranted();
        public abstract void onPermissionDenied();
    }

    public static class RefactoringException extends Exception{
        public RefactoringException(String message) {
            super(message);
        }

        @Override
        public void printStackTrace() {
            Log.e("AZAZA","REFACTORING ERROR: " + getMessage());
            super.printStackTrace();
        }
    }
}
