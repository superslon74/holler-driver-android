package com.holler.app.mvp.main;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holler.app.R;

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
    
    public Subject<Boolean> source;
    private CountDownTimer timer;

    public RequestOrderFragment() {
        this.source = PublishSubject.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map_order_request, container, false);
        ButterKnife.bind(this,view);
        initTimer();

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


    private void initTimer() {
        long time = 30000;
        long interval = 1000;

        timer = new CountDownTimer(time,interval){

            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (((double)millisUntilFinished/(double)time)*100);
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
