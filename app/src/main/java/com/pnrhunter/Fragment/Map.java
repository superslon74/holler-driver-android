package com.pnrhunter.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.pnrhunter.activity.DocumentsActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.pnrhunter.server.OrderServerApi;
import com.pnrhunter.utils.CustomActivity;
import com.pnrhunter.utils.GPSTracker;
import com.squareup.picasso.Picasso;
import com.pnrhunter.activity.MainActivity;
import com.pnrhunter.activity.Offline;
import com.pnrhunter.activity.ShowProfile;
import com.pnrhunter.mvp.welcome.WelcomeView;
import com.pnrhunter.HollerApplication;
import com.pnrhunter.Helper.ConnectionHelper;
import com.pnrhunter.Helper.DataParser;
import com.pnrhunter.Helper.SharedHelper;
import com.pnrhunter.Helper.URLHelper;
import com.pnrhunter.Helper.User;
import com.pnrhunter.Models.AccessDetails;
import com.pnrhunter.Retrofit.ApiInterface;
import com.pnrhunter.Retrofit.RetrofitClient;
//import com.holler.app.Services.FloatingViewService;
import com.pnrhunter.R;
import com.pnrhunter.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import me.philio.pinentry.PinEntryView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


@Deprecated
public class Map
        extends Fragment
        implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        GoogleMap.OnCameraMoveListener {

    public static final int REQUEST_LOCATION = 1450;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    public static SupportMapFragment mapFragment = null;
    public static String TAG = "Map";
    public Handler ha;
    public String myLat = "";
    public String myLng = "";
    String CurrentStatus = " ";
    boolean isOTPRequired = false;
    String PreviousStatus = " ";
    String request_id = " ";
    OrderServerApi.Order incomingOrder = new OrderServerApi.Order();
    int method;
    Activity activity;
    Context context;
    CountDownTimer countDownTimer;
    int value = 0;
    AlertDialog cancelDialog;
    android.app.AlertDialog cancelReasonDialog;
    android.app.AlertDialog otpDialog;
    Marker currentMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    ParserTask parserTask;

    @Deprecated
    ImageView imgCurrentLoc;
    boolean normalPlay = false;
    String s_address = "", d_address = "";
    //content layout 01
    TextView txt01Pickup;
    TextView txt01Timer;
    ImageView img01User;
    TextView txt01UserName;
    TextView txtSchedule;
    RatingBar rat01UserRating;
    //content layer 02
    ImageView img02User;
    TextView txt02UserName;
    RatingBar rat02UserRating;
    TextView txt02ScheduledTime;
    TextView txt02From;
    TextView txt02To;
    TextView topSrcDestTxtLbl;
    //content layer 03
    ImageView img03User;
    ImageView img04User;
    TextView txt03UserName;
    TextView txt04UserName;
    RatingBar rat03UserRating;
    RatingBar rat04UserRating;
    ImageView img03Call;
    ImageView img03Status1;
    ImageView img03Status2;
    ImageView img03Status3;
    //content layer 04
    TextView txt04InvoiceId;
    TextView txt04BasePrice;
    TextView txt04Distance;
    TextView txt04Tax;
    TextView txt04Total;
    TextView txt04AmountToPaid;
    TextView txt04PaymentMode;
    TextView txt04Commision;
    TextView lblProviderName;
    ImageView paymentTypeImg;
    //content layer 05
    ImageView img05User;
    RatingBar rat05UserRating;
    EditText edt05Comment;
    //Button layer 01
    Button btn_01_status, btn_confirm_payment, btn_rate_submit;
    Button btn_go_offline;
    Button btn_send_order;
    LinearLayout lnrGoOffline;
    //Button layer 02
    Button btn_02_accept;
    Button btn_02_reject;
    Button btn_cancel_ride;
    //map layout
    LinearLayout ll_01_mapLayer;
    //content layout
    LinearLayout ll_01_contentLayer_accept_or_reject_now;
    LinearLayout ll_02_contentLayer_accept_or_reject_later;
    LinearLayout ll_03_contentLayer_service_flow;
    LinearLayout ll_04_contentLayer_payment;
    LinearLayout ll_05_contentLayer_feedback;
    LinearLayout errorLayout;
    //menu icon
    ImageView menuIcon;
    int NAV_DRAWER = 0;
    DrawerLayout drawer;
    Utilities utils = new Utilities();
    MediaPlayer mPlayer;
    ImageView imgNavigationToSource;
    String crt_lat = "", crt_lng = "";
    boolean timerCompleted = false;
    TextView destination;
    ConnectionHelper helper;
    LinearLayout destinationLayer;
    View view;
    boolean doubleBackToExitPressedOnce = false;
    //Animation
    Animation slide_down, slide_up;
    //Distance calculation
    TextView lblDistanceTravelled;
    boolean scheduleTrip = false;
    boolean showBatteryAlert = true;
    private String token;
    //map variable
    private GoogleMap mMap;
    private double srcLatitude = 0;
    private double srcLongitude = 0;
    private double destLatitude = 0;
    private double destLongitude = 0;
    private LatLng sourceLatLng;
    private LatLng destLatLng;
    private LatLng currentLatLng;
    private String bookingId;
    private String address;
    private User user = new User();
    private ImageView sos;
    //Button layout
    private Object previous_request_id = " ";
    private String count;
    private JSONArray statusResponses;
    private String feedBackRating;
    private String feedBackComment;
    private String strOTP = "";

    @Inject public RetrofitModule.ServerAPI serverAPI;
    @Inject public Context appContext;
    private GPSTracker.GPSTrackerBinder gpsService;
    private boolean isUserExists = false;

    public Map() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        HollerApplication.getInstance().component().inject(this);
        Intent gpsTrackerBinding = new Intent(appContext, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Map.this.gpsService = (GPSTracker.GPSTrackerBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Map.this.gpsService = null;
            }
        };
        appContext.bindService(gpsTrackerBinding,gpsTrackerConnection,Context.BIND_AUTO_CREATE);

        super.onCreate(savedInstanceState);


        if (activity == null) {
            activity = getActivity();
        }
        if (context == null) {
            context = getContext();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        }
        if (activity == null) {
            activity = getActivity();
        }
        if (context == null) {
            context = getContext();
        }
        findViewById(view);
        token = SharedHelper.getKey(context, "access_token");
        helper = new ConnectionHelper(context);

        //permission to access location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
        } else {
            setUpMapIfNeeded();
            MapsInitializer.initialize(activity);
        }

        ha = new Handler();

        btn_01_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentStatus.equalsIgnoreCase("PICKEDUP") && isOTPRequired) {
                    showOTPDialog();
                } else {
                    update(CurrentStatus, request_id);
                }
            }
        });
        btn_confirm_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });

        btn_rate_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });

        btn_send_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createAndSendOrder();

