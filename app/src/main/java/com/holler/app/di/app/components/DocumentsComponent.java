package com.holler.app.di.app.components;

import com.holler.app.di.app.ActivityScope;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.documents.modules.DocumentsModule;
import com.holler.app.di.app.components.profile.modules.ProfileModule;
import com.holler.app.mvp.documents.DocumentsPresenter;
import com.holler.app.mvp.documents.DocumentsView;
import com.holler.app.mvp.profile.EditProfileView;
import com.holler.app.mvp.profile.ProfilePresenter;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {DocumentsModule.class})
public interface DocumentsComponent {
    void inject(DocumentsView activityEditProfile);

    DocumentsPresenter getPresenter();
}
