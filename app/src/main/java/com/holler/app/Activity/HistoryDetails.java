package com.holler.app.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.holler.app.Fragment.OnGoingTrips;
import com.holler.app.server.OrderServerApi;
import com.squareup.picasso.Picasso;
import com.holler.app.AndarApplication;
import com.holler.app.Helper.ConnectionHelper;
import com.holler.app.Helper.CustomDialog;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Helper.URLHelper;
import com.holler.app.Helper.User;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;

import static com.holler.app.AndarApplication.trimMessage;

public class HistoryDetails extends AppCompatActivity {
    Activity activity;
    Context context;
    Boolean isInternet;
    ConnectionHelper helper;
    CustomDialog customDialog;
    TextView tripAmount;
    TextView tripDate;
    TextView paymentType;
    TextView tripComments;
    TextView tripProviderName;
    TextView tripSource;
    TextView tripDestination;
    TextView lblTitle;
    TextView tripId;
    TextView invoice_txt;
    TextView txt04Total;
    TextView txt04AmountToPaid;
    ImageView tripImg, tripProviderImg, paymentTypeImg;
    RatingBar tripProviderRating;
    LinearLayout sourceAndDestinationLayout;
    View viewLayout;
//    public JSONObject jsonObject;
    ImageView backArrow;
    LinearLayout parentLayout, lnrComments, lnrInvoiceSub, lnrInvoice;
    String tag = "";
    Button btnCancelRide, btnClose, btnViewInvoice, btnStartRide;
    Utilities utils = new Utilities();
    TextView lblBookingID, lblDistanceCovered, lblTimeTaken, lblBasePrice, lblDistancePrice, lblTaxPrice,lblDiscountPrice,lblWalletPrice;
    LinearLayout lnrBookingID,lnrDistanceTravelled, lnrTimeTaken, lnrBaseFare, lnrDistanceFare, lnrTax,lnrDiscount,lnrWallet;