//                final Intent intent = new Intent(getActivity(), FloatingViewService.class);
//                getActivity().startService(intent);
//
//                CreatingOrderTask t = new CreatingOrderTask();
//                t.execute("HI");
            }
        });

        btn_go_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(CurrentStatus, request_id);
            }
        });

        errorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imgCurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double crtLat, crtLng;
                if (!crt_lat.equalsIgnoreCase("") && !crt_lng.equalsIgnoreCase("")) {
                    crtLat = Double.parseDouble(crt_lat);
                    crtLng = Double.parseDouble(crt_lng);
                    if (crtLat != null && crtLng != null) {
                        LatLng loc = new LatLng(crtLat, crtLng);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(16).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            }
        });

        btn_02_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer = null;
                }
                handleIncomingRequest("Accept", request_id);
            }
        });


        btn_02_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer = null;
                }
                handleIncomingRequest("Reject", request_id);
            }
        });

        btn_cancel_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (NAV_DRAWER == 0) {
                        drawer.openDrawer(GravityCompat.START);
                    } else {
                        NAV_DRAWER = 0;
                        drawer.closeDrawers();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        img03Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = SharedHelper.getKey(context, "provider_mobile_no");
                if (mobile != null && !mobile.equalsIgnoreCase("null") && mobile.length() > 0) {
                } else {
                    displayMessage(context.getResources().getString(R.string.user_no_mobile));
                }


            }
        });

        imgNavigationToSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_01_status.getText().toString().equalsIgnoreCase("ARRIVED")) {
                    Uri naviUri = Uri.parse("http://maps.google.com/maps?f=d&hl=en&daddr=" + s_address);
                    Intent intent = new Intent(Intent.ACTION_VIEW, naviUri);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                } else {
                    Uri naviUri2 = Uri.parse("http://maps.google.com/maps?f=d&hl=en&saddr=" + s_address + "&daddr=" + d_address);
                    Intent intent = new Intent(Intent.ACTION_VIEW, naviUri2);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }
                activity.finish();

                //Check if the application has draw over other apps permission or not?
                //This permission is by default available for API<23. But for API > 23
                //you have to ask for the permission in runtime.
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
//                    //If the draw over permission is not available open the settings screen
//                    //to grant the permission.
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            Uri.parse("package:" + context.getPackageName()));
//                    startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
//                } else {
//                    initializeView();
//                }
                showCustomFloatingView(context, true);
            }
        });
        statusCheck();
        return view;
    }

    private void createAndSendOrder(){
        OrderServerApi serverApiClient = OrderServerApi.ApiCreator.createInstance();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(getActivity(), "access_token"));

        OrderServerApi.Order order = new OrderServerApi.Order();
        order.startLatitude = crt_lat;
        order.startLongitude = crt_lng;

        serverApiClient
                .createOrder(headers,order)
                .enqueue(new OrderServerApi.CallbackErrorHandler<OrderServerApi.CreteOrderResponse>(getActivity()) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<OrderServerApi.CreteOrderResponse> response) {
                        Log.d("AZAZA","order successfully created");
                        Toast.makeText(getActivity(),"Location sent",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onUnsuccessfulResponse(retrofit2.Response<OrderServerApi.CreteOrderResponse> response) {
                        super.onUnsuccessfulResponse(response);
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                    }
                });

    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLoc();
        }
    }

    private void enableLoc() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        mGoogleApiClient.connect();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                        utils.print("Location error", "Location error " + connectionResult.getErrorCode());
                    }
                })
                .build();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(false);


        PendingResult<LocationSettingsResult> result =
                LocationServices
                        .SettingsApi
                        .checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_LOCATION);

                        } catch (NullPointerException | IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            try {
                                status.startResolutionForResult(activity, REQUEST_LOCATION);
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        }
                        break;
                }
            }
        });
//	        }

    }

    private void findViewById(View view) {
        //Menu Icon
        menuIcon = (ImageView) view.findViewById(R.id.menuIcon);
        imgCurrentLoc = (ImageView) view.findViewById(R.id.imgCurrentLoc);
        drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);

        //map layer
        ll_01_mapLayer = (LinearLayout) view.findViewById(R.id.ll_01_mapLayer);

        //Button layer 01
        btn_01_status = (Button) view.findViewById(R.id.btn_01_status);
        btn_rate_submit = (Button) view.findViewById(R.id.btn_rate_submit);
        btn_confirm_payment = (Button) view.findViewById(R.id.btn_confirm_payment);

        //Button layer 02
        btn_02_accept = (Button) view.findViewById(R.id.btn_02_accept);
        btn_02_reject = (Button) view.findViewById(R.id.btn_02_reject);
        btn_cancel_ride = (Button) view.findViewById(R.id.btn_cancel_ride);
        btn_go_offline = (Button) view.findViewById(R.id.btn_go_offline);
        btn_send_order = (Button) view.findViewById(R.id.btn_send_order);
//        Button btn_tap_when_arrived, btn_tap_when_pickedup,btn_tap_when_dropped,  btn_tap_when_paid, btn_rate_user
        //content layer
        ll_01_contentLayer_accept_or_reject_now = (LinearLayout) view.findViewById(R.id.ll_01_contentLayer_accept_or_reject_now);
        ll_02_contentLayer_accept_or_reject_later = (LinearLayout) view.findViewById(R.id.ll_02_contentLayer_accept_or_reject_later);
        ll_03_contentLayer_service_flow = (LinearLayout) view.findViewById(R.id.ll_03_contentLayer_service_flow);
        ll_04_contentLayer_payment = (LinearLayout) view.findViewById(R.id.ll_04_contentLayer_payment);
        ll_05_contentLayer_feedback = (LinearLayout) view.findViewById(R.id.ll_05_contentLayer_feedback);
        lnrGoOffline = (LinearLayout) view.findViewById(R.id.lnrGoOffline);
        imgNavigationToSource = (ImageView) view.findViewById(R.id.imgNavigationToSource);

        //content layout 01
        txt01Pickup = (TextView) view.findViewById(R.id.txtPickup);
        txt01Timer = (TextView) view.findViewById(R.id.txt01Timer);
        img01User = (ImageView) view.findViewById(R.id.img01User);
        txt01UserName = (TextView) view.findViewById(R.id.txt01UserName);
        txtSchedule = (TextView) view.findViewById(R.id.txtSchedule);
        rat01UserRating = (RatingBar) view.findViewById(R.id.rat01UserRating);
        sos = (ImageView) view.findViewById(R.id.sos);
        LayerDrawable drawable = (LayerDrawable) rat01UserRating.getProgressDrawable();
        drawable.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        drawable.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);

        //content layer 02
        img02User = (ImageView) view.findViewById(R.id.img02User);
        txt02UserName = (TextView) view.findViewById(R.id.txt02UserName);
        rat02UserRating = (RatingBar) view.findViewById(R.id.rat02UserRating);
        LayerDrawable stars02 = (LayerDrawable) rat02UserRating.getProgressDrawable();
        stars02.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        txt02ScheduledTime = (TextView) view.findViewById(R.id.txt02ScheduledTime);
        lblDistanceTravelled = (TextView) view.findViewById(R.id.lblDistanceTravelled);
        txt02From = (TextView) view.findViewById(R.id.txt02From);
        txt02To = (TextView) view.findViewById(R.id.txt02To);

        //content layer 03
        img03User = (ImageView) view.findViewById(R.id.img03User);
        img04User = (ImageView) view.findViewById(R.id.img04User);
        txt03UserName = (TextView) view.findViewById(R.id.txt03UserName);
        txt04UserName = (TextView) view.findViewById(R.id.txt04UserName);
        rat03UserRating = (RatingBar) view.findViewById(R.id.rat03UserRating);
        rat04UserRating = (RatingBar) view.findViewById(R.id.rat04UserRating);
        LayerDrawable drawable_02 = (LayerDrawable) rat03UserRating.getProgressDrawable();
        drawable_02.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        drawable_02.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        drawable_02.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        img03Call = (ImageView) view.findViewById(R.id.img03Call);
        img03Status1 = (ImageView) view.findViewById(R.id.img03Status1);
        img03Status2 = (ImageView) view.findViewById(R.id.img03Status2);
        img03Status3 = (ImageView) view.findViewById(R.id.img03Status3);

        //content layer 04
        txt04InvoiceId = (TextView) view.findViewById(R.id.invoice_txt);
        txt04BasePrice = (TextView) view.findViewById(R.id.txt04BasePrice);
        txt04Distance = (TextView) view.findViewById(R.id.txt04Distance);
        txt04Tax = (TextView) view.findViewById(R.id.txt04Tax);
        txt04Total = (TextView) view.findViewById(R.id.txt04Total);
        txt04AmountToPaid = (TextView) view.findViewById(R.id.txt04AmountToPaid);
        txt04PaymentMode = (TextView) view.findViewById(R.id.txt04PaymentMode);
        txt04Commision = (TextView) view.findViewById(R.id.txt04Commision);
        destination = (TextView) view.findViewById(R.id.destination);
        lblProviderName = (TextView) view.findViewById(R.id.lblProviderName);
        paymentTypeImg = (ImageView) view.findViewById(R.id.paymentTypeImg);
        errorLayout = (LinearLayout) view.findViewById(R.id.lnrErrorLayout);
        destinationLayer = (LinearLayout) view.findViewById(R.id.destinationLayer);

        //content layer 05
        img05User = (ImageView) view.findViewById(R.id.img05User);
        rat05UserRating = (RatingBar) view.findViewById(R.id.rat05UserRating);

        LayerDrawable stars05 = (LayerDrawable) rat05UserRating.getProgressDrawable();
        stars05.getDrawable(0).setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        stars05.getDrawable(1).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        stars05.getDrawable(2).setColorFilter(Color.parseColor("#FFAB00"), PorterDuff.Mode.SRC_ATOP);
        edt05Comment = (EditText) view.findViewById(R.id.edt05Comment);

        topSrcDestTxtLbl = (TextView) view.findViewById(R.id.src_dest_txt);

        //Load animation
        slide_down = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);


        view.setFocusableInTouchMode(true);
        view.requestFocus();
