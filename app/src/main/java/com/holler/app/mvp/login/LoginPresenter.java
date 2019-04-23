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
import com.holler.app.mvp.register.RegisterPresenter;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import retrofit2.Response;

public class LoginPresenter {

    private RouterModule.Router router;
    private View view;

    private Context context;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;


    private PendingCredentials credentials;

    public LoginPresenter(RouterModule.Router router,
                          Context context,
                          UserStorageModule.UserStorage userStorage,
                          DeviceInfoModule.DeviceInfo deviceInfo,
                          RetrofitModule.ServerAPI serverAPI) {
        Logger.i("Login presenter init");
        this.router = router;
        this.credentials = new PendingCredentials(null,null);

        this.context = context;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
    }

    public void setView(View view){
        this.view = view;
        this.view.setupFields(this.credentials);
    }

    public void goToPasswordView(PendingCredentials newCredentials){
        this.credentials.merge(newCredentials);
        int validationResult = credentials.validate();
        if(validationResult!=PendingCredentials.VALIDATION_EMAIL_ERROR){
            router.goToPasswordScreen();
            view.onFinish();
        }else{
            view.onMessage(credentials.getErrorMessage(validationResult));
        }
    }

    public void goToEmailView(PendingCredentials newCredentials){
        credentials.merge(newCredentials);
        view.onFinish();
        router.goToEmailScreen();
    }

    public void goToMainView(PendingCredentials newCredentials){
        this.credentials.merge(newCredentials);
        int validationResult = credentials.validate();
        if(validationResult==PendingCredentials.VALIDATION_PASSED){
            User user = new User();
            user.deviceType = deviceInfo.deviceType;
            user.deviceId = deviceInfo.deviceId;
            user.deviceToken = deviceInfo.deviceToken;
            user.email = credentials.getEmail();
            user.password = credentials.getPassword();
            signIn(user);
        }else{
            view.onMessage(credentials.getErrorMessage(validationResult));
        }
    }

    public void signIn(final User user) {
        view.showSpinner();

        serverAPI
                .signIn(user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<JsonObject> response) {
                        String accessToken = response.body().get("access_token").getAsString();
                        String currency = response.body().get("currency").getAsString();
                        if (currency == null || currency.isEmpty()) {
                            currency = "$";
                        }
                        SharedHelper.putKey(context, "currency", currency);
                        SharedHelper.putKey(context, "access_token", accessToken);

                        getProfile();
//                        hideSpinner();
                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<JsonObject> response) {
                        super.onUnsuccessfulResponse(response);

                        view.hideSpinner();
                    }

                    @Override
                    public void onDisplayMessage(String message) {
                        view.onMessage(message);
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
//                        hideSpinner();
                    }
                });

    }

    public void getProfile() {
        view.showSpinner();
        String authHeader = "Bearer " + userStorage.getAccessToken();

        serverAPI
                .profile(authHeader)
                .enqueue(new OrderServerApi.CallbackErrorHandler<User>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<User> response) {
                        User newUser = response.body();
                        userStorage.setLoggedIn("true");
                        userStorage.putUser(newUser);

                        credentials.clear();
                        view.onFinish();
                        router.goToMainScreen();
                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<User> response) {
                        switch (response.code()) {
                            case 401:
                                userStorage.setLoggedIn("false");
                                view.onMessage("Something went wrong");
                                break;
                            case 422:
                                view.onMessage("Email already exist");
                                break;
                            default:
                                super.onUnsuccessfulResponse(response);
                        }
                    }

                    @Override
                    public void onDisplayMessage(String message) {
                        view.onMessage(message);
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        view.hideSpinner();
                    }
                });
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

        public static final int VALIDATION_PASSED = -1;
        public static final int VALIDATION_EMAIL_ERROR = 1;
        public static final int VALIDATION_PASSWORD_ERROR = 2;

        public String getErrorMessage(int validationResult){
            switch (validationResult){
                case VALIDATION_EMAIL_ERROR: return "Invalid email";
                case VALIDATION_PASSWORD_ERROR: return "Invalid password";
                default: return  "Invalid credentials";
            }
        }

        public int validate(){
            if(email == null || email.length()==0)
                return VALIDATION_EMAIL_ERROR;
            if(password == null || password.length()==0)
                return VALIDATION_PASSWORD_ERROR;

            return VALIDATION_PASSED;
        }
    }

    public interface View extends SpinnerShower, MessageDisplayer {
        void setupFields(PendingCredentials credentials);
        void onFinish();
    }
}
