package com.holler.app.mvp.details;

import android.content.Intent;
import android.os.Bundle;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerDetailsComponent;
import com.holler.app.di.app.components.details.mudules.DetailsModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.utils.CustomActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class DetailsView extends CustomActivity implements DetailsPresenter.View {

    public static final String ARG_TYPE = "type";
    public static final String ARG_ID = "id";
    public static final String TYPE_PAST_TRIPS = "past_trips";
    public static final String TYPE_UPCOMING_TRIPS = "upcoming_trips";

    @Inject protected DetailsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        buildComponent();
        ButterKnife.bind(this);
        String id = null;
        String type = null;
        try{
            Intent intent = getIntent();
            type = intent.getExtras().getString(ARG_TYPE);
            id = intent.getExtras().getString(ARG_ID);
            presenter.requestData(type,id);
        }catch (NullPointerException e){

        }
    }

    private void buildComponent() {
        AppComponent component = AndarApplication.getInstance().component();
        DaggerDetailsComponent.builder()
                .appComponent(component)
                .detailsModule(new DetailsModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void setView(RetrofitModule.ServerAPI.OrderResponse order) {

    }
}