//        view.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() != KeyEvent.ACTION_DOWN)
//                    return true;
//
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    if (doubleBackToExitPressedOnce) {
//                        getActivity().finish();
//                        return false;
//                    }
//
//                    doubleBackToExitPressedOnce = true;
//                    Toast.makeText(getActivity(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
//
//                    new Handler().postDelayed(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            doubleBackToExitPressedOnce = false;
//                        }
//                    }, 5000);
//                    return true;
//                }
//                return false;
//            }
//        });

        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSosDialog();
            }
        });

        destinationLayer.setOnClickListener(this);
        ll_01_contentLayer_accept_or_reject_now.setOnClickListener(this);
        ll_03_contentLayer_service_flow.setOnClickListener(this);
        ll_04_contentLayer_payment.setOnClickListener(this);
        ll_05_contentLayer_feedback.setOnClickListener(this);
        lnrGoOffline.setOnClickListener(this);
        errorLayout.setOnClickListener(this);

    }

    private void mapClear() {
        if (parserTask != null) {
            parserTask.cancel(true);
            parserTask = null;
        }

        if (!crt_lat.equalsIgnoreCase("") && !crt_lat.equalsIgnoreCase("")) {
            LatLng myLocation = new LatLng(Double.parseDouble(crt_lat), Double.parseDouble(crt_lng));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(16).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


        if (mMap != null) {
            mMap.clear();
        }
        srcLatitude = 0;
        srcLongitude = 0;
        destLatitude = 0;
        destLongitude = 0;
    }

    public void clearVisibility() {

        try {
            if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.VISIBLE) {
                ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_down);
            } else if (ll_02_contentLayer_accept_or_reject_later.getVisibility() == View.VISIBLE) {
                ll_02_contentLayer_accept_or_reject_later.startAnimation(slide_down);
            } else if (ll_03_contentLayer_service_flow.getVisibility() == View.VISIBLE) {
                //ll_03_contentLayer_service_flow.startAnimation(slide_down);
            } else if (ll_04_contentLayer_payment.getVisibility() == View.VISIBLE) {
                ll_04_contentLayer_payment.startAnimation(slide_down);
            } else if (ll_04_contentLayer_payment.getVisibility() == View.VISIBLE) {
                ll_04_contentLayer_payment.startAnimation(slide_down);
            } else if (ll_05_contentLayer_feedback.getVisibility() == View.VISIBLE) {
                ll_05_contentLayer_feedback.startAnimation(slide_down);
            }

            ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
            ll_02_contentLayer_accept_or_reject_later.setVisibility(View.GONE);
            ll_03_contentLayer_service_flow.setVisibility(View.GONE);
            ll_04_contentLayer_payment.setVisibility(View.GONE);
            ll_05_contentLayer_feedback.setVisibility(View.GONE);
            lnrGoOffline.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            FragmentManager fm = getChildFragmentManager();
            mapFragment = ((SupportMapFragment) fm.findFragmentById(R.id.provider_map));
            mapFragment.getMapAsync(this);
        }
        if (mMap != null) {
            setupMap();
        }
    }

    private void setSourceLocationOnMap(LatLng latLng) {
   /*     if (mMap != null){
            mMap.clear();
            if (latLng != null){
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16).build();
                MarkerOptions options = new MarkerOptions().position(latLng).anchor(0.5f, 0.5f);
                options.position(latLng).isDraggable();
                mMap.addMarker(options);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }*/
    }


    private void setPickupLocationOnMap() {
        try {
            if (mMap != null) {
                mMap.clear();
            }
//            sourceLatLng = currentLatLng;
            sourceLatLng = new LatLng(Double.parseDouble(SharedHelper.getKey(context, "current_lat")), Double.parseDouble(SharedHelper.getKey(context, "current_lng")));
            destLatLng = new LatLng(srcLatitude, srcLongitude);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(destLatLng).zoom(16).build();
            MarkerOptions options = new MarkerOptions();
            options.position(destLatLng).isDraggable();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            if (sourceLatLng != null && destLatLng != null) {
                String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude, destLatLng.latitude, destLatLng.longitude);
                FetchUrl fetchUrl = new FetchUrl();
                fetchUrl.execute(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDestinationLocationOnMap() {
//        sourceLatLng = currentLatLng;
        if (SharedHelper.getKey(context, "current_lat").length() > 0 && SharedHelper.getKey(context, "current_lng").length() > 0) {
            sourceLatLng = new LatLng(Double.parseDouble(SharedHelper.getKey(context, "current_lat")), Double.parseDouble(SharedHelper.getKey(context, "current_lng")));
            destLatLng = new LatLng(destLatitude, destLongitude);
            try {
                if (sourceLatLng != null && destLatLng != null) {
                    String url = getUrl(sourceLatLng.latitude, sourceLatLng.longitude, destLatLng.latitude, destLatLng.longitude);
                    FetchUrl fetchUrl = new FetchUrl();
                    fetchUrl.execute(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @SuppressWarnings("MissingPermission")
    @Deprecated
    private void setupMap() {
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setOnCameraMoveListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            activity, R.raw.style_json));


        } catch (Resources.NotFoundException e) {
            Log.e("Map:Style", "Can't find style. Error: ", e);
        }
        mMap = googleMap;
        // do other tasks here
        setupMap();


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
//                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
//            mMap.setMyLocationEnabled(true);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's data! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(context)
                        .setIcon(AccessDetails.site_icon)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
            }
        }
    }

    @Deprecated
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && mGoogleApiClient != null
                && mGoogleApiClient.isConnected()) {
            LocationServices
                    .FusedLocationApi
                    .requestLocationUpdates(
                            mGoogleApiClient,
                            mLocationRequest,
                            (com.google.android.gms.location.LocationListener) this);
        }
    }

    @Deprecated
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Deprecated
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null) {
            if (currentMarker != null) {
                currentMarker.remove();
            }

            MarkerOptions markerOptions1 = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location));
            currentMarker = mMap.addMarker(markerOptions1);

            if (value == 0) {
                myLat = String.valueOf(location.getLatitude());
                myLng = String.valueOf(location.getLongitude());

                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(16).build();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(myLocation);
//                    Marker marker = mMap.addMarker(markerOptions);
//                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                    mMap.setPadding(0, 0, 0, 135);
//                    mMap.getUiSettings().setZoomControlsEnabled(true);
//                    mMap.getUiSettings().setMyLocationButtonEnabled(true);

                checkStatus();

                //check status every 3 sec
                ha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //call function
                        checkStatus();
                        ha.postDelayed(this, 3000);
                    }
                }, 3000);

                value++;

            }

            crt_lat = String.valueOf(location.getLatitude());
            crt_lng = String.valueOf(location.getLongitude());
            currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            SharedHelper.putKey(context, "current_lat", "" + crt_lat);
            SharedHelper.putKey(context, "current_lng", "" + crt_lng);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Deprecated
    @Override
    public void onProviderEnabled(String provider) {

    }

    @Deprecated
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Deprecated
    @Override
    public void onClick(View v) {

    }

    @Override
    @Deprecated
    public void onCameraMove() {
        utils.print("Current marker", "Zoom Level " + mMap.getCameraPosition().zoom);
        if (currentMarker != null) {
            if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(currentMarker.getPosition())) {
                utils.print("Current marker", "Current Marker is not visible");
                if (imgCurrentLoc.getVisibility() == View.GONE) {
                    imgCurrentLoc.setVisibility(View.VISIBLE);
                }
            } else {
                utils.print("Current marker", "Current Marker is visible");
                if (imgCurrentLoc.getVisibility() == View.VISIBLE) {
                    imgCurrentLoc.setVisibility(View.GONE);
                }
                if (mMap.getCameraPosition().zoom < 16.0f) {
                    if (imgCurrentLoc.getVisibility() == View.GONE) {
                        imgCurrentLoc.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getUrl(double source_latitude, double source_longitude, double dest_latitude, double dest_longitude) {

        // Origin of route
        String str_origin = "origin=" + source_latitude + "," + source_longitude;

        // Destination of route
        String str_dest = "destination=" + dest_latitude + "," + dest_longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    private void checkStatus() {
        try {
            /* Battery status check */
            if (Utilities.getBatteryLevel(context)) {
                if (showBatteryAlert) {
//                    Utilities.notify(context, activity);
                    showBatteryAlert = false;
                }
            }


            if (helper.isConnectingToInternet()) {

                if (SharedHelper.getKey(context, "is_track").equalsIgnoreCase("YES")) {
                    if (CurrentStatus.equalsIgnoreCase("DROPPED") || CurrentStatus.equalsIgnoreCase("COMPLETED")) {
                        updateLiveTracking(crt_lat, crt_lng);
                    }
                }

                String url = AccessDetails.serviceurl + "/api/provider/trip?latitude=" + crt_lat + "&longitude=" + crt_lng;

                utils.print("Destination Current Lat", "" + crt_lat);
                utils.print("Destination Current Lng", "" + crt_lng);

                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("CheckStatus", "" + response.toString());
                        try {
                            if (response.optJSONArray("requests").length() > 0) {
                                JSONObject jsonObject = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optJSONObject("user");
                                address = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("s_address");
                                SharedHelper.putKey(context, "is_track", response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("is_track"));

                                user.setFirstName(jsonObject.optString("first_name"));
                                user.setLastName(jsonObject.optString("last_name"));
                                user.setEmail(jsonObject.optString("email"));
                                if (jsonObject.optString("picture").startsWith("http"))
                                    user.setImg(jsonObject.optString("picture"));
                                else
                                    user.setImg(AccessDetails.serviceurl + "/storage/" + jsonObject.optString("picture"));
                                user.setRating(jsonObject.optString("rating"));
                                user.setMobile(jsonObject.optString("mobile"));
                                bookingId = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request").optString("booking_id");
                                isUserExists = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e){
                            txt01UserName.setText("Holler");
                            Map.this.user = new User();
                            isUserExists = false;
                            e.printStackTrace();
                        }

                        if (response.optString("account_status").equals("new") || response.optString("account_status").equals("onboarding")) {
                            ha.removeMessages(0);
//                            Intent intent = new Intent(activity, WaitingForApproval.class);

                            Intent uploadDocuments = new Intent(activity, DocumentsActivity.class);
                            uploadDocuments.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                            activity.startActivity(uploadDocuments);
                            activity.finish();
                        } else {

                            if (response.optString("service_status").equals("offline")) {
                                ha.removeMessages(0);
//                    Intent intent = new Intent(activity, Offline.class);
//                    activity.startActivity(intent);
                                goOffline();
                                gpsService.stopTracking();
                            } else {
                                if (response.optJSONArray("requests") != null && response.optJSONArray("requests").length() > 0) {
                                    JSONObject statusResponse = null;
                                    try {
                                        statusResponses = response.optJSONArray("requests");
                                        statusResponse = response.optJSONArray("requests").getJSONObject(0).optJSONObject("request");
                                        s_address = statusResponse.optString("s_address");
                                        d_address = statusResponse.optString("d_address");
                                        strOTP = statusResponse.optString("otp");
                                        request_id = response.optJSONArray("requests").getJSONObject(0).optString("request_id");
                                        incomingOrder.id = request_id;
                                        incomingOrder.scheduledDate = response
                                                .optJSONArray("requests")
                                                .getJSONObject(0)
                                                .getJSONObject("request")
                                                .optString("schedule_at", "");
                                        String str = incomingOrder.scheduledDate;
                                        String str1 = request_id;

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    if ((statusResponse != null) && (request_id != null)) {
                                        if ((!previous_request_id.equals(request_id) || previous_request_id.equals(" ")) && mMap != null) {
                                            previous_request_id = request_id;
                                            srcLatitude = Double.valueOf(statusResponse.optString("s_latitude"));
                                            srcLongitude = Double.valueOf(statusResponse.optString("s_longitude"));
                                            destLatitude = Double.valueOf(statusResponse.optString("d_latitude"));
                                            destLongitude = Double.valueOf(statusResponse.optString("d_longitude"));
                                            //noinspection deprecation
                                            setSourceLocationOnMap(currentLatLng);
                                            setPickupLocationOnMap();
                                            sos.setVisibility(View.GONE);
                                        }
                                        utils.print("Cur_and_New_status :", "" + CurrentStatus + "," + statusResponse.optString("status"));
                                        if (!PreviousStatus.equals(statusResponse.optString("status"))) {
                                            PreviousStatus = statusResponse.optString("status");
                                            clearVisibility();
                                            utils.print("responseObj(" + request_id + ")", statusResponse.toString());
                                            utils.print("Cur_and_New_status :", "" + CurrentStatus + "," + statusResponse.optString("status"));
                                            if (!statusResponse.optString("status").equals("SEARCHING")) {
                                                timerCompleted = false;
                                                if (mPlayer != null && mPlayer.isPlaying()) {
                                                    mPlayer.stop();
                                                    mPlayer = null;
                                                    countDownTimer.cancel();
                                                }
                                            }
                                            if (statusResponse.optString("status").equals("SEARCHING")) {
                                                scheduleTrip = false;
                                                if (!timerCompleted) {
                                                    setValuesTo_ll_01_contentLayer_accept_or_reject_now(statusResponses);
                                                    if (ll_01_contentLayer_accept_or_reject_now.getVisibility() == View.GONE) {
                                                        ll_01_contentLayer_accept_or_reject_now.startAnimation(slide_up);
                                                    }
                                                    ll_01_contentLayer_accept_or_reject_now.setVisibility(View.VISIBLE);
                                                }
                                                CurrentStatus = "STARTED";
                                            } else if (statusResponse.optString("status").equals("STARTED")) {
                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                if (ll_03_contentLayer_service_flow.getVisibility() == View.GONE) {
                                                    //ll_03_contentLayer_service_flow.startAnimation(slide_up);
                                                }
                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_arrived));
                                                CurrentStatus = "ARRIVED";
                                                sos.setVisibility(View.GONE);
                                                if (srcLatitude == 0 && srcLongitude == 0 && destLatitude == 0 && destLongitude == 0) {
                                                    mapClear();
                                                    srcLatitude = Double.valueOf(statusResponse.optString("s_latitude"));
                                                    srcLongitude = Double.valueOf(statusResponse.optString("s_longitude"));
                                                    destLatitude = Double.valueOf(statusResponse.optString("d_latitude"));
                                                    destLongitude = Double.valueOf(statusResponse.optString("d_longitude"));
                                                    //noinspection deprecation
                                                    //
                                                    setSourceLocationOnMap(currentLatLng);
                                                    setPickupLocationOnMap();
                                                }
                                                img03Status1.setImageResource(R.drawable.arrived);
                                                img03Status2.setImageResource(R.drawable.pickup);
                                                sos.setVisibility(View.GONE);
                                                btn_cancel_ride.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.VISIBLE);
                                                String address = statusResponse.optString("s_address");
                                                if (address != null && !address.equalsIgnoreCase("null") && address.length() > 0)
                                                    destination.setText(address);
                                                else
                                                    destination.setText(getAddress(statusResponse.optString("s_latitude"),
                                                            statusResponse.optString("s_longitude")));
                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.pick_up));
                                            } else if (statusResponse.optString("status").equals("ARRIVED")) {
                                                String needOtp = statusResponse.optString("otp_required", "0");
                                                switch (needOtp){
                                                    case "0": isOTPRequired = false; break;
                                                    default: isOTPRequired = true; break;
                                                }
                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_pickedup));
                                                sos.setVisibility(View.GONE);
                                                img03Status1.setImageResource(R.drawable.arrived_select);
                                                img03Status2.setImageResource(R.drawable.pickup);
                                                CurrentStatus = "PICKEDUP";
                                                setSourceLocationOnMap(currentLatLng);
                                                setDestinationLocationOnMap();
                                                btn_cancel_ride.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.VISIBLE);
                                                String address = statusResponse.optString("d_address");
                                                if (address != null && !address.equalsIgnoreCase("null") && address.length() > 0)
                                                    destination.setText(address);
                                                else
                                                    destination.setText(getAddress(statusResponse.optString("d_latitude"),
                                                            statusResponse.optString("d_longitude")));
                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.drop_at));
                                            } else if (statusResponse.optString("status").equals("PICKEDUP")) {


                                                setValuesTo_ll_03_contentLayer_service_flow(statusResponses);
                                                ll_03_contentLayer_service_flow.setVisibility(View.VISIBLE);
                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_dropped));
                                                sos.setVisibility(View.VISIBLE);
                                                img03Status1.setImageResource(R.drawable.arrived_select);
                                                img03Status2.setImageResource(R.drawable.pickup_select);
                                                CurrentStatus = "DROPPED";
                                                destinationLayer.setVisibility(View.VISIBLE);
                                                btn_cancel_ride.setVisibility(View.GONE);
                                                String address = statusResponse.optString("d_address");
                                                if (address != null && !address.equalsIgnoreCase("null") && address.length() > 0)
                                                    destination.setText(address);
                                                else {
                                                    destination.setText(getAddress(statusResponse.optString("d_latitude"), statusResponse.optString("d_longitude")));
                                                }
                                                topSrcDestTxtLbl.setText(context.getResources().getString(R.string.drop_at));
                                                mapClear();

                                                srcLatitude = Double.valueOf(statusResponse.optString("s_latitude"));
                                                srcLongitude = Double.valueOf(statusResponse.optString("s_longitude"));
                                                destLatitude = Double.valueOf(statusResponse.optString("d_latitude"));
                                                destLongitude = Double.valueOf(statusResponse.optString("d_longitude"));

                                                setSourceLocationOnMap(currentLatLng);
                                                setDestinationLocationOnMap();
                                            } else if (statusResponse.optString("status").equals("DROPPED")
                                                    && statusResponse.optString("paid").equals("0")) {
                                                setValuesTo_ll_04_contentLayer_payment(statusResponses);
                                                if (ll_04_contentLayer_payment.getVisibility() == View.GONE) {
                                                    ll_04_contentLayer_payment.startAnimation(slide_up);
                                                }
                                                ll_04_contentLayer_payment.setVisibility(View.VISIBLE);
                                                img03Status1.setImageResource(R.drawable.arrived);
                                                img03Status2.setImageResource(R.drawable.pickup);
                                                btn_01_status.setText(context.getResources().getString(R.string.tap_when_paid));
                                                sos.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.GONE);
                                                CurrentStatus = "COMPLETED";
                                            } else if (statusResponse.optString("status").equals("DROPPED") && statusResponse.optString("paid").equals("1")) {
                                                setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                                                if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                                                    ll_05_contentLayer_feedback.startAnimation(slide_up);
                                                }
                                                ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                                                btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                                                sos.setVisibility(View.VISIBLE);
                                                destinationLayer.setVisibility(View.GONE);
                                                CurrentStatus = "RATE";
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                            } else if (statusResponse.optString("status").equals("COMPLETED")) {
                                                setValuesTo_ll_05_contentLayer_feedback(statusResponses);
                                                if (ll_05_contentLayer_feedback.getVisibility() == View.GONE) {
                                                    ll_05_contentLayer_feedback.startAnimation(slide_up);
                                                }
                                                edt05Comment.setText("");
                                                ll_05_contentLayer_feedback.setVisibility(View.VISIBLE);
                                                sos.setVisibility(View.GONE);
                                                destinationLayer.setVisibility(View.GONE);
                                                btn_01_status.setText(context.getResources().getString(R.string.rate_user));
                                                CurrentStatus = "RATE";
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                            } else if (statusResponse.optString("status").equals("SCHEDULED")) {
                                                if (mMap != null) {
                                                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                        return;
                                                    }
                                                    mMap.clear();
                                                }
                                                clearVisibility();
                                                CurrentStatus = "SCHEDULED";
                                                if (lnrGoOffline.getVisibility() == View.GONE) {
                                                    lnrGoOffline.startAnimation(slide_up);
                                                }
                                                lnrGoOffline.setVisibility(View.VISIBLE);
                                                utils.print("statusResponse", "null");
                                                destinationLayer.setVisibility(View.GONE);
//                                                if (isMyServiceRunning(LocationTracking.class)) {
//                                                    activity.stopService(service_intent);
//                                                }
//                                                LocationTracking.distance = 0.0f;
                                            }
                                        }
                                    } else {
                                        if (mMap != null) {
                                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                return;
                                            }
                                            timerCompleted = false;
                                            mMap.clear();
                                            if (mPlayer != null && mPlayer.isPlaying()) {
                                                mPlayer.stop();
                                                mPlayer = null;
                                                countDownTimer.cancel();
                                            }

                                        }
