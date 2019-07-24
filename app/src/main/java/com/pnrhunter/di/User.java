package com.pnrhunter.di;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pnrhunter.di.app.modules.RetrofitModule;

public class User implements Parcelable {
    public static final String GENDER_MALE="MALE";
    public static final String GENDER_FEMALE="FEMALE";


    @Expose
    @SerializedName("id")
    public String id;
    @Expose
    @SerializedName("device_type")
    public String deviceType;
    @Expose
    @SerializedName("device_id")
    public String deviceId;
    @Expose
    @SerializedName("device_token")
    public String deviceToken;
    @Expose
    @SerializedName("login_by")
    public String loggedBy;
    @Expose
    @SerializedName("first_name")
    public String firstName;
    @Expose
    @SerializedName("last_name")
    public String lastName;
    @Expose
    @SerializedName("gender")
    public String gender;
    @Expose
    @SerializedName("mobile")
    public String mobile;
    @Expose
    @SerializedName("avatar")
    public String avatar;
    @Expose
    @SerializedName("rating")
    public String rating;

    @Expose
    @SerializedName("status")
    public String status;
    @Expose
    @SerializedName("currency")
    public String currency;
    @Expose
    @SerializedName("sos")
    public String sos;
    @Expose
    @SerializedName("service")
    public ServiceType service;

    @Expose
    @SerializedName("email")
    public String email;
    @Expose
    @SerializedName("password")
    public String password;
    @Expose
    @SerializedName("password_confirmation")
    public String passwordConfirmation;


    public User() {
    }


    protected User(Parcel in) {
        id = in.readString();
        deviceType = in.readString();
        deviceId = in.readString();
        deviceToken = in.readString();
        loggedBy = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        gender = in.readString();
        mobile = in.readString();
        avatar = in.readString();
        rating = in.readString();
        status = in.readString();
        currency = in.readString();
        sos = in.readString();
        email = in.readString();
        password = in.readString();
        passwordConfirmation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(deviceType);
        dest.writeString(deviceId);
        dest.writeString(deviceToken);
        dest.writeString(loggedBy);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(gender);
        dest.writeString(mobile);
        dest.writeString(avatar);
        dest.writeString(rating);
        dest.writeString(status);
        dest.writeString(currency);
        dest.writeString(sos);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(passwordConfirmation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setServiceName(String name){
        if(service == null)
            service = new ServiceType();
        service.name = name;
    }

    public String getServiceName(){
        if(service == null)
            return null;
        return service.name;
    }

    public String getCurrency(){
        if(currency!=null && !currency.isEmpty()) return currency;
        return "$";
    }

    public String getAvatarUrl(){
        return RetrofitModule.ServerAPI.BASE_URL + "storage/" + this.avatar;
    }

    public boolean isMale(){
        return GENDER_MALE.equals(gender);
    }

}