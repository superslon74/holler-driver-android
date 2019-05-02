package com.holler.app.mvp.main;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holler.app.R;
import com.orhanobut.logger.Logger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrderFragment extends Fragment {

    private RequestViewHolder requestView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map_order, container, false);
        ButterKnife.bind(this,view);
//        initTimer();

        requestView = new RequestViewHolder(view);

        return view;
    }

    private class RequestViewHolder{
        @BindView(R.id.of_progress_bar)
        public ProgressBar progressbar;
        @BindView(R.id.of_progress_counter)
        public TextView counterView;
        @OnClick(R.id.of_button_cancel)
        public void cancelOrder(){
            try{
                getFragmentManager()
                        .beginTransaction()
                        .remove(OrderFragment.this)
                        .commit();
            }catch (IllegalStateException | NullPointerException e){
                Logger.e(e,"Can't cancel order");
            }

        }

        public RequestViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initTimer() {
        long time = 30000;
        long interval = 1000;

        CountDownTimer timer = new CountDownTimer(time,interval){

            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (((double)millisUntilFinished/(double)time)*100);
                requestView.progressbar.setProgress(progress);
                int seconds = (int) (millisUntilFinished/interval);
                requestView.counterView.setText(seconds+"");
            }

            @Override
            public void onFinish() {
                requestView.cancelOrder();
            }
        };
        timer.start();
    }


}
