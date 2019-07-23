package com.pnrhunter.mvp.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnrhunter.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StageView extends LinearLayout {
    @BindView(R.id.of_stage_1) protected TextView stage1;
    @BindView(R.id.of_stage_2) protected TextView stage2;
    @BindView(R.id.of_stage_3) protected TextView stage3;

    private List<TextView> stages;

    public StageView(Context context) {
        this(context,null);
    }

    public StageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public StageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }



    private void init(){
        inflate(getContext(), R.layout.layout_stage,this);
        View rootView = getRootView();
        ButterKnife.bind(rootView);
        stages = new ArrayList<>();
        stages.add(stage1);
        stages.add(stage2);
        stages.add(stage3);
    }

    public void setStage(int pos){
        for(int i=0;i<stages.size();i++){
            stages.get(i).setEnabled(i<pos);
        }
    }




}
