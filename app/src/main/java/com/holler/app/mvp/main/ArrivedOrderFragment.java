package com.holler.app.mvp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.holler.app.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class ArrivedOrderFragment extends Fragment{

    public Subject<Boolean> source;

    public ArrivedOrderFragment() {
        this.source = PublishSubject.create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_map_order_arrived, container, false);
        ButterKnife.bind(this,view);

        return view;
    }
    

    
    @OnClick(R.id.of_button_cancel)
    public void cancelOrder(){
        source.onNext(false);
        source.onComplete();
    }

    @OnClick(R.id.of_button_arrived)
    public void acceptOrder(){
        source.onNext(true);
        source.onComplete();
    }


}
