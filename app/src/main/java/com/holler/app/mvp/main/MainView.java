package com.holler.app.mvp.main;

import android.os.Bundle;

import com.holler.app.AndarApplication;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerMainComponent;
import com.holler.app.di.app.components.main.modules.MainScreenModule;
import com.holler.app.utils.CustomActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class MainView extends CustomActivity implements MainPresenter.View {

    @Inject
    public MainPresenter presenter;

    private void buildComponent(){
        AppComponent appComponent = AndarApplication.getInstance().component();
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .mainScreenModule(new MainScreenModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

    }
}
