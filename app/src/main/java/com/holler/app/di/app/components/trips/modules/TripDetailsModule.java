package com.holler.app.di.app.components.trips.modules;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.details.TripDetailsPresenter;
import com.holler.app.mvp.main.UserModel;

import dagger.Module;
import dagger.Provides;

@Module
public class TripDetailsModule {
    private TripDetailsPresenter.View view;

    public TripDetailsModule(TripDetailsPresenter.View view){
        this.view = view;
    }

    @Provides
    public TripDetailsPresenter.View provideView(){
        return view;
    }


    @Provides
    public TripDetailsPresenter providePresenter(Context context,
                                                 TripDetailsPresenter.View view,
                                                 RouterModule.Router router,
                                                 DeviceInfoModule.DeviceInfo deviceInfo,
                                                 UserModel userModel,
                                                 RetrofitModule.ServerAPI serverAPI){

        return new TripDetailsPresenter(context,view,router,deviceInfo, userModel, serverAPI);
    }
}
