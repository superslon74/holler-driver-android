package com.pnrhunter.mvp.main;

import android.os.Bundle;

import com.orhanobut.logger.Logger;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;

public class MainView extends ExtendedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("AZAZA", "Main view started");
    }
}
