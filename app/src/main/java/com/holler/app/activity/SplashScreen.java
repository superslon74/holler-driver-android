package com.holler.app.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.holler.app.BuildConfig;
import com.holler.app.FCM.ForceUpdateChecker;
import com.holler.app.Models.AccessDetails;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.AppComponent;
import com.holler.app.di.DaggerSplashScreenComponent;
import com.holler.app.di.Presenter;
import com.holler.app.di.SplashScreenModule;
import com.holler.app.di.SplashScreenPresenter;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.GPSTracker;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;


public class  SplashScreen
        extends CustomActivity
        implements ForceUpdateChecker.OnUpdateNeededListener, SplashScreenPresenter.View {

    @Inject
    Presenter presenter;

    private void setupComponent(){
        AppComponent appComponent = (AppComponent) AndarApplication.get(this).component();
        DaggerSplashScreenComponent.builder()
                .appComponent(appComponent)
                .splashScreenModule(new SplashScreenModule(this))
                .build()
                .inject(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }

    private void requestPermissions(){
        final CustomActivity activity = this;

        RequestPermissionChain chain = new RequestPermissionChain(){
            @Override
            protected void onFinished() {
                onAllPermissionsGranted();
            }
        };

        chain
                .next(new RequestPermissionChain(activity,Manifest.permission.SYSTEM_ALERT_WINDOW))
                .next(new RequestPermissionChain(activity,Manifest.permission.ACCESS_FINE_LOCATION))
                .next(new RequestPermissionChain(activity,Manifest.permission.INTERNET))
                .next(new RequestPermissionChain(activity,CustomActivity.PERMISSION_ENABLE_LOCATION));

        chain.call();
    }

    private class RequestPermissionChain {

        private String permission;
        private CustomActivity activity;
        private RequestPermissionChain next;
        private RequestPermissionChain chain;

        /**
         * use to build new chain
         */
        public RequestPermissionChain() {
            chain = this;
        }

        /**
         * use to add to chain
         * @param activity
         * @param permission
         */
        public RequestPermissionChain(final CustomActivity activity, String permission) {
            this.permission = permission;
            this.activity = activity;

        }

        public void call(){
            if(this.activity == null){
                onGranted();
                return;
            }

            RequestPermissionHandler handler = new RequestPermissionHandler() {
                @Override
                public void onPermissionGranted() {
                    onGranted();
                }

                @Override
                public void onPermissionDenied() {
                    Log.e("AZAZA","permission "+permission+" denied, requesting again");
                    call();
                }
            };

            activity.checkPermissionAsynchronously(permission,handler);
        }

        private void onGranted(){
            if(next == null){
                onFinished();
                return;
            }
            next.call();
        }

        public RequestPermissionChain next(RequestPermissionChain next){
            next.chain = this.chain;
            this.next = next;
            return next;
        }

        protected void onFinished(){
            chain.onFinished();
        }



    }

    private void onAllPermissionsGranted(){
        presenter.onResume();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
//        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        TextView lblVersion = (TextView) findViewById(R.id.lblVersion);
        lblVersion.setText(getResources().getString(R.string.version) +" "+ BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")");

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setupComponent();


    }



    @Override
    protected void onDestroy() {
//        handleCheckStatus.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void showDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(getString(R.string.connect_to_network))
//                .setIcon(AccessDetails.site_icon)
//                .setCancelable(false)
//                .setPositiveButton(getString(R.string.connect_to_wifi), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        alert.dismiss();
//                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
//                    }
//                })
//                .setNegativeButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        alert.dismiss();
//                        finish();
//                    }
//                });
//        if (alert == null) {
//            alert = builder.create();
//            alert.show();
//        }
    }


    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.new_version_available))
                .setIcon(AccessDetails.site_icon)
                .setMessage(getResources().getString(R.string.update_to_continue))
                .setPositiveButton(getResources().getString(R.string.update),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton(getResources().getString(R.string.no_thanks),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                .create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(SplashScreen.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void gotoActivity(Class<? extends CustomActivity> redirectTo) {
        if(redirectTo == MainActivity.class){
            Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            finish();
        }else if(redirectTo == WelcomeScreenActivity.class){
            Intent mainIntent = new Intent(SplashScreen.this, WelcomeScreenActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        }else{
            Intent mainIntent = new Intent(SplashScreen.this, WelcomeScreenActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        }
    }
}
