package com.pnrhunter.mvp.authorization;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.orhanobut.logger.Logger;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.DeviceInfo;
import com.pnrhunter.mvp.utils.Validator;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.pnrhunter.mvp.utils.activity.KeyboardObserver;
import com.pnrhunter.mvp.utils.activity.MessageDisplayer;
import com.pnrhunter.mvp.utils.activity.SpinnerShower;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.router.TestRouter;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public abstract class RegistrationPresenter {
    protected Validator validator;
    protected Context context;
    protected View view;
    protected AbstractRouter router;
    protected AuthenticationInterface auth;
    protected DeviceInfo deviceInfo;
//    private RetrofitModule.ServerAPI serverAPI;
//    private UserModel userModel;

    protected RegistrationPendingCredentials credentials;
    private ObservableEmitter<Boolean> phoneVerificationEmitter;

    public RegistrationPresenter(Context context,
                                 View view,
                                 AbstractRouter router,
                                 AuthenticationInterface auth,
                                 DeviceInfo deviceInfo,
                                 Validator validator
    ) {

        this.context = context;
        this.view = view;
        this.router = router;
        this.auth = auth;
        this.deviceInfo = deviceInfo;
        this.validator = validator;
        RegistrationPendingCredentials.validator = validator;
    }



    public void goToWelcome() {
        view.onFinish();
        router.goTo(AbstractRouter.ROUTE_WELCOME);
    }

    public void goToMain() {
        router.goTo(AbstractRouter.ROUTE_MAIN);
    }

    public void signUp(RegistrationPendingCredentials credentials) {
        Throwable validationResult = credentials.validate();
        if (validationResult == null) {
            this.credentials = credentials;
            doRequest();
        } else {
            view.showMessage(validationResult.getMessage());
        }
    }

    protected abstract void doRequest();


    protected Observable<Boolean> verifyByPhone() {
        return Observable.<Boolean>create(emitter -> {
            phoneVerificationEmitter = emitter;
            try {
                AccessToken accessToken = AccountKit.getCurrentAccessToken();

                final Intent intent = new Intent(context, AccountKitActivity.class);

                SkinManager manager = new SkinManager(
                        SkinManager.Skin.CONTEMPORARY,
                        context.getColor(R.color.themeAccent),
                        -1,
                        SkinManager.Tint.WHITE,
                        0);

                AccountKitConfiguration.AccountKitConfigurationBuilder accountKitConfigurationBuilder =
                        new AccountKitConfiguration.AccountKitConfigurationBuilder(
                                LoginType.PHONE,
                                AccountKitActivity.ResponseType.TOKEN);


                int themeId;
                ComponentName componentName = ((ExtendedActivity) view).getComponentName();
                try {
                    themeId = context.getPackageManager().getActivityInfo(componentName, 0).getThemeResource();
                } catch (Exception e) {
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
                //TODO: used for test remove it in prod
            }catch (Exception e){
                Logger.e("Account kit error: " + e.getMessage());
                onPhoneVerified("+380000000000");
            }
        });


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

    public void onPhoneVerified(String phone) {
        this.credentials.mobile = phone;
        phoneVerificationEmitter.onNext(true);
    }

    public static class RegistrationPendingCredentials {
        public String email;
        public String name;
        public String lastName;
        public String mobile;
        public String gender;
        public String password;
        public String passwordConfirmation;
        private static Validator validator;

        public RegistrationPendingCredentials(
                String email,
                String name,
                String lastName,
                String gender,
                String password,
                String passwordConfirmation) {
            this.email = email;
            this.name = name;
            this.lastName = lastName;
            this.gender = gender;
            this.password = password;
            this.passwordConfirmation = passwordConfirmation;
        }

        public Throwable validate() {
            Throwable emailValidation = validator.validateEmail(email);
            if(emailValidation!=null) return emailValidation;

            Throwable nameValidation = validator.validateName(name);
            if(nameValidation!=null) return nameValidation;

            Throwable lastNameValidation = validator.validateName(lastName);
            if(nameValidation!=null) return lastNameValidation;

            Throwable passwordValidation = validator.validatePassword(password,passwordConfirmation);
            if(passwordValidation!=null) return passwordValidation;

            return null;
        }
    }

    public interface View extends SpinnerShower, MessageDisplayer, KeyboardObserver {
        void onFinish();
        void startVerificationActivity(Intent i);
    }
}
