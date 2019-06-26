package com.pnrhunter.di.app.components.register.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.di.app.modules.UserStorageModule;
import com.pnrhunter.mvp.main.UserModel;
import com.pnrhunter.mvp.register.RegisterPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class RegisterModule {
    private RegisterPresenter.View view;

    public RegisterModule(RegisterPresenter.View view){
        this.view = view;
    }

    @Provides
    public RegisterPresenter.View provideView(){
        return view;
    }


    @Provides
    public RegisterPresenter providePresenter(Context context,
                                              RegisterPresenter.View view,
                                              RouterModule.Router router,
                                              UserStorageModule.UserStorage userStorage,
                                              DeviceInfoModule.DeviceInfo deviceInfo,
                                              RetrofitModule.ServerAPI serverAPI,
                                              UserModel userModel){

        return new RegisterPresenter(context,view,router,userStorage,deviceInfo,serverAPI, userModel);
    }
}
