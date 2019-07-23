package com.pnrhunter.mvp.documents;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.pnrhunter.mvp.utils.server.objects.Document;
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

public class DocumentsView extends ExtendedActivity
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        ButterKnife.bind(this);

        presenter.requestDocumentsList();
    }

    @OnClick(R.id.da_button_submit)
    public void submitUploading(){
        presenter.uploadDocuments();
    }


    public void displayList(Map<String, Document> documents) {
        FragmentManager fm = getSupportFragmentManager();
        for (String dn : documents.keySet()) {
            Document d = documents.get(dn);
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
    public ObservableEmitter<Double> showProgress(Document document) {
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

    private static Map<Document, Double> progress = new HashMap<>();

    private static int calculateUploadingProgress() {
        double res = 0;
        for(Document d : progress.keySet()){
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
    public void onDocumentSelected(Document document) {
        presenter.onDocumentChanged(document);
    }

}

