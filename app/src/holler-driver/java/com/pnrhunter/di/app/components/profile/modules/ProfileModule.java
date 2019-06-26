package com.pnrhunter.di.app.components.profile.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.main.UserModel;
import com.pnrhunter.mvp.profile.ProfilePresenter;

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
