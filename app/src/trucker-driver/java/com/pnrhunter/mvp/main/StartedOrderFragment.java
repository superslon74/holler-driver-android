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

    private enum Mode {CONFIRMED, STARTED, LOADED, COMPLETED};

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



    public static StartedOrderFragment newInstance(RequestedOrderResponse order) {
        StartedOrderFragment fragment = new StartedOrderFragment();
        Bundle args = new Bundle();

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

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_started, container, false);
        ButterKnife.bind(this,view);

        setupView(Mode.COMPLETED);
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
