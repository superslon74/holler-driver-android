package com.holler.app.mvp.register;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.google.gson.JsonObject;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.holler.app.utils.Validator;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.content.ContextCompat;
import io.reactivex.Flowable;
import retrofit2.Response;

import static com.holler.app.activity.ActivitySocialLogin.APP_REQUEST_CODE;

public class RegisterPresenter {
    private Context context;
    private View view;
    private RouterModule.Router router;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;
    private UserModel userModel;

    private RegistrationPendingCredentials credentials;

    public RegisterPresenter(Context context,
                             View view,
                             RouterModule.Router router,
                             UserStorageModule.UserStorage userStorage,
                             DeviceInfoModule.DeviceInfo deviceInfo,
                             RetrofitModule.ServerAPI serverAPI,
                             UserModel userModel) {

        this.context = context;
        this.view = view;
        this.router = router;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
        this.userModel = userModel;
    }

    public void goToWelcome() {
        view.onFinish();
        router.goToWelcomeScreen();
    }

    public void goToMain() {
        router.goToMainScreen();
    }

    public void signUp(RegistrationPendingCredentials credentials) {
        Throwable validationResult = credentials.validate();
        if (validationResult == null) {
            this.credentials = credentials;
            doRequest();
//            view.onFinish();
//            router.goToMainScreen();
        } else {
            view.onMessage(validationResult.getMessage());
        }
    }

    private void doRequest() {

        userModel
                .checkEmailExists(credentials.email)
                .doOnSubscribe(disposable -> view.showSpinner())
                .doFinally(() -> view.hideSpinner())
                .doOnError(throwable -> view.onMessage(throwable.getMessage()))
                .doOnNext(aBoolean -> verifyByPhone())
                .subscribe();
    }


    private void verifyByPhone() {
        AccessToken accessToken = AccountKit.getCurrentAccessToken();

        final Intent intent = new Intent(context, AccountKitActivity.class);

        SkinManager manager = new SkinManager(
                SkinManager.Skin.CONTEMPORARY,
                context.getColor(R.color.colorAccent),
                -1,
                SkinManager.Tint.WHITE,
                0);

        AccountKitConfiguration.AccountKitConfigurationBuilder accountKitConfigurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);


        int themeId;
        ComponentName componentName = ((CustomActivity)view).getComponentName();
        try {
            themeId = context.getPackageManager().getActivityInfo(componentName,0).getThemeResource();
        }catch (Exception e){
            themeId = -1;
        }
        AccountKitAdvancedManager advancedManager = new AccountKitAdvancedManager(themeId);

        accountKitConfigurationBuilder.setUIManager(manager);

        String code = getCountryZipCode();
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                accountKitConfigurationBuilder
                        .setInitialPhoneNumber(new PhoneNumber("+" + code, "", ""))
                        .build()
        );

        view.startVerificationActivity(intent);
    }

    private String getCountryZipCode() {
        String countryId = "";
        String countryCode = "";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        countryId = manager.getSimCountryIso().toUpperCase();
        String[] codesArray = context.getResources().getStringArray(R.array.CountryCodes);
        for (String codes : codesArray) {
            String[] keyValue = codes.split(",");
            if (keyValue[1].trim().equals(countryId.trim())) {
                countryCode = keyValue[0];
                break;
            }
        }
        return countryCode;
    }

    private void register() {

        userModel
                .register(credentials)
                .doOnSubscribe(disposable -> view.showSpinner())
                .flatMap(timerFinished -> {
                    return userModel
                            .login(credentials.email,credentials.password)
                            .doOnNext(loggedIn -> {
                                router.goToMainScreen();
                            });
                })
                .doFinally(() -> view.hideSpinner())
                .doOnError(throwable -> view.onMessage(throwable.getMessage()))
                .subscribe();


    }


    public void onPhoneVerified(String phone) {
        this.credentials.mobile = phone;
        register();
    }


    public static class RegistrationPendingCredentials {
        public String email;
        public String name;
        public String lastName;
        public String mobile;
        public String gender;
        public String password;
        public String passwordConfirmation;

        public RegistrationPendingCredentials(String email, String name, String lastName, String gender, String password, String passwordConfirmation) {
            this.email = email;
            this.name = name;
            this.lastName = lastName;
            this.gender = gender;
            this.password = password;
            this.passwordConfirmation = passwordConfirmation;
        }

        public Throwable validate() {
            Throwable emailValidation = Validator.validateEmail(email);
            if(emailValidation!=null) return emailValidation;

            Throwable nameValidation = Validator.validateName(name);
            if(nameValidation!=null) return nameValidation;

            Throwable lastNameValidation = Validator.validateName(lastName);
            if(nameValidation!=null) return lastNameValidation;

            Throwable passwordValidation = Validator.validatePassword(password,passwordConfirmation);
            if(passwordValidation!=null) return passwordValidation;

            return null;
        }
    }

    public interface View extends SpinnerShower, MessageDisplayer {
        void onFinish();
        void startVerificationActivity(Intent i);
    }
}
