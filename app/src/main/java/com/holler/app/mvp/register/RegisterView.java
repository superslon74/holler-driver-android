package com.holler.app.mvp.register;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.holler.app.AndarApplication;
import com.holler.app.Helper.ConnectionHelper;
import com.holler.app.Helper.CustomDialog;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Helper.URLHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;
import com.holler.app.activity.ActivityEmail;
import com.holler.app.activity.ActivityPassword;
import com.holler.app.activity.MainActivity;
import com.holler.app.di.User;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerRegisterComponent;
import com.holler.app.di.app.components.register.modules.RegisterModule;
import com.holler.app.di.app.components.splash.modules.SplashScreenModule;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.CustomActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import androidx.annotation.IdRes;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RegisterView
        extends CustomActivity
        implements
        RegisterPresenter.View{



    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source != null && "~#^|$%&*!()_-*.,@/".contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @BindView(R.id.ra_email)
    public EditText emailInput;
    @BindView(R.id.ra_name)
    public EditText nameInput;
    @BindView(R.id.ra_mobile)
    public EditText mobileInput;
    @BindView(R.id.ra_password)
    public EditText passwordInput;
    @BindView(R.id.ra_password_confirmation)
    public EditText passwordConfirmationInput;
    @BindView(R.id.ra_gender)
    public RadioGroup genderGroup;

    @BindView(R.id.ra_male_radio)
    public RadioButton radioMale;
    @BindView(R.id.ra_male_icon)
    public ImageView maleIcon;
    @BindView(R.id.ra_female_radio)
    public RadioButton radioFemale;
    @BindView(R.id.ra_female_icon)
    public ImageView femaleIcon;


    private CustomDialog customDialog;
    private String gender = GENDER_MALE_VALUE;
    private static final int PHONE_VERIFICATION_REQUEST_CODE = 99;

    @Inject public RegisterPresenter presenter;

    private void setupComponent(){
        ButterKnife.bind(this);
        AppComponent appComponent = (AppComponent) AndarApplication.getInstance().component();
        DaggerRegisterComponent.builder()
                .appComponent(appComponent)
                .registerModule(new RegisterModule(this))
                .build()
                .inject(this);
    }

    private static String GENDER_MALE_VALUE = "male";
    private static String GENDER_FEMALE_VALUE = "female";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupComponent();

        final int grey = ContextCompat.getColor(this, R.color.theme);
        final int accent = ContextCompat.getColor(this, R.color.colorAccent);

        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.ra_male_radio:
                        gender = GENDER_MALE_VALUE;
                        femaleIcon.setColorFilter(grey);
                        maleIcon.setColorFilter(accent);
                        break;
                    case R.id.ra_female_radio:
                        gender = GENDER_FEMALE_VALUE;
                        maleIcon.setColorFilter(grey);
                        femaleIcon.setColorFilter(accent);
                        break;
                }
            }
        });

        femaleIcon.setColorFilter(grey);
        maleIcon.setColorFilter(accent);

    }

    @OnClick(R.id.ra_sign_up)
    public void signUp(){
        String name = this.nameInput.getText().toString();
        String email = this.emailInput.getText().toString();
        String mobile = this.mobileInput.getText().toString();
        String gender = this.gender;
        String password = this.passwordInput.getText().toString();
        String passwordConfirmation = this.passwordConfirmationInput.getText().toString();

        RegisterPresenter.RegistrationPendingCredentials credentials =
                new RegisterPresenter.RegistrationPendingCredentials(email, name, mobile, gender, password, passwordConfirmation);

        presenter.signUp(credentials);
    }

    @OnClick(R.id.ra_back_button)
    public void goToWelcomeScreen(){
        presenter.goToWelcome();
    }


    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHONE_VERIFICATION_REQUEST_CODE) {
            if(data == null){
                onMessage("Verification failed");
                return;
            }
            AccountKitLoginResult verificationResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (verificationResult != null) {
                SharedHelper.putKey(this, "account_kit", getString(R.string.True));
            } else {
                SharedHelper.putKey(this, "account_kit", getString(R.string.False));
            }
            if(verificationResult!=null && verificationResult.getAccessToken() != null){
                String accountKitAccessToken = verificationResult.getAccessToken().toString();
                SharedHelper.putKey(this, "account_kit", accountKitAccessToken);
            }else{
                SharedHelper.putKey(this, "account_kit", "");
            }

            presenter.onPhoneVerified();
        }
    }


    public void startVerificationActivity(Intent i){
        super.startActivityForResult(i,PHONE_VERIFICATION_REQUEST_CODE);
    }

    @Override
    public void onMessage(String message) {
        super.onMessage(message);
    }

    @Override
    public void onLoadingStarted() {
//        if(customDialog==null)
//            customDialog = new CustomDialog(this);
//        customDialog.setCancelable(true);
//        customDialog.show();
        onMessage("Loading...");

    }

    @Override
    public void onLoadingFinished() {
//        customDialog.dismiss();
    }

    @Override
    public void onFinish() {
        finish();
    }
}



/**
 * Test example
 */
//    private final CountDownLatch latch = new CountDownLatch(1);
//
//    @Test
//    public void testRegisterUser() throws InterruptedException{
//
//        UserServerApi serverApiClient = UserServerApi.ApiCreator.createInstance();
//
//        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("X-Requested-With", "XMLHttpRequest");
////        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
//        User user = new User();
//        user.email = "alex@gmail.com";
//        user.deviceId = "23b50be39712afaa";
//        user.deviceToken = "c6l63MYuExg:APA91bGYOJj69phx9I_3VMwiXt_bpE_1GyQi-LqtIgvKgWX75gDWBrU5qjr0k3g35JcnFizTr5zEq6YAnsCOrNmZWIq4-ukij8udYH5H-h5zGvlyND-8UOLTBIl9CZcIZCVlNkf8ikMb";
//        user.deviceType = "android";
//        user.firstName = "A";
//        user.lastName = "B";
//        user.gender = "male";
//        user.loggedBy = "manual";
//        user.mobile = "+380688574090";
//        user.password = "1aaaaaaa";
//        user.passwordConfirmation = "1aaaaaaa";
//
//
//        serverApiClient
//                .register(headers,user)
//                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(RegisterActivity.this) {
//                    @Override
//                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
//                        Log.d("AZAZA",""+response.toString());
//                        latch.countDown();
//                    }
//
//                    @Override
//                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> response) {
//                        super.onUnsuccessfulResponse(response);
//                        latch.countDown();
//                    }
//
//                    @Override
//                    public void onFinishHandling() {
//                        super.onFinishHandling();
//                        latch.countDown();
//                    }
//                });
//
//        latch.await();
//    }
//
//    @Test
//    public void testVerifyEmail() throws InterruptedException{
//
//
//        UserServerApi serverApiClient = UserServerApi.ApiCreator.createInstance();
//
//        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("X-Requested-With", "XMLHttpRequest");
////        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
//        User user = new User();
//        user.email = "alex@gmail.com";
//
//        serverApiClient
//                .checkEmailExists(headers,user)
//                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(RegisterActivity.this) {
//                    @Override
//                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
//                        Log.d("AZAZA",""+response.toString());
//                        latch.countDown();
//                    }
//
//                    @Override
//                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> response) {
//                        super.onUnsuccessfulResponse(response);
//                        latch.countDown();
//                    }
//
//                    @Override
//                    public void onFinishHandling() {
//                        super.onFinishHandling();
//                        latch.countDown();
//                    }
//                });
//
//        latch.await();
//    }
