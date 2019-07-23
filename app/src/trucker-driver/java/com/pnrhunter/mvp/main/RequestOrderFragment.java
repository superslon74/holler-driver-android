package com.pnrhunter.mvp.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;

public class RequestOrderFragment extends Fragment{


    @BindView(R.id.of_progress_bar)
    public ProgressBar progressbar;
    @BindView(R.id.of_progress_counter)
    public TextView counterView;
    @BindView(R.id.of_from_address)
    public TextView fromAddressView;
    @BindView(R.id.of_to_address)
    public TextView toAddressView;
    @BindView(R.id.of_weight)
    public TextView weightView;
    @BindView(R.id.of_date)
    public TextView dateView;
    @BindView(R.id.of_payment)
    public TextView paymentView;
    
    public Observable<Boolean> source;
    private ObservableEmitter<Boolean> emitter;
    private Disposable timer;
    private String addressFrom;
    private String addressTo;
    private String weight;
    private String date;
    private String payment;
    private int time = 0;

    public RequestOrderFragment() {
        this.source = Observable.create(emitter -> {
            RequestOrderFragment.this.emitter = emitter;
        });
    }


    private static final String ARG_ADDRESS_FROM = "address_from";
    private static final String ARG_ADDRESS_TO = "address_to";
    private static final String ARG_WEIGHT = "weight";
    private static final String ARG_DATE = "date";
    private static final String ARG_PAYMENT = "payment";
    private static final String ARG_TIME = "time";

    public static RequestOrderFragment newInstance(RequestedOrderResponse order) {
        RequestOrderFragment fragment = new RequestOrderFragment();
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
        View view = inflater.inflate(R.layout.fragment_order_request, container, false);
        ButterKnife.bind(this,view);
        fromAddressView.setText(addressFrom);
        toAddressView.setText(addressTo);
        weightView.setText(weight);
        dateView.setText(date);
        paymentView.setText(payment);
        timer = createTimer(time).subscribe();
        view.setOnTouchListener((v, event) -> true);
        return view;
    }
    

    
    @OnClick(R.id.of_button_cancel)
    public void rejectOrder(){
        emitter.onNext(false);
        emitter.onComplete();
    }
    
    @OnClick({R.id.of_progress_counter, R.id.of_progress_bar, R.id.of_pick_up_button})
    public void acceptOrder(){
        emitter.onNext(true);
        emitter.onComplete();
    }


    private Observable createTimer(int time) {
        long interval = 1000;
        long remainingTime = time*1000;
        return Observable
                .intervalRange(0,remainingTime,0,interval, TimeUnit.MILLISECONDS)
                .doOnNext(pastTime -> {
                    if (time<=pastTime){
                        timer.dispose();
                    }
                    runOnUiThread(() -> {
                        int progress = (int) (((double)(time-pastTime)/(double)time)*100);
                        progressbar.setProgress(progress);
                        int seconds = (int) (time-pastTime);
                        counterView.setText(seconds+"");
                    });
                })
                .doFinally(() -> {
                    rejectOrder();
                });

    }

    private void runOnUiThread(Runnable r){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(r);
    }

}
