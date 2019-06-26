package com.pnrhunter.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.pnrhunter.mvp.details.DetailsView;
import com.pnrhunter.mvp.welcome.WelcomeView;
import com.pnrhunter.Helper.ConnectionHelper;
import com.pnrhunter.Helper.SharedHelper;
import com.pnrhunter.Models.AccessDetails;
import com.pnrhunter.R;
import com.pnrhunter.server.OrderServerApi;
import com.pnrhunter.server.OrderServerApi.ApiCreator;
import com.pnrhunter.server.OrderServerApi.Order;
import com.pnrhunter.server.OrderServerApi.CallbackErrorHandler;
import com.pnrhunter.server.OrderServerApi.CancelOrderCallbackHandler;
import com.pnrhunter.utils.Notificator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;


public class OnGoingTrips extends Fragment {

    Activity activity;
    Context context;
    View rootView;
    UpcomingsAdapter upcomingsAdapter;
    RecyclerView recyclerView;
    RelativeLayout errorLayout;
    ConnectionHelper helper;

    LinearLayout toolbar;
    ImageView backImg;

    private OrderServerApi serverApiClient;

    public OnGoingTrips() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initializing retrofit client
        serverApiClient = ApiCreator.createInstance();
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


        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));

        serverApiClient
                .getScheduledOrders(headers)
                .enqueue(new CallbackErrorHandler<List<Order>>(getActivity()) {
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

                    @Override
                    public void onTimeoutRequest() {
                        getUpcomingList();
                    }

                    @Override
                    public void onFailure(Call<List<Order>> call, Throwable t) {
                        errorLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        super.onFailure(call,t);
                    }

                    @Override
                    public void onFinishHandling() {

                    }
                });


    }

    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(activity, WelcomeView.class);
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
                scheduleNotification(context,o.id,date);
            }catch (Exception e){
                Log.e("AZAZA", "Can't schedule notification..");
                e.printStackTrace();
            }
        }


    }


    private void scheduleNotification(Context context,
                                     String orderId,
                                     Date scheduledDate)
    {
//        TODO: move it to begin screen

        Bundle arguments = new Bundle();
        arguments.putString(DetailsView.ARG_ID, orderId);
        arguments.putString(DetailsView.ARG_TYPE, DetailsView.TYPE_UPCOMING_TRIPS);

        int notificationId = (int)(Long.parseLong(orderId));


        new Notificator(context)
                .buildPendingIntent(DetailsView.class, arguments, notificationId)
                .buildNotification(Notificator.generateTextBasedOnWarningMode())
                .scheduleNotification(notificationId, scheduledDate);

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
                            Intent intent = new Intent(getActivity(), DetailsView.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(DetailsView.ARG_ID, orders.get(getAdapterPosition()).id);
                            intent.putExtra(DetailsView.ARG_TYPE, DetailsView.TYPE_UPCOMING_TRIPS);
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


        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));


        serverApiClient
                .cancelOrder(headers, order)
                .enqueue(new CancelOrderCallbackHandler<JsonObject>(getActivity(), order) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        super.onSuccessfulResponse(response);
                        getUpcomingList();
                    }
                    @Override
                    public void onTimeoutRequest() {
                        getUpcomingList();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        errorLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        super.onFailure(call,t);
                    }

                    @Override
                    public void onFinishHandling() {
                    }
                });
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






}
