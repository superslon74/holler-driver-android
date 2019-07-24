package com.pnrhunter.di.app.components;

import com.pnrhunter.di.app.ActivityScope;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.documents.modules.DocumentsModule;
import com.pnrhunter.mvp.documents.DocumentsPresenter;
import com.pnrhunter.mvp.documents.DocumentsView;

import dagger.Component;

@ActivityScope
@Component(dependencies = {AppComponent.class}, modules = {DocumentsModule.class})
public interface DocumentsComponent {
    void inject(DocumentsView activityEditProfile);

    DocumentsPresenter getPresenter();
}
