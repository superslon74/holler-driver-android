package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.profile.modules.ProfileModule;
import com.pnrhunter.mvp.profile.EditProfileView;
import com.pnrhunter.mvp.profile.ProfilePresenter;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {ProfileModule.class})
public interface ProfileComponent {
    void inject(EditProfileView activityEditProfile);

    ProfilePresenter getPresenter();
}
