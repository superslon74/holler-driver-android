package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.details.mudules.DetailsModule;
import com.holler.app.di.app.components.profile.modules.ProfileModule;
import com.holler.app.mvp.details.DetailsPresenter;
import com.holler.app.mvp.details.DetailsView;
import com.holler.app.mvp.profile.EditProfileView;
import com.holler.app.mvp.profile.ProfilePresenter;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {DetailsModule.class})
public interface DetailsComponent {
    void inject(DetailsView activityDetails);

    DetailsPresenter getPresenter();
}
