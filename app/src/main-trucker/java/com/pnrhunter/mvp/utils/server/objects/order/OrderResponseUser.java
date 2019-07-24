package com.pnrhunter.mvp.utils.server.objects.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderResponseUser implements Parcelable {
    @Expose(deserialize = false)
    @SerializedName("first_name")
    public String firstName;
    @Expose(deserialize = false)
    @SerializedName("last_name")
    public String lastName;
    @Expose(deserialize = false)
    @SerializedName("picture")
    public String avatar;
    @Expose(deserialize = false)
    @SerializedName("rating")
    public String rating;

    protected OrderResponseUser(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        avatar = in.readString();
        rating = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(avatar);
        dest.writeString(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderResponseUser> CREATOR = new Creator<OrderResponseUser>() {
        @Override
        public OrderResponseUser createFromParcel(Parcel in) {
            return new OrderResponseUser(in);
        }

        @Override
        public OrderResponseUser[] newArray(int size) {
            return new OrderResponseUser[size];
        }
    };

    public float getRating() {
        try {
            return Float.parseFloat(rating);
        }catch (NumberFormatException e){
            return -1;
        }
    }
}