package com.pnrhunter.mvp.splash;

import android.Manifest;
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

import com.pnrhunter.HollerApplication;
import com.pnrhunter.BuildConfig;
import com.pnrhunter.FCM.ForceUpdateChecker;
import com.pnrhunter.Models.AccessDetails;
import com.pnrhunter.R;
import com.pnrhunter.di.Presenter;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.DaggerSplashComponent;
import com.pnrhunter.di.app.components.splash.modules.SplashScreenModule;
import com.pnrhunter.utils.CustomActivity;
import com.pnrhunter.utils.GPSTracker;
import com.orhanobut.logger.Logger;
import com.pnrhunter.utils.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;

public class SplashView
        extends CustomActivity
        implements ForceUpdateChecker.OnUpdateNeededListener, SplashPresenter.View{

    @Inject public Presenter presenter;
    @BindView(R.id.sp_version) protected TextView versionView;
    @BindView(R.id.sp_logo) protected ImageView logoView;
//    @BindView(R.id.sp_update_button) protected TextView updateButton;

    private void setupComponent(){
        AppComponent appComponent = (AppComponent) HollerApplication.getInstance().component();
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
        List<Boolean> grantPermissionsResult = new ArrayList<>();
        checkPermissionAsynchronously(Manifest.permission.SYSTEM_ALERT_WINDOW)
                .flatMap(granted -> {
                    grantPermissionsResult.add(granted);
                    return checkPermissionAsynchronously(Manifest.permission.ACCESS_FINE_LOCATION);
                })//ACCESS_FINE_LOCATION
                .flatMap(granted -> {
                    grantPermissionsResult.add(granted);
                    return checkPermissionAsynchronously(Manifest.permission.INTERNET);
                })//INTERNET
//                .flatMap(granted -> {
//                    grantPermissionsResult.add(granted);
//                    return checkPermissionAsynchronously(Manifest.permission.CAMERA);
//                })
//                .flatMap(granted -> {
//                    grantPermissionsResult.add(granted);
//                    return checkPermissionAsynchronously(Manifest.permission.READ_EXTERNAL_STORAGE);
//                })
                .flatMap(granted -> {
                    grantPermissionsResult.add(granted);
                    return checkPermissionAsynchronously(PermissionChecker.PERMISSION_ENABLE_LOCATION);
                })//PERMISSION_ENABLE_LOCATION
                .flatMap(granted -> {
                    grantPermissionsResult.add(granted);
                    boolean allPermissionsGranted = !grantPermissionsResult.contains(false);
                    return Observable.just(allPermissionsGranted);
                })
                .doOnNext(allPermissionsGranted -> {
                    if(allPermissionsGranted)
                        presenter.checkVersion();
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
                })
                .subscribe();
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
