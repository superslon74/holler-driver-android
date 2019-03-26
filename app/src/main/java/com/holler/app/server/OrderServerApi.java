package com.holler.app.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.android.volley.TimeoutError;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.holler.app.Activity.HistoryDetails;
import com.holler.app.Activity.WelcomeScreenActivity;
import com.holler.app.Fragment.OnGoingTrips;
import com.holler.app.Helper.URLHelper;
import com.holler.app.R;
import com.holler.app.utils.Notificator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderServerApi {
    String API_URL_SCHEDULED_ORDERS = "api/provider/requests/upcoming";
    String API_URL_CANCEL_ORDER = "api/provider/cancel";


    @GET(API_URL_SCHEDULED_ORDERS)
    Call<List<Order>> getScheduledOrders(
            @HeaderMap Map<String, String> headers);

    @POST(API_URL_CANCEL_ORDER)
    Call<JsonObject> cancelOrder(
            @HeaderMap Map<String, String> headers,
            @Body Order id);

    @POST("/api/provider/trip/{id}")
    Call<ResponseBody> acceptOrder(
            @HeaderMap Map<String, String> headers,
            @Path("id") String id);

    @POST("/api/provider/trip/{id}/started")
    Call<ResponseBody> startTrip(
            @HeaderMap Map<String, String> headers,
            @Path("id") String id);

    @DELETE("/api/provider/trip/{id}")
    Call<ResponseBody> rejectOrder(
            @HeaderMap Map<String, String> headers,
            @Path("id") String id);

    class ApiCreator{
        public static OrderServerApi createInstance(){
            ConnectionPool pool = new ConnectionPool(4, 10000, TimeUnit.MILLISECONDS);

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
                    .create(OrderServerApi.class);
        }
    }

    abstract class CancelOrderCallbackHandler<T> extends CallbackErrorHandler<T>{

        private Order order;

        public CancelOrderCallbackHandler(Activity activity, Order order) {
            super(activity);
            this.order = order;
        }

        @Override
        public void onSuccessfulResponse(Response<T> response) {
//            TODO: unshedule notifications
            if(this.order!=null){
                if(order.isScheduled()){
                    String id = order.id;
                    Date scheduledDate;
                    try {
                        scheduledDate = order.getScheduledDate();
                    }catch (ParseException e){
                        Log.e("AZAZA", "Cant unschedule notification, order parse error");
                        return;
                    }catch (NullPointerException e){
                        Log.e("AZAZA", "Cant unschedule notification, order scheduledDate is null");
                        return;
                    }

                    Bundle arguments = new Bundle();
                    arguments.putString("post_value", id);
                    arguments.putString("tag", "upcoming_trips");

                    int notificationId = (int)(Long.parseLong(order.id));

                    new Notificator(activity)
                            .buildPendingIntent(HistoryDetails.class, arguments, notificationId)
                            .buildNotification(Notificator.generateTextBasedOnWarningMode())
                            .unscheduleNotification(notificationId, scheduledDate);
                }
            }else{
                Log.e("AZAZA", "Cant unschedule notification order is not defined");
            }
        }
    }

    abstract class AcceptOrderCallbackHandler<T> extends CallbackErrorHandler<T>{

        private Order order;

        public AcceptOrderCallbackHandler(Activity activity, Order order) {
            super(activity);
            this.order = order;
        }

        @Override
        public void onSuccessfulResponse(Response<T> response) {
//            TODO: shedule notifications
            if(this.order!=null){
                if(order.isScheduled()){
                    String id = order.id;
                    Date scheduledDate;
                    try {
                        scheduledDate = order.getScheduledDate();
                    }catch (ParseException e){
                        Log.e("AZAZA", "Cant schedule notification, order parse error");
                        return;
                    }catch (NullPointerException e){
                        Log.e("AZAZA", "Cant schedule notification, order scheduledDate is null");
                        return;
                    }

                    Bundle arguments = new Bundle();
                    arguments.putString("post_value", id);
                    arguments.putString("tag", "upcoming_trips");

                    int notificationId = (int)(Long.parseLong(order.id));

                    new Notificator(activity)
                            .buildPendingIntent(HistoryDetails.class, arguments, notificationId)
                            .buildNotification(Notificator.generateTextBasedOnWarningMode())
                            .scheduleNotification(notificationId, scheduledDate);
                }
            }else{
                Log.e("AZAZA", "Cant schedule notification order is not defined");
            }

        }
    }

    abstract class CallbackErrorHandler<T> implements Callback<T> {
        Activity activity;

        public CallbackErrorHandler(Activity activity) {
            this.activity = activity;
        }


        @Override
        public void onResponse(Call<T> call, retrofit2.Response<T> response) {
            if (response.isSuccessful()) {
                onSuccessfulResponse(response);
            } else {
                onUnsuccessfulResponse(response);
            }
            onFinishHandling();
        }

        public void onFinishHandling(){
//            hideSpinner();

        }

        public void onTimeoutRequest(){

        }

        public abstract void onSuccessfulResponse(retrofit2.Response<T> response);

        public void onUnsuccessfulResponse(retrofit2.Response<T> response) {

            switch (response.code()) {
                case 401:
                    Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(mainIntent);
                    activity.finish();
                    break;
                case 400:
                case 405:
                case 500:
                    displayMessage(activity.getString(R.string.something_went_wrong));
                    break;
                case 422:
                    displayMessage(activity.getString(R.string.please_try_again));
                    break;
                case 503:
                    displayMessage(activity.getString(R.string.server_down));
                    break;

                default:
                    displayMessage(activity.getString(R.string.please_try_again));
                    break;

            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (t instanceof TimeoutError) {
                onTimeoutRequest();
            } else {
                displayMessage(activity.getString(R.string.oops_connect_your_internet));
            }
            onFinishHandling();
        }

        private void displayMessage(String message){
            Snackbar.make(
                    activity.findViewById(R.id.parentLayout),
                    message,
                    Snackbar.LENGTH_SHORT
            ).setAction("Action", null).show();
        }
    }

    class Order {
        @Expose
        @SerializedName("id")
        public String id;
        @Expose(deserialize = false)
        @SerializedName("cancel_reason")
        public String cancelReason;
        @Expose(serialize = false)
        @SerializedName("static_map")
        public String staticMapUrl;
        @Expose(serialize = false)
        @SerializedName("schedule_at")
        public String scheduledDate;
        @Expose(serialize = false)
        @SerializedName("booking_id")
        public String bookedId;
        @Expose(serialize = false)
        @SerializedName("service_type")
        public ServiceType serviceType;

        public boolean isScheduled() {
            if (scheduledDate == null) return false;
            return !scheduledDate.isEmpty();
        }

        public Date getScheduledDate() throws ParseException, NullPointerException {
            if(scheduledDate == null) throw  new NullPointerException("Scheduled date is not defined");
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(this.scheduledDate);
            return d;
        }

        public class ServiceType {
            @Expose(serialize = false)
            @SerializedName("name")
            public String name;
            @Expose(serialize = false)
            @SerializedName("image")
            public String image;
        }
    }
}