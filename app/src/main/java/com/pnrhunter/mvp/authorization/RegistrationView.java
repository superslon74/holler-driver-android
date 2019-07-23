package com.pnrhunter.mvp.authorization;


import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.PhoneNumber;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.orhanobut.logger.Logger;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RegistrationView
        extends ExtendedActivity
        implements
        RegistrationPresenter.View{


    @BindView(R.id.ra_email)
    public EditText emailInput;
    @BindView(R.id.ra_name)
    public EditText nameInput;
    @BindView(R.id.ra_last_name)
    public EditText lastNameInput;
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


    private String gender = GENDER_MALE_VALUE;
    private static final int PHONE_VERIFICATION_REQUEST_CODE = 99;

    @Inject public RegistrationPresenter presenter;


    private static String GENDER_MALE_VALUE = "male";
    private static String GENDER_FEMALE_VALUE = "female";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);

        final int grey = ContextCompat.getColor(this, R.color.black);
        final int accent = ContextCompat.getColor(this, R.color.themeAccent);

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
        String lastName = this.lastNameInput.getText().toString();
        String email = this.emailInput.getText().toString();
        String gender = this.gender;
        String password = this.passwordInput.getText().toString();
        String passwordConfirmation = this.passwordConfirmationInput.getText().toString();

        RegistrationPresenter.RegistrationPendingCredentials credentials =
                new RegistrationPresenter.RegistrationPendingCredentials(email, name, lastName, gender, password, passwordConfirmation);

        presenter.signUp(credentials);
    }

//    @OnClick(R.id.ra_back_button)
//    public void goToWelcomeScreen(){
//        presenter.goToWelcome();
//    }


    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHONE_VERIFICATION_REQUEST_CODE) {
            if(data == null){
                this.showMessage(getBaseContext().getString(R.string.error_account_kit_verification));
                return;
            }


            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(final Account account) {
                    String accountKitId = account.getId();
                    PhoneNumber phoneNumber = account.getPhoneNumber();
                    String phoneNumberString = phoneNumber.toString();

                    presenter.onPhoneVerified(phoneNumberString);
                }

                @Override
                public void onError(final AccountKitError error) {
                    Logger.e(error.toString());
                    RegistrationView.this.showMessage(getBaseContext().getString(R.string.error_account_kit_verification));
                }
            });

        }
    }


    public void startVerificationActivity(Intent i){
        super.startActivityForResult(i,PHONE_VERIFICATION_REQUEST_CODE);
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
//                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> data) {
//                        Log.d("AZAZA",""+data.toString());
//                        latch.countDown();
//                    }
//
//                    @Override
//                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> data) {
//                        super.onUnsuccessfulResponse(data);
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
//                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> data) {
//                        Log.d("AZAZA",""+data.toString());
//                        latch.countDown();
//                    }
//
//                    @Override
//                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> data) {
//                        super.onUnsuccessfulResponse(data);
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
