package com.pnrhunter.mvp.utils.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.orhanobut.logger.Logger;
import com.pnrhunter.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;

public class PermissionChecker {

    private ExtendedActivity activity;

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

    private static Map<Integer, ObservableEmitter<Boolean>> permissionHandlers;
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

    public PermissionChecker(ExtendedActivity context) {
        this.activity = context;
        initPermissionData(context);
    }

    public Observable<Boolean> checkPermissionAsynchronously(String permission) {

        final int code = permissionRequestCodes.get(permission);

        Observable<Boolean> handler = Observable.create(emitter -> {
            switch (permission) {
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                    requestSystemAlertSettings(code,emitter);
                    break;
                case PERMISSION_ENABLE_LOCATION:
                    requestEnableLocation(code, emitter);
                    break;
                case Manifest.permission.INTERNET:
                    requestInternetConnection(code, emitter);
                    break;
                case Manifest.permission.LOCATION_HARDWARE:
                    requestLocationPermission(code, emitter);
                    break;
                default:
                    requestPermission(code, permission, emitter);
            }

            permissionHandlers.put(code, emitter);
        });

        return handler;
    }

    private void requestSystemAlertSettings(int code, ObservableEmitter<Boolean> emitter){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, code);
            } else {
                emitter.onNext(true);
            }
        } else {
            emitter.onNext(false);
        }
    }

    private void requestEnableLocation(int code, ObservableEmitter<Boolean> emitter){
        connectGoogleApi()
                .doOnNext(isConnected -> {
                     emitter.onNext(true);
                })
                .doOnError(throwable -> {
                    if (throwable instanceof ResolvableApiException) {
                        try {

                            ResolvableApiException resolvable = (ResolvableApiException) throwable;
                            resolvable.startResolutionForResult(activity, code);
                        } catch (IntentSender.SendIntentException sendEx) {
                            emitter.onNext(false);
                        }
                    }
//                    activity.unbindService(PermissionChecker.this.gpsTrackerServiceConnection);
                })
                .subscribe();



    }

    private void requestInternetConnection(int code, ObservableEmitter<Boolean> emitter){
        if (isInternet()) {
            emitter.onNext(true);
        } else {
            ((ExtendedActivity) activity).showCompletableMessage(activity.getString(R.string.error_disabled_internet))
                    .doOnComplete(() -> {
                        final Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        activity.startActivityForResult(intent, code);
                    })
                    .subscribe();
        }
    }

    private void requestLocationPermission(int code, ObservableEmitter<Boolean> emitter){
        if (isGPSEnabled()) {
            emitter.onNext(true);
        } else {
            final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(intent, code);
        }
    }

    private void requestPermission(int code, String permission, ObservableEmitter<Boolean> emitter ){
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            String explanation = permissionExplanation.get(permission);

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ((ExtendedActivity) activity).showCompletableMessage(explanation)
                        .doOnComplete(() -> {
                            emitter.onNext(false);
                        })
                        .subscribe();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, code);
            }
        } else {
            emitter.onNext(true);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){

        ObservableEmitter<Boolean> handler = permissionHandlers.get(requestCode);

        String requestedPermission = "";
        for (String s : permissionRequestCodes.keySet()) {
            if (requestCode == permissionRequestCodes.get(s)) {
                requestedPermission = s;
                break;
            }
        }
        if (Manifest.permission.SYSTEM_ALERT_WINDOW.equals(requestedPermission)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(activity)) {
                    handler.onNext(true);
                } else {
                    handler.onNext(false);
                }
            } else {
                handler.onNext(false);
            }
        }
        if (Manifest.permission.INTERNET.equals(requestedPermission)) {
            if (isInternet()) {
                handler.onNext(true);
            } else {
                handler.onNext(false);
            }
        }
        if (Manifest.permission.LOCATION_HARDWARE.equals(requestedPermission)) {
            if (isGPSEnabled()) {
                handler.onNext(true);
            } else {
                handler.onNext(false);
            }
        }
        if (PERMISSION_ENABLE_LOCATION.equals(requestedPermission)) {
            if (isGPSEnabled()) {
                handler.onNext(true);
            } else {
                handler.onNext(false);
            }
        }
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (permissionRequestCodes.get(permission) == requestCode) {
                ObservableEmitter<Boolean> handler = permissionHandlers.get(requestCode);
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    handler.onNext(true);
                } else {
                    handler.onNext(false);
                }
            }
        }
    }

    private boolean isGPSEnabled() {
        LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    private boolean isInternet() {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        return true;
    }

    private Observable<Boolean> connectGoogleApi() {
        return Observable.<Boolean>create(emitter -> {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(500);
            locationRequest.setFastestInterval(100);

            LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .setAlwaysShow(false)
                    .build();

            Task<LocationSettingsResponse> locationSettingsResponse = LocationServices
                    .getSettingsClient(activity)
                    .checkLocationSettings(locationSettingsRequest);


            locationSettingsResponse.addOnSuccessListener((Executor) activity, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    emitter.onNext(true);
                }
            });
            locationSettingsResponse.addOnFailureListener((Executor) activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    emitter.onError(e);
                }
            });

            GoogleApiClient googleApiClient = new GoogleApiClient
                    .Builder(activity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Logger.d("Google api connected");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Exception error = new RuntimeException("GoogleApi connection suspended with flag: "+i);
                            emitter.onError(error);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Exception error = new RuntimeException("GoogleApi connection failed with result: "+connectionResult.toString());
                            emitter.onError(error);
                        }
                    })
                    .build();

            googleApiClient.connect();


        });


    }


}
