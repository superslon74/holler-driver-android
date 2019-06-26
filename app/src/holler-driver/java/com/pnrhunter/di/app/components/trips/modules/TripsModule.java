package com.pnrhunter.di.app.components.trips.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.details.TripsPresenter;
import com.pnrhunter.mvp.main.UserModel;

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
