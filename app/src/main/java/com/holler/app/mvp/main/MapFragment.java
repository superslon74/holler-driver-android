package com.holler.app.mvp.main;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
import com.holler.app.utils.GPSTracker;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;

public class MapFragment extends Fragment {

    @BindView(R.id.ma_map_orders_container)
    public View ordersContainer;

    @BindView(R.id.ma_map_to_current_location_button)
    public ImageView currentLocationButton;

    @BindView(R.id.ma_map_pass_button)
    public View passItOnButton;

    private GPSTracker.GPSTrackerBinder gpsTrackerBinder;
    private GoogleMap googleMap;
    private GPSTracker.LocationChangingListener locationListener;
    private static boolean cameraLocked = false;
    private MainPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.presenter = ((MainView) getActivity()).presenter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map, container, false);
        ButterKnife.bind(this, view);

        //TODO: make observables for this
        initLocationListener();
        bindGpsTracker();
        setupGoogleMap();
        //TODO: fix with observable
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setMapCameraToCurrentPosition(false);
            }
        }, 500);

        return view;
    }

    private void initLocationListener() {
        this.locationListener = new GPSTracker.LocationChangingListener() {
            @Override
            public void onLocationChanged(Location newLocation) {
//                if(!cameraLocked)
                setMarker(newLocation);
            }
        };
    }

    private void bindGpsTracker() {
        Intent gpsTrackerBinding = new Intent(getActivity(), GPSTracker.class);
        ServiceConnection gpsTrackerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                gpsTrackerBinder = (GPSTracker.GPSTrackerBinder) binder;
                gpsTrackerBinder.addLocationListener(locationListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {


            }
        };
        getActivity().bindService(gpsTrackerBinding, gpsTrackerConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupGoogleMap() {
        SupportMapFragment googleMapContainer = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.ma_map_google_map_container);



        googleMapContainer.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapFragment.this.googleMap = googleMap;
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));

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
                        Location location = getCurrentLocation();
                        if (location == null) return;
                        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                        boolean isCurrentLocationOut =
                                !googleMap
                                        .getProjection()
                                        .getVisibleRegion()
                                        .latLngBounds
                                        .contains(currentPosition);
                        boolean isZoomOutOfRange = googleMap.getCameraPosition().zoom > 17 || googleMap.getCameraPosition().zoom < 14;
                        if (isCurrentLocationOut || isZoomOutOfRange) {
                            currentLocationButton.setVisibility(View.VISIBLE);
                        } else {
                            currentLocationButton.setVisibility(View.GONE);
                        }
                    }
                });



                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

//                setMarker(gpsTrackerBinder.getLocation());

            }
        });
    }

    private Location getCurrentLocation() {
        if (gpsTrackerBinder == null) return null;
        return gpsTrackerBinder.getLocation();
    }

    @OnClick(R.id.ma_map_to_current_location_button)
    public void toCurrentPosition(){
        setMapCameraToCurrentPosition(true);
    }

    public void setMapCameraToCurrentPosition(boolean withAnimation) {
        cameraLocked=false;
        if(googleMap==null) return;
        Location location = getCurrentLocation();
        if (location == null) return;
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(16).build();
        if(withAnimation)
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        else
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @OnClick(R.id.ma_map_pass_button)
    public void createOrder(){
        presenter.createAndSendOrder();
//        showRequestOrder();
    }

    private void setMarker(Location location) {
        if (googleMap == null) return;
        if (location == null) return;

        googleMap.clear();
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location));
        googleMap.addMarker(markerOptions);

        if (!cameraLocked) {
            setMapCameraToCurrentPosition(true);
        }
    }

    public void showRequestOrder(OrderModel.Order order) {
        String address = order.data.sAddress;
        int time = order.timeToRespond;
        RequestOrderFragment o = RequestOrderFragment.newInstance(address, time);

        o.source
                .doOnSubscribe(disposable -> addFragment(o))
                .flatMap(isAccepted -> {
                    if(isAccepted){

                        return order.accept().toObservable();
                    }else{

                        return order.reject().toObservable();
                    }
                })
                .doOnError(throwable -> {
                    UserModel.ParsedThrowable error = UserModel.ParsedThrowable.parse(throwable);
                    ((MainView)getActivity()).onMessage(error.getMessage()); //vahahaha
                })
                .doFinally(() -> removeFragment(o))
                .subscribe();
    }

    public void showArrivedOrder(OrderModel.Order order){
        String address = order.data.sAddress;

        ArrivedOrderFragment o = ArrivedOrderFragment.newInstance(address);

        o.source
                .doOnSubscribe(disposable -> addFragment(o))
                .flatMap(isAccepted -> {
                    if(isAccepted){
                        return order
                                .arrived()
                                .toObservable();
                    }else{
                        return order
                                .cancel()
                                .toObservable();
                    }
                })
                .doOnError(throwable -> {
                    UserModel.ParsedThrowable error = UserModel.ParsedThrowable.parse(throwable);
                    ((MainView)getActivity()).onMessage(error.getMessage()); //vahahaha
                })
                .doFinally(() -> removeFragment(o))
                .subscribe();
    }

    public void showRateOrder(OrderModel.Order order){
        RateOrderFragment o = new RateOrderFragment();

        o.source
                .doOnSubscribe(disposable -> addFragment(o))
                .flatMap(rating -> {
                    return order
                            .rate(rating)
                            .toObservable();
                })
                .doOnError(throwable -> {
                    UserModel.ParsedThrowable error = UserModel.ParsedThrowable.parse(throwable);
                    ((MainView)getActivity()).onMessage(error.getMessage()); //vahahaha
                })
                .doFinally(() -> removeFragment(o))
                .subscribe();
    }

    private void addFragment(Fragment f){
        try {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(ordersContainer.getId(), f)
                    .commit();
        }catch (NullPointerException e){
            Observable.timer(3, TimeUnit.SECONDS).flatMap(aLong -> {
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .add(ordersContainer.getId(), f)
                        .commit();
                return Observable.empty();
            }).subscribe();
        }
    }
    private void removeFragment(Fragment f){
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .remove(f)
                .commit();
    }

    public void hidePassItOnButton() {
        try{
            passItOnButton.setVisibility(View.GONE);
        }catch (NullPointerException e){

        }
    }
    public void showPassItOnButton() {
        passItOnButton.setVisibility(View.VISIBLE);
    }

    public static class CustomSupportMapFragment extends SupportMapFragment {
        public View mOriginalContentView;
        public TouchableWrapper mTouchView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
            mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
            mTouchView = new TouchableWrapper(getActivity());
            mTouchView.addView(mOriginalContentView);
            return mTouchView;
        }

        @Override
        public View getView() {
            return mOriginalContentView;
        }
    }
    public static class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    MapFragment.cameraLocked = true;
                    break;

            }
            return super.dispatchTouchEvent(event);
        }
    }

}
