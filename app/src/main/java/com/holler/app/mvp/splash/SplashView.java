package com.holler.app.mvp.splash;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.holler.app.AndarApplication;
import com.holler.app.BuildConfig;
import com.holler.app.FCM.ForceUpdateChecker;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.di.Presenter;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerSplashComponent;
import com.holler.app.di.app.components.splash.modules.SplashScreenModule;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.GPSTracker;
import com.orhanobut.logger.Logger;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

public class SplashView
        extends CustomActivity
        implements ForceUpdateChecker.OnUpdateNeededListener, SplashPresenter.View{

    @Inject public Presenter presenter;
    @BindView(R.id.sp_version) protected TextView versionView;
    @BindView(R.id.sp_logo) protected ImageView logoView;
//    @BindView(R.id.sp_update_button) protected TextView updateButton;

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
        startService(new Intent(this, GPSTracker.class));
        requestPermissions();
    }



    private void requestPermissions(){
        final CustomActivity activity = this;

        RequestPermissionChain chain = new RequestPermissionChain(){
            @Override
            protected void onFinished(boolean allPermissionGranted) {
                if(allPermissionGranted)
                    onAllPermissionsGranted();
                else{
                    showCompletableMessage(getString(R.string.error_permission_required))
                            .doOnComplete(() -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .subscribe();
                }
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
        private boolean allPermissionGranted = true;

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
                onChecked();
                return;
            }

            RequestPermissionHandler handler = new RequestPermissionHandler() {
                @Override
                public void onPermissionGranted() {
                    onChecked();
                }

                @Override
                public void onPermissionDenied() {
                    Logger.d("Permission denied, repeating request");
                    chain.allPermissionGranted=false;
                    onChecked();
                }
            };

            activity.checkPermissionAsynchronously(permission,handler);
        }

        private void onChecked(){
            if(next == null){
                onFinished(this.allPermissionGranted);
                return;
            }
            next.call();
        }

        public RequestPermissionChain next(RequestPermissionChain next){
            next.chain = this.chain;
            this.next = next;
            return next;
        }

        protected void onFinished(boolean allPermissionGranted){
            chain.onFinished(chain.allPermissionGranted);
        }



    }

    private void onAllPermissionsGranted(){
        presenter.checkVersion();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        versionView.setText(
                getResources().getString(R.string.was_powered_by) +"\n "+
                getResources().getString(R.string.was_version)+ " "+ BuildConfig.VERSION_NAME+"."+BuildConfig.VERSION_CODE);

        RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(5000);

        logoView.startAnimation(animation);

        setupComponent();


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
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

    private String appUrl;
    @Override
    public void showAppLinkDialog(String appUrl){
        final AlertDialog updateDialog = new AlertDialog
                .Builder(this)
                .setTitle(getString(R.string.sp_update_title))
                .setMessage(getString(R.string.sp_update_message))
                .setPositiveButton(R.string.sp_update_title_button_confirm, (dialog, which) -> {
                    goToPlayStore();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.sp_update_title_button_cancel, (dialog, which) -> {
                    presenter.goAhead();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create();
        updateDialog.show();
    }

    //    @OnClick(R.id.sp_update_button)
    public void goToPlayStore(){
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}
