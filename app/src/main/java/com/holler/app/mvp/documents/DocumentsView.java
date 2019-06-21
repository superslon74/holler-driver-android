package com.holler.app.mvp.documents;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerDocumentsComponent;
import com.holler.app.di.app.components.documents.modules.DocumentsModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.utils.CustomActivity;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;

public class DocumentsView extends CustomActivity
        implements
        DocumentsPresenter.View,
        DocumentsListItem.OnDocumentViewInteractions {

    @BindView(R.id.da_documents_list)
    public View documentsListView;
    @BindView(R.id.da_uploading_container)
    public View uploadingContainer;
    @BindView(R.id.da_uploading_progress)
    public ProgressBar uploadingProgress;
    @BindView(R.id.da_uploading_caption)
    public TextView uploadingCaption;


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

    @Deprecated
    @Override
    public ObservableEmitter<Double> showProgress(RetrofitModule.ServerAPI.Document document) {
        uploadingContainer.setVisibility(View.VISIBLE);
        progress.put(document,0.0);
        return new ObservableEmitter<Double>() {
            @Override
            public void setDisposable(Disposable d) {

            }

            @Override
            public void setCancellable(Cancellable c) {

            }

            @Override
            public boolean isDisposed() {
                return false;
            }

            @Override
            public ObservableEmitter<Double> serialize() {
                return null;
            }

            @Override
            public boolean tryOnError(Throwable t) {
                return false;
            }

            @Override
            public void onNext(Double value) {
                progress.put(document,value);
                Logger.d(document.name+" "+value+" "+calculateUploadingProgress());
                uploadingProgress.setProgress(calculateUploadingProgress());
            }

            @Override
            public void onError(Throwable error) {

            }

            @Override
            public void onComplete() {
                uploadingContainer.setVisibility(View.GONE);
            }
        };
    }

    private static Map<RetrofitModule.ServerAPI.Document, Double> progress = new HashMap<>();

    private static int calculateUploadingProgress() {
        double res = 0;
        for(RetrofitModule.ServerAPI.Document d : progress.keySet()){
            res += progress.get(d);
        }
        res /= progress.keySet().size();
        res *= 100;
        return (int) res;
    }

    @Deprecated
    @Override
    public void hideProgress() {
        runOnUiThread(() -> {
            uploadingContainer.setVisibility(View.GONE);
        });
    }

    @Override
    public void onDocumentSelected(RetrofitModule.ServerAPI.Document document) {
        presenter.onDocumentChanged(document);
    }

}

