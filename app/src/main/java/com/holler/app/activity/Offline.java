package com.holler.app.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.holler.app.AndarApplication;
import com.holler.app.Fragment.Map;
import com.holler.app.Helper.ConnectionHelper;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.mvp.welcome.WelcomeView;
import com.holler.app.server.OrderServerApi;
import com.holler.app.utils.GPSTracker;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import javax.inject.Inject;


public class Offline extends Fragment {

    Activity activity;
    Context context;
    ConnectionHelper helper;
    Boolean isInternet;
    View rootView;

    String token;
    Button goOnlineBtn;
    //menu icon
    ImageView menuIcon;
    int NAV_DRAWER = 0;
    DrawerLayout drawer;

    Utilities utils = new Utilities();

    @Inject public RetrofitModule.ServerAPI serverAPI;
    @Inject public Context appContext;
    private GPSTracker.GPSTrackerBinder gpsService;


    public Offline() {
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
        AndarApplication.getInstance().component().inject(this);
        Intent gpsTrackerBinding = new Intent(appContext, GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Offline.this.gpsService = (GPSTracker.GPSTrackerBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Offline.this.gpsService = null;
            }
        };
        appContext.bindService(gpsTrackerBinding,gpsTrackerConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.activity_offline, container, false);
        findViewByIdAndInitialize();
        return  rootView;
    }

    public void findViewByIdAndInitialize(){
        helper = new ConnectionHelper(activity);
        isInternet = helper.isConnectingToInternet();
        token = SharedHelper.getKey(activity, "access_token");
        goOnlineBtn = (Button) rootView.findViewById(R.id.goOnlineBtn);
        menuIcon = (ImageView) rootView.findViewById(R.id.menuIcon);
        drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        goOnlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    goOnline();
            }
        });
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NAV_DRAWER == 0) {
                    drawer.openDrawer(Gravity.START);
                }else {
                    NAV_DRAWER = 0;
                    drawer.closeDrawers();
                }
            }
        });
    }


    public void goOnline(){


        String authHeader = "Bearer "+token;
        serverAPI
                .sendStatus(authHeader, RetrofitModule.ServerAPI.STATUS_ONLINE)
                .enqueue(new OrderServerApi.CallbackErrorHandler<JsonObject>(getActivity()) {
                    @Override
                    public void onSuccessfulResponse(retrofit2.Response<JsonObject> response) {
                        FragmentManager manager = MainActivity.fragmentManager;
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.content, new Map());
                        transaction.commitAllowingStateLoss();

                        gpsService.startTracking();
                    }

                    @Override
                    public void onFinishHandling() {
                        super.onFinishHandling();

                    }

                });

    }

    public void displayMessage(String toastString){
        utils.print("displayMessage",""+toastString);
        Snackbar.make(getView(),toastString, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    public void GoToBeginActivity(){
        SharedHelper.putKey(activity,"loggedIn",getString(R.string.False));
        Intent mainIntent;

            mainIntent = new Intent(activity, WelcomeView.class);

            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
