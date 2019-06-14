package com.holler.app.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.holler.app.R;

public class LoadingView extends LinearLayout {


    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_loading, this, true);
        hide();
    }

    public void show(){
        setVisibility(VISIBLE);
    }

    public void hide(){
        setVisibility(GONE);
    }

}
