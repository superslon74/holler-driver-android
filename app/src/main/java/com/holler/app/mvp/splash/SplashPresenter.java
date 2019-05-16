package com.holler.app.mvp.splash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;

import com.google.gson.JsonObject;
import com.holler.app.activity.MainActivity;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.ReactiveServiceBindingFactory;
import com.holler.app.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import retrofit2.Response;

public class SplashPresenter implements Presenter {
    private Context context;
    private RouterModule.Router router;
    private View view;
    private RetrofitModule.ServerAPI serverAPI;
    private UserModel userModel;

    public SplashPresenter(
            Context context,
            RouterModule.Router router,
            View view,
            RetrofitModule.ServerAPI serverAPI,
            UserModel userModel) {
        this.context = context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.userModel = userModel;
    }

    @Override
    public void onResume() {
        if (userModel.isLoggedIn()) {
            updateUserData();
        } else {
            router.goToWelcomeScreen();
        }

    }

    private void updateUserData() {

        userModel
                .login()
                .flatMap(isLogged -> {
                    return GPSTracker.serviceConnection(context);
                })
                .flatMap(service -> {
                    service.startTracking();
                    //TODO: test
//                    router.goToEditProfileScreen();
                    router.goToMainScreen();
                    return Observable.empty();
                })
                .doOnError(throwable -> {
                    throwable.printStackTrace();
                    router.goToWelcomeScreen();
                })
                .subscribe();
    }

    public interface View extends SpinnerShower, MessageDisplayer {

    }

    /**
     *

     зависает в начале изза android.permission.UPDATE_APP_OPS_STATS.

     когда приложение подключает gps tracker service возникает ошибка, отсутствуют права UPDATE_APP_OPS_STATS возможна таже ошибка не позволяет отработать floatinview service'у

     на данный момент сервисы подключаются по мере необходимости, и подвязываются на обьекты (активити) которые нуждаются в них, после того как активити разрушаются сервисы отвязываются и тоже разрушаются, если больше не к кому не привязаны. Есть еще один способ подключения сервисов, при котором сервис остается активным независимо от того нуждается ли ктолибо в его данных.
     */
}
