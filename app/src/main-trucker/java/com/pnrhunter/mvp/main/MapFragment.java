package com.pnrhunter.mvp.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.DaggerFragment;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.pnrhunter.mvp.utils.activity.PermissionChecker;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MapFragment extends DaggerFragment {

    @BindView(R.id.ma_map_to_current_location_button)
    public ImageView currentLocationButton;


    private static boolean cameraLocked = false;
    private static Disposable moveToCurrentLocationTimer = null;
    private GoogleMap googleMap;
    //    private ServiceConnection gpsTrackerServiceConnection;
    private volatile boolean shouldUpdateMapWithoutAnimation = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.presenter = ((MainView) getActivity()).presenter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);

        Observable
                .interval(15, TimeUnit.SECONDS)
                .doOnNext(aLong -> {
                    shouldUpdateMapWithoutAnimation = true;
                })
                .subscribe();

        ExtendedActivity activity = (ExtendedActivity)getActivity();
        activity
                .checkPermissionAsynchronously(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribeOn(Schedulers.newThread())
                .flatMap(aBoolean -> {
                    return activity.checkPermissionAsynchronously(PermissionChecker.PERMISSION_ENABLE_LOCATION);
                })
                .flatMap(aBoolean -> {
                    return setupGoogleMap();
                })
                .flatMap(googleMap -> {
                    MapFragment.this.googleMap = googleMap;
                    return Observable.empty();
                })
                .subscribe();

//        bindGpsTrackerService()
//                .doOnSubscribe(disposable -> {
//                    ((CustomActivity) getActivity()).showSpinner();
//                })
//                .flatMap(service -> {
//                    this.gpsTrackerService = service;
//                    this.gpsTrackerService.addLocationListener(newLocation -> setMarker(newLocation));
//                    return setupGoogleMap();
//                })
//                .flatMap(googleMap -> {
//                    MapFragment.this.googleMap = googleMap;
//                    setMapCameraToCurrentPosition(false, false);
//                    ((CustomActivity) getActivity()).hideSpinner();
//                    ((MainView) getActivity()).displayGamburger(true);
//                    return Observable.empty();
//                })
//                .doOnError(throwable -> {
//                    ((CustomActivity) getActivity()).showMessage(throwable.getMessage());
//                    ((MainView) getActivity()).displayGamburger(false);
//                })
//                .subscribe();


        return view;
    }



    private Observable<GoogleMap> setupGoogleMap() {
        return Observable.create(emitter -> {
            SupportMapFragment googleMapContainer = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.ma_map_google_map_container);

            Handler mainHandler = new Handler(getActivity().getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    googleMapContainer.getMapAsync(new OnMapReadyCallback() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

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
                }
            };
            mainHandler.post(myRunnable);


        });


    }

    private Location getCurrentLocation() {
//        if (gpsTrackerService == null) return null;
//        return gpsTrackerService.getLocation();
        return null;
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


    private void setMarker(Location location) {
        if (googleMap == null) return;
        if (location == null) return;

        googleMap.clear();

        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_current_location));
        googleMap.addMarker(markerOptions);

        if (!cameraLocked) {
            setMapCameraToCurrentPosition(true, shouldUpdateMapWithoutAnimation);
        }
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
