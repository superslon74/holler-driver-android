package com.pnrhunter.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.pnrhunter.R;

import java.util.HashMap;
import java.util.Map;

public class PermissionChecker {

    private Activity context;
    private ServiceConnection gpsTrackerServiceConnection;


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

    private static Map<String, Integer> permissionRequestCodes;

    private static Map<Integer, CustomActivity.RequestPermissionHandler> permissionHandlers;
    private static Map<String, String> permissionExplanation;

    private static void initPermissionData(Context context){
        if(permissionRequestCodes!=null && permissionExplanation!=null) return;
        permissionRequestCodes = new HashMap<>();

        permissionRequestCodes.put(Manifest.permission.READ_CONTACTS, CODE_REQUEST_READ_CONTACTS);
        permissionRequestCodes.put(Manifest.permission.ACCESS_FINE_LOCATION, CODE_REQUEST_ACCESS_FINE_LOCATION);
        permissionRequestCodes.put(Manifest.permission.CALL_PHONE, CODE_REQUEST_CALL_PHONE);
        permissionRequestCodes.put(Manifest.permission.SYSTEM_ALERT_WINDOW, CODE_REQUEST_SYSTEM_ALERT_WINDOW);
        permissionRequestCodes.put(Manifest.permission.READ_EXTERNAL_STORAGE, CODE_REQUEST_READ_EXTERNAL_STORAGE);
        permissionRequestCodes.put(Manifest.permission.CAMERA, CODE_REQUEST_CAMERA);
        permissionRequestCodes.put(Manifest.permission.INTERNET, CODE_REQUEST_INTERNET);
        permissionRequestCodes.put(Manifest.permission.LOCATION_HARDWARE, CODE_REQUEST_LOCATION);
        permissionRequestCodes.put(PERMISSION_ENABLE_LOCATION, CODE_ENABLE_LOCATION);

        permissionHandlers = new HashMap<>();

        permissionExplanation = new HashMap<>();

        permissionExplanation.put(Manifest.permission.READ_CONTACTS, context.getString(R.string.error_permission_explanation_contacts));
        permissionExplanation.put(Manifest.permission.ACCESS_FINE_LOCATION, context.getString(R.string.error_permission_explanation_gps));
        permissionExplanation.put(Manifest.permission.CALL_PHONE, context.getString(R.string.error_permission_explanation_phone));
        permissionExplanation.put(Manifest.permission.SYSTEM_ALERT_WINDOW, "");
        permissionExplanation.put(Manifest.permission.READ_EXTERNAL_STORAGE, context.getString(R.string.error_permission_explanation_storage));
        permissionExplanation.put(Manifest.permission.CAMERA, context.getString(R.string.error_permission_explanation_camera));
        permissionExplanation.put(Manifest.permission.INTERNET, "");
        permissionExplanation.put(Manifest.permission.LOCATION_HARDWARE, context.getString(R.string.error_permission_explanation_gps));
        permissionExplanation.put(PERMISSION_ENABLE_LOCATION, "");
    }

    public PermissionChecker(Activity context) {
        this.context = context;
        initPermissionData(context);
    }

    public void checkPermissionAsynchronously(String permission, final CustomActivity.RequestPermissionHandler handler) {

        final int code = permissionRequestCodes.get(permission);
        permissionHandlers.put(code, handler);

        ///
        switch (permission) {
            case Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(context)) {
                        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                        context.startActivityForResult(intent, code);
                    } else {
                        handler.onPermissionGranted();
                    }
                } else {
                    handler.onPermissionDenied();
                }
                break;
            case PERMISSION_ENABLE_LOCATION:
                Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);

                this.gpsTrackerServiceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {

                        ((GPSTracker.GPSTrackerBinder) service).connectGoogleApi(
                                locationSettingsResponse -> {
                                    handler.onPermissionGranted();
                                    context.unbindService(PermissionChecker.this.gpsTrackerServiceConnection);
                                },
                                e -> {
                                    if (e instanceof ResolvableApiException) {
                                        try {

                                            ResolvableApiException resolvable = (ResolvableApiException) e;
                                            resolvable.startResolutionForResult(context, code);
                                        } catch (IntentSender.SendIntentException sendEx) {
                                            handler.onPermissionDenied();
                                        }
                                    }
                                    context.unbindService(PermissionChecker.this.gpsTrackerServiceConnection);
                                });


                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        handler.onPermissionDenied();
                        context.unbindService(PermissionChecker.this.gpsTrackerServiceConnection);
                    }

                };



                context.bindService(gpsTrackerBinding, this.gpsTrackerServiceConnection, Context.BIND_IMPORTANT);
                break;
            case Manifest.permission.INTERNET:
                if (isInternet()) {
                    handler.onPermissionGranted();
                } else {
                    ((CustomActivity)context).showCompletableMessage(context.getString(R.string.error_disabled_internet))
                            .doOnComplete(() -> {
                                final Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                context.startActivityForResult(intent, code);
                            })
                            .subscribe();
                }
                break;
            case Manifest.permission.LOCATION_HARDWARE:
                if (isGPSEnabled()) {
                    handler.onPermissionGranted();
                } else {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivityForResult(intent, code);
                }
                break;
            default:
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    String explanation = permissionExplanation.get(permission);

                    if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                        ((CustomActivity)context).showCompletableMessage(explanation)
                                .doOnComplete(handler::onPermissionDenied)
                                .subscribe();
                    } else {
                        ActivityCompat.requestPermissions(context, new String[]{permission}, code);
                    }
                } else {
                    handler.onPermissionGranted();
                }
        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        CustomActivity.RequestPermissionHandler handler = permissionHandlers.get(requestCode);
        String requestedPermission = "";
        for (String s : permissionRequestCodes.keySet()) {
            if (requestCode == permissionRequestCodes.get(s)) {
                requestedPermission = s;
                break;
            }
        }
        if (Manifest.permission.SYSTEM_ALERT_WINDOW.equals(requestedPermission)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
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

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (permissionRequestCodes.get(permission) == requestCode) {
                CustomActivity.RequestPermissionHandler handler = permissionHandlers.get(requestCode);
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    handler.onPermissionGranted();
                } else {
                    handler.onPermissionDenied();
                }
            }
        }
    }

    private boolean isGPSEnabled() {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    private boolean isInternet() {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        return true;
    }


}
