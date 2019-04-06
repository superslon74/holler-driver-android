package com.holler.app.Activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.holler.app.AndarApplication;
import com.holler.app.Helper.ConnectionHelper;
import com.holler.app.Helper.CustomDialog;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Helper.URLHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.holler.app.AndarApplication.trimMessage;

import com.facebook.accountkit.AccessToken;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.CustomActivity;


import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;


public class RegisterActivity extends CustomActivity implements RadioGroup.OnCheckedChangeListener {

    public Context context = RegisterActivity.this;
    public Activity activity = RegisterActivity.this;
    String TAG = "RegisterActivity";
    String device_token, device_UDID;
    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email, first_name, last_name, mobile_no, password, confirm_password;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    Boolean fromActivity = false;
    String strViewPager = "";
    RadioGroup genderGrp;
    ImageView maleImg, femaleImg;

    String gender = "male";
    public static int APP_REQUEST_CODE = 99;

    AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder;
    UIManager uiManager;

    Spinner serviceSpinner;

    private String blockCharacterSet = "~#^|$%&*!()_-*.,@/";
    Utilities utils = new Utilities();

    private InputFilter filter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    private UserServerApi serverApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        try {
            Intent intent = getIntent();
            if (intent != null) {

                if (getIntent().getExtras().containsKey("viewpager")) {
                    strViewPager = getIntent().getExtras().getString("viewpager");
                }

                if (getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = true;
                } else if (!getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = false;
                } else {
                    fromActivity = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fromActivity = false;
        }
        findViewById();
        GetToken();

        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        nextICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utilities.hideKeyboard(RegisterActivity.this);

                Pattern ps = Pattern.compile(".*[0-9].*");
                Matcher firstName = ps.matcher(first_name.getText().toString());
                Matcher lastName = ps.matcher(last_name.getText().toString());

                if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                    displayMessage(getString(R.string.email_validation));
                } else if (!Utilities.isValidEmail(email.getText().toString())) {
                    displayMessage(getString(R.string.not_valid_email));
                } else if (first_name.getText().toString().equals("") || first_name.getText().toString().equalsIgnoreCase(getString(R.string.first_name))) {
                    displayMessage(getString(R.string.first_name_empty));
                } else if (firstName.matches()) {
                    displayMessage(getString(R.string.first_name_no_number));
                } else if (last_name.getText().toString().equals("") || last_name.getText().toString().equalsIgnoreCase(getString(R.string.last_name))) {
                    displayMessage(getString(R.string.last_name_empty));
                } else if (lastName.matches()) {
                    displayMessage(getString(R.string.last_name_no_number));
                }  else if (password.getText().toString().equals("") ) {
                    displayMessage(getString(R.string.password_validation));
                } else if(password.length() < 8 || password.length() > 16){
                    displayMessage(getString(R.string.password_validation2));
                } else if(!Utilities.isValidPassword(password.getText().toString().trim())){
                    displayMessage(getString(R.string.password_validation2));
                }  else if (!password.getText().toString().equals(confirm_password.getText().toString())) {
                    displayMessage(getString(R.string.password_mismatch));
                } else {
                    if (isInternet) {
                        checkMailAlreadyExit();
                    } else {
                        displayMessage(getString(R.string.something_went_wrong_net));
                    }
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent mainIntent = new Intent(RegisterActivity.this, ActivityPassword.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                RegisterActivity.this.finish();*/
                onBackPressed();
            }
        });

