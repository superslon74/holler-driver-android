package com.holler.app.mvp.splash;

import android.content.Context;
import android.content.ServiceConnection;

import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashPresenter implements Presenter {
    private Context context;
    private RouterModule.Router router;
    private View view;
    private RetrofitModule.ServerAPI serverAPI;
    private UserModel userModel;
    private ServiceConnection gpsTrackerServiceConnection;

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
                    GPSTracker.ObservableConnection connection = GPSTracker.createConnection(context);
                    gpsTrackerServiceConnection = connection;
                    return connection;
                })
                .flatMap(service -> {
                    service.startTracking();
                    router.goToMainScreen();
                    return Observable.empty();
                })
                .doOnError(throwable -> {
                    view.showCompletableMessage(throwable.getMessage())
                            .doOnComplete(() -> {
                                router.goToWelcomeScreen();
                            })
                            .subscribe();
                })
                .subscribe();
    }

    public void onDestroy() {
        if (gpsTrackerServiceConnection != null)
            context.unbindService(gpsTrackerServiceConnection);
    }

    public interface View extends SpinnerShower, MessageDisplayer {

    }

}
