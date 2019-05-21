package com.holler.app.mvp.password;

import android.content.Context;

import com.google.gson.JsonObject;
import com.holler.app.R;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.holler.app.utils.Validator;
import com.orhanobut.logger.Logger;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChangePasswordPresenter {
    private RouterModule.Router router;
    private ChangePasswordPresenter.View view;

    private Context context;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;

    private String userId;
    private String code;

    public ChangePasswordPresenter(RouterModule.Router router,
                          Context context,
                          UserStorageModule.UserStorage userStorage,
                          DeviceInfoModule.DeviceInfo deviceInfo,
                          RetrofitModule.ServerAPI serverAPI) {
        Logger.i("ChangePassword presenter init");
        this.router = router;

        this.context = context;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
    }

    public void setView(ChangePasswordPresenter.View v){
        this.view = v;
    }

    public void sendCodeOnMailAddress(String email) {
        Logger.i("Sending code to " + email);

        Throwable validationResult = Validator.validateEmail(email);
        if(validationResult!=null){
            view.showMessage(validationResult.getMessage());
            return;
        }

        Single<RetrofitModule.ServerAPI.ForgotPasswordResponseBody> forgotPasswordSource =
                serverAPI.forgotPassword(new RetrofitModule.ServerAPI.ForgotPasswordRequestBody(email));

        forgotPasswordSource
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .doOnSubscribe(disposable -> view.showSpinner())
                .doFinally(() -> view.hideSpinner())
                .subscribe((response)->{
                    Logger.i("Code sent id:" +response.getId()+" otp:"+response.getOtp());
                    this.userId = response.getId();
                    this.code = response.getOtp();
                    view.onFinish();
                    router.goToChangePasswordScreen();
                    view.showMessage(context.getString(R.string.pas_message_code_sent));
                },(throwable)->{
                    view.showMessage(throwable.getMessage());
                });
    }

    public void sendNewPassword(String code, String newPassword, String passwordConfirmation){
        Logger.i("Requesting password changing with: " + code + ", " + newPassword + ", "+ passwordConfirmation);
        Throwable passwordValidationResult = Validator.validatePassword(newPassword, passwordConfirmation);
        if(passwordValidationResult!=null){
            view.showMessage(passwordValidationResult.getMessage());
            return;
        }
        Throwable otpValidationResault = Validator.validateOtp(code, this.code);
        if(otpValidationResault!=null){
            view.showMessage(otpValidationResault.getMessage());
            return;
        }

        Single<JsonObject> changePasswordSource =
                serverAPI.changePassword(new RetrofitModule.ServerAPI.ChangePasswordRequestBody(userId,newPassword,passwordConfirmation));

        changePasswordSource
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .doOnSubscribe(disposable -> view.showSpinner())
                .doFinally(() -> view.hideSpinner())
                .subscribe((response)->{
                    router.goToWelcomeScreen();
                    view.showMessage(context.getString(R.string.pas_message_password_changed));
                    view.onFinish();
                },(throwable)->{
                    view.showMessage(throwable.getMessage());
                });

    }

    public void goToLoginPasswordView() {
        router.goToPasswordScreen();
        view.onFinish();
    }

    public void goToForgotPasswordView() {
        router.goToForgotPasswordScreen();
        view.onFinish();
    }


    public interface View extends SpinnerShower, MessageDisplayer {
        void onFinish();
    }
}