        serverApiClient = UserServerApi.ApiCreator.createInstance();

    }

    public void findViewById() {
        email = (EditText) findViewById(R.id.email);
        first_name = (EditText) findViewById(R.id.first_name);
        last_name = (EditText) findViewById(R.id.last_name);
        mobile_no = (EditText) findViewById(R.id.mobile_no);
        password = (EditText) findViewById(R.id.password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        nextICON = (FloatingActionButton) findViewById(R.id.nextIcon);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        email.setText(SharedHelper.getKey(context, "email"));
        first_name.setFilters(new InputFilter[]{filter});
        last_name.setFilters(new InputFilter[]{filter});


        genderGrp = (RadioGroup) findViewById(R.id.gender_group);
        genderGrp.setOnCheckedChangeListener(this);

        maleImg= (ImageView) findViewById(R.id.male_img);
        femaleImg= (ImageView) findViewById(R.id.female_img);

        maleImg.setColorFilter(ContextCompat.getColor(context,R.color.theme));
        femaleImg.setColorFilter(ContextCompat.getColor(context,R.color.calendar_selected_date_text));
    }



    public void checkMailAlreadyExit(){
        showSpinner();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        UserServerApi.User user = new UserServerApi.User();
        user.email = email.getText().toString();

        serverApiClient
                .checkEmailExists(headers,user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(RegisterActivity.this) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        phoneLogin();
                        hideSpinner();
                    }

                    @Override
                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        if(response.code() == 422){
                            displayMessage(super.activity.getString(R.string.email_exist));
                            return;
                        }
                        super.onUnsuccessfulResponse(response);
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        hideSpinner();
                    }
                });

    }

    private void registerAPI() {

        showSpinner();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        final UserServerApi.User user = new UserServerApi.User();

        user.deviceType = "android";
        user.deviceId = device_UDID;
        user.deviceToken = device_token;
        user.loggedBy = "manual";
        user.firstName = first_name.getText().toString();
        user.lastName = last_name.getText().toString();
        user.gender = gender;
        user.mobile = SharedHelper.getKey(RegisterActivity.this, "mobile");

        user.email = email.getText().toString();
        user.password = password.getText().toString();
        user.passwordConfirmation = password.getText().toString();

        serverApiClient
                .register(headers,user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(RegisterActivity.this) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        SharedHelper.putKey(RegisterActivity.this, "email", email.getText().toString());
                        SharedHelper.putKey(RegisterActivity.this, "password", password.getText().toString());
                        signIn(user);
//                        hideSpinner();
                    }

                    @Override
                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        hideSpinner();
                        switch (response.code()){
                            case 403:
                                Toast.makeText(RegisterActivity.this, "Phone number already in use", Toast.LENGTH_LONG).show();
                                SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
                                Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mainIntent);
                                activity.finish();
                                break;
                            case 401: super.displayMessage(getString(R.string.something_went_wrong)); break;
                            case 422: super.displayMessage(getString(R.string.email_exist)); break;
                            default: super.onUnsuccessfulResponse(response);
                        }

                    }


                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
//                        hideSpinner();
                    }
                });
    }

    public void signIn(final UserServerApi.User user) {
        showSpinner();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");

        serverApiClient
                .signIn(headers,user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(RegisterActivity.this) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        String accessToken = response.body().get("access_token").getAsString();
                        String currency = response.body().get("currency").getAsString();
                        if(currency==null || currency.isEmpty()){
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
                        hideSpinner();
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
//                        hideSpinner();
                    }
                });

    }

    public void getProfile() {
        showSpinner();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(RegisterActivity.this, "access_token"));

        serverApiClient
                .profile(headers)
                .enqueue(new OrderServerApi.CallbackErrorHandler<UserServerApi.User>(RegisterActivity.this) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<UserServerApi.User> response) {
                        UserServerApi.User newUser = response.body();

                        SharedHelper.putKey(RegisterActivity.this, "id", newUser.id);
                        SharedHelper.putKey(RegisterActivity.this, "first_name", newUser.firstName);
                        SharedHelper.putKey(RegisterActivity.this, "last_name", newUser.lastName);
                        SharedHelper.putKey(RegisterActivity.this, "email", newUser.email);
                        if (newUser.avatar!=null && newUser.avatar.startsWith("http"))
                            SharedHelper.putKey(context, "picture", newUser.avatar);
                        else
                            SharedHelper.putKey(context, "picture", AccessDetails.serviceurl +  "/storage/" + newUser.avatar);

                        SharedHelper.putKey(RegisterActivity.this, "gender", "" + newUser.gender);
                        SharedHelper.putKey(RegisterActivity.this, "mobile", newUser.mobile);
                        SharedHelper.putKey(context, "approval_status", newUser.status);
                        SharedHelper.putKey(context, "currency",newUser.getCurrency());


                        SharedHelper.putKey(context, "sos", newUser.sos);
                        SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.True));

                        if (newUser.service != null) {
                            SharedHelper.putKey(context, "service", newUser.service.name);
                        }
                        SharedHelper.putKey(RegisterActivity.this, "login_by", "manual");