    private OrderServerApi.Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);
        findViewByIdAndInitialize();
        order = new OrderServerApi.Order();
        try {
            Intent intent = getIntent();
            tag = intent.getExtras().getString("tag");
            order.id = intent.getExtras().getString("post_value");
        } catch (Exception e) {
            order.id = null;
            e.printStackTrace();
        }

        if (order.id != null) {

            if (tag.equalsIgnoreCase("past_trips")) {
                btnCancelRide.setVisibility(View.GONE);
                lnrComments.setVisibility(View.VISIBLE);
                getRequestDetails();
                lblTitle.setText("Past Trips");
                btnViewInvoice.setVisibility(View.VISIBLE);
                btnStartRide.setVisibility(View.GONE);
            } else {
                btnCancelRide.setVisibility(View.VISIBLE);
                lnrComments.setVisibility(View.GONE);
                getUpcomingDetails();
                lblTitle.setText("Upcoming Trips");
                btnViewInvoice.setVisibility(View.GONE);
                btnStartRide.setVisibility(View.VISIBLE);
            }
        }
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void findViewByIdAndInitialize() {
        activity = HistoryDetails.this;
        context = HistoryDetails.this;
        helper = new ConnectionHelper(activity);
        isInternet = helper.isConnectingToInternet();
        parentLayout = (LinearLayout) findViewById(R.id.parentLayout);
        parentLayout.setVisibility(View.GONE);
        tripAmount = (TextView) findViewById(R.id.tripAmount);
        tripDate = (TextView) findViewById(R.id.tripDate);
        paymentType = (TextView) findViewById(R.id.paymentType);
        paymentTypeImg = (ImageView) findViewById(R.id.paymentTypeImg);
        tripProviderImg = (ImageView) findViewById(R.id.tripProviderImg);
        tripImg = (ImageView) findViewById(R.id.tripImg);
        tripComments = (TextView) findViewById(R.id.tripComments);
        tripProviderName = (TextView) findViewById(R.id.tripProviderName);
        tripProviderRating = (RatingBar) findViewById(R.id.tripProviderRating);
        tripSource = (TextView) findViewById(R.id.tripSource);
        tripDestination = (TextView) findViewById(R.id.tripDestination);
        invoice_txt = (TextView) findViewById(R.id.invoice_txt);
        txt04Total = (TextView) findViewById(R.id.txt04Total);
        txt04AmountToPaid = (TextView) findViewById(R.id.txt04AmountToPaid);
        lblTitle = (TextView) findViewById(R.id.lblTitle);
        tripId = (TextView) findViewById(R.id.trip_id);
        sourceAndDestinationLayout = (LinearLayout) findViewById(R.id.sourceAndDestinationLayout);
        viewLayout = (View) findViewById(R.id.ViewLayout);
        btnCancelRide = (Button) findViewById(R.id.btnCancelRide);
        btnClose = (Button) findViewById(R.id.btnClose);
        btnViewInvoice = (Button) findViewById(R.id.btnViewInvoice);
        btnStartRide = (Button) findViewById(R.id.startRideBtn);
        lnrComments = (LinearLayout) findViewById(R.id.lnrComments);
        lnrInvoice = (LinearLayout) findViewById(R.id.lnrInvoice);
        lnrInvoiceSub = (LinearLayout) findViewById(R.id.lnrInvoiceSub);
        backArrow = (ImageView) findViewById(R.id.backArrow);

        lnrBookingID = (LinearLayout) findViewById(R.id.lnrBookingID);
        lnrDistanceTravelled = (LinearLayout) findViewById(R.id.lnrDistanceTravelled);
        lnrTimeTaken = (LinearLayout) findViewById(R.id.lnrTimeTaken);
        lnrBaseFare = (LinearLayout) findViewById(R.id.lnrBaseFare);
        lnrDistanceFare = (LinearLayout) findViewById(R.id.lnrDistanceFare);
        lnrTax = (LinearLayout) findViewById(R.id.lnrTax);
        lnrDiscount = (LinearLayout) findViewById(R.id.lnrDiscount);
        lnrWallet = (LinearLayout) findViewById(R.id.lnrWallet);


        lblBookingID = (TextView) findViewById(R.id.lblBookingID);
        lblDistanceCovered = (TextView) findViewById(R.id.lblDistanceCovered);
        lblTimeTaken = (TextView) findViewById(R.id.lblTimeTaken);
        lblBasePrice = (TextView) findViewById(R.id.lblBasePrice);
        lblTaxPrice = (TextView) findViewById(R.id.lblTaxPrice);
        lblDistancePrice = (TextView) findViewById(R.id.lblDistancePrice);
        lblDiscountPrice = (TextView) findViewById(R.id.lblDiscountPrice);
        lblWalletPrice = (TextView) findViewById(R.id.lblWalletPrice);


        LayerDrawable drawable = (LayerDrawable) tripProviderRating.getProgressDrawable();
        drawable.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lnrInvoice.setVisibility(View.GONE);
            }
        });

        btnViewInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lnrInvoice.setVisibility(View.VISIBLE);
            }
        });

        btnStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRide();
            }
        });

        lnrInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lnrInvoice.setVisibility(View.GONE);
            }
        });

        lnrInvoiceSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnCancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(getString(R.string.cencel_request))
                        .setIcon(AccessDetails.site_icon)
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                cancelRequest();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void startRide(){
        showSpinner();

        Map<String,String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));

        OrderServerApi serverClientApi = OrderServerApi.ApiCreator.createInstance();
        serverClientApi
                .startTrip(headers, order.id)
                .enqueue(new OrderServerApi.CallbackErrorHandler<ResponseBody>(activity) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<ResponseBody> response) {
                        Intent intent = new Intent(HistoryDetails.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onFinishHandling() {
                        hideSpinner();

                    }

                    @Override
                    public void onTimeoutRequest() {
                        startRide();
                    }

                });
    }


    private void setDetails(JSONArray response) {
        if (response != null && response.length() > 0) {
            Glide.with(activity).load(response.optJSONObject(0).optString("static_map")).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(tripImg);
            if (!response.optJSONObject(0).optString("payment").equalsIgnoreCase("null")) {
                Log.e("History Details", "onResponse: Currency" + SharedHelper.getKey(context, "currency"));
                //tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optString("total"));
            } else {
                //tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + "0");
            }
            String form;
            if (tag.equalsIgnoreCase("past_trips")) {
                form = response.optJSONObject(0).optString("assigned_at");
            } else {
                form = response.optJSONObject(0).optString("schedule_at");
            }
            try {
                order.scheduledDate = form;
                        tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + "\n" + getTime(form));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tripId.setText(response.optJSONObject(0).optString("booking_id"));
            paymentType.setText(response.optJSONObject(0).optString("payment_mode"));
            if (response.optJSONObject(0).optString("payment_mode").equalsIgnoreCase("CASH")) {
                paymentTypeImg.setImageResource(R.drawable.money1);
            } else {
                paymentTypeImg.setImageResource(R.drawable.visa_icon);
            }
            if (response.optJSONObject(0).optJSONObject("user").optString("picture").startsWith("http"))
                Picasso.with(activity).load(response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
            else
                Picasso.with(activity).load(AccessDetails.serviceurl +  "/storage/" + response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
            final JSONArray res = response;
            tripProviderImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject = res.optJSONObject(0).optJSONObject("user");

                    User user = new User();
                    user.setFirstName(jsonObject.optString("first_name"));
                    user.setLastName(jsonObject.optString("last_name"));
                    user.setEmail(jsonObject.optString("email"));
                    if (jsonObject.optString("picture").startsWith("http"))
                        user.setImg(jsonObject.optString("picture"));
                    else
                        user.setImg(AccessDetails.serviceurl + "/storage/" + jsonObject.optString("picture"));
                    user.setRating(jsonObject.optString("rating"));
                    user.setMobile(jsonObject.optString("mobile"));
                    Intent intent = new Intent(context, ShowProfile.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                }
            });

            if (response.optJSONObject(0).optJSONObject("user").optString("rating") != null &&
                    !response.optJSONObject(0).optJSONObject("user").optString("rating").equalsIgnoreCase(""))
                tripProviderRating.setRating(Float.parseFloat(response.optJSONObject(0).optJSONObject("user").optString("rating")));
            else {
                tripProviderRating.setRating(0);
            }

            /*if (!response.optJSONObject(0).optString("rating").equalsIgnoreCase("null") &&
                    !response.optJSONObject(0).optJSONObject("rating").optString("user_comment").equalsIgnoreCase("")) {
                tripComments.setText(response.optJSONObject(0).optJSONObject("rating").optString("user_comment"));
            } else {
                tripComments.setText(getString(R.string.no_comments));
            }*/
            tripProviderName.setText(response.optJSONObject(0).optJSONObject("user").optString("first_name") + " " + response.optJSONObject(0).optJSONObject("user").optString("last_name"));
            if (response.optJSONObject(0).optString("s_address") == null || response.optJSONObject(0).optString("d_address") == null || response.optJSONObject(0).optString("d_address").equals("") || response.optJSONObject(0).optString("s_address").equals("")) {
                sourceAndDestinationLayout.setVisibility(View.GONE);
                viewLayout.setVisibility(View.GONE);
            } else {
                tripSource.setText(response.optJSONObject(0).optString("s_address"));
                tripDestination.setText(response.optJSONObject(0).optString("d_address"));
            }
            parentLayout.setVisibility(View.VISIBLE);
        }
    }

    public void getRequestDetails() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                AccessDetails.serviceurl
                        + URLHelper.GET_HISTORY_DETAILS_API
                        + "?request_id="
                        + order.id,
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                utils.print("Get Trip details", response.toString());
                if (response != null && response.length() > 0) {
                    Glide.with(activity).load(response.optJSONObject(0).optString("static_map")).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(tripImg);
                    if (!response.optJSONObject(0).optString("payment").equalsIgnoreCase("null")) {
                        Log.e("History Details", "onResponse: Currency" + SharedHelper.getKey(context, "currency"));
                        tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optString("total"));
                    } else {
                        tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + "0");
                    }

                    lblBasePrice.setText(SharedHelper.getKey(context, "currency")  + response.optJSONObject(0).optJSONObject("payment").optInt("fixed"));
                    lblDistancePrice.setText(SharedHelper.getKey(context, "currency")  + response.optJSONObject(0).optJSONObject("payment").optInt("distance"));
                    lblTaxPrice.setText(SharedHelper.getKey(context, "currency")  + response.optJSONObject(0).optInt("tax"));
                    lblBookingID.setText(""+response.optJSONObject(0).optString("booking_id"));
                    lblDistanceCovered.setText(response.optJSONObject(0).optInt("distance") + " KM");
                    lblTimeTaken.setText(response.optJSONObject(0).optString("travel_time") + " mins");
                    lblDiscountPrice.setText(SharedHelper.getKey(context, "currency")  + response.optJSONObject(0).optJSONObject("payment").optInt("discount"));
                    lblWalletPrice.setText(SharedHelper.getKey(context, "currency")  + response.optJSONObject(0).optJSONObject("payment").optInt("wallet"));

                    if (!response.optJSONObject(0).optJSONObject("payment").optString("wallet").equalsIgnoreCase("0") && response.optJSONObject(0).optJSONObject("payment").optString("wallet")!=null ){
                        lnrWallet.setVisibility(View.VISIBLE);
                    }

                    if (!response.optJSONObject(0).optJSONObject("payment").optString("discount").equalsIgnoreCase("0") && response.optJSONObject(0).optJSONObject("payment").optString("discount")!=null ){
                        lnrDiscount.setVisibility(View.VISIBLE);
                    }

                    txt04Total.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("total"));
                    txt04AmountToPaid.setText(SharedHelper.getKey(context, "currency") + "" + response.optJSONObject(0).optJSONObject("payment").optInt("payable"));
                    String form;
                    if (tag.equalsIgnoreCase("past_trips")) {
                        form = response.optJSONObject(0).optString("assigned_at");
                    } else {
                        form = response.optJSONObject(0).optString("schedule_at");
                    }
                    try {
                        order.scheduledDate = form;
                        tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + "\n" + getTime(form));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    paymentType.setText(response.optJSONObject(0).optString("payment_mode"));
                    if (response.optJSONObject(0).optString("payment_mode").equalsIgnoreCase("CASH")) {
                        paymentTypeImg.setImageResource(R.drawable.money1);
                    } else {
                        paymentTypeImg.setImageResource(R.drawable.visa_icon);
                    }
                    if (response.optJSONObject(0).optJSONObject("user").optString("picture").startsWith("http"))
                        Picasso.with(activity).load(response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
                    else
                        Picasso.with(activity).load(AccessDetails.serviceurl +  "/storage/" + response.optJSONObject(0).optJSONObject("user").optString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(tripProviderImg);
                    final JSONArray res = response;
                    tripProviderImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject jsonObject = res.optJSONObject(0).optJSONObject("user");

                            User user = new User();
                            user.setFirstName(jsonObject.optString("first_name"));
                            user.setLastName(jsonObject.optString("last_name"));
                            user.setEmail(jsonObject.optString("email"));
                            if (jsonObject.optString("picture").startsWith("http"))
                                user.setImg(jsonObject.optString("picture"));
                            else
                                user.setImg(AccessDetails.serviceurl +  "/storage/" + jsonObject.optString("picture"));
                            user.setRating(jsonObject.optString("rating"));
                            user.setMobile(jsonObject.optString("mobile"));
                            Intent intent = new Intent(context, ShowProfile.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                        }
                    });

                    tripId.setText(response.optJSONObject(0).optString("booking_id"));

                    if (response.optJSONObject(0).optJSONObject("user").optString("rating") != null &&
                            !response.optJSONObject(0).optJSONObject("user").optString("rating").equalsIgnoreCase(""))
                        tripProviderRating.setRating(Float.parseFloat(response.optJSONObject(0).optJSONObject("user").optString("rating")));
                    else {
                        tripProviderRating.setRating(0);
                    }

                    if (!response.optJSONObject(0).optString("rating").equalsIgnoreCase("null") &&
                            !response.optJSONObject(0).optJSONObject("rating").optString("user_comment").equalsIgnoreCase("")) {
                        tripComments.setText(response.optJSONObject(0).optJSONObject("rating").optString("user_comment"));
                    } else {
                        tripComments.setText(getString(R.string.no_comments));
                    }
                    tripProviderName.setText(response.optJSONObject(0).optJSONObject("user").optString("first_name") + " " + response.optJSONObject(0).optJSONObject("user").optString("last_name"));
                    if (response.optJSONObject(0).optString("s_address") == null || response.optJSONObject(0).optString("d_address") == null || response.optJSONObject(0).optString("d_address").equals("") || response.optJSONObject(0).optString("s_address").equals("")) {
                        sourceAndDestinationLayout.setVisibility(View.GONE);
                        viewLayout.setVisibility(View.GONE);
                    } else {
                        tripSource.setText(response.optJSONObject(0).optString("s_address"));
                        tripDestination.setText(response.optJSONObject(0).optString("d_address"));
                    }
                    parentLayout.setVisibility(View.VISIBLE);
                }
                customDialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
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
                                e.printStackTrace();
                            }

                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));

                        } else {
                            displayMessage(getString(R.string.please_try_again));

                        }

                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                        e.printStackTrace();
                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getRequestDetails();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                utils.print("Token", "" + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        AndarApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    private void showSpinner(){
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();
    }

    private void hideSpinner(){
        customDialog.dismiss();
    }

    public void cancelRequest() {

        showSpinner();

        Map<String,String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));

        OrderServerApi serverClientApi = OrderServerApi.ApiCreator.createInstance();
        serverClientApi
                .cancelOrder(headers, order)
                .enqueue(new OrderServerApi.CancelOrderCallbackHandler<JsonObject>(activity,order) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        super.onSuccessfulResponse(response);
                    }

                    @Override
                    public void onFinishHandling() {
                        hideSpinner();
                        Intent intent = new Intent(HistoryDetails.this,HistoryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onTimeoutRequest() {
                        cancelRequest();
                    }

                });
    }


    public void displayMessage(String toastString) {
        Snackbar.make(
                findViewById(R.id.parentLayout),
                toastString,
                Snackbar.LENGTH_SHORT
        ).setAction("Action", null).show();

    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent;

            mainIntent = new Intent(activity, WelcomeScreenActivity.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void getUpcomingDetails() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                AccessDetails.serviceurl
                        + URLHelper.UPCOMING_TRIP_DETAILS
                        + "?request_id="
                        + order.id,
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                setDetails(response);
                utils.print("Get Upcoming Details", response.toString());
                customDialog.dismiss();
                parentLayout.setVisibility(View.VISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                customDialog.dismiss();
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
                                e.printStackTrace();
                            }

                        } else if (response.statusCode == 401) {
                            GoToBeginActivity();
                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            displayMessage(getString(R.string.server_down));

                        } else {
                            displayMessage(getString(R.string.please_try_again));

                        }

                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                        e.printStackTrace();
                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getUpcomingDetails();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        AndarApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    private String getMonth(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String monthName = new SimpleDateFormat("MMM").format(cal.getTime());
        return monthName;
    }

    private String getDate(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String dateName = new SimpleDateFormat("dd").format(cal.getTime());
        return dateName;
    }

    private String getYear(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String yearName = new SimpleDateFormat("yyyy").format(cal.getTime());
        return yearName;
    }

    private String getTime(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String timeName = new SimpleDateFormat("hh:mm a").format(cal.getTime());
        return timeName;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
