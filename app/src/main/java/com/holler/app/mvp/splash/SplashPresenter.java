package com.holler.app.mvp.splash;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.holler.app.di.Presenter;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.holler.app.utils.UpdateChecker;

import io.reactivex.Observable;

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
    public void checkVersion() {
        new UpdateChecker(context)
                .checkForNewVersion()
                .doOnNext(updateNeeded -> {
                    if(updateNeeded){
                        view.showAppLinkDialog("la");
                    }else{
                        checkLogin();
                    }
                })
                .subscribe();

    }

    private void checkLogin() {
        if (!userModel.isLoggedIn()) {
            router.goToWelcomeScreen();
            return;
        }
        userModel
                .login()
                .flatMap(isLogged -> {
                    return Observable.<GPSTracker.GPSTrackerBinder>create(emitter -> {
                        Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);

                        this.gpsTrackerServiceConnection = new ServiceConnection() {
                            @Override
                            public void onServiceConnected(ComponentName name, IBinder service) {
                                emitter.onNext((GPSTracker.GPSTrackerBinder) service);
                            }

                            @Override
                            public void onServiceDisconnected(ComponentName name) {
                                emitter.onError(new Throwable("Can't enable location tracking"));
                            }

                            @Override
                            public void onBindingDied(ComponentName name) {
                                emitter.onError(new Throwable("Can't enable location tracking"));
                            }

                            @Override
                            public void onNullBinding(ComponentName name) {
                                emitter.onError(new Throwable("Can't enable location tracking"));
                            }
                        };

                        context.bindService(gpsTrackerBinding, this.gpsTrackerServiceConnection, Context.BIND_IMPORTANT);
                    });
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

    public void goAhead(){
        checkLogin();
    }

    public interface View extends SpinnerShower, MessageDisplayer {
        void showAppLinkDialog(String appUrl);
    }

}
