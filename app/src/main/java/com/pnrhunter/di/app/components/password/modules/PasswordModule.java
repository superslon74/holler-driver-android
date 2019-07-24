package com.pnrhunter.di.app.components.password.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.di.app.modules.UserStorageModule;
import com.pnrhunter.mvp.password.ChangePasswordPresenter;
import com.pnrhunter.mvp.password.ChangePasswordView;
import com.pnrhunter.mvp.password.ForgotPasswordView;

import dagger.Module;
import dagger.Provides;

@Module
public class PasswordModule {
    private static ChangePasswordPresenter presenter = null;

    private ChangePasswordPresenter.View view;

    public PasswordModule(ChangePasswordView view){
        this.view = (ChangePasswordPresenter.View) view;
    }

    public PasswordModule(ForgotPasswordView view){
        this.view = (ChangePasswordPresenter.View) view;
    }

    @Provides
    public ChangePasswordPresenter.View provideLoginView(){
        return view;
    }

    @Provides
    public ChangePasswordPresenter provideLoginPresenter(ChangePasswordPresenter.View view,
                                                RouterModule.Router router,
                                                Context context,
                                                UserStorageModule.UserStorage userStorage,
                                                DeviceInfoModule.DeviceInfo deviceInfo,
                                                RetrofitModule.ServerAPI serverAPI){

        if(presenter==null){
            presenter = new ChangePasswordPresenter(router,context, userStorage,deviceInfo,serverAPI);
        }
        presenter.setView(view);
        return presenter;
    }
}
