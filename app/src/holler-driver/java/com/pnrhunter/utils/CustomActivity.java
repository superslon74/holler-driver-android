package com.pnrhunter.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.material.snackbar.Snackbar;
import com.pnrhunter.R;
import com.pnrhunter.mvp.main.MainView;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;


public class CustomActivity
        extends AppCompatActivity
        implements SpinnerShower, KeyboardObserver, MessageDisplayer {


    private LoadingView loadingView;

    private FloatingViewSwitcher switcher ;
    private PermissionChecker checker ;

    @Override
    protected void onStop() {
        super.onStop();
        switcher.onActivityCountDecreased();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadingView = findViewById(R.id.loading_view);
        switcher.onActivityCountIncreased();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            switcher.onActivityCountDecreased();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode != -1) {
            switcher.onActivityCountIncreased();
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
        switcher = new FloatingViewSwitcher(this);
        checker = new PermissionChecker(this);
        initKeyboardObserver();
        loadingView = findViewById(R.id.loading_view);

    }


    public Observable<Boolean> checkPermissionAsynchronously(String permission) {
        return checker.checkPermissionAsynchronously(permission);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (listener != null) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
        switcher.onActivityCountDecreased();
        checker.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checker.onRequestPermissionResult(requestCode,permissions,grantResults);
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
