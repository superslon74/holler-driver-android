package com.holler.app.di;

import android.util.Log;

import com.holler.app.Activity.RegisterActivity;
import com.holler.app.Helper.SharedHelper;

import java.util.HashMap;

public class SplashScreenPresenter implements Presenter {
    private View view;
    private RetrofitModule.ServerAPI serverAPI;
    private DeviceInfoModule.DeviceInfo deviceInfo;

    public SplashScreenPresenter(
            View view,
            RetrofitModule.ServerAPI serverAPI,
            DeviceInfoModule.DeviceInfo deviceInfo){
        this.view = view;
        this.serverAPI = serverAPI;
        this.deviceInfo = deviceInfo;
    }

    @Override
    public void onResume() {
        //    TODO: should make server call then decide where to go
        Log.d("AZAZA", "presenter job starting");

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");

        User user = new User();
        user.deviceType = "android";
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;

//        user.email = email.getText().toString();
//        user.password = password.getText().toString();


//        serverAPI.signIn(headers, user);
    }





    public interface View {
        void showProgress();
        void hideProgress();
        void showMessage(String message);
    }
}
