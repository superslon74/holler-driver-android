package com.holler.app.mvp.documents;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.holler.app.activity.DocumentsActivity;
import com.holler.app.activity.WaitingForApproval;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.mvp.main.UserModel;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocumentsPresenter {

    private Context context;
    private View view;
    private RouterModule.Router router;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private RetrofitModule.ServerAPI serverAPI;
    private UserModel userModel;

    private Map<String, RetrofitModule.ServerAPI.Document> documents;


    public DocumentsPresenter(Context context,
                              View view,
                              RouterModule.Router router,
                              DeviceInfoModule.DeviceInfo deviceInfo,
                              RetrofitModule.ServerAPI serverAPI,
                              UserModel userModel) {
        this.context = context;
        this.view = view;
        this.router = router;
        this.deviceInfo = deviceInfo;
        this.serverAPI = serverAPI;
        this.userModel = userModel;
    }

    public void requestDocumentsList() {

        serverAPI
                .getRequiredDocuments(
                        userModel.getAuthHeader(),
                        deviceInfo.deviceType,
                        deviceInfo.deviceId,
                        deviceInfo.deviceToken)
                .toObservable()
                .flatMap(response -> {
                    documents = generateDocumentsMap(response);
                    if (checkAllDocumentsUploaded()) {
                        router.goToWaitingForApprovalScreen();
                    } else {
                        view.displayList(documents);
                    }
                    return Observable.empty();
                })
                .doOnError(throwable -> view.showMessage(throwable.getMessage()))
                .subscribe();


    }


    private Map<String, RetrofitModule.ServerAPI.Document> generateDocumentsMap(List<RetrofitModule.ServerAPI.Document> response) {
        Map<String, RetrofitModule.ServerAPI.Document> result = new HashMap<>();

        for (RetrofitModule.ServerAPI.Document d : response) {
            result.put(d.id, d);
        }

        return result;
    }


    private boolean checkAllDocumentsUploaded() {
        for (String dId : documents.keySet()) {
            RetrofitModule.ServerAPI.Document d = documents.get(dId);
            if (d.remoteUrl == null) return false;
        }
        return true;
    }

    public void uploadDocuments() {
        List<RetrofitModule.ServerAPI.Document> toUpload = new ArrayList<>();
        for (String id : documents.keySet()) {
            RetrofitModule.ServerAPI.Document d = documents.get(id);
            if (d.localUrl != null) {
                toUpload.add(d);
            }
        }
        
        Observable
                .fromIterable(toUpload)
                .flatMap(document -> {
                    return generateDocumentUploadingSource(document).toObservable();
                })
                .flatMap(uploadedDocument -> {
                    documents.put(uploadedDocument.id, uploadedDocument);
                    return Observable.empty();
                })
                .doFinally(() -> {
                    view.hideSpinner();
                    view.displayList(documents);
                    if(checkAllDocumentsUploaded()){
                        router.goToWaitingForApprovalScreen();
                        view.finish();
                    }else{
                        view.showMessage("Documents required");
                    }
                })
                .doOnSubscribe(disposable -> {
                    view.showSpinner();
                })
                .doOnError(throwable -> {
                    view.showMessage(throwable.getMessage());
                })
                .subscribe();



    }

    private Single<RetrofitModule.ServerAPI.Document> generateDocumentUploadingSource(RetrofitModule.ServerAPI.Document document){

        RequestBody deviceType = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.deviceType);
        RequestBody deviceId = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.deviceId);
        RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), deviceInfo.deviceToken);

        File photo = new File(document.localUrl);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), photo);
        String filename = photo.getName();
        MultipartBody.Part fileData = MultipartBody.Part.createFormData("document", filename, requestFile);

        Single<RetrofitModule.ServerAPI.Document> sendingDocumentSource = serverAPI.sendDocument(
                userModel.getAuthHeader(),
                deviceType,
                deviceId,
                deviceToken,
                document.id,
                fileData
        );

        return sendingDocumentSource;
    }

    public void onDocumentChanged(RetrofitModule.ServerAPI.Document document) {
        documents.put(document.id,document);
    }


    public interface View extends MessageDisplayer, Finishable, SpinnerShower {
        void displayList(Map<String, RetrofitModule.ServerAPI.Document> documents);
    }
}