//                        hideSpinner();
                        GoToMainActivity();
                    }

                    @Override
                    public void onUnsuccessfulResponse(retrofit2.Response<UserServerApi.User> response) {
                        switch (response.code()){
                            case 401:
                                SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                                super.displayMessage(getString(R.string.something_went_wrong));
                                break;
                            case 422: super.displayMessage(getString(R.string.email_exist)); break;
                            default: super.onUnsuccessfulResponse(response);
                        }
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        hideSpinner();
                    }
                });
    }

    public void phoneLogin() {

        AccessToken accessToken = AccountKit.getCurrentAccessToken();

        if (accessToken != null) {
            Log.e("LALALA",accessToken.toString());
        } else {
            Log.e("LALALA","AccessToken not exist");
        }

        final Intent intent = new Intent(this, AccountKitActivity.class);
        uiManager = new SkinManager(SkinManager.Skin.TRANSLUCENT,
                ContextCompat.getColor(this, R.color.cancel_ride_color), R.drawable.banner_fb, SkinManager.Tint.WHITE, 85);
        configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        configurationBuilder.setUIManager(uiManager);
        String code = getCountryZipCode();
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder
                        .setInitialPhoneNumber(new PhoneNumber("+"+code, mobile_no.getText().toString(), ""))
                        .build()
        );
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    public String getCountryZipCode(){
        String countryId="";
        String countryCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        countryId= manager.getSimCountryIso().toUpperCase();
        String[] codesArray=this.getResources().getStringArray(R.array.CountryCodes);
        for(String codes : codesArray){
            String[] keyValye=codes.split(",");
            if(keyValye[1].trim().equals(countryId.trim())){
                countryCode=keyValye[0];
                break;
            }
        }
        return countryCode;
    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            if (data != null) {
                AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        Log.e(TAG, "onSuccess: Account Kit" + account.getId());
                        Log.e(TAG, "onSuccess: Account Kit" + AccountKit.getCurrentAccessToken().getToken());
                        if (AccountKit.getCurrentAccessToken().getToken() != null) {
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", AccountKit.getCurrentAccessToken().getToken());
                            PhoneNumber phoneNumber = account.getPhoneNumber();
                            String phoneNumberString = phoneNumber.toString();
                            SharedHelper.putKey(RegisterActivity.this, "mobile", phoneNumberString);
                            registerAPI();
                        } else {
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
                            SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.False));
                            SharedHelper.putKey(context, "email", "");
                            SharedHelper.putKey(context, "login_by", "");
                            SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
                            Intent goToLogin = new Intent(RegisterActivity.this, WelcomeScreenActivity.class);
                            goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(goToLogin);
                            finish();
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {
                        Log.e(TAG, "onError: Account Kit" + accountKitError);
                        displayMessage(""+getResources().getString(R.string.social_cancel));
                    }
                });
                if (loginResult != null) {
                    SharedHelper.putKey(this, "account_kit", getString(R.string.True));
                } else {
                    SharedHelper.putKey(this, "account_kit", getString(R.string.False));
                }
                String toastMessage;
                if (loginResult.getError() != null) {
                    toastMessage = loginResult.getError().getErrorType().getMessage();
                    // showErrorActivity(loginResult.getError());
                } else if (loginResult.wasCancelled()) {
                    toastMessage = "Login Cancelled";
                } else {
                    if (loginResult.getAccessToken() != null) {
                        Log.e(TAG, "onActivityResult: Account Kit" + loginResult.getAccessToken().toString());
                        SharedHelper.putKey(this, "account_kit", loginResult.getAccessToken().toString());
                        toastMessage = "Welcome to Andar Hand...";
                    } else {
                        SharedHelper.putKey(this, "account_kit", "");
                        toastMessage = String.format(
                                "Welcome to Andar Hand...",
                                loginResult.getAuthorizationCode().substring(0, 10));
                    }
                }
            }
        }
    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                utils.print(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = ""+ FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token",""+FirebaseInstanceId.getInstance().getToken());
                utils.print(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            utils.print(TAG, "Failed to complete token refresh");
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            utils.print(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            utils.print(TAG, "Failed to complete device UDID");
        }
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        try{

            Snackbar snackbar =  Snackbar.make(activity.getCurrentFocus(), toastString ,Snackbar.LENGTH_LONG)
                    .setDuration(Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView tv= (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(3);
            snackbar.show();

//            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
//                    .setAction("Action", null).show();
        }catch (Exception e){
            try{
                Toast.makeText(context,""+toastString,Toast.LENGTH_SHORT).show();
            }catch (Exception ee){
                e.printStackTrace();
            }
        }
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent = new Intent(activity, ActivityEmail.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    public void onBackPressed() {
        if (strViewPager.equalsIgnoreCase("yes")){
            super.onBackPressed();
        }else{
            if (fromActivity) {
                Intent mainIntent = new Intent(RegisterActivity.this, ActivityEmail.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                RegisterActivity.this.finish();
            } else if (!fromActivity){
                Intent mainIntent = new Intent(RegisterActivity.this, ActivityPassword.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                RegisterActivity.this.finish();
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.male_btn:
                gender = "male";
                maleImg.setColorFilter(ContextCompat.getColor(context,R.color.theme));
                femaleImg.setColorFilter(ContextCompat.getColor(context,R.color.calendar_selected_date_text));
                break;
            case R.id.female_btn:
                gender = "female";
                femaleImg.setColorFilter(ContextCompat.getColor(context,R.color.theme));
                maleImg.setColorFilter(ContextCompat.getColor(context,R.color.calendar_selected_date_text));
                break;
        }
    }


    private void showSpinner() {
        if(customDialog==null)
            customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();
    }

    private void hideSpinner() {
        customDialog.dismiss();
    }


    interface UserServerApi{

        @POST("api/provider/verify")
        Call<JsonObject> checkEmailExists(@HeaderMap Map<String, String> headers, @Body User user);

        @POST("api/provider/register")
        Call<JsonObject> register(@HeaderMap Map<String, String> headers, @Body User user);

        @POST("api/provider/oauth/token")
        Call<JsonObject> signIn(@HeaderMap Map<String, String> headers, @Body User user);

        @GET("api/provider/profile")
        Call<User> profile(@HeaderMap Map<String, String> headers);

        class User{
            @Expose
            @SerializedName("id")
            String id;
            @Expose
            @SerializedName("device_type")
            String deviceType;
            @Expose
            @SerializedName("device_id")
            String deviceId;
            @Expose
            @SerializedName("device_token")
            String deviceToken;
            @Expose
            @SerializedName("login_by")
            String loggedBy;
            @Expose
            @SerializedName("first_name")
            String firstName;
            @Expose
            @SerializedName("last_name")
            String lastName;
            @Expose
            @SerializedName("gender")
            String gender;
            @Expose
            @SerializedName("mobile")
            String mobile;
            @Expose
            @SerializedName("avatar")
            String avatar;

            @Expose
            @SerializedName("status")
            String status;
            @Expose
            @SerializedName("currency")
            String currency;
            @Expose
            @SerializedName("sos")
            String sos;
            @Expose
            @SerializedName("service")
            ServiceType service;

            @Expose
            @SerializedName("email")
            String email;
            @Expose
            @SerializedName("password")
            String password;
            @Expose
            @SerializedName("password_confirmation")
            String passwordConfirmation;

            class ServiceType{
                @Expose
                @SerializedName("service_type")
                String type;
                @Expose
                @SerializedName("name")
                String name;
            }

            public String getCurrency(){
                if(currency!=null && !currency.isEmpty()) return currency;
                return "$";
            }
        }


        class ApiCreator{
            public static UserServerApi createInstance(){
                ConnectionPool pool = new ConnectionPool(5, 10000, TimeUnit.MILLISECONDS);

                OkHttpClient httpClient = new OkHttpClient
                        .Builder()
                        .cache(null)
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .connectionPool(pool)
                        .build();

                return new Retrofit
                        .Builder()
                        .client(httpClient)
                        .baseUrl(URLHelper.base)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(UserServerApi.class);
            }
        }

    }

    /**
     * Tests above
     */
    private final CountDownLatch latch = new CountDownLatch(1);

    @Test
    public void testRegisterUser() throws InterruptedException{

        UserServerApi serverApiClient = UserServerApi.ApiCreator.createInstance();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
//        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
        UserServerApi.User user = new UserServerApi.User();
        user.email = "alex@gmail.com";
        user.deviceId = "23b50be39712afaa";
        user.deviceToken = "c6l63MYuExg:APA91bGYOJj69phx9I_3VMwiXt_bpE_1GyQi-LqtIgvKgWX75gDWBrU5qjr0k3g35JcnFizTr5zEq6YAnsCOrNmZWIq4-ukij8udYH5H-h5zGvlyND-8UOLTBIl9CZcIZCVlNkf8ikMb";
        user.deviceType = "android";
        user.firstName = "A";
        user.lastName = "B";
        user.gender = "male";
        user.loggedBy = "manual";
        user.mobile = "+380688574090";
        user.password = "1aaaaaaa";
        user.passwordConfirmation = "1aaaaaaa";


        serverApiClient
                .register(headers,user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(RegisterActivity.this) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        Log.d("AZAZA",""+response.toString());
                        latch.countDown();
                    }

                    @Override
                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        super.onUnsuccessfulResponse(response);
                        latch.countDown();
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        latch.countDown();
                    }
                });

        latch.await();
    }

    @Test
    public void testVerifyEmail() throws InterruptedException{


        UserServerApi serverApiClient = UserServerApi.ApiCreator.createInstance();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
//        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
        UserServerApi.User user = new UserServerApi.User();
        user.email = "alex@gmail.com";

        serverApiClient
                .checkEmailExists(headers,user)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(RegisterActivity.this) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        Log.d("AZAZA",""+response.toString());
                        latch.countDown();
                    }

                    @Override
                    public void onUnsuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        super.onUnsuccessfulResponse(response);
                        latch.countDown();
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                        latch.countDown();
                    }
                });

        latch.await();
    }


}
