package com.pnrhunter.mvp.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;

public class StartedOrderFragment extends Fragment{

    private static enum Mode {CONFIRMED, STARTED, LOADED, COMPLETED};

    @BindView(R.id.of_address) protected View addressView;
    @BindView(R.id.of_marker) protected View markerView;
    @BindView(R.id.of_order_weight) protected View orderWeightView;
    @BindView(R.id.of_order_id) protected View orderIdView;
    @BindView(R.id.of_button_submit) protected ImageView buttonSubmit;
    @BindView(R.id.of_button_submit_caption) protected TextView buttonSubmitCaption;
    @BindView(R.id.of_button_navigate) protected ImageView buttonNavigate;
    @BindView(R.id.of_button_navigate_caption) protected TextView buttonNavigateCaption;


    public Observable<Boolean> source;
    private ObservableEmitter<Boolean> emitter;
    private Disposable timer;
    private String addressFrom;
    private String addressTo;
    private String weight;
    private String date;
    private String payment;
    private int time = 0;

    public StartedOrderFragment() {
        this.source = Observable.create(emitter -> {
            StartedOrderFragment.this.emitter = emitter;
        });
    }


    private static final String ARG_ADDRESS_FROM = "address_from";
    private static final String ARG_ADDRESS_TO = "address_to";
    private static final String ARG_WEIGHT = "weight";
    private static final String ARG_DATE = "date";
    private static final String ARG_PAYMENT = "payment";
    private static final String ARG_TIME = "time";

    public static StartedOrderFragment newInstance(RequestedOrderResponse order) {
        StartedOrderFragment fragment = new StartedOrderFragment();
        Bundle args = new Bundle();

        args.putString(ARG_ADDRESS_FROM, order.order.sAddress);
        args.putString(ARG_ADDRESS_TO, order.order.dAddress);
        args.putInt(ARG_WEIGHT, order.order.weight);
        args.putString(ARG_DATE, order.order.startedAt);
        args.putString(ARG_PAYMENT, order.order.paymentMode);
        args.putInt(ARG_TIME, order.timeToRespond);
        fragment.setArguments(args);
        return fragment;
    }

    protected boolean onTouchEvent (MotionEvent me) {
        return true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            addressFrom = getArguments().getString(ARG_ADDRESS_FROM);
            addressTo = getArguments().getString(ARG_ADDRESS_TO);
            weight = getArguments().getInt(ARG_WEIGHT) + "";
            date = getArguments().getString(ARG_DATE);
            payment = getArguments().getString(ARG_PAYMENT);
            time = getArguments().getInt(ARG_TIME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_started, container, false);
        ButterKnife.bind(this,view);
//        fromAddressView.setText(addressFrom);
//        toAddressView.setText(addressTo);
//        weightView.setText(weight);
//        dateView.setText(date);
//        paymentView.setText(payment);
//        timer = createTimer(time).subscribe();
//        stage.setStage(2);
        setupView(Mode.STARTED);
        view.setOnTouchListener((v, event) -> true);
        return view;
    }

    private void setupView(Mode mode){
        switch (mode){
            case CONFIRMED:
                addressView.setVisibility(View.VISIBLE);
                markerView.setVisibility(View.VISIBLE);
                orderWeightView.setVisibility(View.GONE);
                orderIdView.setVisibility(View.GONE);

                buttonSubmitCaption.setText("Arrived");
                break;
            case STARTED:
                addressView.setVisibility(View.GONE);
                markerView.setVisibility(View.GONE);
                orderWeightView.setVisibility(View.GONE);
                orderIdView.setVisibility(View.GONE);
                buttonNavigate.setVisibility(View.GONE);
                buttonNavigateCaption.setVisibility(View.GONE);

                buttonSubmitCaption.setText("Loaded");
                break;
            case LOADED:
                addressView.setVisibility(View.GONE);
                markerView.setVisibility(View.GONE);
                orderWeightView.setVisibility(View.VISIBLE);
                orderIdView.setVisibility(View.VISIBLE);

                buttonSubmitCaption.setText("Arrived");
                break;
            case COMPLETED:

                addressView.setVisibility(View.GONE);
                markerView.setVisibility(View.GONE);
                orderWeightView.setVisibility(View.VISIBLE);
                orderIdView.setVisibility(View.VISIBLE);
                buttonNavigate.setVisibility(View.GONE);
                buttonNavigateCaption.setVisibility(View.GONE);

                buttonSubmitCaption.setText("TTN");
                break;
        }
    }



}
