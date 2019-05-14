package com.holler.app.mvp.profile;

import android.content.Context;

import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;

public class ProfilePresenter {

    private Context context;
    private View view;
    private RouterModule.Router router;
    private UserModel userModel;

    public ProfilePresenter(Context context,
                            View view,
                            RouterModule.Router router,
                            UserModel userModel) {

        this.context = context;
        this.view = view;
        this.router = router;
        this.userModel = userModel;
    }

    public interface View extends SpinnerShower, MessageDisplayer, Finishable {

    }
}
