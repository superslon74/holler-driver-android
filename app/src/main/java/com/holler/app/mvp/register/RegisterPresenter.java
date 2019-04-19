package com.holler.app.mvp.register;

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
import com.google.gson.JsonObject;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.server.OrderServerApi;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.core.content.ContextCompat;
import retrofit2.Response;

import static com.holler.app.activity.ActivitySocialLogin.APP_REQUEST_CODE;

public class RegisterPresenter {
    private Context context;
    private View view;
    private RouterModule.Router router;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;

    private RegistrationPendingCredentials credentials;

    public RegisterPresenter(Context context,
                             View view,
                             RouterModule.Router router,
                             UserStorageModule.UserStorage userStorage,
                             DeviceInfoModule.DeviceInfo deviceInfo,
                             RetrofitModule.ServerAPI serverAPI) {

        this.context = context;
        this.view = view;
        this.router = router;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
    }

    public void goToWelcome() {
        //TODO: user logged in false in shared prefs
        view.onFinish();
        router.goToWelcomeScreen();
    }

    public void goToMain() {
        router.goToMainScreen();
    }

    public void signUp(RegistrationPendingCredentials credentials) {
        int validationResult = credentials.validate();
        if (validationResult == RegistrationPendingCredentials.VALIDATION_PASSED) {
            this.credentials = credentials;
            doRequest();
//            view.onFinish();
//            router.goToMainScreen();
        } else {
            view.onMessage(credentials.getErrorMessage(validationResult));
        }
    }

    private void doRequest() {
        view.onLoadingStarted();
        checkMailAlreadyExit();
    }