//                                        if (isMyServiceRunning(LocationTracking.class)) {
//                                            activity.stopService(service_intent);
//                                        }
//                                        LocationTracking.distance = 0.0f;

                                        clearVisibility();
                                        lnrGoOffline.setVisibility(View.VISIBLE);
                                        destinationLayer.setVisibility(View.GONE);
                                        CurrentStatus = "ONLINE";
                                        PreviousStatus = "NULL";
                                        utils.print("statusResponse", "null");
                                    }

                                } else {
                                    timerCompleted = false;
                                    if (cancelDialog != null) {
                                        if (cancelDialog.isShowing()) {
                                            cancelDialog.dismiss();
                                        }
                                    }

                                    if (PreviousStatus.equalsIgnoreCase("STARTED")) {
                                        Toast.makeText(context, context.getResources().getString(R.string.user_busy), Toast.LENGTH_SHORT).show();
                                    }

                                    if (PreviousStatus.equalsIgnoreCase("ARRIVED")) {
                                        Toast.makeText(context, context.getResources().getString(R.string.user_busy), Toast.LENGTH_SHORT).show();
                                    }

                                    if (cancelReasonDialog != null) {
                                        if (cancelReasonDialog.isShowing()) {
                                            cancelReasonDialog.dismiss();
                                        }
                                    }
                                    if (!PreviousStatus.equalsIgnoreCase("NULL")) {
                                        utils.print("data", "null");
                                        if (mMap != null) {
                                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                return;
                                            }
                                            mMap.clear();
                                        }
                                        if (mPlayer != null && mPlayer.isPlaying()) {
                                            mPlayer.stop();
                                            mPlayer = null;
                                            countDownTimer.cancel();
                                        }
                                        clearVisibility();
                                        lnrGoOffline.setVisibility(View.VISIBLE);
                                        destinationLayer.setVisibility(View.GONE);
                                        CurrentStatus = "ONLINE";
                                        PreviousStatus = "NULL";
                                        utils.print("statusResponse", "null");
                                    }
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        utils.print("Error", error.toString());
                        //errorHandler(error);
                        timerCompleted = false;
                        mapClear();
                        clearVisibility();
                        CurrentStatus = "ONLINE";
                        PreviousStatus = "NULL";
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            mPlayer.stop();
                            mPlayer = null;
                            countDownTimer.cancel();
                        }
