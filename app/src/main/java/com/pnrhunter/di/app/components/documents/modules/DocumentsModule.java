package com.pnrhunter.di.app.components.documents.modules;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.mvp.documents.DocumentsPresenter;
import com.pnrhunter.mvp.main.UserModel;

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
