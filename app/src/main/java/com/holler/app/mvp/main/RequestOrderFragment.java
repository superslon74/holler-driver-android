package com.holler.app.mvp.main;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holler.app.AndarApplication;
import com.holler.app.Fragment.DocumentsListItem;
import com.holler.app.R;
import com.holler.app.activity.DocumentsActivity;
import com.orhanobut.logger.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    
    public Subject<Boolean> source;
    private CountDownTimer timer;
    private String addressBig;
    private String addressSmall;
    private int time = 0;

    public RequestOrderFragment() {
        this.source = PublishSubject.create();
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
        switch (addressValues.length){
            case 2:
                street = addressValues[0];
                town = addressValues[1];
                break;
            case 3:
                street = addressValues[0]+", "+addressValues[1];
                town = addressValues[2];
                break;
            default:
                street = AndarApplication.getInstance().getApplicationContext().getString(R.string.something_went_wrong);
                town = AndarApplication.getInstance().getApplicationContext().getString(R.string.something_went_wrong);
                break;
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
        initTimer(time);

        return view;
    }
    

    
    @OnClick(R.id.of_button_cancel)
    public void rejectOrder(){
        timer.cancel();
        source.onNext(false);
        source.onComplete();
    }
    
    @OnClick({R.id.of_progress_counter, R.id.of_progress_bar, R.id.of_pick_up_button})
    public void acceptOrder(){
        timer.cancel();
        source.onNext(true);
        source.onComplete();
    }


    private void initTimer(int time) {
        long interval = 1000;
        long remainingTime = time*1000;

        timer = new CountDownTimer(remainingTime,interval){

            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (((double)millisUntilFinished/(double)remainingTime)*100);
                progressbar.setProgress(progress);
                int seconds = (int) (millisUntilFinished/interval);
                counterView.setText(seconds+"");
            }

            @Override
            public void onFinish() {
                rejectOrder();
            }
        };
        timer.start();
    }

}
