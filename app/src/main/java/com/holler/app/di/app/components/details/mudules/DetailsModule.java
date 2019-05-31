package com.holler.app.di.app.components.details.mudules;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.details.DetailsPresenter;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.profile.ProfilePresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class DetailsModule {
    private DetailsPresenter.View view;

    public DetailsModule(DetailsPresenter.View view){
        this.view = view;
    }

    @Provides
    public DetailsPresenter.View provideView(){
        return view;
    }


    @Provides
    public DetailsPresenter providePresenter(Context context,
                                             DetailsPresenter.View view,
                                             RouterModule.Router router,
                                             DeviceInfoModule.DeviceInfo deviceInfo,
                                             RetrofitModule.ServerAPI serverAPI){

        return new DetailsPresenter(context,view,router,deviceInfo, serverAPI);
    }
}
