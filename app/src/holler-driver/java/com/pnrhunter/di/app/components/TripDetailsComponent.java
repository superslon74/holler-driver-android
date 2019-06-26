package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.trips.modules.TripDetailsModule;
import com.pnrhunter.mvp.details.DetailsView;
import com.pnrhunter.mvp.details.TripDetailsPresenter;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {TripDetailsModule.class})
public interface TripDetailsComponent {
    void inject(DetailsView activityDetails);

    TripDetailsPresenter getDetailsPresenter();
}
