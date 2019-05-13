package com.holler.app.mvp.splash;

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
import com.holler.app.activity.MainActivity;
import com.holler.app.di.Presenter;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerSplashComponent;
import com.holler.app.di.app.components.splash.modules.SplashScreenModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.utils.CustomActivity;
import com.orhanobut.logger.Logger;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;


public class SplashView
        extends CustomActivity
        implements ForceUpdateChecker.OnUpdateNeededListener, SplashPresenter.View {

    @Inject
    public Presenter presenter;

    private void setupComponent(){
        AppComponent appComponent = (AppComponent) AndarApplication.getInstance().component();
        DaggerSplashComponent.builder()
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
                .next(new RequestPermissionChain(activity,Manifest.permission.CAMERA))
                .next(new RequestPermissionChain(activity,Manifest.permission.READ_EXTERNAL_STORAGE))
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
                    Logger.d("Permission denied, repeating request");
                    onGranted();
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

        TextView lblVersion = (TextView) findViewById(R.id.sp_version);
        lblVersion.setText(
                getResources().getString(R.string.was_powered_by) +"\n "+
                getResources().getString(R.string.was_version)+ " "+ BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")");

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setupComponent();


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
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

}
