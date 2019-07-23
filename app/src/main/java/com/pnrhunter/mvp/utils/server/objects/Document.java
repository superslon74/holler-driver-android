package com.pnrhunter.mvp.utils.server.objects;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Document implements Parcelable {
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

    public boolean isRequired() {
        return this.remoteUrl==null || "".equals(this.remoteUrl);
    }
}
