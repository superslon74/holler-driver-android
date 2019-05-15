package com.holler.app.mvp.profile;

import android.content.Context;

import com.holler.app.di.User;
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

    public void init(){
        view.setFields(userModel.getProfileData());
    }

    public void goToForgotPassword() {
        router.goToForgotPasswordScreen();
    }

    public void sendChanges(String avatar, String firstName, String lastName, String gender) {
        User profileData = userModel.getProfileData();

        profileData.avatar = avatar;
        profileData.firstName = firstName;
        profileData.lastName = lastName;
        profileData.gender=gender;

        userModel
                .updateProfile(profileData)
                .doOnSubscribe(disposable -> view.showSpinner())
                .doOnNext(profileChanged -> {
                    view.setFields(userModel.getProfileData());
                })
                .doFinally(() -> {view.hideSpinner();})
                .subscribe();
    }


    public interface View extends SpinnerShower, MessageDisplayer, Finishable {
        void setFields(User user);
    }
}
