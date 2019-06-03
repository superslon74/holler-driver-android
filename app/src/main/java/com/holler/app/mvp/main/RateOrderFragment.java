package com.holler.app.mvp.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holler.app.R;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RateOrderFragment extends Fragment{
    private static final int TIME_TO_SUBMIT = 7;

    @BindView(R.id.of_star_1)  public ImageView star1;
    @BindView(R.id.of_star_2)  public ImageView star2;
    @BindView(R.id.of_star_3)  public ImageView star3;
    @BindView(R.id.of_star_4)  public ImageView star4;
    @BindView(R.id.of_star_5)  public ImageView star5;

    @BindView(R.id.of_progress_bar)  public ProgressBar progressBarView;
    @BindView(R.id.of_progress_counter)  public TextView progressCounterView;


    public Subject<Integer> source;
    private ArrayList<ImageView> stars;
    private int rate=5;
    private Disposable timer;

    public RateOrderFragment() {
        this.source = PublishSubject.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map_order_rate, container, false);
        ButterKnife.bind(this,view);

        stars = new ArrayList<>();
        stars.add(star1);
        stars.add(star2);
        stars.add(star3);
        stars.add(star4);
        stars.add(star5);

        fillStars(rate);
        timer = createTimer(TIME_TO_SUBMIT).subscribe();

        return view;
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
                        progressBarView.setProgress(progress);
                        int seconds = (int) (time-pastTime);
                        progressCounterView.setText(seconds+"");
                    });
                })
                .doFinally(() -> {
                    rateOrder();
                });

    }

    private void runOnUiThread(Runnable r){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(r);
    }
    

    @OnClick({
            R.id.of_star_1,
            R.id.of_star_2,
            R.id.of_star_3,
            R.id.of_star_4,
            R.id.of_star_5
    })
    public void setRate(View view){
        for(int i=0;i<stars.size();i++){
            stars.get(i).setImageDrawable(getResources().getDrawable(R.drawable.ic_star_yellow_border, getContext().getTheme()));
        }

        switch (view.getId()){
            case R.id.of_star_1: rate=1; break;
            case R.id.of_star_2: rate=2; break;
            case R.id.of_star_3: rate=3; break;
            case R.id.of_star_4: rate=4; break;
            case R.id.of_star_5: rate=5; break;
        }

        fillStars(rate);
    }

    private void fillStars(int toStar){
        for(int i=0;i<toStar;i++){
            stars.get(i).setImageDrawable(getResources().getDrawable(R.drawable.ic_star_yellow_filled, getContext().getTheme()));
        }
    }
    
    @OnClick({
            R.id.of_submit_button,
            R.id.of_progress_bar,
            R.id.of_progress_counter
    })
    public void rateOrder(){
        timer.dispose();
        source.onNext(rate);
        source.onComplete();
    }


}
