package com.holler.app.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.MapsInitializer;
import com.google.firebase.iid.FirebaseInstanceId;
import com.holler.app.BuildConfig;
import com.holler.app.FCM.ForceUpdateChecker;
import com.holler.app.Helper.ConnectionHelper;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Helper.URLHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;
import com.holler.app.di.AppComponent;
import com.holler.app.di.DaggerSplashScreenComponent;
import com.holler.app.di.Presenter;
import com.holler.app.di.SplashScreenModule;
import com.holler.app.di.SplashScreenPresenter;
import com.holler.app.utils.CustomActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;


public class SplashScreen
        extends CustomActivity
        implements ForceUpdateChecker.OnUpdateNeededListener, SplashScreenPresenter.View {

    @Inject
    Presenter presenter;

    private void setupComponent(){
        AppComponent appComponent = (AppComponent) AndarApplication.get(this).component();
        DaggerSplashScreenComponent.builder()
                .appComponent(appComponent)
                .splashScreenModule(new SplashScreenModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    /*****************************************************************************/
    String TAG = "SplashActivity";
    public Context context;
    ConnectionHelper helper;
    Handler handleCheckStatus;
    int retryCount = 0;
    AlertDialog alert;
    String device_token, device_UDID;
    TextView lblVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupComponent();

        setContentView(R.layout.activity_splash);
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        helper = new ConnectionHelper(context);
        lblVersion = (TextView) findViewById(R.id.lblVersion);
        lblVersion.setText(getResources().getString(R.string.version) +" "+ BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")");
        handleCheckStatus = new Handler();

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        handleCheckStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
//                Intent uploadDocuments = new Intent(activity, DocumentsActivity.class);
//                uploadDocuments.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(uploadDocuments);
//                activity.finish()
                if (helper.isConnectingToInternet()) {
                    if (SharedHelper.getKey(context, "loggedIn").equalsIgnoreCase(getString(R.string.True))) {
                        GetToken();
                        signIn();
                    } else {
                        GoToBeginActivity();
                        handleCheckStatus.postDelayed(this, 3000);
                    }
                    if (alert != null && alert.isShowing()) {
                        alert.dismiss();
                    }
                } else {
                    showDialog();
                    handleCheckStatus.postDelayed(this, 3000);
                }
            }
        }, 5000);


    }

    private void signIn() {

        if (true) {

            JSONObject object = new JSONObject();
            try {
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                object.put("email", SharedHelper.getKey(context, "email"));
                object.put("password", SharedHelper.getKey(context, "password"));



            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.serviceurl + URLHelper.login, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    SharedHelper.putKey(context, "access_token", response.optString("access_token"));

                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    getProfile();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    GoToBeginActivity();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };

            AndarApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }


    public void GetToken() {
        try {
            if(!SharedHelper.getKey(context,"device_token").equals("") && SharedHelper.getKey(context,"device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                Log.i(TAG, "GCM Registration Token: " + device_token);
            }else{
                device_token = ""+ FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token",""+FirebaseInstanceId.getInstance().getToken());
                Log.i(TAG, "Failed to complete token refresh: " + device_token);
            }
        }catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            Log.i(TAG, "Device UDID:" + device_UDID);
        }catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Log.d(TAG, "Failed to complete device UDID");
        }
    }

    public void getProfile() {
        retryCount++;
        JSONObject object = new JSONObject();
        String str = AccessDetails.serviceurl +
                URLHelper.USER_PROFILE_API+
                "?device_type=android&device_id="
                +device_UDID+"&device_token="
                +device_token;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                str,
                object,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SharedHelper.putKey(context, "id", response.optString("id"));
                SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                SharedHelper.putKey(context, "email", response.optString("email"));
                SharedHelper.putKey(context, "sos", response.optString("sos"));
                if (response.optString("avatar").startsWith("http"))
                    SharedHelper.putKey(context, "picture", response.optString("avatar"));
                else
                    SharedHelper.putKey(context, "picture", AccessDetails.serviceurl +  "/storage/" + response.optString("avatar"));
                SharedHelper.putKey(context, "gender", response.optString("gender"));
                SharedHelper.putKey(context, "mobile", response.optString("mobile"));
                SharedHelper.putKey(context, "approval_status", response.optString("status"));
                if(!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                    SharedHelper.putKey(context, "currency",response.optString("currency"));
                else
                    SharedHelper.putKey(context, "currency","$");
                SharedHelper.putKey(context, "loggedIn", getString(R.string.True));

                if (response.optJSONObject("service") != null) {
                    try {
                        JSONObject service = response.optJSONObject("service");
                        if (service.optJSONObject("service_type") != null) {
                            JSONObject serviceType = service.optJSONObject("service_type");
                            SharedHelper.putKey(context, "service", serviceType.optString("name"));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                if (response.optString("status").equalsIgnoreCase("new")) {
//                    Intent intent = new Intent(activity, WaitingForApproval.class);

                    Intent uploadDocuments = new Intent(SplashScreen.this, DocumentsActivity.class);
                    uploadDocuments.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                    SplashScreen.this.startActivity(uploadDocuments);
                } else {
                    GoToMainActivity();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (retryCount < 5) {
                    getProfile();
                } else {
                    GoToBeginActivity();
                }
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {

                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                            if (retryCount > 5)
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {

                            json = AndarApplication.trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));
                        }
                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                    }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getProfile();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                String STR = SharedHelper.getKey(context, "access_token");
                headers.put("Authorization", "Bearer " + STR);
                return headers;
            }
        };

        AndarApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }



    public void GoToMainActivity() {
       shouldGoToMainActivity = true;
    }

    public void GoToBeginActivity(){
        shouldGoToBeginActivity = true;
    }


    public void displayMessage(String toastString) {
    }

    @Override
    protected void onDestroy() {
        handleCheckStatus.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.connect_to_network))
                .setIcon(AccessDetails.site_icon)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.connect_to_wifi), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alert.dismiss();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alert.dismiss();
                        finish();
                    }
                });
        if (alert == null) {
            alert = builder.create();
            alert.show();
        }
    }


    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.new_version_available))
                .setIcon(AccessDetails.site_icon)
                .setMessage(getResources().getString(R.string.update_to_continue))
                .setPositiveButton(getResources().getString(R.string.update),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton(getResources().getString(R.string.no_thanks),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                .create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkOverlayPermission();

    }

    private void checkOverlayPermission(){
        final CustomActivity activity = this;
        String overlayPermission = Manifest.permission.SYSTEM_ALERT_WINDOW;
        RequestPermissionHandler overlay = new RequestPermissionHandler() {
            @Override
            public void onPermissionGranted() {
                checkLocationPermission();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(activity,"Floating view not allowed",Toast.LENGTH_LONG).show();
                checkOverlayPermission();
            }
        };
        activity.checkPermissionAsynchronously(overlayPermission,overlay);
    }

    private void checkLocationPermission(){
        final CustomActivity activity = this;
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        RequestPermissionHandler location = new RequestPermissionHandler() {
            @Override
            public void onPermissionGranted() {
                onAllPermissionsGranted();
            }

            @Override
            public void onPermissionDenied() {
                checkLocationPermission();
            }
        };
        activity.checkPermissionAsynchronously(locationPermission,location);
    }

//    TODO: shitcode here
    private boolean shouldGoToMainActivity = false;
    private boolean shouldGoToBeginActivity = false;
    private void onAllPermissionsGranted(){
        if(shouldGoToMainActivity){
            Intent mainIntent = new Intent(SplashScreen.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainIntent);
            finish();
        }else if(shouldGoToBeginActivity){
            Intent mainIntent = new Intent(SplashScreen.this, WelcomeScreenActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        }


    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(SplashScreen.this, message, Toast.LENGTH_SHORT).show();
    }
}
