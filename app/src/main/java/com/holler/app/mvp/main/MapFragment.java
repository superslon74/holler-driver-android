package com.holler.app.mvp.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.holler.app.R;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: connect to gps tracker with MainView

    }

    private GoogleMap googleMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map, container, false);
        ButterKnife.bind(this,view);

        //TODO: launch orders observer
        setupGoogleMap();

        return view;
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

                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.setBuildingsEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(false);
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);
                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        //TODO: check if marker exists get its position
                        LatLng currentPosition = null;
                        if (!googleMap
                                .getProjection()
                                .getVisibleRegion()
                                .latLngBounds
                                .contains(currentPosition)) {
                            //TODO: check for large zoom
                            //TODO: set toCurrentLocation button visible
                        } else {
                            //TODO: set toCurrentLocation button invisible
                        }
                    }
                });
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                //TODO: get google api client from gpstrecker
                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getContext())
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(@Nullable Bundle bundle) {
                                //TODO: make location request

                            }

                            @Override
                            public void onConnectionSuspended(int i) {

                            }
                        })
                        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                            }
                        })
                        .addApi(LocationServices.API)
                        .build();

                googleApiClient.connect();
            }
        });

    }

    //TODO: bind to button
    public void setMapCameraToCurrentPosition(){
        //TODO: get current location
        LatLng currentLocation = new LatLng(0, 0);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(16).build();
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