//                        if (errorLayout.getVisibility() != View.VISIBLE) {
//                            errorLayout.setVisibility(View.VISIBLE);
//                            sos.setVisibility(View.GONE);
//                        }
                    }
                }) {
                    @Override
                    public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("X-Requested-With", "XMLHttpRequest");
                        headers.put("Authorization", "Bearer " + token);
                        return headers;
                    }
                };
                HollerApplication.getInstance().addToRequestQueue(jsonObjectRequest);
            } else {
                displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setValuesTo_ll_01_contentLayer_accept_or_reject_now(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (!status.getJSONObject(0).optString("time_left_to_respond").equals("")) {
                count = status.getJSONObject(0).getString("time_left_to_respond");
            } else {
                count = "0";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        countDownTimer = new CountDownTimer(Integer.parseInt(count) * 1000, 1000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                txt01Timer.setText("" + millisUntilFinished / 1000);
                if (mPlayer == null) {

                } else {
                    if (!mPlayer.isPlaying()) {
                        mPlayer.start();
                    }
                }
                timerCompleted = false;

            }

            public void onFinish() {
                txt01Timer.setText("0");
                mapClear();
                clearVisibility();
                if (mMap != null) {
                    mMap.clear();
                }
                if (mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer = null;
                }
                ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                CurrentStatus = "ONLINE";
                PreviousStatus = "NULL";
                lnrGoOffline.setVisibility(View.VISIBLE);
                destinationLayer.setVisibility(View.GONE);
                timerCompleted = true;
                handleIncomingRequest("Reject", request_id);
            }
        };

        countDownTimer.start();

        try {
            if (!statusResponse.optString("schedule_at").trim().equalsIgnoreCase("") && !statusResponse.optString("schedule_at").equalsIgnoreCase("null")) {
                txtSchedule.setVisibility(View.VISIBLE);
                String strSchedule = "";
                try {
                    strSchedule = getDate(statusResponse.optString("schedule_at")) + "th " + getMonth(statusResponse.optString("schedule_at"))
                            + " " + getYear(statusResponse.optString("schedule_at")) + " at " + getTime(statusResponse.optString("schedule_at"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                txtSchedule.setText("Scheduled at : " + strSchedule);
            } else {
                txtSchedule.setVisibility(View.GONE);
            }

            final JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                if (!user.optString("picture").equals("null")) {
                    if (user.optString("picture").startsWith("http"))
                        Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                    else
                        Picasso.with(context).load(AccessDetails.serviceurl + "/storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img01User);
                } else {
                    img01User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img01User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);

                        startActivity(intent);
                    }
                });
                txt01UserName.setText(user.optString("first_name") + " " + user.optString("last_name"));


                if (statusResponse.getJSONObject("user").getString("rating") != null) {
                    rat01UserRating.setRating(Float.valueOf(user.getString("rating")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        txt01Pickup.setText(address);
    }

    private void setValuesTo_ll_03_contentLayer_service_flow(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                if (!user.optString("mobile").equals("null")) {
                    SharedHelper.putKey(context, "provider_mobile_no", "" + user.optString("mobile"));
                } else {
                    SharedHelper.putKey(context, "provider_mobile_no", "");
                }

                if (!user.optString("picture").equals("null")) {
                    if (user.optString("picture").startsWith("http"))
                        Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                    else
                        Picasso.with(context).load(AccessDetails.serviceurl + "/storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img03User);
                } else {
                    img03User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img03User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);
                        startActivity(intent);
                    }
                });

                txt03UserName.setText(user.optString("first_name") + " " + user.optString("last_name"));
                if (statusResponse.getJSONObject("user").getString("rating") != null) {
                    rat03UserRating.setRating(Float.valueOf(user.getString("rating")));
                } else {
                    rat03UserRating.setRating(0);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setValuesTo_ll_04_contentLayer_payment(JSONArray status) {
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            txt04InvoiceId.setText(context.getResources().getString(R.string.invoice) + " " + bookingId);
            txt04BasePrice.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("fixed"));
            txt04Distance.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("distance"));
            txt04Tax.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("tax"));
            txt04Total.setText(SharedHelper.getKey(context, "currency") + ""
                    + statusResponse.getJSONObject("payment").optString("total"));
            txt04AmountToPaid.setText(SharedHelper.getKey(context, "currency") + ""
                    + statusResponse.getJSONObject("payment").optString("payable"));
            txt04PaymentMode.setText(statusResponse.getString("payment_mode"));
            txt04Commision.setText(SharedHelper.getKey(context, "currency") + "" + statusResponse.getJSONObject("payment").optString("commision"));
            if (statusResponse.getString("payment_mode").equals("CASH")) {
                paymentTypeImg.setImageResource(R.drawable.money_icon);
            } else {
                paymentTypeImg.setImageResource(R.drawable.visa_icon);
            }

            try {
                JSONObject user = statusResponse.getJSONObject("user");
                if (user != null) {
                    if (!user.optString("mobile").equals("null")) {
                        SharedHelper.putKey(context, "provider_mobile_no", "" + user.optString("mobile"));
                    } else {
                        SharedHelper.putKey(context, "provider_mobile_no", "");
                    }

                    if (!user.optString("picture").equals("null")) {
                        if (user.optString("picture").startsWith("http"))
                            Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img04User);
                        else
                            Picasso.with(context).load(AccessDetails.serviceurl + "/storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img04User);
                    } else {
                        img04User.setImageResource(R.drawable.ic_dummy_user);
                    }
                    final User userProfile = this.user;
                    img04User.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, ShowProfile.class);
                            intent.putExtra("user", userProfile);
                            startActivity(intent);
                        }
                    });

                    txt04UserName.setText(user.optString("first_name") + " " + user.optString("last_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setValuesTo_ll_05_contentLayer_feedback(JSONArray status) {
        rat05UserRating.setRating(1.0f);
        feedBackRating = "1";
        rat05UserRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                utils.print("rating", rating + "");
                if (rating < 1.0f) {
                    rat05UserRating.setRating(1.0f);
                    feedBackRating = "1";
                }
                feedBackRating = String.valueOf((int) rating);
            }
        });
        JSONObject statusResponse = new JSONObject();
        try {
            statusResponse = status.getJSONObject(0).getJSONObject("request");
            JSONObject user = statusResponse.getJSONObject("user");
            if (user != null) {
                lblProviderName.setText(context.getResources().getString(R.string.rate_your_trip) +
                        " " + user.optString("first_name") + " " + user.optString("last_name"));
                if (!user.optString("picture").equals("null")) {
                    if (user.optString("picture").startsWith("http"))
                        Picasso.with(context).load(user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img05User);
                    else
                        Picasso.with(context).load(AccessDetails.serviceurl + "/storage/" + user.getString("picture")).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(img05User);
                } else {
                    img05User.setImageResource(R.drawable.ic_dummy_user);
                }
                final User userProfile = this.user;
                img05User.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowProfile.class);
                        intent.putExtra("user", userProfile);
                        startActivity(intent);
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        feedBackComment = edt05Comment.getText().toString();
    }

    private void update(String newStatus, String id) {
        final String status;
        if("ARRIVED" .equals(newStatus) &&  !isUserExists){
            status = "COMPLETED";
            CurrentStatus = "COMPLETED";
        }else{
            status = newStatus;
        }
        Utilities.hideKeyboard(getActivity());
        if (status.equals("ONLINE")) {
            String authHeader = "Bearer " + token;
//            serverAPI
//                    .sendStatus(authHeader, RetrofitModule.ServerAPI.STATUS_OFFLINE)
//                    .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(getActivity()) {
//                        @Override
//                        public void onSuccessfulResponse(retrofit2.Response<JsonObject> data) {
//                            goOffline();
//                            gpsService.stopTracking();
//                        }
//
//                        @Override
//                        public void onFinishHandling() {
//                            super.onFinishHandling();
//                        }
//                    });

        } else {
            String url;
            JSONObject param = new JSONObject();
            if (status.equals("RATE")) {
                url = AccessDetails.serviceurl + "/api/provider/trip/" + id + "/rate";
                try {
                    param.put("rating", feedBackRating);
                    param.put("comment", edt05Comment.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                utils.print("Input", param.toString());
            } else {
                url = AccessDetails.serviceurl + "/api/provider/trip/" + id;
                try {
                    param.put("_method", "PATCH");
                    param.put("status", status);

                    if (SharedHelper.getKey(context, "is_track").equalsIgnoreCase("YES")) {
                        if (CurrentStatus.equalsIgnoreCase("COMPLETED")) {
                            param.put("address", getAddress(crt_lat, crt_lng));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, param, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response.optJSONObject("requests") != null) {
                        utils.print("request", response.optJSONObject("requests").toString());
                    }

                    if (status.equals("RATE")) {
                        clearVisibility();
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                        LatLng myLocation = new LatLng(Double.parseDouble(crt_lat), Double.parseDouble(crt_lng));
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(16).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mapClear();
                        if (mMap != null) {
                            mMap.clear();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    utils.print("Error", error.toString());
                    if (status.equals("RATE")) {
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                    }
                    //errorHandler(error);
                }
            }) {
                @Override
                public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            HollerApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        }
    }


    public void cancelRequest(final String id, final String reason) {


        OrderServerApi.Order order = new OrderServerApi.Order();
        order.id = id;
        order.cancelReason = reason;

        java.util.Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));

        OrderServerApi serverClientApi = OrderServerApi.ApiCreator.createInstance();
        serverClientApi
                .cancelOrder(headers, order)
                .enqueue(new OrderServerApi.CancelOrderCallbackHandler<JsonObject>(activity, order) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        super.onSuccessfulResponse(response);
                        Toast.makeText(context, "" + context.getResources().getString(R.string.request_cancel), Toast.LENGTH_SHORT).show();
                        mapClear();
                        clearVisibility();
                        lnrGoOffline.setVisibility(View.VISIBLE);
                        destinationLayer.setVisibility(View.GONE);
                        CurrentStatus = "ONLINE";
                        PreviousStatus = "NULL";
                    }

                    @Override
                    public void onFinishHandling() {
                    }

                    @Override
                    public void onTimeoutRequest() {
                        cancelRequest(id, reason);
                    }

                });
    }


    private void handleIncomingRequest(final String status, final String id) {
        if (!((Activity) context).isFinishing()) {
        }

        OrderServerApi.Order order = new OrderServerApi.Order();
        order.id = id;
        if (id == incomingOrder.id) {
            order.scheduledDate = incomingOrder.scheduledDate;
        }

        java.util.Map<String, String> headers = new HashMap<>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Authorization", "Bearer " + SharedHelper.getKey(context, "access_token"));

        OrderServerApi serverClientApi = OrderServerApi.ApiCreator.createInstance();

        Call<ResponseBody> serverCall;
        OrderServerApi.CallbackErrorHandler<ResponseBody> handler;

        switch (status) {
            case "Accept":
                serverCall = serverClientApi.acceptOrder(headers, order.id);
                handler = new OrderServerApi.AcceptOrderCallbackHandler<ResponseBody>(activity, order) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<ResponseBody> response) {
                        super.onSuccessfulResponse(response);
                        Toast.makeText(context, context.getResources().getString(R.string.request_accept), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                    }

                    @Override
                    public void onTimeoutRequest() {
                        super.onTimeoutRequest();
                        handleIncomingRequest("Reject", request_id);
                    }

                };
                break;
            default:
                serverCall = serverClientApi.rejectOrder(headers, order.id);
                handler = new OrderServerApi.CallbackErrorHandler<ResponseBody>(activity) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<ResponseBody> response) {
                        if (!timerCompleted) {
                            Toast.makeText(context, "" + context.getResources().getString(R.string.request_reject), Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                txt01Timer.setText("0");
                                mapClear();
                                clearVisibility();
                                if (mMap != null) {
                                    mMap.clear();
                                }
                                if (mPlayer != null && mPlayer.isPlaying()) {
                                    mPlayer.stop();
                                    mPlayer = null;
                                }
                                ll_01_contentLayer_accept_or_reject_now.setVisibility(View.GONE);
                                CurrentStatus = "ONLINE";
                                PreviousStatus = "NULL";
                                lnrGoOffline.setVisibility(View.VISIBLE);
                                destinationLayer.setVisibility(View.GONE);
                                timerCompleted = true;
//                                handleIncomingRequest("Reject", request_id);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();
                    }

                    @Override
                    public void onTimeoutRequest() {
                        super.onTimeoutRequest();
                        handleIncomingRequest("Reject", request_id);
                    }
                };
                break;
        }

        serverCall.enqueue(handler);

    }

    public void errorHandler(VolleyError error) {
        utils.print("Error", error.toString());
        String json = null;
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {

            try {
                JSONObject errorObj = new JSONObject(new String(response.data));
                utils.print("ErrorHandler", "" + errorObj.toString());
                if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                    try {
                        displayMessage(errorObj.optString("message"));
                    } catch (Exception e) {
                        displayMessage(context.getResources().getString(R.string.something_went_wrong));
                    }
                } else if (response.statusCode == 401) {
                    SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.False));
                    GoToBeginActivity();
                } else if (response.statusCode == 422) {
                    json = HollerApplication.trimMessage(new String(response.data));
                    if (json != "" && json != null) {
                        displayMessage(json);
                    } else {
                        displayMessage(context.getResources().getString(R.string.please_try_again));
                    }

                } else if (response.statusCode == 503) {
                    displayMessage(context.getResources().getString(R.string.server_down));
                } else {
                    displayMessage(context.getResources().getString(R.string.please_try_again));
                }

            } catch (Exception e) {
                displayMessage(context.getResources().getString(R.string.something_went_wrong));
            }

        } else {
            displayMessage(context.getResources().getString(R.string.please_try_again));
        }
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    public void GoToBeginActivity() {
        SharedHelper.putKey(activity, "loggedIn", context.getResources().getString(R.string.False));
        Intent mainIntent;


        mainIntent = new Intent(activity, WelcomeView.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void goOffline() {
        try {
            FragmentManager manager = MainActivity.fragmentManager;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content, new Offline());
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "OFFLINE", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer = null;
        }
        if (ha != null) {
            ha.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(context, "Request Cancelled", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            showCustomFloatingView(getActivity(), false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public String getAddress(String strLatitude, String strLongitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        double latitude = Double.parseDouble(strLatitude);
        double longitude = Double.parseDouble(strLongitude);
        String address = "", city = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (address.length() > 0 || city.length() > 0)
            return address + ", " + city;
        else
            return context.getResources().getString(R.string.no_address);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {

        super.onPause();

        if (ha != null) {
            ha.removeCallbacksAndMessages(null);
        }
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.cancel_confirm));
        builder.setIcon(AccessDetails.site_icon);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showReasonDialog();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Reset to previous seletion menu in navigation
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        cancelDialog = builder.create();
        cancelDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg) {
            }
        });
        cancelDialog.show();
    }

    private void showOTPDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.otp_dialog, null);

        Button submitBtn = (Button) view.findViewById(R.id.submit_btn);
        final EditText reason = (EditText) view.findViewById(R.id.reason_etxt);
        final PinEntryView pinView = (PinEntryView) view.findViewById(R.id.pinView);

        builder.setView(view);
        otpDialog = builder.create();
        otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (strOTP.equalsIgnoreCase(pinView.getText().toString())) {
                    otpDialog.dismiss();
                    update(CurrentStatus, request_id);
                } else {
                    // OTP wrong
                    Toast.makeText(context, "Wrong OTP!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        otpDialog.show();
    }

    private void showReasonDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.cancel_dialog, null);

        Button submitBtn = (Button) view.findViewById(R.id.submit_btn);
        final EditText reason = (EditText) view.findViewById(R.id.reason_etxt);

        builder.setView(view);
        cancelReasonDialog = builder.create();
        cancelReasonDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelReasonDialog.dismiss();
                if (reason.getText().toString().length() > 0)
                    cancelRequest(request_id, reason.getText().toString());
                else
                    cancelRequest(request_id, "");
            }
        });
        cancelReasonDialog.show();
    }

    private void showSosDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.sos_confirm));
        builder.setIcon(AccessDetails.site_icon);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancelRequest(request_id);
                dialog.dismiss();
                String mobile = SharedHelper.getKey(context, "sos");
                if (mobile != null && !mobile.equalsIgnoreCase("null") && mobile.length() > 0) {
                } else {
                    displayMessage(context.getResources().getString(R.string.user_no_mobile));
                }

            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Reset to previous seletion menu in navigation
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg) {
            }
        });
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
//            NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.cancelAll();
//            ha.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //call function
//                    checkStatus();
//                    ha.postDelayed(this, 3000);
//                }
//            }, 3000);

