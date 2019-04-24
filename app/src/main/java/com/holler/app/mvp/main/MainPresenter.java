package com.holler.app.mvp.main;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.splash.SplashPresenter;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;

public class MainPresenter {

    private Context context;
    private RouterModule.Router router;
    private MainPresenter.View view;
    private RetrofitModule.ServerAPI serverAPI;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private UserStorageModule.UserStorage userStorage;

    public MainPresenter(Context context,
                         RouterModule.Router router,
                         MainPresenter.View view,
                         RetrofitModule.ServerAPI serverAPI,
                         DeviceInfoModule.DeviceInfo deviceInfo,
                         UserStorageModule.UserStorage userStorage) {

        this.context = context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.deviceInfo = deviceInfo;
        this.userStorage = userStorage;
    }


    public interface View extends SpinnerShower, MessageDisplayer{

    }

}
