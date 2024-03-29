package com.pnrhunter.utils;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.pnrhunter.HollerApplication;
import com.pnrhunter.FloatingViewService.FloatingViewListener;
import com.pnrhunter.FloatingViewService.FloatingViewManager;
import com.pnrhunter.R;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.main.UserModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import io.reactivex.Observable;

public class FloatingViewService extends Service implements FloatingViewListener {

    private FloatingViewManager mFloatingViewManager;
    private static final String ARG_POSITION_X = "floating_view_position_x";
    private static final String ARG_POSITION_Y = "floating_view_position_y";

    private View icon;
    private View text;
    private boolean orderButtonLocked = false;
    private List<View> buttons;

    @Inject protected Context context;
    @Inject protected RouterModule.Router router;
    @Inject protected RetrofitModule.ServerAPI serverAPI;
    @Inject protected UserModel userModel;

    private MediaPlayer passItOnSound;
    private MediaPlayer errorSound;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatingViewManager == null) {
            if(userModel.isLoggedIn())
                initLayout();
            else
                stopSelf();
        }

        return START_REDELIVER_INTENT;
    }

    public FloatingViewService() {
        HollerApplication.getInstance().component().inject(this);
        passItOnSound = MediaPlayer.create(context, R.raw.pass_tone);
        errorSound = MediaPlayer.create(context, R.raw.error_tone);
    }

    private void initLayout() {
        Crashlytics.setUserEmail(userModel.getProfileData().email);

        buttons = new ArrayList<>();
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);

        View mFloatingView = inflater.inflate(R.layout.layout_floating_widget, null);
        icon = mFloatingView.findViewById(R.id.fw_order_icon);
        text = mFloatingView.findViewById(R.id.fw_order_text);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/montserrat_extrabold.ttf");
        ((TextView)text).setTypeface(face);
        View openAppButton = mFloatingView.findViewById(R.id.fw_app_button);
        View orderButton = mFloatingView.findViewById(R.id.fw_order_button);
        View orderButtonContainer = mFloatingView.findViewById(R.id.fw_order_button_container);

        buttons.add(openAppButton);
        buttons.add(orderButtonContainer);

        View.OnClickListener handler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.fw_order_icon:
                    case R.id.fw_order_text:
                    case R.id.fw_order_button:
                    case R.id.fw_order_button_container:
                        if (!orderButtonLocked)
                            getLocation();
                        break;
                    case R.id.fw_app_button:
                        router.goToMainScreen();
                        break;
                    default:
                        Log.d("AZAZA", v.getId() + "");
                }

            }
        };


        openAppButton.setOnClickListener(handler);
        orderButton.setOnClickListener(handler);
        mFloatingView.setOnClickListener(handler);

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_cancel);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_action);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);

        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        SharedPreferences storedPosition = getSharedPreferences("floating_view", Context.MODE_PRIVATE);
        options.moveDirection = FloatingViewManager.MOVE_DIRECTION_DEFAULT;
        options.floatingViewX = storedPosition.getInt(ARG_POSITION_X, 500);
        options.floatingViewY = storedPosition.getInt(ARG_POSITION_Y, 400);

        mFloatingViewManager.addViewToWindow(mFloatingView, options);
    }

    private void getLocation() {
        //TODO: refact with rx java
        showSpinnerAndLockButton();

        Intent gpsTrackerBinding = new Intent(this, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                GPSTracker.GPSTrackerBinder service = (GPSTracker.GPSTrackerBinder) binder;
                Location location = service.getLocation();
                createAndSendOrder(location);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                //TODO: update spinner end error handler
            }
        };
        bindService(gpsTrackerBinding, gpsTrackerConnection, Context.BIND_AUTO_CREATE);
    }

    private void showSpinnerAndLockButton() {
        orderButtonLocked = true;
//            ((ImageView) icon).setImageDrawable(HollerApplication.getInstance().getDrawable(R.drawable.ic_autorenew_black_24dp));
        changeIcon(R.drawable.ic_autorenew_black_24dp);
        RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(1300);

        icon.startAnimation(animation);
    }

    private void hideSpinnerAndUnlockButton() {
        orderButtonLocked = false;
        orderButtonLocked = true;
        icon.setAnimation(null);
        Observable
                .timer(1500, TimeUnit.MILLISECONDS)
                .doOnComplete(() -> {
                    orderButtonLocked = false;
                    changeIcon(0);
                })
                .subscribe();

    }

    private void changeIcon(int resourceId) {
        runOnUiThread(() -> {
            if (resourceId == 0) {
                ((ImageView) icon).setImageDrawable(null);
                ((TextView) text).setText(context.getString(R.string.ma_map_pas_it_on));
                return;
            }
            ((ImageView) icon).setImageDrawable(HollerApplication.getInstance().getDrawable(resourceId));
            ((TextView) text).setText("");
        });
    }

    private void showToast(int stringResourceId) {
        runOnUiThread(() -> {
            String message = HollerApplication.getInstance().getString(stringResourceId);
            Toast.makeText(HollerApplication.getInstance(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void createAndSendOrder(Location location) {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            showToast(R.string.error_permission_denied);
            return;
        }

        if (location == null) {
            changeIcon(R.drawable.ic_close_yellow);
            showToast(R.string.error_no_location);
            hideSpinnerAndUnlockButton();
            return;
        }


        String lat = "" + location.getLatitude();
        String lon = "" + location.getLongitude();

        serverAPI.createOrder(userModel.getAuthHeader(),
                new RetrofitModule.ServerAPI.CreateOrderRequestBody(lat, lon))
                .toObservable()
                .flatMap(createOrderResponse -> {
                    if(createOrderResponse.isSuccessfullyCreated()){
                        changeIcon(R.drawable.ic_check_yellow);
                        restartSuccessSound();
                    }else{
                        changeIcon(R.drawable.ic_close_yellow);
                        restartErrorSound();
                    }



                    runOnUiThread(() -> {
                        Toast.makeText(HollerApplication.getInstance(), createOrderResponse.message, Toast.LENGTH_LONG).show();
                    });
//
//                    Crashlytics.log(Log.ERROR,Crashlytics.TAG, createOrderResponse.isSuccessfullyCreated() + " lat: "+lat+" lon: "+lon);
//                    Crashlytics.getInstance().crash();

                    return Observable.empty();
                })
                .doOnError(throwable -> {
                    changeIcon(R.drawable.ic_close_yellow);
                    showToast(R.string.error_creating_order);
//
//                    Crashlytics.log(Log.ERROR,Crashlytics.TAG,"CreateOrderError");
//                    Crashlytics.logException(throwable);
//                    Crashlytics.getInstance().crash();
                })
                .doOnComplete(() -> {
                    hideSpinnerAndUnlockButton();
                })
                .doFinally(() -> {
                    hideSpinnerAndUnlockButton();
                })
                .subscribe();

    }

    private void restartSuccessSound() throws IOException {
        passItOnSound.stop();
        passItOnSound = MediaPlayer.create(context, R.raw.pass_tone);
        passItOnSound.start();
    }

    private void restartErrorSound() {
        errorSound.stop();
        errorSound = MediaPlayer.create(context, R.raw.error_tone);
        errorSound.start();
    }

    private void runOnUiThread(Runnable r) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(r);
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new FloatingViewBinder();
    }

    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }

    @Override
    public void onTouchFinished(boolean isFinished, int x, int y) {
        //save position to shared preferences
        if (isFinished) return;
        SharedPreferences.Editor positionStorage = getSharedPreferences("floating_view", Context.MODE_PRIVATE).edit();
        positionStorage.putInt(ARG_POSITION_X, x);
        positionStorage.putInt(ARG_POSITION_Y, y);
        positionStorage.apply();
    }

    private void destroy() {
        if (mFloatingViewManager != null) {
            mFloatingViewManager.removeAllViewToWindow();
            mFloatingViewManager = null;
        }
    }

    public class FloatingViewBinder extends Binder {

        public void showButtons(){
            for(View b : buttons){
                b.setVisibility(View.VISIBLE);
            }
        }
        public void hideButtons(){
            for(View b : buttons){
                b.setVisibility(View.GONE);
            }
        }

    }


}
