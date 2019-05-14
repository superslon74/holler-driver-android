package com.holler.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.modules.RouterModule;
import com.orhanobut.logger.Logger;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentHeaderMini extends Fragment {

    @BindView(R.id.hm_title)
    protected TextView headerTextView;

    private String title = "LOL";

    @Inject
    public RouterModule.Router router;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_mini, container, false);
        ButterKnife.bind(this,view);
        headerTextView.setText(title);
        AndarApplication.getInstance().component().inject(this);
        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FragmentHeaderMini);

        CharSequence string = a.getString(R.styleable.FragmentHeaderMini_hm_title);
        int reference = a.getResourceId(R.styleable.FragmentHeaderMini_hm_title, -1);
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

    @OnClick(R.id.lhm_button_back)
    public void goBack(){
        router.goBack();
    }


}
