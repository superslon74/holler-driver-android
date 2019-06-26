package com.pnrhunter.mvp.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pnrhunter.HollerApplication;
import com.pnrhunter.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class ArrivedOrderFragment extends Fragment {

    @BindView(R.id.of_address_big)
    public TextView addressBigView;
    @BindView(R.id.of_address_small)
    public TextView addressSmallView;

    public Subject<Boolean> source;

    private static final String ARG_ADDRESS = "address";
    private static final String ARG_ADDRESS_BIG = "address_big";
    private static final String ARG_ADDRESS_SMALL = "address_small";
    private String address;
    private String addressBig;
    private String addressSmall;


    public ArrivedOrderFragment() {
        this.source = PublishSubject.create();
    }


    public static ArrivedOrderFragment newInstance(String address) {
        ArrivedOrderFragment fragment = new ArrivedOrderFragment();
        Bundle args = new Bundle();
        String[] addressValues = address.split(",");
        String addressBig = "";
        String addressSmall = "";
        if (addressValues.length<2){
            addressBig = HollerApplication.getInstance().getApplicationContext().getString(R.string.something_went_wrong);
            addressSmall = HollerApplication.getInstance().getApplicationContext().getString(R.string.something_went_wrong);
        }else if(addressValues.length==2){
            addressBig = addressValues[0];
            addressSmall = addressValues[1];
        }else{
            addressBig = addressValues[0]+", "+addressValues[1];
            addressSmall = addressValues[2];
        }
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_ADDRESS_BIG, addressBig);
        args.putString(ARG_ADDRESS_SMALL, addressSmall);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getString(ARG_ADDRESS);
            addressBig = getArguments().getString(ARG_ADDRESS_BIG);
            addressSmall = getArguments().getString(ARG_ADDRESS_SMALL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map_order_arrived, container, false);
        ButterKnife.bind(this, view);

        addressBigView.setText(addressBig);
        addressSmallView.setText(addressSmall);

        return view;
    }


    @OnClick(R.id.of_button_cancel)
    public void cancelOrder() {
        source.onNext(false);
        source.onComplete();
    }

    @OnClick(R.id.of_button_arrived)
    public void acceptOrder() {
        source.onNext(true);
        source.onComplete();
    }

    @OnClick(R.id.of_button_navigate)
    public void openGoogleMapForNavigation() {
        Uri url = Uri.parse("http://maps.google.com/maps?f=d&hl=en&daddr=" + address);
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }


}
