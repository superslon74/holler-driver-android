package com.holler.app.utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
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

import com.google.gson.JsonObject;
import com.holler.app.activity.MainActivity;
import com.holler.app.AndarApplication;
import com.holler.app.FloatingViewService.FloatingViewListener;
import com.holler.app.FloatingViewService.FloatingViewManager;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.MainView;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.server.OrderServerApi;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class FloatingViewService extends Service implements FloatingViewListener {

    private FloatingViewManager mFloatingViewManager;
    private static final String ARG_POSITION_X = "floating_view_position_x";
    private static final String ARG_POSITION_Y = "floating_view_position_y";

    private View icon;
    private View text;
    private boolean orderButtonLocked = false;

    @Inject
    protected Context context;
    @Inject
    protected RouterModule.Router router;
    @Inject
    protected RetrofitModule.ServerAPI serverAPI;
    @Inject
    protected UserModel userModel;

    private MediaPlayer passItOnSound;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatingViewManager == null) {
            initLayout();
        }

        return flags;
    }

    public FloatingViewService() {
        AndarApplication.getInstance().component().inject(this);
        passItOnSound = MediaPlayer.create(context, R.raw.pass_it_on);
    }

    private void initLayout() {
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
//            ((ImageView) icon).setImageDrawable(AndarApplication.getInstance().getDrawable(R.drawable.ic_autorenew_black_24dp));
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
            ((ImageView) icon).setImageDrawable(AndarApplication.getInstance().getDrawable(resourceId));
            ((TextView) text).setText("");
        });
    }

    private void showToast(int stringResourceId) {
        runOnUiThread(() -> {
            String message = AndarApplication.getInstance().getString(stringResourceId);
            Toast.makeText(AndarApplication.getInstance(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void createAndSendOrder(Location location) {
        if (location == null) {
            changeIcon(R.drawable.ic_close_yellow);
            showToast(R.string.error_no_location);
            hideSpinnerAndUnlockButton();
            return;
        }

        passItOnSound.start();
        OrderServerApi.Order order = new OrderServerApi.Order();

        String lat = "" + location.getLatitude();
        String lon = "" + location.getLongitude();

        String MESSAGE_REQUEST_SUCCESFULL = "New request Created!";
        serverAPI.createOrder(userModel.getAuthHeader(),
                new RetrofitModule.ServerAPI.CreateOrderRequestBody(lat, lon))
                .toObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(createOrderResponse -> {
                    if (MESSAGE_REQUEST_SUCCESFULL.equals(createOrderResponse.message)) {
                        changeIcon(R.drawable.ic_check_yellow);
//                        showToast(R.string.successfully_created_order);
                    } else {
                        changeIcon(R.drawable.ic_close_yellow);
//                        showToast(R.string.error_creating_order);
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(AndarApplication.getInstance(), createOrderResponse.message, Toast.LENGTH_LONG).show();
                    });
                    return Observable.empty();
                })
                .doOnError(throwable -> {
                    changeIcon(R.drawable.ic_close_yellow);
                    showToast(R.string.error_creating_order);
                })
                .doOnComplete(() -> {
                    hideSpinnerAndUnlockButton();
                })
                .doFinally(() -> {
                    hideSpinnerAndUnlockButton();
                })
                .subscribe();

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
        return null;
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


}
