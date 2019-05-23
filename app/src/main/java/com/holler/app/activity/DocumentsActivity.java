package com.holler.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.holler.app.mvp.documents.DocumentsListItem;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Helper.URLHelper;
import com.holler.app.R;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RetrofitModule.ServerAPI.Document;
import com.holler.app.utils.CustomActivity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

@Deprecated
public class DocumentsActivity
        extends CustomActivity
        implements DocumentsListItem.OnDocumentViewInteractions {

    @BindView(R.id.da_documents_list)
    public View documentsListView;

    private Map<String, Document> documents;


    DocumentsServerApi serverApiClient;

    private String authToken;
    private String deviceType;
    private String deviceId;
    private String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        ButterKnife.bind(this);


        initUserData();
        initServerAPIClient();

//        onCheckPermissions();
        requestDocumentsList();

    }

    @OnClick(R.id.da_button_submit)
    public void submitUploading(){
        Stack<Document> toUpload = new Stack<>();
        for(String id : documents.keySet() ){
            Document d = documents.get(id);
            if(d.localUrl!=null){
                toUpload.push(d);
            }
        }
        uploadDocuments(toUpload);
    }

    @OnClick(R.id.da_back_button)
    public void goBack(){
        onBackPressed();
    }

    //TODO: remove with di
    private void initUserData(){
        authToken = "Bearer " + SharedHelper.getKey(this, "access_token");
        try {
            deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            deviceId = "COULD NOT GET UDID";
        }
        deviceToken = SharedHelper.getKey(this, "device_token");
        deviceType = "android";

    }

    private void initServerAPIClient(){
        ConnectionPool pool = new ConnectionPool(10, 10000, TimeUnit.MILLISECONDS);

        OkHttpClient httpClient = new OkHttpClient
                .Builder()
                .cache(null)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectionPool(pool)
                .build();

        serverApiClient = new Retrofit
                .Builder()
                .client(httpClient)
                .baseUrl(URLHelper.base)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DocumentsServerApi.class);
    }

    private void requestDocumentsList() {

        Call<List<Document>> documentsListCall = serverApiClient.getDocuments(
                authToken,
                deviceType,
                deviceId,
                deviceToken
        );

        documentsListCall.enqueue(new Callback<List<Document>>() {
            @Override
            public void onResponse(@NonNull Call<List<Document>> call, @NonNull Response<List<Document>> response) {
                if (response.isSuccessful()) {
                    documents = generateDocumentsMap(response.body());
                    if(checkAllDocumentsUploaded()){
                        Intent intent = new Intent(DocumentsActivity.this, WaitingForApproval.class);
                        startActivity(intent);
                        finish();
                    }else{
                        displayList();
                    }
                } else {
                    Log.e("UnhandledApiErro", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RetrofitModule.ServerAPI.Document>> call, @NonNull Throwable t) {
                Log.e("AZAZA", "API Error ", t);
            }
        });

    }

    private boolean checkAllDocumentsUploaded(){
        for(String dId : documents.keySet()){
            Document d = documents.get(dId);
            if(d.remoteUrl==null) return false;
        }
        return true;
    }

    private Map<String, Document> generateDocumentsMap(List<Document> responce) {
        Map<String, Document> result = new HashMap<>();

        for (Document d : responce) {
            result.put(d.id, d);
        }

        return result;
    }

    private void displayList() {
        FragmentManager fm = getSupportFragmentManager();
        for (String dn : documents.keySet()) {
            Document d = documents.get(dn);
//            Fragment f = DocumentsListItem.newInstance(d);
            String tag = "documentListItem" + d.id;
            Fragment foundByTag = fm.findFragmentByTag(tag);
            if (foundByTag != null) {
                fm.beginTransaction().remove(foundByTag).commit();
            }
//            fm.beginTransaction().add(documentsListView.getId(), f, tag).commit();

        }
    }

    @Override
    public void onDocumentSelected(RetrofitModule.ServerAPI.Document document) {
        documents.put(document.id,document);
    }



    private void uploadDocuments(final Stack<Document> toUpload){
//        TODO: rewrite with javarx
        showSpinner();
        if(toUpload.size()==0){
            onDocumentsUploaded();
            return;
        }

        Document document = toUpload.pop();
        Call<Document> uploadingDocumentCall = generateDocumentUploadingCall(document);

        uploadingDocumentCall.enqueue(new Callback<Document>() {
            @Override
            public void onResponse(@NonNull Call<Document> call, @NonNull Response<Document> response) {
                if (response.isSuccessful()) {
                    Document uploadedDocument = response.body();
                    documents.put(uploadedDocument.id,uploadedDocument);
                } else {
                    String s = response.raw().toString();
                    Log.e("UnhandledApiErro", response.errorBody().toString());
                }
                uploadDocuments(toUpload);
            }

            @Override
            public void onFailure(@NonNull Call<Document> call, @NonNull Throwable t) {
                Log.e("AZAZA", "API Error ", t);
                uploadDocuments(toUpload);
            }
        });

    }

    private void onDocumentsUploaded(){
        hideSpinner();

        Toast.makeText(this, "Uploading finished.",Toast.LENGTH_LONG).show();
        displayList();


        if(checkAllDocumentsUploaded()) {
            Intent intent = new Intent(this, WaitingForApproval.class);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(this, "Documents required", Toast.LENGTH_LONG).show();
        }
    }

    private Call<Document> generateDocumentUploadingCall(Document document){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);

        RequestBody deviceType = RequestBody.create(MediaType.parse("text/plain"), this.deviceType);
        RequestBody deviceId = RequestBody.create(MediaType.parse("text/plain"), this.deviceId);
        RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), this.deviceToken);

        File photo = new File(document.localUrl);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), photo);
        String filename = photo.getName();
        MultipartBody.Part fileData = MultipartBody.Part.createFormData("document", filename, requestFile);

        Call<Document> sendingDocumentCall = serverApiClient.sendDocument(
                headers,
                deviceType,
                deviceId,
                deviceToken,
                document.id,
                fileData
        );

        return sendingDocumentCall;
    }


    private interface DocumentsServerApi {

        @GET("api/provider/documents")
        Call<List<Document>> getDocuments(
                @Header("Authorization") String header,
                @Query("device_type") String devicetype,
                @Query("device_id") String deviceId,
                @Query("device_token") String deviceToken
        );

        @Multipart
        @POST("api/provider/documents/{id}")
        Call<Document> sendDocument(
                @HeaderMap Map<String, String> token,
                @Part("device_type") RequestBody devicetype,
                @Part("device_id") RequestBody deviceId,
                @Part("device_token") RequestBody deviceToken,

                @Path("id") String documentId,

                @Part MultipartBody.Part document
        );

    }
}