    public void checkMailAlreadyExit() {

        User user = new User();
        user.email = credentials.email;

        serverAPI
                .checkEmailExists(user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<JsonObject> response) {
                        view.onLoadingFinished();
                        verifyByPhone();
                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<JsonObject> response) {
                        if (response.code() == 422) {
                            view.onMessage("Email already taken");
                            return;
                        }
                        super.onUnsuccessfulResponse(response);
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        view.onLoadingFinished();
                    }
                });

    }

    private void verifyByPhone() {
        AccessToken accessToken = AccountKit.getCurrentAccessToken();

        final Intent intent = new Intent(context, AccountKitActivity.class);
        SkinManager manager = new SkinManager(
                SkinManager.Skin.TRANSLUCENT,
                ContextCompat.getColor(context, R.color.cancel_ride_color),
                R.drawable.banner_fb,
                SkinManager.Tint.WHITE,
                85);
        AccountKitConfiguration.AccountKitConfigurationBuilder accountKitConfigurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
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
            String[] keyValye = codes.split(",");
            if (keyValye[1].trim().equals(countryId.trim())) {
                countryCode = keyValye[0];
                break;
            }
        }
        return countryCode;
    }

    private void register() {
        view.onLoadingStarted();

        final User user = new User();

        user.deviceType = deviceInfo.deviceType;
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;
        user.loggedBy = "manual";
        user.firstName = credentials.name;
        user.lastName = credentials.name;
        user.gender = credentials.gender;
        user.mobile = credentials.mobile;

        user.email = credentials.email;
        user.password = credentials.password;
        user.passwordConfirmation = credentials.passwordConfirmation;

        serverAPI
                .register(user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<JsonObject> response) {
                        SharedHelper.putKey(context, "email", credentials.email);
                        SharedHelper.putKey(context, "password", credentials.password);
                        signIn(user);
                    }

                    @Override
                    public void onUnsuccessfulResponse(Response<JsonObject> response) {
                        view.onLoadingFinished();
                        switch (response.code()) {
                            case 403:
                                view.onMessage("Phone number already in use");
                                SharedHelper.putKey(activity, "loggedIn", "false");
                                view.onFinish();
                                router.goToWelcomeScreen();
                                break;
                            case 401:
                                view.onMessage("Something went wrong");
                                break;
                            case 422:
                                view.onMessage("Email has already been taken");
                                break;
                            default:
                                super.onUnsuccessfulResponse(response);
                        }

                    }


                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
//                        hideSpinner();
                    }
                });
    }

    public void signIn(final User user) {
        view.onLoadingStarted();

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
                        view.onLoadingFinished();
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
//                        hideSpinner();
                    }
                });

    }

    public void getProfile() {
        view.onLoadingStarted();
        String authHeader = "Bearer " + userStorage.getAccessToken();

        serverAPI
                .profile(authHeader)
                .enqueue(new OrderServerApi.CallbackErrorHandler<User>(null) {
                    @Override
                    public void onSuccessfulResponse(Response<User> response) {
                        User newUser = response.body();
                        userStorage.setLoggedIn("true");
                        userStorage.putUser(newUser);

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
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        view.onLoadingFinished();
                    }
                });
    }

    public void onPhoneVerified() {
        register();
    }


    public static class RegistrationPendingCredentials {
        private String email;
        private String name;
        private String mobile;
        private String gender;
        private String password;
        private String passwordConfirmation;

        public RegistrationPendingCredentials(String email, String name, String mobile, String gender, String password, String passwordConfirmation) {
            this.email = email;
            this.name = name;
            this.mobile = mobile;
            this.gender = gender;
            this.password = password;
            this.passwordConfirmation = passwordConfirmation;
        }

        public static final int VALIDATION_PASSED = -1;
        public static final int VALIDATION_ERROR_EMAIL_EMPTY = 1;
        public static final int VALIDATION_ERROR_EMAIL = 2;
        public static final int VALIDATION_ERROR_PASSWORD_EMPTY = 3;
        public static final int VALIDATION_ERROR_PASSWORD = 4;
        public static final int VALIDATION_ERROR_PASSWORD_MISMATCHED = 5;
        public static final int VALIDATION_ERROR_NAME_EMPTY = 6;

        public String getErrorMessage(int validationResult) {
            switch (validationResult) {
                case VALIDATION_ERROR_EMAIL_EMPTY:
                    return "Please enter your email";
                case VALIDATION_ERROR_EMAIL:
                    return "Not a valid email id";
                case VALIDATION_ERROR_PASSWORD_EMPTY:
                    return "Password required";
                case VALIDATION_ERROR_PASSWORD:
                    return "Password must contains minimum 8 characters maximum 16 characters and at least one number";
                case VALIDATION_ERROR_PASSWORD_MISMATCHED:
                    return "Please repeat password for confirmation";
                case VALIDATION_ERROR_NAME_EMPTY:
                    return "Enter your name";
                default:
                    return "Validation failed";
            }
        }

        public int validate() {
            if (email == null || email.length() == 0)
                return VALIDATION_ERROR_EMAIL_EMPTY;
            if (name == null || name.length() == 0)
                return VALIDATION_ERROR_NAME_EMPTY;
            if (!isValidEmail())
                return VALIDATION_ERROR_EMAIL;
            if (password == null || password.length() == 0)
                return VALIDATION_ERROR_PASSWORD_EMPTY;
            if (!isValidPassword())
                return VALIDATION_ERROR_PASSWORD;
            if (!isPasswordsMatches())
                return VALIDATION_ERROR_PASSWORD_MISMATCHED;

            return VALIDATION_PASSED;
        }

        private boolean isPasswordsMatches() {
            return password.endsWith(passwordConfirmation);
        }

        private boolean isValidPassword() {
            String PASSWORD_PATTERN = "(?=.*[a-z])(?=.*[\\d]).{8,16}";
            Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
            Matcher matcher = pattern.matcher(password);
            return matcher.matches();
        }

        private boolean isValidEmail() {
            String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
        }
    }

    public interface View {
        void onLoadingStarted();

        void onLoadingFinished();

        void onFinish();

        void startVerificationActivity(Intent i);

        void onMessage(String message);
    }
}
