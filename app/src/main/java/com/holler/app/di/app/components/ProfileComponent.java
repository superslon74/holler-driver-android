package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.profile.modules.ProfileModule;
import com.holler.app.di.app.components.register.modules.RegisterModule;
import com.holler.app.mvp.profile.EditProfileView;
import com.holler.app.mvp.profile.ProfilePresenter;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {ProfileModule.class})
public interface ProfileComponent {
    void inject(EditProfileView activityEditProfile);

    ProfilePresenter getPresenter();
}
