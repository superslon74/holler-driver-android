package com.andar.hand.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.andar.hand.Fragment.DocumentsListItem;
import com.andar.hand.R;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentsActivity extends AppCompatActivity implements DocumentsListItem.OnDocumentViewInteractions{

    private static final int REQUEST_READ_PERMISSIONS_CODE = 937;

    private View documentsListView;

    private Map<String,Document> documents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        documentsListView = (View)findViewById(R.id.documents_list);

        requestDocumentsList();

        //request documents list
        //generate map
        //add fragments for documents
        //listen for upload btn clicked
        //check documents
        //send documents
    }





    private void requestDocumentsList(){
//        TODO: make request
//        TODO: generate and display list on response

        documents = generateDocumentsMap(null);
        displayList();
    }

    private Map<String, Document> generateDocumentsMap(JsonObject responce){
        Map<String,Document> result = new HashMap<>();
//        TODO: parse response
        Document[] docs = new Document[]{
                new Document("111111","Passport",null, null),
                new Document("222222","Driver Card","https://dps.mn.gov/divisions/dvs/PublishingImages/new-cards/mn-adult-dl.jpg",null),
                new Document("333333","Medicine Card",null,null)
        };

        for(Document d : docs){
            result.put(d.id,d);
        }

        return result;
    }

    private void displayList(){
        for(String dn : documents.keySet()){
            Document d = documents.get(dn);
            Fragment f = DocumentsListItem.newInstance(d);
            getSupportFragmentManager().beginTransaction().add(documentsListView.getId(),f,"frag"+d.name).commit();
        }
    }

    @Override
    public void onDocumentSelected(Document document) {
        Log.d("AZAZAZ","Document selected: ("+document.id+","+document.name+" ) remote: "+document.remoteUrl+"; local: "+document.localUrl);
    }


    public static class Document implements Parcelable {
        public String id;
        public String name;
        public String remoteUrl;
        public String localUrl;

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

}
