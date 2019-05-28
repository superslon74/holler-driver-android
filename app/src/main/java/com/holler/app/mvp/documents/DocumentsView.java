package com.holler.app.mvp.documents;

import android.os.Bundle;
import android.view.View;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerDocumentsComponent;
import com.holler.app.di.app.components.documents.modules.DocumentsModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.utils.CustomActivity;

import java.util.Map;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DocumentsView extends CustomActivity
        implements
        DocumentsPresenter.View,
        DocumentsListItem.OnDocumentViewInteractions {

    @BindView(R.id.da_documents_list)
    public View documentsListView;


    @Inject
    protected DocumentsPresenter presenter;

    private void buildComponent() {
        AppComponent component = AndarApplication.getInstance().component();
        DaggerDocumentsComponent.builder()
                .appComponent(component)
                .documentsModule(new DocumentsModule(this))
                .build()
                .inject(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        ButterKnife.bind(this);
        buildComponent();

        presenter.requestDocumentsList();

    }


    @OnClick(R.id.da_button_submit)
    public void submitUploading(){
        presenter.uploadDocuments();
    }


    public void displayList(Map<String, RetrofitModule.ServerAPI.Document> documents) {
        FragmentManager fm = getSupportFragmentManager();
        for (String dn : documents.keySet()) {
            RetrofitModule.ServerAPI.Document d = documents.get(dn);
            Fragment f = DocumentsListItem.newInstance(d);
            String tag = "documentListItem" + d.id;
            Fragment foundByTag = fm.findFragmentByTag(tag);
            if (foundByTag != null) {
                fm.beginTransaction().remove(foundByTag).commit();
            }
            fm.beginTransaction().add(documentsListView.getId(), f, tag).commit();

        }
    }

    @Override
    public void onDocumentSelected(RetrofitModule.ServerAPI.Document document) {
        presenter.onDocumentChanged(document);
    }

}

