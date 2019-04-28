package com.holler.app.mvp.main;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.holler.app.R;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.GPSTracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapFragment extends Fragment {


    @BindView(R.id.ma_map_nav_open_dot)
    public UserStatusDot userStatusDot;

    @BindView(R.id.ma_map_orders_container)
    public View ordersContainer;
    
    @BindView(R.id.ma_map_to_current_location_button)
    public ImageView currentLocationButton;

    private GPSTracker.GPSTrackerBinder gpsTrackerBinder;
    private GoogleMap googleMap;
    private GPSTracker.LocationChangingListener locationListener;
    private boolean cameraLocked = false;
    private MainPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.presenter = ((MainView)getActivity()).presenter;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map, container, false);
        ButterKnife.bind(this,view);

        //TODO: make observables for this
        initLocationListener();
        startOrderObserver();
        bindGpsTracker();
        setupGoogleMap();
        //TODO: fix with observable
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setMapCameraToCurrentPosition();
            }
        },1000);


        return view;
    }

    private void initLocationListener() {
        this.locationListener = new GPSTracker.LocationChangingListener() {
            @Override
            public void onLocationChanged(Location newLocation) {
                if(!cameraLocked)
                    setMarker(newLocation);
            }
        };
    }

    private void startOrderObserver() {

    }

    private void bindGpsTracker() {
        Intent gpsTrackerBinding = new Intent(getActivity(), GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                gpsTrackerBinder =  (GPSTracker.GPSTrackerBinder) binder;
                gpsTrackerBinder.addLocationListener(locationListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {


            }
        };
        getActivity().bindService(gpsTrackerBinding, gpsTrackerConnection, Context.BIND_AUTO_CREATE);
    }

    @OnClick(R.id.ma_map_nav_open_button)
    public void openNavigation(){
        ((MainView)getActivity()).drawerView.openDrawer(Gravity.LEFT);
    }

    private void setupGoogleMap() {
        SupportMapFragment gootleMapContainer = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.ma_map_google_map_container);

        gootleMapContainer.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapFragment.this.googleMap = googleMap;
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.style_json));

                //TODO: check location permission

                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.setBuildingsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(false);
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);

                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        cameraLocked=true;
                        Location location = getCurrentLocation();
                        if(location==null) return;
                        LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
                        boolean isCurrentLocationOut = 
                                !googleMap
                                .getProjection()
                                .getVisibleRegion()
                                .latLngBounds
                                .contains(currentPosition);
                        boolean isZoomOutOfRange = googleMap.getCameraPosition().zoom>17 || googleMap.getCameraPosition().zoom<14;
                        if (isCurrentLocationOut || isZoomOutOfRange) {
                            currentLocationButton.setVisibility(View.VISIBLE);
                        } else {
                            currentLocationButton.setVisibility(View.GONE);
                        }
                    }
                });
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                setMarker(gpsTrackerBinder.getLocation());

            }
        });
    }

    private Location getCurrentLocation(){
        if (gpsTrackerBinder==null) return null;
        return gpsTrackerBinder.getLocation();
    }

    @OnClick(R.id.ma_map_to_current_location_button)
    public void setMapCameraToCurrentPosition(){
        cameraLocked=false;
        Location location = getCurrentLocation();
        if(location==null) return;
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(16).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setMarker(Location location){
        if(googleMap==null)return;
        if(location==null) return;

        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location));

        googleMap.addMarker(markerOptions);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(16).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void showOrder() {
        OrderFragment o = new OrderFragment();
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .add(ordersContainer.getId(),o)
                .commit();
    }




}
