package com.holler.app.di.app.components.documents.modules;

import android.content.Context;

import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.documents.DocumentsPresenter;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.profile.ProfilePresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class DocumentsModule {
    private DocumentsPresenter.View view;

    public DocumentsModule(DocumentsPresenter.View view){
        this.view = view;
    }

    @Provides
    public DocumentsPresenter.View provideView(){
        return view;
    }


    @Provides
    public DocumentsPresenter providePresenter(Context context,
                                               DocumentsPresenter.View view,
                                               RouterModule.Router router,
                                               DeviceInfoModule.DeviceInfo deviceInfo,
                                               RetrofitModule.ServerAPI serverAPI,
                                               UserModel userModel){

        return new DocumentsPresenter(context,view,router,deviceInfo, serverAPI,userModel);
    }
}
