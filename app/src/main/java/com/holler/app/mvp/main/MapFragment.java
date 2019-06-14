package com.holler.app.mvp.main;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.GPSTracker;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class MapFragment extends Fragment {

    @BindView(R.id.ma_map_orders_container)
    public View ordersContainer;

    @BindView(R.id.ma_map_to_current_location_button)
    public ImageView currentLocationButton;

    @BindView(R.id.ma_map_pass_button)
    public View passItOnButton;

    private GPSTracker.GPSTrackerBinder gpsTrackerService;
    private GoogleMap googleMap;
    private GPSTracker.LocationChangingListener locationListener;
    private static boolean cameraLocked = false;
    private static Disposable moveToCurrentLocationTimer = null;
    private MainPresenter presenter;
    private ServiceConnection gpsTrackerServiceConnection;
    private volatile boolean shouldUpdateMapWithoutAnimation = false;
    private MediaPlayer passItOnSound;
    private MediaPlayer errorSound;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.presenter = ((MainView) getActivity()).presenter;
        passItOnSound = MediaPlayer.create(getContext(), R.raw.pass_tone);
        errorSound = MediaPlayer.create(getContext(), R.raw.error_tone);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map, container, false);
        ButterKnife.bind(this, view);

        Observable
                .interval(15, TimeUnit.SECONDS)
                .doOnNext(aLong -> {
                    shouldUpdateMapWithoutAnimation = true;
                })
                .subscribe();

        bindGpsTrackerService()
                .doOnSubscribe(disposable -> {
                    ((CustomActivity) getActivity()).showSpinner();
                })
                .flatMap(service -> {
                    this.gpsTrackerService = service;
                    this.gpsTrackerService.addLocationListener(newLocation -> setMarker(newLocation));
                    return setupGoogleMap();
                })
                .flatMap(googleMap -> {
                    MapFragment.this.googleMap = googleMap;
                    setMapCameraToCurrentPosition(false, false);
                    ((CustomActivity) getActivity()).hideSpinner();
                    return Observable.empty();
                })
                .doOnError(throwable -> {
                    ((CustomActivity) getActivity()).showMessage(throwable.getMessage());
                })
                .subscribe();


        return view;
    }


    private Observable<GPSTracker.GPSTrackerBinder> bindGpsTrackerService() {
        return Observable.<GPSTracker.GPSTrackerBinder>create(emitter -> {
            Intent gpsTrackerBinding = new Intent(getActivity(), GPSTracker.class);

            this.gpsTrackerServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    emitter.onNext((GPSTracker.GPSTrackerBinder) service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    emitter.onError(new Throwable("Can't enable location tracking"));
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    emitter.onError(new Throwable("Can't enable location tracking"));
                }

                @Override
                public void onNullBinding(ComponentName name) {
                    emitter.onError(new Throwable("Can't enable location tracking"));
                }
            };

            getActivity().bindService(gpsTrackerBinding, this.gpsTrackerServiceConnection, Context.BIND_IMPORTANT);
        });
    }

    private Observable<GoogleMap> setupGoogleMap() {
        return Observable.create(emitter -> {
            SupportMapFragment googleMapContainer = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.ma_map_google_map_container);

            googleMapContainer.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));

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
//                            if (isCurrentLocationOut || isZoomOutOfRange || cameraLocked) {
//                                currentLocationButton.setVisibility(View.VISIBLE);
//                            } else {
//                                currentLocationButton.setVisibility(View.GONE);
//                            }
                        }
                    });


                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                    emitter.onNext(googleMap);
                }
            });
        });


    }

    private Location getCurrentLocation() {
        if (gpsTrackerService == null) return null;
        return gpsTrackerService.getLocation();
    }

    @OnClick(R.id.ma_map_to_current_location_button)
    public void toCurrentPosition() {
        setMapCameraToCurrentPosition(true, true);
    }

    public void setMapCameraToCurrentPosition(boolean withAnimation, boolean shouldUpdate) {
        cameraLocked = false;
        if (googleMap == null) return;
        Location location = getCurrentLocation();
        if (location == null) return;
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        CameraPosition cameraPosition = new CameraPosition
                .Builder()
                .target(currentLocation)
                .bearing(location.getBearing())
                .zoom(16)
                .build();

        if (withAnimation && shouldUpdate) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            shouldUpdateMapWithoutAnimation = false;
        } else if (withAnimation && !shouldUpdate) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

    }

    @OnClick(R.id.ma_map_pass_button)
    public void createOrder() {
        presenter
                .createAndSendOrder()
                .doOnSubscribe(disposable -> {
                })
                .doOnNext(orderCreated -> {
                    if(!orderCreated){
                        restartErrorSound();
                    }else{
                        restartSuccessSound();
                    }
                })
                .doOnError(throwable -> {
                    restartErrorSound();
                })
                .subscribe();
//        showRequestOrder();
    }

    private void restartSuccessSound() throws IOException {
        passItOnSound.stop();
        passItOnSound = MediaPlayer.create(getContext(), R.raw.pass_tone);
        passItOnSound.start();
    }

    private void restartErrorSound() {
        errorSound.stop();
        errorSound = MediaPlayer.create(getContext(), R.raw.error_tone);
        errorSound.start();
    }

    private void setMarker(Location location) {
        if (googleMap == null) return;
        if (location == null) return;

        googleMap.clear();
        List<RetrofitModule.ServerAPI.OrderResponse> requestsInSearching = presenter.orderModel.getRequestsInSearching();
        try {
            for (RetrofitModule.ServerAPI.OrderResponse o : requestsInSearching) {
                LatLng p = new LatLng(Double.parseDouble(o.sLatitude), Double.parseDouble(o.sLongitude));
                MarkerOptions m = new MarkerOptions()
                        .position(p)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                googleMap.addMarker(m);
            }
        } catch (NumberFormatException | NullPointerException e) {
            Logger.e(e.getMessage(), e);
        }

        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location));
        googleMap.addMarker(markerOptions);

        if (!cameraLocked) {
            setMapCameraToCurrentPosition(true, shouldUpdateMapWithoutAnimation);
        }
    }

    private RequestOrderFragment requestOrderFragment;
    private ArrivedOrderFragment arrivedOrderFragment;
    private RateOrderFragment rateOrderFragment;

    public void showRequestOrder(OrderModel.Order order) {
        String address = order.data.sAddress;

        int time = order.timeToRespond;
        if (requestOrderFragment != null) {
            removeFragment(requestOrderFragment);
        }

        RequestOrderFragment fragment = RequestOrderFragment.newInstance(address, time);
        requestOrderFragment = fragment;

        fragment.source
                .doOnSubscribe(disposable -> addFragment(fragment))
                .flatMap(isAccepted -> {
                    if (isAccepted) {
                        return order.accept().toObservable();
                    } else {

                        return order.reject().toObservable();
                    }
                })
                .doOnError(throwable -> {
                    UserModel.ParsedThrowable error = UserModel.ParsedThrowable.parse(throwable);
//                    ((MainView)getActivity()).showMessage(error.getMessage()); //vahahaha
                })
                .doFinally(() -> {
                    removeFragment(fragment);
                    presenter.resetState();
                })
                .subscribe();
    }

    public void showArrivedOrder(OrderModel.Order order) {
        String address = order.data.sAddress;

        arrivedOrderFragment = ArrivedOrderFragment.newInstance(address);

        arrivedOrderFragment
                .source
                .doOnSubscribe(disposable -> addFragment(arrivedOrderFragment))
                .flatMap(aBoolean -> {
                    return Observable
                            .timer(1, TimeUnit.SECONDS)
                            .flatMap(aLong -> {
                                return Observable.just(aBoolean);
                            });
                })
                .flatMap(isAccepted -> {
                    if (isAccepted) {
                        return order
                                .arrived()
                                .toObservable();
                    } else {
                        return order
                                .cancel()
                                .toObservable();
                    }
                })
                .doOnError(throwable -> {
                    UserModel.ParsedThrowable error = UserModel.ParsedThrowable.parse(throwable);
//                    ((MainView)getActivity()).showMessage(error.getMessage()); //vahahaha
                })
                .doFinally(() -> removeFragment(arrivedOrderFragment))
                .subscribe();
    }

    public void showRateOrder(OrderModel.Order order) {
        rateOrderFragment = new RateOrderFragment();


        rateOrderFragment.source
                .doOnSubscribe(disposable -> addFragment(rateOrderFragment))
                .flatMap(rating -> {
                    return order
                            .rate(rating)
                            .toObservable();
                })
                .doOnError(throwable -> {
                    UserModel.ParsedThrowable error = UserModel.ParsedThrowable.parse(throwable);
//                    ((MainView)getActivity()).showMessage(error.getMessage()); //vahahaha
                })
                .doFinally(() -> removeFragment(rateOrderFragment))
                .subscribe();
    }

    private void addFragment(Fragment f) {
        try {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(ordersContainer.getId(), f)
                    .commit();
        } catch (NullPointerException e) {
            Observable.timer(3, TimeUnit.SECONDS).flatMap(aLong -> {
                addFragment(f);
                return Observable.empty();
            }).subscribe();
        }
    }

    private void removeFragment(Fragment f) {
        if (f == null) return;
        try {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .remove(f)
                    .commit();
        } catch (Exception e) {
            Logger.e("Can't remove fragment");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
//        removeFragments();
    }

    public void removeFragments() {
        removeFragment(requestOrderFragment);
        removeFragment(arrivedOrderFragment);
        removeFragment(rateOrderFragment);
    }

    public void hidePassItOnButton() {
        try {
            passItOnButton.setVisibility(View.GONE);
        } catch (NullPointerException e) {

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
                    if (MapFragment.moveToCurrentLocationTimer != null)
                        MapFragment.moveToCurrentLocationTimer.dispose();
                    MapFragment.moveToCurrentLocationTimer = Observable
                            .timer(3, TimeUnit.SECONDS)
                            .doOnNext(aLong -> {
                                cameraLocked = false;
                            })
                            .subscribe();
                    break;

            }
            return super.dispatchTouchEvent(event);
        }
    }

}
