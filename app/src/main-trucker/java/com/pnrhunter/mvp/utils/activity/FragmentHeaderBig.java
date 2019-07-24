package com.pnrhunter.mvp.utils.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pnrhunter.R;
import com.orhanobut.logger.Logger;
import com.pnrhunter.mvp.utils.router.AbstractRouter;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentHeaderBig extends DaggerFragment {

    @BindView(R.id.h_title)
    protected TextView headerTextView;

    private String title = "DefaultTitle";

    @Inject
    public AbstractRouter router;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_big, container, true);
        ButterKnife.bind(this,view);
        headerTextView.setText(title);
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FragmentHeaderBig);

        CharSequence string = a.getString(R.styleable.FragmentHeaderBig_hb_title);
        int reference = a.getResourceId(R.styleable.FragmentHeaderBig_hb_title, -1);
        if (string != null) {
            title = string.toString();
            Logger.v("String received, title:  " + title);
        } else {
            Logger.v("No string received");
        }

        if (reference != -1) {
            title = context.getString(reference);
            Logger.v("Reference received, title:  " + title);
        } else {
            Logger.v("No reference received");
        }

        a.recycle();
    }

    @OnClick(R.id.h_back_button)
    public void goBack(){
        router.goBack();
    }


}
