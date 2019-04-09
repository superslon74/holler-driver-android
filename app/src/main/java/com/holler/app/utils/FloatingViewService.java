package com.holler.app.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.holler.app.Activity.MainActivity;
import com.holler.app.AndarApplication;
import com.holler.app.FloatingViewService.FloatingViewListener;
import com.holler.app.FloatingViewService.FloatingViewManager;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.server.OrderServerApi;

import java.util.HashMap;

import okhttp3.ResponseBody;

public class FloatingViewService extends Service implements FloatingViewListener {

    private FloatingViewManager mFloatingViewManager;
    private static final String ARG_POSITION_X = "floating_view_position_x";
    private static final String ARG_POSITION_Y = "floating_view_position_y";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloatingViewManager == null) {
            initLayout();
        }

        return START_REDELIVER_INTENT;
    }

    private void initLayout() {
        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);

        View mFloatingView = inflater.inflate(R.layout.layout_floating_widget, null);

        View openAppButton = mFloatingView.findViewById(R.id.collapsed_iv);
        View orderButton = mFloatingView.findViewById(R.id.orderbtn);


        View.OnClickListener handler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.orderbtn:
                        createAndSendOrder();
                        break;
                    default:
                        Intent intent = new Intent(FloatingViewService.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                }

            }
        };


        openAppButton.setOnClickListener(handler);
        orderButton.setOnClickListener(handler);
        mFloatingView.setOnClickListener(handler);

//        mFloatingView.setOnClickListener();


        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_cancel);
        mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_action);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);

        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        SharedPreferences storedPosition = getSharedPreferences("floating_view", Context.MODE_PRIVATE);
        options.floatingViewX = storedPosition.getInt(ARG_POSITION_X, 0);
        options.floatingViewY = storedPosition.getInt(ARG_POSITION_Y, 0);

        mFloatingViewManager.addViewToWindow(mFloatingView, options);
    }

    private void showSpinner() {

    }

    private void hideSpinner() {

    }

    private void createAndSendOrder() {
        showSpinner();
        OrderServerApi serverApiClient = OrderServerApi.ApiCreator.createInstance();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(AndarApplication.getInstance(), "access_token"));

        OrderServerApi.Order order = new OrderServerApi.Order();
        order.startLatitude = "31.994103";
        order.startLongitude = "46.954053";

        serverApiClient
                .createOrder(headers, order)
                .enqueue(new OrderServerApi.CallbackErrorHandler<ResponseBody>(null) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<ResponseBody> response) {
                        Log.d("AZAZA", "order successfully created");
//                        Toast.makeText(null,"Location sent",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onUnsuccessfulResponse(retrofit2.Response<ResponseBody> response) {
                        super.onUnsuccessfulResponse(response);
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        hideSpinner();
                    }
                });

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
//        if(!isFinished) return;
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
