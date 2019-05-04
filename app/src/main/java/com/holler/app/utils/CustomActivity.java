package com.holler.app.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Toast;


import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.holler.app.R;
import com.holler.app.activity.MainActivity;
import com.holler.app.mvp.main.MainView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CustomActivity
        extends AppCompatActivity
        implements SpinnerShower, KeyboardObserver, MessageDisplayer {

    private static volatile int runningActivitiesCount = 0;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        synchronized (CustomActivity.class) {
            runningActivitiesCount--;
            toggleFloatingViewService(isRunning());
        }
    }

    private boolean isRunning() {
        return runningActivitiesCount > 0;
    }

    private void toggleFloatingViewService(boolean isActivityRunning) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(CustomActivity.this)) {
                final Intent intent = new Intent(this, FloatingViewService.class);
                if ((!isActivityRunning)) {
                    startService(intent);
                } else {
                    stopService(intent);
                }
            }
        } else {
            final Intent intent = new Intent(this, FloatingViewService.class);
            if ((!isActivityRunning)) {
                startService(intent);
            } else {
                stopService(intent);
            }
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
        synchronized (CustomActivity.class) {
            runningActivitiesCount++;
            toggleFloatingViewService(isRunning());
        }

    }



    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private OnActivityResultListener listener = null;

    public void startActivityForResult(Intent intent, int requestCode, OnActivityResultListener listener) {
        if (listener != null)
            this.listener = listener;
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void startIntentSenderForResult(IntentSender intent, int requestCode, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
        if (requestCode != -1) {
            synchronized (CustomActivity.class) {
                runningActivitiesCount++;
                toggleFloatingViewService(isRunning());
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode != -1) {
            synchronized (CustomActivity.class) {
                runningActivitiesCount++;
                toggleFloatingViewService(isRunning());
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (CustomActivity.this instanceof MainView) {
            moveTaskToBack(true);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initKeyboardObserver();
        // for home listen
//        InnerRecevier innerReceiver = new InnerRecevier();
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        registerReceiver(innerReceiver, intentFilter);
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

    private static final int CODE_REQUEST_READ_CONTACTS = 6027;
    private static final int CODE_REQUEST_ACCESS_FINE_LOCATION = 7064;
    private static final int CODE_REQUEST_CALL_PHONE = 6311;
    private static final int CODE_REQUEST_SYSTEM_ALERT_WINDOW = 5757;
    private static final int CODE_REQUEST_READ_EXTERNAL_STORAGE = 7349;
    private static final int CODE_REQUEST_CAMERA = 6482;
    private static final int CODE_REQUEST_INTERNET = 14737;
    private static final int CODE_REQUEST_LOCATION = 1064;
    private static final int CODE_ENABLE_LOCATION = 1450;

    public static final String PERMISSION_ENABLE_LOCATION = "com.holler.app.ACCESS_LOCATION";

    private static Map<String, Integer> permissionRequestCodes = new HashMap<>();

    static {
        permissionRequestCodes.put(Manifest.permission.READ_CONTACTS, CODE_REQUEST_READ_CONTACTS);
        permissionRequestCodes.put(Manifest.permission.ACCESS_FINE_LOCATION, CODE_REQUEST_ACCESS_FINE_LOCATION);
        permissionRequestCodes.put(Manifest.permission.CALL_PHONE, CODE_REQUEST_CALL_PHONE);
        permissionRequestCodes.put(Manifest.permission.SYSTEM_ALERT_WINDOW, CODE_REQUEST_SYSTEM_ALERT_WINDOW);
        permissionRequestCodes.put(Manifest.permission.READ_EXTERNAL_STORAGE, CODE_REQUEST_READ_EXTERNAL_STORAGE);
        permissionRequestCodes.put(Manifest.permission.CAMERA, CODE_REQUEST_CAMERA);
        permissionRequestCodes.put(Manifest.permission.INTERNET, CODE_REQUEST_INTERNET);
        permissionRequestCodes.put(Manifest.permission.LOCATION_HARDWARE, CODE_REQUEST_LOCATION);
        permissionRequestCodes.put(PERMISSION_ENABLE_LOCATION, CODE_ENABLE_LOCATION);
    }

    private static Map<Integer, RequestPermissionHandler> permissionHandlers = new HashMap<>();
    private static Map<String, String> permissionExplanation = new HashMap<>();

    public void checkPermissionAsynchronously(String permission, final RequestPermissionHandler handler) {

        final int code = permissionRequestCodes.get(permission);
        permissionHandlers.put(code, handler);

        ///
        switch (permission) {
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(CustomActivity.this)) {
                        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + CustomActivity.this.getPackageName()));
                        startActivityForResult(intent, code);
                    } else {
                        handler.onPermissionGranted();
                    }
                } else {
                    handler.onPermissionDenied();
                }
                break;
            case PERMISSION_ENABLE_LOCATION:
                Intent gpsTrackerBinding = new Intent(this, GPSTracker.class);
                ServiceConnection gpsTrackerConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder binder) {
                        GPSTracker.GPSTrackerBinder service = (GPSTracker.GPSTrackerBinder) binder;

                        service.connectGoogleApi(
                                locationSettingsResponse -> handler.onPermissionGranted(),
                                e -> {
                                    if (e instanceof ResolvableApiException) {
                                        try {

                                            ResolvableApiException resolvable = (ResolvableApiException) e;
                                            resolvable.startResolutionForResult(CustomActivity.this, code);
                                        } catch (IntentSender.SendIntentException sendEx) {
                                            handler.onPermissionDenied();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };
                bindService(gpsTrackerBinding, gpsTrackerConnection, Context.BIND_AUTO_CREATE);
                break;
            case Manifest.permission.INTERNET:
                if (isInternet()) {
                    handler.onPermissionGranted();
                } else {
                    final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivityForResult(intent, code);
                }
                break;
            case Manifest.permission.LOCATION_HARDWARE:
                if (isGPSEnabled()) {
                    handler.onPermissionGranted();
                } else {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, code);
                }
                break;
            default:
                if (ContextCompat
                        .checkSelfPermission(CustomActivity.this, Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(CustomActivity.this, permission)) {
//                TODO: show explanation based on Manifest.permission.<name>
                        Log.e("AZAZA", "permission explanation needed");
                    } else {
                        ActivityCompat.requestPermissions(CustomActivity.this, new String[]{permission}, code);
                    }
                } else {
                    handler.onPermissionGranted();
                }
        }


    }

    private boolean isGPSEnabled() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    private boolean isInternet() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == SETTINGS_REQUEST_LOCATION)return;

//        TODO: shitcode here
        if (listener != null) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
        synchronized (CustomActivity.class) {
            runningActivitiesCount--;
            toggleFloatingViewService(isRunning());
        }
        RequestPermissionHandler handler = permissionHandlers.get(requestCode);
        String requestedPermission = "";
        for (String s : permissionRequestCodes.keySet()) {
            if (requestCode == permissionRequestCodes.get(s)) {
                requestedPermission = s;
                break;
            }
        }
        if (Manifest.permission.SYSTEM_ALERT_WINDOW.equals(requestedPermission)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(CustomActivity.this)) {
                    handler.onPermissionGranted();
                } else {
                    handler.onPermissionDenied();
                }
            } else {
                handler.onPermissionDenied();
            }
        }
        if (Manifest.permission.INTERNET.equals(requestedPermission)) {
            if (isInternet()) {
                handler.onPermissionGranted();
            } else {
                handler.onPermissionDenied();
            }
        }
        if (Manifest.permission.LOCATION_HARDWARE.equals(requestedPermission)) {
            if (isGPSEnabled()) {
                handler.onPermissionGranted();
            } else {
                handler.onPermissionDenied();
            }
        }
        if (PERMISSION_ENABLE_LOCATION.equals(requestedPermission)) {
            if (isGPSEnabled()) {
                handler.onPermissionGranted();
            } else {
                handler.onPermissionDenied();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (permissionRequestCodes.get(permission) == requestCode) {
                RequestPermissionHandler handler = permissionHandlers.get(requestCode);
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    handler.onPermissionGranted();
                } else {
                    handler.onPermissionDenied();
                }
            }
        }
    }

    public static abstract class RequestPermissionHandler {
        public abstract void onPermissionGranted();

        public abstract void onPermissionDenied();
    }

    public static class RefactoringException extends Exception {
        public RefactoringException(String message) {
            super(message);
        }

        @Override
        public void printStackTrace() {
            Log.e("AZAZA", "REFACTORING ERROR: " + getMessage());
            super.printStackTrace();
        }
    }


    private void initKeyboardObserver() {
        final int MIN_KEYBOARD_HEIGHT_PX = 150;

        final View decorView = (ViewGroup) this.getWindow().getDecorView();

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect windowVisibleDisplayFrame = new Rect();
            private int lastVisibleDecorViewHeight;

            @Override
            public void onGlobalLayout() {
                // Retrieve visible rectangle inside window.
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                // Decide whether keyboard is visible from changing decor view height.
                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                        // Calculate current keyboard height (this includes also navigation bar height when in fullscreen mode).
                        int currentKeyboardHeight = decorView.getHeight() - windowVisibleDisplayFrame.bottom;
                        // Notify listener about keyboard being shown.
                        onKeyboardShown();
                    } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        // Notify listener about keyboard being hidden.
                        onKeyboardHidden();
                    }
                }
                // Save current decor view height for the next call.
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        });
    }


    @Override
    public void onMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .show();
    }


    @Override
    public void onKeyboardShown() {

    }

    @Override
    public void onKeyboardHidden() {

    }

    private static volatile LoadingProgress spinner;

    @Override
    public void showSpinner() {
        runOnUiThread(() -> {
            if (spinner == null) {
                spinner = new LoadingProgress(this);
            }
            spinner.startLoading();
        });
    }

    @Override
    public void hideSpinner() {
        runOnUiThread(() -> {
            if (spinner != null) {
                spinner.stopLoading();
            }
        });
    }


}
