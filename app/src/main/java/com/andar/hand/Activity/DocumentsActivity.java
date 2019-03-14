package com.andar.hand.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.andar.hand.Fragment.DocumentsListItem;
import com.andar.hand.Helper.URLHelper;
import com.andar.hand.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
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

    private Map<String, Document> documents;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    DocumentsServerApi serverApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        documentsListView = (View) findViewById(R.id.documents_list);

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

        //request documents list
        //generate map
        //add fragments for documents
        //listen for upload btn clicked
        //check documents
        //send documents
    }


    private void requestDocumentsList() {

        Call<List<Document>> documentsListCall = serverApiClient.getDocuments(
                "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjExMywiaXNzIjoiaHR0cHM6Ly9hcGkuaG9sbGVyLnRheGkvYXBpL3Byb3ZpZGVyL29hdXRoL3Rva2VuIiwiaWF0IjoxNTUyNDcyMTc0LCJleHAiOjE1NTI4MzIxNzQsIm5iZiI6MTU1MjQ3MjE3NCwianRpIjoiV2t1bHMxWVBSSzNMVHFrUyJ9.kGaydANTlBUgUCwOM9bBOXkmcp0QCaHp4RthrcF2glg",
                "android",
                "23b50be39712afaa",
                "cqT1zB9UK10:APA91bGfTJEhf3PjJ-6u7NmkO8EzarpS2edaOChZp6d4arVla2lFf4jQW5ENku1SDU6hPuswUBS0YAVfBpBP9pw3jB2Y7mrVW_ZPq-Bm9YhASIyrHY5ukKjWA3DIIvuMWtqpajYHxHPt"
        );

        documentsListCall.enqueue(new Callback<List<Document>>() {
            @Override
            public void onResponse(@NonNull Call<List<Document>> call, @NonNull Response<List<Document>> response) {
                if (response.isSuccessful()) {
                    documents = generateDocumentsMap(response.body());
                    displayList();
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

//        Call<Document> cd = generateDocumentUploadingCall(document);
//        cd.enqueue(new Callback<Document>() {
//            @Override
//            public void onResponse(@NonNull Call<Document> call, @NonNull Response<Document> response) {
//                if (response.isSuccessful()) {
//                    Document uploadedDocument = response.body();
//                    documents.put(uploadedDocument.id,uploadedDocument);
//                } else {
//                    String s = response.raw().toString();
//                    Log.e("UnhandledApiErro", response.errorBody().toString());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<Document> call, @NonNull Throwable t) {
//                Log.e("AZAZA", "API Error ", t);
//            }
//        });
    }

    private void uploadDocuments(final Stack<Document> toUpload){
//        TODO: rewrite with javarx
        if(toUpload.size()==0){
            Toast.makeText(this, "Uploading finished.",Toast.LENGTH_LONG).show();
            displayList();
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

    private Call<Document> generateDocumentUploadingCall(Document document){
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjExMywiaXNzIjoiaHR0cHM6Ly9hcGkuaG9sbGVyLnRheGkvYXBpL3Byb3ZpZGVyL29hdXRoL3Rva2VuIiwiaWF0IjoxNTUyNDcyMTc0LCJleHAiOjE1NTI4MzIxNzQsIm5iZiI6MTU1MjQ3MjE3NCwianRpIjoiV2t1bHMxWVBSSzNMVHFrUyJ9.kGaydANTlBUgUCwOM9bBOXkmcp0QCaHp4RthrcF2glg");

        RequestBody deviceType = RequestBody.create(MediaType.parse("text/plain"), "android");
        RequestBody deviceId = RequestBody.create(MediaType.parse("text/plain"), "23b50be39712afaa");
        RequestBody deviceToken = RequestBody.create(MediaType.parse("text/plain"), "cqT1zB9UK10:APA91bGfTJEhf3PjJ-6u7NmkO8EzarpS2edaOChZp6d4arVla2lFf4jQW5ENku1SDU6hPuswUBS0YAVfBpBP9pw3jB2Y7mrVW_ZPq-Bm9YhASIyrHY5ukKjWA3DIIvuMWtqpajYHxHPt");

        File photo = new File(document.localUrl);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), photo);
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

