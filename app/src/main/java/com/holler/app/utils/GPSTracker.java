package com.holler.app.utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.gson.JsonObject;
import com.holler.app.AndarApplication;
import com.holler.app.di.components.app.modules.RetrofitModule;
import com.holler.app.di.components.app.modules.UserStorageModule;
import com.holler.app.server.OrderServerApi;

import javax.inject.Inject;

import retrofit2.Response;

public class GPSTracker
        extends Service
        implements LocationListener {



    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 3;

    private Location lastLocation;
    private Handler sender;

    private LocationManager locationManager;

    @Inject public Context context;
    @Inject public GoogleApiClient googleApiClient;
    @Inject public RetrofitModule.ServerAPI serverAPI;
    @Inject public UserStorageModule.UserStorage userStorage;

    private GPSTrackerBinder binder;

    public GPSTracker() {
        AndarApplication.getInstance().component().inject(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        sender = new Handler();
        binder = new GPSTrackerBinder();
    }

    @SuppressLint("MissingPermission")
    private void connectGoogleApi(ResultCallback<LocationSettingsResult> locationSettingsResultCallback ) {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(false)
                .build();

        LocationServices
                .SettingsApi
                .checkLocationSettings(googleApiClient, locationSettingsRequest)
                .setResultCallback(locationSettingsResultCallback);

        googleApiClient.connect();
    }

    @SuppressLint("MissingPermission")
    public Location getLocation() {
        if(lastLocation == null){
            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(last == null)
                last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(last!=null){
                lastLocation = last;
            }
        }
        return lastLocation;
    }

    private void stopReceiveLocationUpdates() {
        locationManager.removeUpdates(GPSTracker.this);
    }

    @SuppressLint("MissingPermission")
    private void startReceiveLocationUpdates(){
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, GPSTracker.this);
    }


    private Runnable updateLocationLooperBody = new Runnable() {
        @Override
        public void run() {
            try{
                sendLocation(getLocation());
            }finally {
                sender.postDelayed(updateLocationLooperBody, MIN_TIME_BW_UPDATES);
            }
        }
    };

    private void startSendingLocation(){
        sender.postDelayed(updateLocationLooperBody, MIN_TIME_BW_UPDATES);
    }

    private void stopSendingLocation(){
        sender.removeCallbacksAndMessages(null);
    }

    public boolean isNetworkEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isGPSEnabled(){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.lastLocation = location;
        }
    }


    private void sendLocation(Location l){
        try {
            String latitude = ""+l.getLatitude();
            String longitude = ""+l.getLongitude();
            String authHeader = "Bearer " + userStorage.getAccessToken();
            this.serverAPI
                    .sendTripLocation(authHeader, latitude, longitude)
                    .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(null) {
                        @Override
                        public void onSuccessfulResponse(Response<JsonObject> response) {
                            Log.d("AZAZA", "GPSTracker: Location updated");
                        }

                        @Override
                        public void onDisplayMessage(String message) {
                            Log.d("AZAZA", "GPSTracker: message-> " +message);
                        }
                    });
        }catch (NullPointerException e){
            Log.e("AZAZA","GPSTracker error: lastLocation not defined");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        //TODO: check provider & enable listening
        Toast.makeText(context, provider + " enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //TODO: check provider & disable listening
        Toast.makeText(context, provider + " disabled", Toast.LENGTH_LONG).show();
    }

    @Deprecated
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class GPSTrackerBinder extends Binder{

        public void connectGoogleApi(ResultCallback<LocationSettingsResult> locationSettingsResultCallback){
            GPSTracker.this.connectGoogleApi(locationSettingsResultCallback);
        }

        public void startTracking(){
            //TODO: start listenening fo updates
            //TODO: make server calls on each location changed
            //TODO: implement location listener
            GPSTracker.this.startReceiveLocationUpdates();
            GPSTracker.this.startSendingLocation();
        }

        public void stopTracking(){
            GPSTracker.this.stopReceiveLocationUpdates();
            GPSTracker.this.stopSendingLocation();
        }

        public Location getLocation(){
            return GPSTracker.this.getLocation();
        }


    }

}