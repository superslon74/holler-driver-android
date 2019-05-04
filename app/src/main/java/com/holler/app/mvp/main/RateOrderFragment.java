package com.holler.app.mvp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.holler.app.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RateOrderFragment extends Fragment{

    @BindView(R.id.of_star_1)
    public ImageView star1;
    @BindView(R.id.of_star_2)
    public ImageView star2;
    @BindView(R.id.of_star_3)
    public ImageView star3;
    @BindView(R.id.of_star_4)
    public ImageView star4;
    @BindView(R.id.of_star_5)
    public ImageView star5;

    public Subject<Integer> source;
    private ArrayList<ImageView> stars;
    private int rate=0;

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

        fillStars(1);
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
    
    @OnClick(R.id.of_submit_button)
    public void rateOrder(){
        source.onNext(rate);
        source.onComplete();
    }


}
