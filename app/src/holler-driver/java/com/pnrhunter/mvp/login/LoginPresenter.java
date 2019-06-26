package com.pnrhunter.mvp.login;

import android.content.Context;

import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.di.app.modules.UserStorageModule;
import com.pnrhunter.mvp.main.UserModel;
import com.pnrhunter.utils.Finishable;
import com.pnrhunter.utils.MessageDisplayer;
import com.pnrhunter.utils.SpinnerShower;
import com.pnrhunter.utils.Validator;
import com.orhanobut.logger.Logger;

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
            view.finish();
        }else{
            view.showMessage(validationResult.getMessage());
        }
    }

    public void goToEmailView(PendingCredentials newCredentials){
        credentials.merge(newCredentials);
        view.finish();
        router.goToEmailScreen();
    }

    public void goToMainView(PendingCredentials newCredentials){
        this.credentials.merge(newCredentials);
        Throwable validationResult = Validator.validatePassword(credentials.password, credentials.password);
        if(validationResult==null){
            signIn(credentials.getEmail(), credentials.getPassword());
        }else{
            view.showMessage(validationResult.getMessage());
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
                .doOnError(throwable -> view.showMessage(((Exception)throwable).getMessage()))
                .subscribe();

    }

    public void goToWelcomeView() {
        credentials.clear();
        view.finish();
        router.goToWelcomeScreen();
    }

    public void goToRegisterView() {
        router.goToRegisterScreen();
        view.finish();
    }

    public void goToForgotPasswordView() {
        router.goToForgotPasswordScreen();
        view.finish();
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

    public interface View extends SpinnerShower, MessageDisplayer, Finishable {
        void setupFields(PendingCredentials credentials);
    }
}
