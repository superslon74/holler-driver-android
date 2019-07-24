package com.pnrhunter.di.app.components.trips.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.details.TripDetailsPresenter;
import com.pnrhunter.mvp.main.UserModel;

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
