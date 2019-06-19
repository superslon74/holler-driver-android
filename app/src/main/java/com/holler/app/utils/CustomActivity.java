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
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.transform.Source;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class CustomActivity
        extends AppCompatActivity
        implements SpinnerShower, KeyboardObserver, MessageDisplayer {


    private static volatile int runningActivitiesCount = 0;
    private ServiceConnection gpsTrackerServiceConnection;
    private ServiceConnection floatingViewServiceConnection;
    private LoadingView loadingView;
//    private FloatingViewService.FloatingViewBinder floatingView;

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
//        if(floatingView==null)return;

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
        loadingView = findViewById(R.id.loading_view);
        synchronized (CustomActivity.class) {
            runningActivitiesCount++;
            toggleFloatingViewService(isRunning());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(gpsTrackerServiceConnection);
        } catch (IllegalArgumentException e) {
            Logger.e("Can't unbind gps service.. ");
        }
        try {
            unbindService(floatingViewServiceConnection);
        } catch (IllegalArgumentException e) {
            Logger.e("Can't unbind floating service.. ");
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
        loadingView = findViewById(R.id.loading_view);
        this.floatingViewServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
//                floatingView = (FloatingViewService.FloatingViewBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
//        Intent flaotingViewBinding = new Intent(this, FloatingViewService.class);
//        this.bindService(flaotingViewBinding, this.floatingViewServiceConnection, Context.BIND_IMPORTANT);

//        for home listen
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

    //TODO: to strings
    static {
        permissionExplanation.put(Manifest.permission.READ_CONTACTS, "App needs access to contact to provide some feature");
        permissionExplanation.put(Manifest.permission.ACCESS_FINE_LOCATION, "App needs access to gps");
        permissionExplanation.put(Manifest.permission.CALL_PHONE, "App needs access to phone calls");
        permissionExplanation.put(Manifest.permission.SYSTEM_ALERT_WINDOW, "");
        permissionExplanation.put(Manifest.permission.READ_EXTERNAL_STORAGE, "App needs permissions to read external storage");
        permissionExplanation.put(Manifest.permission.CAMERA, "App needs permissions to read camera");
        permissionExplanation.put(Manifest.permission.INTERNET, "");
        permissionExplanation.put(Manifest.permission.LOCATION_HARDWARE, "App needs permissions to gps");
        permissionExplanation.put(PERMISSION_ENABLE_LOCATION, "");
    }

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

                this.gpsTrackerServiceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {

                        ((GPSTracker.GPSTrackerBinder) service).connectGoogleApi(
                                locationSettingsResponse -> {
                                    handler.onPermissionGranted();
                                    unbindService(CustomActivity.this.gpsTrackerServiceConnection);
                                },
                                e -> {
                                    if (e instanceof ResolvableApiException) {
                                        try {

                                            ResolvableApiException resolvable = (ResolvableApiException) e;
                                            resolvable.startResolutionForResult(CustomActivity.this, code);
                                        } catch (IntentSender.SendIntentException sendEx) {
                                            handler.onPermissionDenied();
                                        }
                                    }
                                    unbindService(CustomActivity.this.gpsTrackerServiceConnection);
                                });


                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        handler.onPermissionDenied();
                        unbindService(CustomActivity.this.gpsTrackerServiceConnection);
                    }

                };



                this.bindService(gpsTrackerBinding, this.gpsTrackerServiceConnection, Context.BIND_IMPORTANT);
                break;
            case Manifest.permission.INTERNET:
                if (isInternet()) {
                    handler.onPermissionGranted();
                } else {
                    showCompletableMessage("Please turn on internet..")
                            .doOnComplete(() -> {
                                final Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                startActivityForResult(intent, code);
                            })
                            .subscribe();
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
                if (ContextCompat.checkSelfPermission(CustomActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                    String explanation = permissionExplanation.get(permission);

                    if (ActivityCompat.shouldShowRequestPermissionRationale(CustomActivity.this, permission)) {
                        showCompletableMessage(explanation)
                                .doOnComplete(handler::onPermissionDenied)
                                .subscribe();
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


    @Override
    public void showMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .show();
    }

    public Completable showCompletableMessage(String message) {
        return showMessage(message, true);
    }

    public Completable showMessage(String message, boolean autocancelable) {
        Completable c = Completable.create(emitter -> {

            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            emitter.onComplete();
                        }
                    });

            snackbar.setAction("Close", v -> {
                snackbar.dismiss();
            });

            snackbar.show();

        });

        return c;
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
    public void onKeyboardShown() {

    }

    @Override
    public void onKeyboardHidden() {

    }

    private static volatile LoadingProgress spinner;

    @Override
    public void showSpinner() {
        runOnUiThread(() -> {
            if (loadingView != null) {
                loadingView.show();
            }
        });
    }

    @Override
    public void hideSpinner() {
        runOnUiThread(() -> {
            if (loadingView != null) {
                loadingView.hide();
            }
        });
    }


}
