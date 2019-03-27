package com.holler.app.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.holler.app.Fragment.DocumentsListItem;
import com.holler.app.Helper.CustomDialog;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.Helper.URLHelper;
import com.holler.app.R;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

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

public class DocumentsActivity
        extends AppCompatActivity
        implements DocumentsListItem.OnDocumentViewInteractions {

    private static final int REQUEST_PERMISSIONS_CODE = 6411;

    private View documentsListView;
    private CustomDialog spinner;

    private Map<String, Document> documents;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    DocumentsServerApi serverApiClient;

    private String authToken;
    private String deviceType;
    private String deviceId;
    private String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        documentsListView = (View) findViewById(R.id.documents_list);

        initUserData();
        initServerAPIClient();

        onCheckPermissions();
        requestDocumentsList();

        View submitButton = (View) findViewById(R.id.submitUploading);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stack<Document> toUpload = new Stack<>();
                for(String id : documents.keySet() ){
                    Document d = documents.get(id);
                    if(d.localUrl!=null){
                        toUpload.push(d);
                    }
                }
                uploadDocuments(toUpload);
            }
        });

    }

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
            public void onFailure(@NonNull Call<List<Document>> call, @NonNull Throwable t) {
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
    public void onDocumentSelected(Document document) {
        documents.put(document.id,document);

    }

    private void showSpinner() {
        if (spinner == null) {
             spinner = new CustomDialog(this);
        }
        spinner.setCancelable(false);
        spinner.show();
    }

    private void hideSpinner() {
        spinner.dismiss();
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

    @Override
    public boolean onCheckPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (arePermissionsGranted()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onPermissionsNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "This feature doesn't working without requested permissions..", Toast.LENGTH_LONG).show();
            requestMultiplePermissions();
        } else {
            Toast.makeText(this, "App needs access to camera and storage", Toast.LENGTH_LONG).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsGranted() {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions() {
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), REQUEST_PERMISSIONS_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(permissions[i])) {
                        new AlertDialog.Builder(this)
                                .setMessage("These permissions needed to send select documents on your device.")
                                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestMultiplePermissions();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                    return;
                }
            }
            //all is good, continue flow
        }
    }

    public static class Document implements Parcelable {

        @Expose(serialize = false)
        @SerializedName("id")
        public String id;
        @Expose(serialize = false)
        @SerializedName("name")
        public String name;
        @Expose(serialize = false)
        @SerializedName("url")
        public String remoteUrl;

        public String localUrl;

        public Document() {
        }

        public Document(String id, String name, String remoteUrl, String localUrl) {
            this.id = id;
            this.name = name;
            this.remoteUrl = remoteUrl;
            this.localUrl = localUrl;
        }

        protected Document(Parcel in) {
            id = in.readString();
            name = in.readString();
            remoteUrl = in.readString();
            localUrl = in.readString();
        }

        public static final Creator<Document> CREATOR = new Creator<Document>() {
            @Override
            public Document createFromParcel(Parcel in) {
                return new Document(in);
            }

            @Override
            public Document[] newArray(int size) {
                return new Document[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(remoteUrl);
            dest.writeString(localUrl);
        }

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

