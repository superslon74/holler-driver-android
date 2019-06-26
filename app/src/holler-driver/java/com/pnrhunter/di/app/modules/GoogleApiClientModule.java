package com.pnrhunter.di.app.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GoogleApiClientModule {

    @Provides
    @Singleton
    public GoogleApiClient provideGoogleApiClient(Context context){
        return  new GoogleApiClient
                    .Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d("AZAZA", "GoogleApi connected");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
//                        throw new RuntimeException("GoogleApi connection suspended with flag: "+i);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
//                        throw new RuntimeException("GoogleApi connection failed with result: "+connectionResult.toString());
                        }
                    })
                    .build();

    }

}
