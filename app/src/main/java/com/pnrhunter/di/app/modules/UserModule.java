package com.pnrhunter.di.app.modules;

import com.pnrhunter.mvp.main.UserModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class UserModule {


    @Provides
    @Singleton
    public UserModel provideUserModel(RetrofitModule.ServerAPI serverAPI,
                                      UserStorageModule.UserStorage userStorage,
                                      DeviceInfoModule.DeviceInfo deviceInfo){
        return new UserModel(serverAPI, userStorage, deviceInfo);
    }

}
