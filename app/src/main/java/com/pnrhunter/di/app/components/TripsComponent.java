package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.trips.modules.TripsModule;
import com.pnrhunter.mvp.details.TripsPresenter;
import com.pnrhunter.mvp.details.TripsView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {TripsModule.class})
public interface TripsComponent {
    void inject(TripsView activityTrips);

    TripsPresenter getListsPresenter();
}
