package com.pnrhunter.mvp.documents;

import android.os.Bundle;

import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;

public class WaitingForApprovalView extends ExtendedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
    }

}
