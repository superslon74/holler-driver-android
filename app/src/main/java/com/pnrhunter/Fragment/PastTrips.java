package com.pnrhunter.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.pnrhunter.HollerApplication;
import com.pnrhunter.mvp.details.DetailsView;
import com.pnrhunter.mvp.welcome.WelcomeView;
import com.pnrhunter.Helper.ConnectionHelper;
import com.pnrhunter.Helper.SharedHelper;
import com.pnrhunter.Helper.URLHelper;
import com.pnrhunter.Models.AccessDetails;
import com.pnrhunter.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.pnrhunter.HollerApplication.trimMessage;

public class PastTrips extends Fragment {
    Activity activity;
    Context context;
    Boolean isInternet;
    PostAdapter postAdapter;
    RecyclerView recyclerView;
    RelativeLayout errorLayout;
    ConnectionHelper helper;
    View rootView;

    ImageView backImg;
    LinearLayout toolbar;


    public PastTrips() {
        // Required empty public constructor
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_past_trips, container, false);
        if (activity == null) {
            activity = getActivity();
        }
        if (context == null) {
            context = getContext();
        }
        findViewByIdAndInitialize();
        if (helper.isConnectingToInternet()) {
            getHistoryList();
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


    public void getHistoryList() {


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(AccessDetails.serviceurl + URLHelper.GET_HISTORY_API, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                if (response != null) {
                    postAdapter = new PostAdapter(response);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(activity) {
                        @Override
                        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                            return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                        }
                    });
                    if (postAdapter != null && postAdapter.getItemCount() > 0) {
                        errorLayout.setVisibility(View.GONE);
                        recyclerView.setAdapter(postAdapter);
                    } else {
                        errorLayout.setVisibility(View.VISIBLE);
                    }

                } else {
                    errorLayout.setVisibility(View.VISIBLE);
                }



            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getHistoryList();
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

        HollerApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }


    private class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
        JSONArray jsonArray;

        public PostAdapter(JSONArray array) {
            this.jsonArray = array;
        }

        public void append(JSONArray array) {
            try {
                for (int i = 0; i < array.length(); i++) {
                    this.jsonArray.put(array.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public PostAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_list_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PostAdapter.MyViewHolder holder, int position) {
            String imgUrl = jsonArray.optJSONObject(position).optString("static_map");
            Glide.with(activity)
                    .load(imgUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.tripImg);
            int displayAmount = 0;
            if (jsonArray.optJSONObject(position).optJSONObject("payment") != null) {
                int basePrice = jsonArray.optJSONObject(position).optJSONObject("payment").optInt("fixed");
                int distancePrice = jsonArray.optJSONObject(position).optJSONObject("payment").optInt("distance");
                int taxPrice = jsonArray.optJSONObject(position).optJSONObject("payment").optInt("tax");
                displayAmount = basePrice + distancePrice + taxPrice;

                holder.tripAmount.setText(SharedHelper.getKey(context, "currency") + "" + displayAmount);
            } else {
                holder.tripAmount.setText(SharedHelper.getKey(context, "currency") + ""+displayAmount);
            }

            holder.tripId.setText(jsonArray.optJSONObject(position).optString("booking_id"));

            try {
                if (!jsonArray.optJSONObject(position).optString("assigned_at", "").isEmpty()) {
                    String form = jsonArray.optJSONObject(position).optString("assigned_at");
                    try {
                        holder.tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + " at " + getTime(form));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tripDate, tripTime, tripAmount, tripId;
            ImageView tripImg;

            public MyViewHolder(View itemView) {
                super(itemView);
                tripDate = (TextView) itemView.findViewById(R.id.tripDate);
                tripTime = (TextView) itemView.findViewById(R.id.tripTime);
                tripAmount = (TextView) itemView.findViewById(R.id.tripAmount);
                tripImg = (ImageView) itemView.findViewById(R.id.tripImg);
                tripId = (TextView) itemView.findViewById(R.id.tripid);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (helper.isConnectingToInternet()){
                            Intent intent = new Intent(activity, DetailsView.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            String id = jsonArray.optJSONObject(getAdapterPosition()).optString("id");
                            intent.putExtra(DetailsView.ARG_ID, id);
                            intent.putExtra(DetailsView.ARG_TYPE, DetailsView.TYPE_PAST_TRIPS);
                            activity.startActivity(intent);
                        }else {
                            Toast.makeText(context,"Oops, Connect your internet",Toast.LENGTH_LONG).show();
                        }


                    }
                });
            }
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
    }

    public void findViewByIdAndInitialize() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        errorLayout = (RelativeLayout) rootView.findViewById(R.id.errorLayout);
        errorLayout.setVisibility(View.GONE);
        helper = new ConnectionHelper(activity);
        isInternet = helper.isConnectingToInternet();
        toolbar = (LinearLayout) rootView.findViewById(R.id.lnrTitle);
        backImg = (ImageView) rootView.findViewById(R.id.backArrow);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }catch (Exception e){
            try{
                Toast.makeText(context,""+toastString,Toast.LENGTH_SHORT).show();
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", getString(R.string.False));
        Intent mainIntent;
            mainIntent = new Intent(activity, WelcomeView.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }
}
