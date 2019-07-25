package com.pnrhunter.mvp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;

public class RateOrderFragment extends Fragment{

    @BindView(R.id.of_star_1)  public ImageView star1;
    @BindView(R.id.of_star_2)  public ImageView star2;
    @BindView(R.id.of_star_3)  public ImageView star3;
    @BindView(R.id.of_star_4)  public ImageView star4;
    @BindView(R.id.of_star_5)  public ImageView star5;

    @BindView(R.id.of_submit_button) protected TextView buttonSubmit;

    public Observable<Boolean> source;
    private ObservableEmitter<Boolean> emitter;

    private List<ImageView> stars;
    private int rate=5;
    private Disposable timer;

    public RateOrderFragment() {
        this.source = Observable.create(emitter -> {
            RateOrderFragment.this.emitter = emitter;
        });
    }



    public static RateOrderFragment newInstance(RequestedOrderResponse order) {
        RateOrderFragment fragment = new RateOrderFragment();
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
        View view = inflater.inflate(R.layout.fragment_order_rate, container, false);
        ButterKnife.bind(this,view);
        stars = new ArrayList<>();
        stars.add(star1);
        stars.add(star2);
        stars.add(star3);
        stars.add(star4);
        stars.add(star5);

        fillStars(rate);
        view.setOnTouchListener((v, event) -> true);
        return view;
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
            stars.get(i).setImageDrawable(getResources().getDrawable(R.drawable.vec_star_yellow_border, getContext().getTheme()));
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
            stars.get(i).setImageDrawable(getResources().getDrawable(R.drawable.vec_star_yellow_filled, getContext().getTheme()));
        }
    }


}
