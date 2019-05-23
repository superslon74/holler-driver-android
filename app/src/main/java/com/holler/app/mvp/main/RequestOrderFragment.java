package com.holler.app.mvp.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holler.app.AndarApplication;
import com.holler.app.R;

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
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RequestOrderFragment extends Fragment{


    @BindView(R.id.of_progress_bar)
    public ProgressBar progressbar;
    @BindView(R.id.of_progress_counter)
    public TextView counterView;
    @BindView(R.id.of_message)
    public TextView bigAddressView;
    @BindView(R.id.of_address)
    public TextView smallAddressView;
    
    public Observable<Boolean> source;
    private ObservableEmitter<Boolean> emitter;
    private Disposable timer;
    private String addressBig;
    private String addressSmall;
    private int time = 0;

    public RequestOrderFragment() {
        this.source = Observable.create(emitter -> {
            RequestOrderFragment.this.emitter = emitter;
        });
    }


    private static final String ARG_ADDRESS_BIG = "address_big";
    private static final String ARG_ADDRESS_SMALL = "address_small";
    private static final String ARG_TIME = "time";

    public static RequestOrderFragment newInstance(String address, int time) {
        RequestOrderFragment fragment = new RequestOrderFragment();
        Bundle args = new Bundle();
        String[] addressValues = address.split(",");
        String street = "";
        String town = "";
        if (addressValues.length<2){
            street = AndarApplication.getInstance().getApplicationContext().getString(R.string.something_went_wrong);
            town = AndarApplication.getInstance().getApplicationContext().getString(R.string.something_went_wrong);
        }else if(addressValues.length==2){
            street = addressValues[0];
            town = addressValues[1];
        }else{
            street = addressValues[0]+", "+addressValues[1];
            town = addressValues[2];
        }

        args.putString(ARG_ADDRESS_BIG, street);
        args.putString(ARG_ADDRESS_SMALL, town);
        args.putInt(ARG_TIME, time);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            addressBig = getArguments().getString(ARG_ADDRESS_BIG);
            addressSmall = getArguments().getString(ARG_ADDRESS_SMALL);
            time = getArguments().getInt(ARG_TIME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map_order_request, container, false);
        ButterKnife.bind(this,view);
        bigAddressView.setText(addressBig);
        smallAddressView.setText(addressSmall);
        timer = createTimer(time).subscribe();

        return view;
    }
    

    
    @OnClick(R.id.of_button_cancel)
    public void rejectOrder(){
        timer.dispose();
        emitter.onNext(false);
        emitter.onComplete();
    }
    
    @OnClick({R.id.of_progress_counter, R.id.of_progress_bar, R.id.of_pick_up_button})
    public void acceptOrder(){
        timer.dispose();
        emitter.onNext(true);
        emitter.onComplete();
    }


    private Observable createTimer(int time) {
        long interval = 1000;
        long remainingTime = time*1000;
        return Observable
                .intervalRange(0,remainingTime,0,interval, TimeUnit.MILLISECONDS)
                .doOnNext(pastTime -> {
                    if (time==pastTime){
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
