package com.holler.app.Fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.holler.app.Activity.DocumentsActivity;
import com.holler.app.Activity.HistoryDetails;
import com.holler.app.Activity.MainActivity;
import com.holler.app.Activity.WelcomeScreenActivity;
import com.holler.app.Helper.ConnectionHelper;
import com.holler.app.Helper.CustomDialog;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Helper.URLHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.Services.NotificationPublisher;
import com.holler.app.Services.UserStatusChecker;
import com.holler.app.utils.Notificator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public class OnGoingTrips extends Fragment {

    Activity activity;
    Context context;
    View rootView;
    UpcomingsAdapter upcomingsAdapter;
    RecyclerView recyclerView;
    RelativeLayout errorLayout;
    ConnectionHelper helper;
    CustomDialog spinner;

    LinearLayout toolbar;
    ImageView backImg;

    private OrderServerAPI serverApiClient;

    public OnGoingTrips() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initializing retrofit client
        ConnectionPool pool = new ConnectionPool(4, 10000, TimeUnit.MILLISECONDS);

        OkHttpClient httpClient = new OkHttpClient
                .Builder()
                .cache(null)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(pool)
                .build();

        serverApiClient = new Retrofit
                .Builder()
                .client(httpClient)
                .baseUrl(URLHelper.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OrderServerAPI.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_on_going_trips, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        errorLayout = (RelativeLayout) rootView.findViewById(R.id.errorLayout);
        errorLayout.setVisibility(View.GONE);

        toolbar = (LinearLayout) rootView.findViewById(R.id.lnrTitle);
        backImg = (ImageView) rootView.findViewById(R.id.backArrow);

        helper = new ConnectionHelper(getActivity());
        if (helper.isConnectingToInternet()) {
            getUpcomingList();
        }

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        Bundle bundle = getArguments();
        String toolbar = null;
        if (bundle != null)
            toolbar = bundle.getString("toolbar");

        if (toolbar != null && toolbar.length() > 0) {
            this.toolbar.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        if (upcomingsAdapter != null) {
            getUpcomingList();
        }
        super.onResume();
    }

    public void getUpcomingList() {

        showSpinner();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));

        serverApiClient
                .getScheduledOrders(headers)
                .enqueue(new CallbackHandler<List<Order>>() {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<List<Order>> response) {
                        List<Order> orders = response.body();
                        createScheduledNotificaation(orders);

                        upcomingsAdapter = new UpcomingsAdapter(orders);

                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        if (upcomingsAdapter != null && upcomingsAdapter.getItemCount() > 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            errorLayout.setVisibility(View.GONE);
                            recyclerView.setAdapter(upcomingsAdapter);
                        } else {
                            errorLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });


    }

    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    private void createScheduledNotificaation(List<Order> orders) {

        for(Order o : orders){
            try {
                String text = "You have accepted scheduled ride on " + generateDateRepresentation(o.scheduledDate);
                Date date = o.getScheduledDate();
                scheduleNotification(context,Notificator.WARNING_10_MIN,o.id,date);
                scheduleNotification(context,Notificator.WARNING_30_MIN,o.id,date);
                scheduleNotification(context,Notificator.WARNING_60_MIN,o.id,date);
            }catch (Exception e){
                Log.e("AZAZA", "Can't schedule notification..");
                e.printStackTrace();
            }
        }


    }


    private void scheduleNotification(Context context,
                                      int warningMode,
                                     String orderId,
                                     Date scheduledDate)
    {
//        TODO: move it to begin screen

        Bundle arguments = new Bundle();
        arguments.putString("post_value", orderId);
        arguments.putString("tag", "upcoming_trips");

        int notificationId = (int)(Long.parseLong(orderId+""+warningMode));


        new Notificator(context)
                .buildPendingIntent(HistoryDetails.class, arguments, notificationId)
                .buildNotification(Notificator.generateTextBasedOnWarningMode(warningMode))
                .scheduleNotification(warningMode, notificationId, scheduledDate);

    }

    private class UpcomingsAdapter extends RecyclerView.Adapter<UpcomingsAdapter.MyViewHolder> {
        List<Order> orders;

        public UpcomingsAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @Override
        public UpcomingsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.upcoming_list_item, parent, false);
            return new UpcomingsAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(UpcomingsAdapter.MyViewHolder holder, final int position) {
            final Order currentOrder = orders.get(position);

            Glide
                    .with(activity)
                    .load(currentOrder.staticMapUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.tripImg);

            if (currentOrder.isScheduled()) {
                holder.tripDate.setText(generateDateRepresentation(currentOrder.scheduledDate));
                holder.tripId.setText(currentOrder.bookedId);
            }
            if (currentOrder.serviceType != null) {
                holder.car_name.setText(currentOrder.serviceType.name);
                Glide
                        .with(activity)
                        .load(currentOrder.serviceType.image)
                        .placeholder(R.drawable.car_select)
                        .error(R.drawable.car_select)
                        .dontAnimate()
                        .into(holder.driver_image);
            }

            holder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(AccessDetails.site_icon);
                    builder.setMessage(getString(R.string.cencel_request))
                            .setCancelable(false)
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    cancelRequest(currentOrder);
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

        @Override
        public int getItemCount() {
            return orders.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tripTime, car_name;
            TextView tripDate, tripAmount, tripId;
            ImageView tripImg, driver_image;
            Button btnCancel;

            public MyViewHolder(View itemView) {
                super(itemView);
                tripDate = (TextView) itemView.findViewById(R.id.tripDate);
                tripTime = (TextView) itemView.findViewById(R.id.tripTime);
                tripAmount = (TextView) itemView.findViewById(R.id.tripAmount);
                tripImg = (ImageView) itemView.findViewById(R.id.tripImg);
                car_name = (TextView) itemView.findViewById(R.id.car_name);
                driver_image = (ImageView) itemView.findViewById(R.id.driver_image);
                btnCancel = (Button) itemView.findViewById(R.id.btnCancel);
                tripId = (TextView) itemView.findViewById(R.id.tripid);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (helper.isConnectingToInternet()) {
                            Intent intent = new Intent(getActivity(), HistoryDetails.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("post_value", orders.get(getAdapterPosition()).id);
                            intent.putExtra("tag", "upcoming_trips");
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, "Oops, Connect your internet", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    public void cancelRequest(final Order order) {

        showSpinner();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));


        serverApiClient
                .cancelOrder(headers, order)
                .enqueue(new CallbackHandler<JsonObject>() {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
//                        TODO: unschedule notification
//                        try{
//                            String title = "% min warning";
//                            String text = "You have accepted scheduled ride on " + generateDateRepresentation(order.scheduledDate);
//                            Date date = order.getScheduledDate();
//                            scheduleNotification(context,ALARM_SET_CANCEL,WARNING_10_MIN,order.id,date,text);
//                            scheduleNotification(context,ALARM_SET_CANCEL,WARNING_30_MIN,order.id,date,text);
//                            scheduleNotification(context,ALARM_SET_CANCEL,WARNING_60_MIN,order.id,date,text);
//                        }catch(Exception e){
//
//                        }

                        getUpcomingList();
                    }
                });
    }

    private void showSpinner() {
        if (spinner == null) {
            spinner = new CustomDialog(context);
            spinner.setCancelable(false);
        }
        spinner.show();
    }

    private void hideSpinner() {
        spinner.dismiss();
    }


    private String generateDateRepresentation(String date) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
            return new SimpleDateFormat("d'th' MMM yyyy 'at' hh.mm a", Locale.ENGLISH).format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Wrong date";
        }
    }

    private abstract class CallbackHandler<T> implements Callback<T> {
        @Override
        public void onResponse(Call<T> call, retrofit2.Response<T> response) {
            if (response.isSuccessful()) {
                onSuccessfulResponse(response);
            } else {
                onUnsuccessfulResponse(response);
            }
            hideSpinner();
        }

        public abstract void onSuccessfulResponse(retrofit2.Response<T> response);

        public void onUnsuccessfulResponse(retrofit2.Response<T> response) {
            errorLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            switch (response.code()) {
                case 401:
                    GoToBeginActivity();
                    break;
                case 400:
                case 405:
                case 500:
                    displayMessage(getString(R.string.something_went_wrong));
                    break;
                case 422:
                    displayMessage(getString(R.string.please_try_again));
                    break;
                case 503:
                    displayMessage(getString(R.string.server_down));
                    break;

                default:
                    displayMessage(getString(R.string.please_try_again));
                    break;

            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (t instanceof TimeoutError) {
                getUpcomingList();
            } else {
                displayMessage(getString(R.string.oops_connect_your_internet));
            }
            hideSpinner();
        }
    }

    public static class Order {
        @Expose
        @SerializedName("id")
        String id;
        @Expose(serialize = false)
        @SerializedName("static_map")
        String staticMapUrl;
        @Expose(serialize = false)
        @SerializedName("schedule_at")
        String scheduledDate;
        @Expose(serialize = false)
        @SerializedName("booking_id")
        String bookedId;
        @Expose(serialize = false)
        @SerializedName("service_type")
        ServiceType serviceType;

        public boolean isScheduled() {
            if (scheduledDate == null) return false;
            return !scheduledDate.isEmpty();
        }

        public Date getScheduledDate() throws ParseException, NullPointerException {
            if(scheduledDate == null) throw  new NullPointerException("Scheduled date is not defined");
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(this.scheduledDate);
            return d;
        }

        private class ServiceType {
            @Expose(serialize = false)
            @SerializedName("name")
            String name;
            @Expose(serialize = false)
            @SerializedName("image")
            String image;
        }
    }

    public interface OrderServerAPI {

        @GET("api/provider/requests/upcoming")
        Call<List<Order>> getScheduledOrders(
                @HeaderMap Map<String, String> headers);

        @POST("api/provider/cancel")
        Call<JsonObject> cancelOrder(
                @HeaderMap Map<String, String> headers,
                @Body Order id);


    }


}