//            context.stopService(new Intent(context, CustomFloatingViewService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void updateLiveTracking(String latitude, String longitude) {
        ApiInterface mApiInterface = RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);

        Call<ResponseBody> call = mApiInterface.getLiveTracking("XMLHttpRequest", "Bearer " + token,
                request_id, latitude, longitude);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.e("sUCESS", "SUCESS" + response.body());
                if (response.body() != null) {
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS", "bodyString" + bodyString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    /**
     * Set and initialize the view elements.
     */
    private void initializeView() {
//        context.startService(new Intent(context, FloatingViewService.class));
//        activity.finish();
    }

    @Deprecated
    private void showCustomFloatingView(Context context, boolean isShowOverlayPermission) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            final Intent intent = new Intent(context, CustomFloatingViewService.class);
//            getActivity().startService( intent);
            return;
        }

        if (Settings.canDrawOverlays(context)) {
//            final Intent intent = new Intent(context, CustomFloatingViewService.class);
//            getActivity().startService( intent);
            return;
        }

        if (isShowOverlayPermission) {
//            final CustomActivity activity = (CustomActivity) getActivity();
//            String permission = Manifest.permission.SYSTEM_ALERT_WINDOW;
//            RequestPermissionHandler handler = new RequestPermissionHandler() {
//                @Override
//                public void onPermissionGranted() {
////                    Toast.makeText(activity,"")
////                    Toast.makeText(activity,"Floating view allowed", Toast.LENGTH_LONG).show();
//
//                }
//
//                @Override
//                public void onPermissionDenied() {
//                    Toast.makeText(activity,"Floating view not allowed", Toast.LENGTH_LONG).show();
//                }
//            };
//
//            activity.checkPermissionAsynchronously(permission,handler);
        }
    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private class CreatingOrderTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Log.d("LALALA", strings.toString());
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("s_latitude", "46.9515");
                requestData.put("s_longitude", "32.0521983");
                requestData.put("d_latitude", "46.9515");
                requestData.put("d_longitude", "32.0521983");
                requestData.put("s_address", "Henerala Svyrydova St, 33, Mykolaiv, Mykolaivs'ka oblast, Ukraine, 54000");
                requestData.put("d_address", "Henerala Svyrydova St, 33, Mykolaiv, Mykolaivs'ka oblast, Ukraine, 54000");
                requestData.put("service_type", "1");
                requestData.put("distance", "0");
                requestData.put("schedule_date", "");
                requestData.put("schedule_time", "");
                requestData.put("payment_mode", "CASH");
                requestData.put("use_wallet", "0");
            } catch (Exception e) {
                Log.e("LALALA", "error while formig request data");
            }
            sendRequest(requestData);
            return null;
        }

        private boolean sendRequest(JSONObject requestData) {
            String data = "";
            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) new URL(URLHelper.base + URLHelper.SEND_REQUEST_API).openConnection();

                String auth = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6Ijg4YWI1ODE2YjIyNzEwMmZiNTM4NGE0MjE5MjJlZjlkNTI1YmIyYzJmYjI0NTA5NDhhNTFlOGQ4NDU3ZjUyZjdlODJkNGFiZjNjMWY5MjE1In0.eyJhdWQiOiIyIiwianRpIjoiODhhYjU4MTZiMjI3MTAyZmI1Mzg0YTQyMTkyMmVmOWQ1MjViYjJjMmZiMjQ1MDk0OGE1MWU4ZDg0NTdmNTJmN2U4MmQ0YWJmM2MxZjkyMTUiLCJpYXQiOjE1NDM0MTU0ODIsIm5iZiI6MTU0MzQxNTQ4MiwiZXhwIjoxNTQ0NzExNDgyLCJzdWIiOiIxMTQiLCJzY29wZXMiOlsiKiJdfQ.fnO2zHjPjoz1KOwk0Pl_FSLWxr7f8tfl77cH3kw9LlyX1xlKnifyq8O61yuzWb-azulSd_FJL2VWngmtqPVOs9uUjy4St3uL2dy2ESvcromazHQ9n8JqNO987XXrKoggq7ZWDJv2jmNCUFdGV1PrlPHJYAIjJwKjjUOrDZx30EDIiyIGBz5C1o2iUG643n12Kk3BeqfIr4zofrhtrMZ_qwlmiEu4GwMWjC6SV59z_wMtk83lVRxk3xgnbrOhCQaeD8Ve80OdThPhy07UzHDChRDBWW_9abd22FKxHgYvYiFhFYqfSc1l5Ssh-mgm6X1LZOtaJBgfC4WWhKnKwB5_O4vzAF9OlwPMpRth3mWf0Giw3ZmrnLVMNdkDNilcP7Hh47gEPqBo48ty00rxue2-rnYxtwdLTvvVzixrVSITahYv0rk_wd9G3iC5DV9tVANH1NL_A63p3XFFVCBHnfJPFOKryGNeJA9bDSWqc5Y7oUUXE6myEElkHHVf6DTAQ-1FhTPNjy5FrUAySazL2sis797IUXpH3xKCSR9cyD0dPMVo77CKsPd_E904nHu1kAZ8MHXk3NSFAwqeO9aw7uB3kgi8rXMLLZ5ICcc4-zse4PNu_AGGjrJ_-NxmPIXvn6kR1ksyt51IWOlSulV4wInRSkLbLLKVMOSrn1_FFdAhDWo";

                urlConnection.setRequestProperty("Authorization", auth);
                urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");

                urlConnection.connect();

                OutputStream os = urlConnection.getOutputStream();
                os.write(requestData.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String result = "";

                String l;
                while ((l = br.readLine()) != null) {
                    result += l;
                }

                Log.d("LALALA", "data received");
                Log.d("LALALA", result);
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                urlConnection.disconnect();
            }
            return false;
        }

        @Override
        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
            Log.d("LALALA", "FUFUFUFUFUFUFUFUFUFUF");
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                if (mMap != null) {
                    mMap.clear();
                }

                MarkerOptions markerOptions = new MarkerOptions().title("Source")
                        .position(sourceLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                mMap.addMarker(markerOptions);
                MarkerOptions markerOptions1 = new MarkerOptions().title("Destination")
                        .position(destLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.provider_marker));
                mMap.addMarker(markerOptions);
                mMap.addMarker(markerOptions1);


                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                LatLngBounds bounds;
                builder.include(sourceLatLng);
                builder.include(destLatLng);
                if (CurrentStatus.equalsIgnoreCase("STARTED")) {
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(sourceLatLng).zoom(16).build();
                    MarkerOptions markerOptionsq = new MarkerOptions();
                    markerOptionsq.position(sourceLatLng);
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    bounds = builder.build();
                    int padding = 320; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                }

                mMap.getUiSettings().setMapToolbarEnabled(false);


                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.parseColor(context.getResources().getString(0 + R.color.colorAccent)));

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null && points != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }


}
