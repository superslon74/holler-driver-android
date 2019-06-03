package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.trips.modules.TripDetailsModule;
import com.holler.app.di.app.components.trips.modules.TripsModule;
import com.holler.app.mvp.details.TripDetailsPresenter;
import com.holler.app.mvp.details.DetailsView;
import com.holler.app.mvp.details.TripsPresenter;
import com.holler.app.mvp.details.TripsView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {TripsModule.class})
public interface TripsComponent {
    void inject(TripsView activityTrips);

    TripsPresenter getListsPresenter();
}
