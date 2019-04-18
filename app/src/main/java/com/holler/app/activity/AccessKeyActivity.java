package com.holler.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;
import com.holler.app.mvp.welcome.WelcomeView;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;


public class AccessKeyActivity extends AppCompatActivity {

    EditText txtAccessKey, txtUserName;

    FloatingActionButton btnAccessKey;

    LinearLayout lnrAccessLogin, lnrAccessLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_begin);
        initView();
    }

    private void initView() {
        txtAccessKey = (EditText) findViewById(R.id.txtAccessKey);
        txtUserName = (EditText) findViewById(R.id.txtUserName);
        lnrAccessLogin = (LinearLayout) findViewById(R.id.lnrAccessLogin);
        lnrAccessLoading = (LinearLayout) findViewById(R.id.lnrAccessLoading);
        btnAccessKey = (FloatingActionButton) findViewById(R.id.btnAccessKey);

        btnAccessKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtAccessKey.getText().toString().equalsIgnoreCase("")){
                    displayMessage(getResources().getString(R.string.enter_access_key));
                }else{
                    accessKeyAPI();
                }
            }
        });
    }

    public void accessKeyAPI() {

        JSONObject object = new JSONObject();
        try {
            object.put("username", txtUserName.getText().toString());
            object.put("accesskey",txtAccessKey.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadingVisibility();

        Log.e("REFACTORING", "This method should never be called");
//        AndarApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void processResponse(final JSONObject response) {
        try {
            AccessDetails accessDetails = new AccessDetails();
            accessDetails.status = response.optBoolean("status");

            if (accessDetails.status) {
                JSONArray jsonArrayData = response.optJSONArray("data");
                JSONObject jsonObjectData = jsonArrayData.optJSONObject(0);
                accessDetails.id = jsonObjectData.optInt("id");
                accessDetails.clientName = jsonObjectData.optString("client_name");
                accessDetails.email = jsonObjectData.optString("email");
                accessDetails.product = jsonObjectData.optString("product");
                accessDetails.username = jsonObjectData.optString("username");
                SharedHelper.putKey(AccessKeyActivity.this, "access_username", accessDetails.username);
                accessDetails.password = jsonObjectData.optString("password");
                SharedHelper.putKey(AccessKeyActivity.this, "access_password", accessDetails.password );
                accessDetails.passport = jsonObjectData.optString("passport");
                accessDetails.clientid = jsonObjectData.optInt("clientid");
                accessDetails.serviceurl = jsonObjectData.optString("serviceurl");
                accessDetails.isActive = jsonObjectData.optInt("is_active");
                accessDetails.createdAt = jsonObjectData.optString("created_at");
                accessDetails.updatedAt = jsonObjectData.optString("updated_at");
                accessDetails.isPaid = jsonObjectData.optInt("is_paid");
                accessDetails.isValid = jsonObjectData.optInt("is_valid");

                JSONObject jsonObjectSettings = response.optJSONObject("setting");

                accessDetails.siteTitle = jsonObjectSettings.optString("site_title");
                SharedHelper.putKey(AccessKeyActivity.this, "app_name", accessDetails.siteTitle);
                accessDetails.siteLogo = jsonObjectSettings.optString("site_logo");
                accessDetails.siteEmailLogo = jsonObjectSettings.optString("site_email_logo");
                accessDetails.siteIcon = jsonObjectSettings.optString("site_icon");
                accessDetails.site_icon = Utilities.drawableFromUrl(AccessKeyActivity.this, accessDetails.siteIcon);
                accessDetails.siteCopyright = jsonObjectSettings.optString("site_copyright");
                accessDetails.providerSelectTimeout = jsonObjectSettings.optString("provider_select_timeout");
                accessDetails.providerSearchRadius = jsonObjectSettings.optString("provider_search_radius");
                accessDetails.basePrice = jsonObjectSettings.optString("base_price");
                accessDetails.pricePerMinute = jsonObjectSettings.optString("price_per_minute");
                accessDetails.taxPercentage = jsonObjectSettings.optString("tax_percentage");
                accessDetails.stripeSecretKey = jsonObjectSettings.optString("stripe_secret_key");
                accessDetails.stripePublishableKey = jsonObjectSettings.optString("stripe_publishable_key");
                accessDetails.cash = jsonObjectSettings.optString("CASH");
                accessDetails.card = jsonObjectSettings.optString("CARD");
                accessDetails.manualRequest = jsonObjectSettings.optString("manual_request");
                accessDetails.defaultLang = jsonObjectSettings.optString("default_lang");
                accessDetails.currency = jsonObjectSettings.optString("currency");
                accessDetails.distance = jsonObjectSettings.optString("distance");
                accessDetails.scheduledCancelTimeExceed = jsonObjectSettings.optString("scheduled_cancel_time_exceed");
                accessDetails.pricePerKilometer = jsonObjectSettings.optString("price_per_kilometer");
                accessDetails.commissionPercentage = jsonObjectSettings.optString("commission_percentage");
                accessDetails.storeLinkAndroid = jsonObjectSettings.optString("store_link_android");
                accessDetails.storeLinkIos = jsonObjectSettings.optString("store_link_ios");
                accessDetails.dailyTarget = jsonObjectSettings.optString("daily_target");
                accessDetails.surgePercentage = jsonObjectSettings.optString("surge_percentage");
                accessDetails.surgeTrigger = jsonObjectSettings.optString("surge_trigger");
                accessDetails.demoMode = jsonObjectSettings.optString("demo_mode");
                accessDetails.bookingPrefix = jsonObjectSettings.optString("booking_prefix");
                accessDetails.sosNumber = jsonObjectSettings.optString("sos_number");
                accessDetails.contactNumber = jsonObjectSettings.optString("contact_number");
                accessDetails.contactEmail = jsonObjectSettings.optString("contact_email");
                accessDetails.socialLogin = jsonObjectSettings.optString("social_login");


                if (AccessDetails.isValid == 1){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GoToBeginActivity();
                        }
                    }, 2000);
                }else{
                    displayMessage(getResources().getString(R.string.demo_expired));
                }
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lnrAccessLogin.setVisibility(View.VISIBLE);
                        lnrAccessLoading.setVisibility(View.GONE);
                        displayMessage(response.optString("message"));
                    }
                }, 2000);
            }

        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public void GoToBeginActivity(){
        Intent mainIntent = new Intent(AccessKeyActivity.this, WelcomeView.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void loadingVisibility() {
        lnrAccessLogin.setVisibility(View.GONE);
        lnrAccessLoading.setVisibility(View.VISIBLE);
    }


    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }catch (Exception e){
            try {
                Toast.makeText(AccessKeyActivity.this, "" + toastString, Toast.LENGTH_SHORT).show();
            }catch (Exception ee){
                e.printStackTrace();
            }
        }
    }
}
