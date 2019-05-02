package com.holler.app.mvp.login;

import android.content.Context;
import android.net.Credentials;

import com.google.gson.JsonObject;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.register.RegisterPresenter;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.holler.app.utils.Validator;
import com.orhanobut.logger.Logger;

import retrofit2.Response;

public class LoginPresenter {

    private RouterModule.Router router;
    private View view;

    private Context context;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;
    private UserModel userModel;

    private PendingCredentials credentials;

    public LoginPresenter(RouterModule.Router router,
                          Context context,
                          UserStorageModule.UserStorage userStorage,
                          DeviceInfoModule.DeviceInfo deviceInfo,
                          RetrofitModule.ServerAPI serverAPI,
                          UserModel userModel) {

        this.router = router;
        this.credentials = new PendingCredentials(null,null);

        this.context = context;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
        this.userModel = userModel;
    }

    public void setView(View view){
        this.view = view;
        this.view.setupFields(this.credentials);
    }

    public void goToPasswordView(PendingCredentials newCredentials){
        this.credentials.merge(newCredentials);
        Throwable validationResult = Validator.validateEmail(credentials.email);
        if(validationResult==null){
            router.goToPasswordScreen();
            view.onFinish();
        }else{
            view.onMessage(validationResult.getMessage());
        }
    }

    public void goToEmailView(PendingCredentials newCredentials){
        credentials.merge(newCredentials);
        view.onFinish();
        router.goToEmailScreen();
    }

    public void goToMainView(PendingCredentials newCredentials){
        this.credentials.merge(newCredentials);
        Throwable validationResult = Validator.validatePassword(credentials.password, credentials.password);
        if(validationResult==null){
            signIn(credentials.getEmail(), credentials.getPassword());
        }else{
            view.onMessage(validationResult.toString());
        }
    }

    public void signIn(String email, String password) {

        userModel
                .login(email, password)
                .doOnSubscribe(disposable -> view.showSpinner())
                .doOnNext(isLoggedIn -> {
                    if(isLoggedIn)
                        router.goToMainScreen();
                    else
                        Logger.e("Login Presenter user not logged in without error");
                })
                .doFinally(() -> view.hideSpinner())
                .doOnError(throwable -> view.onMessage(throwable.getMessage()))
                .subscribe();

    }

    public void goToWelcomeView() {
        credentials.clear();
        view.onFinish();
        router.goToWelcomeScreen();
    }

    public void goToRegisterView() {
        router.goToRegisterScreen();
        view.onFinish();
    }

    public void goToForgotPasswordView() {
        router.goToForgotPasswordScreen();
        view.onFinish();
    }

    public static class PendingCredentials{
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public PendingCredentials(String email, String password){
            this.email = email;
            this.password = password;
        }

        public void clear(){
            this.email=null;
            this.password=null;
        }

        public void merge(PendingCredentials newCredentials){
            if(newCredentials.email!=null)
                this.email = newCredentials.email;
            if(newCredentials.password!=null)
                this.password = newCredentials.password;
        }
    }

    public interface View extends SpinnerShower, MessageDisplayer {
        void setupFields(PendingCredentials credentials);
        void onFinish();
    }
}
