package com.holler.app.di.app.components.trips.modules;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.details.TripDetailsPresenter;
import com.holler.app.mvp.details.TripsPresenter;
import com.holler.app.mvp.main.UserModel;

import dagger.Module;
import dagger.Provides;

@Module
public class TripsModule {
    private TripsPresenter.View view;

    public TripsModule(TripsPresenter.View view){
        this.view = view;
    }

    @Provides
    public TripsPresenter.View provideView(){
        return view;
    }


    @Provides
    public TripsPresenter providePresenter(Context context,
                                           TripsPresenter.View view,
                                                 RouterModule.Router router,
                                                 DeviceInfoModule.DeviceInfo deviceInfo,
                                                 UserModel userModel,
                                                 RetrofitModule.ServerAPI serverAPI){

        return new TripsPresenter(context,view,router,deviceInfo, userModel, serverAPI);
    }
}
