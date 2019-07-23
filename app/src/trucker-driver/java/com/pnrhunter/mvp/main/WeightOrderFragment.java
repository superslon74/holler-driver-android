package com.pnrhunter.mvp.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class WeightOrderFragment extends Fragment{

    public Observable<Boolean> source;
    private ObservableEmitter<Boolean> emitter;

    public WeightOrderFragment() {
        this.source = Observable.create(emitter -> {
            WeightOrderFragment.this.emitter = emitter;
        });
    }



    public static WeightOrderFragment newInstance(RequestedOrderResponse order) {
        WeightOrderFragment fragment = new WeightOrderFragment();
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
        View view = inflater.inflate(R.layout.fragment_order_weight, container, false);
        ButterKnife.bind(this,view);

        view.setOnTouchListener((v, event) -> true);
        return view;
    }




}
