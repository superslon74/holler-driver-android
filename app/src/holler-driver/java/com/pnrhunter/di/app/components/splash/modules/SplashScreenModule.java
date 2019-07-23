package com.pnrhunter.di.app.components.splash.modules;

import android.content.Context;

import com.pnrhunter.di.Presenter;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.mvp.main.UserModel;
import com.pnrhunter.mvp.splash.SplashPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class SplashScreenModule {
    private SplashPresenter.View view;

    public SplashScreenModule(SplashPresenter.View view){
        this.view = view;
    }

    @Provides
    public SplashPresenter.View provideView(){
        return view;
    }


    @Provides
    public Presenter providePresenter(Context context,
                                      RouterModule.Router router,
                                      SplashPresenter.View splashScreenView,
                                      RetrofitModule.ServerAPI serverAPI,
                                      UserModel userModel){

        return new SplashPresenter(context, router,splashScreenView,serverAPI, userModel);
    }
}
