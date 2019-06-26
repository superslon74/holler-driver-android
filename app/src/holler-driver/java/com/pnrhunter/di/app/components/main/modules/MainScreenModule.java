package com.pnrhunter.di.app.components.main.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.main.MainPresenter;
import com.pnrhunter.mvp.main.OrderModel;
import com.pnrhunter.mvp.main.UserModel;

import dagger.Module;
import dagger.Provides;

@Module
public class MainScreenModule {
    private MainPresenter.View view;

    public MainScreenModule(MainPresenter.View view) {
        this.view = view;
    }

    @Provides
    public MainPresenter.View provideView() {
        return view;
    }


    @Provides
    public MainPresenter providePresenter(Context context,
                                          RouterModule.Router router,
                                          MainPresenter.View view,
                                          RetrofitModule.ServerAPI serverAPI,
                                          UserModel userModel,
                                          OrderModel orderModel) {

        return new MainPresenter(context, router, view, serverAPI, userModel, orderModel);
    }
}
