package com.holler.app.di.app.components.profile.modules;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.profile.ProfilePresenter;
import com.holler.app.mvp.register.RegisterPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class ProfileModule {
    private ProfilePresenter.View view;

    public ProfileModule(ProfilePresenter.View view){
        this.view = view;
    }

    @Provides
    public ProfilePresenter.View provideView(){
        return view;
    }


    @Provides
    public ProfilePresenter providePresenter(Context context,
                                             ProfilePresenter.View view,
                                              RouterModule.Router router,
                                              UserModel userModel){

        return new ProfilePresenter(context,view,router,userModel);
    }
}
